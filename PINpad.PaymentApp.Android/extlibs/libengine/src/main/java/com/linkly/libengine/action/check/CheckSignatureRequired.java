package com.linkly.libengine.action.check;

import com.linkly.libengine.action.IAction;


public class CheckSignatureRequired extends IAction {

    @Override
    public String getName() {
        return "CheckSignatureRequired";
    }

    @Override
    public void run() {
        if (trans.isSignatureRequired()) {
           d.getWorkflowEngine().setNextAction(CheckScreenSignature.class);
        }
    }
}


