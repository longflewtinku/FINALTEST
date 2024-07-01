package com.linkly.libengine.jobs;

import com.linkly.libpositive.events.PositiveEvent;
import com.linkly.libpositive.events.PositiveReadCardEvent;
import com.linkly.libpositive.events.PositiveTransEvent;

public class EFTJob extends PositiveEvent {

    PositiveEvent eventDetail;

    public EFTJob(EventType type) {
        super(type);
    }


    public EFTJob(PositiveEvent eventDetail) {
        super(eventDetail.getType());
        this.eventDetail = eventDetail;
    }

    public EFTJob(PositiveTransEvent eventDetail) {
        super(eventDetail.getType());
        this.eventDetail = eventDetail;
    }

    public EFTJob( PositiveReadCardEvent eventDetail ){
        super( eventDetail.getType() );
        this.eventDetail = eventDetail;
    }

    public EFTJob(EventType type, boolean startPayment) {
        super(type, startPayment);
    }

    public PositiveEvent getEventDetail() {
        return this.eventDetail;
    }

    public void setEventDetail(PositiveEvent eventDetail) {
        this.eventDetail = eventDetail;
    }
}
