package com.linkly.payment.fragments;

import static com.linkly.libui.keyboard.CustomKeyboard.KBTypes.AMOUNT_KB;

import android.inputmethodservice.Keyboard;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;

import com.google.android.material.textfield.TextInputLayout;
import com.linkly.libengine.engine.Engine;
import com.linkly.libmal.global.platform.EFTPlatform;
import com.linkly.libmal.global.util.DisplayKiosk;
import com.linkly.libmal.global.util.Util;
import com.linkly.libui.IUICurrency;
import com.linkly.libui.IUIDisplay;
import com.linkly.libui.IUIKeyboard;
import com.linkly.libui.display.DisplayRequest;
import com.linkly.libui.views.CustomEditText;
import com.linkly.payment.R;
import com.linkly.payment.activities.ActScreenSaver;
import com.linkly.payment.databinding.ActivityTransBinding;
import com.linkly.payment.utilities.UIUtilities;
import com.linkly.payment.viewmodel.FragKeyboardViewModel;
import com.linkly.payment.viewmodel.data.UIFragData;

import java.lang.ref.WeakReference;

import timber.log.Timber;

// Suppressing deprecation warning for using Keyboard.
@SuppressWarnings("java:S1874")
public abstract class BaseInputAmount extends BaseFragment<ActivityTransBinding, FragKeyboardViewModel> implements IUIKeyboard.OnDoneClickedListener, IUIKeyboard.OnCancelClickedListener {

    protected static final String TAG = BaseInputAmount.class.getSimpleName();
    protected static int minLen;
    protected static int maxLen;
    protected float fontSize;
    protected TextInputLayout enterAmountLayout;
    protected CustomEditText editText;

    public static int getMinLen() {
        return BaseInputAmount.minLen;
    }

    public static int getMaxLen() {
        return BaseInputAmount.maxLen;
    }

    public static void setMinLen(int minLen) {
        BaseInputAmount.minLen = minLen;
    }

    public static void setMaxLen(int maxLen) {
        BaseInputAmount.maxLen = maxLen;
    }

    @Override
    public abstract int getBindingVariable();

    @Override
    public int getLayoutId() {
        return R.layout.fragment_keyboard;
    }

    @Override
    public abstract FragKeyboardViewModel getViewModel();

    @SuppressWarnings("deprecation")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);
        ViewStub pageContent = v.findViewById(R.id.ui2_layout_stub);
        pageContent.setLayoutResource(R.layout.fragment_input_amount);
        pageContent.inflate();

        enterAmountLayout = v.findViewById(R.id.text_input_amount_layout);
        editText = v.findViewById(R.id.txtAmount);
        editText.setHint(Engine.getDep().getPrompt(IUIDisplay.String_id.STR_AMOUNT));
        fontSize = editText.getTextSize();

        setupFloatingActionButton(v);

        int flags = WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON;
        getBaseActivity().getWindow().addFlags(flags);

        showScreen(v);

        //We want to use the POS Keyboard handler logic
        SetEditorActions(editText, this, AMOUNT_KB);
        SetHeader(false, false);
        if (!EFTPlatform.hasPhysicalKeyboard()) {
            editText.setOnKeyListener(this);
        } else {
            // MW: Need this for landscape mode for some reason I don't fully comprehend.
            // keeping it in until I can find the source of the bug
            editText.setOnKeyListener((view, i, keyEvent) -> false);
        }

        updateScreenSaver();

        return v;
    }

    DisplayKiosk.NavigationBarState state;

    @Override
    public void onResume() {
        super.onResume();

        if (EFTPlatform.hasPhysicalKeyboard()) {
            View view = getView();
            if (view != null) {
                RelativeLayout kb = (RelativeLayout) view.findViewById(R.id.keyRelative);
                if (kb != null) {
                    // disable the soft keyboard for amount inputs
                    kb.setVisibility(View.GONE);
                }
            }
        }
        state = new DisplayKiosk.NavigationBarState();
        DisplayKiosk.getInstance().onResume(false);
    }

    @Override
    public void onPause() {
        super.onPause();
        DisplayKiosk.getInstance().setNavigationBarAndButtonsState(state, true);
    }

    protected abstract void showScreen(View v);

    protected abstract void updateScreenSaver();

    protected abstract void setupFloatingActionButton(View v);

    @SuppressWarnings("deprecation")
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Timber.d("onViewCreated...");

        mCustomKeyboard.showCustomKeyboard(view, view.findViewById(R.id.txtAmount));

        CustomEditText e = view.findViewById(R.id.txtAmount);

        if (e != null) {
            e.setFocusable(true);
            e.setText(Engine.getDep().getFramework().getCurrency().formatUIAmount("0", IUICurrency.EAmountFormat.FMT_AMT_SHOW_SYMBOL, Engine.getDep().getPayCfg().getCountryCode(), true));
            e.setSelection(e.getText().toString().length());
            e.setOnKeyListener((v1, keyCode, event) -> {
                Timber.d("BaseInputAmount OnKeyListener RUNNING!");
                Timber.i("KeyCode = %s", keyCode);
                Timber.i("event = %s", event.toString());
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    ActScreenSaver.resetScreenSaver(requireContext().getApplicationContext());
                    if (event.getKeyCode() == Keyboard.KEYCODE_CANCEL) {
                        onCancelClicked();
                        return true;
                    }
                }
                return false;
            });
            e.addTextChangedListener(new CurrencyTextWatcher(e, fontSize));
        }

    }

    @VisibleForTesting
    public void onDisplayChanged(DisplayRequest display) {
        Timber.i("onDisplayChanged");
    }

    public void showScreen(View v, UIFragData fragData) {
        super.showScreen();

        TextView tvPrompt = v.findViewById(R.id.textView2);
        CustomEditText e = v.findViewById(R.id.txtAmount);

        setMinLen(fragData.getMinLen().getValue());

        //Setup  the maximum based on the max Long
        String mLen = "" + Long.MAX_VALUE;
        setMaxLen(mLen.length() - 4);

        if (tvPrompt != null && e != null) {
            String prompt = fragData.getPrompt().getValue();
            tvPrompt.setText(prompt);
            e.setFocusable(true);
            e.setText(Engine.getDep().getFramework().getCurrency().formatUIAmount("0", IUICurrency.EAmountFormat.FMT_AMT_SHOW_SYMBOL, Engine.getDep().getPayCfg().getCountryCode(), true));
            String txt = e.getText().toString();
            e.requestFocus();
            e.setSelection(txt.length());
            e.addTextChangedListener(new CurrencyTextWatcher(e, fontSize));
        }
    }

    protected boolean isInputValid() {
        boolean isValid = true;
        CustomEditText amountEditText  = getView().findViewById(R.id.txtAmount);
        String current = amountEditText .getText().toString();
        String input = current.replaceAll("\\D", "");

        try {
            // we do it this way round as it avoids us thinking 000 is a three digit string
            if (getMinLen() > 0) {
                double d2 = Double.parseDouble(input);

                String doubleAsString = Integer.toString((int) d2);
                if (doubleAsString.length() < getMinLen() || d2 == 0) {
                    isValid = false;
                }
            }
        } catch (NumberFormatException nfe) {
            isValid = false;
        }

        if (!isValid) {
            Util.setInputLayoutError(enterAmountLayout, Engine.getDep().getPrompt(IUIDisplay.String_id.STR_ERROR_FIELD_REQUIRED));
        }

        return isValid;
    }

    private class CurrencyTextWatcher implements TextWatcher {
        private boolean mEditing = false;
        private final WeakReference<CustomEditText> mEditText;
        private float originalFontSize;

        CurrencyTextWatcher(CustomEditText editText, float fontSize) {
            mEditText = new WeakReference<>(editText);
            originalFontSize = fontSize;
        }

        public synchronized void afterTextChanged(Editable s) {
            if (!mEditing) {
                mEditing = true;

                String digits = s.toString().replaceAll("\\D", "");

                //Limit Chars Entered to Protect Backend
                if (digits.length() > getMaxLen()) {
                    digits = digits.substring(0, getMaxLen());
                }

                try {
                    String formatted = Engine.getDep().getFramework().getCurrency().formatUIAmount(digits, IUICurrency.EAmountFormat.FMT_AMT_MIN, Engine.getDep().getPayCfg().getCountryCode(), true);

                    if (Util.isNullOrEmpty(Engine.getDep().getPayCfg().getCountryCode().getSymbol())) {
                        s.replace(0, s.length(), formatted);
                    } else {
                        s.replace(1, s.length(), formatted);
                    }

                } catch (NumberFormatException nfe) {
                    s.clear();
                }

                if (s.length() > 13) {
                    //Reduce the Font Size
                    mEditText.get().setTextSize(23.0f);
                } else {
                    //Set font size back to default
                    mEditText.get().setTextSize(originalFontSize / UIUtilities.getDisplayDensity(requireContext()));
                }

                mEditing = false;
            }
        }

        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            // no actions before text change
        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {
            // no actions during text change
        }
    }
}