package com.linkly.libengine.workflow;

import com.linkly.libengine.action.IAction;

public class WorkflowAddActions extends Workflow {

    // Array constructor
    public WorkflowAddActions(IAction[] actions) {
        for (IAction action : actions)
            this.addAction(action);
    }

    // Single-item constructor
    public WorkflowAddActions(IAction action) {
        this.addAction(action);
    }

    // Empty constructor
    public WorkflowAddActions() {
        this.addAction(null);
    }

}
