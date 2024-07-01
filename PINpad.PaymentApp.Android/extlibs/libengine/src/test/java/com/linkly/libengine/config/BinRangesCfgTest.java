package com.linkly.libengine.config;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

import com.linkly.libbins.BinRanges;
import com.linkly.libconfig.cpat.CardProductCfg;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BinRangesCfgTest {
    @Mock
    private PayCfg mockPayCfg;
    // real instance
    BinRangesCfg binRangesCfg;

    List<BinRanges.BinRange> binRanges = new ArrayList<>();

    private List<CardProductCfg> getCardCfg(){
        CardProductCfg plbTrue = new CardProductCfg();
        plbTrue.setProductLevelBlocking(true);

        CardProductCfg plbFalse = new CardProductCfg();
        plbFalse.setProductLevelBlocking(false);

        return Arrays.asList(
                plbFalse,
                plbTrue,
                plbFalse
        );
    }

    private final List<CardProductCfg> cardProductCfgs = getCardCfg();

    @Before
    public void setUp() throws Exception {
        openMocks(this);

        binRangesCfg = new BinRangesCfg();
        BinRanges binObj = new BinRanges();

        binObj.addIinRange(binRanges, 0, "4966920-4966929,4934140-4934149,4931300-4931309,4902920-4902929,4899820-4899829,4899810-4899819,4889030-4889039,4860400-4860409,4838670-4838679,4838240-4838249,4838230-4838239,4828870-4828879,4828850-4828859,4763570-4763579,4763390-4763399,4763300-4763309,4726970-4726979,4726960-4726969,4726950-4726959,4726940-4726949,4726900-4726909,4726790-4726799,4726780-4726789,4726600-4726609,4722480-4722489,4705680-4705689,4705640-4705649,4682290-4682299,4661680-4661689,4661440-4661449,4645790-4645799,4645530-4645539,4639250-4639259,4629490-4629499,4624820-4624829,4622630-4622639,4622390-4622399,4619770-4619779,4619760-4619769,4619740-4619749,4619730-4619739,4619670-4619679,4619610-4619619,4588580-4588589,4585950-4585959,4585940-4585949,4585240-4585249,4577300-4577309,4564790-4564799,4564750-4564759,4564740-4564749,4564410-4564419,4564320-4564329,4564300-4564309,4564030-4564039,4560610-4560619,4560040-4560049,4537670-4537679,4537510-4537519,4531510-4531519,4524800-4524809,4524790-4524799,4507880-4507889,4506060-4506069,4450880-4450889,4450140-4450149,4450120-4450129,4434990-4434999,4434960-4434969,4434950-4434959,4434890-4434899,4434880-4434889,4434860-4434869,4434850-4434859,4434820-4434829,4434810-4434819,4434800-4434809,4434780-4434789,4434770-4434779,4434750-4434759,4434740-4434749,4434690-4434699,4434670-4434679,4434630-4434639,4434610-4434619,4434600-4434609,4434580-4434589,4434550-4434559,4434530-4434539,4434520-4434529,4434510-4434519,4434500-4434509,4434490-4434499,4434480-4434489,4434470-4434479,4434460-4434469,4434450-4434459,4434420-4434429,4434410-4434419,4434380-4434389,4434370-4434379,4434360-4434369,4434320-4434329,4434310-4434319,4434270-4434279,4434250-4434259,4434210-4434219,4434200-4434209,4434180-4434189,4434170-4434179,4434160-4434169,4434150-4434159,4434140-4434149,4434130-4434139,4434120-4434129,4434110-4434119,4434100-4434109,4434080-4434089,4434050-4434059,4434020-4434029,4434010-4434019,4430840-4430849,4388750-4388759,4373810-4373819,4365310-4365319,4349680-4349689,4337700-4337709,4336960-4336969,4336170-4336179,4336160-4336169,4293210-4293219,4251350-4251359,4239540-4239549,4239530-4239539,4154360-4154369,4133120-4133129,4133030-4133039,4133020-4133029,4097710-4097719,4089670-4089679,4065870-4065879,4062730-4062739,4054970-4054979,4049400-4049409,4048870-4048879,4043420-4043429,4043400-4043409,4041370-4041379,4029930-4029939,4029790-4029799,4026080-4026089,4017950-4017959,4006770-4006779,",
                "Visa Debit");
        binObj.addIinRange(binRanges, 1, "4940970-4940979,4940530-4940539,4940520-4940529,4903270-4903279,4902420-4902429,4889500-4889509,4889490-4889499,4838990-4838999,4838980-4838989,4838220-4838229,4838200-4838209,4838050-4838059,4837890-4837899,4837880-4837889,4837400-4837409,4828830-4828839,4813630-4813639,4782810-4782819,4782800-4782809,4782790-4782799,4773610-4773619,4773600-4773609,4726930-4726939,4726910-4726919,4724770-4724779,4724370-4724379,4724360-4724369,4722450-4722459,4721340-4721349,4715720-4715729,4715270-4715279,4715140-4715149,4685250-4685259,4685240-4685249,4661690-4661699,4637970-4637979,4637960-4637969,4637840-4637849,4637830-4637839,4637820-4637829,4629500-4629509,4624810-4624819,4619570-4619579,4619560-4619569,4619550-4619559,4619530-4619539,4610760-4610769,4601840-4601849,4573560-4573569,4572330-4572339,4572320-4572329,4564940-4564949,4564890-4564899,4564870-4564879,4564850-4564859,4564820-4564829,4564800-4564809,4564720-4564729,4564710-4564719,4564690-4564699,4564680-4564689,4564670-4564679,4564660-4564669,4564650-4564659,4564640-4564649,4564630-4564639,4564620-4564629,4564610-4564619,4564570-4564579,4564500-4564509,4564480-4564489,4564430-4564439,4564420-4564429,4564380-4564389,4564360-4564369,4564090-4564099,4560750-4560759,4557040-4557049,4557030-4557039,4557020-4557029,4557010-4557019,4546050-4546059,4532240-4532249,4531500-4531509,4530300-4530309,4509490-4509499,4506470-4506479,4450100-4450109,4434760-4434769,4426450-4426459,4408160-4408169,4408150-4408159,4392420-4392429,4392410-4392419,4392400-4392409,4392390-4392399,4377320-4377329,4377310-4377319,4377300-4377309,4352190-4352199,4352160-4352169,4352120-4352129,4352110-4352119,4344020-4344029,4336870-4336879,4323870-4323879,4303300-4303309,4297620-4297629,4297610-4297619,4293180-4293189,4293170-4293179,4265340-4265349,4265300-4265309,4265290-4265299,4260110-4260119,4202740-4202749,4182430-4182439,4162390-4162399,4162380-4162389,4147260-4147269,4141460-4141469,4135420-4135429,4133390-4133399,4133370-4133379,4133350-4133359,4133340-4133349,4133330-4133339,4133320-4133329,4133300-4133309,4133290-4133299,4133280-4133289,4133260-4133269,4133250-4133259,4133240-4133249,4133230-4133239,4133220-4133229,4133210-4133219,4133090-4133099,4133060-4133069,4129780-4129789,4109750-4109759,4097740-4097749,4097020-4097029,4095820-4095829,4093490-4093499,4093410-4093419,4072640-4072649,4072200-4072209,4054980-4054989,4052210-4052219,4043410-4043419,4015880-4015889,4006090-4006099,400000-491100,",
                "Visa Credit");
        binObj.addIinRange(binRanges, 2, "40-49,",
                "Visa Catchall");
        binObj.addIinRange(binRanges, 3, "30-39,",
                "No card config");


        when(mockPayCfg.getCards()).thenReturn(cardProductCfgs);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testNoMatch() {
        assertEquals(-1,binRangesCfg.searchBinRanges(mockPayCfg, binRanges, "99291231****3123=291912931203712377"));
    }

    @Test
    public void testMatchVisaDebit() {
        assertNotEquals(-1,binRangesCfg.searchBinRanges(mockPayCfg, binRanges, "423953******3123=291912931203712377"));
    }

    @Test
    public void testMatchVisaCredit() {
        assertEquals(1,binRangesCfg.searchBinRanges(mockPayCfg, binRanges, "491100******3123=291912931203712377"));
    }

    @Test
    public void testMatchVisaCatchall() {
        assertEquals(2,binRangesCfg.searchBinRanges(mockPayCfg, binRanges, "499003******3123=291912931203712377"));
    }

    @Test
    public void testNoCardConfig() {
        assertEquals(-1,binRangesCfg.searchBinRanges(mockPayCfg, binRanges, "399003******3123=291912931203712377"));
    }

    @Test
    public void testLowRangeLimit() {
        assertEquals(1, binRangesCfg.searchBinRanges(mockPayCfg, binRanges, "4940970000000000=291912931203712377"));
    }

    @Test
    public void testHighRangeLimit() {
        assertEquals(1, binRangesCfg.searchBinRanges(mockPayCfg, binRanges, "4940979999999999=291912931203712377"));
    }

    @Test
    public void test19DigitPan() {
        assertEquals(1, binRangesCfg.searchBinRanges(mockPayCfg, binRanges, "4940979999999999999=291912931203712377"));
    }

    @Test
    public void testNoMatchLongPan() {
        assertEquals(-1, binRangesCfg.searchBinRanges(mockPayCfg, binRanges, "9999999999999999999=291912931203712377"));
    }

    @Test
    public void testStartsWithZero() {
        assertEquals(-1, binRangesCfg.searchBinRanges(mockPayCfg, binRanges, "0999999999999999999=291912931203712377"));
    }
}