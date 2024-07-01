package com.linkly.libengine.workflow;

import static com.linkly.libengine.engine.EngineManager.TransType.SALE;
import static java.util.stream.Collectors.toList;

import android.content.Context;

import com.linkly.libengine.action.IAction;
import com.linkly.libengine.dependencies.IDependency;
import com.linkly.libengine.engine.Engine;
import com.linkly.libengine.engine.transactions.TransRec;
import com.linkly.libmal.IMal;
import com.linkly.libmal.MalFactory;
import com.linkly.libmal.global.util.Util;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;


/***
 * TODO: Major refactor required...
 * Following issues:
 * 1. This class is globally called however required initialisation of Engine. However this is called in random parts of our code....
 * 2. Whenever an item is added to the "queue" it will check and automatically process it. Meaning recursive processes can happen...
 * This is fine except this skips our "main loop" (EFTActivityProcessor)... This is probably the reason why integration txn work.
 * 3. Circular dependencies....
 * 4. Context reliance in a thread with no form of clean up...
 *
 * This code is a buggy mess that already has issues and a bunch more that will randomly happen.
 */
public class WorkflowScheduler {
    private static final int MAX_QUEUED_WORKFLOWS = 1;

    private static WorkflowScheduler thisInstance = null;

    private static WorkflowThread fgThread = null;
    private static WorkflowThread bgThread = null;
    private static List<QueuedWorkflow> fgQueuedWorkflows = new ArrayList<QueuedWorkflow>();
    private static List<QueuedWorkflow> bgQueuedWorkflows = new ArrayList<QueuedWorkflow>();

    public static WorkflowScheduler getInstance() {
        if (thisInstance == null) {
            thisInstance = new WorkflowScheduler();
        }
        return thisInstance;
    }

    public int getFGQueueCount() {
        return fgQueuedWorkflows.size();
    }

    public int getTotalQueueCount() {
        return fgQueuedWorkflows.size() + bgQueuedWorkflows.size();
    }

    public WorkflowThread checkThreadRunning(List<QueuedWorkflow> list, WorkflowThread thread, boolean background, boolean external) {
        if (thread == null || !thread.isAlive() || thread.isInterrupted()) {

            thread = new WorkflowThread(list, background, Engine.getDep(), MalFactory.getInstance(), MalFactory.getInstance().getMalContext());
            Timber.i( "Initiate FG Thread");
            if (external)
                Timber.i( "Thread Started Externally");
            thread.start();
        } else {
            Timber.i( "Thread Already Running");
        }

        return thread;
    }

    public boolean checkThreadsRunning(boolean external) {
        Timber.d("checkThreadsRunning...fgQueuedWorkflows count: %d", fgQueuedWorkflows.size());
        if (fgQueuedWorkflows.size() > 0) {
            Timber.i( "Check FG Thread");
            fgThread = checkThreadRunning(fgQueuedWorkflows, fgThread, false, external);
        }

        if (bgQueuedWorkflows.size() > 0) {
            bgThread = checkThreadRunning(bgQueuedWorkflows, bgThread, true, external);
        }

        return true;
    }

    public boolean isTerminalBusy(boolean background) {
        List<QueuedWorkflow> list = fgQueuedWorkflows;
        if (background) {
            list = bgQueuedWorkflows;
        }
        return list.size() >= MAX_QUEUED_WORKFLOWS;
    }

    public boolean queueWorkflow(Workflow workflow, boolean background, boolean startPleaseWaitFirst, boolean exitIdleState) {
        Timber.d("queueWorkflow...startPleaseWaitFirst: %b", startPleaseWaitFirst);
        List<QueuedWorkflow> list = fgQueuedWorkflows;

        boolean addedToWorkflow = false;
        if (background) {
            list = bgQueuedWorkflows;
        }

        synchronized(list) {
            Timber.v("queueWorkflow, foreground = %b, list size = %d", !background, list.size() );
            if (list.size() < MAX_QUEUED_WORKFLOWS) {
                list.add(new QueuedWorkflow(workflow, exitIdleState));
                addedToWorkflow = true;
            }

            // Necessary (it seems) in order for e.g. underlying FragInputIdle to display between
            //  state changes during a Transaction as well as to see FragInputIdle dismissed
            //  properly with the end of a Transaction.
            if (!background && startPleaseWaitFirst)
                Engine.getAppCallbacks().runPleaseWaitScreen();

            // Very important in order for ActIdle's EFTActivityProcessor to exit the taskRunning
            //  state.
            checkThreadsRunning(false);
        }
        return addedToWorkflow;
    }

    public boolean queueWorkflow(Workflow workflow, boolean background, boolean startPleaseWaitFirst) {
        return queueWorkflow(workflow, background, startPleaseWaitFirst, true);
    }

    public boolean queueWorkflow(Workflow workflow, boolean background) {
        Timber.d("queueWorkflow...workflow: %s", workflow.getActions().stream().map(IAction::getName).collect(toList()));
        return queueWorkflow(workflow, background, true, true);
    }

    /***
     * Checks against the current workflow to see if we can interrupt it.
     * This feature and option is only ever for idle operations that have infinite timeout such
     * as password screen & main menu.
     * This is to stop the issue where the terminal could get into a state where it was sitting in idle
     * while being used as an
     * @param isBackground if we should check the background thread or not. Usually this is not used.
     * @return true or false if we interrupted the thread.
     */
    public boolean checkAndRemoveWaitingWorkFlow(boolean isBackground) {
        return checkAndRemovedWaitingWorkflows(isBackground ? bgThread : fgThread);
    }


    // Ignore the interrupt exception here. not much we can do if this has an issue.
    @SuppressWarnings("java:S2142")
    private boolean checkAndRemovedWaitingWorkflows(WorkflowThread thread) {
        // Check our current element
        if(thread != null && thread.getList() != null && !thread.getList().isEmpty()) {
            QueuedWorkflow current = thread.getList().get(0);
            IAction action = current.workflow.getActions().get(0);
            Timber.v("checkAndRemovedWaitingWorkflows: workflow threads are active");

            // Rules are as follows. Must be a single action workflow, that is cancellable and we are running the action
            if(current.workflow.actionCount() == 1 && action != null && action.cancellableAction() && thread.isRunningAction) {
                Timber.v("checkAndRemovedWaitingWorkflows: cancelling workflow %s, action %s", current.workflow.getClass().getName(), action.getName());
                //Interrupt the thread.
                // Note thread.stop is not available on some android devices (With throw and exception)
                thread.interrupt();
                // Also try to cancel the action  as in some cases thread could be a different, main instead of background
                // so just let the action itself decide on how it wants to cancel
                action.cancel();

                // wait for the thread to finish and tidy up
                try {
                    thread.join(500); // give half a second for it to clean up
                } catch (InterruptedException e) {
                    // We can ignore this interrupt exception
                    Timber.w(e);
                }

                return true;
            }
        }

        return false;
    }

    private static class WorkflowThread extends Thread {

        List<QueuedWorkflow> list = null;
        boolean background = false;
        boolean isRunningAction = false;

        IDependency dependencies = null;
        IMal mal = null;
        Context context = null;


        List<QueuedWorkflow> getList() {
            return list;
        }

        public WorkflowThread(List<QueuedWorkflow> list, boolean background, IDependency dependency, IMal mal, Context context) {
            this.list = list;
            this.background = background;
            this.dependencies = dependency;
            this.mal = mal;
            this.context = context;
        }

        @Override
        public void run() {
            Timber.d("run[WorkflowScheduler]...list size: %d", list.size());

            while(!list.isEmpty()) {

                QueuedWorkflow q = null;

                synchronized (list) {
                    q = list.get(0);
                }

                try {
                    WorkflowEngine engine = new WorkflowEngine();
                    isRunningAction = true;
                    exitMainMenuIdleState(q.exitIdleState);
                    engine.run(q.workflow, dependencies, mal, context);
                    enterMainMenuIdleState(q.exitIdleState);
                    isRunningAction = false;
                } catch ( Exception e ) {
                    Timber.w(e);
                    Timber.i( "CAUGHT ERROR");
                }

                synchronized (list) {
                    Timber.d("...removing IAction from working queue...");
                    list.remove(0);
                }

                if(isInterrupted()) {
                    Timber.d("Thread is Interrupted Exiting now");
                    break;
                }
            }
            Timber.d("...workflow IActions all completed, finalizing workflow...");

            /* the background threads shouldn't touch the current transaction */
            if (!background) {
                TransRec currentTransaction = dependencies.getCurrentTransaction();
                potentiallyTailorMainMenuIdleState(dependencies, currentTransaction);
                dependencies.resetCurrentTransaction(null);
                potentiallySignalTransactionExited(dependencies, currentTransaction);
            }
            dependencies.setWorkflowEngine(null);
        }

        private void potentiallyTailorMainMenuIdleState(IDependency dependencies, TransRec currentTransaction) {
            // Decide on the idle state based on transaction finalisation, before its being reset for standalone only financial transactions
            if (currentTransaction != null
                    && currentTransaction.isStandaloneOnlyFinancialTransaction()
                    && currentTransaction.getTransType() != SALE
                    && currentTransaction.isFinalised()) {
                Timber.d("...tailoring MainMenu Idle State...");
                // Prepare Input Amount screen with transaction type as SALE by default when cancelled a SALE transactions.
                // When a financial transaction other than SALE is cancelled, return to main menu screen.
                dependencies.getAppCallbacks().setShouldMainMenuDisplayInputAmountIdle(
                        !currentTransaction.isCancelled());
            }
        }

        private void potentiallySignalTransactionExited(IDependency dependencies, TransRec currentTransaction) {
            if (currentTransaction != null
                    && currentTransaction.isFinalised()) {
                Timber.d("...signalling that transaction flow ended...");
                dependencies.getAppCallbacks().onTransactionFlowExited();
            }
        }

        // Exit Idle State before Workflow started unless
        // special workflow not meant to interrupt Idle
        private void exitMainMenuIdleState(  boolean exitIdleState ) {
            if( Engine.getAppCallbacks() != null &&  exitIdleState ) {
                Engine.getAppCallbacks().exitMainMenuIdleState();
            }
        }

        // Enter Idle State back after Workflow completed if exited before
        private void enterMainMenuIdleState(boolean exitIdleState ) {
            if( Engine.getAppCallbacks() != null && exitIdleState) {
                Engine.getAppCallbacks().enterMainMenuIdleState();
            }
        }
    }



    /* only cares about FG thread */
    public boolean taskRunning() {
        if (fgThread != null && fgThread.isAlive())
            return true;
        return false;
    }

    /* waits for FG thread only */
    public void waitForWorkflow(){
        Util.Sleep(250);

        while (taskRunning()) {
            Util.Sleep(100);
        }
    }

    public static boolean isTransactionRunning() {
        if (WorkflowScheduler.getInstance().taskRunning()) {
            return true;
        }
        return false;
    }

    private static class QueuedWorkflow{
        Workflow workflow;
        boolean exitIdleState;  // parameter to control if running this Workflow should trigger exit from Idle (used in Autosettlement)
        QueuedWorkflow(Workflow w, boolean exitIdleState) {
            workflow = w;
            this.exitIdleState = exitIdleState;
        }
    }
}
