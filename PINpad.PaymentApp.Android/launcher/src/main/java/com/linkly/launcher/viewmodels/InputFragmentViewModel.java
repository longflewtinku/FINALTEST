package com.linkly.launcher.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.Timer;
import java.util.TimerTask;

import timber.log.Timber;

public class InputFragmentViewModel extends ViewModel {
    private static final long UNATTENDED_AUTO_CANCEL_TIMEOUT_MS = 90 * 1000L;
    private MutableLiveData<Boolean> mOnUnattendedAutoCancelTimeout = new MutableLiveData<>(false);

    public LiveData<Boolean> onUnattendedAutoCancelTimeout() {
        return mOnUnattendedAutoCancelTimeout;
    }

    public void startUnattendedAutoCancelTimeout() {
        Timber.d("startUnattendedAutoCancelTimeout...");
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                Timber.d("run[mUnattendedAutoCancelTask]...");
                mOnUnattendedAutoCancelTimeout.postValue(true);
            }
        };
        timer.schedule(task, UNATTENDED_AUTO_CANCEL_TIMEOUT_MS);
    }
}
