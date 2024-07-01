package com.linkly.libpositivesvc.downloader.download;

import android.app.NotificationManager;
import android.content.Context;

import androidx.core.app.NotificationCompat;

import com.linkly.libmal.MalFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import timber.log.Timber;

public class Download {
    private final static String TAG = "DOWNLOAD";
    static private int glbNotificationId = 0;

    final private int CONNECT_TIMEOUT = (60 * 1000);
    private URL downloadURL;
    private String downloadDir;
    private String localName;
    private String localFilePath;
    private DOWNLOAD_STATUS download_status;
    private Thread downloadThread;
    private boolean isCancelled;
    private int fileSizeOnServer;
    private int bytesDownloaded;
    private int tryCount;
    private int maxRetryCount;
    private String displayName;
    /*Manifest Specific Values Not required, just used to keep things in once place -- Should Probably extend the download class to add these*/
    private String maniName;
    private String maniType;
    private String maniVersion;
    private String fileDigest;
    /*This is the Name of the file used while the file is downloading. i.e. in progress*/
    private String localTmpName;
    private String localTmpFilePath;
    /*Notification Progress Display */
    private NotificationManager mNotifyManager;
    private NotificationCompat.Builder mBuilder;
    private int notificationId = 0;

    public Download(String url, String localName) {
        try {

            /*Check if download DIR exist*/
            downloadDir = MalFactory.getInstance().getFile().getWorkingDir() + "/downloads";
            tryCount = 0;
            maxRetryCount = 3;
            if (!MalFactory.getInstance().getFile().fileExist(downloadDir)) {
                //Create the download Directorys
                File dldDir = new File(downloadDir);
                dldDir.mkdirs();
            }


            this.downloadURL = new URL(url);
            this.localName = localName;
            this.localTmpName = localName + ".prt";
            this.download_status = DOWNLOAD_STATUS.DOWNLOAD_INIT;
            isCancelled = false;
            localFilePath = downloadDir + "/" + localName;
            localTmpFilePath = downloadDir + "/" + localTmpName;
        } catch (Exception ex) {
            this.download_status = DOWNLOAD_STATUS.DOWNLOAD_FAILED;
        }

    }

    public Download(String url) {
        try {

            /*Check if download DIR exist*/
            downloadDir = MalFactory.getInstance().getFile().getWorkingDir() + "/downloads";
            tryCount = 0;
            maxRetryCount = 3;

            if (MalFactory.getInstance().getFile().fileExist(downloadDir) == false) {
                //Create the download Directorys
                File dldDir = new File(downloadDir);
                dldDir.mkdirs();
            }
            this.downloadURL = new URL(url);

            this.download_status = DOWNLOAD_STATUS.DOWNLOAD_INIT;
            isCancelled = false;

            localName = url.substring(url.lastIndexOf("/") + 1);

            if(localName.contains("?")){
                localName = localName.substring(0,localName.indexOf('?'));
            }

            localFilePath = downloadDir + "/" + localName;
            this.localTmpName = localName + ".prt";
            localTmpFilePath = downloadDir + "/" + localTmpName;
        } catch (Exception ex) {
            this.download_status = DOWNLOAD_STATUS.DOWNLOAD_FAILED;
        }

    }

    public void startDownload() {
        startDownload(true);
    }

    public void startDownload(boolean showProgres) {
        /*Only Allow One Download With a instance*/
        if (downloadThread != null) {
            if (downloadThread.getState() != Thread.State.TERMINATED) {
                return;
            }
        }

        /*Check if this File has already been downloaded*/
        if (MalFactory.getInstance().getFile().fileExist(localFilePath)) {
            this.download_status = DOWNLOAD_STATUS.DOWNLOAD_COMPLETE;
            return;
        }

        if (showProgres) {
            startProgressNotification();
        }
        bytesDownloaded = 0;

        downloadThread = new Thread(new Runnable() {

            private DOWNLOAD_STATUS attemptDownload() {
                isCancelled = false; //Make sure Cancelled flag is cleared
                try {
                    boolean isResume = true;
                    boolean isSSL = false;
                    tryCount++;
                    download_status = DOWNLOAD_STATUS.DOWNLOAD_COMMS_INITING;
                    /*Make sure we can connect to the network*/
                    boolean bOpen = true; //TODO:  MalFactory.getInstance().getComms().open();

                    if (isCancelled) {
                        download_status = DOWNLOAD_STATUS.DOWNLOAD_COMMS_CANCELLED;
                        return download_status;
                    }

                    if (bOpen) {
                        download_status = DOWNLOAD_STATUS.DOWNLOAD_STARTING;
                        /*Create The Local File*/

                        FileOutputStream fileOutput = new FileOutputStream(localTmpFilePath, true);


                        if (isCancelled) {
                            download_status = DOWNLOAD_STATUS.DOWNLOAD_COMMS_CANCELLED;
                            return download_status;
                        }

                        String protocol = downloadURL.getProtocol();
                        if ("https".equalsIgnoreCase(protocol)) {
                            isSSL = true;
                        }


                        /*Establish Connection to Server*/
                        HttpURLConnection urlConnection;
                        if (isSSL) {
                            urlConnection = (HttpsURLConnection) downloadURL.openConnection();
                        } else {
                            urlConnection = (HttpURLConnection) downloadURL.openConnection();
                        }

                        urlConnection.setRequestMethod("GET");


                        /*Check if the File Already Exists and Is about to Resume*/
                        if (fileOutput.getChannel().size() > 0) {
                            isResume = true;
                            bytesDownloaded = (int) fileOutput.getChannel().size();
                            urlConnection.setRequestProperty("Range", "bytes=" + bytesDownloaded + "-");
                        } else {
                            bytesDownloaded = 0;
                        }

                        try {
                            urlConnection.disconnect();


                            urlConnection.setDoInput(true);
                            urlConnection.setDoOutput(false);
                            urlConnection.setRequestProperty("Accept-Encoding", "*");
                        } catch (Exception ex){
                            Timber.w(ex);
                        }

                        urlConnection.setConnectTimeout(CONNECT_TIMEOUT);
                        Timber.i( " Connecting ");
                        urlConnection.connect();
                        int respCode =  urlConnection.getResponseCode();

                        Timber.i( "Resp Code : " + respCode + " - " + urlConnection.getURL().toString());
                        Timber.i( " Connect Done ");

                        if(respCode != 200){
                            Timber.i("HTTP Error");
                            download_status = DOWNLOAD_STATUS.DOWNLOAD_FAILED;
                            urlConnection.disconnect();
                            fileOutput.close();
                            return download_status;
                        }


                        if (isCancelled) {
                            download_status = DOWNLOAD_STATUS.DOWNLOAD_COMMS_CANCELLED;
                            urlConnection.disconnect();
                            fileOutput.close();
                            return download_status;
                        }

                        /*Get File Size on Server*/
                        Timber.i( " Get File Size On Server");
                        fileSizeOnServer = urlConnection.getContentLength();
                        Map<String, List<String>> map = urlConnection.getHeaderFields();
                        if (fileSizeOnServer == -1) {
                            String clen = urlConnection.getHeaderField("Content-Length");
                            if (clen != null) {
                                fileSizeOnServer = Integer.parseInt(clen);
                            }
                        }

                        if (fileSizeOnServer != -1 && isResume) {
                            fileSizeOnServer += bytesDownloaded;
                        }
                        urlConnection.setConnectTimeout(5000);
                        Timber.i( " Proceed With Download ");
                        /*Read the File From Remote Server */
                        InputStream inputStream = urlConnection.getInputStream();
                        byte[] buffer = new byte[1024];
                        int bufferLength = 0;


                        download_status = DOWNLOAD_STATUS.DOWNLOAD_IN_PROGRESS;

                        while ((bufferLength = inputStream.read(buffer)) > 0) {
                            fileOutput.write(buffer, 0, bufferLength);
                            bytesDownloaded += bufferLength;
                            if (isCancelled) {
                                download_status = DOWNLOAD_STATUS.DOWNLOAD_COMMS_CANCELLED;
                                urlConnection.disconnect();
                                fileOutput.close();
                                return download_status;
                            }
                        }
                        urlConnection.disconnect();
                        fileOutput.close();

                        /*Rename the Tmp File and Create the Real File*/
                        MalFactory.getInstance().getFile().deleteFile(localFilePath);
                        MalFactory.getInstance().getFile().copyFile(localTmpFilePath, localFilePath);
                        MalFactory.getInstance().getFile().deleteFile(localTmpFilePath);

                        download_status = DOWNLOAD_STATUS.DOWNLOAD_COMPLETE;
                    } else {
                        //Download Failed Because Comms Failed to Start
                        download_status = DOWNLOAD_STATUS.DOWNLOAD_FAILED_COMMS;
                    }
                } catch (SocketTimeoutException soc) {
                    Timber.w(soc);
                    download_status = DOWNLOAD_STATUS.DOWNLOAD_FAILED_COMMS;
                    isCancelled = true;
                } catch (UnknownHostException host) {
                    download_status = DOWNLOAD_STATUS.DOWNLOAD_FAILED_COMMS;
                    isCancelled = true;
                } catch (Exception e) {
                    Timber.w(e);

                    download_status = DOWNLOAD_STATUS.DOWNLOAD_FAILED;
                    isCancelled = true;
                }

                return download_status;
            }

            public void run() {
                do {
                    DOWNLOAD_STATUS status = attemptDownload();
                    if (status == DOWNLOAD_STATUS.DOWNLOAD_COMPLETE || status == DOWNLOAD_STATUS.DOWNLOAD_FAILED) {
                        break;
                    }

                    //Prep to retry the download...
                    if (status != DOWNLOAD_STATUS.DOWNLOAD_FAILED_COMMS) {
                        //Only Retry when comms Failures.
                        break;
                    }

                } while (tryCount < maxRetryCount);
            }
        });

        downloadThread.start();
    }

    public void cancelDownload() {
        if (downloadThread != null) {
            isCancelled = true;
            this.download_status = DOWNLOAD_STATUS.DOWNLOAD_COMMS_CANCELLING;
        }
    }

    public int getDownloadProgress() {
        int percentComplete = 0;
        if (download_status == DOWNLOAD_STATUS.DOWNLOAD_IN_PROGRESS) {
            //Work out the progress
            if (fileSizeOnServer > 0) {
                percentComplete = (int) (((100.0 / fileSizeOnServer)) * ((double) bytesDownloaded));
            }
        } else if (download_status == DOWNLOAD_STATUS.DOWNLOAD_COMPLETE) {
            percentComplete = 100;
        }

        return percentComplete;
    }

    /**
     * Deletes any partial download for this file
     */
    public void clearDownloadCache() {
        if (localTmpFilePath == null) {
            return;
        }

        File cacheFile = new File(localTmpFilePath);
        if (!cacheFile.delete()) {
            Timber.i("Failed To Deleted Cache - " + localTmpFilePath);
        }

        cacheFile = new File(localFilePath);
        if (!cacheFile.delete()) {
            Timber.i("Failed To Deleted Cache" + localFilePath);
        }

    }

    public void startProgressNotification() {
        String fileName = maniName;
        notificationId = glbNotificationId++;

        /*Setup the Name to display in the notification */
        if (maniName == null) {
            fileName = this.displayName;
        }

        mNotifyManager = (NotificationManager) MalFactory.getInstance().getMalContext().getSystemService(Context.NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(MalFactory.getInstance().getMalContext(), "");
        mBuilder.setContentTitle(fileName)
                .setContentText("Download Starting")
                .setSmallIcon(android.R.color.transparent);
        new Thread(
                new Runnable() {
                    @Override
                    public void run() {

                        int prog = getDownloadProgress();
                        while (prog < 100) {
                            /*Update the progress bar in the notification*/
                            prog = getDownloadProgress();
                            mBuilder.setProgress(100, prog, false);
                            mBuilder.setContentText("In Progress: " + prog + "%");
                            mNotifyManager.notify(notificationId, mBuilder.build());
                            /*Sleep while the App has chance to do some downloading*/
                            try {
                                Thread.sleep(300);
                            } catch (InterruptedException e) {
                                Timber.i("sleep failure");
                            }

                            if (isCancelled) {
                                break;
                            }
                        }
                        // When the loop is finished, updates the notification
                        if (!isCancelled) {
                            mBuilder.setContentText("Download complete")
                                    // Removes the progress bar
                                    .setProgress(0, 0, false);
                        } else {
                            String msg = "Download Cancelled";

                            if (getDownload_status() == DOWNLOAD_STATUS.DOWNLOAD_FAILED) {
                                msg = "Download Failed";
                            } else if (getDownload_status() == DOWNLOAD_STATUS.DOWNLOAD_FAILED_COMMS) {
                                msg = "Download Comms Failed";
                            }

                            mBuilder.setContentText(msg)
                                    // Removes the progress bar
                                    .setProgress(0, 0, false);
                        }
                        mNotifyManager.notify(notificationId, mBuilder.build());

                        /*Leave the notification there for a small while.*/
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            Timber.i("sleep failure");
                        }

                        mNotifyManager.cancel(notificationId);
                    }
                }
// Starts the thread by calling the run() method in its Runnable
        ).start();
    }

    public URL getDownloadURL() {
        return this.downloadURL;
    }

    public String getLocalFilePath() {
        return this.localFilePath;
    }

    public DOWNLOAD_STATUS getDownload_status() {
        return this.download_status;
    }

    public boolean isCancelled() {
        return this.isCancelled;
    }

    public int getTryCount() {
        return this.tryCount;
    }

    public int getMaxRetryCount() {
        return this.maxRetryCount;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public String getManiName() {
        return this.maniName;
    }

    public String getManiType() {
        return this.maniType;
    }

    public String getManiVersion() {
        return this.maniVersion;
    }

    public String getFileDigest() {
        return this.fileDigest;
    }

    public void setMaxRetryCount(int maxRetryCount) {
        this.maxRetryCount = maxRetryCount;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void setManiName(String maniName) {
        this.maniName = maniName;
    }

    public void setManiType(String maniType) {
        this.maniType = maniType;
    }

    public void setManiVersion(String maniVersion) {
        this.maniVersion = maniVersion;
    }

    public void setFileDigest(String fileDigest) {
        this.fileDigest = fileDigest;
    }

    public enum DOWNLOAD_STATUS {
        DOWNLOAD_INIT,
        DOWNLOAD_COMPLETE,
        DOWNLOAD_IN_PROGRESS,
        DOWNLOAD_FAILED,
        DOWNLOAD_FAILED_COMMS,
        DOWNLOAD_STARTING,
        DOWNLOAD_COMMS_INITING,
        DOWNLOAD_COMMS_CANCELLED,
        DOWNLOAD_COMMS_CANCELLING,
    }

}
