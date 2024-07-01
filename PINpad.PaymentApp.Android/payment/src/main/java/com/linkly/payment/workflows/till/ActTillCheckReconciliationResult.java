package com.linkly.payment.workflows.till;

import static com.linkly.libengine.engine.transactions.properties.TProtocol.HostResult.BATCH_UPLOAD_FAILED;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.HostResult.CONNECT_FAILED;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.HostResult.NO_RESPONSE;
import static com.linkly.libui.UIScreenDef.SETTLEMENT_SUCCESS;
import static com.linkly.libui.UIScreenDef.SETTLEMENT_TERMINAL_SETTLED;

import com.linkly.libengine.action.HostActions.Authorise;
import com.linkly.libengine.action.TransactionApproval;
import com.linkly.libengine.action.TransactionDecliner;
import com.linkly.libengine.engine.protocol.IProto;
import com.linkly.libengine.engine.reporting.DailyBatch;
import com.linkly.libengine.engine.reporting.IDailyBatch;
import com.linkly.libengine.engine.reporting.Reconciliation;
import com.linkly.libengine.status.IStatus;
import com.linkly.payment.workflows.ActBaseCheckReconciliationResult;
import com.linkly.payment.workflows.generic.ActRecOutOfBalance;

import timber.log.Timber;

@SuppressWarnings("java:S3776")
public class ActTillCheckReconciliationResult extends ActBaseCheckReconciliationResult {

    @Override
    public String getName() {
        return "ActTillCheckReconciliationResult";
    }

    @Override
    public void run() {

        if ((trans.getProtocol().getHostResult() == NO_RESPONSE || trans.getProtocol().getHostResult() == CONNECT_FAILED) || trans.getProtocol().getHostResult() == BATCH_UPLOAD_FAILED) {
            if (trans.getAutoSettlementRetryCount() > 0) {
                // Automatic Settlement, retry is required
                Timber.i("AutoSettlement, retrying");
                int retryCount = trans.getAutoSettlementRetryCount() - 1;
                trans.setAutoSettlementRetryCount(retryCount);
                d.getWorkflowEngine().setNextAction(Authorise.class);
            } else {
                if (retryReconciliation()) {
                    d.getWorkflowEngine().setNextAction(Authorise.class);
                } else if (trans.getProtocol().getHostResult() == BATCH_UPLOAD_FAILED) {
                    d.getStatusReporter().reportStatusEvent(IStatus.STATUS_EVENT.STATUS_ERR_TRANSACTION_DECLINED, trans.isSuppressPosDialog());
                    d.getWorkflowEngine().setNextAction(TransactionDecliner.class);
                } else {
                    d.getStatusReporter().reportStatusEvent(IStatus.STATUS_EVENT.STATUS_ERR_TRANSACTION_DECLINED, trans.isSuppressPosDialog());
                    trans.getAudit().setRejectReasonType(IProto.RejectReasonType.COMMS_ERROR);
                    d.getWorkflowEngine().setNextAction(TransactionDecliner.class);
                }
            }
            return;
        }

        switch (trans.getProtocol().getHostResult()) {
            case RECONCILED_IN_BALANCE:
            case AUTHORISED:
                saveSettlementCompletedDatestamp(context);
                d.getStatusReporter().reportStatusEvent(IStatus.STATUS_EVENT.STATUS_HOST_APPROVED, trans.isSuppressPosDialog());
                ui.showScreen(SETTLEMENT_SUCCESS);
                d.getWorkflowEngine().setNextAction(TransactionApproval.class);
                break;

            case RECONCILED_OUT_OF_BALANCE:
                saveSettlementCompletedDatestamp(context);
                d.getStatusReporter().reportStatusEvent(IStatus.STATUS_EVENT.STATUS_ERR_RECONCILIATION_OUT_OF_BALANCE, trans.isSuppressPosDialog());
                trans.getAudit().setRejectReasonType(IProto.RejectReasonType.DECLINED);
                d.getWorkflowEngine().setNextAction(ActRecOutOfBalance.class);
                break;

            case RECONCILE_FAILED_TERMINAL_ALREADY_SETTLED:
                saveSettlementCompletedDatestamp(context);
                d.getStatusReporter().reportStatusEvent(IStatus.STATUS_EVENT.STATUS_ERR_RECONCILIATION_TERMINAL_ALREADY_SETTLED, trans.isSuppressPosDialog());
                ui.showScreen(SETTLEMENT_TERMINAL_SETTLED);
                d.getWorkflowEngine().setNextAction(TransactionDecliner.class);
                break;

            case RECONCILE_FAILED_OUTSIDE_WINDOW:
                d.getStatusReporter().reportStatusEvent(IStatus.STATUS_EVENT.STATUS_ERR_RECONCILIATION_OUTSIDE_WINDOW, trans.isSuppressPosDialog());
                trans.getAudit().setRejectReasonType(IProto.RejectReasonType.DECLINED);
                d.getWorkflowEngine().setNextAction(TransactionDecliner.class);
                break;

            default:
                // to prevent from "Waiting for response" for the host result values that are not handled yet -> treat as declined
                d.getStatusReporter().reportStatusEvent(IStatus.STATUS_EVENT.STATUS_ERR_TRANSACTION_DECLINED, trans.isSuppressPosDialog());
                trans.getAudit().setRejectReasonType(IProto.RejectReasonType.COMMS_ERROR);
                d.getWorkflowEngine().setNextAction(TransactionDecliner.class);
                break;
        }

        // are we cutting over?
        switch (trans.getProtocol().getHostResult()) {
            case RECONCILED_IN_BALANCE:
            case RECONCILED_OUT_OF_BALANCE:
                // do not update record if pre-settlement or last-settlement
                if (trans.isReconciliation()) {
                    // update settlement record, mark trans records as reconciled
                    IDailyBatch dailyBatch = new DailyBatch();
                    Reconciliation recData = dailyBatch.generateDailyBatch(true, d);
                    // if reconciliation already has a db record (UID) in rec database, set the handle. This prevents duplicate records in rec database
                    if (trans.getReconciliation().getUid() > 0) {
                        recData.setUid(trans.getReconciliation().getUid());
                    }
                }
                break;

            case AUTHORISED:
                // pre-settlement
                if (trans.isPreReconciliation()) {
                    // update settlement record, mark trans records as reconciled
                    IDailyBatch dailyBatch = new DailyBatch();
                    Reconciliation recData = dailyBatch.generateDailyBatch(false, d);
                    // if reconciliation already has a db record (UID) in rec database, set the handle. This prevents duplicate records in rec database
                    if (trans.getReconciliation().getUid() > 0) {
                        recData.setUid(trans.getReconciliation().getUid());
                    }
                }
                break;
            default:
                // don't update trans records
                break;
        }

        // if printing, totals calculation will be done as a part of printing receipt
        if (trans.isPrintTransactionListing()) {
            printReconciliation(trans);
        } else if (TillReconciliationUtil.printTotals(trans)){
            // only when skipping printing, if totals were meant to be printed, need to calculate totals separately
            TillReconciliationUtil.generatePeriodTotals(d, trans);
        }
    }

}

