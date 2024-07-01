package com.linkly.libpositivesvc.downloader.download;

import com.linkly.libmal.MalFactory;
import com.linkly.libmal.global.platform.EFTPlatform;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import timber.log.Timber;

public class ResourceInstaller {

    private static final ResourceInstaller ourInstance = new ResourceInstaller();
    private static final String TAG = "ResInstaller";
    private HashMap<String, AppResource> knownRes;
    private ResourceInstaller() {
        initAppFileSystem();
        knownRes = new HashMap<String, AppResource>();

        //Add Known Resources to the KnownResource Map in format filename and install destinantion
        knownRes.put("downloadmanager", new AppResource(RES_TYPE.RES_CONFIG, "downloadmanager", "download.xml", MalFactory.getInstance().getFile().getWorkingDir()));
        knownRes.put("paymenttmp", new AppResource(RES_TYPE.RES_CONFIG, "paymenttmp", "payment.xml", MalFactory.getInstance().getFile().getWorkingDir()));
        knownRes.put("apay-", new AppResource(RES_TYPE.RES_APP, "apay", "apay.apk", null));
        knownRes.put("apay_", new AppResource(RES_TYPE.RES_APP, "apay", "apay.apk", null));
        knownRes.put("launcher-", new AppResource(RES_TYPE.RES_APP, "launcher", "launcher.apk", null));
        knownRes.put("reszip", new AppResource(RES_TYPE.RES_ZIP, "--", "-", null));
        knownRes.put("paxsdk", new AppResource(RES_TYPE.RES_APP, "paxsdk", "paxsdk.apk", null));
        knownRes.put("paydroiddiff-", new AppResource(RES_TYPE.RES_OS_DIFF, "paydroiddiff", "paydroiddiff.zip", null));
        knownRes.put("paydroidfull-", new AppResource(RES_TYPE.RES_OS_FULL, "paydroidfull", "paydroidfull.zip", null));
        //This allows the installation of any APK
        knownRes.put("apk", new AppResource(RES_TYPE.RES_APP, "--", "--", null));

    }

    public static ResourceInstaller getInstance() {
        return ourInstance;
    }

    /*
     * Checks for Directories and Creates any required by known resources
     * */
    private void initAppFileSystem() {
        String basePath = MalFactory.getInstance().getFile().getWorkingDir();
        String cfgPath = basePath + "/cfg";

        File f = new File(cfgPath);
        /*MalConfig Path - Check it exists if not, create it*/
        if (!f.exists()) {
            boolean success = (new File(cfgPath)).mkdirs();
            if (!success) {
                // Directory creation failed
                Timber.i( "Failed to Create MalConfig Dir ");
            }
        }


        String installerPath = MalFactory.getInstance().getFile().getInstallDir();

        File f2 = new File(installerPath);
        /*Installer Path - Check it exists if not, create it*/
        if (!f2.exists()) {
            boolean success = (new File(installerPath)).mkdirs();
            if (!success) {
                // Directory creation failed
                Timber.i( "Failed to Create Installer Dir ");
            }
        }


    }

    public INSTALL_RESULT installResource(String newResourcePath) {
        INSTALL_RESULT result = INSTALL_RESULT.RES_UNKNOWN;
        String resName = newResourcePath.substring(newResourcePath.lastIndexOf("/") + 1);

        if (resName.contains("apay-") || resName.contains("apay_")) {
            resName = "apay-";
        }


        if (resName.contains("launcher-")) {
            resName = "launcher-";
        }

        // for OS (Paydroid) updates - either differential or full
        if (resName.contains("paydroiddiff-") || resName.contains("paydroiddiff_")) {
            resName = "paydroiddiff-";
        } else if (resName.contains("paydroidfull-") || resName.contains("paydroidfull_")) {
            resName = "paydroidfull-";
        }

        AppResource resToInstall = knownRes.get(resName);

        if (resToInstall == null) {
            /*Check For Generic RES*/
            if (newResourcePath.endsWith(".zip")) {
                resToInstall = knownRes.get("reszip");
            }

            /*Install All APK's*/
            if (newResourcePath.contains(".apk")) {
                resToInstall = knownRes.get("apk");
            }

        }


        if (resToInstall != null) {
            if (resToInstall.getType() == RES_TYPE.RES_CONFIG) {
                result = installConfig(resToInstall, newResourcePath);
            } else if (resToInstall.getType() == RES_TYPE.RES_APP) {
                result = installApp(resToInstall, newResourcePath);
            } else if (resToInstall.getType() == RES_TYPE.RES_ZIP) {
                result = installZIP(resToInstall, newResourcePath);
            } else if (resToInstall.getType() == RES_TYPE.RES_OS_DIFF) {
                result = installOS(resToInstall, newResourcePath);
            } else if (resToInstall.getType() == RES_TYPE.RES_OS_FULL) {
                result = installOS(resToInstall, newResourcePath);
            }

        } else {
            Timber.i( "Unknown Resource : " + resName);


            result = INSTALL_RESULT.RES_UNKNOWN;
        }


        return result;
    }

    private INSTALL_RESULT installConfig(AppResource res, String newResourcePath) {
        INSTALL_RESULT result = INSTALL_RESULT.RES_INSTALL_FAILED;
        String newDestPath = res.getInstallPath() + "/" + res.getInstalledName();
        Timber.i( "Installing MalConfig  : " + res.getResName());

        /*Check install Directory, to see if it already contains a files of this packageName */
        if (MalFactory.getInstance().getFile().fileExist(newDestPath)) {
            /*File Exists, Backup the file*/
            MalFactory.getInstance().getFile().copyFile(newDestPath, newDestPath + ".bak");
        }

        /*Copy new resource to Target Dest*/
        Timber.i( "Copy file: " + newResourcePath + " to " + res.getInstallPath() + "/" + res.getInstalledName());
        if (MalFactory.getInstance().getFile().copyFile(newResourcePath, newDestPath)) {
            result = INSTALL_RESULT.RES_INSTALLED;
        }

        return result;
    }

    private boolean attemptDirectInstall(File file) {
        Timber.i( "Sending OS Request To Install " + file.getName());
        Timber.i( "Absolute Path:" + file.getAbsolutePath());

        try {
            Runtime.getRuntime().exec("chmod 766 " + file);
        } catch (IOException e) {
            Timber.w(e);
        }

        if (!file.getName().contains("eftinst")) {
            Timber.i( "We don't install this direct we use eftinstaller: " + file.getName());
            return false;
        }

        if (MalFactory.getInstance().getHardware().installApp(file) == 0)
            return true;
        return false;
    }
    /*******************
     * This Function Copies the downloaded apk files to the
     * Installer DDirect
     * @param res
     * @param newResourcePath
     * @return
     */
    private INSTALL_RESULT installApp(AppResource res, String newResourcePath) {
        String path = MalFactory.getInstance().getFile().getInstallDir() + newResourcePath.substring(newResourcePath.lastIndexOf('/'));

        File f = new File(newResourcePath);
        Timber.i("File Size: " + f.length());

        if (f.length() > 0) {
            Timber.i("File Size: " + f.length() + " (Attempting to install)");
            if ( attemptDirectInstall(f))
                return INSTALL_RESULT.RES_INSTALLED;
            else {
                //Copy the File to Install to the Install on Boot Dir
                MalFactory.getInstance().getFile().copyFile(newResourcePath, path);
            }

            return INSTALL_RESULT.RES_INSTALLED;

        } else {
            return INSTALL_RESULT.RES_INSTALL_FAILED;
        }
    }

    private INSTALL_RESULT installZIP(AppResource res, String newResourcePath) {
        //TODO - Something with a zip file..
        MalFactory.getInstance().getFile().getCommonDir();

        try {
            String fileNamePath ;
            byte[] buffer = new byte[1024];
            ZipInputStream zis = new ZipInputStream(new FileInputStream(newResourcePath));
            ZipEntry zipEntry = zis.getNextEntry();
            while (zipEntry != null) {
                String fileName = zipEntry.getName();
                fileNamePath = MalFactory.getInstance().getFile().getInstallDir() + "/" + fileName;
                File newFile = new File(fileNamePath);
                FileOutputStream fos = new FileOutputStream(newFile);
                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
                fos.close();

                /*If the Zip Contains an apk then Install the APK*/
                if(newFile.getName().endsWith(".apk")){
                    installResource(fileNamePath);
                }

                zipEntry = zis.getNextEntry();
            }
            zis.closeEntry();
            zis.close();
        } catch (Exception e) {
            Timber.w(e);
        }

        return INSTALL_RESULT.RES_INSTALLED;
    }

    // TODO: what about "multi-diff" packages ?  (i.e. few  OTA_DIFF packages as one file - this is still less data than one full download, but will need multiple reboots to install fully) -
    // this is just an idea for a future improvement
    private INSTALL_RESULT installOS(AppResource res, String newResourcePath) {

        if (EFTPlatform.isPaxTerminal()) {
            String path = MalFactory.getInstance().getFile().getInstallDir() + newResourcePath.substring(newResourcePath.lastIndexOf('/'));
            File f = new File(newResourcePath);
            Timber.i("File Size: " + f.length());

            if (f.length() > 0) {
                /*Copy the File to Install to the Install on Boot Dir*/
                MalFactory.getInstance().getFile().copyFile(newResourcePath, path);
                f = new File(path);
                Timber.i( "File Size: " + f.length());
                return INSTALL_RESULT.RES_INSTALLED;
            } else {
                return INSTALL_RESULT.RES_INSTALL_FAILED;
            }
        }

        return INSTALL_RESULT.RES_INSTALLED;
    }


    public enum INSTALL_RESULT {
        RES_INSTALLED,
        RES_UNKNOWN,
        RES_INSTALL_FAILED,
    }

    public enum RES_TYPE {
        RES_CONFIG,
        RES_APP,
        RES_ZIP,
        RES_OS_DIFF,    // OS - differential upgrade package
        RES_OS_FULL     // OS - full install package
    }

    private class AppResource {
        private RES_TYPE type;
        private String resName;
        private String installedName;
        private String installPath;

        public AppResource(RES_TYPE type, String name, String installedName, String path) {
            this.type = type;
            this.resName = name;
            this.installedName = installedName;
            this.installPath = path;
        }

        public RES_TYPE getType() {
            return this.type;
        }

        public String getResName() {
            return this.resName;
        }

        public String getInstalledName() {
            return this.installedName;
        }

        public String getInstallPath() {
            return this.installPath;
        }
    }


}
