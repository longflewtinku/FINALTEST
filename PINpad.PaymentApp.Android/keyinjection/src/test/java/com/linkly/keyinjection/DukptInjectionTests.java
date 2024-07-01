package com.linkly.keyinjection;

import static com.linkly.keyinjection.DukptKeys.generateInitialKey;
import static org.junit.Assert.assertEquals;

import org.junit.Test;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;


/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class DukptInjectionTests {
    @Test
    public void test_initialKeyDerivation() throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        KeySet.KeyVal keyVal = new KeySet.KeyVal();
        keyVal.dukptRandomTrsm = true;
        keyVal.dukptIssuerId = "628000";
        keyVal.dukptBdkIdx = "02";
        keyVal.dukptVendorId = "16";
        String initialKey = generateInitialKey("A423085486C762D0BC6E0468A70B9894", "62800002160000200000");
        assertEquals("9555C5DBC7BCB74843004AFAAC6D7BEE", initialKey);
    }

    @Test
    public void test_generateKsn() {
        KeySet.KeyVal keyVal = new KeySet.KeyVal();
        keyVal.dukptRandomTrsm = true;
        keyVal.dukptIssuerId = "628000";
        keyVal.dukptBdkIdx = "02";
        keyVal.dukptVendorId = "16";

        String ksnStr = DukptKeys.generateKsn(keyVal);
        assertEquals("6280000216", ksnStr.substring(0,10));
        assertEquals("00001", ksnStr.substring(15));
    }

}