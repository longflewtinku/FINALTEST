package com.linkly.payment.qrcodes;

import com.linkly.libengine.engine.transactions.TransRec;

public interface IQRData {

    /*
        this function is used to encode a qr code, how the data is converted and encoded is up to the implementation
     */
    String EncodeQRData(TransRec trans);

    /*
        this function is used to decode a qr code, how the data is saved into the trans structure is also up to the implementation
     */
    TransRec DecodeQRData(String qrData);
}
