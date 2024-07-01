package com.linkly.libengine.engine.protocol.iso8583.openisoj;

import com.linkly.libengine.engine.protocol.iso8583.openisoj.fieldvalidator.IFieldValidator;

import timber.log.Timber;

public class Field implements IField {

    protected IFieldDescriptor _fieldDescriptor;
    private int _fieldNr;
    private String _value;
    private int _offset; // offset to beginning of field
    private int _fieldLength; // length of entire field, including any variable length indicators

    public Field(int fieldNumber, IFieldDescriptor fieldDescriptor) {
        _fieldNr = fieldNumber;
        _fieldDescriptor = fieldDescriptor;
    }

    public static IField getAsciiVar(int fieldNr, int lengthIdnicator, int maxLength, IFieldValidator validator)
            throws Exception {
        return new Field(fieldNr, FieldDescriptor.getAsciiVar(lengthIdnicator, maxLength, validator));
    }

    public static IField getBinFixed(int fieldNr, int packedLength) throws Exception {
        return new Field(fieldNr, FieldDescriptor.getBinaryFixed(packedLength));
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

    public int getFieldNr() {
        return _fieldNr;
    }

    public int getPackedLength() throws Exception {
        try {
            return _fieldDescriptor.getPackedLenghth(_value);
        } catch (Exception e) {
            Timber.w(e);
        }
        return 0;
    }

    public String getValue() {
        return _fieldDescriptor.getAdjuster() == null ? _value : _fieldDescriptor.getAdjuster().get(_value);
    }

    public void setValue(String value) {
        _value = _fieldDescriptor.getAdjuster() == null ? value : _fieldDescriptor.getAdjuster().set(value);

    }

    public byte[] toMsg() throws Exception {
        return _fieldDescriptor.pack(_fieldNr, _value);
    }

    @Override
    public String toString() {
        try {
            return toString("");
        } catch (Exception e) {
            return "ERROR IN DISPLAY!";
        }
    }

    public String toString(String prefix) throws Exception {
        return _fieldDescriptor.display(prefix, _fieldNr, _value);
    }

    public int unpack(byte[] msg, int offset, boolean validateData) {
        UnpackObject obj = _fieldDescriptor.unpack(_fieldNr, msg, offset, validateData);
        _value = obj.data;
        return obj.offset;
    }

}
