package com.linkly.libengine.action.cardprocessing;

import static com.linkly.libengine.engine.transactions.properties.TCard.CvmType.NO_CVM;
import static com.linkly.libengine.status.IStatus.STATUS_EVENT.STATUS_ERR_TRANSACTION_TYPE_NOT_ALLOWED;
import static com.linkly.libui.UIScreenDef.TRANS_NOT_ALLOWED;

import com.linkly.libengine.action.IAction;
import com.linkly.libengine.action.TransactionCanceller;
import com.linkly.libengine.action.check.CheckPAN;
import com.linkly.libengine.engine.transactions.TransRec;
import com.linkly.libengine.engine.transactions.properties.TCard;
import com.linkly.libui.IUIDisplay;

import timber.log.Timber;

public class PKEProcessing extends IAction {
    @Override
    public String getName() {
        return "PKEProcessing";
    }

    @Override
    public void run() {

        if (trans.getCard().getCaptureMethod() != TCard.CaptureMethod.MANUAL) {
            Timber.i( "Not a PKE transaction, skip task");
            return;
        }
        checkCvm(trans);

        CheckPAN checkPan = new CheckPAN();
        checkPan.run(d, mal, context);
        if (d.getWorkflowEngine().isJumping()) {
            // check PAN modified the processing flow
            return;
        }

        //if pke is allowed should be checked before transaction in case of terminal.isPKEAllowed
        //or right after inputing PAN number in case of acquirer/issuer.isPKEAllowed

        if (d.getWorkflowEngine().isJumping()) {
            // this will not happen i just want to show the pattern
            return;
        }

        checkTransAllowed();
    }

    private void checkCvm(TransRec trans) {
        trans.getCard().setCvmType(NO_CVM);
    }

    private void checkTransAllowed() {
        if (trans.isTransactionDisallowed(d.getPayCfg())) {
            ui.showScreen(TRANS_NOT_ALLOWED);
            ui.getResultCode(IUIDisplay.ACTIVITY_ID.ACT_INFORMATION, IUIDisplay.SHORT_TIMEOUT);
            d.getStatusReporter().reportStatusEvent(STATUS_ERR_TRANSACTION_TYPE_NOT_ALLOWED, trans.isSuppressPosDialog());
            d.getWorkflowEngine().setNextAction(TransactionCanceller.class);
        }
    }
}
