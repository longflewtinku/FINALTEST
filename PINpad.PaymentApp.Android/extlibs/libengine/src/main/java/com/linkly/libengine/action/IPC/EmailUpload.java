package com.linkly.libengine.action.IPC;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.Build;

import com.linkly.libengine.action.IAction;
import com.linkly.libengine.config.PayCfg;
import com.linkly.libengine.dependencies.IDependency;
import com.linkly.libengine.engine.transactions.TransRec;
import com.linkly.libengine.engine.transactions.TransRecManager;
import com.linkly.libengine.mail.GMailSender;
import com.linkly.libengine.printing.IReceipt;
import com.linkly.libmal.IMal;
import com.linkly.libmal.MalFactory;
import com.linkly.libmal.global.printing.PrintReceipt;
import com.linkly.libmal.global.util.Util;

import java.io.File;
import java.util.List;

import timber.log.Timber;

public class EmailUpload extends IAction {

    @Override
    public String getName() {
        return "EmailUpload";
    }

    @Override
    public void run() {

        if (d.getPayCfg().isMailEnabled() && trans.getProtocol().isCustomerEmailToUpload())
            runEmailUploadForTransaction(d, mal, trans, true);

        if (d.getPayCfg().isMailEnabled() && trans.getProtocol().isMerchantEmailToUpload()) {
            runEmailUploadForTransaction(d, mal, trans, false);
        }
    }

    public static void deleteSignatureFile(TransRec trans) {
        String filename = MalFactory.getInstance().getFile().getWorkingDir() + "/sig/" + trans.getAudit().getUti() + ".png";
        File sig = new File(filename);
        if (sig.exists())
            sig.delete();

    }
    public static Bitmap addSignatureToReceipt(IDependency d, TransRec trans, Bitmap receipt) {

        String filename = MalFactory.getInstance().getFile().getWorkingDir() + "/sig/" + trans.getAudit().getUti() + ".png";

        File sig = new File(filename);
        if (sig.exists()) {

            Bitmap bitmap = BitmapFactory.decodeFile(filename);

            int height = receipt.getHeight();
            int width = receipt.getWidth();

            int origHeight = bitmap.getHeight();
            int origWidth = bitmap.getWidth();


            if (bitmap != null) {

                int destHeight = origHeight/( origWidth / width ) ;

                Bitmap sigBitmapResized =  Bitmap.createScaledBitmap(bitmap, width, destHeight, false);
                Bitmap result = Bitmap.createBitmap(receipt.getWidth(), receipt.getHeight() + destHeight, receipt.getConfig());
                Canvas canvas = new Canvas(result);

                canvas.drawBitmap(receipt, 0f, 0f, null);
                canvas.drawBitmap(sigBitmapResized, 0, height, null);
                return result;
            }
        }

        return receipt;
    }


    // This function is used in static functions...
    public static boolean runEmailUploadForTransaction(IDependency d, IMal mal, TransRec trans, boolean cardholder) {

        try {

            IReceipt receiptGenerator = d.getPrintManager().getReceiptForTrans(d,trans, mal);
            if (receiptGenerator == null) {
                return true;
            }

            String address = d.getPayCfg().getMailMerchantAddress();
            if (cardholder) {
                if (Util.isNullOrEmpty(trans.getProtocol().getMailCustomerAddress())) {
                    return true;
                }
                address = trans.getProtocol().getMailCustomerAddress();
            }

            Bitmap merchantReceipt = null;
            Bitmap cardholderReceipt = null;

            // only merchant gets the merchant receipt
            if (!cardholder) {

                receiptGenerator.setIsMerchantCopy(true);
                receiptGenerator.setIsCardHolderCopy(false);
                receiptGenerator.setIsDuplicate(true);
                PrintReceipt receiptToPrint = receiptGenerator.generateReceipt(trans);
                merchantReceipt = d.getPrintManager().printReceiptToBitmap(mal.getPrint(), receiptToPrint);
                merchantReceipt = addSignatureToReceipt(d, trans, merchantReceipt);
            }

            receiptGenerator.setIsMerchantCopy(false);
            receiptGenerator.setIsCardHolderCopy(true);
            receiptGenerator.setIsDuplicate(true);
            PrintReceipt receiptToPrint = receiptGenerator.generateReceipt(trans);
            cardholderReceipt = d.getPrintManager().printReceiptToBitmap(mal.getPrint(), receiptToPrint);

            return queueEmailUpload(d, trans, address, merchantReceipt, cardholderReceipt, cardholder);
        } catch ( Exception e) {
            Timber.w(e);
        }
        return false;
    }
    @SuppressWarnings("deprecation")
    private static boolean queueEmailUpload(IDependency d, TransRec trans, String address, Bitmap merchantReceipt, Bitmap cardholderReceipt, boolean cardholder) throws Exception{

        GMailSender sender = new GMailSender(d);
        String storeName = d.getPayCfg().getMailStoreName();
        if (Util.isNullOrEmpty(storeName))
            storeName = d.getPayCfg().getReceipt().getMerchant().getLine0();

        if ( sender.sendMail(merchantReceipt, cardholderReceipt, storeName + ":Receipt from Terminal:" + Build.SERIAL, address, "Please find your receipt attached for: " + storeName) ) {

            if (!cardholder)
                trans.getProtocol().setMerchantEmailToUpload(false);
            else
                trans.getProtocol().setCustomerEmailToUpload(false);
            trans.save();

            deleteSignatureFile(trans);

        }
        return true;

    }


    /* run the email upload for all outstanding transactions, done in silent in the background */
    public static void runEmailUpload(IDependency d, IMal mal) {
        try {

            PayCfg cfg = d.getPayCfg();
            if (cfg.isMailEnabled()) {

                if (TransRec.countTransToUploadToEmailServer() == 0)
                    return;

                List<TransRec> oldestMerchantToUpload = TransRecManager.getInstance().getTransRecDao().getOldestNMerchantEmailToUpload(true, 20);
                if (oldestMerchantToUpload != null || oldestMerchantToUpload.size() > 0) {
                    for (TransRec trans : oldestMerchantToUpload) {

                        if (!runEmailUploadForTransaction(d, mal, trans, false)) {
                            break;
                        }
                    }

                }

                List<TransRec> oldestCardholderToUpload = TransRecManager.getInstance().getTransRecDao().getOldestNCustomerEmailToUpload(true, 20);
                if (oldestCardholderToUpload != null || oldestCardholderToUpload.size() > 0) {
                    for (TransRec trans : oldestCardholderToUpload) {

                        if (!runEmailUploadForTransaction(d, mal, trans, true)) {
                            break;
                        }
                    }
                }
            }
        } catch ( Exception e) {
            Timber.w(e);
        }
    }
}
