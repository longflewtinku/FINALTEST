package com.linkly.libengine.action.check;

import static com.linkly.libui.IUIDisplay.ACTIVITY_ID.ACT_INFORMATION;
import static com.linkly.libui.UIScreenDef.OFFLINE_TRANS_AMOUNT_LIMIT_EXCEEDED;
import static com.linkly.libui.UIScreenDef.REFUND_COUNT_LIMIT_EXCEEDED;
import static com.linkly.libui.UIScreenDef.REFUND_LIMIT_EXCEEDED;

import com.linkly.libengine.action.IAction;
import com.linkly.libengine.action.TransactionCanceller;
import com.linkly.libengine.engine.protocol.IProto;
import com.linkly.libengine.engine.transactions.TransRec;
import com.linkly.libengine.users.User;
import com.linkly.libengine.users.UserManager;
import com.linkly.libmal.global.util.Util;
import com.linkly.libui.IUIDisplay;

/**
 * Checks TMS Refund limits
 */
public class CheckRefundLimits extends IAction {
    @Override
    public String getName() {
        return "CheckRefundLimits";
    }

    @Override
    public void run() {
        // Check the current transaction amount against refund limit
        long refundAmount = super.trans.getAmounts().getTotalAmount();

        long refundLimit;
        if (UserManager.getActiveUser().getPrivileges() == User.Privileges.MANAGER) {
            refundLimit = this.returnValue(super.d.getPayCfg().getManagerRefundLimit());
        } else {
            refundLimit = this.returnValue(super.d.getPayCfg().getMaxRefundLimit());
        }
        long maxCumulativeRefundLimit = this.returnValue(
                super.d.getPayCfg().getMaxCumulativeRefundLimit()
        );
        long maxRefundCount = this.returnValue(
                super.d.getPayCfg().getMaxRefundCount()
        );

        IProto.RejectReasonType reasonType = IProto.RejectReasonType.NOT_SET;
        if (trans.isStartedInOfflineMode()) {
            long offlineTransCeilingLimit = Math.max(d.getPayCfg().getOfflineTransactionCeilingLimitCentsContact(), d.getPayCfg().getOfflineTransactionCeilingLimitCentsContactless());
            if (refundLimit != 0) {
                refundLimit = Math.min(offlineTransCeilingLimit, refundLimit);
            } else {
                refundLimit = offlineTransCeilingLimit;
            }

        }

        // TODO: Q2: There will be a time period TMS flag which will be used to calculate totalRefund in last day
        if (this.checkNotZeroAndLessThanValue(refundLimit, refundAmount) ||
                this.checkNotZeroAndLessThanValue(maxCumulativeRefundLimit, TransRec.getRefundAmountFromMidnight() + refundAmount)) {
            if (trans.isStartedInOfflineMode()) {
                reasonType = IProto.RejectReasonType.OFFLINE_TRANS_AMOUNT_LIMIT_EXCEEDED;
                ui.showScreen(OFFLINE_TRANS_AMOUNT_LIMIT_EXCEEDED);
                ui.getResultCode(ACT_INFORMATION, IUIDisplay.SHORT_TIMEOUT);
            } else {
                reasonType = IProto.RejectReasonType.REFUND_LIMIT_EXCEEDED;
                ui.showScreen(REFUND_LIMIT_EXCEEDED);
                ui.getResultCode(ACT_INFORMATION, IUIDisplay.SHORT_TIMEOUT);
            }
        } else if (this.checkNotZeroAndLessThanValue( maxRefundCount,TransRec.getRefundCountFromMidnight() + 1 )) {
            reasonType = IProto.RejectReasonType.REFUND_LIMIT_COUNT_EXCEEDED;
            ui.showScreen(REFUND_COUNT_LIMIT_EXCEEDED);
            ui.getResultCode(ACT_INFORMATION, IUIDisplay.SHORT_TIMEOUT);
        }

        if( reasonType != IProto.RejectReasonType.NOT_SET ) {
            // Cancel transaction
            d.getProtocol().setInternalRejectReason( super.trans, reasonType );
            d.getWorkflowEngine().setNextAction( TransactionCanceller.class );
        }
    }

    /**
     * Checks if the limit passed is valid (> 0) & is less than the value
     * @param limit to be checked
     * @param value to be checked against
     * */
    private boolean checkNotZeroAndLessThanValue( long limit, long value ) {
        return ( limit > 0 && value > limit );
    }

    /**
     * Returns Parsed Long if not null or Empty. Also checks if the string is actually numeric
     * @param amount String to be parsed
     * @return value : 0 or {@link Long#parseLong(String)}
     * */
    private long returnValue( String amount ) {
        return ( !Util.isNullOrEmpty( amount ) && Util.isNumericString( amount ) ) ?
                Long.parseLong( amount ) :
                0;
    }
}
