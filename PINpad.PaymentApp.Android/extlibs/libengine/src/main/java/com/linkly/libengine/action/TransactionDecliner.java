package com.linkly.libengine.action;

public class TransactionDecliner extends IAction {
    @Override
    public String getName() {
        return "TransactionDecliner";
    }

    @Override
    public void run() {
        trans.setApproved(false);
        trans.setDeclined(true);
        trans.setCancelled(false);
    }
}
