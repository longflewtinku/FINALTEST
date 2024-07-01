package com.linkly.libengine.engine.protocol.svfe.openisoj.fieldvalidator;

public class NoneFieldValidator implements IFieldValidator {

    public String getDescription() {
        return "none";
    }

    public boolean isValid(String value) {
        return true;
    }

}
