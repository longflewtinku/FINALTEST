package com.linkly.payment.fragments;

import static android.view.KeyEvent.KEYCODE_ENTER;
import static com.linkly.payment.activities.BaseActivity.BASE_ACTIVITY_DEF_TIMEOUT;

import android.content.Context;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.fragment.app.Fragment;

import com.linkly.libengine.engine.Engine;
import com.linkly.libui.IUIDisplay;
import com.linkly.libui.IUIKeyboard;
import com.linkly.libui.display.DisplayRequest;
import com.linkly.libui.keyboard.CustomKeyboard;
import com.linkly.libui.views.CustomEditText;
import com.linkly.payment.R;
import com.linkly.payment.activities.BaseActivity;
import com.linkly.payment.viewmodel.BaseViewModel;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import timber.log.Timber;


@SuppressWarnings("deprecation")
public abstract class BaseFragment<T extends ViewDataBinding, V extends BaseViewModel> extends Fragment implements KeyboardView.OnKeyListener {

    private static final String TAG = "BaseFragment";

    @SuppressWarnings("rawtypes")
    private BaseActivity mActivity;
    private View mRootView;
    private T mViewDataBinding;
    private V mViewModel;
    public CustomKeyboard mCustomKeyboard;
    private Timer fragmentTimeout;
    private IUIDisplay.ACTIVITY_ID id; // ID of the fragment

    /**
     * Override for set binding variable
     *
     * @return variable id
     */
    public abstract int getBindingVariable();

    /**
     * @return layout resource id
     */
    public abstract
    @LayoutRes
    int getLayoutId();

    /**
     * Override for set view model
     *
     * @return view model instance
     */
    public abstract V getViewModel();

    @SuppressWarnings("rawtypes")
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof BaseActivity) {
            BaseActivity activity = (BaseActivity) context;
            this.mActivity = activity;
        }
    }

    // TODO FIXME use ViewModels properly (one per screen).
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Timber.d("onCreate...this: %s", this);
        mViewModel = getViewModel();
        setHasOptionsMenu(false);
        Timber.i("On Create");
        DisplayRequest d = mViewModel.getDisplay().getValue();
        Timber.i("Title:" + d.getActivityID().name() + " Prompt:" + d.getUiExtras().getString(IUIDisplay.uiScreenPrompt) + " UID: " + d.getUiExtras().getInt(IUIDisplay.uiUniqueId));
        id = d.getActivityID(); // Make sure
    }

    @Override
    public void onStop() {
        super.onStop();
        Timber.d("onStop...this: %s", this);
        DisplayRequest d = mViewModel.getDisplay().getValue();
        Timber.i("Title:" + d.getActivityID().name() + " Prompt:" + d.getUiExtras().getString(IUIDisplay.uiScreenPrompt) + " UID: " + d.getUiExtras().getInt(IUIDisplay.uiUniqueId));
    }

    // Allows the derived class control over sending the abort response.
    public boolean allowOnPauseResponse() {
        Timber.i("Allowed Response returning default");
        return true;
    }

    @Override
    public void onPause() {
        super.onPause();
        cancelFragmentTimeout();
        DisplayRequest d = mViewModel.getDisplay().getValue();
        Timber.d("onPause...this: %s", this);
        Timber.tag(TAG).i("OnPause mViewModel:" + d.getActivityID().name() + " Frag ID:" + id.name() + " UID: " + d.getUiExtras().getInt(IUIDisplay.uiUniqueId));

        if(d.getActivityID() != id) {
            Timber.w("Mismatching View Model and Fragment, Will not send default response. %s but expected %s", d.getActivityID(), id);
        }

        // Derived fragments point to the same view model.
        // Causes issues in which the old fragment calls on pause uses the data of the new fragment response
        if (allowOnPauseResponse() && d.getActivityID() == id && !d.getUiExtras().getBoolean(IUIDisplay.uiKeepOnScreen, false)) {
            Timber.i("Sending Response abort - this: %s", this);
            sendResponse(IUIDisplay.UIResultCode.ABORT, "", "");
        } else {
            Timber.e("Not sending response for %s", id.name());
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mViewDataBinding = DataBindingUtil.inflate(inflater, getLayoutId(), container, false);
        mRootView = mViewDataBinding.getRoot();
        return mRootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mViewDataBinding = null;
        mRootView = null;
        if( this.fragmentTimeout != null ){
            this.fragmentTimeout.cancel();
            this.fragmentTimeout.purge();
            this.fragmentTimeout = null;
        }
    }

    @Override
    public void onDestroy() {
        DisplayRequest d = mViewModel.getDisplay().getValue();
        Timber.d("onDestroy...this: %s", this);
        Timber.i("Title:" + d.getUiExtras().getString(IUIDisplay.uiScreenTitle) + " Prompt:" + d.getUiExtras().getString(IUIDisplay.uiScreenPrompt) + " UID: " + d.getUiExtras().getInt(IUIDisplay.uiUniqueId));
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        mActivity = null;
        super.onDetach();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewDataBinding.setVariable(getBindingVariable(), mViewModel);
        mViewDataBinding.setLifecycleOwner(getViewLifecycleOwner());
        mViewDataBinding.executePendingBindings();
    }

    @SuppressWarnings("rawtypes")
    public BaseActivity getBaseActivity() {
        return mActivity;
    }

    public T getViewDataBinding() {
        return mViewDataBinding;
    }


    public DisplayRequest getDisplayRequest() {
        return mViewModel.getDisplay().getValue();
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {

        if (event.getAction() == KeyEvent.ACTION_UP) {
            if (keyCode == KEYCODE_ENTER) {
                //Call the Done Call back if provided
                Timber.i("[ACTI DONE  ]" + this.getClass().toString());
                ((IUIKeyboard.OnDoneClickedListener) this).onDoneClicked();
            } else if (keyCode == Keyboard.KEYCODE_CANCEL) {
                //Call the Done Call back if provided
                ((IUIKeyboard.OnCancelClickedListener) this).onCancelClicked();
            }
        }

        return true;
    }

    public void SetEditorActions(CustomEditText editTextView, final IUIKeyboard.OnDoneClickedListener doneListener, CustomKeyboard.KBTypes kbType) {

        if (mCustomKeyboard == null) {
            mCustomKeyboard = new CustomKeyboard(getBaseActivity(),true);
        }

        mCustomKeyboard.registerEditText( editTextView, doneListener, null, kbType );
    }

    public void SetHeader( boolean menuEnabled, boolean headerBarEnabled ) {
        FragHeader header = ( FragHeader ) getChildFragmentManager().findFragmentById( R.id.headlines_fragment );
        if ( header != null ) {
            header.setDropDownVisibility( menuEnabled );
            header.setHeaderBarVisibility( headerBarEnabled );
            if (Engine.getDep().getCustomer().hideBrandDisplayLogoHeader()) {
                header.setCustomerLogoVisibility(false);
            }
        }
    }

    public void SetHeader( boolean menuEnabled, boolean headerBarEnabled, boolean customerLogoEnabled ) {
        SetHeader( menuEnabled, headerBarEnabled );
        FragHeader header = ( FragHeader ) getChildFragmentManager().findFragmentById( R.id.headlines_fragment );
        if ( null != header ) {
            header.setCustomerLogoVisibility( customerLogoEnabled );
        }
    }

    public void cancelFragmentTimeout() {
        if ( fragmentTimeout != null ) {
            fragmentTimeout.cancel();
            fragmentTimeout.purge();
            fragmentTimeout = null;
        }
    }

    public void showScreen() {
        resetFragmentTimeout();
    }

    // global refresh mechanism (mostly used for FragHeader), triggered by APP_REFRESH_SCREEN_EVENT.
    protected void refreshUI() {}

    protected void resetFragmentTimeout() {
        Timber.d("resetFragmentTimeout...");
        cancelFragmentTimeout();
        if (this.getDisplayRequest() == null) {
            Timber.i("Null Display Message in BaseActivity");
        } else if (this.getDisplayRequest().getUiExtras() == null) {
            Timber.i("Null Display Extras in BaseActivity");
        } else {
            int screenTimeout = this.getDisplayRequest().getUiExtras().getInt(IUIDisplay.uiScreenTimeout);
            if (screenTimeout > 0) {
                fragmentTimeout = new Timer();
                fragmentTimeout.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        Timber.e("FragmentTimeout RUNNING!");
                        getDisplayRequest().debug("onTimeout");
                        Timber.i("Abort Timer Timeout");
                        abortFragment("Timeout");
                        if (getBaseActivity() != null)
                            getBaseActivity().returnToMainMenu();
                    }
                    // TODO FIXME band-aid for architecture problems. The fix isn't in this code
                    //  (apart from deleting it), it is in the architecture that causes the situation
                    //  that needs this code.
                    // Wait for BASE_ACTIVITY_DEF_TIMEOUT after fragment timed-out: allow delayed fragment to come
                    // (as happened with GetCard after fallback' transaction restart)
                }, (long)screenTimeout+BASE_ACTIVITY_DEF_TIMEOUT);
            }
        }
    }

    protected void abortFragment(String abortReason) {
        Timber.i("Abort Frag:%s", abortReason);
        sendResponse(IUIDisplay.UIResultCode.ABORT, abortReason, "");
    }

    public void sendResponse(IUIDisplay.UIResultCode uiResultCode, String sResult1, String sResult2) {
        Timber.i("Send Response:%s", uiResultCode.name());
        if (mViewModel.sendResponse(uiResultCode, sResult1, sResult2))

            if (uiResultCode != IUIDisplay.UIResultCode.READY && uiResultCode != IUIDisplay.UIResultCode.CARDREAD) /* dont finish on a READY as its just a status update to the back end */
                finishOnTimeout();
    }

    void sendResponse(HashMap<String, Object> map) {
        if (mViewModel.sendResponse(map))
            finishOnTimeout();
    }

    /* Backup code so that after sending a response if nothing happens we always finish the activity and get back to the main menu */
    // TODO but what if it is a Fragment extending BaseFragment and already in the MainMenu?
    // TODO this makes sense for Standalone only.
    private void finishOnTimeout() {
        DisplayRequest d = mViewModel.getDisplay().getValue();
        if (!d.getUiExtras().getBoolean(IUIDisplay.uiKeepOnScreen, false)) {
            if (getBaseActivity() != null) {
                getBaseActivity().finishOnTimeout(BASE_ACTIVITY_DEF_TIMEOUT);
            }
        }
    }
}
