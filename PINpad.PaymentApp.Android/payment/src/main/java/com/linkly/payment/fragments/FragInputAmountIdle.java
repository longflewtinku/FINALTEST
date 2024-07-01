package com.linkly.payment.fragments;

import static com.linkly.libengine.engine.EngineManager.TransType.SALE;
import static com.linkly.libui.IUICurrency.EAmountFormat.FMT_AMT_SHOW_SYMBOL;
import static com.linkly.libui.IUIDisplay.ACTIVITY_ID.ACT_INPUT_AMOUNT_IDLE;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.linkly.libengine.config.PayCfg;
import com.linkly.libengine.config.PayCfgFactory;
import com.linkly.libengine.engine.Engine;
import com.linkly.libengine.engine.transactions.TransRec;
import com.linkly.libengine.engine.transactions.properties.TAmounts;
import com.linkly.libengine.engine.transactions.properties.TAudit;
import com.linkly.libengine.workflow.CancelAmountIdle;
import com.linkly.libengine.workflow.Workflow;
import com.linkly.libengine.workflow.WorkflowScheduler;
import com.linkly.libui.views.CustomEditText;
import com.linkly.payment.R;
import com.linkly.payment.activities.AppBarHost;
import com.linkly.payment.application.AppCallbacks;
import com.linkly.payment.viewmodel.FragKeyboardViewModel;

import java.util.Objects;

import timber.log.Timber;

public class FragInputAmountIdle extends BaseInputAmount {

    private static final String KEY_SHOULD_FORCE_SALE_TRANSREC = "shouldForceSaleTransRec";

    public static final String TAG = FragInputAmountIdle.class.getSimpleName();
    private FragKeyboardViewModel fragKeyboardViewModel;
    private boolean mShouldForceSaleTransRec = false;

    public static FragInputAmountIdle newInstance(boolean shouldForceSaleTransRec) {
        FragInputAmountIdle frag = new FragInputAmountIdle();
        Bundle args = new Bundle();
        args.putBoolean(KEY_SHOULD_FORCE_SALE_TRANSREC, shouldForceSaleTransRec);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public int getBindingVariable() {
        return com.linkly.payment.BR.fragKeyboardViewModel;
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_keyboard;
    }

    @Override
    public FragKeyboardViewModel getViewModel() {
        fragKeyboardViewModel = ViewModelProviders.of(this).get(FragKeyboardViewModel.class);
        fragKeyboardViewModel.init(ACT_INPUT_AMOUNT_IDLE);
        return fragKeyboardViewModel;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Timber.d("onCreate...savedInstanceState: %s, args: %s", savedInstanceState, getArguments());
        if (getArguments() != null
                && getArguments().containsKey(KEY_SHOULD_FORCE_SALE_TRANSREC)) {
            mShouldForceSaleTransRec = getArguments().getBoolean(KEY_SHOULD_FORCE_SALE_TRANSREC);
        }
        Timber.d("...resolved args: shouldForceSaleTransRec: %b", mShouldForceSaleTransRec);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        Timber.d("onViewStateRestored...savedInstanceStateL %s", savedInstanceState);
        if (savedInstanceState != null
                && savedInstanceState.containsKey(KEY_SHOULD_FORCE_SALE_TRANSREC)) {
            mShouldForceSaleTransRec = savedInstanceState.getBoolean(KEY_SHOULD_FORCE_SALE_TRANSREC);
            Timber.d("...resolved shouldForceSaleTransRec: %b", mShouldForceSaleTransRec);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Timber.d("onViewCreated...shouldForceSaleTransRec: %b", mShouldForceSaleTransRec);
    }

    @Override
    public void onStart() {
        super.onStart();
        Timber.d("onStart[FragInputAmountIdle]...");
        // Instead of setting the new TransRec (effectively) in ActMainMenu onCreate, which runs into
        //  conflict with e.g. PreSettlement which operates on its own TransRec, wait until user
        //  interaction (or automated finishing of foreground Activity) until this fragment is shown
        //  because that is actually when it is needed.
        if (mShouldForceSaleTransRec) {
            Timber.d("...forcing new SALE TransRec...");
            Engine.getDep().resetCurrentTransaction(new TransRec(SALE, Engine.getDep())); // Default transaction type as SALE during the first launch
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Timber.d("onResume[FragInputAmountIdle]...");
        // Controlling the visibility at this point allows the change to be coordinated with the
        //  display of this Fragment.
        ((AppBarHost)requireActivity()).showHeader(false);
        ((AppBarHost)requireActivity()).showTabs(false);
    }

    @Override
    protected void showScreen(View v) {
        showScreen(v, Objects.requireNonNull(fragKeyboardViewModel.getFragData().getValue()));
    }

    @Override
    protected void updateScreenSaver() {
        // do nothing
    }

    @Override
    protected void setupFloatingActionButton(View v) {
        FloatingActionButton fab = v.findViewById(R.id.fab);
        fab.setOnClickListener(view -> handleBackPressed());
    }

    private void handleBackPressed() {
        // always return to main menu screen on back press
        requireActivity().onBackPressed();
        Timber.e("Back Pressed on Amount Entry Screen");
    }

    @Override
    public void onDoneClicked() {
        //Done Handler
        if (isInputValid()) {
            // get our config.
            PayCfg payCfg = new PayCfgFactory().getConfig(requireContext());
            CustomEditText editText = getView().findViewById(R.id.txtAmount);
            String digits = editText.getText().toString().replaceAll("\\D", "");
            digits = Engine.getDep().getFramework().getCurrency().formatUIAmountResponse(digits, payCfg.getCountryCode());
            TransRec trans = Engine.getDep().getCurrentTransaction();
            if (trans == null) {
                Timber.w("Transaction object is null, defaulted to SALE!");
                trans = new TransRec(SALE, Engine.getDep());
            } else {
                // Initializing the audit. Update with latest date/time
                trans.setAudit(new TAudit(Engine.getDep()));
            }
            TAmounts amounts = new TAmounts(payCfg);
            amounts.setAmountUserEntered(digits);
            // check we're setting correct amount depending on trans type. if cash only (aka withdrawl), i.e. NOT purchase with cashback, then save trans amt in cashback only
            if (trans.isCash()) {
                amounts.setCashbackAmount(Long.parseLong(digits));
            } else {
                amounts.setAmount(Long.parseLong(digits));
            }
            trans.setAmounts(amounts);
            Engine.getDep().resetCurrentTransaction(trans);
            Engine.getDep().getAppCallbacks().onTransactionFlowEntered();
            Workflow w = Engine.getAppCallbacks().getWorkflowFactory().getWorkflow(trans.getTransType());
            WorkflowScheduler.getInstance().queueWorkflow(w, false);
            String amountEntered = trans.isCash() ? Engine.getDep().getFramework().getCurrency().formatUIAmount(String.valueOf(trans.getAmounts().getCashbackAmount()),FMT_AMT_SHOW_SYMBOL, payCfg.getCountryCode()) :
                    Engine.getDep().getFramework().getCurrency().formatUIAmount(String.valueOf(trans.getAmounts().getAmount()),FMT_AMT_SHOW_SYMBOL, payCfg.getCountryCode());
            Timber.e("Amount entered : %s and transaction type : %s" , amountEntered  ,trans.getTransType());
        }
    }

    @Override
    public void onCancelClicked() {
        Timber.d("onCancelClicked...");
        // reset transaction
        TransRec trans = Engine.getDep().getCurrentTransaction();
        // NOTE: When you're at the enter amount screen a transaction has already started (a temp
        // record is generated for that purpose).
        if (trans != null) {
            trans.setCancelled(true);
            AppCallbacks.getInstance().setShouldMainMenuDisplayInputAmountIdle(
                    (trans.getTransType() == SALE));
        } else {
            Timber.e("Unexpected transaction object as null!!");
            AppCallbacks.getInstance().setShouldMainMenuDisplayInputAmountIdle(false);
        }

        // cancel action
        WorkflowScheduler.getInstance().queueWorkflow(new CancelAmountIdle(), false);
        Timber.e("Cancel pressed on Amount entry screen");
    }
}