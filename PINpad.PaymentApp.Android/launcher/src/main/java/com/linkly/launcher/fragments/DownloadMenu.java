package com.linkly.launcher.fragments;

import static com.linkly.launcher.ServiceFrontEnd.ACCESSCODE_ADMIN_MENU;
import static com.linkly.launcher.ServiceFrontEnd.ACCESSCODE_EXIT;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.linkly.launcher.Activate;
import com.linkly.launcher.BR;
import com.linkly.launcher.BrandingConfig;
import com.linkly.launcher.InputActivity;
import com.linkly.launcher.LockedDownActivity;
import com.linkly.launcher.R;
import com.linkly.launcher.ServiceFrontEnd;
import com.linkly.launcher.UnattendedServiceModeAuthorizationHost;
import com.linkly.launcher.access.AccessCodeCheckCallbacks;
import com.linkly.launcher.databinding.FragmentDownloadMenuBinding;
import com.linkly.launcher.viewmodels.DownloadMenuViewModel;
import com.linkly.libconfig.MalConfig;
import com.linkly.libconfig.ProfileCfg;
import com.linkly.libmal.MalFactory;
import com.linkly.libmal.global.platform.EFTPlatform;
import com.linkly.libmal.global.platform.Platform;
import com.linkly.libmal.global.util.DisplayKiosk;
import com.linkly.libmal.global.util.Util;
import com.linkly.libpositive.messages.IMessages;
import com.linkly.libpositivesvc.downloader.DownloadDirector;
import com.linkly.libui.IUIDisplay.String_id;
import com.linkly.libui.UI;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import timber.log.Timber;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link DownloadMenu} OnFragmentInteractionListener interface
 * to handle interaction events.
 * Use the {@link DownloadMenu#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DownloadMenu extends Fragment implements View.OnClickListener, AccessCodeCheckCallbacks {

    private static final String TAG = "launcher.Download";

    private String rbtText;
    private DownloadMenuViewModel viewModel;
    private FragmentDownloadMenuBinding binding;
    private ActivityResultLauncher<Intent> adHocAdminAuthLauncher;
    private ActivityResultLauncher<Intent> adHocExitAuthLauncher;

    public DownloadMenu() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment DownloadMenu.
     */
    public static DownloadMenu newInstance() {
        return new DownloadMenu();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LocalBroadcastManager.getInstance(getActivity().getApplicationContext()).registerReceiver(mMessageReceiver, new IntentFilter(IMessages.APP_SVC_PROGRESS_DIALOG));

        viewModel = new ViewModelProvider(this).get(DownloadMenuViewModel.class);

        adHocAdminAuthLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                InputActivity.buildAuthActivityResultCallback(
                        ACCESSCODE_ADMIN_MENU, this));
        adHocExitAuthLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                InputActivity.buildAuthActivityResultCallback(
                        ACCESSCODE_EXIT, this));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(getActivity().getApplicationContext()).unregisterReceiver(mMessageReceiver);
    }

    protected BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (IMessages.APP_SVC_PROGRESS_DIALOG.equals(intent.getAction())) {
                if (intent.getBooleanExtra("PROGRESSDISMISS", false))
                    refreshUI(getView());
            }
        }
    };

    private void refreshUI(View v) {
        if (v == null)
            return;
        SharedPreferences sharedPref = getContext().getApplicationContext().getSharedPreferences("com.linkly.service.cfg", Context.MODE_PRIVATE);
        if (sharedPref == null)
            return;

        rbtText= getResources().getString(R.string.LAST_REBOOT_TIME);
        Date date = new Date(System.currentTimeMillis() - SystemClock.elapsedRealtime());
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault() );
        String dateString = sdf.format(date);
        rbtText += dateString;
        binding.txtRebootTime.setText(rbtText);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = DataBindingUtil.inflate( inflater, R.layout.fragment_download_menu, container, false );
        binding.setViewModel(viewModel);
        binding.setLifecycleOwner(getViewLifecycleOwner());

        View view = binding.getRoot();

        /*Add Button Listeners*/
        binding.runUpdate.setOnClickListener(this);
        binding.runExitKiosk.setOnClickListener(this);

        if (!(ProfileCfg.getInstance() != null && ProfileCfg.getInstance().getCustomerName() != null && ProfileCfg.getInstance().getCustomerName().contains("Opto"))) {
            binding.runActivate.setVisibility(View.GONE);
            binding.runDeactivate.setVisibility(View.GONE);
            binding.runForceUpdate.setVisibility(View.GONE);
            binding.deactivateSpace.setVisibility(View.GONE);
        }

        return view;
    }

    private void updateBranding() {
        Context context = binding.getRoot().getContext();
        int buttonTextColor = BrandingConfig.getBrandDisplayButtonTextColourOrDefault();
        int buttonColor = BrandingConfig.getBrandDisplayButtonColourOrDefault(context.getColor(R.color.color_linkly_primary));

        updateButtonColors( binding.runUpdate, buttonTextColor, buttonColor);
        updateButtonColors( binding.runDeactivate, buttonTextColor, buttonColor);
        updateButtonColors( binding.runForceUpdate, buttonTextColor, buttonColor);
        updateButtonColors( binding.runActivate, buttonTextColor, buttonColor);
        updateButtonColors( binding.runExitKiosk, buttonTextColor, buttonColor);
    }

    private void updateButtonColors(Button b, int textColor, int backgroundColor) {
        b.setTextColor(textColor);
        GradientDrawable drawable = (GradientDrawable) b.getBackground();
        drawable.setColor(backgroundColor);
        b.setBackground(drawable);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.setVariable(BR.viewModel, viewModel);
        binding.executePendingBindings();

        final Observer<Integer> buttonBackColourObserver = newValue -> updateBranding();
        viewModel.getBrandDisplayPrimaryColour().observe(getViewLifecycleOwner(), buttonBackColourObserver);

        final Observer<Integer> buttonTextColourObserver = newValue -> updateBranding();
        viewModel.getBrandDisplayButtonTextColour().observe(getViewLifecycleOwner(), buttonTextColourObserver);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        refreshUI(getView());
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshUI(getView());
    }

    @Override
    public void onDestroyView() {
        binding = null;
        super.onDestroyView();
    }

    @Override
    public void onAttach( @NonNull Context context) {
        super.onAttach(context);

    }

    @Override
    public void onDetach() {
        super.onDetach();

    }

    private static void resetPreferredLauncherAndOpenChooser(Context context) {
        try {
            if (EFTPlatform.getTerminalModel() == Platform.TerminalModel.A80) {
                Intent intent2 = new Intent();
                intent2.setAction(Intent.ACTION_MAIN);
                intent2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                intent2.addCategory(Intent.CATEGORY_DEFAULT);
                context.startActivity(intent2);
            } else {
                Intent intent = new Intent();
                intent.setClassName("com.android.launcher3", "com.android.launcher3.Launcher");
                context.startActivity(intent);
            }
        } catch (Exception ex) {
            // On the older terminals we will be presented with the list, where we need to select "Launcher"
            Intent intent2 = new Intent();
            intent2.setAction(Intent.ACTION_MAIN);
            intent2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
            intent2.addCategory(Intent.CATEGORY_DEFAULT);
            context.startActivity(intent2);
        }
    }

    @Override
    public void onAdminMenuGranted() {
        Timber.e("onAdminGranted...");
        throw new UnsupportedOperationException("Not implemented!");
    }

    @Override
    public void onExitLauncherGranted() {
        Timber.e("onExitKioskModeGranted...");
        DisplayKiosk.getInstance().exitKioskMode(getContext());
        resetPreferredLauncherAndOpenChooser(getContext());
    }

    @Override
    public void onEnterUnattendedServiceModeModeGranted() {
        Timber.e("onExitUnattendedModeGranted...");
        throw new UnsupportedOperationException("Not implemented!");
    }

    @Override
    public void onAccessDenied() {
        Timber.e("onExitUnattendedModeGranted...");
        // no special needs yet
    }

    @Override
    public void onAuthCancellation() {
        Timber.d("onAuthCancellation...");
        // no special needs yet
    }

    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.runUpdate: {
                System.gc();
                Util.DisplayMemoryUsage(UI.getInstance().getPrompt(String_id.STR_RUN_UPDATE));
                Timber.i("Run Update");
                if (MalConfig.getInstance().getDownloadCfg().isValidCfg()) {
                    UI.getInstance().postUINotification(UI.getInstance().getPrompt(String_id.STR_DOWNLOAD_MANAGER), UI.getInstance().getPrompt(String_id.STR_CHECK_UPDATES));
                    DownloadDirector.update(getActivity(), false);

                } else {
                    //Please Wait Toast
                    Toast.makeText(getActivity(), UI.getInstance().getPrompt(String_id.STR_CONFIG_NOT_AVAILABLE_TRY_AGAIN), Toast.LENGTH_SHORT).show();
                }
                break;
            }
            case R.id.runForceUpdate: {
                System.gc();
                Util.DisplayMemoryUsage(UI.getInstance().getPrompt(String_id.STR_FORCE_UPDATE));
                Timber.i("Force Update");
                if (MalConfig.getInstance().getDownloadCfg().isValidCfg()) {
                    UI.getInstance().postUINotification(UI.getInstance().getPrompt(String_id.STR_DOWNLOAD_MANAGER), UI.getInstance().getPrompt(String_id.STR_FORCE_CHECK_UPDATES));
                    DownloadDirector.forceUpdate(getActivity());

                } else {
                    /*Please Wait Toast*/
                    Toast.makeText(getActivity(), UI.getInstance().getPrompt(String_id.STR_CONFIG_NOT_AVAILABLE_TRY_AGAIN), Toast.LENGTH_SHORT).show();
                }
                break;
            }
            case R.id.runActivate: {
                Timber.i("Activate Terminal");
               Intent i = new Intent(MalFactory.getInstance().getMalContext(), Activate.class);
               i.putExtra("ENV", "TEST");
                startActivity(i);

                break;
            }
            case R.id.runDeactivate: {
                Timber.i("Activate Terminal");
                DownloadDirector.deactivateDevice(getActivity());
                break;
            }
            case R.id.runExitKiosk: {
                Timber.i("Exit Launcher");
                passwordProtection();
                break;
            }
        }
    }


    void passwordProtection() {
        InputActivity.accessCodeQuestionableProtection(
                getContext(),
                ServiceFrontEnd.ACCESSCODE_EXIT,
                adHocAdminAuthLauncher,
                adHocExitAuthLauncher
        );
    }

}
