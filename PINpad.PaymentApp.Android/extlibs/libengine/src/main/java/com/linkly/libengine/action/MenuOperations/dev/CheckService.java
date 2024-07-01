package com.linkly.libengine.action.MenuOperations.dev;

import static com.linkly.libui.UIScreenDef.SERVICE_STATUS_NO_PENDING_EVENTS;
import static com.linkly.libui.UIScreenDef.SERVICE_STATUS_PENDING_EVENTS;

import com.linkly.libengine.action.IAction;

public class CheckService extends IAction {

    @Override
    public String getName() {
        return "CheckService";
    }

    @Override
    public void run() {
        ui.showScreen(d.getJobs().pending() ? SERVICE_STATUS_PENDING_EVENTS : SERVICE_STATUS_NO_PENDING_EVENTS);
        ui.displayMainMenuScreen();
    }
}
