package com.linkly.libconfig;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import androidx.annotation.Nullable;

import com.linkly.libmal.global.config.JSONParse;

import java.io.Serializable;
import java.util.List;

import timber.log.Timber;


@SuppressWarnings("serial")
public class ProfileCfg implements Serializable {

    private static final String TAG = "ProfileCfg";
    private static ProfileCfg ourInstance = null;

    private String version;
    private String customerName;
    private String brandName;
    private String paymentName;
    private String serviceName;
    private String paxstoreP2PEKey;
    private String paxstoreP2PESecret;
    private String paxstorePaymentKey;
    private String paxstorePaymentSecret;
    private String paxstoreServiceKey;
    private String paxstoreServiceSecret;
    private boolean enableScreenshot;
    private String language = "english";
    private boolean unattendedModeAllowed;
    private boolean kioskMode = false;
    private String kioskScreenText = "";
    private OrientationString unattendedLockedDownMessage;

    private boolean useBlackListMenuMethod = false; // if true, then we invert the menu logic, we hide apps listed in the blacklist and show everything else
    private List<ProfileCfg.MenuItemCfg> menus;
    private List<ProfileCfg.MenuItemCfg> blacklistMenu; // specifies the blacklisted menu items, used only if useBlackListMenuMethod is true
    private List<ProfileCfg.ApnCfg> apns;
    private SettingsMenus settingsMenus;
    protected MenusCfg paymentMenus = new MenusCfg();
    private boolean validCfg = false;

    private ProfileCfg() {
    }

    @Nullable
    public static ProfileCfg getInstance() {
        if (ourInstance == null) {
            ourInstance = parse();
        }
        return ourInstance;
    }

    private static ProfileCfg parse() {

        try {
            JSONParse j = new JSONParse();
            ourInstance = j.parse("profile.json", ProfileCfg.class);
            Timber.i("Finished parse");
        } catch (Exception e) {
            Timber.e(e);
            ourInstance = null;
        }
        return ourInstance;

    }

    @SuppressWarnings("deprecation")
    private MenuItemCfg buildMenuUsingBlacklistMethod(String packageName, String processName, PackageManager manager){
        // blacklist method
        boolean appBlacklisted = false;
        if( blacklistMenu != null ){
            for (MenuItemCfg menuItem : blacklistMenu) {
                // do unfussy match, if the package name in the blacklist partially matches then exclude this, allows wildcard type behaviour
                // exclude leakcanary processes
                if (packageName.contains(menuItem.getPackageName()) || processName.contains("leakcanary")) {
                    appBlacklisted = true;
                    break;
                }
            }
        }
        if( !appBlacklisted ){
            // app isn't blacklisted, allow it to appear on menu
            ApplicationInfo ai;
            try {
                ai = manager.getApplicationInfo(packageName, 0);
            } catch (PackageManager.NameNotFoundException e) {
                ai = null;
            }
            // if we can't retrieve app info, use package name for label
            String appName = (String) (ai != null ? manager.getApplicationLabel(ai) : packageName);

            // create menu item for this
            MenuItemCfg menuItem = new MenuItemCfg();
            menuItem.setPackageName(packageName);
            menuItem.setDisplayName(appName);
            menuItem.setAutoStart(false); // don't ever auto start
            menuItem.setEnableNavBar(true);
            menuItem.setPriority(1);
            return menuItem;
        } else {
            return null;
        }
    }

    private MenuItemCfg buildMenuUsingWhitelistMethod(String packageName, String processName) {
        // use 'whitelist' method for building menu contents
        if (menus != null) {
            for (MenuItemCfg menu : menus) {
                if (menu.getPackageName().contains(packageName) && !processName.contains("leakcanary")) {
                    return menu;
                }
            }
        }
        return null;
    }

    public MenuItemCfg getMenuItem(String packageName, String processName, PackageManager manager) {
        Timber.i("Check for: %s", packageName);
        if( useBlackListMenuMethod ){
            return buildMenuUsingBlacklistMethod(packageName, processName, manager);
        } else {
            return buildMenuUsingWhitelistMethod(packageName, processName);
        }
    }

    public boolean isDemo() {
        return (customerName != null && customerName.toLowerCase().contains("demo"));
    }

    public String getVersion() {
        return this.version;
    }

    public String getCustomerName() {
        return this.customerName;
    }

    public String getBrandName() {
        return this.brandName;
    }

    public String getPaymentName() {
        return this.paymentName;
    }

    public String getServiceName() {
        return this.serviceName;
    }

    public String getPaxstoreP2PEKey() {
        return this.paxstoreP2PEKey;
    }

    public String getPaxstoreP2PESecret() {
        return this.paxstoreP2PESecret;
    }

    public String getPaxstorePaymentKey() {
        return this.paxstorePaymentKey;
    }

    public String getPaxstorePaymentSecret() {
        return this.paxstorePaymentSecret;
    }

    public String getPaxstoreServiceKey() {
        return this.paxstoreServiceKey;
    }

    public String getPaxstoreServiceSecret() {
        return this.paxstoreServiceSecret;
    }

    public boolean isEnableScreenshot() {
        return this.enableScreenshot;
    }

    public String getLanguage() {
        return this.language;
    }

    public boolean isUnattendedModeAllowed() {
        return this.unattendedModeAllowed;
    }

    public boolean isKioskMode() {
        return this.kioskMode;
    }

    public String getKioskScreenText() {
        return this.kioskScreenText;
    }

    public OrientationString getUnattendedLockedDownMessage() {
        return this.unattendedLockedDownMessage;
    }

    public boolean isUseBlackListMenuMethod() {
        return this.useBlackListMenuMethod;
    }

    public List<MenuItemCfg> getMenus() {
        return this.menus;
    }

    public List<MenuItemCfg> getBlacklistMenu() {
        return this.blacklistMenu;
    }

    public List<ApnCfg> getApns() {
        return this.apns;
    }

    public SettingsMenus getSettingsMenus() {
        return this.settingsMenus;
    }

    public MenusCfg getPaymentMenus() {
        return this.paymentMenus;
    }

    public boolean isValidCfg() {
        return this.validCfg;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public void setBrandName(String brandName) {
        this.brandName = brandName;
    }

    public void setPaymentName(String paymentName) {
        this.paymentName = paymentName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public void setPaxstoreP2PEKey(String paxstoreP2PEKey) {
        this.paxstoreP2PEKey = paxstoreP2PEKey;
    }

    public void setPaxstoreP2PESecret(String paxstoreP2PESecret) {
        this.paxstoreP2PESecret = paxstoreP2PESecret;
    }

    public void setPaxstorePaymentKey(String paxstorePaymentKey) {
        this.paxstorePaymentKey = paxstorePaymentKey;
    }

    public void setPaxstorePaymentSecret(String paxstorePaymentSecret) {
        this.paxstorePaymentSecret = paxstorePaymentSecret;
    }

    public void setPaxstoreServiceKey(String paxstoreServiceKey) {
        this.paxstoreServiceKey = paxstoreServiceKey;
    }

    public void setPaxstoreServiceSecret(String paxstoreServiceSecret) {
        this.paxstoreServiceSecret = paxstoreServiceSecret;
    }

    public void setEnableScreenshot(boolean enableScreenshot) {
        this.enableScreenshot = enableScreenshot;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public void setUnattendedModeAllowed(boolean unattendedModeAllowed) {
        this.unattendedModeAllowed = unattendedModeAllowed;
    }

    public void setKioskMode(boolean kioskMode) {
        this.kioskMode = kioskMode;
    }

    public void setKioskScreenText(String kioskScreenText) {
        this.kioskScreenText = kioskScreenText;
    }

    public void setUnattendedLockedDownMessage(OrientationString unattendedLockedDownMessage) {
        this.unattendedLockedDownMessage = unattendedLockedDownMessage;
    }

    public void setUseBlackListMenuMethod(boolean useBlackListMenuMethod) {
        this.useBlackListMenuMethod = useBlackListMenuMethod;
    }

    public void setMenus(List<MenuItemCfg> menus) {
        this.menus = menus;
    }

    public void setBlacklistMenu(List<MenuItemCfg> blacklistMenu) {
        this.blacklistMenu = blacklistMenu;
    }

    public void setApns(List<ApnCfg> apns) {
        this.apns = apns;
    }

    public void setSettingsMenus(SettingsMenus settingsMenus) {
        this.settingsMenus = settingsMenus;
    }

    public void setPaymentMenus(MenusCfg paymentMenus) {
        this.paymentMenus = paymentMenus;
    }

    public void setValidCfg(boolean validCfg) {
        this.validCfg = validCfg;
    }
    @SuppressWarnings("serial")
    public static class MenuItemCfg implements Serializable{
        private String packageName;
        private String displayName;
        private String imageName;
        private boolean autoStart = false;
        private boolean enableNavBar = false;
        private String autoStartDelay;
        private int priority;

        public String getPackageName() {
            return this.packageName;
        }

        public String getDisplayName() {
            return this.displayName;
        }

        public String getImageName() {
            return this.imageName;
        }

        public boolean isAutoStart() {
            return this.autoStart;
        }

        public boolean isEnableNavBar() {
            return this.enableNavBar;
        }

        public String getAutoStartDelay() {
            return this.autoStartDelay;
        }

        public int getPriority() {
            return this.priority;
        }

        public void setPackageName(String packageName) {
            this.packageName = packageName;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }

        public void setImageName(String imageName) {
            this.imageName = imageName;
        }

        public void setAutoStart(boolean autoStart) {
            this.autoStart = autoStart;
        }

        public void setEnableNavBar(boolean enableNavBar) {
            this.enableNavBar = enableNavBar;
        }

        public void setAutoStartDelay(String autoStartDelay) {
            this.autoStartDelay = autoStartDelay;
        }

        public void setPriority(int priority) {
            this.priority = priority;
        }
    }

    @SuppressWarnings("serial")
    public static class ApnCfg implements Serializable{
        private String name;
        private String apn;
        private String user;
        private String pwd;

        public String getName() {
            return this.name;
        }

        public String getApn() {
            return this.apn;
        }

        public String getUser() {
            return this.user;
        }

        public String getPwd() {
            return this.pwd;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setApn(String apn) {
            this.apn = apn;
        }

        public void setUser(String user) {
            this.user = user;
        }

        public void setPwd(String pwd) {
            this.pwd = pwd;
        }
    }

    public static class SettingsMenus implements Serializable {
        private boolean showSettingsMenu;
        private boolean showSetupWifiBaseMenu;
        private boolean showBluetoothSettingsMenu;
        private boolean showWifiSettingsMenu;
        private boolean showSoundSettingsMenu;
        private boolean showEthernetSettingsMenu;
        private boolean showApnSettingsMenu;
        private boolean showCellularSettingsMenu;
        private boolean showAirplaneModeMenu;
        private boolean showInputSettingsMenu;
        private boolean showInjectKeysSettingsMenu;
        private boolean showDisplaySettingsMenu;

        public boolean isShowSettingsMenu() {
            return this.showSettingsMenu;
        }

        public boolean isShowSetupWifiBaseMenu() {
            return this.showSetupWifiBaseMenu;
        }

        public boolean isShowBluetoothSettingsMenu() {
            return this.showBluetoothSettingsMenu;
        }

        public boolean isShowWifiSettingsMenu() {
            return this.showWifiSettingsMenu;
        }

        public boolean isShowSoundSettingsMenu() {
            return this.showSoundSettingsMenu;
        }

        public boolean isShowEthernetSettingsMenu() {
            return this.showEthernetSettingsMenu;
        }

        public boolean isShowApnSettingsMenu() {
            return this.showApnSettingsMenu;
        }

        public boolean isShowCellularSettingsMenu() {
            return this.showCellularSettingsMenu;
        }

        public boolean isShowAirplaneModeMenu() {
            return this.showAirplaneModeMenu;
        }

        public boolean isShowInputSettingsMenu() {
            return this.showInputSettingsMenu;
        }

        public boolean isShowInjectKeysSettingsMenu() {
            return this.showInjectKeysSettingsMenu;
        }

        public boolean isShowDisplaySettingsMenu() {
            return this.showDisplaySettingsMenu;
        }

        public void setShowSettingsMenu(boolean showSettingsMenu) {
            this.showSettingsMenu = showSettingsMenu;
        }

        public void setShowSetupWifiBaseMenu(boolean showSetupWifiBaseMenu) {
            this.showSetupWifiBaseMenu = showSetupWifiBaseMenu;
        }

        public void setShowBluetoothSettingsMenu(boolean showBluetoothSettingsMenu) {
            this.showBluetoothSettingsMenu = showBluetoothSettingsMenu;
        }

        public void setShowWifiSettingsMenu(boolean showWifiSettingsMenu) {
            this.showWifiSettingsMenu = showWifiSettingsMenu;
        }

        public void setShowSoundSettingsMenu(boolean showSoundSettingsMenu) {
            this.showSoundSettingsMenu = showSoundSettingsMenu;
        }

        public void setShowEthernetSettingsMenu(boolean showEthernetSettingsMenu) {
            this.showEthernetSettingsMenu = showEthernetSettingsMenu;
        }

        public void setShowApnSettingsMenu(boolean showApnSettingsMenu) {
            this.showApnSettingsMenu = showApnSettingsMenu;
        }

        public void setShowCellularSettingsMenu(boolean showCellularSettingsMenu) {
            this.showCellularSettingsMenu = showCellularSettingsMenu;
        }

        public void setShowAirplaneModeMenu(boolean showAirplaneModeMenu) {
            this.showAirplaneModeMenu = showAirplaneModeMenu;
        }

        public void setShowInputSettingsMenu(boolean showInputSettingsMenu) {
            this.showInputSettingsMenu = showInputSettingsMenu;
        }

        public void setShowInjectKeysSettingsMenu(boolean showInjectKeysSettingsMenu) {
            this.showInjectKeysSettingsMenu = showInjectKeysSettingsMenu;
        }

        public void setShowDisplaySettingsMenu(boolean showDisplaySettingsMenu) {
            this.showDisplaySettingsMenu = showDisplaySettingsMenu;
        }
    }

}
