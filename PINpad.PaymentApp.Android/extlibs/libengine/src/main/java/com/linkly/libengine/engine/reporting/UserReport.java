package com.linkly.libengine.engine.reporting;

import java.util.ArrayList;

public class UserReport {

    private ArrayList<UserData> userDataItems;

    public UserReport() {
        userDataItems = new ArrayList<UserData>();
    }

    public ArrayList<UserData> getUserDataItems() {
        return this.userDataItems;
    }


    public class UserData {
        String userId;
        String userName;
        String count;
        long amount;

        public UserData(String userId, String userName, String count, long amount) {
            this.userId = userId;
            this.userName = userName;
            this.count = count;
            this.amount = amount;

        }

        public String getUserId() {
            return this.userId;
        }

        public String getUserName() {
            return this.userName;
        }

        public String getCount() {
            return this.count;
        }

        public long getAmount() {
            return this.amount;
        }
    }

}
