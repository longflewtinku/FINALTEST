package com.linkly.libengine.action.check;

import static com.linkly.libengine.engine.protocol.IProto.RejectReasonType.NOT_SET;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.HostResult.AUTHORISED;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.HostResult.DECLINED;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.HostResult.RECONCILED_IN_BALANCE;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.HostResult.RECONCILED_NO_TOTALS;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.HostResult.RECONCILED_OUT_OF_BALANCE;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.HostResult.REQUEST_REFERRAL;

import com.linkly.libengine.action.IAction;
import com.linkly.libengine.action.TransactionApproval;
import com.linkly.libengine.action.TransactionDecliner;
import com.linkly.libengine.action.TransactionReferral;
import com.linkly.libengine.engine.cards.Ctls;
import com.linkly.libengine.engine.protocol.IProto;
import com.linkly.libengine.engine.transactions.properties.TProtocol;
import com.linkly.libengine.helpers.UIHelpers;
import com.linkly.libengine.overrides.CoreOverrides;
import com.linkly.libengine.status.IStatus;

import timber.log.Timber;

public class CheckResult extends IAction {

    @Override
    public String getName() {
        return "CheckResult";
    }

    @Override
    public void run() {
        Timber.w( "CheckResult, auth method=%s, host result=%s, isReversal=%b", trans.getProtocol().getAuthMethod(), trans.getProtocol().getHostResult(), trans.isReversal() );
        if (trans.getProtocol().getHostResult() == AUTHORISED ||
                trans.getProtocol().getHostResult() == RECONCILED_IN_BALANCE ||
                trans.getProtocol().getHostResult() == RECONCILED_OUT_OF_BALANCE ||
                trans.getProtocol().getHostResult() == RECONCILED_NO_TOTALS) {
            // simple approval
            d.getStatusReporter().reportStatusEvent(IStatus.STATUS_EVENT.STATUS_HOST_APPROVED , trans.isSuppressPosDialog());
            d.getWorkflowEngine().setNextAction(TransactionApproval.class);
        } else if (trans.getProtocol().getHostResult() == REQUEST_REFERRAL) {
            // host initiated referral
            d.getWorkflowEngine().setNextAction(TransactionReferral.class);
        } else if (trans.getProtocol().getHostResult() == DECLINED) {
            // host decline
            d.getStatusReporter().reportStatusEvent(IStatus.STATUS_EVENT.STATUS_ERR_HOST_DECLINED , trans.isSuppressPosDialog());

            int ac = trans.getProtocol().getServerResponseCodeAsInt();

            if ( !CoreOverrides.get().isAutoFillTrans() &&
                 trans.getCard().isCtlsCaptured() && isMasterCardFallForward(ac)) {
                UIHelpers.uiShowTryContact(d,trans);
                Ctls.getInstance().ctlsRetryCardEntry(d, trans);
                trans.getCard().setCtlsToICCFallbackTxn(true);
            } else {
                trans.getAudit().setRejectReasonType( IProto.RejectReasonType.COMMS_ERROR);
                d.getWorkflowEngine().setNextAction(TransactionDecliner.class);
            }

            /* lock this change down to just SVFE for now as dangerous */
        } else if (trans.getProtocol().getHostResult() == TProtocol.HostResult.NO_RESPONSE ||
                trans.getProtocol().getHostResult() == TProtocol.HostResult.CONNECT_FAILED) {
            // comms error and wasn't approved offline above, then decline
            d.getStatusReporter().reportStatusEvent(IStatus.STATUS_EVENT.STATUS_ERR_TRANSACTION_DECLINED , trans.isSuppressPosDialog());
            // set reject reason type to 'comms error' if not set already
            if( trans.getAudit().getRejectReasonType() == NOT_SET ) {
                trans.getAudit().setRejectReasonType(IProto.RejectReasonType.COMMS_ERROR);
            }
            d.getWorkflowEngine().setNextAction(TransactionDecliner.class);
        } else {
            // to prevent from "Waiting for response" for the host result values that are not handled yet -> treat as declined
            d.getStatusReporter().reportStatusEvent(IStatus.STATUS_EVENT.STATUS_ERR_TRANSACTION_DECLINED , trans.isSuppressPosDialog());
            trans.getAudit().setRejectReasonType( IProto.RejectReasonType.COMMS_ERROR);
            d.getWorkflowEngine().setNextAction(TransactionDecliner.class);
        }
    }

    // put logic here if fall forward to insert on declined txn is required
    private boolean isMasterCardFallForward(int ac) {
        // not terribly nice - 65 is the older as2805 2 digit response code. 123 is the iso8583 v 1993 response code that maps to this
        return (ac == 65 || ac == 123);
    }

}
