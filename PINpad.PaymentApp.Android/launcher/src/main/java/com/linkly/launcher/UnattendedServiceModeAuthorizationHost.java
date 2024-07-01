package com.linkly.launcher;

/*
Interface aiding the conveying of AuthHost isUnattendedServiceModeAdminAccessGranted to a wider
 audience.
 */
public interface UnattendedServiceModeAuthorizationHost {
    boolean isUnattendedServiceModeAdminAccessGranted();
    void setUnattendedServiceModeAdminAccessGranted(boolean isGranted);
}
