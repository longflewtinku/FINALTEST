package com.linkly.libengine.engine.transactions.properties;

/**
 * Reconciliation amounts/numbers
 */

import androidx.room.TypeConverter;

import com.google.gson.Gson;

public class TReconciliationFigures {

    // DE-74, 75, 76, 77, 81
    private long creditsNumber = 0;
    private long creditsReversalNumber = 0;
    private long debitsNumber = 0;
    private long debitsReversalNumber = 0;
    private long authorisationsNumber = 0;
    private long cashoutsNumber = 0;

    // DE-86, 87, 88, 89
    private long creditsAmount = 0;
    private long creditsReversalAmount = 0;
    private long debitsAmount = 0;
    private long debitsReversalAmount = 0;
    private long cashoutsAmount = 0;

    // DE-97
    private long netReconciliationAmount = 0;

    private long netReconciliationNumber = 0;

    public void incCreditsNumber() {
        creditsNumber++;
    }
    public void addCreditsAmount(long amount) {
        creditsAmount += amount;
    }

    public void incDebitsNumber() {
        debitsNumber++;
    }
    public void addDebitsAmount(long amount) {
        debitsAmount += amount;
    }

    public void incCashoutsNumber() { cashoutsNumber++; }
    public void addCashoutsAmount(long amount) { cashoutsAmount += amount; }

    public void AddFigures(TReconciliationFigures figures) {
        creditsNumber += figures.getCreditsNumber();
        creditsReversalNumber += figures.getCreditsReversalNumber();
        debitsNumber += figures.getDebitsNumber();
        debitsReversalNumber += figures.getDebitsReversalNumber();
        authorisationsNumber += figures.getAuthorisationsNumber();
        cashoutsNumber += figures.getCashoutsNumber();
        creditsAmount += figures.getCreditsAmount();
        creditsReversalAmount += figures.getCreditsReversalAmount();
        debitsAmount += figures.getDebitsAmount();
        debitsReversalAmount += figures.getDebitsReversalAmount();
        cashoutsAmount += figures.getCashoutsAmount();
        netReconciliationAmount += figures.getNetReconciliationAmount();
        netReconciliationNumber += figures.getNetReconciliationNumber();
    }

    @TypeConverter
    public String objectToString(TReconciliationFigures privileges) {
        Gson gson = new Gson();
        String ret = gson.toJson(privileges);

        return ret;
    }

    @TypeConverter
    public TReconciliationFigures stringToObject( String serialisedObject ) {
        Gson gson = new Gson();
        TReconciliationFigures ret = gson.fromJson(serialisedObject, TReconciliationFigures.class);


        return ret;
    }

    public long getCreditsNumber() {
        return this.creditsNumber;
    }

    public long getCreditsReversalNumber() {
        return this.creditsReversalNumber;
    }

    public long getDebitsNumber() {
        return this.debitsNumber;
    }

    public long getDebitsReversalNumber() {
        return this.debitsReversalNumber;
    }

    public long getAuthorisationsNumber() {
        return this.authorisationsNumber;
    }

    public long getCashoutsNumber() {
        return this.cashoutsNumber;
    }

    public long getCreditsAmount() {
        return this.creditsAmount;
    }

    public long getCreditsReversalAmount() {
        return this.creditsReversalAmount;
    }

    public long getDebitsAmount() {
        return this.debitsAmount;
    }

    public long getDebitsReversalAmount() {
        return this.debitsReversalAmount;
    }

    public long getCashoutsAmount() {
        return this.cashoutsAmount;
    }

    public long getNetReconciliationAmount() {
        return this.netReconciliationAmount;
    }

    public long getNetReconciliationNumber() {
        return this.netReconciliationNumber;
    }

    public void setCreditsNumber(long creditsNumber) {
        this.creditsNumber = creditsNumber;
    }

    public void setCreditsReversalNumber(long creditsReversalNumber) {
        this.creditsReversalNumber = creditsReversalNumber;
    }

    public void setDebitsNumber(long debitsNumber) {
        this.debitsNumber = debitsNumber;
    }

    public void setDebitsReversalNumber(long debitsReversalNumber) {
        this.debitsReversalNumber = debitsReversalNumber;
    }

    public void setAuthorisationsNumber(long authorisationsNumber) {
        this.authorisationsNumber = authorisationsNumber;
    }

    public void setCashoutsNumber(long cashoutsNumber) {
        this.cashoutsNumber = cashoutsNumber;
    }

    public void setCreditsAmount(long creditsAmount) {
        this.creditsAmount = creditsAmount;
    }

    public void setCreditsReversalAmount(long creditsReversalAmount) {
        this.creditsReversalAmount = creditsReversalAmount;
    }

    public void setDebitsAmount(long debitsAmount) {
        this.debitsAmount = debitsAmount;
    }

    public void setDebitsReversalAmount(long debitsReversalAmount) {
        this.debitsReversalAmount = debitsReversalAmount;
    }

    public void setCashoutsAmount(long cashoutsAmount) {
        this.cashoutsAmount = cashoutsAmount;
    }

    public void setNetReconciliationAmount(long netReconciliationAmount) {
        this.netReconciliationAmount = netReconciliationAmount;
    }

    public void setNetReconciliationNumber(long netReconciliationNumber) {
        this.netReconciliationNumber = netReconciliationNumber;
    }
}
