package com.linkly.launcher.fragments;

import android.os.Handler;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.linkly.launcher.BaseStation;
import com.linkly.launcher.fragments.TestingConnectionFragment.ConnectionTestStatus;
import com.linkly.launcher.fragments.TestingConnectionFragment.ConnectionTestStatus.TestState;

import java.util.concurrent.Executor;

import timber.log.Timber;

public class TestingConnectionViewModel extends ViewModel {

    private final MutableLiveData<ConnectionTestStatus> connectionTestStatus = new MutableLiveData<>();

    public LiveData<ConnectionTestStatus> getConnectionTestStatus() {
        return connectionTestStatus;
    }

    public void startConnect(Executor executor, Handler resultHandler) {
        executor.execute(() -> {
            try {
                String ssid = BaseStation.getInstance().getCurrentBaseStationInfo().getWifiSsid();
                String pwd = BaseStation.getInstance().getCurrentBaseStationInfo().getWifiPasswd();
                updateIcons(resultHandler, TestState.RUNNING, TestState.NOT_RUNNING, TestState.NOT_RUNNING);

                if (!BaseStation.getInstance().wifiStart()) {
                    updateIcons(resultHandler, TestState.FAILED, TestState.NOT_RUNNING, TestState.NOT_RUNNING);
                    return;
                }
                if (!BaseStation.getInstance().wifiConnect(ssid, pwd)) {
                    updateIcons(resultHandler, TestState.FAILED, TestState.NOT_RUNNING, TestState.NOT_RUNNING);
                    return;
                }

                updateIcons(resultHandler, TestState.SUCCESS, TestState.RUNNING, TestState.NOT_RUNNING);
                Thread.sleep(1000); /* delay requested by optomany for aesthetics */
                if (!BaseStation.getInstance().wifiCheckNetwork()) {
                    updateIcons(resultHandler, TestState.SUCCESS, TestState.FAILED, TestState.NOT_RUNNING);
                    return;
                }

                updateIcons(resultHandler, TestState.SUCCESS, TestState.SUCCESS, TestState.RUNNING);
                Thread.sleep(1000); /* delay requested by optomany for aesthetics */

                if (!BaseStation.getInstance().wifiCheckHost()) {
                    updateIcons(resultHandler, TestState.SUCCESS, TestState.SUCCESS, TestState.FAILED);
                    return;
                }
                updateIcons(resultHandler, TestState.SUCCESS, TestState.SUCCESS, TestState.SUCCESS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                Timber.e(e);
            }
        });
    }

    private void updateIcons(Handler resultHandler, TestState wifi, TestState network, TestState testConnect) {
        resultHandler.post(() -> connectionTestStatus.postValue(new ConnectionTestStatus(wifi, network, testConnect)));
    }

    public void reset() {
        connectionTestStatus.postValue(null);
    }
}
