package com.linkly.libengine.config;

import android.text.TextUtils;

import com.linkly.libbins.BinRanges;
import com.linkly.libconfig.cpat.CardProductCfg;
import com.linkly.libmal.global.config.ConfigExceptions;
import com.linkly.libmal.global.config.JSONParse;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

public class BlacklistCfg extends BinRanges {

    private static BlacklistCfg mInstance;

    List<CardProductCfg> cards = new ArrayList<>();
    List<BinRanges.BinRange> blacklistedBins = new ArrayList<>();

    /**
     * @return An empty initialized {@link BlacklistCfg} instance
     */
    public static BlacklistCfg getInstance() {
        if (mInstance == null) {
            mInstance = new BlacklistCfg();
        }

        return mInstance;
    }

    public static BlacklistCfg parse(String blacklistFile) throws ConfigExceptions.ParseErrorException {
        // initialize the instance if not already done
        getInstance();

        if (TextUtils.isEmpty(blacklistFile)) {
            Timber.i("Blacklist file not available");
            return mInstance;
        }

        try {
            Timber.i("Parsing Blacklist config");
            JSONParse jsonParse = new JSONParse();

            mInstance = jsonParse.parse(blacklistFile, BlacklistCfg.class);

            if (mInstance.cards == null) {
                Timber.i("No CardProduct info in blacklist file");
            } else {
                Timber.i("%d number of cards parsed", mInstance.cards.size());

                List<BinRanges.BinRange> bins;
                // extract out comma separated bin ranges to a list of bin ranges
                for (CardProductCfg cardProductCfg : mInstance.cards) {
                    bins = new ArrayList<>();
                    mInstance.addIinRange(bins, 0, cardProductCfg.getIinRange(), cardProductCfg.getName());
                    mInstance.blacklistedBins.addAll(bins);
                }
            }
        } catch (ConfigExceptions.FileErrorException e) {
            Timber.i("Blacklist config - %s : File not found", blacklistFile);
        }

        return mInstance;
    }

    public boolean isBlacklistedCard(PayCfg config, BinRangesCfg binRangesCfg, String track2Data) {
        if (binRangesCfg.searchBinRanges(config, blacklistedBins, track2Data) != -1) {
            Timber.i("Card is blacklisted");
            return true;
        }
        return false;
    }
}