package com.linkly.launcher.fragments;

import static com.linkly.launcher.ServiceFrontEnd.ACCESSCODE_UNATTENDED_ESCAPEE;
import static com.linkly.libui.keyboard.CustomKeyboard.KBTypes.AMOUNT_KB;
import static com.linkly.libui.keyboard.CustomKeyboard.KBTypes.NUM_ALPHA;
import static com.linkly.libui.keyboard.CustomKeyboard.KBTypes.TEXT_KB;
import static com.linkly.libui.keyboard.CustomKeyboard.KBTypes.TEXT_KB_CAPS;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.linkly.kotlinandroidtoolkit.extensionfuncs.BundleExtensionsKt;
import com.linkly.launcher.AuthHost;
import com.linkly.launcher.BrandingConfig;
import com.linkly.launcher.R;
import com.linkly.launcher.viewmodels.InputFragmentViewModel;
import com.linkly.libmal.MalFactory;
import com.linkly.libui.IUIDisplay;
import com.linkly.libui.IUIKeyboard;
import com.linkly.libui.UI;
import com.linkly.libui.keyboard.CustomKeyboard;
import com.linkly.libui.views.CustomEditText;

import java.io.File;

import timber.log.Timber;

/**
For each use case, create a new static method to build the Fragment so that all such setups are
 contained here. Also add a new USECASE_YOURNEWUSECASENAME flag if any special needs are to be
 facilitated for the use case.

 Submissions are routed to the parent Activity which must implement AuthHost interface. This still
 uses an Intent to pass data but is not doing so across components.

 For the USECASE_ESCAPE_LOCKDOWN:
 - A timeout of 90s sees the CANCEL mechanism invoked, which _should_ cause the parent Activity to
 finish and relinquish back to the previous Activity (LockedDownActivity).

 For the USECASE_NORMAL:
 - No timeout is applied.
 */
public class InputFragment extends Fragment implements IUIKeyboard.OnDoneClickedListener, IUIKeyboard.OnCancelClickedListener {
    private static final String KEY_USECASE = "UseCase";
    public static final int USECASE_NORMAL = 1;
    public static final int USECASE_ESCAPE_LOCKDOWN = 100;
    private InputFragmentViewModel viewModel;
    private static int maxLen;
    private static int minLen;
    private IUIDisplay.SCREEN_ID inputType;
    private CustomKeyboard mCustomKeyboard;
    private Bitmap brandingHeaderBitmap = null;
    private CustomEditText editText;

    public static InputFragment newInstance(Bundle args) {
        InputFragment instance = new InputFragment();
        instance.setArguments(args);
        return instance;
    }

    public static InputFragment forAuth(int useCase) {
        Bundle inputFragArgs = new Bundle();
        inputFragArgs.putInt(KEY_USECASE, useCase);
        inputFragArgs.putInt(IUIDisplay.uiScreenMaxLen, 9);
        inputFragArgs.putInt(IUIDisplay.uiScreenMinLen, 0);
        inputFragArgs.putSerializable(IUIDisplay.uiScreenID, IUIDisplay.SCREEN_ID.GET_INPUT_NUMBER_PASSWORD);
        inputFragArgs.putString(IUIDisplay.uiScreenInputHint, UI.getInstance().getPrompt(IUIDisplay.String_id.STR_ENTER_PASSWORD));
        inputFragArgs.putString(IUIDisplay.uiScreenTitle, UI.getInstance().getPrompt(IUIDisplay.String_id.STR_ADMIN_PASSWORD));
        inputFragArgs.putString(IUIDisplay.uiScreenPrompt, UI.getInstance().getPrompt(IUIDisplay.String_id.STR_PLEASE_ENTER));
        return newInstance(inputFragArgs);
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_keyboard, container, false);
        viewModel = new ViewModelProvider(this).get(InputFragmentViewModel.class);
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Integer useCase = getArguments().getInt(KEY_USECASE, USECASE_NORMAL);

        if (useCase == USECASE_ESCAPE_LOCKDOWN) {
            // Setup Timer to automatically cancel in order to move back to LockedDownActivity.
            viewModel.onUnattendedAutoCancelTimeout().observe(getViewLifecycleOwner(), shouldAutoCancel -> {
                Timber.d("onChange[onUnattendedAutoCancelTimeout]...shouldAutoCancel: %b", shouldAutoCancel);
                if (shouldAutoCancel != null && shouldAutoCancel) {
                    ((AuthHost) getActivity()).onAuthCancellation();
                }
            });
            viewModel.startUnattendedAutoCancelTimeout();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getActivity().getWindow().setStatusBarColor(
                    BrandingConfig.getBrandDisplayStatusBarColourOrDefault(
                            ContextCompat.getColor(getContext(), R.color.color_linkly_primary)));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Timber.d("onResume...");

        View view = getView();

        ViewStub stub = (ViewStub) view.findViewById(R.id.layout_stub);
        stub.setLayoutResource(R.layout.content_input);
        View v = stub.inflate();

        editText = (CustomEditText) view.findViewById(R.id.txtInput);

        if (mCustomKeyboard == null) {
            mCustomKeyboard = new CustomKeyboard(getActivity(), true);
        }
        mCustomKeyboard.registerEditText(editText, this, null, NUM_ALPHA);

        if (editText != null) {
            editText.requestFocus();
            editText.setFocusableInTouchMode(true);
        }

        try {
            // "/data/data/" + pth + "/files"
            String basePath = MalFactory.getInstance().getFile().getCommonDir();
            File imgFile = new File(basePath, BrandingConfig.getBrandDisplayLogoHeaderOrDefault());
            if (imgFile.exists()) {
                brandingHeaderBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            }

            if (brandingHeaderBitmap != null) {
                ImageView headerLogo = (ImageView) view.findViewById(R.id.header);
                headerLogo.setImageBitmap(brandingHeaderBitmap);
            }
        } catch (Exception ex) {
            Timber.w(ex);
        }

        showScreen(view);
        mCustomKeyboard.showCustomKeyboard(v, editText);

        if (editText != null) {
            editText.requestFocus();
            editText.setEnabled(true);
            editText.setSelection(editText.getText().length());
        }

    }

    @Override
    public void onDoneClicked() {
        //Done Handler
        Timber.d("onDoneClicked...");

        if (isInputValid()) {
            Intent data = new Intent();
            data.putExtra("resultText", editText.getText().toString());
            ((AuthHost) getActivity()).onAuthSubmission(data, ACCESSCODE_UNATTENDED_ESCAPEE);
            // AuthHost is expected to manage Fragments accordingly at this point.
        }

    }

    @Override
    public void onCancelClicked() {
        //Cancel Handler
        ((AuthHost) getActivity()).onAuthCancellation();
        // AuthHost is expected to manage Fragments accordingly at this point.
    }

    public void SetEditorActions(
            CustomEditText editTextView,
            final IUIKeyboard.OnDoneClickedListener doneListener,
            final IUIKeyboard.OnCancelClickedListener cancelListener,
            CustomKeyboard.KBTypes kbType
    ) {

        if (mCustomKeyboard == null) {
            mCustomKeyboard = new CustomKeyboard(getActivity(), false);
        }

        mCustomKeyboard.registerEditText(editTextView, doneListener, cancelListener, kbType);

    }
    @SuppressWarnings("deprecation")
    public void showScreen(View view) {

        Bundle b = getArguments();

        TextView txtTitle = (TextView) view.findViewById(R.id.header_title);
        TextView txtPrompt = (TextView) view.findViewById(R.id.prompt_text);

        CustomEditText input = (CustomEditText) view.findViewById(R.id.txtInput);

        //Get Max and Min values
        maxLen = b.getInt(IUIDisplay.uiScreenMaxLen, 100);
        minLen = b.getInt(IUIDisplay.uiScreenMinLen, 0);

        //Set Input Type
        inputType = BundleExtensionsKt.getSerializableCompat(b, IUIDisplay.uiScreenID, IUIDisplay.SCREEN_ID.class);
        if (inputType != null) {
            switch (inputType) {
                case GET_INPUT_ALPHA_NUMERIC_CAPS:
                    input.setInputType(InputType.TYPE_CLASS_TEXT);
                    SetEditorActions(input, this, this, TEXT_KB_CAPS);
                    break;
                case GET_INPUT_PASSWORD:
                    input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    SetEditorActions(input, this, this, AMOUNT_KB);
                    break;
                case GET_INPUT_NUMBER_PASSWORD:
                    input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
                    SetEditorActions(input, this, this, AMOUNT_KB);
                    break;
                case GET_INPUT_PHONE_NO:
                    input.setInputType(InputType.TYPE_CLASS_PHONE);
                    SetEditorActions(input, this, this, AMOUNT_KB);
                    break;
                case GET_INPUT_NUMBER:
                    input.setInputType(InputType.TYPE_CLASS_NUMBER);
                    SetEditorActions(input, this, this, AMOUNT_KB);
                    break;
                case GET_INPUT_ALPHA:
                case GET_INPUT_ALPHA_NUMERIC:
                default:
                    input.setInputType(InputType.TYPE_CLASS_TEXT);
                    SetEditorActions(input, this, this, TEXT_KB);
                    break;
            }
        } else {
            inputType = IUIDisplay.SCREEN_ID.GET_INPUT_ALPHA;
            SetEditorActions(input, this, this, TEXT_KB);
        }

        //Set Input Fields Max Length
        InputFilter[] filterArray = new InputFilter[1];
        filterArray[0] = new InputFilter.LengthFilter(maxLen);
        input.setFilters(filterArray);


        //Set Input Placeholder Text
        String hintText = b.getString(IUIDisplay.uiScreenInputHint);
        if (hintText != null && hintText.length() > 0) {
            input.setHint(hintText);
        } else {
            input.setHint("");
        }

        //Set The Title Text
        String title = b.getString(IUIDisplay.uiScreenTitle);

        if (title != null && title.length() > 0) {
            txtTitle.setText(title);
        } else {
            txtTitle.setVisibility(View.INVISIBLE);
        }

        //Set The Prompt Text
        String prompt = b.getString(IUIDisplay.uiScreenPrompt);

        if (prompt != null && prompt.length() > 0) {
            txtPrompt.setText(prompt);
        } else {
            txtPrompt.setVisibility(View.INVISIBLE);
        }


        //Default Text Value
        String defaultTxt = b.getString(IUIDisplay.uiScreenDefaultText);

        if (defaultTxt != null && defaultTxt.length() > 0) {
            input.setText(defaultTxt);
        }

    }


    private boolean basicValidation(String text) {
        boolean isValid = false;

        if ((text.length() >= minLen) || (text.length() <= maxLen)) {
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
            Double.parseDouble(text);
        } catch (NumberFormatException nfe) {
            isValid = false;
        }

        return isValid;
    }

    private boolean isInputValid() {
        boolean isValid = true;
        switch (inputType) {
            case GET_INPUT_ALPHA:
                isValid = basicValidation(editText.getText().toString());
                break;
            case GET_INPUT_ALPHA_NUMERIC:
                isValid = basicValidation(editText.getText().toString());
                break;
            case GET_INPUT_PASSWORD:
                isValid = basicValidation(editText.getText().toString());
                break;
            case GET_INPUT_PHONE_NO:
                isValid = basicValidation(editText.getText().toString());
                break;
            case GET_INPUT_NUMBER:
                isValid = numberValidation(editText.getText().toString());
                break;
            default:
                isValid = basicValidation(editText.getText().toString());
                break;
        }

        if (!isValid) {
            editText.setError("Invalid Input");
        }


        return isValid;
    }

}
