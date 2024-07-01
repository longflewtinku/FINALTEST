package com.linkly.libconfig.cpat.Woolworths;

import static com.linkly.libconfig.cpat.PSIIssuerDefines.PSI_ISSUER_AMEX;
import static com.linkly.libconfig.cpat.PSIIssuerDefines.PSI_ISSUER_DINERS;
import static com.linkly.libconfig.cpat.PSIIssuerDefines.PSI_ISSUER_EFTPOS;
import static com.linkly.libconfig.cpat.PSIIssuerDefines.PSI_ISSUER_JCB;
import static com.linkly.libconfig.cpat.PSIIssuerDefines.PSI_ISSUER_MASTERCARD;
import static com.linkly.libconfig.cpat.PSIIssuerDefines.PSI_ISSUER_UNIONPAY;
import static com.linkly.libconfig.cpat.PSIIssuerDefines.PSI_ISSUER_UNKNOWN;
import static com.linkly.libconfig.cpat.PSIIssuerDefines.PSI_ISSUER_VISA;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.linkly.libconfig.cpat.CardProductList;
import com.linkly.libmal.IMal;
import com.linkly.libmal.IMalFile;
import com.linkly.libmal.MalFactory;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

public class WoolworthsCardProductListTest {



    @Test
    public void testConfigParseForPSI() {
        WoolworthsCPATParser parser = mock(WoolworthsCPATParser.class);

        List<WoolworthsCPATEntry> entryList = new ArrayList<>();
        entryList.add( new WoolworthsCPATEntry("1800FFFFF 01 0 005008000300 00"));
        entryList.add( new WoolworthsCPATEntry("2131FFFFF 02 0 005008000300 00"));
        entryList.add( new WoolworthsCPATEntry("22073000F 03 0 905000000300 41"));
        entryList.add( new WoolworthsCPATEntry("2221FFFFF 04 0 805000000300 21"));
        entryList.add( new WoolworthsCPATEntry("2221FFFFF 05 0 805000000300 21"));
        entryList.add( new WoolworthsCPATEntry("2221FFFFF 06 0 805000000300 21"));
        entryList.add( new WoolworthsCPATEntry("2221FFFFF 07 0 805000000300 21"));
        entryList.add( new WoolworthsCPATEntry("2221FFFFF 99 0 805000000300 21"));

        when(parser.getProcessingParametersRecord()).thenReturn(
                new ProcessingParametersRecord("000021142 18 0 009002009641 99")
        );

        when(parser.getWoolworthsCPATEntries()).thenReturn(
            entryList
        );

        List<WWCards.Entry> cardEntries = new ArrayList<>();

        cardEntries.add(new WWCards.Entry("01", "09", "JCB"));
        cardEntries.add(new WWCards.Entry("02", "03", "MASTERCARD"));
        cardEntries.add(new WWCards.Entry("03", "04", "VISA"));
        cardEntries.add(new WWCards.Entry("04", "05", "AMEX"));
        cardEntries.add(new WWCards.Entry("05", "06", "DINERS"));
        cardEntries.add(new WWCards.Entry("06", "31", "UNION PAY"));
        cardEntries.add(new WWCards.Entry("07", "01", "EFTPOS"));

        WWCards cards = mock(WWCards.class);
        when(cards.getEntryList()).thenReturn(cardEntries);

        WoolworthsCardProductList list = new WoolworthsCardProductList(parser, cards);
        CardProductList listConfig = list.getConfig();

        assertEquals(PSI_ISSUER_JCB, listConfig.cards.get(0).getPsi());
        assertEquals(PSI_ISSUER_MASTERCARD, listConfig.cards.get(1).getPsi());
        assertEquals(PSI_ISSUER_VISA, listConfig.cards.get(2).getPsi());
        assertEquals(PSI_ISSUER_AMEX, listConfig.cards.get(3).getPsi());
        assertEquals(PSI_ISSUER_DINERS, listConfig.cards.get(4).getPsi());
        assertEquals(PSI_ISSUER_UNIONPAY, listConfig.cards.get(5).getPsi());
        assertEquals(PSI_ISSUER_EFTPOS, listConfig.cards.get(6).getPsi());
        assertEquals(PSI_ISSUER_UNKNOWN, listConfig.cards.get(7).getPsi());
    }
}