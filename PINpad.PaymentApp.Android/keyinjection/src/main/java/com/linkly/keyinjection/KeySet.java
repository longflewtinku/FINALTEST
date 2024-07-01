package com.linkly.keyinjection;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class KeySet {
    @SerializedName("keys")
    public ArrayList<KeyVal> key;

     public static class KeyVal {

        public String skTcuMod;
        public String skTcuExp;
        public String skManPkTcuMod;
        public String skManPkTcuExp;
        public String ppid;
        public String description;
        public String dukptInitialKey;
        public String dukptInitialKsn;
         public boolean dukptRandomTrsm;
         public String dukptIssuerId;
         public String dukptBdkIdx;
         public String dukptVendorId;
         public String dukptBdk;
         public int dukptKeySlot;
         public boolean keepCurrentKeys;
    }
}
