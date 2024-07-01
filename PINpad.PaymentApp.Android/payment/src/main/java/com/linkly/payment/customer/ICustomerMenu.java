package com.linkly.payment.customer;

import com.linkly.libengine.users.User;
import com.linkly.payment.menus.MenuItems;

import java.util.List;

public interface ICustomerMenu {


    List<MenuItems> getAdminMenuList(User.Privileges level);

    List<MenuItems> getMainMenuList(User.Privileges level);

}
