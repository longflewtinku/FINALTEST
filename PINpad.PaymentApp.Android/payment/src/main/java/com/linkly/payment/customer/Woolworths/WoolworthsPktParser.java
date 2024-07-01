package com.linkly.payment.customer.Woolworths;

import com.google.gson.JsonSyntaxException;
import com.linkly.libconfig.WoolworthsPktConfig;
import com.linkly.libengine.tlv.BerTlv;
import com.linkly.libengine.tlv.BerTlvParser;
import com.linkly.libengine.tlv.BerTlvs;
import com.linkly.libengine.tlv.IBerTlvLogger;
import com.linkly.libmal.IMal;
import com.linkly.libmal.MalFactory;
import com.linkly.libmal.global.config.ConfigExceptions;
import com.linkly.libmal.global.config.Parse;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import timber.log.Timber;

public class WoolworthsPktParser extends Parse {
    private static final int PAGE_BYTE_LENGTH = 8; // Length of 'Page' bytes to remove

    private static int bytes2Int(byte highVal, byte lowVal) {
        return ((highVal & 0xFF) << 8) | (lowVal & 0xFF);
    }

    /**
     * strips 8 'paging' bytes that occur throughout PKT file
     * 
     * @param inputBytes input bytes
     * @return output bytes with paging bytes removed
     */
    public static byte[] stripPageBytes(byte[] inputBytes) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int inputOffset = 0;
        boolean lastPage = false;

        while (inputOffset < inputBytes.length && !lastPage) {
            // skip these bytes, jump forward by PAGE_BYTE_LENGTH
            // read page info into local buffer
            byte[] pageBytes = new byte[PAGE_BYTE_LENGTH];
            System.arraycopy(inputBytes, inputOffset, pageBytes, 0, PAGE_BYTE_LENGTH);

            // data in this page is defined in bytes 6-7 (0 based)
            int dataInThisPage = bytes2Int(pageBytes[6],pageBytes[7]);
            // last page if page number (offset 2-3) equals total pages (offset 0-1)
            int pageNumber = bytes2Int(pageBytes[2], pageBytes[3]);
            int totalPages = bytes2Int(pageBytes[0], pageBytes[1]);
            lastPage = pageNumber == totalPages;
            inputOffset += PAGE_BYTE_LENGTH;

            // copy minimum of remaining bytes or CHUNK_SIZE
            int bytesToCopy = Math.min(inputBytes.length - inputOffset, dataInThisPage);
            if (bytesToCopy > 0) {
                baos.write(Arrays.copyOfRange(inputBytes, inputOffset, inputOffset+bytesToCopy));
                inputOffset += bytesToCopy;
            }
        }
        return baos.toByteArray();
    }

    public static WoolworthsPktConfig parseFromBytes(byte[] input) {
        WoolworthsPktConfig pkConfig = new WoolworthsPktConfig();
        List<WoolworthsPktConfig.CAPKey> keyList = new ArrayList<>();

        BerTlvParser parser = new BerTlvParser(new IBerTlvLogger(){
            @Override
            public boolean isDebugEnabled() {
                // enable for debug, unit tests
                return true;
//                return false;
            }

            @Override
            public void debug(String aFormat, Object... args) {
                String format = aFormat.replace("{}", "%s");
                Timber.v(format, args);
            }
        });

        try {
            // strip 'paging' bytes
            byte[] strippedData = stripPageBytes(input);

            BerTlvs tlvs = parser.parseBuffer(strippedData, 0);
            Timber.i("num tlvs = %d", tlvs.getList().size());

            for (BerTlv tag : tlvs.getList()) {
                switch (tag.getTag().getHexValue()) {
                    case "DFE959":
                        // table version
                        Timber.i("table version : %s", tag.getHexValue());
                        pkConfig.setTableVersion(tag.getHexValue());
                        break;
                    case "FFE911":
                        String rid = "";
                        String keyIdx = "";
                        String keyModulus = "";
                        String keyExponent = "";
                        String algorithm = "";
                        String dateEffective = "";
                        String dateExpiry = "";

                        // container tag, emv key data
                        for (BerTlv keyTag: tag.getValues()) {
                            switch (keyTag.getTag().getHexValue()) {
                                case "DFE918":
                                    Timber.i("RID        : %s", keyTag.getHexValue());
                                    rid = keyTag.getHexValue();
                                    break;
                                case "DFE919":
                                    Timber.i("   Key idx : %s", keyTag.getHexValue());
                                    keyIdx = keyTag.getHexValue();
                                    break;
                                case "DFE91A":
                                    Timber.i("   Key data: %s", keyTag.getHexValue());
                                    keyModulus = keyTag.getHexValue();
                                    break;
                                case "DFE91B":
                                    Timber.i("   Key exponent : %s", keyTag.getHexValue());
                                    keyExponent = keyTag.getHexValue();
                                    break;
                                case "DFE91C":
                                    Timber.i("   Key algrtm : %s", keyTag.getHexValue());
                                    algorithm = keyTag.getHexValue();
                                    break;
                                case "DFE95B":
                                    Timber.i("   Key start  : %s", keyTag.getHexValue());
                                    dateEffective = convertDate(keyTag.getBytesValue());
                                    break;
                                case "DFE95C":
                                    Timber.i("   Key expiry : %s", keyTag.getHexValue());
                                    dateExpiry = convertDate(keyTag.getBytesValue());
                                    break;
                                default:
                                    Timber.i("   Unknown %s : %s", keyTag.getTag().getHexValue(), keyTag.getHexValue());
                                    break;
                            }
                        }
                        keyList.add(new WoolworthsPktConfig.CAPKey(rid, keyIdx, keyExponent, algorithm, dateEffective, dateExpiry, keyModulus));
                        break;
                }
            }
        } catch (Exception e){
            return null;
        }
        pkConfig.setKeyList(keyList);
        return pkConfig;
    }

    /**
     * converts from 4 byte input format DDMMYYYY
     * where DD and MM are binary
     * and YYYY is binary, low byte, high byte
     * to ddmmyy format (string)
     *
     * @param binaryFormat input
     * @return output in ddmmyy format
     */
    private static String convertDate(byte[] binaryFormat){
        int day = binaryFormat[0];
        int month = binaryFormat[1];
        int year = bytes2Int(binaryFormat[3], binaryFormat[2]);
        return String.format(Locale.getDefault(), "%02d%02d%02d", day, month, year%100);
    }


    public static int countPatternOccurrences(byte[] data, byte[] pattern) {
        int count = 0;
        if (pattern.length == 0) return 0;

        // Loop through the data array
        for (int i = 0; i <= data.length - pattern.length; i++) {
            // Check if the pattern matches starting at data[i]
            boolean match = true;
            for (int j = 0; j < pattern.length; j++) {
                if (data[i + j] != pattern[j]) {
                    match = false;
                    break;
                }
            }
            // Increment count if all bytes match
            if (match) {
                count++;
                i += pattern.length - 1; // Move index forward by the pattern length
            }
        }

        return count; // Return the total count of pattern occurrences
    }

    @Override
    @SuppressWarnings("unchecked") // Suppressing unchecked cast because the type check is performed just before casting.
    public <T> T parse(String filename, Type classOfT) throws ConfigExceptions.ParseErrorException, ConfigExceptions.FileErrorException {
        IMal mal = MalFactory.getInstance();
        try {
            byte[] cfgData = this.readFileAsBinary(mal, filename);
            WoolworthsPktConfig result = parseFromBytes(cfgData);
            if (!classOfT.equals(WoolworthsPktConfig.class)) {
                throw new IllegalArgumentException("Unsupported type. Expected: MyConfigType");
            }
            return (T) result;
        } catch (JsonSyntaxException var6) {
            throw new ConfigExceptions.ParseErrorException("Failed to Parse:" + filename, var6);
        } catch (Exception var7) {
            throw new ConfigExceptions.FileErrorException("Unable to load file:" + filename, var7);
        }
    }

    @Override
    public String toString(Object o, Type type) {
        return null;
    }

    @Override
    public <T> T parseFromString(String s, Type type) {
        return null;
    }

    @Override
    public boolean validate(String filePath, String schemaPath) {
        return false;
    }

}
