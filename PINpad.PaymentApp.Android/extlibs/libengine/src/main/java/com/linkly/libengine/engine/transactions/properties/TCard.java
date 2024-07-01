package com.linkly.libengine.engine.transactions.properties;

import static com.linkly.libconfig.cpat.CardProductCfg.DEFAULT_CARD_NAME;
import static com.linkly.libconfig.cpat.PSIIssuerDefines.PSI_ISSUER_AMEX;
import static com.linkly.libconfig.cpat.PSIIssuerDefines.PSI_ISSUER_DINERS;
import static com.linkly.libconfig.cpat.PSIIssuerDefines.PSI_ISSUER_EFTPOS;
import static com.linkly.libconfig.cpat.PSIIssuerDefines.PSI_ISSUER_JCB;
import static com.linkly.libconfig.cpat.PSIIssuerDefines.PSI_ISSUER_MASTERCARD;
import static com.linkly.libconfig.cpat.PSIIssuerDefines.PSI_ISSUER_UNIONPAY;
import static com.linkly.libconfig.cpat.PSIIssuerDefines.PSI_ISSUER_VISA;
import static com.linkly.libengine.engine.EngineManager.TransClass.NOT_SET;
import static com.linkly.libengine.engine.EngineManager.TransType.CASH;
import static com.linkly.libengine.engine.EngineManager.TransType.CASHBACK;
import static com.linkly.libengine.engine.EngineManager.TransType.CASHBACK_AUTO;
import static com.linkly.libengine.engine.EngineManager.TransType.CASH_AUTO;
import static com.linkly.libengine.engine.EngineManager.TransType.COMPLETION;
import static com.linkly.libengine.engine.EngineManager.TransType.COMPLETION_AUTO;
import static com.linkly.libengine.engine.EngineManager.TransType.PREAUTH;
import static com.linkly.libengine.engine.EngineManager.TransType.PREAUTH_AUTO;
import static com.linkly.libengine.engine.EngineManager.TransType.REFUND;
import static com.linkly.libengine.engine.EngineManager.TransType.REFUND_AUTO;
import static com.linkly.libengine.engine.EngineManager.TransType.SALE;
import static com.linkly.libengine.engine.EngineManager.TransType.SALE_AUTO;
import static com.linkly.libengine.engine.transactions.properties.TCard.CaptureMethod.CTLS;
import static com.linkly.libengine.engine.transactions.properties.TCard.CaptureMethod.CTLS_MSR;
import static com.linkly.libengine.engine.transactions.properties.TCard.CaptureMethod.ICC;
import static com.linkly.libengine.engine.transactions.properties.TCard.CaptureMethod.ICC_FALLBACK_KEYED;
import static com.linkly.libengine.engine.transactions.properties.TCard.CaptureMethod.ICC_FALLBACK_SWIPED;
import static com.linkly.libengine.engine.transactions.properties.TCard.CaptureMethod.ICC_OFFLINE;
import static com.linkly.libengine.engine.transactions.properties.TCard.CaptureMethod.MANUAL;
import static com.linkly.libengine.engine.transactions.properties.TCard.CaptureMethod.NOT_CAPTURED;
import static com.linkly.libengine.engine.transactions.properties.TCard.CaptureMethod.RRN_ENTERED;
import static com.linkly.libengine.engine.transactions.properties.TCard.CaptureMethod.SCAN_VOUCHER;
import static com.linkly.libengine.engine.transactions.properties.TCard.CaptureMethod.SWIPED;
import static com.linkly.libengine.engine.transactions.properties.TCard.CardType.NONE;
import static com.linkly.libengine.engine.transactions.properties.TCard.CvmType.NO_CVM_SET;
import static com.linkly.libsecapp.IP2PCtls.P2P_CTLS_ERROR.P2P_CTLS_OK;

import androidx.room.Ignore;

import com.google.gson.Gson;
import com.linkly.libconfig.cpat.CardProductCfg;
import com.linkly.libengine.config.PayCfg;
import com.linkly.libengine.dependencies.IDependency;
import com.linkly.libengine.engine.Engine;
import com.linkly.libengine.engine.EngineManager;
import com.linkly.libengine.engine.transactions.TransRec;
import com.linkly.libengine.overrides.CoreOverrides;
import com.linkly.libpositive.wrappers.TagDataToPOS.CardEntryModeTag;
import com.linkly.libsecapp.IP2PCtls;
import com.linkly.libsecapp.emv.EmvTags;
import com.pax.dal.entity.TrackData;

import java.util.List;

import timber.log.Timber;

@SuppressWarnings("all")
public class TCard {
    private static final int LINKLY_BIN_NUMBER_AMEX = 5;
    private static final int ANSI_TRACK2_LEN = 29;

    public CardType getCardType() {
        return this.cardType;
    }

    public CvmType getCvmType() {
        return this.cvmType;
    }

    public CaptureMethod getCaptureMethod() {
        return this.captureMethod;
    }

    public boolean isCardholderPresent() {
        return this.cardholderPresent;
    }

    public Integer getCardIndex() {
        return this.cardIndex;
    }

    public String getPsi() {
        return this.psi;
    }

    public String getExpiry() {
        return this.expiry;
    }

    public String getValidFrom() {
        return this.validFrom;
    }

    public String getServiceCode() {
        return this.serviceCode;
    }

    public String getCvv() {
        return this.cvv;
    }

    public String getCardHolderName() {
        return this.cardHolderName;
    }

    public String getAid() {
        return this.aid;
    }

    public String getTvr() {
        return this.tvr;
    }

    public String getTsi() {
        return this.tsi;
    }

    public String getCvr() {
        return this.cvr;
    }

    public String getAtc() {
        return this.atc;
    }

    public Integer getPsn() {
        return this.psn;
    }

    public Integer getCryptogramType() {
        return this.cryptogramType;
    }

    public byte[] getCryptogram() {
        return this.cryptogram;
    }

    public boolean isGenAc2Required() {
        return this.genAc2Required;
    }

    public boolean isCardDeclined() {
        return this.cardDeclined;
    }

    public String getHouseNumber() {
        return this.houseNumber;
    }

    public String getPostCodeNumber() {
        return this.postCodeNumber;
    }

    public boolean isMailOrder() {
        return this.mailOrder;
    }

    public boolean isOverTelephone() {
        return this.overTelephone;
    }

    public String getCtlsBalanceValueAOSA() {
        return this.ctlsBalanceValueAOSA;
    }

    public boolean isFaultyMsr() {
        return this.faultyMsr;
    }

    public String getMaskedPan() {
        return this.maskedPan;
    }

    public EngineManager.TransClass getOrigTransClass() {
        return this.origTransClass;
    }

    public Integer getLinklyBinNumber() {
        return this.linklyBinNumber;
    }

    public boolean isResetCvmLimit() {
        return this.resetCvmLimit;
    }

    public boolean isAppSelected() {
        return this.appSelected;
    }

    public LedStatus getLedStatus() {
        return this.ledStatus;
    }

    public TrackData getTrackData() {
        return this.trackData;
    }

    public String getPan() {
        return this.pan;
    }

    public String getTrack2() {
        return this.track2;
    }

    public EmvTags getTags() {
        return this.tags;
    }

    public EmvTags getRespTags() {
        return this.respTags;
    }

    public byte[] getIssuerAuthData() {
        return this.issuerAuthData;
    }

    public byte[] getIssuerAppData() {
        return this.issuerAppData;
    }

    public byte[] getScript71Data() {
        return this.script71Data;
    }

    public byte[] getScript72Data() {
        return this.script72Data;
    }

    public byte[] getAip() {
        return this.aip;
    }

    public byte[] getCtq() {
        return this.ctq;
    }

    public byte[] getOdaFailed() {
        return this.odaFailed;
    }

    public String getArc() {
        return this.arc;
    }

    public IP2PCtls.P2P_CTLS_ERROR getCtlsResultCode() {
        return this.ctlsResultCode;
    }

    public boolean isEnabledSC() {
        return this.enabledSC;
    }

    public boolean isPinMandatorySC() {
        return this.pinMandatorySC;
    }

    public boolean isCashAllowedSC() {
        return this.cashAllowedSC;
    }

    public boolean isCashOnlySC() {
        return this.cashOnlySC;
    }

    public boolean isOnlineSC() {
        return this.onlineSC;
    }

    public boolean isIccCardSC() {
        return this.iccCardSC;
    }

    public boolean isAtmOnlySC() {
        return this.atmOnlySC;
    }

    public boolean isManualAllowed() {
        return this.manualAllowed;
    }

    public boolean isMsrAllowed() {
        return this.msrAllowed;
    }

    public boolean isCtlsAllowed() {
        return this.ctlsAllowed;
    }

    public boolean isEmvAllowed() {
        return this.emvAllowed;
    }

    public int getEmvReadErrors() {
        return this.emvReadErrors;
    }

    public int getMsrReadErrors() {
        return this.msrReadErrors;
    }

    public boolean isShowReadError() {
        return this.showReadError;
    }

    public boolean isCtlsTryAnother() {
        return this.ctlsTryAnother;
    }

    public boolean isCtlsToICCFallbackTxn() {
        return this.ctlsToICCFallbackTxn;
    }

    public boolean isCtlsToMsrFallbackTxn() {
        return this.ctlsToMsrFallbackTxn;
    }

    public boolean isPaypassTransaction() {
        return this.paypassTransaction;
    }

    public int getPinTryCount() {
        return this.pinTryCount;
    }

    public boolean isCtlsMcrPerformed() {
        return this.ctlsMcrPerformed;
    }

    public boolean isEmvCdoPerformed() {
        return this.emvCdoPerformed;
    }

    public void setCardType(CardType cardType) {
        this.cardType = cardType;
    }

    public void setCvmType(CvmType cvmType) {
        this.cvmType = cvmType;
    }

    public void setCardholderPresent(boolean cardholderPresent) {
        this.cardholderPresent = cardholderPresent;
    }

    public void setCardIndex(Integer cardIndex) {
        this.cardIndex = cardIndex;
    }

    public void setPsi(String psi) {
        this.psi = psi;
    }

    public void setExpiry(String expiry) {
        this.expiry = expiry;
    }

    public void setValidFrom(String validFrom) {
        this.validFrom = validFrom;
    }

    public void setServiceCode(String serviceCode) {
        this.serviceCode = serviceCode;
    }

    public void setCvv(String cvv) {
        this.cvv = cvv;
    }

    public void setCardName(String cardName) {
        this.cardName = cardName;
    }

    public void setCardHolderName(String cardHolderName) {
        this.cardHolderName = cardHolderName;
    }

    public void setAid(String aid) {
        this.aid = aid;
    }

    public void setTvr(String tvr) {
        this.tvr = tvr;
    }

    public void setTsi(String tsi) {
        this.tsi = tsi;
    }

    public void setCvr(String cvr) {
        this.cvr = cvr;
    }

    public void setAtc(String atc) {
        this.atc = atc;
    }

    public void setPsn(Integer psn) {
        this.psn = psn;
    }

    public void setCryptogramType(Integer cryptogramType) {
        this.cryptogramType = cryptogramType;
    }

    public void setCryptogram(byte[] cryptogram) {
        this.cryptogram = cryptogram;
    }

    public void setGenAc2Required(boolean genAc2Required) {
        this.genAc2Required = genAc2Required;
    }

    public void setCardDeclined(boolean cardDeclined) {
        this.cardDeclined = cardDeclined;
    }

    public void setHouseNumber(String houseNumber) {
        this.houseNumber = houseNumber;
    }

    public void setPostCodeNumber(String postCodeNumber) {
        this.postCodeNumber = postCodeNumber;
    }

    public void setMailOrder(boolean mailOrder) {
        this.mailOrder = mailOrder;
    }

    public void setOverTelephone(boolean overTelephone) {
        this.overTelephone = overTelephone;
    }

    public void setCtlsBalanceValueAOSA(String ctlsBalanceValueAOSA) {
        this.ctlsBalanceValueAOSA = ctlsBalanceValueAOSA;
    }

    public void setFaultyMsr(boolean faultyMsr) {
        this.faultyMsr = faultyMsr;
    }

    public void setMaskedPan(String maskedPan) {
        this.maskedPan = maskedPan;
    }

    public void setOrigTransClass(EngineManager.TransClass origTransClass) {
        this.origTransClass = origTransClass;
    }

    public void setLinklyBinNumber(Integer linklyBinNumber) {
        this.linklyBinNumber = linklyBinNumber;
    }

    public void setResetCvmLimit(boolean resetCvmLimit) {
        this.resetCvmLimit = resetCvmLimit;
    }

    public void setAppSelected(boolean appSelected) {
        this.appSelected = appSelected;
    }

    public void setLedStatus(LedStatus ledStatus) {
        this.ledStatus = ledStatus;
    }

    public void setTrackData(TrackData trackData) {
        this.trackData = trackData;
    }

    public void setPan(String pan) {
        this.pan = pan;
    }

    public void setTrack2(String track2) {
        this.track2 = track2;
    }

    public void setTags(EmvTags tags) {
        this.tags = tags;
    }

    public void setRespTags(EmvTags respTags) {
        this.respTags = respTags;
    }

    public void setIssuerAuthData(byte[] issuerAuthData) {
        this.issuerAuthData = issuerAuthData;
    }

    public void setIssuerAppData(byte[] issuerAppData) {
        this.issuerAppData = issuerAppData;
    }

    public void setScript71Data(byte[] script71Data) {
        this.script71Data = script71Data;
    }

    public void setScript72Data(byte[] script72Data) {
        this.script72Data = script72Data;
    }

    public void setAip(byte[] aip) {
        this.aip = aip;
    }

    public void setCtq(byte[] ctq) {
        this.ctq = ctq;
    }

    public void setOdaFailed(byte[] odaFailed) {
        this.odaFailed = odaFailed;
    }

    public void setArc(String arc) {
        this.arc = arc;
    }

    public void setCtlsResultCode(IP2PCtls.P2P_CTLS_ERROR ctlsResultCode) {
        this.ctlsResultCode = ctlsResultCode;
    }

    public void setEnabledSC(boolean enabledSC) {
        this.enabledSC = enabledSC;
    }

    public void setPinMandatorySC(boolean pinMandatorySC) {
        this.pinMandatorySC = pinMandatorySC;
    }

    public void setCashAllowedSC(boolean cashAllowedSC) {
        this.cashAllowedSC = cashAllowedSC;
    }

    public void setCashOnlySC(boolean cashOnlySC) {
        this.cashOnlySC = cashOnlySC;
    }

    public void setOnlineSC(boolean onlineSC) {
        this.onlineSC = onlineSC;
    }

    public void setIccCardSC(boolean iccCardSC) {
        this.iccCardSC = iccCardSC;
    }

    public void setAtmOnlySC(boolean atmOnlySC) {
        this.atmOnlySC = atmOnlySC;
    }

    public void setManualAllowed(boolean manualAllowed) {
        this.manualAllowed = manualAllowed;
    }

    public void setMsrAllowed(boolean msrAllowed) {
        this.msrAllowed = msrAllowed;
    }

    public void setEmvAllowed(boolean emvAllowed) {
        this.emvAllowed = emvAllowed;
    }

    public void setEmvReadErrors(int emvReadErrors) {
        this.emvReadErrors = emvReadErrors;
    }

    public void setMsrReadErrors(int msrReadErrors) {
        this.msrReadErrors = msrReadErrors;
    }

    public void setShowReadError(boolean showReadError) {
        this.showReadError = showReadError;
    }

    public void setCtlsTryAnother(boolean ctlsTryAnother) {
        this.ctlsTryAnother = ctlsTryAnother;
    }

    public void setCtlsToMsrFallbackTxn(boolean ctlsToMsrFallbackTxn) {
        this.ctlsToMsrFallbackTxn = ctlsToMsrFallbackTxn;
    }

    public void setPaypassTransaction(boolean paypassTransaction) {
        this.paypassTransaction = paypassTransaction;
    }

    public void setPinTryCount(int pinTryCount) {
        this.pinTryCount = pinTryCount;
    }

    public void setCtlsMcrPerformed(boolean ctlsMcrPerformed) {
        this.ctlsMcrPerformed = ctlsMcrPerformed;
    }

    public void setEmvCdoPerformed(boolean emvCdoPerformed) {
        this.emvCdoPerformed = emvCdoPerformed;
    }

    public enum CardType {NONE, MANUAL, MSR, EMV, CTLS}

    public enum CvmType {
        NO_CVM_SET              (true,  false, false, false, false, false),
        NO_CVM                  (false, true,  false, false, false, false),
        SIG                     (false, false, true,  false, false, false),
        PLAINTEXT_OFFLINE_PIN   (false, false, false, false, true,  false),
        ENCIPHERED_OFFLINE_PIN  (false, false, false, false, true,  false),
        ENCIPHERED_ONLINE_PIN   (false, false, false, true,  false, false),
        PLAINTEXT_PIN_AND_SIG   (false, false, true,  false, true,  false),
        ENCIPHERED_PIN_AND_SIG  (false, false, true,  false, true,  false),
        CDCVM                   (false, false, false, false, false, true);

        private final boolean notSet;
        private final boolean noCvm;
        private final boolean signature;
        private final boolean onlinePin;
        private final boolean offlinePin;
        private final boolean cdCvm;

        CvmType( boolean isNotSet, boolean isNoCvm, boolean isSignature, boolean isOnlinePin, boolean isOfflinePin, boolean isCdCvm ) {
            notSet = isNotSet;
            noCvm = isNoCvm;
            signature = isSignature;
            onlinePin = isOnlinePin;
            offlinePin = isOfflinePin;
            cdCvm = isCdCvm;
        }

        public boolean isNotSet() {
            return this.notSet;
        }

        public boolean isNoCvm() {
            return this.noCvm;
        }

        public boolean isSignature() {
            return this.signature;
        }

        public boolean isOnlinePin() {
            return this.onlinePin;
        }

        public boolean isOfflinePin() {
            return this.offlinePin;
        }

        public boolean isCdCvm() {
            return this.cdCvm;
        }
    }

    public enum CaptureMethod {
        NOT_CAPTURED,
        MANUAL,
        SWIPED,
        ICC,
        ICC_FALLBACK_KEYED,
        ICC_FALLBACK_SWIPED, // fallback to swiped from insert
        ICC_OFFLINE,
        CTLS,
        CTLS_MSR,
        SCAN_VOUCHER,
        RRN_ENTERED
    }


    public enum FallbackType {FALLBACK_TO_ICC_OR_MSR, FALLBACK_TO_ICC, FALLBACK_TO_MSR}

    private static final String TAG = "TCard";
    private static byte[] s_aucVisaRid = new byte[] {(byte) 0xA0, 0x00, 0x00, 0x00, 0x03};
    private static byte[] s_aucMaestroRid = new byte[] {(byte) 0xA0, 0x00, 0x00, 0x00, 0x04, 0x30, 0x60};
    private static byte[] s_aucMastercardRid = new byte[] {(byte) 0xA0, 0x00, 0x00, 0x00, 0x04};
    private static byte[] s_aucAmexRid = new byte[] {(byte) 0xA0, 0x00, 0x00, 0x00, 0x25};
    private static String s_VisaRID = "A000000003";
    private static String s_MaestroRID = "A0000000043060";
    private static String s_MastercardRID = "A000000004";
    private static String s_AMEXRID = "A000000025";
    private static String s_JCBRID = "A000000065";
    private static String s_DINERSRID = "A000000152";
    private static String s_EFTPOSRID = "A000000384";
    private static String s_UNIONPAY_RID = "A000000333";

    private CardType cardType = NONE;
    private CvmType cvmType = NO_CVM_SET;
    private CaptureMethod captureMethod = NOT_CAPTURED;
    private boolean cardholderPresent = true;
    private Integer cardIndex = -1;
    private String psi;
    private String expiry; // YYMM format
    private String validFrom;
    private String serviceCode;
    private String cvv;
    private String cardName;
    private String cardHolderName;
    private String aid;
    private String tvr;
    private String tsi;
    private String cvr;
    private String atc;
    private Integer psn = -1; // default to -1 so we know when its not set
    private Integer cryptogramType;
    private byte[] cryptogram;
    private boolean genAc2Required = true;
    private boolean cardDeclined = true;
    private String houseNumber;
    private String postCodeNumber;
    private boolean mailOrder = false;
    private boolean overTelephone = false;
    private String ctlsBalanceValueAOSA;
    private boolean faultyMsr = false;
    private String maskedPan;
    private EngineManager.TransClass origTransClass = NOT_SET;
    private Integer linklyBinNumber;


    @Ignore
    private boolean resetCvmLimit = false;

    @Ignore
    private boolean appSelected = false;

    @Ignore
    private LedStatus ledStatus = new LedStatus();

    // specific card details
    // TODO: db work - come back to this
    @Ignore
    private TrackData trackData;
    @Ignore
    private String pan;
    @Ignore
    private String track2;

    // TODO: db work - come back to this
    @Ignore
    private EmvTags tags;

    // TODO: db work - come back to this
    @Ignore
    private EmvTags respTags;

    @Ignore
    private byte[] issuerAuthData;
    @Ignore
    private byte[] issuerAppData;
    @Ignore
    private byte[] script71Data;
    @Ignore
    private byte[] script72Data;
    @Ignore
    private byte[] aip;
    @Ignore
    private byte[] ctq;
    @Ignore
    private byte[] odaFailed;

    // saved to trans rec database as card_arc
    private String arc; // auth response code, tag 8a

    @Ignore
    private IP2PCtls.P2P_CTLS_ERROR ctlsResultCode = P2P_CTLS_OK;

    // booleans derived from the service code
    @Ignore
    private boolean enabledSC = false; // determines if the following ones are worth looking at
    @Ignore
    private boolean pinMandatorySC = false;
    @Ignore
    private boolean cashAllowedSC = false;
    @Ignore
    private boolean cashOnlySC = false;
    @Ignore
    private boolean onlineSC = false;
    @Ignore
    private boolean iccCardSC = false;
    @Ignore
    private boolean atmOnlySC = false;
    // allowed card presentation methods
    @Ignore
    private boolean manualAllowed = true;
    @Ignore
    private boolean msrAllowed = true;
    @Ignore
    private boolean ctlsAllowed = true;
    @Ignore
    private boolean emvAllowed = true;
    @Ignore
    private int emvReadErrors = 0;
    @Ignore
    private int msrReadErrors = 0;
    @Ignore
    private boolean showReadError = false;

    // fallback values
    @Ignore
    private boolean ctlsTryAnother = false;
    @Ignore
    private boolean ctlsToICCFallbackTxn = false;
    @Ignore
    private boolean ctlsToMsrFallbackTxn = false;
    @Ignore
    private boolean paypassTransaction = false;
    @Ignore
    private int pinTryCount = 0;

    // true if CTLS txn and Merchant Choice Routing MCR aka Least Cost Routing LCR was performed
    private boolean ctlsMcrPerformed = false;

    private boolean emvCdoPerformed = false;


    public TCard() {
    }

    /**
     * copies TCard, returns new instance - performs deep copy using serialization/deserialization
     *
     * @param copyFrom object to copy
     * @return copy of copyFrom
     */
    public static TCard copy( TCard copyFrom ) {
        Gson gson = new Gson();
        String serializedCopy = gson.toJson(copyFrom);
        return gson.fromJson(serializedCopy, TCard.class );
    }

    public TCard(EngineManager.TransType transType, PayCfg pCfg) {
        if (!pCfg.isContactlessSupported() ||
                (transType != SALE && transType != SALE_AUTO &&
                        transType != CASH && transType != CASH_AUTO &&
                        transType != CASHBACK && transType != CASHBACK_AUTO &&
                        transType != REFUND && transType != REFUND_AUTO &&
                        transType != PREAUTH && transType != PREAUTH_AUTO &&
                        transType != COMPLETION && transType != COMPLETION_AUTO
                        )) {
            setCtlsAllowed(false);
        }
        if (!pCfg.isEmvSupported()) {
            setEmvAllowed(false);
        }

        setManualAllowed(isManualAllowedForTrans(transType, pCfg));
    }

    public static boolean isManualAllowedForTrans(EngineManager.TransType transType, PayCfg pCfg) {
        if (pCfg.getManualEntry() != null) {
            switch (transType) {
                case BALANCE:
                    return pCfg.getManualEntry().isBalance();
                case SALE:
                case SALE_AUTO:
                    return pCfg.getManualEntry().isSale();
                case OFFLINECASH:
                case OFFLINESALE:
                    return pCfg.getManualEntry().isForced();
                case REFUND:
                case REFUND_AUTO:
                    return pCfg.getManualEntry().isRefund() || pCfg.getManualEntry().isOfflineRefund();
                case MANUAL_REVERSAL:
                    return pCfg.getManualEntry().isReversal();
                case CASH:
                case CASH_AUTO:
                    return pCfg.getManualEntry().isCash();
                case PREAUTH:
                case PREAUTH_AUTO:
                    return pCfg.getManualEntry().isPreauth();
            }
        }
        return true;
    }


    public String getTrack2WithSentinels(boolean addLRC) {

        String track2 = getTrack2ForMessage();

        if (track2 == null)
            return null;

        if (track2.contains("?") || track2.contains(";"))
            return track2;

        if (captureMethod == ICC_FALLBACK_SWIPED ||
            captureMethod == SWIPED ||
            captureMethod == CTLS_MSR) {

            String withSentinels = ";";
            withSentinels += track2;
            withSentinels += "?";
            if (addLRC) {
                char ucLRC = 0;
                for (int ii = 0; ii < withSentinels.length(); ii++) {
                    ucLRC ^= (withSentinels.charAt(ii) & 0x0F);
                }
                ucLRC += 0x30;
                withSentinels += ucLRC;
            }
            return withSentinels;
        }

        return track2;

    }


    public void invalidateCard() {
        trackData = null;
        cardIndex = -1;
        pan = null;
        maskedPan = null;
        track2 = null;
        expiry = null;

    }

    public void setCtlsToICCFallbackTxn(boolean ctlsToICCFallbackTxn) {
        setCtlsAllowed(!ctlsToICCFallbackTxn);
        this.ctlsToICCFallbackTxn = ctlsToICCFallbackTxn;

    }

    public void setCtlsAllowed(boolean ctlsAllowed) {
        ledStatus.setCTLSEnabled(ctlsAllowed);
        this.ctlsAllowed = ctlsAllowed;
    }

    public void setCaptureMethod(CaptureMethod captureMethod) {

        /* don't set it to plain old swiped if it is already a fallback to swiped transaction */
        if (captureMethod == SWIPED && this.captureMethod == ICC_FALLBACK_SWIPED) {
            return;
        }

        /* to avoid resetting as we start the transactoin again */
        if (captureMethod == NOT_CAPTURED && this.captureMethod == ICC_FALLBACK_SWIPED) {
            return;
        }

        this.captureMethod = captureMethod;
    }

    public void updateTrackData(IDependency d, TrackData trackData, TransRec trans) {

        this.trackData = trackData;
        updateTrack2(d, trackData.getTrack2(), trans);
    }

    public void updateTrack2(IDependency d, String track2New, TransRec trans) {

        if (track2New != null) {

            this.track2 = track2New;
            this.track2 = this.track2.replaceAll("f", "");
            this.track2 = this.track2.replaceAll("F", "");
            this.track2 = this.track2.replaceAll("d", "=");
            this.track2 = this.track2.replaceAll("D", "=");

            int index = d.getConfig().getBinRangesCfg().getCardsCfgIndex(d.getPayCfg(), track2);
            setCardIndex(index);

            String[] parts = track2.split("=");
            if (parts.length >= 1 && parts[0] != null) {
                setPan(parts[0]);
                trans.getCard().setMaskedPan(trans.getMaskedPan(TransRec.MaskType.MERCH_MASK, d.getPayCfg()));
            }
            if (parts.length >= 2 && parts[1] != null) {
                if (parts[1].length() >= 4) {
                    expiry = parts[1].substring(0, 4);
                }
                if (parts[1].length() >= 7) {
                    serviceCode = parts[1].substring(4, 7);
                    processServiceCodes(d);
                }
            }
        }
    }

    /* special cases for amex */
    public String getTrack2ForMessage() {
        return getTrack2();
    }

    public boolean isManual() {
        return captureMethod == MANUAL || captureMethod == RRN_ENTERED;
    }

    public boolean isSwiped() {
        return captureMethod == SWIPED || captureMethod == ICC_FALLBACK_SWIPED;
    }

    public boolean isIccFallback() {
        return captureMethod == ICC_FALLBACK_KEYED || captureMethod == ICC_FALLBACK_SWIPED;
    }

    public boolean isIccOrCtlsCaptured() {
        return (captureMethod == ICC ||
                captureMethod == ICC_FALLBACK_KEYED ||
                captureMethod == ICC_FALLBACK_SWIPED ||
                captureMethod == ICC_OFFLINE ||
                captureMethod == CTLS ||
                captureMethod == CTLS_MSR);
    }

    public boolean isCtlsMsr() {
        return captureMethod == CTLS_MSR;
    }

    public boolean isCtlsCaptured() {
        return captureMethod == CTLS || captureMethod == CTLS_MSR;
    }

    public boolean isIccCaptured() {
        return captureMethod == ICC || captureMethod == ICC_OFFLINE;
    }

    public boolean isIccFallbackCaptured() {
        return captureMethod == ICC_FALLBACK_SWIPED || captureMethod == ICC_FALLBACK_KEYED;
    }

    public boolean isScanVoucher() {
        return captureMethod == SCAN_VOUCHER;
    }

    public boolean isCupCard(IDependency d) {
        // We do not support CUP. Issue is that a bunch of other logic uses this.
        return false;
    }

    public boolean isVisaCard() {
        return getCardIssuer() == CardIssuer.VISA;
    }

    public boolean isAmexCard(PayCfg config) {
        return getBinNumber(config) == LINKLY_BIN_NUMBER_AMEX;
    }

    public boolean isMasterCard() {
        return getCardIssuer() == CardIssuer.MASTERCARD;
    }

    public boolean isMaestroCard() {
        return getCardIssuer() == CardIssuer.MAESTRO;
    }

    public boolean isEftposCard() {
        return getCardIssuer() == CardIssuer.EFTPOS;
    }

    public CardProductCfg getCardsConfig(PayCfg payCfg) throws NullPointerException {
        if (cardIndex >= 0) {
            return payCfg.getCards().get(cardIndex);
        }

        return payCfg.getDefaultCardProduct();
    }

    public boolean processServiceCodes(IDependency d) {

        boolean isBadDigit = false;

        if (CoreOverrides.get().isIgnoreServiceCodes()) {
            onlineSC = false;
            cashAllowedSC = true;
            pinMandatorySC = false;
            cashOnlySC = false;
            return true;
        }

        if (captureMethod == CTLS_MSR) {
            return false;
        }

        if (serviceCode == null || serviceCode.length() <= 0) {
            onlineSC = true;
            return false;
        }

        enabledSC = true;

        if (isCupCard(d)) {
            Timber.i( "CUP card so dont check service codes");
            // WC assuming cash is allowed and pin optional for amex
            cashAllowedSC = false;
            pinMandatorySC = true;
            onlineSC = true;
            cashOnlySC = false;

            switch (serviceCode.charAt(0)) {
                case '2':
                    iccCardSC = true;
                    break;
                default:
                    break;
            }
        } else if (isAmexCard(d.getPayCfg()) && getTrack2().length() == ANSI_TRACK2_LEN ) {
            Timber.i( "AMEX Ansi card so dont check service codes");
            // WC assuming cash is allowed and pin optional for amex
            cashAllowedSC = true;
            pinMandatorySC = false;

        } else {
            Timber.i( "Check the service codes " + serviceCode);
            switch (serviceCode.charAt(0)) {
                case '1':
                    break;

                case '2':
                case '6':
                    iccCardSC = true;
                    break;

                case '5': /* Fall through */
                    if (!checkCountryCard()) {
                        Timber.i( "Return false");
                        return false;
                    }
                    break;

                case '7': /* Fall through */
                case '9': /* Fall through */
                default:
                    isBadDigit = true;
                    break;
            }

            switch (serviceCode.charAt(1)) {
                case '0':
                    break;

                case '2':
                    onlineSC = true;
                    break;

                default:
                    isBadDigit = true;
                    break;
            }

            switch (serviceCode.charAt(2)) {
                case '0':         // no restrictions and pin required
                    cashAllowedSC = true;
                    pinMandatorySC = true;
                    break;
                case '1':         // no restrictions
                    cashAllowedSC = true;
                    pinMandatorySC = false;
                    break;
                case '2':         // goods and services only (no cash)
                    cashAllowedSC = false;
                    pinMandatorySC = false;
                    break;
                case '3':         // ATM only
                    atmOnlySC = true;
                    break;
                case '4':         // cash only
                    cashAllowedSC = true;
                    cashOnlySC = true;
                    isBadDigit = true;
                    break;
                case '5':         // goods and services only (no cash) and pin required
                    cashAllowedSC = false;
                    pinMandatorySC = true;
                    break;
                case '6':         // no restrictions and require pin where possible
                    cashAllowedSC = true;
                    pinMandatorySC = true;
                    break;
                case '7':         // goods and services only (no cash) and require pin where possible
                    cashAllowedSC = false;
                    pinMandatorySC = true;
                    break;
                default:
                    isBadDigit = true;
                    break;
            }

            if (isBadDigit) {
                Timber.i( "bad service code digit, setting online");
                onlineSC = true;
            }
        }
        return true;
    }

    private boolean checkCountryCard() {
        // TODO implement
        return false;
    }

    public boolean isSignatureVerificationRequired() {
        return cvmType == CvmType.SIG || cvmType == CvmType.PLAINTEXT_PIN_AND_SIG || cvmType == CvmType.ENCIPHERED_PIN_AND_SIG;
    }

    public boolean isPinVerificationRequired() {
        return isOnlinePinVerificationRequired() || isOfflinePinVerified();
    }

    public boolean isOnlinePinVerificationRequired() {
        //TODO: Is PLAINTEXT_PIN_AND_SIG online or offline ENCIPHERED_ONLINE_PIN?
        return cvmType == CvmType.ENCIPHERED_ONLINE_PIN;
    }

    public boolean isOfflinePinVerified() {
        return cvmType == CvmType.PLAINTEXT_OFFLINE_PIN || cvmType == CvmType.ENCIPHERED_OFFLINE_PIN;
    }

    public boolean isDeviceVerified() {
        return cvmType == CvmType.CDCVM;
    }

    public boolean isPinAndSigVerificationRequired() {
        return cvmType == CvmType.PLAINTEXT_PIN_AND_SIG || cvmType == CvmType.ENCIPHERED_PIN_AND_SIG;
    }

    public boolean isNotVerified() {
        return cvmType == CvmType.NO_CVM || cvmType == CvmType.NO_CVM_SET;
    }

    public String getCardName() {
        return cardName;
    }

    public String getCardName(PayCfg config) {

        // if card name not already set, or was previously set to 'unknown'
        if (cardName == null || cardName.length() <= 0 || cardName.equals(DEFAULT_CARD_NAME)) {
            try {
                CardProductCfg cardsConfig = this.getCardsConfig(config);
                cardName = cardsConfig.getName();
            } catch (NullPointerException e) {
                cardName = "UNAVAILABLE";
            }

        }
        return cardName;
    }

    public String getCryptogramTypeCode() {
        if (cryptogramType == null) {
            return "AAC";
        }

        switch ((byte) cryptogramType.intValue() & 0xC0) {
            case 0x40:
                return "TC";
            case 0xC0:
                return "AAR";
            case 0x80:
                return "ARQC";
            default:
                return "AAC";
        }
    }

    public CardIssuer getCardIssuer() {
        CardIssuer issuer = CardIssuer.UNKNOWN;

        Timber.d("aid: %s", aid);
        if (aid != null && aid.length() > 0) {
            if (isCardVisa(aid)) {
                issuer = CardIssuer.VISA;
            } else if (isCardMasterCard(aid)) {
                issuer = CardIssuer.MASTERCARD;
            } else if (isCardMaestro(aid)) {
                issuer = CardIssuer.MAESTRO;
            } else if (isCardAmex(aid)) {
                issuer = CardIssuer.AMEX;
            } else if (isCardJcb(aid)) {
                issuer = CardIssuer.JCB;
            } else if (isCardDiners(aid)) {
                issuer = CardIssuer.DINERS;
            } else if (isCardEftpos(aid)) {
                issuer = CardIssuer.EFTPOS;
            } else if (isCardUnionpay(aid)) {
                issuer = CardIssuer.UNIONPAY;
            }

        } else if (psi != null && psi.length() > 0) {
            Timber.d("Psi: %s", psi);
            switch(psi.toUpperCase()) {
                case PSI_ISSUER_VISA: issuer = CardIssuer.VISA;       break;
                case PSI_ISSUER_AMEX: issuer = CardIssuer.AMEX;       break;
                case PSI_ISSUER_MASTERCARD: issuer = CardIssuer.MASTERCARD; break;
                case PSI_ISSUER_DINERS: issuer = CardIssuer.DINERS;     break;
                case PSI_ISSUER_JCB: issuer = CardIssuer.JCB;        break;
                case PSI_ISSUER_UNIONPAY: issuer = CardIssuer.UNIONPAY;   break;
                case PSI_ISSUER_EFTPOS: issuer = CardIssuer.EFTPOS;     break;
            }
        }

        return issuer;
    }

    public boolean isCardIssuer(String aid, String aidCompare) {
        if (aid == null || aid.length() <= 0 || aidCompare == null || (aidCompare.length() > aid.length())) {
            return false;
        }

        Timber.d("%s - %s", aid, aidCompare);

        return aid.substring(0, aidCompare.length()).equalsIgnoreCase(aidCompare);
    }

    public boolean isCardVisa(String aid) {
        return isCardIssuer(aid, s_VisaRID);
    }

    public boolean isCardMaestro(String aid) {
        return isCardIssuer(aid, s_MaestroRID);
    }

    public boolean isCardMasterCard(String aid) {
        return isCardIssuer(aid, s_MastercardRID);
    }

    public boolean isCardAmex(String aid) {
        return isCardIssuer(aid, s_AMEXRID);
    }

    public boolean isCardJcb(String aid) {
        return isCardIssuer(aid, s_JCBRID);
    }

    public boolean isCardDiners(String aid) {
        return isCardIssuer(aid, s_DINERSRID);
    }

    public boolean isCardEftpos(String aid) {
        return isCardIssuer(aid, s_EFTPOSRID);
    }

    public boolean isCardUnionpay(String aid) {
        return isCardIssuer(aid, s_UNIONPAY_RID);
    }

    public int getBinNumber(PayCfg config) {

        return getCardsConfig(config).getBinNumber(); // If card is not found will return 0
    }

    /***
     * Method to get card type equivalent enum CardEntryModeTag, which can be used for PAD tag CEM
     * @return  CardEntryModeTag
     */
    public CardEntryModeTag getCardEntryMode() {

        CardEntryModeTag cardEntryModeTag = CardEntryModeTag.NONE;
        switch (cardType) {
            case MANUAL:
                cardEntryModeTag = CardEntryModeTag.KEYED;
                break;
            case MSR:
                cardEntryModeTag = CardEntryModeTag.SWIPE;
                break;
            case EMV:
                cardEntryModeTag = CardEntryModeTag.CHIP_CARD;
                break;
            case CTLS:
                cardEntryModeTag = CardEntryModeTag.CONTACTLESS;
                break;
            case NONE:
            default:
                break;
        }
        return cardEntryModeTag;
    }

    public enum CardIssuer {
        UNKNOWN(""),
        VISA("Visa"),
        MASTERCARD("Mastercard"),
        AMEX("AMEX"),
        MAESTRO("MAESTRO"),
        JCB("JCB"),
        DINERS("DINERS"),
        EFTPOS("EFTPOS"),
        UNIONPAY("UNIONPAY");
        private String displayName;

        CardIssuer(String name) {
            displayName = name;
        }

        public String getDisplayName() {
            return this.displayName;
        }
    }
}



