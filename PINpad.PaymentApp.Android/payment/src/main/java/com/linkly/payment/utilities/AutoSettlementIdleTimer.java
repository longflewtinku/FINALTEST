package com.linkly.payment.utilities;

import android.os.CountDownTimer;

import com.linkly.libmal.MalFactory;

import timber.log.Timber;

// TODO: change to workmanager.
public class AutoSettlementIdleTimer {

    // Helper class for AutoSettlementWatcher.
    // Implements CountdownTimer with "finished" flag for called to check if timer finished.
    // Flag will be cleared if timer armed again.
    static class IdleCountDownTimer extends CountDownTimer {
        private static final int COUNTDOWN_TIMER_INTERVAL_MS = 1000; // 1 second
        private boolean finished = false;

        IdleCountDownTimer(long millisInFuture) {
            super(millisInFuture, COUNTDOWN_TIMER_INTERVAL_MS);
        }

        @Override
        public void onTick(long l) {
            // The method is an intentionally-blank override.
        }

        private void restart() {
            synchronized (this) {
                finished = false;
                this.cancel();
                this.start();
            }
        }

        private void cancelInt() {
            synchronized (this) {
                finished = false;
                this.cancel();
            }
        }

        private boolean isFinished() {
            return finished;
        }

        @Override
        public void onFinish() {
            synchronized (this) {
                Timber.i("IdleCountDownTimer Finished");
                this.cancel();
                finished = true;
                // IdleCountDownTimer Finished, check conditions for Autosettlement
                AutoSettlementWatcher.onAutoSettlementIdleTimerFinished(MalFactory.getInstance().getMalContext());
            }
        }

    }

    IdleCountDownTimer idleCountDownTimer;

    AutoSettlementIdleTimer(long millisInFuture) {
        idleCountDownTimer = new IdleCountDownTimer(millisInFuture);
        idleCountDownTimer.start();
    }

    boolean isFinished() {
        return idleCountDownTimer.isFinished();
    }

    void restart() {
        idleCountDownTimer.restart();
    }

    void cancel() {
        idleCountDownTimer.cancelInt();
    }

}
