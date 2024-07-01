package com.linkly.payment.workflows.suncorp;

import com.linkly.libengine.engine.EngineManager;
import com.linkly.libengine.workflow.IWorkflowFactory;
import com.linkly.libengine.workflow.Workflow;
import com.linkly.payment.workflows.generic.GenericWorkflowFactory;

import timber.log.Timber;

//for the moment this is the same as the GenericWorkflowFactory
public class SuncorpWorkflowFactory extends GenericWorkflowFactory implements IWorkflowFactory {
    public Workflow getWorkflow(EngineManager.TransType tType) {
        Timber.i("getTaskList " + tType.toString());
        switch (tType) {

            case RECONCILIATION:
                return new SuncorpReconciliation();
            case RECONCILIATION_AUTO:
                return new SuncorpAutoReconciliation();
            default:
                break;
        }
        return super.getWorkflow(tType);
    }
}
