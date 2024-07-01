package com.linkly.libengine.action;

import static com.linkly.libui.IUIDisplay.UIResultCode.UNKNOWN;

import com.linkly.libmal.global.util.Util;
import com.linkly.libui.display.Display;


public class InitialProcessing extends IAction {

    @Override
    public String getName() {
        return "InitialProcessing";
    }

    @Override
    public void run() {
        trans.setFinalised(false);
        trans.setApproved(false);

        // Old ResultCode could still be lingering from the previous transaction, so clear it out before starting a new transaction
        Display.insertResultCode(UNKNOWN);

        // Any started transaction can be in offline mode if it is allowed (in override parameters)
        // and the device is in airplane mode

        trans.setStartedInOfflineMode(d.getPayCfg().isOfflineFlightModeAllowed() && Util.isInAirplaneMode(context));

    }
}
