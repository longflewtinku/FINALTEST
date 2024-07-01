package com.linkly.libengine.action.cardprocessing;

import static com.linkly.libui.UIScreenDef.REMOVE_CARD;

import com.linkly.libengine.action.IAction;
import com.linkly.libengine.engine.transactions.properties.TCard;
import com.linkly.libengine.helpers.UIHelpers;
import com.linkly.libengine.overrides.CoreOverrides;

public class RemoveCard extends IAction {
    @Override
    public String getName() {
        return "RemoveCard";
    }

    @Override
    public void run() {
        if (CoreOverrides.get().isDisableCardRemove()) {
            return;
        }

        // Only ICC Card should need to be removed.
        if( super.trans.getCard().getCaptureMethod() == TCard.CaptureMethod.ICC ) {

            UIHelpers.cardRemoveWithMessage( d, mal, REMOVE_CARD, true, false, trans );

        }
    }
}
