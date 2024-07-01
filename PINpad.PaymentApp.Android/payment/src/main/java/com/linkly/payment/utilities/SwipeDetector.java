package com.linkly.payment.utilities;


import android.view.MotionEvent;
import android.view.View;

import timber.log.Timber;

public class SwipeDetector implements View.OnTouchListener {

    private static final String PLEASE_PASS_INSTANCE = "please pass SwipeDetector.onSwipeEvent Interface instance";
    private static final int MIN_DISTANCE = 100;
    private float downX;
    private float downY;
    private final View v;

    private OnSwipeEvent swipeEventListener;


    public SwipeDetector(View v) {
        this.v = v;
        v.setOnTouchListener(this);
    }

    public void setOnSwipeListener(OnSwipeEvent listener) {
        try {
            swipeEventListener = listener;
        }
        catch (ClassCastException e) {
            Timber.e(PLEASE_PASS_INSTANCE);
        }
    }


    public void onRightToLeftSwipe() {
        if (swipeEventListener!=null)
            swipeEventListener.swipeEventDetected(v,SwipeTypeEnum.RIGHT_TO_LEFT);
        else
            Timber.e(PLEASE_PASS_INSTANCE);
    }

    public void onLeftToRightSwipe(){
        if (swipeEventListener!=null)
            swipeEventListener.swipeEventDetected(v,SwipeTypeEnum.LEFT_TO_RIGHT);
        else
            Timber.e(PLEASE_PASS_INSTANCE);
    }

    public void onTopToBottomSwipe(){
        if (swipeEventListener!=null)
            swipeEventListener.swipeEventDetected(v,SwipeTypeEnum.TOP_TO_BOTTOM);
        else
            Timber.e(PLEASE_PASS_INSTANCE);
    }

    public void onBottomToTopSwipe(){
        if (swipeEventListener!=null)
            swipeEventListener.swipeEventDetected(v,SwipeTypeEnum.BOTTOM_TO_TOP);
        else
            Timber.e(PLEASE_PASS_INSTANCE);
    }

    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            downX = event.getX();
            downY = event.getY();
            return true;
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            float deltaX = downX - event.getX();
            float deltaY = downY - event.getY();
            boolean clicked = (Math.abs(deltaX) > Math.abs(deltaY)) ? doHorizontalScroll(deltaX) : doVerticalScroll(deltaY);

            if (clicked) {
                v.performClick();       // This should be called on ACTION_UP, whenever returning 'true' (according to SO).
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private boolean doHorizontalScroll(float deltaX) {
        if (Math.abs(deltaX) > MIN_DISTANCE) {
            // left or right
            if (deltaX < 0) {
                this.onLeftToRightSwipe();
                return true;
            }
            if (deltaX > 0) {
                this.onRightToLeftSwipe();
                return true;
            }
        }
        return false;
    }

    private boolean doVerticalScroll(float deltaY) {
        if (Math.abs(deltaY) > MIN_DISTANCE) {
            // top or down
            if (deltaY < 0) {
                this.onTopToBottomSwipe();
                return true;
            }
            if (deltaY > 0) {
                this.onBottomToTopSwipe();
                return true;
            }
        }
        return false;
    }

    public interface OnSwipeEvent
    {
        void swipeEventDetected(View v, SwipeTypeEnum swipeTypeEnum);
    }

    public enum SwipeTypeEnum
    {
        RIGHT_TO_LEFT,LEFT_TO_RIGHT,TOP_TO_BOTTOM,BOTTOM_TO_TOP
    }

}
