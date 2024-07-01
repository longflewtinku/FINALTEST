package com.linkly.payment.menus;

public class MenuReprint extends Menu {


    private static final String TAG = "RePrint Menu";

    private static MenuReprint ourInstance;

    private MenuReprint() {
        super("RePrint");
        /*Create  MainMenu Options*/
        getMenuItems().add(MenuItems.ReprintLast);
        //Note - RePrint Rec is now under the Admin Menu Root as its Controlled based on Customers
    }

    public static MenuReprint getInstance() {
        if (ourInstance == null) {
            ourInstance = new MenuReprint();
        }
        return ourInstance;
    }


}
