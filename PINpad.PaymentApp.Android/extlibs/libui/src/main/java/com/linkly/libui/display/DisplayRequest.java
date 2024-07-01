package com.linkly.libui.display;

import static com.linkly.libui.IUIDisplay.ACTIVITY_ID.ACT_INFORMATION;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import com.linkly.libui.IUIDisplay;

import timber.log.Timber;

/********************************************************************************************************/
/*  Utility class for packing messages to the Display */
/********************************************************************************************************/
public class DisplayRequest implements Parcelable {

    public static final Parcelable.Creator<DisplayRequest> CREATOR
            = new Parcelable.Creator<DisplayRequest>() {
        public DisplayRequest createFromParcel(Parcel in) {
            return new DisplayRequest(in);
        }

        public DisplayRequest[] newArray(int size) {
            return new DisplayRequest[size];
        }
    };
    private static final String TAG = "DisplayRequest";
    private IUIDisplay.ACTIVITY_ID iActivityID;
    private Bundle uiExtras;
    private boolean responded = false;
    @SuppressWarnings("deprecation")
    private DisplayRequest(Parcel in) {
        iActivityID = IUIDisplay.ACTIVITY_ID.values()[in.readInt()];
        uiExtras = in.readParcelable(Bundle.class.getClassLoader());
    }

    public DisplayRequest(Intent intent) {

        if (intent != null) {
            Bundle b = intent.getExtras();
            if (b == null) {
                Timber.i( "Invalid Bundle found");
            } else {
                iActivityID = IUIDisplay.ACTIVITY_ID.values()[b.getInt("iActivityID")];
                uiExtras = b;
            }
        }
    }


    public DisplayRequest(IUIDisplay.ACTIVITY_ID iActivityID, Bundle uiExtras) {
        this.iActivityID = iActivityID;
        this.uiExtras = uiExtras;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(iActivityID == null ? -1 : iActivityID.ordinal());
        out.writeParcelable(uiExtras, flags);
    }

    public IUIDisplay.ACTIVITY_ID getActivityID() {
        return iActivityID;
    }


    public Bundle getUiExtras() {
        return this.uiExtras;
    }

    public void setUiExtras(Bundle uiExtras) {
        this.uiExtras = uiExtras;
    }

    public void debug(String onName) {
        if (iActivityID != null)
            Timber.i("DisplayRequest: " + onName + " " + iActivityID.name());
        if (iActivityID == ACT_INFORMATION) {
            Timber.i("Title:" + getUiExtras().getString(IUIDisplay.uiScreenTitle) + " Prompt:" + getUiExtras().getString(IUIDisplay.uiScreenPrompt));
        }
    }


    public boolean isResponded() {
        return this.responded;
    }

    public void setResponded(boolean responded) {
        this.responded = responded;
    }
}

