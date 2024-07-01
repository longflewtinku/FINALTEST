package com.linkly.libengine.action.cardprocessing;

import com.linkly.libengine.action.IAction;
import com.linkly.libsecapp.IP2PEncrypt;

import timber.log.Timber;

public class CardWipeData extends IAction {

    @Override
    public String getName() {
        return "CardWipeData";
    }

    @Override
    public void run() {
        IP2PEncrypt p2pEncrypt = d.getP2PLib().getIP2PEncrypt();
        // erase PCI sensitive application data
        Timber.i( "call p2pEncrypt.clearData()" );
        p2pEncrypt.clearData();
    }
}
