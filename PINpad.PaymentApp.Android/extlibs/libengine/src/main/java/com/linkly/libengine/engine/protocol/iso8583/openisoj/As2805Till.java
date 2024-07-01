package com.linkly.libengine.engine.protocol.iso8583.openisoj;

import static com.linkly.libengine.engine.protocol.iso8583.openisoj.As2805Till.Bit.DE_024_NII;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.As2805Till.Bit.DE_047_ADDITIONAL_DATA_NATIONAL;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.As2805Till.Bit.DE_055_ICC_DATA;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.As2805Till.Bit.DE_057_CASH_AMOUNT;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.As2805Till.Bit.DE_060_ADDITIONAL_PRIVATE;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.As2805Till.Bit.DE_061_ADDITIONAL_PRIVATE;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.As2805Till.Bit.DE_062_ADDITIONAL_PRIVATE;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.As2805Till.Bit.DE_063_ADDITIONAL_PRIVATE;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.As2805Till.Bit.DE_064_MAC;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.As2805Till.Bit.DE_070_NMIC;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.As2805Till.Bit.DE_071_MESSAGE_NUMBER;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.As2805Till.Bit.DE_072_MESSAGE_NUMBER_LAST;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.As2805Till.Bit.DE_118_CASHOUTS_NUMBER;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.As2805Till.Bit.DE_119_CASHOUTS_AMOUNT;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.As2805Till.Bit.DE_128_MAC;
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

import timber.log.Timber;

public class As2805Till extends Iso8583 implements IMessage {
    public class Bit {

        private Bit() {
        }

        /**
         * Field 24 - network international identification NII
         */
        public static final int DE_024_NII = 24;

        /**
         * Field 47 additional data national
         */
        public static final int DE_047_ADDITIONAL_DATA_NATIONAL = 47;

        /**
         * Field 55 - ICC Data
         */
        public static final int DE_055_ICC_DATA = 55;

        public static final int DE_057_CASH_AMOUNT = 57;

        public static final int DE_060_ADDITIONAL_PRIVATE = 60;
        public static final int DE_061_ADDITIONAL_PRIVATE = 61;
        public static final int DE_062_ADDITIONAL_PRIVATE = 62;
        public static final int DE_063_ADDITIONAL_PRIVATE = 63;
        /**
         * Field 64 - mac
         */
        public static final int DE_064_MAC = 64;

        /**
         * Field 70 - network management information code
         */
        public static final int DE_070_NMIC = 70;

        /**
         * Field 71 - message number
         */
        public static final int DE_071_MESSAGE_NUMBER = 71;

        /**
         * Field 72 - message number last
         */
        public static final int DE_072_MESSAGE_NUMBER_LAST = 72;


        public static final int DE_118_CASHOUTS_NUMBER = 118;
        public static final int DE_119_CASHOUTS_AMOUNT = 119;

        /**
         * Field 128 - mac
         */
        public static final int DE_128_MAC = 128;
    }

    private static Template msgTemplate;

    static {
        try {
            msgTemplate = new Template();
            msgTemplate.setMsgTypeFormatter(Formatters.getBinary());
            // DE 2 uses LLVAR as ...19n, with one byte length
            msgTemplate.put(_002_PAN, FieldDescriptor.getBcdVar(2,19));
            msgTemplate.put(_003_PROC_CODE, FieldDescriptor.getBcdFixed(6));
            msgTemplate.put(_004_TRAN_AMOUNT, FieldDescriptor.getBcdFixed(12));
            msgTemplate.put(_007_TRAN_DATE_TIME, FieldDescriptor.getBcdFixed(10));
            msgTemplate.put(_011_SYS_TRACE_AUDIT_NUM, FieldDescriptor.getBcdFixed(6));
            msgTemplate.put(_012_LOCAL_TRAN_TIME, FieldDescriptor.getBcdFixed(6));
            msgTemplate.put(_013_LOCAL_TRAN_DATE, FieldDescriptor.getBcdFixed(4));
            msgTemplate.put(_014_EXPIRATION_DATE, FieldDescriptor.getExpiryFixed(4)); // same as getBcdFixed but allows A-F chars - required for secapp operation
            msgTemplate.put(_015_SETTLEMENT_DATE, FieldDescriptor.getBcdFixed(4));
            msgTemplate.put(_022_POS_ENTRY_MODE, FieldDescriptor.getBcdFixed(3));
            msgTemplate.put(_023_CARD_SEQUENCE_NUM, FieldDescriptor.getBcdFixed(3));
            msgTemplate.put(DE_024_NII, FieldDescriptor.getBcdFixed(3));
            msgTemplate.put(_025_POS_CONDITION_CODE, FieldDescriptor.getBcdFixed(2));
            // DE 32 uses LLVAR as ...11n, with one byte length
            msgTemplate.put(_032_ACQUIRING_INST_ID_CODE, FieldDescriptor.getBcdVar(2,11));
            // DE 35 uses LLVAR as ...38n, with one byte length
            msgTemplate.put(_035_TRACK_2_DATA, FieldDescriptor.getBcdVar(2,38));
            msgTemplate.put(_037_RETRIEVAL_REF_NUM, FieldDescriptor.getAsciiFixed(12, FieldValidators.getAnp()));
            msgTemplate.put(_038_AUTH_ID_RESPONSE, FieldDescriptor.getAsciiFixed(6, FieldValidators.getAnp()));
            msgTemplate.put(_039_RESPONSE_CODE, FieldDescriptor.getAsciiFixed(2, FieldValidators.getAn()));
            msgTemplate.put(_041_CARD_ACCEPTOR_TERMINAL_ID, FieldDescriptor.getAsciiFixed(8, FieldValidators.getAns()));
            msgTemplate.put(_042_CARD_ACCEPTOR_ID_CODE, FieldDescriptor.getAsciiFixed(15, FieldValidators.getAns()));
            msgTemplate.put(_043_CARD_ACCEPTOR_NAME_LOCATION, FieldDescriptor.getAsciiFixed(40, FieldValidators.getAns()));
            msgTemplate.put(_044_ADDITIONAL_RESPONSE_DATA, FieldDescriptor.getAsciiVar(2, 99, FieldValidators.getAns()));
            msgTemplate.put(DE_047_ADDITIONAL_DATA_NATIONAL, FieldDescriptor.getAsciiVar(3, 999, FieldValidators.getAns()));
            msgTemplate.put(_048_ADDITIONAL_DATA, new FieldDescriptor(new VariableLengthFormatter(3, 999), FieldValidators.getHex(), Formatters.getBinary(), null, null));
            msgTemplate.put(_049_TRAN_CURRENCY_CODE, FieldDescriptor.getBcdFixed(3));
            msgTemplate.put(_050_SETTLEMENT_CURRENCY_CODE, FieldDescriptor.getBcdFixed(3));
            msgTemplate.put(_052_PIN_DATA, FieldDescriptor.getBinaryFixed(8));
            msgTemplate.put(_053_SECURITY_RELATED_CONTROL_INFORMATION, FieldDescriptor.getBcdFixed(16));
            msgTemplate.put(DE_055_ICC_DATA, FieldDescriptor.getBinaryVar(3,999, Formatters.getBinary()));
            // DE057: additional amount not referred in the Till spec
            msgTemplate.put(DE_057_CASH_AMOUNT, FieldDescriptor.getBcdFixed(12));
            msgTemplate.put(DE_060_ADDITIONAL_PRIVATE, FieldDescriptor.getAsciiVar(3, 999, FieldValidators.getAns()));
            msgTemplate.put(DE_061_ADDITIONAL_PRIVATE, FieldDescriptor.getAsciiVar(3, 999, FieldValidators.getAns()));
            msgTemplate.put(DE_062_ADDITIONAL_PRIVATE, FieldDescriptor.getAsciiVar(3, 999, FieldValidators.getAns()));
            msgTemplate.put(DE_063_ADDITIONAL_PRIVATE, FieldDescriptor.getAsciiVar(3, 999, FieldValidators.getAns()));
            msgTemplate.put(DE_064_MAC, FieldDescriptor.getBinaryFixed(8));
            msgTemplate.put(_066_SETTLEMENT_CODE, FieldDescriptor.getBcdFixed(1));
            msgTemplate.put(DE_070_NMIC, FieldDescriptor.getBcdFixed(3));
            msgTemplate.put(DE_071_MESSAGE_NUMBER, FieldDescriptor.getBcdFixed(4));
            msgTemplate.put(DE_072_MESSAGE_NUMBER_LAST, FieldDescriptor.getBcdFixed(4));
            msgTemplate.put(_074_CREDITS_NUMBER, FieldDescriptor.getBcdFixed(10));
            msgTemplate.put(_075_CREDITS_REVERSAL_NUMBER, FieldDescriptor.getBcdFixed(10));
            msgTemplate.put(_076_DEBITS_NUMBER, FieldDescriptor.getBcdFixed(10));
            msgTemplate.put(_077_DEBITS_REVERSAL_NUMBER, FieldDescriptor.getBcdFixed(10));
            msgTemplate.put(_078_TRANSFER_NUMBER, FieldDescriptor.getBcdFixed(10));
            msgTemplate.put(_079_TRANSFER_REVERSAL_NUMBER, FieldDescriptor.getBcdFixed(10));
            msgTemplate.put(_080_INQUIRIES_NUMBER, FieldDescriptor.getBcdFixed(10));
            msgTemplate.put(_081_AUTHORISATIONS_NUMBER, FieldDescriptor.getBcdFixed(10));
            msgTemplate.put(_086_CREDITS_AMOUNT, FieldDescriptor.getBcdFixed(16));
            msgTemplate.put(_087_CREDITS_REVERSAL_AMOUNT, FieldDescriptor.getBcdFixed(16));
            msgTemplate.put(_088_DEBITS_AMOUNT, FieldDescriptor.getBcdFixed(16));
            msgTemplate.put(_089_DEBITS_REVERSAL_AMOUNT, FieldDescriptor.getBcdFixed(16));
            msgTemplate.put(_090_ORIGINAL_DATA_ELEMENTS, FieldDescriptor.getBcdFixed(42));
            msgTemplate.put(_091_FILE_UPDATE_CODE, FieldDescriptor.getAsciiFixed(1, FieldValidators.getAns()));
            msgTemplate.put(_097_AMOUNT_NET_SETTLEMENT, FieldDescriptor.getBinaryFixed(9)); // x + n16 field. easier to treat as binary than be clever here
            msgTemplate.put(_101_FILE_NAME, FieldDescriptor.getAsciiVar(2, 17, FieldValidators.getAns()));
            msgTemplate.put(DE_118_CASHOUTS_NUMBER, FieldDescriptor.getBcdFixed(10));
            msgTemplate.put(DE_119_CASHOUTS_AMOUNT, FieldDescriptor.getBcdFixed(16));
            msgTemplate.put(DE_128_MAC, FieldDescriptor.getBinaryFixed(8));
        } catch (Exception e) {
            Timber.i(e);
            System.exit(1);
        }
    }

    private boolean unpacking = false;

    public As2805Till() {
        super(msgTemplate);
        msgTemplate.setBitmapFormatter(Formatters.getBinary());
        msgTemplate.setMsgTypeFormatter(Formatters.getBcdFixed());
        bitmap.setFormatter(msgTemplate.getBitmapFormatter());
    }

    public As2805Till(byte[] data) throws Exception {
        this();
        unpack(data, 0);
    }

    /**
     * Verify the string in the byte data against the original string
     *
     * @param originalString to be checked against
     * @param field          number of the field
     * @return true if equal
     */
    public boolean verifyString(String originalString, int field) {
        return (originalString != null && originalString.equals(this.get(field)));
    }

    @Override
    protected IField createField(int field) throws UnknownFieldException {

        // I'm certain that TermApp.ISO in Postilion is broken when it comes to MACcing.  Using the 'B' format
        // when sending a message it has to be ASCII fixed 16, otherwise Postilion doesn't pick up the full MAC
        // However when Postilion replies, it packs the MAC in binary fixed 8
        if (unpacking && (field == Bit.DE_064_MAC || field == Bit.DE_128_MAC)) {
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

    public int unpack(byte[] msg, int startingOffset) throws Exception {
        return unpack(msg, startingOffset, true);
    }

    @Override
    public int unpack(byte[] msg, int startingOffset, boolean validateData) throws Exception {
        msgTemplate.setBitmapFormatter(Formatters.getBinary());
        bitmap = new Bitmap(msgTemplate.getBitmapFormatter());

        try {
            // Look at createField for why I'm doing this
            unpacking = true;
            return super.unpack(msg, startingOffset, validateData);
        } finally {
            unpacking = false;
        }
    }
}
