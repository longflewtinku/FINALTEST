package com.linkly.libengine.action.MenuOperations.admin;

import static com.linkly.libengine.engine.transactions.properties.TProtocol.MessageStatus.FINALISED;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.ReversalReason.POWER_FAIL;

import com.linkly.libengine.action.IAction;
import com.linkly.libengine.engine.protocol.IProto;
import com.linkly.libengine.engine.transactions.TransRec;
import com.linkly.libengine.helpers.ECRHelpers;

import timber.log.Timber;

public class PowerFailCheck extends IAction {
    @Override
    public String getName() {
        return "PowerFailCheck";
    }

    // Modification if we have a power failed transaction
    private void setPowerFail(TransRec rec, boolean reversalRequired) {
        d.getProtocol().setInternalRejectReason(rec, IProto.RejectReasonType.POWER_FAIL);

        if(reversalRequired) {
            rec.setToReverse(POWER_FAIL);
        }

        // Set the transaction as declined
        rec.setApproved(false);
        rec.setDeclined(true); // declined by power fail.
        rec.setCancelled(false);
    }

    @Override
    public void run() {
        // We follow the following rules when it comes to power fail
        // 1. if the transaction has displayed/printing approved/started printing approved handle approved
        // 2. if approved but has sig (and not verified). Reverse
        // 3. otherwise finalise.

        boolean requireNotification = false;
        // Power fail txn
        Timber.i( "----------------Power fail txn------------------");

        // Get the last transaction
        TransRec last = TransRec.getLatestFinancialTxn();

        if(last == null) {
            Timber.i( "Previous Transaction is Null");
            return;
        }
        // Debug our the last transaction details
        last.debug();

        Timber.i( "Message Status: %s", last.getProtocol().getMessageStatus().displayName );
        // check if need to reverse the transaction
        switch(last.getProtocol().getMessageStatus()) {
            // Any power fail on a started and saved transaction that has failed before the comms states (eg sending etc)
            // Just update the response code for debugging purposes to state this transaction failed due to power fail
            case NOT_SET:
                Timber.i("Transaction not set");
                setPowerFail(last, false);
                last.updateMessageStatus(FINALISED);
                requireNotification = true;
                break;
                // Power failed during comms messaging, always reverse and set to power fail
            case AUTH_SENT:
                Timber.i("Auth sent reversal required");
                setPowerFail(last, true);
                requireNotification = true;
                break;
                // Waiting for finish is the area from receiving a valid message from the host to pos printing
                // Just need to check the edge case here of signature, all others we can ignore and take the literal value
            case WAITING_FOR_FINISH:
                // Power fail any signature transactions that were approved
                if(last.isApproved() && last.isSignatureRequired() && !last.getAudit().isSignatureChecked()) {
                    Timber.i("Failed signature approval with no signature checked");
                    setPowerFail(last, true);
                } else {
                    Timber.i("No changes required");
                    last.updateMessageStatus(FINALISED);
                }
                requireNotification = true;
                break;
            // All other states we don't care about as these will be dealing with the message itself etc.
            case REVERSAL_QUEUED:
            case ADVICE_QUEUED:
            case REC_QUEUED:
            case FINALISED:
            case FINALISED_AND_REVERSED:
            case POLL_QUEUED:
            case DEFERRED_AUTH:
            default:
                Timber.i("Ignoring Message %s", last.getProtocol().getMessageStatus().displayName);
                break;
        }

        // Edge case here. If we are not an auto/integrated transaction we still need to send the
        // record back for recording purposes such as go insight.
        if(requireNotification && !last.getTransType().autoTransaction) {
            ECRHelpers.ipcSendTransResponse( d, last, context );
        }

        // update our trans record on the terminal.
        last.save();
    }
}
