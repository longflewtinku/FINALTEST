package com.linkly.libengine.engine.comms;

import static android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET;
import static android.net.NetworkCapabilities.TRANSPORT_BLUETOOTH;
import static android.net.NetworkCapabilities.TRANSPORT_CELLULAR;
import static android.net.NetworkCapabilities.TRANSPORT_ETHERNET;
import static android.net.NetworkCapabilities.TRANSPORT_USB;
import static android.net.NetworkCapabilities.TRANSPORT_WIFI;
import static com.linkly.libui.IUIDisplay.String_id.STR_NO_INTERNET_BASE_NOT_REQUIRED;
import static com.linkly.libui.IUIDisplay.String_id.STR_NO_INTERNET_BASE_REQUIRED;

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
import com.linkly.libmal.global.util.Util;

import java.util.List;

import timber.log.Timber;

/*
Monitors availability of various Networks on the device, routing to DebugReporter.
Also tracks the internetEndowingNetwork and triggers notification to user in the case where that
Network is lost. By doing so, this also attempts to monitor Internet availability.
Also provides APIs for other parts of the app to update the status of Internet Availability, e.g.
if an RSA Logon fails due to a Connection issue it may be a pertinent time to update this mechanism.
Initial state is a challenge with such a mechanism, when the app starts the Internet is typically
already established and connection stable so the app cannot react to a new connection to determine
the Internet Endowing Network. If no Internet endowing Network has been determined, upon onLost of
any Network the notification to user will be triggered, this is a failsafe for cases when startup
has not detected the presence of the Internet yet it gets lost.
 */
public class CommsStatusMonitor extends NetworkStatusCallback implements ICommsStatusMonitor {
    private static final CommsStatusMonitor myInstance = new CommsStatusMonitor("Network");
    private ConnectivityManager connectivityManager = null;
    // Using context here is still bad but required due to the random util functions that have context as an argument.
    private Context context = null;

    // Tracks the last Network tested to supply Internet.
    // This is future proof up until Android 12 (API 31): https://source.android.com/docs/core/connect/wifi-sta-sta-concurrency
    private Network internetEndowingNetwork = null;
    private IDependency d;

    private Thread internetProbeThread = null;

    private boolean isProbingForInternet = false;

    private boolean isDependantOnBaseForInternet = false;

    private String customNotificationSoundPath = null;

    private CommsStatusMonitor(String networkName) {
        super(networkName);
    }

    public static CommsStatusMonitor getInstance() {
        return myInstance;
    }

    // TODO: IDependency is a global dependency that contains circular dependencies. Slow will need to work is decoupling etc.
    @Override
    public boolean open(IDependency d, Context context) {
        this.d = d;
        this.context = context;
        try {
            connectivityManager = context.getSystemService(ConnectivityManager.class);
        } catch (Exception e) {
            Timber.e(e);
            return false;
        }

        // create listener for all network types
        NetworkRequest.Builder builder = new NetworkRequest.Builder()
                .addTransportType(TRANSPORT_ETHERNET)
                .addTransportType(TRANSPORT_WIFI)
                .addTransportType(TRANSPORT_BLUETOOTH)
                .addTransportType(TRANSPORT_CELLULAR)
                .addCapability(NET_CAPABILITY_INTERNET);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            builder = builder.addTransportType(TRANSPORT_USB);
        }
        NetworkRequest requestLan = builder.build();
        connectivityManager.registerNetworkCallback(requestLan, this);
        testActiveNetworkForInternet();
        return true;
    }

    @Override
    public boolean close(IDependency d) {
        if (connectivityManager != null) {
            connectivityManager.unregisterNetworkCallback(this);
        }
        return true;
    }

    @Override
    public void onAvailable(Network network) {
        Timber.e("onAvailable...network: %s - %s", getNetworkTypeName(connectivityManager, network, networkName), network);
        d.getDebugReporter().reportNetworkDiagnosticEvent(String.format("network %s onAvailable", getNetworkTypeName(connectivityManager, network, networkName)));
        if (network.equals(connectivityManager.getActiveNetwork())) {
            // Internet Availability cares only for the Active Network.
            checkForHWBaseInternetDependence(network);
            // Remove any notice regarding No Internet | return to base
            Pair<String, String> hostParts = ConfigParsing.parseInternetTestTarget(d, DEFAULT_INTERNET_TEST_HOST_URL, DEFAULT_INTERNET_TEST_HOST_PORT);
            if (NetworkStatusCallback.testConnectOnNetwork(
                    connectivityManager,
                    network,
                    hostParts.first,
                    hostParts.second,
                    INTERNET_TEST_HOST_CONNECT_TIMEOUT_MSEC
            )) {
                internetEndowingNetwork = network;
                d.getAppCallbacks().cancelInternetAvailabilityNotice();
                // Could be still checking/polling for network connection on an "old network" object.
                // As we are cancelling this will mean we need to display internet available again.
                teardownInternetProbe();
            }
        }
    }

    @Override
    public void onLosing(Network network, int maxMsToLive) {
        Timber.e("onLosing...network: %s", getNetworkTypeName(connectivityManager, network, networkName));
        d.getDebugReporter().reportNetworkDiagnosticEvent(String.format("network %s onLosing", getNetworkTypeName(connectivityManager, network, networkName)));
    }

    @Override
    public void onLost(Network network) {
        Timber.e("onLost...network: %s", getNetworkTypeName(connectivityManager, network, networkName));
        d.getDebugReporter().reportNetworkDiagnosticEvent(String.format("network %s onLost", getNetworkTypeName(connectivityManager, network, networkName)));
        handleNetworkLost(network);
    }

    // note: Don't expect to see this unless requestNetwork is used and in newer versions of
    //  Android where the fix is present: https://issuetracker.google.com/issues/144891976
    @Override
    public void onUnavailable() {
        Timber.e("onUnavailable...network: %s", networkName);
        d.getDebugReporter().reportNetworkDiagnosticEvent(String.format("network %s onUnavailable", networkName));
    }

    @Override
    public void onCapabilitiesChanged(Network network, NetworkCapabilities networkCapabilities) {
        Timber.e("onCapabilitiesChanged...network: %s, capabilities: %s", getNetworkTypeName(connectivityManager, network, networkName), networkCapabilities);
        d.getDebugReporter().reportNetworkDiagnosticEvent(String.format("network %s onCapabilitiesChanged, %s", getNetworkTypeName(connectivityManager, network, networkName), networkCapabilities));
    }

    @Override
    public void onLinkPropertiesChanged(Network network, LinkProperties linkProperties) {
        Timber.e("onLinkPropertiesChanged...network: %s, linkProperties: %s", getNetworkTypeName(connectivityManager, network, networkName), linkProperties);
        d.getDebugReporter().reportNetworkDiagnosticEvent(String.format("network %s onLinkPropertiesChanged, %s", getNetworkTypeName(connectivityManager, network, networkName), linkProperties));
    }

    @Override
    public void onBlockedStatusChanged(Network network, boolean blocked) {
        Timber.e("onBlockedStatusChanged...network: %s, blocked: %b", getNetworkTypeName(connectivityManager, network, networkName), blocked);
        d.getDebugReporter().reportNetworkDiagnosticEvent(String.format("network %s onBlockedStatusChanged, blocked = %b", getNetworkTypeName(connectivityManager, network, networkName), blocked));
    }

    // Note this is applied to the Channel in Oreo+ so only used there the first time when the
    //  Channel is setup.
    public void setCustomNotificationSoundPath(String path) {
        Timber.d("setCustomNotificationSoundPath...path: %s", path);
        customNotificationSoundPath = path;
    }

    // API for any aspect of the system that deals with the Internet to update the overall status
    //  as tracked here based on responses/timeouts/whatever.
    public void notifyInternetLost() {
        Timber.d("notifyInternetLost...");
        handleInternetLost(internetEndowingNetwork);
    }

    // API for any aspect of the system that deals with the Internet to update the overall status
    //  as tracked here based on indication of Internet being available.
    public void notifyInternetEstablished() {
        Timber.d("notifyInternetEstablished...");
        Network activeNetwork = connectivityManager.getActiveNetwork();
        if (internetEndowingNetwork != null && !internetEndowingNetwork.equals(activeNetwork)) {
            // The Network supposedly providing Internet wasn't being tracked here, which is something
            //  to squeeze out of existence if possible.
            Timber.e("Had lost track of Internet endowing Network!");
        }
        internetEndowingNetwork = activeNetwork;
        teardownInternetProbe();
        d.getAppCallbacks().cancelInternetAvailabilityNotice();
    }

    // Note that Network can remain established while Internet is lost, hence the split of this method
    //  and handleInternetLost method.
    private void handleNetworkLost(Network network) {
        if (network.equals(internetEndowingNetwork)) {
            internetEndowingNetwork = null;
            handleInternetLost(network);
        }
    }

    /***
     * Stops our "Internet" probing thread running.
     * If a thread was active and "interrupted" will return true. Otherwise false
     * @return boolean if thread was running and stopped, else false.
     */
    private boolean teardownInternetProbe() {
        boolean wasProbing = false;
        // if previous thread is still running, interrupt it
        if (internetProbeThread != null && internetProbeThread.isAlive()) {
            Timber.e("monitor thread still active, terminating");
            internetProbeThread.interrupt();
            try {
                // wait enough time to allow it to end itself properly
                Thread.sleep(INTERNET_TEST_HOST_CONNECT_TIMEOUT_MSEC + 100L);
            } catch (InterruptedException ie) {
                Timber.v(ie);
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                Timber.v(e);
            } finally {
                wasProbing = true;
            }
        }
        isProbingForInternet = false;
        return wasProbing;
    }

    // Used to establish initial state.
    private void testActiveNetworkForInternet() {
        Timber.d("testActiveNetworkForInternet...");
        Network activeNetwork = connectivityManager.getActiveNetwork();
        if (activeNetwork == null) return;
        checkForHWBaseInternetDependence(activeNetwork);
        Pair<String, String> hostParts = ConfigParsing.parseInternetTestTarget(d, DEFAULT_INTERNET_TEST_HOST_URL, DEFAULT_INTERNET_TEST_HOST_PORT);
        if (NetworkStatusCallback.testConnectOnNetwork(
                connectivityManager,
                activeNetwork,
                hostParts.first,
                hostParts.second,
                INTERNET_TEST_HOST_CONNECT_TIMEOUT_MSEC
        )) {
            internetEndowingNetwork = activeNetwork;
        }
    }

    private List<Network> getMobileNetworks(){
        return NetworkStatusCallback.findNetworksForTypes(connectivityManager, new int[]{TRANSPORT_CELLULAR});
    }

    private boolean testConnectOnMobileNetworks(){
        // LAN can include any of these transport types
        final List<Network> networks = getMobileNetworks();
        if(networks.isEmpty()){
            Timber.e("error finding a Mobile network interface");
            return false;
        }

        // test each LAN network interface for connectivity
        Pair<String, String> hostParts = ConfigParsing.parseInternetTestTarget(d, DEFAULT_INTERNET_TEST_HOST_URL, DEFAULT_INTERNET_TEST_HOST_PORT);
        int success = 0;
        for(Network network: networks){
            if(NetworkStatusCallback.testConnectOnNetwork(
                    connectivityManager,
                    network,
                    hostParts.first,
                    hostParts.second,
                    INTERNET_TEST_HOST_CONNECT_TIMEOUT_MSEC)) {
                success++;
            }
        }

        return success > 0;
    }

    private void handleInternetLost(Network network) {
        Timber.d("handleInternetLost...network: %s", network);
        // Network is assumed still active, so commence polling for reestablished Internet.
        teardownInternetProbe();
        internetProbeThread = new Thread(() -> {
            try {
                isProbingForInternet = true;
                boolean notified = false;
                while (isProbingForInternet) {
                    Thread.sleep(INTERNET_TEST_HOST_BACKOFF_TIMER_MSEC);
                    Pair<String, String> hostParts = ConfigParsing.parseInternetTestTarget(d, DEFAULT_INTERNET_TEST_HOST_URL, DEFAULT_INTERNET_TEST_HOST_PORT);
                    if (NetworkStatusCallback.testConnectOnNetwork(
                            connectivityManager,
                            network,
                            hostParts.first,
                            hostParts.second,
                            INTERNET_TEST_HOST_CONNECT_TIMEOUT_MSEC
                    ) || testConnectOnMobileNetworks()) {
                        d.getAppCallbacks().cancelInternetAvailabilityNotice();
                        teardownInternetProbe();
                    } else if(!notified) {
                        // we don't actually know if we have lost internet/network.
                        // Only that we couldn't connect to a host.
                        // Only display ONCE we have tried hit our TEST host url.
                        // Create notice regarding No Internet | return to baseIUIDisplay
                        potentiallyDisplayInternetAvailabilityNotice();
                        notified = true;
                    }
                }
            } catch (InterruptedException ie) {
                Timber.v(ie);
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                Timber.v(e);
            }
        });
        internetProbeThread.start();
    }

    private void potentiallyDisplayInternetAvailabilityNotice() {
        // If context has been garbage collected away due to low memory can just check.
        // Usually if this happens we will be closing anyway
        if (context != null && !Util.isInAirplaneMode(context)) {
            d.getAppCallbacks().displayInternetAvailabilityNotice(getMessageForCommsDropout(), customNotificationSoundPath);
        }
    }

    // For the A920 and A920 Pro, Ethernet can only ever be provided over the Hardware Base.
    // If such a combo has been detected, then until the app restarts expect that Internet would
    //  only be endowed by that Ethernet Network (well at least provide messaging as such).
    private void checkForHWBaseInternetDependence(Network network) {
        if (isDependantOnBaseForInternet) return;
        isDependantOnBaseForInternet = (connectivityManager.getNetworkCapabilities(network)
                .hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) &&
                // TODO: Fix this singleton access
                (MalFactory.getInstance().getHardware().getModel().equals("A920") ||
                        MalFactory.getInstance().getHardware().getModel().equals("A920PRO")));
    }

    private String getMessageForCommsDropout() {
        if (isDependantOnBaseForInternet) {
            return d.getPrompt(STR_NO_INTERNET_BASE_REQUIRED);
        } else {
            return d.getPrompt(STR_NO_INTERNET_BASE_NOT_REQUIRED);
        }
    }
}
