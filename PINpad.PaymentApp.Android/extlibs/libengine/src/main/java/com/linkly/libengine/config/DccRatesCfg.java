package com.linkly.libengine.config;

import com.linkly.libmal.global.config.JSONParse;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

public class DccRatesCfg {

    private static final String TAG = "DccRatesCfg";
    private static DccRatesCfg ourInstance = new DccRatesCfg();
    private String version;

    /***************************************************************/
    /* debug all of the config */
    private String errorIndicator;
    private String baseCurrencyCode ;
    private String timestamp;
    private long lTimestamp;
    private String markUpPercentage;


    private List<CurrencyRate> currencyRates = new ArrayList<>();

    private DccRatesCfg() {
    }

    public static DccRatesCfg getInstance() {
        if (ourInstance == null) {
            ourInstance = new DccRatesCfg();
        }

        return ourInstance;
    }

    public DccRatesCfg parse() {
        try {
            JSONParse j = new JSONParse();
            ourInstance = (DccRatesCfg)j.parse("dcc_rates.json", DccRatesCfg.class);

        } catch (Exception e) {
            Timber.w(e);
            ourInstance = null;
        }
        return ourInstance;

    }

    public String getVersion() {
        return this.version;
    }

    public String getErrorIndicator() {
        return this.errorIndicator;
    }

    public String getBaseCurrencyCode() {
        return this.baseCurrencyCode;
    }

    public String getTimestamp() {
        return this.timestamp;
    }

    public long getLTimestamp() {
        return this.lTimestamp;
    }

    public String getMarkUpPercentage() {
        return this.markUpPercentage;
    }

    public List<CurrencyRate> getCurrencyRates() {
        return this.currencyRates;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setErrorIndicator(String errorIndicator) {
        this.errorIndicator = errorIndicator;
    }

    public void setBaseCurrencyCode(String baseCurrencyCode) {
        this.baseCurrencyCode = baseCurrencyCode;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public void setLTimestamp(long lTimestamp) {
        this.lTimestamp = lTimestamp;
    }

    public void setMarkUpPercentage(String markUpPercentage) {
        this.markUpPercentage = markUpPercentage;
    }

    public void setCurrencyRates(List<CurrencyRate> currencyRates) {
        this.currencyRates = currencyRates;
    }

    public static class CurrencyRate {
        private String currencyCode;
        private String conversionExponent;
        private String conversionRate;

        public String getCurrencyCode() {
            return this.currencyCode;
        }

        public String getConversionExponent() {
            return this.conversionExponent;
        }

        public String getConversionRate() {
            return this.conversionRate;
        }

        public void setCurrencyCode(String currencyCode) {
            this.currencyCode = currencyCode;
        }

        public void setConversionExponent(String conversionExponent) {
            this.conversionExponent = conversionExponent;
        }

        public void setConversionRate(String conversionRate) {
            this.conversionRate = conversionRate;
        }
    }

}