package com.linkly.libengine.engine.reporting;

import static com.linkly.libengine.engine.transactions.properties.TCard.CardIssuer.UNKNOWN;
import static com.linkly.libpositive.messages.IMessages.SHIFT_TOTALS_SCHEDULED_RESET_EVENT;

import android.content.Context;
import androidx.preference.PreferenceManager;
import com.google.gson.Gson;
import com.linkly.libengine.engine.Engine;
import com.linkly.libengine.engine.EngineManager;
import com.linkly.libengine.engine.transactions.TransRec;
import com.linkly.libengine.jobs.EFTJob;
import com.linkly.libengine.jobs.EFTJobScheduleEvent;
import com.linkly.libpositive.events.PositiveEvent;
import com.linkly.libpositive.events.PositiveScheduledEvent;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import timber.log.Timber;

/***
 * WARNING THIS CODE REQUIRES TO BE RUN IN A THREAD!
 */
public class ShiftTotalsReport {
    ShiftTotals shiftTotals;
    Context context;

    public ShiftTotalsReport(Context context) {
        this.context = context;
    }

    public void addTransactionToTotals(TransRec trans) {
        if (trans.approvedAndIncludeInReconciliation()) {

            shiftTotals = getCurrentRecord();
            if (Objects.equals(shiftTotals.getLastTransStan(), trans.getProtocol().getStan())) {
                Timber.e("Transaction already in Shift Totals, not adding");
                return;
            }

            shiftTotals.setLastTransStan( trans.getProtocol().getStan() );

            if (trans.getAmounts().getTip() > 0) {
                shiftTotals.addTip(trans.getAmounts().getTip());
            }

            if (trans.getAmounts().getSurcharge() > 0) {
                shiftTotals.addSurcharge(trans.getAmounts().getSurcharge());
            }

            if( trans.isSale() ) {
                shiftTotals.addSale( trans.getAmounts().getTotalAmount() );
                shiftTotals.addTotal( trans.getAmounts().getTotalAmount() );
            } else if( trans.isCash() ) {
                shiftTotals.addCash( trans.getAmounts().getTotalAmount() );
                shiftTotals.addTotal( trans.getAmounts().getTotalAmount() );
            } else if( trans.isRefund() ) {
                shiftTotals.addRefund( trans.getAmounts().getTotalAmount() );
                shiftTotals.addTotal( -trans.getAmounts().getTotalAmount() );
            } else if( trans.isCashback() ) {
                // add to both purchase and cashout amount, inctrement total count
                shiftTotals.addSale( trans.getAmounts().getTotalAmountWithoutCashback());
                shiftTotals.addCash( trans.getAmounts().getCashbackAmount() );
                shiftTotals.addTotal( trans.getAmounts().getTotalAmount() );
            } else if (trans.isCompletion()) {
                shiftTotals.addCompletion( trans.getAmounts().getTotalAmount() );
                shiftTotals.addTotal( trans.getAmounts().getTotalAmount() );
            } else {
                Timber.e("Shift Totals: unknown transaction type %s, treating as 'sale'",trans.getTransType());
                shiftTotals.addSale( trans.getAmounts().getTotalAmount() );
                shiftTotals.addTotal( trans.getAmounts().getTotalAmount() );
            }

            addTransactionToSchemeTotals(trans);

            daoUpdate( shiftTotals );
        }
    }

    public ShiftTotals getSubTotalsRecord() {
        shiftTotals = daoGetLatest();
        if (shiftTotals != null && shiftTotals.getTotalsTo() == 0) {
            // Record exists and last record is "open", return as SubTotals (current shift Totals)
            return shiftTotals;
        }
        return null;
    }

    public ShiftTotals resetShiftTotals() {

        shiftTotals = daoGetLatest();
        if (shiftTotals != null && shiftTotals.getTotalsTo() == 0) {
            // Record exists and last record is "open". Close it (Reset) by setting "TotalsTo" field
            shiftTotals.setTotalsTo( System.currentTimeMillis() );
            daoUpdate(shiftTotals);
        }
        // Always Create new shift
        createRecord();
        // return record we've just closed (or null)
        return shiftTotals;
    }

    public ShiftTotals resetShiftTotalsAutomatic() {
        if (!getAutomaticShiftTotalsEnabledFlag()) {
            Timber.i("Automatic Shift Totals requested but disabled in params");
            return null;
        }

        shiftTotals = daoGetLatest();
        if (shiftTotals != null && shiftTotals.getTotalsTo() == 0) {
            // Record exists and last record is "open". Close it (Reset) by setting "TotalsTo" field
            shiftTotals.setTotalsTo( System.currentTimeMillis() );
            daoUpdate(shiftTotals);
        }
        // Always Create new shift
        createRecord();
        // return record we've just closed (or null)
        return shiftTotals;
    }

    public ShiftTotals getPreviousShiftTotalsRecord() {
        return daoGetPrevious();
    }

    private ShiftTotals getCurrentRecord() {
        ShiftTotals latestShiftTotals = daoGetLatest();

        if (latestShiftTotals != null && latestShiftTotals.getTotalsTo() == 0) {
            // latest record is "open", done
            return latestShiftTotals;
        }

        // no records in storage or last record is "closed": create new one
        return createRecord();
    }

    public enum ShiftTotalsReportType {
        SHIFT_TOTALS,
        SUB_TOTALS,
        REPRINT_SHIFT_TOTALS,
    }

    private ShiftTotals createRecord() {
        ShiftTotals newShiftTotals = new ShiftTotals();
        newShiftTotals.setTotalsFrom( System.currentTimeMillis() );
        newShiftTotals.setShiftAutoClosingDateTime( calculateShiftAutoClosingDateTime() );
        newShiftTotals.setId( daoInsert(newShiftTotals) );
        scheduleShiftTotalsReset( newShiftTotals.getShiftAutoClosingDateTime() );
        // Delete all but two most recent records from DB
        ShiftTotals prev = daoGetPrevious();
        if (prev != null) {
            daoDeleteBeforeId(prev.getId());
        }
        return newShiftTotals;
    }

    public void onParametersUpdate() {

        ExecutorService executorService = Executors.newSingleThreadExecutor();

        // This stuff needs to be run in a thread....
        // Database accessing here...
        executorService.submit(() -> {
            // Set DB and scheduled events according to (new) parameters values
            shiftTotals = getCurrentRecord();

            // automaticShiftTotalsEnabled flag
            if (getAutomaticShiftTotalsEnabledFlag()) {
                // Refresh Closing DateTime in DB record and update (or create new) scheduled event
                shiftTotals.setShiftAutoClosingDateTime(calculateShiftAutoClosingDateTime());
                scheduleShiftTotalsReset( shiftTotals.getShiftAutoClosingDateTime() );
                daoUpdate(shiftTotals);
            }
            else {
                // remove scheduled event (if any)
                cancelScheduledShiftTotalsReset( shiftTotals.getShiftAutoClosingDateTime() );
            }

            Timber.d("Finished Param Update Thread");
        });

        executorService.shutdown();
        try {
            // Blocking... This code was originally blocking.
            // This will need to be refactored when menu's change.
            // Arbitrarily threw a high number in here to wait. Should be done well before.
            if(executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                Timber.e("Major error - we were doing totals for too long....");
                executorService.shutdownNow();
            }
            Timber.d("Shift totals completed");
        } catch (InterruptedException e) {
            Timber.e("Interrupt Exception");
        }

    }

    private void addTransactionToSchemeTotals(TransRec trans) {
        Reconciliation rec = new Reconciliation();
        HashMap<String, Reconciliation.CardSchemeTotals> schemeTotals = rec.expandStringIntoSchemeTotals(shiftTotals.getSchemeTotals());

        String schemeName = (trans.getCard() != null) ? trans.getCard().getCardIssuer().getDisplayName() : UNKNOWN.getDisplayName();
        Reconciliation.CardSchemeTotals total = schemeTotals.get(schemeName);
        if (total != null) {
            addTransactionToTotals(total, trans);
        } else {
            Reconciliation.CardSchemeTotals newEntry = new Reconciliation.CardSchemeTotals();
            addTransactionToTotals(newEntry, trans);
            schemeTotals.put(newEntry.name, newEntry);
        }

        ArrayList<Reconciliation.CardSchemeTotals> list = new ArrayList<>(schemeTotals.values());

        // Convert the object list to a JSON string
        shiftTotals.setSchemeTotals( new Gson().toJson(list) );
    }

    private void addTransactionToTotals(Reconciliation.CardSchemeTotals totals, TransRec trans) {

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
    }

    private long calculateShiftAutoClosingDateTime() {
        final int MINIMUM_SHIFT_DURATION_MINUTES = 2;
        int paramHour = 0;
        int paramMinute = 0;

        String paramTimeOfDay = PreferenceManager.getDefaultSharedPreferences(context).getString("automaticShiftTotalsTimeOfDay", "00:00");

        paramTimeOfDay = paramTimeOfDay.replaceAll("\\D", "");

        if (paramTimeOfDay.length() == 4) {
            paramHour = Integer.parseInt(paramTimeOfDay.substring(0,2));
            if (paramHour > 23) {
                paramHour = 23;
                Timber.e("Parameter automaticShiftTotalsTimeOfDay hour value not in range");
            }

            paramMinute = Integer.parseInt(paramTimeOfDay.substring(2,4));
            if (paramMinute > 59) {
                paramMinute = 59;
                Timber.e("Parameter automaticShiftTotalsTimeOfDay minute value not in range");
            }
        }

        // If automatic shift totals reset is in past (plus several minutes for grace period) then set it to next day
        Calendar paramDateTime = Calendar.getInstance();
        paramDateTime.set(Calendar.HOUR_OF_DAY, paramHour);
        paramDateTime.set(Calendar.MINUTE, paramMinute);
        paramDateTime.set(Calendar.SECOND, 0);

        Calendar nowPlusGrace = Calendar.getInstance();
        nowPlusGrace.add(Calendar.MINUTE,MINIMUM_SHIFT_DURATION_MINUTES);

        if (nowPlusGrace.after(paramDateTime)) {
            paramDateTime.add(Calendar.DATE,1);
        }
        return paramDateTime.getTime().getTime();
    }

    private void scheduleShiftTotalsReset(long resetDateTime) {
        EFTJobScheduleEvent resetShiftTotalsEvent = new EFTJobScheduleEvent(PositiveScheduledEvent.EventType.UPDATE, SHIFT_TOTALS_SCHEDULED_RESET_EVENT, resetDateTime);
        Engine.getDep().getJobs().schedule(context, resetShiftTotalsEvent);
    }

    private void cancelScheduledShiftTotalsReset(long resetDateTime) {
        EFTJobScheduleEvent resetShiftTotalsEvent = new EFTJobScheduleEvent(PositiveScheduledEvent.EventType.CANCEL, SHIFT_TOTALS_SCHEDULED_RESET_EVENT, resetDateTime);
        Engine.getDep().getJobs().schedule(context, resetShiftTotalsEvent);
    }

    public void terminalStartUp() {
        shiftTotals = daoGetLatest();
        if (shiftTotals == null ) {
            // Shift Totals DB is empty, create new record
            createRecord();
        }
        else {
            // check the state of automatic Shift Totals; if it's necessary to schedule or reset now
            if (getAutomaticShiftTotalsEnabledFlag()) {
                if (shiftTotals.getShiftAutoClosingDateTime() < System.currentTimeMillis()) {
                    Timber.i("Automatic Shift Totals Reset (over)due");
                    EFTJob job = new EFTJob(PositiveEvent.EventType.SHIFT_TOTALS_AUTOMATIC_REPORT);
                    Engine.getJobs().add(job);
                }
                else {
                    Timber.i("Scheduling Automatic Shift Totals Reset");
                    scheduleShiftTotalsReset( shiftTotals.getShiftAutoClosingDateTime() );
                }
            }
        }
    }

    private boolean getAutomaticShiftTotalsEnabledFlag() {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean("automaticShiftTotalsEnabled", false);
    }

    private ShiftTotals daoGetLatest() {
        return ShiftTotalsManager.getShiftTotalsDao(context).getLatest();
    }

    private ShiftTotals daoGetPrevious() {
        return ShiftTotalsManager.getShiftTotalsDao(context).getPrevious();
    }

    private void daoUpdate(ShiftTotals shiftTotals) {
        ShiftTotalsManager.getShiftTotalsDao(context).update(shiftTotals);
    }

    private int daoInsert(ShiftTotals shiftTotals) {
        return (int) ShiftTotalsManager.getShiftTotalsDao(context).insert(shiftTotals);
    }

    private void daoDeleteBeforeId(int id) {
        ShiftTotalsManager.getShiftTotalsDao(context).deleteBeforeId( id );
    }

}
