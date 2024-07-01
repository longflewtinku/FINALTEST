package com.linkly.libui.display;

import android.os.Parcel;
import android.os.Parcelable;

import com.linkly.libui.IUIDisplay.String_id;
import com.linkly.libui.UI;

public class DisplayQuestion implements Parcelable {
    public static final Parcelable.Creator<DisplayQuestion> CREATOR
            = new Parcelable.Creator<DisplayQuestion>() {

        public DisplayQuestion createFromParcel(Parcel in) {
            return new DisplayQuestion(in);
        }

        public DisplayQuestion[] newArray(int size) {
            return new DisplayQuestion[size];
        }
    };
    private String btnTitle;
    private String btnResponse;
    private EButtonStyle btnStyle;

    @SuppressWarnings("deprecation")
    public DisplayQuestion(Parcel in) {
        this(in.readString(), in.readString(), (EButtonStyle)in.readSerializable());
    }

    public DisplayQuestion(String title, String response) {
        this(title, response, EButtonStyle.BTN_STYLE_DEFAULT);
    }

    public DisplayQuestion(String_id btnTitleId, String btnResponse){
        this(btnTitleId,btnResponse,EButtonStyle.BTN_STYLE_DEFAULT);
    }

    public DisplayQuestion(String title, String response, EButtonStyle style) {
        btnTitle = title;
        btnResponse = response;
        btnStyle = style;
    }

    public DisplayQuestion(String_id btnTitleId, String btnResponse, EButtonStyle style ){
        this.btnTitle = UI.getInstance().getPrompt(btnTitleId);
        this.btnResponse = btnResponse;
        this.btnStyle = style;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(btnTitle);
        out.writeString(btnResponse);
        out.writeSerializable(btnStyle);
    }

    public String getTitle() {
        return this.btnTitle;
    }

    public String getResponse() {
        return this.btnResponse;
    }

    public EButtonStyle getStyle() {
        return this.btnStyle;
    }

    public enum EButtonStyle {
        BTN_STYLE_DEFAULT,
        BTN_STYLE_DEFAULT_LEFT_ALIGNED_TEXT,
        BTN_STYLE_RED,
        BTN_STYLE_RED_DOUBLE,
        BTN_STYLE_GREEN,
        BTN_STYLE_LEFT,
        BTN_STYLE_RIGHT,
        BTN_STYLE_DEFAULT_DOUBLE,
        BTN_STYLE_TRANSPARENT,
        BTN_STYLE_TRANSPARENT_DOUBLE,
        BTN_STYLE_PRIMARY_DEFAULT,
        BTN_STYLE_PRIMARY_DEFAULT_DOUBLE,
        BTN_STYLE_PRIMARY_BORDER,
        BTN_STYLE_PRIMARY_BORDER_DOUBLE,
        BTN_STYLE_PRIMARY_DEFAULT_RIGHT,
        BTN_STYLE_PRIMARY_BORDER_LEFT,
        BTN_STYLE_GREY_LEFT,
        BTN_STYLE_GREY_RIGHT
    }
}
