package com.linkly.libengine.workflow;

import com.linkly.libengine.action.DB.DBSave;
import com.linkly.libengine.action.DB.DBTransPurge;
import com.linkly.libengine.action.HostActions.Authorise;
import com.linkly.libengine.action.IPC.PopulateOriginalTransDetails;
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
import com.linkly.libengine.action.user_action.InputPreAuthAuthCode;
import com.linkly.libengine.action.user_action.InputPreAuthRRN;
import com.linkly.libengine.action.user_action.MainMenu;

public class PreauthCancellation extends Workflow {
    public PreauthCancellation() {

        //initial
        this.addAction(new InitialProcessing());

        this.addAction(new InputPreAuthRRN());
        // get last auth code from preauth
        this.addAction(new InputPreAuthAuthCode());

        this.addAction(new PopulateOriginalTransDetails());
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
        this.addAction(new DBSave());
        this.addAction(new MainMenu());
        this.addAction(new SubmitTransactionsSchedule(true));
        this.addAction(new DBTransPurge());
    }
}
