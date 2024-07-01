package com.linkly.payment.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.linkly.payment.R;
import com.linkly.payment.activities.AppMain;


public class FragStatus extends Fragment {
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";

    public FragStatus() {
    }

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static FragStatus newInstance(int sectionNumber) {
        FragStatus fragment = new FragStatus();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_status, container, false);

        TextView textView = (TextView) rootView.findViewById(R.id.textView);
        textView.setText("Text Displayed");
        return rootView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        AppMain.addWatcher( this );
    }
}