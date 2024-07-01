package com.linkly.libconfig;

import androidx.annotation.Keep;

import com.google.gson.annotations.SerializedName;

import java.util.List;

@Keep
public class WoolworthsEpatConfig {
    public String getTableVersion() {
        return this.tableVersion;
    }

    public Tlv_Tag_Emv_Default_Param getDefaultParam() {
        return this.defaultParam;
    }

    public List<Tlv_Tag_Emv_Aid_Param> getParamList() {
        return this.paramList;
    }

    public Tlv_Tag_Emv_Contactless_Aid_Param getContactlessParamList() {
        return this.contactlessParamList;
    }

    @Keep
    public static class Tlv_Tag_Emv_Def_Tags {
        @SerializedName("@Tag_Emv_Amount_Authorised_Binary")
        private String amountAuthorisedBinary;

        @SerializedName("@Tag_Emv_Amount_Other_Binary")
        private String amountOtherBinary;

        @SerializedName("@Tag_Emv_Terminal_Country_Code")
        private String terminalCountryCode;

        @SerializedName("@Tag_Emv_Terminal_Capabilities")
        private String terminalCapabilities;

        @SerializedName("@Tag_Emv_Terminal_Type")
        private String terminalType;

        @SerializedName("@Tag_Emv_Transaction_Reference_Currency_Code")
        private String transactionReferenceCurrencyCode;

        @SerializedName("@Tag_Emv_Transaction_Reference_Currency_Exponent")
        private String transactionReferenceCurrencyExponent;

        @SerializedName("@Tag_Emv_Additional_Terminal_Capabilities")
        private String additionalTerminalCapabilities;

        @SerializedName("@Tag_Emv_Merchant_Category_Code")
        private String merchantCategoryCode;

        public String getAmountAuthorisedBinary() {
            return this.amountAuthorisedBinary;
        }

        public String getAmountOtherBinary() {
            return this.amountOtherBinary;
        }

        public String getTerminalCountryCode() {
            return this.terminalCountryCode;
        }

        public String getTerminalCapabilities() {
            return this.terminalCapabilities;
        }

        public String getTerminalType() {
            return this.terminalType;
        }

        public String getTransactionReferenceCurrencyCode() {
            return this.transactionReferenceCurrencyCode;
        }

        public String getTransactionReferenceCurrencyExponent() {
            return this.transactionReferenceCurrencyExponent;
        }

        public String getAdditionalTerminalCapabilities() {
            return this.additionalTerminalCapabilities;
        }

        public String getMerchantCategoryCode() {
            return this.merchantCategoryCode;
        }
    }

    @Keep
    public static class Tlv_Tag_Emv_Default_Param {
        @SerializedName("Tlv_Tag_Emv_Def_Tags")
        private Tlv_Tag_Emv_Def_Tags tags;

        public Tlv_Tag_Emv_Def_Tags getTags() {
            return this.tags;
        }
    }

    @Keep
    public static class Tlv_Tag_Emv_Aid_Txn_Tags {
        @SerializedName("@Tag_Emv_Term_Act_Code_Default")
        private String termActCodeDefault;

        @SerializedName("@Tag_Emv_Term_Act_Code_Denial")
        private String termActCodeDenial;

        @SerializedName("@Tag_Emv_Term_Act_Code_Online")
        private String termActCodeOnline;

        @SerializedName("@Tag_Emv_Threshold")
        private String threshold;

        @SerializedName("@Tag_Emv_Target_Percent")
        private String targetPercent;

        @SerializedName("@Tag_Emv_Max_Target_Percent")
        private String maxTargetPercent;

        @SerializedName("@Tag_Emv_Ddol_Default")
        private String ddolDefault;

        @SerializedName("@Tag_Emv_Tdol_Default")
        private String tdolDefault;

        @SerializedName("@Tag_Emv_Transaction_Type")
        private String transactionType;

        @SerializedName("@Tag_Emv_Application_Version_Number_Term")
        private String applicationVersionNumberTerm;

        @SerializedName("@Tag_Emv_Terminal_Floor_Limit")
        private String terminalFloorLimit;

        @SerializedName("@Tag_Emv_Mc_Transaction_Category_Code")
        private String mcTransactionCategoryCode;

        @SerializedName("@Tag_Emv_Standin_Act_Code")
        private String standinActCode;

        @SerializedName("@Tag_Emv_Efb_Floor_Limit")
        private String efbFloorLimit;

        @SerializedName("@Tag_Emv_Pin_Bypass")
        private String pinBypass;

        @SerializedName("@Tag_Emv_Small_Value_Enabled")
        private String smallValueEnabled;

        @SerializedName("@Tag_Emv_Small_Value_Terminal_Capabilities")
        private String smallValueTerminalCapabilities;

        @SerializedName("@Tag_Emv_Terminal_Capabilities_Code")
        private String terminalCapabilitiesCode;

        @SerializedName("@Tag_Emv_Additional_Terminal_Capabilities")
        private String additionalTerminalCapabilities;

        @SerializedName("@Tag_Emv_Terminal_Transaction_Qualifiers")
        private String terminalTransactionQualifiers;

        @SerializedName("@Tag_Emv_Contactless_Max_Limit")
        private String contactlessMaxLimit;

        @SerializedName("@Tag_Emv_Contactless_CVM_Limit")
        private String contactlessCVMLimit;

        @SerializedName("@Tag_Emv_Contactless_Enabled")
        private String contactlessEnabled;

        @SerializedName("@Tag_Emv_Contactless_Terminal_Risk_Management_Data")
        private String contactlessTerminalRiskManagementData;

        public String getTermActCodeDefault() {
            return this.termActCodeDefault;
        }

        public String getTermActCodeDenial() {
            return this.termActCodeDenial;
        }

        public String getTermActCodeOnline() {
            return this.termActCodeOnline;
        }

        public String getThreshold() {
            return this.threshold;
        }

        public String getTargetPercent() {
            return this.targetPercent;
        }

        public String getMaxTargetPercent() {
            return this.maxTargetPercent;
        }

        public String getDdolDefault() {
            return this.ddolDefault;
        }

        public String getTdolDefault() {
            return this.tdolDefault;
        }

        public String getTransactionType() {
            return this.transactionType;
        }

        public String getApplicationVersionNumberTerm() {
            return this.applicationVersionNumberTerm;
        }

        public String getTerminalFloorLimit() {
            return this.terminalFloorLimit;
        }

        public String getMcTransactionCategoryCode() {
            return this.mcTransactionCategoryCode;
        }

        public String getStandinActCode() {
            return this.standinActCode;
        }

        public String getEfbFloorLimit() {
            return this.efbFloorLimit;
        }

        public String getPinBypass() {
            return this.pinBypass;
        }

        public String getSmallValueEnabled() {
            return this.smallValueEnabled;
        }

        public String getSmallValueTerminalCapabilities() {
            return this.smallValueTerminalCapabilities;
        }

        public String getTerminalCapabilitiesCode() {
            return this.terminalCapabilitiesCode;
        }

        public String getAdditionalTerminalCapabilities() {
            return this.additionalTerminalCapabilities;
        }

        public String getTerminalTransactionQualifiers() {
            return this.terminalTransactionQualifiers;
        }

        public String getContactlessMaxLimit() {
            return this.contactlessMaxLimit;
        }

        public String getContactlessCVMLimit() {
            return this.contactlessCVMLimit;
        }

        public String getContactlessEnabled() {
            return this.contactlessEnabled;
        }

        public String getContactlessTerminalRiskManagementData() {
            return this.contactlessTerminalRiskManagementData;
        }
    }

    @Keep
    public static class Tlv_Tag_Emv_Aid_Tags {
        @SerializedName("@Tlv_Tag_Emv_Aid_Card_Origin")
        private String cardOrigin;

        @SerializedName("Tlv_Tag_Emv_Aid_Txn_Tags")
        private Tlv_Tag_Emv_Aid_Txn_Tags txnTags;

        public String getCardOrigin() {
            return this.cardOrigin;
        }

        public Tlv_Tag_Emv_Aid_Txn_Tags getTxnTags() {
            return this.txnTags;
        }
    }

    @Keep
    public static class Tlv_Tag_Emv_Aid_Param {
        @SerializedName("@Tlv_Tag_Emv_Aid")
        private String aid;

        @SerializedName("@Tlv_Tag_Emv_Aid_Priority")
        private String priority;

        @SerializedName("@Tlv_Tag_Emv_Aid_App_Sel_Indic")
        private String appSelIndic;

        @SerializedName("@Tlv_Tag_Emv_Display_Text")
        private String displayText;

        @SerializedName("Tlv_Tag_Emv_Aid_Tags")
        private List<Tlv_Tag_Emv_Aid_Tags> tagList;

        public String getAid() {
            return this.aid;
        }

        public String getPriority() {
            return this.priority;
        }

        public String getAppSelIndic() {
            return this.appSelIndic;
        }

        public String getDisplayText() {
            return this.displayText;
        }

        public List<Tlv_Tag_Emv_Aid_Tags> getTagList() {
            return this.tagList;
        }
    }

    @Keep
    public static class Tlv_Tag_Emv_Contactless_Aid_Param {
        @SerializedName("Tlv_Tag_Emv_Aid_Param")
        private List<Tlv_Tag_Emv_Aid_Param> paramList;

        public List<Tlv_Tag_Emv_Aid_Param> getParamList() {
            return this.paramList;
        }
    }

    //------------------- root XML tags -------------------
    @SerializedName("@Tlv_Tag_Emv_Table_Version")
    private String tableVersion;

    @SerializedName("Tlv_Tag_Emv_Default_Param")
    private Tlv_Tag_Emv_Default_Param defaultParam;

    @SerializedName("Tlv_Tag_Emv_Aid_Param")
    private List<Tlv_Tag_Emv_Aid_Param> paramList;

    @SerializedName("Tlv_Tag_Emv_Contactless_Aid_Param")
    private Tlv_Tag_Emv_Contactless_Aid_Param contactlessParamList;
}
