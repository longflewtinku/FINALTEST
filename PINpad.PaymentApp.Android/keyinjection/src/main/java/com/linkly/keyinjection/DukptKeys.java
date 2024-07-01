package com.linkly.keyinjection;

import com.linkly.libmal.global.util.Util;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import timber.log.Timber;

public class DukptKeys {
    public static String generateInitialKey(String bdkStr, String ksn) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        // for some reason, we need to provide 192 bit keys to this algorithm
        // our input is 128 bits though. take first 64 bits and append to the end, this doesn't affect crypto output
        String longBdkStr = bdkStr + bdkStr.substring(0,16);
        byte[] bdk = Util.hexStringToByteArray(longBdkStr);

        byte[] ksnLeft8Bytes = Util.hexStringToByteArray(ksn);
        SecretKey bdkKey = new SecretKeySpec(adjustForParityBits(bdk), "DESede");

        // Create a cipher using Triple DES (also known as DESede)
        Cipher cipher = Cipher.getInstance("DESede/ECB/PKCS5Padding");

        // Encryption
        cipher.init(Cipher.ENCRYPT_MODE, bdkKey);
        byte[] leftmost8Bytes = truncateToEightBytes(cipher.doFinal(ksnLeft8Bytes));
        Timber.i("leftmost 8 bytes of initial key: %s", Util.byteArrayToHexString(leftmost8Bytes));

        // now xor BDK with constant C0C0C0C000000000 C0C0C0C000000000 to generate key for 2nd 3des encryption
        byte[] bdkVariant = xorArrays(bdk, Util.hexStringToByteArray("C0C0C0C000000000C0C0C0C000000000C0C0C0C000000000"));
        SecretKey bdkKeyVariant = new SecretKeySpec(adjustForParityBits(bdkVariant), "DESede");

        // Encryption
        cipher.init(Cipher.ENCRYPT_MODE, bdkKeyVariant);
        byte[] rightmost8Bytes = truncateToEightBytes(cipher.doFinal(ksnLeft8Bytes));
        Timber.i("rightmost 8 bytes of initial key: %s", Util.byteArrayToHexString(rightmost8Bytes));
        String initialKey = Util.byteArrayToHexString(leftmost8Bytes) + Util.byteArrayToHexString(rightmost8Bytes);
        Timber.e("calculated initial key for ksn [%s] is [%s]", ksn, initialKey);
        return initialKey;
    }

    public static String generateKsn(KeySet.KeyVal keyVal){
        Random random = new Random();
        // Define the fixed field values
        long issuerIdNumber = Integer.parseInt(keyVal.dukptIssuerId, 16); // 24 bits
        long bdkIdxCustId = Integer.parseInt(keyVal.dukptBdkIdx, 16);       // 8 bits
        long vendorGrpId = Integer.parseInt(keyVal.dukptVendorId, 16);        // 8 bits (22 in decimal)
        long transactionCounter = 0x00001; // 21 bits

        Timber.i("issuerIdNumber=0x%x", issuerIdNumber);
        Timber.i("bdkIdxCustId=0x%x", bdkIdxCustId);
        Timber.i("vendorGrpId=0x%x", vendorGrpId);
        Timber.i("transactionCounter=0x%x", transactionCounter);

        // Generate a random 19-bit number for the TRMS field
        long trsmId = random.nextInt(524288); // 524288 is 2^19

        Timber.i("trsmId=0x%x", trsmId);

        // High part (32 bits total): Issuer ID (24 bits) + BDK IDX/Cust ID (8 bits)
        long high = (issuerIdNumber << 8) | bdkIdxCustId;

        // Low part (48 bits total): Vendor/Grp ID (8 bits) + TRMS ID (19 bits) + Transaction Counter (21 bits)
        long low = (vendorGrpId << 40) | (trsmId << 21) | transactionCounter;

        // Print out the high and low parts in hexadecimal format
        Timber.i("High Part: 0x%s", Long.toHexString(high));
        Timber.i("Low Part: 0x%s", Long.toHexString(low));

        String ksnString = (Long.toHexString(high) + Long.toHexString(low)).toUpperCase();
        Timber.i("ksn string = %s", ksnString);
        return ksnString;
    }

    // Adjust each byte to clear the parity bit (least significant bit)
    private static byte[] adjustForParityBits(byte[] keyWithParity) {
        byte[] keyWithoutParity = new byte[keyWithParity.length];
        for (int i = 0; i < keyWithParity.length; i++) {
            // Clear the least significant bit
            keyWithoutParity[i] = (byte) (keyWithParity[i] & 0xFE);
        }
        return keyWithoutParity;
    }

    /**
     * XORs two byte arrays.
     *
     * @param array1 the first byte array
     * @param array2 the second byte array
     * @return a new byte array resulting from XOR operation
     * @throws IllegalArgumentException if arrays are not of the same length
     */
    public static byte[] xorArrays(byte[] array1, byte[] array2) {
        if (array1.length != array2.length) {
            throw new IllegalArgumentException("Arrays must be of the same length");
        }

        byte[] result = new byte[array1.length];
        for (int i = 0; i < array1.length; i++) {
            result[i] = (byte) (array1[i] ^ array2[i]);
        }
        return result;
    }

    public static byte[] truncateToEightBytes(byte[] original) {
        if (original.length <= 8) {
            return original;  // Return the original array if it's already 8 bytes or fewer
        }

        byte[] truncated = new byte[8];
        System.arraycopy(original, 0, truncated, 0, 8);  // Copy the first 8 bytes
        return truncated;
    }
}
