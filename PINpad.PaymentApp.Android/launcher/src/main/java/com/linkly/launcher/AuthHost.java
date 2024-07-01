package com.linkly.launcher;

import android.content.Intent;

/*
An interface designed to provide communication between a Fragment and its parent Activity, replacing
 inter-activity communication that was using setResult.
 */
public interface AuthHost {
    void onAuthSubmission(Intent intent, int pwdType);
    void onAuthCancellation();
    boolean isUnattendedServiceModeAdminAccessGranted();
    boolean isAdminMenuAccessGranted();
}
