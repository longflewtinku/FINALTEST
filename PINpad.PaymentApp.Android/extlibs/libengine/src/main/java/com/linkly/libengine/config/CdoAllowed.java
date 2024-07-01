package com.linkly.libengine.config;

public class CdoAllowed {
    private int cardBinNumber;
    private boolean enabled;

    public CdoAllowed(int cardBinNumber, boolean enabled){
        this.cardBinNumber = cardBinNumber;
        this.enabled = enabled;
    }

    public int getCardBinNumber() {
        return this.cardBinNumber;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public void setCardBinNumber(int cardBinNumber) {
        this.cardBinNumber = cardBinNumber;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
