package com.linkly.libconfig.cpat.Woolworths;

import com.linkly.libmal.global.util.Util;

/**
 * For the WOW system, another record containing ICR processing parameters
 * The raw format is similar to {@link WoolworthsCPATEntry} but here we need to ignore the space delimiter
 **/
public class ProcessingParametersRecord {
    /**
     * 9 Digit long & 0 padded. Eg: 000000103
     * Used to identify and verify the CPAT during PINpad sign-on
     */
    private final String versionNumber;
    /**
     * Number of seconds the PINpad shall wait in its idle state before sending the pending Reversal or next SAF Advice transaction to the Bank
     * Limits transmission pace for SAF transactions
     * 3 digits & in seconds
     */
    private final int idleLoopDelay;
    /**
     * Bank limit for offline authorisation of credit card payments
     * 4 digit long
     */
    private final int offlineCreditLimitDollars;
    /**
     * Bank limit for offline authorisation of debit card payments
     * 4 digit long
     */
    private final int offlineDebitLimitDollars;
    /**
     * Number of {@link WoolworthsCPATEntry} objects included in the CPAT (maximum changed from 99 to 999)
     * 3 digit long
     */
    private final int numberOfCpatEntries;
    /**
     * Maximum transaction allowed in PINPad's SAF memory.
     * Byte previously reserved for SAF warning interval has been reassigned to be the most significant digit of the maximum saf limit to increase the maximum from 99 to 999
     * Maximum entries permitted in PINpad’s SAF (maximum changed from 99 to 999)
     * 3 digit long
     */
    private final int safLimit;

    ProcessingParametersRecord( String rawRecord ) {
        if ( !Util.isNullOrEmpty( rawRecord ) ) {
            rawRecord = rawRecord.replaceAll( " ", "" );

            this.versionNumber = rawRecord.substring( 0, 9 );
            this.idleLoopDelay = Integer.parseInt( rawRecord.substring( 9, 12 ) );
            this.offlineCreditLimitDollars = Integer.parseInt( rawRecord.substring( 12, 16 ) );
            this.offlineDebitLimitDollars = Integer.parseInt( rawRecord.substring( 16, 20 ) );
            this.numberOfCpatEntries = Integer.parseInt( rawRecord.substring( 20, 23 ) );
            this.safLimit = Integer.parseInt( rawRecord.substring( 23, 26 ) );
        } else {
            throw new IllegalArgumentException( "rawRecord is incorrect = [" + rawRecord + "]" );
        }
    }

    public String getVersionNumber() {
        return this.versionNumber;
    }

    public int getIdleLoopDelay() {
        return this.idleLoopDelay;
    }

    public int getOfflineCreditLimitDollars() {
        return this.offlineCreditLimitDollars;
    }

    public int getOfflineDebitLimitDollars() {
        return this.offlineDebitLimitDollars;
    }

    public int getNumberOfCpatEntries() {
        return this.numberOfCpatEntries;
    }

    public int getSafLimit() {
        return this.safLimit;
    }
}
