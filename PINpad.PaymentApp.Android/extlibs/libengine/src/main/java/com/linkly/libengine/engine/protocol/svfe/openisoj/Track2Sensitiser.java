package com.linkly.libengine.engine.protocol.svfe.openisoj;

import com.linkly.libmal.global.util.Util;

public class Track2Sensitiser implements Sensitiser {
    private static final Sensitiser instance = new Track2Sensitiser();

    private Track2Sensitiser() {
    }

    public static Sensitiser getInstance() {
        return instance;
    }

    public String sensitise(String data) {
        if (data == null) {
            return null;
        }

        Sensitiser panSenistiser = PanSensitiser.getInstance();
        StringBuilder sb = new StringBuilder();

        String delim = "=";
        if (data.contains("D")) {
            delim = "D";
        }

        Track2 track = new Track2(data);
        String pan = track.getPan();
        String expiry = track.getExpiry();
        String src = track.getServiceRestrictionCode();
        String dD = track.getDiscretionaryData();

        if (expiry == null && src == null && dD == null) {
            sb.append(panSenistiser.sensitise(pan));
            int remaining = data.length() - pan.length();
            sb.append(data.substring(data.length() - remaining));
        } else {
            sb.append(panSenistiser.sensitise(pan)).append(delim);
            sb.append(Util.isNullOrEmpty(expiry) ? delim : expiry);
            sb.append(Util.isNullOrEmpty(src) ? delim : src);
            if (!Util.isNullOrEmpty(dD)) {
                int length = dD.length();
                sb.append(IsoUtils.padRight("", length, '*'));
            }
        }

        // When some retarded banks don't have a SRC or delimiter, the above method will insert
        // a delimiter. This is some horrible code that will stop the delimiter appearing in the
        // sensitised data.
        return sb.toString().substring(0, data.length());

    }
}
