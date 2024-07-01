package com.linkly.libengine.workflow;

import com.linkly.libengine.action.HostActions.PreAuthorisation;
import com.linkly.libengine.action.IPC.CompletionCheckDetails;
import com.linkly.libengine.action.IPC.GetPreauthByCardData;
import com.linkly.libengine.action.cardprocessing.CardPostcomms;
import com.linkly.libengine.action.cardprocessing.DCCProcessing;
import com.linkly.libengine.action.cardprocessing.GetCard;
import com.linkly.libengine.action.cardprocessing.ICCGAC1;
import com.linkly.libengine.action.check.CheckOfflineAllowed;
import com.linkly.libengine.action.check.CheckUserLevel;
import com.linkly.libengine.action.user_action.DisplaySummary;
import com.linkly.libengine.action.user_action.InputAccount;
import com.linkly.libengine.action.user_action.InputCardholderPresent;
import com.linkly.libengine.action.user_action.InputCashback;
import com.linkly.libengine.action.user_action.InputPreAuthAuthCode;
import com.linkly.libengine.action.user_action.InputPreAuthRRN;
import com.linkly.libengine.action.user_action.UiInputOnlinePin;

public class Completion extends CommonPayment {
    public Completion() {
        //initial flow changes
        this.insertBeforeAction(new InputCardholderPresent(), GetCard.class);

        // add in checking of RRN
        this.insertBeforeAction(new InputPreAuthRRN(), GetCard.class);

        // get last auth code from preauth and load the record
        this.insertAfterAction(new InputPreAuthAuthCode(), InputPreAuthRRN.class);

        // allow transaction offline, remove conditional CheckOfflineAllowed checks from CommonPayment
        this.removeAction(CheckOfflineAllowed.class);
        // and replace with unconditional call to CheckOfflineAllowed
        this.insertBeforeAction(new CheckOfflineAllowed(), CheckUserLevel.class);

        // insert get preauth by card, and completion check details before preauth
        this.insertBeforeAction(new GetPreauthByCardData(), PreAuthorisation.class);
        this.insertAfterAction(new CompletionCheckDetails(), GetPreauthByCardData.class);

        // card processing changes
        this.removeConstrainedActions(InputAccount.class); // not required for card identification
        this.removeAction(InputCashback.class);
        this.removeAction(DCCProcessing.class);
        this.removeAction(DisplaySummary.class);
        this.removeAction(ICCGAC1.class);
        this.removeAction(UiInputOnlinePin.class);
        this.removeAction(CardPostcomms.class);
    }
}
