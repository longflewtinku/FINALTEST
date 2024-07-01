package com.positive;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import com.linkly.libpositive.wrappers.Surcharge;

import org.junit.Test;

public class TestTagDataFromPOS {

    private void TestTagDataFromPosInvalid(String input) {
        com.linkly.libpositive.wrappers.TagDataFromPOS parsed = com.linkly.libpositive.wrappers.TagDataFromPOS.builder(input);
        assertNull(parsed);
    }

    @Test
    public void TestTagDataFromPOSValid() {
        String json = "{\"OPR\":\"0|USER\",\"UID\":\"59fb003f4bc243d39577e07b13f99b2a\",\"VER\":\"1.7.4.2\",\"HUB\":\"1\",\"NME\":\"Linkly Test POS\",\"AMT\":\"1000\",\"TIP\":\"123\",\"PCM\":\"10000000\",\"SKU\":\"0fc64291e87e45908c81bd1a773fdbb9\",\"VND\":\"ab1299fcec39480abab54390175c7535\"}";

        com.linkly.libpositive.wrappers.TagDataFromPOS parsed = com.linkly.libpositive.wrappers.TagDataFromPOS.builder(json);

        assertEquals("0fc64291e87e45908c81bd1a773fdbb9", parsed.getSKU());
        assertNull(parsed.getMMM());
    }

    @Test
    public void TestTagDataFromPOSBroken() {
        TestTagDataFromPosInvalid("{\"OPR\":\"0|USER\",\"UID\":\"59fb003f4bc243d39577e07b13f99b2a\",VER1.7.4.2\",\"HUB\":\"1\",\"NME\":\"Linkly Test POS\",\"AMT\":\"1000\",\"TIP\":\"123\",\"PCM\":\"10000000\",\"SKU\":\"0fc64291e87e45908c81bd1a773fdbb9\",\"VND\":\"ab1299fcec39480abab54390175c7535\"}");
    }

    @Test
    public void TestTagDataFromPOSNullData() {
        TestTagDataFromPosInvalid(null);
    }

    @Test
    public void TestTagDataFromPOSSurchargeDataValid() {
        String json = "{\"OPR\":\"0|USER\",\"UID\":\"59fb003f4bc243d39577e07b13f99b2a\",\"VER\":\"1.7.4.2\",\"HUB\":\"1\",\"NME\":\"Linkly Test POS\",\"AMT\":\"1000\",\"TIP\":\"123\",\"PCM\":\"10000000\",\"SKU\":\"0fc64291e87e45908c81bd1a773fdbb9\",\"VND\":\"ab1299fcec39480abab54390175c7535\",\"SC2\":[{\"b\":\"01\",\"t\":\"$\",\"v\":20},{\"b\":\"03\",\"v\":150},{\"b\":\"29\",\"v\":50},{\"b\":\"04\",\"v\":150},{\"b\":\"28\",\"v\":50},{\"b\":\"05\",\"v\":200},{\"b\":\"06\",\"v\":200},{\"b\":\"30\",\"v\":200}]}";
        com.linkly.libpositive.wrappers.TagDataFromPOS parsed = com.linkly.libpositive.wrappers.TagDataFromPOS.builder(json);
        assertNotNull(parsed);
        Surcharge[] array = parsed.getSC2();
        assertNotNull(array);
        assertEquals(8, array.length);
        Surcharge compare[] = new Surcharge[8];
        compare[0] = new Surcharge();
        compare[0].setB("01");
        compare[0].setT("$");
        compare[0].setV("20");
        compare[1] = new Surcharge();
        compare[1].setB("03");
        compare[1].setV("150");
        compare[2] = new Surcharge();
        compare[2].setB("29");
        compare[2].setV("50");
        compare[3] = new Surcharge();
        compare[3].setB("04");
        compare[3].setV("150");
        compare[4] = new Surcharge();
        compare[4].setB("28");
        compare[4].setV("50");
        compare[5] = new Surcharge();
        compare[5].setB("05");
        compare[5].setV("200");
        compare[6] = new Surcharge();
        compare[6].setB("06");
        compare[6].setV("200");
        compare[7] = new Surcharge();
        compare[7].setB("30");
        compare[7].setV("200");

        for(int i =0; i < array.length; ++i) {
            assertEquals(compare[i].getB(), array[i].getB());
            assertEquals(compare[i].getT(), array[i].getT());
            assertEquals(compare[i].getV(), array[i].getV());
        }

    }

    @Test
    public void TestTagDataFromPOSSurchargeDataInvalid() {
        TestTagDataFromPosInvalid("{\"SC2\":\"0|USER\"}");
    }
}
