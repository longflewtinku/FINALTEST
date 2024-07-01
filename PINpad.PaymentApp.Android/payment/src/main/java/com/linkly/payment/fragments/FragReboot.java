package com.linkly.payment.fragments;

import static com.linkly.libpositive.messages.IMessages.APP_REBOOT_EVENT;
import static com.linkly.libui.IUIDisplay.ACTIVITY_ID.ACT_REBOOT;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProviders;

import com.linkly.libengine.engine.Engine;
import com.linkly.libengine.jobs.EFTJobScheduleEvent;
import com.linkly.libmal.MalFactory;
import com.linkly.libui.IUIDisplay;
import com.linkly.payment.R;
import com.linkly.payment.databinding.ActivityTransBinding;
import com.linkly.payment.utilities.UIUtilities;
import com.linkly.payment.viewmodel.FragStandardViewModel;

import java.lang.ref.WeakReference;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import timber.log.Timber;

public class FragReboot extends BaseFragment<ActivityTransBinding, FragStandardViewModel> {

    public static final String TAG = FragReboot.class.getSimpleName();
    private FragStandardViewModel fragStandardViewModel;
    private Timer timer;
    public int rebootTimeout = 10;
    public TextView counter;
    private FragReboot.RebootHandler mHandler = null;
    private static int delayedCount = 0;

    public static FragReboot newInstance() {
        Bundle args = new Bundle();
        FragReboot fragment = new FragReboot();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public int getBindingVariable() {
        return com.linkly.payment.BR.fragStandardViewModel;
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_reboot;
    }

    @Override
    public FragStandardViewModel getViewModel() {
        fragStandardViewModel = ViewModelProviders.of(this).get(FragStandardViewModel.class);
        fragStandardViewModel.init(ACT_REBOOT);
        return fragStandardViewModel;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);

        SetHeader(false, false);

        TextView title  = v.findViewById(R.id.Title);
        if (title != null) {
            title.setText(Engine.getDep().getPrompt(IUIDisplay.String_id.STR_SYSTEM_REBOOTING));
        }

        counter = v.findViewById(R.id.Counter);

        mHandler = new FragReboot.RebootHandler(this);
        Button cancel = v.findViewById(R.id.cancel_button);
        UIUtilities.borderTransparentButton(getActivity(),cancel);
        if (delayedCount < 2) {
            if (cancel != null) {
                cancel.setText(Engine.getDep().getPrompt(IUIDisplay.String_id.STR_DELAY_5_MINUTES));

                cancel.setOnClickListener(v1 -> {
                    timer.cancel();
                    timer = null;
                    delayedCount++;
                    Calendar d = Calendar.getInstance();
                    d.add(Calendar.MINUTE, 5);
                    Long triggerTime = d.getTime().getTime();
                    EFTJobScheduleEvent job = new EFTJobScheduleEvent(EFTJobScheduleEvent.EventType.CREATE, APP_REBOOT_EVENT, triggerTime);
                    Engine.getDep().getJobs().schedule(requireContext().getApplicationContext(), job);
                    getBaseActivity().returnToMainMenu();
                });
            }
        } else {
            cancel.setVisibility(View.GONE);
        }

        startTimer();

        return v;
    }

    private void startTimer() {

        timer = new Timer();

        timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                rebootTimeout--;
                Timber.i("scheduleAtFixedRate:" + rebootTimeout);
                if(rebootTimeout <= 0){
                    //Reboot
                    MalFactory.getInstance().getHardware().reboot();
                    getBaseActivity().returnToMainMenu();
                }else {
                    mHandler.obtainMessage(1).sendToTarget();
                }
            }
        }, 100, 1000);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    public static class RebootHandler extends Handler {

        private final WeakReference<FragReboot> rebootClass;

        RebootHandler(FragReboot rebootClassExternal) {
            super(Looper.myLooper());
            Timber.i("RebootHandler created");
            rebootClass = new WeakReference<FragReboot>(rebootClassExternal);

        }
        public void handleMessage(Message msg) {
            Timber.i("RebootHandler handleMessage");
            FragReboot reboot = rebootClass.get();
            if (reboot != null) {
                reboot.counter.setText("" + reboot.rebootTimeout); //this is the textview
            }
        }

    }

}





