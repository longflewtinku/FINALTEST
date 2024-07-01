package com.linkly.libengine.engine.cards;

import com.linkly.libengine.dependencies.IDependency;
import com.linkly.libengine.engine.transactions.properties.TSurcharge;
import com.linkly.libsecapp.IP2PCtls;

public class CtlsCallbacks implements IP2PCtls.CtlsListener {
    private IDependency d;

    public CtlsCallbacks(IDependency d) {
        this.d = d;
    }

    public long getUpdatedCtlsAmountTrans(String aid, boolean mncCard, boolean mndCard, boolean debitAppLabel) {
        setSurcharge(aid, mncCard, mndCard, debitAppLabel);
        return d.getCurrentTransaction().getAmounts().getTotalAmount();
    }

    private void setSurcharge(String aid, boolean mncCard, boolean mndCard, boolean debitAppLabel) {
        TSurcharge.calculateSurchargeForCtls(d, aid, mncCard, mndCard, debitAppLabel);
    }
}
