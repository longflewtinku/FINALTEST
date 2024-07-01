package com.linkly.libengine.engine.protocol.svfe.openisoj;

public interface IField {
    public int getFieldNr();

    public int getPackedLength() throws Exception;

    public String getValue();

    public void setValue(String value);

    public byte[] toMsg() throws Exception;

    public String toString();

    public String toString(String prefix) throws Exception;

    public int unpack(byte[] msg, int offset) throws Exception;
}
