package com.linkly.libengine.action.cardprocessing;

import static com.linkly.libengine.engine.transactions.properties.TProtocol.ACC_TYPE_CREDIT;
import static com.linkly.libengine.status.IStatus.STATUS_EVENT.STATUS_UI_MANUAL_PAN_ACTIVITY;
import static com.linkly.libsecapp.IP2PEncrypt.ElementType.CVV;
import static com.linkly.libsecapp.IP2PEncrypt.ElementType.EXPIRY_YYMM;
import static com.linkly.libsecapp.IP2PEncrypt.ElementType.PAN;

import android.os.SystemClock;

import com.linkly.libengine.action.IAction;
import com.linkly.libengine.action.TransactionCanceller;
import com.linkly.libengine.action.check.CheckUserLevel;
import com.linkly.libengine.debug.IDebug;
import com.linkly.libengine.engine.protocol.IProto;
import com.linkly.libengine.engine.transactions.properties.TCard;
import com.linkly.libsecapp.IP2PCard;
import com.linkly.libsecapp.IP2PEncrypt;
import com.linkly.libsecapp.P2PLib;
import com.linkly.libui.IUIDisplay;

import timber.log.Timber;

public class GetMOTO  extends IAction {

    public static final int MAN_PAN_ENTRY_LEN_MIN = 14;
    public static final int MAN_PAN_ENTRY_LEN_MAX = 19;
    // CSC/CVV etc
    public static final int CSC_LEN_MIN = 3;
    public static final int CSC_LEN_MAX = 4;

    @Override
    public String getName() {
        return "GetMOTO";
    }

    @Override
    public void run() {

        if (trans.getCard().getCardIndex() != -1) {
            Timber.i("We already have the card details read");
            trans.updateCardStateToMatchType(trans.getCard().getCardType());
            return;
        }

        IP2PCard iMalCard = P2PLib.getInstance().getIP2PCard();
        iMalCard.cardReset(true);
        trans.getCard().getLedStatus().setCTLSEnabled(trans.getCard().isCtlsAllowed());
        trans.getCard().setCaptureMethod(TCard.CaptureMethod.NOT_CAPTURED);

        manualCardGet();
    }

    private void manualCardGet() {

        if ((d.getPayCfg().getPasswordRequiredForAllCards(trans, TCard.CaptureMethod.MANUAL) ) &&
            (!CheckUserLevel.runUserLogin(d, ui))) {
            return;
        }

        P2PLib p2pInstance = P2PLib.getInstance();
        p2pInstance.getIP2PEncrypt().clearData();
        d.getStatusReporter().reportStatusEvent(STATUS_UI_MANUAL_PAN_ACTIVITY , trans.isSuppressPosDialog());


        // SGM: Another hack here :(
        // Seems that our P2Pe getManualPan does not accommodate timeouts.
        // Rather than trying to fix the entire chain (heavy work) for a minor bug easier to just start a timer then check the time after its failed.
        long timeoutStart = SystemClock.elapsedRealtime();
        int screenTimeoutMS = IUIDisplay.LONG_TIMEOUT;

        // SGM: All aboard the hack train & peak coding.....
        // Race condition between secapp manpan activity and displaying the previous ActTrans screen (UIProcessing).
        // There is a race condition between the ActTransaction & the secapp's ManPanActivity.
        // If the ActTransaction gets displayed first everything is fine.
        // If ManPan Activity gets displayed first, no ManPan gets displayed with UI PLEASE wait screen.
        // To make matters worse, depending on the timing for some reason the timeout value for manpan gets messed up and we are stuck indefinitely.
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            Timber.e("Error... timeout thread interrupted.");
        }

        if ( p2pInstance.getIP2PActivity().getManualPan(
                MAN_PAN_ENTRY_LEN_MIN,
                MAN_PAN_ENTRY_LEN_MAX,
                screenTimeoutMS,
                trans.getAmounts().getTotalAmount(),
                d.getPayCfg().isMotoCVVEntry() ? CSC_LEN_MIN : 0,
                d.getPayCfg().isMotoCVVEntry() ? CSC_LEN_MAX : 0,
                d.getPayCfg().isMotoCVVEntryBypassAllowed()) ) {

            IP2PEncrypt p2pEncrypt = p2pInstance.getIP2PEncrypt();
            String maskedManPan = p2pEncrypt.getData(PAN);
            String expiry = p2pEncrypt.getData(EXPIRY_YYMM);
            String cvv = p2pEncrypt.getData(CVV);

            trans.updateCardStateToMatchType(TCard.CardType.MANUAL);
            trans.getCard().setPan(maskedManPan);
            trans.getCard().setMaskedPan(maskedManPan);
            trans.getCard().setExpiry(expiry);
            trans.getCard().setCvv(cvv);
            // assume all MOTO txns are credit
            trans.getProtocol().setAccountType(ACC_TYPE_CREDIT);


            Timber.i("Masked PAN    = %s", maskedManPan);
            Timber.i("Expiry (YYMM) = %s", expiry);
            Timber.i("Masked CVV    = %s", cvv);

        } else {
            long endTime = SystemClock.elapsedRealtime();
            long leeway = 1000; // 1 second leeway for timeout in case other processing gets in the way.
            // We don't know if we have timed out or user has hit cancelled.
            // Easiest way is to just assume if we are close enough to the timeout we can report User timeout rather than cancelled.
            // If we are close enough to timeout just assume that we've timed out.
            if(endTime - timeoutStart > (screenTimeoutMS - leeway)) {
                d.getDebugReporter().reportTimeout(IDebug.DEBUG_POSITION.ENTER_CARD);
                d.getProtocol().setInternalRejectReason(trans, IProto.RejectReasonType.USER_TIMEOUT);
            } else {
                d.getDebugReporter().reportCancelSelect(IDebug.DEBUG_POSITION.ENTER_CARD);
            }

            d.getWorkflowEngine().setNextAction(TransactionCanceller.class);
            //set action
            //todo this cancellation is not propagated properly
        }
    }
}

