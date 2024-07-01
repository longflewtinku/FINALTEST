package com.linkly.libengine.action.cardprocessing;

import static com.linkly.libengine.engine.transactions.properties.TCard.CvmType.NO_CVM_SET;
import static com.linkly.libengine.status.IStatus.STATUS_EVENT.STATUS_ERR_TRANSACTION_TYPE_NOT_ALLOWED;
import static com.linkly.libui.IUIDisplay.ACTIVITY_ID.ACT_INFORMATION;
import static com.linkly.libui.UIScreenDef.TRANS_NOT_ALLOWED;

import com.linkly.libengine.action.IAction;
import com.linkly.libengine.action.TransactionCanceller;
import com.linkly.libengine.action.check.CheckOfflineAllowed;
import com.linkly.libengine.action.check.CheckPAN;
import com.linkly.libengine.action.check.CheckSVCCode;
import com.linkly.libengine.engine.protocol.IProto;
import com.linkly.libengine.engine.transactions.TransRec;
import com.linkly.libengine.engine.transactions.properties.TCard;
import com.linkly.libengine.overrides.CoreOverrides;
import com.linkly.libui.IUIDisplay;

import timber.log.Timber;

public class MagstripeProcessing extends IAction {
    @Override
    public String getName() {
        return "MagstripeProcessing";
    }

    @Override
    public void run() {

        if (!trans.getCard().isSwiped() &&
                trans.getCard().getCaptureMethod() != TCard.CaptureMethod.CTLS_MSR) {
            Timber.i( "Not a magstripe transaction, skip task");
            return;
        }

        CheckSVCCode checkSVCCode = new CheckSVCCode();
        checkSVCCode.run(d, mal, context);
        if (d.getWorkflowEngine().isJumping()) {
            // check PAN modified the processing flow
            return;
        }

        checkCvm(trans);

        CheckPAN checkPan = new CheckPAN();
        checkPan.run(d, mal, context);
        if (d.getWorkflowEngine().isJumping()) {
            // check PAN modified the processing flow
            return;
        }

        checkTransAllowed();
        if (d.getWorkflowEngine().isJumping()) {
            return;
        }

        CheckOfflineAllowed checkOfflineAllowed = new CheckOfflineAllowed();
        checkOfflineAllowed.run(d, mal, context);
    }

    private void checkCvm(TransRec trans) {
        if (trans.getCard().isCtlsCaptured()) {
            trans.updateCvmTypeFromCard();
        } else {
            trans.updateCvmTypeFromConfig(d.getPayCfg());
        }

        if (CoreOverrides.get().getOverrideCvmType() != NO_CVM_SET) {
            trans.getCard().setCvmType(CoreOverrides.get().getOverrideCvmType());
        }
    }

    private void checkTransAllowed() {

        if (trans.isTransactionDisallowed(d.getPayCfg())) {

            ui.showScreen(TRANS_NOT_ALLOWED);
            ui.getResultCode(ACT_INFORMATION, IUIDisplay.SHORT_TIMEOUT);

            d.getStatusReporter().reportStatusEvent(STATUS_ERR_TRANSACTION_TYPE_NOT_ALLOWED ,  trans.isSuppressPosDialog());
            d.getProtocol().setInternalRejectReason( super.trans, IProto.RejectReasonType.TRANS_NOT_ALLOWED );
            d.getWorkflowEngine().setNextAction(TransactionCanceller.class);
        }
    }
}
