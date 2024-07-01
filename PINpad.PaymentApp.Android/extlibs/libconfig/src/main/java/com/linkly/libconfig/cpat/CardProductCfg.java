package com.linkly.libconfig.cpat;

public class CardProductCfg {
    public static final String DEFAULT_CARD_NAME = "UNKNOWN CARD TYPE";
    //Constants used to for displaying allowed account types
    public static final int ACC_DEFAULT                 = 0; //000
    public static final int ACC_CHEQUE                  = 1; //001
    public static final int ACC_SAVINGS                 = 2; //010
    public static final int ACC_CREDIT                  = 4; //100

    private String name;
    private String iinRange;
    private String testIinRange;
    private String panLength;
    private boolean parent;
    private String psi;
    private String customerPanMask;
    private String merchantPanMask;
    private boolean luhnCheck;
    private boolean serviceCodeCheck;
    private boolean printCardholderName;
    private boolean forceOffline;
    private boolean refundOnline;
    private boolean completionOnline;
    private boolean forceReferral;
    private int authorizationBits;
    private int accountSelection;
    private String schemeId;
    private int binNumber; // Linkly terminal document spec. Card Bin Table.

    private CardLimits limits;
    private ServicesAllowed servicesAllowed;
    private OnlinePin onlinePin;
    private PasswordRequired passwordRequired;
    private String schemeLabel;
    private boolean disabled;
    private boolean deferredAuthEnabled;

    private boolean productLevelBlocking;
    private boolean rejectCtls;
    private boolean rejectEmv;
    private boolean forceSign;

    public String getName() {
        return this.name;
    }

    public String getIinRange() {
        return this.iinRange;
    }

    public String getTestIinRange() {
        return this.testIinRange;
    }

    public String getPanLength() {
        return this.panLength;
    }

    public boolean isParent() {
        return this.parent;
    }

    public String getPsi() {
        return this.psi;
    }

    public String getCustomerPanMask() {
        return this.customerPanMask;
    }

    public String getMerchantPanMask() {
        return this.merchantPanMask;
    }

    public boolean isLuhnCheck() {
        return this.luhnCheck;
    }

    public boolean isServiceCodeCheck() {
        return this.serviceCodeCheck;
    }

    public boolean isPrintCardholderName() {
        return this.printCardholderName;
    }

    public boolean isForceOffline() {
        return this.forceOffline;
    }

    public boolean isRefundOnline() {
        return this.refundOnline;
    }

    public boolean isCompletionOnline() {
        return this.completionOnline;
    }

    public boolean isForceReferral() {
        return this.forceReferral;
    }

    public int getAuthorizationBits() {
        return this.authorizationBits;
    }

    public int getAccountSelection() {
        return this.accountSelection;
    }

    public String getSchemeId() {
        return this.schemeId;
    }

    public int getBinNumber() {
        return this.binNumber;
    }

    public CardLimits getLimits() {
        return this.limits;
    }

    public ServicesAllowed getServicesAllowed() {
        return this.servicesAllowed;
    }

    public OnlinePin getOnlinePin() {
        return this.onlinePin;
    }

    public PasswordRequired getPasswordRequired() {
        return this.passwordRequired;
    }

    public String getSchemeLabel() {
        return this.schemeLabel;
    }

    public boolean isDisabled() {
        return this.disabled;
    }

    public boolean isDeferredAuthEnabled() {
        return this.deferredAuthEnabled;
    }

    public boolean isProductLevelBlocking() {
        return this.productLevelBlocking;
    }

    public boolean isRejectCtls() {
        return this.rejectCtls;
    }

    public boolean isRejectEmv() {
        return this.rejectEmv;
    }

    public boolean isForceSign() {
        return this.forceSign;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setIinRange(String iinRange) {
        this.iinRange = iinRange;
    }

    public void setTestIinRange(String testIinRange) {
        this.testIinRange = testIinRange;
    }

    public void setPanLength(String panLength) {
        this.panLength = panLength;
    }

    public void setParent(boolean parent) {
        this.parent = parent;
    }

    public void setPsi(String psi) {
        this.psi = psi;
    }

    public void setCustomerPanMask(String customerPanMask) {
        this.customerPanMask = customerPanMask;
    }

    public void setMerchantPanMask(String merchantPanMask) {
        this.merchantPanMask = merchantPanMask;
    }

    public void setLuhnCheck(boolean luhnCheck) {
        this.luhnCheck = luhnCheck;
    }

    public void setServiceCodeCheck(boolean serviceCodeCheck) {
        this.serviceCodeCheck = serviceCodeCheck;
    }

    public void setPrintCardholderName(boolean printCardholderName) {
        this.printCardholderName = printCardholderName;
    }

    public void setForceOffline(boolean forceOffline) {
        this.forceOffline = forceOffline;
    }

    public void setRefundOnline(boolean refundOnline) {
        this.refundOnline = refundOnline;
    }

    public void setCompletionOnline(boolean completionOnline) {
        this.completionOnline = completionOnline;
    }

    public void setForceReferral(boolean forceReferral) {
        this.forceReferral = forceReferral;
    }

    public void setAuthorizationBits(int authorizationBits) {
        this.authorizationBits = authorizationBits;
    }

    public void setAccountSelection(int accountSelection) {
        this.accountSelection = accountSelection;
    }

    public void setSchemeId(String schemeId) {
        this.schemeId = schemeId;
    }

    public void setBinNumber(int binNumber) {
        this.binNumber = binNumber;
    }

    public void setLimits(CardLimits limits) {
        this.limits = limits;
    }

    public void setServicesAllowed(ServicesAllowed servicesAllowed) {
        this.servicesAllowed = servicesAllowed;
    }

    public void setOnlinePin(OnlinePin onlinePin) {
        this.onlinePin = onlinePin;
    }

    public void setPasswordRequired(PasswordRequired passwordRequired) {
        this.passwordRequired = passwordRequired;
    }

    public void setSchemeLabel(String schemeLabel) {
        this.schemeLabel = schemeLabel;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    public void setDeferredAuthEnabled(boolean deferredAuthEnabled) {
        this.deferredAuthEnabled = deferredAuthEnabled;
    }

    public void setProductLevelBlocking(boolean productLevelBlocking) {
        this.productLevelBlocking = productLevelBlocking;
    }

    public void setRejectCtls(boolean rejectCtls) {
        this.rejectCtls = rejectCtls;
    }

    public void setRejectEmv(boolean rejectEmv) {
        this.rejectEmv = rejectEmv;
    }

    public void setForceSign(boolean forceSign) {
        this.forceSign = forceSign;
    }

    public static class PasswordRequired {
        private boolean moto;
        private boolean deposit;
        private boolean depositOffline;
        private boolean sale;
        private boolean refund;
        private boolean offlineRefund;
        private boolean cash;
        private boolean reversal;
        private boolean cashback;
        private boolean preauth;
        private boolean completion;
        private boolean balance;
        private boolean forced;
        private boolean pinChange;
        private boolean reprint;
        private boolean offlineCash;

        public boolean isMoto() {
            return this.moto;
        }

        public boolean isDeposit() {
            return this.deposit;
        }

        public boolean isDepositOffline() {
            return this.depositOffline;
        }

        public boolean isSale() {
            return this.sale;
        }

        public boolean isRefund() {
            return this.refund;
        }

        public boolean isOfflineRefund() {
            return this.offlineRefund;
        }

        public boolean isCash() {
            return this.cash;
        }

        public boolean isReversal() {
            return this.reversal;
        }

        public boolean isCashback() {
            return this.cashback;
        }

        public boolean isPreauth() {
            return this.preauth;
        }

        public boolean isCompletion() {
            return this.completion;
        }

        public boolean isBalance() {
            return this.balance;
        }

        public boolean isForced() {
            return this.forced;
        }

        public boolean isPinChange() {
            return this.pinChange;
        }

        public boolean isReprint() {
            return this.reprint;
        }

        public boolean isOfflineCash() {
            return this.offlineCash;
        }

        public void setMoto(boolean moto) {
            this.moto = moto;
        }

        public void setDeposit(boolean deposit) {
            this.deposit = deposit;
        }

        public void setDepositOffline(boolean depositOffline) {
            this.depositOffline = depositOffline;
        }

        public void setSale(boolean sale) {
            this.sale = sale;
        }

        public void setRefund(boolean refund) {
            this.refund = refund;
        }

        public void setOfflineRefund(boolean offlineRefund) {
            this.offlineRefund = offlineRefund;
        }

        public void setCash(boolean cash) {
            this.cash = cash;
        }

        public void setReversal(boolean reversal) {
            this.reversal = reversal;
        }

        public void setCashback(boolean cashback) {
            this.cashback = cashback;
        }

        public void setPreauth(boolean preauth) {
            this.preauth = preauth;
        }

        public void setCompletion(boolean completion) {
            this.completion = completion;
        }

        public void setBalance(boolean balance) {
            this.balance = balance;
        }

        public void setForced(boolean forced) {
            this.forced = forced;
        }

        public void setPinChange(boolean pinChange) {
            this.pinChange = pinChange;
        }

        public void setReprint(boolean reprint) {
            this.reprint = reprint;
        }

        public void setOfflineCash(boolean offlineCash) {
            this.offlineCash = offlineCash;
        }
    }

    public static class OnlinePin {
        private String deposit;
        private String sale;
        private String cash;
        private String cashback;
        private String preauth;
        private String balance;
        private String pinChange;
        private String refund;
        private String smallValue;

        public String getDeposit() {
            return this.deposit;
        }

        public String getSale() {
            return this.sale;
        }

        public String getCash() {
            return this.cash;
        }

        public String getCashback() {
            return this.cashback;
        }

        public String getPreauth() {
            return this.preauth;
        }

        public String getBalance() {
            return this.balance;
        }

        public String getPinChange() {
            return this.pinChange;
        }

        public String getRefund() {
            return this.refund;
        }

        public String getSmallValue() {
            return this.smallValue;
        }

        public void setDeposit(String deposit) {
            this.deposit = deposit;
        }

        public void setSale(String sale) {
            this.sale = sale;
        }

        public void setCash(String cash) {
            this.cash = cash;
        }

        public void setCashback(String cashback) {
            this.cashback = cashback;
        }

        public void setPreauth(String preauth) {
            this.preauth = preauth;
        }

        public void setBalance(String balance) {
            this.balance = balance;
        }

        public void setPinChange(String pinChange) {
            this.pinChange = pinChange;
        }

        public void setRefund(String refund) {
            this.refund = refund;
        }

        public void setSmallValue(String smallValue) {
            this.smallValue = smallValue;
        }
    }

    public static class ServicesAllowed {
        private boolean deposit;
        private boolean depositOffline;
        private boolean sale;
        private boolean refund;
        private boolean offlineRefund;
        private boolean cash;
        private boolean reversal;
        private boolean cashback;
        private boolean preauth;
        private boolean balance;
        private boolean forced;
        private boolean moto;
        private boolean pinChange;
        private boolean offlineCash;

        public boolean isDeposit() {
            return this.deposit;
        }

        public boolean isDepositOffline() {
            return this.depositOffline;
        }

        public boolean isSale() {
            return this.sale;
        }

        public boolean isRefund() {
            return this.refund;
        }

        public boolean isOfflineRefund() {
            return this.offlineRefund;
        }

        public boolean isCash() {
            return this.cash;
        }

        public boolean isReversal() {
            return this.reversal;
        }

        public boolean isCashback() {
            return this.cashback;
        }

        public boolean isPreauth() {
            return this.preauth;
        }

        public boolean isBalance() {
            return this.balance;
        }

        public boolean isForced() {
            return this.forced;
        }

        public boolean isMoto() {
            return this.moto;
        }

        public boolean isPinChange() {
            return this.pinChange;
        }

        public boolean isOfflineCash() {
            return this.offlineCash;
        }

        public void setDeposit(boolean deposit) {
            this.deposit = deposit;
        }

        public void setDepositOffline(boolean depositOffline) {
            this.depositOffline = depositOffline;
        }

        public void setSale(boolean sale) {
            this.sale = sale;
        }

        public void setRefund(boolean refund) {
            this.refund = refund;
        }

        public void setOfflineRefund(boolean offlineRefund) {
            this.offlineRefund = offlineRefund;
        }

        public void setCash(boolean cash) {
            this.cash = cash;
        }

        public void setReversal(boolean reversal) {
            this.reversal = reversal;
        }

        public void setCashback(boolean cashback) {
            this.cashback = cashback;
        }

        public void setPreauth(boolean preauth) {
            this.preauth = preauth;
        }

        public void setBalance(boolean balance) {
            this.balance = balance;
        }

        public void setForced(boolean forced) {
            this.forced = forced;
        }

        public void setMoto(boolean moto) {
            this.moto = moto;
        }

        public void setPinChange(boolean pinChange) {
            this.pinChange = pinChange;
        }

        public void setOfflineCash(boolean offlineCash) {
            this.offlineCash = offlineCash;
        }
    }

    public static class CardLimits {
        private int min;
        private int cashFloor;
        private int commFailCashFloor;
        private int cashMax;
        private int cashbackMax;
        private int floor;
        private int commFailFloor;
        private int mangerAuthMax;
        private int telAuthMax;
        private int telPinAuthMax;
        private int max;
        private int offlineRefundMax;
        private int smallValueLimitDollars;

        public int getMin() {
            return this.min;
        }

        public int getCashFloor() {
            return this.cashFloor;
        }

        public int getCommFailCashFloor() {
            return this.commFailCashFloor;
        }

        public int getCashMax() {
            return this.cashMax;
        }

        public int getCashbackMax() {
            return this.cashbackMax;
        }

        public int getFloor() {
            return this.floor;
        }

        public int getCommFailFloor() {
            return this.commFailFloor;
        }

        public int getMangerAuthMax() {
            return this.mangerAuthMax;
        }

        public int getTelAuthMax() {
            return this.telAuthMax;
        }

        public int getTelPinAuthMax() {
            return this.telPinAuthMax;
        }

        public int getMax() {
            return this.max;
        }

        public int getOfflineRefundMax() {
            return this.offlineRefundMax;
        }

        public int getSmallValueLimitDollars() {
            return this.smallValueLimitDollars;
        }

        public void setMin(int min) {
            this.min = min;
        }

        public void setCashFloor(int cashFloor) {
            this.cashFloor = cashFloor;
        }

        public void setCommFailCashFloor(int commFailCashFloor) {
            this.commFailCashFloor = commFailCashFloor;
        }

        public void setCashMax(int cashMax) {
            this.cashMax = cashMax;
        }

        public void setCashbackMax(int cashbackMax) {
            this.cashbackMax = cashbackMax;
        }

        public void setFloor(int floor) {
            this.floor = floor;
        }

        public void setCommFailFloor(int commFailFloor) {
            this.commFailFloor = commFailFloor;
        }

        public void setMangerAuthMax(int mangerAuthMax) {
            this.mangerAuthMax = mangerAuthMax;
        }

        public void setTelAuthMax(int telAuthMax) {
            this.telAuthMax = telAuthMax;
        }

        public void setTelPinAuthMax(int telPinAuthMax) {
            this.telPinAuthMax = telPinAuthMax;
        }

        public void setMax(int max) {
            this.max = max;
        }

        public void setOfflineRefundMax(int offlineRefundMax) {
            this.offlineRefundMax = offlineRefundMax;
        }

        public void setSmallValueLimitDollars(int smallValueLimitDollars) {
            this.smallValueLimitDollars = smallValueLimitDollars;
        }
    }

    public static CardProductCfg getDefaultConfig() {
        CardProductCfg defaultCardProduct = new CardProductCfg();

        defaultCardProduct.name = DEFAULT_CARD_NAME;
        defaultCardProduct.iinRange = "0000-0001";
        defaultCardProduct.panLength = "13,14,15,16,17,18,19,";
        defaultCardProduct.parent = false;
        defaultCardProduct.psi = "";
        defaultCardProduct.customerPanMask = "001";
        defaultCardProduct.merchantPanMask = "101";
        defaultCardProduct.luhnCheck = false;
        defaultCardProduct.serviceCodeCheck = false;
        defaultCardProduct.printCardholderName = false;
        defaultCardProduct.forceOffline = false;
        defaultCardProduct.refundOnline = true;
        defaultCardProduct.forceReferral = false;
        defaultCardProduct.authorizationBits = 5;
        defaultCardProduct.accountSelection = 0;

        defaultCardProduct.limits = new CardProductCfg.CardLimits();
        defaultCardProduct.servicesAllowed = new CardProductCfg.ServicesAllowed();
        defaultCardProduct.passwordRequired = new CardProductCfg.PasswordRequired();
        defaultCardProduct.onlinePin = new CardProductCfg.OnlinePin();

        defaultCardProduct.onlinePin.deposit = "N";
        defaultCardProduct.onlinePin.sale = "N";
        defaultCardProduct.onlinePin.cash = "N";
        defaultCardProduct.onlinePin.cashback = "N";
        defaultCardProduct.onlinePin.preauth = "N";
        defaultCardProduct.onlinePin.balance = "N";
        defaultCardProduct.onlinePin.pinChange = "N";
        defaultCardProduct.onlinePin.refund = "N";

        defaultCardProduct.binNumber = 0; // Linkly Terminal Devlopment Spec 0 is unknown
        defaultCardProduct.productLevelBlocking = false;
        defaultCardProduct.rejectCtls = false;
        defaultCardProduct.rejectEmv = false;

        //Services Allowed
        defaultCardProduct.servicesAllowed.sale = true;
        defaultCardProduct.servicesAllowed.preauth = true;
        defaultCardProduct.servicesAllowed.cash = true;
        defaultCardProduct.servicesAllowed.reversal = true;
        defaultCardProduct.servicesAllowed.moto = true;
        defaultCardProduct.servicesAllowed.cashback = true;
        defaultCardProduct.limits.max = 100;
        defaultCardProduct.limits.cashMax = 100;
        defaultCardProduct.deferredAuthEnabled = false;


        return defaultCardProduct;
    }
}
