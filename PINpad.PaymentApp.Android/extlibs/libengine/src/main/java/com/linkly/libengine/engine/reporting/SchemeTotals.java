package com.linkly.libengine.engine.reporting;

/**
 * The class defining the scheme totals. This scheme totals don't have cash, refund, completion and totals splits. Its usually used for scheme totals send from host. For detailed
 * scheme totals make use {@link Reconciliation.CardSchemeTotals}
 */
public class SchemeTotals {
    /**
     * Used as an identifier for the schemes
     */
    String cardNameIndex;
    /**
     * Total number of credit transaction performed. This consist only of refund transactions and excludes reversed transactions
     */
    Long creditNumber;
    /**
     * Total amount of credit transaction performed. This consist only of refund transactions and excludes reversed transactions
     */
    Long creditAmount;
    /**
     * Total number of debit transaction performed. This is inclusive of purchase, purchase + cash, cashout and completion transactions and excludes reversed transactions
     */
    Long debitNumber;
    /**
     * Total amount of debit transaction performed. This is inclusive of purchase, purchase + cash, cashout and completion transactions and excludes reversed transactions
     */
    Long debitAmount;

    public String getCardNameIndex() {
        return this.cardNameIndex;
    }

    public Long getCreditNumber() {
        return this.creditNumber;
    }

    public Long getCreditAmount() {
        return this.creditAmount;
    }

    public Long getDebitNumber() {
        return this.debitNumber;
    }

    public Long getDebitAmount() {
        return this.debitAmount;
    }

    public void setCardNameIndex(String cardNameIndex) {
        this.cardNameIndex = cardNameIndex;
    }

    public void setCreditNumber(Long creditNumber) {
        this.creditNumber = creditNumber;
    }

    public void setCreditAmount(Long creditAmount) {
        this.creditAmount = creditAmount;
    }

    public void setDebitNumber(Long debitNumber) {
        this.debitNumber = debitNumber;
    }

    public void setDebitAmount(Long debitAmount) {
        this.debitAmount = debitAmount;
    }
}