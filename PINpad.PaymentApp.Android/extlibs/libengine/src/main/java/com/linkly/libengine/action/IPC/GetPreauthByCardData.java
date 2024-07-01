package com.linkly.libengine.action.IPC;


import static com.linkly.libengine.engine.EngineManager.TransType.PREAUTH;
import static com.linkly.libengine.engine.EngineManager.TransType.PREAUTH_AUTO;
import static com.linkly.libengine.engine.EngineManager.TransType.PREAUTH_MOTO;
import static com.linkly.libengine.engine.EngineManager.TransType.PREAUTH_MOTO_AUTO;
import static com.linkly.libengine.status.IStatus.STATUS_EVENT.STATUS_ERR_PREAUTH_NOT_FOUND;
import static com.linkly.libsecapp.IP2PEncrypt.EncryptForStorageType.SHORT_TRACK_FORMAT;
import static com.linkly.libui.UIScreenDef.PREAUTH_NOT_FOUND;

import com.linkly.libengine.action.IAction;
import com.linkly.libengine.action.TransactionCanceller;
import com.linkly.libengine.engine.protocol.IProto;
import com.linkly.libengine.engine.transactions.TransRec;
import com.linkly.libengine.engine.transactions.TransRecManager;
import com.linkly.libsecapp.IP2PEncrypt;
import com.linkly.libsecapp.P2PLib;
import com.linkly.libsecapp.emv.Util;

import java.util.List;

import timber.log.Timber;

public class GetPreauthByCardData extends IAction {
    @Override
    public String getName() {
        return "GetPreauthByCardData";
    }

    @Override
    public void run() {
        trans.setPreauthUid(getPreauthByCardDetails());
        if( trans.getPreauthUid() == null ) {
            Timber.e( "error retrieving preauth card details for given card" );
            // couldn't find original preauth, handle error
            ui.showScreen(PREAUTH_NOT_FOUND);
            d.getProtocol().setInternalRejectReason( trans, IProto.RejectReasonType.PREAUTH_NOT_FOUND );
            d.getStatusReporter().reportStatusEvent(STATUS_ERR_PREAUTH_NOT_FOUND , trans.isSuppressPosDialog());
            d.getWorkflowEngine().setNextAction(TransactionCanceller.class);
        } else {
            // else found card details, preauth txn uid will be set in current trans record
            Timber.i( "retrieved original preauth card details using customer card presented" );
            // set cardholder present, because card is present
            trans.getCard().setCardholderPresent(true);
        }
    }

    /**
     * searches for original preauth txn record for current card
     * sets trans.uid value if found
     *
     * @return UID = record found, null = not found
     */
    public static Integer getPreauthByCardDetails() {
        P2PLib p2pInstance = P2PLib.getInstance();
        IP2PEncrypt p2pEncrypt = p2pInstance.getIP2PEncrypt();
        Integer foundPreAuthUid = null;

        // for each preauth in the terminal
        // get current txn card encrypted data
        byte[] currentCardEncrypted = p2pEncrypt.encryptForStorage(SHORT_TRACK_FORMAT);
        if( currentCardEncrypted == null ) {
            // error condition, log as error so hopefully we pick up in debug/test
            Timber.e( "current card data is null" );
            return foundPreAuthUid;
        }

        List<TransRec> preauths = TransRecManager.getInstance().getTransRecDao().getByTransTypesAndApproved(PREAUTH, PREAUTH_AUTO, PREAUTH_MOTO, PREAUTH_MOTO_AUTO);
        if( preauths != null ) {
            for (TransRec preauth : preauths) {
                // get secure app to compare short track formats (PANs+expiry) for us.
                // NOTE: this will match regardless of card entry method, across manpan, swipe, tap, insert
                if (p2pEncrypt.compareTrackData(SHORT_TRACK_FORMAT,
                        Util.hexStringToByteArray(preauth.getSecurity().getEncTrack2()),
                        currentCardEncrypted)) {
                    // match, break out
                    Timber.i("found trans type PREAUTH with matching card details");
                    foundPreAuthUid = preauth.getUid();
                    break;
                }
            }
        }

        return foundPreAuthUid;
    }
}
