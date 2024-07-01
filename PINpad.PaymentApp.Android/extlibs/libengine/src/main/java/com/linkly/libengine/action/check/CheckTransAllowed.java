package com.linkly.libengine.action.check;

import static com.linkly.libengine.engine.protocol.IProto.RejectReasonType.PREAUTH_TRANS_LIMIT_EXCEEDED;
import static com.linkly.libengine.engine.protocol.IProto.RejectReasonType.TRANS_NOT_ALLOWED;

import com.linkly.libengine.action.IAction;
import com.linkly.libengine.action.TransactionDecliner;
import com.linkly.libengine.config.PayCfg;
import com.linkly.libengine.engine.EngineManager;
import com.linkly.libengine.engine.customers.ICustomer;
import com.linkly.libengine.engine.protocol.IProto;
import com.linkly.libengine.engine.transactions.TransRecManager;

import java.util.Objects;

import timber.log.Timber;

/**
 * Checks the transaction type against config to see if transaction type is allowed
 * If not allowed, it throws up a an error screen, packs the transaction with appropriate field values & declines it
 * */
public class CheckTransAllowed extends IAction {

    @Override
    public String getName() {
        return "CheckTransAllowed";
    }

    @SuppressWarnings("java:S3776") // cognitive complexity(19)
    @Override
    public void run() {
        EngineManager.TransType transType = super.trans.getTransType();
        boolean transAllowed = false;
        PayCfg payCfg = d.getPayCfg();
        ICustomer customer = d.getCustomer();
        IProto.RejectReasonType reasonType = TRANS_NOT_ALLOWED;

        if( payCfg != null && customer != null ) {
            switch ( transType ) {
                case SALE_AUTO:
                case SALE:
                case RECONCILIATION_AUTO:
                case AUTO_LOGON:
                    transAllowed = true;
                    break;

                case CARD_NOT_PRESENT_REFUND:
                    transAllowed = payCfg.isManualAllowed() && payCfg.isRefundTransAllowed();
                    break;

                case SALE_MOTO:
                case SALE_MOTO_AUTO:
                case CARD_NOT_PRESENT:
                    transAllowed = payCfg.isManualAllowed();
                    break;

                case CASHBACK:
                case CASHBACK_AUTO:
                    transAllowed = payCfg.isCashBackAllowed() && !trans.isStartedInOfflineMode();
                    break;
                case CASH:
                case CASH_AUTO:
                    transAllowed = payCfg.isCashTransAllowed() && !trans.isStartedInOfflineMode();
                    break;

                case REFUND_AUTO:
                case REFUND:
                    transAllowed = payCfg.isRefundTransAllowed();
                    break;

                case MANUAL_REVERSAL_AUTO:
                    transAllowed = customer.supportAutoReversals();
                    break;

                case COMPLETION:
                case COMPLETION_AUTO:
                case PREAUTH_CANCEL:
                case PREAUTH_CANCEL_AUTO:
                    transAllowed = payCfg.isPreAuthTransAllowed();
                    break;

                case PREAUTH:
                case PREAUTH_AUTO:
                    transAllowed = payCfg.isPreAuthTransAllowed();
                    if (transAllowed && !isUnderMaxPreAuthTrans() ) {
                        transAllowed = false;
                        reasonType = PREAUTH_TRANS_LIMIT_EXCEEDED;
                    }
                    break;

                case PREAUTH_MOTO:
                case PREAUTH_MOTO_AUTO:
                    transAllowed = payCfg.isPreAuthTransAllowed() && payCfg.isManualAllowed();
                    if(transAllowed && !isUnderMaxPreAuthTrans() ) {
                        transAllowed = false;
                        reasonType = PREAUTH_TRANS_LIMIT_EXCEEDED;
                    }
                    break;

                case REFUND_MOTO:
                case REFUND_MOTO_AUTO:
                    transAllowed = payCfg.isRefundTransAllowed() && payCfg.isManualAllowed();
                    break;

                default:
                    Timber.e( "Transaction Type [%s] not supported yet", transType );
                    break;
            }
        } else {
            Timber.e( "PayCfgImpl or Customer config is null, declining transaction with not allowed" );
        }

        if( !transAllowed ){
            // set transaction decline reason
            Timber.e( "Trans type %s not allowed, declining", transType.name() );
            Objects.requireNonNull(d.getProtocol()).setInternalRejectReason( super.trans, reasonType );
            d.getWorkflowEngine().setNextAction( TransactionDecliner.class );
        }
    }

    private boolean isUnderMaxPreAuthTrans() {
        long countPreAuthTrans = TransRecManager.getInstance().getTransRecDao().countTransByTypeAndApproved(
                EngineManager.TransType.PREAUTH,
                EngineManager.TransType.PREAUTH_AUTO,
                EngineManager.TransType.PREAUTH_MOTO,
                EngineManager.TransType.PREAUTH_MOTO_AUTO
        );
        return countPreAuthTrans < Integer.parseInt(d.getPayCfg().getMaxPreAuthTrans());
    }

}
