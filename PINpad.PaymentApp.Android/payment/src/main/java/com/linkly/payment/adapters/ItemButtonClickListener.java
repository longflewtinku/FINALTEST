package com.linkly.payment.adapters;


import com.linkly.payment.viewmodel.FragSAFViewAndClearViewModel;

public interface ItemButtonClickListener {
    void itemButtonClicked(FragSAFViewAndClearViewModel.SAFTransaction t);
}