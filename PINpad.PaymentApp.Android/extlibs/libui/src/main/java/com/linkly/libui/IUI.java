package com.linkly.libui;

public interface IUI {

    IUI initialiseUI(IUICallbacks uiHandler);
    IUI initialiseUI(IUICallbacks uiHandler, boolean p2peServer);
    void overrideP2P(IUIDisplay ui);

    String getVersion();

    IUIDisplay getUI();

    IUICurrency getCurrency();

    IUIStrings getStrings();

    String getPrompt(IUIDisplay.String_id promptId);

    String getPrompt(IUIDisplay.String_id promptId, String... formatArgs);

    void postUINotification(String title, String message);
}
