package com.linkly.libengine.action.IPC;

import static com.linkly.libengine.engine.transactions.properties.TProtocol.MessageStatus.FINALISED_AND_REVERSED;
import static com.linkly.libengine.status.IStatus.STATUS_EVENT.STATUS_ERR_COMPLETION_AMT_EXCEEDS_PREAUTH_AMT;
import static com.linkly.libengine.status.IStatus.STATUS_EVENT.STATUS_ERR_COMPLETION_ERROR_PREAUTH_REVERSED;
import static com.linkly.libengine.status.IStatus.STATUS_EVENT.STATUS_ERR_PREAUTH_NOT_FOUND;
import static com.linkly.libui.IUIDisplay.String_id.STR_AUTH_CODE;
import static com.linkly.libui.IUIDisplay.String_id.STR_CONFIRM;
import static com.linkly.libui.IUIDisplay.String_id.STR_DATE;
import static com.linkly.libui.IUIDisplay.String_id.STR_RRN;
import static com.linkly.libui.IUIDisplay.String_id.STR_TOTAL_COLON;
import static com.linkly.libui.IUIDisplay.String_id.STR_WOW_CARD;
import static com.linkly.libui.UIScreenDef.EXCEEDS_PREAUTH_AMT;
import static com.linkly.libui.UIScreenDef.PREAUTH_NOT_FOUND;
import static com.linkly.libui.UIScreenDef.PREAUTH_WAS_CANCELLED;

import com.linkly.libengine.action.IAction;
import com.linkly.libengine.action.TransactionCanceller;
import com.linkly.libengine.engine.protocol.IProto;
import com.linkly.libengine.engine.transactions.TransRec;
import com.linkly.libengine.engine.transactions.TransRecManager;
import com.linkly.libengine.engine.transactions.properties.TCard;
import com.linkly.libengine.engine.transactions.properties.TSec;
import com.linkly.libengine.helpers.UIHelpers;
import com.linkly.libmal.global.util.Util;
import com.linkly.libui.IUICurrency;
import com.linkly.libui.IUIDisplay;
import com.linkly.libui.display.DisplayFragmentOption;

import java.util.ArrayList;

import timber.log.Timber;

public class CompletionCheckDetails extends IAction {
    private TransRec originalTxn;

    @Override
    public String getName() {
        return "CompletionCheckDetails";
    }

    @Override
    public void run() {
        if (!getOriginalTxn()) {
            // couldn't find original preauth, handle error
            ui.showScreen(PREAUTH_NOT_FOUND);
            d.getProtocol().setInternalRejectReason(trans, IProto.RejectReasonType.PREAUTH_NOT_FOUND);
            d.getStatusReporter().reportStatusEvent(STATUS_ERR_PREAUTH_NOT_FOUND , trans.isSuppressPosDialog());
            d.getWorkflowEngine().setNextAction(TransactionCanceller.class);
            return;
        }

        // check if preauth is in a state where it can be used
        if (originalTxn.getProtocol().getMessageStatus() == FINALISED_AND_REVERSED) {
            // this preauth was reversed, don't allow
            Timber.e("Preauth was reversed, can't do completion");
            ui.showScreen(PREAUTH_WAS_CANCELLED);
            d.getProtocol().setInternalRejectReason(trans, IProto.RejectReasonType.PREAUTH_ALREADY_CANCELLED);
            d.getStatusReporter().reportStatusEvent(STATUS_ERR_COMPLETION_ERROR_PREAUTH_REVERSED , trans.isSuppressPosDialog());
            d.getWorkflowEngine().setNextAction(TransactionCanceller.class);
            return;
        }

        // copy across data from original preauth
        copyOriginalPreauthDetails();

        if (completionAmountExceedsPreauthValue()) {
            Timber.e("Invalid Completion Amount");
            ui.showScreen(EXCEEDS_PREAUTH_AMT);
            d.getProtocol().setInternalRejectReason(trans, IProto.RejectReasonType.AMOUNT_EXCEEDS_PREAUTH);
            d.getStatusReporter().reportStatusEvent(STATUS_ERR_COMPLETION_AMT_EXCEEDS_PREAUTH_AMT , trans.isSuppressPosDialog());
            d.getWorkflowEngine().setNextAction(TransactionCanceller.class);
            return;
        }

        // Confirmation dialog | bypass for unattented mode.
        if (!d.getProfileCfg().isUnattendedModeAllowed() && !confirmCompletion()) {
            d.getWorkflowEngine().setNextAction(TransactionCanceller.class);
        }
    }

    private boolean getOriginalTxn() {
        if (trans.getPreauthUid() == null) {
            // shouldn't happen, trans.preauthUid should already be set before we get to this state
            Timber.e("Preauth UID is not set, but should be");
            return false;
        }

        originalTxn = TransRecManager.getInstance().getTransRecDao().getByUid(trans.getPreauthUid());
        // also shouldn't happen
        return originalTxn != null;
    }

    /**
     * retrieves card data and some protocol fields from original preauth txn
     */
    private void copyOriginalPreauthDetails() {
        // save cardholder present flag. Used to indicate if card is present at completion (not at preauth)
        boolean cardholderPresent = trans.getCard().isCardholderPresent();

        // deep copy the card object from retrieved txn into our current active txn
        trans.setCard(TCard.copy(originalTxn.getCard()));

        // zero out any CVM flags to avoid CVM required
        trans.getCard().setCvmType(TCard.CvmType.NO_CVM);

        // set cardholder present to FALSE for these RFN (cardholder not present) completions, so we don't prompt for PIN
        trans.getCard().setCardholderPresent(cardholderPresent);

        // copy security data because this is where encrypted card details are stored
        trans.setSecurity(TSec.copy(originalTxn.getSecurity()));

        // copy some other fields such as RRN, auth code, account type
        trans.getProtocol().setAuthCode(originalTxn.getProtocol().getAuthCode());
        trans.getProtocol().setRRN(originalTxn.getProtocol().getRRN());
        trans.getProtocol().setAccountType(originalTxn.getProtocol().getAccountType());
    }

    /**
     * checks preauth txn amount against completion amount
     * TODO: when incremental preauth top-ups, partial cancellations etc are implemented, we need to scan database for those also
     *
     * @return true = completion txn amount exceeds preauth, false = completion amt <= preauth amt
     */
    private boolean completionAmountExceedsPreauthValue() {
        boolean result = trans.getAmounts().getTotalAmount() > originalTxn.getAmounts().getTotalAmount();
        if (result) {
            Timber.e("Completion amount of %d exceeds preauth amount of %d", trans.getAmounts().getTotalAmount(), originalTxn.getAmounts().getTotalAmount());
        }
        return result;
    }

    private boolean confirmCompletion() {
        // Confirmation dialog
        ArrayList<DisplayFragmentOption> fragOptions = new ArrayList<>();
        String lastFour = "****";
        String panMasked = trans.getMaskedPan(TransRec.MaskType.CUSTOMER_MASK, d.getPayCfg());
        if (panMasked.length() > 4) {
            lastFour = panMasked.substring(panMasked.length() - 4);
        }
        fragOptions.add(new DisplayFragmentOption(d.getPrompt(STR_WOW_CARD), lastFour));

        String rrn = "";
        if (!Util.isNullOrWhitespace(trans.getProtocol().getRRN())) {
            rrn = trans.getProtocol().getRRN();
        }
        fragOptions.add(new DisplayFragmentOption(d.getPrompt(STR_RRN), rrn));

        String authCode = trans.getProtocol().getAuthCode();
        if (!Util.isNullOrWhitespace(authCode)) {
            fragOptions.add(new DisplayFragmentOption(d.getPrompt(STR_AUTH_CODE), authCode));
        }

        fragOptions.add(new DisplayFragmentOption(d.getPrompt(STR_DATE), originalTxn.getAudit().getTransDateTimeAsString("dd/MM/yyyy")));

        String totalAmount = String.format("%d", trans.getAmounts().getTotalAmount());
        String displayAmount = d.getFramework().getCurrency().formatAmount(totalAmount, IUICurrency.EAmountFormat.FMT_AMT_SHOW_SYMBOL, d.getPayCfg().getCountryCode());
        fragOptions.add(new DisplayFragmentOption(d.getPrompt(STR_TOTAL_COLON), displayAmount));

        return UIHelpers.uiYesNoQuestion(d, trans.getTransType().getDisplayName(), d.getPrompt(STR_CONFIRM), fragOptions, IUIDisplay.FRAG_TYPE.FRAG_GRID_GENERIC, 0);
    }
}
