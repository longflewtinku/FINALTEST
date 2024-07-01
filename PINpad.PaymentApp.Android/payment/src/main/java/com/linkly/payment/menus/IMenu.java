package com.linkly.payment.menus;

import com.linkly.libengine.dependencies.IDependency;

import java.util.List;

public interface IMenu {

    String getMenuTitle();

    List<MenuItems> getMenuItems();


    /* can return a new menu to display, or just the original main menu or admin menu */
    /* will also call the engine to kick off a task if required */
    IMenu selectMenuItem(MenuItems mEntry, IDependency dependencies);

    IMenu reloadMenu();

}
