package com.linkly.payment.fragments;

import static android.widget.Toast.LENGTH_LONG;
import static com.linkly.libmal.global.platform.Platform.TerminalModel.A30;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.linkly.libengine.engine.reporting.ShiftTotalsReport;
import com.linkly.libmal.MalFactory;
import com.linkly.libmal.global.platform.EFTPlatform;
import com.linkly.libui.IUIDisplay;
import com.linkly.payment.R;
import com.linkly.payment.activities.AppMain;
import com.linkly.payment.utilities.ConfirmDialogFragment;

import java.lang.ref.WeakReference;
import java.util.Locale;

import timber.log.Timber;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FragTotalsSettings#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FragTotalsSettings extends PreferenceFragmentCompat implements Preference.OnPreferenceChangeListener, Preference.OnPreferenceClickListener {

    private static final String SHIFT_TOTALS_ENABLED_PREF_KEY = "shiftTotalsEnabled";
    private static final String SETTLEMENT_ENABLED_PREF_KEY = "settlementEnabled";
    private static final String AUTOMATIC_SHIFT_TOTALS_ENABLED_PREF_KEY = "automaticShiftTotalsEnabled";
    private static final String AUTOMATIC_SHIFT_TOTALS_TIME_OF_DAY_PREF_KEY = "automaticShiftTotalsTimeOfDay";
    private static final String TOTALS_RESET_TO_DEFAULT_PREF_KEY = "totalsResetToDefault";

    private static final String TIME_OF_DAY_INITIAL_VALUE = "HH:MM";

    private boolean shiftTotalsParametersUpdated = false;

    public FragTotalsSettings() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment FragSettings.
     */
    public static FragTotalsSettings newInstance() {
        return new FragTotalsSettings();
    }


    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences_totals, rootKey);

        Preference shiftTotalsEnabled = findPreference(SHIFT_TOTALS_ENABLED_PREF_KEY);
        if (shiftTotalsEnabled != null) {
            shiftTotalsEnabled.setOnPreferenceChangeListener(this);
        }

        Preference automaticShiftTotalsEnabled = findPreference(AUTOMATIC_SHIFT_TOTALS_ENABLED_PREF_KEY);
        if (automaticShiftTotalsEnabled != null) {
            automaticShiftTotalsEnabled.setOnPreferenceChangeListener(this);
        }

        EditTextPreference editTextPreference = findPreference(AUTOMATIC_SHIFT_TOTALS_TIME_OF_DAY_PREF_KEY);
        if (editTextPreference != null) {

            editTextPreference.setOnPreferenceChangeListener(this);

            editTextPreference.setOnBindEditTextListener(
                    editText -> {

                        editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                        editText.setText("");
                        editText.setHint(TIME_OF_DAY_INITIAL_VALUE);
                        editText.addTextChangedListener( new HourMinuteTextWatcher(editText) );
                    });
        }

        Preference resetToDefaultPreference = findPreference(TOTALS_RESET_TO_DEFAULT_PREF_KEY);
        if (resetToDefaultPreference != null) {
            resetToDefaultPreference.setOnPreferenceClickListener(this);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (shiftTotalsParametersUpdated) {
            shiftTotalsParametersUpdated = false;

            ShiftTotalsReport shiftTotalsReport = new ShiftTotalsReport(requireContext());
            shiftTotalsReport.onParametersUpdate();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        AppMain.addWatcher( this );
    }

    @Override
    public boolean onPreferenceChange(@NonNull Preference preference, Object newValue ) {

        switch (preference.getKey()) {
            case AUTOMATIC_SHIFT_TOTALS_TIME_OF_DAY_PREF_KEY :
                // expecting five characters with four digits (as "12:45")
                if (getDigitsFromString(newValue.toString()).length() == TIME_OF_DAY_INITIAL_VALUE.length()-1) {
                    shiftTotalsParametersUpdated = true;
                }
                else {
                    // Parameter update not accepted
                    return false;
                }
                break;

            case SHIFT_TOTALS_ENABLED_PREF_KEY :
            case AUTOMATIC_SHIFT_TOTALS_ENABLED_PREF_KEY :
            case TOTALS_RESET_TO_DEFAULT_PREF_KEY :
                shiftTotalsParametersUpdated = true;
                break;
            default:
                break;
        }
        return true;
    }

    @Override
    public boolean onPreferenceClick(@NonNull Preference preference) {
        if (TOTALS_RESET_TO_DEFAULT_PREF_KEY.equals(preference.getKey())) {
            ConfirmDialogFragment.newInstance(IUIDisplay.String_id.STR_CONFIRM_RESET_TOTALS_SETTINGS.getId(), new ConfirmDialogFragment.ConfirmDialogListener() {
                @Override
                public void onDialogPositiveClick() {
                    resetTotalsSettings();
                    Toast.makeText(requireActivity().getApplicationContext(), IUIDisplay.String_id.STR_TOTALS_SETTINGS_RESET.getId(), LENGTH_LONG).show();
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
    private void resetTotalsSettings() {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(requireContext()).edit();
        editor.putBoolean(SHIFT_TOTALS_ENABLED_PREF_KEY, getResources().getBoolean(R.bool.defaultShiftTotalsEnabled));
        editor.putBoolean(SETTLEMENT_ENABLED_PREF_KEY, getResources().getBoolean(R.bool.defaultSettlementEnabled));
        editor.putBoolean(AUTOMATIC_SHIFT_TOTALS_ENABLED_PREF_KEY, getResources().getBoolean(R.bool.defaultAutomaticShiftTotalsEnabled));
        editor.putString(AUTOMATIC_SHIFT_TOTALS_TIME_OF_DAY_PREF_KEY, getResources().getString(R.string.defaultAutomaticShiftTotalsTimeOfDay));
        editor.commit();
        shiftTotalsParametersUpdated = true;
    }

    private static class HourMinuteTextWatcher implements TextWatcher {
        private static final String HHMM = "HHMM";

        private final WeakReference<EditText> mEditText;
        private String mCurrent = "";

        HourMinuteTextWatcher(EditText editText) {
            mEditText = new WeakReference<>(editText);
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            // This method is intentionally empty
        }

        @SuppressWarnings("java:S3776")
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            String enteredText = s.toString();
            if (!enteredText.equals(mCurrent)) {
                StringBuilder text = new StringBuilder(getDigitsFromString(enteredText));

                //Correctly handle deletion
                if ((text.length() > 0) && (getDigitsFromString(mCurrent).length() != 4) && (enteredText.length() < mCurrent.length())) {
                    text.deleteCharAt(text.length() - 1);
                }

                if (text.length() < HHMM.length()) {
                    text.append(HHMM.substring(text.length()));
                } else {
                    int minute = Integer.parseInt(text.substring(2,4));
                    int hour = Integer.parseInt(text.substring(0,2));
                    //Check HHMM is within bounds
                    if (hour > 23) {
                        hour = 23;
                    }
                    if (minute > 59) {
                        minute = 59;
                    }
                    String checkText = String.format(Locale.getDefault(), "%02d%02d", hour, minute);
                    if (!checkText.equals(text.toString())) {
                        text.replace(0, text.length(), checkText);
                    }
                }
                text.insert(2, ':');

                mCurrent = text.toString();
                mEditText.get().setText(mCurrent);
                mEditText.get().setSelection(mCurrent.length());
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
            // This method is intentionally empty
        }
    }

    static String getDigitsFromString(@NonNull String formattedString) {
        return formattedString.replaceAll("\\D", "");
    }

}
