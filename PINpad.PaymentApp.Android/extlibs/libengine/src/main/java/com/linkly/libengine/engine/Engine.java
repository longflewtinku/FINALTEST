package com.linkly.libengine.engine;

import static com.linkly.libpositivesvc.paxstore.DownloadParamService.checkLanguage;

import android.content.Context;

import com.linkly.libengine.application.IAppCallbacks;
import com.linkly.libengine.config.BinRangesCfg;
import com.linkly.libengine.config.CdoAllowed;
import com.linkly.libengine.config.PayCfg;
import com.linkly.libengine.dependencies.IDependency;
import com.linkly.libengine.engine.comms.CommsStatusMonitor;
import com.linkly.libengine.engine.comms.IComms;
import com.linkly.libengine.engine.customers.ICustomer;
import com.linkly.libengine.engine.protocol.IProto;
import com.linkly.libengine.jobs.IJobs;
import com.linkly.libengine.jobs.Jobs;
import com.linkly.libengine.printing.IPrintManager;
import com.linkly.libengine.status.IStatus;
import com.linkly.libengine.users.UserManager;
import com.linkly.libmal.IMal;
import com.linkly.libpositive.messages.IMessages;
import com.linkly.libsecapp.CtlsCfg;
import com.linkly.libsecapp.EmvCfg;
import com.linkly.libsecapp.IP2PLib;
import com.linkly.libsecapp.P2PLib;
import com.linkly.libui.speech.SpeechUtils;

import timber.log.Timber;

public class Engine {
    private static IDependency dep = null;

    private static boolean isInitialised = false;

    public static boolean isIsInitialised() {
        return isInitialised;
    }

    // WARNING: Only added for unit testing to make things easier... Should only EVER be used in unit tests.
    public static void setDep(IDependency dependency) {
        dep = dependency;
    }


    /**
     * overrides cfg_emv.json offline ceiling limits with offline trans ceiling limit from overrideparams (if set)
     *
     * @param emvCfg input/output object
     */
    private static void applyEmvConfigOverrides(EmvCfg emvCfg, PayCfg config) {
        // set offline ceiling limit from overrides
        //MD : Transaction equal to ceiling limit are getting declined with z1 card declined in secapp, adding 1(+1) when setting
        int offlineCeilingLimitInt = config.getOfflineTransactionCeilingLimitCentsContact() + 1;

        // set offline ceiling limit from overrides
        if( emvCfg == null ) {
            return;
        }

        // set global ceiling limit
        emvCfg.getParams().setOffline_ceiling_limit(offlineCeilingLimitInt);

        // clear scheme level ceiling limit, so global limit is used
        if( emvCfg.getSchemes() != null ) {
            for (EmvCfg.EmvScheme scheme : emvCfg.getSchemes()) {
                scheme.setOffline_ceiling_limit(null);

                // for each scheme aid, clear offline ceiling limit, so global limit is used
                if( scheme.getAids() != null ) {
                    for(EmvCfg.EmvAid aid : scheme.getAids()) {
                        if( aid.getAidCfgDomestic() != null ) {
                            aid.getAidCfgDomestic().setOffline_ceiling_limit(null);
                        }
                        if( aid.getAidCfgInternational() != null ) {
                            aid.getAidCfgInternational().setOffline_ceiling_limit(null);
                        }

                        // try to find a matching BIN number in the CDO allowed table
                        boolean cdoAllowed = getCdoAllowedSetting(aid.getLinklyBinNumberCredit(), config);
                        aid.setCdo_allowed(cdoAllowed);
                    }
                }
            }
        }
    }

    private static boolean getCdoAllowedSetting(int binNumber, PayCfg config) {
        if( config.getCdoAllowedList() != null && config.getCdoAllowedList().size() > 0) {
            for(CdoAllowed cdoAllowed : config.getCdoAllowedList()) {
                if(cdoAllowed.getCardBinNumber() == binNumber) {
                    // we have match
                    return cdoAllowed.isEnabled();
                }
            }
        }
        Timber.w( "Cdo allowed flag not defined for bin number %d", binNumber);
        // else no match in table, disallow CDO
        return false;
    }

    /**
     * overrides cfg_ctls_emv.json floor limit, cvm limit and upper txn limit settings with offline trans ceiling limit from overrideparams (if set)
     *
     * @param ctlsCfg input/output object
     */
    private static void applyCtlsConfigOverrides(CtlsCfg ctlsCfg, PayCfg config) {
        // set offline ceiling limit from overrides
        int offlineCeilingLimitInt = config.getOfflineTransactionCeilingLimitCentsContactless();

        if( ctlsCfg == null ) {
            return;
        }

        // for each AID
        if( ctlsCfg.getAids() != null ) {
            // set offline ceiling limit
            for(CtlsCfg.CtlsAid aid : ctlsCfg.getAids()) {
                aid.setOfflineCeilingLimit(offlineCeilingLimitInt);
            }
        }
    }

    public static boolean initialiseP2PeConfig(PayCfg payCfg, EmvCfg emvCfg, CtlsCfg ctlsCfg) {
        boolean loadedEMV = false;
        boolean loadedCTLS = false;

        if(payCfg != null && payCfg.isValidCfg()) {
            if(emvCfg != null) {
                applyEmvConfigOverrides(emvCfg, payCfg);
                P2PLib.getInstance().getIP2PEmv().emvSetConfiguration(emvCfg.parseToString());
                loadedEMV = true;
            }

            if(ctlsCfg != null) {
                applyCtlsConfigOverrides(ctlsCfg, payCfg);
                P2PLib.getInstance().getIP2PCtls().ctlsSetConfiguration(ctlsCfg.parseToString());
                loadedCTLS = true;
            }
        }

        Timber.e("Loaded EMV: %b, Loaded CTLS: %b", loadedEMV, loadedCTLS);

        return loadedEMV && loadedCTLS;
    }

    public static void init(IDependency dependency, Context context, IMal mal) {
        dep = dependency;

        // initialise workflow engine
        if( dep.getAppCallbacks() != null ) {
            dep.getAppCallbacks().Initialise(dep);
        }

        if( dep.getFramework() != null ) {
            dep.getFramework().initialiseUI( dep.getDisplayCallback() );
            SpeechUtils.getInstance().init(context);
        }

        if (dep.getConfig() != null && dep.getPayCfg() != null && dep.getPayCfg().isValidCfg()) {

            if(!initialiseP2PeConfig(dep.getPayCfg(), dep.getConfig().getEmvCfg(), dep.getConfig().getCtlsCfg())) {
                Timber.e("Failed to initialise card config!");
            }
        }

        // initialise protocol, if provided, and customer config provided (specifies protocol to use)
        if( dep.getProtocol() != null && dep.getCustomer() != null ) {
            dep.getProtocol().init(dep);
        }

        if( dep.getUsrMgr() != null ) {
            dep.getUsrMgr().init(dep, context);
        }

        // Note that Dependencies holds 2x references to PayCfg, one directly and other via Config.
        if (dep.getPayCfg() != null) {
            CommsStatusMonitor.getInstance().open(dep, context);

            if (checkLanguage(dep.getPayCfg().getLanguage())) {
                mal.getHardware().reboot();
            }

            mal.getHardware().
                    getDal().getSys().setScreenOffTime(
                            dependency.getPayCfg().getScreenLockTime() );
        }

        // Everything that is not bound by our horrible dependency structure.
        // <intentionally left blank>

        isInitialised = true;
    }

    public static UserManager getUsrMgr() {
        if( dep == null )
            return null;

        return dep.getUsrMgr();
    }

    public static PayCfg getPayCfg() {
        if(dep == null)
            return null;

        return dep.getPayCfg();
    }

    public static BinRangesCfg getBinRangesCfg() {
        if( dep == null || dep.getConfig() == null )
            return null;

        return dep.getConfig().getBinRangesCfg();
    }

    public static IPrintManager getPrintManager() {
        if( dep == null )
            return null;

        return dep.getPrintManager();
    }

    public static ICustomer getCustomer() {
        if( dep == null )
            return null;

        return dep.getCustomer();
    }

    public static IAppCallbacks getAppCallbacks() {
        if( dep == null )
            return null;

        return dep.getAppCallbacks();
    }

    public static IProto getProtocol() {
        if( dep == null )
            return null;

        return dep.getProtocol();
    }

    public static IMessages getMessages() {
        if( dep == null )
            return null;

        return dep.getMessages();
    }

    public static IJobs getJobs() {
        return (dep == null) ? Jobs.getInstance() : dep.getJobs();
    }

    public static IComms getComms() {
        return (dep == null) ? null : dep.getComms();
    }

    public static IStatus getStatusReporter() {
        return (dep == null) ? null : dep.getStatusReporter();
    }

    public static IDependency getDep() {
        return Engine.dep;
    }
}
