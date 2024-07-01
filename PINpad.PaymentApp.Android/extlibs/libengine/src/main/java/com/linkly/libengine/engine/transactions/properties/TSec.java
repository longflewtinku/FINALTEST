package com.linkly.libengine.engine.transactions.properties;

import com.google.gson.Gson;

public class TSec {
    private String encPan; // single-encrypted in local memory
    private String encTrack2; // single-encrypted in local memory
    private String packedCardDetails; // unencrypted in local memory
    private String ksn;
    private String pinBlock;
    private String pinBlockKsn;
    private String cvv;
    private String expiryDateChip; // Application Expiry Date (EMV 5F24)

    public TSec() {}
    
    /**
     * copies TSec, returns new instance - performs deep copy using serialization/deserialization
     *
     * @param copyFrom object to copy
     * @return copy of copyFrom
     */
    public static TSec copy( TSec copyFrom ) {
        Gson gson = new Gson();
        String serializedCopy = gson.toJson(copyFrom);
        return gson.fromJson(serializedCopy, TSec.class );
    }

    /* some transactions are copied into other ones, e.g. topup-preauth to topup-completion */
    /* pin blocks shouldn't be copied over */
    public void clearForTopupCompletions() {
        encTrack2 = "";
    }

    public String getEncPan() {
        return this.encPan;
    }

    public String getEncTrack2() {
        return this.encTrack2;
    }

    public String getPackedCardDetails() {
        return this.packedCardDetails;
    }

    public String getKsn() {
        return this.ksn;
    }

    public String getPinBlock() {
        return this.pinBlock;
    }

    public String getPinBlockKsn() {
        return this.pinBlockKsn;
    }

    public String getCvv() {
        return this.cvv;
    }

    public String getExpiryDateChip() {
        return this.expiryDateChip;
    }

    public void setEncPan(String encPan) {
        this.encPan = encPan;
    }

    public void setEncTrack2(String encTrack2) {
        this.encTrack2 = encTrack2;
    }

    public void setPackedCardDetails(String packedCardDetails) {
        this.packedCardDetails = packedCardDetails;
    }

    public void setKsn(String ksn) {
        this.ksn = ksn;
    }

    public void setPinBlock(String pinBlock) {
        this.pinBlock = pinBlock;
    }

    public void setPinBlockKsn(String pinBlockKsn) {
        this.pinBlockKsn = pinBlockKsn;
    }

    public void setCvv(String cvv) {
        this.cvv = cvv;
    }

    public void setExpiryDateChip(String expiryDateChip) {
        this.expiryDateChip = expiryDateChip;
    }
}
