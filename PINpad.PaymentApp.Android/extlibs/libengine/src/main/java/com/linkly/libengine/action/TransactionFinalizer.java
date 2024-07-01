package com.linkly.libengine.action;


import static com.linkly.libengine.engine.transactions.properties.TProtocol.MessageStatus.NOT_SET;
import static com.linkly.libengine.status.IStatus.STATUS_EVENT.STATUS_TRANS_FINISHED;

import com.linkly.libengine.engine.transactions.TransRec;
import com.linkly.libengine.engine.transactions.TransRecManager;
import com.linkly.libengine.engine.transactions.properties.TProtocol;

import java.util.List;

public class TransactionFinalizer extends IAction {
    @Override
    public String getName() {
        return "TransactionFinalizer";
    }

    @Override
    public void run() {
        trans.getAudit().updateFinishedDateTimeToNow();
        trans.setFinalised(true);
        d.getStatusReporter().reportStatusEvent(STATUS_TRANS_FINISHED, trans.isSuppressPosDialog());

        // find all records with msg status 'not set'
        List<TransRec> transList = TransRecManager.getInstance().getTransRecDao().findAllByMessageStatus( NOT_SET );
        if (transList != null) {
            // found > 0 records
            for (TransRec tran : transList) {
                // if this is a financial (non-admin) trans type, update it's status to finalised
                if( !tran.getTransType().adminTransaction ) {
                    tran.updateMessageStatus(TProtocol.MessageStatus.FINALISED);
                    tran.save();
                }
            }
        }
    }
}
