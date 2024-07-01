package com.linkly.payment.fragments;

import static com.linkly.libui.IUIDisplay.ACTIVITY_ID.ACT_ADD_TIP;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;

import com.linkly.libengine.engine.Engine;
import com.linkly.libmal.MalFactory;
import com.linkly.libui.IUIDisplay;
import com.linkly.libui.display.DisplayQuestion;
import com.linkly.libui.display.DisplayRequest;
import com.linkly.payment.R;
import com.linkly.payment.activities.ActScreenSaver;
import com.linkly.payment.databinding.ActivityTransBinding;
import com.linkly.payment.utilities.UIUtilities;
import com.linkly.payment.viewmodel.FragStandardViewModel;

import java.io.File;
import java.util.ArrayList;

import timber.log.Timber;


public class FragAddTip extends BaseFragment<ActivityTransBinding, FragStandardViewModel> implements View.OnClickListener {

    public static final String TAG = FragQuestion.class.getSimpleName();
    public static final String TIP_TIMBER = "Add Tip ? : %s";
    private FragStandardViewModel fragStandardViewModel;

    public static FragAddTip newInstance() {
        Bundle args = new Bundle();
        FragAddTip fragment = new FragAddTip();
        fragment.setArguments(args);
        return fragment;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onClick(View v) {
        ArrayList<DisplayQuestion> options = fragStandardViewModel.getDisplay().getValue().getUiExtras().getParcelableArrayList(IUIDisplay.uiScreenOptionList);
        switch (v.getId()) {
            case R.id.no_button:
                sendResponse(IUIDisplay.UIResultCode.OK, options.get(0).getResponse(), "");
                Timber.e(TIP_TIMBER, options.get(0).getResponse());
                break;
            case R.id.yes_button:
                sendResponse(IUIDisplay.UIResultCode.OK,options.get(1).getResponse(), "");
                Timber.e(TIP_TIMBER, options.get(1).getResponse());
                break;
            case R.id.cancel_button:
                sendResponse(IUIDisplay.UIResultCode.OK,options.get(2).getResponse(), "");
                Timber.e(TIP_TIMBER, options.get(2).getResponse());
                break;
        }
    }

    @Override
    public int getBindingVariable() {
        return com.linkly.payment.BR.fragStandardViewModel;
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_add_tip;
    }

    @Override
    public FragStandardViewModel getViewModel() {
        fragStandardViewModel = new ViewModelProvider(this).get(FragStandardViewModel.class);
        fragStandardViewModel.init(ACT_ADD_TIP);
        return fragStandardViewModel;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);
        if (v != null) {
            initView(v);
        }
        ActScreenSaver.cancelScreenSaver();
        return v;
    }

    private void initView(View view) {
        Bitmap logoBm = getLogo();
        if (logoBm != null) {
            ImageView logo = view.findViewById(R.id.logo);
            logo.setImageBitmap(logoBm);
        }

        String titleText;
        DisplayRequest displayRequest = fragStandardViewModel.getDisplay().getValue();
        if (displayRequest != null && displayRequest.getUiExtras().getString(IUIDisplay.uiScreenTitle) != null) {
            titleText = displayRequest.getUiExtras().getString(IUIDisplay.uiScreenTitle);
        } else {
            titleText = getResources().getString(R.string.SALE);
        }
        TextView title = view.findViewById(R.id.title);
        title.setText(titleText);

        TextView question = view.findViewById(R.id.question);
        question.setText(fragStandardViewModel.getPrompt().getValue());

        Button noButton = view.findViewById(R.id.no_button);
        noButton.setText(R.string.NO);
        noButton.setOnClickListener(this);
        GradientDrawable noDrawable = (GradientDrawable) noButton.getBackground().getConstantState().newDrawable().mutate();
        noDrawable.setStroke(2, Engine.getDep().getPayCfg().getBrandDisplayButtonColourOrDefault(getActivity().getColor(R.color.color_linkly_primary)));
        noDrawable.setColor(getContext().getColor(R.color.colorWhite));
        int btnColor = noDrawable.getColor().getDefaultColor();
        int textColor = Engine.getDep().getPayCfg().getBrandDisplayButtonTextColourOrDefault();
        if(textColor == Color.WHITE && btnColor == Color.WHITE) {
            noButton.setTextColor(Engine.getDep().getPayCfg().getBrandDisplayButtonColourOrDefault(getActivity().getColor(R.color.color_linkly_primary)));
        } else {
            noButton.setTextColor(textColor);
        }
        noButton.setBackground(noDrawable);

        Button yesButton = view.findViewById(R.id.yes_button);
        GradientDrawable yesDrawable = (GradientDrawable) yesButton.getBackground().getConstantState().newDrawable().mutate();
        yesDrawable.setColor(Engine.getDep().getPayCfg().getBrandDisplayButtonColourOrDefault(getActivity().getColor(R.color.color_linkly_primary)));
        yesButton.setBackground(yesDrawable);
        yesButton.setText(R.string.YES);
        yesButton.setTextColor(Engine.getDep().getPayCfg().getBrandDisplayButtonTextColourOrDefault());
        yesButton.setOnClickListener(this);

        Button cancelButton = view.findViewById(R.id.cancel_button);
        UIUtilities.borderTransparentButton(getActivity(),cancelButton);
        cancelButton.setText(R.string.CANCEL);
        cancelButton.setOnClickListener(this);
    }

    @SuppressWarnings( "deprecation" )
    private Bitmap getLogo() {
        Bitmap logo = null;
        //Try and Load Branded Gear Here
        try {
            //Force Garbage Collection
            System.gc();
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = false;
            options.inPreferredConfig = Bitmap.Config.RGB_565;
            options.inDither = true;

            String basePath = MalFactory.getInstance().getFile().getCommonDir();
            File imgFile = new File(basePath + "/" + Engine.getDep().getPayCfg().getBrandDisplayLogoHeaderOrDefault());
            if (imgFile.exists()) {
                logo = BitmapFactory.decodeFile(imgFile.getAbsolutePath(), options);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return logo;
    }
}
