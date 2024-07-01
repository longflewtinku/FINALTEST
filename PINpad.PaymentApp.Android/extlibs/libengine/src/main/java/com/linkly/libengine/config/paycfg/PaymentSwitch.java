package com.linkly.libengine.config.paycfg;

import android.content.SharedPreferences;

// A glorified wrapper. that holds our shared preferences.
// But keeps the original structure of our code. When used.
public class PaymentSwitch {

    SharedPreferences paymentSwitchPreferences;

    // Easiest way to refactor is to remove the payment switch data and just reference the preference that it is stored
    public PaymentSwitch(SharedPreferences preferences) {
        paymentSwitchPreferences = preferences;
    }

    public Ip getIp() {
        return new Ip(paymentSwitchPreferences);
    }

    public int getDialTimeout() {
        return paymentSwitchPreferences.getInt("DialTimeout", 30);
    }

    public int getSendTimeout() {
        return paymentSwitchPreferences.getInt("SendTimeout", 30);
    }

    public int getReceiveTimeout() {
        return paymentSwitchPreferences.getInt("ReceiveTimeout", 30);
    }

    public boolean isUseSsl() {
        return paymentSwitchPreferences.getBoolean("UseSsl", false);
    }

    public boolean isClientAuth() {
        return paymentSwitchPreferences.getBoolean("ClientAuth", false);
    }

    public boolean isDisableSecurity() {
        return paymentSwitchPreferences.getBoolean("DisableSecurity", false);
    }

    public String getCertificateFile() {
        return paymentSwitchPreferences.getString("CertificateFile", "");
    }

    public String getPrivateKeyFile() {
        return paymentSwitchPreferences.getString("PrivateKeyFile", "");
    }

    public String getPrivateKeyPassword() {
        return paymentSwitchPreferences.getString("PrivateKeyPassword", "");
    }

    public String getPrivateKeyCertificate() {
        return paymentSwitchPreferences.getString("PrivateKeyCertificate", "");
    }

    public Pstn getPstn() {
        return new Pstn(paymentSwitchPreferences);
    }

    public String getAiic() {
        return paymentSwitchPreferences.getString("Aiic", "");
    }

    public String getNii() {
        return paymentSwitchPreferences.getString("Nii", "");
    }

    public String getCommsType() {
        return paymentSwitchPreferences.getString("CommsType", "");
    }

    public String getIpGatewayHost() {
        return paymentSwitchPreferences.getString("IpGatewayHost", "");
    }

    public String getIpGatewayUser() {
        return paymentSwitchPreferences.getString("IpGatewayUser", "");
    }

    public String getIpGatewayPwd() {
        return paymentSwitchPreferences.getString("IpGatewayPwd", "");
    }

    public String getDefaultCpatVersion() {
        return paymentSwitchPreferences.getString("DefaultCpatVersion", "");
    }

    public String getDefaultPktVersion() {
        return paymentSwitchPreferences.getString("DefaultPktVersion", "");
    }

    public String getDefaultEpatVersion() {
        return paymentSwitchPreferences.getString("DefaultEpatVersion", "");
    }

    public String getDefaultSpotVersion() {
        return paymentSwitchPreferences.getString("DefaultSpotVersion", "");
    }

    public String getDefaultFcatVersion() {
        return paymentSwitchPreferences.getString("DefaultFcatVersion", "");
    }

    public void setDialTimeout(int timeout) {
        paymentSwitchPreferences.edit().putInt("DialTimeout", timeout).apply();
    }

    public void setReceiveTimeout(int timeout) {
        paymentSwitchPreferences.edit().putInt("ReceiveTimeout", timeout).apply();
    }

    public void setSendTimeout(int timeout) {
        paymentSwitchPreferences.edit().putInt("SendTimeout", timeout).apply();
    }

    public static class Pstn {
        private SharedPreferences preferencesPstn;

        public Pstn(SharedPreferences sharedPreferences) {
            preferencesPstn = sharedPreferences;
        }

        public String getPhone() {
            return preferencesPstn.getString("Phone", "");
        }

        public String getPhone2nd() {
            return preferencesPstn.getString("Phone2nd", "");
        }
    }

    public static class Ip {
        SharedPreferences ipPreferences;
        public Ip(SharedPreferences preferences) {
            ipPreferences = preferences;
        }

        public String getHost() {
            return ipPreferences.getString("Host", "");
        }

        public String getHost2nd() {
            return ipPreferences.getString("Host2nd", "");
        }
    }
}