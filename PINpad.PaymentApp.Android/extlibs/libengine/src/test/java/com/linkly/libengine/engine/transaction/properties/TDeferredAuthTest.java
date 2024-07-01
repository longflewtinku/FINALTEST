package com.linkly.libengine.engine.transaction.properties;

import static com.linkly.libengine.engine.transactions.properties.TCard.CaptureMethod.CTLS;
import static com.linkly.libengine.engine.transactions.properties.TCard.CaptureMethod.ICC;
import static com.linkly.libengine.engine.transactions.properties.TCard.CaptureMethod.MANUAL;
import static com.linkly.libengine.engine.transactions.properties.TCard.CaptureMethod.NOT_CAPTURED;
import static com.linkly.libengine.engine.transactions.properties.TCard.CaptureMethod.SWIPED;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

import com.linkly.libconfig.cpat.CardProductCfg;
import com.linkly.libengine.config.Config;
import com.linkly.libengine.dependencies.IDependency;
import com.linkly.libengine.engine.Engine;
import com.linkly.libengine.engine.reporting.ReconciliationDao;
import com.linkly.libengine.engine.reporting.ReconciliationManager;
import com.linkly.libengine.engine.transactions.TransRec;
import com.linkly.libengine.engine.transactions.properties.TCard;
import com.linkly.libengine.engine.transactions.properties.TDeferredAuth;
import com.linkly.libmal.IMal;
import com.linkly.libsecapp.CtlsCfg;
import com.linkly.libsecapp.EmvCfg;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.ArrayList;
import java.util.List;

public class TDeferredAuthTest {

    @Mock
    IDependency dependency;

    @Mock
    TransRec transRec;

    @Mock
    ReconciliationDao reconciliationDao;
    @Mock
    TCard cardInfo;
    @Mock
    Config config;

    @Mock
    IMal mal;

    @Mock
    EmvCfg emvConfig;

    @Mock
    CtlsCfg ctlsConfig;
    @Mock
    CardProductCfg cardProductCfg;

    @Rule //initMocks
    public final MockitoRule rule = MockitoJUnit.rule();

    @Before
    public void setUp() throws Exception {
        openMocks(this);

        ReconciliationManager.reconciliationDao = reconciliationDao;

        when(dependency.getConfig()).thenReturn(config);
        Engine.setDep(dependency);

        when(transRec.getCard()).thenReturn(cardInfo);

        when(config.getEmvCfg()).thenReturn(emvConfig);
        when(config.getCtlsCfg()).thenReturn(ctlsConfig);

        List<EmvCfg.EmvScheme> emvSchemeList = new ArrayList<>();


        List<EmvCfg.EmvAid> aidListVisa = new ArrayList<>();
        EmvCfg.EmvAid aid1 = new EmvCfg.EmvAid();
        aid1.setAid("A000000003"); // visa
        aidListVisa.add(aid1);

        List<EmvCfg.EmvAid> aidListMC = new ArrayList<>();
        EmvCfg.EmvAid aid2 = new EmvCfg.EmvAid();
        aid2.setAid("A000000004"); // mastercard
        aidListMC.add(aid2);


        EmvCfg.EmvScheme scheme1Disabled = new EmvCfg.EmvScheme();
        scheme1Disabled.setDeferredAuthEnabled(false);
        scheme1Disabled.setAids(aidListVisa);
        emvSchemeList.add(scheme1Disabled);

        EmvCfg.EmvScheme scheme2Enabled = new EmvCfg.EmvScheme();
        scheme2Enabled.setDeferredAuthEnabled(true);
        scheme2Enabled.setAids(aidListMC);
        emvSchemeList.add(scheme2Enabled);

        when(emvConfig.getSchemes()).thenReturn(emvSchemeList);

        // msr config mock
        when(cardInfo.getCardsConfig(dependency.getPayCfg())).thenReturn(cardProductCfg);

        List<CtlsCfg.CtlsAid> aidListCtls = new ArrayList<>();

        CtlsCfg.CtlsAid ctls1 = new CtlsCfg.CtlsAid();
        ctls1.setDeferredAuthEnabled(false);
        ctls1.setAid("A000000003"); // visa
        aidListCtls.add(ctls1);

        CtlsCfg.CtlsAid ctls2 = new CtlsCfg.CtlsAid();
        ctls2.setDeferredAuthEnabled(true);
        ctls2.setAid("A000000004"); // mastercard
        aidListCtls.add(ctls2);

        when(ctlsConfig.getAids()).thenReturn(aidListCtls);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testDeferredAuthCaptureMethodNotSet() {
        when(cardInfo.getCaptureMethod()).thenReturn(NOT_CAPTURED);
        boolean result = TDeferredAuth.getDeferredAuthConfigFlag(transRec, dependency.getPayCfg());
        assertFalse(result);
    }

    @Test
    public void testConfigInvalid() {
        when(emvConfig.getSchemes()).thenReturn(null);
        when(cardInfo.getCaptureMethod()).thenReturn(ICC);
        boolean result = TDeferredAuth.getDeferredAuthConfigFlag(transRec, dependency.getPayCfg());
        assertFalse(result);
    }

    @Test
    public void testIccDeferredAuthDisabled() {
        when(cardInfo.getCaptureMethod()).thenReturn(ICC);
        when(cardInfo.getAid()).thenReturn("A000000003");
        boolean result = TDeferredAuth.getDeferredAuthConfigFlag(transRec, dependency.getPayCfg());
        assertFalse(result);
    }

    @Test
    public void testIccDeferredAuthEnabled() {
        when(cardInfo.getCaptureMethod()).thenReturn(ICC);
        when(cardInfo.getAid()).thenReturn("A000000004");
        boolean result = TDeferredAuth.getDeferredAuthConfigFlag(transRec, dependency.getPayCfg());
        assertTrue(result);
    }

    @Test
    public void testSwipeDeferredAuthEnabled() {
        when(cardInfo.getCaptureMethod()).thenReturn(SWIPED);
        when(cardInfo.getCardIndex()).thenReturn(0);
        when(cardProductCfg.isDeferredAuthEnabled()).thenReturn(true);
        boolean result = TDeferredAuth.getDeferredAuthConfigFlag(transRec, dependency.getPayCfg());
        assertTrue(result);
    }

    @Test
    public void testSwipeDeferredAuthDisabled() {
        when(cardInfo.getCaptureMethod()).thenReturn(SWIPED);
        when(cardInfo.getCardIndex()).thenReturn(0);
        when(cardProductCfg.isDeferredAuthEnabled()).thenReturn(false);
        boolean result = TDeferredAuth.getDeferredAuthConfigFlag(transRec, dependency.getPayCfg());
        assertFalse(result);
    }

    @Test
    public void testCtlsDeferredAuthEnabled() {
        when(cardInfo.getCaptureMethod()).thenReturn(CTLS);
        when(cardInfo.getAid()).thenReturn("A000000003");
        boolean result = TDeferredAuth.getDeferredAuthConfigFlag(transRec, dependency.getPayCfg());
        assertFalse(result);
    }

    @Test
    public void testCtlsDeferredAuthDisabled() {
        when(cardInfo.getCaptureMethod()).thenReturn(CTLS);
        when(cardInfo.getAid()).thenReturn("A000000004");
        boolean result = TDeferredAuth.getDeferredAuthConfigFlag(transRec, dependency.getPayCfg());
        assertTrue(result);
    }

    @Test
    public void testManualEntryEnabled() {
        when(cardInfo.getCaptureMethod()).thenReturn(MANUAL);
        when(cardInfo.getCardIndex()).thenReturn(0);
        when(cardProductCfg.isDeferredAuthEnabled()).thenReturn(true);
        boolean result = TDeferredAuth.getDeferredAuthConfigFlag(transRec, dependency.getPayCfg());
        assertTrue(result);
    }
}