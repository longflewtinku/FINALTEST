package com.linkly.payment.openisojtests;

import static org.junit.Assert.assertEquals;

import com.linkly.libengine.engine.protocol.svfe.openisoj.OriginalDataElements;

import org.junit.Test;

public class OriginalDataElementsTests {
    @Test
    public void testUnpack() {
        String input = "1200654123022116282598765432100";
        OriginalDataElements ode = new OriginalDataElements(input);
        assertEquals("1200", ode.getMsgType());
        assertEquals("654123", ode.getStan());
        assertEquals("0221162825", ode.getDatetime());
        assertEquals("98765432100", ode.getAcqInst());
    }
}
