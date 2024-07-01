package com.linkly.libengine.workflow;

import static com.linkly.libengine.engine.protocol.IProto.ProtoResult.PROTO_FAIL;
import static com.linkly.libengine.engine.protocol.IProto.ProtoResult.PROTO_SUCCESS;
import static com.linkly.libengine.engine.transactions.properties.TCard.CardIssuer.EFTPOS;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.HostResult.BATCH_UPLOAD_FAILED;
import static com.linkly.libmal.idal.ISys.ScreenLockTime.FIFTEEN_SEC;
import static com.linkly.libui.UIScreenDef.NO_TRANS_TO_UPLOAD;
import static com.linkly.libui.UIScreenDef.TRANS_UPLOAD_FAILED;
import static com.linkly.libui.UIScreenDef.TRANS_UPLOAD_SUCCESSFUL;
import static com.linkly.libui.UIScreenDef.VAR_PENDING;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

import android.content.Context;

import com.linkly.libengine.action.MenuOperations.admin.SubmitTransactions;
import com.linkly.libengine.config.PayCfg;
import com.linkly.libengine.dependencies.Dependencies;
import com.linkly.libengine.engine.Engine;
import com.linkly.libengine.engine.EngineManager;
import com.linkly.libengine.engine.comms.CommsStatusMonitor;
import com.linkly.libengine.engine.protocol.IProto;
import com.linkly.libengine.engine.transactions.TransRec;
import com.linkly.libengine.engine.transactions.properties.TAudit;
import com.linkly.libengine.engine.transactions.properties.TCard;
import com.linkly.libengine.engine.transactions.properties.TProtocol;
import com.linkly.libmal.IMal;
import com.linkly.libmal.IMalHardware;
import com.linkly.libmal.idal.IDal;
import com.linkly.libmal.idal.ISys;
import com.linkly.libui.IUI;
import com.linkly.libui.IUIDisplay;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

public class WorkflowEngineTest {

    @Mock
    Dependencies dependencies;

    @Mock
    IUIDisplay iuiDisplay;

    @Mock
    IUI iui;

    @Mock
    TransRec transRec;

    @Mock
    TAudit audit;

    @Mock
    TProtocol protocol;

    @Mock
    TCard card;

    @Mock
    IProto proto;

    @Mock
    IMal mal;

    @Mock
    Context context;

    @Mock
    IDal dal;

    @Mock
    ISys iSys;

    @Mock
    IMalHardware hardware;

    @Mock
    PayCfg payCfg;

    @Mock
    CommsStatusMonitor commsStatusMonitor;

    @Mock
    WorkflowEngine workflowEngine;

    @Before
    public void setUp() throws Exception {
        openMocks(this);

        when(payCfg.getScreenLockTime()).thenReturn(FIFTEEN_SEC);
        when(dependencies.getCurrentTransaction()).thenReturn(transRec);
        when(transRec.getAudit()).thenReturn(audit);
        when(transRec.getProtocol()).thenReturn(protocol);
        when(transRec.getCard()).thenReturn(card);
        when(card.getCardIssuer()).thenReturn(EFTPOS);
        when(dependencies.getFramework()).thenReturn(iui);
        when(dependencies.getProtocol()).thenReturn(proto);
        when(dependencies.getWorkflowEngine()).thenReturn(workflowEngine);
        when(iui.getUI()).thenReturn(iuiDisplay);
        when(mal.getMalContext()).thenReturn(context);
        when(mal.getHardware()).thenReturn(hardware);
        when(hardware.getDal()).thenReturn(dal);
        when(dal.getSys()).thenReturn(iSys);

        Engine.setDep(dependencies);
    }

    @Test
    public void shouldDisplayNoTransToUploadMessageWhenNotProcessedForSettlement() {
        try (MockedStatic<TransRec> mockedStatic = Mockito.mockStatic(TransRec.class)) {
            mockedStatic.when(() -> TransRec.countTransInBatch()).thenReturn(0);
            WorkflowEngine engine = new WorkflowEngine();
            engine.run(new WorkflowAddActions(new SubmitTransactions(false)), dependencies, mal, context);
        }
        verify(iuiDisplay).showScreen(NO_TRANS_TO_UPLOAD);
        verify(proto, never()).setInternalRejectReason(transRec, IProto.RejectReasonType.BATCH_UPLOAD_FAILED);
        verify(protocol, never()).setHostResult(BATCH_UPLOAD_FAILED);
    }

    @Test
    public void shouldNotDisplayNoTransToUploadMessageWhenProcessedForSettlement() {
        when(transRec.isReconciliation()).thenReturn(true);
        try (MockedStatic<TransRec> mockedStatic = Mockito.mockStatic(TransRec.class)) {
            mockedStatic.when(() -> TransRec.countTransInBatch()).thenReturn(0);
            WorkflowEngine engine = new WorkflowEngine();
            engine.run(new WorkflowAddActions(new SubmitTransactions(false)), dependencies, mal, context);
        }
        verify(iuiDisplay, never()).showScreen(NO_TRANS_TO_UPLOAD);
        verify(proto, never()).setInternalRejectReason(transRec, IProto.RejectReasonType.BATCH_UPLOAD_FAILED);
        verify(protocol, never()).setHostResult(BATCH_UPLOAD_FAILED);
    }

    @Test
    public void shouldProcessSafUploadWhenSettlementInitiatedWithSaf() {
        when(transRec.isReconciliation()).thenReturn(true);
        when(proto.batchUpload(false)).thenReturn(PROTO_SUCCESS);
        try (MockedStatic<TransRec> mockedStatic = Mockito.mockStatic(TransRec.class)) {
            mockedStatic.when(() -> TransRec.countTransInBatch()).thenReturn(3);
            WorkflowEngine engine = new WorkflowEngine();
            engine.run(new WorkflowAddActions(new SubmitTransactions(false)), dependencies, mal, context);
        }
        verify(iuiDisplay, never()).showScreen(NO_TRANS_TO_UPLOAD);
        verify(iuiDisplay).showScreen(VAR_PENDING, "3");
        verify(protocol, never()).setHostResult(BATCH_UPLOAD_FAILED);
        verify(iuiDisplay).showScreen(TRANS_UPLOAD_SUCCESSFUL);
        verify(proto, never()).setInternalRejectReason(transRec, IProto.RejectReasonType.BATCH_UPLOAD_FAILED);
    }

    @Test
    public void shouldHandleSafUploadFailureWhenSettlementInitiatedWithSaf() {
        when(transRec.isReconciliation()).thenReturn(true);
        when(proto.batchUpload(false)).thenReturn(PROTO_FAIL);
        try (MockedStatic<TransRec> mockedStatic = Mockito.mockStatic(TransRec.class)) {
            mockedStatic.when(() -> TransRec.countTransInBatch()).thenReturn(3);
            WorkflowEngine engine = new WorkflowEngine();
            engine.run(new WorkflowAddActions(new SubmitTransactions(false)), dependencies, mal, context);
        }
        verify(iuiDisplay, never()).showScreen(NO_TRANS_TO_UPLOAD);
        verify(iuiDisplay).showScreen(VAR_PENDING, "3");
        verify(protocol).setHostResult(BATCH_UPLOAD_FAILED);
        verify(proto).setInternalRejectReason(transRec, IProto.RejectReasonType.BATCH_UPLOAD_FAILED);
        verify(iuiDisplay).showScreen(TRANS_UPLOAD_FAILED);
    }

    @Test
    public void shouldListAllStandlaoneOnlyTxns() {
        Class<? extends Enum<?>> transTypes = EngineManager.TransType.class;
        List<String> transTypeNames = new ArrayList<>();

        for (Enum<?> transType:transTypes.getEnumConstants()){
            if(!((EngineManager.TransType)transType).adminTransaction && !((EngineManager.TransType)transType).autoTransaction)
                transTypeNames.add(transType.name());
        }

        assertEquals(19, transTypeNames.size());
        /* Just used to list all possible financial transactions types that can be triggered in standalone mode
            0 = "SALE"
            1 = "SALE_MOTO"
            2 = "CARD_NOT_PRESENT"
            3 = "CARD_NOT_PRESENT_REFUND"
            4 = "CASH"
            5 = "MANUAL_REVERSAL"
            6 = "CASHBACK"
            7 = "PREAUTH"
            8 = "PREAUTH_MOTO"
            9 = "PREAUTH_CANCEL"
            10 = "COMPLETION"
            11 = "TOPUPPREAUTH"
            12 = "TOPUPCOMPLETION"
            13 = "REFUND"
            14 = "BALANCE"
            15 = "DEPOSIT"
            16 = "OFFLINESALE"
            17 = "OFFLINECASH"
            18 = "REFUND_MOTO"
         */
    }
}