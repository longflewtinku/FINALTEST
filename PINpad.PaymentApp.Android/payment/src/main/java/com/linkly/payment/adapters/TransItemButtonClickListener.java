package com.linkly.payment.adapters;


import com.linkly.payment.viewmodel.FragTransactionHistoryViewModel;

public interface TransItemButtonClickListener {
    void itemButtonClicked(FragTransactionHistoryViewModel.TransactionInfo t);
}