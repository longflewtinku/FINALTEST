package com.linkly.payment.fragments;

import static com.linkly.libui.IUIDisplay.ACTIVITY_ID.ACT_INFORMATION_BASIC;

import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.util.Pair;
import androidx.core.widget.TextViewCompat;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.linkly.libengine.engine.Engine;
import com.linkly.libui.IUIDisplay;
import com.linkly.payment.R;
import com.linkly.payment.activities.ActScreenSaver;
import com.linkly.payment.databinding.ActivityTransBinding;
import com.linkly.payment.viewmodel.FragStandardViewModel;

import java.util.Calendar;
import java.util.Date;

public class FragPreAuthSearchAndViewSelectDate extends BaseFragment<ActivityTransBinding, FragStandardViewModel> implements OnClickListener {

    public static final String TAG = FragPreAuthSearchAndViewSelectDate.class.getSimpleName();
    private FragStandardViewModel fragStandardViewModel;

    private Button mPickDateButton;
    private Button mPickDateRangeButton;

    public static FragPreAuthSearchAndViewSelectDate newInstance() {
        Bundle args = new Bundle();
        FragPreAuthSearchAndViewSelectDate fragment = new FragPreAuthSearchAndViewSelectDate();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public int getBindingVariable() {
        return com.linkly.payment.BR.fragStandardViewModel;
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_pre_auth_search_and_view_select_date;
    }

    @Override
    public FragStandardViewModel getViewModel() {
        fragStandardViewModel = ViewModelProviders.of(this).get(FragStandardViewModel.class);
        fragStandardViewModel.init(ACT_INFORMATION_BASIC);
        return fragStandardViewModel;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);
        SetHeader(false, false);
        ActScreenSaver.cancelScreenSaver();
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mPickDateButton = view.findViewById(R.id.btn_pick_date);
        mPickDateRangeButton = view.findViewById(R.id.btn_pick_date_range);

        MaterialDatePicker.Builder<Long> materialDateBuilder = MaterialDatePicker.Builder.datePicker();
        MaterialDatePicker.Builder<Pair<Long, Long>> materialDateRangeBuilder = MaterialDatePicker.Builder.dateRangePicker();

        materialDateBuilder.setTheme(R.style.PaymentMaterialCalendarTheme);
        materialDateRangeBuilder.setTheme(R.style.PaymentMaterialCalendarTheme);

        final MaterialDatePicker<Long> materialDatePicker = materialDateBuilder.build();
        final MaterialDatePicker<Pair<Long, Long>> materialDateRangePicker = materialDateRangeBuilder.build();

        mPickDateButton.setOnClickListener(
                v -> materialDatePicker.show(getActivity().getSupportFragmentManager(), "MATERIAL_DATE_PICKER"));

        mPickDateRangeButton.setOnClickListener(
                v -> materialDateRangePicker.show(getActivity().getSupportFragmentManager(), "MATERIAL_DATE_RANGE_PICKER"));

        materialDatePicker.addOnPositiveButtonClickListener(
                selection ->
                    // Send startDate,endDate,<empty>,<empty>
                    sendResponse(IUIDisplay.UIResultCode.OK, selection+","+selection+",,", ""));


        materialDateRangePicker.addOnPositiveButtonClickListener(
                selection ->
                    // Send startDate,endDate,<empty>,<empty>
                    sendResponse(IUIDisplay.UIResultCode.OK, selection.first + "," + selection.second + ",,", ""));

        Button btnDone = view.findViewById(R.id.btn_done);
        GradientDrawable btnDrawable = (GradientDrawable) btnDone.getBackground().getConstantState().newDrawable().mutate();
        btnDrawable.setColor(Engine.getDep().getPayCfg().getBrandDisplayButtonColourOrDefault(getActivity().getColor(R.color.color_linkly_primary)));
        btnDone.setTextColor(Engine.getDep().getPayCfg().getBrandDisplayButtonTextColourOrDefault());
        btnDone.setBackground(btnDrawable);
        btnDone.setOnClickListener(this);

        Button btnToday = view.findViewById(R.id.btn_expire_today);
        btnToday.setOnClickListener(this);

        Button btnTomorrow = view.findViewById(R.id.btn_expire_tomorrow);
        btnTomorrow.setOnClickListener(this);

        Button btnRrn = view.findViewById(R.id.btn_search_by_rrn);
        btnRrn.setOnClickListener(this);

        Button btnAuthcode = view.findViewById(R.id.btn_search_by_authcode);
        btnAuthcode.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.btn_expire_today) {
            changePressedDrawable(v);
            String timeMillis = Long.toString(new Date().getTime());
            // Send startDate,endDate,<empty>,<empty>
            sendResponse(IUIDisplay.UIResultCode.OK, timeMillis+","+timeMillis+",,", "");
        }
        else if (v.getId() == R.id.btn_expire_tomorrow) {
            changePressedDrawable(v);
            Calendar c = Calendar.getInstance();
            c.setTime(new Date());
            c.add(Calendar.DATE, 1);
            String timeMillis = Long.toString(c.getTimeInMillis());
            // Send startDate,endDate,<empty>,<empty>
            sendResponse(IUIDisplay.UIResultCode.OK, timeMillis+","+timeMillis+",,", "");
        }
        else if (v.getId() == R.id.btn_search_by_rrn) {
            changePressedDrawable(v);
            sendResponse(IUIDisplay.UIResultCode.OK, "RRN", "");
        }
        else if (v.getId() == R.id.btn_search_by_authcode) {
            changePressedDrawable(v);
            // Send <empty>,<empty>,<empty>,search_authcode
            sendResponse(IUIDisplay.UIResultCode.OK, "AUTHCODE", "");
        }

        if (getBaseActivity() != null) {
            getBaseActivity().finishAfterTransition();
        }

    }

    private void changePressedDrawable(View view) {
        if (view instanceof Button) {
            GradientDrawable shapeDrawable = (GradientDrawable) ((Button) view).getBackground().getConstantState().newDrawable().mutate();
            shapeDrawable.setColor(ContextCompat.getColor(getContext(), R.color.colorBlack));
            ((Button) view).setBackground(shapeDrawable);
            ((Button) view).setTextColor(ContextCompat.getColor(getContext(), R.color.colorWhite));
            TextViewCompat.setTextAppearance(((Button) view), R.style.uiTipBtn);
        }
    }

}


