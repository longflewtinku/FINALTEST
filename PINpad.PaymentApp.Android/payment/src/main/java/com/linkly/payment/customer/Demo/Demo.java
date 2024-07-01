package com.linkly.payment.customer.Demo;

import static com.linkly.libengine.engine.customers.ICustomer.PCI_FORMAT.POST_REC;
import static com.linkly.libengine.engine.protocol.IProto.TaskProtocolType.DEMO;

import com.linkly.libconfig.MenusCfg;
import com.linkly.libconfig.cpat.CardProductCfg;
import com.linkly.libengine.action.check.CheckPassword;
import com.linkly.libengine.config.Config;
import com.linkly.libengine.config.IConfig;
import com.linkly.libengine.config.PayCfg;
import com.linkly.libengine.dependencies.IDependency;
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
import com.linkly.libpositive.messages.IMessages;
import com.linkly.libsecapp.IP2PSec;
import com.linkly.payment.customer.ICustomerMenu;
import com.linkly.payment.menus.MenuItems;
import com.linkly.payment.printing.receipts.common.AboutAppReceipt;
import com.linkly.payment.printing.receipts.demo.DemoCashbackReceipt;
import com.linkly.payment.printing.receipts.demo.DemoCashoutReceipt;
import com.linkly.payment.printing.receipts.demo.DemoLogonReceipt;
import com.linkly.payment.printing.receipts.demo.DemoPreauthCancellationReceipt;
import com.linkly.payment.printing.receipts.demo.DemoPreauthCompletionReceipt;
import com.linkly.payment.printing.receipts.demo.DemoReconciliationReceipt;
import com.linkly.payment.printing.receipts.demo.DemoRefundReceipt;
import com.linkly.payment.printing.receipts.demo.DemoReversalReceipt;
import com.linkly.payment.printing.receipts.demo.DemoSaleReceipt;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

public class Demo implements ICustomer, ICustomerMenu {

    private static Demo ourInstance = null;
    private static IDependency d = null;
    private static final byte[] ansiIpek = new byte[] {(byte)0x6A,(byte)0xC2,(byte)0x92,(byte)0xFA,(byte)0xA1,(byte)0x31,(byte)0x5B,(byte)0x4D,(byte)0x85,(byte)0x8A,(byte)0xB3,(byte)0xA3,(byte)0xD7,(byte)0xD5,(byte)0x93,(byte)0x3A};
    private static final byte[] ansiReverseIpek = new byte[] {(byte)0x71,(byte)0x40,(byte)0x29,(byte)0x7E,(byte)0xCB,(byte)0x0D,(byte)0xD8,(byte)0xF1,(byte)0xD6,(byte)0xD8,(byte)0x54,(byte)0xE3,(byte)0x05,(byte)0xFB,(byte)0x41,(byte)0x29};
    private static final byte[] ansiKsn = new byte[] {(byte)0xFF,(byte)0xFF,(byte)0x98,(byte)0x76,(byte)0x54,(byte)0x32,(byte)0x10,(byte)0xE0,(byte)0x00,(byte)0x00};

    private void resetPinKey() {
        d.getP2PLib().getIP2PSec().setInstalledKeyType(IP2PSec.InstalledKeyType.DUKPT);
        d.getP2PLib().getIP2PSec().writeDUKPTKey(ansiIpek, ansiKsn, IP2PSec.KeyGroup.TERM_GROUP);
        d.getP2PLib().getIP2PSec().writeDUKPTKey(ansiReverseIpek, ansiKsn, IP2PSec.KeyGroup.TRANS_GROUP);
    }


    @SuppressWarnings("static")
    protected Demo(IDependency d) {
        super();
        this.d = d;

        // inject test keys for demo customer
        resetPinKey();
    }

    public static Demo getInstance(IDependency d) {

        if (ourInstance == null) {
            ourInstance = new Demo(d);
            ourInstance.overrideConfigs();
        }
        return ourInstance;
    }

    public IProto.TaskProtocolType getProtocolType() {
        return DEMO;
    }

    public IReceipt getReceiptForTrans(TransRec trans) {
        IReceipt receipt = null;

        if (trans != null) {
            switch (trans.getTransType()) {
                case SALE:
                case SALE_AUTO:
                case CARD_NOT_PRESENT:
                case SALE_MOTO:
                case SALE_MOTO_AUTO:
                    receipt = new DemoSaleReceipt();
                    break;

                case TOPUPCOMPLETION:
                case COMPLETION:
                case COMPLETION_AUTO:
                case TOPUPPREAUTH:
                case PREAUTH:
                case PREAUTH_AUTO:
                case PREAUTH_MOTO:
                case PREAUTH_MOTO_AUTO:
                    receipt = new DemoPreauthCompletionReceipt();
                    break;

                case PREAUTH_CANCEL_AUTO:
                case PREAUTH_CANCEL:
                    receipt = new DemoPreauthCancellationReceipt();
                    break;

                case CASH:
                case CASH_AUTO:
                    receipt = new DemoCashoutReceipt();
                    break;

                case CASHBACK:
                case CASHBACK_AUTO:
                    receipt = new DemoCashbackReceipt();
                    break;

                case REFUND:
                case REFUND_AUTO:
                case REFUND_MOTO:
                case REFUND_MOTO_AUTO:
                case CARD_NOT_PRESENT_REFUND:
                    receipt = new DemoRefundReceipt();
                    break;
                case RECONCILIATION_AUTO:
                case RECONCILIATION:
                    receipt = new DemoReconciliationReceipt(IMessages.ReportType.ZReport);
                    break;
                case SUMMARY:
                case SUMMARY_AUTO:
                    receipt = new DemoReconciliationReceipt(IMessages.ReportType.XReport);
                    break;
                case LAST_RECONCILIATION_AUTO:
                    receipt = new DemoReconciliationReceipt(IMessages.ReportType.LastReconciliationReport);
                    break;
                case MANUAL_REVERSAL:
                case MANUAL_REVERSAL_AUTO:
                    receipt = new DemoReversalReceipt();
                    break;
                case AUTO_LOGON:
                    receipt = new DemoLogonReceipt();
                    break;
                case TESTCONNECT:
                    receipt = new AboutAppReceipt();
                    break;
                default:
                    Timber.e( "ERROR unknown trans type %s", trans.getTransType().name() );
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
        List<MenuItems> menuItems = new ArrayList<>();
        MenusCfg menusCfg = d.getProfileCfg().getPaymentMenus();

        String[] menuList = menusCfg.GetAdminMenuList(level.name());
        for (String menuName : menuList) {
            MenuItems item = MenuItems.getFromString(menuName);
            if (item == null)
                continue;

            if (item == MenuItems.ClearTransactions) {
                // Enable this Item only for Demo customer
                item.setDisabled(false);
            }
            menuItems.add(item);
        }

        return menuItems;
    }

    public List<MenuItems> getMainMenuList(User.Privileges level) {
        List<MenuItems> menuItems = new ArrayList<MenuItems>();
        PayCfg payCfg = d.getPayCfg();
        MenusCfg menusCfg = d.getProfileCfg().getPaymentMenus();

        /* transaction menu */
        String [] menuList = menusCfg.GetMainMenuList(level.toString());
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
        //Force Tips Screen to choose Percentage Tip
        d.getPayCfg().setUsePercentageTip(true);
        return true;
    }

    public boolean supportOfflineAsKeyed() {
        return false;
    }

    @Override
    public boolean supportCtlsReferences() {
        return false;
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
        return true;
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
    public PCI_FORMAT wipePciSensitiveData() { return POST_REC; }

    @Override
    public boolean supportAutoRecs() { return false; }

    @Override
    public boolean supportPreAuthCompletion() {
        return true;
    }

    @Override
    public boolean supportAvs() {
        return false;
    }
    @Override
    public boolean supportCscForRefund() {
        return true;
    }

    @Override
    public boolean supportOnlineReversal() {
        return false;
    }

    @Override
    public boolean supportOnlinePin() {
        return false;
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
        return "O"; // O = offline. refer Linkly Terminal Development Specification, Appendix D for acquirer table
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


