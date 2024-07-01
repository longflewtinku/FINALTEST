package com.linkly.launcher;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class ProgressViewModel extends AndroidViewModel {

    private final MutableLiveData<String> selected = new MutableLiveData<>();

    public ProgressViewModel(@NonNull Application application) {
        super(application);
    }

    public void setSelected(String item) {
        selected.setValue(item);
    }

    public LiveData<String> getSelected() {
        return selected;
    }
}
