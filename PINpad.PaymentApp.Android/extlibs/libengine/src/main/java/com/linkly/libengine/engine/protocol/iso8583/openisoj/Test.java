package com.linkly.libengine.engine.protocol.iso8583.openisoj;

import com.linkly.libengine.engine.protocol.iso8583.openisoj.exceptions.UnknownFieldException;

public class Test {
    public static void main(String[] args) throws UnknownFieldException {
        Iso8583 msg = new Iso8583();
        msg.setMsgType(Iso8583.MsgType._0200_TRAN_REQ);
        msg.setFieldValue(Iso8583.Bit._002_PAN, "8564735641235874");
        msg.setTransactionAmount(6756);
        msg.setFieldValue(Iso8583.Bit._011_SYS_TRACE_AUDIT_NUM, "123456");
    }
}
