package com.linkly.libengine.helpers;

import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import javax.net.SocketFactory;

import timber.log.Timber;

@SuppressWarnings("deprecation")
public class NetworkStatusCallback extends ConnectivityManager.NetworkCallback {
    public static final String DEFAULT_INTERNET_TEST_HOST_URL = "www.google.com";
    public static final String DEFAULT_INTERNET_TEST_HOST_PORT = "80";
    public static final int INTERNET_TEST_HOST_CONNECT_TIMEOUT_MSEC = 5000;
    public static final long INTERNET_TEST_HOST_BACKOFF_TIMER_MSEC = 10000L;
    protected final String networkName;

    public NetworkStatusCallback(String networkName){
        this.networkName = networkName;
    }

    // Suppressing deprecation warning for getTypeName and getNetworkInfo
    @SuppressWarnings("java:S1874")
    public static String getNetworkTypeName(ConnectivityManager connectivityManager, Network network, String defaultVal){
        NetworkInfo nwInfo = connectivityManager.getNetworkInfo(network);
        return nwInfo != null ? nwInfo.getTypeName() : defaultVal;
    }

    // Suppressing deprecation warning for ConnectivityManager enum types
    @SuppressWarnings("java:S1874")
    protected static boolean isLanType(ConnectivityManager connectivityManager, Network network){
        if (network == null) return false;
        NetworkInfo nwInfo = connectivityManager.getNetworkInfo(network);
        if (nwInfo == null) return false;
        switch( nwInfo.getType() ){
            case ConnectivityManager.TYPE_ETHERNET:
            case ConnectivityManager.TYPE_WIFI:
            case ConnectivityManager.TYPE_BLUETOOTH:
                return true;
            default:
                return false;
        }
    }

    /**
     * performs simple non-TLS socket connection test on specified network
     * @param network network interface to test
     * @return true = success, false = failure
     */
    // Suppressing deprecation warning for getTypeName and getNetworkInfo
    @SuppressWarnings("java:S1874")
    public static boolean testConnectOnNetwork(
        ConnectivityManager connectivityManager,
        Network network,
        String internetTestHostUrl,
        String internetTestHostPort,
        int connectTimeoutMs
    ){
        Socket testSocket = null;
        boolean socketConnectSuccess = false;
        SocketFactory sf = network.getSocketFactory();
        int port = 0;

        Timber.e("test connect on network %s", getNetworkTypeName(connectivityManager, network,"LAN"));
        try {
            port = Integer.parseInt(internetTestHostPort);
            testSocket = sf.createSocket();

            // relatively short timeout
            testSocket.setSoTimeout(connectTimeoutMs);

            // try DNS lookup - THIS DOESN'T OBEY THE TIMEOUT, AND CAN TAKE 50 SECONDS TO FAIL.
            // The DNS lookup can easily be combined in the socket connect, but the DNS request can be sent over the 'wrong' network interface
            // Using Network.getByName() is the only way I'm aware of that performs DNS on the specified network.
            InetAddress hostAddress = network.getByName(internetTestHostUrl);
            Timber.i( "host name %s, ip address %s", internetTestHostUrl, hostAddress.toString());
            // try to connect to host
            testSocket.connect(new InetSocketAddress(hostAddress, port), connectTimeoutMs);

            // success if we get here, else it would've thrown exception
            socketConnectSuccess = testSocket.isConnected();

        } catch (Exception e) {
            Timber.e(e);
        } finally {
            try {
                // always close the socket as this is just for connectivity test
                if (testSocket != null) {
                    testSocket.close();
                }
            } catch(Exception ignored){
                Timber.e("closing test socket");
            }
        }

        if( socketConnectSuccess ) {
            Timber.e("Host %s, port %d, network %s socket SUCCESSFUL", internetTestHostUrl, port, connectivityManager.getNetworkInfo(network).getTypeName() );
        } else {
            if( connectivityManager.getNetworkInfo(network) == null ) {
                Timber.e("Host %s, port %d, network info is NULL", internetTestHostUrl, port);
            } else {
                Timber.e("Host %s, port %d, network %s socket FAILURE", internetTestHostUrl, port, connectivityManager.getNetworkInfo(network).getTypeName());
            }
        }
        return socketConnectSuccess;
    }

    // Suppressing deprecation warning for getAllNetworks
    @SuppressWarnings("java:S1874")
    public static List<Network> findNetworksForTypes(ConnectivityManager connectivityManager, int[] networkTypes){
        // yes getAllNetworks() is deprecated in API 31, but there is no easy to use equivalent.
        // the reason for deprecation (This method does not provide any notification of network state changes, forcing apps to call it repeatedly. This is inefficient and prone to race conditions.)
        // does not apply in this case, as we use it sparingly
        final Network[] allNetworks = connectivityManager.getAllNetworks();
        List<Network> networks = new ArrayList<>();

        // find the network type
        for (Network nw : allNetworks) {
            NetworkCapabilities actNw = connectivityManager.getNetworkCapabilities(nw);
            if( actNw != null ){
                for( int nwType : networkTypes ){
                    if(actNw.hasTransport(nwType)){
                        networks.add(nw);
                        break;
                    }
                }
            }
        }

        if( networks.isEmpty() ) {
            Timber.e( "Couldn't find network type for network types:" );
            for( int nwType : networkTypes ){
                Timber.e( "   : %d", nwType );
            }
            return networks;
        }

        return networks;
    }
}
