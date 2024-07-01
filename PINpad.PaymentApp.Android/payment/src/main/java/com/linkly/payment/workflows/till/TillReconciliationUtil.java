package com.linkly.payment.workflows.till;

import com.linkly.libengine.dependencies.IDependency;
import com.linkly.libengine.engine.reporting.TimePeriodTotals;
import com.linkly.libengine.engine.transactions.TransRec;
import com.linkly.libmal.global.util.Util;

public class TillReconciliationUtil {

    private TillReconciliationUtil() {
    }

    /**
     * Generate {@link TimePeriodTotals} using the provided details. This totals contains further
     * calculated details like surcharge, tipping and scheme-wise totals breakdown.
     *
     * @param d     {@link IDependency} variable to get config values
     * @param trans {@link  TransRec} variable to get the current transaction details
     * @return Generated {@link TimePeriodTotals} variable
     */
    public static TimePeriodTotals generatePeriodTotals(IDependency d, TransRec trans) {
        boolean tipsEnabled = (d.getCustomer().supportTipsOnReports() && d.getPayCfg().isTipAllowed());
        boolean surchargeEnabled = d.getPayCfg().isSurchargeSupported();
        String terminalId = d.getPayCfg().getStid();
        String autoSettlementTime = d.getPayCfg().getAutoSettlementTime();
        String autoSettlementTimeWindow = d.getPayCfg().getAutoSettlementTimeWindow();

        TimePeriodTotals timePeriodTotals = new TimePeriodTotals(tipsEnabled, surchargeEnabled, terminalId, autoSettlementTime, autoSettlementTimeWindow);
        timePeriodTotals.setReportWindow(trans);
        timePeriodTotals.calculateTimePeriodTotals(trans.getReconciliation());
        return timePeriodTotals;
    }

    /**
     * Check if the totals need to printed or not. Only for following scenario, need to print the totals
     * <ul>
     * <li>For reconciliation with response code 97</li>
     * <li>For Pre/Last reconciliation with response code 00</li>
     * </ul>
     *
     * @param trans {@link TransRec} to check
     * @return {@code true} if Totals is to be printed, {@code false} otherwise
     */
    public static boolean printTotals(TransRec trans) {
        boolean shouldPrintTotals = false;
        // Using Host Response Code
        String responseCode = "";
        if (!Util.isNullOrEmpty(trans.getProtocol().getServerResponseCode())) {
            responseCode = trans.getProtocol().getServerResponseCode();
        }

        if (trans.isReconciliation() && responseCode.equals("97")) {
            // Settlement
            shouldPrintTotals = true;
        }
        if ((trans.isLastReconciliation() || trans.isPreReconciliation()) && responseCode.equals("00")) {
            // Pre-settlement and Last settlement
            shouldPrintTotals = true;
        }
        return shouldPrintTotals;
    }
}