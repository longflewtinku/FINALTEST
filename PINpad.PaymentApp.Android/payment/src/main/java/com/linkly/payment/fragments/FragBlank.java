package com.linkly.payment.fragments;

import static com.linkly.libui.IUIDisplay.ACTIVITY_ID.ACT_BLANK;
import static com.linkly.libui.IUIDisplay.uiScreenBlankType;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProviders;

import com.linkly.libmal.global.util.DisplayKiosk;
import com.linkly.payment.R;
import com.linkly.payment.databinding.ActivityTransBinding;
import com.linkly.payment.viewmodel.FragStandardViewModel;

public class FragBlank extends BaseFragment<ActivityTransBinding, FragStandardViewModel> {

    public static final String TAG = FragBlank.class.getSimpleName();
    private FragStandardViewModel fragStandardViewModel;

    public static FragBlank newInstance() {
        Bundle args = new Bundle();
        FragBlank fragment = new FragBlank();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public int getBindingVariable() {
        return com.linkly.payment.BR.fragStandardViewModel;
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_blank;
    }

    @Override
    public FragStandardViewModel getViewModel() {
        fragStandardViewModel = ViewModelProviders.of(this).get(FragStandardViewModel.class);
        fragStandardViewModel.init(ACT_BLANK);
        return fragStandardViewModel;
    }

    @SuppressWarnings("deprecation")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);
        SetHeader(false, false);

        String type = fragStandardViewModel.getDisplay().getValue().getUiExtras().getString(uiScreenBlankType);
        if (type != null && type.compareToIgnoreCase("DateTime") == 0) {
            DisplayKiosk.getInstance().onResume(true);
            startActivityForResult(new Intent(Settings.ACTION_DATE_SETTINGS), 0);
        } else {
            getBaseActivity().returnToMainMenu();
        }

        return v;
    }

}





