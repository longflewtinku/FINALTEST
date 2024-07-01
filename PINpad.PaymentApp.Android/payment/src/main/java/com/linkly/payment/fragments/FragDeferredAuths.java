package com.linkly.payment.fragments;

import static com.linkly.libengine.engine.transactions.properties.TProtocol.MessageStatus.DEFERRED_AUTH;
import static com.linkly.libui.IUIDisplay.ACTIVITY_ID.ACT_DEFERRED_AUTHS;
import static com.linkly.libui.IUIDisplay.String_id.STR_DEFERRED_AUTH;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.linkly.libengine.engine.Engine;
import com.linkly.libengine.engine.transactions.TransRec;
import com.linkly.libengine.engine.transactions.TransRecManager;
import com.linkly.payment.R;
import com.linkly.payment.adapters.DeferredAuthsAdapter;
import com.linkly.payment.databinding.ActivityTransBinding;
import com.linkly.payment.utilities.UIUtilities;
import com.linkly.payment.viewmodel.FragStandardViewModel;

import java.util.ArrayList;
import java.util.List;

public class FragDeferredAuths extends BaseFragment<ActivityTransBinding, FragStandardViewModel> implements OnClickListener {

    public static final String TAG = FragDeferredAuths.class.getSimpleName();
    private FragStandardViewModel fragStandardViewModel;
    ArrayList<DeferredTransaction> deferredList;
    private DeferredAuthsAdapter deferredAuthsAdapter;

    public static FragDeferredAuths newInstance() {
        Bundle args = new Bundle();
        FragDeferredAuths fragment = new FragDeferredAuths();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public int getBindingVariable() {
        return com.linkly.payment.BR.fragStandardViewModel;
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_deferred_auths;
    }

    @Override
    public FragStandardViewModel getViewModel() {
        fragStandardViewModel = ViewModelProviders.of(this).get(FragStandardViewModel.class);
        fragStandardViewModel.init(ACT_DEFERRED_AUTHS);
        return fragStandardViewModel;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);
        SetHeader(false, false);


        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // find all records with msg status 'DEFERRED_AUTH'
        List<TransRec> transList = TransRecManager.getInstance().getTransRecDao().findAllDeferredAuths();
        deferredList = new ArrayList<>();
        //Initialise Cancel and print buttons
        Button btnPrint = view.findViewById(R.id.btn_print);

        if (transList != null && transList.size() > 0) {
            for (TransRec tran : transList) {
                DeferredTransaction deferredTrans = new DeferredTransaction();

                if (tran.getProtocol().getMessageStatus() == DEFERRED_AUTH) {
                    deferredTrans.status = "Pending";
                } else if ( tran.isApproved() ){
                    deferredTrans.status = "Approved";
                } else if (tran.isDeclined()) {
                    deferredTrans.status = "Declined";
                } else {
                    deferredTrans.status = "Unknown";
                }
                
                deferredTrans.transType = tran.getTransType().getDisplayName();
                deferredTrans.amount = tran.getAmounts().getTotalAmount();
                deferredTrans.receiptNo = tran.getAudit().getReceiptNumber();
                deferredTrans.maskedpan = tran.getCard().getMaskedPan();
                deferredList.add(deferredTrans);

            }
            RecyclerView deferredAuths = view.findViewById(R.id.recycler_deferred_auths);
            deferredAuths.setLayoutManager(new LinearLayoutManager(getActivity()));
            DividerItemDecoration divider = new
                    DividerItemDecoration(getActivity(),
                    DividerItemDecoration.VERTICAL);
            divider.setDrawable(ContextCompat.
                    getDrawable(getActivity().getBaseContext(), R.drawable.line_divider)
            );
            deferredAuths.addItemDecoration(divider);
            deferredAuthsAdapter = new DeferredAuthsAdapter(getBaseActivity(), deferredList);
            deferredAuths.setAdapter(deferredAuthsAdapter);

            //Initialise Cancel and print buttons
            btnPrint.setVisibility(View.VISIBLE);
            btnPrint.setOnClickListener(this);


        } else {
            //textView_Response
            TextView txtResponse = view.findViewById(R.id.textView_Response);
            txtResponse.setText(Engine.getDep().getPrompt(STR_DEFERRED_AUTH));
            btnPrint.setVisibility(View.GONE);
        }

        Button btnCancel = view.findViewById(R.id.btn_cancel);
        UIUtilities.borderTransparentButton(getActivity(),btnCancel);
        btnCancel.setOnClickListener(this);


    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.btn_cancel:
                getBaseActivity().finishAfterTransition();
                break;

            case R.id.btn_print:
                // Commented but not removed due to possibility of wanting this back. Keeping logic as this maybe uncommented. If this is not required. can delete entire fragmenet.
/*                IDependency d = Engine.getDep();
                if (deferredList != null) {
                    IReceipt receiptGenerator = new NMIDeferredAuthsReceipt();
                    PrintReceipt receiptToPrint = receiptGenerator.generateReceipt(deferredList);
                    IPrintManager.PrintPreference printPreference = MalFactory.getInstance().getHardware().hasPrinter() ? PRINT_PREFERENCE_DEFAULT : PRINT_PREFERENCE_SCREEN;
                    Engine.getPrintManager().printReceipt(d, receiptToPrint, null, true, STR_EMPTY, printPreference, MalFactory.getInstance());

                }*/
                break;
            default:
                break;
        }
    }

    public class DeferredTransaction {
        String transType;
        long amount;
        int receiptNo;
        String status;
        String maskedpan;

        public String getTransType() {
            return transType;
        }

        public long getAmount() {
            return amount;
        }

        public int getReceiptNo() {
            return receiptNo;
        }

        public String getStatus() {
            return status;
        }

        public String getMaskedpan() {
            return maskedpan;
        }


    }
}


