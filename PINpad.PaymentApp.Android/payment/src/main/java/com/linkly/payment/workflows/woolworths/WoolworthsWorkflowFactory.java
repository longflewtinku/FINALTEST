package com.linkly.payment.workflows.woolworths;

import com.linkly.libengine.engine.EngineManager;
import com.linkly.libengine.workflow.IWorkflowFactory;
import com.linkly.libengine.workflow.NotSupportedAutoReport;
import com.linkly.libengine.workflow.Workflow;
import com.linkly.libengine.workflow.WorkflowAddActions;
import com.linkly.payment.workflows.generic.GenericWorkflowFactory;
import com.linkly.payment.workflows.generic.Reconciliation;

import timber.log.Timber;

//for the moment this is the same as the GenericWorkflowFactory
public class WoolworthsWorkflowFactory extends GenericWorkflowFactory implements IWorkflowFactory {
    public Workflow getWorkflow(EngineManager.TransType tType) {
        if( null != tType ) {
            Timber.i("getTaskList %s", tType.toString());

            switch (tType) {
                case RECONCILIATION, AUTOSETTLEMENT:
                    return new Reconciliation();
                case RECONCILIATION_AUTO, PRE_RECONCILIATION, SUMMARY_AUTO, LAST_RECONCILIATION_AUTO:
                    // Reconciliation only supported in standalone for now. will support later when required.
                    // Send back to POS not supported/invalid report type.
                    return new WorkflowAddActions(new NotSupportedAutoReport());
                default:
                    break;
            }
        }

        return super.getWorkflow(tType);
    }
}
