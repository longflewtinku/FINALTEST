package com.linkly.libengine.overrides;

import static com.linkly.libengine.engine.transactions.properties.TCard.CvmType.NO_CVM_SET;

import com.linkly.libconfig.ProfileCfg;
import com.linkly.libengine.engine.transactions.properties.TCard;
import com.pax.dal.entity.TrackData;

public class CoreOverrides {
    private static CoreOverrides ourInstance = new CoreOverrides();
    private boolean spoofComms = false;
    private boolean spoofCommsAuthAll = false;
    private boolean spoofApprove2ndGenAC = false;

    private final TCard.CvmType overrideCvmType = NO_CVM_SET;
    // do not reboot on reconciliation
    // always ask if cashback required for EMV - even this is not supported by the card (AUC)

    private boolean autoFillTrans = false;    //When true the terminal will automatically fill the transaction - Test Only

    // do not generate getter/setter for this particular member field
    private TrackData lastMagstripeTracks = null;

    public static void initialise() {
        if( null == CoreOverrides.ourInstance ) {
            ourInstance = new CoreOverrides();

            if ( ProfileCfg.getInstance().isDemo() ) {

                CoreOverrides.get().setSpoofComms( true );
                CoreOverrides.get().setSpoofCommsAuthAll( true );
                CoreOverrides.get().setSpoofApprove2ndGenAC( true );
            }
        }
    }

    public static CoreOverrides get() {
        return ourInstance;
    }


    public TrackData getMagstripeTrackData() {
        // return "last magstripe data" only once
        TrackData magTracks = lastMagstripeTracks;
        lastMagstripeTracks = null;
        return magTracks;
    }

    public boolean isMagstripeTrackDataReady() {
        return (lastMagstripeTracks != null);
    }


    public boolean isApproveReferral() {
        return false;
    }

    public boolean isSpoofComms() {
        return this.spoofComms;
    }

    public boolean isSpoofCommsAuthAll() {
        return this.spoofCommsAuthAll;
    }

    public boolean isSpoofCommsConnectFail() {
        return false;
    }

    public boolean isSpoofCommsReceiveFail() {
        return false;
    }

    public boolean isSpoofApprove2ndGenAC() {
        return this.spoofApprove2ndGenAC;
    }

    public boolean isSpoofApprovePostComms() {
        return false;
    }

    public boolean isSpoofReversePostComms() {
        return false;
    }

    public boolean isSpoofReverseResend() {
        return false;
    }

    public boolean isSpoofReverseFailure() {
        return false;
    }

    public boolean isSpoofRecFailure() {
        return false;
    }

    public boolean isSpoofReferral() {
        return false;
    }

    public boolean isEnableCashback() {
        return false;
    }

    public boolean isEnablePositiveDemo() {
        /* as opposed to Optomany demo */
        return false;
    }

    public long getOverrideAmount() {
        return 0;
    }

    public boolean isDisablePinEntry() {
        return false;
    }

    public TCard.CvmType getOverrideCvmType() {
        return this.overrideCvmType;
    }

    public boolean isDisableCardRemove() {
        return false;
    }

    public boolean isOverrideSignatureChecked() {
        return false;
    }

    public boolean isIgnoreServiceCodes() {
        return false;
    }

    public boolean isDisableUITransitions() {
        // UIAutomator sometimes fails when Display animations/transition effects are enabled - it is recommended to switch them off,
        return true;
    }

    public boolean isRunningAutoTests() {
        // used for tests to disable the auto card detect on the main menu and login screen
        return false;
    }

    public boolean isAutoFillTrans() {
        return this.autoFillTrans;
    }

    public String getAutoUserLoginUserName() {
        //            14429
        return "1234";
    }

    public String getAutoUserLoginDepartment() {
        // Pre prod 116024
        return "4520";
    }


    public void setSpoofComms(boolean spoofComms) {
        this.spoofComms = spoofComms;
    }

    public void setSpoofCommsAuthAll(boolean spoofCommsAuthAll) {
        this.spoofCommsAuthAll = spoofCommsAuthAll;
    }

    public void setSpoofApprove2ndGenAC(boolean spoofApprove2ndGenAC) {
        this.spoofApprove2ndGenAC = spoofApprove2ndGenAC;
    }
}
