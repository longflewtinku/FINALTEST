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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import timber.log.Timber;

/**
 * Package private class. Maps {@link IProto.RejectReasonType} to Eftex specific response codes
   as well as contains maps of Eftex host response codes
 * */
final class As2805EftexRspCodeMap extends As2805RspCodeMap {
    /**
     * Map whose values point to keys of {@link As2805EftexRspCodeMap#rspCodeErrorMap} map
     *
     // IMPORTANT: when defining new response codes, add them to this spreadsheet and publish for support purposes <a href="https://pceftpos.sharepoint.com/:x:/s/IAAS/EQKANS4DuZ1Ou-AAd_RWyDMBnGLYawTSztKfCbgytkiilA?e=SpX5QS">...</a>
     * */
    private static final Map<IProto.RejectReasonType, String> REJECT_REASON_CODE_MAP = new HashMap<>();

    static {
        REJECT_REASON_CODE_MAP.put(COMMS_ERROR, "X0");
        REJECT_REASON_CODE_MAP.put(CANCELLED, "TM");
        REJECT_REASON_CODE_MAP.put(DECLINED_BY_CARD_PRE_COMMS, "Z1");
        REJECT_REASON_CODE_MAP.put(DECLINED_BY_CARD_POST_COMMS, "Z4");
        REJECT_REASON_CODE_MAP.put(SIGNATURE_REJECTED, "TL");
        REJECT_REASON_CODE_MAP.put(USER_TIMEOUT, "TO");
        REJECT_REASON_CODE_MAP.put(PLB_RESTRICTED_ITEM, "PI");
        REJECT_REASON_CODE_MAP.put(CARD_REMOVED, "RC");
        REJECT_REASON_CODE_MAP.put(CHIP_ERROR, "TY");
        REJECT_REASON_CODE_MAP.put(POWER_FAIL, "PF");
        REJECT_REASON_CODE_MAP.put(PROTOCOL_TASKS_FAILED, "ZZ");
        REJECT_REASON_CODE_MAP.put(MAC_FAILED, "X7");
        REJECT_REASON_CODE_MAP.put(INVALID_AMOUNT, "B5");
        REJECT_REASON_CODE_MAP.put(REFUND_LIMIT_EXCEEDED, "Q3");
        REJECT_REASON_CODE_MAP.put(REFUND_LIMIT_COUNT_EXCEEDED, "OD");
        REJECT_REASON_CODE_MAP.put(PASSWORD_CHECK_FAILED, "IP");
        REJECT_REASON_CODE_MAP.put(TRANS_NOT_ALLOWED, "NA");
        REJECT_REASON_CODE_MAP.put(PREAUTH_NOT_FOUND, "NF");
        REJECT_REASON_CODE_MAP.put(PREAUTH_ALREADY_CANCELLED, "NG");
        REJECT_REASON_CODE_MAP.put(AMOUNT_EXCEEDS_PREAUTH, "CA");
        REJECT_REASON_CODE_MAP.put(PREAUTH_NOT_ALLOWED_FOR_CARD, "CB");
        REJECT_REASON_CODE_MAP.put(PREAUTH_TRANS_LIMIT_EXCEEDED, "PT");
        REJECT_REASON_CODE_MAP.put(PREAUTH_EXISTS, "PE");
        REJECT_REASON_CODE_MAP.put(CONNECT_FAILED, "CE");
        REJECT_REASON_CODE_MAP.put(CARD_TYPE_NOT_ALLOWED, "TV");
        REJECT_REASON_CODE_MAP.put(CARD_NOT_ACCEPTED, "TX");
        REJECT_REASON_CODE_MAP.put(DUPLICATE_TXNREF, "B9");
        REJECT_REASON_CODE_MAP.put(DUPLICATE_SESSION, "B8");
        REJECT_REASON_CODE_MAP.put(TOTAL_OFFLINE_TRANS_AMOUNT_LIMIT_EXCEEDED, "CC");
        REJECT_REASON_CODE_MAP.put(OFFLINE_TRANS_AMOUNT_LIMIT_EXCEEDED, "CF");
        REJECT_REASON_CODE_MAP.put(OFFLINE_TRANS_COUNT_LIMIT_EXCEEDED, "CD");
        REJECT_REASON_CODE_MAP.put(TRANSACTION_NOT_FOUND, "NT");
        REJECT_REASON_CODE_MAP.put(VOID_NOT_ALLOWED_FOR_REFUND, "VR");
        REJECT_REASON_CODE_MAP.put(ALREADY_VOIDED, "VA");
        REJECT_REASON_CODE_MAP.put(VOID_NOT_ALLOWED_FOR_ADVICE, "VB");
        REJECT_REASON_CODE_MAP.put(TRANSACTION_ALREADY_SETTLED, "VS");
        REJECT_REASON_CODE_MAP.put(OFFLINE_VOID_NOT_ALLOWED_FOR_ONLINE_APPROVED_TRANSACTION, "VO");
        REJECT_REASON_CODE_MAP.put(BATCH_UPLOAD_FAILED, "UF");
    }

    private final Hashtable<String, MsgDefinition> rspCodeErrorMap = populateRspCodeErrorMap();
    private static final String TEXT_SYSTEM_ERROR_MIXED_CASE = "System Error";
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

    /**
     * Populate the transaction fields with the correct values based on the response code
     * @param protocol {@link TProtocol} object
     * @param responseCode to be used
     * @param transType (nullable) transaction type to match on. If null, matches any trans type
     * @return protocol object with all members if found, else it will return the protocol object as is
     * */
    public TProtocol populateProtocolRecord( TProtocol protocol, String responseCode, EngineManager.TransType transType ){
        if( protocol == null ){
            Timber.e( "TProtocol object passed in is null" );
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

    public TProtocol populateProtocolRecord( TProtocol protocol, String responseCode ) {
        return populateProtocolRecord( protocol, responseCode, null );
    }

    /**
     * Populate the transaction fields with the correct values based on the response code
     * @param protocol {@link TProtocol} object
     * @param rejectReasonType {@link IProto.RejectReasonType} be used
     * @return protocol with all values as expected if found. Else will return the protocol layer as is
     * */
    public TProtocol populateProtocolRecord( TProtocol protocol, IProto.RejectReasonType rejectReasonType ){
        if( protocol == null ){
            Timber.e( "TProtocol object passed in is null" );
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
     * @param rejectReasonType {@link IProto.RejectReasonType} be used
     * @param errorText override text passed from code with more descriptive decline reason, e.g. card expired
     * @return protocol with all values as expected if found. Else will return the protocol layer as is
     * */
    public TProtocol populateProtocolRecord( TProtocol protocol, IProto.RejectReasonType rejectReasonType, String errorText, EngineManager.TransType transType ){
        protocol = populateProtocolRecord( protocol, rejectReasonType );
        if( errorText != null && protocol != null ) {
            // override text
            protocol.setAdditionalResponseText( errorText );
            protocol.setCardAcceptorPrinterData( (TEXT_DECLINED_NEWLINE + errorText).toUpperCase() );
            protocol.setPosResponseText( errorText.toUpperCase() );
        }

        return protocol;
    }

    public TProtocol populateProtocolRecord( TProtocol protocol, IProto.RejectReasonType rejectReasonType, String errorText ) {
        return populateProtocolRecord( protocol, rejectReasonType, errorText, null );
    }

    private Hashtable<String, MsgDefinition> populateRspCodeErrorMap() {
        Hashtable<String, MsgDefinition> rspCodeErrMap = new Hashtable<>();

        // internal errors
        // IMPORTANT: when defining new response codes, add them to this spreadsheet and publish for support purposes https://pceftpos.sharepoint.com/:x:/s/IAAS/EQKANS4DuZ1Ou-AAd_RWyDMBnGLYawTSztKfCbgytkiilA?e=SpX5QS
        // Fields length restrictions:
        // Receipt Message: max 24 characters per line;
        // POS Message: max 20 characters per line, max 2 lines.
        rspCodeErrMap.put( "X0", new MsgDefinition( "Cancelled\nNo Response",              "CANCELLED",                                   "NO RESPONSE"           , "X0") );
        rspCodeErrMap.put( "RC", new MsgDefinition( "Cancelled\nCard Removed",             "CANCELLED\nCARD REMOVED",                     "CARD REMOVED"          , "RC") );
        rspCodeErrMap.put( "S1", new MsgDefinition( "Sig Not Supported",                   "DECLINED\nSIG NOT ALLOWED",                   "SIG NOT ALLOWED"       , "S1") );
        rspCodeErrMap.put( "X7", new MsgDefinition( "TRAN CANCELLED\nSYSTEM ERROR",        "DECLINED CODE X7\nSYSTEM ERROR",              "DECLINED X7"           , "X7") );
        rspCodeErrMap.put( "TY", new MsgDefinition( "Cancelled\nCard Read Failed",         "CANCELLED\nCARD READ FAILED",                 "CARD READ FAILED"      , "TY") );
        rspCodeErrMap.put( "TM", new MsgDefinition( "Operator Cancelled",                  "OPERATOR CANCELLED",                          "OPERATOR CANCELLED"    , "TM") );
        rspCodeErrMap.put( "TO", new MsgDefinition( "Operator Timeout",                    "OPERATOR TIMEOUT",                            "OPERATOR TIMEOUT"      , "TO") );
        rspCodeErrMap.put( "PI", new MsgDefinition( "Restricted Item",                     "RESTRICTED ITEM",                             "RESTRICTED ITEM"       , "PI") );
        rspCodeErrMap.put( "PF", new MsgDefinition( "Power Fail",                          "POWER FAIL",                                  "POWER FAIL"            , "PF") );
        rspCodeErrMap.put( "ZZ", new MsgDefinition( "Logon Failed",                        "LOGON FAILED",                                "LOGON FAILED"          , "ZZ") );
        rspCodeErrMap.put( "IP", new MsgDefinition( "Incorrect Password",                  "INCORRECT PASSWORD",                          "INCORRECT PASSWORD"    , "IP") );
        rspCodeErrMap.put( "NA", new MsgDefinition( "Not Allowed",                         "NOT ALLOWED",                                 "NOT ALLOWED"           , "NA") );
        rspCodeErrMap.put( "NF", new MsgDefinition( "Original Pre-Auth\nNot Found",         "CANCELLED\nPRE-AUTH NOT FOUND",                "PRE-AUTH NOT FOUND"     , "NF") );
        rspCodeErrMap.put( "NG", new MsgDefinition( "Pre-Auth Already\nCancelled",          "PRE-AUTH\nALREADY CANCELLED",                  "ALREADY CANCELLED"     , "NG") );
        rspCodeErrMap.put( "CA", new MsgDefinition( "Amt Exceeds\nPre-Auth Amt",            "CANCELLED\nAMT EXCEEDS PRE-AUTH",              "AMT EXCEEDS PRE-AUTH"   , "CA") );
        rspCodeErrMap.put( "CB", new MsgDefinition( "Card Does Not\nAllow Pre-Auth",        "CANCELLED\nPRE-AUTH NOT ALLOWED",              "PRE-AUTH NOT ALLOWED"   , "CB") );
        rspCodeErrMap.put( "B5", new MsgDefinition( "Invalid Amount",                      "CANCELLED\nINVALID AMOUNT",                   "INVALID AMOUNT"        , "B5") );
        rspCodeErrMap.put( "PT", new MsgDefinition( "Pre-Auth Exceeds\nTransaction Limit",  "DECLINED\nPRE-AUTH NOT ALLOWED",               "PRE-AUTH NOT ALLOWED"   , "PT") );
        rspCodeErrMap.put( "PE", new MsgDefinition( "Pre-Auth Already\nExists",             "DECLINED\nPRE-AUTH EXISTS",                    "PRE-AUTH EXISTS"        , "PE") );
        rspCodeErrMap.put( "Y1", new MsgDefinition( "Approved",                            "APPROVED",                                    "APPROVED"              , "Y1") );
        rspCodeErrMap.put( "Y3", new MsgDefinition( "Approved",                            "APPROVED",                                    "APPROVED"              , "Y3") );
        rspCodeErrMap.put( "Z1", new MsgDefinition( "Declined Z1\nCard Declined",          "DECLINED Z1\nCARD DECLINED",                  "Z1 CARD DECLINED"      , "Z1") );
        rspCodeErrMap.put( "Z3", new MsgDefinition( "Declined Z3\nCard Declined",          "DECLINED Z3\nCARD DECLINED",                  "Z3 CARD DECLINED"      , "Z3") );
        rspCodeErrMap.put( "Z4", new MsgDefinition( "Declined Z4\nCard Declined",          "DECLINED Z4\nCARD DECLINED",                  "Z4 CARD DECLINED"      , "Z4") );
        rspCodeErrMap.put( "TL", new MsgDefinition( "Signature Error",                     "DECLINED\nSIGNATURE ERROR",                   "SIGNATURE ERROR"       , "TL") );
        rspCodeErrMap.put( "Q3", new MsgDefinition( "Refund Limit\nExceeded",              "CANCELLED REFUND\nLIMIT EXCEEDED",            "REFND LIMIT EXCEEDED" , "Q3") );
        rspCodeErrMap.put( "OD", new MsgDefinition( "Refund Count\nExceeded",              "CANCELLED REFUND\nCOUNT EXCEEDED",            "REFND COUNT EXCEEDED" , "OD") );
        rspCodeErrMap.put( "TV", new MsgDefinition( "Card Type\nNot Allowed",              "CANCELLED\nCARD TYPE NOT ALLOWED",            "CARD TYPE DISALLOWED" , "TV") );
        rspCodeErrMap.put( "TX", new MsgDefinition( "Card Not Accepted",                   "CANCELLED\nCARD NOT ACCEPTED",                "CARD NOT ACCEPTED" , "TX") );
        rspCodeErrMap.put( "B9", new MsgDefinition( "Duplicate\nTransaction Reference",    "CANCELLED\nDUPLICATE\nTRANSACTION REFERENCE", "DUPLCT TRANSACTION\nREFERENCE" , "B9") );
        rspCodeErrMap.put( "B8", new MsgDefinition( "Duplicate\nSession ID",               "CANCELLED\nDUPLICATE SESSION ID",             "DUPLICATE SESSION ID" , "B8") );
        rspCodeErrMap.put( "CC", new MsgDefinition( "Total Offline Trans\nLimit Exceeded", "CANCELLED TOTAL OFFLINE\nLIMIT EXCEEDED",     "TOTAL OFFLINE LIMIT\nEXCEEDED" , "CC") );
        rspCodeErrMap.put( "CF", new MsgDefinition( "Offline Trans\nLimit Exceeded",       "CANCELLED OFFLINE\nLIMIT EXCEEDED",           "OFFLINE LIMIT\nEXCEEDED" , "CF") );
        rspCodeErrMap.put( "CD", new MsgDefinition( "Offline Trans\nCount Exceeded",       "CANCELLED OFFLINE\nCOUNT EXCEEDED",           "OFFLINE COUNT\nEXCEEDED" , "CD") );
        rspCodeErrMap.put( "CE", new MsgDefinition( "Connection Failure",                  "DECLINED\nCONNECT FAILED",                    "CONNECT FAILED" , "CE") );
        rspCodeErrMap.put( "NT", new MsgDefinition( "Transaction\nNot Found",              "CANCELLED\nTRANSACTION NOT FOUND",            "TRANSACTION\nNOT FOUND" , "NT") );
        rspCodeErrMap.put( "VR", new MsgDefinition( "Void Not Allowed\nFor Refund",        "CANCELLED\nVOID NOT ALLOWED\nFOR REFUND",     "VOID NOT ALLOWED\nFOR REFUND" , "VR") );
        rspCodeErrMap.put( "VA", new MsgDefinition( "Already Voided",                      "CANCELLED\nALREADY VOIDED",                   "ALREADY VOIDED" , "VA") );
        rspCodeErrMap.put( "VB", new MsgDefinition( "Can't Void\nAlready Complete",        "CANCELLED\nALREADY COMPLETED",                "ALREADY COMPLETED" , "VB") );
        rspCodeErrMap.put( "VS", new MsgDefinition( "Can't Void\nAlready Settled",         "CANCELLED\nALREADY SETTLED",                  "TRANSACTION ALREADY\nSETTLED" , "VS") );
        rspCodeErrMap.put( "VO", new MsgDefinition( "Can't Void\nOnline Approved",         "CANCELLED\nOFFLINE VOID NOT ALLOWED",         "VOID NOT ALLOWED\nONLINE APPROVED" , "VO") );
        rspCodeErrMap.put( "UF", new MsgDefinition( "Batch Upload\nFailed",                "DECLINED\nBATCH UPLOAD FAILED",               "BATCH UPLOAD FAILED", "UF"));

        // IMPORTANT: when defining new response codes, add them to this spreadsheet and publish for support purposes https://pceftpos.sharepoint.com/:x:/s/IAAS/EQKANS4DuZ1Ou-AAd_RWyDMBnGLYawTSztKfCbgytkiilA?e=SpX5QS
        // mapping of the 3 digit DE39 to the 2 digit response code is defined in the termapp.iso spec (in sharepoint), which states it's in accordance with the mapping specified in ISO8583(1993)

        // Fields length restrictions:
        // Receipt Message: max 24 characters per line;
        // POS Message: max 20 characters per line, max 2 lines.

        // this one is special - only applies to trans types in the list
        rspCodeErrMap.put( "800", new MsgDefinition("Approved",                            "APPROVED",                                    "APPROVED",                   "00",
                new ArrayList<>(Arrays.asList(EngineManager.TransType.LOGON, EngineManager.TransType.AUTO_LOGON, EngineManager.TransType.RSA_LOGON))));
        rspCodeErrMap.put( "000", new MsgDefinition("Approved",                            "APPROVED",                                    "APPROVED",                   "00"));
        rspCodeErrMap.put( "001", new MsgDefinition("SIGNATURE REQUIRED",                  "SIGNATURE REQUIRED",                          "SIGNATURE REQUIRED",         "08"));
        rspCodeErrMap.put( "100", new MsgDefinition("Do Not Honour",                       "DO NOT HONOUR",                               "DO NOT HONOUR",              "05"));
        rspCodeErrMap.put( "101", new MsgDefinition("Expired Card",                        "EXPIRED CARD",                                "EXPIRED CARD",               "54"));
        rspCodeErrMap.put( "102", new MsgDefinition("Suspected Fraud",                     "SUSPECTED FRAUD",                             "SUSPECTED FRAUD",            "59"));
        rspCodeErrMap.put( "103", new MsgDefinition("Contact Acquirer",                    "CONTACT ACQUIRER",                            "CONTACT ACQUIRER",           "60"));
        rspCodeErrMap.put( "104", new MsgDefinition("Restricted Card",                     "RESTRICTED CARD",                             "RESTRICTED CARD",            "62"));
        rspCodeErrMap.put( "105", new MsgDefinition("Call Acquirer Security",              "CALL ACQUIRER SECURITY",                      "CALL ACQ. SECURITY",         "66"));
        rspCodeErrMap.put( "106", new MsgDefinition("PIN Tries Exceeded",                  "PIN TRIES EXCEEDED",                          "PIN TRIES EXCEEDED",         "75"));
        rspCodeErrMap.put( "107", new MsgDefinition("Refer To Card Issuer",                "REFER TO CARD ISSUER",                        "REFER TO CARD ISSR",         "01"));
        rspCodeErrMap.put( "108", new MsgDefinition("Rfr To Issuer Special",               "RFR TO ISSUER SPECIAL",                       "RFR TO ISSUER SPECL",        "02"));
        rspCodeErrMap.put( "109", new MsgDefinition("Invalid Merchant",                    "INVALID MERCHANT",                            "INVALID MERCHANT",           "03"));
        rspCodeErrMap.put( "110", new MsgDefinition("Invalid Amount",                      "INVALID AMOUNT",                              "INVALID AMOUNT",             "13"));
        rspCodeErrMap.put( "111", new MsgDefinition("Invalid Card Number",                 "INVALID CARD NUMBER",                         "INVALID CARD NUMBER",        "14"));
        rspCodeErrMap.put( "113", new MsgDefinition("Unacceptable Fee",                    "UNACCEPTABLE FEE",                            "UNACCEPTABLE FEE",           "23"));
        rspCodeErrMap.put( "114", new MsgDefinition("No Acct Of Rqstd Type",               "NO ACCT OF RQSTD TYPE",                       "NO ACCT OF\nRQSTD TYPE",      "39"));
        rspCodeErrMap.put( "115", new MsgDefinition("Func Not Supported",                  "FUNC NOT SUPPORTED",                          "FUNC NOT SUPPORTED",         "40"));
        rspCodeErrMap.put( "116", new MsgDefinition("Not Sufficient Funds",                "NOT SUFFICIENT FUNDS",                        "NOT SUFFICIENT FUNDS",       "51"));
        rspCodeErrMap.put( "117", new MsgDefinition("Incorrect PIN",                       "INCORRECT PIN",                               "INCORRECT PIN",              "55"));
        rspCodeErrMap.put( "118", new MsgDefinition("No Card Record",                      "NO CARD RECORD",                              "NO CARD RECORD",             "56"));
        rspCodeErrMap.put( "119", new MsgDefinition("TNP Cardholder",                      "TNP CARDHOLDER\nDECLINED",                    "TNP CARDHOLDER",             "57"));
        rspCodeErrMap.put( "120", new MsgDefinition("TNP Terminal",                        "TNP TERMINAL",                                "TNP TERMINAL",               "58"));
        rspCodeErrMap.put( "121", new MsgDefinition("Excd Withdraw Amt Lim",               "EXCD WITHDRAW AMT LIM",                       "EXCD WITHDRW AMT LIM",       "61"));
        rspCodeErrMap.put( "122", new MsgDefinition("Security Violation",                  "SECURITY VIOLATION",                          "SECURITY VIOLATION",         "63"));
        rspCodeErrMap.put( "123", new MsgDefinition("Excd Withdraw Frq Lim",               "EXCD WITHDRAW FRQ LIM",                       "EXCD WITHDRW FRQ LIM",       "65"));
        rspCodeErrMap.put( "124", new MsgDefinition("Violation Of Law",                    "VIOLATION OF LAW",                            "VIOLATION OF LAW",           "93"));
        rspCodeErrMap.put( "195", new MsgDefinition("Unable to Dispense",                  "UNABLE TO DISPENSE",                          "UNABLE TO DISPENSE",         RESPONSE_CODE_UNDEFINED_SYSTEM_ERROR));
        rspCodeErrMap.put( "196", new MsgDefinition("Cash Terminal Inactive",              "CASH TERMINAL INACTIVE",                      "CASH TERMNL INACTIVE",       RESPONSE_CODE_UNDEFINED_SYSTEM_ERROR));
        rspCodeErrMap.put( "197", new MsgDefinition("Exceeds Max Withdrawal",              "EXCEEDS MAX WITHDRAWAL",                      "EXCEEDS MAX WITHDRWL",       RESPONSE_CODE_UNDEFINED_SYSTEM_ERROR));
        rspCodeErrMap.put( "198", new MsgDefinition("Trans Already Pending",               "TRANS ALREADY PENDING",                       "TRANS ALRDY PENDING",        RESPONSE_CODE_UNDEFINED_SYSTEM_ERROR));
        rspCodeErrMap.put( "199", new MsgDefinition("Surcharge Unconfirmed",               "SURCHARGE UNCONFIRMED",                       "SURCHARGE UNCNFIRMED",       RESPONSE_CODE_UNDEFINED_SYSTEM_ERROR));
        rspCodeErrMap.put( "200", new MsgDefinition("Do Not Honour Pick Up",               "DO NOT HONOUR PICK UP",                       "DO NOT HONOUR PICKUP",       "04"));
        rspCodeErrMap.put( "201", new MsgDefinition("Expired Card Pick Up",                "EXPIRED CARD PICK UP",                        "EXPIRED CARD PICK UP",       "33"));
        rspCodeErrMap.put( "202", new MsgDefinition("Sspected Fraud PickUp",               "SSPECTED FRAUD PICKUP",                       "SSPCTED FRAUD PICKUP",       "34"));
        rspCodeErrMap.put( "203", new MsgDefinition("Refer Acquirer PickUp",               "REFER ACQUIRER PICKUP",                       "REFER ACQRER PICK UP",       "35"));
        rspCodeErrMap.put( "204", new MsgDefinition("Restrictd Card PickUp",               "RESTRICTD CARD PICKUP",                       "RESTRCTD CARD PICKUP",       "36"));
        rspCodeErrMap.put( "205", new MsgDefinition("Call Acq Sec Pick Up",                "CALL ACQ SEC PICK UP",                        "CALL ACQ SEC PICK UP",       "37"));
        rspCodeErrMap.put( "206", new MsgDefinition("PIN Tries Excd PickUp",               "PIN TRIES EXCD PICKUP",                       "PINTRIES EXCD PICKUP",       "38"));
        rspCodeErrMap.put( "207", new MsgDefinition("Special Cond. Pick Up",               "SPECIAL COND. PICK UP",                       "SPECIAL COND PICK UP",       "07"));
        rspCodeErrMap.put( "208", new MsgDefinition("Lost Card Pick Up",                   "LOST CARD PICK UP",                           "LOST CARD PICK UP",          "41"));
        rspCodeErrMap.put( "209", new MsgDefinition("Stolen Card Pick Up",                 "STOLEN CARD PICK UP",                         "STOLEN CARD PICK UP",        "43"));
        rspCodeErrMap.put( "301", new MsgDefinition("File Actn Not Supprtd",               "FILE ACTN NOT SUPPRTD",                       "FILE ACTN NOT SUPRTD",       "24"));
        rspCodeErrMap.put( "302", new MsgDefinition("File Record Not Found",               "FILE RECORD NOT FOUND",                       "FILE RECORD NOT FND",        "25"));
        rspCodeErrMap.put( "304", new MsgDefinition("File Field Edit Error",               "FILE FIELD EDIT ERROR",                       "FILE FIELD EDIT ERR",        "27"));
        rspCodeErrMap.put( "305", new MsgDefinition("File Locked Out",                     "FILE LOCKED OUT",                             "FILE LOCKED OUT",            "28"));
        rspCodeErrMap.put( "306", new MsgDefinition("File Actn Not Success",               "FILE ACTN NOT SUCCESS",                       "FILE ACTN NOT SUCCSS",       "29"));
        rspCodeErrMap.put( "308", new MsgDefinition("File Duplct Rec Rjctd",               "FILE DUPLCT REC RJCTD",                       "FILE DUPLCT REC RJCD",       "26"));
        rspCodeErrMap.put( "500", new MsgDefinition("Settlement In Balance",               "SETTLEMENT IN BALANCE",                       "SETTLEMNT IN BALANCE",       "97",
                new ArrayList<>(Arrays.asList(EngineManager.TransType.RECONCILIATION, EngineManager.TransType.RECONCILIATION_AUTO, EngineManager.TransType.LAST_RECONCILIATION_AUTO))));
        rspCodeErrMap.put( "501", new MsgDefinition("Settlement Out of Balance",           "SETTLEMENT\nOUT OF BALANCE",                  "SETTLEMNT OUT OF BAL",       RESPONSE_CODE_UNDEFINED_SYSTEM_ERROR,
                new ArrayList<>(Arrays.asList(EngineManager.TransType.RECONCILIATION, EngineManager.TransType.RECONCILIATION_AUTO, EngineManager.TransType.LAST_RECONCILIATION_AUTO))));
        rspCodeErrMap.put( "902", new MsgDefinition("Invalid Transaction",                 "INVALID TRANSACTION",                         "INVALID TRANSACTION",        "12"));
        rspCodeErrMap.put( "903", new MsgDefinition("Re-enter Transaction",                "RE-ENTER TRANSACTION",                        "RE-ENTER TRANSACTION",       "19"));
        rspCodeErrMap.put( "904", new MsgDefinition("Format Error",                        "FORMAT ERROR",                                "FORMAT ERROR",               "30"));
        rspCodeErrMap.put( "905", new MsgDefinition("Acquirer Not Supported",              "ACQUIRER NOT SUPPORTED",                      "ACQUIRER NOT SUPPRTD",       "31"));
        rspCodeErrMap.put( "906", new MsgDefinition("Cutover In Progress",                 "CUTOVER IN PROGRESS",                         "CUTOVER IN PROGRESS",        "90"));
        rspCodeErrMap.put( "907", new MsgDefinition("Issr/Swtch Inoperative",              "ISSR/SWTCH INOPERATIVE",                      "ISSR/SWTCH INOPERTVE",       "91"));
        rspCodeErrMap.put( "908", new MsgDefinition("Routing Error",                       "ROUTING ERROR",                               "ROUTING ERROR",              "92"));
        rspCodeErrMap.put( "909", new MsgDefinition("System Malfunction",                  "SYSTEM MALFUNCTION",                          "SYSTEM MALFUNCTION",         "96"));
        rspCodeErrMap.put( "911", new MsgDefinition("Card Issuer Timed Out",               "CARD ISSUER TIMED OUT",                       "CARD ISSUER TIMEOUT",        "68"));
        rspCodeErrMap.put( "913", new MsgDefinition("Duplicate Transmission",              "DUPLICATE TRANSMISSION",                      "DUPLICATE TRANSMISSN",       "94"));
        rspCodeErrMap.put( "915", new MsgDefinition("Cutover Or Chkpnt Err",               "CUTOVER OR CHKPNT ERR",                       "CUTOVER OR CHKPT ERR",       "95"));
        rspCodeErrMap.put( "921", new MsgDefinition("Security Err No Action",              "SECURITY ERR NO ACTION",                      "SECURITY ERR NO ACTN",       "21"));
        rspCodeErrMap.put( "923", new MsgDefinition("Request In Progress",                 "REQUEST IN PROGRESS",                         "REQUEST IN PROGRESS",        "09"));

        return rspCodeErrMap;
    }

}
