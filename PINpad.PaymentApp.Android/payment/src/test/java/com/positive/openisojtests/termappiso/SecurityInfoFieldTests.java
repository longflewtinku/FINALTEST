package com.linkly.payment.openisojtests.termappiso;

import static org.junit.Assert.assertEquals;

import com.linkly.libengine.engine.protocol.svfe.openisoj.FieldDescriptor;
import com.linkly.libengine.engine.protocol.svfe.openisoj.IFieldDescriptor;
import com.linkly.libengine.engine.protocol.svfe.openisoj.Iso8583TermApp;
import com.linkly.libengine.engine.protocol.svfe.openisoj.Iso8583TermApp.Bit;
import com.linkly.libengine.engine.protocol.svfe.openisoj.IsoUtils;
import com.linkly.libengine.engine.protocol.svfe.openisoj.fieldvalidator.FieldValidators;
import com.linkly.libengine.engine.protocol.svfe.openisoj.formatter.BinaryFormatter;
import com.linkly.libengine.engine.protocol.svfe.openisoj.lengthformatters.VariableLengthFormatter;

import org.junit.Test;

/**
 * Basically field 53 from Iso8583TermApp
 *
 * @author John
 */
public class SecurityInfoFieldTests {
    @Test
    public void testFieldDescriptor() throws Exception {
        IFieldDescriptor fd = new FieldDescriptor(new VariableLengthFormatter(2, 96), FieldValidators.getHex(), new BinaryFormatter(),
                null, null);
        String expected = "3038FFFFDDDDEEEECCCC";
        byte[] packed = fd.pack(53, "FFFFDDDDEEEECCCC");
        String packedString = IsoUtils.byteArrayToHexString(packed);
        assertEquals(expected, packedString);
    }

    @Test
    public void testMessagePack() throws Exception {
        String expected = "423132303000000000020008003636363038FFFFDDDDEEEECCCC";
        Iso8583TermApp msg = new Iso8583TermApp();
        msg.setMsgType(Iso8583TermApp.MsgType._1200_TRAN_REQ);
        msg.set(Bit._039_ACTION_CODE, "666");
        msg.set(Bit._053_SECURITY_INFO, "FFFFDDDDEEEECCCC");
        byte[] bytes = msg.toMsg();
        String actual = IsoUtils.byteArrayToHexString(bytes);
        assertEquals(expected, actual);
    }

    @Test
    public void testMessagePackWithPin() throws Exception {
        String expected = "423132303000000000020018003636366327CE1BB15D7B9530380039997139E00006";
        Iso8583TermApp msg = new Iso8583TermApp();
        msg.setMsgType(Iso8583TermApp.MsgType._1200_TRAN_REQ);
        msg.set(Bit._039_ACTION_CODE, "666");
        msg.set(Bit._052_PIN_DATA, "6327CE1BB15D7B95");
        msg.set(Bit._053_SECURITY_INFO, "0039997139E00006");
        byte[] bytes = msg.toMsg();
        String actual = IsoUtils.byteArrayToHexString(bytes);
        assertEquals(expected, actual);
    }
}
