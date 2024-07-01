package com.linkly.launcher.service;

import android.os.AsyncTask;

import com.linkly.launcher.BaseStation;


@SuppressWarnings("deprecation")
public class UpdateFirmware extends AsyncTask<Object,Object,Object>  {
    public UpdateFirmware() {
        super();
    }

    @Override
    protected Object doInBackground(Object... objects) {
        BaseStation.getInstance().updateFirmware();
        return null;
    }
}
