package com.linkly.libengine.Check;

import static com.linkly.libui.UIScreenDef.BATTERY_TOO_LOW;
import static com.linkly.libui.UIScreenDef.PAPER_OUT;
import static com.linkly.libui.UIScreenDef.PRINTER_ERROR_VAR_CHECK_PRINTER;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.linkly.libengine.action.TransactionCanceller;
import com.linkly.libengine.action.check.CheckPrinter;
import com.linkly.libengine.dependencies.IDependency;
import com.linkly.libengine.engine.transactions.TransRec;
import com.linkly.libengine.status.IStatus;
import com.linkly.libengine.workflow.WorkflowEngine;
import com.linkly.libmal.IMalPrint.PrinterReturn;
import com.linkly.libui.IUI;
import com.linkly.libui.IUIDisplay;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class CheckPrinterTest {
    @Mock
    IDependency mockedDependency;
    @Mock
    IStatus mockedStatusReporter;
    @Mock
    IUI mockedFramework;
    @Mock
    IUIDisplay mockedUiDisplay;
    @Mock
    TransRec mockedTrans;
    @Mock
    WorkflowEngine mockedWorkflowEngine;


    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(mockedDependency.getStatusReporter()).thenReturn(mockedStatusReporter);
        when(mockedDependency.getFramework()).thenReturn(mockedFramework);
        when(mockedFramework.getUI()).thenReturn(mockedUiDisplay);
        when(mockedDependency.getWorkflowEngine()).thenReturn(mockedWorkflowEngine);
    }

    @Test
    public void testHandlePrinterCheckFailureDueToLowVoltage() {
        CheckPrinter.handlePrinterCheckFailure(mockedDependency, mockedTrans, PrinterReturn.VOLTAGE_LOW, TransactionCanceller.class);
        verify(mockedStatusReporter).reportStatusEvent(IStatus.STATUS_EVENT.STATUS_ERR_PRINTER_GENERAL_ERROR, mockedTrans.isSuppressPosDialog());
        verify(mockedUiDisplay).showScreen(BATTERY_TOO_LOW);
        verify(mockedWorkflowEngine).setNextAction(TransactionCanceller.class);
    }

    @Test
    public void testHandlePrinterCheckFailureDueToOutOfPaper() {
        CheckPrinter.handlePrinterCheckFailure(mockedDependency, mockedTrans, PrinterReturn.OUT_OF_PAPER, TransactionCanceller.class);
        verify(mockedStatusReporter).reportStatusEvent(IStatus.STATUS_EVENT.STATUS_ERR_PRINTER_OUT_OF_PAPER, mockedTrans.isSuppressPosDialog());
        verify(mockedUiDisplay).showScreen(PAPER_OUT);
        verify(mockedWorkflowEngine).setNextAction(TransactionCanceller.class);
    }

    @Test
    public void testHandlePrinterCheckFailureDueToOtherErrors() {
        CheckPrinter.handlePrinterCheckFailure(mockedDependency, mockedTrans, PrinterReturn.UNKNOWN_FAILURE, TransactionCanceller.class);
        verify(mockedStatusReporter).reportStatusEvent(IStatus.STATUS_EVENT.STATUS_ERR_PRINTER_GENERAL_ERROR, mockedTrans.isSuppressPosDialog());
        verify(mockedUiDisplay).showScreen(PRINTER_ERROR_VAR_CHECK_PRINTER, PrinterReturn.UNKNOWN_FAILURE.toString());
        verify(mockedWorkflowEngine).setNextAction(TransactionCanceller.class);
    }
}