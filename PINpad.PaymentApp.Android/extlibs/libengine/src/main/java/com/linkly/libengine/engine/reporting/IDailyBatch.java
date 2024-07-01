package com.linkly.libengine.engine.reporting;

import com.linkly.libengine.config.PayCfg;
import com.linkly.libengine.dependencies.IDependency;

public interface IDailyBatch {

    Reconciliation generateDailyBatch(boolean isRec, IDependency payCfg);
}
