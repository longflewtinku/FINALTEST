package com.linkly.payment.fragments;

import static com.linkly.libui.IUIDisplay.ACTIVITY_ID.ACT_QR_CODE;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProviders;

import com.linkly.libui.IUIDisplay;
import com.linkly.payment.R;
import com.linkly.payment.databinding.ActivityTransBinding;
import com.linkly.payment.viewmodel.FragStandardViewModel;

public class FragQRDisplay extends BaseFragment<ActivityTransBinding, FragStandardViewModel> {

    public static final String TAG = FragQRDisplay.class.getSimpleName();
    private FragStandardViewModel fragStandardViewModel;

    public static FragQRDisplay newInstance() {
        Bundle args = new Bundle();
        FragQRDisplay fragment = new FragQRDisplay();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public int getBindingVariable() {
        return com.linkly.payment.BR.fragStandardViewModel;
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_qrdisplay;
    }

    @Override
    public FragStandardViewModel getViewModel() {
        fragStandardViewModel = ViewModelProviders.of(this).get(FragStandardViewModel.class);
        fragStandardViewModel.init(ACT_QR_CODE);
        return fragStandardViewModel;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);
        SetHeader(false, false);

        ImageView qrCodeImage = v.findViewById(R.id.imageCode);

        byte[] bmpdata = fragStandardViewModel.getDisplay().getValue().getUiExtras().getByteArray(IUIDisplay.uiQRBitmapData);
        Bitmap qrBitmap = BitmapFactory.decodeByteArray(bmpdata, 0, bmpdata.length);

        if (qrBitmap != null) {
            qrCodeImage.setImageBitmap(qrBitmap);
            qrCodeImage.setVisibility(View.VISIBLE);
        }

        return v;
    }

}





