package com.linkly.libconfig.cpat.Woolworths;

import com.linkly.libmal.global.util.Util;

/**
 * Processing Specification Code defines the card capture and PIN entry options that apply to the card
 * The PSC is a two digit value that tells the PINpad whether the card can be swiped or keyed and
 * whether PIN or Signature can be used for cardholder verification.
 */
enum ProcessingSpecificationCode {
    // Extracted the following values from CPAT Editor and not the document
    // Note: Document said Swipe, however the document is outdated. It applies to everything
    // Therefore Swipe = Any card present type
    REJECT_TRANS( "00" ),
    SWIPE_PIN_MANDATORY( "10" ),
    SWIPE_PIN_OPTIONAL( "11" ),
    SWIPE_PIN_SIGN_MANDATORY( "12" ),
    SWIPE_SIGN_ONLY( "13" ),
    SWIPE_KEYED_PIN_MANDATORY( "20" ),
    SWIPE_KEYED_PIN_OPTIONAL( "21" ),
    SWIPE_KEYED_PIN_SIGN_MANDATORY( "22" ),
    SWIPE_KEYED_SIGN_ONLY( "23" ),
    JACK_BAUER( "24" ),
    SWIPE_APPROVE_ONLINE_PIN_MANDATORY( "30" ),
    SWIPE_APPROVE_ONLINE_PIN_OPTIONAL( "31" ),
    SWIPE_APPROVE_ONLINE_PIN_SIGN_MANDATORY( "32" ),
    SWIPE_APPROVE_ONLINE_SIGN_ONLY( "33" ),
    SWIPE_KEYED_APPROVE_ONLINE_PIN_MANDATORY( "40" ),
    SWIPE_KEYED_APPROVE_ONLINE_PIN_OPTIONAL( "41" ),
    SWIPE_KEYED_APPROVE_ONLINE_PIN_SIGN_MANDATORY( "42" ),
    SWIPE_KEYED_APPROVE_ONLINE_SIGN_ONLY( "43" ),
    ;

    private final String code;

    ProcessingSpecificationCode( String code ) {
        this.code = code;
    }

    public static ProcessingSpecificationCode getPsc( String code ) {
        if( !Util.isNullOrEmpty( code ) && Util.isNumericString( code ) && code.length() == 2 ) {
            for ( ProcessingSpecificationCode p : ProcessingSpecificationCode.values() ) {
                if ( p.code.equals( code ) )
                    return p;
            }
        }

        throw new IllegalArgumentException( "Unknown Processing code = [" + code + "]" );
    }

    public String getCode() {
        return this.code;
    }
}
