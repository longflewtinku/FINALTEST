package com.linkly.libengine.action.DB;

import static com.linkly.libengine.engine.EngineManager.TransType.PREAUTH;
import static com.linkly.libengine.engine.EngineManager.TransType.PREAUTH_AUTO;
import static com.linkly.libengine.engine.EngineManager.TransType.PREAUTH_MOTO;
import static com.linkly.libengine.engine.EngineManager.TransType.PREAUTH_MOTO_AUTO;
import static com.linkly.libengine.engine.EngineManager.TransType.RECONCILIATION;
import static com.linkly.libengine.engine.reporting.ReconciliationManager.reconciliationDao;

import com.linkly.libengine.action.IAction;
import com.linkly.libengine.config.PayCfg;
import com.linkly.libengine.engine.reporting.Reconciliation;
import com.linkly.libengine.engine.reporting.ReconciliationManager;
import com.linkly.libengine.engine.transactions.TransRec;
import com.linkly.libengine.engine.transactions.TransRecManager;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import timber.log.Timber;

public class DBTransPurge extends IAction {
    private static final int DEFAULT_MAX_DAYS_TRANSACTIONS_TO_STORE = 1;
    private int maxDaysTransactionsToStore;

    private void parseConfig() {
        maxDaysTransactionsToStore = DEFAULT_MAX_DAYS_TRANSACTIONS_TO_STORE;

        try {
            PayCfg payCfg = d.getPayCfg();
            maxDaysTransactionsToStore = Integer.parseInt(payCfg.getMaxDaysTransactionsToStore());
        } catch (Exception ignored) {
            Timber.i("config format error for maxDaysTransactionsToStore, using default");
        }

    }

    @Override
    public String getName() {
        return "DBTransPurge";
    }

    @Override
    public void run() {
        // TODO: Refactor required as ReconiliationManager as an internal dependency on context to initalise Room DB
        // instantiate if it's not already
        ReconciliationManager.getInstance();

        parseConfig();

        // step 1 - purge declined settlement/reconciliation records. no need to keep these around
        this.purgeDeclinedRecs();

        // step 2 - delete original preauth record for certain situations
        this.purgeUsedPreauths();

        // step 3 - delete expired preauth records
        this.purgeExpiredPreauths();

        // step 4 - purge trans database
        this.purgeTransDB();

    }

    private void purgeUsedPreauths() {
        // if trans was a completion and successful, then purge original preauth record as it's 'used'. The preauth cancel transaction will not be purged but will be reversed
        // NOTE: preauth and the reversal/completion are separate transaction records
        if (trans.isCompletion() && trans.isApproved() && trans.getPreauthUid() != null) {
            TransRec originalTxn = TransRecManager.getInstance().getTransRecDao().getByUid(trans.getPreauthUid());
            if( originalTxn != null ) {
                // delete this preauth record
                Timber.i( "purging preauth record uid %d", originalTxn.uid );
                TransRecManager.getInstance().getTransRecDao().delete(originalTxn);
            }
        }
    }

    private void purgeExpiredPreauths()
    {
        Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        long timeNow =  c.getTimeInMillis();

        List<TransRec> preAuthList = TransRecManager.getInstance().getTransRecDao().getByTransTypesAndApproved(PREAUTH, PREAUTH_AUTO, PREAUTH_MOTO, PREAUTH_MOTO_AUTO); // finding all Preauths and approved
        int expiryDays;
        long transExpiryTime;
        if (preAuthList != null ) {
            for (TransRec trans : preAuthList) {
                expiryDays = TransRec.findExpiryDaysByBinNumber(trans.getCard().getLinklyBinNumber(), d.getPayCfg()); // find expiry days for particular card scheme or default from config
                transExpiryTime = TransRec.getTransExpiryTime(trans.getAudit().getTransFinishedDateTime(),expiryDays);  // get transaction expiry time by adding expiry days
                if (timeNow > transExpiryTime) {   // check if transaction expiry time is before time now
                    TransRecManager.getInstance().getTransRecDao().delete(trans);
                    Timber.i("Deleting Pre-auth record with amount %d",  trans.getAmounts().getAmount());
                } else {
                    Timber.i("No Pre-auth record to delete");
                }
            }

        }
    }

    private void purgeDeclinedRecs() {
        List<TransRec> recHistory = TransRecManager.getInstance().getTransRecDao().findAllByTransTypeAndApproved(RECONCILIATION, false);
        if (recHistory != null ) {
            for( TransRec trans : recHistory ) {
                Reconciliation recRecord = reconciliationDao.findByTransId( trans.getUid() );
                if( recRecord != null ) {
                    // delete this also
                    reconciliationDao.delete(recRecord);
                }
                TransRecManager.getInstance().getTransRecDao().delete(trans);
            }
            Timber.i( "purgeDeclinedRecs deleted records: %d", recHistory.size());
        } else {
            Timber.i( "purgeDeclinedRecs no declined recs to delete");
        }
    }

    private void purgeTransDB() {
        int transRecordsDeleted = 0;
        int recRecordsDeleted = 0;

        // Purge the transaction DataBase. We want to delete all transactions after the last Rec
        Timber.i( "Purge TransRec DB");

        // +1 as a full day of transaction record will be contained within 2 reconciliation records
        int maxReconciliationRecords = maxDaysTransactionsToStore + 1;

        long t1 = System.currentTimeMillis();
        List<TransRec> recHistory = TransRecManager.getInstance().getTransRecDao().findLatestByTransType(RECONCILIATION, maxReconciliationRecords);

        long t2 = System.currentTimeMillis();
        Timber.i( "Purge DB: %d", t2 - t1);

        if (recHistory != null && recHistory.size() >= maxReconciliationRecords) {
            TransRec oldestRec = recHistory.get(recHistory.size() - 1);
            if (oldestRec != null && oldestRec.getUid() != 0) {
                int oldIndex = oldestRec.getUid();
                if (oldestRec.getProtocol().getMessageStatus().isFinalised()) {

                    // delete all reconciliation records with older uid
                    Reconciliation recRecord = reconciliationDao.findByTransId( oldestRec.getUid() );
                    if( recRecord != null ) {
                        // delete this also
                        recRecordsDeleted += reconciliationDao.deleteTxnsBeforeUid(recRecord.getUid());
                    }

                    // delete All TransRec With Older TransRec ID except preauths. These need to stay around for at least the preauth expiry period
                    // I KNOW this is ugly but these are different enums. also, not sure how to pass variable arg lists to DAO
                    transRecordsDeleted += TransRecManager.getInstance().getTransRecDao().deleteTxnsBeforeUidExceptTransTypes(oldIndex, PREAUTH, PREAUTH_AUTO, PREAUTH_MOTO, PREAUTH_MOTO_AUTO);
                }
            }

        } else {
            Timber.i( "TransRec Purge - Nothing to Do");
        }

        t2 = System.currentTimeMillis();
        Timber.i( "Purge DB (Total time msec): " + (t2 - t1) + ", deleted " + transRecordsDeleted + " txn records and " + recRecordsDeleted + " reconciliation records" );
    }
}
