package com.linkly.libengine.config.paycfg;

import static com.linkly.libmal.idal.ISys.ScreenLockTime.THIRTY_SEC;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.linkly.libconfig.HotLoadParameters;
import com.linkly.libconfig.InitialParameters;
import com.linkly.libconfig.OverrideParameters;
import com.linkly.libconfig.cpat.CardProductCfg;
import com.linkly.libconfig.uiconfig.ConfigTimeouts;
import com.linkly.libengine.config.CdoAllowed;
import com.linkly.libengine.config.IssuerCfg;
import com.linkly.libengine.env.IdentityEnvVar;
import com.linkly.libmal.IMal;
import com.linkly.libmal.IMalFile;
import com.linkly.libmal.IMalHardware;
import com.linkly.libmal.MalFactory;
import com.linkly.libpositive.wrappers.Surcharge;
import com.stanfy.gsonxml.GsonXml;
import com.stanfy.gsonxml.GsonXmlBuilder;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class PayCfgImplTest {
    private Context mockedContext = mock(Context.class);
    private MockedStatic<MalFactory> iMalMockedStatic  = Mockito.mockStatic(MalFactory.class);
    private MockedShared prefOverride = new MockedShared();
    private MockedShared prefInitParams = new MockedShared();
    private MockedShared prefHotload = new MockedShared();

    static IMal mockedMal = mock(IMal.class);
    GsonXml gsonXml = new GsonXmlBuilder()
            .setXmlParserCreator(
                    () -> {
                        try {
                            return XmlPullParserFactory.newInstance().newPullParser();
                        } catch (XmlPullParserException e) {
                            throw new RuntimeException(e);
                        }
                    }
            )
            .create();


    @Before
    public void setup() {
        prefOverride = new MockedShared();
        prefInitParams = new MockedShared();
        prefHotload = new MockedShared();

        iMalMockedStatic.when(MalFactory::getInstance).thenReturn(
                mockedMal
        );

        // Required for some of the issues that happen below.
        IMalFile mockedMalFile = mock(IMalFile.class);
        when(mockedMal.getFile()).thenReturn(mockedMalFile);
        when(mockedMalFile.getCommonDir()).thenReturn("");
        when(mockedMalFile.fileExist(anyString())).thenReturn(false);

        when(mockedContext.getSharedPreferences(eq("overrideparams"), anyInt())).thenReturn(prefOverride);
        when(mockedContext.getSharedPreferences(eq("initialparams"), anyInt())).thenReturn(prefInitParams);
        when(mockedContext.getSharedPreferences(eq("hotloadparams"), anyInt())).thenReturn(prefHotload);
    }

    @After
    public void tearDown(){
        iMalMockedStatic.close();
    }

    // Dummy XML
    String initialXML = "<parameter xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
            "    xsi:noNamespaceSchemaLocation='../../../payment/src/main/assets/initialparams.xsd'>\n" +
            "    <!-- TID & MID Settings -->\n" +
            "    <!-- Make sure to use your own config please! -->\n" +
            "    <stid>TI530011</stid>\n" +
            "    <mid>800000050000101</mid>\n" +
            "    <!-- Terminal Location   -->\n" +
            "    <terminalTimeZone>Australia/Sydney</terminalTimeZone>\n" +
            "    <!--  Non-TMS Params  -->\n" +
            "    <bankTimeZone>Australia/Sydney</bankTimeZone>\n" +
            "</parameter>";


    @Test
    public void initialParam() {
        PayCfgImpl payCfg = new PayCfgImpl(mockedContext);

        InitialParameters params = gsonXml.fromXml(initialXML, InitialParameters.class);

        MockedStatic<IdentityEnvVar> mockedIdentityEnvVar = Mockito.mockStatic(IdentityEnvVar.class);
        mockedIdentityEnvVar.when(IdentityEnvVar::getMid).thenReturn( "");
        mockedIdentityEnvVar.when(IdentityEnvVar::getTid).thenReturn( "");

        payCfg.loadInitialParams(params, 1);

        assertEquals("TI530011", payCfg.getStid());
        assertEquals("800000050000101", payCfg.getMid());
        assertEquals("Australia/Sydney", payCfg.getTerminalTimeZone());
        assertEquals("Australia/Sydney", payCfg.getBankTimeZone());
    }


    String hotloadXML = "<parameter>\n" +
            "    <!-- Merchant Receipt Lines   -->\n" +
            "    <receipt_merchant_line0>Merchant Line 0</receipt_merchant_line0>\n" +
            "    <receipt_merchant_line1>Merchant Line 1</receipt_merchant_line1>\n" +
            "    <receipt_merchant_line2>Merchant Line 2</receipt_merchant_line2>\n" +
            "    <receipt_merchant_line3>Merchant Line 3</receipt_merchant_line3>\n" +
            "    <receipt_merchant_line4>Merchant Line 4</receipt_merchant_line4>\n" +
            "    <receipt_merchant_line5>Merchant Line 5</receipt_merchant_line5>\n" +
            "    <receipt_merchant_line6>Powered by Linkly</receipt_merchant_line6>\n" +
            "    <footerLine1>footer1</footerLine1>\n" +
            "    <footerLine2>footer2</footerLine2>\n" +
            "\t\n" +
            "\t<!-- MCR Settings   -->\n" +
            "    <mcrEnabled>Y</mcrEnabled>\n" +
            "    <mcrLimit>1000</mcrLimit>\n" +
            "    <mcrUpperLimit>2000</mcrUpperLimit>\n" +
            "\t\n" +
            "\t<!--  Terminal Limits  -->\n" +
            "    <saleLimitCents>1000000</saleLimitCents>\n" +
            "    <cashoutLimitCents>10</cashoutLimitCents>\n" +
            "    <maxRefundLimit>20</maxRefundLimit> \n" +
            "    <managerRefundLimit>30</managerRefundLimit>\n" +
            "    <maxRefundCount>40</maxRefundCount>\n" +
            "    <maxCumulativeRefundLimit>50</maxCumulativeRefundLimit>\n" +
            "    <maxTipPercent>60</maxTipPercent>\n" +
            "    <maxPreAuthTrans>100</maxPreAuthTrans>\n" +
            "    <maxEfbTrans>200</maxEfbTrans>\n" +
            "    <preAuthLimitCents>10000</preAuthLimitCents>\n" +
            "    <overrideCtlsCvmLimitEnabled>N</overrideCtlsCvmLimitEnabled>\n" +
            "    <overrideCtlsCvmLimit>10</overrideCtlsCvmLimit>\n" +
            "\n" +
            "    <!--Terminal Configuration-->\n" +
            "    <showReceiptPromptForAuto>Y</showReceiptPromptForAuto>\n" +
            "    <printCustomerReceipt>ASK</printCustomerReceipt>\n" +
            "    <motoPasswordPrompt>Y</motoPasswordPrompt>\n" +
            "    <refundPasswordPrompt>Y</refundPasswordPrompt>\n" +
            "    <motoCVVEntry>Y</motoCVVEntry>\n" +
            "    <motoCVVEntryBypassAllowed>Y</motoCVVEntryBypassAllowed>\n" +
            "    <mailOrder>Y</mailOrder>\n" +
            "    <telephone>Y</telephone>\n" +
            "    <preauth>Y</preauth>\n" +
            "    <preauthCreditAccountOnly>Y</preauthCreditAccountOnly>\n" +
            "    <cashback>Y</cashback>\n" +
            "    <cashout>Y</cashout>\n" +
            "    <refund>Y</refund>\n" +
            "    <reversal>Y</reversal>\n" +
            "    <tipAllowed>Y</tipAllowed>\n" +
            "    <custRefRequired>DISABLED</custRefRequired>\n" +
            "    <emvSupported>Y</emvSupported>\n" +
            "    <cardholderPresent>Y</cardholderPresent>\n" +
            "    <contactlessSupported>Y</contactlessSupported>\n" +
            "    <loyaltySupported>N</loyaltySupported>\n" +
            "    <version>1.00.00</version>\n" +
            "    <efbSupported>Y</efbSupported>\n" +
            "    <efbAcknowledgeServiceCode>Y</efbAcknowledgeServiceCode>\n" +
            "    <efbPlasticCardLifeDays>1000</efbPlasticCardLifeDays>\n" +
            "    <efbRefundAllowed>N</efbRefundAllowed>\n" +
            "    <efbCashoutAllowed>N</efbCashoutAllowed>\n" +
            "    <efbContinueInFallbackTimeoutMinutes>10</efbContinueInFallbackTimeoutMinutes>\n" +
            "    <efbAuthNumberOverFloorLimitAllowed>N</efbAuthNumberOverFloorLimitAllowed>\n" +
            "    <accessMode>N</accessMode>\n" +
            "    <emvFallback>Y</emvFallback>\n" +
            "    <msrAllowed>Y</msrAllowed>\n" +
            "    <refundSecure>N</refundSecure>\n" +
            "    <signatureSupported>Y</signatureSupported>\n" +
            "\t<useCustomAudioForResult>N</useCustomAudioForResult>\n" +
            "\t\n" +
            "\n" +
            "    <!-- Surcharge Settings -->\n" +
            "    <surchargeSupported>Y</surchargeSupported>\n" +
            "    <sc_eftpos_on>Y</sc_eftpos_on>\n" +
            "    <sc_eftpos_type>$</sc_eftpos_type>\n" +
            "    <sc_eftpos_amount>1</sc_eftpos_amount>\n" +
            "\n" +
            "    <sc_mastercard_credit_on>Y</sc_mastercard_credit_on>\n" +
            "    <sc_mastercard_credit_type>%</sc_mastercard_credit_type>\n" +
            "    <sc_mastercard_credit_amount>2</sc_mastercard_credit_amount>\n" +
            "\n" +
            "    <sc_mastercard_debit_on>Y</sc_mastercard_debit_on>\n" +
            "    <sc_mastercard_debit_type>%</sc_mastercard_debit_type>\n" +
            "    <sc_mastercard_debit_amount>3</sc_mastercard_debit_amount>\n" +
            "\n" +
            "    <sc_visa_credit_on>Y</sc_visa_credit_on>\n" +
            "    <sc_visa_credit_type>%</sc_visa_credit_type>\n" +
            "    <sc_visa_credit_amount>4</sc_visa_credit_amount>\n" +
            "\n" +
            "    <sc_visa_debit_on>Y</sc_visa_debit_on>\n" +
            "    <sc_visa_debit_type>%</sc_visa_debit_type>\n" +
            "    <sc_visa_debit_amount>5</sc_visa_debit_amount>\n" +
            "\n" +
            "    <sc_amex_on>Y</sc_amex_on>\n" +
            "    <sc_amex_type>%</sc_amex_type>\n" +
            "    <sc_amex_amount>6</sc_amex_amount>\n" +
            "\n" +
            "    <sc_diners_club_on>Y</sc_diners_club_on>\n" +
            "    <sc_diners_club_type>%</sc_diners_club_type>\n" +
            "    <sc_diners_club_amount>7</sc_diners_club_amount>\n" +
            "\n" +
            "    <sc_jcb_on>Y</sc_jcb_on>\n" +
            "    <sc_jcb_type>%</sc_jcb_type>\n" +
            "    <sc_jcb_amount>8</sc_jcb_amount>\n" +
            "\n" +
            "    <sc_unionpay_credit_on>Y</sc_unionpay_credit_on>\n" +
            "    <sc_unionpay_credit_type>%</sc_unionpay_credit_type>\n" +
            "    <sc_unionpay_credit_amount>9</sc_unionpay_credit_amount>\n" +
            "\n" +
            "</parameter>\n";

    @Test
    public void testReadHotloadParamFields() {
        PayCfgImpl payCfg = new PayCfgImpl(mockedContext);
        HotLoadParameters params = gsonXml.fromXml(hotloadXML, HotLoadParameters.class);

        IMalHardware hardware = mock(IMalHardware.class);
        when(mockedMal.getHardware()).thenReturn(hardware);
        when(hardware.hasPrinter()).thenReturn(true);

        payCfg.loadHotloadParams(params, 1);

        assertEquals("Merchant Line 0", payCfg.getReceipt().getMerchant().getLine0());
        assertEquals("Merchant Line 1", payCfg.getReceipt().getMerchant().getLine1());
        assertEquals("Merchant Line 2", payCfg.getReceipt().getMerchant().getLine2());
        assertEquals("Merchant Line 3", payCfg.getReceipt().getMerchant().getLine3());
        assertEquals("Merchant Line 4", payCfg.getReceipt().getMerchant().getLine4());
        assertEquals("Merchant Line 5", payCfg.getReceipt().getMerchant().getLine5());
        assertEquals("Powered by Linkly", payCfg.getReceipt().getMerchant().getLine6());
        assertEquals("footer1", payCfg.getReceipt().getFooter().getLine1());
        assertEquals("footer2", payCfg.getReceipt().getFooter().getLine2());
        assertEquals(true, payCfg.isMcrEnabled());
        assertEquals(1000, payCfg.getMcrLimit());
        assertEquals(2000, payCfg.getMcrUpperLimit());
        assertEquals(1000000, payCfg.getSaleLimitCents());
        assertEquals(10, payCfg.getCashoutLimitCents());
        assertEquals("20", payCfg.getMaxRefundLimit());
        assertEquals("30", payCfg.getManagerRefundLimit());
        assertEquals("40", payCfg.getMaxRefundCount());
        assertEquals("50", payCfg.getMaxCumulativeRefundLimit());
        assertEquals("60", payCfg.getMaxTipPercent());
        assertEquals("100", payCfg.getMaxPreAuthTrans());
        assertEquals("200", payCfg.getMaxEfbTrans());
        assertEquals(10000, payCfg.getPreAuthLimitCents());
        assertEquals("N", payCfg.getOverrideCtlsCvmLimitEnabled());
        assertEquals("10", payCfg.getOverrideCtlsCvmLimit());
        assertEquals(true, payCfg.isShowReceiptPromptForAuto());
        assertEquals("ASK", payCfg.getPrintCustomerReceipt());
        assertEquals(true, payCfg.isMotoPasswordPrompt());
        assertEquals(true, payCfg.isRefundPasswordPrompt());
        assertEquals(true, payCfg.isMotoCVVEntry());
        assertEquals(true, payCfg.isMotoCVVEntryBypassAllowed());
        assertEquals(true, payCfg.isMailOrder());
        assertEquals(true, payCfg.isTelephone());
        assertEquals(true, payCfg.isPreauth());
        assertEquals(true, payCfg.isPreauthCreditAccountOnly());
        assertEquals(true, payCfg.isCashback());
        assertEquals(true, payCfg.isCashout());
        assertEquals(true, payCfg.isRefund());
        assertEquals(true, payCfg.isReversal());
        assertEquals(true, payCfg.isTipAllowed());
        assertEquals(2, payCfg.getCustRefRequired());
        assertEquals(true, payCfg.isEmvSupported());
        assertEquals(true, payCfg.isCardholderPresent());
        assertEquals(true, payCfg.isContactlessSupported());
        assertEquals(false, payCfg.isLoyaltySupported());
        assertEquals(true, payCfg.isEfbSupported());
        assertEquals(true, payCfg.isEfbAcknowledgeServiceCode());
        assertEquals(1000, payCfg.getEfbPlasticCardLifeDays());
        assertEquals(false, payCfg.isEfbRefundAllowed());
        assertEquals(false, payCfg.isEfbCashoutAllowed());
        assertEquals(10, payCfg.getEfbContinueInFallbackTimeoutMinutes());
        assertEquals(false, payCfg.isEfbAuthNumberOverFloorLimitAllowed());
        assertEquals(false, payCfg.isAccessMode());
        assertEquals(true, payCfg.isEmvFallback());
        assertEquals(true, payCfg.isMsrAllowed());
        assertEquals(false, payCfg.isRefundSecure());
        assertEquals(true, payCfg.isSignatureSupported());
        assertEquals(false, payCfg.isUseCustomAudioForResult());
    }


    String overrideXML = "<parameter xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
            "    xsi:noNamespaceSchemaLocation='../../../payment/src/main/assets/overrideparams.xsd'>\n" +
            "    <!-- Amex parameters -->\n" +
            "    <seller_id>123456789</seller_id>\n" +
            "    <seller_email>myemail@google.com</seller_email>\n" +
            "    <seller_telephone>61419403298</seller_telephone>\n" +
            "    <payment_facilitator>Till</payment_facilitator>\n" +
            "\n" +
            "    <!--Manager Login Details-->\n" +
            "    <loginManagerUserName>MANAGER</loginManagerUserName>\n" +
            "    <loginManagerUserId>1234</loginManagerUserId>\n" +
            "    <loginManagerInitialPwd>567890</loginManagerInitialPwd>\n" +
            "    <motoRefundPassword>666666</motoRefundPassword>\n" +
            "    <refundPassword>555555</refundPassword>\n" +
            "\n" +
            "    <!--Technician Login Details-->\n" +
            "    <loginTechnicianUserName>TECHNICIAN</loginTechnicianUserName>\n" +
            "    <loginTechnicianUserId>1357</loginTechnicianUserId>\n" +
            "    <loginTechnicianInitialPwd>024680</loginTechnicianInitialPwd>\n" +
            "\n" +
            "    <screenLockTime>30 secs</screenLockTime>\n" +
            "    <pciRebootTime>0235</pciRebootTime>\n" +
            "    <autoSettlementEnabled>Y</autoSettlementEnabled>\n" +
            "    <autoSettlementTime>0000</autoSettlementTime>\n" +
            "    <autoSettlementTimeWindow>60</autoSettlementTimeWindow>\n" +
            "    <autoSettlementRetryCount>3</autoSettlementRetryCount>\n" +
            "    <autoSettlementPrintTransactionListing>Y</autoSettlementPrintTransactionListing>\n" +
            "    <autoSettlementIdlingPeriod>30</autoSettlementIdlingPeriod>\n" +
            "    <maxDaysTransactionsToStore>7</maxDaysTransactionsToStore>\n" +
            "\n" +
            "    <!-- Passcode Security -->\n" +
            "    <passcodeSecuritySupported>Y</passcodeSecuritySupported>\n" +
            "    <passwordRetryLimit>6</passwordRetryLimit>\n" +
            "    <passwordAttemptWindow>30</passwordAttemptWindow>\n" +
            "    <passwordAttemptLockoutDuration>30</passwordAttemptLockoutDuration>\n" +
            "    <passwordMaximumAge>30</passwordMaximumAge>\n" +
            "    <refundPasswordRetryLimit>3</refundPasswordRetryLimit>\n" +
            "    <motoPasswordRetryLimit>3</motoPasswordRetryLimit>\n" +
            "    <rrnRetryLimit>3</rrnRetryLimit>\n" +
            "    <authcodeRetryLimit>3</authcodeRetryLimit>\n" +
            "\n" +
            "    <!--  Localisation  -->\n" +
            "    <currencyCode>AUD</currencyCode>\n" +
            "    <currencyNum>036</currencyNum>\n" +
            "    <countryNum>036</countryNum>\n" +
            "    <language>en_AU</language>\n" +
            "\n" +
            "    <!-- Payment Switch Settings -->\n" +
            "    <paymentSwitch_commsType>TWO_BYTE_LENGTH_FIVE_BYTE_TPDU_HEADER_TCP_DIRECT</paymentSwitch_commsType>\n" +
            "            <!-- **EFTEX local host emulator** -->\n" +
            "    <posCommsEnabled>Y</posCommsEnabled>\n" +
            "    <posCommsHostId>AAA</posCommsHostId>\n" +
            "    <posCommsInterfaceType>2</posCommsInterfaceType>\n" +
            "    <paymentSwitch_useSsl>Y</paymentSwitch_useSsl>\n" +
            "    <paymentSwitch_ip_host>20.92.92.59:2009</paymentSwitch_ip_host>\n" +
            "    <paymentSwitch_ip_host2nd>20.92.92.60:2009</paymentSwitch_ip_host2nd>\n" +
            "\n" +
            "    <paymentSwitch_aiic>00000407642</paymentSwitch_aiic>\n" +
            "    <paymentSwitch_nii>001</paymentSwitch_nii>\n" +
            "    <paymentSwitch_receiveTimeout>20</paymentSwitch_receiveTimeout>\n" +
            "    <paymentSwitch_dialTimeout>10</paymentSwitch_dialTimeout>\n" +
            "    <paymentSwitch_reversalAdviceTimeout>10</paymentSwitch_reversalAdviceTimeout>\n" +
            "    <paymentSwitch_disableSecurity>Y</paymentSwitch_disableSecurity>\n" +
            "\n" +
            "\n" +
            "        <!--**Non-TMS Config**-->\n" +
            "    <customerName>Till</customerName>\n" +
            "    <bankDescription>TILL EFTPOS</bankDescription>\n" +
            "    <retailerName>TILL RETAILER</retailerName>\n" +
            "    <commsFallbackEnabled>Y</commsFallbackEnabled>\n" +
            "    <commsFallbackHost>www.google.com:80</commsFallbackHost>\n" +
            "    <paxstoreUpload>Y</paxstoreUpload>\n" +
            "\n" +
            "    <!-- Issuers - defaults for scheme enabled flags -->\n" +
            "    <issuer1Name>VISA</issuer1Name>\n" +
            "    <issuer1Enabled>Y</issuer1Enabled>\n" +
            "    <issuer2Name>MASTERCARD</issuer2Name>\n" +
            "    <issuer2Enabled>Y</issuer2Enabled>\n" +
            "    <issuer3Name>EFTPOS</issuer3Name>\n" +
            "    <issuer3Enabled>Y</issuer3Enabled>\n" +
            "    <issuer4Name>AMEX</issuer4Name>\n" +
            "    <issuer4Enabled>Y</issuer4Enabled>\n" +
            "    <issuer5Name>DINERS</issuer5Name>\n" +
            "    <issuer5Enabled>Y</issuer5Enabled>\n" +
            "    <issuer6Name>JCB</issuer6Name>\n" +
            "    <issuer6Enabled>Y</issuer6Enabled>\n" +
            "    <issuer7Name>UPI</issuer7Name>\n" +
            "    <issuer7Enabled>Y</issuer7Enabled>\n" +
            "\n" +
            "    <!--- Config Files -->\n" +
            "    <cardProductFile>cardproduct.json</cardProductFile>\n" +
            "    <cfgEmvFile>cfg_emv.json</cfgEmvFile>\n" +
            "    <cfgCtlsFile>cfg_ctls_emv.json</cfgCtlsFile>\n" +
            "    <blacklistFile>blacklist.json</blacklistFile>\n" +
            "\n" +
            "\n" +
            "    <!--  UI Timeouts  -->\n" +
            "    <amountEntryTimeoutSecs>60</amountEntryTimeoutSecs>\n" +
            "    <presentCardTimeoutSecs>60</presentCardTimeoutSecs>\n" +
            "    <accountSelectionTimeoutSecs>30</accountSelectionTimeoutSecs>\n" +
            "    <appSelectionTimeoutSecs>30</appSelectionTimeoutSecs>\n" +
            "    <cardPinEntryTimeoutSecs>60</cardPinEntryTimeoutSecs>\n" +
            "    <confirmSignatureTimeoutSecs>60</confirmSignatureTimeoutSecs>\n" +
            "    <customerReceiptPrintPromptTimeoutSecs>15</customerReceiptPrintPromptTimeoutSecs>\n" +
            "    <receiptRemovePromptTimeoutSecs>15</receiptRemovePromptTimeoutSecs>\n" +
            "    <cardRemovePromptTimeoutSecs>60</cardRemovePromptTimeoutSecs>\n" +
            "    <paperOutTimeoutSecs>120</paperOutTimeoutSecs>\n" +
            "    <decisionScreenTimeoutSecs>2</decisionScreenTimeoutSecs>\n" +
            "    <!--  Accessibility mode Timeouts  -->\n" +
            "    <accessModePresentCardTimeoutSecs>240</accessModePresentCardTimeoutSecs>\n" +
            "    <accessModeAccountSelectionTimeoutSecs>240</accessModeAccountSelectionTimeoutSecs>\n" +
            "    <accessModeAppSelectionTimeoutSecs>240</accessModeAppSelectionTimeoutSecs>\n" +
            "    <accessModePinEntryTimeoutSecs>240</accessModePinEntryTimeoutSecs>\n" +
            "    <accessModeDecisionScreenTimeoutSecs>5</accessModeDecisionScreenTimeoutSecs>\n" +
            "\n" +
            "    <!--  Offline mode configurations  -->\n" +
            "    <offlineFlightModeAllowed>Y</offlineFlightModeAllowed>\n" +
            "    <offlineTransactionCeilingLimitCentsContact>10000</offlineTransactionCeilingLimitCentsContact>\n" +
            "    <offlineTransactionCeilingLimitCentsContactless>5000</offlineTransactionCeilingLimitCentsContactless>\n" +
            "    <offlineSoftLimitAmountCents>80000</offlineSoftLimitAmountCents>\n" +
            "    <offlineSoftLimitCount>16</offlineSoftLimitCount>\n" +
            "    <offlineUpperLimitAmountCents>100000</offlineUpperLimitAmountCents>\n" +
            "    <offlineUpperLimitCount>20</offlineUpperLimitCount>\n" +
            "    <!--  Unattended mode configurations  -->\n" +
            "    <unattendedModeAllowed>Y</unattendedModeAllowed>\n" +
            "    <!-- CDO Settings -->\n" +
            "    <cdoAllowedEftpos>Y</cdoAllowedEftpos>\n" +
            "    <cdoAllowedMastercardCredit>Y</cdoAllowedMastercardCredit>\n" +
            "    <cdoAllowedMastercardDebit>Y</cdoAllowedMastercardDebit>\n" +
            "    <cdoAllowedVisaCredit>Y</cdoAllowedVisaCredit>\n" +
            "    <cdoAllowedVisaDebit>Y</cdoAllowedVisaDebit>\n" +
            "    <cdoAllowedAmex>Y</cdoAllowedAmex>\n" +
            "    <cdoAllowedDinersClub>Y</cdoAllowedDinersClub>\n" +
            "    <cdoAllowedJcb>Y</cdoAllowedJcb>\n" +
            "    <cdoAllowedUnionpayCredit>Y</cdoAllowedUnionpayCredit>\n" +
            "\n" +
            "    <!-- Preauth Settings -->\n" +
            "    <preauthExpiry_default>7</preauthExpiry_default>\n" +
            "    <preauthExpiry_eftpos>7</preauthExpiry_eftpos>\n" +
            "    <preauthExpiry_mastercard_credit>7</preauthExpiry_mastercard_credit>\n" +
            "    <preauthExpiry_mastercard_debit>7</preauthExpiry_mastercard_debit>\n" +
            "    <preauthExpiry_visa_credit>7</preauthExpiry_visa_credit>\n" +
            "    <preauthExpiry_visa_debit>7</preauthExpiry_visa_debit>\n" +
            "    <preauthExpiry_amex>7</preauthExpiry_amex>\n" +
            "    <preauthExpiry_diners_club>7</preauthExpiry_diners_club>\n" +
            "    <preauthExpiry_jcb>7</preauthExpiry_jcb>\n" +
            "    <preauthExpiry_unionpay_credit>7</preauthExpiry_unionpay_credit>\n" +
            "\n" +
            "    <!-- Branding - defaults are empty. Logo Files: filenames; Colors: RGB triplets (like \"AACC55\") -->\n" +
            "    <brandDisplayLogoHeader>000001</brandDisplayLogoHeader>\n" +
            "    <brandDisplayLogoIdle>000002</brandDisplayLogoIdle>\n" +
            "    <brandDisplayLogoSplash>000003</brandDisplayLogoSplash>\n" +
            "    <brandDisplayStatusBarColour>000004</brandDisplayStatusBarColour>\n" +
            "    <brandDisplayButtonColour>00BCB4</brandDisplayButtonColour>\n" +
            "    <brandDisplayButtonTextColour>000005</brandDisplayButtonTextColour>\n" +
            "    <brandDisplayPrimaryColour>FF591F</brandDisplayPrimaryColour>\n" +
            "    <brandReceiptLogoHeader>000006</brandReceiptLogoHeader>\n" +
            "\n" +
            "    <!-- Linkly Bin Number -->\n" +
            "    <bin_eftpos>01</bin_eftpos>\n" +
            "    <bin_mastercard_credit>03</bin_mastercard_credit>\n" +
            "    <bin_mastercard_debit>29</bin_mastercard_debit>\n" +
            "    <bin_visa_credit>04</bin_visa_credit>\n" +
            "    <bin_visa_debit>28</bin_visa_debit>\n" +
            "    <bin_amex>05</bin_amex>\n" +
            "    <bin_diners_club>06</bin_diners_club>\n" +
            "    <bin_jcb>09</bin_jcb>\n" +
            "    <bin_unionpay_credit>30</bin_unionpay_credit>\n" +
            "\n" +
            "</parameter>\n";




    @Test
    public void testOverrideParams() {
        PayCfgImpl params = new PayCfgImpl(mockedContext);
        OverrideParameters paramsOR = gsonXml.fromXml(overrideXML, OverrideParameters.class);


        params.loadOverrideParams("Random Customer 1",  paramsOR, 1);

        assertEquals("123456789", params.getAmexSellerId());
        assertEquals("myemail@google.com", params.getAmexSellerEmail());
        assertEquals("61419403298", params.getAmexSellerTelephone());
        assertEquals("Till", params.getAmexPaymentFacilitator());
        assertEquals("MANAGER", params.getLoginManagerUserName());
        assertEquals("1234", params.getLoginManagerUserId());
        assertEquals("567890", params.getLoginManagerInitialPwd());
        assertEquals("666666", params.getMotoRefundPassword());
        assertEquals("555555", params.getRefundPassword());
        assertEquals("TECHNICIAN", params.getLoginTechnicianUserName());
        assertEquals("1357", params.getLoginTechnicianUserId());
        assertEquals("024680", params.getLoginTechnicianInitialPwd());
        assertEquals(THIRTY_SEC, params.getScreenLockTime());
        assertEquals("0235", params.getPciRebootTime());
        assertEquals(true, params.isAutoSettlementEnabled());
        assertEquals("0000", params.getAutoSettlementTime());
        assertEquals("60", params.getAutoSettlementTimeWindow());
        assertEquals("3", params.getAutoSettlementRetryCount());
        assertEquals(true, params.isAutoSettlementPrintTransactionListing());
        assertEquals("30", params.getAutoSettlementIdlingPeriod());
        assertEquals("7", params.getMaxDaysTransactionsToStore());
        assertEquals(true, params.isPasscodeSecuritySupported());
        assertEquals("6", params.getPasswordRetryLimit());
        assertEquals("30", params.getPasswordAttemptWindow());
        assertEquals("30", params.getPasswordAttemptLockoutDuration());
        assertEquals("30", params.getPasswordMaximumAge());
        assertEquals("3", params.getRefundPasswordRetryLimit());
        assertEquals("3", params.getMotoPasswordRetryLimit());
        assertEquals("3", params.getRrnRetryLimit());
        assertEquals("3", params.getAuthcodeRetryLimit());
        assertEquals("AUD", params.getCurrencyCode());
        assertEquals(36, params.getCurrencyNum());
        assertEquals(36, params.getCountryNum());
        assertEquals("en_AU", params.getLanguage());
        assertEquals("TWO_BYTE_LENGTH_FIVE_BYTE_TPDU_HEADER_TCP_DIRECT", params.getPaymentSwitch().getCommsType());
        assertEquals(true, params.isPosCommsEnabled());
        assertEquals("AAA", params.getPosCommsHostId());
        assertEquals("2", params.getPosCommsInterfaceType());
        assertEquals(true, params.getPaymentSwitch().isUseSsl());
        assertEquals("20.92.92.59:2009", params.getPaymentSwitch().getIp().getHost());
        assertEquals("20.92.92.60:2009", params.getPaymentSwitch().getIp().getHost2nd());
        assertEquals("00000407642", params.getPaymentSwitch().getAiic());
        assertEquals("001", params.getPaymentSwitch().getNii());
        assertEquals(20, params.getPaymentSwitch().getReceiveTimeout());
        assertEquals(10, params.getPaymentSwitch().getDialTimeout());
        assertEquals(true, params.getPaymentSwitch().isDisableSecurity());
        assertEquals("Random Customer 1", params.getCustomerName());
        assertEquals("TILL EFTPOS", params.getBankDescription());
        assertEquals("TILL RETAILER", params.getRetailerName());
        assertEquals(true, params.isCommsFallbackEnabled());
        assertEquals("www.google.com:80", params.getCommsFallbackHost());
        assertEquals(true, params.isPaxstoreUpload());

        List<IssuerCfg> issuerCfgs = new ArrayList<>();
        issuerCfgs.add(new IssuerCfg("VISA", true, false));
        issuerCfgs.add(new IssuerCfg("MASTERCARD", true, false));
        issuerCfgs.add(new IssuerCfg("EFTPOS", true, false));
        issuerCfgs.add(new IssuerCfg("AMEX", true, false));
        issuerCfgs.add(new IssuerCfg("DINERS", true, false));
        issuerCfgs.add(new IssuerCfg("JCB", true, false));
        issuerCfgs.add(new IssuerCfg("UPI", true, false));

        for(int i = 0; i < params.getIssuers().size(); i++) {
            assertEquals(issuerCfgs.get(i).getIssuerName(), params.getIssuers().get(i).getIssuerName());
            assertEquals(issuerCfgs.get(i).isEnabled(), params.getIssuers().get(i).isEnabled());
            assertEquals(issuerCfgs.get(i).isDeferredAuthEnabled(), params.getIssuers().get(i).isDeferredAuthEnabled());
        }

        assertEquals(issuerCfgs.size(), params.getIssuers().size());

        assertEquals("cardproduct.json", params.getCardProductFile());
        assertEquals("cfg_emv.json", params.getCfgEmvFile());
        assertEquals("cfg_ctls_emv.json", params.getCfgCtlsFile());
        assertEquals("blacklist.json", params.getBlacklistFile());

        assertEquals(60 * 1000, params.getUiConfigTimeouts().getTimeoutMilliSecs(ConfigTimeouts.AMOUNT_ENTRY_TIMEOUT, false));
        assertEquals(60 * 1000, params.getUiConfigTimeouts().getTimeoutMilliSecs(ConfigTimeouts.PRESENT_CARD_TIMEOUT, false));
        assertEquals(30 * 1000, params.getUiConfigTimeouts().getTimeoutMilliSecs(ConfigTimeouts.ACCOUNT_SELECTION_TIMEOUT, false));
        assertEquals(30 * 1000, params.getUiConfigTimeouts().getTimeoutMilliSecs(ConfigTimeouts.APP_SELECTION_TIMEOUT, false));

        assertEquals(60 * 1000, params.getUiConfigTimeouts().getTimeoutMilliSecs(ConfigTimeouts.CARD_PIN_ENTRY_TIMEOUT, false));
        assertEquals(60 * 1000, params.getUiConfigTimeouts().getTimeoutMilliSecs(ConfigTimeouts.CONFIRM_SIGNATURE_TIMEOUT, false));
        assertEquals(15 * 1000, params.getUiConfigTimeouts().getTimeoutMilliSecs(ConfigTimeouts.CUSTOMER_PRINT_RECEIPT_TIMEOUT, false));
        assertEquals(15 * 1000, params.getUiConfigTimeouts().getTimeoutMilliSecs(ConfigTimeouts.REMOVE_RECEIPT_TIMEOUT, false));
        assertEquals(60 * 1000, params.getUiConfigTimeouts().getTimeoutMilliSecs(ConfigTimeouts.REMOVE_CARD_TIMEOUT, false));
        assertEquals(120 * 1000, params.getUiConfigTimeouts().getTimeoutMilliSecs(ConfigTimeouts.PAPER_OUT_TIMEOUT, false));
        assertEquals(2 * 1000, params.getUiConfigTimeouts().getTimeoutMilliSecs(ConfigTimeouts.DECISION_SCREEN_TIMEOUT, false));

        assertEquals(240 * 1000, params.getUiConfigTimeouts().getTimeoutMilliSecs(ConfigTimeouts.PRESENT_CARD_TIMEOUT, true));
        assertEquals(240 * 1000, params.getUiConfigTimeouts().getTimeoutMilliSecs(ConfigTimeouts.ACCOUNT_SELECTION_TIMEOUT, true));
        assertEquals(240 * 1000, params.getUiConfigTimeouts().getTimeoutMilliSecs(ConfigTimeouts.APP_SELECTION_TIMEOUT, true));
        assertEquals(240 * 1000, params.getUiConfigTimeouts().getTimeoutMilliSecs(ConfigTimeouts.CARD_PIN_ENTRY_TIMEOUT, true));
        assertEquals(5 * 1000, params.getUiConfigTimeouts().getTimeoutMilliSecs(ConfigTimeouts.DECISION_SCREEN_TIMEOUT, true));


        assertEquals(true, params.isOfflineFlightModeAllowed());
        assertEquals(10000, params.getOfflineTransactionCeilingLimitCentsContact());
        assertEquals(5000, params.getOfflineTransactionCeilingLimitCentsContactless());
        assertEquals(80000, params.getOfflineSoftLimitAmountCents());
        assertEquals(16, params.getOfflineSoftLimitCount());
        assertEquals(100000, params.getOfflineUpperLimitAmountCents());
        assertEquals(20, params.getOfflineUpperLimitCount());
        assertEquals(true, params.isUnattendedModeAllowed());


        // TODO: here still breaking. Follow up and continue tomorrow.
        List<CdoAllowed> cdoAllowedList = new ArrayList<>();
        cdoAllowedList.add(new CdoAllowed(1, true));
        cdoAllowedList.add(new CdoAllowed(3, true));
        cdoAllowedList.add(new CdoAllowed(29, true));
        cdoAllowedList.add(new CdoAllowed(4, true));
        cdoAllowedList.add(new CdoAllowed(28, true));
        cdoAllowedList.add(new CdoAllowed(5, true));
        cdoAllowedList.add(new CdoAllowed(6, true));
        cdoAllowedList.add(new CdoAllowed(9, true));
        cdoAllowedList.add(new CdoAllowed(30, true));
        for(int i = 0; i < params.getCdoAllowedList().size(); i++) {
            assertEquals(cdoAllowedList.get(i).getCardBinNumber(), params.getCdoAllowedList().get(i).getCardBinNumber());
            assertEquals(cdoAllowedList.get(i).isEnabled(), params.getCdoAllowedList().get(i).isEnabled());
        }
        assertEquals(cdoAllowedList.size(), params.getCdoAllowedList().size());


        assertEquals("7", params.getPreAuthExpiry_default());
        assertEquals("7", params.getPreauthExpiry_eftpos());
        assertEquals("7", params.getPreauthExpiry_mastercard_credit());
        assertEquals("7", params.getPreauthExpiry_mastercard_debit());
        assertEquals("7", params.getPreauthExpiry_visa_credit());
        assertEquals("7", params.getPreauthExpiry_visa_debit());
        assertEquals("7", params.getPreauthExpiry_amex());
        assertEquals("7", params.getPreauthExpiry_diners_club());
        assertEquals("7", params.getPreauthExpiry_jcb());
        assertEquals("7", params.getPreauthExpiry_unionpay_credit());

        assertEquals("brandingFiles/000001", params.getBrandDisplayLogoHeader());
        assertEquals("brandingFiles/000002", params.getBrandDisplayLogoIdle());
        assertEquals("brandingFiles/000003", params.getBrandDisplayLogoSplash());
        assertEquals("000004", params.getBrandDisplayStatusBarColour());
        assertEquals("00BCB4", params.getBrandDisplayButtonColour());
        assertEquals("000005", params.getBrandDisplayButtonTextColour());
        assertEquals("FF591F", params.getBrandDisplayPrimaryColour());
        assertEquals("brandingFiles/000006", params.getBrandReceiptLogoHeader());
    }


    // Requires both override and surcharge
    @Test
    public void testSurcharge() {
        PayCfgImpl params = new PayCfgImpl(mockedContext);
        OverrideParameters paramsOR = gsonXml.fromXml(overrideXML, OverrideParameters.class);
        HotLoadParameters paramsHL = gsonXml.fromXml(hotloadXML, HotLoadParameters.class);

        IMalHardware hardware = mock(IMalHardware.class);
        when(mockedMal.getHardware()).thenReturn(hardware);
        when(hardware.hasPrinter()).thenReturn(true);

        params.loadOverrideParams("Random Customer 1",  paramsOR, 1);
        params.loadHotloadParams(paramsHL, 1);

        List<Surcharge> expected = new ArrayList<>();
        expected.add(new Surcharge("01", "$", "1"));
        expected.add(new Surcharge("03", "%", "2"));
        expected.add(new Surcharge("29", "%", "3"));
        expected.add(new Surcharge("04", "%", "4"));
        expected.add(new Surcharge("28", "%", "5"));
        expected.add(new Surcharge("05", "%", "6"));
        expected.add(new Surcharge("06", "%", "7"));
        expected.add(new Surcharge("09", "%", "8"));
        expected.add(new Surcharge("30", "%", "9"));

        List<Surcharge> surcharges = params.getDefaultSc();

        assertEquals(expected.size(), surcharges.size());
        for(int i = 0; i < surcharges.size(); i++) {
            assertEquals(expected.get(i).getB(), surcharges.get(i).getB());
            assertEquals(expected.get(i).getV(), surcharges.get(i).getV());
            assertEquals(expected.get(i).getT(), surcharges.get(i).getT());
        }
    }


    @Test
    public void cardTests() {
        String cardData = "[{\"accountSelection\":7,\"authorizationBits\":0,\"binNumber\":5,\"completionOnline\":false,\"customerPanMask\":\"001\",\"deferredAuthEnabled\":false,\"disabled\":false,\"forceOffline\":false,\"forceReferral\":false,\"forceSign\":false,\"iinRange\":\"340000-349999,370267-370267,370285-370287,370289-370289,374738-374739,376966-376966,376968-376968,377152-377153,377158-377158,377187-377187,377677-377677,370246-370249,377155-377155,379900-379999,\",\"limits\":{\"cashFloor\":0,\"cashMax\":9999,\"cashbackMax\":0,\"commFailCashFloor\":0,\"commFailFloor\":0,\"floor\":0,\"mangerAuthMax\":0,\"max\":9999,\"min\":0,\"offlineRefundMax\":0,\"smallValueLimitDollars\":0,\"telAuthMax\":0,\"telPinAuthMax\":0},\"luhnCheck\":true,\"merchantPanMask\":\"101\",\"name\":\"American Express\",\"onlinePin\":{\"balance\":\"SVC\",\"cash\":\"SVC\",\"cashback\":\"SVC\",\"deposit\":\"SVC\",\"pinChange\":\"SVC\",\"preauth\":\"SVC\",\"refund\":\"SVC\",\"sale\":\"SVC\"},\"panLength\":\"15,\",\"parent\":false,\"passwordRequired\":{\"balance\":false,\"cash\":false,\"cashback\":false,\"completion\":false,\"deposit\":false,\"depositOffline\":false,\"forced\":false,\"moto\":false,\"offlineCash\":false,\"offlineRefund\":false,\"pinChange\":false,\"preauth\":false,\"refund\":false,\"reprint\":false,\"reversal\":false,\"sale\":false},\"printCardholderName\":true,\"productLevelBlocking\":false,\"psi\":\"A\",\"refundOnline\":true,\"rejectCtls\":false,\"rejectEmv\":false,\"schemeLabel\":\"AMEX\",\"serviceCodeCheck\":true,\"servicesAllowed\":{\"balance\":false,\"cash\":true,\"cashback\":true,\"deposit\":false,\"depositOffline\":false,\"forced\":false,\"moto\":true,\"offlineCash\":false,\"offlineRefund\":false,\"pinChange\":false,\"preauth\":true,\"refund\":true,\"reversal\":true,\"sale\":true}},{\"accountSelection\":7,\"authorizationBits\":0,\"binNumber\":5,\"completionOnline\":false,\"customerPanMask\":\"001\",\"deferredAuthEnabled\":false,\"disabled\":false,\"forceOffline\":false,\"forceReferral\":false,\"forceSign\":false,\"iinRange\":\"370000-379999,\",\"limits\":{\"cashFloor\":0,\"cashMax\":9999,\"cashbackMax\":9999,\"commFailCashFloor\":0,\"commFailFloor\":0,\"floor\":0,\"mangerAuthMax\":0,\"max\":9999,\"min\":0,\"offlineRefundMax\":0,\"smallValueLimitDollars\":0,\"telAuthMax\":0,\"telPinAuthMax\":0},\"luhnCheck\":true,\"merchantPanMask\":\"101\",\"name\":\"American Express\",\"onlinePin\":{\"balance\":\"SVC\",\"cash\":\"SVC\",\"cashback\":\"SVC\",\"deposit\":\"SVC\",\"pinChange\":\"SVC\",\"preauth\":\"SVC\",\"refund\":\"SVC\",\"sale\":\"SVC\"},\"panLength\":\"15,\",\"parent\":true,\"passwordRequired\":{\"balance\":false,\"cash\":false,\"cashback\":false,\"completion\":false,\"deposit\":false,\"depositOffline\":false,\"forced\":false,\"moto\":false,\"offlineCash\":false,\"offlineRefund\":false,\"pinChange\":false,\"preauth\":false,\"refund\":false,\"reprint\":false,\"reversal\":false,\"sale\":false},\"printCardholderName\":true,\"productLevelBlocking\":false,\"psi\":\"A\",\"refundOnline\":true,\"rejectCtls\":false,\"rejectEmv\":false,\"schemeLabel\":\"AMEX\",\"serviceCodeCheck\":true,\"servicesAllowed\":{\"balance\":false,\"cash\":true,\"cashback\":true,\"deposit\":false,\"depositOffline\":false,\"forced\":false,\"moto\":true,\"offlineCash\":false,\"offlineRefund\":false,\"pinChange\":false,\"preauth\":true,\"refund\":true,\"reversal\":true,\"sale\":true}},{\"accountSelection\":3,\"authorizationBits\":0,\"binNumber\":28,\"completionOnline\":false,\"customerPanMask\":\"101\",\"deferredAuthEnabled\":false,\"disabled\":false,\"forceOffline\":false,\"forceReferral\":false,\"forceSign\":false,\"iinRange\":\"41330000-41330099,42603300-42603399,42830000-42830099,48380000-48380099,48380200-48380299,48380300-48380399,48967900-48967999,49166200-49166299,\",\"limits\":{\"cashFloor\":0,\"cashMax\":9999,\"cashbackMax\":9999,\"commFailCashFloor\":0,\"commFailFloor\":0,\"floor\":0,\"mangerAuthMax\":0,\"max\":9999,\"min\":0,\"offlineRefundMax\":0,\"smallValueLimitDollars\":0,\"telAuthMax\":0,\"telPinAuthMax\":0},\"luhnCheck\":true,\"merchantPanMask\":\"101\",\"name\":\"Visa Debit\",\"onlinePin\":{\"balance\":\"N\",\"cash\":\"Y\",\"cashback\":\"Y\",\"deposit\":\"\",\"pinChange\":\"\",\"preauth\":\"N\",\"refund\":\"Y\",\"sale\":\"Y\"},\"panLength\":\"13,14,15,16,17,18,19,\",\"parent\":false,\"passwordRequired\":{\"balance\":false,\"cash\":false,\"cashback\":false,\"completion\":false,\"deposit\":false,\"depositOffline\":false,\"forced\":false,\"moto\":false,\"offlineCash\":false,\"offlineRefund\":false,\"pinChange\":false,\"preauth\":false,\"refund\":false,\"reprint\":false,\"reversal\":false,\"sale\":false},\"printCardholderName\":true,\"productLevelBlocking\":false,\"psi\":\"V\",\"refundOnline\":true,\"rejectCtls\":false,\"rejectEmv\":false,\"schemeLabel\":\"VISA\",\"serviceCodeCheck\":true,\"servicesAllowed\":{\"balance\":false,\"cash\":true,\"cashback\":true,\"deposit\":false,\"depositOffline\":false,\"forced\":false,\"moto\":true,\"offlineCash\":false,\"offlineRefund\":false,\"pinChange\":false,\"preauth\":true,\"refund\":true,\"reversal\":true,\"sale\":true}},{\"accountSelection\":4,\"authorizationBits\":0,\"binNumber\":4,\"completionOnline\":false,\"customerPanMask\":\"101\",\"deferredAuthEnabled\":false,\"disabled\":false,\"forceOffline\":false,\"forceReferral\":false,\"forceSign\":false,\"iinRange\":\"40026900-40026999,40067700-40067799,40158800-40158899,40298000-40298099,40298100-40298199,40494000-40494099,40647100-40647199,40647200-40647299,40817700-40817799,40934100-40934199,40934900-40934999,40977200-40977299,40977300-40977399,41097500-41097599,41114900-41114999,41115000-41115099,41330400-41330499,41334000-41334099,41472600-41472699,41623800-41623899,41623900-41623999,41824800-41824899,41824900-41824999,41825000-41825099,41825200-41825299,41825300-41825399,41997300-41997399,42027400-42027499,42152900-42152999,42164500-42164599,42164600-42164699,42169900-42169999,42493400-42493499,42653400-42653499,42831400-42831499,42831500-42831599,42831600-42831699,42831700-42831799,42831800-42831899,42976200-42976299,42976300-42976399,43121700-43121799,43121800-43121899,43121900-43121999,43238700-43238799,43246500-43246599,43440000-43440099,43440100-43440199,43521000-43521099,43521100-43521199,43521200-43521299,43521300-43521399,43521400-43521499,43521500-43521599,43521600-43521699,43521700-43521799,43521800-43521899,43521900-43521999,43521000-43521999,43633200-43633299,43638000-43638099,43638300-43638399,43638400-43638499,43639200-43639299,43655100-43655199,43655900-43655999,43773000-43773099,43773100-43773199,43774600-43774699,43774700-43774799,43775800-43775899,43776900-43776999,44081500-44081599,44081600-44081699,44241200-44241299,44241800-44241899,44264500-44264599,44340600-44340699,44342200-44342299,44343000-44343099,45051200-45051299,45058600-45058699,45247800-45247899,45315200-45315299,45315300-45315399,45315400-45315499,45315500-45315599,45375100-45375199,45460500-45460599,45570400-45570499,45640400-45640499,45644200-45644299,45645300-45645399,45648300-45648399,45648400-45648499,45648700-45648799,45654300-45654399,45710400-45710499,45724400-45724499,45735600-45735699,45743500-45743599,45754300-45754399,45754400-45754499,45754500-45754599,45754600-45754699,45754700-45754799,46019800-46019899,46175600-46175699,46175700-46175799,46194900-46194999,46195000-46195099,46285700-46285799,46286700-46286799,46295000-46295099,46376100-46376199,46377600-46377699,46378200-46378299,46378400-46378499,46449100-46449199,46455300-46455399,46455400-46455499,46455500-46455599,46455600-46455699,46784700-46784799,46875300-46875399,46935500-46935599,46935600-46935699,47055000-47055099,47055100-47055199,47055200-47055299,47152700-47152799,47230200-47230299,47243600-47243699,47243700-47243799,47483900-47483999,47652600-47652699,47654300-47654399,47655000-47655099,47663900-47663999,47827900-47827999,47828000-47828099,47828100-47828199,47911600-47911699,47994300-47994399,48068200-48068299,48136300-48136399,48137300-48137399,48288900-48288999,48302800-48302899,48378800-48378899,48378900-48378999,48389800-48389899,48389900-48389999,48627000-48627099,48627100-48627199,48627700-48627799,48892300-48892399,48894900-48894999,48908000-48908099,49024200-49024299,49071800-49071899,49662900-49662999,49841300-49841399,49841500-49841599,49841600-49841699,49882400-49882499,\",\"limits\":{\"cashFloor\":0,\"cashMax\":9999,\"cashbackMax\":9999,\"commFailCashFloor\":0,\"commFailFloor\":0,\"floor\":0,\"mangerAuthMax\":0,\"max\":9999,\"min\":0,\"offlineRefundMax\":0,\"smallValueLimitDollars\":0,\"telAuthMax\":0,\"telPinAuthMax\":0},\"luhnCheck\":true,\"merchantPanMask\":\"101\",\"name\":\"Visa Credit\",\"onlinePin\":{\"balance\":\"N\",\"cash\":\"Y\",\"cashback\":\"Y\",\"deposit\":\"\",\"pinChange\":\"\",\"preauth\":\"N\",\"refund\":\"Y\",\"sale\":\"Y\"},\"panLength\":\"13,14,15,16,17,18,19,\",\"parent\":false,\"passwordRequired\":{\"balance\":false,\"cash\":false,\"cashback\":false,\"completion\":false,\"deposit\":false,\"depositOffline\":false,\"forced\":false,\"moto\":false,\"offlineCash\":false,\"offlineRefund\":false,\"pinChange\":false,\"preauth\":false,\"refund\":false,\"reprint\":false,\"reversal\":false,\"sale\":false},\"printCardholderName\":true,\"productLevelBlocking\":false,\"psi\":\"V\",\"refundOnline\":true,\"rejectCtls\":false,\"rejectEmv\":false,\"schemeLabel\":\"VISA\",\"serviceCodeCheck\":true,\"servicesAllowed\":{\"balance\":false,\"cash\":true,\"cashback\":true,\"deposit\":false,\"depositOffline\":false,\"forced\":false,\"moto\":true,\"offlineCash\":false,\"offlineRefund\":false,\"pinChange\":false,\"preauth\":true,\"refund\":true,\"reversal\":true,\"sale\":true}},{\"accountSelection\":7,\"authorizationBits\":0,\"binNumber\":4,\"completionOnline\":false,\"customerPanMask\":\"101\",\"deferredAuthEnabled\":false,\"disabled\":false,\"forceOffline\":false,\"forceReferral\":false,\"forceSign\":false,\"iinRange\":\"40060900-40060999,40179500-40179599,40297900-40297999,40299300-40299399,40413700-40413799,40434000-40434099,40434200-40434299,40434300-40434399,40477200-40477299,40488700-40488799,40522100-40522199,40549700-40549799,40549800-40549899,40658700-40658799,40726400-40726499,40896700-40896799,40977100-40977199,40977400-40977499,41330100-41330199,41330200-41330299,41330300-41330399,41330500-41330599,41330700-41330799,41331200-41331299,41331300-41331399,41331600-41331699,41333800-41333899,41354200-41354299,41414600-41414699,41543600-41543699,41570000-41570099,41824400-41824499,41824600-41824699,42395300-42395399,42395400-42395499,42513500-42513599,42601100-42601199,42603500-42603599,42652900-42652999,42653000-42653099,42932100-42932199,42976100-42976199,43123900-43123999,43124000-43124099,43358000-43358099,43358100-43358199,43358200-43358299,43358300-43358399,43361500-43361599,43361600-43361699,43361700-43361799,43369600-43369699,43377000-43377099,43496800-43496899,43635800-43635899,43653100-43653199,43738100-43738199,43887500-43887599,44308400-44308499,44340100-44340199,44340200-44340299,44340400-44340499,44340800-44340899,44340900-44340999,44340000-44340999,44341000-44341099,44341400-44341499,44341900-44341999,44341000-44341999,44342000-44342099,44342500-44342599,44342600-44342699,44342800-44342899,44342900-44342999,44342000-44342999,44343000-44343099,44343100-44343199,44343200-44343299,44343300-44343399,44343400-44343499,44343600-44343699,44343700-44343799,44343800-44343899,44343900-44343999,44344000-44344099,44344100-44344199,44344200-44344299,44344300-44344399,44344500-44344599,44344600-44344699,44344700-44344799,44344800-44344899,44344900-44344999,44344000-44344999,44345000-44345099,44345100-44345199,44345200-44345299,44345300-44345399,44345500-44345599,44345600-44345699,44345800-44345899,44345900-44345999,44346000-44346099,44346100-44346199,44346200-44346299,44346300-44346399,44346400-44346499,44346500-44346599,44346700-44346799,44346900-44346999,44347100-44347199,44347200-44347299,44347300-44347399,44347400-44347499,44347500-44347599,44347700-44347799,44347800-44347899,44347900-44347999,44348000-44348099,44348100-44348199,44348200-44348299,44348300-44348399,44348500-44348599,44348600-44348699,44348700-44348799,44348800-44348899,44348900-44348999,44348000-44348999,44349100-44349199,44349200-44349299,44349300-44349399,44349400-44349499,44349500-44349599,44349600-44349699,44349700-44349799,44349900-44349999,44349000-44349999,44500900-44500999,44501100-44501199,44501200-44501299,44501300-44501399,44501400-44501499,44508800-44508899,45060500-45060599,45060600-45060699,45064700-45064799,45078800-45078899,45247900-45247999,45248000-45248099,45248100-45248199,45248200-45248299,45303000-45303099,45315000-45315099,45315100-45315199,45322400-45322499,45376700-45376799,45542700-45542799,45600400-45600499,45606100-45606199,45640300-45640399,45640900-45640999,45643000-45643099,45643200-45643299,45644100-45644199,45644200-45644299,45645600-45645699,45647400-45647499,45647500-45647599,45647900-45647999,45648600-45648699,45773000-45773099,45852400-45852499,45859400-45859499,45859500-45859599,45885800-45885899,46107600-46107699,46195800-46195899,46196100-46196199,46196300-46196399,46196400-46196499,46196600-46196699,46196700-46196799,46197200-46197299,46197300-46197399,46197400-46197499,46197500-46197599,46197600-46197699,46197700-46197799,46223900-46223999,46224000-46224099,46225900-46225999,46226300-46226399,46248000-46248099,46248100-46248199,46248200-46248299,46248300-46248399,46294900-46294999,46378300-46378399,46392500-46392599,46457900-46457999,46614400-46614499,46616800-46616899,46616900-46616999,46822900-46822999,46875000-46875099,46875100-46875199,46875200-46875299,46875400-46875499,47053900-47053999,47054100-47054199,47056300-47056399,47056400-47056499,47056500-47056599,47056800-47056899,47056900-47056999,47157200-47157299,47213400-47213499,47224500-47224599,47224800-47224899,47247700-47247799,47265800-47265899,47265900-47265999,47266000-47266099,47267800-47267899,47267900-47267999,47269000-47269099,47269100-47269199,47269300-47269399,47269400-47269499,47269500-47269599,47269600-47269699,47269700-47269799,47633000-47633099,47633900-47633999,47635700-47635799,48137400-48137499,48288300-48288399,48288400-48288499,48288500-48288599,48288600-48288699,48288700-48288799,48288800-48288899,48344000-48344099,48344100-48344199,48374000-48374099,48380100-48380199,48380400-48380499,48380600-48380699,48380700-48380799,48382100-48382199,48382300-48382399,48382400-48382499,48386700-48386799,48389700-48389799,48604000-48604099,48890300-48890399,48895000-48895099,48998100-48998199,49023700-49023799,49028900-49028999,49029000-49029099,49029100-49029199,49029200-49029299,49029300-49029399,49092500-49092599,49166100-49166199,49313000-49313099,49341400-49341499,49405200-49405299,49405300-49405399,49409700-49409799,49669200-49669299,40000000-49999999,\",\"limits\":{\"cashFloor\":0,\"cashMax\":9999,\"cashbackMax\":9999,\"commFailCashFloor\":0,\"commFailFloor\":0,\"floor\":0,\"mangerAuthMax\":0,\"max\":9999,\"min\":0,\"offlineRefundMax\":0,\"smallValueLimitDollars\":0,\"telAuthMax\":0,\"telPinAuthMax\":0},\"luhnCheck\":true,\"merchantPanMask\":\"101\",\"name\":\"Visa\",\"onlinePin\":{\"balance\":\"N\",\"cash\":\"Y\",\"cashback\":\"Y\",\"deposit\":\"\",\"pinChange\":\"\",\"preauth\":\"N\",\"refund\":\"Y\",\"sale\":\"Y\"},\"panLength\":\"13,14,15,16,17,18,19,\",\"parent\":false,\"passwordRequired\":{\"balance\":false,\"cash\":false,\"cashback\":false,\"completion\":false,\"deposit\":false,\"depositOffline\":false,\"forced\":false,\"moto\":false,\"offlineCash\":false,\"offlineRefund\":false,\"pinChange\":false,\"preauth\":false,\"refund\":false,\"reprint\":false,\"reversal\":false,\"sale\":false},\"printCardholderName\":true,\"productLevelBlocking\":false,\"psi\":\"V\",\"refundOnline\":true,\"rejectCtls\":false,\"rejectEmv\":false,\"schemeLabel\":\"VISA\",\"serviceCodeCheck\":true,\"servicesAllowed\":{\"balance\":false,\"cash\":true,\"cashback\":true,\"deposit\":false,\"depositOffline\":false,\"forced\":false,\"moto\":true,\"offlineCash\":false,\"offlineRefund\":false,\"pinChange\":false,\"preauth\":true,\"refund\":true,\"reversal\":true,\"sale\":true}},{\"accountSelection\":3,\"authorizationBits\":0,\"binNumber\":29,\"completionOnline\":false,\"customerPanMask\":\"001\",\"deferredAuthEnabled\":false,\"disabled\":false,\"forceOffline\":false,\"forceReferral\":false,\"forceSign\":false,\"iinRange\":\"50212500-50212599,57988900-57988999,57989700-57989799,58201300-58201399,\",\"limits\":{\"cashFloor\":0,\"cashMax\":9999,\"cashbackMax\":9999,\"commFailCashFloor\":0,\"commFailFloor\":0,\"floor\":0,\"mangerAuthMax\":0,\"max\":9999,\"min\":0,\"offlineRefundMax\":0,\"smallValueLimitDollars\":0,\"telAuthMax\":0,\"telPinAuthMax\":0},\"luhnCheck\":true,\"merchantPanMask\":\"101\",\"name\":\"MasterCard Debit\",\"onlinePin\":{\"balance\":\"N\",\"cash\":\"Y\",\"cashback\":\"Y\",\"deposit\":\"\",\"pinChange\":\"\",\"preauth\":\"N\",\"refund\":\"Y\",\"sale\":\"Y\"},\"panLength\":\"16,\",\"parent\":false,\"passwordRequired\":{\"balance\":false,\"cash\":false,\"cashback\":false,\"completion\":false,\"deposit\":false,\"depositOffline\":false,\"forced\":false,\"moto\":false,\"offlineCash\":false,\"offlineRefund\":false,\"pinChange\":false,\"preauth\":false,\"refund\":false,\"reprint\":false,\"reversal\":false,\"sale\":false},\"printCardholderName\":true,\"productLevelBlocking\":false,\"psi\":\"M\",\"refundOnline\":true,\"rejectCtls\":false,\"rejectEmv\":false,\"schemeLabel\":\"MASTERCARD\",\"serviceCodeCheck\":true,\"servicesAllowed\":{\"balance\":false,\"cash\":true,\"cashback\":true,\"deposit\":false,\"depositOffline\":false,\"forced\":false,\"moto\":true,\"offlineCash\":false,\"offlineRefund\":false,\"pinChange\":false,\"preauth\":true,\"refund\":true,\"reversal\":true,\"sale\":true}},{\"accountSelection\":4,\"authorizationBits\":0,\"binNumber\":3,\"completionOnline\":false,\"customerPanMask\":\"001\",\"deferredAuthEnabled\":false,\"disabled\":false,\"forceOffline\":false,\"forceReferral\":false,\"forceSign\":false,\"iinRange\":\"22210000-22219999,22220000-22229999,22230000-22239999,22240000-22249999,22250000-22259999,22260000-22269999,22272100-22272199,22270000-22279999,22280000-22289999,22290000-22299999,22300000-22399999,22400000-22499999,22500000-22599999,22600000-22699999,22700000-22799999,22800000-22899999,22900000-22999999,23000000-23999999,24000000-24999999,25000000-25999999,26000000-26999999,27000000-27099999,27100000-27199999,27200000-27209999,51165400-51165499,51193900-51193999,51195800-51195899,51219700-51219799,51257600-51257699,51404500-51404599,51410700-51410799,51796900-51796999,51798300-51798399,51875600-51875699,51924700-51924799,51924800-51924899,51000000-51999999,52189300-52189399,52189400-52189499,52270000-52270099,52272300-52272399,52310100-52310199,52374400-52374499,52374800-52374899,52390000-52390099,52300000-52399999,52401400-52401499,52403900-52403999,52430900-52430999,52444300-52444399,52471900-52471999,52580700-52580799,52610700-52610799,52687900-52687999,52690100-52690199,52726300-52726399,52902000-52902099,52908700-52908799,52951200-52951299,52951500-52951599,52952300-52952399,52952900-52952999,52953700-52953799,52000000-52999999,53135700-53135799,53276600-53276699,53278000-53278099,53278100-53278199,53278500-53278599,53382900-53382999,53383000-53383099,53383100-53383199,53383800-53383899,53427600-53427699,53531900-53531999,53676300-53676399,53860700-53860799,53863400-53863499,53863500-53863599,53863600-53863699,53864600-53864699,53864700-53864799,53865100-53865199,53000000-53999999,54062100-54062199,54252900-54252999,54306000-54306099,54443400-54443499,54444700-54444799,54463700-54463799,54464700-54464799,54709800-54709899,54726300-54726399,54947100-54947199,54000000-54999999,55020000-55020099,55020100-55020199,55020200-55020299,55079700-55079799,55206000-55206099,55241600-55241699,55244600-55244699,55320500-55320599,55320600-55320699,55500100-55500199,55500500-55500599,55504800-55504899,55522300-55522399,55587800-55587899,55590900-55590999,55605000-55605099,55633400-55633499,55633600-55633699,55673300-55673399,55704500-55704599,55828000-55828099,55870100-55870199,55919500-55919599,55000000-55999999,\",\"limits\":{\"cashFloor\":0,\"cashMax\":9999,\"cashbackMax\":9999,\"commFailCashFloor\":0,\"commFailFloor\":0,\"floor\":0,\"mangerAuthMax\":0,\"max\":9999,\"min\":0,\"offlineRefundMax\":0,\"smallValueLimitDollars\":0,\"telAuthMax\":0,\"telPinAuthMax\":0},\"luhnCheck\":true,\"merchantPanMask\":\"101\",\"name\":\"MasterCard Credit\",\"onlinePin\":{\"balance\":\"N\",\"cash\":\"Y\",\"cashback\":\"Y\",\"deposit\":\"\",\"pinChange\":\"\",\"preauth\":\"N\",\"refund\":\"Y\",\"sale\":\"Y\"},\"panLength\":\"16,\",\"parent\":false,\"passwordRequired\":{\"balance\":false,\"cash\":false,\"cashback\":false,\"completion\":false,\"deposit\":false,\"depositOffline\":false,\"forced\":false,\"moto\":false,\"offlineCash\":false,\"offlineRefund\":false,\"pinChange\":false,\"preauth\":false,\"refund\":false,\"reprint\":false,\"reversal\":false,\"sale\":false},\"printCardholderName\":true,\"productLevelBlocking\":false,\"psi\":\"M\",\"refundOnline\":true,\"rejectCtls\":false,\"rejectEmv\":false,\"schemeLabel\":\"MASTERCARD\",\"serviceCodeCheck\":true,\"servicesAllowed\":{\"balance\":false,\"cash\":true,\"cashback\":true,\"deposit\":false,\"depositOffline\":false,\"forced\":false,\"moto\":true,\"offlineCash\":false,\"offlineRefund\":false,\"pinChange\":false,\"preauth\":true,\"refund\":true,\"reversal\":true,\"sale\":true}},{\"accountSelection\":7,\"authorizationBits\":0,\"binNumber\":3,\"completionOnline\":false,\"customerPanMask\":\"001\",\"deferredAuthEnabled\":false,\"disabled\":false,\"forceOffline\":false,\"forceReferral\":false,\"forceSign\":false,\"iinRange\":\"51006700-51006799,51212700-51212799,51425000-51425099,51481300-51481399,51529500-51529599,51631000-51631099,51631500-51631599,51631900-51631999,51632000-51632099,51632100-51632199,51632300-51632399,51632400-51632499,51632500-51632599,51632600-51632699,51632800-51632899,51632900-51632999,51633000-51633099,51633500-51633599,51633700-51633799,51633900-51633999,51634000-51634099,51634500-51634599,51634900-51634999,51635000-51635099,51635500-51635599,51635900-51635999,51636000-51636099,51636100-51636199,51636500-51636599,51636600-51636699,51636900-51636999,51637000-51637099,51637500-51637599,51637900-51637999,51638000-51638099,51638500-51638599,51638900-51638999,51639000-51639099,51639100-51639199,51749700-51749799,51818200-51818299,51884000-51884099,51886800-51886899,51924400-51924499,52172900-52172999,52296100-52296199,52298000-52298099,52406600-52406699,52801300-52801399,52975700-52975799,53045300-53045399,53135500-53135599,53135600-53135699,53135800-53135899,53135900-53135999,53168300-53168399,53256500-53256599,53265500-53265599,53273700-53273799,53382800-53382899,53383200-53383299,53531600-53531699,53531700-53531799,53531800-53531899,53603500-53603599,53603600-53603699,53719600-53719699,54021500-54021599,54040300-54040399,54048200-54048299,54304800-54304899,54304900-54304999,54356800-54356899,54360400-54360499,54379300-54379399,54581800-54581899,54682700-54682799,54682800-54682899,54817100-54817199,55012400-55012499,55134400-55134499,55143900-55143999,55152100-55152199,55203300-55203399,55228200-55228299,55229400-55229499,55235000-55235099,55235100-55235199,55241100-55241199,55263800-55263899,55500300-55500399,55552200-55552299,55832000-55832099,55832100-55832199,55838800-55838899,55860100-55860199,55860200-55860299,55885000-55885099,55946400-55946499,55973800-55973899,55973900-55973999,55974000-55974099,55974100-55974199,\",\"limits\":{\"cashFloor\":0,\"cashMax\":9999,\"cashbackMax\":9999,\"commFailCashFloor\":0,\"commFailFloor\":0,\"floor\":0,\"mangerAuthMax\":0,\"max\":9999,\"min\":0,\"offlineRefundMax\":0,\"smallValueLimitDollars\":0,\"telAuthMax\":0,\"telPinAuthMax\":0},\"luhnCheck\":true,\"merchantPanMask\":\"101\",\"name\":\"MasterCard\",\"onlinePin\":{\"balance\":\"N\",\"cash\":\"Y\",\"cashback\":\"Y\",\"deposit\":\"\",\"pinChange\":\"\",\"preauth\":\"N\",\"refund\":\"Y\",\"sale\":\"Y\"},\"panLength\":\"16,\",\"parent\":false,\"passwordRequired\":{\"balance\":false,\"cash\":false,\"cashback\":false,\"completion\":false,\"deposit\":false,\"depositOffline\":false,\"forced\":false,\"moto\":false,\"offlineCash\":false,\"offlineRefund\":false,\"pinChange\":false,\"preauth\":false,\"refund\":false,\"reprint\":false,\"reversal\":false,\"sale\":false},\"printCardholderName\":true,\"productLevelBlocking\":false,\"psi\":\"M\",\"refundOnline\":true,\"rejectCtls\":false,\"rejectEmv\":false,\"schemeLabel\":\"MASTERCARD\",\"serviceCodeCheck\":true,\"servicesAllowed\":{\"balance\":false,\"cash\":true,\"cashback\":true,\"deposit\":false,\"depositOffline\":false,\"forced\":false,\"moto\":true,\"offlineCash\":false,\"offlineRefund\":false,\"pinChange\":false,\"preauth\":true,\"refund\":true,\"reversal\":true,\"sale\":true}},{\"accountSelection\":3,\"authorizationBits\":0,\"binNumber\":1,\"completionOnline\":false,\"customerPanMask\":\"101\",\"deferredAuthEnabled\":false,\"disabled\":false,\"forceOffline\":false,\"forceReferral\":false,\"forceSign\":false,\"iinRange\":\"46935400-46935499,49027600-49027699,50100200-50100299,50100700-50100799,50179700-50179799,50180300-50180399,50211800-50211899,50211900-50211999,50281100-50281199,50394600-50394699,50396400-50396499,50457400-50457499,50489700-50489799,50772900-50772999,50773000-50773099,50773100-50773199,50773200-50773299,50773300-50773399,50773400-50773499,50773600-50773699,50773700-50773799,50773800-50773899,50773900-50773999,50774000-50774099,50000000-50999999,56019200-56019299,56022000-56022099,56025100-56025199,56025300-56025399,56025400-56025499,56025600-56025699,56025800-56025899,56026000-56026099,56026100-56026199,56026500-56026599,56026700-56026799,56027300-56027399,56027900-56027999,56028400-56028499,56029800-56029899,56029900-56029999,57988300-57988399,57988600-57988699,57989100-57989199,57989400-57989499,57989500-57989599,57989800-57989899,57990000-57990099,57990300-57990399,57990500-57990599,57991200-57991299,57992800-57992899,57993100-57993199,57993200-57993299,57993400-57993499,57993500-57993599,57993700-57993799,57993900-57993999,57994200-57994299,57997200-57997299,57997300-57997399,57997400-57997499,57998800-57998899,57998900-57998999,57999000-57999099,57999300-57999399,57999400-57999499,57999500-57999599,57999900-57999999,57990000-57990099,58169600-58169699,58169700-58169799,58169800-58169899,58169900-58169999,58180100-58180199,58181000-58181099,58181100-58181199,58181200-58181299,58181300-58181399,58181400-58181499,58185500-58185599,58400100-58400199,58400200-58400299,58400300-58400399,58400400-58400499,58400500-58400599,58400600-58400699,58400700-58400799,58400000-58400999,58619500-58619599,58859500-58859599,58867000-58867099,58899400-58899499,58926400-58926499,58928000-58928099,58955400-58955499,58955500-58955599,58955600-58955699,58964900-58964999,58986800-58986899,60133500-60133599,60157600-60157699,60179200-60179299,60100000-60199999,60384100-60384199,60869800-60869899,60869800-60869899,62204500-62204599,62207800-62207899,63688700-63688799,63688800-63688899,63688900-63688999,63689000-63689099,63689100-63689199,63689200-63689299,63689300-63689399,63689400-63689499,\",\"limits\":{\"cashFloor\":0,\"cashMax\":9999,\"cashbackMax\":9999,\"commFailCashFloor\":0,\"commFailFloor\":0,\"floor\":0,\"mangerAuthMax\":0,\"max\":9999,\"min\":0,\"offlineRefundMax\":0,\"smallValueLimitDollars\":0,\"telAuthMax\":0,\"telPinAuthMax\":0},\"luhnCheck\":true,\"merchantPanMask\":\"101\",\"name\":\"eftpos\",\"onlinePin\":{\"balance\":\"N\",\"cash\":\"Y\",\"cashback\":\"Y\",\"deposit\":\"\",\"pinChange\":\"\",\"preauth\":\"N\",\"refund\":\"Y\",\"sale\":\"Y\"},\"panLength\":\"13,14,15,16,17,18,19,\",\"parent\":false,\"passwordRequired\":{\"balance\":false,\"cash\":false,\"cashback\":false,\"completion\":false,\"deposit\":false,\"depositOffline\":false,\"forced\":false,\"moto\":false,\"offlineCash\":false,\"offlineRefund\":false,\"pinChange\":false,\"preauth\":false,\"refund\":false,\"reprint\":false,\"reversal\":false,\"sale\":false},\"printCardholderName\":true,\"productLevelBlocking\":false,\"psi\":\"E\",\"refundOnline\":true,\"rejectCtls\":false,\"rejectEmv\":false,\"schemeLabel\":\"EFTPOS\",\"serviceCodeCheck\":true,\"servicesAllowed\":{\"balance\":false,\"cash\":true,\"cashback\":true,\"deposit\":false,\"depositOffline\":false,\"forced\":false,\"moto\":true,\"offlineCash\":false,\"offlineRefund\":false,\"pinChange\":false,\"preauth\":true,\"refund\":true,\"reversal\":true,\"sale\":true},\"testIinRange\":\"63000000-63999999,\"},{\"accountSelection\":7,\"authorizationBits\":0,\"binNumber\":1,\"completionOnline\":false,\"customerPanMask\":\"101\",\"deferredAuthEnabled\":false,\"disabled\":false,\"forceOffline\":false,\"forceReferral\":false,\"forceSign\":false,\"iinRange\":\"58953600-58953699,\",\"limits\":{\"cashFloor\":0,\"cashMax\":9999,\"cashbackMax\":9999,\"commFailCashFloor\":0,\"commFailFloor\":0,\"floor\":0,\"mangerAuthMax\":0,\"max\":9999,\"min\":0,\"offlineRefundMax\":0,\"smallValueLimitDollars\":0,\"telAuthMax\":0,\"telPinAuthMax\":0},\"luhnCheck\":true,\"merchantPanMask\":\"101\",\"name\":\"eftpos\",\"onlinePin\":{\"balance\":\"N\",\"cash\":\"Y\",\"cashback\":\"Y\",\"deposit\":\"\",\"pinChange\":\"\",\"preauth\":\"N\",\"refund\":\"Y\",\"sale\":\"Y\"},\"panLength\":\"13,14,15,16,17,18,19,\",\"parent\":false,\"passwordRequired\":{\"balance\":false,\"cash\":false,\"cashback\":false,\"completion\":false,\"deposit\":false,\"depositOffline\":false,\"forced\":false,\"moto\":false,\"offlineCash\":false,\"offlineRefund\":false,\"pinChange\":false,\"preauth\":false,\"refund\":false,\"reprint\":false,\"reversal\":false,\"sale\":false},\"printCardholderName\":true,\"productLevelBlocking\":false,\"psi\":\"E\",\"refundOnline\":true,\"rejectCtls\":false,\"rejectEmv\":false,\"schemeLabel\":\"EFTPOS\",\"serviceCodeCheck\":true,\"servicesAllowed\":{\"balance\":false,\"cash\":true,\"cashback\":true,\"deposit\":false,\"depositOffline\":false,\"forced\":false,\"moto\":true,\"offlineCash\":false,\"offlineRefund\":false,\"pinChange\":false,\"preauth\":true,\"refund\":true,\"reversal\":true,\"sale\":true},\"testIinRange\":\"63000000-63999999,\"},{\"accountSelection\":7,\"authorizationBits\":0,\"binNumber\":9,\"completionOnline\":false,\"customerPanMask\":\"101\",\"deferredAuthEnabled\":false,\"disabled\":false,\"forceOffline\":false,\"forceReferral\":false,\"forceSign\":false,\"iinRange\":\"18000000-18009999,21310000-21319999,35000000-35999999\",\"limits\":{\"cashFloor\":0,\"cashMax\":9999,\"cashbackMax\":9999,\"commFailCashFloor\":0,\"commFailFloor\":0,\"floor\":0,\"mangerAuthMax\":0,\"max\":9999,\"min\":0,\"offlineRefundMax\":0,\"smallValueLimitDollars\":0,\"telAuthMax\":0,\"telPinAuthMax\":0},\"luhnCheck\":true,\"merchantPanMask\":\"101\",\"name\":\"JCB\",\"onlinePin\":{\"balance\":\"N\",\"cash\":\"Y\",\"cashback\":\"Y\",\"deposit\":\"\",\"pinChange\":\"\",\"preauth\":\"N\",\"refund\":\"Y\",\"sale\":\"Y\"},\"panLength\":\"13,14,15,16,17,18,19,\",\"parent\":false,\"passwordRequired\":{\"balance\":false,\"cash\":false,\"cashback\":false,\"completion\":false,\"deposit\":false,\"depositOffline\":false,\"forced\":false,\"moto\":false,\"offlineCash\":false,\"offlineRefund\":false,\"pinChange\":false,\"preauth\":false,\"refund\":false,\"reprint\":false,\"reversal\":false,\"sale\":false},\"printCardholderName\":true,\"productLevelBlocking\":false,\"psi\":\"J\",\"refundOnline\":true,\"rejectCtls\":false,\"rejectEmv\":false,\"schemeLabel\":\"JCB\",\"serviceCodeCheck\":true,\"servicesAllowed\":{\"balance\":false,\"cash\":true,\"cashback\":true,\"deposit\":false,\"depositOffline\":false,\"forced\":false,\"moto\":true,\"offlineCash\":false,\"offlineRefund\":false,\"pinChange\":false,\"preauth\":true,\"refund\":true,\"reversal\":true,\"sale\":true}},{\"accountSelection\":4,\"authorizationBits\":0,\"binNumber\":6,\"completionOnline\":false,\"customerPanMask\":\"101\",\"deferredAuthEnabled\":false,\"disabled\":true,\"forceOffline\":false,\"forceReferral\":false,\"forceSign\":false,\"iinRange\":\"30000000-30599999,30900000-30999999,36000000-36999999,38000000-39999999,\",\"limits\":{\"cashFloor\":0,\"cashMax\":9999,\"cashbackMax\":9999,\"commFailCashFloor\":0,\"commFailFloor\":0,\"floor\":0,\"mangerAuthMax\":0,\"max\":9999,\"min\":0,\"offlineRefundMax\":0,\"smallValueLimitDollars\":0,\"telAuthMax\":0,\"telPinAuthMax\":0},\"luhnCheck\":true,\"merchantPanMask\":\"101\",\"name\":\"Diners\",\"onlinePin\":{\"balance\":\"N\",\"cash\":\"Y\",\"cashback\":\"Y\",\"deposit\":\"\",\"pinChange\":\"\",\"preauth\":\"N\",\"refund\":\"Y\",\"sale\":\"Y\"},\"panLength\":\"13,14,15,16,17,18,19,\",\"parent\":false,\"passwordRequired\":{\"balance\":false,\"cash\":false,\"cashback\":false,\"completion\":false,\"deposit\":false,\"depositOffline\":false,\"forced\":false,\"moto\":false,\"offlineCash\":false,\"offlineRefund\":false,\"pinChange\":false,\"preauth\":false,\"refund\":false,\"reprint\":false,\"reversal\":false,\"sale\":false},\"printCardholderName\":true,\"productLevelBlocking\":false,\"psi\":\"D\",\"refundOnline\":true,\"rejectCtls\":false,\"rejectEmv\":false,\"schemeLabel\":\"DINERS\",\"serviceCodeCheck\":true,\"servicesAllowed\":{\"balance\":false,\"cash\":true,\"cashback\":true,\"deposit\":false,\"depositOffline\":false,\"forced\":false,\"moto\":true,\"offlineCash\":false,\"offlineRefund\":false,\"pinChange\":false,\"preauth\":true,\"refund\":true,\"reversal\":true,\"sale\":true}}]";
        PayCfgImpl params = new PayCfgImpl(mockedContext);
        OverrideParameters paramsOR = gsonXml.fromXml(overrideXML, OverrideParameters.class);
        HotLoadParameters paramsHL = gsonXml.fromXml(hotloadXML, HotLoadParameters.class);
        InitialParameters paramsInit = gsonXml.fromXml(initialXML, InitialParameters.class);

        IMalHardware hardware = mock(IMalHardware.class);
        when(mockedMal.getHardware()).thenReturn(hardware);
        when(hardware.hasPrinter()).thenReturn(true);

        params.loadOverrideParams("Random Customer 1",  paramsOR, 1);
        params.loadHotloadParams(paramsHL, 1);
        params.loadInitialParams(paramsInit, 1);

        Gson gson = new Gson();
        Type cardProductCfgListType = new TypeToken<ArrayList<CardProductCfg>>(){}.getType();
        List<CardProductCfg> cards = gson.fromJson(cardData, cardProductCfgListType);

        // Set our cards for our storage
        params.setCards(cards);

        // Assume our old object has been removed, now we have our new one.
        PayCfgImpl newInstanceOfParams = new PayCfgImpl(mockedContext);
        // Directly accessing the cards should empty
        assertEquals(true, newInstanceOfParams.getCards() == null);
        // Based on config we should reload our card config and allow cashback
        assertEquals(true, newInstanceOfParams.isCashTransAllowed());
        // Check our new list size is 12. (Expected size)
        assertEquals(12, newInstanceOfParams.getCards().size());
    }

}