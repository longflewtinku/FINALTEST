package com.linkly.launcher.fragments;


import static com.linkly.launcher.applications.AppFunctions.MainMenuFunction.exitKioskMode;
import static com.linkly.libpositive.PosIntegrate.CONFIG_TYPE.CT_AMOUNT;
import static com.linkly.libpositive.PosIntegrate.TRANSACTION_TYPE.TRANSACTION_TYPE_SALE;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.linkly.launcher.FakeLauncherActivity;
import com.linkly.launcher.R;
import com.linkly.launcher.applications.AppFunctions;
import com.linkly.launcher.service.LauncherUtils;
import com.linkly.libconfig.MalConfig;
import com.linkly.libmal.MalFactory;
import com.linkly.libmal.global.util.DisplayKiosk;
import com.linkly.libpositive.PosIntegrate;
import com.linkly.libpositive.messages.Messages;
import com.linkly.libpositivesvc.downloader.DownloadDirector;
import com.linkly.libui.UI;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;

import timber.log.Timber;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MainMenu#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MainMenu extends Fragment {

    private static final String TAG = "MainMenu";
    private List<AppFunctions> appFunctions;

    public MainMenu() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment MainMenu.
     */
    public static MainMenu newInstance() {
        MainMenu fragment = new MainMenu();

        return fragment;
    }

    private static void resetPreferredLauncherAndOpenChooser(Context context) {

        Intent intent = new Intent();
        if (intent != null) {
            try {
                if (Build.MODEL.compareTo("A80") == 0) {
                    Intent intent2 = new Intent();
                    intent2.setAction(Intent.ACTION_MAIN);
                    intent2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                    intent2.addCategory(Intent.CATEGORY_DEFAULT);
                    context.startActivity(intent2);
                } else {
                    PackageManager packageManager = context.getPackageManager();
                    ComponentName componentName = new ComponentName(context, FakeLauncherActivity.class);
                    packageManager.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);

                    Intent selector = new Intent(Intent.ACTION_MAIN);
                    selector.addCategory(Intent.CATEGORY_HOME);
                    selector.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(selector);
                    packageManager.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_DEFAULT, PackageManager.DONT_KILL_APP);
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
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_main_menu, container, false);

        loadApps();
        loadListView(inflater, v);
        addClickListener(v);

        return v;
    }

    private void loadApps() {
        int appNum = 1;
        appFunctions = new ArrayList<AppFunctions>();
//        appFunctions.add(new AppFunctions(launchPayment, appNum++ + ". Run Payment"));
//        appFunctions.add(new AppFunctions(autoRec, appNum++ + ". Auto Rec"));
//        appFunctions.add(new AppFunctions(batchUpload, appNum++ + ". Batch Upload"));
        //appFunctions.add(new AppFunctions(heartBeat, "4. Heartbeat"));
       // appFunctions.add(new AppFunctions(runPost, "4. Run Post"));
//        appFunctions.add(new AppFunctions(runUpdate, "5. Run Update"));
//        appFunctions.add(new AppFunctions(runForceUpdate, "6. Force Update"));
        //appFunctions.add(new AppFunctions(runTrans, appNum++ + ". Run Transaction"));
        //appFunctions.add(new AppFunctions(runReversal, appNum++ + ". Run Reversal"));
        //appFunctions.add(new AppFunctions(runInstall, appNum++ + ". Run Install"));
//        appFunctions.add(new AppFunctions(resetAutoRec, appNum++ + ". Reset Auto rec"));
        appFunctions.add(new AppFunctions(exitKioskMode, appNum++ + ". Exit Kiosk Mode"));

    }

    private void addClickListener(View v) {

        ListView list = (ListView) v.findViewById(R.id.function_list);
        if (list == null)
            return;


        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> av, View v, int pos,
                                    long id) {

                AppFunctions f = appFunctions.get(pos);
                switch (f.function) {
                    case launchPayment: {
                        Timber.i("Launch Payment");
                        Messages.getInstance().sendAutoStartRequest(getActivity().getApplicationContext());
                        break;
                    }

                    case autoRec: {
                        Timber.i("Run Auto Rec");
                        Messages.getInstance().sendAutoRecRequest(getActivity().getApplicationContext());
                        LauncherUtils.POSitiveSvcNotify(getActivity().getApplicationContext(), "Auto Rec Run");
                        break;
                    }

                    case batchUpload: {
                        Timber.i("Run Batch Upload");
                        Messages.getInstance().sendBatchUploadRequest(getActivity().getApplicationContext());
                        LauncherUtils.POSitiveSvcNotify(getActivity().getApplicationContext(), "Batch Upload Run ");
                        break;
                    }



                    case runUpdate: {
                        Timber.i("Run Update");
                        if (MalConfig.getInstance().getDownloadCfg().isValidCfg()) {
                            DownloadDirector.update(getActivity(), false);
                            UI.getInstance().postUINotification("Download Manager", "Checking for Updates");
                        } else {
                            UI.getInstance().postUINotification("Download Manager", "MalConfig Unavailable");
                        }
                        break;
                    }
                    case runForceUpdate: {
                        Timber.i("Force Update");
                        if (MalConfig.getInstance().getDownloadCfg().isValidCfg()) {
                            UI.getInstance().postUINotification("Download Manager", "Checking for Updates");
                            DownloadDirector.forceUpdate(getActivity());
                            // ServiceDownloadManager.DOWNLOAD_UPDATE_RESULT res = ServiceDownloadManager.getInstance().runDownloadManagerUpdate(DownloadManager.DOWNLOAD_REQUEST_TYPE.DOWNLOAD_FORCE);
                        } else {
                            /*Please Wait Toast*/
                            UI.getInstance().postUINotification("Download Manager", "MalConfig Unavailable");
                        }
                        break;
                    }

                    case runTrans: {
                        Timber.i("Run Auto TransRec");
                        HashMap<PosIntegrate.CONFIG_TYPE, String> args = new HashMap<PosIntegrate.CONFIG_TYPE, String>();
                        args.put(CT_AMOUNT, "100");
                        PosIntegrate.executeTransaction(getActivity().getApplicationContext(), TRANSACTION_TYPE_SALE, args);
                        break;
                    }

                    case runReversal: {
                        Timber.i("Run Auto Reversal");
                        HashMap<PosIntegrate.CONFIG_TYPE, String> args = new HashMap<PosIntegrate.CONFIG_TYPE, String>();
                        args.put(CT_AMOUNT, "100");

                        PosIntegrate.executeReversal(getActivity().getApplicationContext(), args);
                        break;
                    }

                    case runInstall: {
                        Timber.i("Install");
                        UI.getInstance().postUINotification("Download Manager", "Checking for New Installs");
                        Messages.getInstance().sendAppUpdateRequest(getActivity().getApplicationContext(), MalFactory.getInstance().getFile().getInstallDir());
                        break;
                    }

                    case resetAutoRec: {
                        Calendar calendar = GregorianCalendar.getInstance();
                        SharedPreferences sharedPref = getContext().getApplicationContext().getSharedPreferences("com.linkly.service.cfg", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putLong("lastRec", 0);
                        editor.apply();
                        break;
                    }

                    case exitKioskMode: {
                        DisplayKiosk.getInstance().exitKioskMode(getActivity());
                        resetPreferredLauncherAndOpenChooser(getActivity());
                        break;
                    }

                }
            }
        });
    }

    private void loadListView(final LayoutInflater li, View v) {
        if (v == null)
            return;
        ListView list = (ListView) v.findViewById(R.id.function_list);

        ArrayAdapter<AppFunctions> adapter = new ArrayAdapter<AppFunctions>(this.getContext().getApplicationContext(),
                R.layout.list_item_main_menu,
                appFunctions) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    convertView = li.inflate(R.layout.list_item_main_menu, null);
                }

                TextView appDisplayName = (TextView) convertView.findViewById(R.id.item_app_display_name);
                appDisplayName.setText(appFunctions.get(position).displayName);

                return convertView;
            }
        };

        if (list != null)
            list.setAdapter(adapter);

    }


}
