package com.linkly.libengine.engine.protocol.svfe.openisoj;

public class PadRightSetAdjuster extends Adjuster {

    private int _length;
    private char _padChar;

    public PadRightSetAdjuster(int length, char padChar) {
        _length = length;
        _padChar = padChar;
    }

    @Override
    protected String set(String value) {
        String val = value;
        if (val == null) {
            val = "";
        }

        return IsoUtils.padRight(val, _length, _padChar);
    }
}
