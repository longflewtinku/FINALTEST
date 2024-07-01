package com.linkly.libengine.workflow;

import com.linkly.libengine.action.InitialProcessing;
import com.linkly.libengine.action.JumpTo;
import com.linkly.libengine.action.Printing.PrintShiftTotals;
import com.linkly.libengine.action.TransactionApproval;
import com.linkly.libengine.action.TransactionCanceller;
import com.linkly.libengine.action.TransactionDecliner;
import com.linkly.libengine.action.TransactionFinalizer;
import com.linkly.libengine.action.check.CheckConfig;
import com.linkly.libengine.action.check.CheckPrinter;
import com.linkly.libengine.action.check.CheckReprintShiftTotals;
import com.linkly.libengine.action.user_action.MainMenu;
import com.linkly.libengine.action.user_action.UiProcessing;

public class ReprintShiftTotals extends Workflow {
    public ReprintShiftTotals() {
        this.addAction(new InitialProcessing());
        this.addAction(new CheckConfig());
        this.addAction(new CheckPrinter());
        this.addAction(new UiProcessing());
        this.addAction(new CheckReprintShiftTotals());
        this.addAction(new PrintShiftTotals());

        this.addAction(new TransactionApproval());
        this.addAction(new JumpTo(TransactionFinalizer.class));
        this.addAction(new TransactionCanceller());
        this.addAction(new JumpTo(TransactionFinalizer.class));
        this.addAction(new TransactionDecliner());
        this.addAction(new JumpTo(TransactionFinalizer.class));

        this.addAction(new TransactionFinalizer());
        this.addAction(new MainMenu());
    }
}
