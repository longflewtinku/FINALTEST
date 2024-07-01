package com.linkly.libengine.action.MenuOperations.dev;

import static com.linkly.libengine.engine.reporting.ReconciliationManager.reconciliationDao;
import static com.linkly.libui.UIScreenDef.DATABASE_CLEARED;
import static com.linkly.libui.UIScreenDef.PLEASE_WAIT_BR;

import com.linkly.libengine.action.IAction;
import com.linkly.libengine.engine.reporting.ReconciliationManager;
import com.linkly.libengine.engine.transactions.TransRecManager;

public class DBClear extends IAction {

    @Override
    public String getName() {
        return "DBClear";
    }

    @Override
    public void run() {
        ui.showScreen(PLEASE_WAIT_BR);

        TransRecManager.getInstance().getTransRecDao().deleteAll();
        ReconciliationManager.getInstance(); // create instance if not already done
        reconciliationDao.deleteAll();

        ui.showScreen(DATABASE_CLEARED);
        ui.displayMainMenuScreen();
    }
}
