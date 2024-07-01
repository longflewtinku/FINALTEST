package com.linkly.libengine.action.MenuOperations.dev;

import static com.linkly.libengine.engine.transactions.properties.TProtocol.MessageStatus.REVERSAL_QUEUED;
import static com.linkly.libui.UIScreenDef.NO_TRANS_TO_REVERSED;
import static com.linkly.libui.UIScreenDef.REVERSALS_CLEARED;

import com.linkly.libengine.action.IAction;
import com.linkly.libengine.engine.transactions.TransRec;
import com.linkly.libengine.engine.transactions.TransRecManager;

import java.util.List;

public class ClearReversals extends IAction {

    @Override
    public String getName() {
        return "Clear Reversals";
    }

    @Override
    public void run() {

        // retrieve reversals
        List<TransRec> allTrans = TransRecManager.getInstance().getTransRecDao().findAllByMessageStatus( REVERSAL_QUEUED );
        if(allTrans == null){
            return;
        }

        if (allTrans.size() == 0) {
            ui.showScreen(NO_TRANS_TO_REVERSED);
            return;
        } else {
            for (TransRec trans : allTrans) {
                TransRecManager.getInstance().getTransRecDao().delete(trans);
            }
            ui.showScreen(REVERSALS_CLEARED);
        }
        ui.displayMainMenuScreen();
    }
}
