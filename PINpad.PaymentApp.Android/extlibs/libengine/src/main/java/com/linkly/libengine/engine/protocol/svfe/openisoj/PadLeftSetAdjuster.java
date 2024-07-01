package com.linkly.libengine.engine.protocol.svfe.openisoj;

public class PadLeftSetAdjuster extends Adjuster {

    private int _length;
    private char _padChar;

    public PadLeftSetAdjuster(int length, char padChar) {
        _length = length;
        _padChar = padChar;
    }

    @Override
    protected String set(String value) {
        String val = value;
        if (value == null) {
            val = "";
        }

        return IsoUtils.padLeft(val, _length, _padChar);
    }
}
