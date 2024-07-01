package com.linkly.libengine.action;

import android.content.Context;

import com.linkly.libengine.dependencies.IDependency;
import com.linkly.libengine.engine.transactions.TransRec;
import com.linkly.libmal.IMal;
import com.linkly.libui.IUICurrency;
import com.linkly.libui.IUIDisplay;

abstract public class IAction {
    public IDependency d = null;
    public IUIDisplay ui = null;
    public TransRec trans = null;
    public IUICurrency curr = null;
    public IMal mal = null;
    public Context context = null;

    abstract public String getName();

    private void setDependencies( IDependency dependencies, IMal currentMal, Context currentContext ) {
        if( dependencies == null )
            throw new NullPointerException();

        d = dependencies;
        ui = d.getFramework().getUI();
        curr = d.getFramework().getCurrency();
        trans = d.getCurrentTransaction();
        mal = currentMal;
        context = currentContext;
    }

    public void run( IDependency dependencies, IMal currentMal, Context currentContext ) {
        setDependencies( dependencies, currentMal, currentContext );
        run();
    }

    abstract public void run();

    // If an action is allowed to be interrupted/cancellable
    // The use of this is in cases where we have the terminal sitting on an idle screen doing nothing waiting for user input.
    // E.g we get an auto sale event while on the user logon screen.
    public boolean cancellableAction() {
        return false;
    }

    /**
     * When the action is cancellable, any action can override this to have custom cancel operation
     */
    public void cancel() {
    }
}
