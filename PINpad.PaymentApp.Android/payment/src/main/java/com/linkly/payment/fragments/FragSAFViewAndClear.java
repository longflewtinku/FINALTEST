package com.linkly.payment.fragments;

import static com.linkly.libui.IUIDisplay.ACTIVITY_ID.ACT_SAF_VIEW_AND_CLEAR;

import android.graphics.drawable.GradientDrawable;
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
import com.linkly.libui.IUIDisplay;
import com.linkly.payment.R;
import com.linkly.payment.adapters.SAFViewAndClearAdapter;
import com.linkly.payment.databinding.ActivityTransBinding;
import com.linkly.payment.viewmodel.FragSAFViewAndClearViewModel;

import java.util.Objects;

public class FragSAFViewAndClear extends BaseFragment<ActivityTransBinding, FragSAFViewAndClearViewModel> implements OnClickListener {

    public static final String TAG = FragSAFViewAndClear.class.getSimpleName();
    private FragSAFViewAndClearViewModel fragSAFViewAndClearViewModel;
    RecyclerView recyclerView;
    SAFViewAndClearAdapter safViewAndClearAdapter;

    public static FragSAFViewAndClear newInstance() {
        Bundle args = new Bundle();
        FragSAFViewAndClear fragment = new FragSAFViewAndClear();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public int getBindingVariable() {
        return com.linkly.payment.BR.fragSAFViewAndClearViewModel;
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_saf_view_and_clear;
    }

    @Override
    public FragSAFViewAndClearViewModel getViewModel() {
        fragSAFViewAndClearViewModel = ViewModelProviders.of(this).get(FragSAFViewAndClearViewModel.class);
        fragSAFViewAndClearViewModel.init(ACT_SAF_VIEW_AND_CLEAR);
        return fragSAFViewAndClearViewModel;
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

        // Observe data here
        fragSAFViewAndClearViewModel.getLiveDataSafList().observe(getViewLifecycleOwner(), updatedData -> {
            if(!updatedData.isEmpty()) {
                TextView noTransactions = view.findViewById(R.id.textView_NoTransactions);
                noTransactions.setVisibility(View.GONE);

                recyclerView = view.findViewById(R.id.recycler_saf_view_and_clear);
                recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
                if (getActivity() != null && getActivity().getBaseContext() !=null ) {
                    DividerItemDecoration divider = new
                            DividerItemDecoration(getActivity(),
                            DividerItemDecoration.VERTICAL);
                    divider.setDrawable(Objects.requireNonNull(ContextCompat.
                            getDrawable(getActivity().getBaseContext(), R.drawable.line_divider))
                    );
                    recyclerView.addItemDecoration(divider);
                }

                safViewAndClearAdapter = new SAFViewAndClearAdapter(updatedData, safViewClickResult);
                recyclerView.setAdapter(safViewAndClearAdapter);
            }
        });

        Button btnDone = view.findViewById(R.id.btn_done);
        GradientDrawable btnDrawable = (GradientDrawable) btnDone.getBackground().getConstantState().newDrawable().mutate();
        btnDrawable.setColor(Engine.getDep().getPayCfg().getBrandDisplayButtonColourOrDefault(requireContext().getColor(R.color.color_linkly_primary)));
        btnDone.setTextColor(Engine.getDep().getPayCfg().getBrandDisplayButtonTextColourOrDefault());
        btnDone.setBackground(btnDrawable);
        btnDone.setOnClickListener(this);
    }

    @Override
    public void onDestroyView() {
        if(recyclerView != null) {
            recyclerView.setAdapter(null);
        }

        recyclerView = null;
        safViewAndClearAdapter = null;

        super.onDestroyView();
    }

    SAFViewAndClearAdapter.SafViewClickResult safViewClickResult = uid -> sendResponse(IUIDisplay.UIResultCode.OK, Integer.toString(uid), "");

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_done) {
            if (getBaseActivity() != null) {
                getBaseActivity().finishAfterTransition();
            }
        }
    }

}


