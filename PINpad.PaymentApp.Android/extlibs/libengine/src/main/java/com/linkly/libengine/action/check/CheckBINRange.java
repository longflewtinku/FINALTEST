package com.linkly.libengine.action.check;

import static com.linkly.libui.IUIDisplay.ACTIVITY_ID.ACT_INFORMATION;
import static com.linkly.libui.UIScreenDef.CARD_NOT_ACCEPTED;
import static com.linkly.libui.UIScreenDef.CARD_NOT_READ_PROP;
import static com.linkly.libui.UIScreenDef.CARD_TYPE_NOT_ALLOWED;
import static com.linkly.libui.UIScreenDef.PLB_RESTRICTED_ITEM;

import com.linkly.libconfig.cpat.CardProductCfg;
import com.linkly.libengine.action.IAction;
import com.linkly.libengine.action.TransactionCanceller;
import com.linkly.libengine.action.TransactionDecliner;
import com.linkly.libengine.dependencies.IDependency;
import com.linkly.libengine.engine.protocol.IProto;
import com.linkly.libengine.engine.transactions.TransRec;
import com.linkly.libengine.engine.transactions.properties.TCard;
import com.linkly.libpositive.wrappers.TagDataFromPOS;
import com.linkly.libui.IUIDisplay;

import timber.log.Timber;

public class CheckBINRange extends IAction {
    private static final int MAX_ISO_TRACK_2_LENGTH = 40;

    public static boolean runBinRangeChecking(IDependency d, boolean cancelOnFailure) {
        TransRec trans = d.getCurrentTransaction();
        TCard card = trans.getCard();
        IUIDisplay ui = d.getUI();

        int storedIndex = card.getCardIndex();
        if (storedIndex < 0) {
            String pan = card.getPan();

            /* check the pan is valid */
            if (pan == null || pan.length() <= 12) {
                ui.showScreen(CARD_NOT_READ_PROP);
                ui.getResultCode(IUIDisplay.ACTIVITY_ID.ACT_INFORMATION, IUIDisplay.SHORT_TIMEOUT);
                if (cancelOnFailure) {
                    d.getWorkflowEngine().setNextAction(TransactionCanceller.class);
                }
                return false;
            }

            int index = d.getBinRangesCfg().getCardsCfgIndex(d.getPayCfg(), pan);
            if (index >= 0) {
                Timber.i("checkBinRange: OK %d", index);
                card.setCardIndex(index);
                storedIndex = index;
            } else {
                ui.showScreen(CARD_TYPE_NOT_ALLOWED);
                if (cancelOnFailure) {
                    d.getWorkflowEngine().setNextAction(TransactionCanceller.class);
                }
                return false;
            }
        } else {
            Timber.i("checkBinRange: OK %s", storedIndex);
        }

        // if full track 2, check it's <= 40 chars in length. Reject if > 40 chars
        String track2 = card.getTrack2();
        Timber.i("track 2 len = %d", track2==null?-1:track2.length());
        if (track2 != null && track2.length() > MAX_ISO_TRACK_2_LENGTH) {
            Timber.i("Rejecting card with track 2 length too long, length=%d, max=%d", track2.length(), MAX_ISO_TRACK_2_LENGTH);
            ui.showScreen(CARD_NOT_ACCEPTED);
            d.getProtocol().setInternalRejectReason(trans, IProto.RejectReasonType.CARD_NOT_ACCEPTED);
            if (cancelOnFailure) {
                d.getWorkflowEngine().setNextAction(TransactionCanceller.class);
            }
            return false;
        }

        // if we're here then we have a valid index value
        // BIN range matched to a range in our table. Now check if card scheme has been disabled (e.g. by issuer table disable setting)
        CardProductCfg cardsConfig = d.getBinRangesCfg().getCardProductCfg(d.getPayCfg(), storedIndex);
        if (cardsConfig != null) {
            if (cardsConfig.isDisabled()) {
                Timber.i("MAGSTRIPE disallowing MSR card because scheme is disabled, for scheme name : %s", cardsConfig.getSchemeLabel());
                ui.showScreen(CARD_TYPE_NOT_ALLOWED);
                d.getProtocol().setInternalRejectReason(trans, IProto.RejectReasonType.CARD_TYPE_NOT_ALLOWED);
                if (cancelOnFailure) {
                    d.getWorkflowEngine().setNextAction(TransactionCanceller.class);
                }
                return false;
            } else if (trans.getCard().getCaptureMethod() == TCard.CaptureMethod.CTLS && cardsConfig.isRejectCtls() ||
                    trans.getCard().getCaptureMethod() == TCard.CaptureMethod.ICC && cardsConfig.isRejectEmv()) {
                ui.showScreen(CARD_TYPE_NOT_ALLOWED);
                d.getProtocol().setInternalRejectReason(trans, IProto.RejectReasonType.CARD_TYPE_NOT_ALLOWED);
                d.getWorkflowEngine().setNextAction(TransactionDecliner.class);

                return false;
            }
        }

        // Check if card is subject to the Product Level Blocking
        TagDataFromPOS tagData = trans.getTagDataFromPos();
        if (tagData != null && tagData.getPLB() != null && cardsConfig != null &&
            (tagData.getPLB().equals("1") && cardsConfig.isProductLevelBlocking())) {

            ui.showScreen(PLB_RESTRICTED_ITEM);
            ui.getResultCode(ACT_INFORMATION, IUIDisplay.SHORT_TIMEOUT);
            d.getProtocol().setInternalRejectReason(trans, IProto.RejectReasonType.PLB_RESTRICTED_ITEM);
            d.getWorkflowEngine().setNextAction(TransactionDecliner.class);
            return false;
        }

        card.setPsi(d.getBinRangesCfg().getCardProductCfg(d.getPayCfg(), card.getCardIndex()).getPsi());
        if (trans.getCard().getCaptureMethod() == TCard.CaptureMethod.MANUAL) {
            if (!trans.getCard().isManual() || !trans.getCard().getCardsConfig(d.getPayCfg()).getServicesAllowed().isMoto()) {
                trans.getCard().setCardholderPresent(true);
            } else {
                trans.getCard().setCardholderPresent(trans.getCard().isFaultyMsr());
            }
        }

        // Check if card is in blacklisted bin range.
        if (trans.isStartedInOfflineMode() && d.getConfig().getBlacklistCfg().isBlacklistedCard(d.getPayCfg(), d.getBinRangesCfg(), card.getTrack2())) {
            ui.showScreen(CARD_NOT_ACCEPTED);
            d.getProtocol().setInternalRejectReason(trans, IProto.RejectReasonType.CARD_NOT_ACCEPTED);
            d.getWorkflowEngine().setNextAction(TransactionCanceller.class);
            return false;
        }

        return true;
    }

    @Override
    public String getName() {
        return "CheckBINRange";
    }

    @Override
    public void run() {
        runBinRangeChecking(d, true);
    }
}
