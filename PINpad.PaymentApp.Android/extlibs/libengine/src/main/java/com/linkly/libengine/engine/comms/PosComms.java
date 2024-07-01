package com.linkly.libengine.engine.comms;

import com.linkly.libengine.dependencies.IDependency;
import com.linkly.libengine.helpers.ECRHelpers;
import com.linkly.libmal.MalFactory;
import com.linkly.libpositive.wrappers.PositiveConfigureBankLinkRequest;

import timber.log.Timber;

public class PosComms extends TwoByteLengthHeaderTCPDirectComms {
    private final String SUNCORP_HOST_MSG_PROXY_PORT = "8080";

    public PosComms() {
    }

    private void configureBankLink(IDependency d) {
        // send 'configure' request to connect app to configure bank host link
        PositiveConfigureBankLinkRequest request = new PositiveConfigureBankLinkRequest();
        request.setDeviceCode( "0" );
        request.setHostId( d.getPayCfg().getPosCommsHostId() );
        request.setTerminalId( d.getPayCfg().getStid() );
        request.setMerchantId( d.getPayCfg().getMid() );
        request.setLicenseKey("");
        request.setInterfaceType( d.getPayCfg().getPosCommsInterfaceType() ); // 2 = standard AS2805
        request.setReceiveTimeout( d.getPayCfg().getPaymentSwitch().getReceiveTimeout() );
        request.setListeningPort( SUNCORP_HOST_MSG_PROXY_PORT );

        // TODO: Fix this direct value. We need to refactor POS COMMS
        ECRHelpers.ipcConfigureBankLink( d, request, MalFactory.getInstance().getMalContext());
    }

    @Override
    public boolean open(IDependency d) {
        configureBankLink(d);
        return super.open(d);
    }

    @Override
    public boolean close(IDependency d) {
        return super.close(d);
    }

    @Override
    public boolean isConnected(IDependency d) {
        return super.isConnected(d);
    }

    @Override
    public boolean connect( IDependency d, int iTryCount ) {
        // no harm sending this before every txn in case proxy isn't established yet
        configureBankLink(d);

        // todo: this is test code, remove later
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Timber.w(e);
        }

        // connect to message proxy running in Connect app
        return super.connect( d, "127.0.0.1", SUNCORP_HOST_MSG_PROXY_PORT, false, 10 );
    }

    @Override
    public boolean connect( IDependency d, String url, String port, boolean useSSL, int timeoutSeconds ) {
        // this method shouldn't be used
        return connect(d,0);
    }

    @Override
    public boolean disconnect(IDependency d) {
        return super.disconnect(d);
    }

    @Override
    public int send( IDependency d, byte[] data ) {
        return super.send( d, data );
    }

    @Override
    public String getCommsType(IDependency d) {
        return null;
    }

    @Override
    public byte[] recv(IDependency d) {
        // receive whole message
        return super.recv(d);
    }

    @Override
    public int send(IDependency d, String ipGatewayHost, String mid, String stid, int msgType, byte[] packedBuffer) {
        throw new UnsupportedOperationException();
    }

}
