package com.linkly.payment.customer.Woolworths;

//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import static com.linkly.payment.customer.Woolworths.WoolworthsPktParser.countPatternOccurrences;
import static com.linkly.payment.customer.Woolworths.WoolworthsPktParser.parseFromBytes;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.linkly.libconfig.WoolworthsCfgMapper;
import com.linkly.libconfig.WoolworthsEpatConfig;
import com.linkly.libconfig.WoolworthsPktConfig;
import com.linkly.libengine.action.check.CheckUserLevel;
import com.linkly.libengine.tlv.BerTlv;
import com.linkly.libengine.tlv.BerTlvParser;
import com.linkly.libengine.tlv.BerTlvs;
import com.linkly.libengine.tlv.IBerTlvLogger;
import com.linkly.libmal.global.config.JSONParse;
import com.linkly.libmal.global.config.Parse;
import com.linkly.libmal.global.config.XmlParse;
import com.linkly.libsecapp.CtlsCfg;
import com.linkly.libsecapp.EmvCfg;
import com.linkly.libsecapp.emv.Util;

import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import timber.log.Timber;

public class ParseEpatPkt {
    String testCfgEmvJson = "{\n" +
            "\"version\": \"1.0.0\"," +
            "  \"schemes\": [\n" +
            "    {\n" +
            "      \"rid\" : \"A000000025\",\n" +
            "      \"aids\": [\n" +
            "        {\n" +
            "          \"aid\": \"A00000002501\",\n" +
            "          \"appName\": \"American Exp.\",\n" +
            "          \"defaultAccount\": \"CRD\",\n" +
            "          \"binNumber\": 5\n" +
            "        }\n" +
            "      ]\n" +
            "    },\n" +
            "    {\n" +
            "      \"rid\" : \"A000000003\",\n" +
            "      \"aids\": [\n" +
            "        {\n" +
            "          \"aid\": \"A0000000031010\",\n" +
            "          \"appName\": \"Visa\",\n" +
            "          \"defaultAccount\": \"CRD\",\n" +
            "          \"binNumber\": 4\n" +
            "        },\n" +
            "        {\n" +
            "          \"aid\": \"A0000000031020\",\n" +
            "          \"appName\": \"Visa\",\n" +
            "          \"defaultAccount\": \"CRD\",\n" +
            "          \"binNumber\": 4\n" +
            "        }\n" +
            "      ]\n" +
            "    },\n" +
            "    {\n" +
            "      \"rid\" : \"A000000004\",\n" +
            "      \"aids\": [\n" +
            "        {\n" +
            "          \"aid\": \"A0000000041010\",\n" +
            "          \"appName\": \"MasterCard\",\n" +
            "          \"defaultAccount\": \"CRD\",\n" +
            "          \"binNumber\": 3\n" +
            "        }\n" +
            "      ]\n" +
            "    },\n" +
            "    {\n" +
            "      \"rid\" : \"A000000384\",\n" +
            "      \"aids\": [\n" +
            "        {\n" +
            "          \"aid\": \"A00000038410\",\n" +
            "          \"appName\": \"EFTPOS Saving\",\n" +
            "          \"defaultAccount\": \"SAV\",\n" +
            "          \"binNumber\": 1\n" +
            "        },\n" +
            "        {\n" +
            "          \"aid\": \"A00000038420\",\n" +
            "          \"appName\": \"EFTPOS Cheque\",\n" +
            "          \"defaultAccount\": \"CHQ\",\n" +
            "          \"binNumber\": 1\n" +
            "        }\n" +
            "      ]\n" +
            "    }\n" +
            "  ]\n" +
            "}";
    String testCfgCtlsEmvJson = "{\n" +
            "\"version\": \"2.0.0\"," +
            "  \"tornTransLifetime\": 300,\n" +
            "  \"tornTransLogMaxRecords\": 0,\n" +
            "  \"aids\": [\n" +
            "    {\n" +
            "      \"aid\": \"A0000000041010\",\n" +
            "      \"cardReportedName\": \"Mastercard PayPass\",\n" +
            "      \"schemeLabel\": \"MASTERCARD\",\n" +
            "      \"defaultAccount\": \"CRD\",\n" +
            "      \"binNumber\": 3,\n" +
            "      \"cardDataInputCaps\": \"E0\",\n" +
            "      \"ctlsLimitNoOnDeviceCvm\": 10001,\n" +
            "      \"ctlsLimitOnDeviceCvm\": 10001,\n" +
            "      \"cvmCapsCvm\": 60,\n" +
            "      \"cvmCapsNoCvm\": 8,\n" +
            "      \"defaultUdol\": \"9F6A04\",\n" +
            "      \"kernelConfig\": \"30\",\n" +
            "      \"kernelId\": 0,\n" +
            "      \"mobileSupportInidcator\": 1,\n" +
            "      \"msrCvmCapsCvm\": 10,\n" +
            "      \"msrCvmCapsNoCvm\": 0,\n" +
            "      \"paypassMsrAppVersionNumber\": \"0001\",\n" +
            "      \"securityCaps\": \"08\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"TTQ\": \"34004000\",\n" +
            "      \"aid\": \"A0000000031010\",\n" +
            "      \"cardReportedName\": \"Visa PayWave\",\n" +
            "      \"schemeLabel\": \"VISA\",\n" +
            "      \"defaultAccount\": \"CRD\",\n" +
            "      \"binNumber\": 4\n" +
            "    },\n" +
            "    {\n" +
            "      \"TTQ\": \"04004000\",\n" +
            "      \"aid\": \"A00000038410\",\n" +
            "      \"cardReportedName\": \"EFTPOS Savings\",\n" +
            "      \"schemeLabel\": \"EFTPOS\",\n" +
            "      \"defaultAccount\": \"SAV\",\n" +
            "      \"binNumber\": 1\n" +
            "    },\n" +
            "    {\n" +
            "      \"TTQ\": \"01004000\",\n" +
            "      \"aid\": \"A00000038420\",\n" +
            "      \"cardReportedName\": \"EFTPOS Cheque\",\n" +
            "      \"schemeLabel\": \"EFTPOS\",\n" +
            "      \"defaultAccount\": \"CHQ\",\n" +
            "      \"binNumber\": 1\n" +
            "    },\n" +
            "    {\n" +
            "      \"TTQ\": \"00000000\",\n" +
            "      \"aid\": \"A00000002501\",\n" +
            "      \"cardReportedName\": \"Amex\",\n" +
            "      \"schemeLabel\": \"AMEX\",\n" +
            "      \"defaultAccount\": \"CRD\",\n" +
            "      \"binNumber\": 5\n" +
            "    }\n" +
            "  ],\n" +
            "  \"holdTimeValue\": 0,\n" +
            "  \"messageHoldTime\": 50,\n" +
            "  \"readerLimits\": [\n" +
            "    {\n" +
            "      \"cvmLimit\": \"5001\",\n" +
            "      \"cvmLimitFlg\": true,\n" +
            "      \"floorLimit\": \"0\",\n" +
            "      \"floorLimitFlg\": true,\n" +
            "      \"programId\": \"3102682620\",\n" +
            "      \"statusCheck\": false,\n" +
            "      \"txnLimit\": \"5001\",\n" +
            "      \"txnLimitFlg\": false,\n" +
            "      \"zeroCheck\": true\n" +
            "    },\n" +
            "    {\n" +
            "      \"cvmLimit\": \"5001\",\n" +
            "      \"cvmLimitFlg\": true,\n" +
            "      \"floorLimit\": \"0\",\n" +
            "      \"floorLimitFlg\": true,\n" +
            "      \"programId\": \"3102682612000003\",\n" +
            "      \"statusCheck\": false,\n" +
            "      \"txnLimit\": \"5001\",\n" +
            "      \"txnLimitFlg\": false,\n" +
            "      \"zeroCheck\": true\n" +
            "    },\n" +
            "    {\n" +
            "      \"cvmLimit\": \"5001\",\n" +
            "      \"cvmLimitFlg\": true,\n" +
            "      \"floorLimit\": \"0\",\n" +
            "      \"floorLimitFlg\": true,\n" +
            "      \"programId\": \"3102682612\",\n" +
            "      \"statusCheck\": false,\n" +
            "      \"txnLimit\": \"5001\",\n" +
            "      \"txnLimitFlg\": false,\n" +
            "      \"zeroCheck\": true\n" +
            "    },\n" +
            "    {\n" +
            "      \"cvmLimit\": \"5001\",\n" +
            "      \"cvmLimitFlg\": true,\n" +
            "      \"floorLimit\": \"0\",\n" +
            "      \"floorLimitFlg\": true,\n" +
            "      \"programId\": \"3102682600\",\n" +
            "      \"statusCheck\": false,\n" +
            "      \"txnLimit\": \"5001\",\n" +
            "      \"txnLimitFlg\": false,\n" +
            "      \"zeroCheck\": true\n" +
            "    },\n" +
            "    {\n" +
            "      \"cvmLimit\": \"1000\",\n" +
            "      \"cvmLimitFlg\": true,\n" +
            "      \"floorLimit\": \"0\",\n" +
            "      \"floorLimitFlg\": true,\n" +
            "      \"programId\": \"00000001\",\n" +
            "      \"statusCheck\": true,\n" +
            "      \"txnLimit\": \"1500\",\n" +
            "      \"txnLimitFlg\": true,\n" +
            "      \"zeroCheck\": true\n" +
            "    },\n" +
            "    {\n" +
            "      \"cvmLimit\": \"200\",\n" +
            "      \"cvmLimitFlg\": true,\n" +
            "      \"floorLimit\": \"0\",\n" +
            "      \"floorLimitFlg\": true,\n" +
            "      \"programId\": \"00000006\",\n" +
            "      \"statusCheck\": true,\n" +
            "      \"txnLimit\": \"700\",\n" +
            "      \"txnLimitFlg\": true,\n" +
            "      \"zeroCheck\": true\n" +
            "    },\n" +
            "    {\n" +
            "      \"cvmLimit\": \"200\",\n" +
            "      \"cvmLimitFlg\": true,\n" +
            "      \"floorLimit\": \"0\",\n" +
            "      \"floorLimitFlg\": true,\n" +
            "      \"programId\": \"00000011\",\n" +
            "      \"statusCheck\": true,\n" +
            "      \"txnLimit\": \"300\",\n" +
            "      \"txnLimitFlg\": true,\n" +
            "      \"zeroCheck\": true\n" +
            "    }\n" +
            "  ]\n" +
            "}";

    String testPktData =
            "<PktParameterTable Table_Version=\"100131\">" +
            "<Key Rid=\"A000000003\" Index=\"95\" Data=\"BE9E1FA5E9A803852999C4AB432DB28600DCD9DAB76DFAAA47355A0FE37B1508AC6BF38860D3C6C2E5B12A3CAAF2A7005A7241EBAA7771112C74CF9A0634652FBCA0E5980C54A64761EA101A114E0F0B5572ADD57D010B7C9C887E104CA4EE1272DA66D997B9A90B5A6D624AB6C57E73C8F919000EB5F684898EF8C3DBEFB330C62660BED88EA78E909AFF05F6DA627B\" Exponent=\"000003\" Algorithm=\"01\" Date_Effective=\"010100\" Date_Expiry=\"311215\"/>" +
            "<Key Rid=\"A000000003\" Index=\"99\" Data=\"AB79FCC9520896967E776E64444E5DCDD6E13611874F3985722520425295EEA4BD0C2781DE7F31CD3D041F565F747306EED62954B17EDABA3A6C5B85A1DE1BEB9A34141AF38FCF8279C9DEA0D5A6710D08DB4124F041945587E20359BAB47B7575AD94262D4B25F264AF33DEDCF28E09615E937DE32EDC03C54445FE7E382777\" Exponent=\"000003\" Algorithm=\"01\" Date_Effective=\"010100\" Date_Expiry=\"311215\"/>" +
            "<Key Rid=\"A000000003\" Index=\"92\" Data=\"996AF56F569187D09293C14810450ED8EE3357397B18A2458EFAA92DA3B6DF6514EC060195318FD43BE9B8F0CC669E3F844057CBDDF8BDA191BB64473BC8DC9A730DB8F6B4EDE3924186FFD9B8C7735789C23A36BA0B8AF65372EB57EA5D89E7D14E9C7B6B557460F10885DA16AC923F15AF3758F0F03EBD3C5C2C949CBA306DB44E6A2C076C5F67E281D7EF56785DC4D75945E491F01918800A9E2DC66F60080566CE0DAF8D17EAD46AD8E30A247C9F\" Exponent=\"000003\" Algorithm=\"01\" Date_Effective=\"010100\" Date_Expiry=\"311215\"/>" +
            "<Key Rid=\"A000000003\" Index=\"94\" Data=\"ACD2B12302EE644F3F835ABD1FC7A6F62CCE48FFEC622AA8EF062BEF6FB8BA8BC68BBF6AB5870EED579BC3973E121303D34841A796D6DCBC41DBF9E52C4609795C0CCF7EE86FA1D5CB041071ED2C51D2202F63F1156C58A92D38BC60BDF424E1776E2BC9648078A03B36FB554375FC53D57C73F5160EA59F3AFC5398EC7B67758D65C9BFF7828B6B82D4BE124A416AB7301914311EA462C19F771F31B3B57336000DFF732D3B83DE07052D730354D297BEC72871DCCF0E193F171ABA27EE464C6A97690943D59BDABB2A27EB71CEEBDAFA1176046478FD62FEC452D5CA393296530AA3F41927ADFE434A2DF2AE3054F8840657A26E0FC617\" Exponent=\"000003\" Algorithm=\"01\" Date_Effective=\"010100\" Date_Expiry=\"311215\"/>" +
            "<Key Rid=\"A000000003\" Index=\"96\" Data=\"B74586D19A207BE6627C5B0AAFBC44A2ECF5A2942D3A26CE19C4FFAEEE920521868922E893E7838225A3947A2614796FB2C0628CE8C11E3825A56D3B1BBAEF783A5C6A81F36F8625395126FA983C5216D3166D48ACDE8A431212FF763A7F79D9EDB7FED76B485DE45BEB829A3D4730848A366D3324C3027032FF8D16A1E44D8D\" Exponent=\"000003\" Algorithm=\"01\" Date_Effective=\"010100\" Date_Expiry=\"311214\"/>" +
            "<Key Rid=\"A000000003\" Index=\"51\" Data=\"DB5FA29D1FDA8C1634B04DCCFF148ABEE63C772035C79851D3512107586E02A917F7C7E885E7C4A7D529710A145334CE67DC412CB1597B77AA2543B98D19CF2CB80C522BDBEA0F1B113FA2C86216C8C610A2D58F29CF3355CEB1BD3EF410D1EDD1F7AE0F16897979DE28C6EF293E0A19282BD1D793F1331523FC71A228800468C01A3653D14C6B4851A5C029478E757F\" Exponent=\"000003\" Algorithm=\"01\" Date_Effective=\"010100\" Date_Expiry=\"311215\"/>" +
            "<Key Rid=\"A000000004\" Index=\"F3\" Data=\"98F0C770F23864C2E766DF02D1E833DFF4FFE92D696E1642F0A88C5694C6479D16DB1537BFE29E4FDC6E6E8AFD1B0EB7EA0124723C333179BF19E93F10658B2F776E829E87DAEDA9C94A8B3382199A350C077977C97AFF08FD11310AC950A72C3CA5002EF513FCCC286E646E3C5387535D509514B3B326E1234F9CB48C36DDD44B416D23654034A66F403BA511C5EFA3\" Exponent=\"000003\" Algorithm=\"01\" Date_Effective=\"010100\" Date_Expiry=\"311212\"/>" +
            "<Key Rid=\"A000000004\" Index=\"F8\" Data=\"A1F5E1C9BD8650BD43AB6EE56B891EF7459C0A24FA84F9127D1A6C79D4930F6DB1852E2510F18B61CD354DB83A356BD190B88AB8DF04284D02A4204A7B6CB7C5551977A9B36379CA3DE1A08E69F301C95CC1C20506959275F41723DD5D2925290579E5A95B0DF6323FC8E9273D6F849198C4996209166D9BFC973C361CC826E1\" Exponent=\"000003\" Algorithm=\"01\" Date_Effective=\"010100\" Date_Expiry=\"311212\"/>" +
            "<Key Rid=\"A000000004\" Index=\"FE\" Data=\"A653EAC1C0F786C8724F737F172997D63D1C3251C44402049B865BAE877D0F398CBFBE8A6035E24AFA086BEFDE9351E54B95708EE672F0968BCD50DCE40F783322B2ABA04EF137EF18ABF03C7DBC5813AEAEF3AA7797BA15DF7D5BA1CBAF7FD520B5A482D8D3FEE105077871113E23A49AF3926554A70FE10ED728CF793B62A1\" Exponent=\"000003\" Algorithm=\"01\" Date_Effective=\"010100\" Date_Expiry=\"311215\"/>" +
            "<Key Rid=\"A000000004\" Index=\"F1\" Data=\"A0DCF4BDE19C3546B4B6F0414D174DDE294AABBB828C5A834D73AAE27C99B0B053A90278007239B6459FF0BBCD7B4B9C6C50AC02CE91368DA1BD21AAEADBC65347337D89B68F5C99A09D05BE02DD1F8C5BA20E2F13FB2A27C41D3F85CAD5CF6668E75851EC66EDBF98851FD4E42C44C1D59F5984703B27D5B9F21B8FA0D93279FBBF69E090642909C9EA27F898959541AA6757F5F624104F6E1D3A9532F2A6E51515AEAD1B43B3D7835088A2FAFA7BE7\" Exponent=\"000003\" Algorithm=\"01\" Date_Effective=\"010100\" Date_Expiry=\"311215\"/>" +
            "<Key Rid=\"A000000004\" Index=\"FA\" Data=\"A90FCD55AA2D5D9963E35ED0F440177699832F49C6BAB15CDAE5794BE93F934D4462D5D12762E48C38BA83D8445DEAA74195A301A102B2F114EADA0D180EE5E7A5C73E0C4E11F67A43DDAB5D55683B1474CC0627F44B8D3088A492FFAADAD4F42422D0E7013536C3C49AD3D0FAE96459B0F6B1B6056538A3D6D44640F94467B108867DEC40FAAECD740C00E2B7A8852D\" Exponent=\"000003\" Algorithm=\"01\" Date_Effective=\"010100\" Date_Expiry=\"311215\"/>" +
            "<Key Rid=\"A000000004\" Index=\"EF\" Data=\"A191CB87473F29349B5D60A88B3EAEE0973AA6F1A082F358D849FDDFF9C091F899EDA9792CAF09EF28F5D22404B88A2293EEBBC1949C43BEA4D60CFD879A1539544E09E0F09F60F065B2BF2A13ECC705F3D468B9D33AE77AD9D3F19CA40F23DCF5EB7C04DC8F69EBA565B1EBCB4686CD274785530FF6F6E9EE43AA43FDB02CE00DAEC15C7B8FD6A9B394BABA419D3F6DC85E16569BE8E76989688EFEA2DF22FF7D35C043338DEAA982A02B866DE5328519EBBCD6F03CDD686673847F84DB651AB86C28CF1462562C577B853564A290C8556D818531268D25CC98A4CC6A0BDFFFDA2DCCA3A94C998559E307FDDF915006D9A987B07DDAEB3B\" Exponent=\"000003\" Algorithm=\"01\" Date_Effective=\"010100\" Date_Expiry=\"311215\"/>" +
            "<Key Rid=\"A000000025\" Index=\"03\" Data=\"B0C2C6E2A6386933CD17C239496BF48C57E389164F2A96BFF133439AE8A77B20498BD4DC6959AB0C2D05D0723AF3668901937B674E5A2FA92DDD5E78EA9D75D79620173CC269B35F463B3D4AAFF2794F92E6C7A3FB95325D8AB95960C3066BE548087BCB6CE12688144A8B4A66228AE4659C634C99E36011584C095082A3A3E3\" Exponent=\"000003\" Algorithm=\"01\" Date_Effective=\"010100\" Date_Expiry=\"311220\"/>" +
            "<Key Rid=\"A000000025\" Index=\"0E\" Data=\"AA94A8C6DAD24F9BA56A27C09B01020819568B81A026BE9FD0A3416CA9A71166ED5084ED91CED47DD457DB7E6CBCD53E560BC5DF48ABC380993B6D549F5196CFA77DFB20A0296188E969A2772E8C4141665F8BB2516BA2C7B5FC91F8DA04E8D512EB0F6411516FB86FC021CE7E969DA94D33937909A53A57F907C40C22009DA7532CB3BE509AE173B39AD6A01BA5BB85\" Exponent=\"000003\" Algorithm=\"01\" Date_Effective=\"010100\" Date_Expiry=\"311216\"/>" +
            "<Key Rid=\"A000000025\" Index=\"0F\" Data=\"C8D5AC27A5E1FB89978C7C6479AF993AB3800EB243996FBB2AE26B67B23AC482C4B746005A51AFA7D2D83E894F591A2357B30F85B85627FF15DA12290F70F05766552BA11AD34B7109FA49DE29DCB0109670875A17EA95549E92347B948AA1F045756DE56B707E3863E59A6CBE99C1272EF65FB66CBB4CFF070F36029DD76218B21242645B51CA752AF37E70BE1A84FF31079DC0048E928883EC4FADD497A719385C2BBBEBC5A66AA5E5655D18034EC5\" Exponent=\"000003\" Algorithm=\"01\" Date_Effective=\"010100\" Date_Expiry=\"311217\"/>" +
            "<Key Rid=\"A000000025\" Index=\"10\" Data=\"CF98DFEDB3D3727965EE7797723355E0751C81D2D3DF4D18EBAB9FB9D49F38C8C4A826B99DC9DEA3F01043D4BF22AC3550E2962A59639B1332156422F788B9C16D40135EFD1BA94147750575E636B6EBC618734C91C1D1BF3EDC2A46A43901668E0FFC136774080E888044F6A1E65DC9AAA8928DACBEB0DB55EA3514686C6A732CEF55EE27CF877F110652694A0E3484C855D882AE191674E25C296205BBB599455176FDD7BBC549F27BA5FE35336F7E29E68D783973199436633C67EE5A680F05160ED12D1665EC83D1997F10FD05BBDBF9433E8F797AEE3E9F02A34228ACE927ABE62B8B9281AD08D3DF5C7379685045D7BA5FCDE58637\" Exponent=\"000003\" Algorithm=\"01\" Date_Effective=\"010100\" Date_Expiry=\"311218\"/>" +
            "<Key Rid=\"A000000025\" Index=\"97\" Data=\"E178FFE834B4B767AF3C9A511F973D8E8505C5FCB2D3768075AB7CC946A955789955879AAF737407151521996DFA43C58E6B130EB1D863B85DC9FFB4050947A2676AA6A061A4A7AE1EDB0E36A697E87E037517EB8923136875BA2CA1087CBA7EC7653E5E28A0C261A033AF27E3A67B64BBA26956307EC47E674E3F8B722B3AE0498DB16C7985310D9F3D117300D32B09\" Exponent=\"000003\" Algorithm=\"01\" Date_Effective=\"010100\" Date_Expiry=\"311219\"/>" +
            "<Key Rid=\"A000000025\" Index=\"98\" Data=\"D31A7094FB221CBA6660FB975AAFEA80DB7BB7EAFD7351E748827AB62D4AEECCFC1787FD47A04699A02DB00D7C382E80E804B35C59434C602389D691B9CCD51ED06BE67A276119C4C10E2E40FC4EDDF9DF39B9B0BDEE8D076E2A012E8A292AF8EFE18553470639C1A032252E0E5748B25A3F9BA4CFCEE073038B061837F2AC1B04C279640F5BD110A9DC665ED2FA6828BD5D0FE810A892DEE6B0E74CE8863BDE08FD5FD61A0F11FA0D14978D8CED7DD3\" Exponent=\"000003\" Algorithm=\"01\" Date_Effective=\"010100\" Date_Expiry=\"311218\"/>" +
            "<Key Rid=\"A000000025\" Index=\"99\" Data=\"E1740074229FA0D228A9623581D7A322903FB89BA7686712E601FA8AB24A9789186F15B70CCBBE7421B1CB110D45361688135FFD0DB15A3F516BB291D4A123EBF5A06FBF7E1EE6311B737DABB289570A7959D532B25F1DA6758C84DDCCADC049BC764C05391ABD2CADEFFA7E242D5DD06E56001F0E68151E3388074BD9330D6AFA57CBF33946F531E51E0D4902EE235C756A905FB733940E6EC897B4944A5EDC765705E2ACF76C78EAD78DD9B066DF0B2C88750B8AEE00C9B4D4091FA7338449DA92DBFC908FA0781C0128C492DB993C88BA8BB7CADFE238D477F2517E0E7E3D2B11796A0318CE2AD4DA1DB8E54AB0D94F109DB9CAEEFBEF\" Exponent=\"000003\" Algorithm=\"01\" Date_Effective=\"010100\" Date_Expiry=\"311218\"/>" +
            "<Key Rid=\"A000000025\" Index=\"04\" Data=\"D0F543F03F2517133EF2BA4A1104486758630DCFE3A883C77B4E4844E39A9BD6360D23E6644E1E071F196DDF2E4A68B4A3D93D14268D7240F6A14F0D714C17827D279D192E88931AF7300727AE9DA80A3F0E366AEBA61778171737989E1EE309\" Exponent=\"000003\" Algorithm=\"01\" Date_Effective=\"010100\" Date_Expiry=\"311218\"/>" +
            "<Key Rid=\"A000000025\" Index=\"65\" Data=\"E53EB41F839DDFB474F272CD0CBE373D5468EB3F50F39C95BDF4D39FA82B98DABC9476B6EA350C0DCE1CD92075D8C44D1E57283190F96B3537D9E632C461815EBD2BAF36891DF6BFB1D30FA0B752C43DCA0257D35DFF4CCFC98F84198D5152EC61D7B5F74BD09383BD0E2AA42298FFB02F0D79ADB70D72243EE537F75536A8A8DF962582E9E6812F3A0BE02A4365400D\" Exponent=\"000003\" Algorithm=\"01\" Date_Effective=\"010100\" Date_Expiry=\"311218\"/>" +
            "<Key Rid=\"A000000025\" Index=\"C1\" Data=\"E69E319C34D1B4FB43AED4BD8BBA6F7A8B763F2F6EE5DDF7C92579A984F89C4A9C15B27037764C58AC7E45EFBC34E138E56BA38F76E803129A8DDEB5E1CC8C6B30CF634A9C9C1224BF1F0A9A18D79ED41EBCF1BE78087AE8B7D2F896B1DE8B7E784161A138A0F2169AD33E146D1B16AB595F9D7D98BE671062D217F44EB68C68640C7D57465A063F6BAC776D3E2DAC61\" Exponent=\"000003\" Algorithm=\"01\" Date_Effective=\"010100\" Date_Expiry=\"311217\"/>" +
            "<Key Rid=\"A000000025\" Index=\"C2\" Data=\"B875002F38BA26D61167C5D440367604AD38DF2E93D8EE8DA0E8D9C0CF4CC5788D11DEA689E5F41D23A3DA3E0B1FA5875AE25620F5A6BCCEE098C1B35C691889D7D0EF670EB8312E7123FCC5DC7D2F0719CC80E1A93017F944D097330EDF945762FEE62B7B0BA0348228DBF38D4216E5A67A7EF74F5D3111C44AA31320F623CB3C53E60966D6920067C9E082B746117E48E4F00E110950CA54DA3E38E5453BD5544E3A6760E3A6A42766AD2284E0C9AF\" Exponent=\"000003\" Algorithm=\"01\" Date_Effective=\"010100\" Date_Expiry=\"311220\"/>" +
            "<Key Rid=\"A000000025\" Index=\"C3\" Data=\"B93182ABE343DFBF388C71C4D6747DCDEC60367FE63CFAA942D7D323E688D0832836548BF0EDFF1EDEEB882C75099FF81A93FA525C32425B36023EA02A8899B9BF7D7934E86F997891823006CEAA93091A73C1FDE18ABD4F87A22308640C064C8C027685F1B2DB7B741B67AB0DE05E870481C5F972508C17F57E4F833D63220F6EA2CFBB878728AA5887DE407D10C6B8F58D46779ECEC1E2155487D52C78A5C03897F2BB580E0A2BBDE8EA2E1C18F6AAF3EB3D04C3477DEAB88F150C8810FD1EF8EB0596866336FE2C1FBC6BEC22B4FE5D885647726DB59709A505F75C49E0D8D71BF51E4181212BE2142AB2A1E8C0D3B7136CD7B7708E4D\" Exponent=\"000003\" Algorithm=\"01\" Date_Effective=\"010100\" Date_Expiry=\"311220\"/>" +
            "<Key Rid=\"A000000025\" Index=\"C8\" Data=\"BF0CFCED708FB6B048E3014336EA24AA007D7967B8AA4E613D26D015C4FE7805D9DB131CED0D2A8ED504C3B5CCD48C33199E5A5BF644DA043B54DBF60276F05B1750FAB39098C7511D04BABC649482DDCF7CC42C8C435BAB8DD0EB1A620C31111D1AAAF9AF6571EEBD4CF5A08496D57E7ABDBB5180E0A42DA869AB95FB620EFF2641C3702AF3BE0B0C138EAEF202E21D\" Exponent=\"000003\" Algorithm=\"01\" Date_Effective=\"010100\" Date_Expiry=\"311217\"/>" +
            "<Key Rid=\"A000000025\" Index=\"C9\" Data=\"B362DB5733C15B8797B8ECEE55CB1A371F760E0BEDD3715BB270424FD4EA26062C38C3F4AAA3732A83D36EA8E9602F6683EECC6BAFF63DD2D49014BDE4D6D603CD744206B05B4BAD0C64C63AB3976B5C8CAAF8539549F5921C0B700D5B0F83C4E7E946068BAAAB5463544DB18C63801118F2182EFCC8A1E85E53C2A7AE839A5C6A3CABE73762B70D170AB64AFC6CA482944902611FB0061E09A67ACB77E493D998A0CCF93D81A4F6C0DC6B7DF22E62DB\" Exponent=\"000003\" Algorithm=\"01\" Date_Effective=\"010100\" Date_Expiry=\"311220\"/>" +
            "<Key Rid=\"A000000025\" Index=\"CA\" Data=\"C23ECBD7119F479C2EE546C123A585D697A7D10B55C2D28BEF0D299C01DC65420A03FE5227ECDECB8025FBC86EEBC1935298C1753AB849936749719591758C315FA150400789BB14FADD6EAE2AD617DA38163199D1BAD5D3F8F6A7A20AEF420ADFE2404D30B219359C6A4952565CCCA6F11EC5BE564B49B0EA5BF5B3DC8C5C6401208D0029C3957A8C5922CBDE39D3A564C6DEBB6BD2AEF91FC27BB3D3892BEB9646DCE2E1EF8581EFFA712158AAEC541C0BBB4B3E279D7DA54E45A0ACC3570E712C9F7CDF985CFAFD382AE13A3B214A9E8E1E71AB1EA707895112ABC3A97D0FCB0AE2EE5C85492B6CFD54885CDD6337E895CC70FB3255E3\" Exponent=\"000003\" Algorithm=\"01\" Date_Effective=\"010100\" Date_Expiry=\"311220\"/>" +
            "<Key Rid=\"A000000384\" Index=\"C1\" Data=\"AD6E00E5882CEEF578AFB002980FBD901089100B921200D6442C176D93DA5C9399B8427CBE19C1B2E638F5FC78875C82BE7CE590160D0E8A04242374E5C4B5F307E7412CA8FB2E84BB4F421D6B4C2E08255B2577F55E0667673BD7D7A3D74E083DC19B597A76531135A7C3B1DB93534045E1D52DDB5170ACFAA688922C18764CFE59E3D0578C41A7BD60520CADC58DD9\" Exponent=\"000003\" Algorithm=\"01\" Date_Effective=\"010100\" Date_Expiry=\"311215\"/>" +
            "<Key Rid=\"A000000384\" Index=\"C2\" Data=\"8BFD70FC05456718B7002F1B889040780089D95969F3160A3999B4E056D2CEFC007D3033B02868670C9EFE48624096C2FD0FA7FDAFDCBB4A63A4CD7106A97BDD8CE3F71F2168AFCB4230F2C33492467A6C182B4BFA76EE605B2FEC4B4519B5A92767DF23805EC8708980E18CB089C065D036AD57D196E88AC1552148FC3B62B771B6B144D28DF5AEE74FC31521B6968909A463EA0184261DD751278A10B7C74668520B253B6A860E54065D2B5677A753\" Exponent=\"000003\" Algorithm=\"01\" Date_Effective=\"010100\" Date_Expiry=\"311215\"/>" +
            "<Key Rid=\"A000000384\" Index=\"C3\" Data=\"A1A62B6A0393DE4EF4D9D53684C14EAED4A881A26EE64DBD4550EB3581E003281A4117F7BF9E5648FD5E3882BC4AB46B0A5F48CDF5C2D56C7D14A4CD69D337800050C0994974237628EF8D751E1FABBAAD73ED70F60486930B55A5F3C4AEBB2BB1ADAAAE3A374D02F19420BD528AB19C9D09FC41BB8140C40443AE2C5A24593C216E9604BE0DC69BBB1D1F98CA76212D5D1B59DBB90739145C5F98C7DEFECBE91DA4C59FB40F159A260A0B058861A25D0B88D0B3FFD87CEE52DD28C7B1FFE50A49B66116615DC45696B4A61E563B3CDD36E3F2ADCFFB50DBC3E8280412769D00662917096893B1C319E8133A043DDF05AE82A55FAEFEC1DF\" Exponent=\"000003\" Algorithm=\"01\" Date_Effective=\"010100\" Date_Expiry=\"311215\"/>" +
            "<Key Rid=\"A000000065\" Index=\"05\" Data=\"9C338A8B66142367883B08A7F8D39459CBB00182ECF3722158992C99D69CBD94137EF95BE72E1FDA9E5800242084A5765FE3B00B730E726ADA9A7E219CAEBDC329158D2F88AF1529F3E1D0093A481185F1D1058034B78FE323BD281B02C15F59303CF8E50E7EA3A76DA2E104E363F18EEFFF9802603FD07BB8586783BC209CCC1354BD7EFF94E388682EEF11E7C9A69CFF5747F096557871\" Exponent=\"000003\" Algorithm=\"01\" Date_Effective=\"010100\" Date_Expiry=\"311220\"/>" +
            "<Key Rid=\"A000000065\" Index=\"06\" Data=\"932720B5CEC708D2E3707344E1D0B341782716124EA1E9913BEC427229A4EC195B44C9BE1B63C072E01ECC30E731DA128C94F0011E004AC950A5D810D89156B755F47F2111FB01897A546AE9355310A166B0FAABDA2121E065FFB94124A2E65245B71D8E2AA373654864DD95DBFB4B62C2197E5BC35AEA20DFCBD24721FF4CFDDB1D49B643A631EEF11E7B867266009E4C2D272BB812D0F46C428E210E4A469580CEB666DCD71DB98849\" Exponent=\"000003\" Algorithm=\"01\" Date_Effective=\"010100\" Date_Expiry=\"311220\"/>" +
            "<Key Rid=\"A000000065\" Index=\"07\" Data=\"A1081CFA8A068BAFF78BFCBBA8AFE27933670AF925370CAE31939A42976C543910AD706E2DFEBC19C94FFF9B7DD0533548E2757BE64B0D7F6390F854F48F2D99A01136887AD56A139F762D26A1657F7404FC299B6ACB2331D2B5283AB0D385CFD2FFF35AA90FE07AC506B13A66FBD72E1B6FD9D792C13F8F6D5F4C4EDD96ACB9\" Exponent=\"000003\" Algorithm=\"01\" Date_Effective=\"010100\" Date_Expiry=\"311220\"/>" +
            "<Key Rid=\"A000000065\" Index=\"08\" Data=\"9A20815A3729C2FB032C1A717866EA252E27D7DE539A8DDEA04BD5B3FBE7429C81491E9C16651A1F2D00FD4E62D3D450811F1150ED5C02A741C41458AC80369A8ADFC493898036C9D9B4B62CE91169BE030475A2AB01B9F86E3733E824C41AF02AC9D341E8BEE7E6C7AA80D7B1CA971FAF5BDAF17BB7904F53271605E325EFEF\" Exponent=\"000003\" Algorithm=\"01\" Date_Effective=\"010100\" Date_Expiry=\"311220\"/>" +
            "<Key Rid=\"A000000065\" Index=\"09\" Data=\"A0BA1E941BA2B11DFB9AC5139041CC58B870A3B328F4712DD844439E6544469FD31106167FE926583CBCED6D573DECF9AF67D09875AF285C189681D4045883031E99A0A0F456DD31857DC58960EC24689F68FECEF88832B389D66D2A0481B14B0E05FD36CC00163FCAABAE73B5273D5F1206D4E246DC8AA1977A685FDD344B0D\" Exponent=\"000003\" Algorithm=\"01\" Date_Effective=\"010100\" Date_Expiry=\"311220\"/>" +
            "<Key Rid=\"A000000065\" Index=\"10\" Data=\"98B446D87B5BDA8E13104F71687A5EE2E8106375CC64A54A10B88190C27B3812DB87B0D70FB4E9871DC62CEBC38056DA3E6B3957C8A8067DA81CE7C46A42107817D263933F3188BBB7863B2BC41683CE9C1744C5C06EC0D127A8844AA78F1D2E1E1115B036B7B9148BC9DBF58005313D14C866590C58DFE5D1AE6EBB118744AA2067926498C3CE323BDF5D67981E7FBF\" Exponent=\"000003\" Algorithm=\"01\" Date_Effective=\"010100\" Date_Expiry=\"311220\"/>" +
            "<Key Rid=\"A000000065\" Index=\"11\" Data=\"A2583AA40746E3A63C22478F576D1EFC5FB046135A6FC739E82B55035F71B09BEB566EDB9968DD649B94B6DEDC033899884E908C27BE1CD291E5436F762553297763DAA3B890D778C0F01E3344CECDFB3BA70D7E055B8C760D0179A403D6B55F2B3B083912B183ADB7927441BED3395A199EEFE0DEBD1F5FC3264033DA856F4A8B93916885BD42F9C1F456AAB8CFA83AC574833EB5E87BB9D4C006A4B5346BD9E17E139AB6552D9C58BC041195336485\" Exponent=\"000003\" Algorithm=\"01\" Date_Effective=\"010100\" Date_Expiry=\"311220\"/>" +
            "<Key Rid=\"A000000065\" Index=\"0F\" Data=\"9EFBADDE4071D4EF98C969EB32AF854864602E515D6501FDE576B310964A4F7C2CE842ABEFAFC5DC9E26A619BCF2614FE07375B9249BEFA09CFEE70232E75FFD647571280C76FFCA87511AD255B98A6B577591AF01D003BD6BF7E1FCE4DFD20D0D0297ED5ECA25DE261F37EFE9E175FB5F12D2503D8CFB060A63138511FE0E125CF3A643AFD7D66DCF9682BD246DDEA1\" Exponent=\"000003\" Algorithm=\"01\" Date_Effective=\"010100\" Date_Expiry=\"311220\"/>" +
            "<Key Rid=\"A000000065\" Index=\"13\" Data=\"6C1441D28889A5F46413C8F62F3645AAEB30A1521EEF41FD4F3445BFA1AB29F9AC1A74D9A16B93293296CB09162B149BAC22F88AD8F322D684D6B49A12413FC1B6AC70EDEDB18EC1585519A89B50B3D03E14063C2CA58B7C2BA7FB22799A33BCDE6AFCBEB4A7D64911D08D18C47F9BD14A9FAD8805A15DE5A38945A97919B7AB88EFA11A88C0CD92C6EE7DC352AB0746ABF13585913C8A4E04464B77909C6BD94341A8976C4769EA6C0D30A60F4EE8FA19E767B170DF4FA80312DBA61DB645D5D1560873E2674E1F620083F30180BD96CA5890\" Exponent=\"000003\" Algorithm=\"01\" Date_Effective=\"010100\" Date_Expiry=\"311220\"/>" +
            "<Key Rid=\"A000000333\" Index=\"0A\" Data=\"B2AB1B6E9AC55A75ADFD5BBC34490E53C4C3381F34E60E7FAC21CC2B26DD34462B64A6FAE2495ED1DD383B8138BEA100FF9B7A111817E7B9869A9742B19E5C9DAC56F8B8827F11B05A08ECCF9E8D5E85B0F7CFA644EFF3E9B796688F38E006DEB21E101C01028903A06023AC5AAB8635F8E307A53AC742BDCE6A283F585F48EF\" Exponent=\"000003\" Algorithm=\"01\" Date_Effective=\"010100\" Date_Expiry=\"311299\"/>" +
            "<Key Rid=\"A000000333\" Index=\"08\" Data=\"B61645EDFD5498FB246444037A0FA18C0F101EBD8EFA54573CE6E6A7FBF63ED21D66340852B0211CF5EEF6A1CD989F66AF21A8EB19DBD8DBC3706D135363A0D683D046304F5A836BC1BC632821AFE7A2F75DA3C50AC74C545A754562204137169663CFCC0B06E67E2109EBA41BC67FF20CC8AC80D7B6EE1A95465B3B2657533EA56D92D539E5064360EA4850FED2D1BF\" Exponent=\"000003\" Algorithm=\"01\" Date_Effective=\"010100\" Date_Expiry=\"311299\"/>" +
            "<Key Rid=\"A000000333\" Index=\"09\" Data=\"EB374DFC5A96B71D2863875EDA2EAFB96B1B439D3ECE0B1826A2672EEEFA7990286776F8BD989A15141A75C384DFC14FEF9243AAB32707659BE9E4797A247C2F0B6D99372F384AF62FE23BC54BCDC57A9ACD1D5585C303F201EF4E8B806AFB809DB1A3DB1CD112AC884F164A67B99C7D6E5A8A6DF1D3CAE6D7ED3D5BE725B2DE4ADE23FA679BF4EB15A93D8A6E29C7FFA1A70DE2E54F593D908A3BF9EBBD760BBFDC8DB8B54497E6C5BE0E4A4DAC29E5\" Exponent=\"000003\" Algorithm=\"01\" Date_Effective=\"010100\" Date_Expiry=\"311299\"/>" +
            "<Key Rid=\"A000000333\" Index=\"0B\" Data=\"CF9FDF46B356378E9AF311B0F981B21A1F22F250FB11F55C958709E3C7241918293483289EAE688A094C02C344E2999F315A72841F489E24B1BA0056CFAB3B479D0E826452375DCDBB67E97EC2AA66F4601D774FEAEF775ACCC621BFEB65FB0053FC5F392AA5E1D4C41A4DE9FFDFDF1327C4BB874F1F63A599EE3902FE95E729FD78D4234DC7E6CF1ABABAA3F6DB29B7F05D1D901D2E76A606A8CBFFFFECBD918FA2D278BDB43B0434F5D45134BE1C2781D157D501FF43E5F1C470967CD57CE53B64D82974C8275937C5D8502A1252A8A5D6088A259B694F98648D9AF2CB0EFD9D943C69F896D49FA39702162ACB5AF29B90BADE005BC157\" Exponent=\"000003\" Algorithm=\"01\" Date_Effective=\"010100\" Date_Expiry=\"311299\"/>" +
            "</PktParameterTable>";

    String testData =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                    "<EmvParameterTable Tlv_Tag_Emv_Table_Version=\"100198\">\n" +
                    "  <Tlv_Tag_Emv_Default_Param>\n" +
                    "    <Tlv_Tag_Emv_Def_Tags Tag_Emv_Amount_Authorised_Binary=\"00000000\" Tag_Emv_Amount_Other_Binary=\"00000000\" Tag_Emv_Terminal_Country_Code=\"36\" Tag_Emv_Terminal_Capabilities=\"E0F8C8\" Tag_Emv_Terminal_Type=\"22\" Tag_Emv_Transaction_Reference_Currency_Code=\"36\" Tag_Emv_Transaction_Reference_Currency_Exponent=\"2\" Tag_Emv_Additional_Terminal_Capabilities=\"FF80F0A001\" Tag_Emv_Merchant_Category_Code=\"5411\" />\n" +
                    "  </Tlv_Tag_Emv_Default_Param>\n" +
                    "  <Tlv_Tag_Emv_Aid_Param Tlv_Tag_Emv_Aid=\"A0000000031010\" Tlv_Tag_Emv_Aid_Priority=\"1\" Tlv_Tag_Emv_Aid_App_Sel_Indic=\"1\" Tlv_Tag_Emv_Display_Text=\"Visa\">\n" +
                    "    <Tlv_Tag_Emv_Aid_Tags Tlv_Tag_Emv_Aid_Card_Origin=\"4\">\n" +
                    "      <Tlv_Tag_Emv_Aid_Txn_Tags Tag_Emv_Term_Act_Code_Default=\"DC4000A800\" Tag_Emv_Term_Act_Code_Denial=\"0010000000\" Tag_Emv_Term_Act_Code_Online=\"DC4004F800\" Tag_Emv_Threshold=\"00000000\" Tag_Emv_Target_Percent=\"0\" Tag_Emv_Max_Target_Percent=\"0\" Tag_Emv_Ddol_Default=\"9F3704\" Tag_Emv_Tdol_Default=\"9F02065F2A029A039C0195059F3704\" Tag_Emv_Transaction_Type=\"0\" Tag_Emv_Application_Version_Number_Term=\"008C\" Tag_Emv_Terminal_Floor_Limit=\"00000000\" Tag_Emv_Mc_Transaction_Category_Code=\"82\" Tag_Emv_Standin_Act_Code=\"FC70200000\" Tag_Emv_Efb_Floor_Limit=\"0000005A\" Tag_Emv_Pin_Bypass=\"0\" Tag_Emv_Small_Value_Enabled=\"01\" Tag_Emv_Small_Value_Terminal_Capabilities=\"E008C8\" Tag_Emv_Terminal_Capabilities_Code=\"E0F8C8\" Tag_Emv_Additional_Terminal_Capabilities=\"FF80F0A001\" Tag_Emv_Terminal_Transaction_Qualifiers=\"00000000\"/>\n" +
                    "    </Tlv_Tag_Emv_Aid_Tags>\n" +
                    "    <Tlv_Tag_Emv_Aid_Tags Tlv_Tag_Emv_Aid_Card_Origin=\"0\">\n" +
                    "      <Tlv_Tag_Emv_Aid_Txn_Tags Tag_Emv_Term_Act_Code_Default=\"DC4000A800\" Tag_Emv_Term_Act_Code_Denial=\"0010000000\" Tag_Emv_Term_Act_Code_Online=\"DC4004F800\" Tag_Emv_Threshold=\"00000000\" Tag_Emv_Target_Percent=\"0\" Tag_Emv_Max_Target_Percent=\"0\" Tag_Emv_Ddol_Default=\"9F3704\" Tag_Emv_Tdol_Default=\"9F02065F2A029A039C0195059F3704\" Tag_Emv_Transaction_Type=\"0\" Tag_Emv_Application_Version_Number_Term=\"008C\" Tag_Emv_Terminal_Floor_Limit=\"00000000\" Tag_Emv_Mc_Transaction_Category_Code=\"82\" Tag_Emv_Standin_Act_Code=\"FC70200000\" Tag_Emv_Efb_Floor_Limit=\"0000005A\" Tag_Emv_Pin_Bypass=\"1\" Tag_Emv_Small_Value_Enabled=\"01\" Tag_Emv_Small_Value_Terminal_Capabilities=\"E008C8\" Tag_Emv_Terminal_Capabilities_Code=\"E0F8C8\" Tag_Emv_Additional_Terminal_Capabilities=\"FF80F0A001\" Tag_Emv_Terminal_Transaction_Qualifiers=\"00000000\"/>\n" +
                    "    </Tlv_Tag_Emv_Aid_Tags>\n" +
                    "  </Tlv_Tag_Emv_Aid_Param>\n" +
                    "  <Tlv_Tag_Emv_Aid_Param Tlv_Tag_Emv_Aid=\"A0000000041010\" Tlv_Tag_Emv_Aid_Priority=\"1\" Tlv_Tag_Emv_Aid_App_Sel_Indic=\"1\" Tlv_Tag_Emv_Display_Text=\"MasterCard\">\n" +
                    "    <Tlv_Tag_Emv_Aid_Tags Tlv_Tag_Emv_Aid_Card_Origin=\"4\">\n" +
                    "      <Tlv_Tag_Emv_Aid_Txn_Tags Tag_Emv_Term_Act_Code_Default=\"FE50BCA000\" Tag_Emv_Term_Act_Code_Denial=\"0000000000\" Tag_Emv_Term_Act_Code_Online=\"FE50BCF800\" Tag_Emv_Threshold=\"00000000\" Tag_Emv_Target_Percent=\"0\" Tag_Emv_Max_Target_Percent=\"0\" Tag_Emv_Ddol_Default=\"9F3704\" Tag_Emv_Tdol_Default=\"9F02065F2A029A039C0195059F3704\" Tag_Emv_Transaction_Type=\"0\" Tag_Emv_Application_Version_Number_Term=\"0002\" Tag_Emv_Terminal_Floor_Limit=\"00000005\" Tag_Emv_Mc_Transaction_Category_Code=\"82\" Tag_Emv_Standin_Act_Code=\"FC70200000\" Tag_Emv_Efb_Floor_Limit=\"0000005A\" Tag_Emv_Pin_Bypass=\"0\" Tag_Emv_Small_Value_Enabled=\"01\" Tag_Emv_Small_Value_Terminal_Capabilities=\"E008C8\" Tag_Emv_Terminal_Capabilities_Code=\"E0F8C8\" Tag_Emv_Additional_Terminal_Capabilities=\"FF80F0A001\" Tag_Emv_Terminal_Transaction_Qualifiers=\"00000000\"/>\n" +
                    "    </Tlv_Tag_Emv_Aid_Tags>\n" +
                    "    <Tlv_Tag_Emv_Aid_Tags Tlv_Tag_Emv_Aid_Card_Origin=\"0\">\n" +
                    "      <Tlv_Tag_Emv_Aid_Txn_Tags Tag_Emv_Term_Act_Code_Default=\"FE50BCA000\" Tag_Emv_Term_Act_Code_Denial=\"0000000000\" Tag_Emv_Term_Act_Code_Online=\"FE50BCF800\" Tag_Emv_Threshold=\"00000000\" Tag_Emv_Target_Percent=\"0\" Tag_Emv_Max_Target_Percent=\"0\" Tag_Emv_Ddol_Default=\"9F3704\" Tag_Emv_Tdol_Default=\"9F02065F2A029A039C0195059F3704\" Tag_Emv_Transaction_Type=\"0\" Tag_Emv_Application_Version_Number_Term=\"0002\" Tag_Emv_Terminal_Floor_Limit=\"00000000\" Tag_Emv_Mc_Transaction_Category_Code=\"82\" Tag_Emv_Standin_Act_Code=\"FC70200000\" Tag_Emv_Efb_Floor_Limit=\"0000005A\" Tag_Emv_Pin_Bypass=\"1\" Tag_Emv_Small_Value_Enabled=\"01\" Tag_Emv_Small_Value_Terminal_Capabilities=\"E008C8\" Tag_Emv_Terminal_Capabilities_Code=\"E0F8C8\" Tag_Emv_Additional_Terminal_Capabilities=\"FF80F0A001\" Tag_Emv_Terminal_Transaction_Qualifiers=\"00000000\"/>\n" +
                    "    </Tlv_Tag_Emv_Aid_Tags>\n" +
                    "  </Tlv_Tag_Emv_Aid_Param>\n" +
                    "  <Tlv_Tag_Emv_Aid_Param Tlv_Tag_Emv_Aid=\"A00000002501\" Tlv_Tag_Emv_Aid_Priority=\"1\" Tlv_Tag_Emv_Aid_App_Sel_Indic=\"1\" Tlv_Tag_Emv_Display_Text=\"Amex\">\n" +
                    "    <Tlv_Tag_Emv_Aid_Tags Tlv_Tag_Emv_Aid_Card_Origin=\"4\">\n" +
                    "      <Tlv_Tag_Emv_Aid_Txn_Tags Tag_Emv_Term_Act_Code_Default=\"0000000000\" Tag_Emv_Term_Act_Code_Denial=\"0000000000\" Tag_Emv_Term_Act_Code_Online=\"0000000000\" Tag_Emv_Threshold=\"00000125\" Tag_Emv_Target_Percent=\"10\" Tag_Emv_Max_Target_Percent=\"25\" Tag_Emv_Ddol_Default=\"9F3704\" Tag_Emv_Tdol_Default=\"9F02065F2A029A039C0195059F3704\" Tag_Emv_Transaction_Type=\"0\" Tag_Emv_Application_Version_Number_Term=\"0001\" Tag_Emv_Terminal_Floor_Limit=\"00000000\" Tag_Emv_Mc_Transaction_Category_Code=\"82\" Tag_Emv_Standin_Act_Code=\"0000000000\" Tag_Emv_Efb_Floor_Limit=\"00000000\" Tag_Emv_Pin_Bypass=\"0\" Tag_Emv_Small_Value_Enabled=\"00\" Tag_Emv_Small_Value_Terminal_Capabilities=\"000000\" Tag_Emv_Terminal_Capabilities_Code=\"E0F8C8\" Tag_Emv_Additional_Terminal_Capabilities=\"FF80F0A001\" Tag_Emv_Terminal_Transaction_Qualifiers=\"00000000\"/>\n" +
                    "    </Tlv_Tag_Emv_Aid_Tags>\n" +
                    "    <Tlv_Tag_Emv_Aid_Tags Tlv_Tag_Emv_Aid_Card_Origin=\"0\">\n" +
                    "      <Tlv_Tag_Emv_Aid_Txn_Tags Tag_Emv_Term_Act_Code_Default=\"0000000000\" Tag_Emv_Term_Act_Code_Denial=\"0000000000\" Tag_Emv_Term_Act_Code_Online=\"0000000000\" Tag_Emv_Threshold=\"00000125\" Tag_Emv_Target_Percent=\"10\" Tag_Emv_Max_Target_Percent=\"25\" Tag_Emv_Ddol_Default=\"9F3704\" Tag_Emv_Tdol_Default=\"9F02065F2A029A039C0195059F3704\" Tag_Emv_Transaction_Type=\"0\" Tag_Emv_Application_Version_Number_Term=\"0001\" Tag_Emv_Terminal_Floor_Limit=\"00000032\" Tag_Emv_Mc_Transaction_Category_Code=\"82\" Tag_Emv_Standin_Act_Code=\"0000000000\" Tag_Emv_Efb_Floor_Limit=\"00000032\" Tag_Emv_Pin_Bypass=\"1\" Tag_Emv_Small_Value_Enabled=\"01\" Tag_Emv_Small_Value_Terminal_Capabilities=\"E008C8\" Tag_Emv_Terminal_Capabilities_Code=\"E0F8C8\" Tag_Emv_Additional_Terminal_Capabilities=\"FF80F0A001\" Tag_Emv_Terminal_Transaction_Qualifiers=\"00000000\"/>\n" +
                    "    </Tlv_Tag_Emv_Aid_Tags>\n" +
                    "  </Tlv_Tag_Emv_Aid_Param>\n" +
                    "  <Tlv_Tag_Emv_Aid_Param Tlv_Tag_Emv_Aid=\"A00000038410\" Tlv_Tag_Emv_Aid_Priority=\"1\" Tlv_Tag_Emv_Aid_App_Sel_Indic=\"1\" Tlv_Tag_Emv_Display_Text=\"eftpos sav\">\n" +
                    "    <Tlv_Tag_Emv_Aid_Tags Tlv_Tag_Emv_Aid_Card_Origin=\"4\">\n" +
                    "      <Tlv_Tag_Emv_Aid_Txn_Tags Tag_Emv_Term_Act_Code_Default=\"FC50988800\" Tag_Emv_Term_Act_Code_Denial=\"0600000000\" Tag_Emv_Term_Act_Code_Online=\"FC70BC9800\" Tag_Emv_Threshold=\"00000000\" Tag_Emv_Target_Percent=\"0\" Tag_Emv_Max_Target_Percent=\"0\" Tag_Emv_Ddol_Default=\"9F3704\" Tag_Emv_Tdol_Default=\"9F02065F2A029A039C0195059F3704\" Tag_Emv_Transaction_Type=\"9\" Tag_Emv_Application_Version_Number_Term=\"0100\" Tag_Emv_Terminal_Floor_Limit=\"00000005\" Tag_Emv_Mc_Transaction_Category_Code=\"82\" Tag_Emv_Standin_Act_Code=\"0000000000\" Tag_Emv_Efb_Floor_Limit=\"00000000\" Tag_Emv_Pin_Bypass=\"0\" Tag_Emv_Small_Value_Enabled=\"00\" Tag_Emv_Small_Value_Terminal_Capabilities=\"000000\" Tag_Emv_Terminal_Capabilities_Code=\"E0F8C8\" Tag_Emv_Additional_Terminal_Capabilities=\"FF80F0A001\" Tag_Emv_Terminal_Transaction_Qualifiers=\"00000000\"/>\n" +
                    "    </Tlv_Tag_Emv_Aid_Tags>\n" +
                    "  </Tlv_Tag_Emv_Aid_Param>\n" +
                    "  <Tlv_Tag_Emv_Aid_Param Tlv_Tag_Emv_Aid=\"A00000038420\" Tlv_Tag_Emv_Aid_Priority=\"1\" Tlv_Tag_Emv_Aid_App_Sel_Indic=\"1\" Tlv_Tag_Emv_Display_Text=\"eftpos chq\">\n" +
                    "    <Tlv_Tag_Emv_Aid_Tags Tlv_Tag_Emv_Aid_Card_Origin=\"4\">\n" +
                    "      <Tlv_Tag_Emv_Aid_Txn_Tags Tag_Emv_Term_Act_Code_Default=\"FC50988800\" Tag_Emv_Term_Act_Code_Denial=\"0600000000\" Tag_Emv_Term_Act_Code_Online=\"FC70BC9800\" Tag_Emv_Threshold=\"00000000\" Tag_Emv_Target_Percent=\"0\" Tag_Emv_Max_Target_Percent=\"0\" Tag_Emv_Ddol_Default=\"9F3704\" Tag_Emv_Tdol_Default=\"9F02065F2A029A039C0195059F3704\" Tag_Emv_Transaction_Type=\"9\" Tag_Emv_Application_Version_Number_Term=\"0100\" Tag_Emv_Terminal_Floor_Limit=\"00000005\" Tag_Emv_Mc_Transaction_Category_Code=\"82\" Tag_Emv_Standin_Act_Code=\"0000000000\" Tag_Emv_Efb_Floor_Limit=\"00000000\" Tag_Emv_Pin_Bypass=\"0\" Tag_Emv_Small_Value_Enabled=\"00\" Tag_Emv_Small_Value_Terminal_Capabilities=\"000000\" Tag_Emv_Terminal_Capabilities_Code=\"E0F8C8\" Tag_Emv_Additional_Terminal_Capabilities=\"FF80F0A001\" Tag_Emv_Terminal_Transaction_Qualifiers=\"00000000\"/>\n" +
                    "    </Tlv_Tag_Emv_Aid_Tags>\n" +
                    "  </Tlv_Tag_Emv_Aid_Param>\n" +
                    "  <Tlv_Tag_Emv_Aid_Param Tlv_Tag_Emv_Aid=\"A0000000651010\" Tlv_Tag_Emv_Aid_Priority=\"1\" Tlv_Tag_Emv_Aid_App_Sel_Indic=\"1\" Tlv_Tag_Emv_Display_Text=\"JCB\">\n" +
                    "    <Tlv_Tag_Emv_Aid_Tags Tlv_Tag_Emv_Aid_Card_Origin=\"0\">\n" +
                    "      <Tlv_Tag_Emv_Aid_Txn_Tags Tag_Emv_Term_Act_Code_Default=\"F860242800\" Tag_Emv_Term_Act_Code_Denial=\"0010000000\" Tag_Emv_Term_Act_Code_Online=\"F860ACF800\" Tag_Emv_Threshold=\"00000000\" Tag_Emv_Target_Percent=\"0\" Tag_Emv_Max_Target_Percent=\"0\" Tag_Emv_Ddol_Default=\"9F3704\" Tag_Emv_Tdol_Default=\"9F02065F2A029A039C0195059F3704\" Tag_Emv_Transaction_Type=\"0\" Tag_Emv_Application_Version_Number_Term=\"0200\" Tag_Emv_Terminal_Floor_Limit=\"00000032\" Tag_Emv_Mc_Transaction_Category_Code=\"82\" Tag_Emv_Standin_Act_Code=\"0000000000\" Tag_Emv_Efb_Floor_Limit=\"00000000\" Tag_Emv_Pin_Bypass=\"1\" Tag_Emv_Small_Value_Enabled=\"00\" Tag_Emv_Small_Value_Terminal_Capabilities=\"000000\" Tag_Emv_Terminal_Capabilities_Code=\"E0F8C8\" Tag_Emv_Additional_Terminal_Capabilities=\"FF80F0A001\" Tag_Emv_Terminal_Transaction_Qualifiers=\"00000000\"/>\n" +
                    "    </Tlv_Tag_Emv_Aid_Tags>\n" +
                    "  </Tlv_Tag_Emv_Aid_Param>\n" +
                    "  <Tlv_Tag_Emv_Aid_Param Tlv_Tag_Emv_Aid=\"A000000333010102\" Tlv_Tag_Emv_Aid_Priority=\"1\" Tlv_Tag_Emv_Aid_App_Sel_Indic=\"1\" Tlv_Tag_Emv_Display_Text=\"UPI Credit\">\n" +
                    "    <Tlv_Tag_Emv_Aid_Tags Tlv_Tag_Emv_Aid_Card_Origin=\"4\">\n" +
                    "      <Tlv_Tag_Emv_Aid_Txn_Tags Tag_Emv_Term_Act_Code_Default=\"D84000A800\" Tag_Emv_Term_Act_Code_Denial=\"0010000000\" Tag_Emv_Term_Act_Code_Online=\"D84004F800\" Tag_Emv_Threshold=\"00000000\" Tag_Emv_Target_Percent=\"99\" Tag_Emv_Max_Target_Percent=\"99\" Tag_Emv_Ddol_Default=\"9F3704\" Tag_Emv_Tdol_Default=\"00\" Tag_Emv_Transaction_Type=\"0\" Tag_Emv_Application_Version_Number_Term=\"0020\" Tag_Emv_Terminal_Floor_Limit=\"00000000\" Tag_Emv_Mc_Transaction_Category_Code=\"82\" Tag_Emv_Standin_Act_Code=\"0000000000\" Tag_Emv_Efb_Floor_Limit=\"00000000\" Tag_Emv_Pin_Bypass=\"1\" Tag_Emv_Small_Value_Enabled=\"00\" Tag_Emv_Small_Value_Terminal_Capabilities=\"000000\" Tag_Emv_Terminal_Capabilities_Code=\"E0F8C8\" Tag_Emv_Additional_Terminal_Capabilities=\"FF80F0A001\" Tag_Emv_Terminal_Transaction_Qualifiers=\"00000000\"/>\n" +
                    "    </Tlv_Tag_Emv_Aid_Tags>\n" +
                    "    <Tlv_Tag_Emv_Aid_Tags Tlv_Tag_Emv_Aid_Card_Origin=\"0\">\n" +
                    "      <Tlv_Tag_Emv_Aid_Txn_Tags Tag_Emv_Term_Act_Code_Default=\"D84000A800\" Tag_Emv_Term_Act_Code_Denial=\"0010000000\" Tag_Emv_Term_Act_Code_Online=\"D84004F800\" Tag_Emv_Threshold=\"00000000\" Tag_Emv_Target_Percent=\"99\" Tag_Emv_Max_Target_Percent=\"99\" Tag_Emv_Ddol_Default=\"9F3704\" Tag_Emv_Tdol_Default=\"00\" Tag_Emv_Transaction_Type=\"0\" Tag_Emv_Application_Version_Number_Term=\"0020\" Tag_Emv_Terminal_Floor_Limit=\"00000000\" Tag_Emv_Mc_Transaction_Category_Code=\"82\" Tag_Emv_Standin_Act_Code=\"0000000000\" Tag_Emv_Efb_Floor_Limit=\"00000000\" Tag_Emv_Pin_Bypass=\"1\" Tag_Emv_Small_Value_Enabled=\"00\" Tag_Emv_Small_Value_Terminal_Capabilities=\"000000\" Tag_Emv_Terminal_Capabilities_Code=\"E0F8C8\" Tag_Emv_Additional_Terminal_Capabilities=\"FF80F0A001\" Tag_Emv_Terminal_Transaction_Qualifiers=\"00000000\"/>\n" +
                    "    </Tlv_Tag_Emv_Aid_Tags>\n" +
                    "  </Tlv_Tag_Emv_Aid_Param>\n" +
                    "  <Tlv_Tag_Emv_Aid_Param Tlv_Tag_Emv_Aid=\"A000000333010103\" Tlv_Tag_Emv_Aid_Priority=\"1\" Tlv_Tag_Emv_Aid_App_Sel_Indic=\"1\" Tlv_Tag_Emv_Display_Text=\"UPI Quasi cr\">\n" +
                    "    <Tlv_Tag_Emv_Aid_Tags Tlv_Tag_Emv_Aid_Card_Origin=\"0\">\n" +
                    "      <Tlv_Tag_Emv_Aid_Txn_Tags Tag_Emv_Term_Act_Code_Default=\"D84000A800\" Tag_Emv_Term_Act_Code_Denial=\"0010000000\" Tag_Emv_Term_Act_Code_Online=\"D84004F800\" Tag_Emv_Threshold=\"00000000\" Tag_Emv_Target_Percent=\"99\" Tag_Emv_Max_Target_Percent=\"99\" Tag_Emv_Ddol_Default=\"9F3704\" Tag_Emv_Tdol_Default=\"00\" Tag_Emv_Transaction_Type=\"0\" Tag_Emv_Application_Version_Number_Term=\"0020\" Tag_Emv_Terminal_Floor_Limit=\"00000000\" Tag_Emv_Mc_Transaction_Category_Code=\"82\" Tag_Emv_Standin_Act_Code=\"0000000000\" Tag_Emv_Efb_Floor_Limit=\"00000000\" Tag_Emv_Pin_Bypass=\"1\" Tag_Emv_Small_Value_Enabled=\"00\" Tag_Emv_Small_Value_Terminal_Capabilities=\"000000\" Tag_Emv_Terminal_Capabilities_Code=\"E0F8C8\" Tag_Emv_Additional_Terminal_Capabilities=\"FF80F0A001\" Tag_Emv_Terminal_Transaction_Qualifiers=\"00000000\"/>\n" +
                    "    </Tlv_Tag_Emv_Aid_Tags>\n" +
                    "    <Tlv_Tag_Emv_Aid_Tags Tlv_Tag_Emv_Aid_Card_Origin=\"4\">\n" +
                    "      <Tlv_Tag_Emv_Aid_Txn_Tags Tag_Emv_Term_Act_Code_Default=\"D84000A800\" Tag_Emv_Term_Act_Code_Denial=\"0010000000\" Tag_Emv_Term_Act_Code_Online=\"D84004F800\" Tag_Emv_Threshold=\"00000000\" Tag_Emv_Target_Percent=\"99\" Tag_Emv_Max_Target_Percent=\"99\" Tag_Emv_Ddol_Default=\"9F3704\" Tag_Emv_Tdol_Default=\"\" Tag_Emv_Transaction_Type=\"0\" Tag_Emv_Application_Version_Number_Term=\"0202\" Tag_Emv_Terminal_Floor_Limit=\"00000000\" Tag_Emv_Mc_Transaction_Category_Code=\"82\" Tag_Emv_Standin_Act_Code=\"0000000000\" Tag_Emv_Efb_Floor_Limit=\"00000000\" Tag_Emv_Pin_Bypass=\"1\" Tag_Emv_Small_Value_Enabled=\"00\" Tag_Emv_Small_Value_Terminal_Capabilities=\"000000\" Tag_Emv_Terminal_Capabilities_Code=\"E0F8C8\" Tag_Emv_Additional_Terminal_Capabilities=\"FF80F0A001\" Tag_Emv_Terminal_Transaction_Qualifiers=\"00000000\"/>\n" +
                    "    </Tlv_Tag_Emv_Aid_Tags>\n" +
                    "  </Tlv_Tag_Emv_Aid_Param>\n" +
                    "  <Tlv_Tag_Emv_Aid_Param Tlv_Tag_Emv_Aid=\"A000000333010101\" Tlv_Tag_Emv_Aid_Priority=\"1\" Tlv_Tag_Emv_Aid_App_Sel_Indic=\"1\" Tlv_Tag_Emv_Display_Text=\"UPI Debit\">\n" +
                    "    <Tlv_Tag_Emv_Aid_Tags Tlv_Tag_Emv_Aid_Card_Origin=\"0\">\n" +
                    "      <Tlv_Tag_Emv_Aid_Txn_Tags Tag_Emv_Term_Act_Code_Default=\"D84000A800\" Tag_Emv_Term_Act_Code_Denial=\"0010000000\" Tag_Emv_Term_Act_Code_Online=\"D84004F800\" Tag_Emv_Threshold=\"00000000\" Tag_Emv_Target_Percent=\"99\" Tag_Emv_Max_Target_Percent=\"99\" Tag_Emv_Ddol_Default=\"9F3704\" Tag_Emv_Tdol_Default=\"\" Tag_Emv_Transaction_Type=\"0\" Tag_Emv_Application_Version_Number_Term=\"0020\" Tag_Emv_Terminal_Floor_Limit=\"00000000\" Tag_Emv_Mc_Transaction_Category_Code=\"82\" Tag_Emv_Standin_Act_Code=\"0000000000\" Tag_Emv_Efb_Floor_Limit=\"00000000\" Tag_Emv_Pin_Bypass=\"1\" Tag_Emv_Small_Value_Enabled=\"00\" Tag_Emv_Small_Value_Terminal_Capabilities=\"000000\" Tag_Emv_Terminal_Capabilities_Code=\"E0F8C8\" Tag_Emv_Additional_Terminal_Capabilities=\"FF80F0A001\" Tag_Emv_Terminal_Transaction_Qualifiers=\"00000000\"/>\n" +
                    "    </Tlv_Tag_Emv_Aid_Tags>\n" +
                    "    <Tlv_Tag_Emv_Aid_Tags Tlv_Tag_Emv_Aid_Card_Origin=\"4\">\n" +
                    "      <Tlv_Tag_Emv_Aid_Txn_Tags Tag_Emv_Term_Act_Code_Default=\"D84000A800\" Tag_Emv_Term_Act_Code_Denial=\"0010000000\" Tag_Emv_Term_Act_Code_Online=\"D84004F800\" Tag_Emv_Threshold=\"00000000\" Tag_Emv_Target_Percent=\"99\" Tag_Emv_Max_Target_Percent=\"99\" Tag_Emv_Ddol_Default=\"9F3704\" Tag_Emv_Tdol_Default=\"\" Tag_Emv_Transaction_Type=\"0\" Tag_Emv_Application_Version_Number_Term=\"0020\" Tag_Emv_Terminal_Floor_Limit=\"00000000\" Tag_Emv_Mc_Transaction_Category_Code=\"82\" Tag_Emv_Standin_Act_Code=\"0000000000\" Tag_Emv_Efb_Floor_Limit=\"00000000\" Tag_Emv_Pin_Bypass=\"1\" Tag_Emv_Small_Value_Enabled=\"00\" Tag_Emv_Small_Value_Terminal_Capabilities=\"000000\" Tag_Emv_Terminal_Capabilities_Code=\"E0F8C8\" Tag_Emv_Additional_Terminal_Capabilities=\"FF80F0A001\" Tag_Emv_Terminal_Transaction_Qualifiers=\"00000000\"/>\n" +
                    "    </Tlv_Tag_Emv_Aid_Tags>\n" +
                    "  </Tlv_Tag_Emv_Aid_Param>\n" +
                    "  <Tlv_Tag_Emv_Contactless_Aid_Param>\n" +
                    "   <Tlv_Tag_Emv_Aid_Param Tlv_Tag_Emv_Aid=\"A0000000031010\" Tlv_Tag_Emv_Aid_Priority=\"1\" Tlv_Tag_Emv_Aid_App_Sel_Indic=\"1\" Tlv_Tag_Emv_Display_Text=\"VISA CTLS\">\n" +
                    "       <Tlv_Tag_Emv_Aid_Tags Tlv_Tag_Emv_Aid_Card_Origin=\"0\">\n" +
                    "          <Tlv_Tag_Emv_Aid_Txn_Tags Tag_Emv_Term_Act_Code_Default=\"DC4000A800\" Tag_Emv_Term_Act_Code_Denial=\"0010000000\" Tag_Emv_Term_Act_Code_Online=\"DC4004F800\" Tag_Emv_Threshold=\"00000000\" Tag_Emv_Target_Percent=\"0\" Tag_Emv_Max_Target_Percent=\"0\" Tag_Emv_Ddol_Default=\"9F37049F02065F2A029F6905\" Tag_Emv_Tdol_Default=\"00\" Tag_Emv_Transaction_Type=\"0\" Tag_Emv_Application_Version_Number_Term=\"0140\" Tag_Emv_Terminal_Floor_Limit=\"00000000\" Tag_Emv_Mc_Transaction_Category_Code=\"82\" Tag_Emv_Standin_Act_Code=\"FC70200000\" Tag_Emv_Efb_Floor_Limit=\"0000005A\" Tag_Emv_Pin_Bypass=\"0\" Tag_Emv_Small_Value_Enabled=\"00\" Tag_Emv_Small_Value_Terminal_Capabilities=\"000000\" Tag_Emv_Terminal_Capabilities_Code=\"006040\" Tag_Emv_Additional_Terminal_Capabilities=\"FF80F0A001\" Tag_Emv_Terminal_Transaction_Qualifiers=\"00000000\" Tag_Emv_Contactless_Max_Limit=\"0098967F\" Tag_Emv_Contactless_CVM_Limit=\"00002710\" Tag_Emv_Contactless_Enabled=\"01\" Tag_Emv_Contactless_Terminal_Risk_Management_Data=\"6CF00000\"/>\n" +
                    "       </Tlv_Tag_Emv_Aid_Tags>\n" +
                    "   </Tlv_Tag_Emv_Aid_Param>\n" +
                    "   <Tlv_Tag_Emv_Aid_Param Tlv_Tag_Emv_Aid=\"A0000000041010\" Tlv_Tag_Emv_Aid_Priority=\"1\" Tlv_Tag_Emv_Aid_App_Sel_Indic=\"1\" Tlv_Tag_Emv_Display_Text=\"MC CTLS\">\n" +
                    "       <Tlv_Tag_Emv_Aid_Tags Tlv_Tag_Emv_Aid_Card_Origin=\"0\">\n" +
                    "          <Tlv_Tag_Emv_Aid_Txn_Tags Tag_Emv_Term_Act_Code_Default=\"FC509C8800\" Tag_Emv_Term_Act_Code_Denial=\"0000000000\" Tag_Emv_Term_Act_Code_Online=\"FC509C8800\" Tag_Emv_Threshold=\"00000000\" Tag_Emv_Target_Percent=\"0\" Tag_Emv_Max_Target_Percent=\"0\" Tag_Emv_Ddol_Default=\"9F3704\" Tag_Emv_Tdol_Default=\"9F02065F2A029A039C0195059F3704\" Tag_Emv_Transaction_Type=\"0\" Tag_Emv_Application_Version_Number_Term=\"0002\" Tag_Emv_Terminal_Floor_Limit=\"00000064\" Tag_Emv_Mc_Transaction_Category_Code=\"82\" Tag_Emv_Standin_Act_Code=\"FC70200000\" Tag_Emv_Efb_Floor_Limit=\"0000005A\" Tag_Emv_Pin_Bypass=\"0\" Tag_Emv_Small_Value_Enabled=\"00\" Tag_Emv_Small_Value_Terminal_Capabilities=\"000000\" Tag_Emv_Terminal_Capabilities_Code=\"E068C8\" Tag_Emv_Additional_Terminal_Capabilities=\"FF80F0A001\" Tag_Emv_Terminal_Transaction_Qualifiers=\"00000000\" Tag_Emv_Contactless_Max_Limit=\"05F5E0FF\" Tag_Emv_Contactless_CVM_Limit=\"00002710\" Tag_Emv_Contactless_Enabled=\"01\" Tag_Emv_Contactless_Terminal_Risk_Management_Data=\"6CF00000\"/>\n" +
                    "       </Tlv_Tag_Emv_Aid_Tags>\n" +
                    "   </Tlv_Tag_Emv_Aid_Param>\n" +
                    "   <Tlv_Tag_Emv_Aid_Param Tlv_Tag_Emv_Aid=\"A00000002501\" Tlv_Tag_Emv_Aid_Priority=\"1\" Tlv_Tag_Emv_Aid_App_Sel_Indic=\"1\" Tlv_Tag_Emv_Display_Text=\"Amex CTLS\">\n" +
                    "       <Tlv_Tag_Emv_Aid_Tags Tlv_Tag_Emv_Aid_Card_Origin=\"0\">\n" +
                    "          <Tlv_Tag_Emv_Aid_Txn_Tags Tag_Emv_Term_Act_Code_Default=\"DC50840000\" Tag_Emv_Term_Act_Code_Denial=\"0000000000\" Tag_Emv_Term_Act_Code_Online=\"C400000000\" Tag_Emv_Threshold=\"00000000\" Tag_Emv_Target_Percent=\"0\" Tag_Emv_Max_Target_Percent=\"0\" Tag_Emv_Ddol_Default=\"9F3704\" Tag_Emv_Tdol_Default=\"9F02065F2A029A039C0195059F3704\" Tag_Emv_Transaction_Type=\"0\" Tag_Emv_Application_Version_Number_Term=\"0002\" Tag_Emv_Terminal_Floor_Limit=\"0000000C\" Tag_Emv_Mc_Transaction_Category_Code=\"82\" Tag_Emv_Standin_Act_Code=\"F858A02000\" Tag_Emv_Efb_Floor_Limit=\"00000000\" Tag_Emv_Pin_Bypass=\"0\" Tag_Emv_Small_Value_Enabled=\"01\" Tag_Emv_Small_Value_Terminal_Capabilities=\"E008C8\" Tag_Emv_Terminal_Capabilities_Code=\"E0E8C8\" Tag_Emv_Additional_Terminal_Capabilities=\"FF80F0A001\" Tag_Emv_Terminal_Transaction_Qualifiers=\"00000000\" Tag_Emv_Contactless_Max_Limit=\"000005DC\" Tag_Emv_Contactless_CVM_Limit=\"000003E8\" Tag_Emv_Contactless_Enabled=\"01\" Tag_Emv_Contactless_Terminal_Risk_Management_Data=\"6CF00000\"/>\n" +
                    "       </Tlv_Tag_Emv_Aid_Tags>\n" +
                    "   </Tlv_Tag_Emv_Aid_Param>\n" +
                    "   <Tlv_Tag_Emv_Aid_Param Tlv_Tag_Emv_Aid=\"A00000038410\" Tlv_Tag_Emv_Aid_Priority=\"1\" Tlv_Tag_Emv_Aid_App_Sel_Indic=\"1\" Tlv_Tag_Emv_Display_Text=\"Eftpos Sav CLTS\">\n" +
                    "       <Tlv_Tag_Emv_Aid_Tags Tlv_Tag_Emv_Aid_Card_Origin=\"0\">\n" +
                    "          <Tlv_Tag_Emv_Aid_Txn_Tags Tag_Emv_Term_Act_Code_Default=\"FFFFFFFFFF\" Tag_Emv_Term_Act_Code_Denial=\"0600000000\" Tag_Emv_Term_Act_Code_Online=\"FC509C9800\" Tag_Emv_Threshold=\"00000000\" Tag_Emv_Target_Percent=\"0\" Tag_Emv_Max_Target_Percent=\"0\" Tag_Emv_Ddol_Default=\"9F3704\" Tag_Emv_Tdol_Default=\"9F02065F2A029A039C0195059F3704\" Tag_Emv_Transaction_Type=\"0\" Tag_Emv_Application_Version_Number_Term=\"0100\" Tag_Emv_Terminal_Floor_Limit=\"0000000A\" Tag_Emv_Mc_Transaction_Category_Code=\"82\" Tag_Emv_Standin_Act_Code=\"0000000000\" Tag_Emv_Efb_Floor_Limit=\"00000000\" Tag_Emv_Pin_Bypass=\"0\" Tag_Emv_Small_Value_Enabled=\"00\" Tag_Emv_Small_Value_Terminal_Capabilities=\"000000\" Tag_Emv_Terminal_Capabilities_Code=\"E04808\" Tag_Emv_Additional_Terminal_Capabilities=\"FF80F0A001\" Tag_Emv_Terminal_Transaction_Qualifiers=\"00000000\" Tag_Emv_Contactless_Max_Limit=\"00002710\" Tag_Emv_Contactless_CVM_Limit=\"000003E8\" Tag_Emv_Contactless_Enabled=\"01\" Tag_Emv_Contactless_Terminal_Risk_Management_Data=\"6CF00000\"/>\n" +
                    "       </Tlv_Tag_Emv_Aid_Tags>\n" +
                    "   </Tlv_Tag_Emv_Aid_Param>\n" +
                    "   <Tlv_Tag_Emv_Aid_Param Tlv_Tag_Emv_Aid=\"A00000038420\" Tlv_Tag_Emv_Aid_Priority=\"1\" Tlv_Tag_Emv_Aid_App_Sel_Indic=\"1\" Tlv_Tag_Emv_Display_Text=\"Eftpos Chq CLTS\">\n" +
                    "       <Tlv_Tag_Emv_Aid_Tags Tlv_Tag_Emv_Aid_Card_Origin=\"0\">\n" +
                    "          <Tlv_Tag_Emv_Aid_Txn_Tags Tag_Emv_Term_Act_Code_Default=\"FFFFFFFFFF\" Tag_Emv_Term_Act_Code_Denial=\"0600000000\" Tag_Emv_Term_Act_Code_Online=\"FC509C9800\" Tag_Emv_Threshold=\"00000000\" Tag_Emv_Target_Percent=\"0\" Tag_Emv_Max_Target_Percent=\"0\" Tag_Emv_Ddol_Default=\"9F3704\" Tag_Emv_Tdol_Default=\"9F02065F2A029A039C0195059F3704\" Tag_Emv_Transaction_Type=\"0\" Tag_Emv_Application_Version_Number_Term=\"0100\" Tag_Emv_Terminal_Floor_Limit=\"0000000A\" Tag_Emv_Mc_Transaction_Category_Code=\"82\" Tag_Emv_Standin_Act_Code=\"0000000000\" Tag_Emv_Efb_Floor_Limit=\"00000000\" Tag_Emv_Pin_Bypass=\"0\" Tag_Emv_Small_Value_Enabled=\"00\" Tag_Emv_Small_Value_Terminal_Capabilities=\"000000\" Tag_Emv_Terminal_Capabilities_Code=\"E04808\" Tag_Emv_Additional_Terminal_Capabilities=\"FF80F0A001\" Tag_Emv_Terminal_Transaction_Qualifiers=\"00000000\" Tag_Emv_Contactless_Max_Limit=\"00002710\" Tag_Emv_Contactless_CVM_Limit=\"000003E8\" Tag_Emv_Contactless_Enabled=\"01\" Tag_Emv_Contactless_Terminal_Risk_Management_Data=\"6CF00000\"/>\n" +
                    "       </Tlv_Tag_Emv_Aid_Tags>\n" +
                    "   </Tlv_Tag_Emv_Aid_Param>\n" +
                    "   <Tlv_Tag_Emv_Aid_Param Tlv_Tag_Emv_Aid=\"A000000333010102\" Tlv_Tag_Emv_Aid_Priority=\"1\" Tlv_Tag_Emv_Aid_App_Sel_Indic=\"1\" Tlv_Tag_Emv_Display_Text=\"UPI CTLS Credit\">\n" +
                    "       <Tlv_Tag_Emv_Aid_Tags Tlv_Tag_Emv_Aid_Card_Origin=\"4\">\n" +
                    "          <Tlv_Tag_Emv_Aid_Txn_Tags Tag_Emv_Term_Act_Code_Default=\"DC50840000\" Tag_Emv_Term_Act_Code_Denial=\"0000000000\" Tag_Emv_Term_Act_Code_Online=\"C400000000\" Tag_Emv_Threshold=\"00000000\" Tag_Emv_Target_Percent=\"99\" Tag_Emv_Max_Target_Percent=\"99\" Tag_Emv_Ddol_Default=\"9F3704\" Tag_Emv_Tdol_Default=\"9F02065F2A029A039C0195059F3704\" Tag_Emv_Transaction_Type=\"0\" Tag_Emv_Application_Version_Number_Term=\"0200\" Tag_Emv_Terminal_Floor_Limit=\"00000000\" Tag_Emv_Mc_Transaction_Category_Code=\"82\" Tag_Emv_Standin_Act_Code=\"F858A02000\" Tag_Emv_Efb_Floor_Limit=\"00000000\" Tag_Emv_Pin_Bypass=\"1\" Tag_Emv_Small_Value_Enabled=\"00\" Tag_Emv_Small_Value_Terminal_Capabilities=\"000000\" Tag_Emv_Terminal_Capabilities_Code=\"0068C8\" Tag_Emv_Additional_Terminal_Capabilities=\"C000001001\" Tag_Emv_Terminal_Transaction_Qualifiers=\"26000080\" Tag_Emv_Contactless_Max_Limit=\"05F5E0FF\" Tag_Emv_Contactless_CVM_Limit=\"00002711\" Tag_Emv_Contactless_Enabled=\"01\" Tag_Emv_Contactless_Terminal_Risk_Management_Data=\"6CF00000\"/>\n" +
                    "       </Tlv_Tag_Emv_Aid_Tags>\n" +
                    "   </Tlv_Tag_Emv_Aid_Param>\n" +
                    "   <Tlv_Tag_Emv_Aid_Param Tlv_Tag_Emv_Aid=\"A0000000651010\" Tlv_Tag_Emv_Aid_Priority=\"1\" Tlv_Tag_Emv_Aid_App_Sel_Indic=\"1\" Tlv_Tag_Emv_Display_Text=\"JSpeedy\">\n" +
                    "       <Tlv_Tag_Emv_Aid_Tags Tlv_Tag_Emv_Aid_Card_Origin=\"0\">\n" +
                    "          <Tlv_Tag_Emv_Aid_Txn_Tags Tag_Emv_Term_Act_Code_Default=\"F86024A800\" Tag_Emv_Term_Act_Code_Denial=\"0010000000\" Tag_Emv_Term_Act_Code_Online=\"FC60ACF800\" Tag_Emv_Threshold=\"00000000\" Tag_Emv_Target_Percent=\"0\" Tag_Emv_Max_Target_Percent=\"0\" Tag_Emv_Ddol_Default=\"9F3704\" Tag_Emv_Tdol_Default=\"9F02065F2A029A039C0195059F3704\" Tag_Emv_Transaction_Type=\"0\" Tag_Emv_Application_Version_Number_Term=\"0200\" Tag_Emv_Terminal_Floor_Limit=\"00000032\" Tag_Emv_Mc_Transaction_Category_Code=\"82\" Tag_Emv_Standin_Act_Code=\"0000000000\" Tag_Emv_Efb_Floor_Limit=\"00000000\" Tag_Emv_Pin_Bypass=\"0\" Tag_Emv_Small_Value_Enabled=\"00\" Tag_Emv_Small_Value_Terminal_Capabilities=\"000000\" Tag_Emv_Terminal_Capabilities_Code=\"E068C8\" Tag_Emv_Additional_Terminal_Capabilities=\"7200002001\" Tag_Emv_Terminal_Transaction_Qualifiers=\"00000000\" Tag_Emv_Contactless_Max_Limit=\"05F5E0FF\" Tag_Emv_Contactless_CVM_Limit=\"00001389\" Tag_Emv_Contactless_Enabled=\"01\" Tag_Emv_Contactless_Terminal_Risk_Management_Data=\"6CF00000\"/>\n" +
                    "       </Tlv_Tag_Emv_Aid_Tags>\n" +
                    "   </Tlv_Tag_Emv_Aid_Param>\n" +
                    "   <Tlv_Tag_Emv_Aid_Param Tlv_Tag_Emv_Aid=\"A000000333010103\" Tlv_Tag_Emv_Aid_Priority=\"1\" Tlv_Tag_Emv_Aid_App_Sel_Indic=\"1\" Tlv_Tag_Emv_Display_Text=\"UPI CTLS QUASI C\">\n" +
                    "       <Tlv_Tag_Emv_Aid_Tags Tlv_Tag_Emv_Aid_Card_Origin=\"4\">\n" +
                    "          <Tlv_Tag_Emv_Aid_Txn_Tags Tag_Emv_Term_Act_Code_Default=\"DC50840000\" Tag_Emv_Term_Act_Code_Denial=\"0000000000\" Tag_Emv_Term_Act_Code_Online=\"C400000000\" Tag_Emv_Threshold=\"00000000\" Tag_Emv_Target_Percent=\"99\" Tag_Emv_Max_Target_Percent=\"99\" Tag_Emv_Ddol_Default=\"9F3704\" Tag_Emv_Tdol_Default=\"9F02065F2A029A039C0195059F3704\" Tag_Emv_Transaction_Type=\"0\" Tag_Emv_Application_Version_Number_Term=\"0200\" Tag_Emv_Terminal_Floor_Limit=\"00000000\" Tag_Emv_Mc_Transaction_Category_Code=\"82\" Tag_Emv_Standin_Act_Code=\"0000000000\" Tag_Emv_Efb_Floor_Limit=\"00000000\" Tag_Emv_Pin_Bypass=\"1\" Tag_Emv_Small_Value_Enabled=\"00\" Tag_Emv_Small_Value_Terminal_Capabilities=\"000000\" Tag_Emv_Terminal_Capabilities_Code=\"0068C8\" Tag_Emv_Additional_Terminal_Capabilities=\"C000001001\" Tag_Emv_Terminal_Transaction_Qualifiers=\"00000000\" Tag_Emv_Contactless_Max_Limit=\"05F5E0FF\" Tag_Emv_Contactless_CVM_Limit=\"00002711\" Tag_Emv_Contactless_Enabled=\"01\" Tag_Emv_Contactless_Terminal_Risk_Management_Data=\"6CF00000\"/>\n" +
                    "       </Tlv_Tag_Emv_Aid_Tags>\n" +
                    "   </Tlv_Tag_Emv_Aid_Param>\n" +
                    "   <Tlv_Tag_Emv_Aid_Param Tlv_Tag_Emv_Aid=\"A000000333010101\" Tlv_Tag_Emv_Aid_Priority=\"1\" Tlv_Tag_Emv_Aid_App_Sel_Indic=\"1\" Tlv_Tag_Emv_Display_Text=\"UPI CTLS DEBIT\">\n" +
                    "       <Tlv_Tag_Emv_Aid_Tags Tlv_Tag_Emv_Aid_Card_Origin=\"0\">\n" +
                    "          <Tlv_Tag_Emv_Aid_Txn_Tags Tag_Emv_Term_Act_Code_Default=\"DC50840000\" Tag_Emv_Term_Act_Code_Denial=\"0000000000\" Tag_Emv_Term_Act_Code_Online=\"C400000000\" Tag_Emv_Threshold=\"00000000\" Tag_Emv_Target_Percent=\"99\" Tag_Emv_Max_Target_Percent=\"99\" Tag_Emv_Ddol_Default=\"9F3704\" Tag_Emv_Tdol_Default=\"9F02065F2A029A039C0195059F3704\" Tag_Emv_Transaction_Type=\"0\" Tag_Emv_Application_Version_Number_Term=\"0200\" Tag_Emv_Terminal_Floor_Limit=\"00000000\" Tag_Emv_Mc_Transaction_Category_Code=\"82\" Tag_Emv_Standin_Act_Code=\"0000000000\" Tag_Emv_Efb_Floor_Limit=\"00000000\" Tag_Emv_Pin_Bypass=\"1\" Tag_Emv_Small_Value_Enabled=\"00\" Tag_Emv_Small_Value_Terminal_Capabilities=\"000000\" Tag_Emv_Terminal_Capabilities_Code=\"0068C8\" Tag_Emv_Additional_Terminal_Capabilities=\"C000001001\" Tag_Emv_Terminal_Transaction_Qualifiers=\"00000000\" Tag_Emv_Contactless_Max_Limit=\"05F5E0FF\" Tag_Emv_Contactless_CVM_Limit=\"00002710\" Tag_Emv_Contactless_Enabled=\"01\" Tag_Emv_Contactless_Terminal_Risk_Management_Data=\"6CF00000\"/>\n" +
                    "       </Tlv_Tag_Emv_Aid_Tags>\n" +
                    "   </Tlv_Tag_Emv_Aid_Param>\n" +
                    "  </Tlv_Tag_Emv_Contactless_Aid_Param>\n" +
                    "</EmvParameterTable>\n";


    Parse parse;
    Parse parseJson;
    @Before
    public void init() {
        parse = new XmlParse();
        parseJson = new JSONParse();
    }


    @Test
    public void ParseEpat() {
        WoolworthsEpatConfig ePatParams = parse.parseFromString(testData, WoolworthsEpatConfig.class);
        assertNotNull(ePatParams);
        assertNotNull(ePatParams.getParamList());

        assertEquals(9, ePatParams.getParamList().size() );

        assertEquals("Visa", ePatParams.getParamList().get(0).getDisplayText());
        assertEquals(2, ePatParams.getParamList().get(0).getTagList().size());

        assertEquals("MasterCard", ePatParams.getParamList().get(1).getDisplayText());

        assertEquals("eftpos sav", ePatParams.getParamList().get(3).getDisplayText());
        assertEquals(1, ePatParams.getParamList().get(3).getTagList().size());

        assertNotNull(ePatParams.getContactlessParamList() );
        assertEquals(9, ePatParams.getContactlessParamList().getParamList().size() );

        assertEquals("A0000000031010", ePatParams.getContactlessParamList().getParamList().get(0).getAid() );
        assertEquals("VISA CTLS", ePatParams.getContactlessParamList().getParamList().get(0).getDisplayText() );

    }

    // the following test requires jackson in build.grade entry, e.g. implementation 'com.fasterxml.jackson.dataformat:jackson-dataformat-xml:2.13'
    // however, including that library in build.gradle causes other build clashes to occur.
    // see https://github.com/FasterXML/jackson-dataformat-xml for more info
//
//    @Test
//    public void test_GeneratePktXml() {
//        WoolworthsPktConfig pktConfig = new WoolworthsPktConfig();
//
//
//        // visa keys
//        pktConfig.getKeyList().add( new WoolworthsPktConfig.CAPKey( "A000000003", "95", "000003", "01", "010100", "311215", "BE9E1FA5E9A803852999C4AB432DB28600DCD9DAB76DFAAA47355A0FE37B1508AC6BF38860D3C6C2E5B12A3CAAF2A7005A7241EBAA7771112C74CF9A0634652FBCA0E5980C54A64761EA101A114E0F0B5572ADD57D010B7C9C887E104CA4EE1272DA66D997B9A90B5A6D624AB6C57E73C8F919000EB5F684898EF8C3DBEFB330C62660BED88EA78E909AFF05F6DA627B"));
//        pktConfig.getKeyList().add( new WoolworthsPktConfig.CAPKey( "A000000003", "99", "000003", "01", "010100", "311215", "AB79FCC9520896967E776E64444E5DCDD6E13611874F3985722520425295EEA4BD0C2781DE7F31CD3D041F565F747306EED62954B17EDABA3A6C5B85A1DE1BEB9A34141AF38FCF8279C9DEA0D5A6710D08DB4124F041945587E20359BAB47B7575AD94262D4B25F264AF33DEDCF28E09615E937DE32EDC03C54445FE7E382777"));
//        pktConfig.getKeyList().add( new WoolworthsPktConfig.CAPKey( "A000000003", "92", "000003", "01", "010100", "311215", "996AF56F569187D09293C14810450ED8EE3357397B18A2458EFAA92DA3B6DF6514EC060195318FD43BE9B8F0CC669E3F844057CBDDF8BDA191BB64473BC8DC9A730DB8F6B4EDE3924186FFD9B8C7735789C23A36BA0B8AF65372EB57EA5D89E7D14E9C7B6B557460F10885DA16AC923F15AF3758F0F03EBD3C5C2C949CBA306DB44E6A2C076C5F67E281D7EF56785DC4D75945E491F01918800A9E2DC66F60080566CE0DAF8D17EAD46AD8E30A247C9F"));
//        pktConfig.getKeyList().add( new WoolworthsPktConfig.CAPKey( "A000000003", "94", "000003", "01", "010100", "311215", "ACD2B12302EE644F3F835ABD1FC7A6F62CCE48FFEC622AA8EF062BEF6FB8BA8BC68BBF6AB5870EED579BC3973E121303D34841A796D6DCBC41DBF9E52C4609795C0CCF7EE86FA1D5CB041071ED2C51D2202F63F1156C58A92D38BC60BDF424E1776E2BC9648078A03B36FB554375FC53D57C73F5160EA59F3AFC5398EC7B67758D65C9BFF7828B6B82D4BE124A416AB7301914311EA462C19F771F31B3B57336000DFF732D3B83DE07052D730354D297BEC72871DCCF0E193F171ABA27EE464C6A97690943D59BDABB2A27EB71CEEBDAFA1176046478FD62FEC452D5CA393296530AA3F41927ADFE434A2DF2AE3054F8840657A26E0FC617"));
//        pktConfig.getKeyList().add( new WoolworthsPktConfig.CAPKey( "A000000003", "96", "000003", "01", "010100", "311214", "B74586D19A207BE6627C5B0AAFBC44A2ECF5A2942D3A26CE19C4FFAEEE920521868922E893E7838225A3947A2614796FB2C0628CE8C11E3825A56D3B1BBAEF783A5C6A81F36F8625395126FA983C5216D3166D48ACDE8A431212FF763A7F79D9EDB7FED76B485DE45BEB829A3D4730848A366D3324C3027032FF8D16A1E44D8D"));
//        pktConfig.getKeyList().add( new WoolworthsPktConfig.CAPKey( "A000000003", "51", "000003", "01", "010100", "311215", "DB5FA29D1FDA8C1634B04DCCFF148ABEE63C772035C79851D3512107586E02A917F7C7E885E7C4A7D529710A145334CE67DC412CB1597B77AA2543B98D19CF2CB80C522BDBEA0F1B113FA2C86216C8C610A2D58F29CF3355CEB1BD3EF410D1EDD1F7AE0F16897979DE28C6EF293E0A19282BD1D793F1331523FC71A228800468C01A3653D14C6B4851A5C029478E757F"));
//
//        // mastercard keys
//        pktConfig.getKeyList().add( new WoolworthsPktConfig.CAPKey( "A000000004", "F3", "000003", "01", "010100", "311212", "98F0C770F23864C2E766DF02D1E833DFF4FFE92D696E1642F0A88C5694C6479D16DB1537BFE29E4FDC6E6E8AFD1B0EB7EA0124723C333179BF19E93F10658B2F776E829E87DAEDA9C94A8B3382199A350C077977C97AFF08FD11310AC950A72C3CA5002EF513FCCC286E646E3C5387535D509514B3B326E1234F9CB48C36DDD44B416D23654034A66F403BA511C5EFA3"));
//        pktConfig.getKeyList().add( new WoolworthsPktConfig.CAPKey( "A000000004", "F8", "000003", "01", "010100", "311212", "A1F5E1C9BD8650BD43AB6EE56B891EF7459C0A24FA84F9127D1A6C79D4930F6DB1852E2510F18B61CD354DB83A356BD190B88AB8DF04284D02A4204A7B6CB7C5551977A9B36379CA3DE1A08E69F301C95CC1C20506959275F41723DD5D2925290579E5A95B0DF6323FC8E9273D6F849198C4996209166D9BFC973C361CC826E1"));
//        pktConfig.getKeyList().add( new WoolworthsPktConfig.CAPKey( "A000000004", "FE", "000003", "01", "010100", "311215", "A653EAC1C0F786C8724F737F172997D63D1C3251C44402049B865BAE877D0F398CBFBE8A6035E24AFA086BEFDE9351E54B95708EE672F0968BCD50DCE40F783322B2ABA04EF137EF18ABF03C7DBC5813AEAEF3AA7797BA15DF7D5BA1CBAF7FD520B5A482D8D3FEE105077871113E23A49AF3926554A70FE10ED728CF793B62A1"));
//        pktConfig.getKeyList().add( new WoolworthsPktConfig.CAPKey( "A000000004", "F1", "000003", "01", "010100", "311215", "A0DCF4BDE19C3546B4B6F0414D174DDE294AABBB828C5A834D73AAE27C99B0B053A90278007239B6459FF0BBCD7B4B9C6C50AC02CE91368DA1BD21AAEADBC65347337D89B68F5C99A09D05BE02DD1F8C5BA20E2F13FB2A27C41D3F85CAD5CF6668E75851EC66EDBF98851FD4E42C44C1D59F5984703B27D5B9F21B8FA0D93279FBBF69E090642909C9EA27F898959541AA6757F5F624104F6E1D3A9532F2A6E51515AEAD1B43B3D7835088A2FAFA7BE7"));
//        pktConfig.getKeyList().add( new WoolworthsPktConfig.CAPKey( "A000000004", "FA", "000003", "01", "010100", "311215", "A90FCD55AA2D5D9963E35ED0F440177699832F49C6BAB15CDAE5794BE93F934D4462D5D12762E48C38BA83D8445DEAA74195A301A102B2F114EADA0D180EE5E7A5C73E0C4E11F67A43DDAB5D55683B1474CC0627F44B8D3088A492FFAADAD4F42422D0E7013536C3C49AD3D0FAE96459B0F6B1B6056538A3D6D44640F94467B108867DEC40FAAECD740C00E2B7A8852D"));
//        pktConfig.getKeyList().add( new WoolworthsPktConfig.CAPKey( "A000000004", "EF", "000003", "01", "010100", "311215", "A191CB87473F29349B5D60A88B3EAEE0973AA6F1A082F358D849FDDFF9C091F899EDA9792CAF09EF28F5D22404B88A2293EEBBC1949C43BEA4D60CFD879A1539544E09E0F09F60F065B2BF2A13ECC705F3D468B9D33AE77AD9D3F19CA40F23DCF5EB7C04DC8F69EBA565B1EBCB4686CD274785530FF6F6E9EE43AA43FDB02CE00DAEC15C7B8FD6A9B394BABA419D3F6DC85E16569BE8E76989688EFEA2DF22FF7D35C043338DEAA982A02B866DE5328519EBBCD6F03CDD686673847F84DB651AB86C28CF1462562C577B853564A290C8556D818531268D25CC98A4CC6A0BDFFFDA2DCCA3A94C998559E307FDDF915006D9A987B07DDAEB3B"));
//
//        // amex keys
//        pktConfig.getKeyList().add( new WoolworthsPktConfig.CAPKey( "A000000025", "03", "000003", "01", "010100", "311220", "B0C2C6E2A6386933CD17C239496BF48C57E389164F2A96BFF133439AE8A77B20498BD4DC6959AB0C2D05D0723AF3668901937B674E5A2FA92DDD5E78EA9D75D79620173CC269B35F463B3D4AAFF2794F92E6C7A3FB95325D8AB95960C3066BE548087BCB6CE12688144A8B4A66228AE4659C634C99E36011584C095082A3A3E3"));
//        pktConfig.getKeyList().add( new WoolworthsPktConfig.CAPKey( "A000000025", "0E", "000003", "01", "010100", "311216", "AA94A8C6DAD24F9BA56A27C09B01020819568B81A026BE9FD0A3416CA9A71166ED5084ED91CED47DD457DB7E6CBCD53E560BC5DF48ABC380993B6D549F5196CFA77DFB20A0296188E969A2772E8C4141665F8BB2516BA2C7B5FC91F8DA04E8D512EB0F6411516FB86FC021CE7E969DA94D33937909A53A57F907C40C22009DA7532CB3BE509AE173B39AD6A01BA5BB85"));
//        pktConfig.getKeyList().add( new WoolworthsPktConfig.CAPKey( "A000000025", "0F", "000003", "01", "010100", "311217", "C8D5AC27A5E1FB89978C7C6479AF993AB3800EB243996FBB2AE26B67B23AC482C4B746005A51AFA7D2D83E894F591A2357B30F85B85627FF15DA12290F70F05766552BA11AD34B7109FA49DE29DCB0109670875A17EA95549E92347B948AA1F045756DE56B707E3863E59A6CBE99C1272EF65FB66CBB4CFF070F36029DD76218B21242645B51CA752AF37E70BE1A84FF31079DC0048E928883EC4FADD497A719385C2BBBEBC5A66AA5E5655D18034EC5"));
//        pktConfig.getKeyList().add( new WoolworthsPktConfig.CAPKey( "A000000025", "10", "000003", "01", "010100", "311218", "CF98DFEDB3D3727965EE7797723355E0751C81D2D3DF4D18EBAB9FB9D49F38C8C4A826B99DC9DEA3F01043D4BF22AC3550E2962A59639B1332156422F788B9C16D40135EFD1BA94147750575E636B6EBC618734C91C1D1BF3EDC2A46A43901668E0FFC136774080E888044F6A1E65DC9AAA8928DACBEB0DB55EA3514686C6A732CEF55EE27CF877F110652694A0E3484C855D882AE191674E25C296205BBB599455176FDD7BBC549F27BA5FE35336F7E29E68D783973199436633C67EE5A680F05160ED12D1665EC83D1997F10FD05BBDBF9433E8F797AEE3E9F02A34228ACE927ABE62B8B9281AD08D3DF5C7379685045D7BA5FCDE58637"));
//        pktConfig.getKeyList().add( new WoolworthsPktConfig.CAPKey( "A000000025", "97", "000003", "01", "010100", "311219", "E178FFE834B4B767AF3C9A511F973D8E8505C5FCB2D3768075AB7CC946A955789955879AAF737407151521996DFA43C58E6B130EB1D863B85DC9FFB4050947A2676AA6A061A4A7AE1EDB0E36A697E87E037517EB8923136875BA2CA1087CBA7EC7653E5E28A0C261A033AF27E3A67B64BBA26956307EC47E674E3F8B722B3AE0498DB16C7985310D9F3D117300D32B09"));
//        pktConfig.getKeyList().add( new WoolworthsPktConfig.CAPKey( "A000000025", "98", "000003", "01", "010100", "311218", "D31A7094FB221CBA6660FB975AAFEA80DB7BB7EAFD7351E748827AB62D4AEECCFC1787FD47A04699A02DB00D7C382E80E804B35C59434C602389D691B9CCD51ED06BE67A276119C4C10E2E40FC4EDDF9DF39B9B0BDEE8D076E2A012E8A292AF8EFE18553470639C1A032252E0E5748B25A3F9BA4CFCEE073038B061837F2AC1B04C279640F5BD110A9DC665ED2FA6828BD5D0FE810A892DEE6B0E74CE8863BDE08FD5FD61A0F11FA0D14978D8CED7DD3"));
//        pktConfig.getKeyList().add( new WoolworthsPktConfig.CAPKey( "A000000025", "99", "000003", "01", "010100", "311218", "E1740074229FA0D228A9623581D7A322903FB89BA7686712E601FA8AB24A9789186F15B70CCBBE7421B1CB110D45361688135FFD0DB15A3F516BB291D4A123EBF5A06FBF7E1EE6311B737DABB289570A7959D532B25F1DA6758C84DDCCADC049BC764C05391ABD2CADEFFA7E242D5DD06E56001F0E68151E3388074BD9330D6AFA57CBF33946F531E51E0D4902EE235C756A905FB733940E6EC897B4944A5EDC765705E2ACF76C78EAD78DD9B066DF0B2C88750B8AEE00C9B4D4091FA7338449DA92DBFC908FA0781C0128C492DB993C88BA8BB7CADFE238D477F2517E0E7E3D2B11796A0318CE2AD4DA1DB8E54AB0D94F109DB9CAEEFBEF"));
//        pktConfig.getKeyList().add( new WoolworthsPktConfig.CAPKey( "A000000025", "04", "000003", "01", "010100", "311218", "D0F543F03F2517133EF2BA4A1104486758630DCFE3A883C77B4E4844E39A9BD6360D23E6644E1E071F196DDF2E4A68B4A3D93D14268D7240F6A14F0D714C17827D279D192E88931AF7300727AE9DA80A3F0E366AEBA61778171737989E1EE309"));
//        pktConfig.getKeyList().add( new WoolworthsPktConfig.CAPKey( "A000000025", "65", "000003", "01", "010100", "311218", "E53EB41F839DDFB474F272CD0CBE373D5468EB3F50F39C95BDF4D39FA82B98DABC9476B6EA350C0DCE1CD92075D8C44D1E57283190F96B3537D9E632C461815EBD2BAF36891DF6BFB1D30FA0B752C43DCA0257D35DFF4CCFC98F84198D5152EC61D7B5F74BD09383BD0E2AA42298FFB02F0D79ADB70D72243EE537F75536A8A8DF962582E9E6812F3A0BE02A4365400D"));
//        pktConfig.getKeyList().add( new WoolworthsPktConfig.CAPKey( "A000000025", "C1", "000003", "01", "010100", "311217", "E69E319C34D1B4FB43AED4BD8BBA6F7A8B763F2F6EE5DDF7C92579A984F89C4A9C15B27037764C58AC7E45EFBC34E138E56BA38F76E803129A8DDEB5E1CC8C6B30CF634A9C9C1224BF1F0A9A18D79ED41EBCF1BE78087AE8B7D2F896B1DE8B7E784161A138A0F2169AD33E146D1B16AB595F9D7D98BE671062D217F44EB68C68640C7D57465A063F6BAC776D3E2DAC61"));
//        pktConfig.getKeyList().add( new WoolworthsPktConfig.CAPKey( "A000000025", "C2", "000003", "01", "010100", "311220", "B875002F38BA26D61167C5D440367604AD38DF2E93D8EE8DA0E8D9C0CF4CC5788D11DEA689E5F41D23A3DA3E0B1FA5875AE25620F5A6BCCEE098C1B35C691889D7D0EF670EB8312E7123FCC5DC7D2F0719CC80E1A93017F944D097330EDF945762FEE62B7B0BA0348228DBF38D4216E5A67A7EF74F5D3111C44AA31320F623CB3C53E60966D6920067C9E082B746117E48E4F00E110950CA54DA3E38E5453BD5544E3A6760E3A6A42766AD2284E0C9AF"));
//        pktConfig.getKeyList().add( new WoolworthsPktConfig.CAPKey( "A000000025", "C3", "000003", "01", "010100", "311220", "B93182ABE343DFBF388C71C4D6747DCDEC60367FE63CFAA942D7D323E688D0832836548BF0EDFF1EDEEB882C75099FF81A93FA525C32425B36023EA02A8899B9BF7D7934E86F997891823006CEAA93091A73C1FDE18ABD4F87A22308640C064C8C027685F1B2DB7B741B67AB0DE05E870481C5F972508C17F57E4F833D63220F6EA2CFBB878728AA5887DE407D10C6B8F58D46779ECEC1E2155487D52C78A5C03897F2BB580E0A2BBDE8EA2E1C18F6AAF3EB3D04C3477DEAB88F150C8810FD1EF8EB0596866336FE2C1FBC6BEC22B4FE5D885647726DB59709A505F75C49E0D8D71BF51E4181212BE2142AB2A1E8C0D3B7136CD7B7708E4D"));
//        pktConfig.getKeyList().add( new WoolworthsPktConfig.CAPKey( "A000000025", "C8", "000003", "01", "010100", "311217", "BF0CFCED708FB6B048E3014336EA24AA007D7967B8AA4E613D26D015C4FE7805D9DB131CED0D2A8ED504C3B5CCD48C33199E5A5BF644DA043B54DBF60276F05B1750FAB39098C7511D04BABC649482DDCF7CC42C8C435BAB8DD0EB1A620C31111D1AAAF9AF6571EEBD4CF5A08496D57E7ABDBB5180E0A42DA869AB95FB620EFF2641C3702AF3BE0B0C138EAEF202E21D"));
//        pktConfig.getKeyList().add( new WoolworthsPktConfig.CAPKey( "A000000025", "C9", "000003", "01", "010100", "311220", "B362DB5733C15B8797B8ECEE55CB1A371F760E0BEDD3715BB270424FD4EA26062C38C3F4AAA3732A83D36EA8E9602F6683EECC6BAFF63DD2D49014BDE4D6D603CD744206B05B4BAD0C64C63AB3976B5C8CAAF8539549F5921C0B700D5B0F83C4E7E946068BAAAB5463544DB18C63801118F2182EFCC8A1E85E53C2A7AE839A5C6A3CABE73762B70D170AB64AFC6CA482944902611FB0061E09A67ACB77E493D998A0CCF93D81A4F6C0DC6B7DF22E62DB"));
//        pktConfig.getKeyList().add( new WoolworthsPktConfig.CAPKey( "A000000025", "CA", "000003", "01", "010100", "311220", "C23ECBD7119F479C2EE546C123A585D697A7D10B55C2D28BEF0D299C01DC65420A03FE5227ECDECB8025FBC86EEBC1935298C1753AB849936749719591758C315FA150400789BB14FADD6EAE2AD617DA38163199D1BAD5D3F8F6A7A20AEF420ADFE2404D30B219359C6A4952565CCCA6F11EC5BE564B49B0EA5BF5B3DC8C5C6401208D0029C3957A8C5922CBDE39D3A564C6DEBB6BD2AEF91FC27BB3D3892BEB9646DCE2E1EF8581EFFA712158AAEC541C0BBB4B3E279D7DA54E45A0ACC3570E712C9F7CDF985CFAFD382AE13A3B214A9E8E1E71AB1EA707895112ABC3A97D0FCB0AE2EE5C85492B6CFD54885CDD6337E895CC70FB3255E3"));
//
//        // eftpos keys
//        pktConfig.getKeyList().add( new WoolworthsPktConfig.CAPKey( "A000000384", "C1", "000003", "01", "010100", "311215", "AD6E00E5882CEEF578AFB002980FBD901089100B921200D6442C176D93DA5C9399B8427CBE19C1B2E638F5FC78875C82BE7CE590160D0E8A04242374E5C4B5F307E7412CA8FB2E84BB4F421D6B4C2E08255B2577F55E0667673BD7D7A3D74E083DC19B597A76531135A7C3B1DB93534045E1D52DDB5170ACFAA688922C18764CFE59E3D0578C41A7BD60520CADC58DD9"));
//        pktConfig.getKeyList().add( new WoolworthsPktConfig.CAPKey( "A000000384", "C2", "000003", "01", "010100", "311215", "8BFD70FC05456718B7002F1B889040780089D95969F3160A3999B4E056D2CEFC007D3033B02868670C9EFE48624096C2FD0FA7FDAFDCBB4A63A4CD7106A97BDD8CE3F71F2168AFCB4230F2C33492467A6C182B4BFA76EE605B2FEC4B4519B5A92767DF23805EC8708980E18CB089C065D036AD57D196E88AC1552148FC3B62B771B6B144D28DF5AEE74FC31521B6968909A463EA0184261DD751278A10B7C74668520B253B6A860E54065D2B5677A753"));
//        pktConfig.getKeyList().add( new WoolworthsPktConfig.CAPKey( "A000000384", "C3", "000003", "01", "010100", "311215", "A1A62B6A0393DE4EF4D9D53684C14EAED4A881A26EE64DBD4550EB3581E003281A4117F7BF9E5648FD5E3882BC4AB46B0A5F48CDF5C2D56C7D14A4CD69D337800050C0994974237628EF8D751E1FABBAAD73ED70F60486930B55A5F3C4AEBB2BB1ADAAAE3A374D02F19420BD528AB19C9D09FC41BB8140C40443AE2C5A24593C216E9604BE0DC69BBB1D1F98CA76212D5D1B59DBB90739145C5F98C7DEFECBE91DA4C59FB40F159A260A0B058861A25D0B88D0B3FFD87CEE52DD28C7B1FFE50A49B66116615DC45696B4A61E563B3CDD36E3F2ADCFFB50DBC3E8280412769D00662917096893B1C319E8133A043DDF05AE82A55FAEFEC1DF"));
//
//        // jcb? keys
//        pktConfig.getKeyList().add( new WoolworthsPktConfig.CAPKey( "A000000065", "05", "000003", "01", "010100", "311220", "9C338A8B66142367883B08A7F8D39459CBB00182ECF3722158992C99D69CBD94137EF95BE72E1FDA9E5800242084A5765FE3B00B730E726ADA9A7E219CAEBDC329158D2F88AF1529F3E1D0093A481185F1D1058034B78FE323BD281B02C15F59303CF8E50E7EA3A76DA2E104E363F18EEFFF9802603FD07BB8586783BC209CCC1354BD7EFF94E388682EEF11E7C9A69CFF5747F096557871"));
//        pktConfig.getKeyList().add( new WoolworthsPktConfig.CAPKey( "A000000065", "06", "000003", "01", "010100", "311220", "932720B5CEC708D2E3707344E1D0B341782716124EA1E9913BEC427229A4EC195B44C9BE1B63C072E01ECC30E731DA128C94F0011E004AC950A5D810D89156B755F47F2111FB01897A546AE9355310A166B0FAABDA2121E065FFB94124A2E65245B71D8E2AA373654864DD95DBFB4B62C2197E5BC35AEA20DFCBD24721FF4CFDDB1D49B643A631EEF11E7B867266009E4C2D272BB812D0F46C428E210E4A469580CEB666DCD71DB98849"));
//        pktConfig.getKeyList().add( new WoolworthsPktConfig.CAPKey( "A000000065", "07", "000003", "01", "010100", "311220", "A1081CFA8A068BAFF78BFCBBA8AFE27933670AF925370CAE31939A42976C543910AD706E2DFEBC19C94FFF9B7DD0533548E2757BE64B0D7F6390F854F48F2D99A01136887AD56A139F762D26A1657F7404FC299B6ACB2331D2B5283AB0D385CFD2FFF35AA90FE07AC506B13A66FBD72E1B6FD9D792C13F8F6D5F4C4EDD96ACB9"));
//        pktConfig.getKeyList().add( new WoolworthsPktConfig.CAPKey( "A000000065", "08", "000003", "01", "010100", "311220", "9A20815A3729C2FB032C1A717866EA252E27D7DE539A8DDEA04BD5B3FBE7429C81491E9C16651A1F2D00FD4E62D3D450811F1150ED5C02A741C41458AC80369A8ADFC493898036C9D9B4B62CE91169BE030475A2AB01B9F86E3733E824C41AF02AC9D341E8BEE7E6C7AA80D7B1CA971FAF5BDAF17BB7904F53271605E325EFEF"));
//        pktConfig.getKeyList().add( new WoolworthsPktConfig.CAPKey( "A000000065", "09", "000003", "01", "010100", "311220", "A0BA1E941BA2B11DFB9AC5139041CC58B870A3B328F4712DD844439E6544469FD31106167FE926583CBCED6D573DECF9AF67D09875AF285C189681D4045883031E99A0A0F456DD31857DC58960EC24689F68FECEF88832B389D66D2A0481B14B0E05FD36CC00163FCAABAE73B5273D5F1206D4E246DC8AA1977A685FDD344B0D"));
//        pktConfig.getKeyList().add( new WoolworthsPktConfig.CAPKey( "A000000065", "10", "000003", "01", "010100", "311220", "98B446D87B5BDA8E13104F71687A5EE2E8106375CC64A54A10B88190C27B3812DB87B0D70FB4E9871DC62CEBC38056DA3E6B3957C8A8067DA81CE7C46A42107817D263933F3188BBB7863B2BC41683CE9C1744C5C06EC0D127A8844AA78F1D2E1E1115B036B7B9148BC9DBF58005313D14C866590C58DFE5D1AE6EBB118744AA2067926498C3CE323BDF5D67981E7FBF"));
//        pktConfig.getKeyList().add( new WoolworthsPktConfig.CAPKey( "A000000065", "11", "000003", "01", "010100", "311220", "A2583AA40746E3A63C22478F576D1EFC5FB046135A6FC739E82B55035F71B09BEB566EDB9968DD649B94B6DEDC033899884E908C27BE1CD291E5436F762553297763DAA3B890D778C0F01E3344CECDFB3BA70D7E055B8C760D0179A403D6B55F2B3B083912B183ADB7927441BED3395A199EEFE0DEBD1F5FC3264033DA856F4A8B93916885BD42F9C1F456AAB8CFA83AC574833EB5E87BB9D4C006A4B5346BD9E17E139AB6552D9C58BC041195336485"));
//        pktConfig.getKeyList().add( new WoolworthsPktConfig.CAPKey( "A000000065", "0F", "000003", "01", "010100", "311220", "9EFBADDE4071D4EF98C969EB32AF854864602E515D6501FDE576B310964A4F7C2CE842ABEFAFC5DC9E26A619BCF2614FE07375B9249BEFA09CFEE70232E75FFD647571280C76FFCA87511AD255B98A6B577591AF01D003BD6BF7E1FCE4DFD20D0D0297ED5ECA25DE261F37EFE9E175FB5F12D2503D8CFB060A63138511FE0E125CF3A643AFD7D66DCF9682BD246DDEA1"));
//        pktConfig.getKeyList().add( new WoolworthsPktConfig.CAPKey( "A000000065", "13", "000003", "01", "010100", "311220", "6C1441D28889A5F46413C8F62F3645AAEB30A1521EEF41FD4F3445BFA1AB29F9AC1A74D9A16B93293296CB09162B149BAC22F88AD8F322D684D6B49A12413FC1B6AC70EDEDB18EC1585519A89B50B3D03E14063C2CA58B7C2BA7FB22799A33BCDE6AFCBEB4A7D64911D08D18C47F9BD14A9FAD8805A15DE5A38945A97919B7AB88EFA11A88C0CD92C6EE7DC352AB0746ABF13585913C8A4E04464B77909C6BD94341A8976C4769EA6C0D30A60F4EE8FA19E767B170DF4FA80312DBA61DB645D5D1560873E2674E1F620083F30180BD96CA5890"));
//
//        pktConfig.getKeyList().add( new WoolworthsPktConfig.CAPKey( "A000000333", "0A", "000003", "01", "010100", "311299", "B2AB1B6E9AC55A75ADFD5BBC34490E53C4C3381F34E60E7FAC21CC2B26DD34462B64A6FAE2495ED1DD383B8138BEA100FF9B7A111817E7B9869A9742B19E5C9DAC56F8B8827F11B05A08ECCF9E8D5E85B0F7CFA644EFF3E9B796688F38E006DEB21E101C01028903A06023AC5AAB8635F8E307A53AC742BDCE6A283F585F48EF"));
//        pktConfig.getKeyList().add( new WoolworthsPktConfig.CAPKey( "A000000333", "08", "000003", "01", "010100", "311299", "B61645EDFD5498FB246444037A0FA18C0F101EBD8EFA54573CE6E6A7FBF63ED21D66340852B0211CF5EEF6A1CD989F66AF21A8EB19DBD8DBC3706D135363A0D683D046304F5A836BC1BC632821AFE7A2F75DA3C50AC74C545A754562204137169663CFCC0B06E67E2109EBA41BC67FF20CC8AC80D7B6EE1A95465B3B2657533EA56D92D539E5064360EA4850FED2D1BF"));
//        pktConfig.getKeyList().add( new WoolworthsPktConfig.CAPKey( "A000000333", "09", "000003", "01", "010100", "311299", "EB374DFC5A96B71D2863875EDA2EAFB96B1B439D3ECE0B1826A2672EEEFA7990286776F8BD989A15141A75C384DFC14FEF9243AAB32707659BE9E4797A247C2F0B6D99372F384AF62FE23BC54BCDC57A9ACD1D5585C303F201EF4E8B806AFB809DB1A3DB1CD112AC884F164A67B99C7D6E5A8A6DF1D3CAE6D7ED3D5BE725B2DE4ADE23FA679BF4EB15A93D8A6E29C7FFA1A70DE2E54F593D908A3BF9EBBD760BBFDC8DB8B54497E6C5BE0E4A4DAC29E5"));
//        pktConfig.getKeyList().add( new WoolworthsPktConfig.CAPKey( "A000000333", "0B", "000003", "01", "010100", "311299", "CF9FDF46B356378E9AF311B0F981B21A1F22F250FB11F55C958709E3C7241918293483289EAE688A094C02C344E2999F315A72841F489E24B1BA0056CFAB3B479D0E826452375DCDBB67E97EC2AA66F4601D774FEAEF775ACCC621BFEB65FB0053FC5F392AA5E1D4C41A4DE9FFDFDF1327C4BB874F1F63A599EE3902FE95E729FD78D4234DC7E6CF1ABABAA3F6DB29B7F05D1D901D2E76A606A8CBFFFFECBD918FA2D278BDB43B0434F5D45134BE1C2781D157D501FF43E5F1C470967CD57CE53B64D82974C8275937C5D8502A1252A8A5D6088A259B694F98648D9AF2CB0EFD9D943C69F896D49FA39702162ACB5AF29B90BADE005BC157"));
//
//        pktConfig.setTableVersion( "100131" );
//
//        XmlMapper xmlMapper = new XmlMapper();
//        try {
//            String output = xmlMapper.writeValueAsString(pktConfig);
//            assertNotNull( output );
//            assertEquals( testPktData, output );
//        } catch (JsonProcessingException e) {
//            Timber.w(e);
//            // force failure
//            assertEquals( false, true );
//        }
//    }


    @Test
    public void test_ParsePktBinary() {
        byte[] testData = Util.hexStringToByteArray("000E00011AAB01FADFE95903100140FFE91181BDDFE91805A000000003DFE9190195DFE91A8190BE9E1FA5E9A803852999C4AB432DB28600DCD9DAB76DFAAA47355A0FE37B1508AC6BF38860D3C6C2E5B12A3CAAF2A7005A7241EBAA7771112C74CF9A0634652FBCA0E5980C54A64761EA101A114E0F0B5572ADD57D010B7C9C887E104CA4EE1272DA66D997B9A90B5A6D624AB6C57E73C8F919000EB5F684898EF8C3DBEFB330C62660BED88EA78E909AFF05F6DA627BDFE91B0103DFE91C0101DFE95B040101D007DFE95C041F0CEE07FFE91181ADDFE91805A000000003DFE9190199DFE91A8180AB79FCC9520896967E776E64444E5DCDD6E13611874F3985722520425295EEA4BD0C2781DE7F31CD3D041F565F747306EED62954B17EDABA3A6C5B85A1DE1BEB9A34141AF38FCF8279C9DEA0D5A6710D08DB4124F041945587E20359BAB47B7575AD94262D4B25F264AF33DEDCF28E09615E937DE32EDC03C54445FE7E382777DFE91B0103DFE91C0101DFE95B040101D007DFE95C041F0CEE07FFE91181DDDFE91805A000000003DFE9190192DFE91A81B0996AF56F569187D09293C14810450ED8EE3357397B18A2458EFAA92DA3B6DF6514EC060195318FD43BE9B8F0CC669E3F844057CBDDF8BDA191BB64473BC8DC9A730DB8F6B4EDE3924186FFD9B8C7735789C23A36BA0B8AF65372EB57EA5D89E7D14E9C7B6B5574000E00021AAB01FA60F10885DA16AC923F15AF3758F0F03EBD3C5C2C949CBA306DB44E6A2C076C5F67E281D7EF56785DC4D75945E491F01918800A9E2DC66F60080566CE0DAF8D17EAD46AD8E30A247C9FDFE91B0103DFE91C0101DFE95B040101D007DFE95C041F0CEE07FFE911820125DFE91805A000000003DFE9190194DFE91A81F8ACD2B12302EE644F3F835ABD1FC7A6F62CCE48FFEC622AA8EF062BEF6FB8BA8BC68BBF6AB5870EED579BC3973E121303D34841A796D6DCBC41DBF9E52C4609795C0CCF7EE86FA1D5CB041071ED2C51D2202F63F1156C58A92D38BC60BDF424E1776E2BC9648078A03B36FB554375FC53D57C73F5160EA59F3AFC5398EC7B67758D65C9BFF7828B6B82D4BE124A416AB7301914311EA462C19F771F31B3B57336000DFF732D3B83DE07052D730354D297BEC72871DCCF0E193F171ABA27EE464C6A97690943D59BDABB2A27EB71CEEBDAFA1176046478FD62FEC452D5CA393296530AA3F41927ADFE434A2DF2AE3054F8840657A26E0FC617DFE91B0103DFE91C0101DFE95B040101D007DFE95C041F0CEE07FFE91181ADDFE91805A000000003DFE9190196DFE91A8180B74586D19A207BE6627C5B0AAFBC44A2ECF5A2942D3A26CE19C4FFAEEE920521868922E893E7838225A3947A2614796FB2C0628CE8C11E3825A56D3B1BBAEF783A5C6A81F36F8625395126FA983C5216D3166D48000E00031AAB01FAACDE8A431212FF763A7F79D9EDB7FED76B485DE45BEB829A3D4730848A366D3324C3027032FF8D16A1E44D8DDFE91B0103DFE91C0101DFE95B040101D007DFE95C041F0CEE07FFE91181BDDFE91805A000000003DFE9190151DFE91A8190DB5FA29D1FDA8C1634B04DCCFF148ABEE63C772035C79851D3512107586E02A917F7C7E885E7C4A7D529710A145334CE67DC412CB1597B77AA2543B98D19CF2CB80C522BDBEA0F1B113FA2C86216C8C610A2D58F29CF3355CEB1BD3EF410D1EDD1F7AE0F16897979DE28C6EF293E0A19282BD1D793F1331523FC71A228800468C01A3653D14C6B4851A5C029478E757FDFE91B0103DFE91C0101DFE95B040101D007DFE95C041F0CEE07FFE91181BDDFE91805A000000004DFE91901F3DFE91A819098F0C770F23864C2E766DF02D1E833DFF4FFE92D696E1642F0A88C5694C6479D16DB1537BFE29E4FDC6E6E8AFD1B0EB7EA0124723C333179BF19E93F10658B2F776E829E87DAEDA9C94A8B3382199A350C077977C97AFF08FD11310AC950A72C3CA5002EF513FCCC286E646E3C5387535D509514B3B326E1234F9CB48C36DDD44B416D23654034A66F403BA511C5EFA3DFE91B0103DFE91C0101DFE95B040101D007DFE95C041F0CE407FFE91181ADDFE91805A000000004DFE91901F8DFE91A8180A1F5E1C9BD8650BD43AB6EE56B891EF7459C0A24FA84F912000E00041AAB01FA7D1A6C79D4930F6DB1852E2510F18B61CD354DB83A356BD190B88AB8DF04284D02A4204A7B6CB7C5551977A9B36379CA3DE1A08E69F301C95CC1C20506959275F41723DD5D2925290579E5A95B0DF6323FC8E9273D6F849198C4996209166D9BFC973C361CC826E1DFE91B0103DFE91C0101DFE95B040101D007DFE95C041F0CE407FFE91181ADDFE91805A000000004DFE91901FEDFE91A8180A653EAC1C0F786C8724F737F172997D63D1C3251C44402049B865BAE877D0F398CBFBE8A6035E24AFA086BEFDE9351E54B95708EE672F0968BCD50DCE40F783322B2ABA04EF137EF18ABF03C7DBC5813AEAEF3AA7797BA15DF7D5BA1CBAF7FD520B5A482D8D3FEE105077871113E23A49AF3926554A70FE10ED728CF793B62A1DFE91B0103DFE91C0101DFE95B040101D007DFE95C041F0CE407FFE91181DDDFE91805A000000004DFE91901F1DFE91A81B0A0DCF4BDE19C3546B4B6F0414D174DDE294AABBB828C5A834D73AAE27C99B0B053A90278007239B6459FF0BBCD7B4B9C6C50AC02CE91368DA1BD21AAEADBC65347337D89B68F5C99A09D05BE02DD1F8C5BA20E2F13FB2A27C41D3F85CAD5CF6668E75851EC66EDBF98851FD4E42C44C1D59F5984703B27D5B9F21B8FA0D93279FBBF69E090642909C9EA27F898959541AA6757F5F624104F6E1D3A9532F2A6E51515AEAD1B43B3D7835088A2FAFA000E00051AAB01FA7BE7DFE91B0103DFE91C0101DFE95B040101D007DFE95C041F0CE407FFE91181BDDFE91805A000000004DFE91901FADFE91A8190A90FCD55AA2D5D9963E35ED0F440177699832F49C6BAB15CDAE5794BE93F934D4462D5D12762E48C38BA83D8445DEAA74195A301A102B2F114EADA0D180EE5E7A5C73E0C4E11F67A43DDAB5D55683B1474CC0627F44B8D3088A492FFAADAD4F42422D0E7013536C3C49AD3D0FAE96459B0F6B1B6056538A3D6D44640F94467B108867DEC40FAAECD740C00E2B7A8852DDFE91B0103DFE91C0101DFE95B040101D007DFE95C041F0CE407FFE911820125DFE91805A000000004DFE91901EFDFE91A81F8A191CB87473F29349B5D60A88B3EAEE0973AA6F1A082F358D849FDDFF9C091F899EDA9792CAF09EF28F5D22404B88A2293EEBBC1949C43BEA4D60CFD879A1539544E09E0F09F60F065B2BF2A13ECC705F3D468B9D33AE77AD9D3F19CA40F23DCF5EB7C04DC8F69EBA565B1EBCB4686CD274785530FF6F6E9EE43AA43FDB02CE00DAEC15C7B8FD6A9B394BABA419D3F6DC85E16569BE8E76989688EFEA2DF22FF7D35C043338DEAA982A02B866DE5328519EBBCD6F03CDD686673847F84DB651AB86C28CF1462562C577B853564A290C8556D818531268D25CC98A4CC6A0BDFFFDA2DCCA3A94C998559E307FDDF915006D9A987B07DDAEB3BDFE91B0103DFE91C0101DF000E00061AAB01FAE95B040101D007DFE95C041F0CE407FFE91181DDDFE91805A000000004DFE9190105DFE91A81B0B8048ABC30C90D976336543E3FD7091C8FE4800DF820ED55E7E94813ED00555B573FECA3D84AF6131A651D66CFF4284FB13B635EDD0EE40176D8BF04B7FD1C7BACF9AC7327DFAA8AA72D10DB3B8E70B2DDD811CB4196525EA386ACC33C0D9D4575916469C4E4F53E8E1C912CC618CB22DDE7C3568E90022E6BBA770202E4522A2DD623D180E215BD1D1507FE3DC90CA310D27B3EFCCD8F83DE3052CAD1E48938C68D095AAC91B5F37E28BB49EC7ED597DFE91B0103DFE91C0101DFE95B040101D907DFE95C041F0CE807FFE911820125DFE91805A000000004DFE9190106DFE91A81F8CB26FC830B43785B2BCE37C81ED334622F9622F4C89AAE641046B2353433883F307FB7C974162DA72F7A4EC75D9D657336865B8D3023D3D645667625C9A07A6B7A137CF0C64198AE38FC238006FB2603F41F4F3BB9DA1347270F2F5D8C606E420958C5F7D50A71DE30142F70DE468889B5E3A08695B938A50FC980393A9CBCE44AD2D64F630BB33AD3F5F5FD495D31F37818C1D94071342E07F1BEC2194F6035BA5DED3936500EB82DFDA6E8AFB655B1EF3D0D7EBF86B66DD9F29F6B1D324FE8B26CE38AB2013DD13F611E7A594D675C4432350EA244CC34F3873CBA06592987A1D7E852ADC22EF5A2EE28132031E48F000E00071AAB01FA74037E3B34AB747FDFE91B0103DFE91C0101DFE95B040101D907DFE95C041F0CED07FFE91181DDDFE91805A000000025DFE91901C9DFE91A81B0B362DB5733C15B8797B8ECEE55CB1A371F760E0BEDD3715BB270424FD4EA26062C38C3F4AAA3732A83D36EA8E9602F6683EECC6BAFF63DD2D49014BDE4D6D603CD744206B05B4BAD0C64C63AB3976B5C8CAAF8539549F5921C0B700D5B0F83C4E7E946068BAAAB5463544DB18C63801118F2182EFCC8A1E85E53C2A7AE839A5C6A3CABE73762B70D170AB64AFC6CA482944902611FB0061E09A67ACB77E493D998A0CCF93D81A4F6C0DC6B7DF22E62DBDFE91B0103DFE91C0101DFE95B040101D007DFE95C041F0CE407FFE911820125DFE91805A000000025DFE91901CADFE91A81F8C23ECBD7119F479C2EE546C123A585D697A7D10B55C2D28BEF0D299C01DC65420A03FE5227ECDECB8025FBC86EEBC1935298C1753AB849936749719591758C315FA150400789BB14FADD6EAE2AD617DA38163199D1BAD5D3F8F6A7A20AEF420ADFE2404D30B219359C6A4952565CCCA6F11EC5BE564B49B0EA5BF5B3DC8C5C6401208D0029C3957A8C5922CBDE39D3A564C6DEBB6BD2AEF91FC27BB3D3892BEB9646DCE2E1EF8581EFFA712158AAEC541C0BBB4B3E279D7DA54E45A0ACC3570E712C9F7CDF985CFAFD382AE13A3B214A9E8E1E71AB1EA707895112ABC3000E00081AAB01FAA97D0FCB0AE2EE5C85492B6CFD54885CDD6337E895CC70FB3255E3DFE91B0103DFE91C0101DFE95B040101D007DFE95C041F0CE407FFE91181BDDFE91805A000000384DFE91901C1DFE91A8190AD6E00E5882CEEF578AFB002980FBD901089100B921200D6442C176D93DA5C9399B8427CBE19C1B2E638F5FC78875C82BE7CE590160D0E8A04242374E5C4B5F307E7412CA8FB2E84BB4F421D6B4C2E08255B2577F55E0667673BD7D7A3D74E083DC19B597A76531135A7C3B1DB93534045E1D52DDB5170ACFAA688922C18764CFE59E3D0578C41A7BD60520CADC58DD9DFE91B0103DFE91C0101DFE95B040101D007DFE95C041F0CE407FFE91181DDDFE91805A000000384DFE91901C2DFE91A81B08BFD70FC05456718B7002F1B889040780089D95969F3160A3999B4E056D2CEFC007D3033B02868670C9EFE48624096C2FD0FA7FDAFDCBB4A63A4CD7106A97BDD8CE3F71F2168AFCB4230F2C33492467A6C182B4BFA76EE605B2FEC4B4519B5A92767DF23805EC8708980E18CB089C065D036AD57D196E88AC1552148FC3B62B771B6B144D28DF5AEE74FC31521B6968909A463EA0184261DD751278A10B7C74668520B253B6A860E54065D2B5677A753DFE91B0103DFE91C0101DFE95B040101D007DFE95C041F0CE407FFE911820125DFE91805A000000384DFE91901C3DFE91A81F8A1A62B6A0393DE4E000E00091AAB01FAF4D9D53684C14EAED4A881A26EE64DBD4550EB3581E003281A4117F7BF9E5648FD5E3882BC4AB46B0A5F48CDF5C2D56C7D14A4CD69D337800050C0994974237628EF8D751E1FABBAAD73ED70F60486930B55A5F3C4AEBB2BB1ADAAAE3A374D02F19420BD528AB19C9D09FC41BB8140C40443AE2C5A24593C216E9604BE0DC69BBB1D1F98CA76212D5D1B59DBB90739145C5F98C7DEFECBE91DA4C59FB40F159A260A0B058861A25D0B88D0B3FFD87CEE52DD28C7B1FFE50A49B66116615DC45696B4A61E563B3CDD36E3F2ADCFFB50DBC3E8280412769D00662917096893B1C319E8133A043DDF05AE82A55FAEFEC1DFDFE91B0103DFE91C0101DFE95B040101D007DFE95C041F0CE407FFE91181DDDFE91805A000000065DFE9190111DFE91A81B0A2583AA40746E3A63C22478F576D1EFC5FB046135A6FC739E82B55035F71B09BEB566EDB9968DD649B94B6DEDC033899884E908C27BE1CD291E5436F762553297763DAA3B890D778C0F01E3344CECDFB3BA70D7E055B8C760D0179A403D6B55F2B3B083912B183ADB7927441BED3395A199EEFE0DEBD1F5FC3264033DA856F4A8B93916885BD42F9C1F456AAB8CFA83AC574833EB5E87BB9D4C006A4B5346BD9E17E139AB6552D9C58BC041195336485DFE91B0103DFE91C0101DFE95B040101D007DFE95C041F0CE407FFE91181BDDFE91805A000000065000E000A1AAB01FADFE919010FDFE91A81909EFBADDE4071D4EF98C969EB32AF854864602E515D6501FDE576B310964A4F7C2CE842ABEFAFC5DC9E26A619BCF2614FE07375B9249BEFA09CFEE70232E75FFD647571280C76FFCA87511AD255B98A6B577591AF01D003BD6BF7E1FCE4DFD20D0D0297ED5ECA25DE261F37EFE9E175FB5F12D2503D8CFB060A63138511FE0E125CF3A643AFD7D66DCF9682BD246DDEA1DFE91B0103DFE91C0101DFE95B040101D007DFE95C041F0CE407FFE911820125DFE91805A000000065DFE9190113DFE91A81F8A3270868367E6E29349FC2743EE545AC53BD3029782488997650108524FD051E3B6EACA6A9A6C1441D28889A5F46413C8F62F3645AAEB30A1521EEF41FD4F3445BFA1AB29F9AC1A74D9A16B93293296CB09162B149BAC22F88AD8F322D684D6B49A12413FC1B6AC70EDEDB18EC1585519A89B50B3D03E14063C2CA58B7C2BA7FB22799A33BCDE6AFCBEB4A7D64911D08D18C47F9BD14A9FAD8805A15DE5A38945A97919B7AB88EFA11A88C0CD92C6EE7DC352AB0746ABF13585913C8A4E04464B77909C6BD94341A8976C4769EA6C0D30A60F4EE8FA19E767B170DF4FA80312DBA61DB645D5D1560873E2674E1F620083F30180BD96CA589DFE91B0103DFE91C0101DFE95B040101D007DFE95C041F0CE407FFE91181ADDFE91805A000000333DFE919010ADFE91A8180B2AB1B000E000B1AAB01FA6E9AC55A75ADFD5BBC34490E53C4C3381F34E60E7FAC21CC2B26DD34462B64A6FAE2495ED1DD383B8138BEA100FF9B7A111817E7B9869A9742B19E5C9DAC56F8B8827F11B05A08ECCF9E8D5E85B0F7CFA644EFF3E9B796688F38E006DEB21E101C01028903A06023AC5AAB8635F8E307A53AC742BDCE6A283F585F48EFDFE91B0103DFE91C0101DFE95B040101D007DFE95C041F0C3308FFE91181BDDFE91805A000000333DFE9190108DFE91A8190B61645EDFD5498FB246444037A0FA18C0F101EBD8EFA54573CE6E6A7FBF63ED21D66340852B0211CF5EEF6A1CD989F66AF21A8EB19DBD8DBC3706D135363A0D683D046304F5A836BC1BC632821AFE7A2F75DA3C50AC74C545A754562204137169663CFCC0B06E67E2109EBA41BC67FF20CC8AC80D7B6EE1A95465B3B2657533EA56D92D539E5064360EA4850FED2D1BFDFE91B0103DFE91C0101DFE95B040101D007DFE95C041F0C3308FFE91181DDDFE91805A000000333DFE9190109DFE91A81B0EB374DFC5A96B71D2863875EDA2EAFB96B1B439D3ECE0B1826A2672EEEFA7990286776F8BD989A15141A75C384DFC14FEF9243AAB32707659BE9E4797A247C2F0B6D99372F384AF62FE23BC54BCDC57A9ACD1D5585C303F201EF4E8B806AFB809DB1A3DB1CD112AC884F164A67B99C7D6E5A8A6DF1D3CAE6D7ED3D5BE725B2DE4ADE23FA679BF4EB15000E000C1AAB01FAA93D8A6E29C7FFA1A70DE2E54F593D908A3BF9EBBD760BBFDC8DB8B54497E6C5BE0E4A4DAC29E5DFE91B0103DFE91C0101DFE95B040101D007DFE95C041F0C3308FFE911820125DFE91805A000000333DFE919010BDFE91A81F8CF9FDF46B356378E9AF311B0F981B21A1F22F250FB11F55C958709E3C7241918293483289EAE688A094C02C344E2999F315A72841F489E24B1BA0056CFAB3B479D0E826452375DCDBB67E97EC2AA66F4601D774FEAEF775ACCC621BFEB65FB0053FC5F392AA5E1D4C41A4DE9FFDFDF1327C4BB874F1F63A599EE3902FE95E729FD78D4234DC7E6CF1ABABAA3F6DB29B7F05D1D901D2E76A606A8CBFFFFECBD918FA2D278BDB43B0434F5D45134BE1C2781D157D501FF43E5F1C470967CD57CE53B64D82974C8275937C5D8502A1252A8A5D6088A259B694F98648D9AF2CB0EFD9D943C69F896D49FA39702162ACB5AF29B90BADE005BC157DFE91B0103DFE91C0101DFE95B040101D007DFE95C041F0C3308FFE91181ADDFE91805A000000152DFE919015ADFE91A8180EDD8252468A705614B4D07DE3211B30031AEDB6D33A4315F2CFF7C97DB918993C2DC02E79E2FF8A2683D5BBD0F614BC9AB360A448283EF8B9CF6731D71D6BE939B7C5D0B0452D660CF24C21C47CAC8E26948C8EED8E3D00C016828D642816E658DC2CFC61E7E7D7740633BEFE34107C1FB55DEA7FAAE000E000D1AAB01FAA2B25E85BED948893D07DFE91B0103DFE91C0101DFE95B040101D007DFE95C041F0C3308FFE91181BDDFE91805A000000152DFE919015BDFE91A8190D3F45D065D4D900F68B2129AFA38F549AB9AE4619E5545814E468F382049A0B9776620DA60D62537F0705A2C926DBEAD4CA7CB43F0F0DD809584E9F7EFBDA3778747BC9E25C5606526FAB5E491646D4DD28278691C25956C8FED5E452F2442E25EDC6B0C1AA4B2E9EC4AD9B25A1B836295B823EDDC5EB6E1E0A3F41B28DB8C3B7E3E9B5979CD7E079EF024095A1D19DDDFE91B0103DFE91C0101DFE95B040101D007DFE95C041F0C3308FFE91181DDDFE91805A000000152DFE919015CDFE91A81B0833F275FCF5CA4CB6F1BF880E54DCFEB721A316692CAFEB28B698CAECAFA2B2D2AD8517B1EFB59DDEFC39F9C3B33DDEE40E7A63C03E90A4DD261BC0F28B42EA6E7A1F307178E2D63FA1649155C3A5F926B4C7D7C258BCA98EF90C7F4117C205E8E32C45D10E3D494059D2F2933891B979CE4A831B301B0550CDAE9B67064B31D8B481B85A5B046BE8FFA7BDB58DC0D7032525297F26FF619AF7F15BCEC0C92BCDCBC4FB207D115AA65CD04C1CF982191DFE91B0103DFE91C0101DFE95B040101D007DFE95C041F0C3308FFE911820125DFE91805A000000152DFE919015DDFE91A81F8AD938EA9888E5155F8CD272749172B3A8C504C17460EFA0BED000E000E1AAB00F97CBC5FD32C4A80FD810312281B5A35562800CDC325358A9639C501A537B7AE43DF263E6D232B811ACDB6DDE979D55D6C911173483993A423A0A5B1E1A70237885A241B8EEBB5571E2D32B41F9CC5514DF83F0D69270E109AF1422F985A52CCE04F3DF269B795155A68AD2D6B660DDCD759F0A5DA7B64104D22C2771ECE7A5FFD40C774E441379D1132FAF04CDF55B9504C6DCE9F61776D81C7C45F19B9EFB3749AC7D486A5AD2E781FA9D082FB2677665B99FA5F1553135A1FD2A2A9FBF625CA84A7D736521431178F13100A2516F9A43CE095B032B886C7A6AB126E203BE7DFE91B0103DFE91C0101DFE95B040101D007DFE95C041F0C33080000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000");
        byte[] pattern = { 0x1A, (byte)0xAB };
        assertEquals(14, countPatternOccurrences(testData, pattern));
        WoolworthsPktConfig pktConfig = parseFromBytes(testData);

        assertNotNull(pktConfig);
        assertEquals( "100140", pktConfig.getTableVersion() );
        assertEquals( 30, pktConfig.getKeyList().size() );

        assertEquals( "01", pktConfig.getKeyList().get(0).getAlgorithm() );
        assertEquals( "010100", pktConfig.getKeyList().get(0).getDateEffective() );
        assertEquals( "311230", pktConfig.getKeyList().get(0).getDateExpiry() );
        assertEquals( "03", pktConfig.getKeyList().get(0).getExponent() );
        assertEquals( "95", pktConfig.getKeyList().get(0).getIndex() );
        assertEquals( "BE9E1FA5E9A803852999C4AB432DB28600DCD9DAB76DFAAA47355A0FE37B1508AC6BF38860D3C6C2E5B12A3CAAF2A7005A7241EBAA7771112C74CF9A0634652FBCA0E5980C54A64761EA101A114E0F0B5572ADD57D010B7C9C887E104CA4EE1272DA66D997B9A90B5A6D624AB6C57E73C8F919000EB5F684898EF8C3DBEFB330C62660BED88EA78E909AFF05F6DA627B", pktConfig.getKeyList().get(0).getModulus() );
        assertEquals( "A000000003", pktConfig.getKeyList().get(0).getRid() );

        assertEquals( "01", pktConfig.getKeyList().get(1).getAlgorithm() );
        assertEquals( "010100", pktConfig.getKeyList().get(1).getDateEffective() );
        assertEquals( "311230", pktConfig.getKeyList().get(1).getDateExpiry() );
        assertEquals( "03", pktConfig.getKeyList().get(1).getExponent() );
        assertEquals( "99", pktConfig.getKeyList().get(1).getIndex() );
        assertEquals( "AB79FCC9520896967E776E64444E5DCDD6E13611874F3985722520425295EEA4BD0C2781DE7F31CD3D041F565F747306EED62954B17EDABA3A6C5B85A1DE1BEB9A34141AF38FCF8279C9DEA0D5A6710D08DB4124F041945587E20359BAB47B7575AD94262D4B25F264AF33DEDCF28E09615E937DE32EDC03C54445FE7E382777", pktConfig.getKeyList().get(1).getModulus() );
        assertEquals( "A000000003", pktConfig.getKeyList().get(1).getRid() );

        assertEquals( "01", pktConfig.getKeyList().get(29).getAlgorithm() );
        assertEquals( "010100", pktConfig.getKeyList().get(29).getDateEffective() );
        assertEquals( "311299", pktConfig.getKeyList().get(29).getDateExpiry() );
        assertEquals( "03", pktConfig.getKeyList().get(29).getExponent() );
        assertEquals( "5D", pktConfig.getKeyList().get(29).getIndex() );
        assertEquals( "AD938EA9888E5155F8CD272749172B3A8C504C17460EFA0BED7CBC5FD32C4A80FD810312281B5A35562800CDC325358A9639C501A537B7AE43DF263E6D232B811ACDB6DDE979D55D6C911173483993A423A0A5B1E1A70237885A241B8EEBB5571E2D32B41F9CC5514DF83F0D69270E109AF1422F985A52CCE04F3DF269B795155A68AD2D6B660DDCD759F0A5DA7B64104D22C2771ECE7A5FFD40C774E441379D1132FAF04CDF55B9504C6DCE9F61776D81C7C45F19B9EFB3749AC7D486A5AD2E781FA9D082FB2677665B99FA5F1553135A1FD2A2A9FBF625CA84A7D736521431178F13100A2516F9A43CE095B032B886C7A6AB126E203BE7", pktConfig.getKeyList().get(29).getModulus() );
        assertEquals( "A000000152", pktConfig.getKeyList().get(29).getRid() );
    }

    @Test
    public void test_ParsePktXml(){
        WoolworthsPktConfig pktConfig = parse.parseFromString(testPktData, WoolworthsPktConfig.class);
        assertNotNull(pktConfig);
        assertNotNull(pktConfig.getTableVersion());
        assertEquals( "100131", pktConfig.getTableVersion() );
        assertEquals( 43, pktConfig.getKeyList().size() );
        assertNotNull( pktConfig.getKeyList().get(0).getAlgorithm() );
        assertNotNull( pktConfig.getKeyList().get(0).getDateEffective() );
        assertNotNull( pktConfig.getKeyList().get(0).getDateExpiry() );
        assertNotNull( pktConfig.getKeyList().get(0).getExponent() );
        assertNotNull( pktConfig.getKeyList().get(0).getIndex() );
        assertNotNull( pktConfig.getKeyList().get(0).getModulus() );
        assertNotNull( pktConfig.getKeyList().get(0).getRid() );

        assertEquals( "01", pktConfig.getKeyList().get(0).getAlgorithm() );
        assertEquals( "010100", pktConfig.getKeyList().get(0).getDateEffective() );
        assertEquals( "311215", pktConfig.getKeyList().get(0).getDateExpiry() );
        assertEquals( "000003", pktConfig.getKeyList().get(0).getExponent() );
        assertEquals( "95", pktConfig.getKeyList().get(0).getIndex() );
        assertEquals( "BE9E1FA5E9A803852999C4AB432DB28600DCD9DAB76DFAAA47355A0FE37B1508AC6BF38860D3C6C2E5B12A3CAAF2A7005A7241EBAA7771112C74CF9A0634652FBCA0E5980C54A64761EA101A114E0F0B5572ADD57D010B7C9C887E104CA4EE1272DA66D997B9A90B5A6D624AB6C57E73C8F919000EB5F684898EF8C3DBEFB330C62660BED88EA78E909AFF05F6DA627B", pktConfig.getKeyList().get(0).getModulus() );
        assertEquals( "A000000003", pktConfig.getKeyList().get(0).getRid() );
    }

    @Test
    public void test_MapWoolworthsEpatToEmvCfg() {
        WoolworthsEpatConfig ePatParams = parse.parseFromString(testData, WoolworthsEpatConfig.class);
        assertNotNull( ePatParams );
        WoolworthsPktConfig pktConfig = parse.parseFromString(testPktData, WoolworthsPktConfig.class);
        assertNotNull( pktConfig );

        EmvCfg emvCfg = parseJson.parseFromString(testCfgEmvJson, EmvCfg.class);

        emvCfg = WoolworthsCfgMapper.epatAndPktToEmvCfg( emvCfg, ePatParams, pktConfig);
        assertNotNull(emvCfg);
        assertEquals( "100198", emvCfg.getEpatTableVersion() );
        assertEquals( "1.0.0", emvCfg.getVersion() );
    }

    @Test
    public void test_MapWoolworthsEpatToCtlsCfg() {
        WoolworthsEpatConfig ePatParams = parse.parseFromString(testData, WoolworthsEpatConfig.class);
        assertNotNull( ePatParams );
        WoolworthsPktConfig pktConfig = parse.parseFromString(testPktData, WoolworthsPktConfig.class);
        assertNotNull( pktConfig );

        // parse JSON file data
        CtlsCfg ctlsCfg = parseJson.parseFromString(testCfgCtlsEmvJson, CtlsCfg.class);

        ctlsCfg = WoolworthsCfgMapper.epatAndPktToCtlsCfg( ctlsCfg, ePatParams, pktConfig);
        assertNotNull(ctlsCfg);
        assertEquals( "100198", ctlsCfg.getEpatTableVersion() );
        assertEquals( "2.0.0", ctlsCfg.getVersion() );
    }
}
