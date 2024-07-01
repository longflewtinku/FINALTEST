package com.linkly.payment.fragments;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.linkly.libengine.engine.Engine;
import com.linkly.libmal.global.util.DisplayKiosk;
import com.linkly.payment.R;
import com.linkly.payment.activities.ActScreenSaver;
import com.linkly.payment.activities.ChildBackInterceptCallback;
import com.linkly.payment.activities.MenuHost;
import com.linkly.payment.activities.NestedMenuHost;
import com.linkly.payment.activities.PhysicalKeyEventDispatcher;
import com.linkly.payment.activities.PhysicalKeyEventListener;
import com.linkly.payment.menus.IMenu;
import com.linkly.payment.menus.MenuButton;
import com.linkly.payment.menus.MenuItems;
import com.linkly.payment.utilities.AutoSettlementWatcher;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;


public class FragMenu extends Fragment implements View.OnClickListener, PhysicalKeyEventListener, NestedMenuHost {
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_ALLOW_PHYSICAL_KEY_CONTROL = "allowPhysicalKeyControl";
    private GridView listView;
    private MenuButton btnMenu;
    // A backstack for menus!
    private ArrayList<IMenu> activeMenu;
    private boolean isBackEnabled = false;
    private boolean isPhysicalKeyListeningAllowed = false;

    private final ChildBackInterceptCallback mOnBackPressedCallback = () -> {
        Timber.d("handleOnBackPressed...isBackEnabled: %b", isBackEnabled);
        if (isBackEnabled) {
            back(true);
            return true;
        }
        return false;
    };

    // newInstance relies on this no args constructor, don't add any other form of initialization.
    public FragMenu() {
        // no-op
    }

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static FragMenu newInstance(IMenu initialMenu, boolean shouldAllowPhysicalKeyListening) {
        FragMenu fragment = new FragMenu();
        Bundle args = new Bundle();
        args.putBoolean(ARG_ALLOW_PHYSICAL_KEY_CONTROL, shouldAllowPhysicalKeyListening);
        fragment.setArguments(args);
        /*Get The MainMenu items to render from the engine*/
        // TODO FIXME anti-pattern.
        fragment.activeMenu = new ArrayList<>();
        fragment.activeMenu.add(initialMenu);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle initialArgs = getArguments();
        Timber.d("onCreate...initialArgs is null?: %b", initialArgs == null);
        if (initialArgs != null && initialArgs.containsKey(ARG_ALLOW_PHYSICAL_KEY_CONTROL)) {
            isPhysicalKeyListeningAllowed = initialArgs.getBoolean(ARG_ALLOW_PHYSICAL_KEY_CONTROL);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_menu, container, false);
        rootView.setOnClickListener(this);

        /*Construct the MainMenu */
        listView = rootView.findViewById(R.id.adminMenuList);

        /*Construct the Item Adaptor and set it*/
        btnMenu = new MenuButton(rootView.getContext().getApplicationContext(), activeMenu.get(0).getMenuItems());
        listView.setAdapter(btnMenu);

        rootView.setOnFocusChangeListener((v, hasFocus) -> {
            Timber.d("onFocusChange...hasFocus: %b", hasFocus);
            if (!hasFocus) {
                back(true);
            }
        });

        /*Register the Click Event for the List Items */
        listView.setOnItemClickListener((parent, view, position, id) -> selectView(position));

        // important for _this_ Fragment to intercept key events (still, seems broken).
        listView.setFocusableInTouchMode(true);
        listView.requestFocus();

        isBackEnabled = false;

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        Timber.d("onResume...isPhysicalKeyListeningAllowed: %b", isPhysicalKeyListeningAllowed);
        refreshMenuDisplay();

        // Interim manual callback implementation in lieue of fully functioning OnBackPressedCallback.
        ((MenuHost)requireActivity()).registerBackListener(mOnBackPressedCallback);
        if (isPhysicalKeyListeningAllowed) {
            ((PhysicalKeyEventDispatcher)requireActivity()).registerListener(this);
        }
    }

    @Override
    public void onClick(View v) {

        ActScreenSaver.resetScreenSaver(getContext().getApplicationContext());
        AutoSettlementWatcher.resetIdleState();
    }

    @Override
    public void onKeyEvent(int keyCode, KeyEvent event) {
        Timber.d("onKeyEvent...");
        if (!isPhysicalKeyListeningAllowed) {
            return;
        }
        // TODO FIXME dangerous range assuming underlying values of OS enum.
        boolean numberKeyed = keyCode >= KeyEvent.KEYCODE_1 && keyCode <= KeyEvent.KEYCODE_9;
        if (numberKeyed) {
            myOnKeyDown(keyCode);
        }
    }

    @Override
    public void onPause() {
        Timber.d("onPause...");
        ((MenuHost)requireActivity()).unregisterBackListener(mOnBackPressedCallback);
        if (isPhysicalKeyListeningAllowed) {
            ((PhysicalKeyEventDispatcher)requireActivity()).unregisterListener(this);
        }
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        listView = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (listView != null) {
            listView = null;
        }
    }

    private void selectView(int position) {
        Timber.d("selectView...activeMenu size: %d", activeMenu.size());
        IMenu iMenu = activeMenu.get(activeMenu.size() - 1);
        if (iMenu != null) {
            List<MenuItems> iMenuList = iMenu.getMenuItems();
            if (iMenuList != null && position < iMenuList.size()) {
                IMenu ret = iMenu.selectMenuItem(iMenuList.get(position), Engine.getDep());
                if (ret != null && ret.getClass() != activeMenu.get(activeMenu.size() - 1).getClass()) {
                    Timber.d("...adding subMenu to activeMenu list...");
                    activeMenu.add(ret);
                    refreshMenuDisplay();

                    listView.animate().translationY(0);

                    isBackEnabled = true;
                }
            }
        }
        ActScreenSaver.resetScreenSaver(requireContext());
        AutoSettlementWatcher.resetIdleState();

        refreshMenuDisplay();
    }

    // TODO FIXME dangerous stuff, assuming underlying values of OS enum.
    private void myOnKeyDown(int keyCode) {
        //do whatever you want here
        int key = keyCode - KeyEvent.KEYCODE_1;
        selectView(key);
    }

    private void refreshMenuDisplay() {
        Timber.d("refreshMenuDisplay...activeMenu size: %d", activeMenu.size());

        // Display our back button if required
        DisplayKiosk.getInstance().onResume(isBackEnabled);

        for (int i = 0; i < activeMenu.size(); i++) {
            IMenu m = activeMenu.get(i);
            if (m != null) {
                activeMenu.set(i, m.reloadMenu());
            }
        }
        Timber.d("...modified activeMenu size: %d", activeMenu.size());

        btnMenu = new MenuButton(getContext().getApplicationContext(), activeMenu.get(activeMenu.size() - 1).getMenuItems());
        if (listView != null) {
            listView.setAdapter(btnMenu);
        }

        ((MenuHost) getActivity()).notifyMenuRefreshed();
    }

    /**
     * Call this Function to Go Up a level in the Menu Tree
     * Might want to make this Public to allow other classes to call it
     */
    private void back(boolean isBackKey) {
        Timber.d("back...isBackKey: %b, activeMenu size: %d", isBackKey, activeMenu.size());
        if (activeMenu.size() > 1) {

            activeMenu.remove(activeMenu.size() - 1);

            if (isBackKey) {
                isBackEnabled = false;
            }
            refreshMenuDisplay();
        }
    }

    public String getActiveMenuTitle() {
        return activeMenu.get(activeMenu.size() - 1).getMenuTitle();
    }

    @Override
    public void onChildBackPressed(int position) {
        Timber.d("onChildBackPressed...position: %d", position);
        back(true);
    }
}