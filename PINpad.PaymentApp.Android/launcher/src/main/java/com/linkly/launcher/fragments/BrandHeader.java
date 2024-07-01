package com.linkly.launcher.fragments;


import static android.provider.Settings.ACTION_AIRPLANE_MODE_SETTINGS;
import static com.linkly.libui.IUIDisplay.String_id.STR_AIRPLANE_MODE;
import static com.linkly.libui.IUIDisplay.String_id.STR_APN_SETTINGS;
import static com.linkly.libui.IUIDisplay.String_id.STR_BLUETOOTH_SETTINGS;
import static com.linkly.libui.IUIDisplay.String_id.STR_CELLULAR_SETTINGS;
import static com.linkly.libui.IUIDisplay.String_id.STR_DISPLAY_SETTINGS;
import static com.linkly.libui.IUIDisplay.String_id.STR_ETHERNET_SETTINGS;
import static com.linkly.libui.IUIDisplay.String_id.STR_INJECT_KEYS;
import static com.linkly.libui.IUIDisplay.String_id.STR_INPUT_SETTINGS;
import static com.linkly.libui.IUIDisplay.String_id.STR_SETTINGS;
import static com.linkly.libui.IUIDisplay.String_id.STR_SETUP_WIFI_BASE;
import static com.linkly.libui.IUIDisplay.String_id.STR_SOUND_SETTINGS;
import static com.linkly.libui.IUIDisplay.String_id.STR_WIFI_SETTINGS;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.linkly.launcher.BR;
import com.linkly.launcher.BrandingConfig;
import com.linkly.launcher.R;
import com.linkly.launcher.WifiBaseActivity;
import com.linkly.launcher.databinding.FragmentBrandHeaderBinding;
import com.linkly.launcher.viewmodels.BrandHeaderViewModel;
import com.linkly.launcher.work.UnattendedRebootWorker;
import com.linkly.libconfig.ProfileCfg;
import com.linkly.libmal.MalFactory;
import com.linkly.libmal.global.util.DisplayKiosk;
import com.linkly.libui.IUIDisplay;
import com.linkly.libui.UI;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import timber.log.Timber;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link BrandHeader} factory method to
 * create an instance of this fragment.
 */
public class BrandHeader extends Fragment implements View.OnClickListener, PopupMenu.OnMenuItemClickListener {

    private View view;
    private BrandHeaderViewModel viewModel;
    private FragmentBrandHeaderBinding binding;

    public BrandHeader() {
        // Required empty public constructor
    }

    @Override
    public void onStart() {
        super.onStart();

        try {
            TextView dateView = view.findViewById(R.id.tv_date);
            // date string in Day, Month Day_of_month format (example Monday, October 10)
            String dateString = new SimpleDateFormat("EEEE, MMMM dd", Locale.getDefault()).format(new Date());
            dateView.setText(dateString);
        } catch (Exception ex) {
            Timber.e(ex);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewModel = new ViewModelProvider(this).get(BrandHeaderViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = DataBindingUtil.inflate( inflater, R.layout.fragment_brand_header, container, false );
        binding.setLifecycleOwner(getViewLifecycleOwner());
        view = binding.getRoot();
        binding.setViewModel(viewModel);

        ImageButton upButton = view.findViewById(R.id.menu_but);
        upButton.setOnClickListener(this);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.setVariable(BR.viewModel, viewModel);
        binding.executePendingBindings();

        final Observer<Bitmap> brandDisplayLogoHeaderObserver = newValue -> setupBranding();
        viewModel.getBrandDisplayLogoHeader().observe(getViewLifecycleOwner(), brandDisplayLogoHeaderObserver);

        final Observer<Integer> statusBarColourObserver = newValue -> setupBranding();
        viewModel.getCurrentBrandDisplayStatusBarColour().observe(getViewLifecycleOwner(), statusBarColourObserver);

        setupBranding();
    }

    @Override
    public void onDestroyView() {
        binding = null;
        super.onDestroyView();
    }

    public void showPopup(View v) {
        if (getActivity() != null && !getActivity().isFinishing()) {
            PopupMenu popup = new PopupMenu(getActivity(), v);
            popup.setOnMenuItemClickListener(this);
            MenuInflater inflater = popup.getMenuInflater();
            inflater.inflate(R.menu.menu_header, popup.getMenu());

            initialiseOptionMenus(popup);

            popup.show();
        }
    }

    private void initialiseOptionMenus(PopupMenu popup) {
        String hardwareModel = MalFactory.getInstance().getHardware().getModel();

        boolean showSettingsMenu = ProfileCfg.getInstance().getSettingsMenus().isShowSettingsMenu();
        MenuItem settingsMenu = popup.getMenu().findItem(R.id.action_settings);
        settingsMenu.setTitle(UI.getInstance().getPrompt(STR_SETTINGS));
        settingsMenu.setVisible(showSettingsMenu);

        MenuItem setupWifiBaseMenu = popup.getMenu().findItem(R.id.action_settings_wifibase);
        setupWifiBaseMenu.setTitle(UI.getInstance().getPrompt(STR_SETUP_WIFI_BASE));
        setupWifiBaseMenu.setVisible(showSetupWifiBaseMenu(hardwareModel));

        boolean showBluetoothSettingsMenu = ProfileCfg.getInstance().getSettingsMenus().isShowBluetoothSettingsMenu();
        MenuItem bluetoothSettingsMenu = popup.getMenu().findItem(R.id.action_settings_bluetooth);
        bluetoothSettingsMenu.setTitle(UI.getInstance().getPrompt(STR_BLUETOOTH_SETTINGS));
        bluetoothSettingsMenu.setVisible(showBluetoothSettingsMenu);

        boolean showWifiSettingsMenu = ProfileCfg.getInstance().getSettingsMenus().isShowWifiSettingsMenu();
        MenuItem wifiSettingsMenu = popup.getMenu().findItem(R.id.action_settings_wifi);
        wifiSettingsMenu.setTitle(UI.getInstance().getPrompt(STR_WIFI_SETTINGS));
        wifiSettingsMenu.setVisible(showWifiSettingsMenu);

        boolean showSoundSettingsMenu = ProfileCfg.getInstance().getSettingsMenus().isShowSoundSettingsMenu();
        MenuItem soundSettingsMenu = popup.getMenu().findItem(R.id.action_settings_sound);
        soundSettingsMenu.setTitle(UI.getInstance().getPrompt(STR_SOUND_SETTINGS));
        soundSettingsMenu.setVisible(showSoundSettingsMenu);

        MenuItem ethernetSettingsMenu = popup.getMenu().findItem(R.id.action_settings_ethernet);
        ethernetSettingsMenu.setTitle(UI.getInstance().getPrompt(STR_ETHERNET_SETTINGS));
        ethernetSettingsMenu.setVisible(showEthernetSettingsMenu(hardwareModel));

        MenuItem apnSettingsMenu = popup.getMenu().findItem(R.id.action_settings_apn);
        apnSettingsMenu.setTitle(UI.getInstance().getPrompt(STR_APN_SETTINGS));
        apnSettingsMenu.setVisible(showApnSettingsMenu(hardwareModel));

        boolean showCellularSettingsMenu = ProfileCfg.getInstance().getSettingsMenus().isShowCellularSettingsMenu();
        MenuItem cellularSettingsMenu = popup.getMenu().findItem(R.id.action_settings_cellular);
        cellularSettingsMenu.setTitle(UI.getInstance().getPrompt(STR_CELLULAR_SETTINGS));
        cellularSettingsMenu.setVisible(showCellularSettingsMenu);

        boolean showAirplaneModeMenu = ProfileCfg.getInstance().getSettingsMenus().isShowAirplaneModeMenu();
        MenuItem airplaneModeMenu = popup.getMenu().findItem(R.id.action_airplane_mode);
        airplaneModeMenu.setTitle(UI.getInstance().getPrompt(STR_AIRPLANE_MODE));
        airplaneModeMenu.setVisible(showAirplaneModeMenu);

        boolean showInputSettingsMenu = ProfileCfg.getInstance().getSettingsMenus().isShowInputSettingsMenu();
        MenuItem inputMenu = popup.getMenu().findItem(R.id.action_settings_input);
        inputMenu.setTitle(UI.getInstance().getPrompt(STR_INPUT_SETTINGS));
        inputMenu.setVisible(showInputSettingsMenu);

        boolean showInjectKeysMenu = ProfileCfg.getInstance().getSettingsMenus().isShowInjectKeysSettingsMenu();
        MenuItem injectKeysMenu = popup.getMenu().findItem(R.id.injectKeys);
        injectKeysMenu.setTitle(UI.getInstance().getPrompt(STR_INJECT_KEYS));
        injectKeysMenu.setVisible(showInjectKeysMenu);

        boolean showDisplaySettingsMenu = ProfileCfg.getInstance().getSettingsMenus().isShowDisplaySettingsMenu();
        MenuItem displaySettingsMenu = popup.getMenu().findItem(R.id.action_settings_display);
        displaySettingsMenu.setTitle(UI.getInstance().getPrompt(STR_DISPLAY_SETTINGS));
        displaySettingsMenu.setVisible(showDisplaySettingsMenu);
    }
    @Override
    public void onClick(View v) {
        // Settings menu are shown on the of profile.json, don't show it at all if there is
        // error when loading profile.json configuration
        if (ProfileCfg.getInstance() != null) {
            showPopup(v);
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {

        DisplayKiosk kiosk = DisplayKiosk.getInstance();

        int itemId = item.getItemId();
        if (itemId == R.id.action_settings) {
            kiosk.onResume(true);
            startActivity(new Intent(Settings.ACTION_SETTINGS));
        } else if (itemId == R.id.action_settings_wifibase) {
            kiosk.onResume(false);
            Intent wifibaseIntent = new Intent(this.getActivity(), WifiBaseActivity.class);
            startActivity(wifibaseIntent);
        } else if (itemId == R.id.action_settings_bluetooth) {
            kiosk.onResume(true);
            kiosk.onResume(true);
            startActivity(new Intent(Settings.ACTION_BLUETOOTH_SETTINGS));
        } else if (itemId == R.id.action_settings_input) {
            kiosk.onResume(true);
            startActivity(new Intent(Settings.ACTION_INPUT_METHOD_SETTINGS));
        } else if (itemId == R.id.action_settings_wifi) {
            kiosk.onResume(true);
            startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
        } else if (itemId == R.id.action_settings_sound) {
            kiosk.onResume(true);
            startActivity(new Intent(Settings.ACTION_SOUND_SETTINGS));
        } else if (itemId == R.id.action_settings_ethernet) {
            kiosk.onResume(true);
            try {
                startActivity(new Intent("android.settings.ETHERNET_SETTINGS"));
            } catch( Exception e ){
                Timber.e(e);
                // hide back button
                kiosk.onResume(false);
            }
        } else if (itemId == R.id.action_settings_apn) {
            kiosk.onResume(true);
            startActivity(new Intent(Settings.ACTION_APN_SETTINGS));
        } else if (itemId == R.id.action_settings_cellular) {
            kiosk.onResume(true);
            Intent cellularIntent = new Intent();
            cellularIntent.setAction("android.intent.action.MAIN");
            cellularIntent.setComponent(new ComponentName("com.android.phone", "com.android.phone.MobileNetworkSettings"));
            startActivity(cellularIntent);
        } else if (itemId == R.id.action_airplane_mode) {
            kiosk.onResume(true);
            Intent intent = new Intent(ACTION_AIRPLANE_MODE_SETTINGS);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } else if (itemId == R.id.action_settings_display) {
            kiosk.onResume(true);
            startActivity(new Intent(Settings.ACTION_DISPLAY_SETTINGS));
        } else if (itemId == R.id.action_reboot) {
            kiosk.onResume(false);
            Timber.i("...reboot requested...");
            // Offer user a chance to verify as safety precaution before rebooting.
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setMessage(UI.getInstance().getPrompt(IUIDisplay.String_id.STR_REBOOT_PROMPT))
                    .setCancelable(true)
                    .setPositiveButton(UI.getInstance().getPrompt(IUIDisplay.String_id.STR_REBOOT_CONFIRMATION), (dialog, id) -> performReboot())
                    .setNegativeButton(UI.getInstance().getPrompt(IUIDisplay.String_id.STR_REBOOT_CANCEL), (dialog, id) -> cancelReboot());
            AlertDialog alert = builder.create();
            alert.show();
        } else {
            return false;
        }

        return true;
    }
    private void cancelReboot() {
        Timber.d("cancelReboot...");
        // Do nothing
    }

    private void performReboot() {
        Timber.e("performReboot...");
        UnattendedRebootWorker.rebootNow(getContext());
    }

    private void setupBranding() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && getActivity() != null) {
            getActivity().getWindow().setStatusBarColor(BrandingConfig.getBrandDisplayStatusBarColourOrDefault( binding.getRoot().getContext().getColor(R.color.color_linkly_primary)));
        }
        binding.header.setImageBitmap(viewModel.getBrandDisplayLogoHeader().getValue());
    }

    private boolean showSetupWifiBaseMenu(String hardwareModel) {
        return ProfileCfg.getInstance().getSettingsMenus().isShowSetupWifiBaseMenu()
                && hardwareModel.compareTo("A35") != 0
                && hardwareModel.compareTo("A77") != 0;
    }

    private boolean showEthernetSettingsMenu(String hardwareModel) {
        return ProfileCfg.getInstance().getSettingsMenus().isShowEthernetSettingsMenu()
                && hardwareModel.compareTo("A77") != 0;
    }

    private boolean showApnSettingsMenu(String hardwareModel) {
        return ProfileCfg.getInstance().getSettingsMenus().isShowApnSettingsMenu()
                // check SIM presence for "A35" only as requested in ticket
                && (hardwareModel.compareTo("A35") != 0 || MalFactory.getInstance().getComms().isSIMCardPresent(getContext()));
    }
}
