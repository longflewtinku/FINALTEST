package com.linkly.libengine.engine.protocol.svfe.openisoj.fieldvalidator;

public interface IFieldValidator {
    public String getDescription();

    public boolean isValid(String value);
}
