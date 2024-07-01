package com.linkly.launcher.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.linkly.launcher.BrandingConfig;
import com.linkly.launcher.R;
import com.linkly.libconfig.MalConfig;

import timber.log.Timber;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link OutOfServiceFragment} factory method to
 * create an instance of this fragment.
 */
public class OutOfServiceFragment extends Fragment {

    public static OutOfServiceFragment newInstance() {
        return new OutOfServiceFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_out_of_service, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupMessage(view);
    }

    private void setupMessage(View parentView) {
        TextView messageTextView = parentView.findViewById(R.id.message);
        if (messageTextView != null) {
            try {
                messageTextView.setShadowLayer(8, 1, 1, BrandingConfig.getInstance()
                        .getBrandDisplayButtonColour().getValue());
            } catch (NullPointerException e) {
                // BrandingConfig not initialised for whatever reason (likely during installation of suite)
                Timber.e("BrandingConfig was null!");
            }
            try {
                String lockedDownMessage = MalConfig.getInstance().getProfileCfg()
                        .getUnattendedLockedDownMessage()
                        .get(getResources().getConfiguration().orientation);
                messageTextView.setText(lockedDownMessage);
            } catch (NullPointerException e) {
                // ProfileConfig not initialised for whatever reason (likely during installation of suite)
                Timber.e("ProfileConfig was null!");
            }
        }
    }

}
