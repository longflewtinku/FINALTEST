package com.linkly.libengine.workflow;

import com.linkly.libengine.action.DB.DBSave;
import com.linkly.libengine.action.DB.DBTransPurge;
import com.linkly.libengine.action.HostActions.Authorise;
import com.linkly.libengine.action.HostActions.PostAuthorisation;
import com.linkly.libengine.action.IPC.EnablePowerKey;
import com.linkly.libengine.action.IPC.LookupPreauthByRfn;
import com.linkly.libengine.action.IPC.PopulateOriginalTransDetails;
import com.linkly.libengine.action.IPC.PopulateTransaction;
import com.linkly.libengine.action.IPC.TempLogin;
import com.linkly.libengine.action.IPC.TempLogout;
import com.linkly.libengine.action.IPC.TransResponse;
import com.linkly.libengine.action.InitialProcessing;
import com.linkly.libengine.action.JumpTo;
import com.linkly.libengine.action.MenuOperations.admin.SubmitTransactionsSchedule;
import com.linkly.libengine.action.Printing.PrintFirst;
import com.linkly.libengine.action.Printing.PrintSecond;
import com.linkly.libengine.action.TransactionApproval;
import com.linkly.libengine.action.TransactionCanceller;
import com.linkly.libengine.action.TransactionDecliner;
import com.linkly.libengine.action.TransactionFinalizer;
import com.linkly.libengine.action.check.CheckPreauthReversal;
import com.linkly.libengine.action.check.CheckResult;
import com.linkly.libengine.action.check.CheckReversalResult;
import com.linkly.libengine.action.check.CheckUserLevel;
import com.linkly.libengine.action.user_action.BackToIdlePosNotification;
import com.linkly.libengine.action.user_action.DisplayFinishTransaction;
import com.linkly.libengine.action.user_action.MainMenu;

public class AutoPreauthCancellation extends Workflow {
    public AutoPreauthCancellation() {

        //initial
        this.addAction(new InitialProcessing());
        // locate the original preauth
        this.addAction(new PopulateTransaction());
        this.addAction(new LookupPreauthByRfn()); // jumps to canceller if not found
        this.addAction(new PopulateOriginalTransDetails());
        this.addAction(new TempLogin());


        this.addAction(new CheckPreauthReversal());
        this.addAction(new CheckUserLevel());
        this.addAction(new Authorise());

        this.addAction(new CheckResult());
        this.addAction(new TransactionApproval());
        this.addAction(new JumpTo(CheckReversalResult.class));

        this.addAction(new TransactionDecliner());
        this.addAction(new JumpTo(CheckReversalResult.class));

        this.addAction(new TransactionCanceller());
        this.addAction(new JumpTo(CheckReversalResult.class));

        this.addAction(new CheckReversalResult());
        this.addAction(new PrintFirst());
        this.addAction(new PrintSecond());

        this.addAction(new TransactionFinalizer());
        this.addAction(new PostAuthorisation());
        this.addAction(new DBSave());
        this.addAction(new DisplayFinishTransaction());
        this.addAction(new TransResponse());
        this.addAction(new TempLogout());
        this.addAction(new EnablePowerKey());

        this.addAction(new MainMenu());
        this.addAction(new SubmitTransactionsSchedule(true));
        this.addAction(new DBTransPurge());
        this.addAction(new BackToIdlePosNotification());
    }
}
