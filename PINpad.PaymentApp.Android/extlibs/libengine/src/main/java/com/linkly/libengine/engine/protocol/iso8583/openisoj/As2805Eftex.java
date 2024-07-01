package com.linkly.libengine.engine.protocol.iso8583.openisoj;


import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583Rev93.Bit._002_PAN;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583Rev93.Bit._014_EXPIRY_DATE;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583Rev93.Bit._035_TRACK_2_DATA;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583Rev93.Bit._052_PIN_DATA;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583Rev93.Bit._055_ICC_DATA;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583Rev93.Bit._058_AUTH_AGENT_INST_ID_CODE;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583Rev93.Bit._096_KEY_MANAGEMENT_DATA;

import com.linkly.libengine.engine.protocol.iso8583.openisoj.exceptions.UnknownFieldException;
import com.linkly.libengine.engine.protocol.iso8583.openisoj.fieldvalidator.FieldValidators;
import com.linkly.libengine.engine.protocol.iso8583.openisoj.formatter.Formatters;
import com.linkly.libengine.engine.protocol.iso8583.openisoj.lengthformatters.VariableLengthFormatter;
import com.linkly.libengine.engine.protocol.iso8583.openisoj.termappiso.AdditionalData;

import timber.log.Timber;

public class As2805Eftex extends Iso8583Rev93 implements IMessage {

    private static Template template;

    static {
        try {
            template = getDefaultTemplate();
            template.put(_002_PAN, new FieldDescriptor(new VariableLengthFormatter(2,19), FieldValidators.getBcd(), Formatters.getAscii(), null, null));
            template.put(_014_EXPIRY_DATE, FieldDescriptor.getAsciiFixed(4, FieldValidators.getAns())); // need to allow for non-numeric here due to how the template system/secapp works
            template.put(_035_TRACK_2_DATA, new FieldDescriptor(new VariableLengthFormatter(2,38), FieldValidators.getBcd(), Formatters.getAscii(), null, null));
            template.put(_052_PIN_DATA, FieldDescriptor.getBinaryFixed(8));
            template.put(_055_ICC_DATA, new FieldDescriptor(new VariableLengthFormatter(3,999), FieldValidators.getHex(), Formatters.getBinary(), null, null));
            // iso8583.93 says this is a numeric field only, but found bp-node putting alpha chars here, causing PA to discard entire message. Let's not be fussy as we ignore this field anyways
            template.put(_058_AUTH_AGENT_INST_ID_CODE, FieldDescriptor.getAsciiVar(2, 11, FieldValidators.getAns()));
            template.put(_096_KEY_MANAGEMENT_DATA, new FieldDescriptor(new VariableLengthFormatter(3,999), FieldValidators.getHex(), Formatters.getBinary(), null, null));

        } catch (Exception e) {
            Timber.w(e);
            System.exit(1);
        }
    }

    private boolean unpacking = false;

    public As2805Eftex(){
        super(template);
        template.setBitmapFormatter(Formatters.getBinary());
        template.setMsgTypeFormatter(Formatters.getAscii());
        bitmap.setFormatter(template.getBitmapFormatter());
        setMsgTypeEncoding(MSG_TYPE_ENCODING.ASCII);
    }

    public As2805Eftex(byte[] data) throws Exception {
        this();
        // start at offset 1 to skip encoding character
        unpack(data, 1);
    }

    /**
     * Verify the string in the byte data against the original string
     * Whitespace padding is removed
     * @param originalString to be checked against
     * @param field number of the field
     * @return true if equal
     * */
    public boolean verifyString( String originalString, int field ){
       return ( originalString != null && originalString.trim().equals( this.get( field ).trim() ) );
    }

    @Override
    protected IField createField(int field) throws UnknownFieldException {
        if (field == Iso8583TermApp.Bit._048_PRIVATE_ADDITIONAL_DATA) {
            return new AdditionalData();
        }

        // I'm certain that TermApp.ISO in Postilion is broken when it comes to MACcing.  Using the 'B' format
        // when sending a message it has to be ASCII fixed 16, otherwise Postilion doesn't pick up the full MAC
        // However when Postilion replies, it packs the MAC in binary fixed 8
        if (unpacking && (field == Bit._064_MAC || field == Bit._128_MAC)) {
            return new Field(field, FieldDescriptor.getBinaryFixed(8));
        }

        return super.createField(field);
    }

    public AdditionalData getAdditionalData() throws Exception {
        if (isFieldSet(Iso8583TermApp.Bit._048_PRIVATE_ADDITIONAL_DATA)) {
            return (AdditionalData) getField(Iso8583TermApp.Bit._048_PRIVATE_ADDITIONAL_DATA);
        }
        return null;
    }

    /**
     * Get the structured data out of field 48.16
     *
     * @return Structured Data
     */
    public HashtableMessage getStructuredData() throws Exception {
        if (!isFieldSet(Iso8583TermApp.Bit._048_PRIVATE_ADDITIONAL_DATA)) {
            return null;
        }
        AdditionalData addData = getAdditionalData();
        if (!addData.containsKey(AdditionalData.Field.StructuredData)) {
            return null;
        }

        HashtableMessage sd = new HashtableMessage();
        sd.fromMessageString(addData.get(AdditionalData.Field.StructuredData));

        return sd;
    }

    public void putAdditionalData(AdditionalData addData) throws Exception {
        if (addData == null) {
            this.clearField(48);
        }
        bitmap.setField(Iso8583TermApp.Bit._048_PRIVATE_ADDITIONAL_DATA, addData != null);
        fields.put(48, addData);
    }

    /**
     * Put the structured data message into field 48.16
     *
     * @param data Structured Data
     */
    public void putStructuredData(HashtableMessage data) throws Exception {
        String sd = data.toMessageString();

        AdditionalData addData = getAdditionalData();
        if (addData == null) {
            addData = new AdditionalData();
        }

        addData.put(AdditionalData.Field.StructuredData, sd);
        putAdditionalData(addData);
    }

    @Override
    public byte[] toMsg() throws Exception {
        return toMsg(false);
    }

    public byte[] toMsg( boolean skipEncodingChar ) throws Exception {
        byte[] superMsg = super.toMsg();
        if( skipEncodingChar ) {
            return superMsg;
        } else {
            byte[] msg = new byte[superMsg.length + 1];

            msg[0] = 'B'; // 'B' for binary encoding
            System.arraycopy(superMsg, 0, msg, 1, superMsg.length);

            return msg;
        }
    }

    public int unpack(byte[] msg, int startingOffset) throws Exception {
        return unpack(msg, startingOffset, true);
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
}
