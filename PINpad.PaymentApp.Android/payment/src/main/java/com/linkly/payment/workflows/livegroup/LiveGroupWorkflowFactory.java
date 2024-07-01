package com.linkly.payment.workflows.livegroup;

import com.linkly.libengine.engine.EngineManager;
import com.linkly.libengine.workflow.IWorkflowFactory;
import com.linkly.libengine.workflow.Workflow;
import com.linkly.payment.workflows.generic.AutoReconciliation;
import com.linkly.payment.workflows.generic.AutoSummary;
import com.linkly.payment.workflows.generic.GenericWorkflowFactory;
import com.linkly.payment.workflows.generic.PreReconciliation;
import com.linkly.payment.workflows.generic.Reconciliation;

import timber.log.Timber;

//for the moment this is the same as the GenericWorkflowFactory
public class LiveGroupWorkflowFactory extends GenericWorkflowFactory implements IWorkflowFactory {
    @Override
    public Workflow getWorkflow(EngineManager.TransType tType) {
        if( null != tType ) {
            Timber.i("getTaskList %s", tType.toString());

            switch (tType) {

                case RECONCILIATION:
                case AUTOSETTLEMENT:
                    return new Reconciliation();
                case RECONCILIATION_AUTO:
                    return new AutoReconciliation();
                case PRE_RECONCILIATION:
                    return new PreReconciliation();
                case SUMMARY_AUTO:
                    return new AutoSummary();
                default:
                    break;
            }
        }
        return super.getWorkflow(tType);
    }
}
