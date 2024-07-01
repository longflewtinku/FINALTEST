package com.linkly.libengine.engine.protocol.iso8583;

import static com.linkly.libengine.engine.protocol.IProto.ProtoResult.PROTO_FAIL;
import static com.linkly.libengine.engine.protocol.IProto.ProtoResult.PROTO_SUCCESS;
import static com.linkly.libengine.engine.protocol.iso8583.As2805SuncorpPack.MSGTYPE.ADVICE;
import static com.linkly.libengine.engine.protocol.iso8583.As2805SuncorpPack.MSGTYPE.AUTH;
import static com.linkly.libengine.engine.protocol.iso8583.As2805SuncorpPack.MSGTYPE.NETWORK;
import static com.linkly.libengine.engine.protocol.iso8583.As2805SuncorpPack.MSGTYPE.RECONCILIATION;
import static com.linkly.libengine.engine.protocol.iso8583.As2805SuncorpPack.MSGTYPE.REVERSAL;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.AuthMethod.OFFLINE_NO_COMMS_ATTEMPTED;
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
import static com.linkly.libpositive.wrappers.PositiveTransResult.JournalType.NONE;
import static com.linkly.libsecapp.IP2PEncrypt.EncryptForStorageType.FULL_TRACK_FORMAT;
import static com.linkly.libsecapp.emv.Tag.amt_auth_num;
import static com.linkly.libsecapp.emv.Tag.amt_other_num;
import static com.linkly.libsecapp.emv.Tag.appl_cryptogram;
import static com.linkly.libsecapp.emv.Tag.appl_intchg_profile;
import static com.linkly.libsecapp.emv.Tag.atc;
import static com.linkly.libsecapp.emv.Tag.auth_resp_code;
import static com.linkly.libsecapp.emv.Tag.crypt_info_data;
import static com.linkly.libsecapp.emv.Tag.cvm_results;
import static com.linkly.libsecapp.emv.Tag.df_name;
import static com.linkly.libsecapp.emv.Tag.issuer_app_data;
import static com.linkly.libsecapp.emv.Tag.issuer_script_results;
import static com.linkly.libsecapp.emv.Tag.term_cap;
import static com.linkly.libsecapp.emv.Tag.term_county_code;
import static com.linkly.libsecapp.emv.Tag.term_type;
import static com.linkly.libsecapp.emv.Tag.tran_date;
import static com.linkly.libsecapp.emv.Tag.tran_type;
import static com.linkly.libsecapp.emv.Tag.trans_category_code;
import static com.linkly.libsecapp.emv.Tag.trans_curcy_code;
import static com.linkly.libsecapp.emv.Tag.tvr;
import static com.linkly.libsecapp.emv.Tag.unpred_num;
import static com.linkly.libui.UIScreenDef.COMMS_FAILURE_RECEIVING_RESPONSE;
import static com.linkly.libui.UIScreenDef.CONNECTING_TO_HOST_1_PLEASE_WAIT;
import static com.linkly.libui.UIScreenDef.CONNECTING_TO_HOST_2_PLEASE_WAIT;
import static com.linkly.libui.UIScreenDef.CONNECTING_TO_HOST_PLEASE_WAIT;
import static com.linkly.libui.UIScreenDef.CONNECTING_TO_HOST_SENDING_MSG;
import static com.linkly.libui.UIScreenDef.CONNECTING_TO_HOST_WAITING_FOR_RESPONSE;

import com.linkly.libengine.action.TransactionDecliner;
import com.linkly.libengine.debug.IDebug;
import com.linkly.libengine.dependencies.IDependency;
import com.linkly.libengine.engine.Engine;
import com.linkly.libengine.engine.EngineManager;
import com.linkly.libengine.engine.comms.IComms;
import com.linkly.libengine.engine.protocol.IProto;
import com.linkly.libengine.engine.protocol.iso8583.openisoj.ProcessingCode;
import com.linkly.libengine.engine.protocol.svfe.SvfeUtils;
import com.linkly.libengine.engine.reporting.DailyBatch;
import com.linkly.libengine.engine.reporting.IDailyBatch;
import com.linkly.libengine.engine.reporting.Reconciliation;
import com.linkly.libengine.engine.transactions.TransRec;
import com.linkly.libengine.engine.transactions.TransRecManager;
import com.linkly.libengine.engine.transactions.properties.TProtocol;
import com.linkly.libengine.env.BatchNumber;
import com.linkly.libengine.env.ReceiptNumber;
import com.linkly.libengine.env.Stan;
import com.linkly.libengine.helpers.ECRHelpers;
import com.linkly.libengine.overrides.CoreOverrides;
import com.linkly.libengine.status.IStatus;
import com.linkly.libmal.MalFactory;
import com.linkly.libmal.global.util.Util;
import com.linkly.libpositive.events.PositiveEvent;
import com.linkly.libpositive.events.PositiveTransEvent;
import com.linkly.libpositive.messages.IMessages;
import com.linkly.libpositive.wrappers.PositiveLogonResult;
import com.linkly.libpositive.wrappers.PositiveTransResult;
import com.linkly.libsecapp.IP2PEncrypt;
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


public class As2805SuncorpProto implements IProto {

    private static final String TAG = "As2805SuncorpProto";
    public static TaskProtocolType protocolType;
    private ByteBuffer lastTxMessage;
    private ByteBuffer lastRxMessage;
    private IDependency d = null;
    private IUIDisplay ui = null;
    private static final int NUM_HOSTS = 1;
    private static boolean logonDone = false;

    public boolean init(IDependency dependencies) {
        d = dependencies;
        ui = d.getFramework().getUI();
        d.getComms().open(d);
        
        return true;
    }

    // Figures out the date and time and applies the timezone
    public void syncClock(TransRec trans) {

        if( trans != null &&
            trans.getProtocol().getYear() != null &&
            trans.getProtocol().getBankDate() != null &&
            trans.getProtocol().getBankTime() != null
        ) {

            // Make sure the data passed to us is valid
            if( Util.validDateFormat(trans.getProtocol().getYear() + trans.getProtocol().getBankDate() + trans.getProtocol().getBankTime(), "yyyyMMddhhmmss") &&
                Util.validTimezone(d.getPayCfg().getBankTimeZone()) &&
                Util.validTimezone(d.getPayCfg().getTerminalTimeZone())
            ) {
                // We need to switch the system clock to be bank zone before setting the time.
                // Reason we do this is because there are some issues with daylight savings resulting the clock being off an hour as setting the time how no concept of daylight savings.
                // Eg switching from sydney with bank time (queensland) 1:59 am just before clock goes back will result in sydney time not being updated correctly.
                MalFactory.getInstance().getHardware().setTimeZone(d.getPayCfg().getBankTimeZone());
                // Concatenate the string
                Date date = Util.convertForTimezone(trans.getProtocol().getYear() + trans.getProtocol().getBankDate() + trans.getProtocol().getBankTime(), d.getPayCfg().getBankTimeZone());
                // Set the system time with the raw time
                MalFactory.getInstance().getHardware().setSystemDateTime(date);
                // Switch the timezone back to our original timezone
                MalFactory.getInstance().getHardware().setTimeZone(d.getPayCfg().getTerminalTimeZone());
            } else {
                Timber.e( "Invalid data for syncing clock:" +
                        d.getPayCfg().getBankTimeZone() + " " +
                        d.getPayCfg().getTerminalTimeZone() + " " +
                        trans.getProtocol().getYear() + trans.getProtocol().getBankDate() + trans.getProtocol().getBankTime());
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
    }

    /* perform an authorization for this particular transaction */
    /* if its set to an advice then no need to go online */
    /* once we are about to go online then set flag to say it has been sent */
    /* if we need to sendTCP reversals and make it a new transaction message then we do so */
    public boolean authorize(TransRec trans) {

        boolean result = false;
        TProtocol protocol = trans.getProtocol();

        encryptCardData(Engine.getDep(), trans);

        // set batch number (settlement date) on trans record to current/last known batch for auto cutover testing and to ensure it's not automatically allocated an incorrect one
        trans.getProtocol().setBatchNumber(BatchNumber.getCurValue());



        /* if its a transaction to auth offline then very little to do */
        if (trans.getTransType() == EngineManager.TransType.TESTCONNECT) {
            //----------------------------------- TEST CONNECT processing ---------------------------------------------------
            return performTestConnect(trans);
        } else if ( trans.getTransType() == EngineManager.TransType.AUTO_LOGON ) {
            d.getDebugReporter().reportDebugEvent( IDebug.DEBUG_EVENT.LOGON_STARTED, null );
            d.getStatusReporter().reportStatusEvent( IStatus.STATUS_EVENT.STATUS_LOGON_STARTED , trans.isSuppressPosDialog());

            if( trans.getTransEvent().getOperationType() == PositiveLogonResult.OperationType.RSA_LOGON ) {
                Timber.d( "POS send a RSA logon request. This part still needs to be done" );
            }

            // Note: as this is a user initiated Logon, we wont need to broadcast anything as this happen in app logic once completed.
            boolean logonResult = performLogon( trans, false );

            if( logonResult ) {
                if(trans.getProtocol().getResetStan() != null) {
                    // Reset the stan with the new logon value
                    Stan.setNewValue(trans.getProtocol().getResetStan());
                }
                // Sync the clock with the bank
                syncClock(trans);

                batchUpload(false);
            }

            return logonResult;
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

                    /* update the transaction we want to reverse */
                    transToReverse.setToReverse(CUSTOMER_CANCELLATION);

                    if (trans.getAudit().getReceiptNumber() == -1) {
                        trans.getAudit().setReceiptNumber(ReceiptNumber.getNewValue());
                    }

                    As2805SuncorpUtils.updateRetRefNumber(transToReverse, d.getPayCfg());
                    As2805SuncorpUtils.updateRetRefNumber(trans, d.getPayCfg());

                    /* update the reversal transaction itself */
                    protocol.setAuthMethod(OFFLINE_PRECOMMS_AUTHORISED);
                    trans.updateMessageStatus(FINALISED);
                    result = true;
                    transToReverse.save();
                }
            }
        } else if (trans.getProtocol().isCanAuthOffline()) {
            //----------------------------------- OFFLINE AUTH processing ---------------------------------------------------
            // maybe generate an auth code
            protocol.setAuthMethod(OFFLINE_PRECOMMS_AUTHORISED);
            trans.getAudit().setReceiptNumber(ReceiptNumber.getNewValue());
            As2805SuncorpUtils.updateRetRefNumber(trans, d.getPayCfg());
            trans.updateMessageStatus(MessageStatus.ADVICE_QUEUED);
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
                    protocol.setHostResult(CONNECT_FAILED);
                } else {

                    /* this will attempt to clear out any reversals as necessary */
                    if (/*protocol.getMessageStatus() != AUTH_QUEUED && */batchUpload(false) != PROTO_SUCCESS) {
                        d.getComms().disconnect(d);
                        continue;
                    }

                    protocol.setHostResult(NO_RESPONSE);
                    trans.updateMessageStatus(AUTH_SENT);
                    /* save to DB at this point */
                    trans.save();

                    if (doTxRx(trans, AUTH, false) && !CoreOverrides.get().isSpoofCommsAuthAll()) {
                        processServerResponseCodes(trans);
                        trans.updateMessageStatus(WAITING_FOR_FINISH);

                        Timber.i( "TRUE");
                        result = true;
                        break;
                    }
                    else if (CoreOverrides.get().isSpoofComms() || CoreOverrides.get().isSpoofCommsAuthAll()) {
                        spoofAuth(trans);
                        trans.updateMessageStatus(WAITING_FOR_FINISH);
                        result = true;
                        break;
                    }
                    else {
                        d.getComms().disconnect(d);
                        protocol.setHostResult(NO_RESPONSE);
                        trans.setToReverse(COMMS_FAIL);
                    }
                }
            }
        }


        if (!result) {
            trans.getAudit().setRejectReasonType( IProto.RejectReasonType.DECLINED);
            d.getWorkflowEngine().setNextAction(TransactionDecliner.class);

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

        return true;
    }

    /* go through the transactions in the DB and upload as many as possible */
    /* if protocol state is not finalised then sendTCP to protocol to  deal with */
    public ProtoResult batchUpload(boolean silent) {


        boolean success = true;
        int uploadedCount = 0;
        boolean spoof = CoreOverrides.get().isSpoofComms();
        boolean disconnectRequired = false;

        TransRec currentTrans = d.getCurrentTransaction();

        List<TransRec> allTrans = TransRecManager.getInstance().getTransRecDao().findAllNotMessageStatus(FINALISED);

        if (allTrans != null) {

            try {
                // connect only if we need to
                if (!d.getComms().isConnected(d)) {
                    d.getComms().connect(d, 0);
                    disconnectRequired = true;
                }

                for (TransRec trans : allTrans) {

                    /* this is to sop background threads uploading the current transaction whilst it is processing */
                    /* foreground threads need to do it, so that reversals get sent inline for every auth */
                    if (currentTrans != null && !trans.isFinalised() && silent) {
                        Timber.i( "DONT UPLOAD transaction yet as its not finished");
                        continue;
                    }

                    TProtocol protocol = trans.getProtocol();
                    MessageStatus messageStatus = protocol.getMessageStatus();
                    PositiveTransResult.JournalType type = NONE;
                    if (messageStatus == REC_QUEUED) {
                        if (sendReconciliation(trans, silent) || spoof) {
                            uploadedCount++;
                            trans.updateMessageStatus(FINALISED);
                            // These are just settlements (AKA zreports)
                            // Note: trans.rec is generated inside the send reconciliation function
                            ECRHelpers.ipcSendReportResponse(d, trans, trans.getReconciliation(), IMessages.ReportType.ZReport.toString(), MalFactory.getInstance().getMalContext());
                        } else {
                            success = false;
                            break;
                        }
                    } else if (messageStatus == ADVICE_QUEUED) {
                        if (doTxRx(trans, ADVICE, silent) || spoof) {
                            uploadedCount++;
                            trans.updateMessageStatus(FINALISED);
                            type = PositiveTransResult.JournalType.ADVICE;
                        } else {
                            success = false;
                            break;
                        }
                    } else if (messageStatus == REVERSAL_QUEUED || messageStatus == AUTH_SENT) {
                        if (doTxRx(trans, REVERSAL, silent) || spoof) {
                            uploadedCount++;
                            trans.updateMessageStatus(FINALISED_AND_REVERSED);
                            type = PositiveTransResult.JournalType.REVERSAL;
                        } else {
                            success = false;
                            break;
                        }
                    }

                    // Notify any other application what has happened
                    if(type != NONE) {
                        // Note: Journal type does retain its state
                        trans.setJournalType(type);
                        ECRHelpers.ipcSendTransResponse(d, trans, MalFactory.getInstance().getMalContext());
                    }

                    trans.save();
                }

            } catch( Exception e ) {
                Timber.w(e);
                success = false;
            }

            if( disconnectRequired ) {
                d.getComms().disconnect(d);
            }
        }

        if (spoof) {
            success = true;
        }

        Timber.i( "BatchUpload returned: " + success + " and sent: " + uploadedCount + " transactions");


        return success ? PROTO_SUCCESS : PROTO_FAIL;
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
        if (doTxRx(reconciliationTrans, RECONCILIATION, silent)) {
            processServerResponseCodes(reconciliationTrans);
            reconciliationTrans.updateMessageStatus(MessageStatus.FINALISED);

        } else {
            success = false;
            reconciliationTrans.getProtocol().setHostResult(CONNECT_FAILED);
        }

        Timber.i( "Reconciliation returned: " + success);
        return success;
    }

    /* complicated function used to match up with saveFutureMacs() in As2805SuncorpPack */
    /* unique stan for every send */
    public void getNewStan(TransRec trans) {
        trans.getProtocol().setStan(Stan.getNewValue());
    }

    /* sendTCP and receive a particular message type */
    public boolean doTxRx(TransRec trans, As2805SuncorpPack.MSGTYPE msgType, boolean silent) {

        try {
            IComms icomms = d.getComms();

            Timber.i( "Going to Sleep");

            // get a unique stan every time we try to send
            getNewStan(trans);

            final byte[] packedBuffer = As2805SuncorpPack.pack(d,trans, msgType);
            if (packedBuffer == null) {
                Timber.i( "Failed to pack");
                return false;
            }

            /* store the packed buffer (to help with debugging) */
            lastTxMessage = ByteBuffer.wrap(packedBuffer);

            if (!silent) {
                ui.showScreen(CONNECTING_TO_HOST_SENDING_MSG);
            }

            Timber.i( "Send Packed Buffer:" + packedBuffer.length);

            if (icomms.send(d,packedBuffer) <= 0) {
                Timber.i( "Failed to Send Packed Buffer");
                return false;
            }

            if (!silent) {
                ui.showScreen(CONNECTING_TO_HOST_WAITING_FOR_RESPONSE);
            }

            byte[] response = icomms.recv(d);
            if (response != null) {

                /* store the packed buffer (to help with debugging) */
                lastRxMessage = ByteBuffer.wrap(response);


                if (As2805SuncorpPack.unpack(d, response, trans)) {
                    // valid message

                    // check if host has cut over settlement date
                    checkForHostSettlement(trans);

                    // unpacked response true, return success
                    return true;
                } else {
                    if (!silent) {
                        ui.showScreen(COMMS_FAILURE_RECEIVING_RESPONSE);
                        ui.getResultCode(IUIDisplay.ACTIVITY_ID.ACT_INFORMATION, IUIDisplay.SHORT_TIMEOUT);
                    }
                }
            }
        } catch (Exception e) {
            Timber.i( "doTxRx Exception");
            Timber.w(e);
        }
        return false;
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

    /**
     * Packs, sends a logon request to the bank
     * Will also unpack the response to the request
     * @param trans {@link TransRec} Trans record
     * @param noUi boolean to tell whether UI needs to be updated
     * @return true if successful
     * */
    private boolean performLogon( TransRec trans, boolean noUi ){
        /* connect the comms */
        if ( d.getComms().connect( d, 1 ) ) {

            trans.getProtocol().setHostResult(NO_RESPONSE);
            trans.updateMessageStatus(AUTH_SENT);
            /* save to DB at this point */

            if ( doTxRx( trans, NETWORK, noUi ) || CoreOverrides.get().isSpoofComms() ) {
                As2805SuncorpProto.logonDone = true;
                trans.getProtocol().setHostResult( AUTHORISED );
                trans.updateMessageStatus( FINALISED );
                d.getComms().disconnect(d);
                return true;
            }
        }
        trans.getProtocol().setHostResult( CONNECT_FAILED );
        trans.updateMessageStatus( FINALISED );
        return false;
    }

    private boolean performTestConnect(TransRec trans) {

        for (int i = 0; i < NUM_HOSTS; i++) {
            /* connect the comms */
            if (d.getComms().connect(d, i)) {

                trans.getProtocol().setHostResult(NO_RESPONSE);
                trans.updateMessageStatus(AUTH_SENT);
                /* save to DB at this point */

                if (doTxRx(trans, NETWORK, false) || CoreOverrides.get().isSpoofComms()) {
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

        ArrayList<Tag> tags = new ArrayList<Tag>();
        tags.add(trans_curcy_code);
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
        tags.add(auth_resp_code);
        tags.add(df_name);
        return tags;
    }

    private boolean spoofAuth(TransRec trans) {

        if (CoreOverrides.get().isSpoofComms() || CoreOverrides.get().isSpoofCommsAuthAll()) {
            if (trans.getTransType() == EngineManager.TransType.RECONCILIATION) {
                trans.getProtocol().setHostResult(TProtocol.HostResult.RECONCILED_IN_BALANCE);
                trans.getProtocol().setServerResponseCode("500");
                return true;
            }

            if ((trans.getAmounts().getTotalAmount() % 100) == 65) {
                trans.getProtocol().setServerResponseCode(String.format("%02d", (trans.getAmounts().getTotalAmount() % 100)));
                trans.getProtocol().setHostResult(TProtocol.HostResult.DECLINED);
            } else {
                trans.getProtocol().setAuthMethod(ONLINE_AUTHORISED);
                trans.getProtocol().setServerResponseCode("00");
                trans.getProtocol().setAuthCode("123456");
                trans.getProtocol().setHostResult(AUTHORISED);
            }
            return true;
        }
        return false;
    }

    void processServerResponseCodes(TransRec trans) {
        TProtocol protocol = trans.getProtocol();

        if (spoofAuth(trans)) {
            return;
        }

        String actionCode = protocol.getServerResponseCode();

        switch( actionCode ) {
            case "00":
            case "08":
            case "11":
                trans.getProtocol().setAuthMethod(ONLINE_AUTHORISED);
                protocol.setHostResult(TProtocol.HostResult.AUTHORISED);
                break;

            case "91":
                protocol.setHostResult(TProtocol.HostResult.ISSUER_UNAVAILABLE);
                break;

            case "97":
                trans.getProtocol().setAuthMethod(ONLINE_AUTHORISED);
                String settlementCode = protocol.getSettlementCode();
                int sc = !Util.isNullOrEmpty(settlementCode) ? Integer.valueOf(settlementCode) : 0;
                if( sc == 1 ) {
                    protocol.setHostResult(TProtocol.HostResult.RECONCILED_IN_BALANCE);
                } else {
                    protocol.setHostResult(TProtocol.HostResult.RECONCILED_OUT_OF_BALANCE);
                }
                break;

            case "Q5":
                protocol.setHostResult(TProtocol.HostResult.RECONCILE_FAILED_TERMINAL_ALREADY_SETTLED);
                break;

            case "95":
                protocol.setHostResult(TProtocol.HostResult.RECONCILE_FAILED_OUTSIDE_WINDOW);
                break;

            default:
                // default to declined
                protocol.setHostResult(TProtocol.HostResult.DECLINED);
                break;
        }

    }

    public String getEmvProcessingCode(TransRec trans) {
        try {
            ProcessingCode pc = As2805SuncorpUtils.packProcCode(trans);
            return pc.getTranType();
        } catch (Exception e) {
            Timber.w(e);
        }
        /* default to sale */
        return "00";
    }

    public String calculateRRN(TransRec trans) {
        try {
            return As2805SuncorpUtils.calculateRetRefNumber(trans, d.getPayCfg());
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
            return SvfeUtils.packIccData_common(d, trans, null, false);
        } catch (Exception e) {
            Timber.w(e);
        }
        return null;
    }

    @Override
    public void saveSignatureDeclined( TransRec transRec ) {
        if ( transRec != null && transRec.getProtocol() != null && transRec.getProtocol().isSignatureRequired() ) {
            final String SIGN_DECLINED_CODE = "34";
            As2805SuncorpRspCodeMap rspCodeMap = new As2805SuncorpRspCodeMap();

            transRec.getProtocol().setServerResponseCode( SIGN_DECLINED_CODE );

            // set display and receipt text based off response code
            transRec.getProtocol().setAdditionalResponseText( rspCodeMap.getResponseCodeErrorDisplay( SIGN_DECLINED_CODE ) );
            transRec.getProtocol().setCardAcceptorPrinterData( rspCodeMap.getResponseCodeErrorReceipt( SIGN_DECLINED_CODE ) );
            transRec.getProtocol().setPosResponseText( rspCodeMap.getResponseCodeErrorPos( SIGN_DECLINED_CODE ) );
        }
    }

    @Override
    public boolean performProtocolChecks() {
        if( !As2805SuncorpProto.logonDone && Engine.getDep().getCurrentTransaction().getTransType() != EngineManager.TransType.AUTO_LOGON ){
            Engine.getDep().getDebugReporter().reportDebugEvent( IDebug.DEBUG_EVENT.LOGON_STARTED, null );
            Engine.getDep().getStatusReporter().reportStatusEvent( IStatus.STATUS_EVENT.STATUS_LOGON_STARTED , d.getCurrentTransaction().isSuppressPosDialog() );
            As2805SuncorpProto.logonDone = this.performInlineLogon();
            if( !As2805SuncorpProto.logonDone ){
                Engine.getDep().getStatusReporter().reportStatusEvent( IStatus.STATUS_EVENT.STATUS_ERR_LOGON_FAILED, d.getCurrentTransaction().isSuppressPosDialog() );
                Engine.getDep().getDebugReporter().reportDebugEvent( IDebug.DEBUG_EVENT.LOGON_STARTED, "FAILED" );
            }
            return As2805SuncorpProto.logonDone;
        }

        return true;
    }

    @Override
    public void setInternalRejectReason( TransRec trans, RejectReasonType rejectReasonType) {

    }

    @Override
    public void setInternalRejectReason(TransRec trans, RejectReasonType rejectReasonType, String errorText) {

    }

    @Override
    public int getMaxBatchNumber() {
        return 999999;
    }

    @Override
    public TransRec lookupOriginalTransaction(String txnReference) {
        return null;
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
}
