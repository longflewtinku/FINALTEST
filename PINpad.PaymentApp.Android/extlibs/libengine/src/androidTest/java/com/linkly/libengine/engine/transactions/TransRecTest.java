package com.linkly.libengine.engine.transactions;

import static com.linkly.libengine.engine.EngineManager.TransType.PREAUTH;
import static com.linkly.libengine.engine.EngineManager.TransType.PREAUTH_AUTO;
import static com.linkly.libengine.engine.EngineManager.TransType.PREAUTH_MOTO;
import static com.linkly.libengine.engine.EngineManager.TransType.PREAUTH_MOTO_AUTO;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import android.content.Context;

import androidx.room.Room;
import androidx.sqlite.db.SimpleSQLiteQuery;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.linkly.libengine.config.PayCfg;
import com.linkly.libengine.config.PayCfgFactory;
import com.linkly.libengine.dependencies.Dependencies;
import com.linkly.libengine.dependencies.IDependency;
import com.linkly.libengine.engine.EngineManager.TransType;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

@Ignore("Ignoring as Integration Test")
@RunWith( AndroidJUnit4.class )
public class TransRecTest {
    private TransRecDao transRecDao;
    private TransRecDatabase db;
    private Dependencies dependency = new Dependencies();
    private PayCfg cfg;
    private Context context = ApplicationProvider.getApplicationContext();

    @Before
    public void createDb() {
        db = Room.inMemoryDatabaseBuilder( context, TransRecDatabase.class ).build();
        transRecDao = db.transRecDao();
        dependency.setPayCfg(new PayCfgFactory().getConfig(context));
    }

    @After
    public void closeDb() {
        db.close();
    }

    /**
     * Generate & Insert transactions with certain relevant parameters
     * @param transType {@link TransType} if it is sale, refund etc
     * @param isApproved if transaction is approved
     * @param currentTime to be stored as {@link TransRec#getAudit()}'s transFinishedDateTime value
     * */
    private void insertTrans( TransType transType, boolean isApproved, long currentTime ){
        TransRec transRec = new TransRec( transType, dependency);
        transRec.setApproved( isApproved );
        transRec.getAudit().setTransFinishedDateTime( currentTime );

        this.transRecDao.insert( transRec );
    }

    /**
     * Simple test. Check if DB can differentiate between sale & refund
     * */
    @Test
    public void testRefundOnlyTransactions() {
        transRecDao.deleteAll();

        insertTrans( TransType.REFUND_AUTO,
                true,
                0 );
        insertTrans( TransType.SALE_AUTO,
                true,
                0 );

        List<TransRec> transRecList = transRecDao.findAllByTransType(
                TransType.REFUND_AUTO
        );

        assertEquals( 1,
                transRecList.size() );

        assertEquals( TransType.REFUND_AUTO,
                transRecList.get( 0 ).getTransType() );
    }

    /**
     * Simple test. Check if DB can differentiate between sale & refund
     * */
    @Test
    public void testDeleteExceptTransTypes() {
        transRecDao.deleteAll();

        insertTrans( TransType.SALE_AUTO,
                true,
                0 );
        insertTrans( TransType.REFUND_AUTO,
                true,
                0 );
        insertTrans( TransType.PREAUTH,
                true,
                0 );
        insertTrans( TransType.SALE_AUTO,
                true,
                0 );
        insertTrans( TransType.PREAUTH_AUTO,
                true,
                0 );
        insertTrans( TransType.SALE_AUTO,
                true,
                0 );
        insertTrans( TransType.SALE_AUTO,
                true,
                0 );

        TransRec latest = transRecDao.getLatest();
        assertNotNull( latest );

        int deleted = transRecDao.deleteTxnsBeforeUidExceptTransTypes(latest.getUid(), PREAUTH, PREAUTH_AUTO, PREAUTH_MOTO, PREAUTH_MOTO_AUTO);
        assertEquals( 4, deleted );
    }

    /**
     * Test to see if DB can return records within a certain time period
     * */
    @Test
    public void testTransactionsInCertainTimePeriod() {
        long twoDaysInMs = 172800000;
        long currentTime = System.currentTimeMillis();

        transRecDao.deleteAll();
        this.insertTrans( TransType.SALE_AUTO, true, currentTime );
        this.insertTrans( TransType.REFUND_AUTO, true, System.currentTimeMillis() - twoDaysInMs );

        SimpleSQLiteQuery liteQuery = new SimpleSQLiteQuery( "SELECT * FROM transRecs WHERE audit_transFinishedDateTime > (" +
                ( currentTime - ( twoDaysInMs / 2 ) ) + ")" );
        List<TransRec> transRecList = transRecDao.executeTransListQuery( liteQuery );

        assertNotNull( transRecList );
        assertEquals( 1, transRecList.size() );
        assertEquals( TransType.SALE_AUTO,
                transRecList.get( 0 ).getTransType() );

    }

    /**
     * Happy scenario test. Test should be able to detect refunds within a certain time period & only approved ones
     * */
    @Test
    public void refundAndTimePeriodTest() {
        long oneDayInMs = 86400000;
        long currentTime = System.currentTimeMillis();

        transRecDao.deleteAll();

        // Create dummy sale transactions
        for ( int i = 0; i < 5; i++ ) {
            this.insertTrans( TransType.SALE_AUTO, false, currentTime - ( oneDayInMs * i / 2 ) );
        }

        // Create dummy refunds in the last 24 hours
        this.insertTrans( TransType.REFUND_AUTO, true, currentTime - 10 );
        this.insertTrans( TransType.REFUND, true, currentTime - oneDayInMs / 2 );
        this.insertTrans( TransType.REFUND_MOTO, true, currentTime - ( oneDayInMs * 2 ) );
        this.insertTrans( TransType.REFUND_MOTO_AUTO, false, currentTime );

        // Execute queries
        List<TransRec> transRecList = transRecDao.getTransRecsUntilATime( TransType.REFUND_AUTO,TransType.REFUND,TransType.REFUND_MOTO,TransType.REFUND_MOTO_AUTO, currentTime - oneDayInMs );

        // We should have two
        assertNotNull( transRecList );
        assertEquals( 2, transRecList.size() );
        assertEquals( TransType.REFUND_AUTO, transRecList.get( 1 ).getTransType() );
    }


    /**
     * Scenario where there are no approved refunds within a certain time period
     * */
    @Test
    public void onlyDeclinedRefunds() {
        long currentTime = System.currentTimeMillis();
        long oneDayInMs = 86400000;

        transRecDao.deleteAll();

        // Create dummy sale transactions
        for ( int i = 0; i < 5; i++ ) {
            this.insertTrans( TransType.SALE_AUTO, false, currentTime );
        }

        // Create dummy refunds in the last 24 hours
        this.insertTrans( TransType.REFUND_AUTO, false, currentTime - 10 );
        this.insertTrans( TransType.REFUND_AUTO, false, currentTime - 200 );
        this.insertTrans( TransType.REFUND_AUTO, false, currentTime - 400 );
        this.insertTrans( TransType.REFUND_AUTO, false, currentTime - 1000 );

        // Execute queries
        List<TransRec> transRecList = transRecDao.getTransRecsUntilATime( TransType.REFUND_AUTO,TransType.REFUND,TransType.REFUND_MOTO,TransType.REFUND_MOTO_AUTO, currentTime - oneDayInMs );

        // We should have two
        assertNotNull( transRecList );
        assertEquals( 0, transRecList.size() );
    }

    /**
     * Check if an empty DB doesn't cause issues
     * */
    @Test
    public void emptyTerminal() {
        transRecDao.deleteAll();
        long oneDayInMs = 86400000;
        long currentTime = System.currentTimeMillis() - oneDayInMs;

        assertNotNull( transRecDao.getTransRecsUntilATime(
                TransType.REFUND_AUTO,TransType.REFUND,TransType.REFUND_MOTO,TransType.REFUND_MOTO_AUTO,
                currentTime
        ) );
    }

}