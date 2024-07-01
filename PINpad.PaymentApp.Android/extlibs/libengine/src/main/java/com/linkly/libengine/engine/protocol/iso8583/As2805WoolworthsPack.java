package com.linkly.libengine.engine.protocol.iso8583;

import static com.linkly.libengine.engine.protocol.iso8583.As2805WoolworthsPack.MsgType.AUTH;
import static com.linkly.libengine.engine.protocol.iso8583.As2805WoolworthsPack.MsgType.PREAUTH;
import static com.linkly.libengine.engine.protocol.iso8583.As2805WoolworthsPack.MsgType.UPDATE;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.As2805Woolworths.Bit._047_ADDITIONAL_DATA_NATIONAL;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.As2805Woolworths.Bit._055_ICC_DATA;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.As2805Woolworths.Bit._057_CASH_AMOUNT;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.As2805Woolworths.Bit._060_ADDITIONAL_PRIVATE;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.As2805Woolworths.Bit._070_NMIC;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.As2805Woolworths.Bit._071_MESSAGE_NUMBER;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.As2805Woolworths.Bit._072_MESSAGE_NUMBER_LAST;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.As2805Woolworths.Bit._110_KSN_AND_ENCRYPTION_DATA;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.As2805Woolworths.Bit._128_MAC;
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
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583.Bit._052_PIN_DATA;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583.Bit._054_ADDITIONAL_AMOUNTS;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583.Bit._090_ORIGINAL_DATA_ELEMENTS;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583.Bit._091_FILE_UPDATE_CODE;
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
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583.MsgType._0520_ACQUIRER_RECONCILE_ADV;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583.MsgType._0530_ACQUIRER_RECONCILE_ADV_RSP;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583.MsgType._0600_ADMIN_REQ;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583.MsgType._0610_ADMIN_REQ_RSP;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583.MsgType._0800_NWRK_MNG_REQ;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583.MsgType._0810_NWRK_MNG_REQ_RSP;
import static com.linkly.libengine.engine.reporting.ReconciliationManager.reconciliationDao;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.MessageStatus.FINALISED;
import static com.linkly.libengine.env.TableVersion.getEpatVersion;
import static com.linkly.libengine.env.TableVersion.getFcatVersion;
import static com.linkly.libengine.env.TableVersion.getPktVersion;
import static com.linkly.libengine.env.TableVersion.setEpatVersion;
import static com.linkly.libengine.env.TableVersion.setFcatVersion;
import static com.linkly.libengine.env.TableVersion.setPktVersion;
import static com.linkly.libmal.global.util.Util.hex2Str;
import static com.linkly.libsecapp.IP2PEncrypt.ElementType.CVV;
import static com.linkly.libsecapp.IP2PEncrypt.ElementType.EXPIRY_YYMM;
import static com.linkly.libsecapp.IP2PEncrypt.ElementType.EXPIRY_YYMMDD_CHIP;
import static com.linkly.libsecapp.IP2PEncrypt.ElementType.PAN;
import static com.linkly.libsecapp.IP2PEncrypt.ElementType.PIN_BLOCK;
import static com.linkly.libsecapp.IP2PEncrypt.ElementType.TRACK_2_FULL_MSR;
import static com.linkly.libsecapp.IP2PEncrypt.ElementType.TRACK_2_SHORT_FORMAT;
import static com.linkly.libsecapp.IP2PEncrypt.EncryptForStorageType.CVV_FORMAT;
import static com.linkly.libsecapp.IP2PEncrypt.EncryptForStorageType.FULL_TRACK_FORMAT;
import static com.linkly.libsecapp.IP2PEncrypt.PackFormat.ASCII;
import static com.linkly.libsecapp.IP2PEncrypt.PackFormat.BCD;
import static com.linkly.libsecapp.IP2PSec.KeyGroup.DYNAMIC_GROUP;

import com.linkly.libengine.BuildConfig;
import com.linkly.libengine.config.paycfg.PaymentSwitch;
import com.linkly.libengine.dependencies.IDependency;
import com.linkly.libengine.engine.Engine;
import com.linkly.libengine.engine.protocol.IProto;
import com.linkly.libengine.engine.protocol.iso8583.openisoj.As2805Woolworths;
import com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583;
import com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583Rev93;
import com.linkly.libengine.engine.protocol.iso8583.openisoj.exceptions.UnknownFieldException;
import com.linkly.libengine.engine.reporting.Reconciliation;
import com.linkly.libengine.engine.transactions.TransRec;
import com.linkly.libengine.engine.transactions.properties.TAmounts;
import com.linkly.libengine.engine.transactions.properties.TAudit;
import com.linkly.libengine.engine.transactions.properties.TCard;
import com.linkly.libengine.engine.transactions.properties.TProtocol;
import com.linkly.libengine.overrides.CoreOverrides;
import com.linkly.libmal.MalFactory;
import com.linkly.libmal.global.util.HexDump;
import com.linkly.libmal.global.util.Util;
import com.linkly.libsecapp.CardholderDataElement;
import com.linkly.libsecapp.DecryptResult;
import com.linkly.libsecapp.EncryptResult;
import com.linkly.libsecapp.IP2PEncrypt;
import com.linkly.libsecapp.IP2PSec;
import com.linkly.libsecapp.P2PLib;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

import timber.log.Timber;

public class As2805WoolworthsPack {
    public static final char SUBST_VAL_CVV = 'A';
    private static final char SUBST_VAL_TRACK_2_MSR = 'B';
    private static final char SUBST_VAL_TRACK_2_PAN = 'C';
    private static final char SUBST_VAL_TRACK_2_SHORT = 'D';
    private static final char SUBST_VAL_EXPIRY = 'E';
    private static final int ANDROID_TERMINAL_INDICATOR = 61;

    private static String tableName = "EPAT"; // only download this table until get further info about the others

    private static IP2PEncrypt getP2pEncrypt() {
        return P2PLib.getInstance().getIP2PEncrypt();
    }
    private static IP2PSec getSecMal() {
        return P2PLib.getInstance().getIP2PSec();
    }

    public static byte[] pack( IDependency d, TransRec trans, MsgType msgType, boolean registrationMsg) {

        byte[] result = null;
        try {
            switch( msgType ) {
                case PREAUTH:
                case AUTH:
                    // use same pack code for preauth (0100) and regular auth (0200)
                    result = packAuth(d, trans,msgType);
                    break;
                case ADVICE:
                    result = packAdvice(d, trans, msgType);
                    break;
                case NETWORK:
                    result = packNetwork(d, trans, registrationMsg, msgType);
                    break;
                case REVERSAL:
                    result = packReversal(d, trans, msgType);
                    break;
                case RECONCILIATION:
                    result = packReconciliation(d, trans, msgType);
                    break;
                default:
                    Timber.e( "Error - unexpected/unhandled msgType %s", msgType.name() );
                    break;
            }

            if (result != null) {
                Timber.e("Tx msg:");
                Timber.e(HexDump.dumpHexString("", result));
            }

            return result;

        } catch (Exception e) {
            trans.getProtocol().setMessageStatus(FINALISED);
            trans.save();
            Timber.e( "Pack failed, remove Transaction ref [%s] from batch as useless", trans.getAudit().getReference() );
            Timber.w(e);
            throw e;
        }
    }

    public static byte[] packUpdate( IDependency d, TransRec trans, MsgType msgType, As2805WoolworthsProto.FileUpdate fileUpdate) {

        byte[] result = null;
        try {
            if (msgType == UPDATE) {
                result = packFileUpdate(d, trans, fileUpdate, msgType);
            }
            return result;

        } catch (Exception e) {
            trans.getProtocol().setMessageStatus(FINALISED);
            trans.save();
            Timber.i( "Pack failed, remove from batch as useless");
            Timber.w(e);
            throw e;
        }
    }

    public static boolean isSecurityDisabled(IDependency d) {
        PaymentSwitch paySwitchCfg = d.getPayCfg().getPaymentSwitch();
        return null != paySwitchCfg && paySwitchCfg.isDisableSecurity();
    }


    /**
     * Perform basic sanity checks on the response data
     * @param sentMessageType {@link MsgType} original Message type
     * @param trans {@link TransRec} object
     * @param responseData {@link As2805Woolworths} response object
     * @return true if all sanity checks pass
     * */
    private static boolean isResponseDataIncorrect( As2805Woolworths responseData, TransRec trans, MsgType sentMessageType ){
        if( responseData.getMsgType() != sentMessageType.receiveMsgId ){
            Timber.i( "Received the incorrect Message type. Expected [%x], got [%x]", sentMessageType.receiveMsgId, responseData.getMsgType() );
            return true;
        }

        if ( ( !responseData.verifyString( trans.getAudit().getTerminalId(), _041_CARD_ACCEPTOR_TERMINAL_ID ) ) ) {
            Timber.i( "Terminal IDs don't match. Expected [%s], got [%s]", trans.getAudit().getTerminalId(), responseData.get( _041_CARD_ACCEPTOR_TERMINAL_ID ) );
            return true;
        }

        if ( ( !responseData.verifyString(trans.getAudit().getMerchantId(), _042_CARD_ACCEPTOR_ID_CODE ) ) ) {
            Timber.i( "Merchant IDs don't match. Expected [%s], got [%s]", trans.getAudit().getMerchantId(), responseData.get( _042_CARD_ACCEPTOR_ID_CODE ) );
            return true;
        }

        if ( ( trans.getProtocol().getStan() != Integer.parseInt( responseData.get( _011_SYS_TRACE_AUDIT_NUM ) ) ) ) {
            Timber.i( "STAN doesn't match. Expected [%s], got [%s]", trans.getProtocol().getStan().toString(), responseData.get( _011_SYS_TRACE_AUDIT_NUM ) );
            return true;
        }

        return false;
    }


    /**
     * must unpack the data completely before committing it to the transaction, so we don't get half the details
     * @param d {@link IDependency} object
     * @param responseData Byte array to be filled
     * @param ResponseAct {@link As2805WoolworthsProto.ResponseAction} object
     * @param trans {@link TransRec} object
     * @param sentMessageType {@link MsgType} Message type sent whose response we should be expecting
     * @return result {@link UnPackResult}:
     * <ul>
     *     <li>{@link UnPackResult#UNPACK_OK} Successful unpacking</li>
     *     <li>{@link UnPackResult#MAC_ERROR} Mac Field in the responseData is invalid</li>
     *     <li>{@link UnPackResult#VERIFICATION_FAILED} Fields in the response don't match the fields sent. Eg: TID, MID, Msg Type</li>
     *     <li>{@link UnPackResult#GENERIC_FAILURE} Catch all failure.</li>
     * </ul>
     * */
    @SuppressWarnings("java:S6541")
    public static UnPackResult unpack( IDependency d, byte[] responseData, TransRec trans, As2805WoolworthsProto.ResponseAction ResponseAct, MsgType sentMessageType ) {
        As2805Woolworths resp;
        byte[] decryptedMsg;
        try {
            if (responseData != null) {
                Timber.e("Rx Raw:");
                Timber.e(HexDump.dumpHexString("", responseData));
                if (sentMessageType.msgEncrypted) {
                    // decrypt msg, if encrypted
                    decryptedMsg = decryptMsg(responseData);
                } else {
                    decryptedMsg = responseData;
                }
                // unpack with field validation enabled
                resp = new As2805Woolworths(decryptedMsg, true);
                Timber.e("Rx Msg");
                Timber.e( resp.toString() );

                // Sanity checks
                if( As2805WoolworthsPack.isResponseDataIncorrect( resp, trans, sentMessageType ) ){
                    Timber.e( "Unpack failed, VERIFICATION_FAILED" );
                    return UnPackResult.VERIFICATION_FAILED;
                }

                String nmic = resp.get( _070_NMIC );

                // always validate the mac, in 3DES DUKPT, all messages are macced
                if(!isSecurityDisabled(d)) {
                    if (!verifyMac(decryptedMsg)) {
                        // mac verify failed. downgrade our logon state to trigger required action
                        Timber.e("MAC VERIFICATION ERROR DETECTED");
                        d.getProtocol().setInternalRejectReason(trans, IProto.RejectReasonType.MAC_FAILED);

                        // return the MAC failed. This will trigger an immediate reversal after a logon is done
                        return UnPackResult.MAC_ERROR;
                    } else {
                        Timber.i("MAC verified okay");
                    }
                } else {
                    Timber.e("MAC not verified");
                }

                updateResponseCodeMap(resp,trans);

                // Unpack date and time values for settlement and bank date times
                String settlementDate = resp.get(_015_SETTLEMENT_DATE);  // MMdd format
                if( settlementDate != null ) {
                    trans.getProtocol().setSettlementDate( settlementDate );

                    // for Woolworths, batch number and settlement date are the same thing, except Batch no is integer and settlement date is string. both YYMM format
                    trans.getProtocol().setBatchNumber( Integer.parseInt( settlementDate ) );
                }

                String bankTime = resp.get(_012_LOCAL_TRAN_TIME);
                if (bankTime != null) {
                    trans.getProtocol().setBankTime(bankTime);
                }

                String bankDate = resp.get(_013_LOCAL_TRAN_DATE);
                trans.getProtocol().setBankDate(bankDate);


                String authCode = resp.get(_038_AUTH_ID_RESPONSE);
                if (authCode != null && !authCode.isEmpty()) {
                    trans.getProtocol().setAuthCode(authCode);
                }

                if (CoreOverrides.get().isSpoofComms()) {
                    trans.getProtocol().setPosResponseCode("00");
                    trans.getProtocol().setServerResponseCode("00");
                    trans.getProtocol().setAuthCode("123456");
                }

                String nameLocation = resp.get(_043_CARD_ACCEPTOR_NAME_LOCATION);
                if (nameLocation != null && !nameLocation.isEmpty()) {
                    d.getPayCfg().getReceipt().getMerchant().setLine0(nameLocation);
                }

                String field55 = resp.get(_055_ICC_DATA);
                if (field55 != null) {
                    As2805WoolworthsUtils.unpackIccData(trans, field55);
                }

                String f48Response = resp.get( _048_ADDITIONAL_DATA );
                if( !Util.isNullOrEmpty( f48Response ) ){
                    unpackField48(trans, f48Response, resp.getMsgType(), nmic, ResponseAct);
                }
            }
            Timber.i("Unpack success");
            return UnPackResult.UNPACK_OK;
        } catch (Exception e) {
            Timber.e("Unpack failed");
            Timber.e(e);
            return UnPackResult.GENERIC_FAILURE;
        }
    }


    private static void updateResponseCodeMap(As2805Woolworths resp, TransRec trans) {

        boolean overrideResponseCode;
        switch (resp.getMsgType()) {
            case 0x430:
                overrideResponseCode = false;
                break;
            case 0x210:
            case 0x230:
            case 0x610:
            case 0x310:
            default:
                overrideResponseCode = true;
                break;
        }

        // We shouldn't be updating the main response code when a txn record is reversed, as get last receipt for any reversed transaction shows Approved receipt  TASK: IAAS-1870
        if (overrideResponseCode) {
            String responseCode = resp.get(_039_RESPONSE_CODE);
            // set display and receipt text based off response code
            trans.setProtocol(new As2805WoolworthsRspCodeMap().populateProtocolRecord(trans.getProtocol(), responseCode));
        }
    }

    /* must unpack the data completely before committing it to the transaction, so we dont get half the details */
    /* check the mac etc */
    public static boolean unpack( byte[] responseData, TransRec trans, As2805WoolworthsProto.FileUpdate fileUpdate, MsgType sentMessageType ) {

        As2805Woolworths resp;
        try {
            if (responseData != null) {
                resp = new As2805Woolworths(responseData);

                if( As2805WoolworthsPack.isResponseDataIncorrect( resp, trans, sentMessageType ) ){
                    return false;
                }

                Timber.i( resp.toString());

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

                String f48Response = resp.get( _048_ADDITIONAL_DATA );
                if( !Util.isNullOrEmpty( f48Response ) ){
                    fileUpdate.data = f48Response;

                    // get table EPAT/PKT/FCAT version info, no CPAT until receive the WW confirmation
                    if (fileUpdate.msgNumber == 1) {
                        byte[] field48 = Util.hexStringToByteArray(f48Response);
                        if (tableName.equals("EPAT")) {
                            byte[] EPAT_Version = Arrays.copyOfRange(field48, 10, 13);
                            // save the version info
                            setEpatVersion( Util.byteArrayToHexString(EPAT_Version));
                        } else if (tableName.equals("PKT")) {
                            byte[] PKT_Version = Arrays.copyOfRange(field48, 10, 13);
                            // save the version info
                            setPktVersion( Util.byteArrayToHexString(PKT_Version));
                        } else if (tableName.equals("FCAT")) {
                            byte[] FCAT_Version = Arrays.copyOfRange(field48, 8, 11);
                            // save the version info
                            setFcatVersion( Util.byteArrayToHexString(FCAT_Version));
                        }
                    }
                }
                String msgLast = resp.get( _072_MESSAGE_NUMBER_LAST );
                fileUpdate.msgNumberLast = !Util.isNullOrEmpty( msgLast );
                return true;
            }
        } catch (Exception e) {
            Timber.w(e);
        }
        return false;
    }


    private static void unpackField48_810(TransRec trans, String f48Response, String nmic, As2805WoolworthsProto.ResponseAction ResponseAct) {

        // Field 48 varies depending on the NMIC
        switch(nmic) {
            case "170":
                unpackField48_810_nmic170(trans, f48Response, ResponseAct);
                break;

            default:
                Timber.e( "Unhandled F48 response NMIC: %s", nmic);
                break;
        }

    }

    private static void unpackField48_610(TransRec trans, String f48Response, String nmic, As2805WoolworthsProto.ResponseAction ResponseAct) {

        // todo unpack the data

    }

    // Unpacks field 48 and applies it to the trans rec (If need be) based on the type of message
    // Annoyingly this varies depending on what message is being sent
    private static void unpackField48(TransRec trans, String f48Response, int type, String nmic, As2805WoolworthsProto.ResponseAction ResponseAct) {

        // Field 48 varies depending on the message
        switch(type) {
            case _0810_NWRK_MNG_REQ_RSP:
                unpackField48_810(trans, f48Response, nmic, ResponseAct);
                break;

            case _0610_ADMIN_REQ_RSP:
                unpackField48_610(trans, f48Response, nmic, ResponseAct);
                break;

            default:
                Timber.e( "Unhandled F48 response message: %s", type);
                break;
        }
    }

    private static void unpackField48_810_nmic170(TransRec trans, String f48Response, As2805WoolworthsProto.ResponseAction ResponseAct) {
        /*
            DE48 data from spec (3DES DUKPT format):
            total 19 bytes
            -----------------------------------
            PP_SW Return Code               2an
            CPAT Return Code                2an
            PKT Return Code                 2an
            EPAT Return Code                2an
            FCAT Return Code                2an
            STAN                            6n bcd, 3 bytes
            Local Year (YYYY)               4n bcd, 2 bytes
            MCC (Merchant Category Code)    4an
        */
        byte[] field48 = Util.hexStringToByteArray(f48Response);
        final int F48LEN = 19;

        if(field48.length >= F48LEN) {
            byte[] PP_SW_Code = Arrays.copyOfRange(field48, 0, 2);
            byte[] CPAT_Code = Arrays.copyOfRange(field48, 2, 4);
            String sCPAT = new String(CPAT_Code);
            if (sCPAT.equals("ND")) {
                ResponseAct.CPAT_Require = true;
            }
            byte[] PKT_Code = Arrays.copyOfRange(field48, 4, 6);
            String sPKT = new String(PKT_Code);
            if (sPKT.equals("ND")) {
                ResponseAct.PKT_Require = true;
            }
            byte[] EPAT_Code = Arrays.copyOfRange(field48, 6, 8);
            String sEPAT = new String(EPAT_Code);
            if (sEPAT.equals("ND")) {
                ResponseAct.EPAT_Require = true;
            }
            byte[] FCAT_Code = Arrays.copyOfRange(field48, 8, 10);
            String sFCAT = new String(FCAT_Code);
            if (sFCAT.equals("ND")) {
                ResponseAct.FCAT_Require = true;
            }

            byte[] stan = Arrays.copyOfRange(field48, 10, 13);
            byte[] year = Arrays.copyOfRange(field48, 13, 15);

            // set next stan to use
            Timber.i( "stan from host = %s", hex2Str(stan) );
            int nextStan = Integer.parseInt(Util.bcd2Str(stan));
            Timber.e( "setting next stan to use = %s", nextStan);

            // NOTE: this is the NEXT stan that we must use. Because Stan.getNewValue pre-increments it, save as -1 here to ensure next stan used = this one
            nextStan--;
            if( nextStan <= 0 ){
                // ensure stan is between 1 and 999999
                nextStan = 999999;
            }

            trans.getProtocol().setResetStan(nextStan);

            // Our year value for applying the clock
            trans.getProtocol().setYear(Util.bcd2Str(year));
            Timber.e( "year from host = %s", hex2Str(year) );

            // if we get here, then RSA is complete!
            Timber.i( "DUKPT logon NMIC 170 successful!" );

        } else {
            // If we have reached here either something has very wrong either invalid data etc.
            Timber.e( "DUKPT logon NIMC 170 Error invalid F48");
        }
    }

    public static boolean getEncryptedPinBlock() {
        boolean result = getSecMal().getDUKPTEncryptedPinBlock(DYNAMIC_GROUP);
        if(!result) {
            Timber.e( "WARNING - PIN block capture failed" );
            return false;
        }

        return true;
    }

    // get expiry or other 'non sensitive' card data
    public static String getNonSensitiveElement( IP2PEncrypt.ElementType element ) {
        int dataLen = getP2pEncrypt().getElementLength( element );
        if( dataLen <= 0 ) {
            Timber.i( "WARNING - addSensitiveElement element " + element.name() + " not found, skipping" );
            return null;
        }

        return getP2pEncrypt().getData( element );
    }

    private static int findByteInArray( byte[] dataToSearch, int searchFromIdx, byte byteToFind ) {
        for( int idx = searchFromIdx; idx<dataToSearch.length; idx++ ) {
            if( dataToSearch[idx] == byteToFind ) {
                return idx;
            }
        }

        return -1;
    }

    // get element length 'n', and look for a run of n x byteToFind bytes in dataToSearch, starting at searchFromIdx
    // returns new CardholderDataElement if found
    private static CardholderDataElement findElementPlaceholder(byte[] dataToSearch, int searchFromIdx, IP2PEncrypt.ElementType elementType, char charToFind, IP2PEncrypt.PackFormat packFormat ) {
        int elementLength = getP2pEncrypt().getElementLength( elementType );

        byte[] packedMsg;
        if( packFormat == BCD ) {
            // for Woolworths, various card data fields are BCD packed. so convert input message for comparison from bcd to ascii
            packedMsg = Objects.requireNonNull(hex2Str(dataToSearch)).getBytes();
        } else {
            packedMsg = dataToSearch;
        }

        // data not found in p2pe module SAD
        if( elementLength <= 0 )
            return null;

        // look for a run of at least 'elementLength' bytes, remember it's index
        for( int idx = searchFromIdx; idx<packedMsg.length-elementLength; idx++ ) {

            if( packedMsg[idx] == charToFind ) {
                int runCount;

                // see if there's a run of elementLength bytes here
                for( runCount=0; (runCount < elementLength) && ((idx+runCount) < packedMsg.length); runCount++ ) {
                    if( packedMsg[idx+runCount] != charToFind )
                        break;
                }

                // if we found a run of elementLength
                if( runCount == elementLength ) {
                    // record idx in a new CardholderDataElement object
                    if( packFormat == BCD ) {
                        // divide current index by 2 as the real message is BCD packed, and we're searching in an ASCII (double length) message
                        // element length in bcd format is rounded up if odd length, then divide by 2 to get packed bcd size
                        return new CardholderDataElement(elementType, false, idx / 2, (elementLength + 1) / 2, BCD);
                    } else {
                        return new CardholderDataElement( elementType, false, idx, elementLength, ASCII );
                    }

                }
            }
        }

        // else we didn't find
        return null;
    }

    private static CardholderDataElement[] getSecureElements( byte[] msg ) {

        // const value really - skip MTI (2 bytes) and 8 bytes of bitmap as this can contain binary data and could get a false match
        final int REPLACE_FROM_OFFSET = 10;

        // scan message for fields
        CardholderDataElement cvvElement = findElementPlaceholder( msg, REPLACE_FROM_OFFSET, CVV, SUBST_VAL_CVV, ASCII );
        CardholderDataElement track2MsrElement = findElementPlaceholder( msg, REPLACE_FROM_OFFSET, TRACK_2_FULL_MSR, SUBST_VAL_TRACK_2_MSR, BCD );
        CardholderDataElement panElement = findElementPlaceholder( msg, REPLACE_FROM_OFFSET, PAN, SUBST_VAL_TRACK_2_PAN, BCD );
        CardholderDataElement track2ShortElement = findElementPlaceholder( msg, REPLACE_FROM_OFFSET, TRACK_2_SHORT_FORMAT, SUBST_VAL_TRACK_2_SHORT, BCD );
        CardholderDataElement expiryElement = findElementPlaceholder( msg, REPLACE_FROM_OFFSET, EXPIRY_YYMM, SUBST_VAL_EXPIRY, BCD );

        // add cardholder data element object in the substitution list for every element found
        int numElements = 0;
        if( cvvElement != null )
            numElements++;
        if( track2MsrElement != null )
            numElements++;
        if( panElement != null )
            numElements++;
        if( track2ShortElement != null )
            numElements++;
        if( expiryElement != null )
            numElements++;

        CardholderDataElement[] elements = new CardholderDataElement[numElements];

        int elementNo = 0;
        if( cvvElement != null )
            elements[elementNo++] = cvvElement;
        if( track2MsrElement != null )
            elements[elementNo++] = track2MsrElement;
        if( panElement != null )
            elements[elementNo++] = panElement;
        if( track2ShortElement != null )
            elements[elementNo++] = track2ShortElement;
        if( expiryElement != null )
            elements[elementNo++] = expiryElement;

        return elements;
    }

    private static byte[] encryptFullMessage(byte[] msgPacked, As2805Woolworths msg, boolean disableEncryption, CardholderDataElement[] secureElements) throws Exception {
        // hex dump packed msg
        Timber.e("clear packed msg:");
        Timber.e(HexDump.dumpHexString(msgPacked));

        for( CardholderDataElement element : secureElements ) {
            Timber.e( "element name %s, offset %d, length %d", element.getElementType().name(), element.getSubstitueIndex(), element.getLength() );
        }

        // get STAN
        String stanStr = msg.get(Iso8583Rev93.Bit._011_SYS_TRACE_AUDIT_NUM);
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
                    IP2PEncrypt.EncryptAlgorithm.DUKPT_3DES_OFB_UNIDIRECTIONAL,
                    DYNAMIC_GROUP.getKeyIndex(), IP2PEncrypt.IVToUse.USE_DUKPT_KSN_AND_BINARY_STAN, stanInt);
        }

        // do the encryption
        EncryptResult encResult = getP2pEncrypt().encrypt(msgPacked, params, secureElements);
        if (encResult != null) {
            return encResult.getEncryptedMessage();
        } else {
            Timber.e( "error encrypting message" );
            return null;
        }
    }

    private static byte[] addMac(As2805Woolworths msg, CardholderDataElement[] secureElements) throws Exception {
        // first do the mac
        IP2PEncrypt.MacParameters macParameters = new IP2PEncrypt.MacParameters(IP2PEncrypt.MacAlgorithm.DUKPT_SEND, DYNAMIC_GROUP.getKeyIndex(), "");

        // re-pack with dummy MAC field
        byte[] msgPacked = msg.toMsg();

        // chop off the final 8 bytes (dummy mac) to be macced
        byte[] msgToMac = new byte[msgPacked.length - 8];
        System.arraycopy(msgPacked, 0, msgToMac, 0, msgPacked.length - 8);

        byte[] mac = getP2pEncrypt().getMac(msgToMac, macParameters, secureElements);
        // append mac
        msg.set(_128_MAC, hex2Str(mac));

        return msg.toMsg();
    }

    private static boolean verifyMac(byte[] message) {
        IP2PEncrypt.MacParameters macParameters = new IP2PEncrypt.MacParameters(IP2PEncrypt.MacAlgorithm.DUKPT_RECEIVE, DYNAMIC_GROUP.getKeyIndex(), "");
        return getP2pEncrypt().verifyMac(message, macParameters);
    }

    /**
     * decrypts partially encrypted message, if KSN (DE110) is present
     *
     * @param inputBytes - input message bytes, partially encrypted
     * @return As2805Woolworths object with unpacked, decrypted data
     */
    public static byte[] decryptMsg(byte[] inputBytes) throws Exception {
        // parse first time with no field data validation, as most fields are encrypted
        As2805Woolworths inputMsg = new As2805Woolworths(inputBytes, false);

        // try to extract KSN
        String ksnStr = inputMsg.get(_110_KSN_AND_ENCRYPTION_DATA);
        if (ksnStr == null || ksnStr.length() == 0) {
            // de110 not found, return input msg as output, unchanged
            Timber.e("As2805Woolworths KSN field not found, not decrypting");
            // parse again now with validateData arg set to true
            return inputBytes;
        }

        String stanStr = inputMsg.get(_011_SYS_TRACE_AUDIT_NUM);
        int stanInt = Integer.parseInt(stanStr);

        Timber.e("As2805Woolworths DUKPT decrypting, KSN %s, STAN %d", ksnStr, stanInt);

        // note: we can't actually specify the KSN to use for decrypt. it needs to match what we sent

        IP2PEncrypt.EncryptParameters params = new IP2PEncrypt.EncryptParameters(IP2PEncrypt.PaddingAlgorithm.NONE,
                IP2PEncrypt.EncryptAlgorithm.DUKPT_3DES_OFB_UNIDIRECTIONAL,
                DYNAMIC_GROUP.getKeyIndex(), IP2PEncrypt.IVToUse.USE_DUKPT_KSN_AND_BINARY_STAN, stanInt);

        DecryptResult result = getP2pEncrypt().decrypt(inputBytes, params);
        byte[] decryptedMsg = result.getDecryptedMessage();

        // now selectively copy across cleartext msgs from original msg
        byte[] output = inputMsg.replaceClearTextFields(inputBytes, decryptedMsg);

        if (output != null) {
            Timber.e("Rx Decrypted:");
            Timber.e(HexDump.dumpHexString("", output));
        }
        // return 'raw' decrypted msg bytes
        return output;
    }


    public static byte[] addMacAndEncrypt(TransRec trans, As2805Woolworths msg, boolean encrypt) throws Exception {
        // append a KSN and dummy mac to the current message, so the bitmap is extended (16 byte) and has correct value
        String ksnStr = trans.getSecurity().getKsn();
        if (ksnStr != null && ksnStr.length() == 20) {
            byte[] ksn = Util.hexStringToByteArray(ksnStr);
            byte[] encryptionData = new byte[11];
            encryptionData[0] = 0x05; // 05 = TDEA, DUKPT
            System.arraycopy(ksn, 0, encryptionData, 1, 10);
            msg.set(_110_KSN_AND_ENCRYPTION_DATA, Util.byteArrayToHexString(encryptionData));
        } else {
            // this is where we detect if the terminal doesn't have DUKPT keys
            Timber.e("getDUKPTKsn error, ksn length not 10. ksn = [%s]", ksnStr==null?"null":ksnStr);
        }
        byte[] dummyMac = new byte[8];
        msg.set(_128_MAC, hex2Str(dummyMac));

        // sets elements array for mac operation
        byte[] msgPacked = msg.toMsg();
        CardholderDataElement[] secureElements = getSecureElements(msgPacked);

        msgPacked = addMac(msg, secureElements);
        Timber.e( "Tx Msg");
        Timber.e( msg.toString());

        if (encrypt) {
            // now do full message encryption.
            byte[] msgEncrypted = encryptFullMessage(msgPacked, msg, false, secureElements);
            if (msgEncrypted == null) {
                Timber.e("message encrypt error");
                return null;
            }
            return msg.replaceClearTextFields(msgEncrypted);
        } else {
            return msg.toMsg();
        }
    }


    private static boolean packCardData(As2805Woolworths msg, TransRec trans, boolean useSavedCardDetails) throws UnknownFieldException {
        TCard cardinfo = trans.getCard();

        if( null == getP2pEncrypt() ) {
            Timber.e( "ERROR p2pEncrypt is NULL" );
            return false;
        }

        if ( useSavedCardDetails ) {
            // if we need to use stored card data, load it into the p2pe module
            if( com.linkly.libmal.global.util.Util.isNullOrEmpty(trans.getSecurity().getEncTrack2()) ) {
                Timber.e( "error retrieving saved card details" );
                return false;
            }

            // we have encrypted data, load it into p2pe module from storage
            // calling stash/unstash in caller method
            if( !getP2pEncrypt().decryptFromStorage(com.linkly.libmal.global.util.Util.hexStringToByteArray(trans.getSecurity().getEncTrack2()), FULL_TRACK_FORMAT) ) {
                Timber.e( "error decrypting stored card data ");
                return false;
            }

            // Also load CVV
            if( !com.linkly.libmal.global.util.Util.isNullOrEmpty(trans.getSecurity().getCvv()) ) {
                if( !getP2pEncrypt().decryptFromStorage(com.linkly.libmal.global.util.Util.hexStringToByteArray(trans.getSecurity().getCvv()), CVV_FORMAT) ) {
                    Timber.e( "error decrypting stored CVV data ");
                    return false;
                }
            }
        }

        boolean returnFlag = true;

        // if this was a manual pan transaction, and we have required data
        if ( cardinfo.isManual() && getP2pEncrypt().getElementLength( PAN ) > 0 ) {

            // get expiry length - use long format expiry if we have it
            int expLen = getP2pEncrypt().getElementLength( EXPIRY_YYMMDD_CHIP );
            if ( expLen <= 0 ) {
                // short format - sourced from manual entry or swipe
                expLen = getP2pEncrypt().getElementLength( EXPIRY_YYMM );
            }

            // return error if no expiry found - shouldn't happen
            if ( expLen <= 0 ) {
                Timber.e( "error expLen" );
                returnFlag = false;
            } else {
                // pack pan and expiry if we have them
                msg.set( _002_PAN, As2805WoolworthsUtils.packSensitiveField( PAN, SUBST_VAL_TRACK_2_PAN ) );
                msg.set( _014_EXPIRATION_DATE, As2805WoolworthsUtils.packSensitiveField( EXPIRY_YYMM, SUBST_VAL_EXPIRY ) );
            }
        } else if ( getP2pEncrypt().getElementLength( TRACK_2_FULL_MSR ) > 0 ) {
            // else it's msr/emv/ctls
            msg.set( _035_TRACK_2_DATA, As2805WoolworthsUtils.packSensitiveField( TRACK_2_FULL_MSR, SUBST_VAL_TRACK_2_MSR ) );
        } else {
            // else we don't have required card data, return error
            Timber.e( "error - missing required card data, use saved = " + useSavedCardDetails + ", track2Element = " + TRACK_2_FULL_MSR.toString() );
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
            As2805Woolworths msg = new As2805Woolworths();

            if (msgType == PREAUTH) {
                proto.setOriginalMessageType(100);
                msg.setMsgType(Iso8583.MsgType._0100_AUTH_REQ);
            } else {
                proto.setOriginalMessageType(200);
                msg.setMsgType(Iso8583.MsgType._0200_TRAN_REQ);
            }

            proto.setOriginalStan(proto.getStan());

            if (!packCardData(msg, trans, false) ) {
                return null;
            }

            msg.putProcessingCode(As2805WoolworthsUtils.packProcCode(trans));
            msg.setTransactionAmount(amounts.getTotalAmount());

            msg.set(_011_SYS_TRACE_AUDIT_NUM, As2805WoolworthsUtils.packStan(proto.getStan()));
            msg.set(_022_POS_ENTRY_MODE, As2805WoolworthsUtils.packPosEntryMode(trans));
            msg.set(_023_CARD_SEQUENCE_NUM, As2805WoolworthsUtils.packCardSeqNumber(trans));
            msg.set(_025_POS_CONDITION_CODE, As2805WoolworthsUtils.packPosConditionCode(trans) );
            msg.set(_032_ACQUIRING_INST_ID_CODE, As2805WoolworthsUtils.packAiic(paySwitchCfg) );
            msg.set(_037_RETRIEVAL_REF_NUM, As2805WoolworthsUtils.packRetRefNumber(trans));
            msg.set(_041_CARD_ACCEPTOR_TERMINAL_ID, auditinfo.getTerminalId());
            msg.set(_042_CARD_ACCEPTOR_ID_CODE, As2805WoolworthsUtils.packMerchantId(auditinfo.getMerchantId()));
            msg.set(_047_ADDITIONAL_DATA_NATIONAL, As2805WoolworthsUtils.packAdditionalDataNational47(trans, SUBST_VAL_CVV) );
            msg.set(_049_TRAN_CURRENCY_CODE, "036" );

            // if CVM was an online PIN type
            if (trans.getCard().getCvmType().isOnlinePin()) {
                // tell sec app to encrypt the PIN block now - this is important to do now, as the current KSN may have moved on several since PIN entered
                boolean result = getEncryptedPinBlock();
                String pinblock = getNonSensitiveElement(PIN_BLOCK);
                if (pinblock != null && result) {
                    msg.set(_052_PIN_DATA, pinblock);
                } else {
                    Timber.e("No pin block, sending msg without DE52, probably a NoCVM transaction. Encrypt result = %b, pinblock null = %b", result, pinblock==null);
                }
            }

            if (msgType == AUTH && trans.getAmounts().getTip() > 0 || trans.getAmounts().getSurcharge() > 0) {
                msg.set(_054_ADDITIONAL_AMOUNTS, As2805WoolworthsUtils.packAdditionalAmounts(trans));
            }

            // pack DE55 if not refund, and ICC or CTLS card type
            if( !trans.isRefund() && ( trans.getCard().isIccCaptured() || trans.getCard().isCtlsCaptured() ) ) {
                msg.set( _055_ICC_DATA, As2805WoolworthsUtils.packIccData( trans, msg, false ) );
            }

            // if msgType is Auth and if any cash component (pwcb or cashout) is present, pack it here otherwise pack with zero
            if (msgType == AUTH) {
                if (trans.getAmounts().getCashbackAmount() > 0 || trans.isCash()) {
                    msg.set(_057_CASH_AMOUNT, As2805WoolworthsUtils.packCashAmount(trans));
                } else {
                    msg.set(_057_CASH_AMOUNT, "000000000000");
                }
            }
            msg.set(_060_ADDITIONAL_PRIVATE, "0");

            return addMacAndEncrypt(trans, msg, msgType.msgEncrypted);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static byte[] packAdvice(IDependency d, TransRec trans, MsgType msgType) {

        try {
            Timber.i( "stashing card data" );
            getP2pEncrypt().stash();

            TProtocol proto = trans.getProtocol();
            TAmounts amounts = trans.getAmounts();
            PaymentSwitch paySwitchCfg = d.getPayCfg().getPaymentSwitch();
            As2805Woolworths msg = new As2805Woolworths();

            proto.setOriginalMessageType(220);
            proto.setOriginalStan(proto.getStan());
            proto.setOriginalTransmissionDateTime(trans.getAudit().getTransDateTime());

            if( proto.getAdviceAttempts() > 0 ) {
                // set msg type to 0221 (advice repeat)
                msg.setMsgType(Iso8583.MsgType._0221_TRAN_ADV_REP);
            } else {
                // set msg type to 0220 (advice, first attempt)
                msg.setMsgType(Iso8583.MsgType._0220_TRAN_ADV);
            }

            if (!packCardData(msg, trans, true) ) {
                Timber.e( "ERROR PACKING CARD DATA for ADVICE" );
                return null;
            }

            msg.putProcessingCode(As2805WoolworthsUtils.packProcCode(trans));
            msg.setTransactionAmount(amounts.getTotalAmount());
            msg.set(_007_TRAN_DATE_TIME,As2805WoolworthsUtils.packLocalDateTime(trans));
            msg.set(_011_SYS_TRACE_AUDIT_NUM, As2805WoolworthsUtils.packStan(proto.getStan()));
            msg.set(_012_LOCAL_TRAN_TIME,As2805WoolworthsUtils.packTransTime(trans, d.getPayCfg().getBankTimeZone()));
            msg.set(_013_LOCAL_TRAN_DATE,As2805WoolworthsUtils.packTransDate(trans, d.getPayCfg().getBankTimeZone()));
            msg.set(_022_POS_ENTRY_MODE, As2805WoolworthsUtils.packPosEntryMode(trans));
            msg.set(_023_CARD_SEQUENCE_NUM, As2805WoolworthsUtils.packCardSeqNumber(trans));

            msg.set(_025_POS_CONDITION_CODE, As2805WoolworthsUtils.packPosConditionCode(trans) );
            msg.set(_032_ACQUIRING_INST_ID_CODE, As2805WoolworthsUtils.packAiic(paySwitchCfg) );
            msg.set(_037_RETRIEVAL_REF_NUM, As2805WoolworthsUtils.packRetRefNumber(trans));
            msg.set(_038_AUTH_ID_RESPONSE, As2805WoolworthsUtils.packAuthCode(trans));
            msg.set(_041_CARD_ACCEPTOR_TERMINAL_ID, d.getPayCfg().getStid());
            msg.set(_042_CARD_ACCEPTOR_ID_CODE, As2805WoolworthsUtils.packMerchantId(d.getPayCfg().getMid()));

            msg.set(_047_ADDITIONAL_DATA_NATIONAL, As2805WoolworthsUtils.packAdditionalDataNational47(trans, SUBST_VAL_CVV) );
            msg.set(_049_TRAN_CURRENCY_CODE, "036" );

            if( null != trans.getAmounts() ) {
                if (trans.getAmounts().getTip() > 0 || trans.getAmounts().getCashbackAmount() > 0) {
                    msg.set(_054_ADDITIONAL_AMOUNTS, As2805WoolworthsUtils.packAdditionalAmounts(trans));
                }
            }

            if( !Util.isNullOrEmpty(trans.getEmvTagsString()) ) {
                msg.set(_055_ICC_DATA, trans.getEmvTagsString());
            } else if( !Util.isNullOrEmpty(trans.getCtlsTagsString()) ) {
                msg.set(_055_ICC_DATA, trans.getCtlsTagsString());
            } else {
                Timber.i( "not packing DE 55 for advice as no tag data saved on trans record" );
            }

            if( null != trans.getAmounts() && trans.getAmounts().getCashbackAmount() > 0) {
                msg.set(_057_CASH_AMOUNT, As2805WoolworthsUtils.packCashAmount(trans));
            } else {
                msg.set(_057_CASH_AMOUNT, "000000000000");
            }
            msg.set(_060_ADDITIONAL_PRIVATE, "0");

            return addMacAndEncrypt(trans, msg, msgType.msgEncrypted);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            Timber.i( "unstashing card data" );
            getP2pEncrypt().unstash();
        }
    }

    private static byte[] packReversalImpl(IDependency d, TransRec trans, int stan, MsgType msgType) {

        As2805Woolworths msg = new As2805Woolworths();
        TProtocol proto = trans.getProtocol();
        TAmounts amounts = trans.getAmounts();
        TAudit auditinfo = trans.getAudit();
        PaymentSwitch paySwitchCfg = d.getPayCfg().getPaymentSwitch();

        // For acquirers that need to have the original STAN (so RRN as well) as the transaction to be reversed
        if (Engine.getDep().getPayCfg().isIncludedOrginalStandInRec() || Engine.getDep().getPayCfg().isReversalCopyOriginal()) {
            stan = trans.getProtocol().getOriginalStan();
        }

        try {
            Timber.i( "stashing card data" );
            getP2pEncrypt().stash();

            if( proto.getAdviceAttempts() > 0 ) {
                // set msg type to 0421 (reversal repeat)
                msg.setMsgType(Iso8583.MsgType._0421_ACQUIRER_REV_ADV_REP);
            } else {
                // set msg type to 0420 (reversal, first try)
                msg.setMsgType(Iso8583.MsgType._0420_ACQUIRER_REV_ADV);
            }

            if (!packCardData(msg, trans, true) ) {
                Timber.e( "Card Data of Transaction [%s] couldn't be packed", trans.getAudit().getReference() );
                return null;
            }

            msg.putProcessingCode(As2805WoolworthsUtils.packProcCode(trans));
            msg.setTransactionAmount(amounts.getTotalAmount());

            msg.set(_011_SYS_TRACE_AUDIT_NUM, As2805WoolworthsUtils.packStan(stan));
            msg.set(_022_POS_ENTRY_MODE, As2805WoolworthsUtils.packPosEntryMode(trans));

            msg.set(_023_CARD_SEQUENCE_NUM, As2805WoolworthsUtils.packCardSeqNumber(trans));

            msg.set(_032_ACQUIRING_INST_ID_CODE, As2805WoolworthsUtils.packAiic(paySwitchCfg) );

            msg.set(_037_RETRIEVAL_REF_NUM, trans.getProtocol().getRRN());
            msg.set(_041_CARD_ACCEPTOR_TERMINAL_ID, d.getPayCfg().getStid());
            msg.set(_042_CARD_ACCEPTOR_ID_CODE, As2805WoolworthsUtils.packMerchantId(d.getPayCfg().getMid()));

            msg.set(_047_ADDITIONAL_DATA_NATIONAL, As2805WoolworthsUtils.packAdditionalDataNational47(trans, SUBST_VAL_CVV) );
            msg.set(_049_TRAN_CURRENCY_CODE, "036" );

            if( !Util.isNullOrEmpty(trans.getEmvTagsString()) ) {
                msg.set(_055_ICC_DATA, trans.getEmvTagsString());
            } else if( !Util.isNullOrEmpty(trans.getCtlsTagsString()) ) {
                msg.set(_055_ICC_DATA, trans.getCtlsTagsString());
            } else {
                Timber.i( "not packing DE 55 for reversal as no tag data saved on trans record" );
            }

            if( null != trans.getAmounts() && trans.getAmounts().getCashbackAmount() > 0) {
                msg.set(_057_CASH_AMOUNT, As2805WoolworthsUtils.packCashAmount(trans));
            } else {
                msg.set(_057_CASH_AMOUNT, "000000000000");
            }
            msg.set(_060_ADDITIONAL_PRIVATE, "0");
            msg.set(_090_ORIGINAL_DATA_ELEMENTS, As2805WoolworthsUtils.packOriginalDataElements(d,trans));

            return addMacAndEncrypt(trans, msg, msgType.msgEncrypted);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            Timber.i( "unstashing card data" );
            getP2pEncrypt().unstash();
        }
    }

    private static byte[] packReversal(IDependency d, TransRec trans, MsgType msgType) {

        try {

            if (Engine.getDep().getP2PLib().getIP2PSec().getInstalledKeyType() == IP2PSec.InstalledKeyType.DUKPT) {
                Timber.i( "PACK Reversal with DUKPT keys");
            } else {
                Timber.i( "PACK Reversal with Master Session keys");
            }

            return packReversalImpl(d, trans, trans.getProtocol().getStan(), msgType);

        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Packs F48 field for Network message type
     * @return f48 contains ASCII Hex message
     * */

    @SuppressWarnings({"java:S125","java:S3776"})
    private static String pack170F48(IDependency d) throws IOException {
        PaymentSwitch paySwitchCfg = d.getPayCfg().getPaymentSwitch();

        ByteArrayOutputStream payload = new ByteArrayOutputStream();

        // pinpad serial number
        String serialNumber = MalFactory.getInstance().getHardware().getSerialNumber();
        String paddedSerial = Util.leftPad(serialNumber, 16, '0');
        payload.write( Util.hexStringToByteArray(paddedSerial) );
        // key management version 04 = 3DES DUKPT, 05 = AES DUKPT
        payload.write(0x04);

        // pinpad version
        // Warning: Debug builds need hardcoded version to host otherwise woolies will reject host messages.
        byte[] pinpadVersion;
        if(BuildConfig.DEBUG) {
            Timber.e("Warning: Overriding pinpad version packing for host");
            pinpadVersion = new byte[]{
                    Util.DecToBCD(ANDROID_TERMINAL_INDICATOR, 1 )[0],
                    Util.DecToBCD( 1, 1 )[0],
                    Util.DecToBCD( 5, 1 )[0]};

        } else {
            pinpadVersion = new byte[]{
                    Util.DecToBCD(ANDROID_TERMINAL_INDICATOR, 1 )[0],
                    Util.DecToBCD( BuildConfig.versionMajor, 1 )[0],
                    Util.DecToBCD( BuildConfig.versionMinor, 1 )[0]};
        }

        Timber.d("Setting woolworths pinpad version as: %s", Util.bcd2Str(pinpadVersion));
        payload.write(pinpadVersion);

        // cpat version
        payload.write( Util.str2Bcd(
                Util.padLeft(
                    Integer.toString(d.getPayCfg().getCardProductVersion()),
                    6,
                    '0'
                )
        ) );

        String zeros = "000000";

        // pkt version
        String pktVersion = getPktVersion();
        if( paySwitchCfg != null && !Util.isNullOrEmpty(paySwitchCfg.getDefaultPktVersion() ) && paySwitchCfg.getDefaultPktVersion().length() == 6 ) {
            // use default PKT version from config
            payload.write(Util.str2Bcd(paySwitchCfg.getDefaultPktVersion()));
        } else if (!pktVersion.isEmpty()) {
            byte[] pktBytes = Util.hexStringToByteArray(pktVersion);
            payload.write(pktBytes);
        } else {
            // set zeros
            setPktVersion(zeros);
            payload.write( new byte[] { 0x00, 0x00, 0x00 } );
        }

        // epat version
        String epatVersion = getEpatVersion();
        if( paySwitchCfg != null && !Util.isNullOrEmpty(paySwitchCfg.getDefaultEpatVersion() ) && paySwitchCfg.getDefaultEpatVersion().length() == 6 ) {
            // use default EPAT version from config
            payload.write(Util.str2Bcd(paySwitchCfg.getDefaultEpatVersion()));
        } else if (!epatVersion.isEmpty()) {
            byte[] epatBytes = Util.hexStringToByteArray(epatVersion);
            payload.write(epatBytes);
        } else {
            // set zeros
            setEpatVersion(zeros);
            payload.write(new byte[]{(byte) 0x00, 0x00, 0x00});
        }

        if( paySwitchCfg != null && !Util.isNullOrEmpty(paySwitchCfg.getDefaultSpotVersion() ) && paySwitchCfg.getDefaultSpotVersion().length() == 6 ) {
            // use default SPOT version from config
            payload.write(Util.str2Bcd(paySwitchCfg.getDefaultSpotVersion()));
        } else {
            // spot version
            payload.write(new byte[]{0x00, 0x00, 0x00});
        }

        // fcat version
        String fcatVersion = getFcatVersion();
        if( paySwitchCfg != null && !Util.isNullOrEmpty(paySwitchCfg.getDefaultFcatVersion() ) && paySwitchCfg.getDefaultFcatVersion().length() == 6 ) {
            // use default FCAT version from config
            payload.write(Util.str2Bcd(paySwitchCfg.getDefaultFcatVersion()));
        } else if (!fcatVersion.isEmpty()) {
            byte[] fcatBytes = Util.hexStringToByteArray(fcatVersion);
            payload.write(fcatBytes);
        } else {
            // set the initial version
            setFcatVersion(zeros);
            payload.write( new byte[] { 0x00, 0x00, 0x00 } );
        }

        String terminalModel = MalFactory.getInstance().getHardware().getModel();
        String terminalModelPadded = Util.padRight(terminalModel, 8, ' ');
        payload.write( terminalModelPadded.getBytes() );

        Timber.i( "de48 nmic 170 request payload length = %s", payload.toByteArray().length );

        return Util.byteArrayToHexString(payload.toByteArray());
    }

    private static byte[] packNetwork(IDependency d, TransRec trans, boolean registrationMsg, MsgType msgType) {
        try {
            TProtocol proto = trans.getProtocol();
            TAudit auditInfo = trans.getAudit();

            As2805Woolworths msg = new As2805Woolworths();

            proto.setOriginalMessageType(804);
            proto.setOriginalTransmissionDateTime(trans.getAudit().getTransDateTime());
            trans.setSoftwareVersion(d.getPayCfg().getPaymentAppVersion());

            msg.setMsgType(Iso8583.MsgType._0800_NWRK_MNG_REQ);
            msg.set(_032_ACQUIRING_INST_ID_CODE, As2805WoolworthsUtils.packAiic(d.getPayCfg().getPaymentSwitch()) );

            msg.set(_041_CARD_ACCEPTOR_TERMINAL_ID, auditInfo.getTerminalId());
            msg.set(_042_CARD_ACCEPTOR_ID_CODE, As2805WoolworthsUtils.packMerchantId(auditInfo.getMerchantId()));

            if (registrationMsg) {
                // 'registration' logon (stan 0) must occur when switching keys, i.e. when DUKPT key sequence number is 1
                proto.setOriginalStan(0);
                msg.set(_011_SYS_TRACE_AUDIT_NUM, As2805WoolworthsUtils.packStan(0));
            } else {
                // else use 'normal' (non zero) stan
                proto.setOriginalStan(proto.getStan());
                msg.set(_011_SYS_TRACE_AUDIT_NUM, As2805WoolworthsUtils.packStan(proto.getStan()));
            }
            msg.set(_048_ADDITIONAL_DATA, pack170F48(d) );
            msg.set(_049_TRAN_CURRENCY_CODE, "036" );
            msg.set(_070_NMIC, "170"); // 3DES DUKPT only uses 170

            Timber.e( "message to send");
            Timber.e( msg.toString());
            return addMacAndEncrypt(trans, msg, msgType.msgEncrypted);

        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    private static byte[] packReconciliation(IDependency d, TransRec trans, MsgType msgType) {

        try {

            TProtocol proto = trans.getProtocol();
            TAudit auditinfo = trans.getAudit();
            Reconciliation r = reconciliationDao.findByTransId(trans.getUid());
            trans.setReconciliation(r);

            As2805Woolworths msg = new As2805Woolworths();

            msg.setMsgType(Iso8583.MsgType._0600_ADMIN_REQ);
            msg.putProcessingCode(As2805WoolworthsUtils.packProcCode(trans)); // will pack to 960000 (current settlement totals enquiry)
            msg.set(_011_SYS_TRACE_AUDIT_NUM, As2805WoolworthsUtils.packStan(proto.getStan()));
            msg.set(_032_ACQUIRING_INST_ID_CODE, As2805WoolworthsUtils.packAiic(d.getPayCfg().getPaymentSwitch()) );
            msg.set(_041_CARD_ACCEPTOR_TERMINAL_ID, auditinfo.getTerminalId());
            msg.set(_042_CARD_ACCEPTOR_ID_CODE, As2805WoolworthsUtils.packMerchantId(auditinfo.getMerchantId()));

            return addMacAndEncrypt(trans, msg, msgType.msgEncrypted);

        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static byte[] packFileUpdate(IDependency d, TransRec trans, As2805WoolworthsProto.FileUpdate fileUpdate, MsgType msgType) {

        try {
            TProtocol proto = trans.getProtocol();
            TAudit auditinfo = trans.getAudit();
            PaymentSwitch paySwitchCfg = d.getPayCfg().getPaymentSwitch();
            As2805Woolworths msg = new As2805Woolworths();

            msg.setMsgType(Iso8583.MsgType._0300_ACQUIRER_FILE_UPDATE_REQ);

            msg.set(_011_SYS_TRACE_AUDIT_NUM, As2805WoolworthsUtils.packStan(proto.getStan()));
            msg.set(_032_ACQUIRING_INST_ID_CODE, As2805WoolworthsUtils.packAiic(paySwitchCfg) );

            msg.set(_041_CARD_ACCEPTOR_TERMINAL_ID, auditinfo.getTerminalId());
            msg.set(_042_CARD_ACCEPTOR_ID_CODE, As2805WoolworthsUtils.packMerchantId(auditinfo.getMerchantId()));
            msg.set(_048_ADDITIONAL_DATA, "0001");
            msg.set(_070_NMIC, "151");
            msg.set(_071_MESSAGE_NUMBER, String.format( "%04d", fileUpdate.msgNumber));
            msg.set(_091_FILE_UPDATE_CODE, "4");
            msg.set(_101_FILE_NAME, tableName);

            return addMacAndEncrypt(trans, msg, msgType.msgEncrypted);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * Lists request-response of each message used in Woolies
     * */
    public enum MsgType {
        PREAUTH( _0100_AUTH_REQ, _0110_AUTH_REQ_RSP, true ),
        AUTH( _0200_TRAN_REQ , _0210_TRAN_REQ_RSP, true ),
        ADVICE( _0220_TRAN_ADV, _0230_TRAN_ADV_RSP, true ),
        NETWORK( _0800_NWRK_MNG_REQ, _0810_NWRK_MNG_REQ_RSP, false ),
        REVERSAL( _0420_ACQUIRER_REV_ADV, _0430_ACQUIRER_REV_ADV_RSP, true ),
        RECONCILIATION( _0600_ADMIN_REQ, _0610_ADMIN_REQ_RSP, true ),
        RECONCILIATION_DETAILS( _0520_ACQUIRER_RECONCILE_ADV, _0530_ACQUIRER_RECONCILE_ADV_RSP, true ),
        UPDATE( _0300_ACQUIRER_FILE_UPDATE_REQ, _0310_ACQUIRER_FILE_UPDATE_RSP, true );

        private final int sendMsgId;
        private final int receiveMsgId;
        private final boolean msgEncrypted; // true if message contents are encrypted

        MsgType( int sendMsgId, int receiveMsgId, boolean msgEncrypted ) {
            this.sendMsgId = sendMsgId;
            this.receiveMsgId = receiveMsgId;
            this.msgEncrypted = msgEncrypted;
        }

        public int getSendMsgId() {
            return this.sendMsgId;
        }

        public int getReceiveMsgId() {
            return this.receiveMsgId;
        }
    }

    /**
     * Unpack results for {@link As2805WoolworthsPack#unpack(IDependency, byte[], TransRec, As2805WoolworthsProto.ResponseAction, MsgType)}
     * */
    public enum UnPackResult{
        UNPACK_OK,              // Success
        MAC_ERROR,              // Mac Field couldn't be verified/failed
        VERIFICATION_FAILED,    // Fields unpacked don't match the fields packed
        GENERIC_FAILURE         // Catch-all failure. If seen a lot of times, needs to be expanded
    }


}
