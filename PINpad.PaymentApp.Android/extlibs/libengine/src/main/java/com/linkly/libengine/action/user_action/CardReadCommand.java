package com.linkly.libengine.action.user_action;

import static com.linkly.libengine.engine.transactions.properties.TProtocol.ACC_TYPE_CHEQUE;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.ACC_TYPE_CREDIT;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.ACC_TYPE_DEFAULT;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.ACC_TYPE_SAVINGS;
import static com.linkly.libengine.helpers.ECRHelpers.maskIfFinancial;
import static com.linkly.libengine.status.IStatus.STATUS_EVENT.STATUS_TRANS_DECLINED;
import static com.linkly.libengine.status.IStatus.STATUS_EVENT.STATUS_TRANS_FINISHED;
import static com.linkly.libengine.status.IStatus.STATUS_EVENT.STATUS_UI_ACCOUNT_SELECTION;
import static com.linkly.libengine.status.IStatus.STATUS_EVENT.STATUS_UI_GETCARD;
import static com.linkly.libsecapp.IP2PEncrypt.ElementType.TRACK_1_FULL_CHIP;
import static com.linkly.libsecapp.IP2PEncrypt.ElementType.TRACK_2_FULL_CHIP;
import static com.linkly.libsecapp.emv.Tag.track2_eq_data;
import static com.linkly.libsecapp.emv.Tag.track_1_equiv_data;
import static com.linkly.libsecapp.emv.Tag.track_2_equiv_data;
import static com.linkly.libui.IUIDisplay.NO_TIMEOUT;
import static com.linkly.libui.UIScreenDef.CARD_READ_CONTACTLESS_COMMAND_SUCCESSFUL;
import static com.linkly.libui.UIScreenDef.CARD_READ_EMV_COMMAND_SUCCESSFUL;
import static com.linkly.libui.UIScreenDef.CARD_READ_MAGSTRIPE_COMMAND_SUCCESSFUL;
import static com.linkly.libui.UIScreenDef.CARD_TYPE_NOT_READ;
import static com.linkly.libui.UIScreenDef.ERROR_PRESENT_ONE_CARD;
import static com.linkly.libui.UIScreenDef.MAG_CARD_READ_ERROR;
import static com.linkly.libui.UIScreenDef.READ_ERROR_REMOVE_CARD;
import static com.linkly.libui.UIScreenDef.SELECT_ACCOUNT;
import static com.linkly.libui.UIScreenDef.TRANS_CANCELLED;

import com.linkly.libengine.action.IAction;
import com.linkly.libengine.engine.EngineManager;
import com.linkly.libengine.engine.cards.CardGetThread;
import com.linkly.libengine.engine.cards.EmvListener;
import com.linkly.libengine.engine.transactions.TransRec;
import com.linkly.libengine.engine.transactions.properties.TAmounts;
import com.linkly.libengine.helpers.ECRHelpers;
import com.linkly.libengine.helpers.UIHelpers;
import com.linkly.libmal.global.util.Util;
import com.linkly.libpositive.events.PositiveReadCardEvent;
import com.linkly.libpositive.wrappers.PositiveReadCardResult;
import com.linkly.libpositive.wrappers.TagDataFromPOS;
import com.linkly.libpositive.wrappers.TagDataToPOS;
import com.linkly.libsecapp.IP2PCard;
import com.linkly.libsecapp.IP2PCtls;
import com.linkly.libsecapp.IP2PEMV;
import com.linkly.libsecapp.IP2PEncrypt;
import com.linkly.libsecapp.P2PLib;
import com.linkly.libui.IUIDisplay;
import com.linkly.libui.UIScreenDef;
import com.linkly.libui.display.DisplayQuestion;
import com.linkly.libui.utils.WaitForUIResponseCallable;
import com.pax.dal.entity.TrackData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import timber.log.Timber;

public class CardReadCommand extends IAction {

    private final PositiveReadCardResult positiveReadCardResult = new PositiveReadCardResult();
    private final PositiveReadCardEvent positiveReadCardEvent;
    private static final String APPROVED = "APPROVED";
    boolean suppressPosDialog = false;
    boolean cardRead = false;

    public CardReadCommand( PositiveReadCardEvent positiveReadCardEvent ) {
        this.positiveReadCardEvent = positiveReadCardEvent;
        prePopulateResponse();
    }

    private void prePopulateResponse() {
        if (positiveReadCardEvent != null) {
            positiveReadCardResult.setDeviceCode(positiveReadCardEvent.getDeviceCode());
            positiveReadCardResult.setSubCode(positiveReadCardEvent.getSubCode());
        }
    }

    @Override
    public String getName() {
        return "CardReadCommand";
    }

    @Override
    public void run() {
        if( validateInput() ) {
            showScreen();
            sendCardReadResponse();

            // we need to call clean up of p2pe instance get card
            P2PLib.getInstance().getIP2PCard().cardGetCancel();
        }
    }

    private boolean validateInput() {

        if (positiveReadCardEvent.getPadTagJson() != null) {
            TagDataFromPOS tagDataFromPos = TagDataFromPOS.builder(positiveReadCardEvent.getPadTagJson());

            if (tagDataFromPos != null && tagDataFromPos.getPAT() != null && tagDataFromPos.getPAT().equals("2")) {
                suppressPosDialog = true;
            }
        }

        // refer to linkly terminal developers spec for values
        if( positiveReadCardEvent.getSubCode() != null ) {
            switch (positiveReadCardEvent.getSubCode()) {
                case "0": // read card only
                case "2": // read card and get account
                case "5": // get account only
                    // options to use will be contained in the boolean flags readCard and getAccount in PositiveReadCardEvent object
                    return true;

                case "3": // read card, no reply to POS
                case "4": // read card and get account, no reply to POS
                case "6": // undefined
                case "7": // custom woolworths pre-swipe
                case "8": // custom woolworths loyalty
                case "9": // Custom WW “Deposit” (No Response to this command)
                case "S": // Custom command for Bunnings
                case "A": // Account Verify. When performing an Account Verify the terminal must return the RFN tag as this is how solutions create tokens for cardholders for use in subsequent transactions.
                default:
                    // UNSUPPORTED
                    Timber.e("Unsupported read card subCode type %s", positiveReadCardEvent.getSubCode());
                    break;
            }
        }

        // else return B2, unsupported command response
        positiveReadCardResult.setResponseCode( "B2" );
        positiveReadCardResult.setResponseText( "UNSUPPORTED FUNCTION" );
        sendCardReadResponse(); // send back to POS

        // Finish activity
        d.getStatusReporter().reportStatusEvent( STATUS_TRANS_FINISHED , suppressPosDialog );

        return false;
    }

    private void showScreen() {
        // reset current trans state info
        d.resetCurrentTransaction(null);

        if (positiveReadCardEvent.isReadCard()) {
            cardRead = getCard();
            // Only prompt account if card read was successful
            if (cardRead && positiveReadCardEvent.isGetAccount()) {
                if (d.getCurrentTransaction() == null ||
                        (!(d.getCurrentTransaction().getProtocol().getAccountType() != 0 && d.getCurrentTransaction().getCard().isAppSelected()))) {
                    getAccount();
                } else {
                    applySelectedAccount(d.getCurrentTransaction().getProtocol().getAccountType());
                }
            }
        } else if (positiveReadCardEvent.isGetAccount()) {// Get account only option
            getAccount();
        }

        // Finish activity
        d.getStatusReporter().reportStatusEvent( STATUS_TRANS_FINISHED , suppressPosDialog);
    }

    private void applySelectedAccount(Integer accountType) {
        switch (accountType) {
            case ACC_TYPE_SAVINGS:
                positiveReadCardResult.setAccountSelected("0");
                break;
            case ACC_TYPE_CHEQUE:
                positiveReadCardResult.setAccountSelected("1");
                break;
            case ACC_TYPE_CREDIT:
            case ACC_TYPE_DEFAULT:
            default:
                positiveReadCardResult.setAccountSelected("2");
                break;
        }
    }

    private boolean getCard() {
        boolean done = false;
        cardRead = false;
        int retries = 0;

        while (!done) {
            // We have 2 threads as we have a UI response and the get card response
            ExecutorService executor = Executors.newFixedThreadPool(2);
            IP2PCard iMalCard = d.getP2PLib().getIP2PCard();
            iMalCard.cardReset(true);
            d.getStatusReporter().reportStatusEvent(STATUS_UI_GETCARD , suppressPosDialog);

            int presentCardTime =  60 * 1000;

            HashMap<String, Object> map = new HashMap<>();
            // we need to override the default timeout for get card from 0 to 60 seconds.
            map.put(IUIDisplay.uiPresentCardTimeout, presentCardTime);
            // GET_CARD maps to the FragGetCard.java, in a different thread. This is where the card read happens.
            ui.showScreen(UIScreenDef.GET_CARD_MSR_ICC_CTLS_SCREEN, map);

            // Perform our listening code and handle the responses
            // We get responses from both sides.
            Callable<IP2PCard.CardType> cardReaderResponse = new CardGetThread(presentCardTime, true, true, true);
            // Removing the timeout section for the UI side. Rather than having double up's on timeouts, the card reader is the single point of control on when the logic for timeout happens.
            // Expectation for UI results are anything that results in user interaction.
            Callable<IUIDisplay.UIResultCode> uiResponse = new WaitForUIResponseCallable(ui, NO_TIMEOUT);

            Future<IP2PCard.CardType> cardTypeFuture = executor.submit(cardReaderResponse);
            Future<IUIDisplay.UIResultCode> uiResultCodeFuture = executor.submit(uiResponse);

            // Also need to match the time of the present card screen.
            try {
                // Until either of these have completed or cardTypeFuture Times outs internally
                while(!cardTypeFuture.isDone() && !uiResultCodeFuture.isDone()) {
                    Thread.sleep(100);
                }

                // Check the card response first.
                if(cardTypeFuture.isDone()) {
                    uiResultCodeFuture.cancel(true);
                    IP2PCard.CardType result = cardTypeFuture.get();

                    processCardType(result);

                    if(isFaultyRead(result)) {
                        ui.showScreen(CARD_TYPE_NOT_READ);
                        retries++;
                    } else {
                        // 2 things, valid read or timeout
                        if(result != IP2PCard.CardType.CT_NONE && result != IP2PCard.CardType.CT_TIMEOUT) {
                            // We have read something valid
                            cardRead = true;
                        }

                        done = true;
                    }
                } else {
                    // Assuming that the ui result has returned something.
                    // Expectation is that returned user interaction means no card processing will be happening..
                    cardTypeFuture.cancel(true);

                    IUIDisplay.UIResultCode uiResultCode = uiResultCodeFuture.get();

                    if ( IUIDisplay.UIResultCode.ABORT == uiResultCode ) {
                        Timber.d( "Aborting Card Read, assuming cancel pressed" );
                        done = true;
                    } else {
                        // This shouldnt really happen
                        // Any other issue, throw an error screen/warning screen & try again
                        Timber.d("uiResultCode = %s", uiResultCode.toString());
                        ui.showScreen(CARD_TYPE_NOT_READ);
                        retries++;
                    }
                }
            } catch (InterruptedException e) {
                Timber.e("Card Read Has been Unexpectedly Stopped");
                // Restore interrupted state...
                Thread.currentThread().interrupt();
                done = true;
                packCancelledResponse();

                // we need to call clean up of p2pe instance get card
                P2PLib.getInstance().getIP2PCard().cardGetCancel();
            } catch (Exception e) {
                done = true;

                // we need to call clean up of p2pe instance get card
                P2PLib.getInstance().getIP2PCard().cardGetCancel();
                // Something horrible has happened here can just cancel transaction
                packCancelledResponse();
            } finally {
                // Make sure our executor is shutdown
                executor.shutdown();
            }

            if ( retries >= 3 ) {
                done = true;
            }
        }

        if (!cardRead) {
            // we need to call clean up of p2pe instance get card
            P2PLib.getInstance().getIP2PCard().cardGetCancel();
            Timber.d("No/failed Card Response");
            packCancelledResponse();
        }

        return cardRead;
    }

    boolean isFaultyRead(IP2PCard.CardType cardType) {
        return switch (cardType) {
            case CT_MSR_FAULTY, CT_EMV_FAULTY, CT_CTLS_FAULTY -> true;
            default -> false;
        };
    }

    private void processCardType(IP2PCard.CardType cardType) {

        Timber.d("Card Read has completed - Result: %s", cardType.name());

        switch (cardType) {
            case CT_MSR:
                processMsrCardRead();
                break;
            case CT_MSR_FAULTY:
                ui.showScreen(MAG_CARD_READ_ERROR);
                break;
            case CT_EMV:
                processEmvCardRead();
                break;
            case CT_EMV_FAULTY:
                UIHelpers.cardRemoveWithMessage(d, mal, READ_ERROR_REMOVE_CARD, true, false, null);
                break;
            case CT_CTLS:
                processCtlsCardRead();
                break;
            case CT_CTLS_FAULTY:
                UIHelpers.cardRemoveWithMessage(d, mal, ERROR_PRESENT_ONE_CARD, false, true, null);
                break;
            default:
                break;
        }
    }

    private void processMsrCardRead() {
        // retrieve mag stripe read track data from Sec App. Note this may be whitelisted + full PAN digits, otherwise will be masked format
        TrackData trackData = Objects.requireNonNull(d.getP2PLib()).getIP2PMsr().readFromLastResult();
        if (trackData != null) {
            Timber.d("track1Data = %s", trackData.getTrack1());
            Timber.d("track2Data = %s", trackData.getTrack2());
            Timber.d("track3Data = %s", trackData.getTrack3());
            setTrack1Data(!trackData.getTrack2().isEmpty() ?
                    trackData.getTrack1() :
                    trackData.getTrack3());
            // format the masked track2 data
            String track2 = trackData.getTrack2();
            setTrack2Data(track2);
            setBinNumber(trans, track2);
            setApprovedCardRead();
            positiveReadCardResult.getTagDataToPOS().setCem(TagDataToPOS.CardEntryModeTag.SWIPE);
            d.getDebugReporter().reportCardData(TagDataToPOS.CardEntryModeTag.SWIPE, track2);
            ui.showScreen(CARD_READ_MAGSTRIPE_COMMAND_SUCCESSFUL);
            cardRead = true;
        } else {
            packCancelledResponse();
        }
    }

    private void processEmvCardRead() {
        IP2PEncrypt ip2PEncryptEmv = d.getP2PLib().getIP2PEncrypt();
        TransRec transRec = new TransRec(EngineManager.TransType.REFUND_AUTO, d);
        transRec.setAmounts(new TAmounts(d.getPayCfg()));
        transRec.setSuppressPosDialog(false);
        d.resetCurrentTransaction(transRec);

        IP2PEMV iEmv = Objects.requireNonNull(d.getP2PLib()).getIP2PEmv();

        byte[] countryCode = Util.str2Bcd(transRec.getAudit().getCountryCode());
        byte[] transCurrCode = Util.str2Bcd(transRec.getAmounts().getCurrency());

        byte[] transType = Util.hexStringToByteArray(d.getProtocol().getEmvProcessingCode(transRec));
        iEmv.emvInit(new EmvListener(d), countryCode, transCurrCode, transType[0], false, transRec.isStartedInOfflineMode());
        iEmv.emvStart();

        byte[] track1 = iEmv.emvGetTag(track_1_equiv_data);
        if (track1 == null || track1.length <= 0) {
            setTrack1Data(ip2PEncryptEmv.getMaskedData(TRACK_1_FULL_CHIP));
        }

        byte[] track2BcdFormat = iEmv.emvGetTag(track2_eq_data, true);
        // if we got some data back, get it again via the p2pencrypt method
        if (track2BcdFormat != null && track2BcdFormat.length > 0) {
            // get it again using * masking chars
            // Note: the masked '*' character will be converted to '0' by the connect app before sending the message to POS
            String track2 = ip2PEncryptEmv.getMaskedData(TRACK_2_FULL_CHIP);
            if (track2 != null && !track2.isEmpty()) {
                setTrack2Data(track2); // format the masked track2 data as per Linkly spec for query card response
                setBinNumber(transRec, track2);
                setApprovedCardRead();
                positiveReadCardResult.getTagDataToPOS().setCem(TagDataToPOS.CardEntryModeTag.CHIP_CARD);
                d.getDebugReporter().reportCardData(TagDataToPOS.CardEntryModeTag.CHIP_CARD, track2);
                ui.showScreen(CARD_READ_EMV_COMMAND_SUCCESSFUL);
                cardRead = true;
            }
        }  else {
            packCancelledResponse();
        }
    }

    private void processCtlsCardRead() {
        IP2PEncrypt ip2PEncrypt = d.getP2PLib().getIP2PEncrypt();
        TransRec trans = new TransRec(EngineManager.TransType.REFUND_AUTO, d);
        IP2PCtls iCtls = Objects.requireNonNull(d.getP2PLib()).getIP2PCtls();

        byte[] aucTransDate = Util.hexStringToByteArray(trans.getAudit().getTransDateTimeAsString("yyMMdd"));
        byte[] aucTransTime = Util.hexStringToByteArray(trans.getAudit().getTransDateTimeAsString("hhmmss"));
        long maxCtlsAmount = iCtls.ctlsGetMaxAmount(trans.isStartedInOfflineMode());

        byte ucTransType = 0x20; // setting to refund for Pan retrieval
        byte[] countryCode = Util.str2Bcd(trans.getAudit().getCountryCode());
        byte[] transCurrCode = Util.str2Bcd(trans.getAmounts().getCurrency());

        IP2PCtls.P2P_CTLS_ERROR eRet = iCtls.ctlsGenAC1(0,0,countryCode,transCurrCode,ucTransType,aucTransDate,aucTransTime,maxCtlsAmount,trans.getCard().isResetCvmLimit(),
                false,0,0,trans.isStartedInOfflineMode());
        trans.getCard().setCtlsResultCode(eRet);

        byte[] track1 = iCtls.ctlsGetTag(track_1_equiv_data);
        if (track1 == null || track1.length <= 0) {
            setTrack1Data(ip2PEncrypt.getMaskedData(TRACK_1_FULL_CHIP));
        }
        byte[] track2 = iCtls.ctlsGetTag(track2_eq_data);
        if(track2!=null) {
            Timber.i( "using tag 57");
        }
        if (track2 == null || track2.length <= 0) {
            track2 = iCtls.ctlsGetTag(track_2_equiv_data);
            if(track2!=null) {
                Timber.i( "using tag 9f6b");
            }
        }

        if (track2 != null && track2.length > 0) {
            // get it again using * masking chars
            // Note: the masked '*' character will be converted to '0' by the connect app before sending the message to POS
            String track2Str = ip2PEncrypt.getMaskedData(TRACK_2_FULL_CHIP);
            setTrack2Data(track2Str); // format the masked track2 data as per Linkly spec for query card response
            setBinNumber(trans, track2Str);
            setApprovedCardRead();
            positiveReadCardResult.getTagDataToPOS().setCem(TagDataToPOS.CardEntryModeTag.CONTACTLESS);
            d.getDebugReporter().reportCardData(TagDataToPOS.CardEntryModeTag.CONTACTLESS, track2Str);
            ui.showScreen(CARD_READ_CONTACTLESS_COMMAND_SUCCESSFUL);
            cardRead = true;
        } else {
            packCancelledResponse();
        }
    }

    private void setTrack1Data(String track1Str) {
        if (track1Str != null && !track1Str.isEmpty()) {
            positiveReadCardResult.setTrack1Present(true);
            positiveReadCardResult.setTrack1Or3Data(maskIfFinancial(d.getPayCfg(), track1Str, d.getConfig().getBinRangesCfg()));
        }
    }

    private void setTrack2Data(String track2Str) {
        if (track2Str != null && !track2Str.isEmpty()) {
            positiveReadCardResult.setTrack2Present(true);
            positiveReadCardResult.setTrack2Data(maskIfFinancial(d.getPayCfg(), track2Str, d.getConfig().getBinRangesCfg()));
        }
    }

    private void setBinNumber(TransRec trans, String track2) {
        trans.getCard().updateTrack2(d, track2, trans);
        Timber.d("binNumber = %d", trans.getCard().getBinNumber(d.getPayCfg()));
        positiveReadCardResult.setBinNumber(Integer.toString(trans.getCard().getBinNumber(d.getPayCfg())));
    }

    private void setApprovedCardRead() {
        positiveReadCardResult.setResponseCode("00");
        positiveReadCardResult.setResponseText(APPROVED);
    }

    private void getAccount() {
        HashMap<String, Object> map = new HashMap<>();
        ArrayList<DisplayQuestion> options = new ArrayList<>();
        final IUIDisplay.ACTIVITY_ID id = UIScreenDef.valueOf( SELECT_ACCOUNT.name() ).id;

        // report 'waiting for account selection' status
        d.getStatusReporter().reportStatusEvent(STATUS_UI_ACCOUNT_SELECTION , suppressPosDialog);

        map.put( IUIDisplay.uiScreenFragType, IUIDisplay.FRAG_TYPE.FRAG_GRID );

        options.add( new DisplayQuestion( IUIDisplay.String_id.STR_CHEQUE, "1", DisplayQuestion.EButtonStyle.BTN_STYLE_PRIMARY_DEFAULT_DOUBLE ) );
        options.add( new DisplayQuestion( IUIDisplay.String_id.STR_SAVINGS, "0", DisplayQuestion.EButtonStyle.BTN_STYLE_PRIMARY_DEFAULT_DOUBLE ) );
        options.add( new DisplayQuestion( IUIDisplay.String_id.STR_CREDIT, "2", DisplayQuestion.EButtonStyle.BTN_STYLE_PRIMARY_DEFAULT_DOUBLE ) );
        options.add( new DisplayQuestion( IUIDisplay.String_id.STR_CANCEL, "C", DisplayQuestion.EButtonStyle.BTN_STYLE_TRANSPARENT_DOUBLE ) );
        map.put( IUIDisplay.uiScreenOptionList, options );
        map.put( IUIDisplay.uiTitleId, IUIDisplay.String_id.STR_ACCOUNT_TYPE );

        ui.showScreen( UIScreenDef.SELECT_ACCOUNT, map );
        IUIDisplay.UIResultCode response = ui.getResultCode(id, IUIDisplay.LONG_TIMEOUT );
        if ( IUIDisplay.UIResultCode.OK == response ) {
            String result = ui.getResultText(id, IUIDisplay.uiResultText1 );

            Timber.d( "ACCOUNT SELECTED = %s", result );
            if ( result != null && result.equals( "C" ) ) {
                packCancelledResponse();
            } else {
                positiveReadCardResult.setAccountSelected( result );
                setApprovedCardRead();
            }
        } else if( response == IUIDisplay.UIResultCode.ABORT || response ==  IUIDisplay.UIResultCode.TIMEOUT ) {
            packCancelledResponse();
        }
    }

    private void packCancelledResponse() {
        ui.showScreen(TRANS_CANCELLED);
        positiveReadCardResult.setResponseCode( "DECLINED" );
        positiveReadCardResult.setResponseCode( "ZZ" );
        d.getStatusReporter().reportStatusEvent( STATUS_TRANS_DECLINED, suppressPosDialog );
    }

    private void sendCardReadResponse() {
        ECRHelpers.ipcSendCardReadResponse( d, this.positiveReadCardResult, context );
    }
}
