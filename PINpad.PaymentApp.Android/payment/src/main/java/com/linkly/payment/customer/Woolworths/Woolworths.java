package com.linkly.payment.customer.Woolworths;

import static com.linkly.libengine.engine.customers.ICustomer.PCI_FORMAT.POST_TRANS;
import static com.linkly.libengine.engine.protocol.IProto.TaskProtocolType.AS2805_WOOLWORTHS;

import com.linkly.libconfig.MenusCfg;
import com.linkly.libconfig.ProfileCfg;
import com.linkly.libconfig.cpat.CardProductCfg;
import com.linkly.libengine.action.check.CheckPassword;
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
import com.linkly.payment.printing.receipts.common.DepositReceipt;
import com.linkly.payment.printing.receipts.common.OfflineSaleReceipt;
import com.linkly.payment.printing.receipts.common.ReversalReceipt;
import com.linkly.payment.printing.receipts.generic.CashbackReceipt;
import com.linkly.payment.printing.receipts.generic.CashoutReceipt;
import com.linkly.payment.printing.receipts.generic.LogonReceipt;
import com.linkly.payment.printing.receipts.generic.PreauthCancellationReceipt;
import com.linkly.payment.printing.receipts.generic.PreauthCompletionReceipt;
import com.linkly.payment.printing.receipts.generic.ReconciliationReceipt;
import com.linkly.payment.printing.receipts.generic.RefundReceipt;
import com.linkly.payment.printing.receipts.generic.SaleReceipt;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

public class Woolworths implements ICustomer, ICustomerMenu {

    private static Woolworths ourInstance = null;
    private static IDependency d = null;

    protected Woolworths(IDependency d) {
        super();
        Woolworths.d = d;
    }

    public static Woolworths getInstance(IDependency d) {

        if (ourInstance == null) {
            ourInstance = new Woolworths(d);
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

        d.getP2PLib().getIP2PSec().setInstalledKeyType(IP2PSec.InstalledKeyType.DUKPT);

        return ourInstance;
    }

    public IProto.TaskProtocolType getProtocolType() {
        return AS2805_WOOLWORTHS;
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
                    receipt = new SaleReceipt();
                    break;

                case TOPUPCOMPLETION:
                case COMPLETION:
                case COMPLETION_AUTO:
                case TOPUPPREAUTH:
                case PREAUTH:
                case PREAUTH_AUTO:
                case PREAUTH_MOTO:
                case PREAUTH_MOTO_AUTO:
                    receipt = new PreauthCompletionReceipt();
                    break;

                case PREAUTH_CANCEL_AUTO:
                case PREAUTH_CANCEL:
                    receipt = new PreauthCancellationReceipt();
                    break;

                case OFFLINESALE:
                    receipt = new OfflineSaleReceipt();
                    break;

                case CASH:
                case CASH_AUTO:
                    receipt = new CashoutReceipt();
                    break;

                case CASHBACK:
                case CASHBACK_AUTO:
                    receipt = new CashbackReceipt();
                    break;

                case REFUND:
                case REFUND_AUTO:
                case REFUND_MOTO:
                case REFUND_MOTO_AUTO:
                case CARD_NOT_PRESENT_REFUND:
                    receipt = new RefundReceipt();
                    break;
                case RECONCILIATION_AUTO:
                case RECONCILIATION:
                    receipt = new ReconciliationReceipt(IMessages.ReportType.ZReport);
                    break;
                case SUMMARY:
                case SUMMARY_AUTO:
                    receipt = new ReconciliationReceipt(IMessages.ReportType.XReport);
                    break;
                case MANUAL_REVERSAL:
                case MANUAL_REVERSAL_AUTO:
                    receipt = new ReversalReceipt();
                    break;
                case DEPOSIT:
                    receipt = new DepositReceipt();
                    break;
                case AUTO_LOGON:
                    receipt = new LogonReceipt();
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
     * @param level privileges level
     * @return {@link List} of {@link MenuItems}
     */
    public List<MenuItems> getAdminMenuList(User.Privileges level) {
        List<MenuItems> menuItems = new ArrayList<>();
        MenusCfg menusCfg = d.getProfileCfg().getPaymentMenus();

        Timber.i( "Going to grab Admin menu items for %s", level.toString() );
        String [] menuList = menusCfg.GetAdminMenuList(level.name());
        for (String menuName : menuList) {
            MenuItems item = MenuItems.getFromString(menuName);
            if (item == null)
                continue;
            Timber.i( "Admin Menu Item %s", item.toString() );
            menuItems.add(item);
        }
        return menuItems;
    }

    public List<MenuItems> getMainMenuList(User.Privileges level) {
        List<MenuItems> menuItems = new ArrayList<>();
        List<CardProductCfg> cards = d.getPayCfg().getCards();
        PayCfg payCfg = d.getPayCfg();
        MenusCfg menusCfg = d.getProfileCfg().getPaymentMenus();

        /* transaction menu */
        Timber.i( "Going to grab menu items for %s", level.toString() );

        String [] menuList = menusCfg.GetMainMenuList(level.toString());
        for (String menuName : menuList) {
            MenuItems item = MenuItems.getFromString(menuName);
            if (item == null)
                continue;

            Timber.i( "Main Menu Item %s", item.toString() );
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
                case PreAuthCancel:
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

        d.getPayCfg().setIncludedOrginalStandInRec(false);
        d.getPayCfg().setReversalCopyOriginal(false);

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
    public PCI_FORMAT wipePciSensitiveData() { return POST_TRANS; }

    @Override
    public boolean supportAutoRecs() { return true; }

    @Override
    public boolean supportPreAuthCompletion() { return true; }

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

        return WoolworthsConfigProvider.getInstance();
    }

    @Override
    public int getTcuKeyLength() {
        return 960;
    }

    @Override
    public boolean hideBrandDisplayLogoHeader() {
        return false;
    }

    @Override
    public String getAcquirerCode() {
        return "Q"; // refer Linkly Terminal Development Specification, Appendix D for acquirer table
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


