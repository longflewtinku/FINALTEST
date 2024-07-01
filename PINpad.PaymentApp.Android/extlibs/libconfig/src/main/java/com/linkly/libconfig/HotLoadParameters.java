package com.linkly.libconfig;

import androidx.annotation.Keep;

@Keep
public class HotLoadParameters {

    // ---------- Receipt Text Config ------------
    private String receipt_merchant_line0;
    private String receipt_merchant_line1;
    private String receipt_merchant_line2;
    private String receipt_merchant_line3;
    private String receipt_merchant_line4;
    private String receipt_merchant_line5;
    private String receipt_merchant_line6;
    private String footerLine1;
    private String footerLine2;

    // ----------- MCR Settings --------------
    private String mcrUpperLimit; // if txn value <= this, then mcr logic will apply for this AID. OVERRIDES cfg_ctls_emv.xml AND emvgroup.json settings // If empty or invalid then upper limit check will be skipped
    private String mcrLimit; // if txn value >= this, then mcr logic will apply for this AID. OVERRIDES cfg_ctls_emv.xml AND emvgroup.json settings
    private String mcrEnabled; // if txn value is between mcrLimit & mcrUpperLimit inclusive and this is true for at least one AID, other non-supported AIDs will be excluded from app selection process. OVERRIDES cfg_ctls_emv.xml AND emvgroup.json settings

    // -------------- Terminal Limits ------------------
    private String saleLimitCents;
    private String cashoutLimitCents;
    private String maxRefundLimit;
    private String managerRefundLimit;
    private String maxRefundCount;
    private String maxCumulativeRefundLimit;
    private String maxTipPercent;
    private String maxPreAuthTrans;
    private String maxEfbTrans;
    private String preAuthLimitCents;
    private String overrideCtlsCvmLimitEnabled;
    private String overrideCtlsCvmLimit;

    // -------------- Terminal Configuration ------------------
    private String showReceiptPromptForAuto;
    private String printCustomerReceipt;
    private String motoPasswordPrompt;
    private String refundPasswordPrompt;
    private String motoCVVEntry;
    private String motoCVVEntryBypassAllowed;
    private String mailOrder;
    private String telephone;
    private String cashout;
    private String cashback;
    private String preauth;
    private String preauthCreditAccountOnly;
    private String refund;
    private String reversal;
    private String tipAllowed;
    private String custRefRequired;
    private String emvSupported;
    private String cardholderPresent;
    private String contactlessSupported;
    private String loyaltySupported;
    private String version;
    private String efbSupported;
    private String efbAcknowledgeServiceCode;
    private String efbPlasticCardLifeDays;
    private String efbRefundAllowed;
    private String efbCashoutAllowed;
    private String efbContinueInFallbackTimeoutMinutes;
    private String efbAuthNumberOverFloorLimitAllowed;
    private String accessMode;
    private String emvFallback;
    private String msrAllowed;
    private String refundSecure;
    private String signatureSupported;
    private String useCustomAudioForResult;
    //  ------------------ Surcharge  ------------------
    private String surchargeSupported;
    private String sc_eftpos_on;
    private String sc_eftpos_type;
    private String sc_eftpos_amount;

    private String sc_mastercard_credit_on;
    private String sc_mastercard_credit_type;
    private String sc_mastercard_credit_amount;

    private String sc_mastercard_debit_on;
    private String sc_mastercard_debit_type;
    private String sc_mastercard_debit_amount;

    private String sc_visa_credit_on;
    private String sc_visa_credit_type;
    private String sc_visa_credit_amount;

    private String sc_visa_debit_on;
    private String sc_visa_debit_type;
    private String sc_visa_debit_amount;

    private String sc_amex_on;
    private String sc_amex_type;
    private String sc_amex_amount;

    private String sc_diners_club_on;
    private String sc_diners_club_type;
    private String sc_diners_club_amount;

    private String sc_jcb_on;
    private String sc_jcb_type;
    private String sc_jcb_amount;

    private String sc_unionpay_credit_on;
    private String sc_unionpay_credit_type;
    private String sc_unionpay_credit_amount;

    public String getMotoCVVEntryBypassAllowed() {
        return motoCVVEntryBypassAllowed;
    }

    public void setMotoCVVEntryBypassAllowed(String motoCVVEntryBypassAllowed) {
        this.motoCVVEntryBypassAllowed = motoCVVEntryBypassAllowed;
    }

    public String getEfbAcknowledgeServiceCode() {
        return efbAcknowledgeServiceCode;
    }

    public void setEfbAcknowledgeServiceCode(String efbAcknowledgeServiceCode) {
        this.efbAcknowledgeServiceCode = efbAcknowledgeServiceCode;
    }

    public String getEfbPlasticCardLifeDays() {
        return efbPlasticCardLifeDays;
    }

    public void setEfbPlasticCardLifeDays(String efbPlasticCardLifeDays) {
        this.efbPlasticCardLifeDays = efbPlasticCardLifeDays;
    }

    public String getEfbRefundAllowed() {
        return efbRefundAllowed;
    }

    public void setEfbRefundAllowed(String efbRefundAllowed) {
        this.efbRefundAllowed = efbRefundAllowed;
    }

    public String getEfbCashoutAllowed() {
        return efbCashoutAllowed;
    }

    public void setEfbCashoutAllowed(String efbCashoutAllowed) {
        this.efbCashoutAllowed = efbCashoutAllowed;
    }

    public String getEfbContinueInFallbackTimeoutMinutes() {
        return efbContinueInFallbackTimeoutMinutes;
    }

    public void setEfbContinueInFallbackTimeoutMinutes(String efbContinueInFallbackTimeoutMinutes) {
        this.efbContinueInFallbackTimeoutMinutes = efbContinueInFallbackTimeoutMinutes;
    }

    public String getEfbAuthNumberOverFloorLimitAllowed() {
        return efbAuthNumberOverFloorLimitAllowed;
    }

    public void setEfbAuthNumberOverFloorLimitAllowed(String efbAuthNumberOverFloorLimitAllowed) {
        this.efbAuthNumberOverFloorLimitAllowed = efbAuthNumberOverFloorLimitAllowed;
    }

    public String getRefund() {
        return refund;
    }

    public void setRefund(String refund) {
        this.refund = refund;
    }


    public String getReceipt_merchant_line0() {
        return receipt_merchant_line0;
    }

    public void setReceipt_merchant_line0(String receipt_merchant_line0) {
        this.receipt_merchant_line0 = receipt_merchant_line0;
    }

    public String getReceipt_merchant_line1() {
        return receipt_merchant_line1;
    }

    public void setReceipt_merchant_line1(String receipt_merchant_line1) {
        this.receipt_merchant_line1 = receipt_merchant_line1;
    }

    public String getReceipt_merchant_line2() {
        return receipt_merchant_line2;
    }

    public void setReceipt_merchant_line2(String receipt_merchant_line2) {
        this.receipt_merchant_line2 = receipt_merchant_line2;
    }

    public String getReceipt_merchant_line3() {
        return receipt_merchant_line3;
    }

    public void setReceipt_merchant_line3(String receipt_merchant_line3) {
        this.receipt_merchant_line3 = receipt_merchant_line3;
    }

    public String getReceipt_merchant_line4() {
        return receipt_merchant_line4;
    }

    public void setReceipt_merchant_line4(String receipt_merchant_line4) {
        this.receipt_merchant_line4 = receipt_merchant_line4;
    }

    public String getReceipt_merchant_line5() {
        return receipt_merchant_line5;
    }

    public void setReceipt_merchant_line5(String receipt_merchant_line5) {
        this.receipt_merchant_line5 = receipt_merchant_line5;
    }

    public String getReceipt_merchant_line6() {
        return receipt_merchant_line6;
    }

    public void setReceipt_merchant_line6(String receipt_merchant_line6) {
        this.receipt_merchant_line6 = receipt_merchant_line6;
    }

    public String getFooterLine1() {
        return footerLine1;
    }

    public void setFooterLine1(String footerLine1) {
        this.footerLine1 = footerLine1;
    }

    public String getFooterLine2() {
        return footerLine2;
    }

    public void setFooterLine2(String footerLine2) {
        this.footerLine2 = footerLine2;
    }

    public String getMcrUpperLimit() {
        return mcrUpperLimit;
    }

    public void setMcrUpperLimit(String mcrUpperLimit) {
        this.mcrUpperLimit = mcrUpperLimit;
    }

    public String getMcrLimit() {
        return mcrLimit;
    }

    public void setMcrLimit(String mcrLimit) {
        this.mcrLimit = mcrLimit;
    }

    public String getMcrEnabled() {
        return mcrEnabled;
    }

    public void setMcrEnabled(String mcrEnabled) {
        this.mcrEnabled = mcrEnabled;
    }

    public String getSaleLimitCents() {
        return saleLimitCents;
    }

    public void setSaleLimitCents(String saleLimitCents) {
        this.saleLimitCents = saleLimitCents;
    }

    public String getCashoutLimitCents() {
        return cashoutLimitCents;
    }

    public void setCashoutLimitCents(String cashoutLimitCents) {
        this.cashoutLimitCents = cashoutLimitCents;
    }

    public String getMaxRefundLimit() {
        return maxRefundLimit;
    }

    public void setMaxRefundLimit(String maxRefundLimit) {
        this.maxRefundLimit = maxRefundLimit;
    }

    public String getManagerRefundLimit() {
        return managerRefundLimit;
    }

    public void setManagerRefundLimit(String managerRefundLimit) {
        this.managerRefundLimit = managerRefundLimit;
    }

    public String getMaxRefundCount() {
        return maxRefundCount;
    }

    public void setMaxRefundCount(String maxRefundCount) {
        this.maxRefundCount = maxRefundCount;
    }

    public String getMaxCumulativeRefundLimit() {
        return maxCumulativeRefundLimit;
    }

    public void setMaxCumulativeRefundLimit(String maxCumulativeRefundLimit) {
        this.maxCumulativeRefundLimit = maxCumulativeRefundLimit;
    }

    public String getMaxTipPercent() {
        return maxTipPercent;
    }

    public void setMaxTipPercent(String maxTipPercent) {
        this.maxTipPercent = maxTipPercent;
    }

    public String getMaxPreAuthTrans() {
        return maxPreAuthTrans;
    }

    public void setMaxPreAuthTrans(String maxPreAuthTrans) {
        this.maxPreAuthTrans = maxPreAuthTrans;
    }

    public String getMaxEfbTrans() {
        return maxEfbTrans;
    }

    public void setMaxEfbTrans(String maxEfbTrans) {
        this.maxEfbTrans = maxEfbTrans;
    }

    public String getPreAuthLimitCents() {
        return preAuthLimitCents;
    }

    public void setPreAuthLimitCents(String preAuthLimitCents) {
        this.preAuthLimitCents = preAuthLimitCents;
    }

    public String getShowReceiptPromptForAuto() {
        return showReceiptPromptForAuto;
    }

    public void setShowReceiptPromptForAuto(String showReceiptPromptForAuto) {
        this.showReceiptPromptForAuto = showReceiptPromptForAuto;
    }

    public String getPrintCustomerReceipt() {
        return printCustomerReceipt;
    }

    public void setPrintCustomerReceipt(String printCustomerReceipt) {
        this.printCustomerReceipt = printCustomerReceipt;
    }

    public String getMotoPasswordPrompt() {
        return motoPasswordPrompt;
    }

    public void setMotoPasswordPrompt(String motoPasswordPrompt) {
        this.motoPasswordPrompt = motoPasswordPrompt;
    }

    public String getRefundPasswordPrompt() {
        return refundPasswordPrompt;
    }

    public void setRefundPasswordPrompt(String refundPasswordPrompt) {
        this.refundPasswordPrompt = refundPasswordPrompt;
    }

    public String getMotoCVVEntry() {
        return motoCVVEntry;
    }

    public void setMotoCVVEntry(String motoCVVEntry) {
        this.motoCVVEntry = motoCVVEntry;
    }

    public String getMailOrder() {
        return mailOrder;
    }

    public void setMailOrder(String mailOrder) {
        this.mailOrder = mailOrder;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getCashout() {
        return cashout;
    }

    public void setCashout(String cashout) {
        this.cashout = cashout;
    }

    public String getCashback() {
        return cashback;
    }

    public void setCashback(String cashback) {
        this.cashback = cashback;
    }

    public String getPreauth() {
        return preauth;
    }

    public void setPreauth(String preauth) {
        this.preauth = preauth;
    }

    public String getPreauthCreditAccountOnly() {
        return preauthCreditAccountOnly;
    }

    public void setPreauthCreditAccountOnly(String preauthCreditAccountOnly) {
        this.preauthCreditAccountOnly = preauthCreditAccountOnly;
    }

    public String getReversal() {
        return reversal;
    }

    public void setReversal(String reversal) {
        this.reversal = reversal;
    }

    public String getTipAllowed() {
        return tipAllowed;
    }

    public void setTipAllowed(String tipAllowed) {
        this.tipAllowed = tipAllowed;
    }

    public String getCustRefRequired() {
        return custRefRequired;
    }

    public void setCustRefRequired(String custRefRequired) {
        this.custRefRequired = custRefRequired;
    }

    public String getEmvSupported() {
        return emvSupported;
    }

    public void setEmvSupported(String emvSupported) {
        this.emvSupported = emvSupported;
    }

    public String getCardholderPresent() {
        return cardholderPresent;
    }

    public void setCardholderPresent(String cardholderPresent) {
        this.cardholderPresent = cardholderPresent;
    }

    public String getContactlessSupported() {
        return contactlessSupported;
    }

    public void setContactlessSupported(String contactlessSupported) {
        this.contactlessSupported = contactlessSupported;
    }

    public String getLoyaltySupported() {
        return loyaltySupported;
    }

    public void setLoyaltySupported(String loyaltySupported) {
        this.loyaltySupported = loyaltySupported;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getEfbSupported() {
        return efbSupported;
    }

    public void setEfbSupported(String efbSupported) {
        this.efbSupported = efbSupported;
    }

    public String getAccessMode() {
        return accessMode;
    }

    public void setAccessMode(String accessMode) {
        this.accessMode = accessMode;
    }

    public String getEmvFallback() {
        return emvFallback;
    }

    public void setEmvFallback(String emvFallback) {
        this.emvFallback = emvFallback;
    }

    public String getMsrAllowed() {
        return msrAllowed;
    }

    public void setMsrAllowed(String msrAllowed) {
        this.msrAllowed = msrAllowed;
    }

    public String getRefundSecure() {
        return refundSecure;
    }

    public void setRefundSecure(String refundSecure) {
        this.refundSecure = refundSecure;
    }

    public String getSignatureSupported() {
        return signatureSupported;
    }

    public void setSignatureSupported(String signatureSupported) {
        this.signatureSupported = signatureSupported;
    }

    public String getUseCustomAudioForResult() {
        return useCustomAudioForResult;
    }

    public void setUseCustomAudioForResult(String useCustomAudioForResult) {
        this.useCustomAudioForResult = useCustomAudioForResult;
    }

    public String getSurchargeSupported() {
        return surchargeSupported;
    }

    public void setSurchargeSupported(String surchargeSupported) {
        this.surchargeSupported = surchargeSupported;
    }

    public String getSc_eftpos_on() {
        return sc_eftpos_on;
    }

    public void setSc_eftpos_on(String sc_eftpos_on) {
        this.sc_eftpos_on = sc_eftpos_on;
    }

    public String getSc_eftpos_type() {
        return sc_eftpos_type;
    }

    public void setSc_eftpos_type(String sc_eftpos_type) {
        this.sc_eftpos_type = sc_eftpos_type;
    }

    public String getSc_eftpos_amount() {
        return sc_eftpos_amount;
    }

    public void setSc_eftpos_amount(String sc_eftpos_amount) {
        this.sc_eftpos_amount = sc_eftpos_amount;
    }

    public String getSc_mastercard_credit_on() {
        return sc_mastercard_credit_on;
    }

    public void setSc_mastercard_credit_on(String sc_mastercard_credit_on) {
        this.sc_mastercard_credit_on = sc_mastercard_credit_on;
    }

    public String getSc_mastercard_credit_type() {
        return sc_mastercard_credit_type;
    }

    public void setSc_mastercard_credit_type(String sc_mastercard_credit_type) {
        this.sc_mastercard_credit_type = sc_mastercard_credit_type;
    }

    public String getSc_mastercard_credit_amount() {
        return sc_mastercard_credit_amount;
    }

    public void setSc_mastercard_credit_amount(String sc_mastercard_credit_amount) {
        this.sc_mastercard_credit_amount = sc_mastercard_credit_amount;
    }

    public String getSc_mastercard_debit_on() {
        return sc_mastercard_debit_on;
    }

    public void setSc_mastercard_debit_on(String sc_mastercard_debit_on) {
        this.sc_mastercard_debit_on = sc_mastercard_debit_on;
    }

    public String getSc_mastercard_debit_type() {
        return sc_mastercard_debit_type;
    }

    public void setSc_mastercard_debit_type(String sc_mastercard_debit_type) {
        this.sc_mastercard_debit_type = sc_mastercard_debit_type;
    }

    public String getSc_mastercard_debit_amount() {
        return sc_mastercard_debit_amount;
    }

    public void setSc_mastercard_debit_amount(String sc_mastercard_debit_amount) {
        this.sc_mastercard_debit_amount = sc_mastercard_debit_amount;
    }

    public String getSc_visa_credit_on() {
        return sc_visa_credit_on;
    }

    public void setSc_visa_credit_on(String sc_visa_credit_on) {
        this.sc_visa_credit_on = sc_visa_credit_on;
    }

    public String getSc_visa_credit_type() {
        return sc_visa_credit_type;
    }

    public void setSc_visa_credit_type(String sc_visa_credit_type) {
        this.sc_visa_credit_type = sc_visa_credit_type;
    }

    public String getSc_visa_credit_amount() {
        return sc_visa_credit_amount;
    }

    public void setSc_visa_credit_amount(String sc_visa_credit_amount) {
        this.sc_visa_credit_amount = sc_visa_credit_amount;
    }

    public String getSc_visa_debit_on() {
        return sc_visa_debit_on;
    }

    public void setSc_visa_debit_on(String sc_visa_debit_on) {
        this.sc_visa_debit_on = sc_visa_debit_on;
    }

    public String getSc_visa_debit_type() {
        return sc_visa_debit_type;
    }

    public void setSc_visa_debit_type(String sc_visa_debit_type) {
        this.sc_visa_debit_type = sc_visa_debit_type;
    }

    public String getSc_visa_debit_amount() {
        return sc_visa_debit_amount;
    }

    public void setSc_visa_debit_amount(String sc_visa_debit_amount) {
        this.sc_visa_debit_amount = sc_visa_debit_amount;
    }

    public String getSc_amex_on() {
        return sc_amex_on;
    }

    public void setSc_amex_on(String sc_amex_on) {
        this.sc_amex_on = sc_amex_on;
    }

    public String getSc_amex_type() {
        return sc_amex_type;
    }

    public void setSc_amex_type(String sc_amex_type) {
        this.sc_amex_type = sc_amex_type;
    }

    public String getSc_amex_amount() {
        return sc_amex_amount;
    }

    public void setSc_amex_amount(String sc_amex_amount) {
        this.sc_amex_amount = sc_amex_amount;
    }

    public String getSc_diners_club_on() {
        return sc_diners_club_on;
    }

    public void setSc_diners_club_on(String sc_diners_club_on) {
        this.sc_diners_club_on = sc_diners_club_on;
    }

    public String getSc_diners_club_type() {
        return sc_diners_club_type;
    }

    public void setSc_diners_club_type(String sc_diners_club_type) {
        this.sc_diners_club_type = sc_diners_club_type;
    }

    public String getSc_diners_club_amount() {
        return sc_diners_club_amount;
    }

    public void setSc_diners_club_amount(String sc_diners_club_amount) {
        this.sc_diners_club_amount = sc_diners_club_amount;
    }

    public String getSc_jcb_on() {
        return sc_jcb_on;
    }

    public void setSc_jcb_on(String sc_jcb_on) {
        this.sc_jcb_on = sc_jcb_on;
    }

    public String getSc_jcb_type() {
        return sc_jcb_type;
    }

    public void setSc_jcb_type(String sc_jcb_type) {
        this.sc_jcb_type = sc_jcb_type;
    }

    public String getSc_jcb_amount() {
        return sc_jcb_amount;
    }

    public void setSc_jcb_amount(String sc_jcb_amount) {
        this.sc_jcb_amount = sc_jcb_amount;
    }

    public String getSc_unionpay_credit_on() {
        return sc_unionpay_credit_on;
    }

    public void setSc_unionpay_credit_on(String sc_unionpay_credit_on) {
        this.sc_unionpay_credit_on = sc_unionpay_credit_on;
    }

    public String getSc_unionpay_credit_type() {
        return sc_unionpay_credit_type;
    }

    public void setSc_unionpay_credit_type(String sc_unionpay_credit_type) {
        this.sc_unionpay_credit_type = sc_unionpay_credit_type;
    }

    public String getSc_unionpay_credit_amount() {
        return sc_unionpay_credit_amount;
    }

    public void setSc_unionpay_credit_amount(String sc_unionpay_credit_amount) {
        this.sc_unionpay_credit_amount = sc_unionpay_credit_amount;
    }

    public String getOverrideCtlsCvmLimitEnabled() {
        return overrideCtlsCvmLimitEnabled;
    }

    public void setOverrideCtlsCvmLimitEnabled(String overrideCtlsCvmLimitEnabled) {
        this.overrideCtlsCvmLimitEnabled = overrideCtlsCvmLimitEnabled;
    }

    public String getOverrideCtlsCvmLimit() {
        return overrideCtlsCvmLimit;
    }

    public void setOverrideCtlsCvmLimit(String overrideCtlsCvmLimit) {
        this.overrideCtlsCvmLimit = overrideCtlsCvmLimit;
    }
}

