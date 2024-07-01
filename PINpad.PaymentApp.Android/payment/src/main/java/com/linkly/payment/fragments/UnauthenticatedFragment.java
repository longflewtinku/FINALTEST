package com.linkly.payment.fragments;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.linkly.payment.R;

import timber.log.Timber;

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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_unauthenticated, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button unlockButton = view.findViewById(R.id.btn_unlock);
        if (unlockButton != null) {
            unlockButton.setOnClickListener(view1 -> {
                Timber.d("onClick[unlockButton]...");
                // Button currently has no action, wiring up to show the popup is low ROI.
            });
        }
    }
}
