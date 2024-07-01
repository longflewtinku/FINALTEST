package com.linkly.libengine.engine.reporting;

import static com.linkly.libengine.engine.EngineManager.TransType.RECONCILIATION;
import static com.linkly.libengine.engine.transactions.properties.TCard.CardIssuer.UNKNOWN;

import com.linkly.libengine.engine.EngineManager;
import com.linkly.libengine.engine.transactions.TransRec;
import com.linkly.libengine.engine.transactions.TransRecManager;
import com.linkly.libmal.global.util.Util;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import timber.log.Timber;

public class TimePeriodTotals {

    private long reportWindowStart; // For Till: defining window for local calculations (surcharge and tips)
    private long reportWindowEnd;

    public long getReportWindowStart() {
        return reportWindowStart;
    }
    public long getReportWindowEnd() {
        return reportWindowEnd;
    }

    private final boolean tipsEnabled;
    private final boolean surchargeEnabled;
    private final String terminalId;
    private final String autoSettlementTime;
    private final String autoSettlementTimeWindow;

    public TimePeriodTotals(boolean tipsEnabled, boolean surchargeEnabled, String terminalId, String autoSettlementTime, String autoSettlementTimeWindow ) {
        this.tipsEnabled = tipsEnabled;
        this.surchargeEnabled = surchargeEnabled;
        this.terminalId = terminalId;
        this.autoSettlementTime = autoSettlementTime;
        this.autoSettlementTimeWindow = autoSettlementTimeWindow;
    }

    /**
     * calculateTimePeriodTotals()
     * Calculate the following totals for transactions in given window
     * Scheme based surcharge
     * Total surcharge
     * Total tips
     */
    @SuppressWarnings("java:S3776")
    public void calculateTimePeriodTotals(Reconciliation reconciliation) {
        reconciliation.setTips(new Amounts(0L, 0L, 0L, 0L));
        reconciliation.setSurcharge(new Amounts(0L, 0L, 0L, 0L));
        HashMap<String, Reconciliation.CardSchemeTotals> schemeTotals = new HashMap<>();
        reconciliation.setPreviousSchemeTotals(schemeTotals); // save empty here

        if (reportWindowStart == 0 || reportWindowEnd == 0) {
            return;
        }

        // find all transactions for the given time period. Use transaction finished Date Time stamp for fetching transactions for local Settlement calculations
        // if Settlement Date changed (Host Forced Settlement): Advices' finished date/time updated when sent to host, those transactions will go to the new reconciliation
        List<TransRec> allTrans = TransRecManager.getInstance().getTransRecDao().findByDateRangeFinishedApproved(reportWindowStart, reportWindowEnd);

        if (allTrans == null) {
            return;
        }


        for (TransRec trans : allTrans) {
            if (!terminalId.equals(trans.getAudit().getTerminalId())) {
                continue;
            }

            EngineManager.TransType transType = trans.getTransType();

            if (trans.approvedAndIncludeInReconciliation()
                    && (transType.transClass == EngineManager.TransClass.DEBIT || transType.transClass == EngineManager.TransClass.CREDIT)) {

                if (tipsEnabled && trans.getAmounts().getTip() > 0) {
                    reconciliation.getTips().updateAmounts(0, 1, trans.getAmounts().getTip());
                }

                if (surchargeEnabled && trans.getAmounts().getSurcharge() > 0) {
                    reconciliation.getSurcharge().updateAmounts(0, 1, trans.getAmounts().getSurcharge());
                }

                String schemeName = (trans.getCard() != null) ? trans.getCard().getCardIssuer().getDisplayName() : UNKNOWN.getDisplayName();
                Reconciliation.CardSchemeTotals total = schemeTotals.get(schemeName);
                if (total != null) {
                    addTransToSchemeTotals(total, trans, surchargeEnabled);
                } else {
                    Reconciliation.CardSchemeTotals newEntry = new Reconciliation.CardSchemeTotals();
                    addTransToSchemeTotals(newEntry, trans, surchargeEnabled);
                    schemeTotals.put(newEntry.name, newEntry);
                }
            }
        }

        //Store card scheme totals here
        reconciliation.setPreviousSchemeTotals(schemeTotals);
    }

    private static void addTransToSchemeTotals(Reconciliation.CardSchemeTotals totals, TransRec trans, boolean surchargeSupported) {

        if (!trans.isApproved() || trans.isReversal() || trans.getProtocol().getReversalCount() > 0)
            return;

        totals.name = (trans.getCard() != null) ? trans.getCard().getCardIssuer().getDisplayName() : UNKNOWN.getDisplayName();
        totals.totalCount++;

        EngineManager.TransClass transClass = trans.getTransType().getTransClass();

        if (transClass == EngineManager.TransClass.DEBIT) {
            // e.g. sale, cashout
            totals.totalAmount += trans.getAmounts().getTotalAmount();
        } else {
            // e.g. refund
            totals.totalAmount -= trans.getAmounts().getTotalAmount();
        }

        // increment purchase/cashout/refund totals
        if( trans.isSale() ) {
            totals.purchaseAmount += trans.getAmounts().getTotalAmount();
            totals.purchaseCount++;
        } else if( trans.isCash() ) {
            totals.cashoutAmount += trans.getAmounts().getTotalAmount();
            totals.cashoutCount++;
        } else if( trans.isRefund() ) {
            totals.refundAmount += trans.getAmounts().getTotalAmount();
            totals.refundCount++;
        } else if( trans.isCashback() ) {
            // increment both purchase and cashout amount for pwcb
            totals.purchaseAmount += trans.getAmounts().getTotalAmountWithoutCashback();
            totals.purchaseCount++;
            totals.cashoutAmount += trans.getAmounts().getCashbackAmount();
            totals.cashoutCount++;
        } else if (trans.isCompletion()) {
            totals.completionAmount += trans.getAmounts().getTotalAmount();
            totals.completionCount++;
        }
        addSurchargeTotals(totals, trans, surchargeSupported);
    }

    private static void addSurchargeTotals(Reconciliation.CardSchemeTotals totals, TransRec trans, boolean surchargeSupported) {
        if (surchargeSupported && trans.getAmounts().getSurcharge() > 0) {
            totals.surchargeAmount += trans.getAmounts().getSurcharge();
            totals.surchargeCount++;
        }
    }

    /**
     * setReportWindowSettlement()
     * Set report window for Settlement.
     * Start of the Window:
     * - last successful settlement time from yesterday (manual or auto), if present
     * - yesterday autosettlement time+window if not
     */
    private void setReportWindowSettlement() {
        long yesterdaySettlementTime = findSettlementForDateOffset(-1);
        if (yesterdaySettlementTime != 0) {
            reportWindowStart = yesterdaySettlementTime;
        }
        else {
            reportWindowStart = getAutosettlementTimePlusWindowForDateOffset(-1);
        }
        reportWindowEnd = new java.util.Date().getTime();
    }

    /**
     * setReportWindowPreSettlement()
     * Set report window for Pre-Settlement.
     * Start of the Window:
     * - last successful settlement time from yesterday or today (manual or auto), if present
     * - yesterday autosettlement time+window if not
     */
    private void setReportWindowPreSettlement() {
        long todaySettlementTime = findSettlementForDateOffset(0);
        if (todaySettlementTime != 0) {
            reportWindowStart = todaySettlementTime;
        } else {
            long yesterdaySettlementTime = findSettlementForDateOffset(-1);
            if (yesterdaySettlementTime != 0) {
                reportWindowStart = yesterdaySettlementTime;
            }
        }
        if (reportWindowStart == 0) {
            reportWindowStart = getAutosettlementTimePlusWindowForDateOffset(-1);
        }
        reportWindowEnd = new java.util.Date().getTime();
    }

    /**
     * setReportWindowLastSettlement()
     * Set report window for Last Settlement.
     * If present successful Settlement from today
     *      end window: time of the today's settlement
     *      start window:
     *          search yesterday's (d-1) settlement
     *          if found: start window: time of the (d-1) settlement
     *          if not: time of the (d-1) autosettlement time
     * if present successful Settlement from yesterday (d-1):
     *      end window: time of the (d-1) settlement
     *      start window:
     *          search (d-2) settlement
     *          if found: start window: time of the (d-2) settlement
     *          if not: time of the (d-2) autosettlement time
     *  if not
     *      end window: time of the (d-1) autosettlement time
     *      start window: time of the (d-2) autosettlement time
     *
     */
    private void setReportWindowLastSettlement() {
        reportWindowEnd = findSettlementForDateOffset(0);
        if (reportWindowEnd !=0) { // Found Settlement record from Today
            reportWindowStart = findSettlementForDateOffset(-1);
            if (reportWindowStart ==0) {
                reportWindowStart = getAutosettlementTimePlusWindowForDateOffset(-1);
            }
        } else {
            reportWindowEnd = findSettlementForDateOffset(-1);
            if (reportWindowEnd != 0) { // Found Settlement record from Yesterday
                reportWindowStart = findSettlementForDateOffset(-2);
                if (reportWindowStart == 0) {
                    reportWindowStart = getAutosettlementTimePlusWindowForDateOffset(-2);
                }
            } else {
                reportWindowStart = getAutosettlementTimePlusWindowForDateOffset(-2);
                reportWindowEnd = getAutosettlementTimePlusWindowForDateOffset(-1);
            }
        }
    }

    public void setReportWindow(TransRec trans) {
        reportWindowStart = 0;
        reportWindowEnd = 0;
        if (trans.isReconciliation()) {
            setReportWindowSettlement();
        } else if (trans.isPreReconciliation()) {
            setReportWindowPreSettlement();
        } else if (trans.isLastReconciliation()) {
            setReportWindowLastSettlement();
        }
        Timber.e("Settlement Report Window set from: %s", Util.getDateTimeAsString("dd-MM-yyyy HH:mm:ss", reportWindowStart));
        Timber.e("Settlement Report Window set to:   %s", Util.getDateTimeAsString("dd-MM-yyyy HH:mm:ss", reportWindowEnd));
    }

    private long getAutosettlementTimePlusWindowForDateOffset(int dateOffset) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        int windowMinutes = 0;
        if (!Util.isNullOrEmpty(autoSettlementTime)) {
            int i;
            try {
                i = Integer.parseInt(autoSettlementTime);
            } catch (NumberFormatException e) {
                i = 0;
            }

            if ((i / 100 <= 23) && (i % 100 <= 59)) {
                calendar.set(Calendar.HOUR_OF_DAY, i / 100);
                calendar.set(Calendar.MINUTE, i % 100);
            }
        }

        try {
            windowMinutes = Integer.parseInt(autoSettlementTimeWindow);
        } catch (NumberFormatException ex) {
            Timber.e("format error");
        }
        calendar.add(Calendar.MINUTE, windowMinutes);
        calendar.add(Calendar.DATE, dateOffset);

        return calendar.getTime().getTime();
    }


    private boolean isSameDate(Calendar calendarA, Calendar calendarB) {
        return calendarA.get(Calendar.YEAR) == calendarB.get(Calendar.YEAR)
                && calendarA.get(Calendar.MONTH) == calendarB.get(Calendar.MONTH)
                && calendarA.get(Calendar.DAY_OF_MONTH) == calendarB.get(Calendar.DAY_OF_MONTH);
    }


    private long findSettlementForDateOffset(int dateOffset) {
        // find Settlement for on the day offset from today (yesterday is dateOffset = -1)
        // return 0 if not found
        List<TransRec> records = TransRecManager.getInstance().getTransRecDao().findAllByTransTypeAndApproved(RECONCILIATION, true);
        if (records !=null) {
            Calendar searchDate = Calendar.getInstance();
            searchDate.add(Calendar.DATE, dateOffset);
            for (TransRec trans : records) {
                if (terminalId.equals(trans.getAudit().getTerminalId())) {
                    Calendar transDate = Calendar.getInstance();
                    transDate.setTimeInMillis(trans.getAudit().getTransDateTime());
                    if (isSameDate(transDate, searchDate)) {
                        return trans.getAudit().getTransDateTime();
                    }
                }
            }
        }
        return 0;
    }

}
