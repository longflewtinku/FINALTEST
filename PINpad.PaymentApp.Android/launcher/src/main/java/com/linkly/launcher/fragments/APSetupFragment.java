package com.linkly.launcher.fragments;

import static android.view.View.GONE;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;

import com.linkly.launcher.BaseStation;
import com.linkly.launcher.ProgressViewModel;
import com.linkly.launcher.R;
import com.linkly.libmal.global.util.Util;
import com.linkly.libui.IUIDisplay.String_id;
import com.linkly.libui.UI;
import com.pax.baselink.api.BDeviceInfo;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

import timber.log.Timber;

public class APSetupFragment extends Fragment implements View.OnClickListener, TextWatcher {
    Button btnApplyAPSettings;
    Button btnCancelSetup;
    Button btnNext;
    EditText editTextSSID;
    EditText editTextPassword;
    EditText editTextStartIp;
    EditText editTextEndIp;
    LinearLayout layoutDhcpStartIp;
    LinearLayout layoutDhcpEndIp;
    SwitchCompat switchDHCP;


    private ProgressViewModel model;
    private static final String NETWORK_ID = "((25[0-5]|2[0-4]\\d|[0-1]\\d{2}|[1-9]\\d|\\d)\\.){0,3}";
    private static final String HOST_ID = "(25[0-5]|2[0-4]\\d|[0-1]\\d{2}|[1-9]\\d|\\d)";
    private static final Pattern PARTIAl_IP_ADDRESS = Pattern.compile("^" + NETWORK_ID + HOST_ID + "?$");

    private ExecutorService executor = Executors.newSingleThreadExecutor();

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
        return inflater.inflate(R.layout.fragment_ap_setup, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //Initialise view
        initialiseView(view);

        // TODO: check current settings. if settings are already applied, fill them and set applied = true so 'next' button is visible
        BaseStation.debugExInfo();



    }

    @Override
    public void onDestroyView() {
        btnApplyAPSettings = null;
        btnCancelSetup = null;
        btnNext = null;
        editTextSSID = null;
        editTextPassword = null;
        editTextStartIp = null;
        editTextEndIp = null;
        layoutDhcpStartIp = null;
        layoutDhcpEndIp = null;
        switchDHCP = null;

        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        // dismiss any progress before going to next page
        BaseStation.dismissProgress(0);
        // try to stop any ongoing background work in the executor
        executor.shutdownNow();
        super.onDestroy();
    }

    private void initialiseView(View view) {
        //TextViews
        TextView textAPSetup = view.findViewById(R.id.text_ap_setup);
        textAPSetup.setText(UI.getInstance().getPrompt(String_id.STR_AP_SETUP));

        TextView textSSID = view.findViewById(R.id.text_ssid);
        textSSID.setText(UI.getInstance().getPrompt(String_id.STR_SSID));

        TextView textPassword = view.findViewById(R.id.text_password);
        textPassword.setText(UI.getInstance().getPrompt(String_id.STR_AP_PASSWORD));

        TextView textDhcpStartIP = view.findViewById(R.id.text_dhcp_start_ip);
        textDhcpStartIP.setText(UI.getInstance().getPrompt(String_id.STR_DHCP_START_IP));

        TextView textDhcpEndIP = view.findViewById(R.id.text_dhcp_end_ip);
        textDhcpEndIP.setText(UI.getInstance().getPrompt(String_id.STR_DHCP_END_IP));

        btnApplyAPSettings = view.findViewById(R.id.btn_apply_ap);
        btnApplyAPSettings.setText(UI.getInstance().getPrompt(String_id.STR_APPLY));
        btnApplyAPSettings.setOnClickListener(this);
        //Cancel button
        btnCancelSetup = view.findViewById(R.id.btn_cancel_setup);
        btnCancelSetup.setText(UI.getInstance().getPrompt(String_id.STR_CANCEL_SETUP));
        btnCancelSetup.setOnClickListener(this);

        //Next button
        btnNext = view.findViewById(R.id.btn_next);
        btnNext.setText(UI.getInstance().getPrompt(String_id.STR_NEXT));
        btnNext.setOnClickListener(this);
        btnNext.setVisibility(INVISIBLE);

        editTextSSID = view.findViewById(R.id.editText_ssid);
        editTextSSID.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                setApplied(false);
            }
        });

        editTextPassword = view.findViewById(R.id.editText_password);
        editTextPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                setApplied(false);
            }
        });

        editTextStartIp = view.findViewById(R.id.editText_dhcp_start_ip);
        editTextStartIp.addTextChangedListener(this);
        editTextEndIp = view.findViewById(R.id.editText_dhcp_end_ip);
        editTextEndIp.addTextChangedListener(this);

        layoutDhcpStartIp= view.findViewById(R.id.layout_dhcp_start_ip);
        layoutDhcpEndIp= view.findViewById(R.id.layout_dhcp_end_ip);

        switchDHCP = view.findViewById(R.id.switch_static);
        switchDHCP.setText(UI.getInstance().getPrompt(String_id.STR_ENABLE_WIFI_BASE_DHCP));

        String wifiDhcp = BaseStation.getInstance().getExInfo("wifiDHCP");
        switchDHCP.setChecked("1".equals(wifiDhcp));
        if( switchDHCP.isChecked() ) {
            enableDhcpDetails();
        } else {
            disableDhcpDetails();
        }

        BDeviceInfo devInfo = BaseStation.getInstance().getCurrentBaseStationInfo();

        // debug
        Timber.i("devInfo object : %s", (devInfo == null) ? "null" : devInfo.toString());

        // if settings are already present, then set applied = true, so user can click 'next' to proceed
        setApplied(false);
        boolean cfgSsidSet = false;
        boolean cfgPwdSet = false;

        if (devInfo != null && !Util.isNullOrEmpty(devInfo.getWifiSsid())) {
            cfgSsidSet = true;
            editTextSSID.setText(devInfo.getWifiSsid());
        }

        if (devInfo != null && !Util.isNullOrEmpty(devInfo.getWifiPasswd())) {
            cfgPwdSet = true;
            editTextPassword.setText(devInfo.getWifiPasswd());
        }

        if (cfgSsidSet && cfgPwdSet) {
            setApplied(true);
        }

        switchDHCP.setOnCheckedChangeListener((buttonView, isChecked) -> {
            switchDHCP.setChecked(true);
            enableDhcpDetails();
            BaseStation.getInstance();
            BaseStation.displayError(BaseStation.ERROR_DIALOG_NAME, "Static mode not supported. Using DHCP");

        });

    }

    private void disableDhcpDetails() {
        editTextStartIp.setEnabled(false);
        editTextStartIp.setText("");
        editTextStartIp.setAlpha(.3f);
        editTextEndIp.setEnabled(false);
        editTextEndIp.setAlpha(.3f);
        editTextEndIp.setText("");
        btnApplyAPSettings.requestFocus();
        layoutDhcpStartIp.setVisibility(GONE);
        layoutDhcpEndIp.setVisibility(GONE);
    }

    private void enableDhcpDetails(){
        BDeviceInfo devInfo = BaseStation.getInstance().getCurrentBaseStationInfo();

        String ipStart = null;
        String ipEnd = null;
        if (devInfo != null) {
            ipStart = devInfo.getApIpPoolStart();
            ipEnd = devInfo.getApIpPoolEnd();
        }
        if (ipStart == null) {
            ipStart = "192.168.2.1";
        }
        if (ipEnd == null) {
            ipEnd = "192.168.2.254";
        }

        editTextStartIp.setText(ipStart);
        editTextStartIp.setEnabled(true);
        editTextStartIp.setAlpha(1.0f);
        editTextEndIp.setText(ipEnd);
        editTextEndIp.setEnabled(true);
        editTextEndIp.setAlpha(1.0f);
        layoutDhcpStartIp.setVisibility(VISIBLE);
        layoutDhcpEndIp.setVisibility(VISIBLE);
    }

    private void setApplied(boolean applied) {
        if (this.getActivity() == null || this.getActivity().isFinishing())
            return;

        model.setSelected("LAN");

        editTextStartIp.setEnabled(true);
        editTextEndIp.setEnabled(true);
        switchDHCP.setEnabled(true);
        editTextSSID.setEnabled(true);
        editTextPassword.setEnabled(true);

        if (applied) {
            btnApplyAPSettings.setText(UI.getInstance().getPrompt(String_id.STR_APPLIED));
            btnApplyAPSettings.setBackgroundColor( ContextCompat.getColor( this.getActivity(), R.color.colorGreenDark));
            btnNext.setVisibility(VISIBLE);
        } else {
            btnApplyAPSettings.setText(UI.getInstance().getPrompt(String_id.STR_APPLY));
            btnApplyAPSettings.setBackgroundColor(ContextCompat.getColor( this.getActivity(), android.R.color.darker_gray));
            btnNext.setVisibility(INVISIBLE);
        }
        //refresh Progress fragment
        Fragment fragmentToLaunch = new WiFiConfigProgressFragment();
        FragmentManager fragmentManager = getParentFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.current_progress_fragment, fragmentToLaunch).addToBackStack(null).commit();
    }

    private void setApplying() {
        btnApplyAPSettings.setText(UI.getInstance().getPrompt(String_id.STR_APPLYING));
        editTextStartIp.setEnabled(false);
        editTextEndIp.setEnabled(false);
        switchDHCP.setEnabled(false);
        editTextSSID.setEnabled(false);
        editTextPassword.setEnabled(false);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_apply_ap) {//Call method to apply DHCP here
            applyAPSettings();
        } else if (id == R.id.btn_cancel_setup) {
            if (getActivity() != null) {
                getActivity().finishAfterTransition();
            }
        } else if (id == R.id.btn_next) {//Replace fragment with AP DHCP Setup fragment
            Fragment fragmentToLaunch = new TestingConnectionFragment();
            FragmentManager fragmentManager = getParentFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.settings_fragment, fragmentToLaunch).addToBackStack(null).commit();
        }
    }

    private void applyAPSettings() {

        String ssid = editTextSSID.getText().toString();
        String pwd = editTextPassword.getText().toString();
        String startIp = editTextStartIp.getText().toString();
        String endIp = editTextEndIp.getText().toString();

        BaseStation.getInstance();
        if (ssid.length() < 8) {
            BaseStation.displayError(BaseStation.ERROR_DIALOG_NAME, "SSID too short (min 8)");
            return;
        }
        if (pwd.length() < 8) {
            BaseStation.displayError(BaseStation.ERROR_DIALOG_NAME, "Password too short (min 8)");
            return;
        }

        if (switchDHCP.isChecked()) {
            if (startIp.length() < 7) {
                BaseStation.displayError(BaseStation.ERROR_DIALOG_NAME, "Start IP too short");
                return;
            }
            if (endIp.length() < 7) {
                BaseStation.displayError(BaseStation.ERROR_DIALOG_NAME, "End IP too short");
                return;
            }
        }

        setApplying();
        // always enable DHCP. causes issues in RNDIS mode if this is false. 99.99% users will prefer DHCP here
        BaseStation.WifiApSetupData wifiApSetupData = new BaseStation.WifiApSetupData(ssid, pwd, /*switchDHCP.isChecked()*/true, startIp, endIp);
        executor.execute(() -> performApSetup(wifiApSetupData));
    }

    @WorkerThread
    private void performApSetup(BaseStation.WifiApSetupData wifiApSetupData) {
        boolean result = BaseStation.getInstance().wifiSetAPManualStart(wifiApSetupData);
        runOnUiThread(() -> {
            if (!result) {
                BaseStation.displayError(BaseStation.ERROR_DIALOG_NAME, "Failed to configure AP");
            }
            setApplied(result);
        });
    }

    @WorkerThread
    private void runOnUiThread(Runnable runnable) {
        if (getActivity() != null && !getActivity().isFinishing() && isResumed()) {
            getActivity().runOnUiThread(runnable);
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }
    private String mPreviousText = "";
    @Override
    public void afterTextChanged(Editable s) {
        if(PARTIAl_IP_ADDRESS.matcher(s).matches()) {
            mPreviousText = s.toString();
        } else {
            s.replace(0, s.length(), mPreviousText);
        }
    }

}
