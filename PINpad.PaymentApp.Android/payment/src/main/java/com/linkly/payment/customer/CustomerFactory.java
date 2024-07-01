package com.linkly.payment.customer;

import com.linkly.libengine.config.PayCfg;
import com.linkly.libengine.dependencies.IDependency;
import com.linkly.libengine.engine.customers.ICustomer;
import com.linkly.payment.customer.Demo.Demo;
import com.linkly.payment.customer.LiveGroup.LiveGroup;
import com.linkly.payment.customer.Woolworths.Woolworths;
import com.linkly.payment.customer.till.Till;

import timber.log.Timber;

/**
 * Reads Config and uses it to create a {@link ICustomer} Object
 * */
public class CustomerFactory {

    public static ICustomer createCustomerObj( IDependency dep ) {
        PayCfg payCfg = dep.getPayCfg();

        ICustomer instance;
        if ( payCfg.getCustomerName() == null ) {
            instance = Demo.getInstance( dep );
        } else {
            Timber.e("PayCfgImpl customer name = %s", payCfg.getCustomerName() );
            if (payCfg.getCustomerName().contains("Woolworths")) {
                instance = Woolworths.getInstance(dep);
            } else if (payCfg.getCustomerName().contains("LiveGroup")) {
                instance = LiveGroup.getInstance(dep);
            }  else if (payCfg.getCustomerName().contains("Till")) {
                instance = Till.getInstance(dep);
            } else {
                instance = Demo.getInstance(dep);
            }
        }

        dep.setConfig( instance.getConfigProvider() );
        dep.getConfig().setPayCfg(payCfg);

        return instance;
    }

}

