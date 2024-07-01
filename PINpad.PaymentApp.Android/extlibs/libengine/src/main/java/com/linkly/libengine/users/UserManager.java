package com.linkly.libengine.users;

import static com.linkly.libengine.printing.IPrintManager.PrintPreference.PRINT_PREFERENCE_DEFAULT;
import static com.linkly.libengine.printing.IPrintManager.PrintPreference.PRINT_PREFERENCE_SCREEN;
import static com.linkly.libengine.printing.IPrintManager.ReportType.USER_PASSWORD_REPORT;
import static com.linkly.libengine.printing.IPrintManager.ReportType.USER_REPORT;
import static com.linkly.libengine.status.IStatus.STATUS_EVENT.STATUS_ERR_ACCOUNT_LOCKED;
import static com.linkly.libengine.status.IStatus.STATUS_EVENT.STATUS_ERR_LOGON_FAILED;
import static com.linkly.libengine.status.IStatus.STATUS_EVENT.STATUS_ERR_PASSWORD_INVALID;
import static com.linkly.libengine.users.User.CreateHashBcryptSalt;
import static com.linkly.libengine.users.User.Privileges.FUNCTION_USER;
import static com.linkly.libengine.users.User.Privileges.MANAGER;
import static com.linkly.libengine.users.User.Privileges.MOBILE_USER;
import static com.linkly.libengine.users.User.Privileges.SUPERVISOR;
import static com.linkly.libengine.users.User.Privileges.TECHNICIAN;
import static com.linkly.libengine.users.User.Privileges.USER;
import static com.linkly.libui.IUIDisplay.String_id.STR_EMPTY;
import static com.linkly.libui.IUIDisplay.String_id.STR_PRINT;
import static com.linkly.libui.IUIDisplay.UIResultCode.ABORT;
import static com.linkly.libui.IUIDisplay.UIResultCode.OK;
import static com.linkly.libui.UIScreenDef.ADD_USER_CANCELLED;
import static com.linkly.libui.UIScreenDef.CONFIRM_CANCEL_SCREEN;
import static com.linkly.libui.UIScreenDef.DELETE_USER_SCREEN;
import static com.linkly.libui.UIScreenDef.ENTER_EXISTING_PASSWORD;
import static com.linkly.libui.UIScreenDef.ENTER_NAME;
import static com.linkly.libui.UIScreenDef.ENTER_NEW_PASSWORD;
import static com.linkly.libui.UIScreenDef.ENTER_SUPERVISOR_PASSWORD;
import static com.linkly.libui.UIScreenDef.ENTER_USER_CREDENTIALS;
import static com.linkly.libui.UIScreenDef.ENTER_USER_ID;
import static com.linkly.libui.UIScreenDef.FAILED_TO_LOGIN;
import static com.linkly.libui.UIScreenDef.ID_ALREADY_USED;
import static com.linkly.libui.UIScreenDef.INSUFFICIENT_PRIVILEGES;
import static com.linkly.libui.UIScreenDef.NEW_USER_SCREEN;
import static com.linkly.libui.UIScreenDef.PASSWORD_DOESNT_MATCH;
import static com.linkly.libui.UIScreenDef.PASSWORD_EXPIRED;
import static com.linkly.libui.UIScreenDef.PASSWORD_INCORRECT;
import static com.linkly.libui.UIScreenDef.PASSWORD_RESET;
import static com.linkly.libui.UIScreenDef.PASSWORD_UPDATED;
import static com.linkly.libui.UIScreenDef.PROCESSING_PLEASE_WAIT;
import static com.linkly.libui.UIScreenDef.REENTER_NEW_PASSWORD;
import static com.linkly.libui.UIScreenDef.RESET_FAILED;
import static com.linkly.libui.UIScreenDef.USERS_NOT_FOUND;
import static com.linkly.libui.UIScreenDef.USER_ADDED;
import static com.linkly.libui.UIScreenDef.USER_DELETED;
import static com.linkly.libui.UIScreenDef.USER_DEL_OPERATION_NOT_ALLOWED;
import static com.linkly.libui.UIScreenDef.USER_IS_LOCKED_TOO_MANY_PASSCODE_ATTEMPTS;
import static com.linkly.libui.UIScreenDef.USER_NOT_FOUND;
import static com.linkly.libui.UIScreenDef.USER_PASSWORD;
import static com.linkly.libui.display.DisplayQuestion.EButtonStyle.BTN_STYLE_PRIMARY_BORDER_DOUBLE;
import static com.linkly.libui.display.DisplayQuestion.EButtonStyle.BTN_STYLE_PRIMARY_DEFAULT_DOUBLE;

import android.content.Context;
import android.database.SQLException;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;
import androidx.room.Room;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.linkly.libconfig.ProfileCfg;
import com.linkly.libengine.config.PayCfg;
import com.linkly.libengine.dependencies.IDependency;
import com.linkly.libengine.engine.Engine;
import com.linkly.libengine.engine.reporting.UserPasswordData;
import com.linkly.libengine.engine.reporting.UserReport;
import com.linkly.libengine.engine.transactions.TransRec;
import com.linkly.libengine.engine.transactions.TransRecManager;
import com.linkly.libengine.helpers.UIHelpers;
import com.linkly.libengine.overrides.CoreOverrides;
import com.linkly.libengine.printing.IPrintManager;
import com.linkly.libengine.printing.IReceipt;
import com.linkly.libengine.workflow.WorkflowAddActions;
import com.linkly.libengine.workflow.WorkflowScheduler;
import com.linkly.libmal.IMal;
import com.linkly.libmal.MalFactory;
import com.linkly.libmal.global.config.EnvCfg;
import com.linkly.libmal.global.platform.EFTPlatform;
import com.linkly.libmal.global.printing.PrintReceipt;
import com.linkly.libmal.global.util.Util;
import com.linkly.libui.IUIDisplay;
import com.linkly.libui.IUIDisplay.String_id;
import com.linkly.libui.UIScreenDef;
import com.linkly.libui.display.DisplayQuestion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import timber.log.Timber;

public class UserManager {
    protected static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate( SupportSQLiteDatabase database) {
            Timber.i( "Migrate 1 - 2" );
            database.execSQL(
                    "ALTER TABLE users ADD COLUMN adminAccess INTEGER NOT NULL default 0"
            );
        }
    };

    protected static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate( SupportSQLiteDatabase database) {
            Timber.i( "Migrate 2 - 3" );
            database.execSQL(
                    "ALTER TABLE users ADD COLUMN failedLogonAttempts INTEGER NOT NULL default 0"
            );
            database.execSQL(
                    "ALTER TABLE users ADD COLUMN accountUnlockTimestamp BIGINT NOT NULL default 0"
            );
        }
    };

    protected static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            Timber.i("Migrate 3 - 4");
            try {
                database.execSQL(
                        "ALTER TABLE users ADD COLUMN userInitialPwdHash TEXT"
                );
                database.execSQL(
                        "UPDATE users SET userInitialPwdHash = userPwdHash"
                );
            } catch (SQLException e) {
                Timber.w(e);
                Timber.e("migrate 3 - 4 Failed");
            }
        }
    };

    protected static final Migration MIGRATION_4_5 = new Migration(4, 5) {
        @Override
        public void migrate( SupportSQLiteDatabase database) {
            Timber.i( "Migrate 4 - 5" );
            database.execSQL(
                    "ALTER TABLE users ADD COLUMN badPasscodeAttemptsTimestamps TEXT"
            );
            database.execSQL(
                    "ALTER TABLE users ADD COLUMN accountLockTimestamp BIGINT NOT NULL default 0"

            );
        }
    };

    protected static final Migration MIGRATION_5_6 = new Migration(5, 6) {
        @Override
        public void migrate( SupportSQLiteDatabase database) {
            Timber.i( "Migrate 5 - 6" );
            database.execSQL(
                    "ALTER TABLE users ADD COLUMN lastPasswordModifiedDate INTEGER"
            );
        }
    };

    private static UserManager instance = null;
    public static final Object lock = new Object();
    private static final int MAX_PASSWORD_LEN = 12;
    private static final int MIN_PASSWORD_LEN = 6;
    private static final int MIN_USER_LEN = 4;
    private static final int MAX_USER_LEN = 8;
    private static final int MAX_DEPT_NAME_LEN = 17;
    private static final int MAX_USERNAME_LEN = 8;
    private static final int MIN_USERNAME_LEN = 4;
    private IUIDisplay ui;
    private IDependency d;
    private User activeUser;
    private User upgradedUser;
    private static UserDatabase userDb;
    public static UserDao userDao;

    private static final String DEFAULT_USERID_MANAGER = "1234";
    private static final String DEFAULT_MANAGER = "123456";
    private static final String DEFAULT_USERID_SUPERVISOR = "1236";
    private static final String DEFAULT_SUPERVISOR = "123656";
    private static final String DEFAULT_USERID_AUTO_LOGIN = "1237";
    private static final String DEFAULT_AUTO_LOGIN = "123789";
    private static final String DEFAULT_USERID_TECHNICIAN = "1357";
    private static final String DEFAULT_TECHNICIAN = "024680";
    private static final String DEFAULT_USERID_FUNCTION_USER = "1238";
    private static final String DEFAULT_FUNCTION_USER = "1238";
    private static final String DEFAULT_MANAGER_USER_NAME = "Manager";
    private static final String DEFAULT_SUPERVISOR_USER_NAME = "Supervisor";
    private static final String DEFAULT_AUTOLOGIN_USER_NAME = "AutoLogin";
    private static final String DEFAULT_TECHNICIAN_USER_NAME = "Technician";
    private static final String DEFAULT_FUNCTION_USER_USER_NAME = "FunctionUser";

    private static final int DEFAULT_PASSWORD_MAXIMUM_AGE = 30; //days after which password should be reset.

    // configure list of default users
    private static final List<User> defaultUsers = Arrays.asList(
            new User(DEFAULT_MANAGER_USER_NAME,DEFAULT_USERID_MANAGER, DEFAULT_MANAGER, MANAGER),
            new User(DEFAULT_SUPERVISOR_USER_NAME, DEFAULT_USERID_SUPERVISOR, DEFAULT_SUPERVISOR, SUPERVISOR),
            new User(DEFAULT_TECHNICIAN_USER_NAME,DEFAULT_USERID_TECHNICIAN, DEFAULT_TECHNICIAN, TECHNICIAN),
            new User(DEFAULT_AUTOLOGIN_USER_NAME,DEFAULT_USERID_AUTO_LOGIN, DEFAULT_AUTO_LOGIN, USER)
    );

    public void init(IDependency d, Context context ) {
        this.d = d;
        ui = d.getUI();

        PayCfg cfg = null;
        if( null != d.getConfig() ) {
            cfg = d.getPayCfg();
        }

        // load/create new database
        userDb = Room.databaseBuilder(
                        context,
                UserDatabase.class,
                "Users.db" )
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6)
                .allowMainThreadQueries()
                .build();
        userDao = userDb.userDao();

        if( null != cfg ) {
            // if manager user specified in config file, add it to database if not already found, with initial pwd from config too
            if (!Util.isNullOrEmpty(cfg.getLoginManagerUserId())) {
                User foundUser = userDao.findByUserId(cfg.getLoginManagerUserId());
                if (foundUser == null) {
                    // add user to database
                    userDao.insert(new User(cfg.getLoginManagerUserName(),
                            cfg.getLoginManagerUserId(),
                            cfg.getLoginManagerInitialPwd(), MANAGER));
                } else {
                    // check if user initial password has been modified in the config,
                    // if so, update the initial password and the password, then force the user to change the password
                    if (!foundUser.verifyInitialPassword(d, cfg.getLoginManagerInitialPwd())) {
                        foundUser.setInitialPassword(cfg.getLoginManagerInitialPwd());
                        foundUser.setPassword(cfg.getLoginManagerInitialPwd());
                        userDao.update(foundUser);

                        logoutActiveUser();
                    }
                }
            }
            // if technician user specified in config file, add it to database if not already found, with initial pwd from config too
            if (!Util.isNullOrEmpty(cfg.getLoginTechnicianUserId())) {
                User foundUser = userDao.findByUserId(cfg.getLoginTechnicianUserId());
                if (foundUser == null) {
                    // add user to database
                    userDao.insert(new User(cfg.getLoginTechnicianUserName(),
                            cfg.getLoginTechnicianUserId(),
                            cfg.getLoginTechnicianInitialPwd(), TECHNICIAN));
                } else {
                    // check if user initial password has been modified in the config,
                    // if so, update the initial password and the password, then force the user to change the password
                    if (!foundUser.verifyInitialPassword(d, cfg.getLoginTechnicianInitialPwd())) {
                        foundUser.setInitialPassword(cfg.getLoginTechnicianInitialPwd());
                        foundUser.setPassword(cfg.getLoginTechnicianInitialPwd());
                        userDao.update(foundUser);

                        logoutActiveUser();
                    }
                }
            }
        }

        // Add account for storing bad attempts to perform password protected functions
        User functionUser = userDao.findByUserId(DEFAULT_USERID_FUNCTION_USER);
        if (functionUser == null) {
            // add user to database
            userDao.insert(new User(DEFAULT_FUNCTION_USER_USER_NAME,
                    DEFAULT_USERID_FUNCTION_USER,
                    DEFAULT_FUNCTION_USER, FUNCTION_USER));
        }

        if (d.getCustomer().supportDefaultUsers()) {
            // set up users
            for( User defaultUser : defaultUsers ) {
                User foundUser = userDao.findByUserId( defaultUser.getUserId() );
                if( foundUser == null ) {
                    // add user to database
                    userDao.insert( defaultUser );
                }
            }
        }
    }

    // normal constructor - should be used first
    public static UserManager getInstance() {
        /* kioskLocked as Init can take a while searching DB and setting it up */
        /* And it can definitely happen from 2 threads */
        synchronized (lock) {
            if (instance == null) {
                instance = new UserManager();
            }
        }
        return instance;
    }

    private String GetInputHelper(UIScreenDef screenDef ) {
        ui.showInputScreen(screenDef, null );
        IUIDisplay.UIResultCode uiResult = ui.getResultCode(screenDef.id, IUIDisplay.LONG_TIMEOUT);

        if( uiResult != OK )
            return null;

        return ui.getResultText(screenDef.id, IUIDisplay.uiResultText1);
    }

    public IUIDisplay getUi() {
        return this.ui;
    }

    public IDependency getD() {
        return this.d;
    }

    public User getUpgradedUser() {
        return this.upgradedUser;
    }

    public void setUi(IUIDisplay ui) {
        this.ui = ui;
    }

    public void setD(IDependency d) {
        this.d = d;
    }

    public void setUpgradedUser(User upgradedUser) {
        this.upgradedUser = upgradedUser;
    }

    private enum YesNoResult { YES, NO, CANCEL };

    private YesNoResult GetInputYesNoScreen(UIScreenDef screenDef) {
        HashMap<String, Object> hashMap = new HashMap<>();

        ArrayList<DisplayQuestion> options = new ArrayList<>();
        options.add(new DisplayQuestion(String_id.STR_YES, "OP0", BTN_STYLE_PRIMARY_DEFAULT_DOUBLE));
        options.add(new DisplayQuestion(String_id.STR_NO, "OP1", BTN_STYLE_PRIMARY_BORDER_DOUBLE));
        hashMap.put(IUIDisplay.uiScreenOptionList, options);
        ui.showScreen(screenDef, hashMap);

        // if result not success, then either cancel or timeout
        if (ui.getResultCode(IUIDisplay.ACTIVITY_ID.ACT_QUESTION, IUIDisplay.LONG_TIMEOUT) != OK)
            return YesNoResult.CANCEL;

        // look at result text to determine if yes/no
        String result = ui.getResultText(IUIDisplay.ACTIVITY_ID.ACT_QUESTION, IUIDisplay.uiResultText1);
        if (result.equals("OP0")) {
            // yes selected
            return YesNoResult.YES;
        } else if (result.equals("OP1")) {
            // no selected
            return YesNoResult.NO;
        }

        // unexpected result, treat as cancel
        return YesNoResult.CANCEL;
    }

    private String GetInputHelper_ConfirmCancel(UIScreenDef screenDef ) {
        IUIDisplay.UIResultCode uiResult = IUIDisplay.UIResultCode.UNKNOWN;
        boolean done = false;

        while ( !done ) {
            ui.showInputScreen(screenDef, null );
            uiResult = ui.getResultCode(screenDef.id, IUIDisplay.LONG_TIMEOUT);
            if ( uiResult == ABORT ) {
                // Confirm cancel; if confirmed, set done; else, loop around and get input again
                YesNoResult result = GetInputYesNoScreen(CONFIRM_CANCEL_SCREEN);
                if ( result == YesNoResult.YES )
                    done = true;
            }
            else {
                done = true;
            }
        }

        if ( uiResult != OK )
            return null;

        return ui.getResultText(screenDef.id, IUIDisplay.uiResultText1);
    }

    public static User upgradeUserLevel(User.Privileges level, IUIDisplay ui) {
        getInstance().upgradedUser = null;

        User user = UserManager.getInstance().userLoginUpgrade();
        while(user != null) {

            if ( level.ordinal() > user.privileges.ordinal()) {
                // incorrect password
                ui.showScreen(INSUFFICIENT_PRIVILEGES);
            } else {
                break;
            }
            user = UserManager.getInstance().userLoginUpgrade();
        }
        if (user != null) {
            getInstance().setUpgradedUser(user);
        }
        return getInstance().upgradedUser;
    }

    @SuppressWarnings("static")
    public static User undoUpgradeUserLevel() {
        getInstance().upgradedUser = null;
        return getInstance().getActiveUser();
    }

    public static String getAutoLoginUsername(IDependency d ) {
        if (d.getPayCfg().isAutoUserLogin())
            return CoreOverrides.get().getAutoUserLoginUserName();

        return EnvCfg.getInstance().readValue("userID");
    }

    public static String getAutoLoginDepartmentID( IDependency d ) {
        if (d.getPayCfg().isAutoUserLogin())
            return CoreOverrides.get().getAutoUserLoginDepartment();

        return EnvCfg.getInstance().readValue("deprtID");
    }

    public static boolean isAutoUserLogin( IDependency d, Context context ) {

        boolean autoLogin = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("autologin", true);
        if (autoLogin) {
            return true;
        }
        return d.getPayCfg().isAutoUserLogin();
    }

    public static void userLogin( IDependency d, Context context ) {

        // if user already logged in, return early
        if( getActiveUser() != null )
            return;

        if (d.getPayCfg() != null) {
            if (isAutoUserLogin(d, context)) {
                // perform auto login
                String autoUser = getAutoLoginUsername(d);
                String autoDepartment = getAutoLoginDepartmentID(d);

                if (autoUser != null && !autoUser.isEmpty()) {
                    ExecutorService executor = Executors.newSingleThreadExecutor();

                    Future<User> getUser = executor.submit(() -> userDao.findByUserId(autoUser));

                    User user = null;
                    try {
                        user = getUser.get();
                    } catch(InterruptedException e) {
                        Thread.currentThread().interrupt();
                        Timber.w(e);
                    } catch (Exception e) {
                        Timber.w(e);
                    } finally {
                        executor.shutdown();
                    }

                    if (user != null) {
                        if (autoDepartment != null && !autoDepartment.isEmpty())
                            user.setDepartmentId(autoDepartment);

                        UserManager.getInstance().setActiveUser(user);
                    }
                }
            }
        }

        if( getActiveUser() == null ) {
            // still no user logged in after the above, go to the login UI screen
            WorkflowScheduler.getInstance().queueWorkflow(new WorkflowAddActions(new com.linkly.libengine.action.MenuOperations.users.UserLogin()), false);
        }
    }

    static public void logoutActiveUser() {
        UserManager.getInstance().setActiveUser(null);
        EnvCfg.getInstance().storeValue("userID", "");
        EnvCfg.getInstance().storeValue("deprtID", "");
        Engine.getDep().getAppCallbacks().onLogout();
    }

    static public User getActiveUser() {

        return UserManager.getInstance().activeUser;
    }

    public void setActiveUser(User user) {
        activeUser = user;
    }

    static boolean updateDepartment(IDependency d, User man, String departmentId) {
        return true;
    }

    static boolean autoLoggedIn = false;
    static public boolean autoLoginUser(IDependency d, String userId, String pwd, String departmentId, Context context) {
        if (getActiveUser() == null) {
            if (userId != null && !userId.isEmpty()) {

                if (pwd == null || pwd.isEmpty() || departmentId == null || departmentId.isEmpty()) {
                    return false;
                }

                User man = userDao.findByUserId(userId);
                if (man != null) {
                    if (man.verifyPassword(d, pwd)) {
                        if (!updateDepartment(d, man, departmentId))
                            return false;
                    } else {
                        return false;
                    }

                    UserManager.getInstance().setActiveUser(man);
                    autoLoggedIn = true;
                    return true;
                }
                return false;

            }
            else if (ProfileCfg.getInstance().isDemo()) {
                d.getPayCfg().setAutoUserLogin(true);
                userLogin(d, context);
                autoLoggedIn = true;
                return true;
            }
            else if (userDao.findByUserId(DEFAULT_USERID_SUPERVISOR) != null) {
                User man = userDao.findByUserId(DEFAULT_USERID_SUPERVISOR);
                UserManager.getInstance().setActiveUser(man);
                autoLoggedIn = true;
                return true;
            }


        }
        return true;
    }

    static public boolean undoAutoLoginUser(IDependency d) {
        User user = getActiveUser();
        if (user != null) {

            if (ProfileCfg.getInstance().isDemo()) {
                d.getPayCfg().setAutoUserLogin(false);
                UserManager.getInstance().setActiveUser(null);
            }
            else if (user.getUserId().equals(DEFAULT_USERID_SUPERVISOR)) {
                UserManager.getInstance().setActiveUser(null);
            }
            else if (autoLoggedIn){
                UserManager.getInstance().setActiveUser(null);
            }

        }
        autoLoggedIn = false;
        return true;
    }

    private boolean isDefaultUserAndPwd(User user) {

        if( defaultUsers == null )
            return false;

        for( User defaultUser : defaultUsers ) {
            // if matches a default user id AND password, return true
            if (user.getUserId().compareTo(defaultUser.getUserId()) == 0 &&
                    user.getUserPwdHash().compareTo( defaultUser.getUserPwdHash() ) == 0)
                return true;
        }

        return false;
    }

    private boolean isManagerFirstLogin(User user) {

        PayCfg cfg = null;
        if( null != d.getConfig() ) {
            cfg = d.getPayCfg();
        }

        // if login user id credentials not set/incomplete, then return false here
        if(  null == cfg ||
            Util.isNullOrEmpty(cfg.getLoginManagerInitialPwd()) ||
            Util.isNullOrEmpty(cfg.getLoginManagerUserId()) ) {
            return false;
        }

        // if login manager user id is valid AND entered user ID + pwd match the login manager details, return true
        if ( user.getUserId().compareTo(cfg.getLoginManagerUserId()) == 0 &&
                user.getUserPwdHash().compareTo( CreateHashBcryptSalt(cfg.getLoginManagerInitialPwd()) ) == 0) {
            return true;
        }

        return false;
    }

    private boolean isTechnicianFirstLogin(User user) {

        PayCfg cfg = null;
        if( null != d.getConfig() ) {
            cfg = d.getPayCfg();
        }

        // if login user id credentials not set/incomplete, then return false here
        if(  null == cfg ||
                Util.isNullOrEmpty(cfg.getLoginTechnicianInitialPwd()) ||
                Util.isNullOrEmpty(cfg.getLoginTechnicianUserId()) ) {
            return false;
        }

        // if login technician user id is valid AND entered user ID + pwd match the login technician details, return true
        if ( user.getUserId().compareTo(cfg.getLoginTechnicianUserId()) == 0 &&
                user.getUserPwdHash().compareTo( CreateHashBcryptSalt(cfg.getLoginTechnicianInitialPwd()) ) == 0) {
            return true;
        }

        return false;
    }

    private boolean isUserFirstLogin(User user)
    {
        if(user.getUserPwdHash().compareTo(user.userInitialPwdHash) == 0)
        {
            return true;
        }
        return false;
    }

    private  boolean isUserPasswordExpired(User user)
    {
        Calendar cal = Calendar.getInstance();
        Date today = cal.getTime();
        int passwordMaximumAge = DEFAULT_PASSWORD_MAXIMUM_AGE;
        long lastPasswordModifiedDate = 0;
        User foundUser = userDao.findByUserId(user.getUserId());
        if(foundUser != null)
            lastPasswordModifiedDate = foundUser.getLastPasswordModifiedTimestamp();
        String configpasswordMaximumAge = d.getPayCfg().getPasswordMaximumAge();
        try {
            passwordMaximumAge = Integer.parseInt(configpasswordMaximumAge);
        } catch (NumberFormatException e) {
            Timber.w(e);
        }
        cal.setTimeInMillis(lastPasswordModifiedDate);
        cal.add(Calendar.DAY_OF_MONTH,  passwordMaximumAge);
        Date passwordExpiryTime = cal.getTime();
        if(today.after(passwordExpiryTime))
            return true;// password expired
        else
            return false;// password valid
    }
    private String getDefaultUserPwd(User user) {
        if( defaultUsers == null )
            return null;

        for( User defaultUser : defaultUsers ) {
            if( user.getUserId().compareTo(defaultUser.getUserId()) == 0 ) {
                return defaultUser.getUserPwdHash();
            }
        }

        return null;
    }

    private boolean isActiveUserSupervisorOrManager() {
        return getActiveUser().getPrivileges() == SUPERVISOR || getActiveUser().getPrivileges() == MANAGER;
    }

    public boolean addUser(IMal mal) {
        if ( !isActiveUserSupervisorOrManager() ) {
            ui.showScreen(INSUFFICIENT_PRIVILEGES);
            return false;
        }

        // get user id
        String userId = GetInputHelper_ConfirmCancel( ENTER_USER_ID );

        // user cancel or timeout
        if (userId == null)
            return false;

        // is the user id already taken?
        if (userDao.findByUserId(userId) != null) {
            // yup already taken
            ui.showScreen(ID_ALREADY_USED);
            return false;
        }

        // get user name
        String userName = GetInputHelper_ConfirmCancel( ENTER_NAME );
        if (userName == null)
            return false;

        // user permissions level
        YesNoResult result = GetInputYesNoScreen(NEW_USER_SCREEN);
        if( result == YesNoResult.CANCEL )
            return false;

        User.Privileges userPrivileges;
        if( result == YesNoResult.YES ) {
            userPrivileges = SUPERVISOR;
        } else {
            if (EFTPlatform.isPaxTerminal()) {
                userPrivileges = USER;
            } else {
                userPrivileges = MOBILE_USER;
            }
        }

        // get users pwd
        String userPwd = generateRandomPasswordforUser();
        UIHelpers.YNQuestion result1 = UIHelpers.uiYesNoCancelQuestion(d,USER_PASSWORD,STR_PRINT,userId,userPwd);
        if( result1 == UIHelpers.YNQuestion.YES ) {
            UserPasswordData report = new UserPasswordData(userId, userName, userPwd);
            ui.showScreen(PROCESSING_PLEASE_WAIT);
            IReceipt pwdReport = Engine.getPrintManager().getReceiptForReport(d, USER_PASSWORD_REPORT, mal);
            PrintReceipt receiptToPrint = pwdReport.generateReceipt(report);
            receiptToPrint.setIconFinished(PrintReceipt.SCREEN_ICON.PR_SUCCESS_ICON);
            receiptToPrint.setIconWhilePrinting(PrintReceipt.SCREEN_ICON.PR_SUCCESS_ICON);
            IPrintManager.PrintPreference printPreference = MalFactory.getInstance().getHardware().hasPrinter() ? PRINT_PREFERENCE_DEFAULT : PRINT_PREFERENCE_SCREEN;
            Engine.getPrintManager().printReceipt(d, receiptToPrint, null, false, STR_EMPTY, printPreference, mal);
        } else if(result1 == UIHelpers.YNQuestion.CANCEL){
            ui.showScreen(ADD_USER_CANCELLED);
            return false;
        } else if(result1 == UIHelpers.YNQuestion.NO) {
            ui.showScreen(USER_ADDED);
        }

        // save user record
        User newUser = new User( userName, userId, userPwd,userPwd, userPrivileges, userPrivileges == SUPERVISOR );

        userDao.insert( newUser );
        ui.showScreen(USER_ADDED);

        return true;
    }

    public boolean deleteUser() {
        if ( !isActiveUserSupervisorOrManager() ) {
            ui.showScreen(INSUFFICIENT_PRIVILEGES);
            return false;
        }

        // get user id
        String userId = GetInputHelper(ENTER_USER_ID );
        if (userId == null)
            return false;

        User user = userDao.findByUserId(userId);
        if (user == null) {
            // user not found
            ui.showScreen(USER_NOT_FOUND);
            return false;
        }

        if ( getDefaultUserPwd(user) != null ) {
            // can't delete as default user
            ui.showScreen(USER_DEL_OPERATION_NOT_ALLOWED);
            return false;
        }

        YesNoResult result = GetInputYesNoScreen(DELETE_USER_SCREEN);
        if( result == YesNoResult.CANCEL )
            return false;

        if( result == YesNoResult.YES ) {
            userDao.delete(user);
            // try delete
            ui.showScreen(USER_DELETED);
            return true;
        }

        return false;
    }

    private class LoginUiHelper {

        String userId;
        String userPwd;
        boolean saveCredentials;
        IUIDisplay.UIResultCode uiResultCode;

        LoginUiHelper(UIScreenDef screenDef, boolean upgradedUser) {
            HashMap<String, Object> map = new HashMap<>();

            map.put(IUIDisplay.uiUserUpgrade, upgradedUser);
            map.put(IUIDisplay.uiPasswordMinLen, MIN_PASSWORD_LEN);
            map.put(IUIDisplay.uiPasswordMaxLen, MAX_PASSWORD_LEN);
            map.put(IUIDisplay.uiScreenMinLen, MIN_USERNAME_LEN);
            map.put(IUIDisplay.uiScreenMaxLen, MAX_USERNAME_LEN);

            ui.showInputScreen(screenDef, map);

            uiResultCode = ui.getResultCode(IUIDisplay.ACTIVITY_ID.ACT_USER_LOGIN, IUIDisplay.NO_TIMEOUT);
            if( uiResultCode == OK ) {
                userId = ui.getResultText(IUIDisplay.ACTIVITY_ID.ACT_USER_LOGIN, IUIDisplay.uiResultText1);
                userPwd = ui.getResultText(IUIDisplay.ACTIVITY_ID.ACT_USER_LOGIN, IUIDisplay.uiResultText2);
                saveCredentials = ui.getSelectChoice(IUIDisplay.ACTIVITY_ID.ACT_USER_LOGIN, IUIDisplay.uiSelectChoice);
            }
        }
    }

    private User attemptLogin( LoginUiHelper loginUi ) {
        Timber.d("attemptLogin...");
        // search for user id in db
        User userObj = userDao.findByUserId(loginUi.userId);
        if( userObj == null || userObj.privileges == FUNCTION_USER) {
            d.getStatusReporter().reportStatusEvent(STATUS_ERR_LOGON_FAILED , isSuppressPosDialog());
            ui.showScreen(FAILED_TO_LOGIN);
            return null;
        }

        // found the user, check account not locked or if time is to unlock it
        long timeToUnlock = userObj.timeToUnlockAccount(d);
        if ( timeToUnlock > 0 ) {
            // account locked
            d.getStatusReporter().reportStatusEvent(STATUS_ERR_ACCOUNT_LOCKED, isSuppressPosDialog());
            ui.showScreen(USER_IS_LOCKED_TOO_MANY_PASSCODE_ATTEMPTS, timeMillisToMinSec(timeToUnlock));
            return null;
        }

        // found the user, now validate the password
        if ( !userObj.verifyPassword(d, loginUi.userPwd) ) {
            // incorrect password
            d.getStatusReporter().reportStatusEvent(STATUS_ERR_PASSWORD_INVALID , isSuppressPosDialog());
            ui.showScreen(PASSWORD_INCORRECT);
            return null;
        }

        // if user is a default user, and using the default password for that user, OR a TMS configured manager login for the first time, then force them to change
        if (isDefaultUserAndPwd(userObj) || isManagerFirstLogin(userObj) || isUserFirstLogin(userObj) || isTechnicianFirstLogin(userObj)) {
            // force change pwd
            if (!changeUserPassword(userObj))
                return null;
        }

        if(isUserPasswordExpired(userObj)) {
            //force reset pwd
            ui.showScreen(PASSWORD_EXPIRED);
            if(!changeUserPassword(userObj))
                return null;
        }

        // check if the 'save credentials' tick box is enabled
        if( loginUi.saveCredentials ) {
            // yes it is, record these user credentials
            EnvCfg.getInstance().storeValue("userID", loginUi.userId);
            EnvCfg.getInstance().storeValue("deprtID", userObj.getDepartmentId());
        } else {
            EnvCfg.getInstance().storeValue("userID", "");
            EnvCfg.getInstance().storeValue("deprtID", "");
        }

        Engine.getDep().getAppCallbacks().onLogin();

        return userObj;
    }

    private boolean isSuppressPosDialog() {
        return (d.getCurrentTransaction() != null) && d.getCurrentTransaction().isSuppressPosDialog();
    }

    public User userLoginUpgrade(){
        LoginUiHelper loginUi = new LoginUiHelper(  ENTER_SUPERVISOR_PASSWORD, true );
        if( loginUi.uiResultCode != OK ) {
            // return error
            return null;
        }

        return attemptLogin( loginUi);
    }

    public User userLoginRegular() {
        while (true) {
            // get user id and pwd on one screen
            LoginUiHelper loginUi = new LoginUiHelper( ENTER_USER_CREDENTIALS, false );

            // handle error cases/cancel first
            if( loginUi.uiResultCode == ABORT ) {
                return null;
            } else if( loginUi.uiResultCode != OK ) {
                // go to next attempt
                continue;
            }

            User userObj = attemptLogin( loginUi );
            if( userObj != null ) {
                return userObj;
            }

            if (d.getStatusReporter().getLastStatus() == STATUS_ERR_ACCOUNT_LOCKED) {
                // Account is locked, do not continue
                return null;
            }
        }
    }

    public boolean changeUserPassword(User userToUpdate) {

        if (userToUpdate == null) {

            // get user id
            userToUpdate = getActiveUser();

            // enter existing password
            String pwd = GetInputHelper( ENTER_EXISTING_PASSWORD );

            // if user cancel or timeout, exit out without displaying error
            if ( pwd == null )
                return false;

            if ( !userToUpdate.verifyPassword(d, pwd) ) {
                // incorrect pwd
                ui.showScreen(PASSWORD_INCORRECT);
                return false;
            }
        }


        // enter new pwd
        String pwdNew = GetInputHelper( ENTER_NEW_PASSWORD );
        if (pwdNew == null)
            return false;

        //  re-enter new password
        String pwdVerify = GetInputHelper( REENTER_NEW_PASSWORD );
        if (pwdVerify == null)
            return false;

        if (pwdVerify.compareTo(pwdNew) != 0) {
            ui.showScreen(PASSWORD_DOESNT_MATCH);
            return false;
        }

        userToUpdate.setPassword(pwdNew);
        userToUpdate.setLastPasswordModifiedTimestamp(new Date().getTime());
        // save record with new pwd set
        userDao.update( userToUpdate );
        ui.showScreen(PASSWORD_UPDATED);

        // user updated okay
        return true;
    }

    public boolean resetUserPassword(IMal mal) {

        // get user id
        String userId = GetInputHelper( ENTER_USER_ID );
        if( userId == null )
            return false;

        User userToUpdate = userDao.findByUserId(userId);
        if (userToUpdate == null) {
            ui.showScreen(USER_NOT_FOUND);
            return false;
        }

        if ( !isActiveUserSupervisorOrManager() ) {
            if( !userId.equals( getActiveUser().userId )) {
                ui.showScreen(INSUFFICIENT_PRIVILEGES);
                return false;
            }
        }
        // check if is one of the 'default' users
        String hashedPwd = getDefaultUserPwd(userToUpdate);
        if (hashedPwd == null) {
            // error - not one of the default users, generate random password for user and print
            Timber.e("Not a default user");
            hashedPwd = generateRandomPasswordforUser();

            UIHelpers.YNQuestion result = UIHelpers.uiYesNoCancelQuestion(d,USER_PASSWORD,STR_PRINT,userId,hashedPwd);
            if( result == UIHelpers.YNQuestion.YES ) {
                UserPasswordData report = new UserPasswordData(userId, userToUpdate.userName, hashedPwd);
                ui.showScreen(PROCESSING_PLEASE_WAIT);
                IReceipt pwdReport = Engine.getPrintManager().getReceiptForReport(d, USER_PASSWORD_REPORT, mal);
                PrintReceipt receiptToPrint = pwdReport.generateReceipt(report);
                receiptToPrint.setIconFinished(PrintReceipt.SCREEN_ICON.PR_SUCCESS_ICON);
                receiptToPrint.setIconWhilePrinting(PrintReceipt.SCREEN_ICON.PR_SUCCESS_ICON);
                IPrintManager.PrintPreference printPreference = MalFactory.getInstance().getHardware().hasPrinter() ? PRINT_PREFERENCE_DEFAULT : PRINT_PREFERENCE_SCREEN;
                Engine.getPrintManager().printReceipt(d, receiptToPrint, null, false, STR_EMPTY, printPreference, mal);
            } else if(result == UIHelpers.YNQuestion.CANCEL){
                ui.showScreen(RESET_FAILED);
                return false;
            } else if(result == UIHelpers.YNQuestion.NO) {
                ui.showScreen(PASSWORD_RESET);
            }

            userToUpdate.setPassword(hashedPwd);
            userToUpdate.setInitialPassword(hashedPwd);
        }
        else {
            // reset password to default
            userToUpdate.userPwdHash = hashedPwd;
        }
        userToUpdate.setLastPasswordModifiedTimestamp(new Date().getTime());
        userDao.update( userToUpdate );

        ui.showScreen(PASSWORD_RESET);

        return true;
    }

    private String generateRandomPasswordforUser() {
        // It will generate 6 digit random Number.
        // from 0 to 999999
        Random rnd = new Random();
        int passcode = rnd.nextInt(999999);

        // this will convert any number sequence into 6 character.
        return String.format("%06d", passcode);
    }

    public void userReport(IMal mal) {
        if ( !isActiveUserSupervisorOrManager() ) {
            ui.showScreen(INSUFFICIENT_PRIVILEGES);
            return;
        }

        List<User> userList = userDao.getAll();
        Iterator<User> userToremove = userList.iterator();
        while (userToremove.hasNext()) {
            if (userToremove.next().userId.equals(DEFAULT_USERID_AUTO_LOGIN)) {
                userToremove.remove();
            }
        }

        if (userList != null) {
            UserReport report = new UserReport();
            Timber.i( "=========== User Report ===========");
            ui.showScreen(PROCESSING_PLEASE_WAIT);

            for( User user : userList ) {
                long amount = 0;
                int count = (int)TransRecManager.getInstance().getTransRecDao().countTransByUser(user.getUserId());

                if (count > 0) {
                    List<TransRec> userTrans = TransRecManager.getInstance().getTransRecDao().findByUser( user.getUserId() );
                    if( userTrans != null && userTrans.size() > 0 ) {
                        for (TransRec tran : userTrans) {
                            amount += tran.getAmounts().getTotalAmount();
                        }
                    }
                }

                report.getUserDataItems().add(new UserReport().new UserData(user.getUserId(), user.getUserName(), "" + count, amount));
            }

            // print report
            IReceipt cashReport = Engine.getPrintManager().getReceiptForReport(d, USER_REPORT, mal);
            PrintReceipt receiptToPrint = cashReport.generateReceipt(report);
            receiptToPrint.setIconFinished(PrintReceipt.SCREEN_ICON.PR_SUCCESS_ICON);
            receiptToPrint.setIconWhilePrinting(PrintReceipt.SCREEN_ICON.PR_SUCCESS_ICON);
            IPrintManager.PrintPreference printPreference = MalFactory.getInstance().getHardware().hasPrinter() ? PRINT_PREFERENCE_DEFAULT : PRINT_PREFERENCE_SCREEN;
            Engine.getPrintManager().printReceipt(d, receiptToPrint, null, false, STR_EMPTY, printPreference, mal);
        } else {
            ui.showScreen(USERS_NOT_FOUND);
        }
    }

    public boolean isFunctionUserLocked() {
        // search for user id in db
        User userObj = userDao.findByUserId(DEFAULT_USERID_FUNCTION_USER);
        if( userObj == null || userObj.privileges != FUNCTION_USER) {
            return true;
        }
        // found the user, check account not locked or if time is to unlock it
        return userObj.timeToUnlockAccount(d) > 0;
    }

    public void displayFunctionUserLocked() {
        User userObj = userDao.findByUserId(DEFAULT_USERID_FUNCTION_USER);
        if( userObj == null || userObj.privileges != FUNCTION_USER) {
            return;
        }

        long timeToUnlockMS = userObj.timeToUnlockAccount(d);
        if ( timeToUnlockMS > 0 ) {
            // account locked
            String freeFormText = d.getStatusReporter().convertToFreeForm("MAX RETRY ATTEMPTS", String.format("TRY AGAIN IN %d MIN", getMillisecondsToNearestMinute(timeToUnlockMS)));
            d.getStatusReporter().reportStatusEvent(STATUS_ERR_ACCOUNT_LOCKED, freeFormText , isSuppressPosDialog());
            ui.showScreen(USER_IS_LOCKED_TOO_MANY_PASSCODE_ATTEMPTS, timeMillisToMinSec(timeToUnlockMS));
        }
    }

    public void functionUserLoginSuccess() {
        User userObj = getFunctionUser();
        if( userObj != null )
            userObj.unlockClearBadPasscodeAttempts(d);
    }

    public void functionUserLoginFail() {
        User userObj = getFunctionUser();
        if (userObj != null)
            userObj.processBadPasscodeAttempt(d);
    }

    private User getFunctionUser() {
        User user = userDao.findByUserId(DEFAULT_USERID_FUNCTION_USER);
        if( user == null || user.privileges != FUNCTION_USER) {
            return null;
        }
        return user;
    }

    /**
     * returns to nearest minute rounding up if greater than 30 else down.
     * eg if 29 mins and 30 secs will round up to 30 mins if 29 mins and 15 seconds will be 29 Mins
     * @param timeMs time in Millies
     * @return rounded value
     */
    long getMillisecondsToNearestMinute(long timeMs) {
        return (timeMs + (30*1000-1)) / (60 * 1000);
    }

    private String timeMillisToMinSec(long timeMillis) {
        String retryString = "";

        if (timeMillis != Long.MAX_VALUE) {
            retryString = "Please Try Again\nin ";
            if (timeMillis >= 60 * 1000) {
                retryString += getMillisecondsToNearestMinute(timeMillis) + " "; // minutes, rounded to nearest
                if (timeMillis / (60 * 1000) == 1)
                    retryString += "minute";
                else
                    retryString += "minutes";
            } else {
                retryString += Long.toString(timeMillis / 1000) + " "; // seconds
                if (timeMillis / 1000 == 1)
                    retryString += "second";
                else
                    retryString += "seconds";
            }
        }
        return retryString;
    }

    public enum TaskType {
        ADD,
        DELETE,
        CHANGE_PASSWORD,
        RESET_PASSWORD,
        USER_REPORT,
        LOGIN,
        UPGRADE_USER,
    }
}
