package com.linkly.libconfig.cpat.Woolworths;

/**
 * This file will contain a single entry in the WW CPAT file. <br>
 * The format of a single entry in a text file is: <br>
 * XXXXXXXXX XX X XXXXXXXXXXXX XX <br>
 * Where:
 * <ul>
 *     <li>Card Prefix: 9 chars</li>
 *     <li>Card Name Index: 2 chars</li>
 *     <li>Account Grouping Code: 1 char</li>
 *     <li>Processing Options Bitmap: 6 bytes/12 chars</li>
 *     <li>Processing Specification Code: 2 chars</li>
 * </ul>
 */
class WoolworthsCPATEntry {
    private static final int MAX_FIELDS = 5;
    private final CardPrefix CARD_PREFIX;
    private final CardNameIndex CARD_NAME_INDEX;
    private final AccountGroupingCode ACCOUNT_GROUPING_CODE;
    private final ProcessingOptionsBitmap PROCESSING_OPTIONS;
    private final ProcessingSpecificationCode PROCESSING_SPEC_CODE;


    public WoolworthsCPATEntry( String entry ){
        String[] fields = entry.split( " " );
        if( fields.length != MAX_FIELDS ){
            throw new IllegalArgumentException( "Unexpected Number of fields = [" + entry + "]" );
        }

        this.CARD_PREFIX = new CardPrefix( fields[0] );
        this.CARD_NAME_INDEX = new CardNameIndex( fields[1] );
        this.ACCOUNT_GROUPING_CODE = AccountGroupingCode.getAgc( fields[2] );
        this.PROCESSING_OPTIONS = new ProcessingOptionsBitmap( fields[3] );
        this.PROCESSING_SPEC_CODE = ProcessingSpecificationCode.getPsc( fields[4] );
    }

    public CardPrefix getCARD_PREFIX() {
        return this.CARD_PREFIX;
    }

    public CardNameIndex getCARD_NAME_INDEX() {
        return this.CARD_NAME_INDEX;
    }

    public AccountGroupingCode getACCOUNT_GROUPING_CODE() {
        return this.ACCOUNT_GROUPING_CODE;
    }

    public ProcessingOptionsBitmap getPROCESSING_OPTIONS() {
        return this.PROCESSING_OPTIONS;
    }

    public ProcessingSpecificationCode getPROCESSING_SPEC_CODE() {
        return this.PROCESSING_SPEC_CODE;
    }
}
