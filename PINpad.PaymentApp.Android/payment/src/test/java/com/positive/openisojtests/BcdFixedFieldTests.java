package com.positive.openisojtests;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import com.linkly.libengine.engine.protocol.svfe.openisoj.Field;
import com.linkly.libengine.engine.protocol.svfe.openisoj.FieldDescriptor;
import com.linkly.libengine.engine.protocol.svfe.openisoj.exceptions.FieldDescriptorException;
import com.linkly.libengine.engine.protocol.svfe.openisoj.fieldvalidator.AlphaNumericSpecialFieldValidator;
import com.linkly.libengine.engine.protocol.svfe.openisoj.formatter.BcdFormatter;
import com.linkly.libengine.engine.protocol.svfe.openisoj.lengthformatters.FixedLengthFormatter;

import org.junit.Test;

public class BcdFixedFieldTests {
    @Test
    public void bcdHasNumericValidator() throws Exception {
        FixedLengthFormatter fixedLengthFormatter = new FixedLengthFormatter(8);
        AlphaNumericSpecialFieldValidator alphaNumericSpecialFieldValidator = new AlphaNumericSpecialFieldValidator();
        BcdFormatter bcdFormatter = new BcdFormatter();

        try {
            new FieldDescriptor(fixedLengthFormatter, alphaNumericSpecialFieldValidator, bcdFormatter, null, null);
            fail("Expected FieldDescriptorException");
        } catch (FieldDescriptorException e) {
        }
    }

    @Test
    public void packedLength() throws Exception {
        Field field = new Field(2, FieldDescriptor.getBcdFixed(4));
        field.setValue("12345678");
        int actual = field.getPackedLength();
        assertEquals(4, actual);
    }

    @Test
    public void packOddNumberOfDigits() throws Exception {
        Field field = new Field(2, FieldDescriptor.getBcdFixed(2));
        field.setValue("123");
        byte[] actual = field.toMsg();
        byte[] expected = new byte[] {0x01, 0x23};
        assertArrayEquals(expected, actual);
    }

    @Test
    public void packTest() throws Exception {
        Field field = new Field(2, FieldDescriptor.getBcdFixed(2));
        field.setValue("0012");
        byte[] actual = field.toMsg();
        byte[] expected = new byte[] {0x00, 0x12};
        assertArrayEquals(expected, actual);
    }

    @Test
    public void unpackTest() throws Exception {
        Field field = new Field(2, FieldDescriptor.getBcdFixed(2));
        byte[] msg = new byte[] {0x00, 0x12};
        field.unpack(msg, 0);
        String actual = field.getValue();
        String expected = "0012";
        assertEquals(expected, actual);
    }
}
