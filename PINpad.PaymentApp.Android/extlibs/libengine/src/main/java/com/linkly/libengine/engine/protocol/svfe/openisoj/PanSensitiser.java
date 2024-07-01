package com.linkly.libengine.engine.protocol.svfe.openisoj;

public class PanSensitiser implements Sensitiser {
    private static final Sensitiser instance = new PanSensitiser();

    private PanSensitiser() {
    }

    public static Sensitiser getInstance() {
        return instance;
    }

    public String sensitise(String data) {
        // We need to leave the first six and the last four of the PAN
        if (data == null) {
            return null;
        }

        int endMaskStart = data.length() - 4;
        char[] dataChars = data.toCharArray();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < dataChars.length; i++) {
            if (i < 6) {
                sb.append(dataChars[i]);
            } else if (i < endMaskStart) {
                sb.append("*");
            } else {
                sb.append(dataChars[i]);
            }
        }

        return sb.toString();
    }
}
