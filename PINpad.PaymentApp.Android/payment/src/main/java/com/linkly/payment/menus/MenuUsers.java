package com.linkly.payment.menus;

import static com.linkly.libengine.users.User.Privileges.MANAGER;
import static com.linkly.libengine.users.User.Privileges.SUPERVISOR;

import com.linkly.libengine.users.UserManager;

public class MenuUsers extends Menu {


    private static final String TAG = "UserMenu";

    private static MenuUsers ourInstance;

    private MenuUsers() {
        super("User Manager");
        /*Create DMGR MainMenu Options*/
        this.refreshMenu();
    }

    public static MenuUsers getInstance() {
        if (ourInstance == null) {
            ourInstance = new MenuUsers();
        }
        ourInstance.refreshMenu(); //MD: always call refreshMenu() to get current user's menu items
        return ourInstance;
    }

    public void refreshMenu() {
        getMenuItems().clear();
        if ( UserManager.getActiveUser() != null) {
            if (UserManager.getActiveUser().getPrivileges() == SUPERVISOR || UserManager.getActiveUser().getPrivileges() == MANAGER) {
                getMenuItems().add(MenuItems.ReportUser);
                getMenuItems().add(MenuItems.AddUser);
                getMenuItems().add(MenuItems.DeleteUser);
                getMenuItems().add(MenuItems.ResetPassword);
            }
            getMenuItems().add(MenuItems.ChangePassword);

        }
    }


}
