package com.linkly.payment.adapters;

import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.linkly.payment.BR;
import com.linkly.payment.R;
import com.linkly.payment.databinding.RecyclerPreAuthRowBinding;
import com.linkly.payment.viewmodel.FragPreAuthListViewModel;

import java.util.List;

public class PreAuthListAdapter extends RecyclerView.Adapter<PreAuthListAdapter.ViewHolder> implements PreAuthItemButtonClickListener, ItemTouchListener {
    private final List<FragPreAuthListViewModel.PreAuthTransaction> preAuthTransactions;
    private final PreAuthViewClickResult preAuthViewClickResult;
    private final PreAuthViewUserActivity preAuthViewUserActivity;

    public PreAuthListAdapter(List<FragPreAuthListViewModel.PreAuthTransaction> data, PreAuthViewClickResult preAuthViewClickResult, PreAuthViewUserActivity preAuthViewUserActivity) {
        preAuthTransactions = data;
        this.preAuthViewClickResult = preAuthViewClickResult;
        this.preAuthViewUserActivity = preAuthViewUserActivity;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerPreAuthRowBinding binding = DataBindingUtil.inflate(
                LayoutInflater.from(parent.getContext()),
                R.layout.recycler_pre_auth_row, parent, false);
        return new ViewHolder(binding);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        FragPreAuthListViewModel.PreAuthTransaction transaction = preAuthTransactions.get(position);
        holder.bind(transaction);
        holder.binding.setButtonClickListener(this);

        holder.itemView.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                itemTouch();
                return false;
            }
        });

    }

    // total number of rows
    @Override
    public int getItemCount() {
        if (preAuthTransactions == null)
            return 0;
        return preAuthTransactions.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final RecyclerPreAuthRowBinding binding;

        ViewHolder(RecyclerPreAuthRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Object obj) {
            binding.setVariable(BR.model, obj);
            binding.executePendingBindings();
        }
    }

    @Override
    public void preAuthItemButtonClicked(FragPreAuthListViewModel.PreAuthTransaction t) {
        preAuthViewClickResult.respond(t.getUid());
    }

    public interface PreAuthViewClickResult {
        void respond(int uid);
    }

    @Override
    public void itemTouch() {
        preAuthViewUserActivity.resetTimer();
    }

    public interface PreAuthViewUserActivity {
        void resetTimer();
    }
}