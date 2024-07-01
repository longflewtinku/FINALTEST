package com.linkly.libengine.engine.reporting;

import static com.linkly.libengine.engine.EngineManager.TransType.SALE;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.ACC_TYPE_CHEQUE;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.MessageStatus.FINALISED;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.ReversalState.REVERSIBLE;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

import com.linkly.libengine.config.PayCfg;
import com.linkly.libengine.dependencies.Dependencies;
import com.linkly.libengine.dependencies.IDependency;
import com.linkly.libengine.engine.Engine;
import com.linkly.libengine.engine.EngineManager;
import com.linkly.libengine.engine.customers.ICustomer;
import com.linkly.libengine.engine.transactions.TransRec;
import com.linkly.libengine.engine.transactions.TransRecDao;
import com.linkly.libengine.engine.transactions.TransRecManager;
import com.linkly.libengine.engine.transactions.properties.TCard;
import com.linkly.libengine.engine.transactions.properties.TProtocol;
import com.linkly.libpositive.wrappers.PositiveTransResult;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

public class DailyBatchTest {

    @Mock
    TransRecManager transRecManager;

    @Mock
    ReconciliationManager reconciliationManager;

    @Mock
    ICustomer customer;

    @Mock
    TransRecDao transRecDao;

    @Mock
    IDependency dependency;

    @Mock
    PayCfg config;

    DailyBatch dailyBatch;

    Reconciliation rec;

    @Before
    public void setUp() {
        openMocks(this);

        dailyBatch = new DailyBatch();
        when(transRecManager.getTransRecDao()).thenReturn(transRecDao);
        when(config.isSignatureSupported()).thenReturn(false);
        when(dependency.getPayCfg()).thenReturn(config);
    }

    @Test
    public void shouldProcessDailyBatchAsVisaEmv() {
        List<TransRec> transRecs = new ArrayList<>();
        transRecs.add(getMockedTrans(SALE, "A000000003", "E"));
        when(transRecDao.findBySummedOrReced(false)).thenReturn(transRecs);

        rec = generateDailyBatch(rec);

        assertEquals(1, rec.getPreviousSchemeTotals().get(TCard.CardIssuer.VISA.getDisplayName()).purchaseCount);
    }

    @Test
    public void shouldProcessDailyBatchAsEftposEmv() {
        List<TransRec> transRecs = new ArrayList<>();
        transRecs.add(getMockedTrans(SALE, "A000000384", "M"));
        when(transRecDao.findBySummedOrReced(false)).thenReturn(transRecs);

        rec = generateDailyBatch(rec);

        assertEquals(1, rec.getPreviousSchemeTotals().get(TCard.CardIssuer.EFTPOS.getDisplayName()).purchaseCount);
    }

    @Test
    public void shouldProcessDailyBatchAsEftposMsr() {
        List<TransRec> transRecs = new ArrayList<>();
        transRecs.add(getMockedTrans(SALE, null, "E"));
        when(transRecDao.findBySummedOrReced(false)).thenReturn(transRecs);

        rec = generateDailyBatch(rec);

        assertEquals(1, rec.getPreviousSchemeTotals().get(TCard.CardIssuer.EFTPOS.getDisplayName()).purchaseCount);
    }

    @Test
    public void shouldProcessDailyBatchAsVisaMsr() {
        List<TransRec> transRecs = new ArrayList<>();
        transRecs.add(getMockedTrans(SALE, null, "V"));
        when(transRecDao.findBySummedOrReced(false)).thenReturn(transRecs);

        rec = generateDailyBatch(rec);

        assertEquals(1, rec.getPreviousSchemeTotals().get(TCard.CardIssuer.VISA.getDisplayName()).purchaseCount);
    }

    @Test
    public void shouldProcessDailyBatchAsUnkownIfCardDataIsMissing() {
        List<TransRec> transRecs = new ArrayList<>();
        TransRec transAsNullCard = getMockedTrans(SALE, null, "V");
        transAsNullCard.setCard(null);
        transRecs.add(transAsNullCard);
        when(transRecDao.findBySummedOrReced(false)).thenReturn(transRecs);

        rec = generateDailyBatch(rec);

        assertEquals(1, rec.getPreviousSchemeTotals().get(TCard.CardIssuer.UNKNOWN.getDisplayName()).purchaseCount);
    }

    @Test
    public void shouldProcessDailyBatchForAllSchemesAsMsr() {
        List<TransRec> transRecs = new ArrayList<>();

        transRecs.add(getMockedTrans(SALE, null, "V"));
        transRecs.addAll(getMockedTransList(2, SALE, null, "M"));
        transRecs.addAll(getMockedTransList(3, SALE, null, "E"));
        transRecs.addAll(getMockedTransList(4, SALE, null, "A"));
        transRecs.addAll(getMockedTransList(5, SALE, null, "J"));
        transRecs.addAll(getMockedTransList(6, SALE, null, "U"));
        transRecs.addAll(getMockedTransList(7, SALE, null, "D"));

        when(transRecDao.findBySummedOrReced(false)).thenReturn(transRecs);

        rec = generateDailyBatch(rec);

        assertEquals(1, rec.getPreviousSchemeTotals().get(TCard.CardIssuer.VISA.getDisplayName()).purchaseCount);
        assertEquals(2, rec.getPreviousSchemeTotals().get(TCard.CardIssuer.MASTERCARD.getDisplayName()).purchaseCount);
        assertEquals(3, rec.getPreviousSchemeTotals().get(TCard.CardIssuer.EFTPOS.getDisplayName()).purchaseCount);
        assertEquals(4, rec.getPreviousSchemeTotals().get(TCard.CardIssuer.AMEX.getDisplayName()).purchaseCount);
        assertEquals(5, rec.getPreviousSchemeTotals().get(TCard.CardIssuer.JCB.getDisplayName()).purchaseCount);
        assertEquals(6, rec.getPreviousSchemeTotals().get(TCard.CardIssuer.UNIONPAY.getDisplayName()).purchaseCount);
        assertEquals(7, rec.getPreviousSchemeTotals().get(TCard.CardIssuer.DINERS.getDisplayName()).purchaseCount);
    }

    @Test
    public void shouldProcessDailyBatchForAllSchemesAsEmv() {
        List<TransRec> transRecs = new ArrayList<>();
        transRecs.add(getMockedTrans(SALE, "A000000003", "C"));
        transRecs.addAll(getMockedTransList(2, SALE, "A000000004", "C"));
        transRecs.addAll(getMockedTransList(3, SALE, "A000000384", "C"));
        transRecs.addAll(getMockedTransList(4, SALE, "A000000025", "C"));
        transRecs.addAll(getMockedTransList(5, SALE, "A000000065", "C"));
        transRecs.addAll(getMockedTransList(6, SALE, "A000000333", "C"));
        transRecs.addAll(getMockedTransList(7, SALE, "A000000152", "C"));

        when(transRecDao.findBySummedOrReced(false)).thenReturn(transRecs);

        rec = generateDailyBatch(rec);

        assertEquals(1, rec.getPreviousSchemeTotals().get(TCard.CardIssuer.VISA.getDisplayName()).purchaseCount);
        assertEquals(2, rec.getPreviousSchemeTotals().get(TCard.CardIssuer.MASTERCARD.getDisplayName()).purchaseCount);
        assertEquals(3, rec.getPreviousSchemeTotals().get(TCard.CardIssuer.EFTPOS.getDisplayName()).purchaseCount);
        assertEquals(4, rec.getPreviousSchemeTotals().get(TCard.CardIssuer.AMEX.getDisplayName()).purchaseCount);
        assertEquals(5, rec.getPreviousSchemeTotals().get(TCard.CardIssuer.JCB.getDisplayName()).purchaseCount);
        assertEquals(6, rec.getPreviousSchemeTotals().get(TCard.CardIssuer.UNIONPAY.getDisplayName()).purchaseCount);
        assertEquals(7, rec.getPreviousSchemeTotals().get(TCard.CardIssuer.DINERS.getDisplayName()).purchaseCount);
    }

    @Test
    public void shouldProcessDailyBatchForAllSchemes() {
        List<TransRec> transRecs = new ArrayList<>();
        transRecs.add(getMockedTrans(SALE, "A000000003", "C"));
        transRecs.addAll(getMockedTransList(2, SALE, "A000000004", "C"));
        transRecs.addAll(getMockedTransList(3, SALE, "A000000384", "C"));
        transRecs.addAll(getMockedTransList(4, SALE, "A000000025", "C"));
        transRecs.addAll(getMockedTransList(5, SALE, "A000000065", "C"));
        transRecs.addAll(getMockedTransList(6, SALE, "A000000333", "C"));
        transRecs.addAll(getMockedTransList(7, SALE, "A000000152", "C"));
        transRecs.addAll(getMockedTransList(10, SALE, null, "V"));
        transRecs.addAll(getMockedTransList(20, SALE, null, "M"));
        transRecs.addAll(getMockedTransList(30, SALE, null, "E"));
        transRecs.addAll(getMockedTransList(40, SALE, null, "A"));
        transRecs.addAll(getMockedTransList(50, SALE, null, "J"));
        transRecs.addAll(getMockedTransList(60, SALE, null, "U"));
        transRecs.addAll(getMockedTransList(70, SALE, null, "D"));

        when(transRecDao.findBySummedOrReced(false)).thenReturn(transRecs);

        rec = generateDailyBatch(rec);

        assertEquals(11, rec.getPreviousSchemeTotals().get(TCard.CardIssuer.VISA.getDisplayName()).purchaseCount);
        assertEquals(22, rec.getPreviousSchemeTotals().get(TCard.CardIssuer.MASTERCARD.getDisplayName()).purchaseCount);
        assertEquals(33, rec.getPreviousSchemeTotals().get(TCard.CardIssuer.EFTPOS.getDisplayName()).purchaseCount);
        assertEquals(44, rec.getPreviousSchemeTotals().get(TCard.CardIssuer.AMEX.getDisplayName()).purchaseCount);
        assertEquals(55, rec.getPreviousSchemeTotals().get(TCard.CardIssuer.JCB.getDisplayName()).purchaseCount);
        assertEquals(66, rec.getPreviousSchemeTotals().get(TCard.CardIssuer.UNIONPAY.getDisplayName()).purchaseCount);
        assertEquals(77, rec.getPreviousSchemeTotals().get(TCard.CardIssuer.DINERS.getDisplayName()).purchaseCount);
    }

    private List<TransRec> getMockedTransList(Integer count, EngineManager.TransType ttype, String aid, String psi) {
        List<TransRec> transRecs = new ArrayList<>();
        while (count > 0) {
            transRecs.add(getMockedTrans(ttype, aid, psi));
            count--;
        }

        return transRecs;
    }

    private Reconciliation generateDailyBatch(Reconciliation rec) {
        try (MockedStatic<TransRecManager> mockedStatic = Mockito.mockStatic(TransRecManager.class);
             MockedStatic<ReconciliationManager> mockedStaticReconciliationManager = Mockito.mockStatic(ReconciliationManager.class)) {
            mockedStatic.when(() -> TransRecManager.getInstance()).thenReturn(transRecManager);
            mockedStaticReconciliationManager.when(() -> ReconciliationManager.getInstance()).thenReturn(reconciliationManager);
            when(dependency.getCustomer()).thenReturn(customer);
            rec = dailyBatch.generateDailyBatch(false, dependency);
        }
        return rec;
    }

    private TransRec getMockedTrans(EngineManager.TransType ttype, String aid, String psi) {
        TransRec tran = new TransRec();

        tran.setApproved(true);
        tran.setTransType(ttype);
        tran.setSummedOrReced(false);
        tran.setCancelled(false);

        tran.setEmvTagsString("ABCDEF0123456789");
        tran.setCtlsTagsString("0123456789ABCDEF");
        tran.getAudit().setReceiptNumber(10000);
        tran.getAudit().setUti("UTI" + (1000));
        tran.getAudit().setUserId("USERINDEX" + 1);
        tran.getProtocol().setReversalState(REVERSIBLE);

        /*Protocol Fields*/
        tran.getProtocol().setCanAuthOffline(true);

        TProtocol.MessageStatus msgStatus = TProtocol.MessageStatus.values()[FINALISED.ordinal()];
        tran.getProtocol().setMessageStatus(msgStatus);
        tran.getProtocol().setHostResult(TProtocol.HostResult.AUTHORISED);
        tran.getProtocol().setAuthMethod(TProtocol.AuthMethod.ONLINE_AUTHORISED);
        tran.getProtocol().setStan(1984);
        tran.getProtocol().setBatchNumber(17);
        tran.getProtocol().setAccountType(ACC_TYPE_CHEQUE);
        tran.getProtocol().setAuthCode("AUTHCODE");
        tran.getProtocol().setRRN("RRNDBTEST");

        /*TCard */
        tran.getCard().setCardType(TCard.CardType.EMV);
        tran.getCard().setCvmType(TCard.CvmType.ENCIPHERED_ONLINE_PIN);
        tran.getCard().setCaptureMethod(TCard.CaptureMethod.ICC);
        tran.getCard().setCardholderPresent(true);
        tran.getCard().setPan("9876543211234560");
        tran.getCard().setAid(aid);

        tran.getCard().setPsi(psi);
        tran.getCard().setExpiry("0484");
        tran.getCard().setServiceCode("SERV1");
        tran.getCard().setCvv("CVV");
        tran.getCard().setCardName("CARD NAME");
        tran.getCard().setCardHolderName("Jim huf");

        /*TAmount*/
        tran.getAmounts().setAmount(1234);
        tran.getAmounts().setCashbackAmount(4567);
        tran.getAmounts().setTip(1900);
        tran.getAmounts().setAmountUserEntered("100");
        tran.getAmounts().setCurrency("840");

        /*TSec*/
        //tran.getSecurity().setKsn(new byte[]{1, 2, 4, 5, 6});
        tran.getSecurity().setEncPan("ENCPAN");
        tran.getSecurity().setEncTrack2("ENCTRACK2");

        /*TAudit*/
        tran.getAudit().setTerminalId("1293132");
        tran.getAudit().setMerchantId("MERID");
        tran.getAudit().setReference("TEST");
        tran.getAudit().setVirtualTid("VTID");
        tran.getAudit().setVirtualMid("VMID");
        tran.getAudit().setCountryCode("820");
        tran.getAudit().setTransDateTime(System.currentTimeMillis());

        // create dummy receipt data
        ArrayList<String> linesMerchant = new ArrayList<>();
        linesMerchant.add("merch line 1");
        linesMerchant.add("merch line 2");
        linesMerchant.add("merch line 3");
        tran.getReceipts().add(new PositiveTransResult.Receipt("M", linesMerchant));

        ArrayList<String> linesCustomer = new ArrayList<>();
        linesCustomer.add("cust line 1");
        linesCustomer.add("cust line 2");
        linesCustomer.add("cust line 3");
        linesCustomer.add("blah blah line 4");
        tran.getReceipts().add(new PositiveTransResult.Receipt("C", linesCustomer));

        return tran;
    }
}