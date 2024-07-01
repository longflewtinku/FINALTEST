package com.linkly.libengine.action.cardprocessing;

import static com.linkly.libengine.engine.transactions.properties.TCard.CaptureMethod.ICC;
import static com.linkly.libsecapp.IP2PEMV.P2P_EMV_ERROR_CODES.P2P_EMV_CARD_REMOVED;
import static com.linkly.libsecapp.IP2PEMV.P2P_EMV_ERROR_CODES.P2P_EMV_OK;
import static com.linkly.libui.UIScreenDef.READ_ERROR_REMOVE_CARD;

import com.linkly.libengine.action.IAction;
import com.linkly.libengine.action.TransactionCanceller;
import com.linkly.libengine.action.TransactionDecliner;
import com.linkly.libengine.action.check.CheckCVM;
import com.linkly.libengine.engine.cards.Emv;
import com.linkly.libengine.engine.protocol.IProto;
import com.linkly.libengine.engine.transactions.TransRec;
import com.linkly.libengine.engine.transactions.properties.TCard;
import com.linkly.libengine.helpers.UIHelpers;
import com.linkly.libsecapp.IP2PEMV;
import com.linkly.libsecapp.P2PLib;

import timber.log.Timber;

public class ICCGAC1 extends IAction {
    @Override
    public String getName() {
        return "ICCGAC1";
    }

    @Override
    public void run() {

        //todo card type EMV should be renamed to ICC, EMV is also CTLS
        if (trans.getCard().getCardType() != TCard.CardType.EMV) {
            Timber.i( "Not an ICC, skip task");
            return;
        }

        IP2PEMV.P2P_EMV_ERROR_CODES eRet = Emv.getInstance().genAc1(d,trans, mal);
        Timber.i( "genAc1 result = %s", eRet.toString());
        switch(eRet) {
            case P2P_EMV_USER_CANCELLED:
            case P2P_EMV_USER_TIMEOUT:
                d.getWorkflowEngine().setNextAction(TransactionCanceller.class);
                return;
            default:
                if (trans.getCard().isShowReadError()) {
                    cardReadError(trans);
                }
                if (eRet != P2P_EMV_OK && trans.getCard().getCaptureMethod() == ICC) {
                    IProto iproto = d.getProtocol();
                    assert iproto != null;

                    if( eRet == P2P_EMV_CARD_REMOVED ) {
                        iproto.setInternalRejectReason(trans, IProto.RejectReasonType.CARD_REMOVED);
                    } else {
                        iproto.setInternalRejectReason(trans, IProto.RejectReasonType.DECLINED_BY_CARD_PRE_COMMS);
                    }
                    trans.save();

                    d.getWorkflowEngine().setNextAction(TransactionDecliner.class);
                }
                break;
        }

        CheckCVM checkCVM = new CheckCVM();
        checkCVM.run(d, mal, context);
    }

    private void cardReadError(TransRec trans) {
        if (trans == null || trans.getCard().isShowReadError()) {
            UIHelpers.cardRemoveWithMessage(d, mal, READ_ERROR_REMOVE_CARD, true, false, trans);
            if (trans != null) {
                trans.getCard().setShowReadError(false);
            }
        }
    }
}
