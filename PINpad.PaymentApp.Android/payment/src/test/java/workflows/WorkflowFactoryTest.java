package workflows;

import static com.linkly.libengine.engine.EngineManager.TransType.CASH;
import static com.linkly.libengine.engine.EngineManager.TransType.MANUAL_REVERSAL;
import static com.linkly.libengine.engine.EngineManager.TransType.RECONCILIATION;
import static com.linkly.libengine.engine.EngineManager.TransType.SALE;
import static com.linkly.libengine.engine.transactions.properties.TCard.CardIssuer.EFTPOS;
import static com.linkly.libengine.engine.transactions.properties.TCard.CardIssuer.MASTERCARD;
import static com.linkly.libengine.engine.transactions.properties.TCard.CardIssuer.VISA;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.HostResult.BATCH_UPLOAD_FAILED;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.HostResult.RECONCILED_IN_BALANCE;
import static com.linkly.libmal.idal.ISys.ScreenLockTime.FIFTEEN_SEC;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;

import com.linkly.libconfig.cpat.CardProductCfg;
import com.linkly.libengine.action.IAction;
import com.linkly.libengine.action.user_action.DisplayFinishTransaction;
import com.linkly.libengine.config.IConfig;
import com.linkly.libengine.config.PayCfg;
import com.linkly.libengine.debug.IDebug;
import com.linkly.libengine.dependencies.Dependencies;
import com.linkly.libengine.engine.Engine;
import com.linkly.libengine.engine.comms.CommsStatusMonitor;
import com.linkly.libengine.engine.customers.ICustomer;
import com.linkly.libengine.engine.protocol.IProto;
import com.linkly.libengine.engine.transactions.TransRec;
import com.linkly.libengine.engine.transactions.TransRecDao;
import com.linkly.libengine.engine.transactions.TransRecManager;
import com.linkly.libengine.engine.transactions.properties.TAmounts;
import com.linkly.libengine.engine.transactions.properties.TAudit;
import com.linkly.libengine.engine.transactions.properties.TCard;
import com.linkly.libengine.engine.transactions.properties.TCard.CardIssuer;
import com.linkly.libengine.engine.transactions.properties.TProtocol;
import com.linkly.libengine.status.IStatus;
import com.linkly.libengine.workflow.WorkflowEngine;
import com.linkly.libmal.IMal;
import com.linkly.libmal.IMalHardware;
import com.linkly.libmal.MalFactory;
import com.linkly.libmal.global.util.Util;
import com.linkly.libmal.idal.IDal;
import com.linkly.libmal.idal.ISys;
import com.linkly.libui.IUI;
import com.linkly.libui.IUIDisplay;
import com.linkly.payment.workflows.generic.Reconciliation;
import com.linkly.payment.workflows.till.TillReconciliation;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

public class WorkflowFactoryTest {

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
    TCard card;

    @Mock
    CardProductCfg cardProductCfg;

    @Mock
    TProtocol protocol;

    @Mock
    IProto proto;

    @Mock
    IMal mal;

    @Mock
    IConfig config;

    @Mock
    ICustomer customer;

    @Mock
    IStatus status;

    @Mock
    PayCfg payCfg;

    @Mock
    Context context;

    @Mock
    ContentResolver contentResolver;

    @Mock
    com.linkly.libengine.engine.reporting.Reconciliation reconciliation;

    @Mock
    TransRecDao transRecDao;

    @Mock
    SharedPreferences sharedPreferences;

    @Mock
    SharedPreferences.Editor editor;

    @Mock
    IMalHardware hardware;

    @Mock
    IDal dal;

    @Mock
    ISys iSys;

    @Mock
    IDebug debug;

    @Mock
    DisplayFinishTransaction displayFinishTransaction; // mock DisplayFinishTransaction, as it has android calls

    @Mock
    TransRecManager mockedManager;

    @Mock
    CommsStatusMonitor commsStatusMonitor;

    @Before
    public void setUp() throws Exception {
        openMocks(this);

        when(mockedManager.getTransRecDao()).thenReturn(transRecDao);

        when(payCfg.isOfflineFlightModeAllowed()).thenReturn(false);
        when(payCfg.isUnattendedModeAllowed()).thenReturn(false);
        when(payCfg.getScreenLockTime()).thenReturn(FIFTEEN_SEC);
        when(config.isConfigLoaded()).thenReturn(true);
        when(dependencies.getConfig()).thenReturn(config);
        when(dependencies.getPayCfg()).thenReturn(payCfg);
        when(dependencies.getCurrentTransaction()).thenReturn(transRec);
        when(dependencies.getStatusReporter()).thenReturn(status);
        when(transRec.getAudit()).thenReturn(audit);
        when(transRec.getProtocol()).thenReturn(protocol);
        when(transRec.getReconciliation()).thenReturn(reconciliation);
        when(dependencies.getFramework()).thenReturn(iui);
        when(dependencies.getProtocol()).thenReturn(proto);
        when(dependencies.getCustomer()).thenReturn(customer);
        when(dependencies.getDebugReporter()).thenReturn(debug);

        when(context.getContentResolver()).thenReturn(contentResolver);
        when(iui.getUI()).thenReturn(iuiDisplay);
        when(mal.getMalContext()).thenReturn(context);
        when(mal.getHardware()).thenReturn(hardware);
        when(hardware.getDal()).thenReturn(dal);
        when(dal.getSys()).thenReturn(iSys);
        when(context.getSharedPreferences(anyString(), anyInt())).thenReturn(sharedPreferences);
        when(sharedPreferences.edit()).thenReturn(editor);
        when(cardProductCfg.getName()).thenReturn("VISA");
        when(card.getCardsConfig(any())).thenReturn(cardProductCfg);

        Engine.setDep(dependencies);
    }

    @Test
    public void shouldOverrideReconciliationRecordsWithLocalForSettlement() {

        WorkflowEngine engine = new WorkflowEngine();
        when(dependencies.getWorkflowEngine()).thenReturn(engine);

        try (MockedStatic<TransRec> mockedStaticTransRec = Mockito.mockStatic(TransRec.class);
             MockedStatic<Util> mockedStaticUtil = Mockito.mockStatic(Util.class);
             MockedStatic<MalFactory> mockedStaticMalFactory = Mockito.mockStatic(MalFactory.class);
             MockedStatic<TransRecManager> transRecManagerMockedStatic = mockStatic(TransRecManager.class)) {
            transRecManagerMockedStatic.when(TransRecManager::getInstance).thenReturn(mockedManager);
            mockedStaticMalFactory.when(MalFactory::getInstance).thenReturn(mal);
            mockedStaticUtil.when(() -> Util.isInAirplaneMode(any(Context.class))).thenReturn(false);
            mockedStaticTransRec.when(TransRec::countTransInBatch).thenReturn(0);

            mockTransactionRecords();

            Reconciliation workflow = new Reconciliation();
            EmptyWorkflowForTest mockedWorkflow = new EmptyWorkflowForTest();
            for (IAction action : workflow.getActions()) {
                if (action instanceof DisplayFinishTransaction) {
                    action = displayFinishTransaction;
                }
                mockedWorkflow.addAction(action);
            }
            engine.run(mockedWorkflow, dependencies, mal, context);
        }
        verify(proto, never()).setInternalRejectReason(transRec, IProto.RejectReasonType.BATCH_UPLOAD_FAILED);
        verify(protocol, never()).setHostResult(BATCH_UPLOAD_FAILED);
        verify(transRec, times(2)).setReconciliation(any(com.linkly.libengine.engine.reporting.Reconciliation.class));
        verify(debug, times(1)).reportDebugEvent(IDebug.DEBUG_EVENT.BACK_TO_IDLE, null);
    }


    @Test
    public void shouldNotOverrideReconciliationRecordsWithLocalForTillSettlement() {

        WorkflowEngine engine = new WorkflowEngine();
        when(dependencies.getWorkflowEngine()).thenReturn(engine);
        try (MockedStatic<TransRec> mockedStatic = Mockito.mockStatic(TransRec.class);
             MockedStatic<Util> mockedStaticUtil = Mockito.mockStatic(Util.class);
             MockedStatic<MalFactory> mockedStaticMalFactory = Mockito.mockStatic(MalFactory.class);
             MockedStatic<TransRecManager> transRecManagerMockedStatic = mockStatic(TransRecManager.class)) {
            transRecManagerMockedStatic.when(TransRecManager::getInstance).thenReturn(mockedManager);
            mockedStaticMalFactory.when(() -> MalFactory.getInstance()).thenReturn(mal);
            mockedStaticUtil.when(() -> Util.isInAirplaneMode(any(Context.class))).thenReturn(false);
            mockedStatic.when(() -> TransRec.countTransInBatch()).thenReturn(0);

            mockTransactionRecords();

            TillReconciliation workflow = new TillReconciliation();
            EmptyWorkflowForTest mockedWorkflow = new EmptyWorkflowForTest();
            for (IAction action : workflow.getActions()) {
                if (action instanceof DisplayFinishTransaction) {
                    action = displayFinishTransaction;
                }
                mockedWorkflow.addAction(action);
            }
            engine.run(mockedWorkflow, dependencies, mal, context);
        }
        verify(proto, never()).setInternalRejectReason(transRec, IProto.RejectReasonType.BATCH_UPLOAD_FAILED);
        verify(protocol, never()).setHostResult(BATCH_UPLOAD_FAILED);
        verify(transRec, times(1)).setReconciliation(any(com.linkly.libengine.engine.reporting.Reconciliation.class));
        verify(debug, times(1)).reportDebugEvent(IDebug.DEBUG_EVENT.BACK_TO_IDLE, null);
    }

    private void mockTransactionRecords() {
        List<TransRec> transRecs = new ArrayList<>();
        TAmounts amounts1 = mock(TAmounts.class);
        when(amounts1.getTotalAmount()).thenReturn(20110L);
        when(amounts1.getTotalAmountWithoutTip()).thenReturn(20010L);
        when(amounts1.getTotalAmountWithoutCashback()).thenReturn(10110L);
        when(amounts1.getTotalAmountWithoutSurcharge()).thenReturn(20100L);
        when(amounts1.getBaseAmount()).thenReturn(10000L);
        TransRec mockTransRec1 = mock(TransRec.class);
        when(mockTransRec1.getAmounts()).thenReturn(amounts1);
        when(mockTransRec1.approvedAndIncludeInReconciliation()).thenReturn(true);
        when(mockTransRec1.getProtocol()).thenReturn(protocol);
        when(mockTransRec1.getAudit()).thenReturn(audit);
        mockCardData(mockTransRec1, VISA);
        when(mockTransRec1.getTransType()).thenReturn(SALE);

        TAmounts amounts2 = mock(TAmounts.class);
        when(amounts2.getTotalAmount()).thenReturn(1000L);
        when(amounts2.getTotalAmountWithoutTip()).thenReturn(0L);
        when(amounts2.getTotalAmountWithoutCashback()).thenReturn(0L);
        when(amounts2.getTotalAmountWithoutSurcharge()).thenReturn(0L);
        when(amounts2.getBaseAmount()).thenReturn(0L);
        TransRec mockTransRec2 = mock(TransRec.class);
        when(mockTransRec2.getAmounts()).thenReturn(amounts2);

        when(mockTransRec2.approvedAndIncludeInReconciliation()).thenReturn(true);
        when(mockTransRec2.getProtocol()).thenReturn(protocol);
        when(mockTransRec2.getAudit()).thenReturn(audit);
        mockCardData(mockTransRec2, EFTPOS);
        when(mockTransRec2.getTransType()).thenReturn(CASH);

        TAmounts amounts3 = mock(TAmounts.class);
        when(amounts3.getTotalAmount()).thenReturn(1L);
        when(amounts3.getTotalAmountWithoutTip()).thenReturn(0L);
        when(amounts3.getTotalAmountWithoutCashback()).thenReturn(0L);
        when(amounts3.getTotalAmountWithoutSurcharge()).thenReturn(0L);
        when(amounts3.getBaseAmount()).thenReturn(0L);
        TransRec mockTransRec3 = mock(TransRec.class);
        when(mockTransRec3.getAmounts()).thenReturn(amounts3);

        when(mockTransRec3.approvedAndIncludeInReconciliation()).thenReturn(true);
        when(mockTransRec3.getProtocol()).thenReturn(protocol);
        when(mockTransRec3.getAudit()).thenReturn(audit);
        mockCardData(mockTransRec3, MASTERCARD);
        when(mockTransRec3.getTransType()).thenReturn(MANUAL_REVERSAL);

        transRecs.add(mockTransRec1);
        transRecs.add(mockTransRec2);
        transRecs.add(mockTransRec3);

        when(transRec.getTransType()).thenReturn(RECONCILIATION);
        when(transRecDao.findBySummedOrReced(false)).thenReturn(transRecs);
        when(audit.getReceiptNumber()).thenReturn(1);
        when(transRec.isReconciliation()).thenReturn(true);
        when(protocol.getHostResult()).thenReturn(RECONCILED_IN_BALANCE);
    }

    private void mockCardData(TransRec transRec, CardIssuer cardIssuer) {
        when(transRec.getCard()).thenReturn(card);
        when(card.getCardIssuer()).thenReturn(cardIssuer);
    }
}
