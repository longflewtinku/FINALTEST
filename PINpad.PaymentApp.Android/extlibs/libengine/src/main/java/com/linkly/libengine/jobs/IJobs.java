package com.linkly.libengine.jobs;

import android.content.Context;

import com.linkly.libengine.dependencies.IDependency;
import com.linkly.libpositive.events.PositiveEvent;

public interface IJobs {

    boolean add(EFTJob job);

    boolean schedule(Context context, EFTJobScheduleEvent event);

    boolean pending();

    boolean perform(IDependency d, Context context);

    PositiveEvent getNext();
}
