package eft.com.eftcore;

import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583Rev93.Bit._002_PAN;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583Rev93.Bit._003_PROC_CODE;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583Rev93.Bit._004_TRAN_AMOUNT;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583Rev93.Bit._007_TRAN_DATE_TIME;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583Rev93.Bit._011_SYS_TRACE_AUDIT_NUM;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583Rev93.Bit._012_LOCAL_TRAN_DATETIME;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583Rev93.Bit._014_EXPIRY_DATE;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583Rev93.Bit._022_POS_DATA_CODE;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583Rev93.Bit._023_CARD_SEQ_NR;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583Rev93.Bit._024_FUNC_CODE;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583Rev93.Bit._028_RECON_DATE;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583Rev93.Bit._029_RECON_INDICATOR;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583Rev93.Bit._032_ACQ_INST_ID_CODE;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583Rev93.Bit._035_TRACK_2_DATA;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583Rev93.Bit._037_RET_REF_NR;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583Rev93.Bit._038_APPROVAL_CODE;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583Rev93.Bit._039_ACTION_CODE;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583Rev93.Bit._040_SERVICE_CODE;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583Rev93.Bit._041_TERMINAL_ID;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583Rev93.Bit._042_CARD_ACCEPTOR_ID;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583Rev93.Bit._046_FEES_AMOUNTS;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583Rev93.Bit._049_TRAN_CURRENCY_CODE;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583Rev93.Bit._052_PIN_DATA;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583Rev93.Bit._054_ADDITIONAL_AMOUNTS;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583Rev93.Bit._056_ORIG_DATA_ELEMENTS;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583Rev93.Bit._064_MAC;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583Rev93.Bit._096_KEY_MANAGEMENT_DATA;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583Rev93.Bit._123_RECEIPT_DATA;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583Rev93.Bit._124_DISPLAY_DATA;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583Rev93.Bit._128_MAC;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583Rev93.MsgType._1200_TRAN_REQ;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583Rev93.MsgType._1210_TRAN_REQ_RSP;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583Rev93.MsgType._1420_TRAN_REV_ADV;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583Rev93.MsgType._1430_TRAN_REV_ADV_RSP;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583Rev93.MsgType._1804_NWRK_MNG_REQ;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583Rev93.MsgType._1814_NWRK_MNG_REQ_RSP;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.termappiso.AdditionalData.Field.AuthProfile;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.termappiso.AdditionalData.Field.PosData;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.termappiso.AdditionalData.Field.RoutingInfo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.linkly.libengine.engine.protocol.iso8583.As2805EftexUtils;
import com.linkly.libengine.engine.protocol.iso8583.openisoj.As2805Eftex;
import com.linkly.libengine.engine.protocol.iso8583.openisoj.HashtableMessage;
import com.linkly.libengine.engine.protocol.iso8583.openisoj.termappiso.AdditionalData;
import com.linkly.libmal.global.util.Util;
import com.linkly.libsecapp.emv.EmvTag;
import com.linkly.libsecapp.emv.EmvTags;
import com.linkly.libsecapp.emv.Tag;

import org.junit.Test;

import java.util.concurrent.TimeUnit;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class As2805EftexTests {

    @Test
    public void test1804RSA897Pack() {
        try {

            As2805Eftex msg = new As2805Eftex();

            msg.setMsgType(_1804_NWRK_MNG_REQ);
            msg.set(_007_TRAN_DATE_TIME, "1014085143");
            msg.set(_011_SYS_TRACE_AUDIT_NUM, "000022");
            msg.set(_012_LOCAL_TRAN_DATETIME, "211014215143");
            msg.set(_024_FUNC_CODE, "897");
            msg.set(_041_TERMINAL_ID, "12344321");
            msg.set(_042_CARD_ACCEPTOR_ID, "102505780879851");
            msg.set(_096_KEY_MANAGEMENT_DATA, "0111170090654593051250FE14E8EB7A7493A9782D99C83B52818AD63EF79930100CA6726469F664522FE84D0A861ED00DBD66D15FE48846773DB06A9513F70C889EA6EFE4871549BB4650D9ACCFBC214CAE46ED2D7E41434CD1320CF2A0250B26708B156095D1ABF32D1AB82B474E0A3626CC316FDA232BAF03EFD3E9F5FF3F68B11716B9F5D963576DE7FECCD742488326BDDA8E245324F57A2DA3104A8CC64227F7AA4B15B1FD2E6BAC0E607EC43BD5BF0916713389C2E2E90A313F6BDA11CACE888A7075709E6C029B65547CB0723A7920C630A4E3D8EAC182BAF8901F34E6B3F33D247FB0E09CE03B3C31A2BE6ED0E61FF16D8D7636A957C2E45C9A549BEE702F7BB5C27060B38E3F89AA06A066B16D9E01EE3D8DBAC2B7976D2469835C1854E062234DD112D6D6E766C492DE942A77DF22459986F2727EE9886920BCBA5CF0093FADBE09435E37EA7A8B161D0FB131EAA740941E7330800A4D5B0DF19C62282D234024D9242D58411C3B67D2C8A78EF39F0FE9C70DDD6A49B872D497BD20380C5A793F5CCAB2741EEC3C273DBAEF69FA033C0F3D06419560E0F1807B81CCCEB20C6D321254787828F5F2503CB0F3EC9254CB4BBEEDD4D42507076F6EAF58180A5F24EC2D3064665D0441A4E46A2AC7B369CDD1A483D4C1DB9456C3F614B10A86AEB4E5ACEC73E85D7B8C1395EF2E2FD11E4F01A4D5C02A6CF6D5E03626AD3139EBBD54B85194260003010001");

            byte[] actual = msg.toMsg();
            byte[] expected = {
                    (byte)0x42, (byte)0x31, (byte)0x38, (byte)0x30, (byte)0x34, (byte)0x82, (byte)0x30, (byte)0x01, (byte)0x00, (byte)0x00, (byte)0xC0, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
                    (byte)0x01, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x31, (byte)0x30, (byte)0x31, (byte)0x34, (byte)0x30, (byte)0x38, (byte)0x35, (byte)0x31, (byte)0x34, (byte)0x33, (byte)0x30,
                    (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x32, (byte)0x32, (byte)0x32, (byte)0x31, (byte)0x31, (byte)0x30, (byte)0x31, (byte)0x34, (byte)0x32, (byte)0x31, (byte)0x35, (byte)0x31, (byte)0x34,
                    (byte)0x33, (byte)0x38, (byte)0x39, (byte)0x37, (byte)0x31, (byte)0x32, (byte)0x33, (byte)0x34, (byte)0x34, (byte)0x33, (byte)0x32, (byte)0x31, (byte)0x31, (byte)0x30, (byte)0x32, (byte)0x35,
                    (byte)0x30, (byte)0x35, (byte)0x37, (byte)0x38, (byte)0x30, (byte)0x38, (byte)0x37, (byte)0x39, (byte)0x38, (byte)0x35, (byte)0x31, (byte)0x35, (byte)0x32, (byte)0x37, (byte)0x01, (byte)0x11,
                    (byte)0x17, (byte)0x00, (byte)0x90, (byte)0x65, (byte)0x45, (byte)0x93, (byte)0x05, (byte)0x12, (byte)0x50, (byte)0xFE, (byte)0x14, (byte)0xE8, (byte)0xEB, (byte)0x7A, (byte)0x74, (byte)0x93,
                    (byte)0xA9, (byte)0x78, (byte)0x2D, (byte)0x99, (byte)0xC8, (byte)0x3B, (byte)0x52, (byte)0x81, (byte)0x8A, (byte)0xD6, (byte)0x3E, (byte)0xF7, (byte)0x99, (byte)0x30, (byte)0x10, (byte)0x0C,
                    (byte)0xA6, (byte)0x72, (byte)0x64, (byte)0x69, (byte)0xF6, (byte)0x64, (byte)0x52, (byte)0x2F, (byte)0xE8, (byte)0x4D, (byte)0x0A, (byte)0x86, (byte)0x1E, (byte)0xD0, (byte)0x0D, (byte)0xBD,
                    (byte)0x66, (byte)0xD1, (byte)0x5F, (byte)0xE4, (byte)0x88, (byte)0x46, (byte)0x77, (byte)0x3D, (byte)0xB0, (byte)0x6A, (byte)0x95, (byte)0x13, (byte)0xF7, (byte)0x0C, (byte)0x88, (byte)0x9E,
                    (byte)0xA6, (byte)0xEF, (byte)0xE4, (byte)0x87, (byte)0x15, (byte)0x49, (byte)0xBB, (byte)0x46, (byte)0x50, (byte)0xD9, (byte)0xAC, (byte)0xCF, (byte)0xBC, (byte)0x21, (byte)0x4C, (byte)0xAE,
                    (byte)0x46, (byte)0xED, (byte)0x2D, (byte)0x7E, (byte)0x41, (byte)0x43, (byte)0x4C, (byte)0xD1, (byte)0x32, (byte)0x0C, (byte)0xF2, (byte)0xA0, (byte)0x25, (byte)0x0B, (byte)0x26, (byte)0x70,
                    (byte)0x8B, (byte)0x15, (byte)0x60, (byte)0x95, (byte)0xD1, (byte)0xAB, (byte)0xF3, (byte)0x2D, (byte)0x1A, (byte)0xB8, (byte)0x2B, (byte)0x47, (byte)0x4E, (byte)0x0A, (byte)0x36, (byte)0x26,
                    (byte)0xCC, (byte)0x31, (byte)0x6F, (byte)0xDA, (byte)0x23, (byte)0x2B, (byte)0xAF, (byte)0x03, (byte)0xEF, (byte)0xD3, (byte)0xE9, (byte)0xF5, (byte)0xFF, (byte)0x3F, (byte)0x68, (byte)0xB1,
                    (byte)0x17, (byte)0x16, (byte)0xB9, (byte)0xF5, (byte)0xD9, (byte)0x63, (byte)0x57, (byte)0x6D, (byte)0xE7, (byte)0xFE, (byte)0xCC, (byte)0xD7, (byte)0x42, (byte)0x48, (byte)0x83, (byte)0x26,
                    (byte)0xBD, (byte)0xDA, (byte)0x8E, (byte)0x24, (byte)0x53, (byte)0x24, (byte)0xF5, (byte)0x7A, (byte)0x2D, (byte)0xA3, (byte)0x10, (byte)0x4A, (byte)0x8C, (byte)0xC6, (byte)0x42, (byte)0x27,
                    (byte)0xF7, (byte)0xAA, (byte)0x4B, (byte)0x15, (byte)0xB1, (byte)0xFD, (byte)0x2E, (byte)0x6B, (byte)0xAC, (byte)0x0E, (byte)0x60, (byte)0x7E, (byte)0xC4, (byte)0x3B, (byte)0xD5, (byte)0xBF,
                    (byte)0x09, (byte)0x16, (byte)0x71, (byte)0x33, (byte)0x89, (byte)0xC2, (byte)0xE2, (byte)0xE9, (byte)0x0A, (byte)0x31, (byte)0x3F, (byte)0x6B, (byte)0xDA, (byte)0x11, (byte)0xCA, (byte)0xCE,
                    (byte)0x88, (byte)0x8A, (byte)0x70, (byte)0x75, (byte)0x70, (byte)0x9E, (byte)0x6C, (byte)0x02, (byte)0x9B, (byte)0x65, (byte)0x54, (byte)0x7C, (byte)0xB0, (byte)0x72, (byte)0x3A, (byte)0x79,
                    (byte)0x20, (byte)0xC6, (byte)0x30, (byte)0xA4, (byte)0xE3, (byte)0xD8, (byte)0xEA, (byte)0xC1, (byte)0x82, (byte)0xBA, (byte)0xF8, (byte)0x90, (byte)0x1F, (byte)0x34, (byte)0xE6, (byte)0xB3,
                    (byte)0xF3, (byte)0x3D, (byte)0x24, (byte)0x7F, (byte)0xB0, (byte)0xE0, (byte)0x9C, (byte)0xE0, (byte)0x3B, (byte)0x3C, (byte)0x31, (byte)0xA2, (byte)0xBE, (byte)0x6E, (byte)0xD0, (byte)0xE6,
                    (byte)0x1F, (byte)0xF1, (byte)0x6D, (byte)0x8D, (byte)0x76, (byte)0x36, (byte)0xA9, (byte)0x57, (byte)0xC2, (byte)0xE4, (byte)0x5C, (byte)0x9A, (byte)0x54, (byte)0x9B, (byte)0xEE, (byte)0x70,
                    (byte)0x2F, (byte)0x7B, (byte)0xB5, (byte)0xC2, (byte)0x70, (byte)0x60, (byte)0xB3, (byte)0x8E, (byte)0x3F, (byte)0x89, (byte)0xAA, (byte)0x06, (byte)0xA0, (byte)0x66, (byte)0xB1, (byte)0x6D,
                    (byte)0x9E, (byte)0x01, (byte)0xEE, (byte)0x3D, (byte)0x8D, (byte)0xBA, (byte)0xC2, (byte)0xB7, (byte)0x97, (byte)0x6D, (byte)0x24, (byte)0x69, (byte)0x83, (byte)0x5C, (byte)0x18, (byte)0x54,
                    (byte)0xE0, (byte)0x62, (byte)0x23, (byte)0x4D, (byte)0xD1, (byte)0x12, (byte)0xD6, (byte)0xD6, (byte)0xE7, (byte)0x66, (byte)0xC4, (byte)0x92, (byte)0xDE, (byte)0x94, (byte)0x2A, (byte)0x77,
                    (byte)0xDF, (byte)0x22, (byte)0x45, (byte)0x99, (byte)0x86, (byte)0xF2, (byte)0x72, (byte)0x7E, (byte)0xE9, (byte)0x88, (byte)0x69, (byte)0x20, (byte)0xBC, (byte)0xBA, (byte)0x5C, (byte)0xF0,
                    (byte)0x09, (byte)0x3F, (byte)0xAD, (byte)0xBE, (byte)0x09, (byte)0x43, (byte)0x5E, (byte)0x37, (byte)0xEA, (byte)0x7A, (byte)0x8B, (byte)0x16, (byte)0x1D, (byte)0x0F, (byte)0xB1, (byte)0x31,
                    (byte)0xEA, (byte)0xA7, (byte)0x40, (byte)0x94, (byte)0x1E, (byte)0x73, (byte)0x30, (byte)0x80, (byte)0x0A, (byte)0x4D, (byte)0x5B, (byte)0x0D, (byte)0xF1, (byte)0x9C, (byte)0x62, (byte)0x28,
                    (byte)0x2D, (byte)0x23, (byte)0x40, (byte)0x24, (byte)0xD9, (byte)0x24, (byte)0x2D, (byte)0x58, (byte)0x41, (byte)0x1C, (byte)0x3B, (byte)0x67, (byte)0xD2, (byte)0xC8, (byte)0xA7, (byte)0x8E,
                    (byte)0xF3, (byte)0x9F, (byte)0x0F, (byte)0xE9, (byte)0xC7, (byte)0x0D, (byte)0xDD, (byte)0x6A, (byte)0x49, (byte)0xB8, (byte)0x72, (byte)0xD4, (byte)0x97, (byte)0xBD, (byte)0x20, (byte)0x38,
                    (byte)0x0C, (byte)0x5A, (byte)0x79, (byte)0x3F, (byte)0x5C, (byte)0xCA, (byte)0xB2, (byte)0x74, (byte)0x1E, (byte)0xEC, (byte)0x3C, (byte)0x27, (byte)0x3D, (byte)0xBA, (byte)0xEF, (byte)0x69,
                    (byte)0xFA, (byte)0x03, (byte)0x3C, (byte)0x0F, (byte)0x3D, (byte)0x06, (byte)0x41, (byte)0x95, (byte)0x60, (byte)0xE0, (byte)0xF1, (byte)0x80, (byte)0x7B, (byte)0x81, (byte)0xCC, (byte)0xCE,
                    (byte)0xB2, (byte)0x0C, (byte)0x6D, (byte)0x32, (byte)0x12, (byte)0x54, (byte)0x78, (byte)0x78, (byte)0x28, (byte)0xF5, (byte)0xF2, (byte)0x50, (byte)0x3C, (byte)0xB0, (byte)0xF3, (byte)0xEC,
                    (byte)0x92, (byte)0x54, (byte)0xCB, (byte)0x4B, (byte)0xBE, (byte)0xED, (byte)0xD4, (byte)0xD4, (byte)0x25, (byte)0x07, (byte)0x07, (byte)0x6F, (byte)0x6E, (byte)0xAF, (byte)0x58, (byte)0x18,
                    (byte)0x0A, (byte)0x5F, (byte)0x24, (byte)0xEC, (byte)0x2D, (byte)0x30, (byte)0x64, (byte)0x66, (byte)0x5D, (byte)0x04, (byte)0x41, (byte)0xA4, (byte)0xE4, (byte)0x6A, (byte)0x2A, (byte)0xC7,
                    (byte)0xB3, (byte)0x69, (byte)0xCD, (byte)0xD1, (byte)0xA4, (byte)0x83, (byte)0xD4, (byte)0xC1, (byte)0xDB, (byte)0x94, (byte)0x56, (byte)0xC3, (byte)0xF6, (byte)0x14, (byte)0xB1, (byte)0x0A,
                    (byte)0x86, (byte)0xAE, (byte)0xB4, (byte)0xE5, (byte)0xAC, (byte)0xEC, (byte)0x73, (byte)0xE8, (byte)0x5D, (byte)0x7B, (byte)0x8C, (byte)0x13, (byte)0x95, (byte)0xEF, (byte)0x2E, (byte)0x2F,
                    (byte)0xD1, (byte)0x1E, (byte)0x4F, (byte)0x01, (byte)0xA4, (byte)0xD5, (byte)0xC0, (byte)0x2A, (byte)0x6C, (byte)0xF6, (byte)0xD5, (byte)0xE0, (byte)0x36, (byte)0x26, (byte)0xAD, (byte)0x31,
                    (byte)0x39, (byte)0xEB, (byte)0xBD, (byte)0x54, (byte)0xB8, (byte)0x51, (byte)0x94, (byte)0x26, (byte)0x00, (byte)0x03, (byte)0x01, (byte)0x00, (byte)0x01
            };
            assertEquals(Util.hex2Str(expected), Util.hex2Str(actual));
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void test1314RSA897Unpack() {

        try {
            final byte[] inputData = {
                    (byte)0x42, (byte)0x31, (byte)0x38, (byte)0x31, (byte)0x34, (byte)0x82, (byte)0x30, (byte)0x00, (byte)0x00, (byte)0x02, (byte)0xC0, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
                    (byte)0x01, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x31, (byte)0x30, (byte)0x31, (byte)0x34, (byte)0x30, (byte)0x38, (byte)0x35, (byte)0x31, (byte)0x34, (byte)0x33, (byte)0x30,
                    (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x32, (byte)0x32, (byte)0x32, (byte)0x31, (byte)0x31, (byte)0x30, (byte)0x31, (byte)0x34, (byte)0x32, (byte)0x31, (byte)0x35, (byte)0x31, (byte)0x34,
                    (byte)0x33, (byte)0x38, (byte)0x30, (byte)0x30, (byte)0x31, (byte)0x32, (byte)0x33, (byte)0x34, (byte)0x34, (byte)0x33, (byte)0x32, (byte)0x31, (byte)0x31, (byte)0x30, (byte)0x32, (byte)0x35,
                    (byte)0x30, (byte)0x35, (byte)0x37, (byte)0x38, (byte)0x30, (byte)0x38, (byte)0x37, (byte)0x39, (byte)0x38, (byte)0x35, (byte)0x31, (byte)0x32, (byte)0x35, (byte)0x35, (byte)0x02, (byte)0x40,
                    (byte)0xAF, (byte)0x01, (byte)0xED, (byte)0x5A, (byte)0x56, (byte)0x6F, (byte)0xDB, (byte)0xF0, (byte)0x7E, (byte)0x74, (byte)0xD1, (byte)0xB9, (byte)0x8F, (byte)0x6A, (byte)0x91, (byte)0x14,
                    (byte)0x1F, (byte)0x59, (byte)0x85, (byte)0x48, (byte)0xB0, (byte)0x3C, (byte)0x5D, (byte)0x1D, (byte)0x9A, (byte)0x3D, (byte)0x09, (byte)0xB5, (byte)0x42, (byte)0xC3, (byte)0xE6, (byte)0x3E,
                    (byte)0xFA, (byte)0xD7, (byte)0xC8, (byte)0x68, (byte)0x12, (byte)0xC8, (byte)0x34, (byte)0x78, (byte)0xED, (byte)0x0E, (byte)0x26, (byte)0x94, (byte)0x5B, (byte)0x77, (byte)0x55, (byte)0xB6,
                    (byte)0x13, (byte)0xC6, (byte)0x4F, (byte)0x4A, (byte)0x8D, (byte)0x5F, (byte)0x70, (byte)0x6C, (byte)0xA0, (byte)0x24, (byte)0x63, (byte)0x73, (byte)0x74, (byte)0x5C, (byte)0xEC, (byte)0x15,
                    (byte)0xE8, (byte)0x9E, (byte)0x4D, (byte)0xA5, (byte)0xCE, (byte)0x1E, (byte)0xB9, (byte)0xD8, (byte)0xB8, (byte)0x98, (byte)0xC1, (byte)0x5B, (byte)0xE7, (byte)0x93, (byte)0x58, (byte)0x85,
                    (byte)0xC5, (byte)0xE6, (byte)0xDE, (byte)0x48, (byte)0xB8, (byte)0x9F, (byte)0xB7, (byte)0xF3, (byte)0x6B, (byte)0xEE, (byte)0xE4, (byte)0x5D, (byte)0xCE, (byte)0x91, (byte)0x87, (byte)0x92,
                    (byte)0xFE, (byte)0x20, (byte)0x6F, (byte)0xFD, (byte)0xA2, (byte)0x86, (byte)0x27, (byte)0x92, (byte)0xEE, (byte)0xA9, (byte)0x61, (byte)0x86, (byte)0xF8, (byte)0xDE, (byte)0xF5, (byte)0xDD,
                    (byte)0x0A, (byte)0xB0, (byte)0x4F, (byte)0x38, (byte)0x36, (byte)0x93, (byte)0x0C, (byte)0xEB, (byte)0x6A, (byte)0xF7, (byte)0x7D, (byte)0x01, (byte)0x73, (byte)0xF0, (byte)0x2E, (byte)0x40,
                    (byte)0x5F, (byte)0xAD, (byte)0x85, (byte)0x83, (byte)0xF1, (byte)0x90, (byte)0xF9, (byte)0xA4, (byte)0x98, (byte)0x67, (byte)0x0D, (byte)0x0D, (byte)0x8D, (byte)0xD3, (byte)0x8F, (byte)0xF2,
                    (byte)0xD9, (byte)0xC6, (byte)0xBB, (byte)0x9B, (byte)0x56, (byte)0x7E, (byte)0x9A, (byte)0xFF, (byte)0x8C, (byte)0x91, (byte)0x1C, (byte)0x38, (byte)0xE0, (byte)0x46, (byte)0x91, (byte)0xEC,
                    (byte)0x98, (byte)0x9F, (byte)0x56, (byte)0xEA, (byte)0x31, (byte)0x91, (byte)0xF2, (byte)0x46, (byte)0x1C, (byte)0x44, (byte)0xB6, (byte)0x95, (byte)0x0B, (byte)0x0F, (byte)0x55, (byte)0x2A,
                    (byte)0x87, (byte)0x8F, (byte)0xA0, (byte)0xAE, (byte)0x8E, (byte)0xAD, (byte)0x14, (byte)0x92, (byte)0x9A, (byte)0xF4, (byte)0xB5, (byte)0xAE, (byte)0x6D, (byte)0x1B, (byte)0x46, (byte)0xEE,
                    (byte)0x58, (byte)0xA7, (byte)0xD9, (byte)0xE7, (byte)0xF3, (byte)0xCF, (byte)0x8C, (byte)0x45, (byte)0xAB, (byte)0xBD, (byte)0x7D, (byte)0x5D, (byte)0x6D, (byte)0xE1, (byte)0x31, (byte)0x92,
                    (byte)0xC8, (byte)0xFB, (byte)0x61, (byte)0x0A, (byte)0xF9, (byte)0x3B, (byte)0xC5, (byte)0xB9, (byte)0x8A, (byte)0xFB, (byte)0x40, (byte)0xBB, (byte)0x09, (byte)0x78, (byte)0x5F, (byte)0xE7,
                    (byte)0xBA, (byte)0xE6, (byte)0x4D, (byte)0xC8, (byte)0x76, (byte)0xF7, (byte)0x34, (byte)0xB9, (byte)0x4D, (byte)0xA8, (byte)0x35, (byte)0xC9, (byte)0x96, (byte)0xA1, (byte)0xC7, (byte)0xC9,
                    (byte)0x00, (byte)0x03, (byte)0x01, (byte)0x00, (byte)0x01, (byte)0x01, (byte)0x23, (byte)0x45, (byte)0x67, (byte)0x89, (byte)0xAB, (byte)0xCD, (byte)0xEF
            };
            As2805Eftex msg = new As2805Eftex(inputData);

            assertEquals( _1814_NWRK_MNG_REQ_RSP, msg.getMsgType() );
            assertEquals( "1014085143", msg.get(_007_TRAN_DATE_TIME));
            assertEquals( "000022", msg.get(_011_SYS_TRACE_AUDIT_NUM));
            assertEquals( "211014215143", msg.get(_012_LOCAL_TRAN_DATETIME));
            assertEquals( "800", msg.get(_039_ACTION_CODE));
            assertEquals( "12344321", msg.get(_041_TERMINAL_ID));
            assertEquals( "102505780879851", msg.get(_042_CARD_ACCEPTOR_ID));
            assertEquals( "0240AF01ED5A566FDBF07E74D1B98F6A91141F598548B03C5D1D9A3D09B542C3E63EFAD7C86812C83478ED0E26945B7755B613C64F4A8D5F706CA0246373745CEC15E89E4DA5CE1EB9D8B898C15BE7935885C5E6DE48B89FB7F36BEEE45DCE918792FE206FFDA2862792EEA96186F8DEF5DD0AB04F3836930CEB6AF77D0173F02E405FAD8583F190F9A498670D0D8DD38FF2D9C6BB9B567E9AFF8C911C38E04691EC989F56EA3191F2461C44B6950B0F552A878FA0AE8EAD14929AF4B5AE6D1B46EE58A7D9E7F3CF8C45ABBD7D5D6DE13192C8FB610AF93BC5B98AFB40BB09785FE7BAE64DC876F734B94DA835C996A1C7C900030100010123456789ABCDEF",
                    msg.get(_096_KEY_MANAGEMENT_DATA));

        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void test1804LogonPack() {
        try {

            As2805Eftex msg = new As2805Eftex();

            msg.setMsgType(_1804_NWRK_MNG_REQ);
            msg.set(_007_TRAN_DATE_TIME, "1014085150");
            msg.set(_011_SYS_TRACE_AUDIT_NUM, "000025");
            msg.set(_012_LOCAL_TRAN_DATETIME, "211014215150");
            msg.set(_024_FUNC_CODE, "811");
            msg.set(_032_ACQ_INST_ID_CODE, "12345678901" );
            msg.set(_041_TERMINAL_ID, "12344321");
            msg.set(_042_CARD_ACCEPTOR_ID, "102505780879851");
            msg.set(_096_KEY_MANAGEMENT_DATA, "011117009065459331");

            byte[] actual = msg.toMsg();
            byte[] expected = {
                (byte)0x42, (byte)0x31, (byte)0x38, (byte)0x30, (byte)0x34, (byte)0x82, (byte)0x30, (byte)0x01, (byte)0x01, (byte)0x00, (byte)0xC0, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
                (byte)0x01, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x31, (byte)0x30, (byte)0x31, (byte)0x34, (byte)0x30, (byte)0x38, (byte)0x35, (byte)0x31, (byte)0x35, (byte)0x30, (byte)0x30,
                (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x32, (byte)0x35, (byte)0x32, (byte)0x31, (byte)0x31, (byte)0x30, (byte)0x31, (byte)0x34, (byte)0x32, (byte)0x31, (byte)0x35, (byte)0x31, (byte)0x35,
                (byte)0x30, (byte)0x38, (byte)0x31, (byte)0x31, (byte)0x31, (byte)0x31, (byte)0x31, (byte)0x32, (byte)0x33, (byte)0x34, (byte)0x35, (byte)0x36, (byte)0x37, (byte)0x38, (byte)0x39, (byte)0x30,
                (byte)0x31, (byte)0x31, (byte)0x32, (byte)0x33, (byte)0x34, (byte)0x34, (byte)0x33, (byte)0x32, (byte)0x31, (byte)0x31, (byte)0x30, (byte)0x32, (byte)0x35, (byte)0x30, (byte)0x35, (byte)0x37,
                (byte)0x38, (byte)0x30, (byte)0x38, (byte)0x37, (byte)0x39, (byte)0x38, (byte)0x35, (byte)0x31, (byte)0x30, (byte)0x30, (byte)0x39, (byte)0x01, (byte)0x11, (byte)0x17, (byte)0x00, (byte)0x90,
                (byte)0x65, (byte)0x45, (byte)0x93, (byte)0x31
            };
            assertEquals(Util.hex2Str(expected), Util.hex2Str(actual));
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void test1814LogonUnpack() {

        try {
            final byte[] inputData = {
                (byte)0x42, (byte)0x31, (byte)0x38, (byte)0x31, (byte)0x34, (byte)0x82, (byte)0x30, (byte)0x00, (byte)0x00, (byte)0x02, (byte)0xC0, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
                (byte)0x01, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x01, (byte)0x31, (byte)0x30, (byte)0x31, (byte)0x34, (byte)0x30, (byte)0x38, (byte)0x35, (byte)0x31, (byte)0x35, (byte)0x30, (byte)0x30,
                (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x32, (byte)0x35, (byte)0x32, (byte)0x31, (byte)0x31, (byte)0x30, (byte)0x31, (byte)0x34, (byte)0x32, (byte)0x31, (byte)0x35, (byte)0x31, (byte)0x35,
                (byte)0x30, (byte)0x38, (byte)0x30, (byte)0x30, (byte)0x31, (byte)0x32, (byte)0x33, (byte)0x34, (byte)0x34, (byte)0x33, (byte)0x32, (byte)0x31, (byte)0x31, (byte)0x30, (byte)0x32, (byte)0x35,
                (byte)0x30, (byte)0x35, (byte)0x37, (byte)0x38, (byte)0x30, (byte)0x38, (byte)0x37, (byte)0x39, (byte)0x38, (byte)0x35, (byte)0x31, (byte)0x30, (byte)0x38, (byte)0x33, (byte)0xBE, (byte)0x5F,
                (byte)0xBF, (byte)0xF5, (byte)0x4A, (byte)0x82, (byte)0xDD, (byte)0x39, (byte)0x5A, (byte)0x0A, (byte)0x71, (byte)0x70, (byte)0x31, (byte)0x5E, (byte)0x8B, (byte)0xA8, (byte)0xF8, (byte)0xB1,
                (byte)0x3A, (byte)0xFF, (byte)0x44, (byte)0x2C, (byte)0x4A, (byte)0x12, (byte)0x11, (byte)0xDD, (byte)0xBB, (byte)0x91, (byte)0x57, (byte)0xE6, (byte)0x68, (byte)0xB6, (byte)0x27, (byte)0x77,
                (byte)0x6C, (byte)0x96, (byte)0x17, (byte)0x9C, (byte)0x85, (byte)0xC7, (byte)0x4F, (byte)0x3F, (byte)0xB1, (byte)0x61, (byte)0xE4, (byte)0x43, (byte)0x59, (byte)0xC6, (byte)0x63, (byte)0xB7,
                (byte)0x79, (byte)0xBC, (byte)0xB9, (byte)0x55, (byte)0xFE, (byte)0x8E, (byte)0x42, (byte)0x53, (byte)0xE4, (byte)0x09, (byte)0x26, (byte)0x26, (byte)0x04, (byte)0x03, (byte)0x01, (byte)0x7A,
                (byte)0x99, (byte)0xD5, (byte)0x21, (byte)0x19, (byte)0x2B, (byte)0x5F, (byte)0x2F, (byte)0x34, (byte)0xBA, (byte)0x28, (byte)0x8F, (byte)0x71, (byte)0x65, (byte)0x3C, (byte)0x86, (byte)0xC3,
                (byte)0x43, (byte)0x48, (byte)0x16, (byte)0x79, (byte)0xAF, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
            };
            As2805Eftex msg = new As2805Eftex(inputData);

            assertEquals( _1814_NWRK_MNG_REQ_RSP, msg.getMsgType() );
            assertEquals( "1014085150", msg.get(_007_TRAN_DATE_TIME));
            assertEquals( "000025", msg.get(_011_SYS_TRACE_AUDIT_NUM));
            assertEquals( "211014215150", msg.get(_012_LOCAL_TRAN_DATETIME));
            assertEquals( "800", msg.get(_039_ACTION_CODE));
            assertEquals( "12344321", msg.get(_041_TERMINAL_ID));
            assertEquals( "102505780879851", msg.get(_042_CARD_ACCEPTOR_ID));
            assertEquals( "BE5FBFF54A82DD395A0A7170315E8BA8F8B13AFF442C4A1211DDBB9157E668B627776C96179C85C74F3FB161E44359C663B779BCB955FE8E4253E40926260403017A99D521192B5F2F34BA288F71653C86C343",
                    msg.get(_096_KEY_MANAGEMENT_DATA));
            assertEquals( "481679AF00000000", msg.get(_128_MAC) );

        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void test48AdditionalData() {
        try {
            AdditionalData addData = new AdditionalData();
            addData.put(PosData, "1234432100000300000");
            byte[] actual = addData.toMsg();
            byte[] expected = {
                    '0', '0', '2', '4', (byte)0xF0, (byte)0x00, (byte)0x15, (byte)0x80, (byte)0x00, (byte)0x31, (byte)0x32, (byte)0x33, (byte)0x34, (byte)0x34, (byte)0x33, (byte)0x32, (byte)0x31, (byte)0x30,
                    (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x33, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x30
            };
            assertEquals(Util.hex2Str(expected), Util.hex2Str(actual));
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @Test
    public void testIccDataPack() {
        EmvTags tags = new EmvTags();
        tags.add(Tag.tvr.value(), new byte[] { 0x00, 0x00, 0x00, 0x00, 0x00 });
        tags.add(Tag.tsi.value(), new byte[] { (byte)0xF8, 0x00 });

        byte[] packed = tags.pack();
        assertEquals(Util.hex2Str(packed), "950500000000009B02F800");

        // wrap in FF20 tag
        EmvTag containerTag = new EmvTag( 0xFF20, packed );
        assertEquals(Util.hex2Str(containerTag.pack()), "FF200B950500000000009B02F800");

    }

    @Test
    public void test1200PurchasePack() {
        try {

            As2805Eftex msg = new As2805Eftex();

            msg.setMsgType(_1200_TRAN_REQ);
            msg.set(_003_PROC_CODE, "000000");
            msg.set(_004_TRAN_AMOUNT, "000000001234");
            msg.set(_007_TRAN_DATE_TIME, "1014100151");
            msg.set(_011_SYS_TRACE_AUDIT_NUM, "000026");
            msg.set(_012_LOCAL_TRAN_DATETIME, "211014230138");
            msg.set(_022_POS_DATA_CODE,"51010171310C001");
            msg.set(_023_CARD_SEQ_NR,"001");
            msg.set(_024_FUNC_CODE, "100");
            msg.set(_029_RECON_INDICATOR,"001");
            msg.set(_035_TRACK_2_DATA,"4761739001010119=22122011758928889");
            msg.set(_041_TERMINAL_ID, "12344321");
            msg.set(_042_CARD_ACCEPTOR_ID, "102505780879851");

            AdditionalData addData = new AdditionalData();
            addData.put(PosData, "1234432100000300000");
            msg.putAdditionalData(addData);

//            msg.set(_048_PRIVATE_ADDITIONAL_DATA, "F00015800031323334343332313030303030333030303030");
            msg.set(_049_TRAN_CURRENCY_CODE,"036");
            msg.set(_052_PIN_DATA,"AD8114B401BEC1A9");

            msg.set(_064_MAC,"321329A700000000");
//            msg.set();

            byte[] actual = msg.toMsg();
            byte[] expected = {
                (byte)0x42, (byte)0x31, (byte)0x32, (byte)0x30, (byte)0x30, (byte)0x32, (byte)0x30, (byte)0x07, (byte)0x08, (byte)0x20, (byte)0xC1, (byte)0x90, (byte)0x01, (byte)0x30, (byte)0x30, (byte)0x30,
                (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x31, (byte)0x32, (byte)0x33, (byte)0x34, (byte)0x31,
                (byte)0x30, (byte)0x31, (byte)0x34, (byte)0x31, (byte)0x30, (byte)0x30, (byte)0x31, (byte)0x35, (byte)0x31, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x32, (byte)0x36, (byte)0x32,
                (byte)0x31, (byte)0x31, (byte)0x30, (byte)0x31, (byte)0x34, (byte)0x32, (byte)0x33, (byte)0x30, (byte)0x31, (byte)0x33, (byte)0x38, (byte)0x35, (byte)0x31, (byte)0x30, (byte)0x31, (byte)0x30,
                (byte)0x31, (byte)0x37, (byte)0x31, (byte)0x33, (byte)0x31, (byte)0x30, (byte)0x43, (byte)0x30, (byte)0x30, (byte)0x31, (byte)0x30, (byte)0x30, (byte)0x31, (byte)0x31, (byte)0x30, (byte)0x30,
                (byte)0x30, (byte)0x30, (byte)0x31, (byte)0x33, (byte)0x34, (byte)0x34, (byte)0x37, (byte)0x36, (byte)0x31, (byte)0x37, (byte)0x33, (byte)0x39, (byte)0x30, (byte)0x30, (byte)0x31, (byte)0x30,
                (byte)0x31, (byte)0x30, (byte)0x31, (byte)0x31, (byte)0x39, (byte)0x3D, (byte)0x32, (byte)0x32, (byte)0x31, (byte)0x32, (byte)0x32, (byte)0x30, (byte)0x31, (byte)0x31, (byte)0x37, (byte)0x35,
                (byte)0x38, (byte)0x39, (byte)0x32, (byte)0x38, (byte)0x38, (byte)0x38, (byte)0x39, (byte)0x31, (byte)0x32, (byte)0x33, (byte)0x34, (byte)0x34, (byte)0x33, (byte)0x32, (byte)0x31, (byte)0x31,
                (byte)0x30, (byte)0x32, (byte)0x35, (byte)0x30, (byte)0x35, (byte)0x37, (byte)0x38, (byte)0x30, (byte)0x38, (byte)0x37, (byte)0x39, (byte)0x38, (byte)0x35, (byte)0x31, (byte)0x30, (byte)0x30,
                (byte)0x32, (byte)0x34, (byte)0xF0, (byte)0x00, (byte)0x15, (byte)0x80, (byte)0x00, (byte)0x31, (byte)0x32, (byte)0x33, (byte)0x34, (byte)0x34, (byte)0x33, (byte)0x32, (byte)0x31, (byte)0x30,
                (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x33, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x33, (byte)0x36, (byte)0xAD, (byte)0x81, (byte)0x14,
                (byte)0xB4, (byte)0x01, (byte)0xBE, (byte)0xC1, (byte)0xA9, (byte)0x32, (byte)0x13, (byte)0x29, (byte)0xA7, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
            };
            assertEquals(Util.hex2Str(expected), Util.hex2Str(actual));
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void test1210PurchaseUnpack() {

        try {
            final byte[] inputData = {
                (byte)0x42, (byte)0x31, (byte)0x32, (byte)0x31, (byte)0x30, (byte)0xB2, (byte)0x34, (byte)0x06, (byte)0x10, (byte)0x0F, (byte)0xC5, (byte)0x84, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
                (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x31, (byte)0x30, (byte)0x30, (byte)0x33, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x30,
                (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x32, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x31, (byte)0x31, (byte)0x30, (byte)0x31, (byte)0x32, (byte)0x31, (byte)0x33, (byte)0x34, (byte)0x32,
                (byte)0x31, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x35, (byte)0x33, (byte)0x32, (byte)0x31, (byte)0x31, (byte)0x31, (byte)0x30, (byte)0x32, (byte)0x31, (byte)0x30, (byte)0x33,
                (byte)0x34, (byte)0x30, (byte)0x36, (byte)0x31, (byte)0x37, (byte)0x30, (byte)0x33, (byte)0x35, (byte)0x31, (byte)0x30, (byte)0x31, (byte)0x30, (byte)0x31, (byte)0x35, (byte)0x31, (byte)0x33,
                (byte)0x31, (byte)0x30, (byte)0x43, (byte)0x30, (byte)0x30, (byte)0x31, (byte)0x30, (byte)0x30, (byte)0x31, (byte)0x32, (byte)0x35, (byte)0x30, (byte)0x35, (byte)0x30, (byte)0x34, (byte)0x35,
                (byte)0x38, (byte)0x38, (byte)0x39, (byte)0x39, (byte)0x37, (byte)0x35, (byte)0x37, (byte)0x30, (byte)0x31, (byte)0x36, (byte)0x33, (byte)0x35, (byte)0x34, (byte)0x37, (byte)0x35, (byte)0x34,
                (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x31, (byte)0x30, (byte)0x31, (byte)0x31, (byte)0x32, (byte)0x33, (byte)0x34, (byte)0x34, (byte)0x33, (byte)0x32, (byte)0x31, (byte)0x31,
                (byte)0x30, (byte)0x32, (byte)0x35, (byte)0x30, (byte)0x35, (byte)0x37, (byte)0x38, (byte)0x30, (byte)0x38, (byte)0x37, (byte)0x39, (byte)0x38, (byte)0x35, (byte)0x31, (byte)0x30, (byte)0x36,
                (byte)0x38, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x33, (byte)0x36, (byte)0x43, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x30,
                (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x44, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x30,
                (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x31, (byte)0x30, (byte)0x33, (byte)0x36, (byte)0x43, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x30,
                (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x44, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x30,
                (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x37, (byte)0x34, (byte)0xF0, (byte)0x00, (byte)0x47, (byte)0xC2, (byte)0x00, (byte)0x30, (byte)0x30,
                (byte)0x30, (byte)0x30, (byte)0x39, (byte)0x35, (byte)0x31, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x31, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x30,
                (byte)0x30, (byte)0x31, (byte)0x31, (byte)0x54, (byte)0x65, (byte)0x72, (byte)0x6D, (byte)0x41, (byte)0x70, (byte)0x70, (byte)0x47, (byte)0x4D, (byte)0x20, (byte)0x20, (byte)0x20, (byte)0x53,
                (byte)0x69, (byte)0x6D, (byte)0x53, (byte)0x6E, (byte)0x6B, (byte)0x47, (byte)0x4D, (byte)0x20, (byte)0x20, (byte)0x20, (byte)0x20, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x31,
                (byte)0x38, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x31, (byte)0x38, (byte)0x44, (byte)0x65, (byte)0x66, (byte)0x61, (byte)0x75, (byte)0x6C, (byte)0x74, (byte)0x20, (byte)0x20,
                (byte)0x20, (byte)0x20, (byte)0x20, (byte)0x30, (byte)0x33, (byte)0x36, (byte)0x30, (byte)0x36, (byte)0x30, (byte)0x34, (byte)0x30, (byte)0x30, (byte)0x32, (byte)0x38, (byte)0x34, (byte)0x30,
                (byte)0x43, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x31, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x34, (byte)0x30, (byte)0x30,
                (byte)0x31, (byte)0x38, (byte)0x34, (byte)0x30, (byte)0x44, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x31, (byte)0x30, (byte)0x30, (byte)0x30,
                (byte)0x30, (byte)0x31, (byte)0x30, (byte)0x35, (byte)0x33, (byte)0x30, (byte)0x33, (byte)0x36, (byte)0x44, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x30,
                (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x31, (byte)0x30, (byte)0x30, (byte)0x32, (byte)0x5C, (byte)0x6E, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x0F, (byte)0x18, (byte)0x90,
                (byte)0x90, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
            };
            As2805Eftex msg = new As2805Eftex(inputData);

            // MTI                                                : 1210
            assertEquals( _1210_TRAN_REQ_RSP, msg.getMsgType() );

            // iso8583_f3_processing_code                         : 003000
            assertEquals( "003000", msg.get(_003_PROC_CODE));

           // iso8583_f4_amount_transaction                      : 000000002000
            assertEquals( "000000002000", msg.get(_004_TRAN_AMOUNT));

           // iso8583_f7_date_and_time_transmission              : 1101213421
            assertEquals( "1101213421", msg.get(_007_TRAN_DATE_TIME));

           // iso8583_f11_system_trace_audit_number              : 000053
            assertEquals( "000053", msg.get(_011_SYS_TRACE_AUDIT_NUM));

           // iso8583_f12_date_and_time_local_transaction        : 211102103406
            assertEquals( "211102103406", msg.get(_012_LOCAL_TRAN_DATETIME));

           // iso8583_f14_date_expiration                        : 1703
            assertEquals( "1703", msg.get(_014_EXPIRY_DATE));

           // iso8583_f22_pos_data_code                          : 51010151310C001
            assertEquals( "51010151310C001", msg.get(_022_POS_DATA_CODE));

           // iso8583_f23_card_sequence_number                   : 001
            assertEquals( "001", msg.get(_023_CARD_SEQ_NR));

           // iso8583_f28_date_reconciliation                    : 250504
            assertEquals( "250504", msg.get(_028_RECON_DATE));

           // iso8583_f37_retrieval_reference_number             : 588997570163
            assertEquals( "588997570163", msg.get(_037_RET_REF_NR));

           // iso8583_f38_approval_code                          : 547540
            assertEquals( "547540", msg.get(_038_APPROVAL_CODE));

           // iso8583_f39_action_code                            : 000
            assertEquals( "000", msg.get(_039_ACTION_CODE));

           // iso8583_f40_service_code                           : 101
            assertEquals( "101", msg.get(_040_SERVICE_CODE));

           // iso8583_f41_terminal_id                            : 12344321
            assertEquals( "12344321", msg.get(_041_TERMINAL_ID));

           // iso8583_f42_card_acceptor_id                       : 102505780879851
            assertEquals( "102505780879851", msg.get(_042_CARD_ACCEPTOR_ID));

           // iso8583_f46_amounts_fees                           : 00036C0000000000000000D0000000000001036C0000000000000000D00000000000
            assertEquals( "00036C0000000000000000D0000000000001036C0000000000000000D00000000000", msg.get(_046_FEES_AMOUNTS));

            AdditionalData addData = msg.getAdditionalData();
            assertNotNull(addData);

            // iso8583_f48_additional_data_private                : ­
           // F0 dataset                                         : ­
           // F48 Bitmap                                         : ┬
           // termapp_f48_0_1_pos_data                           : 0000951000000100000
            assertEquals("0000951000000100000", addData.get(PosData));

           // termapp_f48_0_2_authorization_profile              : 11
            assertEquals("11", addData.get(AuthProfile));

           // termapp_f48_0_7_routing_information                : TermAppGM   SimSnkGM    000018000018Default
            assertEquals("TermAppGM   SimSnkGM    000018000018Default     ", addData.get(RoutingInfo));

            // iso8583_f49_currency_code_transaction              : 036
            assertEquals( "036", msg.get(_049_TRAN_CURRENCY_CODE));

           // iso8583_f54_additional_amounts                     : 4002840C0000000100004001840D0000000100001053036D000000000001
            assertEquals( "4002840C0000000100004001840D0000000100001053036D000000000001", msg.get(_054_ADDITIONAL_AMOUNTS));

           // iso8583_f123_receipt_data                          : \n
            assertEquals( "\\n", msg.get(_123_RECEIPT_DATA));

           // iso8583_f124_display_data                          :
            assertEquals( "", msg.get(_124_DISPLAY_DATA));

           // iso8583_f128_message_authentication_code_field     :
            assertEquals( "0F18909000000000", msg.get(_128_MAC));


        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void test1420ReversalPack() {
        try {

            As2805Eftex msg = new As2805Eftex();

            msg.setMsgType(_1420_TRAN_REV_ADV);

            // iso8583_f2_primary_account_number                  : 4761739001010119
            msg.set(_002_PAN, "4761739001010119");

            // iso8583_f3_processing_code                         : 002000
            msg.set(_003_PROC_CODE, "002000");

            // iso8583_f4_amount_transaction                      : 000000001234
            msg.set(_004_TRAN_AMOUNT, "000000001234");

            // iso8583_f7_date_and_time_transmission              : 1101213254
            msg.set(_007_TRAN_DATE_TIME, "1101213254");

            // iso8583_f11_system_trace_audit_number              : 000046
            msg.set(_011_SYS_TRACE_AUDIT_NUM, "000046");

            // iso8583_f12_date_and_time_local_transaction        : 211102103311
            msg.set(_012_LOCAL_TRAN_DATETIME, "211102103311");

            // iso8583_f14_date_expiration                        : 2212
            msg.set(_014_EXPIRY_DATE, "2212");

            // iso8583_f22_pos_data_code                          : 51010151310C001
            msg.set(_022_POS_DATA_CODE, "51010151310C001");

            // iso8583_f23_card_sequence_number                   : 001
            msg.set(_023_CARD_SEQ_NR, "001");

            // iso8583_f24_function_code                          : 400
            msg.set(_024_FUNC_CODE, "400");

            // iso8583_f29_reconciliation_indicator               : 001
            msg.set(_029_RECON_INDICATOR, "001");

            // iso8583_f39_action_code                            : 990
            msg.set(_039_ACTION_CODE, "990");

            // iso8583_f41_terminal_id                            : 12344321
            msg.set(_041_TERMINAL_ID, "12344321");

            // iso8583_f42_card_acceptor_id                       : 102505780879851
            msg.set(_042_CARD_ACCEPTOR_ID, "102505780879851");

            // iso8583_f48_additional_data_private                : ­
            // F48 data
            // F0 dataset                                         : ­
            // F48 datalen                                        :
            // F48 bitmap                                         : Ç
            // termapp_f48_0_1_pos_data                           : 1234432100000500000
            AdditionalData addData = new AdditionalData();
            addData.put(PosData, "1234432100000500000");
            msg.putAdditionalData(addData);

            // iso8583_f49_currency_code_transaction              : 036
            msg.set(_049_TRAN_CURRENCY_CODE, "036");

            // iso8583_f56_original_data_elements                 : 12000000451101213254
            msg.set(_056_ORIG_DATA_ELEMENTS, "12000000451101213254");

            // iso8583_f64_message_authentication_code_field      : áHD│
            msg.set(_064_MAC, "A04844B300000000");

            byte[] actual = msg.toMsg();
            byte[] expected = {
                (byte)0x42, (byte)0x31, (byte)0x34, (byte)0x32, (byte)0x30, (byte)0x72, (byte)0x34, (byte)0x07, (byte)0x08, (byte)0x02, (byte)0xC1, (byte)0x81, (byte)0x01, (byte)0x31, (byte)0x36, (byte)0x34,
                (byte)0x37, (byte)0x36, (byte)0x31, (byte)0x37, (byte)0x33, (byte)0x39, (byte)0x30, (byte)0x30, (byte)0x31, (byte)0x30, (byte)0x31, (byte)0x30, (byte)0x31, (byte)0x31, (byte)0x39, (byte)0x30,
                (byte)0x30, (byte)0x32, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x31, (byte)0x32, (byte)0x33,
                (byte)0x34, (byte)0x31, (byte)0x31, (byte)0x30, (byte)0x31, (byte)0x32, (byte)0x31, (byte)0x33, (byte)0x32, (byte)0x35, (byte)0x34, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x34,
                (byte)0x36, (byte)0x32, (byte)0x31, (byte)0x31, (byte)0x31, (byte)0x30, (byte)0x32, (byte)0x31, (byte)0x30, (byte)0x33, (byte)0x33, (byte)0x31, (byte)0x31, (byte)0x32, (byte)0x32, (byte)0x31,
                (byte)0x32, (byte)0x35, (byte)0x31, (byte)0x30, (byte)0x31, (byte)0x30, (byte)0x31, (byte)0x35, (byte)0x31, (byte)0x33, (byte)0x31, (byte)0x30, (byte)0x43, (byte)0x30, (byte)0x30, (byte)0x31,
                (byte)0x30, (byte)0x30, (byte)0x31, (byte)0x34, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x31, (byte)0x39, (byte)0x39, (byte)0x30, (byte)0x31, (byte)0x32, (byte)0x33, (byte)0x34,
                (byte)0x34, (byte)0x33, (byte)0x32, (byte)0x31, (byte)0x31, (byte)0x30, (byte)0x32, (byte)0x35, (byte)0x30, (byte)0x35, (byte)0x37, (byte)0x38, (byte)0x30, (byte)0x38, (byte)0x37, (byte)0x39,
                (byte)0x38, (byte)0x35, (byte)0x31, (byte)0x30, (byte)0x30, (byte)0x32, (byte)0x34, (byte)0xF0, (byte)0x00, (byte)0x15, (byte)0x80, (byte)0x00, (byte)0x31, (byte)0x32, (byte)0x33, (byte)0x34,
                (byte)0x34, (byte)0x33, (byte)0x32, (byte)0x31, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x35, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x30,
                (byte)0x33, (byte)0x36, (byte)0x32, (byte)0x30, (byte)0x31, (byte)0x32, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x34, (byte)0x35, (byte)0x31, (byte)0x31,
                (byte)0x30, (byte)0x31, (byte)0x32, (byte)0x31, (byte)0x33, (byte)0x32, (byte)0x35, (byte)0x34, (byte)0xA0, (byte)0x48, (byte)0x44, (byte)0xB3, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
            };
            assertEquals(Util.hex2Str(expected), Util.hex2Str(actual));
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void test1430ReversalUnpack() {

        try {
            final byte[] inputData = {
                (byte)0x42, (byte)0x31, (byte)0x34, (byte)0x33, (byte)0x30, (byte)0x32, (byte)0x30, (byte)0x02, (byte)0x10, (byte)0x0A, (byte)0xC1, (byte)0x84, (byte)0x01, (byte)0x30, (byte)0x30, (byte)0x32,
                (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x31, (byte)0x32, (byte)0x33, (byte)0x34, (byte)0x31,
                (byte)0x31, (byte)0x30, (byte)0x31, (byte)0x32, (byte)0x31, (byte)0x33, (byte)0x32, (byte)0x35, (byte)0x34, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x34, (byte)0x36, (byte)0x32,
                (byte)0x31, (byte)0x31, (byte)0x31, (byte)0x30, (byte)0x32, (byte)0x31, (byte)0x30, (byte)0x33, (byte)0x33, (byte)0x31, (byte)0x31, (byte)0x30, (byte)0x30, (byte)0x31, (byte)0x31, (byte)0x37,
                (byte)0x30, (byte)0x32, (byte)0x32, (byte)0x37, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x31, (byte)0x38, (byte)0x31, (byte)0x32, (byte)0x36, (byte)0x30, (byte)0x32,
                (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x31, (byte)0x32, (byte)0x33, (byte)0x34, (byte)0x34, (byte)0x33, (byte)0x32, (byte)0x31, (byte)0x31, (byte)0x30, (byte)0x32, (byte)0x35, (byte)0x30,
                (byte)0x35, (byte)0x37, (byte)0x38, (byte)0x30, (byte)0x38, (byte)0x37, (byte)0x39, (byte)0x38, (byte)0x35, (byte)0x31, (byte)0x30, (byte)0x30, (byte)0x35, (byte)0x33, (byte)0xF0, (byte)0x00,
                (byte)0x32, (byte)0x02, (byte)0x00, (byte)0x54, (byte)0x65, (byte)0x72, (byte)0x6D, (byte)0x41, (byte)0x70, (byte)0x70, (byte)0x47, (byte)0x4D, (byte)0x20, (byte)0x20, (byte)0x20, (byte)0x53,
                (byte)0x69, (byte)0x6D, (byte)0x53, (byte)0x6E, (byte)0x6B, (byte)0x47, (byte)0x4D, (byte)0x20, (byte)0x20, (byte)0x20, (byte)0x20, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x31,
                (byte)0x38, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x31, (byte)0x38, (byte)0x44, (byte)0x65, (byte)0x66, (byte)0x61, (byte)0x75, (byte)0x6C, (byte)0x74, (byte)0x20, (byte)0x20,
                (byte)0x20, (byte)0x20, (byte)0x20, (byte)0x30, (byte)0x33, (byte)0x36, (byte)0x30, (byte)0x36, (byte)0x30, (byte)0x34, (byte)0x30, (byte)0x30, (byte)0x32, (byte)0x38, (byte)0x34, (byte)0x30,
                (byte)0x43, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x31, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x34, (byte)0x30, (byte)0x30,
                (byte)0x31, (byte)0x38, (byte)0x34, (byte)0x30, (byte)0x44, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x31, (byte)0x30, (byte)0x30, (byte)0x30,
                (byte)0x30, (byte)0x31, (byte)0x30, (byte)0x35, (byte)0x33, (byte)0x30, (byte)0x33, (byte)0x36, (byte)0x44, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x30,
                (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x31, (byte)0xFB, (byte)0x7B, (byte)0x0F, (byte)0x79, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00
            };
            As2805Eftex msg = new As2805Eftex(inputData);

            // MTI                                                : 1430
            assertEquals( _1430_TRAN_REV_ADV_RSP, msg.getMsgType() );

            // iso8583_f3_processing_code                         : 002000
            assertEquals( "002000", msg.get(_003_PROC_CODE));

            // iso8583_f4_amount_transaction                      : 000000001234
            assertEquals( "000000001234", msg.get(_004_TRAN_AMOUNT));

            // iso8583_f7_date_and_time_transmission              : 1101213254
            assertEquals( "1101213254", msg.get(_007_TRAN_DATE_TIME));

            // iso8583_f11_system_trace_audit_number              : 000046
            assertEquals( "000046", msg.get(_011_SYS_TRACE_AUDIT_NUM));

            // iso8583_f12_date_and_time_local_transaction        : 211102103311
            assertEquals( "211102103311", msg.get(_012_LOCAL_TRAN_DATETIME));

            // iso8583_f23_card_sequence_number                   : 001
            assertEquals( "001", msg.get(_023_CARD_SEQ_NR));

            // iso8583_f28_date_reconciliation                    : 170227
            assertEquals( "170227", msg.get(_028_RECON_DATE));

            // iso8583_f37_retrieval_reference_number             : 000001812602
            assertEquals( "000001812602", msg.get(_037_RET_REF_NR));

            // iso8583_f39_action_code                            : 000
            assertEquals( "000", msg.get(_039_ACTION_CODE));

            // iso8583_f41_terminal_id                            : 12344321
            assertEquals( "12344321", msg.get(_041_TERMINAL_ID));

            // iso8583_f42_card_acceptor_id                       : 102505780879851
            assertEquals( "102505780879851", msg.get(_042_CARD_ACCEPTOR_ID));

            AdditionalData addData = msg.getAdditionalData();
            assertNotNull(addData);

            // F0 dataset                                         : ­
            // F48 Bitmap                                         : ☻

            // termapp_f48_0_7_routing_information                : TermAppGM   SimSnkGM    000018000018Default
            assertEquals("TermAppGM   SimSnkGM    000018000018Default     ", addData.get(RoutingInfo));

            // iso8583_f48_additional_data_private                : ­

            // iso8583_f49_currency_code_transaction              : 036
            assertEquals( "036", msg.get(_049_TRAN_CURRENCY_CODE));

            // iso8583_f54_additional_amounts                     : 4002840C0000000100004001840D0000000100001053036D000000000001
            assertEquals( "4002840C0000000100004001840D0000000100001053036D000000000001", msg.get(_054_ADDITIONAL_AMOUNTS));

            // iso8583_f64_message_authentication_code_field      : ¹{☼y
            assertEquals( "FB7B0F7900000000", msg.get(_064_MAC));



        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testAmexStructuredDataPack() {
        try {
            HashtableMessage data = new HashtableMessage();
            data.put( "SELLER_ID", "123456789");
            data.put( "SELLER_EMAIL", "myemail@google.com");
            data.put( "SELLER_TELEPHONE", "61419403298");
            String sd = data.toMessageString();
            assertEquals( "19SELLER_ID19123456789212SELLER_EMAIL218myemail@google.com216SELLER_TELEPHONE21161419403298", sd );
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testAmexStructuredDataUnpack() {
        try {
            HashtableMessage data = new HashtableMessage();
            data.fromMessageString("19SELLER_ID19123456789212SELLER_EMAIL218myemail@google.com216SELLER_TELEPHONE21161419403298");

            assertEquals( "123456789", data.get( "SELLER_ID" ) );
            assertEquals( "myemail@google.com", data.get( "SELLER_EMAIL" ) );
            assertEquals( "61419403298", data.get( "SELLER_TELEPHONE" ) );

            int elementCount = 0;
            for (String key : data.keySet()) {
                switch( key ) {
                    case "SELLER_ID":
                        assertEquals( "123456789", data.get(key) );
                        elementCount++;
                        break;
                    case "SELLER_EMAIL":
                        assertEquals( "myemail@google.com", data.get(key) );
                        elementCount++;
                        break;
                    case "SELLER_TELEPHONE":
                        assertEquals( "61419403298", data.get(key) );
                        elementCount++;
                        break;
                    default:
                        // deliberate fail
                        fail();
                        break;
                }
            }

            assertEquals( 3, elementCount );

        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testDateTimeDifference() {
        // under 1 day

        // exceptions
        assertFalse( Util.hasTimeElapsed( null, "2021-11-10 16:21:23", TimeUnit.DAYS.toMillis(1) ) );
        assertFalse( Util.hasTimeElapsed( "", "2021-11-10 16:21:23", TimeUnit.DAYS.toMillis(1) ) );
        assertFalse( Util.hasTimeElapsed( "bollocks", "2021-11-10 16:21:23", TimeUnit.DAYS.toMillis(1) ) );

        // 1 day tests
        assertFalse( Util.hasTimeElapsed( "2021-11-11 16:21:23", "2021-11-10 16:21:23", TimeUnit.DAYS.toMillis(1) ) );
        assertFalse( Util.hasTimeElapsed( "2021-11-11 16:21:23", "2021-11-12 16:21:22", TimeUnit.DAYS.toMillis(1) ) );
        assertTrue( Util.hasTimeElapsed( "2021-11-11 16:21:23", "2021-11-12 16:21:23", TimeUnit.DAYS.toMillis(1) ) );
        assertTrue( Util.hasTimeElapsed( "2021-11-11 16:21:23", "2022-11-12 16:21:22", TimeUnit.DAYS.toMillis(1) ) );

        // 1 hr tests
        assertFalse( Util.hasTimeElapsed( "2021-11-11 16:21:23", "2021-11-10 16:21:23", TimeUnit.HOURS.toMillis(1) ) );
        assertFalse( Util.hasTimeElapsed( "2021-11-11 16:21:23", "2021-11-11 17:21:22", TimeUnit.HOURS.toMillis(1) ) );
        assertTrue( Util.hasTimeElapsed( "2021-11-11 16:21:23", "2021-11-11 17:21:23", TimeUnit.HOURS.toMillis(1) ) );
        assertTrue( Util.hasTimeElapsed( "2021-11-11 16:21:23", "2022-11-11 18:21:22", TimeUnit.HOURS.toMillis(1) ) );
        assertFalse( Util.hasTimeElapsed( "2021-11-30 23:21:23", "2021-12-01 00:00:22", TimeUnit.HOURS.toMillis(1) ) );
        assertTrue( Util.hasTimeElapsed( "2021-11-30 23:21:23", "2021-12-01 00:22:22", TimeUnit.HOURS.toMillis(1) ) );

    }

    @Test
    public void testRrnGeneration() {
        String result;

        result = As2805EftexUtils.generateRrn("47000002", 1, 2);
        assertEquals( "03BCqY001002", result );

        // max numeric TID
        result = As2805EftexUtils.generateRrn("99999999", 123, 456);
        assertEquals( "06laZD123456", result );

        // same as above but with alpha char in TID. alphas should be ignored. base62 of 4700002 instead
        result = As2805EftexUtils.generateRrn("47P00002", 123, 456);
        assertEquals( "00JigU123456", result );

        // similar but with more alphas. digits are 0012, converts to C
        result = As2805EftexUtils.generateRrn("TQP001A2", 123, 456);
        assertEquals( "00000C123456", result );

        // empty terminal id
        result = As2805EftexUtils.generateRrn("", 123, 456);
        assertEquals( "000000123456", result );

        // TID is all alphas
        result = As2805EftexUtils.generateRrn("AAAABCDE", 123, 456);
        assertEquals( "000000123456", result );
    }

}

