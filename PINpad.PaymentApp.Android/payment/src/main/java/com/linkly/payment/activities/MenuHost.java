package com.linkly.payment.activities;

/*
Callbacks to an Activity that hosts Menus.
ChildBackInterceptCallback allows listener to indicate whether back event is consumed or not,
simplifying parent responsibilities.
 */
public interface MenuHost {
    void registerBackListener(ChildBackInterceptCallback callback);
    void unregisterBackListener(ChildBackInterceptCallback callback);
    void registerOnMenuRefreshedListener(OnMenuRefreshedListener callback);
    void unregisterOnMenuRefreshedListener(OnMenuRefreshedListener callback);
    void notifyMenuRefreshed();
}
