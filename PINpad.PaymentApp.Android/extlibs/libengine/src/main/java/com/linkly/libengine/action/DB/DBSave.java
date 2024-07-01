package com.linkly.libengine.action.DB;

import com.linkly.libengine.action.IAction;
import com.linkly.libengine.engine.transactions.TransRec;

public class DBSave extends IAction {

    @Override
    public String getName() {
        return "DBSave";
    }

    @Override
    public void run() {
        TransRec trans = d.getCurrentTransaction();
        trans.save();
    }
}
