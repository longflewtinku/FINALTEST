package com.linkly.payment.fragments;

import static android.view.View.GONE;
import static com.linkly.libsecapp.IP2PCard.CardType.CT_MSR;
import static com.linkly.libsecapp.IP2PCard.CardType.CT_NONE;
import static com.linkly.libui.IUIDisplay.ACTIVITY_ID.ACT_USER_LOGIN;
import static com.linkly.libui.IUIDisplay.String_id.STR_PASSWORD;
import static com.linkly.libui.IUIDisplay.String_id.STR_USER_ID;
import static com.linkly.libui.IUIDisplay.UIResultCode.ABORT;
import static com.linkly.libui.keyboard.CustomKeyboard.KBTypes.USR_PASS_KB;

import android.content.res.Configuration;
import android.inputmethodservice.Keyboard;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Selection;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProviders;
import androidx.preference.PreferenceManager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;
import com.linkly.kotlinandroidtoolkit.extensionfuncs.BundleExtensionsKt;
import com.linkly.libconfig.ProfileCfg;
import com.linkly.libengine.engine.Engine;
import com.linkly.libengine.overrides.CoreOverrides;
import com.linkly.libengine.users.UserManager;
import com.linkly.libmal.global.platform.EFTPlatform;
import com.linkly.libmal.global.util.Util;
import com.linkly.libpositive.messages.IMessages;
import com.linkly.libsecapp.IP2PCard;
import com.linkly.libsecapp.P2PLib;
import com.linkly.libui.IUIDisplay;
import com.linkly.libui.IUIDisplay.String_id;
import com.linkly.libui.IUIKeyboard;
import com.linkly.libui.views.CustomEditText;
import com.linkly.payment.R;
import com.linkly.payment.activities.ActScreenSaver;
import com.linkly.payment.databinding.ActivityTransBinding;
import com.linkly.payment.utilities.UIUtilities;
import com.linkly.payment.viewmodel.FragKeyboardViewModel;
import com.pax.dal.entity.TrackData;

import java.util.HashMap;

import timber.log.Timber;

public class FragLogin extends BaseFragment<ActivityTransBinding, FragKeyboardViewModel>
        implements IUIKeyboard.OnDoneClickedListener,
        IUIKeyboard.OnCancelClickedListener, View.OnClickListener {

    public static final String TAG = FragLogin.class.getSimpleName();
    private FragKeyboardViewModel fragKeyboardViewModel;
    private static boolean upgrading;
    private boolean fragmentRestarting = false;
    private FragLogin.CardGetThread cardThread;

    // Flag added to handle on pause sending of response to business layer.
    // Required as current workflow logic waits indefinitely. but if a screensaver happens,
    // the screen isn't restarted/redrawn as originally we getting reset by onpause logic in base fragment
    private boolean sentResponse = false;
    // Display references.
    private CustomEditText mUserIDView;
    private CustomEditText mPasswordView;
    private CheckBox saveCredentials;
    private TextInputLayout mUidLayout;
    private TextInputLayout mPasswordLayout;

    public static FragLogin newInstance() {
        Bundle args = new Bundle();
        FragLogin fragment = new FragLogin();
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
        fragKeyboardViewModel.init(ACT_USER_LOGIN);
        return fragKeyboardViewModel;
    }

    @Override
    public boolean allowOnPauseResponse() {
        // Adding this override appears to prevent spurious login failures (when correct UID and PW are supplied),
        // because it seems that the 'abort' response, sent during onPause, prevents a later valid response from
        // being sent.
        // i.e., without this method override, sometimes logins fail, for no apparent reason.
        // Added a boolean. it seems that any to background happens this locks up the terminal.
        Timber.i("allowOnPauseResponse: false (override)");
        return !sentResponse;
    }

    @SuppressWarnings("deprecation")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = super.onCreateView(inflater, container, savedInstanceState);

        ViewStub pageContent = v.findViewById(R.id.ui2_layout_stub);
        pageContent.setLayoutResource(R.layout.fragment_login);
        pageContent.inflate();

        // hide the back button for login screens, eg User login
        FloatingActionButton fab = v.findViewById(R.id.fab);
        fab.setVisibility(GONE);

        UIUtilities.validateTerminalConfig(getBaseActivity());

        getBaseActivity().RegisterAsExtendedActivity(IMessages.APP_CHARGING_EVENT);

        // Set up the login form.
        mUidLayout =  v.findViewById(R.id.text_input_user_id);
        mPasswordLayout =  v.findViewById(R.id.text_input_password);

        mUserIDView = v.findViewById(R.id.userid);
        mPasswordView = v.findViewById(R.id.password);
        mUserIDView.setHint(Engine.getDep().getPrompt(STR_USER_ID));
        mPasswordView.setHint(Engine.getDep().getPrompt(STR_PASSWORD));
        saveCredentials = v.findViewById(R.id.saveCred);
        saveCredentials.setText(Engine.getDep().getPrompt(IUIDisplay.String_id.STR_SAVE_CREDS));


        // if auto login is disable we need to disable the checkbox
        if (PreferenceManager.getDefaultSharedPreferences(requireContext()).getBoolean("autologin", true) && !upgrading ) {
            saveCredentials.setVisibility(View.VISIBLE);
        } else {
            saveCredentials.setVisibility(View.INVISIBLE);
        }

        mUserIDView.setFocusableInTouchMode(true);
        mPasswordView.setFocusableInTouchMode(true);

        SetEditorActions(mPasswordView, this, USR_PASS_KB);
        SetEditorActions(mUserIDView, this, USR_PASS_KB);
        if (!EFTPlatform.hasPhysicalKeyboard())
            mPasswordView.setOnKeyListener(this);

        mUserIDView.setOnKeyListener( ( v1, keyCode, event ) -> {
            Timber.i("KeyCode = " + keyCode );
            Timber.i("event = " + event.toString() );
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                ActScreenSaver.resetScreenSaver(requireContext());
                if (event.getKeyCode() == Keyboard.KEYCODE_CANCEL) {
                    onCancelClicked();
                    return true;
                }
            }
            return false;
        } );

        mUserIDView.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                Selection.setSelection(mUserIDView.getText(), mUserIDView.getText().length());
            }

            @Override
            public void afterTextChanged(Editable editable) {
                ActScreenSaver.resetScreenSaver(requireContext());
            }
        });

        mPasswordView.setOnKeyListener( ( view, i, keyEvent ) -> {
            // MW: Need this for landscape mode for some reason I don't fully comprehend.
            // keeping it in until I can find the source of the bug
            return false;
        } );

        mPasswordView.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                Selection.setSelection(mPasswordView.getText(), mPasswordView.getText().length());
            }

            @Override
            public void afterTextChanged(Editable editable) {
                ActScreenSaver.resetScreenSaver(requireContext());
            }
        });


        showScreen(v);

        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mCustomKeyboard.showCustomKeyboard(view, view.findViewById(R.id.userid));
        mCustomKeyboard.showCustomKeyboard( view, view.findViewById( R.id.password ) );

    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        Timber.i("keyCode = " + keyCode );
        Timber.i("event = " + event.toString() );
        boolean ret = super.onKey(v, keyCode, event);
        if (event.getAction() == KeyEvent.ACTION_DOWN)
            ActScreenSaver.resetScreenSaver(requireContext());
        if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK){
            onBackPressed();
            return true;
        }
        return ret;
    }

    @Override
    public void onClick(View v) {}

    @Override
    public void onPause() {
        super.onPause();

        if(UserManager.getActiveUser() != null) {
            Timber.e("Relying on FragLogin to cleanup MenuUser!");
            // TODO why is FragLogin worried about Menus? Menus should be destroyed before being on FragLogin.
            //  This is also a problem as MenuUsers is a singleton and thus needs the real listener first time.
//            MenuUsers.getInstance(new Menu.OnMenuNavigationListener() {
//                @Override
//                public void onNeedToNavigate() {
//                    // no special need for this use case
//                }
//            }).refreshMenu();
        }
        ActScreenSaver.cancelScreenSaver();
        stopCardThread();
    }

    @Override
    public void onDestroy() {
        this.stopCardThread();
        super.onDestroy();
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onResume() {
        super.onResume();
        // always reset this value
        sentResponse = false;

        if (fragmentRestarting) {
            // Abort workflow when fragment state has moved from "Paused" to "Resumed"
            // No reuse of old fragment if new workflow will be created
            getBaseActivity().onBackPressed();
            getBaseActivity().returnToMainMenu();
            return;
        }
        fragmentRestarting = true;

        // if auto login is disable we need to disable the checkbox
        if (PreferenceManager.getDefaultSharedPreferences(requireContext()).getBoolean("autologin", true) && !upgrading) {
            saveCredentials.setVisibility(View.VISIBLE);
        } else {
            saveCredentials.setVisibility(View.INVISIBLE);
        }

        if (getBaseActivity().getIntent().getBooleanExtra("EXIT", false)) {
            getBaseActivity().returnToMainMenu();
            return;
        }

        mUserIDView.requestFocus();

        if (EFTPlatform.isPaxTerminal()) {
            if (!ProfileCfg.getInstance().isDemo()) {
                cardThread = new FragLogin.CardGetThread(this);
                cardThread.start();
            }
        }

        getBaseActivity().checkHideWhenDone();

        ActScreenSaver.enableScreenSaver(true, requireContext());

    }

    @SuppressWarnings("deprecation")
    public void onBackPressed() {
        getBaseActivity().onBackPressed();
        getBaseActivity().returnToMainMenu();
    }

    @Override
    public void onDoneClicked() {
        //Done Handler
        attemptLogin();
    }

    @Override
    public void onCancelClicked() {
        onBackPressed();
    }

    public void showScreen(View v) {
        TextView txtTitle = v.findViewById(R.id.header_title);
        TextView txtPrompt = v.findViewById(R.id.prompt_text);

        //Get Max and Min values
        int maxLen = fragKeyboardViewModel.getDisplay().getValue().getUiExtras().getInt( IUIDisplay.uiScreenMaxLen, 100 );
        int maxLenPW = fragKeyboardViewModel.getDisplay().getValue().getUiExtras().getInt( IUIDisplay.uiPasswordMaxLen, 8 );

        //Set Input Fields Max Length for user id entry
        InputFilter[] filterArray = new InputFilter[1];
        filterArray[0] = new InputFilter.LengthFilter( maxLen );
        mUserIDView.setFilters(filterArray);

        //Set Input Fields Max Length for password entry
        InputFilter[] filterArrayPW = new InputFilter[1];
        filterArrayPW[0] = new InputFilter.LengthFilter( maxLenPW );
        mPasswordView.setFilters(filterArrayPW);


        //Set The Title Text
        String title = fragKeyboardViewModel.getTitle().getValue();

        if ( title != null && title.length() > 0) {
            txtTitle.setText( title );
        } else {
            txtTitle.setVisibility(TextView.INVISIBLE);
            title = " No Title ";
        }

        //Set The Prompt Text
        String prompt = fragKeyboardViewModel.getPrompt().getValue();

        if ( prompt != null && prompt.length() > 0) {
            txtPrompt.setText( prompt );
        } else {
            prompt = " No Prompt ";
            txtPrompt.setVisibility(TextView.INVISIBLE);
        }

        /* if not sent default to a normal login */
        if (fragKeyboardViewModel.getDisplay().getValue().getUiExtras().getBoolean(IUIDisplay.uiUserUpgrade)) {
            upgrading = fragKeyboardViewModel.getDisplay().getValue().getUiExtras().getBoolean(IUIDisplay.uiUserUpgrade);
        } else {
            upgrading = false;
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        if ( getActivity().getResources().getConfiguration().orientation != Configuration.ORIENTATION_PORTRAIT ) {
            // TODO: THIS IS HIDEOUS. Fix this after the demo
            SetHeader( false, false, false );
        } else {
            // Hide MainMenu Drop down - headler_fragment
            SetHeader( true, false );
        }
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid userid, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    @SuppressWarnings("deprecation")
    private void attemptLogin() {

        // Reset errors.
        mUserIDView.setError(null);
        mPasswordView.setError(null);
        mUidLayout.setError(null);
        mPasswordLayout.setError(null);

        // Store values at the time of the login attempt.
        String userid = mUserIDView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password)) {
            focusView = mPasswordView;
            cancel = true;
        } else if (!isPasswordValid(password)) {
            Util.setInputLayoutError(mPasswordLayout, Engine.getDep().getPrompt(String_id.STR_ERROR_PASSWORD_TOO_SHORT));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid userid.
        if (TextUtils.isEmpty(userid)) {
            Util.setInputLayoutError(mUidLayout, Engine.getDep().getPrompt(String_id.STR_ERROR_FIELD_REQUIRED));
            focusView = mUserIDView;
            cancel = true;
        } else if (!isUserIdValid(userid)) {
            Util.setInputLayoutError(mUidLayout, Engine.getDep().getPrompt(IUIDisplay.String_id.STR_ERROR_INVALID_USERID));
            focusView = mUserIDView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            /*Change the Way we attempt the Login and Perform all the work in this activity*/
            if( fragKeyboardViewModel.getDisplay().getValue() != null ) {
                HashMap<String, Object> map = new HashMap<>();
                IUIDisplay.SCREEN_ID iScreenId = BundleExtensionsKt.getSerializableCompat(
                        fragKeyboardViewModel.getDisplay().getValue().getUiExtras(),
                        IUIDisplay.uiScreenID,
                        IUIDisplay.SCREEN_ID.class);

                map.put( IUIDisplay.uiResultText1, userid );
                map.put( IUIDisplay.uiResultText2, password );
                map.put( IUIDisplay.uiScreenID, iScreenId );
                map.put( IUIDisplay.uiResultCode, IUIDisplay.UIResultCode.OK );
                map.put( IUIDisplay.uiSelectChoice, saveCredentials.isChecked() );

                if ( Engine.getAppCallbacks() != null ) {
                    Engine.getAppCallbacks().runPleaseWaitScreen();
                }
                sentResponse = true;
                sendResponse( map );
            } else {
                Timber.e("fragKeyboardViewModel.getDisplay().getValue is NULL" );
            }
        }
    }


    private boolean isUserIdValid(String userid) {
        return userid.length() >= 4;
    }

    private boolean isPasswordValid(String password) {
        return password.length() >= 6;
    }


    //Swipe to Login Functions
    protected void stopCardThread() {
        if (cardThread != null) {
            cardThread.cancel();
            cardThread.fragLogin = null;
            cardThread = null;
        }
    }


    private void attempLoginWithCard(String track2) {
        if (track2 == null || track2.length() == 0) {
            return;
        }

        sentResponse = true;
        sendResponse(IUIDisplay.UIResultCode.OK, track2, "SWIPEDCARD");
    }


    private static class CardGetThread extends Thread {

        boolean running = false;
        FragLogin fragLogin = null;

        CardGetThread(FragLogin cardActivity) {
            this.fragLogin = cardActivity;
        }

        public void run() {
            super.run();

            if (CoreOverrides.get().isRunningAutoTests())
                return;

            IP2PCard.CardType cType = CT_NONE;
            IP2PCard iMalCard = P2PLib.getInstance().getIP2PCard();
            running = true;

            while (running && cType != CT_MSR) {
                if (CoreOverrides.get().isMagstripeTrackDataReady()) {   // hack for automated tests for magstripe
                    cType = CT_MSR;
                } else {
                    cType = iMalCard.cardDetect();
                }
            }

            if (cType == CT_MSR) {
                Timber.i("Card Swipe Seen");
            }

            //Get the Track2 Data and Validate it against the user credentials
            if (running) {
                TrackData cardData = P2PLib.getInstance().getIP2PMsr().readFromLastResult();
                if (cardData != null) {
                    Timber.i("Track2 Data : " + cardData.getTrack2());
                    this.fragLogin.attempLoginWithCard(cardData.getTrack2());
                }
            }
        }


        public void cancel() {
            running = false;
            if (this.isAlive()) {
                P2PLib.getInstance().getIP2PCard().cardDetectCancel();
            }
        }
    }



}



