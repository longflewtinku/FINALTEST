package com.linkly.libengine.action.check;

import static com.linkly.libengine.engine.EngineManager.TransType.OFFLINESALE;
import static com.linkly.libengine.engine.EngineManager.TransType.PREAUTH;
import static com.linkly.libengine.engine.EngineManager.TransType.REFUND;
import static com.linkly.libengine.engine.EngineManager.TransType.SALE;

import com.linkly.libengine.action.IAction;
import com.linkly.libengine.action.TransactionReferral;
import com.linkly.libengine.engine.transactions.TransRec;
import com.linkly.libengine.status.IStatus;
public class CheckReferralRequired extends IAction {
    @Override
    public String getName() {
        return "CheckReferralRequired";
    }

    @Override
    public void run() {
        d.getStatusReporter().reportStatusEvent( IStatus.STATUS_EVENT.STATUS_PROCESSING ,trans.isSuppressPosDialog() );
        checkReferralRequired(trans);
    }

    private void checkReferralRequired(TransRec trans) {
        if (this.isReferralRequired(trans)) {
            d.getWorkflowEngine().setNextAction(TransactionReferral.class);
        }
    }

    //todo copied from TransRec, this function is only used in the TaskCheck(REFERRAL) -> can be moved here and removed from TransRec class
    private boolean isReferralRequired(TransRec trans) {
        if ((trans.getCard().isManual() && (trans.getTransType() == SALE || trans.getTransType() == REFUND || trans.getTransType() == PREAUTH)) &&
            (trans.getCard().getCardsConfig(d.getPayCfg()).isForceReferral())) {
            return true;
        }

        return (trans.getTransType() == OFFLINESALE) && trans.getProtocol().isCanAuthOffline();
    }
}
