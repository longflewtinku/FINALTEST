package com.linkly.action.Printing;

import static com.linkly.libengine.action.Printing.PrintFirst.ReceiptType.CUSTOMER;
import static com.linkly.libengine.action.Printing.PrintFirst.ReceiptType.MERCHANT;
import static com.linkly.libengine.action.Printing.PrintFirst.buildReceiptForBroadcast;
import static org.junit.Assert.assertEquals;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import com.linkly.libengine.action.Printing.PrintFirst;
import com.linkly.libengine.dependencies.IDependency;
import com.linkly.libengine.engine.EngineManager;
import com.linkly.libengine.engine.transactions.TransRec;
import com.linkly.libengine.engine.transactions.properties.TProtocol;
import com.linkly.libmal.IMal;
import com.linkly.libmal.global.printing.PrintReceipt;
import com.linkly.libpositive.events.PositiveTransEvent;
import com.linkly.libpositive.wrappers.PositiveReceiptResponse;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

public class PrintFirstTest {

    @Mock
    PrintReceipt receiptToPrint;

    @Mock(answer = RETURNS_DEEP_STUBS)
    IDependency dependency;

    @Mock
    TransRec transRec;

    @Mock
    EngineManager.TransType transType;

    @Mock
    TProtocol protocol;

    @Mock
    PositiveTransEvent transEvent;

    @Mock
    PrintReceipt printReceipt;

    @Mock
    IMal mal;

    @Mock
    Context context;

    @Before
    public void setUp() throws Exception {
        openMocks(this);

        when(transRec.getProtocol()).thenReturn(protocol);
        when(transRec.getTransEvent()).thenReturn(transEvent);
        when(transRec.getTransType()).thenReturn(transType);
        when(receiptToPrint.convertForFixedWidth()).thenReturn(printReceipt);
        when(transEvent.getDeviceCode()).thenReturn("1");
    }

    @Test
    public void shouldBuildReceiptAsDuplicateWhenDuplicateEnabled() {
        PositiveReceiptResponse positiveReceiptResponse = buildReceiptForBroadcast(receiptToPrint, transRec, CUSTOMER, true, true);

        assertEquals(PositiveReceiptResponse.ReceiptType.DUPLICATE, positiveReceiptResponse.getType());
    }

    @Test
    public void shouldBuildReceiptAsMerchantWhenDuplicateEnabled() {
        PositiveReceiptResponse positiveReceiptResponse = buildReceiptForBroadcast(receiptToPrint, transRec, MERCHANT, true, true);

        assertEquals(PositiveReceiptResponse.ReceiptType.DUPLICATE, positiveReceiptResponse.getType());
    }

    @Test
    public void shouldBuildReceiptAsCustomerWhenDuplicateDisabled() {
        PositiveReceiptResponse positiveReceiptResponse = buildReceiptForBroadcast(receiptToPrint, transRec, CUSTOMER, false, true);

        assertEquals(PositiveReceiptResponse.ReceiptType.CUSTOMER, positiveReceiptResponse.getType());
    }

    @Test
    public void shouldBuildReceiptAsMerchantWhenDuplicateDisabled() {
        PositiveReceiptResponse positiveReceiptResponse = buildReceiptForBroadcast(receiptToPrint, transRec, MERCHANT, false, true);

        assertEquals(PositiveReceiptResponse.ReceiptType.MERCHANT, positiveReceiptResponse.getType());
    }

    @Test
    public void shouldSetPrintCopiesToZeroWhenNotPrinting() {
        PositiveReceiptResponse positiveReceiptResponse = buildReceiptForBroadcast(receiptToPrint, transRec, MERCHANT, false, false);

        assertEquals(0, positiveReceiptResponse.getCopies());
    }

    @Test
    public void shouldSetPrintCopiesToOneWhenPrinting() {
        when(transEvent.getDeviceCode()).thenReturn("0");
        PositiveReceiptResponse positiveReceiptResponse = buildReceiptForBroadcast(receiptToPrint, transRec, MERCHANT, false, true);

        assertEquals(1, positiveReceiptResponse.getCopies());
    }

    @Test
    public void shouldSetPrintCopiesToOneWhenPrintingPrintReceiptDisabled() {
        PositiveReceiptResponse positiveReceiptResponse = buildReceiptForBroadcast(receiptToPrint, transRec, MERCHANT, false, false);

        assertEquals(0, positiveReceiptResponse.getCopies());
    }

    @Test
    public void shouldPrintWhenRequested() {
        PrintFirst.print(dependency, transRec, true, false, true, context, mal);

        Mockito.verify(transRec).print(any(), any(Boolean.class), any(Boolean.class), any(Boolean.class), any());
    }

    @Test
    public void shouldNotPrintWhenRequestedNotTo() {
        PrintFirst.print(dependency, transRec, true, false, false,  context, mal);

        Mockito.verify(transRec, never()).print(any(), any(Boolean.class), any(Boolean.class), any(Boolean.class), any());
    }

    @Test
    public void shouldPrintSignatureRegardlessOfMerchantCopyFlag() {
        when(transRec.isApprovedOrDeferred()).thenReturn(true);
        when(transRec.isSignatureRequired()).thenReturn(true);
        when(dependency.getPayCfg().getPrintCustomerReceipt()).thenReturn("ALWAYS");

        // First test with PrintMerchantReceipt flag enabled
        boolean merchantReceiptInPrefs = true;
        SharedPreferences mockedSharedPrefs = Mockito.mock(SharedPreferences.class);
        when(mockedSharedPrefs.getBoolean("printMerchantReceipt", true)).thenReturn(merchantReceiptInPrefs);
        MockedStatic<PreferenceManager> mockPrefsManager = Mockito.mockStatic(PreferenceManager.class);
        mockPrefsManager.when(() ->PreferenceManager.getDefaultSharedPreferences(any())).thenReturn(mockedSharedPrefs);
        PrintFirst.doPrintFirstOrSecond(dependency, transRec, true, mal, context);
        Mockito.verify(transRec, atLeastOnce()).print(any(), any(Boolean.class), any(Boolean.class), any(Boolean.class), any());

        // Now test with PrintMerchantReceipt flag disabled
        merchantReceiptInPrefs = false;
        when(mockedSharedPrefs.getBoolean("printMerchantReceipt", true)).thenReturn(merchantReceiptInPrefs);
        PrintFirst.doPrintFirstOrSecond(dependency, transRec, true, mal, context);
        Mockito.verify(transRec, atLeastOnce()).print(any(), any(Boolean.class), any(Boolean.class), any(Boolean.class), any());
    }
}