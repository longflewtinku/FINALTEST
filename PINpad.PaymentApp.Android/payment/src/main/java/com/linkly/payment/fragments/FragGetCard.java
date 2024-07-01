package com.linkly.payment.fragments;

import static android.view.View.GONE;
import static android.view.View.INVISIBLE;
import static android.view.View.TEXT_ALIGNMENT_CENTER;
import static android.view.View.VISIBLE;
import static com.linkly.libengine.status.IStatus.STATUS_EVENT.STATUS_ERR_CHIP_UNREADABLE;
import static com.linkly.libengine.status.IStatus.STATUS_EVENT.STATUS_ERR_INSERT_OR_SWIPE_CARD;
import static com.linkly.libsecapp.IP2PCard.CardType.CT_CTLS_FAULTY;
import static com.linkly.libsecapp.IP2PCard.CardType.CT_EMV_FAULTY;
import static com.linkly.libsecapp.IP2PCard.CardType.CT_MSR;
import static com.linkly.libsecapp.IP2PCard.CardType.CT_NONE;
import static com.linkly.libsecapp.IP2PCard.CardType.CT_TIMEOUT;
import static com.linkly.libui.IUIDisplay.ACTIVITY_ID.ACT_GET_CARD;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.widget.TextViewCompat;
import androidx.lifecycle.ViewModelProviders;

import com.linkly.kotlinandroidtoolkit.extensionfuncs.BundleExtensionsKt;
import com.linkly.libconfig.cpat.CardProductCfg;
import com.linkly.libengine.config.PayCfg;
import com.linkly.libengine.config.PayCfgFactory;
import com.linkly.libengine.engine.Engine;
import com.linkly.libengine.engine.transactions.TransRec;
import com.linkly.libengine.overrides.CoreOverrides;
import com.linkly.libmal.global.util.Util;
import com.linkly.libpositive.messages.IMessages;
import com.linkly.libpositive.wrappers.Surcharge;
import com.linkly.libpositive.wrappers.TagDataFromPOS;
import com.linkly.libsecapp.IP2PCard;
import com.linkly.libsecapp.P2PLib;
import com.linkly.libui.IUICurrency;
import com.linkly.libui.IUIDisplay;
import com.linkly.libui.IUIDisplay.String_id;
import com.linkly.libui.UI;
import com.linkly.libui.currency.CountryCode;
import com.linkly.libui.display.DisplayRequest;
import com.linkly.libui.speech.SpeechUtils;
import com.linkly.payment.R;
import com.linkly.payment.activities.ActScreenSaver;
import com.linkly.payment.databinding.ActivityTransBinding;
import com.linkly.payment.utilities.SwipeDetector;
import com.linkly.payment.utilities.UIUtilities;
import com.linkly.payment.viewmodel.FragStandardViewModel;

import java.util.List;
import java.util.Locale;

import timber.log.Timber;

public class FragGetCard extends BaseFragment<ActivityTransBinding, FragStandardViewModel> {

    public static final String TAG = FragGetCard.class.getSimpleName();
    private FragStandardViewModel fragStandardViewModel;
    private View accessModeOverlay;
    private AlertDialog alertDialog;

    private boolean emvEnabled = false;
    private boolean ctlsEnabled = false;
    private boolean swipeEnabled = false;
    private boolean manualEnabled = false;
    private boolean cdcvmEnabled = false;
    private boolean accessMode = false;

    FragLed ctls_leds;
    private TextView promptTv1;

    private Button accessButton;

    private PayCfg payCfg = null;

    public static FragGetCard newInstance() {
        Bundle args = new Bundle();
        FragGetCard fragment = new FragGetCard();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public int getBindingVariable() {
        return com.linkly.payment.BR.fragStandardViewModel;
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_getcard;
    }

    @Override
    public FragStandardViewModel getViewModel() {
        fragStandardViewModel = ViewModelProviders.of(this).get(FragStandardViewModel.class);
        fragStandardViewModel.init(ACT_GET_CARD);
        return fragStandardViewModel;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Timber.e("Getting paycfg");
        payCfg = new PayCfgFactory().getConfig(requireContext());
        Timber.e("Finished getting paycfg");
    }

    @Override
    public void onDestroyView() {
        if (alertDialog != null) {
            alertDialog.dismiss();
        }
        if(payCfg != null) {
            payCfg = null;
        }
        super.onDestroyView();
    }

    @SuppressWarnings("deprecation")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);

        if (v == null) {
            return null;
        }

        accessButton = v.findViewById(R.id.btn_access);
        accessButton.setTextColor(payCfg.getBrandDisplayButtonTextColourOrDefault());
        GradientDrawable shapeDrawable = (GradientDrawable) accessButton.getBackground().getConstantState().newDrawable().mutate();
        shapeDrawable.setColor(payCfg.getBrandDisplayButtonColourOrDefault(getActivity().getColor(R.color.color_linkly_primary)));
        accessButton.setBackground(shapeDrawable);
        TextViewCompat.setTextAppearance(accessButton, R.style.uiDoneBtn);

        getBaseActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        SetHeader(false, false);

        ctls_leds = (FragLed) getChildFragmentManager().findFragmentById(R.id.ctls_leds_frag);

        getBaseActivity().RegisterAsExtendedActivity(IMessages.APP_DISABLE_CANCEL_EVENT);

        accessModeOverlay = v.findViewById(R.id.access_mode_overlay);

        //Add Manual Card Entry Button
        Button manPanButton = v.findViewById(R.id.btn_manual);
        GradientDrawable btnDrawable = (GradientDrawable) manPanButton.getBackground();
        btnDrawable.setColor(payCfg.getBrandDisplayButtonColourOrDefault(getActivity().getColor(R.color.color_linkly_primary)));
        manPanButton.setTextColor(payCfg.getBrandDisplayButtonTextColourOrDefault());
        manPanButton.setBackground(btnDrawable);
        Button cancelBtn = v.findViewById(R.id.btn_cancel);
        UIUtilities.borderTransparentButton(getActivity(),cancelBtn);
        int sdk = Build.VERSION.SDK_INT;

        cancelBtn.setText(Engine.getDep().getPrompt(String_id.STR_CANCEL));

        // hide man pan entry button
        manPanButton.setText(Engine.getDep().getPrompt(String_id.STR_CARD_NOT_PRESENT));
        manPanButton.setVisibility(GONE);
        loadParam();
        if (sdk >= 25 && payCfg.isAccessMode()) {
            enableAccessMode();
        } else {
            v.findViewById(R.id.btn_access).setVisibility(GONE);
            v.findViewById(R.id.card_logos).setVisibility(GONE);
        }

        setupSurchargeDialog(v);

        if (!payCfg.isManualAllowed() && !accessMode) {
            TextViewCompat.setTextAppearance(cancelBtn, R.style.ui2LargeBtnFont);
        }

        RelativeLayout getCardScreen = v.findViewById(R.id.GetCardScreen);
        new SwipeDetector(getCardScreen).setOnSwipeListener((v1, swipeType) -> {
            if (swipeType == SwipeDetector.SwipeTypeEnum.LEFT_TO_RIGHT || swipeType == SwipeDetector.SwipeTypeEnum.RIGHT_TO_LEFT) {
                if (!getBaseActivity().cancelDisabled) {
                    Timber.d("OnSwipeDetector: Stop Card Read");
                    sendResponse(IUIDisplay.UIResultCode.ABORT, "", "");
                }
            }
        });

        //Cancel Button
        cancelBtn.setOnClickListener(view -> {
            if (!getBaseActivity().cancelDisabled) {
                Timber.d("Cancel Button: Stop Card Read");
                sendResponse(IUIDisplay.UIResultCode.ABORT, "", "");
            }
        });

        ActScreenSaver.cancelScreenSaver();

        showScreen(v);
        return v;
    }

    private View.OnTouchListener setOnTouchListener() {
        return new View.OnTouchListener() {
            final GestureDetector gestureDetector = new GestureDetector(FragGetCard.this.getContext(),
                    new GestureDetector.SimpleOnGestureListener() {
                        @Override
                        public boolean onDoubleTapEvent(MotionEvent e) {
                            setAccessModeField();
                            return super.onDoubleTapEvent(e);
                        }
                    });

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gestureDetector.onTouchEvent(event);
                return true;
            }
        };
    }

    private void setAccessModeField() {
        TransRec curTrans = Engine.getDep().getCurrentTransaction();
        if (curTrans != null)
            curTrans.getAudit().setAccessMode(true);

        sendResponse(IUIDisplay.UIResultCode.RESTART, "", "");
    }

    public void startAccessModeText() {
        TransRec curTrans = Engine.getDep().getCurrentTransaction();

        if (curTrans != null && curTrans.getAudit().isAccessMode()) {
            //====1.set speech rate
            SpeechUtils.getInstance().setSpeechRate((float) 0.9);
            //====2.set pause between statement, and start speak


            long majorAmount = curTrans.getAmounts().getTotalAmount() / 100;
            long minorAmount = curTrans.getAmounts().getTotalAmount() % 100;

            CountryCode cCode = payCfg.getCountryCode();

            String totalMajorAmount = Long.toString(majorAmount);
            String totalMinorAmount = Long.toString(minorAmount);

            String majorUnit = "Pound";
            String minorUnit = "Pence";


            if (cCode != null) {
                if (majorAmount != 1)
                    majorUnit = cCode.getMajWordPlural();
                else
                    majorUnit = cCode.getMajWord();

                if (minorAmount != 1)
                    minorUnit = cCode.getMinWordPlural();
                else
                    minorUnit = cCode.getMinWord();


                /* special case for ISK where currency does have minor units but they dont want to see them */
                if (cCode.getAlphaCode().contains("ISK")) {
                    totalMajorAmount = Long.toString(majorAmount * 100); /* as no minor units */
                    totalMinorAmount = "";
                    minorUnit = "";
                }
            }

            String sentence = String.format(UI.getInstance().getPrompt(configureSentence()), totalMajorAmount, majorUnit, totalMinorAmount, minorUnit);

            String[] splitSpeech = sentence.split("\\.");

            for (String speech : splitSpeech) {
                SpeechUtils.getInstance().speak(speech, 500);//500ms
            }
        }
    }

    private String_id configureSentence() {
        IUIDisplay.String_id text = IUIDisplay.String_id.STR_ACCESS_MODE_GET_CARD_TAP_SWIPE_INSERT_TEXT;
        if (ctlsEnabled && emvEnabled && swipeEnabled) {
            text = IUIDisplay.String_id.STR_ACCESS_MODE_GET_CARD_TAP_SWIPE_INSERT_TEXT;
        } else if (ctlsEnabled && emvEnabled) {
            text = IUIDisplay.String_id.STR_ACCESS_MODE_GET_CARD_TAP_INSERT_TEXT;
        } else if (ctlsEnabled && swipeEnabled) {
            text = IUIDisplay.String_id.STR_ACCESS_MODE_GET_CARD_TAP_SWIPE_TEXT;
        } else if (emvEnabled && swipeEnabled) {
            text = String_id.STR_ACCESS_MODE_GET_CARD_SWIPE_INSERT_TEXT;
        } else if (ctlsEnabled) {
            text = String_id.STR_ACCESS_MODE_GET_CARD_TAP_TEXT;
        } else if (emvEnabled) {
            text = String_id.STR_ACCESS_MODE_GET_CARD_INSERT_TEXT;
        } else if (swipeEnabled) {
            text = String_id.STR_ACCESS_MODE_GET_CARD_SWIPE_TEXT;
        }
        return text;
    }

    public void enableAccessMode() {
        if (accessButton == null)
            return;

        accessMode = true;
        accessButton.setVisibility(VISIBLE);

        TransRec trans = Engine.getDep().getCurrentTransaction();
        if (trans != null && trans.getAudit().isAccessMode()) {
            this.startAccessModeText();
        } else {
            accessButton.setOnClickListener(view -> {
                SpeechUtils.getInstance().stop();
                SpeechUtils.getInstance().speak("Double tap anywhere to enable accessibility mode");
                accessModeOverlay.setOnTouchListener(setOnTouchListener());
            });
        }
    }

    private void populateSurchargeTable(Surcharge[] scArray, LayoutInflater inflater, LinearLayout linearLayout) {
        for (Surcharge sc : scArray) {
            if (sc != null && Engine.getBinRangesCfg() != null) {
                // only display card types and surcharge rate if we have an entry for that card type in our card table
                CardProductCfg card = Engine.getBinRangesCfg().getCardFromBin(payCfg, Integer.parseInt(sc.getB()));
                if (card != null) {
                    View itemView = inflater.inflate(R.layout.surcharge_item, null);
                    TextView label = itemView.findViewById(R.id.card_type);
                    label.setText(card.getName());
                    TextView value = itemView.findViewById(R.id.surcharge_value);
                    value.setText(formatSurchargeValue(sc.getT(), sc.getV()));
                    linearLayout.addView(itemView);
                }
            }
        }
    }

    private boolean isSurchargeEnabled() {
        return payCfg != null &&
                payCfg.isSurchargeSupported();
    }

    private void setupSurchargeDialog(View view) {
        Button surchargeButton = view.findViewById(R.id.surcharge_btn);
        surchargeButton.setTextColor(payCfg.getBrandDisplayButtonTextColourOrDefault());
        GradientDrawable shapeDrawable = (GradientDrawable) surchargeButton.getBackground().getConstantState().newDrawable().mutate();
        shapeDrawable.setColor(payCfg.getBrandDisplayButtonColourOrDefault(getActivity().getColor(R.color.color_linkly_primary)));
        surchargeButton.setBackground(shapeDrawable);
        TextViewCompat.setTextAppearance(surchargeButton, R.style.uiDoneBtn);
        boolean showSurcharge = false;

        TransRec trans = Engine.getDep().getCurrentTransaction();

        surchargeButton.setOnClickListener(v -> {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            View surchargeView = inflater.inflate(R.layout.dialog_fragment_surcharge, null);
            LinearLayout linearLayout = surchargeView.findViewById(R.id.surcharge_items);
            Button dismissBtn = surchargeView.findViewById(R.id.dismiss_btn);
            TagDataFromPOS tagData = null;
            if (trans != null) {
                tagData = trans.getTagDataFromPos();
            }

            // if surcharge global flag is enabled in config, AND surcharge is allowed for this card type
            if (isSurchargeEnabled()) {
                // if PAD tag data from POS is present and includes SC2 tag, then use it
                if (tagData != null && tagData.getSC2() != null) {
                    populateSurchargeTable(tagData.getSC2(), inflater, linearLayout);
                } else {
                    if (payCfg != null) {
                        List<Surcharge> scList = payCfg.getDefaultSc();
                        Surcharge[] scArray = scList.toArray(new Surcharge[0]);
                        populateSurchargeTable(scArray, inflater, linearLayout);
                    }
                }

            }
            TextView textView = new TextView(getContext());
            textView.setText(R.string.SURCHARGES);
            textView.setTextAlignment(TEXT_ALIGNMENT_CENTER);
            textView.setTextAppearance(R.style.ui2LargeFont);
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
            alertDialogBuilder.setView(surchargeView);
            alertDialogBuilder.setCustomTitle(textView);
            alertDialog = alertDialogBuilder.create();
            GradientDrawable btnDrawable = (GradientDrawable) dismissBtn.getBackground();
            btnDrawable.setColor(payCfg.getBrandDisplayButtonColourOrDefault(getActivity().getColor(R.color.color_linkly_primary)));
            dismissBtn.setTextColor(payCfg.getBrandDisplayButtonTextColourOrDefault());
            dismissBtn.setBackground(btnDrawable);
            dismissBtn.setOnClickListener(btn -> alertDialog.dismiss());
            alertDialog.show();
        });

        // if surcharge allowed for txn type, and enabled in config
        if (trans != null && trans.getTransType().supportsSurcharge && isSurchargeEnabled()) {
            TagDataFromPOS tagData = trans.getTagDataFromPos();
            if (tagData != null && tagData.getSC2() != null && payCfg != null) {
                showSurcharge = true;
            } else if (payCfg != null && !payCfg.getDefaultSc().isEmpty()) {
                showSurcharge = true;
            }
        }

        if (showSurcharge) {
            surchargeButton.setVisibility(VISIBLE);
            ViewGroup.LayoutParams accessBtnLayoutParams = this.accessButton.getLayoutParams();
            LinearLayout.LayoutParams updatedLayoutParams = new LinearLayout.LayoutParams(accessBtnLayoutParams.width, accessBtnLayoutParams.height, 1.0f);
            this.accessButton.setLayoutParams(updatedLayoutParams);
        } else {
            surchargeButton.setVisibility(GONE);
        }
    }

    private String formatSurchargeValue(String valueType, String value) {
        String formattedValueString = "";
        switch (valueType) {
            case "$": {
                int dollarInt = Integer.parseInt(value);
                Double dollarDouble = dollarInt / 100.00;
                formattedValueString = String.format(Locale.getDefault(), "$ %.2f", dollarDouble);
                break;
            }
            case "%": {
                int percentInt = Integer.parseInt(value);
                Double percentDouble = percentInt / 100.00;
                formattedValueString = String.format(Locale.getDefault(), "%.2f%%", percentDouble);
                break;
            }
        }
        return formattedValueString;
    }

    public void showScreen(View v) {
        getBaseActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        initViews(v);
        configureUiScreens(v);
    }

    @Override
    public void onDestroy() {
        FragLed.pauseLedTimer();
        payCfg = null;
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        FragLed.startLedTimer(ctls_leds);
        showScreen(getView());

    }

    @Override
    public void onPause() {
        super.onPause();
        FragLed.pauseLedTimer();
        SpeechUtils.getInstance().stop();
    }

    @SuppressWarnings("deprecation")
    protected void loadParam() {
        DisplayRequest displayRequest = fragStandardViewModel.getDisplay().getValue();
        if (displayRequest != null) {
            IUIDisplay.SCREEN_ID iScreenId = BundleExtensionsKt.getSerializableCompat(
                    fragStandardViewModel.getDisplay().getValue().getUiExtras(),
                    IUIDisplay.uiScreenID,
                    IUIDisplay.SCREEN_ID.class);

            //Cdcvm
            if (iScreenId == IUIDisplay.SCREEN_ID.GET_CARD_CDCVM) {
                cdcvmEnabled = true;
            }

            //Icc
            switch (iScreenId) {
                case GET_CARD_CDCVM:
                case GET_CARD_MSR_ICC_CTLS:
                case GET_CARD_MSR_ICC_CTLS_MAN:
                case GET_CARD_ICC_CTLS:
                case GET_CARD_MAN_CTLS_ICC:
                case GET_CARD_MSR_ICC_MAN:
                case GET_CARD_ICC:
                case GET_CARD_ICC_MAN:
                case GET_CARD_ICC_MSR:
                    emvEnabled = true;
                    break;
                default:
                    break;
            }

            //CTLS
            switch (iScreenId) {
                case GET_CARD_CDCVM:
                case GET_CARD_CTLS_MSR:
                case GET_CARD_MSR_ICC_CTLS:
                case GET_CARD_MSR_MAN_CTLS:
                case GET_CARD_MSR_ICC_CTLS_MAN:
                case GET_CARD_CTLS:
                case GET_CARD_ICC_CTLS:
                case GET_CARD_CTLS_MAN:
                case GET_CARD_MAN_CTLS_ICC:
                    ctlsEnabled = true;
                    break;
                default:
                    break;
            }

            //MSR
            switch (iScreenId) {
                case GET_CARD_CDCVM:
                case GET_CARD_MSR:
                case GET_CARD_ICC_MSR:
                case GET_CARD_CTLS_MSR:
                case GET_CARD_MSR_MAN:
                case GET_CARD_MSR_ICC_CTLS:
                case GET_CARD_MSR_MAN_CTLS:
                case GET_CARD_MSR_ICC_MAN:
                case GET_CARD_MSR_ICC_CTLS_MAN:
                    swipeEnabled = true;
                    break;
                default:
                    break;
            }


            //Separate case for just manual pan entry icon
            switch (iScreenId) {
                case GET_CARD_MAN:
                case GET_CARD_ICC_MAN:
                case GET_CARD_CTLS_MAN:
                case GET_CARD_MSR_MAN:
                case GET_CARD_MSR_ICC_MAN:
                case GET_CARD_MSR_MAN_CTLS:
                case GET_CARD_MAN_CTLS_ICC:
                case GET_CARD_MSR_ICC_CTLS_MAN:
                    manualEnabled = true;
                    break;
                default:
                    break;
            }
        }

        if (!payCfg.isManualAllowed()) {
            manualEnabled = false;
        }

        //if amount is above the max ctls limit then don't allow it
        TransRec trans = Engine.getDep().getCurrentTransaction();
        if (trans != null) {
            if (trans.getAmounts().getTotalAmount() > P2PLib.getInstance().getIP2PCtls().ctlsGetMaxAmount(trans.isStartedInOfflineMode())) {
                ctlsEnabled = false;
            }
        }
    }

    protected void initViews(View v) {
        //Get the Display Elements References for this screen
        TextView titleTv = v.findViewById(R.id.header_title);
        TextView amountTv = v.findViewById(R.id.amount_txt);

        // pwcb elements
        FrameLayout amountTotalFl = v.findViewById(R.id.amount_total);
        FrameLayout amountCashFl = v.findViewById(R.id.amount_cash);
        FrameLayout amountPurchFl = v.findViewById(R.id.amount_purch);

        promptTv1 = v.findViewById(R.id.prompt_title);

        //Get the Current Transaction so we can render the relevant data
        TransRec curTrans = Engine.getDep().getCurrentTransaction();

        if (curTrans != null) {

            //Display the Transaction Amount
            if (curTrans.getAmounts().getAmountUserEntered() != null && curTrans.getAmounts().getAmountUserEntered().length() > 0 && curTrans.getAmounts().getTotalAmount() > 0) {

                if (curTrans.isCashback()) {
                    // very special pwcb view, showing breakdown of purch, cash, and total values.
                    // hide 'regular' textviews
                    titleTv.setVisibility(GONE);
                    amountTv.setVisibility(GONE);
                    // Hack for Landscape mode
                    if (v.findViewById(R.id.header_layout) != null) {
                        v.findViewById(R.id.header_layout).setVisibility(GONE);
                    }

                    TextView amountTotalText = v.findViewById(R.id.amount_total_text);
                    TextView amountCashText = v.findViewById(R.id.amount_cash_text);
                    TextView amountPurchText = v.findViewById(R.id.amount_purch_text);

                    String totalAmount = Long.toString(curTrans.getAmounts().getTotalAmount());
                    String totalAmountFormatted = Engine.getDep().getFramework().getCurrency().formatUIAmount(totalAmount, IUICurrency.EAmountFormat.FMT_AMT_SHOW_SYMBOL, payCfg.getCountryCode());
                    amountTotalText.setText(totalAmountFormatted);

                    String cashAmount = Long.toString(curTrans.getAmounts().getCashbackAmount());
                    String cashAmountFormatted = Engine.getDep().getFramework().getCurrency().formatUIAmount(cashAmount, IUICurrency.EAmountFormat.FMT_AMT_SHOW_SYMBOL, payCfg.getCountryCode());
                    amountCashText.setText(cashAmountFormatted);

                    String purchAmount = Long.toString(curTrans.getAmounts().getTotalAmountWithoutCashback());
                    String purchAmountFormatted = Engine.getDep().getFramework().getCurrency().formatUIAmount(purchAmount, IUICurrency.EAmountFormat.FMT_AMT_SHOW_SYMBOL, payCfg.getCountryCode());
                    amountPurchText.setText(purchAmountFormatted);

                } else {
                    amountTotalFl.setVisibility(GONE);
                    amountCashFl.setVisibility(GONE);
                    amountPurchFl.setVisibility(GONE);
                    titleTv.setText(curTrans.getTransType().getDisplayName());
                    String totalAmount = Long.toString(curTrans.getAmounts().getTotalAmount());
                    String amt = Engine.getDep().getFramework().getCurrency().formatUIAmount(totalAmount, IUICurrency.EAmountFormat.FMT_AMT_SHOW_SYMBOL, payCfg.getCountryCode());
                    amountTv.setText(amt);
                }
            } else {
                //Hide it the text but keep the space
                amountTv.setVisibility(INVISIBLE);
            }
        } else {
            amountTv.setVisibility(INVISIBLE);
            titleTv.setVisibility(INVISIBLE);
            amountTotalFl.setVisibility(GONE);
            amountCashFl.setVisibility(GONE);
            amountPurchFl.setVisibility(GONE);
        }
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            sendResponse(IUIDisplay.UIResultCode.ABORT, "", "");
            return true;
        }

        return super.onKey(v, keyCode, event);
    }

    public void configureUiImages(View v) {
        ImageView paymentOptionImageView = v.findViewById(R.id.payment_option_icon);
        if (ctlsEnabled) {
            paymentOptionImageView.setVisibility(VISIBLE);
            paymentOptionImageView.setImageResource(R.drawable.ic_tap);
        } else if (emvEnabled) {
            paymentOptionImageView.setVisibility(VISIBLE);
            paymentOptionImageView.setImageResource(R.drawable.ic_insert_card);
        } else if (swipeEnabled) {
            paymentOptionImageView.setVisibility(VISIBLE);
            paymentOptionImageView.setImageResource(R.drawable.ic_swipe_card);
        }

        Button manPanButton = v.findViewById(R.id.btn_manual);
        if (manualEnabled) {
            manPanButton.setVisibility(VISIBLE);
        } else {
            manPanButton.setVisibility(GONE);
        }
    }

    public void configureUiScreens(View v) {

        //Set Prompt Text as required based on the Orginal Display Request

        if (cdcvmEnabled) {
            promptTv1.setText(Engine.getDep().getPrompt(IUIDisplay.String_id.STR_SEE_CONSUMER_DEVICE));
        } else if (ctlsEnabled && emvEnabled && swipeEnabled) {
            promptTv1.setText(Engine.getDep().getPrompt(IUIDisplay.String_id.STR_PLEASE_TAP_SWIPE_OR_INSERT));
        } else if (ctlsEnabled && emvEnabled) {
            promptTv1.setText(Engine.getDep().getPrompt(IUIDisplay.String_id.STR_PLEASE_TAP_OR_INSERT));
        } else if (ctlsEnabled && swipeEnabled) {
            promptTv1.setText(Engine.getDep().getPrompt(IUIDisplay.String_id.STR_PLEASE_TAP_OR_SWIPE));
        } else if (ctlsEnabled) {
            promptTv1.setText(Engine.getDep().getPrompt(IUIDisplay.String_id.STR_PLEASE_TAP));
        } else if (emvEnabled && swipeEnabled) {
            promptTv1.setText(Engine.getDep().getPrompt(IUIDisplay.String_id.STR_PLEASE_SWIPE_OR_INSERT));
            Engine.getDep().getStatusReporter().reportStatusEvent(STATUS_ERR_INSERT_OR_SWIPE_CARD , Engine.getDep().getCurrentTransaction().isSuppressPosDialog());
        } else if (emvEnabled) {
            promptTv1.setText(Engine.getDep().getPrompt(IUIDisplay.String_id.STR_PLEASE_INSERT));
        } else if (swipeEnabled) {
            promptTv1.setText(Engine.getDep().getPrompt(IUIDisplay.String_id.STR_PLEASE_SWIPE));
        } else {
            promptTv1.setText(Engine.getDep().getPrompt(IUIDisplay.String_id.STR_MANUAL_ENTRY));
        }


        if (!ctlsEnabled && Engine.getDep().getCurrentTransaction() != null) {
            Engine.getDep().getCurrentTransaction().getCard().setCtlsAllowed(false);
        }

        configureUiImages(v);

    }

    // Required to disable on pause response on a fragment.
    // (In the base fragment allowOnPauseResponse defaults to true and will respond with an abort to the workflow)
    // Let the card reader and workflow logic control timeouts etc logic rather than this.
    @Override
    public boolean allowOnPauseResponse() {
        Timber.i("Frag Get card, Disabling on Pause Response");
        return false;
    }
}





