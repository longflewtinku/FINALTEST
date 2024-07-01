package com.linkly.libengine.config.paycfg;


// TODO confirm this is even used, not loaded into config etc.
public class ManualEntry {
    private boolean payment = false;
    private boolean paymentOffline= false;
    private boolean sale= false;
    private boolean refund= false;
    private boolean offlineRefund= false;
    private boolean cash= false;
    private boolean reversal= false;
    private boolean cashback= false;
    private boolean preauth= false;
    private boolean balance= false;
    private boolean forced= false;
    private boolean offlineCash= false;

    public boolean isPayment() {
        return this.payment;
    }

    public boolean isPaymentOffline() {
        return this.paymentOffline;
    }

    public boolean isSale() {
        return this.sale;
    }

    public boolean isRefund() {
        return this.refund;
    }

    public boolean isOfflineRefund() {
        return this.offlineRefund;
    }

    public boolean isCash() {
        return this.cash;
    }

    public boolean isReversal() {
        return this.reversal;
    }

    public boolean isCashback() {
        return this.cashback;
    }

    public boolean isPreauth() {
        return this.preauth;
    }

    public boolean isBalance() {
        return this.balance;
    }

    public boolean isForced() {
        return this.forced;
    }

    public boolean isOfflineCash() {
        return this.offlineCash;
    }
}