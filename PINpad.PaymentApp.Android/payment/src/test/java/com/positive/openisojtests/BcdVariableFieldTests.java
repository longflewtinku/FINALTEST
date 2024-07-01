package com.positive.openisojtests;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import com.linkly.libengine.engine.protocol.iso8583.openisoj.Field;
import com.linkly.libengine.engine.protocol.iso8583.openisoj.FieldDescriptor;

import org.junit.Test;

public class BcdVariableFieldTests {
    @Test
    public void pack() throws Exception {
        Field f = new Field(2, FieldDescriptor.getBcdVar(2, 15));
        f.setValue("7777");
        byte[] actual = f.toMsg();
        byte[] expected = {0x04, 0x77, 0x77};
        assertArrayEquals(expected, actual);
    }

    @Test
    public void packOdd() throws Exception {
        Field f = new Field(13, FieldDescriptor.getBcdVar(2, 15));
        f.setValue("777");
        byte[] actual = f.toMsg();
        byte[] expected = {0x03, 0x77, 0x7f};
        assertArrayEquals(expected, actual);
    }

    @Test
    public void packToLengthLimit() throws Exception {
        Field f = new Field(13, FieldDescriptor.getBcdVar(2, 7));
        f.setValue("1234567");
        byte[] actual = f.toMsg();
        byte[] expected = {0x07, 0x12, 0x34, 0x56, 0x7f};
        assertArrayEquals(expected, actual);
    }

    @Test
    public void unpack() throws Exception {
        Field f = new Field(2, FieldDescriptor.getBcdVar(2, 15));

        byte[] msg = new byte[2];
        msg[0] = 0x02;
        msg[1] = 0x77;
        f.unpack(msg, 0, true);
        String actual = f.getValue();
        String expected = "77";
        assertEquals(expected, actual);
    }
}
