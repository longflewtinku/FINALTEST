package com.linkly.libengine.action.user_action;

import static com.linkly.libsecapp.IP2PCard.CardType.CT_MSR;
import static com.linkly.libsecapp.IP2PCard.CardType.CT_NONE;

import com.linkly.libengine.action.IAction;
import com.linkly.libengine.helpers.ECRHelpers;
import com.linkly.libpositive.events.PositiveReadCardEvent;
import com.linkly.libpositive.wrappers.PositiveReadCardResult;
import com.linkly.libpositive.wrappers.TagDataToPOS;
import com.linkly.libsecapp.IP2PCard;
import com.linkly.libsecapp.IP2PMsr;
import com.linkly.libsecapp.P2PLib;
import com.pax.dal.entity.TrackData;

import timber.log.Timber;

/**
 * Action to read card without UI prompt (used for slave mode)
 */
public class SilentCardReadCommand extends IAction {

    private final PositiveReadCardResult positiveReadCardResult = new PositiveReadCardResult();

    boolean cardReadAborted = false;

    public SilentCardReadCommand(PositiveReadCardEvent positiveReadCardEvent) {
        this.positiveReadCardResult.setType(positiveReadCardEvent.getType());
    }

    @Override
    public String getName() {
        return "SilentCardReadCommand";
    }

    @Override
    public void run() {
        startCardRead();
        if (!cardReadAborted) {
            sendCardReadResponse();
        }
    }

    @Override
    public boolean cancellableAction() {
        return true;
    }

    @Override
    public void cancel() {
        cardReadAborted = true;
        try {
            Timber.d("Cancelling card read");
            d.getP2PLib().getIP2PCard().cardGetCancel();
        } catch (Exception e) {
            Timber.w(e);
        }
    }

    private void startCardRead() {
        Timber.d("Read card: Start card read in background, thread %s", Thread.currentThread());
        boolean cardRead = false;

        IP2PCard iMalCard = d.getP2PLib().getIP2PCard();
        iMalCard.cardReset(true);

        IP2PCard.CardType cType = CT_NONE;

        while (CT_NONE == cType) {
            Timber.d("Read card: in loop");
            // Expect the Mag Card only to be swiped; 5secs
            cType = iMalCard.cardGet(true, false, false, 30000, false);

            Timber.d("Read card: got cType %s", cType);
            if (CT_MSR == cType) {
                IP2PMsr msr = d.getP2PLib().getIP2PMsr();
                if (msr != null) {
                    TrackData trackData = msr.readFromLastResult();
                    if (trackData != null) {
                        cardRead = true;
                        packCardResult(trackData);
                        Timber.d("Read card: result set");
                        break;
                    }
                }
            }
        }

        // If the card reading has any sort of error, then send cancelled response
        if (!cardRead) {
            packCancelledResponse();
        }
    }

    private void packCardResult(TrackData trackData) {
        Timber.d("track1Data = %s", trackData.getTrack1());
        Timber.d("track2Data = %s", trackData.getTrack2());
        Timber.d("track3Data = %s", trackData.getTrack3());
        this.positiveReadCardResult.setTrack2Data(trackData.getTrack2());
        this.positiveReadCardResult.setTrack1Or3Data(trackData.getTrack2().length() > 0 ? trackData.getTrack1() : trackData.getTrack3());
        this.positiveReadCardResult.setResponseCode("00");
        this.positiveReadCardResult.setResponseText("APPROVED");

        this.positiveReadCardResult.setTrack1Present(trackData.getTrack1() != null && !trackData.getTrack1().isEmpty());
        this.positiveReadCardResult.setTrack2Present(trackData.getTrack2() != null && !trackData.getTrack2().isEmpty());
        this.positiveReadCardResult.setTrack3Present(trackData.getTrack3() != null && !trackData.getTrack3().isEmpty());

        this.positiveReadCardResult.getTagDataToPOS().setCem(TagDataToPOS.CardEntryModeTag.SWIPE);
    }

    private void packCancelledResponse() {
        this.positiveReadCardResult.setResponseCode("DECLINED");
        this.positiveReadCardResult.setResponseCode("ZZ");
    }

    private void sendCardReadResponse() {
        ECRHelpers.ipcSendCardReadResponse(d, this.positiveReadCardResult, context);
    }
}