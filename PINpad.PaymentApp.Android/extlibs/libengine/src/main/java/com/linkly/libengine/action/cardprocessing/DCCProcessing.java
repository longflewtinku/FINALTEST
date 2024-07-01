package com.linkly.libengine.action.cardprocessing;

import com.linkly.libengine.action.IAction;

import timber.log.Timber;

public class DCCProcessing extends IAction {
    @Override
    public String getName() {
        return "DCCProcessing";
    }

    @Override
    public void run() {
        Timber.e("DCC is not yet supported!");
    }
}
