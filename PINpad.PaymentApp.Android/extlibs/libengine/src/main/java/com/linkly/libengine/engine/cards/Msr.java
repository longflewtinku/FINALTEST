package com.linkly.libengine.engine.cards;

import com.linkly.libengine.dependencies.IDependency;
import com.linkly.libengine.engine.transactions.TransRec;
import com.linkly.libengine.engine.transactions.properties.TCard;
import com.linkly.libengine.overrides.CoreOverrides;
import com.linkly.libengine.status.IStatus;
import com.linkly.libpositive.wrappers.TagDataToPOS;
import com.linkly.libsecapp.IP2PMsr;
import com.pax.dal.entity.TrackData;

import timber.log.Timber;

public class Msr {
    private static Msr ourInstance = new Msr();

    private Msr() {
    }

    public static Msr getInstance() {
        return ourInstance;
    }

    public void start(IDependency d) {
        IP2PMsr ip2PMsr = d.getP2PLib().getIP2PMsr();
        TransRec trans = d.getCurrentTransaction();
        TCard card = trans.getCard();
        long MSRTimeTaken = 0;
        d.getStatusReporter().reportStatusEvent( IStatus.STATUS_EVENT.STATUS_TRANS_MSR , trans.isSuppressPosDialog() );
        MSRTimeTaken = System.currentTimeMillis();
        // first, try to get the fake track data (for automated testing)
        TrackData trackData = CoreOverrides.get().getMagstripeTrackData();
        // if fake track data not present - see if there is the real one
        if (trackData == null) {
            trackData = ip2PMsr.readFromLastResult();
        }

        // TODO: pass the "bad card read" info back
        if (trackData != null) {
            card.updateTrackData(d, trackData, trans);
            d.getDebugReporter().reportCardData( TagDataToPOS.CardEntryModeTag.SWIPE, trackData );
            MSRTimeTaken = System.currentTimeMillis() - MSRTimeTaken;
            Timber.e("MSR Time Taken: %d", MSRTimeTaken);
        }
    }

}
