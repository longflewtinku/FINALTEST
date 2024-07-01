package com.linkly.libengine.workflow;

import com.linkly.libengine.action.IPC.EnablePowerKey;
import com.linkly.libengine.action.IPC.PopulateTransaction;
import com.linkly.libengine.action.IPC.SendReportResponse;
import com.linkly.libengine.action.IPC.TempLogin;
import com.linkly.libengine.action.IPC.TempLogout;
import com.linkly.libengine.action.InitialProcessing;
import com.linkly.libengine.action.Printing.PrintLastReconciliationAuto;
import com.linkly.libengine.action.TransactionFinalizer;
import com.linkly.libengine.action.check.CheckLastReconciliation;
import com.linkly.libengine.action.user_action.BackToIdlePosNotification;
import com.linkly.libengine.action.user_action.DisplayFinishTransaction;
import com.linkly.libengine.action.user_action.UiProcessing;

public class AutoLastReconciliation extends Workflow {
    public AutoLastReconciliation() {
        this.addAction(new InitialProcessing());
        this.addAction(new PopulateTransaction());
        this.addAction(new TempLogin());
        this.addAction(new UiProcessing());
        this.addAction(new CheckLastReconciliation());
        this.addAction(new PrintLastReconciliationAuto());
        this.addAction(new TransactionFinalizer());
        this.addAction(new SendReportResponse());
        this.addAction(new TempLogout());
        this.addAction(new EnablePowerKey());
        this.addAction(new DisplayFinishTransaction());
        this.addAction(new BackToIdlePosNotification());
    }
}
