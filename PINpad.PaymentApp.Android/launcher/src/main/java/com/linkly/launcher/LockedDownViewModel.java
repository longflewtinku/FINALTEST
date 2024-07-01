package com.linkly.launcher;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.viewmodel.ViewModelInitializer;
import static androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY;

public class LockedDownViewModel extends AndroidViewModel {

    static final ViewModelInitializer<LockedDownViewModel> initializer = new ViewModelInitializer<>(
            LockedDownViewModel.class,
            creationExtras -> {
                LauncherApplication app = (LauncherApplication) creationExtras.get(APPLICATION_KEY);
                assert app != null;
                return new LockedDownViewModel(app);
            }
    );
    public LockedDownViewModel(@NonNull Application application) {
        super(application);
    }

    public LiveData<Integer> getCurrentBrandDisplayStatusBarColour() {
        return BrandingConfig.getInstance().getBrandDisplayStatusBarColour();
    }
}
