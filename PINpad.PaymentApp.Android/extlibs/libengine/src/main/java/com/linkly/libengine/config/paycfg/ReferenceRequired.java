package com.linkly.libengine.config.paycfg;

public enum ReferenceRequired {OPTIONAL(0), MANDATORY(1), DISABLED(2);
    public final int referenceCode;
    ReferenceRequired(int referenceCode) {
        this.referenceCode = referenceCode;
    }
    public int getReferenceCode(){
        return this.referenceCode;
    }
}