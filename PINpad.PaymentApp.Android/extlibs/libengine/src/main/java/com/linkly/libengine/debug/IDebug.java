package com.linkly.libengine.debug;

import com.linkly.libpositive.wrappers.TagDataToPOS;
import com.pax.dal.entity.TrackData;

public interface IDebug {
    enum DEBUG_POSITION {
        ENTER_CARD( "ENTER CARD" ),
        SELECT_ACCOUNT( "SELECT ACCOUNT" ),
        ENTER_PIN( "ENTER PIN" ),
        CASHBACK( "CASHBACK" ),
        AUTH( "AUTH" ),
        MOTO( "MOTO" ),
        REFERENCE( "REFERENCE" ),
        // Question is raised about the following except Expiry
        CPCRATEACCEPT( "CPCRATECONFIRM" ),
        EXPIRY( "EXPIRY" ),
        VERIFICATION( "VERIFICATION" ),
        INDICATOR( "INDICATOR" ),
        SELECT_APPLICATION( "SELECT APPLICATION")
        ;
        String position;

        DEBUG_POSITION( String position ) {
            this.position = position;
        }

        public String getPosition() {
            return this.position;
        }
    }

    enum DEBUG_ACCOUNT {
        SAVINGS( "SAV" ),
        CHEQUE( "CHQ" ),
        CREDIT( "CRD" );

        String accountType;

        DEBUG_ACCOUNT( String accountType ) {
            this.accountType = accountType;
        }

        public String getAccountType() {
            return this.accountType;
        }
    }

    enum DEBUG_COMMS_FALLBACK{
        FALLBACK_TO_SECONDARY("FALLBACK_TO_SECONDARY"),
        FALLBACK_RECOVERED("FALLBACK_RECOVERED_TO_PRIMARY");

        String option;

        DEBUG_COMMS_FALLBACK(String option){ this.option = option;}

        public String getOption() {
            return this.option;
        }
    }

    enum DEBUG_KEY {
        YES( "YES" ),
        NO( "NO" ),
        ;

        String keyPressed;

        DEBUG_KEY( String keyPressed ) {
            this.keyPressed = keyPressed;
        }

        public String getKeyPressed() {
            return this.keyPressed;
        }
    }

    enum DEBUG_EVENT {
        ACCOUNT_SELECTED( "PCE_ACCOUNT_SELECTED" ),
        CANCEL_PRESSED( "PCE_CANCEL_PRESSED" ),
        OPERATOR_TIMEOUT( "PCE_OPERATOR_TIMEOUT" ),
        SIGNATURE_KEY( "PCE_SIGNATURE_KEY" ),
        KEY_PRESS( "PCE_KEYPRESS" ),
        // We need to add the CEM due to consistency issues with our spec. Usually we have <TAG>=<Value> but as this is an array of values we prefix data with with ":"
        // If not we get weird PCE_CARD_DATA=:CEM=... malformed data.
        CARD_DATA( "PCE_CARD_DATA:CEM" ),
        REVERSAL_STARTED( "PCE_REVERSAL_STARTED" ),
        TMS_STARTED( "PCE_TMS_STARTED" ),
        LOGON_STARTED( "PCE_LOGON_STARTED" ),
        ADVICE_UPLOAD( "PCE_ADVICE_UPLOAD" ),
        DEFERRED_AUTH_UPLOAD( "PCE_DEFERRED_AUTH_UPLOAD" ),
        BACK_TO_IDLE( "PCE_BACK_TO_IDLE" ),
        CONFIG_FAILURE( "ERROR_CONFIG_FAILURE" ),
        PCI_24HOUR_REBOOT( "PCE_PCI_24HOURS_REBOOT" ),
        COMMS_FALLBACK( "COMMS_FALLBACK" ),
        NETWORK_DIAGNOSTIC( "NETWORK_DIAGNOSTIC" ),
        ;
        private String command;

        DEBUG_EVENT( String command ) {
            this.command = command;
        }

        public String getCommand() {
            return this.command;
        }
    }

    void reportDebugAccountSelect( DEBUG_ACCOUNT accountType );

    void reportCancelSelect( DEBUG_POSITION screenType );

    void reportTimeout( DEBUG_POSITION screenType );

    void reportDebugEvent(DEBUG_EVENT debugEvent, String eventData);

    void reportSignatureKeyPressed( DEBUG_KEY keyPressed );

    void reportYesNoKeyPressed( DEBUG_KEY keyPressed );

    void reportCommsFallbackEvent( DEBUG_COMMS_FALLBACK option );

    void reportNetworkDiagnosticEvent( String text );

    /*
    * Report masked Card data to Payment Interface
    * @param mode = Card present Type
    * @param trackData = Class containing all 3 tracks
    * */
    void reportCardData( TagDataToPOS.CardEntryModeTag mode, TrackData trackData );

    /*
    * Report masked track2 data as it is the only one available
    * Useful for Contactless & Chip cards
    * @param mode = Card present type passed onto reportCardData
    * @param track2 = Used to build up a TrackData object to be passed
    * */
    void reportCardData( TagDataToPOS.CardEntryModeTag mode, String track2 );
}
