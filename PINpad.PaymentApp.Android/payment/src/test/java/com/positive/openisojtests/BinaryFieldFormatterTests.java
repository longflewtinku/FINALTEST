package com.linkly.payment.openisojtests;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import com.linkly.libengine.engine.protocol.svfe.openisoj.formatter.BinaryFormatter;

import org.junit.Test;

public class BinaryFieldFormatterTests {
    @Test
    public void getBytes() throws Exception {
        BinaryFormatter target = new BinaryFormatter();
        String value = "31323334";
        byte[] expected = new byte[] {0x31, 0x32, 0x33, 0x34};
        byte[] actual = target.getBytes(value);
        assertArrayEquals(expected, actual);
    }

    @Test
    public void getString() {
        BinaryFormatter target = new BinaryFormatter();
        String expected = "31323334";
        byte[] input = new byte[] {0x31, 0x32, 0x33, 0x34};
        String actual = target.getString(input);
        assertEquals(expected, actual);
    }

    @Test
    public void packedLength() throws Exception {
        BinaryFormatter formatter = new BinaryFormatter();
        int actual = formatter.getPackedLength(8);
        assertEquals(4, actual);
    }
}
