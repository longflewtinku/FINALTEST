package com.linkly.libengine.engine.protocol.svfe.openisoj;

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
