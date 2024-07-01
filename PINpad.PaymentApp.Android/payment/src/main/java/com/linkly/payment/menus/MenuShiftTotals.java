package com.linkly.payment.menus;

public class MenuShiftTotals extends Menu {


    private static MenuShiftTotals ourInstance;

    private MenuShiftTotals() {
        super("Shift Totals");
        /*Create MainMenu Options*/
        this.refreshMenu();
    }

    public static MenuShiftTotals getInstance() {
        if (ourInstance == null) {
            ourInstance = new MenuShiftTotals();
        }
        return ourInstance;
    }

    public void refreshMenu() {
        getMenuItems().clear();
        getMenuItems().add(MenuItems.SubTotals);
        getMenuItems().add(MenuItems.ShiftTotals);
        getMenuItems().add(MenuItems.ReprintShiftTotals);
    }
}
