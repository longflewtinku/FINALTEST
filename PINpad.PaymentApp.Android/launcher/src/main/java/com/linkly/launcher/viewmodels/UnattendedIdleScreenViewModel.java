package com.linkly.launcher.viewmodels;

import android.graphics.Bitmap;
import android.widget.ImageView;

import androidx.databinding.BindingAdapter;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.linkly.launcher.BrandingConfig;
import com.linkly.libconfig.MalConfig;
import com.linkly.libconfig.ProfileCfg;

public class UnattendedIdleScreenViewModel extends ViewModel {

    public LiveData<Bitmap> getBrandDisplayLogoIdle() {
        return BrandingConfig.getInstance().getBrandDisplayLogoIdle();
    }

    public String getMessageText() {
        ProfileCfg profileConfig = MalConfig.getInstance().getProfileCfg();
        return profileConfig != null? profileConfig.getKioskScreenText() : null;
    }

    @BindingAdapter("imageBitmap")
    public static void loadImage(ImageView iv, Bitmap bitmap) {
        iv.setImageBitmap(bitmap);
    }
}
