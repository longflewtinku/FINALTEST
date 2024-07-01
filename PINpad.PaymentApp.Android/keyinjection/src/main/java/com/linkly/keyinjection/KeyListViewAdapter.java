package com.linkly.keyinjection;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.recyclerview.widget.RecyclerView;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import timber.log.Timber;

public class KeyListViewAdapter extends RecyclerView.Adapter<KeyListViewAdapter.ViewHolder> {

    private ArrayList<KeySet.KeyVal> mData;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;

    // data is passed into the constructor
    KeyListViewAdapter(Context context, ArrayList<KeySet.KeyVal> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
    }

    // inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.recyclerview_row, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        String description = mData.get(position).description;
        holder.buttonKey.setText(description);
        holder.buttonKey.setOnClickListener(view -> {
            if (mClickListener != null) {
                try {
                    mClickListener.onItemClick(view, position);
                } catch (NoSuchPaddingException | IllegalBlockSizeException |
                         NoSuchAlgorithmException | BadPaddingException | InvalidKeyException e) {
                    Timber.e(e);
                    throw new RuntimeException(e);
                }
            }
        });
    }

    // total number of rows
    @Override
    public int getItemCount() {
        if (mData == null)
            return 0;
        return mData.size();
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        Button buttonKey;

        ViewHolder(View itemView) {
            super(itemView);
            buttonKey = itemView.findViewById(R.id.buttonKey);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) {
                try {
                    mClickListener.onItemClick(view, getAdapterPosition());
                } catch (NoSuchPaddingException | InvalidKeyException | BadPaddingException |
                         NoSuchAlgorithmException | IllegalBlockSizeException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }


    // allows clicks events to be caught
    void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position) throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException;
    }
}
