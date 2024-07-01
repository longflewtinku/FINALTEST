package com.linkly.launcher;

import android.annotation.SuppressLint;

public class ByteUtils {

    private final static byte[] hex = "0123456789ABCDEF".getBytes();

    public static byte int2bcd(int in) {
        return (byte) ((in / 10 * 16) + (in % 10));
    }

    public static String Bytes2HexString(byte[] b) {
        if (b == null) return "";
        byte[] buff = new byte[2 * b.length];
        for (int i = 0; i < b.length; i++) {
            buff[2 * i] = hex[(b[i] >> 4) & 0x0f];
            buff[2 * i + 1] = hex[b[i] & 0x0f];
        }
        return new String(buff);
    }

    public static String Byte2HexString(byte b) {
        byte[] buff = new byte[2];
        buff[0] = hex[(b >> 4) & 0x0f];
        buff[1] = hex[b & 0x0f];
        return new String(buff);
    }

    public static String hexString2asciiString(String hex) {
        int j = 0;
        byte[] data = new byte[hex.length() / 2];
        for (int i = 0; i < hex.length() - 1; i += 2) {
            byte a1 = toByte(hex.charAt(i));
            byte a2 = toByte(hex.charAt(i + 1));
            data[j++] = (byte) (a1 * 16 + a2);
        }
        return new String(data);
    }

    public static byte toByte(char c) {
        byte b = (byte) "0123456789abcdef".indexOf(c);
        if (b == -1) b = (byte) "0123456789ABCDEF".indexOf(c);
        return b;
    }

    /**
     * 每个byte直接带空格
     */
    public static String bytes2HexString(byte[] bytes, int len) {
        StringBuilder sb = new StringBuilder();
        int n = bytes.length > len ? len : bytes.length;
        for (int i = 0; i < n; i++)
            sb.append(String.format("0x%02x ", bytes[i]));
        return sb.toString();
    }


    @SuppressLint("DefaultLocale")
    public static void memcpy(byte[] des, int start, String hexString, int len) {
        if (len != hexString.length()) {
            char[] achar = hexString.toLowerCase().toCharArray();
            for (int i = 0; i < len && i < (des.length - start)
                    && (i * 2 + 1) < achar.length; i++) {
                int pos = i * 2;
                des[start + i] = (byte) (toByte(achar[pos]) << 4 | toByte(achar[pos + 1]));
            }
        } else {
            byte[] source = hexString.getBytes();
            for (int i = 0; i < len && i < (des.length - start)
                    && i < source.length; i++)
                des[start + i] = source[i];
        }
    }

    public static void memcpy(byte[] des, int start, byte[] source, int len) {
        memcpy(des, start, source, 0, len);
    }

    public static void memcpy(byte[] des, int dstart, byte[] source,
                              int sstart, int len) {
        int i;
        for (i = 0; (i < len) && i < (des.length - dstart)
                && i < (source.length - sstart); i++)
            des[i + dstart] = source[i + sstart];
    }

    public static void memcpy(byte[] des, String hexString, int len) {
        memcpy(des, 0, hexString, len);
    }

    public static byte[] subBytes(byte[] src, int begin, int count) {
        byte[] bs = new byte[count];
        for (int i = begin; i < begin + count; i++)
            bs[i - begin] = src[i];
        return bs;
    }


    /**
     * str[0]~str[len-1]异或,并且把数据放到str[len],并且返回str[len]
     */
    public static byte getLrc(byte[] str, int len) {
        str[len] = str[0];
        for (int i = 1; i < len; i++)
            str[len] ^= str[i];
        return str[len];
    }

    /**
     * lrc是先前已经有部分数据异或得到的
     */
    public static byte getLrc(byte[] bytes, byte lrc) {
        for (int i = 0; i < bytes.length; i++) {
            lrc ^= bytes[i];
        }
        return lrc;
    }

    /**
     * 获取异或数据
     */
    public static byte getLrc(byte[] bytes) {
        byte lrc = 0x00;
        for (int i = 0; i < bytes.length; i++) {
            lrc ^= bytes[i];
        }
        return lrc;
    }

    /**
     * 高位在低索引,低位在高索引,是大端，跟内存的小端存储差异需要注意
     */
    public static byte[] int2Bytes(int value) {
        byte[] src = new byte[4];
        src[0] = (byte) ((value >> 24) & 0xFF);
        src[1] = (byte) ((value >> 16) & 0xFF);
        src[2] = (byte) ((value >> 8) & 0xFF);
        src[3] = (byte) (value & 0xFF);
        return src;
    }

    /**
     * 高位在低索引,低位在高索引,是大端，跟内存的小端存储差异需要注意
     */
    public static void int2Bytes(int value, byte[] dst, int offset) {
        dst[offset] = (byte) ((value >> 24) & 0xFF);
        dst[offset + 1] = (byte) ((value >> 16) & 0xFF);
        dst[offset + 2] = (byte) ((value >> 8) & 0xFF);
        dst[offset + 3] = (byte) (value & 0xFF);
    }

    /***
     * 高字节在高位，低字节在低位，这个是小端
     */
    public static int bytes2Int(byte[] src, int offset) {
        return (((src[offset + 3] & 0xFF) << 24) | ((src[offset + 2] & 0xFF) << 16) | ((src[offset + 1] & 0xFF) << 8) | (src[offset] & 0xFF));
    }


    /***
     * 高字节在低位，低字节在高位，这个是大端
     */
    public static int bigBytes2Int(byte[] src, int offset) {
        return (((src[offset] & 0xFF) << 24) | ((src[offset + 1] & 0xFF) << 16) | ((src[offset + 2] & 0xFF) << 8) | (src[offset + 3] & 0xFF));
    }


    public static String convertToMac(byte[] mac) {
        if (mac == null)
            return null;

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < mac.length; i++) {
            byte b = mac[i];
            int value;
            if (b >= 0 && b < 16) {
                value = b;
                sb.append("0" + Integer.toHexString(value));
            } else if (b >= 16) {
                value = b;
                sb.append(Integer.toHexString(value));
            } else {
                value = 256 + b;
                sb.append(Integer.toHexString(value));
            }
            if (i != mac.length - 1) {
                sb.append(":");
            }
        }
        return sb.toString();
    }


    public static byte hexStrToByte(String hexbytein) {
        return (byte) Integer.parseInt(hexbytein, 16);
    }

    public static byte[] str2Hex(String hexStr) {
        int hexlen = hexStr.length() / 2;
        byte[] result;
        result = new byte[hexlen];
        for (int i = 0; i < hexlen; i++) {
            result[i] = hexStrToByte(hexStr.substring(i * 2, i * 2 + 2));
        }
        return result;
    }

    public static String Hex2Str(byte[] hexByteIn) {
        int len = hexByteIn.length;
        String restult = new String();
        for (int i = 0; i < len; i++) {
            restult += String.format("%02x", hexByteIn[i]);
        }
        return restult;
    }


}
