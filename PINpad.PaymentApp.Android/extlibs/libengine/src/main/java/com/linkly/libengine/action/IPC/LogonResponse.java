package com.linkly.libengine.action.IPC;

import com.linkly.libengine.action.IAction;
import com.linkly.libengine.action.Printing.PrintFirst;
import com.linkly.libengine.engine.transactions.TransRec;
import com.linkly.libengine.helpers.ECRHelpers;

/**
 * Action class which calls {@link ECRHelpers} class method for logon event
 */
public class LogonResponse extends IAction {
    @Override
    public String getName() {
        return "LogonResponse";
    }

    @Override
    public void run() {
        printLogonReceipt( super.trans );
        ECRHelpers.ipcSendLogonResponse(super.d, super.trans, context);
    }

    /**
     * Will generate a Logon receipt for broadcast purposes
     *
     * @param trans {@link TransRec} object
     */
    private void printLogonReceipt( TransRec trans ) {
        if ( trans.getTransEvent() != null && trans.getTransEvent().isPosPrintingSync() ) {
            PrintFirst.buildAndBroadcastReceipt( d, trans, PrintFirst.ReceiptType.LOGON, false, context, mal );

            if ( trans.isPrintOnTerminal() ) {
                trans.print( d, true, false, mal );
            }
        } else {
            trans.print( d, true, false, mal );
        }
    }
}
