package com.linkly.libengine.engine.transactions;

import static com.linkly.libengine.action.Printing.PrintFirst.buildReceiptForBroadcast;
import static com.linkly.libengine.engine.EngineManager.TransType.AUTO_LOGON;
import static com.linkly.libengine.engine.EngineManager.TransType.BALANCE;
import static com.linkly.libengine.engine.EngineManager.TransType.CARD_NOT_PRESENT;
import static com.linkly.libengine.engine.EngineManager.TransType.CARD_NOT_PRESENT_REFUND;
import static com.linkly.libengine.engine.EngineManager.TransType.CASH;
import static com.linkly.libengine.engine.EngineManager.TransType.CASHBACK;
import static com.linkly.libengine.engine.EngineManager.TransType.CASHBACK_AUTO;
import static com.linkly.libengine.engine.EngineManager.TransType.CASH_AUTO;
import static com.linkly.libengine.engine.EngineManager.TransType.COMPLETION;
import static com.linkly.libengine.engine.EngineManager.TransType.COMPLETION_AUTO;
import static com.linkly.libengine.engine.EngineManager.TransType.DCCRATES;
import static com.linkly.libengine.engine.EngineManager.TransType.DEPOSIT;
import static com.linkly.libengine.engine.EngineManager.TransType.LAST_RECONCILIATION;
import static com.linkly.libengine.engine.EngineManager.TransType.LAST_RECONCILIATION_AUTO;
import static com.linkly.libengine.engine.EngineManager.TransType.LOGON;
import static com.linkly.libengine.engine.EngineManager.TransType.MANUAL_REVERSAL;
import static com.linkly.libengine.engine.EngineManager.TransType.MANUAL_REVERSAL_AUTO;
import static com.linkly.libengine.engine.EngineManager.TransType.OFFLINECASH;
import static com.linkly.libengine.engine.EngineManager.TransType.OFFLINESALE;
import static com.linkly.libengine.engine.EngineManager.TransType.PREAUTH;
import static com.linkly.libengine.engine.EngineManager.TransType.PREAUTH_AUTO;
import static com.linkly.libengine.engine.EngineManager.TransType.PREAUTH_CANCEL;
import static com.linkly.libengine.engine.EngineManager.TransType.PREAUTH_CANCEL_AUTO;
import static com.linkly.libengine.engine.EngineManager.TransType.PREAUTH_MOTO;
import static com.linkly.libengine.engine.EngineManager.TransType.PREAUTH_MOTO_AUTO;
import static com.linkly.libengine.engine.EngineManager.TransType.PRE_RECONCILIATION;
import static com.linkly.libengine.engine.EngineManager.TransType.PRE_RECONCILIATION_AUTO;
import static com.linkly.libengine.engine.EngineManager.TransType.RECONCILIATION;
import static com.linkly.libengine.engine.EngineManager.TransType.RECONCILIATION_AUTO;
import static com.linkly.libengine.engine.EngineManager.TransType.REFUND;
import static com.linkly.libengine.engine.EngineManager.TransType.REFUND_AUTO;
import static com.linkly.libengine.engine.EngineManager.TransType.REFUND_MOTO;
import static com.linkly.libengine.engine.EngineManager.TransType.REFUND_MOTO_AUTO;
import static com.linkly.libengine.engine.EngineManager.TransType.SALE;
import static com.linkly.libengine.engine.EngineManager.TransType.SALE_AUTO;
import static com.linkly.libengine.engine.EngineManager.TransType.SALE_MOTO;
import static com.linkly.libengine.engine.EngineManager.TransType.SALE_MOTO_AUTO;
import static com.linkly.libengine.engine.EngineManager.TransType.SUMMARY_AUTO;
import static com.linkly.libengine.engine.EngineManager.TransType.TESTCONNECT;
import static com.linkly.libengine.engine.EngineManager.TransType.TOPUPCOMPLETION;
import static com.linkly.libengine.engine.EngineManager.TransType.TOPUPPREAUTH;
import static com.linkly.libengine.engine.customers.ICustomer.PCI_FORMAT.POST_TRANS;
import static com.linkly.libengine.engine.transactions.properties.TAudit.ReasonOnlineCode.FALLBACK_FOR_IC;
import static com.linkly.libengine.engine.transactions.properties.TAudit.ReasonOnlineCode.RTIME_FORCED_CARD_ACCEPTOR;
import static com.linkly.libengine.engine.transactions.properties.TCard.CaptureMethod.ICC_FALLBACK_KEYED;
import static com.linkly.libengine.engine.transactions.properties.TCard.CaptureMethod.ICC_FALLBACK_SWIPED;
import static com.linkly.libengine.engine.transactions.properties.TCard.CaptureMethod.MANUAL;
import static com.linkly.libengine.engine.transactions.properties.TCard.CvmType.ENCIPHERED_ONLINE_PIN;
import static com.linkly.libengine.engine.transactions.properties.TCard.CvmType.NO_CVM;
import static com.linkly.libengine.engine.transactions.properties.TCard.CvmType.SIG;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.AuthMethod.EFB_AUTHORISED;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.AuthMethod.LINK_DOWN_EFB_AUTHORISED;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.AuthMethod.OFFLINE_EFB_AUTHORISED;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.HostResult.RECONCILED_IN_BALANCE;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.MessageStatus.ADVICE_QUEUED;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.MessageStatus.AUTH_SENT;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.MessageStatus.DEFERRED_AUTH;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.MessageStatus.FINALISED;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.MessageStatus.FINALISED_AND_REVERSED;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.MessageStatus.NOT_SET;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.MessageStatus.REC_QUEUED;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.MessageStatus.REVERSAL_QUEUED;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.ReversalReason.SIGFAIL;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.ReversalState.NOT_REVERSIBLE;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.ReversalState.REVERSIBLE;
import static com.linkly.libengine.printing.IPrintManager.PrintPreference.PRINT_PREFERENCE_BOTH;
import static com.linkly.libengine.status.IStatus.STATUS_EVENT.STATUS_TRANS_CTLS;
import static com.linkly.libengine.status.IStatus.STATUS_EVENT.STATUS_TRANS_EMV;
import static com.linkly.libengine.status.IStatus.STATUS_EVENT.STATUS_TRANS_MANUAL;
import static com.linkly.libengine.status.IStatus.STATUS_EVENT.STATUS_TRANS_MSR;
import static com.linkly.libmal.global.printing.PrintReceipt.SCREEN_ICON.PR_ERROR_ICON;
import static com.linkly.libmal.global.printing.PrintReceipt.SCREEN_ICON.PR_SUCCESS_ICON;
import static com.linkly.libpositive.wrappers.PositiveTransResult.JournalType.NONE;
import static com.linkly.libsecapp.emv.Tag.cvm_results;
import static com.linkly.libui.IUIDisplay.String_id.STR_EMPTY;

import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.sqlite.db.SimpleSQLiteQuery;

import com.linkly.libconfig.cpat.CardProductCfg;
import com.linkly.libengine.BuildConfig;
import com.linkly.libengine.action.Printing.PrintFirst;
import com.linkly.libengine.config.BinRangesCfg;
import com.linkly.libengine.config.PayCfg;
import com.linkly.libengine.dependencies.IDependency;
import com.linkly.libengine.engine.Engine;
import com.linkly.libengine.engine.EngineManager;
import com.linkly.libengine.engine.EngineManager.TransType;
import com.linkly.libengine.engine.cards.Emv;
import com.linkly.libengine.engine.reporting.Reconciliation;
import com.linkly.libengine.engine.reporting.SchemeTotals;
import com.linkly.libengine.engine.reporting.ShiftTotals;
import com.linkly.libengine.engine.transactions.properties.TAmounts;
import com.linkly.libengine.engine.transactions.properties.TAudit;
import com.linkly.libengine.engine.transactions.properties.TCard;
import com.linkly.libengine.engine.transactions.properties.TProtocol;
import com.linkly.libengine.engine.transactions.properties.TSec;
import com.linkly.libengine.env.BatchNumber;
import com.linkly.libengine.env.ReceiptNumber;
import com.linkly.libengine.env.Stan;
import com.linkly.libengine.overrides.CoreOverrides;
import com.linkly.libengine.printing.IReceipt;
import com.linkly.libengine.status.IStatus;
import com.linkly.libengine.users.User;
import com.linkly.libengine.users.UserManager;
import com.linkly.libmal.IMal;
import com.linkly.libmal.IMalPrint;
import com.linkly.libmal.global.printing.PrintReceipt;
import com.linkly.libmal.global.util.Util;
import com.linkly.libpositive.events.PositiveTransEvent;
import com.linkly.libpositive.wrappers.LinklyBinNumber;
import com.linkly.libpositive.wrappers.PositiveReceiptResponse;
import com.linkly.libpositive.wrappers.PositiveTransResult;
import com.linkly.libpositive.wrappers.TagDataFromPOS;
import com.linkly.libpositive.wrappers.TagDataToPOS;
import com.linkly.libsecapp.IP2PSec;
import com.linkly.libsecapp.P2PLib;
import com.linkly.libsecapp.emv.EmvTags;
import com.linkly.libsecapp.emv.Tag;
import com.pax.dal.entity.TrackData;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import timber.log.Timber;

@Entity(tableName = "transrecs",
        indices = {@Index("cancelled"),
                @Index("summedOrReced"),
                @Index("transType"),
                @Index("prot_messageStatus"),
                @Index("audit_receiptNumber"),
                @Index("audit_uti"),
                @Index("prot_reversalState"),
                @Index("audit_userId"),
                @Index("audit_transDateTime")
        })
public class TransRec {
    @Ignore
    private static final int DEFAULT_PREAUTH_EXPIRY_DAYS = 7;  // used when no expiry day set in config or found null for card scheme

    // !!!!!!!!!!!!!!! NOTE when modifying any fields that aren't annotated with @Ignore, be sure to increment 'version =' in TransRecDatabase.java !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    //------------------------------------- fields that are persisted to database ------------------------------------
    @PrimaryKey(autoGenerate = true)
    public int uid = 0;
    private EngineManager.TransType transType;
    private boolean finalised = false;
    private boolean approved = false;
    private boolean cancelled = false;
    private boolean declined = false;
    private boolean referred = false;
    private boolean deferredAuth = false;
    private String emvTagsString;
    private String ctlsTagsString;

    // If the device was in offline mode when the transaction was started
    // offlineFlightModeAllowed flag from override params takes precedence,
    @Ignore
    private boolean startedInOfflineMode = false;
    @Ignore
    private boolean suppressPosDialog = true;

    public boolean isPatMode(String mode) {
        return tagDataFromPos != null && tagDataFromPos.getPAT() != null && tagDataFromPos.getPAT().equals(mode);
    }

    // override to set emvTags value as we are not storing EmvTags in DB
    public void setEmvTagsString(String emvTagsStringParm) {
        emvTagsString = emvTagsStringParm;

        if (emvTagsString != null) {
            byte[] emvTagsbytes = Util.hexStringToByteArray(emvTagsString);
            try {
                EmvTags emvTags = new EmvTags(emvTagsbytes);
                card.setTags(emvTags);
            } catch (Exception e) {
                Timber.i("Exception during setting Tags");
                Timber.w(e);
            }
        }
    }

    // override to set emvTags value as we are not storing EmvTags in DB
    public void setCtlsTagsString(String ctlsTagsStringParm) {
        ctlsTagsString = ctlsTagsStringParm;
        if (ctlsTagsString != null) {
            byte[] ctlsTagsBytes = Util.hexStringToByteArray(ctlsTagsString);
            try {
                EmvTags ctlsTags = new EmvTags(ctlsTagsBytes);
                card.setTags(ctlsTags);
            } catch (Exception e) {
                Timber.i("Exception during setting Tags");
                Timber.w(e);
            }
        }
    }

    // Can't fully trust finalised flag due to times / waiting for card removal on user.
    // This is a clean way to know if the transaction was Reported or not for power fail situations.
    // Gets set when we use ECRHelpers.sendTransResult
    private boolean reportedToPOS = false;
    @Ignore
    private boolean dccEnquiry = false;

    @Ignore
    private boolean continuePrint = false; // A way for us to sync the pos and the printing values
    @Ignore
    private boolean printOnTerminal = true; // Set true by default only use

    private boolean summedOrReced = false; // summarized or reconciled

    @Embedded(prefix = "prot_")
    private TProtocol protocol = new TProtocol();
    @Embedded(prefix = "card_")
    private TCard card = new TCard();
    @Embedded(prefix = "amts_")
    private TAmounts amounts = new TAmounts();
    @Embedded(prefix = "sec_")
    private TSec security = new TSec();
    @Embedded(prefix = "audit_")
    private TAudit audit = new TAudit();

    @Ignore
    private Reconciliation reconciliation; // not saved
    @Ignore
    private List<SchemeTotals> schemeTotals; // not saved
    @Ignore
    private PositiveTransEvent transEvent; // not saved
    @Ignore
    private TagDataFromPOS tagDataFromPos; // not saved

    private TagDataToPOS tagDataToPos; // we do want to save this because sometimes we may want it back, e.g. in query card command for Local REST

    // truevo fields
    @Ignore
    private String status = ""; // not saved
    @Ignore
    private String transactionStatus = ""; // not saved
    @Ignore
    private String softwareVersion = "";
    @Ignore
    private PositiveTransResult.JournalType journalType = NONE; //not saved (for broadcasting)

    @Ignore
    private boolean functionUserLoggedIn = false;

    // serialised as JSON to database
    private ArrayList<PositiveTransResult.Receipt> receipts = new ArrayList<>();

    // UID (see above) of original preauth transaction. Used to link completions and incremental preauths etc to original
    private Integer preauthUid = null;

    @Ignore
    private boolean printTransactionListing = true;   // Autosettlement related variables, relevant for RECONCILIATION transaction
    @Ignore
    private int autoSettlementRetryCount = 0;

    @Ignore
    private EngineManager.TransType reconciliationOriginalTransType;    // Original transaction type to distinguish automatic settlements from other types

    @Ignore
    private ShiftTotals shiftTotals; // not saved

    //------------------------------------- fields that are NOT persisted to database - annotate with @Ignore ------------------------------------
    @Ignore
    public TransRec(EngineManager.TransType transactionType, IDependency dependency) {
        transType = transactionType;
        finalised = false;

        // Adding Enabling and Disabling of Macing here. It may need to move
        protocol.setIncludeMac(false);
        card = new TCard(transType, dependency.getPayCfg());
        amounts = new TAmounts(dependency.getPayCfg());
        audit = new TAudit(dependency);
    }

    public TransRec() {
    }

    public static int countTransByMessageStatus(List<TProtocol.MessageStatus> statuses) {
        StringBuilder statusString = null;

        for (TProtocol.MessageStatus status : statuses) {
            if (statusString == null) {
                statusString = new StringBuilder();
            } else {
                statusString.append(",");
            }
            statusString.append(status.ordinal());
        }

        SimpleSQLiteQuery query = new SimpleSQLiteQuery("SELECT COUNT(*) FROM transRecs WHERE prot_MessageStatus IN (" + statusString + ")");
        return TransRecManager.getInstance().getTransRecDao().executeIntQuery(query);
    }

    public static int countTransToUploadToPaxstore() {

        if (!Engine.getDep().getPayCfg().isPaxstoreUpload())
            return 0;

        SimpleSQLiteQuery query = new SimpleSQLiteQuery("SELECT COUNT(*) FROM transRecs WHERE prot_paxstoreUploaded = 0");
        return TransRecManager.getInstance().getTransRecDao().executeIntQuery(query);
    }

    public static int countTransToUploadToEmailServer() {
        SimpleSQLiteQuery query = new SimpleSQLiteQuery("SELECT COUNT(*) FROM transRecs WHERE prot_merchantEmailToUpload = 1");
        int merchantCount = TransRecManager.getInstance().getTransRecDao().executeIntQuery(query);

        SimpleSQLiteQuery query2 = new SimpleSQLiteQuery("SELECT COUNT(*) FROM transRecs WHERE prot_customerEmailToUpload = 1");
        int customerCount = TransRecManager.getInstance().getTransRecDao().executeIntQuery(query2);
        return merchantCount + customerCount;
    }

    public static int countTransInBatch() {
        ArrayList<TProtocol.MessageStatus> statusList = new ArrayList<>();
        statusList.add(REC_QUEUED);
        statusList.add(ADVICE_QUEUED);
        statusList.add(AUTH_SENT);
        statusList.add(REVERSAL_QUEUED);
        statusList.add(DEFERRED_AUTH);
        return countTransByMessageStatus(statusList);
    }

    public boolean isFinancialTransaction() {
        return !transType.adminTransaction;
    }

    // This gets a list of the actual financial TransType values (non-admin types).
    public static List<TransType> getFinancialTxnTypeList() {
        List<TransType> transList = new ArrayList<>();

        for (EngineManager.TransType t : EngineManager.TransType.values()) {
            if (!t.adminTransaction) {
                transList.add(t);
            }
        }
        return transList;
    }

    public static List<TransRec> getLastXTransactionsList(int count) {
        return getLastXTransactionsList(count, null);
    }

    public static List<TransRec> getLastXTransactionsList(Integer count, String extraCondition) {
        if(count != null) {
            return TransRecManager.getInstance().getTransRecDao().getLastXTransactionsList(getFinancialTxnTypeList(), extraCondition, count);
        } else {
            return TransRecManager.getInstance().getTransRecDao().getTransactionsList(getFinancialTxnTypeList(), extraCondition);
        }
    }

    public static TransRec getLatestFinancialTxn() {
        return TransRecManager.getInstance().getTransRecDao().getLatestFromTransTypeList(getFinancialTxnTypeList());
    }

    public void debug() {
        if (!BuildConfig.DEBUG) {
            return;
        }

        final String falseString = "FALSE";
        final String trueString = "TRUE";
        final String dashesString = "=====================================================";

        // Root
        String log = "";
        log += "=================== TRANSACTION DEBUG ===============" + "\n";
        log += "uid: " + uid + "\n";
        log += "Type: " + transType.toString() + "\n";
        log += "Finalised: " + (isFinalised() ? trueString : falseString) + "\n";
        log += "Approved: " + (isApprovedOrDeferred() ? trueString : falseString) + "\n";

        // TProtocol
        log += dashesString + "\n";
        log += "PROTOCOL" + "\n";
        log += "ServerResponseCode: " + protocol.getServerResponseCode() + "\n";
        log += "AuthOffline: " + (protocol.isCanAuthOffline() ? trueString : falseString) + "\n";
        log += "MessageStatus: " + protocol.getMessageStatus().toString() + "\n";
        log += "HostResult: " + protocol.getHostResult().toString() + "\n";
        log += "AuthMethod: " + protocol.getAuthMethod().toString() + "\n";
        log += "OriginalMessageType: " + protocol.getOriginalMessageType() + "\n";
        log += "Stan: " + protocol.getStan() + "\n";
        log += "BatchNumber: " + protocol.getBatchNumber() + "\n";
        log += "AccountType: " + protocol.getAccountType() + "\n";
        log += "AuthCode: " + protocol.getAuthCode() + "\n";
        log += "RRN: " + protocol.getRRN() + "\n";
        log += "POSResponseText: " + protocol.getPosResponseText() + "\n";

        // TCard
        log += dashesString + "\n";
        log += "Card" + "\n";
        log += "CardType: " + card.getCardType().toString() + "\n";
        log += "CVM Type: " + card.getCvmType().toString() + "\n";
        log += "CaptureMethod: " + card.getCaptureMethod().toString() + "\n";
        log += "Card Holder Present: " + (card.isCardholderPresent() ? trueString : falseString) + "\n";
        log += "Pan: " + card.getPan() + "\n";
        log += "Track 2: " + card.getTrack2() + "\n";
        log += "PSI: " + card.getPsi() + "\n";
        log += "Expiry: " + card.getExpiry() + "\n";
        log += "Service Code: " + card.getServiceCode() + "\n";
        log += "CVV: " + card.getCvv() + "\n";
        log += "Card Name: " + card.getCardName() + "\n";
        log += "Card Holder Name: " + card.getCardHolderName() + "\n";

        // TAmount
        log += dashesString + "\n";
        log += "Amounts" + "\n";
        log += "Amount: " + amounts.getAmount() + "\n";
        log += "Cash Back Amount: " + amounts.getCashbackAmount() + "\n";
        log += "Gratuity: " + amounts.getTip() + "\n";
        log += "Amount User Entered: " + amounts.getAmountUserEntered() + "\n";
        log += "Currency: " + amounts.getCurrency() + "\n";

        // TAudit
        log += dashesString + "\n";
        log += "Audit" + "\n";
        log += "Uti: " + audit.getUti() + "\n";
        log += "Reference: " + audit.getReference() + "\n";
        log += "UserId: " + audit.getUserId() + "\n";
        log += "TerminalId: " + audit.getTerminalId() + "\n";
        log += "MerchantId: " + audit.getMerchantId() + "\n";
        log += "Reference: " + audit.getReference() + "\n";
        log += "VirtualTid: " + audit.getVirtualTid() + "\n";
        log += "VirtualMid: " + audit.getVirtualMid() + "\n";
        log += "VirtualName: " + audit.getVirtualName() + "\n";
        log += "CountryCode: " + audit.getCountryCode() + "\n";
        log += "TransDateTime: " + audit.getTransDateTimeAsString("dd/MM/yyyy HH:mm:ss") + "\n";

        Timber.i(log);
    }


    public String getDukptData(String dataIn) {
        if (P2PLib.getInstance() != null) {
            byte[] result = P2PLib.getInstance().getIP2PSec().getDUKPTDesEncrypt(dataIn.getBytes(),
                    IP2PSec.KeyGroup.TRANS_GROUP);
            if (result != null) {
                return Util.byteArrayToHexString(result);
            }
        }
        return null;
    }

    public boolean encrypt() {
        // encrypt the data if we don't already have it encrypted
        if (!Util.isNullOrEmpty(security.getEncTrack2())) {
            Timber.i("Encrypted Track2:%s", security.getEncTrack2());
        } else if (!Util.isNullOrEmpty(card.getTrack2())) {

            security.setEncTrack2(getDukptData(card.getTrack2()));
        } else if (!Util.isNullOrEmpty(security.getEncPan())) {
            Timber.i("Encrypted PAN:%s", security.getEncPan());
        } else if (!Util.isNullOrEmpty(card.getPan())) {
            security.setEncPan(getDukptData(card.getPan()));
        }

        return true;
    }

    public void setToReverse(TProtocol.ReversalReason reversalReason) {

        if (isReconciliation())
            return;

        // for apacs - set diagnostic code
        if (reversalReason == SIGFAIL) {
            protocol.setDiagnosticCode("72"); // card acceptor has indicated an invalid signature
        }

        protocol.setReversalReason(reversalReason);
        audit.setReversalDateTime(System.currentTimeMillis());

        TProtocol.MessageStatus msgStatus = FINALISED_AND_REVERSED; // default to allow reversal, overridden below if required
        if (isOfflineAuthorized()) {
            // if deferred auth, in batch, and never sent to host, then we can mark it as finalised, with no reversal required
            // however, if send has been attempted, then we must queue a reversal for it
            // note: we could use similar logic in future for other offline-authorised trans types also
            if (isDeferredAuth() && protocol.getAdviceAttempts() > 0) {
                msgStatus = REVERSAL_QUEUED;
            }
        } else {
            // reversal queued, but not finalised, means reversal will be in the queue and must be processed
            msgStatus = REVERSAL_QUEUED;
        }
        updateMessageStatus(msgStatus);

        // set stan number to zero, to force protocol layer to choose which stan to use in reversal - original or new
        protocol.setStan(0);

        if (audit.getReceiptNumber() == -1) {
            audit.setReceiptNumber(ReceiptNumber.getNewValue());
        }
    }

    private boolean isOfflineAuthorized() {
        return protocol.getAuthMethod() == TProtocol.AuthMethod.OFFLINE_PRECOMMS_AUTHORISED ||
                protocol.getAuthMethod() == TProtocol.AuthMethod.OFFLINE_POSTCOMMS_AUTHORISED;
    }

    public void updateMessageStatus(TProtocol.MessageStatus messageStatus) {
        updateMessageStatus(messageStatus, true);
    }

    public void saveCtlsTagsString() {
        String newCtlsTagValues = Engine.getDep().getProtocol().saveCtlsTagValuesForDB(Engine.getDep(), this);

        if (newCtlsTagValues != null && !newCtlsTagValues.isEmpty())
            ctlsTagsString = newCtlsTagValues;
    }

    public void saveEmvTagsString() {
        String newEmvTagValues = Engine.getDep().getProtocol().saveEmvTagValuesForDB(Engine.getDep(), this);
        if (newEmvTagValues != null && !newEmvTagValues.isEmpty())
            emvTagsString = newEmvTagValues;
    }

    // Helper function, to reduce complexity value of updateMessageStatus()
    private void updateMessageStatusIncCounts(TProtocol.MessageStatus messageStatus) {
        // offline approved transaction are directly reversed and finalized instead of
        // sending reversal advice to host, so need to increment reversal count
        if (isOfflineAuthorized() && messageStatus == FINALISED_AND_REVERSED) {
            protocol.incReversalCount();
        }

        if (messageStatus == REVERSAL_QUEUED) {
            protocol.incReversalCount();
        }

        if (messageStatus == AUTH_SENT) {
            protocol.incAuthCount();
        }

        // we can set it to an advice in lots of scenarios,  REFERAL pre comms and post comms
        // at this point we need to make sure the counts only equate to 1 approval
        if (messageStatus == ADVICE_QUEUED) {
            protocol.setReversalCount(protocol.getAuthCount());
            protocol.incAuthCount();
        }
    }

    public void updateMessageStatus(TProtocol.MessageStatus messageStatus, boolean incrementCounts) {

        TProtocol.MessageStatus currentMessageStatus = protocol.getMessageStatus();

        if (currentMessageStatus != messageStatus) {
            Timber.i("NEW MSG STATUS:%s", messageStatus.name());
        }

        if (currentMessageStatus != messageStatus && incrementCounts) {
            updateMessageStatusIncCounts(messageStatus);
        }

        // if "trans" becomes finalised we no longer need the Sensitive Authentication Data (-PCI),
        if (Engine.getCustomer() != null && Engine.getCustomer().wipePciSensitiveData() == POST_TRANS) {
            Timber.i("updateMessageStatus from: " + currentMessageStatus.displayName + " to: " + messageStatus.displayName);

            if ((currentMessageStatus != FINALISED) && (currentMessageStatus != FINALISED_AND_REVERSED) &&
                    ((messageStatus == FINALISED) || (messageStatus == FINALISED_AND_REVERSED))) {

                Timber.i("Wiping SAD");

                TrackData emptyTrackData = new TrackData();

                card.setPan("");
                card.setTrack2("");
                card.setTrackData(emptyTrackData);
                card.setCvv("");
            }
        }

        protocol.setMessageStatus(messageStatus);
    }

    public boolean checkMsrFallback(boolean force, boolean isCardHolderPresent) {

        int currentErrors = card.getMsrReadErrors();
        if (!isCardHolderPresent) {
            Timber.i("Cardholder not present not allowed so ignore faulty swipe");
        } else if (force || currentErrors >= 2) {
            card.setMsrAllowed(false);
            card.setCtlsAllowed(false);
            card.setEmvAllowed(false);
            card.setCaptureMethod(ICC_FALLBACK_KEYED);
            card.invalidateCard();
            card.setMsrReadErrors(0);
            card.setMailOrder(false);
            card.setFaultyMsr(true);
            card.setOverTelephone(false);
            card.setCardholderPresent(true);
            return true;
        } else {
            card.setMsrReadErrors(currentErrors + 1);

        }
        finalised = false;
        return false;
    }

    /**
     * checkFallback
     * <p>
     * checks if fallback from insert to swipe is allowed
     * NOTE: it's the responsibility of the caller to move the transaction state to TransactionCanceller or initialProcessing
     *
     * @param d     dependency
     * @param force true = force immediate fallback to swipe
     * @return boolean true = fallback is allowed, or has happened (card retry), false = no fallback, cancel txn
     */
    public boolean checkFallback(IDependency d, boolean force) {
        // increment emv read errors
        card.setEmvReadErrors(card.getEmvReadErrors() + 1);

        // allow 2 failed chip read attempts before falling back to insert
        if (force || card.getEmvReadErrors() >= 2) {
            // if fallback from insert to swipe isn't allowed or MSR isn't allowed from TMS config
            // else if the device is in offline mode, return false here.
            // will usually result in transaction cancellation (up to the calling code to decide)
            if (!d.getPayCfg().isEmvFallback() ||
                !d.getPayCfg().isMsrAllowed() ||
                isStartedInOfflineMode()) {
                return false;
            }

            // Based on card tests a fallback should happen straight away rather than retries required.
            card.setMsrAllowed(true);
            card.setCtlsAllowed(false);
            card.setEmvAllowed(false);
            card.setCaptureMethod(ICC_FALLBACK_SWIPED);
            card.invalidateCard();
            card.setEmvReadErrors(0);
            audit.setReasonOnlineCode(FALLBACK_FOR_IC);
            // reset to 0/not set
            protocol.setAccountType(0);
            // TODO: clear pin try counter
            // clear all EMV tags
            if (card.getTags() != null) {
                card.getTags().clear();
            }
        }

        finalised = false;

        // return true to indicate that we should prompt for card entry again
        return true;
    }

    private void updateCardState(IStatus.STATUS_EVENT event, TCard.CaptureMethod method, boolean ctls) {
        if (Engine.getStatusReporter() != null) {
            Engine.getStatusReporter().reportStatusEvent(event ,isSuppressPosDialog());
        }
        card.setCaptureMethod(method);
        card.getLedStatus().setCTLSEnabled(ctls);
    }

    public void updateCardStateToMatchType(TCard.CardType cardType) {
        card.setCardType(cardType);
        switch (cardType) {
            case MSR:
                updateCardState(STATUS_TRANS_MSR, TCard.CaptureMethod.SWIPED, false);
                break;
            case EMV:
                updateCardState(STATUS_TRANS_EMV, TCard.CaptureMethod.ICC, false);
                break;
            case CTLS:
                updateCardState(STATUS_TRANS_CTLS, TCard.CaptureMethod.CTLS, true);
                break;
            case MANUAL:
                updateCardState(STATUS_TRANS_MANUAL, TCard.CaptureMethod.MANUAL, false);
                audit.setReasonOnlineCode(RTIME_FORCED_CARD_ACCEPTOR);
                break;
            default:
                Timber.i("cardType " + cardType + "not handled");
                break;
        }
    }

    /**********************************************************************************
     * functions that behave differently for each transaction type
     **********************************************************************************/
    public boolean isOfflineTransaction(PayCfg config) {
        CardProductCfg cardsConfig = card.getCardsConfig(config);
        if (isRefund() && (cardsConfig.isRefundOnline() && !cardsConfig.getServicesAllowed().isOfflineRefund())) {
            Timber.i("isOfflineTransaction=false");
            return false;
        }

        if (isCompletion() && (cardsConfig.isCompletionOnline())) {
            Timber.i("completion, but 'completion online' config flag set, so isOfflineTransaction=false");
            return false;
        }

        if (isRefund() || isCompletion() || transType == OFFLINESALE || transType == OFFLINECASH) {
            Timber.i("isOfflineTransaction=true");
            return true;
        }
        Timber.i("isOfflineTransaction=false");
        return false;
    }

    public boolean isPreAuth() {
        return (transType == PREAUTH) || (transType == PREAUTH_AUTO) || (transType == PREAUTH_MOTO) || (transType == PREAUTH_MOTO_AUTO) || (transType == TOPUPPREAUTH);
    }

    public boolean isPreAuthCancellation() {
        return (transType == PREAUTH_CANCEL || (transType == PREAUTH_CANCEL_AUTO));
    }

    public boolean isTopupPreAuth() {
        return transType == TOPUPPREAUTH;
    }

    public boolean isTopupCompletion() {
        return transType == TOPUPCOMPLETION;
    }

    public boolean isCompletion() {
        return (transType == COMPLETION) || (transType == TOPUPCOMPLETION) || (transType == COMPLETION_AUTO);
    }

    public boolean isPreAuthOrSecondaryPreAuth() {
        return ((transType == PREAUTH) || (transType == PREAUTH_AUTO) || (transType == PREAUTH_MOTO) || (transType == PREAUTH_MOTO_AUTO) || (transType == PREAUTH_CANCEL) || (transType == PREAUTH_CANCEL_AUTO) || (transType == COMPLETION) || (transType == COMPLETION_AUTO) || (transType == TOPUPPREAUTH) || (transType == TOPUPCOMPLETION));
    }

    public boolean isStandaloneOnlyFinancialTransaction(){
        return !transType.adminTransaction && !transType.autoTransaction;
    }

    public boolean isDccRates() {
        return (transType == DCCRATES);
    }

    public boolean mustAskIfSignatureRequired() {
        // TODO make it so we don't ask twice, for top-ups
        return transType == COMPLETION;
    }

    public boolean mustUsePinAndSig() {
        return card.isCupCard(Engine.getDep()) && (transType == PREAUTH || transType == TOPUPPREAUTH);
    }

    public boolean mustUseSig() {
        if ((transType == OFFLINECASH || transType == OFFLINESALE) && !card.isIccOrCtlsCaptured()) {
            return true;
        }
        return card.isManual() && card.isCardholderPresent();
    }

    public boolean isOfflineDependency(PayCfg config) {

        if (isSale() && card.getCardsConfig(config).isForceOffline()) {
            return true;
        }

        switch (transType) {
            case PREAUTH:
            case PREAUTH_AUTO:
            case PREAUTH_MOTO:
            case PREAUTH_MOTO_AUTO:
            case TOPUPPREAUTH:
            case COMPLETION:
            case TOPUPCOMPLETION:
            case OFFLINESALE:
                return true;
            default:
                return false;
        }
    }

    @SuppressWarnings("java:S3776") // Suppress Sonar complexity warning
    public static boolean isSupervisorRequiredForTransType(PayCfg payCfg, TransRec trans, CardProductCfg.PasswordRequired pwdCfg, TCard.CaptureMethod captureMethd) {

        if (trans == null || pwdCfg == null)
            return false;

        /* don't use the one on trans as might not be quite captured yet */
        if (captureMethd == MANUAL && pwdCfg.isMoto()) {
            return true;
        }

        EngineManager.TransType transType = trans.getTransType();
        if (transType == REFUND && payCfg.isRefundSecure()) {
            return true;
        }
        return (transType == SALE && pwdCfg.isSale()) ||
                ((transType == CASHBACK || transType == CASHBACK_AUTO) && pwdCfg.isCashback()) ||
                (transType == REFUND && pwdCfg.isRefund()) ||
                (transType == CASH && pwdCfg.isCash()) ||
                ((transType == COMPLETION || transType == TOPUPCOMPLETION) && pwdCfg.isCompletion()) ||
                ((transType == PREAUTH || transType == TOPUPPREAUTH) && pwdCfg.isPreauth()) ||
                (transType == BALANCE && pwdCfg.isBalance()) ||
                (transType == DEPOSIT && pwdCfg.isDeposit()) ||
                (transType == OFFLINESALE && pwdCfg.isForced()) ||
                (transType == OFFLINECASH && pwdCfg.isForced());
    }

    public boolean isSupervisorRequired(PayCfg config) {
        CardProductCfg.PasswordRequired pwdCfg = card.getCardsConfig(config).getPasswordRequired();
        boolean ret = false;
        if (pwdCfg != null) {
            ret = isSupervisorRequiredForTransType(config, this, pwdCfg, card.getCaptureMethod());
        }
        return ret;
    }

    @SuppressWarnings({"java:S1871", "java:S3776"}) // suppress "This branch's code block is the same as the block for the branch on line xxx"; also suppress Sonar complexity warning
    public boolean isTransactionDisallowed(PayCfg payCfg) {
        CardProductCfg.ServicesAllowed servicesAllowed = card.getCardsConfig(payCfg).getServicesAllowed();
        BinRangesCfg binCfg = Engine.getBinRangesCfg();

        boolean disallowed = false;
        if ((transType == SALE || transType == SALE_AUTO)&& !servicesAllowed.isSale()) {
            disallowed = true;
        } else if ((transType == CASHBACK || transType == CASHBACK_AUTO) && !servicesAllowed.isCashback()) {
            disallowed = true;
        } else if((transType == REFUND || transType == REFUND_AUTO) && ((!servicesAllowed.isRefund() && !servicesAllowed.isOfflineRefund())
                || (binCfg != null && binCfg.isRefundDisabled(payCfg, card.getTrack2())))) {
            disallowed = true;
        } else if ((transType == CASH || transType == CASH_AUTO) && !servicesAllowed.isCash()) {
            disallowed = true;
        } else if ((transType == PREAUTH || transType == PREAUTH_AUTO || transType == PREAUTH_MOTO || transType == PREAUTH_MOTO_AUTO) && !servicesAllowed.isPreauth()) {
            disallowed = true;
        } else if (transType == TOPUPPREAUTH && !servicesAllowed.isPreauth()) {
            disallowed = true;
        } else if (transType == COMPLETION && !servicesAllowed.isPreauth()) {
            disallowed = true;
        } else if (transType == TOPUPCOMPLETION && !servicesAllowed.isPreauth()) {
            disallowed = true;
        } else if (transType == BALANCE && !servicesAllowed.isBalance()) {
            disallowed = true;
        } else if (transType == DEPOSIT && !servicesAllowed.isDeposit()) {
            disallowed = true;
        } else if (transType == OFFLINESALE && !servicesAllowed.isForced()) {
            disallowed = true;
        } else if (transType == OFFLINECASH && !servicesAllowed.isOfflineCash()) {
            disallowed = true;
        }

        if ((transType == TOPUPPREAUTH ||
                transType == PREAUTH ||
                transType == PREAUTH_MOTO ||
                transType == REFUND ||
                transType == OFFLINESALE ||
                transType == OFFLINECASH ||
                transType == COMPLETION) && binCfg != null) {
            if (binCfg.isSms(payCfg, card.getTrack2())) {
                disallowed = false;
            }
        }

        Timber.i("TransRec disallowed = %b", disallowed);
        return disallowed;
    }

    public boolean isDeposit() {
        return (transType == DEPOSIT);
    }

    public boolean isSale() {
        return (transType == SALE || transType == SALE_AUTO || transType == SALE_MOTO || transType == SALE_MOTO_AUTO || transType == OFFLINESALE || transType == CARD_NOT_PRESENT);
    }

    public boolean isReconciliation() {
        return (transType == RECONCILIATION) || (transType == RECONCILIATION_AUTO);
    }

    public boolean isLastReconciliation() {
        return (transType == LAST_RECONCILIATION) || (transType == LAST_RECONCILIATION_AUTO);
    }

    public boolean isPreReconciliation() {
        return (transType == PRE_RECONCILIATION) || (transType == PRE_RECONCILIATION_AUTO) || (transType == SUMMARY_AUTO);
    }

    public boolean isCnp() {
        return (transType == CARD_NOT_PRESENT) || (transType == CARD_NOT_PRESENT_REFUND);
    }

    public boolean isRefund() {
        return (transType == REFUND) || (transType == CARD_NOT_PRESENT_REFUND) || (transType == REFUND_AUTO) || (transType == REFUND_MOTO) || (transType == REFUND_MOTO_AUTO);
    }

    public boolean isMoto() {
        return (transType == SALE_MOTO) || (transType == SALE_MOTO_AUTO) || (transType == REFUND_MOTO) || (transType == REFUND_MOTO_AUTO) || (transType == PREAUTH_MOTO) || (transType == PREAUTH_MOTO_AUTO);
    }

    public boolean isCashback() {
        return (transType == CASHBACK) || (transType == CASHBACK_AUTO);
    }

    public boolean isCash() {
        return (transType == CASH) || (transType == CASH_AUTO) || (transType == OFFLINECASH);
    }

    public boolean isBalance() {
        return (transType == BALANCE);
    }

    public boolean isLogon() {
        return (transType == AUTO_LOGON) || (transType == LOGON) || (transType == EngineManager.TransType.RSA_LOGON);
    }

    public boolean isForceOnlineRequired(PayCfg cfg) {

        if (card.getCvmType() == ENCIPHERED_ONLINE_PIN) {
            Timber.i("isForceOnlineRequired true as ENCIPHERED_ONLINE_PIN transaction");
            return true;
        }

        return (!isOfflineTransaction(cfg) && (card.getCaptureMethod() == MANUAL || card.isOnlineSC()));
    }

    public boolean isEfbAuthorisedTransaction() {
        return protocol.getAuthMethod() == EFB_AUTHORISED || protocol.getAuthMethod() == OFFLINE_EFB_AUTHORISED || protocol.getAuthMethod() == LINK_DOWN_EFB_AUTHORISED;
    }

    private double convertCentsToDouble(int amount) {
        return (double) amount / 100;
    }

    public double getMaxValueForCardsConfig(PayCfg config, CardProductCfg cardsConfig, boolean cashbackOnly) {
        double maxValue = 0;
        if (cardsConfig.getLimits() != null) {
            maxValue = cardsConfig.getLimits().getMax();
            if ((transType == CASH || transType == CASH_AUTO)) {
                double cardLimit = cardsConfig.getLimits().getCashMax();
                double configLimit = convertCentsToDouble(config.getCashoutLimitCents());

                maxValue = Math.min(cardLimit, configLimit);
            } else if (cashbackOnly) {
                maxValue = cardsConfig.getLimits().getCashbackMax();
            } else if (transType == CASHBACK || transType == CASHBACK_AUTO) {
                maxValue = convertCentsToDouble(config.getSaleLimitCents());
            } else if (transType == REFUND && isOfflineTransaction(config)) {
                maxValue = cardsConfig.getLimits().getOfflineRefundMax();
            } else if (isSale()) {
                maxValue = convertCentsToDouble(config.getSaleLimitCents());
            } else if (isPreAuth()) {
                maxValue = convertCentsToDouble(config.getPreAuthLimitCents());
            }
        }
        return maxValue;
    }

    public double getMaxCashValueForCardsConfig(PayCfg config, CardProductCfg cardsConfig, boolean cashoutOnly) {
        double maxCashValue = 0;
        if (cardsConfig.getLimits() != null) {
            maxCashValue = cardsConfig.getLimits().getMax();
            if (cashoutOnly) {
                double cardLimit = cardsConfig.getLimits().getCashMax();
                double configLimit = convertCentsToDouble(config.getCashoutLimitCents());

                maxCashValue = Math.min(cardLimit, configLimit);
            } else if (transType == CASHBACK || transType == CASHBACK_AUTO) {
                maxCashValue = convertCentsToDouble(config.getCashoutLimitCents());
            }
        }
        return maxCashValue;
    }

    public double getSystemMaxValue(PayCfg config) {

        double maxValue = 0;
        if (config != null) {
            for (CardProductCfg cardsConfig : config.getCards()) {
                double tmpMaxValue = getMaxValueForCardsConfig(config, cardsConfig, false);
                if (tmpMaxValue > maxValue) {
                    maxValue = tmpMaxValue;
                }
            }
        }
        return maxValue;
    }

    public double getSystemMaxCashValue(PayCfg config) {

        double maxValue = 0;
        if (config != null) {
            for (CardProductCfg cardsConfig : config.getCards()) {
                double tmpMaxValue = getMaxCashValueForCardsConfig(config, cardsConfig, false);
                if (tmpMaxValue > maxValue) {
                    maxValue = tmpMaxValue;
                }
            }
        }
        return maxValue;
    }

    public double getMaxValueAllowed(PayCfg config, boolean cashbackOnly) {
        return (card.getCardIndex() >= 0) ?
                getMaxValueForCardsConfig(config, card.getCardsConfig(config), cashbackOnly) :
                getSystemMaxValue(config);
    }

    public double getMaxCashValueAllowed(PayCfg config,boolean cashoutOnly) {
        return (card.getCardIndex() >= 0) ?
                getMaxCashValueForCardsConfig(config, card.getCardsConfig(config), cashoutOnly) :
                getSystemMaxCashValue(config);
    }

    public double getMaxValueAllowedDollars(PayCfg config) {
        return getMaxValueAllowed(config, false);
    }

    public double getMaxCashValueAllowedDollars(PayCfg config, boolean cashoutOnly) {
        return getMaxCashValueAllowed(config, cashoutOnly);
    }

    public long getMinValueAllowedDollars(PayCfg config) {
        return card.getCardsConfig(config).getLimits().getMin();
    }

    public void updateCvmTypeFromCard() {

        // already set for topup completions
        if (isTopupCompletion()) {
            return;
        }

        if (!card.isCtlsCaptured())
            card.setCvmType(SIG);

        if (card.getTags() == null)
            return;

        // check if CVM was performed. it might have been skipped, in which case we don't need to do CVM
        byte[] tsi = card.getTags().getTag(Tag.tsi);
        if (tsi == null || tsi.length != 2) {
            return;
        }

        // check if CVM performed bit is zero
        if ((byte) (tsi[0] & 0x40) == 0) {
            // cvm not performed
            Timber.i("TSI says CVM wasn't performed, setting CVM to No CVM");
            card.setCvmType(NO_CVM);
        } else {
            byte[] cvm = card.getTags().getTag(cvm_results);
            if (cvm == null) {
                return;
            }

            TCard.CvmType cvmType = Emv.emvGetCvmFromByte1(cvm[0]);
            Timber.i("Update CVM Type from card:%s", cvmType.name());
            card.setCvmType(cvmType);
        }
    }

    public int getAccountSelection(PayCfg payCfg) {

        int accountSelectionVal = 0;
        if (payCfg != null && payCfg.getCards().size() > card.getCardIndex()) {
            accountSelectionVal = payCfg.getCards().get(card.getCardIndex()).getAccountSelection();
        }
        return accountSelectionVal;
    }


    public boolean updateCvmTypeFromConfig(PayCfg payCfg) {
        CardProductCfg.OnlinePin onlinePin = card.getCardsConfig(payCfg).getOnlinePin();

        String config;
        switch (transType) {
            default:
            case SALE:
            case SALE_AUTO:
                config = onlinePin.getSale();
                break;
            case BALANCE:
                config = onlinePin.getBalance();
                break;
            case CASH:
            case CASH_AUTO:
                config = onlinePin.getCash();
                break;
            case CASHBACK:
            case CASHBACK_AUTO:
                config = onlinePin.getCashback();
                break;
            case DEPOSIT:
                config = onlinePin.getDeposit();
                break;
            case PREAUTH:
            case TOPUPPREAUTH:
            case PREAUTH_AUTO:
                config = onlinePin.getPreauth();
                break;
            case REFUND:
            case REFUND_AUTO:
                config = onlinePin.getRefund();
                break;
            case COMPLETION:
            case COMPLETION_AUTO:
            case TOPUPCOMPLETION:
            case OFFLINESALE:
            case OFFLINECASH:
                config = "N";
                break;
        }

        if (config.compareToIgnoreCase("Y") == 0 || config.compareToIgnoreCase("YES") == 0) {
            card.setCvmType(ENCIPHERED_ONLINE_PIN);
        } else if (config.compareToIgnoreCase("SVC") == 0 && card.isPinMandatorySC()) {
            card.setCvmType(ENCIPHERED_ONLINE_PIN);
        } else {
            card.setCvmType(SIG);
        }

        return true;
    }

    /**************************************************
     Below are some functions for use on Transactions
     **************************************************/
    public IMalPrint.PrinterReturn print(IDependency d, boolean isMerchantCopy, boolean isDuplicate, IMal mal) {
        // log to logcat by default
        return print(d, isMerchantCopy, isDuplicate, true, mal);
    }

    public IMalPrint.PrinterReturn print(IDependency d, boolean isMerchantCopy, boolean isDuplicate, boolean logToLogcat, IMal mal) {
        IReceipt receiptGenerator = d.getPrintManager().getReceiptForTrans(d, this, mal);
        if (receiptGenerator == null) {
            return IMalPrint.PrinterReturn.UNKNOWN_FAILURE;
        }

        receiptGenerator.setIsMerchantCopy(isMerchantCopy);
        receiptGenerator.setIsCardHolderCopy(!isMerchantCopy);
        receiptGenerator.setIsDuplicate(isDuplicate);

        PrintReceipt receiptToPrint = receiptGenerator.generateReceipt(this);

        if( logToLogcat ) {
            // log receipt to logcat. convert to array of strings first
            // Note receipt type isn't important here as it sets metadata in PositiveReceiptResponse data that's not used when logging
            PositiveReceiptResponse receiptForDebug = buildReceiptForBroadcast(receiptToPrint, null, PrintFirst.ReceiptType.MERCHANT, isDuplicate, true);
            printReceiptToDebug(receiptForDebug);
        }

        if ((CoreOverrides.get().isSpoofComms() && isApprovedOrDeferred()) ||
            (CoreOverrides.get().isSpoofComms() && transType == RECONCILIATION) ||
            isApprovedOrDeferred() ||
            (transType == RECONCILIATION && protocol.getHostResult() == RECONCILED_IN_BALANCE)) {
            receiptToPrint.setIconFinished(PR_SUCCESS_ICON);
            receiptToPrint.setIconWhilePrinting(PR_SUCCESS_ICON);
        } else {
            receiptToPrint.setIconFinished(PR_ERROR_ICON);
            receiptToPrint.setIconWhilePrinting(PR_ERROR_ICON);
        }

        if (audit.isDisablePrinting()) {
            Timber.i("Printing disabled for this transaction");
            return IMalPrint.PrinterReturn.SUCCESS;
        } else {
            return d.getPrintManager().printReceipt(d, receiptToPrint, "", true, STR_EMPTY, PRINT_PREFERENCE_BOTH, mal);
        }
    }

    public boolean isReversible() {
        updateReversalState();
        return protocol.getReversalState() == TProtocol.ReversalState.REVERSIBLE;
    }

    public boolean isReversal() {
        return getTransType() == MANUAL_REVERSAL ||
                getTransType() == MANUAL_REVERSAL_AUTO ||
                getTransType() == PREAUTH_CANCEL_AUTO ||
                getTransType() == PREAUTH_CANCEL;
    }

    private void updateReversalState() {
        TProtocol.MessageStatus messageStatus = protocol.getMessageStatus();

        if (messageStatus == FINALISED_AND_REVERSED || messageStatus == NOT_SET) {
            Timber.w("messageStatus value is %s, setReversalState(NOT_REVERSIBLE)", messageStatus.name());
            protocol.setReversalState(NOT_REVERSIBLE);
        } else if (!transType.supportsReversal) {
            Timber.w("TransType disallows reversal, setReversalState(NOT_REVERSIBLE)");
            protocol.setReversalState(NOT_REVERSIBLE);
        } else {
            protocol.setReversalState(REVERSIBLE);
        }

    }

    public boolean getFunctionUserLoggedIn() {
        return functionUserLoggedIn;
    }

    public int getUid() {
        return this.uid;
    }

    public TransType getTransType() {
        return this.transType;
    }

    public boolean isFinalised() {
        return this.finalised;
    }

    public boolean isApproved() {
        return this.approved;
    }

    public boolean isCancelled() {
        return this.cancelled;
    }

    public boolean isDeclined() {
        return this.declined;
    }

    public boolean isReferred() {
        return this.referred;
    }

    public boolean isDeferredAuth() {
        return this.deferredAuth;
    }

    public String getEmvTagsString() {
        return this.emvTagsString;
    }

    public String getCtlsTagsString() {
        return this.ctlsTagsString;
    }

    public boolean isStartedInOfflineMode() {
        return this.startedInOfflineMode;
    }

    public boolean isSuppressPosDialog() {
        return this.suppressPosDialog;
    }

    public boolean isReportedToPOS() {
        return this.reportedToPOS;
    }

    public boolean isDccEnquiry() {
        return this.dccEnquiry;
    }

    public boolean isContinuePrint() {
        return this.continuePrint;
    }

    public boolean isPrintOnTerminal() {
        return this.printOnTerminal;
    }

    public boolean isSummedOrReced() {
        return this.summedOrReced;
    }

    public TProtocol getProtocol() {
        return this.protocol;
    }

    public TCard getCard() {
        return this.card;
    }

    public TAmounts getAmounts() {
        return this.amounts;
    }

    public TSec getSecurity() {
        return this.security;
    }

    public TAudit getAudit() {
        return this.audit;
    }

    public Reconciliation getReconciliation() {
        return this.reconciliation;
    }

    public List<SchemeTotals> getSchemeTotals() {
        return this.schemeTotals;
    }

    public PositiveTransEvent getTransEvent() {
        return this.transEvent;
    }

    public TagDataFromPOS getTagDataFromPos() {
        return this.tagDataFromPos;
    }

    public TagDataToPOS getTagDataToPos() {
        return this.tagDataToPos;
    }

    public String getStatus() {
        return this.status;
    }

    public String getTransactionStatus() {
        return this.transactionStatus;
    }

    public String getSoftwareVersion() {
        return this.softwareVersion;
    }

    public PositiveTransResult.JournalType getJournalType() {
        return this.journalType;
    }

    public ArrayList<PositiveTransResult.Receipt> getReceipts() {
        return this.receipts;
    }

    public Integer getPreauthUid() {
        return this.preauthUid;
    }

    public boolean isPrintTransactionListing() {
        return this.printTransactionListing;
    }

    public int getAutoSettlementRetryCount() {
        return this.autoSettlementRetryCount;
    }

    public TransType getReconciliationOriginalTransType() {
        return this.reconciliationOriginalTransType;
    }

    public ShiftTotals getShiftTotals() {
        return this.shiftTotals;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public void setTransType(TransType transType) {
        this.transType = transType;
    }

    public void setFinalised(boolean finalised) {
        this.finalised = finalised;
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public void setDeclined(boolean declined) {
        this.declined = declined;
    }

    public void setReferred(boolean referred) {
        this.referred = referred;
    }

    public void setDeferredAuth(boolean deferredAuth) {
        this.deferredAuth = deferredAuth;
    }

    public void setStartedInOfflineMode(boolean startedInOfflineMode) {
        this.startedInOfflineMode = startedInOfflineMode;
    }

    public void setSuppressPosDialog(boolean suppressPosDialog) {
        this.suppressPosDialog = suppressPosDialog;
    }

    public void setReportedToPOS(boolean reportedToPOS) {
        this.reportedToPOS = reportedToPOS;
    }

    public void setDccEnquiry(boolean dccEnquiry) {
        this.dccEnquiry = dccEnquiry;
    }

    public void setContinuePrint(boolean continuePrint) {
        this.continuePrint = continuePrint;
    }

    public void setPrintOnTerminal(boolean printOnTerminal) {
        this.printOnTerminal = printOnTerminal;
    }

    public void setSummedOrReced(boolean summedOrReced) {
        this.summedOrReced = summedOrReced;
    }

    public void setProtocol(TProtocol protocol) {
        this.protocol = protocol;
    }

    public void setCard(TCard card) {
        this.card = card;
    }

    public void setAmounts(TAmounts amounts) {
        this.amounts = amounts;
    }

    public void setSecurity(TSec security) {
        this.security = security;
    }

    public void setAudit(TAudit audit) {
        this.audit = audit;
    }

    public void setReconciliation(Reconciliation reconciliation) {
        this.reconciliation = reconciliation;
    }

    public void setSchemeTotals(List<SchemeTotals> schemeTotals) {
        this.schemeTotals = schemeTotals;
    }

    public void setTransEvent(PositiveTransEvent transEvent) {
        this.transEvent = transEvent;
    }

    public void setTagDataFromPos(TagDataFromPOS tagDataFromPos) {
        this.tagDataFromPos = tagDataFromPos;
    }

    public void setTagDataToPos(TagDataToPOS tagDataToPos) {
        this.tagDataToPos = tagDataToPos;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setTransactionStatus(String transactionStatus) {
        this.transactionStatus = transactionStatus;
    }

    public void setSoftwareVersion(String softwareVersion) {
        this.softwareVersion = softwareVersion;
    }

    public void setJournalType(PositiveTransResult.JournalType journalType) {
        this.journalType = journalType;
    }

    public void setFunctionUserLoggedIn(boolean functionUserLoggedIn) {
        this.functionUserLoggedIn = functionUserLoggedIn;
    }

    public void setReceipts(ArrayList<PositiveTransResult.Receipt> receipts) {
        this.receipts = receipts;
    }

    public void setPreauthUid(Integer preauthUid) {
        this.preauthUid = preauthUid;
    }

    public void setPrintTransactionListing(boolean printTransactionListing) {
        this.printTransactionListing = printTransactionListing;
    }

    public void setAutoSettlementRetryCount(int autoSettlementRetryCount) {
        this.autoSettlementRetryCount = autoSettlementRetryCount;
    }

    public void setReconciliationOriginalTransType(TransType reconciliationOriginalTransType) {
        this.reconciliationOriginalTransType = reconciliationOriginalTransType;
    }

    public void setShiftTotals(ShiftTotals shiftTotals) {
        this.shiftTotals = shiftTotals;
    }

    public enum MaskType {
        CUSTOMER_MASK,
        MERCH_MASK,
        REPORT_MASK,
        EXTERNAL_APP_MASK,
    }

    public String getMaskedPan(MaskType mask, PayCfg cfg) {
        String pan = card.getPan();

        if (isReversal()) {
            TransRec original = TransRecManager.getInstance().getTransRecDao().getByReceiptNumber(audit.getReversalReceiptNumber());
            if (original != null) {
                pan = original.getCard().getPan();
            }
        }

        if (Util.isNullOrEmpty(pan)) {
            pan = card.getMaskedPan();
        }

        if (Util.isNullOrEmpty(pan)) {
            return "****************";
        }

        StringBuilder maskedPan = new StringBuilder();

        String maskCfg;
        switch (mask) {
            case CUSTOMER_MASK:
                maskCfg = card.getCardsConfig(cfg).getCustomerPanMask();
                break;
            case MERCH_MASK:
                maskCfg = card.getCardsConfig(cfg).getMerchantPanMask();
                break;
            case REPORT_MASK:
            case EXTERNAL_APP_MASK:
                maskCfg = "001";
                break;
            default:
                maskCfg = "000";
                break;
        }

        // If set Mask section 1 / 2 / 3
        boolean s1 = (maskCfg.substring(0, 1).compareTo("0") == 0);
        boolean s2 = (maskCfg.substring(1, 2).compareTo("0") == 0);
        boolean s3 = (maskCfg.substring(2, 3).compareTo("0") == 0);

        if (s1) {
            maskedPan.append("******");
        } else {
            maskedPan.append(pan, 0, 6);
        }

        String midPan = pan.substring(6, pan.length() - 4);
        if (s2) {
            int i;
            for (i = 0; i < midPan.length(); i++) {
                maskedPan.append("*");
            }
        } else {
            maskedPan.append(midPan);
        }

        if (s3) {
            maskedPan.append("****");
        } else {
            maskedPan.append(pan.substring(pan.length() - 4));
        }

        return maskedPan.toString();
    }

    public String getMaskedPan(PayCfg cfg) {
        return getMaskedPan(MaskType.MERCH_MASK, cfg);
    }

    @SuppressWarnings("java:S3457")  // Format specifiers should be used instead of string concatenation
    public String getPANHash(PayCfg cfg) {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("MD5");
            digest.update(getMaskedPan(cfg).getBytes(StandardCharsets.US_ASCII), 0, getMaskedPan(cfg).length());
            byte[] magnitude = digest.digest();
            BigInteger bi = new BigInteger(1, magnitude);

            // In C, we could do the following with a variable-width format specifier, like this:  "("%0*x", (magnitude.length << 1), bi)".
            // But java apparently does not support this, so string-concatenation is the best we can do.
            return String.format("%0" + (magnitude.length << 1) + "x", bi);
        } catch (NoSuchAlgorithmException e) {
            Timber.w(e);
        }
        return "";
    }

    public boolean canRefer(PayCfg config) {
        return getTransType().supportsReferral && !(card.isAmexCard(config) && card.isCtlsMsr());
    }

    public String getBestTerminalId(String cfgTid) {
        User activeUser = UserManager.getActiveUser();
        if (activeUser != null && !Util.isNullOrEmpty(activeUser.getTerminalId())) {
            return activeUser.getTerminalId();
        } else if (cfgTid != null) {
            return cfgTid;
        }

        return "";
    }

    public void save() {
        Timber.d("save...");

        // if financial txn and no receipt number allocated, give this one a new/unique value
        if ((audit.getReceiptNumber() == -1) && !transType.adminTransaction) {
            audit.setReceiptNumber(ReceiptNumber.getNewValue());
        }

        //Don't Save Test Connects or logon
        if (TESTCONNECT == transType || AUTO_LOGON == transType) {
            return;
        }

        if (protocol.getBatchNumber() == -1) {
            protocol.setBatchNumber(BatchNumber.getCurValue());
            if (reconciliation != null) {
                reconciliation.setBatchNumber(protocol.getBatchNumber());
            }
        }

        updateReversalState();

        // do the actual save
        if (uid == 0) {
            uid = (int) TransRecManager.getInstance().getTransRecDao().insert(this);
            Timber.i("New Saved Trans Rec UID:%s", uid);
        } else {
            TransRecManager.getInstance().getTransRecDao().update(this);
            Timber.i("Updated Trans Rec UID:%s", uid);
        }

        // save the matching values for the recs
        if ((isReconciliation() || isPreReconciliation()) && reconciliation != null) { //TODO: Remove the null check? we might want this to throw
            reconciliation.save(this);
        }
    }

    public boolean approvedAndIncludeInReconciliation() {
        return transType.includeInReconciliation && isApproved();
    }

    public boolean isVas() {
        return (!Util.isNullOrEmpty(audit.getVirtualMid()) ||
                !Util.isNullOrEmpty(audit.getVirtualTid()));
    }

    public String getVasName() {
        // TODO: Locale here is going to be redundant/might create a bug
        return String.format(Locale.getDefault(), "%s.%s", getTransType().getDisplayName(), Util.isNullOrEmpty(audit.getVirtualName()) ? "VAS" : audit.getVirtualName());
    }

    public String generateEFTPaymentID(String cfgStid) {
        // generate our own version
        // 3995\/172\/03012019144306002
        if (protocol.getStan() == 0)
            protocol.setStan(Stan.getNewValue());

        String dateTime = Util.getDateTimeAsString("ddMMyyyyHHmmssSSS", audit.getLastTransmissionDateTime());

        return String.format("%s/%s/%s", getBestTerminalId(cfgStid), protocol.getStan(), dateTime);
    }

    public String checkEftPaymentId(String cfgStid) {

        if (protocol.getEftPaymentId() == null || protocol.getEftPaymentId().isEmpty()) {
            protocol.setEftPaymentId(generateEFTPaymentID(cfgStid));
        }

        return protocol.getEftPaymentId();
    }

    public String getEftPaymentIdShort(String configStid) {

        String eftPaymentId = checkEftPaymentId(configStid);
        int index;

        index = eftPaymentId.lastIndexOf("/");
        if (index != -1) {
            String shortPaymentId = eftPaymentId.substring(0, index);
            eftPaymentId = shortPaymentId.replace("\\", "");
        }
        return eftPaymentId;
    }

    public String calculateRetRefNumber() {
        if (Engine.getCustomer() == null) {
            return "";
        }
        return Engine.getCustomer().calculateRetRefNumber(this);
    }

    public boolean isApprovedOrDeferred() {
        return (isApproved() || isDeferredAuth());
    }


    public static int getCountOfAllBeforeLastZReport() {
        int count;
        String rawSQL;

        long lastZReportDateTime = getDateTimeLastZReport();
        if (lastZReportDateTime > 0) {
            rawSQL = "SELECT COUNT (*) as count FROM transRecs WHERE  audit_transDateTime < " + lastZReportDateTime;
        } else {

            rawSQL = "SELECT COUNT (*) as count FROM transRecs";
        }
        SimpleSQLiteQuery query = new SimpleSQLiteQuery(rawSQL);
        count = TransRecManager.getInstance().getTransRecDao().executeIntQuery(query);
        return count;
    }


    public static long getDateTimeLastZReport() {
        String rawSQL = "Select audit_lastTransmissionDateTime from transRecs where TransType = " + TransType.RECONCILIATION.ordinal() + " ORDER BY audit_lastTransmissionDateTime DESC LIMIT 1";
        SimpleSQLiteQuery query = new SimpleSQLiteQuery(rawSQL);
        TransRec lastTransmissionDateTimeMap = TransRecManager.getInstance().getTransRecDao().executeTransRecQuery(query);
        if (lastTransmissionDateTimeMap != null) {
            return lastTransmissionDateTimeMap.audit.getLastTransmissionDateTime();
        }
        return 0;
    }

    public static void deleteTransUptoMaxCount(int maximumDeleteCount) {
        String where = "Select uid from transRecs where (Cancelled = 1 OR prot_messageStatus = " + FINALISED.ordinal() + " OR prot_messageStatus = " + FINALISED_AND_REVERSED.ordinal() + " ) AND summedOrReced = 1 AND prot_paxstoreUploaded = 1 ORDER BY audit_transDateTime LIMIT " + maximumDeleteCount;
        SimpleSQLiteQuery query = new SimpleSQLiteQuery(where);
        List<Integer> condition = TransRecManager.getInstance().getTransRecDao().executeListQuery(query);
        TransRecManager.getInstance().getTransRecDao().deleteTransByCondition(condition);
    }

    /**
     * Checks {@link TCard} & {@link TProtocol} signature flags
     *
     * @return true if either one is true
     */
    public boolean isSignatureRequired() {
        return ((card != null && card.isSignatureVerificationRequired()) ||
                (protocol != null && protocol.isSignatureRequired()));
    }

    /**
     * Cancel a transaction & set all relevant fields
     *
     * @param event {@link PositiveTransEvent} object
     */
    public void cancelTransaction(PositiveTransEvent event) {
        if (!isFinalised()) {
            setCancelled(true);

            if (transEvent != null) {
                // Copy access the POS initialised values this overrides the previous event
                event.setCutReceipt(transEvent.isCutReceipt());
                event.setUseTerminalPrinter(transEvent.isUseTerminalPrinter());
                event.setPosPrintingSync(transEvent.isPosPrintingSync());
                event.setAutoPrint(transEvent.isAutoPrint());

                setTransEvent(transEvent);
            }
        }
    }

    /**
     * Will count the number of refunds since midnight
     *
     * @return refundCount
     */
    public static long getRefundCountFromMidnight() {
        List<TransRec> transRecList = TransRecManager.getInstance().getTransRecDao().getTransRecsUntilATime(REFUND_AUTO,REFUND,REFUND_MOTO,REFUND_MOTO_AUTO,
                Util.calculateTimeInMillis(0, 0));

        return transRecList != null ? transRecList.size() : 0;
    }

    /**
     * Will return the total refund amount since midnight
     *
     * @return refundAmount
     */
    public static long getRefundAmountFromMidnight() {
        List<TransRec> transRecList = TransRecManager.getInstance().getTransRecDao().getTransRecsUntilATime(REFUND_AUTO,REFUND,REFUND_MOTO,REFUND_MOTO_AUTO,
                Util.calculateTimeInMillis(0, 0));
        long total = 0;

        for (TransRec trans : transRecList) {
            total += trans.getAmounts().getTotalAmount();
        }

        return total;
    }

    // Helper function, used to reduce Sonar complexity measurement.
    private static Integer findExpiryStep1(Integer linklyBinNumber, List<LinklyBinNumber> linklyBinNumberList) {
        Integer expiryDays = null;
        if (linklyBinNumber != null) {
            for (LinklyBinNumber lbn : linklyBinNumberList) {
                if (Integer.parseInt(lbn.getCard_bin_number()) == linklyBinNumber && !lbn.getPreauth_expiryDays().isEmpty() && lbn.getPreauth_expiryDays() != null) {
                    expiryDays = Integer.parseInt(lbn.getPreauth_expiryDays());
                    break;
                }

            }
        }
        return expiryDays;
    }

    /**
     * return preauth expiry period (days) for given linkly BIN number from configuration, or hard-coded value if misconfigured
     *
     * @param linklyBinNumber input linkly BIN number
     * @return preauth expiry period for this BIN (days)
     */
    public static int findExpiryDaysByBinNumber(Integer linklyBinNumber, PayCfg payCfg) {
        Integer expiryDays = null;
        if (payCfg != null) {
            List<LinklyBinNumber> linklyBinNumberList = payCfg.getLinklyBinNumbers(); // getting list of linkly bin number defined in config
            try {
                //step 1 - try to find expiry days set for particular card scheme bin
                expiryDays = findExpiryStep1(linklyBinNumber, linklyBinNumberList);

                //step 2 - if expiry days not found above for card scheme, use default from config
                if (expiryDays == null) {
                    expiryDays = Integer.parseInt(payCfg.getPreAuthExpiry_default());
                }
            } catch (Exception e) {
                Timber.w(e);
                Timber.e("Error : no expiry days for bin or any default set in config");
            }
        }
        // step 3 - if all of above failed, set hard-coded default
        if (expiryDays == null) {
            expiryDays = DEFAULT_PREAUTH_EXPIRY_DAYS;
        }
        return expiryDays;
    }

    /**
     * return transaction expiry date/time from expiry days and in same unit
     *
     * @param transDateTime input transaction finished date time and expiry days we get from above method
     * @return expiry date/time for this transaction
     */
    public static long getTransExpiryTime(long transDateTime, int expiryDays) {
        Calendar cEnd = Calendar.getInstance();
        cEnd.setTimeInMillis(transDateTime); //converting transaction time to timestamp
        cEnd.set(Calendar.HOUR_OF_DAY, 23);
        cEnd.set(Calendar.MINUTE, 59);
        cEnd.set(Calendar.SECOND, 59);
        cEnd.set(Calendar.MILLISECOND, 999);// setting transaction time to end of the day
        cEnd.add(Calendar.DATE, expiryDays); // add expiry day to transaction time to get transaction expiry date with time as end of day
        return cEnd.getTimeInMillis();

    }

    /**
     * copies required data elements from the trans rec we're reversing into the current reversal txn record
     */
    public void copyFromOriginalTxnForReversal(TransRec txnToReverse) {
        if (transEvent != null) {
            // set receipt number in transEvent as it is needed for reversals during PopulateTransaction Action
            transEvent.setReceiptNumberForReversal(txnToReverse.audit.getReceiptNumber());
        }

        // set reversal receipt number
        audit.setReversalReceiptNumber(txnToReverse.getAudit().getReceiptNumber());

        Timber.i("reversing txn receipt number %d", txnToReverse.getAudit().getReversalReceiptNumber());

        // set txn amounts, deep copy card object
        setAmounts(TAmounts.copy(txnToReverse.getAmounts()));

        // deep copy the card object from retrieved txn into our current active txn
        setCard(TCard.copy(txnToReverse.getCard()));

        // zero out any CVM flags to avoid CVM required
        card.setCvmType(TCard.CvmType.NO_CVM);

        // set cardholder present to FALSE for these RFN (cardholder not present) completions, so we don't prompt for PIN
        card.setCardholderPresent(false);

        // copy security data because this is where encrypted card details are stored
        setSecurity(TSec.copy(txnToReverse.getSecurity()));

        // copy some other fields such as RRN, auth code, account type
        protocol.setAuthCode(txnToReverse.protocol.getAuthCode());
        protocol.setRRN(txnToReverse.protocol.getRRN());
        protocol.setAccountType(txnToReverse.protocol.getAccountType());
    }

    /**
     * logs receipt data to logcat log using .e (error) severity level
     * useful for post-mortem diagnostics purposes
     *
     * @param receipt - input receipt
     */
    public static void printReceiptToDebug(PositiveReceiptResponse receipt){
        if( receipt == null || receipt.getNumberOfLines() == 0 || receipt.getReceiptData() == null || receipt.getReceiptData().length == 0){
            return;
        }

        String[] receiptLines = receipt.getReceiptData();
        Timber.e("***** receipt start *****");
        for( String line: receiptLines ){
            Timber.e(line);
        }
        Timber.e("****** receipt end ******");
    }

    public boolean isVerifiedByPinRequiredOnReceipt() {
        return card.isOfflinePinVerified()
                || (card.isOnlinePinVerificationRequired()
                && !(protocol.isAuthMethodOfflineApproved()
                || protocol.isSubjectToProcessingAsAdvice())
        );
    }
}
