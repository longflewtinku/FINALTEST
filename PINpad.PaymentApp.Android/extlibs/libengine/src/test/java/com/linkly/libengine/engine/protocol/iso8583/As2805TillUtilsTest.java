package com.linkly.libengine.engine.protocol.iso8583;

import static com.linkly.libengine.engine.transactions.properties.TCard.CardIssuer.AMEX;
import static com.linkly.libengine.engine.transactions.properties.TCard.CardIssuer.MASTERCARD;
import static com.linkly.libengine.engine.transactions.properties.TCard.CardIssuer.VISA;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.ACC_TYPE_CHEQUE;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.ACC_TYPE_CREDIT;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.ACC_TYPE_SAVINGS;
import static com.linkly.libmal.global.util.Util.hexStringToByteArray;
import static com.linkly.libsecapp.emv.Tag.appl_intchg_profile;
import static com.linkly.libsecapp.emv.Tag.eftpos_payment_account_reference;
import static com.linkly.libsecapp.emv.Tag.tvr;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

import com.linkly.libengine.dependencies.IDependency;
import com.linkly.libengine.engine.EngineManager;
import com.linkly.libengine.engine.protocol.IProto;
import com.linkly.libengine.engine.protocol.iso8583.openisoj.ProcessingCode;
import com.linkly.libengine.engine.protocol.iso8583.openisoj.exceptions.FormatException;
import com.linkly.libengine.engine.transactions.TransRec;
import com.linkly.libengine.engine.transactions.properties.TCard;
import com.linkly.libengine.engine.transactions.properties.TProtocol;
import com.linkly.libsecapp.emv.EmvTag;
import com.linkly.libsecapp.emv.EmvTags;
import com.linkly.libsecapp.emv.Tag;
import com.linkly.libsecapp.emv.Util;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

import java.util.ArrayList;

public class As2805TillUtilsTest {

    @Mock
    TransRec transRec;

    @Mock
    TProtocol protocol;

    @Mock
    TCard tCard;

    @Mock
    IDependency dependency;

    @Mock
    IProto iProto;

    @Before
    public void setUp() {
        openMocks(this);
        when(transRec.getProtocol()).thenReturn(protocol);
    }

    @Test
    public void shouldPopulateProcessingCodeForRefundCreditTransaction() throws FormatException {
        when(transRec.isRefund()).thenReturn(true);
        when(protocol.getAccountType()).thenReturn(ACC_TYPE_CREDIT);
        ProcessingCode processingCode = As2805TillUtils.packProcCode(transRec);
        assertEquals("203000", processingCode.toString());
    }

    @Test
    public void shouldPopulateProcessingCodeForRefundSavingsTransaction() throws FormatException {
        when(transRec.isRefund()).thenReturn(true);
        when(protocol.getAccountType()).thenReturn(ACC_TYPE_SAVINGS);
        ProcessingCode processingCode = As2805TillUtils.packProcCode(transRec);
        assertEquals("201000", processingCode.toString());
    }

    @Test
    public void shouldPopulateProcessingCodeForRefundChequeTransaction() throws FormatException {
        when(transRec.isRefund()).thenReturn(true);
        when(protocol.getAccountType()).thenReturn(ACC_TYPE_CHEQUE);
        ProcessingCode processingCode = As2805TillUtils.packProcCode(transRec);
        assertEquals("202000", processingCode.toString());
    }

    @Test
    public void shouldPopulateProcessingCodeForPurchaseCreditTransaction() throws FormatException {
        when(transRec.isRefund()).thenReturn(true);
        when(protocol.getAccountType()).thenReturn(ACC_TYPE_CREDIT);
        ProcessingCode processingCode = As2805TillUtils.packProcCode(transRec);
        assertEquals("203000", processingCode.toString());
    }

    @Test
    public void shouldPopulateProcessingCodeForCompletionCreditTransaction() throws FormatException {
        when(transRec.isCompletion()).thenReturn(true);
        when(protocol.getAccountType()).thenReturn(ACC_TYPE_CREDIT);
        ProcessingCode processingCode = As2805TillUtils.packProcCode(transRec);
        assertEquals("003000", processingCode.toString());
    }

    @Test
    public void shouldPopulateProcessingCodeForPreAuthCreditTransaction() throws FormatException {
        when(transRec.isPreAuth()).thenReturn(true);
        when(protocol.getAccountType()).thenReturn(ACC_TYPE_CREDIT);
        ProcessingCode processingCode = As2805TillUtils.packProcCode(transRec);
        assertEquals("003000", processingCode.toString());
    }

    @Test
    public void shouldPopulateProcessingCodeForPreAuthCancellationCreditTransaction() throws FormatException {
        when(transRec.isPreAuthCancellation()).thenReturn(true);
        when(protocol.getAccountType()).thenReturn(ACC_TYPE_CREDIT);
        ProcessingCode processingCode = As2805TillUtils.packProcCode(transRec);
        assertEquals("203000", processingCode.toString());
    }

    @Test
    public void shouldPopulateProcessingCodeForSettlement() throws FormatException {
        when(transRec.isReconciliation()).thenReturn(true);
        ProcessingCode processingCode = As2805TillUtils.packProcCode(transRec);
        assertEquals("950000", processingCode.toString());
    }

    @Test
    public void shouldPopulateProcessingCodeForLastSettlement() throws FormatException {
        when(transRec.isLastReconciliation()).thenReturn(true);
        ProcessingCode processingCode = As2805TillUtils.packProcCode(transRec);
        assertEquals("960000", processingCode.toString());
    }

    @Test
    public void shouldPopulateProcessingCodeForPreSettlement() throws FormatException {
        when(transRec.isPreReconciliation()).thenReturn(true);
        ProcessingCode processingCode = As2805TillUtils.packProcCode(transRec);
        assertEquals("970000", processingCode.toString());
    }

    /*  Field 022 – Point of Service Entry Mode (POSEM)
        051 EMV smart card in an EMV capable terminal
        052 EMV smart card in an EMV capable terminal with no PINpad
        071 Contactless using EMV data
        072 Contactless using EMV data and terminal does not have a PINpad
        101 Stored Credential in an EMV capable terminal
        102 Stored Credential in an EMV capable terminal with no PINpad
        611 PAN manually entered in an EMV capable terminal
        621 PAN read from MSR in an EMV capable terminal
        911 Contactless using Magnetic Stripe Data
     */
    @Test
    public void shouldPopulatePoseForManualEntryMode() {
        when(transRec.getCard()).thenReturn(tCard);
        when(tCard.getCaptureMethod()).thenReturn(TCard.CaptureMethod.MANUAL);
        assertEquals("611", As2805TillUtils.packPosEntryMode(transRec));
    }

    @Test
    public void shouldPopulatePoseForFallbackSwipedEntryMode() {
        when(transRec.getCard()).thenReturn(tCard);
        when(tCard.getCaptureMethod()).thenReturn(TCard.CaptureMethod.ICC_FALLBACK_SWIPED);
        assertEquals("621", As2805TillUtils.packPosEntryMode(transRec));
    }

    @Test
    public void shouldPopulatePoseForSwipedEntryMode() {
        when(transRec.getCard()).thenReturn(tCard);
        when(tCard.getCaptureMethod()).thenReturn(TCard.CaptureMethod.SWIPED);
        assertEquals("621", As2805TillUtils.packPosEntryMode(transRec));
    }

    @Test
    public void shouldPopulatePoseForCtlsEntryMode() {
        when(transRec.getCard()).thenReturn(tCard);
        when(tCard.getCaptureMethod()).thenReturn(TCard.CaptureMethod.CTLS);
        assertEquals("071", As2805TillUtils.packPosEntryMode(transRec));
    }

    @Test
    public void shouldPopulatePoseForCtlsMsrEntryMode() {
        when(transRec.getCard()).thenReturn(tCard);
        when(tCard.getCaptureMethod()).thenReturn(TCard.CaptureMethod.CTLS_MSR);
        assertEquals("911", As2805TillUtils.packPosEntryMode(transRec));
    }

    @Test
    public void shouldPopulatePoseForIccEntryMode() {
        when(transRec.getCard()).thenReturn(tCard);
        when(tCard.getCaptureMethod()).thenReturn(TCard.CaptureMethod.ICC);
        assertEquals("051", As2805TillUtils.packPosEntryMode(transRec));
    }

    @Test
    public void shouldPopulatePoseForIccOfflineEntryMode() {
        when(transRec.getCard()).thenReturn(tCard);
        when(tCard.getCaptureMethod()).thenReturn(TCard.CaptureMethod.ICC_OFFLINE);
        assertEquals("051", As2805TillUtils.packPosEntryMode(transRec));
    }

    @Test
    public void shouldPackFcrInAdditionalDataNational47ForIccFallbackSwiped() {
        String fcrFieldIndicator = "FCR\\";
        when(transRec.getCard()).thenReturn(tCard);
        when(tCard.getCaptureMethod()).thenReturn(TCard.CaptureMethod.ICC_FALLBACK_SWIPED);
        when(tCard.isIccCardSC()).thenReturn(true);
        assertTrue(As2805TillUtils.packAdditionalDataNational47(transRec, ' ', true).contains(fcrFieldIndicator));
    }

    @Test
    public void shouldPackAriInAdditionalDataNational47WhenParTagIsPresent() {
        String expectedAriField = "ARITX3WA5XLLEZRPXAWKI0XOOFGVBDX1\\";
        EmvTags emvTags = mock(EmvTags.class);

        when(transRec.getCard()).thenReturn(tCard);
        when(tCard.getAid()).thenReturn("A000000384");
        when(tCard.getTags()).thenReturn(emvTags);
        when(emvTags.isTagSet(eftpos_payment_account_reference)).thenReturn(true);
        when(emvTags.getTag(eftpos_payment_account_reference)).thenReturn("TX3WA5XLLEZRPXAWKI0XOOFGVBDX1".getBytes());
        assertTrue(As2805TillUtils.packAdditionalDataNational47(transRec, ' ', true).contains(expectedAriField));
    }

    @Test
    public void shouldNotPackAriInAdditionalDataNational47WhenParTagIsWrongLength() {
        String expectedAriField = "ARITX3WXLLEZRPXAWKI0XOOFGVBDX1\\";
        EmvTags emvTags = mock(EmvTags.class);

        when(transRec.getCard()).thenReturn(tCard);
        when(tCard.getAid()).thenReturn("A000000384");
        when(tCard.getTags()).thenReturn(emvTags);
        when(emvTags.isTagSet(eftpos_payment_account_reference)).thenReturn(true);
        when(emvTags.getTag(eftpos_payment_account_reference)).thenReturn("TX3WXLLEZRPXAWKI0XOOFGVBDX1".getBytes());
        assertFalse(As2805TillUtils.packAdditionalDataNational47(transRec, ' ', true).contains(expectedAriField));
    }

    @Test
    public void shouldNotPackAriInAdditionalDataNational47WhenParTagIsNotPresent() {
        EmvTags emvTags = mock(EmvTags.class);

        when(transRec.getCard()).thenReturn(tCard);
        when(tCard.getAid()).thenReturn("A000000384");
        when(tCard.getTags()).thenReturn(emvTags);
        when(emvTags.isTagSet(eftpos_payment_account_reference)).thenReturn(false);
        assertFalse(As2805TillUtils.packAdditionalDataNational47(transRec, ' ', true).contains("ARI"));
    }

    @Test
    public void shouldPackParInIccData55WhenParTagIsPresent() throws Exception {
        EmvTags emvTags = mock(EmvTags.class);
        ArrayList<Tag> tags = new ArrayList<>();
        tags.add(appl_intchg_profile);
        tags.add(eftpos_payment_account_reference);
        tags.add(tvr);

        EmvTag tag1 = mock(EmvTag.class);
        EmvTag tag2 = mock(EmvTag.class);
        EmvTag tag3 = mock(EmvTag.class);

        when(transRec.getCard()).thenReturn(tCard);
        when(tCard.getAid()).thenReturn("A000000384");
        when(tCard.getTags()).thenReturn(emvTags);
        when(emvTags.get(appl_intchg_profile.value())).thenReturn(tag1);
        when(tag1.getData()).thenReturn("A000".getBytes());
        when(tag1.getTag()).thenReturn(appl_intchg_profile.getValue());
        when(tag1.pack()).thenReturn(Util.hexToByteArray("8202A000"));

        when(emvTags.isTagSet(eftpos_payment_account_reference)).thenReturn(true);
        when(emvTags.get(eftpos_payment_account_reference.value())).thenReturn(tag2);
        when(tag2.getData()).thenReturn("TX3WA5XLLEZRPXAWKI0XOOFGVBDX1".getBytes());
        when(tag2.getTag()).thenReturn(eftpos_payment_account_reference.getValue());
        when(tag2.pack()).thenReturn(Util.hexToByteArray("9F241D545833574135584C4C455A52505841574B4930584F4F46475642445831"));

        when(emvTags.get(tvr.value())).thenReturn(tag3);
        when(tag3.getData()).thenReturn("0000000000".getBytes());
        when(tag3.getTag()).thenReturn(tvr.getValue());
        when(tag3.pack()).thenReturn(Util.hexToByteArray("95050000000000"));

        when(dependency.getProtocol()).thenReturn(iProto);
        when(iProto.getEmvTagList()).thenReturn(tags);
        assertEquals("8202A0009F241D545833574135584C4C455A52505841574B4930584F4F4647564244583195050000000000", As2805TillUtils.packIccDataCommon(dependency, transRec, null, false));
        verify(emvTags, never()).remove(eftpos_payment_account_reference.getValue());
    }

    @Test
    public void shouldNotPackParInIccData55WhenParTagIsPresentWithWrongLength() throws Exception {
        EmvTags emvTags = mock(EmvTags.class);
        ArrayList<Tag> tags = new ArrayList<>();
        tags.add(appl_intchg_profile);
        tags.add(eftpos_payment_account_reference);
        tags.add(tvr);

        EmvTag tag1 = mock(EmvTag.class);
        EmvTag tag2 = mock(EmvTag.class);
        EmvTag tag3 = mock(EmvTag.class);

        when(transRec.getCard()).thenReturn(tCard);
        when(tCard.getAid()).thenReturn("A000000384");
        when(tCard.getTags()).thenReturn(emvTags);
        when(emvTags.get(appl_intchg_profile.value())).thenReturn(tag1);
        when(tag1.getData()).thenReturn("A000".getBytes());
        when(tag1.getTag()).thenReturn(appl_intchg_profile.getValue());
        when(tag1.pack()).thenReturn(Util.hexToByteArray("8202A000"));

        when(emvTags.isTagSet(eftpos_payment_account_reference)).thenReturn(true);
        when(emvTags.get(eftpos_payment_account_reference.value())).thenReturn(tag2);
        when(tag2.getData()).thenReturn("TX3WXLLEZRPXAWKI0XOOFGVBDX1".getBytes());
        when(tag2.getTag()).thenReturn(eftpos_payment_account_reference.getValue());
        when(tag2.pack()).thenReturn(Util.hexToByteArray("9F241D54583357584C4C455A52505841574B4930584F4F46475642445831"));

        when(emvTags.get(tvr.value())).thenReturn(tag3);
        when(tag3.getData()).thenReturn("0000000000".getBytes());
        when(tag3.getTag()).thenReturn(tvr.getValue());
        when(tag3.pack()).thenReturn(Util.hexToByteArray("95050000000000"));

        when(dependency.getProtocol()).thenReturn(iProto);
        when(iProto.getEmvTagList()).thenReturn(tags);
        assertEquals("8202A00095050000000000", As2805TillUtils.packIccDataCommon(dependency, transRec, null, false));
        verify(emvTags, times(1)).remove(eftpos_payment_account_reference.getValue());
    }

    @Test
    public void shouldNotPackParInIccData55WhenParTagNotPresent() throws Exception {
        EmvTags emvTags = mock(EmvTags.class);
        ArrayList<Tag> tags = new ArrayList<>();
        tags.add(appl_intchg_profile);
        tags.add(eftpos_payment_account_reference);
        tags.add(tvr);

        EmvTag tag1 = mock(EmvTag.class);
        EmvTag tag3 = mock(EmvTag.class);

        when(transRec.getCard()).thenReturn(tCard);
        when(tCard.getAid()).thenReturn("A000000384");
        when(tCard.getTags()).thenReturn(emvTags);
        when(emvTags.get(appl_intchg_profile.value())).thenReturn(tag1);
        when(tag1.getData()).thenReturn("A000".getBytes());
        when(tag1.getTag()).thenReturn(appl_intchg_profile.getValue());
        when(tag1.pack()).thenReturn(Util.hexToByteArray("8202A000"));

        when(emvTags.get(tvr.value())).thenReturn(tag3);
        when(tag3.getData()).thenReturn("0000000000".getBytes());
        when(tag3.getTag()).thenReturn(tvr.getValue());
        when(tag3.pack()).thenReturn(Util.hexToByteArray("95050000000000"));

        when(dependency.getProtocol()).thenReturn(iProto);
        when(iProto.getEmvTagList()).thenReturn(tags);
        assertEquals("8202A00095050000000000", As2805TillUtils.packIccDataCommon(dependency, transRec, null, false));
        verify(emvTags, never()).remove(eftpos_payment_account_reference.getValue());
    }

    @Test
    public void shouldProcessIssuerAuthDataForAmexScheme() throws FormatException {
        TransRec transRec = new TransRec();
        transRec.setCard(tCard);
        ArgumentCaptor<byte[]> argumentCaptor = ArgumentCaptor.forClass(byte[].class);
        when(tCard.getCardIssuer()).thenReturn(AMEX);

        As2805TillUtils.unpackIccData(transRec, "8A023030910814DB24C68CA33488");

        verify(tCard).setIssuerAuthData(argumentCaptor.capture());
        assertArrayEquals(hexStringToByteArray("14DB24C68CA334883030"), argumentCaptor.getValue());
    }

    @Test
    public void shouldNotProcessIssuerAuthDataForVisaScheme() throws FormatException {
        TransRec transRec = new TransRec();
        transRec.setCard(tCard);
        ArgumentCaptor<byte[]> argumentCaptor = ArgumentCaptor.forClass(byte[].class);
        when(tCard.getCardIssuer()).thenReturn(VISA);

        As2805TillUtils.unpackIccData(transRec, "8A023030910814DB24C68CA33488");

        verify(tCard).setIssuerAuthData(argumentCaptor.capture());
        assertArrayEquals(hexStringToByteArray("14DB24C68CA33488"), argumentCaptor.getValue());
    }

    @Test
    public void shouldProcessIssuerAuthDataForAmexSchemeWithIssuerScripts() throws FormatException {
        TransRec transRec = new TransRec();
        transRec.setCard(tCard);
        ArgumentCaptor<byte[]> argumentCaptor = ArgumentCaptor.forClass(byte[].class);
        when(tCard.getCardIssuer()).thenReturn(AMEX);

        As2805TillUtils.unpackIccData(transRec, "8A023030910827BECCA8393BC6EF72179F180414034803860E04DA9F580903A59F64916A3E69BB");

        verify(tCard).setIssuerAuthData(argumentCaptor.capture());
        assertArrayEquals(hexStringToByteArray("27BECCA8393BC6EF3030"), argumentCaptor.getValue());
        verify(tCard).setScript72Data(argumentCaptor.capture());
        assertArrayEquals(hexStringToByteArray("9F180414034803860E04DA9F580903A59F64916A3E69BB"), argumentCaptor.getValue());
    }

    @Test
    public void shouldNotProcessIssuerAuthDataForAmexSchemeWhenResponseCodeIsMissing() throws FormatException {
        TransRec transRec = new TransRec();
        transRec.setCard(tCard);
        ArgumentCaptor<byte[]> argumentCaptor = ArgumentCaptor.forClass(byte[].class);
        when(tCard.getCardIssuer()).thenReturn(AMEX);

        As2805TillUtils.unpackIccData(transRec, "910A27BECCA8393BC6EF303572179F180414034803860E04DA9F580903A59F64916A3E69BB");

        verify(tCard).setIssuerAuthData(argumentCaptor.capture());
        assertArrayEquals(hexStringToByteArray("27BECCA8393BC6EF3035"), argumentCaptor.getValue());
        verify(tCard).setScript72Data(argumentCaptor.capture());
        assertArrayEquals(hexStringToByteArray("9F180414034803860E04DA9F580903A59F64916A3E69BB"), argumentCaptor.getValue());
    }

    @Test
    public void shouldNotProcessIssuerAuthDataForAmexSchemeWhen91IsMissing() throws FormatException {
        TransRec transRec = new TransRec();
        transRec.setCard(tCard);
        ArgumentCaptor<byte[]> argumentCaptor = ArgumentCaptor.forClass(byte[].class);
        when(tCard.getCardIssuer()).thenReturn(AMEX);

        As2805TillUtils.unpackIccData(transRec, "8A02303072179F180414034803860E04DA9F580903A59F64916A3E69BB");

        verify(tCard, never()).setIssuerAuthData(argumentCaptor.capture());
        verify(tCard).setScript72Data(argumentCaptor.capture());
        assertArrayEquals(hexStringToByteArray("9F180414034803860E04DA9F580903A59F64916A3E69BB"), argumentCaptor.getValue());
    }

    @Test
    public void shouldNotProcessIssuerAuthDataForMastercardSchemeWithIssuerScripts() throws FormatException {
        TransRec transRec = new TransRec();
        transRec.setCard(tCard);
        ArgumentCaptor<byte[]> argumentCaptor = ArgumentCaptor.forClass(byte[].class);
        when(tCard.getCardIssuer()).thenReturn(MASTERCARD);

        As2805TillUtils.unpackIccData(transRec, "8A023030910827BECCA8393BC6EF72179F180414034803860E04DA9F580903A59F64916A3E69BB");

        verify(tCard).setIssuerAuthData(argumentCaptor.capture());
        assertArrayEquals(hexStringToByteArray("27BECCA8393BC6EF"), argumentCaptor.getValue());
        verify(tCard).setScript72Data(argumentCaptor.capture());
        assertArrayEquals(hexStringToByteArray("9F180414034803860E04DA9F580903A59F64916A3E69BB"), argumentCaptor.getValue());
    }

    @Test
    public void shouldReturnPosConditionCodeForSaleAutoTransaction() {
        when(transRec.getTransType()).thenReturn(EngineManager.TransType.SALE_AUTO);
        assertEquals("04", As2805TillUtils.packPosConditionCode(transRec));
    }

    @Test
    public void shouldReturnPosConditionCodeForRefundAutoTransaction() {
        when(transRec.getTransType()).thenReturn(EngineManager.TransType.REFUND_AUTO);
        assertEquals("04", As2805TillUtils.packPosConditionCode(transRec));
    }

    @Test
    public void shouldReturnPosConditionCodeForSaleMotoAutoTransaction() {
        TransRec transRec1 = new TransRec();
        transRec1.setTransType(EngineManager.TransType.SALE_MOTO_AUTO);
        assertEquals("08", As2805TillUtils.packPosConditionCode(transRec1));
    }

    @Test
    public void shouldReturnPosConditionCodeForMotoTransaction() {
        when(transRec.isMoto()).thenReturn(true);
        assertEquals("08", As2805TillUtils.packPosConditionCode(transRec));
    }

    @Test
    public void shouldReturnPosConditionCodeForStandaloneSaleTransaction() {
        when(transRec.getTransType()).thenReturn(EngineManager.TransType.SALE);
        assertEquals("42", As2805TillUtils.packPosConditionCode(transRec));
    }

    @Test
    public void shouldReturnPosConditionCodeForStandaloneRefundTransaction() {
        when(transRec.getTransType()).thenReturn(EngineManager.TransType.REFUND);
        assertEquals("42", As2805TillUtils.packPosConditionCode(transRec));
    }
}
