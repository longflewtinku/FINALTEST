package com.positive.database;


import static com.linkly.libengine.engine.EngineManager.TransType.GRATUITY;
import static com.linkly.libengine.engine.EngineManager.TransType.REFUND;
import static com.linkly.libengine.engine.EngineManager.TransType.SALE;
import static com.linkly.libengine.engine.reporting.ReconciliationManager.reconciliationDao;
import static com.linkly.libengine.engine.reporting.TotalsManager.totalsDao;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.ACC_TYPE_CHEQUE;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.MessageStatus.ADVICE_QUEUED;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.MessageStatus.AUTH_SENT;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.MessageStatus.FINALISED;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.MessageStatus.POLL_QUEUED;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.MessageStatus.REC_QUEUED;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.MessageStatus.REVERSAL_QUEUED;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.ReversalState.NOT_REVERSIBLE;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.ReversalState.REVERSIBLE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.runners.MethodSorters.NAME_ASCENDING;

import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.platform.app.InstrumentationRegistry;

import com.linkly.libengine.config.Config;
import com.linkly.libengine.dependencies.Dependencies;
import com.linkly.libengine.engine.Engine;
import com.linkly.libengine.engine.EngineManager;
import com.linkly.libengine.engine.reporting.Reconciliation;
import com.linkly.libengine.engine.reporting.Totals;
import com.linkly.libengine.engine.transactions.TransRec;
import com.linkly.libengine.engine.transactions.TransRecManager;
import com.linkly.libengine.engine.transactions.properties.TCard;
import com.linkly.libengine.engine.transactions.properties.TProtocol;
import com.linkly.libengine.status.StatusReport;
import com.linkly.libmal.MalFactory;
import com.linkly.libpositive.messages.Messages;
import com.linkly.libpositive.wrappers.PositiveTransResult;
import com.linkly.libsecapp.IP2PEncrypt;
import com.linkly.libsecapp.P2PLib;

import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import timber.log.Timber;

@Ignore("Ignoring as Integration Test")
@RunWith(AndroidJUnit4.class)
@LargeTest
@FixMethodOrder(NAME_ASCENDING)
public class DatabaseTest {

    //    private static final int NUM_OF_NEW_RECS = 53; // if less than 26, there's not enough for trans type enums and tests fail
    private static final int NUM_OF_NEW_RECS = 500; // if less than 26, there's not enough for trans type enums and tests fail
    private IP2PEncrypt p2pEncrypt = null;
    static long[] uidArrayNew = new long[NUM_OF_NEW_RECS];
    static long[] uidArrayNewUnshuffled = new long[NUM_OF_NEW_RECS];
    static List<TransRec> allTransNew = null;

    @Before
    public void setUp() throws Exception {
        // set up a minimal set of dependencies to run the tests
        Dependencies d = new Dependencies();
        d.setConfig(Config.getInstance());
        d.setStatusReporter(StatusReport.getInstance());
        d.setMessages(Messages.getInstance());
        Engine.init(d, InstrumentationRegistry.getInstrumentation().getTargetContext(), MalFactory.getInstance());
    }

    @After
    public void tearDown() throws Exception {
    }

    public void newTestDeleteDataBase() throws Exception {
        /*
            Test

         */
        int i;

        assertNotEquals(null, allTransNew);
        assertEquals(NUM_OF_NEW_RECS, allTransNew.size());

        for (i = 0; i < allTransNew.size(); i++) {
            TransRec tran = allTransNew.get(i);
            TransRecManager.getInstance().getTransRecDao().delete(tran);
        }

        allTransNew = TransRecManager.getInstance().getTransRecDao().findAll();
        assertEquals(0, allTransNew.size());

        Timber.i("Test Delete Finished ");
    }

    public void newTestUpdateDataBase() throws Exception {
        /*
            Test

         */
        int i;

        assertNotEquals(null, allTransNew);
        Timber.i("updating " + allTransNew.size() + " records");

        assertEquals(NUM_OF_NEW_RECS, allTransNew.size());

        for (i = 0; i < allTransNew.size(); i++) {
            TransRec tran = allTransNew.get(i);
            assertNotEquals(tran.getAudit().getReference().contains("MOD"), true);
            tran.getAudit().setReference(tran.getAudit().getReference() + "_MOD");
            TransRecManager.getInstance().getTransRecDao().update(tran);

            /*Read it Back from the Database*/
            TransRec updated = TransRecManager.getInstance().getTransRecDao().getByUid(tran.uid);
            assertNotEquals(null, updated);
            assertEquals(true, tran.getAudit().getReference().contains("MOD"));

        }

        Timber.i("Test Update Finished");
    }

    public void newTestReadLatestDataBase() throws Exception {
        TransRec tran;

        // read latest transaction back
        tran = TransRecManager.getInstance().getTransRecDao().getLatest();
        assertNotEquals(tran, null);
        Timber.i("Test Read Finished");
    }

    public void newTestAddToDataBase() throws Exception {
        int i;

        // delete all records first
        TransRecManager.getInstance().getTransRecDao().deleteAll();
        uidArrayNew = new long[NUM_OF_NEW_RECS];
        uidArrayNewUnshuffled = new long[NUM_OF_NEW_RECS];

        for (i = 0; i < NUM_OF_NEW_RECS; i++) {
            TransRec tran = newFullyPopulatedTrans(i);
            assertNotEquals(tran, null);
            tran.setFinalised(false);
            // insert and add uid to array for searching later
            uidArrayNew[i] = TransRecManager.getInstance().getTransRecDao().insert(tran);
            uidArrayNewUnshuffled[i] = uidArrayNew[i];
        }

        // randomise the array for searching
        shuffleArray(uidArrayNew);

        Timber.i("Test Save Finished ");

    }

    public void newTestCheckNumRecords() {
        allTransNew = TransRecManager.getInstance().getTransRecDao().findAll();
        assertNotEquals(null, allTransNew);

        assertEquals(NUM_OF_NEW_RECS, allTransNew.size());
    }

    public TransRec newFullyPopulatedTrans(int index) {
        TransRec tran = new TransRec();

        tran.setApproved(true);
        EngineManager.TransType ttype = EngineManager.TransType.values()[index % GRATUITY.ordinal()];
        tran.setTransType(ttype);
        tran.setSummedOrReced(index % 2 == 0 ? false : true);
        tran.setCancelled(index % 2 == 0 ? false : true);

        tran.setEmvTagsString("ABCDEF0123456789");
        tran.setCtlsTagsString("0123456789ABCDEF");
        tran.getAudit().setReceiptNumber(index + 10000);
        tran.getAudit().setUti("UTI" + (index + 1000));
        tran.getAudit().setUserId("USERINDEX" + +(index % 10));
        tran.getProtocol().setReversalState(index % 2 == 0 ? NOT_REVERSIBLE : REVERSIBLE);

        /*Protocol Fields*/
        tran.getProtocol().setCanAuthOffline(true);

        TProtocol.MessageStatus msgStatus = TProtocol.MessageStatus.values()[index % POLL_QUEUED.ordinal()];
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

        // TODO:
//        tran.getCard().setMaskedPan(tran.getMaskedPan(TransRec.MaskType.REPORT_MASK));
//        tran.getCard().updateTrack2("12311563816=1231313123", tran);
        tran.getCard().setPsi("PSI");
        tran.getCard().setExpiry("0484");
        tran.getCard().setServiceCode("SERV1");
        tran.getCard().setCvv("CVV");
        tran.getCard().setCardName("CARD NAME");
        tran.getCard().setCardHolderName("Jim huf");

        /*TAmount*/
        tran.getAmounts().setAmount(1234);
        tran.getAmounts().setCashbackAmount(4567);
        tran.getAmounts().setTip(1900);
        tran.getAmounts().setAmountUserEntered("100" + index);
        tran.getAmounts().setCurrency("840");

        /*TSec*/
        //tran.getSecurity().setKsn(new byte[]{1, 2, 4, 5, 6});
        tran.getSecurity().setEncPan("ENCPAN");
        tran.getSecurity().setEncTrack2("ENCTRACK2");

        /*TAudit*/
        tran.getAudit().setTerminalId("1293132");
        tran.getAudit().setMerchantId("MERID");
        tran.getAudit().setReference("TEST" + index);
        tran.getAudit().setVirtualTid("VTID");
        tran.getAudit().setVirtualMid("VMID");
        tran.getAudit().setCountryCode("820");
        tran.getAudit().setTransDateTime(System.currentTimeMillis());

        // create dummy receipt data
        ArrayList<String> linesMerchant = new ArrayList<>();
        linesMerchant.add( "merch line 1" );
        linesMerchant.add( "merch line 2" );
        linesMerchant.add( "merch line 3" );
        tran.getReceipts().add(new PositiveTransResult.Receipt( "M", linesMerchant ));

        ArrayList<String> linesCustomer = new ArrayList<>();
        linesCustomer.add( "cust line 1" );
        linesCustomer.add( "cust line 2" );
        linesCustomer.add( "cust line 3" );
        linesCustomer.add( "blah blah line 4" );
        tran.getReceipts().add(new PositiveTransResult.Receipt( "C", linesCustomer ));

        return tran;
    }

    public void newReadWriteSingleRecord() {
        TransRec tran = newFullyPopulatedTrans(0);

        TransRecManager.getInstance().getTransRecDao().insert(tran);

        Timber.i("Saved Find ");
        TransRec readBack = TransRecManager.getInstance().getTransRecDao().getLatest();

//        readBack.debug();

        //Protocol Checks
        assertEquals(tran.getEmvTagsString(), readBack.getEmvTagsString());
        assertEquals(tran.getCtlsTagsString(), readBack.getCtlsTagsString());
        assertEquals(tran.getProtocol().isCanAuthOffline(), readBack.getProtocol().isCanAuthOffline());
        assertEquals(tran.getProtocol().getMessageStatus(), readBack.getProtocol().getMessageStatus());
        assertEquals(tran.getAudit().getReceiptNumber(), readBack.getAudit().getReceiptNumber());
        assertEquals(tran.getAudit().getUti(), readBack.getAudit().getUti());
        assertEquals(tran.getProtocol().getReversalState(), readBack.getProtocol().getReversalState());
        assertEquals(tran.getAudit().getUserId(), readBack.getAudit().getUserId());

        assertEquals(tran.getProtocol().getHostResult(), readBack.getProtocol().getHostResult());
        assertEquals(tran.getProtocol().getAuthMethod(), readBack.getProtocol().getAuthMethod());
        assertEquals(tran.getProtocol().getStan(), readBack.getProtocol().getStan());
        assertEquals(tran.getProtocol().getBatchNumber(), readBack.getProtocol().getBatchNumber());
        assertEquals(tran.getProtocol().getAccountType(), readBack.getProtocol().getAccountType());
        assertEquals(tran.getProtocol().getAuthCode(), readBack.getProtocol().getAuthCode());
        assertEquals(tran.getProtocol().getRRN(), readBack.getProtocol().getRRN());

        //TCard
        assertEquals(tran.getCard().getCardType(), readBack.getCard().getCardType());
        assertEquals(tran.getCard().getCvmType(), readBack.getCard().getCvmType());
        assertEquals(tran.getCard().getCaptureMethod(), readBack.getCard().getCaptureMethod());
        assertEquals(tran.getCard().isCardholderPresent(), readBack.getCard().isCardholderPresent());
//            assertEquals(tran.getCard().getPan(), readBack.getCard().getPan());
//            assertEquals(tran.getCard().getTrack2(), readBack.getCard().getTrack2());
        assertEquals(tran.getCard().getPsi(), readBack.getCard().getPsi());
        assertEquals(tran.getCard().getExpiry(), readBack.getCard().getExpiry());
        assertEquals(tran.getCard().getServiceCode(), readBack.getCard().getServiceCode());
        assertEquals(tran.getCard().getCvv(), readBack.getCard().getCvv());
        assertEquals(tran.getCard().getCardName(), readBack.getCard().getCardName());
        assertEquals(tran.getCard().getCardHolderName(), readBack.getCard().getCardHolderName());

        //TAmounts
        assertEquals(tran.getAmounts().getAmount(), readBack.getAmounts().getAmount());
        assertEquals(tran.getAmounts().getCashbackAmount(), readBack.getAmounts().getCashbackAmount());
        assertEquals(tran.getAmounts().getTip(), readBack.getAmounts().getTip());
        assertEquals(tran.getAmounts().getAmountUserEntered(), readBack.getAmounts().getAmountUserEntered());
        assertEquals(tran.getAmounts().getCurrency(), readBack.getAmounts().getCurrency());


        //TSec
        assertEquals(tran.getSecurity().getEncPan(), readBack.getSecurity().getEncPan());
        assertEquals(tran.getSecurity().getEncTrack2(), readBack.getSecurity().getEncTrack2());

        //TAudit
        assertEquals(tran.getAudit().getTerminalId(), readBack.getAudit().getTerminalId());
        assertEquals(tran.getAudit().getMerchantId(), readBack.getAudit().getMerchantId());
        assertEquals(tran.getAudit().getReference(), readBack.getAudit().getReference());
        assertEquals(tran.getAudit().getVirtualTid(), readBack.getAudit().getVirtualTid());
        assertEquals(tran.getAudit().getCountryCode(), readBack.getAudit().getCountryCode());
        assertEquals(tran.getAudit().getTransDateTime(), readBack.getAudit().getTransDateTime());

        assertEquals( 2, tran.getReceipts().size() );
        assertEquals( 3, tran.getReceipts().get(0).getLines().size() );
        assertEquals( "M", tran.getReceipts().get(0).getType() );
        assertEquals( 4, tran.getReceipts().get(1).getLines().size() );
        assertEquals( "C", tran.getReceipts().get(1).getType() );
    }

    @Test
    public void testTotalsSingleRecord() {

        Totals totals = new Totals("VISA", SALE, false, 123456, 1234, 566666, 444, 123123, 200, 1234, 1266);

        totalsDao.insert(totals);

        Totals testData = totalsDao.getLatest();
        assertNotEquals(null, testData);

        assertEquals("VISA", testData.getCardName());
        assertEquals(SALE, testData.getTransType());
        assertEquals(123456, testData.getNetAmount());
        assertEquals(1234, testData.getNetCount());
        assertEquals(566666, testData.getDebitAmount());
        assertEquals(444, testData.getDebitCount());
        assertEquals(123123, testData.getCreditAmount());
        assertEquals(200, testData.getCreditCount());
        assertEquals(1234, testData.getFirstTranID());
        assertEquals(1266, testData.getLastTranID());

    }

    @Test
    public void testTotalsMultiRecordsGroupByTransType() {
        Totals totalsVisaSale = new Totals();
        totalsDao.deleteAll();

        totalsVisaSale = new Totals("VISA", SALE, false, 123456, 1234, 566666, 444, 123123, 200, 1234, 1266);
        totalsDao.insert(totalsVisaSale);
        Totals totalsVisaRefund = new Totals("VISA", REFUND, false, 123456, 1234, 566666, 444, 123123, 200, 1234, 1266);
        totalsDao.insert(totalsVisaRefund);
        Totals totalsMc = new Totals("MASTERCARD", SALE, false, 123456, 1234, 566666, 444, 123123, 200, 1234, 1266);
        totalsDao.insert(totalsMc);
        Totals totalsAmex = new Totals("AMEX", SALE, false, 123456, 1234, 566666, 444, 123123, 200, 1234, 1266);
        totalsDao.insert(totalsAmex);

        List<Totals> result = totalsDao.groupByTransType();
        assertNotEquals(null, result);
        assertEquals(2, result.size());

        assertEquals(SALE, result.get(0).getTransType());
        assertEquals(1332, result.get(0).getDebitCount());
        assertEquals(REFUND, result.get(1).getTransType());
        assertEquals(444, result.get(1).getDebitCount());

        totalsDao.deleteAll();
    }

    @Test
    public void testTotalsMultiRecordsGroupByCardName() {
        Totals totalsVisaSale = new Totals();
        totalsDao.deleteAll();

        totalsVisaSale = new Totals("VISA", SALE, false, 123456, 1234, 566666, 444, 123123, 200, 1234, 1266);
        totalsDao.insert(totalsVisaSale);
        Totals totalsVisaRefund = new Totals("VISA", REFUND, false, 123456, 1234, 566666, 444, 123123, 200, 1234, 1266);
        totalsDao.insert(totalsVisaRefund);
        Totals totalsMc = new Totals("MASTERCARD", SALE, false, 123456, 1234, 566666, 333, 123123, 200, 1234, 1266);
        totalsDao.insert(totalsMc);
        Totals totalsAmex = new Totals("AMEX", SALE, false, 123456, 1234, 566666, 111, 123123, 200, 1234, 1266);
        totalsDao.insert(totalsAmex);

        List<Totals> result = totalsDao.groupByCardName();
        assertNotEquals(null, result);
        assertEquals(3, result.size());

        assertEquals("AMEX", result.get(0).getCardName());
        assertEquals("MASTERCARD", result.get(1).getCardName());
        assertEquals("VISA", result.get(2).getCardName());

        assertEquals(111, result.get(0).getDebitCount());
        assertEquals(333, result.get(1).getDebitCount());
        assertEquals(888, result.get(2).getDebitCount());

        totalsDao.deleteAll();
    }

    @Test
    public void testRec() {
        Reconciliation rec = new Reconciliation();
        rec.getSale().amount = 100;
        rec.getSale().count = 2;
        rec.getVas().amount = 123456;
        rec.getVas().count = 123;
        rec.getRefund().amount = 8712;
        rec.getRefund().count = 333;
        rec.getCash().amount = 30000;
        rec.getCash().count = 30;
        rec.getTips().amount = 78900;
        rec.getTips().count = 74;
        rec.getCashback().amount = 900;
        rec.getCashback().count = 9;
        rec.getPreauth().amount = 700;
        rec.getPreauth().count = 7;
        rec.getCompletion().amount = 652;
        rec.getCompletion().count = 6;
        rec.getDeposit().amount = 123456789;
        rec.getDeposit().count = 90874;
        rec.setOfflineTotalAmount(123456);
        rec.getReconciliationFigures().setCreditsAmount(123456);
        rec.getReconciliationFigures().setCreditsNumber(123);
        rec.getPrevReconciliationFigures().setCreditsAmount(55555);
        rec.getPrevReconciliationFigures().setCreditsNumber(555);
        rec.getCurReconciliationFigures().setCreditsAmount(666666);
        rec.getCurReconciliationFigures().setCreditsNumber(66);

        reconciliationDao.insert(rec);

        Reconciliation r = reconciliationDao.getLatest();
        assertNotEquals(null, r);

        assertEquals(100, r.getSale().amount);
        assertEquals(2, r.getSale().count);
        assertEquals(123456, r.getVas().amount);
        assertEquals(123, r.getVas().count);
        assertEquals(8712, r.getRefund().amount);
        assertEquals(333, r.getRefund().count);
        assertEquals(30000, r.getCash().amount);
        assertEquals(30, r.getCash().count);
        assertEquals(78900, r.getTips().amount);
        assertEquals(74, r.getTips().count);
        assertEquals(900, r.getCashback().amount);
        assertEquals(9, r.getCashback().count);
        assertEquals(700, r.getPreauth().amount);
        assertEquals(7, r.getPreauth().count);
        assertEquals(652, r.getCompletion().amount);
        assertEquals(6, r.getCompletion().count);
        assertEquals(123456789, r.getDeposit().amount);
        assertEquals(90874, r.getDeposit().count);

        assertEquals(123456, r.getReconciliationFigures().getCreditsAmount());
        assertEquals(123, r.getReconciliationFigures().getCreditsNumber());

        assertEquals(55555, r.getPrevReconciliationFigures().getCreditsAmount());
        assertEquals(555, r.getPrevReconciliationFigures().getCreditsNumber());

        assertEquals(666666, r.getCurReconciliationFigures().getCreditsAmount());
        assertEquals(66, r.getCurReconciliationFigures().getCreditsNumber());
    }


    @Test
    public void aaa_initialise() throws Exception {
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        P2PLib p2pInstance = P2PLib.getInstance();
        assertNotEquals(p2pInstance, null);

        p2pInstance.Init(appContext, null);

        p2pEncrypt = p2pInstance.getIP2PEncrypt();
        p2pEncrypt.initialise(true);
    }


    // Implementing Fisher–Yates shuffle - code taken from stack overflow article https://stackoverflow.com/questions/1519736/random-shuffling-of-an-array
    public void shuffleArray(long[] ar) {
        // If running on Java 6 or older, use `new Random()` on RHS here
        Random rnd = ThreadLocalRandom.current();
        for (int i = ar.length - 1; i > 0; i--) {
            int index = rnd.nextInt(i + 1);
            // Simple swap
            long a = ar[index];
            ar[index] = ar[i];
            ar[i] = a;
        }
    }

    @Test
    public void test_a_single_record_new() throws Exception {
        newReadWriteSingleRecord();
    }

    @Test
    public void test_b_write_lots_new() throws Exception {
        newTestAddToDataBase();
    }

    @Test
    public void test_c_read_all_records_new() throws Exception {
        newTestCheckNumRecords();
    }

    @Test
    public void test_ca_random_access_new() throws Exception {
        for (long idx : uidArrayNew) {
            // read record with uid = idx
            TransRec recRead = TransRecManager.getInstance().getTransRecDao().getByUid((int) idx);
            assertNotEquals(null, recRead);
            assertEquals(idx, recRead.uid);
        }
    }

    @Test
    public void test_cb_select_latest_by_trans_type_new() throws Exception {
        for (int ttype = 0; ttype < GRATUITY.ordinal(); ttype++) {
            // read record with trans type = ttype
            TransRec rec = TransRecManager.getInstance().getTransRecDao().getLatestByTransType(EngineManager.TransType.values()[ttype]);
            assertNotEquals(null, rec);
            assertEquals(EngineManager.TransType.values()[ttype], rec.getTransType());
        }
    }

    @Test
    public void test_cc_select_summed_or_recced_new() throws Exception {
        List<TransRec> allTrans = TransRecManager.getInstance().getTransRecDao().findBySummedOrReced(false);
        assertNotEquals(null, allTrans);
        assertNotEquals(0, allTrans.size());

        allTrans = TransRecManager.getInstance().getTransRecDao().findBySummedOrReced(true);
        assertNotEquals(null, allTrans);
        assertNotEquals(0, allTrans.size());
    }

    @Test
    public void test_cd_select_cancelled_new() throws Exception {
        List<TransRec> allTrans = TransRecManager.getInstance().getTransRecDao().findByCancelled(false);
        assertNotEquals(null, allTrans);
        assertNotEquals(0, allTrans.size());

        allTrans = TransRecManager.getInstance().getTransRecDao().findByCancelled(true);
        assertNotEquals(null, allTrans);
        assertNotEquals(0, allTrans.size());
    }

    @Test
    public void test_ce_select_by_message_status_new() {
        // find all not finalised records
        List<TransRec> allTrans = TransRecManager.getInstance().getTransRecDao().findAllNotMessageStatus(FINALISED);
        assertNotEquals(null, allTrans);
        assertNotEquals(0, allTrans.size());
    }

    @Test
    public void test_cf_select_by_receipt_number_new() {
        // select index from middle
        int recNumber = 10000 + (NUM_OF_NEW_RECS / 2);
        TransRec trans = TransRecManager.getInstance().getTransRecDao().getByReceiptNumber(recNumber);
        assertNotEquals(null, trans);
    }

    @Test
    public void test_cg_select_by_uti_new() {
        TransRec trans = TransRecManager.getInstance().getTransRecDao().getByUti("UTI1005");
        assertNotEquals(null, trans);
    }

    @Test
    public void test_ch_select_by_rev_state_new() {
        TransRec trans = TransRecManager.getInstance().getTransRecDao().getLatestByReversalState(REVERSIBLE, false);
        assertNotEquals(null, trans);
    }

    @Test
    public void test_ci_select_by_user_id_new() {
        long count = TransRecManager.getInstance().getTransRecDao().countTransByUser("USERINDEX5");
        assertNotEquals(0, count);

        List<TransRec> userTrans = TransRecManager.getInstance().getTransRecDao().findByUser("USERINDEX5");
        assertNotEquals(null, userTrans);
        assertNotEquals(0, userTrans.size());

        long value = 0;
        for (TransRec tran : userTrans) {
            value += tran.getAmounts().getTotalAmount();
        }

        assertNotEquals(0, value);
//        assertEquals( 38505, value ); // value is only correct if NUM_OF_NEW_RECS is 53 - revise or ignore this test if increasing NUM_OF_NEW_RECS

    }

    @Test
    public void test_cj_count_advices_new() {
        ArrayList<TProtocol.MessageStatus> statusList = new ArrayList<>();
        statusList.add(REC_QUEUED);
        statusList.add(ADVICE_QUEUED);
        statusList.add(AUTH_SENT);
        statusList.add(REVERSAL_QUEUED);
        long count = TransRec.countTransByMessageStatus(statusList);
        assertNotEquals(0, count);
//        assertEquals( 27, count ); // value is only correct if NUM_OF_NEW_RECS is 53 - revise or ignore this test if increasing NUM_OF_NEW_RECS
    }

    // getLastXTransactionsList
    @Test
    public void test_ck_get_last_transactions_new() {
        List<TransRec> allTrans = TransRec.getLastXTransactionsList(10);
        assertNotEquals(null, allTrans);
        assertEquals(10, allTrans.size());

        allTrans = TransRec.getLastXTransactionsList(NUM_OF_NEW_RECS + 20);
        assertNotEquals(null, allTrans);
        assertNotEquals(0, allTrans.size());
//        assertEquals( 45, allTrans.size() ); // value is only correct if NUM_OF_NEW_RECS is 53 - revise or ignore this test if increasing NUM_OF_NEW_RECS

        allTrans = TransRec.getLastXTransactionsList(10, "cancelled = 0");
        assertNotEquals(null, allTrans);
        assertEquals(10, allTrans.size());
    }

    @Test
    public void test_d_read_latest_new() throws Exception {
        newTestReadLatestDataBase();
    }

    @Test
    public void test_e_update_lots_new() throws Exception {
        newTestUpdateDataBase();
    }

    @Test
    public void test_f_delete_old_txns_new() {
        int numDeleted = TransRecManager.getInstance().getTransRecDao().deleteTxnsBeforeUid((int) uidArrayNewUnshuffled[10]);
        assertEquals(10, numDeleted);
    }

    @Test
    public void test_g_delete_cancelled_new() {
        int numDeleted = TransRecManager.getInstance().getTransRecDao().deleteByCancelled(true);
        assertNotEquals(0, numDeleted);
    }

    @Test
    public void test_z_delete_all_new() throws Exception {
        newTestDeleteDataBase();
    }
}
