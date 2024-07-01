package com.linkly.payment.crashreporting;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.linkly.libengine.config.PayCfg;
import com.linkly.libengine.dependencies.IDependency;
import com.linkly.libengine.engine.Engine;

import timber.log.Timber;

// A "single-shot class", only needs to be constructed once as early as possible to seed Crashlytics
//  reports with metadata. If instantiated more than once, if any of the dependencies yield new
//  values then those should replace the old, otherwise there is no effect.
public class LinklyCrashlytics {
    public LinklyCrashlytics(String initialLogMessage, String serialNumber, PayCfg config) {
        // Sets Customer name as "user" in Crashlytics, to help distinguish crashes.
        //  Since a Customer isn't a person and we're not using an PII here, this is not subject
        //  to privacy concerns surrounding user tracking. Intentionally mapping to more readable
        //  key names.
        // Note: this cannot be done earlier in MainApp onCreate because ProfileCfg instance is
        //  null at startup.
        FirebaseCrashlytics fc = FirebaseCrashlytics.getInstance();
        if (initialLogMessage != null) {
            fc.log(initialLogMessage);
        }
        if (config != null) {
            String cn = config.getCustomerName();
            if (cn == null) {
                Timber.e("ProfileCfg CustomerName not populated!");
            } else {
                fc.setUserId(cn);
            }
            fc.setCustomKey("terminal_id", config.getStid());
            fc.setCustomKey("merchant_id", config.getMid());
            IDependency deps = Engine.getDep();
            if (deps != null) {
                fc.setCustomKey("serial_number", serialNumber);
            }

            if (config.getReceipt() != null && config.getReceipt().getMerchant() != null) {
                fc.setCustomKey("receipt_data_line_01", config.getReceipt().getMerchant().getLine0());
            }

        } else {
            Timber.e("ProfileCfg not populated!");
        }
    }
}
