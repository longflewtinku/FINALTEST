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
public class UI2PosKeyboardView extends KeyboardView {
    private Paint mPaint;

    private int mXPad = (360 / 32);        // used to adjust the width for padding the onDraw (11)
    private int mYPad = (240 / 11);        // used to adjust the height for padding the onDraw (21)
    private int mDoubleHeightAdjust = 2;   // used to adjust the drawing for double height keys like DONE

    CustomKeyboard.KBTypes mCurrentType;

    public UI2PosKeyboardView(Context context, AttributeSet attrs, int xPad, int yPad, int doubleHeightAdjust) {
        super(context, attrs);

        UpdatePadding(xPad, yPad, doubleHeightAdjust);

        mPaint = new Paint();
        mPaint.setTextAlign(Paint.Align.CENTER);
        mPaint.setFakeBoldText(true);
    }

    public UI2PosKeyboardView(Context context, AttributeSet attrs) {
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
        mCurrentType = kbType;

        final int xPad = 1, doubleHeightAdjust = 1;
        int yPad;
        switch(kbType) {
            default:
            case AMOUNT_KB:
            case USR_PASS_KB:
            case NUM_ALPHA:
            case NUM_ALPHA_NO_SYM:
            case TEXT_KB:
            case TEXT_KB_CAPS:
            case TEXT_KB_CAPS_NO_SYM:
                yPad = 1;
                break;
            case PHONE_KB:
                yPad = 2;
                break;
        }
        UpdatePadding(xPad, yPad, doubleHeightAdjust);
    }

    @SuppressWarnings("fallthrough")
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
            boolean textKb;
            switch(mCurrentType) {
                case AMOUNT_KB:
                case USR_PASS_KB:
                case NUM_ALPHA:
                case NUM_ALPHA_NO_SYM:
                    textKb = false;
                    break;
                case TEXT_KB:
                case TEXT_KB_CAPS:
                case TEXT_KB_CAPS_NO_SYM:
                    textKb = true;
                    break;
                case PHONE_KB:
                case IP_PORT_KB:
                default:
                    //Do nothing for these types of keyboards
                    return;
            }

            int left = key.x + mXPad;
            int top = key.y + mYPad;
            int right = key.x + key.width - mXPad;
            int bottom = key.y + key.height;

            int keyId;
            switch(key.codes[0]) {
                case Keyboard.KEYCODE_CANCEL:
                    keyId = R.drawable.cancelkey;
                    mPaint.setColor(Color.WHITE);
                    break;
                case Keyboard.KEYCODE_DELETE:
                    keyId = R.drawable.delkey;
                    mPaint.setColor(Color.WHITE);
                    break;
                case Keyboard.KEYCODE_DONE:
                    keyId = R.drawable.donekey;
                    mPaint.setColor(Color.WHITE);
                    // double height key so padding needs halving
                    top = key.y + (mYPad / mDoubleHeightAdjust);
                    if(textKb) {
                        right = key.x + key.width + 4;
                    }
                    break;
                case '=': //61
                case '.': //46
                case 'p': //112
                case '0': //48
                    if(textKb) {
                        right -= mXPad;
                    }
                    //deliberate fallthrough
                default:
                    //This undoes initial x padding subtraction (excluding textKb condition above)
                    right += mXPad;
                    keyId = R.drawable.defaultkey;
                    mPaint.setColor(Color.BLACK);
                    break;
            }

            @SuppressWarnings( "deprecation" )
            Drawable dr = getContext().getResources().getDrawable(keyId);
            dr.setBounds(left, top, right, bottom);
            dr.draw(canvas);
            canvas.drawText(key.label.toString(), key.x + (key.width / 2.0f), key.y + (key.height / 2) + (textSize / 2), mPaint);
        }
    }
}
