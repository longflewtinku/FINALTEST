package com.linkly.libconfig;

import androidx.annotation.Keep;

@Keep
public class InitialParameters {
    private String stid;                            //10000001</stid>
    private String mid;                             //APS_LOC_X017</mid>
    private String tkey;                            //transaction key
    private String access_token;
    private String access_token2;
    private String acquirerInstitutionId;           // NOT USED
    private String bankTimeZone; // Bank timezone formatted for android specifications eg Australia/Sydney
    private String terminalTimeZone; // Timezone that the terminal
    private String footerLine1;
    private String footerLine2;

    public String getStid() {
        return this.stid;
    }

    public String getMid() {
        return this.mid;
    }

    public String getTkey() {
        return this.tkey;
    }

    public String getAccess_token() {
        return this.access_token;
    }

    public String getAccess_token2() {
        return this.access_token2;
    }

    public String getAcquirerInstitutionId() {
        return this.acquirerInstitutionId;
    }

    public String getBankTimeZone() {
        return this.bankTimeZone;
    }

    public String getTerminalTimeZone() {
        return this.terminalTimeZone;
    }

    public String getFooterLine1() {
        return this.footerLine1;
    }

    public String getFooterLine2() {
        return this.footerLine2;
    }
}
