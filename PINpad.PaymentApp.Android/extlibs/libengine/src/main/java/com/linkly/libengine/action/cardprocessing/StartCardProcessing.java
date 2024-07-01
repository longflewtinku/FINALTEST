package com.linkly.libengine.action.cardprocessing;

import static com.linkly.libengine.engine.EngineManager.TransType.TOPUPCOMPLETION;

import com.linkly.libengine.action.IAction;
import com.linkly.libengine.engine.cards.Ctls;
import com.linkly.libengine.engine.cards.Emv;
import com.linkly.libengine.engine.cards.Msr;
import com.linkly.libengine.engine.transactions.properties.TCard;

import timber.log.Timber;

public class StartCardProcessing extends IAction {
    @Override
    public String getName() {
        return "StartCardProcessing";
    }

    @Override
    public void run() {

        TCard card = trans.getCard();
        if (trans.getTransType() == TOPUPCOMPLETION && card.getCardType() == TCard.CardType.EMV) {
            Timber.i("Restart the emv process for topup completions");
        } else if (card.getCardIndex() != -1) {
            Timber.i("We already have the card details read");
            return;
        }

        switch(card.getCardType()) {
            case MSR:
                // Already detected from the PollingResult.
                Msr.getInstance().start(d);
                break;
            case EMV:
                Emv.getInstance().start(d);
                break;
            case CTLS:
                Ctls.getInstance().start(d);
                break;
            case MANUAL:        // Fall-through to default
            default:
                break;
        }
    }
}
