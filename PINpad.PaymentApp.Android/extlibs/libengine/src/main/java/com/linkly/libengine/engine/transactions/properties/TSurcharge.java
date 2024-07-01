package com.linkly.libengine.engine.transactions.properties;

import static com.linkly.libengine.action.cardprocessing.CTLSProcessing.isEftposAid;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.ACC_TYPE_CHEQUE;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.ACC_TYPE_SAVINGS;

import com.linkly.libconfig.cpat.CardProductCfg;
import com.linkly.libengine.config.PayCfg;
import com.linkly.libengine.dependencies.IDependency;
import com.linkly.libengine.engine.Engine;
import com.linkly.libengine.engine.transactions.TransRec;
import com.linkly.libmal.global.util.Util;
import com.linkly.libpositive.wrappers.Surcharge;
import com.linkly.libpositive.wrappers.TagDataFromPOS;
import com.linkly.libsecapp.CtlsCfg;
import com.linkly.libsecapp.EmvCfg;

import java.util.List;

import timber.log.Timber;

public class TSurcharge {

    public TSurcharge() {}

    public static void surchargeForSwiped(TransRec trans, PayCfg payCfg) {
        CardProductCfg cardCfg = trans.getCard().getCardsConfig(payCfg);
        updateSurcharge(trans, cardCfg.getBinNumber(), payCfg);
        trans.getCard().setLinklyBinNumber(cardCfg.getBinNumber());
    }

    public static void surchargeForICC(TransRec trans, IDependency dependency) {
        List<EmvCfg.EmvScheme> schemes = dependency.getConfig().getEmvCfg().getSchemes();
        String currAid = trans.getCard().getAid();
        for (EmvCfg.EmvScheme scheme : schemes) {
            for (EmvCfg.EmvAid aid : scheme.getAids()) {
                if (currAid.contains(aid.getAid())) {
                    // check account type selected
                    if( trans.getProtocol().getAccountType().equals(ACC_TYPE_SAVINGS) ||
                            trans.getProtocol().getAccountType().equals(ACC_TYPE_CHEQUE) ){
                        Timber.i( "Applying ICC DEBIT surcharge for AID %s. Linkly BIN no = %d", aid.getAid(), aid.getLinklyBinNumberDebit() );
                        updateSurcharge(trans, aid.getLinklyBinNumberDebit(), dependency.getPayCfg());
                        trans.getCard().setLinklyBinNumber(aid.getLinklyBinNumberDebit());
                    } else {
                        Timber.i( "Applying ICC CREDIT surcharge for AID %s. Linkly BIN no = %d", aid.getAid(), aid.getLinklyBinNumberCredit() );
                        updateSurcharge(trans, aid.getLinklyBinNumberCredit(), dependency.getPayCfg());
                        trans.getCard().setLinklyBinNumber(aid.getLinklyBinNumberCredit());
                    }
                    return;
                }
            }
        }
    }

    public static void calculateSurchargeForCtls(IDependency d, String currAid, boolean mncCard, boolean mndCard, boolean debitAppLabel) {
        TransRec trans = d.getCurrentTransaction();

        trans.getAmounts().setSurcharge(0);

        if ( !trans.getTransType().supportsSurcharge || Util.isNullOrEmpty(currAid))
            return;

        List<CtlsCfg.CtlsAid> aids = Engine.getDep().getConfig().getCtlsCfg().getAids();
        for (CtlsCfg.CtlsAid aid : aids) {
            if (currAid.contains(aid.getAid())) {
                // do not read "defaultAccount" from config;
                // Update the logic below when more formal Surcharge requirements avail
                // mncCard: multinetwork Credit Card (EFTPOS and another Credit App)
                // mndCard: multinetwork Debit Card (EFTPOS and another Debit App)
                if (debitAppLabel || mndCard || (mncCard && isEftposAid(currAid))) {
                    Timber.i("Applying CTLS DEBIT surcharge for AID %s. Linkly BIN no = %d", aid.getAid(), aid.getLinklyBinNumberDebit());
                    updateSurcharge(trans, aid.getLinklyBinNumberDebit(), d.getPayCfg());
                    trans.getCard().setLinklyBinNumber(aid.getLinklyBinNumberDebit());
                }
                else {
                    Timber.i( "Applying CTLS CREDIT surcharge for AID %s. Linkly BIN no = %d", aid.getAid(), aid.getLinklyBinNumberCredit() );
                    updateSurcharge(trans, aid.getLinklyBinNumberCredit(), d.getPayCfg());
                    trans.getCard().setLinklyBinNumber(aid.getLinklyBinNumberCredit());
                }
                return;
            }
        }
    }

    private static void updateSurcharge(TransRec trans, int binNumber, PayCfg payCfg) {
        TagDataFromPOS tagData = trans.getTagDataFromPos();

        // if surcharge global flag is enabled in config AND surcharge allowed for txn type
        if(payCfg != null &&
                payCfg.isSurchargeSupported() &&
                trans.getTransType().supportsSurcharge ) {
            // if PAD tag data from POS is present and includes SC2 tag, then use it
            if (tagData != null && tagData.getSC2() != null) {
                applySurcharge(trans, tagData.getSC2(), binNumber);
            } else {
                List<Surcharge> scList = payCfg.getDefaultSc();
                Surcharge[] scArray = scList.toArray(new Surcharge[0]);
                applySurcharge(trans, scArray, binNumber);
            }
        }
    }

    private static void applySurcharge(TransRec trans, Surcharge[] sc2Array, int binNumber ) {
        long amount = trans.getAmounts().getSurchargeableAmount();

        for (Surcharge sc : sc2Array) {
            if (Integer.parseInt(sc.getB()) == binNumber) {
                if (sc.getT().equals("%")) {
                    double percentDouble = Integer.parseInt(sc.getV()) / 100.00;
                    double surcharge = (percentDouble / 100.00) * amount;
                    trans.getAmounts().setSurcharge(Math.round(surcharge));
                } else {
                    double dollarDouble = Integer.parseInt(sc.getV());
                    trans.getAmounts().setSurcharge((long) dollarDouble);
                }
                // exit early
                return;
            }
        }
        Timber.i( "no match in surcharge data for linkly BIN %d", binNumber );
    }

}

