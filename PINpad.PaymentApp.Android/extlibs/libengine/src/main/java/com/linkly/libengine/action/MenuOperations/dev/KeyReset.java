package com.linkly.libengine.action.MenuOperations.dev;

import static com.linkly.libui.UIScreenDef.KEYS_RESET_SUCCESS;

import com.linkly.libengine.action.IAction;

public class KeyReset extends IAction {

    @Override
    public String getName() {
        return "KeyReset";
    }

    @Override
    public void run() {

        ui.showScreen(KEYS_RESET_SUCCESS);
        ui.displayMainMenuScreen();
    }
}
