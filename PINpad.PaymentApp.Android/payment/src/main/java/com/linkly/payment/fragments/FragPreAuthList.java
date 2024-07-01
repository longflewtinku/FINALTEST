package com.linkly.payment.fragments;

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
import com.linkly.payment.adapters.PreAuthListAdapter;
import com.linkly.payment.databinding.ActivityTransBinding;
import com.linkly.payment.viewmodel.FragPreAuthListViewModel;

public class FragPreAuthList extends BaseFragment<ActivityTransBinding, FragPreAuthListViewModel> implements OnClickListener {

    public static final String TAG = FragPreAuthList.class.getSimpleName();
    private FragPreAuthListViewModel fragPreAuthListViewModel;
    PreAuthListAdapter preAuthListAdapter;
    RecyclerView recyclerView;


    public static FragPreAuthList newInstance() {
        Bundle args = new Bundle();
        FragPreAuthList fragment = new FragPreAuthList();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public int getBindingVariable() {
        return com.linkly.payment.BR.fragPreAuthListViewModel;
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_pre_auth_list;
    }

    @Override
    public FragPreAuthListViewModel getViewModel() {
        fragPreAuthListViewModel = ViewModelProviders.of(this).get(FragPreAuthListViewModel.class);
        fragPreAuthListViewModel.init(IUIDisplay.ACTIVITY_ID.ACT_PRE_AUTH_SEARCH_AND_VIEW);
        return fragPreAuthListViewModel;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);
        SetHeader(false, false);

        fragPreAuthListViewModel.getPreAuthTransactions().observe(getViewLifecycleOwner(), updatedData -> {
            if(!updatedData.isEmpty()) {
                TextView noTransactions = v.findViewById(R.id.textView_NoTransactions);
                noTransactions.setVisibility(View.GONE);

                recyclerView = v.findViewById(R.id.recycler_pre_auth_list);
                recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
                if (getActivity() != null && getActivity().getBaseContext() !=null ) {
                    DividerItemDecoration divider = new
                            DividerItemDecoration(getActivity(),
                            DividerItemDecoration.VERTICAL);
                    divider.setDrawable(ContextCompat.
                            getDrawable(getActivity().getBaseContext(), R.drawable.line_divider)
                    );
                    recyclerView.addItemDecoration(divider);
                }

                preAuthListAdapter = new PreAuthListAdapter(updatedData, preAuthViewClickResult, preAuthViewUserActivity);
                recyclerView.setAdapter(preAuthListAdapter);
            }
        });

        return v;
    }

    @Override
    public void onDestroyView() {
        if(recyclerView != null) {
            recyclerView.setAdapter(null);
        }

        preAuthListAdapter = null;
        recyclerView = null;

        super.onDestroyView();

    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button btnDone = view.findViewById(R.id.btn_done);
        GradientDrawable btnDrawable = (GradientDrawable) btnDone.getBackground().getConstantState().newDrawable().mutate();
        btnDrawable.setColor(Engine.getDep().getPayCfg().getBrandDisplayButtonColourOrDefault(getActivity().getColor(R.color.color_linkly_primary)));
        btnDone.setTextColor(Engine.getDep().getPayCfg().getBrandDisplayButtonTextColourOrDefault());
        btnDone.setBackground(btnDrawable);
        btnDone.setOnClickListener(this);
    }

    PreAuthListAdapter.PreAuthViewClickResult preAuthViewClickResult = uid -> sendResponse(IUIDisplay.UIResultCode.OK, Integer.toString(uid), "");

    PreAuthListAdapter.PreAuthViewUserActivity preAuthViewUserActivity = this::resetFragmentTimeout;

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_done) {
            if (getBaseActivity() != null) {
                getBaseActivity().finishAfterTransition();
            }
        }
    }
}


