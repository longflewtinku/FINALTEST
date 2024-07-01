package com.linkly.payment.workflows.generic;

import com.linkly.libengine.action.IPC.EnablePowerKey;
import com.linkly.libengine.action.IPC.PopulateTransaction;
import com.linkly.libengine.action.IPC.SendReportResponse;
import com.linkly.libengine.action.IPC.TempLogin;
import com.linkly.libengine.action.IPC.TempLogout;
import com.linkly.libengine.action.InitialProcessing;
import com.linkly.libengine.action.TransactionApproval;
import com.linkly.libengine.action.TransactionFinalizer;
import com.linkly.libengine.action.user_action.UiProcessing;
import com.linkly.libengine.workflow.Workflow;

// Also known as an XReport
public class AutoSummary extends Workflow {

    public AutoSummary() {
        this.addAction(new InitialProcessing());
        this.addAction(new PopulateTransaction());
        this.addAction(new TempLogin());
        this.addAction(new UiProcessing());
        this.addAction(new ActCheckReconciliation());
        this.addAction(new ActPrintSummary());
        this.addAction(new TransactionApproval()); // Set the result to approved. Otherwise gets treated as declined
        this.addAction(new TransactionFinalizer());
        this.addAction(new SendReportResponse());
        this.addAction(new TempLogout());
        this.addAction(new EnablePowerKey());
    }
}
