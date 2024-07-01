package com.linkly.libengine.engine.protocol.iso8583;

import java.util.Hashtable;


public final class As2805SuncorpRspCodeMap extends As2805RspCodeMap {
    private final Hashtable<String, MsgDefinition> rspCodeErrorMap = populateRspCodeErrorMap();

    public String getResponseCodeErrorDisplay(String responseCode) {
        return super.getResponseCodeErrorDisplay( responseCode, this.rspCodeErrorMap, "TRAN DECLINED\nSYSTEM ERROR" );
    }


    public String getResponseCodeErrorReceipt(String responseCode) {
        return super.getResponseCodeErrorReceipt( responseCode, this.rspCodeErrorMap );
    }

    public String getResponseCodeErrorPos(String responseCode) {
        return super.getResponseCodeErrorPos( responseCode, this.rspCodeErrorMap );
    }

    protected Hashtable<String, MsgDefinition> populateRspCodeErrorMap() {
        Hashtable<String, MsgDefinition> rspCodeErrMap = new Hashtable<>();

        rspCodeErrMap.put("00", new MsgDefinition( "TRANSACTION APPROVED", "APPROVED",                                           "APPROVED" ));
        rspCodeErrMap.put("01", new MsgDefinition( "TRAN DECLINED\nCALL CARD ISSUER", "DECLINED CODE 01\nCONTACT CARD ISSUER",   "DECLINED CALL ISSUER" ));
        rspCodeErrMap.put("02", new MsgDefinition( "TRAN DECLINED\nCALL CARD ISSUER", "DECLINED CODE 02\nCONTACT CARD ISSUER",   "DECLINED CALL ISSUER"  ));
        rspCodeErrMap.put("05", new MsgDefinition( "TRAN DECLINED\nCALL CARD ISSUER", "DECLINED CODE 05\nCONTACT CARD ISSUER",   "DECLINED CALL ISSUER"  ));
        rspCodeErrMap.put("06", new MsgDefinition( "TRAN DECLINED\nRE ENTER TRANS", "DECLINED CODE 06\nTRANSACTION ERROR",       "DECLINED REENTER TXN"  ));
        rspCodeErrMap.put("08", new MsgDefinition( "APPROVED\nPLEASE SIGN RECEIPT", "APPROVED\nWITH SIGNATURE" ,                  "APPROVED WITH SIG"));
        rspCodeErrMap.put("11", new MsgDefinition( "TRANSACTION APPROVED", "",                                                    "APPROVED"));
        rspCodeErrMap.put("12", new MsgDefinition( "INVALID TRANSACTION\nCALL HELP DESK", "DECLINED CODE 12\nTRANSACTION INVALID","DECLINED INVALID TXN"));
        rspCodeErrMap.put("13", new MsgDefinition( "TRAN DECLINED\nPLEASE TRY AGAIN", "DECLINED CODE 13\nAMOUNT INVALID" ,        "DECLINED TRY AGAIN"));
        rspCodeErrMap.put("14", new MsgDefinition( "TRAN DECLINED\nOTHER PAYMT REQD", "DECLINED CODE 14\nCARD NUMBER INVALID" ,   "DECLINED USE OTHER"));
        rspCodeErrMap.put("19", new MsgDefinition( "TRAN DECLINED\nRE ENTER TRANS", "DECLINED CODE 19\nRE-ENTER TRANSACTION" ,    "DECLINED RE ENTER"));
        rspCodeErrMap.put("30", new MsgDefinition( "SYSTEM ERROR 30\nCALL HELP DESK", "DECLINED CODE 30\nFORMAT ERROR" ,          "DECLINED ERROR 30"));
        rspCodeErrMap.put("31", new MsgDefinition( "TRAN DECLINED\nOTHER PAYMT REQD", "DECLINED CODE 31\nBANK NOT SUPPORTED" ,    "DECLINED USE OTHER"));
        rspCodeErrMap.put("33", new MsgDefinition( "CARD EXPIRED 33\nOTHER PAYMT REQD", "DECLINED CODE 33\nCARD EXPIRED" ,        "DECLINE CARD EXPIRED"));
        rspCodeErrMap.put("34", new MsgDefinition( "TRAN DECLINED\nCALL CARD ISSUER", "DECLINED CODE 34\nCONTACT CARD ISSUER" ,   "DECLINED CALL ISSUER"));
        rspCodeErrMap.put("35", new MsgDefinition( "TRAN DECLINED\nCALL CARD ISSUER", "DECLINED CODE 35\nCONTACT CARD ISSUER" ,   "DECLINED CALL ISSUER"));
        rspCodeErrMap.put("36", new MsgDefinition( "TRAN DECLINED\nCALL CARD ISSUER", "DECLINED CODE 36\nCONTACT CARD ISSUER" ,   "DECLINED CALL ISSUER"));
        rspCodeErrMap.put("38", new MsgDefinition( "PIN INCORRECT\nOTHER PAYMT REQD", "DECLINED CODE 38\nPIN TRIES EXCEEDED" ,    "PIN INCORRECT"));
        rspCodeErrMap.put("39", new MsgDefinition( "NO CREDIT A/C 39\nOTHER PAYMT REQD", "DECLINED CODE 39\nNO CREDIT ACCOUNT" ,  "DECLINED NO CREDIT"));
        rspCodeErrMap.put("40", new MsgDefinition( "INVALID TRANSACTION\nCALL HELP DESK", "DECLINED CODE 40\nTRANSACTION INVALID","DECLINED INVALID TXN"));
        rspCodeErrMap.put("41", new MsgDefinition( "TRAN DECLINED\nOTHER PAYMT REQD", "DECLINED CODE 41\nCONTACT CARD ISSUER" ,   "DECLINED USE OTHER"));
        rspCodeErrMap.put("42", new MsgDefinition( "INVALID ACCOUNT\nOTHER PAYMT REQD", "DECLINED CODE 42\nINVALID ACCOUNT" ,     "INVALID ACCOUNT"));
        rspCodeErrMap.put("43", new MsgDefinition( "TRAN DECLINED\nOTHER PAYMT REQD", "DECLINED CODE 43\nCONTACT CARD ISSUER" ,   "DECLINED USER OTHER"));
        rspCodeErrMap.put("44", new MsgDefinition( "TRAN DECLINED\nSYSTEM ERROR", "DECLINED CODE 44\nINVALID ACCOUNT" ,           "DECLINED SYSTEM ERR"));
        rspCodeErrMap.put("51", new MsgDefinition( "TRAN DECLINED\nOTHER PAYMT REQD", "DECLINED CODE 51\nINSUFFICIENT FUNDS" ,    "DECLINED USE OTHER"));
        rspCodeErrMap.put("52", new MsgDefinition( "NO CHEQUE A/C 52\nOTHER PAYMT REQD", "DECLINED CODE 52\nNO CHEQUE ACCOUNT" ,  "NO CHEQUE ACCOUNT"));
        rspCodeErrMap.put("53", new MsgDefinition( "NO SAVING A/C 53\nOTHER PAYMT REQD", "DECLINED CODE 53\nNO SAVING ACCOUNT" ,  "NO SAVING ACCOUNT"));
        rspCodeErrMap.put("54", new MsgDefinition( "CARD EXPIRED 54\nOTHER PAYMT REQD", "DECLINED CODE 54\nCARD EXPIRED" ,        "DECLINE CARD EXPIRED"));
        rspCodeErrMap.put("55", new MsgDefinition( "PIN INVALID 55\nPLEASE TRY AGAIN", "DECLINED CODE 55\nINVALID PIN" ,          "INVALID PIN"));
        rspCodeErrMap.put("56", new MsgDefinition( "TRAN DECLINED\nOTHER PAYMT REQD", "DECLINED CODE 56\nCONTACT CARD ISSUER" ,   "DECLINED USE OTHER"));
        rspCodeErrMap.put("57", new MsgDefinition( "TRAN DECLINED\nOTHER PAYMT REQD", "DECLINED CODE 57\nCONTACT CARD ISSUER" ,   "DECLINED USE OTHER"));
        rspCodeErrMap.put("58", new MsgDefinition( "TRAN DECLINED\nOTHER PAYMT REQD", "DECLINED CODE 58\nTRAN NOT ALLOWED" ,      "TRAN NOT ALLOWED"));
        rspCodeErrMap.put("59", new MsgDefinition( "TRAN DECLINED\nOTHER PAYMT REQD", "DECLINED CODE 59\nCONTACT CARD ISSUER" ,   "DECLINED USE OTHER"));
        rspCodeErrMap.put("60", new MsgDefinition( "TRAN DECLINED\nCALL HELP DESK", "DECLINED CODE 60" ,                          "DECLINE CALL HELPDSK"));
        rspCodeErrMap.put("61", new MsgDefinition( "TRAN DECLINED\nOTHER PAYMT REQD", "DECLINED CODE 61\nCONTACT CARD ISSUER" ,   "DECLINED USE OTHER"));
        rspCodeErrMap.put("62", new MsgDefinition( "TRAN DECLINED\nOTHER PAYMT REQD", "DECLINED CODE 62\nCONTACT CARD ISSUER" ,   "DECLINED USE OTHER"));
        rspCodeErrMap.put("63", new MsgDefinition( "TRAN CANCELLED\nOTHER PAYMT REQD", "DECLINED CODE 63\nCONTACT CARD ISSUER" ,  "DECLINED USE OTHER"));
        rspCodeErrMap.put("64", new MsgDefinition( "TRAN DECLINED\nSYSTEM ERROR", "",                                             "DECLINED SYSTEM ERR"));
        rspCodeErrMap.put("65", new MsgDefinition( "TRAN DECLINED\nSYSTEM ERROR", "",                                             "DECLINED SYSTEM ERR"));
        rspCodeErrMap.put("75", new MsgDefinition( "PIN INCORRECT\nOTHER PAYMT REQD", "DECLINED\nPIN TRIES EXCEEDED" ,            "PIN TRIES EXCEEDED"));
        rspCodeErrMap.put("76", new MsgDefinition( "TRANSACTION APPROVED", "APPROVED" ,                                           "APPROVED"));
        rspCodeErrMap.put("82", new MsgDefinition( "TRAN CANCELLED\nCALL HELP DESK", "DECLINED CODE 82\nSYSTEM ERROR" ,           "DECLINE CALL HELPDSK"));
        rspCodeErrMap.put("91", new MsgDefinition( "BANK NOT AVAIL 91\nUSE FALLBACK", "DECLINED CODE 91\nISSUER NOT AVAILABLE" ,  "ISSUER NOT AVAILABLE"));
        rspCodeErrMap.put("92", new MsgDefinition( "TRAN DECLINED\nOTHER PAYMT REQD", "DECLINED CODE 92\nBANK NOT SUPPORTED" ,    "BANK NOT SUPPORTED"));
        rspCodeErrMap.put("94", new MsgDefinition( "TRAN DECLINED\nSYSTEM ERROR", "",                                             "DECLINED SYSTEM ERR"));
        rspCodeErrMap.put("95", new MsgDefinition( "SETTLEMENT FAILED", "SETTLEMENT FAILURE\nOUTSIDE SETTLE TIME" ,               "OUTSIDE SETTLE TIME"));
        rspCodeErrMap.put("96", new MsgDefinition( "TRAN CANCELLED\nCALL HELP DESK", "DECLINED CODE 96\nSYSTEM ERROR" ,           "DECLINE CALL HELPDSK"));
        rspCodeErrMap.put("97", new MsgDefinition( "SETTLEMENT SUCCESSFUL", "SETTLEMENT SUCCESS" ,                                "SETTLEMENT SUCCESS"));
        rspCodeErrMap.put("98", new MsgDefinition( "TRAN CANCELLED\nMAC ERROR 98", "DECLINED CODE 98\nAUTHENTICATION ERROR" ,     "DECLINE MAC ERROR"));
        rspCodeErrMap.put("N2", new MsgDefinition( "RSA REQUIRED", "RSA REQUIRED" ,                                               "RSA REQUIRED"));
        rspCodeErrMap.put("Q5", new MsgDefinition( "SETTLEMENT FAILED", "SETTLEMENT FAILURE\nALREADY SETTLED" ,                   "ALREADY SETTLED"));
        rspCodeErrMap.put("Y1", new MsgDefinition( "TRANSACTION APPROVED", "APPROVED" ,                                           "APPROVED Y1"));
        rspCodeErrMap.put("Y3", new MsgDefinition( "TRANSACTION APPROVED", "APPROVED" ,                                           "APPROVED Y3"));
        rspCodeErrMap.put("Z1", new MsgDefinition( "Z1 DECLINED", "DECLINED CODE Z1" ,                                            "DECLINED Z1"));
        rspCodeErrMap.put("Z3", new MsgDefinition( "Z3 DECLINED", "DECLINED CODE Z3" ,                                            "DECLINED Z3"));
        rspCodeErrMap.put("Z4", new MsgDefinition( "Z4 DECLINED", "DECLINED CODE Z4" ,                                            "DECLINED Z4"));

        return rspCodeErrMap;
    }

}
