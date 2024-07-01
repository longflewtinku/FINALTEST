package com.linkly.libengine.engine.reporting;

import android.content.Context;
import android.database.SQLException;

import androidx.annotation.NonNull;
import androidx.room.Room;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.linkly.libmal.MalFactory;

import java.util.Objects;

import timber.log.Timber;

public class ReconciliationManager {
    private static final String TAG = ReconciliationManager.class.getSimpleName();
    private static ReconciliationManager instance = null;
    private static ReconciliationDatabase reconciliationDatabase;
    public static ReconciliationDao reconciliationDao;

    public static ReconciliationManager getInstance() {
        if (instance == null) {
            // TODO: Fix this Malfactory get instance.
            instance = new ReconciliationManager(MalFactory.getInstance().getMalContext());
        }
        return instance;
    }

    ReconciliationManager(Context context) {
        // load/create new database
        reconciliationDatabase = Room.databaseBuilder(
                Objects.requireNonNull(context),
                ReconciliationDatabase.class,
                "Recs.db" )
                .addMigrations( MIGRATION_1_2 )
                .build();

        reconciliationDao = reconciliationDatabase.reconciliationDao();
    }

    // DO NOT CHANGE
    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate( @NonNull SupportSQLiteDatabase database ) {
            Timber.w( "Migrating Rec database from 1 - 2" );

            try{
                database.execSQL( "ALTER TABLE recs ADD COLUMN surcharge TEXT" );

            } catch ( SQLException e ) {
                Timber.e( "Migration Failed" );
                Timber.w(e);
            }
        }
    };
}
