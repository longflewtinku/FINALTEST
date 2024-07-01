package com.linkly.libengine.engine.protocol.svfe.openisoj;

public interface IMessage {
    public byte[] toMsg() throws Exception;

    public int unpack(byte[] msg, int offset) throws Exception;
}
