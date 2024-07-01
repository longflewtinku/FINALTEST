package com.linkly.libengine.workflow;

import com.linkly.libengine.action.DB.DBTransPurge;
import com.linkly.libengine.action.HostActions.Authorise;
import com.linkly.libengine.action.IPC.EnablePowerKey;
import com.linkly.libengine.action.IPC.PopulateTransaction;
import com.linkly.libengine.action.IPC.SendReportResponse;
import com.linkly.libengine.action.IPC.TempLogin;
import com.linkly.libengine.action.IPC.TempLogout;
import com.linkly.libengine.action.InitialProcessing;
import com.linkly.libengine.action.JumpTo;
import com.linkly.libengine.action.TransactionApproval;
import com.linkly.libengine.action.TransactionCanceller;
import com.linkly.libengine.action.TransactionDecliner;
import com.linkly.libengine.action.TransactionFinalizer;
import com.linkly.libengine.action.check.CheckConfig;
import com.linkly.libengine.action.check.CheckPrinter;
import com.linkly.libengine.action.check.CheckReconciliation;
import com.linkly.libengine.action.check.CheckResult;

public class AutoReconciliation extends Workflow {
    public AutoReconciliation() {

        this.addAction(new InitialProcessing());
        this.addAction(new CheckConfig());
        this.addAction(new CheckPrinter());
        this.addAction(new PopulateTransaction());
        this.addAction(new TempLogin());
        this.addAction(new CheckReconciliation());
        this.addAction(new Authorise());
        this.addAction(new DBTransPurge());
        this.addAction(new CheckResult());

        this.addAction(new TransactionApproval());
        this.addAction(new JumpTo(TransactionFinalizer.class));
        this.addAction(new TransactionCanceller());
        this.addAction(new JumpTo(TransactionFinalizer.class));
        this.addAction(new TransactionDecliner());
        this.addAction(new JumpTo(TransactionFinalizer.class));

        this.addAction(new TransactionFinalizer());
        this.addAction(new SendReportResponse());
        this.addAction(new TempLogout());
        this.addAction(new EnablePowerKey());
    }
}
