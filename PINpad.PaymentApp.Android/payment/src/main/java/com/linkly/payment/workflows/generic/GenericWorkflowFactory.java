package com.linkly.payment.workflows.generic;

import androidx.annotation.Nullable;

import com.linkly.libengine.action.MenuOperations.admin.SubmitTransactions;
import com.linkly.libengine.action.MenuOperations.users.UserLogin;
import com.linkly.libengine.engine.EngineManager;
import com.linkly.libengine.workflow.AutoCash;
import com.linkly.libengine.workflow.AutoCompletion;
import com.linkly.libengine.workflow.AutoLastReconciliation;
import com.linkly.libengine.workflow.AutoLogon;
import com.linkly.libengine.workflow.AutoPreAuth;
import com.linkly.libengine.workflow.AutoPreAuthMOTO;
import com.linkly.libengine.workflow.AutoPreauthCancellation;
import com.linkly.libengine.workflow.AutoReconciliation;
import com.linkly.libengine.workflow.AutoRefund;
import com.linkly.libengine.workflow.AutoRefundMOTO;
import com.linkly.libengine.workflow.AutoReversal;
import com.linkly.libengine.workflow.AutoSale;
import com.linkly.libengine.workflow.AutoSaleMOTO;
import com.linkly.libengine.workflow.AutoShiftTotals;
import com.linkly.libengine.workflow.AutoSubTotals;
import com.linkly.libengine.workflow.CNPRefund;
import com.linkly.libengine.workflow.CNPSale;
import com.linkly.libengine.workflow.Cash;
import com.linkly.libengine.workflow.Completion;
import com.linkly.libengine.workflow.IWorkflowFactory;
import com.linkly.libengine.workflow.Logon;
import com.linkly.libengine.workflow.NotImplemented;
import com.linkly.libengine.workflow.PreAuth;
import com.linkly.libengine.workflow.PreAuthMOTO;
import com.linkly.libengine.workflow.PreauthCancellation;
import com.linkly.libengine.workflow.PurchasePlusCashback;
import com.linkly.libengine.workflow.Reconciliation;
import com.linkly.libengine.workflow.Refund;
import com.linkly.libengine.workflow.RefundMOTO;
import com.linkly.libengine.workflow.ReprintShiftTotals;
import com.linkly.libengine.workflow.Reversal;
import com.linkly.libengine.workflow.RsaLogon;
import com.linkly.libengine.workflow.Sale;
import com.linkly.libengine.workflow.SaleMOTO;
import com.linkly.libengine.workflow.ShiftTotals;
import com.linkly.libengine.workflow.ShiftTotalsAutomatic;
import com.linkly.libengine.workflow.SubTotals;
import com.linkly.libengine.workflow.TestConnect;
import com.linkly.libengine.workflow.TopUp;
import com.linkly.libengine.workflow.Workflow;
import com.linkly.libengine.workflow.WorkflowAddActions;

import timber.log.Timber;

public class GenericWorkflowFactory implements IWorkflowFactory {
    @Override
    public Workflow getWorkflow(@Nullable EngineManager.TransType tType) {
        if (tType != null) {
            Timber.i("getTaskList %s", tType.toString());
            switch (tType) {
                case SALE:
                    return new Sale();
                case CASHBACK:
                    return new PurchasePlusCashback();
                case CARD_NOT_PRESENT:
                    return new CNPSale();
                case SALE_AUTO:
                case CASHBACK_AUTO:
                    return new AutoSale();
                case SALE_MOTO:
                    return new SaleMOTO();
                case SALE_MOTO_AUTO:
                    return new AutoSaleMOTO();
                case REFUND_AUTO:
                    return new AutoRefund();
                case REFUND:
                    return new Refund();
                case REFUND_MOTO:
                    return new RefundMOTO();
                case REFUND_MOTO_AUTO:
                    return new AutoRefundMOTO();
                case CARD_NOT_PRESENT_REFUND:
                    return new CNPRefund();
                case COMPLETION:
                    return new Completion();
                case COMPLETION_AUTO:
                    return new AutoCompletion();
                case PREAUTH:
                    return new PreAuth();
                case PREAUTH_CANCEL:
                    return new PreauthCancellation();
                case PREAUTH_AUTO:
                    return new AutoPreAuth();
                case PREAUTH_CANCEL_AUTO:
                    return new AutoPreauthCancellation();
                case PREAUTH_MOTO:
                    return new PreAuthMOTO();
                case PREAUTH_MOTO_AUTO:
                    return new AutoPreAuthMOTO();
                case TOPUPPREAUTH:
                    return new TopUp();
                case CASH:
                    return new Cash();
                case CASH_AUTO:
                    return new AutoCash();
                case MANUAL_REVERSAL:
                    return new Reversal();
                case MANUAL_REVERSAL_AUTO:
                    return new AutoReversal();
                case TESTCONNECT:
                    return new TestConnect();
                case RECONCILIATION:
                    return new Reconciliation();
                case RECONCILIATION_AUTO:
                    return new AutoReconciliation();
                case AUTO_LOGON:
                    return new AutoLogon();
                case RSA_LOGON:
                    return new RsaLogon();
                case LOGON:
                    return new Logon();
                case LAST_RECONCILIATION_AUTO:
                    return new AutoLastReconciliation();
                case SUB_TOTALS_AUTO:
                    return new AutoSubTotals();
                case SHIFT_TOTALS_AUTO:
                    return new AutoShiftTotals();
                case SUB_TOTALS:
                    return new SubTotals();
                case SHIFT_TOTALS:
                    return new ShiftTotals();
                case AUTOMATIC_SHIFT_TOTALS:
                    return new ShiftTotalsAutomatic();
                case REPRINT_SHIFT_TOTALS:
                    return new ReprintShiftTotals();
                case BALANCE:
                case DEPOSIT:
                case OFFLINESALE:
                case OFFLINECASH:
                case TESTCYCLEKEYS:
                case SUMMARY:
                case DCCRATES:
                case GRATUITY:
                case TOPUPCOMPLETION:
                default:
                    return new NotImplemented();
            }
        }
        return new NotImplemented();
    }


    public Workflow getWorkflow(String name) {
        Timber.d("getWorkflow...name: %s", name);
        EngineManager.TransType t = EngineManager.TransType.getTransTypeByString(name);
        if (t != null)
            return getWorkflow(t);

        /* for non transaction workflows */
        switch(name.toLowerCase()) {
            case "logon":
                return new WorkflowAddActions(new UserLogin());
            case "closebatch":
                return new WorkflowAddActions(new SubmitTransactions(false));
        }
        return null;
    }

}
