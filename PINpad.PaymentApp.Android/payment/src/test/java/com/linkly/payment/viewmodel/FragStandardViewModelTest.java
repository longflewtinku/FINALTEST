package com.linkly.payment.viewmodel;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import android.app.Application;

import androidx.lifecycle.MutableLiveData;

import com.linkly.payment.viewmodel.data.UIFragData;

import org.junit.Test;

public class FragStandardViewModelTest {
    private final Application mockApp = mock(Application.class);
    private final FragStandardViewModel testSubject = new FragStandardViewModel(mockApp);

    @Test
    public void FragStandardViewModelUpdate() {
        MutableLiveData<UIFragData> fragData = new MutableLiveData<>();

        testSubject.updateViewModel(fragData);
        assertEquals(fragData, testSubject.fragData);
    }

}
