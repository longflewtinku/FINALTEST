package com.linkly.libengine.engine.protocol.iso8583.openisoj.fieldvalidator;

public class NoneFieldValidator implements IFieldValidator {

    public String getDescription() {
        return "none";
    }

    public boolean isValid(String value) {
        return true;
    }

}
