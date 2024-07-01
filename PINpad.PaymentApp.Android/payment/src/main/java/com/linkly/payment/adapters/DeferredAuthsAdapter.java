package com.linkly.payment.adapters;

import static com.linkly.libengine.engine.transactions.properties.TProtocol.MessageStatus.DEFERRED_AUTH;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.linkly.libengine.action.MenuOperations.admin.SubmitTransactions;
import com.linkly.libengine.engine.Engine;
import com.linkly.libengine.engine.transactions.TransRec;
import com.linkly.libengine.engine.transactions.TransRecManager;
import com.linkly.libengine.workflow.WorkflowAddActions;
import com.linkly.libengine.workflow.WorkflowScheduler;
import com.linkly.libui.IUICurrency;
import com.linkly.payment.R;
import com.linkly.payment.fragments.FragDeferredAuths.DeferredTransaction;

import java.util.ArrayList;

public class DeferredAuthsAdapter extends RecyclerView.Adapter<DeferredAuthsAdapter.MyViewHolder>  {
    private ArrayList<DeferredTransaction> mTransList;
    private LayoutInflater mInflater;

    // data is passed into the constructor
    public DeferredAuthsAdapter(Context context, ArrayList<DeferredTransaction> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mTransList = data;
    }


    @Override
    public DeferredAuthsAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.recycl_deferred_auths_row, parent, false);
        return new DeferredAuthsAdapter.MyViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(DeferredAuthsAdapter.MyViewHolder holder, int position) {
        DeferredTransaction transType = mTransList.get(position);
        holder.textTransName.setText(transType.getTransType());
        String totalAmount = String.format("%d", transType.getAmount());
        String amt = Engine.getDep().getFramework().getCurrency().formatAmount(totalAmount, IUICurrency.EAmountFormat.FMT_AMT_SHOW_SYMBOL, Engine.getDep().getPayCfg().getCountryCode());
        holder.textTransAmount.setText(amt);
        holder.textTransReceiptNo.setText("Receipt No: "+transType.getReceiptNo());
        holder.textTransStatus.setText(""+transType.getStatus());
        //Set the right image here
        if(transType.getStatus().equals("Approved")) {
            holder.imageTransStatusImage.setImageResource(R.mipmap.matchfull);
            holder.btnResubmitDeclined.setVisibility(View.GONE);
        }else if(transType.getStatus().equals("Declined")){
            holder.imageTransStatusImage.setImageResource(R.mipmap.matchfail);
            holder.btnResubmitDeclined.setVisibility(View.VISIBLE);
        }else{
            holder.imageTransStatusImage.setImageResource(R.mipmap.matchnotchecked);
            holder.btnResubmitDeclined.setVisibility(View.GONE);
        }
    }

    // total number of rows
    @Override
    public int getItemCount() {
        if (mTransList == null)
            return 0;
        return mTransList.size();
    }



    // stores and recycles views as they are scrolled off screen
    public class MyViewHolder extends RecyclerView.ViewHolder implements OnClickListener /*implements View.OnClickListener*/ {
        TextView textTransName;
        TextView textTransAmount;
        TextView  textTransReceiptNo;
        TextView textTransStatus;
        ImageView imageTransStatusImage;
        Button btnResubmitDeclined;


        MyViewHolder(View itemView) {
            super(itemView);
            textTransName = itemView.findViewById(R.id.row_trans_type);
            textTransAmount = itemView.findViewById(R.id.row_trans_amount);
            textTransReceiptNo = itemView.findViewById(R.id.row_trans_receipt_no);
            textTransStatus = itemView.findViewById(R.id.row_trans_status);
            imageTransStatusImage = itemView.findViewById(R.id.row_trans_status_image);
            btnResubmitDeclined = itemView.findViewById(R.id.row_btn_resubmit_declined);
            btnResubmitDeclined.setOnClickListener(this);
            itemView.setOnClickListener(this);
        }

        @SuppressWarnings("deprecation")
        @Override
        public void onClick(View v) {
            if(v.getId() == R.id.row_btn_resubmit_declined) {
                TransRec transToResubmit = TransRecManager.getInstance().getTransRecDao().getByReceiptNumber(mTransList.get(getAdapterPosition()).getReceiptNo());
                transToResubmit.updateMessageStatus(DEFERRED_AUTH);
                transToResubmit.save();
                WorkflowScheduler.getInstance().queueWorkflow(new WorkflowAddActions(new SubmitTransactions(false)), false, true);
                ((Activity)v.getContext()).finishAfterTransition();
            }
        }

    }
}