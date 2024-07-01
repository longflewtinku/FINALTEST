package com.linkly.libui.keyboard;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.util.AttributeSet;

import com.linkly.libui.R;

import java.util.List;
@SuppressWarnings("deprecation")
public class PosKeyboardView extends KeyboardView {
    private Paint mPaint;

    private int mXPad = (360 / 32);        // used to adjust the width for padding the onDraw (11)
    private int mYPad = (240 / 11);        // used to adjust the height for padding the onDraw (21)
    private int mDoubleHeightAdjust = 2;   // used to adjust the drawing for double height keys like DONE

    public PosKeyboardView(Context context, AttributeSet attrs, int xPad, int yPad, int doubleHeightAdjust) {
        super(context, attrs);

        UpdatePadding(xPad, yPad, doubleHeightAdjust);

        mPaint = new Paint();
        mPaint.setTextAlign(Paint.Align.CENTER);
        mPaint.setFakeBoldText(true);
        mPaint.setColor(Color.WHITE);
    }

    public PosKeyboardView(Context context, AttributeSet attrs) {
        this(context, attrs, 0, 0, 0);
    }

    public void UpdatePadding(int xPad, int yPad, int doubleHeightAdjust) {
        final float scale = getContext().getResources().getDisplayMetrics().density;
        if (xPad > 0) {
            mXPad = (int) (xPad * scale);
        }
        if (yPad > 0) {
            mYPad = (int) (yPad * scale);
        }
        if (doubleHeightAdjust > 0) {
            mDoubleHeightAdjust = doubleHeightAdjust;
        }
    }

    public void UpdatePadding(CustomKeyboard.KBTypes kbType) {
        int doubleHeightAdjust;
        switch(kbType) {
            case AMOUNT_KB:
            case TEXT_KB:
            case TEXT_KB_CAPS:
                doubleHeightAdjust = 0;
                break;
            case PHONE_KB:
            default:
                doubleHeightAdjust = 1;
                break;
        }
        UpdatePadding(3, 5, doubleHeightAdjust);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        final float scale = getContext().getResources().getDisplayMetrics().density;
        final int textSize = (int) (16 * scale + 0.5f); // text size 16 adjusted for pixels
        mPaint.setTextSize(textSize);

        if (getKeyboard() == null)
            return;

        List<Keyboard.Key> keys = getKeyboard().getKeys();
        if (keys == null)
            return;

        for (Keyboard.Key key : keys) {
            int id = 0;
            int yPad = mYPad;

            switch(key.codes[0]) {
                case Keyboard.KEYCODE_CANCEL:
                    id = R.drawable.cancelkey;
                    break;
                case Keyboard.KEYCODE_DELETE:
                    id = R.drawable.delkey;
                    break;
                case Keyboard.KEYCODE_DONE:
                    id = R.drawable.donekey;
                    // double height key so padding needs halving
                    yPad = (mYPad / mDoubleHeightAdjust);
                    break;
            }

            if(id != 0) {
                @SuppressWarnings( "deprecation" )
                int left = key.x + mXPad;
                int top = key.y + yPad;
                int right = key.x + key.width - mXPad;
                int bottom = key.y + key.height - (yPad) / 3;

                Drawable dr = getContext().getResources().getDrawable(id, null);
                dr.setBounds(left, top, right, bottom);
                dr.draw(canvas);

                canvas.drawText(key.label.toString(), key.x + (key.width / 2.0f),key.y + (key.height / 2) + (textSize / 2), mPaint);
            }
        }
    }
}
