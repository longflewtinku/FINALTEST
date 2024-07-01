package com.linkly.payment.menus;

import com.linkly.libengine.engine.Engine;
import com.linkly.libengine.users.UserManager;
import com.linkly.libmal.global.platform.EFTPlatform;
import com.linkly.libui.IUIDisplay.String_id;
import com.linkly.payment.customer.ICustomerMenu;

import timber.log.Timber;

public class MenuAdmin extends Menu {
    private static final String TAG = "MenuAdmin";
    private static MenuAdmin ourInstance;


    private MenuAdmin() {
        // TODO: WC
        super(Engine.getDep().getPrompt(String_id.STR_ADMIN_MENU));
    }

    public static MenuAdmin getInstance() {
        if (ourInstance == null) {
            ourInstance = new MenuAdmin();
        }
        loadMenuItems();
        return ourInstance;
    }

    public static void loadMenuItems() {
        /*Menu Items are now taken from the Customer Configuration */
        if (ourInstance.getMenuItems() != null) {
            ourInstance.getMenuItems().clear();
        }

        if (UserManager.getActiveUser() != null) {

            if( Engine.getCustomer() != null ) {
                ourInstance.getMenuItems().addAll(
                        ( ( ICustomerMenu ) Engine.getCustomer() ).
                                getAdminMenuList( UserManager.getActiveUser().getPrivileges() ) );
            }
        }

        if (EFTPlatform.isPaxTerminal()) {
            //Add any Constant Options here
        }
    }

    public IMenu reloadMenu() {
        Timber.d("reloadMenu...");
        super.reloadMenu();
        ourInstance = null;
        ourInstance = getInstance();
        return ourInstance;
    }
}
