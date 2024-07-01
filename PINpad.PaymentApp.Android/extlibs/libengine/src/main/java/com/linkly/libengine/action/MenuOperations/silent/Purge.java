package com.linkly.libengine.action.MenuOperations.silent;


import static com.linkly.libengine.engine.EngineManager.TransType.RECONCILIATION;

import com.linkly.libengine.action.IAction;
import com.linkly.libengine.dependencies.IDependency;
import com.linkly.libengine.engine.transactions.TransRec;
import com.linkly.libengine.engine.transactions.TransRecManager;

import java.util.Collections;
import java.util.List;

import timber.log.Timber;

public class Purge extends IAction {

    private static IDependency dep;

    @Override
    public String getName() {
        return "Purge";
    }

    @Override

    public void run() {
        dep = d;
        purgeTransDB();
    }

    public static void purgeTransDBWhenWeAutoRec() {
        /*Purge the transaction DataBase. We want to delete all transactions after the 2nd last Rec*/
        Timber.i( "Purge TransRec DB ");

        long t1 = System.currentTimeMillis();
        List<TransRec> recHistory = TransRecManager.getInstance().getTransRecDao().findAllByTransType(RECONCILIATION, 3);

        long t2 = System.currentTimeMillis();
        Timber.i( "Purge DB: " + (t2 - t1));

        if (recHistory != null && recHistory.size() > 0) {
            if (recHistory.size() >= 3) {
                TransRec oldestRec = recHistory.get(recHistory.size() - 1);
                if (oldestRec != null && oldestRec.getUid() != 0) {
                    Integer oldIndex = oldestRec.getUid();

                    if (oldestRec.getProtocol().getMessageStatus().isFinalised()) {
                        /*Delete All TransRec With Older TransRec ID*/
                        TransRecManager.getInstance().getTransRecDao().deleteTxnsBeforeUid(oldIndex);
                    }
                }
            }

        } else {
            Timber.i( "TransRec Purge - Nothing to Do");
        }
        t2 = System.currentTimeMillis();
        Timber.i( "Purge DB (Total): " + (t2 - t1));
    }



    public static void purgeTransDB() {

        /* if a terminal does an auto rec then we can use that as the basic for deleting transactions */
        /* its quicker and allows us to keep transactions until the rec has definitely been uploaded */
        if (dep.getCustomer().supportAutoRecs()) {
            purgeTransDBWhenWeAutoRec();
            return;
        }

        // returns ordered from newest first to oldest last
        List<TransRec> recHistory = TransRecManager.getInstance().getTransRecDao().findAllByTransType( RECONCILIATION );

        if (recHistory != null && recHistory.size() > 2) {
            // reverse so we have oldest first, newest last
            Collections.reverse(recHistory);
            // keep last (newest) 2 recs
            int purgeableRecs = recHistory.size() - 2;
            for ( int i = 0; i < purgeableRecs; ++i) {
                TransRecManager.getInstance().getTransRecDao().delete(recHistory.get(i));
            }
        }

        int countBeforeLastZReport = TransRec.getCountOfAllBeforeLastZReport();
        int maximumDeleteCount = countBeforeLastZReport - 50; /* changed to 50 so we should have 10 approved at least */

        if (maximumDeleteCount > 0)
            TransRec.deleteTransUptoMaxCount(maximumDeleteCount);

    }
}
