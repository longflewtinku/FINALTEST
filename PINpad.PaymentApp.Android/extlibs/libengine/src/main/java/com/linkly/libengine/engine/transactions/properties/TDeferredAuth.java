package com.linkly.libengine.engine.transactions.properties;

import com.linkly.libconfig.cpat.CardProductCfg;
import com.linkly.libengine.config.PayCfg;
import com.linkly.libengine.engine.Engine;
import com.linkly.libengine.engine.transactions.TransRec;
import com.linkly.libsecapp.CtlsCfg;
import com.linkly.libsecapp.EmvCfg;

import java.util.List;

import timber.log.Timber;

public class TDeferredAuth {
    /**
     * as this is a 'utility' class (meaning all methods contained are static methods),
     * this should never be instantiated.
     * Java adds an implicit public constructor to every class which does not define at least one explicitly.
     * Hence, at least one non-public constructor should be defined.
     */
    private TDeferredAuth() {
        throw new IllegalStateException("Utility class");
    }

    private static boolean swipeCard(TransRec trans, PayCfg payCfg) {
        CardProductCfg cardCfg = trans.getCard().getCardsConfig(payCfg);
        if( cardCfg != null ){
            return cardCfg.isDeferredAuthEnabled();
        }
        Timber.e("error setting deferred auth flag for swiped card, aid not found. deferred auth not allowed");
        return false;
    }

    private static boolean iccCard(TransRec trans) {
        if(Engine.getDep().getConfig().getEmvCfg() == null){
            Timber.e("Emv config not set");
            return false;
        }
        List<EmvCfg.EmvScheme> schemes = Engine.getDep().getConfig().getEmvCfg().getSchemes();
        if(schemes == null){
            Timber.e("Error retrieving EMV scheme config");
            return false;
        }
        String currAid = trans.getCard().getAid();
        for (EmvCfg.EmvScheme scheme : schemes) {
            List<EmvCfg.EmvAid> aids = scheme.getAids();
            if( aids != null ) {
                for (EmvCfg.EmvAid aid : scheme.getAids()) {
                    if (currAid != null && currAid.contains(aid.getAid())) {
                        return scheme.isDeferredAuthEnabled();
                    }
                }
            }
        }
        Timber.e("error setting deferred auth flag for icc card, aid not found. deferred auth not allowed");
        return false;
    }

    private static boolean ctlsCard(TransRec trans) {
        if(Engine.getDep().getConfig().getCtlsCfg() == null){
            Timber.e("Ctls config not set");
            return false;
        }
        List<CtlsCfg.CtlsAid> aids = Engine.getDep().getConfig().getCtlsCfg().getAids();
        if(aids == null){
            Timber.e("Error retrieving CTLS aid config");
            return false;
        }
        String currAid = trans.getCard().getAid();
        for (CtlsCfg.CtlsAid aid : aids) {
            if (currAid != null && currAid.contains(aid.getAid())) {
                return aid.isDeferredAuthEnabled();
            }
        }
        Timber.e("error setting deferred auth flag for ctls card, aid not found. deferred auth not allowed");
        return false;
    }

    public static boolean getDeferredAuthConfigFlag(TransRec trans, PayCfg config) {
        boolean result;
        switch (trans.getCard().getCaptureMethod()) {
            case MANUAL:
            case ICC_FALLBACK_KEYED:
            case SWIPED:
            case ICC_FALLBACK_SWIPED:
                result = swipeCard(trans, config);
                break;
            case ICC_OFFLINE:
            case ICC:
                result = iccCard(trans);
                break;
            case CTLS:
            case CTLS_MSR:
                result = ctlsCard(trans);
                break;
            case NOT_CAPTURED:
            case SCAN_VOUCHER:
            case RRN_ENTERED:
            default:
                // deferred auth not supported for these capture methods
                Timber.e("Deferred auth not supported for this capture type [%s]", trans.getCard().getCaptureMethod());
                result = false;
                break;
        }
        return result;
    }

}

