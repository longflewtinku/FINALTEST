package com.positive.openisojtests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.linkly.libengine.engine.protocol.svfe.openisoj.PanSensitiser;
import com.linkly.libengine.engine.protocol.svfe.openisoj.Sensitiser;

import org.junit.Test;

public class PanSensitiserTests {
    private Sensitiser sensitiser = PanSensitiser.getInstance();

    @Test
    public void testLen10() {
        assertEquals("123456**1234", sensitiser.sensitise("123456001234"));
    }

    @Test
    public void testLen16() {
        assertEquals("123456******1234", sensitiser.sensitise("1234560000001234"));
    }

    @Test
    public void testLen19() {
        assertEquals("123456*********1234", sensitiser.sensitise("1234569876543211234"));
    }

    @Test
    public void testNull() {
        assertNull(sensitiser.sensitise(null));
    }
}
