package com.linkly.launcher.fragments;


import static com.linkly.launcher.ServiceFrontEnd.ACCESSCODE_ADMIN_MENU;
import static com.linkly.launcher.ServiceFrontEnd.ACCESSCODE_EXIT;

import android.os.Bundle;
import android.view.View;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.linkly.launcher.InputActivity;
import com.linkly.launcher.access.AccessCodeCheckCallbacks;

/**
 * Built to be used in place of ViewPager Fragment that should be protected by AccessCode.
 * It is important to NOT display the protected content by default as it would easily be vulnerable
 * to UI attack vectors, so this Fragment provides a simple way to provide a default that opens
 * the authentication/Access-prompt flow and only if/when that is successful should the protected
 * Fragment be injected into the ViewPager data set.
 */
public class UnauthenticatedFragment extends Fragment {
    public static UnauthenticatedFragment newInstance() {
        return new UnauthenticatedFragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                InputActivity.buildAuthActivityResultCallback(
                        ACCESSCODE_ADMIN_MENU, (AccessCodeCheckCallbacks) requireActivity()));
        registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                InputActivity.buildAuthActivityResultCallback(
                        ACCESSCODE_EXIT, (AccessCodeCheckCallbacks) requireActivity()));
    }
}
