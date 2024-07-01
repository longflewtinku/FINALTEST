package com.linkly.libengine.engine.protocol.svfe.openisoj.formatter;

import com.linkly.libengine.engine.protocol.svfe.openisoj.IsoUtils;

public class BcdFormatter implements IFormatter {

    public byte[] getBytes(String value) throws Exception {
        if (value.length() % 2 == 1) {
            value = IsoUtils.padLeft(value, value.length() + 1, '0');
        }

        char[] chars = value.toCharArray();
        int length = chars.length / 2;

        byte[] bytes = new byte[length];

        for (int i = 0; i < length; i++) {
            byte highNibble = Byte.parseByte(String.valueOf(chars[2 * i]));
            byte lowNibble = Byte.parseByte(String.valueOf(chars[2 * i + 1]));

            bytes[i] = (byte) ((byte) (highNibble << 4) | lowNibble);
        }

        return bytes;
    }

    public byte[] getBytes( int value, int length, char padChar ) throws Exception {
        return getBytes( IsoUtils.padLeft(Integer.toString(value), length * 2, '0') );
    }

    public int getPackedLength(int unpackedLength) throws Exception {
        int i = (unpackedLength % 2);
        int b = (unpackedLength / 2);
        return b + i;
    }

    public int getSourceFieldLenForPackedLength(int fieldLength){
        return fieldLength * 2;
    }

    public String getString(byte[] data) throws Exception {
        StringBuffer sb = new StringBuffer();
        for (byte b : data) {
            byte highNibble = (byte) ((b & 0xF0) >> 4);
            byte lowNibble = (byte) (b & 0x0F);
            sb.append(highNibble);
            sb.append(lowNibble);
        }

        return sb.toString();
    }

}
