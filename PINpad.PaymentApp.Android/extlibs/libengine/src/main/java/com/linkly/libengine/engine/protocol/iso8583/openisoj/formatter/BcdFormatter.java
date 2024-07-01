package com.linkly.libengine.engine.protocol.iso8583.openisoj.formatter;

import com.linkly.libengine.engine.protocol.iso8583.openisoj.IsoUtils;

public class BcdFormatter implements IFormatter {
    private boolean fixedLengthField;

    public BcdFormatter( boolean fixedLengthField ) {
        this.fixedLengthField = fixedLengthField;
    }

    public byte[] getBytes(String value) throws Exception {

        if (value.length() % 2 == 1) {
            if( fixedLengthField ) {
              // fixed-length bcd fields are left-padded with 0 if odd length
                value = IsoUtils.padLeft(value, value.length() + 1, '0');
            } else {
                // variable length bcd fields are right-padded with f if odd length
                value += 'F';
            }
        }

        char[] chars = value.toCharArray();
        int length = chars.length / 2;

        byte[] bytes = new byte[length];

        for (int i = 0; i < length; i++) {
            // magic number 16 here indicates the radix/base for parsing of hexadecimal data (0-9 and a-f chars)
            byte highNibble = Byte.parseByte(String.valueOf(chars[2 * i]),16);
            byte lowNibble = Byte.parseByte(String.valueOf(chars[2 * i + 1]),16);

            bytes[i] = (byte) ((byte) (highNibble << 4) | lowNibble);
        }

        return bytes;
    }

    public byte[] getBytes( int value, int length, char padChar ) throws Exception {
        return getBytes( IsoUtils.padLeft(Integer.toString(value), length, '0') );
    }

    public int getPackedLength(int unpackedLength) throws Exception {
        int i = (unpackedLength % 2);
        int b = (unpackedLength / 2);
        return b + i;
    }

    public int getSourceFieldLenForPackedLength(int fieldLength){
        // bcd is specified as number of bcd digits in template, and internally is specified as numeric string, so 1:1
        return fieldLength;
    }

    public String getString(byte[] data) throws Exception {
        // use 0 field length to avoid padding issues
        return getString( data, 0 );
    }

    public String getString(byte[] data, int fieldLen) throws Exception {
        StringBuffer sb = new StringBuffer();
        for (byte b : data) {
            byte highNibble = (byte) ((b & 0xF0) >> 4);
            byte lowNibble = (byte) (b & 0x0F);
            sb.append(highNibble);
            sb.append(lowNibble);
        }

        String outputString = sb.toString();

        if( fieldLen % 2 == 1 ) {
            // field length is odd, we'll have padding to strip out
            if( fixedLengthField ) {
                // fixed length fields have padding char on left
                return outputString.substring( 1, fieldLen+1 );
            } else {
                // var fields have padding char on right
                return outputString.substring( 0, fieldLen );
            }
        }

        return outputString;
    }

    public int getInputLength( int stringLen ) {
        return stringLen;
    }

}
