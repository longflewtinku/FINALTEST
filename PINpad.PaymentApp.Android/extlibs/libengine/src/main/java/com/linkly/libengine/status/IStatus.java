package com.linkly.libengine.status;

public interface IStatus {

    STATUS_EVENT getLastStatus();

    void reportStatusEvent(STATUS_EVENT event , boolean suppressPosDialog);

    /**
     * status request with free-format text override. doesn't need to match one of the enums below
     *
     * @param event - 'base' event - used to determine the key mask and graphics code to display to user
     * @param freeFormText - text to display to POS user. Can be multi-line, separated by newline \n char
     */
    void reportStatusEvent(STATUS_EVENT event, String freeFormText , boolean suppressPosDialog);

    String convertToFreeForm(String line1, String line2);

    enum STATUS_EVENT {
        NOT_SET("N/A"),

        STATUS_TRANS_STARTED("Transaction started"),

        STATUS_TRANS_APPROVED("Transaction Approved"),
        STATUS_TRANS_DECLINED("Transaction Declined"),
        STATUS_TRANS_MSR("Card type = MSR"),
        STATUS_TRANS_MSR_DECLINED("MSR Transaction Declined"),
        STATUS_TRANS_EMV("Card type = EMV"),
        STATUS_TRANS_CTLS("Card type = CTLS"),
        STATUS_TRANS_CTLS_DECLINED("CTLS Transaction Declined"),
        STATUS_TRANS_MANUAL("Card type = manual"),
        STATUS_TRANS_CANCELLED("Transaction Cancelled"),
        STATUS_TRANS_REFERRED("Transaction Referred"),
        STATUS_TRANS_FINISHED("Transaction Finished"),

        STATUS_SETTLEMENT_STARTED("Settlement Started"),

        STATUS_UI_GETCARD("GetCard_ALL Screen Displayed"),
        STATUS_UI_GETCARD_MAN("GetCard_MAN; Screen Displayed"), // use ';' as end of pattern
        STATUS_UI_GETCARD_ICC("GetCard_ICC; Screen Displayed"),
        STATUS_UI_GETCARD_CTLS("GetCard_CTLS; Screen Displayed"),
        STATUS_UI_GETCARD_MSR("GetCard_MSR; Screen Displayed"),
        STATUS_UI_GETCARD_ICC_MAN("GetCard_ICC_MAN; Screen Displayed"),
        STATUS_UI_GETCARD_ICC_CTLS("GetCard_ICC_CTLS; Screen Displayed"),
        STATUS_UI_GETCARD_ICC_MSR("GetCard_ICC_MSR; Screen Displayed"),
        STATUS_UI_GETCARD_CTLS_MAN("GetCard_CTLS_MAN; Screen Displayed"),
        STATUS_UI_GETCARD_CTLS_MSR("GetCard_CTLS_MSR; Screen Displayed"),
        STATUS_UI_GETCARD_MSR_MAN("GetCard_MSR_MAN; Screen Displayed"),
        STATUS_UI_GETCARD_MSR_ICC_MAN("GetCard_MSR_ICC_MAN; Screen Displayed"),
        STATUS_UI_GETCARD_MSR_ICC_CTLS("GetCard_MSR_ICC_CTLS; Screen Displayed"),
        STATUS_UI_GETCARD_MSR_MAN_CTLS("GetCard_MSR_MAN_CTLS; Screen Displayed"),
        STATUS_UI_GETCARD_MAN_CTLS_ICC("GetCard_MAN_CTLS_ICC; Screen Displayed"),

        STATUS_UI_MANUAL_PAN_ACTIVITY("Manual Pan Screen Displayed"),
        STATUS_UI_OFFLINE_PIN_REQUESTED("Pin Requested(Offline)"),
        STATUS_UI_ONLINE_PIN_REQUESTED("Pin Requested(Online)"),
        STATUS_UI_ACCOUNT_SELECTION( "Account Selection Displayed"),
        STATUS_UI_ENTER_TIP( "Enter Tip Displayed"),

        STATUS_HOST_APPROVED("Host Approved"),
        STATUS_HOST_DEFERRED_AUTH("Deferred Auth"),
        STATUS_REVERSAL_APPROVED("Reversal Approved"),
        STATUS_REVERSAL_DECLINED("Reversal Declined"),
        STATUS_ERR_TRANSACTION_DECLINED("Transaction Declined"),
        STATUS_ERR_USER_CANCELLED("Card User Cancelled"),
        STATUS_ERR_GENAC2_FAILED("GENAC2 Failed"),

        STATUS_ERR_PRINTER_GENERAL_ERROR("Printer General Error"),
        STATUS_ERR_PRINTER_OUT_OF_PAPER("Printer Out Of Paper"),
        STATUS_ERR_BATTERY_LOW("Battery Too Low"),
        STATUS_ERR_AMOUNT_HIGH("Amount High"),
        STATUS_ERR_AMOUNT_LOW("Amount Low"),
        STATUS_ERR_CARD_BLOCKED("Card Blocked"),
        STATUS_ERR_CARD_EXPIRED("Card Expired"),
        STATUS_ERR_CARD_TYPE_NOT_ALLOWED("Card Type Not Allowed"),
        STATUS_ERR_CARD_NUMBER_INVALID("Invalid Card Number"),
        STATUS_ERR_PIN_INVALID_RETRY("Pin Invalid Retry"),
        STATUS_ERR_PIN_INVALID_LAST_TRY("Pin Invalid Last Try"),

        STATUS_ERR_CASHBACK_TOO_HIGH("Cashback Too High"),   //   !! Needs Params
        STATUS_ERR_PIN_CVM_REQUIRED("Pin Cvm Required"),  // Not Sure Where this goes
        STATUS_ERR_SIGNATURE_CVM_REQUIRED("Signature Cvm Required"),  // DOnt think we support Sig
        STATUS_ERR_LOCALLY_DECLINED("Locally Declined"),
        STATUS_ERR_HOST_DECLINED("Host Declined"),  //
        STATUS_ERR_ISSUER_DECLINED("Issuer Declined"), // Not sure how to dif Host v Issuer
        STATUS_ERR_ISSUER_UNAVAILABLE("Issuer Unavailable"),//
        STATUS_ERR_UPDATE_IN_PROGRESS("Update In Progress Error"),
        STATUS_ERR_UPDATE_REQUIRED("Update Required Error"),
        STATUS_ERR_REVERSAL_NOT_POSSIBLE("Reversal Not Possible Error"), //
        STATUS_ERR_TRANSACTION_TYPE_NOT_ALLOWED("Transaction Type Not Allowed"), //
        STATUS_ERR_LOGON_FAILED("Login Failed"),  //
        STATUS_ERR_CHIP_UNREADABLE("Chip Unreadable"), //
        STATUS_ERR_CHIP_APP_UNSUPPORTED_PLEASE_SWIPE("Chip App Unsupported Please Swipe"),  // Need to find Correct Fallback point
        STATUS_ERR_CHIP_RID_UNSUPPORTED_PLEASE_SWIPE("Chip Rid Unsupported Please Swipe"),
        STATUS_ERR_CHIP_INVALID_PLEASE_SWIPE("Chip Invalid Please Swipe"),
        STATUS_ERR_CHIP_NOT_ALLOWED_PLEASE_SWIPE("Chip not allowed Please swipe"),      // aS Above
        STATUS_ERR_CHIP_DETECTED_PLEASE_INSERT("Chip detected Please Insert"),
        STATUS_ERR_CHIP_DETECTED_PLEASE_INSERT_OR_FORCE_FALLBACK("Chip detected Please Insert OR Force Fallback"),
        STATUS_ERR_INSERT_OR_SWIPE_CARD("Insert Or Swipe Card"),
        STATUS_ERR_MAGSTRIPE_UNREADABLE("Magnetic Strip Unreadable"),
        STATUS_ERR_MAGSTRIPE_INVALID("Magnetic Stripe Invalid"),
        STATUS_ERR_MAGSTRIPE_NOT_ALLOWED("Magnetic Stripe Not Allowed"),
        STATUS_ERR_MANUAL_INPUT_INVALID("Manual Input Invalid"),
        STATUS_ERR_MANUAL_INPUT_INVALID_LENGTH("Manual Input Invalid Length"), //
        STATUS_ERR_MANUAL_INPUT_INVALID_DATE("Manual Input Invalid Date"),   //
        STATUS_ERR_CASHBACK_ONLY_ALLOWED_ONLINE("Cashback Only Allowed Online"),
        STATUS_ERR_TRANSACTION_ONLY_ALLOWED_ONLINE("Transaction Only Allowed Online"),
        STATUS_ERR_APPROVAL_CODE_INVALID("Approval Code Invalid"),
        STATUS_ERR_PASSWORD_INVALID("Password Invalid"),  //
        STATUS_ERR_CLOSE_BATCH_REQUIRED("Close Batch Required"),  //Not sure if this relates to Batch upload or Rec
        STATUS_ERR_CLOSE_BATCH_NOT_REQUIRED("Close Batch Not Required"),
        STATUS_ERR_TECHNICAL_ERROR("Technical Error"),
        STATUS_ERR_HARDWARE_ERROR("Hardware Error"),
        STATUS_ERR_RECONCILIATION_TERMINAL_ALREADY_SETTLED("Settlement Error - Terminal Already Settled"),
        STATUS_ERR_RECONCILIATION_OUTSIDE_WINDOW("Settlement Error - Outside Settlement Window"),
        STATUS_ERR_RECONCILIATION_OUT_OF_BALANCE("Settlement Error - Totals Mismatch"),
        STATUS_LOGON_STARTED("Logon Started"),
        STATUS_SIGNATURE_OKAY("Signature Okay"),
        STATUS_OPERATOR_TIMEOUT("Operator Timeout"),
        STATUS_SUB_TOTALS_STARTED("Subtotals Started"),
        STATUS_SHIFT_REPORT_STARTED("Shift Report Started"),
        STATUS_ENTER_PASSWORD("Enter Password"),
        STATUS_PROCESSING("Card type="),
        STATUS_PROCESSING_CTLS("CTLS Processing"),
        STATUS_PROCESSING_ICC("ICC Processing"),
        STATUS_PROCESSING_MSR("MSR Processing"),
        STATUS_REMOVE_CARD("Remove Card"),
        STATUS_ERR_COMPLETION_ERROR_PREAUTH_REVERSED("Completion Error - Pre-auth was reversed"), //
        STATUS_ERR_COMPLETION_AMT_EXCEEDS_PREAUTH_AMT("Completion Error - amount exceeds Pre-auth amount"), //
        STATUS_ERR_PREAUTH_NOT_FOUND("Error - Pre-auth not found"), //
        STATUS_ERR_RFN_NOT_ENTERED("Error - rfn not entered/n PAD Data not Found"),
        STATUS_ERR_PREAUTH_EXISTS("Error - Pre-auth with this card details already exists"), //
        STATUS_CONFIRM_SURCHARGE("Confirm Surcharge"),
        STATUS_ERR_ACCOUNT_LOCKED("Account Locked"),
        STATUS_SIGNATURE("Signature"), // signature check pos dialog with only cancel option
        STATUS_NO_RESPONSE_PLEASE_TRY_AGAIN("No Response Please Try Again"),
        STATUS_CUSTOMER_RECEIPT("Customer Receipt"),
        STATUS_ERR_KEY_INJECTION_REQUIRED("Key Injection Required"),
        ;
        public String displayName;

        STATUS_EVENT(String displayName) {
            this.displayName = displayName;
        }
    }

}
