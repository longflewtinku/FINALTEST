package com.linkly.libengine.workflow;

import com.linkly.libengine.action.check.CheckP2P;
import com.linkly.libengine.action.user_action.BackToIdlePosNotification;
import com.linkly.libengine.action.user_action.CardReadCommand;
import com.linkly.libengine.action.user_action.DisplayFinishTransaction;
import com.linkly.libpositive.events.PositiveReadCardEvent;

public class CardRead extends Workflow {
    public CardRead(PositiveReadCardEvent readCardEvent) {
        // check if p2pe is running
        this.addAction(new CheckP2P());
        this.addAction(new CardReadCommand(readCardEvent));
        /*
         MW:
         Even if this is not a transaction, we need to finish the activity & exit
         Hence DisplayFinishTransaction is called
         */
        this.addAction(new DisplayFinishTransaction());
        this.addAction(new BackToIdlePosNotification());
    }
}
