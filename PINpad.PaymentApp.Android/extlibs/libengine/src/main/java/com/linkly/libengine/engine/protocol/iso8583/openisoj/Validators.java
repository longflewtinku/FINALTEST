package com.linkly.libengine.engine.protocol.iso8583.openisoj;

import com.linkly.libengine.engine.protocol.iso8583.openisoj.fieldvalidator.HexFieldValidator;
import com.linkly.libengine.engine.protocol.iso8583.openisoj.fieldvalidator.NumericFieldValidator;

public class Validators {
    public static boolean isHex(String value) {
        HexFieldValidator validator = new HexFieldValidator();
        return validator.isValid(value);
    }

    public static boolean isNumeric(String value) {
        NumericFieldValidator validator = new NumericFieldValidator();
        return validator.isValid(value);
    }
}
