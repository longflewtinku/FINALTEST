package com.linkly.payment.customer.LiveGroup;

import static com.linkly.libengine.engine.customers.ICustomer.PCI_FORMAT.POST_TRANS;
import static com.linkly.libengine.engine.protocol.IProto.TaskProtocolType.AS2805_EFTEX;
import static com.linkly.payment.menus.MenuItems.ShiftTotalsMenu;

import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import com.linkly.libconfig.MenusCfg;
import com.linkly.libconfig.ProfileCfg;
import com.linkly.libengine.action.check.CheckPassword;
import com.linkly.libengine.config.Config;
import com.linkly.libengine.config.IConfig;
import com.linkly.libengine.config.PayCfg;
import com.linkly.libengine.dependencies.IDependency;
import com.linkly.libengine.engine.EngineManager;
import com.linkly.libengine.engine.customers.ICustomer;
import com.linkly.libengine.engine.protocol.IProto;
import com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583Rev93;
import com.linkly.libengine.engine.transactions.TransRec;
import com.linkly.libengine.printing.IReceipt;
import com.linkly.libengine.users.User;
import com.linkly.libmal.MalFactory;
import com.linkly.libpositive.messages.IMessages;
import com.linkly.libsecapp.IP2PSec;
import com.linkly.payment.customer.ICustomerMenu;
import com.linkly.payment.menus.MenuItems;
import com.linkly.payment.printing.receipts.common.AboutAppReceipt;
import com.linkly.payment.printing.receipts.common.DepositReceipt;
import com.linkly.payment.printing.receipts.common.OfflineSaleReceipt;
import com.linkly.payment.printing.receipts.common.ShiftTotalsReceipt;
import com.linkly.payment.printing.receipts.livegroup.LiveGroupCashbackReceipt;
import com.linkly.payment.printing.receipts.livegroup.LiveGroupCashoutReceipt;
import com.linkly.payment.printing.receipts.livegroup.LiveGroupLogonReceipt;
import com.linkly.payment.printing.receipts.livegroup.LiveGroupPreauthCancellationReceipt;
import com.linkly.payment.printing.receipts.livegroup.LiveGroupPreauthCompletionReceipt;
import com.linkly.payment.printing.receipts.livegroup.LiveGroupReconciliationReceipt;
import com.linkly.payment.printing.receipts.livegroup.LiveGroupRefundReceipt;
import com.linkly.payment.printing.receipts.livegroup.LiveGroupReversalReceipt;
import com.linkly.payment.printing.receipts.livegroup.LiveGroupSaleReceipt;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import timber.log.Timber;

public class LiveGroup implements ICustomer, ICustomerMenu {
    @Override
    public IConfig getConfigProvider() {
        // use default config provider
        return Config.getInstance();
    }

    private static LiveGroup ourInstance = null;
    private static IDependency d = null;

    protected LiveGroup(IDependency d) {
        super();
        LiveGroup.d = d;
    }

    public static LiveGroup getInstance(IDependency d) {

        if (ourInstance == null) {
            ourInstance = new LiveGroup(d);
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

        d.getP2PLib().getIP2PSec().setInstalledKeyType(IP2PSec.InstalledKeyType.AS2805);

        return ourInstance;
    }

    public IProto.TaskProtocolType getProtocolType() {
        return AS2805_EFTEX;
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
                    receipt = new LiveGroupSaleReceipt();
                    break;

                case TOPUPCOMPLETION:
                case COMPLETION:
                case COMPLETION_AUTO:
                case TOPUPPREAUTH:
                case PREAUTH:
                case PREAUTH_AUTO:
                case PREAUTH_MOTO:
                case PREAUTH_MOTO_AUTO:
                    receipt = new LiveGroupPreauthCompletionReceipt();
                    break;

                case PREAUTH_CANCEL_AUTO:
                case PREAUTH_CANCEL:
                    receipt = new LiveGroupPreauthCancellationReceipt();
                    break;

                case OFFLINESALE:
                    receipt = new OfflineSaleReceipt();
                    break;

                case CASH:
                case CASH_AUTO:
                    receipt = new LiveGroupCashoutReceipt();
                    break;

                case CASHBACK:
                case CASHBACK_AUTO:
                    receipt = new LiveGroupCashbackReceipt();
                    break;

                case REFUND:
                case REFUND_AUTO:
                case REFUND_MOTO:
                case REFUND_MOTO_AUTO:
                case CARD_NOT_PRESENT_REFUND:
                    receipt = new LiveGroupRefundReceipt();
                    break;
                case RECONCILIATION_AUTO:
                case RECONCILIATION:
                    receipt = new LiveGroupReconciliationReceipt(IMessages.ReportType.ZReport);
                    break;
                case SUMMARY:
                case SUMMARY_AUTO:
                    receipt = new LiveGroupReconciliationReceipt(IMessages.ReportType.XReport);
                    break;
                case LAST_RECONCILIATION_AUTO:
                    receipt = new LiveGroupReconciliationReceipt(IMessages.ReportType.LastReconciliationReport);
                    break;
                case SHIFT_TOTALS:
                case SHIFT_TOTALS_AUTO:
                case AUTOMATIC_SHIFT_TOTALS:
                    receipt = new ShiftTotalsReceipt(IMessages.ReportType.ShiftTotalsReport);
                    break;
                case SUB_TOTALS:
                case SUB_TOTALS_AUTO:
                    receipt = new ShiftTotalsReceipt(IMessages.ReportType.SubShiftTotalsReport);
                    break;
                case REPRINT_SHIFT_TOTALS:
                    receipt = new ShiftTotalsReceipt(IMessages.ReportType.ReprintShiftTotalsReport);
                    break;
                case MANUAL_REVERSAL:
                case MANUAL_REVERSAL_AUTO:
                    receipt = new LiveGroupReversalReceipt();
                    break;
                case DEPOSIT:
                    receipt = new DepositReceipt();
                    break;
                case AUTO_LOGON:
                    receipt = new LiveGroupLogonReceipt();
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

        return "TODO";
    }

    /**********
     * Setup Customer Menu Layouts
     * @param level privileges level
     * @return {@link List} of {@link MenuItems}
     */
    public List<MenuItems> getAdminMenuList(User.Privileges level) {
        List<MenuItems> menuItems = new ArrayList<>();
        MenusCfg menusCfg = d.getProfileCfg().getPaymentMenus();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(Objects.requireNonNull(MalFactory.getInstance().getMalContext()));

        String [] menuList = menusCfg.GetAdminMenuList(level.name());
        for (String menuName : menuList) {
            MenuItems item = MenuItems.getFromString(menuName);
            if (item == null)
                continue;

            switch (item) {
                case ShiftTotalsMenu:
                    if (preferences.getBoolean("shiftTotalsEnabled", false)) {
                        menuItems.add(item);
                    }
                    break;

                case Reconciliation:
                    if (preferences.getBoolean("settlementEnabled", true)) {
                        menuItems.add(item);
                    }
                    break;

                default:
                    menuItems.add(item);
                    break;
            }
        }
        return menuItems;
    }

    public List<MenuItems> getMainMenuList(User.Privileges level) {
        List<MenuItems> menuItems = new ArrayList<>();
        PayCfg payCfg = d.getPayCfg();
        MenusCfg menusCfg = d.getProfileCfg().getPaymentMenus();

        /* transaction menu */
        String [] menuList = menusCfg.GetMainMenuList(level.toString());
        for (String menuName : menuList) {
            Timber.i( "menuName = %s", menuName );
            MenuItems item = MenuItems.getFromString(menuName);
            if (item == null) {
                Timber.i( "item is null" );
                continue;
            }

            switch (item) {
                case Sale:
                    Timber.i( "payCfg.isSaleTransAllowed() = %b", payCfg.isSaleTransAllowed() );
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
                case PreAuthCancel:
                case Completion:
                case PreAuthSearchAndView:
                    if (payCfg.isPreAuthTransAllowed()) {
                        item.setDisabled(false);
                        menuItems.add(item);
                    }
                    break;

                case PreAuthMoto:
                    if (payCfg.isPreAuthTransAllowed() && payCfg.isManualAllowed()) {
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
                case MotoSale:
                    if (payCfg.isManualAllowed()) {
                        menuItems.add(item);
                    }
                    break;

                case MotoRefund:
                    if(payCfg.isRefundTransAllowed() && payCfg.isManualAllowed()) {
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
        return true;
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
        switch( transType ) {
            case REFUND:
                return false;
            default:
                return true;
        }
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
    public int getTcuKeyLength() {
        return 1984;
    }

    @Override
    public boolean hideBrandDisplayLogoHeader() {
        return false;
    }

    @Override
    public String getAcquirerCode() {
        return "L"; // refer Linkly Terminal Development Specification, Appendix D for acquirer table
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
        // Different EFTPOS cless kernel required for LiveGroup
        return "libs_set_1";
    }
}


