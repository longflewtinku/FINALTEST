package com.linkly.libconfig.cpat.Woolworths;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class CardPrefixTest {

    @Test
    public void basicTest() {
        final String CPAT_CARD_PREFIX = "30FFFFFFF";
        final CardPrefix cardPrefix = new CardPrefix( CPAT_CARD_PREFIX );

        assertNotNull( cardPrefix );
        assertEquals( "300000000", cardPrefix.getCardBinStartValue() );
        assertEquals( "309999999", cardPrefix.getCardBinEndValue() );
    }

    @Test
    public void lastIndexRangeTest() {
        final String CPAT_CARD_PREFIX = "50444444F";
        final CardPrefix cardPrefix = new CardPrefix( CPAT_CARD_PREFIX );

        assertNotNull( cardPrefix );
        assertEquals( "504444440", cardPrefix.getCardBinStartValue() );
        assertEquals( "504444449", cardPrefix.getCardBinEndValue() );
    }

    @Test
    public void fullCardBinTest() {
        final String CPAT_CARD_PREFIX = "300000000";
        final CardPrefix cardPrefix = new CardPrefix( CPAT_CARD_PREFIX );

        assertNotNull( cardPrefix );
        assertEquals( CPAT_CARD_PREFIX, cardPrefix.getCardBinStartValue() );
        assertEquals( CPAT_CARD_PREFIX, cardPrefix.getCardBinEndValue() );
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidWildcardPosition() {
        new CardPrefix( "30000F000" );
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidChars() {
        new CardPrefix( "30000000G" );
    }

    @Test(expected = IllegalArgumentException.class)
    public void emptyString() {
        new CardPrefix( "" );
    }

    @Test( expected = IllegalArgumentException.class)
    public void invalidLength() {
        new CardPrefix( "30000000G30000000G" );
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidCharAfterWildCard() {
        new CardPrefix( "30FFFFFFA" );
    }
}