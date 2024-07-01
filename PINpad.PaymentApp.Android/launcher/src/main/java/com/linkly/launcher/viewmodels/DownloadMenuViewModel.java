package com.linkly.launcher.viewmodels;

import android.graphics.drawable.GradientDrawable;
import android.widget.Button;

import androidx.databinding.BindingAdapter;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.linkly.launcher.BrandingConfig;

public class DownloadMenuViewModel extends ViewModel {

    public LiveData<Integer> getBrandDisplayPrimaryColour() {
        return BrandingConfig.getInstance().getBrandDisplayPrimaryColour();
    }

    public LiveData<Integer> getBrandDisplayButtonTextColour() {
        return BrandingConfig.getInstance().getBrandDisplayButtonTextColour();
    }

    public LiveData<Integer> getBrandDisplayButtonColour() {
        return BrandingConfig.getInstance().getBrandDisplayButtonColour();
    }

    @BindingAdapter("buttonBackground")
    public static void setButtonBackground(Button b, int color) {
        GradientDrawable updateDrawable = (GradientDrawable) b.getBackground();
        updateDrawable.setColor(color);
        b.setBackground(updateDrawable);
    }
}
