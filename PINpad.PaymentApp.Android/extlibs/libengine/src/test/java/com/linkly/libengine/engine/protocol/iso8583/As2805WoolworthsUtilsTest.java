package com.linkly.libengine.engine.protocol.iso8583;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import com.linkly.libengine.engine.transactions.TransRec;
import com.linkly.libengine.engine.transactions.properties.TProtocol;

import junit.framework.TestCase;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.Calendar;

public class As2805WoolworthsUtilsTest {

    @Mock
    private TransRec trans;
    @Mock
    private TProtocol protocol;
    @Mock
    private Calendar calendar;
    MockedStatic<Calendar> mockedStatic;

    @Rule //initMocks
    public final MockitoRule rule = MockitoJUnit.rule();

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mockedStatic = Mockito.mockStatic(Calendar.class);
        mockedStatic.when(Calendar::getInstance).thenReturn(calendar);
        when(trans.getProtocol()).thenReturn(protocol);
    }

    @After
    public void tearDown() {
        if (mockedStatic != null) {
            mockedStatic.close();
        }
    }

    @Test
    public void shouldCalculateRetRefNumberForCommonYear() throws Exception {
        when(calendar.get(Calendar.YEAR)).thenReturn(2023);
        when(calendar.get(Calendar.DAY_OF_YEAR)).thenReturn(142);
        when(calendar.get(Calendar.HOUR_OF_DAY)).thenReturn(15);
        when(protocol.getStan()).thenReturn(123456);


        String rrn = As2805WoolworthsUtils.calculateRetRefNumber(trans);
        assertEquals("314215123456", rrn);
    }

    @Test
    public void shouldCalculateRetRefNumberForLeapYear() throws Exception {
        when(calendar.get(Calendar.YEAR)).thenReturn(2024);
        when(calendar.get(Calendar.DAY_OF_YEAR)).thenReturn(60); // Feb 29 in a leap year
        when(calendar.get(Calendar.HOUR_OF_DAY)).thenReturn(10);
        when(protocol.getStan()).thenReturn(654321);

        String rrn = As2805WoolworthsUtils.calculateRetRefNumber(trans);
        assertEquals("406010654321", rrn);
    }

    @Test
    public void shouldCalculateRetRefNumberForYearEnd() throws Exception {
        when(calendar.get(Calendar.YEAR)).thenReturn(2023);
        when(calendar.get(Calendar.DAY_OF_YEAR)).thenReturn(365); // Dec 31 in a common year
        when(calendar.get(Calendar.HOUR_OF_DAY)).thenReturn(23);
        when(protocol.getStan()).thenReturn(789012);

        String rrn = As2805WoolworthsUtils.calculateRetRefNumber(trans);
        assertEquals("336523789012", rrn);
    }

    @Test
    public void shouldCalculateRetRefNumberForMidnight() throws Exception {
        when(calendar.get(Calendar.YEAR)).thenReturn(2023);
        when(calendar.get(Calendar.DAY_OF_YEAR)).thenReturn(1); // Jan 1
        when(calendar.get(Calendar.HOUR_OF_DAY)).thenReturn(0);
        when(protocol.getStan()).thenReturn(345678);

        String rrn = As2805WoolworthsUtils.calculateRetRefNumber(trans);
        assertEquals("300100345678", rrn);
    }

    @Test
    public void shouldCalculateRetRefNumberForEndOfLeapYear() throws Exception {
        when(calendar.get(Calendar.YEAR)).thenReturn(2024);
        when(calendar.get(Calendar.DAY_OF_YEAR)).thenReturn(366); // Dec 31 in a leap year
        when(calendar.get(Calendar.HOUR_OF_DAY)).thenReturn(23);
        when(protocol.getStan()).thenReturn(987654);

        String rrn = As2805WoolworthsUtils.calculateRetRefNumber(trans);
        assertEquals("436623987654", rrn);
    }

    @Test
    public void shouldCalculateRetRefNumberForLeapYearStanAsSingleDigit() throws Exception {
        when(calendar.get(Calendar.YEAR)).thenReturn(2024);
        when(calendar.get(Calendar.DAY_OF_YEAR)).thenReturn(60); // Feb 29 in a leap year
        when(calendar.get(Calendar.HOUR_OF_DAY)).thenReturn(10);
        when(protocol.getStan()).thenReturn(1);

        String rrn = As2805WoolworthsUtils.calculateRetRefNumber(trans);
        assertEquals("406010000001", rrn);
    }
}