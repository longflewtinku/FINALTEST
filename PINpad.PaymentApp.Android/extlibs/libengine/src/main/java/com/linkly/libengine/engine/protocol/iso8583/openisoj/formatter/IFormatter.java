package com.linkly.libengine.engine.protocol.iso8583.openisoj.formatter;

public interface IFormatter {
    /**
     * Format the String and return as an encoded byte array
     *
     * @param value value to format
     * @return Encoded byte array
     */
    public byte[] getBytes(String value) throws Exception;

    /**
     * Format the given integer, left padding to length, and return as an encoded byte array
     *
     * @param value value to format
     * @return Encoded byte array
     */
    public byte[] getBytes(int value, int length, char padChar) throws Exception;

     /**
     * Gets the packed length of the data given the unpacked length
     *
     * @param unpackedLength Unpacked Length
     * @return Packet length of the data
     */
    int getPackedLength(int unpackedLength) throws Exception;

    /**
     * Takes the byte array and converts it to a String for use
     *
     * @param data Data to convert
     * @param fieldLen length of field to decode
     * @return Converted data
     */
    public String getString(byte[] data, int fieldLen) throws Exception;
    public String getString(byte[] data) throws Exception;

    /**
     * Returns number of input elements for given string length
     * Used for input length validation against template
     *
     * @param stringLen string length of input data
     * @return element count of input data
     */
    public int getInputLength( int stringLen );

    /**
     * reverse of 'getPackedLength' - returns source field size for given output field length (bytes)
     * e.g.
     * binary data is specified as size (byte) in template, e.g. 8. but internally is specified as a hex string, so packed length 8 requires 16 chars of input
     * bcd is specified as number of bcd digits in template, and internally is specified as numeric string, so 1:1
     * ascii is specified as number of ascii chars in template, and internally is specified as chars, so 1:1
     *
     * @param fieldLength - packed field size in bytes
     * @return corresponding field size for input
     */
    int getSourceFieldLenForPackedLength(int fieldLength);
}
