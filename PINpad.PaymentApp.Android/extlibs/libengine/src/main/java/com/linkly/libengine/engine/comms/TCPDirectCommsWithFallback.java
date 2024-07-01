package com.linkly.libengine.engine.comms;

import static android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET;
import static android.net.NetworkCapabilities.TRANSPORT_BLUETOOTH;
import static android.net.NetworkCapabilities.TRANSPORT_ETHERNET;
import static android.net.NetworkCapabilities.TRANSPORT_USB;
import static android.net.NetworkCapabilities.TRANSPORT_WIFI;
import static com.linkly.libengine.debug.IDebug.DEBUG_COMMS_FALLBACK.FALLBACK_RECOVERED;
import static com.linkly.libengine.debug.IDebug.DEBUG_COMMS_FALLBACK.FALLBACK_TO_SECONDARY;
import static com.linkly.libengine.helpers.NetworkStatusCallback.DEFAULT_INTERNET_TEST_HOST_PORT;
import static com.linkly.libengine.helpers.NetworkStatusCallback.DEFAULT_INTERNET_TEST_HOST_URL;
import static com.linkly.libengine.helpers.NetworkStatusCallback.INTERNET_TEST_HOST_BACKOFF_TIMER_MSEC;
import static com.linkly.libengine.helpers.NetworkStatusCallback.INTERNET_TEST_HOST_CONNECT_TIMEOUT_MSEC;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Build;
import android.util.Pair;

import com.linkly.libengine.dependencies.IDependency;
import com.linkly.libengine.helpers.ConfigParsing;
import com.linkly.libengine.helpers.NetworkStatusCallback;
import com.linkly.libmal.MalFactory;

import java.util.List;

import timber.log.Timber;

public class TCPDirectCommsWithFallback extends TCPDirectComms {
    private static String internetTestHostUrl = DEFAULT_INTERNET_TEST_HOST_URL;
    private static String internetTestHostPort = DEFAULT_INTERNET_TEST_HOST_PORT;
    private ConnectivityManager connectivityManager = null;
    private NetworkStatusCallback lanCallback = null;
    private boolean fallbackMode = false;
    private boolean hasLan = false;
    private boolean hasSecondaryNetwork = false;
    private boolean fallbackCapable = false; // true if terminal is capable of fallback (has secondary network and SIM etc)
    private boolean fallbackEnabledInConfig = false; // true if comms fallback is enabled in config, false if not
    private Thread fallbackMonitorThread = null;

    private static void applyConfigParams(IDependency d){
        Pair<String, String> hostParts = ConfigParsing.parseInternetTestTarget(d, DEFAULT_INTERNET_TEST_HOST_URL, DEFAULT_INTERNET_TEST_HOST_PORT);
        internetTestHostUrl = hostParts.first;
        internetTestHostPort = hostParts.second;
    }

    @Override
    public boolean open(IDependency d) {
        boolean bNetworkUp = super.open(d);

        // get connectivity manager instance, required even if fallback not enabled in config
        setupConnectivityManager(MalFactory.getInstance().getMalContext()); // TODO: replaced with d.getContext with Mal. Need to fix this properly...

        fallbackEnabledInConfig = d.getPayCfg().isCommsFallbackEnabled();
        if( fallbackEnabledInConfig ){
            Timber.e( "comms fallback enabled");
            // grab config from paycfg
            applyConfigParams(d);

            // NOTE: the dynamic method for detecting LAN and secondary newtork links is not reliable.
            // Simpler and safer to assume both ar available and enable multi-path. No known drawbacks in doing this
            hasLan = true;
            hasSecondaryNetwork = true;

            setupMultipathComms();
        } else {
            Timber.e( "comms fallback disabled");
            fallbackCapable = false;
        }

        // setup comms status listener regardless if fallback is enabled or not
        setupListenerForInternetConnections();

        Timber.i( "open Done");
        // NOTE: this doesn't yet check if networks are up, but no caller even checks it except ip gateway proxy which is dead
        return bNetworkUp;
    }

    /**
     * sets up or disables multipath communications, depending if terminal has both lan and secondary networks, or not
     */
    private void setupMultipathComms(){
        // don't attempt fallback if disabled via config
        if( !fallbackEnabledInConfig ){
            return;
        }

        fallbackCapable = hasLan && hasSecondaryNetwork;

        Timber.i("active network info");
        getCurrentNetworkInfo(); // gets active network info

        Timber.e("has lan = %b, has secondary network = %b", hasLan, hasSecondaryNetwork);

        // enable multipath if fallbackCapable is true, so LAN and mobile can be concurrently connected, prefer LAN for initial connection
        boolean retVal = MalFactory.getInstance().getHardware().setMultipathCommsEnabled(fallbackCapable, false, 0);
        if(!retVal){
            // this should always succeed, if platform supports it. Assume failure indicates platform doesn't support this, set fallbackCapable to false so we don't try again
            Timber.e("Error enabling multipath, initial config");
            fallbackCapable = false;
        }

        if(fallbackCapable) {
            // debug after multipath change
            Timber.i("active network info after multipath");
            getCurrentNetworkInfo();
        }
    }

    /**
     * performs simple non-TLS socket connection test on specified network
     * @param network network interface to test
     * @return true = success, false = failure
     */
    // Suppressing deprecation warning for getTypeName and getNetworkInfo
    @SuppressWarnings("java:S1874")
    private boolean testConnectOnNetwork(Network network){
        return NetworkStatusCallback.testConnectOnNetwork(
                connectivityManager,
                network,
                internetTestHostUrl,
                internetTestHostPort,
                INTERNET_TEST_HOST_CONNECT_TIMEOUT_MSEC
        );
    }

    /**
     * returns a list of available LAN networks
     * @return lan networks
     */
    private List<Network> getLanNetworks(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return NetworkStatusCallback.findNetworksForTypes(connectivityManager,
                    new int[]{TRANSPORT_WIFI, TRANSPORT_ETHERNET, TRANSPORT_BLUETOOTH, TRANSPORT_USB});
        } else {
            return NetworkStatusCallback.findNetworksForTypes(connectivityManager,
                    new int[]{TRANSPORT_WIFI, TRANSPORT_ETHERNET, TRANSPORT_BLUETOOTH});
        }
    }

    /**
     * connect on available LAN network interfaces
     * performs simple non-TLS socket connection test
     *
     * @return true = connect successful, false = connect failed
     */
    private boolean testConnectOnLan(){
        // LAN can include any of these transport types
        final List<Network> networks = getLanNetworks();
        if( networks.isEmpty() ){
            Timber.e("error finding a LAN network interface");
            return false;
        }

        // test each LAN network interface for connectivity
        int success = 0;
        for( Network nw: networks ){
            if( testConnectOnNetwork(nw) ){
                success++;
            }
        }

        return success > 0;
    }

    @Override
    public boolean connect(IDependency d, int iTryCount) {
        // always try to send on default network. will be pointing to LAN if normal or secondary link if fallback
        // use a short connection timeout
        Timber.e("connecting on %s comms link", getFallbackModeLinkName());
        boolean result = super.connect(d, 1, 3);

        // enter fallback mode if we're not already and terminal is capable
        if( !result && enterFallbackMode(d) ){
            Timber.e("connecting on %s comms link", getFallbackModeLinkName());
            // transitioned to fallback mode okay, try again
            result = super.connect(d,1);
        }
        return result;
    }

    @Override
    public boolean close(IDependency d) {
        if( lanCallback != null ) {
            connectivityManager.unregisterNetworkCallback(lanCallback);
        }
        return super.close(d);
    }

    private String getFallbackModeLinkName(){
        return fallbackMode?"secondary/fallback":"primary/lan";
    }

    private void exitFallbackMode(IDependency d){
        // success! set LAN as preferred network, return
        // prefer LAN for initial connection
        // TODO: Work in decoupling this code.
        boolean retVal = MalFactory.getInstance().getHardware().setMultipathCommsEnabled(true, false, 1000);
        if(!retVal){
            Timber.e("Error enabling multipath, exiting fallback");
        }
        fallbackMode = false;
        d.getDebugReporter().reportCommsFallbackEvent(FALLBACK_RECOVERED);
    }

    // Ignore the interrupt exception here. seems harmless
    @SuppressWarnings("java:S2142")
    private boolean enterFallbackMode(IDependency d){
        Timber.e("entering fallback mode. falling back to secondary link");
        if( fallbackMode ){
            Timber.e("already in comms fallback mode");
            return false;
        }
        if( !fallbackCapable){
            Timber.e("Not trying comms fallback. Fallback disabled, or terminal not capable of comms fallback");
            return false;
        }
        // LAN failure, set mobile as default network and reconfigure
        // TODO: Work in decoupling this code.
        boolean retVal = MalFactory.getInstance().getHardware().setMultipathCommsEnabled(true, true, 1000);
        if(!retVal){
            Timber.e("Error enabling multipath, entering fallback");
        }
        fallbackMode = true;

        // send Z debug message to POS here
        d.getDebugReporter().reportCommsFallbackEvent(FALLBACK_TO_SECONDARY);

        // start thread here to monitor internet status on LAN
        // if previous thread is still running, interrupt it
        if( fallbackMonitorThread != null && fallbackMonitorThread.isAlive() ){
            Timber.e("monitor thread still active, terminating");
            fallbackMonitorThread.interrupt();
            try {
                // wait enough time to allow it to end itself properly
                Thread.sleep(INTERNET_TEST_HOST_CONNECT_TIMEOUT_MSEC+100L);
            } catch( Exception e){
                Timber.v(e);
            }
        }
        fallbackMonitorThread = new Thread(() -> {
            try {
                while (fallbackMode) {
                    Thread.sleep(INTERNET_TEST_HOST_BACKOFF_TIMER_MSEC);
                    if( testConnectOnLan() ){
                        Timber.e( "------------ connect on LAN success! exiting fallback mode -------------------" );
                        exitFallbackMode(d);
                    } else {
                        Timber.e("fallback thread LAN test failed, retry in %d sec", INTERNET_TEST_HOST_BACKOFF_TIMER_MSEC/1000);
                    }
                }
            } catch( Exception e ){
                Timber.v(e);
            }
        });
        fallbackMonitorThread.start();
        return true;
    }

    private void setupConnectivityManager(Context context){
        connectivityManager = context.getSystemService(ConnectivityManager.class);
    }

    void debugNetworkInfo(Network network){
        if( network == null ){
            Timber.e("network is NULL");
            return;
        }
        NetworkCapabilities caps = connectivityManager.getNetworkCapabilities(network);
        LinkProperties linkProperties = connectivityManager.getLinkProperties(network);

        Timber.i("--------------------------");
        Timber.i( "Network : %s", network.toString() );
        Timber.i( "NetworkCaps : %s", caps.toString() );
        Timber.i( "LinkProperties : %s", linkProperties.toString() );
        Timber.i("--------------------------");
    }

    void setupListenerForInternetConnections(){
        // listen for internet connectivity events on the store LAN connection (ethernet, usb or wifi)
        NetworkRequest.Builder builder = new NetworkRequest.Builder()
                .addTransportType(TRANSPORT_ETHERNET)
                .addTransportType(TRANSPORT_WIFI)
                .addTransportType(TRANSPORT_BLUETOOTH)
                .addCapability(NET_CAPABILITY_INTERNET);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            builder = builder.addTransportType(TRANSPORT_USB);
        }
        NetworkRequest requestLan = builder.build();

        lanCallback = new FallbackNetworkStatusCallback("LAN");
        connectivityManager.registerNetworkCallback(requestLan, lanCallback);
    }

    void getCurrentNetworkInfo(){
        Network currentNetwork = connectivityManager.getActiveNetwork();
        debugNetworkInfo(currentNetwork);
    }

    /**
     * private class that extends NetworkStatusCallback
     * purpose of this is to intercept the onAvailable event for LAN network types, and act on it
     */
    private class FallbackNetworkStatusCallback extends NetworkStatusCallback {
        public FallbackNetworkStatusCallback(String networkName) {
            super(networkName);
        }

        @Override
        public void onAvailable(Network network) {
            // if this is a LAN network type, and we previously didn't have a LAN connection then reconfigure multipath comms
            if( isLanType(connectivityManager, network) && !hasLan ){
                hasLan = true;
                setupMultipathComms();
            }
        }
    }

}