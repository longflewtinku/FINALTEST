package com.linkly.libengine.users;

import static com.linkly.libengine.users.UserManager.userDao;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverter;

import com.linkly.libengine.dependencies.IDependency;
import com.linkly.libmal.global.util.Util;

import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;

import at.favre.lib.crypto.bcrypt.BCrypt;
import timber.log.Timber;

@Entity(tableName = "users")
public class User {

    private static final int DEFAULT_RETRY_LIMIT = 6;
    private static final int MAX_RETRY_LIMIT = 20;
    private static final int DEFAULT_PASSWORD_ATTEMPT_WINDOW_MS = 30 * 60 * 1000;
    private static final int DEFAULT_PASSWORD_ATTEMPT_LOCKOUT_DURATION_MS = 30 * 60 * 1000;
    private static final int MAX_PASSWORD_ATTEMPT_LOCKOUT_DURATION_MS = 24 * 60 * 60 * 1000;
    private static final int MAX_BAD_ATTEMPTS_TIMESTAMPS_STORED = 100;

    @PrimaryKey(autoGenerate = true)
    public int uid;

    public String userId;           // user id (numeric)
    public String userInitialPwdHash;
    public String userPwdHash;
    public String userName;         // name for display
    public Privileges privileges;

    @Ignore
    private String departmentId; //This is Optomany Specific and Selected At runtime not stored in user DB

    @Ignore
    private String terminalId; //This is Optomany Specific and Selected At runtime not stored in user DB
    public boolean adminAccess;

    private int failedLogonAttempts;    // not used
    private long accountUnlockTimestamp;   // not used
    private LinkedList<Long> badPasscodeAttemptsTimestamps;
    private long accountLockTimestamp;   // time when account was locked. 0 if account is not locked

    private long lastPasswordModifiedTimestamp;

    private static  final byte[] salt = { (byte)0x0E, (byte)0x5D, (byte)0x65, (byte)0x59, (byte)0x4C, (byte)0xA2, (byte)0x59, (byte)0x51, (byte)0x36, (byte)0x44, (byte)0x15, (byte)0xF0, (byte)0xF6, (byte)0x50, (byte)0x4C, (byte)0xA1 };


    public User(){}

    public User(String userName, String userId, String password, Privileges privileges) {
        this( userName, userId, password, privileges, true );
    }

    public User(
            String userName,
            String userId,
            String password,
            Privileges privileges,
            boolean adminAccess) {
        this(userName, userId, password, password, privileges, adminAccess);
    }

    public User(
            String userName,
            String userId,
            String userInitialPassword,
            String password,
            Privileges privileges,
            boolean adminAccess){
        this.userName = userName;
        this.userId = userId;
        this.userInitialPwdHash = CreateHashBcryptSalt(userInitialPassword);
        this.userPwdHash = CreateHashBcryptSalt(password);
        this.privileges = privileges;
        this.adminAccess = adminAccess;
        this.badPasscodeAttemptsTimestamps = new LinkedList<>();
        this.accountLockTimestamp = 0;
        this.lastPasswordModifiedTimestamp = Calendar.getInstance().getTimeInMillis();
    }

    public static String CreateHashBcryptSalt(String password){
        return Util.hex2Str(BCrypt.withDefaults().hash(9,salt, password.getBytes(StandardCharsets.UTF_8)));
    }

    public void setInitialPassword(String newInitialPassword) {
        this.userInitialPwdHash = CreateHashBcryptSalt(newInitialPassword);
    }

    public void setPassword( String newPassword ) {
        this.userPwdHash = CreateHashBcryptSalt( newPassword );
    }

    public Boolean verifyInitialPassword(IDependency d, String toCompare) {
        // didn't match an optomany login, check against regular password hash
        if (toCompare != null && this.getUserInitialPwdHash() != null) {
            return CreateHashBcryptSalt(toCompare).compareTo(this.getUserInitialPwdHash()) == 0;
        }

        return false;
    }

    public Boolean verifyPassword(IDependency d, String toCompare) {
        if (isAccountLocked(d))
            return false;

        if (toCompare != null && this.getUserPwdHash() != null) {
            if ( CreateHashBcryptSalt(toCompare).compareTo(this.getUserPwdHash()) == 0) {
                unlockClearBadPasscodeAttempts(d);
                return true;
            } else {
                processBadPasscodeAttempt(d);
            }
        }

        return false;
    }

    public void processBadPasscodeAttempt(IDependency d) {
        if(d.getPayCfg().isPasscodeSecuritySupported()) {
            Date now = new Date();
            boolean updateFlag = false;
            int retryLimit = DEFAULT_RETRY_LIMIT;
            long passwordAttemptWindow = DEFAULT_PASSWORD_ATTEMPT_WINDOW_MS;
            String configRetryLimit = d.getPayCfg().getPasswordRetryLimit();
            String configPasswordAttemptWindow = d.getPayCfg().getPasswordAttemptWindow();

            try {
                retryLimit = Integer.parseInt(configRetryLimit);
                passwordAttemptWindow = Integer.parseInt(configPasswordAttemptWindow) * 60 * 1000;
            } catch (NumberFormatException e) {
                Timber.w(e);
            }

            if (passwordAttemptWindow == 0 && retryLimit == 0)
                // No window, no restrictions, no locking
                return;

            if (retryLimit > MAX_RETRY_LIMIT)
                retryLimit = MAX_RETRY_LIMIT;
            // Remove expired attempts
            while (badPasscodeAttemptsTimestamps.size() > 0) {
                if (badPasscodeAttemptsTimestamps.getFirst() < now.getTime() - passwordAttemptWindow) {
                    badPasscodeAttemptsTimestamps.removeFirst();
                    updateFlag = true;
                } else
                    break;
            }

            // Record bad attempt
            if (this.badPasscodeAttemptsTimestamps.size() < MAX_BAD_ATTEMPTS_TIMESTAMPS_STORED) {
                this.badPasscodeAttemptsTimestamps.add(now.getTime());
                updateFlag = true;
            }

            // Lock user if necessary
            if (badPasscodeAttemptsTimestamps.size() >= retryLimit && accountLockTimestamp == 0) {
                accountLockTimestamp = now.getTime();
                updateFlag = true;
            }

            if (updateFlag)
                userDao.update(this);
        } else {
            d.getPayCfg().setPasswordAttemptWindow(String.valueOf(0));
            d.getPayCfg().setPasswordRetryLimit(String.valueOf(0));
            d.getPayCfg().setPasswordAttemptLockoutDuration(String.valueOf(0));
        }
    }

    public long timeToUnlockAccount(IDependency d) {
        long timeToUnlock = 0;
        if (accountLockTimestamp != 0) {
            long passwordAttemptLockoutDuration = DEFAULT_PASSWORD_ATTEMPT_LOCKOUT_DURATION_MS;
            String configPasswordAttemptLockoutDuration = d.getPayCfg().getPasswordAttemptLockoutDuration();

            try {
                passwordAttemptLockoutDuration = Integer.parseInt(configPasswordAttemptLockoutDuration) * 60 * 1000;
            } catch (NumberFormatException e) {
                Timber.w(e);
            }

            if (passwordAttemptLockoutDuration > MAX_PASSWORD_ATTEMPT_LOCKOUT_DURATION_MS)
                passwordAttemptLockoutDuration = MAX_PASSWORD_ATTEMPT_LOCKOUT_DURATION_MS;

            if (passwordAttemptLockoutDuration == 0) {
                // No window, no lock
                accountLockTimestamp = 0;
                return accountLockTimestamp;
            }

            Date now = new Date();
            if (accountLockTimestamp + passwordAttemptLockoutDuration < now.getTime()) {
                // unlock account
                timeToUnlock = 0;
                unlockClearBadPasscodeAttempts(d);
            } else {
                timeToUnlock = accountLockTimestamp + passwordAttemptLockoutDuration - now.getTime();
            }
        }
        return timeToUnlock;
    }

    public boolean isAccountLocked(IDependency d) {
        return timeToUnlockAccount(d) != 0;
    }

    public void unlockClearBadPasscodeAttempts(IDependency d) {
        badPasscodeAttemptsTimestamps.clear();
        accountLockTimestamp = 0;
        userDao.update(this);
    }

    @TypeConverter
    public int privilegeToInt(User.Privileges privileges) {
        return privileges.ordinal();
    }

    @TypeConverter
    public User.Privileges intToPrivilege(int val) {
        return User.Privileges.values()[val];
    }

    public int getUid() {
        return this.uid;
    }

    public String getUserId() {
        return this.userId;
    }

    public String getUserInitialPwdHash() {
        return this.userInitialPwdHash;
    }

    public String getUserPwdHash() {
        return this.userPwdHash;
    }

    public String getUserName() {
        return this.userName;
    }

    public Privileges getPrivileges() {
        return this.privileges;
    }

    public String getDepartmentId() {
        return this.departmentId;
    }

    public String getTerminalId() {
        return this.terminalId;
    }

    public boolean isAdminAccess() {
        return this.adminAccess;
    }

    public int getFailedLogonAttempts() {
        return this.failedLogonAttempts;
    }

    public long getAccountUnlockTimestamp() {
        return this.accountUnlockTimestamp;
    }

    public LinkedList<Long> getBadPasscodeAttemptsTimestamps() {
        return this.badPasscodeAttemptsTimestamps;
    }

    public long getAccountLockTimestamp() {
        return this.accountLockTimestamp;
    }

    public long getLastPasswordModifiedTimestamp() {
        return this.lastPasswordModifiedTimestamp;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setUserInitialPwdHash(String userInitialPwdHash) {
        this.userInitialPwdHash = userInitialPwdHash;
    }

    public void setUserPwdHash(String userPwdHash) {
        this.userPwdHash = userPwdHash;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setPrivileges(Privileges privileges) {
        this.privileges = privileges;
    }

    public void setDepartmentId(String departmentId) {
        this.departmentId = departmentId;
    }

    public void setTerminalId(String terminalId) {
        this.terminalId = terminalId;
    }

    public void setAdminAccess(boolean adminAccess) {
        this.adminAccess = adminAccess;
    }

    public void setFailedLogonAttempts(int failedLogonAttempts) {
        this.failedLogonAttempts = failedLogonAttempts;
    }

    public void setAccountUnlockTimestamp(long accountUnlockTimestamp) {
        this.accountUnlockTimestamp = accountUnlockTimestamp;
    }

    public void setBadPasscodeAttemptsTimestamps(LinkedList<Long> badPasscodeAttemptsTimestamps) {
        this.badPasscodeAttemptsTimestamps = badPasscodeAttemptsTimestamps;
    }

    public void setAccountLockTimestamp(long accountLockTimestamp) {
        this.accountLockTimestamp = accountLockTimestamp;
    }

    public void setLastPasswordModifiedTimestamp(long lastPasswordModifiedTimestamp) {
        this.lastPasswordModifiedTimestamp = lastPasswordModifiedTimestamp;
    }

    public enum Privileges {
        USER,
        MOBILE_USER,
        SUPERVISOR,
        MANAGER,
        TECHNICIAN,
        FUNCTION_USER,
    }

    @TypeConverter
    public static String fromLinkedList(LinkedList<Long> list) {
        StringBuilder string = new StringBuilder();
        for(Long l : list) string.append(l.toString()).append(",");
        return string.toString();
    }

    @TypeConverter
    public static LinkedList<Long> toLinkedList(String longsAsString) {
        LinkedList<Long> l = new LinkedList<>();
        if (longsAsString != null) {
            String[] stringList = longsAsString.split(",");
            for (String s : stringList) {
                if (s.length()>0) {
                    try {
                        l.add(Long.parseLong(s));
                    } catch (NumberFormatException e) {
                        Timber.w(e);
                    }
                }
            }
        }
        return l;
    }

}

