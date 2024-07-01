package com.linkly.launcher;

import static com.linkly.launcher.fragments.InputFragment.USECASE_ESCAPE_LOCKDOWN;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.viewpager.widget.ViewPager;

import com.linkly.launcher.access.AccessCodeCheckCallbacks;
import com.linkly.launcher.access.AccessCodeHelpers;
import com.linkly.launcher.fragments.InputFragment;
import com.linkly.launcher.fragments.MenuHostFragment;
import com.linkly.launcher.work.UnattendedRebootWorker;
import com.linkly.libconfig.MalConfig;
import com.linkly.libmal.MalFactory;
import com.linkly.libmal.global.util.DisplayKiosk;
import com.linkly.libpositive.messages.IMessages;

import timber.log.Timber;

/***
 * Most of the Menu Authorization is contained in this Activity and its child Fragments, however to
 *  suppress showing Out of Service when
 */
public class ServiceFrontEnd extends AppCompatActivity implements AccessCodeCheckCallbacks, AuthHost {

    public static final int ACCESSCODE_EXIT = 1;
    public static final int ACCESSCODE_ADMIN_MENU = 2;
    public static final int ACCESSCODE_UNATTENDED_ESCAPEE = 3;

    private boolean isAdminMenuAccessGranted = false;

    /**
     * The {@link ViewPager} that will host the section contents.
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Timber.v("ServiceFrontEnd onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_front_end);

        //To make the status bar color green
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().setStatusBarColor(BrandingConfig.getBrandDisplayStatusBarColourOrDefault(getColor(R.color.color_linkly_primary)));
        }
        DisplayKiosk.getInstance().enterKioskMode(ServiceFrontEnd.this);

        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(mMessageReceiver, new IntentFilter(IMessages.APP_SVC_PROGRESS_DIALOG));

        FragmentContainerView fragContainer = findViewById(R.id.frag_container);
        if (fragContainer == null) {
            Timber.e("ServiceFrontEnd fragContainer not found!");
            return;
        }

        Fragment contentFrag = (MalConfig.getInstance().getProfileCfg().isUnattendedModeAllowed())?
            InputFragment.forAuth(USECASE_ESCAPE_LOCKDOWN) : new MenuHostFragment();
        getSupportFragmentManager().beginTransaction()
                .setReorderingAllowed(true)
                .add(R.id.frag_container, contentFrag, null)
                .commit();
    }

    @Override
    protected void onPause() {
        Timber.v("ServiceFrontEnd onPause");
        super.onPause();
    }

    @Override
    protected void onResume() {
        Timber.v("ServiceFrontEnd onResume");
        super.onResume();
        DisplayKiosk.getInstance().onResume(false);
    }

    protected BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (IMessages.APP_SVC_PROGRESS_DIALOG.equals(intent.getAction())) {
                checkForProgressDialog(intent);
            }
        }
    };

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    @SuppressWarnings("deprecation")
    ProgressDialog progressDialog = null;
    @SuppressWarnings("deprecation")
    public void checkForProgressDialog(Intent intent) {

        if (intent.getBooleanExtra("PROGRESS", false)) {
            String title = intent.getStringExtra("TITLE");
            String message = intent.getStringExtra("MESSAGE");

            try {

                if (progressDialog != null) {
                    progressDialog.dismiss();
                    progressDialog = null;
                }
                if (Looper.myLooper() != null)
                    progressDialog = ProgressDialog.show(this, title, message, true);

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else if (intent.getBooleanExtra("PROGRESSDISMISS", false)) {
            if (progressDialog != null) {
                progressDialog.dismiss();
                progressDialog = null;
            }

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_service_front_end, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {

        if (MalFactory.getInstance().getHardware().processVolumeKey(getApplicationContext(), event))
            return true;

        return super.dispatchKeyEvent(event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(mMessageReceiver);
        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }

    @Override
    public void onAdminMenuGranted() {
        Timber.e("onAdminMenuGranted...");
        isAdminMenuAccessGranted = true;
        DisplayKiosk.getInstance().enableStatusBarDropDown(true);
    }

    @Override
    public void onExitLauncherGranted() {
        Timber.e("onExitLauncherGranted...");
        isAdminMenuAccessGranted = false;
        ((UnattendedServiceModeAuthorizationHost)getApplication())
                .setUnattendedServiceModeAdminAccessGranted(false);
        throw new UnsupportedOperationException("Not implemented!");
    }

    @Override
    public void onEnterUnattendedServiceModeModeGranted() {
        Timber.e("onEnterUnattendedServiceModeModeGranted...");
        isAdminMenuAccessGranted = false;
        UnattendedRebootWorker.scheduleReboot(this);
        ((UnattendedServiceModeAuthorizationHost)getApplication())
                .setUnattendedServiceModeAdminAccessGranted(true);
        getSupportFragmentManager().beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.frag_container, new MenuHostFragment(), null)
                .commit();
    }

    @Override
    public void onAccessDenied() {
        Timber.e("onAccessDenied...");
        isAdminMenuAccessGranted = false;
        onAuthCancellation();
    }

    // note that the content of the Intent is sensitive but is not sent over IPC or between components.
    @Override
    public void onAuthSubmission(Intent intent, int pwdType) {
        Timber.d("onAuthSubmission...");
        AccessCodeHelpers.checkAccessCode(intent.getStringExtra("resultText"), pwdType, this);
    }

    @Override
    public void onAuthCancellation() {
        Timber.d("onAuthCancellation...");
        ((UnattendedServiceModeAuthorizationHost)getApplication())
                .setUnattendedServiceModeAdminAccessGranted(false);
        if (MalConfig.getInstance().getProfileCfg().isUnattendedModeAllowed()) {
            // Without forcing start of LockedDownLauncher it may not be present in backstack causing a
            //  Launcher restart.
            startActivity(LockedDownActivity.buildStartIntent(getApplicationContext()));
        }
        finishAfterTransition();
    }

    @Override
    public boolean isUnattendedServiceModeAdminAccessGranted() {
        Timber.d("isUnattendedServiceModeAdminAccessGranted...%b",
                ((UnattendedServiceModeAuthorizationHost)getApplication())
                .isUnattendedServiceModeAdminAccessGranted());
        return ((UnattendedServiceModeAuthorizationHost)getApplication())
                .isUnattendedServiceModeAdminAccessGranted();
    }

    @Override
    public boolean isAdminMenuAccessGranted() {
        Timber.d("isAdminMenuAccessGranted...%b", isAdminMenuAccessGranted);
        return isAdminMenuAccessGranted;
    }
}
