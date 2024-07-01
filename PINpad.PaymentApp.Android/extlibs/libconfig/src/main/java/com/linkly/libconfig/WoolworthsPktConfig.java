package com.linkly.libconfig;

import androidx.annotation.Keep;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

@Keep
public class WoolworthsPktConfig {
    public String getTableVersion() {
        return this.tableVersion;
    }

    public List<CAPKey> getKeyList() {
        return this.keyList;
    }

    public void setTableVersion(String tableVersion) {
        this.tableVersion = tableVersion;
    }

    public void setKeyList(List<CAPKey> keyList) {
        this.keyList = keyList;
    }

    @Keep
    public static class CAPKey {
        @SerializedName("@Rid")
        private final String rid;

        @SerializedName("@Index")
        private final String index;

        @SerializedName("@Data")
        private final String modulus;

        @SerializedName("@Exponent")
        private final String exponent;

        @SerializedName("@Algorithm")
        private final String algorithm;

        @SerializedName("@Date_Effective")
        private final String dateEffective;

        @SerializedName("@Date_Expiry")
        private final String dateExpiry;

        public CAPKey( String rid,
                       String index,
                       String exponent,
                       String algorithm,
                       String dateEffective,
                       String dateExpiry,
                       String modulus ) {
            this.rid = rid;
            this.index = index;
            this.modulus = modulus;
            this.exponent = exponent;
            this.algorithm = algorithm;
            this.dateEffective = dateEffective;
            this.dateExpiry = dateExpiry;
        }

        public String getRid() {
            return this.rid;
        }

        public String getIndex() {
            return this.index;
        }

        public String getModulus() {
            return this.modulus;
        }

        public String getExponent() {
            return this.exponent;
        }

        public String getAlgorithm() {
            return this.algorithm;
        }

        public String getDateEffective() {
            return this.dateEffective;
        }

        public String getDateExpiry() {
            return this.dateExpiry;
        }
    }

    //------------------- root XML tags -------------------
    @SerializedName("@Table_Version")
    private String tableVersion;

    @SerializedName("Key")
    private List<CAPKey> keyList = new ArrayList<>();


}
