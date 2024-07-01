package com.linkly.libengine.engine.comms;

import com.google.gson.Gson;

public class ProxyRequest {
    private TxRequest txRequest;
    private ConfigRequest configRequest;

    public ProxyRequest(TxRequest txRequest) {
        this.txRequest = txRequest;
    }

    public ProxyRequest(ConfigRequest configRequest) {
        this.configRequest = configRequest;
    }

    public ProxyRequest(TxRequest txRequest, ConfigRequest configRequest) {
        this.txRequest = txRequest;
        this.configRequest = configRequest;
    }

    public String toJsonString() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    public TxRequest getTxRequest() {
        return this.txRequest;
    }

    public ConfigRequest getConfigRequest() {
        return this.configRequest;
    }

    public void setTxRequest(TxRequest txRequest) {
        this.txRequest = txRequest;
    }

    public void setConfigRequest(ConfigRequest configRequest) {
        this.configRequest = configRequest;
    }
}
