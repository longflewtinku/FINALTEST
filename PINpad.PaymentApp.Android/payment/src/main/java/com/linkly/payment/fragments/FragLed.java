package com.linkly.payment.fragments;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.fragment.app.Fragment;

import com.linkly.libengine.engine.Engine;
import com.linkly.libengine.engine.transactions.TransRec;
import com.linkly.libengine.engine.transactions.properties.LedStatus;
import com.linkly.libengine.workflow.WorkflowScheduler;
import com.linkly.payment.R;
import com.linkly.payment.activities.ActIdle;
import com.linkly.payment.activities.AppMain;

import java.lang.ref.WeakReference;
import java.util.Timer;
import java.util.TimerTask;

import timber.log.Timber;

public class FragLed extends Fragment {

    private View view;
    ImageView led1;
    ImageView led2;
    ImageView led3;
    ImageView led4;

    public void updateLed(Context context) {
        if (Engine.getDep().getCurrentTransaction() != null) {
            LedStatus ledStatus = Engine.getDep().getCurrentTransaction().getCard().getLedStatus();
            if (ledStatus != null) {
                ledStatus.updateLeds(context);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        refresh(requireContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_leds, container, false);

        led1 = (ImageView) view.findViewById(R.id.ctls_led1);
        led2 = (ImageView) view.findViewById(R.id.ctls_led2);
        led3 = (ImageView) view.findViewById(R.id.ctls_led3);
        led4 = (ImageView) view.findViewById(R.id.ctls_led4);

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        AppMain.addWatcher( this );
    }

    private void hideLeds() {
        if (view == null)
            return;

        LinearLayout leds = (LinearLayout) view.findViewById(R.id.ctls_leds);

        if (leds != null) {
            leds.setVisibility(GONE);
        }
    }

    public void refresh(Context context) {


        LedStatus ledStatus;
        TransRec trans = Engine.getDep().getCurrentTransaction();
        if (trans == null) {
            hideLeds();
            return;
        }

        if (!WorkflowScheduler.isTransactionRunning()) {
            hideLeds();
            return;
        }

        updateLed(context);
        ledStatus = trans.getCard().getLedStatus();

        if (ledStatus != null) {

            int resId1 = ledStatus.getResIdLed1();
            int resId2 = ledStatus.getResIdLed2();
            int resId3 = ledStatus.getResIdLed3();
            int resId4 = ledStatus.getResIdLed4();

            LinearLayout leds = (LinearLayout) view.findViewById(R.id.ctls_leds);

            if (leds != null) {

                if (ledStatus.isVisible()) {
                    leds.setVisibility(VISIBLE);
                } else {
                    leds.setVisibility(GONE);
                    return;
                }
            }

            if (led1 != null) {
                led1.setColorFilter(resId1);
            }

            if (led2 != null) {
                led2.setColorFilter(resId2);
            }

            if (led3 != null) {
                led3.setColorFilter(resId3);
            }

            if (led4 != null) {
                led4.setColorFilter(resId4);
            }

            if( null != leds ) {
                leds.invalidate();
            }
        }

    }


    private static Timer ledTimer = null;

    public static void startLedTimer(FragLed ctlsLeds) {
        if (ledTimer == null && ctlsLeds != null) {
            Timber.i("startLedTimer");
            ledTimer = new Timer();
            ledTimer.schedule(new FragLed.FlashTimerTask(ctlsLeds), 0, 10);
        }
    }


    public static void pauseLedTimer() {
        if (ledTimer != null) {
            Timber.i("pauseLedTimer");
            ledTimer.cancel();
            ledTimer = null;
        }
    }
    static long lastTime = 0;
    private static class FlashTimerTask extends TimerTask {

        private final WeakReference<FragLed> mCtlsLeds;

        public FlashTimerTask(FragLed ctlsLeds) {
            super();
            mCtlsLeds = new WeakReference<>(ctlsLeds);
        }

        @Override
        public void run() {
            ActIdle m = AppMain.getApp().getAppActivity();

            if (m != null) {
                m.runOnUiThread(() -> {
                    try {
                        mCtlsLeds.get().refresh(m.getApplicationContext());
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                });
            }
        }
    }



}
