package eftexAs2805Instrumented;

import static com.linkly.libengine.engine.protocol.iso8583.As2805EftexPack.UnPackResult.UNPACK_OK;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583Rev93.Bit._003_PROC_CODE;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583Rev93.Bit._004_TRAN_AMOUNT;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583Rev93.Bit._007_TRAN_DATE_TIME;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583Rev93.Bit._011_SYS_TRACE_AUDIT_NUM;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583Rev93.Bit._012_LOCAL_TRAN_DATETIME;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583Rev93.Bit._022_POS_DATA_CODE;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583Rev93.Bit._023_CARD_SEQ_NR;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583Rev93.Bit._024_FUNC_CODE;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583Rev93.Bit._029_RECON_INDICATOR;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583Rev93.Bit._035_TRACK_2_DATA;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583Rev93.Bit._041_TERMINAL_ID;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583Rev93.Bit._042_CARD_ACCEPTOR_ID;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583Rev93.Bit._049_TRAN_CURRENCY_CODE;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583Rev93.Bit._052_PIN_DATA;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583Rev93.Bit._064_MAC;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.Iso8583Rev93.MsgType._1200_TRAN_REQ;
import static com.linkly.libengine.engine.protocol.iso8583.openisoj.termappiso.AdditionalData.Field.PosData;
import static com.linkly.libsecapp.IP2PEncrypt.EncryptAlgorithm.AS2805_3DES_OFB;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.content.Intent;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.platform.app.InstrumentationRegistry;

import com.linkly.libengine.config.Config;
import com.linkly.libengine.dependencies.Dependencies;
import com.linkly.libengine.engine.protocol.Protocol;
import com.linkly.libengine.engine.protocol.iso8583.As2805EftexPack;
import com.linkly.libengine.engine.protocol.iso8583.openisoj.As2805Eftex;
import com.linkly.libengine.engine.protocol.iso8583.openisoj.termappiso.AdditionalData;
import com.linkly.libengine.engine.transactions.TransRec;
import com.linkly.libmal.global.util.Util;
import com.linkly.libsecapp.CardholderDataElement;
import com.linkly.libsecapp.EncryptResult;
import com.linkly.libsecapp.IP2PEncrypt;
import com.linkly.libsecapp.IP2PSec;
import com.linkly.libsecapp.P2PLib;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Arrays;

import timber.log.Timber;

@Ignore("Ignoring as integration")
@RunWith(AndroidJUnit4.class)
@LargeTest
public class eftexAs2805Test {
    private Context context;
    private IP2PEncrypt p2pEncrypt = null;
    private IP2PSec p2pSec = null;
    private static final String TEST_ENDPOINT_IP = "192.168.1.34";
    private static final int TEST_ENDPOINT_PORT = 201;
    private static final byte[] AIIC_BLOCK = new byte[] { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01, 0x23, 0x45, 0x67, (byte)0x89, 0x01 };
    private static P2PLib p2pInstance = P2PLib.getInstance();

    private static final byte[] skTcuMod = {
            (byte)0xBB, (byte)0x9D, (byte)0x54, (byte)0xC6, (byte)0x7C, (byte)0xBB, (byte)0x36, (byte)0xA9, (byte)0x43, (byte)0x07, (byte)0xE7, (byte)0x0B, (byte)0x00, (byte)0xA1, (byte)0xF1, (byte)0xFB,
            (byte)0xD6, (byte)0x67, (byte)0xD6, (byte)0x59, (byte)0xC4, (byte)0x39, (byte)0x5F, (byte)0x84, (byte)0x1C, (byte)0x58, (byte)0x95, (byte)0xB5, (byte)0x37, (byte)0x19, (byte)0xEF, (byte)0xF5,
            (byte)0x0B, (byte)0x38, (byte)0x14, (byte)0x11, (byte)0x44, (byte)0x92, (byte)0x59, (byte)0x70, (byte)0x04, (byte)0xF0, (byte)0x42, (byte)0x95, (byte)0x6E, (byte)0xD1, (byte)0x16, (byte)0x52,
            (byte)0xD8, (byte)0x32, (byte)0x69, (byte)0xAB, (byte)0x6C, (byte)0x32, (byte)0x84, (byte)0x1C, (byte)0xCF, (byte)0x62, (byte)0x75, (byte)0x8F, (byte)0x24, (byte)0x18, (byte)0x54, (byte)0x4F,
            (byte)0x94, (byte)0x16, (byte)0x6E, (byte)0x8D, (byte)0x11, (byte)0xA2, (byte)0x2F, (byte)0x39, (byte)0xB9, (byte)0xC0, (byte)0x63, (byte)0x6D, (byte)0xCA, (byte)0x21, (byte)0x43, (byte)0xAD,
            (byte)0x37, (byte)0x16, (byte)0xD7, (byte)0x78, (byte)0x4E, (byte)0x11, (byte)0x44, (byte)0x02, (byte)0x57, (byte)0x13, (byte)0x4D, (byte)0x5F, (byte)0x5B, (byte)0xB5, (byte)0x6B, (byte)0x72,
            (byte)0x0E, (byte)0xCF, (byte)0xD6, (byte)0x83, (byte)0x3C, (byte)0x5B, (byte)0x2E, (byte)0x38, (byte)0x65, (byte)0xEB, (byte)0x0E, (byte)0x2C, (byte)0xE4, (byte)0x4D, (byte)0x60, (byte)0x62,
            (byte)0x53, (byte)0x70, (byte)0x4B, (byte)0x26, (byte)0x05, (byte)0x57, (byte)0x56, (byte)0x17, (byte)0x06, (byte)0x8A, (byte)0x0A, (byte)0xAD, (byte)0x59, (byte)0x39, (byte)0xC3, (byte)0xAB,
            (byte)0x6B, (byte)0x3F, (byte)0x9D, (byte)0x9B, (byte)0x23, (byte)0x5E, (byte)0x86, (byte)0x3C, (byte)0xD5, (byte)0xE9, (byte)0xB7, (byte)0x2C, (byte)0x12, (byte)0x46, (byte)0x50, (byte)0x3E,
            (byte)0xCA, (byte)0x62, (byte)0xE1, (byte)0x20, (byte)0xCF, (byte)0x0E, (byte)0x2C, (byte)0x02, (byte)0x78, (byte)0x3B, (byte)0x4A, (byte)0xC3, (byte)0xD0, (byte)0x0D, (byte)0xF9, (byte)0xE6,
            (byte)0x3A, (byte)0x65, (byte)0xC1, (byte)0xAD, (byte)0xB6, (byte)0x56, (byte)0x30, (byte)0xAA, (byte)0xBC, (byte)0x02, (byte)0x58, (byte)0x87, (byte)0x4E, (byte)0x94, (byte)0x25, (byte)0x5E,
            (byte)0xF7, (byte)0xB1, (byte)0xBA, (byte)0x4E, (byte)0x08, (byte)0xC8, (byte)0x2F, (byte)0x9D, (byte)0xDD, (byte)0xF5, (byte)0xE3, (byte)0xE1, (byte)0xFC, (byte)0x43, (byte)0xDC, (byte)0xFD,
            (byte)0x19, (byte)0x9C, (byte)0x7F, (byte)0xF9, (byte)0x7B, (byte)0xC8, (byte)0xA2, (byte)0xFE, (byte)0x00, (byte)0xA3, (byte)0xFD, (byte)0x14, (byte)0xF8, (byte)0x38, (byte)0x66, (byte)0xA6,
            (byte)0x89, (byte)0x32, (byte)0x13, (byte)0x6E, (byte)0x64, (byte)0x92, (byte)0xD6, (byte)0xAB, (byte)0xB9, (byte)0x31, (byte)0x17, (byte)0x19, (byte)0x28, (byte)0x14, (byte)0xBD, (byte)0xFB,
            (byte)0x3D, (byte)0xD8, (byte)0xE9, (byte)0x50, (byte)0xCB, (byte)0x83, (byte)0x88, (byte)0xC9, (byte)0x57, (byte)0xC4, (byte)0x78, (byte)0xB3, (byte)0x1D, (byte)0x57, (byte)0x17, (byte)0xB0,
            (byte)0x9D, (byte)0xA7, (byte)0xED, (byte)0xB3, (byte)0x2D, (byte)0x64, (byte)0x69, (byte)0x1F
    };

    private static final byte[] skTcuExp = {
            (byte)0x30, (byte)0x86, (byte)0xAE, (byte)0x21, (byte)0xB4, (byte)0xAB, (byte)0x5B, (byte)0xA5, (byte)0x06, (byte)0x00, (byte)0x6A, (byte)0x43, (byte)0x0F, (byte)0x43, (byte)0xD3, (byte)0xEA,
            (byte)0x16, (byte)0x77, (byte)0xBF, (byte)0x1A, (byte)0x72, (byte)0xC5, (byte)0x80, (byte)0x2E, (byte)0x14, (byte)0x82, (byte)0xD9, (byte)0x1E, (byte)0x09, (byte)0x05, (byte)0x7D, (byte)0x6A,
            (byte)0x3D, (byte)0x1F, (byte)0xA1, (byte)0x19, (byte)0x22, (byte)0x4A, (byte)0x54, (byte)0x35, (byte)0xC4, (byte)0x21, (byte)0x3C, (byte)0xA7, (byte)0x98, (byte)0x05, (byte)0x1E, (byte)0xA8,
            (byte)0x08, (byte)0xE1, (byte)0x69, (byte)0x31, (byte)0x7C, (byte)0x87, (byte)0xC8, (byte)0x62, (byte)0x07, (byte)0x45, (byte)0xE9, (byte)0x30, (byte)0x3F, (byte)0xDE, (byte)0xC7, (byte)0x17,
            (byte)0xDA, (byte)0x2E, (byte)0xBF, (byte)0x23, (byte)0x5F, (byte)0xB7, (byte)0x3A, (byte)0x4D, (byte)0xCC, (byte)0xFC, (byte)0xB4, (byte)0xFA, (byte)0xF5, (byte)0xB8, (byte)0x0A, (byte)0xE0,
            (byte)0x27, (byte)0x8A, (byte)0x91, (byte)0xEA, (byte)0xBD, (byte)0x0E, (byte)0x7C, (byte)0x2F, (byte)0xBA, (byte)0xC3, (byte)0xF4, (byte)0x54, (byte)0x40, (byte)0xCD, (byte)0x38, (byte)0xE0,
            (byte)0x44, (byte)0x72, (byte)0x7F, (byte)0x82, (byte)0x6C, (byte)0x3F, (byte)0x0D, (byte)0x42, (byte)0x6B, (byte)0x22, (byte)0x14, (byte)0x05, (byte)0x8A, (byte)0x7B, (byte)0x74, (byte)0xFD,
            (byte)0x99, (byte)0xA5, (byte)0x46, (byte)0xDA, (byte)0xAB, (byte)0xB0, (byte)0xFE, (byte)0x44, (byte)0x70, (byte)0x5D, (byte)0x8E, (byte)0x0A, (byte)0xFC, (byte)0x2B, (byte)0xD6, (byte)0x69,
            (byte)0xFA, (byte)0xB9, (byte)0x1D, (byte)0xA3, (byte)0x6F, (byte)0x93, (byte)0xCC, (byte)0xDD, (byte)0x03, (byte)0xDC, (byte)0x50, (byte)0xDA, (byte)0x8F, (byte)0x6C, (byte)0xA5, (byte)0x7E,
            (byte)0xAD, (byte)0x85, (byte)0x38, (byte)0xB3, (byte)0xA2, (byte)0x2C, (byte)0x4D, (byte)0xF7, (byte)0xC3, (byte)0xDE, (byte)0x7B, (byte)0xB7, (byte)0x5E, (byte)0x46, (byte)0x98, (byte)0xB5,
            (byte)0x71, (byte)0x53, (byte)0x2D, (byte)0x07, (byte)0xDB, (byte)0xF9, (byte)0x2A, (byte)0xD1, (byte)0x1A, (byte)0x4E, (byte)0xE2, (byte)0xC8, (byte)0xFB, (byte)0x6E, (byte)0x0E, (byte)0x1E,
            (byte)0x65, (byte)0xD3, (byte)0x37, (byte)0x8D, (byte)0xE5, (byte)0xEF, (byte)0x2C, (byte)0x31, (byte)0xC5, (byte)0x47, (byte)0x66, (byte)0xBF, (byte)0xD1, (byte)0xD2, (byte)0xD6, (byte)0x8D,
            (byte)0xDC, (byte)0xE0, (byte)0x1B, (byte)0x0F, (byte)0x6A, (byte)0xF2, (byte)0x6B, (byte)0xD0, (byte)0x94, (byte)0x71, (byte)0x1F, (byte)0xC6, (byte)0x2C, (byte)0x8F, (byte)0x3B, (byte)0xF4,
            (byte)0x7E, (byte)0xF5, (byte)0xAB, (byte)0x87, (byte)0x18, (byte)0xA4, (byte)0xB5, (byte)0xDF, (byte)0xBC, (byte)0x32, (byte)0x21, (byte)0x7F, (byte)0xBF, (byte)0xF1, (byte)0xB5, (byte)0x7E,
            (byte)0x91, (byte)0x09, (byte)0xC9, (byte)0xF1, (byte)0x14, (byte)0xEC, (byte)0xC8, (byte)0x42, (byte)0x98, (byte)0x24, (byte)0x1A, (byte)0xFB, (byte)0xB3, (byte)0x81, (byte)0xD3, (byte)0xBC,
            (byte)0xEF, (byte)0x37, (byte)0xF7, (byte)0x30, (byte)0x93, (byte)0xA2, (byte)0x37, (byte)0xF9
    };

    private static final byte[] skManPkTcuMod = {
            (byte)0x50, (byte)0xFE, (byte)0x14, (byte)0xE8, (byte)0xEB, (byte)0x7A, (byte)0x74, (byte)0x93, (byte)0xA9, (byte)0x78, (byte)0x2D, (byte)0x99, (byte)0xC8, (byte)0x3B, (byte)0x52, (byte)0x81,
            (byte)0x8A, (byte)0xD6, (byte)0x3E, (byte)0xF7, (byte)0x99, (byte)0x30, (byte)0x10, (byte)0x0C, (byte)0xA6, (byte)0x72, (byte)0x64, (byte)0x69, (byte)0xF6, (byte)0x64, (byte)0x52, (byte)0x2F,
            (byte)0xE8, (byte)0x4D, (byte)0x0A, (byte)0x86, (byte)0x1E, (byte)0xD0, (byte)0x0D, (byte)0xBD, (byte)0x66, (byte)0xD1, (byte)0x5F, (byte)0xE4, (byte)0x88, (byte)0x46, (byte)0x77, (byte)0x3D,
            (byte)0xB0, (byte)0x6A, (byte)0x95, (byte)0x13, (byte)0xF7, (byte)0x0C, (byte)0x88, (byte)0x9E, (byte)0xA6, (byte)0xEF, (byte)0xE4, (byte)0x87, (byte)0x15, (byte)0x49, (byte)0xBB, (byte)0x46,
            (byte)0x50, (byte)0xD9, (byte)0xAC, (byte)0xCF, (byte)0xBC, (byte)0x21, (byte)0x4C, (byte)0xAE, (byte)0x46, (byte)0xED, (byte)0x2D, (byte)0x7E, (byte)0x41, (byte)0x43, (byte)0x4C, (byte)0xD1,
            (byte)0x32, (byte)0x0C, (byte)0xF2, (byte)0xA0, (byte)0x25, (byte)0x0B, (byte)0x26, (byte)0x70, (byte)0x8B, (byte)0x15, (byte)0x60, (byte)0x95, (byte)0xD1, (byte)0xAB, (byte)0xF3, (byte)0x2D,
            (byte)0x1A, (byte)0xB8, (byte)0x2B, (byte)0x47, (byte)0x4E, (byte)0x0A, (byte)0x36, (byte)0x26, (byte)0xCC, (byte)0x31, (byte)0x6F, (byte)0xDA, (byte)0x23, (byte)0x2B, (byte)0xAF, (byte)0x03,
            (byte)0xEF, (byte)0xD3, (byte)0xE9, (byte)0xF5, (byte)0xFF, (byte)0x3F, (byte)0x68, (byte)0xB1, (byte)0x17, (byte)0x16, (byte)0xB9, (byte)0xF5, (byte)0xD9, (byte)0x63, (byte)0x57, (byte)0x6D,
            (byte)0xE7, (byte)0xFE, (byte)0xCC, (byte)0xD7, (byte)0x42, (byte)0x48, (byte)0x83, (byte)0x26, (byte)0xBD, (byte)0xDA, (byte)0x8E, (byte)0x24, (byte)0x53, (byte)0x24, (byte)0xF5, (byte)0x7A,
            (byte)0x2D, (byte)0xA3, (byte)0x10, (byte)0x4A, (byte)0x8C, (byte)0xC6, (byte)0x42, (byte)0x27, (byte)0xF7, (byte)0xAA, (byte)0x4B, (byte)0x15, (byte)0xB1, (byte)0xFD, (byte)0x2E, (byte)0x6B,
            (byte)0xAC, (byte)0x0E, (byte)0x60, (byte)0x7E, (byte)0xC4, (byte)0x3B, (byte)0xD5, (byte)0xBF, (byte)0x09, (byte)0x16, (byte)0x71, (byte)0x33, (byte)0x89, (byte)0xC2, (byte)0xE2, (byte)0xE9,
            (byte)0x0A, (byte)0x31, (byte)0x3F, (byte)0x6B, (byte)0xDA, (byte)0x11, (byte)0xCA, (byte)0xCE, (byte)0x88, (byte)0x8A, (byte)0x70, (byte)0x75, (byte)0x70, (byte)0x9E, (byte)0x6C, (byte)0x02,
            (byte)0x9B, (byte)0x65, (byte)0x54, (byte)0x7C, (byte)0xB0, (byte)0x72, (byte)0x3A, (byte)0x79, (byte)0x20, (byte)0xC6, (byte)0x30, (byte)0xA4, (byte)0xE3, (byte)0xD8, (byte)0xEA, (byte)0xC1,
            (byte)0x82, (byte)0xBA, (byte)0xF8, (byte)0x90, (byte)0x1F, (byte)0x34, (byte)0xE6, (byte)0xB3, (byte)0xF3, (byte)0x3D, (byte)0x24, (byte)0x7F, (byte)0xB0, (byte)0xE0, (byte)0x9C, (byte)0xE0,
            (byte)0x3B, (byte)0x3C, (byte)0x31, (byte)0xA2, (byte)0xBE, (byte)0x6E, (byte)0xD0, (byte)0xE6, (byte)0x1F, (byte)0xF1, (byte)0x6D, (byte)0x8D, (byte)0x76, (byte)0x36, (byte)0xA9, (byte)0x57,
            (byte)0xC2, (byte)0xE4, (byte)0x5C, (byte)0x9A, (byte)0x54, (byte)0x9B, (byte)0xEE, (byte)0x70, (byte)0x2F, (byte)0x7B, (byte)0xB5, (byte)0xC2, (byte)0x70, (byte)0x60, (byte)0xB3, (byte)0x8E,
    };

    private static final byte[] skManPkTcuExp = {
            (byte)0x3F, (byte)0x89, (byte)0xAA, (byte)0x06, (byte)0xA0, (byte)0x66, (byte)0xB1, (byte)0x6D, (byte)0x9E, (byte)0x01, (byte)0xEE, (byte)0x3D, (byte)0x8D, (byte)0xBA, (byte)0xC2, (byte)0xB7,
            (byte)0x97, (byte)0x6D, (byte)0x24, (byte)0x69, (byte)0x83, (byte)0x5C, (byte)0x18, (byte)0x54, (byte)0xE0, (byte)0x62, (byte)0x23, (byte)0x4D, (byte)0xD1, (byte)0x12, (byte)0xD6, (byte)0xD6,
            (byte)0xE7, (byte)0x66, (byte)0xC4, (byte)0x92, (byte)0xDE, (byte)0x94, (byte)0x2A, (byte)0x77, (byte)0xDF, (byte)0x22, (byte)0x45, (byte)0x99, (byte)0x86, (byte)0xF2, (byte)0x72, (byte)0x7E,
            (byte)0xE9, (byte)0x88, (byte)0x69, (byte)0x20, (byte)0xBC, (byte)0xBA, (byte)0x5C, (byte)0xF0, (byte)0x09, (byte)0x3F, (byte)0xAD, (byte)0xBE, (byte)0x09, (byte)0x43, (byte)0x5E, (byte)0x37,
            (byte)0xEA, (byte)0x7A, (byte)0x8B, (byte)0x16, (byte)0x1D, (byte)0x0F, (byte)0xB1, (byte)0x31, (byte)0xEA, (byte)0xA7, (byte)0x40, (byte)0x94, (byte)0x1E, (byte)0x73, (byte)0x30, (byte)0x80,
            (byte)0x0A, (byte)0x4D, (byte)0x5B, (byte)0x0D, (byte)0xF1, (byte)0x9C, (byte)0x62, (byte)0x28, (byte)0x2D, (byte)0x23, (byte)0x40, (byte)0x24, (byte)0xD9, (byte)0x24, (byte)0x2D, (byte)0x58,
            (byte)0x41, (byte)0x1C, (byte)0x3B, (byte)0x67, (byte)0xD2, (byte)0xC8, (byte)0xA7, (byte)0x8E, (byte)0xF3, (byte)0x9F, (byte)0x0F, (byte)0xE9, (byte)0xC7, (byte)0x0D, (byte)0xDD, (byte)0x6A,
            (byte)0x49, (byte)0xB8, (byte)0x72, (byte)0xD4, (byte)0x97, (byte)0xBD, (byte)0x20, (byte)0x38, (byte)0x0C, (byte)0x5A, (byte)0x79, (byte)0x3F, (byte)0x5C, (byte)0xCA, (byte)0xB2, (byte)0x74,
            (byte)0x1E, (byte)0xEC, (byte)0x3C, (byte)0x27, (byte)0x3D, (byte)0xBA, (byte)0xEF, (byte)0x69, (byte)0xFA, (byte)0x03, (byte)0x3C, (byte)0x0F, (byte)0x3D, (byte)0x06, (byte)0x41, (byte)0x95,
            (byte)0x60, (byte)0xE0, (byte)0xF1, (byte)0x80, (byte)0x7B, (byte)0x81, (byte)0xCC, (byte)0xCE, (byte)0xB2, (byte)0x0C, (byte)0x6D, (byte)0x32, (byte)0x12, (byte)0x54, (byte)0x78, (byte)0x78,
            (byte)0x28, (byte)0xF5, (byte)0xF2, (byte)0x50, (byte)0x3C, (byte)0xB0, (byte)0xF3, (byte)0xEC, (byte)0x92, (byte)0x54, (byte)0xCB, (byte)0x4B, (byte)0xBE, (byte)0xED, (byte)0xD4, (byte)0xD4,
            (byte)0x25, (byte)0x07, (byte)0x07, (byte)0x6F, (byte)0x6E, (byte)0xAF, (byte)0x58, (byte)0x18, (byte)0x0A, (byte)0x5F, (byte)0x24, (byte)0xEC, (byte)0x2D, (byte)0x30, (byte)0x64, (byte)0x66,
            (byte)0x5D, (byte)0x04, (byte)0x41, (byte)0xA4, (byte)0xE4, (byte)0x6A, (byte)0x2A, (byte)0xC7, (byte)0xB3, (byte)0x69, (byte)0xCD, (byte)0xD1, (byte)0xA4, (byte)0x83, (byte)0xD4, (byte)0xC1,
            (byte)0xDB, (byte)0x94, (byte)0x56, (byte)0xC3, (byte)0xF6, (byte)0x14, (byte)0xB1, (byte)0x0A, (byte)0x86, (byte)0xAE, (byte)0xB4, (byte)0xE5, (byte)0xAC, (byte)0xEC, (byte)0x73, (byte)0xE8,
            (byte)0x5D, (byte)0x7B, (byte)0x8C, (byte)0x13, (byte)0x95, (byte)0xEF, (byte)0x2E, (byte)0x2F, (byte)0xD1, (byte)0x1E, (byte)0x4F, (byte)0x01, (byte)0xA4, (byte)0xD5, (byte)0xC0, (byte)0x2A,
            (byte)0x6C, (byte)0xF6, (byte)0xD5, (byte)0xE0, (byte)0x36, (byte)0x26, (byte)0xAD, (byte)0x31, (byte)0x39, (byte)0xEB, (byte)0xBD, (byte)0x54, (byte)0xB8, (byte)0x51, (byte)0x94, (byte)0x26,
    };

    private static final byte[] ppid = {
            (byte)0x01, (byte)0x11, (byte)0x17, (byte)0x00, (byte)0x90, (byte)0x65, (byte)0x45, (byte)0x93
    };

    private void setupP2pe() {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        P2PLib p2pInstance = P2PLib.getInstance();
        assertNotEquals(p2pInstance, null);
        p2pInstance.Init( context, null );
        p2pEncrypt = p2pInstance.getIP2PEncrypt();
        p2pEncrypt.initialise(true);
        p2pSec = p2pInstance.getIP2PSec();

    }


/*
    execute first because it loads keys for all other tests
*/


    @Test
    public void aaa_testKeyInject() {
        setupP2pe();

        //Send the key data to SecApp Via intent
        Intent intent = new Intent();
        intent.setAction("com.linkly.APP_SEND_KEY_INJECTION");
        intent.putExtra("INJECTION_TYPE", "TEST");
        intent.putExtra("SKTCU_MOD", Util.byteArrayToHexString(skTcuMod) );
        intent.putExtra("SKTCU_EXP", Util.byteArrayToHexString(skTcuExp));
        intent.putExtra("SK_MAN_PK_TCU_MOD", Util.byteArrayToHexString(skManPkTcuMod));
        intent.putExtra("SK_MAN_PK_TCU_EXP", Util.byteArrayToHexString(skManPkTcuExp));
        intent.putExtra("PPID", Util.byteArrayToHexString(ppid));
        context.sendBroadcast(intent);

        // wait a little bit for key injection to complete
        Util.Sleep(500);

        // test PPID is injected
        assertArrayEquals( ppid, p2pSec.as2805GetPpid() );

        byte[] expectedBuffer = new byte[skManPkTcuExp.length + skManPkTcuMod.length];
        System.arraycopy(skManPkTcuMod, 0, expectedBuffer, 0, skManPkTcuMod.length);
        System.arraycopy(skManPkTcuExp, 0, expectedBuffer, skManPkTcuMod.length, skManPkTcuExp.length);

        // test skManPkTcu parts are injected
        assertArrayEquals( expectedBuffer, p2pSec.as2805GetSkManPkTcu());
    }

    public byte[] generate1804RSARequest( String type, byte[] payload ) {

        byte[] template = {
                (byte)0x42, (byte)0x31, (byte)0x38, (byte)0x30, (byte)0x34, (byte)0x82, (byte)0x30, (byte)0x01, (byte)0x00, (byte)0x00, (byte)0xC0, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
                (byte)0x01, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x31, (byte)0x30, (byte)0x31, (byte)0x34, (byte)0x30, (byte)0x38, (byte)0x35, (byte)0x31, (byte)0x34, (byte)0x33, (byte)0x30,
                (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x32, (byte)0x32, (byte)0x32, (byte)0x31, (byte)0x31, (byte)0x30, (byte)0x31, (byte)0x34, (byte)0x32, (byte)0x31, (byte)0x35, (byte)0x31, (byte)0x34,
                (byte)0x33, (byte)0x38, (byte)0x39, (byte)0x37, (byte)0x31, (byte)0x32, (byte)0x33, (byte)0x34, (byte)0x34, (byte)0x33, (byte)0x32, (byte)0x31, (byte)0x31, (byte)0x30, (byte)0x32, (byte)0x35,
                (byte)0x30, (byte)0x35, (byte)0x37, (byte)0x38, (byte)0x30, (byte)0x38, (byte)0x37, (byte)0x39, (byte)0x38, (byte)0x35, (byte)0x31,
        };

        String payloadLen = String.format( "%03d", payload.length );

        // substitute de 24 in offset 0x31
        System.arraycopy( type.getBytes(), 0, template, 0x31, 3 );

        byte[] output = new byte[3 + template.length + payload.length];

        System.arraycopy( template, 0, output, 0, template.length );
        System.arraycopy( payloadLen.getBytes(), 0, output, template.length, 3 );
        System.arraycopy( payload, 0, output, template.length+3, payload.length );

        return output;
    }

    @Test
    public void testRsaKeyInitPart1() {
        setupP2pe();

        // generate first part of rsa key init
        // de96 payload is as follows:
        // fixed 8 bytes PPID
        // LLLLvar skMan mod and exp, lots of bytes
        // LLLLvar pktcu exponent

        byte[] skManPkTcu = p2pSec.as2805GetSkManPkTcu();
        assertNotNull(skManPkTcu);

        byte[] skManPkTcuLen = new byte[2];
        skManPkTcuLen[0] = (byte)(skManPkTcu.length / 256);
        skManPkTcuLen[1] = (byte)(skManPkTcu.length % 256);

        byte[] pkTcuExpWithLength = new byte[] { 0x00, 0x03, 0x01, 0x00, 0x01 };

        byte[] ppid = p2pSec.as2805GetPpid();

        int lengthOfPayload = ppid.length + skManPkTcuLen.length + skManPkTcu.length + pkTcuExpWithLength.length;
        byte[] payload = new byte[lengthOfPayload];

        System.arraycopy( ppid, 0, payload, 0, ppid.length );
        System.arraycopy( skManPkTcuLen, 0, payload, ppid.length, skManPkTcuLen.length );
        System.arraycopy( skManPkTcu, 0, payload, ppid.length+skManPkTcuLen.length, skManPkTcu.length );
        System.arraycopy( pkTcuExpWithLength, 0, payload, ppid.length+skManPkTcuLen.length+skManPkTcu.length, pkTcuExpWithLength.length );

        byte[] request = generate1804RSARequest( "897", payload );

        assertNotNull( request );

        // send to host
        byte[] response = sendReceive( TEST_ENDPOINT_IP, TEST_ENDPOINT_PORT, request );
        assertNotNull( response );

        byte[] respPayload = extractPayload( response );
        assertNotNull( respPayload );

        // response payload format
        // PKsp modulus || PKsp exponent, each is variable and LLLLvar
        // RNsp 8 bytes

        assertEquals(255, respPayload.length);

        byte[] pkSpMod = new byte[240];
        byte[] pkSpExp = new byte[3];
        byte[] rnSp = new byte[8];

        System.arraycopy(respPayload, 2, pkSpMod, 0, 240 );
        System.arraycopy(respPayload, 244, pkSpExp, 0, 3 );
        System.arraycopy(respPayload, 247, rnSp, 0, 8 );

        // check received fields
        byte[] pkSpModExpected = new byte[] {
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
        };
        byte[] pkSpExpExpected = new byte[] { 0x01, 0x00, 0x01 };

        assertArrayEquals( pkSpModExpected, pkSpMod );
        assertArrayEquals( pkSpExpExpected, pkSpExp );
        assertArrayEquals( new byte[] { 0x01, 0x23, 0x45, 0x67, (byte)0x89, (byte)0xab, (byte)0xcd, (byte)0xef}, rnSp );

        // inject sponsor key
        p2pSec.as2805InjectPkSponsor(pkSpMod, pkSpExp);

    }


    @Test
    public void testRsaKeyInitPart2() {
        setupP2pe();

        // this is the hard-coded RN that the emulator returns
        byte[] rnSp = new byte[] { 0x01, 0x23, 0x45, 0x67, (byte)0x89, (byte)0xab, (byte)0xcd, (byte)0xef};

        // generate part 2 payload
        // 8 byte tcuid
        // LLLLvar sSKtcu(Pksp(KI, TCUID, DTS, RNsp))

        byte[] skTcuEncBlock = p2pSec.as2805GenerateSkTcuKiBlock(rnSp);
        byte[] ppid = p2pSec.as2805GetPpid();

        // assert lengths are as expected
        assertEquals( 248, skTcuEncBlock.length );

        ByteArrayOutputStream payload = new ByteArrayOutputStream();

        try {
            // ppid
            payload.write(ppid);

            // length of skTcu block
            byte[] skTcuBlockLen = new byte[] { 0x02, 0x48 };

            payload.write(skTcuBlockLen);

            // skTcu(DFormat1(pkSp(DFormat1(KI, PPID, DTS, RN))))
            payload.write(skTcuEncBlock);

        } catch( Exception e ) {
            assertTrue( false );
        }

        // send/receive
        byte[] request = generate1804RSARequest( "898", payload.toByteArray() );

        assertNotNull( request );

        // send to host
        byte[] response = sendReceive( TEST_ENDPOINT_IP, TEST_ENDPOINT_PORT, request );
        assertNotNull( response );

        byte[] respPayload = extractPayload( response );
        assertNotNull( respPayload );

        // response payload is:
        // eKI(KCA) b16
        // AIIC n..11, LLVAR
        byte[] eKIv44Kca = Arrays.copyOfRange(respPayload, 0, 16);
        byte[] eKIv24Kmach = new byte[16];

        // load KCA
        assertTrue( p2pSec.as2805LoadKcaKmach(eKIv44Kca, eKIv24Kmach, AIIC_BLOCK) );
    }

    @Test
    public void testRsaKeyInitPart3() {
        setupP2pe();

        // part 3 request payload
        // tcuid b8
        // KVC(KIA)

        byte[] ppid = p2pSec.as2805GetPpid();
        byte[] kcvKia = p2pSec.as2805GetKcvKia();

        byte[] payload = new byte[11];
        System.arraycopy( ppid, 0, payload, 0, ppid.length );
        System.arraycopy( kcvKia, 0, payload, ppid.length, 3 );

        // send/receive
        byte[] request = generate1804RSARequest( "899", payload );

        assertNotNull( request );

        // send to host
        byte[] response = sendReceive( TEST_ENDPOINT_IP, TEST_ENDPOINT_PORT, request );
        assertNotNull( response );

        byte[] respPayload = extractPayload( response );
        assertNotNull( respPayload );

        // unpack part 3 payload
        // eKIA(KEK1) b16
        // eKIA(KEK2) b16
        // eKIA(PPASN) b16
        // MAC of eKIA(KEK1)||eKIA(KEK2)||eKIA(PPASN) using acquirers KMACI b4
        // KVC(KEK1) b3
        // KVC(KEK2) b3
        byte[] eKiaKek1eKiaKek2eKiav88Ppasn = Arrays.copyOfRange(respPayload, 0, 40);
        byte[] mac = Arrays.copyOfRange(respPayload, 40, 44);
        byte[] kek1Kvc = Arrays.copyOfRange(respPayload, 44, 47);
        byte[] kek2Kvc = Arrays.copyOfRange(respPayload, 47, 50);
        byte[] ppasnKvc = new byte[3]; // null fill, doesn't matter

        assertTrue( p2pSec.as2805LoadKek1Kek2Ppasn(eKiaKek1eKiaKek2eKiav88Ppasn, kek1Kvc, kek2Kvc, ppasnKvc, AIIC_BLOCK) );
    }

    @Test
    public void testRsaKeyInitPart4() {
        setupP2pe();

        // part 4 session key exchange
        // tcuid b8
        // KEK flag n1

        byte[] ppid = p2pSec.as2805GetPpid();
        byte[] kekFlag = "1".getBytes();

        byte[] payload = new byte[9];
        System.arraycopy( ppid, 0, payload, 0, ppid.length );
        System.arraycopy( kekFlag, 0, payload, ppid.length, 1 );

        // send/receive
        byte[] request = generate1804RSARequest( "811", payload );

        assertNotNull( request );

        // send to host
        byte[] response = sendReceive( TEST_ENDPOINT_IP, TEST_ENDPOINT_PORT, request );
        assertNotNull( response );

        byte[] respPayload = extractPayload( response );
        assertNotNull( respPayload );

        // unpack part 4 payload
        // KVC(KEK1)
        // eKEK1(KMACs)
        // eKEK1(KMACr)
        // eKEK1(KDs)
        // eKEK1(KDr)
        // eKEK1(KPP)
        byte[] eKek1Block = new byte[80];
        byte[] kvcKek1 = new byte[3];
        System.arraycopy( respPayload, 3, eKek1Block, 0, 80 );
        System.arraycopy( respPayload, 0, kvcKek1, 0, 3 );

        assertTrue( p2pSec.as2805LoadSessionKeysEftexStyle('1', eKek1Block, kvcKek1 ) );
    }

    @Test
    public void testRsaKeyInitPart5() {
        setupP2pe();
        final int TEST_STAN = 41;

        // send a macced and encrypted transaction request
        byte[] requestMsgClear = new byte[] {
            (byte)0x31, (byte)0x32, (byte)0x30, (byte)0x30, (byte)0x32, (byte)0x30, (byte)0x07, (byte)0x08, (byte)0x20, (byte)0xC1, (byte)0x90, (byte)0x01, (byte)0x30, (byte)0x30, (byte)0x30,
            (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x31, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x31,
            (byte)0x30, (byte)0x32, (byte)0x37, (byte)0x30, (byte)0x32, (byte)0x34, (byte)0x31, (byte)0x34, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x34, (byte)0x31, (byte)0x32,
            (byte)0x31, (byte)0x31, (byte)0x30, (byte)0x32, (byte)0x37, (byte)0x31, (byte)0x35, (byte)0x34, (byte)0x31, (byte)0x32, (byte)0x30, (byte)0x35, (byte)0x31, (byte)0x30, (byte)0x31, (byte)0x30,
            (byte)0x31, (byte)0x37, (byte)0x31, (byte)0x33, (byte)0x31, (byte)0x30, (byte)0x43, (byte)0x30, (byte)0x30, (byte)0x31, (byte)0x30, (byte)0x30, (byte)0x31, (byte)0x31, (byte)0x30, (byte)0x30,
            (byte)0x30, (byte)0x30, (byte)0x31, (byte)0x33, (byte)0x34, (byte)0x34, (byte)0x37, (byte)0x36, (byte)0x31, (byte)0x37, (byte)0x33, (byte)0x39, (byte)0x30, (byte)0x30, (byte)0x31, (byte)0x30,
            (byte)0x31, (byte)0x30, (byte)0x31, (byte)0x31, (byte)0x39, (byte)0x3D, (byte)0x32, (byte)0x32, (byte)0x31, (byte)0x32, (byte)0x32, (byte)0x30, (byte)0x31, (byte)0x31, (byte)0x37, (byte)0x35,
            (byte)0x38, (byte)0x39, (byte)0x32, (byte)0x38, (byte)0x38, (byte)0x38, (byte)0x39, (byte)0x31, (byte)0x32, (byte)0x33, (byte)0x34, (byte)0x34, (byte)0x33, (byte)0x32, (byte)0x31, (byte)0x31,
            (byte)0x30, (byte)0x32, (byte)0x35, (byte)0x30, (byte)0x35, (byte)0x37, (byte)0x38, (byte)0x30, (byte)0x38, (byte)0x37, (byte)0x39, (byte)0x38, (byte)0x35, (byte)0x31, (byte)0x30, (byte)0x30,
            (byte)0x32, (byte)0x34, (byte)0xF0, (byte)0x00, (byte)0x15, (byte)0x80, (byte)0x00, (byte)0x31, (byte)0x32, (byte)0x33, (byte)0x34, (byte)0x34, (byte)0x33, (byte)0x32, (byte)0x31, (byte)0x30,
            (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x34, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x30, (byte)0x33, (byte)0x36, (byte)0xC6, (byte)0xEB, (byte)0x68,
            (byte)0xD9, (byte)0x54, (byte)0xD2, (byte)0x70, (byte)0x56 //, (byte)0x77, (byte)0xEA, (byte)0x88, (byte)0xBC, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00
        };

        IP2PEncrypt.EncryptParameters encryptParameters = new IP2PEncrypt.EncryptParameters( IP2PEncrypt.PaddingAlgorithm.FF_BYTES, AS2805_3DES_OFB, 0, TEST_STAN );
        IP2PEncrypt.MacParameters macParameters = new IP2PEncrypt.MacParameters( IP2PEncrypt.MacAlgorithm.AS2805, 0, "" );

        // create zero size array
        CardholderDataElement[] elements = new CardholderDataElement[0];
        byte[] mac = p2pEncrypt.getMac( requestMsgClear, macParameters, elements );

        assertNotNull( mac );

        // add mac to msg to send
        byte[] maccedMsg = new byte[requestMsgClear.length + 8];
        System.arraycopy( requestMsgClear, 0, maccedMsg, 0, requestMsgClear.length );
        System.arraycopy( mac, 0, maccedMsg, requestMsgClear.length, 8 );

        EncryptResult encryptResult = p2pEncrypt.encrypt( maccedMsg, encryptParameters, elements );
        assertNotNull( encryptResult );
        assertNotNull( encryptResult.getEncryptedMessage() );
        assertTrue( encryptResult.getEncryptedMessage().length > 0 );

        byte[] encryptedPayload = encryptResult.getEncryptedMessage();

        byte[] encryptedHdr = new byte[]{
                (byte) 0x54, (byte) 0xDF, (byte) 0x02, (byte) 0x06, (byte) 0x30, (byte) 0x30, (byte) 0x30, (byte) 0x30, (byte) 0x34, (byte) 0x31, (byte) 0xDF, (byte) 0x01, (byte) 0x08, (byte) 0x31, (byte) 0x32, (byte) 0x33,
                (byte) 0x34, (byte) 0x34, (byte) 0x33, (byte) 0x32, (byte) 0x31, (byte) 0xDF, (byte) 0x00, (byte) 0x01, (byte) 0x31, (byte) 0x42
        };

        byte[] msgToSend = new byte[encryptedPayload.length + encryptedHdr.length];
        System.arraycopy( encryptedHdr, 0, msgToSend, 0, encryptedHdr.length );
        System.arraycopy( encryptedPayload, 0, msgToSend, encryptedHdr.length, encryptedPayload.length );

        byte[] response = sendReceive( TEST_ENDPOINT_IP, TEST_ENDPOINT_PORT, msgToSend );
        assertNotNull( response );


        final int ENCRYPTED_HEADER_OVERHEAD = 26;
        assertTrue( response.length > ENCRYPTED_HEADER_OVERHEAD );
        // get ready to decrypt
        byte[] encryptedResponse = new byte[response.length - ENCRYPTED_HEADER_OVERHEAD];

        System.arraycopy( response, ENCRYPTED_HEADER_OVERHEAD, encryptedResponse, 0, encryptedResponse.length );

        // decrypt
        byte[] decryptedMsg = p2pSec.as2805DecryptMessage( encryptedResponse, TEST_STAN, IP2PEncrypt.PaddingAlgorithm.FF_BYTES );
        assertNotNull(decryptedMsg);

        // validate mac on response
        assertTrue( p2pSec.as2805VerifyMac(decryptedMsg) );

    }

    private byte[] extractPayload( byte[] input ) {
        assertTrue( input.length > 0x4E );
        byte[] response = new byte[input.length - 0x4e];
        System.arraycopy( input, 0x4e, response, 0, response.length );
        return response;
    }

    private byte[] sendReceive( String endpoint, int port, byte[] sendData ) {
        Socket tcpSocket = null;

        try {
            // attempt to connect
            tcpSocket = new Socket( endpoint, port );

            OutputStream outputStream = tcpSocket.getOutputStream();

            ByteArrayOutputStream combinedMsg = new ByteArrayOutputStream();
            // append 2 byte length prefix
            byte[] txMsgLength = new byte[2];
            txMsgLength[0] = (byte)(sendData.length / 0x100);
            txMsgLength[1] = (byte)(sendData.length % 0x100);
            combinedMsg.write( txMsgLength );
            combinedMsg.write( sendData );

            // do the send
            outputStream.write( combinedMsg.toByteArray() );

            // read response msg
            tcpSocket.setSoTimeout(5 * 1000);
            InputStream inputStream = tcpSocket.getInputStream();

            byte[] msgRxLength = new byte[2];

            // receive length bytes
            int bytesRead = inputStream.read(msgRxLength, 0, 2);
            if( bytesRead != 2 ) {
                throw new Exception();
            }

            int rxMsgLength = msgRxLength[0] * 0x100 + (msgRxLength[1] & 0xff);

            byte[] msgReceived = new byte[rxMsgLength];
            bytesRead = inputStream.read(msgReceived, 0, rxMsgLength);
            if( bytesRead != rxMsgLength ) {
                throw new Exception();
            }
            return msgReceived;

        } catch (Exception ex) {
            Timber.i( "connectSocket failed" );
            ex.printStackTrace();
            return null;
        } finally {
            try {
                // close socket
                if (tcpSocket != null) {
                    tcpSocket.close();
                }
            } catch( Exception e ) {
                Timber.w(e);
            }
        }
    }

    @Test
    public void test1200RequestResponse() throws Exception {
        setupP2pe();

        Dependencies d = new Dependencies();
        d.setConfig(Config.getInstance());
        d.setProtocol(new Protocol());


        As2805Eftex msg = new As2805Eftex();

        msg.setMsgType(_1200_TRAN_REQ);
        msg.set(_003_PROC_CODE, "000000");
        msg.set(_004_TRAN_AMOUNT, "000000001234");
        msg.set(_007_TRAN_DATE_TIME, "1014100151");
        msg.set(_011_SYS_TRACE_AUDIT_NUM, "000026");
        msg.set(_012_LOCAL_TRAN_DATETIME, "211014230138");
        msg.set(_022_POS_DATA_CODE, "51010171310C001");
        msg.set(_023_CARD_SEQ_NR, "001");
        msg.set(_024_FUNC_CODE, "100");
        msg.set(_029_RECON_INDICATOR, "001");
        msg.set(_035_TRACK_2_DATA, "4761739001010119=22122011758928889");
        msg.set(_041_TERMINAL_ID, "W5890003");
        msg.set(_042_CARD_ACCEPTOR_ID, "611000602005890");

        AdditionalData addData = new AdditionalData();
        addData.put(PosData, "1234432100000300000");
        msg.putAdditionalData(addData);

//            msg.set(_048_PRIVATE_ADDITIONAL_DATA, "F00015800031323334343332313030303030333030303030");
        msg.set(_049_TRAN_CURRENCY_CODE, "036");
        msg.set(_052_PIN_DATA, "AD8114B401BEC1A9");
        msg.set(_064_MAC, "321329A700000000");
//            msg.set();

        // try send/receive
        byte[] msgToSend = As2805EftexPack.addMacAndEncrypt(false, msg);
        assertNotNull(msgToSend);

        byte[] rxMsg = sendReceive( TEST_ENDPOINT_IP, TEST_ENDPOINT_PORT, msgToSend );
        assertNotNull(rxMsg);

        // unpack 1210 response
        TransRec trans = new TransRec();
        trans.getProtocol().setStan(26);
        trans.getAudit().setTerminalId("W5890003");
        trans.getAudit().setMerchantId("611000602005890");
        As2805EftexPack.UnPackResult result = As2805EftexPack.unpack( d, rxMsg, trans, As2805EftexPack.MsgType.AUTH, null );
        assertNotNull(result);
        assertEquals(UNPACK_OK, result);

    }

}

