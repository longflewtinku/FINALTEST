package com.linkly.libengine.engine.protocol.iso8583;

import static com.linkly.libengine.engine.customers.ICustomer.PCI_FORMAT.POST_TRANS;
import static com.linkly.libengine.engine.protocol.iso8583.As2805SuncorpPack.MSGTYPE.ADVICE;
import static com.linkly.libengine.engine.protocol.iso8583.As2805SuncorpPack.MSGTYPE.AUTH;
import static com.linkly.libengine.engine.protocol.iso8583.As2805SuncorpPack.MSGTYPE.NETWORK;
import static com.linkly.libengine.engine.protocol.iso8583.As2805SuncorpPack.MSGTYPE.RECONCILIATION;
import static com.linkly.libengine.engine.protocol.iso8583.As2805SuncorpPack.MSGTYPE.REVERSAL;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.As2805Suncorp.Bit._047_ADDITIONAL_DATA_NATIONAL;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.As2805Suncorp.Bit._055_ICC_DATA;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.As2805Suncorp.Bit._057_CASH_AMOUNT;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.As2805Suncorp.Bit._060_ADDITIONAL_PRIVATE;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.As2805Suncorp.Bit._062_ADDITIONAL_PRIVATE;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.As2805Suncorp.Bit._064_MAC;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.As2805Suncorp.Bit._070_NMIC;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.As2805Suncorp.Bit._118_CASHOUTS_NUMBER;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.As2805Suncorp.Bit._119_CASHOUTS_AMOUNT;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.As2805Suncorp.Bit._128_MAC;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.As2805Suncorp.MsgType._0830_NWRK_MGMT_KEY_CHANGE_RESPONSE;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583.Bit._002_PAN;
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
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583.Bit._048_ADDITIONAL_DATA;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583.Bit._052_PIN_DATA;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583.Bit._054_ADDITIONAL_AMOUNTS;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583.Bit._066_SETTLEMENT_CODE;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583.Bit._074_CREDITS_NUMBER;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583.Bit._075_CREDITS_REVERSAL_NUMBER;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583.Bit._076_DEBITS_NUMBER;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583.Bit._077_DEBITS_REVERSAL_NUMBER;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583.Bit._078_TRANSFER_NUMBER;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583.Bit._079_TRANSFER_REVERSAL_NUMBER;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583.Bit._080_INQUIRIES_NUMBER;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583.Bit._081_AUTHORISATIONS_NUMBER;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583.Bit._086_CREDITS_AMOUNT;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583.Bit._087_CREDITS_REVERSAL_AMOUNT;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583.Bit._088_DEBITS_AMOUNT;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583.Bit._089_DEBITS_REVERSAL_AMOUNT;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583.Bit._090_ORIGINAL_DATA_ELEMENTS;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583.Bit._097_AMOUNT_NET_SETTLEMENT;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583.MsgType._0810_NWRK_MNG_REQ_RSP;
import static com.linkly.libengine.engine.reporting.ReconciliationManager.reconciliationDao;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.MessageStatus.FINALISED;
import static com.linkly.libsecapp.IP2PEncrypt.ElementType.CVV;
import static com.linkly.libsecapp.IP2PEncrypt.ElementType.EXPIRY_YYMM;
import static com.linkly.libsecapp.IP2PEncrypt.ElementType.EXPIRY_YYMMDD_CHIP;
import static com.linkly.libsecapp.IP2PEncrypt.ElementType.PAN;
import static com.linkly.libsecapp.IP2PEncrypt.ElementType.PIN_BLOCK;
import static com.linkly.libsecapp.IP2PEncrypt.ElementType.PIN_BLOCK_KSN;
import static com.linkly.libsecapp.IP2PEncrypt.ElementType.TRACK_2_FULL_MSR;
import static com.linkly.libsecapp.IP2PEncrypt.ElementType.TRACK_2_SHORT_FORMAT;
import static com.linkly.libsecapp.IP2PEncrypt.EncryptForStorageType.SHORT_TRACK_FORMAT;
import static com.linkly.libsecapp.IP2PEncrypt.PackFormat.ASCII;
import static com.linkly.libsecapp.IP2PEncrypt.PackFormat.BCD;

import com.linkly.libengine.config.paycfg.PaymentSwitch;
import com.linkly.libengine.dependencies.IDependency;
import com.linkly.libengine.engine.Engine;
import com.linkly.libengine.engine.protocol.iso8583.openisoj.As2805Suncorp;
import com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583;
import com.linkly.libengine.engine.protocol.iso8583.openisoj.exceptions.UnknownFieldException;
import com.linkly.libengine.engine.reporting.Reconciliation;
import com.linkly.libengine.engine.reporting.ReconciliationManager;
import com.linkly.libengine.engine.transactions.TransRec;
import com.linkly.libengine.engine.transactions.properties.TAmounts;
import com.linkly.libengine.engine.transactions.properties.TAudit;
import com.linkly.libengine.engine.transactions.properties.TCard;
import com.linkly.libengine.engine.transactions.properties.TProtocol;
import com.linkly.libengine.engine.transactions.properties.TReconciliationFigures;
import com.linkly.libengine.engine.transactions.properties.TSec;
import com.linkly.libengine.overrides.CoreOverrides;
import com.linkly.libmal.IMal;
import com.linkly.libmal.MalFactory;
import com.linkly.libmal.global.util.Util;
import com.linkly.libsecapp.CardholderDataElement;
import com.linkly.libsecapp.EncryptResult;
import com.linkly.libsecapp.IP2PEncrypt;
import com.linkly.libsecapp.IP2PSec;
import com.linkly.libsecapp.P2PLib;

import java.util.Arrays;
import java.util.Objects;

import timber.log.Timber;

public class As2805SuncorpPack {
    private static final String TAG = "As2805SuncorpPack";

    public static final char substValCvv = 'A';
    private static final char substValTrack2Msr = 'B';
    private static final char substValTrack2Pan = 'C';
    private static final char substValTrack2Short = 'D';
    public static final boolean p2peEncryptEnabled = false;

    // p2pe sensitive authentication data element definition - for msg encryption by p2pe app
    private static CardholderDataElement[] elements = null;

    public static byte[] pack(IDependency d, TransRec trans, MSGTYPE msgType) {

        byte[] result = null;
        try {
            if (msgType == AUTH) {
                result = packAuth(d,trans);
                // need to update the trans record as auth updates state values if a reversal is needed otherwise we will get hard crashes.
                trans.save();
            }
            if (msgType == ADVICE) {
                result = packAdvice(d,trans);
            }
            if (msgType == NETWORK) {
                result = packNetwork(d,trans);
            }
            if (msgType == REVERSAL) {
                result = packReversal(d,trans);
            }
            if (msgType == RECONCILIATION) {
                result = packReconciliation(d,trans);
            }



            result = encryptMessage(result);

        } catch (Exception e) {
            trans.getProtocol().setMessageStatus(FINALISED);
            trans.save();
            Timber.i( "Pack failed, remove from batch as useless");
            Timber.w(e);
            throw e;
        }
        return result;
    }


    /* must unpack the data completely before committing it to the transaction, so we dont get half the details */
    /* check the mac etc */
    public static boolean unpack(IDependency d, byte[] responseData, TransRec trans) {

        As2805Suncorp resp = null;
        try {
            if (responseData != null) {
                resp = new As2805Suncorp(responseData);

                As2805SuncorpRspCodeMap rspCodeMap = new As2805SuncorpRspCodeMap();
                Timber.i( resp.toString());

                String responseCode = resp.get(_039_RESPONSE_CODE);
                // this acquirer uses 2 digit repsonse codes which fit in both pos response code and server response code fields
                trans.getProtocol().setPosResponseCode(responseCode);
                trans.getProtocol().setServerResponseCode(responseCode);
                if( "08".equals( responseCode ) ){
                    trans.getProtocol().setSignatureRequired( true );
                }

                // set display and receipt text based off response code
                trans.getProtocol().setAdditionalResponseText(rspCodeMap.getResponseCodeErrorDisplay(responseCode));
                trans.getProtocol().setCardAcceptorPrinterData(rspCodeMap.getResponseCodeErrorReceipt(responseCode));
                trans.getProtocol().setPosResponseText(rspCodeMap.getResponseCodeErrorPos(responseCode));

                // Unpack date and time values for settlement and bank date times
                String settlementDate = resp.get(_015_SETTLEMENT_DATE);  // MMdd format
                if( settlementDate != null ) {
                    trans.getProtocol().setSettlementDate( settlementDate );

                    // for Suncorp, batch number and settlement date are the same thing, except Batch no is integer and settlement date is string. both YYMM format
                    trans.getProtocol().setBatchNumber( Integer.parseInt( settlementDate ) );
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
                    trans.getProtocol().setServerResponseCode("00");
                    trans.getProtocol().setAuthCode("123456");
                }

                String field55 = resp.get(_055_ICC_DATA);
                if (field55 != null) {
                    As2805SuncorpUtils.unpackIccData(trans, field55);
                }

                // set settlement code in trans rec if present
                String settlementCode = resp.get(_066_SETTLEMENT_CODE);
                if( !Util.isNullOrEmpty( settlementCode ) ) {
                    trans.getProtocol().setSettlementCode(settlementCode);
                }

                String f48Response = resp.get( _048_ADDITIONAL_DATA );
                if( !Util.isNullOrEmpty( f48Response ) ){

                    unpackField48(trans, f48Response, resp.getMsgType());
                }

                /* check the terminal ids match  */
                if ((trans.getAudit().getTerminalId().compareTo(resp.get(_041_CARD_ACCEPTOR_TERMINAL_ID)) == 0)) {
                    Timber.i( "Terminal IDs match");
                    return true;
                }
            }
        } catch (Exception e) {
            Timber.w(e);
        }
        return false;
    }

    private static void unpackField48_810(TransRec trans, String f48Response) {

        /*
            Fields are:
            4 bytes -> OWF(KEKn, PPASN)
            6 bytes -> Password. This is gonna be ignored
            2 bytes -> Year. This needs to be stored
            40 Bytes -> Advertising. This will be ignored again
            3 bytes -> STAN. Will be used to reset stan
        */

        byte[] field48 = Util.hexStringToByteArray(f48Response);
        final int F48LEN = 55;

        if(field48.length >= F48LEN) {
            byte[] owf = Arrays.copyOfRange(field48, 0, 4); // TODO: when we get keys
            byte[] password = Arrays.copyOfRange(field48, 4, 10); // Ignored
            // Our year value for applying the clock
            trans.getProtocol().setYear(Util.bcd2Str(Arrays.copyOfRange(field48, 10, 12)));
            byte[] advertising = Arrays.copyOfRange(field48, 12, 52); // Ignore
            // Our reset stan value
            trans.getProtocol().setResetStan(Integer.parseInt(Util.bcd2Str(Arrays.copyOfRange(field48, 52, 55))));
        } else {
            // If we have reached here either something has very wrong either invalid data etc.
            Timber.e( "Error invalid F48:" + f48Response);
        }
    }


    // Unpacks field 48 and applies it to the trans rec (If need be) based on the type of message
    // Annoyingly this varies depending on what message is being sent
    private static void unpackField48(TransRec trans, String f48Response, int type) {

        // Field 48 varies depending on the message
        switch(type) {
            case _0810_NWRK_MNG_REQ_RSP:
                unpackField48_810(trans, f48Response);
                break;

                // TODO: handle others once keys has been implemented
            case _0830_NWRK_MGMT_KEY_CHANGE_RESPONSE:
                Timber.i( "NYI");
                break;
            default:
                Timber.e( "Unhandled F48 response message: " + type);
                break;
        }
    }

    // get expiry or other 'non sensitive' card data
    public static String getNonSensitiveElement( IP2PEncrypt.ElementType element ) {
        // ok so issues where P2PLib is reinitialised. Previously this was a static value...
        // If secapp crashes/process death these would be invalid...
        // There is "IDependencies however these are static functions..." would need a major refactor from static/singleton to object based
        IP2PEncrypt p2pEncrypt = P2PLib.getInstance().getIP2PEncrypt();
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
    private static CardholderDataElement findElementPlaceholder(byte[] dataToSearch, int searchFromIdx, IP2PEncrypt.ElementType elementType, char charToFind, IP2PEncrypt.PackFormat packFormat ) {

        // ok so issues where P2PLib is reinitialised. Previously this was a static value...
        // If secapp crashes/process death these would be invalid...
        // There is "IDependencies however these are static functions..." would need a major refactor from static/singleton to object based
        IP2PEncrypt p2pEncrypt = P2PLib.getInstance().getIP2PEncrypt();

        int elementLength = p2pEncrypt.getElementLength( elementType );

        byte[] packedMsg;
        if( packFormat == BCD ) {
            // for suncorp, various card data fields are BCD packed. so convert input message for comparison from bcd to ascii
            packedMsg = Objects.requireNonNull(Util.hex2Str(dataToSearch)).getBytes();
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

    public static byte[] encryptMessage( byte[] msg ) {

        // ok so issues where P2PLib is reinitialised. Previously this was a static value...
        // If secapp crashes/process death these would be invalid...
        // There is "IDependencies however these are static functions..." would need a major refactor from static/singleton to object based
        IP2PEncrypt p2pEncrypt = P2PLib.getInstance().getIP2PEncrypt();

        IP2PEncrypt.EncryptParameters encryptParameters;
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
            com.linkly.libmal.global.util.Util.short2ByteArray( (short)msg.length, cleartextBlock, 0 );
            // copy message payload
            System.arraycopy( msg, 0, cleartextBlock, 2, msg.length );

        } else {
            // if no encryption enabled, the message isn't encapsulated in encryption block etc
            cleartextBlock = msg;
        }

        // const value really - skip MTI (2 bytes) and 8 bytes of bitmap as this can contain binary data and could get a false match
        int replaceFromOffset = p2peEncryptEnabled ? 12 : 8;

        // scan message for fields
        CardholderDataElement cvvElement = findElementPlaceholder( cleartextBlock, replaceFromOffset, CVV, substValCvv, ASCII );
        CardholderDataElement track2MsrElement = findElementPlaceholder( cleartextBlock, replaceFromOffset, TRACK_2_FULL_MSR, substValTrack2Msr, BCD );
        CardholderDataElement panElement = findElementPlaceholder( cleartextBlock, replaceFromOffset, PAN, substValTrack2Pan, BCD );
        CardholderDataElement track2ShortElement = findElementPlaceholder( cleartextBlock, replaceFromOffset, TRACK_2_SHORT_FORMAT, substValTrack2Short, BCD );

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

    private static boolean packCardData(As2805Suncorp msg, TransRec trans, boolean useSavedCardDetails) throws UnknownFieldException {

        // ok so issues where P2PLib is reinitialised. Previously this was a static value...
        // If secapp crashes/process death these would be invalid...
        // There is "IDependencies however these are static functions..." would need a major refactor from static/singleton to object based
        IP2PEncrypt p2pEncrypt = P2PLib.getInstance().getIP2PEncrypt();

        TCard cardinfo = trans.getCard();
        IP2PEncrypt.ElementType track2Element = null;
        char track2SubstChar = 0;

        // if we need to use stored card data, load it into the p2pe module
        if ( useSavedCardDetails ) {
            if( com.linkly.libmal.global.util.Util.isNullOrEmpty(trans.getSecurity().getEncTrack2()) )
                return false;

            // we have encrypted data, load it into p2pe module from storage
            p2pEncrypt.stash();
            if( !p2pEncrypt.decryptFromStorage(com.linkly.libmal.global.util.Util.hexStringToByteArray(trans.getSecurity().getEncTrack2()), SHORT_TRACK_FORMAT) ) {
                p2pEncrypt.unstash();
                return false;
            }
            p2pEncrypt.unstash();

            // use short format track 2
            track2Element = TRACK_2_SHORT_FORMAT;
            track2SubstChar = substValTrack2Short;
        } else {
            // use long format track 2 (if we have it)
            track2Element = TRACK_2_FULL_MSR;
            track2SubstChar = substValTrack2Msr;
        }

        // if this was a manual pan transaction, and we have required data
        if( ( cardinfo.isManual() || useSavedCardDetails ) && p2pEncrypt.getElementLength( PAN ) > 0 ) {

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
            msg.set(_002_PAN, As2805SuncorpUtils.packSensitiveField( PAN, substValTrack2Pan ));

            // if expiry is 4 digits only (YYMM), then append 2 'day' digits, assuming expires at end of month
            /*if( expLen == 4 ) {
                String exp = getNonSensitiveElement( EXPIRY_YYMM );

                // expiry is only 4 digits, needs DD appended
                exp = exp + String.format("%02d", com.linkly.libmal.global.util.Util.getLastDayOfMonth(exp));

                msg.set(com.linkly.libengine.engine.protocol.iso8583.openisoj.As2805Suncorp.Bit._014_EXPIRY_DATE, exp );
            } else */{
                String exp = getNonSensitiveElement( EXPIRY_YYMM );

                // else if expLen != 4, assume it's 6 and sourced from chip, and already has DD appended
                msg.set(_014_EXPIRATION_DATE, exp );
            }

        } else if (p2pEncrypt.getElementLength(track2Element) > 0) {
            // else it's msr/emv/ctls
            msg.set(_035_TRACK_2_DATA, As2805SuncorpUtils.packSensitiveField( track2Element, track2SubstChar ) );
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
            TAmounts amounts = trans.getAmounts();
            TAudit auditinfo = trans.getAudit();
            PaymentSwitch paySwitchCfg = d.getPayCfg().getPaymentSwitch();
            As2805Suncorp msg = new As2805Suncorp();

            if (trans.isPreAuth()) {
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

            msg.putProcessingCode(As2805SuncorpUtils.packProcCode(trans));
            msg.setTransactionAmount(amounts.getTotalAmount());

            msg.set(_011_SYS_TRACE_AUDIT_NUM, As2805SuncorpUtils.packStan(proto.getStan()));
            msg.set(_022_POS_ENTRY_MODE, As2805SuncorpUtils.packPosEntryMode(trans));
            msg.set(_023_CARD_SEQUENCE_NUM, As2805SuncorpUtils.packCardSeqNumber(trans));

            // pack DE 024 - NII only if provided (it's an optional field)
            As2805SuncorpUtils.packNii( paySwitchCfg, msg );

            msg.set(_025_POS_CONDITION_CODE, 42 ); // suncorp spec says always use 42
            msg.set(_032_ACQUIRING_INST_ID_CODE, As2805SuncorpUtils.packAiic(paySwitchCfg) );
            msg.set(_041_CARD_ACCEPTOR_TERMINAL_ID, auditinfo.getTerminalId());
            msg.set(_042_CARD_ACCEPTOR_ID_CODE, As2805SuncorpUtils.packMerchantId(auditinfo.getMerchantId()));

            msg.set(_047_ADDITIONAL_DATA_NATIONAL, As2805SuncorpUtils.packAdditionalDataNational47(trans) );

            P2PLib p2pInstance = P2PLib.getInstance();
            IP2PEncrypt p2pEncrypt = p2pInstance.getIP2PEncrypt();
            int pinBlockLen = p2pEncrypt.getElementLength(PIN_BLOCK);
            int pinBlockKsnLen = p2pEncrypt.getElementLength(PIN_BLOCK_KSN);

            if (pinBlockLen > 0 && pinBlockKsnLen > 0) {
                msg.set(_052_PIN_DATA, As2805SuncorpUtils.packPinBlock(Util.hexStringToByteArray(p2pEncrypt.getData(PIN_BLOCK))));
            }

            if (trans.getAmounts().getTip() > 0 ) {
                msg.set(_054_ADDITIONAL_AMOUNTS, As2805SuncorpUtils.packAdditionalAmounts(trans));
            }

            msg.set(_055_ICC_DATA, As2805SuncorpUtils.packIccData(trans, msg));

            // if any cash component (pwcb or cashout) is present, pack it here
            if( trans.getAmounts().getCashbackAmount() > 0 || trans.isCash()) {
                msg.set(_057_CASH_AMOUNT, As2805SuncorpUtils.packCashAmount(trans));
            }

            // TODO: confirm if we can exclude de 61 - product/service codes
//            msg.set(_061_ADDITIONAL_PRIVATE, "test private 61 data");

            // invoice number
            msg.set(_062_ADDITIONAL_PRIVATE, Util.padLeft(trans.getAudit().getReceiptNumber().toString(), 6, '0' ) );

            if (trans.getProtocol().isIncludeMac()) {
                msg.set(msg.isExtended() ? _128_MAC : _064_MAC, As2805SuncorpUtils.packMac(msg));
                secinfo.setKsn(Util.byteArrayToHexString(P2PLib.getInstance().getIP2PSec().getDUKPTKsn(IP2PSec.KeyGroup.TERM_GROUP)));
            }

            Timber.i( msg.toString());
            return msg.toMsg();
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static byte[] packAdvice(IDependency d, TransRec trans) {

        try {

            TProtocol proto = trans.getProtocol();
            TSec secinfo = trans.getSecurity();
            TAmounts amounts = trans.getAmounts();
            TAudit auditinfo = trans.getAudit();
            PaymentSwitch paySwitchCfg = d.getPayCfg().getPaymentSwitch();
            As2805Suncorp msg = new As2805Suncorp();

            proto.setOriginalMessageType(220);
            proto.setOriginalStan(proto.getStan());
            proto.setOriginalTransmissionDateTime(trans.getAudit().getTransDateTime());

            msg.setMsgType(Iso8583.MsgType._0220_TRAN_ADV);

            if (!packCardData(msg, trans, false) ) {
                return null;
            }

            msg.putProcessingCode(As2805SuncorpUtils.packProcCode(trans));
            msg.setTransactionAmount(amounts.getTotalAmount());

            msg.set(_011_SYS_TRACE_AUDIT_NUM, As2805SuncorpUtils.packStan(proto.getStan()));
            msg.set(_012_LOCAL_TRAN_TIME,As2805SuncorpUtils.packTransTime(trans, d.getPayCfg().getBankTimeZone()));
            msg.set(_013_LOCAL_TRAN_DATE,As2805SuncorpUtils.packTransDate(trans, d.getPayCfg().getBankTimeZone()));
            msg.set(_022_POS_ENTRY_MODE, As2805SuncorpUtils.packPosEntryMode(trans));
            msg.set(_023_CARD_SEQUENCE_NUM, As2805SuncorpUtils.packCardSeqNumber(trans));

            // pack NII only if provided (it's an optional field)
            As2805SuncorpUtils.packNii( paySwitchCfg, msg );

            msg.set(_025_POS_CONDITION_CODE, 42 ); // suncorp spec says always use 42
            msg.set(_032_ACQUIRING_INST_ID_CODE, As2805SuncorpUtils.packAiic(paySwitchCfg) );

            msg.set(_038_AUTH_ID_RESPONSE, As2805SuncorpUtils.packAuthCode(trans));
            msg.set(_041_CARD_ACCEPTOR_TERMINAL_ID, auditinfo.getTerminalId());
            msg.set(_042_CARD_ACCEPTOR_ID_CODE, As2805SuncorpUtils.packMerchantId(auditinfo.getMerchantId()));

            msg.set(_047_ADDITIONAL_DATA_NATIONAL, As2805SuncorpUtils.packAdditionalDataNational47(trans) );

            if (trans.getAmounts().getTip() > 0 || trans.getAmounts().getCashbackAmount() > 0) {
                msg.set(_054_ADDITIONAL_AMOUNTS, As2805SuncorpUtils.packAdditionalAmounts(trans));
            }

            msg.set(_055_ICC_DATA, As2805SuncorpUtils.packIccData(trans, msg));
            msg.set(_062_ADDITIONAL_PRIVATE, Util.padLeft(trans.getAudit().getReceiptNumber().toString(), 6, '0' ) );

            if (trans.getProtocol().isIncludeMac()) {
                secinfo.setKsn(Util.byteArrayToHexString(P2PLib.getInstance().getIP2PSec().getDUKPTKsn(IP2PSec.KeyGroup.TERM_GROUP)));

                msg.set(msg.isExtended() ? _128_MAC : _064_MAC, As2805SuncorpUtils.packMac(msg));
            }

            Timber.i( msg.toString());
            return msg.toMsg();

        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static As2805Suncorp packReversalImpl(IDependency d, TransRec trans, int stan) {

        As2805Suncorp msg = new As2805Suncorp();
        TProtocol proto = trans.getProtocol();
        TAmounts amounts = trans.getAmounts();
        TAudit auditinfo = trans.getAudit();
        PaymentSwitch paySwitchCfg = d.getPayCfg().getPaymentSwitch();

        // For acquirers that need to have the original STAN (so RRN as well) as the transaction to be reversed
        if (Engine.getDep().getPayCfg().isIncludedOrginalStandInRec() || Engine.getDep().getPayCfg().isReversalCopyOriginal()) {
            stan = trans.getProtocol().getOriginalStan();
        }

        try {
            msg.setMsgType(Iso8583.MsgType._0420_ACQUIRER_REV_ADV);

            if (Engine.getDep().getCustomer().wipePciSensitiveData() != POST_TRANS) {

                if (!packCardData(msg, trans, true) ) {
                    return null;
                }
            }

            msg.putProcessingCode(As2805SuncorpUtils.packProcCode(trans));
            msg.setTransactionAmount(amounts.getTotalAmount());
            msg.set(_011_SYS_TRACE_AUDIT_NUM, As2805SuncorpUtils.packStan(stan));
            msg.set(_022_POS_ENTRY_MODE, As2805SuncorpUtils.packPosEntryMode(trans));

            if (Engine.getDep().getCustomer().wipePciSensitiveData() != POST_TRANS) {
                msg.set(_023_CARD_SEQUENCE_NUM, As2805SuncorpUtils.packCardSeqNumber(trans));
            }

            // pack DE 024 - NII only if provided (it's an optional field)
            As2805SuncorpUtils.packNii( paySwitchCfg, msg );

            msg.set(_025_POS_CONDITION_CODE, 42 ); // suncorp spec says always use 42
            msg.set(_032_ACQUIRING_INST_ID_CODE, As2805SuncorpUtils.packAiic(paySwitchCfg) );

            msg.set(_037_RETRIEVAL_REF_NUM, As2805SuncorpUtils.packRetRefNumber(trans, d.getPayCfg()));
            msg.set(_041_CARD_ACCEPTOR_TERMINAL_ID, auditinfo.getTerminalId());
            msg.set(_042_CARD_ACCEPTOR_ID_CODE, As2805SuncorpUtils.packMerchantId(auditinfo.getMerchantId()));

            msg.set(_047_ADDITIONAL_DATA_NATIONAL, As2805SuncorpUtils.packAdditionalDataNational47(trans) );

            if (trans.getAmounts().getTip() > 0 || trans.getAmounts().getCashbackAmount() > 0) {
                msg.set(_054_ADDITIONAL_AMOUNTS, As2805SuncorpUtils.packAdditionalAmounts(trans));
            }

            msg.set(_055_ICC_DATA, As2805SuncorpUtils.packIccData(trans, msg));

            msg.set(_090_ORIGINAL_DATA_ELEMENTS, As2805SuncorpUtils.packOriginalDataElements(d,trans) );

            return msg;
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static byte[] packReversal(IDependency d, TransRec trans) {

        try {

            if (Engine.getDep().getP2PLib().getIP2PSec().getInstalledKeyType() == IP2PSec.InstalledKeyType.DUKPT) {
                Timber.i( "PACK Reversal with DUKPT keys");
            } else {
                Timber.i( "PACK Reversal with Master Session keys");
            }

            As2805Suncorp msg = packReversalImpl(d, trans, trans.getProtocol().getStan());

            if (trans.getProtocol().isIncludeMac()) {
              trans.getSecurity().setKsn(Util.byteArrayToHexString(P2PLib.getInstance().getIP2PSec().getDUKPTKsn(IP2PSec.KeyGroup.TERM_GROUP)));
              
                // TODO implement using P2Pe msg.set(_053_SECURITY_INFO, As2805SuncorpUtils.packKsn(trans.getSecurity().getKsn()));
                msg.set(msg.isExtended() ? _128_MAC : _064_MAC, As2805SuncorpUtils.packMac(msg));
            }

            Timber.i( msg.toString());
            return msg.toMsg();

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
    private static String packF48(IMal mal){

        byte[] sn = Util.StringToBcd(mal.getHardware().getSerialNumber(), 8);
        String Serial = Util.byteArrayToHexString(sn);

        // TODO: key data stuff.
        String kekFlag = "01";
        String PPASN = "00112233";
        String ACFNameVer = "11111111111";
        String checkSum = "5555";

        return Serial+kekFlag+PPASN+ACFNameVer+checkSum;
    }

    private static byte[] packNetwork(IDependency d, TransRec trans) {

        try {

            TProtocol proto = trans.getProtocol();
            TAudit auditInfo = trans.getAudit();

            As2805Suncorp msg = new As2805Suncorp();

            proto.setOriginalMessageType(804);
            proto.setOriginalStan(proto.getStan());
            proto.setOriginalTransmissionDateTime(trans.getAudit().getTransDateTime());
            trans.setSoftwareVersion(d.getPayCfg().getPaymentAppVersion());

            msg.setMsgType(Iso8583.MsgType._0800_NWRK_MNG_REQ);
            msg.set(_011_SYS_TRACE_AUDIT_NUM, As2805SuncorpUtils.packStan(proto.getStan()));
            msg.set(_032_ACQUIRING_INST_ID_CODE, As2805SuncorpUtils.packAiic(d.getPayCfg().getPaymentSwitch()) );

            msg.set(_041_CARD_ACCEPTOR_TERMINAL_ID, auditInfo.getTerminalId());
            msg.set(_042_CARD_ACCEPTOR_ID_CODE, As2805SuncorpUtils.packMerchantId(auditInfo.getMerchantId()));

            msg.set(_048_ADDITIONAL_DATA, packF48(MalFactory.getInstance()) );
            msg.set(_060_ADDITIONAL_PRIVATE, d.getPayCfg().getPaymentAppVersion() );
            msg.set(_070_NMIC, "001");

            Timber.i( msg.toString());
            return msg.toMsg();
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static byte[] packReconciliation(IDependency d, TransRec trans) {

        try {

            TProtocol proto = trans.getProtocol();
            TAmounts amounts = trans.getAmounts();
            TAudit auditinfo = trans.getAudit();
            TSec secinfo = trans.getSecurity();
            ReconciliationManager.getInstance(); // Make sure our instance is already created.
            Reconciliation r = reconciliationDao.findByTransId(trans.getUid());
            trans.setReconciliation(r);
            TReconciliationFigures reconcFigures = trans.getReconciliation().getReconciliationFigures();

            As2805Suncorp msg = new As2805Suncorp();

            msg.setMsgType(Iso8583.MsgType._0520_ACQUIRER_RECONCILE_ADV);
            msg.set(_011_SYS_TRACE_AUDIT_NUM, As2805SuncorpUtils.packStan(proto.getStan()));
            msg.set(_032_ACQUIRING_INST_ID_CODE, As2805SuncorpUtils.packAiic(d.getPayCfg().getPaymentSwitch()) );

            msg.set(_041_CARD_ACCEPTOR_TERMINAL_ID, auditinfo.getTerminalId());
            msg.set(_042_CARD_ACCEPTOR_ID_CODE, As2805SuncorpUtils.packMerchantId(auditinfo.getMerchantId()));

            // TODO: implement this - 0 = merchant initiated. 1 = automatic settlement - max capacity
            msg.set(_066_SETTLEMENT_CODE, "0" );

            // mandatory fields
            msg.set(_074_CREDITS_NUMBER, String.format("%010d", reconcFigures.getCreditsNumber()));
            msg.set(_075_CREDITS_REVERSAL_NUMBER, "0000000000"); // spec says zero fill
            msg.set(_076_DEBITS_NUMBER, String.format("%010d", reconcFigures.getDebitsNumber()));
            msg.set(_077_DEBITS_REVERSAL_NUMBER, "0000000000"); // spec says zero fill
            msg.set(_078_TRANSFER_NUMBER, "0000000000"); // spec says zero fill
            msg.set(_079_TRANSFER_REVERSAL_NUMBER, "0000000000"); // spec says zero fill
            msg.set(_080_INQUIRIES_NUMBER, "0000000000"); // spec says zero fill
            msg.set(_081_AUTHORISATIONS_NUMBER, "0000000000"); // spec says zero fill

            // optional fields
            msg.set(_086_CREDITS_AMOUNT, String.format("%016d", reconcFigures.getCreditsAmount()));
            msg.set(_087_CREDITS_REVERSAL_AMOUNT, "0000000000000000"); // spec says zero fill
            msg.set(_088_DEBITS_AMOUNT, String.format("%016d", reconcFigures.getDebitsAmount()));
            msg.set(_089_DEBITS_REVERSAL_AMOUNT, "0000000000000000"); // spec says zero fill

            // amount, net reconciliation - need to add "sign" (C or D)
            long netReconcAmount = reconcFigures.getNetReconciliationAmount();
            String sign = "43"; // hex code for 'C'
            if (netReconcAmount < 0) {
                sign = "44"; // hex code for 'D'
                netReconcAmount = -netReconcAmount;
            }
            msg.set(_097_AMOUNT_NET_SETTLEMENT, String.format("%s%016d", sign, netReconcAmount));

            msg.set(_118_CASHOUTS_NUMBER, String.format("%010d", reconcFigures.getCashoutsNumber() ) );
            msg.set(_119_CASHOUTS_AMOUNT, String.format("%016d", reconcFigures.getCashoutsAmount() ) );

            if (trans.getProtocol().isIncludeMac()) {
                msg.set(_128_MAC, As2805SuncorpUtils.packMac(msg));
                secinfo.setKsn(Util.byteArrayToHexString(P2PLib.getInstance().getIP2PSec().getDUKPTKsn(IP2PSec.KeyGroup.TERM_GROUP)));
            }
            Timber.i( msg.toString());
            return msg.toMsg();

        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public enum MSGTYPE {AUTH, ADVICE, NETWORK, REVERSAL, RECONCILIATION, RECONCILIATION_DETAILS}


}
