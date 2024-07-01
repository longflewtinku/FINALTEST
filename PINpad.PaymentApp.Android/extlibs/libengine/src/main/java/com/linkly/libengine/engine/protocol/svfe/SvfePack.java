package com.linkly.libengine.engine.protocol.svfe;

import static com.linkly.libengine.engine.protocol.svfe.SvfePack.MSGTYPE.AUTH;
import static com.linkly.libengine.engine.protocol.svfe.SvfePack.MSGTYPE.BATCH_UPLOAD;
import static com.linkly.libengine.engine.protocol.svfe.SvfePack.MSGTYPE.NETWORK;
import static com.linkly.libengine.engine.protocol.svfe.SvfePack.MSGTYPE.RECONCILIATION;
import static com.linkly.libengine.engine.protocol.svfe.SvfePack.MSGTYPE.RECONCILIATION_TRAILER;
import static com.linkly.libengine.engine.protocol.svfe.SvfePack.MSGTYPE.REVERSAL;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.MessageStatus.FINALISED;
import static com.linkly.libsecapp.IP2PEncrypt.ElementType.CVV;
import static com.linkly.libsecapp.IP2PEncrypt.ElementType.EXPIRY_YYMM;
import static com.linkly.libsecapp.IP2PEncrypt.ElementType.EXPIRY_YYMMDD_CHIP;
import static com.linkly.libsecapp.IP2PEncrypt.ElementType.PAN;
import static com.linkly.libsecapp.IP2PEncrypt.ElementType.TRACK_2_FULL_MSR;
import static com.linkly.libsecapp.IP2PEncrypt.ElementType.TRACK_2_SHORT_FORMAT;
import static com.linkly.libsecapp.IP2PEncrypt.EncryptForStorageType.SHORT_TRACK_FORMAT;
import static com.linkly.libsecapp.IP2PEncrypt.PackFormat.ASCII;

import com.linkly.libengine.dependencies.IDependency;
import com.linkly.libengine.engine.Engine;
import com.linkly.libengine.engine.protocol.svfe.openisoj.Iso8583;
import com.linkly.libengine.engine.protocol.svfe.openisoj.Iso8583Rev93;
import com.linkly.libengine.engine.protocol.svfe.openisoj.Iso8583Svfe;
import com.linkly.libengine.engine.protocol.svfe.openisoj.Iso8583TermApp;
import com.linkly.libengine.engine.protocol.svfe.openisoj.Iso8583TermApp.Bit;
import com.linkly.libengine.engine.protocol.svfe.openisoj.exceptions.UnknownFieldException;
import com.linkly.libengine.engine.transactions.TransRec;
import com.linkly.libengine.engine.transactions.properties.TAmounts;
import com.linkly.libengine.engine.transactions.properties.TAudit;
import com.linkly.libengine.engine.transactions.properties.TCard;
import com.linkly.libengine.engine.transactions.properties.TProtocol;
import com.linkly.libengine.engine.transactions.properties.TReconciliationFigures;
import com.linkly.libengine.engine.transactions.properties.TSec;
import com.linkly.libengine.overrides.CoreOverrides;
import com.linkly.libmal.global.util.Util;
import com.linkly.libsecapp.CardholderDataElement;
import com.linkly.libsecapp.EncryptResult;
import com.linkly.libsecapp.IP2PEncrypt;
import com.linkly.libsecapp.IP2PSec;
import com.linkly.libsecapp.P2PLib;

import timber.log.Timber;

public class SvfePack {
    private static final String TAG = "SvfePack";

    public static final char substValCvv = 'U';
    private static final char substValTrack2Msr = 'W';
    private static final char substValTrack2Pan = 'X';
    private static final char substValTrack2Short = 'Y';

    public static final boolean p2peEncryptEnabled = false;

    // p2pe sensitive authentication data element definition - for msg encryption by p2pe app
    private static CardholderDataElement[] elements = null;

    private static P2PLib p2pInstance = P2PLib.getInstance();
    private static IP2PEncrypt p2pEncrypt = p2pInstance.getIP2PEncrypt();

    public static byte[] pack(IDependency d, TransRec trans, MSGTYPE msgType) {
        boolean cardDataStashed = false;

        byte[] result = null;
        try {
            // erase any cardholder data element configured previously. will be overridden below if cardholder data is present in message
            elements = new CardholderDataElement[1];
            elements[0] = null;

            if (msgType == AUTH) {
                result = packAuth(d, trans);
            }
            if (msgType == BATCH_UPLOAD) {
                // save card data for 'main' transaction as batch upload code will replace it in the p2pe module
                cardDataStashed = true;
                p2pEncrypt.stash();
                result = packBatchUpload(d, trans);
            }
            if (msgType == NETWORK) {
                result = packNetwork(trans);
            }
            if (msgType == REVERSAL) {
                // save card data for 'main' transaction as reversal code will replace it in the p2pe module
                cardDataStashed = true;
                p2pEncrypt.stash();
                result = packReversal(d, trans);
            }
            if (msgType == RECONCILIATION) {
                result = packReconciliation(trans, false);
            }
            if (msgType == RECONCILIATION_TRAILER) {
                result = packReconciliation(trans, true);
            }

            if (result == null) {
                throw new IllegalStateException();
            }

            result = encryptMessage( result );

            if( cardDataStashed ) {
                // restore card data for 'main' transaction
                p2pEncrypt.unstash();
            }
            return result;

        } catch (Exception e) {

            /* if an auth has failed to save a reversal mac it will not send the auth */
            Timber.i( "Transaction will not pack so we can't have it stuck in the batch");
            trans.debug();
            Timber.w(e);
            trans.getProtocol().setMessageStatus(FINALISED);
            trans.save();

            if( cardDataStashed ) {
                // restore card data for 'main' transaction
                p2pEncrypt.unstash();
            }

            throw e;
        }

    }


    /* must unpack the data completely before committing it to the transaction, so we dont get half the details */
    /* check the mac etc */
    public static boolean unpack(byte[] responseData, TransRec trans) {

        Iso8583Svfe resp = null;
        try {
            if (responseData != null) {
                resp = new Iso8583Svfe(responseData);
                if (resp != null) {


                    Timber.i( resp.toString());


                    String rrn = resp.get(Bit._037_RET_REF_NR);
                    trans.getProtocol().setRRN(rrn);

                    String actionCode = resp.get(Bit._039_ACTION_CODE);

                    String responseCode = SvfeRspCodeMap.getResponseCode(actionCode);
                    trans.getProtocol().setServerResponseCode(responseCode);

                    trans.getProtocol().setErrorCode(actionCode);
                    trans.getProtocol().setAdditionalResponseText(SvfeRspCodeMap.getResponseCodeError(actionCode));

                    String authCode = resp.get(Bit._038_APPROVAL_CODE);
                    if (authCode != null && authCode.length() > 0) {
                        trans.getProtocol().setAuthCode(authCode);
                    }
                    if (CoreOverrides.get().isSpoofComms()) {
                        trans.getProtocol().setServerResponseCode("00");
                        trans.getProtocol().setAuthCode("123456");
                    }

                    String field55 = resp.get(Bit._055_ICC_DATA);
                    if (field55 != null) {
                        SvfeUtils.unpackIccData(trans, field55);
                    }

                    /* check the terminal ids match */
                    if ((trans.getAudit().getTerminalId().compareTo(resp.get(Bit._041_TERMINAL_ID)) == 0)) {
                        Timber.i( "Terminal IDs match");
                        return true;
                    }

                }
            }
        } catch (Exception e) {
            Timber.w(e);
        }
        return false;
    }

    // get expiry or other 'non sensitive' card data
    public static String getNonSensitiveElement( IP2PEncrypt.ElementType element ) {
        int dataLen = p2pEncrypt.getElementLength( element );
        if( dataLen <= 0 ) {
            Timber.i( "WARNING - addSensitiveElement element " + element.name() + " not found, skipping" );
            return null;
        }

        return p2pEncrypt.getData( element );
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
    private static CardholderDataElement findElementPlaceholder( byte[] dataToSearch, int searchFromIdx, IP2PEncrypt.ElementType elementType, char charToFind ) {
        int elementLength = p2pEncrypt.getElementLength( elementType );
        // data not found in p2pe module SAD
        if( elementLength <= 0 )
            return null;

        // look for a run of at least 'elementLength' bytes, remember it's index
        for( int idx = searchFromIdx; idx<dataToSearch.length; idx++ ) {

            if( dataToSearch[idx] == (byte)charToFind ) {
                int runCount = 0;

                // see if there's a run of elementLength bytes here
                for( runCount=0; (runCount < elementLength) && ((idx+runCount) < dataToSearch.length); runCount++ ) {
                    if( dataToSearch[idx+runCount] != (byte)charToFind )
                        break;
                }

                // if we found a run of elementLength
                if( runCount == elementLength ) {
                    // record idx in a new CardholderDataElement object
                    return new CardholderDataElement( elementType, false, idx, elementLength, ASCII );
                }
            }
        }

        // else we didn't find
        return null;
    }

    public static byte[] encryptMessage( byte[] msg ) {

        IP2PEncrypt.EncryptParameters encryptParameters = null;
        if( p2peEncryptEnabled ) {
            // encryption enabled, enable padding and dukpt algorithm
            encryptParameters = new IP2PEncrypt.EncryptParameters(IP2PEncrypt.PaddingAlgorithm.RIGHT_ZEROS, IP2PEncrypt.EncryptAlgorithm.DUKPT_ANSI_2009, 1, IP2PEncrypt.IVToUse.USE_RANDOM_IV, null);
        } else {
            // perform p2pe message encryption - no encryption just yet (for testing)
            encryptParameters = new IP2PEncrypt.EncryptParameters(IP2PEncrypt.PaddingAlgorithm.NONE, IP2PEncrypt.EncryptAlgorithm.NONE, 1);
        }

        byte[] cleartextBlock = null;
        if( p2peEncryptEnabled ) {
            // cleartext block format:
            // 2 length bytes - big endian <- add this
            // message payload <- add this
            // optional zero padding <- don't add - added by p2pe module

            // pre-pend length bytes
            cleartextBlock = new byte[ msg.length + 2 ];

            // length bytes
            Util.short2ByteArray( (short)msg.length, cleartextBlock, 0 );
            // copy message payload
            System.arraycopy( msg, 0, cleartextBlock, 2, msg.length );

        } else {
            // if no encryption enabled, the message isn't encapsulated in encryption block etc
            cleartextBlock = msg;
        }

        // const value really - skip MTI (4 bytes) and 8 bytes of bitmap as this can contain binary data and could get a false match
        int replaceFromOffset = p2peEncryptEnabled ? 14 : 12;

        // scan message for fields
        CardholderDataElement cvvElement = findElementPlaceholder( cleartextBlock, replaceFromOffset, CVV, substValCvv );
        CardholderDataElement track2MsrElement = findElementPlaceholder( cleartextBlock, replaceFromOffset, TRACK_2_FULL_MSR, substValTrack2Msr );
        CardholderDataElement panElement = findElementPlaceholder( cleartextBlock, replaceFromOffset, PAN, substValTrack2Pan );
        CardholderDataElement track2ShortElement = findElementPlaceholder( cleartextBlock, replaceFromOffset, TRACK_2_SHORT_FORMAT, substValTrack2Short );

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

        elements = new CardholderDataElement[numElements];

        int elementNo = 0;
        if( cvvElement != null )
            elements[elementNo++] = cvvElement;
        if( track2MsrElement != null )
            elements[elementNo++] = track2MsrElement;
        if( panElement != null )
            elements[elementNo++] = panElement;
        if( track2ShortElement != null )
            elements[elementNo++] = track2ShortElement;

        // do the encryption
        EncryptResult encryptResult = p2pEncrypt.encrypt(cleartextBlock, encryptParameters, elements);
        if (encryptResult == null)
            return null;

        byte[] result = null;
        if( p2peEncryptEnabled ) {
            // msg to send format:
            // 2 bytes length <- added by calling function - don't add here
            // KSN - 10 bytes <- add this
            // IV - 8 bytes <- add this
            // encrypted block <- add this

            // alloc result buffer
            result = new byte[ encryptResult.getEncryptedMessage().length + 18 ];
            System.arraycopy( encryptResult.getDukptKsn(), 0, result, 0, 10 );
            System.arraycopy( encryptResult.getIv(), 0, result, 10, 8 );
            System.arraycopy( encryptResult.getEncryptedMessage(), 0, result, 18, encryptResult.getEncryptedMessage().length );

        } else {
            // p2pe disabled, return the cleartext message
            result = encryptResult.getEncryptedMessage();
        }

        return result;
    }

    private static boolean packCardData(Iso8583Svfe msg, TransRec trans, boolean useSavedCardDetails) throws UnknownFieldException {
        TCard cardinfo = trans.getCard();
        IP2PEncrypt.ElementType track2Element = null;
        char track2SubstChar = 0;

        // if we need to use stored card data, load it into the p2pe module
        if ( useSavedCardDetails ) {
            if( Util.isNullOrEmpty(trans.getSecurity().getEncTrack2()) )
                return false;

            // we have encrypted data, load it into p2pe module from storage
            if( !p2pEncrypt.decryptFromStorage(Util.hexStringToByteArray(trans.getSecurity().getEncTrack2()), SHORT_TRACK_FORMAT) )
                return false;

            // use short format track 2
            track2Element = TRACK_2_SHORT_FORMAT;
            track2SubstChar = substValTrack2Short;
        } else {
            // use long format track 2 (if we have it)
            track2Element = TRACK_2_FULL_MSR;
            track2SubstChar = substValTrack2Msr;
        }

        // if this was a manual pan transaction, and we have required data
        if( cardinfo.isManual() && p2pEncrypt.getElementLength( PAN ) > 0 ) {

            // get expiry length - use long format expiry if we have it
            int expLen = p2pEncrypt.getElementLength(EXPIRY_YYMMDD_CHIP);
            if( expLen <= 0 ) {
                // short format - sourced from manual entry or swipe
                expLen = p2pEncrypt.getElementLength(EXPIRY_YYMM);
            }

            // return error if no expiry found - shouldn't happen
            if( expLen <= 0 )
                return false;

            // pack pan and expiry if we have them
            msg.set(Bit._002_PAN, SvfeUtils.packSensitiveField( PAN, substValTrack2Pan ));

            // if expiry is 4 digits only (YYMM), then append 2 'day' digits, assuming expires at end of month
            if( expLen == 4 ) {
                String exp = getNonSensitiveElement( EXPIRY_YYMM );

                // expiry is only 4 digits, needs DD appended
                exp = exp + String.format("%02d", Util.getLastDayOfMonth(exp));

                msg.set(Bit._014_EXPIRY_DATE, exp );
            } else {
                String exp = getNonSensitiveElement( EXPIRY_YYMMDD_CHIP );

                // else if expLen != 4, assume it's 6 and sourced from chip, and already has DD appended
                msg.set(Bit._014_EXPIRY_DATE, exp );
            }

        } else if (p2pEncrypt.getElementLength(track2Element) > 0) {
            // else it's msr/emv/ctls
            msg.set(Bit._035_TRACK_2_DATA, SvfeUtils.packSensitiveField( track2Element, track2SubstChar ) );
        } else {
            // else we don't have required card data, return error
            return false;
        }
        return true;
    }

    private static byte[] packAuth(IDependency d, TransRec trans) {

        try {

            TProtocol proto = trans.getProtocol();
            TSec secinfo = trans.getSecurity();
            TCard cardinfo = trans.getCard();
            TAmounts amounts = trans.getAmounts();
            TAudit auditinfo = trans.getAudit();

            Iso8583Svfe msg = new Iso8583Svfe();

            DebugKeys();

            proto.setOriginalStan(proto.getStan());
            proto.setOriginalMessageType(200);
            msg.setMsgType(Iso8583.MsgType._0200_TRAN_REQ);

            msg.putProcessingCode(SvfeUtils.packProcCode(trans));
            msg.setTransactionAmount(amounts.getTotalAmount());
            trans.getAudit().setLastTransmissionDateTime(msg.getTransmissionDateTime().setNow());
            proto.setOriginalTransmissionDateTime(trans.getAudit().getLastTransmissionDateTime());
            msg.set(Bit._007_TRAN_DATE_TIME, trans.getAudit().getLastTransmissionDateTimeAsString("MMddhhmmss"));

            msg.set(Bit._011_SYS_TRACE_AUDIT_NUM, SvfeUtils.packStan(proto.getStan()));
            msg.set(Bit._012_LOCAL_TRAN_DATETIME, SvfeUtils.packLocalDateTime(trans));
            msg.set(Bit._022_POS_DATA_CODE, SvfeUtils.packPosEntryMode(trans));
            msg.set(Bit._024_FUNC_CODE, "200");
            msg.set(Bit._025_MSG_REASON_CODE, "00");

            if (!packCardData(msg, trans, false) ) {
                return null;
            }

            if (!Util.isNullOrEmpty(proto.getRRN()) && trans.isCompletion())
                msg.set(Bit._037_RET_REF_NR, proto.getRRN());
            msg.set(Bit._041_TERMINAL_ID, auditinfo.getTerminalId());
            msg.set(Bit._042_CARD_ACCEPTOR_ID, SvfeUtils.packMerchantId(auditinfo.getMerchantId()));
            msg.set(Bit._048_PRIVATE_ADDITIONAL_DATA, SvfeUtils.packAdditionalData(trans, false));
            msg.set(Bit._049_TRAN_CURRENCY_CODE, amounts.getCurrency());
            msg.set(Bit._052_PIN_DATA, SvfeUtils.packPinBlock(!p2peEncryptEnabled));


            if (/* trans.getAmounts().getTip() > 0 || */trans.getAmounts().getCashbackAmount() > 0) {
                msg.set(Iso8583TermApp.Bit._054_ADDITIONAL_AMOUNTS, SvfeUtils.packAdditionalAmounts(trans));
            }

            msg.set(Iso8583Rev93.Bit._055_ICC_DATA, SvfeUtils.packIccData(d, trans, msg));

            Timber.i( msg.toString());

            return msg.toMsg();
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static Iso8583Svfe packReversalFields(IDependency d, TransRec trans, int stan) {

        Iso8583Svfe msg = new Iso8583Svfe();

        TProtocol proto = trans.getProtocol();
        TCard cardinfo = trans.getCard();
        TAmounts amounts = trans.getAmounts();
        TAudit auditinfo = trans.getAudit();

        // For acquirers that need to have the original STAN (so RRN as well) as the transaction to be reversed
        if (d.getPayCfg().isIncludedOrginalStandInRec() || d.getPayCfg().isReversalCopyOriginal()) {
            stan = trans.getProtocol().getOriginalStan();
        }

        try {

            msg.setMsgType(Iso8583.MsgType._0400_ACQUIRER_REV_REQ);

            msg.putProcessingCode(SvfeUtils.packProcCode(trans));
            msg.setTransactionAmount(amounts.getTotalAmount());
            msg.getTransmissionDateTime().setNow(); /* note we dont update the transmission date time, as the reversal would then have a reversal date time of transmission if it was resent */
            msg.set(Bit._011_SYS_TRACE_AUDIT_NUM, SvfeUtils.packStan(stan));
            msg.set(Bit._012_LOCAL_TRAN_DATETIME, SvfeUtils.packReversalDateTime(d, trans)); /* possibly wrong if resending */

            msg.set(Bit._022_POS_DATA_CODE, SvfeUtils.packPosEntryMode(trans));
            msg.set(Bit._024_FUNC_CODE, "400");
            msg.set(Bit._025_MSG_REASON_CODE, "00");

            if (!packCardData(msg, trans, true) ) {
                return null;
            }

            msg.set(Bit._037_RET_REF_NR, SvfeUtils.packRetRefNumber(trans));
            msg.set(Bit._041_TERMINAL_ID, auditinfo.getTerminalId());
            msg.set(Bit._042_CARD_ACCEPTOR_ID, SvfeUtils.packMerchantId(auditinfo.getMerchantId()));
            msg.set(Bit._048_PRIVATE_ADDITIONAL_DATA, SvfeUtils.packAdditionalData(trans, true));
            msg.set(Bit._049_TRAN_CURRENCY_CODE, amounts.getCurrency());

            if (/*trans.getAmounts().getTip() > 0 || */trans.getAmounts().getCashbackAmount() > 0) {
                msg.set(Iso8583TermApp.Bit._054_ADDITIONAL_AMOUNTS, SvfeUtils.packAdditionalAmounts(trans));
            }

            msg.set(Iso8583Rev93.Bit._055_ICC_DATA,  SvfeUtils.packIccData(d, trans, msg));

            return msg;
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static byte[] packBatchUpload(IDependency d, TransRec trans) {

        try {

            TProtocol proto = trans.getProtocol();
            TSec secinfo = trans.getSecurity();
            TCard cardinfo = trans.getCard();
            TAmounts amounts = trans.getAmounts();
            TAudit auditinfo = trans.getAudit();

            Iso8583Svfe msg = new Iso8583Svfe();

            DebugKeys();

            msg.setMsgType(Iso8583.MsgType._0320_ACQUIRER_FILE_UPDATE_ADV);
            msg.putProcessingCode(SvfeUtils.packProcCode(trans));
            msg.setTransactionAmount(amounts.getTotalAmount());
            msg.set(Bit._007_TRAN_DATE_TIME, TAudit.getDateTimeAsString("MMddhhmmss", msg.getTransmissionDateTime().setNow(), "GMT"));
            msg.set(Bit._011_SYS_TRACE_AUDIT_NUM, SvfeUtils.packStan(proto.getStan()));
            msg.set(Bit._012_LOCAL_TRAN_DATETIME, SvfeUtils.packLocalDateTime(trans));
            msg.set(Bit._022_POS_DATA_CODE, SvfeUtils.packPosEntryMode(trans));
            msg.set(Bit._024_FUNC_CODE, "200");
            msg.set(Bit._025_MSG_REASON_CODE, "00");

            if (!packCardData(msg, trans, true) ) {
                return null;
            }

            msg.set(Bit._037_RET_REF_NR, SvfeUtils.packRetRefNumber(trans));
            msg.set(Bit._041_TERMINAL_ID, auditinfo.getTerminalId());
            msg.set(Bit._042_CARD_ACCEPTOR_ID, SvfeUtils.packMerchantId(auditinfo.getMerchantId()));
            msg.set(Bit._049_TRAN_CURRENCY_CODE, amounts.getCurrency());
            msg.set(Bit._052_PIN_DATA, SvfeUtils.packPinBlock(!p2peEncryptEnabled));



            if (/* trans.getAmounts().getTip() > 0 || */trans.getAmounts().getCashbackAmount() > 0) {
                msg.set(Iso8583TermApp.Bit._054_ADDITIONAL_AMOUNTS, SvfeUtils.packAdditionalAmounts(trans));
            }

            msg.set(Iso8583Rev93.Bit._055_ICC_DATA, SvfeUtils.packIccData(d, trans, msg));

            Timber.i( msg.toString());

            return msg.toMsg();
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static byte[] packReversal(IDependency d, TransRec trans) {

        try {

            DebugKeys();
            Iso8583Svfe msg = packReversalFields(d, trans, trans.getProtocol().getStan());

            if (msg == null)
                return null;

            Timber.i( msg.toString());

            return msg.toMsg();

        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            Timber.w(e);
            throw new RuntimeException(e);
        }
    }

    private static byte[] packNetwork(TransRec trans) {

        try {

            TProtocol proto = trans.getProtocol();
            TAudit auditinfo = trans.getAudit();

            Iso8583Svfe msg = new Iso8583Svfe();

            proto.setOriginalMessageType(800);
            proto.setOriginalStan(proto.getStan());
            proto.setOriginalTransmissionDateTime(trans.getAudit().getTransDateTime());

            msg.setMsgType(Iso8583.MsgType._0800_NWRK_MNG_REQ);

            msg.putProcessingCode(SvfeUtils.packProcCode(trans));
            trans.getAudit().setLastTransmissionDateTime(msg.getTransmissionDateTime().setNow());
            msg.set(Bit._007_TRAN_DATE_TIME, trans.getAudit().getLastTransmissionDateTimeAsString("MMddhhmmss"));
            msg.set(Bit._011_SYS_TRACE_AUDIT_NUM, SvfeUtils.packStan(proto.getStan()));
            msg.set(Bit._024_FUNC_CODE, "831");
            msg.set(Bit._041_TERMINAL_ID, auditinfo.getTerminalId());
            msg.set(Bit._042_CARD_ACCEPTOR_ID, SvfeUtils.packMerchantId(auditinfo.getMerchantId()));
            Timber.i( msg.toString());

            return msg.toMsg();
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static byte[] packReconciliation(TransRec trans, boolean trailer) {

        try {

            TProtocol proto = trans.getProtocol();
            TAudit auditinfo = trans.getAudit();
            TReconciliationFigures reconcFigures = trans.getReconciliation().getReconciliationFigures();
            Iso8583Svfe msg = new Iso8583Svfe();

            DebugKeys();
            msg.setMsgType(Iso8583.MsgType._0520_ACQUIRER_RECONCILE_ADV);
            msg.getTransmissionDateTime().setNow(); /* note we dont update the transmission date time, as the rversal would then have a reversal date time of transmission if it was resent */

            if (trailer) {
                msg.putProcessingCode(SvfeUtils.packProcCode(trans));
            }
            msg.set(Bit._011_SYS_TRACE_AUDIT_NUM, SvfeUtils.packStan(proto.getStan()));
            msg.set(Iso8583.Bit._015_SETTLEMENT_DATE, SvfeUtils.packReconciliationDate(trans)); /* possibly wrong if resending */
            msg.set(Bit._024_FUNC_CODE, Iso8583Rev93.FuncCode._504_RECONCILIATION_ADVICE);
            msg.set(Bit._041_TERMINAL_ID, auditinfo.getTerminalId());
            msg.set(Bit._042_CARD_ACCEPTOR_ID, SvfeUtils.packMerchantId(auditinfo.getMerchantId()));

            // amount, net reconciliation - need to add "sign" (C or D)
            if (!trailer) {
                long netReconcAmount = reconcFigures.getNetReconciliationAmount();
                String sign = "C";
                if (netReconcAmount < 0) {
                    sign = "D";
                    netReconcAmount = -netReconcAmount;
                }
                msg.set(Iso8583.Bit._005_SETTLE_AMOUNT, String.format("%s%012d", sign, netReconcAmount));
            }
            Timber.i( msg.toString());

            return msg.toMsg();

        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    private static void DebugKeys() {
        if (P2PLib.getInstance().getIP2PSec().getInstalledKeyType() == IP2PSec.InstalledKeyType.DUKPT) {
            Timber.i( "PACK BatchUpload with DUKPT keys");
        }
    }

    public enum MSGTYPE {AUTH, NETWORK, REVERSAL, RECONCILIATION, RECONCILIATION_TRAILER, BATCH_UPLOAD, RECONCILIATION_DETAILS}

}
