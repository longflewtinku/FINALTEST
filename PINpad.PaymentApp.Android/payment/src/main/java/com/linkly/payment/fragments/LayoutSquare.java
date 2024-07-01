package com.linkly.payment.fragments;


import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

public class LayoutSquare extends FrameLayout {
    public LayoutSquare(Context context) {
        super(context);
    }

    public LayoutSquare(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LayoutSquare(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //noinspection SuspiciousNameCombination
        super.onMeasure(widthMeasureSpec, widthMeasureSpec);
    }
}