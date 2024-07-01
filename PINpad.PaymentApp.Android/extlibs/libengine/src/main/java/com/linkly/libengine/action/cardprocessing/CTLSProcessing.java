package com.linkly.libengine.action.cardprocessing;

import static com.linkly.libengine.engine.protocol.IProto.RejectReasonType.DECLINED_BY_CARD_PRE_COMMS;
import static com.linkly.libengine.engine.transactions.properties.TCard.CaptureMethod.CTLS_MSR;
import static com.linkly.libengine.engine.transactions.properties.TCard.CvmType.CDCVM;
import static com.linkly.libengine.engine.transactions.properties.TCard.CvmType.ENCIPHERED_ONLINE_PIN;
import static com.linkly.libengine.engine.transactions.properties.TCard.CvmType.NO_CVM;
import static com.linkly.libengine.engine.transactions.properties.TCard.CvmType.SIG;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.ACC_TYPE_CHEQUE;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.ACC_TYPE_CREDIT;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.ACC_TYPE_SAVINGS;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.AuthMethod.OFFLINE_PRECOMMS_AUTHORISED;
import static com.linkly.libengine.status.IStatus.STATUS_EVENT.STATUS_TRANS_CTLS_DECLINED;
import static com.linkly.libsecapp.IP2PCtls.P2P_CTLS_ERROR.P2P_CTLS_CARD_GENERATED_AAC;
import static com.linkly.libsecapp.IP2PCtls.P2P_CTLS_ERROR.P2P_CTLS_CARD_REPORTED_ERROR;
import static com.linkly.libsecapp.IP2PCtls.P2P_CTLS_ERROR.P2P_CTLS_CARD_UNSUPPORTED;
import static com.linkly.libsecapp.IP2PCtls.P2P_CTLS_ERROR.P2P_CTLS_DATA_ERR;
import static com.linkly.libsecapp.IP2PCtls.P2P_CTLS_ERROR.P2P_CTLS_FALLBACK_TO_ICC;
import static com.linkly.libsecapp.IP2PCtls.P2P_CTLS_ERROR.P2P_CTLS_NO_APP;
import static com.linkly.libsecapp.IP2PCtls.P2P_CTLS_ERROR.P2P_CTLS_OK;
import static com.linkly.libsecapp.IP2PCtls.P2P_CTLS_ERROR.P2P_CTLS_SEE_PHONE;
import static com.linkly.libsecapp.IP2PCtls.P2P_CTLS_ERROR.P2P_CTLS_TRANS_RETRY;
import static com.linkly.libsecapp.IP2PCtls.P2P_CTLS_ERROR.P2P_CTLS_TRANS_RETRY_SILENT;
import static com.linkly.libsecapp.IP2PCtls.P2P_CTLS_ERROR.P2P_CTLS_TRY_ANOTHER_CARD;
import static com.linkly.libsecapp.IP2PEMV.P2P_CTLS_CVM.P2P_CTLS_CVM_CDCVM;
import static com.linkly.libsecapp.IP2PEMV.P2P_CTLS_CVM.P2P_CTLS_CVM_ONLINE_PIN;
import static com.linkly.libsecapp.IP2PEMV.P2P_CTLS_CVM.P2P_CTLS_CVM_SIGNATURE;
import static com.linkly.libsecapp.IP2PEncrypt.ElementType.SERVICE_CODE;
import static com.linkly.libsecapp.IP2PEncrypt.ElementType.TRACK_2_FULL_CHIP;
import static com.linkly.libsecapp.emv.Tag.aid;
import static com.linkly.libsecapp.emv.Tag.amt_other_num;
import static com.linkly.libsecapp.emv.Tag.appl_cryptogram;
import static com.linkly.libsecapp.emv.Tag.appl_id;
import static com.linkly.libsecapp.emv.Tag.appl_intchg_profile;
import static com.linkly.libsecapp.emv.Tag.appl_label;
import static com.linkly.libsecapp.emv.Tag.appl_pan_seqnum;
import static com.linkly.libsecapp.emv.Tag.appl_pre_name;
import static com.linkly.libsecapp.emv.Tag.atc;
import static com.linkly.libsecapp.emv.Tag.card_transaction_qualifiers;
import static com.linkly.libsecapp.emv.Tag.crypt_info_data;
import static com.linkly.libsecapp.emv.Tag.cvm_results;
import static com.linkly.libsecapp.emv.Tag.effect_date;
import static com.linkly.libsecapp.emv.Tag.eftpos_payment_account_reference;
import static com.linkly.libsecapp.emv.Tag.eftpos_token_requestor_id;
import static com.linkly.libsecapp.emv.Tag.emv_discretionary_data;
import static com.linkly.libsecapp.emv.Tag.expiry_date;
import static com.linkly.libsecapp.emv.Tag.ifd_ser_num;
import static com.linkly.libsecapp.emv.Tag.isssuer_code_tbl;
import static com.linkly.libsecapp.emv.Tag.issuer_app_data;
import static com.linkly.libsecapp.emv.Tag.mer_id;
import static com.linkly.libsecapp.emv.Tag.outcome_param_set;
import static com.linkly.libsecapp.emv.Tag.pos_entry_mode;
import static com.linkly.libsecapp.emv.Tag.service_code;
import static com.linkly.libsecapp.emv.Tag.temr_id;
import static com.linkly.libsecapp.emv.Tag.term_type;
import static com.linkly.libsecapp.emv.Tag.third_party_data;
import static com.linkly.libsecapp.emv.Tag.track2_eq_data;
import static com.linkly.libsecapp.emv.Tag.track_2_equiv_data;
import static com.linkly.libsecapp.emv.Tag.tran_date;
import static com.linkly.libsecapp.emv.Tag.trans_curcy_code;
import static com.linkly.libsecapp.emv.Tag.trans_seq_counter;
import static com.linkly.libsecapp.emv.Tag.unpred_num;
import static com.linkly.libsecapp.emv.Tag.visa_oda_result;
import static com.linkly.libsecapp.emv.Tag.visa_ttq;
import static com.linkly.libui.UIScreenDef.CTLS_NOT_ENABLED;

import com.linkly.libengine.action.IAction;
import com.linkly.libengine.action.InitialProcessing;
import com.linkly.libengine.action.TransactionDecliner;
import com.linkly.libengine.action.TransactionReferral;
import com.linkly.libengine.action.check.CheckBINRange;
import com.linkly.libengine.dependencies.IDependency;
import com.linkly.libengine.engine.cards.Emv;
import com.linkly.libengine.engine.transactions.TransRec;
import com.linkly.libengine.engine.transactions.properties.TCard;
import com.linkly.libengine.engine.transactions.properties.TProtocol;
import com.linkly.libengine.env.Stan;
import com.linkly.libengine.helpers.UIHelpers;
import com.linkly.libmal.MalFactory;
import com.linkly.libmal.global.util.Util;
import com.linkly.libpositive.wrappers.TagDataToPOS;
import com.linkly.libsecapp.CtlsCfg;
import com.linkly.libsecapp.IP2PCtls;
import com.linkly.libsecapp.IP2PEMV;
import com.linkly.libsecapp.IP2PEncrypt;
import com.linkly.libsecapp.emv.EmvTags;
import com.linkly.libsecapp.emv.Tag;

import java.util.ArrayList;
import java.util.Arrays;

import timber.log.Timber;

public class CTLSProcessing extends IAction {
    IP2PCtls iCtls;
    IP2PEncrypt iP2PE;

    @Override
    public String getName() {
        return "CTLSProcessing";
    }

    @Override
    public void run() {
        iCtls = d.getP2PLib().getIP2PCtls();
        iP2PE = d.getP2PLib().getIP2PEncrypt();
        if (trans != null &&
            trans.getCard() != null &&
            trans.getCard().getCaptureMethod() == TCard.CaptureMethod.CTLS) {
            Timber.e("Start CTLS 1st Gen AC Check");
            genAc1(d);
            Timber.e("End CTLS 1st Gen AC Check");
        }
    }

    public void genAc1(IDependency d) {


        //P2P_CTLS_MSR_CARD

        ctlsGetEMVTags(d, trans);

        trans.saveCtlsTagsString();

        ctlsGetCvmType(trans);

        if (trans.getCard().getCtlsResultCode() == P2P_CTLS_OK) {

            /* check we have a valid card reading */
            if (!CheckBINRange.runBinRangeChecking(d, false)) {
                ctlsRetryCardEntry(); /* if we failed to get a good card read we can retry with ctls */
                return;
            }

            /* if the cryptogram checking has come to a conclusion then return here */
            if (ctlsCheckCryptogram(d)) {
                return;
            }

            // for selected AID (if any), check cfg_emv.json for matching defaultAccount setting and if found, select that account for this transaction (skips user account select state)
            ctlsGetDefaultAccount(d,trans);
        }

        checkCtlsResult(d);
    }

    private CtlsCfg.CtlsAid getCtlsAidCfg( IDependency d, TransRec trans ) {
        if( d.getConfig() != null ) {
            CtlsCfg emvCfg = d.getConfig().getCtlsCfg();

            if (emvCfg == null || emvCfg.getAids() == null)
                return null;

            // find match for selected AID
            for (CtlsCfg.CtlsAid aid : emvCfg.getAids()) {
                // perform partial match, i.e. aid on card can be longer, but first part must match
                if (trans.getCard().getAid().startsWith(aid.getAid())) {
                    return aid;
                }
            }
        }
        return null;
    }

    private void ctlsGetDefaultAccount(IDependency d, TransRec trans) {
        CtlsCfg.CtlsAid aidCfg = getCtlsAidCfg( d, trans );

        if( null != aidCfg ) {
            // found matching AID in config. does it have a default account?
            if (aidCfg.getDefaultAccount() != null) {
                // yes, select this account
                switch (aidCfg.getDefaultAccount()) {
                    case "CHQ":
                        trans.getProtocol().setAccountType(ACC_TYPE_CHEQUE);
                        break;
                    case "SAV":
                        trans.getProtocol().setAccountType(ACC_TYPE_SAVINGS);
                        break;
                    case "CRD":
                    default:
                        trans.getProtocol().setAccountType(ACC_TYPE_CREDIT);
                        break;
                }
            } else {
                // matched AID, but no default account configured. default to credit
                trans.getProtocol().setAccountType(ACC_TYPE_CREDIT);
            }
        }
        // finished, return
    }

    public boolean checkCtlsResult(IDependency d) {


        if (!trans.getCard().isCtlsCaptured()) {
            return false;
        }

        if (iCtls.ctlsIsMagStripeCard()) {
            trans.getCard().setCaptureMethod(CTLS_MSR);
        }

        if (!ctlsCheckAllowed(trans)) {
            ui.showScreen(CTLS_NOT_ENABLED);

            trans.getCard().setCtlsResultCode(P2P_CTLS_TRANS_RETRY);
            d.getStatusReporter().reportStatusEvent(STATUS_TRANS_CTLS_DECLINED , trans.isSuppressPosDialog());
        }

        // sets relevant processing flags for card retry and displays msg to user
        checkCtlsFallback(trans);

        IP2PCtls.P2P_CTLS_ERROR eRet = trans.getCard().getCtlsResultCode();

        if (eRet == P2P_CTLS_CARD_UNSUPPORTED ||
                eRet == P2P_CTLS_CARD_REPORTED_ERROR ||
                eRet == P2P_CTLS_TRANS_RETRY ||
                eRet == P2P_CTLS_TRANS_RETRY_SILENT ||
                eRet == P2P_CTLS_FALLBACK_TO_ICC ||
                eRet == P2P_CTLS_SEE_PHONE) {
            Timber.e( "CTLS result code %s returned, retrying card entry", eRet.toString() );
            ctlsRetryCardEntry();
        } else if (eRet == P2P_CTLS_CARD_GENERATED_AAC && trans.isRefund()) {
            Timber.i( "CTLS REFUND transaction to continue");
            return true;
        } else if ( eRet == P2P_CTLS_NO_APP ) { /* display the error and try for a tap again */
            Timber.e( "CTLS result code %s returned, retrying card entry", eRet.toString() );
            UIHelpers.uiDisplayCtlsError(d,trans);
            ctlsRetryCardEntry();
        } else if (eRet != P2P_CTLS_OK) {
            Timber.e( "CTLS result code %s returned, DECLINING", eRet.toString() );
            String errorStr = UIHelpers.uiDisplayCtlsError(d,trans);
            d.getProtocol().setInternalRejectReason( trans, DECLINED_BY_CARD_PRE_COMMS, errorStr );
            d.getWorkflowEngine().setNextAction(TransactionDecliner.class);
            d.getStatusReporter().reportStatusEvent(STATUS_TRANS_CTLS_DECLINED , trans.isSuppressPosDialog());
        } else if( checkCtlsRejectTokenDevice(trans) ) {
            // final check - check if we want to reject digital wallets/tokenized transaction in particular conditions
            // yes reject, set flags to reject, ask for new card
            UIHelpers.uiShowWalletsNotAllowedTryAnother(d,trans);
            trans.getCard().setCvmType(NO_CVM); // clear CVM type flag or it can display 'see consumer device' on card entry screen
            ctlsRetryCardEntry();
        } else {
            // card allowed
            Timber.i( "CTLS transaction to continue, no problems");
            return true;
        }
        return false;
    }

    /**
     * checks if it's a wallet/phone/token device presented, and rejects this form factor, depending on settings
     *
     * @param trans - trans rec input
     * @return true = device rejected, false = device allowed
     */
    private boolean checkCtlsRejectTokenDevice(TransRec trans){
        CtlsCfg.CtlsAid aidCfg = getCtlsAidCfg( d, trans );
        boolean parPresent = !Util.isNullOrEmpty(iCtls.ctlsGetTag(eftpos_payment_account_reference));
        boolean tokenRequesterIdPresent = !Util.isNullOrEmpty(iCtls.ctlsGetTag(eftpos_token_requestor_id));

        // rejection logic
        if( aidCfg != null && aidCfg.isRejectWallets() && // reject wallets is set for this AID/scheme and
                (parPresent || tokenRequesterIdPresent) // at least one of the token data elements is present
        ){
            // then reject card/device
            Timber.e("rejecting card due to token tag being present. par tag present = %b, tokenRequestorId present = %b", parPresent, tokenRequesterIdPresent);
            return true;
        }
        return false;
    }

    public boolean checkCtlsFallback(TransRec trans) {

        IP2PCtls.P2P_CTLS_ERROR eRet = trans.getCard().getCtlsResultCode();

        if (eRet == P2P_CTLS_FALLBACK_TO_ICC || eRet == P2P_CTLS_NO_APP) {
            trans.getCard().setCtlsToICCFallbackTxn(true);
        }

        if (eRet == P2P_CTLS_CARD_UNSUPPORTED ||
                eRet == P2P_CTLS_CARD_REPORTED_ERROR) {
            if (trans.getCard().isPaypassTransaction()) {
                trans.getCard().setCtlsToICCFallbackTxn(true);
            }
        } else if (eRet == P2P_CTLS_TRANS_RETRY_SILENT) {
            Timber.i( "Silent Retry");
        } else if (isVisaFallback(trans)) {
            trans.getCard().setCtlsToICCFallbackTxn(true);
            trans.getCard().setCtlsResultCode(P2P_CTLS_TRANS_RETRY);
        } else if (isNullCard(trans, eRet)) {
            trans.getCard().setCtlsResultCode(P2P_CTLS_TRANS_RETRY);
            trans.getCard().setCtlsTryAnother(true);
        }


        /* fallback transaction so suggest trying contact */
        if (trans.getCard().isCtlsToICCFallbackTxn()) {
            UIHelpers.uiShowTryContact(d,trans);
        }
        else if (trans.getCard().isCtlsTryAnother()) {
            UIHelpers.uiShowTryAnotherCard(d,trans);
            trans.getCard().setCtlsTryAnother(false);
        }

        return true;
    }

    public boolean isNullCard(TransRec trans, IP2PCtls.P2P_CTLS_ERROR eRet) {

        if (eRet == P2P_CTLS_SEE_PHONE)
            return false;
        /* Visa T5 22 .... */
        if (    eRet == P2P_CTLS_DATA_ERR ||
                eRet == P2P_CTLS_TRY_ANOTHER_CARD) {
            return true;
        }

        if (!iCtls.ctlsIsMagStripeCard()) {

            String aid = trans.getCard().getAid();
            if (aid == null || aid.isEmpty())
                return true;

            byte[] aip = trans.getCard().getAip();
            if (aip == null || aip.length <= 0)
                return true;
        }
        return false;
    }

    public boolean ctlsCheckAllowed(TransRec trans) {

        if (!trans.getCard().isPaypassTransaction()) {
            return true;
        }

        if ((trans.getCard().getAip()[1] & 0x80) > 0) {
            Timber.i( "CTLS is allowed on this paypass card");
            return true;
        }

        Timber.i( "CTLS NOT allowed on this paypass card");
        return false;

    }

    public boolean ctlsCheckCryptogram(IDependency d) {

        if (iCtls.ctlsIsMagStripeCard()) {
            return false;
        }

        // NOTE: some kernels (e.g. JCB) don't return 9f17 (CID) so card.CryptogramType won't be set. However, they should all have CID result set
        if (trans.getCard().getCryptogramType() == null && iCtls.ctlsGetCidResult() == null) {
            d.getProtocol().setInternalRejectReason( trans, DECLINED_BY_CARD_PRE_COMMS);
            trans.getCard().setCardDeclined(true);
            d.getWorkflowEngine().setNextAction(TransactionDecliner.class);
            return true;
        }

        IP2PEMV.P2P_EMV_CID_RESULT cidResult = iCtls.ctlsGetCidResult();

        Timber.i( "GENAC1 Result " + cidResult.name() + " " + cidResult.toString());

        switch(cidResult)
        {
            case P2P_EMV_CID_AAC: // 0x00
                Timber.i( "1st Gen AC AAC returned");

                if ( trans.isRefund()) {
                    Timber.i( "CTLS AAC returned for refund, attempting online");
                    break;
                }
                else if (isVisaFallback(trans)) {
                    Timber.i( "CTLS AAC returned visa, fallback to icc");
                    // visa - bespoke logic determines if fallback or not
                    trans.getCard().setCtlsToICCFallbackTxn(true);
                    trans.getCard().setCtlsResultCode(P2P_CTLS_TRANS_RETRY);
                } else if (trans.getCard().isMasterCard() || trans.getCard().isMaestroCard()) {
                    Timber.i( "CTLS AAC returned for mastercard/maestro, fallback to icc");
                    // always fallback for paypass - mastercard/maestro
                    trans.getCard().setCtlsToICCFallbackTxn(true);
                    trans.getCard().setCtlsResultCode(P2P_CTLS_TRANS_RETRY);
                } else {
                    Timber.i( "CTLS AAC other decline");
                    if (d.getProtocol().requiresDeclinedAdvices()) {
                        d.getProtocol().preAuthorize(trans); /* this is done in case the protocol needs to update the transaction with values that are needed for the declined advice to work */
                        trans.updateMessageStatus(TProtocol.MessageStatus.ADVICE_QUEUED, false);
                        trans.save();
                    }
                    d.getProtocol().setInternalRejectReason( trans, DECLINED_BY_CARD_PRE_COMMS );
                    trans.getCard().setCardDeclined(true);
                    d.getWorkflowEngine().setNextAction(TransactionDecliner.class);
                    return true;

                }
                break;

            case P2P_EMV_CID_TC: // 0x40:
                Timber.i( "1st Gen AC TC returned - APPROVED");
                trans.getProtocol().setCanAuthOffline(true);
                trans.getProtocol().setAuthMethod(OFFLINE_PRECOMMS_AUTHORISED);
                trans.getCard().setCardDeclined(false);
                break;

            case P2P_EMV_CID_AAR: //0xC0:
                d.getProtocol().preAuthorize(trans); /* this is done in case the protocol needs to update the transaction with values that are needed for the declined advice to work */
                d.getWorkflowEngine().setNextAction(TransactionReferral.class);
                Timber.i( "1st Gen AC returned Referred");
                trans.getCard().setCardDeclined(false);
                break;

            case P2P_EMV_CID_ARQC://0x80:
                Emv.SetEmvReasonOnlineCode(trans);
                trans.getCard().setCardDeclined(false);
                Timber.i( "1st Gen AC returned ARQC - ONLINE REQUIRED");
                break;

            default:
                Timber.i("1st Gen AC returned unexpected result %s, assuming decline", cidResult.name());
                trans.getCard().setCardDeclined(true);
                break;
        }

        return false;
    }

    public boolean isVisaFallback(TransRec trans) {

        if (!ctlsIsVisaCard()) {
            return false;
        }

        byte[] ctq = trans.getCard().getCtq();
        byte[] oda = iCtls.ctlsGetTag(visa_oda_result);

        boolean odaFailed = false;
        if (oda != null && (oda.length > 0) && (oda[0] == 0x01))
            odaFailed = true;

        // byte 1 bit 5 means "switch interface if ODA fails
        return (ctq != null && (ctq[0] & 0x10) > 0 && odaFailed);
    }

    public boolean ctlsIsVisaCard() {
        return isVisaAid(iCtls.ctlsGetTag(aid));
    }

    public static boolean isVisaAid(byte[] aid) {
        return compareAid(aid, visaRid);
    }

    public static boolean isVisaAid(String aid) {
        return compareAid(Util.hexStringToByteArray(aid), visaRid);
    }

    protected static final byte[] visaRid = new byte[] {(byte) 0xA0, 0x00, 0x00, 0x00, 0x03};

    protected static boolean compareAid(byte[] aid, byte[] compareTo) {
        if (aid == null || compareTo == null) {
            return false;
        }

        byte[] rid = Arrays.copyOf(aid, compareTo.length);

        return (Arrays.equals(rid, compareTo));
    }

    private void ctlsRetryCardEntry() {
        // save boolean fallback flags for setting on new card object
        boolean isCtlsAllowed = trans.getCard().isCtlsAllowed();
        boolean isCtlsTryAnother = trans.getCard().isCtlsTryAnother();

        Util.disableCancel(false, true);
        TCard newCard = new TCard(trans.getTransType(), d.getPayCfg());
        newCard.setCvmType(trans.getCard().getCvmType());
        trans.setCard(newCard);
        trans.getCard().getLedStatus().reset();
        trans.getCard().getLedStatus().setCTLSEnabled(true);
        trans.getCard().setCardIndex(-1);
        trans.getCard().setCtlsAllowed(isCtlsAllowed); // specifies if ctls is allowed if going back to card presentation screen
        trans.getCard().setCtlsTryAnother(isCtlsTryAnother);
        d.getWorkflowEngine().setNextAction(InitialProcessing.class);
    }


    @SuppressWarnings("java:S6541") // Code Smell: A "Brain Method" was detected.
    public int ctlsGetMagStripeTags(TransRec trans) {

        byte[] track2 = iCtls.ctlsGetTag(track2_eq_data);
        if(track2!=null) {
            Timber.i( "using tag 57");
        }
        if (track2 == null || track2.length <= 0) {
            track2 = iCtls.ctlsGetTag(track_2_equiv_data);
            if(track2!=null) {
                Timber.i( "using tag 9f6b");
            }
        }

        if (track2 != null && track2.length > 0) {
            // get it again using * masking chars
            String track2Str = iP2PE.getMaskedData(TRACK_2_FULL_CHIP);
            if (track2Str != null && track2Str.length() > 0) {
                trans.getCard().updateTrack2(d, track2Str, trans);
                d.getDebugReporter().reportCardData( TagDataToPOS.CardEntryModeTag.CONTACTLESS, track2Str );
            }
        }

        // Read Application Expiry Date to p2pencrypt secure data store only. Needed for adding it to host message later
        // Will not be saving it's value into TCard record, so return value is ignored
        iCtls.ctlsGetTag(expiry_date);

        String appName = "";
        byte[] appLabel = iCtls.ctlsGetTag(appl_pre_name);
        byte[] appLabel2 = iCtls.ctlsGetTag(appl_label);

        if (appLabel != null && appLabel.length > 0) {
            appName = new String(appLabel);
        }
        if ((appName.length() == 0 || !Util.isAsciiPrintable(appName)) &&
            (appLabel2 != null && appLabel2.length > 0)) {
            appName = new String(appLabel2);
        }

        if (appName.length() > 0 && Util.isAsciiPrintable(appName)) {
            trans.getCard().setCardName(appName.trim());
        }

        // Requires AID for default account selection
        byte[] cardAid = iCtls.ctlsGetTag(aid);
        if (cardAid != null && cardAid.length > 0) {
            trans.getCard().setAid(Util.byteArrayToHexString(cardAid));
        }

        // ATC is required for receipts
        byte[] atcString = iCtls.ctlsGetTag( Tag.atc );
        if( atcString != null && atcString.length > 0 ){
            trans.getCard().setAtc( Util.byteArrayToHexString( atcString ) );
        }

        if (iCtls.ctlsIsMagStripeCard()) {
            if (trans.getCard().getTags() == null) {
                EmvTags emvTags = new EmvTags();
                trans.getCard().setTags(emvTags);
            }

            if( ctlsIsAmexCard()) {
                byte[] unpredNum = iCtls.ctlsGetTag(unpred_num);
                byte[] cryptogram = iCtls.ctlsGetTag(appl_cryptogram);
                byte[] effectiveDate = iCtls.ctlsGetTag(effect_date);
                byte[] thirdPartyData = iCtls.ctlsGetTag(third_party_data);
                byte[] atcNum = iCtls.ctlsGetTag(atc);

                if (unpredNum != null)
                    trans.getCard().getTags().add(unpred_num, unpredNum);
                if (cryptogram != null)
                    trans.getCard().getTags().add(appl_cryptogram, cryptogram);
                if (atcNum != null)
                    trans.getCard().getTags().add(atc, atcNum);
                if (effectiveDate != null)
                    trans.getCard().getTags().add(effect_date, effectiveDate);
                if (thirdPartyData != null)
                    trans.getCard().getTags().add(third_party_data, thirdPartyData);

                // Need to pack date and currency for lower protocol
                String date = trans.getAudit().getTransDateTimeAsString("yyMMdd");
                iCtls.ctlsSetTag(tran_date, Util.str2Bcd(date));
                trans.getCard().getTags().add(tran_date, Util.str2Bcd(date));

                Integer currency = Integer.valueOf(trans.getAmounts().getCurrency());
                iCtls.ctlsSetTag(trans_curcy_code, Util.DecToBCD(currency, 2));
                trans.getCard().getTags().add(trans_curcy_code,Util.DecToBCD(currency, 2));

            }

            if (ctlsIsMasterCard()) {
                byte[] emvDisc = iCtls.ctlsGetTag(emv_discretionary_data);
                if (emvDisc != null)
                    trans.getCard().getTags().add(emv_discretionary_data, emvDisc);

                byte[] thirdPartyData = iCtls.ctlsGetTag(third_party_data);
                if (thirdPartyData != null)
                    trans.getCard().getTags().add(third_party_data, thirdPartyData);
            }

        }
        return 0;
    }

    public boolean ctlsIsMasterCard() {
        return isMastercardAid(iCtls.ctlsGetTag(aid));
    }

    protected static final byte[] mastercardRid = new byte[] {(byte) 0xA0, 0x00, 0x00, 0x00, 0x04};
    protected static final byte[] amexRid = new byte[] {(byte) 0xA0, 0x00, 0x00, 0x00, 0x25};
    protected static final byte[] eftposRid = new byte[] {(byte) 0xA0, 0x00, 0x00, 0x03, (byte)0x84};

    public static boolean isMastercardAid(byte[] aid) {
        return compareAid(aid, mastercardRid);
    }

    public static boolean isMastercardAid(String aid) {
        return compareAid(Util.hexStringToByteArray(aid), mastercardRid);
    }

    public boolean ctlsIsAmexCard() {
        return (isAmexAid(iCtls.ctlsGetTag(aid)));
    }

    public static boolean isAmexAid(byte[] aid) {
        return compareAid(aid, amexRid);
    }

    public static boolean isAmexAid(String aid) {
        return compareAid(Util.hexStringToByteArray(aid), amexRid);
    }

    public static boolean isEftposAid(byte[] aid) {
        return compareAid(aid, eftposRid);
    }

    public static boolean isEftposAid(String aid) {
        return compareAid(Util.hexStringToByteArray(aid), eftposRid);
    }

    public boolean ctlsIsEpalCard() {
        return (isEftposAid(iCtls.ctlsGetTag(aid)));
    }


    private int ctlsGetEMVTags(IDependency d, TransRec trans) {

        ctlsGetMagStripeTags(trans);

        /* if a mag stripe card we don't need any of the other parameters */
        if (iCtls.ctlsIsMagStripeCard()) {
            return 0;
        }

        byte[] aip = iCtls.ctlsGetTag(appl_intchg_profile);
        if (aip != null && aip.length > 0) {
            trans.getCard().setAip(aip);
        }

        byte[] ctq = iCtls.ctlsGetTag(card_transaction_qualifiers);
        if (ctq != null && ctq.length > 0) {
            trans.getCard().setCtq(ctq);
        }


        byte[] validFrom = iCtls.ctlsGetTag(effect_date);
        if (validFrom != null && validFrom.length > 0) {
            trans.getCard().setValidFrom(Util.byteArrayToHexString(validFrom));
        }

        byte[] cardAid = iCtls.ctlsGetTag(aid);
        if (cardAid != null && cardAid.length > 0) {
            trans.getCard().setAid(Util.byteArrayToHexString(cardAid));
        }

        cardAid = iCtls.ctlsGetTag(appl_id);
        if (cardAid != null && cardAid.length > 0) {
            trans.getCard().setAid(Util.byteArrayToHexString(cardAid));
        }

        byte[] tvr = iCtls.ctlsGetTag(Tag.tvr);
        if (tvr != null && tvr.length > 0) {
            trans.getCard().setTvr(Util.byteArrayToHexString(tvr));
        }

        byte[] tsi = iCtls.ctlsGetTag(Tag.tsi);
        if (tsi != null && tsi.length > 0) {
            trans.getCard().setTsi(Util.byteArrayToHexString(tsi));
        }

        byte[] psn = iCtls.ctlsGetTag(appl_pan_seqnum);
        if (psn != null && psn.length > 0) {
            String s = Util.bcd2Str(psn);
            trans.getCard().setPsn(Integer.valueOf(s));
        }

        byte[] cryptogramType = iCtls.ctlsGetTag(crypt_info_data);
        if (cryptogramType != null && cryptogramType.length > 0) {
            trans.getCard().setCryptogramType((int) cryptogramType[0]);
        }

        byte[] cryptogramValue = iCtls.ctlsGetTag(appl_cryptogram);
        if (cryptogramValue != null && cryptogramValue.length > 0) {
            trans.getCard().setCryptogram(cryptogramValue);
        }

        // 20/12/2013 - WC according to new paypass rules, all failed ctls transactions should offer fall forward to icc (insert)
        // the point of this next line is just to check if a paypass specific tag is present. if it is, then assume this is a paypass txn.
        // should be present in all (aac, tc, arqc) txn outcomes
        // getting this flag as fall forward for all declines only applies to paypass cards
        if (trans.getCard().isCardMasterCard(Util.byteArrayToHexString(cardAid)) || trans.getCard().isCardMaestro(Util.byteArrayToHexString(cardAid))) {
            byte[] outcomeParamSet = iCtls.ctlsGetTag(outcome_param_set);
            if (outcomeParamSet != null && outcomeParamSet.length > 0) {
                trans.getCard().setPaypassTransaction(true);
            }
        }

        byte[] iad = iCtls.ctlsGetTag(issuer_app_data);
        if (iad != null && iad.length >= 7) {
            byte[] cvr;
            cvr = Arrays.copyOfRange(iad, 3, 7);
            trans.getCard().setCvr(Util.byteArrayToHexString(cvr));
        }

        setCustomTags(trans);
        ArrayList<Tag> tags = d.getProtocol().getEmvTagList();

        EmvTags emvTags = new EmvTags();
        trans.getCard().setTags(emvTags);

        for (Tag tag : tags) {

            byte[] value = iCtls.ctlsGetTag(tag);
            if (value != null) {
                Timber.i( "EmvTag: %X ,value: %s (%s)", tag.value(), Util.byteArrayToHexString(value), tag.name());
                trans.getCard().getTags().add(tag, value);
            } else {
                Timber.i( "EmvTag: %X ,value: %s (%s)", tag.value(), "NOT AVAILABLE", tag.name());
            }
        }

        boolean mcrPerformed = iCtls.ctlsMcrPerformed();
        trans.getCard().setCtlsMcrPerformed( mcrPerformed );

        return 0;
    }

    private String packCvmr() {

        IP2PEMV.P2P_CTLS_CVM cvm = d.getP2PLib().getIP2PCtls().ctlsGetCvmRequired();
        if (cvm == null) {
            return ("1F0302");
        }

        if (cvm == P2P_CTLS_CVM_SIGNATURE) {
            return ("1E0002");
        } else if (cvm == P2P_CTLS_CVM_ONLINE_PIN) {
            return ("420302");
        } else if (cvm == P2P_CTLS_CVM_CDCVM) {
            return ("3F0002");
        } else {
            return ("1F0302");
        }
    }

    private void setCustomTags(TransRec trans) {
        iCtls.ctlsSetTag(trans_seq_counter, Util.hexStringToByteArray(String.format("%08d", Stan.getCurValue())));
        try {
            String svcCode = d.getP2PLib().getIP2P().p2peGetData(SERVICE_CODE.ordinal());
            if (Util.isNullOrEmpty(svcCode)) {
                svcCode = "000";
            }
            iCtls.ctlsSetTag(service_code, Util.hexStringToByteArray("0" + svcCode));
        } catch (Exception e) {
            Timber.w(e);
        }

        iCtls.ctlsSetTag(ifd_ser_num, MalFactory.getInstance().getHardware().getSerialNumber().substring(0, 8).getBytes());

        String date = trans.getAudit().getTransDateTimeAsString("yyMMdd");
        iCtls.ctlsSetTag(tran_date, Util.str2Bcd(date));

        Integer currency = Integer.valueOf(trans.getAmounts().getCurrency());
        iCtls.ctlsSetTag(trans_curcy_code, Util.DecToBCD(currency, 2));
        switch (trans.getCard().getCaptureMethod()) {
            case CTLS_MSR:
                iCtls.ctlsSetTag(pos_entry_mode, new byte[]{(byte) 0x91});
                break;
            case CTLS:
                iCtls.ctlsSetTag(pos_entry_mode, new byte[]{(byte) 0x07});
                break;
            default:
                break;
        }

        if (iCtls.ctlsGetTag(cvm_results) == null) {
            iCtls.ctlsSetTag(cvm_results, Util.hexStringToByteArray(packCvmr()));
        }

        if (iCtls.ctlsGetTag(amt_other_num) == null) {
            iCtls.ctlsSetTag(amt_other_num, Util.hexStringToByteArray("000000000000"));
        }

        if (iCtls.ctlsGetTag(term_type) == null) {
            iCtls.ctlsSetTag(term_type, Util.hexStringToByteArray("22"));
        }

        if (iCtls.ctlsGetTag(isssuer_code_tbl) == null) {
            iCtls.ctlsSetTag(isssuer_code_tbl, Util.hexStringToByteArray("01"));
        }

        iCtls.ctlsSetTag(mer_id, Util.rightPadding(trans.getAudit().getMerchantId(), 15).getBytes());
        iCtls.ctlsSetTag(temr_id, Util.rightPadding(trans.getBestTerminalId(d.getPayCfg().getStid()), 8).getBytes());

        if (iCtls.ctlsGetTag(appl_pre_name) == null && trans.getCard().getCardName(d.getPayCfg()).length() > 0) {
            String cardname = Util.rightPadding(trans.getCard().getCardName(d.getPayCfg()), 16);
            iCtls.ctlsSetTag(appl_pre_name, cardname.substring(0,16).getBytes());
        }

        if (iCtls.ctlsGetTag(appl_label) == null && trans.getCard().getCardName(d.getPayCfg()).length() > 0) {
            String cardname = Util.rightPadding(trans.getCard().getCardName(d.getPayCfg()), 16);
            iCtls.ctlsSetTag(appl_label, cardname.substring(0,16).getBytes());
        }

        if (trans.getCard().isEftposCard()) {
            byte[] ttq = iCtls.ctlsGetTag(visa_ttq);
            // if ttq not found in tag collection, use value from config
            if (ttq == null || ttq.length <= 0) {
                CtlsCfg.CtlsAid aidCfg = getCtlsAidCfg( d, trans );
                if( null != aidCfg ) {
                    iCtls.ctlsSetTag(visa_ttq, Util.hexStringToByteArray(aidCfg.getTTQ()));
                }
            }


        }
    }

    private void ctlsGetCvmType(TransRec trans) {
        IP2PEMV.P2P_CTLS_CVM cvm = d.getP2PLib().getIP2PCtls().ctlsGetCvmRequired();
        if (cvm == null) {
            trans.getCard().setCvmType(NO_CVM);
            return;
        }

        if (cvm == P2P_CTLS_CVM_SIGNATURE) {
            trans.getCard().setCvmType(SIG);
        } else if (cvm == P2P_CTLS_CVM_ONLINE_PIN) {
            trans.getCard().setCvmType(ENCIPHERED_ONLINE_PIN);
        } else if (cvm == P2P_CTLS_CVM_CDCVM) {
            trans.getCard().setCvmType(CDCVM);
        } else {
            trans.getCard().setCvmType(NO_CVM);
        }
    }
}
