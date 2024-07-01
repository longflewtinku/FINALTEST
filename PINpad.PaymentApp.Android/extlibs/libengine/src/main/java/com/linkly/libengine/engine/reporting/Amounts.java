package com.linkly.libengine.engine.reporting;

import androidx.room.TypeConverter;

import com.google.gson.Gson;

public class Amounts {
    public long amount;
    public long count;
    public long reversalAmount;
    public long reversalCount;

    public Amounts() {
        amount = 0;
        count = 0;
        reversalAmount = 0;
        reversalCount = 0;
    }

    public Amounts( long amount, long count, long reversalAmount, long reversalCount ) {
        this.amount = amount;
        this.count = count;
        this.reversalAmount = reversalAmount;
        this.reversalCount = reversalCount;
    }

    public void add( Amounts amountsToAdd ) {
        this.amount += amountsToAdd.amount;
        this.count += amountsToAdd.count;
        this.reversalAmount += amountsToAdd.reversalAmount;
        this.reversalCount += amountsToAdd.reversalCount;
    }

    public void updateAmounts( int revCount, int authCount, long amount ) {
        this.amount += (authCount * amount);
        this.count += authCount;
        this.reversalAmount += (amount * revCount);
        this.reversalCount += revCount;
    }

    @TypeConverter
    public String amountsToString( Amounts input ) {
        Gson gson = new Gson();
        String ret = gson.toJson(input);
        return ret;
    }

    @TypeConverter
    public Amounts stringToAmounts( String serialisedObject ) {
        Gson gson = new Gson();
        Amounts ret = gson.fromJson(serialisedObject, Amounts.class);
        return ret;
    }
}
