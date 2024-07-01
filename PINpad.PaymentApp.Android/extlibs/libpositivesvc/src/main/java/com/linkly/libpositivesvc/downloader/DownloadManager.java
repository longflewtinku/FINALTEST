package com.linkly.libpositivesvc.downloader;

import com.linkly.libmal.IMalFile;
import com.linkly.libmal.MalFactory;
import com.linkly.libmal.global.config.EnvCfg;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import timber.log.Timber;


/***************************************************************
 *  HOW IT WORKS -
 *  - Files are all downloaded into the /data/data/../downloads directory
 *  - While incomplete the files have the suffix .prt
 *  - Once the download of a file is completed the .prt is removed
 *  - Downloads will not start if a file exists in the /downloads directory (.i.e. the none .prt file), the download object will report the download as completed
 *  - Once all file downloads are completed the resource installer will perform the correct install routine
 *      - .apk files will be copied to the /Installer director for the EFT Installer App to perform the actual install. This is trigger once all other Resource installer tasks are completed
 *      - .xml files will be copied to the correct paths, which are defined at the head of the ResourceInstaller Class. (all configs are local to the service so no need for the shared folder)
 *
 *
 */

public class DownloadManager {
    private static final String TAG = "launcher.DownloadMan";
    /*Keys for the Configs stored for this Module*/
    private static final String SYSDIGESTCFG = "dmgr.SysDigest";
    private static final String sysLastDownloadDateCfg = "dmgr.dldDate";
    private static final String sysLastDownloadResultCfg = "dmgr.dldResult";
    private static final String sysLastDownloadReasonCfg = "dmgr.dldReason";
    private static DownloadManager instance = null;
    private static boolean inProgress = false;
    private String remoteDigest;        //Created as a "global" so we dont need to make multiple requests


    public DownloadManager() {
        super();

        InitFileSystem();

        // customize if needed

        // is there any Paydroid package left from the last installation (- it is not deleted automatically)
        CheckPaydroidUpdateToBeRemoved();
    }

    public static DownloadManager getInstance() {
        if (instance == null) {
            instance = new DownloadManager();
        }
        return instance;
    }

    private void CheckPaydroidUpdateToBeRemoved() {
        Timber.i( "CheckPaydroidUpdateToBeRemoved");
        String instFilePath =  MalFactory.getInstance().getFile().getInstallDir();

        /*Get List Of Files*/
        File instDir = new File(instFilePath);
        File[] filesToInstall = instDir.listFiles();

        if (filesToInstall != null && filesToInstall.length > 0) {
            Timber.i("Files Found In Install Dir " + filesToInstall.length);
            IMalFile malFile = MalFactory.getInstance().getFile();

            for (File fileNow : filesToInstall) {
                // if we get Paydroid on the list - store it so it will be processed as the last item
                if (fileNow.getName().toLowerCase().startsWith("paydroid")) {
                    String fullPath = instFilePath + "/" + fileNow.getName();
                    Timber.i( "Paydroid found - removing; file:" + fullPath);
                    malFile.deleteFile(fullPath);
                }
            }
        }
    }


    public static void setLastDownloadDate(String downloadRes, String reason) {
        Date date = new Date();

        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        String dateString = sdf.format(date);

        EnvCfg.getInstance().storeValue(sysLastDownloadDateCfg, dateString);
        EnvCfg.getInstance().storeValue(sysLastDownloadResultCfg, downloadRes);
        EnvCfg.getInstance().storeValue(sysLastDownloadReasonCfg, reason);

    }


    public void InitFileSystem() {
        /*Check if download DIR exist*/
        String downloadDir = MalFactory.getInstance().getFile().getWorkingDir() + "/downloads";
        String digestDir = MalFactory.getInstance().getFile().getCommonDir() + "/digests";

        if (MalFactory.getInstance().getFile().fileExist(downloadDir) == false) {
            //Create the download Directorys
            File dldDir = new File(downloadDir);
            dldDir.mkdirs();
        }

        if (MalFactory.getInstance().getFile().fileExist(digestDir) == false) {
            //Create the download Directorys
            File digDir = new File(digestDir);
            digDir.mkdirs();
        }

    }

    public enum DOWNLOAD_REQUEST_TYPE {
        DOWNLOAD_GENTLE,
        DOWNLOAD_FORCE,
    }


    public enum DOWNLOAD_UPDATE_RESULT {
        NOT_REQUIRED,
        USER_CANCELLED,
        UPDATE_FAILED,
        UPDATE_SUCCESS,
        UPDATE_BUSY,

    }

}
