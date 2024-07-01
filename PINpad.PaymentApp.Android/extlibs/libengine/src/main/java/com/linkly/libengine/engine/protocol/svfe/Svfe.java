package com.linkly.libengine.engine.protocol.svfe;

import static com.linkly.libengine.engine.protocol.IProto.ProtoResult.PROTO_FAIL;
import static com.linkly.libengine.engine.protocol.IProto.ProtoResult.PROTO_SUCCESS;
import static com.linkly.libengine.engine.protocol.svfe.SvfePack.MSGTYPE.AUTH;
import static com.linkly.libengine.engine.protocol.svfe.SvfePack.MSGTYPE.BATCH_UPLOAD;
import static com.linkly.libengine.engine.protocol.svfe.SvfePack.MSGTYPE.NETWORK;
import static com.linkly.libengine.engine.protocol.svfe.SvfePack.MSGTYPE.RECONCILIATION;
import static com.linkly.libengine.engine.protocol.svfe.SvfePack.MSGTYPE.RECONCILIATION_TRAILER;
import static com.linkly.libengine.engine.protocol.svfe.SvfePack.MSGTYPE.REVERSAL;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.AuthMethod.OFFLINE_PRECOMMS_AUTHORISED;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.AuthMethod.ONLINE_AUTHORISED;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.HostResult.AUTHORISED;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.HostResult.CONNECT_FAILED;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.HostResult.NO_RESPONSE;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.MessageStatus;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.MessageStatus.AUTH_SENT;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.MessageStatus.FINALISED;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.MessageStatus.FINALISED_AND_REVERSED;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.MessageStatus.REC_QUEUED;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.MessageStatus.REVERSAL_QUEUED;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.MessageStatus.WAITING_FOR_FINISH;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.ReversalReason.COMMS_FAIL;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.ReversalReason.CUSTOMER_CANCELLATION;
import static com.linkly.libsecapp.IP2PEncrypt.EncryptForStorageType.SHORT_TRACK_FORMAT;
import static com.linkly.libsecapp.emv.Tag.aid;
import static com.linkly.libsecapp.emv.Tag.amt_auth_num;
import static com.linkly.libsecapp.emv.Tag.amt_other_num;
import static com.linkly.libsecapp.emv.Tag.appl_cryptogram;
import static com.linkly.libsecapp.emv.Tag.appl_intchg_profile;
import static com.linkly.libsecapp.emv.Tag.appl_pan_seqnum;
import static com.linkly.libsecapp.emv.Tag.atc;
import static com.linkly.libsecapp.emv.Tag.auth_resp_code;
import static com.linkly.libsecapp.emv.Tag.crypt_info_data;
import static com.linkly.libsecapp.emv.Tag.cvm_results;
import static com.linkly.libsecapp.emv.Tag.df_name;
import static com.linkly.libsecapp.emv.Tag.ifd_ser_num;
import static com.linkly.libsecapp.emv.Tag.iss_auth_data;
import static com.linkly.libsecapp.emv.Tag.issuer_app_data;
import static com.linkly.libsecapp.emv.Tag.issuer_script_cmd;
import static com.linkly.libsecapp.emv.Tag.issuer_script_id;
import static com.linkly.libsecapp.emv.Tag.issuer_script_results2;
import static com.linkly.libsecapp.emv.Tag.term_cap;
import static com.linkly.libsecapp.emv.Tag.term_county_code;
import static com.linkly.libsecapp.emv.Tag.term_type;
import static com.linkly.libsecapp.emv.Tag.term_ver_num;
import static com.linkly.libsecapp.emv.Tag.third_party_data;
import static com.linkly.libsecapp.emv.Tag.tran_date;
import static com.linkly.libsecapp.emv.Tag.tran_type;
import static com.linkly.libsecapp.emv.Tag.trans_category_code;
import static com.linkly.libsecapp.emv.Tag.trans_curcy_code;
import static com.linkly.libsecapp.emv.Tag.trans_seq_counter;
import static com.linkly.libsecapp.emv.Tag.tsi;
import static com.linkly.libsecapp.emv.Tag.tvr;
import static com.linkly.libsecapp.emv.Tag.unpred_num;
import static com.linkly.libui.UIScreenDef.COMMS_FAILURE_RECEIVING_RESPONSE;
import static com.linkly.libui.UIScreenDef.CONNECTING_TO_HOST_1_PLEASE_WAIT;
import static com.linkly.libui.UIScreenDef.CONNECTING_TO_HOST_2_PLEASE_WAIT;
import static com.linkly.libui.UIScreenDef.CONNECTING_TO_HOST_PLEASE_WAIT;
import static com.linkly.libui.UIScreenDef.CONNECTING_TO_HOST_SENDING_MSG;
import static com.linkly.libui.UIScreenDef.CONNECTING_TO_HOST_WAITING_FOR_RESPONSE;
import static com.linkly.libui.UIScreenDef.SELECT_OPTION;

import com.linkly.libengine.action.TransactionDecliner;
import com.linkly.libengine.dependencies.IDependency;
import com.linkly.libengine.engine.EngineManager;
import com.linkly.libengine.engine.comms.IComms;
import com.linkly.libengine.engine.protocol.IProto;
import com.linkly.libengine.engine.protocol.svfe.openisoj.ProcessingCode;
import com.linkly.libengine.engine.reporting.Reconciliation;
import com.linkly.libengine.engine.transactions.TransRec;
import com.linkly.libengine.engine.transactions.TransRecManager;
import com.linkly.libengine.engine.transactions.properties.TAudit;
import com.linkly.libengine.engine.transactions.properties.TProtocol;
import com.linkly.libengine.env.ReceiptNumber;
import com.linkly.libengine.env.Stan;
import com.linkly.libengine.overrides.CoreOverrides;
import com.linkly.libmal.MalFactory;
import com.linkly.libmal.global.util.Util;
import com.linkly.libsecapp.IP2PEncrypt;
import com.linkly.libsecapp.P2PLib;
import com.linkly.libsecapp.emv.Tag;
import com.linkly.libui.IUIDisplay;
import com.linkly.libui.IUIDisplay.String_id;
import com.linkly.libui.UIScreenDef;
import com.linkly.libui.display.DisplayQuestion;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import timber.log.Timber;
/* only class to edit TProtocol.MessageStatus */


public class Svfe implements IProto {

    private static final String TAG = "Svfe";
    private ByteBuffer lastTxMessage;
    private ByteBuffer lastRxMessage;
    private IDependency d = null;
    private IUIDisplay ui = null;

    public boolean init(IDependency dependencies) {
        d = dependencies;
        ui = dependencies.getUI();

        d.getComms().open(d);
        return true;
    }

    private enum REV_ACTIONS {
        REVERSAL_APPROVED,
        REVERSAL_RETRANSMIT,
        REVERSAL_ASK_USER,
    };

    REV_ACTIONS processReversalResponseCodes(TransRec trans) {
        boolean retransmit = false;
        TProtocol protocol = trans.getProtocol();

        spoofAuth(trans);

        String actionCode = protocol.getServerResponseCode();
        if (actionCode == null)
            actionCode = "30"; /* default to retransmit in case of failure */

        int ac = Integer.valueOf(actionCode);


        /* the SVFE retransmit codes are mapped as follows */
        // 904 = 30
        // 905 = 30
        // 907 = 91
        // 909 = 96
        // 910 = 30
        // 912 = 30
        // 923 = 09

        if (ac == 5 || ac == 30 || ac == 91 || ac == 96 || ac == 9 ) {
            return REV_ACTIONS.REVERSAL_RETRANSMIT;
        } else if (ac != 0 && trans.isReversal()) {  /* we only ask user for manual reversals, auto ones are considered done if we get a response */
            return REV_ACTIONS.REVERSAL_ASK_USER;
        }
        return REV_ACTIONS.REVERSAL_APPROVED;
    }

    void processServerResponseCodes(TransRec trans) {
        TProtocol protocol = trans.getProtocol();

        spoofAuth(trans);

        String actionCode = protocol.getServerResponseCode();

        if (actionCode == null || actionCode.isEmpty())
            actionCode = "00";

        int ac = Integer.valueOf(actionCode);

        if (ac == 0 || ac == 8 || ac == 103 || ac == 11) {
            protocol.setHostResult(TProtocol.HostResult.AUTHORISED);
            trans.getProtocol().setAuthMethod(ONLINE_AUTHORISED);
        } else if (ac == 2 || ac == 1) {
            protocol.setHostResult(TProtocol.HostResult.REQUEST_REFERRAL);
        } else if (trans.isReconciliation() && ac == 5) {
            protocol.setHostResult(TProtocol.HostResult.RECONCILED_OUT_OF_BALANCE);
        } else if (trans.isReconciliation() && ac == 0) {
            protocol.setHostResult(TProtocol.HostResult.RECONCILED_IN_BALANCE);
        } else if (ac == 907) { // should be 91 TODO check this don't just change as could cause other issues
            protocol.setHostResult(TProtocol.HostResult.ISSUER_UNAVAILABLE);
        } else {
            protocol.setHostResult(TProtocol.HostResult.DECLINED);
        }
    }

    /* perform any tasks that this protocol must do before doing an auth */
    public boolean preAuthorize(TransRec trans) {
        return true;
    }

    /* perform an authorization for this particular transaction */
    /* if its set to an advice then no need to go online */
    /* once we are about to go online then set flag to say it has been sent */
    /* if we need to sendTCP reversals and make it a new transaction message then we do so */
    public boolean authorize(TransRec trans) {

        boolean result = false;
        TAudit audit = trans.getAudit();
        TProtocol protocol = trans.getProtocol();



        /* if its a transaction to auth offline then very little to do */
        if (trans.getTransType() == EngineManager.TransType.TESTCONNECT) {
            return performTestConnect(trans);

        } else if (trans.isReconciliation()) {

            // smash through all the message states so that we dont need to come back to this transaction
            protocol.setAuthMethod(OFFLINE_PRECOMMS_AUTHORISED);
            Timber.i( "setCanAuthOffline=OFFLINE_PRECOMMS_AUTHORISED");
            Timber.i( "Force a rec to be approved, as settlement already done after each transaction");
            protocol.setHostResult(AUTHORISED);
            protocol.setServerResponseCode("500");
            protocol.setHostResult(TProtocol.HostResult.RECONCILED_IN_BALANCE);
            trans.getAudit().setLastTransmissionDateTime(System.currentTimeMillis());
            trans.updateMessageStatus(MessageStatus.FINALISED);
            trans.save();

            Timber.i( "No REC message to send to Server");

            trans.print(d,true, false, MalFactory.getInstance());

            result = true;
        } else if (trans.isReversal()) {

            TransRec transToReverse = TransRecManager.getInstance().getTransRecDao().getByReceiptNumber(trans.getAudit().getReversalReceiptNumber());
            if (transToReverse != null) {
                if (transToReverse.isReversible()) {

                    /* update the transaction we want to reverse */
                    transToReverse.setToReverse(CUSTOMER_CANCELLATION);

                    if (trans.getAudit().getReceiptNumber() == -1) {
                        trans.getAudit().setReceiptNumber(ReceiptNumber.getNewValue());
                    }
                    trans.getCard().setCardIndex(transToReverse.getCard().getCardIndex());

                    SvfeUtils.updateRetRefNumber(transToReverse);
                    SvfeUtils.updateRetRefNumber(trans);

                    /* update the reversal transaction itself */
                    result = true;
                    transToReverse.save();


                    if (batchUpload(false) == PROTO_SUCCESS ) {
                        Timber.i( "setCanAuthOffline=OFFLINE_PRECOMMS_AUTHORISED");
                        protocol.setAuthMethod(ONLINE_AUTHORISED);
                        trans.updateMessageStatus(FINALISED);
                    } else {
                        protocol.setHostResult(NO_RESPONSE);
                    }
                }
            }
        }
        else {

            trans.save();

            for (int i = 0; i < 2; i++) {
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
                    if (batchUpload(false) != PROTO_SUCCESS) {
                        d.getComms().disconnect(d);
                        continue;
                    }

                    protocol.setHostResult(NO_RESPONSE);
                    trans.updateMessageStatus(AUTH_SENT);
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
    /* for example the final info message on openway */
    public boolean postAuthorize(TransRec trans) {

        /* no more messages to sendTCP */
        if (trans.getProtocol().getMessageStatus() == WAITING_FOR_FINISH) {
            trans.updateMessageStatus(FINALISED);
            trans.save();
        }

        return true;
    }

    @Override
    public void authorizeOffline(TransRec trans, TProtocol.AuthMethod authMethod) {
    }

    /* go through the transactions in the DB and upload as many as possible */
    /* if protocol state is not finalised then sendTCP to protocol to  deal with */
    public ProtoResult batchUpload(boolean silent) {


        boolean spoof = CoreOverrides.get().isSpoofComms();
        boolean success = true;
        int uploadedCount = 0;

        List<TransRec> allTrans = TransRecManager.getInstance().getTransRecDao().findAllNotMessageStatus( FINALISED );

        TransRec currentTrans = d.getCurrentTransaction();

        if (allTrans != null) {

            d.getComms().connect(d, 0);

            for (TransRec trans : allTrans) {

                /* this is to sop background threads uploading the current transaction whilst it is processing */
                /* foreground threads need to do it, so that reversals get sent inline for every auth */
                if (currentTrans != null && !trans.isFinalised() && silent) {
                    Timber.i( "DONT UPLOAD transaction yet as its not finished");
                    continue;
                }


                TProtocol protocol = trans.getProtocol();
                MessageStatus messageStatus = protocol.getMessageStatus();

                if (messageStatus == REC_QUEUED) {
                    if (sendReconciliation(trans, silent) || spoof) {
                        uploadedCount++;
                        trans.updateMessageStatus(FINALISED);
                        trans.save();
                    } else {
                        success = false;
                        break;
                    }
                } else if (messageStatus == REVERSAL_QUEUED || messageStatus == AUTH_SENT) {

                    int count = 0;
                    while (success && messageStatus == REVERSAL_QUEUED || messageStatus == AUTH_SENT) {

                        count++;
                        REV_ACTIONS action = REV_ACTIONS.REVERSAL_RETRANSMIT;

                        //REVERSAL_APPROVED,
                        //REVERSAL_RETRANSMIT,
                        //REVERSAL_ASK_USER,

                        if (doTxRx(trans, REVERSAL, silent) || spoof) {
                            action = processReversalResponseCodes(trans);
                        }

                        if (action == REV_ACTIONS.REVERSAL_APPROVED) {
                            uploadedCount++;
                            trans.updateMessageStatus(FINALISED_AND_REVERSED);
                            trans.save();
                            break;
                        } else if (action == REV_ACTIONS.REVERSAL_RETRANSMIT){
                            Timber.i( "Send it again times:" + count);
                            if (count >= 3) {
                                Timber.i( "Give up resending");
                                success = false;
                                break;
                            }
                        }
                        else if (action == REV_ACTIONS.REVERSAL_ASK_USER) {

                            if (silent) {
                                Timber.i( "Failed to send but no need to ask user in the background, will try later");
                                success = false;
                                break;
                            } else {
                                HashMap<String, Object> map = new HashMap<>();
                                ArrayList<DisplayQuestion> options = new ArrayList<>();
                                options.add(new DisplayQuestion(String_id.STR_RETRY, "OP0", DisplayQuestion.EButtonStyle.BTN_STYLE_DEFAULT));
                                options.add(new DisplayQuestion(String_id.STR_DISMISS, "OP1", DisplayQuestion.EButtonStyle.BTN_STYLE_DEFAULT));
                                map.put(IUIDisplay.uiScreenOptionList, options);

                                ui.showScreen(SELECT_OPTION, map);

                                IUIDisplay.UIResultCode resultCode = ui.getResultCode(IUIDisplay.ACTIVITY_ID.ACT_QUESTION, IUIDisplay.LONG_TIMEOUT);
                                if (resultCode == IUIDisplay.UIResultCode.OK) {
                                    String result = ui.getResultText(IUIDisplay.ACTIVITY_ID.ACT_QUESTION, IUIDisplay.uiResultText1);
                                    if (result.equals("OP0")) {
                                        Timber.i( "Keep retrying with the send");
                                        count = 0;
                                    } else {
                                        Timber.i( "Dismiss this reversal");
                                        uploadedCount++;
                                        trans.updateMessageStatus(FINALISED);
                                        trans.save();
                                        success = false;
                                        break;
                                    }
                                }
                            }
                        }
                    }

                    if (!success)
                        break;
                }
            }
        }

        if (spoof) {
            success = true;
        }

        Timber.i( "BatchUpload returned: " + success + " and sent: " + uploadedCount + " transactions");

        return (success ? PROTO_SUCCESS : PROTO_FAIL);
    }

    @Override
    public boolean timeSync(TransRec trans) {
        return performTimeSync(trans);
    }

    @Override
    public boolean discountVoucherRedeem(TransRec trans) { return false; }

    @Override
    public boolean discountVoucherReverse(TransRec trans) { return false; }

    @Override
    public String encryptCardData(IDependency d, TransRec trans) {

        P2PLib p2pInstance = P2PLib.getInstance();
        IP2PEncrypt p2pEncrypt = p2pInstance.getIP2PEncrypt();

        if (p2pEncrypt == null)
            return null;

        byte[] encResult = p2pEncrypt.encryptForStorage(SHORT_TRACK_FORMAT);

        if (encResult != null) {
            trans.getSecurity().setEncTrack2(Util.byteArrayToHexString(encResult));
        }
        return null;
    }

    private boolean performTimeSync(TransRec trans) {
        return false;
    }

    public boolean performBatchSync(TransRec reconciliationTrans, boolean silent) {

        Reconciliation rec = reconciliationTrans.getReconciliation();
        ArrayList<TransRec> recTransList  = rec.getRecTransList();

        for(TransRec t : recTransList) {
            if (!doTxRx(t, BATCH_UPLOAD, silent)) {
                Timber.i( "Batch upload comms failed Receipt:" + t.getAudit().getReceiptNumber() + " Stan:" + t.getProtocol().getStan());
                return false;
            }
            processServerResponseCodes(t);

            if (t.getProtocol().getServerResponseCode() == null || t.getProtocol().getServerResponseCode().compareToIgnoreCase("00") != 0) {
                Timber.i( "Batch upload failed Receipt:" + t.getAudit().getReceiptNumber() + " Stan:" + t.getProtocol().getStan() + " RC:" + t.getProtocol().getServerResponseCode());
                return false;
            }
        }

        if (recTransList.size() > 0) {
            if (doTxRx(reconciliationTrans, RECONCILIATION_TRAILER, silent)) {
                processServerResponseCodes(reconciliationTrans);
                reconciliationTrans.updateMessageStatus(MessageStatus.FINALISED);
                reconciliationTrans.save();
            }
        }
        return true;
    }

    public boolean sendReconciliation(TransRec reconciliationTrans, boolean silent) {

        boolean success = true;
        boolean mainTransaction = false;
        reconciliationTrans.getProtocol().setHostResult(NO_RESPONSE);

        if (d.getCurrentTransaction() != null) {
            if (d.getCurrentTransaction().getUid() == reconciliationTrans.getUid()) {
                mainTransaction = true;
            }
        }
        try {
            if (doTxRx(reconciliationTrans, RECONCILIATION, silent)) {
                processServerResponseCodes(reconciliationTrans);

                if (reconciliationTrans.getProtocol().getHostResult() == TProtocol.HostResult.RECONCILED_OUT_OF_BALANCE) {
                    if (!performBatchSync(reconciliationTrans, silent)) {
                        success = false;
                    }

                } else {
                    reconciliationTrans.updateMessageStatus(MessageStatus.FINALISED);
                }

            } else {
                success = false;
                reconciliationTrans.getProtocol().setHostResult(CONNECT_FAILED);
            }
            reconciliationTrans.save();

            /* this is done so we print it when this is actually the main transaction being run */
            /* if it is just doing a batch upload it wont print it again and again */
            if (mainTransaction && !silent) {
                reconciliationTrans.print(d,true, false, MalFactory.getInstance());
            }
        } catch ( Exception e ) {
            Timber.w(e);
            success = false;
        }
        Timber.i( "Reconciliation returned: " + success);
        return success;
    }

    /* complicated function used to match up with saveFutureMacs() in SvfePack */
    /* unique stan for every send */
    /* unless we are on a phone then we only use a new stan for the first send of a reversal or a rec*/
    /* recs miss a stan to allow for the scenario where we reverse the last auth */
    public boolean calculateStan(TransRec trans, SvfePack.MSGTYPE msgType) {

        if (msgType == REVERSAL) {
            Timber.i( "SvfePack REV DONT Increment");
        } else {
            trans.getProtocol().setStan(Stan.getNewValue());
        }
        return true;
    }

    /* sendTCP and receive a particular message type */
    public boolean doTxRx(TransRec trans, SvfePack.MSGTYPE msgType, boolean silent) {

        try {
            IComms icomms = d.getComms();

            calculateStan(trans, msgType);

            final byte[] packedBuffer = SvfePack.pack(d, trans, msgType) ;
            if (packedBuffer == null) {
                Timber.i( "Failed to pack");
                return false;
            }

            // allocate buffer to hold 2 length bytes and the payload to send
            byte[] bufferLen = new byte[2 + packedBuffer.length];
            Util.short2ByteArray( (short)packedBuffer.length, bufferLen, 0 );
            System.arraycopy( packedBuffer, 0, bufferLen, 2, packedBuffer.length );

            /* store the packed buffer (to help with debugging) */
            lastTxMessage = ByteBuffer.wrap(packedBuffer);

            if (!silent) {
                ui.showScreen(CONNECTING_TO_HOST_SENDING_MSG);
            }

            Timber.i( "Send Message:" + bufferLen.length);

            if (icomms.send(d, bufferLen) <= 0) {
                Timber.i( "Failed to Send Message");
                return false;
            }

            if (!silent) {
                ui.showScreen(CONNECTING_TO_HOST_WAITING_FOR_RESPONSE);
            }

            byte[] lengthResponse = icomms.recv(d, 2);

            if (lengthResponse != null && lengthResponse.length == 2) {
                int len = lengthResponse[0] & 0xFF;
                len = len << 8;
                len = len | (lengthResponse[1] & 0xFF);

                byte[] response = icomms.recv(d, len);
                if (response != null) {

                    /* store the packed buffer (to help with debugging) */
                    lastRxMessage = ByteBuffer.wrap(response);


                    if (SvfePack.unpack(response, trans)) {
                        return true;
                    } else {
                        if (!silent) {
                            ui.showScreen(COMMS_FAILURE_RECEIVING_RESPONSE);
                            ui.getResultCode(IUIDisplay.ACTIVITY_ID.ACT_INFORMATION, IUIDisplay.SHORT_TIMEOUT);
                        }
                    }
                }
            }
        } catch (Exception e) {
            Timber.i( "doTxRx Exception");
            Timber.w(e);
        }
        return false;
    }


    private boolean performTestConnect(TransRec trans) {

        for (int i = 0; i < 1; i++) {
            /* connect the comms */
            if (d.getComms().connect(d, i)) {


                trans.getProtocol().setHostResult(NO_RESPONSE);
                trans.updateMessageStatus(AUTH_SENT);
                trans.save();

                if (doTxRx(trans, NETWORK, false) || CoreOverrides.get().isSpoofComms()) {
                    trans.getProtocol().setHostResult(AUTHORISED);
                    trans.updateMessageStatus(FINALISED);
                    trans.save();
                    d.getComms().disconnect(d);
                    return true;
                }
            }
        }
        trans.getProtocol().setHostResult(CONNECT_FAILED);
        trans.updateMessageStatus(FINALISED);
        trans.save();
        return false;
    }

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
    public void saveSignatureDeclined( TransRec transRec ) {    }

    @Override
    public boolean performProtocolChecks() {
        return true;
    }

    @Override
    public void setInternalRejectReason( TransRec trans, RejectReasonType rejectReasonType) {

    }

    @Override
    public void setInternalRejectReason(TransRec trans, RejectReasonType rejectReasonType, String errorText) {

    }


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

    public ArrayList<Tag> getEmvTagList() {

        ArrayList<Tag> tags = new ArrayList<Tag>();
        tags.add(trans_curcy_code);  // 5f2A
        tags.add(appl_pan_seqnum); // 0x5f34
        tags.add(appl_intchg_profile);  // 82
        tags.add(df_name); // 84 O
        tags.add(issuer_script_cmd); // 0x86 O
        tags.add(auth_resp_code); // 0x8A O
        tags.add(iss_auth_data); // 0x91 O
        tags.add(tvr); // 0x95 M
        tags.add(tran_date); // 0x9a M
        tags.add(tsi); // 0x9b M
        tags.add(tran_type); // 0x9c M
        tags.add(amt_auth_num); // 9f02 M
        tags.add(amt_other_num); // 9f03 M
        tags.add(term_ver_num); // 0x9f09 O
        tags.add(issuer_app_data); // 0x9f10 M
        tags.add(issuer_script_id); // 0x9f18 O
        tags.add(term_county_code); // 0x9f1a M
        tags.add(ifd_ser_num); // 9F1e O
        tags.add(appl_cryptogram); //0x9f26 M
        tags.add(crypt_info_data); // 0x9f27 M
        tags.add(term_cap); // 0x9f33 O
        tags.add(cvm_results); // 0x9f34 M
        tags.add(term_type); // 0x9f35        M
        tags.add(atc); // 0x9f36 M
        tags.add(unpred_num); // 0x9f37 M
        tags.add(trans_seq_counter); // 0x9f41 O
        tags.add(trans_category_code);//0x9F53), O
        tags.add(issuer_script_results2);//0x9F5B, )
        tags.add(aid); // 4f M
        tags.add(third_party_data); // 0x9F6E O

        return tags;
    }

    private boolean spoofAuth(TransRec trans) {


        if (CoreOverrides.get().isSpoofRecFailure() && trans.isReconciliation()) {
            trans.getProtocol().setServerResponseCode("05");

        } else if (CoreOverrides.get().isSpoofApprovePostComms()) {
            trans.getProtocol().setServerResponseCode("00");
            trans.getProtocol().setAuthCode("123456");

        } else if (CoreOverrides.get().isSpoofReverseResend()) {
            trans.getProtocol().setServerResponseCode("923");

        } else if (CoreOverrides.get().isSpoofReverseFailure()) {
            trans.getProtocol().setServerResponseCode("924");

        } else if (CoreOverrides.get().isSpoofReversePostComms()) {
            trans.getProtocol().setServerResponseCode(null);
        }

        else if (CoreOverrides.get().isSpoofComms() || CoreOverrides.get().isSpoofCommsAuthAll()) {

            if (trans.isReconciliation()) {
                trans.getProtocol().setServerResponseCode("500");
                trans.getProtocol().setHostResult(TProtocol.HostResult.RECONCILED_IN_BALANCE);
                return true;

            } else if ((trans.getAmounts().getTotalAmount() % 100) == 2) {
                trans.getProtocol().setServerResponseCode(String.format("%02d", (trans.getAmounts().getTotalAmount() % 100)));
                trans.getProtocol().setHostResult(TProtocol.HostResult.REQUEST_REFERRAL);

            } else if ((trans.getAmounts().getTotalAmount() % 100) == 65) {
                trans.getProtocol().setServerResponseCode(String.format("%02d", (trans.getAmounts().getTotalAmount() % 100)));
                trans.getProtocol().setHostResult(TProtocol.HostResult.DECLINED);

            } else {
                trans.getProtocol().setServerResponseCode("00");
                trans.getProtocol().setAuthCode("123456");
                trans.getProtocol().setHostResult(AUTHORISED);
                trans.getProtocol().setAuthMethod(ONLINE_AUTHORISED);

            }
            return true;
        }
        return false;
    }


    public String getEmvProcessingCode(TransRec trans) {
        try {
            ProcessingCode pc = SvfeUtils.packProcCode(trans);
            return pc.getTranType();
        } catch (Exception e) {
            Timber.w(e);
        }
        /* default to sale */
        return "00";
    }

    public String calculateRRN(TransRec trans) {
        try {
            return trans.getProtocol().getRRN();
        } catch (Exception e) {
            Timber.w(e);
        }
        /* default to sale */
        return null;
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
    public int getMaxBatchNumber() { return 999999; }

    @Override
    public TransRec lookupOriginalTransaction(String txnReference) {
        return null;
    }
}
