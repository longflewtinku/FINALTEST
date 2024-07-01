package com.linkly.libengine.engine.protocol.iso8583;

import static com.linkly.libengine.engine.protocol.IProto.RejectReasonType.ALREADY_VOIDED;
import static com.linkly.libengine.engine.protocol.IProto.RejectReasonType.AMOUNT_EXCEEDS_PREAUTH;
import static com.linkly.libengine.engine.protocol.IProto.RejectReasonType.BATCH_UPLOAD_FAILED;
import static com.linkly.libengine.engine.protocol.IProto.RejectReasonType.CANCELLED;
import static com.linkly.libengine.engine.protocol.IProto.RejectReasonType.CARD_NOT_ACCEPTED;
import static com.linkly.libengine.engine.protocol.IProto.RejectReasonType.CARD_REMOVED;
import static com.linkly.libengine.engine.protocol.IProto.RejectReasonType.CARD_TYPE_NOT_ALLOWED;
import static com.linkly.libengine.engine.protocol.IProto.RejectReasonType.CHIP_ERROR;
import static com.linkly.libengine.engine.protocol.IProto.RejectReasonType.COMMS_ERROR;
import static com.linkly.libengine.engine.protocol.IProto.RejectReasonType.CONNECT_FAILED;
import static com.linkly.libengine.engine.protocol.IProto.RejectReasonType.DECLINED_BY_CARD_POST_COMMS;
import static com.linkly.libengine.engine.protocol.IProto.RejectReasonType.DECLINED_BY_CARD_PRE_COMMS;
import static com.linkly.libengine.engine.protocol.IProto.RejectReasonType.DUPLICATE_SESSION;
import static com.linkly.libengine.engine.protocol.IProto.RejectReasonType.DUPLICATE_TXNREF;
import static com.linkly.libengine.engine.protocol.IProto.RejectReasonType.INVALID_AMOUNT;
import static com.linkly.libengine.engine.protocol.IProto.RejectReasonType.KEY_INJECTION_REQUIRED;
import static com.linkly.libengine.engine.protocol.IProto.RejectReasonType.MAC_FAILED;
import static com.linkly.libengine.engine.protocol.IProto.RejectReasonType.OFFLINE_TRANS_AMOUNT_LIMIT_EXCEEDED;
import static com.linkly.libengine.engine.protocol.IProto.RejectReasonType.OFFLINE_TRANS_COUNT_LIMIT_EXCEEDED;
import static com.linkly.libengine.engine.protocol.IProto.RejectReasonType.OFFLINE_VOID_NOT_ALLOWED_FOR_ONLINE_APPROVED_TRANSACTION;
import static com.linkly.libengine.engine.protocol.IProto.RejectReasonType.PASSWORD_CHECK_FAILED;
import static com.linkly.libengine.engine.protocol.IProto.RejectReasonType.PLB_RESTRICTED_ITEM;
import static com.linkly.libengine.engine.protocol.IProto.RejectReasonType.POWER_FAIL;
import static com.linkly.libengine.engine.protocol.IProto.RejectReasonType.PREAUTH_ALREADY_CANCELLED;
import static com.linkly.libengine.engine.protocol.IProto.RejectReasonType.PREAUTH_EXISTS;
import static com.linkly.libengine.engine.protocol.IProto.RejectReasonType.PREAUTH_NOT_ALLOWED_FOR_CARD;
import static com.linkly.libengine.engine.protocol.IProto.RejectReasonType.PREAUTH_NOT_FOUND;
import static com.linkly.libengine.engine.protocol.IProto.RejectReasonType.PREAUTH_TRANS_LIMIT_EXCEEDED;
import static com.linkly.libengine.engine.protocol.IProto.RejectReasonType.PROTOCOL_TASKS_FAILED;
import static com.linkly.libengine.engine.protocol.IProto.RejectReasonType.REFUND_LIMIT_COUNT_EXCEEDED;
import static com.linkly.libengine.engine.protocol.IProto.RejectReasonType.REFUND_LIMIT_EXCEEDED;
import static com.linkly.libengine.engine.protocol.IProto.RejectReasonType.SIGNATURE_REJECTED;
import static com.linkly.libengine.engine.protocol.IProto.RejectReasonType.TOTAL_OFFLINE_TRANS_AMOUNT_LIMIT_EXCEEDED;
import static com.linkly.libengine.engine.protocol.IProto.RejectReasonType.TRANSACTION_ALREADY_SETTLED;
import static com.linkly.libengine.engine.protocol.IProto.RejectReasonType.TRANSACTION_NOT_FOUND;
import static com.linkly.libengine.engine.protocol.IProto.RejectReasonType.TRANS_NOT_ALLOWED;
import static com.linkly.libengine.engine.protocol.IProto.RejectReasonType.USER_TIMEOUT;
import static com.linkly.libengine.engine.protocol.IProto.RejectReasonType.VOID_NOT_ALLOWED_FOR_ADVICE;
import static com.linkly.libengine.engine.protocol.IProto.RejectReasonType.VOID_NOT_ALLOWED_FOR_REFUND;

import com.linkly.libengine.engine.EngineManager;
import com.linkly.libengine.engine.protocol.IProto;
import com.linkly.libengine.engine.transactions.properties.TProtocol;
import com.linkly.libmal.global.util.Util;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import timber.log.Timber;

/**
 * Package private class. Maps {@link IProto.RejectReasonType} to Woolworths specific response codes
   as well as contains maps of Woolworths host response codes
 * */
final class As2805WoolworthsRspCodeMap extends As2805RspCodeMap {
    /**
     * Map whose values point to keys of {@link As2805WoolworthsRspCodeMap#rspCodeErrorMap} map
     * */
    private final Map<IProto.RejectReasonType, String> REJECT_REASON_CODE_MAP = new HashMap<IProto.RejectReasonType, String>() {{
        put(COMMS_ERROR, "X0");
        put(CANCELLED, "TM");
        put(DECLINED_BY_CARD_PRE_COMMS, "Z1");
        put(DECLINED_BY_CARD_POST_COMMS, "Z4");
        put(SIGNATURE_REJECTED, "TL");
        put(USER_TIMEOUT, "TO");
        put(PLB_RESTRICTED_ITEM, "PI");
        put(CARD_REMOVED, "RC");
        put(CHIP_ERROR, "TY");
        put(POWER_FAIL, "PF");
        put(PROTOCOL_TASKS_FAILED, "ZZ");
        put(MAC_FAILED, "X7");
        put(INVALID_AMOUNT, "B5");
        put(REFUND_LIMIT_EXCEEDED, "Q3");
        put(REFUND_LIMIT_COUNT_EXCEEDED, "OD");
        put(PASSWORD_CHECK_FAILED, "IP");
        put(TRANS_NOT_ALLOWED, "NA");
        put(PREAUTH_NOT_FOUND, "NF");
        put(PREAUTH_ALREADY_CANCELLED, "NG");
        put(AMOUNT_EXCEEDS_PREAUTH, "CA");
        put(PREAUTH_NOT_ALLOWED_FOR_CARD, "CB");
        put(PREAUTH_TRANS_LIMIT_EXCEEDED, "PT");
        put(PREAUTH_EXISTS, "PE");
        put(CONNECT_FAILED, "CE");
        put(CARD_TYPE_NOT_ALLOWED, "TV");
        put(CARD_NOT_ACCEPTED, "TX");
        put(DUPLICATE_TXNREF, "B9");
        put(DUPLICATE_SESSION, "B8");
        put(TOTAL_OFFLINE_TRANS_AMOUNT_LIMIT_EXCEEDED, "CC");
        put(OFFLINE_TRANS_AMOUNT_LIMIT_EXCEEDED, "CF");
        put(OFFLINE_TRANS_COUNT_LIMIT_EXCEEDED, "CD");
        put(TRANSACTION_NOT_FOUND, "NT");
        put(VOID_NOT_ALLOWED_FOR_REFUND, "VR");
        put(ALREADY_VOIDED, "VA");
        put(VOID_NOT_ALLOWED_FOR_ADVICE, "VB");
        put(TRANSACTION_ALREADY_SETTLED, "VS");
        put(OFFLINE_VOID_NOT_ALLOWED_FOR_ONLINE_APPROVED_TRANSACTION, "VO");
        put(BATCH_UPLOAD_FAILED, "UF");
        put(KEY_INJECTION_REQUIRED, "KE" );
    }};

    private final Hashtable<String, As2805RspCodeMap.MsgDefinition> rspCodeErrorMap = populateRspCodeErrorMap();
    private static final String TEXT_SYSTEM_ERROR_MIXED_CASE = "System Error";
    private static final String TEXT_SYSTEM_ERROR_UPPER_CASE = TEXT_SYSTEM_ERROR_MIXED_CASE.toUpperCase();
    private static final String TEXT_DECLINED_SYSTEM_ERROR_UPPER_CASE = "DECLINED\n" + TEXT_SYSTEM_ERROR_UPPER_CASE;
    private static final String RESPONSE_CODE_UNDEFINED_SYSTEM_ERROR = "XX";
    private static final String TEXT_DECLINED_NEWLINE = "DECLINED\n";

    /**
     * translates input (acquirer) response code to 2 digit response code
     *
     * @param responseCode input acquirer response code, usually 3 digit
     * @return 2 char response code for POS
     */
    private String getPosResponseCode(String responseCode, EngineManager.TransType transType) {
        // if not found/defined, then returns XX undefined system error response code
        return super.getPosResponseCode( responseCode,
                this.rspCodeErrorMap,
                RESPONSE_CODE_UNDEFINED_SYSTEM_ERROR,
                transType );
    }

    private String getResponseCodeErrorDisplay(String responseCode, EngineManager.TransType transType) {
        return super.getResponseCodeErrorDisplay( responseCode,
                this.rspCodeErrorMap,
                TEXT_SYSTEM_ERROR_MIXED_CASE,
                transType );
    }
    private String getResponseCodeErrorReceipt(String responseCode, EngineManager.TransType transType) {
        return super.getResponseCodeErrorReceipt( responseCode, this.rspCodeErrorMap, transType );
    }

    private String getResponseCodeErrorPos(String responseCode, EngineManager.TransType transType) {
        return super.getResponseCodeErrorPos( responseCode, this.rspCodeErrorMap, transType );
    }

    private String getResponseCodeErrorDisplay(String responseCode) {
        return super.getResponseCodeErrorDisplay( responseCode,
                this.rspCodeErrorMap,
                TEXT_SYSTEM_ERROR_MIXED_CASE );
    }


    private String getResponseCodeErrorReceipt(String responseCode) {
        return super.getResponseCodeErrorReceipt( responseCode, this.rspCodeErrorMap );
    }

    private String getResponseCodeErrorPos(String responseCode) {
        return super.getResponseCodeErrorPos( responseCode, this.rspCodeErrorMap );
    }

    /**
     * Populate the transaction fields with the correct values based on the response code
     * @param protocol {@link TProtocol} object
     * @param responseCode to be used
     * @return protocol object with all members if found, else it will return the protocol object as is
     * */
    public TProtocol populateProtocolRecord( TProtocol protocol, String responseCode ){
        if( protocol == null ){
            Timber.e( "TProtocol object passed in is null 1" );
            return null;
        }

        if ( !Util.isNullOrEmpty( responseCode ) ) {
            // response codes are 2 chars for woolies so safe to set this in both pos and server response code fields
            protocol.setPosResponseCode( responseCode );
            protocol.setServerResponseCode( responseCode );
            protocol.setAdditionalResponseText( this.getResponseCodeErrorDisplay( responseCode ) );
            protocol.setCardAcceptorPrinterData( this.getResponseCodeErrorReceipt( responseCode ) );
            protocol.setPosResponseText( this.getResponseCodeErrorPos( responseCode ) );
        } else {
            Timber.e( "Response code is Null or empty for transRec [%s]", protocol.getStan() );
        }
        return protocol;
    }

    /**
     * Populate the transaction fields with the correct values based on the response code
     * @param protocol {@link TProtocol} object
     * @param rejectReasonType {@link IProto.RejectReasonType} be used
     * @return protocol with all values as expected if found. Else will return the protocol layer as is
     * */
    public TProtocol populateProtocolRecord( TProtocol protocol, IProto.RejectReasonType rejectReasonType ){
        if( protocol == null ){
            Timber.e( "TProtocol object passed in is null 2" );
            return null;
        }

        // search table for internal response code
        if( REJECT_REASON_CODE_MAP.containsKey( rejectReasonType ) ){
            return populateProtocolRecord( protocol, REJECT_REASON_CODE_MAP.get( rejectReasonType ) );
        }

        Timber.e( "Internal Response Code not found in lookup table [%s]", rejectReasonType.toString() );
        return protocol;
    }
    /**
     * Populate the transaction fields with the correct values based on the response code
     * @param protocol {@link TProtocol} object
     * @param responseCode to be used
     * @param transType (nullable) transaction type to match on. If null, matches any trans type
     * @return protocol object with all members if found, else it will return the protocol object as is
     * */
    public TProtocol populateProtocolRecord( TProtocol protocol, String responseCode, EngineManager.TransType transType ){
        if( protocol == null ){
            Timber.e( "TProtocol object passed in is null 3" );
            return null;
        }

        if ( !Util.isNullOrEmpty( responseCode ) ) {
            protocol.setPosResponseCode( this.getPosResponseCode(responseCode, transType) ); // use translated field to set 2 digit POS response code
            protocol.setServerResponseCode( responseCode );
            protocol.setAdditionalResponseText( this.getResponseCodeErrorDisplay( responseCode, transType ) );
            protocol.setCardAcceptorPrinterData( this.getResponseCodeErrorReceipt( responseCode, transType ) );
            protocol.setPosResponseText( this.getResponseCodeErrorPos( responseCode, transType ) );
        } else {
            Timber.e( "Response code is Null or empty for transRec [%s]", protocol.getStan() );
        }
        return protocol;
    }

    /**
     * Populate the transaction fields with the correct values based on the response code
     * @param protocol {@link TProtocol} object
     * @param rejectReasonType {@link IProto.RejectReasonType} be used
     * @param errorText override text passed from code with more descriptive decline reason, e.g. card expired
     * @return protocol with all values as expected if found. Else will return the protocol layer as is
     * */
    public TProtocol populateProtocolRecord( TProtocol protocol, IProto.RejectReasonType rejectReasonType, String errorText ){
        protocol = populateProtocolRecord( protocol, rejectReasonType );
        if( errorText != null && protocol != null ) {
            // override text
            protocol.setAdditionalResponseText( errorText );
            protocol.setCardAcceptorPrinterData( (TEXT_DECLINED_NEWLINE + errorText).toUpperCase() );
            protocol.setPosResponseText( errorText.toUpperCase() );
        }

        return protocol;
    }

    private Hashtable<String, MsgDefinition> populateRspCodeErrorMap() {
        Hashtable<String, MsgDefinition> rspCodeErrMap = new Hashtable<>();
        final String INCORRECT_PASSWORD = "Incorrect Password";
        final String NOT_ALLOWED = "Not Allowed";

        // internal errors
        rspCodeErrMap.put( "X0", new MsgDefinition( "Cancelled\nNo Response",       "CANCELLED",                        "NO RESPONSE"           ) );
        rspCodeErrMap.put( "RC", new MsgDefinition( "Cancelled\nCard Removed",      "CANCELLED\nCARD REMOVED",          "CARD REMOVED"          ) );
        rspCodeErrMap.put( "S1", new MsgDefinition( "Sig Not Supported",            "DECLINED\nSIG NOT ALLOWED",        "SIG NOT ALLOWED"       ) );
        rspCodeErrMap.put( "Z4", new MsgDefinition( "Contact Issuer",               "DECLINED\nCONTACT ISSUER",         "CONTACT ISSUER"        ) );
        rspCodeErrMap.put( "X7", new MsgDefinition( "TRAN CANCELLED\nSYSTEM ERROR", "DECLINED CODE X7\nSYSTEM ERROR",   "DECLINED X7"           ) );
        rspCodeErrMap.put( "TY", new MsgDefinition( "Cancelled\nCard Read Failed",  "CANCELLED\nCARD READ FAILED",      "CARD READ FAILED"      ) );
        rspCodeErrMap.put( "TM", new MsgDefinition( "Operator Cancelled",           "OPERATOR CANCELLED",               "OPERATOR CANCELLED"    ) );
        rspCodeErrMap.put( "TO", new MsgDefinition( "Operator Timeout",             "OPERATOR TIMEOUT",                 "OPERATOR TIMEOUT"      ) );
        rspCodeErrMap.put( "PI", new MsgDefinition( "Restricted Item",              "RESTRICTED ITEM",                  "RESTRICTED ITEM"       ) );
        rspCodeErrMap.put( "PF", new MsgDefinition( "Power Fail",                   "POWER FAIL",                       "POWER FAIL"            ) );
        rspCodeErrMap.put( "ZZ", new MsgDefinition( "Logon Failed",                 "LOGON FAILED",                     "LOGON FAILED"          ) );
        // Whitespace is aligned with above lines
        rspCodeErrMap.put( "IP", new MsgDefinition( INCORRECT_PASSWORD,             INCORRECT_PASSWORD.toUpperCase(),   INCORRECT_PASSWORD.toUpperCase() ) );
        rspCodeErrMap.put( "NA", new MsgDefinition( NOT_ALLOWED,                    NOT_ALLOWED.toUpperCase(),          NOT_ALLOWED.toUpperCase() ) );
        rspCodeErrMap.put( "NF", new MsgDefinition( "Original Pre-Auth\nNot Found",  "CANCELLED\nPRE-AUTH NOT FOUND",     "PRE-AUTH NOT FOUND"     ) );
        rspCodeErrMap.put( "NG", new MsgDefinition( "Pre-Auth Already\nCancelled",  "PRE-AUTH\nALREADY CANCELLED",        "ALREADY CANCELLED"     ) );
        rspCodeErrMap.put( "CA", new MsgDefinition( "Amt Exceeds\nPreAuth Amt",     "CANCELLED\nAMT EXCEEDS PREAUTH",   "AMT EXCEEDS PREAUTH"   ) );
        rspCodeErrMap.put( "CB", new MsgDefinition( "Card Does Not\nAllow Preauth", "CANCELLED\nPRE-AUTH NOT ALLOWED",   "PRE-AUTH NOT ALLOWED"   ) );
        rspCodeErrMap.put( "B5", new MsgDefinition( "Invalid Amount",               "CANCELLED\nINVALID AMOUNT",        "INVALID AMOUNT"   ) );
        rspCodeErrMap.put( "PT", new MsgDefinition( "Pre-Auth Exceeds\nTransaction Limit", "DECLINED\nPRE-AUTH NOT ALLOWED",  "PRE-AUTH NOT ALLOWED"   ) );
        rspCodeErrMap.put( "PE", new MsgDefinition( "Pre-Auth Already\nExists",      "DECLINED\nPRE-AUTH EXISTS",         "PRE-AUTH EXISTS"   ) );
        rspCodeErrMap.put( "UF", new MsgDefinition("Batch Upload\nFailed", "DECLINED\nBATCH UPLOAD FAILED", "BATCH UPLOAD FAILED"));
        rspCodeErrMap.put( "KE", new MsgDefinition("Key Load\nRequired",   "DECLINED\nKEY LOAD REQUIRED",   "KEY LOAD REQUIRED"));
        // Void lookup's
        rspCodeErrMap.put( "VR", new MsgDefinition( "Void Not Allowed\nFor Refund",        "CANCELLED\nVOID NOT ALLOWED\nFOR REFUND",     "VOID NOT ALLOWED\nFOR REFUND" , "VR") );
        rspCodeErrMap.put( "VA", new MsgDefinition( "Already Voided",                      "CANCELLED\nALREADY VOIDED",                   "ALREADY VOIDED" , "VA") );
        rspCodeErrMap.put( "VB", new MsgDefinition( "Can't Void\nAlready Complete",        "CANCELLED\nALREADY COMPLETED",                "ALREADY COMPLETED" , "VB") );
        rspCodeErrMap.put( "VS", new MsgDefinition( "Can't Void\nAlready Settled",         "CANCELLED\nALREADY SETTLED",                  "TRANSACTION ALREADY\nSETTLED" , "VS") );
        rspCodeErrMap.put( "VO", new MsgDefinition( "Can't Void\nOnline Approved",         "CANCELLED\nOFFLINE VOID NOT ALLOWED",         "VOID NOT ALLOWED\nONLINE APPROVED" , "VO") );

        // response codes from host
        rspCodeErrMap.put( "00", new MsgDefinition( "Approved",                     "APPROVED",                         "APPROVED"              ) );
        rspCodeErrMap.put( "01", new MsgDefinition( "Contact Issuer",               "DECLINED\nCONTACT ISSUER",         "CONTACT BANK"          ) );
        rspCodeErrMap.put( "04", new MsgDefinition( "Contact Bank",                 "DECLINED\nCONTACT BANK",           "CALL SUPERVISOR"       ) );
        rspCodeErrMap.put( "08", new MsgDefinition( "Approved",                     "APPROVED",                         "APPROVED"              ) );
        rspCodeErrMap.put( "12", new MsgDefinition( "Invalid",                      "DECLINED\nINVALID",                "INVALID TRANSACTION"   ) );
        rspCodeErrMap.put( "13", new MsgDefinition( "Invalid Amount",               "DECLINED\nINVALID AMOUNT",         "INVALID AMOUNT"        ) );
        rspCodeErrMap.put( "14", new MsgDefinition( "Invalid Card No",              "DECLINED\nINVALID CARD NUMBER",    "CARD NUMBER INVALID"   ) );
        rspCodeErrMap.put( "36", new MsgDefinition( "Bank Unavailable",             "DECLINED\nBANK UNAVAILABLE",       "NO BANK DO MANUAL"     ) );
        rspCodeErrMap.put( "42", new MsgDefinition( "No Account",                   "DECLINED\nNO ACCOUNT",             "NO ACCOUNT"            ) );
        rspCodeErrMap.put( "39", new MsgDefinition( "No Credit Account",            "DECLINED\nNO CREDIT ACCOUNT",      "NO CREDIT ACCOUNT"     ) );
        rspCodeErrMap.put( "51", new MsgDefinition( "Contact Bank",                 "DECLINED\nCONTACT BANK",           "CONTACT BANK"          ) );
        rspCodeErrMap.put( "52", new MsgDefinition( "No Cheque Acct",               "DECLINED\nNO CHEQUE ACCOUNT",      "NO CHEQUE ACCOUNT"     ) );
        rspCodeErrMap.put( "53", new MsgDefinition( "No Savings Acct",              "DECLINED\nNO SAVINGS ACCOUNT",     "NO SAVINGS ACCOUNT"    ) );
        rspCodeErrMap.put( "54", new MsgDefinition( "Expired Card",                 "DECLINED\nEXPIRED CARD",           "EXPIRED CARD"          ) );
        rspCodeErrMap.put( "55", new MsgDefinition( "Incorrect PIN",                "DECLINED\nINCORRECT PIN",          "PIN ERROR"             ) );
        rspCodeErrMap.put( "61", new MsgDefinition( "Over Card Limit",              "DECLINED\nOVER CARD LIMIT",        "OVER CARD LIMIT"       ) );
        rspCodeErrMap.put( "65", new MsgDefinition( "Over Card Limit",              "DECLINED\nOVER CARD LIMIT",        "OVER CARD LIMIT"       ) );
        rspCodeErrMap.put( "75", new MsgDefinition( "Exceed PIN Tries",             "DECLINED\nEXCEED PIN TRIES",       "EXCEED PIN TRIES"      ) );
        rspCodeErrMap.put( "76", new MsgDefinition( "Approved",                     "APPROVED",                         "APPROVED"              ) );
        rspCodeErrMap.put( "91", new MsgDefinition( "Bank Unavailable",             "DECLINED\nBANK UNAVAILABLE",       "NO BANK DO MANUAL"     ) );
        rspCodeErrMap.put( "96", new MsgDefinition( "Retry",                        "DECLINED\nRETRY",                  "SYSTEM MALFUNCT'N"     ) );
        rspCodeErrMap.put( "97", new MsgDefinition( "Settlement\nTotals Reset",     "SETTLEMENT\nTOTALS RESET",         "TOTALS RESET"          ) );
        rspCodeErrMap.put( "OD", new MsgDefinition( "Refund Limit",                 "DECLINED\nREFUND LIMIT",           "OVER REFUND LIMIT"     ) );
        rspCodeErrMap.put( "Q3", new MsgDefinition( "Refund Limit",                 "DECLINED\nREFUND LIMIT",           "OVER REFUND LIMIT"     ) );
        rspCodeErrMap.put( "Q5", new MsgDefinition( "Cancelled\nPower Fail",        "CANCELLED\nPOWER FAIL",            "POWER FAIL"            ) );
        rspCodeErrMap.put( "Q6", new MsgDefinition( "Cancelled\nNo Manual Card",    "CANCELLED\nNO MANUAL CARD",        "NO MANUAL CARD"        ) );
        rspCodeErrMap.put( "RI", new MsgDefinition( "Cancelled\nItems Not Allowed", "CANCELLED\nITEMS NOT ALLOWED",     "ITEMS NOT ALLOWED"     ) );
        rspCodeErrMap.put( "N0", new MsgDefinition( "CPAT Seq Error",               "DECLINED\nCPAT SEQUENCE ERROR",    "CPAT SEQUENCE ERROR"   ) );
        rspCodeErrMap.put( "N1", new MsgDefinition( "",                             "",                                 "LOGON SUCCESSFUL"      ) );
        rspCodeErrMap.put( "N2", new MsgDefinition( "Pinpad ID Error",              "DECLINED\nPINPAD ID ERROR",        "PINPAD ID ERROR"       ) );
        rspCodeErrMap.put( "CU", new MsgDefinition( "Invalid Currency",             "DECLINED\nINVALID CURRENCY",       "INVALID CURRENCY"      ) );
        rspCodeErrMap.put( "N3", new MsgDefinition( "Approved",                     "APPROVED",                         "APPROVED"              ) );
        rspCodeErrMap.put( "Y1", new MsgDefinition( "Approved",                     "APPROVED",                         "APPROVED") );
        rspCodeErrMap.put( "Y3", new MsgDefinition( "Approved",                     "APPROVED",                         "APPROVED") );
        rspCodeErrMap.put( "Z1", new MsgDefinition( "Contact Issuer",               "DECLINED\nCONTACT ISSUER",         "CONTACT ISSUER"        ) );
        rspCodeErrMap.put( "Z3", new MsgDefinition( "Contact Issuer",               "DECLINED\nCONTACT ISSUER",         "CONTACT ISSUER"        ) );
        rspCodeErrMap.put( "TL", new MsgDefinition( "Signature Error",              "DECLINED\nSIGNATURE ERROR",        "SIGNATURE ERROR"       ) );

        rspCodeErrMap.put( "02", new MsgDefinition( TEXT_SYSTEM_ERROR_MIXED_CASE, TEXT_DECLINED_SYSTEM_ERROR_UPPER_CASE, TEXT_SYSTEM_ERROR_UPPER_CASE ) );
        rspCodeErrMap.put( "03", new MsgDefinition( TEXT_SYSTEM_ERROR_MIXED_CASE, TEXT_DECLINED_SYSTEM_ERROR_UPPER_CASE, TEXT_SYSTEM_ERROR_UPPER_CASE ) );
        rspCodeErrMap.put( "05", new MsgDefinition( TEXT_SYSTEM_ERROR_MIXED_CASE, TEXT_DECLINED_SYSTEM_ERROR_UPPER_CASE, TEXT_SYSTEM_ERROR_UPPER_CASE ) );
        rspCodeErrMap.put( "06", new MsgDefinition( TEXT_SYSTEM_ERROR_MIXED_CASE, TEXT_DECLINED_SYSTEM_ERROR_UPPER_CASE, TEXT_SYSTEM_ERROR_UPPER_CASE ) );
        rspCodeErrMap.put( "07", new MsgDefinition( TEXT_SYSTEM_ERROR_MIXED_CASE, TEXT_DECLINED_SYSTEM_ERROR_UPPER_CASE, TEXT_SYSTEM_ERROR_UPPER_CASE ) );
        rspCodeErrMap.put( "09", new MsgDefinition( TEXT_SYSTEM_ERROR_MIXED_CASE, TEXT_DECLINED_SYSTEM_ERROR_UPPER_CASE, TEXT_SYSTEM_ERROR_UPPER_CASE ) );
        rspCodeErrMap.put( "10", new MsgDefinition( TEXT_SYSTEM_ERROR_MIXED_CASE, TEXT_DECLINED_SYSTEM_ERROR_UPPER_CASE, TEXT_SYSTEM_ERROR_UPPER_CASE ) );
        rspCodeErrMap.put( "15", new MsgDefinition( TEXT_SYSTEM_ERROR_MIXED_CASE, TEXT_DECLINED_SYSTEM_ERROR_UPPER_CASE, TEXT_SYSTEM_ERROR_UPPER_CASE ) );
        rspCodeErrMap.put( "16", new MsgDefinition( TEXT_SYSTEM_ERROR_MIXED_CASE, TEXT_DECLINED_SYSTEM_ERROR_UPPER_CASE, TEXT_SYSTEM_ERROR_UPPER_CASE ) );
        rspCodeErrMap.put( "17", new MsgDefinition( TEXT_SYSTEM_ERROR_MIXED_CASE, TEXT_DECLINED_SYSTEM_ERROR_UPPER_CASE, TEXT_SYSTEM_ERROR_UPPER_CASE ) );
        rspCodeErrMap.put( "18", new MsgDefinition( TEXT_SYSTEM_ERROR_MIXED_CASE, TEXT_DECLINED_SYSTEM_ERROR_UPPER_CASE, TEXT_SYSTEM_ERROR_UPPER_CASE ) );
        rspCodeErrMap.put( "19", new MsgDefinition( TEXT_SYSTEM_ERROR_MIXED_CASE, TEXT_DECLINED_SYSTEM_ERROR_UPPER_CASE, TEXT_SYSTEM_ERROR_UPPER_CASE ) );
        rspCodeErrMap.put( "20", new MsgDefinition( TEXT_SYSTEM_ERROR_MIXED_CASE, TEXT_DECLINED_SYSTEM_ERROR_UPPER_CASE, TEXT_SYSTEM_ERROR_UPPER_CASE ) );
        rspCodeErrMap.put( "21", new MsgDefinition( TEXT_SYSTEM_ERROR_MIXED_CASE, TEXT_DECLINED_SYSTEM_ERROR_UPPER_CASE, TEXT_SYSTEM_ERROR_UPPER_CASE ) );
        rspCodeErrMap.put( "22", new MsgDefinition( TEXT_SYSTEM_ERROR_MIXED_CASE, TEXT_DECLINED_SYSTEM_ERROR_UPPER_CASE, TEXT_SYSTEM_ERROR_UPPER_CASE ) );
        rspCodeErrMap.put( "23", new MsgDefinition( TEXT_SYSTEM_ERROR_MIXED_CASE, TEXT_DECLINED_SYSTEM_ERROR_UPPER_CASE, TEXT_SYSTEM_ERROR_UPPER_CASE ) );
        rspCodeErrMap.put( "24", new MsgDefinition( TEXT_SYSTEM_ERROR_MIXED_CASE, TEXT_DECLINED_SYSTEM_ERROR_UPPER_CASE, TEXT_SYSTEM_ERROR_UPPER_CASE ) );
        rspCodeErrMap.put( "25", new MsgDefinition( TEXT_SYSTEM_ERROR_MIXED_CASE, TEXT_DECLINED_SYSTEM_ERROR_UPPER_CASE, TEXT_SYSTEM_ERROR_UPPER_CASE ) );
        rspCodeErrMap.put( "26", new MsgDefinition( TEXT_SYSTEM_ERROR_MIXED_CASE, TEXT_DECLINED_SYSTEM_ERROR_UPPER_CASE, TEXT_SYSTEM_ERROR_UPPER_CASE ) );
        rspCodeErrMap.put( "27", new MsgDefinition( TEXT_SYSTEM_ERROR_MIXED_CASE, TEXT_DECLINED_SYSTEM_ERROR_UPPER_CASE, TEXT_SYSTEM_ERROR_UPPER_CASE ) );
        rspCodeErrMap.put( "28", new MsgDefinition( TEXT_SYSTEM_ERROR_MIXED_CASE, TEXT_DECLINED_SYSTEM_ERROR_UPPER_CASE, TEXT_SYSTEM_ERROR_UPPER_CASE ) );
        rspCodeErrMap.put( "29", new MsgDefinition( TEXT_SYSTEM_ERROR_MIXED_CASE, TEXT_DECLINED_SYSTEM_ERROR_UPPER_CASE, TEXT_SYSTEM_ERROR_UPPER_CASE ) );
        rspCodeErrMap.put( "30", new MsgDefinition( TEXT_SYSTEM_ERROR_MIXED_CASE, TEXT_DECLINED_SYSTEM_ERROR_UPPER_CASE, TEXT_SYSTEM_ERROR_UPPER_CASE ) );
        rspCodeErrMap.put( "31", new MsgDefinition( TEXT_SYSTEM_ERROR_MIXED_CASE, TEXT_DECLINED_SYSTEM_ERROR_UPPER_CASE, TEXT_SYSTEM_ERROR_UPPER_CASE ) );
        rspCodeErrMap.put( "32", new MsgDefinition( TEXT_SYSTEM_ERROR_MIXED_CASE, TEXT_DECLINED_SYSTEM_ERROR_UPPER_CASE, TEXT_SYSTEM_ERROR_UPPER_CASE ) );
        rspCodeErrMap.put( "33", new MsgDefinition( TEXT_SYSTEM_ERROR_MIXED_CASE, TEXT_DECLINED_SYSTEM_ERROR_UPPER_CASE, TEXT_SYSTEM_ERROR_UPPER_CASE ) );
        rspCodeErrMap.put( "34", new MsgDefinition( TEXT_SYSTEM_ERROR_MIXED_CASE, TEXT_DECLINED_SYSTEM_ERROR_UPPER_CASE, TEXT_SYSTEM_ERROR_UPPER_CASE ) );
        rspCodeErrMap.put( "35", new MsgDefinition( TEXT_SYSTEM_ERROR_MIXED_CASE, TEXT_DECLINED_SYSTEM_ERROR_UPPER_CASE, TEXT_SYSTEM_ERROR_UPPER_CASE ) );
        rspCodeErrMap.put( "37", new MsgDefinition( TEXT_SYSTEM_ERROR_MIXED_CASE, TEXT_DECLINED_SYSTEM_ERROR_UPPER_CASE, TEXT_SYSTEM_ERROR_UPPER_CASE ) );
        rspCodeErrMap.put( "38", new MsgDefinition( TEXT_SYSTEM_ERROR_MIXED_CASE, TEXT_DECLINED_SYSTEM_ERROR_UPPER_CASE, TEXT_SYSTEM_ERROR_UPPER_CASE ) );
        rspCodeErrMap.put( "40", new MsgDefinition( TEXT_SYSTEM_ERROR_MIXED_CASE, TEXT_DECLINED_SYSTEM_ERROR_UPPER_CASE, TEXT_SYSTEM_ERROR_UPPER_CASE ) );
        rspCodeErrMap.put( "41", new MsgDefinition( TEXT_SYSTEM_ERROR_MIXED_CASE, TEXT_DECLINED_SYSTEM_ERROR_UPPER_CASE, TEXT_SYSTEM_ERROR_UPPER_CASE ) );
        rspCodeErrMap.put( "43", new MsgDefinition( TEXT_SYSTEM_ERROR_MIXED_CASE, TEXT_DECLINED_SYSTEM_ERROR_UPPER_CASE, TEXT_SYSTEM_ERROR_UPPER_CASE ) );
        rspCodeErrMap.put( "44", new MsgDefinition( TEXT_SYSTEM_ERROR_MIXED_CASE, TEXT_DECLINED_SYSTEM_ERROR_UPPER_CASE, TEXT_SYSTEM_ERROR_UPPER_CASE ) );
        rspCodeErrMap.put( "45", new MsgDefinition( TEXT_SYSTEM_ERROR_MIXED_CASE, TEXT_DECLINED_SYSTEM_ERROR_UPPER_CASE, TEXT_SYSTEM_ERROR_UPPER_CASE ) );
        rspCodeErrMap.put( "46", new MsgDefinition( TEXT_SYSTEM_ERROR_MIXED_CASE, TEXT_DECLINED_SYSTEM_ERROR_UPPER_CASE, TEXT_SYSTEM_ERROR_UPPER_CASE ) );
        rspCodeErrMap.put( "47", new MsgDefinition( TEXT_SYSTEM_ERROR_MIXED_CASE, TEXT_DECLINED_SYSTEM_ERROR_UPPER_CASE, TEXT_SYSTEM_ERROR_UPPER_CASE ) );
        rspCodeErrMap.put( "48", new MsgDefinition( TEXT_SYSTEM_ERROR_MIXED_CASE, TEXT_DECLINED_SYSTEM_ERROR_UPPER_CASE, TEXT_SYSTEM_ERROR_UPPER_CASE ) );
        rspCodeErrMap.put( "49", new MsgDefinition( TEXT_SYSTEM_ERROR_MIXED_CASE, TEXT_DECLINED_SYSTEM_ERROR_UPPER_CASE, TEXT_SYSTEM_ERROR_UPPER_CASE ) );
        rspCodeErrMap.put( "50", new MsgDefinition( TEXT_SYSTEM_ERROR_MIXED_CASE, TEXT_DECLINED_SYSTEM_ERROR_UPPER_CASE, TEXT_SYSTEM_ERROR_UPPER_CASE ) );
        rspCodeErrMap.put( "56", new MsgDefinition( TEXT_SYSTEM_ERROR_MIXED_CASE, TEXT_DECLINED_SYSTEM_ERROR_UPPER_CASE, TEXT_SYSTEM_ERROR_UPPER_CASE ) );
        rspCodeErrMap.put( "57", new MsgDefinition( TEXT_SYSTEM_ERROR_MIXED_CASE, TEXT_DECLINED_SYSTEM_ERROR_UPPER_CASE, TEXT_SYSTEM_ERROR_UPPER_CASE ) );
        rspCodeErrMap.put( "58", new MsgDefinition( TEXT_SYSTEM_ERROR_MIXED_CASE, TEXT_DECLINED_SYSTEM_ERROR_UPPER_CASE, TEXT_SYSTEM_ERROR_UPPER_CASE ) );
        rspCodeErrMap.put( "59", new MsgDefinition( TEXT_SYSTEM_ERROR_MIXED_CASE, TEXT_DECLINED_SYSTEM_ERROR_UPPER_CASE, TEXT_SYSTEM_ERROR_UPPER_CASE ) );
        rspCodeErrMap.put( "60", new MsgDefinition( TEXT_SYSTEM_ERROR_MIXED_CASE, TEXT_DECLINED_SYSTEM_ERROR_UPPER_CASE, TEXT_SYSTEM_ERROR_UPPER_CASE ) );
        rspCodeErrMap.put( "62", new MsgDefinition( TEXT_SYSTEM_ERROR_MIXED_CASE, TEXT_DECLINED_SYSTEM_ERROR_UPPER_CASE, TEXT_SYSTEM_ERROR_UPPER_CASE ) );
        rspCodeErrMap.put( "63", new MsgDefinition( TEXT_SYSTEM_ERROR_MIXED_CASE, TEXT_DECLINED_SYSTEM_ERROR_UPPER_CASE, TEXT_SYSTEM_ERROR_UPPER_CASE ) );
        rspCodeErrMap.put( "64", new MsgDefinition( TEXT_SYSTEM_ERROR_MIXED_CASE, TEXT_DECLINED_SYSTEM_ERROR_UPPER_CASE, TEXT_SYSTEM_ERROR_UPPER_CASE ) );
        rspCodeErrMap.put( "66", new MsgDefinition( TEXT_SYSTEM_ERROR_MIXED_CASE, TEXT_DECLINED_SYSTEM_ERROR_UPPER_CASE, TEXT_SYSTEM_ERROR_UPPER_CASE ) );
        rspCodeErrMap.put( "67", new MsgDefinition( TEXT_SYSTEM_ERROR_MIXED_CASE, TEXT_DECLINED_SYSTEM_ERROR_UPPER_CASE, TEXT_SYSTEM_ERROR_UPPER_CASE ) );
        rspCodeErrMap.put( "68", new MsgDefinition( TEXT_SYSTEM_ERROR_MIXED_CASE, TEXT_DECLINED_SYSTEM_ERROR_UPPER_CASE, TEXT_SYSTEM_ERROR_UPPER_CASE ) );
        rspCodeErrMap.put( "69", new MsgDefinition( TEXT_SYSTEM_ERROR_MIXED_CASE, TEXT_DECLINED_SYSTEM_ERROR_UPPER_CASE, TEXT_SYSTEM_ERROR_UPPER_CASE ) );
        rspCodeErrMap.put( "70", new MsgDefinition( TEXT_SYSTEM_ERROR_MIXED_CASE, TEXT_DECLINED_SYSTEM_ERROR_UPPER_CASE, TEXT_SYSTEM_ERROR_UPPER_CASE ) );
        rspCodeErrMap.put( "71", new MsgDefinition( TEXT_SYSTEM_ERROR_MIXED_CASE, TEXT_DECLINED_SYSTEM_ERROR_UPPER_CASE, TEXT_SYSTEM_ERROR_UPPER_CASE ) );
        rspCodeErrMap.put( "72", new MsgDefinition( TEXT_SYSTEM_ERROR_MIXED_CASE, TEXT_DECLINED_SYSTEM_ERROR_UPPER_CASE, TEXT_SYSTEM_ERROR_UPPER_CASE ) );
        rspCodeErrMap.put( "73", new MsgDefinition( TEXT_SYSTEM_ERROR_MIXED_CASE, TEXT_DECLINED_SYSTEM_ERROR_UPPER_CASE, TEXT_SYSTEM_ERROR_UPPER_CASE ) );
        rspCodeErrMap.put( "74", new MsgDefinition( TEXT_SYSTEM_ERROR_MIXED_CASE, TEXT_DECLINED_SYSTEM_ERROR_UPPER_CASE, TEXT_SYSTEM_ERROR_UPPER_CASE ) );
        rspCodeErrMap.put( "77", new MsgDefinition( TEXT_SYSTEM_ERROR_MIXED_CASE, TEXT_DECLINED_SYSTEM_ERROR_UPPER_CASE, TEXT_SYSTEM_ERROR_UPPER_CASE ) );
        rspCodeErrMap.put( "78", new MsgDefinition( TEXT_SYSTEM_ERROR_MIXED_CASE, TEXT_DECLINED_SYSTEM_ERROR_UPPER_CASE, TEXT_SYSTEM_ERROR_UPPER_CASE ) );
        rspCodeErrMap.put( "79", new MsgDefinition( TEXT_SYSTEM_ERROR_MIXED_CASE, TEXT_DECLINED_SYSTEM_ERROR_UPPER_CASE, TEXT_SYSTEM_ERROR_UPPER_CASE ) );
        rspCodeErrMap.put( "80", new MsgDefinition( TEXT_SYSTEM_ERROR_MIXED_CASE, TEXT_DECLINED_SYSTEM_ERROR_UPPER_CASE, TEXT_SYSTEM_ERROR_UPPER_CASE ) );
        rspCodeErrMap.put( "81", new MsgDefinition( TEXT_SYSTEM_ERROR_MIXED_CASE, TEXT_DECLINED_SYSTEM_ERROR_UPPER_CASE, TEXT_SYSTEM_ERROR_UPPER_CASE ) );
        rspCodeErrMap.put( "82", new MsgDefinition( TEXT_SYSTEM_ERROR_MIXED_CASE, TEXT_DECLINED_SYSTEM_ERROR_UPPER_CASE, TEXT_SYSTEM_ERROR_UPPER_CASE ) );
        rspCodeErrMap.put( "83", new MsgDefinition( TEXT_SYSTEM_ERROR_MIXED_CASE, TEXT_DECLINED_SYSTEM_ERROR_UPPER_CASE, TEXT_SYSTEM_ERROR_UPPER_CASE ) );
        rspCodeErrMap.put( "84", new MsgDefinition( TEXT_SYSTEM_ERROR_MIXED_CASE, TEXT_DECLINED_SYSTEM_ERROR_UPPER_CASE, TEXT_SYSTEM_ERROR_UPPER_CASE ) );
        rspCodeErrMap.put( "85", new MsgDefinition( TEXT_SYSTEM_ERROR_MIXED_CASE, TEXT_DECLINED_SYSTEM_ERROR_UPPER_CASE, TEXT_SYSTEM_ERROR_UPPER_CASE ) );
        rspCodeErrMap.put( "86", new MsgDefinition( TEXT_SYSTEM_ERROR_MIXED_CASE, TEXT_DECLINED_SYSTEM_ERROR_UPPER_CASE, TEXT_SYSTEM_ERROR_UPPER_CASE ) );
        rspCodeErrMap.put( "87", new MsgDefinition( TEXT_SYSTEM_ERROR_MIXED_CASE, TEXT_DECLINED_SYSTEM_ERROR_UPPER_CASE, TEXT_SYSTEM_ERROR_UPPER_CASE ) );
        rspCodeErrMap.put( "88", new MsgDefinition( TEXT_SYSTEM_ERROR_MIXED_CASE, TEXT_DECLINED_SYSTEM_ERROR_UPPER_CASE, TEXT_SYSTEM_ERROR_UPPER_CASE ) );
        rspCodeErrMap.put( "89", new MsgDefinition( TEXT_SYSTEM_ERROR_MIXED_CASE, TEXT_DECLINED_SYSTEM_ERROR_UPPER_CASE, TEXT_SYSTEM_ERROR_UPPER_CASE ) );
        rspCodeErrMap.put( "90", new MsgDefinition( TEXT_SYSTEM_ERROR_MIXED_CASE, TEXT_DECLINED_SYSTEM_ERROR_UPPER_CASE, TEXT_SYSTEM_ERROR_UPPER_CASE ) );
        rspCodeErrMap.put( "92", new MsgDefinition( TEXT_SYSTEM_ERROR_MIXED_CASE, TEXT_DECLINED_SYSTEM_ERROR_UPPER_CASE, TEXT_SYSTEM_ERROR_UPPER_CASE ) );
        rspCodeErrMap.put( "94", new MsgDefinition( TEXT_SYSTEM_ERROR_MIXED_CASE, TEXT_DECLINED_SYSTEM_ERROR_UPPER_CASE, TEXT_SYSTEM_ERROR_UPPER_CASE ) );
        rspCodeErrMap.put( "95", new MsgDefinition( TEXT_SYSTEM_ERROR_MIXED_CASE, TEXT_DECLINED_SYSTEM_ERROR_UPPER_CASE, TEXT_SYSTEM_ERROR_UPPER_CASE ) );
        rspCodeErrMap.put( "98", new MsgDefinition( TEXT_SYSTEM_ERROR_MIXED_CASE, TEXT_DECLINED_SYSTEM_ERROR_UPPER_CASE, TEXT_SYSTEM_ERROR_UPPER_CASE ) );
        rspCodeErrMap.put( "99", new MsgDefinition( TEXT_SYSTEM_ERROR_MIXED_CASE, TEXT_DECLINED_SYSTEM_ERROR_UPPER_CASE, TEXT_SYSTEM_ERROR_UPPER_CASE ) );
        rspCodeErrMap.put( "XX", new MsgDefinition( TEXT_SYSTEM_ERROR_MIXED_CASE, TEXT_DECLINED_SYSTEM_ERROR_UPPER_CASE, TEXT_SYSTEM_ERROR_UPPER_CASE ) );

        return rspCodeErrMap;
    }

}
