package com.linkly.libengine.action.check;

import static com.linkly.libui.IUIDisplay.ACTIVITY_ID.ACT_INFORMATION;
import static com.linkly.libui.UIScreenDef.TERMINAL_NOT_CONFIGURED;

import com.linkly.libengine.action.IAction;
import com.linkly.libengine.action.TransactionCanceller;
import com.linkly.libengine.debug.IDebug;
import com.linkly.libui.IUIDisplay;

public class CheckConfig extends IAction {

    @Override
    public String getName() {
        return "CheckConfig";
    }

    @Override
    public void run() {
        if(!d.getConfig().isConfigLoaded()) {
            ui.showScreen(TERMINAL_NOT_CONFIGURED);
            ui.getResultCode(ACT_INFORMATION, IUIDisplay.SHORT_TIMEOUT);

            // send our debug messages
            if(d.getConfig().getConfigErrors() != null) {
                for (Exception e:d.getConfig().getConfigErrors()) {
                    d.getDebugReporter().reportDebugEvent(IDebug.DEBUG_EVENT.CONFIG_FAILURE, e.getMessage());
                }
            }

            d.getWorkflowEngine().setNextAction(TransactionCanceller.class);
        }
    }
}
