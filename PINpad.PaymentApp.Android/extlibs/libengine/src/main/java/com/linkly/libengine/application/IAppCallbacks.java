package com.linkly.libengine.application;

import android.content.Context;

import com.linkly.libengine.dependencies.IDependency;
import com.linkly.libengine.workflow.IWorkflowFactory;
import com.linkly.libmal.global.platform.StartupParams;

public interface IAppCallbacks {

    IAppCallbacks Initialise(IDependency d);

    void runApplication(Context context, StartupParams startupParams);
    void exitApplication();
    boolean initialiseP2Pe(Context context);

    void runPleaseWaitScreen();

    IWorkflowFactory getWorkflowFactory();
    void setWorkflowFactory(IWorkflowFactory workflowFactory);
    void enterMainMenuIdleState();
    void exitMainMenuIdleState();
    void displayInternetAvailabilityNotice(String message, String customNotificationSoundPath);
    void cancelInternetAvailabilityNotice();

    // Intended for management of global timers, etc.
    void onUserInteraction();
    void onTransactionFlowEntered();
    void onTransactionFlowExited();
    void onLogin();
    void onLogout();

    void setShouldMainMenuDisplayInputAmountIdle(boolean shouldMainMenuDisplayInputAmountIdle);
    public boolean getShouldMainMenuDisplayInputAmountIdle();
}
