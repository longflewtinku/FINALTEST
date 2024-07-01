package com.linkly.libengine.action.user_action;

import static com.linkly.libui.IUICurrency.EAmountFormat.FMT_AMT_SHOW_SYMBOL;
import static com.linkly.libui.UIScreenDef.CONFIRM_SURCHARGE_OKAY;

import com.linkly.libengine.action.IAction;
import com.linkly.libengine.action.TransactionCanceller;
import com.linkly.libengine.engine.protocol.IProto;
import com.linkly.libengine.helpers.UIHelpers;
import com.linkly.libengine.status.IStatus;
import com.linkly.libui.IUIDisplay;

/**
 * Displays a prompt to the user to confirm if they are okay with the surcharge amount added
 * Will also send a status message to the POS to replicate the dialog options
 * If Yes -> Transaction proceeds
 * If No  -> Transaction cancels
 */
public class ConfirmSurcharge extends IAction {
    @Override
    public String getName() {
        return "ConfirmSurcharge";
    }

    @Override
    public void run() {
        final long SURCHARGE_AMOUNT = super.trans.getAmounts().getSurcharge();
        if( SURCHARGE_AMOUNT > 0 ) {
            final String SURCHARGE_AMOUNT_FORMATTED =
                    super.curr.formatUIAmount(
                            Long.toString( SURCHARGE_AMOUNT ),
                            FMT_AMT_SHOW_SYMBOL,
                            d.getPayCfg().getCountryCode() );

            super.d.getStatusReporter().reportStatusEvent( IStatus.STATUS_EVENT.STATUS_CONFIRM_SURCHARGE , trans.isSuppressPosDialog() );
            UIHelpers.YNQuestion ynQuestion = UIHelpers.uiYesNoCancelQuestion( d, CONFIRM_SURCHARGE_OKAY,
                    IUIDisplay.String_id.STR_CONFIRM_SURCHARGE,
                    SURCHARGE_AMOUNT_FORMATTED );

            if ( ynQuestion != UIHelpers.YNQuestion.YES ) {
                super.d.getProtocol().setInternalRejectReason(
                        super.trans,
                        IProto.RejectReasonType.CANCELLED );
                super.d.getWorkflowEngine().setNextAction( TransactionCanceller.class );
            }
        }
    }
}
