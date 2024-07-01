package com.linkly.launcher.fragments;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.linkly.launcher.ProgressViewModel;
import com.linkly.launcher.R;
import com.linkly.libui.IUIDisplay.String_id;
import com.linkly.libui.UI;


public class WiFiConfigProgressFragment extends Fragment {
    ProgressViewModel model;
    TextView textPair;
    TextView textLAN;
    TextView textAP;
    TextView textTest;

    @Override
    public void onDestroyView() {
        model = null;
        textPair = null;
        textLAN = null;
        textAP = null;
        textTest = null;
        super.onDestroyView();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getActivity() != null) {
            model = new ViewModelProvider(getActivity()).get(ProgressViewModel.class);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_current_progress, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initialiseViews(view);

    }

    private void initialiseViews(View view) {
        TextView textTitle = view.findViewById(R.id.text_title);
        textTitle.setText(UI.getInstance().getPrompt(String_id.STR_WIFI_BASE_CONFIG));

        TextView textCurrentProgress = view.findViewById(R.id.text_current_progress);
        textCurrentProgress.setText(UI.getInstance().getPrompt(String_id.STR_CURRENT_PROGRESS));

        textPair = view.findViewById(R.id.text_pair);
        textPair.setText(UI.getInstance().getPrompt(String_id.STR_PAIR));

        textLAN = view.findViewById(R.id.text_lan);
        textLAN.setText(UI.getInstance().getPrompt(String_id.STR_LAN));

        textAP = view.findViewById(R.id.text_ap);
        textAP.setText(UI.getInstance().getPrompt(String_id.STR_AP));

        textTest = view.findViewById(R.id.text_test);
        textTest.setText(UI.getInstance().getPrompt(String_id.STR_TEST_NL));
    }

    @SuppressWarnings("fallthrough")
    @Override
    public void onResume() {
        super.onResume();

        int idPair = R.drawable.wifi_base_round_image_yellow;
        int idLAN  = R.drawable.wifi_base_round_image_grey;
        int idAP   = R.drawable.wifi_base_round_image_grey;
        int idTest = R.drawable.wifi_base_round_image_grey;

        String items = model.getSelected().getValue();
        if(null != items) {
            //Set current item to Yellow
            switch(items) {
                case "PAIRED": idLAN  = R.drawable.wifi_base_round_image_yellow; break;
                case "LAN":    idAP   = R.drawable.wifi_base_round_image_yellow; break;
                case "AP":     idTest = R.drawable.wifi_base_round_image_yellow; break;
                default:       break;
            }
            //Set all items before this to Green
            switch(items) {
                case "TEST":   idTest = R.drawable.wifi_base_round_image_green; //deliberate fallthrough
                case "AP":     idAP   = R.drawable.wifi_base_round_image_green; //deliberate fallthrough
                case "LAN":    idLAN  = R.drawable.wifi_base_round_image_green; //deliberate fallthrough
                case "PAIRED": idPair = R.drawable.wifi_base_round_image_green; //deliberate fallthrough
                default:       break;
            }
        }
        textPair.setBackground(ResourcesCompat.getDrawable(getResources(), idPair, null ));
        textLAN .setBackground(ResourcesCompat.getDrawable(getResources(), idLAN,  null ));
        textAP  .setBackground(ResourcesCompat.getDrawable(getResources(), idAP,   null ));
        textTest.setBackground(ResourcesCompat.getDrawable(getResources(), idTest, null ));
    }
}
