package com.linkly.libui.utils;

import static com.linkly.libui.IUIDisplay.ACTIVITY_ID.ACT_GET_CARD;

import com.linkly.libui.IUIDisplay;

import java.util.concurrent.Callable;

public class WaitForUIResponseCallable implements Callable<IUIDisplay.UIResultCode> {
    IUIDisplay display = null;
    int presentCardTimeMS = 0;
    public WaitForUIResponseCallable(IUIDisplay ui, int timeoutMs) {
        display = ui;
        presentCardTimeMS = timeoutMs;
    }

    @Override
    public IUIDisplay.UIResultCode call() {
        return display.getResultCode(ACT_GET_CARD, presentCardTimeMS);
    }
}