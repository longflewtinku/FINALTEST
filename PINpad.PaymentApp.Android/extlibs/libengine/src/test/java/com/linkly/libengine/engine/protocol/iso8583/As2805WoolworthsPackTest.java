package com.linkly.libengine.engine.protocol.iso8583;

import static com.linkly.libengine.engine.EngineManager.TransType.SALE;
import static com.linkly.libengine.engine.EngineManager.TransType.SALE_AUTO;
import static com.linkly.libengine.engine.EngineManager.TransType.SALE_MOTO_AUTO;
import static com.linkly.libengine.engine.protocol.iso8583.As2805WoolworthsPack.MsgType;
import static com.linkly.libengine.engine.protocol.iso8583.As2805WoolworthsPack.UnPackResult.UNPACK_OK;
import static com.linkly.libengine.engine.protocol.iso8583.As2805WoolworthsPack.pack;
import static com.linkly.libengine.engine.protocol.iso8583.As2805WoolworthsPack.unpack;
import static com.linkly.libengine.engine.protocol.iso8583.As2805WoolworthsUtils.getCurrentDukptKeyIndex;
import static com.linkly.libengine.engine.transactions.properties.TCard.CaptureMethod.CTLS;
import static com.linkly.libengine.engine.transactions.properties.TCard.CaptureMethod.ICC;
import static com.linkly.libengine.engine.transactions.properties.TCard.CaptureMethod.MANUAL;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.ACC_TYPE_CREDIT;
import static com.linkly.libsecapp.IP2PEncrypt.ElementType.CVV;
import static com.linkly.libsecapp.IP2PEncrypt.ElementType.EXPIRY_YYMM;
import static com.linkly.libsecapp.IP2PEncrypt.ElementType.PAN;
import static com.linkly.libsecapp.IP2PEncrypt.ElementType.PIN_BLOCK;
import static com.linkly.libsecapp.IP2PEncrypt.ElementType.PIN_BLOCK_KSN;
import static com.linkly.libsecapp.IP2PEncrypt.ElementType.TRACK_2_FULL_MSR;
import static com.linkly.libsecapp.IP2PEncrypt.EncryptForStorageType.CVV_FORMAT;
import static com.linkly.libsecapp.IP2PEncrypt.EncryptForStorageType.FULL_TRACK_FORMAT;
import static com.linkly.libsecapp.IP2PSec.InstalledKeyType.AS2805;
import static com.linkly.libsecapp.IP2PSec.KeyGroup.DYNAMIC_GROUP;
import static com.linkly.libsecapp.emv.Tag.amt_auth_num;
import static com.linkly.libsecapp.emv.Tag.amt_other_num;
import static com.linkly.libsecapp.emv.Tag.appl_cryptogram;
import static com.linkly.libsecapp.emv.Tag.appl_intchg_profile;
import static com.linkly.libsecapp.emv.Tag.atc;
import static com.linkly.libsecapp.emv.Tag.crypt_info_data;
import static com.linkly.libsecapp.emv.Tag.cvm_results;
import static com.linkly.libsecapp.emv.Tag.term_cap;
import static com.linkly.libsecapp.emv.Tag.term_county_code;
import static com.linkly.libsecapp.emv.Tag.tran_date;
import static com.linkly.libsecapp.emv.Tag.tran_type;
import static com.linkly.libsecapp.emv.Tag.trans_curcy_code;
import static com.linkly.libsecapp.emv.Tag.tvr;
import static com.linkly.libsecapp.emv.Tag.unpred_num;
import static com.linkly.libsecapp.emv.Util.hexStringToByteArray;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import android.content.Context;

import com.linkly.libengine.BuildConfig;
import com.linkly.libengine.config.Config;
import com.linkly.libengine.config.PayCfg;
import com.linkly.libengine.config.paycfg.PaymentSwitch;
import com.linkly.libengine.dependencies.IDependency;
import com.linkly.libengine.engine.Engine;
import com.linkly.libengine.engine.customers.ICustomer;
import com.linkly.libengine.engine.reporting.Reconciliation;
import com.linkly.libengine.engine.reporting.ReconciliationDao;
import com.linkly.libengine.engine.reporting.ReconciliationManager;
import com.linkly.libengine.engine.transactions.TransRec;
import com.linkly.libengine.engine.transactions.properties.TAmounts;
import com.linkly.libengine.engine.transactions.properties.TAudit;
import com.linkly.libengine.engine.transactions.properties.TCard;
import com.linkly.libengine.engine.transactions.properties.TProtocol;
import com.linkly.libengine.engine.transactions.properties.TReconciliationFigures;
import com.linkly.libengine.engine.transactions.properties.TSec;
import com.linkly.libmal.IMal;
import com.linkly.libmal.IMalHardware;
import com.linkly.libmal.MalFactory;
import com.linkly.libsecapp.CardholderDataElement;
import com.linkly.libsecapp.DecryptResult;
import com.linkly.libsecapp.EncryptResult;
import com.linkly.libsecapp.IP2PEncrypt;
import com.linkly.libsecapp.IP2PSec;
import com.linkly.libsecapp.P2PEncrypt;
import com.linkly.libsecapp.P2PLib;
import com.linkly.libsecapp.emv.EmvTags;
import com.linkly.libsecapp.emv.Util;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.stubbing.Answer;

import timber.log.Timber;

public class As2805WoolworthsPackTest {

    @Mock
    IDependency dependency;

    @Mock
    TransRec transRec;

    @Mock
    TSec sec;

    @Mock
    TProtocol protocol;

    @Mock
    TAudit audit;

    @Mock
    ReconciliationDao reconciliationDao;

    @Mock
    Reconciliation reconciliation;

    @Mock
    Reconciliation orgReconciliation;

    @Mock
    TReconciliationFigures reconciliationFigures;

    @Mock
    PaymentSwitch paymentSwitch;

    @Mock
    PayCfg payCfg;

    @Mock
    TCard cardInfo;

    @Mock
    P2PLib p2PLib;

    @Mock
    P2PEncrypt p2PEncrypt;

    @Mock
    Config config;

    @Mock
    IMalHardware hardware;

    @Mock
    IMal mal;

    @Mock
    ICustomer customer;

    @Mock
    IP2PSec ip2PSec;

    // mock a singleton class
    private MockedStatic<P2PLib> p2PLibMockedStatic;
    private MockedStatic<MalFactory> malFactoryMockedStatic;

    private static String testT2Data = "5237481234567895D19111019826871591001"; // 17 digit PAN
    private static String testPan = "5237481234567895";
    private static String testExpiry = "1911";
    private static String testCvv = "810";
    private static String testPinpadSerialNumber = "0123456789012345"; // pinpad serial number

    private static String testTid = "W0112001";
    private static String testMid = "611000607000112";

    @Rule //initMocks
    public final MockitoRule rule = MockitoJUnit.rule();


    private void mockIccData() throws Exception {
        EmvTags tags = new EmvTags();
        tags.add(amt_auth_num, "000000005600");
        tags.add(amt_other_num, "000000000000");
        tags.add(term_county_code, "036");
        tags.add(tvr, "0000000000");
        tags.add(trans_curcy_code, "036");
        tags.add(tran_date, "220602");
        tags.add(tran_type, "00");
        tags.add(unpred_num, "38CB3C42");
        tags.add(appl_intchg_profile, "5C00");
        tags.add(atc, "0002");
        tags.add(term_cap, "E0E080");
        tags.add(crypt_info_data, "80");
        tags.add(appl_cryptogram, "254F892E25AE7473");
        tags.add(cvm_results, "010302");

//        tags.add(tran_type, "01");
//        tags.add(appl_pan_seqnum, "0123");
//        tags.add(auth_resp_code, "00");
//        tags.add(issuer_country_code, "036");
//        tags.add(aid, "A0000000041010");
//        tags.add(issuer_app_data, "00123456789ABCDE");
//        tags.add(term_type, "21");
//        tags.add(tsi, "1204");
        when(cardInfo.getTags()).thenReturn(tags);
    }

    public class SystemOutTree extends Timber.Tree {
        @Override
        protected void log(int priority, String tag, String message, Throwable t) {
            if (t != null) {
                System.out.println(tag + ": " + message + "\n" + t);
            } else {
                System.out.println(tag + ": " + message);
            }
        }
    }

    @Before
    public void setUp() throws Exception {
        Timber.plant(new SystemOutTree());
        ReconciliationManager.reconciliationDao = reconciliationDao;

        p2PLibMockedStatic = mockStatic(P2PLib.class);
        p2PLibMockedStatic.when(P2PLib::getInstance).thenReturn(p2PLib);

        malFactoryMockedStatic = mockStatic(MalFactory.class);
        malFactoryMockedStatic.when(MalFactory::getInstance).thenReturn(mal);

        when(p2PEncrypt.getElementLength(TRACK_2_FULL_MSR)).thenReturn(37);
        when(p2PEncrypt.decryptFromStorage(any(byte[].class), eq(FULL_TRACK_FORMAT))).thenReturn(true);
        when(p2PEncrypt.decryptFromStorage(any(byte[].class), eq(CVV_FORMAT))).thenReturn(true);
        when(p2PEncrypt.getElementLength(PIN_BLOCK)).thenReturn(8);
        when(ip2PSec.getDUKPTEncryptedPinBlock(DYNAMIC_GROUP)).thenReturn(true);
        when(p2PEncrypt.getData(PIN_BLOCK)).thenReturn("6597C8A12B8A6F7F");
        when(p2PEncrypt.getData(PIN_BLOCK_KSN)).thenReturn("62800002160000200001");
        when(p2PEncrypt.getMac(any(byte[].class), any(IP2PEncrypt.MacParameters.class), any(CardholderDataElement[].class))).thenReturn(Util.hexStringToByteArray("78D3D20600000000"));
        when(p2PEncrypt.verifyMac(any(byte[].class), any(IP2PEncrypt.MacParameters.class))).thenReturn(true);
        when(p2PEncrypt.encrypt(any(byte[].class), any(IP2PEncrypt.EncryptParameters.class), any(CardholderDataElement[].class)))
                .thenAnswer((Answer<EncryptResult>) invocation -> {
                    // Extract arguments passed to the method
                    Object[] args = invocation.getArguments();
                    byte[] message = (byte[]) args[0];
                    IP2PEncrypt.EncryptParameters params = (IP2PEncrypt.EncryptParameters) args[1];
                    CardholderDataElement[] elements = (CardholderDataElement[]) args[2];

                    String srcData = null;
                    for( CardholderDataElement element : elements ){
                        if (element == null) {
                            continue;
                        }
                        switch( element.getElementType() ) {
                            case TRACK_2_FULL_CHIP:
                            case TRACK_2_FULL_MSR:
                                srcData = testT2Data;
                                break;

                            case PAN:
                                srcData = testPan;
                                break;

                            case EXPIRY_YYMM:
                                srcData = testExpiry;
                                break;

                            case CVV:
                                srcData = testCvv;
                                break;

                            default:
                                Timber.e("unsupported element type %d", element.getElementType());
                                break;
                        }

                        if (srcData != null) {
                            switch (element.getPackFormat()) {
                                case ASCII:
                                    // direct byte substitution
                                    System.arraycopy(srcData.getBytes(), 0, message, element.getSubstitueIndex(), element.getLength());
                                    break;
                                case BCD:
                                    String bcdInputData = srcData;
                                    int bcdLength = (srcData.length() + 1) / 2;
                                    // convert data to bcd
                                    if (srcData.length() % 2 == 1) {
                                        bcdInputData += 'F';
                                    }
                                    byte[] bcdData = Util.hexStringToByteArray(bcdInputData);
                                    System.arraycopy(bcdData, 0, message, element.getSubstitueIndex(), bcdLength);
                                    break;
                            }
                        }
                    }
                    // substitute dummy pan, expiry, cvv, track2 values
                    // For example, let's just create a new EncryptResult based on some logic
                    EncryptResult result = new EncryptResult();
                    // Assume EncryptResult has a method to set data and you want to return some fixed data
                    result.setEncryptedMessage(message);
                    return result;
                });

        when(p2PLib.getIP2PEncrypt()).thenReturn(p2PEncrypt);
        when(dependency.getP2PLib()).thenReturn(p2PLib);
        when(config.getPayCfg()).thenReturn(payCfg);
        when(dependency.getConfig()).thenReturn(config);
        when(mal.getMalContext()).thenReturn(mock(Context.class));
        when(mal.getHardware()).thenReturn(hardware);
        when(hardware.getSerialNumber()).thenReturn(testPinpadSerialNumber);
        when(hardware.getModel()).thenReturn("A920");
        Engine.setDep(dependency);

        when(payCfg.getPaymentSwitch()).thenReturn(paymentSwitch);
        when(payCfg.getCardProductVersion()).thenReturn(111);
        when(payCfg.getBankTimeZone()).thenReturn("Australia/Sydney");
        when(paymentSwitch.getDefaultPktVersion()).thenReturn("000222");
        when(paymentSwitch.getDefaultEpatVersion()).thenReturn("000333");
        when(paymentSwitch.getDefaultSpotVersion()).thenReturn("000444");
        when(paymentSwitch.getDefaultFcatVersion()).thenReturn("000555");

        mockIccData();

        when(customer.getTcuKeyLength()).thenReturn(960);
        when(dependency.getCustomer()).thenReturn(customer);
        when(ip2PSec.getInstalledKeyType()).thenReturn(AS2805);
        when(ip2PSec.as2805GetKeys(960)).thenReturn(true);
        when(ip2PSec.getDUKPTKsn(DYNAMIC_GROUP)).thenReturn(Util.hexStringToByteArray("62800002160000200001"));
        when(ip2PSec.as2805GetPpid()).thenReturn(hexStringToByteArray("7750343030202020"));
        when(ip2PSec.as2805GeteKiaPpid()).thenReturn(hexStringToByteArray("0CA7E583")); // encrypted PPID
        when(ip2PSec.as2805GetSkManPkTcu()).thenReturn(hexStringToByteArray(
                "588566d270f22eacf74cd974d9c13b236c4564e2b5d16ecc080ad2ff0140837da6ca1d61b57947927a6909cb2d66027e1463b90f9f4505889f55740ac08e8261315cfdc4f8eff8bbbf965bb2f1bc4efd2dfc59a4d00616afba703962fb59ef5a4984fffd04ec32376fee595f02849e1955658ffaad8514a8d77db13df71f9ceb106246a639263be047fe30c2985d7edfc3a68c9eeae1c6f9342575fe34c0f77ec95d2d17656dd80e09da62f2bf012269c575b7298c030b4a044ea0ae05b7274d4b4c92fdb7bbadf492b2f381d6ffeca3d83a42fb8bc796db279f873a194ae22df571145abccec6a5d3f2d4a4493cfe7944bea9fd75a8279c364bc10df59f4ded"));
        when(ip2PSec.as2805GenerateSkTcuKiBlock(null)) // in real world, this will be passed as a random byte[] from 810 response for the 191 request
                .thenReturn(hexStringToByteArray(
                        "193F7461BE5378F3636FCE90A7E3D330AAC3944E47AF6E421E6B853515F0AB40B531F60366EC6D4E92F4D665E5583E384E60FF396A60ED8ACDB0E6990D45D8186CF584C7308F2DCD57AAD3C3405B2805AF9BFEA2421D41EA728225A41F287EC6B16F8CC2442916DCD67D36ACBA66A0E57BAC998FC64A17A9"));
        when(ip2PSec.as2805GeteKekPpasn(1)).thenReturn(hexStringToByteArray("A1A1A1A1A1A1A1A1"));
        when(ip2PSec.as2805GeteKekPpasn(2)).thenReturn(hexStringToByteArray("A2A2A2A2A2A2A2A2"));

        when(p2PLib.getIP2PSec()).thenReturn(ip2PSec);
    }

    @After
    public void tearDown() throws Exception {
        p2PLibMockedStatic.close();
        malFactoryMockedStatic.close();
    }

    private void mockPurchaseInsertTransDetails() {
        TAmounts amounts = new TAmounts();
        amounts.setAmount(5600L);
        when(cardInfo.getCaptureMethod()).thenReturn(ICC);
        when(cardInfo.isCtlsCaptured()).thenReturn(true);
        when(cardInfo.getCvmType()).thenReturn(TCard.CvmType.ENCIPHERED_ONLINE_PIN);
        when(transRec.getTransType()).thenReturn(SALE_AUTO); // 'AUTO' means POS initiated
        when(transRec.isSale()).thenReturn(true);
        when(protocol.getRRN()).thenReturn("124412000011");
        when(protocol.getAccountType()).thenReturn(ACC_TYPE_CREDIT);

        when(transRec.getAmounts()).thenReturn(amounts);
        when(transRec.getSecurity()).thenReturn(sec);
        when(sec.getKsn()).thenReturn("62800002160000200001");
        when(cardInfo.isManual()).thenReturn(false);
        when(transRec.getCard()).thenReturn(cardInfo);
        when(protocol.getStan()).thenReturn(11);
        when(transRec.getProtocol()).thenReturn(protocol);
        when(audit.getTransDateTimeAsString("MMddHHmmss")).thenReturn("0809040103");
        when(audit.getTerminalId()).thenReturn("W0112001");
        when(audit.getMerchantId()).thenReturn("611000607000112");
        when(transRec.getAudit()).thenReturn(audit);
        when(paymentSwitch.getAiic()).thenReturn("00062800000");
        when(payCfg.getPaymentSwitch()).thenReturn(paymentSwitch);
        when(dependency.getPayCfg()).thenReturn(payCfg);
    }

    @Test
    public void testPackFinancialRequestMessage() {
        mockPurchaseInsertTransDetails();
        when(paymentSwitch.isDisableSecurity()).thenReturn(true);

        byte[] packedMessage = pack(dependency, transRec, MsgType.AUTH, false);

        String expected = "0200" + //msg type
                "B020068128C292900000000000040001" +  // bitmaps
                "003000" + // proc code
                "000000005600" + // total amount
                "000011" + // stan
                "0051" + // POSE
                "0000" + // CSN
                "04" + // POSC
                "313100062800000F" + // AIIC LLVAR
                "3337" + testT2Data + "F" + // track 2 LLVAR
                "313234343132303030303131" + // RRN
                "5730313132303031" + // TID
                "363131303030363037303030313132" + // MID
                "30303654434330375C" + // additional data LLLVAR(TCC07\)
                "0036" + // currency code
                "6597C8A12B8A6F7F" + // PIN block
                // de 55, icc data
                "3038369F02060000000056009F03060000000000009F1A020036950500000000005F2A0200369A032206029C01009F370438CB3C4282025C009F360200029F3303E0E0809F2701809F2608254F892E25AE74739F3403010302" +
                "000000000000" + // amount cash
                "30303130" + // de60
                "3031310562800002160000200001" + // de110 - enc data/KSN
                "78D3D20600000000"; // MAC

        String actual = Util.byteArrayToHexString(packedMessage);
        assertEquals(expected, actual);
    }

    @Test
    public void testPackFinancialRequestMessageWithEncrypt() {
        mockPurchaseInsertTransDetails();
        when(paymentSwitch.isDisableSecurity()).thenReturn(false);

        when(p2PEncrypt.encrypt(any(byte[].class), any(IP2PEncrypt.EncryptParameters.class), any(CardholderDataElement[].class)))
                .thenAnswer((Answer<EncryptResult>) invocation -> {
                            EncryptResult encResult = new EncryptResult();
                            encResult.setEncryptedMessage(Util.hexStringToByteArray("348816ABF5B7999CC7DCB9E41D3AFD9F1568AA66A1EAC577DC068ED21DC013F6528E2B74EFA637D351728AEA0CADFA821CC761D24AC5BDF2C0DF805D2B0790BF952A6802258E86379C3586DEA3A2B69F69D4790D2625E16C34C315361DC712F4CDD6B53EE44C7B5D1249E10AAF22AFE3F4135E8F9938099B8DAA393BE096E0EA51ED658A306A1651B9C654B97E8C590E32B1683FEADBB59F375DE5871154B5A40DE1C47785873ECA53A6E0A1A153C35FE31683FE5F46719283B19757E93F885A7F724EE35AE4FDD93D603CE692DE06E03DE3434E2F27F354D40A38BCE87A8F908FE4D033718EAA9F4C029ABC28621C29"));
                            encResult.setDukptKsn(Util.hexStringToByteArray("62800002160000200001"));
                            return encResult;
                        }
                );

        byte[] packedMessage = pack(dependency, transRec, MsgType.AUTH, false);

        String expected = "0200"+
                "B020068128C292900000000000040001"+ // bitmaps in clear
                "AA66A1"+ // proc code, encrypted
                "EAC577DC068E"+ // total amt, encrypted
                "000011"+ // stan in clear
                "13F6"+
                "528E"+
                "2B"+
                "3131A637D351728A"+ // pos entry mode, csn, pos condition code, all encrypted
                "3337ADFA821CC761D24AC5BDF2C0DF805D2B0790BF"+ // track 2, length in clear, rest encrypted
                "952A6802258E86379C3586DE"+ // rrn, encrypted
                "5730313132303031"+ // tid in clear
                "363131303030363037303030313132"+ // mid in clear
                "3030367B5D1249E10A"+ // de47 addtiional data, length is clear but data encrypted
                "AF22"+
                "AFE3F4135E8F9938"+ // currency code and pin block, encrypted
                // de 55, length in clear but data encrypted
                "303836AA393BE096E0EA51ED658A306A1651B9C654B97E8C590E32B1683FEADBB59F375DE5871154B5A40DE1C47785873ECA53A6E0A1A153C35FE31683FE5F46719283B19757E93F885A7F724EE35AE4FDD93D603CE692DE06"+
                "E03DE3434E2F" + // amount cash
                "303031D4"+ // de 60 length is in clear, but 1 data byte encrypted
                "3031310562800002160000200001"+ // de110 KSN completely in the clear
                "9F4C029ABC28621C"; // MAC

        String actual = Util.byteArrayToHexString(packedMessage);
        assertEquals(expected, actual);
    }


    @Test
    public void testPackAdviceRequestMessageWithEncrypt() {
        mockAdviceTransaction();
        when(paymentSwitch.isDisableSecurity()).thenReturn(false);

        when(p2PEncrypt.encrypt(any(byte[].class), any(IP2PEncrypt.EncryptParameters.class), any(CardholderDataElement[].class)))
                .thenAnswer((Answer<EncryptResult>) invocation -> {
                            EncryptResult encResult = new EncryptResult();
                            encResult.setEncryptedMessage(Util.hexStringToByteArray("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF"));
                            encResult.setDukptKsn(Util.hexStringToByteArray("62800002160000200001"));
                            return encResult;
                        }
                );

        byte[] packedMessage = pack(dependency, transRec, MsgType.ADVICE, false);

        String expected = "0220"+
                "B238068128C286900000000000040001"+ // bitmaps in clear
                "FFFFFF"+ // proc code, encrypted
                "FFFFFFFFFFFF"+ // total amt, encrypted
                "FFFFFFFFFF"+ // de7 trans date/time
                "001234"+ // de11 stan in clear
                "FFFFFF" + // de12 local tran time
                "FFFF"+// de13 local tran date
                "FFFF"+ // de22 pos entry mode
                "FFFF"+ // de23 card seq num
                "FF"+ // de25 pos entry mode, csn, pos condition code, all encrypted
                "3131FFFFFFFFFFFF" + // de32 AIIC LLVAR
                "3337FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF"+ // track 2, length in clear, rest encrypted
                "FFFFFFFFFFFFFFFFFFFFFFFF"+ // rrn, encrypted
                "5730313132303031"+ // tid in clear
                "363131303030363037303030313132"+ // mid in clear
                "303036FFFFFFFFFFFF"+ // de47 addtiional data, length is clear but data encrypted
                "FFFF"+ // currency code de49
                "303430FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF"+ // de54 additional amounts
                // de 55, length in clear but data encrypted
                "303836FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF"+
                "FFFFFFFFFFFF" + // amount cash
                "303031FF"+ // de 60 length is in clear, but 1 data byte encrypted
                "3031310562800002160000200001"+ // de110 KSN completely in the clear
                "FFFFFFFFFFFFFFFF"; // MAC

        String actual = Util.byteArrayToHexString(packedMessage);
        assertEquals(expected, actual);
    }

    @Test
    public void testPackManPanFinancialRequestMessage() {
        mockPurchaseInsertTransDetails();
        when(transRec.getTransType()).thenReturn(SALE_MOTO_AUTO); // 'AUTO' means POS initiated
        when(cardInfo.getCaptureMethod()).thenReturn(MANUAL);
        when(cardInfo.isManual()).thenReturn(true);
        when(cardInfo.isCtlsCaptured()).thenReturn(false);
        when(p2PEncrypt.getElementLength(TRACK_2_FULL_MSR)).thenReturn(0);
        when(p2PEncrypt.getElementLength(PAN)).thenReturn(16);
        when(p2PEncrypt.getElementLength(EXPIRY_YYMM)).thenReturn(4);
        when(p2PEncrypt.getElementLength(CVV)).thenReturn(3);
        when(paymentSwitch.isDisableSecurity()).thenReturn(true);

        byte[] packedMessage = pack(dependency, transRec, MsgType.AUTH, false);

        String expected = "0200" + //msg type
                "F024068108C290900000000000040001" +  // bitmaps
                "3136" + testPan + // de2 pan
                "003000" + // de3 proc code
                "000000005600" + // de4 total amount
                "000011" + // de11 stan
                testExpiry + // de14 expiry
                "0011" + // de22 POSE
                "0000" + // de23 CSN
                "08" + // de25 POSC
                "313100062800000F" + // de32 AIIC LLVAR
                "313234343132303030303131" + // de37 RRN
                "5730313132303031" + // de41 TID
                "363131303030363037303030313132" + // de42 MID
                "30323954434330375C434349315C4343563831305C434354315C45434D30315C" + // de47 additional data LLLVAR(TCC07\CCI1\CCV810\CCT1\ECM01\)
                "0036" + // de49 currency code
                "6597C8A12B8A6F7F" + // de52 PIN block
                "000000000000" + // de57 amount cash
                "30303130" + // de60
                "3031310562800002160000200001" + // de110 - enc data/KSN
                "78D3D20600000000"; // de64 MAC

        String actual = Util.byteArrayToHexString(packedMessage);
        assertEquals(expected, actual);
    }

    @Test
    public void testEncryptedPackManPanFinancialRequestMessage() {
        mockPurchaseInsertTransDetails();
        when(transRec.getTransType()).thenReturn(SALE_MOTO_AUTO); // 'AUTO' means POS initiated
        when(cardInfo.getCaptureMethod()).thenReturn(MANUAL);
        when(cardInfo.isManual()).thenReturn(true);
        when(cardInfo.isCtlsCaptured()).thenReturn(false);
        when(p2PEncrypt.getElementLength(TRACK_2_FULL_MSR)).thenReturn(0);
        when(p2PEncrypt.getElementLength(PAN)).thenReturn(16);
        when(p2PEncrypt.getElementLength(EXPIRY_YYMM)).thenReturn(4);
        when(p2PEncrypt.getElementLength(CVV)).thenReturn(3);
        when(paymentSwitch.isDisableSecurity()).thenReturn(false);

        when(p2PEncrypt.encrypt(any(byte[].class), any(IP2PEncrypt.EncryptParameters.class), any(CardholderDataElement[].class)))
                .thenAnswer((Answer<EncryptResult>) invocation -> {
                            EncryptResult encResult = new EncryptResult();
                            encResult.setEncryptedMessage(Util.hexStringToByteArray("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF"));
                            encResult.setDukptKsn(Util.hexStringToByteArray("62800002160000200001"));
                            return encResult;
                        }
                );

        byte[] packedMessage = pack(dependency, transRec, MsgType.AUTH, false);

        String expected = "0200" + //msg type
                "F024068108C290900000000000040001" +  // bitmaps
                "3136" + "FFFFFFFFFFFFFFFF" + // de2 pan
                "FFFFFF" + // de3 proc code
                "FFFFFFFFFFFF" + // de4 total amount
                "000011" + // de11 stan
                "FFFF" + // de14 expiry
                "FFFF" + // de22 POSE
                "FFFF" + // de23 CSN
                "FF" + // de25 POSC
                "3131FFFFFFFFFFFF" + // de32 AIIC LLVAR
                "FFFFFFFFFFFFFFFFFFFFFFFF" + // de37 RRN
                "5730313132303031" + // de41 TID
                "363131303030363037303030313132" + // de42 MID
                "303239FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF" + // de47 additional data LLLVAR(TCC07\CCI1\CCV810\CCT1\ECM01\)
                "FFFF" + // de49 currency code
                "FFFFFFFFFFFFFFFF" + // de52 PIN block
                "FFFFFFFFFFFF" + // de57 amount cash
                "303031FF" + // de60
                "3031310562800002160000200001" + // de110 - enc data/KSN
                "FFFFFFFFFFFFFFFF"; // de64 MAC

        String actual = Util.byteArrayToHexString(packedMessage);
        assertEquals(expected, actual);
    }


    @Test
    public void testPackLogonRequestMessage() {
        // only execute in debug unit tests, not release. as no benefit, and release version can change and cause this test to fail
        if (BuildConfig.DEBUG) {
            mockPurchaseInsertTransDetails();
            when(paymentSwitch.isDisableSecurity()).thenReturn(true);

            byte[] packedMessage = pack(dependency, transRec, MsgType.NETWORK, true);

            String expected = "0800" + //msg type
                    "8020000100C180000400000000040001" +  // bitmaps
                    "000000" + // stan for registration message should be zeros. but for 'normal logon' it should be real stan
                    "313100062800000F" + // AIIC LLVAR
                    "5730313132303031" + // TID
                    "363131303030363037303030313132" + // MID
                    "303335" + // de48 length
                    testPinpadSerialNumber +
                    "04" + // key management version
                    "610105" + // pinpad version NOTE: debug builds have this hard-coded. so shouldn't fail in pipeline if version changes
                    "000111" + // cpat ver
                    "000222" + // pkt ver
                    "000333" + // epat ver
                    "000444" + // spot ver
                    "000555" + // fcat ver
                    "4139323020202020" + // pinpad model A920
                    "0036" + // de49 currency code
                    "0170" + // de70 NMIC
                    "3031310562800002160000200001" + // de110 - enc data/KSN
                    "78D3D20600000000"; // MAC

            String actual = Util.byteArrayToHexString(packedMessage);
            assertEquals(expected, actual);
        }
    }


    @Test
    public void testPackLogonRequestMessageWithEncrypt() {
        // only execute in debug unit tests, not release. as no benefit, and release version can change and cause this test to fail
        if (BuildConfig.DEBUG) {
            mockPurchaseInsertTransDetails();
            when(paymentSwitch.isDisableSecurity()).thenReturn(false);

            when(p2PEncrypt.encrypt(any(byte[].class), any(IP2PEncrypt.EncryptParameters.class), any(CardholderDataElement[].class)))
                    .thenAnswer((Answer<EncryptResult>) invocation -> {
                                EncryptResult encResult = new EncryptResult();
                                encResult.setEncryptedMessage(Util.hexStringToByteArray("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF"));
                                encResult.setDukptKsn(Util.hexStringToByteArray("62800002160000200001"));
                                return encResult;
                            }
                    );

            byte[] packedMessage = pack(dependency, transRec, MsgType.NETWORK, true);

            String expected = "0800" + //msg type
                    "8020000100C180000400000000040001" +  // bitmaps
                    "000000" + // stan for registration message should be zeros. but for 'normal logon' it should be real stan
                    "313100062800000F" + // AIIC LLVAR
                    "5730313132303031" + // TID
                    "363131303030363037303030313132" + // MID
                    "303335"+ // de48 length
                    testPinpadSerialNumber+
                    "04"+ // key management version
                    "610105" + // pinpad version. note that debug builds are hard-coded to return this. shouldn't break pipeline validations
                    "000111" + // cpat ver
                    "000222" + // pkt ver
                    "000333" + // epat ver
                    "000444" + // spot ver
                    "000555" + // fcat ver
                    "4139323020202020" + // pinpad model A920
                    "0036" + // de49 currency code
                    "0170" + // de70 NMIC
                    "3031310562800002160000200001" + // de110 - enc data/KSN
                    "78D3D20600000000"; // MAC

            String actual = Util.byteArrayToHexString(packedMessage);

            assertEquals(expected, actual);
        }
    }


    @Test
    public void testUnpackFinancialMessage() {
        mockPurchaseInsertTransDetails();
        when(paymentSwitch.isDisableSecurity()).thenReturn(false);

        when(p2PEncrypt.decrypt(any(byte[].class), any(IP2PEncrypt.EncryptParameters.class)))
                .thenAnswer((Answer<DecryptResult>) invocation -> {
                            DecryptResult result = new DecryptResult();
                            // NOTE: I've manually fiddled the 'cleartext' fields - MTI, bitmaps, DE11, 41, 42, 110, and length indicators
                            // because these are cleartext in the incoming message, the decrypted value will be random. cleartext values must be copied from raw received msg
                            result.setDecryptedMessage(Util.hexStringToByteArray(
                                    "AB1C"+
                                    "BEA19830912731298DEF1072137AB0FA"+
                                    "003000"+
                                    "000000005600"+
                                    "7AC01E"+
                                    "13442106020602"+
                                    "A41E00062800000F"+
                                    "3030"+
                                    "17102398ABE1930C"+
                                    "128E710F28751A237ED89182312374"+
                                    "0ABCDE544F4B35323337343838303030303034353030333631395C"+
                                    "0036"+
                                    "ABDEF20562800002160000200001"+
                                    "E75C12BD0000000000000000000000"));
                            result.setDukptKsn(Util.hexStringToByteArray("62800002160000200001"));
                            return result;
                        }
                );

        byte[] inputBytes = Util.hexStringToByteArray("0210"+
                "B03A000102C280000000000000040001"+ // bitmaps
                "7E54C5"+ // de3, proc code
                "56362CE82258"+ // de4, trans amt
                "000011"+ // de11, stan
                "8272ADF9D77C0531312F59316A268891025730313132303031363131303030363037303030313132303234DBF661EE3B18B6210E5743FDC4C323D03F3FAC9354E30CF0E330"+
                "3031310562800002160000200001"+ // KSN
                "AFC7D4E8F9AF092E" // mac
        );
        As2805WoolworthsPack.UnPackResult result;
        As2805WoolworthsProto.ResponseAction respAct = new As2805WoolworthsProto.ResponseAction();
        result = unpack(dependency, inputBytes, transRec, respAct, MsgType.AUTH);
        assertEquals(UNPACK_OK, result);
    }



    @Test
    public void testUnpackReversalMessage() throws Exception {
        mockPurchaseInsertTransDetails();
        when(audit.getTerminalId()).thenReturn("B6666020");
        when(audit.getMerchantId()).thenReturn("6110006020B6666");
        when(protocol.getStan()).thenReturn(4);
        when(paymentSwitch.isDisableSecurity()).thenReturn(false);

        when(p2PEncrypt.decrypt(any(byte[].class), any(IP2PEncrypt.EncryptParameters.class)))
                .thenAnswer((Answer<DecryptResult>) invocation -> {
                            DecryptResult result = new DecryptResult();
                            // NOTE: I've manually fiddled the 'cleartext' fields - MTI, bitmaps, DE11, 41, 42, 110, and length indicators
                            // because these are cleartext in the incoming message, the decrypted value will be random. cleartext values must be copied from raw received msg
                            result.setDecryptedMessage(Util.hexStringToByteArray(
                                "B22A883FFA9915AE4B595B899F8F85727D470030000000000010008508060425FDBA00062800000F3030A6916DF87BE53BAE5E2208DB7952A4C20EE5F66246214410C955544F4B35323436353138303030303230303031363133345C00369EEC0969336BFDAE9B38F7686A74CDD3C34800000000"
                            ));
                            result.setDukptKsn(Util.hexStringToByteArray("62800002160000200013"));
                            return result;
                        }
                );

        byte[] encryptedMsg = Util.hexStringToByteArray(
                "0430B022000102C28000000000000004000166F809507D17ED3749000004EEEF3131D857AF64B24536CF423636363630323036313130303036303230423636363630323496F8B9DAEC9F46752D993E068BEF798B6CBEFD5DEE55E6561BE03031310562800002160000200013AA6CD6A0AB8CAF13"                                    
        );

        As2805WoolworthsPack.UnPackResult result;
        As2805WoolworthsProto.ResponseAction respAct = new As2805WoolworthsProto.ResponseAction();
        result = unpack(dependency, encryptedMsg, transRec, respAct, MsgType.REVERSAL);
        assertEquals(UNPACK_OK, result);
    }

    @Test
    public void testUnpack210Message() throws Exception {
        mockPurchaseInsertTransDetails();
        when(paymentSwitch.isDisableSecurity()).thenReturn(false);
        when(audit.getTerminalId()).thenReturn("B6666020");
        when(audit.getMerchantId()).thenReturn("6110006020B6666");
        when(protocol.getStan()).thenReturn(7);

        when(p2PEncrypt.decrypt(any(byte[].class), any(IP2PEncrypt.EncryptParameters.class)))
                .thenAnswer((Answer<DecryptResult>) invocation -> {
                            DecryptResult result = new DecryptResult();
                            // NOTE: I've manually fiddled the 'cleartext' fields - MTI, bitmaps, DE11, 41, 42, 110, and length indicators
                            // because these are cleartext in the incoming message, the decrypted value will be random. cleartext values must be copied from raw received msg
                            result.setDecryptedMessage(Util.hexStringToByteArray(
                                    "4D77049ECBF53974B927D7882F6ACCF9055200300000000000100079075413510504240425FFD800062800000F3936BC2C190F3C2844623D7934AF9502C089E8D2731863D9BB2169A5544F4B35323436353138303030303230303031363133345C0036D8EFA03FD1F17241849F12B0CCF18C4CC23000000000"
                            ));
                            result.setDukptKsn(Util.hexStringToByteArray("62800002160000200017"));
                            return result;
                        }
                );

        byte[] encryptedMsg = Util.hexStringToByteArray(
                "0210"+
                "B03A000102C280000000000000040001"+
                "003000"+"000000001000"+
                "000007"+
                "13510504240425313100062800000F39364236363636303230363131303030363032304236363636303234544F4B35323436353138303030303230303031363133345C003630313105628000021600002000178C4CC23000000000"
        );

//        byte[] decryptedMsg = decryptMsg(encryptedMsg);
//        assertEquals(Util.byteArrayToHexString(expectedDecryptedMsg), Util.byteArrayToHexString(decryptedMsg));
        As2805WoolworthsPack.UnPackResult result;
        As2805WoolworthsProto.ResponseAction respAct = new As2805WoolworthsProto.ResponseAction();
        result = unpack(dependency, encryptedMsg, transRec, respAct, MsgType.AUTH);
        assertEquals(UNPACK_OK, result);
    }

    @Test
    public void test_getCurrentDukptKeyIndex() {
        when(ip2PSec.getDUKPTKsn(DYNAMIC_GROUP)).thenReturn(Util.hexStringToByteArray("62800002160000200001"));
        assertEquals(1, getCurrentDukptKeyIndex());

        when(ip2PSec.getDUKPTKsn(DYNAMIC_GROUP)).thenReturn(Util.hexStringToByteArray("628000021600002F4240"));
        assertEquals(1000000, getCurrentDukptKeyIndex());

        when(ip2PSec.getDUKPTKsn(DYNAMIC_GROUP)).thenReturn(Util.hexStringToByteArray("62800002160000300001"));
        assertEquals(1048577, getCurrentDukptKeyIndex());

        when(ip2PSec.getDUKPTKsn(DYNAMIC_GROUP)).thenReturn(Util.hexStringToByteArray("628000021600003FFFFF"));
        assertEquals(2097151, getCurrentDukptKeyIndex());
    }

    //

    private void mockReversalTransaction(String dateTime) {
        TSec sec = mock(TSec.class);
        TAmounts amounts = new TAmounts();
        amounts.setAmount(1000L);
        amounts.setTip(100L);
        amounts.setSurcharge(10L);
        amounts.setCashbackAmount(2000L);
        when(cardInfo.getCaptureMethod()).thenReturn(CTLS);
        when(transRec.getTransType()).thenReturn(SALE);
        when(sec.getEncTrack2()).thenReturn("EEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEE");
        when(sec.getCvv()).thenReturn("353637");
        when(ip2PSec.getInstalledKeyType()).thenReturn(AS2805);
        when(transRec.getSecurity()).thenReturn(sec);
        when(protocol.getRRN()).thenReturn("000000000013");
        when(transRec.getAmounts()).thenReturn(amounts);
        when(transRec.getSecurity()).thenReturn(sec);
        when(sec.getKsn()).thenReturn("62800002160000200001");
        when(cardInfo.isManual()).thenReturn(false);
        when(transRec.getCard()).thenReturn(cardInfo);
        when(protocol.getStan()).thenReturn(1234);
        when(transRec.getProtocol()).thenReturn(protocol);
        when(audit.getTransDateTimeAsString("ddMMHHmmss")).thenReturn(dateTime);
        when(payCfg.getStid()).thenReturn(testTid);
        when(payCfg.getMid()).thenReturn(testMid);
        when(audit.getTerminalId()).thenReturn(testTid);
        when(audit.getMerchantId()).thenReturn(testMid);
        when(transRec.getAudit()).thenReturn(audit);
        when(paymentSwitch.getAiic()).thenReturn("00062800000");
        when(paymentSwitch.isDisableSecurity()).thenReturn(true);
        when(payCfg.getPaymentSwitch()).thenReturn(paymentSwitch);
        when(dependency.getPayCfg()).thenReturn(payCfg);
        when(payCfg.getCardProductVersion()).thenReturn(111);

        when(payCfg.isIncludedOrginalStandInRec()).thenReturn(true);
        when(payCfg.isReversalCopyOriginal()).thenReturn(false);
        when(protocol.getOriginalStan()).thenReturn(1233);
    }

    private void mockAdviceTransaction() {
        TSec sec = mock(TSec.class);
        TAmounts amounts = new TAmounts();
        amounts.setAmount(1000L);
        amounts.setTip(100L);
        amounts.setSurcharge(10L);
        amounts.setCashbackAmount(2000L);
        amounts.setCurrency("036");

        when(cardInfo.getCaptureMethod()).thenReturn(CTLS);
        when(transRec.getTransType()).thenReturn(SALE);
        when(sec.getEncTrack2()).thenReturn("EEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEE");
        when(sec.getCvv()).thenReturn("353637");
        when(transRec.getSecurity()).thenReturn(sec);
        when(protocol.getRRN()).thenReturn("000000000013");


        when(transRec.getAmounts()).thenReturn(amounts);
        when(transRec.getSecurity()).thenReturn(sec);
        when(sec.getKsn()).thenReturn("62800002160000200001");
        when(cardInfo.isManual()).thenReturn(false);
        when(transRec.getCard()).thenReturn(cardInfo);
        when(protocol.getStan()).thenReturn(1234);
        when(transRec.getProtocol()).thenReturn(protocol);
        when(audit.getTransDateTimeAsString("MMddHHmmss")).thenReturn("0809040103");
        when(audit.getTransDateTimeAsString("HHmmss", "Australia/Sydney")).thenReturn("080904");
        when(audit.getTransDateTimeAsString("MMdd", "Australia/Sydney")).thenReturn("0423");
        when(audit.getTransDateTime()).thenReturn(123456789012L);
        when(payCfg.getStid()).thenReturn(testTid);
        when(payCfg.getMid()).thenReturn(testMid);
        when(audit.getTerminalId()).thenReturn(testTid);
        when(audit.getMerchantId()).thenReturn(testMid);
        when(transRec.getAudit()).thenReturn(audit);
        when(paymentSwitch.getAiic()).thenReturn("00062800000");
        when(paymentSwitch.isDisableSecurity()).thenReturn(true);
        when(payCfg.getPaymentSwitch()).thenReturn(paymentSwitch);
        when(dependency.getPayCfg()).thenReturn(payCfg);
        when(transRec.getEmvTagsString()).thenReturn("9F02060000000056009F03060000000000009F1A020036950500000000005F2A0200369A032206029C01009F370438CB3C4282025C009F360200029F3303E0E0809F2701809F2608254F892E25AE74739F3403010302");
    }

}