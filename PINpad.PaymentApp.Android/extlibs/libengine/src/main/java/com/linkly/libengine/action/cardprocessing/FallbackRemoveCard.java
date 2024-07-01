package com.linkly.libengine.action.cardprocessing;

import static com.linkly.libui.UIScreenDef.REMOVE_CARD;

import com.linkly.libengine.action.IAction;
import com.linkly.libengine.helpers.UIHelpers;
import com.linkly.libengine.overrides.CoreOverrides;

public class FallbackRemoveCard extends IAction {
    @Override
    public String getName() {
        return "FallbackRemoveCard";
    }

    @Override
    public void run() {
        if (CoreOverrides.get().isDisableCardRemove()) {
            return;
        }

        if (trans.getCard().getEmvReadErrors() > 0) {
            UIHelpers.cardRemoveWithMessage(d, mal, REMOVE_CARD, true, false, trans);
        }
    }
}
