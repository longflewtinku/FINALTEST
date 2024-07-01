package com.linkly.payment.activities;

/*
An interface designed to provide communication between a Fragment and its parent Activity, replacing
 Activity-to-AlertDialog callback communication.
 */
public interface AuthHost {
    void onAuthSubmission(String input);
    void onAuthCancellation();
    boolean isDevMenuAccessGranted();
}
