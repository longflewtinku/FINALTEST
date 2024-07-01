package com.linkly.libengine.engine.protocol.svfe.openisoj.formatter;

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
    public byte[] getBytes( int value, int length, char padChar ) throws Exception;

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
     * @return Converted data
     */
    public String getString(byte[] data) throws Exception;
}
