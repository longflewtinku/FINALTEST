package com.linkly.libengine.action.MenuOperations.admin;

import static com.linkly.libengine.engine.protocol.IProto.ProtoResult.PROTO_DECLINED;
import static com.linkly.libengine.engine.protocol.IProto.ProtoResult.PROTO_FAIL;
import static com.linkly.libengine.engine.protocol.IProto.ProtoResult.PROTO_SUCCESS;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.HostResult.BATCH_UPLOAD_FAILED;
import static com.linkly.libengine.helpers.UIHelpers.uiShowDismissableScreen;
import static com.linkly.libpositive.messages.IMessages.APP_REFRESH_SCREEN_EVENT;
import static com.linkly.libui.UIScreenDef.DEFERRED_AUTH_DECLINED;
import static com.linkly.libui.UIScreenDef.NO_TRANS_TO_UPLOAD;
import static com.linkly.libui.UIScreenDef.TRANS_UPLOAD_FAILED;
import static com.linkly.libui.UIScreenDef.TRANS_UPLOAD_SUCCESSFUL;
import static com.linkly.libui.UIScreenDef.VAR_PENDING;

import android.content.Intent;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.linkly.libengine.action.IAction;
import com.linkly.libengine.action.IPC.EmailUpload;
import com.linkly.libengine.action.IPC.PaxstoreUpload;
import com.linkly.libengine.engine.protocol.IProto;
import com.linkly.libengine.engine.transactions.TransRec;
import com.linkly.libmal.MalFactory;

public class SubmitTransactions extends IAction {

    private boolean silent = false;

    public SubmitTransactions(boolean silent) {
        this.silent = silent;
    }

    @Override
    public String getName() {
        return "SubmitTransactions";
    }

    // Suppressing complexity warning. limit is 15 this is 17. Not worth it.
    @SuppressWarnings("java:S3776")
    @Override
    public void run() {
        int transInBatch = TransRec.countTransInBatch();

        if (transInBatch <= 0) {
            if (!silent && !(trans != null && trans.isReconciliation())) {
                ui.showScreen(NO_TRANS_TO_UPLOAD);
                ui.displayMainMenuScreen();
            }
            checkOtherUploads();
            return;
        }

        if (!silent) {
            ui.showScreen(VAR_PENDING, Integer.toString(transInBatch));
        }

        if (d.getProtocol() != null) {
            IProto.ProtoResult res = d.getProtocol().batchUpload(silent);
            if (res != PROTO_SUCCESS && trans != null) {
                trans.getProtocol().setHostResult(BATCH_UPLOAD_FAILED);
                d.getProtocol().setInternalRejectReason(trans, IProto.RejectReasonType.BATCH_UPLOAD_FAILED);
            }
            if (!silent) {
                if (res == PROTO_SUCCESS) {
                    ui.showScreen(TRANS_UPLOAD_SUCCESSFUL);
                } else if (res == PROTO_FAIL) {
                    ui.showScreen(TRANS_UPLOAD_FAILED);
                } else if (res == PROTO_DECLINED) {
                    uiShowDismissableScreen(d, DEFERRED_AUTH_DECLINED);
                }
            }
        }

        if (!silent)
            ui.displayMainMenuScreen();
        refreshUI();

        checkOtherUploads();
    }

    private void checkOtherUploads() {
        if (silent) {
            PaxstoreUpload.runPaxStoreUpload(d);
        }
        EmailUpload.runEmailUpload(d, mal);
    }

    public void refreshUI() {
        Intent tempIntent = new Intent();
        tempIntent.setAction(APP_REFRESH_SCREEN_EVENT);
        LocalBroadcastManager.getInstance(MalFactory.getInstance().getMalContext()).sendBroadcast(tempIntent);
    }
}
