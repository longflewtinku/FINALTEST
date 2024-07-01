package com.linkly.libengine.engine.reporting;

import static com.linkly.libengine.engine.EngineManager.TransType;
import static com.linkly.libengine.engine.reporting.ReconciliationManager.reconciliationDao;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.linkly.libengine.engine.transactions.TransRec;
import com.linkly.libengine.engine.transactions.properties.TReconciliationFigures;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import timber.log.Timber;

@Entity(tableName = "recs", indices = { @Index("transID") })
public class Reconciliation {
    private static final String TAG = "Reconciliation";

    @Ignore
    private ArrayList<TransRec> recTransList = new ArrayList<>();
    @Ignore
    private Integer receiptNumber = -1;

    @PrimaryKey(autoGenerate = true)
    public int uid = 0;
    private Integer transID = -1;
    private Integer batchNumber = -1;

    private Amounts sale = new Amounts();
    private Amounts vas  = new Amounts();
    private Amounts refund  = new Amounts();
    private Amounts cash = new Amounts();
    private Amounts tips = new Amounts();
    private Amounts cashback = new Amounts();
    private Amounts preauth = new Amounts();
    private Amounts completion = new Amounts();
    private Amounts deposit = new Amounts();
    private Amounts surcharge = new Amounts();

    private long subTotalCount = 0;
    private long subTotalAmount = 0;
    private long totalCount = 0;
    private long totalAmount = 0;

    private long onlineTotalCount = 0;
    private long onlineTotalAmount = 0;
    private long offlineTotalCount = 0;
    private long offlineTotalAmount = 0;

    // reconciliation figures
    private TReconciliationFigures reconciliationFigures = new TReconciliationFigures();
    private TReconciliationFigures prevReconciliationFigures = new TReconciliationFigures();
    private TReconciliationFigures curReconciliationFigures = new TReconciliationFigures();

    long startTran = 0;
    long endTran = 0;
    long startReceiptTran = 0;
    long endReceiptTran = 0;
    private String previousSchemeTotalsData = "";

    public Reconciliation() {
        // creates ReconciliationManager singleton instance if not already - opens database
        ReconciliationManager.getInstance();
    }

    public Reconciliation( boolean unitTest ) {
        if( !unitTest ) {
            ReconciliationManager.getInstance();
        }
    }

    public HashMap<String, CardSchemeTotals> expandStringIntoSchemeTotals(String schemeTotals) {
        Gson gson = new Gson();
        HashMap<String, CardSchemeTotals> cardSchemeTotals = new HashMap<>();

        Type cardSchemeTotalsType = new TypeToken<ArrayList<CardSchemeTotals>>(){}.getType();
        ArrayList<CardSchemeTotals> list =  gson.fromJson(schemeTotals, cardSchemeTotalsType);

        if( null != list ) {
            for (CardSchemeTotals entry: list) {
                cardSchemeTotals.put( entry.name, entry );
            }
        }

        return cardSchemeTotals;
    }

    public ArrayList<Reconciliation.CardSchemeTotals> getPreviousSchemeTotalsAsArray() {
        Gson gson = new Gson();
        ArrayList<Reconciliation.CardSchemeTotals> cardSchemeTotals = new ArrayList<Reconciliation.CardSchemeTotals>();

        Type cardSchemeTotalsType = new TypeToken<ArrayList<CardSchemeTotals>>(){}.getType();
        ArrayList<CardSchemeTotals> list =  gson.fromJson(previousSchemeTotalsData, cardSchemeTotalsType);

        if( list != null ) {
            cardSchemeTotals.addAll(list);
        }

        return cardSchemeTotals;
    }


    public HashMap<String, CardSchemeTotals> getPreviousSchemeTotals() {
        return expandStringIntoSchemeTotals(previousSchemeTotalsData);
    }

    public void setPreviousSchemeTotals(HashMap<String, CardSchemeTotals> totals) {

        previousSchemeTotalsData = "";
        if (totals == null || totals.size() == 0)
            return;

        ArrayList<Reconciliation.CardSchemeTotals> list = new ArrayList<>();
        list.addAll(totals.values());

        // Convert the object list to a JSON string
        previousSchemeTotalsData = new Gson().toJson(list);
    }

    @SuppressWarnings("rawtypes")
    public void addSchemeTotals(String schemeTotals) {

        HashMap<String, CardSchemeTotals> currentSchemeTotals = getPreviousSchemeTotals();
        HashMap<String, CardSchemeTotals> newSchemeTotals = expandStringIntoSchemeTotals(schemeTotals);

        Iterator it = newSchemeTotals.entrySet().iterator();
        while (it.hasNext()) {
            HashMap.Entry pair = (HashMap.Entry)it.next();
            String additionalTotalsName  = (String)pair.getKey();
            CardSchemeTotals additionalTotals  = (CardSchemeTotals)pair.getValue();


            CardSchemeTotals existingTotals = currentSchemeTotals.get(additionalTotalsName);
            if (existingTotals != null) {
                existingTotals.add(additionalTotals);
            } else {
                currentSchemeTotals.put(additionalTotalsName, additionalTotals);
            }
        }

        /* set the string with the updated values */
        setPreviousSchemeTotals(currentSchemeTotals);
    }

    public void AddValues(Reconciliation rec) {
        sale.add(rec.sale);
        refund.add(rec.refund);
        cash.add(rec.cash);
        tips.add(rec.tips);
        cashback.add(rec.cashback);
        preauth.add(rec.preauth);
        completion.add(rec.completion);
        vas.add(rec.vas);
        deposit.add(rec.deposit);

        subTotalCount += rec.getSubTotalCount();
        subTotalAmount += rec.getSubTotalAmount();
        totalCount += rec.getTotalCount();
        totalAmount += rec.getTotalAmount();
        onlineTotalCount += rec.getOnlineTotalCount();
        onlineTotalAmount += rec.getOnlineTotalAmount();
        offlineTotalCount += rec.getOfflineTotalCount();
        offlineTotalAmount += rec.getOfflineTotalAmount();

        addSchemeTotals(rec.previousSchemeTotalsData);
    }

    public void save(TransRec trans) {


        // save if trans id is set, and trans type is reconciliation or summary report or pre-reconciliation
        if (trans.getUid() > 0 && (trans.getTransType() == TransType.RECONCILIATION || trans.getTransType() == TransType.SUMMARY || trans.isPreReconciliation())) {
            this.transID = trans.getUid();

            if( this.uid == 0 ) {
                this.uid = (int)reconciliationDao.insert(this);
            } else {
                reconciliationDao.update(this);
            }
        }
    }

    public ArrayList<TransRec> getRecTransList() {
        return this.recTransList;
    }

    public Integer getReceiptNumber() {
        return this.receiptNumber;
    }

    public int getUid() {
        return this.uid;
    }

    public Integer getTransID() {
        return this.transID;
    }

    public Integer getBatchNumber() {
        return this.batchNumber;
    }

    public Amounts getSale() {
        return this.sale;
    }

    public Amounts getVas() {
        return this.vas;
    }

    public Amounts getRefund() {
        return this.refund;
    }

    public Amounts getCash() {
        return this.cash;
    }

    public Amounts getTips() {
        return this.tips;
    }

    public Amounts getCashback() {
        return this.cashback;
    }

    public Amounts getPreauth() {
        return this.preauth;
    }

    public Amounts getCompletion() {
        return this.completion;
    }

    public Amounts getDeposit() {
        return this.deposit;
    }

    public Amounts getSurcharge() {
        return this.surcharge;
    }

    public long getSubTotalCount() {
        return this.subTotalCount;
    }

    public long getSubTotalAmount() {
        return this.subTotalAmount;
    }

    public long getTotalCount() {
        return this.totalCount;
    }

    public long getTotalAmount() {
        return this.totalAmount;
    }

    public long getOnlineTotalCount() {
        return this.onlineTotalCount;
    }

    public long getOnlineTotalAmount() {
        return this.onlineTotalAmount;
    }

    public long getOfflineTotalCount() {
        return this.offlineTotalCount;
    }

    public long getOfflineTotalAmount() {
        return this.offlineTotalAmount;
    }

    public TReconciliationFigures getReconciliationFigures() {
        return this.reconciliationFigures;
    }

    public TReconciliationFigures getPrevReconciliationFigures() {
        return this.prevReconciliationFigures;
    }

    public TReconciliationFigures getCurReconciliationFigures() {
        return this.curReconciliationFigures;
    }

    public long getStartTran() {
        return this.startTran;
    }

    public long getEndTran() {
        return this.endTran;
    }

    public long getStartReceiptTran() {
        return this.startReceiptTran;
    }

    public long getEndReceiptTran() {
        return this.endReceiptTran;
    }

    public String getPreviousSchemeTotalsData() {
        return this.previousSchemeTotalsData;
    }

    public void setRecTransList(ArrayList<TransRec> recTransList) {
        this.recTransList = recTransList;
    }

    public void setReceiptNumber(Integer receiptNumber) {
        this.receiptNumber = receiptNumber;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public void setTransID(Integer transID) {
        this.transID = transID;
    }

    public void setBatchNumber(Integer batchNumber) {
        this.batchNumber = batchNumber;
    }

    public void setSale(Amounts sale) {
        this.sale = sale;
    }

    public void setVas(Amounts vas) {
        this.vas = vas;
    }

    public void setRefund(Amounts refund) {
        this.refund = refund;
    }

    public void setCash(Amounts cash) {
        this.cash = cash;
    }

    public void setTips(Amounts tips) {
        this.tips = tips;
    }

    public void setCashback(Amounts cashback) {
        this.cashback = cashback;
    }

    public void setPreauth(Amounts preauth) {
        this.preauth = preauth;
    }

    public void setCompletion(Amounts completion) {
        this.completion = completion;
    }

    public void setDeposit(Amounts deposit) {
        this.deposit = deposit;
    }

    public void setSurcharge(Amounts surcharge) {
        this.surcharge = surcharge;
    }

    public void setSubTotalCount(long subTotalCount) {
        this.subTotalCount = subTotalCount;
    }

    public void setSubTotalAmount(long subTotalAmount) {
        this.subTotalAmount = subTotalAmount;
    }

    public void setTotalCount(long totalCount) {
        this.totalCount = totalCount;
    }

    public void setTotalAmount(long totalAmount) {
        this.totalAmount = totalAmount;
    }

    public void setOnlineTotalCount(long onlineTotalCount) {
        this.onlineTotalCount = onlineTotalCount;
    }

    public void setOnlineTotalAmount(long onlineTotalAmount) {
        this.onlineTotalAmount = onlineTotalAmount;
    }

    public void setOfflineTotalCount(long offlineTotalCount) {
        this.offlineTotalCount = offlineTotalCount;
    }

    public void setOfflineTotalAmount(long offlineTotalAmount) {
        this.offlineTotalAmount = offlineTotalAmount;
    }

    public void setReconciliationFigures(TReconciliationFigures reconciliationFigures) {
        this.reconciliationFigures = reconciliationFigures;
    }

    public void setPrevReconciliationFigures(TReconciliationFigures prevReconciliationFigures) {
        this.prevReconciliationFigures = prevReconciliationFigures;
    }

    public void setCurReconciliationFigures(TReconciliationFigures curReconciliationFigures) {
        this.curReconciliationFigures = curReconciliationFigures;
    }

    public void setStartTran(long startTran) {
        this.startTran = startTran;
    }

    public void setEndTran(long endTran) {
        this.endTran = endTran;
    }

    public void setStartReceiptTran(long startReceiptTran) {
        this.startReceiptTran = startReceiptTran;
    }

    public void setEndReceiptTran(long endReceiptTran) {
        this.endReceiptTran = endReceiptTran;
    }

    public void setPreviousSchemeTotalsData(String previousSchemeTotalsData) {
        this.previousSchemeTotalsData = previousSchemeTotalsData;
    }

    @SuppressWarnings("java:S1104") /*Make xxx a static final constant or non-public and provide accessors if needed*/
    public static class CardSchemeTotals {
        public String name;
        public long purchaseAmount;
        public long purchaseCount;
        public long cashoutAmount;
        public long cashoutCount;
        public long refundAmount;
        public long refundCount;
        public long completionAmount;
        public long completionCount;
        public long totalAmount;
        public long totalCount;
        public long surchargeAmount;
        public long surchargeCount;

        public CardSchemeTotals() {
            name = "";
            purchaseAmount = 0;
            purchaseCount = 0;
            cashoutAmount = 0;
            cashoutCount = 0;
            refundAmount = 0;
            refundCount = 0;
            completionAmount = 0;
            completionCount = 0;
            totalAmount = 0;
            totalCount = 0;
            surchargeAmount = 0;
            surchargeCount = 0;
        }

        @SuppressWarnings("java:S107") /*Constructor has 13 parameters, which is greater than 7 authorized*/
        public CardSchemeTotals( String name, long purchaseAmount, long purchaseCount, long cashoutAmount, long cashoutCount, long refundAmount, long refundCount, long completionAmount, long completionCount,long totalAmount, long totalCount , long surchargeAmount, long surchargeCount) {
            this.name = name;
            this.purchaseAmount = purchaseAmount;
            this.purchaseCount = purchaseCount;
            this.cashoutAmount = cashoutAmount;
            this.cashoutCount = cashoutCount;
            this.refundAmount = refundAmount;
            this.refundCount = refundCount;
            this.completionAmount = completionAmount;
            this.completionCount = completionCount;
            this.totalAmount = totalAmount;
            this.totalCount = totalCount;
            this.surchargeAmount = surchargeAmount;
            this.surchargeCount = surchargeCount;
        }

        public void add(CardSchemeTotals additional) {
            purchaseAmount += additional.purchaseAmount;
            purchaseCount += additional.purchaseCount;
            cashoutAmount += additional.cashoutAmount;
            cashoutCount += additional.cashoutCount;
            refundAmount += additional.refundAmount;
            refundCount += additional.refundCount;
            completionAmount += additional.refundAmount;
            completionCount += additional.refundCount;
            totalAmount += additional.totalAmount;
            totalCount += additional.totalCount;
            surchargeAmount += additional.surchargeAmount;
            surchargeCount += additional.surchargeCount;
        }
    }

    public void debug() {
        //Root
        String log = "";
        log += "=================== RECONCILIATION DEBUG ===============" + "\n";
        log += "uid: " + uid + "\n";
        log += "transID: " + transID.toString() + "\n";
        log += "batchNumber: " + batchNumber.toString() + "\n";
        log += "=================== totals by txn info ===============" + "\n";
        log += "sale: " + sale.amountsToString(sale) + "\n";
        log += "vas: " + vas.amountsToString(vas) + "\n";
        log += "refund: " + refund.amountsToString(refund) + "\n";
        log += "cash: " + cash.amountsToString(cash) + "\n";
        log += "tips: " + tips.amountsToString(tips) + "\n";
        log += "cashback: " + cashback.amountsToString(cashback) + "\n";
        log += "preauth: " + preauth.amountsToString(preauth) + "\n";
        log += "completion: " + completion.amountsToString(completion) + "\n";
        log += "deposit: " + deposit.amountsToString(deposit) + "\n";
        log += "=================== totals summary info ===============" + "\n";
        log += "subTotalCount: " + subTotalCount + "\n";
        log += "subTotalAmount: " + subTotalAmount + "\n";
        log += "totalCount: " + totalCount + "\n";
        log += "totalAmount: " + totalAmount + "\n";
        log += "onlineTotalCount: " + onlineTotalCount + "\n";
        log += "onlineTotalAmount: " + onlineTotalAmount + "\n";
        log += "offlineTotalCount: " + offlineTotalCount + "\n";
        log += "offlineTotalAmount: " + offlineTotalAmount + "\n";
        log += "=================== tran coverage info ===============" + "\n";
        log += "startTran: " + startTran + "\n";
        log += "endTran: " + endTran + "\n";
        log += "startReceiptTran: " + startReceiptTran + "\n";
        log += "endReceiptTran: " + endReceiptTran + "\n";

        Timber.i( log);
    }
}
