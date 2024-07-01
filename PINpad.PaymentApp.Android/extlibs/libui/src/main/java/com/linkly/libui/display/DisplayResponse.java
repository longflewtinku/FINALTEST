package com.linkly.libui.display;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import com.linkly.libui.IUIDisplay;


public class DisplayResponse implements Parcelable {

    public static final Parcelable.Creator<DisplayResponse> CREATOR
            = new Parcelable.Creator<DisplayResponse>() {
        public DisplayResponse createFromParcel(Parcel in) {
            return new DisplayResponse(in);
        }

        public DisplayResponse[] newArray(int size) {
            return new DisplayResponse[size];
        }
    };
    private static final String TAG = "DisplayResponse";
    private IUIDisplay.ACTIVITY_ID iActivityID;
    private Bundle uiExtras;

    @SuppressWarnings("deprecation")
    private DisplayResponse(Parcel in) {
        iActivityID = IUIDisplay.ACTIVITY_ID.values()[in.readInt()];
        uiExtras = in.readParcelable(Bundle.class.getClassLoader());
    }

    public DisplayResponse(Bundle uiExtras) {
        this.uiExtras = uiExtras;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(iActivityID == null ? -1 : iActivityID.ordinal());
        out.writeParcelable(uiExtras, flags);
    }

    public Bundle getUiExtras() {
        return this.uiExtras;
    }

    public IUIDisplay.ACTIVITY_ID getIActivityID() {
        return this.iActivityID;
    }

    public void setIActivityID(IUIDisplay.ACTIVITY_ID iActivityID) {
        this.iActivityID = iActivityID;
    }

    public void setUiExtras(Bundle uiExtras) {
        this.uiExtras = uiExtras;
    }
}
