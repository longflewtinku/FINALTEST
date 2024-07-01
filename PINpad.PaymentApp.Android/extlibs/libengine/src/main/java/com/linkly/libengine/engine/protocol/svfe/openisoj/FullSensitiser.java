package com.linkly.libengine.engine.protocol.svfe.openisoj;

/**
 * Sensitise everything
 *
 * @author John Oxley &lt;john.oxley@gmail.com&gt;
 */
public class FullSensitiser implements Sensitiser {
    private static FullSensitiser instance = new FullSensitiser();

    private FullSensitiser() {
    }

    public static FullSensitiser getInstance() {
        return instance;
    }

    public String sensitise(String data) {
        if (data == null) {
            return null;
        }
        return IsoUtils.padLeft("", data.length(), '*');
    }
}
