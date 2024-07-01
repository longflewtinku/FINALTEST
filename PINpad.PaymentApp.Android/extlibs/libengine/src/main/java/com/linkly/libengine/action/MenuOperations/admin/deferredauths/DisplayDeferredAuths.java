package com.linkly.libengine.action.MenuOperations.admin.deferredauths;

import static com.linkly.libui.IUIDisplay.ACTIVITY_ID.ACT_DEFERRED_AUTHS;
import static com.linkly.libui.IUIDisplay.String_id.STR_SHOW_DEFERRED_AUTHS;
import static com.linkly.libui.IUIDisplay.UIResultCode.OK;
import static com.linkly.libui.UIScreenDef.DISPLAY_DEFERRED_AUTHS;

import com.linkly.libengine.action.IAction;
import com.linkly.libengine.action.TransactionCanceller;
import com.linkly.libui.IUIDisplay;

import java.util.HashMap;

public class DisplayDeferredAuths extends IAction {
    @Override
    public String getName() {
        return "DisplayDeferredAuths";
    }

    @Override
    public void run() {
        //shows recent approved and declined deferred auths
            HashMap<String, Object> map = new HashMap<>();
            map.put(IUIDisplay.uiTitleId, STR_SHOW_DEFERRED_AUTHS);
            ui.showInputScreen(DISPLAY_DEFERRED_AUTHS, map );
            IUIDisplay.UIResultCode uiRes = ui.getResultCode(ACT_DEFERRED_AUTHS, IUIDisplay.LONG_TIMEOUT);

            if (uiRes == OK) {
                //Update later when required
            } else {
                d.getWorkflowEngine().setNextAction(TransactionCanceller.class);
            }
        }

}
