package com.linkly.libengine.workflow;

import com.linkly.libengine.action.IPC.PopulateTransaction;
import com.linkly.libengine.action.IPC.TempLogin;
import com.linkly.libengine.action.IPC.TempLogout;
import com.linkly.libengine.action.IPC.TransResponse;
import com.linkly.libengine.action.IPC.UserUndoUpgrade;
import com.linkly.libengine.action.cardprocessing.DCCProcessing;
import com.linkly.libengine.action.cardprocessing.FallbackRemoveCard;
import com.linkly.libengine.action.cardprocessing.GetCard;
import com.linkly.libengine.action.cardprocessing.GetMOTO;
import com.linkly.libengine.action.cardprocessing.ManualProcessing;
import com.linkly.libengine.action.check.CheckPassword;
import com.linkly.libengine.action.check.CheckUserLevel;
import com.linkly.libengine.action.check.TimeNeedsFixing;
import com.linkly.libengine.action.user_action.ConfirmSurcharge;
import com.linkly.libengine.action.user_action.EnsureDisplayActIdle;
import com.linkly.libengine.action.user_action.InputAmount;
import com.linkly.libengine.action.user_action.InputReference;
import com.linkly.libengine.action.user_action.InputTip;
import com.linkly.libengine.action.user_action.MainMenu;

public class AutoSaleMOTO extends CommonPayment{

        public AutoSaleMOTO()
        {
                // initial processing changes

                this.removeAction(InputAmount.class);
                this.removeAction(InputTip.class);
                this.removeAction(InputReference.class);

                // Restrict input to only allow moto
                this.insertAfterAction( new CheckPassword(), CheckUserLevel.class );
                this.insertAfterAction(new ManualProcessing(), CheckPassword.class);
                this.removeAction(GetCard.class);
                this.insertAfterAction(new GetMOTO(), ManualProcessing.class);
                this.insertAfterAction(new EnsureDisplayActIdle(), GetMOTO.class);
                this.insertAfterAction( new ConfirmSurcharge(), EnsureDisplayActIdle.class );

                this.insertAfterAction(new PopulateTransaction(), FallbackRemoveCard.class);
                this.insertAfterAction(new TempLogin(), PopulateTransaction.class);

                //card processing changes
                this.removeAction(DCCProcessing.class);

                //finishing state changes
                this.removeAction(UserUndoUpgrade.class);
                this.removeAction(TimeNeedsFixing.class);
                this.removeAction(MainMenu.class);

                this.insertAfterAction(new TempLogout(), TransResponse.class);
        }
}
