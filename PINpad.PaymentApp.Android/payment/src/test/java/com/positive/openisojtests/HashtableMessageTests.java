package com.linkly.payment.openisojtests;

import static org.junit.Assert.assertEquals;

import com.linkly.libengine.engine.protocol.svfe.openisoj.HashtableMessage;

import org.junit.Test;

public class HashtableMessageTests {
    @Test
    public void fromMessageString() {
        HashtableMessage hash = new HashtableMessage();

        String msgString = "15Unit1227This is the data for unit 1210TagNumber215tag 2";
        hash.fromMessageString(msgString);

        assertEquals("This is the data for unit 1", hash.get("Unit1"));
        assertEquals("tag 2", hash.get("TagNumber2"));

        assertEquals(2, hash.keySet().size());
        assertEquals(2, hash.size());
    }

    @Test
    public void toMessageString() {
        HashtableMessage hash = new HashtableMessage();

        hash.put("Unit1", "This is the data for unit 1");
        hash.put("TagNumber2", "tag 2");
        String expected = "15Unit1227This is the data for unit 1210TagNumber215tag 2";
        assertEquals(expected, hash.toMessageString());
    }
}
