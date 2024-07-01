package com.linkly.libengine.engine.comms;

import android.content.Context;

import com.linkly.libengine.dependencies.IDependency;

public interface ICommsStatusMonitor {
    boolean open(IDependency d, Context context);
    boolean close(IDependency d);
}
