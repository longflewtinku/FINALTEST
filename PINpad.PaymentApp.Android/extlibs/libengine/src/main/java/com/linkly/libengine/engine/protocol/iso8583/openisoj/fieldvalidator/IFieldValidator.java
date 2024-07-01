package com.linkly.libengine.engine.protocol.iso8583.openisoj.fieldvalidator;

public interface IFieldValidator {
    public String getDescription();

    public boolean isValid(String value);
}
