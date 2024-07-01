package com.linkly.libconfig.cpat.Woolworths;

/**
 * Account Grouping Code
 * Controls the account type selection for the card
 */
enum AccountGroupingCode {
    /**
     * Do not prompt for Account Selection
     * Set DE 3 to account type CREDIT (30)
     * Only to be used for Credit Amounts
     */
    DEFAULT( "0" ),
    /**
     * Account Selection is required
     * Cheque or Savings only
     */
    DEBIT( "1" ),
    /**
     * Do not prompt for account selection
     * Set DE 3 to account type CREDIT (30)
     */
    CREDIT( "2" ),
    /**
     * All account types: CRD, CHQ, SAV
     * Account selection is required
     */
    ALL( "3" ),
    ;

    private final String CODE;

    AccountGroupingCode( String code ) {
        this.CODE = code;
    }

    public static AccountGroupingCode getAgc( String code ) {
        for ( AccountGroupingCode agc :
                AccountGroupingCode.values() ) {
            if ( agc.CODE.equals( code ) ) {
                return agc;
            }
        }
        return DEFAULT;
    }

    public String getCODE() {
        return this.CODE;
    }
}
