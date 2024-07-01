package com.linkly.libengine.action;

import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.linkly.libengine.config.PayCfgFactory;
import com.linkly.libengine.config.paycfg.PayCfgImpl;

import timber.log.Timber;

public class ReadHotloadParams extends IAction{
    @Override
    public String getName() {
        return "ReadHotloadParams";
    }

    @Override
    public void run() {
        Timber.e("Running parsing of Hotload Params");

        if(new PayCfgFactory().loadHotloadParams(d.getPayCfg(), mal.getFile())) {
            // This shouldn't be here but the way this is architected nothing we can do :(
            new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(context, "Successfully Loaded New\nPayment App Configuration", Toast.LENGTH_SHORT).show());

            // We need to reload our menu options.
        }
    }
}
