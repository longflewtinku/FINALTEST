package com.linkly.libengine.action.check;

import static com.linkly.libengine.engine.transactions.properties.TAudit.ReasonOnlineCode.RTIME_CARD_ACCEPTOR_SUSPICIOUS;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.ReversalReason.SIGFAIL;
import static com.linkly.libengine.status.IStatus.STATUS_EVENT.STATUS_ERR_GENAC2_FAILED;
import static com.linkly.libengine.status.IStatus.STATUS_EVENT.STATUS_OPERATOR_TIMEOUT;
import static com.linkly.libengine.status.IStatus.STATUS_EVENT.STATUS_SIGNATURE;
import static com.linkly.libengine.status.IStatus.STATUS_EVENT.STATUS_SIGNATURE_OKAY;
import static com.linkly.libui.IUIDisplay.String_id.STR_SIG_CORRECT;
import static com.linkly.libui.IUIDisplay.UIResultCode.POS_YES;
import static com.linkly.libui.IUIDisplay.UIResultCode.TIMEOUT;

import com.linkly.libconfig.uiconfig.ConfigTimeouts;
import com.linkly.libengine.action.IAction;
import com.linkly.libengine.action.TransactionDecliner;
import com.linkly.libengine.action.user_action.UiApproved;
import com.linkly.libengine.debug.IDebug;
import com.linkly.libengine.engine.protocol.IProto;
import com.linkly.libengine.helpers.UIHelpers;
import com.linkly.libengine.overrides.CoreOverrides;
import com.linkly.libmal.global.platform.EFTPlatform;
import com.linkly.libui.IUIDisplay;
import com.linkly.libui.UIScreenDef;

public class CheckSignature extends IAction {
    @Override
    public String getName() {
        return "CheckSignature";
    }

    @Override
    public void run() {
        final int SCREEN_TIMEOUT =
                super.d.getPayCfg().getUiConfigTimeouts().
                getTimeoutMilliSecs(
                        ConfigTimeouts.CONFIRM_SIGNATURE_TIMEOUT,
                        super.trans.getAudit().isAccessMode()
                );

        if ( CoreOverrides.get().isAutoFillTrans() || !EFTPlatform.isAppPrinting() || super.trans.getAudit().isDisablePrinting() )
            return;

        if (super.trans.isSignatureRequired() && !super.trans.getAudit().isSignatureChecked()) {
            /*
               Suppress POS inputs like yes/no. Payment app will continue to support cancellation requests from the POS
               PAT 1 (enabled with dialogs) The behaviour of the terminal is as normal however all user input must be driven through the terminal itself
               (i.e.. Print Customer Copy Y/N) rather than prompting on the POS.
             */
            super.d.getStatusReporter().reportStatusEvent(trans.isPatMode("1") ? STATUS_SIGNATURE : STATUS_SIGNATURE_OKAY, trans.isSuppressPosDialog());

            boolean signOkay = false;

            /*
                TODO:
                Only PAT mode 1 is supported for signature prompt to enable terminal inputs. We need to support PAT mode 2 by suppressing all dialogs to POS.
                Recommendation is to identify the PAT mode early in the Transaction event processing and suppress all POS display messages

                Reference: Linkly Terminal Developer Specification, Section 9.7, "Tags from POS to PIN pad"
                Pay at terminal mode tag, when this is provided the terminal must alter some of its behaviour to ensure that the solution operates as it should.
                    0 (disabled) This indicates that the terminal should perform as normal and should be treated the same as if the PAT tag was not present.
                    1 (enabled with dialogs) The behaviour of the terminal is as normal however all user input must be driven through the terminal itself
                      (i.e.. Print Customer Copy Y/N) rather than prompting on the POS.
                    2 (enabled without dialogs) The same behaviour as option 1, however all dialog (display) messages must be suppressed.
            */
            if (super.trans.getTransType().autoTransaction && !(trans.getTransEvent() != null && trans.getTransEvent().isUseTerminalPrinterForSignatureCheck()) && !(trans.isPatMode("1") || trans.isPatMode("2"))) {
                super.d.getUI().showScreen( UIScreenDef.SIGNATURE_REQUIRED, super.trans.getTransType().displayId );
                IUIDisplay.UIResultCode uiResultCode = d.getUI().getResultCode( IUIDisplay.ACTIVITY_ID.ACT_INFORMATION, SCREEN_TIMEOUT );
                if ( POS_YES == uiResultCode ) {
                    signOkay = true;
                } else if ( TIMEOUT == uiResultCode ) {
                    // send timeout message
                    d.getStatusReporter().reportStatusEvent( STATUS_OPERATOR_TIMEOUT , trans.isSuppressPosDialog());
                }

            } else {
                signOkay = UIHelpers.uiYesNoQuestion( super.d, "", super.d.getPrompt( STR_SIG_CORRECT ) + "?", SCREEN_TIMEOUT );
            }

            if ( signOkay ) {
                super.trans.getAudit().setSignatureChecked( true );
                super.d.getWorkflowEngine().setNextAction(UiApproved.class);
                d.getDebugReporter().reportSignatureKeyPressed( IDebug.DEBUG_KEY.YES );
            } else {
                super.d.getStatusReporter().reportStatusEvent( STATUS_ERR_GENAC2_FAILED , trans.isSuppressPosDialog());
                super.trans.setToReverse( SIGFAIL );
                super.d.getWorkflowEngine().setNextAction( TransactionDecliner.class );
                super.trans.getAudit().setReasonOnlineCode( RTIME_CARD_ACCEPTOR_SUSPICIOUS );
                super.d.getProtocol().setInternalRejectReason( super.trans, IProto.RejectReasonType.SIGNATURE_REJECTED );
                d.getDebugReporter().reportSignatureKeyPressed( IDebug.DEBUG_KEY.NO );

                super.trans.save();
            }
        }
    }
}
