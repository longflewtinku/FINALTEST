//package eft.com.eftcore;
package com.linkly.libconfig.cpat.Woolworths;

import static org.junit.Assert.assertEquals;

import com.linkly.libmal.global.config.Parse;
import com.linkly.libmal.global.config.XmlParse;

import org.junit.Test;

public class WWCardsTest {

    @Test
    public void simpleTest() {
        final String VERSION = "000003";
        final String PAYMENT_CARD = "PAYMENT CARD";
        final String INDEX = "11";
        final String BIN_NUMBER = "31";

        Parse parse = new XmlParse();

        WWCards wwCards = (WWCards) parse.parseFromString(
                "<CARDS>\n" +
                        "  <VER>" + VERSION + "</VER>\n" +
                        "  <ENTRY>\n" +
                        "    <INDEX>" + INDEX + "</INDEX>\n" +
                        "    <PCEFTBIN>" + BIN_NUMBER + "</PCEFTBIN>\n" +
                        "    <DESCR>" + PAYMENT_CARD + "</DESCR>\n" +
                        "  </ENTRY>\n" +
                "</CARDS>",
                WWCards.class );

        assertEquals( VERSION, wwCards.getVersion() );
        assertEquals( 1, wwCards.getEntryList().size() );

        WWCards.Entry entry = wwCards.getEntryList().get(0);

        assertEquals( INDEX, entry.getIndex() );
        assertEquals( BIN_NUMBER, entry.getLinklyBinNumber() );
        assertEquals( PAYMENT_CARD, entry.getAppName() );
    }

    @Test
    public void fullFileTest() {
        final String fileContents = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<CARDS>\n" +
                "  <VER>000003</VER>\n" +
                "  <ENTRY>\n" +
                "    <INDEX>00</INDEX>\n" +
                "    <PCEFTBIN>00</PCEFTBIN>\n" +
                "    <DESCR>PAYMENT CARD</DESCR>\n" +
                "  </ENTRY>\n" +
                "  <ENTRY>\n" +
                "    <INDEX>01</INDEX>\n" +
                "    <PCEFTBIN>05</PCEFTBIN>\n" +
                "    <DESCR>AMEX CARD</DESCR>\n" +
                "  </ENTRY>\n" +
                "  <ENTRY>\n" +
                "    <INDEX>02</INDEX>\n" +
                "    <PCEFTBIN>06</PCEFTBIN>\n" +
                "    <DESCR>DINERS CLUB</DESCR>\n" +
                "  </ENTRY>\n" +
                "  <ENTRY>\n" +
                "    <INDEX>03</INDEX>\n" +
                "    <PCEFTBIN>00</PCEFTBIN>\n" +
                "    <DESCR>PAYMENT CARD</DESCR>\n" +
                "  </ENTRY>\n" +
                "  <ENTRY>\n" +
                "    <INDEX>04</INDEX>\n" +
                "    <PCEFTBIN>03</PCEFTBIN>\n" +
                "    <DESCR>MASTERCARD</DESCR>\n" +
                "  </ENTRY>\n" +
                "  <ENTRY>\n" +
                "    <INDEX>05</INDEX>\n" +
                "    <PCEFTBIN>04</PCEFTBIN>\n" +
                "    <DESCR>VISA</DESCR>\n" +
                "  </ENTRY>\n" +
                "  <ENTRY>\n" +
                "    <INDEX>06</INDEX>\n" +
                "    <PCEFTBIN>09</PCEFTBIN>\n" +
                "    <DESCR>JCB</DESCR>\n" +
                "  </ENTRY>\n" +
                "  <ENTRY>\n" +
                "    <INDEX>07</INDEX>\n" +
                "    <PCEFTBIN>07</PCEFTBIN>\n" +
                "    <DESCR>GIFT CARD</DESCR>\n" +
                "  </ENTRY>\n" +
                "  <!-- Repeated as 18 for future migration -->\n" +
                "  <ENTRY>\n" +
                "    <INDEX>08</INDEX>\n" +
                "    <PCEFTBIN>01</PCEFTBIN>\n" +
                "    <DESCR>DEBIT CARD</DESCR>\n" +
                "  </ENTRY>\n" +
                "  <ENTRY>\n" +
                "    <INDEX>09</INDEX>\n" +
                "    <PCEFTBIN>07</PCEFTBIN>\n" +
                "    <DESCR>GIFT CARD</DESCR>\n" +
                "  </ENTRY>\n" +
                "  <ENTRY>\n" +
                "    <INDEX>10</INDEX>\n" +
                "    <PCEFTBIN>07</PCEFTBIN>\n" +
                "    <DESCR>ESSENTIALS CARD</DESCR>\n" +
                "  </ENTRY>\n" +
                "  <ENTRY>\n" +
                "    <INDEX>11</INDEX>\n" +
                "    <PCEFTBIN>07</PCEFTBIN>\n" +
                "    <DESCR>RETURNS CARD</DESCR>\n" +
                "  </ENTRY>\n" +
                "  <ENTRY>\n" +
                "    <INDEX>12</INDEX>\n" +
                "    <PCEFTBIN>12</PCEFTBIN>\n" +
                "    <DESCR>HI TRADE CARD</DESCR>\n" +
                "  </ENTRY>\n" +
                "  <ENTRY>\n" +
                "    <INDEX>13</INDEX>\n" +
                "    <PCEFTBIN>13</PCEFTBIN>\n" +
                "    <DESCR>WINZ CARD</DESCR>\n" +
                "  </ENTRY>\n" +
                "  <ENTRY>\n" +
                "    <INDEX>14</INDEX>\n" +
                "    <PCEFTBIN>14</PCEFTBIN>\n" +
                "    <DESCR>Onecard Visa</DESCR>\n" +
                "  </ENTRY>\n" +
                "  <ENTRY>\n" +
                "    <INDEX>15</INDEX>\n" +
                "    <PCEFTBIN>07</PCEFTBIN>\n" +
                "    <DESCR>GROCERIES ONLY</DESCR>\n" +
                "  </ENTRY>\n" +
                "  <ENTRY>\n" +
                "    <INDEX>16</INDEX>\n" +
                "    <PCEFTBIN>00</PCEFTBIN>\n" +
                "    <DESCR>CHRISTMAS CLUB</DESCR>\n" +
                "  </ENTRY>\n" +
                "  <ENTRY>\n" +
                "    <INDEX>17</INDEX>\n" +
                "    <PCEFTBIN>00</PCEFTBIN>\n" +
                "    <DESCR>WOW Prepaid</DESCR>\n" +
                "  </ENTRY>\n" +
                "  <ENTRY>\n" +
                "    <INDEX>18</INDEX>\n" +
                "    <PCEFTBIN>03</PCEFTBIN>\n" +
                "    <DESCR>BASICS CARD</DESCR>\n" +
                "  </ENTRY>\n" +
                "  <!--Was index 07 and PCEFT BIN 7-->\n" +
                "  <ENTRY>\n" +
                "    <INDEX>19</INDEX>\n" +
                "    <PCEFTBIN>30</PCEFTBIN>\n" +
                "    <DESCR>UNIONPAY</DESCR>\n" +
                "  </ENTRY>\n" +
                "  <ENTRY>\n" +
                "    <INDEX>20</INDEX>\n" +
                "    <PCEFTBIN>20</PCEFTBIN>\n" +
                "    <DESCR>STARCASH</DESCR>\n" +
                "  </ENTRY>\n" +
                "  <ENTRY>\n" +
                "    <INDEX>21</INDEX>\n" +
                "    <PCEFTBIN>20</PCEFTBIN>\n" +
                "    <DESCR>STARCARD</DESCR>\n" +
                "  </ENTRY>\n" +
                "  <ENTRY>\n" +
                "    <INDEX>22</INDEX>\n" +
                "    <PCEFTBIN>20</PCEFTBIN>\n" +
                "    <DESCR>VITALGAS</DESCR>\n" +
                "  </ENTRY>\n" +
                "  <ENTRY>\n" +
                "    <INDEX>23</INDEX>\n" +
                "    <PCEFTBIN>20</PCEFTBIN>\n" +
                "    <DESCR>MOTORPASS</DESCR>\n" +
                "  </ENTRY>\n" +
                "  <ENTRY>\n" +
                "    <INDEX>24</INDEX>\n" +
                "    <PCEFTBIN>20</PCEFTBIN>\n" +
                "    <DESCR>MOTORCHARGE</DESCR>\n" +
                "  </ENTRY>\n" +
                "  <ENTRY>\n" +
                "    <INDEX>25</INDEX>\n" +
                "    <PCEFTBIN>20</PCEFTBIN>\n" +
                "    <DESCR>FLEETCARD</DESCR>\n" +
                "  </ENTRY>\n" +
                "  <ENTRY>\n" +
                "    <INDEX>26</INDEX>\n" +
                "    <PCEFTBIN>20</PCEFTBIN>\n" +
                "    <DESCR>EG FUEL CARD C</DESCR>\n" +
                "  </ENTRY>\n" +
                "  <ENTRY>\n" +
                "    <INDEX>27</INDEX>\n" +
                "    <PCEFTBIN>7</PCEFTBIN>\n" +
                "    <DESCR>GIFT CARD</DESCR>\n" +
                "  </ENTRY>\n" +
                "  <!--NZ Gift card requires CPAT change as was EG Fuel-->\n" +
                "  <ENTRY>\n" +
                "    <INDEX>28</INDEX>\n" +
                "    <PCEFTBIN>20</PCEFTBIN>\n" +
                "    <DESCR>EG FUEL CARD W</DESCR>\n" +
                "  </ENTRY>\n" +
                "  <ENTRY>\n" +
                "    <INDEX>29</INDEX>\n" +
                "    <PCEFTBIN>20</PCEFTBIN>\n" +
                "    <DESCR>EG FUEL CARD F</DESCR>\n" +
                "  </ENTRY>\n" +
                "  <!-- New index for 27 -->\n" +
                "  <ENTRY>\n" +
                "    <INDEX>79</INDEX>\n" +
                "    <PCEFTBIN>79</PCEFTBIN>\n" +
                "    <DESCR>QC GIFT CARD</DESCR>\n" +
                "  </ENTRY>\n" +
                "  <ENTRY>\n" +
                "    <INDEX>80</INDEX>\n" +
                "    <PCEFTBIN>79</PCEFTBIN>\n" +
                "    <DESCR>QC GIFT CARD NZ</DESCR>\n" +
                "  </ENTRY>\n" +
                "</CARDS>";

        Parse parse = new XmlParse();
        WWCards wwCards = (WWCards) parse.parseFromString( fileContents, WWCards.class );

        assertEquals( "000003", wwCards.getVersion() );
        assertEquals( 32, wwCards.getEntryList().size() );

        WWCards.Entry entry = wwCards.getEntryList().get(31);

        assertEquals( "80", entry.getIndex() );
        assertEquals( "79", entry.getLinklyBinNumber() );
        assertEquals( "QC GIFT CARD NZ", entry.getAppName() );
    }
}
