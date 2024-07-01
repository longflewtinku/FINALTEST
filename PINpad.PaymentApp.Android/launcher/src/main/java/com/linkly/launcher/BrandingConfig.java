package com.linkly.launcher;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.gson.Gson;
import com.linkly.libmal.MalFactory;
import com.linkly.libmal.global.util.Util;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import timber.log.Timber;

public class BrandingConfig {
    static class Parameters {
        String brandDisplayLogoHeader;
        String brandDisplayLogoIdle;
        String brandDisplayLogoSplash;
        String brandDisplayStatusBarColour;
        String brandDisplayButtonColour;
        String brandDisplayButtonTextColour;
        String brandDisplayPrimaryColour;
        String brandReceiptLogoHeader;
    }

    private static BrandingConfig instance;

    public static BrandingConfig getInstance() {
        if( instance == null ) {
            instance = new BrandingConfig();
        }
        return instance;
    }

    public BrandingConfig() {
        reloadBrandingData();
    }

    private final MutableLiveData<Bitmap> brandDisplayLogoHeader = new MutableLiveData<>();
    public LiveData<Bitmap> getBrandDisplayLogoHeader() { return brandDisplayLogoHeader; }

    private final MutableLiveData<Bitmap> brandDisplayLogoIdle = new MutableLiveData<>();
    public LiveData<Bitmap> getBrandDisplayLogoIdle() { return brandDisplayLogoIdle; }

    private final MutableLiveData<Integer> brandDisplayStatusBarColour = new MutableLiveData<>();
    public LiveData<Integer> getBrandDisplayStatusBarColour() { return brandDisplayStatusBarColour; }

    private static final MutableLiveData<Integer> brandDisplayPrimaryColour = new MutableLiveData<>();
    public LiveData<Integer> getBrandDisplayPrimaryColour() { return brandDisplayPrimaryColour; }

    private static final MutableLiveData<Integer> brandDisplayButtonColour = new MutableLiveData<>();
    public LiveData<Integer> getBrandDisplayButtonColour() { return brandDisplayButtonColour; }

    private static final MutableLiveData<Integer> brandDisplayButtonTextColour = new MutableLiveData<>();
    public LiveData<Integer> getBrandDisplayButtonTextColour() { return brandDisplayButtonTextColour; }

    private static final MutableLiveData<Boolean> autoLaunchInProgress = new MutableLiveData<>();
    public LiveData<Boolean> getAutoLaunchInProgress() { return autoLaunchInProgress; }

    public void reloadBrandingData() {
        File imgFile = null;
        String basePath = MalFactory.getInstance().getFile().getCommonDir();// "/data/data/" + pth + "/files"
        imgFile = new File(basePath, BrandingConfig.getBrandDisplayLogoHeaderOrDefault());
        if (imgFile.exists()) {
            brandDisplayLogoHeader.setValue(BitmapFactory.decodeFile(imgFile.getAbsolutePath()));
        }
        imgFile = new File(basePath, BrandingConfig.getBrandDisplayLogoIdleOrDefault());
        if (imgFile.exists()) {
            brandDisplayLogoIdle.setValue(BitmapFactory.decodeFile(imgFile.getAbsolutePath()));
        }

        Context context = MalFactory.getInstance().getMalContext();
        brandDisplayStatusBarColour.setValue( getBrandDisplayStatusBarColourOrDefault(context.getColor(R.color.color_linkly_primary)) );
        brandDisplayButtonColour.setValue(getBrandDisplayButtonColourOrDefault(context.getColor(R.color.color_linkly_primary)));
        brandDisplayButtonTextColour.setValue( getBrandDisplayButtonTextColourOrDefault() );
        brandDisplayPrimaryColour.setValue( getBrandDisplayPrimaryColourOrDefault(context.getColor(R.color.color_linkly_primary)) );
    }

    public static void setAutoLaunchInProgress(Boolean value){
        autoLaunchInProgress.setValue(value);
    }

    private static int getColourOrDefault(String colourValue, int defaultColour) {
        int colour = defaultColour;
        try {
            if (!Util.isNullOrEmpty(colourValue)) {
                colour = Color.parseColor("#" + colourValue);
                if (colour == Color.WHITE) {
                    colour = defaultColour;
                }
            }
        } catch (IllegalArgumentException ignored) {
            Timber.e(ignored);
        }
        return colour;
    }

    public static int getBrandDisplayStatusBarColourOrDefault(int defaultColour) {
        return getColourOrDefault(getConfigValue().brandDisplayStatusBarColour, getBrandDisplayPrimaryColourOrDefault(defaultColour));
    }

    public static int getBrandDisplayButtonColourOrDefault(int defaultColour) {
        return getColourOrDefault(getConfigValue().brandDisplayButtonColour, getBrandDisplayPrimaryColourOrDefault(defaultColour));
    }

    public static int getBrandDisplayButtonTextColourOrDefault() {
        return getColourOrDefault(getConfigValue().brandDisplayButtonTextColour,Color.WHITE);
    }

    public static int getBrandDisplayPrimaryColourOrDefault(int defaultColour) {
        return getColourOrDefault(getConfigValue().brandDisplayPrimaryColour, defaultColour);
    }

    private static Parameters getConfigValue() {
        String packageName = MalFactory.getInstance().getMalContext().getPackageName();
        Parameters obj = new Parameters();
        try {
            String paramsFile = "/data/data/" + packageName + "/files/EFT/brandingFiles/brandingParameters.json";
            File f = new File(paramsFile);
            if (f.exists()) {
                String jsonFileContents =  readFileFromStream(new FileInputStream(paramsFile), f.length());
                Gson gson = new Gson();
                obj = gson.fromJson(jsonFileContents, Parameters.class);
                return obj;
            }
        } catch (Exception e) {
            Timber.e("Error processing Branding config file");
        }
        return obj;
    }

    private static String readFileFromStream(InputStream iStream, long len) {
        try {
            byte[] data = new byte[(int) len];
            iStream.read(data);
            iStream.close();
            return new String(data, "UTF-8");
        } catch (Exception e) {
            Timber.w(e);
        }
        return null;
    }

    private static String getStringOrDefault( String str, String defaultString ) {
        return !Util.isNullOrEmpty(str) ?  str : defaultString;
    }

    public static String getBrandDisplayLogoHeaderOrDefault() {
        // Check branding Header Logo exists. If not, return default provided by RES app
        return getStringOrDefault( getConfigValue().brandDisplayLogoHeader, "header.png" );
    }

    public static String getBrandDisplayLogoIdleOrDefault() {
        // Check branding Idle Logo exists. If not, return default provided by RES app
        return getStringOrDefault( getConfigValue().brandDisplayLogoIdle, "screensaver.png" );
    }

    public static String getBrandDisplayLogoSplashOrDefault() {
        // Check branding Splash Logo exists. If not, return default provided by RES app
        return getStringOrDefault( getConfigValue().brandDisplayLogoSplash, "splashlogo.png" );
    }

}
