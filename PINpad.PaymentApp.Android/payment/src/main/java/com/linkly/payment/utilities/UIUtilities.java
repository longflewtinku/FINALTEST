package com.linkly.payment.utilities;

import static com.linkly.libui.IUIDisplay.uiDisableScreensaver;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.provider.Settings;
import android.widget.Button;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.linkly.libengine.config.PayCfg;
import com.linkly.libengine.engine.Engine;
import com.linkly.libengine.users.UserManager;
import com.linkly.libmal.MalFactory;
import com.linkly.libmal.global.platform.EFTPlatform;
import com.linkly.libmal.global.platform.Platform;
import com.linkly.libui.IUIDisplay;
import com.linkly.libui.IUIDisplay.String_id;
import com.linkly.libui.UI;
import com.linkly.libui.display.DisplayRequest;
import com.linkly.payment.R;
import com.linkly.payment.activities.ActIdle;
import com.linkly.payment.activities.ActScreenSaver;
import com.linkly.payment.activities.AppMain;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import timber.log.Timber;

public class UIUtilities {

    private UIUtilities() {
    }

    @SuppressWarnings("deprecation")
    private static String getText(DisplayRequest displayRequest, String textIdKey, String textArgKey, String textKey) {
        String_id textId = (textIdKey != null) ? (String_id)displayRequest.getUiExtras().get(textIdKey) : null;
        String[] textArgs = (textArgKey != null) ? (String[])displayRequest.getUiExtras().get(textArgKey) : null;
        String text;

        if (textId != null && textId.getId()!= 0) {
            if (textArgs != null && textArgs.length > 0) {
                text = UI.getInstance().getPrompt(textId,textArgs);
            }
            else {
                text = UI.getInstance().getPrompt(textId);
            }
        }
        else {
            text = displayRequest.getUiExtras().getString(textKey);
        }

        if (text == null)
            text = "";

        return text;
    }

    public static String getTitleText(DisplayRequest displayRequest) {
        return getText(displayRequest, IUIDisplay.uiTitleId, IUIDisplay.uiTitleArg, IUIDisplay.uiScreenTitle);
    }

    public static String getPromptText(DisplayRequest displayRequest) {
        return getText(displayRequest, IUIDisplay.uiPromptId, IUIDisplay.uiPromptIdArg, IUIDisplay.uiScreenPrompt);
    }

    public static String getHintText(DisplayRequest displayRequest) {
        return getText(displayRequest, IUIDisplay.uiInputHintId, null, IUIDisplay.uiScreenInputHint);
    }

    private static void addPermission(final Activity activity, List<String> permissionsList, String permission) {
        if (activity.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
            permissionsList.add(permission);
        }
    }

    private static List<String> getPermissionList(final Activity activity) {
        final List<String> permissionsList = new ArrayList<>();
        final int currentApiVersion = Build.VERSION.SDK_INT;
        Timber.i("checkPermission: currentApiVersion=%d", currentApiVersion);

        // debug only - checks if this app can start activities from background. required on android 10+ devices.
        // Pax can enable this permission automatically using customer resource packages
        boolean canDrawOverlays = Settings.canDrawOverlays(activity);
        Timber.e( "canDrawOverlays = %b", canDrawOverlays );

        if (currentApiVersion >= Build.VERSION_CODES.M) {   // API LEVEL 23

            addPermission(activity, permissionsList, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (!Platform.isPaxTill())
                addPermission(activity, permissionsList, Manifest.permission.ACCESS_COARSE_LOCATION);
        } else {
            Timber.i("checkPermission - SDK Version < 23");
        }
        return permissionsList;

    }

    private static final int REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 124;
    public static boolean checkPermission(final Activity activity) {

        final List<String> permissionsList = getPermissionList(activity);
        if (!permissionsList.isEmpty()) {

            showMessageOK(activity, "  WARNING:\n  You must Approve ALL permissions \n  for the Payment app to work. ",
                    (dialog, which) -> ActivityCompat.requestPermissions(activity, permissionsList.toArray(new String[0]), REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS));
            return true;
        }
        return false;
    }

    /* checks if we have permissions required to run the app */
    public static void checkPermissionAgain(final Activity activity) {

        final List<String> permissionsList = getPermissionList(activity);

        for (String permission : permissionsList) {

            if (activity.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {

                showMessageOK(activity, "  WARNING\n  Permissions Not Granted \n  App Will Exit",
                        (dialog, which) -> activity.finishAffinity());
            }
        }
    }

    private static void showMessageOK(final Activity activity, String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(activity)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .create()
                .show();
    }

    public static void validateCommsAvailability(Activity activity) {
        boolean wifi = Objects.requireNonNull(MalFactory.getInstance()).getComms().isWifiConnectedToNetwork(activity.getApplicationContext());
        boolean sim =  Objects.requireNonNull(MalFactory.getInstance()).getComms().isSIMCardPresent(activity.getApplicationContext());

        if (!wifi && !sim && UserManager.getActiveUser() != null) {
            androidx.appcompat.app.AlertDialog.Builder builder1 = new androidx.appcompat.app.AlertDialog.Builder(activity);
            builder1.setTitle("Comms Limited");
            builder1.setMessage("No WI-FI or Active SIM Found\nOperations will be limited");

            builder1.setCancelable(true);

            builder1.setPositiveButton("Dismiss",
                    (dialog, id) -> dialog.cancel());

            builder1.create().show();
        }
    }

    public static void validateConfigAvailability(Activity activity) {
        PayCfg payCfg = Engine.getDep().getPayCfg();

        if ((payCfg == null || (!payCfg.isValidCfg()) && Platform.isPaxTerminal())) {
            //Only Show the TMS Message on the PAx terminals as  most people on Mobiles wont understand the message
            androidx.appcompat.app.AlertDialog.Builder builder1 = new androidx.appcompat.app.AlertDialog.Builder(activity);
            builder1.setTitle("MalConfig Invalid");
            builder1.setMessage("Please Update MalConfig\nFrom the TMS");
            builder1.setCancelable(true);
            builder1.setPositiveButton("Dismiss",
                    (dialog, id) -> dialog.cancel());
            androidx.appcompat.app.AlertDialog alert11 = builder1.create();
            alert11.show();
        }

    }

    // Check the terminal is using the correct packages
    private static boolean initialised = false;
    public static void validateTerminalConfig(Activity activity) {

        if (initialised) {
            return;
        }
        validateConfigAvailability(activity);
        initialised = true;
        validateCommsAvailability(activity);
    }

    public static void launchActivity(Class<?> cls, boolean noHistory, DisplayRequest p, boolean sendExtras) {

        if (EFTPlatform.isAppHidden()) {
            Timber.i("This App is Hidden");
            return;
        }
        ActIdle actIdle = AppMain.getApp().getAppActivity();
        if (actIdle != null) {
            Intent intent = new Intent(actIdle, cls);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            p.getUiExtras().putInt("iActivityID", p.getActivityID().ordinal());
            Timber.i("Launch Activity: " + p.getActivityID().name() + ": " + p.getActivityID().ordinal());

            if (sendExtras)
                intent.putExtras(p.getUiExtras());

            if (noHistory) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            }

            AppMain.getApp().getApplicationContext().startActivity(intent);
        }
    }

    public static void screenSaverRequest(DisplayRequest m) {
        if (m.getUiExtras().getBoolean(uiDisableScreensaver, false)) {
            ActScreenSaver.cancelScreenSaver();
        }
    }

    public static void borderTransparentButton(FragmentActivity activity,Button button) {
        GradientDrawable cancelDrawable = (GradientDrawable) button.getBackground().getConstantState().newDrawable().mutate();
        cancelDrawable.setStroke(2, Engine.getDep().getPayCfg().getBrandDisplayButtonColourOrDefault(activity.getColor(R.color.color_linkly_primary)));
        button.setBackground(cancelDrawable);
    }

    public static float getDisplayDensity(Context context) {
        return context.getResources().getDisplayMetrics().density;
    }

}
