package com.linkly.libengine.dependencies;

import static com.linkly.libengine.status.IStatus.STATUS_EVENT.STATUS_TRANS_STARTED;

import androidx.annotation.Nullable;

import com.linkly.libconfig.DownloadCfg;
import com.linkly.libconfig.ProfileCfg;
import com.linkly.libengine.application.IAppCallbacks;
import com.linkly.libengine.config.BinRangesCfg;
import com.linkly.libengine.config.IConfig;
import com.linkly.libengine.config.PayCfg;
import com.linkly.libengine.debug.IDebug;
import com.linkly.libengine.engine.Engine;
import com.linkly.libengine.engine.comms.IComms;
import com.linkly.libengine.engine.customers.ICustomer;
import com.linkly.libengine.engine.protocol.IProto;
import com.linkly.libengine.engine.transactions.TransRec;
import com.linkly.libengine.jobs.IJobs;
import com.linkly.libengine.printing.IPrintManager;
import com.linkly.libengine.status.IStatus;
import com.linkly.libengine.users.UserManager;
import com.linkly.libengine.workflow.WorkflowEngine;
import com.linkly.libengine.workflow.WorkflowScheduler;
import com.linkly.libpositive.events.PositiveTransEvent;
import com.linkly.libpositive.messages.IMessages;
import com.linkly.libsecapp.IP2PLib;
import com.linkly.libui.IUI;
import com.linkly.libui.IUICallbacks;
import com.linkly.libui.IUIDisplay;

import java.util.HashMap;

import timber.log.Timber;

public class Dependencies implements IDependency {
    private WorkflowScheduler workflowScheduler;
    @Nullable
    private TransRec transactionData; // transient - updated with each transaction

    private IPrintManager printManager; // set once at startup

    private IUI framework; // set once at startup

    private IConfig config; // set once at startup

    private IAppCallbacks appCallbacks; // set once at startup

    private ICustomer customer; // set once at startup

    private IProto protocol; // set once at startup

    private IMessages messages; // set once at startup

    private IJobs jobs; // set once at startup

    private IComms comms; // set once at startup

    private IP2PLib p2PLib;

    private IStatus statusReporter;

    private UserManager usrMgr; // set once at startup

    private IUICallbacks displayCallback; // set once at startup

    private PositiveTransEvent ipcEvent;
    private HashMap<Long, WorkflowEngine> engineMap = new HashMap<>();

    private IDebug debugReporter;

    private PayCfg payCfg;

    @Override
    public WorkflowScheduler getWorkflowScheduler() {
        return this.workflowScheduler;
    }

    @Override
    public TransRec getCurrentTransaction() {
        return this.transactionData;
    }

    @Override
    public void resetIPCEvent(PositiveTransEvent ipcEvent) {
        this.ipcEvent = ipcEvent;
    }

    @Override
    public void resetCurrentTransaction(TransRec trans) {
        this.transactionData = trans;

        Timber.d("resetCurrentTransaction...TransRec: %s", trans);

        if (ipcEvent != null && trans != null)
            this.transactionData.setTransEvent(ipcEvent);

        /* possibly remove this from dependencies */
        if (trans != null && Engine.getStatusReporter() != null) {
            Engine.getStatusReporter().reportStatusEvent(STATUS_TRANS_STARTED , trans.isSuppressPosDialog());

            transactionData.setEmvTagsString(trans.getEmvTagsString());
            transactionData.setCtlsTagsString(trans.getCtlsTagsString());

        }

    }

    public void setWorkflowEngine(WorkflowEngine engine) {
        if (engine == null) {
            Timber.i( "Remove Workflowengine for ThreadID:%d", Thread.currentThread().getId());
            engineMap.remove(Thread.currentThread().getId());
        } else {
            Timber.i( "Set Workflowengine:" + engine + " ThreadID:" + Thread.currentThread().getId());
            engineMap.put(Thread.currentThread().getId(), engine);
        }
    }

    public WorkflowEngine getWorkflowEngine() {
        WorkflowEngine workflowEngine = engineMap.get(Thread.currentThread().getId());
        Timber.i( "Get Workflowengine:" + workflowEngine + " ThreadID:" + Thread.currentThread().getId());
        return workflowEngine;
    }

    public IUIDisplay getUI() {
        if( framework == null )
            return null;

        return framework.getUI();
    }

    public String getPrompt(IUIDisplay.String_id promptId) {
        if( framework == null )
            return null;

        return framework.getPrompt(promptId);
    }

    public PayCfg getPayCfg() {
        return payCfg;
    }

    public BinRangesCfg getBinRangesCfg() {
        if( config == null )
            return null;

        return config.getBinRangesCfg();
    }

    public ProfileCfg getProfileCfg() {
        if( config == null )
            return null;

        return config.getProfileCfg();
    }

    public DownloadCfg getDownloadCfg() {
        if( config == null )
            return null;

        return config.getDownloadCfg();
    }

    public IPrintManager getPrintManager() {
        return this.printManager;
    }

    public IUI getFramework() {
        return this.framework;
    }

    public IConfig getConfig() {
        return this.config;
    }

    public IAppCallbacks getAppCallbacks() {
        return this.appCallbacks;
    }

    public ICustomer getCustomer() {
        return this.customer;
    }

    public IProto getProtocol() {
        return this.protocol;
    }

    public IMessages getMessages() {
        return this.messages;
    }

    public IJobs getJobs() {
        return this.jobs;
    }

    public IComms getComms() {
        return this.comms;
    }

    public IP2PLib getP2PLib() {
        return this.p2PLib;
    }

    public IStatus getStatusReporter() {
        return this.statusReporter;
    }

    public UserManager getUsrMgr() {
        return this.usrMgr;
    }

    public IUICallbacks getDisplayCallback() {
        return this.displayCallback;
    }

    public IDebug getDebugReporter() {
        return this.debugReporter;
    }

    public void setPrintManager(IPrintManager printManager) {
        this.printManager = printManager;
    }

    public void setPayCfg(PayCfg config) { payCfg = config; }

    public void setFramework(IUI framework) {
        this.framework = framework;
    }

    public void setConfig(IConfig config) {
        this.config = config;
    }

    public void setAppCallbacks(IAppCallbacks appCallbacks) {
        this.appCallbacks = appCallbacks;
    }

    public void setCustomer(ICustomer customer) {
        this.customer = customer;
    }

    public void setProtocol(IProto protocol) {
        this.protocol = protocol;
    }

    public void setMessages(IMessages messages) {
        this.messages = messages;
    }

    public void setJobs(IJobs jobs) {
        this.jobs = jobs;
    }

    public void setComms(IComms comms) {
        this.comms = comms;
    }

    @Override
    public void setP2PLib(IP2PLib p2PLib) {
        this.p2PLib = p2PLib;
    }

    public void setStatusReporter(IStatus statusReporter) {
        this.statusReporter = statusReporter;
    }

    public void setUsrMgr(UserManager usrMgr) {
        this.usrMgr = usrMgr;
    }

    public void setDisplayCallback(IUICallbacks displayCallback) {
        this.displayCallback = displayCallback;
    }

    public void setDebugReporter(IDebug debugReporter) {
        this.debugReporter = debugReporter;
    }
}
