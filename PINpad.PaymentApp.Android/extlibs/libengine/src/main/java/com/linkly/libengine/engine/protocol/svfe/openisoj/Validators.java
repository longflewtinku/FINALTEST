package com.linkly.libengine.engine.protocol.svfe.openisoj;

import com.linkly.libengine.engine.protocol.svfe.openisoj.fieldvalidator.HexFieldValidator;
import com.linkly.libengine.engine.protocol.svfe.openisoj.fieldvalidator.NumericFieldValidator;

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
