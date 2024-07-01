package com.linkly.payment.adapters;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.paging.PagingDataAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.linkly.payment.BR;
import com.linkly.payment.R;
import com.linkly.payment.databinding.RecyclerTransItemBinding;
import com.linkly.payment.viewmodel.FragTransactionHistoryViewModel;

public class TransactionItemAdapter extends PagingDataAdapter<FragTransactionHistoryViewModel.TransactionInfo,TransactionItemAdapter.ViewHolder> implements TransItemButtonClickListener, ItemTouchListener {
    private final ClickResult clickResult;
    private final UserActivity userActivity;

    public TransactionItemAdapter(@NonNull androidx.recyclerview.widget.DiffUtil.ItemCallback<FragTransactionHistoryViewModel.TransactionInfo> diffCallback, ClickResult clickResult, UserActivity userActivity) {
        super(diffCallback);
        this.clickResult = clickResult;
        this.userActivity = userActivity;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerTransItemBinding binding = DataBindingUtil.inflate(
                LayoutInflater.from(parent.getContext()),
                R.layout.recycler_trans_item, parent, false);
        return new ViewHolder(binding);
    }

    // binds the data to the TextView in each row
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        FragTransactionHistoryViewModel.TransactionInfo transaction = getItem(position);
        holder.bind(transaction);
        holder.binding.setButtonClickListener(this);
        holder.itemView.setOnTouchListener((v, event) -> {
            itemTouch();
            return false;
        });
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final RecyclerTransItemBinding binding;

        ViewHolder(RecyclerTransItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Object obj) {
            binding.setVariable(BR.model, obj);
            binding.executePendingBindings();
        }
    }

    public void itemButtonClicked(FragTransactionHistoryViewModel.TransactionInfo t) {
        clickResult.respond(t.getUid());
    }

    public interface ClickResult {
        void respond(int uid);
    }

    @Override
    public void itemTouch() {
        userActivity.resetTimer();
    }

    public interface UserActivity {
        void resetTimer();
    }

}