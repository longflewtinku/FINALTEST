package com.linkly.libengine.action.Printing;

import static com.linkly.libengine.action.Printing.PrintFirst.ReceiptType.CUSTOMER;
import static com.linkly.libengine.action.Printing.PrintFirst.ReceiptType.MERCHANT;
import static com.linkly.libpositive.PosIntegrate.ResultResponse.RES_OK;
import static com.linkly.libpositive.PosIntegrate.ResultResponse.RES_TRANSACTION_NOT_FOUND;
import static com.linkly.libpositive.wrappers.PositiveError.NO_ERROR;
import static com.linkly.libpositive.wrappers.PositiveError.TRANSACTION_NOT_FOUND;

import com.linkly.libengine.action.IAction;
import com.linkly.libengine.action.check.CheckPrinter;
import com.linkly.libengine.engine.transactions.TransRec;
import com.linkly.libmal.IMalPrint;
import com.linkly.libmal.global.util.Util;
import com.linkly.libpositive.PosIntegrate;
import com.linkly.libpositive.events.PositiveTransEvent;
import com.linkly.libpositive.wrappers.PositiveError;
import com.linkly.libpositive.wrappers.PositiveErrorResponse;

import timber.log.Timber;

public class PrintReprintLastAuto extends IAction {

    PositiveTransEvent event;

    public PrintReprintLastAuto(PositiveTransEvent event) {
        this.event = event;
    }

    @Override
    public String getName() {
        return "PrintReprintLastAuto";
    }

    @Override
    public void run() {

        // Get the latest transaction record
        TransRec latest = TransRec.getLatestFinancialTxn();
        PositiveError err = NO_ERROR;
        PosIntegrate.ResultResponse resultResponse = RES_OK;

        if (latest != null && event != null) {

            // We need to set our current transaction to allow the print response to happen
            d.resetCurrentTransaction(latest);
            // The event is never saved. We pass in the one that has been populated and contains all our config required
            latest.setTransEvent(event);
            // Make sure we set the setPrintOnTerminalFlag. (This flag can get changed on the response of broadcast receipt)
            latest.setPrintOnTerminal(latest.getTransEvent().isUseTerminalPrinter());

            // if receipt type specified in request event, then use it. otherwise default to customer receipt
            PrintFirst.ReceiptType receiptType = CUSTOMER;
            String requestReceiptType = latest.getTransEvent().getReceiptCopyType();
            if( !Util.isNullOrEmpty(requestReceiptType) ) {
                if( requestReceiptType.equalsIgnoreCase("M") ) {
                    receiptType = MERCHANT;
                } else if( !requestReceiptType.equalsIgnoreCase("C") ) {
                    Timber.e( "unexpected receipt type value %s", latest.getTransEvent().getReceiptCopyType() );
                }
            }

            // Broadcast the latest customer receipt (Only)
            IMalPrint.PrinterReturn printingStatus = PrintFirst.print(d, latest, receiptType == MERCHANT, true, context, mal );
            if (!IMalPrint.PrinterReturn.SUCCESS.equals(printingStatus)) {
                CheckPrinter.handlePrinterCheckFailure(d, latest, printingStatus, PrintFirst.class);
            }

        } else {
            Timber.i( "Nothing to reprint");
            err = TRANSACTION_NOT_FOUND;
            resultResponse = RES_TRANSACTION_NOT_FOUND;
        }

        if(event != null) {
            // Handle the response
            PositiveErrorResponse response = new PositiveErrorResponse(err, resultResponse);
            // Send our final response to the host
            //send merchant number we get from request to response
            d.getMessages().sendReceiptResponse(context, response, event.getCommandSubCode(), event.getMerchantNumber());
        }
    }

}
