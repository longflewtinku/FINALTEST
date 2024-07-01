package com.linkly.launcher.fragments;


import static com.linkly.libui.IUIDisplay.String_id.STR_PAIRED;
import static com.linkly.libui.IUIDisplay.String_id.STR_UNPAIRED;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;

import com.linkly.launcher.BaseStation;
import com.linkly.launcher.ProgressViewModel;
import com.linkly.launcher.R;
import com.linkly.launcher.service.UpdateFirmware;
import com.linkly.libmal.global.util.DisplayKiosk;
import com.linkly.libui.IUIDisplay.String_id;
import com.linkly.libui.UI;

public class PairWithBaseStationFragment extends Fragment implements View.OnClickListener {
    Button btnBluetoothSettings;
    Button btnCancelSetup;
    Button btnNext;
    Button connectionStatus;
    private ProgressViewModel model;

    private BasePairingThread pairingThread;
    private final BasePairingListener basePairingListener = new BasePairingListener() {

        private void runOnUiThread(Runnable runnable) {
            if (getActivity() != null && !getActivity().isFinishing() && isResumed()) {
                getActivity().runOnUiThread(runnable);
            }
        }

        @Override
        public void onPairingFailed() {
            runOnUiThread(() -> {
                BaseStation.dismissProgress();
                BaseStation.displayError(BaseStation.ERROR_DIALOG_NAME, "Device not paired. Pair with the base first.");
                connectionStatus.setText(UI.getInstance().getPrompt(STR_UNPAIRED));
                connectionStatus.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.darker_gray));
                connectionStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.ui2ColorTwo));
            });
        }

        @SuppressWarnings("deprecation")
        @Override
        public void onPairingSuccess() {
            runOnUiThread(() -> {
                connectionStatus.setText(UI.getInstance().getPrompt(STR_PAIRED));
                BaseStation.dismissProgress();
                connectionStatus.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.colorGreenDark));
                connectionStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorWhite));

                if (BaseStation.getInstance().checkFirmwareOnConnect()) {
                    BaseStation.displayError(BaseStation.ERROR_DIALOG_NAME, "Starting Firmware Update");
                    UpdateFirmware update = new UpdateFirmware();
                    update.execute();
                }
            });
        }
    };

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
        return inflater.inflate(R.layout.fragment_pair_with_bs, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //Initialise view
        initialiseViews(view);
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshStatus();
    }

    @Override
    public void onPause() {
        // dismiss any progress before going to next page
        BaseStation.dismissProgress(0);

        super.onPause();
    }

    @Override
    public void onDestroy() {
        if (pairingThread != null && pairingThread.isAlive()) {
            pairingThread.resetListener();
        }
        super.onDestroy();
    }

    private void initialiseViews(View view) {

        TextView textPairWithBs = view.findViewById(R.id.text_pair_with_bs);
        textPairWithBs.setText(UI.getInstance().getPrompt(String_id.STR_PAIR_WITH_WIFI_BASE_STATION));

        TextView textCurrentStatus = view.findViewById(R.id.text_current_status);
        textCurrentStatus.setText(UI.getInstance().getPrompt(String_id.STR_CURRENT_STATUS));

        TextView textDetailsBS = view.findViewById(R.id.text_details);
        textDetailsBS.setText(UI.getInstance().getPrompt(String_id.STR_PAIR_BS_DETAILS));

        btnBluetoothSettings = view.findViewById(R.id.btn_bluetooth_settings);
        btnBluetoothSettings.setText(UI.getInstance().getPrompt(String_id.STR_OPEN_BLUETOOTH_SETTINGS));
        btnBluetoothSettings.setOnClickListener(this);
        //Cancel button
        btnCancelSetup = view.findViewById(R.id.btn_cancel_setup);
        btnCancelSetup.setText(UI.getInstance().getPrompt(String_id.STR_CANCEL_SETUP));
        btnCancelSetup.setVisibility(View.VISIBLE);
        btnCancelSetup.setOnClickListener(this);

        //Next button
        btnNext = view.findViewById(R.id.btn_next);
        btnNext.setText(UI.getInstance().getPrompt(String_id.STR_NEXT));
        btnNext.setOnClickListener(this);

        connectionStatus = view.findViewById(R.id.btn_bluetooth_status);
        connectionStatus.setText(UI.getInstance().getPrompt(STR_UNPAIRED));
    }
    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_bluetooth_settings) {
            DisplayKiosk kiosk = DisplayKiosk.getInstance();
            kiosk.onResume(true);
            startActivity(new Intent(Settings.ACTION_BLUETOOTH_SETTINGS));
        } else if (id == R.id.btn_cancel_setup) {
            if (getActivity() != null) {
                getActivity().finishAfterTransition();
            }
        } else if (id == R.id.btn_next) {
            //Replace fragment
            Fragment fragmentToLaunch = new LANSetupFragment();
            FragmentManager fragmentManager = getParentFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.settings_fragment, fragmentToLaunch).addToBackStack(null).commit();
        }
    }

    private void refreshStatus() {
        model.setSelected("UNPAIRED");
        BaseStation.displayProgress(BaseStation.ERROR_DIALOG_NAME, "Loading paired device");

        if (pairingThread == null || !pairingThread.isPairingInProgress() || !pairingThread.isAlive()) {
            pairingThread = new BasePairingThread(basePairingListener);
            pairingThread.start();
        }

        DisplayKiosk.getInstance().onResume(false);
    }

    static class BasePairingThread extends Thread {

        private boolean pairingInProgress = false;
        private BasePairingListener basePairingListener;

        public BasePairingThread(@Nullable BasePairingListener basePairingListener) {
            this.basePairingListener = basePairingListener;
        }

        @Override
        public void run() {
            pairingInProgress = true;
            if (BaseStation.getInstance().isConnected()) {
                if (basePairingListener != null) {
                    basePairingListener.onPairingSuccess();
                }
            } else {
                if (basePairingListener != null) {
                    basePairingListener.onPairingFailed();
                }
            }
            pairingInProgress = false;
        }

        public boolean isPairingInProgress() {
            return pairingInProgress;
        }

        public void resetListener() {
            this.basePairingListener = null;
        }
    }

    interface BasePairingListener {
        void onPairingFailed();

        void onPairingSuccess();
    }

}
