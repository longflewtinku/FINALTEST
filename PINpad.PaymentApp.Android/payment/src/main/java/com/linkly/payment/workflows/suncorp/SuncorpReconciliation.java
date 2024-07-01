package com.linkly.payment.workflows.suncorp;

import com.linkly.libengine.action.DB.DBSave;
import com.linkly.libengine.action.DB.DBTransPurge;
import com.linkly.libengine.action.HostActions.Authorise;
import com.linkly.libengine.action.InitialProcessing;
import com.linkly.libengine.action.JumpTo;
import com.linkly.libengine.action.TransactionApproval;
import com.linkly.libengine.action.TransactionDecliner;
import com.linkly.libengine.action.TransactionFinalizer;
import com.linkly.libengine.action.check.CheckPrinter;
import com.linkly.libengine.action.user_action.MainMenu;
import com.linkly.libengine.action.user_action.UiProcessing;
import com.linkly.libengine.workflow.Workflow;

public class SuncorpReconciliation extends Workflow {
    public SuncorpReconciliation() {
        this.addAction(new InitialProcessing());
        this.addAction(new CheckPrinter());
        this.addAction(new UiProcessing());
        this.addAction(new ActSuncorpCheckReconciliation());
        this.addAction(new Authorise());
        this.addAction(new ActSuncorpCheckReconciliationResult()); // jumps to approved, out of balance or decliner

        this.addAction(new TransactionApproval());
        this.addAction(new JumpTo(TransactionFinalizer.class));

        this.addAction(new ActSuncorpRecOutOfBalance());
        this.addAction(new JumpTo(TransactionFinalizer.class));

        this.addAction(new TransactionDecliner());
        this.addAction(new JumpTo(TransactionFinalizer.class));

        this.addAction(new TransactionFinalizer());
        this.addAction(new DBSave());

        this.addAction(new DBTransPurge());
        this.addAction(new MainMenu());
    }
}
