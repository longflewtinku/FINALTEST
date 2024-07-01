package com.linkly.payment.menus;

public enum MenuType {

    SUBMENU("MENU"), TASK("TASK");

    private int value;
    private String displayText;

    MenuType(String text) {
        this.value = this.ordinal();
        this.displayText = text;
    }

}
