package com.linkly.libengine.action.MenuOperations.dev;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

import android.content.Intent;

import com.linkly.libengine.action.IAction;

public class KeyInject extends IAction {
    @Override
    public String getName() {
        return "KeyInject";
    }

    @Override
    public void run() {

        Intent intent = new Intent();
        intent.setAction("android.pax.ecr.injectKey");
        intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
        intent.addCategory(Intent.CATEGORY_DEFAULT);

        context.getApplicationContext().startActivity(intent);
    }
}
