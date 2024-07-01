package com.linkly.libengine.action.MenuOperations.dev;

import com.linkly.libengine.action.IAction;

public class TestAutoDownload extends IAction {


    @Override
    public String getName() {
        return "TestAutoDownload";
    }

    @Override
    public void run() {
        d.getMessages().sendDownloadRequest(context, false);
    }
}
