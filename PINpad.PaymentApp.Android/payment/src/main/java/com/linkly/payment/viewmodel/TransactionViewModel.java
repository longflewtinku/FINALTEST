package com.linkly.payment.viewmodel;

import android.app.Application;

import androidx.lifecycle.MutableLiveData;

import com.linkly.payment.viewmodel.data.UIFragData;

public class TransactionViewModel extends BaseViewModel {

    /* default constructor */
    public TransactionViewModel(Application application) {
        super(application);
    }

    /* default update function as not needed */
    protected void updateViewModel(MutableLiveData<UIFragData> fragData) {
        this.fragData = fragData;
    }
}
