package com.linkly.libengine.action.cardprocessing;

import com.linkly.libengine.action.IAction;
import com.linkly.libengine.engine.cards.Ctls;
import com.linkly.libengine.engine.cards.Emv;
import com.linkly.libengine.engine.transactions.PanHash;
import com.linkly.libengine.engine.transactions.properties.TProtocol;
import com.linkly.libsecapp.P2PLib;

import timber.log.Timber;


public class CardPostcomms extends IAction {
    @Override
    public String getName() {
        return "CardPostcomms";
    }

    @Override
    public void run() {
        long genAC2 = 0;
        switch(trans.getCard().getCardType()) {
            case MANUAL:
            case MSR:
                if (trans.getProtocol().getHostResult() == TProtocol.HostResult.AUTHORISED) {
                    PanHash.setNewValue(trans.getPANHash(d.getPayCfg()));
                }
                break;
            case EMV:
                genAC2 = System.currentTimeMillis();
                Emv.getInstance().genAc2(d);
                break;
            case CTLS:
                genAC2 = System.currentTimeMillis();
                Ctls.getInstance().genAc2(d);
                break;
            default:
                break;
        }

        if(genAC2 != 0) {
            Timber.e("genAC2 Time Taken: %d", System.currentTimeMillis() - genAC2);
        }
    }
}
