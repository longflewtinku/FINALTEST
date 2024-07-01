package com.linkly.libconfig.uiconfig;

import androidx.annotation.Keep;

/**
 * Contains a list of Configurable Timeouts
 * */
@Keep
public enum ConfigTimeouts {
    /**
     * Amount Entry Screen Timeout
     * Is only applicable in Standalone mode operation
     */
    AMOUNT_ENTRY_TIMEOUT(300),
    /**
     * Card Present Timeout.
     * Same enum represents Access Mode & normal mode
     */
    PRESENT_CARD_TIMEOUT(300,480),
    /**
     * Account Selection Timeout
     * Will be applicable for account selection screens
     */
    ACCOUNT_SELECTION_TIMEOUT(300,480),
    /**
     * App Selection Timeout
     * Will be applicable for application selection screens
     */
    APP_SELECTION_TIMEOUT(300,480),
    /**
     * Card PIN Entry Timeout
     * Represents access mode & normal mode both
     */
    CARD_PIN_ENTRY_TIMEOUT(300,480),
    /**
     * Confirm Signature Timeout
     * Used during the period after merchant receipt is printed and we are waiting for confirmation of signature from merchant
     */
    CONFIRM_SIGNATURE_TIMEOUT(300),
     /**
     * Remove Card Timeout (Secs)
     * */
    REMOVE_CARD_TIMEOUT(300),
    /**
     * Print Customer Receipt Timeout milliSecs
     * Only applicable if Terminal is printing receipts + also printing customer receipts
     */
    CUSTOMER_PRINT_RECEIPT_TIMEOUT(300),
    /**
     * Remove Merchant Receipt Timeout milliSecs
     * Used during the period after merchant receipt is printed and we are waiting for confirmation of remove merchant copy prompt
     */
    REMOVE_RECEIPT_TIMEOUT(300),
    /**
     * Paper Out Timeout milliSecs
     * Used during period while printing paper is out and we are waiting for inserting new roll
     */
    PAPER_OUT_TIMEOUT(300),
    /**
     * Decision Screen Timeout in milliSecs
     * Used in one of the Approved, Declined or Cancelled screens
     */
    DECISION_SCREEN_TIMEOUT(5, 10),
    ;

    private int maximumSecs;
    private int accessModeMaximumSecs;

    ConfigTimeouts(int max) {
        this.maximumSecs = max;
    }

    ConfigTimeouts(int maximum, int accessModeMaximum) {
        this.maximumSecs = maximum;
        this.accessModeMaximumSecs = accessModeMaximum;
    }

    public int getMaximumSecs() {
        return maximumSecs;
    }

    public int getAccessModeMaximumSecs() {
        return accessModeMaximumSecs;
    }
}
