package com.linkly.payment.fragments

import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.linkly.libengine.engine.Engine
import com.linkly.libui.IUIDisplay
import com.linkly.libui.IUIDisplay.ACTIVITY_ID
import com.linkly.payment.BR
import com.linkly.payment.R
import com.linkly.payment.activities.ActScreenSaver
import com.linkly.payment.adapters.TransactionItemAdapter
import com.linkly.payment.adapters.TransactionItemAdapter.ClickResult
import com.linkly.payment.adapters.TransactionItemAdapter.UserActivity
import com.linkly.payment.databinding.ActivityTransBinding
import com.linkly.payment.viewmodel.FragTransactionHistoryViewModel
import com.linkly.payment.viewmodel.FragTransactionHistoryViewModel.TransactionInfo
import kotlinx.coroutines.launch
import java.util.Objects


class FragTransactionHistory :
    BaseFragment<ActivityTransBinding?, FragTransactionHistoryViewModel?>(), View.OnClickListener {

    private lateinit var fragViewModel: FragTransactionHistoryViewModel

    private lateinit var itemAdapter: TransactionItemAdapter
    override fun onDestroyView() {
        itemAdapter.submitData(viewLifecycleOwner.lifecycle, PagingData.empty())
        super.onDestroyView()
    }

    override fun getBindingVariable(): Int {
        return BR.fragTransactionHistoryViewModel
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_transaction_history
    }

    override fun getViewModel(): FragTransactionHistoryViewModel {
        fragViewModel = ViewModelProviders.of(this).get(
            FragTransactionHistoryViewModel::class.java
        )
        fragViewModel.init(ACTIVITY_ID.ACT_TRANSACTION_HISTORY)
        return fragViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = super.onCreateView(inflater, container, savedInstanceState)
        SetHeader(false, false)
        ActScreenSaver.cancelScreenSaver()
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_trans_items)


        recyclerView.setLayoutManager(LinearLayoutManager(activity))
        if (requireActivity().baseContext != null) {
            val divider = DividerItemDecoration(
                activity,
                DividerItemDecoration.VERTICAL
            )
            divider.setDrawable(
                Objects.requireNonNull<Drawable?>(
                    ContextCompat.getDrawable(
                        requireActivity().baseContext, R.drawable.line_divider
                    )
                )
            )
            recyclerView.addItemDecoration(divider)
        }

        itemAdapter = TransactionItemAdapter(diffCallback, clickResult, userActivity);
        recyclerView.setAdapter(itemAdapter)

        // add load state listener so we can update text on screen
        itemAdapter.addLoadStateListener { loadStates: CombinedLoadStates ->
            val textViewLoading = view.findViewById<TextView>(R.id.textView_Loading)
            val textViewNoTransactions =
                view.findViewById<TextView>(R.id.textView_NoTransactions)

            when(loadStates.refresh) {
                is LoadState.Loading  -> textViewLoading.visibility = View.VISIBLE
                is LoadState.NotLoading -> {
                    // triggered after loading is complete
                    textViewLoading.visibility = View.GONE
                    // check if we have zero items
                    if (itemAdapter.getItemCount() <= 0) {
                        textViewNoTransactions.visibility = View.VISIBLE
                    }
                }
                is LoadState.Error -> textViewNoTransactions.visibility = View.VISIBLE
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            fragViewModel.getTransList().collect {
                pagingData -> itemAdapter.submitData(pagingData)
            }
        }

        val btnDone = view.findViewById<Button>(R.id.btn_done)
        btnDone.setOnClickListener(this)
        val shapeDrawable =
            btnDone.background.constantState!!.newDrawable().mutate() as GradientDrawable
        shapeDrawable.setColor(
            Engine.getDep().getPayCfg().getBrandDisplayButtonColourOrDefault(
                requireActivity().getColor(R.color.color_linkly_primary)
            )
        )
        btnDone.setTextColor(Engine.getDep().getPayCfg().getBrandDisplayButtonTextColourOrDefault())
        btnDone.background = shapeDrawable
    }

    private var clickResult = ClickResult { uid: Int ->
        sendResponse(
            IUIDisplay.UIResultCode.OK,
            uid.toString(),
            ""
        )
    }
    private var userActivity = UserActivity { resetFragmentTimeout() }
    override fun onClick(v: View) {
        if (v.id == R.id.btn_done && baseActivity != null) {
            baseActivity.finishAfterTransition()
        }
    }

    companion object {
        val TAG = FragTransactionHistory::class.java.getSimpleName()
        fun newInstance(): FragTransactionHistory {
            val args = Bundle()
            val fragment = FragTransactionHistory()
            fragment.setArguments(args)
            return fragment
        }

        private val diffCallback: DiffUtil.ItemCallback<TransactionInfo> =
            object : DiffUtil.ItemCallback<TransactionInfo>() {
                override fun areItemsTheSame(
                    oldItem: TransactionInfo,
                    newItem: TransactionInfo
                ): Boolean {
                    return oldItem.uid == newItem.uid
                }

                override fun areContentsTheSame(
                    oldItem: TransactionInfo,
                    newItem: TransactionInfo
                ): Boolean {
                    // this is good enough comparison for our purposes as uid is unique
                    return oldItem.uid == newItem.uid
                }
            }

    }
}

