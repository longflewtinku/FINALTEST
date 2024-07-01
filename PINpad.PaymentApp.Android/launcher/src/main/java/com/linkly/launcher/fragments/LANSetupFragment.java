package com.linkly.launcher.fragments;

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
import android.widget.ScrollView;
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
import com.linkly.launcher.Utils;
import com.linkly.libmal.global.util.Util;
import com.linkly.libui.IUIDisplay.String_id;
import com.linkly.libui.UI;

import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

import timber.log.Timber;

public class LANSetupFragment extends Fragment {
    Button btnApplySettings;
    Button btnCancelSetup;
    Button btnNext;
    EditText editTextIpAddress;
    EditText editTextSubnetMask;
    EditText editTextGateway;
    EditText editTextDNSServer;
    SwitchCompat switchStatic;
    SwitchCompat switchRndis;
    TextView textIPAddress;
    TextView textSubnetMask;
    TextView textGateway;
    TextView textDNSServer;
    ScrollView lanSettingsView;
    private boolean rndisEnabled;
    private boolean allowUpdateApplyState = true;
    private ProgressViewModel model;
    private static final String NETWORK_ID = "((25[0-5]|2[0-4]\\d|[0-1]\\d{2}|[1-9]\\d|\\d)\\.){0,3}";
    private static final String HOST_ID = "(25[0-5]|2[0-4]\\d|[0-1]\\d{2}|[1-9]\\d|\\d)";
    private static final Pattern PARTIAl_IP_ADDRESS = Pattern.compile("^" + NETWORK_ID + HOST_ID + "?$");

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private enum ApplyState { APPLIED, APPLYING, APPLY_FAILED, APPLY_PENDING }

    @Override
    public void onDestroyView() {
        btnApplySettings = null;
        btnCancelSetup = null;
        btnNext = null;
        editTextIpAddress = null;
        editTextSubnetMask = null;
        editTextGateway = null;
        editTextDNSServer = null;
        switchStatic = null;
        switchRndis = null;
        textIPAddress = null;
        textSubnetMask = null;
        textGateway = null;
        textDNSServer = null;
        lanSettingsView = null;
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
        return inflater.inflate(R.layout.fragment_lan_setup, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initialiseViews(view);
    }

    @Override
    public void onDestroy() {
        // dismiss any progress before going to next page
        BaseStation.dismissProgress(0);
        // try to stop any ongoing background work in the executor
        executor.shutdownNow();
        super.onDestroy();
    }

    private void initialiseViews(View view){
        btnApplySettings = view.findViewById(R.id.btn_apply);
        btnApplySettings.setText(UI.getInstance().getPrompt(String_id.STR_APPLY));
        btnApplySettings.setOnClickListener(view1 -> {
            //Call method to apply DHCP here
            updateApplyState(ApplyState.APPLYING);

            if (switchRndis.isChecked()) {
                executor.execute(this::performRndisSetup);
            } else {
                BaseStation.IpSetupData ipSetupData = new BaseStation.IpSetupData(
                        switchStatic.isChecked(),
                        editTextIpAddress.getText().toString(),
                        editTextSubnetMask.getText().toString(),
                        editTextGateway.getText().toString(),
                        editTextDNSServer.getText().toString(),
                        "8.8.8.8"); //Use known Google DNS as secondary
                executor.execute(() -> performLanSetup(ipSetupData));
            }
        });

        //Cancel button
        btnCancelSetup = view.findViewById(R.id.btn_cancel_setup);
        btnCancelSetup.setText(UI.getInstance().getPrompt(String_id.STR_CANCEL_SETUP));
        btnCancelSetup.setOnClickListener(view1 -> {
            if (getActivity() != null) {
                getActivity().finishAfterTransition();
            }
        });

        //Next button
        btnNext = view.findViewById(R.id.btn_next);
        btnNext.setText(UI.getInstance().getPrompt(String_id.STR_NEXT));
        btnNext.setOnClickListener(view1 -> {
            //Replace fragment with AP Setup fragment
            Fragment fragmentToLaunch = new APSetupFragment();
            FragmentManager fragmentManager = getParentFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.settings_fragment, fragmentToLaunch).addToBackStack(null).commit();
        });

        TextWatcher applyPendingTextWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                //Intentionally not handled
            }
            @Override public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(allowUpdateApplyState) {
                    updateApplyState(ApplyState.APPLY_PENDING);
                }
            }
            @Override public void afterTextChanged(Editable editable) {
                //Intentionally not handled
            }
        };

        // hide lan settings by default
        lanSettingsView = view.findViewById(R.id.scroll_view);
        editTextIpAddress = view.findViewById(R.id.editText_ip_address);
        editTextIpAddress.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s,int start,int count,int after) {
                //Intentionally not handled
            }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(allowUpdateApplyState) {
                    updateApplyState(ApplyState.APPLY_PENDING);
                }
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
        });
        editTextSubnetMask = view.findViewById(R.id.editText_subnet_mask);
        editTextSubnetMask.addTextChangedListener(applyPendingTextWatcher);
        editTextGateway = view.findViewById(R.id.editText_gateway);
        editTextGateway.addTextChangedListener(applyPendingTextWatcher);
        editTextDNSServer = view.findViewById(R.id.editText_dns_server);
        editTextDNSServer.addTextChangedListener(applyPendingTextWatcher);

        //Initialise editText
        TextView textLanSetup = view.findViewById(R.id.text_lan_setup);
        textLanSetup.setText(UI.getInstance().getPrompt(String_id.STR_LAN_SETUP));

        textIPAddress = view.findViewById(R.id.text_ip_address);
        textIPAddress.setText(UI.getInstance().getPrompt(String_id.STR_IP_ADDRESS));

        textSubnetMask = view.findViewById(R.id.text_subnet_mask);
        textSubnetMask.setText(UI.getInstance().getPrompt(String_id.STR_SUBNET_MASK));

        textGateway = view.findViewById(R.id.text_gateway);
        textGateway.setText(UI.getInstance().getPrompt(String_id.STR_GATEWAY));

        textDNSServer = view.findViewById(R.id.text_dns_server);
        textDNSServer.setText(UI.getInstance().getPrompt(String_id.STR_DNS_SERVER));

        //Initialise switch values

        switchStatic = view.findViewById(R.id.switch_static);
        switchRndis = view.findViewById(R.id.rndis_mode);
        updateValuesFromBaseStation();

        switchStatic.setOnCheckedChangeListener((buttonView, isChecked) -> setStaticIpDetails(switchRndis.isChecked(), isChecked));
        switchRndis.setOnCheckedChangeListener((buttonView, isChecked) -> updateStaticSwitchState(isChecked));

        updateApplyState(ApplyState.APPLIED);
    }

    private void setStaticIpDetails(boolean isRndis, boolean isStatic) {
        String prefix = isRndis ? "usb" : "lan";
        String addr = BaseStation.getInstance().getExInfo(prefix + "Addr");
        String mask = BaseStation.getInstance().getExInfo(prefix + "Mask");
        String gateWay = BaseStation.getInstance().getExInfo(prefix + "GateWay");
        String dns1 = BaseStation.getInstance().getExInfo(prefix + "Dns1");

        boolean isEditable = !isRndis && isStatic;
        if(isEditable) {
            //Set reasonable defaults
            if (Util.isNullOrEmpty(addr))
                addr = "192.168.1.44";
            if (Util.isNullOrEmpty(mask))
                mask = "255.255.255.0";
            if (Util.isNullOrEmpty(gateWay))
                gateWay = "192.168.1.1";
            if (Util.isNullOrEmpty(dns1))
                dns1 = "192.168.1.1";
        }

        //Prevent multiple calls to updating the apply state
        allowUpdateApplyState = false;
        editTextIpAddress.setText(addr);
        editTextSubnetMask.setText(mask);
        editTextGateway.setText(gateWay);
        editTextDNSServer.setText(dns1);
        allowUpdateApplyState = true;

        String_id text = isStatic ? String_id.STR_CONFIGURE_STATIC_IP : String_id.STR_CONFIGURE_DHCP;
        switchStatic.setText(UI.getInstance().getPrompt(text));

        updateApplyState(ApplyState.APPLY_PENDING);
    }

    private void updateValuesFromBaseStation() {
        // netRoute ETH indicates base is in ethernet mode. USB indicates tcp over usb/RNDIS mode
        String netRoute = BaseStation.getInstance().getExInfo("netRoute");

        //Update RNDIS switch value
        rndisEnabled = "USB".equals(netRoute);
        switchRndis.setChecked("USB".equals(netRoute));

        //Update static switch value
        updateStaticSwitchState(rndisEnabled);
    }

    private void updateStaticSwitchState(boolean isRndis) {
        boolean isStatic = false;

        String_id text = isRndis ? String_id.STR_RNDIS_MODE : String_id.STR_ETH_MODE;
        switchRndis.setText(UI.getInstance().getPrompt(text));

        if(isRndis) {
            //RNDIS is DHCP only
            switchStatic.setChecked(false);
        } else {
            if(!rndisEnabled) {
                //Get source of truth from the base - but only when in ethernet mode and base values are valid
                String dhcp = BaseStation.getInstance().getExInfo("lanDHCP");
                isStatic = "0".equals(dhcp);
            }
            switchStatic.setChecked(isStatic);
        }

        setStaticIpDetails(isRndis, isStatic);
    }

    private void setControlEnabled(View v, boolean enabled) {
        v.setEnabled(enabled);
        v.setAlpha(enabled ? 1.0f : 0.5f);
    }

    private void updateApplyState(ApplyState state) {
        model.setSelected("PAIRED");

        String_id applyButtonTextId;
        int applyButtonColorId;
        int nextButtonVisible = INVISIBLE;
        boolean editable = true;
        boolean refresh = true;
        switch (state) {
            default:
            case APPLIED:
                applyButtonTextId = String_id.STR_APPLIED;
                applyButtonColorId = R.color.colorGreenDark;
                nextButtonVisible = VISIBLE;
                break;
            case APPLY_PENDING:
                applyButtonTextId = String_id.STR_APPLY;
                applyButtonColorId = R.color.colorBlue;
                break;
            case APPLY_FAILED:
                applyButtonTextId = String_id.STR_APPLY;
                applyButtonColorId = R.color.colorRed;
                break;
            case APPLYING:
                applyButtonTextId = String_id.STR_APPLYING;
                applyButtonColorId = android.R.color.darker_gray;
                editable = false;
                refresh = false;
                break;
        }

        btnApplySettings.setText(UI.getInstance().getPrompt(applyButtonTextId));
        if (getActivity() != null) {
            btnApplySettings.setBackgroundColor(ContextCompat.getColor(getActivity(), applyButtonColorId));
        }
        btnNext.setVisibility(nextButtonVisible);

        boolean isRndis = switchRndis.isChecked();
        boolean isStatic = switchStatic.isChecked();

        //editText controls only enabled when in Static mode
        boolean editTextEnabled = editable && isStatic;
        setControlEnabled(editTextIpAddress, editTextEnabled);
        setControlEnabled(editTextSubnetMask, editTextEnabled);
        setControlEnabled(editTextGateway, editTextEnabled);
        setControlEnabled(editTextDNSServer, editTextEnabled);
        setControlEnabled(textIPAddress, editTextEnabled);
        setControlEnabled(textSubnetMask, editTextEnabled);
        setControlEnabled(textDNSServer, editTextEnabled);
        setControlEnabled(textGateway, editTextEnabled);
        //Static switch not available when in RNDIS
        setControlEnabled(switchStatic, editable && !isRndis);
        //RNDIS switch available as long as settings aren't being applied
        setControlEnabled(switchRndis, editable);

        if (refresh) {
            //refresh Progress fragment
            Fragment fragmentToLaunch = new WiFiConfigProgressFragment();
            FragmentManager fragmentManager = getParentFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.current_progress_fragment, fragmentToLaunch).addToBackStack(null).commit();
        }
    }

    @WorkerThread
    private void performRndisSetup() {
        final boolean success = changeBaseRndisSetting(true);
        runOnUiThread(() -> {
            if(success) {
                updateValuesFromBaseStation();
                updateApplyState(ApplyState.APPLIED);
            } else {
                updateApplyState(ApplyState.APPLY_FAILED);
            }
        });
    }

    @WorkerThread
    private void performLanSetup(BaseStation.IpSetupData ipSetupData) {
        boolean success = true;
        // disable rndis if required
        if (rndisEnabled) {
            success = changeBaseRndisSetting(false);
        }

        if (success) {
            success = applyLanSettings(ipSetupData);
        }

        boolean finalSuccess = success;
        runOnUiThread(() -> {
            if(finalSuccess) {
                updateValuesFromBaseStation();
                updateApplyState(ApplyState.APPLIED);
            } else {
                updateApplyState(ApplyState.APPLY_FAILED);
            }
        });
    }

    @WorkerThread
    private void runOnUiThread(Runnable runnable) {
        if (getActivity() != null && !getActivity().isFinishing() && isResumed()) {
            getActivity().runOnUiThread(runnable);
        }
    }

    @WorkerThread
    private boolean applyLanSettings(BaseStation.IpSetupData ipSetupData) {
        boolean success;

        BaseStation.displayProgress( "LAN Setup", "Configuring " + (ipSetupData.isStatic()?"Static IP":"DHCP") );

        if(!ipSetupData.isStatic()){
            success = BaseStation.getInstance().setDhcp();
            if (!success) {
                runOnUiThread(() -> BaseStation.displayError(BaseStation.ERROR_DIALOG_NAME, "Failed to enable LAN DHCP"));
            } else {
                BaseStation.dismissProgress();
            }
        } else {
            success = BaseStation.getInstance().setStatic(ipSetupData);
            if (!success) {
                runOnUiThread(() -> BaseStation.displayError(BaseStation.ERROR_DIALOG_NAME, "Failed to Set LAN Static\n" +
                        ipSetupData.getIp() + ":" +
                        ipSetupData.getNetmask() + "\n" +
                        ipSetupData.getGateway() + ":" +
                        ipSetupData.getPreferredDns()));
            } else {
                BaseStation.dismissProgress();
            }
        }
        return success;
    }

    @WorkerThread
    private boolean changeBaseRndisSetting( boolean enable ) {
        String resultString = "command = %s, result = %b, display %s, errtext %s, data %s";

        // 1. Ensure A920 is on the base
        Utils.CmdResult cmdResult = Utils.sendCmd(0x01, 0x01);
        Timber.i(resultString, "GetSummary", cmdResult.result, cmdResult.display, cmdResult.errString, Util.byteArrayToHexString(cmdResult.data) );
        long start = System.currentTimeMillis();
        while( !cmdResult.result ) {
            Utils.CmdResult finalCmdResult = cmdResult;
            runOnUiThread(() -> BaseStation.displayProgress(BaseStation.RNDIS_SETUP_DIALOG_TITLE, "Please place terminal on base to continue\n" + finalCmdResult.errString));

            cmdResult = Utils.sendCmd(0x01, 0x01);
            Timber.i(resultString, "GetSummary", cmdResult.result, cmdResult.display, cmdResult.errString, Util.byteArrayToHexString(cmdResult.data));
            Util.Sleep(1000);

            // Executor is shutdown if the fragment is destroyed, no need to continue with the process
            if (executor.isShutdown()) {
                return false;
            }
            if ((System.currentTimeMillis() - start) > 30000) {
                BaseStation.displayError(BaseStation.RNDIS_SETUP_DIALOG_TITLE, "Error, not returned to base. Base must be powered");
                return false;
            }
        }

        // 2. get all info about the base
        cmdResult = Utils.sendCmd(0x01, 0x02);
        Timber.i(resultString, "GetInfo", cmdResult.result, cmdResult.display, cmdResult.errString, Util.byteArrayToHexString(cmdResult.data));
        if( cmdResult.result ) {
            Timber.i("data string %s", Arrays.toString(cmdResult.data));
        }

        // 3. configure RNDIS on or off
        runOnUiThread(() -> BaseStation.displayProgress(BaseStation.RNDIS_SETUP_DIALOG_TITLE, (enable ? "Enabling" : "Disabling") + " RNDIS..."));
        cmdResult = Utils.sendCmd(0x04, enable?0x01:0x02);
        Timber.i(resultString, enable ? "Enable RNDIS" : "Disable RNDIS", cmdResult.result, cmdResult.display, cmdResult.errString, Util.byteArrayToHexString(cmdResult.data));

        // 4. reconnect bluetooth - drop, then connect again
        BaseStation.getInstance().reconnect();

        // 5. display result
        Utils.CmdResult finalCmdResult1 = cmdResult;
        runOnUiThread(() -> BaseStation.displayError(BaseStation.RNDIS_SETUP_DIALOG_TITLE, (enable ? "Enable" : "Disable") + " RNDIS " + (finalCmdResult1.result ? "success!" : "FAILURE\n" + finalCmdResult1.errString)));

        return cmdResult.result;
    }

}
