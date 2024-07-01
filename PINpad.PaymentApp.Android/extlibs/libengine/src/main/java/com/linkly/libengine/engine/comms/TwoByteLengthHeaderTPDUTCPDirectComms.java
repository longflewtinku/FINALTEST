package com.linkly.libengine.engine.comms;

import com.linkly.libengine.dependencies.IDependency;

import java.nio.ByteBuffer;

public class TwoByteLengthHeaderTPDUTCPDirectComms extends TwoByteLengthHeaderTCPDirectComms {

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
        // add 5 byte tpdu header to data
        ByteBuffer msgInclLengthHeader = ByteBuffer.allocate(5+data.length);
        msgInclLengthHeader.put( new byte[] { 0x60, 0x00, 0x01, 0x00, 0x00 });
        msgInclLengthHeader.put( data );

        return super.send(d, msgInclLengthHeader.array());
    }

    @Override
    public byte[] recv(IDependency d) {
        // receive using super class first
        byte[] respInclTpduHeader = super.recv(d);

        if (respInclTpduHeader != null && respInclTpduHeader.length > 5) {
            // strip tpdu header off
            byte[] result = new byte[respInclTpduHeader.length-5];
            System.arraycopy( respInclTpduHeader, 5, result, 0, respInclTpduHeader.length-5 );
            return result;
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
        throw new UnsupportedOperationException();
    }

}