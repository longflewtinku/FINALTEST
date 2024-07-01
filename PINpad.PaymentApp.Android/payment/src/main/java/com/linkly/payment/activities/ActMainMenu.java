package com.linkly.payment.activities;

import static com.linkly.libengine.engine.EngineManager.TransType.SALE;
import static com.linkly.libui.UIScreenDef.ENTER_AMOUNT_IDLE;
import static com.linkly.payment.utilities.UIUtilities.validateTerminalConfig;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.tabs.TabLayout;
import com.linkly.libengine.config.PayCfg;
import com.linkly.libengine.config.PayCfgFactory;
import com.linkly.libengine.engine.Engine;
import com.linkly.libengine.overrides.CoreOverrides;
import com.linkly.libengine.users.UserManager;
import com.linkly.libmal.global.platform.EFTPlatform;
import com.linkly.libmal.global.util.DisplayKiosk;
import com.linkly.libpositive.messages.IMessages;
import com.linkly.libui.IUIDisplay;
import com.linkly.libui.currency.CountryCode;
import com.linkly.libui.currency.ISOCountryCodes;
import com.linkly.payment.R;
import com.linkly.payment.application.AppCallbacks;
import com.linkly.payment.customer.ICustomerMenu;
import com.linkly.payment.databinding.ActivityMainmenuBinding;
import com.linkly.payment.fragments.FragHeader;
import com.linkly.payment.fragments.FragInputAmountIdle;
import com.linkly.payment.fragments.FragMainMenu;
import com.linkly.payment.menus.MenuItems;
import com.linkly.payment.utilities.AutoSettlementWatcher;
import com.linkly.payment.utilities.Pci24HourRebootWatcher;
import com.linkly.payment.viewmodel.MainMenuViewModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import timber.log.Timber;

/**
 * An Activity that Hosts Fragments and provides Auth callbacks and state by implementing AuthHost.
 * The MainMenu is the main use-case for this Activity but it does a bunch of other things on
 * the side, some of those may be better solved in child Fragments.
 * <p>
 * Breaks free from the Engine.getDep().getFramework().getUI().showSomeActivity mechanism which
 *  is inherently vulnerable to auth bypass. Instead, manages the display of child Fragments. Note
 *  that ActTransaction must still exist as a standalone Activity in order to facilitate legacy APIs.
 * <p>
 * Keeps custom classes and logic close to where it is needed to increase atomicity. A Fragment per
 *  feature is the way to go when adding to Standalone.
 *  TODO create a progress/error fragment to cover any async delay caused by validateTerminalConfig
 *   and also provide the UI for its error feedback and buttons to avoid use of AlertDialog.
 */
public class ActMainMenu extends BaseActivity<ActivityMainmenuBinding, MainMenuViewModel>
        implements FragHeader.onHeaderMenuItemSelectedListener, AppBarHost, MenuHost,
        PhysicalKeyEventDispatcher {
    private ActivityMainmenuBinding mBinding;
    private TabLayout mTabLayout;
    // Mechanism for registering/unregistering child Fragment listeners to dispatch onKeyUp events to.
    private final List<PhysicalKeyEventListener> mPhysicalKeyEventListeners = new ArrayList<>();

    private final List<ChildBackInterceptCallback> mBackCallbacks = new ArrayList<>();
    private final List<OnMenuRefreshedListener> mOnMenuRefreshedListeners = new ArrayList<>();

    //---------------------------------------------------------------
    // REQUIRED to handle navigation bar. Pax navigation bar changes are System wide.
    // This will break soft lock the terminal...
    // Issues when entering from main menu to system menus EG wifi.
    // SERIOUS DON'T TOUCH THIS UNLESS YOU KNOW WHAT YOU'RE DOING!!!!
    // IF IN A CODE REVIEW YOU SEE THIS COMMENT REMOVED QUESTION HEAVILY WHY THEY ARE DOING THIS!!!
    private DisplayKiosk.NavigationBarState state;
    private boolean overrideToSetBackButtonEnabled = false;
    //---------------------------------------------------------------
    private MainMenuViewModel mainMenuViewModel;

    @Override
    public int getBindingVariable() {
        return com.linkly.payment.BR.mainMenuViewModel;
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_mainmenu;
    }

    @Override
    public MainMenuViewModel initViewModel() {
        mainMenuViewModel = new ViewModelProvider(this).get(MainMenuViewModel.class);
        return mainMenuViewModel;
    }

    @Override
    public MainMenuViewModel getViewModel() {
        return mainMenuViewModel;
    }

    @SuppressWarnings("java:S3776")// Suppressing cognitive complexity(20)
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Timber.d("onCreate...this: %s", this);

        mBinding = getMViewDataBinding();

        validateTerminalConfig(this);

        Timber.i("Main Activity On Create");

        RegisterAsExtendedActivity(IMessages.APP_CHARGING_EVENT);
        RegisterAsExtendedActivity(IMessages.APP_REFRESH_SCREEN_EVENT);
        RegisterAsExtendedActivity(IMessages.APP_FINISH_UI_EVENT);

        if (UserManager.getActiveUser() == null) {
            Timber.i("No user logged in, kill the main menu");
            finishAfterTransition();

            if (!EFTPlatform.startupParams.hideWhenDone) {
                UserManager.userLogin(Engine.getDep(), getApplicationContext());
            }
            return;
        }
        //Create the Menus
        Timber.e("Logged In User: %s", UserManager.getActiveUser().getUserName());

        mBinding.mainContent.setOnTouchListener((v, event) -> {
            ActScreenSaver.resetScreenSaver(getApplicationContext());
            if (Engine.getAppCallbacks() != null) {
                Engine.getAppCallbacks().onUserInteraction();
            }
            Pci24HourRebootWatcher.resetIdle();
            AutoSettlementWatcher.resetIdleState();
            return false;
        });

        Pci24HourRebootWatcher.setIdle();

        mTabLayout = mBinding.tabs;

        routeContent();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Timber.d("onOptionsItemSelected...item: %s", item.getTitle());
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Timber.d("onNewIntent...this: %s, intent: %s", this, intent.toUri(0));
        if (CoreOverrides.get().isDisableUITransitions()) {
            overridePendingTransition(0, 0);
        } else {
            overridePendingTransition(0, R.anim.slide_out_left);
        }
        // reroute content but avoid reinitialising InputAmountIdle to avoid infinite loop :o
        if (isInputAmountAsIdleAllowed()) {
            showInputAmountIdle();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Timber.d("onResume...this: %s", this);

        // Record the old navbar state. (Essentially treat this like a stack)
        state = new DisplayKiosk.NavigationBarState();
        DisplayKiosk.getInstance().enterKioskMode(this);

        /*Added to return to Login Screen if Returned to Main Menu logged out*/
        if (UserManager.getActiveUser() == null) {
            finishAfterTransition();
        }

        // TODO FIXME  shouldn't be in this situation where MainMenu resumes when it is not meant to!
        checkHideWhenDone();

        Pci24HourRebootWatcher.setIdle();

        ActScreenSaver.resetScreenSaver(getApplicationContext());
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        Timber.d("onKeyUp...listener count: %d, keyCode: %d, event: %s",
                mPhysicalKeyEventListeners.size(), keyCode, event);
        // Bug fixing.... FragInputAmountIdle is handled in this activity.
        // Issues where inputting the amount in the FragInputAmountIdle results in key presses would be processed in this activity also
        // We don't want this to happen. So if we are showing the FragInputAmountIdle screen, we "ignore" any keypress.
        // Note: this menu stuff will be completely refactored & extracted into an MPOS app.
        // So not going to do a proper fix.
        if(getSupportFragmentManager().findFragmentByTag(FragInputAmountIdle.TAG) != null &&
                !getSupportFragmentManager().findFragmentByTag(FragInputAmountIdle.TAG).isVisible()) {
            mPhysicalKeyEventListeners.forEach(it -> it.onKeyEvent(keyCode, event));
        }

        return super.onKeyUp(keyCode, event);
    }

    @Override
    public void registerBackListener(ChildBackInterceptCallback callback) {
        Timber.d("registerBackListener...");
        mBackCallbacks.add(callback);
    }

    @Override
    public void unregisterBackListener(ChildBackInterceptCallback callback) {
        Timber.d("unregisterBackListener...");
        mBackCallbacks.remove(callback);
    }

    @Override
    public void registerOnMenuRefreshedListener(OnMenuRefreshedListener callback) {
        Timber.d("registerTabTitleListener...");
        mOnMenuRefreshedListeners.add(callback);
    }

    @Override
    public void unregisterOnMenuRefreshedListener(OnMenuRefreshedListener callback) {
        Timber.d("unregisterTabTitleListener...");
        mOnMenuRefreshedListeners.remove(callback);
    }

    @Override
    public void notifyMenuRefreshed() {
        Timber.d("notifyMenuRefreshed...");
        mOnMenuRefreshedListeners.forEach(OnMenuRefreshedListener::onMenuRefreshed);
    }

    @Override
    @Deprecated
    public void onBackPressed() {
        Timber.d("onBackPressed...");
        // All callbacks registered via (OnBackPressedCallback) addCallback are evaluated upon
        //  this super call.
        super.onBackPressed();
        // In lieu of OnBackPressedCallback working properly, manually callback to registered listeners.
        for (ChildBackInterceptCallback aCallback : mBackCallbacks) {
            if (aCallback.handleOnBackPressed()) {
                // Back event consumed by child listener.
                return;
            }
        }
        // backing out of the app from MainMenu is not supported. Only Admins are allowed to Exit
        //  and that is done from Hamburger Menu.
        showMainMenu();
        AutoSettlementWatcher.resetIdleState();
    }

    @Override
    protected void onPause() {
        Timber.i("onPause...");
        Pci24HourRebootWatcher.clearIdle();
        // Apply reverting the nav bar stack
        if(overrideToSetBackButtonEnabled) {
            DisplayKiosk.getInstance().onResume(true);
            overrideToSetBackButtonEnabled = false;
        } else {
            DisplayKiosk.getInstance().setNavigationBarAndButtonsState(state, true);
        }

        super.onPause();
    }

    @Override
    protected void onDestroy() {
        Timber.i("onDestroy...");
        Pci24HourRebootWatcher.clearIdle();
        super.onDestroy();
    }

    /**
     * Logic to decide which Fragment to show.
     */
    private void routeContent() {
        Timber.d("routeContent...");
        if (isInputAmountAsIdleAllowed()
            && AppCallbacks.getInstance().getShouldMainMenuDisplayInputAmountIdle()) {
            // Always initialise this Activity with InputAmountIdle if it is allowed.
            //  Onus is to ensure that coming back to this Activity sees it re-initialise.
            setupDefaultInputAmountIdle();
            showInputAmountIdle();
        } else {
            showMainMenu();
        }
    }

    private void setupDefaultInputAmountIdle() {
        Timber.d("setupDefaultInputAmountIdle...");
        // Prepare Input Amount screen with transaction type as SALE by default
        HashMap<String, Object> map = new HashMap<>();
        PayCfg payCfg = new PayCfgFactory().getConfig(getApplicationContext());
        CountryCode cCode = ISOCountryCodes.getInstance().getCountryFrom3Num(payCfg.getCurrencyNum() + "");
        map.put(IUIDisplay.uiScreenCurrency, cCode.getAlphaCode());
        map.put(IUIDisplay.uiTitleId, SALE.displayId);

        // Instead of forcing the creation of a new SALE TransRec here (BTW we are in onCreate), do it
        //  if/when the user actually lands on FragInputAmountIdle. Why? Because there are interactions
        //  with this Activity onCreate during e.g. PreSettlement flow whereby a TransRec is being
        //  operated on and would otherwise be CANCELLED by this premature new SALE TransRec.

        // NOTE: doesn't actually show input screen, just prepares the abstract static domain model which is tapped into
        //  on the other side by the Fragment itself. This mechanism is purely for Activities, and prone
        //  to vulnerabilities when content needs to be protected by auth.
        Engine.getDep().getFramework().getUI().showInputScreen(ENTER_AMOUNT_IDLE, map); // Display amount entry
    }

    private void showInputAmountIdle() {
        Timber.d("showInputAmountIdle...");
        // NOTE: avoid early display update by deferring tab & header visibility control to child
        //  Fragment's onResume.

        // NOTE: Always allow it to be replaced to allow its lifecycle to be exercised so it can
        //  potentially setup a TransRec as well. May be able to increase performance by avoiding
        //  this replace, but it would be a bit less robust in doing so (needs very careful testing
        //  attention).

        // actually displays the Fragment
        boolean shouldForceSaleTransRec = AppCallbacks.getInstance().getShouldMainMenuDisplayInputAmountIdle();
        getSupportFragmentManager().beginTransaction()
                .disallowAddToBackStack()
                .replace(R.id.fcv_content, FragInputAmountIdle.newInstance(shouldForceSaleTransRec), FragInputAmountIdle.TAG)
                .commit();
    }

    private void showMainMenu() {
        Timber.d("showMainMenu...");
        Fragment potentialMainMenuFragment = getSupportFragmentManager().findFragmentByTag(FragMainMenu.TAG);
        if (potentialMainMenuFragment != null) {
            // Assume already in foreground, avoid replacing.
            Timber.d("...avoiding replacing FragMainMenu since already found.");
            ((FragMainMenu)potentialMainMenuFragment).refreshAdapter();
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            Fragment potentiallyDanglingFragment = getSupportFragmentManager().findFragmentByTag(FragInputAmountIdle.TAG);
            if (potentiallyDanglingFragment != null) {
                ft.remove(potentiallyDanglingFragment);
            }
            ft.commit();
            // Coming back to a child Fragment does not see its onResume called, so force desired
            //  surrounding state here.
            // If this needs to be expanded upon, consider using OnBackStackChangedListener.
            showTabs(true);
            showHeader(true);
            return;
        }
        Timber.d("...frags list: %s", getSupportFragmentManager().getFragments().toArray());
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fcv_content, FragMainMenu.newInstance(), FragMainMenu.TAG)
                .commit();
    }

    @Override
    protected void finishUI() {
        super.finishUI();
        finishAfterTransition();
    }

    private boolean isInputAmountAsIdleAllowed() {
        Timber.d("isInputAmountAsIdleAllowed...");
        return ((ICustomerMenu) Objects.requireNonNull(Engine.getCustomer())).getMainMenuList(UserManager.getActiveUser().getPrivileges()).contains(MenuItems.Sale);
    }

    @Override
    public void onHeaderMenuItemSelected() {
        Timber.i("Drop Down MainMenu Selected");
    }

    // Call back function to allow the menu options when selected to override the reverted state.
    @Override
    public void onHeaderMenuItemNavigationBarOverride(boolean showBackNavigationBar) {
        overrideToSetBackButtonEnabled = showBackNavigationBar;
    }

    @Override
    public void showTabs(boolean shouldShow) {
        Timber.d("showTabs...shouldShow: %b", shouldShow);
        int visibility = (shouldShow) ? View.VISIBLE : View.GONE;
        mTabLayout.setVisibility(visibility);
    }

    @Override
    public void showHeader(boolean shouldShow) {
        Timber.d("showHeader...shouldShow: %b", shouldShow);
        int visibility = (shouldShow)? View.VISIBLE : View.GONE;
        mBinding.fcvHeader.setVisibility(visibility);
        if (shouldShow) {
            getSupportFragmentManager().beginTransaction()
                    .disallowAddToBackStack()
                    .replace(R.id.fcv_header, FragHeader.newInstance(true))
                    .commit();
        }
    }

    @Override
    public TabLayout getTabLayout() {
        return mTabLayout;
    }

    @Override
    public void registerListener(PhysicalKeyEventListener listener) {
        Timber.d("registerListener[PhysicalKeyEventListener]...");
        mPhysicalKeyEventListeners.add(listener);
    }

    @Override
    public void unregisterListener(PhysicalKeyEventListener listener) {
        Timber.d("unregisterListener[PhysicalKeyEventListener]...");
        mPhysicalKeyEventListeners.remove(listener);
    }
}
