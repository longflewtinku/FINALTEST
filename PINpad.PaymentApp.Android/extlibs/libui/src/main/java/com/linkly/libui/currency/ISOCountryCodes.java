package com.linkly.libui.currency;

import java.util.ArrayList;

public class ISOCountryCodes {
    private static ISOCountryCodes instance;
    private static ArrayList<CountryCode> knownCodes;

    public static ISOCountryCodes getInstance() {

        if (instance != null) {
            return instance;
        }

        instance = new ISOCountryCodes();

        /*Add the codes */
        knownCodes = new ArrayList<CountryCode>();
        knownCodes.add(new CountryCode("AED", "784", "2", "Dirham"));
        knownCodes.add(new CountryCode("AFN", "971", "2", "Afghani"));
        knownCodes.add(new CountryCode("ALL", "008", "2", "Lek"));
        knownCodes.add(new CountryCode("AMD", "051", "2", "Armenian Dram"));
        knownCodes.add(new CountryCode("ANG", "532", "2", "Netherl Guilder"));
        knownCodes.add(new CountryCode("AOA", "973", "2", "Kwanza"));
        knownCodes.add(new CountryCode("ARS", "032", "2", "Argentine Peso"));
        knownCodes.add(new CountryCode("AUD", "036", "2", "Australian Dollar", "$", "dollar", "dollars", "cent", "cents"));
        knownCodes.add(new CountryCode("AWG", "533", "2", "Aruban Guilder"));
        knownCodes.add(new CountryCode("AZN", "944", "2", "Azerbaijn Manat"));
        knownCodes.add(new CountryCode("BAM", "977", "2", "Convert. Marks"));
        knownCodes.add(new CountryCode("BBD", "052", "2", "Barbados Dollar"));
        knownCodes.add(new CountryCode("BDT", "050", "2", "Bangladesh Taka"));
        knownCodes.add(new CountryCode("BGN", "975", "2", "Bulgarian Lev"));
        knownCodes.add(new CountryCode("BHD", "048", "3", "Bahraini Dinar"));
        knownCodes.add(new CountryCode("BIF", "108", "0", "Burundian Franc"));
        knownCodes.add(new CountryCode("BMD", "060", "2", "Bermuda Dollar"));
        knownCodes.add(new CountryCode("BND", "096", "2", "Brunei Dollar"));
        knownCodes.add(new CountryCode("BOB", "068", "2", "Boliviano"));
        knownCodes.add(new CountryCode("BRL", "986", "2", "Brazilian Real"));
        knownCodes.add(new CountryCode("BSD", "044", "2", "Bahamian Dollar"));
        knownCodes.add(new CountryCode("BTN", "064", "2", "Ngultrum"));
        knownCodes.add(new CountryCode("BWP", "072", "2", "Pula"));
        knownCodes.add(new CountryCode("BYR", "974", "0", "Belarussn Ruble"));
        knownCodes.add(new CountryCode("BZD", "084", "2", "Belize Dollar"));
        knownCodes.add(new CountryCode("CAD", "124", "2", "Canadian Dollar"));
        knownCodes.add(new CountryCode("CDF", "976", "2", "Franc Congolais"));
        knownCodes.add(new CountryCode("CHE", "947", "2", "WIR euro"));
        knownCodes.add(new CountryCode("CHF", "756", "2", "Swiss Franc"));
        knownCodes.add(new CountryCode("CHW", "948", "2", "WIR Franc"));
        knownCodes.add(new CountryCode("CLP", "152", "2", "Chilean Peso"));
        knownCodes.add(new CountryCode("CNY", "156", "2", "Renminbi"));
        knownCodes.add(new CountryCode("COP", "170", "2", "Colombian Peso"));
        knownCodes.add(new CountryCode("COU", "970", "2", "UniDeValor Real"));
        knownCodes.add(new CountryCode("CRC", "188", "2", "CostaRica Colon"));
        knownCodes.add(new CountryCode("CUP", "192", "2", "Cuban Peso"));
        knownCodes.add(new CountryCode("CVE", "132", "2", "Cp Verde Escudo"));
        knownCodes.add(new CountryCode("CZK", "203", "2", "Czech Koruna"));
        knownCodes.add(new CountryCode("DJF", "262", "0", "Djibouti Franc"));
        knownCodes.add(new CountryCode("DKK", "208", "2", "Danish Krone"));
        knownCodes.add(new CountryCode("DOP", "214", "2", "Dominican Peso"));
        knownCodes.add(new CountryCode("DZD", "012", "2", "Algerian Dinar"));
        knownCodes.add(new CountryCode("EEK", "233", "2", "Kroon"));
        knownCodes.add(new CountryCode("EGP", "818", "2", "Egyptian Pound"));
        knownCodes.add(new CountryCode("ERN", "232", "2", "Nakfa"));
        knownCodes.add(new CountryCode("ETB", "230", "2", "Ethiopian Birr"));
        knownCodes.add(new CountryCode("EUR", "978", "2", "Euro", "€", "Euro", "Euros", "Cent", "Cents"));
        knownCodes.add(new CountryCode("EUR", "372", "2", "Euro", "€", "Euro", "Euros", "Cent", "Cents"));
        knownCodes.add(new CountryCode("FJD", "242", "2", "Fiji Dollar"));
        knownCodes.add(new CountryCode("FKP", "238", "2", "Falkland Pound"));
        knownCodes.add(new CountryCode("GBP", "826", "2", "Pound Sterling", "£",  "Pound", "Pounds", "Pence", "Pence"));
        knownCodes.add(new CountryCode("GEL", "981", "2", "Lari"));
        knownCodes.add(new CountryCode("GHS", "936", "2", "Cedi"));
        knownCodes.add(new CountryCode("GIP", "292", "2", "Gibraltar Pound"));
        knownCodes.add(new CountryCode("GMD", "270", "2", "Dalasi"));
        knownCodes.add(new CountryCode("GNF", "324", "0", "Guinea Franc"));
        knownCodes.add(new CountryCode("GTQ", "320", "2", "Quetzal"));
        knownCodes.add(new CountryCode("GYD", "328", "2", "Guyana Dollar"));
        knownCodes.add(new CountryCode("HKD", "344", "2", "Hong Kong Dollr"));
        knownCodes.add(new CountryCode("HNL", "340", "2", "Lempira"));
        knownCodes.add(new CountryCode("HRK", "191", "2", "Croatian Kuna"));
        knownCodes.add(new CountryCode("HTG", "332", "2", "Haiti Gourde"));
        knownCodes.add(new CountryCode("HUF", "348", "2", "Forint"));
        knownCodes.add(new CountryCode("IDR", "360", "2", "Rupiah"));
        knownCodes.add(new CountryCode("ILS", "376", "2", "Israeli NSheqel"));
        knownCodes.add(new CountryCode("INR", "356", "2", "Indian Rupee"));
        knownCodes.add(new CountryCode("IQD", "368", "3", "Iraqi Dinar"));
        knownCodes.add(new CountryCode("IRR", "364", "2", "Iranian Rial"));
        knownCodes.add(new CountryCode("ISK", "352", "2", "Iceland Krona", "kr", "Krona", "Kronas", "", ""));
        knownCodes.add(new CountryCode("JMD", "388", "2", "Jamaican Dollar"));
        knownCodes.add(new CountryCode("JOD", "400", "3", "Jordanian Dinar"));
        knownCodes.add(new CountryCode("JPY", "392", "0", "Japanese Yen"));
        knownCodes.add(new CountryCode("KES", "404", "2", "Kenyan Shilling"));
        knownCodes.add(new CountryCode("KGS", "417", "2", "Som"));
        knownCodes.add(new CountryCode("KHR", "116", "2", "Riel"));
        knownCodes.add(new CountryCode("KMF", "174", "0", "Comoro Franc"));
        knownCodes.add(new CountryCode("KPW", "408", "2", "Nth Korean Won"));
        knownCodes.add(new CountryCode("KRW", "410", "0", "Sth Korean Won"));
        knownCodes.add(new CountryCode("KWD", "414", "3", "Kuwaiti Dinar"));
        knownCodes.add(new CountryCode("KYD", "136", "2", "Cayman Is Dollr"));
        knownCodes.add(new CountryCode("KZT", "398", "2", "Tenge"));
        knownCodes.add(new CountryCode("LAK", "418", "2", "Kip"));
        knownCodes.add(new CountryCode("LBP", "422", "2", "Lebanese Pound"));
        knownCodes.add(new CountryCode("LKR", "144", "2", "Sri Lanka Rupee"));
        knownCodes.add(new CountryCode("LRD", "430", "2", "Liberian Dollar"));
        knownCodes.add(new CountryCode("LSL", "426", "2", "Loti"));
        knownCodes.add(new CountryCode("LTL", "440", "2", "Lithuann Litas"));
        knownCodes.add(new CountryCode("LVL", "428", "2", "Latvian Lats"));
        knownCodes.add(new CountryCode("LYD", "434", "3", "Libyan Dinar"));
        knownCodes.add(new CountryCode("MAD", "504", "2", "Moroccan Dirham"));
        knownCodes.add(new CountryCode("MDL", "498", "2", "Moldovan Leu"));
        knownCodes.add(new CountryCode("MKD", "807", "2", "Denar"));
        knownCodes.add(new CountryCode("MMK", "104", "2", "Kyat"));
        knownCodes.add(new CountryCode("MNT", "496", "2", "Tugrik"));
        knownCodes.add(new CountryCode("MOP", "446", "2", "Pataca"));
        knownCodes.add(new CountryCode("MUR", "480", "2", "Mauritius Rupee"));
        knownCodes.add(new CountryCode("MVR", "462", "2", "Rufiyaa"));
        knownCodes.add(new CountryCode("MWK", "454", "2", "Kwacha"));
        knownCodes.add(new CountryCode("MXN", "484", "2", "Mexican Peso"));
        knownCodes.add(new CountryCode("MYR", "458", "2", "Malaysian Ringgit"));
        knownCodes.add(new CountryCode("MZN", "943", "2", "Metical"));
        knownCodes.add(new CountryCode("NAD", "516", "2", "Namibian Dollar"));
        knownCodes.add(new CountryCode("NGN", "566", "2", "Naira"));
        knownCodes.add(new CountryCode("NIO", "558", "2", "Cordoba Oro"));
        knownCodes.add(new CountryCode("NOK", "578", "2", "Norwegian Krone"));
        knownCodes.add(new CountryCode("NPR", "524", "2", "Nepalese Rupee"));
        knownCodes.add(new CountryCode("NZD", "554", "2", "New Zealand Dollar", "$", "dollar", "dollars", "cent", "cents"));
        knownCodes.add(new CountryCode("OMR", "512", "3", "Rial Omani"));
        knownCodes.add(new CountryCode("PAB", "590", "2", "Balboa"));
        knownCodes.add(new CountryCode("PEN", "604", "2", "Nuevo Sol"));
        knownCodes.add(new CountryCode("PGK", "598", "2", "Kina"));
        knownCodes.add(new CountryCode("PHP", "608", "2", "Philippine Peso"));
        knownCodes.add(new CountryCode("PKR", "586", "2", "Pakistan Rupee"));
        knownCodes.add(new CountryCode("PLN", "985", "2", "Zloty"));
        knownCodes.add(new CountryCode("PYG", "600", "0", "Guarani"));
        knownCodes.add(new CountryCode("QAR", "634", "2", "Qatari Rial"));
        knownCodes.add(new CountryCode("RON", "946", "2", "Romanian NewLeu"));
        knownCodes.add(new CountryCode("RSD", "941", "2", "Serbian Dinar"));
        knownCodes.add(new CountryCode("RUB", "643", "2", "Russian Ruble"));
        knownCodes.add(new CountryCode("RWF", "646", "0", "Rwanda Franc"));
        knownCodes.add(new CountryCode("SAR", "682", "2", "Saudi Riyal"));
        knownCodes.add(new CountryCode("SBD", "090", "2", "Solomon Dollar"));
        knownCodes.add(new CountryCode("SCR", "690", "2", "SeychellesRupee"));
        knownCodes.add(new CountryCode("SDG", "938", "2", "Sudanese Pound"));
        knownCodes.add(new CountryCode("SEK", "752", "2", "Swedish Krona"));
        knownCodes.add(new CountryCode("SGD", "702", "2", "Singapre Dollar"));
        knownCodes.add(new CountryCode("SHP", "654", "2", "St.Helena Pound"));
        knownCodes.add(new CountryCode("SKK", "703", "2", "Slovak Koruna"));
        knownCodes.add(new CountryCode("SLL", "694", "2", "Leone"));
        knownCodes.add(new CountryCode("SOS", "706", "2", "Somali Shilling"));
        knownCodes.add(new CountryCode("SRD", "968", "2", "Surinam Dollar"));
        knownCodes.add(new CountryCode("STD", "678", "2", "Dobra"));
        knownCodes.add(new CountryCode("SYP", "760", "2", "Syrian Pound"));
        knownCodes.add(new CountryCode("SZL", "748", "2", "Lilangeni"));
        knownCodes.add(new CountryCode("THB", "764", "2", "Baht"));
        knownCodes.add(new CountryCode("TJS", "972", "2", "Somoni"));
        knownCodes.add(new CountryCode("TMM", "795", "2", "Manat"));
        knownCodes.add(new CountryCode("TND", "788", "3", "Tunisian Dinar"));
        knownCodes.add(new CountryCode("TOP", "776", "2", "Pa'anga"));
        knownCodes.add(new CountryCode("TRY", "949", "2", "NewTurkish Lira"));
        knownCodes.add(new CountryCode("TTD", "780", "2", "T and T Dollar"));
        knownCodes.add(new CountryCode("TWD", "901", "2", "Taiwan Dollar"));
        knownCodes.add(new CountryCode("TZS", "834", "2", "Tanzanian Shill"));
        knownCodes.add(new CountryCode("UAH", "980", "2", "Hryvnia"));
        knownCodes.add(new CountryCode("UGX", "800", "2", "Uganda Shilling"));
        knownCodes.add(new CountryCode("USD", "840", "2", "US Dollar",  "$", "Dollar", "Dollars", "Cent", "Cents"));
        knownCodes.add(new CountryCode("UYU", "858", "2", "Peso Uruguayo"));
        knownCodes.add(new CountryCode("UZS", "860", "2", "Uzbekistan Som"));
        knownCodes.add(new CountryCode("VEF", "937", "2", "Bolivar Fuerte"));
        knownCodes.add(new CountryCode("VND", "704", "0", "Vietnamese Dong"));
        knownCodes.add(new CountryCode("VUV", "548", "0", "Vatu"));
        knownCodes.add(new CountryCode("WST", "882", "2", "Samoan Tala"));
        knownCodes.add(new CountryCode("XAF", "950", "0", "CFA franc BEAC"));
        knownCodes.add(new CountryCode("XCD", "951", "2", "E.Carib Dollar"));
        knownCodes.add(new CountryCode("XOF", "952", "0", "CFA Franc BCEAO"));
        knownCodes.add(new CountryCode("XPF", "953", "0", "CFP Franc"));
        knownCodes.add(new CountryCode("YER", "886", "2", "Yemeni Rial"));
        knownCodes.add(new CountryCode("ZAR", "710", "2", "S.African Rand", "R", "Rand", "Rands", "Cent", "Cents"));
        knownCodes.add(new CountryCode("ZMK", "894", "2", "Kwacha"));
        knownCodes.add(new CountryCode("ZMW", "967", "2", "Kwacha"));
        knownCodes.add(new CountryCode("ZWD", "716", "2", "Zimbabwe Dollar"));
        knownCodes.add(new CountryCode("MLT", "470", "2", "Malta", "€", "Euro", "Euros", "Cent", "Cents"));



        return instance;
    }

    public int getDecimalsFrom3Num(String num) {
        CountryCode country = getCountryFromInt( Integer.parseInt(num) );
        if( country != null ) {
            return country.getDecimals();
        }
        return 2;
    }

    public CountryCode getCountryFrom3Num(String num) {
        return getCountryFromInt( Integer.parseInt(num) );
    }

    public CountryCode getCountryFromInt(int num) {
        CountryCode country;

        for (int i = 0; i < knownCodes.size(); i++) {
            country = knownCodes.get(i);
            int numCode = Integer.valueOf(country.getNumCode());
            if (numCode == num) {
                return country;
            }
        }
        return null;
    }

    public String getAlphaCodeFrom3Num(String num) {
        CountryCode country = getCountryFromInt( Integer.parseInt(num) );
        if( country != null ) {
            return country.getAlphaCode();
        }
        return "";
    }


    public String getGetNumFromAlpha(String alpha) {
        CountryCode country;

        for (int i = 0; i < knownCodes.size(); i++) {
            country = knownCodes.get(i);
            String numCode = country.getAlphaCode();
            if (numCode.compareTo(alpha) == 0) {
                return country.getNumCode();
            }
        }
        return "";
    }

}
