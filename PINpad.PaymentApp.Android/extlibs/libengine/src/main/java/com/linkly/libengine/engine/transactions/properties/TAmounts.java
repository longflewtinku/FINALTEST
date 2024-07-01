package com.linkly.libengine.engine.transactions.properties;

import com.google.gson.Gson;
import com.linkly.libengine.config.PayCfg;
import com.linkly.libengine.engine.Engine;
import com.linkly.libengine.overrides.CoreOverrides;
import com.linkly.libui.currency.ISOCountryCodes;

import java.util.Locale;

public class TAmounts {

    private long amount = 0;
    private long preAuthedAmount = 0;
    private long topupAmount = 0;
    private long cashbackAmount = 0;
    private long tip = 0;
    private long surcharge = 0;
    private String amountUserEntered;
    private String currency;
    private boolean tipEntered = false;
    private long discountedAmount = 0;
    private String voucherCode = "";

    // Required due to being a DB entity
    public TAmounts() {

    }

    public TAmounts(PayCfg payCfg) {
        if( payCfg != null ) {
            currency = String.format( Locale.getDefault(), "%03d", payCfg.getCurrencyNum() );

            if ( CoreOverrides.get().getOverrideAmount() > 0 ) {
                amount = CoreOverrides.get().getOverrideAmount();
                amountUserEntered = String.format( Locale.getDefault(), "%d", amount );
            }
        }
    }

    /**
     * copies TAmounts, returns new instance - performs deep copy using serialization/deserialization
     *
     * @param copyFrom object to copy
     * @return copy of copyFrom
     */
    public static TAmounts copy( TAmounts copyFrom ) {
        Gson gson = new Gson();
        String serializedCopy = gson.toJson(copyFrom);
        return gson.fromJson(serializedCopy, TAmounts.class );
    }

    public static int getAmountCurExMul(int currencyNum) {
        int decimals = ISOCountryCodes.getInstance().getDecimalsFrom3Num(currencyNum + "");
        return (int)Math.pow(10, decimals);
    }


    public long getTotalAmount() {
        // special case for topup completions
        if (topupAmount > 0) {
            return topupAmount;
        }

        return amount + cashbackAmount + tip + surcharge;
    }

    public long getTotalAmountWithoutTip() {
        // special case for topup completions
        if (topupAmount > 0) {
            return topupAmount;
        }

        return amount + cashbackAmount + surcharge;
    }

    public long getBaseAmount() {
        // special case for topup completions
        if (topupAmount > 0) {
            return topupAmount;
        }

        return amount;
    }

    public long getTotalAmountWithoutCashback() {
        // special case for topup completions
        if (topupAmount > 0) {
            return topupAmount;
        }

        return amount + tip + surcharge;
    }

    public long getTotalAmountWithoutSurcharge() {
        // special case for topup completions
        if (topupAmount > 0) {
            return topupAmount;
        }

        return amount + tip + cashbackAmount;
    }

    public long getSurchargeableAmount() {
        // surcharge should be calculated on base amount and tip amt only, NOT cash, or any existing surcharge amount (to avoid double-surcharging)
        return amount + tip;
    }

    public long getAmount() {
        return this.amount;
    }

    public long getPreAuthedAmount() {
        return this.preAuthedAmount;
    }

    public long getTopupAmount() {
        return this.topupAmount;
    }

    public long getCashbackAmount() {
        return this.cashbackAmount;
    }

    public long getTip() {
        return this.tip;
    }

    public long getSurcharge() {
        return this.surcharge;
    }

    public String getAmountUserEntered() {
        return this.amountUserEntered;
    }

    public String getCurrency() {
        return this.currency;
    }

    public boolean isTipEntered() {
        return this.tipEntered;
    }

    public long getDiscountedAmount() {
        return this.discountedAmount;
    }

    public String getVoucherCode() {
        return this.voucherCode;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public void setPreAuthedAmount(long preAuthedAmount) {
        this.preAuthedAmount = preAuthedAmount;
    }

    public void setTopupAmount(long topupAmount) {
        this.topupAmount = topupAmount;
    }

    public void setCashbackAmount(long cashbackAmount) {
        this.cashbackAmount = cashbackAmount;
    }

    public void setTip(long tip) {
        this.tip = tip;
    }

    public void setSurcharge(long surcharge) {
        this.surcharge = surcharge;
    }

    public void setAmountUserEntered(String amountUserEntered) {
        this.amountUserEntered = amountUserEntered;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public void setTipEntered(boolean tipEntered) {
        this.tipEntered = tipEntered;
    }

    public void setDiscountedAmount(long discountedAmount) {
        this.discountedAmount = discountedAmount;
    }

    public void setVoucherCode(String voucherCode) {
        this.voucherCode = voucherCode;
    }
}

