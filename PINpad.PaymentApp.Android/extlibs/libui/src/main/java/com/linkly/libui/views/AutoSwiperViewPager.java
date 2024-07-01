package com.linkly.libui.views;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.viewpager.widget.ViewPager;

public class AutoSwiperViewPager extends ViewPager {

    private Handler handler;
    private Runnable runnable;

    public AutoSwiperViewPager(Context context) {
        super(context);
    }

    public AutoSwiperViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void activate() {

        if (handler == null) {
            handler = new Handler(Looper.myLooper());
        }

        if (runnable == null) {
            runnable = new Runnable() {
                public void run() {
                    setCurrentItem(nextItem(1), true);
                    handler.postDelayed(this, 5000);
                }
            };
        }
        if (runnable != null)
            handler.postDelayed(runnable, 5000);

    }

    public void deactivate() {
        if (handler!= null)
            handler.removeCallbacks(runnable);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        // Never allow swiping to switch between pages
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Never allow swiping to switch between pages
        setCurrentItem(nextItem(1), true);

        // reset the timer
        deactivate();
        activate();
        return false;
    }

    private int nextItem(int i) {
        int page = getCurrentItem() + i;
        if (getAdapter().getCount() == page) {
            page = 0;
        }
        return page;
    }
}
