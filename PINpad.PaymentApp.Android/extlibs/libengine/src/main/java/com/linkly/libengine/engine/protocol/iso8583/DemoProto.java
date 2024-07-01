package com.linkly.libengine.engine.protocol.iso8583;

import static com.linkly.libengine.engine.EngineManager.TransType.AUTO_LOGON;
import static com.linkly.libengine.engine.EngineManager.TransType.LOGON;
import static com.linkly.libengine.engine.EngineManager.TransType.PREAUTH_AUTO;
import static com.linkly.libengine.engine.EngineManager.TransType.PREAUTH_MOTO;
import static com.linkly.libengine.engine.EngineManager.TransType.PREAUTH_MOTO_AUTO;
import static com.linkly.libengine.engine.protocol.IProto.ProtoResult.PROTO_FAIL;
import static com.linkly.libengine.engine.protocol.IProto.ProtoResult.PROTO_SUCCESS;
import static com.linkly.libengine.engine.protocol.iso8583.As2805TillPack.MsgType.ADVICE;
import static com.linkly.libengine.engine.protocol.iso8583.As2805TillPack.MsgType.AUTH;
import static com.linkly.libengine.engine.protocol.iso8583.As2805TillPack.MsgType.NETWORK;
import static com.linkly.libengine.engine.protocol.iso8583.As2805TillPack.MsgType.PREAUTH;
import static com.linkly.libengine.engine.protocol.iso8583.As2805TillPack.MsgType.RECONCILIATION;
import static com.linkly.libengine.engine.protocol.iso8583.As2805TillPack.MsgType.REVERSAL;
import static com.linkly.libengine.engine.protocol.iso8583.As2805TillPack.isSecurityDisabled;
import static com.linkly.libengine.engine.protocol.iso8583.As2805TillUtils.calculateRetRefNumber;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.AuthMethod.OFFLINE_EFB_AUTHORISED;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.AuthMethod.OFFLINE_NO_COMMS_ATTEMPTED;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.AuthMethod.OFFLINE_POSTCOMMS_AUTHORISED;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.AuthMethod.OFFLINE_PRECOMMS_AUTHORISED;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.AuthMethod.ONLINE_AUTHORISED;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.HostResult.AUTHORISED;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.HostResult.CONNECT_FAILED;
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
import static com.linkly.libengine.env.AS2805LogonState.LogonState.KEK_LOGON;
import static com.linkly.libengine.env.AS2805LogonState.LogonState.KTM_LOGON;
import static com.linkly.libengine.env.AS2805LogonState.LogonState.LOGGED_ON;
import static com.linkly.libengine.env.AS2805LogonState.LogonState.LOGON_REQUIRED;
import static com.linkly.libengine.env.AS2805LogonState.LogonState.RSA_LOGON;
import static com.linkly.libengine.env.AS2805LogonState.LogonState.SESSION_KEY_LOGON;
import static com.linkly.libpositive.messages.IMessages.BATCH_UPLOAD_EVENT;
import static com.linkly.libpositive.wrappers.PositiveTransResult.JournalType.NONE;
import static com.linkly.libsecapp.IP2PEncrypt.EncryptForStorageType.CVV_FORMAT;
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
import static com.linkly.libui.UIScreenDef.CONNECTING_TO_HOST_PLEASE_WAIT;
import static com.linkly.libui.UIScreenDef.CONNECTING_TO_HOST_SENDING_MSG;
import static com.linkly.libui.UIScreenDef.CONNECTING_TO_HOST_WAITING_FOR_RESPONSE;
import static com.linkly.libui.UIScreenDef.LOGON_FAILED;

import com.linkly.libengine.debug.IDebug;
import com.linkly.libengine.dependencies.IDependency;
import com.linkly.libengine.engine.Engine;
import com.linkly.libengine.engine.EngineManager;
import com.linkly.libengine.engine.protocol.IProto;
import com.linkly.libengine.engine.protocol.iso8583.openisoj.ProcessingCode;
import com.linkly.libengine.engine.protocol.svfe.SvfeUtils;
import com.linkly.libengine.engine.reporting.DailyBatch;
import com.linkly.libengine.engine.reporting.IDailyBatch;
import com.linkly.libengine.engine.reporting.Reconciliation;
import com.linkly.libengine.engine.transactions.TransRec;
import com.linkly.libengine.engine.transactions.TransRecManager;
import com.linkly.libengine.engine.transactions.properties.TProtocol;
import com.linkly.libengine.env.AS2805LogonState;
import com.linkly.libengine.env.BatchNumber;
import com.linkly.libengine.env.ReceiptNumber;
import com.linkly.libengine.env.Stan;
import com.linkly.libengine.helpers.ECRHelpers;
import com.linkly.libengine.jobs.EFTJobScheduleEvent;
import com.linkly.libengine.status.IStatus;
import com.linkly.libmal.MalFactory;
import com.linkly.libmal.global.util.Util;
import com.linkly.libpositive.events.PositiveEvent;
import com.linkly.libpositive.events.PositiveScheduledEvent;
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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import timber.log.Timber;

/* only class to edit TProtocol.MessageStatus */
public class DemoProto implements IProto {
    private static final int COMMS_CONNECT_EMULATION_PAUSE = 300;
    private static final int COMMS_RECEIVE_EMULATION_PAUSE = 300;
    private static final boolean CONVERT_CENTS_TO_RESPONSE_CODE = true;

    public static final String FAILED_TO_SEND_PACKED_BUFFER = "Failed to Send Packed Buffer";

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
        public RfnTag(String tid, String rrn, String authNumber) {
            this.tid = tid;
            this.rrn = rrn;
            this.authNumber = authNumber;
        }

        /**
         * unpack constructor
         *
         * @param packedRfn packed RFN tag in above format
         */
        public RfnTag(String packedRfn) {
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
            } catch (Exception e) {
                Timber.e(e, "parse of RFN threw exception");
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

    private IDependency d = null;
    private IUIDisplay ui = null;
    private ByteBuffer lastTxMessage;
    private ByteBuffer lastRxMessage;
    private final As2805TillRspCodeMap rspCodeMap = new As2805TillRspCodeMap();
    private static final int PERIODIC_BATCH_UPLOAD_TIMEOUT_SEC = 10 * 60; // hardcoded 10mins for now
    private static boolean batchUploadInProgress = false;
    private static boolean batchUploadRequestToCancel = false;

    public boolean init(IDependency dependencies) {
        d = dependencies;
        ui = d.getFramework().getUI();

        // if terminal was in logged on state, revert back to logon required on power up
        if (AS2805LogonState.getCurValue() == LOGGED_ON) {
            AS2805LogonState.setNewValue(LOGON_REQUIRED);
        }
        return true;
    }

    // Figures out the date and time and applies the timezone
    public void syncClock(TransRec trans) {

        if (trans != null &&
                trans.getProtocol().getYear() != null &&
                trans.getProtocol().getBankDate() != null &&
                trans.getProtocol().getBankTime() != null
        ) {
            Timber.i("Data for Syncing clock:" +
                    d.getPayCfg().getBankTimeZone() + " " +
                    d.getPayCfg().getTerminalTimeZone() + " " +
                    trans.getProtocol().getYear() + trans.getProtocol().getBankDate().substring(2) + trans.getProtocol().getBankTime());

            // Make sure the data passed to us is valid
            if (Util.validDateFormat(trans.getProtocol().getYear() + trans.getProtocol().getBankDate().substring(2) + trans.getProtocol().getBankTime(), "yyyyMMddHHmmss") &&
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
                if (!MalFactory.getInstance().getHardware().setSystemDateTime(date)) {
                    Timber.e("setSystemDateTime failed");
                }
                // Switch the timezone back to our original timezone
                MalFactory.getInstance().getHardware().setTimeZone(d.getPayCfg().getTerminalTimeZone());
            } else {
                Timber.e("Invalid data for syncing clock:" +
                        d.getPayCfg().getBankTimeZone() + " " +
                        d.getPayCfg().getTerminalTimeZone() + " " +
                        trans.getProtocol().getYear() + trans.getProtocol().getBankDate().substring(2) + trans.getProtocol().getBankTime());
            }
        } else {
            Timber.e("Time not set");
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

    @Override
    public void authorizeOffline(TransRec trans, TProtocol.AuthMethod authMethod) {
        Timber.i("*** Authorising offline **");
        if(trans.isEfbAuthorisedTransaction()) {
            trans.setProtocol(this.rspCodeMap.populateProtocolRecord(trans.getProtocol(), "00"));
            trans.getProtocol().setHostResult(AUTHORISED);
        } else if (authMethod == OFFLINE_POSTCOMMS_AUTHORISED) {
            // if authorised post comms, set response code to Y3
            trans.setProtocol(new As2805TillRspCodeMap().populateProtocolRecord(trans.getProtocol(), "Y3"));
        } else if (authMethod == OFFLINE_PRECOMMS_AUTHORISED) {
            trans.setProtocol(new As2805TillRspCodeMap().populateProtocolRecord(trans.getProtocol(), "Y1"));
        }
        trans.getProtocol().setHostResult(AUTHORISED);

    // if reversal queued
        if (trans.getProtocol().getMessageStatus() == REVERSAL_QUEUED) {
            Timber.i("original txn was reversed, creating new approved advice record");
            createNewAdviceRecordAndApprove(trans); // NB updates current trans record to point to new approved advice record
        } else {
            setBankDateTime(trans);
            trans.updateMessageStatus(MessageStatus.ADVICE_QUEUED);
        }

        //----------------------------------- OFFLINE AUTH processing ---------------------------------------------------
        // maybe generate an auth code
        trans.getProtocol().setAuthMethod(authMethod);
        // allocate new receipt number. increments for each transaction. reverts back to 1 when batch closed
        trans.getAudit().setReceiptNumber(ReceiptNumber.getNewValue());
        trans.getProtocol().setBatchNumber(BatchNumber.getCurValue());

        As2805TillUtils.updateRetRefNumber(trans);
        trans.updateMessageStatus(MessageStatus.ADVICE_QUEUED);
        setBankDateTime(trans);
    }

    private void setBankDateTime(TransRec trans) {
        // assign from trans audit Datetime
        Date date = new Date(trans.getAudit().getTransDateTime());
        DateFormat dateFormat = new SimpleDateFormat("yyMMddHHmmss", Locale.getDefault());
        String strDate = dateFormat.format(date);
        trans.getProtocol().setBankTime(strDate);
    }

    @Override
    public ProtoResult batchUpload(boolean silent) {
        return batchUpload(this, silent);
    }

    /* perform an authorization for this particular transaction */
    /* if its set to an advice then no need to go online */
    /* once we are about to go online then set flag to say it has been sent */
    /* if we need to sendTCP reversals and make it a new transaction message then we do so */
    @SuppressWarnings({"java:S3776", "java:S135"})
    // java:S3776: Cognitive complexity(70)
    // java:S135: Loops should not contain more than a single "break" or "continue" statement
    public boolean authorize(TransRec trans) {

        boolean result = false;
        TProtocol protocol = trans.getProtocol();

        encryptCardData(Engine.getDep(), trans);

        // set batch number (settlement date) on trans record to current/last known batch for auto cutover testing and to ensure it's not automatically allocated an incorrect one
        trans.getProtocol().setBatchNumber(BatchNumber.getCurValue());

        // NOT_SET, AUTH_SENT, REVERSAL_QUEUED, WAITING_FOR_FINISH, ADVICE_QUEUED, FINALISED

        if (trans.getTransType() == EngineManager.TransType.TESTCONNECT) {
            //----------------------------------- TEST CONNECT processing ---------------------------------------------------
            return performTestConnect(trans);
        } else if (trans.getTransType() == AUTO_LOGON ||
                trans.getTransType() == LOGON ||
                trans.getTransType() == EngineManager.TransType.RSA_LOGON) {

            d.getDebugReporter().reportDebugEvent(IDebug.DEBUG_EVENT.LOGON_STARTED, null);
            d.getStatusReporter().reportStatusEvent(IStatus.STATUS_EVENT.STATUS_LOGON_STARTED , trans.isSuppressPosDialog());

            // perform RKI (if required). Do this before socket connect because it takes a few seconds and remote end will hang up if RKI is performed
            P2PLib.getInstance().getIP2PSec().as2805GetKeys(d.getCustomer().getTcuKeyLength());

            switch (trans.getTransType()) {
                case RSA_LOGON:
                    // set rsa logon required flag
                    AS2805LogonState.setNewValue(RSA_LOGON);
                    break;
                case AUTO_LOGON:
                case LOGON:
                default:
                    switch (AS2805LogonState.getCurValue()) {
                        case RSA_LOGON:
                        case KTM_LOGON:
                        case KEK_LOGON:
                        case LOGON_REQUIRED:
                            // rsa isn't fully completed, revert to base 'rsa logon'
                            AS2805LogonState.setNewValue(RSA_LOGON);
                            break;
                        case SESSION_KEY_LOGON:
                            // do nothing
                            break;
                        case FILE_UPDATE_REQUIRED:
                        case LOGGED_ON:
                            // set logon 101 (session key change) required flag
                            AS2805LogonState.setNewValue(SESSION_KEY_LOGON);
                            break;
                    }
                    break;
            }

            result = performLogon(trans, false);
            return result;

        } else if (trans.isReconciliation()) {
            //----------------------------------- RECONCILIATION/SETTLEMENT processing ---------------------------------------------------
            protocol.setAuthMethod(OFFLINE_PRECOMMS_AUTHORISED);
            trans.updateMessageStatus(MessageStatus.REC_QUEUED);

            ui.showScreen(CONNECTING_TO_HOST_PLEASE_WAIT);

            if (sendReconciliation(trans, false)) {
                trans.updateMessageStatus(FINALISED);
            }
            result = true;
        } else if (trans.isReversal()) {
            //----------------------------------- REVERSAL processing ---------------------------------------------------
            TransRec transToReverse = TransRecManager.getInstance().getTransRecDao().getByReceiptNumber(trans.getAudit().getReversalReceiptNumber());
            if (transToReverse != null) {
                if (transToReverse.isReversible()) {
                    Timber.i("original txn isReversible!");

                    // update the transaction we want to reverse
                    trans.setToReverse(CUSTOMER_CANCELLATION);
                    trans.setFinalised(false);
                    As2805TillUtils.updateRetRefNumber(trans);

                    // update this txn record (reversal txn)
                    if (trans.getAudit().getReceiptNumber() == -1) {
                        trans.getAudit().setReceiptNumber(ReceiptNumber.getNewValue());
                    }
                    As2805TillUtils.updateRetRefNumber(trans);
                    protocol.setAuthMethod(OFFLINE_PRECOMMS_AUTHORISED);

                    // take original msg type and stan from original txn
                    trans.getProtocol().setOriginalStan(transToReverse.getProtocol().getOriginalStan());
                    trans.getProtocol().setOriginalMessageType(transToReverse.getProtocol().getOriginalMessageType());
                    trans.setProtocol(this.rspCodeMap.populateProtocolRecord(trans.getProtocol(), "00"));
                    result = true;

                } else {
                    Timber.e("original txn isReversible is FALSE");
                }
            }
        } else if (trans.getProtocol().isCanAuthOffline()) {
            //----------------------------------- OFFLINE AUTH processing ---------------------------------------------------
            // maybe generate an auth code
            if (protocol.getAuthMethod() != OFFLINE_EFB_AUTHORISED) {   // preserve AuthMethod
                protocol.setAuthMethod(OFFLINE_PRECOMMS_AUTHORISED);
            }
            protocol.setHostResult(AUTHORISED);
            trans.getAudit().setReceiptNumber(ReceiptNumber.getNewValue());
            As2805TillUtils.updateRetRefNumber(trans);
            trans.updateMessageStatus(MessageStatus.ADVICE_QUEUED);

            // if completion txn, set response code to 00
            if (trans.isCompletion()) {
                trans.setProtocol(this.rspCodeMap.populateProtocolRecord(trans.getProtocol(), "00"));
            }
            // Skip other steps(as in eftex). Interface method AuthorizeOffline will be called by workflow post card 2nd Gen AC step if offline auth is required
            result = true;
        } else {
            //----------------------------------- else financial txn handling ---------------------------------------------------

            trans.save();

            UIScreenDef screenDef = CONNECTING_TO_HOST_PLEASE_WAIT;
            ui.showScreen(screenDef);

            /* this will attempt to clear out any reversals as necessary */
            batchUpload(this, false);

            protocol.setHostResult(NO_RESPONSE);
            trans.updateMessageStatus(AUTH_SENT);
            trans.save();

            if (doTxRx(trans, trans.isPreAuth() ? PREAUTH : AUTH, false, false, false)) {
                // emulating healthy comms
                trans.updateMessageStatus(WAITING_FOR_FINISH);
                result = true;
            } else {
                // tx/rx "failed" - emulating "no response"
                if (protocol.getHostResult() == NO_RESPONSE) {
                    trans.setToReverse(COMMS_FAIL);
                }
            }
        }

        trans.save();

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
        if (trans.isPreAuth()) {
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
        if (!rfnTag.parse()) {
            return null;
        }

        // do database lookup based on rfn tag values
        return TransRecManager.getInstance().getTransRecDao().findByTidRrnAuthNumber(EngineManager.TransType.PREAUTH, PREAUTH_AUTO,PREAUTH_MOTO,PREAUTH_MOTO_AUTO,rfnTag.tid, rfnTag.rrn, rfnTag.authNumber);
    }


    /* go through the transactions in the DB and upload as many as possible */
    /* if protocol state is not finalised then sendTCP to protocol to  deal with */
    @SuppressWarnings({"java:S3776", "java:S135"})
    // java:S3776: Cognitive complexity(74)
    // java:S135: Loops should not contain more than a single "break" or "continue" statement
    public static ProtoResult batchUpload(DemoProto demoProto, boolean silent) {

        // NOT_SET, AUTH_SENT, REVERSAL_QUEUED, WAITING_FOR_FINISH, ADVICE_QUEUED, AUTH_QUEUED, FINALISED
        boolean success = true;
        int uploadedCount = 0;

        // deal with possible async calls (from Periodic Batch Upload)
        if (!allowToContinueBatchUpload(demoProto, silent)) {
            success = false;
        } else {
            batchUploadInProgress = true;
            batchUploadRequestToCancel = false;

            TransRec currentTrans = demoProto.d.getCurrentTransaction();

            List<TransRec> allTrans = TransRecManager.getInstance().getTransRecDao().findAllNotMessageStatus(FINALISED);

            if (allTrans != null) {
                try {
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
                            if (demoProto.sendReconciliation(trans, silent)) {
                                uploadedCount++;
                                trans.updateMessageStatus(FINALISED);
                                // These are just settlements (AKA zreports)
                                // Note: trans.rec is generated inside the send reconciliation function
                                ECRHelpers.ipcSendReportResponse(demoProto.d, trans, trans.getReconciliation(), IMessages.ReportType.ZReport.toString(), MalFactory.getInstance().getMalContext());
                            } else {
                                success = false;
                                break;
                            }
                        } else if (messageStatus == REVERSAL_QUEUED || messageStatus == AUTH_SENT) {
                            // Make sure that LogonState is correct
                            if (AS2805LogonState.getCurValue() != LOGGED_ON && !demoProto.performInlineLogon()) {
                                // if we fail our online logon, we can just exit.
                                // Host will reject anyway
                                success = false;
                                Timber.w("Bank logon required and failed. exiting back upload");
                                break;
                            }

                            if (demoProto.doTxRx(trans, REVERSAL, silent, false, false)) {
                                uploadedCount++;
                                trans.updateMessageStatus(FINALISED_AND_REVERSED);
                                type = PositiveTransResult.JournalType.REVERSAL;
                            } else {
                                success = false;
                                demoProto.processServerResponseCodes(trans);
                                trans.save(); // e.g. update advice attempts
                                break;
                            }
                        }

                        // DO NOT put this in the else if chain above, because a message could transition from REVERSAL_QUEUED to ADVICE_QUEUED above and we want to process it
                        if (messageStatus == ADVICE_QUEUED) {
                            if (demoProto.doTxRx(trans, ADVICE, silent, false, false)) {
                                uploadedCount++;
                                demoProto.processServerResponseCodes(trans);
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
                            ECRHelpers.ipcSendTransResponse(demoProto.d, trans, MalFactory.getInstance().getMalContext());
                        }

                        trans.save();
                    }

                } catch (Exception e) {
                    Timber.i(e);
                    success = false;
                }

            }

            Timber.i("BatchUpload returned: " + success + " and sent: " + uploadedCount + " transactions");
            batchUploadInProgress = false;
        }

        demoProto.setupNextBatchUpload();

        return success ? PROTO_SUCCESS : PROTO_FAIL;
    }

    private void setupNextBatchUpload() {
        long startTime = new Date().getTime();
        long triggerTime = startTime + PERIODIC_BATCH_UPLOAD_TIMEOUT_SEC * 1000;

        EFTJobScheduleEvent nextBatchUploadEvent = new EFTJobScheduleEvent(PositiveScheduledEvent.EventType.UPDATE, BATCH_UPLOAD_EVENT, triggerTime);
        d.getJobs().schedule(MalFactory.getInstance().getMalContext(), nextBatchUploadEvent);
    }

    @SuppressWarnings("java:S3776") // Cognitive complexity(17) needs to be addressed later
    private static boolean allowToContinueBatchUpload(DemoProto as2805TillProto, boolean silent) {
        boolean allow = true;
        if (batchUploadInProgress) {
            // "silent" is true for scheduled batch uploads
            if (silent) {
                // we are scheduled Batch upload; do not interrupt any running one
                allow = false;
            } else {
                batchUploadRequestToCancel = true;
                int timeout = as2805TillProto.d.getPayCfg().getPaymentSwitch().getReceiveTimeout();

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
                } catch (InterruptedException e) {
                    Timber.i("Thread interrupted exception: %s", e.getMessage());
                    Thread.currentThread().interrupt();
                } catch (Exception e) {
                    Timber.i("General exception: %s", e.getMessage());
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
        if (doTxRx(reconciliationTrans, RECONCILIATION, silent, false, false)) {
            processServerResponseCodes(reconciliationTrans);
            reconciliationTrans.updateMessageStatus(MessageStatus.FINALISED);
        } else {
            success = false;
            reconciliationTrans.getProtocol().setHostResult(CONNECT_FAILED);
            reconciliationTrans.setProtocol( this.rspCodeMap.populateProtocolRecord( reconciliationTrans.getProtocol(), "CE" ) );
        }

        Timber.i("Reconciliation returned: %s", success);
        return success;
    }

    /* complicated function used to match up with saveFutureMacs() in As2805TillPack */
    /* unique stan for every send */
    public void getNewStan(TransRec trans) {
        // assign a new stan if the trans record doesn't currently have one. E.g. for pin entry we need a stan, so stan would already have been set
        if (0 == trans.getProtocol().getStan()) {
            trans.getProtocol().setStan(Stan.getNewValue());
        }
    }

    /* Emulate send and receive a particular message type */
    public boolean doTxRx(TransRec trans, As2805TillPack.MsgType msgType, boolean silent, boolean useZeroStan, boolean emulateNoResponse) {
        TProtocol protocol = trans.getProtocol();

        if (!useZeroStan) {
            // get a unique stan every time we try to send
            getNewStan(trans);
        }

        // update trans record with original trans data for reversal
        trans.save();

        if (!silent) {
            d.getStatusReporter().reportStatusEvent(IStatus.STATUS_EVENT.STATUS_PROCESSING , trans.isSuppressPosDialog());
            ui.showScreen(CONNECTING_TO_HOST_SENDING_MSG);
        }

        Util.Sleep(COMMS_CONNECT_EMULATION_PAUSE);

        // if advice (0220 or reversal 0420 advice), increment advice attempt counter
        if (msgType == ADVICE || msgType == REVERSAL) {
            protocol.setAdviceAttempts(protocol.getAdviceAttempts() + 1);
            trans.save();
        }

        if (!silent) {
            ui.showScreen(CONNECTING_TO_HOST_WAITING_FOR_RESPONSE);
        }

        Util.Sleep(COMMS_RECEIVE_EMULATION_PAUSE);

        if (!emulateNoResponse) {
            unpack(trans);
            checkForHostSettlement(trans);
            return true;
        }
        return false;
    }

    private void checkForHostSettlement(TransRec trans) {
        int batchNumber = trans.getProtocol().getBatchNumber();

        // don't do this if batchNumber not returned from host, or current txn is a reconciliation
        if (batchNumber == -1 || trans.isReconciliation()) {
            return;
        }

        // if batch number received doesn't match what terminal currently has, host has cut over
        Timber.i("Host settlement date check . Terminal value (last known):" + BatchNumber.getCurValue() + ", Host value (from msg):" + batchNumber);
        if (batchNumber != BatchNumber.getCurValue()) {
            Timber.i("HOST HAS CUT OVER THE SETTLEMENT DATE. Prev:" + BatchNumber.getCurValue() + ", Host val:" + batchNumber);

            // update batch number env var to new value
            BatchNumber.setNewValue(batchNumber);

            // if there are any unsettled transactions, then do background settlement now
            // get unreconciled totals
            IDailyBatch dailyBatch = new DailyBatch();
            Reconciliation recData = dailyBatch.generateDailyBatch(false, d);
            if (recData.getTotalAmount() > 0 && recData.getTotalCount() > 0) {
                Timber.i("HOST HAS CUT OVER THE SETTLEMENT DATE - UNRECONCILED TRANSACTIONS FOUND. DOING BACKGROUND SETTLEMENT NOW");

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

    private boolean do0800Msg(TransRec trans, boolean noUi, String nmic, boolean useZeroStan) {
        // set stan to zero so it gets a new one with each msg
        trans.getProtocol().setStan(0);
        if (!doTxRx(trans, NETWORK, noUi, useZeroStan, false)) {
            trans.getProtocol().setHostResult(CONNECT_FAILED);
            trans.setProtocol( this.rspCodeMap.populateProtocolRecord( trans.getProtocol(), "CE" ) );
            trans.updateMessageStatus(FINALISED);
            AS2805LogonState.setNewValue(RSA_LOGON); // revert back to initial state
            return false;
        }

        // if not "00" then treat as declined
        if (!"00".equals(trans.getProtocol().getServerResponseCode())) {
            // if not 00 approved resp code, return failure
            Timber.i("0810 nmic " + nmic + " returned error response code " + trans.getProtocol().getServerResponseCode());
            trans.updateMessageStatus(FINALISED);
            AS2805LogonState.setNewValue(RSA_LOGON); // revert back to initial state
            return false;
        }

        return true;
    }

    /**
     * Packs, sends a logon request to the bank
     * Will also unpack the response to the request
     *
     * @param trans {@link TransRec} Trans record
     * @param noUi  boolean to tell whether UI needs to be updated
     * @return true if successful
     */
    @SuppressWarnings("java:S3776") // Cognitive complexity(39) needs to be addressed later
    private boolean performLogon(TransRec trans, boolean noUi) {
        boolean result = true;

        if (isSecurityDisabled(d) ||
                AS2805LogonState.getCurValue() == LOGGED_ON) {
            // nothing to do, return success
            trans.getProtocol().setHostResult(AUTHORISED);
            trans.updateMessageStatus(FINALISED);
            return result;
        }

        trans.getProtocol().setHostResult(NO_RESPONSE);
        trans.updateMessageStatus(AUTH_SENT);

        do {
            AS2805LogonState.LogonState curState = AS2805LogonState.getCurValue();
            Timber.i("AS2805 logon state = %s", curState.toString());

            switch (curState) {
                case RSA_LOGON:
                    // 0800 nmic 191
                    if (do0800Msg(trans, noUi, "191", true)) {
                        // move to next state
                        AS2805LogonState.setNewValue(KTM_LOGON);
                    } else {
                        result = false;
                    }
                    break;

                case KTM_LOGON:
                    // 0800 nmic 192
                    if (do0800Msg(trans, noUi, "192", true)) {
                        // move to next state
                        AS2805LogonState.setNewValue(KEK_LOGON);
                    } else {
                        result = false;
                    }
                    break;

                case KEK_LOGON:
                    // 0800 nmic 193
                    if (do0800Msg(trans, noUi, "193", true)) {
                        // move to next state
                        AS2805LogonState.setNewValue(LOGON_REQUIRED);
                    } else {
                        result = false;
                    }
                    break;

                case LOGON_REQUIRED:
                    // 0800 nmic 170
                    if (do0800Msg(trans, noUi, "170", true)) {
                        // Reset the stan with the new logon value
                        if (trans.getProtocol().getResetStan() != null) {
                            Stan.setNewValue(trans.getProtocol().getResetStan());
                        }
                        // Sync the clock with the bank
                        syncClock(trans);

                        AS2805LogonState.setNewValue(LOGGED_ON);
                    } else {
                        result = false;
                    }
                    break;

                case SESSION_KEY_LOGON:
                    // 0800 nmic 101
                    if (do0800Msg(trans, noUi, "101", true)) {
                        // move to next state
                        AS2805LogonState.setNewValue(LOGGED_ON);
                    } else {
                        result = false;
                    }
                    break;
                default:
                    Timber.d("Unknown state = %s", AS2805LogonState.getCurValue().name());
                    result = false;
                    break;
            }
        } while (AS2805LogonState.getCurValue() != LOGGED_ON && result);

        // if we get here, then all is good
        if (LOGGED_ON == AS2805LogonState.getCurValue() && result) {
            trans.getProtocol().setHostResult(AUTHORISED);
            trans.updateMessageStatus(FINALISED);
        }

        return result;
    }

    private boolean performTestConnect(TransRec trans) {
        trans.getProtocol().setHostResult(AUTHORISED);
        trans.updateMessageStatus(FINALISED);
        return true;
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

    void processServerResponseCodes(TransRec trans) {
        unpack(trans);
    }

    public String getEmvProcessingCode(TransRec trans) {
        try {
            ProcessingCode pc = As2805TillUtils.packProcCode(trans);
            return pc.getTranType();
        } catch (Exception e) {
            Timber.i(e);
        }
        /* default to sale */
        return "00";
    }

    public String calculateRRN(TransRec trans) {
        return calculateRetRefNumber();
    }

    public byte[] getLastTxMessage() {
        if (lastTxMessage == null)
            return new byte[0];
        return lastTxMessage.array();
    }

    public byte[] getLastRxMessage() {
        if (lastRxMessage == null)
            return new byte[0];
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
        P2PLib p2PLibInstance = P2PLib.getInstance();
        IP2PEncrypt p2pEncrypt = p2PLibInstance.getIP2PEncrypt();

        if (p2pEncrypt != null) {
            byte[] encResult = p2pEncrypt.encryptForStorage(FULL_TRACK_FORMAT);
            if (encResult != null) {
                trans.getSecurity().setEncTrack2(Util.byteArrayToHexString(encResult));
            }

            byte[] encCvvResult = p2pEncrypt.encryptForStorage(CVV_FORMAT);

            if (encCvvResult != null) {
                trans.getSecurity().setCvv(Util.byteArrayToHexString(encCvvResult));
            }
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
            Timber.i(e);
        }
        return null;
    }

    @Override
    public String saveCtlsTagValuesForDB(IDependency d, TransRec trans) {
        return saveEmvTagValuesForDB(d, trans);
    }

    @Override
    public void saveSignatureDeclined(TransRec transRec) {
        // No use case at the moment. If needed, we need to set appropriate response code & text after Signature CVM has been declined
    }

    @Override
    public boolean performProtocolChecks() {
        boolean ret = true;

        // if a logon of some type is required AND this txn is not a logon txn itself
        if (AS2805LogonState.getCurValue() != LOGGED_ON && !Engine.getDep().getCurrentTransaction().isLogon() && !isSecurityDisabled(d)) {
            // then do a logon
            Engine.getDep().getDebugReporter().reportDebugEvent(IDebug.DEBUG_EVENT.LOGON_STARTED, null);
            Engine.getDep().getStatusReporter().reportStatusEvent(IStatus.STATUS_EVENT.STATUS_LOGON_STARTED , d.getCurrentTransaction().isSuppressPosDialog());
            if (!performInlineLogon()) {
                ret = false;
                ui.showScreen(LOGON_FAILED);
                Engine.getDep().getStatusReporter().reportStatusEvent(IStatus.STATUS_EVENT.STATUS_ERR_LOGON_FAILED , d.getCurrentTransaction().isSuppressPosDialog());
                Engine.getDep().getDebugReporter().reportDebugEvent(IDebug.DEBUG_EVENT.LOGON_STARTED, "FAILED");
                TransRec transRec = Engine.getDep().getCurrentTransaction();

                if (transRec != null && transRec.getProtocol() != null) {
                    // According to specs, any response code other '00', '08', '11' is taken as declined
                    d.getProtocol().setInternalRejectReason(transRec, RejectReasonType.PROTOCOL_TASKS_FAILED);
                } else {
                    Timber.e("Either trans or protocol was null");
                }
            }
        }
        return ret;
    }

    @Override
    public void setInternalRejectReason(TransRec trans, RejectReasonType rejectReasonType) {
        trans.setProtocol(this.rspCodeMap.populateProtocolRecord(trans.getProtocol(), rejectReasonType));
    }

    @Override
    public void setInternalRejectReason(TransRec trans, RejectReasonType rejectReasonType, String errorText) {
        trans.setProtocol(this.rspCodeMap.populateProtocolRecord(trans.getProtocol(), rejectReasonType, errorText));
    }

    @Override
    public int getMaxBatchNumber() {
        return 999999;
    }

    /**
     * Performs an inline logon by generating a dummy {@link TransRec} object which is never saved
     *
     * @return true if Logon is authorised
     */
    private boolean performInlineLogon() {
        TransRec inlineLogonTrans = new TransRec(EngineManager.TransType.AUTO_LOGON, d);
        inlineLogonTrans.setTransEvent(new PositiveTransEvent(PositiveEvent.EventType.AUTO_LOGON));
        inlineLogonTrans.getTransEvent().setOperationType(PositiveLogonResult.OperationType.LOGON);

        Timber.d("Going to perform an AutoLogon");
        if (inlineLogonTrans.getProtocol() != null && inlineLogonTrans.getAudit() != null) {
            boolean result;

            // perform RKI (if required). Do this before socket connect because it takes a few seconds and remote end will hang up if RKI is performed
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

    public void unpack(TransRec trans) {
        TProtocol proto = trans.getProtocol();

        Date date = new Date(trans.getAudit().getTransDateTime());
        DateFormat timeFormat = new SimpleDateFormat("HHmmss", Locale.getDefault());
        String strTime = timeFormat.format(date);
        proto.setBankTime(strTime);

        DateFormat dateFormat = new SimpleDateFormat("yyMMdd", Locale.getDefault());
        String strDate = dateFormat.format(date);
        proto.setBankDate(strDate);

        if (trans.getTransType() == EngineManager.TransType.RECONCILIATION) {
            proto.setAuthMethod(ONLINE_AUTHORISED);
            proto.setHostResult(TProtocol.HostResult.RECONCILED_IN_BALANCE);
            proto.setPosResponseCode("00");
            proto.setServerResponseCode("500");
        } else {
            String responseCode = CONVERT_CENTS_TO_RESPONSE_CODE ? String.format("%02d",trans.getAmounts().getTotalAmount()%100) : "00";

            if ("08".equals(responseCode)) {
                proto.setSignatureRequired(true);
            }
            if ("00".equals(responseCode) || "08".equals(responseCode)) {
                proto.setAuthMethod(ONLINE_AUTHORISED);
                proto.setPosResponseCode(responseCode);
                proto.setServerResponseCode(responseCode);
                proto.setHostResult(AUTHORISED);
                proto.setAuthCode(String.format("%06d",proto.getStan()%1000000));   // use stan as authcode
                String rrn = calculateRRN(trans);
                trans.getProtocol().setRRN(rrn);
            } else {
                proto.setHostResult(TProtocol.HostResult.DECLINED);
                proto.setPosResponseCode(responseCode);
                proto.setServerResponseCode(responseCode);
                proto.setPosResponseText("DECLINED");
            }
        }
    }
}
