package com.linkly.payment.workflows.till;

import com.linkly.libengine.engine.EngineManager;
import com.linkly.libengine.workflow.IWorkflowFactory;
import com.linkly.libengine.workflow.NotSupportedAutoTxn;
import com.linkly.libengine.workflow.Workflow;
import com.linkly.libengine.workflow.WorkflowAddActions;
import com.linkly.payment.workflows.generic.GenericWorkflowFactory;

import timber.log.Timber;

//for the moment this is the same as the GenericWorkflowFactory
public class TillWorkflowFactory extends GenericWorkflowFactory implements IWorkflowFactory {

    @Override
    public Workflow getWorkflow(EngineManager.TransType tType) {
        if (null != tType) {
            Timber.i("getTaskList %s", tType.toString());

            switch (tType) {
                case RECONCILIATION:
                case AUTOSETTLEMENT:
                case PRE_RECONCILIATION:
                case LAST_RECONCILIATION:
                    return new TillReconciliation();
                case RECONCILIATION_AUTO:
                case PRE_RECONCILIATION_AUTO:
                case LAST_RECONCILIATION_AUTO:
                case SUMMARY_AUTO:
                    return new TillAutoReconciliation();
                case MANUAL_REVERSAL_AUTO:
                case PREAUTH_AUTO:
                case PREAUTH_MOTO_AUTO:
                case PREAUTH_CANCEL_AUTO:
                case COMPLETION_AUTO:
                    return new WorkflowAddActions(new NotSupportedAutoTxn());
                default:
                    break;
            }
        }
        return super.getWorkflow(tType);
    }
}
