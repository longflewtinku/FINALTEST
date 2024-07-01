package com.linkly.payment.fragments;


import static android.provider.Settings.ACTION_AIRPLANE_MODE_SETTINGS;
import static com.linkly.libpositive.messages.IMessages.APP_REFRESH_SCREEN_EVENT;
import static com.linkly.libui.IUIDisplay.String_id.STR_AIRPLANE_MODE;
import static com.linkly.libui.IUIDisplay.String_id.STR_APN_SETTINGS;
import static com.linkly.libui.IUIDisplay.String_id.STR_APP_SETTINGS;
import static com.linkly.libui.IUIDisplay.String_id.STR_CELLULAR_SETTINGS;
import static com.linkly.libui.IUIDisplay.String_id.STR_DISPLAY_SETTINGS;
import static com.linkly.libui.IUIDisplay.String_id.STR_ETHERNET_SETTINGS;
import static com.linkly.libui.IUIDisplay.String_id.STR_EXIT;
import static com.linkly.libui.IUIDisplay.String_id.STR_INPUT_SETTINGS;
import static com.linkly.libui.IUIDisplay.String_id.STR_LOGOUT;
import static com.linkly.libui.IUIDisplay.String_id.STR_SETTINGS;
import static com.linkly.libui.IUIDisplay.String_id.STR_SOUND_SETTINGS;
import static com.linkly.libui.IUIDisplay.String_id.STR_TOTALS_SETTINGS;
import static com.linkly.libui.IUIDisplay.String_id.STR_WIFI_SETTINGS;
import static com.linkly.payment.R.id.batch_count;
import static com.linkly.payment.R.id.batch_indicator;
import static com.linkly.payment.R.id.battery_indicator;
import static com.linkly.payment.R.id.menu_but;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.PreferenceManager;

import com.linkly.libconfig.ProfileCfg;
import com.linkly.libengine.engine.Engine;
import com.linkly.libengine.engine.transactions.TransRec;
import com.linkly.libengine.overrides.CoreOverrides;
import com.linkly.libengine.users.UserManager;
import com.linkly.libmal.IMalPrint;
import com.linkly.libmal.MalFactory;
import com.linkly.libmal.global.util.Util;
import com.linkly.libui.IUIDisplay.String_id;
import com.linkly.libui.UI;
import com.linkly.payment.R;
import com.linkly.payment.activities.ActScreenSaver;
import com.linkly.payment.activities.ActSettings;
import com.linkly.payment.activities.ActSplash;
import com.linkly.payment.activities.ActTotalsSettings;
import com.linkly.payment.activities.AppMain;
import com.linkly.payment.utilities.AutoSettlementWatcher;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import timber.log.Timber;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link FragHeader.onHeaderMenuItemSelectedListener} interface
 * to handle interaction events.
 * Use the {@link FragHeader} factory method to
 * create an instance of this fragment.
 */
public class FragHeader extends Fragment implements View.OnClickListener, PopupMenu.OnMenuItemClickListener {

    public static final String TAG = "FragHeader";

    private static final String KEY_SHOULD_SETUP_FOR_MAINMENU_USECASE = "shouldSetupForMainMenuUseCase";
    private static File imgFile = null;
    private static Bitmap myBitmap = null;
    private View view;
    private onHeaderMenuItemSelectedListener menuListener;
    public static int transCount = 0;

    private boolean mShouldSetupForMainMenuUseCase = false;

    public static FragHeader newInstance() {
        return newInstance(false);
    }

    public static FragHeader newInstance(boolean shouldSetupForMainMenuUseCase) {
        Timber.d("newInstance...shouldSetupForMainMenuUseCase: %b", shouldSetupForMainMenuUseCase);
        FragHeader frag = new FragHeader();
        Bundle args = new Bundle();
        args.putBoolean(KEY_SHOULD_SETUP_FOR_MAINMENU_USECASE, shouldSetupForMainMenuUseCase);
        frag.setArguments(args);
        return frag;
    }

    protected BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            if (intent != null) {
                Timber.d("onReceive...intent: %s", intent.toUri(0));
                String action = intent.getAction();

                if(action != null && action.equals(APP_REFRESH_SCREEN_EVENT)) {
                    refreshUI();
                }
            }
        }
    };


    public FragHeader() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Timber.d("onCreate...savedInstanceState: %s", savedInstanceState);
        if (getArguments() != null
                && getArguments().containsKey(KEY_SHOULD_SETUP_FOR_MAINMENU_USECASE)) {
            mShouldSetupForMainMenuUseCase = getArguments().getBoolean(KEY_SHOULD_SETUP_FOR_MAINMENU_USECASE);
        }
        Timber.d("...resolved args: mShouldSetupForMainMenuUseCase: %b", mShouldSetupForMainMenuUseCase);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_brand_header_large, container, false);

        ImageButton upButton =  view.findViewById(menu_but);
        upButton.setOnClickListener(this);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Timber.d("onViewCreated...");
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        Timber.d("onViewStateRestored...");
        if (savedInstanceState != null
                && savedInstanceState.containsKey(KEY_SHOULD_SETUP_FOR_MAINMENU_USECASE)) {
            mShouldSetupForMainMenuUseCase = savedInstanceState.getBoolean(KEY_SHOULD_SETUP_FOR_MAINMENU_USECASE);
            Timber.d("...shouldSetupForMainMenuUseCase: %b", mShouldSetupForMainMenuUseCase);
        }

        refreshUI();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        Timber.d("onAttach...");
        try {
            if( context instanceof onHeaderMenuItemSelectedListener ) {
                menuListener = (onHeaderMenuItemSelectedListener) context;
            }
        } catch (ClassCastException e) {
            Timber.d(e);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Timber.d("onStart...shouldSetupForMainMenuUseCase: %b", mShouldSetupForMainMenuUseCase);

        view.setOnTouchListener( ( view, motionEvent ) -> {
            ActScreenSaver.resetScreenSaver(requireContext().getApplicationContext());
            return view.performClick();
        } );

        if (!mShouldSetupForMainMenuUseCase) {
            LinearLayout batch = view.findViewById(batch_indicator);
            batch.setVisibility(View.GONE);
        }

        //Try and Load Branded Gear Here
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = false;
            options.inPreferredConfig = Bitmap.Config.RGB_565;
            String lightBackgroundFileName = "/header.png";
            String darkBackgroundFileName = "/header_dark_background.png";
            String fileToUse;

            fileToUse = Engine.getDep().getPayCfg().getBrandDisplayLogoHeader();
            if (!Util.isNullOrEmpty(fileToUse)) {
                fileToUse = "/" + fileToUse;
            } else {
                if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    fileToUse = darkBackgroundFileName;
                } else {
                    fileToUse = lightBackgroundFileName;
                }
            }

            if (imgFile == null || myBitmap == null) {
                String basePath = MalFactory.getInstance().getFile().getCommonDir();// "/data/data/" + pth + "/files"
                imgFile = new File(basePath + fileToUse);
                if (imgFile.exists()) {
                    myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath(), options);
                }
            }

            ImageView headerLogo = view.findViewById(R.id.header);
            if (myBitmap != null) {
                headerLogo.setImageBitmap(myBitmap);
            } else {
                headerLogo.setVisibility(View.GONE);
            }

        } catch (Exception ex) {
            Timber.w(ex);
        }

        TextView dateView = view.findViewById(R.id.tv_date);
        // date string in Day, Month Day_of_month format (example Monday, October 10)
        String dateString = new SimpleDateFormat("EEEE, MMMM dd", Locale.getDefault()).format(new Date());
        dateView.setText(dateString);
    }

    @Override
    public void onResume() {
        super.onResume();
        Timber.d("onResume...");
        if (mShouldSetupForMainMenuUseCase) {
            LocalBroadcastManager.getInstance(requireContext().getApplicationContext())
                    .registerReceiver(mMessageReceiver, new IntentFilter(APP_REFRESH_SCREEN_EVENT));
        }
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onMenuItemClick(MenuItem item) {
        AutoSettlementWatcher.resetIdleState();
        switch(item.getItemId()) {
            case R.id.action_logout:
                if (UserManager.getActiveUser() != null) {
                    performLogout();
                    return true;
                }
                // intentional fall through
                exitApplication();
                break;
            case R.id.action_exit:
                exitApplication();
                break;
            case R.id.action_settings:
                startActivity(Settings.ACTION_SETTINGS);
                break;
            case R.id.action_settings_input:
                startActivity(Settings.ACTION_INPUT_METHOD_SETTINGS);
                break;
            case R.id.action_settings_display:
                startActivity(Settings.ACTION_DISPLAY_SETTINGS);
                break;
            case R.id.action_settings_wifi:
                startActivity(Settings.ACTION_WIFI_SETTINGS);
                break;
            case R.id.action_settings_ethernet:
                try {
                    startActivity("android.settings.ETHERNET_SETTINGS"); // no variable for Ethernet settings...
                } catch(ActivityNotFoundException e) { // Unlike other settings, this is a custom one and may not exist.
                    Timber.d("Ethernet Settings not Available");
                    menuListener.onHeaderMenuItemNavigationBarOverride(false); // Revert our navigation bar change.
                }
                break;
            case R.id.action_settings_sound:
                startActivity(Settings.ACTION_SOUND_SETTINGS);
                break;
            case R.id.action_settings_apn:
                startActivity(Settings.ACTION_APN_SETTINGS);
                break;
            case R.id.action_settings_cellular:
                ActScreenSaver.cancelScreenSaver();
                menuListener.onHeaderMenuItemNavigationBarOverride(true);
                Intent cellularIntent = new Intent();
                cellularIntent.setAction("android.intent.action.MAIN");
                cellularIntent.setComponent(new ComponentName("com.android.phone", "com.android.phone.MobileNetworkSettings"));
                startActivity(cellularIntent);
                break;
            case R.id.action_airplane_mode:
                ActScreenSaver.cancelScreenSaver();
                menuListener.onHeaderMenuItemNavigationBarOverride(true);
                Intent intent = new Intent(ACTION_AIRPLANE_MODE_SETTINGS);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                break;
            case R.id.app_settings:
                ActScreenSaver.cancelScreenSaver();
                menuListener.onHeaderMenuItemNavigationBarOverride(true);
                startActivity(new Intent(getContext(), ActSettings.class));
                break;
            case R.id.totals_settings:
                ActScreenSaver.cancelScreenSaver();
                menuListener.onHeaderMenuItemNavigationBarOverride(true);
                startActivity(new Intent(getContext(), ActTotalsSettings.class));
                break;
            default:
                return false;
        }

        return true;
    }

    @Override
    public void onClick(View v) {

        if (v.getId() == menu_but) {
            showPopup(v);
            // fall through
        }
        ActScreenSaver.resetScreenSaver(requireContext().getApplicationContext());
        AutoSettlementWatcher.resetIdleState();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Timber.d("onSaveInstanceState...shouldSetupForMainMenuUseCase: %b", mShouldSetupForMainMenuUseCase);
        outState.putBoolean(KEY_SHOULD_SETUP_FOR_MAINMENU_USECASE, mShouldSetupForMainMenuUseCase);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onPause() {
        Timber.d("onPause...");
        if (mShouldSetupForMainMenuUseCase) {
            LocalBroadcastManager.getInstance(requireContext().getApplicationContext())
                    .unregisterReceiver(mMessageReceiver);
        }
        super.onPause();
    }

    @Override
    public void onDetach() {
        menuListener = null;
        view = null;
        super.onDetach();
    }

    @Override
    public void onDestroy() {
        AppMain.addWatcher( this );
        super.onDestroy();
    }

    public void refreshBatteryIndicator() {

        if (CoreOverrides.get().isRunningAutoTests())
            return;

        LinearLayout battery =  view.findViewById(battery_indicator);
        TextView baterryText = view.findViewById(R.id.text_battery_low);

        try {

            //Highjack this to display Low Battery Notification
            if (MalFactory.getInstance() != null) {
                IMalPrint.PrinterReturn status = MalFactory.getInstance().getHardware().getMalPrint().getPrinterStatus();

                if (status == IMalPrint.PrinterReturn.VOLTAGE_LOW) {
                    battery.setVisibility(View.VISIBLE);
                    if (baterryText != null) {
                        baterryText.setText(Engine.getDep().getPrompt(String_id.STR_BATTERY_LOW));
                    }
                    return;
                }
                battery.setVisibility(View.GONE);
            }
        } catch (Exception ex) {
            battery.setVisibility(View.GONE);
        }
    }

    public void refreshBatchIndicator() {
        Timber.d("refreshBatchIndicator...");

        if (CoreOverrides.get().isRunningAutoTests())
            return;

        LinearLayout batch =  view.findViewById(batch_indicator);
        try {
            transCount = TransRec.countTransInBatch();

            if (transCount > 0) {
                batch.setVisibility(View.VISIBLE);
                TextView batchCount =  view.findViewById(batch_count);
                batchCount.setText(String.valueOf(transCount));
            } else {
                Timber.d("...transCount is zero.");
                batch.setVisibility(View.GONE);
            }
        } catch (Exception ex) {
            batch.setVisibility(View.GONE);
        }

    }

    public void showPopup(View v) {
        PopupMenu popup = new PopupMenu(requireActivity(), v);
        popup.setOnMenuItemClickListener(this);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.menu_header, popup.getMenu());

        ActScreenSaver.resetScreenSaver(requireContext().getApplicationContext());
        initialiseOptionMenus(popup);
        popup.show();
    }

    private void initialiseOptionMenus(PopupMenu popup) {
        MenuItem logOutMenu = popup.getMenu().findItem(R.id.action_logout);
        logOutMenu.setTitle(Engine.getDep().getPrompt(STR_LOGOUT));

        boolean showSettingsMenu = ProfileCfg.getInstance().getSettingsMenus().isShowSettingsMenu();
        MenuItem settingsMenu = popup.getMenu().findItem(R.id.action_settings);
        settingsMenu.setTitle(UI.getInstance().getPrompt(STR_SETTINGS));

        boolean showWifiSettingsMenu = ProfileCfg.getInstance().getSettingsMenus().isShowWifiSettingsMenu();
        MenuItem wifiSettingsMenu = popup.getMenu().findItem(R.id.action_settings_wifi);
        wifiSettingsMenu.setTitle(Engine.getDep().getPrompt(STR_WIFI_SETTINGS));

        boolean showSoundSettingsMenu = ProfileCfg.getInstance().getSettingsMenus().isShowSoundSettingsMenu();
        MenuItem soundSettingsMenu = popup.getMenu().findItem(R.id.action_settings_sound);
        soundSettingsMenu.setTitle(UI.getInstance().getPrompt(STR_SOUND_SETTINGS));
        soundSettingsMenu.setVisible(showSoundSettingsMenu);

        boolean showEthernetSettingsMenu = ProfileCfg.getInstance().getSettingsMenus().isShowEthernetSettingsMenu();
        MenuItem ethernetSettingsMenu = popup.getMenu().findItem(R.id.action_settings_ethernet);
        ethernetSettingsMenu.setTitle(Engine.getDep().getPrompt(STR_ETHERNET_SETTINGS));

        boolean showApnSettingsMenu = ProfileCfg.getInstance().getSettingsMenus().isShowApnSettingsMenu();
        MenuItem apnSettingsMenu = popup.getMenu().findItem(R.id.action_settings_apn);
        apnSettingsMenu.setTitle(Engine.getDep().getPrompt(STR_APN_SETTINGS));

        boolean showCellularSettingsMenu = ProfileCfg.getInstance().getSettingsMenus().isShowCellularSettingsMenu();
        MenuItem cellularSettingsMenu = popup.getMenu().findItem(R.id.action_settings_cellular);
        cellularSettingsMenu.setTitle(Engine.getDep().getPrompt(STR_CELLULAR_SETTINGS));

        boolean showAirplaneModeMenu = ProfileCfg.getInstance().getSettingsMenus().isShowAirplaneModeMenu();
        MenuItem airplaneModeMenu = popup.getMenu().findItem(R.id.action_airplane_mode);
        airplaneModeMenu.setTitle(UI.getInstance().getPrompt(STR_AIRPLANE_MODE));

        boolean showDisplaySettingsMenu = ProfileCfg.getInstance().getSettingsMenus().isShowDisplaySettingsMenu();
        MenuItem displaySettingsMenu = popup.getMenu().findItem(R.id.action_settings_display);
        displaySettingsMenu.setTitle(Engine.getDep().getPrompt(STR_DISPLAY_SETTINGS));

        MenuItem appSettingsMenu = popup.getMenu().findItem(R.id.app_settings);
        appSettingsMenu.setTitle(Engine.getDep().getPrompt(STR_APP_SETTINGS));

        MenuItem totalsSettingsMenu = popup.getMenu().findItem(R.id.totals_settings);
        totalsSettingsMenu.setTitle(Engine.getDep().getPrompt(STR_TOTALS_SETTINGS));

        boolean showInputSettingsMenu = ProfileCfg.getInstance().getSettingsMenus().isShowInputSettingsMenu();
        MenuItem inputMenu = popup.getMenu().findItem(R.id.action_settings_input);
        inputMenu.setTitle(UI.getInstance().getPrompt(STR_INPUT_SETTINGS));

        MenuItem exitMenu = popup.getMenu().findItem(R.id.action_exit);
        exitMenu.setTitle(Engine.getDep().getPrompt(STR_EXIT));

        if (UserManager.getActiveUser() != null) {
            //Remove Options Not Required
            cellularSettingsMenu.setVisible(showCellularSettingsMenu);
            settingsMenu.setVisible(showSettingsMenu);
            displaySettingsMenu.setVisible(showDisplaySettingsMenu);
            wifiSettingsMenu.setVisible(showWifiSettingsMenu);
            ethernetSettingsMenu.setVisible(showEthernetSettingsMenu);
            airplaneModeMenu.setVisible(showAirplaneModeMenu);
            inputMenu.setVisible(showInputSettingsMenu);
            appSettingsMenu.setVisible(showApnSettingsMenu);
        } else {
            /*Remove all Items other than Quit when not logged in*/
            exitMenu.setVisible(true);
            logOutMenu.setVisible(false);
            cellularSettingsMenu.setVisible(true);
            settingsMenu.setVisible(false);
            displaySettingsMenu.setVisible(false);
            wifiSettingsMenu.setVisible(false);
            ethernetSettingsMenu.setVisible(false);
            inputMenu.setVisible(false);
            appSettingsMenu.setVisible(false);
            totalsSettingsMenu.setVisible(false);
        }
    }

    private void performLogout(){
        Timber.i("LOGOUT REQUEST" );
        if( menuListener != null ) {
            menuListener.onHeaderMenuItemSelected();
        }
        UserManager.logoutActiveUser();
        // Because ActSplash has launchMode="singleTask" all Activities above it in the stack will be finished.
        Intent clearTaskStackIntent = new Intent(getContext(), ActSplash.class);
        startActivity(clearTaskStackIntent);
    }

    // Reduces a bunch of common code...
    private void startActivity(String activityName) {
        ActScreenSaver.cancelScreenSaver();
        // As we are exiting override our base activity to state that we are overriding the button.
        // As menus are "apart" of other fragments, we need to change this at the activity level.
        menuListener.onHeaderMenuItemNavigationBarOverride(true);
        startActivity(new Intent(activityName));
    }

    private void exitApplication() {
        if (Engine.getAppCallbacks() != null) {
            Engine.getAppCallbacks().exitApplication();
        }
    }

    public void setDropDownVisibility( boolean isVisible ) {
        if ( getView() != null ) {
            ImageButton upButton = getView().findViewById( menu_but );
            if ( upButton != null ) {
                upButton.setVisibility( isVisible ? View.VISIBLE : View.GONE );
            }
        }
    }

    public void setCustomerLogoVisibility( boolean isVisible ) {
        if ( null != getView() ) {
            View logo = getView().findViewById( R.id.header );
            if ( null != logo ) {
                logo.setVisibility( isVisible ? View.VISIBLE : View.INVISIBLE );
            }
        }
    }

    public void setHeaderBarVisibility( boolean isVisible ) {
        if( MalFactory.getInstance() == null ) {
            return;
        }
        boolean dateMenuPanelVisible = PreferenceManager.getDefaultSharedPreferences(requireContext().getApplicationContext()).getBoolean("DateMenuPanel", true);
        isVisible &= dateMenuPanelVisible;

        if ( view != null ) {
            View headerBanner = view.findViewById( R.id.headerBar );
            if ( headerBanner != null ) {
                headerBanner.setVisibility(isVisible ? View.VISIBLE : View.GONE);
            }
        }
    }

    private void refreshUI() {
        Timber.d("refreshUI...shouldSetupForMainMenuUseCase: %b", mShouldSetupForMainMenuUseCase);
        if (mShouldSetupForMainMenuUseCase) {
            setHeaderBarVisibility(true);
            refreshBatchIndicator();
            refreshBatteryIndicator();
        } else {
            Timber.d("...ignoring request to refreshUI since FragHeader NOT in MainMenu use-case.");
        }
    }

    public void setUseCase(boolean shouldSetupForMainMenuUseCase) {
        Timber.d("setUseCase...");
        mShouldSetupForMainMenuUseCase = shouldSetupForMainMenuUseCase;
        if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED)) {
            refreshUI();
        }
    }

    // Define Interfaces that Hosting activities should handle - These should be option, but menu should limit options based
    // on the interfaces implemented by the calling activity... if that's possible
    public interface onHeaderMenuItemSelectedListener {
        void onHeaderMenuItemSelected();

        /*
        Used for edge cases where we want to specifically override reverting the navigation bar that was originally set when the fragment was generated.
        Required as most of these options start external system menus. Issues arise as the nav bar changes are system wide and stateful.
        Originally the terminal would revert to the "last state" which usually is not showing a bar. This would soft lock our terminal.
         */
        void onHeaderMenuItemNavigationBarOverride(boolean showBackNavigationBar);
    }
}
