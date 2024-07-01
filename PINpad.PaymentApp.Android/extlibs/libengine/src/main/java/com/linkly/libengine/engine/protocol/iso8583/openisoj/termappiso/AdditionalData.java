package com.linkly.libengine.engine.protocol.iso8583.openisoj.termappiso;

import com.linkly.libengine.engine.protocol.iso8583.openisoj.FieldDescriptor;
import com.linkly.libengine.engine.protocol.iso8583.openisoj.IField;
import com.linkly.libengine.engine.protocol.iso8583.openisoj.IFieldDescriptor;
import com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583TermApp;
import com.linkly.libengine.engine.protocol.iso8583.openisoj.IsoUtils;
import com.linkly.libengine.engine.protocol.iso8583.openisoj.UnpackObject;
import com.linkly.libengine.engine.protocol.iso8583.openisoj.exceptions.FieldFormatException;
import com.linkly.libengine.engine.protocol.iso8583.openisoj.fieldvalidator.FieldValidators;
import com.linkly.libengine.engine.protocol.iso8583.openisoj.lengthformatters.VariableLengthFormatter;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

public class AdditionalData extends TreeMap<AdditionalData.Field, String> implements IField {
    private static final long serialVersionUID = -882642900720512097L;
    private static VariableLengthFormatter lenFormatter;
    private int _offset; // offset to beginning of field
    private int _fieldLength; // length of entire field, including any variable length indicators

    static {
        try {
            lenFormatter = new VariableLengthFormatter(4, 9999);
        } catch (Exception e) {
            System.exit(1);
        }

    }

    public AdditionalData() {
    }

    public AdditionalData(String msg) {
        throw new RuntimeException("Not implemented");
    }

    private byte[] generateBitmap() throws Exception {
        int bitmap = 0;
        for (Field f : this.keySet()) {
            bitmap += Math.pow(2, 16 - f.field());
        }

        byte[] b = new byte[2];
        b[0] = (byte) (bitmap / 256);
        b[1] = (byte) (bitmap % 256);
        return b;
    }

    private IFieldDescriptor getFieldDescriptor(Field field) throws Exception {
        switch (field) {
            case PosData:
                return FieldDescriptor.getAsciiFixed(19, FieldValidators.getAns());
            case AuthProfile:
                return FieldDescriptor.getAsciiFixed(2, FieldValidators.getAn());
            case CardVerificationData:
                return FieldDescriptor.getAsciiFixed(4, FieldValidators.getAns());
            case ExtendedTranType:
                return FieldDescriptor.getAsciiFixed(4, FieldValidators.getN());
            case AddNodeData:
                return FieldDescriptor.getAsciiVar(3, 255, FieldValidators.getAns());
            case InquiryRspData:
                return FieldDescriptor.getAsciiVar(3, 999, FieldValidators.getAns());
            case RoutingInfo:
                return FieldDescriptor.getAsciiFixed(48, FieldValidators.getAns());
            case CardholderInfo:
                return FieldDescriptor.getAsciiVar(2, 50, FieldValidators.getAns());
            case AddressVerificationData:
                return FieldDescriptor.getAsciiFixed(29, FieldValidators.getAns());
            case CardVerificationResult:
                return FieldDescriptor.getAsciiFixed(1, FieldValidators.getAns());
            case AddressVerificationResult:
                return FieldDescriptor.getAsciiFixed(1, FieldValidators.getAn());
            case RetentionData:
                return FieldDescriptor.getAsciiVar(3, 999, FieldValidators.getAns());
            case BankDetails:
                return FieldDescriptor.getAsciiFixed(31, FieldValidators.getAns());
            case PayeeNameAddress:
                return FieldDescriptor.getAsciiFixed(253, FieldValidators.getAns());
            case PayerAccIdentification:
                return FieldDescriptor.getAsciiVar(2, 28, FieldValidators.getAns());
            case StructuredData:
                return FieldDescriptor.getAsciiVar(4, 9999, FieldValidators.getAns());
            default:
                throw new RuntimeException("Field not implemented: " + field.name());
        }
    }

    public int getFieldNr() {
        return Iso8583TermApp.Bit._048_PRIVATE_ADDITIONAL_DATA;
    }

    private List<Field> getFieldsFrom(byte[] bitmap) {
        int bit = (bitmap[0] & 0xff) * 256 + (bitmap[1] & 0xff);
        List<Field> fields = new ArrayList<Field>();
        for (Field field : Field.values()) {
            int a = 0x10000 >> field.field;
            int b = bit & a;
            if (a == b) {
                fields.add(field);
            }
        }

        return fields;
    }

    public int getMsgLength() throws Exception {
        int length = generateBitmap().length;

        for (Entry<Field, String> kvp : this.entrySet()) {
            IFieldDescriptor fieldDesc = getFieldDescriptor(kvp.getKey());
            length += fieldDesc.getPackedLenghth(kvp.getValue());
        }
        return length;
    }

    public int getPackedLength() throws Exception {
        return getMsgLength() + lenFormatter.getLengthOfLengthIndicator() + 3;
    }

    public String getValue() {
        return null;
    }

    public void setValue(String value) {
    }

    public void setOffset(int offset) {
        _offset = offset;
    }

    public int getOffset() {
        return _offset;
    }

    public void setFieldLength(int len) {
        _fieldLength = len;
    }

    public int getFieldLength() {
        return _fieldLength;
    }

    public byte[] toMsg() throws Exception {
        try {
            byte[] msg = new byte[getPackedLength()];
            lenFormatter.pack(msg, getPackedLength() - 4, 0);

            msg[4] = (byte) 0xf0;
            // next 2 bytes are length, binary, big endian. updated after to correct length
            msg[5] = 0x00;
            msg[6] = 0x00;
            int offset = 7;

            byte[] bitmap = generateBitmap();
            System.arraycopy(bitmap, 0, msg, offset, bitmap.length);
            offset += bitmap.length;

            for (Entry<Field, String> kvp : this.entrySet()) {
                IFieldDescriptor fieldDesc = getFieldDescriptor(kvp.getKey());
                byte[] fieldData = fieldDesc.pack(kvp.getKey().field, kvp.getValue());
                System.arraycopy(fieldData, 0, msg, offset, fieldData.length);
                offset += fieldData.length;
            }

            // go back and update length. length includes
            int length = offset - 7; // exclude 4 LLLL ascii chars, F0 byte and 2 length bytes
            msg[5] = (byte)((length / 256)%256);
            msg[6] = (byte)(length % 256);
            return msg;
        } catch (FieldFormatException ffe) {
            throw new FieldFormatException("48." + ffe.getFieldNr(), ffe.getFieldMessage());
        }
    }

    @Override
    public String toString() {
        try {
            return toString("");
        } catch (Exception e) {
            return "";
        }
    }

    public String toString(String prefix) throws Exception {
        StringBuffer sb = new StringBuffer();
        String newline = System.getProperty("line.separator");
        for (Entry<Field, String> kvp : this.entrySet()) {
            String fieldNr = IsoUtils.padLeft("" + kvp.getKey().field(), 3, '0');
            sb.append(newline);
            sb.append(prefix);
            sb.append("[Additional Data     ] 048.");
            sb.append(fieldNr);
            sb.append(" [");
            sb.append(kvp.getValue());
            sb.append("]");
        }

        int newlineLen = newline.length();
        return sb.toString().substring(newlineLen);
    }

    public int unpack(byte[] msg, int offset, boolean validateData) throws Exception {
        offset += 7; // 0xf0 + length
        byte[] bitmap = new byte[2];
        bitmap[0] = msg[offset];
        bitmap[1] = msg[offset + 1];
        offset += 2;

        List<Field> setFields = getFieldsFrom(bitmap);
        for (Field field : setFields) {
            IFieldDescriptor fieldDesc = getFieldDescriptor(field);
            UnpackObject unpacked = fieldDesc.unpack(field.field, msg, offset, validateData);
            put(field, unpacked.data);
            offset = unpacked.offset;
        }

        return offset;
    }

    public enum Field {
        PosData(1),
        AuthProfile(2),
        CardVerificationData(3),
        ExtendedTranType(4),
        AddNodeData(5),
        InquiryRspData(6),
        RoutingInfo(7),
        CardholderInfo(8),
        AddressVerificationData(9),
        CardVerificationResult(10),
        AddressVerificationResult(11),
        RetentionData(12),
        BankDetails(13),
        PayeeNameAddress(14),
        PayerAccIdentification(15),
        StructuredData(16);

        private int field;

        Field(int field) {
            this.field = field;
        }

        public int field() {
            return field;
        }
    }
}
