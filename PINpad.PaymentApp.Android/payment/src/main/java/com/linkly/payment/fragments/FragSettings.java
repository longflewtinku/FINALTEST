    package com.linkly.payment.fragments;

    import static android.widget.Toast.LENGTH_LONG;
    import static com.linkly.libmal.global.platform.Platform.TerminalModel.A30;

    import android.annotation.SuppressLint;
    import android.content.SharedPreferences;
    import android.os.Bundle;
    import android.widget.Toast;

    import androidx.annotation.NonNull;
    import androidx.fragment.app.Fragment;
    import androidx.preference.DropDownPreference;
    import androidx.preference.Preference;
    import androidx.preference.PreferenceFragmentCompat;
    import androidx.preference.PreferenceManager;

    import com.linkly.libmal.MalFactory;
    import com.linkly.libmal.global.platform.EFTPlatform;
    import com.linkly.libui.IUIDisplay;
    import com.linkly.payment.R;
    import com.linkly.payment.activities.AppMain;
    import com.linkly.payment.utilities.ConfirmDialogFragment;

    import java.util.Arrays;
    import java.util.List;

    import timber.log.Timber;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FragSettings#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FragSettings extends PreferenceFragmentCompat implements Preference.OnPreferenceChangeListener, Preference.OnPreferenceClickListener {
    private static final String SCREEN_SAVER_TIMER_PREF_KEY = "screenSaverTimer";
    private static final String MERCHANT_RECEIPT_FIRST_PREF_KEY = "merchantReceiptFirst";
    private static final String PRINT_MERCHANT_RECEIPT_PREF_KEY = "printMerchantReceipt";
    private static final String PRINT_CONTACTLESS_RECEIPTS_PREF_KEY = "printContactlessReceipts";
    private static final String PRINT_TO_SCREEN_PREF_KEY = "printToScreen";
    private static final String AUTO_LOGIN_PREF_KEY = "autologin";
    private static final String AUTO_LOGOFF_PREF_KEY = "autoLogoff";
    private static final String AUTO_LOGOFF_TIMEOUT_PREF_KEY = "autoLogoffTimeout";
    private static final String DATE_MENU_PANEL_PREF_KEY = "DateMenuPanel";
    private static final String DISPLAY_DEV_MENU_PREF_KEY = "DisplayDevMenu";
    private static final String USB_MODE_PREF_KEY = "usb_mode";
    private static final String RESET_TO_DEFAULT_PREF_KEY = "resetToDefault";

    public FragSettings() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment FragSettings.
     */
    public static FragSettings newInstance() {
        return new FragSettings();
    }


    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);

        DropDownPreference dropDownPreference = this.findPreference(USB_MODE_PREF_KEY);
        if( A30 == EFTPlatform.getTerminalModel() && dropDownPreference != null ){
            dropDownPreference.setVisible( true );

            dropDownPreference.setOnPreferenceChangeListener( this );
        }

        Preference resetToDefaultPreference = findPreference(RESET_TO_DEFAULT_PREF_KEY);
        if (resetToDefaultPreference != null) {
            resetToDefaultPreference.setOnPreferenceClickListener(this);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        AppMain.addWatcher( this );
    }

    @Override
    public boolean onPreferenceChange(@NonNull Preference preference, Object newValue ) {
        Timber.d("Preference changed of [" + preference + "], new value = " + newValue );
        List<String> usbModes = Arrays.asList( getResources().getStringArray( R.array.usb_modes ) );
        boolean changed = true;

        if( usbModes.contains( newValue ) ){
            int index = usbModes.indexOf( newValue );

            try {
                MalFactory.getInstance().getHardware().getDal().getSys().setUsbMode( index );
            } catch ( Exception e ){
                Timber.w(e);
                changed = false;
            }
        } else {
            Timber.w("usbModes = " + usbModes + " do not contain = " + newValue );
            changed = false;
        }

        return changed;
    }

    @Override
    public boolean onPreferenceClick(@NonNull Preference preference) {
        if (RESET_TO_DEFAULT_PREF_KEY.equals(preference.getKey())) {
            ConfirmDialogFragment.newInstance(IUIDisplay.String_id.STR_CONFIRM_RESET_LOCAL_APP_SETTINGS.getId(), new ConfirmDialogFragment.ConfirmDialogListener() {
                @Override
                public void onDialogPositiveClick() {
                    resetLocalAppSettings();
                    Toast.makeText(requireActivity().getApplicationContext(), IUIDisplay.String_id.STR_LOCAL_APP_SETTINGS_RESET.getId(), LENGTH_LONG).show();
                    requireActivity().finishAfterTransition();
                }

                @Override
                public void onDialogNegativeClick() {
                    // no-op
                }
            }).show(getParentFragmentManager(), null);
        }
        return true;
    }

    @SuppressLint("ApplySharedPref")
    private void resetLocalAppSettings() {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(requireContext()).edit();
        editor.putInt(SCREEN_SAVER_TIMER_PREF_KEY, getResources().getInteger(R.integer.defaultScreenSaverTimer));
        editor.putBoolean(MERCHANT_RECEIPT_FIRST_PREF_KEY, getResources().getBoolean(R.bool.defaultMerchantReceiptFirst));
        editor.putBoolean(PRINT_MERCHANT_RECEIPT_PREF_KEY, getResources().getBoolean(R.bool.defaultPrintMerchantReceipt));
        editor.putBoolean(PRINT_CONTACTLESS_RECEIPTS_PREF_KEY, getResources().getBoolean(R.bool.defaultPrintContactlessReceipts));
        editor.putBoolean(PRINT_TO_SCREEN_PREF_KEY, getResources().getBoolean(R.bool.defaultPrintToScreen));
        editor.putBoolean(AUTO_LOGIN_PREF_KEY, getResources().getBoolean(R.bool.defaultAutologin));
        editor.putBoolean(AUTO_LOGOFF_PREF_KEY, getResources().getBoolean(R.bool.defaultAutoLogoff));
        editor.putInt(AUTO_LOGOFF_TIMEOUT_PREF_KEY, getResources().getInteger(R.integer.defaultAutoLogoffTimeout));
        editor.putBoolean(DATE_MENU_PANEL_PREF_KEY, getResources().getBoolean(R.bool.defaultDateMenuPanel));
        editor.putBoolean(DISPLAY_DEV_MENU_PREF_KEY, getResources().getBoolean(R.bool.defaultDisplayDevMenu));
        editor.putString(USB_MODE_PREF_KEY, getResources().getString(R.string.defaultUsbMode));
        editor.commit();
    }
}
