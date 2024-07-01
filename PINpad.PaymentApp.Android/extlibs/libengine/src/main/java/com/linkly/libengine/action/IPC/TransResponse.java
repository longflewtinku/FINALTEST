package com.linkly.libengine.action.IPC;

import com.linkly.libengine.action.IAction;
import com.linkly.libengine.helpers.ECRHelpers;


public class TransResponse extends IAction {
    @Override
    public String getName() {
        return "TransResponse";
    }

    @Override
    public void run() {

        ECRHelpers.ipcSendTransResponse( d, trans, context );
        // Update the trans record stating we have reported the result
        trans.setReportedToPOS(true);
        trans.save();
    }
}
