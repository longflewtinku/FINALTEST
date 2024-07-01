package com.linkly.libengine.engine.comms;

import com.linkly.libengine.dependencies.IDependency;
import com.linkly.libmal.global.util.Util;

import timber.log.Timber;

public class CommsFactory {
    enum CommsType {
        POS_ROUTED, // route through linkly connect app
        TWO_BYTE_LENGTH_FIVE_BYTE_TPDU_HEADER_TCP_DIRECT, // 2 byte length, 5 byte tpdu, direct to destination host
        TWO_BYTE_LENGTH_TCP_DIRECT, // 2 byte length, direct to destination host
        NO_HEADER_TCP_DIRECT, // no header, direct to destination host
        IP_GATEWAY_PROXY_APP // to route messages via IP Gateway Proxy app running on localhost. Uses simple proprietary commands
    }

    private static final String TAG = "CommsFactory";

    private IComms returnDefault() {
        return new TwoByteLengthHeaderTCPDirectComms();
    }

    public IComms getCommsObj(IDependency d) {

        // if POS comms is enabled (routes all bank messages via POS link/linkly connect app)
        if( d.getPayCfg().isPosCommsEnabled() ) {
            return new PosComms();
        }

        try {
            String commsTypeStr = d.getPayCfg().getPaymentSwitch().getCommsType();
            if (Util.isNullOrEmpty(commsTypeStr)) {
                // default to TCPDirectComms
                Timber.e( "NO COMMS TYPE DEFINED, value = " + commsTypeStr + ". Defaulting to TCP direct comms");
                return returnDefault();
            } else {
                CommsType commsType = CommsType.valueOf(commsTypeStr);
                switch (commsType) {
                    case POS_ROUTED:
                        return new PosComms();

                    case TWO_BYTE_LENGTH_FIVE_BYTE_TPDU_HEADER_TCP_DIRECT:
                        return new TwoByteLengthHeaderTPDUTCPDirectComms();

                    case TWO_BYTE_LENGTH_TCP_DIRECT:
                        return new TwoByteLengthHeaderTCPDirectComms();

                    case NO_HEADER_TCP_DIRECT:
                        return new TCPDirectComms();

                    case IP_GATEWAY_PROXY_APP:
                        return new IpGatewayProxyComms();

                    default:
                        Timber.e( "INVALID COMMS TYPE DEFINED, value = " + commsTypeStr + ". Defaulting to TCP direct comms");
                        return returnDefault();
                }
            }
        } catch( Exception e ) {
            Timber.e( "INVALID COMMS TYPE DEFINED. Defaulting to TCP direct comms");
            return returnDefault();
        }
    }
}
