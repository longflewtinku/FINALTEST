package com.linkly.payment.customer;

import com.linkly.libengine.config.IConfig;
import com.linkly.libengine.config.PayCfg;
import com.linkly.libengine.dependencies.IDependency;
import com.linkly.libengine.engine.EngineManager;
import com.linkly.libengine.engine.customers.ICustomer;
import com.linkly.libengine.engine.protocol.IProto;
import com.linkly.libengine.engine.transactions.TransRec;
import com.linkly.libengine.printing.IReceipt;
import com.linkly.libengine.users.User;
import com.linkly.payment.customer.Demo.Demo;
import com.linkly.payment.customer.LiveGroup.LiveGroup;
import com.linkly.payment.customer.Woolworths.Woolworths;
import com.linkly.payment.customer.till.Till;
import com.linkly.payment.menus.MenuItems;

import java.util.List;

import timber.log.Timber;

public class Customer implements ICustomer, ICustomerMenu {
    private static IDependency d = null;
    private static ICustomer instance = null;

    public static ICustomer createCustomerObj(IDependency dep) {
        d = dep;
        PayCfg payCfg = d.getPayCfg();

        if (payCfg.getCustomerName() == null) {
            Timber.e("PayCfgImpl customer name is NULL - starting DEMO customer instance" );
            instance = Demo.getInstance(d);
        } else {
            Timber.e("PayCfgImpl customer name = %s", payCfg.getCustomerName() );
            if (payCfg.getCustomerName().contains("Woolworths")) {
                instance = Woolworths.getInstance(d);
            } else if (payCfg.getCustomerName().contains("LiveGroup")) {
                instance = LiveGroup.getInstance(d);
            } else if (payCfg.getCustomerName().contains("Till")) {
                instance = Till.getInstance(d);
            } else {
                instance = Demo.getInstance(d);
            }
        }

        return instance;
    }

    private static ICustomer getInstance() {
        return instance;
    }

    public boolean overrideConfigs() {
        PayCfg payCfg = d.getPayCfg();
        if (payCfg == null || !payCfg.isValidCfg()) {
            return true;
        }
        return true;
    }

    @Override
    public IProto.TaskProtocolType getProtocolType() {
        return instance.getProtocolType();
    }

    public String getTerminalType(TransRec trans) {
        return getInstance().getTerminalType(trans);
    }

    public IReceipt getReceiptForTrans(TransRec trans) { return getInstance().getReceiptForTrans(trans); }

    public List<MenuItems> getAdminMenuList(User.Privileges level) {
        return ((ICustomerMenu)getInstance()).getAdminMenuList(level);
    }



    /* Customer Variant Functions */


    public List<MenuItems> getMainMenuList(User.Privileges level) {
        return ((ICustomerMenu)getInstance()).getMainMenuList(level);
    }

    @Override
    public String calculateRetRefNumber(TransRec trans) {
        return getInstance().calculateRetRefNumber(trans);
    }

    @Override
    public boolean supportOfflineAsKeyed() {
        return getInstance().supportOfflineAsKeyed();
    }

    @Override
    public boolean supportCtlsReferences() {
        return getInstance().supportCtlsReferences();
    }

    @Override
    public boolean supportRecWithAuthCount() {
        return getInstance().supportRecWithAuthCount();
    }

    @Override
    public boolean supportMotoAndTelephone() {
        return getInstance().supportMotoAndTelephone();
    }


    @Override
    public boolean supportTipsOnReports() {
        return getInstance().supportTipsOnReports();
    }

    @Override
    public boolean supportDefaultUsers() {
        return getInstance().supportDefaultUsers();
    }

    @Override
    public boolean supportAutoReversals() {
        return getInstance().supportAutoReversals();
    }

    @Override
    public boolean supportReversalsForTransType(EngineManager.TransType transType) {
        return getInstance().supportReversalsForTransType(transType);
    }

    @Override
    public boolean supportManualVoids() {
        return getInstance().supportManualVoids();
    }

    @Override
    public boolean supportStoringDBEncryptedCardData() { return getInstance().supportStoringDBEncryptedCardData(); }

    @Override
    public boolean supportFullDailyBatchReport() { return getInstance().supportFullDailyBatchReport(); }

    @Override
    public PCI_FORMAT wipePciSensitiveData() { return getInstance().wipePciSensitiveData(); }

    @Override
    public boolean supportAutoRecs() { return getInstance().supportAutoRecs(); }

    @Override
    public boolean supportPreAuthCompletion() {
        return getInstance().supportPreAuthCompletion();
    }

    @Override
    public boolean supportAvs() {
        return getInstance().supportAvs();
    }
    @Override
    public boolean supportCscForRefund() {
        return getInstance().supportCscForRefund();
    }

    @Override
    public boolean supportOnlineReversal() {
        return getInstance().supportOnlineReversal();
    }

    @Override
    public boolean supportOnlinePin() {
        return getInstance().supportOnlinePin();
    }

    @Override
    public IConfig getConfigProvider() {
        return instance.getConfigProvider();
    }

    @Override
    public int getTcuKeyLength() {
        return instance.getTcuKeyLength();
    }

    @Override
    public boolean hideBrandDisplayLogoHeader() {
        return getInstance().hideBrandDisplayLogoHeader();
    }

    @Override
    public String getAcquirerCode(){
        return getInstance().getAcquirerCode();
    }

    @Override
    public String getTransPasscode(TransRec trans) {
        return getInstance().getTransPasscode(trans);
    }

    @Override
    public int getTransPasscodeRetryCount(TransRec trans) {
        return getInstance().getTransPasscodeRetryCount(trans);
    }

    @Override
    public String getRequiredSecAppFlavor() {
        return getInstance().getRequiredSecAppFlavor();
    }
}

