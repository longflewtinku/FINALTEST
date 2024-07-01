package com.linkly.libengine.engine.transactions.properties;

import static com.linkly.libengine.engine.transactions.properties.TAudit.ReasonOnlineCode.NOT_SET;

import android.annotation.SuppressLint;

import androidx.room.Ignore;

import com.linkly.libengine.config.PayCfg;
import com.linkly.libengine.dependencies.IDependency;
import com.linkly.libengine.engine.Engine;
import com.linkly.libengine.engine.protocol.IProto;
import com.linkly.libengine.overrides.CoreOverrides;
import com.linkly.libengine.users.UserManager;
import com.sun.mail.imap.protocol.ID;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.UUID;

import timber.log.Timber;


public class TAudit {

    @Ignore
    private static final String TAG = "TAudit";

    private String uti = "12345";
    private String userId = "";

    @Ignore
    private String userPwd = "";

    private String terminalId = "";
    private String merchantId = "";
    private String reference = "";
    private String virtualTid = "";
    private String virtualMid = "";
    private String virtualName = "";
    private String countryCode = "036";
    private long transDateTime = 0;
    private long transFinishedDateTime = 0;
    private long reversalDateTime  = 0;
    private long lastTransmissionDateTime  = 0;
    private Integer receiptNumber  = 0;
    private Integer reversalReceiptNumber  = 0;
    private boolean disablePrinting = false;
    private IProto.RejectReasonType rejectReasonType = IProto.RejectReasonType.NOT_SET;
    private ReasonOnlineCode reasonOnlineCode = NOT_SET;
    private String departmentId = "";
    private int adviceRetryCount = 0;

    @Ignore
    private boolean skipReference = false;

    private boolean signatureChecked = false;
    @Ignore
    private String ipcUserId = "";
    @Ignore
    private String ipcUserPwd = "";
    @Ignore
    private String ipcDepartmentId = "";
    @Ignore
    private boolean loyaltyPlayed = false;
    @Ignore
    private boolean accessMode = false;
    @Ignore
    private boolean loyaltyAppSentCard = false;

    public void setRejectReasonType( IProto.RejectReasonType rejectReasonType) {
        this.rejectReasonType = rejectReasonType;
    }

    // Required for POJO
    public TAudit() {
    }

    @SuppressWarnings("static")
    public TAudit(IDependency dependency) {
        uti = generate_UUIDString();

        if( dependency.getPayCfg() != null ) {
            Timber.e("Mid: %s, Tid: %s", dependency.getPayCfg().getMid(), dependency.getPayCfg().getStid());
            terminalId = dependency.getPayCfg().getStid();
            merchantId = dependency.getPayCfg().getMid();

            countryCode = String.format( "%03d", dependency.getPayCfg().getCountryNum() );
        }
        transDateTime = System.currentTimeMillis();
        lastTransmissionDateTime = System.currentTimeMillis();
        reversalDateTime = System.currentTimeMillis();
        receiptNumber = -1; //ReceiptNumber.getNewValue();       //Incrementing this here will cause lots of increments when temp trans are created, moving it to the trans save
        reversalReceiptNumber = -1; // We default this value to stop it being null and cause possible crashes

        if ( dependency.getUsrMgr() != null && dependency.getUsrMgr().getActiveUser() != null) {
            userId = dependency.getUsrMgr().getActiveUser().getUserId();
            departmentId = dependency.getUsrMgr().getActiveUser().getDepartmentId();
        } else {
            userId = "AUTO";
            departmentId = "AUTO";
        }

        if (CoreOverrides.get().isOverrideSignatureChecked()) {
            signatureChecked = true;
        }
    }

    public void setReasonOnlineCode(ReasonOnlineCode code) {

        if (reasonOnlineCode == NOT_SET ||
                reasonOnlineCode.ordinal() > code.ordinal()) {
            reasonOnlineCode = code;
        }
    }

    private String generate_UUIDString() {
        UUID uuid = UUID.randomUUID();
        return uuid.toString().toUpperCase();
    }

    public static String getDateTimeAsString(String format, long dateTime, String timeZone) {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat(format);

        if (timeZone != null && timeZone.length() > 0)
            sdf.setTimeZone(TimeZone.getTimeZone(timeZone));

        Date resultdate = new Date(dateTime);
        return sdf.format(resultdate);
    }

    //"dd/MM/yyyy HH:mm:ss"
    public String getTransDateTimeAsString(String format) {
        return getDateTimeAsString(format, this.transDateTime, null);
    }

    public String getTransDateTimeAsString(String format, String timezone) {
        return getDateTimeAsString(format, this.transDateTime, timezone);
    }

    public String getLastTransmissionDateTimeAsString(String format) {
        return getDateTimeAsString(format, this.lastTransmissionDateTime, "GMT");
    }

    public String getReversalDateTimeAsString(String format) {
        return getDateTimeAsString(format, this.lastTransmissionDateTime, null);
    }

    public void updateDateTimes(String dateTimeyyyyMMddhhmmss) {

        try {

            if (dateTimeyyyyMMddhhmmss != null && dateTimeyyyyMMddhhmmss.length() > 0) {
                DateFormat df = new SimpleDateFormat("yyyyMMddhhmmss");
                Date dateTime = df.parse(dateTimeyyyyMMddhhmmss);
                transDateTime = dateTime.getTime();
                lastTransmissionDateTime = dateTime.getTime();
                reversalDateTime = dateTime.getTime();

                Timber.i( "Updated Time to :" + df.format(dateTime));
            }
        } catch (ParseException e) {
            Timber.w(e);
        }
    }

    public void updateFinishedDateTimeToNow() {
        transFinishedDateTime = System.currentTimeMillis();
    }

    public String getUti() {
        return this.uti;
    }

    public String getUserId() {
        return this.userId;
    }

    public String getUserPwd() {
        return this.userPwd;
    }

    public String getTerminalId() {
        return this.terminalId;
    }

    public String getMerchantId() {
        return this.merchantId;
    }

    public String getReference() {
        return this.reference;
    }

    public String getVirtualTid() {
        return this.virtualTid;
    }

    public String getVirtualMid() {
        return this.virtualMid;
    }

    public String getVirtualName() {
        return this.virtualName;
    }

    public String getCountryCode() {
        return this.countryCode;
    }

    public long getTransDateTime() {
        return this.transDateTime;
    }

    public long getTransFinishedDateTime() {
        return this.transFinishedDateTime;
    }

    public long getReversalDateTime() {
        return this.reversalDateTime;
    }

    public long getLastTransmissionDateTime() {
        return this.lastTransmissionDateTime;
    }

    public Integer getReceiptNumber() {
        return this.receiptNumber;
    }

    public Integer getReversalReceiptNumber() {
        return this.reversalReceiptNumber;
    }

    public boolean isDisablePrinting() {
        return this.disablePrinting;
    }

    public IProto.RejectReasonType getRejectReasonType() {
        return this.rejectReasonType;
    }

    public ReasonOnlineCode getReasonOnlineCode() {
        return this.reasonOnlineCode;
    }

    public String getDepartmentId() {
        return this.departmentId;
    }

    public int getAdviceRetryCount() {
        return this.adviceRetryCount;
    }

    public boolean isSkipReference() {
        return this.skipReference;
    }

    public boolean isSignatureChecked() {
        return this.signatureChecked;
    }

    public String getIpcUserId() {
        return this.ipcUserId;
    }

    public String getIpcUserPwd() {
        return this.ipcUserPwd;
    }

    public String getIpcDepartmentId() {
        return this.ipcDepartmentId;
    }

    public boolean isLoyaltyPlayed() {
        return this.loyaltyPlayed;
    }

    public boolean isAccessMode() {
        return this.accessMode;
    }

    public boolean isLoyaltyAppSentCard() {
        return this.loyaltyAppSentCard;
    }

    public void setUti(String uti) {
        this.uti = uti;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setUserPwd(String userPwd) {
        this.userPwd = userPwd;
    }

    public void setTerminalId(String terminalId) {
        this.terminalId = terminalId;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public void setVirtualTid(String virtualTid) {
        this.virtualTid = virtualTid;
    }

    public void setVirtualMid(String virtualMid) {
        this.virtualMid = virtualMid;
    }

    public void setVirtualName(String virtualName) {
        this.virtualName = virtualName;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public void setTransDateTime(long transDateTime) {
        this.transDateTime = transDateTime;
    }

    public void setTransFinishedDateTime(long transFinishedDateTime) {
        this.transFinishedDateTime = transFinishedDateTime;
    }

    public void setReversalDateTime(long reversalDateTime) {
        this.reversalDateTime = reversalDateTime;
    }

    public void setLastTransmissionDateTime(long lastTransmissionDateTime) {
        this.lastTransmissionDateTime = lastTransmissionDateTime;
    }

    public void setReceiptNumber(Integer receiptNumber) {
        this.receiptNumber = receiptNumber;
    }

    public void setReversalReceiptNumber(Integer reversalReceiptNumber) {
        this.reversalReceiptNumber = reversalReceiptNumber;
    }

    public void setDisablePrinting(boolean disablePrinting) {
        this.disablePrinting = disablePrinting;
    }

    public void setDepartmentId(String departmentId) {
        this.departmentId = departmentId;
    }

    public void setAdviceRetryCount(int adviceRetryCount) {
        this.adviceRetryCount = adviceRetryCount;
    }

    public void setSkipReference(boolean skipReference) {
        this.skipReference = skipReference;
    }

    public void setSignatureChecked(boolean signatureChecked) {
        this.signatureChecked = signatureChecked;
    }

    public void setIpcUserId(String ipcUserId) {
        this.ipcUserId = ipcUserId;
    }

    public void setIpcUserPwd(String ipcUserPwd) {
        this.ipcUserPwd = ipcUserPwd;
    }

    public void setIpcDepartmentId(String ipcDepartmentId) {
        this.ipcDepartmentId = ipcDepartmentId;
    }

    public void setLoyaltyPlayed(boolean loyaltyPlayed) {
        this.loyaltyPlayed = loyaltyPlayed;
    }

    public void setAccessMode(boolean accessMode) {
        this.accessMode = accessMode;
    }

    public void setLoyaltyAppSentCard(boolean loyaltyAppSentCard) {
        this.loyaltyAppSentCard = loyaltyAppSentCard;
    }

    // this list is in priority order to ensure that the highest priority reason isn't overridden by a lower priority reason code
    // ###### WARNING: CAREFUL WHEN CHANGING THIS ENUM #########
    public enum ReasonOnlineCode {
            NOT_SET,
            FALLBACK_FOR_IC,                 /* 04 (icc fallback to MSR */
            RTIME_FORCED_ICC,                /* 05 TVR empty */
            RTIME_FORCED_CARD_ACCEPTOR,      /* 06 Keyed */
            RTIME_FORCED_CARD_ISSUER,        /* 09 Service Code, TVR byte 3 b4, TVR byte 2 b7 (new card or expired) */
            RTIME_CARD_ACCEPTOR_SUSPICIOUS,  /* 11 gen AC2 fail, signature fail */
            TERMINAL_RANDOM_SELECT,          /* 03 TVR byte 4 bit 5 */
            RTIME_OVER_FLOOR,                /* 10 byte 4 b8, or over floor limits for MSR */
            // 08 Missing
            ICC_COMMON_DATA_FAIL,            /* 00 Not Used */
            ICC_APP_DATA_FAIL,               /* 01 Not Used */
            ICC_RANDOM_SELECT,               /* 02 Not used */
            RTIME_FORCED_TERMINAL,           /* 07 TVR byte 4 b4, */
            }


}
