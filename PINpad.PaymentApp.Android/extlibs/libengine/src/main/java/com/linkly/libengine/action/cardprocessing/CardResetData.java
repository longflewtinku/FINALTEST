package com.linkly.libengine.action.cardprocessing;

import com.linkly.libengine.action.IAction;

public class CardResetData extends IAction {
    @Override
    public String getName() {
        return "CardResetData";
    }

    @Override
    public void run() {
        d.getP2PLib().getIP2PMsr().resetReadResult();
    }
}
