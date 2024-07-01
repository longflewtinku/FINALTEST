package com.linkly.libengine.engine.protocol.svfe.openisoj.formatter;

import com.linkly.libengine.engine.protocol.svfe.openisoj.IsoUtils;

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

    public String getString(byte[] data) throws Exception {
        return new String(data);
    }

}
