package com.linkly.payment.viewmodel;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import android.app.Application;

import androidx.lifecycle.MutableLiveData;

import com.linkly.payment.viewmodel.data.UIFragData;

import org.junit.Test;

public class MainMenuViewModelTest {
    private final Application mockApp = mock(Application.class);
    private final MainMenuViewModel testSubject = new MainMenuViewModel(mockApp);

    @Test
    public void MainMenuViewModelUpdate() {
        MutableLiveData<UIFragData> fragData = new MutableLiveData<>();

        testSubject.updateViewModel(fragData);
        assertEquals(fragData, testSubject.fragData);
    }

}
