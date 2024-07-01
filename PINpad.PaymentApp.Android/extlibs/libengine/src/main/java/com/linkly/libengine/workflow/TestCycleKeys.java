package com.linkly.libengine.workflow;

import com.linkly.libengine.action.HostActions.Authorise;
import com.linkly.libengine.action.InitialProcessing;
import com.linkly.libengine.action.JumpTo;
import com.linkly.libengine.action.MenuOperations.dev.KeyReset;
import com.linkly.libengine.action.TransactionApproval;
import com.linkly.libengine.action.TransactionCanceller;
import com.linkly.libengine.action.TransactionDecliner;
import com.linkly.libengine.action.TransactionFinalizer;
import com.linkly.libengine.action.check.CheckCycleKeysResult;
import com.linkly.libengine.action.check.CheckResult;
import com.linkly.libengine.action.user_action.MainMenu;
import com.linkly.libengine.action.user_action.UiProcessing;

public class TestCycleKeys extends Workflow {
    public TestCycleKeys() {
        //initial
        this.addAction(new InitialProcessing());
        this.addAction(new UiProcessing());
        this.addAction(new KeyReset());
        this.addAction(new Authorise());
        this.addAction(new CheckResult());

        this.addAction(new TransactionApproval());
        this.addAction(new JumpTo(CheckCycleKeysResult.class));

        this.addAction(new TransactionDecliner());
        this.addAction(new JumpTo(CheckCycleKeysResult.class));

        this.addAction(new TransactionCanceller());
        this.addAction(new JumpTo(CheckCycleKeysResult.class));

        this.addAction(new CheckCycleKeysResult());
        this.addAction(new TransactionFinalizer());
        this.addAction(new MainMenu());
    }
}
