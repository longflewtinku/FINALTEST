package com.linkly.libengine.action.check;

import static com.linkly.libengine.engine.transactions.properties.TCard.CvmType.NO_CVM;
import static com.linkly.libengine.engine.transactions.properties.TCard.CvmType.NO_CVM_SET;

import com.linkly.libengine.action.IAction;
import com.linkly.libengine.engine.transactions.properties.TCard;
import com.linkly.libengine.overrides.CoreOverrides;

import timber.log.Timber;

public class CheckCVM extends IAction {
    @Override
    public String getName() {
        return "CheckCVM";
    }

    @Override
    public void run() {

        TCard cardinfo = trans.getCard();

        if (cardinfo.isManual()) {
            cardinfo.setCvmType(NO_CVM);
            Timber.i( "No CVM check for manual transactions");
            return;
        }

        if (trans.getCard().isIccCaptured()) {
            trans.updateCvmTypeFromCard();
        } else if (trans.getCard().isCtlsCaptured()) {
            trans.updateCvmTypeFromCard();
        } else {
            trans.updateCvmTypeFromConfig(d.getPayCfg());
        }


        if (CoreOverrides.get().getOverrideCvmType() != NO_CVM_SET) {
            cardinfo.setCvmType(CoreOverrides.get().getOverrideCvmType());
        }
    }
}
