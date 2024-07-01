package com.linkly.libengine.tlv;

public interface IBerTlvLogger {

    boolean isDebugEnabled();

    void debug(String aFormat, Object ...args);
}
