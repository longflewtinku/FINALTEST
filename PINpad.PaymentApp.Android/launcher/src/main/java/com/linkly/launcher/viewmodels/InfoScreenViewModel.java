package com.linkly.launcher.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.linkly.launcher.BrandingConfig;

public class InfoScreenViewModel extends ViewModel {

    public LiveData<Integer> getBrandDisplayButtonColour() {
        return BrandingConfig.getInstance().getBrandDisplayButtonColour();
    }

    public LiveData<Integer> getBrandDisplayButtonTextColour() {
        return BrandingConfig.getInstance().getBrandDisplayButtonTextColour();
    }

    public LiveData<Boolean> getAutoLaunchInProgress() {
        return BrandingConfig.getInstance().getAutoLaunchInProgress();
    }
}
