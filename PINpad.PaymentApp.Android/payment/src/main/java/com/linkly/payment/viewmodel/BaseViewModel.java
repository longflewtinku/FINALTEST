/*
 *  Copyright (C) 2017 MINDORKS NEXTGEN PRIVATE LIMITED
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      https://mindorks.com/license/apache-v2
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License
 */

package com.linkly.payment.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.linkly.libengine.engine.Engine;
import com.linkly.libui.IUIDisplay;
import com.linkly.libui.display.DisplayRequest;
import com.linkly.payment.viewmodel.data.UIFragData;
import com.linkly.payment.viewmodel.data.UITransData;

import java.util.HashMap;

import timber.log.Timber;

public abstract class BaseViewModel extends AndroidViewModel {

    protected UITransData repository;
    private DisplayRequest displayRequestForResponse; /* so every ViewModel replies properly to the correct request */


    /* standard pieces of data that all (or most) views will sue */
    protected MutableLiveData<UIFragData> fragData;

    /* default constructor */
    protected BaseViewModel(@NonNull Application application) {
        super(application);
        repository = UITransData.getInstance();
    }

    public LiveData<DisplayRequest> getDisplay() {
        return UITransData.getInstance().getLatestRequest();
    }

    private boolean initialised = false;
    /* called to configure he fragment for the correct activity */
    public void init(IUIDisplay.ACTIVITY_ID activityId) {

        displayRequestForResponse = UITransData.getInstance().getLatestRequest().getValue();
        if (!initialised) {
            fragData = repository.getFragMap(activityId);
            updateViewModel(fragData);
            initialised = true;
        }
    }

    /* children implement */
    protected abstract void updateViewModel(MutableLiveData<UIFragData> fragData);



    /* send the response to the back end */
    /* should only be called by the base fragment sendResponse() */
    /* as this will also enable the timers to finish the activity */
    public boolean sendResponse(IUIDisplay.UIResultCode uiResultCode, String sResult1, String sResult2) {
        if (displayRequestForResponse != null) {
            Timber.i("sendResponse:" + displayRequestForResponse.getActivityID().name() + uiResultCode.name());
            Engine.getDep().getUI().sendResponse(displayRequestForResponse, uiResultCode, sResult1, sResult2);
            displayRequestForResponse = null;
            return true;
        }
        return false;
    }

    /* send the response to the back end */
    /* should only be called by the base fragment sendResponse() */
    /* as this will also enable the timers to finish the activity */
    public boolean sendResponse(HashMap<String, Object> map) {
        if (displayRequestForResponse != null) {
            Engine.getDep().getUI().sendResponse(displayRequestForResponse, map);
            displayRequestForResponse = null;
            return true;
        }
        return false;
    }

    public LiveData<String> getTitle() {
        return fragData.getValue().getTitle();
    }

    public LiveData<String> getPrompt() {
        return fragData.getValue().getPrompt();
    }

    public LiveData<String> getHint() {
        return fragData.getValue().getHint();
    }

    // Instead of passing arguments in a traditional way, this framework passes them into memory
    //  held by static libs and makes them available via inheritance from this Base class. The
    //  UIFragData data itself is more LiveDatas, making this an async mechanism to provide
    //  async mechanisms to provide actual display data.
    public MutableLiveData<UIFragData> getFragData() {
        return this.fragData;
    }

    // Purely exists for ActTransaction to update onDisplayChanged. This is part of a navigation
    //  mechanism that would be better replaced with AndroidX Navigation, SafeArgs, and standard
    //  use of ViewModels (one per Activity/Fragment) and composed of framework mechanisms where
    //  needed rather than inheriting them always.
    public void setDisplayRequestForResponse(DisplayRequest displayRequestForResponse) {
        this.displayRequestForResponse = displayRequestForResponse;
    }
}
