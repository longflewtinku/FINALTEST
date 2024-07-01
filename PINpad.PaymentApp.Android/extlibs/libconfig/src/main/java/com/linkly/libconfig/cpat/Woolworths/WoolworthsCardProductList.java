package com.linkly.libconfig.cpat.Woolworths;

import static com.linkly.libconfig.cpat.PSIIssuerDefines.PSI_ISSUER_AMEX;
import static com.linkly.libconfig.cpat.PSIIssuerDefines.PSI_ISSUER_DINERS;
import static com.linkly.libconfig.cpat.PSIIssuerDefines.PSI_ISSUER_EFTPOS;
import static com.linkly.libconfig.cpat.PSIIssuerDefines.PSI_ISSUER_JCB;
import static com.linkly.libconfig.cpat.PSIIssuerDefines.PSI_ISSUER_MASTERCARD;
import static com.linkly.libconfig.cpat.PSIIssuerDefines.PSI_ISSUER_UNIONPAY;
import static com.linkly.libconfig.cpat.PSIIssuerDefines.PSI_ISSUER_UNKNOWN;
import static com.linkly.libconfig.cpat.PSIIssuerDefines.PSI_ISSUER_VISA;

import com.linkly.libconfig.cpat.CardProductCfg;
import com.linkly.libconfig.cpat.CardProductList;
import com.linkly.libconfig.cpat.ICardProductList;
import com.linkly.libmal.IMalFile;
import com.linkly.libmal.MalFactory;
import com.linkly.libmal.global.config.XmlParse;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

/**
 * Creates a {@link CardProductList} for the app layer to use from {@link WoolworthsCPATParser} & {@link WWCards} files
 * */
public class WoolworthsCardProductList implements ICardProductList {

    /**
     * Object initialized here, members set inside constructor
     * */
    private final CardProductList cardProductList = new CardProductList();
    /**
     * Will contain a map (not Java Map) of WW index & Linkly Bin numbers along with Card Names
     * */
    private final WWCards wwCards;

    /**
     * Public constructor, will take care of all members
     * Takes in CPAT parser and WWCards parsed files
     * */
    public WoolworthsCardProductList(WoolworthsCPATParser woolworthsCPAT, WWCards wwCards) {
        this.wwCards = wwCards;

        cardProductList.cardProductVersion = Integer.parseInt( woolworthsCPAT.getProcessingParametersRecord().getVersionNumber() );

        cardProductList.cards = convertToCards( woolworthsCPAT.getWoolworthsCPATEntries() );
    }



    /**
     * Can be null if implementation is incomplete or throws an internal error
     * @return {@link CardProductList} object based on the implementation
     * */
    @Override
    public CardProductList getConfig() {
        return this.cardProductList;
    }

    /**
     * Populates a list of {@link CardProductCfg}
     * Cannot be null but the list can be empty
     * @param woolworthsCPATEntries to be converted
     * @return {@link CardProductCfg} list
     * */
    private List<CardProductCfg> convertToCards( List<WoolworthsCPATEntry> woolworthsCPATEntries ) {
        List<CardProductCfg> cards = new ArrayList<>();

        for ( WoolworthsCPATEntry entry : woolworthsCPATEntries ) {
            cards.add( mapToCardProductCfg( entry ) );
        }

        return cards;
    }


    // Maps the linkly bin number and populates the PSI value.
    // Woolies config files don't populate this at all.
    private String psiOffLinklyBinNumber(String linklyBinNumber) {

        // Parse to simplify the data meaning we don't need to care about prefix 0's etc
        int binNumber = Integer.parseInt(linklyBinNumber);

        // Only for finance cards. Links with PSI
        switch (binNumber) {
            case 1: // EFTPOS
                return PSI_ISSUER_EFTPOS;
            case 2: // BANK Card/ Union pay
                return PSI_ISSUER_UNIONPAY;
            case 3: // Master card credit
                return PSI_ISSUER_MASTERCARD;
            case 4: // Visa Credit
                return PSI_ISSUER_VISA;
            case 5: // Amex
                return PSI_ISSUER_AMEX;
            case 6: // Diners
                return PSI_ISSUER_DINERS;
            case 7, 9, 11: // JCB
                return PSI_ISSUER_JCB;
            case 10:  // MAESTRO
                return PSI_ISSUER_MASTERCARD;
            case 28:
                return PSI_ISSUER_VISA; //	Visa debit
            case 29:
                return PSI_ISSUER_MASTERCARD; //	Mastercard debit
            case 30:
                return PSI_ISSUER_UNIONPAY; //	UnionPay credit
            case 31:
                return PSI_ISSUER_UNIONPAY; //	UnionPay debit
            default:
                break;
        }

        return PSI_ISSUER_UNKNOWN;
    }

    /**
     * Converts {@link WoolworthsCPATEntry} into {@link CardProductCfg}. Cannot be null.
     * @param woolworthsCPATEntry to be mapped
     * @return {@link CardProductCfg} object
     * */
    private CardProductCfg mapToCardProductCfg( WoolworthsCPATEntry woolworthsCPATEntry ){
        CardProductCfg cardProductCfg = CardProductCfg.getDefaultConfig();

        // Name & Bin Number
        for ( WWCards.Entry entry : this.wwCards.getEntryList() ) {
            if( entry.getIndex().equals( woolworthsCPATEntry.getCARD_NAME_INDEX().getINDEX() ) ){
                cardProductCfg.setName( entry.getAppName() );
                cardProductCfg.setBinNumber( Integer.parseInt( entry.getLinklyBinNumber() ) );
                // Add PSI
                cardProductCfg.setPsi(psiOffLinklyBinNumber(entry.getLinklyBinNumber()));
                break;
            }
        }

        // IIN Range
        String iinRange = woolworthsCPATEntry.getCARD_PREFIX().getCardBinStartValue() + "-" + woolworthsCPATEntry.getCARD_PREFIX().getCardBinEndValue();

        cardProductCfg.setIinRange( iinRange );

        // Luhn Check
        cardProductCfg.setLuhnCheck(
                woolworthsCPATEntry.getPROCESSING_OPTIONS().isEnabled(
                        ProcessingOptionsBitmap.Bits.LUHN_CHECK
                )
        );

        // AGC
        switch ( woolworthsCPATEntry.getACCOUNT_GROUPING_CODE() ){
            case DEBIT:
                cardProductCfg.setAccountSelection( CardProductCfg.ACC_SAVINGS +
                        CardProductCfg.ACC_CHEQUE );
                break;
            case ALL:
                cardProductCfg.setAccountSelection( CardProductCfg.ACC_SAVINGS +
                        CardProductCfg.ACC_CHEQUE +
                        CardProductCfg.ACC_CREDIT );
                break;
            case DEFAULT:
            case CREDIT:
            default:
                cardProductCfg.setAccountSelection( CardProductCfg.ACC_CREDIT );
                break;
        }

        // PSC
        this.extractProcessingSpecCode( cardProductCfg, woolworthsCPATEntry.getPROCESSING_SPEC_CODE() );

        // ESC
        cardProductCfg.setServiceCodeCheck(
                woolworthsCPATEntry.getPROCESSING_OPTIONS().isEnabled(
                        ProcessingOptionsBitmap.Bits.ESC_BIT
                )
        );

        // Refund
        cardProductCfg.getServicesAllowed().setRefund(
                woolworthsCPATEntry.getPROCESSING_OPTIONS().isEnabled(
                        ProcessingOptionsBitmap.Bits.ALLOW_REFUND
                )
        );

        // Contactless Reject
        cardProductCfg.setRejectCtls(
                woolworthsCPATEntry.getPROCESSING_OPTIONS().isEnabled(
                        ProcessingOptionsBitmap.Bits.REJECT_CTLS
                )
        );

        // EMV Reject
        cardProductCfg.setRejectEmv(
                woolworthsCPATEntry.getPROCESSING_OPTIONS().isEnabled(
                        ProcessingOptionsBitmap.Bits.REJECT_EMV
                )
        );

        // Small value pin
        cardProductCfg.getOnlinePin().setSmallValue(
                woolworthsCPATEntry.getPROCESSING_OPTIONS().isEnabled(
                        ProcessingOptionsBitmap.Bits.SMALL_VALUE_PIN_MANDATORY
                ) ? "Y" : "N"
        );

        // Small value limit
        cardProductCfg.getLimits().setSmallValueLimitDollars(
                this.getSmallValueLimit( woolworthsCPATEntry.getPROCESSING_OPTIONS() )
        );

        return cardProductCfg;
    }

    /**
     * Extracts small value limit
     * */
    private int getSmallValueLimit( ProcessingOptionsBitmap processingOptionsBitmap ){
        final String SMALL_VALUE_LIMIT = ( processingOptionsBitmap.isEnabled( ProcessingOptionsBitmap.Bits.SMALL_VALUE_LIMIT_1 ) ? "1" : "0" ) +
                ( processingOptionsBitmap.isEnabled( ProcessingOptionsBitmap.Bits.SMALL_VALUE_LIMIT_2 ) ? "1" : "0" ) +
                ( processingOptionsBitmap.isEnabled( ProcessingOptionsBitmap.Bits.SMALL_VALUE_LIMIT_3 ) ? "1" : "0" ) +
                ( processingOptionsBitmap.isEnabled( ProcessingOptionsBitmap.Bits.SMALL_VALUE_LIMIT_4 ) ? "1" : "0" ) +
                ( processingOptionsBitmap.isEnabled( ProcessingOptionsBitmap.Bits.SMALL_VALUE_LIMIT_5 ) ? "1" : "0" ) +
                ( processingOptionsBitmap.isEnabled( ProcessingOptionsBitmap.Bits.SMALL_VALUE_LIMIT_6 ) ? "1" : "0" ) +
                ( processingOptionsBitmap.isEnabled( ProcessingOptionsBitmap.Bits.SMALL_VALUE_LIMIT_7 ) ? "1" : "0" ) +
                ( processingOptionsBitmap.isEnabled( ProcessingOptionsBitmap.Bits.SMALL_VALUE_LIMIT_8 ) ? "1" : "0" );

        return Integer.parseInt( SMALL_VALUE_LIMIT, 2 );
    }

    /**
     * Extracts the whole {@link ProcessingSpecificationCode} object into corresponding members of {@link CardProductCfg}
     * */
    private void extractProcessingSpecCode( CardProductCfg cardProductCfg,
                                            ProcessingSpecificationCode processingSpecificationCode ) {
        boolean signMandatory = false;
        boolean pinMandatory = false;
        boolean manualAllowed = false;
        boolean approveOnline = false;

        switch ( processingSpecificationCode ) {
            case REJECT_TRANS: // Don't enable anything
                cardProductCfg.setServicesAllowed( new CardProductCfg.ServicesAllowed() );
                break;
            case SWIPE_PIN_OPTIONAL:
                break;
            case SWIPE_PIN_MANDATORY:
                pinMandatory = true;
                break;
            case SWIPE_PIN_SIGN_MANDATORY:
                pinMandatory = true;
                signMandatory = true;
                break;
            case SWIPE_SIGN_ONLY:
                signMandatory = true;
                break;
            case SWIPE_KEYED_PIN_MANDATORY:
                pinMandatory = true;
                manualAllowed = true;
                break;
            case SWIPE_KEYED_PIN_OPTIONAL:
                manualAllowed = true;
                break;
            case SWIPE_KEYED_PIN_SIGN_MANDATORY:
                pinMandatory = true;
                signMandatory = true;
                manualAllowed = true;
                break;
            case SWIPE_KEYED_SIGN_ONLY:
                manualAllowed = true;
                signMandatory = true;
                break;
            case SWIPE_APPROVE_ONLINE_PIN_MANDATORY:
                pinMandatory = true;
                approveOnline = true;
                break;
            case SWIPE_APPROVE_ONLINE_PIN_OPTIONAL:
                approveOnline = true;
                break;
            case SWIPE_APPROVE_ONLINE_PIN_SIGN_MANDATORY:
                approveOnline = true;
                signMandatory = true;
                pinMandatory = true;
                break;
            case SWIPE_APPROVE_ONLINE_SIGN_ONLY:
                approveOnline = true;
                signMandatory = true;
                break;
            case SWIPE_KEYED_APPROVE_ONLINE_PIN_MANDATORY:
                manualAllowed = true;
                pinMandatory = true;
                approveOnline = true;
                break;
            case SWIPE_KEYED_APPROVE_ONLINE_PIN_OPTIONAL:
                approveOnline = true;
                manualAllowed = true;
                break;
            case SWIPE_KEYED_APPROVE_ONLINE_PIN_SIGN_MANDATORY:
                approveOnline = true;
                signMandatory = true;
                pinMandatory = true;
                manualAllowed = true;
                break;
            case SWIPE_KEYED_APPROVE_ONLINE_SIGN_ONLY:
                approveOnline = true;
                signMandatory = true;
                manualAllowed = true;
                break;
            default:
                Timber.w( "Code is not implemented = [%s]", processingSpecificationCode );
        }

        cardProductCfg.setForceSign( signMandatory );
        if( approveOnline && pinMandatory ) {
            CardProductCfg.OnlinePin onlinePin = new CardProductCfg.OnlinePin();

            onlinePin.setSale( "Y" );
            onlinePin.setRefund( "Y" );
            onlinePin.setPreauth( "Y" );
            onlinePin.setPinChange( "Y" );
            onlinePin.setDeposit( "Y" );
            onlinePin.setCashback( "Y" );
            onlinePin.setBalance( "Y" );

            cardProductCfg.setOnlinePin( onlinePin );
        }

        cardProductCfg.getServicesAllowed().setMoto( manualAllowed );
    }
}
