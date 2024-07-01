package com.linkly.libengine.status;

import static com.linkly.libengine.status.IStatus.STATUS_EVENT.NOT_SET;

import com.linkly.libengine.engine.Engine;
import com.linkly.libmal.MalFactory;
import com.linkly.libmal.global.util.Util;

import java.util.Objects;

import timber.log.Timber;


public class StatusReport implements IStatus {

    private static STATUS_EVENT lastStatus = NOT_SET;
    private static String lastFreeFormText = "";
    private static StatusReport ourInstance = new StatusReport();

    public static StatusReport getInstance() {
        return ourInstance;
    }

    @Override
    public STATUS_EVENT getLastStatus() {
        return lastStatus;
    }

    @Override
    public void reportStatusEvent(STATUS_EVENT event,  boolean suppressPosDialog) {
        if (event != lastStatus && !suppressPosDialog) {
            lastStatus = event;
            lastFreeFormText = "";
            Objects.requireNonNull(Engine.getMessages()).sendTransactionStatus(MalFactory.getInstance().getMalContext(), lastStatus.displayName, lastStatus.ordinal());
        }
    }


    @Override
    public String convertToFreeForm(String line1, String line2) {
        return String.format("%s\n%s", line1, line2);
    }

    @Override
    public void reportStatusEvent(STATUS_EVENT event, String freeFormText,  boolean suppressPosDialog) {
        if(suppressPosDialog) {
            return;
        }
        if(Util.isNullOrEmpty(freeFormText) ) {
            Timber.e( "error - invalid or empty free-form text passed. sending status event without free-form text");
            reportStatusEvent(event, suppressPosDialog);
            return;
        }
        // combine event.displayName and freeFormText together
        String combinedStatusString = event.displayName + "|" + freeFormText;
        // if event or free-form text has changed, send status request, else ignore
        if (event != lastStatus || !freeFormText.equals(lastFreeFormText) ) {
            lastStatus = event;
            lastFreeFormText = freeFormText;
            Objects.requireNonNull(Engine.getMessages()).sendTransactionStatus(MalFactory.getInstance().getMalContext(), combinedStatusString, lastStatus.ordinal());
        }
    }
}
