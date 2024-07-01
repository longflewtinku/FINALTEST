package com.linkly.libconfig;

import com.linkly.libmal.MalFactory;
import com.linkly.libmal.global.config.EnvCfg;
import com.linkly.libmal.global.config.JSONParse;

import timber.log.Timber;

public class DownloadCfg {

    private static final String TAG = "DownloadCfg";
    private static final String sysLastDownloadDateCfg = "dmgr.dldDate";
    private static final String sysLastDownloadResultCfg = "dmgr.dldResult";
    private static final String sysLastDownloadReasonCfg = "dmgr.dldReason";
    private static DownloadCfg ourInstance = new DownloadCfg();
    protected boolean validCfg = false;
    private String lastDownload;
    private String lastDownloadResult;
    private String lastDownloadReason;
    /***************************************************************/
    /* debug all of the config */
    private String timeZone;
    private DownloadCfg.AutoRec autoRecon = new DownloadCfg.AutoRec();;
    private DownloadCfg.Heartbeat heartbeat = new DownloadCfg.Heartbeat();
    private DownloadCfg.Gprs gprs = new DownloadCfg.Gprs();


    protected DownloadCfg() {
    }

    public static DownloadCfg getInstance() {
        return ourInstance;
    }

    public DownloadCfg parse() {

        if (ourInstance == null || !ourInstance.isValidCfg()) {
            try {
                validCfg = false;
                ourInstance = new DownloadCfg();

                JSONParse j = new JSONParse();
                ourInstance = (DownloadCfg)j.parse("download.json", DownloadCfg.class);
                ourInstance.validCfg = true;

            } catch (Exception e) {
                Timber.w(e);
            }
        } else {
            ourInstance.validCfg = true;
        }

        if (ourInstance != null && MalFactory.getInstance().getMalContext() != null) {
            ourInstance.lastDownload = EnvCfg.getInstance().readValue(sysLastDownloadDateCfg);
            ourInstance.lastDownloadResult = EnvCfg.getInstance().readValue(sysLastDownloadResultCfg);
            ourInstance.lastDownloadReason = EnvCfg.getInstance().readValue(sysLastDownloadReasonCfg);

        }
        return ourInstance;

    }

    /***************************************************************/
    /* details for parsing the xml */

    public int getUTCOffSet() {
        int offset = 0;

        if (this.timeZone != null && this.timeZone.length() > 0 && this.timeZone.contains("UTC")) {
            int sign = 1;
            if (this.timeZone.contains("UTC-")) {
                sign = -1;
            }

            offset = Integer.parseInt(this.timeZone.substring(4, this.timeZone.indexOf(":")));
            offset *= sign;
        }

        return offset;
    }

    public boolean isValidCfg() {
        return this.validCfg;
    }

    public String getLastDownload() {
        return this.lastDownload;
    }

    public String getLastDownloadResult() {
        return this.lastDownloadResult;
    }

    public String getLastDownloadReason() {
        return this.lastDownloadReason;
    }

    public String getTimeZone() {
        return this.timeZone;
    }

    public AutoRec getAutoRecon() {
        return this.autoRecon;
    }

    public Heartbeat getHeartbeat() {
        return this.heartbeat;
    }

    public Gprs getGprs() {
        return this.gprs;
    }

    public static class AutoRec {
        private String time;

        public String getTime() {
            return this.time;
        }
    }

    public static class Heartbeat {
        private String day;
        private String startTime;
        private String endTime;

        public String getDay() {
            return this.day;
        }

        public String getStartTime() {
            return this.startTime;
        }

        public String getEndTime() {
            return this.endTime;
        }
    }

    public static class Gprs {
        private GprsSetting primary = new GprsSetting();
        private GprsSetting secondary = new GprsSetting();

        public GprsSetting getPrimary() {
            return this.primary;
        }

        public GprsSetting getSecondary() {
            return this.secondary;
        }
    }

    public static class GprsSetting {
        private String apnName;
        private String apn;
        private String user;
        private String password;
        private String dialup;

        public String getApnName() {
            return this.apnName;
        }

        public String getApn() {
            return this.apn;
        }

        public String getUser() {
            return this.user;
        }

        public String getPassword() {
            return this.password;
        }

        public String getDialup() {
            return this.dialup;
        }
    }

}