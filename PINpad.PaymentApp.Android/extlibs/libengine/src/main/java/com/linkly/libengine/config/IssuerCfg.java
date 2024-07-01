package com.linkly.libengine.config;

public class IssuerCfg {
    private String issuerName;
    private boolean enabled;
    // if true, and we're in transit/flight mode of operation, then perform transaction as deferred auth instead of advice when uploading offline approved txns
    private boolean deferredAuthEnabled;

    public IssuerCfg( String issuerName, boolean enabled, boolean deferredAuthEnabled ) {
        this.issuerName = issuerName;
        this.enabled = enabled;
        this.deferredAuthEnabled = deferredAuthEnabled;
    }

    public String getIssuerName() {
        return this.issuerName;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public boolean isDeferredAuthEnabled() {
        return this.deferredAuthEnabled;
    }

    public void setIssuerName(String issuerName) {
        this.issuerName = issuerName;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setDeferredAuthEnabled(boolean deferredAuthEnabled) {
        this.deferredAuthEnabled = deferredAuthEnabled;
    }
}
