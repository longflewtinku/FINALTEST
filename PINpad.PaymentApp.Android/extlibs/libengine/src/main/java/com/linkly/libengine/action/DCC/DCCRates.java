package com.linkly.libengine.action.DCC;

import static com.linkly.libui.IUIDisplay.SCREEN_ICON.PROCESSING_ICON;
import static com.linkly.libui.UIScreenDef.ACT_INFORMATION_SCREEN;
import static com.linkly.libui.UIScreenDef.TRANS_DECLINED;

import com.linkly.libengine.action.IAction;
import com.linkly.libengine.engine.protocol.IProto;
import com.linkly.libui.IUIDisplay;

import java.util.HashMap;

public class DCCRates extends IAction {
    @Override
    public String getName() {
        return "DCCRates";
    }

    @Override
    public void run() {

        HashMap<String, Object> map = new HashMap<>();
        map.put(IUIDisplay.uiScreenIcon, PROCESSING_ICON);
        ui.showScreen(ACT_INFORMATION_SCREEN, map);

        IProto iproto = d.getProtocol();
        if (iproto.authorize(trans)) {
            trans.setApproved(true);
            trans.print(d,false, false, mal);
        } else {
            ui.showScreen(TRANS_DECLINED, map);
        }


    }
}
