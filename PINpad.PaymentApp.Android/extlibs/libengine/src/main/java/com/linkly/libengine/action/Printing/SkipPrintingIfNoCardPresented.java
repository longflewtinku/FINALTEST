package com.linkly.libengine.action.Printing;

import static com.linkly.libengine.engine.transactions.properties.TCard.CaptureMethod.NOT_CAPTURED;

import com.linkly.libengine.action.IAction;
import com.linkly.libengine.action.TransactionFinalizer;
import com.linkly.libmal.global.util.Util;

public class SkipPrintingIfNoCardPresented extends IAction {
    @Override
    public String getName() {
        return "SkipPrintingIfNoCardPresented";
    }

    @Override
    public void run() {
        // if card not captured or PAN not read from card, treat the same - skip printing
        // note for ICC misread scenarios, capture method can be set, but PAN will be null or empty
        if (trans.getCard().getCaptureMethod() == NOT_CAPTURED || Util.isNullOrEmpty(trans.getCard().getMaskedPan())) {
            d.getWorkflowEngine().setNextAction(TransactionFinalizer.class);
        }
    }
}
