package com.linkly.libengine.env;

import androidx.room.Room;

import com.linkly.libmal.MalFactory;

public class EnvVarManager {
    private static EnvVarManager instance = null;
    private static EnvVarDao envVarDao = null;

    public static EnvVarManager getInstance() {
        if (instance == null) {
            instance = new EnvVarManager();
        }
        return instance;
    }

    public static void setEnvVarDao(EnvVarDao value){
        envVarDao = value;
    }

    public static EnvVarDao getEnvVarDao(){
        return envVarDao;
    }

    EnvVarManager() {
        if( MalFactory.getInstance().getMalContext() != null ) {
            // load/create new database
            EnvVarDatabase envVarDb = Room.databaseBuilder(MalFactory.getInstance().getMalContext(), EnvVarDatabase.class, "EnvVars.db").allowMainThreadQueries().build();
            setEnvVarDao(envVarDb.envVarDao());
        }
    }


}
