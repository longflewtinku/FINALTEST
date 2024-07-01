package com.linkly.libconfig;

import com.linkly.libmal.global.util.Util;
import com.linkly.libsecapp.CtlsCfg;
import com.linkly.libsecapp.EmvCfg;

import java.io.ByteArrayOutputStream;
import java.security.MessageDigest;
import java.util.ArrayList;

import timber.log.Timber;

public class WoolworthsCfgMapper {
    /**
     * convert woolies epat and pkt config objects to our internal EmvCfg format
     *
     * @param epatConfig {@link WoolworthsEpatConfig} object parsed from EPAT file
     * @param pktConfig {@link WoolworthsPktConfig} object parsed from PKT file
     * @return populated {@link EmvCfg} obect
     */
    public static EmvCfg epatAndPktToEmvCfg( EmvCfg emvCfg, WoolworthsEpatConfig epatConfig, WoolworthsPktConfig pktConfig ) {
        EmvCfg ret = epatToEmvCfg( emvCfg, epatConfig );
        return pktToEmvCfg( ret, pktConfig );
    }

    /**
     * convert woolies epat and pkt config objects to our internal EmvCfg format
     *
     * @param ctlsCfg - input ctlsCfg to merge epat and pkt settings into
     * @param epatConfig - input config settings from EPAT table
     * @param pktConfig - input config settings from PKT table
     * @return CtlsCfg merged configuration data
     */
    public static CtlsCfg epatAndPktToCtlsCfg( CtlsCfg ctlsCfg, WoolworthsEpatConfig epatConfig, WoolworthsPktConfig pktConfig ) {
        CtlsCfg ret = epatToCtlsCfg( ctlsCfg, epatConfig );
        return pktToCtlsCfg( ret, pktConfig );
    }

    /**
     * Generate a SHA-1 hash for the given CAPK data, conforms to EMV standards
     * required because config from PKT doesn't include hash but it's 100% required by ctls/contact kernels
     *
     * @param capk input capk object to generate hash for
     * @param indexByte index byte 0x00-0xFF
     * @return SHA-1 hash in string format
     */
    private static String generateHash(WoolworthsPktConfig.CAPKey capk, byte indexByte) {
        try {
            ByteArrayOutputStream payload = new ByteArrayOutputStream();
            byte[] ridBytes = Util.hexStringToByteArray(capk.getRid());
            byte[] modulusBytes = Util.hexStringToByteArray(capk.getModulus());
            byte[] exponentBytes = Util.hexStringToByteArray(getSignificantBytesAsString(capk.getExponent()));

            payload.write(ridBytes);
            payload.write(indexByte);
            payload.write(modulusBytes);
            payload.write(exponentBytes);

            // generate SHA-1 on
            MessageDigest md = MessageDigest.getInstance("SHA-1");

            return Util.byteArrayToHexString(md.digest(payload.toByteArray()));
        } catch( Exception e ) {
            Timber.e( "error generating CAPK hash value" );
            return "";
        }
    }

    /**
     * appends PKT (CA Public Key data) to existing EmvCfg struct
     *
     * @param emvCfg object to add PKT data to
     * @param pktConfig input PKT ca public key object
     * @return EmvCfg merged configuration
     */
    private static EmvCfg pktToEmvCfg( EmvCfg emvCfg, WoolworthsPktConfig pktConfig ) {
        if( emvCfg == null || pktConfig == null) {
            return null;
        }
        // for each entry in PKT table
        for(WoolworthsPktConfig.CAPKey capKey : pktConfig.getKeyList() ) {
            // find corresponding RID/scheme in EmvCfg
            for(EmvCfg.EmvScheme scheme : emvCfg.getSchemes()) {
                if( scheme.getRid().equals(capKey.getRid())) {
                    // yes we have a match.
                    if( scheme.getCapks() == null ) {
                        scheme.setCapks(new ArrayList<>());
                    }

                    // add and populate CAPK entry
                    EmvCfg.EmvCapk newCapk = new EmvCfg.EmvCapk();
                    scheme.getCapks().add(newCapk);

                    String indexHex = capKey.getIndex();
                    byte[] indexByte = Util.hexStringToByteArray(indexHex);
                    int indexInt = indexByte[0] & 0xff; // convert single byte (will only ever be 1) to positive integer

                    newCapk.setIndexhex(indexHex);
                    newCapk.setIndex(indexInt);
                    newCapk.setModulus(capKey.getModulus());
                    newCapk.setExponent(getSignificantBytesAsString(capKey.getExponent()));
                    newCapk.setHash(generateHash(capKey, indexByte[0]));
                    newCapk.setExpiry(capKey.getDateExpiry());
                    newCapk.setTest_key(false); // for test keys, woolies have completely separate PKT tables
                }
            }
        }

        return emvCfg;
    }

    /**
     * strips insignificant (00) leading/left bytes out of passed hex string
     * e.g. 000003 input -> 03 output
     *
     * @param exponentStr hex String
     * @return stripped hex string output
     */
    private static String getSignificantBytesAsString(String exponentStr) {
        byte[] exponentBytes = Util.hexStringToByteArray(exponentStr);
        // find first significant byte, copy from there
        int copyFrom;
        int copyLength = exponentBytes.length;
        for( copyFrom=0; copyFrom<exponentBytes.length; copyFrom++ ) {
            if( exponentBytes[copyFrom] != 0 ) {
                break;
            }
            copyLength--;
        }
        byte[] exponentStripped = new byte[copyLength];
        System.arraycopy( exponentBytes, copyFrom, exponentStripped, 0, exponentStripped.length );
        return Util.byteArrayToHexString(exponentStripped);
    }

    private static String parseHexValue( String inputStr, int multiplier, int defaultValue ) {
        int outputVal = defaultValue;
        if( inputStr != null && inputStr.length() >= 8 ) {
            int inputInt = Util.fourAsciiHexBytesToInt(inputStr);
            outputVal = inputInt * multiplier;
        }
        return String.valueOf(outputVal);
    }

    private static EmvCfg.EmvScheme findSchemeOrCreateNew( String rid, EmvCfg emvCfg ) {
        // search current AID list for one matching AID
        EmvCfg.EmvScheme scheme = null;
        for(EmvCfg.EmvScheme searchScheme : emvCfg.getSchemes() ) {
            if( searchScheme.getRid() != null && searchScheme.getRid().equals(rid)) {
                // found it
                scheme = searchScheme;
                break;
            }
        }

        // add new record if we didn't find a matching one above, and add to list
        if( scheme == null ) {
            scheme = new EmvCfg.EmvScheme();
            emvCfg.getSchemes().add(scheme);
        }

        return scheme;
    }

    private static EmvCfg.EmvAid findEmvAidOrCreateNew( String aid, EmvCfg.EmvScheme scheme ) {
        // search current AID list for one matching AID
        EmvCfg.EmvAid emvAid = null;
        for(EmvCfg.EmvAid searchAid : scheme.getAids() ) {
            if( searchAid.getAid() != null && searchAid.getAid().equals(aid)) {
                // found it
                emvAid = searchAid;
                break;
            }
        }

        // add new record if we didn't find a matching one above, and add to list
        if( emvAid == null ) {
            emvAid = new EmvCfg.EmvAid();
            scheme.getAids().add(emvAid);
        }

        return emvAid;
    }

    /**
     * takes contact emv settings from EPAT data and populates EmvCfg object
     *
     * @param emvCfg - input/output parameter - emvCfg object to fill with data from EPAT
     * @param epatConfig - input epat configuration
     * @return input EmvCfg object merged with epat data
     */
    private static EmvCfg epatToEmvCfg( EmvCfg emvCfg, WoolworthsEpatConfig epatConfig ) {

        if( emvCfg == null || epatConfig == null) {
            return null;
        }

        // root elements
        // set epat table version in separate version field
        emvCfg.setEpatTableVersion(epatConfig.getTableVersion());

        // target
        EmvCfg.Params params = new EmvCfg.Params();
        // source
        WoolworthsEpatConfig.Tlv_Tag_Emv_Def_Tags defaultTags = epatConfig.getDefaultParam().getTags();

        params.setFloor_limit(0); // risk mgmt params overridden at AID level
        params.setRs_threshold(0);
        params.setTarget_percent(0);
        params.setMax_targ_percent(0);
        params.setTac_default(""); // tacs overridden/set for each AID individually
        params.setTac_denial("");
        params.setTac_online("");
        params.setDefault_tdol("");
        params.setDefault_ddol("");
        params.setTerm_caps(defaultTags.getTerminalCapabilities());
        params.setTerm_add_caps(defaultTags.getAdditionalTerminalCapabilities());
        params.setTerm_type(defaultTags.getTerminalType());
        params.setMerch_cat_code(Integer.parseInt(defaultTags.getMerchantCategoryCode()));
        params.setTerm_cat_code("R"); // aka terminal category code and set at AID level. NB woolies EPAT tool has the value 'R' for all. Refer EMV specs for more info.
        params.setTerm_count_code(Integer.parseInt(defaultTags.getTerminalCountryCode()));
        params.setTerm_curr_code(Integer.parseInt(defaultTags.getTransactionReferenceCurrencyCode()));
        params.setTerm_curr_exponent(Integer.parseInt(defaultTags.getTransactionReferenceCurrencyExponent()));
        emvCfg.setParams(params);

        // for each CONTACT emv in source
        EmvCfg.EmvScheme scheme = null;
        for(WoolworthsEpatConfig.Tlv_Tag_Emv_Aid_Param emvAidParam : epatConfig.getParamList() ) {
            // has scheme changed? look at first 5 bytes of AID
            if( scheme == null || !emvAidParam.getAid().startsWith(scheme.getRid())) {
                // find scheme in existing data, or create new one
                scheme = findSchemeOrCreateNew( emvAidParam.getAid().substring(0,10), emvCfg );

                scheme.setRid(emvAidParam.getAid().substring(0,10));

                // if aids list not initialised, do it now
                if( scheme.getAids() == null ) {
                    scheme.setAids(new ArrayList<>());
                }

                // enable partial selection for all schemes
                scheme.setPartial(2);
            }

            // find AID in existing scheme, or create new one

            // create new AID, set AID level params
            EmvCfg.EmvAid aid = findEmvAidOrCreateNew( emvAidParam.getAid(), scheme );

            aid.setAid( emvAidParam.getAid() );
            aid.setAppName( emvAidParam.getDisplayText() );

            // TODO: priority, appSelIndic, displayText not used from src

            // search each origin mode (domestic or international). use domestic settings for now
            for(WoolworthsEpatConfig.Tlv_Tag_Emv_Aid_Tags originTags : emvAidParam.getTagList() ) {
                EmvCfg.EmvAidCfg aidCfg;
                // 4 = domestic, 0 = international
                if( originTags.getCardOrigin().equals("4") ) {
                    aidCfg = aid.getAidCfgDomestic();
                    // create new obj if not set already
                    if( aidCfg == null ) {
                        aidCfg = new EmvCfg.EmvAidCfg();
                    }
                    aid.setAidCfgDomestic(aidCfg);
                } else if( originTags.getCardOrigin().equals("0")) {
                    aidCfg = aid.getAidCfgInternational();
                    // create new obj if not set already
                    if( aidCfg == null ) {
                        aidCfg = new EmvCfg.EmvAidCfg();
                    }
                    aid.setAidCfgInternational(aidCfg);
                } else {
                    Timber.e( "unexpected card origin value %s", originTags.getCardOrigin() );
                    continue;
                }

                aidCfg.setTermAvn(originTags.getTxnTags().getApplicationVersionNumberTerm());
                aidCfg.setSecondAvn(originTags.getTxnTags().getApplicationVersionNumberTerm());
                aidCfg.setFloor_limit( parseHexValue( originTags.getTxnTags().getTerminalFloorLimit(), 100, 0 ) ); // value is in whole dollars, append '00' cents on end
                aidCfg.setRs_threshold( parseHexValue( originTags.getTxnTags().getThreshold(), 1, 0 ) );
                aidCfg.setTarget_percent(originTags.getTxnTags().getTargetPercent());
                aidCfg.setMax_targ_percent(originTags.getTxnTags().getMaxTargetPercent());
                aidCfg.setTac_default(originTags.getTxnTags().getTermActCodeDefault());
                aidCfg.setTac_denial(originTags.getTxnTags().getTermActCodeDenial());
                aidCfg.setTac_online(originTags.getTxnTags().getTermActCodeOnline());
                aidCfg.setDefault_tdol(originTags.getTxnTags().getTdolDefault());
                aidCfg.setDefault_ddol(originTags.getTxnTags().getDdolDefault());
            }
        }
        return emvCfg;
    }

    /**
     * appends PKT (CA Public Key data) to existing CtlsCfg struct
     *
     * @param ctlsCfg {@link CtlsCfg} object which doesn't have CA Public Key Data
     * @param pktConfig {@link WoolworthsPktConfig} which has CA Public Key Data
     * @return populated {@link CtlsCfg} object with CA Public Key data
     */
    private static CtlsCfg pktToCtlsCfg( CtlsCfg ctlsCfg, WoolworthsPktConfig pktConfig ) {
        if( ctlsCfg == null || pktConfig == null ) {
            return null;
        }

        // init/erase any existing capk list in dest obj
        ctlsCfg.setCapks(new ArrayList<>());

        // for each entry in PKT table
        for(WoolworthsPktConfig.CAPKey capKey : pktConfig.getKeyList() ) {
            // populate CtlsCapk object in a 1:1 mapping. easy

            // add and populate CAPK entry
            CtlsCfg.CtlsCapk newCapk = new CtlsCfg.CtlsCapk();
            ctlsCfg.getCapks().add(newCapk);

            String indexHex = capKey.getIndex();
            byte[] indexByte = Util.hexStringToByteArray(indexHex);
            int indexInt = indexByte[0] & 0xff; // convert single byte (will only ever be 1) to positive integer

            newCapk.setRid(capKey.getRid());
            newCapk.setIndexhex(indexHex);
            newCapk.setIndex(indexInt);
            newCapk.setModulus(capKey.getModulus());
            newCapk.setExponent(getSignificantBytesAsString(capKey.getExponent()));
            newCapk.setHash(generateHash(capKey, indexByte[0]));
            newCapk.setExpiry(capKey.getDateExpiry());
            newCapk.setTestKey(false); // for test keys, woolies have completely separate PKT tables
        }

        return ctlsCfg;
    }

    private static int amtStringToInt( String value, int defaultVal ) {
        int ret = defaultVal;
        if( value != null && value.length() >= 8 ) {
            ret = Util.fourAsciiHexBytesToInt(value);
        } else {
            Timber.e( "invalid input value" );
        }

        return ret;
    }

    /**
     * takes CTLS emv settings from EPAT data and populates CtlsCfg object
     *
     * @param epatConfig {@link WoolworthsEpatConfig} object which has CTLS - EMV Settings
     * @param ctlsCfg {@link CtlsCfg} which needs to be filled
     * @return populated {@link CtlsCfg} object with CTLS - EMV Settings
     */
    private static CtlsCfg epatToCtlsCfg( CtlsCfg ctlsCfg, WoolworthsEpatConfig epatConfig ) {

        if( ctlsCfg == null || epatConfig == null) {
            return null;
        }

        // set root elements
        // set epat table version in separate version field
        ctlsCfg.setEpatTableVersion(epatConfig.getTableVersion());

        // create aids list if it's null
        if( ctlsCfg.getAids() == null ) {
            ctlsCfg.setAids(new ArrayList<>());
        }

        // for each CONTACTLESS emv in source
        for(WoolworthsEpatConfig.Tlv_Tag_Emv_Aid_Param ctlsAidParam : epatConfig.getContactlessParamList().getParamList() ) {
            // TODO: priority, appSelIndic, displayText not used from src
            // search each origin mode (domestic or international). use domestic settings for now
            for(WoolworthsEpatConfig.Tlv_Tag_Emv_Aid_Tags originTags : ctlsAidParam.getTagList() ) {
                // 4 = domestic, 0 = international
                if( originTags.getCardOrigin().equals("4") || originTags.getCardOrigin().equals("0") ) {
                    // search current AID list for one matching AID
                    CtlsCfg.CtlsAid aid = null;
                    for(CtlsCfg.CtlsAid ctlsAid : ctlsCfg.getAids() ) {
                        if( ctlsAid.getAid() != null && ctlsAid.getAid().equals(ctlsAidParam.getAid())) {
                            // found it, update this AID record
                            aid = ctlsAid;
                            break;
                        }
                    }

                    // add new AID record if we didn't find a matching one above
                    if( aid == null ) {
                        aid = new CtlsCfg.CtlsAid();
                        ctlsCfg.getAids().add(aid);
                    }

                    // use these values
                    aid.setAid( ctlsAidParam.getAid() );
                    // DO NOT set scheme Label - this is sensitive and must come from Linkly provided cfg_ctls_emv.json file
                    aid.setCardReportedName( ctlsAidParam.getDisplayText() );
                    aid.setTermAppVersion(Integer.parseInt(originTags.getTxnTags().getApplicationVersionNumberTerm()));

                    // risk mgmt params
                    aid.setFloorLimit(amtStringToInt(originTags.getTxnTags().getTerminalFloorLimit(), 0));
                    aid.setCvmLimit(amtStringToInt(originTags.getTxnTags().getContactlessCVMLimit(), 0 ));
                    aid.setCtlsLimit(amtStringToInt(originTags.getTxnTags().getContactlessMaxLimit(), 99999999));
                    aid.setTacDefault(originTags.getTxnTags().getTermActCodeDefault());
                    aid.setTacDenial(originTags.getTxnTags().getTermActCodeDenial());
                    aid.setTacOnline(originTags.getTxnTags().getTermActCodeOnline());
                    aid.setDefaultTdol(originTags.getTxnTags().getTdolDefault());
                    aid.setDefaultDdol(originTags.getTxnTags().getDdolDefault());

                    // TTQ: woolies usually set TTQ to all zeros. Only use value from EPAT if it's correct length and not all zeros. else use cfg_ctls_emv.json table version
                    if( originTags.getTxnTags().getTerminalTransactionQualifiers().length() == 8 &&
                            !originTags.getTxnTags().getTerminalTransactionQualifiers().equals("00000000")) {
                        aid.setTTQ(originTags.getTxnTags().getTerminalTransactionQualifiers());
                    }

                    aid.setMerchCatCode(Integer.parseInt(originTags.getTxnTags().getMcTransactionCategoryCode()));
                    aid.setTermCaps(originTags.getTxnTags().getTerminalCapabilitiesCode());
                    aid.setTermAddCaps(originTags.getTxnTags().getAdditionalTerminalCapabilities());

                    String termRiskMgmtData = originTags.getTxnTags().getContactlessTerminalRiskManagementData();
                    // if data is present and multiple of 2 chars
                    StringBuilder riskMgmtData = new StringBuilder();
                    if( termRiskMgmtData.length() > 0 && termRiskMgmtData.length() % 2 == 0 ) {
                        // risk management data packed in tag 9f1d. wrap in a 9f1d tag and length
                        riskMgmtData.append( "9F1D" );
                        riskMgmtData.append( String.format("%02x", termRiskMgmtData.length()/2));
                        riskMgmtData.append( termRiskMgmtData );
                    } else {
                        // empty, pack empty string
                        Timber.d( "Packing empty string" );
                    }

                    aid.setTxnTagData(riskMgmtData.toString());
                    aid.setTermType(Integer.parseInt(epatConfig.getDefaultParam().getTags().getTerminalType()));
                    // 0=disabled, 1=enabled
                    aid.setDisabled(originTags.getTxnTags().getContactlessEnabled().equals("0"));

                    // NOTE: these source tags not used from source object Tlv_Tag_Emv_Aid_Txn_Tags
                    // threshold - not used for ctls
                    // targetPercent - not used for ctls
                    // maxTargetPercent - not used for ctls
                    // transactionType - not used from ctls
                    // standinActCode - not implemented yet, will be in future
                    // efbFloorLimit - not implemented yet, will be in future
                    // pinBypass - not implemented yet, might be? in future
                    // smallValueEnabled - not implemented yet, will be in future
                    // smallValueTerminalCapabilities - not implemented yet, will be in future
                    // contactlessEnabled - not implemented yet, we have alternate flags for this

                    // NOTE! the following tags are not set in the dest object here, and will be obtained/merged from the default cfg_ctls_emv.json file
                    // ctlsLimitNoOnDeviceCvm
                    // ctlsLimitOnDeviceCvm
                    // paypassMsrAppVersionNumber
                    // msrCvmCapsNoCvm
                    // msrCvmCapsCvm
                    // cvmCapsNoCvm
                    // cvmCapsCvm
                    // kernelConfig
                    // cardDataInputCaps
                    // securityCaps
                    // defaultUdol
                    // mobileSupportIndicator
                    // kernelId
                    // txnTagData
                    // defaultAccount
                    // binNumber
                    // mcrLimit
                    // mcrEnabled
                    // disabled <- set programatically at runtime
                    break;
                }
            }
        }
        return ctlsCfg;
    }
}