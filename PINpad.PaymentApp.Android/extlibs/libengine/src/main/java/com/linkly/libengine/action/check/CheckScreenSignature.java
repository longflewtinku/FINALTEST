package com.linkly.libengine.action.check;

import static com.linkly.libengine.engine.transactions.properties.TAudit.ReasonOnlineCode.RTIME_CARD_ACCEPTOR_SUSPICIOUS;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.ReversalReason.SIGFAIL;
import static com.linkly.libui.UIScreenDef.GET_SIGNATURE;

import com.linkly.libengine.action.IAction;
import com.linkly.libengine.action.TransactionDecliner;
import com.linkly.libengine.action.user_action.UiApproved;
import com.linkly.libengine.debug.IDebug;
import com.linkly.libengine.engine.protocol.IProto;
import com.linkly.libengine.overrides.CoreOverrides;
import com.linkly.libmal.global.platform.EFTPlatform;
import com.linkly.libui.IUIDisplay;

public class CheckScreenSignature extends IAction {

    @Override
    public String getName() {
        return "CheckScreenSignature";
    }

    @Override
    public void run() {

        if (!CoreOverrides.get().isAutoFillTrans()) {
            if (!EFTPlatform.printToScreen() || trans.getAudit().isDisablePrinting())
                return;

            if (trans.isSignatureRequired() && !trans.getAudit().isSignatureChecked()) {

                d.getUI().showScreen(GET_SIGNATURE);
                IUIDisplay.UIResultCode res = d.getUI().getResultCode(IUIDisplay.ACTIVITY_ID.ACT_SIG, IUIDisplay.LONG_TIMEOUT);

                if (res == IUIDisplay.UIResultCode.OK) {
                    d.getDebugReporter().reportSignatureKeyPressed( IDebug.DEBUG_KEY.YES );
                    trans.getAudit().setSignatureChecked(true);
                    super.d.getWorkflowEngine().setNextAction(UiApproved.class);
                } else {
                    trans.setToReverse(SIGFAIL);
                    super.d.getProtocol().setInternalRejectReason( super.trans, IProto.RejectReasonType.SIGNATURE_REJECTED );
                    d.getDebugReporter().reportSignatureKeyPressed( IDebug.DEBUG_KEY.NO );
                    d.getWorkflowEngine().setNextAction(TransactionDecliner.class);
                    trans.getAudit().setReasonOnlineCode(RTIME_CARD_ACCEPTOR_SUSPICIOUS);
                }
                trans.save();
            }
        }


    }
}
