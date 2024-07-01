package com.linkly.libui.display;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Arrays;

public class DisplayTableRow implements Parcelable {
    public static final Parcelable.Creator<DisplayTableRow> CREATOR
            = new Parcelable.Creator<DisplayTableRow>() {
        public DisplayTableRow createFromParcel(Parcel in) {
            return new DisplayTableRow(in);
        }

        public DisplayTableRow[] newArray(int size) {
            return new DisplayTableRow[size];
        }
    };
    ArrayList<DisplayTableItem> items;

    @SuppressWarnings("deprecation")
    public DisplayTableRow(Parcel in) {
        DisplayTableItem[] array = ( DisplayTableItem[] )in.readArray( DisplayTableItem.class.getClassLoader() );
        items = new ArrayList<>( Arrays.asList( array ) );
    }

    public DisplayTableRow() {
        items = new ArrayList<>();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeList(items);
    }

    public ArrayList<DisplayTableItem> getItems() {
        return this.items;
    }
}

