package com.linkly.libengine.helpers;

import android.util.Pair;

import com.linkly.libengine.dependencies.IDependency;
import com.linkly.libmal.global.util.Util;

import timber.log.Timber;

public class ConfigParsing {
    private ConfigParsing() {}
    public static Pair<String, String> parseInternetTestTarget(IDependency d, String defaultUrl, String defaultPort) {
        String configHost = d.getPayCfg().getCommsFallbackHost();
        if(!configHost.isEmpty()) {
            String[] parts = configHost.split(":");
            // apply config param if present and not empty
            if (parts.length == 2 && !Util.isNullOrEmpty(parts[0]) && !Util.isNullOrEmpty(parts[1])) {
                return new Pair<>(parts[0], parts[1]);
            }
            // else use default host and port
        }
        // else use default host and port
        Timber.e("Using comms fallback host %s, port %s", defaultUrl, defaultPort);
        return new Pair<>(defaultUrl, defaultPort);
    }
}
