package com.linkly.libengine.action.user_action;

import com.linkly.libengine.action.IAction;
import com.linkly.libengine.debug.IDebug;

public class BackToIdlePosNotification extends IAction {

    @Override
    public String getName() {
        return "BackToIdlePosNotification";
    }

    @Override
    public void run() {
        d.getDebugReporter().reportDebugEvent(IDebug.DEBUG_EVENT.BACK_TO_IDLE, null);
    }
}
