package com.linkly.libengine.engine.cards;

import static com.linkly.libengine.engine.EngineManager.TransType.TOPUPCOMPLETION;
import static com.linkly.libengine.engine.protocol.IProto.RejectReasonType.CARD_REMOVED;
import static com.linkly.libengine.engine.protocol.IProto.RejectReasonType.DECLINED_BY_CARD_POST_COMMS;
import static com.linkly.libengine.engine.protocol.IProto.RejectReasonType.DECLINED_BY_CARD_PRE_COMMS;
import static com.linkly.libengine.engine.transactions.properties.TAudit.ReasonOnlineCode.RTIME_CARD_ACCEPTOR_SUSPICIOUS;
import static com.linkly.libengine.engine.transactions.properties.TAudit.ReasonOnlineCode.RTIME_FORCED_CARD_ACCEPTOR;
import static com.linkly.libengine.engine.transactions.properties.TAudit.ReasonOnlineCode.RTIME_FORCED_CARD_ISSUER;
import static com.linkly.libengine.engine.transactions.properties.TAudit.ReasonOnlineCode.RTIME_FORCED_ICC;
import static com.linkly.libengine.engine.transactions.properties.TAudit.ReasonOnlineCode.RTIME_FORCED_TERMINAL;
import static com.linkly.libengine.engine.transactions.properties.TAudit.ReasonOnlineCode.RTIME_OVER_FLOOR;
import static com.linkly.libengine.engine.transactions.properties.TAudit.ReasonOnlineCode.TERMINAL_RANDOM_SELECT;
import static com.linkly.libengine.engine.transactions.properties.TCard.CaptureMethod.ICC_FALLBACK_KEYED;
import static com.linkly.libengine.engine.transactions.properties.TCard.CvmType.ENCIPHERED_OFFLINE_PIN;
import static com.linkly.libengine.engine.transactions.properties.TCard.CvmType.ENCIPHERED_ONLINE_PIN;
import static com.linkly.libengine.engine.transactions.properties.TCard.CvmType.ENCIPHERED_PIN_AND_SIG;
import static com.linkly.libengine.engine.transactions.properties.TCard.CvmType.NO_CVM;
import static com.linkly.libengine.engine.transactions.properties.TCard.CvmType.NO_CVM_SET;
import static com.linkly.libengine.engine.transactions.properties.TCard.CvmType.PLAINTEXT_OFFLINE_PIN;
import static com.linkly.libengine.engine.transactions.properties.TCard.CvmType.PLAINTEXT_PIN_AND_SIG;
import static com.linkly.libengine.engine.transactions.properties.TCard.CvmType.SIG;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.ACC_TYPE_CHEQUE;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.ACC_TYPE_CREDIT;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.ACC_TYPE_SAVINGS;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.AuthMethod.OFFLINE_POSTCOMMS_AUTHORISED;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.AuthMethod.OFFLINE_PRECOMMS_AUTHORISED;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.HostResult.CONNECT_FAILED;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.HostResult.NO_RESPONSE;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.ReversalReason.GENACFAIL;
import static com.linkly.libengine.status.IStatus.STATUS_EVENT.STATUS_ERR_HOST_DECLINED;
import static com.linkly.libengine.status.IStatus.STATUS_EVENT.STATUS_ERR_ISSUER_UNAVAILABLE;
import static com.linkly.libsecapp.IP2PEMV.P2P_EMV_ERROR_CODES.P2P_EMV_6800;
import static com.linkly.libsecapp.IP2PEMV.P2P_EMV_ERROR_CODES.P2P_EMV_6985;
import static com.linkly.libsecapp.IP2PEMV.P2P_EMV_ERROR_CODES.P2P_EMV_BAD_RESPONSE;
import static com.linkly.libsecapp.IP2PEMV.P2P_EMV_ERROR_CODES.P2P_EMV_CARD_REMOVED;
import static com.linkly.libsecapp.IP2PEMV.P2P_EMV_ERROR_CODES.P2P_EMV_CMD_FAILED;
import static com.linkly.libsecapp.IP2PEMV.P2P_EMV_ERROR_CODES.P2P_EMV_COMMS_ERROR;
import static com.linkly.libsecapp.IP2PEMV.P2P_EMV_ERROR_CODES.P2P_EMV_GENERAL_ERROR;
import static com.linkly.libsecapp.IP2PEMV.P2P_EMV_ERROR_CODES.P2P_EMV_INVALID_ARG;
import static com.linkly.libsecapp.IP2PEMV.P2P_EMV_ERROR_CODES.P2P_EMV_OK;
import static com.linkly.libsecapp.IP2PEMV.P2P_EMV_ERROR_CODES.P2P_EMV_SERVICE_NOT_ALLOWED;
import static com.linkly.libsecapp.IP2PEMV.P2P_EMV_HOST_DECISION.P2P_EMV_HOST_AUTHORISED;
import static com.linkly.libsecapp.IP2PEMV.P2P_EMV_HOST_DECISION.P2P_EMV_HOST_DECLINED;
import static com.linkly.libsecapp.IP2PEMV.P2P_EMV_HOST_DECISION.P2P_EMV_HOST_FAILED_TO_CONNECT;
import static com.linkly.libsecapp.IP2PEMV.P2P_EMV_HOST_DECISION.P2P_EMV_HOST_NOT_SET;
import static com.linkly.libsecapp.IP2PEMV.P2P_EMV_HOST_DECISION.P2P_EMV_HOST_REFFERRED;
import static com.linkly.libsecapp.IP2PEMV.P2P_EMV_POS_DECISION.P2P_EMV_POS_DEC_FORCE_DECLINE;
import static com.linkly.libsecapp.IP2PEMV.P2P_EMV_POS_DECISION.P2P_EMV_POS_DEC_NO_DECISION;
import static com.linkly.libsecapp.IP2PEMV.P2P_EMV_REFER_DECISION.P2P_EMV_REFER_AUTHORISED;
import static com.linkly.libsecapp.IP2PEMV.P2P_EMV_REFER_DECISION.P2P_EMV_REFER_DECLINED;
import static com.linkly.libsecapp.IP2PEMV.P2P_EMV_REFER_DECISION.P2P_EMV_REFER_NOT_SET;
import static com.linkly.libsecapp.IP2PEncrypt.ElementType.SERVICE_CODE;
import static com.linkly.libsecapp.IP2PEncrypt.ElementType.TRACK_2_FULL_CHIP;
import static com.linkly.libsecapp.emv.Tag.account_type;
import static com.linkly.libsecapp.emv.Tag.aid;
import static com.linkly.libsecapp.emv.Tag.appl_cryptogram;
import static com.linkly.libsecapp.emv.Tag.appl_cryptogram_genAc2;
import static com.linkly.libsecapp.emv.Tag.appl_id;
import static com.linkly.libsecapp.emv.Tag.appl_label;
import static com.linkly.libsecapp.emv.Tag.appl_pan_seqnum;
import static com.linkly.libsecapp.emv.Tag.appl_pre_name;
import static com.linkly.libsecapp.emv.Tag.auth_resp_code;
import static com.linkly.libsecapp.emv.Tag.cardholder_name;
import static com.linkly.libsecapp.emv.Tag.crypt_info_data;
import static com.linkly.libsecapp.emv.Tag.crypt_info_data_genAc2;
import static com.linkly.libsecapp.emv.Tag.cvm_list;
import static com.linkly.libsecapp.emv.Tag.effect_date;
import static com.linkly.libsecapp.emv.Tag.expiry_date;
import static com.linkly.libsecapp.emv.Tag.isssuer_code_tbl;
import static com.linkly.libsecapp.emv.Tag.issuer_app_data;
import static com.linkly.libsecapp.emv.Tag.issuer_script_results;
import static com.linkly.libsecapp.emv.Tag.mer_id;
import static com.linkly.libsecapp.emv.Tag.pos_entry_mode;
import static com.linkly.libsecapp.emv.Tag.service_code;
import static com.linkly.libsecapp.emv.Tag.temr_id;
import static com.linkly.libsecapp.emv.Tag.term_cap;
import static com.linkly.libsecapp.emv.Tag.track2_eq_data;
import static com.linkly.libui.UIScreenDef.CARD_REMOVED_EARLY;
import static com.pax.jemv.clcommon.ACType.AC_ARQC;
import static com.pax.jemv.clcommon.ACType.AC_TC;

import com.linkly.libengine.action.InitialProcessing;
import com.linkly.libengine.action.TransactionCanceller;
import com.linkly.libengine.action.TransactionDecliner;
import com.linkly.libengine.action.TransactionReferral;
import com.linkly.libengine.config.PayCfg;
import com.linkly.libengine.dependencies.IDependency;
import com.linkly.libengine.engine.protocol.IProto;
import com.linkly.libengine.engine.transactions.TransRec;
import com.linkly.libengine.engine.transactions.properties.TCard;
import com.linkly.libengine.engine.transactions.properties.TProtocol;
import com.linkly.libengine.overrides.CoreOverrides;
import com.linkly.libengine.status.IStatus;
import com.linkly.libmal.IMal;
import com.linkly.libmal.global.util.Util;
import com.linkly.libpositive.wrappers.TagDataToPOS;
import com.linkly.libsecapp.EmvCfg;
import com.linkly.libsecapp.IP2PEMV;
import com.linkly.libsecapp.IP2PEncrypt;
import com.linkly.libsecapp.P2PLib;
import com.linkly.libsecapp.emv.EmvTag;
import com.linkly.libsecapp.emv.EmvTags;
import com.linkly.libsecapp.emv.Tag;
import com.linkly.libui.IUIDisplay;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import timber.log.Timber;

public class Emv {

    private static Emv ourInstance = new Emv();
    EmvListener listener = null;

    private Emv() {
    }

    public static Emv getInstance() {
        return ourInstance;
    }

    public static TCard.CvmType emvGetCvmFromByte1(byte byte1) {
        TCard.CvmType eCVM = NO_CVM_SET;

        Timber.i( "byte 1 = 0X%X", byte1 );

        // look at 1st uint8_t to determine CVM type performed - see EMV spec book 3, 'CV Rule uint8_t 1' for spec
        switch (byte1 & 0x3F)   // mask off high 2 bits
        {
            case 0x01:  // plaintext pin
                Timber.i( "plaintext pin");
                eCVM = PLAINTEXT_OFFLINE_PIN;
                break;
            case 0x02:  // enciphered online pin
                Timber.i( "enciphered online pin");
                eCVM = ENCIPHERED_ONLINE_PIN;
                break;
            case 0x03:  // plaintext pin AND signature
                Timber.i( "plaintext pin AND signature");
                eCVM = PLAINTEXT_PIN_AND_SIG;
                break;
            case 0x04:  // enciphered offline pin
                Timber.i( "enciphered offline pin");
                eCVM = ENCIPHERED_OFFLINE_PIN;
                break;
            case 0x05:  // enciphered offline pin AND signature
                Timber.i( "enciphered offline pin AND signature");
                eCVM = ENCIPHERED_PIN_AND_SIG;
                break;

            case 0x1E:  // signature (paper)
                Timber.i( "signature (paper)");
                eCVM = SIG;
                break;

            case 0x00:  // CVM failed
                Timber.i( "CVM FAILED");
                eCVM = NO_CVM_SET;
                break;
            case 0x1F:  // NO CVM required
                Timber.i( "NO CVM required");
                eCVM = NO_CVM;
                break;
            default:
                Timber.i( "unknown CVM 0x%x", (byte1 & 0x3F));
                eCVM = NO_CVM_SET;
                break;
        }
        return eCVM;
    }

    /*****************************************************************************
     * debug the rule from uint8_t 2
     *****************************************************************************/
    private void emvGetCVMRuleFromByte2(byte byte2) {

        Timber.i( "byte 2 = 0X%X", byte2);
        switch (byte2) {
            case 0x0:
                Timber.i( "Always.");
                break;
            case 0x1:
                Timber.i( "If unattended cash.");
                break;
            case 0x2:
                Timber.i( "If not unattended cash and not manual cash and not purchase with cashback.");
                break;
            case 0x3:
                Timber.i( "If terminal supports the CVM.");
                break;
            case 0x4:
                Timber.i( "If manual cash.");
                break;
            case 0x5:
                Timber.i( "If purchase with cashback.");
                break;
            case 0x6:
                Timber.i( "If transaction is in the application currency and is under X value (see section 10.5 for a discussion of X.");
                break;
            case 0x7:
                Timber.i( "If transaction is in the application currency and is over X value.");
                break;
            case 0x8:
                Timber.i( "If transaction is in the application currency and is under Y value (see section 10.5 for a discussion of Y.");
                break;
            case 0x9:
                Timber.i( "If transaction is in the application currency and is over Y value.");
                break;
            default:
                break;
        }
        if (byte2 >= 0x0A && byte2 <= 0x7F) {
            Timber.i( "RFU");
        } else if (byte2 >= 0x80) {
            Timber.i( "Reserved for use by individual payment system");
        }
    }

    private int emvGetCVMResultFromByte3(byte byte3) {
        int res = -1;

        switch (byte3) {
            case 0x0:
                Timber.i( "Unkown");
                break;
            case 0x1:
                Timber.i( "Failed");
                break;
            case 0x2:
                Timber.i( "Success");
                res = 1;
                break;
        }

        return res;
    }

    /*****************************************************************************
     * output the currently configured cvm list
     *****************************************************************************/
    private void emvDebugCvmList(TransRec trans) {

        int i = 8; /* data starts at the 8th byte */
//        boolean forceOnlinePinTest = false;
//        boolean forceOfflinePintest = false;
        byte[] cvmList = trans.getCard().getTags().getTag(cvm_list);

        if (cvmList == null) {
            return;
        }
/*
        if (forceOfflinePintest) {
            cvmList[8] = 0x01;
            cvmList[9] = 0x00;
            iEmv.emvSetTag((short) 0x8E, cvmList);
        }

        if (forceOnlinePinTest) {
            cvmList[8] = 0x02;
            cvmList[9] = 0x00;
            iEmv.emvSetTag((short) 0x8E, cvmList);
        }
*/

        while (i < cvmList.length) {
            Timber.i( "**************************** CVM Rule %d ****************************", ( ( i - 8 ) / 2 + 1 ) );
            emvGetCvmFromByte1(cvmList[i]);
            i++;
            emvGetCVMRuleFromByte2(cvmList[i]);
            i++;
        }
        Timber.i( "*********************************************************************");
    }

    public void start(IDependency d) {
        IP2PEMV iEmv = d.getP2PLib().getIP2PEmv();
        IP2PEncrypt iP2PE = d.getP2PLib().getIP2PEncrypt();
        TransRec trans = d.getCurrentTransaction();
        IP2PEMV.P2P_EMV_ERROR_CODES errorCode = P2P_EMV_OK;

        d.getStatusReporter().reportStatusEvent( IStatus.STATUS_EVENT.STATUS_TRANS_EMV , trans.isSuppressPosDialog() );

        if (listener == null) {
            listener = new EmvListener(d);
        }

        byte[] countryCode = Util.str2Bcd(trans.getAudit().getCountryCode());//  { 0x08, 0x26 };
        byte[] transCurrCode = Util.str2Bcd(trans.getAmounts().getCurrency());//  { 0x08, 0x26 };

        byte[] transType = Util.hexStringToByteArray(d.getProtocol().getEmvProcessingCode(trans));

        boolean bypassEnabled = false;

        iEmv.emvInit(listener, countryCode, transCurrCode, transType[0], bypassEnabled, trans.isStartedInOfflineMode());
        errorCode = iEmv.emvStart();
        if (errorCode != P2P_EMV_OK) {
            if (errorCode == P2P_EMV_CARD_REMOVED ||
                    errorCode == P2P_EMV_COMMS_ERROR ||
                    errorCode == P2P_EMV_GENERAL_ERROR ||
                    errorCode == P2P_EMV_CMD_FAILED ||
                    errorCode == P2P_EMV_BAD_RESPONSE ||
                    errorCode == P2P_EMV_6800) {
                Timber.i( "EMV_FALLBACK_3_TIMES");

            }
            emvHandleError(d, errorCode, trans);
            return;
        }

        emvSetInputEMVTags(d, trans, iEmv);

        emvOverrideCvmList(trans, iEmv);

        emvGetEMVTags(d, trans, iEmv, iP2PE);

        emvDebugCvmList(trans);

        // for selected AID (if any), check cfg_emv.json for matching defaultAccount setting and if found, select that account for this transaction (skips user account select state)
        emvGetDefaultAccount(d,trans);

    }

    public void emvGetDefaultAccount(IDependency d, TransRec trans) {
        if( d.getConfig() != null ) {
            EmvCfg emvCfg = d.getConfig().getEmvCfg();

            if( emvCfg == null || emvCfg.getSchemes() == null )
                return;

            // find match for selected AID
            for( EmvCfg.EmvScheme emvScheme : emvCfg.getSchemes() ) {
                // check the RID for this scheme matches the selected AID
                if( trans.getCard().getAid().startsWith(emvScheme.getRid())) {
                    // yes it does, now check AIDs
                    for (EmvCfg.EmvAid emvAid : emvScheme.getAids()) {
                        // perform partial match, i.e. aid on card can be longer, but first part must match
                        if (trans.getCard().getAid().startsWith(emvAid.getAid())) {
                            // found matching AID in config. does it have a default account?
                            if (emvAid.getDefaultAccount() != null) {
                                // yes, select this account
                                switch (emvAid.getDefaultAccount()) {
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
                            // finished, return
                            return;
                        }
                    }
                }
            }
        }
    }

    /* set all of the tags sent back apart from the script data */
    private void setEmvResponseTags(TransRec trans, IP2PEMV iEmv) {
        EmvTags tags = trans.getCard().getRespTags();
        if (tags == null) {
            return;
        }

        for (EmvTag tag : tags.values()) {

            if ((short) tag.getTag() == 0x71) {
                Timber.i( "Script 71: %s", Util.byteArrayToHexString(tag.getData()));
            } else if ((short) tag.getTag() == 0x72) {
                Timber.i( "Script 72: %s", Util.byteArrayToHexString(tag.getData()));
            } else if ((short) tag.getTag() == 0x8A && CoreOverrides.get().isSpoofComms()) {
                Timber.i( "Response code: %s", new String(tag.getData()));
                iEmv.emvSetTag((short) tag.getTag(), "00".getBytes());
            } else if ((short) tag.getTag() == 0x91 && trans.getCard().getIssuerAuthData() != null) {
                // Update tag 91 data with processed Issuer Auth Data(IAD). This could be processed differently for different acquirers based on host response
                byte[] issuerAuthData = trans.getCard().getIssuerAuthData();
                iEmv.emvSetTag((short) tag.getTag(), issuerAuthData);
                Timber.i("IAD: %s", Util.byteArrayToHexString(issuerAuthData));
            } else {
                iEmv.emvSetTag((short) tag.getTag(), tag.getData());
            }
        }
    }

    public IP2PEMV.P2P_EMV_ERROR_CODES genAc1(IDependency d, TransRec trans, IMal mal) {
        IP2PEMV iEmv = d.getP2PLib().getIP2PEmv();
        IP2PEncrypt iP2PE = d.getP2PLib().getIP2PEncrypt();

        boolean bypassEmvCvm = false;
        IP2PEMV.P2P_EMV_POS_DECISION ePosDecision = P2P_EMV_POS_DEC_NO_DECISION;
        updateEmvAmounts(d, trans, iEmv);

        iEmv.emvOverrideGenAc1Result(-1);
        if (trans.isOfflineTransaction(d.getPayCfg())) {
            // clear online PIN cvm bit, so kernel doesn't attempt online pin
            byte[] termCaps = iEmv.emvGetTag(term_cap);
            termCaps[1] &= ~0x40;
            iEmv.emvSetTag((short) 0x9F33, termCaps);

            // force decline (request aac cryptogram) from card
            Timber.i( "P2P_EMV_POS_DEC_FORCE_DECLINE");
            ePosDecision = P2P_EMV_POS_DEC_FORCE_DECLINE;
            trans.getCard().setGenAc2Required(false);

            if (d.getCustomer().supportOfflineAsKeyed())
                trans.getCard().setCaptureMethod(ICC_FALLBACK_KEYED);

            // overrides aac result as declined to offline approved
            iEmv.emvOverrideGenAc1Result(AC_TC);

            if (trans.getTransType() == TOPUPCOMPLETION) {
                bypassEmvCvm = true;
            }
        } else if( trans.isRefund() ) {
            // this is a refund but not an offline one - i.e. go online for auth

            // force decline (request aac cryptogram) from card, as per EMV industry specific rules spec
            Timber.i( "P2P_EMV_POS_DEC_FORCE_DECLINE");
            ePosDecision = P2P_EMV_POS_DEC_FORCE_DECLINE;
            trans.getCard().setGenAc2Required(false);

            // don't treat trans aac result as declined, treat it as 'go online for auth'
            iEmv.emvOverrideGenAc1Result(AC_ARQC);
        }

        if (CoreOverrides.get().isAutoFillTrans()) {
            byte[] termCaps = iEmv.emvGetTag(term_cap);
            termCaps[1] = 0x2F;
            iEmv.emvSetTag((short) 0x9F33, termCaps);
        }

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

        Timber.i( "emvGenAC1:%s", ePosDecision.name());
        IP2PEMV.P2P_EMV_ERROR_CODES eRet = iEmv.emvGenAC1(ePosDecision, bypassEmvCvm, trans.getAmounts().getTotalAmount(), trans.getAmounts().getCashbackAmount(), ucTransType);

        mal.getHardware().enablePowerKey(false);

        emvGetEMVTags(d, trans, iEmv, iP2PE);

        if (eRet != P2P_EMV_OK) {
            emvHandleError(d, eRet, trans);
            return eRet;
        }

        IP2PEMV.P2P_EMV_CID_RESULT result = iEmv.emvGetCidResult();
        Timber.i( "GENAC1 Result " + result.name() + " " + result.toString());

        /* just for debug purposes to compare to the CidResult in case anything strange happens */
        if (trans.getCard().getCryptogramType() != null) {
            switch ((byte) trans.getCard().getCryptogramType().intValue() & 0xC0) {
                case 0:
                    Timber.i( "Debug Crypt Only (1st Gen AC AAC returned - DECLINED OFFLINE)");
                    break;
                case 0x40:
                    Timber.i( "Debug Crypt Only (1st Gen AC TC returned - APPROVED)");
                    break;
                case 0xC0:
                    Timber.i( "Debug Crypt Only (1st Gen AC returned Referred)");
                    break;
                case 0x80:
                    Timber.i( "Debug Crypt Only (1st Gen AC returned ARQC - ONLINE REQUIRED)");
                    break;
            }
        }

        switch (result) {
            case P2P_EMV_CID_AAC:
                Timber.i( "1st Gen AC AAC returned - DECLINED OFFLINE");

                if (d.getProtocol().requiresDeclinedAdvices()) {
                    trans.updateMessageStatus(TProtocol.MessageStatus.ADVICE_QUEUED, false);
                    trans.save();
                }

                d.getProtocol().setInternalRejectReason( trans, DECLINED_BY_CARD_PRE_COMMS);
                d.getWorkflowEngine().setNextAction(TransactionDecliner.class);
                trans.getCard().setGenAc2Required(false);
                trans.getCard().setCardDeclined(true);
                break;

            case P2P_EMV_CID_TC:
                Timber.i("1st Gen AC TC returned - APPROVED");
                trans.getProtocol().setCanAuthOffline(true);
                trans.getProtocol().setAuthMethod(OFFLINE_PRECOMMS_AUTHORISED); // setting this is a suggestion to authorise offline, depending on checks
                trans.getCard().setGenAc2Required(false);
                break;

            case P2P_EMV_CID_AAR:
                d.getWorkflowEngine().setNextAction(TransactionReferral.class);
                Timber.i( "1st Gen AC returned Referred");
                break;

            case P2P_EMV_CID_ARQC:
                Timber.i( "1st Gen AC returned ARQC - ONLINE REQUIRED");
                 SetEmvReasonOnlineCode(trans);

                break;
        }

        return P2P_EMV_OK;
    }


    public static void SetEmvReasonOnlineCode(TransRec trans) {
        if (trans.isCashback())
            trans.getAudit().setReasonOnlineCode(RTIME_FORCED_CARD_ACCEPTOR);
        else if ( emvCheckTvrBitSet( trans, 4, ( byte ) 0x80 ) ) {
            trans.getAudit().setReasonOnlineCode(RTIME_OVER_FLOOR);
        } else if ( emvCheckTvrBitSet( trans, 4, ( byte ) 0x10 ) ) { /* transaction selected randomly */
            trans.getAudit().setReasonOnlineCode(TERMINAL_RANDOM_SELECT);
        } else if ( emvCheckTvrBitSet( trans, 4, ( byte ) 0x08 ) ) { /* merchant forced transaction online */
            trans.getAudit().setReasonOnlineCode(RTIME_FORCED_TERMINAL);
        } else if ( emvCheckTvrBitSet( trans, 2, ( byte ) 0x08 ) ) { /* new card */
            trans.getAudit().setReasonOnlineCode(RTIME_FORCED_CARD_ISSUER);
        } else if ( emvCheckTvrBitSet( trans, 2, ( byte ) 0x40 ) ) { /* expired card */
            trans.getAudit().setReasonOnlineCode(RTIME_FORCED_CARD_ISSUER);
        } else {
            trans.getAudit().setReasonOnlineCode(RTIME_FORCED_ICC);
        }
    }

    private static boolean emvCheckTvrBitSet(TransRec trans, int byteNumber, byte bitMask) {

        String tvr= trans.getCard().getTvr();
        if (tvr == null || tvr.length() == 0) {
            return false;
        }

        byte[] bTvr = Util.hexStringToByteArray(tvr);
        byte value = bTvr[byteNumber - 1];
        byte result = (byte) (value & bitMask);
        if (result != 0) {
            return true;
        }
        return false;
    }

    /* for amex we have been told to use the last 2 bytes of the IAD if possible */
    private String getAuthResponseCodeTag8A(TransRec trans, TProtocol.HostResult hostRes, PayCfg config) {
        String protocolRespCode = trans.getProtocol().getPosResponseCode(); // use 2-digit (aka POS) response code
        // connect failure scenarios
        if( hostRes == CONNECT_FAILED || hostRes == NO_RESPONSE ) {
            // don't set a tag 8A ARC value if we didn't get one. Leave it up to the kernel to set one
            return "";
        }

        if( Util.isNullOrEmpty(protocolRespCode) || protocolRespCode.equals("X0")) {
            // host response code not set, or X0 indicates connect failure, treat same as above. Let kernel set ARC
            return "";
        }

        // for amex, we take the ARC from the last 2 bytes of the IAD tag 91
        if (trans.getCard().isAmexCard(config) && trans.getCard().getIssuerAuthData() != null) {
            byte[] iad = trans.getCard().getIssuerAuthData();
            if (iad != null && iad.length >= 10) {
                String s = String.format("%c%c", iad[iad.length - 2], iad[iad.length - 1]);
                Timber.i( "Amex - using 8A value '%s'", s );
                return s;
            }
        }

        // else for all other cases, use actual resp code from DE39
        return protocolRespCode;
    }

    public boolean genAc2(IDependency d) {

        IP2PEMV.P2P_EMV_REFER_DECISION referDecision = P2P_EMV_REFER_NOT_SET;
        TransRec trans = d.getCurrentTransaction();
        IUIDisplay ui = d.getUI();
        TCard cardInfo = trans.getCard();
        IP2PEMV iEmv = d.getP2PLib().getIP2PEmv();
        IP2PEMV.P2P_EMV_HOST_DECISION eHostResult = P2P_EMV_HOST_NOT_SET;
        TProtocol.HostResult hostRes = trans.getProtocol().getHostResult();

        if (!cardInfo.isGenAc2Required()) {
            Timber.i("GenAc2 Not Required");
            emvGetEMVTagsAtEnd(trans, false, iEmv);

            // if refund transaction, and comms error
            if( trans.isRefund() && ((hostRes == CONNECT_FAILED) || (hostRes == NO_RESPONSE)) ) {
                // we didn't get host approval, attempt to authorise offline. CheckResult has logic to determine if this will succeed/authorise or not
                trans.getProtocol().setCanAuthOffline(true);
                trans.getProtocol().setAuthMethod(OFFLINE_POSTCOMMS_AUTHORISED);
            }

            return true;
        }

        switch (hostRes) {
            case CONNECT_FAILED:
            case NO_RESPONSE:
            default:
                Timber.i( "CONNECT_FAILED, NO_RESPONSE=P2P_EMV_HOST_FAILED_TO_CONNECT, hostRes = %s", hostRes.name() );
                eHostResult = P2P_EMV_HOST_FAILED_TO_CONNECT;
                d.getStatusReporter().reportStatusEvent(STATUS_ERR_ISSUER_UNAVAILABLE, trans.isSuppressPosDialog());
                break;

            case REQUEST_REFERRAL:
                Timber.i( "REQUEST_REFERRAL=P2P_EMV_HOST_REFERRED");
                eHostResult = P2P_EMV_HOST_REFFERRED;

                if (trans.isReferred())
                    referDecision = P2P_EMV_REFER_AUTHORISED;
                else
                    referDecision = P2P_EMV_REFER_DECLINED;
                break;

            case DECLINED:
                Timber.i( "DECLINED=P2P_EMV_HOST_DECLINED");
                eHostResult = P2P_EMV_HOST_DECLINED;
                d.getStatusReporter().reportStatusEvent(STATUS_ERR_HOST_DECLINED ,trans.isSuppressPosDialog());
                break;

            case AUTHORISED:
                Timber.i( "AUTHORISED=P2P_EMV_HOST_AUTHORISED");
                eHostResult = P2P_EMV_HOST_AUTHORISED;
                break;
        }

        /* put things like the (9F10 back to the card (not the script data) */
        setEmvResponseTags(trans, iEmv);

        Timber.i( "Script71: %s", Util.byteArrayToHexString(trans.getCard().getScript71Data()));
        Timber.i( "Script72: %s", Util.byteArrayToHexString(trans.getCard().getScript72Data()));
        Timber.i( "emvGenAc2, host result = %s", eHostResult.name() );
        IP2PEMV.P2P_EMV_ERROR_CODES eRet = iEmv.emvGenAc2(eHostResult, referDecision, trans.getCard().getScript71Data(), trans.getCard().getScript72Data(),
                trans.getProtocol().getAuthCode(), getAuthResponseCodeTag8A(trans, hostRes, d.getPayCfg()));

        trans.getCard().setGenAc2Required(false);

        // update tags after 2nd Gen AC and serialise them to trans record
        emvGetEMVTagsAtEnd(trans, true, iEmv);

        if (eRet != P2P_EMV_OK && !CoreOverrides.get().isSpoofComms() && !CoreOverrides.get().isSpoofApprove2ndGenAC()) {
            // show error that card was removed. most error codes except the ones listed here occur because card has been removed early
            if (eRet != P2P_EMV_INVALID_ARG && eRet != P2P_EMV_6985 && eRet != P2P_EMV_SERVICE_NOT_ALLOWED ) {
                ui.showScreen(CARD_REMOVED_EARLY);
                d.getProtocol().setInternalRejectReason( trans, CARD_REMOVED );
            } else {
                d.getProtocol().setInternalRejectReason( trans, DECLINED_BY_CARD_POST_COMMS);
            }

            trans.getAudit().setReasonOnlineCode(RTIME_CARD_ACCEPTOR_SUSPICIOUS);
            Timber.e( "GenAc2 host authorised but card declined, eRet=%s, generating reversal", eRet.name());
            if (eHostResult == P2P_EMV_HOST_AUTHORISED) {
                hostAuthorisedCardDeclined(d, trans);
            }
            Timber.i( "GenAc2 card declined the transaction");
            return false;
        }

        // set default value
        trans.getCard().setCardDeclined(false);

        switch( iEmv.emvGetCidResult() ) {
            case P2P_EMV_CID_AAC:
                Timber.i( "card declined the transaction");
                trans.getCard().setCardDeclined(true);

                // if host authorised, but card declined, then reverse txn and report failure reason as 'declined by card'
                if (eHostResult == P2P_EMV_HOST_AUTHORISED) {
                    hostAuthorisedCardDeclined(d, trans);
                }
                return false;

            case P2P_EMV_CID_TC:
                Timber.i( "card approved the transaction");
                if(hostRes == CONNECT_FAILED || hostRes == NO_RESPONSE) {
                    // we didn't get host approval, attempt to authorise offline. CheckResult has logic to determine if this will succeed/authorise or not
                    trans.getProtocol().setCanAuthOffline(true);
                    trans.getProtocol().setAuthMethod(OFFLINE_POSTCOMMS_AUTHORISED);
                }
                break;

            default:
                Timber.e("Unexpected CID result: %s", iEmv.emvGetCidResult().name());
                break;
        }

        return true;
    }

    private void hostAuthorisedCardDeclined(IDependency d, TransRec trans) {
        Timber.e( "GenAc2 host authorised but card declined, generating reversal");
        trans.setToReverse(GENACFAIL);
        d.getProtocol().setInternalRejectReason( trans, DECLINED_BY_CARD_POST_COMMS);
        setArcToZ4(trans);
        trans.save();
        d.getWorkflowEngine().setNextAction(TransactionDecliner.class);
    }

    /**
     * sets EMV tag in trans record for 8A (auth response code) to Z4, and saves trans record in database
     * Z4 is not EMV defined, but means 'transaction authorised by host but declined by card'
     * in this case, a reversal should always also be generated
     *
     * @param trans
     */
    private void setArcToZ4(TransRec trans) {
        // update EMV tag 8A. This may just be a woolies thing. if so in future, move to protocol setInternalRejectReason implementation
        byte[] currentArc = trans.getCard().getTags().getTag(auth_resp_code);
        if( currentArc != null && currentArc.length == 2 ) {
            Timber.i("ARC from kernel = %x %x", currentArc[0], currentArc[1]);
        }

        // error level to highlight in log, often this is an unexpected condition
        Timber.e("Host Authorised but card declined, declining and overwriting ARC (tag 8A) with Z4");
        byte[] declinedOfflinePostCommsArc = { 0x5A, 0x34 }; // ascii Z4, means declined by card post-comms
        trans.getCard().getTags().add(auth_resp_code, declinedOfflinePostCommsArc);
        trans.getCard().setArc("5A34");

        // save/serialise EMV tags to string
        trans.saveEmvTagsString();
    }

    private void updateEmvAmounts(IDependency d, TransRec trans, IP2PEMV iEmv) {
        long totalAmount = trans.getAmounts().getTotalAmount();
        long cashbackAmount = trans.getAmounts().getCashbackAmount();

        if (trans.isBalance()) {
            totalAmount = 0;
        }

        iEmv.emvSetTag((short) 0x81, Util.intToByteArray((int)totalAmount,4));
        iEmv.emvSetTag((short) 0x9F02, Util.DecToBCD(totalAmount, 6));

        // update 9c (trans type) if cash amt is present, may have been introduced later in transaction
        if (cashbackAmount > 0) {
        String ttype = d.getProtocol().getEmvProcessingCode(trans);
        byte[] transType = Util.hexStringToByteArray(ttype);
        iEmv.emvSetTag((short) 0x9c, transType);

            // set 9f03, only when cashback amt > 0
            byte[] cashbackAmtBcd = Util.DecToBCD(cashbackAmount, 6);
            iEmv.emvSetTag((short) 0x9F03, cashbackAmtBcd);
        }

        // if pwcb or cash then set floor limit to zero, else leave it and it'll use the cfg_emv or cfg_ctls_emv floor limit settings
        if (trans.isCashback()) {
            int floor = 0;
            String sFloor = String.format("%08x", floor);
            iEmv.emvSetTag((short) 0x9f1b, Util.hexStringToByteArray(sFloor));
        }
    }

    private int emvSetInputEMVTags(IDependency d, TransRec trans, IP2PEMV iEmv) {

        int sequenceNumber = trans.getProtocol().getStan();

        iEmv.emvSetTag((short) 0x9F41, Util.DecToBCD(sequenceNumber, 4));

        String date = trans.getAudit().getTransDateTimeAsString("yyMMdd");
        iEmv.emvSetTag((short) 0x9a, Util.str2Bcd(date));

        String time = trans.getAudit().getTransDateTimeAsString("HHmmss");
        iEmv.emvSetTag((short) 0x9f21, Util.str2Bcd(time));

        // get processing code, but being saved as trans type?
        String ttype = d.getProtocol().getEmvProcessingCode(trans);
        byte[] transType = Util.hexStringToByteArray(ttype);
        iEmv.emvSetTag((short) 0x9c, transType);


        Integer currency = Integer.valueOf(trans.getAmounts().getCurrency());
        iEmv.emvSetTag((short) 0x9f42, Util.DecToBCD(currency, 2));
        iEmv.emvSetTag((short) 0x5f2a, Util.DecToBCD(currency, 2));

        Integer country = Integer.valueOf(trans.getAudit().getCountryCode());
        iEmv.emvSetTag((short) 0x9f1a, Util.DecToBCD(country, 2));

        updateEmvAmounts(d, trans, iEmv);
        return 0;
    }

    private int emvOverrideCvmList(TransRec trans, IP2PEMV iEmv) {

        /*
            800000 (Byte 1 Bit 8) Manual key entry
            400000 (Byte 1 Bit 7) Magnetic stripe
            200000 (Byte 1 Bit 6) IC with contacts

            008000 (Byte 2 Bit 8) Plaintext ENCIPHERED_ONLINE_PIN for ICC verification
            004000 (Byte 2 Bit 7) Enciphered ENCIPHERED_ONLINE_PIN for online verification
            002000 (Byte 2 Bit 6) Signature (paper)
            001000 (Byte 2 Bit 5) Enciphered ENCIPHERED_ONLINE_PIN for offline verification
            000800 (Byte 2 Bit 4) No CVM Required

            000080 (Byte 3 Bit 8) SDA
            000040 (Byte 3 Bit 7) DDA
            000020 (Byte 3 Bit 6) Card capture

            000008 (Byte 3 Bit 4) CDA
        */

        /* remove online pin for completion because they are done offline */
        if (trans.isCompletion()) {
            byte[] termCaps = iEmv.emvGetTag(term_cap);
            termCaps[1] &= 0xBF;
            iEmv.emvSetTag((short)term_cap.getValue(), termCaps);
            Timber.i( "emvOverrideCvmList to:" + Util.byteArrayToHexString(termCaps));
        }
        return 0;
    }

    private void emvHandleError(IDependency d, IP2PEMV.P2P_EMV_ERROR_CODES iError, TransRec trans) {
        TCard cardInfo = trans.getCard();
        Timber.i( "emvHandleError " + iError.name());

        switch (iError) {
            // special case for candidate list empty
            case P2P_EMV_6985:
            case P2P_EMV_CANDIDATE_LIST_EMPTY:
            case P2P_EMV_COMMS_ERROR:
            case P2P_EMV_ATR_ERROR:
            case P2P_EMV_BAD_RESPONSE:
            case P2P_EMV_6800:
            case P2P_EMV_CMD_FAILED:
                if( !trans.checkFallback(d,true) ) {
                    d.getWorkflowEngine().setNextAction(TransactionCanceller.class);
                } else {
                    d.getWorkflowEngine().setNextAction(InitialProcessing.class);
                }
                trans.getCard().setShowReadError(true);

                Timber.i( "Return FALLBACK for: " + iError.name());
                return;// EMV_FALLBACK; //TODO sort icc failures and returns to fallback

            case P2P_EMV_CARD_REMOVED:
            case P2P_EMV_CARD_BLOCKED:
            case P2P_EMV_APPLICATION_BLOCKED:
            case P2P_EMV_USER_CANCELLED:
            case P2P_EMV_USER_TIMEOUT:
            case P2P_EMV_BAD_DATA_FORMAT:
            case P2P_EMV_SERVICE_NOT_ALLOWED:
            case P2P_EMV_DENIAL:
            case P2P_EMV_KEY_EXPIRED:
            case P2P_EMV_NO_PINPAD:
            case P2P_EMV_PIN_BYPASSED:
            case P2P_EMV_CONFIG_ERROR:
            case P2P_EMV_MISSING_DATA:
            case P2P_EMV_OVERFLOW:
            case P2P_EMV_NO_LOG:
            case P2P_EMV_NO_RECORD:
            case P2P_EMV_INVALID_PIN:
            case P2P_EMV_INVALID_ARG:
                Timber.i( "return CANCELLED for: " + iError.name());
                break;
            default:
                Timber.i( "Unhandled error " + iError.name());
                break;
        }
    }


    private void emvCacheMultiTags(IDependency d, IP2PEMV iEmv) {
        List<Tag> tags = new ArrayList<>();

        tags.add(account_type);
        tags.add(effect_date);
        tags.add(appl_id);
        tags.add(Tag.tvr);
        tags.add(Tag.tsi);
        tags.add(appl_pan_seqnum);
        tags.add(cardholder_name);
        tags.add(track2_eq_data);
        tags.add(appl_pre_name);
        tags.add(appl_label);
        tags.add(crypt_info_data);
        tags.add(appl_cryptogram);
        tags.add(aid);

        ArrayList<Tag> tags2 = d.getProtocol().getEmvTagList();

        if (tags2 != null) {
            for (Tag tag : tags2) {
                tags.add(tag);
            }
        } else {
            Timber.i( "Null tag list");
        }

        List<byte[]> tagsCached = iEmv.emvGetTags(tags);
        if (tagsCached != null && tagsCached.size() > 0) {
            Timber.i( "Cached Tags Count:" + tagsCached.size());
        }
    }

    private void emvCacheMultitagsAtEnd(IP2PEMV iEmv) {
        List<Tag> tags = new ArrayList<>();

        tags.add(crypt_info_data);
        tags.add(appl_cryptogram);
        tags.add(Tag.tvr);
        tags.add(Tag.tsi);
        tags.add(issuer_app_data);
        tags.add(issuer_script_results);
        tags.add(auth_resp_code);

        List<byte[]> tagsCached = iEmv.emvGetTags(tags);
        if (tagsCached != null && tagsCached.size() > 0) {
            Timber.i( "Cached Tags Count:%s", tagsCached.size());
            for( int i=0; i<tagsCached.size(); i++ ) {
                Tag tag = tags.get(i);
                Timber.i( "EmvTag after GenAC2: %X value: %s (%s)", tag.value(), Util.byteArrayToHexString(tagsCached.get(i)), tag.name() );
            }
        }
    }

    public int emvGetEMVTags(IDependency d, TransRec trans, IP2PEMV iEmv, IP2PEncrypt iP2PE) {

        emvCacheMultiTags(d, iEmv);

        byte[] validFrom = iEmv.emvGetTag(effect_date, true);
        if (validFrom != null && validFrom.length > 0) {
            trans.getCard().setValidFrom(Util.byteArrayToHexString(validFrom));
        }

        byte[] cardAid = iEmv.emvGetTag(appl_id, true);
        if (cardAid != null && cardAid.length > 0) {
            trans.getCard().setAid(Util.byteArrayToHexString(cardAid));
        }

        byte[] atc = iEmv.emvGetTag( Tag.atc, true );
        if( atc != null && atc.length > 0 ){
            trans.getCard().setAtc( Util.byteArrayToHexString( atc ) );
        }

        byte[] tvr = iEmv.emvGetTag(Tag.tvr, true);
        if (tvr != null && tvr.length > 0) {
            trans.getCard().setTvr(Util.byteArrayToHexString(tvr));
        }

        byte[] tsi = iEmv.emvGetTag(Tag.tsi, true);
        if (tsi != null && tsi.length > 0) {
            trans.getCard().setTsi(Util.byteArrayToHexString(tsi));
        }


        byte[] psn = iEmv.emvGetTag(appl_pan_seqnum, true);
        if (psn != null && psn.length > 0 && psn.length <= 3) {
            String s = Util.bcd2Str(psn);
            trans.getCard().setPsn(Integer.valueOf(s));
        }

        /*Get cardholder_name*/
        byte[] chName = iEmv.emvGetTag(cardholder_name, true);
        if (chName != null && chName.length > 0) {
            String s = new String(chName);
            if (s.contains("[A-Za-z]")) {
                trans.getCard().setCardHolderName(s);
            }
        }

        byte[] arcBinary = iEmv.emvGetTag(auth_resp_code, true);
        if (arcBinary != null && arcBinary.length == 2) {
            trans.getCard().setArc(Util.byteArrayToHexString(arcBinary));
        }

        // WC perform 'get tag' operation - this causes the track 2 data to be set internally in the p2pencrypt secure data store
        byte[] track2BcdFormat = iEmv.emvGetTag(track2_eq_data, true);
        // if we got some data back, get it again via the p2pencrypt method
        if (track2BcdFormat != null && track2BcdFormat.length > 0) {
            // get it again using * masking chars
            String track2 = iP2PE.getMaskedData(TRACK_2_FULL_CHIP);
            if (track2 != null && track2.length() > 0) {
                trans.getCard().updateTrack2(d, track2, trans);
                d.getDebugReporter().reportCardData( TagDataToPOS.CardEntryModeTag.CHIP_CARD, track2 );
            }
        }

        // Read Application Expiry Date to p2pencrypt secure data store only. Needed for adding it to host message later
        // Will not be saving it's value into TCard record, so return value is ignored
        iEmv.emvGetTag(expiry_date, true);

        String appName = "";
        byte[] appLabel = iEmv.emvGetTag(appl_pre_name, true);
        byte[] appLabel2 = iEmv.emvGetTag(appl_label, true);

        if (appLabel != null && appLabel.length > 0) {
            appName = new String(appLabel);
        }
        if (appName == null || appName.length() <= 0 || !Util.isAsciiPrintable(appName)) {
            if (appLabel2 != null && appLabel2.length > 0) {
                appName = new String(appLabel2);
            }
        }

        if (appName.length() > 0 && Util.isAsciiPrintable(appName)) {
            trans.getCard().setCardName(appName.trim());
        }

        byte[] cryptogramType = iEmv.emvGetTag(crypt_info_data, true);
        if (cryptogramType != null && cryptogramType.length > 0) {
            trans.getCard().setCryptogramType((int) cryptogramType[0]);
        }

        byte[] cryptogramValue = iEmv.emvGetTag(appl_cryptogram, true);
        if (cryptogramValue != null && cryptogramValue.length > 0) {
            trans.getCard().setCryptogram(cryptogramValue);
        }

        setCustomTags(trans, d.getPayCfg(), iEmv);
        IProto proto = d.getProtocol();
        ArrayList<Tag> tags = proto.getEmvTagList();

        EmvTags emvTags = new EmvTags();
        trans.getCard().setTags(emvTags);

        for (Tag tag : tags) {
            byte[] value = iEmv.emvGetTag(tag, true);
            if (value != null) {
                trans.getCard().getTags().add(tag, value);
            }
        }

        // output card/trans tag collection to debug
        debugEmvTags(trans);

        return 0;
    }

    private void debugEmvTags(TransRec trans) {
        for (EmvTag tag : trans.getCard().getTags().values() ) {
            if (tag != null) {
                Timber.i( "EmvTag: %s value: %s (%s)", tag.getHexTag(), Util.byteArrayToHexString(tag.getData()), tag.getName() );
            }
        }
    }

    private void setCustomTags(TransRec trans, PayCfg cfg, IP2PEMV iEmv) {

        try {
            String svcCode = P2PLib.getInstance().getIP2P().p2peGetData(SERVICE_CODE.ordinal());
            if (Util.isNullOrEmpty(svcCode)) {
                svcCode = "000";
            }
            iEmv.emvSetTag((short)service_code.value(), Util.hexStringToByteArray("0" + svcCode));
        } catch (Exception e) {
            Timber.w(e);
        }

        iEmv.emvSetTag((short) pos_entry_mode.value(), new byte[]{(byte) 0x05});
        iEmv.emvSetTag((short) mer_id.getValue(), Util.rightPadding(trans.getAudit().getMerchantId(), 15).getBytes());
        iEmv.emvSetTag((short) temr_id.getValue(), Util.rightPadding(trans.getBestTerminalId(cfg.getStid()), 8).getBytes());

        if (iEmv.emvGetTag(isssuer_code_tbl) == null) {
            iEmv.emvSetTag((short)isssuer_code_tbl.value(), Util.hexStringToByteArray("01"));
        }

        if (iEmv.emvGetTag(appl_pre_name) == null && trans.getCard().getCardName(cfg).length() > 0) {
            String cardname = Util.rightPadding(trans.getCard().getCardName(cfg), 16);
            iEmv.emvSetTag((short)appl_pre_name.value(), cardname.substring(0,16).getBytes());
        }


        if (iEmv.emvGetTag(appl_label) == null && trans.getCard().getCardName(cfg).length() > 0) {
            String cardname = Util.rightPadding(trans.getCard().getCardName(cfg), 16);
            iEmv.emvSetTag((short)appl_label.value(), cardname.substring(0,16).getBytes());
        }

    }

    private int emvGetEMVTagsAtEnd(TransRec trans, boolean saveSeparateGenAc2Values, IP2PEMV iEmv) {
        Timber.i("emvGetEMVTagsAtEnd, saveSeparate = %b", saveSeparateGenAc2Values );
        emvCacheMultitagsAtEnd(iEmv);

        byte[] cryptogramType = iEmv.emvGetTag(crypt_info_data, true);
        byte[] cryptogramValue = iEmv.emvGetTag(appl_cryptogram, true);

        if (cryptogramType != null && cryptogramType.length > 0 && cryptogramValue != null && cryptogramValue.length > 0) {

            Timber.i("cryptogram type = %x", cryptogramType[0] );
            if (!trans.isReferred()) {
                trans.getCard().setCryptogramType((int) cryptogramType[0]);
                EmvTag t = new EmvTag( saveSeparateGenAc2Values ? crypt_info_data_genAc2.value() : crypt_info_data.value(), cryptogramType);
                trans.getCard().getTags().remove( t.getTag() );
                trans.getCard().getTags().put(t.getTag(), t);
            }


            trans.getCard().setCryptogram(cryptogramValue);
            EmvTag t2 = new EmvTag(saveSeparateGenAc2Values ? appl_cryptogram_genAc2.value() : appl_cryptogram.value(),cryptogramValue);
            trans.getCard().getTags().remove(t2.getTag());
            trans.getCard().getTags().put(t2.getTag(), t2);
        }

        byte[] tvr = iEmv.emvGetTag(Tag.tvr, true);
        if (tvr != null && tvr.length > 0) {
            trans.getCard().setTvr(Util.byteArrayToHexString(tvr));
            EmvTag t = new EmvTag(Tag.tvr.value(),tvr);
            trans.getCard().getTags().remove(t.getTag());
            trans.getCard().getTags().put(t.getTag(), t);
        }

        byte[] tsi = iEmv.emvGetTag(Tag.tsi, true);
        if (tsi != null && tsi.length > 0) {
            trans.getCard().setTsi(Util.byteArrayToHexString(tsi));
            EmvTag t = new EmvTag(Tag.tsi.value(),tsi);
            trans.getCard().getTags().remove(t.getTag());
            trans.getCard().getTags().put(t.getTag(), t);
        }

        byte[] iad = iEmv.emvGetTag(issuer_app_data, true);
        if (iad != null && iad.length >= 7) {
            byte[] cvr = new byte[4];
            cvr = Arrays.copyOfRange(iad, 3, 7);
            trans.getCard().setCvr(Util.byteArrayToHexString(cvr));
            EmvTag t = new EmvTag(Tag.issuer_app_data.value(),iad);
            trans.getCard().getTags().remove(t.getTag());
            trans.getCard().getTags().put(t.getTag(), t);
        }

        byte[] issuer_script_results = iEmv.emvGetTag(Tag.issuer_script_results, true);
        if (issuer_script_results != null && issuer_script_results.length > 0) {
            EmvTag t = new EmvTag(Tag.issuer_script_results.value(),issuer_script_results);
            trans.getCard().getTags().remove(t.getTag());
            trans.getCard().getTags().put(t.getTag(),t );
        }

        byte[] arc = iEmv.emvGetTag(auth_resp_code, true);
        if( arc != null && arc.length > 0 ) {
            EmvTag t = new EmvTag(Tag.auth_resp_code.value(),arc);
            trans.getCard().getTags().put(t.getTag(),t);
        }

        // output tag collection to debug here
        debugEmvTags(trans);
        trans.getCard().setEmvCdoPerformed(iEmv.emvCdoPerformed());
        /* gets the string set so its stored in the DB */
        trans.saveEmvTagsString();

        return 0;
    }
}
