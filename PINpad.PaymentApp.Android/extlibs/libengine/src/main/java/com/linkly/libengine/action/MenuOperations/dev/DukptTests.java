package com.linkly.libengine.action.MenuOperations.dev;

import static com.linkly.libui.IUIDisplay.ACTIVITY_ID.ACT_INPUT;
import static com.linkly.libui.IUIDisplay.SCREEN_ICON.PROCESSING_ICON;
import static com.linkly.libui.IUIDisplay.UIResultCode.OK;
import static com.linkly.libui.UIScreenDef.ACT_INFORMATION_SCREEN;
import static com.linkly.libui.UIScreenDef.ENTER_NUMBER_KEY_ITERATIONS;

import com.linkly.libengine.action.IAction;
import com.linkly.libengine.engine.protocol.iso8583.As2805WoolworthsUtils;
import com.linkly.libui.IUIDisplay;

import java.util.HashMap;

public class DukptTests extends IAction {

    @Override
    public String getName() {
        return "Dukpt Test";
    }

    @Override
    public void run() {
        int numTrans = uiInputNumTests();
        if( numTrans <= 0 ) {
            ui.displayMainMenuScreen();
            return;
        }

        HashMap<String, Object> map = new HashMap<>();
        map.put(IUIDisplay.uiScreenIcon, PROCESSING_ICON);
        ui.showScreen(ACT_INFORMATION_SCREEN, map);

        for (int i = 0; i < numTrans; i++) {
            As2805WoolworthsUtils.incrementDukptKsn();
        }

        ui.displayMainMenuScreen();
    }


    private int uiInputNumTests() {
        HashMap<String, Object> map = new HashMap<>();
        ui.showInputScreen(ENTER_NUMBER_KEY_ITERATIONS, map);
        IUIDisplay.UIResultCode res = ui.getResultCode(ACT_INPUT, IUIDisplay.LONG_TIMEOUT);
        if (res == OK) {
            return Integer.parseInt(ui.getResultText(ACT_INPUT, IUIDisplay.uiResultText1));
        } else {
            return 0;
        }
    }
}
