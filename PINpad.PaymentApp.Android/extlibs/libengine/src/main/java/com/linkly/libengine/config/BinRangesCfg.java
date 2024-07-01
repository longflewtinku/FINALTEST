package com.linkly.libengine.config;

import com.linkly.libbins.BinRanges;
import com.linkly.libconfig.cpat.CardProductCfg;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

public class BinRangesCfg extends BinRanges {
    List<BinRange> cardBins = new ArrayList<>();
    List<BinRange> refundDisabledBins = new ArrayList<>();
    List<BinRange> cashbackBins = new ArrayList<>();
    List<BinRange> smsBins = new ArrayList<>();


    public void initialiseBinRanges(PayCfg config) {
        List<CardProductCfg> cards = config.getCards();
        if (cards == null) {
            return;
        }

        for (int i = 0; i < cards.size(); i++) {
            if (cards.get(i) == null){ //in case one of the items happen to be null
                continue;
            }
            String iinRange = config.getIinranges(i);
            addIinRange(cardBins, i, iinRange, cards.get(i).getName());
        }
    }

    public boolean isRefundDisabled(PayCfg config, String track2data) {

        if (searchBinRanges(config, refundDisabledBins, track2data) != -1) {
            Timber.i("Refund disabled");
            return true;
        }
        return false;
    }

    public boolean isSms(PayCfg config, String track2data) {

        if (searchBinRanges(config, smsBins, track2data) != -1) {
            return true;
        }
        return false;
    }

    public int getCardsCfgIndex(PayCfg config, String track2data) {
        return searchBinRanges(config, cardBins, track2data);
    }

    public CardProductCfg getCardProductCfg(PayCfg config, int index) {
        List<CardProductCfg> cards = config.getCards();
        if (index >= 0 && cards != null && index < cards.size()) {
            return cards.get(index);
        }
        return null;
    }

    public CardProductCfg getCardFromBin(PayCfg config, int binNumber) {
        List<CardProductCfg> cards = config.getCards();
        for ( CardProductCfg card : cards) {
            if (card.getBinNumber() == binNumber) {
                return card;
            }
        }
        return null;
    }

    public String removeMaskedData(String binData) {
        if (!binData.contains("*")) {
            return binData;
        }
        return binData.substring(0, binData.indexOf("*"));
    }

    public int searchBinRanges(PayCfg config, List<BinRange> binRanges, String binData) {
        if (binData == null) {
            Timber.e("Card range check - no match found for card prefix, input binData is null");
            return -1;
        }

        double d;
        try {
            binData = removeMaskedData(binData);
            String binRange = makeXDigitsExact(binData, "1");
            d = Double.parseDouble(binRange);
        } catch (Exception e) {
            Timber.w(e);
            return -1;
        }

        for (BinRange b : binRanges) {
            if (b != null && d >= b.start && d <= b.finish) {
                Timber.e("Matched BIN: " + b.index + " " + b.value + " RANGE:" + fmt(b.start) + ":" + fmt(b.finish) + " With: " + fmt(d));
                if (getCardProductCfg(config, b.index) != null) {
                    return b.index;
                }
            }
        }

        Timber.e("Card range check - no match found for card prefix %s", binData);
        return -1;
    }
}

