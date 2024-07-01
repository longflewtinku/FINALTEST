package com.linkly.libengine.debug;

import android.content.Context;

import com.linkly.libengine.helpers.ECRHelpers;
import com.linkly.libpositive.messages.IMessages;
import com.linkly.libpositive.wrappers.TagDataToPOS;
import com.pax.dal.entity.TrackData;

import timber.log.Timber;
public class DebugReport implements IDebug {
    private static DebugReport ourInstance = new DebugReport();

    public static DebugReport getInstance() {
        return ourInstance;
    }

    // Currently required to maintain context until a refactor happens.
    // Requirement is to decouple IMessages etc to be cleaner.
    private Context context;
    private IMessages posMessageRouting = null;

    public void init(Context context, IMessages posMessageRouting) {
        this.context = context;
        this.posMessageRouting = posMessageRouting;
    }

    @Override
    public void reportDebugAccountSelect( DEBUG_ACCOUNT accountType ) {
        this.reportDebugEvent( DEBUG_EVENT.ACCOUNT_SELECTED, accountType.accountType );
        Timber.e("Account Selected : %s", accountType);
    }

    @Override
    public void reportCancelSelect( DEBUG_POSITION screenType ) {
        this.reportDebugEvent( DEBUG_EVENT.CANCEL_PRESSED, screenType.position );
        Timber.e("Cancel Pressed on Screen : %s", screenType);
    }

    @Override
    public void reportTimeout( DEBUG_POSITION screenType ) {
        this.reportDebugEvent( DEBUG_EVENT.OPERATOR_TIMEOUT, screenType.position );
        Timber.e("Operator Timeout on Screen : %s", screenType);
    }


    @Override
    public void reportDebugEvent( DEBUG_EVENT debugEvent, String eventData) {
        if ( posMessageRouting != null && context != null) {
            posMessageRouting.sendDebugEvent(context, debugEvent.getCommand(), eventData );
        } else {
            Timber.e("PosMessage Routing: %s/Context: %s", posMessageRouting, context);
        }
    }

    @Override
    public void reportSignatureKeyPressed( DEBUG_KEY keyPressed ) {
        this.reportDebugEvent( DEBUG_EVENT.SIGNATURE_KEY, keyPressed.keyPressed );
    }

    @Override
    public void reportYesNoKeyPressed( DEBUG_KEY keyPressed ) {
        this.reportDebugEvent( DEBUG_EVENT.KEY_PRESS, keyPressed.keyPressed );
    }

    @Override
    public void reportCommsFallbackEvent( DEBUG_COMMS_FALLBACK option ){
        this.reportDebugEvent( DEBUG_EVENT.COMMS_FALLBACK, option.option );
    }

    @Override
    public void reportNetworkDiagnosticEvent( String text ){
        this.reportDebugEvent( DEBUG_EVENT.NETWORK_DIAGNOSTIC, text );
    }

    @Override
    public void reportCardData( TagDataToPOS.CardEntryModeTag mode, TrackData trackData ) {
        StringBuilder trackDetails = new StringBuilder(  );
        String COLON_SEPARATOR = ":";

        if( null == mode || null == trackData )
            return;

        // Expected output String - <Presentation Type>:T1<track1 data>:T2<track2 data>:T3<track3 data>
        // First element added is the <Presentation Type> as '=' delimiter is handled externally.
        trackDetails
                .append(mode.getMode());

        if(trackData.getTrack1() != null && !trackData.getTrack1().isEmpty()){
            trackDetails
                    .append(COLON_SEPARATOR)
                    .append("T1") // Track 1
                    .append(trackData.getTrack1());
        }

        if( trackData.getTrack2() != null && !trackData.getTrack2().isEmpty()){
            trackDetails
                    .append(COLON_SEPARATOR)
                    .append("T2") // Track 2
                    .append(ECRHelpers.getMaskedTrackData(trackData.getTrack2()));
        }

        if(trackData.getTrack3() != null && !trackData.getTrack3().isEmpty()){
            trackDetails
                    .append(COLON_SEPARATOR)
                    .append("T3")
                    .append(trackData.getTrack3());
        }

        reportDebugEvent(DEBUG_EVENT.CARD_DATA, trackDetails.toString());
    }

    @Override
    public void reportCardData( TagDataToPOS.CardEntryModeTag mode, String track2 ) {
        TrackData trackData = new TrackData();

        trackData.setTrack2( track2 );
        this.reportCardData( mode, trackData );
    }
}
