package com.linkly.libengine.action.IPC;

import android.graphics.Bitmap;
import android.util.Base64;

import com.linkly.libengine.action.IAction;
import com.linkly.libengine.dependencies.IDependency;
import com.linkly.libengine.engine.transactions.TransRec;
import com.linkly.libengine.engine.transactions.TransRecManager;

import java.io.ByteArrayOutputStream;
import java.util.List;

import timber.log.Timber;

public class PaxstoreUpload extends IAction {


    public static String encodeToBase64(Bitmap image)
    {
        Bitmap immagex=image;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        immagex.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] b = baos.toByteArray();
        String imageEncoded = Base64.encodeToString(b, Base64.NO_WRAP);

        Timber.i(imageEncoded);
        return imageEncoded;
    }

    @Override
    public String getName() {
        return "PaxstoreUpload";
    }

    @Override
    public void run() {
        runPaxStoreUpload(d);
    }
    public static void runPaxStoreUpload(IDependency d) {
        try {

            if (!d.getPayCfg().isPaxstoreUpload())
                return;

            if (TransRec.countTransToUploadToPaxstore() == 0)
                return;
            
            List<TransRec> oldest20NotUploaded = TransRecManager.getInstance().getTransRecDao().getOldestNPaxstoreUploaded(false, 20);
            if (oldest20NotUploaded == null || oldest20NotUploaded.size() == 0)
                return;

            for (TransRec trans : oldest20NotUploaded) {
                // upload the approved or declined transaction
                if (trans.isApproved() || trans.isDeclined())
                {
                    if (performUpload(trans, d)) {
                        trans.getProtocol().setPaxstoreUploaded(true);
                        trans.save();
                    } else {
                        break; /* no point in continuing to fail to upload transactions */
                    }
                }

            }

        } catch ( Exception e) {
            Timber.w(e);
        }
    }

    private static boolean performRecUpload(TransRec trans, IDependency d) {
        return false;
    }


    private static boolean performUpload(TransRec trans, IDependency d) {
        return false;
    }
}
