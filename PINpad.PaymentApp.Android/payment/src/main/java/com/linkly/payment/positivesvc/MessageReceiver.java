package com.linkly.payment.positivesvc;

import static com.linkly.libengine.action.Loyalty.GameCode.GAME_ACTION;
import static com.linkly.libengine.action.Loyalty.GameCode.RECEIVE_NOMINATE_ACTION;
import static com.linkly.libengine.helpers.UIHelpers.wakeTerminalIfSleeping;
import static com.linkly.libpositive.PosIntegrate.ResultResponse.RES_BAD_REQUEST;
import static com.linkly.libpositive.PosIntegrate.ResultResponse.RES_TERMINAL_BUSY;
import static com.linkly.libpositive.events.PositiveEvent.EventType.AUTO_START;
import static com.linkly.libpositive.events.PositiveEvent.EventType.CONTINUE_PRINT;
import static com.linkly.libpositive.events.PositiveEvent.EventType.REBOOT;
import static com.linkly.libpositive.events.PositiveEvent.EventType.SHIFT_TOTALS_AUTOMATIC_REPORT;
import static com.linkly.libpositive.messages.IMessages.ANDROID_POWER_CONNECTED;
import static com.linkly.libpositive.messages.IMessages.ANDROID_POWER_DISCONNECTED;
import static com.linkly.libpositive.messages.IMessages.APP_CHARGING_EVENT;
import static com.linkly.libpositive.messages.IMessages.APP_CONFIG_EVENT;
import static com.linkly.libpositive.messages.IMessages.APP_DISABLE_CANCEL_EVENT;
import static com.linkly.libpositive.messages.IMessages.APP_LOGON_EVENT;
import static com.linkly.libpositive.messages.IMessages.APP_REBOOT_EVENT;
import static com.linkly.libpositive.messages.IMessages.APP_REBOOT_SUSPEND_EVENT;
import static com.linkly.libpositive.messages.IMessages.APP_REBOOT_WARNING_EVENT;
import static com.linkly.libpositive.messages.IMessages.APP_RESTART_EVENT;
import static com.linkly.libpositive.messages.IMessages.APP_SEND_EFT_FILES_EVENT;
import static com.linkly.libpositive.messages.IMessages.APP_STATUS_INFO_REQUEST_EVENT;
import static com.linkly.libpositive.messages.IMessages.BATCH_UPLOAD_EVENT;
import static com.linkly.libpositive.messages.IMessages.POS_KEY_EVENT;
import static com.linkly.libpositive.messages.IMessages.READ_CARD_REQUEST_EVENT;
import static com.linkly.libpositive.messages.IMessages.REPORT_REQUEST_EVENT;
import static com.linkly.libpositive.messages.IMessages.SERVICE_EVENT;
import static com.linkly.libpositive.messages.IMessages.SHIFT_TOTALS_SCHEDULED_RESET_EVENT;
import static com.linkly.libpositive.messages.IMessages.TRANSACTION_REQUEST_EVENT;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;
import android.os.Bundle;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.gson.Gson;
import com.linkly.libengine.action.Loyalty.LoyaltyProcessing;
import com.linkly.libengine.application.IAppCallbacks;
import com.linkly.libengine.engine.Engine;
import com.linkly.libengine.engine.transactions.TransRec;
import com.linkly.libengine.helpers.ECRHelpers;
import com.linkly.libengine.jobs.EFTJob;
import com.linkly.libengine.workflow.WorkflowScheduler;
import com.linkly.libmal.MalFactory;
import com.linkly.libmal.global.platform.StartupParams;
import com.linkly.libmal.global.util.Util;
import com.linkly.libpositive.events.PositiveEvent;
import com.linkly.libpositive.events.PositiveReadCardEvent;
import com.linkly.libpositive.events.PositiveTransEvent;
import com.linkly.libpositivesvc.POSitiveSvcLib;
import com.linkly.payment.application.AppCallbacks;

import timber.log.Timber;

public class MessageReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Timber.d("onReceive...intent: %s", intent.toUri(0));
        wakeTerminalIfSleeping(context);
        Gson gson = new Gson();

        if ( intent.getAction() != null ) {
            Timber.i("onReceive called: %s", intent.getAction());
        } else {
            Timber.e("Discarding intent");
            return;
        }

        IAppCallbacks iAppCallbacks = Engine.getAppCallbacks();
        if (iAppCallbacks == null)
            iAppCallbacks = new AppCallbacks();

        switch ( intent.getAction() ){
            case APP_REBOOT_WARNING_EVENT: {
                if (WorkflowScheduler.isTransactionRunning()) {
                    Intent tempIntent = new Intent();
                    tempIntent.setAction(APP_REBOOT_SUSPEND_EVENT);
                    context.sendBroadcast(tempIntent);
                    Timber.i("We are trading so suspend the reboot");
                }
                break;
            }
            case APP_DISABLE_CANCEL_EVENT: {
                boolean cancelDisabled = intent.getExtras() != null && intent.getExtras().getBoolean("CancelDisabled");
                Util.disableCancel(cancelDisabled, true);
                break;
            }
            case APP_SEND_EFT_FILES_EVENT: {
                try {
                    Timber.i("Copy resources over received in broadcast:%s", MalFactory.getInstance().getFile().getCommonDir());
                    MalFactory.getInstance().initialiseFiles(context);

                    // run this on a thread as it can take a relatively long time and cause system to terminate the app
                    new Thread(() -> POSitiveSvcLib.copyResources(context, intent, MalFactory.getInstance().getFile().getCommonDir(), false)).start();

                } catch (Exception e) {
                    Timber.i("APP_SEND_EFT_FILES_EVENT: Error receiving files from someone");
                }

                // Start App to setup PCI Reboot timer
                // NOTE! this takes a while to complete and can cause the PA to be unresponsive to
                //  launch intents during terminal startup. We may want to review if this is
                //  necessary/essential to do here.
                // IFF hideWhenDone and autoStarted are true here, the app will background itself
                //  despite being in Standalone mode, unless Credentials are saved along with
                //  AutoLogon ON where reboot lands on ActMainMenu.
                iAppCallbacks.runApplication(context, new StartupParams(true, true, false, false, true));
                break;
            }
            case BATCH_UPLOAD_EVENT: {
                Timber.i("-MessageReceiver: Batch Upload Event Received");
                performBatchUpload();
                break;
            }
            case APP_RESTART_EVENT: {
                Timber.i("App Restart Event Received");
                iAppCallbacks.runApplication(context, new StartupParams(false, false, false, false, false));
                break;
            }
            case APP_REBOOT_EVENT: {
                Timber.i("App Reboot Event Received");
                iAppCallbacks.runApplication(context, new StartupParams(false, false, false, false, false));
                Engine.getJobs().add(new EFTJob(REBOOT));
                break;
            }
            case TRANSACTION_REQUEST_EVENT: {
                handleActionTransReqEvent(intent, context, gson, iAppCallbacks);
                break;
            }
            case REPORT_REQUEST_EVENT: {
                handleActionReportReqEvent(intent);
                break;
            }
            case ANDROID_POWER_CONNECTED: {
                handleActionPower(true);
                break;
            }
            case ANDROID_POWER_DISCONNECTED: {
                handleActionPower(false);
                break;
            }
            case RECEIVE_NOMINATE_ACTION: {
                Timber.i("Loyalty: Received RECEIVE_NOMINATE_ACTION");
                LoyaltyProcessing.processIntent(intent);
                break;
            }
            case GAME_ACTION: {
                Timber.i("Loyalty: Received GAME_ACTION");
                break;
            }
            case READ_CARD_REQUEST_EVENT: {
                handleActionReadCardReqEvent(intent, context, gson, iAppCallbacks);
                break;
            }
            case APP_STATUS_INFO_REQUEST_EVENT: {
                handleActionStatusInfoReqEvent(intent, context, gson, iAppCallbacks);
                break;
            }
            case APP_LOGON_EVENT: {
                handleActionAppLogonEvent(intent, context, gson, iAppCallbacks);
                break;
            }
            case APP_CONFIG_EVENT: {
                handleActionAppConfigEvent(intent, context, gson, iAppCallbacks);
                break;
            }
            case POS_KEY_EVENT: {
                handleActionPosKeyEvent(intent, gson);
                break;
            }
            case Intent.ACTION_AIRPLANE_MODE_CHANGED: {
                handleAirplaneModeEvent(intent);
                break;
            }
            case SHIFT_TOTALS_SCHEDULED_RESET_EVENT: {
                Timber.i("Shift Totals Scheduled Reset Event Received");
                iAppCallbacks.runApplication(context, new StartupParams(false, true, true, false, true));
                Engine.getJobs().add(new EFTJob(SHIFT_TOTALS_AUTOMATIC_REPORT));
                break;
            }
            default: {
                handleActionDefault(intent, context, gson, iAppCallbacks);
                break;
            }
        }
    }

    private void handleActionTransReqEvent(Intent intent, Context context, Gson gson, IAppCallbacks iAppCallbacks) {
        Timber.d("Request Received time" );
        String eventRequest = intent.getStringExtra(SERVICE_EVENT);
        Timber.i(eventRequest);
        PositiveTransEvent event = gson.fromJson(eventRequest, PositiveTransEvent.class);

        if (event == null || event.getType() == null) {
            Timber.e("TRANSACTION_REQUEST_EVENT Event Received but type is unknown");
            ECRHelpers.ipcSendNullTransResponse(Engine.getDep(), event, RES_BAD_REQUEST, context);
        }
        else {
            if (CONTINUE_PRINT == event.getType()) {
                Timber.d("Continue Print event received, skipping jobs and directly setting variables");
                if (Engine.getDep().getCurrentTransaction() != null) {
                    TransRec trans = Engine.getDep().getCurrentTransaction();
                    // We may get an override to use the terminal printer
                    if (event.isUseTerminalPrinter()) { // if we have been told to use the terminal printer
                        trans.setPrintOnTerminal(event.isUseTerminalPrinter());
                    }

                    trans.setContinuePrint(true);
                    Timber.i("Continue Print flag set");
                }
            } else {
                // check if terminal is busy processing a transaction already
                // 1. cancel any idle process in progress
                WorkflowScheduler.getInstance().checkAndRemoveWaitingWorkFlow(false);
                // 2. then check if busy
                boolean isTerminalBusy = WorkflowScheduler.getInstance().isTerminalBusy(false);
                if( !isTerminalBusy ) {
                    boolean background;

                    // not busy - launch ActIdle and start the operation
                    Timber.i("LAUNCH APP FOR TRANSACTION EVENT:%s", event.getType().name());

                    // Reprint must be run in foreground, in case we need to display a paper-out message.
                    if (event.getType() == PositiveEvent.EventType.QUERY_TRANS && event.isReprint()) {
                        background = false;
                    } else {
                        background = event.getType().backgroundTask;
                    }


                    iAppCallbacks.runApplication(context, new StartupParams(background, true, false, false, true));
                    Engine.getJobs().add(new EFTJob(event));
                    Timber.i("TRANSACTION_REQUEST_EVENT Event Received");
                } else {
                    // we are busy - respond with busy response back to connect app
                    Timber.e("TRANSACTION_REQUEST_EVENT Event Received but transaction is already running");
                    ECRHelpers.ipcSendNullTransResponse(Engine.getDep(), event, RES_TERMINAL_BUSY, context);
                }
            }
        }
    }

    private void handleActionReportReqEvent(Intent intent) {
        Bundle b = intent.getExtras();
        if (b != null) {
            Timber.i("Print report:%s", b.toString());
        }
    }

    private void handleActionPower(boolean isConnected) {
        Intent tempIntent = new Intent();
        tempIntent.setAction(APP_CHARGING_EVENT);
        tempIntent.putExtra("Charging", isConnected);
        if (MalFactory.getInstance() != null && MalFactory.getInstance().getMalContext() != null) {
            LocalBroadcastManager.getInstance(MalFactory.getInstance().getMalContext()).sendBroadcast(tempIntent);
        }
        Timber.i("Power %s", (isConnected ? "connected" : "disconnected"));
    }

    private void handleActionReadCardReqEvent(Intent intent, Context context, Gson gson, IAppCallbacks iAppCallbacks) {
        PositiveReadCardEvent readCardEvent = gson.fromJson(intent.getStringExtra(SERVICE_EVENT), PositiveReadCardEvent.class);
        iAppCallbacks.runApplication(context,
                new StartupParams(
                        readCardEvent.getType().backgroundTask,
                        true,
                        false,
                        false,
                        true));
        Timber.i(READ_CARD_REQUEST_EVENT);
        Engine.getJobs().add(new EFTJob(readCardEvent));
    }

    private void handleActionStatusInfoReqEvent(Intent intent, Context context, Gson gson, IAppCallbacks iAppCallbacks) {
        String eventJson = intent.getStringExtra(SERVICE_EVENT);
        PositiveEvent event = gson.fromJson(eventJson, PositiveEvent.class);

        if (event != null && !WorkflowScheduler.isTransactionRunning()) {
            iAppCallbacks.runApplication(context,
                    new StartupParams(
                            event.getType().backgroundTask,
                            false,
                            false,
                            false,
                            true));
            Engine.getJobs().add(new EFTJob((event)));
        }
    }

    private void handleActionAppLogonEvent(Intent intent, Context context, Gson gson, IAppCallbacks iAppCallbacks) {
        if ( intent.hasExtra(SERVICE_EVENT)) {
            PositiveTransEvent event = gson.fromJson(intent.getStringExtra(SERVICE_EVENT), PositiveTransEvent.class);

            Timber.i(APP_LOGON_EVENT);
            if (event != null) {
                final PositiveEvent.EventType eventType = event.getType();
                final StartupParams params = new StartupParams(
                        eventType.backgroundTask,
                        eventType.hideWhenDone,
                        eventType.hideWhenDone,
                        eventType.showSplashScreen,
                        true);

                Timber.i("LAUNCH APP FOR TRANSACTION EVENT:%s", eventType.name());
                iAppCallbacks.runApplication(context, params);
                Engine.getJobs().add(new EFTJob(event));
                Timber.i("APP_LOGON_EVENT Event Received");
            }                } else {
            Timber.e("%s Event received but intent has no data", APP_LOGON_EVENT );
        }

    }

    private void handleActionAppConfigEvent(Intent intent, Context context, Gson gson, IAppCallbacks iAppCallbacks) {
        if (intent.hasExtra(SERVICE_EVENT)) {
            PositiveEvent event = gson.fromJson(intent.getStringExtra(SERVICE_EVENT), PositiveEvent.class);

            Timber.i(APP_CONFIG_EVENT);
            if (event != null) {
                Timber.d("Received new config from connect app");
                Timber.d("Terminal ID = %s", event.getTerminalId());
                Timber.d("Merchant ID = %s", event.getMerchantId());
                // We don't care about NII or AIIC

                final PositiveEvent.EventType eventType = event.getType();
                final StartupParams params = new StartupParams(
                        eventType.backgroundTask,
                        eventType.hideWhenDone,
                        eventType.hideWhenDone,
                        eventType.showSplashScreen,
                        true
                );

                iAppCallbacks.runApplication(context, params);
                Engine.getJobs().add(new EFTJob(event));
            }
        }
    }

    private void handleActionPosKeyEvent(Intent intent, Gson gson) {
        if (intent.hasExtra(SERVICE_EVENT)) {
            PositiveTransEvent event = gson.fromJson(intent.getStringExtra(SERVICE_EVENT), PositiveTransEvent.class);
            Timber.i(POS_KEY_EVENT);
            Engine.getJobs().add(new EFTJob(event));
        }
    }

    private void handleAirplaneModeEvent(Intent intent) {
        boolean airplaneModeEnabled = intent.getBooleanExtra("state", true);
        Timber.i("Airplane mode changed. Enabled?%b", airplaneModeEnabled);
        if (!airplaneModeEnabled) {
            performBatchUpload();
        }
    }

    private void handleActionDefault(Intent intent, Context context, Gson gson, IAppCallbacks iAppCallbacks) {
        // Something happened in the LinklyService
        Timber.i("Alternative service event = %s", intent.getAction() );
        try {
            String eventJson = intent.getStringExtra(SERVICE_EVENT);
            PositiveEvent event = gson.fromJson(eventJson, PositiveEvent.class);

            if (event != null) {
                Timber.i("LAUNCH APP FOR SERVICE EVENT: %s", event.getType().name());
                final PositiveEvent.EventType eventType = event.getType();
                iAppCallbacks.runApplication(context,
                        new StartupParams(
                                eventType.backgroundTask,
                                eventType.hideWhenDone,
                                eventType.hideWhenDone,
                                eventType.showSplashScreen,
                                true));

                // for auto start just doing the launch above is enough
                // For auto rec we dont do these anymore for Optomany
                if (eventType != AUTO_START) {
                    Engine.getJobs().add(new EFTJob((eventType)));
                }
            }
        } catch (Exception e) {
            Timber.w(e);
        }
    }

    private void performBatchUpload() {
        EFTJob job = new EFTJob(PositiveEvent.EventType.BATCH_UPLOAD);
        Engine.getJobs().add(job);
    }

    public static int getBatteryPercentage(Intent intent) {
        int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        Timber.i("level:" + level + " scale:" + scale);
        return (int)(level * 100/ (float)scale);
    }

}
