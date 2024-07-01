package com.linkly.libengine.engine.reporting;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "shiftTotals")
public class ShiftTotals {

    @PrimaryKey(autoGenerate = true)
    private int id = 0;
    private long saleAmount = 0;
    private long saleCount = 0;
    private long cashAmount = 0;
    private long cashCount = 0;
    private long refundAmount = 0;
    private long refundCount = 0;
    private long completionAmount = 0;
    private long completionCount = 0;
    private long tipAmount = 0;
    private long tipCount = 0;
    private long surchargeAmount = 0;
    private long surchargeCount = 0;
    private long totalAmount = 0;
    private long totalCount = 0;
    private String schemeTotals = "";
    private long totalsFrom = 0;
    private long totalsTo = 0;
    private Integer lastTransStan = 0;
    private long shiftAutoClosingDateTime = 0;

    public void setId(int id) {this.id = id;}
    public void setSaleAmount(long saleAmount) {this.saleAmount = saleAmount;}
    public void setSaleCount(long saleCount) {this.saleCount = saleCount;}
    public void setCashAmount(long cashAmount) {this.cashAmount = cashAmount;}
    public void setCashCount(long cashCount) {this.cashCount = cashCount;}
    public void setRefundAmount(long refundAmount) {this.refundAmount = refundAmount;}
    public void setRefundCount(long refundCount) {this.refundCount = refundCount;}
    public void setCompletionAmount(long completionAmount) {this.completionAmount = completionAmount;}
    public void setCompletionCount(long completionCount) {this.completionCount = completionCount;}
    public void setTipAmount(long tipAmount) {this.tipAmount = tipAmount;}
    public void setTipCount(long tipCount) {this.tipCount = tipCount;}
    public void setSurchargeAmount(long surchargeAmount) {this.surchargeAmount = surchargeAmount;}
    public void setSurchargeCount(long surchargeCount) {this.surchargeCount = surchargeCount;}
    public void setTotalAmount(long totalAmount) {this.totalAmount = totalAmount;}
    public void setTotalCount(long totalCount) {this.totalCount = totalCount;}
    public void setSchemeTotals(String schemeTotals) {this.schemeTotals = schemeTotals;}
    public void setTotalsFrom(long totalsFrom) {this.totalsFrom = totalsFrom;}
    public void setTotalsTo(long totalsTo) {this.totalsTo = totalsTo;}
    public void setLastTransStan(Integer lastTransStan) {this.lastTransStan = lastTransStan;}
    public void setShiftAutoClosingDateTime(long shiftAutoClosingDateTime) { this.shiftAutoClosingDateTime = shiftAutoClosingDateTime;}

    public int getId() {return id;}
    public long getSaleAmount() {return saleAmount;}
    public long getSaleCount() {return saleCount;}
    public long getCashAmount() {return cashAmount;}
    public long getCashCount() {return cashCount;}
    public long getRefundAmount() {return refundAmount;}
    public long getRefundCount() {return refundCount;}
    public long getCompletionAmount() {return completionAmount;}
    public long getCompletionCount() {return completionCount;}
    public long getTipAmount() {return tipAmount;}
    public long getTipCount() {return tipCount;}
    public long getSurchargeAmount() {return surchargeAmount;}
    public long getSurchargeCount() {return surchargeCount;}
    public long getTotalAmount() {return totalAmount;}
    public long getTotalCount() {return totalCount;}
    public String getSchemeTotals() {return schemeTotals;}
    public long getTotalsFrom() {return totalsFrom;}
    public long getTotalsTo() {return totalsTo;}
    public Integer getLastTransStan() {return lastTransStan;}
    public long getShiftAutoClosingDateTime() {return shiftAutoClosingDateTime;}

    public void addTip(long amount) {
        tipAmount += amount;
        tipCount++;
    }

    public void addSurcharge(long amount) {
        surchargeAmount += amount;
        surchargeCount++;
    }

    public void addSale(long amount) {
        saleAmount += amount;
        saleCount++;
    }

    public void addCash(long amount) {
        cashAmount += amount;
        cashCount++;
    }

    public void addRefund(long amount) {
        refundAmount += amount;
        refundCount++;
    }

    public void addCompletion(long amount) {
        completionAmount += amount;
        completionCount++;
    }

    public void addTotal(long amount) {
        totalAmount += amount;
        totalCount++;
    }

}
