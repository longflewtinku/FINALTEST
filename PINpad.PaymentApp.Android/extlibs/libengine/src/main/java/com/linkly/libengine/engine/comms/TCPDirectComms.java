package com.linkly.libengine.engine.comms;

import static com.linkly.libmal.IMalComms.CommsResult.FAIL;

import com.linkly.libengine.config.paycfg.PaymentSwitch;
import com.linkly.libengine.dependencies.IDependency;
import com.linkly.libengine.overrides.CoreOverrides;
import com.linkly.libmal.IMal;
import com.linkly.libmal.IMalComms;
import com.linkly.libmal.IMalCommsIF;
import com.linkly.libmal.MalFactory;
import com.linkly.libmal.idal.IDalCommManager;

import timber.log.Timber;

public class TCPDirectComms implements IComms {

    public static IMalCommsIF iMalComms;
    public IMal imal;
    private IMalComms activeConnection = null;

    public String getCommsType(IDependency d) {
        if (iMalComms.isOpen( IDalCommManager.EChannelType.WIFI )) {
            return "Wi-Fi";
        } else if (iMalComms.isOpen( IDalCommManager.EChannelType.MOBILE )) {
            return "Cellular";
        } else {
            return "None";
        }
    }

    public boolean open(IDependency d) {
        boolean bNetwork = false;
        // TODO: fix this global dependency.....
        imal = MalFactory.getInstance();
        iMalComms = imal.getComms();
        Timber.i( "Calling isSim");
        if (imal.getIsSim()) {
            return false;
        }
        Timber.i( "Calling openWifi");

        //If Wifi is not connected locally, don't try to open
        if (iMalComms.isWifiConnectedToNetwork(imal.getMalContext())) { // TODO: fix this random context stuff....
            if (iMalComms.openWifi()) {
                bNetwork = true;
                Timber.i( "Wi-Fi UP");
            }
        }

        Timber.i( "open Done");
        return bNetwork;
    }

    public boolean isConnected(IDependency d) {
        return activeConnection != null || CoreOverrides.get().isSpoofComms();
    }

    public boolean connect(IDependency d, String url, String port, boolean useSSL, int timeoutSec) {
        boolean result = false;
        IMalComms.CommsResult cResult = FAIL;

        Timber.i( "connect: " + url + ":" + port);
        if (CoreOverrides.get().isSpoofCommsConnectFail()) {
            return false;
        }

        if(CoreOverrides.get().isSpoofComms()) {
            return true;
        }

        activeConnection = iMalComms.createMalConnection();
        if (activeConnection != null) {
              cResult = activeConnection.connectTCP(url, Integer.valueOf(port), timeoutSec);
        }
        if (cResult == IMalComms.CommsResult.SUCCESS) {
            result = true;
            CommsStatusMonitor.getInstance().notifyInternetEstablished();
        } else if (cResult == IMalComms.CommsResult.TIMEOUT
                || cResult == FAIL) {
            CommsStatusMonitor.getInstance().notifyInternetLost();
        }
        return result;
    }

    public boolean connect(IDependency d, int iTryCount) {
        return connect(d, iTryCount, 0);
    }

    // Suppressing complexity warning. limit is 15 this is 20. Not worth it.
    @SuppressWarnings("java:S3776")
    public boolean connect(IDependency d, int iTryCount, int timeoutSecs) {
        IMalComms.CommsResult cResult = FAIL;
        boolean result = false;
        PaymentSwitch paySwitchCfg = d.getPayCfg().getPaymentSwitch();
        String host = paySwitchCfg.getIp().getHost();
        Timber.e("Connect Start");

        if(CoreOverrides.get().isSpoofComms()){
            return true;
        }

        Timber.i( "connect: %s", host);
        if (CoreOverrides.get().isSpoofCommsConnectFail()) {
            return false;
        }

        if (iTryCount >= 2) {
            host = paySwitchCfg.getIp().getHost2nd();
            Timber.i( "Use Secondary host");

        }
        String[] parts = host.split(":");
        if (parts[0] != null && parts[1] != null) {
            Timber.i( "Host:" + parts[0] + ":" + parts[1]);
        } else {
            return false;
        }

        activeConnection = iMalComms.createMalConnection();
        if (activeConnection != null) {
            int timeoutToUse = (timeoutSecs == 0) ? paySwitchCfg.getDialTimeout() : timeoutSecs;
            try {
                if ( paySwitchCfg.isUseSsl() ) {
                    if ( paySwitchCfg.isClientAuth() )
                        cResult = activeConnection.connectTCP( parts[0], Integer.parseInt( parts[1] ), timeoutToUse, new String[]{ paySwitchCfg.getCertificateFile() }, paySwitchCfg.getPrivateKeyFile(), paySwitchCfg.getPrivateKeyPassword(), paySwitchCfg.getPrivateKeyCertificate() );
                    else if(paySwitchCfg.getCertificateFile() != null)
                        cResult = activeConnection.connectTCP( parts[0], Integer.parseInt( parts[1] ), timeoutToUse, new String[]{ paySwitchCfg.getCertificateFile() } );
                    else
                        Timber.e("Comms Result :%s",cResult);
                } else {
                    cResult = activeConnection.connectTCP( parts[0], Integer.parseInt( parts[1] ), timeoutToUse );
                }
            } catch ( Exception e){
                Timber.w(e);
            }
        }
        if (cResult == IMalComms.CommsResult.SUCCESS) {
            result = true;
            CommsStatusMonitor.getInstance().notifyInternetEstablished();
        } else if (cResult == IMalComms.CommsResult.TIMEOUT
                || cResult == FAIL) {
            CommsStatusMonitor.getInstance().notifyInternetLost();
        }

        return (result);
    }

    @Override
    public int send(IDependency d, byte[] data) {
        if (activeConnection == null)
            return 0;
        Timber.v("Send Start - Bytes %d", data.length);

        int sent = activeConnection.sendTCP(data);

        Timber.v("Send End");
        if (sent == 0)
            disconnect(d);
        return sent;

    }

    @Override
    public int send(IDependency d, String ipGatewayHost, String mid, String stid, int msgType, byte[] packedBuffer) {
        throw new UnsupportedOperationException("call derived class instead");
    }

    @Override
    public byte[] recv(IDependency d, int iBytes) {
        return recv(d, iBytes, d.getPayCfg().getPaymentSwitch().getReceiveTimeout());
    }

    @Override
    public byte[] recv(IDependency d, int iBytes, int timeout) {
        Timber.v("Recv Start - Bytes %d", iBytes);
        if (CoreOverrides.get().isSpoofCommsReceiveFail()) {
            return null;
        } else if (CoreOverrides.get().isSpoofComms()) {
            return null;
        } else if (activeConnection == null) {
            return null;
        } else {
            byte[] msg = activeConnection.recvTCP(iBytes, timeout);
            Timber.v("Recv End - Bytes %d", iBytes);
            return msg;
        }
    }

    @Override
    public byte[] recv( IDependency d ) {
        return null;
    }

    @Override
    public boolean disconnect(IDependency d) {
        IMalComms.CommsResult cResult;
        if (activeConnection != null) {
            Timber.e( "disconnect: disconnecting now" );
            cResult = activeConnection.disconnectTCP();

            activeConnection = null;
            return cResult == IMalComms.CommsResult.SUCCESS;
        } else {
            Timber.e( "disconnect: no active connection" );
            return true;
        }
    }

    @Override
    public boolean close(IDependency d) {
        disconnect(null);
        iMalComms.closeWifi();
        iMalComms.closeGprs();
        return true;
    }

}