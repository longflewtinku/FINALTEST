package com.linkly.libengine.engine.protocol.iso8583;

import static com.linkly.libengine.engine.EngineManager.TransType.COMPLETION;
import static com.linkly.libengine.engine.EngineManager.TransType.PREAUTH;
import static com.linkly.libengine.engine.EngineManager.TransType.RECONCILIATION;
import static com.linkly.libengine.engine.EngineManager.TransType.SALE;
import static com.linkly.libengine.engine.protocol.IProto.RejectReasonType.MAC_FAILED;
import static com.linkly.libengine.engine.protocol.IProto.RejectReasonType.SIGNATURE_REJECTED;
import static com.linkly.libengine.engine.protocol.iso8583.As2805TillPack.MsgType;
import static com.linkly.libengine.engine.protocol.iso8583.As2805TillPack.UnPackResult.UNPACK_OK;
import static com.linkly.libengine.engine.protocol.iso8583.As2805TillPack.pack;
import static com.linkly.libengine.engine.protocol.iso8583.As2805TillPack.unpack;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.As2805Till.Bit.DE_118_CASHOUTS_NUMBER;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.As2805Till.Bit.DE_119_CASHOUTS_AMOUNT;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.As2805Till.Bit.DE_128_MAC;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583.Bit._003_PROC_CODE;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583.Bit._011_SYS_TRACE_AUDIT_NUM;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583.Bit._012_LOCAL_TRAN_TIME;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583.Bit._013_LOCAL_TRAN_DATE;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583.Bit._015_SETTLEMENT_DATE;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583.Bit._032_ACQUIRING_INST_ID_CODE;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583.Bit._039_RESPONSE_CODE;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583.Bit._041_CARD_ACCEPTOR_TERMINAL_ID;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583.Bit._042_CARD_ACCEPTOR_ID_CODE;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583.Bit._048_ADDITIONAL_DATA;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583.Bit._074_CREDITS_NUMBER;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583.Bit._075_CREDITS_REVERSAL_NUMBER;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583.Bit._076_DEBITS_NUMBER;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583.Bit._077_DEBITS_REVERSAL_NUMBER;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583.Bit._078_TRANSFER_NUMBER;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583.Bit._079_TRANSFER_REVERSAL_NUMBER;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583.Bit._080_INQUIRIES_NUMBER;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583.Bit._081_AUTHORISATIONS_NUMBER;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583.Bit._086_CREDITS_AMOUNT;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583.Bit._087_CREDITS_REVERSAL_AMOUNT;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583.Bit._088_DEBITS_AMOUNT;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583.Bit._089_DEBITS_REVERSAL_AMOUNT;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583.Bit._097_AMOUNT_NET_SETTLEMENT;
import static com.linkly.libengine.engine.transactions.properties.TCard.CaptureMethod.CTLS;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.AuthMethod.EFB_AUTHORISED;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.AuthMethod.LINK_DOWN_EFB_AUTHORISED;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.AuthMethod.OFFLINE_EFB_AUTHORISED;
import static com.linkly.libsecapp.IP2PEncrypt.ElementType.PIN_BLOCK;
import static com.linkly.libsecapp.IP2PEncrypt.ElementType.TRACK_2_FULL_MSR;
import static com.linkly.libsecapp.IP2PEncrypt.EncryptForStorageType.CVV_FORMAT;
import static com.linkly.libsecapp.IP2PEncrypt.EncryptForStorageType.FULL_TRACK_FORMAT;
import static com.linkly.libsecapp.IP2PSec.InstalledKeyType.AS2805;
import static com.linkly.libsecapp.emv.Util.hexStringToByteArray;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import android.content.Context;

import com.linkly.libengine.config.Config;
import com.linkly.libengine.config.PayCfg;
import com.linkly.libengine.config.paycfg.PaymentSwitch;
import com.linkly.libengine.dependencies.IDependency;
import com.linkly.libengine.engine.Engine;
import com.linkly.libengine.engine.customers.ICustomer;
import com.linkly.libengine.engine.protocol.iso8583.openisoj.As2805Till;
import com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583;
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
import com.linkly.libsecapp.IP2PSec;
import com.linkly.libsecapp.P2PEncrypt;
import com.linkly.libsecapp.P2PLib;
import com.linkly.libsecapp.emv.Util;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

public class As2805TillPackTest {

    @Mock
    IDependency dependency;

    @Mock
    TransRec transRec;

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
    IMal mal;

    @Mock
    ICustomer customer;

    @Mock
    IP2PSec ip2PSec;

    // mock a singleton class
    private MockedStatic<P2PLib> p2PLibMockedStatic;
    private MockedStatic<MalFactory> malFactoryMockedStatic;

    @Rule //initMocks
    public final MockitoRule rule = MockitoJUnit.rule();

    @Before
    public void setUp() throws Exception {
        ReconciliationManager.reconciliationDao = reconciliationDao;

        p2PLibMockedStatic = mockStatic(P2PLib.class);
        p2PLibMockedStatic.when(P2PLib::getInstance).thenReturn(p2PLib);

        malFactoryMockedStatic = mockStatic(MalFactory.class);
        malFactoryMockedStatic.when(MalFactory::getInstance).thenReturn(mal);

        when(p2PEncrypt.getElementLength(TRACK_2_FULL_MSR)).thenReturn(37);
        when(p2PEncrypt.decryptFromStorage(any(byte[].class), eq(FULL_TRACK_FORMAT))).thenReturn(true);
        when(p2PEncrypt.decryptFromStorage(any(byte[].class), eq(CVV_FORMAT))).thenReturn(true);
        when(p2PEncrypt.getElementLength(PIN_BLOCK)).thenReturn(8);
        when(p2PEncrypt.getData(PIN_BLOCK)).thenReturn("2A2A2A2A2A2A2A2A");
        when(p2PLib.getIP2PEncrypt()).thenReturn(p2PEncrypt);
        when(dependency.getP2PLib()).thenReturn(p2PLib);
        when(config.getPayCfg()).thenReturn(payCfg);
        when(dependency.getConfig()).thenReturn(config);
        when(mal.getMalContext()).thenReturn(mock(Context.class));
        Engine.setDep(dependency);

        when(customer.getTcuKeyLength()).thenReturn(960);
        when(dependency.getCustomer()).thenReturn(customer);
        when(ip2PSec.getInstalledKeyType()).thenReturn(AS2805);
        when(ip2PSec.as2805GetKeys(960)).thenReturn(true);
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

    /*
    Field	Field Name	Size	Attrib	Octets	M/C	Comment
    ---	Message Type Id	4	n	2	M	0800
    ---	Primary Bit Map	4	b	8	M
    001	Secondary Bit Map	64	b	8	M
    011	System Trace Audit Number	6	n	3	M	Set to Zeros
    032	Acquiring Institution Id	11	n	8	M
    041	Card Acceptor Terminal Id	8	ans	8	M
    042	Card Acceptor Merchant Id	15	ans	15	M
    048	Additional Data Private:
    Data Length
    Key Management Version
    Signed Terminal key
    PINpad Id	...999
    03 (Triple DES)
    PINpad Id
    070	Network Management Information Code	3	n	2	M	191 - Security initialization sign on
     */
    @Test
    public void shouldPackRsaLogonRequestMessage() {
        IMalHardware iMalHardware = mock(IMalHardware.class);
        when(iMalHardware.getAppVersion()).thenReturn("4444");
        when(mal.getHardware()).thenReturn(iMalHardware);
        TSec sec = mock(TSec.class);
        when(transRec.getSecurity()).thenReturn(sec);
        when(transRec.getProtocol()).thenReturn(protocol);
        when(audit.getTerminalId()).thenReturn("N9393001");
        when(audit.getMerchantId()).thenReturn("057998206712988");

        when(transRec.getAudit()).thenReturn(audit);
        when(paymentSwitch.getAiic()).thenReturn("00000407643");
        when(payCfg.getPaymentSwitch()).thenReturn(paymentSwitch);
        when(dependency.getPayCfg()).thenReturn(payCfg);

        byte[] packedMessage = pack(dependency, transRec, MsgType.NETWORK, "191");

        String expected =
                "0800" + // message type
                "8020000100C10000" + // primary bitmap
                "0400000000000000" + // secondary bitmap
                "000000" + // stan
                "1100000407643F" + // AIIC
                "4E39333933303031" + // TID
                "303537393938323036373132393838" + // MID
                "323635" + // data length
                "03" + // Key managment version (Triple DES)
                "588566D270F22EACF74CD974D9C13B236C4564E2B5D16ECC080AD2FF0140837DA6CA1D61B57947927A6909CB2D66027E1463B90F9F4505889F55740AC08E8261315CFDC4F8EFF8BBBF965BB2F1BC4EFD2DFC59A4D00616AFBA703962FB59EF5A4984FFFD04EC32376FEE595F02849E1955658FFAAD8514A8D77DB13DF71F9CEB106246A639263BE047FE30C2985D7EDFC3A68C9EEAE1C6F9342575FE34C0F77EC95D2D17656DD80E09DA62F2BF012269C575B7298C030B4A044EA0AE05B7274D4B4C92FDB7BBADF492B2F381D6FFECA3D83A42FB8BC796DB279F873A194AE22DF571145ABCCEC6A5D3F2D4A4493CFE7944BEA9FD75A8279C364BC10DF59F4DED" + // SkManPkTcu
                "7750343030202020" + // PPID
                "0191"; // NMIC

        String actual = Util.byteArrayToHexString(packedMessage);
        assertEquals(expected, actual);
    }

    /*
    Field	Field Name	Size	Attrib	Octets	M/C	Comment
    ---	Message Type Id	4	n	2	M	0800
    ---	Primary Bit Map	4	b	8	M
    001	Secondary Bit Map	64	b	8	M
    011	System Trace Audit Number	6	n	3	M	Set to Zeros
    032	Acquiring Institution Id	11	n	8	M
    041	Card Acceptor Terminal Id	8	ans	8	M
    042	Card Acceptor Merchant Id	15	ans	15	M
    048	Additional Data Private:
    Data Length
    Key Management Version
    Cipher Text
    PINpad Id	...999
    03 (Triple DES)
    PINpad Id
    070	Network Management Information Code	3	n	2	M	192 - Security initialization Request
     */
    @Test
    public void shouldPackKtmLogonRequestMessage() {
        IMalHardware iMalHardware = mock(IMalHardware.class);
        when(iMalHardware.getAppVersion()).thenReturn("4444");
        when(mal.getHardware()).thenReturn(iMalHardware);
        TSec sec = mock(TSec.class);
        when(transRec.getSecurity()).thenReturn(sec);
        when(transRec.getProtocol()).thenReturn(protocol);
        when(audit.getTerminalId()).thenReturn("N9393001");
        when(audit.getMerchantId()).thenReturn("057998206712988");

        when(transRec.getAudit()).thenReturn(audit);
        when(paymentSwitch.getAiic()).thenReturn("00000407643");
        when(payCfg.getPaymentSwitch()).thenReturn(paymentSwitch);
        when(dependency.getPayCfg()).thenReturn(payCfg);


        byte[] packedMessage = pack(dependency, transRec, MsgType.NETWORK, "192");

        String expected =
                "0800" + // message type
                "8020000100C10000" + // primary bitmap
                "0400000000000000" + // secondary bitmap
                "000000" + // stan
                "1100000407643F" + // AIIC
                "4E39333933303031" + // TID
                "303537393938323036373132393838" + // MID
                "313239" + // data length
                "03" + // Key managment version (Triple DES)
                "193F7461BE5378F3636FCE90A7E3D330AAC3944E47AF6E421E6B853515F0AB40B531F60366EC6D4E92F4D665E5583E384E60FF396A60ED8ACDB0E6990D45D8186CF584C7308F2DCD57AAD3C3405B2805AF9BFEA2421D41EA728225A41F287EC6B16F8CC2442916DCD67D36ACBA66A0E57BAC998FC64A17A9" + // GenerateSkTcuKiBlock
                "7750343030202020" + // PPID
                "0192"; // NMIC

        String actual = Util.byteArrayToHexString(packedMessage);
        assertEquals(expected, actual);
    }

    /*
    Field	Field Name	Size	Attrib	Octets	M/C	Comment
    ---	Message Type Id	4	n	2	M	0800
    ---	Primary Bit Map	4	b	8	M
    001	Secondary Bit Map	64	b	8	M
    011	System Trace Audit Number	6	n	3	M	Set to Zeros
    032	Acquiring Institution Id	11	n	8	M
    041	Card Acceptor Terminal Id	8	ans	8	M
    042	Card Acceptor Merchant Id	15	ans	15	M
    048	Additional Data Private:
    Data Length
    Key Management Version
    eKIA(PPID)
    PPID	...999
    03 (Triple DES)
    PPID encrypted by KIA
    PPID in clear
    070	Network Management Information Code	3	n	2	M	193 - KEK initialization Request
     */
    @Test
    public void shouldPackKekLogonRequestMessage() {
        IMalHardware iMalHardware = mock(IMalHardware.class);
        when(iMalHardware.getAppVersion()).thenReturn("4444");
        when(mal.getHardware()).thenReturn(iMalHardware);
        TSec sec = mock(TSec.class);
        when(transRec.getSecurity()).thenReturn(sec);
        when(transRec.getProtocol()).thenReturn(protocol);
        when(audit.getTerminalId()).thenReturn("N9393001");
        when(audit.getMerchantId()).thenReturn("057998206712988");

        when(transRec.getAudit()).thenReturn(audit);
        when(paymentSwitch.getAiic()).thenReturn("00000407643");
        when(payCfg.getPaymentSwitch()).thenReturn(paymentSwitch);
        when(dependency.getPayCfg()).thenReturn(payCfg);

        byte[] packedMessage = pack(dependency, transRec, MsgType.NETWORK, "193");

        String expected =
                "0800" + // message type
                "8020000100C10000" + // primary bitmap
                "0400000000000000" + // secondary bitmap
                "000000" + // stan
                "1100000407643F" + // AIIC
                "4E39333933303031" + // TID
                "303537393938323036373132393838" + // MID
                "303137" + // data length
                "03" + // Key managment version (Triple DES)
                "0CA7E58300000000" + // encrypted PPID
                "7750343030202020" + // PPID
                "0193"; // NMIC

        String actual = Util.byteArrayToHexString(packedMessage); // Getting extra 4 byes of zero, not sure why??
        assertEquals(expected, actual);
    }

    /*
    Field	Field Name	Size	Attrib	Octets	M/C	Comment
    ---	Message Type	4	n	2	M	0800
    ---	Primary Bit Map	64	b	8	M
    001	Secondary Bit Map	64	b	8	M
    011	Systems Trace Audit Number	6	n	3	M	Zeroes
    032	Acquirer Institution ID Code	...11	n	8	M
    041 	Card Acceptor Terminal ID	8	ans	8	M
    042	Card Acceptor ID Code	15	ans	15	M
    048	Additional Data – Private
    Data Length
    PINPad Identification
    Key Management Ver
    PINpad Author. Code
    eKEK1(PPASN)
    eKEK2(PPASN)	...999
    03 - Triple DES
    PPASN encrypted by KEK1
    PPASN encrypted by KEK2
    070	NMIC	3	n	2	M	“170” Normal Sign on
    */
    @Test
    public void shouldPackLogonRequestMessage() {
        IMalHardware iMalHardware = mock(IMalHardware.class);
        when(iMalHardware.getAppVersion()).thenReturn("4444");
        when(mal.getHardware()).thenReturn(iMalHardware);
        TSec sec = mock(TSec.class);
        when(transRec.getSecurity()).thenReturn(sec);
        when(transRec.getProtocol()).thenReturn(protocol);
        when(audit.getTerminalId()).thenReturn("N9393001");
        when(audit.getMerchantId()).thenReturn("057998206712988");

        when(transRec.getAudit()).thenReturn(audit);
        when(paymentSwitch.getAiic()).thenReturn("00000407643");
        when(payCfg.getPaymentSwitch()).thenReturn(paymentSwitch);
        when(dependency.getPayCfg()).thenReturn(payCfg);


        byte[] packedMessage = pack(dependency, transRec, MsgType.NETWORK, "170");

        String expected =
                "0800" + //message type
                "8020000100C10000" + // primary bitmap
                "0400000000000000" + // secondary bitmap
                "000000" + // stan
                "1100000407643F" + // AIIC
                "4E39333933303031" + // TID
                "303537393938323036373132393838" + // MID
                "303239" + // data length
                "7750343030202020" + // PPID
                "03" + // Key managment version (Triple DES)
                "0CA7E583" + // PINpad Author code - We use the encrypted PPID here. Spec limits to 4 bytes
                "A1A1A1A1A1A1A1A1" + // eKEK1(PPASN)
                "A2A2A2A2A2A2A2A2" + // eKEK1(PPASN)
                "0170"; // NMIC
        String actual = Util.byteArrayToHexString(packedMessage);
        assertEquals(expected, actual);
    }

    /*
    Field	Field Name	Size	Attrib	Octets	M/C	Comment
    ---	Message Type	4	n	2	M	0800
    ---	Primary Bit Map	64	b	8	M
    001	Secondary Bit Map	64	b	8	M
    011	Systems Trace Audit Number	6	n	3	M	The Current STAN
    032	Acquirer Institution ID Code	...11	n	8	M
    041 	Card Acceptor Terminal ID	8	ans	8	M
    042	Card Acceptor ID Code	15	ans	15	M
    048	Additional Data – Private
    Data Length
    PINPad Identification
    Key Management Ver
    eKEK1(PPASN)
    eKEK2(PPASN)

    03 - Triple DES
    PPASN encrypted by KEK1
    PPASN encrypted by KEK2
    070	NMIC	3	n	2	M	“101” Session Key Change
     */

    @Test
    public void shouldPackSessionKeyLogonRequestMessage() {
        IMalHardware iMalHardware = mock(IMalHardware.class);
        when(iMalHardware.getAppVersion()).thenReturn("4444");
        when(mal.getHardware()).thenReturn(iMalHardware);
        TSec sec = mock(TSec.class);
        when(transRec.getSecurity()).thenReturn(sec);
        when(transRec.getProtocol()).thenReturn(protocol);
        when(audit.getTerminalId()).thenReturn("N9393001");
        when(audit.getMerchantId()).thenReturn("057998206712988");

        when(transRec.getAudit()).thenReturn(audit);
        when(paymentSwitch.getAiic()).thenReturn("00000407643");
        when(payCfg.getPaymentSwitch()).thenReturn(paymentSwitch);
        when(dependency.getPayCfg()).thenReturn(payCfg);


        byte[] packedMessage = pack(dependency, transRec, MsgType.NETWORK, "101");

        String expected =
                "0800" + // message type
                "8020000100C10000" + // primary bitmap
                "0400000000000000" + // secondary bitmap
                "000000" + // stan
                "1100000407643F" + // AIIC
                "4E39333933303031" + // TID
                "303537393938323036373132393838" + // MID
                "303235" + // data length
                "7750343030202020" + // PPID
                "03" + // Key managment version (Triple DES)
                "A1A1A1A1A1A1A1A1" + // eKEK1(PPASN)
                "A2A2A2A2A2A2A2A2" + // eKEK1(PPASN)
                "0101"; // NMIC
        String actual = Util.byteArrayToHexString(packedMessage);
        assertEquals(expected, actual);
    }

    /*
    ########
    *Sample*
    ########

    Message Type............................ 0200
    Bitmap.................................. 3220068128C28280
    3 Processing code....................... 003000
    4 Amount, transaction................... 000000002200
    7 Transmission date and time............ 0804141608
    11 System trace audit number............ 000043
    22 Point of service entry mode.......... 071
    23 Card sequence number................. 001
    25 POS condition code................... 42
    32 Acquiring institution ID code........ (11) 00000407643
    35 Track 2 data......................... (37) 5353181800254161D22122029723063028710
    37 Retrieval reference number........... 000000000013
    41 Terminal id.......................... W5890003
    42 Merchant id.......................... 611000602005890
    47 Additional data - National........... (006) Contains 1 Tags
       TCC Terminal Capability Code......... 07
    49 Currency code, transaction........... 036
    55 ICC data............................. (156) Contains 22 Tags
       5F28: Issuer Country Code............ (02) 0036
       5F2A: Txn Currency Code.............. (02) 0036
       5F34: PAN Sequence Number............ (01) 01
       82: Interchange Profile.............. (02) 1980
       95: Terminal Verify Results.......... (05) 0000008001
       9A: Transaction Date................. (03) 220805
       9C: Transaction Type................. (01) 00
       9F02: Amount Authorised.............. (06) 000000002200
       9F03: Amount, Other.................. (06) 000000000000
       9F10: Issuer App Data................ (26) 0110A04001222800000000000000000000FF00000000000000FF
       9F1A: Terminal Country Code.......... (02) 0036
       9F26: App Cryptogram................. (08) D8F3E2BE1F76CAB8
       9F27: Cryptogram Info Data........... (01) 80
       9F33: Terminal Capabilities.......... (03) E00808
       9F34: CVM Results.................... (03) 1F0302
       9F35: Terminal Type.................. (01) 22
       9F36: Trans Counter (ATC)............ (02) 00EF
       9F37: Unpredictable Number........... (04) BF45CF32
       9F6E: Form Factor Indicator.......... (07) 00360000303000
       9F42: App Currency Code.............. (02) 0036
       84: Dedicated File Name.............. (07) A0000000041010
       9B: Transaction Status Info.......... (02) 0000
    57 Amount, cash - national use.......... 000000000000
    Cryptogram Validation Failed. Received: D8F3E2BE1F76CAB8 Calculated:9536F0578AC595B3
    Mandatory Field 12 Is Missing.
    Unexpected Field 23 In Message.
    Mandatory Field 26 Is Missing.
    Unexpected Field 55 In Message.
    Field 55 Contains Invalid Data.
    Mandatory Field 64 Is Missing.

    ######
    *Spec*
    ######

    Field 	Field Name 	Size 	Attrib 	Octet s 	Comment
    --- 	Message Type Id 	4 	n 	2 	'0200'
    --- 	Primary Bit Map 	64 	b 	8
    001 	Bit Map, Extended 	64 	b 	8 	Conditional
    002 	Primary Account No 	..19 	n 	 	LLVAR. Conditional.
    003 	Processing Code 	6 	n 	3
    004 	Amount, Transaction 	12 	n 	6
    007 	Transmission Date & Time 	10 	n 	5
    011 	System Trace Audit No 	6 	n 	3
    012 	Time, Local Trans 	6 	n 	3
    013 	Date, Local Trans 	4 	n 	2
    014 	Date, Expiry 	4 	n 	2 	Conditional.
    022 	POS Entry Mode 	3 	n 	2
    023 	Card Sequence Number 	3 	n 	2 	Conditional.
    024 	Function Code (Not supported on Connex) 	3 	n 	2 	Conditional.
    025 	POS Condition Code 	2 	n 	1
    026 	POS PIN Capture Code 	2 	n 	1
    032 	Acquiring Inst ID Code 	..11 	n 	 	LLVAR
    035 	Track 2 Data 	..37 	z 	 	LLVAR. Conditional.
    037 	Retrieval Reference Number 	12 	an 	12
    038 	Authorisation ID Response 	6 	an 	6 	Conditional (NA, not present on AUTH Req)
    041 	Card Acceptor Terminal ID 	8 	ans 	8
    042 	Card Acceptor ID 	15 	ans 	15
    047 	Additional Data National 	…999 	ans 	 	LLLVAR
    049 	Currency Code, Transaction 	3 	n 	2
    052 	PIN Block 	64 	b 	8 	Key = eTMK1(TPK)
    For DUKPT Key = DPK
    055 	EMV ICC related data 	…999 	b 	 	0LLLVAR. Conditional.
    057 	Amount, Cash 	12 	n 	6
    062 	Private Use 	…999 	ans 	 	LLLVAR
    064 	MAC 	64 	b 	8 	Conditional
    Only where DUKPT is not in use. Key = eTMK1(TAKS)
    121 	KSN 	…999 	ans 	 	LLLVAR. Conditional -
    Field 	Field Name 	Size 	Attrib 	Octet s 	Comment
                        Required for DUKPT only
    128 	MAC 	64 	b 	8 	Conditional
    Only required where
    DUKPT is used
    Key = DAKS
    */

    @Test
    public void shouldPackPreAuthRequestMessage() {
        TAmounts amounts = new TAmounts();
        amounts.setAmount(1000L);
        amounts.setTip(100L);
        amounts.setSurcharge(10L);
        amounts.setCashbackAmount(2000L);
        when(cardInfo.getCaptureMethod()).thenReturn(CTLS);
        when(transRec.getTransType()).thenReturn(SALE);
        when(protocol.getRRN()).thenReturn("000000000013");

        when(transRec.getAmounts()).thenReturn(amounts);
        when(cardInfo.isManual()).thenReturn(false);
        when(transRec.getCard()).thenReturn(cardInfo);
        when(protocol.getStan()).thenReturn(1234);
        when(transRec.getProtocol()).thenReturn(protocol);
        when(audit.getTerminalId()).thenReturn("W5890003");
        when(audit.getMerchantId()).thenReturn("611000602005890");
        when(transRec.getAudit()).thenReturn(audit);
        when(paymentSwitch.getAiic()).thenReturn("407643");
        when(paymentSwitch.isDisableSecurity()).thenReturn(true);
        when(payCfg.getPaymentSwitch()).thenReturn(paymentSwitch);
        when(dependency.getPayCfg()).thenReturn(payCfg);


        byte[] packedMessage = pack(dependency, transRec, MsgType.PREAUTH, "");

        String fields_till_dateTime = "0100" + //msg type
                "3220068128C29000" +  // bitmaps
                "000000" + // proc code
                "000000003110"; // total amount
        String dateTime = "0809040103"; // date & time
        String fields_after_dateTime = "001234" + // stan
                "0071" + // POSE
                "0000" + // CSN
                "42" + // POSC
                "06407643" + // AIIC LLVAR
                "37BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBF" + // track 2 LLVAR
                "303030303030303030303133" + // RRN
                "573538393030303" + // TID
                "3363131303030363032303035383930" + // MID
                "30313254434330375C54414D33305C" + // additional data LLLVAR(TCC07\TAM30\)
                "0036" + // currency code
                "2A2A2A2A2A2A2A2A"; // PIN block

        String actual = Util.byteArrayToHexString(packedMessage);
        assertEquals(fields_till_dateTime, actual.substring(0, fields_till_dateTime.length()));
        assertEquals(fields_after_dateTime, actual.substring(fields_till_dateTime.length() + dateTime.length()));
    }

    /*
    ########
    *Sample*
    ########

    Message Type............................ 0200
    Bitmap.................................. 3220068128C28280
    3 Processing code....................... 003000
    4 Amount, transaction................... 000000002200
    7 Transmission date and time............ 0804141608
    11 System trace audit number............ 000043
    22 Point of service entry mode.......... 071
    23 Card sequence number................. 001
    25 POS condition code................... 42
    32 Acquiring institution ID code........ (11) 00000407643
    35 Track 2 data......................... (37) 5353181800254161D22122029723063028710
    37 Retrieval reference number........... 000000000013
    41 Terminal id.......................... W5890003
    42 Merchant id.......................... 611000602005890
    47 Additional data - National........... (006) Contains 1 Tags
       TCC Terminal Capability Code......... 07
    49 Currency code, transaction........... 036
    55 ICC data............................. (156) Contains 22 Tags
       5F28: Issuer Country Code............ (02) 0036
       5F2A: Txn Currency Code.............. (02) 0036
       5F34: PAN Sequence Number............ (01) 01
       82: Interchange Profile.............. (02) 1980
       95: Terminal Verify Results.......... (05) 0000008001
       9A: Transaction Date................. (03) 220805
       9C: Transaction Type................. (01) 00
       9F02: Amount Authorised.............. (06) 000000002200
       9F03: Amount, Other.................. (06) 000000000000
       9F10: Issuer App Data................ (26) 0110A04001222800000000000000000000FF00000000000000FF
       9F1A: Terminal Country Code.......... (02) 0036
       9F26: App Cryptogram................. (08) D8F3E2BE1F76CAB8
       9F27: Cryptogram Info Data........... (01) 80
       9F33: Terminal Capabilities.......... (03) E00808
       9F34: CVM Results.................... (03) 1F0302
       9F35: Terminal Type.................. (01) 22
       9F36: Trans Counter (ATC)............ (02) 00EF
       9F37: Unpredictable Number........... (04) BF45CF32
       9F6E: Form Factor Indicator.......... (07) 00360000303000
       9F42: App Currency Code.............. (02) 0036
       84: Dedicated File Name.............. (07) A0000000041010
       9B: Transaction Status Info.......... (02) 0000
    57 Amount, cash - national use.......... 000000000000
    Cryptogram Validation Failed. Received: D8F3E2BE1F76CAB8 Calculated:9536F0578AC595B3
    Mandatory Field 12 Is Missing.
    Unexpected Field 23 In Message.
    Mandatory Field 26 Is Missing.
    Unexpected Field 55 In Message.
    Field 55 Contains Invalid Data.
    Mandatory Field 64 Is Missing.

    ######
    *Spec*
    ######

    Field 	Field Name 	Size 	Attrib 	Octet s 	Comment
    --- 	Message Type Id 	4 	n 	2 	'0200'
    --- 	Primary Bit Map 	64 	b 	8
    001 	Bit Map, Extended 	64 	b 	8 	Conditional
    002 	Primary Account No 	..19 	n 	 	LLVAR. Conditional.
    003 	Processing Code 	6 	n 	3
    004 	Amount, Transaction 	12 	n 	6
    007 	Transmission Date & Time 	10 	n 	5
    011 	System Trace Audit No 	6 	n 	3
    012 	Time, Local Trans 	6 	n 	3
    013 	Date, Local Trans 	4 	n 	2
    014 	Date, Expiry 	4 	n 	2 	Conditional.
    022 	POS Entry Mode 	3 	n 	2
    023 	Card Sequence Number 	3 	n 	2 	Conditional.
    024 	Function Code (Not supported on Connex) 	3 	n 	2 	Conditional.
    025 	POS Condition Code 	2 	n 	1
    026 	POS PIN Capture Code 	2 	n 	1
    032 	Acquiring Inst ID Code 	..11 	n 	 	LLVAR
    035 	Track 2 Data 	..37 	z 	 	LLVAR. Conditional.
    037 	Retrieval Reference Number 	12 	an 	12
    038 	Authorisation ID Response 	6 	an 	6 	Conditional (NA, not present on AUTH Req)
    041 	Card Acceptor Terminal ID 	8 	ans 	8
    042 	Card Acceptor ID 	15 	ans 	15
    047 	Additional Data National 	…999 	ans 	 	LLLVAR
    049 	Currency Code, Transaction 	3 	n 	2
    052 	PIN Block 	64 	b 	8 	Key = eTMK1(TPK)
    For DUKPT Key = DPK
    055 	EMV ICC related data 	…999 	b 	 	0LLLVAR. Conditional.
    057 	Amount, Cash 	12 	n 	6
    062 	Private Use 	…999 	ans 	 	LLLVAR
    064 	MAC 	64 	b 	8 	Conditional
    Only where DUKPT is not in use. Key = eTMK1(TAKS)
    121 	KSN 	…999 	ans 	 	LLLVAR. Conditional -
    Field 	Field Name 	Size 	Attrib 	Octet s 	Comment
                        Required for DUKPT only
    128 	MAC 	64 	b 	8 	Conditional
    Only required where
    DUKPT is used
    Key = DAKS
    */

    @Test
    public void shouldPackFinancialRequestMessage() {
        TAmounts amounts = new TAmounts();
        amounts.setAmount(1000L);
        amounts.setTip(100L);
        amounts.setSurcharge(10L);
        amounts.setCashbackAmount(2000L);
        when(cardInfo.getCaptureMethod()).thenReturn(CTLS);
        when(transRec.getTransType()).thenReturn(SALE);
        when(protocol.getRRN()).thenReturn("000000000013");

        when(transRec.getAmounts()).thenReturn(amounts);
        when(cardInfo.isManual()).thenReturn(false);
        when(transRec.getCard()).thenReturn(cardInfo);
        when(protocol.getStan()).thenReturn(1234);
        when(transRec.getProtocol()).thenReturn(protocol);
        when(audit.getTransDateTimeAsString("MMddHHmmss")).thenReturn("0809040103");
        when(audit.getTerminalId()).thenReturn("W5890003");
        when(audit.getMerchantId()).thenReturn("611000602005890");
        when(transRec.getAudit()).thenReturn(audit);
        when(paymentSwitch.getAiic()).thenReturn("407643");
        when(paymentSwitch.isDisableSecurity()).thenReturn(false);
        when(payCfg.getPaymentSwitch()).thenReturn(paymentSwitch);
        when(dependency.getPayCfg()).thenReturn(payCfg);


        byte[] packedMessage = pack(dependency, transRec, MsgType.AUTH, "");

        String fields_till_dateTime = "0200" + //msg type
                "3220068128C29080" +  // bitmaps
                "000000" + // proc code
                "000000003110"; // total amount
        String dateTime = "0809040036";// date & time
        String fields_after_dateTime = "001234" + // stan
                "0071" + // POSE
                "0000" + // CSN
                "42" + // POSC
                "06407643" + // AIIC LLVAR
                "37BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBF" + // track 2 LLVAR
                "303030303030303030303133" + // RRN
                "573538393030303" + // TID
                "3363131303030363032303035383930" + // MID
                "30313254434330375C54414D33305C" + // additional data LLLVAR(TCC07\TAM30\)
                "0036" + // currency code
                "2A2A2A2A2A2A2A2A" + // PIN block
                "000000002000"; // amount cash

        String actual = Util.byteArrayToHexString(packedMessage);
        assertEquals(fields_till_dateTime, actual.substring(0, fields_till_dateTime.length()));
        assertEquals(fields_after_dateTime, actual.substring(fields_till_dateTime.length() + dateTime.length()));
    }

    /*
    ########
    *Sample*
    ########

    Message Type............................ 0200
    Bitmap.................................. 3220068128C28280
    3 Processing code....................... 003000
    4 Amount, transaction................... 000000002200
    7 Transmission date and time............ 0804141608
    11 System trace audit number............ 000043
    22 Point of service entry mode.......... 071
    23 Card sequence number................. 001
    25 POS condition code................... 42
    32 Acquiring institution ID code........ (11) 00000407643
    35 Track 2 data......................... (37) 5353181800254161D22122029723063028710
    37 Retrieval reference number........... 000000000013
    41 Terminal id.......................... W5890003
    42 Merchant id.......................... 611000602005890
    47 Additional data - National........... (006) Contains 1 Tags
       TCC Terminal Capability Code......... 07
    49 Currency code, transaction........... 036
    55 ICC data............................. (156) Contains 22 Tags
       5F28: Issuer Country Code............ (02) 0036
       5F2A: Txn Currency Code.............. (02) 0036
       5F34: PAN Sequence Number............ (01) 01
       82: Interchange Profile.............. (02) 1980
       95: Terminal Verify Results.......... (05) 0000008001
       9A: Transaction Date................. (03) 220805
       9C: Transaction Type................. (01) 00
       9F02: Amount Authorised.............. (06) 000000002200
       9F03: Amount, Other.................. (06) 000000000000
       9F10: Issuer App Data................ (26) 0110A04001222800000000000000000000FF00000000000000FF
       9F1A: Terminal Country Code.......... (02) 0036
       9F26: App Cryptogram................. (08) D8F3E2BE1F76CAB8
       9F27: Cryptogram Info Data........... (01) 80
       9F33: Terminal Capabilities.......... (03) E00808
       9F34: CVM Results.................... (03) 1F0302
       9F35: Terminal Type.................. (01) 22
       9F36: Trans Counter (ATC)............ (02) 00EF
       9F37: Unpredictable Number........... (04) BF45CF32
       9F6E: Form Factor Indicator.......... (07) 00360000303000
       9F42: App Currency Code.............. (02) 0036
       84: Dedicated File Name.............. (07) A0000000041010
       9B: Transaction Status Info.......... (02) 0000
    57 Amount, cash - national use.......... 000000000000
    Cryptogram Validation Failed. Received: D8F3E2BE1F76CAB8 Calculated:9536F0578AC595B3
    Mandatory Field 12 Is Missing.
    Unexpected Field 23 In Message.
    Mandatory Field 26 Is Missing.
    Unexpected Field 55 In Message.
    Field 55 Contains Invalid Data.
    Mandatory Field 64 Is Missing.

    ######
    *Spec*
    ######

    Field 	Field Name 	Size 	Attrib 	Octet s 	Comment
    --- 	Message Type Id 	4 	n 	2 	'0200'
    --- 	Primary Bit Map 	64 	b 	8
    001 	Bit Map, Extended 	64 	b 	8 	Conditional
    002 	Primary Account No 	..19 	n 	 	LLVAR. Conditional.
    003 	Processing Code 	6 	n 	3
    004 	Amount, Transaction 	12 	n 	6
    007 	Transmission Date & Time 	10 	n 	5
    011 	System Trace Audit No 	6 	n 	3
    012 	Time, Local Trans 	6 	n 	3
    013 	Date, Local Trans 	4 	n 	2
    014 	Date, Expiry 	4 	n 	2 	Conditional.
    022 	POS Entry Mode 	3 	n 	2
    023 	Card Sequence Number 	3 	n 	2 	Conditional.
    024 	Function Code (Not supported on Connex) 	3 	n 	2 	Conditional.
    025 	POS Condition Code 	2 	n 	1
    026 	POS PIN Capture Code 	2 	n 	1
    032 	Acquiring Inst ID Code 	..11 	n 	 	LLVAR
    035 	Track 2 Data 	..37 	z 	 	LLVAR. Conditional.
    037 	Retrieval Reference Number 	12 	an 	12
    038 	Authorisation ID Response 	6 	an 	6 	Conditional (NA, not present on AUTH Req)
    041 	Card Acceptor Terminal ID 	8 	ans 	8
    042 	Card Acceptor ID 	15 	ans 	15
    047 	Additional Data National 	…999 	ans 	 	LLLVAR
    049 	Currency Code, Transaction 	3 	n 	2
    052 	PIN Block 	64 	b 	8 	Key = eTMK1(TPK)
    For DUKPT Key = DPK
    055 	EMV ICC related data 	…999 	b 	 	0LLLVAR. Conditional.
    057 	Amount, Cash 	12 	n 	6
    062 	Private Use 	…999 	ans 	 	LLLVAR
    064 	MAC 	64 	b 	8 	Conditional
    Only where DUKPT is not in use. Key = eTMK1(TAKS)
    121 	KSN 	…999 	ans 	 	LLLVAR. Conditional -
    Field 	Field Name 	Size 	Attrib 	Octet s 	Comment
                        Required for DUKPT only
    128 	MAC 	64 	b 	8 	Conditional
    Only required where
    DUKPT is used
    Key = DAKS
    */

    @Test
    public void shouldPackReversalRequestMessage() {
        String dateTime = "2012115959";
        mockReversalTransaction(dateTime);

        byte[] packedMessage = pack(dependency, transRec, MsgType.REVERSAL, "");

        String fields_till_dateTime = "0420" + //msg type
                "B220040128C08080" + // bitmaps
                "0000004000000000" + // extended bitmaps
                "000000" + // proc code
                "000000003110";
        String fields_after_dateTime = "001233" + // org stan
                "0071" + // POSE
                "06407643" + // AIIC LLVAR
                "37BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBF" + // track 2 LLVAR
                "303030303030303030303133" + // RRN
                "5735383930303033" + // TID
                "363131303030363032303035383930" + // MID
                "0036" + // currency code
                "000000002000" + // amount cash
                // 	Message Type Id 	4 	n 	2 	Original Msg. Type Id
                // 	System Trace Audit No 	6 	n 	3 	STAN from original msg.
                // 	Date, Local Trans 	4 	n 	2 	Zero
                // 	Time, Local Trans 	6 	n 	3 	Zero.
                // 	Acquiring Inst ID Code 	..11 	n 	 	Zero
                // 	Forwarding Inst Id 	..11 	n 	 	Zero
                "000000123320121159590000000000000000000000";

        String actual = Util.byteArrayToHexString(packedMessage);
        assertEquals(fields_till_dateTime, actual.substring(0, fields_till_dateTime.length()));
        assertEquals(fields_after_dateTime, actual.substring(fields_till_dateTime.length() + dateTime.length()));
    }

    @Test
    public void shouldPackReversalRequestMessageWhenSignatureRejected() {
        String dateTime = "2012115959";
        mockReversalTransaction(dateTime);
        when(audit.getRejectReasonType()).thenReturn(SIGNATURE_REJECTED);
        byte[] packedMessage = pack(dependency, transRec, MsgType.REVERSAL, "");

        String fields_till_dateTime = "0420" + //msg type
                "B220040128C08084" + // bitmaps
                "0000004000000000" + // extended bitmaps
                "000000" + // proc code
                "000000003110";
        String fields_after_dateTime = "001233" + // org stan
                "0071" + // POSE
                "06407643" + // AIIC LLVAR
                "37BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBF" + // track 2 LLVAR
                "303030303030303030303133" + // RRN
                "5735383930303033" + // TID
                "363131303030363032303035383930" + // MID
                "0036" + // currency code
                "000000002000" + // amount cash
                "303036433630313033" + // DE62 with forced post reason indicator
                // 	Message Type Id 	4 	n 	2 	Original Msg. Type Id
                // 	System Trace Audit No 	6 	n 	3 	STAN from original msg.
                // 	Date, Local Trans 	4 	n 	2 	Zero
                // 	Time, Local Trans 	6 	n 	3 	Zero.
                // 	Acquiring Inst ID Code 	..11 	n 	 	Zero
                // 	Forwarding Inst Id 	..11 	n 	 	Zero
                "000000123320121159590000000000000000000000";

        String actual = Util.byteArrayToHexString(packedMessage);
        assertEquals(fields_till_dateTime, actual.substring(0, fields_till_dateTime.length()));
        assertEquals(fields_after_dateTime, actual.substring(fields_till_dateTime.length() + dateTime.length()));
    }

    @Test
    public void shouldPackReversalRequestMessageWhenMacFailed() {
        String dateTime = "2012115959";
        mockReversalTransaction(dateTime);
        when(audit.getRejectReasonType()).thenReturn(MAC_FAILED);
        byte[] packedMessage = pack(dependency, transRec, MsgType.REVERSAL, "");

        String fields_till_dateTime = "0420" + //msg type
                "B220040128C08084" + // bitmaps
                "0000004000000000" + // extended bitmaps
                "000000" + // proc code
                "000000003110";
        String fields_after_dateTime = "001233" + // org stan
                "0071" + // POSE
                "06407643" + // AIIC LLVAR
                "37BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBF" + // track 2 LLVAR
                "303030303030303030303133" + // RRN
                "5735383930303033" + // TID
                "363131303030363032303035383930" + // MID
                "0036" + // currency code
                "000000002000" + // amount cash
                "303036433630313041" + // DE62 with forced post reason indicator
                // 	Message Type Id 	4 	n 	2 	Original Msg. Type Id
                // 	System Trace Audit No 	6 	n 	3 	STAN from original msg.
                // 	Date, Local Trans 	4 	n 	2 	Zero
                // 	Time, Local Trans 	6 	n 	3 	Zero.
                // 	Acquiring Inst ID Code 	..11 	n 	 	Zero
                // 	Forwarding Inst Id 	..11 	n 	 	Zero
                "000000123320121159590000000000000000000000";

        String actual = Util.byteArrayToHexString(packedMessage);
        assertEquals(fields_till_dateTime, actual.substring(0, fields_till_dateTime.length()));
        assertEquals(fields_after_dateTime, actual.substring(fields_till_dateTime.length() + dateTime.length()));
    }

    /*
  ########
  *Sample*
  ########

  Message Type............................ 0220
  Bitmap.................................. 323806812CC28280
  3 Processing code....................... 003000
  4 Amount, transaction................... 000000002800
  7 Transmission date and time............ 0803094258
  11 System trace audit number............ 000039
  12 Time, local transaction.............. 094258
  13 Date, local transaction.............. 0803
  22 Point of service entry mode.......... 071
  23 Card sequence number................. 001
  25 POS condition code................... 42
  32 Acquiring institution ID code........ (11) 00000407643
  35 Track 2 data......................... (37) 5353181800254161D22122029723063028710
  37 Retrieval reference number........... 000000000009
  38 Auth identification response......... 000187
  41 Terminal id.......................... W5890003
  42 Merchant id.......................... 611000602005890
  47 Additional data - National........... (006) Contains 1 Tags
     TCC Terminal Capability Code......... 07
  49 Currency code, transaction........... 036
  55 ICC data............................. (165) Contains 23 Tags
     5F28: Issuer Country Code............ (02) 0036
     5F2A: Txn Currency Code.............. (02) 0036
     5F34: PAN Sequence Number............ (01) 01
     4F: Application ID (AID)............. (07) A0000000041010
     82: Interchange Profile.............. (02) 1980
     95: Terminal Verify Results.......... (05) 0000008001
     9A: Transaction Date................. (03) 220803
     9C: Transaction Type................. (01) 00
     9F02: Amount Authorised.............. (06) 000000002800
     9F03: Amount, Other.................. (06) 000000000000
     9F10: Issuer App Data................ (26) 0110A04001222800000000000000000000FF00000000000000FF
     9F1A: Terminal Country Code.......... (02) 0036
     9F26: App Cryptogram................. (08) 14652BDCA4AE7B97
     9F27: Cryptogram Info Data........... (01) 80
     9F33: Terminal Capabilities.......... (03) E00808
     9F34: CVM Results.................... (03) 1F0302
     9F35: Terminal Type.................. (01) 22
     9F36: Trans Counter (ATC)............ (02) 00EB
     9F37: Unpredictable Number........... (04) 1A81C218
     9F6E: Form Factor Indicator.......... (07) 00360000303000
     9F42: App Currency Code.............. (02) 0036
     84: Dedicated File Name.............. (07) A0000000041010
     9B: Transaction Status Info.......... (02) 0000
  57 Amount, cash - national use.......... 000000000000
  Cryptogram Validation Failed. Received: 14652BDCA4AE7B97 Calculated:66D30EA3785BB6BE
  Unexpected Field 7 In Message.
  Unexpected Field 23 In Message.
  Mandatory Field 24 Is Missing.
  Unexpected Field 35 In Message.
  Unexpected Field 37 In Message.
  Unexpected Field 49 In Message.
  Unexpected Field 55 In Message.
  Field 55 Contains Invalid Data.
  Unexpected Field 57 In Message.
  Mandatory Field 62 Is Missing.
  Mandatory Field 64 Is Missing.


  ######
  *Spec*
  ######

  Field 	Field Name 	Size 	Attrib 	Octet s 	Comment
  --- 	Message Type Id 	4 	n 	2 	0220/0221
  --- 	Primary Bit Map 	64 	b 	8
  003 	Processing Code 	6 	n 	3
  004 	Amount, Transaction 	12 	n 	6
  007 	Transmission Date & Time 	10 	n 	5
  011 	System Trace Audit No 	6 	n 	3
  012 	Time, Local Trans 	6 	n 	3
  013 	Date, Local Trans 	4 	n 	2
  022 	POS Entry Mode 	3 	n 	2
  023 	Card Sequence Number 	6 	n 	3
  025 	POS Condition Code 	2 	n 	1
  026 	POS PIN Capture Code 	2 	n 	1
  032 	Acquiring Inst ID Code 	..11 	n
  035 	Track 2 Data 	..37 	z
  037 	Retrieval Reference Number 	12 	an 	12
  038 	Authorisation ID Response 	6 	an 	6 	If obtained, else Spaces.
  041 	Card Acceptor Terminal ID 	8 	ans 	8
  042 	Card Acceptor ID 	15 	ans 	15
  047 	Additional Data National 	...999 	ans
  049 	Currency Code, Transaction 	3 	n 	3
  055 	EMV ICC related data 	…999 	b 	 	0LLLVAR
  057 	Amount, Cash 	12 	n 	6
  062 	Private Use 	…999 	ans 	 	LLLVAR
  064 	MAC 	64 	b 	8 	Conditional - Only where DUKPT is not in use. Key = eTMK1(TAKS)
  121 	KSN 	…999 	ans 	 	LLLVAR. Conditional -
  Required for DUKPT only.
  128 	MAC 	64 	b 	8 	Conditional - Only required where DUKPT is used Key = DAKS
  */
    @Test
    public void shouldPackAdviseRequestMessage() {
        mockAdviceTransaction();

        byte[] packedMessage = pack(dependency, transRec, MsgType.ADVICE, "");

        String expected = "0220" + //msg type
                "322006812CC28080" +  // bitmaps
                "000000" + // proc code
                "000000003110" + // total amount
                "0809040103" + // date & time
                "001234" + // stan
                "0071" + // POSE
                "0000" + // CSN
                "42" + // POSC
                "06407643" + // AIIC LLVAR
                "37BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBF" + // track 2 LLVAR
                "303030303030303030303133" + // RRN
                "303030303030" +  // AuthCode
                "573538393030303" + // TID
                "3363131303030363032303035383930" + // MID
                "30313254434330375C54414D33305C" + // additional data LLLVAR(TCC07\TAM30\)
                "0036" + // currency code
                "000000002000"; // amount cash
                assertEquals(expected, Util.byteArrayToHexString(packedMessage));
    }

    @Test
    public void shouldPackAdviseRequestMessageForEfbTransaction() {
        mockAdviceTransaction();
        when(protocol.getAuthMethod()).thenReturn(EFB_AUTHORISED);
        when(transRec.isEfbAuthorisedTransaction()).thenReturn(true);

        byte[] packedMessage = pack(dependency, transRec, MsgType.ADVICE, "");

        String expected = "0220" + //msg type
                "322006812CC28084" +  // bitmaps
                "000000" + // proc code
                "000000003110" + // total amount
                "0809040103" + // date & time
                "001234" + // stan
                "0071" + // POSE
                "0000" + // CSN
                "42" + // POSC
                "06407643" + // AIIC LLVAR
                "37BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBF" + // track 2 LLVAR
                "303030303030303030303133" + // RRN
                "303030303030" +  // AuthCode
                "573538393030303" + // TID
                "3363131303030363032303035383930" + // MID
                "30313754434330375C54414D33305C46424B455C" + // additional data LLLVAR(TCC07\TAM30\FBKE\)
                "0036" + // currency code
                "000000002000" + // amount cash
                "303036433630313031"; // DE62 with advice indicator
        assertEquals(expected, Util.byteArrayToHexString(packedMessage));
    }

    @Test
    public void shouldPackAdviseRequestMessageForEfbTransactionWhenContinueInFallbackTimer() {
        mockAdviceTransaction();
        when(protocol.getAuthMethod()).thenReturn(OFFLINE_EFB_AUTHORISED);
        when(transRec.isEfbAuthorisedTransaction()).thenReturn(true);

        byte[] packedMessage = pack(dependency, transRec, MsgType.ADVICE, "");

        String expected = "0220" + //msg type
                "322006812CC28084" +  // bitmaps
                "000000" + // proc code
                "000000003110" + // total amount
                "0809040103" + // date & time
                "001234" + // stan
                "0071" + // POSE
                "0000" + // CSN
                "42" + // POSC
                "06407643" + // AIIC LLVAR
                "37BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBF" + // track 2 LLVAR
                "303030303030303030303133" + // RRN
                "303030303030" +  // AuthCode
                "573538393030303" + // TID
                "3363131303030363032303035383930" + // MID
                "30313754434330375C54414D33305C46424B455C" + // additional data LLLVAR(TCC07\TAM30\FBKE\)
                "0036" + // currency code
                "000000002000" + // amount cash
                "303036433630313034"; // DE62 with advice indicator
        assertEquals(expected, Util.byteArrayToHexString(packedMessage));
    }

    @Test
    public void shouldPackAdviseRequestMessageForEfbTransactionWhenIssuerLinkDown() {
        mockAdviceTransaction();
        when(protocol.getAuthMethod()).thenReturn(LINK_DOWN_EFB_AUTHORISED);
        when(transRec.isEfbAuthorisedTransaction()).thenReturn(true);

        byte[] packedMessage = pack(dependency, transRec, MsgType.ADVICE, "");

        String expected = "0220" + //msg type
                "322006812CC28084" +  // bitmaps
                "000000" + // proc code
                "000000003110" + // total amount
                "0809040103" + // date & time
                "001234" + // stan
                "0071" + // POSE
                "0000" + // CSN
                "42" + // POSC
                "06407643" + // AIIC LLVAR
                "37BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBF" + // track 2 LLVAR
                "303030303030303030303133" + // RRN
                "303030303030" +  // AuthCode
                "573538393030303" + // TID
                "3363131303030363032303035383930" + // MID
                "30313754434330375C54414D33305C46424B455C" + // additional data LLLVAR(TCC07\TAM30\FBKE\)
                "0036" + // currency code
                "000000002000" + // amount cash
                "303036433630313032"; // DE62 with advice indicator
        assertEquals(expected, Util.byteArrayToHexString(packedMessage));
    }

    @Test
    public void shouldPackAdviseRequestMessageWhenEmvOffline() {
        mockAdviceTransaction();
        when(cardInfo.isCtlsCaptured()).thenReturn(true);

        byte[] packedMessage = pack(dependency, transRec, MsgType.ADVICE, "");

        String expected = "0220" + //msg type
                "322006812CC28084" +  // bitmaps
                "000000" + // proc code
                "000000003110" + // total amount
                "0809040103" + // date & time
                "001234" + // stan
                "0071" + // POSE
                "0000" + // CSN
                "42" + // POSC
                "06407643" + // AIIC LLVAR
                "37BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBF" + // track 2 LLVAR
                "303030303030303030303133" + // RRN
                "303030303030" +  // AuthCode
                "573538393030303" + // TID
                "3363131303030363032303035383930" + // MID
                "30313254434330375C54414D33305C" + // additional data LLLVAR(TCC07\TAM30\)
                "0036" + // currency code
                "000000002000" + // amount cash
                "303036433630313037"; // DE62 with advice indicator
        assertEquals(expected, Util.byteArrayToHexString(packedMessage));
    }

    @Test
    public void shouldPackAdviseRequestMessageWhenPreAuthOffline() {
        mockAdviceTransaction();
        when(transRec.getTransType()).thenReturn(PREAUTH);
        when(transRec.isPreAuth()).thenReturn(true);
        when(cardInfo.isCtlsCaptured()).thenReturn(true);

        byte[] packedMessage = pack(dependency, transRec, MsgType.ADVICE, "");

        String expected = "0220" + //msg type
                "322006812CC28084" +  // bitmaps
                "000000" + // proc code
                "000000003110" + // total amount
                "0809040103" + // date & time
                "001234" + // stan
                "0071" + // POSE
                "0000" + // CSN
                "42" + // POSC
                "06407643" + // AIIC LLVAR
                "37BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBF" + // track 2 LLVAR
                "303030303030303030303133" + // RRN
                "303030303030" +  // AuthCode
                "573538393030303" + // TID
                "3363131303030363032303035383930" + // MID
                "30313254434330375C54414D33305C" + // additional data LLLVAR(TCC07\TAM30\)
                "0036" + // currency code
                "000000002000" + // amount cash
                "303036433630313038"; // DE62 with advice indicator
        assertEquals(expected, Util.byteArrayToHexString(packedMessage));
    }

    @Test
    public void shouldPackAdviseRequestMessageForCompletion() {
        mockAdviceTransaction();
        when(transRec.getTransType()).thenReturn(COMPLETION);

        byte[] packedMessage = pack(dependency, transRec, MsgType.ADVICE, "");

        String expected = "0220" + //msg type
                "322006812CC28084" +  // bitmaps
                "000000" + // proc code
                "000000003110" + // total amount
                "0809040103" + // date & time
                "001234" + // stan
                "0071" + // POSE
                "0000" + // CSN
                "42" + // POSC
                "06407643" + // AIIC LLVAR
                "37BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBF" + // track 2 LLVAR
                "303030303030303030303133" + // RRN
                "303030303030" +  // AuthCode
                "573538393030303" + // TID
                "3363131303030363032303035383930" + // MID
                "30313254434330375C54414D33305C" + // additional data LLLVAR(TCC07\TAM30\)
                "0036" + // currency code
                "000000002000" + // amount cash
                "303036433630313035"; // DE62 with advice indicator
        assertEquals(expected, Util.byteArrayToHexString(packedMessage));
    }

    /*
    ########
    *Sample*
    ########
    Message Type............................ 0500
    Bitmap.................................. A020000100C040000078078080000000
    3 Processing code....................... 950000
    11 System trace audit number............ 000044
    32 Acquiring institution ID code........ (11) 00000407643
    41 Terminal id.......................... W5890003
    42 Merchant id.......................... 611000602005890
    50 Currency code, settlement............ 036
    74 Credits, number...................... 0000000000
    75 Credits, reversal number............. 0000000000
    76 Debits, number....................... 0000000004
    77 Debits, reversal number.............. 0000000000
    86 Credits, amount...................... 0000000000000000
    87 Credits, reversal amount............. 0000000000000000
    88 Debits, amount....................... 0000000000002622
    89 Debits, reversal amount.............. 0000000000000000
    97 Amount, net settlement............... X'0C30303030303030303030303032363232'

    Message Validation Errors:
    Mandatory Field 7 Is Missing.
    Mandatory Field 12 Is Missing.
    Mandatory Field 13 Is Missing.
    Mandatory Field 61 Is Missing.
    Mandatory Field 62 Is Missing.
    Field 97 Contains Invalid Data.
    Mandatory Field 128 Is Missing.

    ######
    *Spec*
    ######
    Field 	Field Name 	Size 	Attrib 	Octet s 	Comment
    --- 	Message Type ID 	4 	n 	2 	'0500'
    --- 	Primary Bit Map 	64 	b 	8
    001 	Bit Map, Extended 	64 	b 	8
    003 	Processing Code 	6 	n 	3
    007 	Transmission Date & Time 	10 	n 	5
    011 	System Trace Audit No 	6 	n 	3
    012 	Time, Local Trans 	6 	n 	3
    013 	Date, Local Trans 	4 	n 	2
    032 	Acquiring Inst ID Code 	..11 	n 	 	LLVAR
    041 	Card Acceptor Terminal ID 	8 	ans 	8
    042 	Card Acceptor ID 	15 	ans 	15
    050 	Currency Code, Settlement 	3 	n 	2
    061 	Private Use 	…999 	ans 	 	LLLVAR
    062 	Private Use 	…999 	ans 	 	LLLVAR
    074 	Credits, Number 	10 	n 	5 	Zeros
    075 	Credits, Reversal Number 	10 	n 	5 	Zeros
    076 	Debits, Number 	10 	n 	5 	Zeros
    077 	Debits, Reversal Number 	10 	n 	5 	Zeros
    086 	Credits, Amount 	16 	n 	8 	Zeros
    087 	Credits, Reversal Amount 	16 	n 	8 	Zeros
    088 	Debits, Amount 	16 	n 	8 	Zeros
    089 	Debits, Reversal Amount 	16 	n 	8 	Zeros
    097 	Amount, Net Settlement 	x + 	x+n 	9 	Zeros
            n16
    121 	KSN 	…999 	ans 	 	LLLVAR. Conditional
                        Required for DUKPT only
    128 	MAC 	64 	b 	8 	Conditional
                        Non-DUKPT
                        Key = eTMK1(TAKS)
                        DUKPT
                        Key = DAKS
    */
    @Test
    public void shouldPackReconcileMessage() {
        when(protocol.getStan()).thenReturn(1234);
        when(transRec.isReconciliation()).thenReturn(true);
        when(transRec.getProtocol()).thenReturn(protocol);
        when(audit.getTerminalId()).thenReturn("W5890003");
        when(audit.getMerchantId()).thenReturn("611000602005890");
        when(transRec.getAudit()).thenReturn(audit);
        when(reconciliationDao.findByTransId(0)).thenReturn(reconciliation);
        when(reconciliationFigures.getCreditsNumber()).thenReturn(10L);
        when(reconciliationFigures.getCreditsReversalNumber()).thenReturn(1L);
        when(reconciliationFigures.getDebitsNumber()).thenReturn(4L);
        when(reconciliationFigures.getDebitsReversalNumber()).thenReturn(2L);
        when(reconciliationFigures.getCreditsAmount()).thenReturn(100L);
        when(reconciliationFigures.getCreditsReversalAmount()).thenReturn(10L);
        when(reconciliationFigures.getDebitsAmount()).thenReturn(40L);
        when(reconciliationFigures.getDebitsReversalAmount()).thenReturn(20L);
        when(orgReconciliation.getReconciliationFigures()).thenReturn(reconciliationFigures);
        when(transRec.getReconciliation()).thenReturn(orgReconciliation);
        when(paymentSwitch.getAiic()).thenReturn("407643");
        when(paymentSwitch.isDisableSecurity()).thenReturn(true);
        when(payCfg.getPaymentSwitch()).thenReturn(paymentSwitch);
        when(dependency.getPayCfg()).thenReturn(payCfg);
        when(audit.getTransDateTimeAsString("MMddHHmmss")).thenReturn("0609164352");
        when(audit.getTransDateTimeAsString("HHmmss", payCfg.getBankTimeZone())).thenReturn("174453");
        when(audit.getTransDateTimeAsString("MMdd", payCfg.getBankTimeZone())).thenReturn("0710");

        String field61_500 = "C20101";

        byte[] packedMessage = pack(dependency, transRec, MsgType.RECONCILIATION, "");

        String expected = "0500" +
                "A238000100C040080078078080000000" +
                "950000" +
                "0609164352" +
                "001234" +
                "174453" +
                "0710" +
                "06" + // LL n
                "407643" +
                "5735383930303033" +
                "363131303030363032303035383930" +
                "0036" +
                "303036" + // LLL
                Util.byteArrayToHexString(field61_500.getBytes()) +
                "0000000010" +
                "00000000010" +
                "00000000400" +
                "00000002000" +
                "000000000010" +
                "000000000000" +
                "000100000000" +
                "000000040000" +
                "000000000002" +
                "00C0000000000000000";
        assertEquals(expected, Util.byteArrayToHexString(packedMessage));
    }

    @Test
    public void shouldUnpackSettlementResponseWithOnlyTwoSchemeRecords() throws Exception {
        TransRec transRec = new TransRec();
        transRec.setTransType(RECONCILIATION);
        transRec.setProtocol(protocol);
        transRec.setAudit(audit);

        when(protocol.getStan()).thenReturn(24102);
        when(audit.getTerminalId()).thenReturn("F2773051");
        when(audit.getMerchantId()).thenReturn("611000602002773");
        when(paymentSwitch.getAiic()).thenReturn("407643");
        when(paymentSwitch.isDisableSecurity()).thenReturn(true);
        when(payCfg.getPaymentSwitch()).thenReturn(paymentSwitch);
        when(dependency.getPayCfg()).thenReturn(payCfg);
        when(audit.getTransDateTimeAsString("MMddHHmmss")).thenReturn("0609164352");
        when(audit.getTransDateTimeAsString("HHmmss", payCfg.getBankTimeZone())).thenReturn("174453");
        when(audit.getTransDateTimeAsString("MMdd", payCfg.getBankTimeZone())).thenReturn("0710");

        byte[] hostResponse = new byte[]{
                0x05,0x10,(byte)0xa0,0x3a,0x00,0x01,0x02,(byte)0xc1,0x00,0x00,
                0x00,0x7f,(byte)0x87,(byte)0x80,(byte)0x80,0x00,0x06,0x01,(byte)0x96,0x00,
                0x00,0x02,0x41,0x02,0x02,0x09,0x42,0x10,0x01,0x10,
                0x01,0x11,0x00,0x06,0x28,0x00,0x00,0x0f,0x30,
                0x30,0x46,0x32,0x37,0x37,0x33,0x30,0x35,0x31,0x36,
                0x31,0x31,0x30,0x30,0x30,0x36,0x30,0x32,0x30,0x30,
                0x32,0x37,0x37,0x33,0x30,0x36,0x32,
                (byte) 0xC2,0x29,0x20,0x30,0x33,
                0x00,0x00,0x00,0x00,0x13,
                0x00,0x00,0x00,0x00,0x00,0x13,0x00,0x00,
                0x00,0x00,0x00,0x00,0x23,
                0x00,0x00,0x00,0x00,0x00,0x00,0x13,0x00,
                (byte) 0xC2,0x29,0x20,0x33,0x33,
                0x00,0x00,0x00,0x10,0x33,
                0x00,0x00,0x00,0x00,0x00,0x10,0x33,0x00,
                0x00,0x00,0x00,0x02,0x33,
                0x00,0x00,0x00,0x00,0x00,0x00,0x01,0x33,
                0x00,0x00,0x00,0x00,0x00,0x00,
                0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x09,0x00,
                0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
                0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
                0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
                0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
                0x00,0x00,0x00,0x00,0x00,0x00,0x67,0x55,0x00,0x00,
                0x00,0x00,0x00,0x00,0x00,0x00,0x43,0x00,0x00,0x00,
                0x00,0x00,0x00,0x67,0x55,0x00,0x00,0x00,0x00,0x00,
                0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x39,(byte)0xf3,
                0x63,(byte)0x88,0x00,0x00,0x00,0x00};
        As2805TillPack.UnPackResult result;
        result = unpack(dependency, hostResponse, transRec, MsgType.RECONCILIATION);
        assertEquals(UNPACK_OK, result);
        assertEquals("03", transRec.getSchemeTotals().get(0).getCardNameIndex());
        assertEquals(13, transRec.getSchemeTotals().get(0).getCreditNumber().longValue());
        assertEquals(130000, transRec.getSchemeTotals().get(0).getCreditAmount().longValue());
        assertEquals(23, transRec.getSchemeTotals().get(0).getDebitNumber().longValue());
        assertEquals(1300, transRec.getSchemeTotals().get(0).getDebitAmount().longValue());
        assertEquals("33", transRec.getSchemeTotals().get(1).getCardNameIndex());
        assertEquals(1033, transRec.getSchemeTotals().get(1).getCreditNumber().longValue());
        assertEquals(103300, transRec.getSchemeTotals().get(1).getCreditAmount().longValue());
        assertEquals(233, transRec.getSchemeTotals().get(1).getDebitNumber().longValue());
        assertEquals(133, transRec.getSchemeTotals().get(1).getDebitAmount().longValue());
    }

    @Test
    @SuppressWarnings("java:S5961") // More assertions needed to validate message parsing for this UT
    public void shouldUnpackSettlementResponse() throws Exception {
        TransRec transRec = new TransRec();
        transRec.setTransType(RECONCILIATION);
        transRec.setProtocol(protocol);
        transRec.setAudit(audit);

        when(protocol.getStan()).thenReturn(24102);
        when(audit.getTerminalId()).thenReturn("F2773051");
        when(audit.getMerchantId()).thenReturn("611000602002773");
        when(paymentSwitch.getAiic()).thenReturn("407643");
        when(paymentSwitch.isDisableSecurity()).thenReturn(true);
        when(payCfg.getPaymentSwitch()).thenReturn(paymentSwitch);
        when(dependency.getPayCfg()).thenReturn(payCfg);
        when(audit.getTransDateTimeAsString("MMddHHmmss")).thenReturn("0609164352");
        when(audit.getTransDateTimeAsString("HHmmss", payCfg.getBankTimeZone())).thenReturn("174453");
        when(audit.getTransDateTimeAsString("MMdd", payCfg.getBankTimeZone())).thenReturn("0710");

        byte[] hostResponse = new byte[]{
                0x05,0x10,(byte)0xa0,0x3a,0x00,0x01,0x02,(byte)0xc1,0x00,0x00,
                0x00,0x7f,(byte)0x87,(byte)0x80,(byte)0x80,0x00,0x06,0x01,(byte)0x96,0x00,
                0x00,0x02,0x41,0x02,0x02,0x09,0x42,0x10,0x01,0x10,
                0x01,0x11,0x00,0x06,0x28,0x00,0x00,0x0f,0x30,
                0x30,0x46,0x32,0x37,0x37,0x33,0x30,0x35,0x31,0x36,
                0x31,0x31,0x30,0x30,0x30,0x36,0x30,0x32,0x30,0x30,
                0x32,0x37,0x37,0x33,0x31,0x38,0x36,
                (byte) 0xC2,0x29,0x20,0x30,0x34,
                0x00,0x00,0x00,0x00,0x11,
                0x00,0x00,0x00,0x00,0x00,0x02,0x00,0x00,
                0x00,0x00,0x00,0x00,0x33,
                0x00,0x00,0x00,0x00,0x00,0x00,0x40,0x00,
                (byte) 0xC2,0x29,0x20,0x30,0x35,
                0x00,0x00,0x00,0x11,0x00,
                0x00,0x00,0x00,0x00,0x02,0x00,0x00,0x00,
                0x00,0x00,0x00,0x22,0x00,
                0x00,0x00,0x00,0x01,0x00,0x00,0x00,0x00,
                (byte) 0xC2,0x29,0x20,0x39,0x39,
                0x00,0x00,0x00,0x00, (byte) 0x89,
                (byte) 0x99,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
                0x00,0x40,0x00,0x00,0x02,
                0x00,0x00,0x50,0x00,0x00,0x00,0x00,0x00,
                (byte) 0xC2,0x29,0x20,0x30,0x33,
                0x00,0x00,0x00,0x00,0x13,
                0x00,0x00,0x00,0x00,0x00,0x13,0x00,0x00,
                0x00,0x00,0x00,0x00,0x23,
                0x00,0x00,0x00,0x00,0x00,0x00,0x13,0x00,
                (byte) 0xC2,0x29,0x20,0x33,0x33,
                0x00,0x00,0x00,0x10,0x33,
                0x00,0x00,0x00,0x00,0x00,0x10,0x33,0x00,
                0x00,0x00,0x00,0x02,0x33,
                0x00,0x00,0x00,0x00,0x00,0x00,0x01,0x33,
                (byte) 0xC2,0x29,0x20,0x30,0x37,
                0x00,0x00,0x00,0x00,0x17,
                0x00,0x00,0x00,0x00,0x00,0x17,0x00,0x00,
                0x00,0x00,0x00,0x00,0x27,
                0x00,0x00,0x00,0x00,0x00,0x00,0x17,0x00,
                0x00,0x00,0x00,0x00,0x00,0x00,
                0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x09,0x00,
                0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
                0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
                0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
                0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
                0x00,0x00,0x00,0x00,0x00,0x00,0x67,0x55,0x00,0x00,
                0x00,0x00,0x00,0x00,0x00,0x00,0x43,0x00,0x00,0x00,
                0x00,0x00,0x00,0x67,0x55,0x00,0x00,0x00,0x00,0x00,
                0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x39,(byte)0xf3,
                0x63,(byte)0x88,0x00,0x00,0x00,0x00};
        As2805TillPack.UnPackResult result;
        result = unpack(dependency, hostResponse, transRec, MsgType.RECONCILIATION);
        assertEquals(UNPACK_OK, result);
        assertEquals("04", transRec.getSchemeTotals().get(0).getCardNameIndex());
        assertEquals(11, transRec.getSchemeTotals().get(0).getCreditNumber().longValue());
        assertEquals(20000, transRec.getSchemeTotals().get(0).getCreditAmount().longValue());
        assertEquals(33, transRec.getSchemeTotals().get(0).getDebitNumber().longValue());
        assertEquals(4000, transRec.getSchemeTotals().get(0).getDebitAmount().longValue());
        assertEquals("05", transRec.getSchemeTotals().get(1).getCardNameIndex());
        assertEquals(1100, transRec.getSchemeTotals().get(1).getCreditNumber().longValue());
        assertEquals(2000000, transRec.getSchemeTotals().get(1).getCreditAmount().longValue());
        assertEquals(2200, transRec.getSchemeTotals().get(1).getDebitNumber().longValue());
        assertEquals(100000000, transRec.getSchemeTotals().get(1).getDebitAmount().longValue());
        assertEquals("99", transRec.getSchemeTotals().get(2).getCardNameIndex());
        assertEquals(89, transRec.getSchemeTotals().get(2).getCreditNumber().longValue());
        assertEquals(9900000000000000L, transRec.getSchemeTotals().get(2).getCreditAmount().longValue());
        assertEquals(40000002, transRec.getSchemeTotals().get(2).getDebitNumber().longValue());
        assertEquals(500000000000L, transRec.getSchemeTotals().get(2).getDebitAmount().longValue());
        assertEquals("03", transRec.getSchemeTotals().get(3).getCardNameIndex());
        assertEquals("33", transRec.getSchemeTotals().get(4).getCardNameIndex());
        assertEquals("07", transRec.getSchemeTotals().get(5).getCardNameIndex());
        assertEquals(17, transRec.getSchemeTotals().get(5).getCreditNumber().longValue());
        assertEquals(170000, transRec.getSchemeTotals().get(5).getCreditAmount().longValue());
        assertEquals(27, transRec.getSchemeTotals().get(5).getDebitNumber().longValue());
        assertEquals(1700, transRec.getSchemeTotals().get(5).getDebitAmount().longValue());
    }

    @Test
    public void shouldUnpackSettlementResponseAndPopulateReconciliationRecord() throws Exception {
        TransRec transRec = new TransRec();
        transRec.setTransType(RECONCILIATION);
        transRec.setProtocol(protocol);
        transRec.setAudit(audit);

        when(protocol.getStan()).thenReturn(521);
        when(audit.getTerminalId()).thenReturn("TI530004");
        when(audit.getMerchantId()).thenReturn("800000050000101");
        when(paymentSwitch.getAiic()).thenReturn("407642");
        when(paymentSwitch.isDisableSecurity()).thenReturn(true);
        when(payCfg.getPaymentSwitch()).thenReturn(paymentSwitch);
        when(dependency.getPayCfg()).thenReturn(payCfg);
        when(audit.getTransDateTimeAsString("MMddHHmmss")).thenReturn("0609164352");
        when(audit.getTransDateTimeAsString("HHmmss", payCfg.getBankTimeZone())).thenReturn("174453");
        when(audit.getTransDateTimeAsString("MMdd", payCfg.getBankTimeZone())).thenReturn("0710");

        byte[] hostResponse =
                Util.hexStringToByteArray(
                        "0510A23A000102C1400000780780800006019500000301002324000521002324030103010640764239355449353330303034383030303030303530303030313031313836C2292030340000000000000000000000000000000000000000000000000000C2292030350000000000000000000000000000000000030000000000001300C2292039390000000003000000000000318000000000100000000000023164C2292030330000000000000000000000000000000000000000000000000000C2292033330000000000000000000000000000000000000000000000000000C2292030370000000000000000000000000000000000020000000000003533" +
                        "0036" + // currency code
                        "0000000004" + // credits number
                        "0000000001" + // credits, reversal number
                        "0000000018" + // debits number
                        "0000000003" + // debits, reversal number
                        "0000000000008180" + // credits amount
                        "0000000000005000" + // credits, reversal amount
                        "0000000000105774" + // debits amount
                        "0000000000077777" + // debits, reversal amount
                        "440000000000024817" + // amount, net settlement
                        "0000000010" + // cash, total no
                        "0000000000001000" + // cash, total amount
                        "2997420800000000");
        As2805TillPack.UnPackResult result;


        result = unpack(dependency, hostResponse, transRec, MsgType.RECONCILIATION);
        assertEquals(UNPACK_OK, result);
        assertEquals(18, transRec.getReconciliation().getSale().count);
        assertEquals(3, transRec.getReconciliation().getSale().reversalCount);
        assertEquals(105774, transRec.getReconciliation().getSale().amount);
        assertEquals(77777, transRec.getReconciliation().getSale().reversalAmount);
        assertEquals(4, transRec.getReconciliation().getRefund().count);
        assertEquals(1, transRec.getReconciliation().getRefund().reversalCount);
        assertEquals(8180, transRec.getReconciliation().getRefund().amount);
        assertEquals(5000, transRec.getReconciliation().getRefund().reversalAmount);
        assertEquals(10, transRec.getReconciliation().getCash().count);
        assertEquals(1000, transRec.getReconciliation().getCash().amount);
    }

    @Test
    public void shouldUnpackSettlementResponseAndPopulateReconciliationRecordWithNetTotalAsCredit() throws Exception {
        TransRec transRec = new TransRec();
        transRec.setTransType(RECONCILIATION);
        transRec.setProtocol(protocol);
        transRec.setAudit(audit);

        when(protocol.getStan()).thenReturn(521);
        when(audit.getTerminalId()).thenReturn("TI530004");
        when(audit.getMerchantId()).thenReturn("800000050000101");
        when(paymentSwitch.getAiic()).thenReturn("407642");
        when(paymentSwitch.isDisableSecurity()).thenReturn(true);
        when(payCfg.getPaymentSwitch()).thenReturn(paymentSwitch);
        when(dependency.getPayCfg()).thenReturn(payCfg);
        when(audit.getTransDateTimeAsString("MMddHHmmss")).thenReturn("0609164352");
        when(audit.getTransDateTimeAsString("HHmmss", payCfg.getBankTimeZone())).thenReturn("174453");
        when(audit.getTransDateTimeAsString("MMdd", payCfg.getBankTimeZone())).thenReturn("0710");

        byte[] hostResponse =
                Util.hexStringToByteArray(
                        "0510A23A000102C1400000780780800006019500000301002324000521002324030103010640764239355449353330303034383030303030303530303030313031313836C2292030340000000000000000000000000000000000000000000000000000C2292030350000000000000000000000000000000000030000000000001300C2292039390000000003000000000000318000000000100000000000023164C2292030330000000000000000000000000000000000000000000000000000C2292033330000000000000000000000000000000000000000000000000000C2292030370000000000000000000000000000000000020000000000003533" +
                                "0036" + // currency code
                                "0000000002" + // credits number
                                "0000000000" + // credits, reversal number
                                "0000000003" + // debits number
                                "0000000000" + // debits, reversal number
                                "0000000000109517" + // credits amount
                                "0000000000000000" + // credits, reversal amount
                                "0000000000022410" + // debits amount
                                "0000000000000000" + // debits, reversal amount
                                "430000000000087107" + // amount, net settlement
                                "0000000001" + // cash, total no
                                "0000000000006500" + // cash, total amount
                                "2997420800000000");
        As2805TillPack.UnPackResult result;
        result = unpack(dependency, hostResponse, transRec, MsgType.RECONCILIATION);
        assertEquals(UNPACK_OK, result);
        assertEquals(3, transRec.getReconciliation().getSale().count);
        assertEquals(0, transRec.getReconciliation().getSale().reversalCount);
        assertEquals(22410, transRec.getReconciliation().getSale().amount);
        assertEquals(0, transRec.getReconciliation().getSale().reversalAmount);
        assertEquals(2, transRec.getReconciliation().getRefund().count);
        assertEquals(0, transRec.getReconciliation().getRefund().reversalCount);
        assertEquals(109517, transRec.getReconciliation().getRefund().amount);
        assertEquals(0, transRec.getReconciliation().getRefund().reversalAmount);
        assertEquals(1, transRec.getReconciliation().getCash().count);
        assertEquals(6500, transRec.getReconciliation().getCash().amount);
        assertEquals(-87107, transRec.getReconciliation().getTotalAmount());
    }

    @Test
    public void shouldUnpackSettlementResponseAndPopulateReconciliationRecordWithNetTotalAsDebit() throws Exception {
        TransRec transRec = new TransRec();
        transRec.setTransType(RECONCILIATION);
        transRec.setProtocol(protocol);
        transRec.setAudit(audit);

        when(protocol.getStan()).thenReturn(521);
        when(audit.getTerminalId()).thenReturn("TI530004");
        when(audit.getMerchantId()).thenReturn("800000050000101");
        when(paymentSwitch.getAiic()).thenReturn("407642");
        when(paymentSwitch.isDisableSecurity()).thenReturn(true);
        when(payCfg.getPaymentSwitch()).thenReturn(paymentSwitch);
        when(dependency.getPayCfg()).thenReturn(payCfg);
        when(audit.getTransDateTimeAsString("MMddHHmmss")).thenReturn("0609164352");
        when(audit.getTransDateTimeAsString("HHmmss", payCfg.getBankTimeZone())).thenReturn("174453");
        when(audit.getTransDateTimeAsString("MMdd", payCfg.getBankTimeZone())).thenReturn("0710");

        byte[] hostResponse =
                Util.hexStringToByteArray(
                        "0510A23A000102C1400000780780800006019500000301002324000521002324030103010640764239355449353330303034383030303030303530303030313031313836C2292030340000000000000000000000000000000000000000000000000000C2292030350000000000000000000000000000000000030000000000001300C2292039390000000003000000000000318000000000100000000000023164C2292030330000000000000000000000000000000000000000000000000000C2292033330000000000000000000000000000000000000000000000000000C2292030370000000000000000000000000000000000020000000000003533" +
                                "0036" + // currency code
                                "0000000002" + // credits number
                                "0000000000" + // credits, reversal number
                                "0000000003" + // debits number
                                "0000000000" + // debits, reversal number
                                "0000000000109517" + // credits amount
                                "0000000000000000" + // credits, reversal amount
                                "0000000000022410" + // debits amount
                                "0000000000000000" + // debits, reversal amount
                                "440000000000077107" + // amount, net settlement
                                "0000000001" + // cash, total no
                                "0000000000006500" + // cash, total amount
                                "2997420800000000");
        As2805TillPack.UnPackResult result;
        result = unpack(dependency, hostResponse, transRec, MsgType.RECONCILIATION);
        assertEquals(UNPACK_OK, result);
        assertEquals(3, transRec.getReconciliation().getSale().count);
        assertEquals(0, transRec.getReconciliation().getSale().reversalCount);
        assertEquals(22410, transRec.getReconciliation().getSale().amount);
        assertEquals(0, transRec.getReconciliation().getSale().reversalAmount);
        assertEquals(2, transRec.getReconciliation().getRefund().count);
        assertEquals(0, transRec.getReconciliation().getRefund().reversalCount);
        assertEquals(109517, transRec.getReconciliation().getRefund().amount);
        assertEquals(0, transRec.getReconciliation().getRefund().reversalAmount);
        assertEquals(1, transRec.getReconciliation().getCash().count);
        assertEquals(6500, transRec.getReconciliation().getCash().amount);
        assertEquals(77107, transRec.getReconciliation().getTotalAmount());
    }

    @Test
    @SuppressWarnings("java:S5961") // More assertions needed to validate message parsing for this UT
    public void test0510Unpack() throws Exception{
        final byte[] inputData = {
                0x05,0x10,(byte)0xa0,0x3a,0x00,0x01,0x02,(byte)0xc1,0x00,0x00,
                0x00,0x7f,(byte)0x87,(byte)0x80,(byte)0x80,0x00,0x06,0x01,(byte)0x96,0x00,
                0x00,0x02,0x41,0x02,0x02,0x09,0x42,0x10,0x01,0x10,
                0x01,0x11,0x00,0x06,0x28,0x00,0x00,0x0f,0x30,
                0x30,0x46,0x32,0x37,0x37,0x33,0x30,0x35,0x31,0x36,
                0x31,0x31,0x30,0x30,0x30,0x36,0x30,0x32,0x30,0x30,
                0x32,0x37,0x37,0x33,0x30,0x36,0x32,
                (byte) 0xC2,0x29,0x20,0x30,0x34,0x00,0x00,0x00,0x00,0x02,0x00,
                0x00,0x00,0x00,0x00,0x01,0x00,0x00,0x00,0x00,0x00,
                0x00,0x01,0x00,0x00,0x00,0x00,0x00,0x00,0x01,0x00,
                (byte) 0xC2,0x29,0x20,0x30,0x35,0x00,0x00,0x00,0x00,0x10,0x00,
                0x00,0x00,0x00,0x00,0x10,0x00,0x00,0x00,0x00,0x00,
                0x00,0x02,0x00,0x00,0x00,0x00,0x00,0x00,0x01,0x00,
                0x00,0x00,0x00,0x00,0x00,0x00,
                0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x09,0x00,
                0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
                0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
                0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
                0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
                0x00,0x00,0x00,0x00,0x00,0x00,0x67,0x55,0x00,0x00,
                0x00,0x00,0x00,0x00,0x00,0x00,0x43,0x00,0x00,0x00,
                0x00,0x00,0x00,0x67,0x55,0x00,0x00,0x00,0x00,0x00,
                0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x39,(byte)0xf3,
                0x63,(byte)0x88,0x00,0x00,0x00,0x00};
        As2805Till msg = new As2805Till(inputData);

        assertEquals( Iso8583.MsgType._0510_ACQUIRER_RECONCILE_REQ_RSP, msg.getMsgType() );
        assertEquals( "960000", msg.get(_003_PROC_CODE));
        assertEquals( "024102", msg.get(_011_SYS_TRACE_AUDIT_NUM));
        assertEquals( "020942", msg.get(_012_LOCAL_TRAN_TIME));
        assertEquals( "1001", msg.get(_013_LOCAL_TRAN_DATE));
        assertEquals( "1001", msg.get(_015_SETTLEMENT_DATE));
        assertEquals( "00062800000", msg.get(_032_ACQUIRING_INST_ID_CODE));
        assertEquals( "00", msg.get(_039_RESPONSE_CODE));
        assertEquals( "F2773051", msg.get(_041_CARD_ACCEPTOR_TERMINAL_ID));
        assertEquals( "611000602002773", msg.get(_042_CARD_ACCEPTOR_ID_CODE));
        assertEquals( "C2292030340000000002000000000001000000000000010000000000000100C2292030350000000010000000000010000000000000020000000000000100", msg.get(_048_ADDITIONAL_DATA));
        assertEquals( "0000000000", msg.get(_074_CREDITS_NUMBER));
        assertEquals( "0000000000", msg.get(_075_CREDITS_REVERSAL_NUMBER));
        assertEquals( "0000000009", msg.get(_076_DEBITS_NUMBER));
        assertEquals( "0000000000", msg.get(_077_DEBITS_REVERSAL_NUMBER));
        assertEquals( "0000000000", msg.get(_078_TRANSFER_NUMBER));
        assertEquals( "0000000000", msg.get(_079_TRANSFER_REVERSAL_NUMBER));
        assertEquals( "0000000000", msg.get(_080_INQUIRIES_NUMBER));
        assertEquals( "0000000000", msg.get(_081_AUTHORISATIONS_NUMBER));
        assertEquals( "0000000000000000", msg.get(_086_CREDITS_AMOUNT));
        assertEquals( "0000000000000000", msg.get(_087_CREDITS_REVERSAL_AMOUNT));
        assertEquals( "0000000000006755", msg.get(_088_DEBITS_AMOUNT));
        assertEquals( "0000000000000000", msg.get(_089_DEBITS_REVERSAL_AMOUNT));
        assertEquals( "430000000000006755", msg.get(_097_AMOUNT_NET_SETTLEMENT));
        assertEquals( "0000000000", msg.get(DE_118_CASHOUTS_NUMBER));
        assertEquals( "0000000000000000", msg.get(DE_119_CASHOUTS_AMOUNT));
        assertEquals( "39F3638800000000", msg.get(DE_128_MAC));
    }

    @Test
    public void shouldExecuteTestInBatch() throws Exception {
        shouldPackPreAuthRequestMessage();
        tearDown();
        setUp();
        shouldPackRsaLogonRequestMessage();
        tearDown();
        setUp();
        shouldPackAdviseRequestMessage();
    }

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
        when(cardInfo.isManual()).thenReturn(false);
        when(transRec.getCard()).thenReturn(cardInfo);
        when(protocol.getStan()).thenReturn(1234);
        when(transRec.getProtocol()).thenReturn(protocol);
        when(audit.getTransDateTimeAsString("ddMMHHmmss")).thenReturn(dateTime);
        when(payCfg.getStid()).thenReturn("W5890003");
        when(payCfg.getMid()).thenReturn("611000602005890");
        when(transRec.getAudit()).thenReturn(audit);
        when(paymentSwitch.getAiic()).thenReturn("407643");
        when(paymentSwitch.isDisableSecurity()).thenReturn(true);
        when(payCfg.getPaymentSwitch()).thenReturn(paymentSwitch);
        when(dependency.getPayCfg()).thenReturn(payCfg);

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

        when(cardInfo.getCaptureMethod()).thenReturn(CTLS);
        when(transRec.getTransType()).thenReturn(SALE);
        when(sec.getEncTrack2()).thenReturn("EEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEE");
        when(sec.getCvv()).thenReturn("353637");
        when(transRec.getSecurity()).thenReturn(sec);
        when(protocol.getRRN()).thenReturn("000000000013");


        when(transRec.getAmounts()).thenReturn(amounts);
        when(cardInfo.isManual()).thenReturn(false);
        when(transRec.getCard()).thenReturn(cardInfo);
        when(protocol.getStan()).thenReturn(1234);
        when(transRec.getProtocol()).thenReturn(protocol);
        when(audit.getTransDateTimeAsString("MMddHHmmss")).thenReturn("0809040103");
        when(payCfg.getStid()).thenReturn("W5890003");
        when(payCfg.getMid()).thenReturn("611000602005890");
        when(transRec.getAudit()).thenReturn(audit);
        when(paymentSwitch.getAiic()).thenReturn("407643");
        when(paymentSwitch.isDisableSecurity()).thenReturn(true);
        when(payCfg.getPaymentSwitch()).thenReturn(paymentSwitch);
        when(dependency.getPayCfg()).thenReturn(payCfg);
    }

}