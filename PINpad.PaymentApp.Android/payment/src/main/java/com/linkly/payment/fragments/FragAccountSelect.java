package com.linkly.payment.fragments;

import static com.linkly.libui.IUIDisplay.ACTIVITY_ID.ACT_SELECT_ACCOUNT;
import static com.linkly.libui.IUIDisplay.String_id.STR_CANCEL;
import static com.linkly.libui.IUIDisplay.String_id.STR_CHEQUE;
import static com.linkly.libui.IUIDisplay.String_id.STR_CREDIT;
import static com.linkly.libui.IUIDisplay.String_id.STR_SAVINGS;
import static com.linkly.payment.fragments.FragApplicationSelect.changeDrawable;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
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

public class FragAccountSelect extends BaseFragment<ActivityTransBinding, FragStandardViewModel> implements GestureDetector.OnDoubleTapListener {
    public static final String TAG = FragQuestion.class.getSimpleName();
    private FragStandardViewModel fragStandardViewModel;
    private String accessModeSelectedAccount = null;
    private Button accessModeCheque;
    private Button accessModeSavings;
    private Button accessModeCredit;
    private Button accessModeCancel;


    public static FragAccountSelect newInstance() {
        Bundle args = new Bundle();
        FragAccountSelect fragment = new FragAccountSelect();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public int getBindingVariable() {
        return com.linkly.payment.BR.fragStandardViewModel;
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_account_select;
    }

    @Override
    public FragStandardViewModel getViewModel() {
        fragStandardViewModel = new ViewModelProvider(this).get(FragStandardViewModel.class);
        fragStandardViewModel.init(ACT_SELECT_ACCOUNT);
        return fragStandardViewModel;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        if(isAccessMode()) {
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

    private boolean isAccessMode() {
        TransRec curTrans = Engine.getDep().getCurrentTransaction();
        return(curTrans != null && curTrans.getAudit() != null && curTrans.getAudit().isAccessMode());
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(isAccessMode()) {
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
        ArrayList<DisplayQuestion> displayOptions = displayRequest.getUiExtras().getParcelableArrayList(IUIDisplay.uiScreenOptionList);
        TextView title = view.findViewById(R.id.title);
        title.setText(titleText);

        TextView prompt = view.findViewById(R.id.prompt_text);
        prompt.setText(fragStandardViewModel.getPrompt().getValue());
        TextView amountLabel = view.findViewById(R.id.value_label);
        TextView value = view.findViewById(R.id.value);
        if (displayFragmentOptions != null) {
            amountLabel.setText(displayFragmentOptions.get(0).getFragText());
            value.setText(displayFragmentOptions.get(0).getFragAmount());
        }

        FragLed leds = (FragLed) getChildFragmentManager().findFragmentById(R.id.ctls_leds_frag);
        if (leds != null) {
            leds.refresh(requireContext().getApplicationContext());
        }

        if (displayOptions != null) {
            Button chequeButton = view.findViewById(R.id.cheque);
            chequeButton.setVisibility(View.GONE);
            Button savingsButton = view.findViewById(R.id.savings);
            savingsButton.setVisibility(View.GONE);
            Button creditButton = view.findViewById(R.id.credit);
            creditButton.setVisibility(View.GONE);
            Button cancelButton = view.findViewById(R.id.cancel_button);
            UIUtilities.borderTransparentButton(getActivity(),cancelButton);


            for (int index = 0; index < displayOptions.size(); index++) {
                String buttonText = displayOptions.get(index).getTitle();
                final int i = index;
                if (buttonText.equals(Engine.getDep().getPrompt(STR_CHEQUE))) {
                    chequeButton.setText(displayOptions.get(index).getTitle());
                    chequeButton.setOnClickListener(v -> sendResponse(IUIDisplay.UIResultCode.OK, displayOptions.get(i).getResponse(), ""));
                    chequeButton.setVisibility(View.VISIBLE);
                    changeDrawable(requireContext(),chequeButton);
                } else if (buttonText.equals(Engine.getDep().getPrompt(STR_SAVINGS))) {
                    savingsButton.setText(displayOptions.get(index).getTitle());
                    savingsButton.setOnClickListener(v -> sendResponse(IUIDisplay.UIResultCode.OK, displayOptions.get(i).getResponse(), ""));
                    savingsButton.setVisibility(View.VISIBLE);
                    changeDrawable(requireContext(),savingsButton);
                } else if (buttonText.equals(Engine.getDep().getPrompt(STR_CREDIT))) {
                    creditButton.setText(displayOptions.get(index).getTitle());
                    creditButton.setOnClickListener(v -> sendResponse(IUIDisplay.UIResultCode.OK, displayOptions.get(i).getResponse(), ""));
                    creditButton.setVisibility(View.VISIBLE);
                    changeDrawable(requireContext(),creditButton);
                } else if (buttonText.equals(Engine.getDep().getPrompt(STR_CANCEL))) {
                    cancelButton.setText(displayOptions.get(index).getTitle());
                    cancelButton.setOnClickListener(v -> sendResponse(IUIDisplay.UIResultCode.OK, displayOptions.get(i).getResponse(), ""));

                }
            }
        }
    }

    private int prevButtonId = 0;

    private View.OnTouchListener buttonTouchListener() {

        return new View.OnTouchListener() {
            GestureDetector gestureDetector = new GestureDetector(FragAccountSelect.this.getContext(), new GestureDetector.SimpleOnGestureListener() {
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


}
