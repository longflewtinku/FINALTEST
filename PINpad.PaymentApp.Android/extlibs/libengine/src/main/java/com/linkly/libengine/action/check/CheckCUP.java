package com.linkly.libengine.action.check;

import com.linkly.libengine.action.IAction;
import com.linkly.libengine.dependencies.IDependency;

public class CheckCUP extends IAction {
    @Override
    public String getName() {
        return "CheckCUP";
    }

    @Override
    public void run() {
        this.checkCup(d);
    }

    private void checkCup(IDependency dependencies) {
        // No support for cup card right now. To add back if we need it
    }
}
