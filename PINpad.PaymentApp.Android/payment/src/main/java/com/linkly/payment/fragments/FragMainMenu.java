package com.linkly.payment.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.preference.PreferenceManager;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.linkly.kotlinandroidtoolkit.extensionfuncs.BundleExtensionsKt;
import com.linkly.libengine.config.PayCfg;
import com.linkly.libengine.config.PayCfgFactory;
import com.linkly.libengine.engine.Engine;
import com.linkly.libui.IUIDisplay;
import com.linkly.libui.display.DisplayRequest;
import com.linkly.libui.views.ViewUtils;
import com.linkly.payment.BR;
import com.linkly.payment.R;
import com.linkly.payment.activities.ActScreenSaver;
import com.linkly.payment.activities.AppBarHost;
import com.linkly.payment.activities.AuthHost;
import com.linkly.payment.activities.BaseActivity;
import com.linkly.payment.activities.MenuHost;
import com.linkly.payment.activities.OnMenuRefreshedListener;
import com.linkly.payment.databinding.FragmentMainMenuBinding;
import com.linkly.payment.menus.IMenu;
import com.linkly.payment.menus.MainMenu;
import com.linkly.payment.menus.MenuAdmin;
import com.linkly.payment.menus.MenuDevelopment;
import com.linkly.payment.utilities.AutoSettlementWatcher;
import com.linkly.payment.utilities.Pci24HourRebootWatcher;
import com.linkly.payment.viewmodel.FragMainMenuViewModel;

import java.util.Calendar;
import java.util.Locale;

import timber.log.Timber;

/**
 * Takes the MainMenu part of ActMainMenu and isolates it in its own Fragment, un-nests
 * FragInputAmountIdle to treat them as siblings in the parent Activity.
 */
public class FragMainMenu extends BaseFragment<FragmentMainMenuBinding, FragMainMenuViewModel>
        implements AuthHost {

    public static final String TAG = FragMainMenu.class.getSimpleName();

    private ViewPager2 mViewPager;
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private AlertDialog alertToShow = null;

    private boolean isDevMenuPasswordEntered = false;

    private final OnMenuRefreshedListener mOnMenuRefreshedListener = () -> {
        Timber.d("onMenuRefreshed...");
        try {
            mSectionsPagerAdapter.notifyDataSetChanged();
        } catch (IllegalStateException e) {
            // Ideally wouldn't have this situation but it happens as a result of legacy
            //  architecture providing titles from child Fragments rather than a backing dataset.
        }
    };

    public static FragMainMenu newInstance() {
        return new FragMainMenu();
    }

    private final ViewPager2.OnPageChangeCallback mPageChangeCallback = new ViewPager2.OnPageChangeCallback() {

        @Override
        public void onPageSelected(int position) {
            super.onPageSelected(position);
            Timber.d("onPageSelected...position: %d", position);
            ActScreenSaver.resetScreenSaver(getContext().getApplicationContext());
            Engine.getAppCallbacks().onUserInteraction();
            Pci24HourRebootWatcher.resetIdle();
            AutoSettlementWatcher.resetIdleState();

            if (position == 2 && !isDevMenuAccessGranted()) {
                showDialog(requireActivity());
            }
        }
    };

    @Override
    public int getBindingVariable() {
        // TODO eventually use the correct variable when it gets built.
        return BR.fragMainMenuViewModel;
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_main_menu;
    }

    @Override
    public FragMainMenuViewModel getViewModel() {
        FragMainMenuViewModel viewModel = ViewModelProviders.of(this).get(FragMainMenuViewModel.class);
        return viewModel;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Timber.d("onViewCreated...this: %s", this);

        //Create the Menus

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(
                resolveAdapterUseCase(),
                this,
                this
        );
        ViewUtils.findViewByIdAndRun(
                view,
                R.id.container,
                "FragMainMenu ViewPager not found!",
                (ViewUtils.OnRunOperation<ViewPager2>) viewPager -> {
                    viewPager.setAdapter(mSectionsPagerAdapter);
                    // If save is enabled then DevMenu Fragment may be reused even after onDestroy!
                    viewPager.setSaveEnabled(false);
                    viewPager.registerOnPageChangeCallback(mPageChangeCallback);
                    setupTabLayout(
                            ((AppBarHost) requireActivity()).getTabLayout(),
                            viewPager
                    );
                    mViewPager = viewPager;
                }
        );
        setDefaultPage();

        ViewUtils.findViewByIdAndRun(
                view,
                R.id.main_content,
                "FragMainMenu main content LinearLayout not found!",
                (ViewUtils.OnRunOperation<LinearLayout>) mainContent -> {
                    Timber.d("[OnTouch] RUNNING!");
                    mainContent.setOnTouchListener((v, event) -> {
                        ActScreenSaver.resetScreenSaver(getContext().getApplicationContext());
                        Engine.getAppCallbacks().onUserInteraction();
                        Pci24HourRebootWatcher.resetIdle();
                        AutoSettlementWatcher.resetIdleState();
                        return false;
                    });
                }
        );

        Pci24HourRebootWatcher.setIdle();
    }

    @Override
    public void onResume() {
        super.onResume();
        Timber.d("onResume...this: %s", this);
        // Controlling the visibility at this point allows the change to be coordinated with the
        //  display of this Fragment.
        ((AppBarHost)requireActivity()).showHeader(true);
        ((AppBarHost)requireActivity()).showTabs(true);
        ((MenuHost)requireActivity()).registerOnMenuRefreshedListener(mOnMenuRefreshedListener);
        // TODO FIXME upgrade to dedicated ViewModel and Flows.
        refreshUI();
        mSectionsPagerAdapter.setUseCase(resolveAdapterUseCase());
    }

    @Override
    public void onPause() {
        super.onPause();
        ((MenuHost)requireActivity()).unregisterOnMenuRefreshedListener(mOnMenuRefreshedListener);
    }

    @Override
    public void onDestroyView() {
        Timber.d("onDestroyView...");
        mViewPager.unregisterOnPageChangeCallback(mPageChangeCallback);
        mViewPager.setAdapter(null);
        mViewPager = null;
        super.onDestroyView();
    }

    // Legacy means to provide args to screen.
    @SuppressWarnings("deprecation")
    public void setDefaultPage() {
        IUIDisplay.SCREEN_ID iScreenID = IUIDisplay.SCREEN_ID.MAIN_MENU;
        DisplayRequest displayRequest = ((BaseActivity) requireActivity()).getDisplayRequest();
        if (displayRequest != null) {
            iScreenID = BundleExtensionsKt.getSerializableCompat(
                    displayRequest.getUiExtras(), IUIDisplay.uiScreenID, IUIDisplay.SCREEN_ID.class);
        }

        int pageNumber = (iScreenID == IUIDisplay.SCREEN_ID.ADMIN_MENU) ? 1 : 0;
        ((AppBarHost) requireActivity()).getTabLayout().getTabAt(pageNumber).select();
    }

    private void setupTabLayout(TabLayout tabLayout, ViewPager2 nonNullViewPager) {
        new TabLayoutMediator(tabLayout, nonNullViewPager, (tab, position) -> {
            try {
                // TODO FIXME find a way that doesn't need to peek at the ViewPager2 Fragments.
                //  Doesn't suit ViewPager2 well since the Tabs exist before the Fragments!
                switch (position) {
                    case 0:
                    case 1:
                        FragMenu menuFrag = (FragMenu) getChildFragmentManager().findFragmentByTag("f" + position);
                        tab.setText(menuFrag.getActiveMenuTitle());
                        break;
                    case 2:
                        if (isDevMenuAccessGranted()) {
                            try {
                                FragMenu menuFrag2 = (FragMenu) getChildFragmentManager().findFragmentByTag("f" + position);
                                tab.setText(menuFrag2.getActiveMenuTitle());
                            } catch (ClassCastException e) {
                                // Occurs during transition from UnauthenticatedFragment to FragMenu
                                tab.setText(getString(R.string.menu_developer));
                            }
                        } else {
                            // Special-case for non-FragMenu Fragments like UnauthenticatedFragment.
                            tab.setText(getString(R.string.menu_developer));
                        }
                        break;
                    default:
                        // do not update title
                }
            } catch (NullPointerException e) {
                // ViewPager2 doesn't create Fragments ahead of time, and we're not doing that anymore too.
                //  So provide some static defaults to fallback to.
                switch (position) {
                    case 2:
                        tab.setText(getString(R.string.menu_developer));
                        break;
                    case 1:
                        tab.setText(getString(R.string.menu_adminitration));
                        break;
                    default:
                        tab.setText(getString(R.string.menu_main));
                }
            }
        }).attach();

        PayCfg payCfg = new PayCfgFactory().getConfig(requireContext());

        if (payCfg != null) {
            tabLayout.setSelectedTabIndicatorColor(payCfg
                    .getBrandDisplayPrimaryColourOrDefault(ContextCompat.getColor(
                            requireContext(), R.color.color_linkly_primary)));
        }
    }

    public void refreshAdapter() {
        mSectionsPagerAdapter.notifyDataSetChanged();
    }

    private void showDialog(Context context) {
        final AlertDialog.Builder alert = new AlertDialog.Builder(context);

        alert.setTitle("Dev Menu");
        alert.setMessage("Enter password");
        final EditText input = new EditText(context);

        alert.setView(input);

        alert.setOnKeyListener((dialogInterface, i, keyEvent) -> {
            if (i == KeyEvent.KEYCODE_ENTER) {

                checkPassword(input);
                if (alertToShow != null)
                    alertToShow.dismiss();
                return true;
            }
            return false;
        });
        alert.setOnDismissListener(dialog -> {
            if (!isDevMenuAccessGranted()) {
                mViewPager.setCurrentItem(1);
            }
        });
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        input.setMaxLines(1);

        alertToShow = alert.create();
        if (alertToShow.getWindow() != null) {
            alertToShow.getWindow().setSoftInputMode(
                    WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
        alertToShow.show();
    }

    private void checkPassword(EditText input) {
        Timber.d("checkPassword...dayOfYear: %d", Calendar.getInstance().get(Calendar.DAY_OF_YEAR));
        String value = input.getText().toString();
        String password = String.format( Locale.getDefault(), "3123%03d", Calendar.getInstance().get(Calendar.DAY_OF_YEAR));
        if (value.equals(password)) {
            Timber.e("DevMenu access granted.");
            isDevMenuPasswordEntered = true;
            // ViewPager2 _will_ update its dataset upon calling notifyDataSetChanged.
            mSectionsPagerAdapter.notifyDataSetChanged();
        }
    }

    private SectionsPagerAdapter.UseCase resolveAdapterUseCase() {
        IMenu adminMenu = MenuAdmin.getInstance();
        boolean isAdminMenuEnabled = (!adminMenu.getMenuItems().isEmpty());
        boolean isDevMenuEnabled = false;

        // disable DevMenu if AdminMenu not allowed
        if (!isAdminMenuEnabled) {
            isDevMenuEnabled = false;
        } else {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
            isDevMenuEnabled = preferences.getBoolean("DisplayDevMenu", true);
        }
        if (isAdminMenuEnabled && isDevMenuEnabled) {
            return SectionsPagerAdapter.UseCase.ADMINISTRATION_PLUS_DEVMENU;
        } else if (isAdminMenuEnabled) {
            return SectionsPagerAdapter.UseCase.ADMINISTRATION;
        } else {
            return SectionsPagerAdapter.UseCase.NORMAL;
        }
    }

    @Override
    public void onAuthSubmission(String input) {
        Timber.d("onAuthSubmission...");
        checkPassword(input);
        // TODO a means to notify since popup won't cause this Activity to reresume
    }

    @Override
    public void onAuthCancellation() {
        Timber.d("onAuthCancellation...");
    }

    @Override
    public boolean isDevMenuAccessGranted() {
        Timber.d("isDevMenuAccessGranted...%b", isDevMenuPasswordEntered);
        return isDevMenuPasswordEntered;
    }

    // Fixme use byte/char array instead of String to store and pass sensitive information
    private void checkPassword(String input) {
        String password = String.format(Locale.getDefault(), "3123%03d", Calendar.getInstance().get(Calendar.DAY_OF_YEAR));
        if (input.equals(password)) {
            isDevMenuPasswordEntered = true;
        }
    }

    public static class SectionsPagerAdapter extends FragmentStateAdapter {
        // Simplifies logic branching within this class and keeps Context out of it.
        public enum UseCase {
            NORMAL,
            ADMINISTRATION,
            ADMINISTRATION_PLUS_DEVMENU
        }

        private UseCase mUseCase;
        private AuthHost mAuthHost;

        public SectionsPagerAdapter(
                UseCase useCase,
                AuthHost authHost,
                @NonNull Fragment hostFragment) {
            super(hostFragment);
            Timber.d("construct[SectionsPagerAdapter]...useCase: %s", useCase.name());
            mUseCase = useCase;
            mAuthHost = authHost;
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (mUseCase) {
                case ADMINISTRATION_PLUS_DEVMENU:
                    return createFragmentForUseCaseAdministrationPlusDevMenu(position);
                case ADMINISTRATION:
                    return createFragmentForUseCaseAdministration(position);
                default:
                    return createFragmentForUseCaseNormal();
            }
        }

        @Override
        public int getItemCount() {
            switch (mUseCase) {
                case ADMINISTRATION_PLUS_DEVMENU:
                    return 3;
                case ADMINISTRATION:
                    return 2;
                default:
                    return 1;
            }
        }

        @Override
        public long getItemId(int position) {
            switch (position) {
                case 2:
                    return (mAuthHost.isDevMenuAccessGranted())? 200 : 2;
                default:
                    return position;
            }
        }

        public void setUseCase(UseCase useCase) {
            mUseCase = useCase;
            notifyDataSetChanged();
        }

        public UseCase getUseCase() {
            return mUseCase;
        }

        private Fragment createFragmentForUseCaseNormal() {
            return FragMenu.newInstance(MainMenu.getInstance(), true);
        }

        private Fragment createFragmentForUseCaseAdministration(int position) {
            if (position == 1) {
                return FragMenu.newInstance(MenuAdmin.getInstance(), false);
            }
            return FragMenu.newInstance(MainMenu.getInstance(), true);
        }

        private Fragment createFragmentForUseCaseAdministrationPlusDevMenu(int position) {
            Timber.d("createFragmentForUseCaseAdministrationPlusDevMenu...");
            switch (position) {
                case 2:
                    return (mAuthHost.isDevMenuAccessGranted())?
                            FragMenu.newInstance(MenuDevelopment.getInstance(), false)
                            : UnauthenticatedFragment.newInstance();
                case 1:
                    return FragMenu.newInstance(MenuAdmin.getInstance(), false);
                default:
                    return FragMenu.newInstance(MainMenu.getInstance(), true);
            }
        }
    }
}
