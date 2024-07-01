package com.linkly.libengine.engine.protocol.iso8583;

import static com.linkly.libengine.engine.EngineManager.TransType.COMPLETION;
import static com.linkly.libengine.engine.protocol.IProto.RejectReasonType.MAC_FAILED;
import static com.linkly.libengine.engine.protocol.IProto.RejectReasonType.SIGNATURE_REJECTED;
import static com.linkly.libengine.engine.protocol.iso8583.As2805TillPack.MsgType.ADVICE;
import static com.linkly.libengine.engine.protocol.iso8583.As2805TillPack.MsgType.AUTH;
import static com.linkly.libengine.engine.protocol.iso8583.As2805TillPack.MsgType.NETWORK;
import static com.linkly.libengine.engine.protocol.iso8583.As2805TillPack.MsgType.PREAUTH;
import static com.linkly.libengine.engine.protocol.iso8583.As2805TillPack.MsgType.RECONCILIATION;
import static com.linkly.libengine.engine.protocol.iso8583.As2805TillPack.MsgType.REVERSAL;
import static com.linkly.libengine.engine.protocol.iso8583.As2805TillPack.MsgType.UPDATE;
import static com.linkly.libengine.engine.protocol.iso8583.As2805TillUtils.repackDE55Tags;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.As2805Till.Bit.DE_047_ADDITIONAL_DATA_NATIONAL;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.As2805Till.Bit.DE_055_ICC_DATA;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.As2805Till.Bit.DE_057_CASH_AMOUNT;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.As2805Till.Bit.DE_061_ADDITIONAL_PRIVATE;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.As2805Till.Bit.DE_062_ADDITIONAL_PRIVATE;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.As2805Till.Bit.DE_064_MAC;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.As2805Till.Bit.DE_070_NMIC;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.As2805Till.Bit.DE_071_MESSAGE_NUMBER;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.As2805Till.Bit.DE_072_MESSAGE_NUMBER_LAST;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.As2805Till.Bit.DE_118_CASHOUTS_NUMBER;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.As2805Till.Bit.DE_119_CASHOUTS_AMOUNT;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.As2805Till.Bit.DE_128_MAC;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583.Bit._002_PAN;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583.Bit._007_TRAN_DATE_TIME;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583.Bit._011_SYS_TRACE_AUDIT_NUM;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583.Bit._012_LOCAL_TRAN_TIME;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583.Bit._013_LOCAL_TRAN_DATE;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583.Bit._014_EXPIRATION_DATE;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583.Bit._015_SETTLEMENT_DATE;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583.Bit._022_POS_ENTRY_MODE;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583.Bit._023_CARD_SEQUENCE_NUM;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583.Bit._025_POS_CONDITION_CODE;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583.Bit._032_ACQUIRING_INST_ID_CODE;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583.Bit._035_TRACK_2_DATA;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583.Bit._037_RETRIEVAL_REF_NUM;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583.Bit._038_AUTH_ID_RESPONSE;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583.Bit._039_RESPONSE_CODE;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583.Bit._041_CARD_ACCEPTOR_TERMINAL_ID;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583.Bit._042_CARD_ACCEPTOR_ID_CODE;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583.Bit._043_CARD_ACCEPTOR_NAME_LOCATION;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583.Bit._048_ADDITIONAL_DATA;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583.Bit._049_TRAN_CURRENCY_CODE;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583.Bit._050_SETTLEMENT_CURRENCY_CODE;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583.Bit._052_PIN_DATA;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583.Bit._053_SECURITY_RELATED_CONTROL_INFORMATION;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583.Bit._074_CREDITS_NUMBER;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583.Bit._075_CREDITS_REVERSAL_NUMBER;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583.Bit._076_DEBITS_NUMBER;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583.Bit._077_DEBITS_REVERSAL_NUMBER;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583.Bit._086_CREDITS_AMOUNT;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583.Bit._087_CREDITS_REVERSAL_AMOUNT;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583.Bit._088_DEBITS_AMOUNT;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583.Bit._089_DEBITS_REVERSAL_AMOUNT;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583.Bit._090_ORIGINAL_DATA_ELEMENTS;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583.Bit._091_FILE_UPDATE_CODE;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583.Bit._097_AMOUNT_NET_SETTLEMENT;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583.Bit._101_FILE_NAME;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583.MsgType._0100_AUTH_REQ;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583.MsgType._0110_AUTH_REQ_RSP;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583.MsgType._0200_TRAN_REQ;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583.MsgType._0210_TRAN_REQ_RSP;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583.MsgType._0220_TRAN_ADV;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583.MsgType._0230_TRAN_ADV_RSP;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583.MsgType._0300_ACQUIRER_FILE_UPDATE_REQ;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583.MsgType._0310_ACQUIRER_FILE_UPDATE_RSP;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583.MsgType._0420_ACQUIRER_REV_ADV;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583.MsgType._0430_ACQUIRER_REV_ADV_RSP;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583.MsgType._0500_ACQUIRER_RECONCILE_REQ;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583.MsgType._0510_ACQUIRER_RECONCILE_REQ_RSP;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583.MsgType._0610_ADMIN_REQ_RSP;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583.MsgType._0800_NWRK_MNG_REQ;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583.MsgType._0810_NWRK_MNG_REQ_RSP;
import static com.linkly.libengine.engine.reporting.ReconciliationManager.reconciliationDao;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.AuthMethod.EFB_AUTHORISED;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.AuthMethod.LINK_DOWN_EFB_AUTHORISED;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.AuthMethod.OFFLINE_EFB_AUTHORISED;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.MessageStatus.FINALISED;
import static com.linkly.libmal.global.util.Util.hex2Str;
import static com.linkly.libsecapp.IP2PEncrypt.ElementType.CVV;
import static com.linkly.libsecapp.IP2PEncrypt.ElementType.EXPIRY_YYMM;
import static com.linkly.libsecapp.IP2PEncrypt.ElementType.EXPIRY_YYMMDD_CHIP;
import static com.linkly.libsecapp.IP2PEncrypt.ElementType.PAN;
import static com.linkly.libsecapp.IP2PEncrypt.ElementType.PIN_BLOCK;
import static com.linkly.libsecapp.IP2PEncrypt.ElementType.TRACK_2_FULL_MSR;
import static com.linkly.libsecapp.IP2PEncrypt.ElementType.TRACK_2_SHORT_FORMAT;
import static com.linkly.libsecapp.IP2PEncrypt.EncryptForStorageType.CVV_FORMAT;
import static com.linkly.libsecapp.IP2PEncrypt.EncryptForStorageType.EXPIRY_DATE_CHIP_FORMAT;
import static com.linkly.libsecapp.IP2PEncrypt.EncryptForStorageType.FULL_TRACK_FORMAT;
import static com.linkly.libsecapp.IP2PEncrypt.PackFormat.ASCII;
import static com.linkly.libsecapp.IP2PEncrypt.PackFormat.BCD;

import com.linkly.libengine.config.paycfg.PaymentSwitch;
import com.linkly.libengine.dependencies.IDependency;
import com.linkly.libengine.engine.Engine;
import com.linkly.libengine.engine.protocol.iso8583.openisoj.As2805Till;
import com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583;
import com.linkly.libengine.engine.protocol.iso8583.openisoj.exceptions.PackRuntimeException;
import com.linkly.libengine.engine.protocol.iso8583.openisoj.exceptions.UnknownFieldException;
import com.linkly.libengine.engine.reporting.Amounts;
import com.linkly.libengine.engine.reporting.Reconciliation;
import com.linkly.libengine.engine.reporting.SchemeTotals;
import com.linkly.libengine.engine.transactions.TransRec;
import com.linkly.libengine.engine.transactions.properties.TAmounts;
import com.linkly.libengine.engine.transactions.properties.TAudit;
import com.linkly.libengine.engine.transactions.properties.TCard;
import com.linkly.libengine.engine.transactions.properties.TProtocol;
import com.linkly.libengine.engine.transactions.properties.TReconciliationFigures;
import com.linkly.libengine.env.Aiic;
import com.linkly.libengine.overrides.CoreOverrides;
import com.linkly.libmal.MalFactory;
import com.linkly.libmal.global.util.Util;
import com.linkly.libsecapp.CardholderDataElement;
import com.linkly.libsecapp.EncryptResult;
import com.linkly.libsecapp.IP2PEncrypt;
import com.linkly.libsecapp.IP2PSec;
import com.linkly.libsecapp.P2PLib;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import timber.log.Timber;

public class As2805TillPack {
    public static final char SUBST_VAL_CVV = 'A';
    private static final char SUBST_VAL_TRACK_2_MSR = 'B';
    private static final char SUBST_VAL_TRACK_2_PAN = 'C';
    private static final char SUBST_VAL_TRACK_2_SHORT = 'D';
    private static final char SUBST_VAL_EXPIRY = 'E';
    private static final char SUBST_VAL_DE47 = 'F';
    public static final String ZERO_FILLED_AMOUNT = "000000000000";
    public static final String ERROR_INVALID_FIELD_48 = "Error invalid F48";
    public static final String ZERO_FILLED_NUMBER_FORMATTER = "%010d";
    public static final String ZERO_FILLED_AMOUNT_FORMATTER = "%016d";

    private static final P2PLib p2pInstance = P2PLib.getInstance();
    private static final IP2PEncrypt p2pEncrypt = p2pInstance.getIP2PEncrypt();
    private static final IP2PSec secMal = p2pInstance.getIP2PSec();
    private static byte[] randomNumber;
    private static final String TABLE_NAME = "EPAT"; // only download this table until get further info about the others
    private static char kekUpdateFlag = '1';

    public static byte[] pack(IDependency d, TransRec trans, MsgType msgType, String nmic) {

        byte[] result = null;
        try {
            switch (msgType) {
                case PREAUTH:
                case AUTH:
                    // use same pack code for preauth (0100) and regular auth (0200)
                    result = packAuth(d, trans, msgType);
                    break;
                case ADVICE:
                    result = packAdvice(d, trans);
                    break;
                case NETWORK:
                    result = packNetwork(d, trans, nmic);
                    break;
                case REVERSAL:
                    result = packReversal(d, trans);
                    break;
                case RECONCILIATION:
                    result = packReconciliation(d, trans);
                    break;
                default:
                    Timber.e("Error - unexpected/unhandled msgType %s", msgType.name());
                    break;
            }

            return result;

        } catch (Exception e) {
            trans.getProtocol().setMessageStatus(FINALISED);
            trans.save();
            Timber.e(e, "Pack failed, remove Transaction ref [%s] from batch as useless", trans.getAudit().getReference());
            throw e;
        }
    }

    public static byte[] packUpdate(IDependency d, TransRec trans, MsgType msgType, As2805TillProto.FileUpdate fileUpdate) {

        byte[] result = null;
        try {
            if (msgType == UPDATE) {
                result = packFileUpdate(d, trans, fileUpdate);
            }
            return result;

        } catch (Exception e) {
            trans.getProtocol().setMessageStatus(FINALISED);
            trans.save();
            Timber.i(e, "Pack failed, remove from batch as useless");
            throw e;
        }
    }

    public static boolean isSecurityDisabled(IDependency d) {
        PaymentSwitch paySwitchCfg = d.getPayCfg().getPaymentSwitch();
        return null != paySwitchCfg && paySwitchCfg.isDisableSecurity();
    }


    /**
     * Perform basic sanity checks on the response data
     *
     * @param sentMessageType {@link MsgType} original Message type
     * @param trans           {@link TransRec} object
     * @param responseData    {@link As2805Till} response object
     * @return true if all sanity checks pass
     */
    private static boolean isResponseDataIncorrect(As2805Till responseData, TransRec trans, MsgType sentMessageType) {
        if (responseData.getMsgType() != sentMessageType.receiveMsgId) {
            Timber.e("Received the incorrect Message type. Expected [%x], got [%x]", sentMessageType.receiveMsgId, responseData.getMsgType());
            return true;
        }

        if ((!responseData.verifyString(trans.getAudit().getTerminalId(), _041_CARD_ACCEPTOR_TERMINAL_ID))) {
            Timber.e("Terminal IDs don't match. Expected [%s], got [%s]", trans.getAudit().getTerminalId(), responseData.get(_041_CARD_ACCEPTOR_TERMINAL_ID));
            return true;
        }

        if ((!responseData.verifyString(trans.getAudit().getMerchantId(), _042_CARD_ACCEPTOR_ID_CODE))) {
            Timber.e("Merchant IDs don't match. Expected [%s], got [%s]", trans.getAudit().getMerchantId(), responseData.get(_042_CARD_ACCEPTOR_ID_CODE));
            return true;
        }

        if ((trans.getProtocol().getStan() != Integer.parseInt(responseData.get(_011_SYS_TRACE_AUDIT_NUM)))) {
            Timber.e("STAN doesn't match. Expected [%s], got [%s]", trans.getProtocol().getStan().toString(), responseData.get(_011_SYS_TRACE_AUDIT_NUM));
            return true;
        }

        return false;
    }


    /**
     * must unpack the data completely before committing it to the transaction, so we don't get half the details
     *
     * @param d               {@link IDependency} object
     * @param responseData    Byte array to be filled
     * @param trans           {@link TransRec} object
     * @param sentMessageType {@link MsgType} Message type sent whose response we should be expecting
     * @return result {@link UnPackResult}:
     * <ul>
     *     <li>{@link UnPackResult#UNPACK_OK} Successful unpacking</li>
     *     <li>{@link UnPackResult#MAC_ERROR} Mac Field in the responseData is invalid</li>
     *     <li>{@link UnPackResult#VERIFICATION_FAILED} Fields in the response don't match the fields sent. Eg: TID, MID, Msg Type</li>
     *     <li>{@link UnPackResult#GENERIC_FAILURE} Catch all failure.</li>
     * </ul>
     */
    @SuppressWarnings("java:S3776") // cognitive complexity(33)
    public static UnPackResult unpack(IDependency d, byte[] responseData, TransRec trans, MsgType sentMessageType) {
        As2805Till resp;
        try {
            if (responseData != null) {
                resp = new As2805Till(responseData);

                Timber.e("Rx msg type: %s", resp.toString());

                // Sanity checks
                if (As2805TillPack.isResponseDataIncorrect(resp, trans, sentMessageType)) {
                    return UnPackResult.VERIFICATION_FAILED;
                }

                String nmic = resp.get(DE_070_NMIC);

                // validate MAC for these message types
                boolean validateMac;
                switch (resp.getMsgType()) {
                    case 0x110:
                    case 0x210:
                    case 0x230:
                    case 0x510:
                    case 0x430:
                        validateMac = true;
                        break;
                    case 0x310:
                        // if NMIC is 151, it is macced
                        validateMac = "151".equals(nmic);
                        break;
                    default:
                        validateMac = false;
                        break;
                }

                if (validateMac && !isSecurityDisabled(d) && !secMal.as2805VerifyMac(responseData)) {
                    // mac verify failed. downgrade our logon state to trigger required action
                    Timber.e("MAC VERIFICATION ERROR DETECTED");
                    d.getProtocol().setInternalRejectReason(trans, MAC_FAILED);

                    // return the MAC failed. This will trigger an immediate reversal after a logon is done
                    return UnPackResult.MAC_ERROR;
                }

                updateResponseCodeMap(resp, trans);

                // Unpack date and time values for settlement and bank date times
                String settlementDate = resp.get(_015_SETTLEMENT_DATE); // MMdd format
                if (settlementDate != null) {
                    trans.getProtocol().setSettlementDate(settlementDate);
                }

                String bankTime = resp.get(_012_LOCAL_TRAN_TIME);
                trans.getProtocol().setBankTime(bankTime);

                String bankDate = resp.get(_013_LOCAL_TRAN_DATE);
                trans.getProtocol().setBankDate(bankDate);


                String authCode = resp.get(_038_AUTH_ID_RESPONSE);
                if (authCode != null && authCode.length() > 0) {
                    trans.getProtocol().setAuthCode(authCode);
                }

                if (CoreOverrides.get().isSpoofComms()) {
                    trans.getProtocol().setPosResponseCode("00");
                    trans.getProtocol().setServerResponseCode("00");
                    trans.getProtocol().setAuthCode("123456");
                }

                String nameLocation = resp.get(_043_CARD_ACCEPTOR_NAME_LOCATION);
                if (nameLocation != null && nameLocation.length() > 0) {
                    d.getPayCfg().getReceipt().getMerchant().setLine0(nameLocation);
                }

                String field55 = resp.get(DE_055_ICC_DATA);
                if (field55 != null) {
                    As2805TillUtils.unpackIccData(trans, field55);
                }

                // setup the KEK key update flag for the NMIC 101 and 170
                if ((("101".equals(nmic)) || ("170".equals(nmic)))) {
                    String f53Response = resp.get(_053_SECURITY_RELATED_CONTROL_INFORMATION);

                    if (f53Response != null && f53Response.contains("1")) {
                        kekUpdateFlag = '1';
                        Timber.i("KEK1 updated");
                    } else if (f53Response != null && f53Response.contains("2")) {
                        kekUpdateFlag = '2';
                        Timber.i("KEK2 updated");
                    } else {
                        kekUpdateFlag = '0';
                        Timber.i("KEK not updated");
                    }
                }

                String f48Response = resp.get(_048_ADDITIONAL_DATA);
                if (!Util.isNullOrEmpty(f48Response)) {
                    unpackField48(trans, f48Response, resp.getMsgType(), nmic);
                }

                Reconciliation reconciliation = new Reconciliation(false);
                String creditsNumber = resp.get(_074_CREDITS_NUMBER);
                String creditsReversalNumber = resp.get(_075_CREDITS_REVERSAL_NUMBER);
                String creditsAmount = resp.get(_086_CREDITS_AMOUNT);
                String creditsReversalAmount = resp.get(_087_CREDITS_REVERSAL_AMOUNT);

                if ((creditsNumber != null) && (creditsAmount != null)) {
                    Timber.e("creditsNumber = %s", creditsNumber);
                    Timber.e("creditsReversalNumber = %s", creditsReversalNumber);
                    Timber.e("creditsAmount = %s", creditsAmount);
                    Timber.e("creditsReversalAmount = %s", creditsReversalAmount);
                    reconciliation.setRefund(new Amounts(Long.parseLong(creditsAmount), Long.parseLong(creditsNumber), Long.parseLong(creditsReversalAmount), Long.parseLong(creditsReversalNumber)));
                }

                String debitsNumber = resp.get(_076_DEBITS_NUMBER);
                String debitsReversalNumber = resp.get(_077_DEBITS_REVERSAL_NUMBER);
                String debitsAmount = resp.get(_088_DEBITS_AMOUNT);
                String debitsReversalAmount = resp.get(_089_DEBITS_REVERSAL_AMOUNT);
                if ((debitsNumber != null) && (debitsAmount != null)) {
                    Timber.e("debitsNumber = %s", debitsNumber);
                    Timber.e("debitsReversalNumber = %s", debitsReversalNumber);
                    Timber.e("debitsAmount = %s", debitsAmount);
                    Timber.e("debitsReversalAmount = %s", debitsReversalAmount);
                    reconciliation.setSale(new Amounts(Long.parseLong(debitsAmount), Long.parseLong(debitsNumber), Long.parseLong(debitsReversalAmount), Long.parseLong(debitsReversalNumber)));
                }

                String amountNetSettlement = resp.get(_097_AMOUNT_NET_SETTLEMENT);
                if (amountNetSettlement != null) {
                    Timber.e("amountNetSettlement = %s", amountNetSettlement);
                    // Used on Acquirer Reconciliation Messages, this field contains the net value of all gross amounts pertaining to the settlement period. The first position,
                    // credit("C") or debit("D") indicators is in ascii form
                    reconciliation.setTotalAmount(Long.parseLong(amountNetSettlement.substring(2)) * (amountNetSettlement.startsWith("43") ? -1 : 1));
                }

                String cashNumber = resp.get(DE_118_CASHOUTS_NUMBER);
                String cashAmount = resp.get(DE_119_CASHOUTS_AMOUNT);
                if ((cashNumber != null) && (cashAmount != null)) {
                    Timber.e("cashNumber = %s", cashNumber);
                    Timber.e("cashAmount = %s", cashAmount);
                    reconciliation.setCash(new Amounts(Long.parseLong(cashAmount), Long.parseLong(cashNumber), 0L, 0L));
                }
                trans.setReconciliation(reconciliation);
            }
            return UnPackResult.UNPACK_OK;
        } catch (Exception e) {
            Timber.e(e);
            return UnPackResult.GENERIC_FAILURE;
        }
    }


    private static void updateResponseCodeMap(As2805Till resp, TransRec trans) {

        boolean overrideResponseCode;
        switch (resp.getMsgType()) {
            case 0x430:
                overrideResponseCode = false;
                break;
            case 0x210:
            case 0x230:
            case 0x510:
            case 0x310:
            default:
                overrideResponseCode = true;
                break;
        }

        // We shouldn't be updating the main response code when a txn record is reversed, as get last receipt for any reversed transaction shows Approved receipt  TASK: IAAS-1870
        if (overrideResponseCode) {
            String responseCode = resp.get(_039_RESPONSE_CODE);
            // set display and receipt text based off response code
            trans.setProtocol(new As2805TillRspCodeMap().populateProtocolRecord(trans.getProtocol(), responseCode));
        }
    }

    /* must unpack the data completely before committing it to the transaction, so we dont get half the details */
    /* check the mac etc */
    public static boolean unpack(byte[] responseData, TransRec trans, As2805TillProto.FileUpdate fileUpdate, MsgType sentMessageType) {

        As2805Till resp;
        try {
            if (responseData != null) {
                resp = new As2805Till(responseData);

                if (As2805TillPack.isResponseDataIncorrect(resp, trans, sentMessageType)) {
                    return false;
                }

                Timber.e(resp.toString());

                String responseCode = resp.get(_039_RESPONSE_CODE);
                if ("08".equals(responseCode)) {
                    trans.getProtocol().setSignatureRequired(true);
                }
                updateResponseCodeMap(resp, trans);

                String bankTime = resp.get(_012_LOCAL_TRAN_TIME);
                trans.getProtocol().setBankTime(bankTime);

                String bankDate = resp.get(_013_LOCAL_TRAN_DATE);
                trans.getProtocol().setBankDate(bankDate);

                if (CoreOverrides.get().isSpoofComms()) {
                    trans.getProtocol().setPosResponseCode("00");
                    trans.getProtocol().setServerResponseCode("00");
                    trans.getProtocol().setAuthCode("123456");
                }

                String f48Response = resp.get(_048_ADDITIONAL_DATA);
                if (!Util.isNullOrEmpty(f48Response)) {
                    fileUpdate.data = f48Response;

                    //TODO:

                }
                String msgLast = resp.get(DE_072_MESSAGE_NUMBER_LAST);
                fileUpdate.msgNumberLast = !Util.isNullOrEmpty(msgLast);
                return true;
            }
        } catch (Exception e) {
            Timber.e(e);
        }
        return false;
    }


    private static void unpackField48For810(TransRec trans, String f48Response, String nmic) {

        // Field 48 varies depending on the NMIC
        switch (nmic) {
            case "191":
                unpackField48For810WithNmic191(f48Response);
                break;

            case "192":
                unpackField48For810WithNmic192(f48Response);
                break;

            case "193":
                unpackField48For810WithNmic193(f48Response);
                break;

            case "101":
                unpackField48For810WithNmic101(f48Response);
                break;

            case "170":
                unpackField48For810WithNmic170(trans, f48Response);
                break;

            default:
                Timber.e("Unhandled F48 response NMIC: %s", nmic);
                break;
        }

    }

    private static void unpackField48For610(TransRec trans, String f48Response, String nmic) {

        // todo unpack the data
        Timber.i("Record Present %b, Field 48 = %s, nmic = %s", trans != null, f48Response, nmic);

    }

    // Unpacks field 48 and applies it to the trans rec (If need be) based on the type of message
    // Annoyingly this varies depending on what message is being sent
    private static void unpackField48(TransRec trans, String f48Response, int type, String nmic) {

        // Field 48 varies depending on the message
        switch (type) {
            case _0810_NWRK_MNG_REQ_RSP:
                unpackField48For810(trans, f48Response, nmic);
                break;


            case _0510_ACQUIRER_RECONCILE_REQ_RSP:
                unpackField48For510(trans, f48Response);
                break;

            case _0610_ADMIN_REQ_RSP:
                unpackField48For610(trans, f48Response, nmic);
                break;

            default:
                Timber.e("Unhandled F48 response message: %s", type);
                break;
        }
    }

    private static void unpackField48For510(TransRec trans, String f48Response) {

        // Following implementation is as per Till spec, section 11.2.24
        // Note: This implementation is expected to change, as the FIS host implementation might change to adopt net amount. Also, in the current implementation the first byte is
        // is hardcoded as space 0x20. This hardcoded space field will be updated with credit/debit indicator("C" or "D"). This credit/debit sign is currently calculated
        List<SchemeTotals> schemeTotals = new ArrayList<>();
        for (int i = 0; i < f48Response.length(); i += 62) {
            if (f48Response.startsWith("C2", i) && f48Response.startsWith("29", i + 2)) {
                SchemeTotals schemeTotal = new SchemeTotals();
                schemeTotal.setCardNameIndex(Util.hexToAscii(f48Response.substring(i + 6, i + 10)));
                schemeTotal.setCreditNumber(Long.parseLong(f48Response.substring(i + 10, i + 20)));
                schemeTotal.setCreditAmount(Long.parseLong(f48Response.substring(i + 20, i + 36)));
                schemeTotal.setDebitNumber(Long.parseLong(f48Response.substring(i + 36, i + 46)));
                schemeTotal.setDebitAmount(Long.parseLong(f48Response.substring(i + 46, i + 62)));
                schemeTotals.add(schemeTotal);
            }
        }

        trans.setSchemeTotals(schemeTotals);
    }

    private static void unpackField48For810WithNmic191(String f48Response) {

        /*
            DE48 are:
            Sponsor Public key = 480 bytes
            Random number = 8 bytes
        */
        byte[] field48 = Util.hexStringToByteArray(f48Response);
        final int F48LEN = 488;

        if (field48.length >= F48LEN) {
            byte[] pkSpMod = Arrays.copyOfRange(field48, 0, 240);
            byte[] pkSpExp = Arrays.copyOfRange(field48, 240, 480);
            // save in member variable, used in nmic 192 0800 request, which will happen next
            randomNumber = Arrays.copyOfRange(field48, 480, 488);

            secMal.as2805InjectPkSponsor(pkSpMod, pkSpExp);
        } else {
            // If we have reached here either something has very wrong either invalid data etc.
            Timber.w("Error invalid F48:%s", f48Response);
        }
    }

    private static void unpackField48For810WithNmic192(String f48Response) {

        /*
            DE48 are:
            Encrypted KCA = 16 bytes
            Encrypted KMACH = 16 bytes
            AIIC = 8 bytes
        */
        byte[] field48 = Util.hexStringToByteArray(f48Response);
        final int F48LEN = 38;

        if (field48.length >= F48LEN) {
            byte[] eKIv44Kca = Arrays.copyOfRange(field48, 0, 16);
            byte[] eKIv24Kmach = Arrays.copyOfRange(field48, 16, 32);

            // unpack AIIC, numeric string with 2 length bytes
            byte[] aiicLenBytes = new byte[2];
            System.arraycopy(field48, 32, aiicLenBytes, 0, 2);
            String aiicLenStr = new String(aiicLenBytes);
            int aiicLenInt = Integer.parseInt(aiicLenStr);

            if (aiicLenInt > 0 && aiicLenInt <= 16) {
                byte[] aiicBytes = new byte[aiicLenInt / 2];
                System.arraycopy(field48, 34, aiicBytes, 0, aiicLenInt / 2);
                // convert to string
                String aiicStr = Util.bcd2Str(aiicBytes);
                // save to env var
                Aiic.setNewValue(aiicStr);
                Timber.i("Saved AIIC value [%s]", aiicStr);
            } else {
                Timber.e("ERROR, invalid AIIC length %d", aiicLenInt);
                throw new IllegalArgumentException();
            }

            // load KCA and KMACH
            secMal.as2805LoadKcaKmach(eKIv44Kca, eKIv24Kmach, packAiicBlock());
        } else {
            // If we have reached here either something has very wrong either invalid data etc.
            Timber.e(ERROR_INVALID_FIELD_48);
        }
    }

    private static void unpackField48For810WithNmic193(String f48Response) {

        /*
            DE48 are:
            Encrypted KEK1 = 16 bytes
            Encrypted KEK2 = 16 bytes
            Encrypted PPASN = 8 bytes
            KEK1Kvc = 3 bytes
            KEK2Kvc = 3 bytes
            PPASNKvc = 3 bytes
        */
        byte[] field48 = Util.hexStringToByteArray(f48Response);
        final int F48LEN = 49;

        if (field48.length >= F48LEN) {
            byte[] eKiaKek1eKiaKek2eKiav88Ppasn = Arrays.copyOfRange(field48, 0, 40);
            byte[] kek1Kvc = Arrays.copyOfRange(field48, 40, 43);
            byte[] kek2Kvc = Arrays.copyOfRange(field48, 43, 46);
            byte[] ppasnKvc = Arrays.copyOfRange(field48, 46, 49);

            Timber.i("KEK1 KVC from host = %s", hex2Str(kek1Kvc));
            Timber.i("KEK2 KVC from host = %s", hex2Str(kek2Kvc));
            Timber.i("PPASN KVC from host = %s", hex2Str(ppasnKvc));

            // load keks
            if (!secMal.as2805LoadKek1Kek2Ppasn(eKiaKek1eKiaKek2eKiav88Ppasn, kek1Kvc, kek2Kvc, ppasnKvc, packAiicBlock())) {
                // load keks failed, abort current logon process and revert state to RSA required
                Timber.e("Loading of KEKS failed - either KVC mismatch occurred, or PED dev exception thrown");
            }
        } else {
            // If we have reached here either something has very wrong either invalid data etc.
            Timber.e(ERROR_INVALID_FIELD_48);
        }

    }

    private static void unpackField48For810WithNmic101(String f48Response) {
        /*
            DE48 data from spec:
            Encrypted KPP = 16 bytes
            Encrypted KMACs = 16 bytes
            Encrypted KMACr = 16 bytes
            Encrypted KDs = 16 bytes
            Encrypted KDr = 16 bytes
            KPPKvc = 3 bytes
            KMACsKvc = 3 bytes
            KMACrKvc = 3 bytes
            KDsKvc = 3 bytes
            KDrKvc = 3 bytes
        */
        byte[] field48 = Util.hexStringToByteArray(f48Response);
        final int F48LEN = 95;

        if (field48.length >= F48LEN) {
            byte[] eKekKppKmacsKmacrKdsKdr = new byte[16 * 5];
            byte[] kvcKppKmacsKmacrKdsKdr = new byte[3 * 5];

            System.arraycopy(field48, 0, eKekKppKmacsKmacrKdsKdr, 0, 16 * 5);
            System.arraycopy(field48, 80, kvcKppKmacsKmacrKdsKdr, 0, 3 * 5);

            secMal.as2805LoadSessionKeysTillStyle(kekUpdateFlag, eKekKppKmacsKmacrKdsKdr, kvcKppKmacsKmacrKdsKdr);
        } else {
            // If we have reached here either something has very wrong either invalid data etc.
            Timber.e(ERROR_INVALID_FIELD_48);
        }
    }

    private static void unpackField48For810WithNmic170(TransRec trans, String f48Response) {
        /*
            DE48 data from spec:
            Encrypted KPP = 16 bytes
            Encrypted KMACs = 16 bytes
            Encrypted KMACr = 16 bytes
            Encrypted KDs = 16 bytes
            Encrypted KDr = 16 bytes
            KPPKvc = 3 bytes
            KMACsKvc = 3 bytes
            KMACrKvc = 3 bytes
            KDsKvc = 3 bytes
            KDrKvc = 3 bytes
            STAN = 3 bytes
            Local year = 2 bytes
        */
        byte[] field48 = Util.hexStringToByteArray(f48Response);
        final int F48LEN = 100;

        if (field48.length >= F48LEN) {
            byte[] stan = Arrays.copyOfRange(field48, 95, 98);
            byte[] year = Arrays.copyOfRange(field48, 98, 100);

            // set next stan to use
            Timber.e("stan from host = %s", hex2Str(stan));
            Integer nextStan = Integer.parseInt(Util.bcd2Str(stan));
            Timber.e("setting next stan to use = %s", nextStan);

            // NOTE: this is the NEXT stan that we must use. Because Stan.getNewValue pre-increments it, save as -1 here to ensure next stan used = this one
            nextStan--;
            if (nextStan <= 0) {
                // ensure stan is between 1 and 999999
                nextStan = 999999;
            }

            trans.getProtocol().setResetStan(nextStan);

            // Our year value for applying the clock
            trans.getProtocol().setYear(Util.bcd2Str(year));
            Timber.i("year from host = %s", hex2Str(year));

            byte[] eKekKppKmacsKmacrKdsKdr = new byte[16 * 5];
            byte[] kvcKppKmacsKmacrKdsKdr = new byte[3 * 5];

            System.arraycopy(field48, 0, eKekKppKmacsKmacrKdsKdr, 0, 16 * 5);
            System.arraycopy(field48, 80, kvcKppKmacsKmacrKdsKdr, 0, 3 * 5);

            secMal.as2805LoadSessionKeysTillStyle(kekUpdateFlag, eKekKppKmacsKmacrKdsKdr, kvcKppKmacsKmacrKdsKdr);

            // if we get here, then RSA is complete!
            Timber.e("RSA and session key load successful!");

        } else {
            // If we have reached here either something has very wrong either invalid data etc.
            Timber.e(ERROR_INVALID_FIELD_48);
        }
    }


    // get expiry or other 'non sensitive' card data
    public static String getNonSensitiveElement(IP2PEncrypt.ElementType element) {
        int dataLen = p2pEncrypt.getElementLength(element);
        if (dataLen <= 0) {
            Timber.i("WARNING - addSensitiveElement element " + element.name() + " not found, skipping");
            return null;
        }

        return p2pEncrypt.getData(element);
    }

    // get element length 'n', and look for a run of n x byteToFind bytes in dataToSearch, starting at searchFromIdx
    // returns new CardholderDataElement if found
    @SuppressWarnings("java:S3776")// java:S3776: Cognitive complexity(22)
    private static CardholderDataElement findElementPlaceholder(byte[] dataToSearch, int searchFromIdx, IP2PEncrypt.ElementType elementType, char charToFind, IP2PEncrypt.PackFormat packFormat) {
        int elementLength = p2pEncrypt.getElementLength(elementType);

        byte[] packedMsg;
        if (packFormat == BCD) {
            // for Till, various card data fields are BCD packed. so convert input message for comparison from bcd to ascii
            packedMsg = Objects.requireNonNull(hex2Str(dataToSearch)).getBytes();
        } else {
            packedMsg = dataToSearch;
        }

        // data not found in p2pe module SAD
        if (elementLength <= 0)
            return null;

        // look for a run of at least 'elementLength' bytes, remember it's index
        for (int idx = searchFromIdx; idx < packedMsg.length - elementLength; idx++) {

            if (packedMsg[idx] == charToFind) {
                int runCount;

                // see if there's a run of elementLength bytes here
                for (runCount = 0; (runCount < elementLength) && ((idx + runCount) < packedMsg.length); runCount++) {
                    if (packedMsg[idx + runCount] != charToFind)
                        break;
                }

                // if we found a run of elementLength
                if (runCount == elementLength) {
                    // record idx in a new CardholderDataElement object
                    if (packFormat == BCD) {
                        // divide current index by 2 as the real message is BCD packed, and we're searching in an ASCII (double length) message
                        // element length in bcd format is rounded up if odd length, then divide by 2 to get packed bcd size
                        return new CardholderDataElement(elementType, false, idx / 2, (elementLength + 1) / 2, BCD);
                    } else {
                        return new CardholderDataElement(elementType, false, idx, elementLength, ASCII);
                    }

                }
            }
        }

        // else we didn't find
        return null;
    }

    private static CardholderDataElement[] getSecureElements(byte[] msg) {

        // const value really - skip MTI (2 bytes) and 8 bytes of bitmap as this can contain binary data and could get a false match
        final int REPLACE_FROM_OFFSET = 10;

        // scan message for fields
        CardholderDataElement cvvElement = findElementPlaceholder(msg, REPLACE_FROM_OFFSET, CVV, SUBST_VAL_CVV, ASCII);
        CardholderDataElement track2MsrElement = findElementPlaceholder(msg, REPLACE_FROM_OFFSET, TRACK_2_FULL_MSR, SUBST_VAL_TRACK_2_MSR, BCD);
        CardholderDataElement panElement = findElementPlaceholder(msg, REPLACE_FROM_OFFSET, PAN, SUBST_VAL_TRACK_2_PAN, BCD);
        CardholderDataElement track2ShortElement = findElementPlaceholder(msg, REPLACE_FROM_OFFSET, TRACK_2_SHORT_FORMAT, SUBST_VAL_TRACK_2_SHORT, BCD);
        CardholderDataElement expiryElement = findElementPlaceholder(msg, REPLACE_FROM_OFFSET, EXPIRY_YYMM, SUBST_VAL_EXPIRY, BCD);

        // add cardholder data element object in the substitution list for every element found
        int numElements = 0;
        if (cvvElement != null)
            numElements++;
        if (track2MsrElement != null)
            numElements++;
        if (panElement != null)
            numElements++;
        if (track2ShortElement != null)
            numElements++;
        if (expiryElement != null)
            numElements++;

        CardholderDataElement[] elements = new CardholderDataElement[numElements];

        int elementNo = 0;
        if (cvvElement != null)
            elements[elementNo++] = cvvElement;
        if (track2MsrElement != null)
            elements[elementNo++] = track2MsrElement;
        if (panElement != null)
            elements[elementNo++] = panElement;
        if (track2ShortElement != null)
            elements[elementNo++] = track2ShortElement;
        if (expiryElement != null)
            elements[elementNo] = expiryElement;

        return elements;
    }

    @SuppressWarnings("java:S3776")// java:S3776: Cognitive complexity(20)
    private static int patternOffset(byte[] data, int searchFromIdx, char charToFind, int lengthToFind) {
        if (data != null && data.length >= searchFromIdx + lengthToFind) {
            for (int idx = searchFromIdx; idx < data.length - lengthToFind; idx++) {
                if (data[idx] == charToFind) {
                    int j;
                    for (j = 0; j < lengthToFind; j++) {
                        if (data[idx + j] != charToFind)
                            break;
                    }

                    if (j == lengthToFind) {
                        return idx;
                    }
                }
            }
        }
        return -1;
    }

    @SuppressWarnings({"java:S3776","java:S6541"})// java:S3776: Cognitive complexity(55); java:S6541: "Brain Method"
    private static byte[] encryptSensitiveFields(As2805Till msg, boolean disableEncryption) throws Exception {
        // we want to concatenate sensitive elements together in a buffer. Count size of output buffer and get secApp to build it then encrypt fields together
        int encDataFieldLen = 0;
        int numElementsToEnc = 0;
        int de47Offset = 0;
        if (msg.isFieldSet(_002_PAN)) {
            numElementsToEnc++;
        }
        if (msg.isFieldSet(_014_EXPIRATION_DATE)) {
            numElementsToEnc++;
        }
        if (msg.isFieldSet(_035_TRACK_2_DATA)) {
            numElementsToEnc++;
        }
        if (msg.isFieldSet(DE_047_ADDITIONAL_DATA_NATIONAL)) {
            numElementsToEnc++;
        }
        if (numElementsToEnc > 0) {
            CardholderDataElement[] encElements = new CardholderDataElement[numElementsToEnc];
            int element = 0;
            int fieldLen = 0;
            if (msg.isFieldSet(_002_PAN)) {
                fieldLen = (p2pEncrypt.getElementLength(PAN) + 1) / 2;
                encElements[element++] = new CardholderDataElement(PAN, false, encDataFieldLen, fieldLen, BCD);
                encDataFieldLen += fieldLen;
            }
            if (msg.isFieldSet(_014_EXPIRATION_DATE)) {
                fieldLen = (p2pEncrypt.getElementLength(EXPIRY_YYMM) + 1) / 2;
                encElements[element++] = new CardholderDataElement(EXPIRY_YYMM, false, encDataFieldLen, fieldLen, BCD);
                encDataFieldLen += fieldLen;
            }
            if (msg.isFieldSet(_035_TRACK_2_DATA)) {
                fieldLen = (p2pEncrypt.getElementLength(TRACK_2_FULL_MSR) + 1) / 2;
                encElements[element++] = new CardholderDataElement(TRACK_2_FULL_MSR, false, encDataFieldLen, fieldLen, BCD);
                encDataFieldLen += fieldLen;
            }
            if (msg.isFieldSet(DE_047_ADDITIONAL_DATA_NATIONAL)) {
                de47Offset = encDataFieldLen;
                fieldLen = p2pEncrypt.getElementLength(CVV);

                // get offset of CVV in DE47
                CardholderDataElement cvvInDE47Element = findElementPlaceholder(msg.get(DE_047_ADDITIONAL_DATA_NATIONAL).getBytes(), 0, CVV, SUBST_VAL_CVV, ASCII);
                if (cvvInDE47Element != null) {
                    encElements[element] = new CardholderDataElement(CVV, false, encDataFieldLen + cvvInDE47Element.getSubstitueIndex(), fieldLen, ASCII);
                }
                encDataFieldLen += msg.get(DE_047_ADDITIONAL_DATA_NATIONAL).length();
            }

            // create buffer to hold output
            byte[] output = new byte[encDataFieldLen + 8];
            if (msg.isFieldSet(DE_047_ADDITIONAL_DATA_NATIONAL)) {
                System.arraycopy(msg.get(DE_047_ADDITIONAL_DATA_NATIONAL).getBytes(), 0, output, de47Offset, msg.get(DE_047_ADDITIONAL_DATA_NATIONAL).length());
            }
            String stanStr = msg.get(_011_SYS_TRACE_AUDIT_NUM);
            int stanInt = Integer.parseInt(stanStr);

            // set up encryption params
            IP2PEncrypt.EncryptParameters params;
            if (disableEncryption) {
                // encryption disabled
                params = new IP2PEncrypt.EncryptParameters(IP2PEncrypt.PaddingAlgorithm.NONE,
                        IP2PEncrypt.EncryptAlgorithm.NONE,
                        0, stanInt);
            } else {
                // encryption enabled
                params = new IP2PEncrypt.EncryptParameters(IP2PEncrypt.PaddingAlgorithm.RIGHT_ZEROS,
                        IP2PEncrypt.EncryptAlgorithm.AS2805_3DES_OFB,
                        0, stanInt);
            }

            // do the encryption
            EncryptResult encResult = p2pEncrypt.encrypt(output, params, encElements);
            if (encResult != null) {
                byte[] encData = encResult.getEncryptedMessage();

                // prefill DE47 with pattern before message building
                if (msg.isFieldSet(DE_047_ADDITIONAL_DATA_NATIONAL)) {
                    String de47pattern = new String(new char[msg.get(DE_047_ADDITIONAL_DATA_NATIONAL).length()]).replace("\0", Character.toString(SUBST_VAL_DE47));
                    msg.set(DE_047_ADDITIONAL_DATA_NATIONAL, de47pattern);
                }

                byte[] result = msg.toMsg();
                // now copy out data field by field into message
                int offset = 0;
                if (msg.isFieldSet(_002_PAN)) {
                    fieldLen = (p2pEncrypt.getElementLength(PAN) + 1) / 2;
                    CardholderDataElement ele = findElementPlaceholder(result, 12, PAN, SUBST_VAL_TRACK_2_PAN, BCD);
                    if (ele != null) {
                        System.arraycopy(encData, offset, result, ele.getSubstitueIndex(), fieldLen);
                        offset += fieldLen;
                    }
                }
                if (msg.isFieldSet(_014_EXPIRATION_DATE)) {
                    fieldLen = (p2pEncrypt.getElementLength(EXPIRY_YYMM) + 1) / 2;
                    CardholderDataElement ele = findElementPlaceholder(result, 12, EXPIRY_YYMM, SUBST_VAL_EXPIRY, BCD);
                    if (ele != null) {
                        System.arraycopy(encData, offset, result, ele.getSubstitueIndex(), fieldLen);
                        offset += fieldLen;
                    }
                }
                if (msg.isFieldSet(_035_TRACK_2_DATA)) {
                    fieldLen = (p2pEncrypt.getElementLength(TRACK_2_FULL_MSR) + 1) / 2;
                    CardholderDataElement ele = findElementPlaceholder(result, 12, TRACK_2_FULL_MSR, SUBST_VAL_TRACK_2_MSR, BCD);
                    if (ele != null) {
                        System.arraycopy(encData, offset, result, ele.getSubstitueIndex(), fieldLen);
                        offset += fieldLen;
                    }
                }

                if (msg.isFieldSet(DE_047_ADDITIONAL_DATA_NATIONAL)) {
                    fieldLen = msg.get(DE_047_ADDITIONAL_DATA_NATIONAL).length();
                    int de47offset = patternOffset(result, 12, SUBST_VAL_DE47, fieldLen);
                    if (de47offset > 0) {
                        System.arraycopy(encData, offset, result, de47offset, fieldLen);
                    } else {
                        Timber.e("DE47 pattern not found");
                    }
                }

                // return a modified byte array with encrypted parts overwritten
                return result;
            }
        }

        return msg.toMsg();
    }

    public static byte[] addMacAndEncrypt(IDependency d, As2805Till msg) throws Exception {
        byte[] msgPacked = msg.toMsg();

        if (!isSecurityDisabled(d)) {
            // sets elements array for mac operation
            CardholderDataElement[] macElements = getSecureElements(msgPacked);

            // first do the mac
            IP2PEncrypt.MacParameters macParameters = new IP2PEncrypt.MacParameters(IP2PEncrypt.MacAlgorithm.AS2805, 0, "");

            // append a dummy mac to the current message, so the bitmap has correct value
            byte[] dummyMac = new byte[8];
            msg.set(msg.isExtended() ? DE_128_MAC : DE_064_MAC, hex2Str(dummyMac));
            // re-pack with dummy MAC field
            msgPacked = msg.toMsg();

            // chop off the final 8 bytes (dummy mac) to be macced
            byte[] msgToMac = new byte[msgPacked.length - 8];
            System.arraycopy(msgPacked, 0, msgToMac, 0, msgPacked.length - 8);

            byte[] mac = p2pEncrypt.getMac(msgToMac, macParameters, macElements);

            // append mac
            msg.set(msg.isExtended() ? DE_128_MAC : DE_064_MAC, hex2Str(mac));
        }

        Timber.e("message to send");
        Timber.e(msg.toString());

        // now do encryption.
        return encryptSensitiveFields(msg, isSecurityDisabled(d));
    }

    @SuppressWarnings({"java:S3776","java:S6541"})// java:S3776: Cognitive complexity(20)
    private static boolean packCardData(As2805Till msg, TransRec trans, boolean useSavedCardDetails) throws UnknownFieldException {
        TCard cardinfo = trans.getCard();

        if (null == p2pEncrypt) {
            Timber.e("ERROR p2pEncrypt is NULL");
            return false;
        }

        if (useSavedCardDetails) {
            // if we need to use stored card data, load it into the p2pe module
            if (Util.isNullOrEmpty(trans.getSecurity().getEncTrack2())) {
                Timber.e("error retrieving saved card details");
                return false;
            }

            // we have encrypted data, load it into p2pe module from storage
            // calling stash/unstash in caller method
            if (!p2pEncrypt.decryptFromStorage(Util.hexStringToByteArray(trans.getSecurity().getEncTrack2()), FULL_TRACK_FORMAT)) {
                Timber.e("error decrypting stored card data ");
                return false;
            }

            // Also load CVV
            if (!Util.isNullOrEmpty(trans.getSecurity().getCvv()) && !p2pEncrypt.decryptFromStorage(Util.hexStringToByteArray(trans.getSecurity().getCvv()), CVV_FORMAT)) {
                Timber.e("error decrypting stored CVV data ");
                return false;
            }

            // load expiryDate tag data into the p2pe module
            // calling stash/unstash in caller method
            if (!Util.isNullOrEmpty(trans.getSecurity().getExpiryDateChip()) && !p2pEncrypt.decryptFromStorage(Util.hexStringToByteArray(trans.getSecurity().getExpiryDateChip()), EXPIRY_DATE_CHIP_FORMAT)) {
                Timber.e("error decrypting stored card data ");
                return false;
            }
        }

        boolean returnFlag = true;

        // if this was a manual pan transaction, and we have required data
        if (cardinfo.isManual() && p2pEncrypt.getElementLength(PAN) > 0) {

            // get expiry length - use long format expiry if we have it
            int expLen = p2pEncrypt.getElementLength(EXPIRY_YYMMDD_CHIP);
            if (expLen <= 0) {
                // short format - sourced from manual entry or swipe
                expLen = p2pEncrypt.getElementLength(EXPIRY_YYMM);
            }

            // return error if no expiry found - shouldn't happen
            if (expLen <= 0) {
                Timber.e("error expLen");
                returnFlag = false;
            } else {
                // pack pan and expiry if we have them
                msg.set(_002_PAN, As2805TillUtils.packSensitiveField(PAN, SUBST_VAL_TRACK_2_PAN));
                msg.set(_014_EXPIRATION_DATE, As2805TillUtils.packSensitiveField(EXPIRY_YYMM, SUBST_VAL_EXPIRY));
            }
        } else if (p2pEncrypt.getElementLength(TRACK_2_FULL_MSR) > 0) {
            // else it's msr/emv/ctls
            msg.set(_035_TRACK_2_DATA, As2805TillUtils.packSensitiveField(TRACK_2_FULL_MSR, SUBST_VAL_TRACK_2_MSR));
        } else {
            // else we don't have required card data, return error
            Timber.e("error - missing required card data, use saved = " + useSavedCardDetails + ", track2Element = " + TRACK_2_FULL_MSR);
            returnFlag = false;
        }

        return returnFlag;
    }


    private static byte[] packAuth(IDependency d, TransRec trans, MsgType msgType) {

        try {
            TProtocol proto = trans.getProtocol();
            TAmounts amounts = trans.getAmounts();
            TAudit auditinfo = trans.getAudit();
            PaymentSwitch paySwitchCfg = d.getPayCfg().getPaymentSwitch();
            As2805Till msg = new As2805Till();

            if (msgType == PREAUTH) {
                proto.setOriginalMessageType(100);
                msg.setMsgType(Iso8583.MsgType._0100_AUTH_REQ);
            } else {
                proto.setOriginalMessageType(200);
                msg.setMsgType(Iso8583.MsgType._0200_TRAN_REQ);
            }

            proto.setOriginalStan(proto.getStan());

            if (!packCardData(msg, trans, false)) {
                return new byte[0];
            }

            msg.putProcessingCode(As2805TillUtils.packProcCode(trans));
            msg.setTransactionAmount(amounts.getTotalAmount());

            // set DE7 and record transmission time on transaction record in case it's required for later reversal
            trans.getAudit().setLastTransmissionDateTime(msg.getTransmissionDateTime().setNow()); // sets _007_TRAN_DATE_TIME
            proto.setOriginalTransmissionDateTime(trans.getAudit().getLastTransmissionDateTime());
            msg.set(_011_SYS_TRACE_AUDIT_NUM, As2805TillUtils.packStan(proto.getStan()));
            msg.set(_012_LOCAL_TRAN_TIME, As2805TillUtils.packTransTime(trans, d.getPayCfg().getBankTimeZone()));
            msg.set(_013_LOCAL_TRAN_DATE, As2805TillUtils.packTransDate(trans, d.getPayCfg().getBankTimeZone()));
            msg.set(_022_POS_ENTRY_MODE, As2805TillUtils.packPosEntryMode(trans));
            msg.set(_023_CARD_SEQUENCE_NUM, As2805TillUtils.packCardSeqNumber(trans));
            msg.set(_025_POS_CONDITION_CODE, As2805TillUtils.packPosConditionCode(trans));
            msg.set(_032_ACQUIRING_INST_ID_CODE, As2805TillUtils.packAiic(paySwitchCfg));
            msg.set(_037_RETRIEVAL_REF_NUM, As2805TillUtils.packRetRefNumber(trans));
            msg.set(_041_CARD_ACCEPTOR_TERMINAL_ID, auditinfo.getTerminalId());
            msg.set(_042_CARD_ACCEPTOR_ID_CODE, As2805TillUtils.packMerchantId(auditinfo.getMerchantId()));
            msg.set(DE_047_ADDITIONAL_DATA_NATIONAL, As2805TillUtils.packAdditionalDataNational47(trans, SUBST_VAL_CVV, false));
            msg.set(_049_TRAN_CURRENCY_CODE, "036");

            String pinblock = getNonSensitiveElement(PIN_BLOCK);
            if (pinblock == null) {
                // if no pin was entered, this can be NULL. in that case we always need to send a 'null' pin block in de52
                Timber.i("no pin block sent, generate NULL pin block");
                secMal.as2805GetPinBlock(true, "0", trans.getProtocol().getStan(), (int) trans.getAmounts().getTotalAmount(), 1000, null);
                pinblock = getNonSensitiveElement(PIN_BLOCK);
            }

            if (pinblock != null) {
                msg.set(_052_PIN_DATA, pinblock);
            } else {
                Timber.e("ERROR - no pin block, sending msg without DE52");
            }

            // pack DE55 if not refund, and ICC or CTLS card type
            if (!trans.isRefund() && (trans.getCard().isIccCaptured() || trans.getCard().isCtlsCaptured())) {
                msg.set(DE_055_ICC_DATA, As2805TillUtils.packIccData(trans, msg, false));
            }

            // if msgType is Auth and if any cash component (pwcb or cashout) is present, pack it here otherwise pack with zero
            if (msgType == AUTH) {
                msg.set(DE_057_CASH_AMOUNT, isCashTransaction(trans) ? As2805TillUtils.packCashAmount(trans) : ZERO_FILLED_AMOUNT);
            }

            //TODO: TILL Field 095 Replacement Amount, used in increamental auth
            return addMacAndEncrypt(d, msg);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new PackRuntimeException(msgType, e);
        }
    }

    private static boolean isCashTransaction(TransRec trans) {
        return trans.getAmounts().getCashbackAmount() > 0 || trans.isCash();
    }

    private static byte[] packAdvice(IDependency d, TransRec trans) {

        try {
            Timber.i("stashing card data");
            p2pEncrypt.stash();

            TProtocol proto = trans.getProtocol();
            TAmounts amounts = trans.getAmounts();
            PaymentSwitch paySwitchCfg = d.getPayCfg().getPaymentSwitch();
            As2805Till msg = new As2805Till();

            proto.setOriginalMessageType(220);
            proto.setOriginalStan(proto.getStan());
            proto.setOriginalTransmissionDateTime(trans.getAudit().getTransDateTime());

            if (proto.getAdviceAttempts() > 0) {
                // set msg type to 0221 (advice repeat)
                msg.setMsgType(Iso8583.MsgType._0221_TRAN_ADV_REP);
            } else {
                // set msg type to 0220 (advice, first attempt)
                msg.setMsgType(Iso8583.MsgType._0220_TRAN_ADV);
            }

            if (!packCardData(msg, trans, true)) {
                Timber.e("ERROR PACKING CARD DATA for ADVICE");
                return new byte[0];
            }

            msg.putProcessingCode(As2805TillUtils.packProcCode(trans));
            msg.setTransactionAmount(amounts.getTotalAmount());
            msg.set(_007_TRAN_DATE_TIME, As2805TillUtils.packLocalDateTime(trans));
            msg.set(_011_SYS_TRACE_AUDIT_NUM, As2805TillUtils.packStan(proto.getStan()));
            msg.set(_012_LOCAL_TRAN_TIME, As2805TillUtils.packTransTime(trans, d.getPayCfg().getBankTimeZone()));
            msg.set(_013_LOCAL_TRAN_DATE, As2805TillUtils.packTransDate(trans, d.getPayCfg().getBankTimeZone()));
            msg.set(_022_POS_ENTRY_MODE, As2805TillUtils.packPosEntryMode(trans));
            msg.set(_023_CARD_SEQUENCE_NUM, As2805TillUtils.packCardSeqNumber(trans));

            msg.set(_025_POS_CONDITION_CODE, As2805TillUtils.packPosConditionCode(trans));
            msg.set(_032_ACQUIRING_INST_ID_CODE, As2805TillUtils.packAiic(paySwitchCfg));
            msg.set(_037_RETRIEVAL_REF_NUM, As2805TillUtils.packRetRefNumber(trans));
            msg.set(_038_AUTH_ID_RESPONSE, As2805TillUtils.packAuthCode(d, trans));
            msg.set(_041_CARD_ACCEPTOR_TERMINAL_ID, d.getPayCfg().getStid());
            msg.set(_042_CARD_ACCEPTOR_ID_CODE, As2805TillUtils.packMerchantId(d.getPayCfg().getMid()));

            msg.set(DE_047_ADDITIONAL_DATA_NATIONAL, As2805TillUtils.packAdditionalDataNational47(trans, SUBST_VAL_CVV, true));
            msg.set(_049_TRAN_CURRENCY_CODE, "036");

            if (trans.isRefund()) {
                Timber.i("not packing DE 55 for advice because refund");
            } else {
                if (!Util.isNullOrEmpty(trans.getEmvTagsString())) {
                    msg.set(DE_055_ICC_DATA, repackDE55Tags(trans, trans.getEmvTagsString())); // update tags to be included into msg with tags soted in secure storage
                } else if (!Util.isNullOrEmpty(trans.getCtlsTagsString())) {
                    msg.set(DE_055_ICC_DATA, repackDE55Tags(trans, trans.getCtlsTagsString())); // update tags to be included into msg with tags soted in secure storage
                } else {
                    Timber.i("not packing DE 55 for advice as no tag data saved on trans record");
                }
            }

            if (null != trans.getAmounts() && trans.getAmounts().getCashbackAmount() > 0) {
                msg.set(DE_057_CASH_AMOUNT, As2805TillUtils.packCashAmount(trans));
            } else {
                msg.set(DE_057_CASH_AMOUNT, ZERO_FILLED_AMOUNT);
            }

            msg.set(DE_062_ADDITIONAL_PRIVATE, packAdviceIndicator(trans));
            return addMacAndEncrypt(d, msg);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new PackRuntimeException(ADVICE, e);
        } finally {
            Timber.i("unstashing card data");
            p2pEncrypt.unstash();
        }
    }

    private static byte[] packReversalImpl(IDependency d, TransRec trans, int stan) {

        As2805Till msg = new As2805Till();
        TProtocol proto = trans.getProtocol();
        TAmounts amounts = trans.getAmounts();
        PaymentSwitch paySwitchCfg = d.getPayCfg().getPaymentSwitch();

        // For acquirers that need to have the original STAN (so RRN as well) as the transaction to be reversed
        if (Engine.getDep().getPayCfg().isIncludedOrginalStandInRec() || Engine.getDep().getPayCfg().isReversalCopyOriginal()) {
            stan = trans.getProtocol().getOriginalStan();
        }

        try {
            Timber.i("stashing card data");
            p2pEncrypt.stash();

            if (proto.getAdviceAttempts() > 0) {
                // set msg type to 0421 (reversal repeat)
                msg.setMsgType(Iso8583.MsgType._0421_ACQUIRER_REV_ADV_REP);
            } else {
                // set msg type to 0420 (reversal, first try)
                msg.setMsgType(Iso8583.MsgType._0420_ACQUIRER_REV_ADV);
            }

            if (!packCardData(msg, trans, true)) {
                Timber.e("Card Data of Transaction [%s] couldn't be packed", trans.getAudit().getReference());
                return new byte[0];
            }

            msg.putProcessingCode(As2805TillUtils.packProcCode(trans));
            msg.setTransactionAmount(amounts.getTotalAmount());
            msg.getTransmissionDateTime().setNow(); // packs _007_TRAN_DATE_TIME with time now. no need to save this

            msg.set(_011_SYS_TRACE_AUDIT_NUM, As2805TillUtils.packStan(stan));
            msg.set(_022_POS_ENTRY_MODE, As2805TillUtils.packPosEntryMode(trans));

            msg.set(_032_ACQUIRING_INST_ID_CODE, As2805TillUtils.packAiic(paySwitchCfg));

            msg.set(_037_RETRIEVAL_REF_NUM, trans.getProtocol().getRRN());
            msg.set(_041_CARD_ACCEPTOR_TERMINAL_ID, d.getPayCfg().getStid());
            msg.set(_042_CARD_ACCEPTOR_ID_CODE, As2805TillUtils.packMerchantId(d.getPayCfg().getMid()));

            msg.set(_049_TRAN_CURRENCY_CODE, "036");

            if (trans.isRefund()) {
                Timber.i("not packing DE 55 for reversal because refund");
            } else {
                if (!Util.isNullOrEmpty(trans.getEmvTagsString())) {
                    msg.set(DE_055_ICC_DATA, repackDE55Tags(trans, trans.getEmvTagsString())); // update tags to be included into msg with tags soted in secure storage
                } else if (!Util.isNullOrEmpty(trans.getCtlsTagsString())) {
                    msg.set(DE_055_ICC_DATA, repackDE55Tags(trans, trans.getCtlsTagsString())); // update tags to be included into msg with tags soted in secure storage
                } else {
                    Timber.i("not packing DE 55 for reversal as no tag data saved on trans record");
                }
            }

            if (null != trans.getAmounts() && trans.getAmounts().getCashbackAmount() > 0) {
                msg.set(DE_057_CASH_AMOUNT, As2805TillUtils.packCashAmount(trans));
            } else {
                msg.set(DE_057_CASH_AMOUNT, ZERO_FILLED_AMOUNT);
            }
            msg.set(DE_062_ADDITIONAL_PRIVATE, packForcedPostReasonIndicator(trans));
            msg.set(_090_ORIGINAL_DATA_ELEMENTS, As2805TillUtils.packOriginalDataElements(trans));
            //TODO: TILL Field 095 Replacement amount for partial reversal
            return addMacAndEncrypt(d, msg);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new PackRuntimeException(REVERSAL, e);
        } finally {
            Timber.i("unstashing card data");
            p2pEncrypt.unstash();
        }
    }

    private static byte[] packReversal(IDependency d, TransRec trans) {

        try {

            if (Engine.getDep().getP2PLib().getIP2PSec().getInstalledKeyType() == IP2PSec.InstalledKeyType.DUKPT) {
                Timber.i("PACK Reversal with DUKPT keys");
            } else {
                Timber.i("PACK Reversal with Master Session keys");
            }

            return packReversalImpl(d, trans, trans.getProtocol().getStan());

        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new PackRuntimeException(REVERSAL, e);
        }
    }

    /**
     * Packs F48 field for Network message type
     *
     * @return f48 contains ASCII Hex message
     */

    private static String pack191F48(IDependency d) throws IOException {
        ByteArrayOutputStream payload = new ByteArrayOutputStream();

        // perform RKI (if required)
        secMal.as2805GetKeys(d.getCustomer().getTcuKeyLength());

        byte[] ppid = secMal.as2805GetPpid();
        byte[] skManPkTcu = secMal.as2805GetSkManPkTcu();
        // length of the following should be 272 bytes, see ww spec
        // key management version 03
        payload.write(0x03);
        payload.write(skManPkTcu);
        // ppid
        payload.write(ppid);

        Timber.e("de48 nmic 191 request payload length = %s", payload.toByteArray().length);

        return Util.byteArrayToHexString(payload.toByteArray());
    }

    private static String pack192F48() throws IOException {
        byte[] skTcuEncBlock = secMal.as2805GenerateSkTcuKiBlock(randomNumber);
        byte[] ppid = secMal.as2805GetPpid();

        ByteArrayOutputStream payload = new ByteArrayOutputStream();
        // key management version 03
        payload.write(0x03);
        // skTcu(DFormat1(pkSp(DFormat1(KI, PPID, DTS, RN))))
        payload.write(skTcuEncBlock);
        // ppid
        payload.write(ppid);

        Timber.e("de48 nmic 192 request payload length = %s", payload.toByteArray().length);

        return Util.byteArrayToHexString(payload.toByteArray());
    }

    private static String pack193F48() throws IOException {
        byte[] encPpid = secMal.as2805GeteKiaPpid();

        ByteArrayOutputStream payload = new ByteArrayOutputStream();

        // key management version 03
        payload.write(0x03);

        // encrypted PPID under KIA
        if (encPpid != null) {
            payload.write(encPpid); // 32 bits
        }
        payload.write(new byte[]{0x00, 0x00, 0x00, 0x00}); // 4 x NULL bytes

        // ppid
        byte[] ppid = secMal.as2805GetPpid();
        payload.write(ppid);

        Timber.e("de48 nmic 193 request payload length = %s", payload.toByteArray().length);

        return Util.byteArrayToHexString(payload.toByteArray());
    }

    private static String pack170F48() throws IOException {
        byte[] ppid = secMal.as2805GetPpid();

        ByteArrayOutputStream payload = new ByteArrayOutputStream();

        // pinpad id
        payload.write(ppid);
        // key management version 03
        payload.write(0x03);

        if (secMal.as2805GeteKiaPpid() != null) {
            payload.write(secMal.as2805GeteKiaPpid());
        }
        if (secMal.as2805GeteKekPpasn(1) != null) {
            payload.write(secMal.as2805GeteKekPpasn(1));
        }
        if (secMal.as2805GeteKekPpasn(2) != null) {
            payload.write(secMal.as2805GeteKekPpasn(2));
        }

        Timber.e("de48 nmic 170 request payload length = %s", payload.toByteArray().length);

        return Util.byteArrayToHexString(payload.toByteArray());
    }

    private static String pack101F48() throws IOException {
        byte[] ppid = secMal.as2805GetPpid();

        ByteArrayOutputStream payload = new ByteArrayOutputStream();

        // pinpad id
        payload.write(ppid);
        // key management version 03
        payload.write(0x03);
        payload.write(secMal.as2805GeteKekPpasn(1));
        payload.write(secMal.as2805GeteKekPpasn(2));

        Timber.e("de48 nmic 101 request payload length = %s", payload.toByteArray().length);

        return Util.byteArrayToHexString(payload.toByteArray());
    }

    private static byte[] packNetwork(IDependency d, TransRec trans, String nmic) {

        try {

            TProtocol proto = trans.getProtocol();
            TAudit auditInfo = trans.getAudit();

            As2805Till msg = new As2805Till();

            proto.setOriginalMessageType(804);
            proto.setOriginalTransmissionDateTime(trans.getAudit().getTransDateTime());
            trans.setSoftwareVersion(d.getPayCfg().getPaymentAppVersion());

            msg.setMsgType(Iso8583.MsgType._0800_NWRK_MNG_REQ);
            msg.set(_032_ACQUIRING_INST_ID_CODE, As2805TillUtils.packAiic(d.getPayCfg().getPaymentSwitch()));

            msg.set(_041_CARD_ACCEPTOR_TERMINAL_ID, auditInfo.getTerminalId());
            msg.set(_042_CARD_ACCEPTOR_ID_CODE, As2805TillUtils.packMerchantId(auditInfo.getMerchantId()));

            // Pack DE48 according to the NMIC
            if (nmic.equals("191")) {
                // use zero stan for this msg, as per spec
                proto.setOriginalStan(0);
                msg.set(_011_SYS_TRACE_AUDIT_NUM, As2805TillUtils.packStan(0));
                msg.set(_048_ADDITIONAL_DATA, pack191F48(d));
            } else if (nmic.equals("192")) {
                // use zero stan for this msg, as per spec
                proto.setOriginalStan(0);
                msg.set(_011_SYS_TRACE_AUDIT_NUM, As2805TillUtils.packStan(0));
                msg.set(_048_ADDITIONAL_DATA, pack192F48());
            } else if (nmic.equals("193")) {
                // use zero stan for this msg, as per spec
                proto.setOriginalStan(0);
                msg.set(_011_SYS_TRACE_AUDIT_NUM, As2805TillUtils.packStan(0));
                msg.set(_048_ADDITIONAL_DATA, pack193F48());
            } else if (nmic.equals("170")) {
                // use zero stan for this msg, as per spec
                proto.setOriginalStan(0);
                msg.set(_011_SYS_TRACE_AUDIT_NUM, As2805TillUtils.packStan(0));
                msg.set(_048_ADDITIONAL_DATA, pack170F48());
            } else {
                // use ACTUAL stan for this msg, as per spec
                proto.setOriginalStan(proto.getStan());
                msg.set(_011_SYS_TRACE_AUDIT_NUM, As2805TillUtils.packStan(proto.getStan()));
                msg.set(_048_ADDITIONAL_DATA, pack101F48());
            }

            msg.set(DE_070_NMIC, nmic);

            Timber.e(msg.toString());
            return msg.toMsg();

        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new PackRuntimeException(NETWORK, e);
        }
    }


    private static byte[] packReconciliation(IDependency d, TransRec trans) {

        try {

            TProtocol proto = trans.getProtocol();
            TAudit auditinfo = trans.getAudit();
            Reconciliation r = reconciliationDao.findByTransId(trans.getUid());
            TReconciliationFigures reconcFigures = trans.getReconciliation().getReconciliationFigures();

            trans.setReconciliation(r);

            As2805Till msg = new As2805Till();

            msg.setMsgType(Iso8583.MsgType._0500_ACQUIRER_RECONCILE_REQ);
            msg.putProcessingCode(As2805TillUtils.packProcCode(trans)); // will pack to 960000 (current settlement totals enquiry)
            msg.set(_007_TRAN_DATE_TIME, As2805TillUtils.packLocalDateTime(trans));
            msg.set(_011_SYS_TRACE_AUDIT_NUM, As2805TillUtils.packStan(proto.getStan()));
            msg.set(_012_LOCAL_TRAN_TIME, As2805TillUtils.packTransTime(trans, d.getPayCfg().getBankTimeZone()));
            msg.set(_013_LOCAL_TRAN_DATE, As2805TillUtils.packTransDate(trans, d.getPayCfg().getBankTimeZone()));
            msg.set(_032_ACQUIRING_INST_ID_CODE, As2805TillUtils.packAiic(d.getPayCfg().getPaymentSwitch()));
            msg.set(_041_CARD_ACCEPTOR_TERMINAL_ID, auditinfo.getTerminalId());
            msg.set(_042_CARD_ACCEPTOR_ID_CODE, As2805TillUtils.packMerchantId(auditinfo.getMerchantId()));
            msg.set(_050_SETTLEMENT_CURRENCY_CODE, "036");
            msg.set(DE_061_ADDITIONAL_PRIVATE, "C20101");
            msg.set(_074_CREDITS_NUMBER, String.format(ZERO_FILLED_NUMBER_FORMATTER, reconcFigures.getCreditsNumber()));
            msg.set(_075_CREDITS_REVERSAL_NUMBER, String.format(ZERO_FILLED_NUMBER_FORMATTER, reconcFigures.getCreditsReversalNumber()));
            msg.set(_076_DEBITS_NUMBER, String.format(ZERO_FILLED_NUMBER_FORMATTER, reconcFigures.getDebitsNumber()));
            msg.set(_077_DEBITS_REVERSAL_NUMBER, String.format(ZERO_FILLED_NUMBER_FORMATTER, reconcFigures.getDebitsReversalNumber()));
            msg.set(_086_CREDITS_AMOUNT, String.format(ZERO_FILLED_AMOUNT_FORMATTER, reconcFigures.getCreditsAmount()));
            msg.set(_087_CREDITS_REVERSAL_AMOUNT, String.format(ZERO_FILLED_AMOUNT_FORMATTER, reconcFigures.getCreditsReversalAmount()));
            msg.set(_088_DEBITS_AMOUNT, String.format(ZERO_FILLED_AMOUNT_FORMATTER, reconcFigures.getDebitsAmount()));
            msg.set(_089_DEBITS_REVERSAL_AMOUNT, String.format(ZERO_FILLED_AMOUNT_FORMATTER, reconcFigures.getDebitsReversalAmount()));

            // amount, net reconciliation - need to add "sign" (C or D)
            long netReconcAmount = reconcFigures.getNetReconciliationAmount();
            String sign = "C"; // credit
            if (netReconcAmount < 0) {
                sign = "D"; // debit
                netReconcAmount = -netReconcAmount;
            }
            msg.set(_097_AMOUNT_NET_SETTLEMENT, String.format("%s%016d", sign, netReconcAmount));
            return addMacAndEncrypt(d, msg);

        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new PackRuntimeException(RECONCILIATION, e);
        }
    }

    private static byte[] packFileUpdate(IDependency d, TransRec trans, As2805TillProto.FileUpdate fileUpdate) {

        String msgNumber = String.format("%04d", fileUpdate.msgNumber);
        try {
            TProtocol proto = trans.getProtocol();
            TAudit auditinfo = trans.getAudit();
            PaymentSwitch paySwitchCfg = d.getPayCfg().getPaymentSwitch();
            As2805Till msg = new As2805Till();

            msg.setMsgType(Iso8583.MsgType._0300_ACQUIRER_FILE_UPDATE_REQ);

            msg.set(_011_SYS_TRACE_AUDIT_NUM, As2805TillUtils.packStan(proto.getStan()));
            msg.set(_032_ACQUIRING_INST_ID_CODE, As2805TillUtils.packAiic(paySwitchCfg));

            msg.set(_041_CARD_ACCEPTOR_TERMINAL_ID, auditinfo.getTerminalId());
            msg.set(_042_CARD_ACCEPTOR_ID_CODE, As2805TillUtils.packMerchantId(auditinfo.getMerchantId()));
            msg.set(_048_ADDITIONAL_DATA, "0001");
            msg.set(DE_070_NMIC, "151");
            msg.set(DE_071_MESSAGE_NUMBER, msgNumber);
            msg.set(_091_FILE_UPDATE_CODE, "4");
            msg.set(_101_FILE_NAME, TABLE_NAME);

            return addMacAndEncrypt(d, msg);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new PackRuntimeException(msgNumber, e);
        }
    }

    private static byte[] packAiicBlock() {
        // right-justify the AIIC in 16 byte buffer, BCD formatted
        // get string value from env var
        String aiicStr = Aiic.getCurValue();
        if (Util.isNullOrEmpty(aiicStr)) {
            Timber.e("AIIC not set, returning error");
            return new byte[0];
        } else if (aiicStr.length() > 32) {
            Timber.e("AIIC invalid length %d, returning error", aiicStr.length());
            return new byte[0];
        }

        // create output buffer, with AIIC as bcd padded to right end
        byte[] output = Util.hexStringToByteArray(Util.padLeft(aiicStr, 32, '0'));
        Timber.i("AIIC output bytes %s", Util.bcd2Str(output));

        return output;
    }

    private static String packForcedPostReasonIndicator(TransRec trans) {
        StringBuilder fieldData = new StringBuilder();
    /*
        Forced Post Reason Indicator (Tag C6) - The value populated here will reflect why the device has generated a reversal.
        0x03 = Signature failed verification (Reversal)
        0x06 = Void Transaction (Reversal) // Not Supported
        0x09 = CUP Verified Refund
        0x0A = MAC Error (Reversal)
     */
        if (trans.getAudit().getRejectReasonType() == SIGNATURE_REJECTED) {
            fieldData.append("C60103");
        } else if (trans.getAudit().getRejectReasonType() == MAC_FAILED) {
            fieldData.append("C6010A");
        }
        //TODO: Implement support for matched refunds
        // 0x09 = CUP Verified Refund 0x0A = MAC Error (Reversal)

        return Util.isNullOrEmpty(fieldData.toString()) ? null : fieldData.toString();
    }

    private static String packAdviceIndicator(TransRec trans) {
        StringBuilder fieldData = new StringBuilder();
    /*
        Advice Indicator (Tag C6) - The value populated in this field will reflect why the device has generated an advice.
        0x01 = No Host Response – Financial Transaction Timeout (Advice)
        0x02 = Issuer Not Available - RC=91 (Advice).
        0x03 = Signature failed verification (Reversal)
        0x04 = Continue in Fallback timer has not expired (Advice)
        0x05 = Completion Transaction (Advice)
        0x06 = Void Transaction (Reversal) // Not Supported
        0x07 = Offline Approved EMV Purchase (Advice)
        0x08 = Offline Approved Pre Auth EMV (Advice)
        0x09 = CUP Verified Refund
        0x0A = MAC Error (Reversal)
     */
        if (trans.getTransType() == COMPLETION) {
            fieldData.append("C60105");
        } else if (trans.getProtocol().getAuthMethod() == OFFLINE_EFB_AUTHORISED) {
            fieldData.append("C60104");
        } else if (trans.getProtocol().getAuthMethod() == EFB_AUTHORISED) {
            fieldData.append("C60101");
        } else if (trans.getProtocol().getAuthMethod() == LINK_DOWN_EFB_AUTHORISED) {
            fieldData.append("C60102");
        } else if (trans.getCard().isCtlsCaptured() || trans.getCard().isIccCaptured()) {
            fieldData.append(trans.isPreAuth() ? "C60108" : "C60107");
        }

        return Util.isNullOrEmpty(fieldData.toString()) ? null : fieldData.toString();
    }

    /**
     * Lists request-response of each message used in Till
     */
    public enum MsgType {
        PREAUTH(_0100_AUTH_REQ, _0110_AUTH_REQ_RSP),
        AUTH(_0200_TRAN_REQ, _0210_TRAN_REQ_RSP),
        ADVICE(_0220_TRAN_ADV, _0230_TRAN_ADV_RSP),
        NETWORK(_0800_NWRK_MNG_REQ, _0810_NWRK_MNG_REQ_RSP),
        REVERSAL(_0420_ACQUIRER_REV_ADV, _0430_ACQUIRER_REV_ADV_RSP),
        RECONCILIATION(_0500_ACQUIRER_RECONCILE_REQ, _0510_ACQUIRER_RECONCILE_REQ_RSP),
        UPDATE(_0300_ACQUIRER_FILE_UPDATE_REQ, _0310_ACQUIRER_FILE_UPDATE_RSP);

        private final int sendMsgId;
        private final int receiveMsgId;

        MsgType(int sendMsgId, int receiveMsgId) {
            this.sendMsgId = sendMsgId;
            this.receiveMsgId = receiveMsgId;
        }

        public int getSendMsgId() {
            return this.sendMsgId;
        }

        public int getReceiveMsgId() {
            return this.receiveMsgId;
        }
    }

    /**
     * Unpack results for {@link As2805TillPack#unpack(IDependency, byte[], TransRec, MsgType)}
     */
    public enum UnPackResult {
        UNPACK_OK,              // Success
        MAC_ERROR,              // Mac Field couldn't be verified/failed
        VERIFICATION_FAILED,    // Fields unpacked don't match the fields packed
        GENERIC_FAILURE         // Catch-all failure. If seen a lot of times, needs to be expanded
    }


}
