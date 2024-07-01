package com.linkly.payment.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.linkly.payment.BR;
import com.linkly.payment.R;
import com.linkly.payment.databinding.RecyclerSafRowBinding;
import com.linkly.payment.viewmodel.FragSAFViewAndClearViewModel;

import java.util.List;

public class SAFViewAndClearAdapter extends RecyclerView.Adapter<SAFViewAndClearAdapter.ViewHolder> implements ItemButtonClickListener {
    private final List<FragSAFViewAndClearViewModel.SAFTransaction> safTransactions;
    private final SafViewClickResult safViewClickResult;

    public SAFViewAndClearAdapter(List<FragSAFViewAndClearViewModel.SAFTransaction> data, SafViewClickResult safViewClickResult) {
        safTransactions = data;
        this.safViewClickResult = safViewClickResult;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerSafRowBinding binding = DataBindingUtil.inflate(
                LayoutInflater.from(parent.getContext()),
                R.layout.recycler_saf_row, parent, false);
        return new ViewHolder(binding);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        FragSAFViewAndClearViewModel.SAFTransaction transaction = safTransactions.get(position);
        holder.bind(transaction);
        holder.binding.setButtonClickListener(this);
    }

    // total number of rows
    @Override
    public int getItemCount() {
        if (safTransactions == null)
            return 0;
        return safTransactions.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final RecyclerSafRowBinding binding;

        ViewHolder(RecyclerSafRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Object obj) {
            binding.setVariable(BR.model, obj);
            binding.executePendingBindings();
        }
    }

    public void itemButtonClicked(FragSAFViewAndClearViewModel.SAFTransaction t) {
        safViewClickResult.respond(t.getUid());
    }

    public interface SafViewClickResult {
        void respond(int uid);
    }

}