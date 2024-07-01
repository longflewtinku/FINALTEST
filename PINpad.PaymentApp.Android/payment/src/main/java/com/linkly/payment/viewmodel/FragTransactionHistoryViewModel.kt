package com.linkly.payment.viewmodel

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.paging.PagingData
import androidx.paging.filter
import androidx.paging.map
import com.linkly.libengine.engine.Engine
import com.linkly.libengine.engine.transactions.TransRec
import com.linkly.libengine.engine.transactions.TransRecManager
import com.linkly.libmal.global.util.Util
import com.linkly.libui.IUICurrency
import com.linkly.libui.IUIDisplay.String_id
import com.linkly.payment.pagingsources.TransHistoryRepository
import com.linkly.payment.viewmodel.data.UIFragData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Locale


class FragTransactionHistoryViewModel(application: Application) : BaseViewModel(application) {

    // Alright still have to use a bit of a "hacky way to inject this due to other major architectural issues.
    // Once we fix how Viewmodels work things should be a lot better.
    private val transHistoryRepository : TransHistoryRepository = TransHistoryRepository(TransRecManager.getInstance().transRecDao)

    override fun updateViewModel(fragData: MutableLiveData<UIFragData>?) {
        this.fragData = fragData
    }

    fun getTransList(): Flow<PagingData<TransactionInfo>> {
        return transHistoryRepository.getTransRec().map { pagingData ->
            pagingData.filter { !it.transType.adminTransaction }
                .map { populateTransactionInfoObj(it) }
        }
    }

    private fun populateTransactionInfoObj(tran: TransRec): TransactionInfo {
        val transactionInfo = TransactionInfo()
        transactionInfo.status = if (tran.isApproved) { "Approved"} else { "Declined" }

        // product don't want receipt text and response code to be visible
        transactionInfo.isTransResultTextVisible = false
        transactionInfo.type = tran.transType.displayName
        if (tran.amounts.getTotalAmount() > 0) {
            transactionInfo.amount = Engine.getDep().getFramework().getCurrency().formatAmount(
                String.format(Locale.getDefault(), "%d", tran.amounts.getTotalAmount()),
                IUICurrency.EAmountFormat.FMT_AMT_SHOW_SYMBOL,
                Engine.getDep().getPayCfg().getCountryCode()
            )
        } else {
            transactionInfo.isAmountVisible = false
        }
        transactionInfo.uid = tran.getUid()
        if (tran.card != null) {
            if (!Util.isNullOrEmpty(tran.card.maskedPan)) {
                transactionInfo.pan = tran.card.maskedPan
                transactionInfo.cardName = tran.card.getCardName(Engine.getDep().getPayCfg())
            } else {
                transactionInfo.isCardDataVisible = false
            }
        }

        // e.g. "2 Jun 2022 2:45 PM"
        transactionInfo.date = tran.audit.getTransDateTimeAsString("dd/MM/yy HH:mm")

        // product team don't want rrn to be visible
        transactionInfo.isRrnVisible = false

        return transactionInfo
    }


    class TransactionInfo {
        var type: String? = null
        var status: String? = null
        var amount: String? = null
        var isAmountVisible = true
        var pan: String? = null
        var date: String? = null
        var rrn: String? = null
        var isRrnVisible = true
        var cardName: String? = null
        var isCardDataVisible = true
        var transResultText: String? = null
        var isTransResultTextVisible = true
        var buttonText: String = Engine.getDep().getPrompt(String_id.STR_REPRINT)
        var isButtonVisible = true
        var uid = 0
    }

}