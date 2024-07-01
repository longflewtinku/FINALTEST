package com.linkly.libengine.action;

import com.linkly.libengine.engine.transactions.TransRec;
import com.linkly.libengine.engine.transactions.properties.TCard;

import timber.log.Timber;

public class ConstrainedAction extends IAction {

    public IAction getDependentAction() {
        return this.dependentAction;
    }

    public IConstraint getConstraint() {
        return this.constraint;
    }

    public interface IConstraint {
        public boolean check(TransRec trans);
    }

    public static class IsCaptureMethod implements IConstraint {
        TCard.CaptureMethod captureMethod = null;
        public IsCaptureMethod(TCard.CaptureMethod captureMethod) {
            this.captureMethod = captureMethod;
        }
        @Override
        public boolean check(TransRec trans) {
            return trans.getCard().getCaptureMethod() == this.captureMethod;
        }
    }

    IAction dependentAction = null;
    IConstraint constraint = null;

    private ConstrainedAction() {}

    public ConstrainedAction(IAction action, IConstraint constraint) {
        dependentAction = action;
        this.constraint = constraint;
    }

    @Override
    public String getName() {
        // Return the name of the action we are trying to perform
        return (dependentAction != null) ? dependentAction.getName() : "ConstrainedAction";
    }

    @Override
    public void run() {

        Timber.i( "Check if action " + dependentAction.getClass().getSimpleName() + " allowed");
        if (constraint.check(trans)) {
            Timber.i( "WorkflowEngine: Run constrained action:%s", dependentAction.getClass().getSimpleName());
            this.dependentAction.run(d, mal, context);
        } else {
            Timber.i( "Not allowed:%s", dependentAction.getClass().getSimpleName());

        }
    }
}
