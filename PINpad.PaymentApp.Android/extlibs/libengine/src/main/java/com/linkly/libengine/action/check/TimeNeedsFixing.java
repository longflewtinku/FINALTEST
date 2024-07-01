package com.linkly.libengine.action.check;

import static com.linkly.libui.IUIDisplay.ACTIVITY_ID.ACT_BLANK;
import static com.linkly.libui.UIScreenDef.ACT_BLANK_SCREEN;
import static com.linkly.libui.UIScreenDef.TIMESYNC;

import com.linkly.libengine.action.IAction;
import com.linkly.libui.IUIDisplay;

import java.util.HashMap;

public class TimeNeedsFixing extends IAction {
    @Override
    public String getName() {
        return "TimeNeedsFixing";
    }

    @Override
    public void run() {

        if (trans.getProtocol().getErrorCode() == null || trans.getProtocol().getErrorCode().isEmpty())
            return;

        if (trans.getProtocol().getErrorCode().compareToIgnoreCase("3") == 0) {
            ui.showScreen(TIMESYNC);
            HashMap<String, Object> map = new HashMap<>();
            map.put(IUIDisplay.uiScreenBlankType, "DateTime");
            ui.showScreen(ACT_BLANK_SCREEN, map);
            ui.getResultCode(ACT_BLANK, IUIDisplay.MAX_TIMEOUT);
        }
    }
}
