package com.linkly.libui.display;

import static com.linkly.libui.display.DisplayFragmentOption.AdditionalIcons.ICON_NOT_SET;

import android.os.Parcel;
import android.os.Parcelable;

public class DisplayFragmentOption implements Parcelable {
    public static final Creator<DisplayFragmentOption> CREATOR
            = new Creator<DisplayFragmentOption>() {

        public DisplayFragmentOption createFromParcel(Parcel in) {
            return new DisplayFragmentOption(in);
        }

        public DisplayFragmentOption[] newArray(int size) {
            return new DisplayFragmentOption[size];
        }
    };
    private String fragText;
    private String fragAmount;
    private AdditionalIcons icon = ICON_NOT_SET;
    @SuppressWarnings("deprecation")
    public DisplayFragmentOption(Parcel in) {
        this.fragText = in.readString();
        this.fragAmount = in.readString();
        this.icon = (AdditionalIcons) in.readSerializable();
    }

    public DisplayFragmentOption(String fragText, AdditionalIcons icon) {
        this.fragText = fragText;
        this.icon = icon;
    }

    public DisplayFragmentOption(String fragText, String fragAmount) {
        this.fragText = fragText;
        this.fragAmount = fragAmount;
    }

    public DisplayFragmentOption(String fragText) {
        this.fragText = fragText;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(fragText);
        out.writeString(fragAmount);
        out.writeSerializable(icon);
    }


    public String getFragText() {
        return this.fragText;
    }
    public String getFragAmount() {
        return this.fragAmount;
    }

    public AdditionalIcons getIcon() {
        return this.icon;
    }


    public enum AdditionalIcons {
        ICON_NOT_SET,
        ICON_MATCH_FULL,     /* Matched */
        ICON_MATCH_PARTIAL,  /* PartialMatch */
        ICON_MATCH_NOT_CHECKED,  /* Not Checked */
        ICON_MATCH_FAIL      /* NotMatched */
    }




}
