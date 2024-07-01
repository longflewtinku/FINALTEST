package com.linkly.libconfig;

import java.util.ArrayList;
import java.util.List;

public class MenusCfg  {

    protected List<UserMenuList> userMenuLists = new ArrayList<>();

    public static class UserMenuList {
        private String level;
        private String mainMenu;
        private String adminMenu;

        public String getLevel() {
            return this.level;
        }

        public String getMainMenu() {
            return this.mainMenu;
        }

        public String getAdminMenu() {
            return this.adminMenu;
        }

        public void setLevel(String level) {
            this.level = level;
        }

        public void setMainMenu(String mainMenu) {
            this.mainMenu = mainMenu;
        }

        public void setAdminMenu(String adminMenu) {
            this.adminMenu = adminMenu;
        }
    }

    private void InitDefaultLists() {
        UserMenuList listDefault = new UserMenuList();
        listDefault.level = "DEFAULT";
        listDefault.mainMenu = "Sale,Cash,Reversal,Refund,Pre-Auth,Completion,Reconciliation,Submit Transactions,Test Connect,Card Not Present";
        listDefault.adminMenu = "System Info,Test Connect,Submit Transactions,Reconciliation,Daily Batch,Reprint Rec,Reprint Manager,Download Manager"; // User Manager,
        userMenuLists.add(listDefault);
    }

    public String[] GetMainMenuList(String userLevel) {

        if (userMenuLists == null || userMenuLists.size() <= 0)
            InitDefaultLists();

        for ( UserMenuList list : userMenuLists) {
            if (list.getLevel().equals(userLevel) || list.getLevel().equals("DEFAULT")) {
                return list.getMainMenu().split(",");
            }
        }

        return null;
    }

    public String[] GetAdminMenuList(String userLevel) {

        if (userMenuLists == null || userMenuLists.size() <= 0)
            InitDefaultLists();

        for ( UserMenuList list : userMenuLists) {
            if (list.getLevel().equals(userLevel)) {
                return list.getAdminMenu().split(",");
            }
        }

        /* only look for default if we didnt find it above */
        for ( UserMenuList list : userMenuLists) {
            if (list.getLevel().equals("DEFAULT")) {
                return list.getAdminMenu().split(",");
            }
        }


        return null;
    }
}
