package com.linkly.libengine.action.MenuOperations.admin;

import static com.linkly.libui.UIScreenDef.PROCESSING_PLEASE_WAIT_SHORT;

import com.linkly.libengine.action.IAction;

public class InitiateRKI extends IAction {
    @Override
    public String getName() {
        return "InitiateRKI";
    }

    @Override
    public void run() {
        ui.showScreen(PROCESSING_PLEASE_WAIT_SHORT);

        // perform RKI
        if(d.getP2PLib() != null) {
            d.getP2PLib().getIP2PSec().as2805GetKeys(d.getCustomer().getTcuKeyLength());
        }
    }
}
