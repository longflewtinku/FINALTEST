package com.linkly.payment.menus;

import static com.linkly.payment.menus.MenuType.SUBMENU;
import static com.linkly.payment.menus.MenuType.TASK;

import com.linkly.libengine.action.IAction;
import com.linkly.libengine.action.MenuOperations.admin.CNPMenu;
import com.linkly.libengine.action.MenuOperations.admin.Exit;
import com.linkly.libengine.action.MenuOperations.admin.Information;
import com.linkly.libengine.action.MenuOperations.admin.PreAuthSearchAndView;
import com.linkly.libengine.action.MenuOperations.admin.PrintCtlsConfig;
import com.linkly.libengine.action.MenuOperations.admin.PrintEmvConfig;
import com.linkly.libengine.action.MenuOperations.admin.PurgeFile;
import com.linkly.libengine.action.MenuOperations.admin.SafViewAndClear;
import com.linkly.libengine.action.MenuOperations.admin.SubmitTransactions;
import com.linkly.libengine.action.MenuOperations.admin.TransactionHistory;
import com.linkly.libengine.action.MenuOperations.admin.deferredauths.DisplayDeferredAuths;
import com.linkly.libengine.action.MenuOperations.admin.reports.ReportDailyBatch;
import com.linkly.libengine.action.MenuOperations.admin.reports.ReportHistory;
import com.linkly.libengine.action.MenuOperations.admin.reports.ReportTotals;
import com.linkly.libengine.action.MenuOperations.admin.reprint.RePrintByNumber;
import com.linkly.libengine.action.MenuOperations.admin.reprint.RePrintLast;
import com.linkly.libengine.action.MenuOperations.admin.reprint.RePrintLastReconciliation;
import com.linkly.libengine.action.MenuOperations.dev.CheckService;
import com.linkly.libengine.action.MenuOperations.dev.ClearReversals;
import com.linkly.libengine.action.MenuOperations.dev.CommsLoopTests;
import com.linkly.libengine.action.MenuOperations.dev.DukptTests;
import com.linkly.libengine.action.MenuOperations.dev.DBClear;
import com.linkly.libengine.action.MenuOperations.dev.DBList;
import com.linkly.libengine.action.MenuOperations.dev.DBRecList;
import com.linkly.libengine.action.MenuOperations.dev.DBStats;
import com.linkly.libengine.action.MenuOperations.dev.IccDiagnostics;
import com.linkly.libengine.action.MenuOperations.dev.KeyClear;
import com.linkly.libengine.action.MenuOperations.dev.KeyInject;
import com.linkly.libengine.action.MenuOperations.dev.KeyReset;
import com.linkly.libengine.action.MenuOperations.dev.TestAddTime;
import com.linkly.libengine.action.MenuOperations.dev.TestAddTrans;
import com.linkly.libengine.action.MenuOperations.dev.TestAutoDownload;
import com.linkly.libengine.action.MenuOperations.dev.TestSchedule;
import com.linkly.libengine.action.MenuOperations.tms.FullUpdate;
import com.linkly.libengine.action.MenuOperations.users.UserAdd;
import com.linkly.libengine.action.MenuOperations.users.UserChangePassword;
import com.linkly.libengine.action.MenuOperations.users.UserDelete;
import com.linkly.libengine.action.MenuOperations.users.UserLogin;
import com.linkly.libengine.action.MenuOperations.users.UserReport;
import com.linkly.libengine.action.MenuOperations.users.UserResetPassword;
import com.linkly.libengine.action.check.CheckP2P;
import com.linkly.libengine.engine.Engine;
import com.linkly.libengine.engine.EngineManager;
import com.linkly.libengine.engine.EngineManager.TransType;
import com.linkly.libengine.workflow.Workflow;
import com.linkly.libengine.workflow.WorkflowAddActions;
import com.linkly.libui.IUIDisplay.String_id;
import com.linkly.payment.R;

@SuppressWarnings("java:S115")
public enum MenuItems {
    Sale(String_id.STR_SALE, "Sale", EngineManager.TransType.SALE, R.drawable.ic_purchase, false),
    Pwcb(String_id.STR_CASHBACK_SHORT, "Sale+Cash", EngineManager.TransType.CASHBACK, R.drawable.ic_purchase_and_cashout, false),
    Cash(String_id.STR_CASH, "Cash", EngineManager.TransType.CASH, R.drawable.ic_cashout, false),
    SaleAuto(String_id.STR_SALE_AUTO, "Sale Auto", EngineManager.TransType.SALE_AUTO, R.drawable.ic_purchase, false),
    CardNotPresent(String_id.STR_CARD_NOT_PRESENT, "Card Not Present", TASK, new WorkflowAddActions(new CNPMenu()), R.drawable.ic_card_not_present),
    Reversal(String_id.STR_REVERSAL, "Reversal", EngineManager.TransType.MANUAL_REVERSAL, R.drawable.ic_void, false),
    ReversalAuto(String_id.STR_REVERSAL_AUTO, "ReversalAuto", TransType.MANUAL_REVERSAL_AUTO, R.drawable.ic_void, false),
    PreAuth(String_id.STR_PRE_AUTH, "Pre-Auth", EngineManager.TransType.PREAUTH, R.drawable.ic_pre_auth, true),
    PreAuthMoto(String_id.STR_PRE_AUTH_MOTO, "MOTO Pre-Auth", TransType.PREAUTH_MOTO, R.drawable.ic_pre_auth, true),
    PreAuthAuto(String_id.STR_PREAUTH_AUTO, "PreAuth Auto", EngineManager.TransType.PREAUTH, R.drawable.ic_pre_auth, false),
    PreAuthCancel(String_id.STR_PRE_AUTH_CANCEL, "Pre-Auth Cancel", TransType.PREAUTH_CANCEL, R.drawable.ic_pre_auth_cancel, false),
    Completion(String_id.STR_PRE_AUTH_COMPLETION, "Pre-Auth Completion", EngineManager.TransType.COMPLETION, R.drawable.ic_pre_auth_completion, true),
    CompletionAuto(String_id.STR_COMPLETION_AUTO, "Completion Auto", TransType.COMPLETION_AUTO, R.drawable.ic_pre_auth_completion, true),
    Refund(String_id.STR_REFUND, "Refund", EngineManager.TransType.REFUND, R.drawable.ic_refund, false),
    RefundAuto(String_id.STR_REFUND_AUTO, "RefundAuto", EngineManager.TransType.REFUND_AUTO, R.drawable.ic_refund, false),
    Reconciliation(String_id.STR_RECONCILIATION_MENU_BTN, "Reconciliation", EngineManager.TransType.RECONCILIATION, R.drawable.ic_settlement, false),
    ReconciliationAuto(String_id.STR_RECONCILIATION_MENU_BTN, "Reconciliation Auto", EngineManager.TransType.RECONCILIATION, R.drawable.ic_settlement, false),
    PreReconciliation(String_id.STR_PRE_RECONCILIATION_MENU_BTN, "Pre-Reconciliation", EngineManager.TransType.PRE_RECONCILIATION, R.drawable.ic_settlement, false),
    LastReconciliation(String_id.STR_LAST_RECONCILIATION_MENU_BTN, "Last Reconciliation", EngineManager.TransType.LAST_RECONCILIATION, R.drawable.ic_settlement, false),
    TestConnect(String_id.STR_TEST_CONNECT, "Test Connect", EngineManager.TransType.TESTCONNECT, R.drawable.ic_logon, false),
    TestAcquiringConnection(String_id.STR_TEST_ACQUIRING_CONNECTION, "Test Acquiring Connection", EngineManager.TransType.TESTCONNECT, R.drawable.ic_logon, false),
    DccGetRates(String_id.STR_DCC_RATES, "DCC Rates", TransType.DCCRATES, R.drawable.ic_system_info, false),
    RsaLogon(String_id.STR_RSA_LOGON, "RSA Logon", TransType.RSA_LOGON, R.drawable.ic_logon, false),
    Logon(String_id.STR_LOGON, "Logon", TransType.LOGON, R.drawable.ic_logon, false),
    MotoSale(String_id.STR_SALE_MOTO, "MOTO Sale", TransType.SALE_MOTO, R.drawable.ic_purchase_moto, false),
    MotoRefund(String_id.STR_REFUND_MOTO, "MOTO Refund", TransType.REFUND_MOTO, R.drawable.ic_refund_moto, false),

    // Admin Menu
    BatchUpload(String_id.STR_SUBMIT_TRANS, "Submit Transactions", TASK, new WorkflowAddActions(new SubmitTransactions(false)), R.drawable.ic_batch_upload),
    Totals(String_id.STR_TOTALS_REPORT, "Totals", TASK, new WorkflowAddActions(new ReportTotals()), R.drawable.ic_batch_review),
    History(String_id.STR_HISTORY_REPORT, "History", TASK, new WorkflowAddActions(new ReportHistory(false)), R.drawable.ic_batch_review),
    DailyBatch(String_id.STR_X_REPORT, "Daily Batch", TASK, new WorkflowAddActions(new ReportDailyBatch(false)), R.drawable.ic_settlement),
    TestCycleKeys(String_id.STR_TEST_CYCLE_KEYS, "Test Cycle Keys", EngineManager.TransType.TESTCYCLEKEYS, R.drawable.ic_logon, false),
    ClearTransactions(String_id.STR_CLEAR_TRANSACTIONS, "Clear Transactions", TASK, false, new WorkflowAddActions(new DBClear()), null,  R.drawable.ic_system_info, true, false),

    //RePrint Menu
    Reprint(String_id.STR_REPRINT_MAN, "Reprint Manager", SUBMENU, MenuReprint.class, R.drawable.ic_reprint_receipt),
    ReprintLast(String_id.STR_REPRINT_LAST, "Reprint Last", TASK, new WorkflowAddActions(new RePrintLast()), R.drawable.ic_reprint_receipt),
    ReprintNum(String_id.STR_REPRINT_NUM, "Reprint Number", TASK, new WorkflowAddActions(new RePrintByNumber()), R.drawable.ic_reprint_receipt),
    ReprintRec(String_id.STR_REPRINT_REC, "Reprint Rec", TASK, new WorkflowAddActions(new RePrintLastReconciliation()), R.drawable.ic_reprint_receipt),

    DownloadMan(String_id.STR_DOWNLOAD_MAN, "Download Manager", SUBMENU, MenuDownloadManager.class, R.drawable.ic_file_download),
    Update(String_id.STR_UPDATE, "Update", TASK, new WorkflowAddActions(new FullUpdate()), R.drawable.ic_file_download),
    ForceUpdate(String_id.STR_FORCE_UPDATE, "Force Update", TASK, new WorkflowAddActions(new com.linkly.libengine.action.MenuOperations.tms.Update()), R.drawable.ic_update),
    TMSLogon(String_id.STR_TMS_LOGON, "TMS Logon", TASK, new WorkflowAddActions(new com.linkly.libengine.action.MenuOperations.tms.Update()), R.drawable.ic_logon),
    Exit(String_id.STR_EXIT, "Exit", TASK, new WorkflowAddActions(new Exit()), R.drawable.ic_exit),
    InitiateRKI(String_id.STR_INITIATE_RKI, "Initiate RKI", TASK, new WorkflowAddActions(new com.linkly.libengine.action.MenuOperations.admin.InitiateRKI()), R.drawable.ic_sync),

    //SystemInfo Menu
    SystemInfo(String_id.STR_SYS_INFO_MENU, "System Info", SUBMENU, MenuSystemInfo.class, R.drawable.ic_system_info),
    AboutApp(String_id.STR_SUMMARY, "Summary", TASK, new WorkflowAddActions(new Information()), R.drawable.ic_system_info),
    EMVConfig(String_id.STR_EMV_CONFIG, "Emv Config", TASK, new WorkflowAddActions(new IAction[]{new CheckP2P(), new PrintEmvConfig()}), R.drawable.ic_system_info),
    CTLSConfig(String_id.STR_CTLS, "Ctls Config", TASK, new WorkflowAddActions(new IAction[]{new CheckP2P(), new PrintCtlsConfig()}), R.drawable.ic_system_info),

    UserMan(String_id.STR_USER_MAN, "User Manager", SUBMENU, MenuUsers.class, R.drawable.ic_user_manager),
    LoginUser(String_id.STR_LOGIN_TEST, "Login Test", TASK, new WorkflowAddActions(new UserLogin())),
    AddUser(String_id.STR_ADD_USER, "Add User", TASK, new WorkflowAddActions(new UserAdd()), R.drawable.ic_add_user),
    DeleteUser(String_id.STR_DELETE_USER, "Delete User", TASK, new WorkflowAddActions(new UserDelete()), R.drawable.ic_remove_user),
    ChangePassword(String_id.STR_CHANGE_PASSWORD, "Change Password", TASK, new WorkflowAddActions(new UserChangePassword()), R.drawable.ic_change_password),
    ResetPassword(String_id.STR_RESET_PASSWORD, "Reset Password", TASK, new WorkflowAddActions(new UserResetPassword()), R.drawable.ic_reset_password),
    ReportUser(String_id.STR_USER_REPORT, "User Report", TASK, new WorkflowAddActions(new UserReport()), R.drawable.ic_user_report),

    ShowDeferredAuths(String_id.STR_SHOW_DEFERRED_AUTHS, "Show Deferred Auths", TASK, new WorkflowAddActions(new DisplayDeferredAuths())/*, R.drawable.madduser*/),
    SafViewAndClear(String_id.STR_SAF_VIEW_AND_CLEAR, "View and Clear SAF", TASK, new WorkflowAddActions(new SafViewAndClear()), R.drawable.ic_pre_auth_search_and_view),
    PreAuthSearchAndView(String_id.STR_PRE_AUTH_SEARCH_AND_VIEW, "Pre-Auth Search and View", TASK, new WorkflowAddActions(new PreAuthSearchAndView()), R.drawable.ic_pre_auth_search_and_view),
    TransactionHistory(String_id.STR_TRANSACTION_HISTORY, "Transaction History", TASK, new WorkflowAddActions(new TransactionHistory()), R.drawable.ic_batch_review),

    /*Database Menu*/
    DBMan(String_id.STR_DATABASE_MAN, "Database Manager", SUBMENU, MenuDatabase.class),
    DBStats(String_id.STR_DATABASE_STATS, "Database Stats", TASK, new WorkflowAddActions(new DBStats())),
    DBList(String_id.STR_DATABASE_LIST, "Trans List", TASK, new WorkflowAddActions(new DBList())),
    DBClear(String_id.STR_DATABASE_CLEAR, "Database Clear", TASK, new WorkflowAddActions(new DBClear())),
    DBRecList(String_id.STR_RECONCILIATIONS_LIST, "Recs List", TASK, new WorkflowAddActions(new DBRecList())),

    /*Developer Menu*/
    ServiceCheck(String_id.STR_CHECK_SERVICE, "Check Service", TASK, new WorkflowAddActions(new CheckService())),
    ClearKeys(String_id.STR_CLEAR_KEYS, "Clear Keys", TASK, new WorkflowAddActions(new KeyClear())),
    TestSchedule(String_id.STR_TEST_SCHEDULE, "Test Schedule", TASK, new WorkflowAddActions(new TestSchedule())),
    TestAddTrans(String_id.STR_TEST_ADD_TRANS, "Test Add TransRec", TASK, new WorkflowAddActions(new TestAddTrans())),
    TestAutoDownload(String_id.STR_TEST_AUTO_DOWNLOAD, "Test Auto DL", TASK, new WorkflowAddActions(new TestAutoDownload())),
    TestAddTime(String_id.STR_TEST_ADD_TIME, "Test Add Time", TASK, new WorkflowAddActions(new TestAddTime())),
    IccDiags(String_id.STR_ICC_DIAG, "ICC Diags", TASK, new WorkflowAddActions(new IccDiagnostics())),
    FilePurge(String_id.STR_PURGE_FILE_SYSTEM, "Purge File System", TASK, new WorkflowAddActions(new PurgeFile())),
    KeyInject(String_id.STR_INJECT_KEY, "Inject Key E800", TASK, new WorkflowAddActions(new KeyInject())),
    ResetKeys(String_id.STR_RESET_KEYS, "Reset Keys", TASK, new WorkflowAddActions(new KeyReset())),
    ClearReversal(String_id.STR_CLEAR_REVERSALS, "Clear Reversals",TASK, new WorkflowAddActions(new ClearReversals())),
    CommsLoopTest(String_id.STR_COMMS_LOOP_TEST, "Comms Test",TASK, new WorkflowAddActions(new CommsLoopTests())),
    DukptTests(String_id.STR_DUKPT_TEST, "Dukpt Test",TASK, new WorkflowAddActions(new DukptTests())),

    //Shift Totals Menu
    ShiftTotalsMenu(String_id.STR_SHIFT_TOTALS_MENU, "Shift Totals", SUBMENU, MenuShiftTotals.class, R.drawable.ic_batch_review),
    SubTotals(String_id.STR_SUB_TOTALS, "Sub Totals", TransType.SUB_TOTALS, R.drawable.ic_batch_review, false),
    ShiftTotals(String_id.STR_SHIFT_TOTALS, "Shift Totals", TransType.SHIFT_TOTALS, R.drawable.ic_batch_review, false),
    ReprintShiftTotals(String_id.STR_REPRINT_SHIFT_TOTALS, "Reprint Shift Totals", TransType.REPRINT_SHIFT_TOTALS, R.drawable.ic_batch_review, false);

    private int value;
    private String displayText;
    private String_id displayTextId;
    private String key;

    private MenuType menuType;

    private boolean defaultTrans;
    private Workflow workflow;
    private Class<?> subMenu;
    private int iconResId;
    private boolean disabled;
    private boolean hidden;
    private EngineManager.TransType transType;


    MenuItems(String_id textId, String key, MenuType menuType, boolean isDefault, Workflow workflow, Class<?> subMenu, int iconResId, boolean disabled, boolean hidden) {
        this.value = this.ordinal();
        this.displayTextId = textId;
        this.key = key;
        this.menuType = menuType;
        this.defaultTrans = isDefault;
        this.workflow = workflow;
        this.subMenu = subMenu;
        this.iconResId = iconResId;
        this.disabled = disabled;
        this.hidden = hidden;
    }


    MenuItems(String_id textId, String key, MenuType menuType, Class<?> subMenu) {
        this(textId, key, menuType, false, new WorkflowAddActions(), subMenu, 0, false, false);
    }


    MenuItems(String_id textId, String key, MenuType menuType, Class<?> subMenu, int iconResId) {
        this(textId, key, menuType, false, new WorkflowAddActions(), subMenu, iconResId, false, false);
    }


    MenuItems(String_id textId, String key, MenuType menuType, Workflow workflow) {
        this(textId, key, menuType, false, workflow, null, 0, false, false);
    }

    MenuItems(String_id textId, String key, MenuType menuType, Workflow workflow, int iconResId) {
        this(textId, key, menuType, false, workflow, null, iconResId, false, false);
    }

    MenuItems(String_id textId, String key, EngineManager.TransType transType, int iconResId, boolean disabled) {
        this.value = this.ordinal();
        this.displayTextId = textId;
        this.key = key;
        this.menuType = TASK;
        this.defaultTrans = false;
        this.subMenu = null;
        this.iconResId = iconResId;
        this.disabled = disabled;
        this.hidden = false;
        this.transType = transType;
    }

    public String getDisplayTextId() {
        return Engine.getDep().getPrompt(this.displayTextId);
    }

    public static MenuItems getFromString(String transType) {

        MenuItems[] menuEnums = MenuItems.values();
        for (MenuItems menuEnum : menuEnums) {
            if (menuEnum.key.compareTo(transType) == 0) {
                return menuEnum;
            }
        }
        return null;
    }

    public int getValue() {
        return this.value;
    }

    public String getKey() {
        return this.key;
    }

    public MenuType getMenuType() {
        return this.menuType;
    }

    public boolean isDefaultTrans() {
        return this.defaultTrans;
    }

    public Workflow getWorkflow() {
        return this.workflow;
    }

    public Class<?> getSubMenu() {
        return this.subMenu;
    }

    public int getIconResId() {
        return this.iconResId;
    }

    public boolean isDisabled() {
        return this.disabled;
    }

    public boolean isHidden() {
        return this.hidden;
    }

    public TransType getTransType() {
        return this.transType;
    }

    public void setIconResId(int iconResId) {
        this.iconResId = iconResId;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }
}
