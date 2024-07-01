package com.linkly.libengine.workflow;

import static com.linkly.libengine.engine.protocol.IProto.RejectReasonType.ABANDONED;

import android.content.Context;

import com.linkly.libengine.action.IAction;
import com.linkly.libengine.action.TransactionDecliner;
import com.linkly.libengine.action.TransactionFinalizer;
import com.linkly.libengine.dependencies.IDependency;
import com.linkly.libengine.engine.protocol.IProto;
import com.linkly.libmal.IMal;

import java.util.Iterator;
import java.util.List;

import timber.log.Timber;

@SuppressWarnings("rawtypes")
public class WorkflowEngine {

    private Class nextAction = null;

    public void setNextAction(Class result) {
        // todo think if it's a good idea to return false if the next action is not on the list,
        //      will this benefit the caller?
        this.nextAction = result;
    }

    /**
     *
     * @return true if next action was set inside of the transaction
     */
    public boolean isJumping() {
        return nextAction != null;
    }

    public void run(Workflow workflow, IDependency dependencies, IMal mal, Context context) {
        long lastTime;

        Timber.i( "Starting workflow: %s", workflow.getClass().getSimpleName());
        dependencies.setWorkflowEngine(this);
        List<IAction> actions = workflow.getActions();
        Iterator<IAction> it = actions.iterator();
        IAction action = null;

        while (it.hasNext()) {
            try {
                if (nextAction != null) {
                    Timber.i("Workflow: %s. Jumping to action: %s ", workflow.getClass().getSimpleName(), nextAction.getSimpleName());
                    // if there is a jump reiterate and change the marker(iterator) to point the new action on the list
                    Iterator<IAction> itJumpTo = actions.iterator();
                    while (itJumpTo.hasNext()) {
                        IAction nextAction = itJumpTo.next();
                        if (nextAction.getClass().equals(this.nextAction)) {
                            it = itJumpTo;
                            action = nextAction;
                            break;
                        }
                    }
                    if (action == null)
                        Timber.e("ERROR: action not found, re-running current action again: %s", nextAction.getName());
                    this.nextAction = null;
                } else {
                    action = it.next();
                    Timber.e("Workflow: " + workflow.getClass().getSimpleName() + ". Running next action: " + action.getClass().getSimpleName());
                }
                if (action == null) {
                    Timber.e("Workflow: " + workflow.getClass().getSimpleName() + ". Critical processing error, next action not set.");
                    break;
                }
                // Timing on how long it takes to process an action
                lastTime = System.currentTimeMillis(); // Get the start tick count here
                action.run(dependencies, mal, context);
                Timber.e("----------- Finished %s - Time Taken %d ---------", action.getName(), System.currentTimeMillis() - lastTime);
            } catch (Exception e) {
                Timber.w(e);
                if (dependencies.getCurrentTransaction() != null) {
                    if (dependencies.getCurrentTransaction().getAudit().getRejectReasonType() != ABANDONED) {
                        dependencies.getCurrentTransaction().getAudit().setRejectReasonType( IProto.RejectReasonType.ABANDONED);
                        this.setNextAction(TransactionDecliner.class);
                    }else {
                        this.setNextAction(TransactionFinalizer.class);
                    }
                    dependencies.getCurrentTransaction().save();
                    /* TODO save details of failure and make the idle loop handle nicely with option to reprint */
                } else {
                    return;
                }
            }
        }
    }
}
