package com.linkly.payment.fragments;

import static android.view.View.GONE;
import static com.linkly.libui.IUIDisplay.ACTIVITY_ID.ACT_INPUT_AMOUNT;

import android.os.Bundle;
import android.view.View;

import androidx.lifecycle.ViewModelProviders;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.linkly.libengine.engine.Engine;
import com.linkly.libui.IUIDisplay;
import com.linkly.libui.views.CustomEditText;
import com.linkly.payment.R;
import com.linkly.payment.activities.ActScreenSaver;
import com.linkly.payment.viewmodel.FragKeyboardViewModel;

import java.util.Objects;

import timber.log.Timber;

public class FragInputAmount extends BaseInputAmount {

    public static final String TAG = FragInputAmount.class.getSimpleName();
    private FragKeyboardViewModel fragKeyboardViewModel;

    public static FragInputAmount newInstance() {
        Bundle args = new Bundle();
        FragInputAmount fragment = new FragInputAmount();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onStart() {
        super.onStart();
        Timber.d("onStart[FragInputAmount]...");
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
        fragKeyboardViewModel.init(ACT_INPUT_AMOUNT);
        return fragKeyboardViewModel;
    }

    @Override
    protected void showScreen(View v) {
        showScreen(v, Objects.requireNonNull(fragKeyboardViewModel.getFragData().getValue()));
    }

    @Override
    protected void updateScreenSaver() {
        ActScreenSaver.cancelScreenSaver();
    }

    @Override
    protected void setupFloatingActionButton(View v) {
        // hide the back button for normal amount input flows, eg Tip entry
        FloatingActionButton fab = v.findViewById(R.id.fab);
        fab.setVisibility(GONE);
    }

    @Override
    public void onDoneClicked() {
        //Done Handler
        if (isInputValid()) {
            CustomEditText editText = getView().findViewById(R.id.txtAmount);
            String digits = editText.getText().toString().replaceAll("\\D", "");
            digits = Engine.getDep().getFramework().getCurrency().formatUIAmountResponse(digits, Engine.getDep().getPayCfg().getCountryCode());
            sendResponse(IUIDisplay.UIResultCode.OK, digits, "");
        }
    }

    @Override
    public void onCancelClicked() {
        //Cancel Handler
        try {
            requireActivity().onBackPressed();
        } catch (Exception e) {
            // This is a slow way to update an unmanaged task stack. BaseActivity does this already.
            abortFragment("Cancel clicked");
        }
    }
}