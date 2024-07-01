package com.linkly.payment.menus;

import com.linkly.libengine.engine.Engine;
import com.linkly.libengine.users.UserManager;
import com.linkly.libui.IUIDisplay;
import com.linkly.payment.customer.ICustomerMenu;
public class MainMenu extends Menu {

    private static MainMenu ourInstance;


    private MainMenu() {
        // TODO: WC not sure of best way to make this work with dependency injection
        super(Engine.getDep().getPrompt(IUIDisplay.String_id.STR_MAIN_MENU));
        /*Menu Items are now taken from the Customer Configuration */
        if(UserManager.getActiveUser() != null) {
            getMenuItems().addAll(((ICustomerMenu) Engine.getCustomer()).getMainMenuList(UserManager.getActiveUser().getPrivileges()));
        }


    }

    public static MainMenu getInstance() {

        if (ourInstance == null) {
            ourInstance = new MainMenu();
        }

        return ourInstance;
    }

    public IMenu reloadMenu() {
        super.reloadMenu();
        ourInstance = null;
        ourInstance = getInstance();
        return ourInstance;
    }

}
