package com.linkly.libengine.workflow;

import com.linkly.libengine.action.HostActions.PreAuthorisation;
import com.linkly.libengine.action.IPC.CompletionCheckDetails;
import com.linkly.libengine.action.IPC.GetPreauthByCardData;
import com.linkly.libengine.action.IPC.GetPreauthByRfnTag;
import com.linkly.libengine.action.IPC.PopulateTransaction;
import com.linkly.libengine.action.IPC.TempLogin;
import com.linkly.libengine.action.IPC.TempLogout;
import com.linkly.libengine.action.IPC.TransResponse;
import com.linkly.libengine.action.cardprocessing.CardPostcomms;
import com.linkly.libengine.action.cardprocessing.DCCProcessing;
import com.linkly.libengine.action.cardprocessing.FallbackRemoveCard;
import com.linkly.libengine.action.cardprocessing.GetCard;
import com.linkly.libengine.action.cardprocessing.ICCGAC1;
import com.linkly.libengine.action.check.CheckOfflineAllowed;
import com.linkly.libengine.action.check.CheckUserLevel;
import com.linkly.libengine.action.check.IsSignatureRequired;
import com.linkly.libengine.action.check.TimeNeedsFixing;
import com.linkly.libengine.action.user_action.DisplaySummary;
import com.linkly.libengine.action.user_action.InputAccount;
import com.linkly.libengine.action.user_action.InputAmount;
import com.linkly.libengine.action.user_action.InputCashback;
import com.linkly.libengine.action.user_action.InputReference;
import com.linkly.libengine.action.user_action.InputTip;
import com.linkly.libengine.action.user_action.MainMenu;
import com.linkly.libengine.action.user_action.UiInputOnlinePin;

public class AutoCompletion extends CommonPayment {
    public AutoCompletion() {

        // initial processing changes

        this.removeAction(InputAmount.class);
        this.removeAction(InputTip.class);
        this.removeAction(InputReference.class);

        // card no present completions - lookup card details based off RFN tag, if integrated.
        // if no RFN tag we need to prompt for card to be presented - includes standalone or integrated with no RFN tag
        // NOTE: jumps to CompletionCheckDetails if we have card details
        this.insertBeforeAction(new GetPreauthByRfnTag(), GetCard.class);

        this.insertAfterAction(new PopulateTransaction(), FallbackRemoveCard.class);
        this.insertAfterAction(new TempLogin(), PopulateTransaction.class);

        // allow transaction offline, remove conditional CheckOfflineAllowed checks from CommonPayment
        this.removeAction(CheckOfflineAllowed.class);
        // and replace with unconditional call to CheckOfflineAllowed
        this.insertBeforeAction(new CheckOfflineAllowed(), CheckUserLevel.class);

        // insert get preauth by card, and completion check details before preauth
        this.insertBeforeAction(new GetPreauthByCardData(), PreAuthorisation.class);
        this.insertAfterAction(new CompletionCheckDetails(), GetPreauthByCardData.class);


        this.removeConstrainedActions(InputAccount.class); // not required for card identification
        this.removeAction(InputCashback.class);
        this.removeAction(DCCProcessing.class);
        this.removeAction(DisplaySummary.class);
        this.removeAction(ICCGAC1.class);
        this.removeAction(IsSignatureRequired.class);
        this.removeAction(UiInputOnlinePin.class);
        this.removeAction(CardPostcomms.class);

        //finishing state changes
        this.removeAction(TimeNeedsFixing.class);
        this.removeAction(MainMenu.class);
        this.insertAfterAction(new TempLogout(), TransResponse.class);

    }
}
