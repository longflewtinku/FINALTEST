package com.linkly.libconfig.cpat.Woolworths;

import com.linkly.libmal.global.util.Util;

/**
 * The card prefixes are nine digit values, of which can contain ‘wild’ digits signified by the hexadecimal digit ‘F’. <br>
 * A ‘wild’ digit can substitute any value. <br>
 * The first entry in the CPAT must be the lowest prefix, e.g ‘30FFFFFFF’.  <br>
 * This value matches any card with a prefix of ‘300000000’ to ‘309999999’. <br>
 * The next entry in the CPAT must be the next lowest prefix, e.g.’34FFFFFFF’, and so on.
 */
class CardPrefix {
    /**
     * Current length of a CPAT Card string. No more or less
     * */
    private static final int CPAT_CARD_PREFIX_LENGTH = 9;

    /**
     * Wildcard character, if present can be substituted from 0 to 9
     * */
    private static final String WILDCARD = "F";
    /**
     * This will contain the lowest bin range possible.
     * As per the example above: it will be '300000000'
     */
    private final String cardBinStartValue;

    /**
     * This will contain the lowest bin range possible.
     * As per the example above: it will be '309999999'
     */
    private final String cardBinEndValue;

    /**
     *
     * */
    public CardPrefix( String cpatCardPrefix ){
        if( this.checkIfValid( cpatCardPrefix ) ){
            this.cardBinStartValue = cpatCardPrefix.replace( WILDCARD, "0" );
            this.cardBinEndValue = cpatCardPrefix.replace( WILDCARD, "9" );
        } else {
            throw new IllegalArgumentException(
                    "CPAT Card Prefix provided had invalid args = [" + cpatCardPrefix + "]" );
        }
    }

    /**
     * Performs multiple checks on the string.
     * */
    private boolean checkIfValid( String cpatCardPrefix ){
        if( !Util.isNullOrEmpty( cpatCardPrefix ) && cpatCardPrefix.length() == CPAT_CARD_PREFIX_LENGTH ){
            // If string contains wildcard 'F' character, it should be followed by only F to be valid
            if( cpatCardPrefix.contains( WILDCARD ) ) {
                String wildcard = cpatCardPrefix.substring( cpatCardPrefix.indexOf( WILDCARD ) );
                wildcard = wildcard.replace( WILDCARD, "" );

                return wildcard.isEmpty();
            }

            // If it doesn't have wildcard char, it should be only numbers
            return Util.isNumericString( cpatCardPrefix );
        }
        return false;
    }

    public String getCardBinStartValue() {
        return this.cardBinStartValue;
    }

    public String getCardBinEndValue() {
        return this.cardBinEndValue;
    }
}
