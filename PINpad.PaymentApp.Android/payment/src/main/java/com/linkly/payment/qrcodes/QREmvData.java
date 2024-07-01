package com.linkly.payment.qrcodes;

import com.linkly.libengine.engine.transactions.TransRec;
import com.linkly.libengine.tlv.BerTlv;
import com.linkly.libengine.tlv.BerTlvParser;
import com.linkly.libengine.tlv.BerTlvs;
import com.linkly.libengine.tlv.HexUtil;

import java.util.ArrayList;
import java.util.List;

public class QREmvData implements IQRData {

    public String EncodeQRData(TransRec trans) {

        return null;
    }

    public TransRec DecodeQRData(String qrData) {

        DecodedQRData data = new DecodedQRData();
        data.decodeData(qrData);

        // do some application selection

        return null;
    }

    public class EncodedQRData {
        String qrData;

        // S
        public void AddString(String tag, String data) {

            if (tag.length() != 2) return;

            qrData += tag;
            qrData += Integer.toHexString(data.length());
            qrData += data;
        }

        // ANS
        public void AddStringSpecial(String tag, String data) {
            if (tag.length() != 2) return;

            // Special character checking

            qrData += tag;
            qrData += Integer.toHexString(data.length());
            qrData += data;
        }

        // N
        public void AddInt(String tag, int value) {
            if (tag.length() != 2) return;

            qrData += tag;
            qrData += Integer.toHexString(value);
            qrData += value + "";
        }
    }


    public class DecodedQRData {

        String payloadFormatIndicator = "";
        ArrayList<ApplicationTemplate> applicationTemplate = new ArrayList<>();
        CommonTemplate commonDataTemplate = null;

        public void decodeData(String data) {

            byte[] bytes = HexUtil.parseHex(data);
            BerTlvParser parcer = new BerTlvParser();
            BerTlvs tlvs = parcer.parse(bytes, 0, bytes.length);

            parseTlv(tlvs.getList());
        }

        public void parseTlv(List<BerTlv> tlvs) {
            for (BerTlv tlv : tlvs) {

                switch (tlv.getTag().toString()) {
                    case "85":
                        payloadFormatIndicator = tlv.getTextValue();
                        break;
                    case "61":
                        applicationTemplate.add(parseTlvApplicationTemplate(tlv.getValues()));
                        break;
                    case "62":
                        commonDataTemplate = parseTlvCommonTemplate(tlv.getValues());
                        break;
                }
            }
        }

        private ApplicationTemplate parseTlvApplicationTemplate(List<BerTlv> tlvs) {
            ApplicationTemplate template = new ApplicationTemplate();

            for (BerTlv tlv : tlvs) {

                switch (tlv.getTag().toString()) {
                    case "63":
                        template.transparentTemplateData = tlv.getBytesValue();
                        break;
                    case "4F":
                        template.ADF = tlv.getTextValue();
                        break;
                    case "50":
                        template.applicationName = tlv.getTextValue();
                        break;
                    case "9F08":
                        template.versionNumber = tlv.getTextValue();
                        break;
                    case "5F20":
                        template.cardNumber = tlv.getTextValue();
                        break;
                    case "5A":
                        template.PAN = tlv.getTextValue();
                        break;
                    case"5F50":
                        template.issuerURL = tlv.getTextValue();
                        break;
                    case "9F25":
                        template.last4Pan = tlv.getTextValue();
                        break;
                    case "5F2D":
                        template.languagePreference = tlv.getTextValue();
                        break;
                    case "57":
                        template.track2Equiv = tlv.getTextValue();
                        break;
                    case "9F19":
                        template.tokenRequesterID = tlv.getTextValue();
                        break;
                    case "9F24":
                        template.paymentAccountReference = tlv.getTextValue();
                        break;
                }
            }

            return null;
        }

        private CommonTemplate parseTlvCommonTemplate(List<BerTlv> tlvs) {
            CommonTemplate template = new CommonTemplate();
            for (BerTlv tlv : tlvs) {

                switch (tlv.getTag().toString()) {
                    case "64":
                        template.transparentTemplateData = tlv.getBytesValue();
                        break;
                    case "9F08":
                        template.versionNumber = tlv.getTextValue();
                        break;
                    case "5F20":
                        template.cardNumber = tlv.getTextValue();
                        break;
                    case "5A":
                        template.PAN = tlv.getTextValue();
                        break;
                    case"5F50":
                        template.issuerURL = tlv.getTextValue();
                        break;
                    case "9F25":
                        template.last4Pan = tlv.getTextValue();
                        break;
                    case "5F2D":
                        template.languagePreference = tlv.getTextValue();
                        break;
                    case "57":
                        template.track2Equiv = tlv.getTextValue();
                        break;
                    case "9F19":
                        template.tokenRequesterID = tlv.getTextValue();
                        break;
                    case "9F24":
                        template.paymentAccountReference = tlv.getTextValue();
                        break;
                }
            }
            return null;
        }

        private class CommonTemplate {
            byte[] transparentTemplateData;
            String versionNumber;
            String cardNumber;
            String PAN;
            String issuerURL;
            String last4Pan;
            String languagePreference;
            String track2Equiv;
            String tokenRequesterID;
            String paymentAccountReference;
        }

        private class ApplicationTemplate extends CommonTemplate {
            String ADF;
            String applicationName;
        }

    }
}
