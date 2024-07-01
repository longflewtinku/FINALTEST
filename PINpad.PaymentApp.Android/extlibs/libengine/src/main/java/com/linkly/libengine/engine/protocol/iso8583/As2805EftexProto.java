package com.linkly.libengine.engine.protocol.iso8583;

import static com.linkly.libengine.engine.EngineManager.TransType.AUTO_LOGON;
import static com.linkly.libengine.engine.EngineManager.TransType.LOGON;
import static com.linkly.libengine.engine.EngineManager.TransType.PREAUTH_AUTO;
import static com.linkly.libengine.engine.EngineManager.TransType.PREAUTH_MOTO;
import static com.linkly.libengine.engine.EngineManager.TransType.PREAUTH_MOTO_AUTO;
import static com.linkly.libengine.engine.protocol.IProto.ProtoResult.PROTO_FAIL;
import static com.linkly.libengine.engine.protocol.IProto.ProtoResult.PROTO_SUCCESS;
import static com.linkly.libengine.engine.protocol.iso8583.As2805EftexPack.MsgType.ADVICE;
import static com.linkly.libengine.engine.protocol.iso8583.As2805EftexPack.MsgType.AUTH;
import static com.linkly.libengine.engine.protocol.iso8583.As2805EftexPack.MsgType.DEFERRED_AUTH;
import static com.linkly.libengine.engine.protocol.iso8583.As2805EftexPack.MsgType.NETWORK;
import static com.linkly.libengine.engine.protocol.iso8583.As2805EftexPack.MsgType.RECONCILIATION;
import static com.linkly.libengine.engine.protocol.iso8583.As2805EftexPack.MsgType.REVERSAL;
import static com.linkly.libengine.engine.protocol.iso8583.As2805EftexPack.RKI_REQUEST_FINAL;
import static com.linkly.libengine.engine.protocol.iso8583.As2805EftexPack.RKI_REQUEST_INITIAL;
import static com.linkly.libengine.engine.protocol.iso8583.As2805EftexPack.RKI_REQUEST_SECOND;
import static com.linkly.libengine.engine.protocol.iso8583.As2805EftexPack.SESSION_KEY_EXCHANGE;
import static com.linkly.libengine.engine.transactions.properties.TCard.CvmType.NO_CVM;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.AuthMethod.OFFLINE_NO_COMMS_ATTEMPTED;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.AuthMethod.OFFLINE_POSTCOMMS_AUTHORISED;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.AuthMethod.OFFLINE_PRECOMMS_AUTHORISED;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.AuthMethod.ONLINE_AUTHORISED;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.HostResult.AUTHORISED;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.HostResult.CONNECT_FAILED;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.HostResult.DECLINED;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.HostResult.NOT_SET;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.HostResult.NO_RESPONSE;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.HostResult.RECONCILED_OFFLINE_HOST_CUTOVER;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.MessageStatus;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.MessageStatus.ADVICE_QUEUED;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.MessageStatus.AUTH_SENT;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.MessageStatus.FINALISED;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.MessageStatus.FINALISED_AND_REVERSED;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.MessageStatus.REC_QUEUED;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.MessageStatus.REVERSAL_QUEUED;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.MessageStatus.WAITING_FOR_FINISH;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.ReversalReason.COMMS_FAIL;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.ReversalReason.CUSTOMER_CANCELLATION;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.ReversalReason.SIGFAIL;
import static com.linkly.libengine.env.AS2805LogonState.LogonState.KEK_LOGON;
import static com.linkly.libengine.env.AS2805LogonState.LogonState.KTM_LOGON;
import static com.linkly.libengine.env.AS2805LogonState.LogonState.LOGGED_ON;
import static com.linkly.libengine.env.AS2805LogonState.LogonState.LOGON_REQUIRED;
import static com.linkly.libengine.env.AS2805LogonState.LogonState.RSA_LOGON;
import static com.linkly.libengine.env.AS2805LogonState.LogonState.SESSION_KEY_LOGON;
import static com.linkly.libengine.env.SettlementDate.isSettlementDateChanged;
import static com.linkly.libmal.global.util.Util.isNullOrEmpty;
import static com.linkly.libpositive.messages.IMessages.BATCH_UPLOAD_EVENT;
import static com.linkly.libpositive.wrappers.PositiveTransResult.JournalType.NONE;
import static com.linkly.libsecapp.IP2PEncrypt.EncryptForStorageType.FULL_TRACK_FORMAT;
import static com.linkly.libsecapp.emv.Tag.aid;
import static com.linkly.libsecapp.emv.Tag.amt_auth_num;
import static com.linkly.libsecapp.emv.Tag.amt_other_num;
import static com.linkly.libsecapp.emv.Tag.app_curr_code;
import static com.linkly.libsecapp.emv.Tag.appl_cryptogram;
import static com.linkly.libsecapp.emv.Tag.appl_cryptogram_genAc2;
import static com.linkly.libsecapp.emv.Tag.appl_intchg_profile;
import static com.linkly.libsecapp.emv.Tag.appl_pan_seqnum;
import static com.linkly.libsecapp.emv.Tag.atc;
import static com.linkly.libsecapp.emv.Tag.auth_resp_code;
import static com.linkly.libsecapp.emv.Tag.crypt_info_data;
import static com.linkly.libsecapp.emv.Tag.crypt_info_data_genAc2;
import static com.linkly.libsecapp.emv.Tag.cvm_results;
import static com.linkly.libsecapp.emv.Tag.df_name;
import static com.linkly.libsecapp.emv.Tag.eftpos_payment_account_reference;
import static com.linkly.libsecapp.emv.Tag.eftpos_token_requestor_id;
import static com.linkly.libsecapp.emv.Tag.issuer_app_data;
import static com.linkly.libsecapp.emv.Tag.issuer_country_code;
import static com.linkly.libsecapp.emv.Tag.issuer_script_results;
import static com.linkly.libsecapp.emv.Tag.issuer_script_results2;
import static com.linkly.libsecapp.emv.Tag.term_cap;
import static com.linkly.libsecapp.emv.Tag.term_county_code;
import static com.linkly.libsecapp.emv.Tag.term_type;
import static com.linkly.libsecapp.emv.Tag.third_party_data;
import static com.linkly.libsecapp.emv.Tag.tran_date;
import static com.linkly.libsecapp.emv.Tag.tran_type;
import static com.linkly.libsecapp.emv.Tag.trans_category_code;
import static com.linkly.libsecapp.emv.Tag.trans_curcy_code;
import static com.linkly.libsecapp.emv.Tag.tsi;
import static com.linkly.libsecapp.emv.Tag.tvr;
import static com.linkly.libsecapp.emv.Tag.unpred_num;
import static com.linkly.libsecapp.emv.Tag.visa_ttq;
import static com.linkly.libui.UIScreenDef.COMMS_FAILURE_RECEIVING_RESPONSE;
import static com.linkly.libui.UIScreenDef.CONNECTING_TO_HOST_1_PLEASE_WAIT;
import static com.linkly.libui.UIScreenDef.CONNECTING_TO_HOST_2_PLEASE_WAIT;
import static com.linkly.libui.UIScreenDef.CONNECTING_TO_HOST_PLEASE_WAIT;
import static com.linkly.libui.UIScreenDef.CONNECTING_TO_HOST_SENDING_MSG;
import static com.linkly.libui.UIScreenDef.CONNECTING_TO_HOST_WAITING_FOR_RESPONSE;
import static com.linkly.libui.UIScreenDef.LOGON_FAILED;

import com.linkly.libengine.debug.IDebug;
import com.linkly.libengine.dependencies.IDependency;
import com.linkly.libengine.engine.Engine;
import com.linkly.libengine.engine.EngineManager;
import com.linkly.libengine.engine.comms.IComms;
import com.linkly.libengine.engine.comms.IpGatewayProxyComms;
import com.linkly.libengine.engine.protocol.IProto;
import com.linkly.libengine.engine.protocol.iso8583.openisoj.ProcessingCode;
import com.linkly.libengine.engine.protocol.svfe.SvfeUtils;
import com.linkly.libengine.engine.reporting.DailyBatch;
import com.linkly.libengine.engine.reporting.IDailyBatch;
import com.linkly.libengine.engine.reporting.Reconciliation;
import com.linkly.libengine.engine.transactions.TransRec;
import com.linkly.libengine.engine.transactions.TransRecManager;
import com.linkly.libengine.engine.transactions.properties.TCard;
import com.linkly.libengine.engine.transactions.properties.TDeferredAuth;
import com.linkly.libengine.engine.transactions.properties.TProtocol;
import com.linkly.libengine.env.AS2805LogonState;
import com.linkly.libengine.env.BatchNumber;
import com.linkly.libengine.env.ItemNumber;
import com.linkly.libengine.env.LastLogonTime;
import com.linkly.libengine.env.ReceiptNumber;
import com.linkly.libengine.env.SettlementDate;
import com.linkly.libengine.env.Stan;
import com.linkly.libengine.env.TxnsNoReponse;
import com.linkly.libengine.env.TxnsSinceLogon;
import com.linkly.libengine.helpers.ECRHelpers;
import com.linkly.libengine.jobs.EFTJobScheduleEvent;
import com.linkly.libengine.overrides.CoreOverrides;
import com.linkly.libengine.status.IStatus;
import com.linkly.libmal.MalFactory;
import com.linkly.libmal.global.util.HexDump;
import com.linkly.libmal.global.util.NetworkHelper;
import com.linkly.libmal.global.util.Util;
import com.linkly.libpositive.events.PositiveEvent;
import com.linkly.libpositive.events.PositiveTransEvent;
import com.linkly.libpositive.messages.IMessages;
import com.linkly.libpositive.wrappers.PositiveLogonResult;
import com.linkly.libpositive.wrappers.PositiveTransResult;
import com.linkly.libpositive.wrappers.TagDataToPOS;
import com.linkly.libsecapp.IP2PEncrypt;
import com.linkly.libsecapp.IP2PSec;
import com.linkly.libsecapp.P2PLib;
import com.linkly.libsecapp.emv.Tag;
import com.linkly.libui.IUIDisplay;
import com.linkly.libui.UIScreenDef;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import timber.log.Timber;

/* only class to edit TProtocol.MessageStatus */

public class As2805EftexProto implements IProto {


    private static final String TAG = As2805EftexProto.class.getSimpleName();
    private static final boolean SPOOF_COMMS_FAIL_ON_AUTH = false;
    private static final int LOGON_TRIGGER_TXN_COUNT = 256;
    private static final int LOGON_TRIGGER_TXN_FAIL_COUNT = 2;
    private ByteBuffer lastTxMessage;
    private ByteBuffer lastRxMessage;
    private IDependency d = null;
    private IUIDisplay ui = null;
    private static final int NUM_HOSTS = 1;

    private final As2805EftexRspCodeMap RSP_CODE_MAP = new As2805EftexRspCodeMap();
    private static TProtocol.HostResult lastBatchUploadHostResult = NOT_SET;
    private static final int PERIODIC_BATCH_UPLOAD_TIMEOUT_SEC = 5*60; // hardcoded 5 mins for now
    private static boolean batchUploadInProgress = false;
    private static boolean batchUploadRequestToCancel = false;

    public static class ResponseAction {
        boolean CPAT_Require;
        boolean PKT_Require;
        boolean EPAT_Require;
        boolean FCAT_Require;
    }

    ResponseAction Response = new ResponseAction();

    public class FileUpdate {
        int msgNumber;
        boolean msgNumberLast;
        // define the data structure here if used later, currently the App does not use the CPAT/EPAT
        String data;
    }

    public boolean init(IDependency dependencies) {
        d = dependencies;
        ui = d.getFramework().getUI();
        d.getComms().open(d);

        // set clock settings
        changeClockSettings();
        return true;
    }

    /**
     * how we sync the clock with eftex:
     *
     * response messages from eftex don't have date/time in them, so we enable the android system 'automatic date and time' feature
     * this uses a mix of methods, e.g. NTP to retrieve GMT
     * we still need to set the time zone of the terminal though, based on the time zone setting from TMS
     */
    public void changeClockSettings() {
        Timber.i( "Time zone settings: %s", d.getPayCfg().getTerminalTimeZone() );
        // Make sure the data passed to us is valid
        if(  Util.validTimezone(d.getPayCfg().getTerminalTimeZone()) ) {
            // TODO: Fix this dependency stuff.
            MalFactory.getInstance().getHardware().setAutoTime(true);
            // set terminal time zone
            MalFactory.getInstance().getHardware().setTimeZone(d.getPayCfg().getTerminalTimeZone());
        } else {
            Timber.e( "Invalid time zone data: %s", d.getPayCfg().getTerminalTimeZone() );
        }
    }

    /* perform any tasks that this protocol must do before doing an auth */
    public boolean preAuthorize(TransRec trans) {
        return true;
    }

    /**
     * create a new advice record and mark/save it as approved
     * copy trans details from input trans object
     *
     * @param trans input trans object
     */
    private void createNewAdviceRecordAndApprove(TransRec trans) {
        // create new advice trans record
        TransRec newAdvice = new TransRec(trans.getTransType(), d);
        // copy trans data as required
        newAdvice.setCard(trans.getCard());
        newAdvice.setAmounts(trans.getAmounts());
        newAdvice.setSecurity(trans.getSecurity()); // includes encrypted card details
        newAdvice.setTransEvent(trans.getTransEvent()); // includes printing status
        newAdvice.setPrintOnTerminal(trans.isPrintOnTerminal());
        newAdvice.setProtocol(trans.getProtocol()); // copy protocol object
        newAdvice.setAudit(trans.getAudit()); // copy audit object
        // copy emv tag strings for emv and ctls
        newAdvice.setEmvTagsString(trans.getEmvTagsString());
        newAdvice.setCtlsTagsString(trans.getCtlsTagsString());

        // set flag on txn record to indicate advice is required
        newAdvice.getProtocol().setMessageStatus(ADVICE_QUEUED);
        newAdvice.save();

        // switch current txn object pointer to new advice
        d.resetCurrentTransaction(newAdvice);
    }

    /**
     * authorise transaction offline, either pre-comms or post-comms, depending on authMethod
     *
     * @param trans trans record
     * @param authMethod authorisation method - pre comms, post comms
     */
    public void authorizeOffline(TransRec trans, TProtocol.AuthMethod authMethod) {
        Timber.e("*** Authorising offline **");

        // set 'deferred auth' flag to true, if operating started in transit/flight/offline mode, and issuer setting says process this as deferred auth
        boolean processAsDeferredAuth = trans.isStartedInOfflineMode() && TDeferredAuth.getDeferredAuthConfigFlag(trans, d.getPayCfg());
        Timber.e("processing as deferred auth = %b", processAsDeferredAuth);
        trans.setDeferredAuth(processAsDeferredAuth);

        // if authorised post comms, set response code to Y3
        if( authMethod == OFFLINE_POSTCOMMS_AUTHORISED ) {
            trans.setProtocol(new As2805EftexRspCodeMap().populateProtocolRecord(trans.getProtocol(), "Y3", trans.getTransType()));
        } else if( authMethod == OFFLINE_PRECOMMS_AUTHORISED ) {
            trans.setProtocol(new As2805EftexRspCodeMap().populateProtocolRecord(trans.getProtocol(), "Y1", trans.getTransType()));
        } else if (EngineManager.TransType.COMPLETION_AUTO.equals(trans.getTransType()) || EngineManager.TransType.COMPLETION.equals(trans.getTransType())) {
            // Using 00 Approved for pre-auth completion (offline)
            trans.setProtocol(new As2805EftexRspCodeMap().populateProtocolRecord(trans.getProtocol(), "000", trans.getTransType()));
        }
        trans.getProtocol().setHostResult(AUTHORISED);

        // if reversal queued
        if (trans.getProtocol().getMessageStatus() == REVERSAL_QUEUED) {
            Timber.e("original txn was reversed, creating new approved advice record");
            createNewAdviceRecordAndApprove(trans); // NB updates current trans record to point to new approved advice record
            return;
        }

        //----------------------------------- OFFLINE AUTH processing ---------------------------------------------------
        // maybe generate an auth code
        trans.getProtocol().setAuthMethod(authMethod);
        // allocate new receipt number. increments for each transaction. reverts back to 1 when batch closed
        trans.getAudit().setReceiptNumber(ReceiptNumber.getNewValue());
        trans.getProtocol().setBatchNumber(BatchNumber.getCurValue());

        As2805EftexUtils.updateRetRefNumber(trans);
        trans.updateMessageStatus(MessageStatus.ADVICE_QUEUED);
        setBankDateTime(trans);
    }

    private void setBankDateTime(TransRec trans) {
        // assign from trans audit Datetime
        Date date = new Date(trans.getAudit().getTransDateTime());
        As2805EftexUtils.setBankDateAndTime(date, trans);
    }

    private void setReversalBankDateTime(TransRec trans) {
        // assign from trans audit reversal Datetime
        Date date = new Date(trans.getAudit().getReversalDateTime());
        As2805EftexUtils.setBankDateAndTime(date, trans);
    }

    /**
     * Function in which will change our any settlement bound config.
     */
    private void postSettlementEnvValueChanges() {
        // increment batch number
        BatchNumber.getNewValue();
        // Reset our item number counter (to 0)
        // All our transactions use this value by incrementing first.
        ItemNumber.setNewValue(0);
    }

    /* perform an authorization for this particular transaction */
    /* if its set to an advice then no need to go online */
    /* once we are about to go online then set flag to say it has been sent */
    /* if we need to sendTCP reversals and make it a new transaction message then we do so */
    public boolean authorize(TransRec trans) {

        boolean result = false;
        TProtocol protocol = trans.getProtocol();

        // increment number of txns since logon count, this controls auto logons if we reach certain number
        TxnsSinceLogon.increment();

        encryptCardData(Engine.getDep(), trans);

        // set batch number (settlement date) on trans record to current/last known batch for auto cutover testing and to ensure it's not automatically allocated an incorrect one
        trans.getProtocol().setBatchNumber(BatchNumber.getCurValue());


        if (trans.getTransType() == EngineManager.TransType.TESTCONNECT) {
            //----------------------------------- TEST CONNECT processing ---------------------------------------------------
            return performTestConnect(trans);
        } else if (trans.getTransType() == AUTO_LOGON ||
                   trans.getTransType() == LOGON ||
                   trans.getTransType() == EngineManager.TransType.RSA_LOGON) {

            d.getDebugReporter().reportDebugEvent( IDebug.DEBUG_EVENT.LOGON_STARTED, null );
            d.getStatusReporter().reportStatusEvent( IStatus.STATUS_EVENT.STATUS_LOGON_STARTED , trans.isSuppressPosDialog() );

            // First clause captures standalone PA RSA logons. Second catches Local REST API RSA Logons
            // (because AUTO_LOGON type is used, for standard or RSA Logons from Local REST API).
            if ((trans.getTransType() == EngineManager.TransType.RSA_LOGON) ||
                (trans.getTransEvent() != null && (trans.getTransEvent().getOperationType() == PositiveLogonResult.OperationType.RSA_LOGON))) {
                AS2805LogonState.setNewValue(RSA_LOGON);
            }

            // perform RKI (if required). Do this before socket connect because it takes a few seconds and remote end will hang up if RKI is performed
            // Note: Directly calling a new instance as potentially a new instance could of been generated if p2pe is reinitialised.
            // Need to do a full refactor with protocol in which we switch it from initialised once to initialised when required.
            P2PLib.getInstance().getIP2PSec().as2805GetKeys(d.getCustomer().getTcuKeyLength());

            if ((trans.getTransType() == AUTO_LOGON) ||
                (trans.getTransType() == LOGON)) {
                switch( AS2805LogonState.getCurValue() ) {
                    case LOGGED_ON:
                        // set logon 101 (session key change) required flag
                        AS2805LogonState.setNewValue(LOGON_REQUIRED);
                        break;
                    default:
                        // rsa isn't fully completed, revert to base 'rsa logon'
                        AS2805LogonState.setNewValue(RSA_LOGON);
                        break;
                }
            }

            result = false;

            if (!d.getComms().connect(d,1)) {
                protocol.setHostResult(CONNECT_FAILED);
                trans.setProtocol( this.RSP_CODE_MAP.populateProtocolRecord( trans.getProtocol(), "CE" ) );
            } else {
                result = performLogon( trans, false );
                d.getComms().disconnect(d);
            }
            return result;

        } else if ( trans.isReconciliation() ) {
            //----------------------------------- RECONCILIATION/SETTLEMENT processing ---------------------------------------------------
            // TODO: not sure if this is the best way to handle this. Might be better off saving the reconciliation record in authorize() as queued advice, then just attempting a batch upload
            protocol.setAuthMethod(OFFLINE_PRECOMMS_AUTHORISED);
            trans.updateMessageStatus(MessageStatus.REC_QUEUED);
            trans.getProtocol().setBatchNumber(BatchNumber.getCurValue());

            // Perform any Env values.
            postSettlementEnvValueChanges();

            ui.showScreen(CONNECTING_TO_HOST_PLEASE_WAIT);

            if (!d.getComms().connect(d,0)) {
                protocol.setHostResult(CONNECT_FAILED);
                trans.setProtocol( this.RSP_CODE_MAP.populateProtocolRecord( trans.getProtocol(), "CE" ) );
            } else {
                if (sendReconciliation(trans, false) ) {
                    trans.updateMessageStatus(FINALISED);
                }
            }
            result = true;
        } else if (trans.isReversal()) {
            //----------------------------------- REVERSAL processing ---------------------------------------------------
            TransRec transToReverse = TransRecManager.getInstance().getTransRecDao().getByReceiptNumber(trans.getAudit().getReversalReceiptNumber());
            if (transToReverse != null) {
                if (transToReverse.isReversible()) {

                    /* update the transaction we want to reverse */
                    transToReverse.setToReverse(CUSTOMER_CANCELLATION);
                    transToReverse.setApproved(false); // set approved flag for original trans to false/declined

                    if (trans.getAudit().getReceiptNumber() == -1) {
                        trans.getAudit().setReceiptNumber(ReceiptNumber.getNewValue());
                    }

                    As2805EftexUtils.updateRetRefNumber(transToReverse);
                    As2805EftexUtils.updateRetRefNumber(trans);

                    /* update the reversal transaction itself */
                    protocol.setAuthMethod(OFFLINE_PRECOMMS_AUTHORISED);
                    trans.updateMessageStatus(FINALISED);
                    trans.getProtocol().setHostResult(AUTHORISED);
                    setReversalBankDateTime(trans);
                    trans.setProtocol(new As2805EftexRspCodeMap().populateProtocolRecord(trans.getProtocol(), "000")); // T0 or Y1 may also be valid here
                    transToReverse.save();
                    result = true;
                }
            }
        } else if (trans.getProtocol().isCanAuthOffline()) {
            // Skip this step. Interface method AuthorizeOffline will be called by workflow post card 2nd Gen AC step if offline auth is required
            result = true;
        } else {
            // if operating in offline mode, immediately return connect failed status
            if( trans.isStartedInOfflineMode() ) {
                Timber.e("offline mode is enabled, returning connect failure");
                protocol.setHostResult(CONNECT_FAILED);
                return false;
            }

            Timber.i( "*** we need to authorise online **" );
            //----------------------------------- else financial txn handling ---------------------------------------------------

            // allocate new receipt number. increments for each transaction. reverts back to 1 when batch closed
            trans.getAudit().setReceiptNumber(ReceiptNumber.getNewValue());
            trans.getProtocol().setBatchNumber(BatchNumber.getCurValue());

            trans.save();

            for (int i = 0; i < NUM_HOSTS; i++) {

                UIScreenDef screenDef;

                switch(i){
                    case 2: screenDef = CONNECTING_TO_HOST_2_PLEASE_WAIT; break;
                    case 1: screenDef = CONNECTING_TO_HOST_1_PLEASE_WAIT; break;
                    default: screenDef = CONNECTING_TO_HOST_PLEASE_WAIT; break;
                }

                /* connect the comms */
                ui.showScreen(screenDef);

                if (!d.getComms().connect(d,i)) {
                    // connection failed
                    protocol.setHostResult(CONNECT_FAILED);
                    trans.setProtocol( this.RSP_CODE_MAP.populateProtocolRecord( trans.getProtocol(), "CE" ) );
                } else {

                    /* this will attempt to clear out any reversals as necessary */
                    if ( batchUpload(false) != PROTO_SUCCESS ) {
                        d.getComms().disconnect(d);
                        // Set the transaction response & text accordingly
                        if (lastBatchUploadHostResult != NOT_SET) {
                            // transfer Host result from Batch Upload
                            protocol.setHostResult(lastBatchUploadHostResult);
                            processServerResponseCodes(trans);
                        }
                        else {
                            trans.getProtocol().setPosResponseCode(COMMS_FAIL.code);
                            trans.getProtocol().setServerResponseCode(COMMS_FAIL.code);
                            trans.getProtocol().setPosResponseText(COMMS_FAIL.toString());
                        }
                        // a failure to upload batch here counts as a connect failure for the current auth, as no request will be sent
                        protocol.setHostResult(CONNECT_FAILED);
                        continue;
                    }

                    protocol.setHostResult(NO_RESPONSE);
                    trans.updateMessageStatus(AUTH_SENT);
                    /* save to DB at this point */
                    trans.save();

                    if (doTxRx(trans, trans.isPreAuth()? As2805EftexPack.MsgType.PREAUTH : As2805EftexPack.MsgType.AUTH, false, null, false) && !CoreOverrides.get().isSpoofCommsAuthAll()) {
                        // got a response, process the result
                        trans.updateMessageStatus(WAITING_FOR_FINISH);
                        processServerResponseCodes(trans);

                        Timber.i( "TRUE");
                        result = true;
                    } else {
                        // tx/rx failed - no (valid) host message received to act on
                        // if host result = no response, then we need to send a reversal
                        if( protocol.getHostResult() == NO_RESPONSE ) {
                            trans.setToReverse(COMMS_FAIL);
                        }
                        d.getComms().disconnect(d);
                    }
                    break;
                }
            }
        }

        trans.save();

        d.getComms().disconnect(d);
        return result;
    }

    /* perform any tasks that this protocol must do after doing an auth */
    public boolean postAuthorize(TransRec trans) {

        /* no more messages to sendTCP */
        if (trans.getProtocol().getMessageStatus() == WAITING_FOR_FINISH) {
            trans.updateMessageStatus(FINALISED);
            trans.save();
        }

        // if transaction type was preauth, then populate RFN tag
        if (trans.isPreAuthOrSecondaryPreAuth()) {
            RfnTag rfnTag = new RfnTag(trans.getAudit().getTerminalId(), trans.getProtocol().getRRN(), trans.getProtocol().getAuthCode());
            TagDataToPOS tagDataToPOS = new TagDataToPOS();
            tagDataToPOS.setRFN(rfnTag.pack());
            trans.setTagDataToPos(tagDataToPOS);
        }

        return true;
    }

    /* go through the transactions in the DB and upload as many as possible */
    /* if protocol state is not finalised then sendTCP to protocol to  deal with */
    public ProtoResult batchUpload(boolean silent) {

        boolean success = true;
        int uploadedCount = 0;
        boolean disconnectRequired = false; // tracks if this method needed to do a connect, and used to know if disconnect is required

        // deal with possible async calls (from Periodic Batch Upload)
        if (!allowToContinueBatchUpload( silent )) {
            success = false;
        }
        else {
            batchUploadInProgress = true;
            batchUploadRequestToCancel = false;

            lastBatchUploadHostResult = NOT_SET;
            TransRec currentTrans = d.getCurrentTransaction();

            List<TransRec> allTrans = TransRecManager.getInstance().getTransRecDao().findAllNotMessageStatus(FINALISED);

            if (allTrans != null) {

                try {
                    // connect only if we need to
                    if (!d.getComms().isConnected(d)) {
                        boolean connectResult;

                        // if the batch upload is being done silently then only check network with retry
                        // otherwise jump straight into a connection attempt. We still have timeout fallback
                        // in the connection attempt if the connection is unsuccessful
                        if (silent) {
                            // TODO: Fix this
                            if (NetworkHelper.checkNetworkWithRetry(MalFactory.getInstance().getMalContext(), true)) {
                                connectResult = d.getComms().connect(d, 0);
                            } else {
                                // network not available, return error
                                Timber.e("batchUpload - network not available, returning connection error");
                                connectResult = false;
                            }
                        } else {
                            connectResult = d.getComms().connect(d, 0);
                        }

                        if( !connectResult ) {
                            Timber.e( "Batch upload failed due to connection failure" );
                            success = false;
                        } else {
                            disconnectRequired = true;
                        }
                    }

                    // only execute if no failures above
                    if( success ) {
                        for (TransRec trans : allTrans) {
                            // check if there is a request to interrupt Batch Upload
                            if (batchUploadRequestToCancel) {
                                success = false;
                                break;
                            }

                            /* this is to stop background threads uploading the current transaction whilst it is processing */
                            /* foreground threads need to do it, so that reversals get sent inline for every auth */
                            if (currentTrans != null && !trans.isFinalised() && silent) {
                                Timber.i("DONT UPLOAD transaction yet as its not finished");
                                continue;
                            }

                            MessageStatus messageStatus = trans.getProtocol().getMessageStatus();
                            PositiveTransResult.JournalType type = NONE;
                            if (messageStatus == REC_QUEUED) {
                                if (sendReconciliation(trans, silent)) {
                                    uploadedCount++;
                                    trans.updateMessageStatus(FINALISED);
                                    // These are just settlements (AKA zreports)
                                    // Note: trans.rec is generated inside the send reconciliation function
                                    // TODO: Fix context here.
                                    ECRHelpers.ipcSendReportResponse(d, trans, trans.getReconciliation(), IMessages.ReportType.ZReport.toString(), MalFactory.getInstance().getMalContext());
                                } else {
                                    success = false;
                                    break;
                                }
                            } else if (messageStatus == REVERSAL_QUEUED || messageStatus == AUTH_SENT) {
                                // Make sure that LogonState is correct
                                if (AS2805LogonState.getCurValue() != LOGGED_ON) {
                                    performInlineLogon();
                                }

                                if (doTxRx(trans, REVERSAL, silent, null, false)) {
                                    uploadedCount++;
                                    // reversal of deferred auth causes reverting back to advice queued status as we'll need to send it again
                                    if( trans.isDeferredAuth() ) {
                                        trans.updateMessageStatus(ADVICE_QUEUED);
                                        trans.getProtocol().setStan(0); // forces use of a new STAN for next send attempt for the deferred auth
                                    } else {
                                        trans.updateMessageStatus(FINALISED_AND_REVERSED);
                                    }
                                    type = PositiveTransResult.JournalType.REVERSAL;
                                } else {
                                    success = false;
                                    processServerResponseCodes(trans);
                                    trans.save(); // e.g. update advice attempts
                                    break;
                                }
                            }

                            // DO NOT put this in the else if chain above, because a message could transition from REVERSAL_QUEUED to ADVICE_QUEUED above and we want to process it
                            if (trans.getProtocol().getMessageStatus() == ADVICE_QUEUED) {
                                boolean loggedIn = true;
                                // Make sure that LogonState is correct
                                if (AS2805LogonState.getCurValue() != LOGGED_ON) {
                                    loggedIn = performInlineLogon();
                                }
                                if (loggedIn) {
                                    // Treat deferred auth transactions as an auth transaction, so in case of failed comms a reversal can be generated
                                    if (trans.isDeferredAuth()) {
                                        trans.getProtocol().setHostResult(NO_RESPONSE);
                                        trans.updateMessageStatus(AUTH_SENT);
                                        /* save to DB at this point */
                                        trans.save();
                                    }

                                    // deferred auths are queued + sent similar to advices
                                    if (doTxRx(trans, trans.isDeferredAuth() ? DEFERRED_AUTH : ADVICE, silent, null, false)) {
                                        uploadedCount++;
                                        processAdviceOrDeferredAuthResponseCodes(trans);
                                        trans.updateMessageStatus(FINALISED);
                                        type = PositiveTransResult.JournalType.ADVICE;
                                    } else {
                                        // for deferred auths, if host result = no response, then we need to send a reversal and queue resend again
                                        if( trans.isDeferredAuth() && trans.getProtocol().getHostResult() == NO_RESPONSE ) {
                                            trans.setToReverse(COMMS_FAIL);
                                        }

                                        success = false;
                                        trans.save(); // e.g. update advice attempts
                                        break;
                                    }
                                }
                            }

                            // Notify any other application what has happened
                            if (type != NONE) {
                                // Note: Journal type does retain its state
                                trans.setJournalType(type);
                                ECRHelpers.ipcSendTransResponse(d, trans, MalFactory.getInstance().getMalContext());
                            }

                            lastBatchUploadHostResult = trans.getProtocol().getHostResult();
                            trans.save();
                        }
                    }

                } catch (Exception e) {
                    Timber.w(e);
                    success = false;
                }

                if (disconnectRequired) {
                    d.getComms().disconnect(d);
                }
            }

            Timber.e("BatchUpload returned: " + success + " and sent: " + uploadedCount + " transactions");
            batchUploadInProgress = false;
        }

        setupNextBatchUpload();

        return success ? PROTO_SUCCESS : PROTO_FAIL;
    }

    private void setupNextBatchUpload() {
        long startTime = new Date().getTime();
        long triggerTime = startTime + PERIODIC_BATCH_UPLOAD_TIMEOUT_SEC *1000;

        EFTJobScheduleEvent nextBatchUploadEvent = new EFTJobScheduleEvent(EFTJobScheduleEvent.EventType.UPDATE, BATCH_UPLOAD_EVENT, triggerTime);
        d.getJobs().schedule(MalFactory.getInstance().getMalContext(), nextBatchUploadEvent);
    }

    private boolean allowToContinueBatchUpload(boolean silent) {
        boolean allow = true;
        if (batchUploadInProgress) {
            // "silent" is true for scheduled batch uploads
            if (silent) {
                // we are scheduled Batch upload; do not interrupt any running one
                allow = false;
            }
            else {
                batchUploadRequestToCancel = true;
                int timeout = d.getPayCfg().getPaymentSwitch().getReceiveTimeout();

                try {
                    long start = System.currentTimeMillis();
                    while (batchUploadInProgress) {
                        Thread.sleep(100L);
                        if (System.currentTimeMillis() - start > (long) timeout * 1000 * 2) {
                            // waited two comms timeouts, safety break allowing to continue with batch upload regardless
                            Timber.e("Timed out waiting for scheduled Batch Upload interruption");
                            break;
                        }
                    }
                } catch (Exception e) {
                    Timber.w(e);
                }
            }
        }
        return allow;
    }

    @Override
    public boolean timeSync(TransRec trans) {
        return true;
    }

    public boolean sendReconciliation(TransRec reconciliationTrans, boolean silent) {

        boolean success = true;

        // update record
        reconciliationTrans.getProtocol().setHostResult(NO_RESPONSE);
        reconciliationTrans.save();

        // send/receive
        if (doTxRx(reconciliationTrans, RECONCILIATION, silent, null, false)) {
            processServerResponseCodes(reconciliationTrans);
            reconciliationTrans.updateMessageStatus(MessageStatus.FINALISED);
        } else {
            success = false;
            reconciliationTrans.getProtocol().setHostResult(CONNECT_FAILED);
            reconciliationTrans.setProtocol( this.RSP_CODE_MAP.populateProtocolRecord( reconciliationTrans.getProtocol(), "CE" ) );
        }

        Timber.e( "Reconciliation returned: %s", success);
        return success;
    }


    /* complicated function used to match up with saveFutureMacs() in As2805EftexPack */
    /* unique stan for every send */
    public void getNewStan(TransRec trans) {
        // assign a new stan if the trans record doesn't currently have one. E.g. for pin entry we need a stan, so stan would already have been set
        if( 0 == trans.getProtocol().getStan() ) {
            trans.getProtocol().setStan(Stan.getNewValue());
        }
    }

    /* sendTCP and receive a particular message type */
    public boolean doTxRx( TransRec trans, As2805EftexPack.MsgType msgType, boolean silent, String funcCode, boolean useZeroStan) {
        TProtocol protocol = trans.getProtocol();

        try {
            IComms icomms = d.getComms();

            if( !useZeroStan ) {
                // get a unique stan every time we try to send
                getNewStan(trans);
            }

            final byte[] packedBuffer = As2805EftexPack.pack(d,trans, msgType, funcCode );
            if (packedBuffer == null || packedBuffer.length == 0) {
                Timber.i( "Failed to pack");
                // treat as connect failed - no reversal required because request wasn't sent
                protocol.setHostResult(CONNECT_FAILED);
                trans.setProtocol( this.RSP_CODE_MAP.populateProtocolRecord( trans.getProtocol(), "CE" ) );
                return false;
            }
            // update trans record with original trans data for reversal
            trans.save();

            if( SPOOF_COMMS_FAIL_ON_AUTH && (msgType == AUTH || msgType == DEFERRED_AUTH)) {
                trans.setProtocol( this.RSP_CODE_MAP.populateProtocolRecord( trans.getProtocol(), "X0" ) );
                protocol.setHostResult(NO_RESPONSE);
                return false;
            }

            /* store the packed buffer (to help with debugging) */
            lastTxMessage = ByteBuffer.wrap(packedBuffer);

            if (!silent) {
                d.getStatusReporter().reportStatusEvent(IStatus.STATUS_EVENT.STATUS_PROCESSING , trans.isSuppressPosDialog());
                ui.showScreen(CONNECTING_TO_HOST_SENDING_MSG);
            }

            // report debug message, if applicable
            reportDebugEvent(msgType);

            Timber.i( "Send Packed Buffer:%s", packedBuffer.length);
            Timber.i(  HexDump.dumpHexString( "msgtx: ", packedBuffer));

            if( icomms instanceof IpGatewayProxyComms ) {
                if (icomms.send(d,
                        d.getPayCfg().getPosCommsHostId(),
                        d.getPayCfg().getMid(), d.getPayCfg().getStid(),
                        msgType.getSendMsgId(), packedBuffer ) <= 0) {
                    Timber.i( "Failed to Send Packed Buffer");
                    return false;
                }
            } else {
                if (icomms.send(d, packedBuffer) <= 0) {
                    Timber.i( "Failed to Send Packed Buffer");

                    // treat as connect failed - no reversal required because request wasn't sent
                    protocol.setHostResult(CONNECT_FAILED);
                    trans.setProtocol( this.RSP_CODE_MAP.populateProtocolRecord( trans.getProtocol(), "CE" ) );
                    return false;
                }

            }

            // if advice (0200 deferred auth, 0220 or reversal 0420 advice), increment advice attempt counter
            if( msgType == DEFERRED_AUTH || msgType == ADVICE || msgType == REVERSAL ) {
                protocol.setAdviceAttempts(protocol.getAdviceAttempts() + 1);
                trans.save();
            }

            if (!silent) {
                ui.showScreen(CONNECTING_TO_HOST_WAITING_FOR_RESPONSE);
            }

            boolean completed = false;
            int retries = 0;

            do {
                // Only loop if invalid response
                byte[] response = icomms.recv(d);
                if (response != null) {

                    /* store the packed buffer (to help with debugging) */
                    lastRxMessage = ByteBuffer.wrap(response);



                    As2805EftexPack.UnPackResult unPackResult = As2805EftexPack.unpack( d, response, trans, msgType, funcCode );
                    switch ( unPackResult ){

                        case UNPACK_OK:
                            // check if host has cut over settlement date
                            checkForHostSettlement(trans);
                            TxnsNoReponse.reset();
                            return true;
                        case MAC_ERROR:
                            // invalid response message, treat as no response
                            protocol.setHostResult(NO_RESPONSE);
                            if (!silent) {
                                ui.showScreen(COMMS_FAILURE_RECEIVING_RESPONSE);
                                ui.getResultCode(IUIDisplay.ACTIVITY_ID.ACT_INFORMATION, IUIDisplay.SHORT_TIMEOUT);
                            }
                            // set logon required state
                            AS2805LogonState.setNewValue(LOGON_REQUIRED);
                            completed = true;
                            break;

                        case VERIFICATION_FAILED:
                        case GENERIC_FAILURE:
                        default:
                            Timber.e( "Unpack failed with [%s]", unPackResult );
                            if ( ++retries >= 3  ) {
                                Timber.e( "Retries extinguished" );
                                protocol.setHostResult( NO_RESPONSE );
                                completed = true;
                            }
                            break;
                    }
                } else {
                    // receive failed, treat as no response
                    trans.setProtocol( this.RSP_CODE_MAP.populateProtocolRecord( trans.getProtocol(), "X0" ) );
                    protocol.setHostResult(NO_RESPONSE);
                    TxnsNoReponse.increment();
                    completed = true;
                }
            } while( !completed );

        } catch (Exception e) {
            Timber.i( "doTxRx Exception" );
            Timber.w(e);
            protocol.setHostResult( NO_RESPONSE );
        }
        return false;
    }

    private void reportDebugEvent(As2805EftexPack.MsgType msgType) {
        if (msgType == ADVICE) {
            d.getDebugReporter().reportDebugEvent(IDebug.DEBUG_EVENT.ADVICE_UPLOAD, null);
        } else if (msgType == REVERSAL) {
            d.getDebugReporter().reportDebugEvent(IDebug.DEBUG_EVENT.REVERSAL_STARTED, null);
        } else if (msgType == DEFERRED_AUTH) {
            d.getDebugReporter().reportDebugEvent(IDebug.DEBUG_EVENT.DEFERRED_AUTH_UPLOAD, null);
        }
    }

    private void checkForHostSettlement(TransRec trans) {
        String settlementDate = trans.getProtocol().getSettlementDate();

        // don't do this if settlement date is not returned from host, or current txn is a reconciliation
        if (isNullOrEmpty(settlementDate) || trans.isReconciliation()) {
            return;
        }

        // if settlement date received doesn't match what terminal currently has, host has cut over
        Timber.e("Host settlement date check . Terminal value (last known):" + SettlementDate.getCurValue() + ", Host value (from msg):" + settlementDate);

        // settlement environment values update, if the settlement date has changed
        if (isSettlementDateChanged(settlementDate)) {
            Timber.e("HOST HAS CUT OVER THE SETTLEMENT DATE. Prev:" + SettlementDate.getCurValue() + ", Host val:" + settlementDate);

            // update batch number and item number/transaction sequence number env variables to new values
            postSettlementEnvValueChanges();
            SettlementDate.setNewValue(settlementDate);

            // if there are any unsettled transactions, then do background settlement now
            // get unreconciled totals
            IDailyBatch dailyBatch = new DailyBatch();
            Reconciliation recData = dailyBatch.generateDailyBatch(false, d);
            if( recData.getTotalAmount() > 0 && recData.getTotalCount() > 0 ) {
                Timber.e( "HOST HAS CUT OVER THE SETTLEMENT DATE - UNRECONCILED TRANSACTIONS FOUND. DOING BACKGROUND SETTLEMENT NOW");

                // create settlement record, mark all trans records as reconciled
                recData = dailyBatch.generateDailyBatch(true, d);

                TransRec recTrans = new TransRec(EngineManager.TransType.RECONCILIATION, d);
                recTrans.setApproved(true);
                recTrans.setFinalised(true);
                // set transaction record batch number to match the reconciliation record batch number (batch number of newest txn in reconciliation)
                recTrans.getProtocol().setBatchNumber(recData.getBatchNumber());

                recTrans.getProtocol().setHostResult(RECONCILED_OFFLINE_HOST_CUTOVER);
                recTrans.getProtocol().setAuthMethod(OFFLINE_NO_COMMS_ATTEMPTED);
                recTrans.getProtocol().setMessageStatus(FINALISED);
                recTrans.setReconciliation(recData);
                recTrans.save();
                ECRHelpers.ipcSendReportResponse(d, recTrans, recTrans.getReconciliation(), IMessages.ReportType.ZReport.toString(), MalFactory.getInstance().getMalContext(), false);
            }
        }
    }

    private boolean doRsaKeyInitMsg(TransRec trans, boolean noUi, String funcCode, boolean useZeroStan ) {
        // set stan to zero so it gets a new one with each msg
        trans.getProtocol().setStan(0);
        if( !doTxRx(trans, NETWORK, noUi, funcCode, useZeroStan) ) {
            trans.getProtocol().setHostResult( CONNECT_FAILED );
            trans.setProtocol( this.RSP_CODE_MAP.populateProtocolRecord( trans.getProtocol(), "CE" ) );
            trans.updateMessageStatus( FINALISED );
            AS2805LogonState.setNewValue(RSA_LOGON); // revert back to initial state
            return false;
        }

        // if not "800" then treat as declined
        if( !"800".equals(trans.getProtocol().getServerResponseCode())) {
            // if not 00 approved resp code, return failure
            Timber.i( "1814 funcCode " + funcCode + " returned error response code " + trans.getProtocol().getServerResponseCode());
            trans.updateMessageStatus( FINALISED );
            AS2805LogonState.setNewValue(RSA_LOGON); // revert back to initial state
            return false;
        }

        return true;
    }

    private void logonSuccessfulTasks() {
        // move to next state
        AS2805LogonState.setNewValue( LOGGED_ON );
        // reset env vars controlling auto logons
        TxnsSinceLogon.reset();
        LastLogonTime.setToNow();
    }

    /**
     * Packs, sends a logon request to the bank
     * Will also unpack the response to the request
     * @param trans {@link TransRec} Trans record
     * @param noUi boolean to tell whether UI needs to be updated
     * @return true if successful
     * */
    private boolean performLogon( TransRec trans, boolean noUi ){
        boolean connectRequired = !d.getComms().isConnected( d );
        boolean result = true;


        if ( CoreOverrides.get().isSpoofComms() ||
//                isSecurityDisabled( d ) ||
                AS2805LogonState.getCurValue() == LOGGED_ON ) {
            // nothing to do, return success
            trans.getProtocol().setHostResult( AUTHORISED );
            trans.updateMessageStatus( FINALISED );
            return result;
        }

        /* connect the comms, if required */
        if ( connectRequired && !d.getComms().connect( d, 1 ) ) {
            // connect failure
            trans.getProtocol().setHostResult( CONNECT_FAILED );
            trans.setProtocol( this.RSP_CODE_MAP.populateProtocolRecord( trans.getProtocol(), "CE" ) );
            trans.updateMessageStatus( FINALISED );
            return false;
        }

        trans.getProtocol().setHostResult( NO_RESPONSE );
        trans.updateMessageStatus( AUTH_SENT );

        do {
            AS2805LogonState.LogonState curState = AS2805LogonState.getCurValue();
            Timber.e( "AS2805 logon state = %s", curState.toString() );

            switch ( curState ) {
                case RSA_LOGON:
                    // 1804 func code 897 rsa key init part 1
                    if ( doRsaKeyInitMsg( trans, noUi, RKI_REQUEST_INITIAL, false ) ) {
                        // move to next state
                        AS2805LogonState.setNewValue( KTM_LOGON );
                    } else {
                        result = false;
                    }
                    break;

                case KTM_LOGON:
                    // 1804 func code 898 rsa key init part 2
                    if ( doRsaKeyInitMsg( trans, noUi, RKI_REQUEST_SECOND, false ) ) {
                        // move to next state
                        AS2805LogonState.setNewValue( KEK_LOGON );
                    } else {
                        result = false;
                    }
                    break;

                case KEK_LOGON:
                    // 1804 func code 899 rsa key init part 3
                    if ( doRsaKeyInitMsg( trans, noUi, RKI_REQUEST_FINAL, false ) ) {
                        // move to next state
                        AS2805LogonState.setNewValue( SESSION_KEY_LOGON );
                    } else {
                        result = false;
                    }
                    break;

                case SESSION_KEY_LOGON:
                case LOGON_REQUIRED:
                    // 1804 func code 811 session key exchange
                    if ( doRsaKeyInitMsg( trans, noUi, SESSION_KEY_EXCHANGE, false ) ) {
                        logonSuccessfulTasks();
                    } else {
                        result = false;
                    }
                    break;

                default:
                    Timber.d( "Unknown state = %s", AS2805LogonState.getCurValue().name() );
                    result = false;
                    break;
            }
        } while ( AS2805LogonState.getCurValue() != LOGGED_ON && result );

        // if we get here, then all is good
        if( LOGGED_ON == AS2805LogonState.getCurValue() && result ) {
            trans.getProtocol().setHostResult( AUTHORISED );
            trans.updateMessageStatus( FINALISED );
        }

        // NEVER forget to disconnect - very important!
        if ( connectRequired ) {
            d.getComms().disconnect( d );
        }

        return result;
    }

    private boolean performTestConnect(TransRec trans) {

        for (int i = 0; i < NUM_HOSTS; i++) {
            /* connect the comms */
            if (d.getComms().connect(d, i)) {

                trans.getProtocol().setHostResult(NO_RESPONSE);
                trans.updateMessageStatus(AUTH_SENT);
                /* save to DB at this point */

                if (doTxRx(trans, NETWORK, false, null, false) || CoreOverrides.get().isSpoofComms()) {
                    trans.getProtocol().setHostResult(AUTHORISED);
                    trans.updateMessageStatus(FINALISED);
                    d.getComms().disconnect(d);

                    return true;
                }
            }
        }
        trans.getProtocol().setHostResult(CONNECT_FAILED);
        trans.setProtocol( this.RSP_CODE_MAP.populateProtocolRecord( trans.getProtocol(), "CE" ) );
        trans.updateMessageStatus(FINALISED);
        return false;
    }

    public String getEmvTagValuesForDB(TransRec trans) {
        return null;
    }
    public String getCtlsTagValuesForDB(TransRec trans) {
        return null;
    }
    public ArrayList<Tag> getEmvTagList() {

        ArrayList<Tag> tags = new ArrayList<>();
        tags.add(issuer_country_code);
        tags.add(trans_curcy_code);
        tags.add(appl_pan_seqnum);
        tags.add(aid);
        tags.add(appl_intchg_profile);
        tags.add(tvr);
        tags.add(tran_date);
        tags.add(tran_type);
        tags.add(amt_auth_num);
        tags.add(amt_other_num);
        tags.add(issuer_app_data);
        tags.add(term_county_code);
        tags.add(appl_cryptogram);
        tags.add(crypt_info_data);
        tags.add(term_cap);
        tags.add(cvm_results);
        tags.add(term_type);
        tags.add(atc);
        tags.add(unpred_num);
        tags.add(trans_category_code);
        tags.add(issuer_script_results);
        tags.add(issuer_script_results2);
        tags.add(visa_ttq);
        tags.add(third_party_data);
        tags.add(eftpos_payment_account_reference);
        tags.add(eftpos_token_requestor_id);
        tags.add(auth_resp_code);
        tags.add(app_curr_code);
        tags.add(df_name);
        tags.add(tsi);
        tags.add(appl_cryptogram_genAc2);
        tags.add(crypt_info_data_genAc2);
        return tags;
    }

    private void setAuthorisedFlags(TransRec trans) {
        TProtocol protocol = trans.getProtocol();
        trans.getProtocol().setAuthMethod(ONLINE_AUTHORISED);
        protocol.setHostResult(TProtocol.HostResult.AUTHORISED);
        if( trans.isReconciliation() ) {
            protocol.setHostResult(TProtocol.HostResult.RECONCILED_IN_BALANCE);
        }
    }

    void processAdviceOrDeferredAuthResponseCodes(TransRec trans) {
        TProtocol protocol = trans.getProtocol();

        String actionCode = protocol.getAdviceResponseCode();
        if( isNullOrEmpty(actionCode) ) {
            // shouldn't really happen, but if it does, then default to declined
            Timber.e( "advice response code is NULL" );
            protocol.setHostResult(DECLINED);
            return;
        }

        // else all other trans types
        switch( actionCode ) {
            case "000":
            case "001":
                protocol.setHostResult(AUTHORISED);
                break;
            default:
                protocol.setHostResult(DECLINED);
                break;
        }
    }

    void processServerResponseCodes(TransRec trans) {
        TProtocol protocol = trans.getProtocol();

        String actionCode = protocol.getServerResponseCode();
        if( isNullOrEmpty(actionCode) ) {
            // shouldn't really happen, but if it does, then default to declined
            Timber.e( "actionCode is NULL" );
            protocol.setHostResult(DECLINED);
            return;
        }

        // else all other trans types
        switch( actionCode ) {
            case "000":
                // 'standard' approved values
                // if signature CVM was set, and config says signature is NOT supported
                if( trans.getCard().getCvmType() == TCard.CvmType.SIG && d.getPayCfg() != null && !d.getPayCfg().isSignatureSupported()) {
                    // .. then set NO CVM as host has responded approved
                    trans.getCard().setCvmType(NO_CVM);
                }
                setAuthorisedFlags(trans);
                break;

            case "001":
                if( d.getPayCfg() != null && d.getPayCfg().isSignatureSupported() ) {
                    // approved with signature
                    trans.getProtocol().setSignatureRequired(true);
                    setAuthorisedFlags(trans);
                } else {
                    Timber.e( TAG, "Declining sig required txn because signature not supported" );
                    // decline and generate a reversal for this
                    protocol.setHostResult(DECLINED);

                    // update display, receipt text etc with declined text
                    trans.setProtocol( this.RSP_CODE_MAP.populateProtocolRecord( trans.getProtocol(), "S1" ) );

                    // use signature failure reason
                    trans.setToReverse(SIGFAIL);
                    trans.save();
                }
                break;

            case "500":
                if (trans.isReconciliation()) {
                    setAuthorisedFlags(trans);
                } else {
                    // default to declined
                    protocol.setHostResult(DECLINED);
                }
                break;

            case "501":
                if (trans.isReconciliation()) {
                    trans.getProtocol().setHostResult(TProtocol.HostResult.RECONCILED_OUT_OF_BALANCE);
                } else {
                    // default to declined
                    protocol.setHostResult(DECLINED);
                }
                break;

            default:
                // default to declined
                protocol.setHostResult(DECLINED);
                break;
        }

    }

    public String getEmvProcessingCode(TransRec trans) {
        try {
            ProcessingCode pc = As2805EftexUtils.packProcCode(trans);
            return pc.getTranType();
        } catch (Exception e) {
            Timber.w(e);
        }
        /* default to sale */
        return "00";
    }

    public String calculateRRN(TransRec trans) {
        try {
            return As2805EftexUtils.calculateRetRefNumber(trans);
        } catch (Exception e) {
            return "N\\A";
        }
    }

    public byte[] getLastTxMessage() {
        if (lastTxMessage == null)
            return null;
        return lastTxMessage.array();
    }
    public byte[] getLastRxMessage() {
        if (lastRxMessage == null)
            return null;
        return lastRxMessage.array();
    }
    public boolean requiresDeclinedAdvices() {
        return false;
    }


    @Override
    public boolean discountVoucherRedeem(TransRec trans) {
        return false;
    }

    @Override
    public boolean discountVoucherReverse(TransRec trans) {
        return false;
    }

    @Override
    public String encryptCardData(IDependency d, TransRec trans) {
        P2PLib p2pInstance = P2PLib.getInstance();
        IP2PEncrypt p2pEncrypt = p2pInstance.getIP2PEncrypt();

        if (p2pEncrypt == null)
            return null;

        byte[] encResult = p2pEncrypt.encryptForStorage(FULL_TRACK_FORMAT);

        if (encResult != null) {
            trans.getSecurity().setEncTrack2(Util.byteArrayToHexString(encResult));
        }
        return null;
    }

    @Override
    public String saveEmvTagValuesForDB(IDependency d, TransRec trans) {
        if (trans.getCard().getTags() == null || trans.getCard().getTags().isEmpty()) {
            return null;
        }
        try {
            return SvfeUtils.packIccData_common(d, trans, null, false);
        } catch (Exception e) {
            Timber.w(e);
        }
        return null;
    }

    @Override
    public String saveCtlsTagValuesForDB(IDependency d, TransRec trans) {
        if (trans.getCard().getTags() == null || trans.getCard().getTags().isEmpty()) {
            return null;
        }

        try {
            return As2805EftexUtils.packIccDataCommon(d, trans, null, false);
        } catch (Exception e) {
            Timber.w(e);
        }
        return null;
    }

    @Override
    public void saveSignatureDeclined( TransRec transRec ) {}

    @Override
    public boolean performProtocolChecks() {
        boolean ret = true;

        TransRec trans = d.getCurrentTransaction();
        if( trans != null && trans.isStartedInOfflineMode() ) {
            // skip protocol checks if operating offline
            return true;
        }

        // trigger an automatic logon if it's been >= 24hrs or > 256 txns since last logon
        if( AS2805LogonState.getCurValue() == AS2805LogonState.LogonState.LOGGED_ON ) {
            if (LastLogonTime.isOverOneDay()) {
                Timber.e("Over 24hrs since last logon, flagging logon required");
                AS2805LogonState.setNewValue(LOGON_REQUIRED);
            } else if (TxnsSinceLogon.getCurValue() >= LOGON_TRIGGER_TXN_COUNT) {
                Timber.e("%d or more txns since last logon, flagging logon required", LOGON_TRIGGER_TXN_COUNT);
                AS2805LogonState.setNewValue(LOGON_REQUIRED);
            } else if (TxnsNoReponse.getCurValue() > LOGON_TRIGGER_TXN_FAIL_COUNT) {
                Timber.e("More than %d no responses, the keys could be out of sync, flagging logon required", LOGON_TRIGGER_TXN_FAIL_COUNT);
                AS2805LogonState.setNewValue(LOGON_REQUIRED);
            }
        }

        // if a logon of some type is required AND this txn is not a logon txn itself
        if( AS2805LogonState.getCurValue() != LOGGED_ON && d.getCurrentTransaction() != null
                && !d.getCurrentTransaction().isLogon() ) {
            // then do a logon
            d.getDebugReporter().reportDebugEvent( IDebug.DEBUG_EVENT.LOGON_STARTED, null );
            d.getStatusReporter().reportStatusEvent( IStatus.STATUS_EVENT.STATUS_LOGON_STARTED , d.getCurrentTransaction().isSuppressPosDialog() );
            if (!performInlineLogon()) {
                ret = false;
                ui.showScreen( LOGON_FAILED );
                d.getStatusReporter().reportStatusEvent(IStatus.STATUS_EVENT.STATUS_ERR_LOGON_FAILED , d.getCurrentTransaction().isSuppressPosDialog());
                d.getDebugReporter().reportDebugEvent(IDebug.DEBUG_EVENT.LOGON_STARTED, "FAILED");
                TransRec transRec = d.getCurrentTransaction();

                if( transRec != null && transRec.getProtocol() != null ){
                    // According to specs, any response code other '00', '08', '11' is taken as declined
                    d.getProtocol().setInternalRejectReason( transRec, RejectReasonType.PROTOCOL_TASKS_FAILED );
                } else {
                    Timber.e( "Either trans or protocol was null" );
                }
            }
        }
        return ret;
    }

    @Override
    public void setInternalRejectReason( TransRec trans, RejectReasonType rejectReasonType) {
        trans.setProtocol( this.RSP_CODE_MAP.populateProtocolRecord( trans.getProtocol(), rejectReasonType ) );
    }

    @Override
    public void setInternalRejectReason(TransRec trans, RejectReasonType rejectReasonType, String errorText) {
        trans.setProtocol( this.RSP_CODE_MAP.populateProtocolRecord( trans.getProtocol(), rejectReasonType, errorText ) );
    }

    @Override
    public int getMaxBatchNumber() {
        return 999;
    }


    /**
     * used to look up transaction details based on passed reference, could be from POS, could be manually entered
     * the format of the reference may vary by customer
     *
     * @param txnReference
     * @return true = transaction/card data found
     */
    public TransRec lookupOriginalTransaction(String txnReference) {
        RfnTag rfnTag = new RfnTag(txnReference);

        // if parsing of the RFN tag fails, return null
        if( !rfnTag.parse() ) {
            return null;
        }

        // do database lookup based on rfn tag values
        return TransRecManager.getInstance().getTransRecDao().findByTidRrnAuthNumber(EngineManager.TransType.PREAUTH, PREAUTH_AUTO, PREAUTH_MOTO, PREAUTH_MOTO_AUTO,rfnTag.tid, rfnTag.rrn, rfnTag.authNumber);
    }

    /**
     * Performs an inline logon by generating a dummy {@link TransRec} object which is never saved
     * @return true if Logon is authorised
     * */
    private boolean performInlineLogon() {
        TransRec inlineLogonTrans = new TransRec(EngineManager.TransType.AUTO_LOGON, d);
        inlineLogonTrans.setTransEvent(new PositiveTransEvent(PositiveEvent.EventType.AUTO_LOGON));
        inlineLogonTrans.getTransEvent().setOperationType(PositiveLogonResult.OperationType.LOGON);

        Timber.d("Going to perform an AutoLogon");
        if (inlineLogonTrans.getProtocol() != null && inlineLogonTrans.getAudit() != null) {
            boolean result;

            // perform RKI (if required). Do this before socket connect because it takes a few seconds and remote end will hang up if RKI is performed
            // Need to make sure we have access to the most recent value.
            P2PLib.getInstance().getIP2PSec().as2805GetKeys(d.getCustomer().getTcuKeyLength());

            inlineLogonTrans.setTransType(EngineManager.TransType.AUTO_LOGON);
            result = this.performLogon(inlineLogonTrans, true);
            // We need to send an inline response here.
            ECRHelpers.ipcSendLogonResponse(d, inlineLogonTrans, MalFactory.getInstance().getMalContext(), false);

            return result;
        } else {
            Timber.d("Protocol Object = " + inlineLogonTrans.getProtocol() + " Audit object = " + inlineLogonTrans.getAudit());
            return false;
        }
    }

    private class RfnTag {
        // woolies RFN tag format:
        //
        // TID DE 41        8 AN
        // RRN DE 37       12 AN
        // Auth no DE 38    6 AN
        // ---------------------
        // total 26 chars
        String tid;
        String rrn;
        String authNumber;
        String packedRfn;

        /**
         * pack contstructor
         *
         * @param tid
         * @param rrn
         * @param authNumber
         */
        public RfnTag(String tid, String rrn, String authNumber ) {
            this.tid = tid;
            this.rrn = rrn;
            this.authNumber = authNumber;
        }

        /**
         * unpack constructor
         *
         * @param packedRfn packed RFN tag in above format
         */
        public RfnTag( String packedRfn ) {
            this.packedRfn = packedRfn;
        }

        /**
         * parse provided packed RFN
         *
         * @return true = parse success, false = parse failed
         */
        public boolean parse() {
            try {
                // use substring as field widths are fixed. trim after to get rid of padding spaces
                tid = packedRfn.substring(0, 8).trim();
                rrn = packedRfn.substring(8, 20).trim();
                authNumber = packedRfn.substring(20, 26).trim();
            } catch( Exception e ) {
                Timber.w(e);
                Timber.e( "parse of RFN threw exception" );
                return false;
            }

            return true;
        }

        /**
         * packs to the format above, right-padding with whitespace where required
         *
         * @return packed RFN value
         */
        public String pack() {
            return Util.padRight(tid, 8, ' ') +
                    Util.padRight(rrn, 12, ' ') +
                    Util.padRight(authNumber, 6, ' ');
        }

    }
}
