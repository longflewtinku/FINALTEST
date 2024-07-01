package com.linkly.libengine.workflow;

import com.linkly.libengine.action.TransactionFinalizer;
import com.linkly.libengine.action.user_action.BackToIdlePosNotification;
import com.linkly.libengine.action.user_action.DisplayFinishTransaction;
import com.linkly.libengine.action.user_action.MainMenu;
import com.linkly.libengine.action.user_action.UiCancelled;

public class CancelAmountIdle extends Workflow {
    public CancelAmountIdle() {
        //initial
        this.addAction(new UiCancelled());
        this.addAction(new TransactionFinalizer());
        this.addAction(new DisplayFinishTransaction());
        this.addAction(new MainMenu());
        this.addAction(new BackToIdlePosNotification());
    }
}
