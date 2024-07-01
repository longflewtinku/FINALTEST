package com.linkly.launcher.fragments;

import static com.linkly.launcher.ServiceFrontEnd.ACCESSCODE_ADMIN_MENU;
import static com.linkly.launcher.ServiceFrontEnd.ACCESSCODE_EXIT;
import static com.linkly.libpositivesvc.downloader.DownloadDirector.tmsSystem.PaxStoreTMS;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.linkly.launcher.AuthHost;
import com.linkly.launcher.InfoScreen;
import com.linkly.launcher.InputActivity;
import com.linkly.launcher.R;
import com.linkly.launcher.access.AccessCodeCheckCallbacks;
import com.linkly.libmal.global.util.DisplayKiosk;
import com.linkly.libpositivesvc.downloader.DownloadDirector;

import timber.log.Timber;

/*
Formerly ServiceFrontEnd (Activity), now abstracted to a Fragment as part of AuthHost Mechanism
 upgrade. The Auth Mechanism is scoped to the parent Activity (ServiceFrontEnd), which should implement
 AuthHost interface.

 So far, AuthHost upgrade is only for Unattended Modes.
 */
public class MenuHostFragment extends Fragment {
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager2 mViewPager;
    private boolean accessCodePromptRunning = false;
    private ActivityResultLauncher<Intent> adHocAdminAuthLauncher;
    private ActivityResultLauncher<Intent> adHocExitAuthLauncher;

    private ViewPager2.OnPageChangeCallback mPageChangeCallback = new ViewPager2.OnPageChangeCallback() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            super.onPageScrolled(position, positionOffset, positionOffsetPixels);
            Timber.d("onPageScrolled...position: %d", position);
            if (position == 0 && positionOffset > 0.2f) {
                Timber.d("...ViewPager content lock check...");
                // IFF access isn't already granted then abort the swipe and launch Access Code
                //  Protection (aka auth challenge).
                if (!((AuthHost)requireActivity()).isAdminMenuAccessGranted()
                        && mViewPager != null) {
                    Timber.d("...ViewPager content is LOCKED, resetting current page...");
                    accessCodeProtection(ACCESSCODE_ADMIN_MENU);
                    mViewPager.setCurrentItem(0);
                }
            }
        }

        @Override
        public void onPageSelected(int position) {
            super.onPageSelected(position);
            Timber.d("onPageSelected...position: %d", position);
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            super.onPageScrollStateChanged(state);
            Timber.d("onPageScrollStateChanged...state: %d", state);
        }
    };

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        return inflater.inflate(R.layout.fragment_menu_host, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewPager = view.findViewById(R.id.container);

        DownloadDirector.systemInUse = PaxStoreTMS;

        adHocAdminAuthLauncher = registerForActivityResult(
                new StartActivityForResult(),
                InputActivity.buildAuthActivityResultCallback(
                        ACCESSCODE_ADMIN_MENU, (AccessCodeCheckCallbacks) requireActivity()));
        adHocExitAuthLauncher = registerForActivityResult(
                new StartActivityForResult(),
                InputActivity.buildAuthActivityResultCallback(
                        ACCESSCODE_EXIT, (AccessCodeCheckCallbacks) requireActivity()));

        if (mViewPager != null) {
            mViewPager.setCurrentItem(0);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Timber.d("onResume...");
        // Force recreation of adapter data to branch based on auth state. It is necessary to
        //  recreate the Adapter to do this with ViewPager2. This comes at the consequence of
        //  the ViewPager2 resetting its current page to zero but that was the intention of the
        //  prior implementation anyway.

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        /*
         * The {@link SectionsPagerAdapter} that will provide
         * fragments for each of the sections. We use a
         * {@link FragmentPagerAdapter} derivative, which will keep every
         * loaded fragment in memory. If this becomes too memory intensive, it
         * may be best to switch to a
         * {@link FragmentStatePagerAdapter}.
         */
        mSectionsPagerAdapter = new SectionsPagerAdapter(
                (AuthHost)requireActivity(),
                getChildFragmentManager(), getLifecycle());

        // Set up the ViewPager with the sections adapter.
        if (mViewPager != null) {
            mViewPager.setAdapter(mSectionsPagerAdapter);
            mViewPager.registerOnPageChangeCallback(mPageChangeCallback);
        }
    }

    @Override
    public void onDestroyView() {
        Timber.d("onDestroyView...");
        mViewPager.unregisterOnPageChangeCallback(mPageChangeCallback);
        super.onDestroyView();
    }

    // NOTE: it is super important that the content that needs to be protected is not already
    //  displayed, otherwise this AccessCode Prompt mechanism (which opens an Activity on top)
    //  will be superficial (broken) auth. This is why UnauthenticatedFragment exists.
    private void accessCodeProtection(int requestType) {

        if (accessCodePromptRunning) {
            return;
        }
        accessCodePromptRunning = true;

        InputActivity.accessCodeQuestionableProtection(
                getContext(),
                requestType,
                adHocAdminAuthLauncher,
                adHocExitAuthLauncher
        );
    }

    public static class SectionsPagerAdapter extends FragmentStateAdapter {
        private static final int AMOUNT_PAGES = 2;
        private AuthHost mAuthHost;

        public SectionsPagerAdapter(AuthHost authHost, @NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
            super(fragmentManager, lifecycle);
            mAuthHost = authHost;
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            if (position == 1) {
                if (mAuthHost.isAdminMenuAccessGranted()) {
                    return DownloadMenu.newInstance();
                } else {
                    return UnauthenticatedFragment.newInstance();
                }
            } else {
                // fail closed
                return InfoScreen.newInstance();
            }
        }

        @Override
        public int getItemCount() {
            return AMOUNT_PAGES;
        }
    }
}
