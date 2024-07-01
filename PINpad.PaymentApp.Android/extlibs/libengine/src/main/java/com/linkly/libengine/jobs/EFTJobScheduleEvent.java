package com.linkly.libengine.jobs;

import com.linkly.libpositive.events.PositiveScheduledEvent;

public class EFTJobScheduleEvent extends PositiveScheduledEvent {
    public EFTJobScheduleEvent(EventType type, String action, long triggerTime) {
        super(type, action, triggerTime);
    }

}
