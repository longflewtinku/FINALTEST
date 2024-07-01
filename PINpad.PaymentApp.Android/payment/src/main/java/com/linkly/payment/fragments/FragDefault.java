package com.linkly.payment.fragments;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.linkly.payment.R;
import com.linkly.payment.activities.AppMain;

public class FragDefault extends Fragment {
    private static final String TAG = "FragDefault";

    public FragDefault() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_default_info, container, false);
        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        AppMain.addWatcher( this );
    }


}
