package com.linkly.libengine.engine.reporting;

import static com.linkly.libengine.engine.reporting.TotalsManager.totalsDao;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.linkly.libengine.engine.EngineManager;

import java.util.List;

import timber.log.Timber;

@Entity(tableName = "totals", indices = {@Index(value = {"transType","cardName"})})
public class Totals {

    private static final String TAG = "Totals";

    @PrimaryKey(autoGenerate = true)
    public int uid = 0;
    String cardName = "";
    long netAmount = 0;
    long netCount = 0;
    EngineManager.TransType transType;
    boolean isGroupTotal = false;
    long debitAmount = 0;
    long debitCount = 0;
    long creditAmount = 0;
    long creditCount = 0;
    long firstTranID = 0;
    long lastTranID = 0;

    public Totals() {
        // creates TotalsManager singleton instance if not already - opens database
        TotalsManager.getInstance();
    }

    @Ignore
    public Totals(String cardName, EngineManager.TransType transType, boolean isGroupTotal, long netAmount, long netCount, long debitAmount, long debitCount, long creditAmount, long creditCount, long firstTranID, long lastTranID ) {
        this.cardName = cardName;
        this.netAmount = netAmount;
        this.netCount = netCount;
        this.transType = transType;
        this.isGroupTotal = isGroupTotal;
        this.debitAmount = debitAmount;
        this.debitCount = debitCount;
        this.creditAmount = creditAmount;
        this.creditCount = creditCount;
        this.firstTranID = firstTranID;
        this.lastTranID = lastTranID;
    }

    public static void clearTotals() {
        totalsDao.deleteAll();
    }

    private void addAmount(long amount, long count) {
        this.netAmount += amount;
        this.netCount += count;
    }

    private void addDebitAmount(long amount, long count) {
        this.debitAmount += amount;
        this.debitCount += count;
    }

    private void addCreditAmount(long amount, long count) {
        this.creditAmount += amount;
        this.creditCount += count;
    }

    private void updateFirstTranID(long newMessageID) {
        if ((newMessageID < firstTranID) || firstTranID == 0) {
            firstTranID = newMessageID;
        }
    }

    private void updateLastTranID(long newMessageID) {
        if ((newMessageID > lastTranID) || lastTranID == 0) {
            lastTranID = newMessageID;
        }
    }

    private void addToTotals(long amount, long count, long debitAmount, long debitCount, long creditAmount, long creditCount, long firstTranID, long lastTranID) {
        addAmount(amount, count);
        addDebitAmount(debitAmount, debitCount);
        addCreditAmount(creditAmount, creditCount);
        updateFirstTranID(firstTranID);
        updateLastTranID(lastTranID);
    }

    private void addToTotals(long amount, int newTranMsgId) {
        long debitAmount = 0, creditAmount = 0, debitCount = 0, creditCount = 0;
        if(amount > 0) {
            debitAmount = amount;
            debitCount = 1;
        } else {
            creditAmount = -amount;
            creditCount = 1;
        }
        addToTotals(amount, 1, debitAmount, debitCount, creditAmount, creditCount, newTranMsgId, newTranMsgId);
    }

    public void addToTotals(Totals totalsData) {
        addToTotals(totalsData.getNetAmount(),    totalsData.getNetCount(),
                    totalsData.getDebitAmount(),  totalsData.getDebitCount(),
                    totalsData.getCreditAmount(), totalsData.getCreditCount(),
                    totalsData.getFirstTranID(),  totalsData.getLastTranID());
    }

    public static List<Totals> getCardTypeGroupedTotals() {
        return totalsDao.groupByCardName();
    }

    public static List<Totals> getTransTypeGroupedTotals() {
        return totalsDao.groupByTransType();
    }

    public static boolean updateTotalCount(EngineManager.TransType transType, String cardName, long amount, int messageID) {
        // search on trans type and card name
        Totals totals = null;
        List<Totals> totalsList = null;

        try {
            totalsList = totalsDao.findByTransTypeAndCardName(transType.ordinal(), cardName);
        } catch ( Exception e) {
            Timber.w(e);
        }

        if (totalsList != null && totalsList.size() > 0) {

            totals = totalsList.get(0);
        } else {
            //Not found; create new totals object
            totals = new Totals();
            totals.setCardName(cardName);
            totals.setTransType(transType);
        }

        totals.addToTotals(amount, messageID);

        totalsDao.insert(totals);

        return true;
    }

    public int getUid() {
        return this.uid;
    }

    public String getCardName() {
        return this.cardName;
    }

    public long getNetAmount() {
        return this.netAmount;
    }

    public long getNetCount() {
        return this.netCount;
    }

    public EngineManager.TransType getTransType() {
        return this.transType;
    }

    public boolean isGroupTotal() {
        return this.isGroupTotal;
    }

    public long getDebitAmount() {
        return this.debitAmount;
    }

    public long getDebitCount() {
        return this.debitCount;
    }

    public long getCreditAmount() {
        return this.creditAmount;
    }

    public long getCreditCount() {
        return this.creditCount;
    }

    public long getFirstTranID() {
        return this.firstTranID;
    }

    public long getLastTranID() {
        return this.lastTranID;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public void setCardName(String cardName) {
        this.cardName = cardName;
    }

    public void setNetAmount(long netAmount) {
        this.netAmount = netAmount;
    }

    public void setNetCount(long netCount) {
        this.netCount = netCount;
    }

    public void setTransType(EngineManager.TransType transType) {
        this.transType = transType;
    }

    public void setGroupTotal(boolean isGroupTotal) {
        this.isGroupTotal = isGroupTotal;
    }

    public void setDebitAmount(long debitAmount) {
        this.debitAmount = debitAmount;
    }

    public void setDebitCount(long debitCount) {
        this.debitCount = debitCount;
    }

    public void setCreditAmount(long creditAmount) {
        this.creditAmount = creditAmount;
    }

    public void setCreditCount(long creditCount) {
        this.creditCount = creditCount;
    }

    public void setFirstTranID(long firstTranID) {
        this.firstTranID = firstTranID;
    }

    public void setLastTranID(long lastTranID) {
        this.lastTranID = lastTranID;
    }
}
