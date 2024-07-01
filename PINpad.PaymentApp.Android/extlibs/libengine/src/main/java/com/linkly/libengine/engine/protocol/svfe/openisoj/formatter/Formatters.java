package com.linkly.libengine.engine.protocol.svfe.openisoj.formatter;

public class Formatters {
    public static IFormatter getAscii() {
        return new AsciiFormatter();
    }

    public static IFormatter getBcd() {
        return new BcdFormatter();
    }

    public static IFormatter getBinary() {
        return new BinaryFormatter();
    }
}
