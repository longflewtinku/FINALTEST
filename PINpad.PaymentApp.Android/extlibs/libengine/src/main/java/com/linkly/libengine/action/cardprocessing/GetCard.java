package com.linkly.libengine.action.cardprocessing;

import static com.linkly.libengine.action.cardprocessing.GetMOTO.CSC_LEN_MAX;
import static com.linkly.libengine.action.cardprocessing.GetMOTO.CSC_LEN_MIN;
import static com.linkly.libengine.action.cardprocessing.GetMOTO.MAN_PAN_ENTRY_LEN_MAX;
import static com.linkly.libengine.action.cardprocessing.GetMOTO.MAN_PAN_ENTRY_LEN_MIN;
import static com.linkly.libengine.engine.transactions.properties.TAudit.ReasonOnlineCode.RTIME_FORCED_CARD_ACCEPTOR;
import static com.linkly.libengine.engine.transactions.properties.TCard.CvmType.CDCVM;
import static com.linkly.libengine.status.IStatus.STATUS_EVENT.STATUS_ERR_CHIP_UNREADABLE;
import static com.linkly.libengine.status.IStatus.STATUS_EVENT.STATUS_UI_GETCARD;
import static com.linkly.libengine.status.IStatus.STATUS_EVENT.STATUS_UI_GETCARD_CTLS;
import static com.linkly.libengine.status.IStatus.STATUS_EVENT.STATUS_UI_GETCARD_CTLS_MAN;
import static com.linkly.libengine.status.IStatus.STATUS_EVENT.STATUS_UI_GETCARD_CTLS_MSR;
import static com.linkly.libengine.status.IStatus.STATUS_EVENT.STATUS_UI_GETCARD_ICC;
import static com.linkly.libengine.status.IStatus.STATUS_EVENT.STATUS_UI_GETCARD_ICC_CTLS;
import static com.linkly.libengine.status.IStatus.STATUS_EVENT.STATUS_UI_GETCARD_ICC_MAN;
import static com.linkly.libengine.status.IStatus.STATUS_EVENT.STATUS_UI_GETCARD_ICC_MSR;
import static com.linkly.libengine.status.IStatus.STATUS_EVENT.STATUS_UI_GETCARD_MAN;
import static com.linkly.libengine.status.IStatus.STATUS_EVENT.STATUS_UI_GETCARD_MAN_CTLS_ICC;
import static com.linkly.libengine.status.IStatus.STATUS_EVENT.STATUS_UI_GETCARD_MSR;
import static com.linkly.libengine.status.IStatus.STATUS_EVENT.STATUS_UI_GETCARD_MSR_ICC_CTLS;
import static com.linkly.libengine.status.IStatus.STATUS_EVENT.STATUS_UI_GETCARD_MSR_ICC_MAN;
import static com.linkly.libengine.status.IStatus.STATUS_EVENT.STATUS_UI_GETCARD_MSR_MAN;
import static com.linkly.libengine.status.IStatus.STATUS_EVENT.STATUS_UI_GETCARD_MSR_MAN_CTLS;
import static com.linkly.libengine.status.IStatus.STATUS_EVENT.STATUS_UI_MANUAL_PAN_ACTIVITY;
import static com.linkly.libsecapp.IP2PCard.CardType.CT_CTLS;
import static com.linkly.libsecapp.IP2PCard.CardType.CT_CTLS_FAULTY;
import static com.linkly.libsecapp.IP2PCard.CardType.CT_EMV;
import static com.linkly.libsecapp.IP2PCard.CardType.CT_EMV_FAULTY;
import static com.linkly.libsecapp.IP2PCard.CardType.CT_MSR;
import static com.linkly.libsecapp.IP2PCard.CardType.CT_MSR_FAULTY;
import static com.linkly.libsecapp.IP2PCard.CardType.CT_NONE;
import static com.linkly.libsecapp.IP2PEncrypt.ElementType.CVV;
import static com.linkly.libsecapp.IP2PEncrypt.ElementType.EXPIRY_YYMM;
import static com.linkly.libsecapp.IP2PEncrypt.ElementType.PAN;
import static com.linkly.libui.IUIDisplay.ACTIVITY_ID.ACT_GET_CARD;
import static com.linkly.libui.IUIDisplay.NO_TIMEOUT;
import static com.linkly.libui.IUIDisplay.UIResultCode.CARDREAD;
import static com.linkly.libui.IUIDisplay.UIResultCode.OK;
import static com.linkly.libui.IUIDisplay.UIResultCode.RESTART;
import static com.linkly.libui.IUIDisplay.UIResultCode.TIMEOUT;
import static com.linkly.libui.UIScreenDef.ERROR_PRESENT_ONE_CARD;
import static com.linkly.libui.UIScreenDef.GET_CARD_CDCVM_SCREEN;
import static com.linkly.libui.UIScreenDef.GET_CARD_CTLS_MAN_SCREEN;
import static com.linkly.libui.UIScreenDef.GET_CARD_CTLS_MSR_SCREEN;
import static com.linkly.libui.UIScreenDef.GET_CARD_CTLS_SCREEN;
import static com.linkly.libui.UIScreenDef.GET_CARD_ICC_CTLS_SCREEN;
import static com.linkly.libui.UIScreenDef.GET_CARD_ICC_MAN_SCREEN;
import static com.linkly.libui.UIScreenDef.GET_CARD_ICC_MSR_SCREEN;
import static com.linkly.libui.UIScreenDef.GET_CARD_ICC_SCREEN;
import static com.linkly.libui.UIScreenDef.GET_CARD_MAN_CTLS_ICC_SCREEN;
import static com.linkly.libui.UIScreenDef.GET_CARD_MAN_SCREEN;
import static com.linkly.libui.UIScreenDef.GET_CARD_MSR_ICC_CTLS_MAN_SCREEN;
import static com.linkly.libui.UIScreenDef.GET_CARD_MSR_ICC_CTLS_SCREEN;
import static com.linkly.libui.UIScreenDef.GET_CARD_MSR_ICC_MAN_SCREEN;
import static com.linkly.libui.UIScreenDef.GET_CARD_MSR_MAN_CTLS_SCREEN;
import static com.linkly.libui.UIScreenDef.GET_CARD_MSR_MAN_SCREEN;
import static com.linkly.libui.UIScreenDef.GET_CARD_MSR_SCREEN;
import static com.linkly.libui.UIScreenDef.MAG_CARD_READ_ERROR;
import static com.linkly.libui.UIScreenDef.READ_ERROR_REMOVE_CARD;
import static com.linkly.libui.UIScreenDef.TRANS_READING_CARD;
import static com.linkly.libui.UIScreenDef.ZERO_TRANS_AMOUNT;

import com.linkly.libconfig.uiconfig.ConfigTimeouts;
import com.linkly.libengine.action.IAction;
import com.linkly.libengine.action.InitialProcessing;
import com.linkly.libengine.action.TransactionCanceller;
import com.linkly.libengine.action.check.CheckUserLevel;
import com.linkly.libengine.debug.IDebug;
import com.linkly.libengine.dependencies.IDependency;
import com.linkly.libengine.engine.Engine;
import com.linkly.libengine.engine.cards.CardGetThread;
import com.linkly.libengine.engine.protocol.IProto;
import com.linkly.libengine.engine.transactions.TransRec;
import com.linkly.libengine.engine.transactions.properties.TCard;
import com.linkly.libengine.helpers.UIHelpers;
import com.linkly.libengine.overrides.CoreOverrides;
import com.linkly.libengine.status.IStatus;
import com.linkly.libmal.MalFactory;
import com.linkly.libmal.global.platform.EFTPlatform;
import com.linkly.libsecapp.IP2PCard;
import com.linkly.libsecapp.IP2PEncrypt;
import com.linkly.libsecapp.IP2PLib;
import com.linkly.libsecapp.P2PLib;
import com.linkly.libui.IUIDisplay;
import com.linkly.libui.UIScreenDef;
import com.linkly.libui.utils.WaitForUIResponseCallable;

import java.util.HashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import timber.log.Timber;

public class GetCard extends IAction {

    @Override
    public String getName() {
        return "GetCard";
    }


    @Override
    public void run() {
        // We have 2 threads as we have a UI response and the get card response
        ExecutorService executor = Executors.newFixedThreadPool(2);

        // Can loop back into getcard in which our card reader state is already running. (generally failed ctls reads) I.E pulled away too early
        // Want to make sure the card reader is reset. Calling secapp's "getCard" is stateful so the first timeout value passed through is stored..
        // This means subsequent calls fail in secapp.
        d.getP2PLib().getIP2PCard().cardGetCancel();
        // clear any surcharge amount. in case where surcharge was provided previously and we've come back to get card again, e.g. fallback or swiped icc card says insert
        // don't clear tip amount because this could come from the POS. Surcharge is always based on the type of card so added after presentation/identification
        trans.getAmounts().setSurcharge(0);

        //Check transaction total amount before displaying GetCard screen.
        if (!isTranTotalAmountValid()) {
            ui.showScreen(ZERO_TRANS_AMOUNT);
            d.getProtocol().setInternalRejectReason( trans, IProto.RejectReasonType.INVALID_AMOUNT );
            d.getWorkflowEngine().setNextAction(TransactionCanceller.class);
            return;
        }

        if (trans.getCard().getCardIndex() != -1) {
            Timber.i("We already have the card details read");
            trans.updateCardStateToMatchType(trans.getCard().getCardType());
            return;
        }

        int presentCardTime = super.d.getPayCfg().getUiConfigTimeouts().
                getTimeoutMilliSecs(
                        ConfigTimeouts.PRESENT_CARD_TIMEOUT,
                        super.trans.getAudit().isAccessMode()
                );

        HashMap<String, Object> map = new HashMap<>();

        IP2PCard iMalCard = d.getP2PLib().getIP2PCard();
        iMalCard.cardReset(true);
        trans.getCard().getLedStatus().setCTLSEnabled(trans.getCard().isCtlsAllowed());
        trans.getCard().setCaptureMethod(TCard.CaptureMethod.NOT_CAPTURED);

        // If we have an auto transaction need to save for power fail situations so on boot up we can notify any external devices
        if(trans.getTransType().autoTransaction) {
            trans.save();
        }

        d.getStatusReporter().reportStatusEvent(getStatusEventId(getScreenId(trans)), trans.isSuppressPosDialog());

        ui.showScreen(getScreenId(trans),map);

        // Perform our listening code and handle the responses
        // We get responses from both sides.
        Callable<IP2PCard.CardType> cardReaderResponse = new CardGetThread(presentCardTime, hasMsr(trans), trans.getCard().isEmvAllowed(), trans.getCard().isCtlsAllowed());
        // Removing the timeout section for the UI side. Rather than having double up's on timeouts, the card reader is the single point of control on when the logic for timeout happens.
        // Expectation for UI results are anything that results in user interaction.
        Callable<IUIDisplay.UIResultCode> uiResponse = new WaitForUIResponseCallable(ui, NO_TIMEOUT);

        Future<IP2PCard.CardType> cardTypeFuture = executor.submit(cardReaderResponse);
        Future<IUIDisplay.UIResultCode> uiResultCodeFuture = executor.submit(uiResponse);

        try {
            // Until either of these have completed or cardTypeFuture Times outs internally
            while(!cardTypeFuture.isDone() && !uiResultCodeFuture.isDone()) {
                Thread.sleep(100);
            }

            // Check the card response first.
            if(cardTypeFuture.isDone()) {
                uiResultCodeFuture.cancel(true);
                IP2PCard.CardType result = cardTypeFuture.get();

                processCardType(d, result);
            } else {
                // Assuming that the ui result has returned something.
                // Expectation is that returned user interaction means no card processing will be happening..
                cardTypeFuture.cancel(true);
                // we need to call clean up of p2pe instance get card
                P2PLib.getInstance().getIP2PCard().cardGetCancel();
                IUIDisplay.UIResultCode uiResultCode = uiResultCodeFuture.get();

                processUiResult(d, uiResultCode);
            }
        } catch (InterruptedException e) {
            Timber.e("Card Read Has been Unexpectedly Stopped");
            // Restore interrupted state...
            Thread.currentThread().interrupt();
            // Something horrible has happened here can just cancel transaction
            d.getWorkflowEngine().setNextAction(TransactionCanceller.class);
            d.getDebugReporter().reportCancelSelect( IDebug.DEBUG_POSITION.ENTER_CARD );

            // we need to call clean up of p2pe instance get card
            P2PLib.getInstance().getIP2PCard().cardGetCancel();
        } catch (Exception e) {
            // Something horrible has happened here can just cancel transaction
            Timber.e("Unexpected Error on Card Read Cancelling Transaction");
            d.getWorkflowEngine().setNextAction(TransactionCanceller.class);
            d.getDebugReporter().reportCancelSelect( IDebug.DEBUG_POSITION.ENTER_CARD );

            // we need to call clean up of p2pe instance get card
            P2PLib.getInstance().getIP2PCard().cardGetCancel();
        } finally {
            // Make sure our executor is shutdown
            executor.shutdown();
        }

    }

    private boolean isTranTotalAmountValid() {

        // check the transaction amount
        long totalAmount = trans.getAmounts().getTotalAmount();
        return totalAmount > 0;
    }

    private void processUiResult(IDependency d, IUIDisplay.UIResultCode uiResultCode) {
        Timber.e( "GetCardResult = %s", uiResultCode.toString() );

        if (uiResultCode == OK ) {
            String resultText = ui.getResultText(ACT_GET_CARD, IUIDisplay.uiResultText1);
            if (resultText.equals("VOUCHER")) {
                trans.getCard().setCaptureMethod(TCard.CaptureMethod.SCAN_VOUCHER);
            }
        } else if (uiResultCode == IUIDisplay.UIResultCode.MANUAL) {
            Timber.e("Card Read - Manual Card Read");
            manualCardGet();
        } else if (uiResultCode == TIMEOUT) {
            // Note we shouldn't actually get this response as timeouts are handled by the "get card" logic.
            d.getWorkflowEngine().setNextAction(TransactionCanceller.class);
            d.getDebugReporter().reportTimeout(IDebug.DEBUG_POSITION.ENTER_CARD);
            Timber.e("Card Read - Timeout");
            if (trans.getAmounts().getDiscountedAmount() > 0) {
                IProto iproto = d.getProtocol();
                iproto.discountVoucherReverse(trans);
            }
            d.getProtocol().setInternalRejectReason(trans, IProto.RejectReasonType.USER_TIMEOUT);
        } else if (uiResultCode == RESTART){
            Timber.e("Card Read - Restarting");
            d.getWorkflowEngine().setNextAction(GetCard.class);
        } else {
            Timber.e("Card Read - Cancelling");
            d.getWorkflowEngine().setNextAction(TransactionCanceller.class);
            d.getDebugReporter().reportCancelSelect(IDebug.DEBUG_POSITION.ENTER_CARD);
        }
    }

    /***
     * processes the card type
     * @param dependencies
     * @param cType
     */
    private void processCardType(IDependency dependencies, IP2PCard.CardType cType) {
        boolean cancelCardReader = false;

        Timber.i("P2PCard Detected: %s", cType.toString());

        switch(cType) {
            case CT_MSR -> updateCardStateToMatchType(trans, TCard.CardType.MSR);
            case CT_MSR_FAULTY -> {
                Timber.e("Card Read - Faulty MSR");
                cardMsrReadError(null);
                if (trans.checkMsrFallback(false, d.getPayCfg().isCardholderPresent())) {
                    manualCardGet();
                } else {
                    dependencies.getWorkflowEngine().setNextAction(InitialProcessing.class);
                }
                cancelCardReader = true;
            }
            case CT_EMV -> {
                ui.showScreen(TRANS_READING_CARD, trans.getTransType().displayId);
                updateCardStateToMatchType(trans, TCard.CardType.EMV);
            }
            case CT_EMV_FAULTY -> {
                Timber.e("Card Read - Faulty EMV");
                cardReadError(dependencies);
                if( !trans.checkFallback(dependencies, false) ) {
                    dependencies.getWorkflowEngine().setNextAction(TransactionCanceller.class);
                    d.getDebugReporter().reportTimeout(IDebug.DEBUG_POSITION.ENTER_CARD);
                } else {
                    dependencies.getWorkflowEngine().setNextAction(InitialProcessing.class);
                }

                cancelCardReader = true;
            }
            case CT_CTLS -> updateCardStateToMatchType(trans, TCard.CardType.CTLS);
            case CT_CTLS_FAULTY -> {
                Timber.e("Card Read - Faulty CTLS");
                ctlsReadError(dependencies);
                cancelCardReader = true;
            }
            // for CT_NONE - is returned for timeout.
            case CT_NONE, CT_TIMEOUT -> {
                d.getWorkflowEngine().setNextAction(TransactionCanceller.class);
                d.getDebugReporter().reportTimeout(IDebug.DEBUG_POSITION.ENTER_CARD);
                Timber.e("Card Read - CT_NONE or CT_TIMEOUT - accepting user timeout");
                if (trans.getAmounts().getDiscountedAmount() > 0) {
                    IProto iproto = d.getProtocol();
                    iproto.discountVoucherReverse(trans);
                }
                d.getProtocol().setInternalRejectReason(trans, IProto.RejectReasonType.USER_TIMEOUT);
                cancelCardReader = true;
            }
        }

        if(cancelCardReader) {
            Timber.e("Resetting Card Reader");
            // we need to call clean up of p2pe instance get card
            P2PLib.getInstance().getIP2PCard().cardGetCancel();
        }
    }

    private void updateCardStateToMatchType(TransRec trans, TCard.CardType cardType) {
        trans.getCard().setCardType(cardType);
        switch(cardType) {
            case MSR:
                updateCardState(trans, TCard.CaptureMethod.SWIPED, false);
                break;
            case EMV:
                updateCardState(trans, TCard.CaptureMethod.ICC, false);
                break;
            case CTLS:
                updateCardState(trans, TCard.CaptureMethod.CTLS, true);
                break;
            case MANUAL:
                updateCardState(trans, TCard.CaptureMethod.MANUAL, false);
                trans.getAudit().setReasonOnlineCode(RTIME_FORCED_CARD_ACCEPTOR);
                break;
            default:
                break;
        }
    }

    private void updateCardState(TransRec trans, TCard.CaptureMethod method, boolean ctls) {
        trans.getCard().setCaptureMethod(method);
        trans.getCard().getLedStatus().setCTLSEnabled(ctls);
        Timber.e("Card Capture Method : %s", trans.getCard().getCaptureMethod());
    }


    private void cardMsrReadError(TransRec trans) {
        if (trans == null || trans.getCard().isShowReadError()) {
            ui.showScreen(MAG_CARD_READ_ERROR);
            if (trans != null) {
                trans.getCard().setShowReadError(false);
            }
        }
    }

    private void cardReadError( IDependency d ) {
        UIHelpers.cardRemoveWithMessage(d, mal, READ_ERROR_REMOVE_CARD, true, false, trans);
    }

    private void manualCardGet() {
        if ((d.getPayCfg().getPasswordRequiredForAllCards(trans, TCard.CaptureMethod.MANUAL) ) &&
            (!CheckUserLevel.runUserLogin(d, ui))) {
            return;
        }

        IP2PLib p2pInstance = d.getP2PLib();
        p2pInstance.getIP2PEncrypt().clearData();

        d.getStatusReporter().reportStatusEvent(STATUS_UI_MANUAL_PAN_ACTIVITY , trans.isSuppressPosDialog());

        if (p2pInstance.getIP2PActivity().getManualPan(MAN_PAN_ENTRY_LEN_MIN, MAN_PAN_ENTRY_LEN_MAX, IUIDisplay.LONG_TIMEOUT, trans.getAmounts().getTotalAmount(), CSC_LEN_MIN, CSC_LEN_MAX, false)) {
            IP2PEncrypt p2pEncrypt = p2pInstance.getIP2PEncrypt();
            String maskedManPan = p2pEncrypt.getData(PAN);
            String expiry = p2pEncrypt.getData(EXPIRY_YYMM);
            String cvv = p2pEncrypt.getData(CVV);

            trans.updateCardStateToMatchType(TCard.CardType.MANUAL);
            trans.getCard().setPan(maskedManPan);
            trans.getCard().setMaskedPan(maskedManPan);
            trans.getCard().setExpiry(expiry);
            trans.getCard().setCvv(cvv);

            Timber.i("Masked PAN    = %s", maskedManPan);
            Timber.i("Expiry (YYMM) = %s", expiry);
            Timber.i("Masked CVV    = %s", cvv);
        } else {
            d.getDebugReporter().reportCancelSelect(IDebug.DEBUG_POSITION.ENTER_CARD);
            d.getWorkflowEngine().setNextAction(TransactionCanceller.class);
            //set action
            //todo this cancellation is not propagated properly
        }
    }

    private void ctlsReadError(IDependency dependencies) {
        UIHelpers.cardRemoveWithMessage( dependencies, mal, ERROR_PRESENT_ONE_CARD, false, true, trans);
        d.getWorkflowEngine().setNextAction(InitialProcessing.class);
    }

    private boolean hasMsr(TransRec trans) {
        return EFTPlatform.hasMSR() && !trans.isStartedInOfflineMode() && d.getPayCfg().isMsrAllowed() && trans.getCard().isMsrAllowed();
    }

    private UIScreenDef getScreenId(TransRec trans) {
        if (trans.getCard().getCvmType() == CDCVM)
            return GET_CARD_CDCVM_SCREEN;

        int entryMask = 0;
        final int MSR = 0x01;
        final int ICC = 0x02;
        final int CTLS = 0x04;
        final int MAN = 0x08;
        TCard cardInfo = trans.getCard();
        if (hasMsr(trans))
            entryMask |= MSR;
        if (cardInfo.isEmvAllowed())
            entryMask |= ICC;
        if (cardInfo.isCtlsAllowed())
            entryMask |= CTLS;


        switch (entryMask) {
            case MAN:                       return GET_CARD_MAN_SCREEN;
            case ICC:                       return GET_CARD_ICC_SCREEN;
            case CTLS:                      return GET_CARD_CTLS_SCREEN;
            case MSR:                       return GET_CARD_MSR_SCREEN;
            case ICC | MAN:                 return GET_CARD_ICC_MAN_SCREEN;
            case ICC | CTLS:                return GET_CARD_ICC_CTLS_SCREEN;
            case ICC | MSR:                 return GET_CARD_ICC_MSR_SCREEN;
            case CTLS | MAN:                return GET_CARD_CTLS_MAN_SCREEN;
            case CTLS | MSR:                return GET_CARD_CTLS_MSR_SCREEN;
            case MSR | MAN:                 return GET_CARD_MSR_MAN_SCREEN;
            case MSR | ICC | MAN:           return GET_CARD_MSR_ICC_MAN_SCREEN;
            case MSR | ICC | CTLS:          return GET_CARD_MSR_ICC_CTLS_SCREEN;
            case MSR | MAN | CTLS:          return GET_CARD_MSR_MAN_CTLS_SCREEN;
            case MAN | CTLS | ICC:          return GET_CARD_MAN_CTLS_ICC_SCREEN;
            case MSR | ICC | CTLS | MAN:    // Deliberate fallthrough
            default:                        return GET_CARD_MSR_ICC_CTLS_MAN_SCREEN;
        }
    }

    private IStatus.STATUS_EVENT getStatusEventId(UIScreenDef screenDef) {
        switch (screenDef) {
            case GET_CARD_MAN_SCREEN:           return STATUS_UI_GETCARD_MAN;
            case GET_CARD_ICC_SCREEN:           return STATUS_UI_GETCARD_ICC;
            case GET_CARD_CTLS_SCREEN:          return STATUS_UI_GETCARD_CTLS;
            case GET_CARD_MSR_SCREEN:           return STATUS_UI_GETCARD_MSR;
            case GET_CARD_ICC_MAN_SCREEN:       return STATUS_UI_GETCARD_ICC_MAN;
            case GET_CARD_ICC_CTLS_SCREEN:      return STATUS_UI_GETCARD_ICC_CTLS;
            case GET_CARD_ICC_MSR_SCREEN:       return STATUS_UI_GETCARD_ICC_MSR;
            case GET_CARD_CTLS_MAN_SCREEN:      return STATUS_UI_GETCARD_CTLS_MAN;
            case GET_CARD_CTLS_MSR_SCREEN:      return STATUS_UI_GETCARD_CTLS_MSR;
            case GET_CARD_MSR_MAN_SCREEN:       return STATUS_UI_GETCARD_MSR_MAN;
            case GET_CARD_MSR_ICC_MAN_SCREEN:   return STATUS_UI_GETCARD_MSR_ICC_MAN;
            case GET_CARD_MSR_ICC_CTLS_SCREEN:  return STATUS_UI_GETCARD_MSR_ICC_CTLS;
            case GET_CARD_MSR_MAN_CTLS_SCREEN:  return STATUS_UI_GETCARD_MSR_MAN_CTLS;
            case GET_CARD_MAN_CTLS_ICC_SCREEN:  return STATUS_UI_GETCARD_MAN_CTLS_ICC;
            case GET_CARD_CDCVM_SCREEN:    // Deliberate fallthrough
            default:                            return STATUS_UI_GETCARD;
        }
    }



}
