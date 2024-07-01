package com.linkly.libengine.engine.protocol.iso8583;

import com.linkly.libengine.engine.EngineManager;
import com.linkly.libmal.global.util.Util;

import java.util.List;
import java.util.Map;

import timber.log.Timber;


/**
 * Base class for As2805 response code maps.
 * The subclass needs to initialise a Map with string keys (length not fixed) &
 * {@link As2805RspCodeMap.MsgDefinition} values.
 * */
abstract class As2805RspCodeMap {
    private static final String TEXT_SYSTEM_ERROR = "SYSTEM ERROR";

    /**
     * Data class containing different string values for display, receipt & pos
     * */
    protected static class MsgDefinition{
        final String msgDisplay;
        final String msgReceipt;
        final String msgPos;
        final String responseCodePos; // 2 character POS response code (optional)
        final List<EngineManager.TransType> transTypes; // if set, then this response code is for these trans types ONLY

        /**
         * Constructor
         * @param msgDisplay Display for String, no defined limit
         * @param msgReceipt receipt text, no defined limit
         * @param msgPos text to be sent back to the pos, no defined limit
         * @param responseCodePos optional field - 2 char POS response code
         * */
        protected MsgDefinition( String msgDisplay, String msgReceipt, String msgPos, String responseCodePos, List<EngineManager.TransType> transTypes ) {
            this.msgDisplay = msgDisplay;
            this.msgReceipt = msgReceipt;
            this.msgPos = msgPos;
            this.responseCodePos = responseCodePos;
            this.transTypes = transTypes;
        }

        protected MsgDefinition( String msgDisplay, String msgReceipt, String msgPos, String responseCodePos ) {
            this( msgDisplay, msgReceipt, msgPos, responseCodePos, null );
        }

        protected MsgDefinition( String msgDisplay, String msgReceipt, String msgPos ) {
            this(msgDisplay, msgReceipt, msgPos, null);
        }

    }

    /**
     * looks for given responseCode AND optionally trans type in provided map
     *
     * @param responseCode response code to match
     * @param customerRespCodeMap input map to search
     * @param transType optional trans type to match. if null, don't match trans type
     * @return MsgDefinition object of match, or null if no match
     */
    private static MsgDefinition respCodeAndTransTypeMatch( String responseCode,
                                               Map<String, MsgDefinition> customerRespCodeMap,
                                               EngineManager.TransType transType ) {
        MsgDefinition rspCode;
        if ( responseCode != null ) {
            rspCode = customerRespCodeMap.get(responseCode);
            if (rspCode != null) {
                // we've matched the response code
                // is this a match on trans type rsp code?
                if (transType != null && rspCode.transTypes != null ) {
                    // yes it is, trans type must be in list
                    if( rspCode.transTypes.contains(transType) ) {
                        return rspCode;
                    }
                } else {
                    // no it's not, we only need to match response code
                    return rspCode;
                }
            }
        }
        return null;
    }

    /**
     * Tries to find the key in the map & return it. If not present will return the default value
     * @param responseCode key to be used
     * @param customerRespCodeMap Populated map supplied by the subclass
     * @param defaultValue to be returned to the subclass
     * @param transType optional trans type to match on. if null, match any trans type
     * @return displayText
     * */
    protected String getPosResponseCode( String responseCode,
                                         Map<String, MsgDefinition> customerRespCodeMap,
                                         String defaultValue,
                                         EngineManager.TransType transType ) {
        MsgDefinition rspCode = respCodeAndTransTypeMatch(responseCode, customerRespCodeMap, transType);
        if( rspCode != null && !Util.isNullOrEmpty(rspCode.responseCodePos) ) {
            return rspCode.responseCodePos;
        } else if(responseCode != null && responseCode.length() == 2) {
            // Fallback and check the response code value passed in.
            // Some customer resp maps don't have response code populated as they are 2 characters already (fits POS field) so just return that value.
            Timber.d("...No pos response code found but response code matches expecting POS Len, returning... response code - %s", responseCode);
            return responseCode;
        }

        // return default error if no resp code match
        Timber.e( "Unable to find POS response code for resp code %s, returning default value %s. Probably missing from map", responseCode, defaultValue );
        return defaultValue;
    }

    protected String getPosResponseCode( String responseCode,
                                         Map<String, MsgDefinition> customerRespCodeMap,
                                         String defaultValue ) {
        return getPosResponseCode( responseCode, customerRespCodeMap, defaultValue, null );
    }

    /**
     * Tries to find the key in the map & return it. If not present will return the default value
     * @param responseCode key to be used
     * @param customerRespCodeMap Populated map supplied by the subclass
     * @param defaultValue to be returned to the subclass
     * @param transType optional trans type to match on. if null, match any trans type
     * @return displayText
     * */
    protected String getResponseCodeErrorDisplay( String responseCode,
                                                  Map<String, MsgDefinition> customerRespCodeMap,
                                                  String defaultValue,
                                                  EngineManager.TransType transType ) {
        MsgDefinition rspCode = respCodeAndTransTypeMatch(responseCode, customerRespCodeMap, transType);
        if( rspCode != null && !Util.isNullOrEmpty(rspCode.msgDisplay) ) {
            return rspCode.msgDisplay;
        }

        // return default error if no resp code match
        Timber.e( "Unable to find display data for resp code %s, returning default value %s. Probably missing from map", responseCode, defaultValue );
        return defaultValue;
    }

    protected String getResponseCodeErrorDisplay( String responseCode,
                                                  Map<String, MsgDefinition> customerRespCodeMap,
                                                  String defaultValue){
        return getResponseCodeErrorDisplay(responseCode, customerRespCodeMap, defaultValue, null);
    }

    /**
     * Tries to find the key in the map & return it. If not present will return the default value ""
     * @param responseCode key to be used
     * @param customerRespCodeMap Populated map supplied by the subclass
     * @param transType optional trans type to match on. if null, match any trans type
     * @return receipt text
     * */
    protected String getResponseCodeErrorReceipt( String responseCode,
                                                  Map<String, MsgDefinition> customerRespCodeMap,
                                                  EngineManager.TransType transType ) {
        MsgDefinition rspCode = respCodeAndTransTypeMatch(responseCode, customerRespCodeMap, transType);
        if( rspCode != null && !Util.isNullOrEmpty(rspCode.msgReceipt) ) {
            return rspCode.msgReceipt;
        }

        // return no receipt data
        Timber.w( "Unable to find receipt data for resp code %s, returning default value. Probably missing from map", responseCode );
        return "";
    }

    protected String getResponseCodeErrorReceipt( String responseCode,
                                                  Map<String, MsgDefinition> customerRespCodeMap ){
        return getResponseCodeErrorReceipt( responseCode, customerRespCodeMap, null );
    }

    /**
     * Tries to find the key in the map & return it. If not present will return the default value "DECLINED" + responseCode
     * @param responseCode key to be used
     * @param customerRespCodeMap Populated map supplied by the subclass
     * @param transType optional trans type to match on. if null, match any trans type
     * @return pos Text
     * */
    protected String getResponseCodeErrorPos( String responseCode,
                                              Map<String, MsgDefinition> customerRespCodeMap,
                                              EngineManager.TransType transType ) {
        MsgDefinition rspCode = respCodeAndTransTypeMatch(responseCode, customerRespCodeMap, transType);
        if( rspCode != null && !Util.isNullOrEmpty(rspCode.msgPos) ) {
            return rspCode.msgPos;
        }

        // return default error "SYSTEM ERROR [resp code]"
        Timber.e( "Unable to find POS message for resp code %s, returning default value. Probably missing from map", responseCode );
        return TEXT_SYSTEM_ERROR + " " + responseCode;
    }

    protected String getResponseCodeErrorPos( String responseCode,
                                              Map<String, MsgDefinition> customerRespCodeMap ){
        return getResponseCodeErrorPos( responseCode, customerRespCodeMap, null );
    }
}
