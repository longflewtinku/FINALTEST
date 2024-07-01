package com.linkly.libengine.engine.protocol.iso8583.openisoj.fieldvalidator;

public class BcdFieldValidator implements IFieldValidator {

    public String getDescription() {
        return "bcd";
    }

    public boolean isValid(String value) {
        // match digits 0-9, a-f, and A-F
        return value.matches("^[0-9|a-f|A-F|=]+$");
    }

}
