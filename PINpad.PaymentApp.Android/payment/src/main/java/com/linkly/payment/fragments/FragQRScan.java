package com.linkly.payment.fragments;

import static com.linkly.libui.IUIDisplay.ACTIVITY_ID.ACT_QR_SCAN;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProviders;

import com.linkly.libmal.MalFactory;
import com.linkly.libmal.idal.IScanner;
import com.linkly.libui.IUIDisplay;
import com.linkly.payment.R;
import com.linkly.payment.activities.ActScreenSaver;
import com.linkly.payment.databinding.ActivityTransBinding;
import com.linkly.payment.viewmodel.FragStandardViewModel;

public class FragQRScan extends BaseFragment<ActivityTransBinding, FragStandardViewModel> {

    public static final String TAG = FragQRScan.class.getSimpleName();
    private FragStandardViewModel fragStandardViewModel;
    private IScanner iScanner;

    public static FragQRScan newInstance() {
        Bundle args = new Bundle();
        FragQRScan fragment = new FragQRScan();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public int getBindingVariable() {
        return com.linkly.payment.BR.fragStandardViewModel;
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_qrscan;
    }

    @Override
    public FragStandardViewModel getViewModel() {
        fragStandardViewModel = ViewModelProviders.of(this).get(FragStandardViewModel.class);
        fragStandardViewModel.init(ACT_QR_SCAN);
        return fragStandardViewModel;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);
        SetHeader(false, false);
        ActScreenSaver.cancelScreenSaver();

        this.iScanner = MalFactory.getInstance().getHardware().getDal().getScanner();

        if ( iScanner != null) {
            iScanner.open();
            iScanner.setTimeout( 30000 );
            iScanner.setContinuousTimes( 1 );
            iScanner.setContinuousInterval( 1000 );
            iScanner.start( new IScanner.IScanListener() {
                @Override
                public void onRead( String data ) {
                    sendResponse( IUIDisplay.UIResultCode.OK, data, "" );
                    close();
                }

                @Override
                public void onFinish() {
                    sendResponse( IUIDisplay.UIResultCode.FAIL, "TIMEOUT", "" );
                    close();
                }

                @Override
                public void onCancel() {
                    sendResponse( IUIDisplay.UIResultCode.MANUAL, "CANCEL", "" );
                    close();
                }
            } );
        }

        return v;
    }

    public void close() {
        iScanner.close();
    }

}





