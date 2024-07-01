package com.linkly.libengine.engine.reporting;

import java.util.ArrayList;

public class TotalsReport {

    private boolean isFullReport;

    private ArrayList<TotalsGroup> totalsDataItems;

    public TotalsReport() {
        totalsDataItems = new ArrayList<TotalsGroup>();
    }

    public boolean isFullReport() {
        return this.isFullReport;
    }

    public ArrayList<TotalsGroup> getTotalsDataItems() {
        return this.totalsDataItems;
    }

    public void setFullReport(boolean isFullReport) {
        this.isFullReport = isFullReport;
    }

    public static class TotalsGroup {
        public String cardName;
        ArrayList<Totals> transTotals;

        public TotalsGroup(String cardName) {
            this.cardName = cardName;
            this.transTotals = new ArrayList<Totals>();
        }

        public String getCardName() {
            return this.cardName;
        }

        public ArrayList<Totals> getTransTotals() {
            return this.transTotals;
        }
    }


}
