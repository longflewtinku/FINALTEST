package com.linkly.payment.workflows.generic;

import com.linkly.libengine.action.DB.DBSave;
import com.linkly.libengine.action.DB.DBTransPurge;
import com.linkly.libengine.action.HostActions.Authorise;
import com.linkly.libengine.action.InitialProcessing;
import com.linkly.libengine.action.JumpTo;
import com.linkly.libengine.action.MenuOperations.admin.SubmitTransactions;
import com.linkly.libengine.action.TransactionApproval;
import com.linkly.libengine.action.TransactionCanceller;
import com.linkly.libengine.action.TransactionDecliner;
import com.linkly.libengine.action.TransactionFinalizer;
import com.linkly.libengine.action.check.CheckConfig;
import com.linkly.libengine.action.user_action.BackToIdlePosNotification;
import com.linkly.libengine.action.user_action.DisplayFinishTransaction;
import com.linkly.libengine.action.user_action.UiProcessing;
import com.linkly.libengine.workflow.Workflow;

public class Reconciliation extends Workflow {
    public Reconciliation() {
        this.addAction(new InitialProcessing());
        this.addAction(new CheckConfig());
        this.addAction(new UiProcessing());
        this.addAction(new ActCheckAutoSettlement()); // Set autoSettlement flags and switch to "Reconciliation" transaction type
        this.addAction(new ActCheckReconciliation());
        this.addAction(new SubmitTransactions(false)); // upload any SAF transactions to prior to host settlement
        this.addAction(new Authorise());
        this.addAction(new ActCheckReconciliationResult()); // jumps to approved, out of balance or decliner, or Authorise to retry

        this.addAction(new TransactionCanceller());
        this.addAction(new JumpTo(TransactionFinalizer.class));

        this.addAction(new TransactionApproval());
        this.addAction(new JumpTo(TransactionFinalizer.class));

        this.addAction(new ActRecOutOfBalance());
        this.addAction(new JumpTo(TransactionFinalizer.class));

        this.addAction(new TransactionDecliner());
        this.addAction(new JumpTo(TransactionFinalizer.class));

        this.addAction(new TransactionFinalizer());
        this.addAction(new DBSave());

        this.addAction(new DBTransPurge());

        this.addAction(new DisplayFinishTransaction());
        this.addAction(new BackToIdlePosNotification());
    }
}
