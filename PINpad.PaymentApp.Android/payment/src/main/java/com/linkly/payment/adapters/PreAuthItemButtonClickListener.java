package com.linkly.payment.adapters;


import com.linkly.payment.viewmodel.FragPreAuthListViewModel;

public interface PreAuthItemButtonClickListener {
    void preAuthItemButtonClicked(FragPreAuthListViewModel.PreAuthTransaction t);
}