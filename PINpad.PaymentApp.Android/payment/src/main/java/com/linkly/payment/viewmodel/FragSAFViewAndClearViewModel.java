package com.linkly.payment.viewmodel;

import static com.linkly.libengine.engine.transactions.properties.TProtocol.MessageStatus.ADVICE_QUEUED;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.MessageStatus.FINALISED;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.MessageStatus.FINALISED_AND_REVERSED;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.MessageStatus.REVERSAL_QUEUED;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.linkly.libengine.engine.Engine;
import com.linkly.libengine.engine.transactions.TransRec;
import com.linkly.libengine.engine.transactions.TransRecManager;
import com.linkly.libengine.engine.transactions.properties.TProtocol;
import com.linkly.libui.IUICurrency;
import com.linkly.payment.viewmodel.data.UIFragData;

import java.util.ArrayList;
import java.util.List;

public class FragSAFViewAndClearViewModel extends BaseViewModel {

    private final LiveData<List<TransRec>> allSafTransactions;

    /* default constructor */
    public FragSAFViewAndClearViewModel(Application application) {
        super(application);
        List<TProtocol.MessageStatus> statusList = new ArrayList<>();
        statusList.add(ADVICE_QUEUED);
        statusList.add(REVERSAL_QUEUED);
        allSafTransactions = TransRecManager.getInstance().getTransRecDao().findAllByMessageStatus(statusList);
    }

    private SAFTransaction convertToSAFTransaction(TransRec transRec) {
        SAFTransaction safTrans = new SAFTransaction();

        if (transRec.getProtocol().getMessageStatus() == REVERSAL_QUEUED) {
            safTrans.status = "Reversal";
        } else if (transRec.getProtocol().getMessageStatus() == ADVICE_QUEUED) {
            safTrans.status = "Advice";
        } else {
            safTrans.status = "Unknown";
        }

        safTrans.type = transRec.getTransType().getDisplayName();

        String totalAmount = String.format("%d", transRec.getAmounts().getTotalAmount());
        safTrans.amount = Engine.getDep().getFramework().getCurrency().formatAmount(totalAmount, IUICurrency.EAmountFormat.FMT_AMT_SHOW_SYMBOL, Engine.getDep().getPayCfg().getCountryCode());

        safTrans.uid = transRec.getUid();
        safTrans.pan = transRec.getCard().getMaskedPan();
        safTrans.date = transRec.getAudit().getTransDateTimeAsString("dd/MM/yy HH:mm");
        safTrans.rrn = "RRN:" + transRec.getProtocol().getRRN();

        return safTrans;
    }

    public LiveData<List<SAFTransaction>> getLiveDataSafList() {
        return Transformations.map(allSafTransactions, transactions -> {
            List<SAFTransaction> converted = new ArrayList<>();

            if(!transactions.isEmpty()) {
                for (TransRec trans: transactions) {
                    converted.add(convertToSAFTransaction(trans));
                }
            }

            return converted;
        });
    }

    public static class SAFTransaction {
        String type;
        String status;
        String amount;
        String pan;
        String date;
        String rrn;
        int uid;

        public String getType() { return type; }
        public String getAmount() {return amount;}
        public String getStatus() { return status; }
        public String getPan() { return pan; }
        public String getDate() { return date; }
        public String getRrn() { return rrn; }
        public int getUid() { return uid; }
    }

    /* default update function as not needed */
    protected void updateViewModel(MutableLiveData<UIFragData> fragData) {
        this.fragData = fragData;
    }

}
