package com.linkly.libengine.action;

import static com.linkly.libengine.status.IStatus.STATUS_EVENT.STATUS_TRANS_REFERRED;

public class TransactionReferral extends IAction {
    @Override
    public String getName() {
        return "TransactionReferral";
    }

    @Override
    public void run() {
        trans.setReferred(true);
        trans.getProtocol().setAdditionalResponseText("");
        d.getStatusReporter().reportStatusEvent(STATUS_TRANS_REFERRED , trans.isSuppressPosDialog());
    }
}
