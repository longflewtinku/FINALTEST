package com.linkly.libengine.engine.protocol.iso8583.openisoj;

import static com.linkly.libengine.engine.protocol.iso8583.openisoj.As2805Woolworths.Bit._024_NII;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.As2805Woolworths.Bit._047_ADDITIONAL_DATA_NATIONAL;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.As2805Woolworths.Bit._055_ICC_DATA;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.As2805Woolworths.Bit._057_CASH_AMOUNT;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.As2805Woolworths.Bit._060_ADDITIONAL_PRIVATE;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.As2805Woolworths.Bit._061_ADDITIONAL_PRIVATE;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.As2805Woolworths.Bit._062_ADDITIONAL_PRIVATE;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.As2805Woolworths.Bit._063_ADDITIONAL_PRIVATE;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.As2805Woolworths.Bit._064_MAC;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.As2805Woolworths.Bit._070_NMIC;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.As2805Woolworths.Bit._071_MESSAGE_NUMBER;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.As2805Woolworths.Bit._072_MESSAGE_NUMBER_LAST;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.As2805Woolworths.Bit._110_KSN_AND_ENCRYPTION_DATA;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.As2805Woolworths.Bit._118_CASHOUTS_NUMBER;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.As2805Woolworths.Bit._119_CASHOUTS_AMOUNT;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.As2805Woolworths.Bit._128_MAC;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583.Bit._002_PAN;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583.Bit._003_PROC_CODE;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583.Bit._004_TRAN_AMOUNT;
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
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583.Bit._044_ADDITIONAL_RESPONSE_DATA;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583.Bit._048_ADDITIONAL_DATA;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583.Bit._049_TRAN_CURRENCY_CODE;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583.Bit._050_SETTLEMENT_CURRENCY_CODE;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583.Bit._052_PIN_DATA;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583.Bit._053_SECURITY_RELATED_CONTROL_INFORMATION;
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
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583.Bit._091_FILE_UPDATE_CODE;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583.Bit._097_AMOUNT_NET_SETTLEMENT;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583.Bit._101_FILE_NAME;

import com.linkly.libengine.engine.protocol.iso8583.openisoj.exceptions.UnknownFieldException;
import com.linkly.libengine.engine.protocol.iso8583.openisoj.fieldvalidator.FieldValidators;
import com.linkly.libengine.engine.protocol.iso8583.openisoj.formatter.Formatters;
import com.linkly.libengine.engine.protocol.iso8583.openisoj.lengthformatters.VariableLengthFormatter;
import com.linkly.libmal.global.util.HexDump;
import com.linkly.libsecapp.emv.Util;

import timber.log.Timber;

public class As2805Woolworths extends Iso8583 implements IMessage {
    public class Bit {

        /**
         * Field 24 - network international identification NII
         */
        public static final int _024_NII = 24;

        /**
         * Field 47 additional data national
         */
        public static final int _047_ADDITIONAL_DATA_NATIONAL = 47;

        /**
         * Field 55 - ICC Data
         */
        public static final int _055_ICC_DATA = 55;

        public static final int _057_CASH_AMOUNT = 57;

        public static final int _060_ADDITIONAL_PRIVATE = 60;
        public static final int _061_ADDITIONAL_PRIVATE = 61;
        public static final int _062_ADDITIONAL_PRIVATE = 62;
        public static final int _063_ADDITIONAL_PRIVATE = 63;

        /**
         * Field 64 - mac
         */
        public static final int _064_MAC = 64;

        /**
         * Field 70 - network management information code
         */
        public static final int _070_NMIC = 70;

        /**
         * Field 71 - message number
         */
        public static final int _071_MESSAGE_NUMBER = 71;

        /**
         * Field 72 - message number last
         */
        public static final int _072_MESSAGE_NUMBER_LAST = 72;

        public static final int _110_KSN_AND_ENCRYPTION_DATA = 110;

        public static final int _118_CASHOUTS_NUMBER = 118;
        public static final int _119_CASHOUTS_AMOUNT = 119;

        /**
         * Field 128 - mac
         */
        public static final int _128_MAC = 128;
    }

    private static Template template;

    static {
        try {
            template = new Template();
            template.setMsgTypeFormatter(Formatters.getBinary());
            // DE 2 uses a non standard format of LLvar. Two Ascii digits are used to indicate the length
            template.put(_002_PAN, new FieldDescriptor(new VariableLengthFormatter(2,19), FieldValidators.getBcd(), Formatters.getBcdVar(), null, null));
            template.put(_003_PROC_CODE, FieldDescriptor.getBcdFixed(6));
            template.put(_004_TRAN_AMOUNT, FieldDescriptor.getBcdFixed(12));
            template.put(_007_TRAN_DATE_TIME, FieldDescriptor.getBcdFixed(10));
            template.put(_011_SYS_TRACE_AUDIT_NUM, FieldDescriptor.getBcdFixed(6));
            template.put(_012_LOCAL_TRAN_TIME, FieldDescriptor.getBcdFixed(6));
            template.put(_013_LOCAL_TRAN_DATE, FieldDescriptor.getBcdFixed(4));
            template.put(_014_EXPIRATION_DATE, FieldDescriptor.getExpiryFixed(4)); // same as getBcdFixed but allows A-F chars - required for secapp operation
            template.put(_015_SETTLEMENT_DATE, FieldDescriptor.getBcdFixed(4));
            template.put(_022_POS_ENTRY_MODE, FieldDescriptor.getBcdFixed(3));
            template.put(_023_CARD_SEQUENCE_NUM,FieldDescriptor.getBcdFixed(3));
            template.put(_024_NII, FieldDescriptor.getBcdFixed(3));
            template.put(_025_POS_CONDITION_CODE, FieldDescriptor.getBcdFixed(2));
            // DE 32 uses a non standard format of LLvar. Two Ascii digits are used to indicate the length
            template.put(_032_ACQUIRING_INST_ID_CODE, new FieldDescriptor(new VariableLengthFormatter(2,11), FieldValidators.getBcd(), Formatters.getBcdVar(), null, null));
            // DE 35 uses a non standard format of LLvar. Two Ascii digits are used to indicate the length
            template.put(_035_TRACK_2_DATA, new FieldDescriptor(new VariableLengthFormatter(2,38), FieldValidators.getBcd(), Formatters.getBcdVar(), null, null));
            template.put(_037_RETRIEVAL_REF_NUM, FieldDescriptor.getAsciiFixed(12, FieldValidators.getAnp()));
            template.put(_038_AUTH_ID_RESPONSE, FieldDescriptor.getAsciiFixed(6, FieldValidators.getAnp()));
            template.put(_039_RESPONSE_CODE, FieldDescriptor.getAsciiFixed(2, FieldValidators.getNone()));
            template.put(_041_CARD_ACCEPTOR_TERMINAL_ID, FieldDescriptor.getAsciiFixed(8, FieldValidators.getAns()));
            template.put(_042_CARD_ACCEPTOR_ID_CODE, FieldDescriptor.getAsciiFixed(15, FieldValidators.getAns()));
            template.put(_043_CARD_ACCEPTOR_NAME_LOCATION, FieldDescriptor.getAsciiFixed(40, FieldValidators.getAns()));
            template.put(_044_ADDITIONAL_RESPONSE_DATA,FieldDescriptor.getAsciiVar(2, 99, FieldValidators.getAns()));
            template.put(_047_ADDITIONAL_DATA_NATIONAL, FieldDescriptor.getAsciiVar(3, 999, FieldValidators.getNone()));
            template.put(_048_ADDITIONAL_DATA, new FieldDescriptor(new VariableLengthFormatter(3,999), FieldValidators.getHex(), Formatters.getBinary(), null, null));
            template.put(_049_TRAN_CURRENCY_CODE, FieldDescriptor.getBcdFixed(3));
            template.put(_050_SETTLEMENT_CURRENCY_CODE,FieldDescriptor.getBcdFixed(3));
            template.put(_052_PIN_DATA, FieldDescriptor.getBinaryFixed(8));
            template.put(_053_SECURITY_RELATED_CONTROL_INFORMATION, FieldDescriptor.getBcdFixed(16));
            template.put(_054_ADDITIONAL_AMOUNTS,FieldDescriptor.getAsciiVar(3, 120, FieldValidators.getAn()));
            // ICC data is an LLLVAR ANS field, so length digits are ascii, but data is binary packed
            template.put(_055_ICC_DATA, new FieldDescriptor(new VariableLengthFormatter(3,999), FieldValidators.getHex(), Formatters.getBinary(), null, null));
            template.put(_057_CASH_AMOUNT, FieldDescriptor.getBcdFixed(12) );
            template.put(_060_ADDITIONAL_PRIVATE, FieldDescriptor.getAsciiVar(3, 999, FieldValidators.getNone()));
            template.put(_061_ADDITIONAL_PRIVATE, FieldDescriptor.getAsciiVar(3, 999, FieldValidators.getNone()));
            template.put(_062_ADDITIONAL_PRIVATE, FieldDescriptor.getAsciiVar(3, 999, FieldValidators.getNone()));
            template.put(_063_ADDITIONAL_PRIVATE, FieldDescriptor.getAsciiVar(3, 999, FieldValidators.getNone()));
            template.put(_064_MAC, FieldDescriptor.getBinaryFixed(8));
            template.put(_066_SETTLEMENT_CODE, FieldDescriptor.getBcdFixed(1));
            template.put(_070_NMIC, FieldDescriptor.getBcdFixed(3));
            template.put(_071_MESSAGE_NUMBER, FieldDescriptor.getBcdFixed(4));
            template.put(_072_MESSAGE_NUMBER_LAST, FieldDescriptor.getBcdFixed(4));
            template.put(_074_CREDITS_NUMBER, FieldDescriptor.getBcdFixed(10));
            template.put(_075_CREDITS_REVERSAL_NUMBER, FieldDescriptor.getBcdFixed(10));
            template.put(_076_DEBITS_NUMBER, FieldDescriptor.getBcdFixed(10));
            template.put(_077_DEBITS_REVERSAL_NUMBER, FieldDescriptor.getBcdFixed(10));
            template.put(_078_TRANSFER_NUMBER, FieldDescriptor.getBcdFixed(10));
            template.put(_079_TRANSFER_REVERSAL_NUMBER, FieldDescriptor.getBcdFixed(10));
            template.put(_080_INQUIRIES_NUMBER, FieldDescriptor.getBcdFixed(10));
            template.put(_081_AUTHORISATIONS_NUMBER, FieldDescriptor.getBcdFixed(10));
            template.put(_086_CREDITS_AMOUNT, FieldDescriptor.getBcdFixed(16));
            template.put(_087_CREDITS_REVERSAL_AMOUNT, FieldDescriptor.getBcdFixed(16));
            template.put(_088_DEBITS_AMOUNT, FieldDescriptor.getBcdFixed(16));
            template.put(_089_DEBITS_REVERSAL_AMOUNT, FieldDescriptor.getBcdFixed(16));
            template.put(_090_ORIGINAL_DATA_ELEMENTS, FieldDescriptor.getBcdFixed(42));
            template.put(_091_FILE_UPDATE_CODE, FieldDescriptor.getAsciiFixed(1, FieldValidators.getAns()));
            template.put(_097_AMOUNT_NET_SETTLEMENT, FieldDescriptor.getBinaryFixed(9)); // x + n16 field. easier to treat as binary than be clever here
            template.put(_101_FILE_NAME,FieldDescriptor.getAsciiVar(2, 17, FieldValidators.getAns()));
            template.put(_110_KSN_AND_ENCRYPTION_DATA, new FieldDescriptor(new VariableLengthFormatter(3,999), FieldValidators.getHex(), Formatters.getBinary(), null, null));
            template.put(_118_CASHOUTS_NUMBER, FieldDescriptor.getBcdFixed(10));
            template.put(_119_CASHOUTS_AMOUNT, FieldDescriptor.getBcdFixed(16));
            template.put(_128_MAC, FieldDescriptor.getBinaryFixed(8));
        } catch (Exception e) {
            Timber.w(e);
            System.exit(1);
        }
    }

    private boolean unpacking = false;

    public As2805Woolworths(){
        super(template);
        template.setBitmapFormatter(Formatters.getBinary());
        template.setMsgTypeFormatter(Formatters.getBcdFixed());
        bitmap.setFormatter(template.getBitmapFormatter());
    }

    public As2805Woolworths(byte[] data, boolean validateData) throws Exception {
        this();
        unpack(data, 0, validateData);
    }

    public As2805Woolworths(byte[] data) throws Exception {
        this(data, false);
    }

    /**
     * Verify the string in the byte data against the original string
     * @param originalString to be checked against
     * @param field number of the field
     * @return true if equal
     * */
    public boolean verifyString( String originalString, int field ){
       return ( originalString != null && originalString.equals( this.get( field ) ) );
    }

    @Override
    protected IField createField(int field) throws UnknownFieldException {

        // I'm certain that TermApp.ISO in Postilion is broken when it comes to MACcing.  Using the 'B' format
        // when sending a message it has to be ASCII fixed 16, otherwise Postilion doesn't pick up the full MAC
        // However when Postilion replies, it packs the MAC in binary fixed 8
        if (unpacking && (field == Bit._064_MAC || field == Bit._128_MAC)) {
            return new Field(field, FieldDescriptor.getBinaryFixed(8));
        }

        return super.createField(field);
    }

    @Override
    public byte[] toMsg() throws Exception {
        byte[] superMsg = super.toMsg();
        byte[] msg = new byte[superMsg.length];

        System.arraycopy(superMsg, 0, msg, 0, superMsg.length);

        return msg;
    }

    @Override
    public int unpack(byte[] msg, int startingOffset, boolean validateData) throws Exception {
        template.setBitmapFormatter(Formatters.getBinary());
        bitmap = new Bitmap(template.getBitmapFormatter());

        try {
            // Look at createField for why I'm doing this
            unpacking = true;
            return super.unpack(msg, startingOffset, validateData);
        } finally {
            unpacking = false;
        }
    }

    public byte[] replaceClearTextFields(byte[] partialEncryptedMsg, byte[] decryptedMsg) throws Exception {
        byte[] outputMsg = new byte[partialEncryptedMsg.length];

        // finally, truncate output hybrid encrypted msg to make it same length as cleartext msg for transmission

        // copy 'cleartext'/whitelisted fields
        // MTI
        // bitmaps
        // DE11/stan
        // DE41/tid
        // DE42/mid
        // DE110/KSN
        // PLUS all length indicators for variable length fields up to and including DE110
        int[] cleartextFieldList = {
                _011_SYS_TRACE_AUDIT_NUM,
                _041_CARD_ACCEPTOR_TERMINAL_ID,
                _042_CARD_ACCEPTOR_ID_CODE,
                _110_KSN_AND_ENCRYPTION_DATA
        };

        // copy encrypted to output ready for substitution
        System.arraycopy(decryptedMsg, 0, outputMsg, 0, partialEncryptedMsg.length);

        // copy MTI
        System.arraycopy(partialEncryptedMsg, 0, outputMsg, 0, 2);

        // copy bitmap
        int bitmapLen = isExtended() ? 16 : 8;
        System.arraycopy(partialEncryptedMsg, 2, outputMsg, 2, bitmapLen);

        // whitelisted fields
        for (int fieldIdx: cleartextFieldList) {
            try {
                int offset = getOffsetOfField(fieldIdx);
                int packedLen = getFieldPackedLength(fieldIdx);

                if (offset >= 0 && packedLen >= 0) {
                    System.arraycopy(partialEncryptedMsg, offset, outputMsg, offset, packedLen);
                } else {
                    Timber.e("error substituting cleartext field no %d", fieldIdx);
                }
            } catch (Exception e){
                // possibly field not found in message or template
                Timber.e("replaceClearTextFields exception : %s", e.getMessage());
            }
        }

        // replace length indicators of any variable length fields in msg
        for (int i = 2; i < _110_KSN_AND_ENCRYPTION_DATA; i++) {
            if (bitmap.isFieldSet(i)) {
                int lengthOfLength = getLengthOfLength(i);
                if (lengthOfLength > 0) {
                    int offset = getOffsetOfField(i);
                    // variable length field found in the message, copy the length bytes across
                    System.arraycopy(partialEncryptedMsg, offset, outputMsg, offset, lengthOfLength);
                }
            }
        }
        return outputMsg;
    }

    public byte[] replaceClearTextFields(byte[] inputMsg) throws Exception {
        return replaceClearTextFields(toMsg(), inputMsg);
    }
}
