package com.positive.openisojtests;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import com.linkly.libengine.engine.protocol.svfe.openisoj.FieldDescriptor;
import com.linkly.libengine.engine.protocol.svfe.openisoj.IFieldDescriptor;
import com.linkly.libengine.engine.protocol.svfe.openisoj.exceptions.FieldDescriptorException;
import com.linkly.libengine.engine.protocol.svfe.openisoj.fieldvalidator.FieldValidators;
import com.linkly.libengine.engine.protocol.svfe.openisoj.fieldvalidator.NoneFieldValidator;
import com.linkly.libengine.engine.protocol.svfe.openisoj.formatter.BinaryFormatter;
import com.linkly.libengine.engine.protocol.svfe.openisoj.formatter.Formatters;
import com.linkly.libengine.engine.protocol.svfe.openisoj.lengthformatters.FixedLengthFormatter;
import com.linkly.libengine.engine.protocol.svfe.openisoj.lengthformatters.VariableLengthFormatter;

import org.junit.Test;

public class FieldDescriptorTests {
    @Test
    public void binaryFieldMustHaveHexValidator() throws Exception {
        FixedLengthFormatter fixedLengthFormatter = new FixedLengthFormatter(8);
        NoneFieldValidator noneFieldValidator = new NoneFieldValidator();
        BinaryFormatter binaryFormatter = new BinaryFormatter();

        try {
            new FieldDescriptor(fixedLengthFormatter, noneFieldValidator, binaryFormatter, null,
                    null);
            fail("Binary formatter must have hex validator");
        } catch (FieldDescriptorException e) {
        }
    }

    @Test
    public void testDisplayFixed() throws Exception {
        IFieldDescriptor fd = FieldDescriptor.getAsciiFixed(10, FieldValidators.getN());
        String result = fd.display("   ", 7, "0321164153");
        String expected = "   007(010)[0321164153]";
        assertEquals(expected, result);
    }

    @Test
    public void testDisplayFixedAns() throws Exception {
        IFieldDescriptor fd = FieldDescriptor.getAsciiFixed(8, FieldValidators.getAns());
        String result = fd.display("   ", 41, "20202020");
        String expected = "   041(008)[20202020]";
        assertEquals(expected, result);
    }

    @Test
    public void testDisplayLLVarN() throws Exception {
        IFieldDescriptor fd = FieldDescriptor.getAsciiVar(2, 11, FieldValidators.getN());
        String result = fd.display("", 100, "333333");
        String expected = "100(006)[333333]";
        assertEquals(expected, result);
    }

    @Test
    public void testBinaryVar() throws Exception {
        FieldDescriptor fd = new FieldDescriptor(new VariableLengthFormatter(1, 9, Formatters.getAscii()),
                FieldValidators.getHex(), Formatters.getBinary(), null, null);

        byte[] actual = fd.pack(2, "1234");
        byte[] expected = {0x32, 0x12, 0x34};
        assertArrayEquals(expected, actual);
    }

    @Test
    public void testBinaryVarOddLength() throws Exception {
        FieldDescriptor fd = new FieldDescriptor(new VariableLengthFormatter(1, 9, Formatters.getAscii()),
                FieldValidators.getHex(), Formatters.getBinary(), null, null);

        byte[] actual = fd.pack(2, "91234");
        byte[] expected = {0x33, 0x09, 0x12, 0x34};
        assertArrayEquals(expected, actual);
    }
}
