package com.linkly.libui.display;

import android.os.Parcel;
import android.os.Parcelable;

import com.linkly.libui.IUIDisplay.String_id;
import com.linkly.libui.UI;

public class DisplayTableItem implements Parcelable {
    public static final int TEXT_BOLD = 1;
    public static final int TEXT_NORMAL = 0;
    public static final int LEFT_ALIGN = 0;
    public static final int RIGHT_ALIGN = 1;
    public static final Parcelable.Creator<DisplayTableItem> CREATOR
            = new Parcelable.Creator<DisplayTableItem>() {
        public DisplayTableItem createFromParcel(Parcel in) {
            return new DisplayTableItem(in);
        }

        public DisplayTableItem[] newArray(int size) {
            return new DisplayTableItem[size];
        }
    };
    String title;
    int bgColor;
    int alignment = 0;
    int textStyle = 0;

    public DisplayTableItem(Parcel in) {
        title = in.readString();
        bgColor = in.readInt();
        alignment = in.readInt();
        textStyle = in.readInt();

    }

    public DisplayTableItem(String_id titleId, String ... titleArgs){
        if( titleArgs != null && titleArgs.length > 0 && titleArgs[0].length() > 0){
            // Add a switch case if needed
            this.title = UI.getInstance().getPrompt(titleId, titleArgs);
        }
        else if (titleId.getId() > 0 ){
            this.title = UI.getInstance().getPrompt(titleId);
        }
        this.bgColor = 0xFFFFFFFF;
    }

    public DisplayTableItem(String title, int alignment, int textStyle) {
        this.title = title;
        this.bgColor = 0xFFFFFFFF;
        this.alignment = alignment;
        this.textStyle = textStyle;
    }

    public DisplayTableItem(String_id titleId, int alignment, int textStyle, String ... titleArgs ){
        if( titleArgs != null && titleArgs.length > 0 && titleArgs[0].length() > 0){
            this.title = UI.getInstance().getPrompt(titleId,titleArgs);
        }
        else if (titleId.getId() > 0){
            this.title = UI.getInstance().getPrompt(titleId);
        }
        this.bgColor = 0xFFFFFFFF;
        this.alignment = alignment;
        this.textStyle = textStyle;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeInt(bgColor);
        dest.writeInt(alignment);
        dest.writeInt(textStyle);


    }

    public String getTitle() {
        return this.title;
    }

    public int getBgColor() {
        return this.bgColor;
    }

    public int getAlignment() {
        return this.alignment;
    }

    public int getTextStyle() {
        return this.textStyle;
    }
}