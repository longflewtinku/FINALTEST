package com.linkly.libengine.config;

import com.linkly.libconfig.HotLoadParameters;
import com.linkly.libconfig.InitialParameters;
import com.linkly.libconfig.OverrideParameters;
import com.linkly.libconfig.cpat.CardProductCfg;
import com.linkly.libconfig.uiconfig.UiConfigTimeouts;
import com.linkly.libengine.config.paycfg.ManRec;
import com.linkly.libengine.config.paycfg.ManualEntry;
import com.linkly.libengine.config.paycfg.PaymentSwitch;
import com.linkly.libengine.config.paycfg.Receipt;
import com.linkly.libengine.engine.transactions.TransRec;
import com.linkly.libengine.engine.transactions.properties.TCard;
import com.linkly.libmal.idal.ISys;
import com.linkly.libpositive.wrappers.LinklyBinNumber;
import com.linkly.libpositive.wrappers.Surcharge;
import com.linkly.libui.currency.CountryCode;

import java.util.List;

 public interface PayCfg  {

    // initialise our config based on the 3 objects that contain our configuration
    boolean loadHotloadParams(HotLoadParameters params, long lastModified);
    // Customer name was originally passed into here to be loaded as part of the config.
    boolean loadOverrideParams(String customerName, OverrideParameters params, long lastModified);
    boolean loadInitialParams(InitialParameters params, long lastModified);
    // Obtains meta data information about the previous load.
    // Helps in deciding if we want to "reset and load the config object"
    long getConfigFileMetaDataLastModifiedOverrideParams();
    long getConfigFileMetaDataLastModifiedHotloadParams();
    long getConfigFileMetaDataLastModifiedInitialParams();

    /***************************************************************/
    /* details for parsing the xml */
    boolean isCashBackAllowed();

    boolean isSaleTransAllowed();

    boolean isCashTransAllowed();

    /* if either moto or telephone allowed return true */
    /* and if the user is allowed to do manual */
    boolean isManualAllowed();
    boolean isReversalTransAllowed();
    boolean isOfflineRefundTransAllowed();
    boolean isRefundTransAllowed();
    boolean isPreAuthTransAllowed();
    boolean isCompletionTransAllowed();
    boolean isBalanceTransAllowed();
    boolean isDepositTransAllowed();
    boolean isOfflineSaleTransAllowed();
    boolean isReconciliationAllowed();
    boolean isOfflineCashAllowed();
    String getStid();
    String getMid();
    boolean isValidCfg();
    String getCustomerName();
    String getCurrencyCode();
    int getCurrencyNum();
    int getCountryNum();
    boolean isCashout();
    boolean isCashback();
    boolean isPurchase();
    boolean isRefund();
    boolean isReversal();

     boolean isCardholderPresent();
     boolean isAccessMode();
     boolean isMailOrder();
     boolean isTelephone();
     boolean isPreauth();
     boolean isPreauthCreditAccountOnly();
     String getVirtualCardNumber();
     String getSupervisorCardNumber();
     boolean isTipAllowed();
     String getPanMask();
     boolean isEmvSupported();
     boolean isContactlessSupported();
     boolean isDccSupported();
     boolean isLoyaltySupported();
     boolean isDeferredAuthEnabled();
     int getMaxDeferredAuthCount();
     int getMaxDeferredAuthValue();
     boolean isAutoUserLogin();
     int getExitAppAction();
     int getCustRefRequired();
     String getCustRefPrompt();
     int getReconBackOff1();
     int getReconBackOff2();
     int getReconBackOff3();
     String getLogo();
     boolean isDisableField55Advice();
     int getCheckTimersTimeout();
     int getScreenTimeout();
     boolean isUseP2pe();
     PaymentSwitch getPaymentSwitch();
     ManRec getManRecon();
     Receipt getReceipt();

     ManualEntry getManualEntry();
     int getCardProductVersion();
     List<CardProductCfg> getCards();

     CardProductCfg getDefaultCardProduct();
     boolean isIncludedOrginalStandInRec();
     boolean isReversalCopyOriginal();
     String getStrConfigSource();
     String getLanguage();
     String getAccessToken();
     String getAccessToken2();
     boolean isMailMerchant();
     String getMailMerchantAddress();
     String getMailHost();
     String getMailPort();
     String getMailUser();
     String getMailPassword();
     String getMailSender();
     String getMailStoreName();
     boolean isPaxstoreUpload();
     int getSaleLimitCents();
     int getPreAuthLimitCents();
     int getOfflineTransactionCeilingLimitCentsContact();
     int getOfflineTransactionCeilingLimitCentsContactless();
     int getCashoutLimitCents();
     String getMaxRefundLimit();
     boolean isOfflineFlightModeAllowed();
     int getOfflineSoftLimitAmountCents();
     int getOfflineSoftLimitCount();
     int getOfflineUpperLimitAmountCents();
     int getOfflineUpperLimitCount();
     boolean isUnattendedModeAllowed();
     String getBankDescription();
     String getRetailerName();
     boolean isCommsFallbackEnabled();
     String getCommsFallbackHost();
     boolean isUsePercentageTip();
     boolean isRefundSecure();
     boolean isEfbSupported();
     boolean isEfbAcknowledgeServiceCode();
     int getEfbPlasticCardLifeDays();
     boolean isEfbRefundAllowed();
     boolean isEfbCashoutAllowed();
     int getEfbContinueInFallbackTimeoutMinutes();
     boolean isEfbAuthNumberOverFloorLimitAllowed();
     boolean isSurchargeSupported();
     boolean isPasscodeSecuritySupported();
     int getMcrLimit();
     int getMcrUpperLimit();
     boolean isMcrEnabled();
     boolean isPosCommsEnabled();
     String getPosCommsHostId();
     String getPosCommsInterfaceType();
     boolean isEmvFallback();
     boolean isMsrAllowed();
     String getBankTimeZone();
     String getTerminalTimeZone();
     List<IssuerCfg> getIssuers();
     List<Surcharge> getDefaultSc();
     List<LinklyBinNumber> getLinklyBinNumbers();
     List<CdoAllowed> getCdoAllowedList();
     String getLoginManagerUserId();
     String getLoginManagerInitialPwd();
     String getLoginManagerUserName();
     String getLoginTechnicianUserId();
     String getLoginTechnicianInitialPwd();
     String getLoginTechnicianUserName();
     boolean isShowReceiptPromptForAuto();
     String getPrintCustomerReceipt();
     boolean isMotoPasswordPrompt();
     boolean isRefundPasswordPrompt();
     boolean isMotoCVVEntry();
     boolean isMotoCVVEntryBypassAllowed();
     String getManagerRefundLimit();
     String getMaxRefundCount();
     String getMaxCumulativeRefundLimit();
     String getMaxTipPercent();
     String getPreAuthExpiry_default();
     String getPreauthExpiry_eftpos();
     String getPreauthExpiry_mastercard_credit();
     String getPreauthExpiry_mastercard_debit();
     String getPreauthExpiry_visa_credit();
     String getPreauthExpiry_visa_debit();
     String getPreauthExpiry_amex();
     String getPreauthExpiry_diners_club();
     String getPreauthExpiry_jcb();
     String getPreauthExpiry_unionpay_credit();
     String getMaxPreAuthTrans();
     String getMaxEfbTrans();
     ISys.ScreenLockTime getScreenLockTime();
     String getMotoRefundPassword();
     String getRefundPassword();
     String getOverrideCtlsCvmLimit();
     String getOverrideCtlsCvmLimitEnabled();
     String getBrandDisplayLogoHeader();
     String getBrandDisplayLogoIdle();
     String getBrandDisplayLogoSplash();
     String getBrandDisplayStatusBarColour();
     String getBrandDisplayButtonColour();
     String getBrandDisplayButtonTextColour();
     String getBrandDisplayPrimaryColour();
     String getBrandReceiptLogoHeader();
     String getAmexSellerId();
     String getAmexSellerEmail();
     String getAmexSellerTelephone();
     String getAmexPaymentFacilitator();
     String getCardProductFile();
     String getCfgEmvFile();
     String getCfgCtlsFile();
     String getBlacklistFile();
     String getEpatFile();
     String getPktFile();
     String getCardsFile();
     UiConfigTimeouts getUiConfigTimeouts();
     String getPasswordRetryLimit();
     String getPasswordAttemptWindow();
     String getPasswordAttemptLockoutDuration();
     String getPasswordMaximumAge();
     String getRefundPasswordRetryLimit();
     String getMotoPasswordRetryLimit();
     String getRrnRetryLimit();
     String getAuthcodeRetryLimit();
     String getPciRebootTime();
     boolean isAutoSettlementEnabled();
     String getAutoSettlementRetryCount();
     boolean isAutoSettlementPrintTransactionListing();
     String getAutoSettlementTime();
     String getAutoSettlementTimeWindow();
     String getAutoSettlementIdlingPeriod();
     String getMaxDaysTransactionsToStore();
     String getIinranges(int cardIndex);
     String getAcquirerInstitutionId();
     String getAcquirerId(String cardSchemeId, String departmentId );
     String getTerminalIdentifier(String cardSchemeId, String departmentId );
     String getMerchantNumber(String cardSchemeId, String departmentId );

     boolean hasMerchantAgreement(String cardSchemeId, String departmentId) ;

     boolean isDemo();

     CountryCode getCountryCode();

     boolean isMailEnabled();

     boolean isSignatureSupported();

     int getBrandDisplayStatusBarColourOrDefault(int defaultColour);

     int getBrandDisplayButtonColourOrDefault(int defaultColour);

     int getBrandDisplayButtonTextColourOrDefault();

     int getBrandDisplayPrimaryColourOrDefault(int defaultColour);

     String getBrandDisplayLogoHeaderOrDefault();

     String getBrandDisplayLogoIdleOrDefault();

     String getBrandDisplayLogoSplashOrDefault();

     String getBrandReceiptLogoHeaderOrDefault();

     boolean isUseCustomAudioForResult();

    void setCardProductVersion(int cardProductVersion);

    void setCards(List<CardProductCfg> cards);


     boolean isReferenceEnabled(int refRequired);
     boolean isReferenceAllowed(int refRequired);

     boolean isReferenceMandatory(int refRequired);

     boolean isReferenceOptional(int refRequired);

     void setPasswordRetryLimit(String passwordRetryLimit);

     void setPasswordAttemptWindow(String passwordAttemptWindow);

     void setPasswordAttemptLockoutDuration(String passwordAttemptLockoutDuration);

     boolean getPasswordRequiredForAllCards(TransRec trans, TCard.CaptureMethod captureMethod );

     void setAutoUserLogin(boolean autoUserLogin);

     void setLoyaltySupported(boolean loyaltySupported);

     void setStid(String stid);

     void setMid(String mid);
     void setUsePercentageTip(boolean usePercentageTip);

     void setIncludedOrginalStandInRec(boolean includedOrginalStandInRec);

     void setReversalCopyOriginal(boolean reversalCopyOriginal);

     void setPaymentAppVersion(String version);

     String getPaymentAppVersion();
}
