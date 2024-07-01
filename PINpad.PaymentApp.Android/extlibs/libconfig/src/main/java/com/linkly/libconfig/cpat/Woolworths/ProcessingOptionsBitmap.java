package com.linkly.libconfig.cpat.Woolworths;

import com.linkly.libmal.global.util.Util;

import java.util.ArrayList;
import java.util.List;

/**
 * The Processing Options permit specific rules to be applied to a card prefix range.<br>
 * There are 48-bits (6-bytes) for use as options flags per prefix range.<br>
 * The 48-bits represent a bit map, formatted in the same way as the Primary Bit Map.  1 = true, 0 = false. <br>
 * E.g.  Bit-1 set is 8000 0000 0000hex and Bit-48 set is 0000 0000 0001hex.
 */
class ProcessingOptionsBitmap {
    private static final int SMALLEST_VALUE = 1;
    private static final int LARGEST_VALUE = 48;

    enum Bits {
        /**
         * Check ESC on Track 2.
         * If this bit is set, the ESC shall be checked on these swiped cards to see if IC card must be docked.
         * For fuel cards, this option will be turned off, as there is no ESC data on track 2 for fuel cards.
         */
        ESC_BIT( SMALLEST_VALUE ),
        /**
         * Allow Balance Enquiry, Online Mandatory, PIN Mandatory.
         */
        ALLOW_BALANCE( 2 ),
        /**
         * Allow Deposit without PIN or Signature prompting regardless of PSC value.
         */
        ALLOW_DEPOSIT( 3 ),
        /**
         * Allow Cash Advance on Credit Account, Online Mandatory, PIN Mandatory.
         */
        ALLOW_CASH( 4 ),
        /**
         * Paper voucher capture permitted (blocks manual entry of fuel cards if set to false).
         */
        PAPER_VOUCHER( 5 ),
        /**
         * Capture product data (blocks entry of fuel card product data if set to false).
         */
        CAPTURE_PRODUCT_DATA( 6 ),
        /**
         * Split tender permitted
         */
        SPLIT_TENDER_PERMITTED( 7 ),
        /**
         * Loyalty card may be tendered
         */
        LOYALTY_CARD_TENDERED( 8 ),
        /**
         * Fuel discount permitted
         */
        FUEL_DISCOUNT( 9 ),
        /**
         * Card supports Luhn check digit validation
         */
        LUHN_CHECK( 10 ),
        /**
         * Card range acquired externally, not via WOW Switch.  E.g. Amex (Aust) or ENZ acquired.
         */
        EXTERNAL_CARD_RANGE( 11 ),
        /**
         * Allow Refund
         */
        ALLOW_REFUND( 12 ),
        /**
         * Release Transition (Enable AMEX ESC Check and AMEX AID)
         * ‘Small Value’ PIN prompt mandatory
         */
        RELEASE_TRANSACTION( 13 ),

        SMALL_VALUE_PIN_MANDATORY( 14 ),
        /**
         * Reject Products 41-46. eg Bit 15 set, reject product 41.
         */
        REJECT_PRODUCT_41( 15 ),
        REJECT_PRODUCT_42( 16 ),
        REJECT_PRODUCT_43( 17 ),
        REJECT_PRODUCT_44( 18 ),
        REJECT_PRODUCT_45( 19 ),
        REJECT_PRODUCT_46( 20 ),
        /**
         * Reject if card presented via Contactless Reader
         */
        REJECT_CTLS( 21 ),
        /**
         * Reject if card presented via Chip Reader
         */
        REJECT_EMV( 22 ),
        /**
         * ‘Small Value’ Limit – in Dollars up to $255
         */
        SMALL_VALUE_LIMIT_1( 25 ),
        SMALL_VALUE_LIMIT_2( 26 ),
        SMALL_VALUE_LIMIT_3( 27 ),
        SMALL_VALUE_LIMIT_4( 28 ),
        SMALL_VALUE_LIMIT_5( 29 ),
        SMALL_VALUE_LIMIT_6( 30 ),
        SMALL_VALUE_LIMIT_7( 31 ),
        SMALL_VALUE_LIMIT_8( 32 ),
        /**
         * Token lookup advice maximum age (0-6 BCD).  The advice becomes stale after being unsent for the time configured.  Where age = 2n + 12 hours.
         * 0000 = 12 hours
         * 0001 = 14 hours
         * 0110 = 24 hours
         */
        TOKEN_LOOKUP_ADVICE_AGE_1( 33 ),
        TOKEN_LOOKUP_ADVICE_AGE_2( 34 ),
        TOKEN_LOOKUP_ADVICE_AGE_3( 35 ),
        TOKEN_LOOKUP_ADVICE_AGE_4( 36 ),
        /**
         * Stale token lookup advice deletion time (0-7 BCD).  Configurable between midnight and 7AM at 1 hour intervals.  The time that stale token lookup advice purging is to occur.
         * 0000 = 0000 hours
         * 0001 = 0100 hours
         * 0111 = 0700 hours
         */
        STALE_TOKEN_LOOKUP_ADVICE_DELETION_TIME_1( 37 ),
        STALE_TOKEN_LOOKUP_ADVICE_DELETION_TIME_2( 38 ),
        STALE_TOKEN_LOOKUP_ADVICE_DELETION_TIME_3( 39 ),
        STALE_TOKEN_LOOKUP_ADVICE_DELETION_TIME_4( 40 ),
        /**
         * Fuel Card OLT Index (0-9 BCD).  This parameter is only relevant to fuel card BINs and relates the CPAT entry to an OLT Index in the FCAT Offline Limit Table, which in-turn defines the EFB limits for particular product groups purchased with that fuel card.
         */
        FUEL_CARD_OLT_INDEX_1( 41 ),
        FUEL_CARD_OLT_INDEX_2( 42 ),
        FUEL_CARD_OLT_INDEX_3( 43 ),
        FUEL_CARD_OLT_INDEX_4( 44 ),
        /**
         * Application to process the transaction (0-7 BCD).  The Idle app shall report unknown card if the selected app isn’t loaded.
         * 0000		EFTPOS app
         * 0001		Fuel Card module
         * 0010-0111	EFTPOS app
         */
        APPLICATION_TO_PROCESS_1( 45 ),
        APPLICATION_TO_PROCESS_2( 46 ),
        APPLICATION_TO_PROCESS_3( 47 ),
        APPLICATION_TO_PROCESS_4( LARGEST_VALUE ),
        ;

        private final int bit;

        Bits( int bit ) {
            this.bit = bit;
        }

        public int getBit() {
            return this.bit;
        }
    }

    /**
     * Will contain all of the {@link Bits} which are set
     * */
    private final List<Bits> BITS_SET;

    /**
     * Expected length of the raw processing bitmap
     * */
    private static final int LENGTH = 12;

    /**
     * Unpacks the hex string & sets the bits in {@link ProcessingOptionsBitmap#BITS_SET}
     * @param bytes raw String from the cpat file
     * @throws IllegalArgumentException if :
     * 1. bytes is null or empty
     * 2. Bytes is not {@link ProcessingOptionsBitmap#LENGTH} size long
     * */
    ProcessingOptionsBitmap( String bytes ){
        if( !Util.isNullOrEmpty( bytes ) && bytes.length() == LENGTH ) {
            byte[] byteArray = Util.hexToByteArray( bytes );
            List<Bits> bitsList = new ArrayList<>();

            for ( Bits b : Bits.values() ) {
                if ( this.checkIfBitIsSet( byteArray, b.getBit() ) ) {
                    bitsList.add( b );
                }
            }

            this.BITS_SET = bitsList;
        } else {
            throw new IllegalArgumentException( "Raw Bytes from CPAT file are incorrect = [" + bytes + "]" );
        }
    }

    /**
     * Checks if the bits are set in the byteArray
     * Got this code from : https://stackoverflow.com/a/10514651
     * @param byteArray to be checked
     * @param bitToCheck integer value of the bit
     * @return True if set
     * */
    private boolean checkIfBitIsSet( byte[] byteArray, int bitToCheck ){
        if( bitToCheck >= SMALLEST_VALUE && bitToCheck <= LARGEST_VALUE ) {
            int bitIndex = ( bitToCheck - 1 ) % 8;
            int byteIndex = ( bitToCheck - 1 ) / 8;
            int bitMask = 1 << 7 - bitIndex;
            return ( byteArray[byteIndex] & bitMask ) > 0;
        }
        throw new IllegalArgumentException( "Illegal Argument passed. Bit exceeds bounds = [" +
                bitToCheck + "]" );
    }

    /**
     * Will check if the bit was added in the constructor
     * @param bitToCheck {@link Bits} object to check for
     * @return True if set
     * */
    public boolean isEnabled( Bits bitToCheck ){
        return this.BITS_SET.contains( bitToCheck );
    }

}
