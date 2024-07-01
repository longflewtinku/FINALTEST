package com.linkly.payment.openisojtests;

import static org.junit.Assert.assertEquals;

import com.linkly.libengine.engine.protocol.svfe.openisoj.SignedAmount;

import org.junit.Test;

public class SignedAmountTests {
    @Test
    public void testPackNegative() {
        SignedAmount amount = new SignedAmount(-17384);
        assertEquals(-17384, amount.getValue());
        assertEquals("D", amount.getSign());
        assertEquals("D0017384", amount.getPacked(7));
        assertEquals("D0000017384", amount.getPacked(10));
    }

    @Test
    public void testPackPostitive() {
        SignedAmount amount = new SignedAmount(17384);
        assertEquals(17384, amount.getValue());
        assertEquals("C", amount.getSign());
        assertEquals("C0017384", amount.getPacked(7));
        assertEquals("C0000017384", amount.getPacked(10));
    }

    @Test
    public void testUnpackNegative() {
        SignedAmount amount = new SignedAmount("D0017384");
        assertEquals(-17384, amount.getValue());
        assertEquals("D", amount.getSign());
        assertEquals("D0017384", amount.getPacked(7));
        assertEquals("D0000017384", amount.getPacked(10));
    }

    @Test
    public void testUnpackPositive() {
        SignedAmount amount = new SignedAmount("C0000017384");
        assertEquals(17384, amount.getValue());
        assertEquals("C", amount.getSign());
        assertEquals("C0017384", amount.getPacked(7));
        assertEquals("C0000017384", amount.getPacked(10));
    }
}
