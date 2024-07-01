package com.linkly.libengine.engine.reporting;

public class UserPasswordData {
        String userId;
        String userName;
        String password;

        public UserPasswordData(){

        }
        public UserPasswordData(String userId, String userName, String pwd) {
            this.userId = userId;
            this.userName = userName;
            this.password = pwd;

        }

    public String getUserId() {
        return this.userId;
    }

    public String getUserName() {
        return this.userName;
    }

    public String getPassword() {
        return this.password;
    }
}

