package com.linkly.payment.menus;

public class MenuDatabase extends Menu {

    private static final String TAG = "MenuDatabase";
    private static MenuDatabase ourInstance;

    private MenuDatabase() {
        super("Database");
        getMenuItems().add(MenuItems.DBStats);
        getMenuItems().add(MenuItems.DBList);
        getMenuItems().add(MenuItems.DBClear);
        getMenuItems().add(MenuItems.DBRecList);
    }

    public static MenuDatabase getInstance() {
        if (ourInstance == null) {
            ourInstance = new MenuDatabase();
        }
        return ourInstance;
    }


}
