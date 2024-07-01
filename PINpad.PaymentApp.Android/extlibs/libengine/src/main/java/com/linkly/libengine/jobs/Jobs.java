package com.linkly.libengine.jobs;

import static com.linkly.libengine.engine.EngineManager.TransType.AUTOMATIC_SHIFT_TOTALS;
import static com.linkly.libengine.engine.EngineManager.TransType.AUTOSETTLEMENT;
import static com.linkly.libengine.engine.EngineManager.TransType.AUTO_LOGON;
import static com.linkly.libengine.engine.EngineManager.TransType.LAST_RECONCILIATION_AUTO;
import static com.linkly.libengine.engine.EngineManager.TransType.RECONCILIATION;
import static com.linkly.libengine.engine.EngineManager.TransType.RECONCILIATION_AUTO;
import static com.linkly.libengine.engine.EngineManager.TransType.SHIFT_TOTALS_AUTO;
import static com.linkly.libengine.engine.EngineManager.TransType.SUB_TOTALS_AUTO;
import static com.linkly.libengine.engine.EngineManager.TransType.SUMMARY_AUTO;
import static com.linkly.libpositive.PosIntegrate.ResultResponse.RES_TERMINAL_BUSY;
import static com.linkly.libpositive.PosIntegrate.ResultResponse.RES_TRANSACTION_TYPE_NOT_FOUND;
import static com.linkly.libpositive.events.PositiveEvent.EventType.Z_REPORT;
import static com.linkly.libpositive.events.PositiveTransEvent.POS_KEYPRESS.CANCEL;
import static com.linkly.libpositive.events.PositiveTransEvent.POS_KEYPRESS.UNKNOWN;

import android.content.Context;

import com.linkly.libengine.action.IAction;
import com.linkly.libengine.action.MenuOperations.admin.PaymentAppStatusInfo;
import com.linkly.libengine.action.MenuOperations.admin.SubmitTransactions;
import com.linkly.libengine.action.MenuOperations.admin.reports.ReportHistory;
import com.linkly.libengine.action.Printing.PrintReprintLastAuto;
import com.linkly.libengine.action.Printing.PrintReprintTransaction;
import com.linkly.libengine.action.QueryTransactions;
import com.linkly.libengine.action.user_action.BackToIdlePosNotification;
import com.linkly.libengine.action.user_action.DisplayFinishTransaction;
import com.linkly.libengine.action.user_action.SilentCardReadCommand;
import com.linkly.libengine.dependencies.IDependency;
import com.linkly.libengine.engine.Engine;
import com.linkly.libengine.engine.EngineManager;
import com.linkly.libengine.engine.transactions.TransRec;
import com.linkly.libengine.engine.transactions.TransRecManager;
import com.linkly.libengine.env.IdentityEnvVar;
import com.linkly.libengine.helpers.ECRHelpers;
import com.linkly.libengine.workflow.CardRead;
import com.linkly.libengine.workflow.Reconciliation;
import com.linkly.libengine.workflow.Workflow;
import com.linkly.libengine.workflow.WorkflowAddActions;
import com.linkly.libengine.workflow.WorkflowScheduler;
import com.linkly.libmal.global.platform.Platform;
import com.linkly.libmal.global.util.Util;
import com.linkly.libpositive.events.PositiveEvent;
import com.linkly.libpositive.events.PositiveReadCardEvent;
import com.linkly.libpositive.events.PositiveTransEvent;
import com.linkly.libpositivesvc.POSitiveSvcLib;
import com.linkly.libsecapp.P2PLib;
import com.linkly.libui.IUIDisplay;
import com.linkly.libui.display.Display;

import java.util.ArrayList;

import timber.log.Timber;

public class Jobs implements IJobs {

    private static ArrayList<EFTJob> jobList = new ArrayList<>();
    private static final String TRANSACTION_NOT_FOUND_DEBUG = "Transaction not found:%s";

    private static Jobs instance = null;

    public static Jobs getInstance() {
        if (instance == null) {
            instance = new Jobs();
        }
        return instance;
    }

    public boolean add(EFTJob job) {

        int i;
        PositiveEvent oldEvent = null;
        /*Only Add an Event Once, i.e. we dont want 3 Auto Recs because the apps been off.*/
        for (i = 0; i < jobList.size(); i++) {
            oldEvent = jobList.get(i);
            if (oldEvent != null && oldEvent.getType() == job.getType()) {
                jobList.remove(i);
            }
        }

        jobList.add(job);
        return true;
    }

    public boolean pending() {
        //TODO : Maybe Check if Event has expired... got too old.
        return (jobList != null && !jobList.isEmpty());
    }

    public EFTJob getNext() {
        EFTJob event = null;

        if (pending()) {
            //Get the Oldest... Maybe we want to Specify order/priority..
            event = jobList.get(0);
            jobList.remove(0);
        }

        return event;
    }

    /* don't do anything that takes a long time on here as it is called from the Display thread */
    /* especially dont wait for anything back from the Display thread */
    public boolean perform(IDependency d, Context context) {

        EFTJob job = getNext();
        if ( job != null ) {
            boolean addedToWorkflow = false;
            Timber.d("perform...job: %s", job.getType().name());
            switch ( job.getType() ) {
                case AUTO_REC: {
                    //Generate a Rec request
                    d.getMessages().sendAutoRecResponse(context);
                    TransRec trans = new TransRec( RECONCILIATION, d);
                    Engine.getDep().resetCurrentTransaction( trans );
                    addedToWorkflow = WorkflowScheduler.getInstance().queueWorkflow( new Reconciliation(), false );
                    break;
                }
                case AUTO_REBOOT:
                case REBOOT:
                case PCI24HOUR_REBOOT: {
                    WorkflowScheduler.getInstance().checkAndRemoveWaitingWorkFlow(false);
                    addedToWorkflow = WorkflowScheduler.getInstance().queueWorkflow( new WorkflowAddActions( new com.linkly.libengine.action.MenuOperations.admin.Reboot() ), false );
                    break;
                }
                case BATCH_UPLOAD: {
                    addedToWorkflow = WorkflowScheduler.getInstance().queueWorkflow( new WorkflowAddActions( new SubmitTransactions( true ) ), true, false, false );
                    break;
                }
                case RUN_TRANS:
                case RUN_TRANS_SILENT: {
                    PositiveTransEvent transEvent = ( PositiveTransEvent ) job.getEventDetail();
                    Timber.i("TransRec Type: %s", transEvent.getTransType());
                    EngineManager.TransType transType = EngineManager.TransType.getTransTypeByString(transEvent.getTransType());
                    if (null == transType) {
                        Timber.i(TRANSACTION_NOT_FOUND_DEBUG, transEvent.getTransType());
                        ECRHelpers.ipcSendNullTransResponse(d, transEvent, RES_TRANSACTION_TYPE_NOT_FOUND, context);
                        break;
                    }
                    TransRec trans = new TransRec(transType, d);

                    Workflow w = Engine.getAppCallbacks().getWorkflowFactory().getWorkflow(trans.getTransType());
                    if (w != null) {
                        WorkflowScheduler.getInstance().checkAndRemoveWaitingWorkFlow(false);
                        Engine.getDep().resetIPCEvent(transEvent);
                        Engine.getDep().resetCurrentTransaction(trans);
                        addedToWorkflow = WorkflowScheduler.getInstance().queueWorkflow(w, false);

                        // Return a response stating we are in progress already with something else.
                        Timber.v("addedToWorkflow = %b", addedToWorkflow);
                        if(!addedToWorkflow) {
                            Timber.e("sending RES_TERMINAL_BUSY response");
                            ECRHelpers.ipcSendNullTransResponse(d, transEvent, RES_TERMINAL_BUSY, context);
                        } else {
                            Timber.v("workflow added to queue okay");
                        }
                    } else {
                        Timber.i(TRANSACTION_NOT_FOUND_DEBUG, transEvent.getTransType());
                        ECRHelpers.ipcSendNullTransResponse(d, transEvent, RES_TRANSACTION_TYPE_NOT_FOUND, context);
                    }
                    break;
                }

                case X_REPORT:
                case Z_REPORT: {
                    // Auto rec
                    PositiveTransEvent transEvent = (PositiveTransEvent) job.getEventDetail();

                    TransRec trans = new TransRec(job.getType() == Z_REPORT ? RECONCILIATION_AUTO : SUMMARY_AUTO, d);
                    WorkflowScheduler.getInstance().checkAndRemoveWaitingWorkFlow(false);
                    Engine.getDep().resetIPCEvent(transEvent);
                    Engine.getDep().resetCurrentTransaction(trans);

                    // get the relevant auto reconciliation workflow for given customer
                    Workflow w = Engine.getAppCallbacks().getWorkflowFactory().getWorkflow(job.getType() == Z_REPORT ? RECONCILIATION_AUTO : SUMMARY_AUTO);
                    addedToWorkflow = WorkflowScheduler.getInstance().queueWorkflow(w, false);
                    break;
                }
                case HISTORY_REPORT: {
                    WorkflowScheduler.getInstance().checkAndRemoveWaitingWorkFlow(false);
                	WorkflowScheduler.getInstance().queueWorkflow( new WorkflowAddActions( new ReportHistory( true ) ), false );
                    WorkflowScheduler.getInstance().waitForWorkflow();
                    break;
                }
                case LAST_RECONCILIATION_REPORT: {
                    PositiveTransEvent transEvent = (PositiveTransEvent) job.getEventDetail();
                    WorkflowScheduler.getInstance().checkAndRemoveWaitingWorkFlow(false);
                    Engine.getDep().resetIPCEvent(transEvent);

                    TransRec trans = new TransRec(LAST_RECONCILIATION_AUTO, d);
                    Engine.getDep().resetCurrentTransaction(trans);

                    Workflow w = Engine.getAppCallbacks().getWorkflowFactory().getWorkflow(LAST_RECONCILIATION_AUTO);
                    addedToWorkflow = WorkflowScheduler.getInstance().queueWorkflow(w, false);
                    break;
                }
                case SUB_SHIFT_TOTALS_REPORT: {
                    PositiveTransEvent transEvent = (PositiveTransEvent) job.getEventDetail();
                    WorkflowScheduler.getInstance().checkAndRemoveWaitingWorkFlow(false);
                    Engine.getDep().resetIPCEvent(transEvent);

                    TransRec trans = new TransRec(SUB_TOTALS_AUTO, d);
                    Engine.getDep().resetCurrentTransaction(trans);

                    Workflow w = Engine.getAppCallbacks().getWorkflowFactory().getWorkflow(SUB_TOTALS_AUTO);
                    addedToWorkflow = WorkflowScheduler.getInstance().queueWorkflow(w, false);
                    break;
                }
                case SHIFT_TOTALS_REPORT: {
                    PositiveTransEvent transEvent = (PositiveTransEvent) job.getEventDetail();
                    WorkflowScheduler.getInstance().checkAndRemoveWaitingWorkFlow(false);
                    Engine.getDep().resetIPCEvent(transEvent);

                    TransRec trans = new TransRec(SHIFT_TOTALS_AUTO, d);
                    Engine.getDep().resetCurrentTransaction(trans);

                    Workflow w = Engine.getAppCallbacks().getWorkflowFactory().getWorkflow(SHIFT_TOTALS_AUTO);
                    addedToWorkflow = WorkflowScheduler.getInstance().queueWorkflow(w, false);
                    break;
                }
                case SHIFT_TOTALS_AUTOMATIC_REPORT: {
                    // Scheduled event: close current Shift Totals and print report
                    PositiveTransEvent transEvent = (PositiveTransEvent) job.getEventDetail();
                    WorkflowScheduler.getInstance().checkAndRemoveWaitingWorkFlow(false);
                    Engine.getDep().resetIPCEvent(transEvent);

                    TransRec trans = new TransRec(AUTOMATIC_SHIFT_TOTALS, d);
                    Engine.getDep().resetCurrentTransaction(trans);

                    Workflow w = Engine.getAppCallbacks().getWorkflowFactory().getWorkflow(AUTOMATIC_SHIFT_TOTALS);
                    addedToWorkflow = WorkflowScheduler.getInstance().queueWorkflow(w, false, true, false);
                    break;
                }
                case CANCEL_TRANS: {
                	final PositiveTransEvent posTransEvent = ( PositiveTransEvent ) job.getEventDetail();
                	Timber.i( "TransRec cancelled time: " + posTransEvent.getCancelTime() );
                    if ( Engine.getDep().getCurrentTransaction() != null ) {

                        // Cancel the transaction if it is not too far into the transaction
                        TransRec trans = Engine.getDep().getCurrentTransaction();
                        if (!trans.isApprovedOrDeferred() && !trans.isFinalised()) {
	                        trans.cancelTransaction( posTransEvent );
                            Display.insertResultCode( IUIDisplay.UIResultCode.ABORT );
                        } else {
                            //Transaction is already processed.We cannot cancel the transaction
                            Timber.i( "Cannot cancel the transaction" );
                        }
                    }
                    break;
                }

                case POS_KEY_PRESS: {
                	final PositiveTransEvent posTransEvent = ( PositiveTransEvent ) job.getEventDetail();
                	PositiveTransEvent.POS_KEYPRESS posKeypress = posTransEvent.getPosKeypress();
                    if (posKeypress == null) {
                        Timber.e("POS_KEYPRESS is null");
                        posKeypress = UNKNOWN;
                    }
                    // Ignore any POS keypress except during a transaction
                    TransRec trans = Engine.getDep().getCurrentTransaction();
                    if( trans != null ){
                        IUIDisplay.UIResultCode code = IUIDisplay.UIResultCode.UNKNOWN;

                        switch( posKeypress ){
                            case CANCEL:
                                if(!trans.isApprovedOrDeferred() && !trans.isFinalised()) {
                                    if (trans.isMoto()) {
                                        // manual card data entry is done by secapp, cancel it's activity if any
                                        try {
                                            P2PLib.getInstance().getIP2P().cancelCurrentActivity();
                                        } catch (Exception e) {
                                            Timber.w(e);
                                        }
                                    }
                                    code = IUIDisplay.UIResultCode.ABORT;
	                                trans.cancelTransaction( posTransEvent );
                                } else {
                                    Timber.d( "Cancel Keypress received but cannot cancel the transaction" );
                                }
                                break;
                            case YES:
                                code = IUIDisplay.UIResultCode.POS_YES;
                                break;
                            case OK:
                                code = IUIDisplay.UIResultCode.OK;
                                break;
                            case DECLINE:
                                code = IUIDisplay.UIResultCode.ABORT;
                                break;
                            case UNKNOWN:
                            default:
                                Timber.d( "UNKNOWN KEYPRESS" );
                                break;
                        }

                        if( code != IUIDisplay.UIResultCode.UNKNOWN ) {
                            Display.insertResultCode( code );
                        }
                	} else if( CANCEL == posKeypress ){
                        // Assume that Cancel keypress is returned for non-transaction prompts
                        Display.insertResultCode( IUIDisplay.UIResultCode.ABORT );
                        Timber.d("Cancel keypress received, cancelling the current workflow");
                        WorkflowScheduler.getInstance().checkAndRemoveWaitingWorkFlow(false);
                    } else {
                    	Timber.w( "Transaction is null but we received POS_KEYPRESS = " + posKeypress );
                    }
                    break;
                }

                // Query/Reprint latest transaction
                case QUERY_TRANS: {
                    Workflow workflow;
                    PositiveTransEvent transEvent = (PositiveTransEvent) job.getEventDetail();
                    Timber.i("Query TransRec Type: %s", transEvent.getTransType());

                    if (transEvent.isReprint()) {
                        if (transEvent.getReference() != null) {
                            Timber.i("looking for txnRef %s", transEvent.getReference());
                            TransRec trans = TransRecManager.getInstance().getTransRecDao().getByReference(transEvent.getReference());
                            WorkflowScheduler.getInstance().checkAndRemoveWaitingWorkFlow(false);
                        	workflow = new WorkflowAddActions(new PrintReprintTransaction(transEvent, trans));
                        } else {
                        	IAction[] actions = { new PrintReprintLastAuto(transEvent), new DisplayFinishTransaction() } ;
                        	workflow = new WorkflowAddActions(actions);
                        }
                    } else {
	                    workflow = new WorkflowAddActions(new QueryTransactions(transEvent));
                    }

                    workflow.addAction(new BackToIdlePosNotification());
                    addedToWorkflow = WorkflowScheduler.getInstance().queueWorkflow(workflow, false, false);
                    break;
                }
                case READ_CARD: {
                    Timber.d("Read Card Event Found");
                    addedToWorkflow = WorkflowScheduler.getInstance().queueWorkflow(new CardRead((PositiveReadCardEvent) job.getEventDetail()), true);
                    break;
                }
                case READ_CARD_SILENT: {
                    Timber.d("Silent Read Card Event Found");
                	addedToWorkflow = WorkflowScheduler.getInstance().queueWorkflow(new WorkflowAddActions(new SilentCardReadCommand((PositiveReadCardEvent) job.getEventDetail())), false, false);
                    break;
                }
                case REQUEST_APP_INFO_STATUS: {
                    Timber.i( "Request Info" );
                	addedToWorkflow = WorkflowScheduler.getInstance().queueWorkflow( new WorkflowAddActions( new PaymentAppStatusInfo(job.getEventDetail().getMerchantNumber()) ), true, false, false );
                    break;
                }
                case AUTO_LOGON:{
                    PositiveTransEvent transEvent = ( PositiveTransEvent ) job.getEventDetail();

                    // TODO: Don't hardcode the string
                    TransRec trans = new TransRec( EngineManager.TransType.getTransTypeByString( "logonauto" ), d);
                    if( Engine.getAppCallbacks() != null ) {
                        Workflow w = Engine.getAppCallbacks().getWorkflowFactory().getWorkflow( AUTO_LOGON );

                        if ( w != null ) {
                            WorkflowScheduler.getInstance().checkAndRemoveWaitingWorkFlow(false);
                            Engine.getDep().resetIPCEvent( transEvent );
                            Engine.getDep().resetCurrentTransaction( trans );
                            trans.setPrintOnTerminal(trans.getTransEvent().isUseTerminalPrinter());
                            addedToWorkflow = WorkflowScheduler.getInstance().queueWorkflow( w, false );
                        } else {
                            Timber.i( TRANSACTION_NOT_FOUND_DEBUG, transEvent.getTransType() );
                            ECRHelpers.ipcSendNullTransResponse( d, transEvent, RES_TRANSACTION_TYPE_NOT_FOUND, context );
                        }
                    } else {
                        Timber.e( "Engine getAppCallbacks is null" );
                    }
                    break;
                }
                case UPDATE_CONFIG: {
                    PositiveEvent event = job.getEventDetail();
                    if ( event != null ) {
                        if( !Util.isNullOrEmpty( event.getMerchantId() ) ) {
                            d.getPayCfg().setMid( event.getMerchantId() );
                            IdentityEnvVar.setMid( event.getMerchantId() );
                        }
                        if( !Util.isNullOrEmpty( event.getTerminalId() ) ) {
                            IdentityEnvVar.setTid( event.getTerminalId() );
                            d.getPayCfg().setStid( event.getTerminalId() );
                        }
                        ECRHelpers.ipcSendConfigUpdateStatus( d, true, context );
                    } else {
                        Timber.e( "Null event received for UPDATE_CONFIG" );
                        ECRHelpers.ipcSendConfigUpdateStatus( d, false, context );
                    }
                    break;
                }
                case AUTO_SETTLEMENT: {
                    d.getMessages().sendAutoRecResponse(context);
                    TransRec trans = new TransRec( AUTOSETTLEMENT, d);
                    Engine.getDep().resetCurrentTransaction( trans );
                    Workflow w = Engine.getAppCallbacks().getWorkflowFactory().getWorkflow(AUTOSETTLEMENT);
                    addedToWorkflow = WorkflowScheduler.getInstance().queueWorkflow(w, false, true, false);
                    break;
                }
                case AUTO_START:
                case RUN_UPDATE:
                case HEART_BEAT:
                case USER_ADMIN:
                case CLEAR_DATABASE:
                case CANCEL_TRANS2:
                case CONTINUE_PRINT:    // Handled in MessageReceiver to speed up transaction
                default: {
                    Timber.i( "Unknown Scheduled Job = " + job.getType().toString() + " Ignoring the job" );
                    break;
                }
            }

            if(!addedToWorkflow) {
                Timber.e("Failed to add Workflow to Queue");
            }
        }
        return true;
    }

    public boolean schedule(Context context, EFTJobScheduleEvent event) {
        if (Platform.isPaxTerminal()) {
            POSitiveSvcLib.configureScheduledEvent(context, event);
        }
        return true;
    }

}
