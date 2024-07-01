package com.linkly.launcher.fragments;


import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.os.HandlerCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;

import com.linkly.launcher.BaseStation;
import com.linkly.launcher.ProgressViewModel;
import com.linkly.launcher.R;
import com.linkly.launcher.fragments.TestingConnectionFragment.ConnectionTestStatus.TestState;
import com.linkly.libui.IUIDisplay.String_id;
import com.linkly.libui.UI;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import timber.log.Timber;

public class TestingConnectionFragment extends Fragment implements View.OnClickListener {
    Button btnFinish;
    Button btnCancelSetup;
    Button btnCancel;
    Button btnCheckConfig;
    Button btnRetry;
    EditText editTextStartIp;
    EditText editTextEndIp;
    LinearLayout testButtonsLayout;// make this layout visible if it is not able to connect
    LinearLayout testCancelLayout;// default layout for buttons
    LinearLayout testFinishLayout;// make this layout visible if the test is completed
    LinearLayout testRetryLayout; // make this layout visible if it is not able to connect

    TextView testConfigStatus; //update this value once configuration is tested (both for success and failure)

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Handler mainThreadHandler = HandlerCompat.createAsync(Looper.getMainLooper());

    ImageView progressWifi;
    ImageView progressNetwork;
    ImageView progressHost;
    private ProgressViewModel model;
    private TestingConnectionViewModel testingConnectionViewModel;

    @Override
    public void onDestroyView() {
        btnFinish = null;
        btnCancelSetup = null;
        btnCancel = null;
        btnCheckConfig = null;
        btnRetry = null;
        editTextStartIp = null;
        editTextEndIp = null;
        testButtonsLayout = null;
        testCancelLayout = null;
        testFinishLayout = null;
        testRetryLayout = null;
        testConfigStatus = null;
        super.onDestroyView();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getActivity() != null) {
            model = new ViewModelProvider(getActivity()).get(ProgressViewModel.class);
            testingConnectionViewModel = new ViewModelProvider(getActivity()).get(TestingConnectionViewModel.class);

            testingConnectionViewModel.getConnectionTestStatus().observe(this, connectionTestStatus -> {
                if (connectionTestStatus != null) {
                    updateIcons(connectionTestStatus);
                }
            });
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_test_connection, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initialiseView(view);
    }

    @Override
    public void onResume() {
        super.onResume();
        ConnectionTestStatus connectionTestStatus = testingConnectionViewModel.getConnectionTestStatus().getValue();
        if (connectionTestStatus != null) {
            updateIcons(connectionTestStatus);
        }
    }

    @Override
    public void onDestroy() {
        Timber.e("Testing connection fragment destroyed");
        executorService.shutdownNow();

        // dismiss any progress before going to next page
        BaseStation.dismissProgress(0);
        super.onDestroy();
    }

    private void initialiseView(View view) {

        TextView textTestingConn = view.findViewById(R.id.text_test_connection);
        textTestingConn.setText(UI.getInstance().getPrompt(String_id.STR_TESTING_CONNECTION));

        TextView textConnToWIFI = view.findViewById(R.id.text_wifi_network);
        textConnToWIFI.setText(UI.getInstance().getPrompt(String_id.STR_CONNECTING_TO_WIFI_NW));

        TextView textConnToNW = view.findViewById(R.id.text_network);
        textConnToNW.setText(UI.getInstance().getPrompt(String_id.STR_CONNECTING_TO_NW));

        TextView textTestConnToHost = view.findViewById(R.id.text_host);
        textTestConnToHost.setText(UI.getInstance().getPrompt(String_id.STR_TESTING_CONN_TO_HOST));



        btnFinish = view.findViewById(R.id.btn_finish);
        btnFinish.setText(UI.getInstance().getPrompt(String_id.STR_FINISH));
        btnFinish.setOnClickListener(this);

        //Cancel button
        btnCancelSetup = view.findViewById(R.id.btn_cancel_setup);
        btnCancelSetup.setText(UI.getInstance().getPrompt(String_id.STR_CANCEL_SETUP));
        btnCancelSetup.setOnClickListener(this);

        btnCancel = view.findViewById(R.id.btn_cancel);
        btnCancel.setText(UI.getInstance().getPrompt(String_id.STR_CANCEL_SETUP));
        btnCancel.setOnClickListener(this);

        btnRetry = view.findViewById(R.id.btn_retry);
        btnRetry.setText(UI.getInstance().getPrompt(String_id.STR_RETRY_CONN));
        btnRetry.setOnClickListener(this);

        btnCheckConfig = view.findViewById(R.id.btn_check_config);
        btnCheckConfig.setText(UI.getInstance().getPrompt(String_id.STR_CHECK_CONFIG));
        btnCheckConfig.setOnClickListener(this);

        testConfigStatus = view.findViewById(R.id.text_config_status);
        testConfigStatus.setText(UI.getInstance().getPrompt(String_id.STR_CONFIG_TESTED_SUCCESS));

        editTextStartIp = view.findViewById(R.id.editText_dhcp_start_ip);
        editTextEndIp = view.findViewById(R.id.editText_dhcp_end_ip);
        progressWifi = view.findViewById(R.id.text_wifi_progress);
        progressNetwork = view.findViewById(R.id.text_network_progress);
        progressHost = view.findViewById(R.id.text_host_progress);

        testCancelLayout = view.findViewById(R.id.test_cancel_layout);
        testFinishLayout = view.findViewById(R.id.test_finish_layout);
        testButtonsLayout = view.findViewById(R.id.test_buttons_layout);
        testRetryLayout = view.findViewById(R.id.test_retry_layout);

        testingConnectionViewModel.startConnect(executorService, mainThreadHandler);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_finish || id == R.id.btn_cancel_setup || id == R.id.btn_cancel) {
            if (getActivity() != null) {
                getActivity().finishAfterTransition();
            }
        } else if (id == R.id.btn_retry) {
            testingConnectionViewModel.startConnect(executorService, mainThreadHandler);
            resetLayout();
        } else if (id == R.id.btn_check_config) {
            Fragment fragmentToLaunch = new LANSetupFragment();
            FragmentManager fragmentManager = getParentFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.settings_fragment, fragmentToLaunch).addToBackStack(null).commit();
            resetLayout();
            testingConnectionViewModel.reset();
        }
    }

    private void resetLayout() {
        testCancelLayout.setVisibility(VISIBLE);
        testConfigStatus.setVisibility(GONE);
        testRetryLayout.setVisibility(GONE);
        testButtonsLayout.setVisibility(GONE);
    }


    private Drawable getIcon(TestState value) {
        if (value == TestState.NOT_RUNNING)
            return ResourcesCompat.getDrawable(getResources(), R.drawable.hourglass, null);
        else if (value == TestState.SUCCESS)
            return ResourcesCompat.getDrawable(getResources(), R.drawable.matchfull, null );
        else if (value == TestState.FAILED)
            return ResourcesCompat.getDrawable(getResources(), R.drawable.matchfail, null );
        else // RUNNING
            return ResourcesCompat.getDrawable(getResources(), R.drawable.anim_progress_icon, null );
    }

    public void updateIcons(ConnectionTestStatus connectionTestStatus) {
        progressWifi.setImageDrawable(getIcon(connectionTestStatus.wifi));
        progressNetwork.setImageDrawable(getIcon(connectionTestStatus.network));
        progressHost.setImageDrawable(getIcon(connectionTestStatus.testConnect));
        model.setSelected("AP");

        if (connectionTestStatus.wifi == TestState.SUCCESS && connectionTestStatus.network == TestState.SUCCESS && connectionTestStatus.testConnect == TestState.SUCCESS) {
            model.setSelected("TEST");

            testConfigStatus.setVisibility(VISIBLE);
            testConfigStatus.setText("Configuration Tested Successfully");
            testConfigStatus.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.backgroundstylegreen, null));
            testConfigStatus.setTextColor(getResources().getColor(R.color.colorGreenDark, null));

            testCancelLayout.setVisibility(GONE);
            testFinishLayout.setVisibility(VISIBLE);
            testButtonsLayout.setVisibility(GONE);
            testRetryLayout.setVisibility(GONE);
        } else if (connectionTestStatus.wifi == TestState.FAILED || connectionTestStatus.network == TestState.FAILED || connectionTestStatus.testConnect == TestState.FAILED) {
            testConfigStatus.setVisibility(VISIBLE);
            testConfigStatus.setText("Unable To Connect");
            testConfigStatus.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.backgroundstylered, null));
            testConfigStatus.setTextColor(getResources().getColor(R.color.colorRed, null));

            testCancelLayout.setVisibility(GONE);
            testFinishLayout.setVisibility(GONE);
            testButtonsLayout.setVisibility(VISIBLE);
            testRetryLayout.setVisibility(VISIBLE);
        }
        //refresh Progress fragment
        Fragment fragmentToLaunch = new WiFiConfigProgressFragment();
        FragmentManager fragmentManager = getParentFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.current_progress_fragment, fragmentToLaunch).addToBackStack(null).commit();
    }

    public static class ConnectionTestStatus {

        private final TestState wifi;
        private final TestState network;
        private final TestState testConnect;

        public ConnectionTestStatus(TestState wifi, TestState network, TestState testConnect) {
            this.wifi = wifi;
            this.network = network;
            this.testConnect = testConnect;
        }

        public enum TestState {
            NOT_RUNNING, RUNNING, FAILED, SUCCESS
        }
    }
}
