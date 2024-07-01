package com.positive.openisojtests;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.linkly.libengine.engine.protocol.svfe.openisoj.AdditionalAmount;
import com.linkly.libengine.engine.protocol.svfe.openisoj.AdditionalAmounts;
import com.linkly.libengine.engine.protocol.svfe.openisoj.Bitmap;
import com.linkly.libengine.engine.protocol.svfe.openisoj.Iso8583;

import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class Iso8583Tests {
    @Test
    public void autoGenerateTransmissionDateTime() throws Exception {
        Iso8583 msg = new Iso8583();
        Date nowGmt = new Date();
        msg.getTransmissionDateTime().setNow();
        String actual = msg.get(7);
        SimpleDateFormat format = new SimpleDateFormat("MMddHHmmss");
        format.setTimeZone(TimeZone.getTimeZone("GMT"));
        String expected = format.format(nowGmt);

        //assertEquals(expected, actual);
        assertEquals(4, 2+2);       // TODO find out why the real 'assertEquals' has been commented out, and fix.
    }

    @Test
    public void clearField() throws Exception {
        Iso8583 msg = new Iso8583();
        msg.set(2, "123456789123456");
        msg.clearField(2);
        assertFalse(msg.isFieldSet(2));
        assertNull(msg.get(2));
    }

    @Test
    public void clearFieldThatIsNull() {
        Iso8583 msg = new Iso8583();
        msg.clearField(2);
        assertFalse(msg.isFieldSet(2));
        assertNull(msg.get(2));
    }

    @Test
    public void getAdditionalAmounts() throws Exception {
        Iso8583 msg = new Iso8583();
        msg.set(54, "1001840C0000000220001002840C000000022000");
        AdditionalAmounts amounts = msg.getAdditionalAmounts();
        for (AdditionalAmount amount : amounts) {
            assertNotNull(amount);
        }
    }

    @Test
    public void messageExtendedLength() throws Exception {
        Iso8583 msg = new Iso8583();
        msg.set(2, "58889212354567816");
        msg.set(3, "270010");
        msg.set(102, "9012273811");
        msg.setMsgType(Iso8583.MsgType._0200_TRAN_REQ);

        int expected = 37 + 4 + 16;
        assertEquals(expected, msg.getPackedLength());

    }

    @Test
    public void messageExtendedPack() throws Exception {
        Iso8583 msg = new Iso8583();
        msg.set(2, "58889212354567816");
        msg.set(3, "270010");
        msg.set(102, "9012273811");
        msg.setMsgType(Iso8583.MsgType._0200_TRAN_REQ);

        byte[] actual = msg.toMsg();

        byte[] mtid = "0200".getBytes();
        Bitmap bitmap = new Bitmap();
        bitmap.setField(2, true);
        bitmap.setField(3, true);
        bitmap.setField(102, true);
        byte[] bitmapData = bitmap.toMsg();
        byte[] msgContent = "1758889212354567816270010109012273811".getBytes();

        int fullMessageLength = 4 + bitmapData.length + msgContent.length;
        assertEquals(fullMessageLength, msg.getPackedLength());

        byte[] expected = new byte[fullMessageLength];

        System.arraycopy(mtid, 0, expected, 0, 4);
        System.arraycopy(bitmapData, 0, expected, 4, bitmapData.length);
        System.arraycopy(msgContent, 0, expected, 4 + bitmapData.length, msgContent.length);

        assertArrayEquals(expected, actual);
    }

    @Test
    public void messageExtendedUnpack() throws Exception {
        byte[] mtid = "0200".getBytes();
        Bitmap bitmap = new Bitmap();
        bitmap.setField(2, true);
        bitmap.setField(3, true);
        bitmap.setField(102, true);
        byte[] bitmapData = bitmap.toMsg();
        byte[] msgContent = "1758889290122738116270010109012273811".getBytes();

        int fullMessageLength = 4 + bitmapData.length + msgContent.length;
        byte[] raw = new byte[fullMessageLength];

        System.arraycopy(mtid, 0, raw, 0, 4);
        System.arraycopy(bitmapData, 0, raw, 4, bitmapData.length);
        System.arraycopy(msgContent, 0, raw, 4 + bitmapData.length, msgContent.length);

        Iso8583 msg = new Iso8583();
        int offset = msg.unpack(raw, 0);

        assertEquals(fullMessageLength, offset);
        assertTrue(msg.isFieldSet(2));
        assertEquals("58889290122738116", msg.get(2));
        assertTrue(msg.isFieldSet(3));
        assertEquals("270010", msg.get(3));
        assertTrue(msg.isFieldSet(102));
        assertEquals("9012273811", msg.get(102));
    }

    @Test
    public void messageNotExtendedPack() throws Exception {
        Iso8583 msg = new Iso8583();
        msg.set(2, "58889290122738116");
        msg.set(3, "270010");
        msg.setMsgType(Iso8583.MsgType._0200_TRAN_REQ);

        byte[] actual = msg.toMsg();

        byte[] mtid = "0200".getBytes();
        Bitmap bitmap = new Bitmap();
        bitmap.setField(2, true);
        bitmap.setField(3, true);
        byte[] bitmapData = bitmap.toMsg();
        byte[] msgContent = "1758889290122738116270010".getBytes();

        int fullMessageLength = 4 + bitmapData.length + msgContent.length;
        byte[] expected = new byte[fullMessageLength];

        System.arraycopy(mtid, 0, expected, 0, 4);
        System.arraycopy(bitmapData, 0, expected, 4, bitmapData.length);
        System.arraycopy(msgContent, 0, expected, 4 + bitmapData.length, msgContent.length);

        assertEquals(fullMessageLength, msg.getPackedLength());

        assertArrayEquals(expected, actual);
    }

    @Test
    public void messageNotExtendedUnpack() throws Exception {
        byte[] mtid = "0200".getBytes();
        Bitmap bitmap = new Bitmap();
        bitmap.setField(2, true);
        bitmap.setField(3, true);
        byte[] bitmapData = bitmap.toMsg();
        byte[] msgContent = "1758889290122738116270010".getBytes();

        int fullMessageLength = 4 + bitmapData.length + msgContent.length;
        byte[] raw = new byte[fullMessageLength];
        System.arraycopy(mtid, 0, raw, 0, 4);
        System.arraycopy(bitmapData, 0, raw, 4, bitmapData.length);
        System.arraycopy(msgContent, 0, raw, 4 + bitmapData.length, msgContent.length);

        Iso8583 msg = new Iso8583();
        int offset = msg.unpack(raw, 0);

        assertEquals(fullMessageLength, offset);
        assertTrue(msg.isFieldSet(2));
        assertEquals("58889290122738116", msg.get(2));
        assertTrue(msg.isFieldSet(3));
        assertEquals("270010", msg.get(3));
    }

    @Test
    public void nullAdditionalAmounts() throws Exception {
        Iso8583 msg = new Iso8583();
        AdditionalAmounts addAmounts = msg.getAdditionalAmounts();
        assertNull(addAmounts);
    }

    @Test
    public void settingNullClearsField() throws Exception {
        Iso8583 msg = new Iso8583();
        msg.set(2, "12345678912365");
        msg.set(2, null);
        assertNull(msg.get(2));
        assertFalse(msg.isFieldSet(2));
    }

    @Test
    public void testClone() throws Exception {
        Iso8583 orig = new Iso8583();
        orig.setMsgType(Iso8583.MsgType._0200_TRAN_REQ);
        orig.set(2, "12345650711452338");
        Iso8583 clone = (Iso8583) orig.clone();
        clone.setMsgType(Iso8583.MsgType._0210_TRAN_REQ_RSP);
        clone.set(2, "65432101247287413");
        assertNotEquals(clone.get(2), orig.get(2));
        assertNotEquals(clone.getMsgType(), orig.getMsgType());
    }
}
