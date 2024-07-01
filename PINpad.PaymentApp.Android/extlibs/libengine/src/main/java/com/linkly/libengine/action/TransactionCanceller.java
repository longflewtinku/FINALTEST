package com.linkly.libengine.action;

import com.linkly.libengine.engine.protocol.IProto;
import com.linkly.libengine.engine.transactions.properties.TProtocol;

public class TransactionCanceller extends IAction {
    @Override
    public String getName() {
        return "TransactionCanceller";
    }

    @Override
    public void run() {
        trans.setApproved(false);
        trans.setCancelled(true);
        trans.setDeclined(false);

        // if the transaction is cancelled then we can set this here to save everyone who calls it passing it in
        if ( trans.getAudit().getRejectReasonType() == IProto.RejectReasonType.NOT_SET ) {
                super.d.getProtocol().setInternalRejectReason( super.trans, IProto.RejectReasonType.CANCELLED );
        }

        if (trans.getAmounts().getDiscountedAmount() > 0) {
            IProto iproto = d.getProtocol();
            iproto.discountVoucherReverse(trans);
        }

        // set message status to finalised, so no power fail process occurs
        trans.updateMessageStatus(TProtocol.MessageStatus.FINALISED);
    }
}
