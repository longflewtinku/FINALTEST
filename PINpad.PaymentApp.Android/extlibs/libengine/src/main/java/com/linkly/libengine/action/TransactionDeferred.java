package com.linkly.libengine.action;

import static com.linkly.libengine.engine.transactions.properties.TProtocol.MessageStatus.DEFERRED_AUTH;

import com.linkly.libengine.status.IStatus;

public class TransactionDeferred extends IAction {
    @Override
    public String getName() {
        return "TransactionDeferred";
    }

    @Override
    public void run() {
        trans.setDeferredAuth(true);
        trans.getCard().setGenAc2Required(false);
        d.getStatusReporter().reportStatusEvent(IStatus.STATUS_EVENT.STATUS_HOST_DEFERRED_AUTH , trans.isSuppressPosDialog());
        trans.updateMessageStatus(DEFERRED_AUTH);

    }
}
