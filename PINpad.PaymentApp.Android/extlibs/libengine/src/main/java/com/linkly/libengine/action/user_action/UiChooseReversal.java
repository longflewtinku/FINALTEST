package com.linkly.libengine.action.user_action;

import static com.linkly.libui.IUIDisplay.ACTIVITY_ID.ACT_INPUT;
import static com.linkly.libui.IUIDisplay.ACTIVITY_ID.ACT_QUESTION;
import static com.linkly.libui.IUIDisplay.String_id.STR_REVERSE_LAST;
import static com.linkly.libui.IUIDisplay.String_id.STR_VOID;
import static com.linkly.libui.IUIDisplay.UIResultCode.OK;
import static com.linkly.libui.UIScreenDef.CANT_FIND_THE_LATEST;
import static com.linkly.libui.UIScreenDef.CANT_REVERSE_A_REFUND;
import static com.linkly.libui.UIScreenDef.ENTER_RECEIPT_NUM;
import static com.linkly.libui.UIScreenDef.PLEASE_CHOOSE_REVERSAL;
import static com.linkly.libui.display.DisplayQuestion.EButtonStyle.BTN_STYLE_PRIMARY_DEFAULT;

import com.linkly.libengine.action.IAction;
import com.linkly.libengine.action.TransactionCanceller;
import com.linkly.libengine.dependencies.IDependency;
import com.linkly.libengine.engine.EngineManager;
import com.linkly.libengine.engine.transactions.TransRec;
import com.linkly.libengine.engine.transactions.TransRecManager;
import com.linkly.libui.IUIDisplay;
import com.linkly.libui.display.DisplayQuestion;

import java.util.ArrayList;
import java.util.HashMap;

import timber.log.Timber;

public class UiChooseReversal extends IAction {

    private static final String LAST = "Last";
    private static final String VOID = "Void";

    @Override
    public String getName() {
        return "UiChooseReversal";
    }

    @Override
    public void run() {

        this.uiChooseReversal(d);
    }

    private void uiChooseReversal(IDependency dependencies) {


        HashMap<String, Object> map = new HashMap<>();
        ArrayList<DisplayQuestion> options = new ArrayList<>();
        options.add(new DisplayQuestion(STR_REVERSE_LAST, LAST, BTN_STYLE_PRIMARY_DEFAULT));

        if (!d.getCustomer().supportManualVoids()) {
            Timber.i("VOID not available on mobiles as breaks Maccing");
        } else {
            options.add(new DisplayQuestion(STR_VOID, VOID, BTN_STYLE_PRIMARY_DEFAULT));
        }

        map.put(IUIDisplay.uiScreenOptionList, options);
        map.put(IUIDisplay.uiEnableBackButton, true);
        ui.showScreen(PLEASE_CHOOSE_REVERSAL, map);
        IUIDisplay.UIResultCode res = ui.getResultCode(ACT_QUESTION, IUIDisplay.LONG_TIMEOUT);
        if (res == OK) {
            String result = ui.getResultText(ACT_QUESTION, IUIDisplay.uiResultText1);

            switch (result) {
                case VOID:
                    if (!uiInputReversalReceiptNumber(dependencies)) {
                        d.getWorkflowEngine().setNextAction(TransactionCanceller.class);
                    }
                    break;
                case LAST:
                default:
                    // find the last transaction that can be reversed
                    TransRec txnToReverse = TransRecManager.getInstance().getTransRecDao().getLatest();

                    if (txnToReverse != null) {
                        if (txnToReverse.isRefund() && !d.getCustomer().supportReversalsForTransType(EngineManager.TransType.REFUND)) {
                            ui.showScreen(CANT_REVERSE_A_REFUND);
                            d.getWorkflowEngine().setNextAction(TransactionCanceller.class);
                        } else {
                            // continue to next state
                            trans.copyFromOriginalTxnForReversal(txnToReverse);
                        }
                    } else {
                        ui.showScreen(CANT_FIND_THE_LATEST);
                        d.getWorkflowEngine().setNextAction(TransactionCanceller.class);
                    }
            }

        } else {
            d.getWorkflowEngine().setNextAction(TransactionCanceller.class);
        }
    }

    private boolean uiInputReversalReceiptNumber(IDependency dependencies) {
        HashMap<String, Object> map = new HashMap<>();
        map.put(IUIDisplay.uiTitleId, trans.getTransType().displayId);

        ui.showInputScreen(ENTER_RECEIPT_NUM, map);
        IUIDisplay.UIResultCode res = ui.getResultCode(ACT_INPUT, IUIDisplay.LONG_TIMEOUT);
        if (res == OK) {
            String result = ui.getResultText(ACT_INPUT, IUIDisplay.uiResultText1);
            trans.getAudit().setReversalReceiptNumber(Integer.valueOf(result));
            return true;
        }

        d.getWorkflowEngine().setNextAction(TransactionCanceller.class);
        return false;
    }

}
