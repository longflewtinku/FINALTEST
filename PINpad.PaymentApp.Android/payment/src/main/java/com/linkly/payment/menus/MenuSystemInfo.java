package com.linkly.payment.menus;

public class MenuSystemInfo extends Menu {


    private static final String TAG = "SysInfo";

    private static MenuSystemInfo ourInstance;

    private MenuSystemInfo() {
        super("System Info");
        /*Create DMGR MainMenu Options*/
        this.refreshMenu();
    }

    public static MenuSystemInfo getInstance() {
        if (ourInstance == null) {
            ourInstance = new MenuSystemInfo();
        }
        return ourInstance;
    }

    public void refreshMenu() {
        getMenuItems().clear();
        getMenuItems().add(MenuItems.AboutApp);
        getMenuItems().add(MenuItems.EMVConfig);
        getMenuItems().add(MenuItems.CTLSConfig);
    }
}
