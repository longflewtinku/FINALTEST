package com.linkly.payment.openisojtests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.linkly.libengine.engine.protocol.svfe.openisoj.FullSensitiser;
import com.linkly.libengine.engine.protocol.svfe.openisoj.Sensitiser;

import org.junit.Test;

public class FullSensitiserTests {
    private Sensitiser sensitiser = FullSensitiser.getInstance();

    @Test
    public void testNull() {
        assertNull(sensitiser.sensitise(null));
    }

    @Test
    public void testValid() {
        String input = "123456";
        String expected = "******";
        String actual = sensitiser.sensitise(input);
        assertEquals(expected, actual);
    }
}
