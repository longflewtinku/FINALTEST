package com.linkly.libengine.action.check;

import static com.linkly.libengine.engine.transactions.properties.TProtocol.ACC_TYPE_CHEQUE;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.ACC_TYPE_SAVINGS;
import static com.linkly.libui.UIScreenDef.ONLY_CREDIT_ACCOUNT_ALLOWED;

import com.linkly.libengine.action.IAction;
import com.linkly.libengine.action.TransactionDecliner;
import com.linkly.libengine.engine.protocol.IProto;

import java.util.Objects;

public class CheckCreditAccountAllowed extends IAction {

    @Override
    public String getName() {
        return "CheckCreditAccountAllowed";
    }

    @Override
    public void run() {
        //Check for pre-auth transaction and if account type is cheque or savings, decline the transaction
        if (trans.isPreAuth() && d.getPayCfg().isPreauthCreditAccountOnly() &&
            (trans.getProtocol().getAccountType() == ACC_TYPE_CHEQUE ||
             trans.getProtocol().getAccountType() == ACC_TYPE_SAVINGS)) {
            ui.showScreen(ONLY_CREDIT_ACCOUNT_ALLOWED);
            Objects.requireNonNull(d.getProtocol()).setInternalRejectReason(super.trans, IProto.RejectReasonType.PREAUTH_NOT_ALLOWED_FOR_CARD);
            d.getWorkflowEngine().setNextAction(TransactionDecliner.class);
        }
    }
}
