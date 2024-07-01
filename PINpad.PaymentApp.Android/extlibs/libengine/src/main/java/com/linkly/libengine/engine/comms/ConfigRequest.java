package com.linkly.libengine.engine.comms;

public class ConfigRequest {
    private String hostName;
    private Integer hostPort;
    private String userName;
    private String password;

    public ConfigRequest( String hostName, Integer hostPort, String userName, String password ) {
        this.hostName = hostName;
        this.hostPort = hostPort;
        this.userName = userName;
        this.password = password;
    }

    public String getHostName() {
        return this.hostName;
    }

    public Integer getHostPort() {
        return this.hostPort;
    }

    public String getUserName() {
        return this.userName;
    }

    public String getPassword() {
        return this.password;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public void setHostPort(Integer hostPort) {
        this.hostPort = hostPort;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
