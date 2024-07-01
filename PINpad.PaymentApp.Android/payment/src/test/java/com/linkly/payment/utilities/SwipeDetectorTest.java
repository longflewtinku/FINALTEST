package com.linkly.payment.utilities;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import android.view.View;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class SwipeDetectorTest {
    private final SwipeDetector.OnSwipeEvent mockListener = mock(SwipeDetector.OnSwipeEvent.class);
    private final View mockView = mock(View.class);
    private SwipeDetector testSubject;

    @Before
    public void setUp() throws Exception {
        testSubject = new SwipeDetector(mockView);
    }

    @After
    public void tearDown() throws Exception {
        reset(mockListener);
    }

    @Test
    public void onRightToLeftSwipe() {
        testSubject.setOnSwipeListener(mockListener);
        testSubject.onRightToLeftSwipe();
        // Verify listener is called with right parameters
        verify(mockListener).swipeEventDetected(mockView, SwipeDetector.SwipeTypeEnum.RIGHT_TO_LEFT);
        // Verify listener is only called once
        verify(mockListener, times(1)).swipeEventDetected(any(), any());
    }

    @Test
    public void onRightToLeftSwipeWhenNoListener() {
        // Listener has not been set
        testSubject.onRightToLeftSwipe();
        // Verify listener is never called
        verify(mockListener, times(0)).swipeEventDetected(any(), any());
    }

    @Test
    public void onLeftToRightSwipe() {
        testSubject.setOnSwipeListener(mockListener);
        testSubject.onLeftToRightSwipe();
        // Verify listener is called with right parameters
        verify(mockListener).swipeEventDetected(mockView, SwipeDetector.SwipeTypeEnum.LEFT_TO_RIGHT);
        // Verify listener is only called once
        verify(mockListener, times(1)).swipeEventDetected(any(), any());
    }

    @Test
    public void onLeftToRightSwipeWhenNoListener() {
        // Listener has not been set
        testSubject.onLeftToRightSwipe();
        // Verify listener is never called
        verify(mockListener, times(0)).swipeEventDetected(any(), any());
    }

    @Test
    public void onTopToBottomSwipe() {
        testSubject.setOnSwipeListener(mockListener);
        testSubject.onTopToBottomSwipe();
        // Verify listener is called with right parameters
        verify(mockListener).swipeEventDetected(mockView, SwipeDetector.SwipeTypeEnum.TOP_TO_BOTTOM);
        // Verify listener is only called once
        verify(mockListener, times(1)).swipeEventDetected(any(), any());
    }

    @Test
    public void onTopToBottomSwipeWhenNoListener() {
        // Listener has not been set
        testSubject.onTopToBottomSwipe();
        // Verify listener is never called
        verify(mockListener, times(0)).swipeEventDetected(any(), any());
    }

    @Test
    public void onBottomToTopSwipe() {
        testSubject.setOnSwipeListener(mockListener);
        testSubject.onBottomToTopSwipe();
        // Verify listener is called with right parameters
        verify(mockListener).swipeEventDetected(mockView, SwipeDetector.SwipeTypeEnum.BOTTOM_TO_TOP);
        // Verify listener is only called once
        verify(mockListener, times(1)).swipeEventDetected(any(), any());
    }

    @Test
    public void onBottomToTopSwipeWhenNoListener() {
        // Listener has not been set
        testSubject.onBottomToTopSwipe();
        // Verify listener is never called
        verify(mockListener, times(0)).swipeEventDetected(any(), any());
    }

}