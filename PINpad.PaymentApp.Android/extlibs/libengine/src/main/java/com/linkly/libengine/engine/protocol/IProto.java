package com.linkly.libengine.engine.protocol;

import com.linkly.libengine.dependencies.IDependency;
import com.linkly.libengine.engine.transactions.TransRec;
import com.linkly.libengine.engine.transactions.properties.TProtocol;
import com.linkly.libsecapp.emv.Tag;

import java.util.ArrayList;

public interface IProto {
    boolean init(IDependency dependencies);

    boolean preAuthorize(TransRec trans);

    boolean authorize(TransRec trans);

    boolean postAuthorize(TransRec trans);

    void authorizeOffline(TransRec trans, TProtocol.AuthMethod authMethod);

    ProtoResult batchUpload(boolean silent);

    boolean timeSync(TransRec trans);

    String getEmvProcessingCode(TransRec trans);

    String calculateRRN(TransRec trans);

    boolean discountVoucherRedeem(TransRec trans);

    boolean discountVoucherReverse(TransRec trans);
    
    String encryptCardData(IDependency d, TransRec trans);

    ArrayList<Tag> getEmvTagList();

    String saveEmvTagValuesForDB(IDependency d, TransRec trans);
    String saveCtlsTagValuesForDB(IDependency d, TransRec trans);

    /**
     * Set appropriate response code & text after Signature CVM has been declined
     * */
    void saveSignatureDeclined( TransRec transRec );

    /**
     * Perform protocol specific checks & do any if necessary
     * This can be Bank logon if required, RSA logon
     * @return true if all checks are successful
     * */
    boolean performProtocolChecks();

    /**
     * set response code, receipt and screen text based on reject reason code passed
     * this is how internal response codes are implemented
     *
     * @param rejectReasonType {@link RejectReasonType} object
     * @param trans {@link TransRec} object to be updated
     */
    void setInternalRejectReason( TransRec trans, RejectReasonType rejectReasonType);

    /**
     * override where caller can send specific text for decline
     *
     * @param trans Transaction to set in the internal reject reason
     * @param rejectReasonType Reject reason type to be set
     */
    void setInternalRejectReason( TransRec trans, RejectReasonType rejectReasonType, String errorText );

    byte[] getLastTxMessage();
    byte[] getLastRxMessage();

    boolean requiresDeclinedAdvices();

    int getMaxBatchNumber();

    enum TaskProtocolType {
        AS2805_SUNCORP,
        AS2805_WOOLWORTHS,
        AS2805_EFTEX,
        AS2805_TILL,
        DEMO;
    }
    enum ProtoResult {PROTO_FAIL, PROTO_SUCCESS, PROTO_HALTED, PROTO_DECLINED}

    /**
     * internal error response codes/rejection reasons. Up to protocol layer to
     */
    enum RejectReasonType {
        NOT_SET,
        COMMS_ERROR, // no response from host
        CANCELLED,
        DECLINED,
        DECLINED_BY_CARD_PRE_COMMS,
        DECLINED_BY_CARD_POST_COMMS, // maps to Z4, internal response code for card related decline post comms
        SIGNATURE_REJECTED,
        ABANDONED,
        FRAUD_SUSPECTED,
        USER_TIMEOUT,
        PLB_RESTRICTED_ITEM,
        CARD_REMOVED, // inserted card removed too early
        CHIP_ERROR, // card returned error in Gen AC
        POWER_FAIL,
        PROTOCOL_TASKS_FAILED,
        MAC_FAILED,
        INVALID_AMOUNT,
        REFUND_LIMIT_EXCEEDED, // Amount value from a 24 hour period
        REFUND_LIMIT_COUNT_EXCEEDED, // Count of transactions in 24 hour period
        PASSWORD_CHECK_FAILED,
        TRANS_NOT_ALLOWED,
        RFN_NOT_ENTERED,
        PREAUTH_NOT_FOUND, // original preauth lookup failed for given RFN or card data
        PREAUTH_ALREADY_CANCELLED,
        AMOUNT_EXCEEDS_PREAUTH, // completion amount exceeds net total of preauth amounts
        PREAUTH_NOT_ALLOWED_FOR_CARD, // card type doesn't support/allow preauths
        PREAUTH_TRANS_LIMIT_EXCEEDED, //preauth transactions storedon terminal limit exceeded
        PREAUTH_EXISTS, //preauth transaction with the same card details exists
        CONNECT_FAILED, //Unable to connect to the host
        CARD_NOT_ACCEPTED, // Card not accepted - e.g. track 2 data invalid (too long), or was in blacklisted bin ranges
        CARD_TYPE_NOT_ALLOWED, // Either card scheme is disabled or contactless/emv is not allowed
        DUPLICATE_TXNREF, // TxnRef is unique for all transactions except Reversals
        DUPLICATE_SESSION, // Session id is unique for Local Rest
        TOTAL_OFFLINE_TRANS_AMOUNT_LIMIT_EXCEEDED, //for cumulative amount check
        OFFLINE_TRANS_AMOUNT_LIMIT_EXCEEDED, //for single transaction ceiling limit check
        OFFLINE_TRANS_COUNT_LIMIT_EXCEEDED,
        TRANSACTION_NOT_FOUND,
        VOID_NOT_ALLOWED_FOR_REFUND,
        ALREADY_VOIDED,
        TRANSACTION_ALREADY_SETTLED,
        VOID_NOT_ALLOWED_FOR_ADVICE,
        OFFLINE_VOID_NOT_ALLOWED_FOR_ONLINE_APPROVED_TRANSACTION,
        ISSUER_NOT_AVAILABLE, // Issuer link is down
        BATCH_UPLOAD_FAILED, // Batch upload of SAF transactions failed
        KEY_INJECTION_REQUIRED, // key load is required, e.g. initial keys not loaded, or DUKPT keys have been exhausted
    }

    /**
     * used to look up transaction details based on passed reference, could be from POS, could be manually entered
     * the format of the reference may vary by customer
     *
     * @param txnReference Transaction reference to get the original transaction
     * @return true = transaction/card data found
     */
    TransRec lookupOriginalTransaction(String txnReference);

}
