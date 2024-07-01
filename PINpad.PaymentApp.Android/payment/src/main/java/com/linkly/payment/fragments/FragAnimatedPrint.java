package com.linkly.payment.fragments;

import static com.linkly.libui.IUIDisplay.ACTIVITY_ID.ACT_ANIMATED_PRINT;

import android.os.Bundle;
import android.os.StrictMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;

import com.linkly.payment.R;
import com.linkly.payment.databinding.ActivityTransBinding;
import com.linkly.payment.printing.PrintManager;
import com.linkly.payment.viewmodel.FragStandardViewModel;

import ir.beigirad.zigzagview.ZigzagView;

public class FragAnimatedPrint extends BaseFragment<ActivityTransBinding, FragStandardViewModel> {

    public static final String TAG = FragAnimatedPrint.class.getSimpleName();
    private FragStandardViewModel fragStandardViewModel;


    public static FragAnimatedPrint newInstance() {
        Bundle args = new Bundle();
        FragAnimatedPrint fragment = new FragAnimatedPrint();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public int getBindingVariable() {
        return com.linkly.payment.BR.fragStandardViewModel;
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_animated_print;
    }

    @Override
    public FragStandardViewModel getViewModel() {
        fragStandardViewModel = ViewModelProviders.of(this).get(FragStandardViewModel.class);
        fragStandardViewModel.init(ACT_ANIMATED_PRINT);
        return fragStandardViewModel;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);
        SetHeader(false, false);


        if (PrintManager.lastReceiptPrinted != null) {
            final ImageView imageView = v.findViewById(R.id.receiptImage);
            final ZigzagView zigzagView = v.findViewById(R.id.zigZagView);

            imageView.setImageBitmap( PrintManager.lastReceiptPrinted );

            TranslateAnimation slide = new TranslateAnimation( zigzagView.getX(), zigzagView.getX(), zigzagView.getY(), zigzagView.getY() - 2500 );
            slide.setDuration( 3500 );
            zigzagView.startAnimation(slide);
        }

        return v;
    }

    public void storeEmail() {



    }

}





