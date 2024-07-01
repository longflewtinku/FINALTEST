package com.linkly.libengine.engine.transactions.properties;

import static com.linkly.libengine.engine.transactions.properties.TProtocol.AuthMethod.EFB_AUTHORISED;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.AuthMethod.LINK_DOWN_EFB_AUTHORISED;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.AuthMethod.OFFLINE_EFB_AUTHORISED;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.AuthMethod.OFFLINE_POSTCOMMS_AUTHORISED;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.AuthMethod.OFFLINE_PRECOMMS_AUTHORISED;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.ReversalState.NOT_REVERSIBLE;
import static com.linkly.libmal.global.util.Util.IntegerValueOf;

import androidx.room.Ignore;

import com.linkly.libengine.engine.Engine;
import com.linkly.libmal.global.util.Util;
import com.linkly.libsecapp.IP2PEncrypt;
import com.linkly.libsecapp.IP2PSec;
import com.linkly.libsecapp.P2PLib;

import java.util.Calendar;
import java.util.Date;

import timber.log.Timber;

public class TProtocol {

    private static final String TAG = "TProtocol";
    private boolean canAuthOffline = false; // true if actions indicate that offline auth should be performed
    private MessageStatus messageStatus = MessageStatus.NOT_SET;
    private ReversalReason reversalReason = ReversalReason.NOT_SET;
    private HostResult hostResult = HostResult.NOT_SET;
    private AuthMethod authMethod = AuthMethod.NOT_SET;
    private Integer stan = 0;
    private Integer originalStan;
    private Integer originalMessageType;
    private long    originalTransmissionDateTime;
    private AuthEntity authEntity = AuthEntity.NOT_SET;
    private Integer avsHouseNumberResult;
    private Integer avsPostcodeResult;
    private Integer cscResult;
    private Integer aavResult;
    private String mastercardAssignedID; /* Elavon */
    private String uniqueTransactionResponseReferenceNumber; /* Elavon */
    private String paymentAccountReference; /* Elavon */
    private String serverTransDateTime; /* Elavon */
    private Integer voidItemNumber; /* Elavon */
    private String eftPaymentId;
    private String acquirerReferenceData;
    private String paymentChannelCounter;
    private String authDateTimeUtc;
    private String cardAcceptorNumber;
    private String acquirerId;
    private String schemeId;
    private String schemeData;
    private String merchantDepartmentId;
    private String errorCode;
    private String userTerminalId;
    private Integer batchNumber = -1;
    private Integer accountType = 0;
    private String authCode;
    private String RRN;
    private ReversalState reversalState = NOT_REVERSIBLE;
    private Integer reversalCount = 0;
    private boolean reversed = false; // set to true when a reversal has been properly uploaded.
    private String serverResponseCode; // actual response code from acquirer/server, not limited in size
    private String posResponseCode; // response code to report to POS (2 char limit)
    private String settlementCode;
    private String adviceResponseCode; // used to hold host response code for both advices (1230 and 1430 responses, AND deferred auths)
    private Integer authCount = 0;
    private Integer adviceAttempts = 0;
    private Integer sessionNumber = -1; //TODO AA-49: If this is mutually exclusive with batchNumber, use that instead?
    private String messageNumber = ""; //TODO AA-49: If this is mutually exclusive with stan, use that instead?
    private String referralNumber;
    private String schemeReferenceData;
    private String merchantTokenId;
    private String cardHash;
    private String cardReference;
    private String diagnosticCode = "";
    private String confirmationCode = "";
    private boolean paxstoreUploaded = false;
    private String bankDate; // YYMMDD from host
    private String bankTime; // hhmmss from host
    private String settlementDate; // YYMMDD

    @Ignore
    private String year; // YYYY format
    @Ignore
    private Integer resetStan;

    private boolean signatureRequired = false;

    /* email details */
    private boolean merchantEmailToUpload = false;
    private boolean customerEmailToUpload = false;
    private String mailCustomerAddress;


    @Ignore
    private boolean fallforwardToContact = false;
    @Ignore
    private boolean onlinePinRequired = false;

    // specific to NMI
    private String operationToken;
    private String operationGUID;

    @Ignore
    private Date serverDateTime;
    @Ignore
    private boolean includeMac = false;
    @Ignore
    private boolean triedPostCommsReferral = false;
    @Ignore
    private static long efbContinueInFallbackTimeStamp = 0; // timestamp of the last transaction if it has received no response from the host or zero otherwise

    private String additionalResponseText = ""; // additional response text will be displayed on approved/declined screen
    private String cardAcceptorPrinterData = ""; // text for the receipt from the host - defined by acquirer/spec
    private String posResponseText = ""; // text to accompany response code for returning to POS. limited to 20 characters

    public static final int ACC_TYPE_DEFAULT        = 0;
    public static final int ACC_TYPE_SAVINGS        = 10;
    public static final int ACC_TYPE_CHEQUE         = 20;
    public static final int ACC_TYPE_CREDIT         = 30;
    public static final int ACC_TYPE_DEFAULT_CONFIG = 40;


    public TProtocol() {
    }

    public boolean isAuthMethodOfflineApproved() {
        return authMethod == OFFLINE_PRECOMMS_AUTHORISED ||
                authMethod == OFFLINE_POSTCOMMS_AUTHORISED ||
                authMethod == OFFLINE_EFB_AUTHORISED ||
                authMethod == EFB_AUTHORISED ||
                authMethod == LINK_DOWN_EFB_AUTHORISED;
    }

    public boolean isIncludeMac() {
        return includeMac && (P2PLib.getInstance().getIP2PSec().getInstalledKeyType() == IP2PSec.InstalledKeyType.DUKPT);
    }

    public ReversalReason getReversalReason() {
        return reversalReason;
    }

    public String getAccountTypeName() {
        switch(accountType) {
            case ACC_TYPE_SAVINGS: return "SAVINGS";
            case ACC_TYPE_CHEQUE:  return "CHEQUE";
            case ACC_TYPE_CREDIT:  return "CREDIT";
			default:               break;
        }
        return "USER";
    }

    public void incReversalCount() {
        reversalCount++;
    }

    public void incAuthCount() {
        authCount++;
    }

    public boolean isCanAuthOffline() {
        return this.canAuthOffline;
    }

    public MessageStatus getMessageStatus() {
        return this.messageStatus;
    }

    public HostResult getHostResult() {
        return this.hostResult;
    }

    public AuthMethod getAuthMethod() {
        return this.authMethod;
    }

    public Integer getStan() {
        return this.stan;
    }

    public Integer getOriginalStan() {
        return this.originalStan;
    }

    public Integer getOriginalMessageType() {
        return this.originalMessageType;
    }

    public long getOriginalTransmissionDateTime() {
        return this.originalTransmissionDateTime;
    }

    public AuthEntity getAuthEntity() {
        return this.authEntity;
    }

    public Integer getAvsHouseNumberResult() {
        return this.avsHouseNumberResult;
    }

    public Integer getAvsPostcodeResult() {
        return this.avsPostcodeResult;
    }

    public Integer getCscResult() {
        return this.cscResult;
    }

    public Integer getAavResult() {
        return this.aavResult;
    }

    public String getMastercardAssignedID() {
        return this.mastercardAssignedID;
    }

    public String getUniqueTransactionResponseReferenceNumber() {
        return this.uniqueTransactionResponseReferenceNumber;
    }

    public String getPaymentAccountReference() {
        return this.paymentAccountReference;
    }

    public String getServerTransDateTime() {
        return this.serverTransDateTime;
    }

    public Integer getVoidItemNumber() {
        return this.voidItemNumber;
    }

    public String getEftPaymentId() {
        return this.eftPaymentId;
    }

    public String getAcquirerReferenceData() {
        return this.acquirerReferenceData;
    }

    public String getPaymentChannelCounter() {
        return this.paymentChannelCounter;
    }

    public String getAuthDateTimeUtc() {
        return this.authDateTimeUtc;
    }

    public String getCardAcceptorNumber() {
        return this.cardAcceptorNumber;
    }

    public String getAcquirerId() {
        return this.acquirerId;
    }

    public String getSchemeId() {
        return this.schemeId;
    }

    public String getSchemeData() {
        return this.schemeData;
    }

    public String getMerchantDepartmentId() {
        return this.merchantDepartmentId;
    }

    public String getErrorCode() {
        return this.errorCode;
    }

    public String getUserTerminalId() {
        return this.userTerminalId;
    }

    public Integer getBatchNumber() {
        return this.batchNumber;
    }

    public Integer getAccountType() {
        return this.accountType;
    }

    public String getAuthCode() {
        return this.authCode;
    }

    public String getRRN() {
        return this.RRN;
    }

    public ReversalState getReversalState() {
        return this.reversalState;
    }

    public Integer getReversalCount() {
        return this.reversalCount;
    }

    public boolean isReversed() {
        return this.reversed;
    }

    public String getServerResponseCode() {
        return this.serverResponseCode;
    }

    public String getPosResponseCode() {
        return this.posResponseCode;
    }

    public String getSettlementCode() {
        return this.settlementCode;
    }

    public String getAdviceResponseCode() {
        return this.adviceResponseCode;
    }

    public Integer getAuthCount() {
        return this.authCount;
    }

    public Integer getAdviceAttempts() {
        return this.adviceAttempts;
    }

    public Integer getSessionNumber() {
        return this.sessionNumber;
    }

    public String getMessageNumber() {
        return this.messageNumber;
    }

    public String getReferralNumber() {
        return this.referralNumber;
    }

    public String getSchemeReferenceData() {
        return this.schemeReferenceData;
    }

    public String getMerchantTokenId() {
        return this.merchantTokenId;
    }

    public String getCardHash() {
        return this.cardHash;
    }

    public String getCardReference() {
        return this.cardReference;
    }

    public String getDiagnosticCode() {
        return this.diagnosticCode;
    }

    public String getConfirmationCode() {
        return this.confirmationCode;
    }

    public boolean isPaxstoreUploaded() {
        return this.paxstoreUploaded;
    }

    public String getBankDate() {
        return this.bankDate;
    }

    public String getBankTime() {
        return this.bankTime;
    }

    public String getSettlementDate() {
        return this.settlementDate;
    }

    public String getYear() {
        return this.year;
    }

    public Integer getResetStan() {
        return this.resetStan;
    }

    public boolean isSignatureRequired() {
        return this.signatureRequired;
    }

    public boolean isMerchantEmailToUpload() {
        return this.merchantEmailToUpload;
    }

    public boolean isCustomerEmailToUpload() {
        return this.customerEmailToUpload;
    }

    public String getMailCustomerAddress() {
        return this.mailCustomerAddress;
    }

    public boolean isFallforwardToContact() {
        return this.fallforwardToContact;
    }

    public boolean isOnlinePinRequired() {
        return this.onlinePinRequired;
    }

    public String getOperationToken() {
        return this.operationToken;
    }

    public String getOperationGUID() {
        return this.operationGUID;
    }

    public Date getServerDateTime() {
        return this.serverDateTime;
    }

    public boolean isTriedPostCommsReferral() {
        return this.triedPostCommsReferral;
    }

    public String getAdditionalResponseText() {
        return this.additionalResponseText;
    }

    public String getCardAcceptorPrinterData() {
        return this.cardAcceptorPrinterData;
    }

    public String getPosResponseText() {
        return this.posResponseText;
    }

    public void setCanAuthOffline(boolean canAuthOffline) {
        this.canAuthOffline = canAuthOffline;
    }

    public void setMessageStatus(MessageStatus messageStatus) {
        this.messageStatus = messageStatus;
    }

    public void setReversalReason(ReversalReason reversalReason) {
        this.reversalReason = reversalReason;
    }

    public void setHostResult(HostResult hostResult) {
        this.hostResult = hostResult;
    }

    public void setAuthMethod(AuthMethod authMethod) {
        this.authMethod = authMethod;
    }

    public void setStan(Integer stan) {
        this.stan = stan;
    }

    public void setOriginalStan(Integer originalStan) {
        this.originalStan = originalStan;
    }

    public void setOriginalMessageType(Integer originalMessageType) {
        this.originalMessageType = originalMessageType;
    }

    public void setOriginalTransmissionDateTime(long originalTransmissionDateTime) {
        this.originalTransmissionDateTime = originalTransmissionDateTime;
    }

    public void setAuthEntity(AuthEntity authEntity) {
        this.authEntity = authEntity;
    }

    public void setAvsHouseNumberResult(Integer avsHouseNumberResult) {
        this.avsHouseNumberResult = avsHouseNumberResult;
    }

    public void setAvsPostcodeResult(Integer avsPostcodeResult) {
        this.avsPostcodeResult = avsPostcodeResult;
    }

    public void setCscResult(Integer cscResult) {
        this.cscResult = cscResult;
    }

    public void setAavResult(Integer aavResult) {
        this.aavResult = aavResult;
    }

    public void setMastercardAssignedID(String mastercardAssignedID) {
        this.mastercardAssignedID = mastercardAssignedID;
    }

    public void setUniqueTransactionResponseReferenceNumber(String uniqueTransactionResponseReferenceNumber) {
        this.uniqueTransactionResponseReferenceNumber = uniqueTransactionResponseReferenceNumber;
    }

    public void setPaymentAccountReference(String paymentAccountReference) {
        this.paymentAccountReference = paymentAccountReference;
    }

    public void setServerTransDateTime(String serverTransDateTime) {
        this.serverTransDateTime = serverTransDateTime;
    }

    public void setVoidItemNumber(Integer voidItemNumber) {
        this.voidItemNumber = voidItemNumber;
    }

    public void setEftPaymentId(String eftPaymentId) {
        this.eftPaymentId = eftPaymentId;
    }

    public void setAcquirerReferenceData(String acquirerReferenceData) {
        this.acquirerReferenceData = acquirerReferenceData;
    }

    public void setPaymentChannelCounter(String paymentChannelCounter) {
        this.paymentChannelCounter = paymentChannelCounter;
    }

    public void setAuthDateTimeUtc(String authDateTimeUtc) {
        this.authDateTimeUtc = authDateTimeUtc;
    }

    public void setCardAcceptorNumber(String cardAcceptorNumber) {
        this.cardAcceptorNumber = cardAcceptorNumber;
    }

    public void setAcquirerId(String acquirerId) {
        this.acquirerId = acquirerId;
    }

    public void setSchemeId(String schemeId) {
        this.schemeId = schemeId;
    }

    public void setSchemeData(String schemeData) {
        this.schemeData = schemeData;
    }

    public void setMerchantDepartmentId(String merchantDepartmentId) {
        this.merchantDepartmentId = merchantDepartmentId;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public void setUserTerminalId(String userTerminalId) {
        this.userTerminalId = userTerminalId;
    }

    public void setBatchNumber(Integer batchNumber) {
        this.batchNumber = batchNumber;
    }

    public void setAccountType(Integer accountType) {
        this.accountType = accountType;
    }

    public void setAuthCode(String authCode) {
        this.authCode = authCode;
    }

    public void setRRN(String RRN) {
        this.RRN = RRN;
    }

    public void setReversalState(ReversalState reversalState) {
        this.reversalState = reversalState;
    }

    public void setReversalCount(Integer reversalCount) {
        this.reversalCount = reversalCount;
    }

    public void setReversed(boolean reversed) {
        this.reversed = reversed;
    }

    public void setServerResponseCode(String serverResponseCode) {
        this.serverResponseCode = serverResponseCode;
    }

    public void setPosResponseCode(String posResponseCode) {
        this.posResponseCode = posResponseCode;
    }

    public void setSettlementCode(String settlementCode) {
        this.settlementCode = settlementCode;
    }

    public void setAdviceResponseCode(String adviceResponseCode) {
        this.adviceResponseCode = adviceResponseCode;
    }

    public void setAuthCount(Integer authCount) {
        this.authCount = authCount;
    }

    public void setAdviceAttempts(Integer adviceAttempts) {
        this.adviceAttempts = adviceAttempts;
    }

    public void setSessionNumber(Integer sessionNumber) {
        this.sessionNumber = sessionNumber;
    }

    public void setMessageNumber(String messageNumber) {
        this.messageNumber = messageNumber;
    }

    public void setReferralNumber(String referralNumber) {
        this.referralNumber = referralNumber;
    }

    public void setSchemeReferenceData(String schemeReferenceData) {
        this.schemeReferenceData = schemeReferenceData;
    }

    public void setMerchantTokenId(String merchantTokenId) {
        this.merchantTokenId = merchantTokenId;
    }

    public void setCardHash(String cardHash) {
        this.cardHash = cardHash;
    }

    public void setCardReference(String cardReference) {
        this.cardReference = cardReference;
    }

    public void setConfirmationCode(String confirmationCode) {
        this.confirmationCode = confirmationCode;
    }

    public void setPaxstoreUploaded(boolean paxstoreUploaded) {
        this.paxstoreUploaded = paxstoreUploaded;
    }

    public void setBankTime(String bankTime) {
        this.bankTime = bankTime;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public void setResetStan(Integer resetStan) {
        this.resetStan = resetStan;
    }

    public void setSignatureRequired(boolean signatureRequired) {
        this.signatureRequired = signatureRequired;
    }

    public void setMerchantEmailToUpload(boolean merchantEmailToUpload) {
        this.merchantEmailToUpload = merchantEmailToUpload;
    }

    public void setCustomerEmailToUpload(boolean customerEmailToUpload) {
        this.customerEmailToUpload = customerEmailToUpload;
    }

    public void setMailCustomerAddress(String mailCustomerAddress) {
        this.mailCustomerAddress = mailCustomerAddress;
    }

    public void setFallforwardToContact(boolean fallforwardToContact) {
        this.fallforwardToContact = fallforwardToContact;
    }

    public void setOnlinePinRequired(boolean onlinePinRequired) {
        this.onlinePinRequired = onlinePinRequired;
    }

    public void setOperationToken(String operationToken) {
        this.operationToken = operationToken;
    }

    public void setOperationGUID(String operationGUID) {
        this.operationGUID = operationGUID;
    }

    public void setServerDateTime(Date serverDateTime) {
        this.serverDateTime = serverDateTime;
    }

    public void setIncludeMac(boolean includeMac) {
        this.includeMac = includeMac;
    }

    public void setTriedPostCommsReferral(boolean triedPostCommsReferral) {
        this.triedPostCommsReferral = triedPostCommsReferral;
    }

    public void setAdditionalResponseText(String additionalResponseText) {
        this.additionalResponseText = additionalResponseText;
    }

    public void setCardAcceptorPrinterData(String cardAcceptorPrinterData) {
        this.cardAcceptorPrinterData = cardAcceptorPrinterData;
    }

    public void setPosResponseText(String posResponseText) {
        this.posResponseText = posResponseText;
    }

    public enum ReversalState {
        NOT_REVERSIBLE, // transactions that don't support reversal
        REVERSIBLE,     // transactions that can be reversed (not already reversed and suitable transaction type for reversal)
    }

    public enum ReversalReason {
        // Need to return the reason why
        NOT_SET("NS"),
        CUSTOMER_CANCELLATION("CC"),
        COMMS_FAIL("CF"),
        TIMEOUT("TO"),
        POWER_FAIL("PF"),
        OPERATOR_REVERSAL("OR"),
        GENACFAIL("GF"),
        SIGFAIL("SF"),
        ACQUIRER_APPROVED("AA"),
        ;
        public String code;
        ReversalReason(String code) {
            this.code = code;
        }
    }

    // when EMV transaction performed (and there is a chance 2nd Gen AC could be performed, i.e. contact EMV trans - send tag update with Email Trigger message
    public enum EmvTagsUpdated {NOT_SET, EMV_TAGS_UPDATED}

    public enum AuthMethod {
        NOT_SET,
        ONLINE_AUTHORISED,
        VOICE_REFERRAL_PED_INITIATED,
        OFFLINE_PRECOMMS_AUTHORISED,  // authorised offline without going online
        OFFLINE_POSTCOMMS_AUTHORISED, // authorised offline after comms failure, e.g. 1st Gen AC returned ARQC, 2nd returned TC after comms failure
        OFFLINE_NO_COMMS_ATTEMPTED,
        VOICE_REFERRAL_HOST_INITIATED,
        OFFLINE_EFB_AUTHORISED,         // Two modes supported: EFB authorised offline by the terminal without attempting go online (while "Continue in EFB timer" doesn't expire)
        EFB_AUTHORISED,                 // "Traditional" EFB - authorised by the terminal after comms error or no response from the host
        LINK_DOWN_EFB_AUTHORISED        // "Traditional" EFB - authorised by the terminal when host reports the response code as Issuer link down("91")
    }

    /* doesnt indicate if the transaction was approved or not, just what the host sent back */
    public enum HostResult {
        NOT_SET("N/A"),
        CONNECT_FAILED("Connect Failed"),
        NO_RESPONSE("No Response"),
        AUTHORISED("Host Authorised"),
        REQUEST_REFERRAL("Request Referral"),
        DECLINED("Declined"),
        RECONCILED_IN_BALANCE("Reconciled in Balance"),
        RECONCILED_OUT_OF_BALANCE("Reconciled out of Balance"),
        RECONCILED_NO_TOTALS("Reconciled No Totals"),
        RECONCILE_FAILED_TERMINAL_ALREADY_SETTLED("Terminal Already Settled"),
        RECONCILE_FAILED_OUTSIDE_WINDOW("Settlement Failed\nOutside Window"),
        RECONCILED_OFFLINE_HOST_CUTOVER("Reconciled Offline by Host"),
        ISSUER_UNAVAILABLE("Issuer Or Switch Inoperative"),
        HOLD_RESPONSE("Hold Response"),
        PLEASE_RETRY("Retry"),
        BATCH_UPLOAD_FAILED("Batch Upload\nFailed");
        public String displayName;

        HostResult(String displayName) {
            this.displayName = displayName;
        }
    }

    public enum MessageStatus {
        NOT_SET("Initial"),
        AUTH_SENT("AuthSent"),                  //Batch
        REVERSAL_QUEUED("RevQueued"),           //Batch
        WAITING_FOR_FINISH("Waiting"),
        ADVICE_QUEUED("AdvQueued"),             //Batch
        REC_QUEUED("RecQueued"),                //Batch
        FINALISED("Finalised"),
        FINALISED_AND_REVERSED("FinalisedAndReversed"),  /* can go back to AuthSent if we are having another go */
        POLL_QUEUED("PollQueued"),
        DEFERRED_AUTH("Deferred Auth");
        public String displayName;

        MessageStatus(String displayName) {
            this.displayName = displayName;
        }

        public boolean isFinalised() {
            return ordinal() == FINALISED.ordinal() || ordinal() == FINALISED_AND_REVERSED.ordinal();
        }
    }

    public String getOrginalTransmissionDateTimeAsString(String format) {
        return TAudit.getDateTimeAsString(format, this.originalTransmissionDateTime, null);
    }


    public enum AuthEntity {
        NOT_SET(0),
        CARD_ACCEPTOR(1),
        ACQUIRER(2),
        CARD_SCHEME(4),
        CARD_ISSUER(8);

        AuthEntity(int value) {
            this.value = value;
        }


        public static AuthEntity newAuthEntity(int paxErrorCode) {
            AuthEntity[] authValues = AuthEntity.values();
            for (AuthEntity authEnt : authValues) {
                if (authEnt.value == paxErrorCode) {
                    return authEnt;
                }
            }
            return null;
        }

        public int value = 0;
    }

    public void setDiagnosticCode( String val ){

        diagnosticCode = val;
    }

    public int getMessageNumberInt() {

        if (Util.isNullOrWhitespace(messageNumber))
            return 0;

        return Integer.parseInt(messageNumber);

    }


    public int getServerResponseCodeAsInt() {

        /* TODO put fallback decision into the protocol layer , as the check of error codes is protocol\acquirer specific*/
        int ac = 5;

        if (!Util.isNullOrEmpty(serverResponseCode)) {
            if (serverResponseCode.contains("Y") || serverResponseCode.contains("Z")) {
                Timber.i( "Cant convert response code:" + serverResponseCode);
            } else if ( serverResponseCode.matches("[0-9]+")) {
                ac = IntegerValueOf(serverResponseCode, ac);
            } else if ( serverResponseCode.matches("[0-9A-F]+")) {
                ac = (int)Long.parseLong(serverResponseCode, 16);
            } else if ( Util.isNumericString(serverResponseCode)) {
                ac = IntegerValueOf(serverResponseCode, ac);
            }
        }

        return ac;
    }

    public void populateProtocolRecord( String responseCode ) {

    }

    /**
     * pre-pend current clock year (excl century) to input string
     * @param mmdd input in month, day format
     * @return yymmdd format
     */
    private String addYear(String mmdd) {
        Calendar cal = Calendar.getInstance();
        int yearFull = cal.get(Calendar.YEAR); // 4 digit year
        return  "" + yearFull%100 + mmdd; // add least significant 2 digits of year e.g. 2022->22
    }

    /**
     * takes input as either MMDD format or YYMMDD and returns YYMMDD format.
     * takes year from clock if MMDD provided
     * @param input date in mmdd or yymmdd
     * @return yymmdd format
     */
    private String setDateAsYYMMDD(String input){
        // if only 4 digits passed, assume MMDD format, pre-pend 2 digit current year (excl century), taken from clock
        if(input != null) {
            if(input.length() == 4 ) {
                return addYear(input);
            } else if(input.length() == 6){
                return input;
            }
        }
        Timber.e( "ERROR, invalid input to setDateAsYYMMD value %s", input==null?"null":input);
        return null;
    }

    public void setSettlementDate(String settlementDate) {
        this.settlementDate = setDateAsYYMMDD(settlementDate);
    }

    public void setBankDate(String bankDate) {
        this.bankDate = setDateAsYYMMDD(bankDate);
    }

    public long getEfbContinueInFallbackTimeStamp() { return efbContinueInFallbackTimeStamp; }
    public static void setEfbContinueInFallbackTimeStamp(long timestamp) { efbContinueInFallbackTimeStamp = timestamp; }

    // If/when there is a problem communicating a Transaction to the Host, our system will attempt to
    //  send a 220 Message.
    // If that happened, then subsequently and when connectivity is reestablished the Batch Upload
    //  set of Transaction is all those with originalMessageType == 220 | 1220.
    // Because originalMessageType is set prior to Batch Upload and after Batch Upload, the value
    //  indicates how the Transaction _should be_ processed by Host -or- _was_ processed by Host.

    public boolean isSubjectToProcessingAsAdvice() {
        return (originalMessageType == 220 || originalMessageType == 1220);
    }
}
