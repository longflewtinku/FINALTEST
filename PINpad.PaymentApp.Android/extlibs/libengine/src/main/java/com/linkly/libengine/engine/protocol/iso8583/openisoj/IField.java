package com.linkly.libengine.engine.protocol.iso8583.openisoj;

public interface IField {
    public int getFieldNr();

    public int getPackedLength() throws Exception;

    public String getValue();

    public void setValue(String value);
    public void setOffset(int offset);
    public int getOffset();
    public void setFieldLength(int len);
    public int getFieldLength();

    public byte[] toMsg() throws Exception;

    public String toString();

    public String toString(String prefix) throws Exception;

    public int unpack(byte[] msg, int offset, boolean validateData) throws Exception;
}
