package com.linkly.libengine.engine.protocol.iso8583.openisoj;

import com.linkly.libengine.engine.protocol.iso8583.openisoj.exceptions.UnknownFieldException;
import com.linkly.libengine.engine.protocol.iso8583.openisoj.fieldvalidator.FieldValidators;
import com.linkly.libengine.engine.protocol.iso8583.openisoj.formatter.BinaryFormatter;
import com.linkly.libengine.engine.protocol.iso8583.openisoj.formatter.Formatters;
import com.linkly.libengine.engine.protocol.iso8583.openisoj.lengthformatters.VariableLengthFormatter;

import timber.log.Timber;

public class Iso8583Svfe extends Iso8583Rev93 implements IMessage {
    private static Template template;

    static {
        template = getDefaultTemplate();
        template.remove(Bit._002_PAN);
        template.remove(Iso8583.Bit._005_SETTLE_AMOUNT);
        template.remove(Bit._014_EXPIRY_DATE);
        template.remove(Bit._022_POS_DATA_CODE);
        template.remove(Bit._035_TRACK_2_DATA);
        template.remove(Bit._052_PIN_DATA);
        template.remove(Bit._048_PRIVATE_ADDITIONAL_DATA);
        template.remove(Bit._053_SECURITY_INFO);
        template.remove(Bit._054_ADDITIONAL_AMOUNTS);
        template.remove(Bit._055_ICC_DATA);
        template.remove(Bit._064_MAC);
        template.remove(Bit._128_MAC);
        try {
            // use less fussy validator for pan as we set placeholder data that's replaced by the p2pe module later
            template.put(Bit._002_PAN, FieldDescriptor.getAsciiVar(2, 19, FieldValidators.getAns()));
            template.put(Iso8583.Bit._005_SETTLE_AMOUNT, FieldDescriptor.getAsciiAlphaNumeric(13));
            template.put(Bit._014_EXPIRY_DATE, FieldDescriptor.getAsciiFixed(6, FieldValidators.getN()));
            template.put(Iso8583.Bit._015_SETTLEMENT_DATE, FieldDescriptor.getAsciiFixed(6, FieldValidators.getN()));
            template.put(Bit._022_POS_DATA_CODE, FieldDescriptor.getAsciiFixed(3, FieldValidators.getN()));
            template.put(Bit._025_MSG_REASON_CODE, FieldDescriptor.getAsciiFixed(2, FieldValidators.getN()));
            // use less fussy validator for track 2 as we set placeholder data that's replaced by the p2pe module later
            template.put(Bit._035_TRACK_2_DATA, FieldDescriptor.getAsciiVar(2, 37, FieldValidators.getAns()));
            template.put(Bit._048_PRIVATE_ADDITIONAL_DATA, FieldDescriptor.getAsciiVar(3, 999, FieldValidators.getNone()));
            template.put(Bit._052_PIN_DATA, FieldDescriptor.getBinaryFixed(8));
            template.put(Bit._053_SECURITY_INFO, new FieldDescriptor(new VariableLengthFormatter(2, 96),
                    FieldValidators.getHex(), new BinaryFormatter(), null, null));

            template.put(Bit._054_ADDITIONAL_AMOUNTS, FieldDescriptor.getAsciiVar(3, 999, FieldValidators.getNone()));

            template.put(Bit._055_ICC_DATA, FieldDescriptor.getBinaryVar(3, 999, Formatters.getBinary()));


        } catch (Exception e) {
            Timber.w(e);
            System.exit(1);
        }
    }

    private boolean ascii = false;
    private boolean unpacking = false;

    public Iso8583Svfe() {
        this(false);
    }

    public Iso8583Svfe(boolean ascii) {
        super(template);
        this.ascii = ascii;

        if (ascii) {
            template.setBitmapFormatter(Formatters.getAscii());
        } else {
            template.setBitmapFormatter(Formatters.getBinary());
        }

        bitmap.setFormatter(template.getBitmapFormatter());
    }

    public Iso8583Svfe(byte[] data) throws Exception {
        this(data[0] == 'A');
        unpack(data, 0);
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

    public int unpack(byte[] msg, int startingOffset) throws Exception {
        return unpack(msg, startingOffset, true);
    }

    @Override
    public int unpack(byte[] msg, int startingOffset, boolean validateData) throws Exception {
        ascii = msg[0] == 'A';
        if (msg[0] == 'A' || msg[0] == 'B') {
            startingOffset++;
        }
        if (ascii) {
            template.setBitmapFormatter(Formatters.getAscii());
        } else {
            template.setBitmapFormatter(Formatters.getBinary());
        }

        bitmap = new Bitmap(template.getBitmapFormatter());

        try {
            // Look at createField for why I'm doing this
            unpacking = true;
            return super.unpack(msg, startingOffset, validateData);
        } finally {
            unpacking = false;
        }
    }

    public class ActionCode extends Iso8583Rev93.ActionCode {
    }

    public class Bit extends Iso8583Rev93.Bit {
    }

    public class FuncCode extends Iso8583Rev93.FuncCode {
        public static final String _500_RECON = "500";
    }

    public class MsgType extends Iso8583Rev93.MsgType {
    }
}
