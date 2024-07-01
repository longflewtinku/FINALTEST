package com.linkly.libengine.engine.protocol.iso8583;

import static com.linkly.libengine.engine.protocol.iso8583.As2805EftexPack.MsgType.ADVICE;
import static com.linkly.libengine.engine.protocol.iso8583.As2805EftexPack.MsgType.REVERSAL;
import static com.linkly.libengine.engine.protocol.iso8583.As2805EftexPack.MsgType.UPDATE;
import static com.linkly.libengine.engine.protocol.iso8583.As2805EftexUtils.packActionCode;
import static com.linkly.libengine.engine.protocol.iso8583.As2805EftexUtils.packAdditionalAmounts;
import static com.linkly.libengine.engine.protocol.iso8583.As2805EftexUtils.packAdditionalData;
import static com.linkly.libengine.engine.protocol.iso8583.As2805EftexUtils.packAuthCode;
import static com.linkly.libengine.engine.protocol.iso8583.As2805EftexUtils.packCardSeqNumber;
import static com.linkly.libengine.engine.protocol.iso8583.As2805EftexUtils.packDe48AdviceStyle;
import static com.linkly.libengine.engine.protocol.iso8583.As2805EftexUtils.packDe55AdviceStyle;
import static com.linkly.libengine.engine.protocol.iso8583.As2805EftexUtils.packIccDataForDe48;
import static com.linkly.libengine.engine.protocol.iso8583.As2805EftexUtils.packIccDataInFF20Container;
import static com.linkly.libengine.engine.protocol.iso8583.As2805EftexUtils.packLocalDateTimeDe12;
import static com.linkly.libengine.engine.protocol.iso8583.As2805EftexUtils.packMerchantId;
import static com.linkly.libengine.engine.protocol.iso8583.As2805EftexUtils.packOriginalDataElements;
import static com.linkly.libengine.engine.protocol.iso8583.As2805EftexUtils.packPosDataCode;
import static com.linkly.libengine.engine.protocol.iso8583.As2805EftexUtils.packProcCode;
import static com.linkly.libengine.engine.protocol.iso8583.As2805EftexUtils.packReconIndicator;
import static com.linkly.libengine.engine.protocol.iso8583.As2805EftexUtils.packRetRefNumber;
import static com.linkly.libengine.engine.protocol.iso8583.As2805EftexUtils.packSensitiveField;
import static com.linkly.libengine.engine.protocol.iso8583.As2805EftexUtils.packStan;
import static com.linkly.libengine.engine.protocol.iso8583.As2805EftexUtils.unpackIccData;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583Rev93.Bit._002_PAN;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583Rev93.Bit._011_SYS_TRACE_AUDIT_NUM;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583Rev93.Bit._012_LOCAL_TRAN_DATETIME;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583Rev93.Bit._014_EXPIRY_DATE;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583Rev93.Bit._022_POS_DATA_CODE;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583Rev93.Bit._023_CARD_SEQ_NR;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583Rev93.Bit._024_FUNC_CODE;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583Rev93.Bit._028_RECON_DATE;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583Rev93.Bit._029_RECON_INDICATOR;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583Rev93.Bit._035_TRACK_2_DATA;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583Rev93.Bit._037_RET_REF_NR;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583Rev93.Bit._038_APPROVAL_CODE;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583Rev93.Bit._039_ACTION_CODE;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583Rev93.Bit._041_TERMINAL_ID;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583Rev93.Bit._042_CARD_ACCEPTOR_ID;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583Rev93.Bit._049_TRAN_CURRENCY_CODE;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583Rev93.Bit._050_SETTLEMENT_CURRENCY_CODE;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583Rev93.Bit._052_PIN_DATA;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583Rev93.Bit._054_ADDITIONAL_AMOUNTS;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583Rev93.Bit._055_ICC_DATA;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583Rev93.Bit._056_ORIG_DATA_ELEMENTS;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583Rev93.Bit._064_MAC;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583Rev93.Bit._074_NR_CREDITS;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583Rev93.Bit._075_NR_CREDITS_REVERSAL;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583Rev93.Bit._076_NR_DEBITS;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583Rev93.Bit._077_NR_DEBITS_REVERSAL;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583Rev93.Bit._081_NR_AUTHS;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583Rev93.Bit._086_AMOUNT_CREDITS;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583Rev93.Bit._087_AMOUNT_CREDITS_REVERSAL;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583Rev93.Bit._088_AMOUNT_DEBITS;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583Rev93.Bit._089_AMOUNT_DEBITS_REVERSAL;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583Rev93.Bit._096_KEY_MANAGEMENT_DATA;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583Rev93.Bit._097_AMOUNT_NET_RECON;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583Rev93.Bit._101_FILE_NAME;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583Rev93.Bit._128_MAC;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583Rev93.MsgType._1100_AUTH_REQ;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583Rev93.MsgType._1110_AUTH_REQ_RSP;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583Rev93.MsgType._1200_TRAN_REQ;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583Rev93.MsgType._1210_TRAN_REQ_RSP;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583Rev93.MsgType._1220_TRAN_ADV;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583Rev93.MsgType._1230_TRAN_ADV_RSP;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583Rev93.MsgType._1304_FILE_ACTION_REQ;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583Rev93.MsgType._1314_FILE_ACTION_REQ_RSP;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583Rev93.MsgType._1420_TRAN_REV_ADV;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583Rev93.MsgType._1430_TRAN_REV_ADV_RSP;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583Rev93.MsgType._1520_RECON_ADV;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583Rev93.MsgType._1530_RECON_ADV_RSP;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583Rev93.MsgType._1804_NWRK_MNG_REQ;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583Rev93.MsgType._1814_NWRK_MNG_REQ_RSP;
import static com.linkly.libengine.engine.reporting.ReconciliationManager.reconciliationDao;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.MessageStatus.FINALISED;
import static com.linkly.libmal.global.util.Util.hex2Str;
import static com.linkly.libsecapp.IP2PEncrypt.ElementType.CVV;
import static com.linkly.libsecapp.IP2PEncrypt.ElementType.EXPIRY_YYMM;
import static com.linkly.libsecapp.IP2PEncrypt.ElementType.EXPIRY_YYMMDD_CHIP;
import static com.linkly.libsecapp.IP2PEncrypt.ElementType.PAN;
import static com.linkly.libsecapp.IP2PEncrypt.ElementType.PIN_BLOCK;
import static com.linkly.libsecapp.IP2PEncrypt.ElementType.TRACK_2_FULL_MSR;
import static com.linkly.libsecapp.IP2PEncrypt.ElementType.TRACK_2_SHORT_FORMAT;
import static com.linkly.libsecapp.IP2PEncrypt.EncryptAlgorithm.AS2805_3DES_OFB;
import static com.linkly.libsecapp.IP2PEncrypt.EncryptForStorageType.FULL_TRACK_FORMAT;
import static com.linkly.libsecapp.IP2PEncrypt.PackFormat.ASCII;
import static com.linkly.libsecapp.IP2PEncrypt.PackFormat.BCD;

import android.annotation.SuppressLint;

import com.linkly.libengine.BuildConfig;
import com.linkly.libengine.config.paycfg.PaymentSwitch;
import com.linkly.libengine.dependencies.IDependency;
import com.linkly.libengine.engine.Engine;
import com.linkly.libengine.engine.protocol.IProto;
import com.linkly.libengine.engine.protocol.iso8583.openisoj.As2805Eftex;
import com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583Rev93;
import com.linkly.libengine.engine.protocol.iso8583.openisoj.exceptions.FormatException;
import com.linkly.libengine.engine.protocol.iso8583.openisoj.exceptions.UnknownFieldException;
import com.linkly.libengine.engine.reporting.Reconciliation;
import com.linkly.libengine.engine.transactions.TransRec;
import com.linkly.libengine.engine.transactions.properties.TAmounts;
import com.linkly.libengine.engine.transactions.properties.TAudit;
import com.linkly.libengine.engine.transactions.properties.TCard;
import com.linkly.libengine.engine.transactions.properties.TProtocol;
import com.linkly.libengine.engine.transactions.properties.TReconciliationFigures;
import com.linkly.libengine.env.Aiic;
import com.linkly.libmal.MalFactory;
import com.linkly.libmal.global.util.HexDump;
import com.linkly.libmal.global.util.Util;
import com.linkly.libsecapp.CardholderDataElement;
import com.linkly.libsecapp.EncryptResult;
import com.linkly.libsecapp.IP2PEncrypt;
import com.linkly.libsecapp.IP2PSec;
import com.linkly.libsecapp.P2PLib;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Locale;

import timber.log.Timber;

public class As2805EftexPack {
    // these DE48 function code values are defined in the EFTEX specification "Termapp.ISO interface - EFTEX enhancements, v1.0"
    public static final String RKI_REQUEST_INITIAL = "897";
    public static final String RKI_REQUEST_SECOND = "898";
    public static final String RKI_REQUEST_FINAL = "899";
    public static final String SESSION_KEY_EXCHANGE = "811";
    public static final String ACTION_CODE_KEY_MGMT_APPROVED = "800";

    public static final char SUBST_VAL_CVV = 'A';
    private static final char SUBST_VAL_TRACK_2_MSR = 'B';
    private static final char SUBST_VAL_TRACK_2_PAN = 'C';
    private static final char SUBST_VAL_TRACK_2_SHORT = 'D';
    private static final char SUBST_VAL_EXPIRY = 'E';

    private static byte[] randomNumber;
    private static String tableName = "EPAT"; // only download this table until get further info about the others

    private static final int TERMAPP_ENCRYPTED_HEADER_OVERHEAD = 25;

    public static byte[] packAiicBlock() {
        // right-justify the AIIC in 16 byte buffer, BCD formatted
        // get string value from env var
        String aiicStr = Aiic.getCurValue();
        if( Util.isNullOrEmpty(aiicStr) ) {
            Timber.e( "AIIC not set, returning error" );
            return null;
        } else if( aiicStr.length() > 16 ) {
            Timber.e( "AIIC invalid length %d, returning error", aiicStr.length() );
            return null;
        }

        // check length, if odd then pre-pend a zero
        if( aiicStr.length() % 2 == 1 ) {
            aiicStr = "0" + aiicStr;
        }

        // convert to bcd
        byte[] aiicBcd = Util.hexStringToByteArray(aiicStr);

        // create output buffer, and copy aiic bcd to right end
        byte[] output = new byte[16];
        System.arraycopy( aiicBcd, 0, output, 16-aiicBcd.length, aiicBcd.length );
        return output;
    }

    public static byte[] pack(IDependency d, TransRec trans, MsgType msgType, String nmic) throws FormatException {

        byte[] result = null;
        try {
            switch( msgType ) {
                case PREAUTH:
                case AUTH:
                    // use same pack code for preauth (0100), regular auth (0200)
                    result = packAuth(d, trans);
                    break;
                case DEFERRED_AUTH:
                    // deferred auths are a hybrid of regular auths and advices. they can be queued like advices, but msg format is very similar to a regular auth
                    result = packDeferredAuth(d, trans);
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
                    Timber.e( "Error - unexpected/unhandled msgType %s", msgType.name() );
                    break;
            }

            return result;

        } catch (Exception e) {
            trans.getProtocol().setMessageStatus(FINALISED);
            trans.save();
            Timber.e("Pack failed, remove Transaction ref [%s] from batch as useless", trans.getAudit().getReference());
            Timber.w(e);
            throw e;
        }
    }

    public static byte[] packUpdate(IDependency d, TransRec trans, MsgType msgType, As2805EftexProto.FileUpdate fileUpdate) {

        byte[] result = null;
        try {
            if (msgType == UPDATE) {
                result = packFileUpdate(d, trans, fileUpdate);
            }
            return result;

        } catch (Exception e) {
            trans.getProtocol().setMessageStatus(FINALISED);
            trans.save();
            Timber.i("Pack failed, remove from batch as useless");
            Timber.w(e);
            throw e;
        }
    }

    public static boolean isSecurityDisabled(IDependency d) {
        PaymentSwitch paySwitchCfg = d.getPayCfg().getPaymentSwitch();
        return null != paySwitchCfg && paySwitchCfg.isDisableSecurity();
    }


    /**
     * Perform validations on the response message data elements.
     * Validates:
     * - msg type as expected (always),
     * - TID matches (if present),
     * - MID matches (if present)
     * - and stan matches (always)
     *
     * @param sentMessageType {@link MsgType} original Message type
     * @param trans           {@link TransRec} object
     * @param responseData    {@link As2805Eftex} response object
     * @return false if any validation fails, true otherwise
     */
    private static boolean validateResponseMessage(As2805Eftex responseData, TransRec trans, MsgType sentMessageType) {
        // always validate, should always be present
        if (responseData.getMsgType() != sentMessageType.receiveMsgId) {
            Timber.e("Received the incorrect Message type. Expected [%x], got [%x]", sentMessageType.receiveMsgId, responseData.getMsgType());
            return false;
        }

        // only validate TID if it's in the response, otherwise matching STAN only is okay - otherwise stuck reversals can occur
        if ( responseData.isFieldSet(_041_TERMINAL_ID ) && (!responseData.verifyString(trans.getAudit().getTerminalId(), _041_TERMINAL_ID))) {
            Timber.e("Terminal IDs don't match. Expected [%s], got [%s]", trans.getAudit().getTerminalId().trim(), responseData.get(_041_TERMINAL_ID).trim() );

            // IAAS-2550 : to prevent stuck reversals/advices on TID/MID changes, we don't return false here for those message-types.
            if ( !(sentMessageType == REVERSAL) && !(sentMessageType == ADVICE)) {
                return false;
            }
        }

        // only validate MID if it's in the response, otherwise matching STAN only is okay - otherwise stuck reversals can occur
        if (responseData.isFieldSet(_042_CARD_ACCEPTOR_ID ) && (!responseData.verifyString(trans.getAudit().getMerchantId(), _042_CARD_ACCEPTOR_ID))) {
            Timber.e("Merchant IDs don't match. Expected [%s], got [%s]", trans.getAudit().getMerchantId().trim(), responseData.get(_042_CARD_ACCEPTOR_ID).trim());

            // IAAS-2550 : to prevent stuck reversals/advices on TID/MID changes, we don't return false here for those messages-types.
            if ( !(sentMessageType == REVERSAL) && !(sentMessageType == ADVICE)) {
                return false;
            }
        }

        // always validate, should always be present
        if ((trans.getProtocol().getStan() != Integer.parseInt(responseData.get(_011_SYS_TRACE_AUDIT_NUM)))) {
            Timber.e("STAN doesn't match. Expected [%s], got [%s]", trans.getProtocol().getStan().toString(), responseData.get(_011_SYS_TRACE_AUDIT_NUM));
            return false;
        }

        return true;
    }

    private static byte[] decryptMessage(byte[] input, TransRec trans) {
        if( input == null || input.length < (TERMAPP_ENCRYPTED_HEADER_OVERHEAD+1) ) {
            Timber.e( "insufficient data to decrypt" );
            throw new IllegalArgumentException();
        }
        // response msg is encrypted, skip to the encrypted bit and decrypt
        byte[] encryptedData = new byte[input.length-TERMAPP_ENCRYPTED_HEADER_OVERHEAD-1];

        // skip first 'B' byte
        System.arraycopy(input, TERMAPP_ENCRYPTED_HEADER_OVERHEAD+1, encryptedData, 0, encryptedData.length );

        // get stan from trans record
        byte[] decrypted = P2PLib.getInstance().getIP2PSec().as2805DecryptMessage(encryptedData, trans.getProtocol().getStan(), IP2PEncrypt.PaddingAlgorithm.FF_BYTES );
        if( decrypted == null ) {
            Timber.e( "Error decrypting message" );
            return null;
        }

        // pre-pend 'B' char in front so it's similar to normal output and common code can be used
        byte[] output = new byte[decrypted.length+1];
        output[0] = 'B';
        System.arraycopy( decrypted, 0, output, 1, decrypted.length );
        return output;
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
    public static UnPackResult unpack(IDependency d, byte[] responseData, TransRec trans, MsgType sentMessageType, String funcCode) {
        As2805Eftex resp;
        try {
            if (responseData != null) {
                if( responseData[0] == 'T' ) {
                    responseData = decryptMessage(responseData, trans);
                }
                Timber.e( "Rx Msg: %s", HexDump.dumpHexString(responseData) );

                resp = new As2805Eftex(responseData);
                Timber.e("Rx msg type: %x, %s", resp.getMsgType(), resp.toString());

                // Sanity checks
                if (!As2805EftexPack.validateResponseMessage(resp, trans, sentMessageType)) {
                    return UnPackResult.VERIFICATION_FAILED;
                }

                // validate MAC for these message types
                boolean validateMac;
                boolean clearMacBitInBitmap = false;
                switch (resp.getMsgType()) {
                    case 0x1110:
                    case 0x1210:
                    case 0x1230:
                    case 0x1430:
                        validateMac = true;
                        break;
                    case 0x1814:
                        // if func code is 811 (session key exchange), it is macced
                        validateMac = SESSION_KEY_EXCHANGE.equals(funcCode);
                        clearMacBitInBitmap = false;
                        break;
                    default:
                        validateMac = false;
                        break;
                }

                // only unpack key mgmt data if action code = 800 (approved)
                if( ACTION_CODE_KEY_MGMT_APPROVED.equals(resp.get(_039_ACTION_CODE))) {
                    // act on key data before validating MAC
                    String f96Response = resp.get(_096_KEY_MANAGEMENT_DATA);
                    if (!Util.isNullOrEmpty(f96Response)) {
                        unpackField96(trans, f96Response, resp, funcCode);
                    }
                }

                if (validateMac && !isSecurityDisabled(d) ) {
                    byte[] maccedData = new byte[responseData.length-1];
                    System.arraycopy(responseData, 1, maccedData, 0, maccedData.length );
                    boolean macValid = false;
                    int macByteOffset = 0;
                    final int MAC_BYTE_OFFSET_NORMAL_BITMAP = 4+7;
                    final int MAC_BYTE_OFFSET_EXTENDED_BITMAP = 4+7+8;

                    if( resp.get(_064_MAC) != null ) {
                        macByteOffset = MAC_BYTE_OFFSET_NORMAL_BITMAP;
                    } else if( resp.get(_128_MAC) != null ) {
                        macByteOffset = MAC_BYTE_OFFSET_EXTENDED_BITMAP;
                    }

                    if( macByteOffset != 0 ) {
                        if( clearMacBitInBitmap ) {
                            // modify input data to clear mac bit in bitmap
                            maccedData[macByteOffset] &= ~1;
                        }
                        Timber.i("Data to mac, with mac on end: %s", HexDump.dumpHexString(maccedData));
                        macValid = P2PLib.getInstance().getIP2PSec().as2805VerifyMac(maccedData);
                    }

                    if( !macValid ) {
                        // mac verify failed. downgrade our logon state to trigger required action
                        Timber.e("MAC VERIFICATION ERROR DETECTED");
                        Timber.i("Data to mac, with mac on end: %s", HexDump.dumpHexString(maccedData));
                        d.getProtocol().setInternalRejectReason(trans, IProto.RejectReasonType.MAC_FAILED);

                        // return the MAC failed. This will trigger an immediate reversal after a logon is done
                        return UnPackResult.MAC_ERROR;
                    }
                }

                updateResponseCodeMap(resp, trans);

                // Unpack date and time values for settlement and bank date times
                String settlementDate = resp.get(_028_RECON_DATE); // yyMMdd format
                if (settlementDate != null) {
                    trans.getProtocol().setSettlementDate(settlementDate);
                }

                String bankDateTime = resp.get(_012_LOCAL_TRAN_DATETIME); // date/time in YYMMDDhhmmss format
                trans.getProtocol().setBankTime(As2805EftexUtils.getBankTimeHHMMSS(bankDateTime));
                trans.getProtocol().setBankDate(As2805EftexUtils.getBankDateYYMMDD(bankDateTime));

                String rrnCode = resp.get(_037_RET_REF_NR);
                if (rrnCode != null && rrnCode.length() > 0) {
                    if(trans.getProtocol().getRRN() == null) {
                        Timber.e("RRN not previously set. Updating to %s", rrnCode);
                        // apparently RRN could contain some white space, so trimming it before saving
                        trans.getProtocol().setRRN(rrnCode.trim());
                    } else if(!rrnCode.equals(trans.getProtocol().getRRN())) {
                        Timber.e("Mismatching RRN values: %s to %s", rrnCode, trans.getProtocol().getRRN());
                    }
                }

                String authCode = resp.get(_038_APPROVAL_CODE);
                if (authCode != null && authCode.length() > 0) {
                    trans.getProtocol().setAuthCode(authCode);
                }

                String field55 = resp.get(_055_ICC_DATA);
                if (field55 != null) {
                    unpackIccData(trans, field55);
                }
            }
            return UnPackResult.UNPACK_OK;
        } catch (Exception e) {
            Timber.w(e);
            return UnPackResult.GENERIC_FAILURE;
        }
    }


    private static void updateResponseCodeMap(As2805Eftex resp, TransRec trans) {
        boolean overrideResponseCode;
        boolean updateAdviceResponseCode;

        Timber.e("GetMessage Type: %04x ",resp.getMsgType());
        switch (resp.getMsgType()) {
            case 0x1230:
            case 0x1430:
                overrideResponseCode = false;
                updateAdviceResponseCode = true;
                break;

            case 0x1210:
                // deferred auths are like advices, we don't want to update resp codes, text etc
                overrideResponseCode = !trans.isDeferredAuth();
                updateAdviceResponseCode = trans.isDeferredAuth();
                break;
            case 0x1110:
            case 0x1530:
            default:
                overrideResponseCode = true;
                updateAdviceResponseCode = false;
                break;
        }

        // We shouldn't be updating the main response code when a txn record is reversed, as get last receipt for any reversed transaction shows Approved receipt  TASK: IAAS-1870
        String responseCode = resp.get(_039_ACTION_CODE);
        if (overrideResponseCode) {
            // set display and receipt text based off response code
            trans.setProtocol(new As2805EftexRspCodeMap().populateProtocolRecord(trans.getProtocol(), responseCode, trans.getTransType()));
        }

        // if advice or deferred auth, set the advice response code and deferred auth approved flags
        if( updateAdviceResponseCode ){
            trans.getProtocol().setAdviceResponseCode(responseCode);
        }
    }

    private static void unpackField96_1814(TransRec trans, String f96Response, String funcCode) {

        // Field 48 varies depending on the NMIC
        switch (funcCode) {
            case RKI_REQUEST_INITIAL:
                unpackRsaKeyInitPart1(trans, f96Response);
                break;

            case RKI_REQUEST_SECOND:
                unpackRsaKeyInitPart2(trans, f96Response);
                break;

            case RKI_REQUEST_FINAL:
                unpackRsaKeyInitPart3(trans, f96Response);
                break;

            case SESSION_KEY_EXCHANGE:
                unpackSessionKeyExchange(trans, f96Response);
                break;

            default:
                Timber.e("Unhandled F96 response func code: %s", funcCode);
                break;
        }

    }

    // Unpacks field 48 and applies it to the trans rec (If need be) based on the type of message
    // Annoyingly this varies depending on what message is being sent
    private static void unpackField96(TransRec trans, String f48Response, As2805Eftex resp, String funcCode) {

        // Field 48 varies depending on the message
        switch (resp.getMsgType()) {
            case _1814_NWRK_MNG_REQ_RSP:
                unpackField96_1814(trans, f48Response, funcCode);
                break;

            default:
                Timber.e("Unhandled F48 response message: %x", resp.getMsgType());
                break;
        }
    }

    private static void unpackRsaKeyInitPart1(TransRec trans, String keyData) {

        byte[] respPayload = Util.hexStringToByteArray(keyData);

        // response payload format
        // PKsp modulus || PKsp exponent, each is variable and LLLLvar
        // RNsp 8 bytes
        if(255 != respPayload.length) {
            Timber.e( "invalid input data length %d", respPayload.length);
            throw new IllegalArgumentException();
        }

        byte[] pkSpMod = new byte[240];
        byte[] pkSpExp = new byte[3];
        byte[] rnSp = new byte[8];

        System.arraycopy(respPayload, 2, pkSpMod, 0, 240 );
        System.arraycopy(respPayload, 244, pkSpExp, 0, 3 );
        System.arraycopy(respPayload, 247, rnSp, 0, 8 );

        // eftex PRODUCTION public sponsor key, length = 0xF0h = 240 bytes = 1920 bits
        byte[] pkSpEftexProd = {
                (byte)0xC1, (byte)0x48, (byte)0x5E, (byte)0xE5, (byte)0x69, (byte)0x5E, (byte)0xB1, (byte)0xB5, (byte)0xCD, (byte)0x39, (byte)0x6E, (byte)0x72, (byte)0xB4, (byte)0xE2, (byte)0x45, (byte)0x6D,
                (byte)0x6E, (byte)0xD4, (byte)0x3A, (byte)0x04, (byte)0x2D, (byte)0x2C, (byte)0x66, (byte)0x9D, (byte)0x17, (byte)0xA8, (byte)0x41, (byte)0x09, (byte)0xCA, (byte)0x0E, (byte)0x0E, (byte)0xF9,
                (byte)0x32, (byte)0xED, (byte)0xE8, (byte)0x44, (byte)0x50, (byte)0x45, (byte)0x62, (byte)0x72, (byte)0xF8, (byte)0x8C, (byte)0xB7, (byte)0x32, (byte)0x20, (byte)0x2B, (byte)0x2E, (byte)0x19,
                (byte)0xDD, (byte)0x98, (byte)0x06, (byte)0x28, (byte)0xCA, (byte)0xF3, (byte)0x86, (byte)0x8B, (byte)0x35, (byte)0x4C, (byte)0x8D, (byte)0x44, (byte)0xDA, (byte)0xCC, (byte)0xDB, (byte)0x8E,
                (byte)0x34, (byte)0x07, (byte)0xEA, (byte)0x3E, (byte)0xC1, (byte)0xE2, (byte)0xB1, (byte)0x9E, (byte)0x47, (byte)0x93, (byte)0x10, (byte)0x0E, (byte)0x3A, (byte)0x0D, (byte)0x87, (byte)0x21,
                (byte)0x67, (byte)0x26, (byte)0xD1, (byte)0xA6, (byte)0xC3, (byte)0x9D, (byte)0xA3, (byte)0xF0, (byte)0xD8, (byte)0x1D, (byte)0x21, (byte)0x02, (byte)0x4C, (byte)0x1C, (byte)0x3C, (byte)0x1D,
                (byte)0xE3, (byte)0xB7, (byte)0x8F, (byte)0xC3, (byte)0x7D, (byte)0x05, (byte)0x83, (byte)0x3C, (byte)0x04, (byte)0xFB, (byte)0x51, (byte)0xC8, (byte)0xE9, (byte)0x6B, (byte)0x2B, (byte)0x3D,
                (byte)0x7C, (byte)0xA3, (byte)0x18, (byte)0x2E, (byte)0xAE, (byte)0x4C, (byte)0x55, (byte)0xD4, (byte)0xFC, (byte)0x8B, (byte)0x5E, (byte)0x87, (byte)0x65, (byte)0x55, (byte)0x6B, (byte)0x8D,
                (byte)0xE2, (byte)0x13, (byte)0x13, (byte)0xFB, (byte)0xC9, (byte)0x8F, (byte)0x3F, (byte)0x49, (byte)0x7C, (byte)0xEF, (byte)0x36, (byte)0x6B, (byte)0xF8, (byte)0x4A, (byte)0x74, (byte)0xF0,
                (byte)0xEA, (byte)0x47, (byte)0x37, (byte)0x27, (byte)0xC3, (byte)0x2B, (byte)0x30, (byte)0xE4, (byte)0xD6, (byte)0x71, (byte)0xFC, (byte)0x3E, (byte)0xDE, (byte)0x4E, (byte)0xFB, (byte)0xA4,
                (byte)0x17, (byte)0x1A, (byte)0x47, (byte)0x95, (byte)0x6C, (byte)0x5A, (byte)0xA1, (byte)0x14, (byte)0xB9, (byte)0x67, (byte)0xC2, (byte)0x51, (byte)0x37, (byte)0xF5, (byte)0x44, (byte)0x28,
                (byte)0xE8, (byte)0xD0, (byte)0xC0, (byte)0x08, (byte)0xFA, (byte)0x2D, (byte)0x05, (byte)0x0F, (byte)0x8A, (byte)0xB1, (byte)0x22, (byte)0x5D, (byte)0x21, (byte)0x23, (byte)0xBF, (byte)0xDC,
                (byte)0xF6, (byte)0x00, (byte)0x56, (byte)0xE5, (byte)0x96, (byte)0x7A, (byte)0xCB, (byte)0x99, (byte)0xBB, (byte)0x21, (byte)0xDD, (byte)0xF3, (byte)0xAF, (byte)0x07, (byte)0xC2, (byte)0x48,
                (byte)0xDF, (byte)0xC2, (byte)0x82, (byte)0x2F, (byte)0x3C, (byte)0x38, (byte)0x36, (byte)0x74, (byte)0x0C, (byte)0x18, (byte)0xE1, (byte)0x7F, (byte)0x4F, (byte)0xBA, (byte)0x15, (byte)0x48,
                (byte)0x14, (byte)0x29, (byte)0xB8, (byte)0x85, (byte)0x94, (byte)0xE2, (byte)0xCF, (byte)0x03, (byte)0x47, (byte)0x8F, (byte)0x86, (byte)0xDA, (byte)0x6C, (byte)0x74, (byte)0x31, (byte)0x99,
        };

        // EFTEX TEST public sponsor key, length = 0xF0h = 240 bytes = 1920 bits
       byte[] pkSpEftexTest = new byte[] {
                (byte)0xD5, (byte)0x13, (byte)0x7E, (byte)0x3A, (byte)0x52, (byte)0xCE, (byte)0x72, (byte)0xCD, (byte)0xF5, (byte)0xB5, (byte)0x7D, (byte)0x1F, (byte)0xC0, (byte)0xFE, (byte)0xD9, (byte)0x7C,
                (byte)0xD0, (byte)0x3A, (byte)0x76, (byte)0x41, (byte)0x38, (byte)0xB8, (byte)0x9A, (byte)0xD1, (byte)0x63, (byte)0x7B, (byte)0xD2, (byte)0xB6, (byte)0x88, (byte)0x35, (byte)0x0C, (byte)0x12,
                (byte)0x6A, (byte)0x43, (byte)0xF0, (byte)0x3A, (byte)0x5C, (byte)0x8F, (byte)0x40, (byte)0x7D, (byte)0xEA, (byte)0xA7, (byte)0x38, (byte)0x87, (byte)0x17, (byte)0x60, (byte)0xAB, (byte)0x1F,
                (byte)0xF2, (byte)0x72, (byte)0xCB, (byte)0x2A, (byte)0xD8, (byte)0x01, (byte)0xFC, (byte)0x8F, (byte)0xBF, (byte)0xA3, (byte)0x07, (byte)0x49, (byte)0x72, (byte)0x94, (byte)0xA0, (byte)0x42,
                (byte)0xCA, (byte)0x7B, (byte)0x43, (byte)0x8B, (byte)0x48, (byte)0x02, (byte)0x45, (byte)0x07, (byte)0xF2, (byte)0x24, (byte)0x47, (byte)0xDE, (byte)0x54, (byte)0xE0, (byte)0x27, (byte)0x92,
                (byte)0xC8, (byte)0x4A, (byte)0xA1, (byte)0x0D, (byte)0xDB, (byte)0x32, (byte)0xAA, (byte)0xFB, (byte)0x63, (byte)0x27, (byte)0x0B, (byte)0xE6, (byte)0x89, (byte)0x4A, (byte)0x12, (byte)0x8E,
                (byte)0xEE, (byte)0x2B, (byte)0x2A, (byte)0xF4, (byte)0x38, (byte)0xD7, (byte)0x40, (byte)0xA4, (byte)0xA2, (byte)0x8D, (byte)0x56, (byte)0x27, (byte)0xC9, (byte)0x1D, (byte)0xB0, (byte)0x2B,
                (byte)0x6B, (byte)0x38, (byte)0x25, (byte)0x27, (byte)0xE9, (byte)0xC2, (byte)0xE7, (byte)0x6F, (byte)0x21, (byte)0x6A, (byte)0xAD, (byte)0x23, (byte)0x2C, (byte)0xB0, (byte)0xCB, (byte)0xA0,
                (byte)0x89, (byte)0x10, (byte)0xA6, (byte)0x84, (byte)0xEB, (byte)0x53, (byte)0x38, (byte)0x71, (byte)0x85, (byte)0x65, (byte)0x27, (byte)0xE1, (byte)0x35, (byte)0x9F, (byte)0xD7, (byte)0x5C,
                (byte)0x7C, (byte)0x80, (byte)0x80, (byte)0x5C, (byte)0xCF, (byte)0xEB, (byte)0x89, (byte)0xE8, (byte)0x61, (byte)0x02, (byte)0x14, (byte)0x6D, (byte)0x36, (byte)0x08, (byte)0x6F, (byte)0x66,
                (byte)0x20, (byte)0x1A, (byte)0xC9, (byte)0x07, (byte)0x08, (byte)0x0E, (byte)0x44, (byte)0x03, (byte)0x11, (byte)0x78, (byte)0x19, (byte)0xD4, (byte)0xFF, (byte)0x93, (byte)0xAD, (byte)0x4E,
                (byte)0x24, (byte)0x03, (byte)0x9F, (byte)0xE5, (byte)0x07, (byte)0xA5, (byte)0x56, (byte)0x82, (byte)0x09, (byte)0x2E, (byte)0xFD, (byte)0x24, (byte)0x50, (byte)0x52, (byte)0x25, (byte)0x78,
                (byte)0x54, (byte)0xF9, (byte)0xF8, (byte)0x49, (byte)0xC2, (byte)0xD6, (byte)0x42, (byte)0xDB, (byte)0xBE, (byte)0x67, (byte)0x44, (byte)0xCB, (byte)0x5C, (byte)0x1B, (byte)0x70, (byte)0xC3,
                (byte)0x51, (byte)0x9A, (byte)0x13, (byte)0xBE, (byte)0xFD, (byte)0x67, (byte)0xF1, (byte)0xE5, (byte)0x05, (byte)0x52, (byte)0xF6, (byte)0x7B, (byte)0x80, (byte)0x43, (byte)0xA1, (byte)0xDA,
                (byte)0x83, (byte)0x98, (byte)0x2A, (byte)0x02, (byte)0x5E, (byte)0x60, (byte)0xAF, (byte)0x57, (byte)0x0E, (byte)0x5D, (byte)0x77, (byte)0xD4, (byte)0x33, (byte)0xA1, (byte)0x73, (byte)0x53,
        };

        // Linkly HOST EMULATOR public sponsor key, length = 0xF0h = 240 bytes = 1920 bits
        byte[] pkSpLinklyHostEmulator = new byte[] {
                (byte)0xAF, (byte)0x01, (byte)0xED, (byte)0x5A, (byte)0x56, (byte)0x6F, (byte)0xDB, (byte)0xF0, (byte)0x7E, (byte)0x74, (byte)0xD1, (byte)0xB9, (byte)0x8F, (byte)0x6A, (byte)0x91, (byte)0x14,
                (byte)0x1F, (byte)0x59, (byte)0x85, (byte)0x48, (byte)0xB0, (byte)0x3C, (byte)0x5D, (byte)0x1D, (byte)0x9A, (byte)0x3D, (byte)0x09, (byte)0xB5, (byte)0x42, (byte)0xC3, (byte)0xE6, (byte)0x3E,
                (byte)0xFA, (byte)0xD7, (byte)0xC8, (byte)0x68, (byte)0x12, (byte)0xC8, (byte)0x34, (byte)0x78, (byte)0xED, (byte)0x0E, (byte)0x26, (byte)0x94, (byte)0x5B, (byte)0x77, (byte)0x55, (byte)0xB6,
                (byte)0x13, (byte)0xC6, (byte)0x4F, (byte)0x4A, (byte)0x8D, (byte)0x5F, (byte)0x70, (byte)0x6C, (byte)0xA0, (byte)0x24, (byte)0x63, (byte)0x73, (byte)0x74, (byte)0x5C, (byte)0xEC, (byte)0x15,
                (byte)0xE8, (byte)0x9E, (byte)0x4D, (byte)0xA5, (byte)0xCE, (byte)0x1E, (byte)0xB9, (byte)0xD8, (byte)0xB8, (byte)0x98, (byte)0xC1, (byte)0x5B, (byte)0xE7, (byte)0x93, (byte)0x58, (byte)0x85,
                (byte)0xC5, (byte)0xE6, (byte)0xDE, (byte)0x48, (byte)0xB8, (byte)0x9F, (byte)0xB7, (byte)0xF3, (byte)0x6B, (byte)0xEE, (byte)0xE4, (byte)0x5D, (byte)0xCE, (byte)0x91, (byte)0x87, (byte)0x92,
                (byte)0xFE, (byte)0x20, (byte)0x6F, (byte)0xFD, (byte)0xA2, (byte)0x86, (byte)0x27, (byte)0x92, (byte)0xEE, (byte)0xA9, (byte)0x61, (byte)0x86, (byte)0xF8, (byte)0xDE, (byte)0xF5, (byte)0xDD,
                (byte)0x0A, (byte)0xB0, (byte)0x4F, (byte)0x38, (byte)0x36, (byte)0x93, (byte)0x0C, (byte)0xEB, (byte)0x6A, (byte)0xF7, (byte)0x7D, (byte)0x01, (byte)0x73, (byte)0xF0, (byte)0x2E, (byte)0x40,
                (byte)0x5F, (byte)0xAD, (byte)0x85, (byte)0x83, (byte)0xF1, (byte)0x90, (byte)0xF9, (byte)0xA4, (byte)0x98, (byte)0x67, (byte)0x0D, (byte)0x0D, (byte)0x8D, (byte)0xD3, (byte)0x8F, (byte)0xF2,
                (byte)0xD9, (byte)0xC6, (byte)0xBB, (byte)0x9B, (byte)0x56, (byte)0x7E, (byte)0x9A, (byte)0xFF, (byte)0x8C, (byte)0x91, (byte)0x1C, (byte)0x38, (byte)0xE0, (byte)0x46, (byte)0x91, (byte)0xEC,
                (byte)0x98, (byte)0x9F, (byte)0x56, (byte)0xEA, (byte)0x31, (byte)0x91, (byte)0xF2, (byte)0x46, (byte)0x1C, (byte)0x44, (byte)0xB6, (byte)0x95, (byte)0x0B, (byte)0x0F, (byte)0x55, (byte)0x2A,
                (byte)0x87, (byte)0x8F, (byte)0xA0, (byte)0xAE, (byte)0x8E, (byte)0xAD, (byte)0x14, (byte)0x92, (byte)0x9A, (byte)0xF4, (byte)0xB5, (byte)0xAE, (byte)0x6D, (byte)0x1B, (byte)0x46, (byte)0xEE,
                (byte)0x58, (byte)0xA7, (byte)0xD9, (byte)0xE7, (byte)0xF3, (byte)0xCF, (byte)0x8C, (byte)0x45, (byte)0xAB, (byte)0xBD, (byte)0x7D, (byte)0x5D, (byte)0x6D, (byte)0xE1, (byte)0x31, (byte)0x92,
                (byte)0xC8, (byte)0xFB, (byte)0x61, (byte)0x0A, (byte)0xF9, (byte)0x3B, (byte)0xC5, (byte)0xB9, (byte)0x8A, (byte)0xFB, (byte)0x40, (byte)0xBB, (byte)0x09, (byte)0x78, (byte)0x5F, (byte)0xE7,
                (byte)0xBA, (byte)0xE6, (byte)0x4D, (byte)0xC8, (byte)0x76, (byte)0xF7, (byte)0x34, (byte)0xB9, (byte)0x4D, (byte)0xA8, (byte)0x35, (byte)0xC9, (byte)0x96, (byte)0xA1, (byte)0xC7, (byte)0xC9,
        };
        byte[] pkSpExpExpected = new byte[] { 0x01, 0x00, 0x01 };

        boolean pkSpMatch = false;

        // cycle through allowed pkSp keys to find a match
        if( Arrays.equals(pkSpEftexProd, pkSpMod) && Arrays.equals( pkSpExpExpected, pkSpExp ) ) {
            pkSpMatch = true;
            Timber.i( "Matched EFTEX prod PkSp" );
        }

        // these keys are allowed in non-prod builds only.
        // Using BUILD_TYPE_PRODUCTION, which is false for debug and release, and true for production
        if( !pkSpMatch && !BuildConfig.BUILD_TYPE_PRODUCTION) {
            if (Arrays.equals(pkSpEftexTest, pkSpMod) && Arrays.equals(pkSpExpExpected, pkSpExp)) {
                pkSpMatch = true;
                Timber.i("Matched EFTEX test PkSp");
            }

            if (!pkSpMatch && Arrays.equals(pkSpLinklyHostEmulator, pkSpMod) && Arrays.equals(pkSpExpExpected, pkSpExp)) {
                pkSpMatch = true;
                Timber.i("Matched Linkly Host Emulator PkSp");
            }
        }

        if( !pkSpMatch ) {
            Timber.e( "PkSp - no match - failing RKI process" );
            throw new IllegalArgumentException();
        }

        // copy random number
        randomNumber = new byte[rnSp.length];
        System.arraycopy( rnSp, 0, randomNumber, 0, rnSp.length );
        Timber.i( "Random number = %s", Util.hex2Str(randomNumber));

        // inject sponsor key
        P2PLib.getInstance().getIP2PSec().as2805InjectPkSponsor(pkSpMod, pkSpExp);

    }

    private static void unpackRsaKeyInitPart2(TransRec trans, String keyData) {
        byte[] respPayload = Util.hexStringToByteArray(keyData);

        // response payload is:
        // eKI(KCA) b16
        // AIIC n..11, LLVAR
        byte[] eKIv44Kca = Arrays.copyOfRange(respPayload, 0, 16);
        byte[] eKIv24Kmach = new byte[16];

        // unpack AIIC, numeric string with 2 length bytes
        byte[] aiicLenBytes = new byte[2];
        System.arraycopy(respPayload, 16, aiicLenBytes, 0, 2);
        String aiicLenStr = new String(aiicLenBytes);
        int aiicLenInt = Integer.parseInt(aiicLenStr);

        if( aiicLenInt > 0 && aiicLenInt <= 16 ) {
            byte[] aiicBytes = new byte[aiicLenInt];
            System.arraycopy(respPayload, 18, aiicBytes, 0, aiicLenInt );
            // convert to string
            String aiicStr = new String(aiicBytes);
            // save to env var
            Aiic.setNewValue(aiicStr);
            Timber.i( "Saved AIIC value [%s]", aiicStr );
        } else {
            Timber.e( "ERROR, invalid AIIC length %d", aiicLenInt );
            throw new IllegalArgumentException();
        }

        // load KCA
        if( !P2PLib.getInstance().getIP2PSec().as2805LoadKcaKmach(eKIv44Kca, eKIv24Kmach, packAiicBlock() ) ) {
            Timber.e( "rsa key init part 2 load kcaKmach failure" );
            throw new IllegalArgumentException();
        }

    }

    private static void unpackRsaKeyInitPart3(TransRec trans, String keyData) {
        byte[] respPayload = Util.hexStringToByteArray(keyData);

        // unpack part 3 payload
        // eKIA(KEK1) b16
        // eKIA(KEK2) b16
        // eKIA(PPASN) b16
        // MAC of eKIA(KEK1)||eKIA(KEK2)||eKIA(PPASN) using acquirers KMACI b4
        // KVC(KEK1) b3
        // KVC(KEK2) b3
        byte[] eKiaKek1eKiaKek2eKiav88Ppasn = Arrays.copyOfRange(respPayload, 0, 40);
//        byte[] mac = Arrays.copyOfRange(respPayload, 40, 44); // not used
        byte[] kek1Kvc = Arrays.copyOfRange(respPayload, 44, 47);
        byte[] kek2Kvc = Arrays.copyOfRange(respPayload, 47, 50);
        byte[] ppasnKvc = new byte[3]; // null fill, doesn't matter

        if( !P2PLib.getInstance().getIP2PSec().as2805LoadKek1Kek2Ppasn(eKiaKek1eKiaKek2eKiav88Ppasn, kek1Kvc, kek2Kvc, ppasnKvc, packAiicBlock()) ) {
            Timber.e( "rsa key init part 3 load keks failure" );
            throw new IllegalArgumentException();
        }
    }

    private static void unpackSessionKeyExchange(TransRec trans, String keyData) {
        byte[] respPayload = Util.hexStringToByteArray(keyData);

        // unpack part 4 payload
        // KVC(KEK1)
        // eKEK1(KMACs)
        // eKEK1(KMACr)
        // eKEK1(KDs)
        // eKEK1(KDr)
        // eKEK1(KPP)
        byte[] eKek1Block = new byte[80];
        byte[] kvcKek1 = new byte[3];
        System.arraycopy( respPayload, 3, eKek1Block, 0, 80 );
        System.arraycopy( respPayload, 0, kvcKek1, 0, 3 );

        if( !P2PLib.getInstance().getIP2PSec().as2805LoadSessionKeysEftexStyle('1', eKek1Block, kvcKek1 ) ) {
            Timber.e( "rsa key init part 3 load session keys failure" );
            throw new IllegalArgumentException();
        }
    }

    // get expiry or other 'non sensitive' card data
    public static String getNonSensitiveElement(IP2PEncrypt.ElementType element) {
        int dataLen = P2PLib.getInstance().getIP2PEncrypt().getElementLength(element);
        if (dataLen <= 0) {
            Timber.i("WARNING - addSensitiveElement element " + element.name() + " not found, skipping");
            return null;
        }

        return P2PLib.getInstance().getIP2PEncrypt().getData(element);
    }

    private static int findByteInArray(byte[] dataToSearch, int searchFromIdx, byte byteToFind) {
        for (int idx = searchFromIdx; idx < dataToSearch.length; idx++) {
            if (dataToSearch[idx] == byteToFind) {
                return idx;
            }
        }

        return -1;
    }

    // get element length 'n', and look for a run of n x byteToFind bytes in dataToSearch, starting at searchFromIdx
    // returns new CardholderDataElement if found
    private static CardholderDataElement findElementPlaceholder(byte[] dataToSearch, int searchFromIdx, IP2PEncrypt.ElementType elementType, char charToFind) {
        int elementLength = P2PLib.getInstance().getIP2PEncrypt().getElementLength(elementType);

        byte[] packedMsg;
        packedMsg = dataToSearch;

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
                    if (IP2PEncrypt.PackFormat.ASCII == BCD) {
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

        // const value really - skip B prefix char and MTI (4 chars) and 8 bytes of bitmap as this can contain binary data and could get a false match
        final int REPLACE_FROM_OFFSET = 13;

        // scan message for fields
        CardholderDataElement cvvElement = findElementPlaceholder(msg, REPLACE_FROM_OFFSET, CVV, SUBST_VAL_CVV);
        CardholderDataElement track2MsrElement = findElementPlaceholder(msg, REPLACE_FROM_OFFSET, TRACK_2_FULL_MSR, SUBST_VAL_TRACK_2_MSR);
        CardholderDataElement panElement = findElementPlaceholder(msg, REPLACE_FROM_OFFSET, PAN, SUBST_VAL_TRACK_2_PAN);
        CardholderDataElement track2ShortElement = findElementPlaceholder(msg, REPLACE_FROM_OFFSET, TRACK_2_SHORT_FORMAT, SUBST_VAL_TRACK_2_SHORT);
        CardholderDataElement expiryElement = findElementPlaceholder(msg, REPLACE_FROM_OFFSET, EXPIRY_YYMM, SUBST_VAL_EXPIRY);

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
            elements[elementNo++] = expiryElement;

        return elements;
    }

    private static byte[] encryptFullMessage(byte[] msgPacked, As2805Eftex msg, boolean disableEncryption, CardholderDataElement[] secureElements) throws Exception {
        // hex dump packed msg
        Timber.e( "packed msg: %s", HexDump.dumpHexString(msgPacked) );

        for( CardholderDataElement element : secureElements ) {
            Timber.e( "element name %s, offset %d, length %d", element.getElementType().name(), element.getSubstitueIndex(), element.getLength() );
        }

        // get STAN
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
            params = new IP2PEncrypt.EncryptParameters( IP2PEncrypt.PaddingAlgorithm.FF_BYTES, AS2805_3DES_OFB, 0, stanInt );
        }

        // do the encryption
        EncryptResult encResult = P2PLib.getInstance().getIP2PEncrypt().encrypt(msgPacked, params, secureElements);
        if (encResult != null) {
            return encResult.getEncryptedMessage();
        } else {
            Timber.e( "error encrypting message" );
            return null;
        }
    }

    public static byte[] addMacAndEncrypt(boolean securityDisabled, As2805Eftex msg) throws Exception {
        if( securityDisabled ) {
            return msg.toMsg();
        }

        // pack to buffer, skip 'B' encoding char
        byte[] msgPacked = msg.toMsg( true );

        // sets elements array for mac operation
        CardholderDataElement[] secureElements = getSecureElements(msgPacked);

        // first do the mac
        IP2PEncrypt.MacParameters macParameters = new IP2PEncrypt.MacParameters(IP2PEncrypt.MacAlgorithm.AS2805, 0, "");

        // append a dummy mac to the current message, so the bitmap has correct value
        byte[] dummyMac = new byte[8];
        msg.set(msg.isExtended() ? _128_MAC : _064_MAC, hex2Str(dummyMac));
        // and pack again
        msgPacked = msg.toMsg( true );

        // chop off the final 8 bytes (dummy mac) to be macced
        byte[] msgToMac = new byte[msgPacked.length - 8];
        System.arraycopy(msgPacked, 0, msgToMac, 0, msgPacked.length - 8);

        // generate the mac
        byte[] mac = P2PLib.getInstance().getIP2PEncrypt().getMac(msgToMac, macParameters, secureElements);

        // append actual calculated mac
        msg.set(msg.isExtended() ? _128_MAC : _064_MAC, hex2Str(mac));

        Timber.e("message to send");
        Timber.e(msg.toString());

        // pack again to get msg incl correct mac
        msgPacked = msg.toMsg( true );

        // now do full message encryption.
        byte[] msgEncrypted = encryptFullMessage(msgPacked, msg, false, secureElements);
        if( msgEncrypted == null ) {
            Timber.e( "message encrypt error" );
            return null;
        }

        return wrapMsgInHeader( msgEncrypted, msg );
    }

    private static byte[] wrapMsgInHeader(byte[] inputMsg, As2805Eftex msg) {
        try {
            // wrap header pre-pended. 25 bytes plus B character (its not in encrypted part)
            byte[] wrappedMsg = new byte[inputMsg.length + TERMAPP_ENCRYPTED_HEADER_OVERHEAD + 1];

            // 'T'
            ByteArrayOutputStream builder = new ByteArrayOutputStream();
            builder.write('T');

            // df02 tag, length 06
            builder.write(Util.hexStringToByteArray("DF0206"));

            // append stan as ascii string, 6 bytes
            String stanStr = msg.get(_011_SYS_TRACE_AUDIT_NUM);
            if (stanStr == null) {
                Timber.e("STAN error - packing code didn't pack STAN");
                return null;
            }
            builder.write(stanStr.getBytes());

            // df01 tag, length 08
            builder.write(Util.hexStringToByteArray("DF0108"));

            String tid = msg.get(_041_TERMINAL_ID);
            if (tid == null) {
                Timber.e("TID error, TID not packed");
                return null;
            }
            builder.write(tid.getBytes());

            // DF00 tag, len 1, value '1' (full encryption)
            builder.write(Util.hexStringToByteArray("DF000131"));

            // 'B' byte isn't encrypted, tack it on the front
            builder.write( 'B' );

            // build header
            byte[] header = builder.toByteArray();

            // 25 byte header plus B byte, should be 26 bytes
            System.arraycopy(header, 0, wrappedMsg, 0, header.length);
            // append encrypted msg
            System.arraycopy(inputMsg, 0, wrappedMsg, header.length, inputMsg.length);

            return wrappedMsg;
        } catch( Exception e ) {
            Timber.w(e);
            return null;
        }
    }

    private static boolean packCardData(As2805Eftex msg, TransRec trans, boolean useSavedCardDetails) throws UnknownFieldException {
        TCard cardinfo = trans.getCard();

        IP2PEncrypt p2pEncrypt = P2PLib.getInstance().getIP2PEncrypt();

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
                msg.set(_002_PAN, packSensitiveField(PAN, SUBST_VAL_TRACK_2_PAN));
                msg.set(_014_EXPIRY_DATE, packSensitiveField(EXPIRY_YYMM, SUBST_VAL_EXPIRY));
            }
        } else if (p2pEncrypt.getElementLength(TRACK_2_FULL_MSR) > 0) {
            // else it's msr/emv/ctls
            msg.set(_035_TRACK_2_DATA, packSensitiveField(TRACK_2_FULL_MSR, SUBST_VAL_TRACK_2_MSR));
        } else {
            // else we don't have required card data, return error
            Timber.e("error - missing required card data, use saved = " + useSavedCardDetails + ", track2Element = " + TRACK_2_FULL_MSR.toString());
            returnFlag = false;
        }

        return returnFlag;
    }

    private static As2805Eftex packCommon1200Fields(TransRec trans) throws FormatException {
        try {
            TProtocol proto = trans.getProtocol();
            TAmounts amounts = trans.getAmounts();
            TAudit auditinfo = trans.getAudit();
            As2805Eftex msg = new As2805Eftex();

            if (trans.isPreAuth()) {
                proto.setOriginalMessageType(1100);
                msg.setMsgType(_1100_AUTH_REQ);
            } else {
                proto.setOriginalMessageType(1200);
                msg.setMsgType(_1200_TRAN_REQ);
            }

            proto.setOriginalStan(proto.getStan());

            msg.putProcessingCode(packProcCode(trans));
            msg.setTransactionAmount(amounts.getTotalAmount());

            // set DE7 and record transmission time on transaction record in case it's required for later reversal
            trans.getAudit().setLastTransmissionDateTime(msg.getTransmissionDateTime().setNow()); // sets _007_TRAN_DATE_TIME
            proto.setOriginalTransmissionDateTime(trans.getAudit().getLastTransmissionDateTime());

            msg.set(_011_SYS_TRACE_AUDIT_NUM, packStan(proto.getStan()));
            msg.set(_012_LOCAL_TRAN_DATETIME, packLocalDateTimeDe12(trans));
            msg.set(_022_POS_DATA_CODE, packPosDataCode(trans));
            msg.set(_023_CARD_SEQ_NR, packCardSeqNumber(trans));
            msg.set(_024_FUNC_CODE, "100"); // Original authorization, amount accurate. Used for all authorizations.
            msg.set(_029_RECON_INDICATOR, packReconIndicator(proto.getBatchNumber()));
            msg.set(_037_RET_REF_NR, packRetRefNumber(trans));

            // 35 is filled by secure card method
            msg.set(_041_TERMINAL_ID, auditinfo.getTerminalId());
            msg.set(_042_CARD_ACCEPTOR_ID, packMerchantId(auditinfo.getMerchantId()));
            msg.set(_049_TRAN_CURRENCY_CODE, "036");

            // only pack DE52 if online PIN cvm
            if( trans.getCard().getCvmType().isOnlinePin() ) {
                String pinblock = getNonSensitiveElement(PIN_BLOCK);
                if (pinblock != null) {
                    msg.set(_052_PIN_DATA, pinblock);
                } else {
                    Timber.e("No pin block, but should be. Sending msg without DE52");
                }
            }

            // As mentioned in packAdditionalAmounts tipping amount is being included unless told otherwise by eftex,
            // Delete this comment along with the one in packAdditionalAmounts
            if (trans.getAmounts().getTip() > 0 || trans.getAmounts().getCashbackAmount() > 0) {
                msg.set(_054_ADDITIONAL_AMOUNTS, packAdditionalAmounts(trans));
            }
            return msg;
        } catch (Exception e) {
            throw new FormatException("common auth pack error");
        }
    }

    private static byte[] packAuth(IDependency d, TransRec trans) throws FormatException {
        try {
            // pack common format fields here, and specific ones below
            As2805Eftex msg = packCommon1200Fields(trans);

            // PAN is always current
            if (!packCardData(msg, trans, false)) {
                return new byte[0];
            }

            // pack DE55 if not refund, and ICC or CTLS card type
            if (!trans.isRefund() && (trans.getCard().isIccCaptured() || trans.getCard().isCtlsCaptured())) {
                msg.set(_055_ICC_DATA, packIccDataInFF20Container(trans, msg));
            }
            // pack de48, which is a constructed field containing sub-fields
            String iccDataForDe48 = packIccDataForDe48(trans, msg);
            msg.putAdditionalData(packAdditionalData(d, trans, iccDataForDe48));

            return addMacAndEncrypt(isSecurityDisabled(d), msg);
        } catch (Exception e) {
            throw new FormatException("auth pack error");
        }
    }

    private static byte[] packDeferredAuth(IDependency d, TransRec trans) throws FormatException {

        IP2PEncrypt p2pEncrypt = P2PLib.getInstance().getIP2PEncrypt();
        try {
            // pack common format fields here, and specific ones below
            As2805Eftex msg = packCommon1200Fields(trans);
            Timber.i("Stashing card data - deferred auth");
            // stashing here instructs secApp to put any current transaction card data into a temporary 'stash' buffer
            // this is used in cases where a transaction is in progress, and an inline batch upload needs to happen
            p2pEncrypt.stash();

            // PAN is always stored PAN
            if (!packCardData(msg, trans, true)) {
                return new byte[0];
            }

            // deferred auths pack ICC data the 'advice way'
            packDe55AdviceStyle(msg, trans);
            packDe48AdviceStyle(d, msg, trans);

            return addMacAndEncrypt(isSecurityDisabled(d), msg);
        } catch (Exception e) {
            throw new FormatException("deferred auth pack error");
        } finally {
            Timber.i("Unstashing card data - deferred auth");
            // load current trans card data back into secApp card buffers
            p2pEncrypt.unstash();
        }
    }

    private static byte[] packAdvice(IDependency d, TransRec trans) {

        IP2PEncrypt p2pEncrypt = P2PLib.getInstance().getIP2PEncrypt();
        try {
            Timber.i("stashing card data");
            p2pEncrypt.stash();

            TProtocol proto = trans.getProtocol();
            TAmounts amounts = trans.getAmounts();
            TCard card = trans.getCard();
            As2805Eftex msg = new As2805Eftex();

            proto.setOriginalMessageType(1220);
            proto.setOriginalStan(proto.getStan());
            proto.setOriginalTransmissionDateTime(trans.getAudit().getTransDateTime());

            // eftex always use 1220, never 1221
            msg.setMsgType(Iso8583Rev93.MsgType._1220_TRAN_ADV);

            if (!packCardData(msg, trans, true)) {
                Timber.e("ERROR PACKING CARD DATA for ADVICE");
                return null;
            }

            msg.putProcessingCode(packProcCode(trans));
            msg.setTransactionAmount(amounts.getTotalAmount());
            msg.getTransmissionDateTime().setNow(); // packs _007_TRAN_DATE_TIME with time now. no need to save this

            msg.set(_011_SYS_TRACE_AUDIT_NUM, packStan(proto.getStan()));
            msg.set(_012_LOCAL_TRAN_DATETIME, packLocalDateTimeDe12(trans));
            msg.set(_022_POS_DATA_CODE, packPosDataCode(trans));
            // only pack de23 if manual PAN entry
            if( card.isManual() ) {
                msg.set(_023_CARD_SEQ_NR, packCardSeqNumber(trans));
            }
            msg.set(_024_FUNC_CODE, "200"); // indicates advice
            msg.set(_029_RECON_INDICATOR, packReconIndicator(proto.getBatchNumber()));
            msg.set(_037_RET_REF_NR, packRetRefNumber(trans));
            msg.set(_038_APPROVAL_CODE, packAuthCode(trans));
            msg.set(_039_ACTION_CODE, packActionCode(trans, false));
            msg.set(_041_TERMINAL_ID, d.getPayCfg().getStid());
            msg.set(_042_CARD_ACCEPTOR_ID, packMerchantId(d.getPayCfg().getMid()));
            msg.set(_049_TRAN_CURRENCY_CODE, "036");

            if (trans.getAmounts().getTip() > 0 || trans.getAmounts().getCashbackAmount() > 0) {
                msg.set(_054_ADDITIONAL_AMOUNTS, packAdditionalAmounts(trans));
            }

            packDe55AdviceStyle(msg, trans);
            packDe48AdviceStyle(d, msg, trans);

            return addMacAndEncrypt(isSecurityDisabled(d), msg);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            Timber.i("unstashing card data");
            p2pEncrypt.unstash();
        }
    }

    private static byte[] packReversalImpl(IDependency d, TransRec trans, int stan) {

        As2805Eftex msg = new As2805Eftex();
        TProtocol proto = trans.getProtocol();
        TAmounts amounts = trans.getAmounts();
        TCard card = trans.getCard();
        PaymentSwitch paySwitchCfg = d.getPayCfg().getPaymentSwitch();
        IP2PEncrypt p2pEncrypt = P2PLib.getInstance().getIP2PEncrypt();

        // For acquirers that need to have the original STAN (so RRN as well) as the transaction to be reversed
        if (Engine.getDep().getPayCfg().isIncludedOrginalStandInRec() || Engine.getDep().getPayCfg().isReversalCopyOriginal()) {
            stan = trans.getProtocol().getOriginalStan();
        }

        try {
            Timber.i("stashing card data");
            p2pEncrypt.stash();

            msg.setMsgType(_1420_TRAN_REV_ADV);
            if (proto.getAdviceAttempts() > 0) {
                // TODO: do we need to do anything to signify repeat?
            } else {
            }

            if (!packCardData(msg, trans, true)) {
                Timber.e("Card Data of Transaction [%s] couldn't be packed", trans.getAudit().getReference());
                return null;
            }

            msg.putProcessingCode(packProcCode(trans));
            msg.setTransactionAmount(amounts.getTotalAmount());
            msg.getTransmissionDateTime().setNow(); // packs _007_TRAN_DATE_TIME with time now. no need to save this

            msg.set(_011_SYS_TRACE_AUDIT_NUM, packStan(stan));
            msg.set(_012_LOCAL_TRAN_DATETIME, packLocalDateTimeDe12(trans));
            msg.set(_022_POS_DATA_CODE, packPosDataCode(trans));
            // only pack de23 if manual PAN entry
            if( card.isManual() ) {
                msg.set(_023_CARD_SEQ_NR, packCardSeqNumber(trans));
            }
            msg.set(_024_FUNC_CODE, "400"); // Original authorization, amount accurate. Used for all authorizations.
            msg.set(_029_RECON_INDICATOR, packReconIndicator(proto.getBatchNumber()));
            msg.set(_037_RET_REF_NR, packRetRefNumber(trans));
            msg.set(_039_ACTION_CODE, packActionCode(trans, true));
            msg.set(_041_TERMINAL_ID, d.getPayCfg().getStid());
            msg.set(_042_CARD_ACCEPTOR_ID, packMerchantId(d.getPayCfg().getMid()));
            msg.set(_049_TRAN_CURRENCY_CODE, "036");

            if (trans.getAmounts().getTip() > 0 || trans.getAmounts().getCashbackAmount() > 0) {
                msg.set(_054_ADDITIONAL_AMOUNTS, packAdditionalAmounts(trans));
            }

            packDe55AdviceStyle(msg, trans);
            packDe48AdviceStyle(d, msg, trans);

            msg.set(_056_ORIG_DATA_ELEMENTS, packOriginalDataElements(trans, paySwitchCfg) );
            return addMacAndEncrypt(isSecurityDisabled(d), msg);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
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
            throw new RuntimeException(e);
        }
    }

    /**
     * Packs F48 field for Network message type
     *
     * @return f48 contains ASCII Hex message
     */

    private static String packRsaKeyInitPart1(IDependency d) throws IOException {
        // generate first part of rsa key init
        // de96 payload is as follows:
        // fixed 8 bytes PPID
        // LLLLvar skMan mod and exp, 512 bytes
        // LLLLvar pktcu exponent

        byte[] skManPkTcu = P2PLib.getInstance().getIP2PSec().as2805GetSkManPkTcu();

        // convert skManPkTcu length from int to BCD. first to string, then string->bcd
        String skManPkTcuLenStr = String.format(Locale.getDefault(), "%04d", skManPkTcu.length );
        byte[] skManPkTcuLen = Util.str2Bcd(skManPkTcuLenStr);

        byte[] pkTcuExpWithLength = new byte[] { 0x00, 0x03, 0x01, 0x00, 0x01 };

        byte[] ppid = P2PLib.getInstance().getIP2PSec().as2805GetPpid();

        int lengthOfPayload = ppid.length + skManPkTcuLen.length + skManPkTcu.length + pkTcuExpWithLength.length;
        byte[] payload = new byte[lengthOfPayload];

        System.arraycopy( ppid, 0, payload, 0, ppid.length );
        System.arraycopy( skManPkTcuLen, 0, payload, ppid.length, skManPkTcuLen.length );
        System.arraycopy( skManPkTcu, 0, payload, ppid.length+skManPkTcuLen.length, skManPkTcu.length );
        System.arraycopy( pkTcuExpWithLength, 0, payload, ppid.length+skManPkTcuLen.length+skManPkTcu.length, pkTcuExpWithLength.length );

        return Util.byteArrayToHexString(payload);
    }

    private static String packRsaKeyInitPart2(IDependency d) throws IOException {
        // this is the hard-coded RN that the emulator returns
        // generate part 2 payload
        // 8 byte tcuid
        // LLLLvar sSKtcu(Pksp(KI, TCUID, DTS, RNsp))

        byte[] skTcuEncBlock = P2PLib.getInstance().getIP2PSec().as2805GenerateSkTcuKiBlock(randomNumber);
        Timber.i( "skTcuEncBlock = %s", skTcuEncBlock == null ? "null" : Util.hex2Str(skTcuEncBlock));
        byte[] ppid = P2PLib.getInstance().getIP2PSec().as2805GetPpid();
        Timber.i( "ppid = %s", ppid == null ? "null" : Util.hex2Str(ppid));



        ByteArrayOutputStream payload = new ByteArrayOutputStream();

        try {
            // ppid
            payload.write(ppid);

            // length of skTcu block
            byte[] skTcuBlockLen = new byte[] { 0x02, 0x48 };

            payload.write(skTcuBlockLen);

            // skTcu(DFormat1(pkSp(DFormat1(KI, PPID, DTS, RN))))
            payload.write(skTcuEncBlock);

        } catch( Exception e ) {
            Timber.w(e);
        }

        return Util.byteArrayToHexString(payload.toByteArray());
    }

    private static String packRsaKeyInitPart3(IDependency d) throws IOException {

        // part 3 request payload
        // tcuid b8
        // KVC(KIA)

        byte[] ppid = P2PLib.getInstance().getIP2PSec().as2805GetPpid();
        byte[] kcvKia = P2PLib.getInstance().getIP2PSec().as2805GetKcvKia();

        byte[] payload = new byte[11];
        System.arraycopy( ppid, 0, payload, 0, ppid.length );
        System.arraycopy( kcvKia, 0, payload, ppid.length, 3 );

        return Util.byteArrayToHexString(payload);
    }

    private static String packSessionKeyExchange(IDependency d) throws IOException {
        // part 4 session key exchange
        // tcuid b8
        // KEK flag n1

        byte[] ppid = P2PLib.getInstance().getIP2PSec().as2805GetPpid();
        byte[] kekFlag = "1".getBytes();

        byte[] payload = new byte[9];
        System.arraycopy( ppid, 0, payload, 0, ppid.length );
        System.arraycopy( kekFlag, 0, payload, ppid.length, 1 );

        return Util.byteArrayToHexString(payload);
    }

    private static String pack101F48(IDependency d) throws IOException {
        byte[] ppid = P2PLib.getInstance().getIP2PSec().as2805GetPpid();

        ByteArrayOutputStream payload = new ByteArrayOutputStream();

        // todo: implement file download properly
        // pinpad id
        payload.write(ppid);
        // key management version 03
        payload.write(0x03);
        payload.write(P2PLib.getInstance().getIP2PSec().as2805GeteKekPpasn(1));
        payload.write(P2PLib.getInstance().getIP2PSec().as2805GeteKekPpasn(2));

        Timber.i("de48 nmic 101 request payload length = %d", payload.toByteArray().length);

        return Util.byteArrayToHexString(payload.toByteArray());
    }

    private static byte[] packNetwork(IDependency d, TransRec trans, String funcCode) {

        try {

            TProtocol proto = trans.getProtocol();
            TAudit auditInfo = trans.getAudit();

            As2805Eftex msg = new As2805Eftex();

            proto.setOriginalMessageType(804);
            proto.setOriginalTransmissionDateTime(trans.getAudit().getTransDateTime());
            trans.setSoftwareVersion(d.getPayCfg().getPaymentAppVersion());

            msg.setMsgType(_1804_NWRK_MNG_REQ);
            msg.getTransmissionDateTime().setNow(); // packs _007_TRAN_DATE_TIME with time now. no need to save this
            msg.set(_011_SYS_TRACE_AUDIT_NUM, packStan(proto.getStan()));
            msg.set(_012_LOCAL_TRAN_DATETIME, packLocalDateTimeDe12(trans));
            msg.set(_024_FUNC_CODE, funcCode);
            msg.set(_041_TERMINAL_ID, auditInfo.getTerminalId());
            msg.set(_042_CARD_ACCEPTOR_ID, packMerchantId(auditInfo.getMerchantId()));

            switch( funcCode ) {
                case RKI_REQUEST_INITIAL:
                    msg.set(_096_KEY_MANAGEMENT_DATA, packRsaKeyInitPart1(d) );
                    break;
                case RKI_REQUEST_SECOND:
                    msg.set(_096_KEY_MANAGEMENT_DATA, packRsaKeyInitPart2(d) );
                    break;
                case RKI_REQUEST_FINAL:
                    msg.set(_096_KEY_MANAGEMENT_DATA, packRsaKeyInitPart3(d) );
                    break;
                case SESSION_KEY_EXCHANGE:
                    msg.set(_096_KEY_MANAGEMENT_DATA, packSessionKeyExchange(d) );
                    break;
                default:
                    Timber.e( "Unexpected func code %s", funcCode );
                    break;
            }

            Timber.e(msg.toString());
            return msg.toMsg();

        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    @SuppressLint("DefaultLocale")
    private static byte[] packReconciliation(IDependency d, TransRec trans) {

        try {

            TProtocol proto = trans.getProtocol();
            TAudit auditinfo = trans.getAudit();
            Reconciliation r = reconciliationDao.findByTransId(trans.getUid());
            TReconciliationFigures reconcFigures = trans.getReconciliation().getReconciliationFigures();

            trans.setReconciliation(r);

            As2805Eftex msg = new As2805Eftex();

            msg.setMsgType(_1520_RECON_ADV);
            msg.getTransmissionDateTime().setNow(); // note we dont update the transmission date time in trans record
            msg.set(_011_SYS_TRACE_AUDIT_NUM,       packStan(proto.getStan()));
            msg.set(_012_LOCAL_TRAN_DATETIME,       packLocalDateTimeDe12(trans));
            msg.set(_024_FUNC_CODE,                 "500");
            msg.set(_029_RECON_INDICATOR,           packReconIndicator(proto.getBatchNumber()));
            msg.set(_041_TERMINAL_ID,               auditinfo.getTerminalId());
            msg.set(_042_CARD_ACCEPTOR_ID,          packMerchantId(auditinfo.getMerchantId()));
            msg.set(_049_TRAN_CURRENCY_CODE,        "036");
            msg.set(_050_SETTLEMENT_CURRENCY_CODE,  "036");
            msg.set(_074_NR_CREDITS,                String.format("%010d", reconcFigures.getCreditsNumber()));
            msg.set(_075_NR_CREDITS_REVERSAL,       String.format("%010d", reconcFigures.getCreditsReversalNumber()));
            msg.set(_076_NR_DEBITS,                 String.format("%010d", reconcFigures.getDebitsNumber()));
            msg.set(_077_NR_DEBITS_REVERSAL,        String.format("%010d", reconcFigures.getDebitsReversalNumber()));
            msg.set(_081_NR_AUTHS,                  String.format("%010d", reconcFigures.getAuthorisationsNumber()));
            msg.set(_086_AMOUNT_CREDITS,            String.format("%016d", reconcFigures.getCreditsAmount()));
            msg.set(_087_AMOUNT_CREDITS_REVERSAL,   String.format("%016d", reconcFigures.getCreditsReversalAmount()));
            msg.set(_088_AMOUNT_DEBITS,             String.format("%016d", reconcFigures.getDebitsAmount()));
            msg.set(_089_AMOUNT_DEBITS_REVERSAL,    String.format("%016d", reconcFigures.getDebitsReversalAmount()));

            // amount, net reconciliation - need to add "sign" (C or D)
            long netReconcAmount = reconcFigures.getNetReconciliationAmount();
            String sign;
            if (netReconcAmount > 0) {
                // net amt positive, use 'D' to indicate net total is debit
                sign = "D"; // debit
            } else {
                // net amt is negative, or zero. switch sign on variable and set 'C' prefix to indicate credit
                sign = "C"; // credit
                netReconcAmount = -netReconcAmount;
            }
            msg.set(_097_AMOUNT_NET_RECON, String.format("%s%016d", sign, netReconcAmount));

            return addMacAndEncrypt(isSecurityDisabled(d), msg);

        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static byte[] packFileUpdate(IDependency d, TransRec trans, As2805EftexProto.FileUpdate fileUpdate) {

        try {
            TProtocol proto = trans.getProtocol();
            TAudit auditinfo = trans.getAudit();
            As2805Eftex msg = new As2805Eftex();

            msg.setMsgType(_1304_FILE_ACTION_REQ);

            msg.set(_011_SYS_TRACE_AUDIT_NUM, packStan(proto.getStan()));

            msg.set(_041_TERMINAL_ID, auditinfo.getTerminalId());
            msg.set(_042_CARD_ACCEPTOR_ID, packMerchantId(auditinfo.getMerchantId()));
            msg.set(_101_FILE_NAME, tableName);

            return addMacAndEncrypt(isSecurityDisabled(d), msg);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * Lists request-response of each message used in Eftex
     * */
    public enum MsgType {
        PREAUTH( _1100_AUTH_REQ , _1110_AUTH_REQ_RSP ),
        AUTH( _1200_TRAN_REQ , _1210_TRAN_REQ_RSP ),
        ADVICE( _1220_TRAN_ADV, _1230_TRAN_ADV_RSP ),
        DEFERRED_AUTH( _1200_TRAN_REQ , _1210_TRAN_REQ_RSP ),
        NETWORK( _1804_NWRK_MNG_REQ, _1814_NWRK_MNG_REQ_RSP ),
        REVERSAL( _1420_TRAN_REV_ADV, _1430_TRAN_REV_ADV_RSP ),
        RECONCILIATION( _1520_RECON_ADV, _1530_RECON_ADV_RSP ),
        RECONCILIATION_DETAILS( _1520_RECON_ADV, _1530_RECON_ADV_RSP ),
        UPDATE( _1304_FILE_ACTION_REQ, _1314_FILE_ACTION_REQ_RSP );

        private final int sendMsgId;
        private final int receiveMsgId;

        MsgType( int sendMsgId, int receiveMsgId ) {
            this.sendMsgId = sendMsgId;
            this.receiveMsgId = receiveMsgId;
        }

        public int getSendMsgId() {
            return this.sendMsgId;
        }
    }

    /**
     * Unpack results for {@link As2805EftexPack#unpack(IDependency, byte[], TransRec, MsgType, String)}
     * */
    public enum UnPackResult{
        UNPACK_OK,              // Success
        MAC_ERROR,              // Mac Field couldn't be verified/failed
        VERIFICATION_FAILED,    // Fields unpacked don't match the fields packed
        GENERIC_FAILURE         // Catch-all failure. If seen a lot of times, needs to be expanded
    }


}
