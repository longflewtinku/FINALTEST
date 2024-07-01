package com.linkly.payment.viewmodel;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import android.app.Application;
import android.content.SharedPreferences;

import androidx.lifecycle.MutableLiveData;

import com.linkly.libengine.engine.reporting.ReconciliationManager;
import com.linkly.libengine.engine.transactions.TransRecDao;
import com.linkly.libengine.engine.transactions.TransRecManager;
import com.linkly.libmal.MalFactory;
import com.linkly.payment.viewmodel.data.UIFragData;
import com.linkly.payment.viewmodel.data.UITransData;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

public class FragPreAuthListViewModelTest {
    private final Application mockApp = mock(Application.class);

    MockedStatic<TransRecManager> mockedStatic;


    @Rule //initMocks
    public final MockitoRule rule = MockitoJUnit.rule();


    @After
    public void after() {
        if(mockedStatic != null) {
            mockedStatic.close();
        }
    }

    @Test
    public void FragPreAuthListViewModelUpdate() {

        TransRecDao mockedDao = mock(TransRecDao.class);
        TransRecManager transRecManager = mock(TransRecManager.class);
        when(transRecManager.getTransRecDao()).thenReturn(mockedDao);
        when(mockedDao.getByTransTypesAndApprovedLiveData(any(), any(), any(), any())).thenReturn(new MutableLiveData<>());
        when(mockApp.getSharedPreferences(anyString(), anyInt())).thenReturn(mock(SharedPreferences.class));

        mockedStatic = mockStatic(TransRecManager.class);
        mockedStatic.when(TransRecManager::getInstance).thenReturn(transRecManager);

        FragPreAuthListViewModel testSubject = new FragPreAuthListViewModel(mockApp);
        MutableLiveData<UIFragData> fragData = new MutableLiveData<>();

        testSubject.updateViewModel(fragData);
        assertEquals(fragData, testSubject.fragData);
    }

}