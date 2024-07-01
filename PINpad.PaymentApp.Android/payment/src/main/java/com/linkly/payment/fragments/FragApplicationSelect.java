package com.linkly.payment.fragments;

import static com.linkly.libui.IUIDisplay.String_id.STR_CANCEL;
import static com.linkly.libui.IUIDisplay.String_id.STR_CHEQUE;
import static com.linkly.libui.IUIDisplay.String_id.STR_CREDIT;
import static com.linkly.libui.IUIDisplay.String_id.STR_SAVINGS;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.widget.TextViewCompat;
import androidx.lifecycle.ViewModelProvider;

import com.linkly.libengine.engine.Engine;
import com.linkly.libengine.engine.transactions.TransRec;
import com.linkly.libmal.MalFactory;
import com.linkly.libui.IUIDisplay;
import com.linkly.libui.display.DisplayFragmentOption;
import com.linkly.libui.display.DisplayQuestion;
import com.linkly.libui.display.DisplayRequest;
import com.linkly.libui.speech.SpeechUtils;
import com.linkly.payment.R;
import com.linkly.payment.activities.ActScreenSaver;
import com.linkly.payment.databinding.ActivityTransBinding;
import com.linkly.payment.utilities.UIUtilities;
import com.linkly.payment.viewmodel.FragStandardViewModel;
import com.pax.neptunelite.api.NeptuneLiteUser;

import java.io.File;
import java.util.ArrayList;

import timber.log.Timber;

public class FragApplicationSelect extends BaseFragment<ActivityTransBinding, FragStandardViewModel> implements GestureDetector.OnDoubleTapListener {
    public static final String TAG = FragApplicationSelect.class.getSimpleName();
    private FragStandardViewModel fragStandardViewModel;
    private String accessModeSelectedAccount = null;
    private Button accessModeCheque;
    private Button accessModeSavings;
    private Button accessModeCredit;
    private Button accessModeCancel;


    public static FragApplicationSelect newInstance() {
        Bundle args = new Bundle();
        FragApplicationSelect fragment = new FragApplicationSelect();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public int getBindingVariable() {
        return com.linkly.payment.BR.fragStandardViewModel;
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_application_select;
    }

    @Override
    public FragStandardViewModel getViewModel() {
        fragStandardViewModel = new ViewModelProvider(this).get(FragStandardViewModel.class);
        fragStandardViewModel.init(IUIDisplay.ACTIVITY_ID.ACT_SELECT_APPLICATION);
        return fragStandardViewModel;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        TransRec curTrans = Engine.getDep().getCurrentTransaction();
        if (curTrans.getAudit().isAccessMode()) {
            initAccessModeView(view);
            try {
                NeptuneLiteUser.getInstance().getDal(getBaseActivity()).getSys().showStatusBar(false);
            } catch (Exception e) {
                Timber.w(e);
            }
        } else {
            initView(view);
        }
        ActScreenSaver.cancelScreenSaver();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        TransRec curTrans = Engine.getDep().getCurrentTransaction();
        if (curTrans.getAudit().isAccessMode()) {
            SpeechUtils.getInstance().speak(getResources().getString(R.string.ACCESS_MODE_ACCOUNT_SELECT_INSRUCTIONS));
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        try {
            NeptuneLiteUser.getInstance().getDal(getBaseActivity()).getSys().showStatusBar(true);
        } catch (Exception e) {
            Timber.w(e);
        }
    }

    @SuppressWarnings("deprecation")
    private void initAccessModeView(View view) {
        View normalMode = view.findViewById(R.id.normal_mode);
        normalMode.setVisibility(View.GONE);
        View accessMode = view.findViewById(R.id.access_mode);
        accessMode.setVisibility(View.VISIBLE);
        accessModeCheque = accessMode.findViewById(R.id.cheque_access_mode);
        accessModeSavings = accessMode.findViewById(R.id.savings_access_mode);
        accessModeCredit = accessMode.findViewById(R.id.credit_access_mode);
        accessModeCancel = accessMode.findViewById(R.id.cancel_access_mode);
        accessMode.findViewById(R.id.access_mode_overlay).setOnTouchListener(buttonTouchListener());

        DisplayRequest displayRequest = fragStandardViewModel.getDisplay().getValue();
        if (displayRequest != null) {
            ArrayList<DisplayQuestion> displayOptions = displayRequest.getUiExtras().getParcelableArrayList(IUIDisplay.uiScreenOptionList);
            if (displayOptions != null) {
                int noOfBtnsTodisplay = displayOptions.size();

                for (int index = 0; index < displayOptions.size(); index++) {
                    String buttonText = displayOptions.get(index).getTitle();
                    if (buttonText.equals(Engine.getDep().getPrompt(STR_CHEQUE))) {
                        accessModeCheque.setText(displayOptions.get(index).getTitle());
                        changeDrawable(requireContext(),accessModeCheque);
                    } else if (buttonText.equals(Engine.getDep().getPrompt(STR_SAVINGS))) {
                        accessModeSavings.setText(displayOptions.get(index).getTitle());
                        changeDrawable(requireContext(),accessModeSavings);
                    } else if (buttonText.equals(Engine.getDep().getPrompt(STR_CREDIT))) {
                        accessModeCredit.setText(displayOptions.get(index).getTitle());
                        changeDrawable(requireContext(),accessModeCredit);
                    } else if (buttonText.equals(Engine.getDep().getPrompt(STR_CANCEL))) {
                        accessModeCancel.setText(displayOptions.get(index).getTitle());
                        UIUtilities.borderTransparentButton(getActivity(),accessModeCancel);
                    }
                }

            }
        }
    }

    @SuppressWarnings("deprecation")
    private void initView(View view) {
        Bitmap logoBm = getLogo();
        if (logoBm != null) {
            ImageView logo = view.findViewById(R.id.logo);
            logo.setImageBitmap(logoBm);
        }

        String titleText;
        DisplayRequest displayRequest = fragStandardViewModel.getDisplay().getValue();

        if (fragStandardViewModel.getTitle().getValue() != null) {
            titleText = fragStandardViewModel.getTitle().getValue();
        } else {
            titleText = getResources().getString(R.string.SALE);
        }

        ArrayList<DisplayFragmentOption> displayFragmentOptions = displayRequest.getUiExtras().getParcelableArrayList(IUIDisplay.uiScreenFragOptionList);
        TextView title = view.findViewById(R.id.title);
        title.setText(titleText);

        TextView prompt = view.findViewById(R.id.prompt_text);
        prompt.setText(fragStandardViewModel.getPrompt().getValue());
        TextView amountLabel = view.findViewById(R.id.value_label);
        TextView value = view.findViewById(R.id.value);

        //configure Buttons

        if (displayFragmentOptions != null) {
            amountLabel.setText(displayFragmentOptions.get(0).getFragText());
            value.setText(displayFragmentOptions.get(0).getFragAmount());
        }

        FragLed leds = (FragLed) getChildFragmentManager().findFragmentById(R.id.ctls_leds_frag);
        if (leds != null) {
            leds.refresh(requireContext().getApplicationContext());
        }
        configureButtons(view);


    }

    @SuppressWarnings("deprecation")
    private void configureButtons(View v) {
        ArrayList<DisplayQuestion> options;
        //Dynamically display all buttons

        int btnCount = 0;
        options = fragStandardViewModel.getDisplay().getValue().getUiExtras().getParcelableArrayList(IUIDisplay.uiScreenOptionList);

        //Get the Display Elements References for this screen
        Button okButton = v.findViewById(R.id.ok_button);

        if (options != null && !options.isEmpty()) {
            //If options are provided hide the standard OK Button
            okButton.setVisibility(View.GONE);

            LinearLayout btnArea = v.findViewById(R.id.buttons_layout);
            for (btnCount = 0; btnCount < options.size(); btnCount++) {
                Button extraBtn = new Button(getBaseActivity());
                DisplayQuestion option = options.get(btnCount);

                extraBtn.setText(option.getTitle());
                extraBtn.setLayoutParams(okButton.getLayoutParams());
                extraBtn.setBackground(okButton.getBackground());
                changeDrawable(requireContext(),extraBtn);
                extraBtn.setOnClickListener(v1 -> sendResponse(IUIDisplay.UIResultCode.OK, option.getResponse(), ""));
                //Add  Buttons
                btnArea.addView(extraBtn);
//              Apply Custom Styling
                if (option.getStyle() == DisplayQuestion.EButtonStyle.BTN_STYLE_RED) {
                    GradientDrawable shapeDrawable = (GradientDrawable) extraBtn.getBackground().getConstantState().newDrawable().mutate();
                    shapeDrawable.setColor(ContextCompat.getColor(requireContext(), R.color.colorRed));
                    extraBtn.setBackground(ContextCompat.getDrawable(requireContext(),R.drawable.ui2_buttonstyle_cancel));
                    TextViewCompat.setTextAppearance(extraBtn, R.style.getCardScreenBtnFontCancel);
                } else if (option.getStyle() == DisplayQuestion.EButtonStyle.BTN_STYLE_TRANSPARENT) {
                    extraBtn.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.ui2_buttonstyle_cancel));
                    TextViewCompat.setTextAppearance(extraBtn, R.style.uiCancelBtn);
                    UIUtilities.borderTransparentButton(getActivity(),extraBtn);
                } else if (option.getStyle() == DisplayQuestion.EButtonStyle.BTN_STYLE_PRIMARY_DEFAULT_DOUBLE) {
                    GradientDrawable shapeDrawable = (GradientDrawable) extraBtn.getBackground().getConstantState().newDrawable().mutate();
                    shapeDrawable.setColor(Engine.getDep().getPayCfg().getBrandDisplayButtonColourOrDefault(requireContext().getColor(R.color.color_linkly_primary)));
                    extraBtn.setBackground(shapeDrawable);
                    extraBtn.setTextColor(Engine.getDep().getPayCfg().getBrandDisplayButtonTextColourOrDefault());
                    TextViewCompat.setTextAppearance(extraBtn, R.style.uiDoneBtn);
                } else {
                    extraBtn.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.ui2_buttonstyle));
                    TextViewCompat.setTextAppearance(extraBtn, R.style.ui2LargeBtnFont);
                }

            }
        }
    }

    private int prevButtonId = 0;

    private View.OnTouchListener buttonTouchListener() {

        return new View.OnTouchListener() {
            GestureDetector gestureDetector = new GestureDetector(FragApplicationSelect.this.getContext(), new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onDoubleTapEvent(MotionEvent e) {
                    SpeechUtils.getInstance().stop();
                    if (accessModeSelectedAccount != null) {
                        sendResponse(IUIDisplay.UIResultCode.OK, accessModeSelectedAccount, "");
                    }
                    return super.onDoubleTapEvent(e);
                }

                @Override
                public boolean onSingleTapConfirmed(MotionEvent e) {
                    selectButton(e.getY());
                    return super.onSingleTapConfirmed(e);
                }

                @Override
                public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                    selectButton(e2.getY());
                    return super.onScroll(e1, e2, distanceX, distanceY);
                }
            });

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gestureDetector.onTouchEvent(event);
                return true;
            }
        };
    }

    @SuppressWarnings("deprecation")
    private void selectButton(float y) {
        DisplayRequest displayRequest = fragStandardViewModel.getDisplay().getValue();
        if (displayRequest != null) {
            ArrayList<DisplayQuestion> displayOptions = displayRequest.getUiExtras().getParcelableArrayList(IUIDisplay.uiScreenOptionList);
            if (displayOptions != null) {
                int accessModeChequeMaxY = accessModeCheque.getHeight() + accessModeCheque.getTop();
                int accessModeSavingsMaxY = accessModeSavings.getHeight() + accessModeSavings.getTop();
                int accessModeCreditMaxY = accessModeCredit.getHeight() + accessModeCredit.getTop();
                int accessModeCancelMaxY = accessModeCancel.getHeight() + accessModeCancel.getTop();
                if (accessModeCheque.getTop() < y && accessModeChequeMaxY >= y && prevButtonId != R.id.cheque_access_mode) {
                    prevButtonId = R.id.cheque_access_mode;
                    accessModeButtonAction(R.id.cheque_access_mode, displayOptions.get(0).getResponse());
                } else if (accessModeSavings.getTop() < y && accessModeSavingsMaxY >= y && prevButtonId != R.id.savings_access_mode) {
                    prevButtonId = R.id.savings_access_mode;
                    accessModeButtonAction(R.id.savings_access_mode, displayOptions.get(1).getResponse());
                } else if (accessModeCredit.getTop() < y && accessModeCreditMaxY >= y && prevButtonId != R.id.credit_access_mode) {
                    prevButtonId = R.id.credit_access_mode;
                    accessModeButtonAction(R.id.credit_access_mode, displayOptions.get(2).getResponse());
                } else if (accessModeCancel.getTop() < y && accessModeCancelMaxY >= y && prevButtonId != R.id.cancel_access_mode) {
                    prevButtonId = R.id.cancel_access_mode;
                    accessModeButtonAction(R.id.cancel_access_mode, displayOptions.get(3).getResponse());
                }
            }
        }
    }

    private void accessModeButtonAction(int buttonId, String displayOption) {
        switch (buttonId) {
            case R.id.cheque_access_mode: {
                accessModeSelectedAccount = displayOption;
                SpeechUtils.getInstance().stop();
                SpeechUtils.getInstance().speak(getResources().getString(R.string.ACCESS_MODE_CHEQUE_SELECTION));
                break;
            }
            case R.id.savings_access_mode: {
                accessModeSelectedAccount = displayOption;
                SpeechUtils.getInstance().stop();
                SpeechUtils.getInstance().speak(getResources().getString(R.string.ACCESS_MODE_SAVINGS_SELECTION));
                break;
            }
            case R.id.credit_access_mode: {
                accessModeSelectedAccount = displayOption;
                SpeechUtils.getInstance().stop();
                SpeechUtils.getInstance().speak(getResources().getString(R.string.ACCESS_MODE_CREDIT_SELECTION));
                break;
            }
            case R.id.cancel_access_mode: {
                accessModeSelectedAccount = displayOption;
                SpeechUtils.getInstance().stop();
                SpeechUtils.getInstance().speak(getResources().getString(R.string.ACCESS_MODE_CANCEL_SELECTION));
                break;
            }
        }
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

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {

        return false;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
        return false;
    }

    public static void changeDrawable(Context context, Button button) {
        GradientDrawable btnDrawable = (GradientDrawable) button.getBackground().getConstantState().newDrawable().mutate();
        btnDrawable.setColor(Engine.getDep().getPayCfg().getBrandDisplayButtonColourOrDefault(context.getColor(R.color.color_linkly_primary)));
        button.setTextColor(Engine.getDep().getPayCfg().getBrandDisplayButtonTextColourOrDefault());
        button.setBackground(btnDrawable);
    }
}
