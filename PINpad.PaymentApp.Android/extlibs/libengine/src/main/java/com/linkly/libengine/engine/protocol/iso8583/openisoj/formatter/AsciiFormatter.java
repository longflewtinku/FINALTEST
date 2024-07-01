package com.linkly.libengine.engine.protocol.iso8583.openisoj.formatter;

import com.linkly.libengine.engine.protocol.iso8583.openisoj.IsoUtils;

public class AsciiFormatter implements IFormatter {

    public byte[] getBytes(String value) throws Exception {
        return value.getBytes();
    }

    public byte[] getBytes( int value, int length, char padChar ) throws Exception {
        return IsoUtils.padLeft(Integer.toString(value), length, '0').getBytes();
    }

    public int getPackedLength(int unpackedLength) throws Exception {
        return unpackedLength;
    }

    public int getSourceFieldLenForPackedLength(int fieldLength){
        // ascii is specified as number of ascii chars in template, and internally is specified as chars, so 1:1
        return fieldLength;
    }

    public String getString(byte[] data, int fieldLen) throws Exception {
        return new String(data);
    }

    public String getString(byte[] data) throws Exception {
        return new String(data);
    }

    public int getInputLength( int stringLen ) {
        return stringLen;
    }

}
