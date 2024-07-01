package com.linkly.libengine.engine.protocol.iso8583.openisoj.formatter;

public class Formatters {
    public static IFormatter getAscii() {
        return new AsciiFormatter();
    }

    public static IFormatter getBcdFixed() {
        return new BcdFormatter(true);
    }

    public static IFormatter getBcdVar() {
        return new BcdFormatter(false);
    }

    public static IFormatter getBinary() {
        return new BinaryFormatter();
    }
}
