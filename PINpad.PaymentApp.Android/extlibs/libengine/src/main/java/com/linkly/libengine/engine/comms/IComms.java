package com.linkly.libengine.engine.comms;

import com.linkly.libengine.dependencies.IDependency;

public interface IComms {

    boolean open(IDependency d);

    boolean close(IDependency d);

    boolean isConnected(IDependency d);

    boolean connect(IDependency d, int iTryCount);

    boolean connect(IDependency d, String url, String port, boolean useSSL, int timeoutSeconds);

    boolean disconnect(IDependency d);

    int send( IDependency d, byte[] data );

    int send(IDependency d, String ipGatewayHost, String mid, String stid, int msgType, byte[] packedBuffer);

    byte[] recv(IDependency d, int iBytes);

    byte[] recv(IDependency d, int iBytes, int timeoutSeconds);

    byte[] recv(IDependency d);

    String getCommsType(IDependency d);
}
