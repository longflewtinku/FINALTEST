package com.linkly.libengine.Check;

import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.linkly.libconfig.cpat.CardProductCfg;
import com.linkly.libengine.action.TransactionCanceller;
import com.linkly.libengine.action.TransactionDecliner;
import com.linkly.libengine.action.check.CheckBINRange;
import com.linkly.libengine.config.BinRangesCfg;
import com.linkly.libengine.config.PayCfg;
import com.linkly.libengine.dependencies.IDependency;
import com.linkly.libengine.engine.protocol.IProto;
import com.linkly.libengine.engine.transactions.TransRec;
import com.linkly.libengine.engine.transactions.properties.TCard;
import com.linkly.libui.IUIDisplay;
import com.linkly.libui.UIScreenDef;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class CheckBINRangeTest {
    @Mock(answer = RETURNS_DEEP_STUBS)
    IDependency dependency;
    @Mock
    CardProductCfg cardProductConfig;
    @Mock
    IUIDisplay uiDisplay;
    @Mock(answer = RETURNS_DEEP_STUBS)
    TransRec trans;
    private boolean cancelOnFailure;

    @Mock
    PayCfg payCfg;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(dependency.getCurrentTransaction()).thenReturn(trans);
        when(trans.getCard().getCardIndex()).thenReturn(5);
        when(dependency.getBinRangesCfg().getCardProductCfg(any(), any(Integer.class))).thenReturn(cardProductConfig);
        when(dependency.getUI()).thenReturn(uiDisplay);
        when(dependency.getPayCfg()).thenReturn(payCfg);
        cancelOnFailure = true;
    }

    @Test
    public void testCardRejectedWhenNoStoredIndexAndInvalidPan() {
        int invalidCardIndex = -1;
        String invalidLengthPan = "12345678";
        when(trans.getCard().getCardIndex()).thenReturn(invalidCardIndex);
        when(trans.getCard().getPan()).thenReturn(invalidLengthPan);

        Assert.assertFalse(CheckBINRange.runBinRangeChecking(dependency, cancelOnFailure));
        verify(uiDisplay).showScreen(UIScreenDef.CARD_NOT_READ_PROP);
        verify(uiDisplay).getResultCode(IUIDisplay.ACTIVITY_ID.ACT_INFORMATION, IUIDisplay.SHORT_TIMEOUT);
        verify(dependency.getWorkflowEngine()).setNextAction(TransactionCanceller.class);
    }

    @Test
    public void testCardRejectedWhenNoStoredIndexAndMissingCardIndex() {
        int invalidCardIndex = -1;
        String validLengthPan = "1234567898765432";
        when(trans.getCard().getCardIndex()).thenReturn(invalidCardIndex);
        when(trans.getCard().getPan()).thenReturn(validLengthPan);
        when(dependency.getBinRangesCfg().getCardsCfgIndex(payCfg, validLengthPan)).thenReturn(invalidCardIndex);

        Assert.assertFalse(CheckBINRange.runBinRangeChecking(dependency, cancelOnFailure));
        verify(uiDisplay).showScreen(UIScreenDef.CARD_TYPE_NOT_ALLOWED);
    }

    @Test
    public void testFullTrackIsRejectedWhenGreaterThan40() {
        String stringLength41 = "Hello,this is a string with 41 characters";
        when(trans.getCard().getTrack2()).thenReturn(stringLength41);

        Assert.assertFalse(CheckBINRange.runBinRangeChecking(dependency, cancelOnFailure));
        verify(uiDisplay).showScreen(UIScreenDef.CARD_NOT_ACCEPTED);
        verify(dependency.getProtocol()).setInternalRejectReason(trans, IProto.RejectReasonType.CARD_NOT_ACCEPTED);
        verify(dependency.getWorkflowEngine()).setNextAction(TransactionCanceller.class);
    }

    @Test
    public void testCardNotAllowedWhenDisabled() {
        when(cardProductConfig.isDisabled()).thenReturn(true);

        Assert.assertFalse(CheckBINRange.runBinRangeChecking(dependency, cancelOnFailure));
        verify(uiDisplay).showScreen(UIScreenDef.CARD_TYPE_NOT_ALLOWED);
        verify(dependency.getProtocol()).setInternalRejectReason(trans, IProto.RejectReasonType.CARD_TYPE_NOT_ALLOWED);
        verify(dependency.getWorkflowEngine()).setNextAction(TransactionCanceller.class);
    }

    @Test
    public void testCardNotAllowedWhenRejectCTLS() {
        when(trans.getCard().getCaptureMethod()).thenReturn(TCard.CaptureMethod.CTLS);
        when(cardProductConfig.isRejectCtls()).thenReturn(true);

        Assert.assertFalse(CheckBINRange.runBinRangeChecking(dependency, cancelOnFailure));
        verify(uiDisplay).showScreen(UIScreenDef.CARD_TYPE_NOT_ALLOWED);
        verify(dependency.getProtocol()).setInternalRejectReason(trans, IProto.RejectReasonType.CARD_TYPE_NOT_ALLOWED);
        verify(dependency.getWorkflowEngine()).setNextAction(TransactionDecliner.class);
    }

    @Test
    public void testCardNotAllowedWhenRejectEMV() {
        when(trans.getCard().getCaptureMethod()).thenReturn(TCard.CaptureMethod.ICC);
        when(cardProductConfig.isRejectEmv()).thenReturn(true);

        Assert.assertFalse(CheckBINRange.runBinRangeChecking(dependency, cancelOnFailure));
        verify(uiDisplay).showScreen(UIScreenDef.CARD_TYPE_NOT_ALLOWED);
        verify(dependency.getProtocol()).setInternalRejectReason(trans, IProto.RejectReasonType.CARD_TYPE_NOT_ALLOWED);
        verify(dependency.getWorkflowEngine()).setNextAction(TransactionDecliner.class);
    }

    @Test
    public void testCardRejectedWhenBlockedByProductLevelBlocking() {
        when(trans.getTagDataFromPos().getPLB()).thenReturn("1");
        when(cardProductConfig.isProductLevelBlocking()).thenReturn(true);

        Assert.assertFalse(CheckBINRange.runBinRangeChecking(dependency, cancelOnFailure));
        verify(uiDisplay).showScreen(UIScreenDef.PLB_RESTRICTED_ITEM);
        verify(uiDisplay).getResultCode(IUIDisplay.ACTIVITY_ID.ACT_INFORMATION, IUIDisplay.SHORT_TIMEOUT);
        verify(dependency.getProtocol()).setInternalRejectReason(trans, IProto.RejectReasonType.PLB_RESTRICTED_ITEM);
        verify(dependency.getWorkflowEngine()).setNextAction(TransactionDecliner.class);
    }

    @Test
    public void testCardRejectedWhenBlacklistedInOfflineMode() {
        when(trans.isStartedInOfflineMode()).thenReturn(true);
        when(trans.getCard().getTrack2()).thenReturn("some random string value");
        when(dependency.getConfig().getBlacklistCfg().isBlacklistedCard(any(), any(BinRangesCfg.class), anyString())).thenReturn(true);

        Assert.assertFalse(CheckBINRange.runBinRangeChecking(dependency, cancelOnFailure));
        verify(uiDisplay).showScreen(UIScreenDef.CARD_NOT_ACCEPTED);
        verify(dependency.getProtocol()).setInternalRejectReason(trans, IProto.RejectReasonType.CARD_NOT_ACCEPTED);
        verify(dependency.getWorkflowEngine()).setNextAction(TransactionCanceller.class);
    }
}