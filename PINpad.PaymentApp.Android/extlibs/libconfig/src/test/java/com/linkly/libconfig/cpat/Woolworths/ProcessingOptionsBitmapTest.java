package com.linkly.libconfig.cpat.Woolworths;

import static com.linkly.libconfig.cpat.Woolworths.ProcessingOptionsBitmap.Bits.ALLOW_BALANCE;
import static com.linkly.libconfig.cpat.Woolworths.ProcessingOptionsBitmap.Bits.ALLOW_CASH;
import static com.linkly.libconfig.cpat.Woolworths.ProcessingOptionsBitmap.Bits.ALLOW_DEPOSIT;
import static com.linkly.libconfig.cpat.Woolworths.ProcessingOptionsBitmap.Bits.ALLOW_REFUND;
import static com.linkly.libconfig.cpat.Woolworths.ProcessingOptionsBitmap.Bits.CAPTURE_PRODUCT_DATA;
import static com.linkly.libconfig.cpat.Woolworths.ProcessingOptionsBitmap.Bits.ESC_BIT;
import static com.linkly.libconfig.cpat.Woolworths.ProcessingOptionsBitmap.Bits.EXTERNAL_CARD_RANGE;
import static com.linkly.libconfig.cpat.Woolworths.ProcessingOptionsBitmap.Bits.FUEL_DISCOUNT;
import static com.linkly.libconfig.cpat.Woolworths.ProcessingOptionsBitmap.Bits.LOYALTY_CARD_TENDERED;
import static com.linkly.libconfig.cpat.Woolworths.ProcessingOptionsBitmap.Bits.LUHN_CHECK;
import static com.linkly.libconfig.cpat.Woolworths.ProcessingOptionsBitmap.Bits.PAPER_VOUCHER;
import static com.linkly.libconfig.cpat.Woolworths.ProcessingOptionsBitmap.Bits.REJECT_CTLS;
import static com.linkly.libconfig.cpat.Woolworths.ProcessingOptionsBitmap.Bits.REJECT_EMV;
import static com.linkly.libconfig.cpat.Woolworths.ProcessingOptionsBitmap.Bits.SMALL_VALUE_LIMIT_1;
import static com.linkly.libconfig.cpat.Woolworths.ProcessingOptionsBitmap.Bits.SMALL_VALUE_LIMIT_2;
import static com.linkly.libconfig.cpat.Woolworths.ProcessingOptionsBitmap.Bits.SMALL_VALUE_LIMIT_3;
import static com.linkly.libconfig.cpat.Woolworths.ProcessingOptionsBitmap.Bits.SMALL_VALUE_LIMIT_4;
import static com.linkly.libconfig.cpat.Woolworths.ProcessingOptionsBitmap.Bits.SMALL_VALUE_LIMIT_5;
import static com.linkly.libconfig.cpat.Woolworths.ProcessingOptionsBitmap.Bits.SMALL_VALUE_LIMIT_6;
import static com.linkly.libconfig.cpat.Woolworths.ProcessingOptionsBitmap.Bits.SMALL_VALUE_LIMIT_7;
import static com.linkly.libconfig.cpat.Woolworths.ProcessingOptionsBitmap.Bits.SMALL_VALUE_LIMIT_8;
import static com.linkly.libconfig.cpat.Woolworths.ProcessingOptionsBitmap.Bits.SMALL_VALUE_PIN_MANDATORY;
import static com.linkly.libconfig.cpat.Woolworths.ProcessingOptionsBitmap.Bits.SPLIT_TENDER_PERMITTED;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ProcessingOptionsBitmapTest {
    @Test
    public void basicUnpackLuhnCheck() {
        final String CPAT_BITMAP = "005008000300";
        final ProcessingOptionsBitmap processingOptionsBitmap = new ProcessingOptionsBitmap( CPAT_BITMAP );

        assertNotNull( processingOptionsBitmap );
        assertTrue( processingOptionsBitmap.isEnabled( LUHN_CHECK ) );
    }

    @Test
    public void basicDisabledESCCheck() {
        final String CPAT_BITMAP = "005008000300";
        final ProcessingOptionsBitmap processingOptionsBitmap = new ProcessingOptionsBitmap( CPAT_BITMAP );

        assertNotNull( processingOptionsBitmap );
        assertFalse( processingOptionsBitmap.isEnabled( ESC_BIT ) );

    }

    @Test
    public void fullRecordCheck() {
        final String CPAT_BITMAP = "805000230300";
        final ProcessingOptionsBitmap processingOptionsBitmap = new ProcessingOptionsBitmap( CPAT_BITMAP );

        assertNotNull( processingOptionsBitmap );
        // All following values from CPAT Editor
        assertTrue( processingOptionsBitmap.isEnabled( LUHN_CHECK ) );
        assertTrue( processingOptionsBitmap.isEnabled( ESC_BIT ) );
        assertFalse( processingOptionsBitmap.isEnabled( ALLOW_BALANCE ) );
        assertFalse( processingOptionsBitmap.isEnabled( ALLOW_DEPOSIT ) );
        assertFalse( processingOptionsBitmap.isEnabled( ALLOW_CASH ) );
        assertTrue( processingOptionsBitmap.isEnabled( ALLOW_REFUND ) );
        assertFalse( processingOptionsBitmap.isEnabled( SMALL_VALUE_PIN_MANDATORY ) );
        assertFalse( processingOptionsBitmap.isEnabled( REJECT_CTLS ) );
        assertFalse( processingOptionsBitmap.isEnabled( REJECT_EMV ) );
        assertFalse( processingOptionsBitmap.isEnabled( EXTERNAL_CARD_RANGE ) );
        assertFalse( processingOptionsBitmap.isEnabled( PAPER_VOUCHER ) );
        assertFalse( processingOptionsBitmap.isEnabled( CAPTURE_PRODUCT_DATA ) );
        assertFalse( processingOptionsBitmap.isEnabled( SPLIT_TENDER_PERMITTED ) );
        assertFalse( processingOptionsBitmap.isEnabled( LOYALTY_CARD_TENDERED ) );
        assertFalse( processingOptionsBitmap.isEnabled( FUEL_DISCOUNT ) );
    }

    @Test
    public void allFlagsSet() {
        final String CPAT_BITMAP = "FFFFFFFFFFFF";
        final ProcessingOptionsBitmap processingOptionsBitmap = new ProcessingOptionsBitmap( CPAT_BITMAP );

        assertTrue( processingOptionsBitmap.isEnabled( LUHN_CHECK ) );
        assertTrue( processingOptionsBitmap.isEnabled( ESC_BIT ) );
        assertTrue( processingOptionsBitmap.isEnabled( ALLOW_BALANCE ) );
        assertTrue( processingOptionsBitmap.isEnabled( ALLOW_DEPOSIT ) );
        assertTrue( processingOptionsBitmap.isEnabled( ALLOW_CASH ) );
        assertTrue( processingOptionsBitmap.isEnabled( ALLOW_REFUND ) );
        assertTrue( processingOptionsBitmap.isEnabled( SMALL_VALUE_PIN_MANDATORY ) );
        assertTrue( processingOptionsBitmap.isEnabled( REJECT_CTLS ) );
        assertTrue( processingOptionsBitmap.isEnabled( REJECT_EMV ) );
        assertTrue( processingOptionsBitmap.isEnabled( EXTERNAL_CARD_RANGE ) );
        assertTrue( processingOptionsBitmap.isEnabled( PAPER_VOUCHER ) );
        assertTrue( processingOptionsBitmap.isEnabled( CAPTURE_PRODUCT_DATA ) );
        assertTrue( processingOptionsBitmap.isEnabled( SPLIT_TENDER_PERMITTED ) );
        assertTrue( processingOptionsBitmap.isEnabled( LOYALTY_CARD_TENDERED ) );
        assertTrue( processingOptionsBitmap.isEnabled( FUEL_DISCOUNT ) );
    }


    @Test
    public void allFlagsNotSet() {
        final String CPAT_BITMAP = "000000000000";
        final ProcessingOptionsBitmap processingOptionsBitmap = new ProcessingOptionsBitmap( CPAT_BITMAP );

        assertFalse( processingOptionsBitmap.isEnabled( LUHN_CHECK ) );
        assertFalse( processingOptionsBitmap.isEnabled( ESC_BIT ) );
        assertFalse( processingOptionsBitmap.isEnabled( ALLOW_BALANCE ) );
        assertFalse( processingOptionsBitmap.isEnabled( ALLOW_DEPOSIT ) );
        assertFalse( processingOptionsBitmap.isEnabled( ALLOW_CASH ) );
        assertFalse( processingOptionsBitmap.isEnabled( ALLOW_REFUND ) );
        assertFalse( processingOptionsBitmap.isEnabled( SMALL_VALUE_PIN_MANDATORY ) );
        assertFalse( processingOptionsBitmap.isEnabled( REJECT_CTLS ) );
        assertFalse( processingOptionsBitmap.isEnabled( REJECT_EMV ) );
        assertFalse( processingOptionsBitmap.isEnabled( EXTERNAL_CARD_RANGE ) );
        assertFalse( processingOptionsBitmap.isEnabled( PAPER_VOUCHER ) );
        assertFalse( processingOptionsBitmap.isEnabled( CAPTURE_PRODUCT_DATA ) );
        assertFalse( processingOptionsBitmap.isEnabled( SPLIT_TENDER_PERMITTED ) );
        assertFalse( processingOptionsBitmap.isEnabled( LOYALTY_CARD_TENDERED ) );
        assertFalse( processingOptionsBitmap.isEnabled( FUEL_DISCOUNT ) );
    }

    @Test( expected = IllegalArgumentException.class )
    public void incorrectTextSize() {
        new ProcessingOptionsBitmap( "80500023030" );
    }

    @Test
    public void extractSmallValueLimitExtraction( ){
        final String CPAT_BITMAP = "805000230300";
        final ProcessingOptionsBitmap processingOptionsBitmap = new ProcessingOptionsBitmap( CPAT_BITMAP );

        assertFalse( processingOptionsBitmap.isEnabled( SMALL_VALUE_LIMIT_1 ) );
        assertFalse( processingOptionsBitmap.isEnabled( SMALL_VALUE_LIMIT_2 ) );
        assertTrue( processingOptionsBitmap.isEnabled( SMALL_VALUE_LIMIT_3 ) );
        assertFalse( processingOptionsBitmap.isEnabled( SMALL_VALUE_LIMIT_4 ) );
        assertFalse( processingOptionsBitmap.isEnabled( SMALL_VALUE_LIMIT_5 ) );
        assertFalse( processingOptionsBitmap.isEnabled( SMALL_VALUE_LIMIT_6 ) );
        assertTrue( processingOptionsBitmap.isEnabled( SMALL_VALUE_LIMIT_7 ) );
        assertTrue( processingOptionsBitmap.isEnabled( SMALL_VALUE_LIMIT_8 ) );
    }
}