package com.linkly.payment.fragments;

import static android.widget.LinearLayout.LayoutParams.MATCH_PARENT;
import static android.widget.LinearLayout.LayoutParams.WRAP_CONTENT;
import static com.linkly.libui.IUIDisplay.ACTIVITY_ID.ACT_QUESTION;
import static com.linkly.libui.display.DisplayQuestion.EButtonStyle.BTN_STYLE_DEFAULT_DOUBLE;
import static com.linkly.libui.display.DisplayQuestion.EButtonStyle.BTN_STYLE_DEFAULT_LEFT_ALIGNED_TEXT;
import static com.linkly.libui.display.DisplayQuestion.EButtonStyle.BTN_STYLE_GREEN;
import static com.linkly.libui.display.DisplayQuestion.EButtonStyle.BTN_STYLE_GREY_LEFT;
import static com.linkly.libui.display.DisplayQuestion.EButtonStyle.BTN_STYLE_GREY_RIGHT;
import static com.linkly.libui.display.DisplayQuestion.EButtonStyle.BTN_STYLE_LEFT;
import static com.linkly.libui.display.DisplayQuestion.EButtonStyle.BTN_STYLE_PRIMARY_BORDER;
import static com.linkly.libui.display.DisplayQuestion.EButtonStyle.BTN_STYLE_PRIMARY_BORDER_DOUBLE;
import static com.linkly.libui.display.DisplayQuestion.EButtonStyle.BTN_STYLE_PRIMARY_BORDER_LEFT;
import static com.linkly.libui.display.DisplayQuestion.EButtonStyle.BTN_STYLE_PRIMARY_DEFAULT;
import static com.linkly.libui.display.DisplayQuestion.EButtonStyle.BTN_STYLE_PRIMARY_DEFAULT_DOUBLE;
import static com.linkly.libui.display.DisplayQuestion.EButtonStyle.BTN_STYLE_PRIMARY_DEFAULT_RIGHT;
import static com.linkly.libui.display.DisplayQuestion.EButtonStyle.BTN_STYLE_RED;
import static com.linkly.libui.display.DisplayQuestion.EButtonStyle.BTN_STYLE_RED_DOUBLE;
import static com.linkly.libui.display.DisplayQuestion.EButtonStyle.BTN_STYLE_RIGHT;
import static com.linkly.libui.display.DisplayQuestion.EButtonStyle.BTN_STYLE_TRANSPARENT;
import static com.linkly.libui.display.DisplayQuestion.EButtonStyle.BTN_STYLE_TRANSPARENT_DOUBLE;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.widget.TextViewCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.gridlayout.widget.GridLayout;
import androidx.lifecycle.ViewModelProvider;

import com.linkly.kotlinandroidtoolkit.extensionfuncs.BundleExtensionsKt;
import com.linkly.libengine.engine.Engine;
import com.linkly.libmal.global.util.DisplayKiosk;
import com.linkly.libmal.global.util.Util;
import com.linkly.libui.IUIDisplay;
import com.linkly.libui.display.DisplayFragmentOption;
import com.linkly.libui.display.DisplayQuestion;
import com.linkly.payment.R;
import com.linkly.payment.activities.ActScreenSaver;
import com.linkly.payment.databinding.ActivityTransBinding;
import com.linkly.payment.utilities.UIUtilities;
import com.linkly.payment.viewmodel.FragStandardViewModel;

import java.util.ArrayList;

import timber.log.Timber;

public class FragQuestion extends BaseFragment<ActivityTransBinding, FragStandardViewModel> implements View.OnClickListener {

    public static final String TAG = FragQuestion.class.getSimpleName();
    private FragStandardViewModel fragStandardViewModel;
    private static final int CUSTOM_OFFSET = 1000;
    private ArrayList<DisplayQuestion> options;
    private ArrayList<DisplayFragmentOption> fragOptions;

    public static FragQuestion newInstance() {
        Bundle args = new Bundle();
        FragQuestion fragment = new FragQuestion();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public int getBindingVariable() {
        return com.linkly.payment.BR.fragStandardViewModel;
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_question;
    }

    @Override
    public FragStandardViewModel getViewModel() {
        fragStandardViewModel = new ViewModelProvider(this).get(FragStandardViewModel.class);
        fragStandardViewModel.init(ACT_QUESTION);
        return fragStandardViewModel;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);
        initViews(v);
        ActScreenSaver.cancelScreenSaver();
        return v;
    }

    @SuppressWarnings("deprecation")
    private void configureFragments() {
        Timber.d("configureFragments...");

        fragOptions = fragStandardViewModel.getDisplay().getValue().getUiExtras().getParcelableArrayList(IUIDisplay.uiScreenFragOptionList);

        IUIDisplay.FRAG_TYPE fragType = BundleExtensionsKt.getSerializableCompat(
                fragStandardViewModel.getDisplay().getValue().getUiExtras(),
                IUIDisplay.uiScreenFragType,
                IUIDisplay.FRAG_TYPE.class);

        if (fragType != IUIDisplay.FRAG_TYPE.FRAG_NOT_SET && fragOptions != null && fragOptions.size() > 0) {
            Timber.d("..fragOptions are populated...");

            if (fragType == IUIDisplay.FRAG_TYPE.FRAG_MOTO) {
                Timber.d("..configure for: FRAG_MOTO...");
                FragmentManager fm = getChildFragmentManager();
                FragmentTransaction fragmentTransaction = fm.beginTransaction();

                FragMoto f1 = new FragMoto();
                f1.fragOptions = fragOptions;
                fragmentTransaction.replace(R.id.extended_info_fragment_frame, f1);
                fragmentTransaction.commit();
            } else if (fragType == IUIDisplay.FRAG_TYPE.FRAG_GRID) {
                Timber.d("..configure for: FRAG_GRID...");
                FragmentManager fm = getChildFragmentManager();
                FragmentTransaction fragmentTransaction = fm.beginTransaction();

                FragGrid f1 = new FragGrid();
                f1.fragOptions = fragOptions;
                fragmentTransaction.replace(R.id.extended_info_fragment_frame, f1);
                fragmentTransaction.commit();
            } else if (fragType == IUIDisplay.FRAG_TYPE.FRAG_GRID_GENERIC) {
                Timber.d("..configure for: FRAG_GRID_GENERIC...");
                FragmentManager fm = getChildFragmentManager();
                FragmentTransaction fragmentTransaction = fm.beginTransaction();

                FragGridGeneric f1 = new FragGridGeneric();
                f1.fragOptions = fragOptions;
                fragmentTransaction.replace(R.id.extended_info_fragment_frame, f1);
                fragmentTransaction.commit();
            }

        } else {
            Timber.d("..fragOptions are unpopulated, removing extended info nested fragment...");
            FragmentManager fm = getChildFragmentManager();
            FragmentTransaction fragmentTransaction = fm.beginTransaction();
            Fragment f = fm.findFragmentById(R.id.extended_info_fragment_frame);
            if (f != null) {
                fragmentTransaction.remove(f);
                fragmentTransaction.commit();
            }
        }
    }

    @SuppressWarnings("deprecation")
    private void configureButtons(View v) {
        Timber.d("configureButtons...");
        int btnCount;
        boolean grid = false;
        boolean longText = false;
        boolean leftAlignedText = false;
        //Get the Button Area

        options = fragStandardViewModel.getDisplay().getValue().getUiExtras().getParcelableArrayList(IUIDisplay.uiScreenOptionList);

        //Get the Display Elements References for this screen
        Button okButton = v.findViewById(R.id.ok_button);
        Button okButton2 = v.findViewById(R.id.ok_button2);

        if (options != null && options.size() > 0) {
            //If options are provided hide the standard OK Button
            okButton.setVisibility(Button.GONE);
            okButton2.setVisibility(Button.GONE);

            for (DisplayQuestion opt : options) {
                if (opt.getStyle() == BTN_STYLE_LEFT || opt.getStyle() == BTN_STYLE_PRIMARY_BORDER_LEFT || opt.getStyle() == BTN_STYLE_GREY_LEFT) {
                    grid = true;
                }
                if (opt.getTitle().length() > 14) {
                    longText = true;
                }
                if (opt.getStyle() == BTN_STYLE_DEFAULT_LEFT_ALIGNED_TEXT) {
                    leftAlignedText = true;
                }
            }

            LinearLayout btnArea = v.findViewById(R.id.button_area);
            GridLayout btnArea2 = v.findViewById(R.id.button_area2);

            if (grid) {
                Timber.d("...configuring for: grid...");
                btnArea2.removeAllViews();
                btnArea.setVisibility(View.GONE);
                btnArea2.setVisibility(View.VISIBLE);

                LinearLayout row = new LinearLayout(getBaseActivity());
                row.setLayoutParams(new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT));

                for (btnCount = 0; btnCount < options.size(); btnCount++) {

                    Button extraBtn = new Button(getBaseActivity());
                    DisplayQuestion option = options.get(btnCount);
                    extraBtn.setText(option.getTitle());
                    Timber.d("...configuring button: %s", option.getTitle());

                    extraBtn.setBackground(okButton2.getBackground());
                    extraBtn.setTag(CUSTOM_OFFSET + btnCount);
                    extraBtn.setLayoutParams(okButton2.getLayoutParams());

                    //The following call is deprecated but PAX is using the OLD SDK
                    if (longText) {
                        TextViewCompat.setTextAppearance( extraBtn, R.style.ui2ExtraSmallFont );
                    } else {
                        TextViewCompat.setTextAppearance(extraBtn, R.style.ButtonFontDark);
                    }

                    if (leftAlignedText) {
                        extraBtn.setGravity(Gravity.START);
                    }

                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT);

                    if (option.getStyle() == BTN_STYLE_RIGHT ||
                            option.getStyle() == BTN_STYLE_GREEN ||
                            option.getStyle() == BTN_STYLE_PRIMARY_DEFAULT ||
                            option.getStyle() == BTN_STYLE_PRIMARY_DEFAULT_RIGHT ||
                            option.getStyle() == BTN_STYLE_GREY_RIGHT) {
                        Timber.d("Setting Right Button");
                        lp.setMargins(
                                v.getResources().getInteger(R.integer.question_button_green_start),
                                v.getResources().getInteger(R.integer.question_button_green_top),
                                v.getResources().getInteger(R.integer.question_button_green_end),
                                v.getResources().getInteger(R.integer.question_button_green_bottom));
                        lp.weight = 1;
                        lp.gravity = Gravity.END;
                        row.setWeightSum(2);

                    } else if (option.getStyle() == BTN_STYLE_LEFT ||
                            option.getStyle() == BTN_STYLE_RED ||
                            option.getStyle() == BTN_STYLE_PRIMARY_BORDER ||
                            option.getStyle() == BTN_STYLE_PRIMARY_BORDER_LEFT ||
                            option.getStyle() == BTN_STYLE_GREY_LEFT) {
                        Timber.d("Setting LEFT Button");

                        lp.setMargins(
                                v.getResources().getInteger(R.integer.question_button_red_start),
                                v.getResources().getInteger(R.integer.question_button_red_top),
                                v.getResources().getInteger(R.integer.question_button_red_end),
                                v.getResources().getInteger(R.integer.question_button_red_bottom));
                        lp.weight = 1;
                        lp.gravity = Gravity.START;

                    } else {
                        Timber.d("Setting Button");

                        lp.weight = 1;
                    }
                    extraBtn.setLayoutParams(lp);


                    //Apply Custom Styling
                    if (option.getStyle() == BTN_STYLE_RED) {
                        GradientDrawable shapeDrawable = (GradientDrawable) extraBtn.getBackground().getConstantState().newDrawable().mutate();
                        shapeDrawable.setColor(ContextCompat.getColor(getContext(), R.color.colorRed));
                        extraBtn.setBackground(shapeDrawable);
                        TextViewCompat.setTextAppearance(extraBtn, longText ? R.style.ui2SmallFontLight : R.style.ui2MediumFontLight);
                        row.addView(extraBtn);
                    } else if (option.getStyle() == BTN_STYLE_GREEN) {
                        GradientDrawable shapeDrawable = (GradientDrawable) extraBtn.getBackground().getConstantState().newDrawable().mutate();
                        shapeDrawable.setColor(ContextCompat.getColor(getContext(), R.color.colorGreen));
                        extraBtn.setBackground(shapeDrawable);
                        TextViewCompat.setTextAppearance(extraBtn, longText ? R.style.ui2SmallFontLight : R.style.ui2MediumFontLight);
                        row.addView(extraBtn);
                    } else if (option.getStyle() == BTN_STYLE_RED_DOUBLE) {
                        GradientDrawable shapeDrawable = (GradientDrawable) extraBtn.getBackground().getConstantState().newDrawable().mutate();
                        shapeDrawable.setColor(ContextCompat.getColor(getContext(), R.color.colorRed));
                        TextViewCompat.setTextAppearance(extraBtn, longText ? R.style.ui2SmallFontLight : R.style.ButtonFontLight);
                        extraBtn.setBackground(shapeDrawable);

                        LinearLayout.LayoutParams linearLayoutParams =
                                new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT);
                        linearLayoutParams.setMargins(10, 0, 10, 0);
                        extraBtn.setLayoutParams(linearLayoutParams);

                        row.addView(extraBtn);

                    } else if (option.getStyle() == BTN_STYLE_DEFAULT_DOUBLE) {
                        GradientDrawable shapeDrawable = (GradientDrawable) extraBtn.getBackground().getConstantState().newDrawable().mutate();
                        shapeDrawable.setColor(ContextCompat.getColor(getContext(), R.color.ui2MenuButton));
                        TextViewCompat.setTextAppearance(extraBtn, longText ? R.style.ui2ExtraSmallFont : R.style.ButtonFontDark);
                        extraBtn.setBackground(shapeDrawable);

                        LinearLayout.LayoutParams linearLayoutParams =
                                new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT);
                        linearLayoutParams.setMargins(10, 0, 10, 10);
                        extraBtn.setLayoutParams(linearLayoutParams);

                        row.addView(extraBtn);

                    }  else if (option.getStyle() == BTN_STYLE_TRANSPARENT) {
                        TextViewCompat.setTextAppearance( extraBtn, longText ? R.style.uiCancelBtnSmallFont : R.style.uiCancelBtn);
                        extraBtn.setBackgroundResource(R.drawable.ui2_buttonstyle_cancel);
                        UIUtilities.borderTransparentButton(getActivity(),extraBtn);
                        row.addView(extraBtn);
                        extraBtn.setAllCaps(false);
                    } else if (option.getStyle() == BTN_STYLE_TRANSPARENT_DOUBLE) {
                        TextViewCompat.setTextAppearance(extraBtn, longText ? R.style.uiCancelBtnSmallFont : R.style.uiCancelBtn);
                        extraBtn.setBackgroundResource(R.drawable.ui2_buttonstyle_cancel);
                        extraBtn.setAllCaps(false);
                        UIUtilities.borderTransparentButton(getActivity(),extraBtn);
                        LinearLayout.LayoutParams linearLayoutParams =
                                new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT);
                        linearLayoutParams.setMargins(10, 0, 10, 0);
                        extraBtn.setLayoutParams(linearLayoutParams);

                        row.addView(extraBtn);

                    } else if (option.getStyle() == BTN_STYLE_PRIMARY_DEFAULT || option.getStyle() == BTN_STYLE_PRIMARY_DEFAULT_RIGHT) {
                        GradientDrawable shapeDrawable = (GradientDrawable) extraBtn.getBackground().getConstantState().newDrawable().mutate();
                        shapeDrawable.setColor(Engine.getDep().getPayCfg().getBrandDisplayButtonColourOrDefault(getActivity().getColor(R.color.color_linkly_primary)));
                        extraBtn.setTextColor(Engine.getDep().getPayCfg().getBrandDisplayButtonTextColourOrDefault());
                        extraBtn.setBackground(shapeDrawable);
                        extraBtn.setAllCaps(false);
                        TextViewCompat.setTextAppearance(extraBtn, longText ? R.style.uiDoneBtnSmallFont : R.style.uiDoneBtn);
                        row.addView(extraBtn);
                    } else if (option.getStyle() == BTN_STYLE_PRIMARY_DEFAULT_DOUBLE) {
                        GradientDrawable shapeDrawable = (GradientDrawable) extraBtn.getBackground().getConstantState().newDrawable().mutate();
                        shapeDrawable.setColor(Engine.getDep().getPayCfg().getBrandDisplayButtonColourOrDefault(getActivity().getColor(R.color.color_linkly_primary)));
                        extraBtn.setTextColor(Engine.getDep().getPayCfg().getBrandDisplayButtonTextColourOrDefault());
                        extraBtn.setBackground(shapeDrawable);
                        TextViewCompat.setTextAppearance(extraBtn, longText ? R.style.uiDoneBtnSmallFont : R.style.uiDoneBtn);
                        extraBtn.setAllCaps(false);
                        LinearLayout.LayoutParams linearLayoutParams =
                                new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT);
                        linearLayoutParams.setMargins(10, 0, 10, 0);
                        extraBtn.setLayoutParams(linearLayoutParams);

                        row.addView(extraBtn);
                    } else if (option.getStyle() == BTN_STYLE_PRIMARY_BORDER || option.getStyle() == BTN_STYLE_PRIMARY_BORDER_LEFT) {
                        GradientDrawable shapeDrawable = (GradientDrawable) extraBtn.getBackground().getConstantState().newDrawable().mutate();
                        shapeDrawable.setStroke(2, Engine.getDep().getPayCfg().getBrandDisplayButtonColourOrDefault(getActivity().getColor(R.color.color_linkly_primary)));
                        shapeDrawable.setColor(ContextCompat.getColor(requireContext(), R.color.colorWhite));
                        int btnColor = shapeDrawable.getColor().getDefaultColor();
                        int textColor = Engine.getDep().getPayCfg().getBrandDisplayButtonTextColourOrDefault();
                        if(btnColor == Color.WHITE && textColor == Color.WHITE) {
                            extraBtn.setTextColor(Engine.getDep().getPayCfg().getBrandDisplayButtonColourOrDefault(getActivity().getColor(R.color.color_linkly_primary)));
                        } else {
                            extraBtn.setTextColor(textColor);
                        }
                        extraBtn.setAllCaps(false);
                        extraBtn.setBackground(shapeDrawable);
                        TextViewCompat.setTextAppearance(extraBtn, longText ? R.style.uiBorderBtnSmallFont : R.style.uiBorderBtn);
                        row.addView(extraBtn);

                    } else if (option.getStyle() == BTN_STYLE_PRIMARY_BORDER_DOUBLE) {
                        GradientDrawable shapeDrawable = (GradientDrawable) extraBtn.getBackground().getConstantState().newDrawable().mutate();
                        shapeDrawable.setStroke(2, Engine.getDep().getPayCfg().getBrandDisplayButtonColourOrDefault(getActivity().getColor(R.color.color_linkly_primary)));
                        shapeDrawable.setColor(ContextCompat.getColor(requireContext(), R.color.colorWhite));
                        extraBtn.setBackground(shapeDrawable);
                        int btnColor = shapeDrawable.getColor().getDefaultColor();
                        int textColor = Engine.getDep().getPayCfg().getBrandDisplayButtonTextColourOrDefault();
                        if(btnColor == Color.WHITE && textColor == Color.WHITE) {
                            extraBtn.setTextColor(Engine.getDep().getPayCfg().getBrandDisplayButtonColourOrDefault(getActivity().getColor(R.color.color_linkly_primary)));
                        } else {
                            extraBtn.setTextColor(textColor);
                        }
                        TextViewCompat.setTextAppearance(extraBtn, longText ? R.style.uiBorderBtnSmallFont : R.style.uiBorderBtn);
                        LinearLayout.LayoutParams linearLayoutParams =
                                new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT);
                        linearLayoutParams.setMargins(10, 0, 10, 0);
                        extraBtn.setLayoutParams(linearLayoutParams);
                        row.addView(extraBtn);

                    } else if (option.getStyle() == BTN_STYLE_GREY_LEFT || option.getStyle() == BTN_STYLE_GREY_RIGHT) {
                        extraBtn.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.ui2_buttonstyle_tip, null));
                        extraBtn.setAllCaps(false);
                        TextViewCompat.setTextAppearance(extraBtn, longText ? R.style.uiTipBtnSmallFont : R.style.uiTipBtn);
                        row.addView(extraBtn);
                    } else {
                        row.addView(extraBtn);
                    }


                    extraBtn.setOnClickListener(this);

                    //Add Additional Buttons
                    if (option.getStyle() == BTN_STYLE_RIGHT ||
                            option.getStyle() == BTN_STYLE_GREEN ||
                            option.getStyle() == BTN_STYLE_DEFAULT_DOUBLE ||
                            option.getStyle() == BTN_STYLE_RED_DOUBLE ||
                            option.getStyle() == BTN_STYLE_TRANSPARENT_DOUBLE ||
                            option.getStyle() == BTN_STYLE_PRIMARY_DEFAULT_DOUBLE ||
                            option.getStyle() == BTN_STYLE_PRIMARY_DEFAULT ||
                            option.getStyle() == BTN_STYLE_PRIMARY_DEFAULT_RIGHT ||
                            option.getStyle() == BTN_STYLE_GREY_RIGHT) {
                        btnArea2.addView(row);
                        row = new LinearLayout(getBaseActivity());
                        row.setLayoutParams(new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT));
                    }
                }

            } else {
                Timber.d("...configuring for: not grid...");
                for (btnCount = 0; btnCount < options.size(); btnCount++) {
                    Button extraBtn = new Button(getBaseActivity());
                    DisplayQuestion option = options.get(btnCount);

                    extraBtn.setText(option.getTitle());
                    Timber.d("...configuring button: %s", option.getTitle());
                    extraBtn.setLayoutParams(okButton.getLayoutParams());
                    extraBtn.setBackground(okButton.getBackground());
                    extraBtn.setTag(CUSTOM_OFFSET + btnCount);
                    //The following call is deprecated but PAX is using the OLD SDK
                    TextViewCompat.setTextAppearance( extraBtn, longText ? R.style.ui2SmallFont : R.style.ui2MediumFont);

                    if (leftAlignedText) {
                        extraBtn.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
                        String text = "  " + option.getTitle();
                        extraBtn.setText( text );
                    }

                    final float scale = getResources().getDisplayMetrics().density;

                    //Apply Custom Styling
                    if (option.getStyle() == DisplayQuestion.EButtonStyle.BTN_STYLE_RED) {
                        GradientDrawable shapeDrawable = (GradientDrawable) extraBtn.getBackground().getConstantState().newDrawable().mutate();
                        shapeDrawable.setColor(ContextCompat.getColor( getContext(), R.color.colorRed));
                        extraBtn.setBackground(shapeDrawable);
                        TextViewCompat.setTextAppearance( extraBtn, longText ? R.style.ui2SmallFontLight : R.style.ui2MediumFontLight);
                    } else if (option.getStyle() == BTN_STYLE_RED_DOUBLE) {
                        GradientDrawable shapeDrawable = (GradientDrawable) extraBtn.getBackground().getConstantState().newDrawable().mutate();
                        shapeDrawable.setColor(ContextCompat.getColor( getContext(), R.color.colorRed));
                        extraBtn.setBackground(shapeDrawable);
                        TextViewCompat.setTextAppearance( extraBtn, longText ? R.style.ui2SmallFontLight : R.style.ButtonFontLight);
                    } else if (option.getStyle() == DisplayQuestion.EButtonStyle.BTN_STYLE_GREEN) {
                        GradientDrawable shapeDrawable = (GradientDrawable) extraBtn.getBackground().getConstantState().newDrawable().mutate();
                        shapeDrawable.setColor(ContextCompat.getColor(getContext(), R.color.colorGreen));
                        extraBtn.setBackground(shapeDrawable);
                        TextViewCompat.setTextAppearance(extraBtn, longText ? R.style.ui2SmallFontLight : R.style.ui2MediumFontLight);
                    } else if (option.getStyle() == BTN_STYLE_LEFT) {
                        extraBtn.setWidth((int) (50 * scale));
                        extraBtn.setLeft(0);
                    } else if (option.getStyle() == BTN_STYLE_RIGHT) {
                        extraBtn.setWidth((int) (100 * scale));
                        extraBtn.setLeft((int) (101 * scale));
                    } else if (option.getStyle() == BTN_STYLE_TRANSPARENT || option.getStyle() == BTN_STYLE_TRANSPARENT_DOUBLE) {
                        extraBtn.setBackgroundResource(R.drawable.ui2_buttonstyle_cancel);
                        TextViewCompat.setTextAppearance(extraBtn, longText ? R.style.uiCancelBtnSmallFont : R.style.uiCancelBtn);
                        extraBtn.setAllCaps(false);
                        UIUtilities.borderTransparentButton(getActivity(),extraBtn);
                    } else if (option.getStyle() == BTN_STYLE_PRIMARY_DEFAULT || option.getStyle() == BTN_STYLE_PRIMARY_DEFAULT_DOUBLE) {
                        GradientDrawable shapeDrawable = (GradientDrawable) extraBtn.getBackground().getConstantState().newDrawable().mutate();
                        shapeDrawable.setColor(Engine.getDep().getPayCfg().getBrandDisplayButtonColourOrDefault(getActivity().getColor(R.color.color_linkly_primary)));
                        extraBtn.setTextColor(Engine.getDep().getPayCfg().getBrandDisplayButtonTextColourOrDefault());
                        extraBtn.setBackground(shapeDrawable);
                        TextViewCompat.setTextAppearance(extraBtn, longText ? R.style.uiDoneBtnSmallFont : R.style.uiDoneBtn);
                        extraBtn.setAllCaps(false);
                    } else if (option.getStyle() == BTN_STYLE_PRIMARY_BORDER || option.getStyle() == BTN_STYLE_PRIMARY_BORDER_DOUBLE) {
                        GradientDrawable shapeDrawable = (GradientDrawable) extraBtn.getBackground().getConstantState().newDrawable().mutate();
                        shapeDrawable.setStroke(2, Engine.getDep().getPayCfg().getBrandDisplayButtonColourOrDefault(getActivity().getColor(R.color.color_linkly_primary)));
                        shapeDrawable.setColor(ContextCompat.getColor(requireContext(), R.color.colorWhite));
                        TextViewCompat.setTextAppearance(extraBtn, longText ? R.style.uiBorderBtnSmallFont : R.style.uiBorderBtn);
                        extraBtn.setBackground(shapeDrawable);
                        int btnColor = shapeDrawable.getColor().getDefaultColor();
                        int textColor = Engine.getDep().getPayCfg().getBrandDisplayButtonTextColourOrDefault();
                        if(btnColor == Color.WHITE && textColor == Color.WHITE) {
                            extraBtn.setTextColor(Engine.getDep().getPayCfg().getBrandDisplayButtonColourOrDefault(getActivity().getColor(R.color.color_linkly_primary)));
                        } else {
                            extraBtn.setTextColor(textColor);
                        }
                        extraBtn.setAllCaps(false);
                    } else if (option.getStyle() == BTN_STYLE_PRIMARY_BORDER_LEFT) {
                        GradientDrawable shapeDrawable = (GradientDrawable) extraBtn.getBackground().getConstantState().newDrawable().mutate();
                        shapeDrawable.setStroke(2, Engine.getDep().getPayCfg().getBrandDisplayButtonColourOrDefault(getActivity().getColor(R.color.color_linkly_primary)));
                        shapeDrawable.setColor(ContextCompat.getColor(requireContext(), R.color.colorWhite));
                        extraBtn.setBackground(shapeDrawable);
                        int btnColor = shapeDrawable.getColor().getDefaultColor();
                        int textColor = Engine.getDep().getPayCfg().getBrandDisplayButtonTextColourOrDefault();
                        if(btnColor == Color.WHITE && textColor == Color.WHITE) {
                            extraBtn.setTextColor(Engine.getDep().getPayCfg().getBrandDisplayButtonColourOrDefault(getActivity().getColor(R.color.color_linkly_primary)));
                        } else {
                            extraBtn.setTextColor(textColor);
                        }
                        TextViewCompat.setTextAppearance(extraBtn, longText ? R.style.uiBorderBtnSmallFont : R.style.uiBorderBtn);
                        extraBtn.setAllCaps(false);
                        extraBtn.setWidth((int) (50 * scale));
                        extraBtn.setLeft(0);
                    } else if (option.getStyle() == BTN_STYLE_PRIMARY_DEFAULT_RIGHT) {
                        GradientDrawable shapeDrawable = (GradientDrawable) extraBtn.getBackground().getConstantState().newDrawable().mutate();
                        shapeDrawable.setColor(Engine.getDep().getPayCfg().getBrandDisplayButtonColourOrDefault(getActivity().getColor(R.color.color_linkly_primary)));
                        extraBtn.setTextColor(Engine.getDep().getPayCfg().getBrandDisplayButtonTextColourOrDefault());
                        TextViewCompat.setTextAppearance(extraBtn, longText ? R.style.uiDoneBtnSmallFont : R.style.uiDoneBtn);
                        extraBtn.setBackground(shapeDrawable);
                        extraBtn.setAllCaps(false);
                        extraBtn.setWidth((int) (100 * scale));
                        extraBtn.setLeft((int) (101 * scale));
                    } else if (option.getStyle() == BTN_STYLE_GREY_LEFT) {
                        extraBtn.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.ui2_buttonstyle_tip, null));
                        extraBtn.setAllCaps(false);
                        TextViewCompat.setTextAppearance(extraBtn, longText ? R.style.uiTipBtnSmallFont : R.style.uiTipBtn);
                        extraBtn.setWidth((int) (50 * scale));
                        extraBtn.setLeft(0);
                    } else if (option.getStyle() == BTN_STYLE_GREY_RIGHT) {
                        extraBtn.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.ui2_buttonstyle_tip, null));
                        extraBtn.setAllCaps(false);
                        TextViewCompat.setTextAppearance(extraBtn, longText ? R.style.uiTipBtnSmallFont : R.style.uiTipBtn);
                        extraBtn.setWidth((int) (100 * scale));
                        extraBtn.setLeft((int) (101 * scale));
                    }
                    extraBtn.setOnClickListener(this);

                    //Add Additional Buttons
                    btnArea.addView(extraBtn);
                }
            }
        } else {
            okButton.setTag(IUIDisplay.UIResultCode.OK);
        }
    }

    private void setScreenText(View v) {
        //Set the title Text
        TextView txtTitle = v.findViewById(R.id.header_title);
        String title = fragStandardViewModel.getTitle().getValue();

        String overrideTitle = fragStandardViewModel.getDisplay().getValue().getUiExtras().getString(IUIDisplay.uiScreenTitle);

        if (!Util.isNullOrEmpty(overrideTitle))
            title = overrideTitle;

        if (title != null && title.length() > 0) {
            txtTitle.setText(title);
            txtTitle.setVisibility(TextView.VISIBLE);
        } else {
            txtTitle.setVisibility(TextView.INVISIBLE);
        }

        //Set question Text
        TextView txtQuestion = v.findViewById(R.id.Question);
        String question = fragStandardViewModel.getPrompt().getValue();
        Timber.d("FragQuestion Question - %s, Title - %s Override Title - %s", question, title, overrideTitle);
        // prevent any input and clicks
        txtQuestion.setClickable(false);

        if (question != null && question.length() > 0) {

            txtQuestion.setText(question);
            txtQuestion.setVisibility(TextView.VISIBLE);
        } else {
            txtQuestion.setVisibility(TextView.GONE);
        }
    }

    DisplayKiosk.NavigationBarState state;

    @Override
    public void onResume() {
        super.onResume();
        state = new DisplayKiosk.NavigationBarState();
        if (fragStandardViewModel.getDisplay().getValue().getUiExtras().getBoolean(IUIDisplay.uiEnableBackButton)) {
            DisplayKiosk.getInstance().onResume(true);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        DisplayKiosk.getInstance().setNavigationBarAndButtonsState(state, true);
    }

    protected void initViews(View v) {

        configureFragments();

        configureButtons(v);

        SetHeader(false, false);

        setScreenText(v);
    }

    public void onClick(View v) {
        int tag = (int) v.getTag();

        if (tag >= CUSTOM_OFFSET) {
            DisplayQuestion option = options.get(tag - CUSTOM_OFFSET);
            sendResponse(IUIDisplay.UIResultCode.OK, option.getResponse(), "");
        } else {
            sendResponse(IUIDisplay.UIResultCode.OK, "OK", "");
        }
    }

}





