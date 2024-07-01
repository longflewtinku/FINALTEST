package com.linkly.libengine.engine.cards;

import static com.linkly.libengine.status.IStatus.STATUS_EVENT.STATUS_ERR_CHIP_UNREADABLE;
import static com.linkly.libsecapp.IP2PCard.CardType.CT_CTLS_FAULTY;
import static com.linkly.libsecapp.IP2PCard.CardType.CT_EMV_FAULTY;
import static com.linkly.libsecapp.IP2PCard.CardType.CT_MSR;

import com.linkly.libengine.engine.Engine;
import com.linkly.libengine.overrides.CoreOverrides;
import com.linkly.libsecapp.IP2PCard;
import com.linkly.libsecapp.P2PLib;

import java.util.concurrent.Callable;

import timber.log.Timber;

public class CardGetThread implements Callable<IP2PCard.CardType> {
    private final int timeoutMs;
    private final boolean swipeEnabled;
    private final boolean emvEnabled;
    private final boolean ctlsEnabled;


    public CardGetThread(int timeoutMS, boolean getMsr, boolean getEmv, boolean getCtls) {
        timeoutMs = timeoutMS;
        swipeEnabled = getMsr;
        emvEnabled = getEmv;
        ctlsEnabled = getCtls;
    }

    @Override
    public IP2PCard.CardType call() {
        Timber.e("-----CardGetThread: Run - Timeout: %d ms -----", timeoutMs);

        IP2PCard.CardType cType;
        IP2PCard iMalCard = P2PLib.getInstance().getIP2PCard();
        if (CoreOverrides.get().isMagstripeTrackDataReady()) {   // hack for automated tests for magstripe
            cType = CT_MSR;
            Timber.i("GetCard returned %s", cType);
        } else {
            // Blocking
            cType = iMalCard.cardGet(
                    swipeEnabled,
                    emvEnabled,
                    ctlsEnabled,
                    timeoutMs,
                    false);

            if (cType == CT_EMV_FAULTY) {
                // Note - in some situations, e.g. QueryCard, getCurrentTransaction() will return null; so prevent a crash
                // here, by checking for non-null before accessing 'isSuppressDialog()'.
                Engine.getDep().getStatusReporter().reportStatusEvent(STATUS_ERR_CHIP_UNREADABLE,
                        Engine.getDep().getCurrentTransaction() != null && Engine.getDep().getCurrentTransaction().isSuppressPosDialog());
                Timber.e("CardGetThread: Faulty EMV read");
            } else if (cType == CT_CTLS_FAULTY) {
                Timber.e("CardGetThread: Faulty CTLS read");
            }

        }

        Timber.e("CardType: %s", cType);
        Timber.e("-----Finished Card Read-----");

        return cType;
    }
}