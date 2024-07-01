package com.linkly.libengine.action.user_action;

import static com.linkly.libui.IUIDisplay.SCREEN_ICON.PROCESSING_ICON;
import static com.linkly.libui.UIScreenDef.ACT_INFORMATION_SCREEN;

import com.linkly.libengine.action.IAction;
import com.linkly.libengine.status.IStatus;
import com.linkly.libui.IUIDisplay;

import java.util.HashMap;

public class UiProcessing extends IAction {
    @Override
    public String getName() {
        return "UiProcessing";
    }

    @Override
    public void run() {
        this.uiShowProcessing();
    }

    private void uiShowProcessing() {
        HashMap<String, Object> map = new HashMap<>();
        d.getStatusReporter().reportStatusEvent( IStatus.STATUS_EVENT.STATUS_PROCESSING , trans.isSuppressPosDialog() );
        map.put(IUIDisplay.uiScreenIcon, PROCESSING_ICON);
        ui.showScreen(ACT_INFORMATION_SCREEN, map);
    }
}
