package com.linkly.launcher;

import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.linkly.launcher.applications.AppDetail;
import com.linkly.launcher.applications.AppMgr;
import com.linkly.launcher.databinding.FragmentInfoScreenBinding;
import com.linkly.launcher.viewmodels.InfoScreenViewModel;
import com.linkly.libconfig.MalConfig;
import com.linkly.libpositivesvc.downloader.DownloadDirector;
import com.linkly.libui.IUIDisplay.String_id;
import com.linkly.libui.UI;

import java.util.List;
import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;

import timber.log.Timber;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link InfoScreen OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link InfoScreen#newInstance} factory method to
 * create an instance of this fragment.
 */
public class InfoScreen extends Fragment {
    private static final long AUTO_LAUNCH_CHECK_INITIAL_DELAY_MSEC = 20000;
    private static final long AUTO_LAUNCH_CHECK_BACKOFF_DELAY_MSEC = 5000;
    private static final long MILLISECONDS_SINCE_BOOT_THRESHOLD = 90000;
    private String verText;
    private static final int BUTTON_MARGIN = 10;
    private Handler handler;
    private Runnable runnable;
    private InfoScreenViewModel viewModel;
    private FragmentInfoScreenBinding binding;
    private static int autoLaunchAttemptNo = 0;

    public InfoScreen() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment InfoScreen.
     */
    public static InfoScreen newInstance() {
        return new InfoScreen();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(InfoScreenViewModel.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = DataBindingUtil.inflate( inflater, R.layout.fragment_info_screen, container, false );
        binding.setLifecycleOwner(getViewLifecycleOwner());
        View view = binding.getRoot();
        binding.setViewModel(viewModel);

        try {

            loadListView(inflater, view);
            addClickListener(view);
            loadTextInfoPOSitiveSvc();
            loadTextInfoPayment();

        } catch (Exception e) {
            Timber.w(e);
        }

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.setVariable(BR.viewModel, viewModel);
        binding.executePendingBindings();

        final Observer<Integer> buttonBackColourObserver = newValue -> refreshListViewData();
        viewModel.getBrandDisplayButtonColour().observe(getViewLifecycleOwner(), buttonBackColourObserver);

        final Observer<Integer> buttonTextColourObserver = newValue -> refreshListViewData();
        viewModel.getBrandDisplayButtonTextColour().observe(getViewLifecycleOwner(), buttonTextColourObserver);

        final Observer<Boolean> autoLaunchInProgressObserver = this::refreshAutoLaunchView;
        viewModel.getAutoLaunchInProgress().observe(getViewLifecycleOwner(), autoLaunchInProgressObserver);
    }

    @Override
    public void onPause() {
        // means fragment has gone to back. this can happen very quickly on startup where onResume is called quickly after
        Timber.d("InfoScreen onPause");
        super.onPause();
    }

    @Override
    public void onResume() {
        Timber.d("InfoScreen onResume");
        super.onResume();
    }

    private void killAutoLaunchTimer(){
        if( handler != null && runnable != null ) {
            handler.removeCallbacks(runnable);  // stop the Runnable when the Activity is destroyed
            handler = null;
            runnable = null;
        }
    }

    @Override
    public void onDestroyView() {
        Timber.d("InfoScreen onDestroyView");
        binding = null;
        killAutoLaunchTimer();
        super.onDestroyView();
    }

    @SuppressWarnings("deprecation")
    public PackageInfo getPackageInfo(String packagename) {
        try {
            return requireContext().getApplicationContext().getPackageManager().getPackageInfo(packagename, 0);
        } catch ( Exception e) {
            Timber.e(e);
        }
        return null;
    }

    public String getConfigSource() {
        return "PAXSTORE";
    }

    private void loadTextInfoPOSitiveSvc() {
        try {
            String positiveSvcVersionName;
            PackageInfo pinfo = getPackageInfo(requireContext().getApplicationContext().getPackageName());
            positiveSvcVersionName = pinfo.versionName;

            if (positiveSvcVersionName != null && !positiveSvcVersionName.isEmpty()) {
                String version = "" + positiveSvcVersionName + " (" + getConfigSource() + ")";

                /* use the string in the xml as it will be localised properly. On boot up UI.getInstance().getPrompt doesnt work at this point */
                if (verText == null) {
                    verText = binding.svcVersionLbl.getText().toString();
                }

                String serviceName = MalConfig.getInstance().getProfileCfg().getServiceName();
                if (serviceName != null && !serviceName.isEmpty())
                    binding.svcVersionLbl.setText(getString(R.string.app_name_version, serviceName, verText));

                binding.svcVersion.setText(version);
            }
        } catch (Exception e) {
            Timber.w(e);
        }
    }

    private void loadTextInfoPayment() {
        try {
            String paymentAppVersionName;
            PackageInfo pinfo = getPackageInfo("com.linkly.payment");

            if (pinfo != null) {
                paymentAppVersionName = pinfo.versionName;

                if (paymentAppVersionName != null && !paymentAppVersionName.isEmpty()) {
                    String version = "" + paymentAppVersionName;

                    /* use the string in the xml as it will be localised properly. On boot up UI.getInstance().getPrompt doesnt work at this point */
                    if (verText == null) {
                        verText = binding.paymentVersion.getText().toString();
                    }

                    String paymentName = MalConfig.getInstance().getProfileCfg().getPaymentName();
                    if (paymentName != null && !paymentName.isEmpty())
                        binding.paymentVersion.setText(getString(R.string.app_name_version, paymentName, verText ));

                    binding.payVersion.setText(version);
                }
            }

        } catch (Exception e) {
            Timber.w(e);
        }
    }

    /**
     * user selected application item from the menu
     * @param v view of button pressed
     */
    private void addClickListener(View v) {
        if (v == null)
            return;

        ListView list = v.findViewById(R.id.apps_list);
        if (list != null) {
            list.setOnItemClickListener((av, v1, pos, id) -> {
                if (!DownloadDirector.isInProgress()) {
                    // start the app
                    AppMgr.getInstance().startApp(getContext(), pos);
                } else {
                    UI.getInstance().postUINotification(UI.getInstance().getPrompt(String_id.STR_DOWNLOAD_MANAGER), UI.getInstance().getPrompt(String_id.STR_DOWNLOAD_IN_PROGRESS));
                }
            });
        }
    }

    // TODO FIXME: SharedPreferences needed here, timeout should be about time for reboot to occur.
    private boolean isFirstLaunchAfterBoot(){
        // use system millisecond tick counter to determine if this is the first boot, this timer starts at 0 when terminal starts. If within threshould, assume first boot
        long millisecondsSinceBoot = SystemClock.elapsedRealtime();
        Timber.d("millisecondsSinceBoot is %d", millisecondsSinceBoot);
        return millisecondsSinceBoot < MILLISECONDS_SINCE_BOOT_THRESHOLD;
    }

    private static int getAndIncrementLaunchAttemptNo(){
        return autoLaunchAttemptNo++;
    }

    private void setupAutoLaunchRetryTimer(String packageName) {
        handler = new Handler(Looper.getMainLooper());
        runnable = () -> {
            boolean revertToMainMenu = false;
            // check if we're still in the foreground (i.e. the launch attempt didn't succeed)
            if (!packageName.equals(getForegroundAppPackage(requireContext()))) {
                if (getAndIncrementLaunchAttemptNo() > 6) {
                    // failure to launch!
                    revertToMainMenu = true;
                    Timber.e("auto launch hasn't completed yet, exhausted retries, giving up");
                } else {
                    Timber.d("auto launch hasn't completed yet, trying again");
                    AppMgr.getInstance().autoStartApps(requireContext(), true);
                }
            } else {
                Timber.d("auto launch completed, launcher not in foreground any more");
                revertToMainMenu = true;
            }

            if (revertToMainMenu) {
                // launch was successful, clear auto launch UI flag
                Timber.d("clearing auto launch flag");
                BrandingConfig.setAutoLaunchInProgress(false);
            } else {
                // attempt to launch again, wait for another period then check again
                handler.postDelayed(runnable, AUTO_LAUNCH_CHECK_BACKOFF_DELAY_MSEC);
            }
        };

        // initial delay
        handler.postDelayed(runnable, AUTO_LAUNCH_CHECK_INITIAL_DELAY_MSEC);
    }

    private void setAutoLaunchParams(View v){
        // if we're about to auto-launch an app, don't display the app list
        AppDetail autoLaunchApp = AppMgr.getApkToAutoLaunch(requireContext());

        // on initial view, hide/display either 'auto launch' or app menu list
        Timber.d("Already passed boot time threshold?: %b", !isFirstLaunchAfterBoot());
        if( isFirstLaunchAfterBoot() && autoLaunchApp != null ) {
            Timber.d("Displaying 'auto launching app' screen");
            TextView tv = v.findViewById(R.id.auto_launch_notification);
            tv.setText(getString(R.string.auto_launching, autoLaunchApp.displayName));

            refreshAutoLaunchView(true);
            BrandingConfig.setAutoLaunchInProgress(true);

            // set a timer to turn off autoLaunchInProgress flag
            if( handler == null ){
                setupAutoLaunchRetryTimer(autoLaunchApp.packageName);
            }
        } else {
            Timber.d("Displaying normal launcher app menu");
            refreshAutoLaunchView(false);
            BrandingConfig.setAutoLaunchInProgress(false);
        }
    }

    private String getForegroundAppPackage(Context context) {
        UsageStatsManager usageStatsManager = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
        if (usageStatsManager != null) {
            long currentTime = System.currentTimeMillis();
            List<UsageStats> stats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_BEST, currentTime - 1000 * 10, currentTime);
            if (stats != null && !stats.isEmpty()) {
                SortedMap<Long, UsageStats> runningTask = new TreeMap<>();
                for (UsageStats usageStats : stats) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        // sort based on last foregrounded service used
                        runningTask.put(usageStats.getLastTimeForegroundServiceUsed(), usageStats);
                    } else {
                        // sort based on last used
                        runningTask.put(usageStats.getLastTimeUsed(), usageStats);
                    }
                }
                if (!runningTask.isEmpty()) {
                    return Objects.requireNonNull(runningTask.get(runningTask.lastKey())).getPackageName();
                }
            }
        }
        return null;
    }

    // Suppressing complexity warning here. Existing code structure.
    @SuppressWarnings("java:S3776")
    private void loadListView(final LayoutInflater li, View v) {
        if (v == null || getActivity() == null)
            return;

        // AutoLaunch from here (as well as Launcher (Service)), except in Unattended Mode.
        //  Unattended Mode only autoLaunches Key Injector and MPOS app (if present) and the
        //  Service vector is relied upon for that.
        if (!MalConfig.getInstance().getProfileCfg().isUnattendedModeAllowed()) {
            setAutoLaunchParams(v);
        }

        // display list of apps for user to select from
        final List<AppDetail> appList = AppMgr.getInstance().getApps();
        if (appList.isEmpty()) {
            View rootView = getActivity().findViewById(android.R.id.content);
            String message = "Profile configuration error, please reload resource package.";
            Snackbar.make(rootView, message, BaseTransientBottomBar.LENGTH_INDEFINITE).show();
        } else {

            ListView list = v.findViewById(R.id.apps_list);
            if (list != null) {

                ArrayAdapter<AppDetail> adapter = new ArrayAdapter<>(this.getActivity(),
                        R.layout.list_item_info_screen,
                        appList) {
                    @Override
                    public View getView(int position, View convertView, ViewGroup parent) {
                        if (convertView == null) {
                            convertView = li.inflate(R.layout.list_item_info_screen, null);
                        }
                        Context context = convertView.getContext();
                        RelativeLayout launcherLayout = (RelativeLayout) convertView.findViewById(R.id.launcher_layout);
                        GradientDrawable layoutDrawable = (GradientDrawable) launcherLayout.getBackground();
                        layoutDrawable.setColor(viewModel.getBrandDisplayButtonColour().getValue() != null ? viewModel.getBrandDisplayButtonColour().getValue() :
                                BrandingConfig.getBrandDisplayButtonColourOrDefault(context.getColor(R.color.color_linkly_primary)));
                        launcherLayout.setBackground(layoutDrawable);

                        ImageView appIcon = (ImageView) convertView.findViewById(R.id.item_app_icon);
                        appIcon.setImageDrawable(appList.get(position).icon);

                        TextView appDisplayName = (TextView) convertView.findViewById(R.id.item_app_display_name);
                        appDisplayName.setText(appList.get(position).displayName);
                        appDisplayName.setTextColor(viewModel.getBrandDisplayButtonTextColour().getValue() != null ? viewModel.getBrandDisplayButtonTextColour().getValue() :
                                BrandingConfig.getBrandDisplayButtonTextColourOrDefault());

                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT
                        );
                        params.weight = 1.0f;
                        params.setMargins(BUTTON_MARGIN, BUTTON_MARGIN, BUTTON_MARGIN, BUTTON_MARGIN);
                        convertView.setLayoutParams(params);

                        return convertView;
                    }
                };
                list.setAdapter(adapter);
            }
        }
    }

    private void refreshListViewData() {
        ListView list = binding.getRoot().findViewById(R.id.apps_list);
        if (list != null) {
            BaseAdapter la = (BaseAdapter) list.getAdapter();
            if (la != null) {
                la.notifyDataSetChanged();
            }
        }
    }


    private void refreshAutoLaunchView(Boolean newValue) {
        Timber.d("refreshAutoLaunchView %b", newValue);
        TextView tv = binding.getRoot().findViewById(R.id.auto_launch_notification);
        ListView list = binding.getRoot().findViewById(R.id.apps_list);
        if( tv != null ) {
            tv.setVisibility(Boolean.TRUE.equals(newValue)?View.VISIBLE:View.GONE);
            list.setVisibility(Boolean.TRUE.equals(newValue)?View.GONE:View.VISIBLE);
        }
    }
}
