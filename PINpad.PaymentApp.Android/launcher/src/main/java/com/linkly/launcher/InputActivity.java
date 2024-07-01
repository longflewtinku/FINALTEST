package com.linkly.launcher;

import static com.linkly.launcher.ServiceFrontEnd.ACCESSCODE_ADMIN_MENU;
import static com.linkly.launcher.ServiceFrontEnd.ACCESSCODE_EXIT;
import static com.linkly.launcher.access.AccessCodeHelpers.checkAccessCode;
import static com.linkly.libui.keyboard.CustomKeyboard.KBTypes.AMOUNT_KB;
import static com.linkly.libui.keyboard.CustomKeyboard.KBTypes.NUM_ALPHA;
import static com.linkly.libui.keyboard.CustomKeyboard.KBTypes.TEXT_KB;
import static com.linkly.libui.keyboard.CustomKeyboard.KBTypes.TEXT_KB_CAPS;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewStub;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;

import com.linkly.kotlinandroidtoolkit.extensionfuncs.BundleExtensionsKt;
import com.linkly.launcher.access.AccessCodeCheckCallbacks;
import com.linkly.libmal.MalFactory;
import com.linkly.libui.IUIDisplay;
import com.linkly.libui.IUIKeyboard;
import com.linkly.libui.UI;
import com.linkly.libui.keyboard.CustomKeyboard;
import com.linkly.libui.views.CustomEditText;

import java.io.File;

import timber.log.Timber;

public class InputActivity extends AppCompatActivity implements IUIKeyboard.OnDoneClickedListener, IUIKeyboard.OnCancelClickedListener {
    private static final String TAG = "InputActivity";
    private static int maxLen;
    private static int minLen;
    private IUIDisplay.SCREEN_ID inputType;
    private CustomKeyboard mCustomKeyboard;
    private Bitmap brandingHeaderBitmap = null;

    public static ActivityResultCallback<ActivityResult> buildAuthActivityResultCallback(int requestCode, AccessCodeCheckCallbacks activityCallbacks) {
        return result -> {
            Timber.d("buildAuthActivityResultCallback...requestCode: %d", requestCode);
            if (result.getResultCode() == Activity.RESULT_OK) {
                Intent data = result.getData();
                String pwd = data.getStringExtra("resultText");
                checkAccessCode(pwd, requestCode, activityCallbacks);
            } else if (result.getResultCode() == Activity.RESULT_CANCELED) {
                activityCallbacks.onAuthCancellation();
            }
        };
    }

    // NOTE: it is super important that the content that needs to be protected is not already
    //  displayed, otherwise this AccessCode Prompt mechanism (which opens an Activity on top)
    //  will be superficial (broken) auth. This is why UnauthenticatedFragment exists.
    public static void accessCodeQuestionableProtection(
            Context context,
            int requestType,
            ActivityResultLauncher<Intent> adHocAdminAuthLauncher,
            ActivityResultLauncher<Intent> adHocExitAuthLauncher
    ) {
        Intent intent = new Intent(context, InputActivity.class);

        Bundle b = new Bundle();
        b.putInt(IUIDisplay.uiScreenMaxLen, 9);
        b.putInt(IUIDisplay.uiScreenMinLen, 0);
        b.putSerializable(IUIDisplay.uiScreenID, IUIDisplay.SCREEN_ID.GET_INPUT_NUMBER_PASSWORD);
        b.putString(IUIDisplay.uiScreenInputHint, UI.getInstance().getPrompt(IUIDisplay.String_id.STR_ENTER_PASSWORD));
        b.putString(IUIDisplay.uiScreenTitle, UI.getInstance().getPrompt(IUIDisplay.String_id.STR_ADMIN_PASSWORD));
        b.putString(IUIDisplay.uiScreenPrompt, UI.getInstance().getPrompt(IUIDisplay.String_id.STR_PLEASE_ENTER));

        intent.putExtras(b); //Put your id to your next Intent
        if (requestType == ACCESSCODE_ADMIN_MENU) {
            adHocAdminAuthLauncher.launch(intent);
        } else if (requestType == ACCESSCODE_EXIT) {
            adHocExitAuthLauncher.launch(intent);
        } else {
            Timber.e("Unhandled AccessCode Auth requestType!");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_keyboard);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().setStatusBarColor(BrandingConfig.getBrandDisplayStatusBarColourOrDefault(getColor(R.color.color_linkly_primary)));
        }
        ViewStub stub = (ViewStub) findViewById(R.id.layout_stub);
        stub.setLayoutResource(R.layout.content_input);
        View v = stub.inflate();

        CustomEditText editText = (CustomEditText) findViewById(R.id.txtInput);

        if (mCustomKeyboard == null) {
            mCustomKeyboard = new CustomKeyboard(this, true);
        }
        mCustomKeyboard.registerEditText(editText, this, null, NUM_ALPHA);

        if (editText != null) {
            editText.requestFocus();
            editText.setFocusableInTouchMode(true);
        }

        try {
            String basePath = MalFactory.getInstance().getFile().getCommonDir();// "/data/data/" + pth + "/files"
            File imgFile = new File(basePath, BrandingConfig.getBrandDisplayLogoHeaderOrDefault());
            if (imgFile.exists()) {
                brandingHeaderBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            }

           if (brandingHeaderBitmap != null){
                ImageView headerLogo = (ImageView) findViewById(R.id.header);
                headerLogo.setImageBitmap(brandingHeaderBitmap);
            }
        } catch (Exception ex) {
            Timber.w(ex);
        }

        showScreen();
        mCustomKeyboard.showCustomKeyboard(v, editText);
    }


    @Override
    protected void onResume() {
        super.onResume();

        CustomEditText editText = (CustomEditText) findViewById(R.id.txtInput);
        if (editText != null){
            editText.requestFocus();
            editText.setEnabled(true);
            editText.setSelection(editText.getText().length());
        }

    }

    @Override
    public void onDoneClicked() {
        //Done Handler

        if (isInputValid()) {

            CustomEditText editText = (CustomEditText) findViewById(R.id.txtInput);

            Intent data = new Intent();
            data.putExtra("resultText", editText.getText().toString());
            setResult(RESULT_OK, data);
            finishAfterTransition();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onCancelClicked() {
        //Cancel Handler
        finishAfterTransition();

    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {

        if (MalFactory.getInstance().getHardware().processVolumeKey(getApplicationContext(), event))
            return true;

        return super.dispatchKeyEvent(event);
    }

    public void SetEditorActions(CustomEditText editTextView, final IUIKeyboard.OnDoneClickedListener doneListener, final IUIKeyboard.OnCancelClickedListener cancelListener, CustomKeyboard.KBTypes kbType) {

        if (mCustomKeyboard == null) {
            mCustomKeyboard = new CustomKeyboard(this, false);
        }

        mCustomKeyboard.registerEditText(editTextView, doneListener, cancelListener, kbType);

    }

    @SuppressWarnings("deprecation")
    public void showScreen() {

        Bundle b = getIntent().getExtras();

        TextView txtTitle = (TextView) findViewById(R.id.header_title);
        TextView txtPrompt = (TextView) findViewById(R.id.prompt_text);

        CustomEditText input = (CustomEditText) findViewById(R.id.txtInput);

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
            txtTitle.setVisibility(TextView.INVISIBLE);
        }

        //Set The Prompt Text
        String prompt = b.getString(IUIDisplay.uiScreenPrompt);

        if (prompt != null && prompt.length() > 0) {
            txtPrompt.setText(prompt);
        } else {
            txtPrompt.setVisibility(TextView.INVISIBLE);
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
            double d = Double.parseDouble(text);
        } catch (NumberFormatException nfe) {
            isValid = false;
        }

        return isValid;
    }


    private boolean isValidEmailAddress(String email) {
        String ePattern = "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(ePattern);
        java.util.regex.Matcher m = p.matcher(email);
        return m.matches();
    }


    private boolean isInputValid() {
        boolean isValid = true;
        CustomEditText input = (CustomEditText) findViewById(R.id.txtInput);
        switch (inputType) {
            case GET_INPUT_ALPHA:
                isValid = basicValidation(input.getText().toString());
                break;
            case GET_INPUT_ALPHA_NUMERIC:
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
            input.setError("Invalid Input");
        }


        return isValid;
    }

}
