package com.linkly.libengine.action;

public class TransactionApproval extends IAction {
    @Override
    public String getName() {
        return "TransactionApproval";
    }

    @Override
    public void run() {
        trans.setApproved(true); //Do we need to set this for Deferred Auths?
    }
}
