package com.linkly.libengine.action.DCC;

import static com.linkly.libui.IUIDisplay.SCREEN_ICON.PROCESSING_ICON;
import static com.linkly.libui.UIScreenDef.ACT_INFORMATION_SCREEN;

import com.linkly.libengine.action.IAction;
import com.linkly.libengine.action.TransactionDecliner;
import com.linkly.libengine.engine.protocol.IProto;
import com.linkly.libui.IUIDisplay;

import java.util.HashMap;

public class DCCLookup extends IAction {
    @Override
    public String getName() {
        return "DCCLookup";
    }

    @Override
    public void run() {

        if (!d.getPayCfg().isDccSupported())
            return;

        HashMap<String, Object> map = new HashMap<>();
        map.put(IUIDisplay.uiScreenIcon, PROCESSING_ICON);
        ui.showScreen(ACT_INFORMATION_SCREEN, map);

        IProto iproto = d.getProtocol();
        trans.setDccEnquiry(true);

        if (iproto.authorize(trans)) {
            trans.setApproved(true);

        } else {
            trans.setApproved(false);
            d.getWorkflowEngine().setNextAction(TransactionDecliner.class);
        }

        trans.setDccEnquiry(false);

    }
}
