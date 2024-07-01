package com.linkly.payment.fragments;

import static android.view.View.INVISIBLE;
import static com.linkly.libui.display.DisplayFragmentOption.AdditionalIcons.ICON_MATCH_FAIL;
import static com.linkly.libui.display.DisplayFragmentOption.AdditionalIcons.ICON_MATCH_FULL;
import static com.linkly.libui.display.DisplayFragmentOption.AdditionalIcons.ICON_MATCH_NOT_CHECKED;
import static com.linkly.libui.display.DisplayFragmentOption.AdditionalIcons.ICON_MATCH_PARTIAL;
import static com.linkly.libui.display.DisplayFragmentOption.AdditionalIcons.ICON_NOT_SET;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.linkly.libui.display.DisplayFragmentOption;
import com.linkly.payment.R;
import com.linkly.payment.activities.AppMain;

import java.util.ArrayList;

public class FragMoto extends Fragment {
    private static final String TAG = "FragHeader";
    public ArrayList<DisplayFragmentOption> fragOptions;

    private View view;

    public FragMoto() {
        // Required empty public constructor
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    public void setImage(ImageView imageView, TextView textView, DisplayFragmentOption fragOption) {

        if (fragOption == null)
            return;

        if (imageView != null) {
            if (fragOption.getIcon() == ICON_NOT_SET)
                imageView.setVisibility(INVISIBLE);
            else if (fragOption.getIcon() == ICON_MATCH_FULL)
                imageView.setImageResource(R.mipmap.matchfull);
            else if (fragOption.getIcon() == ICON_MATCH_PARTIAL)
                imageView.setImageResource(R.mipmap.matchpartial);
            else if (fragOption.getIcon() == ICON_MATCH_NOT_CHECKED)
                imageView.setImageResource(R.mipmap.matchnotchecked);
            else if (fragOption.getIcon() == ICON_MATCH_FAIL)
                imageView.setImageResource(R.mipmap.matchfail);
        }

        if (textView != null) {
            textView.setText(fragOption.getFragText());
        }

    }
    public void configureInfo() {

        /* default to hidden */
        if( fragOptions != null && fragOptions.size() >= 3) {
            view.setVisibility(View.VISIBLE);
            ImageView imageView1 = view.findViewById(R.id.icon1);
            TextView textView1 = view.findViewById(R.id.text1);
            setImage(imageView1, textView1, fragOptions.get(0));

            ImageView imageView2 = view.findViewById(R.id.icon2);
            TextView textView2 = view.findViewById(R.id.text2);
            setImage(imageView2, textView2, fragOptions.get(1));

            ImageView imageView3 = view.findViewById(R.id.icon3);
            TextView textView3 = view.findViewById(R.id.text3);
            setImage(imageView3, textView3, fragOptions.get(2));
            view.setVisibility(View.VISIBLE);
        }
    }
    @Override
    public void onResume() {
        super.onResume();
        configureInfo();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        AppMain.addWatcher( this );
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_moto_info, container, false);
        configureInfo();
        return view;
    }



}
