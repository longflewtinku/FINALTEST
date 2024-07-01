package com.linkly.payment.workflows.generic;

import com.linkly.libengine.action.HostActions.Authorise;
import com.linkly.libengine.action.InitialProcessing;
import com.linkly.libengine.action.JumpTo;
import com.linkly.libengine.action.TransactionApproval;
import com.linkly.libengine.action.TransactionCanceller;
import com.linkly.libengine.action.TransactionDecliner;
import com.linkly.libengine.action.TransactionFinalizer;
import com.linkly.libengine.action.check.CheckConfig;
import com.linkly.libengine.action.user_action.MainMenu;
import com.linkly.libengine.action.user_action.UiProcessing;
import com.linkly.libengine.workflow.Workflow;

public class PreReconciliation extends Workflow {

    public PreReconciliation() {
        this.addAction(new InitialProcessing());
        this.addAction(new CheckConfig());
        this.addAction(new UiProcessing());

        this.addAction(new ActCheckReconciliation());
        this.addAction(new Authorise());
        this.addAction(new ActCheckReconciliationResult()); // jumps to approved, out of balance or decliner, or Authorise to retry

        this.addAction(new TransactionCanceller());
        this.addAction(new JumpTo(TransactionFinalizer.class));

        this.addAction(new TransactionApproval());
        this.addAction(new ActPrintSummary());
        this.addAction(new JumpTo(TransactionFinalizer.class));

        this.addAction(new ActRecOutOfBalance());
        this.addAction(new JumpTo(TransactionFinalizer.class));

        this.addAction(new TransactionDecliner());
        this.addAction(new JumpTo(TransactionFinalizer.class));

        this.addAction(new TransactionFinalizer());
        this.addAction(new MainMenu());
    }
}
