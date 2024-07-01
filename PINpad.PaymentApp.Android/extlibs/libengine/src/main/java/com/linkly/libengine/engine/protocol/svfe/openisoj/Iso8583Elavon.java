package com.linkly.libengine.engine.protocol.svfe.openisoj;

import com.linkly.libengine.engine.protocol.svfe.openisoj.fieldvalidator.FieldValidators;
import com.linkly.libengine.engine.protocol.svfe.openisoj.formatter.BcdFormatter;
import com.linkly.libengine.engine.protocol.svfe.openisoj.formatter.Formatters;
import com.linkly.libengine.engine.protocol.svfe.openisoj.lengthformatters.ElavonVariableLengthFormatter;
import com.linkly.libengine.engine.protocol.svfe.openisoj.lengthformatters.VariableLengthFormatter;

import timber.log.Timber;

public class Iso8583Elavon extends Iso8583Rev93 implements IMessage {
    private static Template template;

    static {
        try {
            template = new Template();
            template.setMsgTypeFormatter(Formatters.getBinary());
            template.put(Iso8583Rev93.Bit._002_PAN,new FieldDescriptor(new VariableLengthFormatter(1, 19, Formatters.getBcd()), FieldValidators.getN(), Formatters.getBcd(), null, FullSensitiser.getInstance()));
            template.put(Iso8583Rev93.Bit._003_PROC_CODE, FieldDescriptor.getBcdFixed(3));
            template.put(Iso8583Rev93.Bit._004_TRAN_AMOUNT, FieldDescriptor.getBcdFixed(6));
            template.put(Iso8583Rev93.Bit._006_BILLING_AMOUNT, FieldDescriptor.getBcdFixed(6));
            template.put(Iso8583Rev93.Bit._010_CONVERSION_RATE, FieldDescriptor.getBcdFixed(5));
            template.put(Iso8583Rev93.Bit._011_SYS_TRACE_AUDIT_NUM, FieldDescriptor.getBcdFixed(3));
            template.put(Iso8583Rev93.Bit._012_LOCAL_TRAN_DATETIME, FieldDescriptor.getBcdFixed(6));
            template.put(Iso8583Rev93.Bit._014_EXPIRY_DATE, FieldDescriptor.getBcdFixed(2));
            template.put(Iso8583Rev93.Bit._022_POS_DATA_CODE, FieldDescriptor.getAsciiFixed(12, FieldValidators.getAns()));
            template.put(Iso8583Rev93.Bit._024_FUNC_CODE, FieldDescriptor.getBcdFixed(2));
            template.put(Iso8583Rev93.Bit._025_MSG_REASON_CODE, FieldDescriptor.getBcdFixed(2));
            template.put(Iso8583Rev93.Bit._028_RECON_DATE, FieldDescriptor.getBcdFixed(3));
            template.put(Iso8583Rev93.Bit._029_RECON_INDICATOR, FieldDescriptor.getBcdFixed(2));
            template.put(Iso8583Rev93.Bit._032_ACQ_INST_ID_CODE, new FieldDescriptor(new VariableLengthFormatter(1, 6, Formatters.getBcd()), FieldValidators.getN(), Formatters.getBcd(), null, null));
            template.put(Iso8583Rev93.Bit._035_TRACK_2_DATA, new FieldDescriptor(new VariableLengthFormatter(1, 37, Formatters.getBcd()),FieldValidators.getTrack2(), Formatters.getAscii(), null, FullSensitiser.getInstance()));
            template.put(Iso8583Rev93.Bit._037_RET_REF_NR, FieldDescriptor.getAsciiFixed(12, FieldValidators.getAnp()));
            template.put(Iso8583Rev93.Bit._038_APPROVAL_CODE, FieldDescriptor.getAsciiFixed(6, FieldValidators.getAnp()));
            template.put(Iso8583Rev93.Bit._039_ACTION_CODE, FieldDescriptor.getBcdFixed(2));
            template.put(Iso8583Rev93.Bit._041_TERMINAL_ID, FieldDescriptor.getBcdFixed(4));
            template.put(Iso8583Rev93.Bit._042_CARD_ACCEPTOR_ID, FieldDescriptor.getAsciiFixed(15, FieldValidators.getAns()));
            template.put(Iso8583Rev93.Bit._043_PAYMENT_FACILITATOR, FieldDescriptor.getAsciiVar(2, 99, FieldValidators.getAns()));
            template.put(Iso8583Rev93.Bit._044_ADDITIONAL_RESPONSE_DATA,FieldDescriptor.getAsciiVar(2, 99, FieldValidators.getAns()));
            template.put(Iso8583Rev93.Bit._048_PRIVATE_ADDITIONAL_DATA, new FieldDescriptor(new ElavonVariableLengthFormatter(2, 500, new BcdFormatter(), 2, true),FieldValidators.getN(), Formatters.getBcd(), null, null));
            template.put(Iso8583Rev93.Bit._049_TRAN_CURRENCY_CODE, FieldDescriptor.getBcdFixed(2));
            template.put(Iso8583Rev93.Bit._050_SETTLEMENT_CURRENCY_CODE,FieldDescriptor.getBcdFixed(2));
            template.put(Iso8583Rev93.Bit._051_CARDHOLDER_BILLING_CURRENCY_CODE,FieldDescriptor.getBcdFixed(2));
            template.put(Iso8583Rev93.Bit._052_PIN_DATA, FieldDescriptor.getBcdFixed(8));
            template.put(Iso8583Rev93.Bit._053_SECURITY_INFO, FieldDescriptor.getAsciiFixed(16, FieldValidators.getAns()));
            template.put(Iso8583Rev93.Bit._054_ADDITIONAL_AMOUNTS,new FieldDescriptor(new VariableLengthFormatter(3, 120, Formatters.getBcd()), FieldValidators.getAns(), Formatters.getAscii(), null, null));

            template.put(Iso8583Rev93.Bit._055_ICC_DATA, FieldDescriptor.getBinaryVar(4, 500, Formatters.getBinary()));
            template.put(Iso8583Rev93.Bit._060_ELAVON_PRIVATE_DATA, FieldDescriptor.getBinaryVar(4, 500, Formatters.getBinary()));
            template.put(Iso8583Rev93.Bit._063_ELAVON_PRIVATE_DATA_3, FieldDescriptor.getBinaryVar(4, 500, Formatters.getBinary()));
            template.put(Iso8583Rev93.Bit._064_MAC, FieldDescriptor.getBinaryFixed(4));

            template.put(Iso8583Rev93.Bit._074_NR_CREDITS, FieldDescriptor.getBcdFixed(5));
            template.put(Iso8583Rev93.Bit._075_NR_CREDITS_REVERSAL, FieldDescriptor.getBcdFixed(5));
            template.put(Iso8583Rev93.Bit._076_NR_DEBITS, FieldDescriptor.getBcdFixed(5));
            template.put(Iso8583Rev93.Bit._077_NR_DEBITS_REVERSAL, FieldDescriptor.getBcdFixed(5));

            template.put(Iso8583Rev93.Bit._086_AMOUNT_CREDITS, FieldDescriptor.getBcdFixed(8));
            template.put(Iso8583Rev93.Bit._087_AMOUNT_CREDITS_REVERSAL, FieldDescriptor.getBcdFixed(8));
            template.put(Iso8583Rev93.Bit._088_AMOUNT_DEBITS, FieldDescriptor.getBcdFixed(8));
            template.put(Iso8583Rev93.Bit._089_AMOUNT_DEBITS_REVERSAL, FieldDescriptor.getBcdFixed(8));

            template.put(Iso8583Rev93.Bit._097_AMOUNT_NET_RECON, FieldDescriptor.getBcdFixed(9));
            template.put(Iso8583Rev93.Bit._128_MAC, FieldDescriptor.getBcdFixed(4));


        } catch (Exception e) {
            Timber.w(e);
            System.exit(1);
        }
    }

    private boolean ascii;
    private boolean unpacking = false;

    public Iso8583Elavon() {
        this(false);
    }

    public Iso8583Elavon(boolean ascii) {
        super(template);
        this.ascii = ascii;

        if (ascii) {
            template.setBitmapFormatter(Formatters.getAscii());
        } else {
            template.setBitmapFormatter(Formatters.getBinary());
        }

        template.setMsgTypeFormatter(Formatters.getBinary());
        bitmap.setFormatter(template.getBitmapFormatter());
    }

    public Iso8583Elavon(byte[] data) throws Exception {
        this(data[0] == 'A');
        unpack(data, 0);
    }

    @Override
    public byte[] toMsg() throws Exception {
        byte[] superMsg = super.toMsg();
        byte[] msg = new byte[superMsg.length];
        System.arraycopy(superMsg, 0, msg, 0, superMsg.length);
        return msg;
    }

    @Override
    public int unpack(byte[] msg, int startingOffset) throws Exception {
        ascii = msg[0] == 'A';
        if (ascii) {
            template.setBitmapFormatter(Formatters.getAscii());
        } else {
            template.setBitmapFormatter(Formatters.getBinary());
        }

        bitmap = new Bitmap(template.getBitmapFormatter());

        try {
            // Look at createField for why I'm doing this
            unpacking = true;
            return super.unpack(msg, startingOffset);
        } finally {
            unpacking = false;
        }
    }

    public class Bit extends Iso8583Rev93.Bit {
    }

    public class FuncCode extends Iso8583Rev93.FuncCode {
        public static final String _500_RECON = "500";
    }

    public class MsgType extends Iso8583Rev93.MsgType {
    }

    public static class ElavonCardDataInputMode {
        public static final String _0_UNKNOWN = "0";
        public static final String _1_MANUAL = "1";
        public static final String _2_MAGSTRIPE = "2";
        public static final String _3_CONTACTLESS_MAGSTRIPE = "3";
        public static final String _4_CONTACTLESS_ICC = "4";
        public static final String _5_ICC = "5";
        public static final String _6_KEY_ENTRY = "6";
        public static final String _8_INAPP_ECOMMERCE = "8";
        public static final String _9_FALLBACK_ICC_TO_MANUAL = "9";
    }

    public static class ElavonCardholderAuthMethod {
        public static final String _0_NONE = "0";
        public static final String _1_PIN = "1";
        public static final String _5_SIGNATURE = "5";
        public static final String _6_OTHER = "6";
    }


}
