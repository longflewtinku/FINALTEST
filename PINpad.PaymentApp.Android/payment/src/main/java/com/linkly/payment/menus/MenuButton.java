package com.linkly.payment.menus;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.VectorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.core.content.ContextCompat;

import com.linkly.libmal.global.util.Util;
import com.linkly.payment.R;
import com.linkly.payment.activities.AppMain;

import java.util.List;

public class MenuButton extends BaseAdapter {

    private LayoutInflater mInflater;
    private List<MenuItems> mDataSource;
    private
    @DrawableRes
    int mIconId;

    public MenuButton(Context context, List<MenuItems> items) {
        mDataSource = items;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void setDefaultIcon(@DrawableRes int iconId) {
        mIconId = iconId;
    }

    @Override
    public int getCount() {
        return mDataSource.size();
    }

    @Override
    public Object getItem(int position) {
        return mDataSource.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        try {
            RelativeLayout rowView = (RelativeLayout) mInflater.inflate(R.layout.fragment_button_menu, parent, false);
            TextView titleTextView = (TextView) rowView.findViewById(R.id.list_title);
            MenuItems item = (MenuItems) getItem(position);
            ImageView icon = (ImageView) rowView.findViewById(R.id.ui2_list_thumbnail);

            if (item.isDisabled()) {
                if (item.getIconResId() > 0 && AppMain.getApp() != null && AppMain.getApp().getResources() != null) {
                    icon.setImageBitmap(AddTwoImages(getBitmap(parent.getContext(), item.getIconResId()),
                            BitmapFactory.decodeResource(AppMain.getApp().getResources(), R.mipmap.greybox)));
                }
            } else {
                if (item.getIconResId() > 0) {
                    icon.setImageResource(item.getIconResId());
                }
            }
            if (!Util.isNullOrEmpty(item.getDisplayTextId())) {
                titleTextView.setText(item.getDisplayTextId());
            }
            return rowView;

        } catch (Exception ex) {
            /*Resource Not Found, just render as is*/
            ex.printStackTrace();
        }
        return null;
    }

    private static Bitmap AddTwoImages(Bitmap bottomImage, Bitmap topImage) {

        int sizex = bottomImage.getWidth();
        int sizey = bottomImage.getHeight();

        Paint paint = new Paint();
        Bitmap imageBitmap = Bitmap.createBitmap(sizex, sizey, Bitmap.Config.ARGB_8888);
        Canvas comboImage = new Canvas(imageBitmap);
        comboImage.drawBitmap(bottomImage, 0f, 0f, paint);
        PorterDuff.Mode mode = PorterDuff.Mode.ADD;//Porterduff MODE
        paint.setXfermode(new PorterDuffXfermode(mode));

        Bitmap ScaledtopImage = Bitmap.createScaledBitmap(topImage, sizex, sizey, false);
        comboImage.drawBitmap(ScaledtopImage, 0f, 0f, paint);

        return imageBitmap;

    }

    private static Bitmap getBitmap(VectorDrawable vectorDrawable) {
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(),
                vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        vectorDrawable.draw(canvas);
        return bitmap;
    }

    private static Bitmap getBitmap(Context context, int drawableId) {
        Drawable drawable = ContextCompat.getDrawable(context, drawableId);
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        } else if (drawable instanceof VectorDrawable) {
            return getBitmap((VectorDrawable) drawable);
        } else {
            throw new IllegalArgumentException("unsupported drawable type");
        }
    }

}
