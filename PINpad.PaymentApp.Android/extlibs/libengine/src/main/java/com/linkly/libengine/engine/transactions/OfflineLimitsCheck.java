package com.linkly.libengine.engine.transactions;

import static com.linkly.libengine.engine.protocol.IProto.RejectReasonType.NOT_SET;
import static com.linkly.libengine.engine.protocol.IProto.RejectReasonType.OFFLINE_TRANS_AMOUNT_LIMIT_EXCEEDED;
import static com.linkly.libengine.engine.protocol.IProto.RejectReasonType.OFFLINE_TRANS_COUNT_LIMIT_EXCEEDED;
import static com.linkly.libengine.engine.protocol.IProto.RejectReasonType.TOTAL_OFFLINE_TRANS_AMOUNT_LIMIT_EXCEEDED;
import static com.linkly.libengine.engine.transactions.properties.TCard.CaptureMethod.NOT_CAPTURED;

import com.linkly.libengine.dependencies.IDependency;
import com.linkly.libengine.engine.protocol.IProto;
import com.linkly.libengine.engine.transactions.properties.TProtocol;

import timber.log.Timber;

public class OfflineLimitsCheck {
    public interface OfflineLimitsCheckCallback{
        void transCountExceeded();
        void transValueExceeded();
        void softLimitCountExceeded();
        void softLimitValueExceeded();
        void transactionLimitExceeded();
    }

    private final TransRec trans;
    private final OfflineLimitsCheckCallback callback;
    private final IDependency d;

    public OfflineLimitsCheck(IDependency d, TransRec trans, OfflineLimitsCheckCallback callback) {
        this.d = d;
        this.trans = trans;
        this.callback = callback;
    }

    /**
     * counts all offline/SAF transactions,
     */
    public void execute() {
        long transactionAmount = trans.getAmounts().getTotalAmount();
        TProtocol.MessageStatus[] offlineApprovedMessageStatuses = new TProtocol.MessageStatus[]{
                TProtocol.MessageStatus.ADVICE_QUEUED,
                TProtocol.MessageStatus.REVERSAL_QUEUED
        };
        int offlineApprovedTransactionsCount = TransRecManager.getInstance().getTransRecDao()
                .getTransCountByMsgStatus(offlineApprovedMessageStatuses);
        long offlineApprovedTransactionsAmount = TransRecManager.getInstance().getTransRecDao()
                .getTotalAmountWithMsgStatus(offlineApprovedMessageStatuses);

        Timber.i( "offline approved count = %d, amount = %d", offlineApprovedTransactionsCount, offlineApprovedTransactionsAmount );

        // check for upper limit first, if already gonna cross upper limit no need to check soft limit
        double upperLimitAmountCents = d.getPayCfg().getOfflineUpperLimitAmountCents();
        int upperLimitCount = d.getPayCfg().getOfflineUpperLimitCount();
        if (offlineApprovedTransactionsCount >= upperLimitCount) {
            Timber.e( "offline transaction count limit exceeded, trans will probably decline");
            callback.transCountExceeded();
            return;
        } else if ((offlineApprovedTransactionsAmount + transactionAmount) > upperLimitAmountCents) {
            Timber.e("Total offline transaction amount limit exceeded, trans will probably decline");
            callback.transValueExceeded();
            return;
        }

        // check soft limits
        double softLimitAmountCents = d.getPayCfg().getOfflineSoftLimitAmountCents();
        int softLimitCount = d.getPayCfg().getOfflineSoftLimitCount();

        if (offlineApprovedTransactionsCount >= softLimitCount ) {
            Timber.w( "offline transaction count soft limit exceeded");
            callback.softLimitCountExceeded();
        } else if((offlineApprovedTransactionsAmount + transactionAmount) > softLimitAmountCents) {
            Timber.w( "offline transaction amount soft limit exceeded");
            callback.softLimitValueExceeded();
        }

        // check transaction limits, if card is captured

        if (trans.getCard().getCaptureMethod() != NOT_CAPTURED) {
           // check the transaction amount limit isn't exceeded. We check before card is presented, but new amount incl surcharge/tip could exceed trans limit
            if (trans.getCard().isIccCaptured() || trans.getCard().isSwiped()) {
                if (trans.getAmounts().getTotalAmount() > d.getPayCfg().getOfflineTransactionCeilingLimitCentsContact() ) {
                    Timber.e( "contact transaction limit exceeded, trans will probably decline");
                    callback.transactionLimitExceeded();
                }
            } else if (trans.getCard().isCtlsCaptured()) {
                if (trans.getAmounts().getTotalAmount() > d.getPayCfg().getOfflineTransactionCeilingLimitCentsContactless() ) {
                    Timber.e( "contactless transaction limit exceeded, trans will probably decline");
                    callback.transactionLimitExceeded();
                }
            }
        }
    }

    /**
     * used as part of logic to determine if we should authorise the current transaction following comms failure
     * checks if current transaction would exceed the 'hard' transaction count and/or amount limits
     *
     * @return false - transaction won't exceed any limits, true - trans would exceed limits, recommend decline
     */
    public static IProto.RejectReasonType wouldTransactionExceedOfflineLimits(IDependency d, TransRec trans) {
        final IProto.RejectReasonType[] reason = new IProto.RejectReasonType[1];
        reason[0] = NOT_SET;

        // check the cumulative/SAF limits
        OfflineLimitsCheck limitCheck = new OfflineLimitsCheck(d, trans, new OfflineLimitsCheck.OfflineLimitsCheckCallback() {
            @Override
            public void transCountExceeded() {
                reason[0] = OFFLINE_TRANS_COUNT_LIMIT_EXCEEDED;
            }

            @Override
            public void transValueExceeded() {
                reason[0] = TOTAL_OFFLINE_TRANS_AMOUNT_LIMIT_EXCEEDED;
            }

            @Override
            public void softLimitCountExceeded() {
                // ignore
            }

            @Override
            public void softLimitValueExceeded() {
                // ignore
            }

            @Override
            public void transactionLimitExceeded() {
                // check the transaction amount limit isn't exceeded. We check before card is presented, but new amount incl surcharge/tip could exceed trans limit
                reason[0] = OFFLINE_TRANS_AMOUNT_LIMIT_EXCEEDED;
            }
        });
        limitCheck.execute();

        return reason[0];
    }
}
