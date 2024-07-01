package com.linkly.launcher;

import static android.content.Context.WIFI_SERVICE;
import static com.linkly.libmal.IMalComms.CommsResult.FAIL;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Environment;

import com.linkly.libmal.IMalComms;
import com.linkly.libmal.MalFactory;
import com.linkly.libmal.global.util.Util;
import com.pax.baselink.api.BDeviceInfo;
import com.pax.baselink.api.BWifiManageParam;
import com.pax.baselink.api.BaseLinkApi;
import com.pax.baselink.api.BaseResp;
import com.pax.baselink.api.EFileType;
import com.pax.baselink.api.EWifiManageType;
import com.pax.baselink.listener.BUpdateFirmwareListener;

import java.io.File;
import java.util.List;
import java.util.Set;

import timber.log.Timber;

@SuppressWarnings("deprecation")
public class BaseStation {
    private static final String TAG = "BluetoothBase";
    public static final String ERROR_DIALOG_NAME = "Base Station Configuration";
    public static final String RNDIS_SETUP_DIALOG_TITLE = "RNDIS Setup";
    private static final int DEFAULT_DIALOG_TIMEOUT_MILLIS = 3500;

    private BaseLinkApi L920manager;
    private BluetoothAdapter mBluetoothAdapter;
    public static String currentBtMac = "";
    private static BaseStation thisInstance = null;
    private BDeviceInfo currentBaseStationInfo = null;

    public BaseLinkApi getL920manager() {
        return this.L920manager;
    }

    public BluetoothAdapter getMBluetoothAdapter() {
        return this.mBluetoothAdapter;
    }

    public BDeviceInfo getCurrentBaseStationInfo() {
        return this.currentBaseStationInfo;
    }

    public static class IpSetupData {
        private final boolean isStatic;
        private final String ip;
        private final String netmask;
        private final String gateway;
        private final String preferredDns;
        private final String standbyDns;

        public IpSetupData(boolean isStatic, String ip, String netmask, String gateway, String preferredDns, String standbyDns) {
            this.isStatic = isStatic;
            this.ip = ip;
            this.netmask = netmask;
            this.gateway = gateway;
            this.preferredDns = preferredDns;
            this.standbyDns = standbyDns;
        }

        public IpSetupData(boolean isStatic, String ip, String netmask, String gateway, String preferredDns) {
            this(isStatic, ip, netmask, gateway, preferredDns, preferredDns);
        }

        public boolean isStatic() {
            return this.isStatic;
        }

        public String getIp() {
            return this.ip;
        }

        public String getNetmask() {
            return this.netmask;
        }

        public String getGateway() {
            return this.gateway;
        }

        public String getPreferredDns() {
            return this.preferredDns;
        }

        public String getStandbyDns() {
            return this.standbyDns;
        }
    }

    public static class WifiApSetupData {

        final String ssid;
        final String password;
        final boolean dhcpEnable;
        final String dhcpStart;
        final String dhcpEnd;

        public WifiApSetupData(String ssid, String passwd, boolean dhcpEnable, String dhcpStart, String dhcpEnd) {
            this.ssid = ssid;
            this.password = passwd;
            this.dhcpEnable = dhcpEnable;
            this.dhcpStart = dhcpStart;
            this.dhcpEnd = dhcpEnd;
        }
    }

    private BaseStation(Context context) {
        L920manager = BaseLinkApi.getInstance(context);
        mBluetoothAdapter = ((BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter();
    }

    public static BaseStation getInstance() {
        if (thisInstance == null) {
            thisInstance = new BaseStation(MalFactory.getInstance().getMalContext());
        }
        return thisInstance;
    }

    public boolean isBluetoothEnabled() {
        // Device does not support Bluetooth or Bluetooth is not enabled
        return mBluetoothAdapter != null && mBluetoothAdapter.isEnabled();
    }

    public boolean reconnect() {
        L920manager.btDisconnect();
        Util.Sleep(1000);
        return isConnected();
    }

    // Came up during linting.
    @SuppressLint("MissingPermission")
    public boolean isConnected() {

        if (!isBluetoothEnabled())
            return false;

        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

        for(BluetoothDevice bt : pairedDevices) {
            Timber.i("Bluetooth device connected:" + bt.getName() + ":" + bt.getAddress());
            if (L920manager.btConnect(bt.getAddress())) {
                Timber.i("Bluetooth device connected:" + bt.getName() + ":" + bt.getAddress());
                currentBtMac = bt.getAddress();
                getInfo();
                return true;
            }
        }

        return false;
    }

    public static void debugExInfo() {
        if( BaseStation.getInstance().getCurrentBaseStationInfo() == null ) {
            return;
        }

        String exInfo = BaseStation.getInstance().getCurrentBaseStationInfo().getExDeviceInfo();
        String[] splits = exInfo.split("#");

        // debug
        for (String values : splits) {
            String[] results = values.split("=");
            if (results.length >= 2) {
                Timber.i( "base info key = [%s], value = [%s]", results[0], results[1] );
            }
        }
    }

    public String getExInfo(String tagName) {
        if( BaseStation.getInstance().getCurrentBaseStationInfo() == null ) {
            return null;
        }

        String exInfo = BaseStation.getInstance().getCurrentBaseStationInfo().getExDeviceInfo();
        String[] splits = exInfo.split("#");

        for (String values : splits) {
            if (values.contains(tagName)) {
                String[] results = values.split("=");
                if (results.length >= 2)
                    return results[1];
            }
        }
        return null;

    }

    public boolean checkLan() {
        BaseResp<String> ret = BaseLinkApi.LanPort.ping("8.8.8.8", 5);
        if (ret.respCode == BaseResp.SUCCESS) {

            return true;
        }

        getInfo();
        return false;

    }
    public boolean setDhcp() {
        BaseResp<?> ret = BaseLinkApi.LanPort.setDhcp();
        if (ret.respCode == BaseResp.SUCCESS) {
            checkLan();
            getInfo();
            return true;
        }
        Timber.e("Failed to enable LAN DHCP, err = %d", ret.respCode);
        return false;
    }

    public boolean setStatic(IpSetupData ipSetupData) {
        BaseResp<?> ret = BaseLinkApi.LanPort.setStaticIp(ipSetupData.ip, ipSetupData.netmask, ipSetupData.gateway, ipSetupData.preferredDns, ipSetupData.standbyDns);
        if (ret.respCode == BaseResp.SUCCESS) {
            checkLan();
            getInfo();
            return true;
        }
        Timber.e("Failed to Set LAN Static\n" + ipSetupData.ip + ":" + ipSetupData.netmask + "\n" + ipSetupData.gateway + ":" + ipSetupData.preferredDns);
        return false;

    }

    public boolean wifiReset() {
        BWifiManageParam param = new BWifiManageParam();
        param.setManageType(EWifiManageType.WIFI_OPERATION_AP_RESET);
        BaseResp<?> ret = L920manager.wifiManage(param, 15);
        if (ret.respCode == BaseResp.SUCCESS) {
            return true;
        }
        return false;

    }
    public boolean wifiStart() {

        WifiManager wifiManager = (WifiManager)MalFactory.getInstance().getMalContext().getApplicationContext().getSystemService(WIFI_SERVICE);
        if (!wifiManager.isWifiEnabled())
            return wifiManager.setWifiEnabled(true);
        wifiManager.disconnect();
        return true;

    }

    public boolean wifiOpen() {

        BWifiManageParam param = new BWifiManageParam();
        param.setManageType(EWifiManageType.WIFI_OPERATION_OPEN);
        BaseResp<?> ret = L920manager.wifiManage(param, 15);
        if (ret.respCode == BaseResp.SUCCESS) {
            return true;
        }
        return false;
    }

    public boolean wifiClose() {
        BWifiManageParam param = new BWifiManageParam();
        param.setManageType(EWifiManageType.WIFI_OPERATION_CLOSE);
        BaseResp<?> ret = L920manager.wifiManage(param, 15);
        if (ret.respCode == BaseResp.SUCCESS) {
            return true;
        }
        return false;
    }

    public static boolean wifiIsConnected(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = null;
        if (connectivityManager != null) {
            networkInfo = connectivityManager.getNetworkInfo(NetworkCapabilities.TRANSPORT_WIFI);
        }

        return networkInfo != null && networkInfo.getState() == NetworkInfo.State.CONNECTED;
    }

    private int getExistingNetworkId(String SSID) {
        WifiManager wifiManager = (WifiManager) MalFactory.getInstance().getMalContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        List<WifiConfiguration> configuredNetworks = wifiManager.getConfiguredNetworks();
        if (configuredNetworks != null) {
            for (WifiConfiguration existingConfig : configuredNetworks) {
                if (existingConfig.SSID.equals(SSID)) {
                    return existingConfig.networkId;
                }
            }
        }
        return -1;
    }

    public boolean wifiConnect(String networkSSID, String networkPass) throws InterruptedException {

        WifiConfiguration wifiConfig = new WifiConfiguration();
        wifiConfig.SSID = String.format("\"%s\"", networkSSID);
        wifiConfig.preSharedKey = String.format("\"%s\"", networkPass);
        wifiConfig.hiddenSSID = true;
        wifiConfig.status = WifiConfiguration.Status.ENABLED;
        wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
        wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
        wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
        wifiConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
        wifiConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
        wifiConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);

        WifiManager wifiManager = (WifiManager)MalFactory.getInstance().getMalContext().getApplicationContext().getSystemService(WIFI_SERVICE);
        List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();

        boolean ret;
        if (list != null) {
            for (WifiConfiguration c : list) {
                if (c.SSID.compareTo(wifiConfig.SSID) == 0) {
                    Timber.i("Configured SSID tp remove: " + c.SSID);
                    // save after removeNetwork not necessary to persist config according to android documentation
                    ret = wifiManager.removeNetwork(c.networkId);
                    Timber.i( "remove returned %b", ret );
                    // short wait to allow removal of old wifi network settings
                    Thread.sleep( 5000 );
                    break;
                }

            }
        }

        Timber.i( "before setWifiEnabled" );
        ret = wifiManager.setWifiEnabled(true);
        Timber.i( "after setWifiEnabled ret %b", ret );

        Timber.i( "adding new network" );
        int netId = wifiManager.addNetwork(wifiConfig);
        Timber.i( "after new network, netId = %d", netId );
        if (netId == -1) {
            netId = getExistingNetworkId(wifiConfig.SSID);
        }
        if (netId == -1) {
            displayError(ERROR_DIALOG_NAME, "Empty Network Id\nMissing: " + networkSSID);
            return false;
        }

        if (!wifiManager.enableNetwork(netId, true)) {
            displayError(ERROR_DIALOG_NAME, "Failed to Enable Wifi");
            return false;
        }
        if (!wifiManager.reconnect()) {
            displayError(ERROR_DIALOG_NAME, "Failed to Run Reconnect API");
            return false;
        }


        for ( int i=0; i < 40; i++) {
            if ( wifiIsConnected(MalFactory.getInstance().getMalContext().getApplicationContext())) {
                return true;
            }
            Thread.sleep(1000);
        }

        displayError(ERROR_DIALOG_NAME, "Failed to Connect Wifi\nSSID:" +  networkSSID);
        return false;
    }

    public static void dismissProgress(int delayInMillis) {
        WifiBaseActivity.dismissProgressDialog(delayInMillis);
    }

    public static void dismissProgress() {
        dismissProgress(DEFAULT_DIALOG_TIMEOUT_MILLIS);
    }

    public static void displayProgress(String title, String message) {
        WifiBaseActivity.newProgressDialog(title, message, true);
    }

    public static void displayError(String title, String message, int delay) {
        WifiBaseActivity.newProgressDialog(title, message, false);
        WifiBaseActivity.dismissProgressDialog(delay == 0 ? DEFAULT_DIALOG_TIMEOUT_MILLIS : delay);
        if (delay > 0)
            Util.Sleep(delay);
    }

    public static void displayError(String title, String message) {
        displayError(title, message, 0);
    }

    public static void tryDisplayError(String title, String message) {
        WifiBaseActivity.newProgressDialog(title, message, false);
        WifiBaseActivity.dismissProgressDialog(DEFAULT_DIALOG_TIMEOUT_MILLIS);
    }

    public boolean wifiCheckNetwork() {

        WifiManager wifiManager = (WifiManager)MalFactory.getInstance().getMalContext().getApplicationContext().getSystemService(WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ip = wifiInfo.getIpAddress();
        if ( ip <= 0 ) {
            tryDisplayError(ERROR_DIALOG_NAME, "Failed to get Wifi IP address");
            return false;
        }

        int linkSpeed = wifiInfo.getLinkSpeed();
        if ( linkSpeed <= 0 ) {
            tryDisplayError(ERROR_DIALOG_NAME, "Failed to get Link Speed");
            return false;
        }

        return true;


    }

    public boolean connect(String url, String port, int timeout) {
        boolean result = false;
        IMalComms.CommsResult cResult = FAIL;

        IMalComms activeConnection = MalFactory.getInstance().getComms().createMalConnection();
        if (activeConnection != null) {
            cResult = activeConnection.connectTCP(url, Integer.valueOf(port), timeout);
            activeConnection.disconnectTCP();
        }
        if (cResult == IMalComms.CommsResult.SUCCESS) {
            result = true;
        }
        return (result);
    }

    public boolean wifiCheckHost() throws InterruptedException {


        /* we need to spoof a card index so that the test connect can get a scheme id, and then the merchant \ acquirer id etc */
        String url = "https://8.8.8.8";
        String port = "";

        if (url.contains("https")) {
            url = url.replaceAll("https://", "");
            port = "443";
        } else if (url.contains("http")){
            url = url.replaceAll("http://", "");
            port = "80";
        } else {
            return false;
        }

        String[] splits = url.split("/");
        url = splits[0];

        if (url == null || port == null) {
            tryDisplayError(ERROR_DIALOG_NAME, "No Host Configured\nAssume Connect OK");
            return true;
        }

        for (int i = 0; i < 3; i++) {
            // connect the comms
            if (connect(url, port, 5)) {
                return true;
            }
            Thread.sleep(1000);
        }
        tryDisplayError(ERROR_DIALOG_NAME, "Failed to connect to\n" + url + ":" + port);
        return false;
    }


    public boolean wifiSetAPManualStart(WifiApSetupData wifiApSetupData) {

        BWifiManageParam param = new BWifiManageParam();
        param.setManageType(EWifiManageType.WIFI_MODE_AP_AUTO_START);
        param.setSsid(wifiApSetupData.ssid);
        param.setPasswd(wifiApSetupData.password);

        param.setChannel("9");


        BaseResp<?> ret = L920manager.wifiManage(param, 15);
        if (ret.respCode == BaseResp.SUCCESS) {
            getInfo();

            return true;
        } else {
            Timber.e("Failed to configure AP\nError:%s", ret.respMsg);
        }
        return false;
    }

    public String checkForNewFirmware() {
        String firmwareFile = null;
        try {
            File directory = new File(Environment.getExternalStorageDirectory() + "/Download");
            File[] files = directory.listFiles();
            for (File file : files) {
                Timber.i("File Name: " + file.getName());
                if (file.getName().contains("Base-L920")) {
                    firmwareFile = file.getAbsolutePath();
                    break;
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return firmwareFile;
    }

    public String getCurrentFirmware() {
        if( BaseStation.getInstance().getCurrentBaseStationInfo() == null ) {
            return null;
        }

        String currentFirmware = BaseStation.getInstance().getCurrentBaseStationInfo().getProlinOSVer();
        if (currentFirmware == null)
            return null;
        currentFirmware = currentFirmware.replace("V", "");
        return currentFirmware;
    }


    String getVersionFromFilename(String newFirmwareFile) {
        if (newFirmwareFile == null)
            return null;

        String[] parts = newFirmwareFile.split("-V");
        if (parts.length < 2 || parts[1] == null || parts[1].length() == 0){
            return null;
        }

        String newVersion = parts[1];
        newVersion = newVersion.replace("_SIG.zip", "");
        return newVersion;

    }
    boolean checkIfUpdateNeeded(String newFirmwareFile, String currentFirmware) {

        String newVersion = getVersionFromFilename(newFirmwareFile);
        if (newVersion == null || currentFirmware == null)
            return false;

        if ( currentFirmware.contains("2018") || currentFirmware.contains("2019"))
            return true;

        if ( newVersion != null && newVersion.compareTo(currentFirmware) > 0)
            return true;

        return false;

    }

    boolean checkIfFirmwareMatches(String newFirmwareFile, String currentFirmware) {

        String newVersion = getVersionFromFilename(newFirmwareFile);
        if (newVersion == null || currentFirmware == null)
            return false;

        if ( newVersion.compareTo(currentFirmware) == 0)
            return true;

        return false;
    }

    public boolean checkFirmwareOnConnect() {
        String newFirmwareFile = checkForNewFirmware();
        String currentFirmware = getCurrentFirmware();
        return checkIfUpdateNeeded(newFirmwareFile, currentFirmware);
    }

    public boolean rebootAndCheck(String newFirmware) {

        String currentFirmware = "Not Set";
        displayProgress(ERROR_DIALOG_NAME, "Firmware updated Completed\nRebooting\nPlease Wait...");
        BaseStation.getInstance().reboot();

        if (newFirmware == null)
            return false;

        Util.Sleep(3000);
        for (int i = 0; i < 30; i++) {
            if ( isConnected() ) {
                currentFirmware = getCurrentFirmware();
                if (currentFirmware != null) {
                    if (checkIfFirmwareMatches(newFirmware, currentFirmware)) {
                        displayError(ERROR_DIALOG_NAME, "Firmware update Completed\nSuccesfully\n" + currentFirmware, 3000);
                        return true;
                    } else {
                        displayProgress(ERROR_DIALOG_NAME, "Firmware update Running\nComparing Please Wait...\nNew:" + newFirmware + "\nCurrent:" + currentFirmware);
                    }

                }
            }
            Util.Sleep(1000);
        }
        displayError(ERROR_DIALOG_NAME, "Firmware update Failed\nNOT Updated\nNew:" + newFirmware + "\nCurrent:" + currentFirmware, 3000);
        return false;
    }
    public boolean updateFirmware() {

        final String newFirmwareFile = checkForNewFirmware();
        final String currentFirmware = getCurrentFirmware();

        Util.Sleep(DEFAULT_DIALOG_TIMEOUT_MILLIS); /* to allow initial message to show first */
        if (newFirmwareFile == null || newFirmwareFile.length() == 0) {
            displayError(ERROR_DIALOG_NAME, "No Firmware found to install");
            return false;
        }

        displayError(ERROR_DIALOG_NAME, "Firmware replace:\n" + currentFirmware + "with:\n" + getVersionFromFilename(newFirmwareFile), DEFAULT_DIALOG_TIMEOUT_MILLIS);

        if (!BaseStation.getInstance().isConnected()) {
            displayError(ERROR_DIALOG_NAME, "Not Connected to BaseStation");
            return false;
        }

        L920manager.updateFirmware(EFileType.PROLIN_OS_BUNDLE, newFirmwareFile, new BUpdateFirmwareListener() {

            @Override
            public void onSucc() {
                Timber.i("Success");
                String newFirmware = checkForNewFirmware();
                File f = new File(newFirmwareFile);
                f.delete();
                rebootAndCheck(newFirmware);
            }

            @Override
            public void onProgress(int currentSize, int totalSize) {
                int percent = (100 * currentSize) / totalSize;
                Timber.i("Progress:" + percent + "%");
                displayProgress(ERROR_DIALOG_NAME, "DO NOT POWER OFF\nUPDATE PROGRESS : " + percent + "%");
            }

            @Override
            public void onError(int errCode, String errDesc) {
                Timber.i("errCode:" + errCode + "errDesc:" + errDesc);
                displayError(ERROR_DIALOG_NAME, "Firmware updated Failed\nerrCode:" + errCode + "errDesc:" + errDesc);
                File f = new File(newFirmwareFile);
                f.delete();

            }
        });
        return true;
    }

    public boolean reboot() {
        return L920manager.reboot();
    }

    public boolean getInfo() {
        BaseResp<BDeviceInfo> ret = L920manager.getDeviceInfo();
        if (ret.respCode == BaseResp.SUCCESS) {
            BDeviceInfo devInfo = ret.getRespData();
            if (devInfo != null) {

                Timber.i("getApGateway:%s", devInfo.getApGateway());
                Timber.i("getApIpPoolEnd:%s", devInfo.getApIpPoolEnd());
                Timber.i("getApIpPoolStart:%s", devInfo.getApIpPoolStart());
                Timber.i("getApMask:%s", devInfo.getApMask());
                Timber.i("getCustomerSN:%s", devInfo.getCustomerSN());
                Timber.i("getDateTime:%s", devInfo.getDateTime());
                Timber.i("getExDeviceInfo:%s", devInfo.getExDeviceInfo());
                Timber.i("getProductSN:%s", devInfo.getModel());
                Timber.i("getProductSN:%s", devInfo.getProductSN());
                Timber.i("getProlinAppVer:%s", devInfo.getProlinAppVer());
                Timber.i("getProlinOSVer:%s", devInfo.getProlinOSVer());
                Timber.i("getVendor:%s", devInfo.getVendor());
                Timber.i("getWifiPasswd:%s", devInfo.getWifiPasswd());
                Timber.i("getWifiSsid:%s", devInfo.getWifiSsid());
                Timber.i("getWifiStatus:%s", devInfo.getWifiStatus());
                Timber.i("getIsWifiSsidHidden:%s", ((devInfo.getIsWifiSsidHidden() != null && devInfo.getIsWifiSsidHidden()) ? "TRUE" : "FALSE"));
                Timber.i("getWifiManageType:%s", devInfo.getWifiManageType().name());
                currentBaseStationInfo = devInfo;
                return true;
            }

        }
        return false;

    }

}
