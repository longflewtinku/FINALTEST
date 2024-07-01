package com.linkly.launcher.applications;
public class AppFunctions {

    public CharSequence displayName;
    public MainMenuFunction function;

    public AppFunctions(MainMenuFunction function, CharSequence displayName) {
        this.function = function;
        this.displayName = displayName;
    }
    public enum MainMenuFunction {
        launchPayment,
        autoRec,
        batchUpload,
        heartBeat,
        runPost,
        runUpdate,
        runForceUpdate,
        runTrans,
        runReversal,
        runInstall,
        resetAutoRec,
        exitKioskMode,
        testPaxStore,
    }
}