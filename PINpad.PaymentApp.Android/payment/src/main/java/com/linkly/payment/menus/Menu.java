package com.linkly.payment.menus;

import static com.linkly.libui.UIScreenDef.ENTER_AMOUNT_IDLE;
import static com.linkly.payment.menus.MenuType.SUBMENU;
import static com.linkly.payment.menus.MenuType.TASK;

import com.linkly.libengine.config.PayCfg;
import com.linkly.libengine.dependencies.IDependency;
import com.linkly.libengine.engine.EngineManager;
import com.linkly.libengine.engine.transactions.TransRec;
import com.linkly.libengine.workflow.Workflow;
import com.linkly.libengine.workflow.WorkflowScheduler;
import com.linkly.libui.IUIDisplay;
import com.linkly.libui.currency.CountryCode;
import com.linkly.libui.currency.ISOCountryCodes;
import com.linkly.payment.application.AppCallbacks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import timber.log.Timber;

public class Menu implements IMenu {

    private final String menuTitle;
    private final List<MenuItems> menuItems = new ArrayList<>();


    public Menu(String title) {
        Timber.d("constructor[Menu]...title: %s", title);
        this.menuTitle = title;
    }

    @Override
    public String getMenuTitle() {
        return menuTitle;
    }

    @Override
    public List<MenuItems> getMenuItems() {
        return menuItems;
    }

    @Override
    public IMenu reloadMenu() {
        return this;
    }

    @SuppressWarnings("java:S3776")// Cognitive complexity(19)
    @Override
    public IMenu selectMenuItem(MenuItems mEntry, IDependency dependencies) {
        Timber.d("selectMenuItem...item key: %s", mEntry.getKey());
        // Handle the MainMenu Entry
        if (mEntry.getMenuType() == TASK && !mEntry.isDisabled()) {

            if (mEntry.getTransType() != null) {
                Timber.d("...MainMenu TASK has trans type: %s", mEntry.getTransType().name());
                if (!mEntry.getTransType().adminTransaction && !mEntry.getTransType().autoTransaction) {
                    Timber.d("...navigating for MainMenu TASK...displayId: %s", mEntry.getTransType().displayId.name());
                    // Handle menu items related to financial transactions differently, so idle state is still managed for the amount entry,
                    // so that financial transactions workflow is triggered only after an successful amount entry
                    HashMap<String, Object> map = new HashMap<>();
                    PayCfg paycfg = dependencies.getPayCfg();
                    CountryCode cCode = ISOCountryCodes.getInstance().getCountryFrom3Num(paycfg.getCurrencyNum() + "");
                    map.put(IUIDisplay.uiScreenCurrency, cCode.getAlphaCode());
                    map.put(IUIDisplay.uiTitleId, mEntry.getTransType().displayId);
                    AppCallbacks.getInstance().setShouldMainMenuDisplayInputAmountIdle(
                            (mEntry.getTransType() == EngineManager.TransType.SALE));
                    dependencies.resetCurrentTransaction(new TransRec(mEntry.getTransType(), dependencies)); // Ensure transaction type is still retained
                    dependencies.getFramework().getUI().showInputScreen(ENTER_AMOUNT_IDLE, map); // Display amount entry
                } else {
                    Timber.d("...queuing workflow with trans type from MainMenu TASK...");
                    AppCallbacks.getInstance().setShouldMainMenuDisplayInputAmountIdle(false);
                    // TODO FIXME relies on ActIdle existing in the task stack!
                    Workflow w = AppCallbacks.getInstance().getWorkflowFactory().getWorkflow(mEntry.getTransType());
                    dependencies.resetCurrentTransaction(new TransRec(mEntry.getTransType(), dependencies));
                    WorkflowScheduler.getInstance().queueWorkflow(w, false);
                }
                Timber.e("Menu selected : %s", mEntry.getTransType());
            } else if (mEntry.getWorkflow() != null) {
                Timber.d("...queuing workflow that lacks trans type from MainMenu TASK...");
                AppCallbacks.getInstance().setShouldMainMenuDisplayInputAmountIdle(false);
                WorkflowScheduler.getInstance().queueWorkflow(mEntry.getWorkflow(), false);
            } else {
                Timber.i("No longer supported");
                AppCallbacks.getInstance().setShouldMainMenuDisplayInputAmountIdle(false);
            }

        } else if (mEntry.getMenuType() == SUBMENU) {
            try {
                IMenu submenu = (IMenu) mEntry.getSubMenu().getMethod("getInstance").invoke(this);
                Timber.d("...reflecting to invoke submenu getInstance: %s", mEntry.getSubMenu());
                return submenu;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return this;
    }
}
