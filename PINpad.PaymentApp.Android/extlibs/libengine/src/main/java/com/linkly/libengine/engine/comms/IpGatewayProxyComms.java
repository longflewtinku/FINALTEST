package com.linkly.libengine.engine.comms;

import com.linkly.libengine.dependencies.IDependency;

import java.security.InvalidParameterException;

import timber.log.Timber;

public class IpGatewayProxyComms extends TwoByteLengthHeaderTCPDirectComms {
    private static final String TAG = IpGatewayProxyComms.class.getSimpleName();
    private static final String IP_GATEWAY_PROXY_HOST = "127.0.0.1";
    private static final String IP_GATEWAY_PROXY_PORT = "15487";
    private boolean linkConfigured = false;

    @Override
    public String getCommsType(IDependency d) {
        return super.getCommsType(d);
    }

    private ConfigRequest buildConfigRequest( IDependency d ) {
        String ipGatewayHost = d.getPayCfg().getPaymentSwitch().getIpGatewayHost();
        String ipGatewayUser = d.getPayCfg().getPaymentSwitch().getIpGatewayUser();
        String ipGatewayPwd = d.getPayCfg().getPaymentSwitch().getIpGatewayPwd();

        if( ipGatewayHost == null || ipGatewayUser == null || ipGatewayPwd == null ) {
            throw new InvalidParameterException();
        }

        // split host from <hostname>:<port> format
        String[] hostData = ipGatewayHost.split( ":" );
        int hostPort = 443; // default to SSL port
        if( hostData.length == 2 ) {
            hostPort = Integer.parseInt(hostData[1]);
        }

        // build request
        return new ConfigRequest( hostData[0], hostPort, ipGatewayUser, ipGatewayPwd );
    }

    private boolean setupUplink(IDependency d) {
        boolean retVal = false;

        ProxyRequest cfgRequest = new ProxyRequest( buildConfigRequest(d) );
        String msgStr = cfgRequest.toJsonString();

        // send request and wait for response
        try {
            if (super.connect(d, IP_GATEWAY_PROXY_HOST, IP_GATEWAY_PROXY_PORT, false, 4)) {
                if (send(d, msgStr.getBytes()) > 0) {
                    byte[] resp = recv(d);

                    if (resp.length == 2) {
                        String respStr = new String(resp);
                        if (respStr.compareTo("00") == 0) {
                            // host connected without error
                            Timber.i( "IP Gateway config OK");
                            retVal = true;
                        } else if (respStr.compareTo("01") == 0) {
                            // host already connected
                            Timber.i( "IP Gateway config - Already Configured");
                            retVal = true;
                        } else if (respStr.compareTo("E0") == 0) {
                            // exception starting server instance
                            Timber.e( "IP Gateway config exception E0 starting uplink server instance");
                        } else if (respStr.compareTo("E1") == 0) {
                            // exception starting server instance
                            Timber.e( "IP Gateway config exception E1 starting uplink server instance");
                        } else {
                            Timber.e( "IP Gateway config exception Non-handled error = " + respStr);
                        }
                    } else {
                        Timber.e( "IP Gateway config exception recv ret error");
                    }
                }
            }
        } catch( Exception e ) {
            Timber.w(e);
        } finally {
            Timber.e( "IP Gateway config - disconnecting socket");
            disconnect(d);
        }

        Timber.i( "bottom of setupUplink" );

        linkConfigured = retVal;

        return retVal;
    }

    @Override
    public boolean open(IDependency d) {
        if( !super.open(d) ) {
            Timber.e( "super.open() failed" );
            return false;
        }

        return setupUplink(d);
    }

    @Override
    public boolean isConnected(IDependency d) {
        return super.isConnected(d);
    }

    @Override
    public boolean connect(IDependency d, String url, String port, boolean useSSL, int timeoutSec) {
        throw new UnsupportedOperationException( "method not supported");
    }

    @Override
    public boolean connect(IDependency d, int iTryCount) {
        // if IP gateway uplink not configured, do it now
        if( !linkConfigured ) {
            Timber.e( "connect: configuring IP gateway uplink" );
            if( !setupUplink(d) ) {
                // link config failed, return error
                Timber.e( "connect: configuring IP gateway uplink FAILED, connect error" );
                return false;
            }
        }

        // connect to the ip gateway proxy app using regular socket connect
        Timber.i( "connect: connecting" );
        return super.connect( d, IP_GATEWAY_PROXY_HOST, IP_GATEWAY_PROXY_PORT, false, 30 );
    }

    @Override
    public int send(IDependency d, String ipGatewayHost, String mid, String stid, int msgType, byte[] packedBuffer) {
        TxRequest msg = new TxRequest( mid, stid, ipGatewayHost, msgType, packedBuffer );
        ProxyRequest txRequest = new ProxyRequest(msg);
        String msgStr = txRequest.toJsonString();

        return super.send( d, msgStr.getBytes() );
    }

    @Override
    public int send(IDependency d, byte[] data) {
        // wrap the tx request in the JSON type for sending
        return super.send(d, data);
    }

    @Override
    public byte[] recv(IDependency d) {
        byte[] response = super.recv(d);

        if( response.length == 2 ) {
            String respStr = new String(response);
            if( respStr.contentEquals("E1") ) {
                // 'E1' indicates server isn't set up. Try to set it up again for next attempt
                setupUplink(d);
            }
        }

        return response;
    }

    @Override
    public boolean disconnect(IDependency d) {
        Timber.i( "disconnect: disconnecting" );
        return super.disconnect(d);
    }

    @Override
    public boolean close(IDependency d) {
        return super.close(d);
    }

}