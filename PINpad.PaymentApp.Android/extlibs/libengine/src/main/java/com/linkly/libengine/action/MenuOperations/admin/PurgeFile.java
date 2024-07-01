package com.linkly.libengine.action.MenuOperations.admin;

import com.linkly.libengine.action.IAction;

import java.io.File;

public class PurgeFile extends IAction {

    @Override
    public String getName() {
        return "PurgeFile";
    }

    @Override
    public void run() {

        File[] files = new File(mal.getFile().getWorkingDir()).listFiles();

        for (File f : files) {
            f.delete();
        }
    }
}
