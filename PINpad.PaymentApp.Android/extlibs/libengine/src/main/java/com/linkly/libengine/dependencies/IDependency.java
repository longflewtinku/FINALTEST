package com.linkly.libengine.dependencies;

import androidx.annotation.Nullable;

import com.linkly.libconfig.DownloadCfg;
import com.linkly.libconfig.ProfileCfg;
import com.linkly.libengine.application.IAppCallbacks;
import com.linkly.libengine.config.BinRangesCfg;
import com.linkly.libengine.config.IConfig;
import com.linkly.libengine.config.PayCfg;
import com.linkly.libengine.debug.IDebug;
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

public interface IDependency {
    WorkflowScheduler getWorkflowScheduler();
    @Nullable
    TransRec getCurrentTransaction();

    void resetIPCEvent(PositiveTransEvent ipcEvent);
    void resetCurrentTransaction(TransRec trans);

    void setWorkflowEngine(WorkflowEngine engine);
    WorkflowEngine getWorkflowEngine();

    IUI getFramework();
    IConfig getConfig();
    void setConfig(IConfig configProvider);
    IPrintManager getPrintManager();
    IUIDisplay getUI();
    ICustomer getCustomer();
    IAppCallbacks getAppCallbacks();
    IProto getProtocol();
    IMessages getMessages();
    IJobs getJobs();
    IComms getComms();
    IP2PLib getP2PLib();
    IStatus getStatusReporter();
    IDebug getDebugReporter();

    String getPrompt(IUIDisplay.String_id promptId);
    UserManager getUsrMgr();
    IUICallbacks getDisplayCallback();
    PayCfg getPayCfg();
    BinRangesCfg getBinRangesCfg();
    ProfileCfg getProfileCfg();
    DownloadCfg getDownloadCfg();

    // so our p2pe object can be invalid if secapp crashes.
    // This will need to be reinitialised/updated accordingly.
    void setP2PLib(IP2PLib p2PLib);
}
