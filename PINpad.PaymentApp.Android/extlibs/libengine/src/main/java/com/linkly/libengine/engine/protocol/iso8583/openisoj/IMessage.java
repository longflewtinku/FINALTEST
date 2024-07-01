package com.linkly.libengine.engine.protocol.iso8583.openisoj;

public interface IMessage {
    public byte[] toMsg() throws Exception;

    public int unpack(byte[] msg, int offset, boolean validateData) throws Exception;
}
