package com.linkly.libengine.action;

@SuppressWarnings("rawtypes")
public class JumpTo extends IAction {

    private Class jumpToAction = null;

    private JumpTo() {}

    public JumpTo(Class jumpToAction) {
        this.jumpToAction = jumpToAction;
    }

    @Override
    public String getName() {
        return "JumpTo";
    }

    @Override
    public void run() {

        d.getWorkflowEngine().setNextAction(this.jumpToAction);
    }
}
