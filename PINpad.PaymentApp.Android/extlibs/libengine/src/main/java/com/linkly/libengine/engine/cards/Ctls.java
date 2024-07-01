package com.linkly.libengine.engine.cards;

import static com.linkly.libengine.engine.transactions.properties.TProtocol.AuthMethod.OFFLINE_POSTCOMMS_AUTHORISED;
import static com.linkly.libengine.helpers.UIHelpers.uiShowTryContact;
import static com.linkly.libsecapp.IP2PEMV.P2P_EMV_CID_RESULT.P2P_EMV_CID_TC;
import static com.linkly.libsecapp.IP2PEMV.P2P_EMV_HOST_DECISION.P2P_EMV_HOST_FAILED_TO_CONNECT;
import static com.linkly.libsecapp.emv.Tag.aid;
import static com.linkly.libsecapp.emv.Tag.visa_oda_result;

import com.linkly.libengine.action.InitialProcessing;
import com.linkly.libengine.action.TransactionDecliner;
import com.linkly.libengine.dependencies.IDependency;
import com.linkly.libengine.engine.EngineManager;
import com.linkly.libengine.engine.transactions.TransRec;
import com.linkly.libengine.engine.transactions.properties.TCard;
import com.linkly.libengine.engine.transactions.properties.TProtocol;
import com.linkly.libengine.overrides.CoreOverrides;
import com.linkly.libengine.status.IStatus;
import com.linkly.libmal.global.util.Util;
import com.linkly.libsecapp.IP2PCtls;
import com.linkly.libsecapp.IP2PEMV;
import com.linkly.libsecapp.P2PLib;
import com.linkly.libui.IUIDisplay;
import com.linkly.libui.UIScreenDef;

import java.util.Arrays;

import timber.log.Timber;

public class Ctls {
    private static final Ctls ourInstance = new Ctls();
    private CtlsCallbacks listener = null;
    private IUIDisplay ui;
    private IDependency d;

    private Ctls() {
    }

    public static Ctls getInstance() {
        return ourInstance;
    }

    private boolean mcrAllowed(IDependency d, TransRec trans) {
        if( !d.getPayCfg().isMcrEnabled() ) {
            Timber.w( "Not allowing merchant choice routing (MCR) because disabled in config" );
            return false;
        }

        if( trans.isPreAuth() ||
            trans.isCompletion() ) {
            Timber.w( "Not allowing merchant choice routing (MCR) because trans type (%s) doesn't allow it", trans.getTransType().name() );
            return false;
        } else {
            return true;
        }
    }

    public void start(IDependency d) {
        this.d = d;
        IP2PCtls ctls = d.getP2PLib().getIP2PCtls();
        TransRec trans = d.getCurrentTransaction();
        ui = d.getUI();

        d.getStatusReporter().reportStatusEvent( IStatus.STATUS_EVENT.STATUS_TRANS_CTLS , trans.isSuppressPosDialog());

        if (listener == null) {
            listener = new CtlsCallbacks(d);
        }

        ctls.ctlsInit(listener);

        if (d.getPayCfg().isContactlessSupported()) {

            byte[] aucTransDate = Util.hexStringToByteArray(trans.getAudit().getTransDateTimeAsString("yyMMdd"));
            byte[] aucTransTime = Util.hexStringToByteArray(trans.getAudit().getTransDateTimeAsString("hhmmss"));
            long maxCtlsAmount = ctls.ctlsGetMaxAmount(trans.isStartedInOfflineMode());

            byte ucTransType = 0;
            if (trans.isSale()) {
                ucTransType = 0x00;
            } else if (trans.isCash()) {
                ucTransType = 0x01;
            } else if (trans.isCashback()) {
                ucTransType = 0x09;
            } else if( trans.isRefund()) {
                ucTransType = 0x20;
            } else {
                // unexpected to get here. assume 0x00 (sale)
                Timber.i( "WARNING - UNHANDLED TRANS TYPE, setting CTLS tag 9c to 0x00" );
            }

            if (CoreOverrides.get().isAutoFillTrans())
                ctls.ctlsDisableRemoveCard(true);

            byte[] countryCode = Util.str2Bcd(trans.getAudit().getCountryCode());
            byte[] transCurrCode = Util.str2Bcd(trans.getAmounts().getCurrency());

            IP2PCtls.P2P_CTLS_ERROR eRet = ctls.ctlsGenAC1(trans.getAmounts().getTotalAmount(), trans.getAmounts().getCashbackAmount(),
                    countryCode, transCurrCode, ucTransType, aucTransDate, aucTransTime, maxCtlsAmount, trans.getCard().isResetCvmLimit(),
                    mcrAllowed(d, trans), d.getPayCfg().getMcrLimit(), d.getPayCfg().getMcrUpperLimit(), trans.isStartedInOfflineMode()
            );
            trans.getCard().setCtlsResultCode(eRet);
        }
    }

    public void genAc2(IDependency d) {

        TransRec trans = d.getCurrentTransaction();
        TCard cardInfo = trans.getCard();
        IP2PCtls ctls = d.getP2PLib().getIP2PCtls();
        cardInfo.getLedStatus().reset();

        if (!cardInfo.isGenAc2Required()) {
            Timber.e("CTLS 2nd Gen AC required = false");
            return;
        }

        TProtocol.HostResult hostRes = trans.getProtocol().getHostResult();

        switch (hostRes) {
            case DECLINED:
                if ((trans.getTransType() != EngineManager.TransType.REFUND) &&
                    (trans.getCard().isCtlsToICCFallbackTxn())) {
                    uiShowTryContact(d,trans);
                }
                break;

            case CONNECT_FAILED:
            case NO_RESPONSE:
                commsFailureApprovalLogic(trans, ctls);
                break;

            default:
                Timber.e("CTLS GenAc2 unexpected hostRes value: %s", hostRes.name());
                break;
        }
    }

    /**
     * called from 2nd Gen AC logic, when couldn't authorise online due to comms error
     * decides if offline approval should be attempted, sets appropriate transaction flags
     *
     * @param trans input/output transaction object
     */
    private void commsFailureApprovalLogic(TransRec trans, IP2PCtls ctls) {
        boolean approveTxn = false;

        if( trans.isRefund() ) {
            // refunds don't require a 2nd Gen AC. we don't want to look at 2nd Gen AC result because we set up the kernel to return an AAC decline cryptogram
            approveTxn = true;
        } else if( ctls.ctlsGenAC2(P2P_EMV_HOST_FAILED_TO_CONNECT) ) {
            // do Ctls 2nd GenAC (essentially 2nd TAA for schemes supporting TVR/TAC default).
            // check CID to check result (approve/decline)
            IP2PEMV.P2P_EMV_CID_RESULT eCidResult = ctls.ctlsGetCidResult();
            if( eCidResult == P2P_EMV_CID_TC ) {
                approveTxn = true;
            } else {
                // treat as declined
                Timber.e("Ctls 2nd Gen AC/TAA returned decline");
            }
        } else {
            // else error. this is returned if the card scheme doesn't support TVR/TAC defaults. Do some application level checks here
            Timber.w("Ctls 2nd Gen AC/TAA check failed, scheme doesn't support TVR/TAC. Performing basic checks");
            approveTxn = !isCardExpired(trans);
        }

        if( approveTxn ) {
            // attempt offline approval. CheckResult will make final decision if it's allowed or not
            trans.getProtocol().setCanAuthOffline(true);
            trans.getProtocol().setAuthMethod(OFFLINE_POSTCOMMS_AUTHORISED);
        } else {
            // similar to contact GenAC2 failure, treat as card decline
            trans.getCard().setCardDeclined(true);
            d.getWorkflowEngine().setNextAction(TransactionDecliner.class);
        }
    }

    /**
     * checks if card is expired. displays an error to the user if it is, and sets appropriate response codes/text etc
     *
     * @param trans input trans rec
     * @return true if expired/false if not
     */
    private boolean isCardExpired(TransRec trans) {
        // expiry is in YYMM format, so we can do an alphabetial/lexicographical comparison with current date also in YYMM format
        String currentDate = trans.getAudit().getTransDateTimeAsString("yyMM");
        String cardExpiry = trans.getCard().getExpiry();

        if( currentDate == null || cardExpiry == null ) {
            // error, err on the cautious side, treat as expired
            Timber.e( "error, currentDate is null (%b) or card expiry is null (%b)", currentDate==null, cardExpiry==null );
            return true;
        }

        // returns true if expiry is bigger than current date
        boolean expired = currentDate.compareTo(cardExpiry)>0;
        if( expired ) {
            ui.showScreen(UIScreenDef.CARD_EXPIRED);
        }

        return expired;
    }

    public void ctlsRetryCardEntry(IDependency d, TransRec trans) {

        Util.disableCancel(false, true);
        TCard newCard = new TCard(trans.getTransType(), d.getPayCfg());
        newCard.setCvmType(trans.getCard().getCvmType());
        trans.setCard(newCard);
        trans.getCard().getLedStatus().reset();
        trans.getCard().getLedStatus().setCTLSEnabled(true);
        trans.getCard().setCardIndex(-1);
        trans.setFinalised(false);
        d.getWorkflowEngine().setNextAction(InitialProcessing.class);

    }
}
