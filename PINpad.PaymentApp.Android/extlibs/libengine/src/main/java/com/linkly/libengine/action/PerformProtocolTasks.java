package com.linkly.libengine.action;

import timber.log.Timber;

public class PerformProtocolTasks extends IAction {

    @Override
    public String getName() {
        return "PerformProtocolTasks";
    }

    @Override
    public void run() {
        if( d.getProtocol() != null ) {
            if( !d.getProtocol().performProtocolChecks() ){
                d.getWorkflowEngine().setNextAction( TransactionCanceller.class );
            }
        } else {
            Timber.e( "Protocol layer is null" );
        }
    }
}
