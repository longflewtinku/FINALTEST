package com.linkly.libengine.action.MenuOperations.dev;

import static com.linkly.libui.UIScreenDef.ICC_DIAGS_DISABLED;
import static com.linkly.libui.UIScreenDef.ICC_DIAGS_ENABLED;

import com.linkly.libengine.action.IAction;
import com.linkly.libengine.env.IccDiags;

public class IccDiagnostics extends IAction {
    @Override
    public String getName() {
        return "IccDiagnostics";
    }

    @Override
    public void run() {
        if (!IccDiags.getCurValue()) {
            IccDiags.setNewValue(true);
            ui.showScreen(ICC_DIAGS_ENABLED);
        } else {
            IccDiags.setNewValue(false);
            ui.showScreen(ICC_DIAGS_DISABLED);
        }
        ui.displayMainMenuScreen();
    }
}
