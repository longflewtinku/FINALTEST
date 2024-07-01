package com.linkly.payment.viewmodel;

import static com.linkly.libengine.engine.EngineManager.TransType.PREAUTH;
import static com.linkly.libengine.engine.EngineManager.TransType.PREAUTH_AUTO;
import static com.linkly.libengine.engine.EngineManager.TransType.PREAUTH_MOTO;
import static com.linkly.libengine.engine.EngineManager.TransType.PREAUTH_MOTO_AUTO;
import static com.linkly.libui.IUIDisplay.uiResultText1;

import android.app.Application;
import android.util.Pair;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.linkly.libengine.config.PayCfg;
import com.linkly.libengine.config.PayCfgFactory;
import com.linkly.libengine.engine.Engine;
import com.linkly.libengine.engine.transactions.TransRec;
import com.linkly.libengine.engine.transactions.TransRecManager;
import com.linkly.libui.IUICurrency;
import com.linkly.payment.viewmodel.data.UIFragData;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import timber.log.Timber;

public class FragPreAuthListViewModel extends BaseViewModel {

    private final PayCfg config;

    private final LiveData<List<TransRec>> preAuthTxns;

    /* default constructor */
    public FragPreAuthListViewModel(Application application) {
        super(application);
        config = new PayCfgFactory().getConfig(application);
        preAuthTxns = TransRecManager.getInstance().getTransRecDao().getByTransTypesAndApprovedLiveData(PREAUTH, PREAUTH_AUTO, PREAUTH_MOTO, PREAUTH_MOTO_AUTO);
    }

    public LiveData<List<PreAuthTransaction>> getPreAuthTransactions() {
        return Transformations.map(preAuthTxns, entities -> {
           List<PreAuthTransaction> converted = new ArrayList<>();

            String fetchString = getDisplay().getValue().getUiExtras().getString(uiResultText1);
            String[] fetchParams;
            Timber.d("Fetch String = %s", fetchString);

            if( fetchString == null ) {
                return converted;
            }

            // get requested Pre-Auth properties to enlist:
            // <start date>,<end date>,<RRN>,<AuthId>
            fetchParams = fetchString.split(",", -1);

            if(fetchParams.length == 4 && !entities.isEmpty()) {
                Pair<Long,Long> startEndDateTime = calculateStartDateMillis(fetchParams[0], fetchParams[1]);

                for (TransRec trans : entities) {
                    int expiryDays = TransRec.findExpiryDaysByBinNumber(trans.getCard().getLinklyBinNumber(), config); // find expiry days for particular card scheme or default from config
                    long transExpiryTime = TransRec.getTransExpiryTime(trans.getAudit().getTransFinishedDateTime(), expiryDays);  // get transaction expiry time by adding expiry days
                    boolean isExpired = (transExpiryTime >= startEndDateTime.first && transExpiryTime <= startEndDateTime.second);
                    // check if transaction expiry time is before time now
                    if ( isExpired || checkAndHandleLeadingZeros(trans, fetchParams)) {
                        converted.add(transToPreAuthConvert(trans));
                        Timber.i("Preauth record with amount %d - expired: %b", trans.getAmounts().getAmount(), isExpired);
                    } else {
                        Timber.i("No Preauth record to display");
                    }
                }
            }

            return converted;
        });
    }

    private PreAuthTransaction transToPreAuthConvert(TransRec transRec) {

            PreAuthTransaction preAuthTrans = new PreAuthTransaction();
            preAuthTrans.type = transRec.getTransType().getDisplayName();

            String totalAmount = String.format("%d", transRec.getAmounts().getTotalAmount());
            preAuthTrans.amount = Engine.getDep().getFramework().getCurrency().formatAmount(totalAmount, IUICurrency.EAmountFormat.FMT_AMT_SHOW_SYMBOL, config.getCountryCode());
            preAuthTrans.uid = transRec.getUid();
            preAuthTrans.pan = transRec.getCard().getMaskedPan();
            preAuthTrans.date = transRec.getAudit().getTransDateTimeAsString("dd/MM/yy HH:mm");
            preAuthTrans.rrn = "RRN:" + transRec.getProtocol().getRRN();
            preAuthTrans.authCode = "AuthCode:" + transRec.getProtocol().getAuthCode();
            return preAuthTrans;
    }

    public static class PreAuthTransaction {
        String type;
        String amount;
        String pan;
        String date;
        String rrn;
        String authCode;
        int uid;

        public String getType() { return type; }
        public String getAmount() {return amount;}
        public String getPan() { return pan; }
        public String getDate() { return date; }
        public String getRrn() { return rrn; }
        public String getAuthCode() { return authCode; }
        public int getUid() { return uid; }
    }

    /* default update function as not needed */
    protected void updateViewModel(MutableLiveData<UIFragData> fragData) {
        this.fragData = fragData;
    }

    private boolean checkAndHandleLeadingZeros(TransRec trans, String[] fetchParams) {
        if (trans.getProtocol().getAuthCode() != null && trans.getProtocol().getRRN() != null) {
            return trans.getProtocol().getRRN().replaceAll("^0+", "").equals(fetchParams[2].replaceAll("^0+", "")) ||
                    trans.getProtocol().getAuthCode().replaceAll("^0+", "").equals(fetchParams[3].replaceAll("^0+", ""));
        }

        return false;
    }

    Pair<Long,Long> calculateStartDateMillis(String start, String end) {
        if (!start.equals("") && !end.equals("") && config != null) {
            try {

                Calendar cStart = Calendar.getInstance();
                cStart.setTimeInMillis(Long.parseLong(start));
                cStart.set(Calendar.HOUR_OF_DAY,0);
                cStart.set(Calendar.MINUTE,0);
                cStart.set(Calendar.SECOND,0);
                cStart.set(Calendar.MILLISECOND,0);

                Calendar cEnd = Calendar.getInstance();
                cEnd.setTimeInMillis(Long.parseLong(end));
                cEnd.set(Calendar.HOUR_OF_DAY,23);
                cEnd.set(Calendar.MINUTE,59);
                cEnd.set(Calendar.SECOND,59);
                cEnd.set(Calendar.MILLISECOND,999);

                return new Pair<>(cStart.getTimeInMillis(),cEnd.getTimeInMillis());
            } catch (NumberFormatException ex) {
                Timber.e("format error while parsing dates");
            }
        }
        return new Pair<>(0L,0L);
    }

}
