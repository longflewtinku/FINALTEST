package com.linkly.libengine.config.paycfg;

import android.content.SharedPreferences;

public class Receipt {

    private final SharedPreferences  preferences;

    public Receipt(SharedPreferences pref) {
        preferences = pref;
    }

/*    public String getLogo() {
        return preferences.getString("Logo", "");
    }

    public String getPrinter() {
        return this.printer;
    }*/

    public MerchantRec getMerchant() {
        return new MerchantRec(preferences);
    }

    public PromoRec getPromo() {
        return null;
    }

    public HelpRec getHelp() {
        return null;
    }

    public FooterRec getFooter() {
        return new FooterRec(preferences);
    }

/*    public boolean isShowResponseCodeInfo() {
        return this.showResponseCodeInfo;
    }*/

    public static class FooterRec {

        private SharedPreferences sharedPreferences;

        public FooterRec(SharedPreferences pref) {
            sharedPreferences = pref;
        }

        public String getLine1() {
            return sharedPreferences.getString("FooterLine1", "");
        }

        public String getLine2() {

            return sharedPreferences.getString("FooterLine2", "");
        }
    }

    public static class HelpRec {

        public String getLine1() {
            return "";
        }

        public String getLine2() {
            return "";
        }

        public String getLine3() {
            return "";
        }

        public String getLine4() {
            return "";
        }

        public String getLine5() {
            return "";
        }
    }

    public static class PromoRec {

        public String getLine2() {
            return "";
        }

        public String getLine3() {
            return "";
        }

        public String getLine4() {
            return "";
        }

        public String getLine5() {
            return "";
        }

        public String getLine6() {
            return "";
        }

        public String getLine7() {
            return "";
        }

        public String getLine8() {
            return "";
        }

        public String getLine9() {
            return "";
        }

        public String getLine10() {
            return "";
        }

        public String getLine1() {
            return "";
        }
    }

    public static class MerchantRec {

        private SharedPreferences preferences;

        public MerchantRec(SharedPreferences pref) {
            preferences = pref;
        }

        public void setLine0(String line) {
            preferences.edit().putString("Receipt_merchant_line0", line).apply();
        }

        public String getLine0() {
            return preferences.getString("Receipt_merchant_line0", "");
        }

        public String getLine1() {

            return preferences.getString("Receipt_merchant_line1", "");
        }

        public String getLine2() {

            return preferences.getString("Receipt_merchant_line2", "");
        }

        public String getLine3() {

            return preferences.getString("Receipt_merchant_line3", "");
        }

        public String getLine4() {

            return preferences.getString("Receipt_merchant_line4", "");
        }

        public String getLine5() {

            return preferences.getString("Receipt_merchant_line5", "");
        }

        public String getLine6() {

            return preferences.getString("Receipt_merchant_line6", "");
        }

    }
}

