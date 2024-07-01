package com.linkly.libengine.config;

import com.linkly.libconfig.DownloadCfg;
import com.linkly.libconfig.MalConfig;
import com.linkly.libconfig.ProfileCfg;
import com.linkly.libconfig.cpat.CardProductCfg;
import com.linkly.libconfig.cpat.CardProductList;
import com.linkly.libconfig.cpat.legacy.LegacyCardProductCfg;
import com.linkly.libmal.global.config.JSONParse;
import com.linkly.libsecapp.CtlsCfg;
import com.linkly.libsecapp.EmvCfg;

import java.util.List;

import timber.log.Timber;

public class Config implements IConfig {

    private static final Config ourInstance = new Config();
    private BinRangesCfg binRangesCfg;
    private boolean configLoaded = false;
    private EmvCfg emvCfg = null;
    private CtlsCfg ctlsCfg = null;
    private BlacklistCfg blacklistCfg = null;

    private PayCfg payCfg = null;

    public Config() {
        super();
        binRangesCfg = null;
        binRangesCfg = new BinRangesCfg();
    }

    public static Config getInstance() {
        return ourInstance;
    }

    public boolean isConfigLoaded() {
        return configLoaded;
    }

    private IssuerCfg getIssuerCfg(String issuerName ) {

        if( null == payCfg || null == payCfg.getIssuers() || null == issuerName ) {
            return null;
        }

        // do case insensitive comparison to find issuer name in issuer config table
        for( IssuerCfg issuerCfg : payCfg.getIssuers() ) {
            if( null != issuerCfg.getIssuerName() && issuerName.compareToIgnoreCase(issuerCfg.getIssuerName()) == 0 ) {
                return issuerCfg;
            }
        }

        // else not found
        return null;
    }

    /**
     * For each entry in card product list, check if there's an entry in issuer table for it and apply 'disable' flag if set
     */
    private void applyIssuerConfigToCardProduct(List<CardProductCfg> cards) {

        if(cards == null) {
            Timber.e("CardProduct List is null");
            return;
        }

        for (CardProductCfg card: cards) {
            IssuerCfg issuerCfg = getIssuerCfg(card.getSchemeLabel());
            Timber.d("Cards config: issuer scheme label = %s", card.getSchemeLabel());
            Timber.d("Cards config: issuer scheme name = %s", card.getName());
            // Default to "disabled" if Issuer was not found. Note: we check exact match of IssuerName and CardProduct entry
            if(issuerCfg != null && issuerCfg.isEnabled()) {
                card.setDisabled(false);
                Timber.d("Cards config: %s scheme enabled", card.getSchemeLabel());
            } else {
                card.setDisabled(true);
                Timber.e("Cards config: %s scheme disabled", card.getSchemeLabel());
            }
            card.setDeferredAuthEnabled(issuerCfg != null && issuerCfg.isDeferredAuthEnabled());
        }
    }

    private void applyIssuerConfigToEmvCfg() {
        if( emvCfg == null ) {
            Timber.e("Error EMV CFG null");
            return;
        }
        for (EmvCfg.EmvScheme scheme : emvCfg.getSchemes()) {
            IssuerCfg issuerCfg = getIssuerCfg(scheme.getScheme_label() );
            // Default to "disabled" if Issuer was not found. Note: we check exact match of IssuerName and EmvCfg entry
            if (null != issuerCfg && issuerCfg.isEnabled()) {
                scheme.setDisabled(false);
            } else {
                scheme.setDisabled(true);
                Timber.e("Cards config: %s scheme disabled", scheme.getScheme_label());
            }
            scheme.setDeferredAuthEnabled(issuerCfg != null && issuerCfg.isDeferredAuthEnabled());
        }
    }

    private void applyIssuerConfigToCtlsCfg() {
        if( ctlsCfg == null ) {
            Timber.e("Error ctls CFG null");
            return;
        }
        for (CtlsCfg.CtlsAid aid : ctlsCfg.getAids()) {
            IssuerCfg issuerCfg = getIssuerCfg(aid.getSchemeLabel() );
            // Default to "disabled" if Issuer was not found. Note: we check exact match of IssuerName and CtlsCfg entry
            if (null != issuerCfg && issuerCfg.isEnabled()) {
                aid.setDisabled(false);
            } else {
                aid.setDisabled(true);
                Timber.e("Cards config: %s scheme disabled", aid.getSchemeLabel());
            }
            aid.setDeferredAuthEnabled(null != issuerCfg && issuerCfg.isDeferredAuthEnabled());
        }
    }

    public boolean loadConfig() {
        if (!configLoaded) {
            final long startTime = System.currentTimeMillis();
            MalConfig.getInstance().loadConfig();
            JSONParse j = new JSONParse();

            CardProductList cardProductList = new LegacyCardProductCfg(
                    getPayCfg().getCardProductFile()).
                    getConfig();
            if (cardProductList != null) {
                getPayCfg().setCardProductVersion(cardProductList.cardProductVersion);
                if (cardProductList.cards == null) {
                    Timber.e("ERROR - no cards found");
                }
                applyIssuerConfigToCardProduct(cardProductList.cards); // This applies to Magstripe transactions
                getPayCfg().setCards(cardProductList.cards);
            } else {
                Timber.e("Card Product List parse FAILED");
            }

            // read emv config from cfg_emv.json
            emvCfg = j.parse("cfg_emv.json", EmvCfg.class);
            // apply issuer disabled flag to EMV config
            applyIssuerConfigToEmvCfg();

            // read ctls config from cfg_ctls_emv.json
            ctlsCfg = j.parse("cfg_ctls_emv.json", CtlsCfg.class);
            // apply issuer disabled flag to CTLS config
            applyIssuerConfigToCtlsCfg();

            // read blacklisted config from blacklisted_bin_ranges.json
            blacklistCfg = BlacklistCfg.parse(getPayCfg().getBlacklistFile());

            if (getPayCfg() != null && getPayCfg().isValidCfg()) {

                /* sort out the card group bin ranges so they can be retrieved via card data */
                binRangesCfg.initialiseBinRanges(getPayCfg());
            }
            final long endTime = System.currentTimeMillis();
            Timber.i("Total Config Parse Time: %s", (endTime - startTime));
            configLoaded = true;
        }

        return true;
    }

    @Override
    public PayCfg getPayCfg() {
        return payCfg;
    }

    @Override
    public void setPayCfg(PayCfg config) {
        payCfg = config;
    }

    @Override
    public BinRangesCfg getBinRangesCfg() {
        return binRangesCfg;
    }

    @Override
    public DownloadCfg getDownloadCfg() {
        return MalConfig.getInstance().getDownloadCfg();
    }

    @Override
    public ProfileCfg getProfileCfg() {
        return MalConfig.getInstance().getProfileCfg();
    }

    @Override
    public List<Exception> getConfigErrors() {
        return null;
    }

    public EmvCfg getEmvCfg() {
        return this.emvCfg;
    }

    public CtlsCfg getCtlsCfg() {
        return this.ctlsCfg;
    }

    public BlacklistCfg getBlacklistCfg() {
        return this.blacklistCfg;
    }
}
