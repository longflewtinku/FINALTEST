package com.linkly.libconfig;

import androidx.annotation.Keep;

@Keep
public class OverrideParameters {
    private String currencyCode;                    //>826</currencyNum>
    private String currencyNum;                        //>826</currencyNum>
    private String countryNum;                         //>826</countryNum>


    private String dccSupported;                    //>N</dccSupported>
    private String deferredAuthEnabled;              //>N</deferredAuthEnabled>
    private String maxDeferredAuthCount;                //>5</maxDeferredAuthCount>
    private String maxDeferredAuthValue;                 //>50</maxDeferredAuthValue>

    private String language;                           //en_GB \ is_IS
    private String paymentSwitch_ip_host;           //>35.190.51.137:700</paymentSwitch.ip.host>
    private String paymentSwitch_ip_host2nd;        //35.190.51.137:700</paymentSwitch.ip.host2nd>
    private String paymentSwitch_receiveTimeout;
    private String paymentSwitch_dialTimeout;
    private String paymentSwitch_commsType;
    private String paymentSwitch_ipGatewayHost;
    private String paymentSwitch_ipGatewayUser;
    private String paymentSwitch_ipGatewayPwd;

    private String paymentSwitch_certificateFile;      //ps.pem</paymentSwitch.certificateFile>
    private String paymentSwitch_privateKeyFile;       //ClientV2.key</paymentSwitch.privateKeyFile>
    private String paymentSwitch_privateKeyPassword;   //ps.pem</paymentSwitch.privateKeyPassword>
    private String paymentSwitch_privateKeyCertificate;//pxpClientV2.pem</paymentSwitch.privateKeyCertificate>
    private String paymentSwitch_aiic;              // for Australian acquirers
    private String paymentSwitch_nii;              // for Australian acquirers
    private String paymentSwitch_useSsl;            //Y</paymentSwitch.useSsl>
    private String paymentSwitch_disableSecurity;            // if yes, disables maccing, card encryption etc
    private String paymentSwitch_defaultPktVersion;     // default table version number for woolworths
    private String paymentSwitch_defaultEpatVersion;        // default table version number for woolworths
    private String paymentSwitch_defaultSpotVersion;        // default table version number for woolworths
    private String paymentSwitch_defaultFcatVersion;        // default table version number for woolworths

    private String seller_id;
    private String seller_email;
    private String seller_telephone;
    private String payment_facilitator;


    private String mailCardholder;
    private String mailMerchant;
    private String mailMerchantAddress;
    private String mailHost;
    private String mailPort;
    private String mailUser;
    private String mailPassword;
    private String mailSender;
    private String mailStoreName;
    private String paxstoreUpload;
    private String offlineTransactionCeilingLimitCentsContact;
    private String offlineTransactionCeilingLimitCentsContactless;

    // Total terminal supported limit not for specific to cards.

    // Refund limit per transaction
    private String offlineFlightModeAllowed;
    private String offlineSoftLimitAmountCents;
    private String offlineSoftLimitCount;
    private String offlineUpperLimitAmountCents;
    private String offlineUpperLimitCount;
    private String unattendedModeAllowed;
    private String bankDescription;
    private String retailerName;
    private String commsFallbackEnabled;
    private String commsFallbackHost;


    // POS connection config
    private String posCommsEnabled;
    private String posCommsHostId;
    private String posCommsInterfaceType;

    // issuer table settings
    private String issuer1Name;
    private String issuer1Enabled;
    private String issuer1DeferredAuthEnabled;
    private String issuer2Name;
    private String issuer2Enabled;
    private String issuer2DeferredAuthEnabled;
    private String issuer3Name;
    private String issuer3Enabled;
    private String issuer3DeferredAuthEnabled;
    private String issuer4Name;
    private String issuer4Enabled;
    private String issuer4DeferredAuthEnabled;
    private String issuer5Name;
    private String issuer5Enabled;
    private String issuer5DeferredAuthEnabled;
    private String issuer6Name;
    private String issuer6Enabled;
    private String issuer6DeferredAuthEnabled;
    private String issuer7Name;
    private String issuer7Enabled;
    private String issuer7DeferredAuthEnabled;
    private String issuer8Name;
    private String issuer8Enabled;
    private String issuer8DeferredAuthEnabled;
    private String issuer9Name;
    private String issuer9Enabled;
    private String issuer9DeferredAuthEnabled;
    private String issuer10Name;
    private String issuer10Enabled;
    private String issuer10DeferredAuthEnabled;

    private String cdoAllowedEftpos;
    private String cdoAllowedMastercardCredit;
    private String cdoAllowedMastercardDebit;
    private String cdoAllowedVisaCredit;
    private String cdoAllowedVisaDebit;
    private String cdoAllowedAmex;
    private String cdoAllowedDinersClub;
    private String cdoAllowedJcb;
    private String cdoAllowedUnionpayCredit;

    private String loginManagerUserId;
    private String loginManagerUserName;
    private String loginManagerInitialPwd;

    private String loginTechnicianUserId;
    private String loginTechnicianUserName;
    private String loginTechnicianInitialPwd;

    //linkly bin number
    private String bin_eftpos;
    private String bin_mastercard_credit;
    private String bin_mastercard_debit;
    private String bin_visa_credit;
    private String bin_visa_debit;
    private String bin_amex;
    private String bin_diners_club;
    private String bin_jcb;
    private String bin_unionpay_credit;


    // Branding
    private String brandDisplayLogoHeader;
    private String brandDisplayLogoSplash;
    private String brandDisplayLogoIdle;
    private String brandDisplayStatusBarColour;
    private String brandDisplayButtonColour;
    private String brandDisplayButtonTextColour;
    private String brandDisplayPrimaryColour;
    private String brandReceiptLogoHeader;
    private String preauthExpiry_default;
    private String preauthExpiry_eftpos;
    private String preauthExpiry_mastercard_credit;
    private String preauthExpiry_mastercard_debit;
    private String preauthExpiry_visa_credit;
    private String preauthExpiry_visa_debit;
    private String preauthExpiry_amex;
    private String preauthExpiry_diners_club;
    private String preauthExpiry_jcb;
    private String preauthExpiry_unionpay_credit;

    //max pre-auth transaction stored on terminal

    //max EFB transactions stored on terminal

    // Maximum screen time
    private String screenLockTime;
    // CTLS CVM limit cents, if the ctlsCvmLimitEnabled flag is set

    // Password from TMS/Config file for Refunds & Moto transactions
    private String motoRefundPassword;
    // Password from TMS/Config file for Refunds
    private String refundPassword;
    /**
     * Amount Entry Screen Timeout (Secs)
     * Is only applicable in Standalone mode operation
     */
    private String amountEntryTimeoutSecs;
    /**
     * Card Present Timeout (Secs)
     */
    private String presentCardTimeoutSecs;
    /**
     * Account Selection Timeout (Secs)
     * Will be applicable for account selection screens
     */
    private String accountSelectionTimeoutSecs;
    /**
     * Application Selection Timeout (Secs)
     * Will be applicable for application selection screens
     */
    private String appSelectionTimeoutSecs;
    /**
     * Card PIN Entry Timeout (Secs)
     */
    private String cardPinEntryTimeoutSecs;
    /**
     * Confirm Signature Timeout (Secs)
     */
    private String confirmSignatureTimeoutSecs;
    /**
     * Print Customer Receipt Timeout (Secs)
     * Only applicable if Terminal is printing receipts + also printing customer receipts
     */
    private String customerReceiptPrintPromptTimeoutSecs;
    /**
     * Remove Merchant Receipt Timeout (Secs)
     * Only applicable if Terminal is printing receipts + also printing customer receipts
     */
    private String receiptRemovePromptTimeoutSecs;
    /**
     * Remove Card Timeout (Secs)
     */
    private String cardRemovePromptTimeoutSecs;
    /**
     * Paper Out Timeout (Secs)
     */
    private String paperOutTimeoutSecs;
    /**
     * Decision Screen (Approved, Declined, Cancelled) Timeout (Secs)
     */
    private String decisionScreenTimeoutSecs;
    /**
     * Card Present Timeout for access mode
     * Time in secs
     */
    private String accessModePresentCardTimeoutSecs;
    /**
     * Account Selection Screen Timeout in Access Mode
     * Time in secs
     */
    private String accessModeAccountSelectionTimeoutSecs;
    /**
     * Application Selection Screen Timeout in Access Mode
     * Time in secs
     */
    private String accessModeAppSelectionTimeoutSecs;
    /**
     * Card Pin Entry Timeout in Access Mode
     * Time in secs
     */
    private String accessModePinEntryTimeoutSecs;
    /**
     * Decision Screen (Approved, Declined, Cancelled) Timeout in Access Mode
     * Time in secs
     */
    private String accessModeDecisionScreenTimeoutSecs;

    // NOTE: must match the parameter names given in paxstore template file
    private String passcodeSecuritySupported;
    private String cardProductFile;
    private String cfgEmvFile;
    private String cfgCtlsFile;
    private String blacklistFile;
    private String epatFile;
    private String pktFile;
    private String cardsFile;
    private String passwordRetryLimit;
    private String passwordAttemptWindow;
    private String passwordAttemptLockoutDuration;
    private String pciRebootTime;
    private String passwordMaximumAge;
    private String refundPasswordRetryLimit;
    private String motoPasswordRetryLimit;
    private String rrnRetryLimit;
    private String authcodeRetryLimit;
    private String autoSettlementEnabled;
    private String autoSettlementRetryCount;
    private String autoSettlementPrintTransactionListing;
    private String autoSettlementTime;
    private String autoSettlementTimeWindow;
    private String autoSettlementIdlingPeriod;

    private String maxDaysTransactionsToStore;

    public String getCurrencyCode() {
        return this.currencyCode;
    }

    public String getCurrencyNum() {
        return this.currencyNum;
    }

    public String getCountryNum() {
        return this.countryNum;
    }

    public String getDccSupported() {
        return this.dccSupported;
    }

    public String getDeferredAuthEnabled() {
        return this.deferredAuthEnabled;
    }

    public String getMaxDeferredAuthCount() {
        return this.maxDeferredAuthCount;
    }

    public String getMaxDeferredAuthValue() {
        return this.maxDeferredAuthValue;
    }

    public String getLanguage() {
        return this.language;
    }

    public String getPaymentSwitch_ip_host() {
        return this.paymentSwitch_ip_host;
    }

    public String getPaymentSwitch_ip_host2nd() {
        return this.paymentSwitch_ip_host2nd;
    }

    public String getPaymentSwitch_receiveTimeout() {
        return this.paymentSwitch_receiveTimeout;
    }

    public String getPaymentSwitch_dialTimeout() {
        return this.paymentSwitch_dialTimeout;
    }

    public String getPaymentSwitch_commsType() {
        return this.paymentSwitch_commsType;
    }

    public String getPaymentSwitch_ipGatewayHost() {
        return this.paymentSwitch_ipGatewayHost;
    }

    public String getPaymentSwitch_ipGatewayUser() {
        return this.paymentSwitch_ipGatewayUser;
    }

    public String getPaymentSwitch_ipGatewayPwd() {
        return this.paymentSwitch_ipGatewayPwd;
    }

    public String getPaymentSwitch_certificateFile() {
        return this.paymentSwitch_certificateFile;
    }

    public String getPaymentSwitch_privateKeyFile() {
        return this.paymentSwitch_privateKeyFile;
    }

    public String getPaymentSwitch_privateKeyPassword() {
        return this.paymentSwitch_privateKeyPassword;
    }

    public String getPaymentSwitch_privateKeyCertificate() {
        return this.paymentSwitch_privateKeyCertificate;
    }

    public String getPaymentSwitch_aiic() {
        return this.paymentSwitch_aiic;
    }

    public String getPaymentSwitch_nii() {
        return this.paymentSwitch_nii;
    }

    public String getPaymentSwitch_useSsl() {
        return this.paymentSwitch_useSsl;
    }

    public String getPaymentSwitch_disableSecurity() {
        return this.paymentSwitch_disableSecurity;
    }

    public String getPaymentSwitch_defaultPktVersion() {
        return this.paymentSwitch_defaultPktVersion;
    }

    public String getPaymentSwitch_defaultEpatVersion() {
        return this.paymentSwitch_defaultEpatVersion;
    }

    public String getPaymentSwitch_defaultSpotVersion() {
        return this.paymentSwitch_defaultSpotVersion;
    }

    public String getPaymentSwitch_defaultFcatVersion() {
        return this.paymentSwitch_defaultFcatVersion;
    }

    public String getSeller_id() {
        return this.seller_id;
    }

    public String getSeller_email() {
        return this.seller_email;
    }

    public String getSeller_telephone() {
        return this.seller_telephone;
    }

    public String getPayment_facilitator() {
        return this.payment_facilitator;
    }

    public String getMailCardholder() {
        return this.mailCardholder;
    }

    public String getMailMerchant() {
        return this.mailMerchant;
    }

    public String getMailMerchantAddress() {
        return this.mailMerchantAddress;
    }

    public String getMailHost() {
        return this.mailHost;
    }

    public String getMailPort() {
        return this.mailPort;
    }

    public String getMailUser() {
        return this.mailUser;
    }

    public String getMailPassword() {
        return this.mailPassword;
    }

    public String getMailSender() {
        return this.mailSender;
    }

    public String getMailStoreName() {
        return this.mailStoreName;
    }

    public String getPaxstoreUpload() {
        return this.paxstoreUpload;
    }

    public String getOfflineTransactionCeilingLimitCentsContact() {
        return this.offlineTransactionCeilingLimitCentsContact;
    }

    public String getOfflineTransactionCeilingLimitCentsContactless() {
        return this.offlineTransactionCeilingLimitCentsContactless;
    }

    public String getOfflineFlightModeAllowed() {
        return this.offlineFlightModeAllowed;
    }

    public String getOfflineSoftLimitAmountCents() {
        return this.offlineSoftLimitAmountCents;
    }

    public String getOfflineSoftLimitCount() {
        return this.offlineSoftLimitCount;
    }

    public String getOfflineUpperLimitAmountCents() {
        return this.offlineUpperLimitAmountCents;
    }

    public String getOfflineUpperLimitCount() {
        return this.offlineUpperLimitCount;
    }

    public String getUnattendedModeAllowed() {
        return this.unattendedModeAllowed;
    }

    public String getBankDescription() {
        return this.bankDescription;
    }

    public String getRetailerName() {
        return this.retailerName;
    }

    public String getCommsFallbackEnabled() {
        return this.commsFallbackEnabled;
    }

    public String getCommsFallbackHost() {
        return this.commsFallbackHost;
    }

    public String getPosCommsEnabled() {
        return this.posCommsEnabled;
    }

    public String getPosCommsHostId() {
        return this.posCommsHostId;
    }

    public String getPosCommsInterfaceType() {
        return this.posCommsInterfaceType;
    }

    public String getIssuer1Name() {
        return this.issuer1Name;
    }

    public String getIssuer1Enabled() {
        return this.issuer1Enabled;
    }

    public String getIssuer1DeferredAuthEnabled() {
        return this.issuer1DeferredAuthEnabled;
    }

    public String getIssuer2Name() {
        return this.issuer2Name;
    }

    public String getIssuer2Enabled() {
        return this.issuer2Enabled;
    }

    public String getIssuer2DeferredAuthEnabled() {
        return this.issuer2DeferredAuthEnabled;
    }

    public String getIssuer3Name() {
        return this.issuer3Name;
    }

    public String getIssuer3Enabled() {
        return this.issuer3Enabled;
    }

    public String getIssuer3DeferredAuthEnabled() {
        return this.issuer3DeferredAuthEnabled;
    }

    public String getIssuer4Name() {
        return this.issuer4Name;
    }

    public String getIssuer4Enabled() {
        return this.issuer4Enabled;
    }

    public String getIssuer4DeferredAuthEnabled() {
        return this.issuer4DeferredAuthEnabled;
    }

    public String getIssuer5Name() {
        return this.issuer5Name;
    }

    public String getIssuer5Enabled() {
        return this.issuer5Enabled;
    }

    public String getIssuer5DeferredAuthEnabled() {
        return this.issuer5DeferredAuthEnabled;
    }

    public String getIssuer6Name() {
        return this.issuer6Name;
    }

    public String getIssuer6Enabled() {
        return this.issuer6Enabled;
    }

    public String getIssuer6DeferredAuthEnabled() {
        return this.issuer6DeferredAuthEnabled;
    }

    public String getIssuer7Name() {
        return this.issuer7Name;
    }

    public String getIssuer7Enabled() {
        return this.issuer7Enabled;
    }

    public String getIssuer7DeferredAuthEnabled() {
        return this.issuer7DeferredAuthEnabled;
    }

    public String getIssuer8Name() {
        return this.issuer8Name;
    }

    public String getIssuer8Enabled() {
        return this.issuer8Enabled;
    }

    public String getIssuer8DeferredAuthEnabled() {
        return this.issuer8DeferredAuthEnabled;
    }

    public String getIssuer9Name() {
        return this.issuer9Name;
    }

    public String getIssuer9Enabled() {
        return this.issuer9Enabled;
    }

    public String getIssuer9DeferredAuthEnabled() {
        return this.issuer9DeferredAuthEnabled;
    }

    public String getIssuer10Name() {
        return this.issuer10Name;
    }

    public String getIssuer10Enabled() {
        return this.issuer10Enabled;
    }

    public String getIssuer10DeferredAuthEnabled() {
        return this.issuer10DeferredAuthEnabled;
    }

    public String getCdoAllowedEftpos() {
        return this.cdoAllowedEftpos;
    }

    public String getCdoAllowedMastercardCredit() {
        return this.cdoAllowedMastercardCredit;
    }

    public String getCdoAllowedMastercardDebit() {
        return this.cdoAllowedMastercardDebit;
    }

    public String getCdoAllowedVisaCredit() {
        return this.cdoAllowedVisaCredit;
    }

    public String getCdoAllowedVisaDebit() {
        return this.cdoAllowedVisaDebit;
    }

    public String getCdoAllowedAmex() {
        return this.cdoAllowedAmex;
    }

    public String getCdoAllowedDinersClub() {
        return this.cdoAllowedDinersClub;
    }

    public String getCdoAllowedJcb() {
        return this.cdoAllowedJcb;
    }

    public String getCdoAllowedUnionpayCredit() {
        return this.cdoAllowedUnionpayCredit;
    }

    public String getLoginManagerUserId() {
        return this.loginManagerUserId;
    }

    public String getLoginManagerUserName() {
        return this.loginManagerUserName;
    }

    public String getLoginManagerInitialPwd() {
        return this.loginManagerInitialPwd;
    }

    public String getLoginTechnicianUserId() {
        return this.loginTechnicianUserId;
    }

    public String getLoginTechnicianUserName() {
        return this.loginTechnicianUserName;
    }

    public String getLoginTechnicianInitialPwd() {
        return this.loginTechnicianInitialPwd;
    }

    public String getBin_eftpos() {
        return this.bin_eftpos;
    }

    public String getBin_mastercard_credit() {
        return this.bin_mastercard_credit;
    }

    public String getBin_mastercard_debit() {
        return this.bin_mastercard_debit;
    }

    public String getBin_visa_credit() {
        return this.bin_visa_credit;
    }

    public String getBin_visa_debit() {
        return this.bin_visa_debit;
    }

    public String getBin_amex() {
        return this.bin_amex;
    }

    public String getBin_diners_club() {
        return this.bin_diners_club;
    }

    public String getBin_jcb() {
        return this.bin_jcb;
    }

    public String getBin_unionpay_credit() {
        return this.bin_unionpay_credit;
    }

    public String getBrandDisplayLogoHeader() {
        return this.brandDisplayLogoHeader;
    }

    public String getBrandDisplayLogoSplash() {
        return this.brandDisplayLogoSplash;
    }

    public String getBrandDisplayLogoIdle() {
        return this.brandDisplayLogoIdle;
    }

    public String getBrandDisplayStatusBarColour() {
        return this.brandDisplayStatusBarColour;
    }

    public String getBrandDisplayButtonColour() {
        return this.brandDisplayButtonColour;
    }

    public String getBrandDisplayButtonTextColour() {
        return this.brandDisplayButtonTextColour;
    }

    public String getBrandDisplayPrimaryColour() {
        return this.brandDisplayPrimaryColour;
    }

    public String getBrandReceiptLogoHeader() {
        return this.brandReceiptLogoHeader;
    }

    public String getPreauthExpiry_default() {
        return this.preauthExpiry_default;
    }

    public String getPreauthExpiry_eftpos() {
        return this.preauthExpiry_eftpos;
    }

    public String getPreauthExpiry_mastercard_credit() {
        return this.preauthExpiry_mastercard_credit;
    }

    public String getPreauthExpiry_mastercard_debit() {
        return this.preauthExpiry_mastercard_debit;
    }

    public String getPreauthExpiry_visa_credit() {
        return this.preauthExpiry_visa_credit;
    }

    public String getPreauthExpiry_visa_debit() {
        return this.preauthExpiry_visa_debit;
    }

    public String getPreauthExpiry_amex() {
        return this.preauthExpiry_amex;
    }

    public String getPreauthExpiry_diners_club() {
        return this.preauthExpiry_diners_club;
    }

    public String getPreauthExpiry_jcb() {
        return this.preauthExpiry_jcb;
    }

    public String getPreauthExpiry_unionpay_credit() {
        return this.preauthExpiry_unionpay_credit;
    }

    public String getScreenLockTime() {
        return this.screenLockTime;
    }

    public String getMotoRefundPassword() {
        return this.motoRefundPassword;
    }

    public String getRefundPassword() {
        return this.refundPassword;
    }

    public String getAmountEntryTimeoutSecs() {
        return this.amountEntryTimeoutSecs;
    }

    public String getPresentCardTimeoutSecs() {
        return this.presentCardTimeoutSecs;
    }

    public String getAccountSelectionTimeoutSecs() {
        return this.accountSelectionTimeoutSecs;
    }

    public String getAppSelectionTimeoutSecs() {
        return this.appSelectionTimeoutSecs;
    }

    public String getCardPinEntryTimeoutSecs() {
        return this.cardPinEntryTimeoutSecs;
    }

    public String getConfirmSignatureTimeoutSecs() {
        return this.confirmSignatureTimeoutSecs;
    }

    public String getCustomerReceiptPrintPromptTimeoutSecs() {
        return this.customerReceiptPrintPromptTimeoutSecs;
    }

    public String getReceiptRemovePromptTimeoutSecs() {
        return this.receiptRemovePromptTimeoutSecs;
    }

    public String getCardRemovePromptTimeoutSecs() {
        return this.cardRemovePromptTimeoutSecs;
    }

    public String getPaperOutTimeoutSecs() {
        return this.paperOutTimeoutSecs;
    }

    public String getDecisionScreenTimeoutSecs() {
        return this.decisionScreenTimeoutSecs;
    }

    public String getAccessModePresentCardTimeoutSecs() {
        return this.accessModePresentCardTimeoutSecs;
    }

    public String getAccessModeAccountSelectionTimeoutSecs() {
        return this.accessModeAccountSelectionTimeoutSecs;
    }

    public String getAccessModeAppSelectionTimeoutSecs() {
        return this.accessModeAppSelectionTimeoutSecs;
    }

    public String getAccessModePinEntryTimeoutSecs() {
        return this.accessModePinEntryTimeoutSecs;
    }

    public String getAccessModeDecisionScreenTimeoutSecs() {
        return this.accessModeDecisionScreenTimeoutSecs;
    }

    public String getPasscodeSecuritySupported() {
        return this.passcodeSecuritySupported;
    }

    public String getCardProductFile() {
        return this.cardProductFile;
    }

    public String getCfgEmvFile() {
        return this.cfgEmvFile;
    }

    public String getCfgCtlsFile() {
        return this.cfgCtlsFile;
    }

    public String getBlacklistFile() {
        return this.blacklistFile;
    }

    public String getEpatFile() {
        return this.epatFile;
    }

    public String getPktFile() {
        return this.pktFile;
    }

    public String getCardsFile() {
        return this.cardsFile;
    }

    public String getPasswordRetryLimit() {
        return this.passwordRetryLimit;
    }

    public String getPasswordAttemptWindow() {
        return this.passwordAttemptWindow;
    }

    public String getPasswordAttemptLockoutDuration() {
        return this.passwordAttemptLockoutDuration;
    }

    public String getPciRebootTime() {
        return this.pciRebootTime;
    }

    public String getPasswordMaximumAge() {
        return this.passwordMaximumAge;
    }

    public String getRefundPasswordRetryLimit() {
        return this.refundPasswordRetryLimit;
    }

    public String getMotoPasswordRetryLimit() {
        return this.motoPasswordRetryLimit;
    }

    public String getRrnRetryLimit() {
        return this.rrnRetryLimit;
    }

    public String getAuthcodeRetryLimit() {
        return this.authcodeRetryLimit;
    }

    public String getAutoSettlementEnabled() {
        return this.autoSettlementEnabled;
    }

    public String getAutoSettlementRetryCount() {
        return this.autoSettlementRetryCount;
    }

    public String getAutoSettlementPrintTransactionListing() {
        return this.autoSettlementPrintTransactionListing;
    }

    public String getAutoSettlementTime() {
        return this.autoSettlementTime;
    }

    public String getAutoSettlementTimeWindow() {
        return this.autoSettlementTimeWindow;
    }

    public String getAutoSettlementIdlingPeriod() {
        return this.autoSettlementIdlingPeriod;
    }

    public String getMaxDaysTransactionsToStore() {
        return this.maxDaysTransactionsToStore;
    }
}
