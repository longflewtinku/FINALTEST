package com.linkly.libconfig;

import android.content.res.Configuration;

import java.io.Serializable;

@SuppressWarnings("serial")
public class OrientationString implements Serializable {
    public String portrait;
    public String landscape;

    public String get(int orientation) {
        if (Configuration.ORIENTATION_LANDSCAPE == orientation) {
            return landscape;
        }
        return portrait;
    }

    public String getPortrait() {
        return this.portrait;
    }

    public String getLandscape() {
        return this.landscape;
    }

    public void setPortrait(String portrait) {
        this.portrait = portrait;
    }

    public void setLandscape(String landscape) {
        this.landscape = landscape;
    }
}
