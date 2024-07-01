package com.linkly.libconfig.cpat.Woolworths;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class WoolworthsCPATEntryTest {

    @Test
    public void basicUnpackingTest() {
        final String RAW_CPAT_ENTRY = "1800FFFFF 06 0 005008000300 00";
        final WoolworthsCPATEntry woolworthsCPATEntry = new WoolworthsCPATEntry( RAW_CPAT_ENTRY );

        assertNotNull( woolworthsCPATEntry );
        assertEquals( "180000000", woolworthsCPATEntry.getCARD_PREFIX().getCardBinStartValue() );
        assertEquals( "180099999", woolworthsCPATEntry.getCARD_PREFIX().getCardBinEndValue() );
        assertEquals( "06", woolworthsCPATEntry.getCARD_NAME_INDEX().getINDEX() );
        assertEquals( "0", woolworthsCPATEntry.getACCOUNT_GROUPING_CODE().getCODE() );
        assertTrue( woolworthsCPATEntry.getPROCESSING_OPTIONS().isEnabled( ProcessingOptionsBitmap.Bits.LUHN_CHECK ) );
        assertEquals( "00", woolworthsCPATEntry.getPROCESSING_SPEC_CODE().getCode() );
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidFieldLengthTest() {
        final String RAW_CPAT_ENTRY = "1800FFFFF 06 0 0050080003000 00";
        new WoolworthsCPATEntry( RAW_CPAT_ENTRY );
    }

    @Test(expected = IllegalArgumentException.class)
    public void extraFieldsTest() {
        final String RAW_CPAT_ENTRY = "1800FFFFF 06 0 005008000300 00 30";
        new WoolworthsCPATEntry( RAW_CPAT_ENTRY );
    }

    @Test( expected = IllegalArgumentException.class)
    public void missingFieldsTest() {
        final String RAW_CPAT_ENTRY = "1800FFFFF 06 0 005008000300";
        new WoolworthsCPATEntry( RAW_CPAT_ENTRY );
    }
}