package com.linkly.libengine.engine.protocol.svfe.openisoj.exceptions;

@SuppressWarnings("serial")
public class UnknownFieldException extends Exception {
    private String _fieldNumber;

    public UnknownFieldException(String fieldNumber) {
        super("Field " + fieldNumber + " is unknown");
        _fieldNumber = fieldNumber;
    }

    public String getFieldNumber() {
        return _fieldNumber;
    }

    public void setFieldNumber(String fieldNumber) {
        _fieldNumber = fieldNumber;
    }

}
