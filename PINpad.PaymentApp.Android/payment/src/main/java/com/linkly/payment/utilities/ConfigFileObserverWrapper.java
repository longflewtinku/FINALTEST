package com.linkly.payment.utilities;

import android.os.Build;
import android.os.FileObserver;

import androidx.annotation.Nullable;

import java.io.File;

public class ConfigFileObserverWrapper {
    public interface FileObserverCallback {
        void onEvent(int mask, String path);
    }

    private FileObserver fileObserver = null;
    private FileObserverCallback fileObserverCallback = null;

    // Annoyingly we need to wrap this as some of the constructors for FileObserver have been deprecated.
    // Cannot wrap build.version check in a constructor when super is called.
    @SuppressWarnings("deprecation")
    private void initConfigFileObserverWrapper(String path, int mask) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            fileObserver = new FileObserver(new File(path), mask) {
                @Override
                public void onEvent(int event, @Nullable String path) {
                    fileObserverCallback.onEvent(event, path);
                }
            };
        } else {
            fileObserver = new FileObserver(path, mask) {
                @Override
                public void onEvent(int event, @Nullable String path) {
                    fileObserverCallback.onEvent(event, path);
                }
            };
        }
    }

    public ConfigFileObserverWrapper(String path, int mask, FileObserverCallback callback) {
        initConfigFileObserverWrapper(path, mask);
        fileObserverCallback = callback;
    }

    public void startWatching() {
        if (fileObserver != null) {
            fileObserver.startWatching();
        }
    }

    public void stopWatching() {
        if (fileObserver != null) {
            fileObserver.stopWatching();
        }
    }
}
