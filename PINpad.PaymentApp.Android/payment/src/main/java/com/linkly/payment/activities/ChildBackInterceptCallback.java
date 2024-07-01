package com.linkly.payment.activities;

/*
Should return true if consuming the back event.
 */
public interface ChildBackInterceptCallback {
    boolean handleOnBackPressed();
}
