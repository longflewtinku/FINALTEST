package com.linkly.libengine.engine.protocol.svfe;

import java.util.Hashtable;

public final class SvfeRspCodeMap {
    private static Hashtable<String, String> rspCodeMap = populateRspCodeMap();
    private static Hashtable<String, String> rspCodeErrorMap = populateRspCodeErrorMap();

    private SvfeRspCodeMap() {
    }

    public static String getResponseCode(String actionCode) {
        String rspCode = null;
        if (actionCode != null) {
            rspCode = rspCodeMap.get(actionCode);
            if (rspCode == null) {
                rspCode = "05";
            }
        }
        return rspCode;
    }

    public static String getResponseCodeError(String actionCode) {
        String rspCode = null;
        if (actionCode != null) {
            rspCode = rspCodeErrorMap.get(actionCode);
            if (rspCode == null) {
                rspCode = "";
            }
        }
        return rspCode;
    }


    protected static Hashtable<String, String> populateRspCodeErrorMap() {
        Hashtable<String, String> rspCodeErrMap = new Hashtable<String, String>();

        rspCodeErrMap.put("001", "Approve with ID");
        rspCodeErrMap.put("003", "Successful transaction");
        rspCodeErrMap.put("005", "System Error");
        rspCodeErrMap.put("020", "Successful transaction (Negative Balance)");
        rspCodeErrMap.put("095", "Reconcile Error");
        rspCodeErrMap.put("100", "Do not honor transaction");
        rspCodeErrMap.put("101", "Expired Card");
        rspCodeErrMap.put("103", "Call Issuer");
        rspCodeErrMap.put("104", "Card is restricted");
        rspCodeErrMap.put("105", "Call security");
        rspCodeErrMap.put("106", "Excessive pin failures");
        rspCodeErrMap.put("107", "Call Issuer");
        rspCodeErrMap.put("109", "Invalid merchant ID");
        rspCodeErrMap.put("110", "Cannot process amount");
        rspCodeErrMap.put("111", "Invalid account retry");
        rspCodeErrMap.put("116", "Insufficient funds retry");
        rspCodeErrMap.put("117", "Incorrect Pin");
        rspCodeErrMap.put("118", "Forced post, no account on file");
        rspCodeErrMap.put("119", "Transaction not permitted by law");
        rspCodeErrMap.put("120", "Not permitted");
        rspCodeErrMap.put("121", "Account limit exceeded");
        rspCodeErrMap.put("123", "Card limit exceeded");
        rspCodeErrMap.put("125", "Bad Card");
        rspCodeErrMap.put("126", "Pin processing error");
        rspCodeErrMap.put("200", "Invalid card");
        rspCodeErrMap.put("201", "Card expired");
        rspCodeErrMap.put("202", "Invalid card");
        rspCodeErrMap.put("203", "Call security");
        rspCodeErrMap.put("204", "Account restricted");
        rspCodeErrMap.put("206", "Invalid Pin");
        rspCodeErrMap.put("208", "Lost Card");
        rspCodeErrMap.put("209", "Stolen Card");
        rspCodeErrMap.put("901", "Invalid payment parameters");
        rspCodeErrMap.put("902", "Invalid transaction retry");
        rspCodeErrMap.put("903", "Transaction needs to be entered again");
        rspCodeErrMap.put("904", "The message received was not within standards");
        rspCodeErrMap.put("905", "Issuing institution is unknown");
        rspCodeErrMap.put("907", "Issuer inoperative");
        rspCodeErrMap.put("909", "System malfunction");
        rspCodeErrMap.put("910", "Issuer inoperative");
        rspCodeErrMap.put("911", "No knowledge of any attempt");
        rspCodeErrMap.put("912", "Time out waiting for response");
        rspCodeErrMap.put("913", "Duplicate transaction received");
        rspCodeErrMap.put("914", "Could not find the original transaction");
        rspCodeErrMap.put("915", "Amount > than original");
        rspCodeErrMap.put("920", "Pin processing error");
        rspCodeErrMap.put("923", "Request in progress");
        rspCodeErrMap.put("940", "Pick up card, special condition");
        rspCodeErrMap.put("941", "Failed currency conversion");
        return rspCodeErrMap;
    }

    protected static Hashtable<String, String> populateRspCodeMap() {

        Hashtable<String, String> localRspCode = new Hashtable<String, String>();

        /* standard iso mappings */
        localRspCode.put("000", "00");
        localRspCode.put("001", "08");
        localRspCode.put("002", "10");
        localRspCode.put("003", "11");
        localRspCode.put("004", "16");
        localRspCode.put("005", "30");
        localRspCode.put("100", "05");
        localRspCode.put("101", "54");
        localRspCode.put("102", "59");
        localRspCode.put("103", "60");
        localRspCode.put("104", "62");
        localRspCode.put("105", "66");
        localRspCode.put("106", "75");
        localRspCode.put("107", "01");
        localRspCode.put("108", "02");
        localRspCode.put("109", "03");
        localRspCode.put("110", "13");
        localRspCode.put("111", "14");
        localRspCode.put("113", "23");
        localRspCode.put("114", "42");
        localRspCode.put("115", "40");
        localRspCode.put("116", "51");
        localRspCode.put("117", "55");
        localRspCode.put("118", "56");
        localRspCode.put("119", "57");
        localRspCode.put("120", "58");
        localRspCode.put("121", "61");
        localRspCode.put("122", "63");
        localRspCode.put("123", "65");
        localRspCode.put("124", "93");
        localRspCode.put("125", "30");
        localRspCode.put("126", "55");
        localRspCode.put("200", "04");
        localRspCode.put("201", "33");
        localRspCode.put("202", "34");
        localRspCode.put("203", "35");
        localRspCode.put("204", "36");
        localRspCode.put("205", "37");
        localRspCode.put("206", "38");
        localRspCode.put("207", "07");
        localRspCode.put("208", "41");
        localRspCode.put("209", "43");
        localRspCode.put("301", "24");
        localRspCode.put("302", "25");
        localRspCode.put("304", "27");
        localRspCode.put("305", "28");
        localRspCode.put("306", "29");
        localRspCode.put("308", "26");


        /* special cases for svfe */
        localRspCode.put("901", "30");
        localRspCode.put("902", "12");
        localRspCode.put("903", "19");
        localRspCode.put("904", "30");
        localRspCode.put("905", "31");
        localRspCode.put("906", "90");
        localRspCode.put("907", "91");
        localRspCode.put("909", "96");
        localRspCode.put("910", "30");
        localRspCode.put("911", "68");
        localRspCode.put("912", "30");
        localRspCode.put("913", "94");
        localRspCode.put("914", "25");
        localRspCode.put("915", "95");
        localRspCode.put("920", "55");
        localRspCode.put("923", "09");
        localRspCode.put("940", "35");
        localRspCode.put("941", "05");
        return localRspCode;
    }
}
