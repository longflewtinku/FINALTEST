package com.linkly.payment.customer.Suncorp;

import static com.linkly.libengine.engine.customers.ICustomer.PCI_FORMAT.POST_TRANS;
import static com.linkly.libengine.engine.protocol.IProto.TaskProtocolType.AS2805_SUNCORP;

import com.linkly.libconfig.MenusCfg;
import com.linkly.libconfig.ProfileCfg;
import com.linkly.libconfig.cpat.CardProductCfg;
import com.linkly.libengine.action.check.CheckPassword;
import com.linkly.libengine.config.Config;
import com.linkly.libengine.config.IConfig;
import com.linkly.libengine.config.PayCfg;
import com.linkly.libengine.dependencies.IDependency;
import com.linkly.libengine.engine.Engine;
import com.linkly.libengine.engine.EngineManager;
import com.linkly.libengine.engine.customers.ICustomer;
import com.linkly.libengine.engine.protocol.IProto;
import com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583Rev93;
import com.linkly.libengine.engine.protocol.iso8583.openisoj.IsoUtils;
import com.linkly.libengine.engine.transactions.TransRec;
import com.linkly.libengine.engine.transactions.properties.TAudit;
import com.linkly.libengine.engine.transactions.properties.TProtocol;
import com.linkly.libengine.printing.IReceipt;
import com.linkly.libengine.users.User;
import com.linkly.libsecapp.IP2PSec;
import com.linkly.libsecapp.P2PLib;
import com.linkly.payment.customer.ICustomerMenu;
import com.linkly.payment.menus.MenuItems;
import com.linkly.payment.printing.receipts.common.AboutAppReceipt;
import com.linkly.payment.printing.receipts.common.DepositReceipt;
import com.linkly.payment.printing.receipts.common.OfflineSaleReceipt;
import com.linkly.payment.printing.receipts.common.ReversalReceipt;
import com.linkly.payment.printing.receipts.suncorp.SuncorpCashbackReceipt;
import com.linkly.payment.printing.receipts.suncorp.SuncorpCashoutReceipt;
import com.linkly.payment.printing.receipts.suncorp.SuncorpLogonReceipt;
import com.linkly.payment.printing.receipts.suncorp.SuncorpPreauthCompletionReceipt;
import com.linkly.payment.printing.receipts.suncorp.SuncorpReconciliationReceipt;
import com.linkly.payment.printing.receipts.suncorp.SuncorpRefundReceipt;
import com.linkly.payment.printing.receipts.suncorp.SuncorpSaleReceipt;

import java.util.ArrayList;
import java.util.List;

public class Suncorp implements ICustomer, ICustomerMenu {

    private static Suncorp ourInstance = null;
    private static IDependency d = null;
    private static final byte[] ansiIpek = new byte[] {(byte)0x6A,(byte)0xC2,(byte)0x92,(byte)0xFA,(byte)0xA1,(byte)0x31,(byte)0x5B,(byte)0x4D,(byte)0x85,(byte)0x8A,(byte)0xB3,(byte)0xA3,(byte)0xD7,(byte)0xD5,(byte)0x93,(byte)0x3A};
    private static final byte[] ansiReverseIpek = new byte[] {(byte)0x71,(byte)0x40,(byte)0x29,(byte)0x7E,(byte)0xCB,(byte)0x0D,(byte)0xD8,(byte)0xF1,(byte)0xD6,(byte)0xD8,(byte)0x54,(byte)0xE3,(byte)0x05,(byte)0xFB,(byte)0x41,(byte)0x29};
    private static final byte[] ansiKsn = new byte[] {(byte)0xFF,(byte)0xFF,(byte)0x98,(byte)0x76,(byte)0x54,(byte)0x32,(byte)0x10,(byte)0xE0,(byte)0x00,(byte)0x00};

    private void resetPinKey() {
        P2PLib.getInstance().getIP2PSec().setInstalledKeyType(IP2PSec.InstalledKeyType.DUKPT);
        P2PLib.getInstance().getIP2PSec().writeDUKPTKey(ansiIpek, ansiKsn, IP2PSec.KeyGroup.TERM_GROUP);
        P2PLib.getInstance().getIP2PSec().writeDUKPTKey(ansiReverseIpek, ansiKsn, IP2PSec.KeyGroup.TRANS_GROUP);
    }

    @SuppressWarnings("static")
    protected Suncorp(IDependency d) {
        super();
        this.d = d;

        // inject test keys for demo/suncorp customer
        resetPinKey();
    }

    public static Suncorp getInstance(IDependency d) {

        if (ourInstance == null) {
            ourInstance = new Suncorp(d);
            ourInstance.overrideConfigs();
        }

        // sanity check some config values and apply sensible defaults if they're not set
        if( null != d.getConfig() && null != d.getPayCfg() && null != d.getPayCfg().getPaymentSwitch() ) {
            if ( 0 == d.getPayCfg().getPaymentSwitch().getDialTimeout() ) {
                d.getPayCfg().getPaymentSwitch().setDialTimeout( 15 );
            }

            if ( 0 == d.getPayCfg().getPaymentSwitch().getReceiveTimeout() ) {
                d.getPayCfg().getPaymentSwitch().setReceiveTimeout( 15 );
            }
        }

        return ourInstance;
    }

    public IProto.TaskProtocolType getProtocolType() {
        return AS2805_SUNCORP;
    }

    public IReceipt getReceiptForTrans(TransRec trans) {
        IReceipt receipt = null;

        if (trans != null) {
            switch (trans.getTransType()) {
                case SALE:
                case SALE_AUTO:
                case CARD_NOT_PRESENT:
                case SALE_MOTO_AUTO:
                    receipt = new SuncorpSaleReceipt();
                    break;

                case TOPUPCOMPLETION:
                case COMPLETION:
                case TOPUPPREAUTH:
                case PREAUTH:
                    receipt = new SuncorpPreauthCompletionReceipt();
                    break;

                case OFFLINESALE:
                    receipt = new OfflineSaleReceipt();
                    break;

                case CASH:
                case CASH_AUTO:
                    receipt = new SuncorpCashoutReceipt();
                    break;

                case CASHBACK:
                case CASHBACK_AUTO:
                    receipt = new SuncorpCashbackReceipt();
                    break;

                case REFUND:
                case REFUND_AUTO:
                case CARD_NOT_PRESENT_REFUND:
                    receipt = new SuncorpRefundReceipt();
                    break;
                case RECONCILIATION_AUTO:
                case RECONCILIATION:
                    receipt = new SuncorpReconciliationReceipt(true, true);
                    break;
                case MANUAL_REVERSAL:
                case MANUAL_REVERSAL_AUTO:
                    receipt = new ReversalReceipt();
                    break;
                case DEPOSIT:
                    receipt = new DepositReceipt();
                    break;
                case AUTO_LOGON:
                    receipt = new SuncorpLogonReceipt();
                    break;
                case TESTCONNECT:
                    receipt = new AboutAppReceipt();
                    break;
            }
        }

        return receipt;
    }

    public String getTerminalType(TransRec trans) {
        if (trans == null || trans.getProtocol().isIncludeMac()) {
            return Iso8583Rev93.PosDataCode.TerminalType._04_ECR;
        } else {
            return Iso8583Rev93.PosDataCode.TerminalType._01_POS;
        }
    }

    @Override
    public String calculateRetRefNumber(TransRec trans) {
        TProtocol proto = trans.getProtocol();
        TAudit auditinfo = trans.getAudit();
        String tid = auditinfo.getTerminalId();
        String tidLast2 = "00";
        if (tid != null && tid.length() > 2)
            tidLast2 = tid.substring(tid.length() - 2);
        String rrn = IsoUtils.padLeft(String.valueOf(trans.getAudit().getReceiptNumber()), 6, '0') + tidLast2
                + IsoUtils.padLeft(String.valueOf(proto.getBatchNumber()), 4, '0');

        return rrn;
    }

    /**********
     * Setup Customer Menu Layouts
     * @param level
     * @return
     */
    public List<MenuItems> getAdminMenuList(User.Privileges level) {
        List<MenuItems> menuItems = new ArrayList<MenuItems>();
        MenusCfg menusCfg = d.getProfileCfg().getPaymentMenus();

        String [] menuList = menusCfg.GetAdminMenuList("USER");
        for (String menuName : menuList) {
            MenuItems item = MenuItems.getFromString(menuName);
            if (item == null)
                continue;
            menuItems.add(item);
        }
        return menuItems;
    }

    public List<MenuItems> getMainMenuList(User.Privileges level) {
        List<MenuItems> menuItems = new ArrayList<MenuItems>();
        PayCfg payCfg = d.getPayCfg();
        MenusCfg menusCfg = d.getProfileCfg().getPaymentMenus();

        /* transaction menu */
        String [] menuList = menusCfg.GetMainMenuList("USER");
        for (String menuName : menuList) {
            MenuItems item = MenuItems.getFromString(menuName);
            if (item == null)
                continue;

            switch (item) {
                case Sale:
                    if (payCfg.isSaleTransAllowed()) {
                        menuItems.add(item);
                    }
                    break;

                case Pwcb:
                    if (payCfg.isCashBackAllowed()) {
                        menuItems.add(item);
                    }
                    break;

                case Cash:
                    if (payCfg.isCashTransAllowed()) {
                        menuItems.add(item);
                    }
                    break;

                case Reversal:
                    if (payCfg.isReversalTransAllowed()) {
                        menuItems.add(item);
                    }
                    break;

                case Refund:
                    if (payCfg.isRefundTransAllowed()) {
                        menuItems.add(item);
                    }
                    break;

                case PreAuth:
                case Completion:
                    if (payCfg.isPreAuthTransAllowed()) {
                        item.setDisabled(false);
                        menuItems.add(item);
                    }
                    break;

                case Reconciliation:
                    if (payCfg.isReconciliationAllowed()) {
                        menuItems.add(item);
                    }
                    break;

                case CardNotPresent:
                    if (d.getPayCfg().isManualAllowed()) {
                        menuItems.add(item);
                    }
                    break;
                case BatchUpload:
                case TestConnect:
                default:
                    menuItems.add(item);
                    break;
            }
        }
        return menuItems;
    }


    public boolean overrideConfigs() {
        PayCfg payCfg = d.getPayCfg();
        if (payCfg == null || !payCfg.isValidCfg()) {
            return true;
        }

        if (ProfileCfg.getInstance().isDemo()) {
            d.getPayCfg().getPaymentSwitch().setDialTimeout(1);
            d.getPayCfg().getPaymentSwitch().setReceiveTimeout(1);
            d.getPayCfg().getPaymentSwitch().setSendTimeout(1);
        }

        //Force Tips Screen to choose Percentage Tip
        d.getPayCfg().setUsePercentageTip(true);


        return true;
    }

    public boolean supportOfflineAsKeyed() {
        return false;
    }

    @Override
    public boolean supportCtlsReferences() {
        return true;
    }

    @Override
    public boolean supportRecWithAuthCount() {
        return false;
    }

    @Override
    public boolean supportMotoAndTelephone() {
        return true;
    }

    @Override
    public boolean supportTipsOnReports() {
        return true;
    }

    @Override
    public boolean supportDefaultUsers() {
        return true;
    }

    @Override
    public boolean supportAutoReversals() {
        return false;
    }

    @Override
    public boolean supportReversalsForTransType(EngineManager.TransType transType) {
        return true;
    }

    @Override
    public boolean supportManualVoids() {
        return false;
    }

    @Override
    public boolean supportStoringDBEncryptedCardData() { return true; }

    @Override
    public boolean supportFullDailyBatchReport() { return false; }

    @Override
    public PCI_FORMAT wipePciSensitiveData() { return POST_TRANS; }

    @Override
    public boolean supportAutoRecs() { return true; }

    @Override
    public boolean supportPreAuthCompletion() {
        return false;// Not sure about return value. Phil Please confirm
    }
    @Override
    public boolean supportAvs() {
        return false;
    }

    @Override
    public boolean supportCscForRefund() {
        return false;
    }

    @Override
    public boolean supportOnlineReversal() {
        return true;
    }

    @Override
    public boolean supportOnlinePin() {
        return true;
    }

    @Override
    public IConfig getConfigProvider() {
        return Config.getInstance();
    }

    @Override
    public int getTcuKeyLength() {
        // not supported currently
        return 0;
    }

    @Override
    public boolean hideBrandDisplayLogoHeader() {
        return false;
    }

    @Override
    public String getAcquirerCode() {
        return "U"; // refer Linkly Terminal Development Specification, Appendix D for acquirer table
    }

    @Override
    public String getTransPasscode(TransRec trans) {
        // use default implementation
        return CheckPassword.loadPasswordFromConfig(d, trans);
    }

    @Override
    public int getTransPasscodeRetryCount(TransRec trans) {
        return CheckPassword.loadPasswordRetryLimitFromConfig(d, trans);
    }

    @Override
    public String getRequiredSecAppFlavor() {
        return "libs_set_1";
    }
}


