package com.linkly.libengine.config.paycfg;

import static com.linkly.libengine.config.paycfg.ReferenceRequired.DISABLED;
import static com.linkly.libengine.config.paycfg.ReferenceRequired.MANDATORY;
import static com.linkly.libengine.config.paycfg.ReferenceRequired.OPTIONAL;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;

import androidx.annotation.Keep;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.linkly.libconfig.HotLoadParameters;
import com.linkly.libconfig.InitialParameters;
import com.linkly.libconfig.OverrideParameters;
import com.linkly.libconfig.cpat.CardProductCfg;
import com.linkly.libconfig.uiconfig.ConfigTimeouts;
import com.linkly.libconfig.uiconfig.UiConfigTimeouts;
import com.linkly.libengine.BuildConfig;
import com.linkly.libengine.config.CdoAllowed;
import com.linkly.libengine.config.IssuerCfg;
import com.linkly.libengine.config.PayCfg;
import com.linkly.libengine.engine.Engine;
import com.linkly.libengine.engine.transactions.TransRec;
import com.linkly.libengine.engine.transactions.properties.TCard;
import com.linkly.libengine.env.IdentityEnvVar;
import com.linkly.libmal.MalFactory;
import com.linkly.libmal.global.util.Util;
import com.linkly.libmal.idal.ISys;
import com.linkly.libpositive.wrappers.LinklyBinNumber;
import com.linkly.libpositive.wrappers.Surcharge;
import com.linkly.libui.currency.CountryCode;
import com.linkly.libui.currency.ISOCountryCodes;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import timber.log.Timber;

@Keep
public class PayCfgImpl implements PayCfg {



    // ------------------- Initial Params Value Id's -------------------------------
    public static final String STID = "Stid";
    public static final String MID = "Mid";
    public static final String TKEY = "Tkey";
    public static final String ACCESS_TOKEN = "Access_token";
    public static final String ACCESS_TOKEN_2 = "Access_token2";
    public static final String ACQUIRER_INSTITUTION_ID = "AcquirerInstitutionId";
    public static final String BANK_TIME_ZONE = "BankTimeZone";
    public static final String TERMINAL_TIME_ZONE = "TerminalTimeZone";
    public static final String PAYMENT_APP_VERSION = "PaymentAppVersion";
    // ------------------- Hotload Params Value Id's -------------------------------
    public static final String RECEIPT_MERCHANT_LINE_0 = "Receipt_merchant_line0";
    public static final String RECEIPT_MERCHANT_LINE_1 = "Receipt_merchant_line1";
    public static final String RECEIPT_MERCHANT_LINE_2 = "Receipt_merchant_line2";
    public static final String RECEIPT_MERCHANT_LINE_3 = "Receipt_merchant_line3";
    public static final String RECEIPT_MERCHANT_LINE_4 = "Receipt_merchant_line4";
    public static final String RECEIPT_MERCHANT_LINE_5 = "Receipt_merchant_line5";
    public static final String RECEIPT_MERCHANT_LINE_6 = "Receipt_merchant_line6";
    public static final String FOOTER_LINE_1 = "FooterLine1";
    public static final String FOOTER_LINE_2 = "FooterLine2";
    public static final String MCR_ENABLED = "McrEnabled";
    public static final String MCR_LIMIT = "McrLimit";
    public static final String MCR_UPPER_LIMIT = "McrUpperLimit";
    public static final String SALE_LIMIT_CENTS = "SaleLimitCents";
    public static final String CASHOUT_LIMIT_CENTS = "CashoutLimitCents";
    public static final String MAX_REFUND_LIMIT = "MaxRefundLimit";
    public static final String MANAGER_REFUND_LIMIT = "ManagerRefundLimit";
    public static final String MAX_REFUND_COUNT = "MaxRefundCount";
    public static final String MAX_CUMULATIVE_REFUND_LIMIT = "MaxCumulativeRefundLimit";
    public static final String MAX_TIP_PERCENT = "MaxTipPercent";
    public static final String MAX_PRE_AUTH_TRANS = "MaxPreAuthTrans";
    public static final String MAX_EFB_TRANS = "MaxEfbTrans";
    public static final String PRE_AUTH_LIMIT_CENTS = "PreAuthLimitCents";
    public static final String OVERRIDE_CTLS_CVM_LIMIT_ENABLED = "OverrideCtlsCvmLimitEnabled";
    public static final String OVERRIDE_CTLS_CVM_LIMIT = "OverrideCtlsCvmLimit";
    public static final String SHOW_RECEIPT_PROMPT_FOR_AUTO = "ShowReceiptPromptForAuto";
    public static final String PRINT_CUSTOMER_RECEIPT = "PrintCustomerReceipt";
    public static final String MOTO_PASSWORD_PROMPT = "MotoPasswordPrompt";
    public static final String REFUND_PASSWORD_PROMPT = "RefundPasswordPrompt";
    public static final String MOTO_CVV_ENTRY = "MotoCVVEntry";
    public static final String MOTO_CVV_ENTRY_BYPASS_ALLOWED = "MotoCVVEntryBypassAllowed";
    public static final String MAIL_ORDER = "MailOrder";
    public static final String TELEPHONE = "Telephone";
    public static final String PREAUTH = "Preauth";
    public static final String PREAUTH_CREDIT_ACCOUNT_ONLY = "PreauthCreditAccountOnly";
    public static final String CASHBACK = "Cashback";
    public static final String CASHOUT = "Cashout";
    public static final String REFUND = "Refund";
    public static final String REVERSAL = "Reversal";
    public static final String TIP_ALLOWED = "TipAllowed";
    public static final String CUST_REF_REQUIRED = "CustRefRequired";
    public static final String EMV_SUPPORTED = "EmvSupported";
    public static final String CARDHOLDER_PRESENT = "CardholderPresent";
    public static final String CONTACTLESS_SUPPORTED = "ContactlessSupported";
    public static final String LOYALTY_SUPPORTED = "LoyaltySupported";
    public static final String EFB_SUPPORTED = "EfbSupported";
    public static final String EFB_ACKNOWLEDGE_SERVICE_CODE = "EfbAcknowledgeServiceCode";
    public static final String EFB_PLASTIC_CARD_LIFE_DAYS = "EfbPlasticCardLifeDays";
    public static final String EFB_REFUND_ALLOWED = "EfbRefundAllowed";
    public static final String EFB_CASHOUT_ALLOWED = "EfbCashoutAllowed";
    public static final String EFB_CONTINUE_IN_FALLBACK_TIMEOUT_MINUTES = "EfbContinueInFallbackTimeoutMinutes";
    public static final String EFB_AUTH_NUMBER_OVER_FLOOR_LIMIT_ALLOWED = "EfbAuthNumberOverFloorLimitAllowed";
    public static final String ACCESS_MODE = "AccessMode";
    public static final String EMV_FALLBACK = "EmvFallback";
    public static final String MSR_ALLOWED = "MsrAllowed";
    public static final String REFUND_SECURE = "RefundSecure";
    public static final String SIGNATURE_SUPPORTED = "SignatureSupported";
    public static final String USE_CUSTOM_AUDIO_FOR_RESULT = "UseCustomAudioForResult";
    public static final String SURCHARGE_SUPPORTED = "SurchargeSupported";
    public static final String SC_EFTPOS_ON = "Sc_eftpos_on";
    public static final String SC_EFTPOS_TYPE = "Sc_eftpos_type";
    public static final String SC_EFTPOS_AMOUNT = "Sc_eftpos_amount";
    public static final String SC_MASTERCARD_CREDIT_ON = "Sc_mastercard_credit_on";
    public static final String SC_MASTERCARD_CREDIT_TYPE = "Sc_mastercard_credit_type";
    public static final String SC_MASTERCARD_CREDIT_AMOUNT = "Sc_mastercard_credit_amount";
    public static final String SC_MASTERCARD_DEBIT_ON = "Sc_mastercard_debit_on";
    public static final String SC_MASTERCARD_DEBIT_TYPE = "Sc_mastercard_debit_type";
    public static final String SC_MASTERCARD_DEBIT_AMOUNT = "Sc_mastercard_debit_amount";
    public static final String SC_VISA_CREDIT_ON = "Sc_visa_credit_on";
    public static final String SC_VISA_CREDIT_TYPE = "Sc_visa_credit_type";
    public static final String SC_VISA_CREDIT_AMOUNT = "Sc_visa_credit_amount";
    public static final String SC_VISA_DEBIT_ON = "Sc_visa_debit_on";
    public static final String SC_VISA_DEBIT_TYPE = "Sc_visa_debit_type";
    public static final String SC_VISA_DEBIT_AMOUNT = "Sc_visa_debit_amount";
    public static final String SC_AMEX_ON = "Sc_amex_on";
    public static final String SC_AMEX_TYPE = "Sc_amex_type";
    public static final String SC_AMEX_AMOUNT = "Sc_amex_amount";
    public static final String SC_DINERS_CLUB_ON = "Sc_diners_club_on";
    public static final String SC_DINERS_CLUB_TYPE = "Sc_diners_club_type";
    public static final String SC_DINERS_CLUB_AMOUNT = "Sc_diners_club_amount";
    public static final String SC_JCB_ON = "Sc_jcb_on";
    public static final String SC_JCB_TYPE = "Sc_jcb_type";
    public static final String SC_JCB_AMOUNT = "Sc_jcb_amount";
    public static final String SC_UNIONPAY_CREDIT_ON = "Sc_unionpay_credit_on";
    public static final String SC_UNIONPAY_CREDIT_TYPE = "Sc_unionpay_credit_type";
    public static final String SC_UNIONPAY_CREDIT_AMOUNT = "Sc_unionpay_credit_amount";

    // ------------------- Override Params Value Id's -------------------------------
    public static final String CUSTOMER_NAME = "CustomerName";
    public static final String CURRENCY_CODE = "CurrencyCode";
    public static final String CURRENCY_NUM = "CurrencyNum";
    public static final String COUNTRY_NUM = "CountryNum";
    public static final String DCC_SUPPORTED = "DccSupported";
    public static final String DEFERRED_AUTH_ENABLED = "DeferredAuthEnabled";
    public static final String MAX_DEFERRED_AUTH_COUNT = "MaxDeferredAuthCount";
    public static final String MAX_DEFERRED_AUTH_VALUE = "MaxDeferredAuthValue";
    public static final String LANGUAGE = "Language";
    public static final String HOST = "Host";
    public static final String HOST_2_ND = "Host2nd";
    public static final String NII = "Nii";
    public static final String AIIC = "Aiic";
    public static final String RECEIVE_TIMEOUT = "ReceiveTimeout";
    public static final String DIAL_TIMEOUT = "DialTimeout";
    public static final String COMMS_TYPE = "CommsType";
    public static final String IP_GATEWAY_HOST = "IpGatewayHost";
    public static final String IP_GATEWAY_USER = "IpGatewayUser";
    public static final String IP_GATEWAY_PWD = "IpGatewayPwd";
    public static final String DEFAULT_PKT_VERSION = "DefaultPktVersion";
    public static final String DEFAULT_EPAT_VERSION = "DefaultEpatVersion";
    public static final String DEFAULT_SPOT_VERSION = "DefaultSpotVersion";
    public static final String DEFAULT_FCAT_VERSION = "DefaultFcatVersion";
    public static final String POS_COMMS_ENABLED = "PosCommsEnabled";
    public static final String POS_COMMS_HOST_ID = "PosCommsHostId";
    public static final String POS_COMMS_INTERFACE_TYPE = "PosCommsInterfaceType";
    public static final String BRAND_DISPLAY_STATUS_BAR_COLOUR = "BrandDisplayStatusBarColour";
    public static final String BRAND_DISPLAY_BUTTON_COLOUR = "BrandDisplayButtonColour";
    public static final String BRAND_DISPLAY_BUTTON_TEXT_COLOUR = "BrandDisplayButtonTextColour";
    public static final String BRAND_DISPLAY_PRIMARY_COLOUR = "BrandDisplayPrimaryColour";
    public static final String BRAND_DISPLAY_LOGO_HEADER = "BrandDisplayLogoHeader";
    public static final String BRANDING_FILES_FOLDER = "brandingFiles/";
    public static final String BRAND_DISPLAY_LOGO_IDLE = "BrandDisplayLogoIdle";
    public static final String BRAND_DISPLAY_LOGO_SPLASH = "BrandDisplayLogoSplash";
    public static final String BRAND_RECEIPT_LOGO_HEADER = "BrandReceiptLogoHeader";
    public static final String CLIENT_AUTH = "ClientAuth";
    public static final String CERTIFICATE_FILE = "CertificateFile";
    public static final String PRIVATE_KEY_FILE = "PrivateKeyFile";
    public static final String PRIVATE_KEY_PASSWORD = "PrivateKeyPassword";
    public static final String PRIVATE_KEY_CERTIFICATE = "PrivateKeyCertificate";
    public static final String USE_SSL = "UseSsl";
    public static final String DISABLE_SECURITY = "DisableSecurity";
    public static final String AMEX_SELLER_ID = "AmexSellerId";
    public static final String AMEX_SELLER_EMAIL = "AmexSellerEmail";
    public static final String AMEX_SELLER_TELEPHONE = "AmexSellerTelephone";
    public static final String AMEX_PAYMENT_FACILITATOR = "AmexPaymentFacilitator";
    public static final String MAIL_MERCHANT = "MailMerchant";
    public static final String MAIL_MERCHANT_ADDRESS = "MailMerchantAddress";
    public static final String MAIL_HOST = "MailHost";
    public static final String MAIL_PORT = "MailPort";
    public static final String MAIL_USER = "MailUser";
    public static final String MAIL_PASSWORD = "MailPassword";
    public static final String MAIL_SENDER = "MailSender";
    public static final String MAIL_STORE_NAME = "MailStoreName";
    public static final String PAXSTORE_UPLOAD = "PaxstoreUpload";
    public static final String OFFLINE_TRANSACTION_CEILING_LIMIT_CENTS_CONTACT = "OfflineTransactionCeilingLimitCentsContact";
    public static final String OFFLINE_TRANSACTION_CEILING_LIMIT_CENTS_CONTACTLESS = "OfflineTransactionCeilingLimitCentsContactless";
    public static final String OFFLINE_FLIGHT_MODE_ALLOWED = "OfflineFlightModeAllowed";
    public static final String OFFLINE_SOFT_LIMIT_AMOUNT_CENTS = "OfflineSoftLimitAmountCents";
    public static final String OFFLINE_SOFT_LIMIT_COUNT = "OfflineSoftLimitCount";
    public static final String OFFLINE_UPPER_LIMIT_AMOUNT_CENTS = "OfflineUpperLimitAmountCents";
    public static final String OFFLINE_UPPER_LIMIT_COUNT = "OfflineUpperLimitCount";
    public static final String UNATTENDED_MODE_ALLOWED = "UnattendedModeAllowed";
    public static final String BANK_DESCRIPTION = "BankDescription";
    public static final String RETAILER_NAME = "RetailerName";
    public static final String COMMS_FALLBACK_ENABLED = "CommsFallbackEnabled";
    public static final String COMMS_FALLBACK_HOST = "CommsFallbackHost";
    public static final String PASSCODE_SECURITY_SUPPORTED = "PasscodeSecuritySupported";
    public static final String LOGIN_MANAGER_USER_ID = "LoginManagerUserId";
    public static final String LOGIN_MANAGER_INITIAL_PWD = "LoginManagerInitialPwd";
    public static final String LOGIN_MANAGER_USER_NAME = "LoginManagerUserName";
    public static final String LOGIN_TECHNICIAN_USER_ID = "LoginTechnicianUserId";
    public static final String LOGIN_TECHNICIAN_INITIAL_PWD = "LoginTechnicianInitialPwd";
    public static final String LOGIN_TECHNICIAN_USER_NAME = "LoginTechnicianUserName";
    public static final String PRE_AUTH_EXPIRY_DEFAULT = "PreAuthExpiry_default";
    public static final String PREAUTH_EXPIRY_EFTPOS = "PreauthExpiry_eftpos";
    public static final String PREAUTH_EXPIRY_MASTERCARD_CREDIT = "PreauthExpiry_mastercard_credit";
    public static final String PREAUTH_EXPIRY_MASTERCARD_DEBIT = "PreauthExpiry_mastercard_debit";
    public static final String PREAUTH_EXPIRY_VISA_CREDIT = "PreauthExpiry_visa_credit";
    public static final String PREAUTH_EXPIRY_VISA_DEBIT = "PreauthExpiry_visa_debit";
    public static final String PREAUTH_EXPIRY_AMEX = "PreauthExpiry_amex";
    public static final String PREAUTH_EXPIRY_DINERS_CLUB = "PreauthExpiry_diners_club";
    public static final String PREAUTH_EXPIRY_JCB = "PreauthExpiry_jcb";
    public static final String PREAUTH_EXPIRY_UNIONPAY_CREDIT = "PreauthExpiry_unionpay_credit";
    public static final String SCREEN_LOCK_TIME = "ScreenLockTime";
    public static final String MOTO_REFUND_PASSWORD = "MotoRefundPassword";
    public static final String REFUND_PASSWORD = "RefundPassword";
    public static final String PCI_REBOOT_TIME = "PciRebootTime";
    public static final String AUTO_SETTLEMENT_ENABLED = "AutoSettlementEnabled";
    public static final String AUTO_SETTLEMENT_RETRY_COUNT = "AutoSettlementRetryCount";
    public static final String AUTO_SETTLEMENT_PRINT_TRANSACTION_LISTING = "AutoSettlementPrintTransactionListing";
    public static final String AUTO_SETTLEMENT_TIME = "AutoSettlementTime";
    public static final String AUTO_SETTLEMENT_TIME_WINDOW = "AutoSettlementTimeWindow";
    public static final String AUTO_SETTLEMENT_IDLING_PERIOD = "AutoSettlementIdlingPeriod";
    public static final String MAX_DAYS_TRANSACTIONS_TO_STORE = "MaxDaysTransactionsToStore";
    public static final String BIN_EFTPOS = "Bin_eftpos";
    public static final String BIN_MASTERCARD_CREDIT = "Bin_mastercard_credit";
    public static final String BIN_MASTERCARD_DEBIT = "Bin_mastercard_debit";
    public static final String BIN_VISA_CREDIT = "Bin_visa_credit";
    public static final String BIN_VISA_DEBIT = "Bin_visa_debit";
    public static final String BIN_AMEX = "Bin_amex";
    public static final String BIN_DINERS_CLUB = "Bin_diners_club";
    public static final String BIN_JCB = "Bin_jcb";
    public static final String BIN_UNIONPAY_CREDIT = "Bin_unionpay_credit";
    public static final String CDO_ALLOWED_EFTPOS = "CdoAllowedEftpos";
    public static final String CDO_ALLOWED_MASTERCARD_CREDIT = "CdoAllowedMastercardCredit";
    public static final String CDO_ALLOWED_MASTERCARD_DEBIT = "CdoAllowedMastercardDebit";
    public static final String CDO_ALLOWED_VISA_CREDIT = "CdoAllowedVisaCredit";
    public static final String CDO_ALLOWED_VISA_DEBIT = "CdoAllowedVisaDebit";
    public static final String CDO_ALLOWED_AMEX = "CdoAllowedAmex";
    public static final String CDO_ALLOWED_DINERS_CLUB = "CdoAllowedDinersClub";
    public static final String CDO_ALLOWED_JCB = "CdoAllowedJcb";
    public static final String CDO_ALLOWED_UNIONPAY_CREDIT = "CdoAllowedUnionpayCredit";
    public static final String CARD_PRODUCT_FILE = "CardProductFile";
    public static final String CFG_EMV_FILE = "CfgEmvFile";
    public static final String CFG_CTLS_FILE = "CfgCtlsFile";
    public static final String BLACKLIST_FILE = "BlacklistFile";
    public static final String EPAT_FILE = "EpatFile";
    public static final String PKT_FILE = "PktFile";
    public static final String CARDS_FILE = "CardsFile";
    public static final String PASSWORD_RETRY_LIMIT = "PasswordRetryLimit";
    public static final String PASSWORD_ATTEMPT_WINDOW = "PasswordAttemptWindow";
    public static final String PASSWORD_ATTEMPT_LOCKOUT_DURATION = "PasswordAttemptLockoutDuration";
    public static final String PASSWORD_MAXIMUM_AGE = "PasswordMaximumAge";
    public static final String PASSWORD_RETRY_LIMIT1 = "RefundPasswordRetryLimit";
    public static final String PASSWORD_RETRY_LIMIT2 = "MotoPasswordRetryLimit";
    public static final String RRN_RETRY_LIMIT = "RrnRetryLimit";
    public static final String AUTHCODE_RETRY_LIMIT = "AuthcodeRetryLimit";
    public static final String CARD_PRODUCT_VERSION = "CardProductVersion";
    public static final String AUTO_USER_LOGIN = "AutoUserLogin";
    public static final String USE_PERCENTAGE_TIP = "UsePercentageTip";
    public static final String INCLUDED_ORGINAL_STAND_IN_REC = "IncludedOrginalStandInRec";
    public static final String REVERSAL_COPY_ORIGINAL = "ReversalCopyOriginal";
    public static final String OVERRIDE_LAST_MODIFIED = "overrideLastModified";
    public static final String HOTLOAD_LAST_MODIFIED = "hotloadLastModified";
    public static final String INITIAL_LAST_MODIFIED = "initialLastModified";

    //----------------------------------- Cards ID------------------------------------
    public static final String CARDS = "Cards";
    //--------------------------------------------------------------------------------
    private static final String ACCESS_MODE_SUFFIX = "_AccessMode"; // Helps differentiate between normal and AccessMode for timeout lookup

    SharedPreferences overrideParamsPref;

    SharedPreferences initialParamsPref;

    SharedPreferences hotloadParamsPref;



    public PayCfgImpl(Context context) {
        overrideParamsPref = context.getSharedPreferences("overrideparams", Context.MODE_PRIVATE);
        initialParamsPref = context.getSharedPreferences("initialparams", Context.MODE_PRIVATE);
        hotloadParamsPref = context.getSharedPreferences("hotloadparams", Context.MODE_PRIVATE);
    }

    private static int getIntValue(String input, int defaultValue) {
        int ret = defaultValue;
        // Check if it is a number only
        if (input != null && input.matches("\\d+")) {
            ret = Integer.parseInt(input);
        }
        return ret;
    }

    private static boolean convertBoolean(String s) {
        if (Util.isNullOrWhitespace(s))
            return false;
        return s.toLowerCase().contains("true") || s.toLowerCase().contains("y");
    }

    private int convertSecToMilliSecs( String timeSecs , String maxTimeSecs) {
        final int SEC_TO_MILLI_SEC = 1000;
        if ( !Util.isNullOrEmpty( timeSecs ) && Util.isNumericString( timeSecs ) ) {
            return Integer.parseInt( timeSecs ) * SEC_TO_MILLI_SEC;
        }
        if(Integer.parseInt(timeSecs) > Integer.parseInt(maxTimeSecs)) {
            return Integer.parseInt(maxTimeSecs) * SEC_TO_MILLI_SEC;
        }

        throw new IllegalArgumentException( "Time format is wrong" );
    }

    // Removes duplicated code
    private void storeBrandingFileConfig(SharedPreferences.Editor editor, String id, String filename) {
        if (!Util.isNullOrEmpty(filename)) {
            editor.putString(id, BRANDING_FILES_FOLDER + filename);
        } else {
            editor.putString(id, "");
        }
    }

    private void processBrandingParams(OverrideParameters params, SharedPreferences.Editor editor) {
        editor.putString(BRAND_DISPLAY_STATUS_BAR_COLOUR, params.getBrandDisplayStatusBarColour());
        editor.putString(BRAND_DISPLAY_BUTTON_COLOUR, params.getBrandDisplayButtonColour());
        editor.putString(BRAND_DISPLAY_BUTTON_TEXT_COLOUR, params.getBrandDisplayButtonTextColour());
        editor.putString(BRAND_DISPLAY_PRIMARY_COLOUR, params.getBrandDisplayPrimaryColour());

        storeBrandingFileConfig(editor, BRAND_DISPLAY_LOGO_HEADER, params.getBrandDisplayLogoHeader());
        storeBrandingFileConfig(editor, BRAND_DISPLAY_LOGO_IDLE, params.getBrandDisplayLogoIdle());
        storeBrandingFileConfig(editor, BRAND_DISPLAY_LOGO_SPLASH, params.getBrandDisplayLogoSplash());
        storeBrandingFileConfig(editor, BRAND_RECEIPT_LOGO_HEADER, params.getBrandReceiptLogoHeader());
    }

    public boolean loadInitialParams(InitialParameters initialParameters, long lastModified) {
        SharedPreferences.Editor editor = initialParamsPref.edit();

        editor.clear(); // Clear out EVERYTHING. We don't want old values remaining
        if (initialParameters != null) {
            editor.putString(STID, initialParameters.getStid());
            editor.putString(MID, initialParameters.getMid());
            editor.putString(TKEY, initialParameters.getTkey());
            editor.putString(ACCESS_TOKEN, initialParameters.getAccess_token());
            editor.putString(ACCESS_TOKEN_2, initialParameters.getAccess_token2());
            editor.putString(ACQUIRER_INSTITUTION_ID, initialParameters.getAcquirerInstitutionId());
            editor.putString(BANK_TIME_ZONE, initialParameters.getBankTimeZone());
            editor.putString(TERMINAL_TIME_ZONE, initialParameters.getTerminalTimeZone());
        }

        if (!Util.isNullOrEmpty(IdentityEnvVar.getMid())) {
            editor.putString(MID, IdentityEnvVar.getMid());
        }

        if (!Util.isNullOrEmpty(IdentityEnvVar.getTid())) {
            editor.putString(STID, IdentityEnvVar.getTid());
        }
        editor.putLong(INITIAL_LAST_MODIFIED, lastModified);
        editor.apply();
        return true;
    }

    public boolean loadHotloadParams(HotLoadParameters hotloadParameters, long lastModified) {
        if (hotloadParameters != null) {

            SharedPreferences.Editor editor = hotloadParamsPref.edit();

            editor.clear(); // Clear out EVERYTHING. We don't want old values remaining
            // Receipt lines
            editor.putString(RECEIPT_MERCHANT_LINE_0, hotloadParameters.getReceipt_merchant_line0());
            editor.putString(RECEIPT_MERCHANT_LINE_1, hotloadParameters.getReceipt_merchant_line1());
            editor.putString(RECEIPT_MERCHANT_LINE_2, hotloadParameters.getReceipt_merchant_line2());
            editor.putString(RECEIPT_MERCHANT_LINE_3, hotloadParameters.getReceipt_merchant_line3());
            editor.putString(RECEIPT_MERCHANT_LINE_4, hotloadParameters.getReceipt_merchant_line4());
            editor.putString(RECEIPT_MERCHANT_LINE_5, hotloadParameters.getReceipt_merchant_line5());
            editor.putString(RECEIPT_MERCHANT_LINE_6, hotloadParameters.getReceipt_merchant_line6());
            editor.putString(FOOTER_LINE_1, hotloadParameters.getFooterLine1());
            editor.putString(FOOTER_LINE_2, hotloadParameters.getFooterLine2());


            // MCR Settings
            editor.putBoolean(MCR_ENABLED, convertBoolean(hotloadParameters.getMcrEnabled()));
            editor.putInt(MCR_LIMIT, getIntValue(hotloadParameters.getMcrLimit(),0));
            editor.putInt(MCR_UPPER_LIMIT, getIntValue(hotloadParameters.getMcrUpperLimit(),0));

            // Terminal limits
            editor.putInt(SALE_LIMIT_CENTS, getIntValue(hotloadParameters.getSaleLimitCents(), 0));
            editor.putInt(CASHOUT_LIMIT_CENTS, getIntValue(hotloadParameters.getCashoutLimitCents(), 0));
            editor.putString(MAX_REFUND_LIMIT, hotloadParameters.getMaxRefundLimit());
            editor.putString(MANAGER_REFUND_LIMIT, hotloadParameters.getManagerRefundLimit());
            editor.putString(MAX_REFUND_COUNT, hotloadParameters.getMaxRefundCount());
            editor.putString(MAX_CUMULATIVE_REFUND_LIMIT, hotloadParameters.getMaxCumulativeRefundLimit());
            editor.putString(MAX_TIP_PERCENT, hotloadParameters.getMaxTipPercent());
            editor.putString(MAX_PRE_AUTH_TRANS, hotloadParameters.getMaxPreAuthTrans());
            editor.putString(MAX_EFB_TRANS, hotloadParameters.getMaxEfbTrans());
            editor.putInt(PRE_AUTH_LIMIT_CENTS, getIntValue(hotloadParameters.getPreAuthLimitCents(), 0));
            editor.putString(OVERRIDE_CTLS_CVM_LIMIT_ENABLED, hotloadParameters.getOverrideCtlsCvmLimitEnabled());
            editor.putString(OVERRIDE_CTLS_CVM_LIMIT, hotloadParameters.getOverrideCtlsCvmLimit());

            // Terminal configuration
            editor.putBoolean(SHOW_RECEIPT_PROMPT_FOR_AUTO, convertBoolean(hotloadParameters.getShowReceiptPromptForAuto()));
            editor.putString(PRINT_CUSTOMER_RECEIPT,  Util.isNullOrEmpty(hotloadParameters.getPrintCustomerReceipt()) ? "ASK" : hotloadParameters.getPrintCustomerReceipt());
            editor.putBoolean(MOTO_PASSWORD_PROMPT, convertBoolean(hotloadParameters.getMotoPasswordPrompt()));
            editor.putBoolean(REFUND_PASSWORD_PROMPT, convertBoolean(hotloadParameters.getRefundPasswordPrompt()));
            editor.putBoolean(MOTO_CVV_ENTRY, convertBoolean(hotloadParameters.getMotoCVVEntry()));
            editor.putBoolean(MOTO_CVV_ENTRY_BYPASS_ALLOWED, convertBoolean(hotloadParameters.getMotoCVVEntryBypassAllowed()));
            editor.putBoolean(MAIL_ORDER, convertBoolean(hotloadParameters.getMailOrder()));
            editor.putBoolean(TELEPHONE, convertBoolean(hotloadParameters.getTelephone()));
            editor.putBoolean(PREAUTH, convertBoolean(hotloadParameters.getPreauth()));
            editor.putBoolean(PREAUTH_CREDIT_ACCOUNT_ONLY, convertBoolean(hotloadParameters.getPreauthCreditAccountOnly()));
            editor.putBoolean(CASHBACK, convertBoolean(hotloadParameters.getCashback()));
            editor.putBoolean(CASHOUT, convertBoolean(hotloadParameters.getCashout()));
            editor.putBoolean(REFUND, convertBoolean(hotloadParameters.getRefund()));
            editor.putBoolean(REVERSAL, convertBoolean(hotloadParameters.getReversal()));
            editor.putBoolean(TIP_ALLOWED, convertBoolean(hotloadParameters.getTipAllowed()));

            String custRef = hotloadParameters.getCustRefRequired();
            if (Util.isNullOrEmpty(custRef))
                editor.putInt(CUST_REF_REQUIRED, DISABLED.referenceCode);
            else
                editor.putInt(CUST_REF_REQUIRED, (custRef.compareToIgnoreCase("OPTIONAL") == 0) ? OPTIONAL.referenceCode : (custRef.compareToIgnoreCase("MANDATORY") == 0) ? MANDATORY.referenceCode : DISABLED.referenceCode);

            editor.putBoolean(EMV_SUPPORTED, convertBoolean(hotloadParameters.getEmvSupported()));
            editor.putBoolean(CARDHOLDER_PRESENT, convertBoolean(hotloadParameters.getCardholderPresent()));
            editor.putBoolean(CONTACTLESS_SUPPORTED, convertBoolean(hotloadParameters.getContactlessSupported()));
            editor.putBoolean(LOYALTY_SUPPORTED, convertBoolean(hotloadParameters.getLoyaltySupported()));
            // Missing version never here but never used....
            editor.putBoolean(EFB_SUPPORTED, convertBoolean(hotloadParameters.getEfbSupported()));
            editor.putBoolean(EFB_ACKNOWLEDGE_SERVICE_CODE, convertBoolean(hotloadParameters.getEfbAcknowledgeServiceCode()));
            editor.putInt(EFB_PLASTIC_CARD_LIFE_DAYS, getIntValue(hotloadParameters.getEfbPlasticCardLifeDays(),0));
            editor.putBoolean(EFB_REFUND_ALLOWED, convertBoolean(hotloadParameters.getEfbRefundAllowed()));
            editor.putBoolean(EFB_CASHOUT_ALLOWED, convertBoolean(hotloadParameters.getEfbCashoutAllowed()));
            editor.putInt(EFB_CONTINUE_IN_FALLBACK_TIMEOUT_MINUTES, getIntValue(hotloadParameters.getEfbContinueInFallbackTimeoutMinutes(),0));
            editor.putBoolean(EFB_AUTH_NUMBER_OVER_FLOOR_LIMIT_ALLOWED, convertBoolean(hotloadParameters.getEfbAuthNumberOverFloorLimitAllowed()));
            editor.putBoolean(ACCESS_MODE, convertBoolean(hotloadParameters.getAccessMode()));
            editor.putBoolean(EMV_FALLBACK, convertBoolean(hotloadParameters.getEmvFallback()));
            editor.putBoolean(MSR_ALLOWED, convertBoolean(hotloadParameters.getMsrAllowed()));
            editor.putBoolean(REFUND_SECURE, convertBoolean(hotloadParameters.getRefundSecure()));
            editor.putBoolean(SIGNATURE_SUPPORTED, convertBoolean(hotloadParameters.getSignatureSupported()));
            editor.putBoolean(USE_CUSTOM_AUDIO_FOR_RESULT, convertBoolean(hotloadParameters.getUseCustomAudioForResult()));

            // Surcharge values
            editor.putBoolean(SURCHARGE_SUPPORTED, convertBoolean(hotloadParameters.getSurchargeSupported()));
            editor.putBoolean(SC_EFTPOS_ON, convertBoolean(hotloadParameters.getSc_eftpos_on()));
            editor.putString(SC_EFTPOS_TYPE, hotloadParameters.getSc_eftpos_type());
            editor.putString(SC_EFTPOS_AMOUNT, hotloadParameters.getSc_eftpos_amount());
            editor.putBoolean(SC_MASTERCARD_CREDIT_ON, convertBoolean(hotloadParameters.getSc_mastercard_credit_on()));
            editor.putString(SC_MASTERCARD_CREDIT_TYPE, hotloadParameters.getSc_mastercard_credit_type());
            editor.putString(SC_MASTERCARD_CREDIT_AMOUNT, hotloadParameters.getSc_mastercard_credit_amount());
            editor.putBoolean(SC_MASTERCARD_DEBIT_ON, convertBoolean(hotloadParameters.getSc_mastercard_debit_on()));
            editor.putString(SC_MASTERCARD_DEBIT_TYPE, hotloadParameters.getSc_mastercard_debit_type());
            editor.putString(SC_MASTERCARD_DEBIT_AMOUNT, hotloadParameters.getSc_mastercard_debit_amount());
            editor.putBoolean(SC_VISA_CREDIT_ON, convertBoolean(hotloadParameters.getSc_visa_credit_on()));
            editor.putString(SC_VISA_CREDIT_TYPE, hotloadParameters.getSc_visa_credit_type());
            editor.putString(SC_VISA_CREDIT_AMOUNT, hotloadParameters.getSc_visa_credit_amount());
            editor.putBoolean(SC_VISA_DEBIT_ON, convertBoolean(hotloadParameters.getSc_visa_debit_on()));
            editor.putString(SC_VISA_DEBIT_TYPE, hotloadParameters.getSc_visa_debit_type());
            editor.putString(SC_VISA_DEBIT_AMOUNT, hotloadParameters.getSc_visa_debit_amount());
            editor.putBoolean(SC_AMEX_ON, convertBoolean(hotloadParameters.getSc_amex_on()));
            editor.putString(SC_AMEX_TYPE, hotloadParameters.getSc_amex_type());
            editor.putString(SC_AMEX_AMOUNT, hotloadParameters.getSc_amex_amount());
            editor.putBoolean(SC_DINERS_CLUB_ON, convertBoolean(hotloadParameters.getSc_diners_club_on()));
            editor.putString(SC_DINERS_CLUB_TYPE, hotloadParameters.getSc_diners_club_type());
            editor.putString(SC_DINERS_CLUB_AMOUNT, hotloadParameters.getSc_diners_club_amount());
            editor.putBoolean(SC_JCB_ON, convertBoolean(hotloadParameters.getSc_jcb_on()));
            editor.putString(SC_JCB_TYPE, hotloadParameters.getSc_jcb_type());
            editor.putString(SC_JCB_AMOUNT, hotloadParameters.getSc_jcb_amount());
            editor.putBoolean(SC_UNIONPAY_CREDIT_ON, convertBoolean(hotloadParameters.getSc_unionpay_credit_on()));
            editor.putString(SC_UNIONPAY_CREDIT_TYPE, hotloadParameters.getSc_unionpay_credit_type());
            editor.putString(SC_UNIONPAY_CREDIT_AMOUNT, hotloadParameters.getSc_unionpay_credit_amount());
            // Store the last modified value to be referenced when reloading the files.
            editor.putLong(HOTLOAD_LAST_MODIFIED, lastModified);
            editor.apply();
            Timber.e("applied hotload params");
            return true;
        } else {
            Timber.e("parsing of hotload params failed");
            return false;
        }
    }

    private boolean addIssuerToConfig(SharedPreferences.Editor editor, int index, String name, String enabled, String deferredAuthEnabled) {
        String nameId = "Issuer" + index + "Name";
        String enabledId = "Issuer" + index + "Enabled";
        String deferredAuthEnabledId = "Issuer" + index + DEFERRED_AUTH_ENABLED;

        if(name != null && !name.isEmpty()) {
            editor.putString(nameId, name);
            editor.putBoolean(enabledId, convertBoolean(enabled));
            editor.putBoolean(deferredAuthEnabledId, convertBoolean(deferredAuthEnabled));
            return true;
        }

        return false;
    }


    public boolean loadOverrideParams(String customerName, OverrideParameters overrideParamsDetails, long lastModified) {
        SharedPreferences.Editor editor = overrideParamsPref.edit();

        if (overrideParamsPref != null) {
            editor.clear(); // Clear out EVERYTHING. We don't want old values remaining
            editor.putString(CUSTOMER_NAME, customerName);
            editor.putString(CURRENCY_CODE, overrideParamsDetails.getCurrencyCode());
            editor.putInt(CURRENCY_NUM, getIntValue(overrideParamsDetails.getCurrencyNum(),36));
            editor.putInt(COUNTRY_NUM, getIntValue(overrideParamsDetails.getCountryNum(),36));

            editor.putBoolean(DCC_SUPPORTED, convertBoolean(overrideParamsDetails.getDccSupported()));
            editor.putBoolean(DEFERRED_AUTH_ENABLED,convertBoolean(overrideParamsDetails.getDeferredAuthEnabled()));
            editor.putInt(MAX_DEFERRED_AUTH_COUNT, getIntValue(overrideParamsDetails.getMaxDeferredAuthCount(),0));
            editor.putInt(MAX_DEFERRED_AUTH_VALUE, getIntValue(overrideParamsDetails.getMaxDeferredAuthValue(),0));
            editor.putString(LANGUAGE, overrideParamsDetails.getLanguage());

            editor.putString(HOST, overrideParamsDetails.getPaymentSwitch_ip_host());
            editor.putString(HOST_2_ND,overrideParamsDetails.getPaymentSwitch_ip_host2nd());
            editor.putString(NII, overrideParamsDetails.getPaymentSwitch_nii());
            editor.putString(AIIC, overrideParamsDetails.getPaymentSwitch_aiic());
            editor.putInt(RECEIVE_TIMEOUT, getIntValue(overrideParamsDetails.getPaymentSwitch_receiveTimeout(),0));
            editor.putInt(DIAL_TIMEOUT, getIntValue(overrideParamsDetails.getPaymentSwitch_dialTimeout(),0));
            editor.putString(COMMS_TYPE, overrideParamsDetails.getPaymentSwitch_commsType());
            editor.putString(IP_GATEWAY_HOST, overrideParamsDetails.getPaymentSwitch_ipGatewayHost());
            editor.putString(IP_GATEWAY_USER, overrideParamsDetails.getPaymentSwitch_ipGatewayUser());
            editor.putString(IP_GATEWAY_PWD, overrideParamsDetails.getPaymentSwitch_ipGatewayPwd());

            editor.putString(DEFAULT_PKT_VERSION, overrideParamsDetails.getPaymentSwitch_defaultPktVersion());
            editor.putString(DEFAULT_EPAT_VERSION, overrideParamsDetails.getPaymentSwitch_defaultEpatVersion());
            editor.putString(DEFAULT_SPOT_VERSION, overrideParamsDetails.getPaymentSwitch_defaultSpotVersion());
            editor.putString(DEFAULT_FCAT_VERSION, overrideParamsDetails.getPaymentSwitch_defaultFcatVersion());

            editor.putBoolean(POS_COMMS_ENABLED, convertBoolean(overrideParamsDetails.getPosCommsEnabled()));
            editor.putString(POS_COMMS_HOST_ID, overrideParamsDetails.getPosCommsHostId());
            editor.putString(POS_COMMS_INTERFACE_TYPE, overrideParamsDetails.getPosCommsInterfaceType());

            processBrandingParams(overrideParamsDetails, editor);

            editor.putBoolean(CLIENT_AUTH, !Util.isNullOrWhitespace(overrideParamsDetails.getPaymentSwitch_privateKeyFile()) &&
                    !Util.isNullOrWhitespace(overrideParamsDetails.getPaymentSwitch_privateKeyCertificate()));
            editor.putString(CERTIFICATE_FILE, overrideParamsDetails.getPaymentSwitch_certificateFile());
            editor.putString(PRIVATE_KEY_FILE, overrideParamsDetails.getPaymentSwitch_privateKeyFile());
            editor.putString(PRIVATE_KEY_PASSWORD, overrideParamsDetails.getPaymentSwitch_privateKeyPassword());
            editor.putString(PRIVATE_KEY_CERTIFICATE, overrideParamsDetails.getPaymentSwitch_privateKeyCertificate());
            editor.putBoolean(USE_SSL, convertBoolean(overrideParamsDetails.getPaymentSwitch_useSsl()));
            editor.putBoolean(DISABLE_SECURITY, convertBoolean(overrideParamsDetails.getPaymentSwitch_disableSecurity()));

            editor.putString(AMEX_SELLER_ID, overrideParamsDetails.getSeller_id());
            editor.putString(AMEX_SELLER_EMAIL, overrideParamsDetails.getSeller_email());
            editor.putString(AMEX_SELLER_TELEPHONE, overrideParamsDetails.getSeller_telephone());
            editor.putString(AMEX_PAYMENT_FACILITATOR, overrideParamsDetails.getPayment_facilitator());

            editor.putBoolean(MAIL_MERCHANT, convertBoolean(overrideParamsDetails.getMailMerchant()));
            editor.putString(MAIL_MERCHANT_ADDRESS, overrideParamsDetails.getMailMerchantAddress());

            editor.putString(MAIL_HOST, overrideParamsDetails.getMailHost());
            editor.putString(MAIL_PORT, overrideParamsDetails.getMailPort());
            editor.putString(MAIL_USER, overrideParamsDetails.getMailUser());
            editor.putString(MAIL_PASSWORD, overrideParamsDetails.getMailPassword());
            editor.putString(MAIL_SENDER, overrideParamsDetails.getMailSender());
            editor.putString(MAIL_STORE_NAME, overrideParamsDetails.getMailStoreName());

            editor.putBoolean(PAXSTORE_UPLOAD, convertBoolean(overrideParamsDetails.getPaxstoreUpload()));

            editor.putInt(OFFLINE_TRANSACTION_CEILING_LIMIT_CENTS_CONTACT, getIntValue(overrideParamsDetails.getOfflineTransactionCeilingLimitCentsContact(), 0));
            editor.putInt(OFFLINE_TRANSACTION_CEILING_LIMIT_CENTS_CONTACTLESS, getIntValue(overrideParamsDetails.getOfflineTransactionCeilingLimitCentsContactless(), 0));

            editor.putBoolean(OFFLINE_FLIGHT_MODE_ALLOWED, convertBoolean(overrideParamsDetails.getOfflineFlightModeAllowed()));
            editor.putInt(OFFLINE_SOFT_LIMIT_AMOUNT_CENTS, getIntValue(overrideParamsDetails.getOfflineSoftLimitAmountCents(), 0));
            editor.putInt(OFFLINE_SOFT_LIMIT_COUNT, getIntValue(overrideParamsDetails.getOfflineSoftLimitCount(),0));
            editor.putInt(OFFLINE_UPPER_LIMIT_AMOUNT_CENTS, getIntValue(overrideParamsDetails.getOfflineUpperLimitAmountCents(), 0));
            editor.putInt(OFFLINE_UPPER_LIMIT_COUNT, getIntValue(overrideParamsDetails.getOfflineUpperLimitCount(),0));

            editor.putBoolean(UNATTENDED_MODE_ALLOWED, convertBoolean(overrideParamsDetails.getUnattendedModeAllowed()));

            editor.putString(BANK_DESCRIPTION, overrideParamsDetails.getBankDescription()); // Details about the bank

            editor.putString(RETAILER_NAME, overrideParamsDetails.getRetailerName());

            editor.putBoolean(COMMS_FALLBACK_ENABLED, convertBoolean(overrideParamsDetails.getCommsFallbackEnabled()));
            editor.putString(COMMS_FALLBACK_HOST, overrideParamsDetails.getCommsFallbackHost());

            editor.putBoolean(PASSCODE_SECURITY_SUPPORTED, convertBoolean(overrideParamsDetails.getPasscodeSecuritySupported()));

            editor.putString(LOGIN_MANAGER_USER_ID, overrideParamsDetails.getLoginManagerUserId());
            editor.putString(LOGIN_MANAGER_INITIAL_PWD, overrideParamsDetails.getLoginManagerInitialPwd());
            editor.putString(LOGIN_MANAGER_USER_NAME, overrideParamsDetails.getLoginManagerUserName());
            editor.putString(LOGIN_TECHNICIAN_USER_ID, overrideParamsDetails.getLoginTechnicianUserId());
            editor.putString(LOGIN_TECHNICIAN_INITIAL_PWD, overrideParamsDetails.getLoginTechnicianInitialPwd());
            editor.putString(LOGIN_TECHNICIAN_USER_NAME, overrideParamsDetails.getLoginTechnicianUserName());

            editor.putString(PRE_AUTH_EXPIRY_DEFAULT, overrideParamsDetails.getPreauthExpiry_default());
            editor.putString(PREAUTH_EXPIRY_EFTPOS, overrideParamsDetails.getPreauthExpiry_eftpos());
            editor.putString(PREAUTH_EXPIRY_MASTERCARD_CREDIT, overrideParamsDetails.getPreauthExpiry_mastercard_credit());
            editor.putString(PREAUTH_EXPIRY_MASTERCARD_DEBIT, overrideParamsDetails.getPreauthExpiry_mastercard_debit());
            editor.putString(PREAUTH_EXPIRY_VISA_CREDIT, overrideParamsDetails.getPreauthExpiry_visa_credit());
            editor.putString(PREAUTH_EXPIRY_VISA_DEBIT, overrideParamsDetails.getPreauthExpiry_visa_debit());
            editor.putString(PREAUTH_EXPIRY_AMEX, overrideParamsDetails.getPreauthExpiry_amex());
            editor.putString(PREAUTH_EXPIRY_DINERS_CLUB, overrideParamsDetails.getPreauthExpiry_diners_club());
            editor.putString(PREAUTH_EXPIRY_JCB, overrideParamsDetails.getPreauthExpiry_jcb());
            editor.putString(PREAUTH_EXPIRY_UNIONPAY_CREDIT, overrideParamsDetails.getPreauthExpiry_unionpay_credit());

            editor.putString(SCREEN_LOCK_TIME, overrideParamsDetails.getScreenLockTime());
            editor.putString(MOTO_REFUND_PASSWORD, overrideParamsDetails.getMotoRefundPassword());
            editor.putString(REFUND_PASSWORD, overrideParamsDetails.getRefundPassword());

            editor.putString(PCI_REBOOT_TIME, overrideParamsDetails.getPciRebootTime());

            editor.putBoolean(AUTO_SETTLEMENT_ENABLED, convertBoolean(overrideParamsDetails.getAutoSettlementEnabled()));
            editor.putString(AUTO_SETTLEMENT_RETRY_COUNT, overrideParamsDetails.getAutoSettlementRetryCount());
            editor.putBoolean(AUTO_SETTLEMENT_PRINT_TRANSACTION_LISTING, convertBoolean(overrideParamsDetails.getAutoSettlementPrintTransactionListing()));
            editor.putString(AUTO_SETTLEMENT_TIME, overrideParamsDetails.getAutoSettlementTime());
            editor.putString(AUTO_SETTLEMENT_TIME_WINDOW, overrideParamsDetails.getAutoSettlementTimeWindow());
            editor.putString(AUTO_SETTLEMENT_IDLING_PERIOD, overrideParamsDetails.getAutoSettlementIdlingPeriod());

            editor.putString(MAX_DAYS_TRANSACTIONS_TO_STORE, overrideParamsDetails.getMaxDaysTransactionsToStore());

            //Linkly Bin Number
            editor.putString(BIN_EFTPOS, overrideParamsDetails.getBin_eftpos());
            editor.putString(PREAUTH_EXPIRY_EFTPOS, overrideParamsDetails.getPreauthExpiry_eftpos());
            editor.putString(BIN_MASTERCARD_CREDIT, overrideParamsDetails.getBin_mastercard_credit());
            editor.putString(PREAUTH_EXPIRY_MASTERCARD_CREDIT, overrideParamsDetails.getPreauthExpiry_mastercard_credit());
            editor.putString(BIN_MASTERCARD_DEBIT, overrideParamsDetails.getBin_mastercard_debit());
            editor.putString(PREAUTH_EXPIRY_MASTERCARD_DEBIT, overrideParamsDetails.getPreauthExpiry_mastercard_debit());
            editor.putString(BIN_VISA_CREDIT, overrideParamsDetails.getBin_visa_credit());
            editor.putString(PREAUTH_EXPIRY_VISA_CREDIT, overrideParamsDetails.getPreauthExpiry_visa_credit());
            editor.putString(BIN_VISA_DEBIT, overrideParamsDetails.getBin_visa_debit());
            editor.putString(PREAUTH_EXPIRY_VISA_DEBIT, overrideParamsDetails.getPreauthExpiry_visa_debit());
            editor.putString(BIN_AMEX, overrideParamsDetails.getBin_amex());
            editor.putString(PREAUTH_EXPIRY_AMEX, overrideParamsDetails.getPreauthExpiry_amex());
            editor.putString(BIN_DINERS_CLUB, overrideParamsDetails.getBin_diners_club());
            editor.putString(PREAUTH_EXPIRY_DINERS_CLUB, overrideParamsDetails.getPreauthExpiry_diners_club());
            editor.putString(BIN_JCB, overrideParamsDetails.getBin_jcb());
            editor.putString(PREAUTH_EXPIRY_JCB, overrideParamsDetails.getPreauthExpiry_jcb());
            editor.putString(BIN_UNIONPAY_CREDIT, overrideParamsDetails.getBin_unionpay_credit());
            editor.putString(PREAUTH_EXPIRY_UNIONPAY_CREDIT, overrideParamsDetails.getPreauthExpiry_unionpay_credit());

            // copy issuer config from overrideparameters class format to ourInstance (PayCfgImpl) format
            addIssuerToConfig(editor, 1, overrideParamsDetails.getIssuer1Name(), overrideParamsDetails.getIssuer1Enabled(), overrideParamsDetails.getIssuer1DeferredAuthEnabled());
            addIssuerToConfig(editor, 2, overrideParamsDetails.getIssuer2Name(), overrideParamsDetails.getIssuer2Enabled(), overrideParamsDetails.getIssuer2DeferredAuthEnabled());
            addIssuerToConfig(editor, 3, overrideParamsDetails.getIssuer3Name(), overrideParamsDetails.getIssuer3Enabled(), overrideParamsDetails.getIssuer3DeferredAuthEnabled());
            addIssuerToConfig(editor, 4, overrideParamsDetails.getIssuer4Name(), overrideParamsDetails.getIssuer4Enabled(), overrideParamsDetails.getIssuer4DeferredAuthEnabled());
            addIssuerToConfig(editor, 5, overrideParamsDetails.getIssuer5Name(), overrideParamsDetails.getIssuer5Enabled(), overrideParamsDetails.getIssuer5DeferredAuthEnabled());
            addIssuerToConfig(editor, 6, overrideParamsDetails.getIssuer6Name(), overrideParamsDetails.getIssuer6Enabled(), overrideParamsDetails.getIssuer6DeferredAuthEnabled());
            addIssuerToConfig(editor, 7, overrideParamsDetails.getIssuer7Name(), overrideParamsDetails.getIssuer7Enabled(), overrideParamsDetails.getIssuer7DeferredAuthEnabled());
            addIssuerToConfig(editor, 8, overrideParamsDetails.getIssuer8Name(), overrideParamsDetails.getIssuer8Enabled(), overrideParamsDetails.getIssuer8DeferredAuthEnabled());
            addIssuerToConfig(editor, 9, overrideParamsDetails.getIssuer9Name(), overrideParamsDetails.getIssuer9Enabled(), overrideParamsDetails.getIssuer9DeferredAuthEnabled());
            addIssuerToConfig(editor, 10, overrideParamsDetails.getIssuer10Name(), overrideParamsDetails.getIssuer10Enabled(), overrideParamsDetails.getIssuer10DeferredAuthEnabled());

            // cdo allowed flags
            editor.putBoolean(CDO_ALLOWED_EFTPOS, convertBoolean(overrideParamsDetails.getCdoAllowedEftpos()));
            editor.putBoolean(CDO_ALLOWED_MASTERCARD_CREDIT, convertBoolean(overrideParamsDetails.getCdoAllowedMastercardCredit()));
            editor.putBoolean(CDO_ALLOWED_MASTERCARD_DEBIT, convertBoolean(overrideParamsDetails.getCdoAllowedMastercardDebit()));
            editor.putBoolean(CDO_ALLOWED_VISA_CREDIT, convertBoolean(overrideParamsDetails.getCdoAllowedVisaCredit()));
            editor.putBoolean(CDO_ALLOWED_VISA_DEBIT, convertBoolean(overrideParamsDetails.getCdoAllowedVisaDebit()));
            editor.putBoolean(CDO_ALLOWED_AMEX, convertBoolean(overrideParamsDetails.getCdoAllowedAmex()));
            editor.putBoolean(CDO_ALLOWED_DINERS_CLUB, convertBoolean(overrideParamsDetails.getCdoAllowedDinersClub()));
            editor.putBoolean(CDO_ALLOWED_JCB, convertBoolean(overrideParamsDetails.getCdoAllowedJcb()));
            editor.putBoolean(CDO_ALLOWED_UNIONPAY_CREDIT, convertBoolean(overrideParamsDetails.getCdoAllowedUnionpayCredit()));

            // Timeout params
            editor.putInt(ConfigTimeouts.AMOUNT_ENTRY_TIMEOUT.name(), convertSecToMilliSecs(overrideParamsDetails.getAmountEntryTimeoutSecs(), String.valueOf(ConfigTimeouts.AMOUNT_ENTRY_TIMEOUT.getMaximumSecs())));
            editor.putInt(ConfigTimeouts.PRESENT_CARD_TIMEOUT.name(), convertSecToMilliSecs(overrideParamsDetails.getPresentCardTimeoutSecs(), String.valueOf(ConfigTimeouts.PRESENT_CARD_TIMEOUT.getMaximumSecs())));
            editor.putInt(ConfigTimeouts.ACCOUNT_SELECTION_TIMEOUT.name(), convertSecToMilliSecs(overrideParamsDetails.getAccountSelectionTimeoutSecs(),String.valueOf(ConfigTimeouts.ACCOUNT_SELECTION_TIMEOUT.getMaximumSecs())));
            editor.putInt(ConfigTimeouts.APP_SELECTION_TIMEOUT.name(), convertSecToMilliSecs(overrideParamsDetails.getAppSelectionTimeoutSecs(),String.valueOf(ConfigTimeouts.APP_SELECTION_TIMEOUT.getMaximumSecs())));
            editor.putInt(ConfigTimeouts.CARD_PIN_ENTRY_TIMEOUT.name(), convertSecToMilliSecs(overrideParamsDetails.getCardPinEntryTimeoutSecs(),String.valueOf(ConfigTimeouts.CARD_PIN_ENTRY_TIMEOUT.getMaximumSecs())));
            editor.putInt(ConfigTimeouts.CONFIRM_SIGNATURE_TIMEOUT.name(), convertSecToMilliSecs(overrideParamsDetails.getConfirmSignatureTimeoutSecs(),String.valueOf(ConfigTimeouts.CONFIRM_SIGNATURE_TIMEOUT.getMaximumSecs())));
            editor.putInt(ConfigTimeouts.CUSTOMER_PRINT_RECEIPT_TIMEOUT.name(), convertSecToMilliSecs(overrideParamsDetails.getCustomerReceiptPrintPromptTimeoutSecs(),String.valueOf(ConfigTimeouts.CUSTOMER_PRINT_RECEIPT_TIMEOUT.getMaximumSecs())));
            editor.putInt(ConfigTimeouts.REMOVE_RECEIPT_TIMEOUT.name(), convertSecToMilliSecs(overrideParamsDetails.getReceiptRemovePromptTimeoutSecs(),String.valueOf(ConfigTimeouts.REMOVE_RECEIPT_TIMEOUT.getMaximumSecs())));
            editor.putInt(ConfigTimeouts.REMOVE_CARD_TIMEOUT.name(), convertSecToMilliSecs(overrideParamsDetails.getCardRemovePromptTimeoutSecs(),String.valueOf(ConfigTimeouts.REMOVE_CARD_TIMEOUT.getMaximumSecs())));
            editor.putInt(ConfigTimeouts.PAPER_OUT_TIMEOUT.name(), convertSecToMilliSecs(overrideParamsDetails.getPaperOutTimeoutSecs(),String.valueOf(ConfigTimeouts.PAPER_OUT_TIMEOUT.getMaximumSecs())));
            editor.putInt(ConfigTimeouts.DECISION_SCREEN_TIMEOUT.name(), convertSecToMilliSecs(overrideParamsDetails.getDecisionScreenTimeoutSecs(),String.valueOf(ConfigTimeouts.DECISION_SCREEN_TIMEOUT.getMaximumSecs())));
            // Timeout param with access mode enabled.
            editor.putInt(ConfigTimeouts.PRESENT_CARD_TIMEOUT.name() + ACCESS_MODE_SUFFIX, convertSecToMilliSecs(overrideParamsDetails.getAccessModePresentCardTimeoutSecs(),String.valueOf(ConfigTimeouts.PRESENT_CARD_TIMEOUT.getAccessModeMaximumSecs())));
            editor.putInt(ConfigTimeouts.ACCOUNT_SELECTION_TIMEOUT.name() + ACCESS_MODE_SUFFIX, convertSecToMilliSecs(overrideParamsDetails.getAccessModeAccountSelectionTimeoutSecs(),String.valueOf(ConfigTimeouts.ACCOUNT_SELECTION_TIMEOUT.getAccessModeMaximumSecs())));
            editor.putInt(ConfigTimeouts.APP_SELECTION_TIMEOUT.name() + ACCESS_MODE_SUFFIX, convertSecToMilliSecs(overrideParamsDetails.getAccessModeAppSelectionTimeoutSecs(),String.valueOf(ConfigTimeouts.APP_SELECTION_TIMEOUT.getAccessModeMaximumSecs())));
            editor.putInt(ConfigTimeouts.CARD_PIN_ENTRY_TIMEOUT.name() + ACCESS_MODE_SUFFIX, convertSecToMilliSecs(overrideParamsDetails.getAccessModePinEntryTimeoutSecs(),String.valueOf(ConfigTimeouts.CARD_PIN_ENTRY_TIMEOUT.getAccessModeMaximumSecs())));
            editor.putInt(ConfigTimeouts.DECISION_SCREEN_TIMEOUT.name()+ ACCESS_MODE_SUFFIX, convertSecToMilliSecs(overrideParamsDetails.getAccessModeDecisionScreenTimeoutSecs(),String.valueOf(ConfigTimeouts.DECISION_SCREEN_TIMEOUT.getAccessModeMaximumSecs())));


            editor.putString(CARD_PRODUCT_FILE, overrideParamsDetails.getCardProductFile());
            editor.putString(CFG_EMV_FILE, overrideParamsDetails.getCfgEmvFile());
            editor.putString(CFG_CTLS_FILE, overrideParamsDetails.getCfgCtlsFile());
            editor.putString(BLACKLIST_FILE, overrideParamsDetails.getBlacklistFile());
            editor.putString(EPAT_FILE, overrideParamsDetails.getEpatFile());
            editor.putString(PKT_FILE, overrideParamsDetails.getPktFile());
            editor.putString(CARDS_FILE, overrideParamsDetails.getCardsFile());

            editor.putString(PASSWORD_RETRY_LIMIT, overrideParamsDetails.getPasswordRetryLimit());
            editor.putString(PASSWORD_ATTEMPT_WINDOW, overrideParamsDetails.getPasswordAttemptWindow());
            editor.putString(PASSWORD_ATTEMPT_LOCKOUT_DURATION, overrideParamsDetails.getPasswordAttemptLockoutDuration());
            editor.putString(PASSWORD_MAXIMUM_AGE, overrideParamsDetails.getPasswordMaximumAge());
            editor.putString(PASSWORD_RETRY_LIMIT1, overrideParamsDetails.getRefundPasswordRetryLimit());
            editor.putString(PASSWORD_RETRY_LIMIT2, overrideParamsDetails.getMotoPasswordRetryLimit());
            editor.putString(RRN_RETRY_LIMIT, overrideParamsDetails.getRrnRetryLimit());
            editor.putString(AUTHCODE_RETRY_LIMIT, overrideParamsDetails.getAuthcodeRetryLimit());
            // Store the last modified value to be referenced when reloading the files.
            editor.putLong(OVERRIDE_LAST_MODIFIED, lastModified);
        }
        editor.apply();
        return true;
    }

    public long getConfigFileMetaDataLastModifiedOverrideParams() {
        return overrideParamsPref.getLong(OVERRIDE_LAST_MODIFIED, 0);
    }

    public long getConfigFileMetaDataLastModifiedHotloadParams() {
        return hotloadParamsPref.getLong(HOTLOAD_LAST_MODIFIED, 0);
    }

    public long getConfigFileMetaDataLastModifiedInitialParams() {
        return initialParamsPref.getLong(INITIAL_LAST_MODIFIED, 0);
    }

    /***************************************************************/
    /* details for parsing the xml */
    public boolean isCashBackAllowed() {

        if (!isCashback()) {
            // disallowed by global flag
            return false;
        }
        loadCards();
        if (this.cards == null) {
            return false;
        }

        for (CardProductCfg card : this.cards) {
            if ( card.getServicesAllowed() != null && card.getServicesAllowed().isCashback() ) {
                return true;
            }
        }
        return false;

    }

    public boolean isSaleTransAllowed() {

        loadCards();
        if (this.cards == null) {
            Timber.i( "cards == null" );
            return false;
        }

        for (CardProductCfg card : this.cards) {
            if( card.getServicesAllowed() == null ) {
                Timber.i( "card.getServicesAllowed() == null" );
            }

            if ( card.getServicesAllowed() != null && card.getServicesAllowed().isSale() ) {
                return true;
            }
        }
        return false;
    }

    public boolean isCashTransAllowed() {

        if (!isCashout()) {
            // disallowed by global flag
            return false;
        }
        loadCards();
        if (this.cards == null) {
            return false;
        }

        for (CardProductCfg card : this.cards) {
            if ( card.getServicesAllowed() != null && card.getServicesAllowed().isCash()) {
                return true;
            }
        }
        return false;
    }

    /* if either moto or telephone allowed return true */
    /* and if the user is allowed to do manual */
    public boolean isManualAllowed() {
        loadCards();
        if (this.cards == null) {
            return false;
        }

        if ( !isTelephone() && !isMailOrder()) {
            return false;
        }

        for (CardProductCfg card : this.cards) {
            if ( card.getServicesAllowed() != null && card.getServicesAllowed().isMoto()) {
                return true;
            }
        }
        return false;
    }

    public boolean isReversalTransAllowed() {

        if( !isReversal() ) {
            // disallowed by global flag
            return false;
        }
        loadCards();
        if (this.cards == null) {
            return false;
        }

        for (CardProductCfg card : this.cards) {
            if (card.getServicesAllowed() != null && card.getServicesAllowed().isReversal()) {
                return true;
            }
        }
        return false;
    }

    public boolean isOfflineRefundTransAllowed() {

        if (this.cards == null) {
            return false;
        }
        loadCards();
        for (CardProductCfg card : this.cards) {
            if (card.getServicesAllowed() != null && card.getServicesAllowed().isOfflineRefund()) {
                return true;
            }
        }
        return false;
    }

    public boolean isRefundTransAllowed() {

        if (!isRefund()) {
            return false;
        }
        loadCards();
        if (this.cards == null) {
            return false;
        }

        for (CardProductCfg card : this.cards) {
            if (card.getServicesAllowed() != null && card.getServicesAllowed().isRefund()) {
                return true;
            }
        }
        return false;
    }

    public boolean isPreAuthTransAllowed() {

        if (!isPreauth())
            return false;
        loadCards();
        if (this.cards == null) {
            return false;
        }

        for (CardProductCfg card : this.cards) {
            if (card.getServicesAllowed() != null && card.getServicesAllowed().isPreauth()) {
                return true;
            }
        }
        return false;
    }

    public boolean isCompletionTransAllowed() {
        loadCards();
        if (this.cards == null) {
            return false;
        }

        for (CardProductCfg card : this.cards) {
            if (card.getServicesAllowed() != null && card.getServicesAllowed().isPreauth()) {
                return true;
            }
        }
        return false;
    }

    public boolean isBalanceTransAllowed() {
        loadCards();
        if (this.cards == null) {
            return false;
        }

        for (CardProductCfg card : this.cards) {
            if (card.getServicesAllowed() != null && card.getServicesAllowed().isBalance()) {
                return true;
            }
        }
        return false;
    }

    public boolean isDepositTransAllowed() {
        loadCards();
        if (this.cards == null) {
            return false;
        }

        for (CardProductCfg card : this.cards) {
            if (card.getServicesAllowed() != null && card.getServicesAllowed().isDeposit()) {
                return true;
            }
        }
        return false;
    }

    public boolean isOfflineSaleTransAllowed() {
        loadCards();
        if (this.cards == null) {
            return false;
        }

        for (CardProductCfg card : this.cards) {
            if (card.getServicesAllowed() != null && card.getServicesAllowed().isForced()) {
                return true;
            }
        }
        return false;
    }

    public boolean getPasswordRequiredForAllCards(TransRec trans, TCard.CaptureMethod captureMethod ) {
        loadCards();
        // Check if blanket Moto password prompt is set
        if( captureMethod == TCard.CaptureMethod.MANUAL ){
            return true;
        }

        for (CardProductCfg cfg : cards) {
            if (!TransRec.isSupervisorRequiredForTransType(this, trans, cfg.getPasswordRequired(), captureMethod)) {
                return false;
            }
        }
        return true;
    }

    public boolean isReconciliationAllowed() {
        return getManRecon() != null && getManRecon().isEnabled();
    }

    public boolean isOfflineCashAllowed() {
        loadCards();
        if (this.cards == null) {
            return false;
        }

        for (CardProductCfg card : this.cards) {
            if (card.getServicesAllowed() != null && card.getServicesAllowed().isOfflineCash()) {
                return true;
            }
        }
        return false;
    }

    public String getStid() {
        return initialParamsPref.getString(STID, "");
    }

    public String getMid() {
        return initialParamsPref.getString(MID, "");
    }

    public boolean isValidCfg() {
        // Confirm that all our expected files have been loaded
        return getConfigFileMetaDataLastModifiedOverrideParams() != 0 &&
                getConfigFileMetaDataLastModifiedInitialParams() != 0 &&
                getConfigFileMetaDataLastModifiedHotloadParams() != 0;
    }

    public String getCustomerName() {
        return overrideParamsPref.getString(CUSTOMER_NAME, "");
    }

    public String getCurrencyCode() {
        return overrideParamsPref.getString(CURRENCY_CODE, "036");
    }

    public int getCurrencyNum() {
        return overrideParamsPref.getInt(CURRENCY_NUM, 36);
    }

    public int getCountryNum() {
        return overrideParamsPref.getInt(COUNTRY_NUM, 36);
    }

    public boolean isCashout() {
        return hotloadParamsPref.getBoolean(CASHOUT, false);
    }

    public boolean isCashback() {
        return hotloadParamsPref.getBoolean(CASHBACK, false);
    }

    public boolean isPurchase() {
        return overrideParamsPref.getBoolean("Purchase", false);
    }

    public boolean isRefund() {
        return hotloadParamsPref.getBoolean(REFUND, false);
    }

    public boolean isReversal() {
        return hotloadParamsPref.getBoolean(REVERSAL, false);
    }

    public boolean isCardholderPresent() {
        return hotloadParamsPref.getBoolean(CARDHOLDER_PRESENT, false);
    }

    public boolean isAccessMode() {
        return hotloadParamsPref.getBoolean(ACCESS_MODE, false);
    }

    public boolean isMailOrder() {
        return hotloadParamsPref.getBoolean(MAIL_ORDER, false);
    }

    public boolean isTelephone() {
        return hotloadParamsPref.getBoolean(TELEPHONE, false);
    }

    public boolean isPreauth() {
        return hotloadParamsPref.getBoolean(PREAUTH, false);
    }

    public boolean isPreauthCreditAccountOnly() {
        return hotloadParamsPref.getBoolean(PREAUTH_CREDIT_ACCOUNT_ONLY, false);
    }

    public String getVirtualCardNumber() {
        return overrideParamsPref.getString("VirtualCardNumber", "");
    }

    public String getSupervisorCardNumber() {
        return overrideParamsPref.getString("SupervisorCardNumber", "");
    }

    public boolean isTipAllowed() {
        return hotloadParamsPref.getBoolean(TIP_ALLOWED, false);
    }

    public String getPanMask() {
        return overrideParamsPref.getString("PanMask", "");
    }

    public boolean isEmvSupported() {
        return hotloadParamsPref.getBoolean(EMV_SUPPORTED, false);
    }

    public boolean isContactlessSupported() {
        return hotloadParamsPref.getBoolean(CONTACTLESS_SUPPORTED, false);
    }

    public boolean isDccSupported() {
        return overrideParamsPref.getBoolean(DCC_SUPPORTED, false);
    }

    public boolean isLoyaltySupported() {
        return hotloadParamsPref.getBoolean(LOYALTY_SUPPORTED, false);
    }

    public boolean isDeferredAuthEnabled() {
        return overrideParamsPref.getBoolean(DEFERRED_AUTH_ENABLED, false);
    }

    public int getMaxDeferredAuthCount() {
        return overrideParamsPref.getInt(MAX_DEFERRED_AUTH_COUNT, 0);
    }

    public int getMaxDeferredAuthValue() {
        return overrideParamsPref.getInt(MAX_DEFERRED_AUTH_VALUE, 0);
    }

    public boolean isAutoUserLogin() {
        return overrideParamsPref.getBoolean(AUTO_USER_LOGIN, false);
    }

    public int getExitAppAction() {
        return overrideParamsPref.getInt("ExitAppAction", 0);
    }

    public int getCustRefRequired() {
        return hotloadParamsPref.getInt(CUST_REF_REQUIRED, 0);
    }

    public String getCustRefPrompt() {
        return overrideParamsPref.getString("CustRefPrompt", "");
    }

    public int getReconBackOff1() {
        return overrideParamsPref.getInt("ReconBackOff1", 0);
    }

    public int getReconBackOff2() {
        return overrideParamsPref.getInt("ReconBackOff2", 0);
    }

    public int getReconBackOff3() {
        return overrideParamsPref.getInt("ReconBackOff3", 0);
    }

    public String getLogo() {
        return overrideParamsPref.getString("Logo", "");
    }

    public boolean isDisableField55Advice() {
        return overrideParamsPref.getBoolean("DisableField55Advice", false);
    }

    public int getCheckTimersTimeout() {
        return overrideParamsPref.getInt("CheckTimersTimeout", 0);
    }

    public int getScreenTimeout() {
        return overrideParamsPref.getInt("ScreenTimeout", 0);
    }

    public boolean isUseP2pe() {
        return overrideParamsPref.getBoolean("UseP2pe", true);
    }

    public PaymentSwitch getPaymentSwitch() {
        return new PaymentSwitch(overrideParamsPref);
    }

    public ManRec getManRecon() {
        return new ManRec(overrideParamsPref);
    }

    public Receipt getReceipt() {
        return new Receipt(hotloadParamsPref);
    }

    // TODO: Follow up with this manual entry via override params may not exit.
    public ManualEntry getManualEntry() {
        return new ManualEntry();
    }

    public int getCardProductVersion() {
        return overrideParamsPref.getInt(CARD_PRODUCT_VERSION, 0);
    }

    public List<CardProductCfg> getCards() {
        return this.cards;
    }

    public CardProductCfg getDefaultCardProduct() {
        return CardProductCfg.getDefaultConfig();
    }

    public boolean isIncludedOrginalStandInRec() {
        return overrideParamsPref.getBoolean(INCLUDED_ORGINAL_STAND_IN_REC, false);
    }

    public boolean isReversalCopyOriginal() {
        return overrideParamsPref.getBoolean(REVERSAL_COPY_ORIGINAL, false);
    }

    public String getStrConfigSource() {
        return overrideParamsPref.getString("StrConfigSource", "");
    }

    public String getLanguage() {
        return overrideParamsPref.getString(LANGUAGE, "");
    }

    public String getAccessToken() {
        return overrideParamsPref.getString("AccessToken", "");
    }

    public String getAccessToken2() {
        return overrideParamsPref.getString("AccessToken2", "");
    }

    public boolean isMailMerchant() {
        return overrideParamsPref.getBoolean(MAIL_MERCHANT, false);
    }

    public String getMailMerchantAddress() {
        return overrideParamsPref.getString(MAIL_MERCHANT_ADDRESS, "");
    }

    public String getMailHost() {
        return overrideParamsPref.getString(MAIL_HOST, "");
    }

    public String getMailPort() {
        return overrideParamsPref.getString(MAIL_PORT, "");
    }

    public String getMailUser() {
        return overrideParamsPref.getString(MAIL_USER, "");
    }

    public String getMailPassword() {
        return overrideParamsPref.getString(MAIL_PASSWORD, "");
    }

    public String getMailSender() {
        return overrideParamsPref.getString(MAIL_SENDER, "");
    }

    public String getMailStoreName() {
        return overrideParamsPref.getString(MAIL_STORE_NAME, "");
    }

    public boolean isPaxstoreUpload() {
        return overrideParamsPref.getBoolean(PAXSTORE_UPLOAD, false);
    }

    @Override
    public int getSaleLimitCents() {
        return hotloadParamsPref.getInt(SALE_LIMIT_CENTS, 0);
    }

    @Override
    public int getPreAuthLimitCents() {
        return hotloadParamsPref.getInt(PRE_AUTH_LIMIT_CENTS, 0);
    }

    public int getOfflineTransactionCeilingLimitCentsContact() {
        return overrideParamsPref.getInt(OFFLINE_TRANSACTION_CEILING_LIMIT_CENTS_CONTACT, 0);
    }

    public int getOfflineTransactionCeilingLimitCentsContactless() {
        return overrideParamsPref.getInt(OFFLINE_TRANSACTION_CEILING_LIMIT_CENTS_CONTACTLESS, 0);
    }

    @Override
    public int getCashoutLimitCents() {
        return hotloadParamsPref.getInt(CASHOUT_LIMIT_CENTS, 0);
    }

    public String getMaxRefundLimit() {
        return hotloadParamsPref.getString(MAX_REFUND_LIMIT, "");
    }

    public boolean isOfflineFlightModeAllowed() {
        return overrideParamsPref.getBoolean(OFFLINE_FLIGHT_MODE_ALLOWED, false);
    }

    @Override
    public int getOfflineSoftLimitAmountCents() {
        return overrideParamsPref.getInt(OFFLINE_SOFT_LIMIT_AMOUNT_CENTS, 0);
    }

    public int getOfflineSoftLimitCount() {
        return overrideParamsPref.getInt(OFFLINE_SOFT_LIMIT_COUNT, 0);
    }

    @Override
    public int getOfflineUpperLimitAmountCents() {
        return overrideParamsPref.getInt(OFFLINE_UPPER_LIMIT_AMOUNT_CENTS, 0);
    }

    public int getOfflineUpperLimitCount() {
        return overrideParamsPref.getInt(OFFLINE_UPPER_LIMIT_COUNT, 0);
    }

    public boolean isUnattendedModeAllowed() {
        return overrideParamsPref.getBoolean(UNATTENDED_MODE_ALLOWED, false);
    }

    public String getBankDescription() {
        return overrideParamsPref.getString(BANK_DESCRIPTION, "");
    }

    public String getRetailerName() {
        return overrideParamsPref.getString(RETAILER_NAME, "");
    }

    public boolean isCommsFallbackEnabled() {
        return overrideParamsPref.getBoolean(COMMS_FALLBACK_ENABLED, true);
    }

    public String getCommsFallbackHost() {
        return configTryOrReturnDefault(overrideParamsPref,COMMS_FALLBACK_HOST, "www.google.com:80");
    }

    public boolean isUsePercentageTip() {
        return overrideParamsPref.getBoolean(USE_PERCENTAGE_TIP, false);
    }

    public boolean isRefundSecure() {
        return hotloadParamsPref.getBoolean(REFUND_SECURE, false);
    }

    public boolean isEfbSupported() {
        return hotloadParamsPref.getBoolean(EFB_SUPPORTED, false);
    }

    public boolean isEfbAcknowledgeServiceCode() {
        return hotloadParamsPref.getBoolean(EFB_ACKNOWLEDGE_SERVICE_CODE, false);
    }

    public int getEfbPlasticCardLifeDays() {
        return hotloadParamsPref.getInt(EFB_PLASTIC_CARD_LIFE_DAYS, 0);
    }

    public boolean isEfbRefundAllowed() {
        return hotloadParamsPref.getBoolean(EFB_REFUND_ALLOWED, false);
    }

    public boolean isEfbCashoutAllowed() {
        return hotloadParamsPref.getBoolean(EFB_CASHOUT_ALLOWED, false);
    }

    public int getEfbContinueInFallbackTimeoutMinutes() {
        return hotloadParamsPref.getInt(EFB_CONTINUE_IN_FALLBACK_TIMEOUT_MINUTES, 0);
    }

    public boolean isEfbAuthNumberOverFloorLimitAllowed() {
        return hotloadParamsPref.getBoolean(EFB_AUTH_NUMBER_OVER_FLOOR_LIMIT_ALLOWED, false);
    }

    public boolean isSurchargeSupported() {
        return hotloadParamsPref.getBoolean(SURCHARGE_SUPPORTED, false);
    }

    public boolean isPasscodeSecuritySupported() {
        return overrideParamsPref.getBoolean(PASSCODE_SECURITY_SUPPORTED, true);
    }

    public int getMcrLimit() {
        return hotloadParamsPref.getInt(MCR_LIMIT, 0);
    }

    public int getMcrUpperLimit() {
        return hotloadParamsPref.getInt(MCR_UPPER_LIMIT, 0);
    }

    public boolean isMcrEnabled() {
        return hotloadParamsPref.getBoolean(MCR_ENABLED, false);
    }

    public boolean isPosCommsEnabled() {
        return overrideParamsPref.getBoolean(POS_COMMS_ENABLED, false);
    }

    public String getPosCommsHostId() {
        return overrideParamsPref.getString(POS_COMMS_HOST_ID, "");
    }

    public String getPosCommsInterfaceType() {
        return overrideParamsPref.getString(POS_COMMS_INTERFACE_TYPE, "");
    }

    public boolean isEmvFallback() {
        return hotloadParamsPref.getBoolean(EMV_FALLBACK, true);
    }

    public boolean isMsrAllowed() {
        return hotloadParamsPref.getBoolean(MSR_ALLOWED, true);
    }

    public String getBankTimeZone() {
        return initialParamsPref.getString(BANK_TIME_ZONE, "");
    }

    public String getTerminalTimeZone() {
        return initialParamsPref.getString(TERMINAL_TIME_ZONE, "");
    }

    @Override
    public String getPaymentAppVersion() {
        return initialParamsPref.getString(PAYMENT_APP_VERSION, "");
    }

    @Override
    public void setPaymentAppVersion(String version) {
        initialParamsPref.edit().putString(PAYMENT_APP_VERSION, version).apply();
    }


    private IssuerCfg getIssuer(int index) {
        String name = "Issuer" + index + "Name";
        String enabled = "Issuer" + index + "Enabled";
        String deferredAuthEnabled = "Issuer" + index + DEFERRED_AUTH_ENABLED;

        if(!overrideParamsPref.getString(name, "").isEmpty()) {
            return new IssuerCfg(
                    overrideParamsPref.getString(name, ""),
                    overrideParamsPref.getBoolean(enabled, false),
                    overrideParamsPref.getBoolean(deferredAuthEnabled, false)
            );
        }

        return null;
    }

    private static final int ISSUER_START_IDX = 1;
    private static final int ISSUER_MAX_TOTAL = 10;

    public List<IssuerCfg> getIssuers() {
        List<IssuerCfg> issuerCfgs = new ArrayList<>();
        for(int i = ISSUER_START_IDX; i <= ISSUER_MAX_TOTAL; i++){
            IssuerCfg cfg = getIssuer(i);
            if(cfg != null) {
                issuerCfgs.add(cfg);
            }
        }

        return issuerCfgs;
    }

    private boolean addLinklyBinNumber(List<LinklyBinNumber> list, String bin, String cardName, String expiry) {
        if (Util.isNullOrEmpty(bin) || Util.isNullOrEmpty(cardName) || expiry == null) {
            return false;
        }

        list.add(new LinklyBinNumber(bin, cardName, expiry));
        Timber.i("added linkly bin number for scheme '" + cardName + "' : " + bin + " and preauth expiryDays for scheme '" + expiry);

        return true;
    }

    private boolean addToDefaultSurcharge(List<Surcharge> surchargeList, boolean enabled, String bin, String type, String amount) {
        // not set or null, don't add to list
        if (!enabled || Util.isNullOrEmpty(type) || Util.isNullOrEmpty(amount) ) {
            return false;
        }

        // it's fine, add it
        Timber.i("added surcharge for scheme: " + bin + ", type = " + type + ", amount =" + amount);
        surchargeList.add(new Surcharge(bin, type, amount));
        return true;
    }

    public List<Surcharge> getDefaultSc() {
        List<Surcharge> surchargeList = new ArrayList<>();

        addToDefaultSurcharge(surchargeList, hotloadParamsPref.getBoolean(SC_EFTPOS_ON, false), overrideParamsPref.getString(BIN_EFTPOS, ""), hotloadParamsPref.getString(SC_EFTPOS_TYPE, ""), hotloadParamsPref.getString(SC_EFTPOS_AMOUNT, ""));
        addToDefaultSurcharge(surchargeList, hotloadParamsPref.getBoolean(SC_MASTERCARD_CREDIT_ON, false), overrideParamsPref.getString(BIN_MASTERCARD_CREDIT, ""), hotloadParamsPref.getString(SC_MASTERCARD_CREDIT_TYPE, ""), hotloadParamsPref.getString(SC_MASTERCARD_CREDIT_AMOUNT, ""));
        addToDefaultSurcharge(surchargeList, hotloadParamsPref.getBoolean(SC_MASTERCARD_DEBIT_ON, false), overrideParamsPref.getString(BIN_MASTERCARD_DEBIT, ""), hotloadParamsPref.getString(SC_MASTERCARD_DEBIT_TYPE, ""), hotloadParamsPref.getString(SC_MASTERCARD_DEBIT_AMOUNT, ""));
        addToDefaultSurcharge(surchargeList, hotloadParamsPref.getBoolean(SC_VISA_CREDIT_ON, false), overrideParamsPref.getString(BIN_VISA_CREDIT, ""), hotloadParamsPref.getString(SC_VISA_CREDIT_TYPE, ""), hotloadParamsPref.getString(SC_VISA_CREDIT_AMOUNT, ""));
        addToDefaultSurcharge(surchargeList, hotloadParamsPref.getBoolean(SC_VISA_DEBIT_ON, false), overrideParamsPref.getString(BIN_VISA_DEBIT, ""), hotloadParamsPref.getString(SC_VISA_DEBIT_TYPE, ""), hotloadParamsPref.getString(SC_VISA_DEBIT_AMOUNT, ""));
        addToDefaultSurcharge(surchargeList, hotloadParamsPref.getBoolean(SC_AMEX_ON, false), overrideParamsPref.getString(BIN_AMEX, ""), hotloadParamsPref.getString(SC_AMEX_TYPE, ""), hotloadParamsPref.getString(SC_AMEX_AMOUNT, ""));
        addToDefaultSurcharge(surchargeList, hotloadParamsPref.getBoolean(SC_DINERS_CLUB_ON, false), overrideParamsPref.getString(BIN_DINERS_CLUB, ""), hotloadParamsPref.getString(SC_DINERS_CLUB_TYPE, ""), hotloadParamsPref.getString(SC_DINERS_CLUB_AMOUNT, ""));
        addToDefaultSurcharge(surchargeList, hotloadParamsPref.getBoolean(SC_JCB_ON, false), overrideParamsPref.getString(BIN_JCB, ""), hotloadParamsPref.getString(SC_JCB_TYPE, ""), hotloadParamsPref.getString(SC_JCB_AMOUNT, ""));
        addToDefaultSurcharge(surchargeList, hotloadParamsPref.getBoolean(SC_UNIONPAY_CREDIT_ON, false), overrideParamsPref.getString(BIN_UNIONPAY_CREDIT, ""), hotloadParamsPref.getString(SC_UNIONPAY_CREDIT_TYPE, ""), hotloadParamsPref.getString(SC_UNIONPAY_CREDIT_AMOUNT, ""));

        return surchargeList;
    }

    public List<LinklyBinNumber> getLinklyBinNumbers() {
        List<LinklyBinNumber> linklyBinNumbers = new ArrayList<>();
        addLinklyBinNumber(linklyBinNumbers, overrideParamsPref.getString(BIN_EFTPOS, ""), "eftpos", overrideParamsPref.getString(PREAUTH_EXPIRY_EFTPOS, ""));
        addLinklyBinNumber(linklyBinNumbers, overrideParamsPref.getString(BIN_MASTERCARD_CREDIT, ""), "mastercard credit", overrideParamsPref.getString(PREAUTH_EXPIRY_MASTERCARD_CREDIT, ""));
        addLinklyBinNumber(linklyBinNumbers, overrideParamsPref.getString(BIN_MASTERCARD_DEBIT, ""), "mastercard debit", overrideParamsPref.getString(PREAUTH_EXPIRY_MASTERCARD_DEBIT, ""));
        addLinklyBinNumber(linklyBinNumbers, overrideParamsPref.getString(BIN_VISA_CREDIT, ""), "visa credit", overrideParamsPref.getString(PREAUTH_EXPIRY_VISA_CREDIT, ""));
        addLinklyBinNumber(linklyBinNumbers, overrideParamsPref.getString(BIN_VISA_DEBIT, ""), "visa debit", overrideParamsPref.getString(PREAUTH_EXPIRY_VISA_DEBIT, ""));
        addLinklyBinNumber(linklyBinNumbers, overrideParamsPref.getString(BIN_AMEX, ""), "amex", overrideParamsPref.getString(PREAUTH_EXPIRY_AMEX, ""));
        addLinklyBinNumber(linklyBinNumbers, overrideParamsPref.getString(BIN_DINERS_CLUB, ""), "diners club", overrideParamsPref.getString(PREAUTH_EXPIRY_DINERS_CLUB, ""));
        addLinklyBinNumber(linklyBinNumbers, overrideParamsPref.getString(BIN_JCB, ""), "jcb", overrideParamsPref.getString(PREAUTH_EXPIRY_JCB, ""));
        addLinklyBinNumber(linklyBinNumbers, overrideParamsPref.getString(BIN_UNIONPAY_CREDIT, ""), "unionpay credit", overrideParamsPref.getString(PREAUTH_EXPIRY_UNIONPAY_CREDIT, ""));
        return linklyBinNumbers;
    }

    private boolean createCdoAllowed(List<CdoAllowed> allowedList, String bin, boolean enabled) {
        if (Util.isNullOrEmpty(bin)) {
            Timber.e("Invalid Bin Range: %s", bin);
            return false;
        }

        int result = Integer.parseInt(bin);

        allowedList.add(new CdoAllowed(result, enabled));
        return true;
    }

    public List<CdoAllowed> getCdoAllowedList() {
        List<CdoAllowed> allowedList = new ArrayList<>();
        createCdoAllowed(allowedList, overrideParamsPref.getString(BIN_EFTPOS, ""), overrideParamsPref.getBoolean(CDO_ALLOWED_EFTPOS, false));
        createCdoAllowed(allowedList, overrideParamsPref.getString(BIN_MASTERCARD_CREDIT, ""), overrideParamsPref.getBoolean(CDO_ALLOWED_MASTERCARD_CREDIT, false));
        createCdoAllowed(allowedList, overrideParamsPref.getString(BIN_MASTERCARD_DEBIT, ""), overrideParamsPref.getBoolean(CDO_ALLOWED_MASTERCARD_DEBIT, false));
        createCdoAllowed(allowedList, overrideParamsPref.getString(BIN_VISA_CREDIT, ""), overrideParamsPref.getBoolean(CDO_ALLOWED_VISA_CREDIT, false));
        createCdoAllowed(allowedList, overrideParamsPref.getString(BIN_VISA_DEBIT, ""), overrideParamsPref.getBoolean(CDO_ALLOWED_VISA_DEBIT, false));
        createCdoAllowed(allowedList, overrideParamsPref.getString(BIN_AMEX, ""), overrideParamsPref.getBoolean(CDO_ALLOWED_AMEX, false));
        createCdoAllowed(allowedList, overrideParamsPref.getString(BIN_DINERS_CLUB, ""), overrideParamsPref.getBoolean(CDO_ALLOWED_DINERS_CLUB, false));
        createCdoAllowed(allowedList, overrideParamsPref.getString(BIN_JCB, ""), overrideParamsPref.getBoolean(CDO_ALLOWED_JCB, false));
        createCdoAllowed(allowedList, overrideParamsPref.getString(BIN_UNIONPAY_CREDIT, ""), overrideParamsPref.getBoolean(CDO_ALLOWED_UNIONPAY_CREDIT, false));
        return allowedList;
    }

    public String getLoginManagerUserId() {
        return overrideParamsPref.getString(LOGIN_MANAGER_USER_ID, "");
    }

    public String getLoginManagerInitialPwd() {
        return overrideParamsPref.getString(LOGIN_MANAGER_INITIAL_PWD, "");
    }

    public String getLoginManagerUserName() {
        return overrideParamsPref.getString(LOGIN_MANAGER_USER_NAME, "");
    }

    public String getLoginTechnicianUserId() {
        return overrideParamsPref.getString(LOGIN_TECHNICIAN_USER_ID, "");
    }

    public String getLoginTechnicianInitialPwd() {
        return overrideParamsPref.getString(LOGIN_TECHNICIAN_INITIAL_PWD, "");
    }

    public String getLoginTechnicianUserName() {
        return overrideParamsPref.getString(LOGIN_TECHNICIAN_USER_NAME, "");
    }

    public boolean isShowReceiptPromptForAuto() {
        return hotloadParamsPref.getBoolean(SHOW_RECEIPT_PROMPT_FOR_AUTO, false);
    }

    public String getPrintCustomerReceipt() {
        return hotloadParamsPref.getString(PRINT_CUSTOMER_RECEIPT, "");
    }

    public boolean isMotoPasswordPrompt() {
        return hotloadParamsPref.getBoolean(MOTO_PASSWORD_PROMPT, false);
    }

    public boolean isRefundPasswordPrompt() {
        return hotloadParamsPref.getBoolean(REFUND_PASSWORD_PROMPT, false);
    }

    public boolean isMotoCVVEntry() {
        return hotloadParamsPref.getBoolean(MOTO_CVV_ENTRY, false);
    }

    public boolean isMotoCVVEntryBypassAllowed() {
        return hotloadParamsPref.getBoolean(MOTO_CVV_ENTRY_BYPASS_ALLOWED, false);
    }

    public String getManagerRefundLimit() {
        return hotloadParamsPref.getString(MANAGER_REFUND_LIMIT, "");
    }

    public String getMaxRefundCount() {
        return hotloadParamsPref.getString(MAX_REFUND_COUNT, "");
    }

    public String getMaxCumulativeRefundLimit() {
        return hotloadParamsPref.getString(MAX_CUMULATIVE_REFUND_LIMIT, "");
    }

    public String getMaxTipPercent() {
        return hotloadParamsPref.getString(MAX_TIP_PERCENT, "");
    }

    public String getPreAuthExpiry_default() {
        return overrideParamsPref.getString(PRE_AUTH_EXPIRY_DEFAULT, "");
    }

    public String getPreauthExpiry_eftpos() {
        return overrideParamsPref.getString(PREAUTH_EXPIRY_EFTPOS, "");
    }

    public String getPreauthExpiry_mastercard_credit() {
        return overrideParamsPref.getString(PREAUTH_EXPIRY_MASTERCARD_CREDIT, "");
    }

    public String getPreauthExpiry_mastercard_debit() {
        return overrideParamsPref.getString(PREAUTH_EXPIRY_MASTERCARD_DEBIT, "");
    }

    public String getPreauthExpiry_visa_credit() {
        return overrideParamsPref.getString(PREAUTH_EXPIRY_VISA_CREDIT, "");
    }

    public String getPreauthExpiry_visa_debit() {
        return overrideParamsPref.getString(PREAUTH_EXPIRY_VISA_DEBIT, "");
    }

    public String getPreauthExpiry_amex() {
        return overrideParamsPref.getString(PREAUTH_EXPIRY_AMEX, "");
    }

    public String getPreauthExpiry_diners_club() {
        return overrideParamsPref.getString(PREAUTH_EXPIRY_DINERS_CLUB, "");
    }

    public String getPreauthExpiry_jcb() {
        return overrideParamsPref.getString(PREAUTH_EXPIRY_JCB, "");
    }

    public String getPreauthExpiry_unionpay_credit() {
        return overrideParamsPref.getString(PREAUTH_EXPIRY_UNIONPAY_CREDIT, "");
    }

    public String getMaxPreAuthTrans() {
        return hotloadParamsPref.getString(MAX_PRE_AUTH_TRANS, "");
    }

    public String getMaxEfbTrans() {
        return hotloadParamsPref.getString(MAX_EFB_TRANS, "");
    }

    public ISys.ScreenLockTime getScreenLockTime() {
        return ISys.ScreenLockTime.getLockTime(overrideParamsPref.getString(SCREEN_LOCK_TIME, ""));
    }

    public String getMotoRefundPassword() {
        return overrideParamsPref.getString(MOTO_REFUND_PASSWORD, "");
    }

    public String getRefundPassword() {
        return overrideParamsPref.getString(REFUND_PASSWORD, "");
    }

    public String getOverrideCtlsCvmLimit() {
        return hotloadParamsPref.getString(OVERRIDE_CTLS_CVM_LIMIT, "");
    }

    public String getOverrideCtlsCvmLimitEnabled() {
        return hotloadParamsPref.getString(OVERRIDE_CTLS_CVM_LIMIT_ENABLED, "");
    }

    public String getBrandDisplayLogoHeader() {
        return overrideParamsPref.getString(BRAND_DISPLAY_LOGO_HEADER, "");
    }

    public String getBrandDisplayLogoIdle() {
        return overrideParamsPref.getString(BRAND_DISPLAY_LOGO_IDLE, "");
    }

    public String getBrandDisplayLogoSplash() {
        return overrideParamsPref.getString(BRAND_DISPLAY_LOGO_SPLASH, "");
    }

    public String getBrandDisplayStatusBarColour() {
        return overrideParamsPref.getString(BRAND_DISPLAY_STATUS_BAR_COLOUR, "");
    }

    public String getBrandDisplayButtonColour() {
        return overrideParamsPref.getString(BRAND_DISPLAY_BUTTON_COLOUR, "");
    }

    public String getBrandDisplayButtonTextColour() {
        return overrideParamsPref.getString(BRAND_DISPLAY_BUTTON_TEXT_COLOUR, "");
    }

    public String getBrandDisplayPrimaryColour() {
        return overrideParamsPref.getString(BRAND_DISPLAY_PRIMARY_COLOUR, "");
    }

    public String getBrandReceiptLogoHeader() {
        return overrideParamsPref.getString(BRAND_RECEIPT_LOGO_HEADER, "");
    }

    public String getAmexSellerId() {
        return overrideParamsPref.getString(AMEX_SELLER_ID, "");
    }

    public String getAmexSellerEmail() {
        return overrideParamsPref.getString(AMEX_SELLER_EMAIL, "");
    }

    public String getAmexSellerTelephone() {
        return overrideParamsPref.getString(AMEX_SELLER_TELEPHONE, "");
    }

    public String getAmexPaymentFacilitator() {
        return overrideParamsPref.getString(AMEX_PAYMENT_FACILITATOR, "");
    }

    public String getCardProductFile() {
        return overrideParamsPref.getString(CARD_PRODUCT_FILE, "");
    }

    public String getCfgEmvFile() {
        return overrideParamsPref.getString(CFG_EMV_FILE, "");
    }

    public String getCfgCtlsFile() {
        return overrideParamsPref.getString(CFG_CTLS_FILE, "");
    }

    public String getBlacklistFile() {
        return overrideParamsPref.getString(BLACKLIST_FILE, "");
    }

    public String getEpatFile() {
        return overrideParamsPref.getString(EPAT_FILE, "");
    }

    public String getPktFile() {
        return overrideParamsPref.getString(PKT_FILE, "");
    }

    public String getCardsFile() {
        return overrideParamsPref.getString(CARDS_FILE, "");
    }

    public UiConfigTimeouts getUiConfigTimeouts() {
        return new UiConfigTimeouts(overrideParamsPref, ACCESS_MODE_SUFFIX);
    }

    public String getPasswordRetryLimit() {
        return overrideParamsPref.getString(PASSWORD_RETRY_LIMIT, "");
    }

    public String getPasswordAttemptWindow() {
        return overrideParamsPref.getString(PASSWORD_ATTEMPT_WINDOW, "");
    }

    public String getPasswordAttemptLockoutDuration() {
        return overrideParamsPref.getString(PASSWORD_ATTEMPT_LOCKOUT_DURATION, "");
    }

    public String getPasswordMaximumAge() {
        return overrideParamsPref.getString(PASSWORD_MAXIMUM_AGE, "");
    }

    public String getRefundPasswordRetryLimit() {
        return overrideParamsPref.getString(PASSWORD_RETRY_LIMIT1, "");
    }

    public String getMotoPasswordRetryLimit() {
        return overrideParamsPref.getString(PASSWORD_RETRY_LIMIT2, "");
    }

    public String getRrnRetryLimit() {
        return overrideParamsPref.getString(RRN_RETRY_LIMIT, "");
    }

    public String getAuthcodeRetryLimit() {
        return overrideParamsPref.getString(AUTHCODE_RETRY_LIMIT, "");
    }

    public String getPciRebootTime() {
        return overrideParamsPref.getString(PCI_REBOOT_TIME, "");
    }

    public boolean isAutoSettlementEnabled() {
        return overrideParamsPref.getBoolean(AUTO_SETTLEMENT_ENABLED, false);
    }

    public String getAutoSettlementRetryCount() {
        return overrideParamsPref.getString(AUTO_SETTLEMENT_RETRY_COUNT, "");
    }

    public boolean isAutoSettlementPrintTransactionListing() {
        return overrideParamsPref.getBoolean(AUTO_SETTLEMENT_PRINT_TRANSACTION_LISTING, false);
    }

    public String getAutoSettlementTime() {
        return overrideParamsPref.getString(AUTO_SETTLEMENT_TIME, "");
    }

    public String getAutoSettlementTimeWindow() {
        return overrideParamsPref.getString(AUTO_SETTLEMENT_TIME_WINDOW, "");
    }

    public String getAutoSettlementIdlingPeriod() {
        return overrideParamsPref.getString(AUTO_SETTLEMENT_IDLING_PERIOD, "");
    }

    public String getMaxDaysTransactionsToStore() {
        return overrideParamsPref.getString(MAX_DAYS_TRANSACTIONS_TO_STORE, "");
    }

    public String getIinranges(int cardIndex) {
        String iinRanges = cards.get(cardIndex).getIinRange();
        // only allow test ranges in non-production builds
        if (!BuildConfig.BUILD_TYPE_PRODUCTION) {
            String testIinRanges = cards.get(cardIndex).getTestIinRange();
            if (testIinRanges != null) {
                return iinRanges + "," + testIinRanges;
            }
        }
        return iinRanges;
    }
    public String getAcquirerInstitutionId() {
        return initialParamsPref.getString(ACQUIRER_INSTITUTION_ID, "");
    }

    public String getAcquirerId(String cardSchemeId, String departmentId ) {
        return null;
    }


    public String getTerminalIdentifier(String cardSchemeId, String departmentId ) {
        return null;
    }

    public String getMerchantNumber(String cardSchemeId, String departmentId ) {
        return null;
    }

    public boolean hasMerchantAgreement(String cardSchemeId, String departmentId ) {
        return false;
    }

    public boolean isDemo() {
        return overrideParamsPref.getString(CUSTOMER_NAME, "").contains( "Demo" );
    }

    public CountryCode getCountryCode() {
        return ISOCountryCodes.getInstance().getCountryFrom3Num(getCurrencyNum() + "");
    }

    private boolean isValidSmtpServer() {
        return !Util.isNullOrEmpty( overrideParamsPref.getString(MAIL_HOST, "") ) &&
                !Util.isNullOrEmpty( overrideParamsPref.getString(MAIL_PORT, "")) &&
                !Util.isNullOrEmpty( overrideParamsPref.getString(MAIL_USER, "")) &&
                !Util.isNullOrEmpty( overrideParamsPref.getString(MAIL_PASSWORD, "" )) &&
                !Util.isNullOrEmpty( overrideParamsPref.getString(MAIL_SENDER, "" ));
    }

    public boolean isMailEnabled() {
        return isMailMerchant() && !Util.isNullOrEmpty( overrideParamsPref.getString(MAIL_MERCHANT_ADDRESS, "" )) && isValidSmtpServer();
    }

    public boolean isSignatureSupported() {
        return MalFactory.getInstance().getHardware().hasPrinter() && hotloadParamsPref.getBoolean(SIGNATURE_SUPPORTED, false);
    }


    private static int getColourOrDefault( String colorValue, int defaultColour) {
        int colour = defaultColour;
        try {
            if (!Util.isNullOrEmpty(colorValue)) {
                colour = Color.parseColor("#" + colorValue);
                if (colour == Color.WHITE) {
                    colour = defaultColour;
                }
            }
        } catch (IllegalArgumentException ignored) {
            Timber.e(ignored);
        }
        return colour;
    }

    /***
     * Required as some config values input an empty string.
     * The shared preference default value only returns no value if the data doesn't contain the id.
     * When changed to kotlin could make this an extension function.
     * @param preferences config for preferences
     * @param id Id of our value
     * @param defaultValue our fallback value
     * @return either the default value if not in map or empty string or the value stored in shared preferences
     */
    private String configTryOrReturnDefault(SharedPreferences preferences, String id, String defaultValue) {
        if(preferences.getString(id, defaultValue).isEmpty()) {
            return defaultValue;
        }
        // Return the normal value
        return preferences.getString(id, defaultValue);
    }

    public int getBrandDisplayStatusBarColourOrDefault(int defaultColour) {
        return getColourOrDefault(overrideParamsPref.getString(BRAND_DISPLAY_STATUS_BAR_COLOUR, ""), getBrandDisplayPrimaryColourOrDefault(defaultColour));
    }

    public int getBrandDisplayButtonColourOrDefault(int defaultColour) {
        return getColourOrDefault(overrideParamsPref.getString(BRAND_DISPLAY_BUTTON_COLOUR, ""), getBrandDisplayPrimaryColourOrDefault(defaultColour));
    }

    public int getBrandDisplayButtonTextColourOrDefault() {
        return getColourOrDefault(overrideParamsPref.getString(BRAND_DISPLAY_BUTTON_TEXT_COLOUR, ""), Color.WHITE);
    }

    public int getBrandDisplayPrimaryColourOrDefault(int defaultColour) {
        return getColourOrDefault( overrideParamsPref.getString(BRAND_DISPLAY_PRIMARY_COLOUR, ""), defaultColour);
    }

    public String getBrandDisplayLogoHeaderOrDefault() {
        // Check branding Header exists. If not, return default provided by RES app
        if (Engine.getCustomer() != null && Engine.getCustomer().hideBrandDisplayLogoHeader()) {
            return overrideParamsPref.getString(BRAND_DISPLAY_LOGO_HEADER, "");
        }
        return overrideParamsPref.getString(BRAND_DISPLAY_LOGO_HEADER, "header.png");
    }

    public String getBrandDisplayLogoIdleOrDefault() {
        return configTryOrReturnDefault(overrideParamsPref, BRAND_DISPLAY_LOGO_IDLE, "screensaver.png");
    }

    public String getBrandDisplayLogoSplashOrDefault() {
        return configTryOrReturnDefault(overrideParamsPref, BRAND_DISPLAY_LOGO_SPLASH, "splashlogo.png");
    }

    public String getBrandReceiptLogoHeaderOrDefault() {
        // Check branding Splash logo exists. If not, return default provided by RES app
        return configTryOrReturnDefault(overrideParamsPref, BRAND_RECEIPT_LOGO_HEADER, "receipt.bmp");
    }

    public boolean isUseCustomAudioForResult() {
        return hotloadParamsPref.getBoolean(USE_CUSTOM_AUDIO_FOR_RESULT, true);
    }

    @Override
    public void setCardProductVersion(int cardProductVersion) {
        overrideParamsPref.edit()
                .putInt(CARD_PRODUCT_VERSION, cardProductVersion)
                .apply();
    }

    private List<CardProductCfg> cards;

    @Override
    public void setCards(List<CardProductCfg> newCards) {
        cards = newCards;
        if(newCards != null) {
            Gson gson = new Gson();
            String data = gson.toJson(newCards);
            overrideParamsPref.edit().putString(CARDS, data).apply();
        }
    }

    private void loadCards() {
        if(cards == null) {
            String cardData = overrideParamsPref.getString(CARDS, "");
            if(!cardData.isEmpty()) {
                Gson gson = new Gson();
                Type cardProductCfgListType = new TypeToken<ArrayList<CardProductCfg>>(){}.getType();
                cards = gson.fromJson(cardData, cardProductCfgListType);
            }
        }
    }

    public boolean isReferenceEnabled(int refRequired) {
        return refRequired == OPTIONAL.getReferenceCode() || refRequired == MANDATORY.getReferenceCode();
    }

    public boolean isReferenceAllowed(int refRequired) {
        return refRequired != DISABLED.getReferenceCode();
    }

    public boolean isReferenceMandatory(int refRequired) {
        return refRequired == MANDATORY.getReferenceCode();
    }

    public boolean isReferenceOptional(int refRequired) {
        return refRequired == OPTIONAL.getReferenceCode();
    }

    //----------------------- Setters Required in which dynamic modification is required --------------------------------
    public void setPasswordRetryLimit(String passwordRetryLimit) {
        overrideParamsPref.edit().putString(PASSWORD_RETRY_LIMIT, passwordRetryLimit).apply();
    }

    public void setPasswordAttemptWindow(String passwordAttemptWindow) {
        overrideParamsPref.edit().putString(PASSWORD_ATTEMPT_WINDOW, passwordAttemptWindow).apply();
    }

    public void setPasswordAttemptLockoutDuration(String passwordAttemptLockoutDuration) {
        overrideParamsPref.edit().putString(PASSWORD_ATTEMPT_LOCKOUT_DURATION, passwordAttemptLockoutDuration).apply();
    }

    public void setAutoUserLogin(boolean autoUserLogin) {
        overrideParamsPref.edit().putBoolean(AUTO_USER_LOGIN, autoUserLogin).apply();
    }

    public void setLoyaltySupported(boolean loyaltySupported) {
        overrideParamsPref.edit().putBoolean(LOYALTY_SUPPORTED, loyaltySupported).apply();
    }

    public void setStid(String stid) {
        initialParamsPref.edit().putString(STID, stid).apply();
    }

    public void setMid(String mid) {
        initialParamsPref.edit().putString(MID, mid).apply();
    }

    @Override
    public void setUsePercentageTip(boolean usePercentageTip) {
        overrideParamsPref.edit().putBoolean(USE_PERCENTAGE_TIP, usePercentageTip).apply();
    }

    @Override
    public void setIncludedOrginalStandInRec(boolean includedOrginalStandInRec) {
        overrideParamsPref.edit()
                .putBoolean(INCLUDED_ORGINAL_STAND_IN_REC, includedOrginalStandInRec)
                .apply();
    }

    public void setReversalCopyOriginal(boolean reversalCopyOriginal) {
        overrideParamsPref.edit()
                .putBoolean(REVERSAL_COPY_ORIGINAL, reversalCopyOriginal)
                .apply();
    }

    public void debugAllParams() {
        debugParams("Override Params", overrideParamsPref);
        debugParams("Hotload Params", hotloadParamsPref);
        debugParams("Initial Params", initialParamsPref);
    }

    private void debugParams(String paramsName, SharedPreferences preferences) {
        // Get all keys
        Map<String, ?> allEntries = preferences.getAll();

        Timber.e("-------- %s: %d--------", paramsName, allEntries.size());
        // Iterate through the entries and log key-value pairs
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            // Log key-value pairs
            Timber.e("paramsName %s", "Key: " + key + ", Value: " + value.toString());
        }
    }
}
