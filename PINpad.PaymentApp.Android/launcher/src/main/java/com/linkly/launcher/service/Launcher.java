package com.linkly.launcher.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.linkly.launcher.applications.AppMgr;
import com.linkly.libconfig.MalConfig;
import com.linkly.libconfig.ProfileCfg;
import com.linkly.libmal.IMalFile;
import com.linkly.libmal.MalFactory;
import com.linkly.libmal.global.util.Util;
import com.linkly.libpositive.events.PositiveScheduledEvent;
import com.linkly.libpositive.messages.Messages;
import com.linkly.libpositivesvc.POSitiveSvcCheck;
import com.linkly.libpositivesvc.paxstore.DownloadParamService;
import com.linkly.libsecapp.P2PLib;
import com.linkly.libui.UI;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.Date;
import java.util.List;

import timber.log.Timber;

public class Launcher extends Service {
    private LauncherServiceThread service;
    private boolean initialised = false;
    public static final boolean READY_TO_UPDATE = true;
    public static final String PROFILE_XML_FILENAME = "profile.xml";

    @Override
    public void onDestroy() {
        if(service != null) {
            // Interrupt our service thread as it's an "infinite loop"
            service.interrupt();
            service = null;
        }

        super.onDestroy();
        LauncherUtils.POSitiveSvcNotify(getApplicationContext(), "Service Ended!!");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!initialised) {
            MalFactory.getInstance().initialiseMal(getApplicationContext());
            UI.getInstance().initialiseUI(null);
            P2PLib.InitiateService(getApplicationContext());
            initialised = true;
        }

        //Check if this is a restart -
        Timber.i("Service Started : %s", UI.getInstance().getVersion());

        //Start the Service Task
        if (service == null) {
            service = new LauncherServiceThread();
            service.start();
            LauncherUtils.POSitiveSvcNotify(getApplicationContext(), "Started");

        }
        return Service.START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /*Below we will add the Main Logic of the eftlogo Service*/
    private class LauncherServiceThread extends Thread {
        @Override
        public void run() {
            LauncherUtils.POSitiveSvcNotify(getApplicationContext(), "Loading Config");
            boolean bNewProfile = checkForNewResources();

            /* load the initial config */
            MalConfig.getInstance().loadConfig();

            /* load the list of apps we care about */
            AppMgr.getInstance().loadApps(getApplicationContext());
            configureApns(bNewProfile);
            launcherServiceMain();
        }

        @SuppressWarnings("java:S2189") // supress endless loop warning
        private void launcherServiceMain() {
            int rebootCycle = 0;

            try {
                Context context = getApplicationContext();
                //To Do Add the Tasks that should be done in the background.
                //Note: Cant Bind too tightly to app as we might want to restart the app if its crashed or other similar actions

                //Auto Rec
                //Check for new Software
                //Start Sale TransRec
                //Launch App if not running (including in Unattended Mode*!)
                // * Limited launching takes place for Unattended Mode.
                AppMgr.getInstance().autoStartApps(context, false);

                while (true) {
                    Thread.sleep(1000);

                    /*We Dont Need to Check Every Second */
                    if (rebootCycle >= 60) {
                        rebootCycle = 0;
                        autoRebootSchedule();
                        System.gc();
                    } else {
                        Util.DisplayMemoryUsage("Launcher");
                        rebootCycle++;
                    }
                    processScheduledTasks();
                }
            } catch (InterruptedException interruptedException) {
              Timber.e("Launcher Service Main Interrupted.");
              // Nothing we need to do except interrupt our current thread.
                Thread.currentThread().interrupt();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        private void configureApns(boolean bNewProfile) {
            if (!bNewProfile)
                return;

            Timber.i("New profile.xml found");
            ProfileCfg p = MalConfig.getInstance().getProfileCfg();

            if (p != null) {
                List<ProfileCfg.ApnCfg>  apns = p.getApns();
                if (apns != null) {
                    for (ProfileCfg.ApnCfg c : apns) {
                        addApns(c.getName(), c.getApn(), c.getUser(), c.getPwd());
                    }
                }
            }

            IMalFile iFile = MalFactory.getInstance().getFile();
            String profileCfg = getFullPath(iFile.getCommonDir(), PROFILE_XML_FILENAME);

            if (iFile.fileExist(profileCfg)) {
                Timber.i("New profile.xml installed");
            }
        }

        private void processScheduledTasks() {
            if (LauncherController.getScheduledEventList() != null) {
                if (LauncherController.getScheduledEventList().size() > 0) {
                    PositiveScheduledEvent event = LauncherController.getScheduledEventList().get(0);

                    if (event != null) {
                        Date now = new Date();
                        // Check this events trigger time, to see if we must now trigger
                        if (now.getTime() >= event.getTriggerTime()) {
                            // This Event has now expired
                            Timber.i("Scheduled Task - %s", event.getAction());
                            Messages.getInstance().sendScheduledEventTrigger(getApplicationContext(), event);
                            LauncherController.getScheduledEventList().remove(0);
                        }
                    }
                }
            }
        }

        private void autoRebootSchedule() {
            if (ProfileCfg.getInstance().isDemo() || MalConfig.getInstance().getDownloadCfg() == null || MalConfig.getInstance().getDownloadCfg().getAutoRecon() == null)
                return;

            if (POSitiveSvcCheck.EFTCheckAutoRebootSchedule(getApplicationContext(), MalConfig.getInstance().getDownloadCfg().getAutoRecon().getTime())) {
                Messages.getInstance().sendAutoRebootRequest(getApplicationContext());
                LauncherUtils.POSitiveSvcNotify(getApplicationContext(), "Auto Reboot Run");
            }
        }

        public String md5(InputStream is) {
            String md5 = "";
            char[] hexDigits = "0123456789abcdef".toCharArray();
            try {
                byte[] bytes = new byte[4096];
                int read;
                MessageDigest digest = MessageDigest.getInstance("MD5");

                while ((read = is.read(bytes)) != -1) {
                    digest.update(bytes, 0, read);
                }

                byte[] messageDigest = digest.digest();

                StringBuilder sb = new StringBuilder(32);

                for (byte b : messageDigest) {
                    sb.append(hexDigits[(b >> 4) & 0x0f]);
                    sb.append(hexDigits[b & 0x0f]);
                }

                md5 = sb.toString();
            } catch (Exception e) {
                Timber.w(e);
            }

            return md5;
        }

        private boolean doFilesMatch(String file1, String file2) {
            try {
                String md5Checksum1 = md5(new FileInputStream(file1));
                String md5Checksum2 = md5(new FileInputStream(file2));

                if (md5Checksum1.equals(md5Checksum2)) {
                    //file is same
                    return true;
                }
            } catch (Exception e) {
                Timber.w(e);
            }
            return false;
        }

        private boolean checkForNewResources() {
            IMalFile iFile = MalFactory.getInstance().getFile();
            boolean bNewConfig = false;
            boolean bNewProfile = false;

            if( moveFromCommonToWorkingDir("payment.xml")){
                Timber.i("New Payment File Exists");
                bNewConfig = true;
            }

            if( moveFromCommonToWorkingDir("download.xml")){
                Timber.i("New Download File Exists");
                bNewConfig = true;
            }

            String profileCfg = getFullPath(iFile.getCommonDir(), PROFILE_XML_FILENAME);
            String newProfileCfg = getFullPath(iFile.getWorkingDir(), PROFILE_XML_FILENAME);

            if (iFile.fileExist(profileCfg)) {
                Timber.i("New Profile File Exists");

                bNewProfile = true;
                if (iFile.fileExist(newProfileCfg)) {
                    if (doFilesMatch(profileCfg, newProfileCfg)) {
                        bNewProfile = false; /* guess its not a new one, since its the same */
                    }
                }
                if(iFile.copyFile(profileCfg, getFullPath(iFile.getWorkingDir(), "/"+PROFILE_XML_FILENAME))) {
                    bNewConfig = true;
                }
            }

            // we used to have a common directory that everyone could use (EFT)
            // and a working directory that was local (files)
            // we only need one directory now to stop the confusion
            // every app has a woring directory (the root), this is where we open files from
            // then there is a list of files that we give to other apps // this is in the common directory
            DownloadParamService.loadNewResources();

            String[] fileList = {
                    "header.png",
                    "splashlogo.png",
                    "screensaver.png",
                    "receipt.bmp"
            };

            for( String file: fileList ) {
                /*Check for Branding files loaded via USB Loader*/
                if (moveFromWorkingToCommonDir(file)) {
                    bNewConfig = true;
                }
            }

            if (bNewConfig) {
                LauncherUtils.POSitiveSvcNotify(getApplicationContext(), "New Config Found");
            }

            return bNewProfile;
        }

        private String getFullPath(String folder, String filename){
            return String.format("%s/%s", folder, filename);
        }

        @SuppressWarnings({"java:S899", "java:S4042"}) // ignore delete return value
        private boolean moveFromCommonToWorkingDir(String filename){
            IMalFile iFile = MalFactory.getInstance().getFile();
            String filenameFull = getFullPath(iFile.getCommonDir(), filename);

            if (iFile.fileExist(filenameFull) && iFile.copyFile(filenameFull, getFullPath(iFile.getWorkingDir(), filename))) {
                /*Remove the Common file*/
                new File(filenameFull).delete();
                return true;
            }
            return false;
        }

        @SuppressWarnings({"java:S899", "java:S4042"}) // ignore delete return value
        private boolean moveFromWorkingToCommonDir(String filename){
            IMalFile iFile = MalFactory.getInstance().getFile();
            String filenameFull = getFullPath(iFile.getWorkingDir(), filename);

            if (iFile.fileExist(filenameFull) && iFile.copyFile(filenameFull, getFullPath(iFile.getCommonDir(), filename))) {
                /*Remove the Common file*/
                new File(filenameFull).delete();
                return true;
            }
            return false;
        }

        private void addApns(String apnName, String apn, String user, String password) {
            if (apnName != null && apn != null && user != null && password != null) {

                Timber.i("switchAPN using " + apnName + ": APN=" + apn + " USER=" + user + " PASS=" + password);
                int ret = MalFactory.getInstance().getHardware().getDal().getCommManager().switchAPN( apnName, apn, user, password, 3 );/* 3 = PAP or CHAP */
                Timber.i("switchAPN returned %s", ret);
            }
        }
    }
}
