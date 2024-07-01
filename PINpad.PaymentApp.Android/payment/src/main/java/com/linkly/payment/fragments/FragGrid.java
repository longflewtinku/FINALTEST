package com.linkly.payment.fragments;


import static android.view.View.GONE;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.linkly.libui.display.DisplayFragmentOption;
import com.linkly.payment.R;
import com.linkly.payment.activities.AppMain;

import java.util.ArrayList;

public class FragGrid extends Fragment {
    private static final String TAG = "FragHeader";
    public ArrayList<DisplayFragmentOption> fragOptions;

    private View view;

    public FragGrid() {
        // Required empty public constructor
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    public void setText(ArrayList<DisplayFragmentOption> fragOptions, int index, TextView view1, TextView view2) {

        DisplayFragmentOption fragOption = null;

        if (fragOptions.size() > index)
            fragOption = fragOptions.get(index);

        if (fragOption != null) {
            if (view1 != null) {
                view1.setText(fragOption.getFragText());

                // if only text (no amount), then center it
                if( fragOption.getFragAmount() == null ) {
                    view1.setGravity(Gravity.CENTER_HORIZONTAL);
                    if( view2 != null ) {
                        view2.setVisibility(GONE);
                    }
                }
            }

            if (view2 != null && fragOption.getFragAmount() != null ) {
                view2.setText(fragOption.getFragAmount());
            } else {
                view2.setVisibility(GONE);
            }

        } else {
            if (view1 != null)
                view1.setVisibility(GONE);

            if (view2 != null)
                view2.setVisibility(GONE);
        }


    }
    public void configureInfo() {

        /* default to hidden */
        if( fragOptions != null && fragOptions.size() > 0) {
            view.setVisibility(View.VISIBLE);

            TextView textView1 = (TextView) view.findViewById(R.id.text1);
            TextView textView1a = (TextView) view.findViewById(R.id.text1a);
            setText(fragOptions, 0,textView1, textView1a );

            TextView textView2 = (TextView) view.findViewById(R.id.text2);
            TextView textView2a = (TextView) view.findViewById(R.id.text2a);
            setText(fragOptions,1,textView2, textView2a );

            TextView textView3 = (TextView) view.findViewById(R.id.text3);
            TextView textView3a = (TextView) view.findViewById(R.id.text3a);
            setText(fragOptions,2,textView3, textView3a );

            TextView textView4 = (TextView) view.findViewById(R.id.text4);
            TextView textView4a = (TextView) view.findViewById(R.id.text4a);
            setText(fragOptions,3,textView4, textView4a );

            view.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        configureInfo();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_grid_info, container, false);
        configureInfo();
        return view;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        AppMain.addWatcher( this );
    }
}
