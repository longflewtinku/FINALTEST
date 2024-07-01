package com.linkly.libengine.engine.protocol.iso8583.openisoj;

public class UnpackObject {
    public String data;

    public int offset;

    public UnpackObject() {
    }

    public UnpackObject(String data, int offset) {
        this.data = data;
        this.offset = offset;
    }
}
