package com.linkly.libengine.engine.comms;

import static android.util.Base64.DEFAULT;

import android.util.Base64;

import com.google.gson.Gson;


public class TxRequest {
    private String caId;
    private String catId;
    private String hostId;
    private Integer msgType;
    private String message;

    public TxRequest(String caId, String catId, String hostId, Integer mType, byte[] messageBinary ) {
        this.caId = caId;
        this.catId = catId;
        this.hostId = hostId;
        this.msgType = mType;
        setMessage(messageBinary);
    }

    public void setMessage( byte[] messageBinary ) {
        message = Base64.encodeToString(messageBinary,DEFAULT);
    }

    public String toJsonString() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    public static TxRequest createFromJson(String jsonStr ) {
        Gson gson = new Gson();
        return gson.fromJson( jsonStr, TxRequest.class );
    }

    public String getCaId() {
        return this.caId;
    }

    public String getCatId() {
        return this.catId;
    }

    public String getHostId() {
        return this.hostId;
    }

    public Integer getMsgType() {
        return this.msgType;
    }

    public String getMessage() {
        return this.message;
    }

    public void setCaId(String caId) {
        this.caId = caId;
    }

    public void setCatId(String catId) {
        this.catId = catId;
    }

    public void setHostId(String hostId) {
        this.hostId = hostId;
    }

    public void setMsgType(Integer msgType) {
        this.msgType = msgType;
    }
}
