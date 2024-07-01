package com.linkly.libengine.action.check;

import static com.linkly.libengine.engine.transactions.properties.TCard.CvmType.NO_CVM;
import static com.linkly.libengine.engine.transactions.properties.TCard.CvmType.PLAINTEXT_PIN_AND_SIG;
import static com.linkly.libengine.engine.transactions.properties.TCard.CvmType.SIG;
import static com.linkly.libui.UIScreenDef.IS_SIGN_REQUIRED;

import com.linkly.libengine.action.IAction;
import com.linkly.libengine.action.TransactionCanceller;
import com.linkly.libengine.debug.IDebug;
import com.linkly.libengine.engine.transactions.properties.TCard;
import com.linkly.libengine.helpers.UIHelpers;

import timber.log.Timber;

public class IsSignatureRequired extends IAction {

    @Override
    public String getName() {
        return "IsSignatureRequired";
    }

    @Override
    public void run() {

        if (!trans.getCard().isSwiped() &&
             trans.getCard().getCaptureMethod() != TCard.CaptureMethod.ICC_FALLBACK_KEYED &&
             trans.getCard().getCaptureMethod() != TCard.CaptureMethod.CTLS_MSR &&
             trans.getCard().getCaptureMethod() != TCard.CaptureMethod.MANUAL) {
            Timber.i( "Not a magnetic or PKE transaction, skip task");
            return;
        }

        this.checkSignatureRequired();
    }

    private void checkSignatureRequired() {

        TCard cardinfo = trans.getCard();
        if (trans.mustUsePinAndSig()) {
            if (uiInputSignatureRequired()) {
                cardinfo.setCvmType(PLAINTEXT_PIN_AND_SIG);
                trans.getProtocol().setCanAuthOffline(false);
            }
        } else if (trans.mustAskIfSignatureRequired()) {
            cardinfo.setCvmType(uiInputSignatureRequired() ? SIG : NO_CVM);
        } else if (trans.mustUseSig() || cardinfo.getCardsConfig(d.getPayCfg()).isForceSign()) {
            cardinfo.setCvmType(SIG);
        }
    }

    private boolean uiInputSignatureRequired() {
        UIHelpers.YNQuestion resp =  UIHelpers.uiYesNoCancelQuestion(d, IS_SIGN_REQUIRED, null);
        if (resp == UIHelpers.YNQuestion.YES) {
            d.getDebugReporter().reportSignatureKeyPressed( IDebug.DEBUG_KEY.YES );
            trans.getCard().setCvmType(SIG);
            return true;
        } else if (resp == UIHelpers.YNQuestion.CANCEL) {
            d.getDebugReporter().reportSignatureKeyPressed( IDebug.DEBUG_KEY.NO );
            d.getWorkflowEngine().setNextAction(TransactionCanceller.class);
        }
        return false;
    }
}
