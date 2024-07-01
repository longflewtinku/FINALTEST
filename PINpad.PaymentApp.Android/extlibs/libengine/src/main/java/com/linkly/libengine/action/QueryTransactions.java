package com.linkly.libengine.action;


import static com.linkly.libengine.engine.transactions.properties.TProtocol.MessageStatus.ADVICE_QUEUED;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.MessageStatus.REVERSAL_QUEUED;

import com.linkly.libengine.engine.transactions.TransRec;
import com.linkly.libengine.engine.transactions.TransRecManager;
import com.linkly.libengine.helpers.ECRHelpers;
import com.linkly.libengine.status.IStatus;
import com.linkly.libpositive.PosIntegrate;
import com.linkly.libpositive.events.PositiveTransEvent;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

public class QueryTransactions extends IAction {
    PositiveTransEvent event;

    public QueryTransactions(PositiveTransEvent event) {
        this.event = event;
    }

    @Override
    public String getName() {
        return "GetLastTransaction";
    }

    @Override
    public void run() {
        if (event.isGetLastTransaction()) {
            trans = TransRec.getLatestFinancialTxn();
        } else if (event.getFilter() != null) { // Local Rest Txn Listing Api with Filter Element to list transactions
            List<TransRec> transList = new ArrayList<>();
            if (event.getFilter().equalsIgnoreCase("Unsent")) { // Filter "Unsent" will list all unsent transaction which are not sent to host and approved offline
                transList.addAll(TransRecManager.getInstance().getTransRecDao().findAllByMessageStatusAndApproved(REVERSAL_QUEUED));
                transList.addAll(TransRecManager.getInstance().getTransRecDao().findAllByMessageStatusAndApproved(ADVICE_QUEUED));
            } else {  // if Filter is empty/absent, considering filter value as "All" which list all financial txn that are sent/unsent and approved
                transList.addAll(TransRecManager.getInstance().getTransRecDao().findAllApprovedTrans(TransRec.getFinancialTxnTypeList()));
            }
            Timber.d("transList : %s", transList);
            ECRHelpers.ipcSendTransListResponse(d, transList, context);
        } else {
            String uti = event.getUti();

            Timber.i("looking for UTI %s", uti);
            trans = TransRecManager.getInstance().getTransRecDao().getByUti(uti);
            if (trans != null) {
                Timber.i("found txn for UTI %s", uti);
            } else {
                Timber.e("No txn found for UTI %s", uti);
            }
        }

        if (trans != null) {
            Timber.i("trans is not NULL, is auto %b, is admin %b", trans.getTransType().autoTransaction, trans.getTransType().adminTransaction);
        } else {
            Timber.i("trans is NULL");
        }

        // if trans record found, AND was an 'auto' transaction (initiated by POS), AND not an admin (e.g. reconciliation) txn then return it
        if (trans != null && trans.getTransType().autoTransaction && !trans.getTransType().adminTransaction) {
            trans.setTransEvent(event);
            // MW: show Approved text as the Query card request is approved
            d.getStatusReporter().reportStatusEvent(IStatus.STATUS_EVENT.STATUS_TRANS_APPROVED, trans.isSuppressPosDialog());
            ECRHelpers.ipcSendTransResponse(d, trans, context);
            d.getStatusReporter().reportStatusEvent(IStatus.STATUS_EVENT.STATUS_TRANS_FINISHED, trans.isSuppressPosDialog());
        } else {
            // else return not found
            Timber.i("sending session not found response back");
            ECRHelpers.ipcSendNullTransResponse(d, event, PosIntegrate.ResultResponse.RES_SESSION_NOT_FOUND, context);
        }
    }
}
