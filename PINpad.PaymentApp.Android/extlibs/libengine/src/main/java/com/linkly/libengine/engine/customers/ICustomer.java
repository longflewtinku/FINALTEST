package com.linkly.libengine.engine.customers;

import com.linkly.libengine.config.IConfig;
import com.linkly.libengine.engine.EngineManager;
import com.linkly.libengine.engine.protocol.IProto;
import com.linkly.libengine.engine.transactions.TransRec;
import com.linkly.libengine.printing.IReceipt;

public interface ICustomer {

    IProto.TaskProtocolType getProtocolType();

    String getTerminalType(TransRec trans);

    String calculateRetRefNumber(TransRec trans);

    IReceipt getReceiptForTrans(TransRec trans);

    boolean overrideConfigs();

    boolean supportOfflineAsKeyed();

    boolean supportCtlsReferences();

    boolean supportRecWithAuthCount();

    boolean supportMotoAndTelephone();

    boolean supportTipsOnReports();

    boolean supportDefaultUsers();

    boolean supportAutoReversals();

    boolean supportReversalsForTransType(EngineManager.TransType transType);

    boolean supportManualVoids();

    boolean supportStoringDBEncryptedCardData();

    boolean supportFullDailyBatchReport();

    PCI_FORMAT wipePciSensitiveData();

    boolean supportAutoRecs();

    boolean supportPreAuthCompletion();

    boolean supportAvs();

    boolean supportCscForRefund();

    boolean supportOnlineReversal();

    boolean supportOnlinePin();

    IConfig getConfigProvider();

    int getTcuKeyLength(); // used for AS2805 6.5.3 asymmetric style key management only. Specifies TCU key length, in bits. Allowed values are 1984 and 960 only. Passed to secure app when initiating RKI

    boolean hideBrandDisplayLogoHeader();

    public enum PCI_FORMAT {
        NEVER,
        POST_TRANS,
        POST_REC
    }

    String getAcquirerCode();

    /**
     * returns passcode for specific transaction, where elevated user privileges are required
     *
     * @return empty = no password required, !empty = passcode
     */
    String getTransPasscode(TransRec trans);

    /**
     * returns passcode for specific transaction, where elevated user privileges are required.
     * This value is tightly coupled with the String getTransPasscode(TransRec trans);
     *
     * @return 0 or the count.
     */
    int getTransPasscodeRetryCount(TransRec trans);

    String getRequiredSecAppFlavor();
}
