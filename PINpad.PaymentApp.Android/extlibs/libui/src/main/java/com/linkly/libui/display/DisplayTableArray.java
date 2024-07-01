package com.linkly.libui.display;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Arrays;

public class DisplayTableArray implements Parcelable {
    public static final Parcelable.Creator<DisplayTableArray> CREATOR
            = new Parcelable.Creator<DisplayTableArray>() {
        public DisplayTableArray createFromParcel(Parcel in) {
            return new DisplayTableArray(in);
        }

        public DisplayTableArray[] newArray(int size) {
            return new DisplayTableArray[size];
        }
    };
    ArrayList<DisplayTableRow> rows;


    @SuppressWarnings("deprecation")
    public DisplayTableArray(Parcel in) {
        DisplayTableRow[] array = ( DisplayTableRow[] )in.readArray( DisplayTableRow.class.getClassLoader() );
        rows = new ArrayList<>( Arrays.asList( array ) );
    }


    public DisplayTableArray() {
        rows = new ArrayList<>();

    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeList(rows);
    }

    public ArrayList<DisplayTableRow> getRows() {
        return this.rows;
    }
}
