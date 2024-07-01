package com.linkly.libengine.action.MenuOperations.admin;

import static com.linkly.libui.IUIDisplay.ACTIVITY_ID.ACT_QUESTION;
import static com.linkly.libui.IUIDisplay.String_id.STR_CANCEL;
import static com.linkly.libui.IUIDisplay.String_id.STR_REFUND;
import static com.linkly.libui.IUIDisplay.String_id.STR_SALE;
import static com.linkly.libui.IUIDisplay.uiDisableScreensaver;
import static com.linkly.libui.UIScreenDef.PLEASE_CHOOSE_CARD_NOT_PRESENT;
import static com.linkly.libui.display.DisplayQuestion.EButtonStyle.BTN_STYLE_DEFAULT;
import static com.linkly.libui.display.DisplayQuestion.EButtonStyle.BTN_STYLE_TRANSPARENT;

import com.linkly.libengine.action.IAction;
import com.linkly.libengine.engine.EngineManager;
import com.linkly.libengine.engine.transactions.TransRec;
import com.linkly.libengine.workflow.Workflow;
import com.linkly.libengine.workflow.WorkflowScheduler;
import com.linkly.libui.IUIDisplay;
import com.linkly.libui.display.DisplayQuestion;

import java.util.ArrayList;
import java.util.HashMap;

public class CNPMenu extends IAction {

    @Override
    public String getName() {
        return "CNPMenu";
    }

    @Override
    public void run() {
        HashMap<String, Object> map = new HashMap<>();
        ArrayList<DisplayQuestion> options = new ArrayList<>();

        mal.getHardware().enablePowerKey(false);
        options.add(new DisplayQuestion(STR_SALE, "Sale", BTN_STYLE_DEFAULT));
        options.add(new DisplayQuestion(STR_REFUND, "Refund", BTN_STYLE_DEFAULT));
        options.add(new DisplayQuestion(STR_CANCEL, "Cancel", BTN_STYLE_TRANSPARENT));

        map.put(uiDisableScreensaver, true);
        map.put(IUIDisplay.uiScreenOptionList, options);
        ui.showScreen(PLEASE_CHOOSE_CARD_NOT_PRESENT, map);
        if (ui.getResultCode(ACT_QUESTION, IUIDisplay.LONG_TIMEOUT) == IUIDisplay.UIResultCode.OK) {
            String result = ui.getResultText(ACT_QUESTION, IUIDisplay.uiResultText1);

            ui.displayPleaseWaitScreen();
            if (!this.RunCardNotPresentTrans(result, true)) {
                ui.displayMainMenuScreen();
            }
        } else {
            ui.displayMainMenuScreen();
        }
    }

    private boolean RunCardNotPresentTrans(String transType, boolean wait) {

        EngineManager.TransType trType = EngineManager.TransType.getTransTypeByString(transType);
        if (trType == null)
            return false;

        EngineManager.TransType cnpTransType = trType.getCNPEquivalentType();
        if (cnpTransType == null)
            return false;

        TransRec trans = new TransRec(cnpTransType, d);

        d.resetCurrentTransaction(trans);
        Workflow w = d.getAppCallbacks().getWorkflowFactory().getWorkflow(cnpTransType);
        WorkflowScheduler.getInstance().queueWorkflow(w, false, false);

        return true;
    }

}
