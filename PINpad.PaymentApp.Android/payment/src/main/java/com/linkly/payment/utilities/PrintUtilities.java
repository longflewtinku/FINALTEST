package com.linkly.payment.utilities;

import static android.net.wifi.WifiManager.WIFI_STATE_ENABLED;
import static android.telephony.TelephonyManager.NETWORK_TYPE_1xRTT;
import static android.telephony.TelephonyManager.NETWORK_TYPE_CDMA;
import static android.telephony.TelephonyManager.NETWORK_TYPE_EDGE;
import static android.telephony.TelephonyManager.NETWORK_TYPE_EHRPD;
import static android.telephony.TelephonyManager.NETWORK_TYPE_EVDO_0;
import static android.telephony.TelephonyManager.NETWORK_TYPE_EVDO_A;
import static android.telephony.TelephonyManager.NETWORK_TYPE_EVDO_B;
import static android.telephony.TelephonyManager.NETWORK_TYPE_GPRS;
import static android.telephony.TelephonyManager.NETWORK_TYPE_GSM;
import static android.telephony.TelephonyManager.NETWORK_TYPE_HSDPA;
import static android.telephony.TelephonyManager.NETWORK_TYPE_HSPA;
import static android.telephony.TelephonyManager.NETWORK_TYPE_HSPAP;
import static android.telephony.TelephonyManager.NETWORK_TYPE_HSUPA;
import static android.telephony.TelephonyManager.NETWORK_TYPE_IDEN;
import static android.telephony.TelephonyManager.NETWORK_TYPE_IWLAN;
import static android.telephony.TelephonyManager.NETWORK_TYPE_LTE;
import static android.telephony.TelephonyManager.NETWORK_TYPE_NR;
import static android.telephony.TelephonyManager.NETWORK_TYPE_TD_SCDMA;
import static android.telephony.TelephonyManager.NETWORK_TYPE_UMTS;
import static android.telephony.TelephonyManager.NETWORK_TYPE_UNKNOWN;
import static com.linkly.libengine.printing.Receipt.MEDIUM_FONT;
import static com.linkly.libengine.printing.Receipt.SMALL_FONT;
import static com.linkly.libengine.printing.Receipt.getText;
import static com.linkly.libmal.global.util.Util.GetCellularNetworkName;
import static com.linkly.libmal.global.util.Util.GetCellularSignalStrength;
import static com.linkly.libmal.global.util.Util.GetWifiGateway;
import static com.linkly.libmal.global.util.Util.GetWifiIPAddress;
import static com.linkly.libmal.global.util.Util.GetWifiSignalStrength;
import static com.linkly.libmal.global.util.Util.GetWifiSubnetMask;
import static com.linkly.libui.IUIDisplay.String_id.STR_APN;
import static com.linkly.libui.IUIDisplay.String_id.STR_APP_NAME;
import static com.linkly.libui.IUIDisplay.String_id.STR_BAND;
import static com.linkly.libui.IUIDisplay.String_id.STR_CELLULAR_INFO;
import static com.linkly.libui.IUIDisplay.String_id.STR_CELLULAR_NETWORK;
import static com.linkly.libui.IUIDisplay.String_id.STR_GATEWAY;
import static com.linkly.libui.IUIDisplay.String_id.STR_IP_ADDRESS;
import static com.linkly.libui.IUIDisplay.String_id.STR_NOT_CONNECTED;
import static com.linkly.libui.IUIDisplay.String_id.STR_SIGNAL_STRENGTH;
import static com.linkly.libui.IUIDisplay.String_id.STR_SUBNET_MASK;
import static com.linkly.libui.IUIDisplay.String_id.STR_WIFI_INFO;
import static com.linkly.libui.display.DisplayUtil.GetSignalLevelString;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;

import androidx.core.content.ContextCompat;

import com.linkly.libconfig.ProfileCfg;
import com.linkly.libengine.dependencies.IDependency;
import com.linkly.libmal.MalFactory;
import com.linkly.libmal.global.platform.Platform;
import com.linkly.libmal.global.printing.PrintReceipt;

public class PrintUtilities {

    private PrintUtilities() {
    }

    public static void addNetworkStatus(IDependency d, PrintReceipt receipt) {
        TelephonyManager telephonyManager = (TelephonyManager) MalFactory.getInstance().getMalContext().getSystemService(Context.TELEPHONY_SERVICE);
        WifiManager wifiManager = (WifiManager) MalFactory.getInstance().getMalContext().getSystemService(Context.WIFI_SERVICE);

        if (telephonyManager != null) {
            receipt.getLines().add(new PrintReceipt.PrintTableLine(getText(STR_CELLULAR_INFO), "", MEDIUM_FONT));

            if (Platform.isPaxTerminal()) {
                addAPNDetails(d, receipt);
            }

            // cellular info
            if (telephonyManager.getDataState() == TelephonyManager.DATA_CONNECTED) {
                receipt.getLines().add(new PrintReceipt.PrintTableLine(getText(STR_CELLULAR_NETWORK), GetCellularNetworkName(MalFactory.getInstance().getMalContext()), SMALL_FONT));
                int signalLevel = GetCellularSignalStrength(MalFactory.getInstance().getMalContext());
                receipt.getLines().add(new PrintReceipt.PrintTableLine(getText(STR_SIGNAL_STRENGTH).toUpperCase()+": ", (signalLevel * 25) + "% " + GetSignalLevelString(signalLevel), SMALL_FONT));
                if (ContextCompat.checkSelfPermission(MalFactory.getInstance().getMalContext(), Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                    receipt.getLines().add(new PrintReceipt.PrintTableLine(getText(STR_BAND), networkTypeToString(telephonyManager.getDataNetworkType()), SMALL_FONT));
                }
            } else {
                receipt.getLines().add(new PrintReceipt.PrintTableLine(getText(STR_SIGNAL_STRENGTH).toUpperCase()+": ", getText(STR_NOT_CONNECTED), SMALL_FONT));
            }
        }

        receipt.getLines().add(new PrintReceipt.PrintSpaceLine());

        receipt.getLines().add(new PrintReceipt.PrintTableLine(getText(STR_WIFI_INFO), "", MEDIUM_FONT));
        if (wifiManager != null && wifiManager.getWifiState() == WIFI_STATE_ENABLED) {
            // Wifi info
            receipt.getLines().add(new PrintReceipt.PrintTableLine(getText(STR_IP_ADDRESS).toUpperCase(), GetWifiIPAddress(MalFactory.getInstance().getMalContext()), SMALL_FONT));
            receipt.getLines().add(new PrintReceipt.PrintTableLine(getText(STR_SUBNET_MASK).toUpperCase(), GetWifiSubnetMask(MalFactory.getInstance().getMalContext()), SMALL_FONT));
            receipt.getLines().add(new PrintReceipt.PrintTableLine(getText(STR_GATEWAY).toUpperCase(), GetWifiGateway(MalFactory.getInstance().getMalContext()), SMALL_FONT));
            int wifiStrength = GetWifiSignalStrength(MalFactory.getInstance().getMalContext());
            receipt.getLines().add(new PrintReceipt.PrintTableLine(getText(STR_SIGNAL_STRENGTH).toUpperCase()+": ", (wifiStrength * 25 ) + "% " + GetSignalLevelString(wifiStrength), SMALL_FONT));
        } else {
            receipt.getLines().add(new PrintReceipt.PrintTableLine(getText(STR_SIGNAL_STRENGTH).toUpperCase()+": ", getText(STR_NOT_CONNECTED), SMALL_FONT));
        }
    }

    private static void addAPNDetails(IDependency d, PrintReceipt receipt) {

        // getting it dynamically doesnt seem to work, so we just display the ones from the config
        int i = 0;
        ProfileCfg p = d.getProfileCfg();
        if (p == null)
            return;

        if (p.getApns() == null)
            return;

        for (ProfileCfg.ApnCfg a : p.getApns()) {
            i++;
            receipt.getLines().add(new PrintReceipt.PrintTableLine(getText(STR_APP_NAME)+" (" + i + "): ", a.getName(), SMALL_FONT));
            receipt.getLines().add(new PrintReceipt.PrintTableLine(getText(STR_APN)+"      (" + i + "): ", a.getApn(), SMALL_FONT));
        }
    }

    public static String networkTypeToString(int networkType) {
        switch (networkType) {
            case NETWORK_TYPE_1xRTT:
            case NETWORK_TYPE_GPRS:
            case NETWORK_TYPE_EDGE:
            case NETWORK_TYPE_IDEN:
            case NETWORK_TYPE_CDMA:
                return "2G";
            case NETWORK_TYPE_GSM:
            case NETWORK_TYPE_UMTS:
            case NETWORK_TYPE_EHRPD:
            case NETWORK_TYPE_EVDO_0:
            case NETWORK_TYPE_EVDO_A:
            case NETWORK_TYPE_EVDO_B:
            case NETWORK_TYPE_TD_SCDMA:
            case NETWORK_TYPE_IWLAN:
            case NETWORK_TYPE_HSUPA:
            case NETWORK_TYPE_HSPAP:
            case NETWORK_TYPE_HSPA:
            case NETWORK_TYPE_HSDPA:
                return "3G";
            case NETWORK_TYPE_LTE:
                return "4G";
            case NETWORK_TYPE_NR:
                return "5G";

            case NETWORK_TYPE_UNKNOWN:
            default:
                return "N/A";
        }
    }
}
