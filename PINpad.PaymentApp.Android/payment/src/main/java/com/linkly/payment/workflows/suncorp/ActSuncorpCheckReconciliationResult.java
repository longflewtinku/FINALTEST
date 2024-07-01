package com.linkly.payment.workflows.suncorp;

import com.linkly.libengine.action.IAction;
import com.linkly.libengine.action.Printing.PrintFirst;
import com.linkly.libengine.action.TransactionApproval;
import com.linkly.libengine.action.TransactionDecliner;
import com.linkly.libengine.engine.protocol.IProto;
import com.linkly.libengine.engine.reporting.DailyBatch;
import com.linkly.libengine.engine.reporting.IDailyBatch;
import com.linkly.libengine.engine.reporting.Reconciliation;
import com.linkly.libengine.engine.transactions.TransRec;
import com.linkly.libengine.status.IStatus;

public class ActSuncorpCheckReconciliationResult extends IAction {

    private void printReconciliation(TransRec trans) {
        if(trans.getTransEvent() != null && trans.getTransEvent().isPosPrintingSync()) {
            PrintFirst.buildAndBroadcastReceipt(d, trans, PrintFirst.ReceiptType.SETTLEMENT, false, context, mal);

            if(trans.isPrintOnTerminal()) {
                trans.print(d, true, false, mal);
            }
        } else {
            trans.print(d, true, false, mal);
        }
    }

    @Override
    public String getName() {
        return "ActSuncorpCheckReconciliationResult";
    }

    @Override
    public void run() {

        // are we cutting over?
        switch( trans.getProtocol().getHostResult() ) {
            case RECONCILED_IN_BALANCE:
            case RECONCILED_OUT_OF_BALANCE:
                // update settlement record, mark trans records as reconciled
                IDailyBatch dailyBatch = new DailyBatch();
                Reconciliation recData = dailyBatch.generateDailyBatch(true, d);
                // if reconciliation already has a db record (UID) in rec database, set the handle. This prevents duplicate records in rec database
                if( trans.getReconciliation().getUid() > 0 ) {
                    recData.setUid( trans.getReconciliation().getUid() );
                }
                trans.setReconciliation(recData);
                break;

            default:
                // don't update trans records
                break;
        }

        // print receipt
        printReconciliation(trans);

        switch( trans.getProtocol().getHostResult() ) {
            case RECONCILED_IN_BALANCE:
                // TODO: something here
                d.getStatusReporter().reportStatusEvent(IStatus.STATUS_EVENT.STATUS_HOST_APPROVED , trans.isSuppressPosDialog());
                d.getWorkflowEngine().setNextAction(TransactionApproval.class);
                break;

            case RECONCILED_OUT_OF_BALANCE:
                d.getStatusReporter().reportStatusEvent(IStatus.STATUS_EVENT.STATUS_ERR_RECONCILIATION_OUT_OF_BALANCE , trans.isSuppressPosDialog());
                trans.getAudit().setRejectReasonType( IProto.RejectReasonType.DECLINED);
                d.getWorkflowEngine().setNextAction(ActSuncorpRecOutOfBalance.class);
                break;

            case RECONCILE_FAILED_TERMINAL_ALREADY_SETTLED:
                d.getStatusReporter().reportStatusEvent(IStatus.STATUS_EVENT.STATUS_ERR_RECONCILIATION_TERMINAL_ALREADY_SETTLED , trans.isSuppressPosDialog());
                trans.getAudit().setRejectReasonType( IProto.RejectReasonType.DECLINED);
                d.getWorkflowEngine().setNextAction(TransactionDecliner.class);
                break;

            case RECONCILE_FAILED_OUTSIDE_WINDOW:
                d.getStatusReporter().reportStatusEvent(IStatus.STATUS_EVENT.STATUS_ERR_RECONCILIATION_OUTSIDE_WINDOW , trans.isSuppressPosDialog());
                trans.getAudit().setRejectReasonType( IProto.RejectReasonType.DECLINED);
                d.getWorkflowEngine().setNextAction(TransactionDecliner.class);
                break;

            case NO_RESPONSE:
            case CONNECT_FAILED:
            default:
                // to prevent from "Waiting for response" for the host result values that are not handled yet -> treat as declined
                d.getStatusReporter().reportStatusEvent(IStatus.STATUS_EVENT.STATUS_ERR_TRANSACTION_DECLINED,trans.isSuppressPosDialog());
                trans.getAudit().setRejectReasonType( IProto.RejectReasonType.COMMS_ERROR);
                d.getWorkflowEngine().setNextAction(TransactionDecliner.class);
                break;
        }
    }
}
