package com.linkly.libengine.engine.transactions;

import androidx.room.Room;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.linkly.libengine.engine.Engine;
import com.linkly.libmal.MalFactory;
import com.linkly.libsecapp.IP2PSec;
import com.linkly.libsecapp.P2PLib;
import com.pax.dal.entity.ECheckMode;

import timber.log.Timber;

public class TransRecManager {
    public static final byte TRANS_REC_DB_ENCRYPTION_KEY_ID = 4;
    private static TransRecManager instance = null;
    private final TransRecDao transRecDao;

    public TransRecDao getTransRecDao() {
        return transRecDao;
    }

    public static TransRecManager getInstance() {
        if (instance == null) {
            instance = new TransRecManager();
        }
        return instance;
    }

    // handy example for when we have to add extra columns when upgrading dbs
    /// NOTE FINALISED DO NOT CHANGE
    /// NOTE FINALISED DO NOT CHANGE
    /// NOTE FINALISED DO NOT CHANGE
    /// NOTE FINALISED DO NOT CHANGE
    /// NOTE FINALISED DO NOT CHANGE
    /// NOTE FINALISED DO NOT CHANGE
    /// NOTE FINALISED DO NOT CHANGE
    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            Timber.i( "migrate 1 - 2");
            try {
                // add message status column
                database.execSQL("ALTER TABLE transrecs ADD COLUMN prot_aavResult INTEGER"); //
                database.execSQL("ALTER TABLE transrecs ADD COLUMN prot_mastercardAssignedID TEXT"); //
                database.execSQL("ALTER TABLE transrecs ADD COLUMN prot_uniqueTransactionResponseReferenceNumber TEXT"); //
                database.execSQL("ALTER TABLE transrecs ADD COLUMN prot_paymentAccountReference TEXT"); //
                database.execSQL("ALTER TABLE transrecs ADD COLUMN prot_serverTransDateTime TEXT"); //
                database.execSQL("ALTER TABLE transrecs ADD COLUMN prot_voidItemNumber INTEGER"); //
                database.execSQL("ALTER TABLE transrecs ADD COLUMN prot_schemeData TEXT"); //
                database.execSQL("ALTER TABLE transrecs ADD COLUMN audit_adviceRetryCount INTEGER"); //
            } catch ( Exception e) {
                Timber.w(e);
                Timber.e( "migrate 1 - 2 Failed");
            }
        }
    };


    /// NOTE FINALISED DO NOT CHANGE
    /// NOTE FINALISED DO NOT CHANGE
    /// NOTE FINALISED DO NOT CHANGE
    /// NOTE FINALISED DO NOT CHANGE
    /// NOTE FINALISED DO NOT CHANGE
    /// NOTE FINALISED DO NOT CHANGE
    /// NOTE FINALISED DO NOT CHANGE
    static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // add message status column
            Timber.i( "migrate 2 - 3");
            try {
                database.execSQL("ALTER TABLE transrecs ADD COLUMN deferredAuth INTEGER NOT NULL default 0");  //
                database.execSQL("ALTER TABLE transrecs ADD COLUMN prot_paxstoreUploaded INTEGER"); //
                database.execSQL("ALTER TABLE transrecs ADD COLUMN sec_ksn TEXT"); //
                database.execSQL("ALTER TABLE transrecs ADD COLUMN sec_pinBlock TEXT"); //
                database.execSQL("ALTER TABLE transrecs ADD COLUMN sec_pinBlockKsn TEXT"); //
            } catch ( Exception e) {
                Timber.w(e);
                Timber.e( "migrate 2 - 3 Failed");
            }
        }
    };

    static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // add message status column
            Timber.i( "migrate 3 - 4");
            try {
                database.execSQL("ALTER TABLE transrecs ADD COLUMN prot_merchantEmailToUpload INTEGER");
                database.execSQL("ALTER TABLE transrecs ADD COLUMN prot_customerEmailToUpload INTEGER");
                database.execSQL("ALTER TABLE transrecs ADD COLUMN prot_mailCustomerAddress TEXT");
            } catch ( Exception e) {
                Timber.w(e);
                Timber.e( "migrate 3 - 4 Failed");
            }
        }
    };

    static final Migration MIGRATION_4_5 = new Migration(4, 5) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // add message status column
            Timber.i( "migrate 4 - 5");
            try {
                database.execSQL("ALTER TABLE transrecs ADD COLUMN card_arc TEXT");
            } catch ( Exception e) {
                Timber.w(e);
                Timber.e( "migrate 4 - 5 Failed");
            }
        }
    };

    static final Migration MIGRATION_5_6 = new Migration(5, 6) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // add message status column
            Timber.i( "migrate 5 - 6");
            try {
                database.execSQL("ALTER TABLE transrecs ADD COLUMN card_ctlsMcrPerformed INTEGER");
            } catch ( Exception e) {
                Timber.w(e);
                Timber.e( "migrate 5 - 6 Failed");
            }
        }
    };

    static final Migration MIGRATION_6_7 = new Migration(6, 7) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // add message status column
            Timber.i( "migrate 6 - 7");
            try {
                database.execSQL("ALTER TABLE transrecs ADD COLUMN prot_signatureRequired INTEGER");  //
                database.execSQL("ALTER TABLE transrecs ADD COLUMN audit_signatureChecked INTEGER");  //
            } catch ( Exception e) {
                Timber.w(e);
                Timber.e( "migrate 6 - 7 Failed");
            }
        }
    };

    static final Migration MIGRATION_7_8 = new Migration(7, 8) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // add message status column
            Timber.i( "migrate 7 - 8");
            try {
                database.execSQL("ALTER TABLE transrecs ADD COLUMN sec_cvv TEXT"); //
            } catch ( Exception e) {
                Timber.w(e);
                Timber.e( "migrate 7 - 8 Failed");
            }
        }
    };

    static final Migration MIGRATION_8_9 = new Migration(8, 9) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // add message status column
            Timber.i( "migrate 8 - 9");
            try {
                database.execSQL("ALTER TABLE transrecs ADD COLUMN preauthUid INTEGER");
            } catch ( Exception e) {
                Timber.w(e);
                Timber.e( "migrate 8 - 9 Failed");
            }
        }
    };

    static final Migration MIGRATION_9_10 = new Migration(9, 10) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // add message status column
            Timber.i( "migrate 9 - 10");
            try {
                database.execSQL("ALTER TABLE transrecs ADD COLUMN prot_posResponseCode TEXT");
            } catch ( Exception e) {
                Timber.w(e);
                Timber.e( "migrate 9 - 10 Failed");
            }
        }
    };

    static final Migration MIGRATION_10_11 = new Migration(10, 11) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // add message status column
            Timber.i( "migrate 10 - 11");
            try {
                database.execSQL("ALTER TABLE transrecs ADD COLUMN receipts TEXT");
                database.execSQL("ALTER TABLE transrecs ADD COLUMN tagDataToPos TEXT");
            } catch ( Exception e) {
                Timber.w(e);
                Timber.e( "migrate 10 - 11 Failed");
            }
        }
    };

    static final Migration MIGRATION_11_12 = new Migration(11, 12) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // add message status column
            Timber.i( "migrate 11 - 12");
            try {
                database.execSQL("ALTER TABLE transrecs ADD COLUMN card_linklyBinNumber INTEGER");
            } catch ( Exception e) {
                Timber.w(e);
                Timber.e( "migrate 11 - 12 Failed");
            }
        }
    };

    static final Migration MIGRATION_12_13 = new Migration(12, 13) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // add message status column
            Timber.i( "migrate 12 - 13");
            try {
                database.execSQL("ALTER TABLE transrecs ADD COLUMN startedInOfflineMode INTEGER");
                database.execSQL("ALTER TABLE transrecs ADD COLUMN card_emvCdoPerformed INTEGER");
            } catch ( Exception e) {
                Timber.w(e);
                Timber.e( "migrate 12 - 13 Failed");
            }
        }
    };

    static final Migration MIGRATION_13_14 = new Migration(13, 14) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // add message status column
            Timber.i( "migrate 13 - 14");
            try {
                database.execSQL("ALTER TABLE transrecs ADD COLUMN sec_expiryDateChip TEXT");
            } catch ( Exception e) {
                Timber.w(e);
                Timber.e( "migrate 13 - 14 Failed");
            }
        }
    };

    static final Migration MIGRATION_14_15 = new Migration(14, 15) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // add message status column
            Timber.i( "migrate 14 - 15");
            try {
                database.execSQL("ALTER TABLE transrecs ADD COLUMN prot_adviceResponseCode TEXT");
            } catch ( Exception e) {
                Timber.w(e);
                Timber.e( "migrate 14 - 15 Failed");
            }
        }
    };

    public void loadDbEncryptionKey() {
        Timber.i( "load DB encryption key");
        byte[] tdk = new byte[] {(byte) 0x76, (byte) 0x8A, (byte) 0x5B, (byte) 0x4E, (byte) 0xFB, (byte) 0x48, (byte) 0xBC, (byte) 0x30, (byte) 0xC1, (byte) 0xCC, (byte) 0x3F, (byte) 0xD2, (byte) 0x25, (byte) 0x22, (byte) 0x71, (byte) 0x4D};
        if( P2PLib.getInstance().getIP2PSec() != null )
            P2PLib.getInstance().getIP2PSec().writeKey(IP2PSec.KeyType.TMK, (byte) 0, IP2PSec.KeyType.TDK, TRANS_REC_DB_ENCRYPTION_KEY_ID, tdk, ECheckMode.KCV_NONE, null);
    }

    private TransRecManager() {
        Timber.i( "TransRecManager constructor");
        // load/create new database
        TransRecDatabase transRecDatabase = Room.databaseBuilder(MalFactory.getInstance().getMalContext(), TransRecDatabase.class, "TransRec.db")
                .allowMainThreadQueries()
                .addMigrations(MIGRATION_1_2,
                        MIGRATION_2_3,
                        MIGRATION_3_4,
                        MIGRATION_4_5,
                        MIGRATION_5_6,
                        MIGRATION_6_7,
                        MIGRATION_7_8,
                        MIGRATION_8_9,
                        MIGRATION_9_10,
                        MIGRATION_10_11,
                        MIGRATION_11_12,
                        MIGRATION_13_14,
                        MIGRATION_14_15)
                .build();
        transRecDao = transRecDatabase.transRecDao();
    }
}
