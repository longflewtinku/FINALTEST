package com.linkly.payment.fragments;

import static android.view.View.GONE;
import static com.linkly.libui.IUIDisplay.ACTIVITY_ID.ACT_INPUT;
import static com.linkly.libui.IUIDisplay.SCREEN_ID.GET_INPUT_ALPHA;
import static com.linkly.libui.IUIDisplay.SCREEN_ID.GET_INPUT_ALPHA_NUMERIC;
import static com.linkly.libui.IUIDisplay.SCREEN_ID.GET_INPUT_ALPHA_NUMERIC_CAPS;
import static com.linkly.libui.IUIDisplay.SCREEN_ID.GET_INPUT_EMAIL;
import static com.linkly.libui.IUIDisplay.SCREEN_ID.GET_INPUT_NUMBER_PASSWORD;
import static com.linkly.libui.keyboard.CustomKeyboard.KBTypes.AMOUNT_KB;
import static com.linkly.libui.keyboard.CustomKeyboard.KBTypes.NUM_ALPHA;
import static com.linkly.libui.keyboard.CustomKeyboard.KBTypes.NUM_ALPHA_NO_SYM;
import static com.linkly.libui.keyboard.CustomKeyboard.KBTypes.PHONE_KB;
import static com.linkly.libui.keyboard.CustomKeyboard.KBTypes.TEXT_KB;
import static com.linkly.libui.keyboard.CustomKeyboard.KBTypes.TEXT_KB_CAPS;
import static com.linkly.libui.keyboard.CustomKeyboard.KBTypes.TEXT_KB_CAPS_NO_SYM;
import static com.linkly.libui.keyboard.CustomKeyboard.KBTypes.USR_PASS_KB;

import android.content.res.Configuration;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;
import com.linkly.kotlinandroidtoolkit.extensionfuncs.BundleExtensionsKt;
import com.linkly.libengine.engine.Engine;
import com.linkly.libmal.global.platform.EFTPlatform;
import com.linkly.libmal.global.util.Util;
import com.linkly.libui.IUIDisplay;
import com.linkly.libui.IUIDisplay.String_id;
import com.linkly.libui.IUIKeyboard;
import com.linkly.libui.display.DisplayRequest;
import com.linkly.libui.views.CustomEditText;
import com.linkly.payment.R;
import com.linkly.payment.activities.ActScreenSaver;
import com.linkly.payment.databinding.ActivityTransBinding;
import com.linkly.payment.viewmodel.FragKeyboardViewModel;

import java.util.regex.Pattern;

public class FragInput extends BaseFragment<ActivityTransBinding, FragKeyboardViewModel>
        implements IUIKeyboard.OnDoneClickedListener, IUIKeyboard.OnCancelClickedListener,
        View.OnClickListener{

    public static final String TAG = FragInput.class.getSimpleName();
    private FragKeyboardViewModel fragKeyboardViewModel;
    private int maxLen;
    private int minLen;
    private IUIDisplay.SCREEN_ID inputType;
    private TextInputLayout uiCustomInputLayout;

    public static FragInput newInstance() {
        Bundle args = new Bundle();
        FragInput fragment = new FragInput();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public int getBindingVariable() {
        return com.linkly.payment.BR.fragKeyboardViewModel;
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_keyboard;
    }

    @Override
    public FragKeyboardViewModel getViewModel() {
        fragKeyboardViewModel = ViewModelProviders.of(this).get(FragKeyboardViewModel.class);
        fragKeyboardViewModel.init(ACT_INPUT);
        return fragKeyboardViewModel;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = super.onCreateView(inflater, container, savedInstanceState);

        ViewStub pageContent = v.findViewById(R.id.ui2_layout_stub);
        pageContent.setLayoutResource(R.layout.fragment_input);
        pageContent.inflate();

        uiCustomInputLayout = v.findViewById(R.id.text_input_custom_layout);
        CustomEditText editText = v.findViewById(R.id.txtInput);

        editText.requestFocus();
        editText.setFocusableInTouchMode(true);

        // hide the back button for input flows, eg refund password
        FloatingActionButton fab = v.findViewById(R.id.fab);
        fab.setVisibility(GONE);

        showScreen(v, fragKeyboardViewModel.getDisplay().getValue());
        if( getActivity() != null && Configuration.ORIENTATION_LANDSCAPE == getActivity().getResources().getConfiguration().orientation ) {
            SetHeader( false, false, false );
        } else {
            SetHeader( false, false );
        }

        if (!EFTPlatform.hasPhysicalKeyboard()) {
            editText.setOnKeyListener(this);
        } else {
            editText.setOnKeyListener( ( view, i, keyEvent ) -> {
                // MW: Need this for landscape mode for some reason I don't fully comprehend.
                // keeping it in until I can find the source of the bug
                return false;
            } );
        }

        ActScreenSaver.cancelScreenSaver();

        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mCustomKeyboard.showCustomKeyboard(view, view.findViewById(R.id.txtInput));

    }
    @Override
    public void onResume() {
        super.onResume();

        if (EFTPlatform.hasPhysicalKeyboard()) {
            RelativeLayout kb = (RelativeLayout) getView().findViewById(R.id.keyRelative);
            if (kb != null) {
                //If platform has Physical KB, only enable the soft kb if the input has alpha
                if(inputType == GET_INPUT_ALPHA || inputType == GET_INPUT_ALPHA_NUMERIC || inputType == GET_INPUT_EMAIL || inputType == GET_INPUT_ALPHA_NUMERIC_CAPS) {
                    kb.setVisibility(View.VISIBLE);
                }

            }
        }
    }

    @Override
    public void onDoneClicked() {
        //Done Handler
        if (isInputValid()) {
            CustomEditText editText = (CustomEditText) getView().findViewById(R.id.txtInput);
            sendResponse(IUIDisplay.UIResultCode.OK, editText.getText().toString(), "");
        }
    }

    @Override
    public void onCancelClicked() {
        abortFragment("Cancel clicked");
    }



    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.skip_button:
                // send empty text response
                sendResponse( IUIDisplay.UIResultCode.OK, "", "");
                break;
        }
    }

    @Override
    public void onDestroyView() {
        fragKeyboardViewModel = null;
        CustomEditText editText = getView().findViewById(R.id.txtInput);
        editText.setOnKeyListener(null);
        uiCustomInputLayout = null;
        super.onDestroyView();
    }

    @SuppressWarnings("deprecation")
    public void showScreen(View v, DisplayRequest message) {
        TextView txtTitle = v.findViewById(R.id.header_title);
        TextView txtPrompt = v.findViewById(R.id.prompt_text);
        Button skipbutton = v.findViewById(R.id.skip_button);
        CustomEditText input = v.findViewById(R.id.txtInput);

        // set visibility of skip button and text
        skipbutton.setText(Engine.getDep().getPrompt(IUIDisplay.String_id.STR_SKIP));
        if (fragKeyboardViewModel.getDisplay().getValue().getUiExtras().getBoolean(IUIDisplay.uiSkipButtonOn)) {
            skipbutton.setVisibility(View.VISIBLE);
            skipbutton.setOnClickListener(this);
        } else {
            skipbutton.setVisibility(View.INVISIBLE);
        }

        //Get Max and Min values
        maxLen = fragKeyboardViewModel.getDisplay().getValue().getUiExtras().getInt(IUIDisplay.uiScreenMaxLen, 100);
        minLen = fragKeyboardViewModel.getDisplay().getValue().getUiExtras().getInt(IUIDisplay.uiScreenMinLen, 0);

        //Get validation values
        String customValidations = fragKeyboardViewModel.getDisplay().getValue().getUiExtras().getString(IUIDisplay.uiError);

        //Set Input Type
        inputType = BundleExtensionsKt.getSerializableCompat(
                fragKeyboardViewModel.getDisplay().getValue().getUiExtras(),
                IUIDisplay.uiScreenID,
                IUIDisplay.SCREEN_ID.class);
        if (inputType != null) {
            switch (inputType) {
                case GET_INPUT_ALPHA_NUMERIC_CAPS:
                    input.setInputType(InputType.TYPE_CLASS_TEXT);
                    SetEditorActions(input, this, TEXT_KB_CAPS);
                    break;

                case GET_INPUT_ALPHA_NUMERIC_CAPS_NO_SYM:
                    input.setInputType(InputType.TYPE_CLASS_TEXT);
                    SetEditorActions(input, this, TEXT_KB_CAPS_NO_SYM);
                    break;

                case GET_INPUT_NUMERIC_ALPHA_NO_SYM:
                    input.setInputType(InputType.TYPE_CLASS_TEXT);
                    SetEditorActions(input, this, NUM_ALPHA_NO_SYM);
                    break;

                case GET_INPUT_NUMERIC_ALPHA:
                    input.setInputType(InputType.TYPE_CLASS_TEXT);
                    SetEditorActions(input, this, NUM_ALPHA);
                    break;
                case GET_INPUT_PASSWORD:
                    input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    SetEditorActions(input, this, USR_PASS_KB);
                    break;
                case GET_INPUT_NUMBER_PASSWORD:
                    input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
                    SetEditorActions(input, this, AMOUNT_KB);
                    showValidations(uiCustomInputLayout,customValidations);
                    break;
                case GET_INPUT_PHONE_NO:
                    input.setInputType(InputType.TYPE_CLASS_PHONE);
                    SetEditorActions(input, this, PHONE_KB);
                    break;
                case GET_INPUT_NUMBER:
                    input.setInputType(InputType.TYPE_CLASS_NUMBER);
                    SetEditorActions(input, this, AMOUNT_KB);
                    showValidations(uiCustomInputLayout,customValidations);
                    break;
                case GET_INPUT_ALPHA_NUMERIC:
                    input.setInputType(InputType.TYPE_CLASS_TEXT);
                    SetEditorActions(input, this, TEXT_KB);
                    showValidations(uiCustomInputLayout,customValidations);
                    break;
                case GET_INPUT_ALPHA:
                case GET_INPUT_EMAIL:
                default:
                    input.setInputType(InputType.TYPE_CLASS_TEXT);
                    SetEditorActions(input, this, TEXT_KB);
                    break;
            }
        } else {
            inputType = GET_INPUT_ALPHA;
            SetEditorActions(input, this, TEXT_KB);
        }

        //Set Input Fields Max Length
        InputFilter[] filterArray = new InputFilter[1];
        filterArray[0] = new InputFilter.LengthFilter(maxLen);
        input.setFilters(filterArray);


        //Set Input Placeholder Text
        String hintText = fragKeyboardViewModel.getHint().getValue();
        if (hintText != null && hintText.length() > 0) {
            input.setHint(hintText);
        } else {
            input.setHint("");
        }

        //Set The Title Text
        String title = fragKeyboardViewModel.getTitle().getValue();

        if (title != null && title.length() > 0) {
            txtTitle.setText(title);
        } else {
            txtTitle.setVisibility(TextView.INVISIBLE);
            title = " No Title ";
        }

        //Set The Prompt Text
        String prompt = fragKeyboardViewModel.getPrompt().getValue();

        if (prompt != null && prompt.length() > 0) {
            txtPrompt.setText(prompt);
        } else {
            prompt = " No Prompt ";
            txtPrompt.setVisibility(TextView.INVISIBLE);
        }

    }

    private void showValidations(TextInputLayout input, String error) {
        if(error != null)  {
            Util.setInputLayoutError(input,error);
        }
    }

    public boolean basicEmailValidation(String email)
    {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\."+
                "[a-zA-Z0-9_+&*-]+)*@" +
                "(?:[a-zA-Z0-9-]+\\.)+[a-z" +
                "A-Z]{2,7}$";

        Pattern pat = Pattern.compile(emailRegex);
        if (email == null)
            return false;
        return pat.matcher(email).matches();
    }

    private boolean basicValidation(String text) {
        boolean isValid = false;

        if ((text.length() >= minLen) && (text.length() <= maxLen)) {
            isValid = true;
        }

        return isValid;
    }

    private boolean numberValidation(String text) {
        boolean isValid = true;

        if ((text.length() < minLen) || (text.length() > maxLen)) {
            isValid = false;
        }

        try {
            double d = Double.parseDouble(text);
        } catch (NumberFormatException nfe) {
            isValid = false;
        }

        return isValid;
    }


    private boolean isInputValid() {
        boolean isValid = true;
        CustomEditText input = getView().findViewById(R.id.txtInput);
        switch (inputType) {
            case GET_INPUT_ALPHA:
                isValid = basicValidation(input.getText().toString());
                break;
            case GET_INPUT_EMAIL:
                isValid = basicEmailValidation(input.getText().toString());
                break;
            case GET_INPUT_ALPHA_NUMERIC:
            case GET_INPUT_ALPHA_NUMERIC_CAPS:
                isValid = basicValidation(input.getText().toString());
                break;
            case GET_INPUT_PASSWORD:
                isValid = basicValidation(input.getText().toString());
                break;
            case GET_INPUT_PHONE_NO:
                isValid = basicValidation(input.getText().toString());
                break;
            case GET_INPUT_NUMBER:
                isValid = numberValidation(input.getText().toString());
                break;
            default:
                isValid = basicValidation(input.getText().toString());
                break;
        }

        if (!isValid) {
            if (inputType == GET_INPUT_EMAIL) {
                Util.setInputLayoutError(uiCustomInputLayout,Engine.getDep().getPrompt(String_id.STR_ERROR_EMAIL_FIELD_REQUIRED));
            } else if (inputType == GET_INPUT_NUMBER_PASSWORD) {
                Util.setInputLayoutError(uiCustomInputLayout, Engine.getDep().getPrompt(String_id.STR_ERROR_PASSCODE_BTW_6_12));
            } else {
                Util.setInputLayoutError(uiCustomInputLayout, Engine.getDep().getPrompt(String_id.STR_ERROR_FIELD_REQUIRED));
            }
        }

        return isValid;
    }

}



