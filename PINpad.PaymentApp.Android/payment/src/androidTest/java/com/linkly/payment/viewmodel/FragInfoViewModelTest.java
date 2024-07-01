package com.linkly.payment.viewmodel;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static org.junit.Assert.assertEquals;

import androidx.lifecycle.MutableLiveData;
import androidx.test.annotation.UiThreadTest;

import com.linkly.payment.viewmodel.data.UIFragData;

import org.junit.Test;


public class FragInfoViewModelTest {
    private final FragInfoViewModel testSubject = new FragInfoViewModel(getApplicationContext());

    // Note - @UIThreadTest is needed, to force this test to run in the main/UI thread; otherwise it will fail.
    @Test
    @UiThreadTest
    public void FragInfoViewModelUpdate() {
        UIFragData uiFragData = new UIFragData();
        MutableLiveData<UIFragData> uiFragDataContainer = new MutableLiveData<>();

        uiFragDataContainer.setValue(uiFragData);

        testSubject.updateViewModel(uiFragDataContainer);

        assertEquals(uiFragDataContainer, testSubject.fragData);
    }
}
