package com.linkly.libengine.engine.protocol.iso8583;

import static com.linkly.libengine.engine.protocol.IProto.RejectReasonType.AMOUNT_EXCEEDS_PREAUTH;
import static com.linkly.libengine.engine.protocol.IProto.RejectReasonType.BATCH_UPLOAD_FAILED;
import static com.linkly.libengine.engine.protocol.IProto.RejectReasonType.CANCELLED;
import static com.linkly.libengine.engine.protocol.IProto.RejectReasonType.CARD_REMOVED;
import static com.linkly.libengine.engine.protocol.IProto.RejectReasonType.CARD_TYPE_NOT_ALLOWED;
import static com.linkly.libengine.engine.protocol.IProto.RejectReasonType.CHIP_ERROR;
import static com.linkly.libengine.engine.protocol.IProto.RejectReasonType.COMMS_ERROR;
import static com.linkly.libengine.engine.protocol.IProto.RejectReasonType.DECLINED_BY_CARD_POST_COMMS;
import static com.linkly.libengine.engine.protocol.IProto.RejectReasonType.DECLINED_BY_CARD_PRE_COMMS;
import static com.linkly.libengine.engine.protocol.IProto.RejectReasonType.INVALID_AMOUNT;
import static com.linkly.libengine.engine.protocol.IProto.RejectReasonType.ISSUER_NOT_AVAILABLE;
import static com.linkly.libengine.engine.protocol.IProto.RejectReasonType.MAC_FAILED;
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
import static com.linkly.libengine.engine.protocol.IProto.RejectReasonType.TRANS_NOT_ALLOWED;
import static com.linkly.libengine.engine.protocol.IProto.RejectReasonType.USER_TIMEOUT;

import com.linkly.libengine.engine.protocol.IProto;
import com.linkly.libengine.engine.transactions.properties.TProtocol;
import com.linkly.libmal.global.util.Util;

import java.util.EnumMap;
import java.util.HashMap;

import timber.log.Timber;

/**
 * Package private class. Maps {@link IProto.RejectReasonType} to Till specific response codes
 * as well as contains maps of Till host response codes
 */
final class As2805TillRspCodeMap extends As2805RspCodeMap {
    private static final String APPROVED_CAPS = "APPROVED";
    private static final String DECLINED_CONTACT_ISSUER = "DECLINED\nCONTACT ISSUER";
    private static final String CONTACT_ISSUER = "Contact Issuer";
    public static final String APPROVED = "Approved";
    public static final String CONTACT_ISSUER_CAPS = "CONTACT ISSUER";
    public static final String POWER_FAIL_CAPS = "POWER FAIL";
    /**
     * Map whose values point to keys of {@link As2805TillRspCodeMap#rspCodeErrorMap} map
     */
    private static final EnumMap<IProto.RejectReasonType, String> REJECT_REASON_CODE_MAP = new EnumMap<>(IProto.RejectReasonType.class);

    static {
        REJECT_REASON_CODE_MAP.put(COMMS_ERROR, "X0");
        REJECT_REASON_CODE_MAP.put(ISSUER_NOT_AVAILABLE, "91");
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
        REJECT_REASON_CODE_MAP.put(CARD_TYPE_NOT_ALLOWED, "TV");
        REJECT_REASON_CODE_MAP.put(BATCH_UPLOAD_FAILED, "UF");
    }

    private final HashMap<String, MsgDefinition> rspCodeErrorMap = populateRspCodeErrorMap();
    private static final String TEXT_SYSTEM_ERROR_MIXED_CASE = "System Error";
    private static final String TEXT_SYSTEM_ERROR_UPPER_CASE = TEXT_SYSTEM_ERROR_MIXED_CASE.toUpperCase();
    private static final String DECLINED = "DECLINED\n";
    private static final String TEXT_DECLINED_SYSTEM_ERROR_UPPER_CASE = DECLINED + TEXT_SYSTEM_ERROR_UPPER_CASE;
    private static final String TEXT_DECLINED_NEWLINE = DECLINED;

    private String getResponseCodeErrorDisplay(String responseCode) {
        return super.getResponseCodeErrorDisplay(responseCode,
                this.rspCodeErrorMap,
                TEXT_SYSTEM_ERROR_MIXED_CASE);
    }


    private String getResponseCodeErrorReceipt(String responseCode) {
        return super.getResponseCodeErrorReceipt(responseCode, this.rspCodeErrorMap);
    }

    private String getResponseCodeErrorPos(String responseCode) {
        return super.getResponseCodeErrorPos(responseCode, this.rspCodeErrorMap);
    }

    /**
     * Populate the transaction fields with the correct values based on the response code
     *
     * @param protocol     {@link TProtocol} object
     * @param responseCode to be used
     * @return protocol object with all members if found, else it will return the protocol object as is
     */
    public TProtocol populateProtocolRecord(TProtocol protocol, String responseCode) {
        if (protocol == null) {
            Timber.e("TProtocol object passed in is null");
            return null;
        }

        if (!Util.isNullOrEmpty(responseCode)) {
            // response codes are 2 chars so safe to set this in both pos and server response code fields
            protocol.setPosResponseCode(responseCode);
            protocol.setServerResponseCode(responseCode);
            protocol.setAdditionalResponseText(this.getResponseCodeErrorDisplay(responseCode));
            protocol.setCardAcceptorPrinterData(this.getResponseCodeErrorReceipt(responseCode));
            protocol.setPosResponseText(this.getResponseCodeErrorPos(responseCode));
        } else {
            Timber.e("Response code is Null or empty for transRec [%s]", protocol.getStan());
        }
        return protocol;
    }

    /**
     * Populate the transaction fields with the correct values based on the response code
     *
     * @param protocol         {@link TProtocol} object
     * @param rejectReasonType {@link IProto.RejectReasonType} be used
     * @return protocol with all values as expected if found. Else will return the protocol layer as is
     */
    public TProtocol populateProtocolRecord(TProtocol protocol, IProto.RejectReasonType rejectReasonType) {
        if (protocol == null) {
            Timber.e("TProtocol object passed in is null");
            return null;
        }

        // search table for internal response code
        if (REJECT_REASON_CODE_MAP.containsKey(rejectReasonType)) {
            return populateProtocolRecord(protocol, REJECT_REASON_CODE_MAP.get(rejectReasonType));
        }

        Timber.e("Internal Response Code not found in lookup table [%s]", rejectReasonType.toString());
        return protocol;
    }

    /**
     * Populate the transaction fields with the correct values based on the response code
     *
     * @param protocol         {@link TProtocol} object
     * @param rejectReasonType {@link IProto.RejectReasonType} be used
     * @param errorText        override text passed from code with more descriptive decline reason, e.g. card expired
     * @return protocol with all values as expected if found. Else will return the protocol layer as is
     */
    public TProtocol populateProtocolRecord(TProtocol protocol, IProto.RejectReasonType rejectReasonType, String errorText) {
        protocol = populateProtocolRecord(protocol, rejectReasonType);
        if (errorText != null && protocol != null) {
            // override text
            protocol.setAdditionalResponseText(errorText);
            protocol.setCardAcceptorPrinterData((TEXT_DECLINED_NEWLINE + errorText).toUpperCase());
            protocol.setPosResponseText(errorText.toUpperCase());
        }

        return protocol;
    }

    private HashMap<String, MsgDefinition> populateRspCodeErrorMap() {
        HashMap<String, MsgDefinition> rspCodeErrMap = new HashMap<>();
        final String INCORRECT_PASSWORD = "Incorrect Password";
        final String NOT_ALLOWED = "Not Allowed";

        // internal errors
        rspCodeErrMap.put("X0", new MsgDefinition("Cancelled\nNo Response", "CANCELLED", "NO RESPONSE"));
        rspCodeErrMap.put("RC", new MsgDefinition("Cancelled\nCard Removed", "CANCELLED\nCARD REMOVED", "CARD REMOVED"));
        rspCodeErrMap.put("S1", new MsgDefinition("Sig Not Supported", "DECLINED\nSIG NOT ALLOWED", "SIG NOT ALLOWED"));
        rspCodeErrMap.put("Z4", new MsgDefinition(CONTACT_ISSUER, DECLINED_CONTACT_ISSUER, CONTACT_ISSUER_CAPS));
        rspCodeErrMap.put("X7", new MsgDefinition("TRAN CANCELLED\nSYSTEM ERROR", "DECLINED CODE X7\nSYSTEM ERROR", "DECLINED X7"));
        rspCodeErrMap.put("TY", new MsgDefinition("Cancelled\nCard Read Failed", "CANCELLED\nCARD READ FAILED", "CARD READ FAILED"));
        rspCodeErrMap.put("TM", new MsgDefinition("Operator Cancelled", "OPERATOR CANCELLED", "OPERATOR CANCELLED"));
        rspCodeErrMap.put("TO", new MsgDefinition("Operator Timeout", "OPERATOR TIMEOUT", "OPERATOR TIMEOUT"));
        rspCodeErrMap.put("PI", new MsgDefinition("Restricted Item", "RESTRICTED ITEM", "RESTRICTED ITEM"));
        rspCodeErrMap.put("PF", new MsgDefinition("Power Fail", POWER_FAIL_CAPS, POWER_FAIL_CAPS));
        rspCodeErrMap.put("ZZ", new MsgDefinition("Logon Failed", "LOGON FAILED", "LOGON FAILED"));
        // Whitespace is aligned with above lines
        rspCodeErrMap.put("IP", new MsgDefinition(INCORRECT_PASSWORD, INCORRECT_PASSWORD.toUpperCase(), INCORRECT_PASSWORD.toUpperCase()));
        rspCodeErrMap.put("NA", new MsgDefinition(NOT_ALLOWED, NOT_ALLOWED.toUpperCase(), NOT_ALLOWED.toUpperCase()));
        rspCodeErrMap.put("NF", new MsgDefinition("Original Pre-Auth\nNot Found", "CANCELLED\nPRE-AUTH NOT FOUND", "PRE-AUTH NOT FOUND"));
        rspCodeErrMap.put("NG", new MsgDefinition("Pre-Auth Already\nCancelled", "PRE-AUTH\nALREADY CANCELLED", "ALREADY CANCELLED"));
        rspCodeErrMap.put("CA", new MsgDefinition("Amt Exceeds\nPre-Auth Amt", "CANCELLED\nAMT EXCEEDS PREAUTH", "AMT EXCEEDS PRE-AUTH"));
        rspCodeErrMap.put("CB", new MsgDefinition("Card Does Not\nAllow Pr-eauth", "CANCELLED\nPRE-AUTH NOT ALLOWED", "PRE-AUTH NOT ALLOWED"));
        rspCodeErrMap.put("CE", new MsgDefinition("Connection Failure",            "DECLINED\nCONNECT FAILED",         "CONNECT FAILED" ) );
        rspCodeErrMap.put("B5", new MsgDefinition("Invalid Amount", "CANCELLED\nINVALID AMOUNT", "INVALID AMOUNT"));
        rspCodeErrMap.put("PT", new MsgDefinition("Pre-Auth Exceeds\nTransaction Limit", "DECLINED\nPREAUTH NOT ALLOWED", "PRE-AUTH NOT ALLOWED"));
        rspCodeErrMap.put("PE", new MsgDefinition("Pre-Auth Already\nExists", "DECLINED\nPRE-AUTH EXISTS", "PRE-AUTH EXISTS"));
        rspCodeErrMap.put("UF", new MsgDefinition("Batch Upload\nFailed", "DECLINED\nBATCH UPLOAD FAILED", "BATCH UPLOAD FAILED"));
        rspCodeErrMap.put("TV", new MsgDefinition("Card Type\nNot Allowed", "CANCELLED\nCARD TYPE NOT ALLOWED", "CARD TYPE DISALLOWED"));

        // response codes from host
        rspCodeErrMap.put("00", new MsgDefinition(APPROVED, APPROVED_CAPS, APPROVED_CAPS));
        rspCodeErrMap.put("01", new MsgDefinition(CONTACT_ISSUER, DECLINED_CONTACT_ISSUER, "CONTACT BANK"));
        rspCodeErrMap.put("04", new MsgDefinition("Contact Bank", "DECLINED\nCONTACT BANK", "CALL SUPERVISOR"));
        rspCodeErrMap.put("08", new MsgDefinition(APPROVED, APPROVED_CAPS, APPROVED_CAPS));
        rspCodeErrMap.put("12", new MsgDefinition("Invalid", "DECLINED\nINVALID", "INVALID TRANSACTION"));
        rspCodeErrMap.put("13", new MsgDefinition("Invalid Amount", "DECLINED\nINVALID AMOUNT", "INVALID AMOUNT"));
        rspCodeErrMap.put("14", new MsgDefinition("Invalid Card No", "DECLINED\nINVALID CARD NUMBER", "CARD NUMBER INVALID"));
        rspCodeErrMap.put("36", new MsgDefinition("Bank Unavailable", "DECLINED\nBANK UNAVAILABLE", "NO BANK DO MANUAL"));
        rspCodeErrMap.put("42", new MsgDefinition("No Account", "DECLINED\nNO ACCOUNT", "NO ACCOUNT"));
        rspCodeErrMap.put("39", new MsgDefinition("No Credit Account", "DECLINED\nNO CREDIT ACCOUNT", "NO CREDIT ACCOUNT"));
        rspCodeErrMap.put("51", new MsgDefinition("Contact Bank", "DECLINED\nCONTACT BANK", "CONTACT BANK"));
        rspCodeErrMap.put("52", new MsgDefinition("No Cheque Acct", "DECLINED\nNO CHEQUE ACCOUNT", "NO CHEQUE ACCOUNT"));
        rspCodeErrMap.put("53", new MsgDefinition("No Savings Acct", "DECLINED\nNO SAVINGS ACCOUNT", "NO SAVINGS ACCOUNT"));
        rspCodeErrMap.put("54", new MsgDefinition("Expired Card", "DECLINED\nEXPIRED CARD", "EXPIRED CARD"));
        rspCodeErrMap.put("55", new MsgDefinition("Incorrect PIN", "DECLINED\nINCORRECT PIN", "PIN ERROR"));
        rspCodeErrMap.put("61", new MsgDefinition("Over Card Limit", "DECLINED\nOVER CARD LIMIT", "OVER CARD LIMIT"));
        rspCodeErrMap.put("65", new MsgDefinition("Over Card Limit", "DECLINED\nOVER CARD LIMIT", "OVER CARD LIMIT"));
        rspCodeErrMap.put("75", new MsgDefinition("Exceed PIN Tries", "DECLINED\nEXCEED PIN TRIES", "EXCEED PIN TRIES"));
        rspCodeErrMap.put("76", new MsgDefinition(APPROVED, APPROVED_CAPS, APPROVED_CAPS));
        rspCodeErrMap.put("91", new MsgDefinition("Bank Unavailable", "DECLINED\nBANK UNAVAILABLE", "NO BANK DO MANUAL"));
        rspCodeErrMap.put("96", new MsgDefinition("Retry", "DECLINED\nRETRY", "SYSTEM MALFUNCT'N"));
        rspCodeErrMap.put("97", new MsgDefinition("Settlement\nTotals Reset", "SETTLEMENT\nTOTALS RESET", "TOTALS RESET"));
        rspCodeErrMap.put("OD", new MsgDefinition("Refund Limit", "DECLINED\nREFUND LIMIT", "OVER REFUND LIMIT"));
        rspCodeErrMap.put("Q3", new MsgDefinition("Refund Limit", "DECLINED\nREFUND LIMIT", "OVER REFUND LIMIT"));
        rspCodeErrMap.put("Q5", new MsgDefinition("Cancelled\nPower Fail", "CANCELLED\nPOWER FAIL", POWER_FAIL_CAPS));
        rspCodeErrMap.put("Q6", new MsgDefinition("Cancelled\nNo Manual Card", "CANCELLED\nNO MANUAL CARD", "NO MANUAL CARD"));
        rspCodeErrMap.put("RI", new MsgDefinition("Cancelled\nItems Not Allowed", "CANCELLED\nITEMS NOT ALLOWED", "ITEMS NOT ALLOWED"));
        rspCodeErrMap.put("N0", new MsgDefinition("CPAT Seq Error", "DECLINED\nCPAT SEQUENCE ERROR", "CPAT SEQUENCE ERROR"));
        rspCodeErrMap.put("N1", new MsgDefinition("", "", "LOGON SUCCESSFUL"));
        rspCodeErrMap.put("N2", new MsgDefinition("Pinpad ID Error", "DECLINED\nPINPAD ID ERROR", "PINPAD ID ERROR"));
        rspCodeErrMap.put("NH", new MsgDefinition("Unknown Terminal", "UNKNOWN\nTERMINAL", "UNKNOWN TERMINAL"));
        rspCodeErrMap.put("CU", new MsgDefinition("Invalid Currency", "DECLINED\nINVALID CURRENCY", "INVALID CURRENCY"));
        rspCodeErrMap.put("N3", new MsgDefinition("", "", APPROVED_CAPS));
        rspCodeErrMap.put("Y1", new MsgDefinition(APPROVED, APPROVED_CAPS, APPROVED_CAPS));
        rspCodeErrMap.put("Y3", new MsgDefinition(APPROVED, APPROVED_CAPS, APPROVED_CAPS));
        rspCodeErrMap.put("Z1", new MsgDefinition(CONTACT_ISSUER, DECLINED_CONTACT_ISSUER, CONTACT_ISSUER_CAPS));
        rspCodeErrMap.put("Z3", new MsgDefinition(CONTACT_ISSUER, DECLINED_CONTACT_ISSUER, CONTACT_ISSUER_CAPS));
        rspCodeErrMap.put("TL", new MsgDefinition("Signature Error", "DECLINED\nSIGNATURE ERROR", "SIGNATURE ERROR"));

        rspCodeErrMap.put("02", new MsgDefinition(TEXT_SYSTEM_ERROR_MIXED_CASE, TEXT_DECLINED_SYSTEM_ERROR_UPPER_CASE, TEXT_SYSTEM_ERROR_UPPER_CASE));
        rspCodeErrMap.put("03", new MsgDefinition(TEXT_SYSTEM_ERROR_MIXED_CASE, TEXT_DECLINED_SYSTEM_ERROR_UPPER_CASE, TEXT_SYSTEM_ERROR_UPPER_CASE));
        rspCodeErrMap.put("05", new MsgDefinition(TEXT_SYSTEM_ERROR_MIXED_CASE, TEXT_DECLINED_SYSTEM_ERROR_UPPER_CASE, TEXT_SYSTEM_ERROR_UPPER_CASE));
        rspCodeErrMap.put("06", new MsgDefinition(TEXT_SYSTEM_ERROR_MIXED_CASE, TEXT_DECLINED_SYSTEM_ERROR_UPPER_CASE, TEXT_SYSTEM_ERROR_UPPER_CASE));
        rspCodeErrMap.put("07", new MsgDefinition(TEXT_SYSTEM_ERROR_MIXED_CASE, TEXT_DECLINED_SYSTEM_ERROR_UPPER_CASE, TEXT_SYSTEM_ERROR_UPPER_CASE));
        rspCodeErrMap.put("09", new MsgDefinition(TEXT_SYSTEM_ERROR_MIXED_CASE, TEXT_DECLINED_SYSTEM_ERROR_UPPER_CASE, TEXT_SYSTEM_ERROR_UPPER_CASE));
        rspCodeErrMap.put("10", new MsgDefinition(TEXT_SYSTEM_ERROR_MIXED_CASE, TEXT_DECLINED_SYSTEM_ERROR_UPPER_CASE, TEXT_SYSTEM_ERROR_UPPER_CASE));
        rspCodeErrMap.put("15", new MsgDefinition(TEXT_SYSTEM_ERROR_MIXED_CASE, TEXT_DECLINED_SYSTEM_ERROR_UPPER_CASE, TEXT_SYSTEM_ERROR_UPPER_CASE));
        rspCodeErrMap.put("16", new MsgDefinition(TEXT_SYSTEM_ERROR_MIXED_CASE, TEXT_DECLINED_SYSTEM_ERROR_UPPER_CASE, TEXT_SYSTEM_ERROR_UPPER_CASE));
        rspCodeErrMap.put("17", new MsgDefinition(TEXT_SYSTEM_ERROR_MIXED_CASE, TEXT_DECLINED_SYSTEM_ERROR_UPPER_CASE, TEXT_SYSTEM_ERROR_UPPER_CASE));
        rspCodeErrMap.put("18", new MsgDefinition(TEXT_SYSTEM_ERROR_MIXED_CASE, TEXT_DECLINED_SYSTEM_ERROR_UPPER_CASE, TEXT_SYSTEM_ERROR_UPPER_CASE));
        rspCodeErrMap.put("19", new MsgDefinition(TEXT_SYSTEM_ERROR_MIXED_CASE, TEXT_DECLINED_SYSTEM_ERROR_UPPER_CASE, TEXT_SYSTEM_ERROR_UPPER_CASE));
        rspCodeErrMap.put("20", new MsgDefinition(TEXT_SYSTEM_ERROR_MIXED_CASE, TEXT_DECLINED_SYSTEM_ERROR_UPPER_CASE, TEXT_SYSTEM_ERROR_UPPER_CASE));
        rspCodeErrMap.put("21", new MsgDefinition(TEXT_SYSTEM_ERROR_MIXED_CASE, TEXT_DECLINED_SYSTEM_ERROR_UPPER_CASE, TEXT_SYSTEM_ERROR_UPPER_CASE));
        rspCodeErrMap.put("22", new MsgDefinition(TEXT_SYSTEM_ERROR_MIXED_CASE, TEXT_DECLINED_SYSTEM_ERROR_UPPER_CASE, TEXT_SYSTEM_ERROR_UPPER_CASE));
        rspCodeErrMap.put("23", new MsgDefinition(TEXT_SYSTEM_ERROR_MIXED_CASE, TEXT_DECLINED_SYSTEM_ERROR_UPPER_CASE, TEXT_SYSTEM_ERROR_UPPER_CASE));
        rspCodeErrMap.put("24", new MsgDefinition(TEXT_SYSTEM_ERROR_MIXED_CASE, TEXT_DECLINED_SYSTEM_ERROR_UPPER_CASE, TEXT_SYSTEM_ERROR_UPPER_CASE));
        rspCodeErrMap.put("25", new MsgDefinition(TEXT_SYSTEM_ERROR_MIXED_CASE, TEXT_DECLINED_SYSTEM_ERROR_UPPER_CASE, TEXT_SYSTEM_ERROR_UPPER_CASE));
        rspCodeErrMap.put("26", new MsgDefinition(TEXT_SYSTEM_ERROR_MIXED_CASE, TEXT_DECLINED_SYSTEM_ERROR_UPPER_CASE, TEXT_SYSTEM_ERROR_UPPER_CASE));
        rspCodeErrMap.put("27", new MsgDefinition(TEXT_SYSTEM_ERROR_MIXED_CASE, TEXT_DECLINED_SYSTEM_ERROR_UPPER_CASE, TEXT_SYSTEM_ERROR_UPPER_CASE));
        rspCodeErrMap.put("28", new MsgDefinition(TEXT_SYSTEM_ERROR_MIXED_CASE, TEXT_DECLINED_SYSTEM_ERROR_UPPER_CASE, TEXT_SYSTEM_ERROR_UPPER_CASE));
        rspCodeErrMap.put("29", new MsgDefinition(TEXT_SYSTEM_ERROR_MIXED_CASE, TEXT_DECLINED_SYSTEM_ERROR_UPPER_CASE, TEXT_SYSTEM_ERROR_UPPER_CASE));
        rspCodeErrMap.put("30", new MsgDefinition(TEXT_SYSTEM_ERROR_MIXED_CASE, TEXT_DECLINED_SYSTEM_ERROR_UPPER_CASE, TEXT_SYSTEM_ERROR_UPPER_CASE));
        rspCodeErrMap.put("31", new MsgDefinition(TEXT_SYSTEM_ERROR_MIXED_CASE, TEXT_DECLINED_SYSTEM_ERROR_UPPER_CASE, TEXT_SYSTEM_ERROR_UPPER_CASE));
        rspCodeErrMap.put("32", new MsgDefinition(TEXT_SYSTEM_ERROR_MIXED_CASE, TEXT_DECLINED_SYSTEM_ERROR_UPPER_CASE, TEXT_SYSTEM_ERROR_UPPER_CASE));
        rspCodeErrMap.put("33", new MsgDefinition(TEXT_SYSTEM_ERROR_MIXED_CASE, TEXT_DECLINED_SYSTEM_ERROR_UPPER_CASE, TEXT_SYSTEM_ERROR_UPPER_CASE));
        rspCodeErrMap.put("34", new MsgDefinition(TEXT_SYSTEM_ERROR_MIXED_CASE, TEXT_DECLINED_SYSTEM_ERROR_UPPER_CASE, TEXT_SYSTEM_ERROR_UPPER_CASE));
        rspCodeErrMap.put("35", new MsgDefinition(TEXT_SYSTEM_ERROR_MIXED_CASE, TEXT_DECLINED_SYSTEM_ERROR_UPPER_CASE, TEXT_SYSTEM_ERROR_UPPER_CASE));
        rspCodeErrMap.put("37", new MsgDefinition(TEXT_SYSTEM_ERROR_MIXED_CASE, TEXT_DECLINED_SYSTEM_ERROR_UPPER_CASE, TEXT_SYSTEM_ERROR_UPPER_CASE));
        rspCodeErrMap.put("38", new MsgDefinition(TEXT_SYSTEM_ERROR_MIXED_CASE, TEXT_DECLINED_SYSTEM_ERROR_UPPER_CASE, TEXT_SYSTEM_ERROR_UPPER_CASE));
        rspCodeErrMap.put("40", new MsgDefinition(TEXT_SYSTEM_ERROR_MIXED_CASE, TEXT_DECLINED_SYSTEM_ERROR_UPPER_CASE, TEXT_SYSTEM_ERROR_UPPER_CASE));
        rspCodeErrMap.put("41", new MsgDefinition(TEXT_SYSTEM_ERROR_MIXED_CASE, TEXT_DECLINED_SYSTEM_ERROR_UPPER_CASE, TEXT_SYSTEM_ERROR_UPPER_CASE));
        rspCodeErrMap.put("43", new MsgDefinition(TEXT_SYSTEM_ERROR_MIXED_CASE, TEXT_DECLINED_SYSTEM_ERROR_UPPER_CASE, TEXT_SYSTEM_ERROR_UPPER_CASE));
        rspCodeErrMap.put("44", new MsgDefinition(TEXT_SYSTEM_ERROR_MIXED_CASE, TEXT_DECLINED_SYSTEM_ERROR_UPPER_CASE, TEXT_SYSTEM_ERROR_UPPER_CASE));
        rspCodeErrMap.put("45", new MsgDefinition(TEXT_SYSTEM_ERROR_MIXED_CASE, TEXT_DECLINED_SYSTEM_ERROR_UPPER_CASE, TEXT_SYSTEM_ERROR_UPPER_CASE));
        rspCodeErrMap.put("46", new MsgDefinition(TEXT_SYSTEM_ERROR_MIXED_CASE, TEXT_DECLINED_SYSTEM_ERROR_UPPER_CASE, TEXT_SYSTEM_ERROR_UPPER_CASE));
        rspCodeErrMap.put("47", new MsgDefinition(TEXT_SYSTEM_ERROR_MIXED_CASE, TEXT_DECLINED_SYSTEM_ERROR_UPPER_CASE, TEXT_SYSTEM_ERROR_UPPER_CASE));
        rspCodeErrMap.put("48", new MsgDefinition(TEXT_SYSTEM_ERROR_MIXED_CASE, TEXT_DECLINED_SYSTEM_ERROR_UPPER_CASE, TEXT_SYSTEM_ERROR_UPPER_CASE));
        rspCodeErrMap.put("49", new MsgDefinition(TEXT_SYSTEM_ERROR_MIXED_CASE, TEXT_DECLINED_SYSTEM_ERROR_UPPER_CASE, TEXT_SYSTEM_ERROR_UPPER_CASE));
        rspCodeErrMap.put("50", new MsgDefinition(TEXT_SYSTEM_ERROR_MIXED_CASE, TEXT_DECLINED_SYSTEM_ERROR_UPPER_CASE, TEXT_SYSTEM_ERROR_UPPER_CASE));
        rspCodeErrMap.put("56", new MsgDefinition(TEXT_SYSTEM_ERROR_MIXED_CASE, TEXT_DECLINED_SYSTEM_ERROR_UPPER_CASE, TEXT_SYSTEM_ERROR_UPPER_CASE));
        rspCodeErrMap.put("57", new MsgDefinition(TEXT_SYSTEM_ERROR_MIXED_CASE, TEXT_DECLINED_SYSTEM_ERROR_UPPER_CASE, TEXT_SYSTEM_ERROR_UPPER_CASE));
        rspCodeErrMap.put("58", new MsgDefinition(TEXT_SYSTEM_ERROR_MIXED_CASE, TEXT_DECLINED_SYSTEM_ERROR_UPPER_CASE, TEXT_SYSTEM_ERROR_UPPER_CASE));
        rspCodeErrMap.put("59", new MsgDefinition(TEXT_SYSTEM_ERROR_MIXED_CASE, TEXT_DECLINED_SYSTEM_ERROR_UPPER_CASE, TEXT_SYSTEM_ERROR_UPPER_CASE));
        rspCodeErrMap.put("60", new MsgDefinition(TEXT_SYSTEM_ERROR_MIXED_CASE, TEXT_DECLINED_SYSTEM_ERROR_UPPER_CASE, TEXT_SYSTEM_ERROR_UPPER_CASE));
        rspCodeErrMap.put("62", new MsgDefinition(TEXT_SYSTEM_ERROR_MIXED_CASE, TEXT_DECLINED_SYSTEM_ERROR_UPPER_CASE, TEXT_SYSTEM_ERROR_UPPER_CASE));
        rspCodeErrMap.put("63", new MsgDefinition(TEXT_SYSTEM_ERROR_MIXED_CASE, TEXT_DECLINED_SYSTEM_ERROR_UPPER_CASE, TEXT_SYSTEM_ERROR_UPPER_CASE));
        rspCodeErrMap.put("64", new MsgDefinition(TEXT_SYSTEM_ERROR_MIXED_CASE, TEXT_DECLINED_SYSTEM_ERROR_UPPER_CASE, TEXT_SYSTEM_ERROR_UPPER_CASE));
        rspCodeErrMap.put("66", new MsgDefinition(TEXT_SYSTEM_ERROR_MIXED_CASE, TEXT_DECLINED_SYSTEM_ERROR_UPPER_CASE, TEXT_SYSTEM_ERROR_UPPER_CASE));
        rspCodeErrMap.put("67", new MsgDefinition(TEXT_SYSTEM_ERROR_MIXED_CASE, TEXT_DECLINED_SYSTEM_ERROR_UPPER_CASE, TEXT_SYSTEM_ERROR_UPPER_CASE));
        rspCodeErrMap.put("68", new MsgDefinition(TEXT_SYSTEM_ERROR_MIXED_CASE, TEXT_DECLINED_SYSTEM_ERROR_UPPER_CASE, TEXT_SYSTEM_ERROR_UPPER_CASE));
        rspCodeErrMap.put("69", new MsgDefinition(TEXT_SYSTEM_ERROR_MIXED_CASE, TEXT_DECLINED_SYSTEM_ERROR_UPPER_CASE, TEXT_SYSTEM_ERROR_UPPER_CASE));
        rspCodeErrMap.put("70", new MsgDefinition(TEXT_SYSTEM_ERROR_MIXED_CASE, TEXT_DECLINED_SYSTEM_ERROR_UPPER_CASE, TEXT_SYSTEM_ERROR_UPPER_CASE));
        rspCodeErrMap.put("71", new MsgDefinition(TEXT_SYSTEM_ERROR_MIXED_CASE, TEXT_DECLINED_SYSTEM_ERROR_UPPER_CASE, TEXT_SYSTEM_ERROR_UPPER_CASE));
        rspCodeErrMap.put("72", new MsgDefinition(TEXT_SYSTEM_ERROR_MIXED_CASE, TEXT_DECLINED_SYSTEM_ERROR_UPPER_CASE, TEXT_SYSTEM_ERROR_UPPER_CASE));
        rspCodeErrMap.put("73", new MsgDefinition(TEXT_SYSTEM_ERROR_MIXED_CASE, TEXT_DECLINED_SYSTEM_ERROR_UPPER_CASE, TEXT_SYSTEM_ERROR_UPPER_CASE));
        rspCodeErrMap.put("74", new MsgDefinition(TEXT_SYSTEM_ERROR_MIXED_CASE, TEXT_DECLINED_SYSTEM_ERROR_UPPER_CASE, TEXT_SYSTEM_ERROR_UPPER_CASE));
        rspCodeErrMap.put("77", new MsgDefinition(TEXT_SYSTEM_ERROR_MIXED_CASE, TEXT_DECLINED_SYSTEM_ERROR_UPPER_CASE, TEXT_SYSTEM_ERROR_UPPER_CASE));
        rspCodeErrMap.put("78", new MsgDefinition(TEXT_SYSTEM_ERROR_MIXED_CASE, TEXT_DECLINED_SYSTEM_ERROR_UPPER_CASE, TEXT_SYSTEM_ERROR_UPPER_CASE));
        rspCodeErrMap.put("79", new MsgDefinition(TEXT_SYSTEM_ERROR_MIXED_CASE, TEXT_DECLINED_SYSTEM_ERROR_UPPER_CASE, TEXT_SYSTEM_ERROR_UPPER_CASE));
        rspCodeErrMap.put("80", new MsgDefinition(TEXT_SYSTEM_ERROR_MIXED_CASE, TEXT_DECLINED_SYSTEM_ERROR_UPPER_CASE, TEXT_SYSTEM_ERROR_UPPER_CASE));
        rspCodeErrMap.put("81", new MsgDefinition(TEXT_SYSTEM_ERROR_MIXED_CASE, TEXT_DECLINED_SYSTEM_ERROR_UPPER_CASE, TEXT_SYSTEM_ERROR_UPPER_CASE));
        rspCodeErrMap.put("82", new MsgDefinition(TEXT_SYSTEM_ERROR_MIXED_CASE, TEXT_DECLINED_SYSTEM_ERROR_UPPER_CASE, TEXT_SYSTEM_ERROR_UPPER_CASE));
        rspCodeErrMap.put("83", new MsgDefinition(TEXT_SYSTEM_ERROR_MIXED_CASE, TEXT_DECLINED_SYSTEM_ERROR_UPPER_CASE, TEXT_SYSTEM_ERROR_UPPER_CASE));
        rspCodeErrMap.put("84", new MsgDefinition(TEXT_SYSTEM_ERROR_MIXED_CASE, TEXT_DECLINED_SYSTEM_ERROR_UPPER_CASE, TEXT_SYSTEM_ERROR_UPPER_CASE));
        rspCodeErrMap.put("85", new MsgDefinition(TEXT_SYSTEM_ERROR_MIXED_CASE, TEXT_DECLINED_SYSTEM_ERROR_UPPER_CASE, TEXT_SYSTEM_ERROR_UPPER_CASE));
        rspCodeErrMap.put("86", new MsgDefinition(TEXT_SYSTEM_ERROR_MIXED_CASE, TEXT_DECLINED_SYSTEM_ERROR_UPPER_CASE, TEXT_SYSTEM_ERROR_UPPER_CASE));
        rspCodeErrMap.put("87", new MsgDefinition(TEXT_SYSTEM_ERROR_MIXED_CASE, TEXT_DECLINED_SYSTEM_ERROR_UPPER_CASE, TEXT_SYSTEM_ERROR_UPPER_CASE));
        rspCodeErrMap.put("88", new MsgDefinition(TEXT_SYSTEM_ERROR_MIXED_CASE, TEXT_DECLINED_SYSTEM_ERROR_UPPER_CASE, TEXT_SYSTEM_ERROR_UPPER_CASE));
        rspCodeErrMap.put("89", new MsgDefinition(TEXT_SYSTEM_ERROR_MIXED_CASE, TEXT_DECLINED_SYSTEM_ERROR_UPPER_CASE, TEXT_SYSTEM_ERROR_UPPER_CASE));
        rspCodeErrMap.put("90", new MsgDefinition(TEXT_SYSTEM_ERROR_MIXED_CASE, TEXT_DECLINED_SYSTEM_ERROR_UPPER_CASE, TEXT_SYSTEM_ERROR_UPPER_CASE));
        rspCodeErrMap.put("92", new MsgDefinition(TEXT_SYSTEM_ERROR_MIXED_CASE, TEXT_DECLINED_SYSTEM_ERROR_UPPER_CASE, TEXT_SYSTEM_ERROR_UPPER_CASE));
        rspCodeErrMap.put("94", new MsgDefinition(TEXT_SYSTEM_ERROR_MIXED_CASE, TEXT_DECLINED_SYSTEM_ERROR_UPPER_CASE, TEXT_SYSTEM_ERROR_UPPER_CASE));
        rspCodeErrMap.put("95", new MsgDefinition(TEXT_SYSTEM_ERROR_MIXED_CASE, TEXT_DECLINED_SYSTEM_ERROR_UPPER_CASE, TEXT_SYSTEM_ERROR_UPPER_CASE));
        rspCodeErrMap.put("98", new MsgDefinition(TEXT_SYSTEM_ERROR_MIXED_CASE, TEXT_DECLINED_SYSTEM_ERROR_UPPER_CASE, TEXT_SYSTEM_ERROR_UPPER_CASE));
        rspCodeErrMap.put("99", new MsgDefinition(TEXT_SYSTEM_ERROR_MIXED_CASE, TEXT_DECLINED_SYSTEM_ERROR_UPPER_CASE, TEXT_SYSTEM_ERROR_UPPER_CASE));
        rspCodeErrMap.put("XX", new MsgDefinition(TEXT_SYSTEM_ERROR_MIXED_CASE, TEXT_DECLINED_SYSTEM_ERROR_UPPER_CASE, TEXT_SYSTEM_ERROR_UPPER_CASE));
        return rspCodeErrMap;
    }

}
