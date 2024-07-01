package com.linkly.payment.viewmodel.data;

import androidx.lifecycle.MutableLiveData;

import com.linkly.libui.IUIDisplay;
import com.linkly.libui.display.DisplayRequest;

import java.util.HashMap;

/* global store for display data  for ActTransaction */
/* Call SetDisplayRequest to alter the display wth a new request */
public class UITransData {

    private static UITransData ourData = new UITransData();
    private MutableLiveData<DisplayRequest> latestRequest = new MutableLiveData<>();

    /* a map of different fragment data for each fragment type */
    HashMap<IUIDisplay.ACTIVITY_ID, MutableLiveData<UIFragData>> fragMap = new HashMap<>();

    public static UITransData getInstance() {
        return ourData;
    }


    /* This is called to update the UI, hopefully puts data in the right place so UI is updated  properly Via Observers */
    public void setDisplayRequest(DisplayRequest newrequest) {


        latestRequest.postValue(newrequest);
        IUIDisplay.ACTIVITY_ID activityId = newrequest.getActivityID();

        MutableLiveData<UIFragData> fData = getFragMap(activityId);
        if (fData == null) {
            fData = new MutableLiveData<UIFragData>();
            fData.setValue(new UIFragData());
            fragMap.put(activityId, fData);
        }

        UIFragData f = fData.getValue();
        f.setDisplayRequest(newrequest);

    }

    /* used to get the correct UI for the activity */
    public MutableLiveData<UIFragData> getFragMap(IUIDisplay.ACTIVITY_ID activityId){

        MutableLiveData<UIFragData> fData = fragMap.get(activityId);
        if (fData == null) {
            fData = new MutableLiveData<UIFragData>();
            fData.setValue(new UIFragData());
            fragMap.put(activityId, fData);
        }
        return fragMap.get(activityId);
    }

    public MutableLiveData<DisplayRequest> getLatestRequest() {
        return this.latestRequest;
    }

    public HashMap<IUIDisplay.ACTIVITY_ID, MutableLiveData<UIFragData>> getFragMap() {
        return this.fragMap;
    }

    public void setLatestRequest(MutableLiveData<DisplayRequest> latestRequest) {
        this.latestRequest = latestRequest;
    }

    public void setFragMap(HashMap<IUIDisplay.ACTIVITY_ID, MutableLiveData<UIFragData>> fragMap) {
        this.fragMap = fragMap;
    }
}
