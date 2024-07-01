package eft.com.eftcore;

import static com.linkly.libengine.engine.protocol.iso8583.openisoj.As2805Suncorp.Bit._024_NII;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.As2805Suncorp.Bit._047_ADDITIONAL_DATA_NATIONAL;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.As2805Suncorp.Bit._055_ICC_DATA;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.As2805Suncorp.Bit._057_CASH_AMOUNT;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.As2805Suncorp.Bit._060_ADDITIONAL_PRIVATE;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.As2805Suncorp.Bit._061_ADDITIONAL_PRIVATE;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.As2805Suncorp.Bit._062_ADDITIONAL_PRIVATE;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.As2805Suncorp.Bit._064_MAC;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.As2805Suncorp.Bit._070_NMIC;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.As2805Suncorp.Bit._118_CASHOUTS_NUMBER;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.As2805Suncorp.Bit._119_CASHOUTS_AMOUNT;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.As2805Suncorp.Bit._128_MAC;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.As2805Suncorp.MsgType._0820_NWRK_MGMT_KEY_CHANGE_ADVICE;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.As2805Suncorp.MsgType._9820_PRIVATE_NETWORK_MSG_REQUEST;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583.Bit._002_PAN;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583.Bit._003_PROC_CODE;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583.Bit._004_TRAN_AMOUNT;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583.Bit._011_SYS_TRACE_AUDIT_NUM;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583.Bit._012_LOCAL_TRAN_TIME;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583.Bit._013_LOCAL_TRAN_DATE;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583.Bit._014_EXPIRATION_DATE;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583.Bit._015_SETTLEMENT_DATE;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583.Bit._022_POS_ENTRY_MODE;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583.Bit._023_CARD_SEQUENCE_NUM;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583.Bit._025_POS_CONDITION_CODE;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583.Bit._032_ACQUIRING_INST_ID_CODE;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583.Bit._035_TRACK_2_DATA;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583.Bit._037_RETRIEVAL_REF_NUM;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583.Bit._038_AUTH_ID_RESPONSE;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583.Bit._039_RESPONSE_CODE;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583.Bit._041_CARD_ACCEPTOR_TERMINAL_ID;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583.Bit._042_CARD_ACCEPTOR_ID_CODE;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583.Bit._048_ADDITIONAL_DATA;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583.Bit._052_PIN_DATA;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583.Bit._053_SECURITY_RELATED_CONTROL_INFORMATION;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583.Bit._054_ADDITIONAL_AMOUNTS;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583.Bit._066_SETTLEMENT_CODE;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583.Bit._074_CREDITS_NUMBER;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583.Bit._075_CREDITS_REVERSAL_NUMBER;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583.Bit._076_DEBITS_NUMBER;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583.Bit._077_DEBITS_REVERSAL_NUMBER;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583.Bit._078_TRANSFER_NUMBER;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583.Bit._079_TRANSFER_REVERSAL_NUMBER;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583.Bit._080_INQUIRIES_NUMBER;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583.Bit._081_AUTHORISATIONS_NUMBER;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583.Bit._086_CREDITS_AMOUNT;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583.Bit._087_CREDITS_REVERSAL_AMOUNT;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583.Bit._088_DEBITS_AMOUNT;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583.Bit._089_DEBITS_REVERSAL_AMOUNT;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583.Bit._090_ORIGINAL_DATA_ELEMENTS;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583.Bit._097_AMOUNT_NET_SETTLEMENT;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583.Bit._118_PAYMENTS_NUMBER;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583.Bit._119_PAYMENTS_REVERSAL_NUMBER;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import com.linkly.libengine.engine.protocol.iso8583.openisoj.As2805Suncorp;
import com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583;

import org.junit.Test;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class As2805SuncorpTests {
    private static final String TAG = "As2805SuncorpTests";

    @Test
    public void test0100Pack() {
        try {

            As2805Suncorp msg = new As2805Suncorp();

            msg.setMsgType(Iso8583.MsgType._0100_AUTH_REQ);
            msg.set(_002_PAN, "1234567890123456789");
            msg.set(_003_PROC_CODE, "010203" );
            msg.set(_004_TRAN_AMOUNT, "000000010000" );
            msg.set(_011_SYS_TRACE_AUDIT_NUM, "112233");
            msg.set(_014_EXPIRATION_DATE, "2012" );
            msg.set(_022_POS_ENTRY_MODE, "402" );
            msg.set(_023_CARD_SEQUENCE_NUM, "002" );
            msg.set(_024_NII, "152" );
            msg.set(_025_POS_CONDITION_CODE, "30" );
            msg.set(_032_ACQUIRING_INST_ID_CODE, "12345678901");
            msg.set(_035_TRACK_2_DATA, "4000000000000002D20120129831209388123" );
            msg.set(_041_CARD_ACCEPTOR_TERMINAL_ID, "87654321");
            msg.set(_042_CARD_ACCEPTOR_ID_CODE, "998877665544332");
            msg.set(_047_ADDITIONAL_DATA_NATIONAL, "additional data national" );
            msg.set(_052_PIN_DATA, "0102030405060708" );
            msg.set(_055_ICC_DATA, "9F0206000000001200" );
            msg.set(_061_ADDITIONAL_PRIVATE, "product/service codes go here");
            msg.set(_062_ADDITIONAL_PRIVATE, "invoice number goes here");
            msg.set(_064_MAC, "1A2B3C4D50120173");

            byte[] actual = msg.toMsg();
            byte[] expected = { 0x01, 0x00, 0x70, 0x24, 0x07, (byte)0x81, 0x20, (byte)0xC2, 0x12, 0x0D, 0x19, 0x12, 0x34, 0x56, 0x78, (byte)0x90, 0x12, 0x34, 0x56, 0x78,
                    (byte)0x9F, 0x01, 0x02, 0x03, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x11, 0x22, 0x33, 0x20, 0x12, 0x04, 0x02, 0x00, 0x02, 0x01, 0x52, 0x30,
                    0x11, 0x12, 0x34, 0x56, 0x78, (byte)0x90, 0x1F, 0x37, 0x40, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x02, (byte)0xD2, 0x01, 0x20, 0x12, (byte)0x98, 0x31,
                    0x20, (byte)0x93, (byte)0x88, 0x12, 0x3F, 0x38, 0x37, 0x36, 0x35, 0x34, 0x33, 0x32, 0x31, 0x39, 0x39, 0x38, 0x38, 0x37, 0x37, 0x36, 0x36, 0x35,
                    0x35, 0x34, 0x34, 0x33, 0x33, 0x32, 0x30, 0x32, 0x34, 0x61, 0x64, 0x64, 0x69, 0x74, 0x69, 0x6F, 0x6E, 0x61, 0x6C, 0x20, 0x64, 0x61,
                    0x74, 0x61, 0x20, 0x6E, 0x61, 0x74, 0x69, 0x6F, 0x6E, 0x61, 0x6C, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x30, 0x30, 0x39,
                    (byte)0x9F, 0x02, 0x06, 0x00, 0x00, 0x00, 0x00, 0x12, 0x00, 0x30, 0x32, 0x39, 0x70, 0x72, 0x6F, 0x64, 0x75, 0x63, 0x74, 0x2F, 0x73, 0x65,
                    0x72, 0x76, 0x69, 0x63, 0x65, 0x20, 0x63, 0x6F, 0x64, 0x65, 0x73, 0x20, 0x67, 0x6F, 0x20, 0x68, 0x65, 0x72, 0x65, 0x30, 0x32, 0x34,
                    0x69, 0x6E, 0x76, 0x6F, 0x69, 0x63, 0x65, 0x20, 0x6E, 0x75, 0x6D, 0x62, 0x65, 0x72, 0x20, 0x67, 0x6F, 0x65, 0x73, 0x20, 0x68, 0x65,
                    0x72, 0x65, 0x1A, 0x2B, 0x3C, 0x4D, 0x50, 0x12, 0x01, 0x73 };
            assertArrayEquals(expected, actual);

        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void test0100Unpack() {
        // test that we can unpack the message packed in the 0100 pack test above. not a real-world use case but useful to test unpack logic
        try {
            final byte[] inputData = { 0x01, 0x00, 0x70, 0x24, 0x07, (byte)0x81, 0x20, (byte)0xC2, 0x12, 0x0D, 0x19, 0x12, 0x34, 0x56, 0x78, (byte)0x90, 0x12, 0x34, 0x56, 0x78,
                    (byte)0x9F, 0x01, 0x02, 0x03, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x11, 0x22, 0x33, 0x20, 0x12, 0x04, 0x02, 0x00, 0x02, 0x01, 0x52, 0x30,
                    0x11, 0x12, 0x34, 0x56, 0x78, (byte)0x90, 0x1F, 0x37, 0x40, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x02, (byte)0xD2, 0x01, 0x20, 0x12, (byte)0x98, 0x31,
                    0x20, (byte)0x93, (byte)0x88, 0x12, 0x3F, 0x38, 0x37, 0x36, 0x35, 0x34, 0x33, 0x32, 0x31, 0x39, 0x39, 0x38, 0x38, 0x37, 0x37, 0x36, 0x36, 0x35,
                    0x35, 0x34, 0x34, 0x33, 0x33, 0x32, 0x30, 0x32, 0x34, 0x61, 0x64, 0x64, 0x69, 0x74, 0x69, 0x6F, 0x6E, 0x61, 0x6C, 0x20, 0x64, 0x61,
                    0x74, 0x61, 0x20, 0x6E, 0x61, 0x74, 0x69, 0x6F, 0x6E, 0x61, 0x6C, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x30, 0x30, 0x39,
                    (byte)0x9F, 0x02, 0x06, 0x00, 0x00, 0x00, 0x00, 0x12, 0x00, 0x30, 0x32, 0x39, 0x70, 0x72, 0x6F, 0x64, 0x75, 0x63, 0x74, 0x2F, 0x73, 0x65,
                    0x72, 0x76, 0x69, 0x63, 0x65, 0x20, 0x63, 0x6F, 0x64, 0x65, 0x73, 0x20, 0x67, 0x6F, 0x20, 0x68, 0x65, 0x72, 0x65, 0x30, 0x32, 0x34,
                    0x69, 0x6E, 0x76, 0x6F, 0x69, 0x63, 0x65, 0x20, 0x6E, 0x75, 0x6D, 0x62, 0x65, 0x72, 0x20, 0x67, 0x6F, 0x65, 0x73, 0x20, 0x68, 0x65,
                    0x72, 0x65, 0x1A, 0x2B, 0x3C, 0x4D, 0x50, 0x12, 0x01, 0x73 };
            As2805Suncorp msg = new As2805Suncorp(inputData);

            assertEquals( Iso8583.MsgType._0100_AUTH_REQ, msg.getMsgType() );
            assertEquals( "1234567890123456789", msg.get(_002_PAN));
            assertEquals( "010203", msg.get(_003_PROC_CODE));
            assertEquals( "000000010000", msg.get(_004_TRAN_AMOUNT));
            assertEquals( "112233", msg.get(_011_SYS_TRACE_AUDIT_NUM));
            assertEquals( "2012", msg.get(_014_EXPIRATION_DATE));
            assertEquals( "402", msg.get(_022_POS_ENTRY_MODE));
            assertEquals( "002", msg.get(_023_CARD_SEQUENCE_NUM));
            assertEquals( "152", msg.get(_024_NII));
            assertEquals( "30", msg.get(_025_POS_CONDITION_CODE));
            assertEquals( "12345678901", msg.get(_032_ACQUIRING_INST_ID_CODE));
            assertEquals( "87654321", msg.get(_041_CARD_ACCEPTOR_TERMINAL_ID));
            assertEquals( "998877665544332", msg.get(_042_CARD_ACCEPTOR_ID_CODE));
            assertEquals( "additional data national", msg.get(_047_ADDITIONAL_DATA_NATIONAL));
            assertEquals( "0102030405060708", msg.get(_052_PIN_DATA));
            assertEquals( "9F0206000000001200", msg.get(_055_ICC_DATA));
            assertEquals( "product/service codes go here", msg.get(_061_ADDITIONAL_PRIVATE));
            assertEquals( "invoice number goes here", msg.get(_062_ADDITIONAL_PRIVATE));
            assertEquals( "1A2B3C4D50120173", msg.get(_064_MAC));

        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void test0110Unpack() {
        // test that we can unpack the message packed in the 0100 pack test above. not a real-world use case but useful to test unpack logic
        try {
            As2805Suncorp msg = new As2805Suncorp();

            msg.setMsgType(Iso8583.MsgType._0110_AUTH_REQ_RSP);
            msg.set(_003_PROC_CODE, "010203" );
            msg.set(_004_TRAN_AMOUNT, "000000010000" );
            msg.set(_011_SYS_TRACE_AUDIT_NUM, "112233");
            msg.set(_012_LOCAL_TRAN_TIME, "120140");
            msg.set(_013_LOCAL_TRAN_DATE, "0723");
            msg.set(_015_SETTLEMENT_DATE, "0723" );
            msg.set(_024_NII, "152" );
            msg.set(_032_ACQUIRING_INST_ID_CODE, "12345678901");
            msg.set(_037_RETRIEVAL_REF_NUM, "002710238571" );
            msg.set(_038_AUTH_ID_RESPONSE, "182370" );
            msg.set(_039_RESPONSE_CODE, "00" );
            msg.set(_041_CARD_ACCEPTOR_TERMINAL_ID, "87654321");
            msg.set(_042_CARD_ACCEPTOR_ID_CODE, "998877665544332");
            msg.set(_055_ICC_DATA, "9F0206000000001200" );
            msg.set(_060_ADDITIONAL_PRIVATE, "tms/nm flags");
            msg.set(_064_MAC, "1A2B3C4D50120173");

            byte[] actual = msg.toMsg();
            byte[] expected = new byte[] { 0x01, 0x10, 0x30, 0x3A, 0x01, 0x01, 0x0E, (byte)0xC0, 0x02, 0x11, 0x01, 0x02, 0x03, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x11, 0x22,
                    0x33, 0x12, 0x01, 0x40, 0x07, 0x23, 0x07, 0x23, 0x01, 0x52, 0x11, 0x12, 0x34, 0x56, 0x78, (byte)0x90, 0x1F, 0x30, 0x30, 0x32, 0x37, 0x31, 0x30, 0x32, 0x33,
                    0x38, 0x35, 0x37, 0x31, 0x31, 0x38, 0x32, 0x33, 0x37, 0x30, 0x30, 0x30, 0x38, 0x37, 0x36, 0x35, 0x34, 0x33, 0x32, 0x31, 0x39, 0x39, 0x38, 0x38, 0x37,
                    0x37, 0x36, 0x36, 0x35, 0x35, 0x34, 0x34, 0x33, 0x33, 0x32, 0x30, 0x30, 0x39, (byte)0x9F, 0x02, 0x06, 0x00, 0x00, 0x00, 0x00, 0x12, 0x00, 0x30, 0x31, 0x32,
                    0x74, 0x6D, 0x73, 0x2F, 0x6E, 0x6D, 0x20, 0x66, 0x6C, 0x61, 0x67, 0x73, 0x1A, 0x2B, 0x3C, 0x4D, 0x50, 0x12, 0x01, 0x73 };
            assertArrayEquals(expected, actual);

            As2805Suncorp msgRxd = new As2805Suncorp(expected);

            assertEquals( Iso8583.MsgType._0110_AUTH_REQ_RSP, msgRxd.getMsgType() );
            assertEquals( "010203", msgRxd.get(_003_PROC_CODE));
            assertEquals( "000000010000", msgRxd.get(_004_TRAN_AMOUNT));
            assertEquals( "112233", msgRxd.get(_011_SYS_TRACE_AUDIT_NUM));
            assertEquals( "120140", msgRxd.get(_012_LOCAL_TRAN_TIME));
            assertEquals( "0723", msgRxd.get(_013_LOCAL_TRAN_DATE));
            assertEquals( "0723", msgRxd.get(_015_SETTLEMENT_DATE));
            assertEquals( "152", msgRxd.get(_024_NII));
            assertEquals( "12345678901", msgRxd.get(_032_ACQUIRING_INST_ID_CODE));
            assertEquals( "002710238571", msgRxd.get(_037_RETRIEVAL_REF_NUM));
            assertEquals( "182370", msgRxd.get(_038_AUTH_ID_RESPONSE));
            assertEquals( "00", msgRxd.get(_039_RESPONSE_CODE));
            assertEquals( "87654321", msgRxd.get(_041_CARD_ACCEPTOR_TERMINAL_ID));
            assertEquals( "998877665544332", msgRxd.get(_042_CARD_ACCEPTOR_ID_CODE));
            assertEquals( "9F0206000000001200", msgRxd.get(_055_ICC_DATA));
            assertEquals( "tms/nm flags", msgRxd.get(_060_ADDITIONAL_PRIVATE));
            assertEquals( "1A2B3C4D50120173", msgRxd.get(_064_MAC));
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void test0200PurchasePack() {
        try {

            As2805Suncorp msg = new As2805Suncorp();

            msg.setMsgType(Iso8583.MsgType._0200_TRAN_REQ);
            msg.set(_002_PAN, "1234567890123456789");
            msg.set(_003_PROC_CODE, "010203" );
            msg.set(_004_TRAN_AMOUNT, "000000010000" );
            msg.set(_011_SYS_TRACE_AUDIT_NUM, "112233");
            msg.set(_014_EXPIRATION_DATE, "2012" );
            msg.set(_022_POS_ENTRY_MODE, "402" );
            msg.set(_023_CARD_SEQUENCE_NUM, "002" );
            msg.set(_024_NII, "152" );
            msg.set(_025_POS_CONDITION_CODE, "30" );
            msg.set(_032_ACQUIRING_INST_ID_CODE, "12345678901");
            msg.set(_035_TRACK_2_DATA, "4000000000000002D20120129831209388123" );
            msg.set(_041_CARD_ACCEPTOR_TERMINAL_ID, "87654321");
            msg.set(_042_CARD_ACCEPTOR_ID_CODE, "998877665544332");
            msg.set(_047_ADDITIONAL_DATA_NATIONAL, "additional data national" );
            msg.set(_052_PIN_DATA, "0102030405060708" );
            msg.set(_054_ADDITIONAL_AMOUNTS, "109127319203" );
            msg.set(_055_ICC_DATA, "9F0206000000001200" );
            msg.set(_061_ADDITIONAL_PRIVATE, "product/service codes go here");
            msg.set(_062_ADDITIONAL_PRIVATE, "invoice number goes here");
            msg.set(_064_MAC, "1A2B3C4D50120173");

            byte[] actual = msg.toMsg();
            byte[] expected = { 0x02, 0x00, 0x70, 0x24, 0x07, (byte)0x81, 0x20, (byte)0xC2, 0x16, 0x0D, 0x19, 0x12, 0x34, 0x56, 0x78, (byte)0x90, 0x12, 0x34, 0x56, 0x78, (byte)0x9F, 0x01, 0x02, 0x03,
                    0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x11, 0x22, 0x33, 0x20, 0x12, 0x04, 0x02, 0x00, 0x02, 0x01, 0x52, 0x30, 0x11, 0x12, 0x34, 0x56, 0x78, (byte)0x90, 0x1F, 0x37,
                    0x40, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x02, (byte)0xD2, 0x01, 0x20, 0x12, (byte)0x98, 0x31, 0x20, (byte)0x93, (byte)0x88, 0x12, 0x3F, 0x38, 0x37, 0x36, 0x35, 0x34, 0x33, 0x32,
                    0x31, 0x39, 0x39, 0x38, 0x38, 0x37, 0x37, 0x36, 0x36, 0x35, 0x35, 0x34, 0x34, 0x33, 0x33, 0x32, 0x30, 0x32, 0x34, 0x61, 0x64, 0x64, 0x69, 0x74, 0x69, 0x6F,
                    0x6E, 0x61, 0x6C, 0x20, 0x64, 0x61, 0x74, 0x61, 0x20, 0x6E, 0x61, 0x74, 0x69, 0x6F, 0x6E, 0x61, 0x6C, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x30,
                    0x31, 0x32, 0x31, 0x30, 0x39, 0x31, 0x32, 0x37, 0x33, 0x31, 0x39, 0x32, 0x30, 0x33, 0x30, 0x30, 0x39, (byte)0x9F, 0x02, 0x06, 0x00, 0x00, 0x00, 0x00, 0x12, 0x00,
                    0x30, 0x32, 0x39, 0x70, 0x72, 0x6F, 0x64, 0x75, 0x63, 0x74, 0x2F, 0x73, 0x65, 0x72, 0x76, 0x69, 0x63, 0x65, 0x20, 0x63, 0x6F, 0x64, 0x65, 0x73, 0x20, 0x67,
                    0x6F, 0x20, 0x68, 0x65, 0x72, 0x65, 0x30, 0x32, 0x34, 0x69, 0x6E, 0x76, 0x6F, 0x69, 0x63, 0x65, 0x20, 0x6E, 0x75, 0x6D, 0x62, 0x65, 0x72, 0x20, 0x67, 0x6F,
                    0x65, 0x73, 0x20, 0x68, 0x65, 0x72, 0x65, 0x1A, 0x2B, 0x3C, 0x4D, 0x50, 0x12, 0x01, 0x73 };
            assertArrayEquals(expected, actual);

        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void test0210Unpack() {
        // test that we can unpack the message packed in the 0100 pack test above. not a real-world use case but useful to test unpack logic
        try {
            final byte[] inputData = { 0x02, 0x10, 0x30, 0x3A, 0x00, 0x01, 0x0E, (byte)0xC0, 0x02, 0x11, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x14, (byte)0x80, 0x00, 0x00, 0x56, 0x12, 0x01,
                    0x40, 0x07, 0x23, 0x07, 0x23, 0x09, 0x12, 0x34, 0x56, 0x78, (byte)0x9F, 0x30, 0x30, 0x32, 0x37, 0x31, 0x30, 0x32, 0x33, 0x38, 0x35, 0x37, 0x31, 0x31, 0x38, 0x32, 0x33, 0x37,
                    0x30, 0x30, 0x30, 0x39, 0x39, 0x39, 0x36, 0x33, 0x33, 0x38, 0x35, 0x54, 0x45, 0x53, 0x54, 0x5F, 0x53, 0x55, 0x4E, 0x43, 0x4F, 0x52, 0x50, 0x20, 0x20, 0x20, 0x30, 0x30, 0x39, (byte)0x9F,
                    0x02, 0x06, 0x00, 0x00, 0x00, 0x00, 0x12, 0x00, 0x30, 0x31, 0x32, 0x74, 0x6D, 0x73, 0x2F, 0x6E, 0x6D, 0x20, 0x66, 0x6C, 0x61, 0x67, 0x73, 0x1A, 0x2B, 0x3C, 0x4D, 0x50,
                    0x12, 0x01, 0x73 };
            As2805Suncorp msg = new As2805Suncorp(inputData);

            assertEquals( Iso8583.MsgType._0210_TRAN_REQ_RSP, msg.getMsgType() );
            assertEquals( "000000", msg.get(_003_PROC_CODE));
            assertEquals( "000000001480", msg.get(_004_TRAN_AMOUNT));
            assertEquals( "000056", msg.get(_011_SYS_TRACE_AUDIT_NUM));
            assertEquals( "120140", msg.get(_012_LOCAL_TRAN_TIME));
            assertEquals( "0723", msg.get(_013_LOCAL_TRAN_DATE));
            assertEquals( "0723", msg.get(_015_SETTLEMENT_DATE));
            assertEquals( "123456789", msg.get(_032_ACQUIRING_INST_ID_CODE));
            assertEquals( "002710238571", msg.get(_037_RETRIEVAL_REF_NUM));
            assertEquals( "182370", msg.get(_038_AUTH_ID_RESPONSE));
            assertEquals( "99963385", msg.get(_041_CARD_ACCEPTOR_TERMINAL_ID));
            assertEquals( "TEST_SUNCORP   ", msg.get(_042_CARD_ACCEPTOR_ID_CODE));
            assertEquals( "9F0206000000001200", msg.get(_055_ICC_DATA));
            assertEquals( "tms/nm flags", msg.get(_060_ADDITIONAL_PRIVATE));
            assertEquals( "1A2B3C4D50120173", msg.get(_064_MAC));

        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void test0200CashoutPack() {
        try {

            As2805Suncorp msg = new As2805Suncorp();

            msg.setMsgType(Iso8583.MsgType._0200_TRAN_REQ);
            msg.set(_002_PAN, "1234567890123456789");
            msg.set(_003_PROC_CODE, "010203");
            msg.set(_004_TRAN_AMOUNT, "000000010000");
            msg.set(_011_SYS_TRACE_AUDIT_NUM, "112233");
            msg.set(_014_EXPIRATION_DATE, "2012");
            msg.set(_022_POS_ENTRY_MODE, "402");
            msg.set(_023_CARD_SEQUENCE_NUM, "002");
            msg.set(_024_NII, "152");
            msg.set(_025_POS_CONDITION_CODE, "30");
            msg.set(_032_ACQUIRING_INST_ID_CODE, "12345678901");
            msg.set(_035_TRACK_2_DATA, "4000000000000002D20120129831209388123");
            msg.set(_041_CARD_ACCEPTOR_TERMINAL_ID, "87654321");
            msg.set(_042_CARD_ACCEPTOR_ID_CODE, "998877665544332");
            msg.set(_047_ADDITIONAL_DATA_NATIONAL, "additional data national");
            msg.set(_052_PIN_DATA, "0102030405060708");
            msg.set(_055_ICC_DATA, "9F0206000000001200");
            msg.set(_057_CASH_AMOUNT, "000000001000");
            msg.set(_061_ADDITIONAL_PRIVATE, "product/service codes go here");
            msg.set(_062_ADDITIONAL_PRIVATE, "invoice number goes here");
            msg.set(_064_MAC, "1A2B3C4D50120173");

            byte[] actual = msg.toMsg();
            byte[] expected = {0x02, 0x00, 0x70, 0x24, 0x07, (byte)0x81, 0x20, (byte)0xC2, 0x12, (byte)0x8D, 0x19, 0x12, 0x34, 0x56, 0x78, (byte)0x90, 0x12, 0x34, 0x56, 0x78, (byte)0x9F,
                    0x01, 0x02, 0x03, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x11, 0x22, 0x33, 0x20, 0x12, 0x04, 0x02, 0x00, 0x02, 0x01, 0x52, 0x30, 0x11, 0x12,
                    0x34, 0x56, 0x78, (byte)0x90, 0x1F, 0x37, 0x40, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x02, (byte)0xD2, 0x01, 0x20, 0x12, (byte)0x98, 0x31, 0x20, (byte)0x93, (byte)0x88,
                    0x12, 0x3F, 0x38, 0x37, 0x36, 0x35, 0x34, 0x33, 0x32, 0x31, 0x39, 0x39, 0x38, 0x38, 0x37, 0x37, 0x36, 0x36, 0x35, 0x35, 0x34, 0x34, 0x33,
                    0x33, 0x32, 0x30, 0x32, 0x34, 0x61, 0x64, 0x64, 0x69, 0x74, 0x69, 0x6F, 0x6E, 0x61, 0x6C, 0x20, 0x64, 0x61, 0x74, 0x61, 0x20, 0x6E, 0x61,
                    0x74, 0x69, 0x6F, 0x6E, 0x61, 0x6C, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x30, 0x30, 0x39, (byte)0x9F, 0x02, 0x06, 0x00, 0x00, 0x00,
                    0x00, 0x12, 0x00, 0x00, 0x00, 0x00, 0x00, 0x10, 0x00, 0x30, 0x32, 0x39, 0x70, 0x72, 0x6F, 0x64, 0x75, 0x63, 0x74, 0x2F, 0x73, 0x65, 0x72,
                    0x76, 0x69, 0x63, 0x65, 0x20, 0x63, 0x6F, 0x64, 0x65, 0x73, 0x20, 0x67, 0x6F, 0x20, 0x68, 0x65, 0x72, 0x65, 0x30, 0x32, 0x34, 0x69, 0x6E,
                    0x76, 0x6F, 0x69, 0x63, 0x65, 0x20, 0x6E, 0x75, 0x6D, 0x62, 0x65, 0x72, 0x20, 0x67, 0x6F, 0x65, 0x73, 0x20, 0x68, 0x65, 0x72, 0x65, 0x1A,
                    0x2B, 0x3C, 0x4D, 0x50, 0x12, 0x01, 0x73};
            assertArrayEquals(expected, actual);

        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void test0220AdvicePack() {
        try {

            As2805Suncorp msg = new As2805Suncorp();

            msg.setMsgType(Iso8583.MsgType._0220_TRAN_ADV);
            msg.set(_002_PAN, "1234567890123456789");
            msg.set(_003_PROC_CODE, "010203");
            msg.set(_004_TRAN_AMOUNT, "000000010000");
            msg.set(_011_SYS_TRACE_AUDIT_NUM, "112233");
            msg.set(_012_LOCAL_TRAN_TIME, "112233");
            msg.set(_013_LOCAL_TRAN_DATE, "0421");
            msg.set(_014_EXPIRATION_DATE, "2012");
            msg.set(_022_POS_ENTRY_MODE, "402");
            msg.set(_023_CARD_SEQUENCE_NUM, "002");
            msg.set(_024_NII, "152");
            msg.set(_025_POS_CONDITION_CODE, "30");
            msg.set(_032_ACQUIRING_INST_ID_CODE, "12345678901");
            msg.set(_035_TRACK_2_DATA, "4000000000000002D20120129831209388123");
            msg.set(_038_AUTH_ID_RESPONSE, "123987");
            msg.set(_041_CARD_ACCEPTOR_TERMINAL_ID, "87654321");
            msg.set(_042_CARD_ACCEPTOR_ID_CODE, "998877665544332");
            msg.set(_047_ADDITIONAL_DATA_NATIONAL, "additional data national");
            msg.set(_054_ADDITIONAL_AMOUNTS, "0102030405060708");
            msg.set(_055_ICC_DATA, "9F0206000000001200");
            msg.set(_062_ADDITIONAL_PRIVATE, "invoice number goes here");
            msg.set(_064_MAC, "1A2B3C4D50120173");

            byte[] actual = msg.toMsg();
            byte[] expected = {0x02, 0x20, 0x70, 0x3C, 0x07, (byte)0x81, 0x24, (byte)0xC2, 0x06, 0x05, 0x19, 0x12, 0x34, 0x56, 0x78, (byte)0x90, 0x12, 0x34, 0x56, 0x78,
                    (byte)0x9F, 0x01, 0x02, 0x03, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x11, 0x22, 0x33, 0x11, 0x22, 0x33, 0x04, 0x21, 0x20, 0x12, 0x04, 0x02,
                    0x00, 0x02, 0x01, 0x52, 0x30, 0x11, 0x12, 0x34, 0x56, 0x78, (byte)0x90, 0x1F, 0x37, 0x40, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x02, (byte)0xD2,
                    0x01, 0x20, 0x12, (byte)0x98, 0x31, 0x20, (byte)0x93, (byte)0x88, 0x12, 0x3F, 0x31, 0x32, 0x33, 0x39, 0x38, 0x37, 0x38, 0x37, 0x36, 0x35, 0x34, 0x33,
                    0x32, 0x31, 0x39, 0x39, 0x38, 0x38, 0x37, 0x37, 0x36, 0x36, 0x35, 0x35, 0x34, 0x34, 0x33, 0x33, 0x32, 0x30, 0x32, 0x34, 0x61, 0x64,
                    0x64, 0x69, 0x74, 0x69, 0x6F, 0x6E, 0x61, 0x6C, 0x20, 0x64, 0x61, 0x74, 0x61, 0x20, 0x6E, 0x61, 0x74, 0x69, 0x6F, 0x6E, 0x61, 0x6C,
                    0x30, 0x31, 0x36, 0x30, 0x31, 0x30, 0x32, 0x30, 0x33, 0x30, 0x34, 0x30, 0x35, 0x30, 0x36, 0x30, 0x37, 0x30, 0x38, 0x30, 0x30, 0x39,
                    (byte)0x9F, 0x02, 0x06, 0x00, 0x00, 0x00, 0x00, 0x12, 0x00, 0x30, 0x32, 0x34, 0x69, 0x6E, 0x76, 0x6F, 0x69, 0x63, 0x65, 0x20, 0x6E, 0x75,
                    0x6D, 0x62, 0x65, 0x72, 0x20, 0x67, 0x6F, 0x65, 0x73, 0x20, 0x68, 0x65, 0x72, 0x65, 0x1A, 0x2B, 0x3C, 0x4D, 0x50, 0x12, 0x01, 0x73};
            assertArrayEquals(expected, actual);

        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    @Test
    public void test0420AdvicePack() {
        try {

            As2805Suncorp msg = new As2805Suncorp();

            msg.setMsgType(Iso8583.MsgType._0420_ACQUIRER_REV_ADV);
            msg.set(_002_PAN, "1234567890123456789");
            msg.set(_003_PROC_CODE, "010203");
            msg.set(_004_TRAN_AMOUNT, "000000010000");
            msg.set(_011_SYS_TRACE_AUDIT_NUM, "112233");
            msg.set(_014_EXPIRATION_DATE, "2012");
            msg.set(_022_POS_ENTRY_MODE, "402");
            msg.set(_023_CARD_SEQUENCE_NUM, "002");
            msg.set(_024_NII, "152");
            msg.set(_025_POS_CONDITION_CODE, "30");
            msg.set(_032_ACQUIRING_INST_ID_CODE, "12345678901");
            msg.set(_035_TRACK_2_DATA, "4000000000000002D20120129831209388123");
            msg.set(_037_RETRIEVAL_REF_NUM, "123987000888");
            msg.set(_041_CARD_ACCEPTOR_TERMINAL_ID, "87654321");
            msg.set(_042_CARD_ACCEPTOR_ID_CODE, "998877665544332");
            msg.set(_047_ADDITIONAL_DATA_NATIONAL, "additional data national");
            msg.set(_048_ADDITIONAL_DATA, "6164646974696F6E616C20646174612070726976617465203438");
            msg.set(_054_ADDITIONAL_AMOUNTS, "0102030405060708");
            msg.set(_055_ICC_DATA, "9F0206000000001200");
            msg.set(_090_ORIGINAL_DATA_ELEMENTS, "010203040506070801020304050607080102030405");
            msg.set(_128_MAC, "1A2B3C4D50120173");

            byte[] actual = msg.toMsg();
            byte[] expected = {0x04, 0x20, (byte)0xF0, 0x24, 0x07, (byte)0x81, 0x28, (byte)0xC3, 0x06, 0x00, 0x00, 0x00, 0x00, 0x40, 0x00, 0x00, 0x00, 0x01, 0x19, 0x12, 0x34, 0x56, 0x78,
                    (byte)0x90, 0x12, 0x34, 0x56, 0x78, (byte)0x9F, 0x01, 0x02, 0x03, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x11, 0x22, 0x33, 0x20, 0x12, 0x04, 0x02, 0x00, 0x02, 0x01,
                    0x52, 0x30, 0x11, 0x12, 0x34, 0x56, 0x78, (byte)0x90, 0x1F, 0x37, 0x40, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x02, (byte)0xD2, 0x01, 0x20, 0x12, (byte)0x98, 0x31, 0x20,
                    (byte)0x93, (byte)0x88, 0x12, 0x3F, 0x31, 0x32, 0x33, 0x39, 0x38, 0x37, 0x30, 0x30, 0x30, 0x38, 0x38, 0x38, 0x38, 0x37, 0x36, 0x35, 0x34, 0x33, 0x32, 0x31, 0x39,
                    0x39, 0x38, 0x38, 0x37, 0x37, 0x36, 0x36, 0x35, 0x35, 0x34, 0x34, 0x33, 0x33, 0x32,
                    0x30, 0x32, 0x34, 0x61, 0x64, 0x64, 0x69, 0x74, 0x69, 0x6F, 0x6E,  0x61, 0x6C, 0x20, 0x64, 0x61, 0x74, 0x61, 0x20, 0x6E, 0x61, 0x74, 0x69, 0x6F, 0x6E, 0x61, 0x6C, // field 47
                    0x30, 0x32, 0x36, 0x61, 0x64, 0x64, 0x69, 0x74, 0x69, 0x6F, 0x6E, 0x61, 0x6C, 0x20, 0x64, 0x61, 0x74, 0x61, 0x20, 0x70, 0x72, 0x69, 0x76, 0x61, 0x74, 0x65, 0x20, 0x34, 0x38, // field 48
                    0x30, 0x31, 0x36, 0x30, 0x31,
                    0x30, 0x32, 0x30, 0x33, 0x30, 0x34, 0x30, 0x35, 0x30, 0x36, 0x30, 0x37, 0x30, 0x38, 0x30, 0x30, 0x39, (byte)0x9F, 0x02, 0x06, 0x00, 0x00, 0x00, 0x00, 0x12,
                    0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x01, 0x02, 0x03, 0x04, 0x05, 0x1A, 0x2B, 0x3C,
                    0x4D, 0x50, 0x12, 0x01, 0x73};
            assertArrayEquals(expected, actual);

        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void test0520AdvicePack() {
        try {

            As2805Suncorp msg = new As2805Suncorp();

            msg.setMsgType(Iso8583.MsgType._0520_ACQUIRER_RECONCILE_ADV);
            msg.set(_011_SYS_TRACE_AUDIT_NUM, "112233");
            msg.set(_032_ACQUIRING_INST_ID_CODE, "12345678901");
            msg.set(_041_CARD_ACCEPTOR_TERMINAL_ID, "87654321");
            msg.set(_042_CARD_ACCEPTOR_ID_CODE, "998877665544332");
            msg.set(_066_SETTLEMENT_CODE, "1");
            msg.set(_074_CREDITS_NUMBER,            "0000000001");
            msg.set(_075_CREDITS_REVERSAL_NUMBER,   "0000000000");
            msg.set(_076_DEBITS_NUMBER,             "0000000002");
            msg.set(_077_DEBITS_REVERSAL_NUMBER,    "0000000000");
            msg.set(_078_TRANSFER_NUMBER,           "0000000000");
            msg.set(_079_TRANSFER_REVERSAL_NUMBER,  "0000000000");
            msg.set(_080_INQUIRIES_NUMBER,          "0000000000");
            msg.set(_081_AUTHORISATIONS_NUMBER,     "0000000000");
            msg.set(_086_CREDITS_AMOUNT,            "0000000008123456");
            msg.set(_087_CREDITS_REVERSAL_AMOUNT,   "0000000008123123");
            msg.set(_088_DEBITS_AMOUNT,             "0000000008123123");
            msg.set(_089_DEBITS_REVERSAL_AMOUNT,    "0000000008001200");
            msg.set(_097_AMOUNT_NET_SETTLEMENT,     "430000000008001200");
            msg.set(_118_PAYMENTS_NUMBER,           "0000000100");
            msg.set(_119_PAYMENTS_REVERSAL_NUMBER,  "0000000000000100");
            msg.set(_128_MAC, "1A2B3C4D50120173");

            byte[] actual = msg.toMsg();
            byte[] expected = {0x05, 0x20, (byte)0x80, 0x20, 0x00, 0x01, 0x00, (byte)0xC0, 0x00, 0x00, 0x40, 0x7F, (byte)0x87, (byte)0x80, (byte)0x80, 0x00, 0x06, 0x01, 0x11, 0x22, 0x33, 0x11, 0x12, 0x34,
                    0x56, 0x78, (byte)0x90, 0x1F, 0x38, 0x37, 0x36, 0x35, 0x34, 0x33, 0x32, 0x31, 0x39, 0x39, 0x38, 0x38, 0x37, 0x37, 0x36, 0x36, 0x35, 0x35, 0x34, 0x34, 0x33, 0x33,
                    0x32, 0x01, 0x00, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x02, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                    0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x08, 0x12, 0x34, 0x56, 0x00, 0x00,
                    0x00, 0x00, 0x08, 0x12, 0x31, 0x23, 0x00, 0x00, 0x00, 0x00, 0x08, 0x12, 0x31, 0x23, 0x00, 0x00, 0x00, 0x00, 0x08, 0x00, 0x12, 0x00, 0x43, 0x00, 0x00, 0x00,
                    0x00, 0x08, 0x00, 0x12, 0x00, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01, 0x00, 0x1A, 0x2B, 0x3C, 0x4D, 0x50, 0x12, 0x01, 0x73};
            assertArrayEquals(expected, actual);

        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void test0530Unpack() {
        // test that we can unpack the message packed in the 0800 pack test above. not a real-world use case but useful to test unpack logic
        try {
            final byte[] inputData = {0x05, 0x30, (byte)0x80, 0x3A, 0x00, 0x01, 0x02, (byte)0xC0, 0x00, 0x10, 0x40, 0x7F, (byte)0x87, (byte)0x80, (byte)0x80, 0x00, 0x06, 0x00, 0x00, 0x01, (byte)0x84, 0x14, 0x37, 0x37, 0x07, 0x29, 0x07, 0x29, 0x06,
                    0x57, (byte)0x99, 0x42, 0x39, 0x37, 0x39, 0x39, 0x39, 0x36, 0x33, 0x33, 0x38, 0x35, 0x33, 0x31, 0x35, 0x37, 0x39, 0x39, 0x32, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x33, 0x30, 0x31, 0x32, 0x54,
                    0x4D, 0x53, 0x2F, 0x4E, 0x4D, 0x20, 0x66, 0x6C, 0x61, 0x67, 0x73, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00,
                    0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                    0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x10, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x44, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x10, 0x00, 0x00, 0x00,
                    0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 };
            As2805Suncorp msg = new As2805Suncorp(inputData);

            assertEquals( Iso8583.MsgType._0530_ACQUIRER_RECONCILE_ADV_RSP, msg.getMsgType() );
            assertEquals( "000184", msg.get(_011_SYS_TRACE_AUDIT_NUM));
            assertEquals( "579942", msg.get(_032_ACQUIRING_INST_ID_CODE));
            assertEquals( "99963385", msg.get(_041_CARD_ACCEPTOR_TERMINAL_ID));
            assertEquals( "315799200000003", msg.get(_042_CARD_ACCEPTOR_ID_CODE));
            assertEquals( "TMS/NM flags", msg.get(_060_ADDITIONAL_PRIVATE));
            assertEquals( "1", msg.get(_066_SETTLEMENT_CODE));
            assertEquals( "0000000000", msg.get(_074_CREDITS_NUMBER));
            assertEquals( "0000000000", msg.get(_075_CREDITS_REVERSAL_NUMBER));
            assertEquals( "0000000001", msg.get(_076_DEBITS_NUMBER));
            assertEquals( "0000000000", msg.get(_077_DEBITS_REVERSAL_NUMBER));
            assertEquals( "0000000000", msg.get(_078_TRANSFER_NUMBER));
            assertEquals( "0000000000", msg.get(_079_TRANSFER_REVERSAL_NUMBER));
            assertEquals( "0000000000", msg.get(_080_INQUIRIES_NUMBER));
            assertEquals( "0000000000", msg.get(_081_AUTHORISATIONS_NUMBER));

            assertEquals( "0000000000000000", msg.get(_086_CREDITS_AMOUNT));
            assertEquals( "0000000000000000", msg.get(_087_CREDITS_REVERSAL_AMOUNT));
            assertEquals( "0000000000001000", msg.get(_088_DEBITS_AMOUNT));
            assertEquals( "0000000000000000", msg.get(_089_DEBITS_REVERSAL_AMOUNT));
            assertEquals( "440000000000001000", msg.get(_097_AMOUNT_NET_SETTLEMENT));
            assertEquals( "0000000000", msg.get(_118_CASHOUTS_NUMBER));
            assertEquals( "0000000000000000", msg.get(_119_CASHOUTS_AMOUNT));

        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void test0800Pack() {
        try {

            As2805Suncorp msg = new As2805Suncorp();

            msg.setMsgType(Iso8583.MsgType._0800_NWRK_MNG_REQ);
            msg.set(_011_SYS_TRACE_AUDIT_NUM, "112233");
            msg.set(_032_ACQUIRING_INST_ID_CODE, "12345678901");
            msg.set(_041_CARD_ACCEPTOR_TERMINAL_ID, "87654321");
            msg.set(_042_CARD_ACCEPTOR_ID_CODE, "998877665544332");
            msg.set(_048_ADDITIONAL_DATA, "74657374206164646974696F6E616C207072697661746520646174612068657265");
            msg.set(_060_ADDITIONAL_PRIVATE, "s/w vers 1234");
            msg.set(_070_NMIC, "001");

            byte[] actual = msg.toMsg();
            byte[] expected = {0x08, 0x00, (byte)0x80, 0x20, 0x00, 0x01, 0x00, (byte)0xC1, 0x00, 0x10, 0x04, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x11, 0x22, 0x33, 0x11, 0x12, 0x34, 0x56, 0x78,
                    (byte)0x90, 0x1F, 0x38, 0x37, 0x36, 0x35, 0x34, 0x33, 0x32, 0x31, 0x39, 0x39, 0x38, 0x38, 0x37, 0x37, 0x36, 0x36, 0x35, 0x35, 0x34, 0x34, 0x33, 0x33, 0x32, 0x30, 0x33, 0x33, 0x74,
                    0x65, 0x73, 0x74, 0x20, 0x61, 0x64, 0x64, 0x69, 0x74, 0x69, 0x6F, 0x6E, 0x61, 0x6C, 0x20, 0x70, 0x72, 0x69, 0x76, 0x61, 0x74, 0x65, 0x20, 0x64, 0x61, 0x74, 0x61, 0x20, 0x68, 0x65,
                    0x72, 0x65, 0x30, 0x31, 0x33, 0x73, 0x2F, 0x77, 0x20, 0x76, 0x65, 0x72, 0x73, 0x20, 0x31, 0x32, 0x33, 0x34, 0x00, 0x01 };
            assertArrayEquals(expected, actual);

        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void test0820Pack() {
        try {

            As2805Suncorp msg = new As2805Suncorp();

            msg.setMsgType(_0820_NWRK_MGMT_KEY_CHANGE_ADVICE);
            msg.set(_011_SYS_TRACE_AUDIT_NUM, "112233");
            msg.set(_024_NII, "123");
            msg.set(_032_ACQUIRING_INST_ID_CODE, "1234567");
            msg.set(_041_CARD_ACCEPTOR_TERMINAL_ID, "87654321");
            msg.set(_042_CARD_ACCEPTOR_ID_CODE, "998877665544332");
            msg.set(_053_SECURITY_RELATED_CONTROL_INFORMATION, "0123456789012345");
            msg.set(_070_NMIC, "001");

            byte[] actual = msg.toMsg();
            byte[] expected = {0x08, 0x20, (byte)0x80, 0x20, 0x01, 0x01, 0x00, (byte)0xC0, 0x08, 0x00, 0x04, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x11,
                    0x22, 0x33, 0x01, 0x23, 0x07, 0x12, 0x34, 0x56, 0x7F, 0x38, 0x37, 0x36, 0x35, 0x34, 0x33, 0x32, 0x31, 0x39, 0x39, 0x38, 0x38,
                    0x37, 0x37, 0x36, 0x36, 0x35, 0x35, 0x34, 0x34, 0x33, 0x33, 0x32, 0x01, 0x23, 0x45, 0x67, (byte)0x89, 0x01, 0x23, 0x45, 0x00, 0x01 };
            assertArrayEquals(expected, actual);

        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void test9820Pack() {
        try {

            As2805Suncorp msg = new As2805Suncorp();

            msg.setMsgType(_9820_PRIVATE_NETWORK_MSG_REQUEST);
            msg.set(_041_CARD_ACCEPTOR_TERMINAL_ID, "87654321");
            msg.set(_042_CARD_ACCEPTOR_ID_CODE, "998877665544332");
            msg.set(_048_ADDITIONAL_DATA,  "6B6579206461746120676F657320696E2068657265"); // key data goes here
            msg.set(_070_NMIC, "191");

            byte[] actual = msg.toMsg();
            byte[] expected = {(byte)0x98, 0x20, (byte)0x80, 0x00, 0x00, 0x00, 0x00, (byte)0xC1, 0x00, 0x00, 0x04, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                    0x38, 0x37, 0x36, 0x35, 0x34, 0x33, 0x32, 0x31,
                    0x39, 0x39, 0x38, 0x38, 0x37, 0x37, 0x36, 0x36, 0x35, 0x35, 0x34, 0x34, 0x33, 0x33, 0x32,
                    0x30, 0x32, 0x31, 0x6B, 0x65, 0x79, 0x20, 0x64, 0x61, 0x74, 0x61, 0x20, 0x67, 0x6F, 0x65, 0x73, 0x20, 0x69, 0x6E, 0x20, 0x68, 0x65, 0x72, 0x65, // Key data here
                    0x01, (byte)0x91 };
            assertArrayEquals(expected, actual);

        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void test0810Unpack() {
        // test that we can unpack the message packed in the 0800 pack test above. not a real-world use case but useful to test unpack logic
        try {
            final byte[] inputData = {0x08, 0x00, (byte)0x80, 0x20, 0x00, 0x01, 0x00, (byte)0xC1, 0x00, 0x10, 0x04, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x11, 0x22, 0x33, 0x11, 0x12, 0x34, 0x56, 0x78,
                    (byte)0x90, 0x1F, 0x38, 0x37, 0x36, 0x35, 0x34, 0x33, 0x32, 0x31, 0x39, 0x39, 0x38, 0x38, 0x37, 0x37, 0x36, 0x36, 0x35, 0x35, 0x34, 0x34, 0x33, 0x33, 0x32,
                    0x30, 0x33, 0x33, // Field 48 length
                    0x74, 0x65, 0x73, 0x74, 0x20, 0x61, 0x64, 0x64, 0x69, 0x74, 0x69, 0x6F, 0x6E, 0x61, 0x6C, 0x20, 0x70, 0x72, 0x69, 0x76, 0x61, 0x74, 0x65, 0x20, 0x64, 0x61, 0x74, 0x61, 0x20, 0x68, 0x65, 0x72, 0x65,// field 48 data
                    0x30, 0x31, 0x33, 0x73, 0x2F, 0x77, 0x20, 0x76, 0x65, 0x72, 0x73, 0x20, 0x31, 0x32, 0x33, 0x34, 0x00, 0x01 };
            As2805Suncorp msg = new As2805Suncorp(inputData);

            assertEquals( Iso8583.MsgType._0800_NWRK_MNG_REQ, msg.getMsgType() );
            assertEquals( "112233", msg.get(_011_SYS_TRACE_AUDIT_NUM));
            assertEquals( "12345678901", msg.get(_032_ACQUIRING_INST_ID_CODE));
            assertEquals( "87654321", msg.get(_041_CARD_ACCEPTOR_TERMINAL_ID));
            assertEquals( "998877665544332", msg.get(_042_CARD_ACCEPTOR_ID_CODE));
            assertEquals( "74657374206164646974696F6E616C207072697661746520646174612068657265", msg.get(_048_ADDITIONAL_DATA)); // This now will return the string value
            assertEquals( "s/w vers 1234", msg.get(_060_ADDITIONAL_PRIVATE));
            assertEquals( "001", msg.get(_070_NMIC));

        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

