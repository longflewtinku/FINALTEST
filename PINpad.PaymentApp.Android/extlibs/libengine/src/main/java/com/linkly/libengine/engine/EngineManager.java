package com.linkly.libengine.engine;

import com.linkly.libui.IUIDisplay.String_id;

import timber.log.Timber;


public class EngineManager {
    private static EngineManager instance = null;
    private EngineManager() {
        super();
    }

    public static EngineManager getInstance() {
        if (instance == null) {
            instance = new EngineManager();
        }
        return instance;
    }

    public enum TransClass {
        NOT_SET,
        DEBIT,
        CREDIT,
        ADMIN,
        BALANCE
    }

    public enum TransType {
        // VERY IMPORTANT!!! DO NOT CHANGE THE ORDER OF THESE OR DELETE FROM THE MIDDLE. ALWAYS ADD AT THE END.
        // Reason for this: The enum ordinal value is saved in the trans database, so when upgrading software on terminals, the mapping can be broken if the order changes or items are deleted
        SALE                    (String_id.STR_SALE,               true,  true,  true,  false, false, TransClass.DEBIT, true ),
        SALE_AUTO               (String_id.STR_SALE,               true,  true,  true,  false, true, TransClass.DEBIT, true ),
        SALE_MOTO               (String_id.STR_SALE,               true,  true,  true,  false, false, TransClass.DEBIT, true ),
        SALE_MOTO_AUTO          (String_id.STR_SALE,               true,  true,  true,  false, true, TransClass.DEBIT, true ),
        CARD_NOT_PRESENT        (String_id.STR_CARD_NOT_PRESENT,   true,  true,  true,  false, false, TransClass.DEBIT, true ),
        CARD_NOT_PRESENT_REFUND (String_id.STR_REFUND_NOT_PRESENT, true,  true,  true,  false, false, TransClass.CREDIT, false ),
        CASH                    (String_id.STR_CASH,               true,  true,  false, false, false, TransClass.DEBIT, false ),
        CASH_AUTO               (String_id.STR_CASH,               true,  true,  false, false, true, TransClass.DEBIT, false ),
        MANUAL_REVERSAL         (String_id.STR_REVERSAL,           false, true,  false, false, false, TransClass.CREDIT, false ),
        MANUAL_REVERSAL_AUTO    (String_id.STR_REVERSAL,           false, true,  false, false, true, TransClass.CREDIT, false ),
        CASHBACK                (String_id.STR_CASHBACK,           true,  true,  true,  false, false, TransClass.DEBIT, true ),
        CASHBACK_AUTO           (String_id.STR_CASHBACK,           true,  true,  true,  false, true, TransClass.DEBIT, true ),
        PREAUTH                 (String_id.STR_PRE_AUTH,           true,  false, false, false, false, TransClass.DEBIT, false ),
        PREAUTH_AUTO            (String_id.STR_PRE_AUTH,           true,  false, false, false, true, TransClass.DEBIT, false ),
        PREAUTH_MOTO            (String_id.STR_PRE_AUTH,           true,  false, false, false, false, TransClass.DEBIT, false ),
        PREAUTH_MOTO_AUTO       (String_id.STR_PRE_AUTH,           true,  false, false, false, true, TransClass.DEBIT, false ),
        PREAUTH_CANCEL          (String_id.STR_PRE_AUTH_CANCEL,    false, false, false, false, false, TransClass.CREDIT, false ),
        PREAUTH_CANCEL_AUTO     (String_id.STR_PRE_AUTH_CANCEL,    false, false, false, false, true, TransClass.CREDIT, false ),
        COMPLETION              (String_id.STR_PRE_AUTH_COMPLETION,         false,  true,  false, false, false, TransClass.DEBIT, false ),
        COMPLETION_AUTO         (String_id.STR_PRE_AUTH_COMPLETION,         false,  true,  false, false, true, TransClass.DEBIT, false ),
        TOPUPPREAUTH            (String_id.STR_TOPUP_PRE_AUTH,     true,  false, false, false, false, TransClass.DEBIT, false ),
        TOPUPCOMPLETION         (String_id.STR_TOPUP_COMPLETION,   true,  true,  false, false, false, TransClass.DEBIT, false ),
        REFUND                  (String_id.STR_REFUND,             true,  true,  false, false, false, TransClass.CREDIT, false ),
        REFUND_AUTO             (String_id.STR_REFUND,             true,  true,  false, false, true, TransClass.CREDIT, false ),
        BALANCE                 (String_id.STR_BALANCE,            false, false, false, false, false, TransClass.BALANCE, false ),
        DEPOSIT                 (String_id.STR_DEPOSIT,            true,  true,  false, false, false, TransClass.CREDIT, false ),
        OFFLINESALE             (String_id.STR_OFFLINE_SALE,       true,  true,  true,  false, false, TransClass.DEBIT, true ),
        OFFLINECASH             (String_id.STR_OFFLINE_CASH,       true,  true,  true,  false, false, TransClass.DEBIT, false ),
        TESTCONNECT             (String_id.STR_TEST_CONNECT,       false, false, false, true,  false, TransClass.ADMIN, false ),
        TESTCYCLEKEYS           (String_id.STR_TEST_CYCLE_KEYS,    false, false, false, true,  false, TransClass.ADMIN, false ),
        RECONCILIATION          (String_id.STR_RECONCILIATION,     false, false, false, true,  false, TransClass.ADMIN, false ),
        RECONCILIATION_AUTO     (String_id.STR_RECONCILIATION,     false, false, false, true,  true, TransClass.ADMIN, false ),
        SUMMARY                 (String_id.STR_SUMMARY,            false, false, false, true,  false, TransClass.ADMIN, false ),
        SUMMARY_AUTO            (String_id.STR_SUMMARY,            false, false, false, true,  true, TransClass.ADMIN, false ),
        DCCRATES                (String_id.STR_DCC_RATES,          false, false, false, true,  false, TransClass.ADMIN, false ),
        GRATUITY                (String_id.STR_GRATUITY,           false, false, false, true,  false, TransClass.DEBIT, false ),
        AUTO_LOGON              (String_id.STR_LOGON,              false, false, false, true,  true, TransClass.ADMIN , false ),
        RSA_LOGON               (String_id.STR_RSA_LOGON,          false, false, false, true,  false, TransClass.ADMIN , false ),
        LOGON                   (String_id.STR_LOGON,              false, false, false, true,  false, TransClass.ADMIN , false ),
        REFUND_MOTO             (String_id.STR_REFUND,             true,  true,  false, false, false, TransClass.CREDIT, false ),
        REFUND_MOTO_AUTO        (String_id.STR_REFUND_MOTO_AUTO,   true,  true,  true,  false, true, TransClass.CREDIT, false ),
        LAST_RECONCILIATION     (String_id.STR_LAST_RECONCILIATION,false, false, false, true,  false, TransClass.ADMIN, false ),
        LAST_RECONCILIATION_AUTO(String_id.STR_LAST_RECONCILIATION,false, false, false, true,  true, TransClass.ADMIN, false ),
        PRE_RECONCILIATION     (String_id.STR_PRE_RECONCILIATION,false, false, false, true,  false, TransClass.ADMIN, false ),
        PRE_RECONCILIATION_AUTO(String_id.STR_PRE_RECONCILIATION,false, false, false, true,  true, TransClass.ADMIN, false ),
        AUTOSETTLEMENT          (String_id.STR_RECONCILIATION,     false, false, false, true,  false, TransClass.ADMIN, false ),
        SHIFT_TOTALS            (String_id.STR_SHIFT_TOTALS,       false, false, false, true,  false, TransClass.ADMIN, false ),
        SHIFT_TOTALS_AUTO       (String_id.STR_SHIFT_TOTALS,       false, false, false, true,  true, TransClass.ADMIN, false ),
        SUB_TOTALS              (String_id.STR_SUB_TOTALS,       false, false, false, true,  false, TransClass.ADMIN, false ),
        SUB_TOTALS_AUTO         (String_id.STR_SUB_TOTALS,       false, false, false, true,  true, TransClass.ADMIN, false ),
        REPRINT_SHIFT_TOTALS    (String_id.STR_REPRINT_SHIFT_TOTALS,       false, false, false, true,  false, TransClass.ADMIN, false ),
        AUTOMATIC_SHIFT_TOTALS  (String_id.STR_SHIFT_TOTALS,       false, false, false, true,  false, TransClass.ADMIN, false ),
        ;

        public String_id displayId;
        public boolean supportsReversal;
        public boolean includeInReconciliation;
        public boolean supportsReferral;
        public boolean adminTransaction; // a transaction that does NOT affect terminal totals
        public boolean autoTransaction;
        public boolean supportsSurcharge;
        public TransClass transClass;

        TransType(String_id displayId, boolean supportsReversal, boolean includeInReconciliation, boolean supportsReferral, boolean adminTransaction, boolean autoTransaction, TransClass transClass, boolean supportsSurcharge) {
            this.displayId = displayId;
            this.supportsReversal = supportsReversal;
            this.includeInReconciliation = includeInReconciliation;
            this.supportsReferral = supportsReferral;
            this.adminTransaction = adminTransaction;
            this.autoTransaction = autoTransaction;
            this.transClass = transClass;
            this.supportsSurcharge = supportsSurcharge;
        }

        public TransType getCNPEquivalentType() {
            if (displayId.compareTo(SALE.displayId) == 0)
                return CARD_NOT_PRESENT;
            if (displayId.compareTo(REFUND.displayId) == 0)
                return CARD_NOT_PRESENT_REFUND;
            return null;
        }

        public TransClass getTransClass() {
            return this.transClass;
        }

        public String getDisplayName(){
            return Engine.getDep().getPrompt(this.displayId);
        }

        public static TransType getTransTypeByString(String func) {

            switch(func.toLowerCase()) {
                case "sale":                    return SALE;
                case "saleauto":                return SALE_AUTO;
                case "cashback":                return CASHBACK;
                case "cashbackauto":            return CASHBACK_AUTO;
                case "refund":                  return REFUND;
                case "refundauto":              return REFUND_AUTO;
                case "reversalauto":            return MANUAL_REVERSAL_AUTO;
                case "preauthauto":             return PREAUTH_AUTO;
                case "preauthmotoauto":         return PREAUTH_MOTO_AUTO;
                case "preauthcancelauto":       return PREAUTH_CANCEL_AUTO;
                case "completionauto":          return COMPLETION_AUTO;
                case "reconciliationauto":      return RECONCILIATION_AUTO;
                case "cashauto":                return CASH_AUTO;
                case "salemoto":                return SALE_MOTO;
                case "salemotoauto":            return SALE_MOTO_AUTO;
                case "refundmoto":              return REFUND_MOTO;
                case "refundmotoauto":          return REFUND_MOTO_AUTO;
                case "logonauto":               return AUTO_LOGON;
            }
            Timber.e( "Transaction Type not supported:%s", func);
            return null;
        }

    }
}

