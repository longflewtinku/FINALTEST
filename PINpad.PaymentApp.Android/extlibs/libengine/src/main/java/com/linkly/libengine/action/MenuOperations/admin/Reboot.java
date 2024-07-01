package com.linkly.libengine.action.MenuOperations.admin;

import static com.linkly.libui.IUIDisplay.ACTIVITY_ID.ACT_REBOOT;
import static com.linkly.libui.UIScreenDef.ACT_REBOOT_SCREEN;

import com.linkly.libengine.action.IAction;
import com.linkly.libmal.MalFactory;
import com.linkly.libui.IUIDisplay;

import java.util.HashMap;

public class Reboot extends IAction {

    @Override
    public String getName() {
        return "Reboot";
    }

    @Override
    public void run() {
        if (d.getProfileCfg().isUnattendedModeAllowed()) {
            // if unattended mode do not show anything, just reboot
            MalFactory.getInstance().getHardware().reboot();
        } else {
            HashMap<String, Object> map = new HashMap<>();
            ui.showScreen(ACT_REBOOT_SCREEN, map);
            ui.getResultCode(ACT_REBOOT, IUIDisplay.MEDIUM_TIMEOUT);
        }
    }
}
