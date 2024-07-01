package com.linkly.payment.workflows;

import static com.linkly.libengine.engine.EngineManager.TransType.AUTOSETTLEMENT;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.HostResult.BATCH_UPLOAD_FAILED;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.HostResult.CONNECT_FAILED;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.HostResult.NO_RESPONSE;
import static com.linkly.libengine.status.IStatus.STATUS_EVENT.STATUS_NO_RESPONSE_PLEASE_TRY_AGAIN;
import static com.linkly.libui.IUIDisplay.FIVE_SEC_TIMEOUT;
import static com.linkly.libui.IUIDisplay.String_id.STR_NO_RESPONSE_PLEASE_TRY_AGAIN;
import static com.linkly.libui.IUIDisplay.String_id.STR_SETTLEMENT;
import static com.linkly.libui.IUIDisplay.UIResultCode.POS_YES;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import com.linkly.libengine.action.HostActions.Authorise;
import com.linkly.libengine.action.IAction;
import com.linkly.libengine.action.Printing.PrintFirst;
import com.linkly.libengine.action.TransactionApproval;
import com.linkly.libengine.action.TransactionDecliner;
import com.linkly.libengine.engine.protocol.IProto;
import com.linkly.libengine.engine.reporting.DailyBatch;
import com.linkly.libengine.engine.reporting.IDailyBatch;
import com.linkly.libengine.engine.reporting.Reconciliation;
import com.linkly.libengine.engine.transactions.TransRec;
import com.linkly.libengine.helpers.UIHelpers;
import com.linkly.libengine.status.IStatus;
import com.linkly.libui.IUIDisplay;
import com.linkly.libui.UIScreenDef;
import com.linkly.libui.display.DisplayQuestion;
import com.linkly.payment.workflows.generic.ActRecOutOfBalance;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Objects;

import timber.log.Timber;

public class ActBaseCheckReconciliationResult extends IAction {
    protected void printReconciliation(TransRec trans) {
        if (trans.getTransEvent() != null && trans.getTransEvent().isPosPrintingSync()) {
            PrintFirst.buildAndBroadcastReceipt(d, trans, PrintFirst.ReceiptType.SETTLEMENT, false, context, mal);

            if (trans.isPrintOnTerminal()) {
                trans.print(d, true, false, mal);
            }
        } else {
            trans.print(d, true, false, mal);
        }
    }

    @Override
    public String getName() {
        return "ActBaseCheckReconciliationResult";
    }

    @SuppressWarnings("java:S3776") // cognitive complexity(23)
    @Override
    public void run() {

        if ((trans.getProtocol().getHostResult() == NO_RESPONSE || trans.getProtocol().getHostResult() == CONNECT_FAILED) || trans.getProtocol().getHostResult() == BATCH_UPLOAD_FAILED) {
            if (trans.getAutoSettlementRetryCount() > 0) {
                // Automatic Settlement, retry is required
                Timber.i("AutoSettlement, retrying");
                int retryCount = trans.getAutoSettlementRetryCount() - 1;
                trans.setAutoSettlementRetryCount(retryCount);
                d.getWorkflowEngine().setNextAction(Authorise.class);
            } else {
                if (retryReconciliation()) {
                    d.getWorkflowEngine().setNextAction(Authorise.class);
                } else if (trans.getProtocol().getHostResult() == BATCH_UPLOAD_FAILED) {
                    d.getStatusReporter().reportStatusEvent(IStatus.STATUS_EVENT.STATUS_ERR_TRANSACTION_DECLINED, trans.isSuppressPosDialog());
                    d.getWorkflowEngine().setNextAction(TransactionDecliner.class);
                } else {
                    d.getStatusReporter().reportStatusEvent(IStatus.STATUS_EVENT.STATUS_ERR_TRANSACTION_DECLINED, trans.isSuppressPosDialog());
                    trans.getAudit().setRejectReasonType(IProto.RejectReasonType.COMMS_ERROR);
                    d.getWorkflowEngine().setNextAction(TransactionDecliner.class);
                }
            }
            return;
        }
        // are we cutting over?
        switch (trans.getProtocol().getHostResult()) {
            case RECONCILED_IN_BALANCE:
            case RECONCILED_OUT_OF_BALANCE:
                // do not update record if pre-settlement or last-settlement
                if (trans.isReconciliation()) {
                    // update settlement record, mark trans records as reconciled
                    IDailyBatch dailyBatch = new DailyBatch();
                    Reconciliation recData = dailyBatch.generateDailyBatch(true, d);
                    // if reconciliation already has a db record (UID) in rec database, set the handle. This prevents duplicate records in rec database
                    if (trans.getReconciliation().getUid() > 0) {
                        recData.setUid(trans.getReconciliation().getUid());
                    }
                    trans.setReconciliation(recData);
                }
                break;

            case AUTHORISED:
                // pre-settlement
                if (trans.isPreReconciliation()) {
                    // update settlement record, mark trans records as reconciled
                    IDailyBatch dailyBatch = new DailyBatch();
                    Reconciliation recData = dailyBatch.generateDailyBatch(false, d);
                    // if reconciliation already has a db record (UID) in rec database, set the handle. This prevents duplicate records in rec database
                    if (trans.getReconciliation().getUid() > 0) {
                        recData.setUid(trans.getReconciliation().getUid());
                    }
                    trans.setReconciliation(recData);
                }
                break;
            default:
                // don't update trans records
                break;
        }

        if (trans.isPrintTransactionListing()) {
            printReconciliation(trans);
        }

        switch (trans.getProtocol().getHostResult()) {
            case RECONCILED_IN_BALANCE:
            case AUTHORISED:
                saveSettlementCompletedDatestamp(context);
                d.getStatusReporter().reportStatusEvent(IStatus.STATUS_EVENT.STATUS_HOST_APPROVED, trans.isSuppressPosDialog());
                d.getWorkflowEngine().setNextAction(TransactionApproval.class);
                break;

            case RECONCILED_OUT_OF_BALANCE:
                saveSettlementCompletedDatestamp(context);
                d.getStatusReporter().reportStatusEvent(IStatus.STATUS_EVENT.STATUS_ERR_RECONCILIATION_OUT_OF_BALANCE, trans.isSuppressPosDialog());
                trans.getAudit().setRejectReasonType(IProto.RejectReasonType.DECLINED);
                d.getWorkflowEngine().setNextAction(ActRecOutOfBalance.class);
                break;

            case RECONCILE_FAILED_TERMINAL_ALREADY_SETTLED:
                saveSettlementCompletedDatestamp(context);
                d.getStatusReporter().reportStatusEvent(IStatus.STATUS_EVENT.STATUS_ERR_RECONCILIATION_TERMINAL_ALREADY_SETTLED, trans.isSuppressPosDialog());
                trans.getAudit().setRejectReasonType(IProto.RejectReasonType.DECLINED);
                d.getWorkflowEngine().setNextAction(TransactionDecliner.class);
                break;

            case RECONCILE_FAILED_OUTSIDE_WINDOW:
                d.getStatusReporter().reportStatusEvent(IStatus.STATUS_EVENT.STATUS_ERR_RECONCILIATION_OUTSIDE_WINDOW, trans.isSuppressPosDialog());
                trans.getAudit().setRejectReasonType(IProto.RejectReasonType.DECLINED);
                d.getWorkflowEngine().setNextAction(TransactionDecliner.class);
                break;

            default:
                // to prevent from "Waiting for response" for the host result values that are not handled yet -> treat as declined
                d.getStatusReporter().reportStatusEvent(IStatus.STATUS_EVENT.STATUS_ERR_TRANSACTION_DECLINED, trans.isSuppressPosDialog());
                trans.getAudit().setRejectReasonType(IProto.RejectReasonType.COMMS_ERROR);
                d.getWorkflowEngine().setNextAction(TransactionDecliner.class);
                break;
        }
    }

    @SuppressWarnings("java:S3776") // cognitive complexity(16)
    protected boolean retryReconciliation() {
        boolean retryDecision = false;
        if (trans.getReconciliationOriginalTransType() == AUTOSETTLEMENT) {
            // Automatic Settlement. No more retries, display info screen and go back to idle
            IUIDisplay ui = d.getUI();
            HashMap<String, Object> map = new HashMap<>();
            ArrayList<DisplayQuestion> options = new ArrayList<>();
            map.put(IUIDisplay.uiScreenTitle, d.getPrompt(STR_SETTLEMENT));

            options.add(new DisplayQuestion(d.getPrompt(IUIDisplay.String_id.STR_OK), "OP0", DisplayQuestion.EButtonStyle.BTN_STYLE_PRIMARY_DEFAULT_DOUBLE));
            map.put(IUIDisplay.uiScreenOptionList, options);
            ui.showScreen(UIScreenDef.NO_RESPONSE, map);
            if (ui.getResultCode(IUIDisplay.ACTIVITY_ID.ACT_QUESTION, FIVE_SEC_TIMEOUT) == IUIDisplay.UIResultCode.OK) {
                // The Result is not important in this situation
                ui.getResultText(IUIDisplay.ACTIVITY_ID.ACT_QUESTION, IUIDisplay.uiResultText1);
            }
        } else {
            // Manual Settlement
            if (isAutoReconciliation()) {
                // Display dialog on POS (if not suppressed by PAT mode)
                d.getStatusReporter().reportStatusEvent(STATUS_NO_RESPONSE_PLEASE_TRY_AGAIN, trans.isSuppressPosDialog());
                if (trans.isPatMode("1") || trans.isPatMode("2")) { // PAT 1 or PAT 2 - PINPAD input enabled
                    retryDecision = UIHelpers.uiYesNoQuestion(d, d.getPrompt(STR_SETTLEMENT), d.getPrompt(STR_NO_RESPONSE_PLEASE_TRY_AGAIN), FIVE_SEC_TIMEOUT);
                } else { // PAT 0 or not PAT tag: Dialog on POS only
                    IUIDisplay.UIResultCode uiResultCode = d.getUI().getResultCode(IUIDisplay.ACTIVITY_ID.ACT_INFORMATION, FIVE_SEC_TIMEOUT);
                    if (POS_YES == uiResultCode) {
                        retryDecision = true;
                    }
                }
            } else {
                retryDecision = UIHelpers.uiYesNoQuestion(d, d.getPrompt(STR_SETTLEMENT), d.getPrompt(STR_NO_RESPONSE_PLEASE_TRY_AGAIN), FIVE_SEC_TIMEOUT);
            }
        }
        return retryDecision;
    }

    private boolean isAutoReconciliation() {
        return super.trans.getTransType().autoTransaction || (super.trans.getReconciliationOriginalTransType() != null && super.trans.getReconciliationOriginalTransType().autoTransaction);
    }

    // Save timestamp of settlement performed
    protected void saveSettlementCompletedDatestamp(Context context) {
        // Update settlement timestamp only when the transaction type is reconciliation
        if (trans != null && trans.isReconciliation()) {
            Calendar calendar = Calendar.getInstance();
            SharedPreferences settlementCompleted = PreferenceManager.getDefaultSharedPreferences(Objects.requireNonNull(context));
            Timber.w("Settlement completed. Saving date/time stamp: %s", calendar.getTime().toString());
            settlementCompleted.edit().putString("lastCompletedSettlementTimestamp", calendar.getTimeInMillis() + "").apply();
        }
    }

    public static long getLastCompletedSettlementTimestamp(Context context) {
        SharedPreferences settlementCompleted = PreferenceManager.getDefaultSharedPreferences(Objects.requireNonNull(context));
        String lastCompletedSettlementTimestamp = settlementCompleted.getString("lastCompletedSettlementTimestamp", "0");
        if (lastCompletedSettlementTimestamp.equals("0"))
            return 0;
        // There is a saved datestamp
        try {
            return Long.parseLong(lastCompletedSettlementTimestamp);
        } catch (NumberFormatException ex) {
            Timber.i("Error parsing lastCompletedSettlementTimestamp");
            return 0;
        }
    }

}
