package com.linkly.libengine;

import static com.linkly.libsecapp.IP2PUI.P2PUIReturnCodes.OK;
import static com.linkly.libsecapp.IP2PUI.P2PUIScreens.P2P_INFO_SCREEN;
import static com.linkly.libsecapp.IP2PUI.P2PUIScreens.P2P_MENU_SCREEN;
import static com.linkly.libsecapp.IP2PUI.P2PUIScreens.P2P_PIN_SCREEN;
import static com.linkly.libui.IUIDisplay.ACTIVITY_ID.ACT_INFORMATION;
import static com.linkly.libui.IUIDisplay.ACTIVITY_ID.ACT_INPUT_PIN;
import static com.linkly.libui.IUIDisplay.ACTIVITY_ID.ACT_QUESTION;
import static com.linkly.libui.UIScreenDef.ACT_INFORMATION_SCREEN;
import static com.linkly.libui.UIScreenDef.ACT_INPUT_SCREEN;
import static com.linkly.libui.UIScreenDef.ACT_QUESTION_SCREEN;

import android.content.Intent;

import com.linkly.libmal.MalFactory;
import com.linkly.libsecapp.IP2PUI;
import com.linkly.libui.IUIDisplay;
import com.linkly.libui.UI;

import java.util.HashMap;

public class P2PCallbackUI implements IP2PUI.UIListener {

    public IP2PUI.P2PUIReturnCodes displayScreen(IP2PUI.P2PUIScreens iScreenID, HashMap<String, Object> mExtras, boolean block) {

        // TODO: Modify this switch case
        switch (iScreenID) {
            case P2P_MENU_SCREEN:
                UI.getInstance().getUI().showScreen(ACT_QUESTION_SCREEN, mExtras);
                break;
            case P2P_PIN_SCREEN:
                UI.getInstance().getUI().showScreen(ACT_INPUT_SCREEN, mExtras);
                break;
            case P2P_INFO_SCREEN:
            default:
                UI.getInstance().getUI().showScreen(ACT_INFORMATION_SCREEN, mExtras);
                break;
        }

        if (block)
            UI.getInstance().getUI().getResultCode(mapScreens(iScreenID), (int)mExtras.get(IUIDisplay.uiScreenTimeout));

        return OK;
    }

    public IP2PUI.P2PUIReturnCodes getResultCode(IP2PUI.P2PUIScreens iScreenID, int iTimeoutMS) {
        IUIDisplay.UIResultCode ret = UI.getInstance().getUI().getResultCode(mapScreens(iScreenID), iTimeoutMS);
        return mapReturnCodes(ret);
    }

    public boolean getSelectChoice(IP2PUI.P2PUIScreens iScreenId, String textName){
        return UI.getInstance().getUI().getSelectChoice(mapScreens(iScreenId), textName);
    }

    public String getResultText(IP2PUI.P2PUIScreens iScreenId, String textName){
        return UI.getInstance().getUI().getResultText(mapScreens(iScreenId), textName);
    }

    public boolean getResultBoolean(IP2PUI.P2PUIScreens iScreenId, String boolName){
        return UI.getInstance().getUI().getResultBoolean(mapScreens(iScreenId), boolName);
    }

    public byte[] getResultPinBlock(IP2PUI.P2PUIScreens iScreenId){
       return UI.getInstance().getUI().getResultPinBlock(mapScreens(iScreenId));
    }

    public void displayLed(String ledColour, String ledNum) {

        if (ledColour.length() > 0 && ledNum.length() > 0) {
            Intent intent = new Intent();
            intent.setAction("com.linkly.LED_EVENT");
            intent.putExtra(IUIDisplay.uiLedColour, ledColour);
            intent.putExtra(IUIDisplay.uiLedNumber, ledNum);
            MalFactory.getInstance().getMalContext().sendBroadcast(intent);
        }
    }

    public void displayLed2(boolean one, boolean two, boolean three, boolean four) {
        UI.getInstance().getUI().displayLed2(one, two, three, four);
    }


    private IUIDisplay.ACTIVITY_ID mapScreens(IP2PUI.P2PUIScreens iScreenID) {
        if (iScreenID == P2P_INFO_SCREEN)
            return ACT_INFORMATION;
        else if (iScreenID == P2P_MENU_SCREEN)
            return ACT_QUESTION;
        else if (iScreenID == P2P_PIN_SCREEN)
            return ACT_INPUT_PIN;
        return ACT_INFORMATION;
    }

    private IP2PUI.P2PUIReturnCodes mapReturnCodes(IUIDisplay.UIResultCode resultCode) {
        return IP2PUI.P2PUIReturnCodes.values()[resultCode.ordinal()];
    }

}
