package com.linkly.libengine.workflow;

import com.linkly.libengine.action.DCC.DCCRates;
import com.linkly.libengine.action.user_action.MainMenu;

public class DccRates extends Workflow {
    public DccRates() {
        //initial
        this.addAction(new DCCRates());
        this.addAction(new MainMenu());
    }
}
