package com.linkly.payment.menus;

import com.linkly.payment.BuildConfig;

public class MenuDevelopment extends Menu {
    private static MenuDevelopment ourInstance;


    private MenuDevelopment() {
        super("Dev Menu");
        /* Admin  Menu */

        /*
         * Unused menu items. Keeping here in case we want to re-introduce later
         *
         * MenuItems.AppSettings
         * MenuItems.TestKeys
         * MenuItems.KeyClear
         * MenuItems.MSGLog
         * MenuItems.ClearMSGLog
         * MenuItems.EventLog
         * MenuItems.ServiceCheck
         * MenuItems.TestSchedule
         * MenuItems.TestAddTime
         * MenuItems.KeyInject
         * MenuItems.TestAutoDownload
         * MenuItems.ResetKeys
         */
        getMenuItems().add(MenuItems.DBMan);
        getMenuItems().add(MenuItems.TestAddTrans);
        getMenuItems().add(MenuItems.IccDiags);
        getMenuItems().add(MenuItems.ClearReversal);
        getMenuItems().add(MenuItems.CommsLoopTest);

        if (BuildConfig.DEBUG) {
            // we don't want these tests in non-debug builds
            getMenuItems().add(MenuItems.DukptTests);
        }

    }

    public static MenuDevelopment getInstance() {
        if (ourInstance == null) {
            ourInstance = new MenuDevelopment();
        }
        return ourInstance;
    }

    public IMenu reloadMenu() {
        super.reloadMenu();
        return getInstance();
    }
}
