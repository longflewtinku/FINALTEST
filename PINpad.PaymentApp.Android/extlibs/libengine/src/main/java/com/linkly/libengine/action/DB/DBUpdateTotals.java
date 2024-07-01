package com.linkly.libengine.action.DB;

import static com.linkly.libengine.engine.reporting.Totals.updateTotalCount;

import com.linkly.libengine.action.IAction;
import com.linkly.libengine.engine.EngineManager;
import com.linkly.libengine.engine.transactions.TransRec;
import com.linkly.libengine.engine.transactions.TransRecManager;
import com.linkly.libengine.engine.transactions.properties.TAmounts;

import timber.log.Timber;

public class DBUpdateTotals extends IAction {

    @Override
    public String getName() {
        return "DBUpdateTotals";
    }

    @Override
    public void run() {

        //Update the totals to reflect this transaction values
        Timber.i( "Updating Totals");

        //Check that we should be Updating the Terminals
        if (trans.isApproved()) {
            TransRec transToUpdate = null;
            long sign;
            //Determine if this is a Increment or Dec - Use sign as a multiplier to make negative
            if (trans.isReversal()) {
                sign = -1;
                //Get the Original Transaction - TODO: Check With Auto Reversal
                transToUpdate = TransRecManager.getInstance().getTransRecDao().getByReceiptNumber(trans.getAudit().getReversalReceiptNumber());
            } else {
                sign = trans.getTransType().transClass == EngineManager.TransClass.CREDIT ? -1 : 1;
            }

            if(transToUpdate == null) {
                transToUpdate = trans;
            }

            TAmounts amounts = transToUpdate.getAmounts();
            String cardName = transToUpdate.getCard().getCardName(d.getPayCfg());
            int messageNumber = transToUpdate.getProtocol().getMessageNumberInt();
            EngineManager.TransType transType = transToUpdate.getTransType();

            if(transType == EngineManager.TransType.CASHBACK || transType == EngineManager.TransType.CASHBACK_AUTO) {
                //Handle special case where we want to split cashback transaction into SALE + CASH
                transType = EngineManager.TransType.SALE;
            }

            if (amounts.getAmount() > 0) {
                updateTotalCount(transType, cardName, sign * amounts.getAmount(), messageNumber);
            }
            if (amounts.getCashbackAmount() > 0) {
                updateTotalCount(EngineManager.TransType.CASH, cardName, sign * amounts.getCashbackAmount(), messageNumber);
            }
            if (amounts.getTip() > 0) {
                updateTotalCount(EngineManager.TransType.GRATUITY, cardName, sign * amounts.getTip(), messageNumber);
            }
        }
    }
}
