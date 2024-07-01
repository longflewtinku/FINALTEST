package com.linkly.libengine.engine.protocol.iso8583;

import static com.linkly.libengine.engine.EngineManager.TransType.AUTO_LOGON;
import static com.linkly.libengine.engine.EngineManager.TransType.LOGON;
import static com.linkly.libengine.engine.EngineManager.TransType.PREAUTH_AUTO;
import static com.linkly.libengine.engine.EngineManager.TransType.PREAUTH_MOTO;
import static com.linkly.libengine.engine.EngineManager.TransType.PREAUTH_MOTO_AUTO;
import static com.linkly.libengine.engine.protocol.IProto.ProtoResult.PROTO_FAIL;
import static com.linkly.libengine.engine.protocol.IProto.ProtoResult.PROTO_SUCCESS;
import static com.linkly.libengine.engine.protocol.iso8583.As2805WoolworthsPack.MsgType.ADVICE;
import static com.linkly.libengine.engine.protocol.iso8583.As2805WoolworthsPack.MsgType.AUTH;
import static com.linkly.libengine.engine.protocol.iso8583.As2805WoolworthsPack.MsgType.NETWORK;
import static com.linkly.libengine.engine.protocol.iso8583.As2805WoolworthsPack.MsgType.PREAUTH;
import static com.linkly.libengine.engine.protocol.iso8583.As2805WoolworthsPack.MsgType.RECONCILIATION;
import static com.linkly.libengine.engine.protocol.iso8583.As2805WoolworthsPack.MsgType.REVERSAL;
import static com.linkly.libengine.engine.protocol.iso8583.As2805WoolworthsPack.isSecurityDisabled;
import static com.linkly.libengine.engine.protocol.iso8583.As2805WoolworthsUtils.getCurrentDukptKeyIndex;
import static com.linkly.libengine.engine.protocol.iso8583.As2805WoolworthsUtils.getCurrentDukptKsn;
import static com.linkly.libengine.engine.protocol.iso8583.As2805WoolworthsUtils.incrementDukptKsn;
import static com.linkly.libengine.engine.protocol.iso8583.As2805WoolworthsUtils.tidOrMidChanged;
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
import static com.linkly.libengine.env.AS2805LogonState.LogonState.DUKPT_REGISTRATION_REQUIRED;
import static com.linkly.libengine.env.AS2805LogonState.LogonState.LOGGED_ON;
import static com.linkly.libengine.env.AS2805LogonState.LogonState.LOGON_REQUIRED;
import static com.linkly.libengine.env.AS2805LogonState.LogonState.SESSION_KEY_LOGON;
import static com.linkly.libpositive.messages.IMessages.BATCH_UPLOAD_EVENT;
import static com.linkly.libpositive.wrappers.PositiveTransResult.JournalType.NONE;
import static com.linkly.libsecapp.IP2PEncrypt.EncryptForStorageType.CVV_FORMAT;
import static com.linkly.libsecapp.IP2PEncrypt.EncryptForStorageType.FULL_TRACK_FORMAT;
import static com.linkly.libsecapp.IP2PSec.KeyGroup.DYNAMIC_GROUP;
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
import static com.linkly.libui.UIScreenDef.KEY_INJECTION_REQUIRED;
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
import com.linkly.libengine.engine.transactions.properties.TSec;
import com.linkly.libengine.env.AS2805LogonState;
import com.linkly.libengine.env.BatchNumber;
import com.linkly.libengine.env.IncrementDukptKsn;
import com.linkly.libengine.env.LastUsedMerchantId;
import com.linkly.libengine.env.ReceiptNumber;
import com.linkly.libengine.env.Stan;
import com.linkly.libengine.env.LastUsedTerminalId;
import com.linkly.libengine.helpers.ECRHelpers;
import com.linkly.libengine.jobs.EFTJobScheduleEvent;
import com.linkly.libengine.overrides.CoreOverrides;
import com.linkly.libengine.status.IStatus;
import com.linkly.libmal.MalFactory;
import com.linkly.libmal.global.util.Util;
import com.linkly.libpositive.events.PositiveEvent;
import com.linkly.libpositive.events.PositiveTransEvent;
import com.linkly.libpositive.messages.IMessages;
import com.linkly.libpositive.wrappers.PositiveLogonResult;
import com.linkly.libpositive.wrappers.PositiveTransResult;
import com.linkly.libpositive.wrappers.TagDataToPOS;
import com.linkly.libsecapp.IP2PEncrypt;
import com.linkly.libsecapp.P2PLib;
import com.linkly.libsecapp.emv.Tag;
import com.linkly.libui.IUIDisplay;
import com.linkly.libui.UIScreenDef;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import timber.log.Timber;

/* only class to edit TProtocol.MessageStatus */

public class As2805WoolworthsProto implements IProto {

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


    private static final String TAG = As2805WoolworthsProto.class.getSimpleName();
    private static final boolean SPOOF_COMMS_FAIL_ON_AUTH = false;
    private ByteBuffer lastTxMessage;
    private ByteBuffer lastRxMessage;
    private IDependency d = null;
    private IUIDisplay ui = null;
    private static final int NUM_HOSTS = 1;
    private final As2805WoolworthsRspCodeMap RSP_CODE_MAP = new As2805WoolworthsRspCodeMap();
    private static TProtocol.HostResult lastBatchUploadHostResult = NOT_SET;
    private static final int periodicBatchUploadTimeoutSec = 10*60; // hardcoded 10mins for now
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

        // if terminal was in logged on state, revert back to logon required on power up
        if( AS2805LogonState.getCurValue() == LOGGED_ON ) {
            AS2805LogonState.setNewValue(LOGON_REQUIRED);
        }
        return true;
    }

    // Figures out the date and time and applies the timezone
    public void syncClock(TransRec trans) {

        if( trans != null &&
            trans.getProtocol().getYear() != null &&
            trans.getProtocol().getBankDate() != null &&
            trans.getProtocol().getBankTime() != null
        ) {
            Timber.i( "Data for Syncing clock:" +
                    d.getPayCfg().getBankTimeZone() + " " +
                    d.getPayCfg().getTerminalTimeZone() + " " +
                    trans.getProtocol().getYear() + trans.getProtocol().getBankDate().substring(2) + trans.getProtocol().getBankTime());

            // Make sure the data passed to us is valid
            if (Util.validDateFormat(trans.getProtocol().getYear() // YYYY format
                    + trans.getProtocol().getBankDate().substring(2) // .getBankDate() returns YYMMDD format. Chop off year as we have above
                    + trans.getProtocol().getBankTime(), "yyyyMMddHHmmss") &&
                    Util.validTimezone(d.getPayCfg().getBankTimeZone()) &&
                    Util.validTimezone(d.getPayCfg().getTerminalTimeZone())
            ) {
                // We need to switch the system clock to be bank zone before setting the time.
                // Reason we do this is because there are some issues with daylight savings resulting the clock being off an hour as setting the time how no concept of daylight savings.
                // Eg switching from sydney with bank time (queensland) 1:59 am just before clock goes back will result in sydney time not being updated correctly.
                MalFactory.getInstance().getHardware().setTimeZone(d.getPayCfg().getBankTimeZone());
                // Concatenate the string
                Date date = Util.convertForTimezone(trans.getProtocol().getYear() + trans.getProtocol().getBankDate().substring(2) + trans.getProtocol().getBankTime(), d.getPayCfg().getBankTimeZone());
                // Set the system time with the raw time
                if ( !MalFactory.getInstance().getHardware().setSystemDateTime( date ) ) {
                    Timber.e( "setSystemDateTime failed");
                }
                // Switch the timezone back to our original timezone
                MalFactory.getInstance().getHardware().setTimeZone(d.getPayCfg().getTerminalTimeZone());
            } else {
                Timber.e( "Invalid data for syncing clock:" +
                        d.getPayCfg().getBankTimeZone() + " " +
                        d.getPayCfg().getTerminalTimeZone() + " " +
                        trans.getProtocol().getYear() + trans.getProtocol().getBankDate().substring(2) + trans.getProtocol().getBankTime());
            }
        } else {
            Timber.e( "Time not set");
        }
    }

    /* perform any tasks that this protocol must do before doing an auth */
    public boolean preAuthorize(TransRec trans) {
        return true;
    }

    @Override
    public void authorizeOffline(TransRec trans, TProtocol.AuthMethod authMethod) {
        Timber.e("*** Authorising offline **");

        // set 'deferred auth' flag to true, if operating started in transit/flight/offline mode, and issuer setting says process this as deferred auth
        boolean processAsDeferredAuth = trans.isStartedInOfflineMode() && TDeferredAuth.getDeferredAuthConfigFlag(trans, d.getPayCfg());
        Timber.e("processing as deferred auth = %b", processAsDeferredAuth);
        trans.setDeferredAuth(processAsDeferredAuth);

        // if authorised post comms, set response code to Y3
        if( authMethod == OFFLINE_POSTCOMMS_AUTHORISED ) {
            trans.setProtocol(new As2805WoolworthsRspCodeMap().populateProtocolRecord(trans.getProtocol(), "Y3", trans.getTransType()));
        } else if( authMethod == OFFLINE_PRECOMMS_AUTHORISED ) {
            trans.setProtocol(new As2805WoolworthsRspCodeMap().populateProtocolRecord(trans.getProtocol(), "Y1", trans.getTransType()));
        } else if (EngineManager.TransType.COMPLETION_AUTO.equals(trans.getTransType()) || EngineManager.TransType.COMPLETION.equals(trans.getTransType())) {
            // Using 00 Approved for pre-auth completion (offline)
            trans.setProtocol(new As2805WoolworthsRspCodeMap().populateProtocolRecord(trans.getProtocol(), "00", trans.getTransType()));
        } else {
            Timber.e("Unexpected authMethod %s", authMethod);
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

        As2805WoolworthsUtils.updateRetRefNumber(trans);
        trans.updateMessageStatus(MessageStatus.ADVICE_QUEUED);
        setBankDateTime(trans);
    }

    private void setBankDateTime(TransRec trans) {
        // assign from trans audit Datetime
        Date date = new Date(trans.getAudit().getTransDateTime());
        As2805WoolworthsUtils.setBankDateAndTime(date, trans);
    }

    private void setReversalBankDateTime(TransRec trans) {
        // assign from trans audit reversal Datetime
        Date date = new Date(trans.getAudit().getReversalDateTime());
        As2805WoolworthsUtils.setBankDateAndTime(date, trans);
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

    /* perform an authorization for this particular transaction */
    /* if its set to an advice then no need to go online */
    /* once we are about to go online then set flag to say it has been sent */
    /* if we need to sendTCP reversals and make it a new transaction message then we do so */
    public boolean authorize(TransRec trans) {
        boolean result = false;
        TProtocol protocol = trans.getProtocol();

        // check if DUKPT keys are loaded, or exhausted
        if (dukptKeyLoadRequired()) {
            // revert logon state to prevent EFB transactions
            AS2805LogonState.setNewValue(DUKPT_REGISTRATION_REQUIRED);
            // better to decline rather than allow EFB
            protocol.setHostResult(DECLINED);
            d.getProtocol().setInternalRejectReason(trans, RejectReasonType.KEY_INJECTION_REQUIRED);
            trans.updateMessageStatus(FINALISED);
            trans.save();
            return false;
        }

        encryptCardData(Engine.getDep(), trans);

        // set batch number (settlement date) on trans record to current/last known batch for auto cutover testing and to ensure it's not automatically allocated an incorrect one
        trans.getProtocol().setBatchNumber(BatchNumber.getCurValue());


        if (trans.getTransType() == EngineManager.TransType.TESTCONNECT) {
            //----------------------------------- TEST CONNECT processing ---------------------------------------------------
            return performTestConnect(trans);
        } else if ( trans.getTransType() == AUTO_LOGON ||
                trans.getTransType() == LOGON ||
                trans.getTransType() == EngineManager.TransType.RSA_LOGON ) {

            d.getDebugReporter().reportDebugEvent( IDebug.DEBUG_EVENT.LOGON_STARTED, null );
            d.getStatusReporter().reportStatusEvent( IStatus.STATUS_EVENT.STATUS_LOGON_STARTED , trans.isSuppressPosDialog() );

            // Perform registration check, for logon processing
            if (trans.isLogon() && isKeyRegistrationRequired()) {
                AS2805LogonState.setNewValue(DUKPT_REGISTRATION_REQUIRED);
                Timber.w("DUKPT registration required! Hence not updating the logon state as logon required");
            } else {
                AS2805LogonState.setNewValue(LOGON_REQUIRED);
            }

            result = false;

            if (!d.getComms().connect(d,1)) {
                protocol.setHostResult(CONNECT_FAILED);
            } else {
                result = performLogon( trans, false );
                d.getComms().disconnect(d);
            }
            return result;

        } else if ( trans.isReconciliation() ) {
            //----------------------------------- RECONCILIATION/SETTLEMENT processing ---------------------------------------------------
            protocol.setAuthMethod(OFFLINE_PRECOMMS_AUTHORISED);
            trans.updateMessageStatus(MessageStatus.REC_QUEUED);

            ui.showScreen(CONNECTING_TO_HOST_PLEASE_WAIT);

            if (!d.getComms().connect(d,0)) {
                protocol.setHostResult(CONNECT_FAILED);
            } else {
                if (sendReconciliation(trans, false) || CoreOverrides.get().isSpoofComms()) {
                    trans.updateMessageStatus(FINALISED);
                }
            }
            result = true;
        } else if (trans.isReversal()) {
            //----------------------------------- REVERSAL processing ---------------------------------------------------
            TransRec transToReverse = TransRecManager.getInstance().getTransRecDao().getByReceiptNumber(trans.getAudit().getReversalReceiptNumber());
            if (transToReverse != null) {
                if (transToReverse.isReversible()) {
                    Timber.i( "original txn isReversible!" );

                    /* update the transaction we want to reverse */
                    transToReverse.setToReverse(CUSTOMER_CANCELLATION);
                    transToReverse.setApproved(false); // set approved flag for original trans to false/declined

                    // update this txn record (reversal txn)
                    if (trans.getAudit().getReceiptNumber() == -1) {
                        trans.getAudit().setReceiptNumber(ReceiptNumber.getNewValue());
                    }

                    As2805WoolworthsUtils.updateRetRefNumber(transToReverse);
                    As2805WoolworthsUtils.updateRetRefNumber(trans);
                    protocol.setAuthMethod(OFFLINE_PRECOMMS_AUTHORISED);

                    // Need to set the current record as "authorised" and "finalised".
                    // Allows the current transaction to be considered a successful void/reversal.
                    trans.updateMessageStatus(FINALISED);
                    trans.getProtocol().setHostResult(AUTHORISED);

                    // take original msg type and stan from original txn
                    trans.getProtocol().setOriginalStan(transToReverse.getProtocol().getOriginalStan());
                    trans.getProtocol().setOriginalMessageType(transToReverse.getProtocol().getOriginalMessageType());
                    setReversalBankDateTime(trans);
                    trans.setProtocol( this.RSP_CODE_MAP.populateProtocolRecord( trans.getProtocol(), "00" ) );
                    transToReverse.save();
                    result = true;

                } else {
                    Timber.e( "original txn isReversible is FALSE" );
                }
            }
        } else if (trans.getProtocol().isCanAuthOffline()) {
            //----------------------------------- OFFLINE AUTH processing ---------------------------------------------------
            // maybe generate an auth code
            protocol.setAuthMethod(OFFLINE_PRECOMMS_AUTHORISED);
            trans.getAudit().setReceiptNumber(ReceiptNumber.getNewValue());
            As2805WoolworthsUtils.updateRetRefNumber(trans);
            trans.updateMessageStatus(MessageStatus.ADVICE_QUEUED);

            // if completion txn, set response code to 00
            // TODO: when we authorise other transactions offline, e.g. pre-comms below floor limit, then EMV/CTLS code should set internal response code to Y1
            if( trans.isCompletion() ) {
                trans.setProtocol(this.RSP_CODE_MAP.populateProtocolRecord(trans.getProtocol(), "00"));
            }
            result = true;
        } else {
            //----------------------------------- else financial txn handling ---------------------------------------------------

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
                        continue;
                    }

                    protocol.setHostResult(NO_RESPONSE);
                    trans.updateMessageStatus(AUTH_SENT);
                    trans.save();

                    // Transaction is saved in the doTxRx after the packing the possible reversal data but before being sent.
                    if (doTxRx(trans, trans.isPreAuth()?PREAUTH:AUTH, false, false, Response) && !CoreOverrides.get().isSpoofCommsAuthAll()) {
                        // got a response, process the result
                        trans.updateMessageStatus(WAITING_FOR_FINISH);
                        processServerResponseCodes(trans);

                        Timber.i( "TRUE");
                        result = true;
                        break;
                    }
                    else if (CoreOverrides.get().isSpoofComms() || CoreOverrides.get().isSpoofCommsAuthAll()) {
                        // spoofing comms
                        spoofAuth(trans);
                        trans.updateMessageStatus(WAITING_FOR_FINISH);
                        result = true;
                        break;
                    } else {
                        // tx/rx failed - no (valid) host message received to act on
                        // if host result = no response, then we need to send a reversal
                        if( protocol.getHostResult() == NO_RESPONSE ) {
                            trans.setToReverse(COMMS_FAIL);
                        }
                        d.getComms().disconnect(d);
                        break;
                    }
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
        if( trans.isPreAuth() ) {
            RfnTag rfnTag = new RfnTag(trans.getAudit().getTerminalId(), trans.getProtocol().getRRN(), trans.getProtocol().getAuthCode());
            TagDataToPOS tagDataToPOS = new TagDataToPOS();
            tagDataToPOS.setRFN(rfnTag.pack());
            trans.setTagDataToPos(tagDataToPOS);
        }

        return true;
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
        return TransRecManager.getInstance().getTransRecDao().findByTidRrnAuthNumber(EngineManager.TransType.PREAUTH, PREAUTH_AUTO, PREAUTH_MOTO, PREAUTH_MOTO_AUTO, rfnTag.tid, rfnTag.rrn, rfnTag.authNumber);
    }

    @Override
    public ProtoResult batchUpload(boolean silent) {
        return batchUpload(this, silent);
    }

    /* go through the transactions in the DB and upload as many as possible */
    /* if protocol state is not finalised then sendTCP to protocol to  deal with */
    @SuppressWarnings({"java:S3776", "java:S135", "fallthrough"})
    // java:S3776: Cognitive complexity(70)
    // java:S135: Loops should not contain more than a single "break" or "continue" statement
    public static ProtoResult batchUpload(As2805WoolworthsProto proto, boolean silent) {
        boolean success = true;
        int uploadedCount = 0;
        boolean spoof = CoreOverrides.get().isSpoofComms();
        boolean disconnectRequired = false;

        // deal with possible async calls (from Periodic Batch Upload)
        if (!allowToContinueBatchUpload(proto, silent)) {
            success = false;
        }
        else {
            batchUploadInProgress = true;
            batchUploadRequestToCancel = false;

            lastBatchUploadHostResult = NOT_SET;
            TransRec currentTrans = proto.d.getCurrentTransaction();

            List<TransRec> allTrans = TransRecManager.getInstance().getTransRecDao().findAllNotMessageStatus(FINALISED);

            if (allTrans != null) {

                try {
                    // connect only if we need to
                    if (!proto.d.getComms().isConnected(proto.d)) {
                        proto.d.getComms().connect(proto.d, 0);
                        disconnectRequired = true;
                    }

                    for (TransRec trans : allTrans) {
                        // check if there is a request to interrupt Batch Upload
                        if (batchUploadRequestToCancel) {
                            success = false;
                            break;
                        }

                        /* this is to sop background threads uploading the current transaction whilst it is processing */
                        /* foreground threads need to do it, so that reversals get sent inline for every auth */
                        if (currentTrans != null && !trans.isFinalised() && silent) {
                            Timber.i("DONT UPLOAD transaction yet as its not finished");
                            continue;
                        }

                        TProtocol protocol = trans.getProtocol();
                        MessageStatus messageStatus = protocol.getMessageStatus();
                        PositiveTransResult.JournalType type = NONE;
                        if (messageStatus == REC_QUEUED) {
                            if (proto.sendReconciliation(trans, silent) || spoof) {
                                uploadedCount++;
                                trans.updateMessageStatus(FINALISED);
                                // These are just settlements (AKA zreports)
                                // Note: trans.rec is generated inside the send reconciliation function
                                ECRHelpers.ipcSendReportResponse(proto.d, trans, trans.getReconciliation(), IMessages.ReportType.ZReport.toString(), MalFactory.getInstance().getMalContext());
                            } else {
                                success = false;
                                break;
                            }
                        } else if (messageStatus == REVERSAL_QUEUED || messageStatus == AUTH_SENT) {
                            // Make sure that LogonState is correct
                            if (AS2805LogonState.getCurValue() != LOGGED_ON) {
                                // if we fail our online logon, we can just exit.
                                // Host will reject anyway
                                if(!proto.performInlineLogon()) {
                                    success = false;
                                    Timber.w("Bank logon required and failed. exiting back upload");
                                    break;
                                }
                            }

                            if (proto.doTxRx(trans, REVERSAL, silent, false, proto.Response) || spoof) {
                                uploadedCount++;
                                trans.updateMessageStatus(FINALISED_AND_REVERSED);
                                type = PositiveTransResult.JournalType.REVERSAL;
                            } else {
                                success = false;
                                proto.processServerResponseCodes(trans);
                                trans.save(); // e.g. update advice attempts
                                break;
                            }
                        }

                        // DO NOT put this in the else if chain above, because a message could transition from REVERSAL_QUEUED to ADVICE_QUEUED above and we want to process it
                        if (messageStatus == ADVICE_QUEUED) {
                            if (proto.doTxRx(trans, ADVICE, silent, false, proto.Response) || spoof) {
                                uploadedCount++;
                                proto.processServerResponseCodes(trans);
                                trans.updateMessageStatus(FINALISED);
                                type = PositiveTransResult.JournalType.ADVICE;
                            } else {
                                success = false;
                                trans.save(); // e.g. update advice attempts
                                break;
                            }
                        }

                        // Notify any other application what has happened
                        if (type != NONE) {
                            // Note: Journal type does retain its state
                            trans.setJournalType(type);
                            ECRHelpers.ipcSendTransResponse(proto.d, trans, MalFactory.getInstance().getMalContext());
                        }

                        lastBatchUploadHostResult = trans.getProtocol().getHostResult();
                        trans.save();
                    }

                } catch (Exception e) {
                    Timber.w(e);
                    success = false;
                }

                if (disconnectRequired) {
                    proto.d.getComms().disconnect(proto.d);
                }
            }

            if (spoof) {
                success = true;
            }

            Timber.i("BatchUpload returned: " + success + " and sent: " + uploadedCount + " transactions");
            batchUploadInProgress = false;
        }

        proto.setupNextBatchUpload();

        return success ? PROTO_SUCCESS : PROTO_FAIL;
    }

    private void setupNextBatchUpload() {
        long startTime = new Date().getTime();
        long triggerTime = startTime + periodicBatchUploadTimeoutSec*1000;

        EFTJobScheduleEvent nextBatchUploadEvent = new EFTJobScheduleEvent(EFTJobScheduleEvent.EventType.UPDATE, BATCH_UPLOAD_EVENT, triggerTime);
        d.getJobs().schedule(MalFactory.getInstance().getMalContext(), nextBatchUploadEvent);
    }

    private static boolean allowToContinueBatchUpload(As2805WoolworthsProto proto, boolean silent) {
        boolean allow = true;
        if (batchUploadInProgress) {
            // "silent" is true for scheduled batch uploads
            if (silent) {
                // we are scheduled Batch upload; do not interrupt any running one
                allow = false;
            }
            else {
                batchUploadRequestToCancel = true;
                int timeout = proto.d.getPayCfg().getPaymentSwitch().getReceiveTimeout();

                try {
                    long start = System.currentTimeMillis();
                    while (batchUploadInProgress) {
                        Thread.sleep(100L);
                        if (System.currentTimeMillis() - start > timeout * 1000 * 2) {
                            // waited two comms timeouts, safety break allowing to continue with batch upload regardless
                            Timber.i("Timed out waiting for scheduled Batch Upload interruption");
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
        if (doTxRx(reconciliationTrans, RECONCILIATION, silent, false, Response)) {
            processServerResponseCodes(reconciliationTrans);
            reconciliationTrans.updateMessageStatus(MessageStatus.FINALISED);
        } else {
            success = false;
            reconciliationTrans.getProtocol().setHostResult(CONNECT_FAILED);
        }

        Timber.i( "Reconciliation returned: %s", success);
        return success;
    }

    /* complicated function used to match up with saveFutureMacs() in As2805WoolworthsPack */
    /* unique stan for every send */
    public void getNewStan(TransRec trans) {
        // assign a new stan if the trans record doesn't currently have one. E.g. for pin entry we need a stan, so stan would already have been set
        if( 0 == trans.getProtocol().getStan() ) {
            trans.getProtocol().setStan(Stan.getNewValue());
        }
    }

    /**
     * checks if dukpt keys are injected, and not exhausted (run out of key iterations)
     *
     * @return true = dukpt keys are okay to go, false = dukpt keys exhausted
     */
    private boolean dukptKeyLoadRequired() {
        return getCurrentDukptKeyIndex() < 0;
    }

    /**
     * checks if the next DUKPT KSN key increment would hit the key sequence number limit,
     * and a switch to the next loaded key would occur.
     *
     * Forward-looking - called before the increment and rollover to next key and reset
     * to key sequence number to 1 has occurred
     *
     * @return true = next iteration will cause key change
     */
    private boolean dukptKeyChangeRequired() {
        int currentDukptKeyIndex = getCurrentDukptKeyIndex();
        // return true if next key index will reach or exceed the 'switch at' limit
        return currentDukptKeyIndex + 1 >= DYNAMIC_GROUP.getSwitchAtKeyIteration();
    }

    /**
     * checks if the the current DUKPT key is 'new', i.e. has counter value 1
     *
     * @return true = key is new
     */
    private boolean dukptKeyIsNew() {
        return getCurrentDukptKeyIndex() == 1;
    }

    /**
     * checks if DUKPT ksn increment is required, and pre-increments before send/receive
     *
     * this must be called prior to send/receive of every message pair
     *
     * this is a bit more complex than you'd think, because the very first
     * message pair will use DUKPT key sequence number 1, and no pre-increment is required
     * in this case
     *
     * @param trans
     */
    private void incrementDukptKsnIfRequired(TransRec trans) throws IOException {
        // special case for KSN 1 - first time this key has been used
        // we don't want to increment if this is the first time it's been used, but we do want to on the 2nd use
        int currentDukptKeyIndex = getCurrentDukptKeyIndex();
        TSec sec = trans.getSecurity();

        Timber.i("getCurrentDukptKeyIndex = %d", currentDukptKeyIndex);
        if (currentDukptKeyIndex == 1){
            // check 'increment next time' flag
            boolean incrementRequired = IncrementDukptKsn.getCurValue();
            if (!incrementRequired) {
                // increment isn't required this time, but set to true so next time it will
                Timber.i("DUKPT ksn is 1, increment not required as first time it's used");
                IncrementDukptKsn.setNewValue(true);
                // set ksn 1
                String ksn = getCurrentDukptKsn();
                Timber.e("Transaction assigned KSN %s", ksn);
                sec.setKsn(ksn);
                return;
            } else {
                Timber.i("DUKPT ksn is 1, increment is required");
            }
        } else {
            // not on key 1, increment isn't required
            IncrementDukptKsn.setNewValue(false);
        }

        // every message pair with the host needs a NEW ksn number (i.e. counter value doesn't go backwards)
        // increment and store KSN in trans rec
        String newKsn = incrementDukptKsn();
        Timber.e("transaction assigned new KSN [%s]", newKsn);

        // if no KSN returned, indicates key injection is required, no key present
        if (newKsn == null || newKsn.isEmpty()){
            throw new IOException("Key injection required");
        }

        sec.setKsn(newKsn);

        // we might have rolled back to 1, e.g. when switching BDK's
        if (getCurrentDukptKeyIndex() == 1) {
            Timber.i("DUKPT key has rolled to next BDK, increment is required next time round, and reverting logon state");
            IncrementDukptKsn.setNewValue(true);
        }
    }

    /* sendTCP and receive a particular message type */
    @SuppressWarnings("java:S6541")
    public boolean doTxRx(TransRec trans, As2805WoolworthsPack.MsgType msgType, boolean silent, boolean registrationMsg, ResponseAction Response) {
        TProtocol protocol = trans.getProtocol();

        try {
            IComms icomms = d.getComms();

            if (!registrationMsg) {
                // get a unique stan every time we try to send, except if it's a registration (which uses zero stan)
                getNewStan(trans);
            }

            // assigns a new KSN if required
            incrementDukptKsnIfRequired(trans);

            final byte[] packedBuffer = As2805WoolworthsPack.pack(d, trans, msgType, registrationMsg);
            if (packedBuffer == null) {
                Timber.e( "Failed to pack, returning CONNECT_FAILED");
                // treat as connect failed - no reversal required because request wasn't sent
                protocol.setHostResult(CONNECT_FAILED);
                return false;
            }
            // update trans record with original trans data for reversal
            trans.save();

            if( SPOOF_COMMS_FAIL_ON_AUTH && (msgType == AUTH || msgType == PREAUTH ) ) {
                trans.setProtocol( this.RSP_CODE_MAP.populateProtocolRecord( trans.getProtocol(), "X0" ) );
                protocol.setHostResult(NO_RESPONSE);
                Timber.e( "doTxRx SPOOF_COMMS_FAIL_ON_AUTH set, returning NO_RESPONSE");
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

            if( icomms instanceof IpGatewayProxyComms ) {
                if (icomms.send(d,
                        d.getPayCfg().getPosCommsHostId(),
                        d.getPayCfg().getMid(), d.getPayCfg().getStid(),
                        msgType.getSendMsgId(), packedBuffer ) <= 0) {
                    Timber.e( "Failed to Send Packed Buffer IPGW proxy comms");
                    return false;
                }
            } else {
                if (icomms.send(d, packedBuffer) <= 0) {
                    Timber.e( "Failed to Send Packed Buffer");

                    // treat as connect failed - no reversal required because request wasn't sent
                    protocol.setHostResult(CONNECT_FAILED);
                    return false;
                }

            }

            // if advice (0220 or reversal 0420 advice), increment advice attempt counter
            if( msgType == ADVICE || msgType == REVERSAL ) {
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


                    As2805WoolworthsPack.UnPackResult unPackResult = As2805WoolworthsPack.unpack(d, response, trans, Response, msgType);
                    switch ( unPackResult ){
                        case UNPACK_OK:
                            // check if host has cut over settlement date
                            checkForHostSettlement(trans);
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
                    completed = true;
                }
            } while( !completed );

        } catch (Exception e) {
            Timber.e( "doTxRx Exception" );
            Timber.e(e);
            protocol.setHostResult( NO_RESPONSE );
        }
        Timber.e( "doTxRx returning false");
        return false;
    }

    private void reportDebugEvent(As2805WoolworthsPack.MsgType msgType) {
        if (msgType == ADVICE) {
            d.getDebugReporter().reportDebugEvent(IDebug.DEBUG_EVENT.ADVICE_UPLOAD, null);
        } else if (msgType == REVERSAL) {
            d.getDebugReporter().reportDebugEvent(IDebug.DEBUG_EVENT.REVERSAL_STARTED, null);
        }
    }

    private void checkForHostSettlement(TransRec trans) {
        int batchNumber = trans.getProtocol().getBatchNumber();

        // don't do this if batchNumber not returned from host, or current txn is a reconciliation
        if( batchNumber == -1 || trans.isReconciliation() ) {
            return;
        }

        // if batch number received doesn't match what terminal currently has, host has cut over
        Timber.i( "Host settlement date check . Terminal value (last known):" + BatchNumber.getCurValue() + ", Host value (from msg):" + batchNumber );
        if( batchNumber != BatchNumber.getCurValue() ) {
            Timber.i( "HOST HAS CUT OVER THE SETTLEMENT DATE. Prev:" + BatchNumber.getCurValue() + ", Host val:" + batchNumber );

            // update batch number env var to new value
            BatchNumber.setNewValue(batchNumber);

            // if there are any unsettled transactions, then do background settlement now
            // get unreconciled totals
            IDailyBatch dailyBatch = new DailyBatch();
            Reconciliation recData = dailyBatch.generateDailyBatch(false, d);
            if( recData.getTotalAmount() > 0 && recData.getTotalCount() > 0 ) {
                Timber.i( "HOST HAS CUT OVER THE SETTLEMENT DATE - UNRECONCILED TRANSACTIONS FOUND. DOING BACKGROUND SETTLEMENT NOW");

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

    private boolean do0800Msg(TransRec trans, boolean noUi, boolean registrationMsg) {
        // set stan to zero so it gets a new one with each msg
        trans.getProtocol().setStan(0);

        if( !doTxRx(trans, NETWORK, noUi, registrationMsg, Response) ) {
            trans.getProtocol().setHostResult( CONNECT_FAILED );
            trans.updateMessageStatus( FINALISED );
            return false;
        }

        // if not "00" then treat as declined
        // N3 Config file update required response code to be treated as approved. We will be handling the config file update via Maxstore
        // instead of financial host
        if( (!"00".equals(trans.getProtocol().getServerResponseCode()) && (!"N3".equals(trans.getProtocol().getServerResponseCode())) )) {
            // if not 00 approved resp code, return failure
            Timber.i( "0810 nmic 170 returned error response code %s", trans.getProtocol().getServerResponseCode());
            trans.updateMessageStatus( FINALISED );
            return false;
        }
        return true;
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
                isSecurityDisabled( d ) ||
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
            trans.updateMessageStatus( FINALISED );
            return false;
        }

        trans.getProtocol().setHostResult( NO_RESPONSE );
        trans.updateMessageStatus( AUTH_SENT );

        do {
            AS2805LogonState.LogonState curState = AS2805LogonState.getCurValue();
            Timber.i( "AS2805 logon state = %s", curState.toString() );

            switch ( curState ) {
                case DUKPT_REGISTRATION_REQUIRED, LOGON_REQUIRED:
                    boolean isRegistration = curState == DUKPT_REGISTRATION_REQUIRED;
                    // 0800 nmic 170
                    if ( do0800Msg(trans, noUi, isRegistration) ) {
                        // Reset the stan with the new logon value
                        if ( trans.getProtocol().getResetStan() != null ) {
                            Stan.setNewValue( trans.getProtocol().getResetStan() );
                        }
                        // Sync the clock with the bank
                        syncClock( trans );
                        // all logon states (registration or normal logon required) go straight to 'logged on' if successful
                        AS2805LogonState.setNewValue( LOGGED_ON );
                        if (isRegistration) {
                            // save current tid/mid after successful registration
                            LastUsedTerminalId.setNewValue(d.getPayCfg().getStid());
                            LastUsedMerchantId.setNewValue(d.getPayCfg().getMid());
                        }
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

                if (doTxRx(trans, NETWORK, false, false, Response) || CoreOverrides.get().isSpoofComms()) {
                    trans.getProtocol().setHostResult(AUTHORISED);
                    trans.updateMessageStatus(FINALISED);
                    d.getComms().disconnect(d);

                    return true;
                }
            }
        }
        trans.getProtocol().setHostResult(CONNECT_FAILED);
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

    private boolean spoofAuth(TransRec trans) {

        if (CoreOverrides.get().isSpoofComms() || CoreOverrides.get().isSpoofCommsAuthAll()) {
            if (trans.getTransType() == EngineManager.TransType.RECONCILIATION) {
                trans.getProtocol().setHostResult(TProtocol.HostResult.RECONCILED_IN_BALANCE);
                trans.getProtocol().setPosResponseCode("00");
                trans.getProtocol().setServerResponseCode("500");
                return true;
            }

            if ((trans.getAmounts().getTotalAmount() % 100) == 65) {
                trans.getProtocol().setPosResponseCode(String.format("%02d", (trans.getAmounts().getTotalAmount() % 100)));
                trans.getProtocol().setServerResponseCode(String.format("%02d", (trans.getAmounts().getTotalAmount() % 100)));
                trans.getProtocol().setHostResult(DECLINED);
            } else {
                trans.getProtocol().setAuthMethod(ONLINE_AUTHORISED);
                trans.getProtocol().setPosResponseCode("00");
                trans.getProtocol().setServerResponseCode("00");
                trans.getProtocol().setAuthCode("123456");
                trans.getProtocol().setHostResult(AUTHORISED);
            }
            return true;
        }
        return false;
    }

    private void setAuthorisedFlags(TransRec trans) {
        TProtocol protocol = trans.getProtocol();
        trans.getProtocol().setAuthMethod(ONLINE_AUTHORISED);
        protocol.setHostResult(TProtocol.HostResult.AUTHORISED);
        if( trans.isReconciliation() ) {
            protocol.setHostResult(TProtocol.HostResult.RECONCILED_IN_BALANCE);
        }
    }

    void processServerResponseCodes(TransRec trans) {
        TProtocol protocol = trans.getProtocol();

        if (spoofAuth(trans)) {
            return;
        }

        String actionCode = protocol.getServerResponseCode();
        if( Util.isNullOrEmpty(actionCode) ) {
            // shouldn't really happen, but if it does, then default to declined
            Timber.e( "actionCode is NULL" );
            protocol.setHostResult(DECLINED);
            return;
        }

        // else all other trans types
        switch( actionCode ) {

            case "65":
                // 65 -The terminal should prompts the cardholder to perform a contact transaction
                trans.getProtocol().setPosResponseCode("65");
                trans.getProtocol().setServerResponseCode( "65" );
                protocol.setHostResult(DECLINED);
                break;

            case "76":
                // 76 indicates txn approved, and session key change required
                Timber.d( "host request key update NMIC 101" );
                AS2805LogonState.setNewValue(SESSION_KEY_LOGON);
                setAuthorisedFlags(trans);
                // IAAS-1528: Map 76 to 00
                trans.getProtocol().setPosResponseCode("00");
                trans.getProtocol().setServerResponseCode( "00" );
                trans.getProtocol().setPosResponseText("APPROVED");
                break;

            case "08":
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

            case "00":
            case "11":
                // 'standard' approved values
                // if signature CVM was set, and config says signature is NOT supported
                if( trans.getCard().getCvmType() == TCard.CvmType.SIG && d.getPayCfg() != null && !d.getPayCfg().isSignatureSupported()) {
                    // .. then set NO CVM as host has responded approved
                    trans.getCard().setCvmType(NO_CVM);
                }
                setAuthorisedFlags(trans);
                break;

            case "96":
            case "98":
            case "X7": // MAC verification failure
                AS2805LogonState.setNewValue(LOGON_REQUIRED);
                break;

            case "97":
                protocol.setHostResult(TProtocol.HostResult.RECONCILE_FAILED_TERMINAL_ALREADY_SETTLED);
                break;

            default:
                // default to declined
                protocol.setHostResult(DECLINED);
                break;
        }

    }

    public String getEmvProcessingCode(TransRec trans) {
        try {
            ProcessingCode pc = As2805WoolworthsUtils.packProcCode(trans);
            return pc.getTranType();
        } catch (Exception e) {
            Timber.w(e);
        }
        /* default to sale */
        return "00";
    }

    public String calculateRRN(TransRec trans) {
        try {
            return As2805WoolworthsUtils.calculateRetRefNumber(trans);
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

        byte[] encCvvResult = p2pEncrypt.encryptForStorage(CVV_FORMAT);

        if (encCvvResult != null) {
            trans.getSecurity().setCvv(Util.byteArrayToHexString(encCvvResult));
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
            return SvfeUtils.packIccData_common(d, trans, null, false);
        } catch (Exception e) {
            Timber.w(e);
        }
        return null;
    }

    @Override
    public void saveSignatureDeclined( TransRec transRec ) {}

    @Override
    public boolean performProtocolChecks() {
        TransRec transRec = d.getCurrentTransaction();
        boolean ret = true;
        boolean isLogon = d.getCurrentTransaction() != null && d.getCurrentTransaction().isLogon();
        boolean isSuppressPosDialog = d.getCurrentTransaction() != null && d.getCurrentTransaction().isSuppressPosDialog();

        // if the KSN key iteration we're about to do would reset/roll to next dukpt key, revert logon state
        // to ensure a 'registration' logon occurs
        if (isKeyRegistrationRequired()) {
            AS2805LogonState.setNewValue(DUKPT_REGISTRATION_REQUIRED);
        }

        // check if DUKPT keys are loaded, or exhausted
        if (dukptKeyLoadRequired()) {
            // revert logon state to prevent EFB transactions
            Timber.e("Reverting logon state to DUKPT_REGISTRATION_REQUIRED as DUKPT keys not present. Key load then registration 0800 required");
            AS2805LogonState.setNewValue(DUKPT_REGISTRATION_REQUIRED);

            ui.showScreen(KEY_INJECTION_REQUIRED);
            d.getStatusReporter().reportStatusEvent(IStatus.STATUS_EVENT.STATUS_ERR_KEY_INJECTION_REQUIRED, isSuppressPosDialog);
            d.getDebugReporter().reportDebugEvent(IDebug.DEBUG_EVENT.LOGON_STARTED, "FAILED, KEY INJECTION REQUIRED");
            d.getProtocol().setInternalRejectReason( transRec, IProto.RejectReasonType.KEY_INJECTION_REQUIRED );
            return false;
        }

        // if a logon of some type is required AND this txn is not a logon txn itself
        if (AS2805LogonState.getCurValue() != LOGGED_ON && !isLogon && !isSecurityDisabled(d)) {
            // then do a logon
            d.getDebugReporter().reportDebugEvent( IDebug.DEBUG_EVENT.LOGON_STARTED, null );
            d.getStatusReporter().reportStatusEvent( IStatus.STATUS_EVENT.STATUS_LOGON_STARTED, isSuppressPosDialog);
            if (!performInlineLogon()) {
                ret = false;
                ui.showScreen(LOGON_FAILED);
                d.getStatusReporter().reportStatusEvent(IStatus.STATUS_EVENT.STATUS_ERR_LOGON_FAILED, isSuppressPosDialog);
                d.getDebugReporter().reportDebugEvent(IDebug.DEBUG_EVENT.LOGON_STARTED, "FAILED");
                d.getProtocol().setInternalRejectReason( transRec, IProto.RejectReasonType.PROTOCOL_TASKS_FAILED );
            }
        }
        return ret;
    }

    private boolean isKeyRegistrationRequired() {
        boolean keyRegistrationRequired = false;
        if (dukptKeyChangeRequired()) {
            Timber.e("Reverting logon state to DUKPT_REGISTRATION_REQUIRED as current DUKPT key is exhausted. Registration 0800 required on next msg");
            keyRegistrationRequired = true;
        } else if (tidOrMidChanged(d)) {
            Timber.e("Reverting logon state to DUKPT_REGISTRATION_REQUIRED as TID or MID changed. Registration 0800 required");
            keyRegistrationRequired = true;
        } else if (dukptKeyIsNew() && !IncrementDukptKsn.getCurValue()) { // DUKPT Key is new and only when its not yet set to increment
            Timber.e("Reverting logon state to DUKPT_REGISTRATION_REQUIRED as new key present (KSN counter value 1). Registration 0800 required");
            keyRegistrationRequired = true;
        }
        return keyRegistrationRequired;
    }

    @Override
    public void setInternalRejectReason( TransRec trans, RejectReasonType rejectReasonType) {
        if (trans == null || trans.getProtocol() == null) {
            Timber.e( "Either trans was null [%b] or protocol was null", trans == null );
            return;
        }
        trans.setProtocol( this.RSP_CODE_MAP.populateProtocolRecord( trans.getProtocol(), rejectReasonType ) );
    }

    @Override
    public void setInternalRejectReason(TransRec trans, RejectReasonType rejectReasonType, String errorText) {
        if (trans == null || trans.getProtocol() == null) {
            Timber.e( "Either trans was null [%b] or protocol was null", trans == null );
            return;
        }
        trans.setProtocol( this.RSP_CODE_MAP.populateProtocolRecord( trans.getProtocol(), rejectReasonType, errorText ) );
    }

    @Override
    public int getMaxBatchNumber() {
        return 999999;
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
            inlineLogonTrans.setTransType(EngineManager.TransType.AUTO_LOGON);
            boolean result = this.performLogon(inlineLogonTrans, true);
            // We need to send an inline response here.
            ECRHelpers.ipcSendLogonResponse(d, inlineLogonTrans, MalFactory.getInstance().getMalContext(), false);
            return result;
        } else {
            Timber.d("Protocol Object = " + inlineLogonTrans.getProtocol() + " Audit object = " + inlineLogonTrans.getAudit());
            return false;
        }
    }
}
