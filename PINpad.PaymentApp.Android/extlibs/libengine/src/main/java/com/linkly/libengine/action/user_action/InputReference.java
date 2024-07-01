package com.linkly.libengine.action.user_action;

import static com.linkly.libui.IUIDisplay.ACTIVITY_ID.ACT_INPUT;
import static com.linkly.libui.IUIDisplay.UIResultCode.OK;
import static com.linkly.libui.UIScreenDef.ENTER_REFERENCE;
import static com.linkly.libui.UIScreenDef.REFERENCE_VAR;

import com.linkly.libengine.action.IAction;
import com.linkly.libengine.action.TransactionCanceller;
import com.linkly.libengine.overrides.CoreOverrides;
import com.linkly.libui.IUIDisplay;
import com.linkly.libui.UIScreenDef;

import java.util.HashMap;

public class InputReference extends IAction {
    @Override
    public String getName() {
        return "InputReference";
    }

    @Override
    public void run() {

            if (CoreOverrides.get().isEnablePositiveDemo())
                return;

            if (!d.getCustomer().supportCtlsReferences() && trans.getCard().isCtlsCaptured()) {
                return;
            }

            if (trans.getAudit().getReference() != null && !trans.getAudit().getReference().isEmpty())
                return;

            if (trans.getAudit().isSkipReference())
                return;

            if (d.getPayCfg().isReferenceEnabled(DisplayCNP.getRefRequiredSetting(d))) {

                HashMap<String, Object> map = new HashMap<>();
                UIScreenDef screenDef;
                boolean isReferenceOptional = d.getPayCfg().isReferenceOptional(DisplayCNP.getRefRequiredSetting(d));

                if (d.getPayCfg().getCustRefPrompt() != null && !d.getPayCfg().getCustRefPrompt().isEmpty()) {
                    screenDef = REFERENCE_VAR;
                    map.put(IUIDisplay.uiPromptIdArg, d.getPayCfg().getCustRefPrompt());
                } else {
                    screenDef = ENTER_REFERENCE;
                }

                // only if it's optional
                if (isReferenceOptional) {
                    map.put(IUIDisplay.uiSkipButtonOn, true);
                }
                else{
                    map.put(IUIDisplay.uiScreenMinLen, 1);
                }
                ui.showInputScreen(screenDef, map );
                IUIDisplay.UIResultCode res = ui.getResultCode(ACT_INPUT, IUIDisplay.LONG_TIMEOUT);
                if (res == OK) {
                    String result = ui.getResultText(ACT_INPUT, IUIDisplay.uiResultText1);
                    if (result == null || result.isEmpty())
                        trans.getAudit().setSkipReference(true);
                    else
                        trans.getAudit().setReference(result);
                } else {
                    d.getWorkflowEngine().setNextAction(TransactionCanceller.class);
                }
            }
    }
}
