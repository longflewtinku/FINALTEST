package com.linkly.libengine.env;

import static com.linkly.libengine.env.EnvVarManager.getEnvVarDao;
import static com.linkly.libengine.env.EnvVarManager.getInstance;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import timber.log.Timber;

@Entity(tableName = "envvars", indices = {@Index( value = {"name"}, unique = true )})
public class EnvVar {
    @Ignore
    private static final String TAG = "EnvVar";

    @PrimaryKey(autoGenerate = true)
    public int uid;
    public String name; // name is a primary key, and must be unique
    public String value;

    @Ignore
    public EnvVar(){
    }

    public EnvVar( String name, String value ) {
        this.name = name;
        this.value = value;
    }

    public static String getEnvValueString( String name ) {
        // call constructor if required
        getInstance();
        if( getEnvVarDao() == null ){
            Timber.e("getEnvValueString %s FAILED, envVarDao not configured", name);
            return null;
        }

        EnvVar val;
        try {
            val = getEnvVarDao().findByName(name);
        } catch (Exception e ) {
            Timber.i( e.getMessage());
            val = null;
        }

        if( val == null )
            return "";

        return val.getValue();
    }

    public static Integer getEnvValueInteger( String name ) {
        String val = getEnvValueString( name );
        if( val != null && val.length() > 0 )
            return Integer.parseInt(val);
        else
            return 0;
    }

    public static boolean getEnvValueBoolean( String name ) {
        String val = getEnvValueString( name );
        if( val != null && val.compareTo("true") == 0 )
            return true;
        else
            return false;
    }

    public static Integer getIntegerAutoIncrement( String name, Integer maximum ) {
        Integer val = getEnvValueInteger( name );

        val++;

        if( val > maximum ) {
            val = 1;
        }

        // increment then return new value
        setEnvValue( name, val );
        return val;
    }

    public static void setEnvValue( String name, String value ) {
        // call constructor if required
        getInstance();
        if( getEnvVarDao() == null ){
            Timber.e("setEnvValue %s, %s FAILED, envVarDao not configured", name, value);
            return;
        }

        try {
            EnvVar val = getEnvVarDao().findByName(name);
            if (val == null) {
                // insert new record
                getEnvVarDao().insert(new EnvVar(name, value));
            } else {
                // update value in record
                val.setValue(value);
                getEnvVarDao().update(val);
            }
        } catch (Exception e) {
            Timber.i( e.getMessage());
        }
    }

    public static void setEnvValue( String name, Integer value ) {
        String valString = value.toString();
        setEnvValue( name, valString );
    }

    public static void setEnvValue( String name, boolean value ) {
        String valString = value ? "true" : "false";
        setEnvValue( name, valString );
    }

    public static void deleteEnvValue( String name ) {
        // call constructor if required
        getInstance();
        if( getEnvVarDao() == null ){
            Timber.e("deleteEnvValue %s FAILED, envVarDao not configured", name);
            return;
        }

        try {
            EnvVar val = getEnvVarDao().findByName(name);
            if (val != null) {
                getEnvVarDao().delete(val);
            }
        } catch (Exception e) {
            Timber.i( e.getMessage());
        }
    }

    public static void deleteAll() {
        // call constructor if required
        getInstance();

        try {
            getEnvVarDao().deleteAll();
        } catch( Exception e ){
            Timber.i( e.getMessage());
        }
    }

    public int getUid() {
        return this.uid;
    }

    public String getName() {
        return this.name;
    }

    public String getValue() {
        return this.value;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setValue(String value) {
        this.value = value;
    }
}