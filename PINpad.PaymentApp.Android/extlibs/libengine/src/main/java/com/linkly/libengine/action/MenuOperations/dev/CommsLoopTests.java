package com.linkly.libengine.action.MenuOperations.dev;

import static com.linkly.libpositive.events.PositiveEvent.EventType.AUTO_LOGON;
import static com.linkly.libui.IUIDisplay.ACTIVITY_ID.ACT_INPUT;
import static com.linkly.libui.IUIDisplay.SCREEN_ICON.PROCESSING_ICON;
import static com.linkly.libui.IUIDisplay.UIResultCode.OK;
import static com.linkly.libui.UIScreenDef.ACT_INFORMATION_SCREEN;
import static com.linkly.libui.UIScreenDef.ENTER_NUMBER_TRANSACTIONS;

import com.linkly.libengine.action.IAction;
import com.linkly.libengine.engine.EngineManager;
import com.linkly.libengine.engine.protocol.IProto;
import com.linkly.libengine.engine.transactions.TransRec;
import com.linkly.libpositive.events.PositiveTransEvent;
import com.linkly.libui.IUIDisplay;

import java.util.Date;
import java.util.HashMap;

import timber.log.Timber;

public class CommsLoopTests extends IAction {

    @Override
    public String getName() {
        return "Comms Loop Test";
    }

    private long getTickCount() {
        Date now = new Date();
        return now.getTime();
    }

    @Override
    public void run() {
        int numTrans = uiInputNumTests();
        if( numTrans <= 0 ) {
            ui.displayMainMenuScreen();
            return;
        }

        HashMap<String, Object> map = new HashMap<>();
        map.put(IUIDisplay.uiScreenIcon, PROCESSING_ICON);
        ui.showScreen(ACT_INFORMATION_SCREEN, map);

        IProto prot = d.getProtocol();
        TransRec trans = new TransRec(EngineManager.TransType.AUTO_LOGON, d);
        trans.setTransEvent(new PositiveTransEvent(AUTO_LOGON));
        int executed = 0;
        int passed = 0;
        int totalTestTimeSuccesses = 0;
        for (int i = 0; i < numTrans; i++) {
            long timeStart = getTickCount();
            boolean result = prot.authorize(trans);
            if( result ){
                long timeTest = getTickCount() - timeStart;
                totalTestTimeSuccesses += timeTest;
                Timber.e("test time %d msec", timeTest);
                passed++;
            }
            executed++;
            double averageTimeSuccess = (passed > 0) ? (double)totalTestTimeSuccesses/passed : 999999999;
            Timber.e( "Executed %d, %d passed, %d failed. Average time for successful tests %.0f msec",
                    executed, passed, executed-passed,
                    averageTimeSuccess);
        }

        ui.displayMainMenuScreen();
    }


    private int uiInputNumTests() {
        HashMap<String, Object> map = new HashMap<>();
        ui.showInputScreen(ENTER_NUMBER_TRANSACTIONS, map);
        IUIDisplay.UIResultCode res = ui.getResultCode(ACT_INPUT, IUIDisplay.LONG_TIMEOUT);
        if (res == OK) {
            return Integer.parseInt(ui.getResultText(ACT_INPUT, IUIDisplay.uiResultText1));
        } else {
            return 0;
        }
    }
}
