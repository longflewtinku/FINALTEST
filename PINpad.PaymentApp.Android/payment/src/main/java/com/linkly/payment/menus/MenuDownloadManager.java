package com.linkly.payment.menus;

public class MenuDownloadManager extends Menu {

    private static final String TAG = "DMGRMenu";

    private static MenuDownloadManager ourInstance;

    private MenuDownloadManager() {
        super("Download");
        /*Create DMGR MainMenu Options*/
        getMenuItems().add(MenuItems.Update);
        getMenuItems().add(MenuItems.ForceUpdate);
    }

    public static MenuDownloadManager getInstance() {
        if (ourInstance == null) {
            ourInstance = new MenuDownloadManager();
        }
        return ourInstance;
    }


}
