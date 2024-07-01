package com.linkly.libengine.workflow;

import com.linkly.libengine.action.HostActions.Authorise;
import com.linkly.libengine.action.IPC.EnablePowerKey;
import com.linkly.libengine.action.IPC.LogonResponse;
import com.linkly.libengine.action.IPC.PopulateTransaction;
import com.linkly.libengine.action.IPC.TempLogin;
import com.linkly.libengine.action.IPC.TempLogout;
import com.linkly.libengine.action.InitialProcessing;
import com.linkly.libengine.action.JumpTo;
import com.linkly.libengine.action.TransactionApproval;
import com.linkly.libengine.action.TransactionCanceller;
import com.linkly.libengine.action.TransactionDecliner;
import com.linkly.libengine.action.TransactionFinalizer;
import com.linkly.libengine.action.check.CheckConfig;
import com.linkly.libengine.action.check.CheckResult;
import com.linkly.libengine.action.check.CheckTestConnectionResult;
import com.linkly.libengine.action.user_action.BackToIdlePosNotification;
import com.linkly.libengine.action.user_action.DisplayFinishTransaction;
import com.linkly.libengine.action.user_action.UiProcessing;

public class AutoLogon extends Workflow {
    public AutoLogon() {
        //initial
        this.addAction( new InitialProcessing() );
        this.addAction(new CheckConfig());
        this.addAction( new TempLogin() );
        this.addAction( new PopulateTransaction() );
        this.addAction( new UiProcessing() );
        this.addAction( new Authorise() );
        this.addAction( new CheckResult() );

        this.addAction( new TransactionApproval() );
        this.addAction( new JumpTo( CheckTestConnectionResult.class ) );

        this.addAction( new TransactionDecliner() );
        this.addAction( new JumpTo( CheckTestConnectionResult.class ) );

        this.addAction( new TransactionCanceller() );
        this.addAction( new JumpTo( CheckTestConnectionResult.class ) );

        this.addAction( new CheckTestConnectionResult() );
        this.addAction( new TransactionFinalizer() );
        this.addAction( new LogonResponse() );
        this.addAction( new TempLogout() );
        this.addAction( new EnablePowerKey() );
        this.addAction( new DisplayFinishTransaction() );
        this.addAction(new BackToIdlePosNotification());
    }
}
