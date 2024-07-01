package com.linkly.payment.openisojtests;

import static org.junit.Assert.assertEquals;

import com.linkly.libengine.engine.protocol.svfe.openisoj.Iso8583Rev93;
import com.linkly.libengine.engine.protocol.svfe.openisoj.Iso8583Rev93.PosDataCode;

import org.junit.Test;

public class Rev93PosDataTests {
    @Test
    public void testConstruct() {
        String expected = "810101211148101";
        PosDataCode pdc = new Iso8583Rev93.PosDataCode();
        pdc.setCardDataInputCapability(PosDataCode.CardDataInputCapability._8_MAGSTRIPE_ICC);
        pdc.setCardholderAuthCapability(PosDataCode.CardholderAuthCapability._1_PIN);
        pdc.setCardCaptureCapability(PosDataCode.CardCaptureCapability._0_NONE);
        pdc.setOperatingEnvironment(PosDataCode.OperatingEnvironment._1_ATTENDED_ON_ACCEPTOR_PREMISES);
        pdc.setCardholderPresent(PosDataCode.CardholderPresent._0_PRESENT);
        pdc.setCardPresent(PosDataCode.CardPresent._1_PRESENT);
        pdc.setCardDataInputMode(PosDataCode.CardDataInputMode._2_MAGSTRIPE);
        pdc.setCardholderAuthMethod(PosDataCode.CardholderAuthMethod._1_PIN);
        pdc.setCardholderAuthEntity(PosDataCode.CardholderAuthEntity._1_ICC);
        pdc.setCardDataOutputCapability(PosDataCode.CardDataOutputCapability._1_NONE);
        pdc.setTerminalOutputCapability(PosDataCode.TerminalOutputCapability._4_PRINTING_AND_DISPLAY);
        pdc.setPinCaptureCapability(PosDataCode.PinCaptureCapability._8_EIGHT);
        pdc.setTerminalOperator(PosDataCode.TerminalOperator._1_CARD_ACCEPTOR_OPERATED);
        pdc.setTerminalType(PosDataCode.TerminalType._01_POS);

        assertEquals(expected, pdc.toString());
    }

    @Test
    public void testUnpack() {
        String msg = "810101211148101";
        PosDataCode pdc = new PosDataCode(msg);
        assertEquals(PosDataCode.CardDataInputCapability._8_MAGSTRIPE_ICC, pdc.getCardDataInputCapability());
        assertEquals(PosDataCode.CardholderAuthCapability._1_PIN, pdc.getCardholderAuthCapability());
        assertEquals(PosDataCode.CardCaptureCapability._0_NONE, pdc.getCardCaptureCapability());
        assertEquals(PosDataCode.OperatingEnvironment._1_ATTENDED_ON_ACCEPTOR_PREMISES, pdc.getOperatingEnvironment());
        assertEquals(PosDataCode.CardholderPresent._0_PRESENT, pdc.getCardholderPresent());
        assertEquals(PosDataCode.CardPresent._1_PRESENT, pdc.getCardPresent());
        assertEquals(PosDataCode.CardDataInputMode._2_MAGSTRIPE, pdc.getCardDataInputMode());
        assertEquals(PosDataCode.CardholderAuthMethod._1_PIN, pdc.getCardholderAuthMethod());
        assertEquals(PosDataCode.CardholderAuthEntity._1_ICC, pdc.getCardholderAuthEntity());
        assertEquals(PosDataCode.CardDataOutputCapability._1_NONE, pdc.getCardDataOutputCapability());
        assertEquals(PosDataCode.TerminalOutputCapability._4_PRINTING_AND_DISPLAY, pdc.getTerminalOutputCapability());
        assertEquals(PosDataCode.PinCaptureCapability._8_EIGHT, pdc.getPinCaptureCapability());
        assertEquals(PosDataCode.TerminalOperator._1_CARD_ACCEPTOR_OPERATED, pdc.getTerminalOperator());
        assertEquals(PosDataCode.TerminalType._01_POS, pdc.getTerminalType());
    }
}
