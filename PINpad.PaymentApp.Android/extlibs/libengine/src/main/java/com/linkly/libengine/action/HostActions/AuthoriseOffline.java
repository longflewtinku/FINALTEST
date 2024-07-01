package com.linkly.libengine.action.HostActions;

import static com.linkly.libengine.engine.protocol.IProto.RejectReasonType.COMMS_ERROR;
import static com.linkly.libengine.engine.transactions.OfflineLimitsCheck.wouldTransactionExceedOfflineLimits;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.AuthMethod.NOT_SET;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.HostResult.CONNECT_FAILED;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.HostResult.NO_RESPONSE;

import com.linkly.libengine.action.IAction;
import com.linkly.libengine.engine.protocol.IProto;
import com.linkly.libengine.engine.transactions.properties.TProtocol;

import timber.log.Timber;

// inputs:
// - looks at protocol.canAuthOffline flag
// - also checks if below offline limits
// outputs:
// - will call protocol layer to store advice if offline approved
// - sets protocol.AuthMethod to appropriate value if approved, or NOT_SET if declined

@SuppressWarnings("java:S3776")
public class AuthoriseOffline extends IAction {
    @Override
    public String getName() {
        return "AuthoriseOffline";
    }

    @Override
    public void run() {
        TProtocol protocol = trans.getProtocol();

        if (trans.isEfbAuthorisedTransaction()) {
            Timber.e("Approved by EFB");
            d.getProtocol().authorizeOffline(trans, protocol.getAuthMethod());
        } else if(trans.isCompletion()) {
            // Transaction is "Completion/Completion Auto", skip the offline limit check
            Timber.e("Skip Offline limit Check for Completions");
            d.getProtocol().authorizeOffline(trans, protocol.getAuthMethod());
        } else {
            // check auth offline is set. if not, skip this state
            if (!protocol.isCanAuthOffline()) {
                Timber.e("Offline auth not required, skipping state");
                return;
            }

            // check if it's cash or pwcb txn, reject if it is. Never allowed in offline
            if (trans.isCash() || trans.isCashback()) {
                // note it's really hard to get here for emv/ctls because tac-default settings will usually decline in 2nd Gen AC due to over floor limit, online pin entered etc
                // and if started in airplane mode/offline mode, we reject cash txns immediately before asking for card to be presented
                Timber.e("AuthoriseOffline - not allowing offline auth because cash or cashback txn");
                // unset authMethod to not set
                protocol.setAuthMethod(NOT_SET);
                // display comms error type message to user/POS. Reasoning for this error is that cash txns should always attempt online auth,
                // so the reason for failure is not that we don't accept offline cashback, it's because the comms failed
                d.getProtocol().setInternalRejectReason(trans, IProto.RejectReasonType.COMMS_ERROR);
                trans.getAudit().setRejectReasonType(IProto.RejectReasonType.COMMS_ERROR);
                return;
            }

            // check offline limits haven't been exceeded
            IProto.RejectReasonType offlineLimitsRejectReason = wouldTransactionExceedOfflineLimits(d, trans);
            if (offlineLimitsRejectReason == IProto.RejectReasonType.NOT_SET) {
                // approve
                Timber.e("Authorised offline");
                d.getProtocol().authorizeOffline(trans, trans.getProtocol().getAuthMethod());
            } else {
                // decline. CheckResult will catch this as a comms error decline scenario due to TProtocol.HostResult
                Timber.e("Declined offline auth attempt. Offline rules say decline");
                // don't modify host result
                // unset authMethod to not set
                protocol.setAuthMethod(NOT_SET);

                // if comms were attempted,
                if (protocol.getHostResult() == CONNECT_FAILED || protocol.getHostResult() == NO_RESPONSE) {
                    // protocol layer has already set response code/text to indicate comms failure as the primary reason for decline
                    // note: d.getProtocol().setInternalRejectReason not required because it calls protocol layer to set repsonse codes/text etc. This has already been done
                    Timber.i("AuthoriseOffline: decline because limits don't allow it, set reject reason to COMMS_ERROR");
                    trans.getAudit().setRejectReasonType(COMMS_ERROR);
                } else {
                    // set response code/text to appropriate values for decline reason. currently the only reason is offline limits exceeded
                    d.getProtocol().setInternalRejectReason(trans, offlineLimitsRejectReason);
                    trans.getAudit().setRejectReasonType(offlineLimitsRejectReason);
                }
            }
        }
    }
}
