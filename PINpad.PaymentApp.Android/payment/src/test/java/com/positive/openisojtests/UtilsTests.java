package com.linkly.payment.openisojtests;

import static org.junit.Assert.assertEquals;

import com.linkly.libengine.engine.protocol.svfe.openisoj.IsoUtils;

import org.junit.Test;

public class UtilsTests {
    @Test
    public void maskPan() {
        String pan = "1234567890123456";
        String expected = "123456******3456";
        String actual = IsoUtils.maskPan(pan);
        assertEquals(expected, actual);
    }

    @Test
    public void maskPanShort() {
        String shortPan = "1234567890";
        String actualShort = IsoUtils.maskPan(shortPan);
        assertEquals(shortPan, actualShort);
    }

    @Test
    public void msgTypeToInt() {
        assertEquals(0x200, IsoUtils.msgTypeToInt("0200"));
        assertEquals(0x1200, IsoUtils.msgTypeToInt("1200"));
        assertEquals(0x1804, IsoUtils.msgTypeToInt("1804"));
        assertEquals(0x9430, IsoUtils.msgTypeToInt("9430"));
    }

    @Test
    public void msgTypeToString() {
        assertEquals("0200", IsoUtils.msgTypeToString(0x200));
        assertEquals("1200", IsoUtils.msgTypeToString(0x1200));
        assertEquals("1804", IsoUtils.msgTypeToString(0x1804));
        assertEquals("9430", IsoUtils.msgTypeToString(0x9430));
    }

    @Test
    public void testGetRequestMsgType() {
        assertEquals(0x200, IsoUtils.getRequestMsgType(0x200));
        assertEquals(0x200, IsoUtils.getRequestMsgType(0x210));
        assertEquals(0x420, IsoUtils.getRequestMsgType(0x420));
        assertEquals(0x420, IsoUtils.getRequestMsgType(0x430));
    }
}
