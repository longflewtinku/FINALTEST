package com.linkly.libengine.engine.protocol.svfe.openisoj.formatter;

import com.linkly.libengine.engine.protocol.svfe.openisoj.IsoUtils;
import com.linkly.libengine.engine.protocol.svfe.openisoj.Validators;
import com.linkly.libengine.engine.protocol.svfe.openisoj.exceptions.FormatException;

public class BinaryFormatter implements IFormatter {

    public byte[] getBytes(String value) throws Exception {
        if (!Validators.isHex(value)) {
            throw new FormatException("Value \"" + value + "\" is not valid HEX");
        }

        int length = value.length();
        if (length % 2 != 0) {
            length++;
            value = IsoUtils.padLeft(value, length, '0');
        }

        int numberChars = value.length();
        byte[] bytes = new byte[numberChars / 2];

        for (int i = 0; i < numberChars; i += 2) {
            bytes[i / 2] = (byte) Integer.valueOf(value.substring(i, i + 2), 16).intValue();
        }
        return bytes;
    }

    public byte[] getBytes( int value, int length, char padChar ) throws Exception {
        // NOTE: as we're converting to binary, and the input length is in bytes, we padding to length * 2 (to get number of nibbles)
        // e.g. if value=57, and length=2, then we want to return bytes 00, 57
        return getBytes( IsoUtils.padLeft(Integer.toString(value), length*2, '0' ) );
    }

    public int getPackedLength(int unpackedLength) {
        if (unpackedLength % 2 != 0) {
            return (unpackedLength + 1) / 2;
        }
        return unpackedLength / 2;
    }

    public int getSourceFieldLenForPackedLength(int fieldLength){
        return fieldLength * 2;
    }

    public String getString(byte[] data) {
        StringBuilder sb = new StringBuilder();
        for (byte b : data) {
            byte highNibble = (byte) ((b & 0xF0) >> 4);
            byte lowNibble = (byte) (b & 0x0F);
            sb.append(Integer.toHexString(highNibble).toUpperCase());
            sb.append(Integer.toHexString(lowNibble).toUpperCase());
        }

        return sb.toString();
    }

}
