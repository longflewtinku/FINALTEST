package com.linkly.libengine.action;

import static com.linkly.libui.IUIDisplay.ACTIVITY_ID.ACT_INFORMATION;
import static com.linkly.libui.UIScreenDef.TRANS_NOT_IMPLEMENTED;

import com.linkly.libui.IUIDisplay;

public class NotImplemented extends IAction {
    @Override
    public String getName() {
        return "NotImplemented";
    }

    @Override
    public void run() {

        ui.showScreen(TRANS_NOT_IMPLEMENTED, trans.getTransType().displayId);
        ui.getResultCode(ACT_INFORMATION, IUIDisplay.SHORT_TIMEOUT);
    }
}
