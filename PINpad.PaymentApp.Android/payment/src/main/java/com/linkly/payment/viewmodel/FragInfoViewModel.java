package com.linkly.payment.viewmodel;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.linkly.libmal.global.util.Util;
import com.linkly.libui.IUIDisplay;
import com.linkly.payment.R;
import com.linkly.payment.viewmodel.data.UIFragData;

public class FragInfoViewModel extends BaseViewModel {

    public final MutableLiveData<Boolean> titleVisible= new MutableLiveData<>();
    public final MutableLiveData<Boolean> promptVisible= new MutableLiveData<>();
    public final MutableLiveData<Boolean> imageProcessing = new MutableLiveData<>();
    public final MutableLiveData<Boolean> imageDeclined = new MutableLiveData<>();
    public final MutableLiveData<Boolean> imageCheck = new MutableLiveData<>();
    public final MutableLiveData<Boolean> imagePrinting = new MutableLiveData<>();
    public final MutableLiveData<Boolean> background = new MutableLiveData<>();
    public final MutableLiveData<Boolean> padding = new MutableLiveData<>();
    public final MutableLiveData<Integer> backgroundResource = new MutableLiveData<>();

    public LiveData<Boolean> getTitleVisible() {return titleVisible;}
    public LiveData<Boolean> getPromptVisible() {return promptVisible;}
    public LiveData<Boolean> getImageProcessing() {return imageProcessing;}
    public LiveData<Boolean> getImageDeclined() {return imageDeclined;}
    public LiveData<Boolean> getImageCheck() {return imageCheck;}
    public LiveData<Boolean> getImagePrinting() {return imagePrinting;}
    public LiveData<Boolean> getPadding() {return padding;}
    public LiveData<Boolean> getBackground() {return background;}
    public LiveData<Integer> getBackgroundResource() {return backgroundResource;}


    public FragInfoViewModel(Application application) {
        super(application);
    }


    /* overrides parent so we can update the local values for the correct display */
    protected void updateViewModel(MutableLiveData<UIFragData> fragData) {

        this.fragData = fragData;

        String t = fragData.getValue().getTitle().getValue();

        titleVisible.setValue(!Util.isNullOrWhitespace(t));

        String p = fragData.getValue().getPrompt().getValue();
        promptVisible.setValue(!Util.isNullOrWhitespace(p));

        IUIDisplay.SCREEN_ICON infoIcon = fragData.getValue().getInfoIcon().getValue();

        padding.setValue(false);
        background.setValue(true);

        if ( infoIcon == IUIDisplay.SCREEN_ICON.NO_ICON ) {
            padding.setValue(true);
            background.setValue(false);
            backgroundResource.setValue(R.drawable.bg_in_progress);
        } else if (infoIcon == IUIDisplay.SCREEN_ICON.SUCCESS_ICON) {

            backgroundResource.setValue(R.drawable.bg_authorised);
        } else if (infoIcon == IUIDisplay.SCREEN_ICON.ERROR_ICON) {

            backgroundResource.setValue(R.drawable.bg_try_again);
        } else if (infoIcon == IUIDisplay.SCREEN_ICON.ERROR_ICON_WITH_BEEP) {

            backgroundResource.setValue(R.drawable.bg_try_again);
        } else if (infoIcon == IUIDisplay.SCREEN_ICON.IN_PROGRESS) {
            backgroundResource.setValue(R.drawable.bg_in_progress);
        } else if (infoIcon == IUIDisplay.SCREEN_ICON.PROCESSING_ICON_STILL) {
            backgroundResource.setValue(R.drawable.bg_remove_card);
        }  else {
            backgroundResource.setValue(R.drawable.bg_in_progress);
        }


    }

}
