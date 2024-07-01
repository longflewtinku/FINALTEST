package com.linkly.libengine.workflow;

import androidx.annotation.Nullable;

import com.linkly.libengine.engine.EngineManager;

public interface IWorkflowFactory {
    Workflow getWorkflow(@Nullable EngineManager.TransType tType);
    Workflow getWorkflow(String name);
}
