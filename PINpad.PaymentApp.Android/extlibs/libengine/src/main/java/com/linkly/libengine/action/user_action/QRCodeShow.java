package com.linkly.libengine.action.user_action;

import static com.linkly.libui.IUIDisplay.ACTIVITY_ID.ACT_QR_SCAN;
import static com.linkly.libui.IUIDisplay.SCREEN_ICON.PROCESSING_ICON;
import static com.linkly.libui.IUIDisplay.UIResultCode.OK;
import static com.linkly.libui.UIScreenDef.ACT_INFORMATION_SCREEN;
import static com.linkly.libui.UIScreenDef.ACT_QR_SCAN_SCREEN;

import com.linkly.libengine.action.IAction;
import com.linkly.libui.IUIDisplay;
import com.linkly.libui.IUIDisplay.String_id;

import java.util.HashMap;

public class QRCodeShow extends IAction {

    @Override
    public String getName() {
        return "QRCodeShow";
    }

    @Override
    public void run() {
        if (trans.getCard().isScanVoucher())
        {
            HashMap<String, Object> map = new HashMap<>();

            ui.showScreen(ACT_QR_SCAN_SCREEN, map);

            IUIDisplay.UIResultCode res = ui.getResultCode(ACT_QR_SCAN, IUIDisplay.LONG_TIMEOUT);
            if (res == OK) {
                String result = ui.getResultText(ACT_QR_SCAN, IUIDisplay.uiResultText1);
                trans.getAmounts().setVoucherCode(result);

                map = new HashMap<>();
                map.put(IUIDisplay.uiPromptId, String_id.STR_VALIDATING_CODE);
                map.put(IUIDisplay.uiScreenIcon, PROCESSING_ICON);
                ui.showScreen(ACT_INFORMATION_SCREEN, map);
            }
        }
    }
}
