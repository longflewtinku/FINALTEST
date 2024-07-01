package com.linkly.libengine.engine.protocol.iso8583.openisoj.exceptions;

import com.linkly.libengine.engine.protocol.iso8583.As2805TillPack.MsgType;

@SuppressWarnings("serial")
public class PackRuntimeException extends RuntimeException {


    public PackRuntimeException(String msgNumber, Throwable cause) {
        super("Unable to pack with message number " + msgNumber, cause);
    }

    public PackRuntimeException(MsgType msgType, Throwable cause) {
        super("Unable to pack for message type " + msgType.name(), cause);
    }
}
