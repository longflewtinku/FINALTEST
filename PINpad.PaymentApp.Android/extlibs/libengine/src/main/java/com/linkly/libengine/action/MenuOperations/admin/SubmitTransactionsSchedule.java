package com.linkly.libengine.action.MenuOperations.admin;

import static com.linkly.libpositive.messages.IMessages.BATCH_UPLOAD_EVENT;

import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import com.linkly.libengine.action.IAction;
import com.linkly.libengine.config.PayCfg;
import com.linkly.libengine.engine.transactions.TransRec;
import com.linkly.libengine.jobs.EFTJobScheduleEvent;

import java.util.Date;

import timber.log.Timber;

public class SubmitTransactionsSchedule extends IAction {
    private boolean silent = false;
    private static EFTJobScheduleEvent batchUploadEvent = null;

    public SubmitTransactionsSchedule(boolean silent) {
        this.silent = silent;
    }

    @Override
    public String getName() {
        return "SubmitTransactionsSchedule";
    }

    @Override
    public void run() {
        // If in airplane mode there's no point of scheduling a batch upload;
        // It will be taken care of when the user switches off the airplane mode
        if (d.getPayCfg().isOfflineFlightModeAllowed() && trans != null && trans.isStartedInOfflineMode()) {
            return;
        }

        boolean isScheduled = batchUploadEvent != null; //Is an event already Scheduled
        int offset = getBatchBackoff();
        int batchCount = TransRec.countTransInBatch();
        long startTime = new Date().getTime();
        long triggerTime = startTime + offset;
        int paxstoreCount = TransRec.countTransToUploadToPaxstore();
        int emailCount = TransRec.countTransToUploadToEmailServer();

        Timber.i("Transaction Batch upload Event will happen in: " + (triggerTime - startTime) + "ms");
        /*First Check if any transaction in batch */
        if (batchCount > 0 || paxstoreCount > 0 || emailCount > 0) {
            if (isScheduled) {
                batchUploadEvent = new EFTJobScheduleEvent(EFTJobScheduleEvent.EventType.UPDATE, BATCH_UPLOAD_EVENT, triggerTime);
            } else {
                incBatchBackoffIndex();
                batchUploadEvent = new EFTJobScheduleEvent(EFTJobScheduleEvent.EventType.CREATE, BATCH_UPLOAD_EVENT, triggerTime);
            }
            d.getJobs().schedule(context, batchUploadEvent);
        } else {

            /*If the Task is Scheduled and batch is now clear*/
            if (isScheduled && batchCount == 0 && paxstoreCount == 0) {
                Timber.i("No Batch upload required");
                /*Cancel The existing Transaction */
                EFTJobScheduleEvent cancel = new EFTJobScheduleEvent(EFTJobScheduleEvent.EventType.CANCEL, BATCH_UPLOAD_EVENT, 0);
                d.getJobs().schedule(context, cancel);
                batchUploadEvent = null;
            }
        }
    }

    private void incBatchBackoffIndex() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        int index = sharedPref.getInt("batchBackOff", 0) + 1;

        if (index > 2) {
            index = 0;
        }

        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("batchBackOff", index);
        editor.commit();
    }

    private int getBatchBackoff() {
        PayCfg cfg = d.getPayCfg();
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        int offsetIndex = sharedPref.getInt("batchBackOff", 0);
        int triggerTime = 0;

        if (offsetIndex == 0) {
            triggerTime = cfg.getReconBackOff1();
        } else if (offsetIndex == 1) {
            triggerTime = cfg.getReconBackOff2();
        } else if (offsetIndex == 2) {
            triggerTime = cfg.getReconBackOff3();
        }

        return triggerTime * 1000;
    }
}
