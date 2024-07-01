package com.linkly.libengine.action.check;

import static com.linkly.libengine.status.IStatus.STATUS_EVENT.STATUS_ERR_CARD_NUMBER_INVALID;
import static com.linkly.libengine.status.IStatus.STATUS_EVENT.STATUS_ERR_CARD_TYPE_NOT_ALLOWED;
import static com.linkly.libui.IUIDisplay.ACTIVITY_ID.ACT_INFORMATION;
import static com.linkly.libui.UIScreenDef.CARD_TYPE_NOT_ALLOWED;
import static com.linkly.libui.UIScreenDef.INVALID_CARD_NUMBER_RE_ENTER;

import com.linkly.libconfig.cpat.CardProductCfg;
import com.linkly.libengine.action.IAction;
import com.linkly.libengine.action.TransactionCanceller;
import com.linkly.libengine.engine.transactions.properties.TCard;
import com.linkly.libsecapp.IP2PEncrypt;
import com.linkly.libsecapp.P2PLib;
import com.linkly.libui.IUIDisplay;

import java.util.StringTokenizer;

import timber.log.Timber;

public class CheckPAN extends IAction {
    @Override
    public String getName() {
        return "CheckPAN";
    }

    @Override
    public void run() {
        boolean bChecked = false;

        TCard cardinfo = trans.getCard();
        CardProductCfg cardsConfig = cardinfo.getCardsConfig(d.getPayCfg());

        String panLengths = cardsConfig.getPanLength();

        StringTokenizer st2 = new StringTokenizer(panLengths, ",");

        while (st2.hasMoreElements()) {
            String s = (String) st2.nextElement();
            int length = Integer.parseInt(s);
            if (length == cardinfo.getPan().length()) {
                Timber.i( "PAN checked at length: " + length);
                bChecked = true;
                break;
            }
        }

        if (!bChecked) {
            d.getStatusReporter().reportStatusEvent(STATUS_ERR_CARD_TYPE_NOT_ALLOWED , trans.isSuppressPosDialog());
            ui.showScreen(CARD_TYPE_NOT_ALLOWED);
            d.getWorkflowEngine().setNextAction(TransactionCanceller.class);

            return;
        }

        if (cardsConfig.isLuhnCheck()) {
            // call p2pe to check the LUHN digit
            IP2PEncrypt p2pEncrypt = d.getP2PLib().getIP2PEncrypt();

            if (!p2pEncrypt.checkLuhn()) {
                d.getStatusReporter().reportStatusEvent(STATUS_ERR_CARD_NUMBER_INVALID , trans.isSuppressPosDialog());
                ui.showScreen(INVALID_CARD_NUMBER_RE_ENTER);
                ui.getResultCode(ACT_INFORMATION, IUIDisplay.SHORT_TIMEOUT);

                d.getWorkflowEngine().setNextAction(TransactionCanceller.class);
            }
        }
    }
}
