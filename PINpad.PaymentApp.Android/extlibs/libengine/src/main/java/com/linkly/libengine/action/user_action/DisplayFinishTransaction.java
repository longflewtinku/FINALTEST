package com.linkly.libengine.action.user_action;

import static com.linkly.libpositive.messages.IMessages.APP_FINISH_TRANSACTION_EVENT;

import android.content.Intent;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.linkly.libengine.action.IAction;
import com.linkly.libmal.MalFactory;

public class DisplayFinishTransaction extends IAction {

    @Override
    public String getName() {
        return "DisplayFinishTransaction";
    }

    @Override
    public void run() {
        Intent tempIntent = new Intent();
        
        tempIntent.setAction(APP_FINISH_TRANSACTION_EVENT);
        LocalBroadcastManager.getInstance(MalFactory.getInstance().getMalContext()).sendBroadcast(tempIntent);
    }
}
