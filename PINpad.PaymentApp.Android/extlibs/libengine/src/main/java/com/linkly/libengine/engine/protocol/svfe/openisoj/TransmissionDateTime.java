package com.linkly.libengine.engine.protocol.svfe.openisoj;

import com.linkly.libengine.engine.protocol.svfe.openisoj.exceptions.UnknownFieldException;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class TransmissionDateTime {
    private AMessage _message;

    public TransmissionDateTime(AMessage message) {
        _message = message;
    }

    public long setNow() throws UnknownFieldException {
        SimpleDateFormat format = new SimpleDateFormat("MMddHHmmss");
        format.setTimeZone(TimeZone.getTimeZone("GMT"));
        Date now = new Date();
        _message.setFieldValue(Iso8583.Bit._007_TRAN_DATE_TIME, format.format(now));
        return now.getTime();
    }

    public long getNow() throws UnknownFieldException {
        SimpleDateFormat format = new SimpleDateFormat("MMddHHmmss");
        format.setTimeZone(TimeZone.getTimeZone("GMT"));
        Date now = new Date();
        return now.getTime();
    }

}
