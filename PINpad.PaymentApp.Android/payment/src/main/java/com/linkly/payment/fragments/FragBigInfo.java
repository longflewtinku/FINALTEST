package com.linkly.payment.fragments;

import static com.linkly.libengine.printing.IPrintManager.PrintPreference.PRINT_PREFERENCE_DEFAULT;
import static com.linkly.libengine.printing.IPrintManager.PrintPreference.PRINT_PREFERENCE_SCREEN;
import static com.linkly.libui.IUIDisplay.ACTIVITY_ID.ACT_BIG_INFO;
import static com.linkly.libui.IUIDisplay.String_id.STR_EMPTY;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProviders;

import com.linkly.libengine.engine.Engine;
import com.linkly.libengine.printing.IPrintManager;
import com.linkly.libengine.printing.IReceipt;
import com.linkly.libmal.MalFactory;
import com.linkly.libmal.global.platform.EFTPlatform;
import com.linkly.libmal.global.printing.PrintReceipt;
import com.linkly.libmal.global.util.DisplayKiosk;
import com.linkly.libui.IUIDisplay;
import com.linkly.libui.IUIDisplay.String_id;
import com.linkly.payment.R;
import com.linkly.payment.databinding.ActivityTransBinding;
import com.linkly.payment.printing.receipts.common.PlainTextReceipt;
import com.linkly.payment.viewmodel.FragStandardViewModel;

public class FragBigInfo extends BaseFragment<ActivityTransBinding, FragStandardViewModel> {

    public static final String TAG = FragBigInfo.class.getSimpleName();
    private FragStandardViewModel fragStandardViewModel;

    public static FragBigInfo newInstance() {
        Bundle args = new Bundle();
        FragBigInfo fragment = new FragBigInfo();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public int getBindingVariable() {
        return com.linkly.payment.BR.fragStandardViewModel;
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_biginfo;
    }

    @Override
    public FragStandardViewModel getViewModel() {
        fragStandardViewModel = ViewModelProviders.of(this).get(FragStandardViewModel.class);
        fragStandardViewModel.init(ACT_BIG_INFO);
        return fragStandardViewModel;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);

        TextView text = v.findViewById(R.id.textField);
        if (text != null)
            text.setMovementMethod(new ScrollingMovementMethod());

        Button print = v.findViewById(R.id.btnPrint);
        print.setText(Engine.getDep().getPrompt(String_id.STR_PRINT_OUT));
        if (print != null) {
            print.setOnClickListener(v1 -> {
                IReceipt receiptGenerator = new PlainTextReceipt();
                PrintReceipt receiptToPrint = receiptGenerator.generateReceipt(fragStandardViewModel.getFragData().getValue().getPrompt().getValue());
                IPrintManager.PrintPreference printPreference = MalFactory.getInstance().getHardware().hasPrinter() ? PRINT_PREFERENCE_DEFAULT : PRINT_PREFERENCE_SCREEN;
                Engine.getPrintManager().printReceipt(Engine.getDep(), receiptToPrint, null, true, STR_EMPTY, printPreference, MalFactory.getInstance());
            });
            if (!EFTPlatform.isAppPrinting()) {
                print.setVisibility(View.GONE);
            }
        }

        SetHeader(false, false);

        return v;
    }

    DisplayKiosk.NavigationBarState state;

    @Override
    public void onResume() {
        super.onResume();
        state = new DisplayKiosk.NavigationBarState();
        if (fragStandardViewModel.getDisplay().getValue().getUiExtras().getBoolean(IUIDisplay.uiEnableBackButton)) {
            DisplayKiosk.getInstance().onResume(true);
        } else {
            DisplayKiosk.getInstance().enterKioskMode(requireActivity());
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        DisplayKiosk.getInstance().setNavigationBarAndButtonsState(state, true);
    }

}





