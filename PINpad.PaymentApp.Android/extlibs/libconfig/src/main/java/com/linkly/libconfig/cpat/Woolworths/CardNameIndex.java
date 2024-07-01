package com.linkly.libconfig.cpat.Woolworths;

import com.linkly.libmal.global.util.Util;

/**
 * Contains the index of the appropriate card name table entry that has the correct card name text for the receipt and the Totals Bin for the POS.
 * */
// MW: Not going to write tests for this class.
class CardNameIndex {
    private final String INDEX;
    private static final int LENGTH = 2;

    CardNameIndex( String cardNameIndex ) {
        if ( !Util.isNullOrEmpty( cardNameIndex ) &&
                cardNameIndex.length() == LENGTH &&
                Util.isNumericString( cardNameIndex ) ) {
            this.INDEX = cardNameIndex;
        } else {
            throw new IllegalArgumentException( "Invalid Card name Index passed = [" + cardNameIndex + "]" );
        }
    }

    public String getINDEX() {
        return this.INDEX;
    }
}
