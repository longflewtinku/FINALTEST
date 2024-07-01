package com.linkly.libengine.workflow;

import com.linkly.libconfig.ProfileCfg;
import com.linkly.libengine.action.MenuOperations.admin.PowerFailCheck;
import com.linkly.libengine.action.MenuOperations.admin.ShiftTotalsTerminalStartUp;
import com.linkly.libengine.action.MenuOperations.admin.SubmitTransactionsSchedule;

public class Startup extends Workflow {
    public Startup() {

        if (!ProfileCfg.getInstance().isDemo()) {
            addAction(new PowerFailCheck());
            this.addAction(new SubmitTransactionsSchedule(true));
        }
        addAction(new ShiftTotalsTerminalStartUp());
    }


}
