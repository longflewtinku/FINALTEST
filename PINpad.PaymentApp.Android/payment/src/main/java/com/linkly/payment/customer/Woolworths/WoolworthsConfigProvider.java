package com.linkly.payment.customer.Woolworths;

import com.linkly.libconfig.DownloadCfg;
import com.linkly.libconfig.MalConfig;
import com.linkly.libconfig.ProfileCfg;
import com.linkly.libconfig.WoolworthsCfgMapper;
import com.linkly.libconfig.WoolworthsEpatConfig;
import com.linkly.libconfig.WoolworthsPktConfig;
import com.linkly.libconfig.cpat.CardProductCfg;
import com.linkly.libconfig.cpat.CardProductList;
import com.linkly.libconfig.cpat.Woolworths.WWCards;
import com.linkly.libconfig.cpat.Woolworths.WoolworthsCPATParser;
import com.linkly.libconfig.cpat.Woolworths.WoolworthsCardProductList;
import com.linkly.libengine.config.BinRangesCfg;
import com.linkly.libengine.config.BlacklistCfg;
import com.linkly.libengine.config.IConfig;
import com.linkly.libengine.config.IssuerCfg;
import com.linkly.libengine.config.PayCfg;
import com.linkly.libengine.env.TableVersion;
import com.linkly.libengine.helpers.NotificationErrorHelper;
import com.linkly.libmal.IMalFile;
import com.linkly.libmal.MalFactory;
import com.linkly.libmal.global.config.JSONParse;
import com.linkly.libmal.global.config.Parse;
import com.linkly.libmal.global.config.XmlParse;
import com.linkly.libmal.global.util.Util;
import com.linkly.libsecapp.CtlsCfg;
import com.linkly.libsecapp.EmvCfg;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

public class WoolworthsConfigProvider implements IConfig {
    private static final WoolworthsConfigProvider ourInstance = new WoolworthsConfigProvider();
    private final BinRangesCfg binRangesCfg = new BinRangesCfg();
    private boolean configLoaded = false;
    private EmvCfg emvCfg = null;
    private CtlsCfg ctlsCfg = null;
    private BlacklistCfg blacklistCfg = null;
    private PayCfg payCfg = null;

    private final List<Exception> parseExceptions = new ArrayList<>();

    private WoolworthsConfigProvider() {
        super();
    }

    public static WoolworthsConfigProvider getInstance() {
        return ourInstance;
    }

    public boolean isConfigLoaded() {
        return configLoaded;
    }

    private IssuerCfg getIssuerCfg( String issuerName ) {
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

    private void applyIssuerConfigToCardProduct(List<CardProductCfg> cards) {

        if(cards == null) {
            Timber.e("CardProduct List is null");
            return;
        }

        for (CardProductCfg card: cards) {
            IssuerCfg issuerCfg = getIssuerCfg(card.getName());
            if(issuerCfg != null && !issuerCfg.isEnabled()) {
                card.setDisabled(true);
            }
        }
    }

    private void applyIssuerConfigToEmvCfg() {
        if( emvCfg == null ) {
            Timber.e("Error EMV CFG null");
            return;
        }
        for (EmvCfg.EmvScheme scheme : emvCfg.getSchemes()) {
            IssuerCfg issuerCfg = getIssuerCfg( scheme.getScheme_label() );
            if (null != issuerCfg && !issuerCfg.isEnabled()) {
                scheme.setDisabled(true);
            }
        }
    }

    private void applyIssuerConfigToCtlsCfg() {
        if( ctlsCfg == null ) {
            Timber.e("Error ctls CFG null");
            return;
        }
        for (CtlsCfg.CtlsAid aid : ctlsCfg.getAids()) {
            IssuerCfg issuerCfg = getIssuerCfg( aid.getSchemeLabel() );
            if (null != issuerCfg && !issuerCfg.isEnabled()) {
                aid.setDisabled(true);
            }
        }
    }

    private void applyCtlsLimitOverride() {
        if( getPayCfg() == null || ctlsCfg == null || ctlsCfg.getAids() == null ) {
            return;
        }

        // if config not set for these params, exit early. We require valid data in both for it to be active
        if( Util.isNullOrEmpty(getPayCfg().getOverrideCtlsCvmLimitEnabled()) ||
                Util.isNullOrEmpty(getPayCfg().getOverrideCtlsCvmLimit()) ) {
            return;
        }

        // check if enabled
        if( !getPayCfg().getOverrideCtlsCvmLimitEnabled().equals("Y") ) {
            return;
        }

        // validate limit is numeric
        int limit;
        try {
            limit = Integer.parseInt(getPayCfg().getOverrideCtlsCvmLimit());
        } catch (NumberFormatException nfe) {
            Timber.e( "Error - override ctls cvm limit is not numeric, not applying" );
            // nope, return
            return;
        }

        // else we have a valid limit, apply it as override to ctls config
        for (CtlsCfg.CtlsAid aid : ctlsCfg.getAids()) {
            int setToLimit;

            setToLimit = Math.max(limit, 0);
            aid.setCvmLimit(setToLimit);
            Timber.i( "Setting AID %s override CTLS CVM limit to %d", aid.getAid(), setToLimit );
        }
    }

    private void handleConfigError(Exception e) {
        if(e == null) {
            Timber.e("Handling a null error");
            return;
        }
        // Debug the stack trace
        Timber.w(e);
        Timber.e("Displaying Info, %s", e.getMessage());
        NotificationErrorHelper.displayNotification("Config Error", e.getMessage());

        // record our exception
        parseExceptions.add(e);
    }


    private <T> T parseWithException(Parse parser, String filename, Type classOfT) {
        try {
            return parser.parse(filename, classOfT);
        } catch(final Exception e) { // Realistically we should only get the errors defined out our XML.parse function
            handleConfigError(e);
        }
        // Return null here
        return null;
    }

    /**
     * Gets filePath by looking at custom created directories "/EFT/" & working directory
     * Paxstore sends files down to "/EFT/" which is weird but it is what it is
     * @param fileName of the file to search for
     * @return path of the file
     * */
    private String getFilePath( String fileName ){
        IMalFile iMalFile = MalFactory.getInstance().getFile();
        final String commonDir = iMalFile.getCommonDir() + "/";
        final String workingDir = iMalFile.getWorkingDir() + "/";
        final String installDir = iMalFile.getInstallDir() + "/";

        String filePath;
        if( iMalFile.fileExist( commonDir + fileName ) ) {
            filePath = ( commonDir + fileName );
        } else if( iMalFile.fileExist( workingDir + fileName ) ) {
            filePath = ( workingDir + fileName );
        } else {
            filePath = ( installDir + fileName );
        }

        Timber.v( "Using filePath [%s]", filePath );

        return filePath;
    }

    public boolean loadConfig() {

        if (!configLoaded) {
            final long startTime = System.currentTimeMillis();
            MalConfig.getInstance().loadConfig();

            JSONParse j = new JSONParse();
            XmlParse xmlParser = new XmlParse();
            WoolworthsPktParser pktParser = new WoolworthsPktParser();

            // clear our current list
            parseExceptions.clear();

            // read card product
            CardProductList cardproductList;
            try {
                // Parse the required files into the required objects
                WoolworthsCPATParser woolworthsCPAT = new WoolworthsCPATParser(
                        getFilePath( getPayCfg().getCardProductFile() )
                );
                WWCards wwCards = xmlParser.parse(
                        getPayCfg().getCardsFile(),
                        WWCards.class );

                cardproductList = new WoolworthsCardProductList(woolworthsCPAT
                        ,wwCards
                         ).getConfig();
            } catch ( final Exception e ) {
                handleConfigError(e);
                return false;
            }

            if ( cardproductList != null ) {
                getPayCfg().setCardProductVersion(cardproductList.cardProductVersion);
                // Note we apply the modification before we pass it to the cards.
                applyIssuerConfigToCardProduct(cardproductList.cards); // This applies to Magstripe transactions
                // Woolworths config contains no Cashout limit, applying System wide Cashout limit
                applyCashoutLimitToCardProduct(cardproductList.cards, getPayCfg().getCashoutLimitCents());
                getPayCfg().setCards( cardproductList.cards );
            }

            // read most of our emv and ctls settings from EPAT file
            // read CAPK data from PKT
            WoolworthsEpatConfig epatConfig = parseWithException(xmlParser, getPayCfg().getEpatFile(), WoolworthsEpatConfig.class);
            WoolworthsPktConfig pktConfig = parseWithException(pktParser, getPayCfg().getPktFile(), WoolworthsPktConfig.class);

            // Note: with this ca
            // read ctls config from cfg_ctls_emv.json
            ctlsCfg = parseWithException(j, getPayCfg().getCfgCtlsFile(), CtlsCfg.class);
            emvCfg = parseWithException(j, getPayCfg().getCfgEmvFile(), EmvCfg.class);

            emvCfg = WoolworthsCfgMapper.epatAndPktToEmvCfg(emvCfg, epatConfig, pktConfig);
            ctlsCfg = WoolworthsCfgMapper.epatAndPktToCtlsCfg(ctlsCfg, epatConfig, pktConfig);

            // Just loading an empty blacklist config for now. Need to load from file if needed later
            blacklistCfg = BlacklistCfg.getInstance();

            TableVersion.setEpatVersion( emvCfg.getEpatTableVersion() );
            if( pktConfig != null ) {
                TableVersion.setPktVersion( pktConfig.getTableVersion() );
            } else {
                Timber.e( "PKT Config is null " );
            }

            // apply issuer disabled flag to EMV config
            applyIssuerConfigToEmvCfg();
            // apply issuer disabled flag to CTLS config
            applyIssuerConfigToCtlsCfg();

            // apply ctls cvm limit overrides
            applyCtlsLimitOverride();

            if (getPayCfg() != null && getPayCfg().isValidCfg()) {

                /* sort out the card group bin ranges so they can be retrieved via card data */
                binRangesCfg.initialiseBinRanges(getPayCfg());
            } else {
                Timber.e("PayCFG is not valid");
            }

            final long endTime = System.currentTimeMillis();
            Timber.i("Total Config Parse Time: %s", (endTime - startTime));
            if(emvCfg != null && ctlsCfg != null && cardproductList != null) {
                configLoaded = true;
            }
        }

        return true;
    }

    private void applyCashoutLimitToCardProduct(List<CardProductCfg> cards, int cashoutLimitCents ) {

        if(cards == null) {
            Timber.e("CardProduct List is null");
            return;
        }

        for (CardProductCfg card: cards) {
            card.getLimits().setCashMax(cashoutLimitCents);
            card.getLimits().setCashbackMax(cashoutLimitCents);
        }
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
    public DownloadCfg getDownloadCfg() { return MalConfig.getInstance().getDownloadCfg(); }

    @Override
    public ProfileCfg getProfileCfg() { return MalConfig.getInstance().getProfileCfg(); }

    @Override
    public List<Exception> getConfigErrors() {
        return parseExceptions;
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
