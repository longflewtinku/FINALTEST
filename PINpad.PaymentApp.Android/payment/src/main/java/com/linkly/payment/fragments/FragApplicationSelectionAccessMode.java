package com.linkly.payment.fragments;

import static com.linkly.libui.IUIDisplay.ACTIVITY_ID.ACT_ACCESS_APP_SELECTION;

import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.linkly.libui.IUIDisplay;
import com.linkly.libui.display.DisplayQuestion;
import com.linkly.libui.display.DisplayRequest;
import com.linkly.libui.speech.SpeechUtils;
import com.linkly.payment.R;
import com.linkly.payment.databinding.ActivityTransBinding;
import com.linkly.payment.viewmodel.FragStandardViewModel;
import com.pax.neptunelite.api.NeptuneLiteUser;

import java.util.ArrayList;

import timber.log.Timber;

public class FragApplicationSelectionAccessMode extends BaseFragment<ActivityTransBinding,
        FragStandardViewModel> {
    public static final String TAG = FragApplicationSelectionAccessMode.class.getSimpleName();

    private FragStandardViewModel fragStandardViewModel;
    private LinearLayout linearLayout;

    public static FragApplicationSelectionAccessMode newInstance() {
        Bundle args = new Bundle();
        FragApplicationSelectionAccessMode fragment = new FragApplicationSelectionAccessMode();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public int getBindingVariable() {
        return com.linkly.payment.BR.fragStandardViewModel;
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_access_mode_application_selection;
    }

    @Override
    public FragStandardViewModel getViewModel() {
        fragStandardViewModel = new ViewModelProvider(this).get(FragStandardViewModel.class);
        fragStandardViewModel.init(ACT_ACCESS_APP_SELECTION);
        return fragStandardViewModel;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            NeptuneLiteUser.getInstance().getDal(getBaseActivity()).getSys().showStatusBar(false);
        } catch (Exception e) {
            Timber.w(e);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        if (view != null) {
            linearLayout = view.findViewById(R.id.application_selection_access_mode);
            initViews();
        }
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        View overlayView = view.findViewById(R.id.application_selection_access_mode_overlay);
        overlayView.setOnTouchListener(overlayTouchListener());
        SpeechUtils.getInstance().speak(getResources().getString(R.string.ACCESS_MODE_ACCOUNT_SELECT_INSRUCTIONS));
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
    private void initViews() {
        DisplayRequest displayRequest = fragStandardViewModel.getDisplay().getValue();
        if (displayRequest != null) {
            ArrayList<DisplayQuestion> displayOptions = displayRequest.getUiExtras().getParcelableArrayList(IUIDisplay.uiScreenOptionList);
            if (displayOptions != null) {
                int idCounter = 10;
                for (DisplayQuestion option : displayOptions) {
                    Button button = new Button(getContext());
                    button.setId(idCounter);
                    button.setText(option.getTitle());
                    button.setTextAppearance(R.style.ui2HeaderFont);
                    button.setBackground(getResources().getDrawable(R.drawable.ui2_buttonstyle, getContext().getTheme()));
                    button.setTextColor(getResources().getColor(R.color.colorWhite, getActivity().getTheme()));
                    button.setEnabled(false);
                    button.getBackground().setColorFilter(getResources().getColor(R.color.colorBlack, getActivity().getTheme()), PorterDuff.Mode.SRC_ATOP);
                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0);
                    layoutParams.weight = 1;
                    layoutParams.setMargins(4, 4, 4, 4);
                    button.setLayoutParams(layoutParams);
                    linearLayout.addView(button);
                    idCounter++;
                }
            }
        }
    }

    private String accessModeSelectedAccount = null;
    private int prevButtonId = 0;

    private void selectedAccount(float y) {
        final int childCount = linearLayout.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View childView = linearLayout.getChildAt(i);
            int childViewMaxY = childView.getHeight() + childView.getTop();
            if (childView.getTop() < y && childViewMaxY >= y) {
                if (childView.getId() != prevButtonId) {
                    prevButtonId = childView.getId();
                    accessModeButtonAction(i);
                }
                break;
            }
        }
    }

    @SuppressWarnings("deprecation")
    private void accessModeButtonAction(int index) {
        DisplayRequest displayRequest = fragStandardViewModel.getDisplay().getValue();
        if (displayRequest != null) {
            ArrayList<DisplayQuestion> displayOptions = displayRequest.getUiExtras().getParcelableArrayList(IUIDisplay.uiScreenOptionList);
            if (displayOptions != null) {
                DisplayQuestion option = displayOptions.get(index);
                String wordsToSpeak = processTitle(option.getTitle()) + getResources().getString(R.string.ACCESS_MODE_DOUBLE_TAP);
                accessModeSelectedAccount = option.getResponse();
                SpeechUtils.getInstance().stop();
                SpeechUtils.getInstance().speak(wordsToSpeak);
            }
        }
    }

    private String processTitle(String title) {
        String transformedTitle = title;
        if (title.contains("CHQ")) {
            transformedTitle = title.replace("CHQ", getResources().getString(R.string.CHEQUE));
        } else if (title.contains("CRDT")) {
            transformedTitle = title.replace("CRDT", getResources().getString(R.string.CREDIT));
        } else if (title.contains("SAV")) {
            transformedTitle = title.replace("SAV",  getResources().getString(R.string.SAVINGS));
        }
        return transformedTitle;
    }

    private View.OnTouchListener overlayTouchListener() {

        return new View.OnTouchListener() {
            GestureDetector gestureDetector = new GestureDetector(FragApplicationSelectionAccessMode.this.getContext(), new GestureDetector.SimpleOnGestureListener() {
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
                    selectedAccount(e.getY());
                    return super.onSingleTapConfirmed(e);
                }

                @Override
                public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                    selectedAccount(e2.getY());
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

}
