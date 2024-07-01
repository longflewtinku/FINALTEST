package com.linkly.libengine.engine.protocol.iso8583.openisoj.fieldvalidator;

public class HexFieldValidator implements IFieldValidator {

    public String getDescription() {
        return "hex";
    }

    public boolean isValid(String value) {
        // match digits 0-9, a-f, and A-F, between zero (no input) to infinite times
        return value.matches("^[0-9|a-f|A-F]*$");
    }

}
