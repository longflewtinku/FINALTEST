package com.linkly.payment.openisojtests;

import static org.junit.Assert.assertEquals;

import com.linkly.libengine.engine.protocol.svfe.openisoj.IsoConvert;

import org.junit.Test;

public class IsoConvertTests {
    @Test
    public void fromIntToMsgType() {
        String res = IsoConvert.fromIntToMsgType(0x200);
        assertEquals("0200", res);
    }

    @Test
    public void fromMsgTypeToInt() {
        int res = IsoConvert.fromMsgTypeToInt("0200");
        assertEquals(0x200, res);
    }
}
