package com.linkly.libengine.action.MenuOperations.admin;

import static com.linkly.libpositive.messages.IMessages.APP_RESTART_EVENT;

import com.linkly.libengine.action.IAction;
import com.linkly.libengine.jobs.EFTJobScheduleEvent;

import java.util.Date;

public class Restart extends IAction {

    private static boolean isRestartScheduled = false;

    @Override
    public String getName() {
        return "Restart";
    }

    @Override
    public void run() {
        /* we only do one so crash scenarios dont restart app once for each thread instanly */
        if (!isRestartScheduled) {
            isRestartScheduled = true;
            EFTJobScheduleEvent event = new EFTJobScheduleEvent(EFTJobScheduleEvent.EventType.CREATE, APP_RESTART_EVENT, new Date().getTime() + 500);
            d.getJobs().schedule(context, event);
        }
    }
}
