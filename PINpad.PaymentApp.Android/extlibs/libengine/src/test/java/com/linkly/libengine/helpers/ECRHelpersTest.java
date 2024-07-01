package com.linkly.libengine.helpers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.linkly.libengine.config.BinRangesCfg;
import com.linkly.libengine.config.PayCfg;

import junit.framework.TestCase;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class ECRHelpersTest extends TestCase {

    @Mock
    PayCfg payCfg = mock(PayCfg.class);

    public void setUp() throws Exception {
        super.setUp();
    }

    public void testFormatTrack2ForChip() {
        String inputTrack2 = "53676320****4578D25092019480601089201F";
        assertEquals("5367630000004578=2509????????????????", ECRHelpers.getMaskedTrackData(inputTrack2));
    }

    // 4284180200002100D????????????????????F
    public void testInvalidTrack2NoExpiry() {
        String inputTrack2 = "428418******2100D????????????????????F";
        assertEquals("4284180000002100=????????????????????", ECRHelpers.getMaskedTrackData(inputTrack2));
    }

    public void testFormatTrack2ForMsr() {
        String inputTrack2 = "37424500***1003=241220115041234500000";
        assertEquals("374245000001003=2412?????????????????", ECRHelpers.getMaskedTrackData(inputTrack2));
    }
    public void testFormatTrack2ForQueryResponseWith19DigitPan() {
        String inputTrack2 = "56027950*******8811D2705201160254447784F";
        assertEquals("5602790000000008811=2705???????????????", ECRHelpers.getMaskedTrackData(inputTrack2));
    }

    public void testFormatTrack2ForQueryResponseWithNoExpiry() {
        String inputTrack2 = "56027950*****8118D2F";
        assertEquals("56027900000008118=2", ECRHelpers.getMaskedTrackData(inputTrack2));
    }

    public void testFormatTrack2ForQueryResponseWithNoExpiryAndDiscretionaryData() {
        String inputTrack2 = "56027950*****8118";
        assertEquals("56027900000008118", ECRHelpers.getMaskedTrackData(inputTrack2));
    }
    public void testFormatTrack2ForQueryResponseWithInvalidPan() {
        String inputTrack2 = "5602D2705201160254447784F";
        assertEquals("5602=2705???????????????", ECRHelpers.getMaskedTrackData(inputTrack2));
    }

    public void testFormatTrack2ForQueryResponseWithWrongData() {
        String inputTrack2 = "56027950*****8118D2705201160254447784F";
        assertEquals("56027900000008118=2705???????????????", ECRHelpers.getMaskedTrackData(inputTrack2));
    }

    @Mock
    BinRangesCfg binRangesCfg;
    public void testMaskIfLoyaltyCard() {
        MockitoAnnotations.openMocks(this);
        // return -1 here means card not found in financial BIN table
        when(binRangesCfg.getCardsCfgIndex(any(), any(String.class))).thenReturn(-1);
        String inputTrack2 = "7001230123456789D2705001293810928312F";
        assertEquals("7001230123456789=2705001293810928312", ECRHelpers.maskIfFinancial(payCfg, inputTrack2, binRangesCfg));
    }

    public void testMaskIfUnmaskedFinancialCard() {
        MockitoAnnotations.openMocks(this);
        // return 2 here means card is in financial BINs table
        when(binRangesCfg.getCardsCfgIndex(any(), any(String.class))).thenReturn(2);
        String inputTrack2 = "4001230123456789D2705001293810928312F";
        assertEquals("4001230000006789=2705???????????????", ECRHelpers.maskIfFinancial(payCfg, inputTrack2, binRangesCfg));
    }

    public void testMaskIfMaskedFinancialCard() {
        MockitoAnnotations.openMocks(this);
        // return 2 here means card is in financial BINs table
        when(binRangesCfg.getCardsCfgIndex(any(), any(String.class))).thenReturn(2);
        String inputTrack2 = "40012308****6789D2705???????????????F";
        // output should have first 6, last 4 unmasked
        assertEquals("4001230000006789=2705???????????????", ECRHelpers.maskIfFinancial(payCfg, inputTrack2, binRangesCfg));
    }

    public void testMaskNotFinancialButAlreadyMasked() {
        MockitoAnnotations.openMocks(this);
        // return -1 to indicate it's not in the BIN table
        when(binRangesCfg.getCardsCfgIndex(any(), any(String.class))).thenReturn(-1);
        String inputTrack2 = "40012308****6789D2705???????????????F";
        // output should have first 6, last 4 unmasked
        assertEquals("4001230000006789=2705???????????????", ECRHelpers.maskIfFinancial(payCfg, inputTrack2, binRangesCfg));
    }

    public void testFinancialCardNoExpiry() {
        MockitoAnnotations.openMocks(this);
        // return -1 to indicate it's not in the BIN table
        when(binRangesCfg.getCardsCfgIndex(any(), any(String.class))).thenReturn(2);
        String inputTrack2 = "40012308****6789==12345=123456789F";
        // output should have first 6, last 4 unmasked
        assertEquals("4001230000006789==?????=?????????", ECRHelpers.maskIfFinancial(payCfg, inputTrack2, binRangesCfg));
    }

    public void testLoyaltyCardExtraDelimiters() {
        MockitoAnnotations.openMocks(this);
        // return -1 to indicate it's not in the BIN table
        when(binRangesCfg.getCardsCfgIndex(any(), any(String.class))).thenReturn(-1);
        String inputTrack2 = "4001230881926789=D1234D02913829123";
        // output should be same as input
        assertEquals("4001230881926789==1234=02913829123", ECRHelpers.maskIfFinancial(payCfg, inputTrack2, binRangesCfg));
    }

}