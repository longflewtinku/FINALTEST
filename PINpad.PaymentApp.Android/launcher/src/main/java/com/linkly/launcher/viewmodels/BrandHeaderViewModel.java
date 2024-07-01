package com.linkly.launcher.viewmodels;

import android.graphics.Bitmap;
import android.widget.ImageView;

import androidx.databinding.BindingAdapter;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.linkly.launcher.BrandingConfig;

public class BrandHeaderViewModel extends ViewModel {

    public LiveData<Bitmap> getBrandDisplayLogoHeader() {
        return BrandingConfig.getInstance().getBrandDisplayLogoHeader();
    }

    public LiveData<Integer> getCurrentBrandDisplayStatusBarColour() {
        return BrandingConfig.getInstance().getBrandDisplayStatusBarColour();
    }

    // Commented out due to conflicting warnings. This code when added will cause issues.
    // Seems that we still on fresh install this code doesn't get called and the logo still
    // gets displayed
    //@BindingAdapter("imageBitmap")
    public static void loadImage(ImageView iv, Bitmap bitmap) {
        iv.setImageBitmap(bitmap);
    }
}
