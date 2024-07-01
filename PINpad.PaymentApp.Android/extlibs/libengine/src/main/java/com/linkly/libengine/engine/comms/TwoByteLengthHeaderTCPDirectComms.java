package com.linkly.libengine.engine.comms;

import com.linkly.libengine.dependencies.IDependency;

import java.nio.ByteBuffer;

public class TwoByteLengthHeaderTCPDirectComms extends TCPDirectCommsWithFallback {

    @Override
    public String getCommsType(IDependency d) {
        return super.getCommsType(d);
    }

    @Override
    public boolean open(IDependency d) {
        return super.open(d);
    }

    @Override
    public boolean isConnected(IDependency d) {
        return super.isConnected(d);
    }

    @Override
    public boolean connect(IDependency d, String url, String port, boolean useSSL, int timeoutSec) {
        return super.connect( d, url, port, useSSL, timeoutSec );
    }

    @Override
    public boolean connect(IDependency d, int iTryCount) {
        return super.connect(d, iTryCount);
    }

    @Override
    public int send(IDependency d, byte[] data) {
        // add 2 byte length header to data
        ByteBuffer msgInclLengthHeader = ByteBuffer.allocate(2+data.length);
        msgInclLengthHeader.putShort(Integer.valueOf(data.length).shortValue());
        msgInclLengthHeader.put( data );

        return super.send(d, msgInclLengthHeader.array());
    }

    @Override
    public byte[] recv(IDependency d) {
        // receive 2 length bytes first
        byte[] lengthResponse = super.recv(d, 2);

        if (lengthResponse != null && lengthResponse.length == 2) {
            // calculate length of payload
            int len = lengthResponse[0] & 0xFF;
            len = len << 8;
            len = len | (lengthResponse[1] & 0xFF);

            // receive + return the message payload
            return super.recv(d, len);
        }
        return null;
    }

    @Override
    public boolean disconnect(IDependency d) {
        return super.disconnect(d);
    }

    @Override
    public boolean close(IDependency d) {
        return super.close(d);
    }

    @Override
    public int send(IDependency d, String ipGatewayHost, String mid, String stid, int msgType, byte[] packedBuffer) {
        throw new UnsupportedOperationException("call derived class instead");
    }

}