package com.linkly.payment.fragments;

import static com.linkly.libui.IUIDisplay.ACTIVITY_ID.ACT_PICK_GRATUITY;

import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.widget.TextViewCompat;
import androidx.lifecycle.ViewModelProvider;

import com.linkly.libengine.engine.Engine;
import com.linkly.libmal.MalFactory;
import com.linkly.libui.IUIDisplay;
import com.linkly.libui.display.DisplayFragmentOption;
import com.linkly.libui.display.DisplayQuestion;
import com.linkly.libui.display.DisplayRequest;
import com.linkly.payment.R;
import com.linkly.payment.activities.ActScreenSaver;
import com.linkly.payment.databinding.ActivityTransBinding;
import com.linkly.payment.utilities.UIUtilities;
import com.linkly.payment.viewmodel.FragStandardViewModel;

import java.io.File;
import java.util.ArrayList;

public class FragTipPick extends BaseFragment<ActivityTransBinding, FragStandardViewModel> {

    public static final String TAG = FragQuestion.class.getSimpleName();
    private FragStandardViewModel fragStandardViewModel;

    public static FragTipPick newInstance() {
        Bundle args = new Bundle();
        FragTipPick fragment = new FragTipPick();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public int getBindingVariable() {
        return com.linkly.payment.BR.fragStandardViewModel;
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_tip_amount;
    }

    @Override
    public FragStandardViewModel getViewModel() {
        fragStandardViewModel = new ViewModelProvider(this).get(FragStandardViewModel.class);
        fragStandardViewModel.init(ACT_PICK_GRATUITY);
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
            titleText = getResources().getString(R.string.GRATUITY);
        }
        TextView title = view.findViewById(R.id.title);
        title.setText(titleText);
        if (displayRequest != null) {
            initButtons(view, displayRequest);
            initValues(view, displayRequest);
        }

    }

    private void setButton(Button button, DisplayQuestion option) {
        if (button != null && option != null) {
            button.setText(option.getTitle());
            button.setOnClickListener(v -> {
                changePressedDrawable(button);
                sendResponse(IUIDisplay.UIResultCode.OK, option.getResponse(), "");
            });
        } else if (button != null) { // Hide the button if options don't exist
            button.setVisibility(View.INVISIBLE);
        }
    }

    private void changePressedDrawable(Button button) {
        ConstraintLayout firstRow = button.getRootView().findViewById(R.id.tip_row_1);
        ConstraintLayout secondRow = button.getRootView().findViewById(R.id.tip_row_2);
        if (button == firstRow.findViewById(R.id.tip_1) || button == firstRow.findViewById(R.id.tip_2) ||
                button == firstRow.findViewById(R.id.tip_3) || button == firstRow.findViewById(R.id.tip_4) ||
                button == secondRow.findViewById(R.id.tip_1) || button == secondRow.findViewById(R.id.tip_2)) {
            GradientDrawable shapeDrawable = (GradientDrawable) button.getBackground().getConstantState().newDrawable().mutate();
            shapeDrawable.setColor(ContextCompat.getColor(getContext(), R.color.colorBlack));
            button.setBackground(shapeDrawable);
            button.setTextColor(ContextCompat.getColor(getContext(), R.color.colorWhite));
            TextViewCompat.setTextAppearance(button, R.style.uiTipBtn);
        }
    }

    @SuppressWarnings("deprecation")
    private void initButtons(View view, DisplayRequest displayRequest) {
        ArrayList<DisplayQuestion> options = displayRequest.getUiExtras().getParcelableArrayList(IUIDisplay.uiScreenOptionList);
        // we have to take a bunch of assumptions

        if (options == null) {
            return;
        }

        // Alright the way we calculate this is the following.
        // none,
        // other,
        // tip 1, (possibly not available)
        // tip 2, (possibly not available)
        // tip 3, (possibly not available)
        // tip 4, (possibly not available)
        // back,
        // accept
        int numberOfTipValues = options.size() - 4; // Subtract 4 as there are 4 "non" tip options that must always exist, the remaining value is the number of tips available.
        int currentIdx = 0; // current index of our options
        int firstTipIndexOffset = 2; // based on our options tipping values start at index 2.

        ConstraintLayout firstRow = view.findViewById(R.id.tip_row_1);
        setButton(firstRow.findViewById(R.id.tip_1), options.get(currentIdx++)); // none value option
        setButton(firstRow.findViewById(R.id.tip_2), options.get(currentIdx++)); // other value option
        setButton(firstRow.findViewById(R.id.tip_3), (numberOfTipValues > currentIdx - firstTipIndexOffset) ? options.get(currentIdx++) : null); // Get our tip value 1 (if it exists)
        setButton(firstRow.findViewById(R.id.tip_4), (numberOfTipValues > currentIdx - firstTipIndexOffset) ? options.get(currentIdx++) : null); // Get our tip value 2 (if it exists)

        ConstraintLayout secondRow = view.findViewById(R.id.tip_row_2);
        Button changingTip1 = secondRow.findViewById(R.id.tip_1);
        Button changingTip2 = secondRow.findViewById(R.id.tip_3);

        setButton(secondRow.findViewById(R.id.tip_2), (numberOfTipValues > currentIdx - firstTipIndexOffset) ? options.get(currentIdx++) : null); // Get our tip value 3 (if it exists)

        Button acceptTip = secondRow.findViewById(R.id.tip_4);
        GradientDrawable shapeDrawable = (GradientDrawable) acceptTip.getBackground().getConstantState().newDrawable().mutate();
        shapeDrawable.setColor(Engine.getDep().getPayCfg().getBrandDisplayButtonColourOrDefault(getActivity().getColor(R.color.color_linkly_primary)));
        acceptTip.setTextColor(Engine.getDep().getPayCfg().getBrandDisplayButtonTextColourOrDefault());
        acceptTip.setBackground(shapeDrawable);
        TextViewCompat.setTextAppearance(acceptTip, R.style.uiDoneBtn);
        acceptTip.setAllCaps(false);

        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setButton(changingTip2, (numberOfTipValues > currentIdx - firstTipIndexOffset) ? options.get(currentIdx++) : null); // Get our tip value 4 (if it exists)
            setCancelButtonStyling(changingTip1, options.get(currentIdx++)); // cancel
        } else {
            setButton(changingTip1, (numberOfTipValues > currentIdx - firstTipIndexOffset) ? options.get(currentIdx++) : null); // Get our tip value 4 (if it exists)
            setCancelButtonStyling(changingTip2, options.get(currentIdx++)); // cancel

            ((ConstraintLayout.LayoutParams) acceptTip.getLayoutParams()).topMargin = (int) getResources().getDimension(R.dimen.spacer_margin);
            ((ConstraintLayout.LayoutParams) changingTip2.getLayoutParams()).topMargin = (int) getResources().getDimension(R.dimen.spacer_margin);
        }

        setButton(acceptTip, options.get(currentIdx++)); // accept
    }

    private void setCancelButtonStyling(Button cancelButton, DisplayQuestion cancelOption) {
        cancelButton.setBackgroundResource(R.drawable.ui2_buttonstyle_cancel);
        TextViewCompat.setTextAppearance( cancelButton, R.style.uiCancelBtn);
        UIUtilities.borderTransparentButton(getActivity(),cancelButton);
        cancelButton.setAllCaps(false);
        cancelButton.setText(cancelOption.getTitle());
        cancelButton.setOnClickListener(v -> sendResponse(IUIDisplay.UIResultCode.OK, cancelOption.getResponse(), ""));
    }

    @SuppressWarnings("deprecation")
    private void initValues(View view, DisplayRequest displayRequest) {
        ArrayList<DisplayFragmentOption> fragmentOptions = displayRequest.getUiExtras().getParcelableArrayList(IUIDisplay.uiScreenFragOptionList);
        RelativeLayout sale = view.findViewById(R.id.sale);
        TextView saleLabel = sale.findViewById(R.id.label);
        saleLabel.setText(fragmentOptions.get(0).getFragText());
        TextView saleValue = sale.findViewById(R.id.value);
        saleValue.setText(fragmentOptions.get(0).getFragAmount());

        RelativeLayout tip = view.findViewById(R.id.tip);
        TextView tipLabel = tip.findViewById(R.id.label);
        tipLabel.setText(fragmentOptions.get(1).getFragText());
        TextView tipValue = tip.findViewById(R.id.value);
        tipValue.setText(fragmentOptions.get(1).getFragAmount());
        int tindex = 2;
        if(fragmentOptions.size() == 4) {
            RelativeLayout cashback = view.findViewById(R.id.cashback);
            cashback.setVisibility(View.VISIBLE);
            TextView cashbackLabel = cashback.findViewById(R.id.label);
            cashbackLabel.setText(fragmentOptions.get(2).getFragText());
            TextView cashbackValue = cashback.findViewById(R.id.value);
            cashbackValue.setText(fragmentOptions.get(2).getFragAmount());
            tindex = 3;
        }

        RelativeLayout total = view.findViewById(R.id.total);
        TextView totalLabel = total.findViewById(R.id.label);
        totalLabel.setText(fragmentOptions.get(tindex).getFragText());
        TextView totalValue = total.findViewById(R.id.value);
        totalValue.setText(fragmentOptions.get(tindex).getFragAmount());
    }

    @SuppressWarnings("deprecation")
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
