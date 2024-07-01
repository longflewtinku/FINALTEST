package com.linkly.payment.fragments;

import static com.linkly.libui.IUIDisplay.ACTIVITY_ID.ACT_INFORMATION_BASIC;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProviders;

import com.linkly.libengine.engine.Engine;
import com.linkly.libui.IUIDisplay.String_id;
import com.linkly.payment.R;
import com.linkly.payment.databinding.ActivityTransBinding;
import com.linkly.payment.viewmodel.FragStandardViewModel;

public class FragInformationBasic extends BaseFragment<ActivityTransBinding, FragStandardViewModel> {

    public static final String TAG = FragInformationBasic.class.getSimpleName();
    private FragStandardViewModel fragStandardViewModel;

    public static FragInformationBasic newInstance() {
        Bundle args = new Bundle();
        FragInformationBasic fragment = new FragInformationBasic();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public int getBindingVariable() {
        return com.linkly.payment.BR.fragStandardViewModel;
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_informationbasic;
    }

    @Override
    public FragStandardViewModel getViewModel() {
        fragStandardViewModel = ViewModelProviders.of(this).get(FragStandardViewModel.class);
        fragStandardViewModel.init(ACT_INFORMATION_BASIC);
        return fragStandardViewModel;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = super.onCreateView(inflater, container, savedInstanceState);

        SetHeader(false, false);

        ImageView inProgressView = rootView.findViewById(R.id.inProgressView);
        if (inProgressView != null) {
            inProgressView.setVisibility(ImageView.VISIBLE);
        }

        TextView textPleaseWait = rootView.findViewById(R.id.infoPrompt);
        textPleaseWait.setText(Engine.getDep().getPrompt(String_id.STR_PLEASE_WAIT_BR));

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}