package com.linkly.payment.openisojtests;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import com.linkly.libengine.engine.protocol.svfe.openisoj.Ministatement;

import org.junit.Test;

import java.util.Hashtable;
import java.util.List;

public class MinistatementTests {
    @Test
    public void testUnpack() {
        String msStr = "DATE_TIME|SEQ_NR|TRAN_TYPE|TRAN_AMOUNT|CURR_CODE~20121104120000|000000|01|005|840~20121104120000|000000|01|013|840~20121104120000|000000|42|5000|840~";
        Ministatement ms = new Ministatement();
        ms.parse(msStr);
        List<String> tags = ms.getTags();
        List<Hashtable<String, String>> lines = ms.getLines();
        assertEquals(5, tags.size());
        String[] expectedTags = {"DATE_TIME", "SEQ_NR", "TRAN_TYPE", "TRAN_AMOUNT", "CURR_CODE"};
        assertArrayEquals(expectedTags, tags.toArray());

        assertEquals(3, lines.size());
        Hashtable<String, String> line = lines.get(0);
        assertEquals("20121104120000", line.get(Ministatement.Tag.DATE_TIME));
        assertEquals("840", line.get(Ministatement.Tag.CURR_CODE));
    }
}
