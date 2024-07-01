package com.linkly.libengine.workflow;

import com.linkly.libengine.action.ConstrainedAction;
import com.linkly.libengine.action.IAction;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import timber.log.Timber;


@SuppressWarnings("rawtypes")
public abstract class Workflow {
    List<IAction> actions = new ArrayList<>();

    public void addAction(IAction action) {
        actions.add(action);
    }

    public void insertBeforeAction(IAction insertAction, Class afterThisAction) {
        final int NOT_FOUND = -1;
        int foundIndex = NOT_FOUND;
        for (int idx = 0; idx < this.actions.size(); ++idx) {
            if (this.actions.get(idx).getClass() == afterThisAction) {
                foundIndex = idx;
                break;
            }
        }
        if (foundIndex == NOT_FOUND) {
            Timber.i( "Critical processing error when inserting action");
        } else {
            actions.add(foundIndex, insertAction);
        }
    }

    public void insertAfterAction(IAction insertAction, Class afterThisAction) {
        final int NOT_FOUND = -1;
        int foundIndex = NOT_FOUND;
        for (int idx = 0; idx < this.actions.size(); ++idx) {
            if (this.actions.get(idx).getClass() == afterThisAction) {
                foundIndex = idx;
                break;
            }
        }
        if (foundIndex == NOT_FOUND) {
            Timber.i( "Critical processing error when inserting action");
        } else {
            actions.add(foundIndex + 1, insertAction);
        }
    }

    public void removeAction(Class action) {
        for (int idx = 0; idx < this.actions.size(); ++idx) {
            if (this.actions.get(idx).getClass() == action) {
                Timber.v( "Removed action:" + action + "(" + idx + ")");
                actions.remove(idx);
                break;
            }
        }

        Timber.v( "Warning, can't remove action, reason: not found");
    }

    @SuppressWarnings("rawtypes")
    public void removeConstrainedActions(Class action) {
        ListIterator<IAction> iter = actions.listIterator();
        int count = 0;
        while( iter.hasNext() ) {
            IAction iaction = iter.next();
            if( iaction.getClass() == ConstrainedAction.class ) {
                ConstrainedAction constrainedAction = (ConstrainedAction)iaction;
                if( constrainedAction.getDependentAction().getClass() == action ) {
                    iter.remove();
                    count++;
                }
            }
        }
        Timber.i( "removed %d %s constrained actions", count, action.getName() );
    }

    public List<IAction> getActions() {
        return this.actions;
    }

    // Returns the count of how many actions we have.
    public int actionCount() {
        return actions.size();
    }
}
