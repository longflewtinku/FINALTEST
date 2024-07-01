package com.linkly.libengine.engine.transactions.properties;


import android.content.Context;

import com.linkly.libengine.R;
import com.linkly.libengine.engine.Engine;
import com.linkly.libui.UI;

import java.util.Objects;

public class LedStatus {
    int ledOff = android.R.color.transparent;
    int ledOn = R.color.color_linkly_primary;
    int defaultColor = ledOff;
    int resIdLed1 = defaultColor;
    int resIdLed2 = ledOff;
    int resIdLed3 = ledOff;
    int resIdLed4 = ledOff;
    boolean visible = false;


    public LedStatus() {

        if (true /* || Ctls_Pax.useUkLights*/) {
            defaultColor = ledOn;
        }
        resIdLed1 = defaultColor;
    }

    public void reset() {
        resIdLed1 = defaultColor;
        resIdLed2 = ledOff;
        resIdLed3 = ledOff;
        resIdLed4 = ledOff;
        visible = false;

    }

    public void setCTLSEnabled(boolean isEnabled) {
        resIdLed1 = isEnabled ? defaultColor : ledOff;
        resIdLed2 = ledOff;
        resIdLed3 = ledOff;
        resIdLed4 = ledOff;

        if (isEnabled) {
            visible = true;
        } else {
            visible = false;
        }



    }

    public void updateLeds(Context context) {

        if (UI.getInstance().getUI().getLedOne())
            resIdLed1 = Engine.getDep().getPayCfg().getBrandDisplayPrimaryColourOrDefault(Objects.requireNonNull(context).getColor(ledOn));
        else
            resIdLed1 = ledOff;

        if (UI.getInstance().getUI().getLedTwo())
            resIdLed2 = Engine.getDep().getPayCfg().getBrandDisplayPrimaryColourOrDefault(Objects.requireNonNull(context).getColor(ledOn));
        else
            resIdLed2 = ledOff;

        if (UI.getInstance().getUI().getLedThree())
            resIdLed3 = Engine.getDep().getPayCfg().getBrandDisplayPrimaryColourOrDefault(Objects.requireNonNull(context).getColor(ledOn));
        else
            resIdLed3 = ledOff;

        if (UI.getInstance().getUI().getLedFour())
            resIdLed4 = Engine.getDep().getPayCfg().getBrandDisplayPrimaryColourOrDefault(Objects.requireNonNull(context).getColor(ledOn));
        else
            resIdLed4 = ledOff;
    }

    public int getLedOff() {
        return this.ledOff;
    }

    public int getLedOn() {
        return this.ledOn;
    }

    public int getDefaultColor() {
        return this.defaultColor;
    }

    public int getResIdLed1() {
        return this.resIdLed1;
    }

    public int getResIdLed2() {
        return this.resIdLed2;
    }

    public int getResIdLed3() {
        return this.resIdLed3;
    }

    public int getResIdLed4() {
        return this.resIdLed4;
    }

    public boolean isVisible() {
        return this.visible;
    }

    public void setLedOff(int ledOff) {
        this.ledOff = ledOff;
    }

    public void setLedOn(int ledOn) {
        this.ledOn = ledOn;
    }

    public void setDefaultColor(int defaultColor) {
        this.defaultColor = defaultColor;
    }

    public void setResIdLed1(int resIdLed1) {
        this.resIdLed1 = resIdLed1;
    }

    public void setResIdLed2(int resIdLed2) {
        this.resIdLed2 = resIdLed2;
    }

    public void setResIdLed3(int resIdLed3) {
        this.resIdLed3 = resIdLed3;
    }

    public void setResIdLed4(int resIdLed4) {
        this.resIdLed4 = resIdLed4;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }
}
