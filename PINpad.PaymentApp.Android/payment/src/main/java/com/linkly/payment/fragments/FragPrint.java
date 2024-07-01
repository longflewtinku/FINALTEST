package com.linkly.payment.fragments;

import static com.linkly.libui.IUIDisplay.ACTIVITY_ID.ACT_SCREEN_PRINT;

import android.graphics.Bitmap;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;

import com.linkly.libengine.action.IPC.EmailUpload;
import com.linkly.libengine.engine.Engine;
import com.linkly.libui.IUIDisplay;
import com.linkly.libui.IUIDisplay.String_id;
import com.linkly.payment.R;
import com.linkly.payment.databinding.ActivityTransBinding;
import com.linkly.payment.printing.PrintManager;
import com.linkly.payment.viewmodel.FragStandardViewModel;

import java.io.ByteArrayOutputStream;

public class FragPrint extends BaseFragment<ActivityTransBinding, FragStandardViewModel> {

    public static final String TAG = FragPrint.class.getSimpleName();
    private FragStandardViewModel fragStandardViewModel;
    private WebView webView;

    public static FragPrint newInstance() {
        Bundle args = new Bundle();
        FragPrint fragment = new FragPrint();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public int getBindingVariable() {
        return com.linkly.payment.BR.fragStandardViewModel;
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_print;
    }

    @Override
    public FragStandardViewModel getViewModel() {
        fragStandardViewModel = ViewModelProviders.of(this).get(FragStandardViewModel.class);
        fragStandardViewModel.init(ACT_SCREEN_PRINT);
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            WebView.enableSlowWholeDocumentDraw();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(builder.build());
        }

        webView = (WebView) v.findViewById(R.id.textField);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setJavaScriptEnabled(false);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        webView.getSettings().setLoadsImagesAutomatically(true);
        webView.getSettings().setUseWideViewPort(false);


        webView.setPadding(0, 0, 0, 0);
        webView.setInitialScale(110);

        // TODO FIXME  this work should be started after the View is created and in a background
        //  thread.
        if (PrintManager.lastReceiptPrinted != null) {
            String html = "<html><body><center><img src='{IMAGE_PLACEHOLDER}' /></center></body></html>";

            Bitmap bitmapToUse = PrintManager.lastReceiptPrinted;
            if (Engine.getDep().getCurrentTransaction() != null) {
                bitmapToUse = EmailUpload.addSignatureToReceipt(Engine.getDep(), Engine.getDep().getCurrentTransaction(), PrintManager.lastReceiptPrinted);
            }

            // Convert bitmap to Base64 encoded image for web
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmapToUse.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            String imgageBase64 = Base64.encodeToString(byteArray, Base64.DEFAULT);
            String image = "data:image/png;base64," + imgageBase64;

            // Use image for the img src parameter in your html and load to webview
            html = html.replace("{IMAGE_PLACEHOLDER}", image);
            webView.loadDataWithBaseURL("file:///android_asset/", html, "text/html", "utf-8", "");
        }


        Button print = (Button) v.findViewById(R.id.btnDone);
        GradientDrawable printDrawable = (GradientDrawable) print.getBackground().getConstantState().newDrawable().mutate();
        printDrawable.setColor(Engine.getDep().getPayCfg().getBrandDisplayButtonColourOrDefault(requireActivity().getColor(R.color.color_linkly_primary)));
        print.setTextColor(Engine.getDep().getPayCfg().getBrandDisplayButtonTextColourOrDefault());
        print.setBackground(printDrawable);
        print.setText(Engine.getDep().getPrompt(String_id.STR_DONE));
        print.setOnClickListener(v1 -> sendResponse(IUIDisplay.UIResultCode.OK, "", ""));

        return v;
    }

}





