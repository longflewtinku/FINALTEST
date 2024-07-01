package com.linkly.libui.keyboard;

import static android.view.KeyEvent.KEYCODE_ENTER;

import android.app.Activity;
import android.content.res.Configuration;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.text.Editable;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.inputmethod.BaseInputConnection;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.linkly.libui.IUIKeyboard;
import com.linkly.libui.R;
import com.linkly.libui.views.CustomEditText;

import java.lang.ref.WeakReference;
import java.util.HashMap;

import timber.log.Timber;


// SRC http://www.fampennings.nl/maarten/android/09keyboard/index.htm

@SuppressWarnings("deprecation")
public class CustomKeyboard {
    public static final int KEYCODE_UNCAPS = -10;
    public static final int KEYCODE_CAPS = -200;
    public static final int KEYCODE_DOUBLE_ZERO = -100;
    public static final int KEYCODE_SET_KB_NUMERIC = -300;
    public static final int KEYCODE_TRIPLE_ZERO = -1000;
    public static final int KEYCODE_DUMMY = -9999;

    private BaseInputConnection mInputConnection;
    private Keyboard keyboard;
    private KeyboardView kv;
    private WeakReference<Activity> mHostActivity;
    private KBTypes keyboardTypeSet;
    private boolean caps = true;
    private boolean symbols = true;
    private HashMap<Integer, KBTypes> keyTypesMap = new HashMap<>();

    /**
     * The key (code) handler.
     */
    private KeyboardView.OnKeyboardActionListener mOnKeyboardActionListener = new KeyboardView.OnKeyboardActionListener() {

        @Override
        public void onKey(int primaryCode, int[] keyCodes) {

            View focusCurrent = mHostActivity.get().getWindow().getCurrentFocus();

            CustomEditText edittext = (CustomEditText) focusCurrent;
            Editable editable = edittext.getText();
            int start = edittext.getSelectionStart();


            switch (primaryCode) {
                case Keyboard.KEYCODE_DELETE:
                    if (editable != null && start > 0) {
                        editable.delete(start - 1, start);
                    }
                    break;
                case Keyboard.KEYCODE_SHIFT:
                    if (keyboardTypeSet != KBTypes.TEXT_KB_CAPS) {
                        caps = !caps;
                        if (!symbols)
                            keyboard = new Keyboard(mHostActivity.get(), R.xml.qwertycapsnosym);
                        else
                            keyboard = new Keyboard(mHostActivity.get(), R.xml.qwertycaps);

                        keyboard.setShifted(caps);
                        kv.setKeyboard(keyboard);
                        if (kv != null) {
                            kv.invalidateAllKeys();
                        }
                    }
                    break;
                case KEYCODE_UNCAPS:
                    if (keyboardTypeSet != KBTypes.TEXT_KB_CAPS) {
                        caps = !caps;
                        if (!symbols)
                            keyboard = new Keyboard(mHostActivity.get(), R.xml.qwertynosym);
                        else
                            keyboard = new Keyboard(mHostActivity.get(), R.xml.qwerty);
                        keyboard.setShifted(caps);
                        kv.setKeyboard(keyboard);
                        if (kv != null) {
                            kv.invalidateAllKeys();
                        }
                    }
                    break;
                case Keyboard.KEYCODE_DONE:
                    if (keyboardTypeSet == KBTypes.IP_PORT_KB) {
                        mInputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER));
                        if (kv != null) {
                            kv.setVisibility(View.GONE);
                        }
                    }

                    break;
                case android.inputmethodservice.Keyboard.KEYCODE_CANCEL:
                    if (kv != null) {
                        kv.setVisibility(View.GONE);
                    }
                    mInputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, android.inputmethodservice.Keyboard.KEYCODE_CANCEL));
                    break;
                case KEYCODE_DOUBLE_ZERO:
                    editable.insert(start, "00");

                    break;
                case KEYCODE_CAPS:
                    caps = true;
                    if (!symbols)
                        keyboard = new Keyboard(mHostActivity.get(), R.xml.qwertycapsnosym);
                    else
                        keyboard = new Keyboard(mHostActivity.get(), R.xml.qwertycaps);
                    keyboard.setShifted(true);
                    kv.setKeyboard(keyboard);

                    break;
                case KEYCODE_SET_KB_NUMERIC:
                    keyboard = new Keyboard(mHostActivity.get(), R.xml.usrpas);
                    kv.setKeyboard(keyboard);
                    break;
                case KEYCODE_TRIPLE_ZERO:
                    editable.insert(start, "000");

                    break;
                case KEYCODE_DUMMY:
                    break;
                default:
                    char code = (char) primaryCode;
                    if (Character.isLetter(code) && caps) {
                        code = Character.toUpperCase(code);
                    }
                    editable.insert(start, String.valueOf(code));
                    break;
            }
        }

        @Override
        public void onPress(int primaryCode) {
            switch (primaryCode) {

                case Keyboard.KEYCODE_DONE:
                    mInputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER));
                    break;
                case android.inputmethodservice.Keyboard.KEYCODE_CANCEL:
                    mInputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, android.inputmethodservice.Keyboard.KEYCODE_CANCEL));
                    break;
                default:

                    break;

            }
        }

        @Override
        public void onRelease(int primaryCode) {
            switch (primaryCode) {

                case Keyboard.KEYCODE_DONE:
                    mInputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER));
                    break;
                case android.inputmethodservice.Keyboard.KEYCODE_CANCEL:
                    mInputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, android.inputmethodservice.Keyboard.KEYCODE_CANCEL));
                    break;
                default:

                    break;

            }
        }

        @Override
        public void onText(CharSequence text) {
        }

        @Override
        public void swipeDown() {
        }

        @Override
        public void swipeLeft() {
        }

        @Override
        public void swipeRight() {
        }

        @Override
        public void swipeUp() {
        }

    };

    /**
     * Create a custom keyboard, that uses the KeyboardView (with resource id <var>viewid</var>) of the <var>host</var> activity,
     * and load the keyboard layout from xml file <var>layoutid</var> (see {@link Keyboard} for description).
     * Note that the <var>host</var> activity must have a <var>KeyboardView</var> in its layout (typically aligned with the bottom of the activity).
     * Note that the keyboard layout xml file may include key codes for navigation; see the constants in this class for their values.
     *
     * @param host The hosting activity.
     */
    public CustomKeyboard(Activity host, boolean useSecondUI) {
        mHostActivity = new WeakReference<Activity>(host);

        mHostActivity.get().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    /**
     * Returns whether the CustomKeyboard is visible.
     */
    public boolean isCustomKeyboardVisible() {
        return kv.getVisibility() == View.VISIBLE;
    }

    /**
     * Make the CustomKeyboard visible, and hide the system keyboard for view v.
     */
    public void showCustomKeyboard(View v, View editTextView) {
        int visible = View.VISIBLE;

        Timber.i("get:" + editTextView.getId());

        KBTypes kbType = keyTypesMap.get(editTextView.getId());
        if (kbType != null) {
            setKeyboardType(v, kbType);
        }

        if( Configuration.ORIENTATION_LANDSCAPE == this.mHostActivity.get().getResources().getConfiguration().orientation &&
                this.keyboardTypeSet.isNumericInputOnly() ){
            Timber.i("hiding keyboard" );
            visible = View.GONE;
        }

        if (kv != null) {
            kv.setVisibility(visible);
            kv.setEnabled(true);
        }
        if (editTextView != null) {
            ((InputMethodManager) mHostActivity.get().getSystemService(Activity.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(v.getWindowToken(), 0);
        }
    }

    /**
     * Make the CustomKeyboard invisible.
     */
    public void hideCustomKeyboard() {
        if (kv != null) {
            kv.setVisibility(View.GONE);
            kv.setEnabled(false);
        }
    }

    public void setKeyboardType(View view, KBTypes kbType) {

        if (kv == null) {
            UI2PosKeyboardView v = (UI2PosKeyboardView) view.findViewById(R.id.ui2keyboardview);
            if (v == null)
                return;
            v.UpdatePadding(kbType);
            kv = (KeyboardView) v;
        }

        if (kv == null) {
            return;
        }

        symbols = true;
        Timber.i("Keyboard Type: " + kbType.toString());
        if (kbType == KBTypes.AMOUNT_KB) {
            keyboard = new Keyboard(mHostActivity.get(), R.xml.amount);
            keyboardTypeSet = KBTypes.AMOUNT_KB;
        } else if (kbType == KBTypes.USR_PASS_KB) {
            keyboard = new Keyboard(mHostActivity.get(), R.xml.usrpas);
            keyboardTypeSet = KBTypes.AMOUNT_KB;
        } else if (kbType == KBTypes.NUM_ALPHA) {
            keyboard = new Keyboard(mHostActivity.get(), R.xml.usrpas);
            keyboardTypeSet = KBTypes.AMOUNT_KB;
        } else if (kbType == KBTypes.NUM_ALPHA_NO_SYM) {
            keyboard = new Keyboard(mHostActivity.get(), R.xml.usrpas);
            keyboardTypeSet = KBTypes.AMOUNT_KB;
            symbols = false;
        } else if (kbType == KBTypes.TEXT_KB) {
            keyboard = new Keyboard(mHostActivity.get(), R.xml.qwerty);
            keyboardTypeSet = KBTypes.TEXT_KB;
            caps = false;
            keyboard.setShifted(caps);
        } else if (kbType == KBTypes.TEXT_KB_CAPS_NO_SYM) {
            keyboard = new Keyboard(mHostActivity.get(), R.xml.qwertycapsnosym);
            keyboardTypeSet = KBTypes.TEXT_KB_CAPS;
            caps = true;
            symbols = false;
            keyboard.setShifted(caps);

        } else if (kbType == KBTypes.TEXT_KB_CAPS) {
            keyboard = new Keyboard(mHostActivity.get(), R.xml.qwertycaps);
            keyboardTypeSet = KBTypes.TEXT_KB_CAPS;
            caps = true;
            keyboard.setShifted(caps);
        } else if (kbType == KBTypes.TEXT_KB_CAPS_NO_SYM) {
            keyboard = new Keyboard(mHostActivity.get(), R.xml.qwertycapsnosym);
            keyboardTypeSet = KBTypes.TEXT_KB_CAPS;
            caps = true;
            symbols = false;
            keyboard.setShifted(caps);
        } else if (kbType == KBTypes.PHONE_KB) {
            keyboard = new Keyboard(mHostActivity.get(), R.xml.phone);
            keyboardTypeSet = KBTypes.PHONE_KB;
        } else {
            keyboard = new Keyboard(mHostActivity.get(), R.xml.ip_port);
            keyboardTypeSet = KBTypes.IP_PORT_KB;
        }

        kv.setOnKeyboardActionListener(mOnKeyboardActionListener);
        kv.setPreviewEnabled(false);
        kv.setKeyboard(keyboard);
    }

    public void unregisterEditText() {
        mInputConnection = null;
    }
    /**
     * Register <var>EditText<var> with resource id <var>resid</var> (on the hosting activity) for using this custom keyboard.
     */
    public void registerEditText(CustomEditText edittext, final IUIKeyboard.OnDoneClickedListener doneListener, final IUIKeyboard.OnCancelClickedListener cancelListener, KBTypes kbType) {

        if (keyTypesMap.containsKey(edittext.getId())) {
            keyTypesMap.remove(edittext.getId());
            Timber.i("remove:" + edittext.getId());
        }

        keyTypesMap.put(edittext.getId(), kbType);
        Timber.i("put:" + edittext.getId() + ":" + kbType.toString());

        mInputConnection = new BaseInputConnection(edittext, true);

        // Make the custom keyboard appear
        edittext.setOnFocusChangeListener(new OnFocusChangeListener() {
            // NOTE By setting the on focus listener, we can show the custom keyboard when the edit box gets focus, but also hide it when the edit box loses focus
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    showCustomKeyboard(mHostActivity.get().findViewById(R.id.ui2keyboardview), v);
                } else {
                    hideCustomKeyboard();
                }
            }
        });

        edittext.setOnClickListener(new OnClickListener() {
            // NOTE By setting the on click listener, we can show the custom keyboard again, by tapping on an edit box that already had focus (but that had the keyboard hidden).
            @Override
            public void onClick(View v) {
                showCustomKeyboard(mHostActivity.get().findViewById(R.id.ui2keyboardview), v);
            }
        });

        // Disable standard keyboard hard way
        // NOTE There is also an easy way: 'edittext.setInputType(InputType.TYPE_NULL)' (but you will not have a cursor, and no 'edittext.setCursorVisible(true)' doesn't work )
        edittext.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                CustomEditText edittext = (CustomEditText) v;
                int inType = edittext.getInputType();       // Backup the input type
                edittext.setInputType(InputType.TYPE_NULL); // Disable standard keyboard
                edittext.onTouchEvent(event);               // Call native handler
                edittext.setInputType(inType);              // Restore input type
                return true; // Consume touch event
            }
        });

        edittext.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (doneListener != null && (id == EditorInfo.IME_ACTION_DONE || keyEvent.getKeyCode() == KEYCODE_ENTER)) {
                    doneListener.onDoneClicked();
                    return true;
                }

                if (cancelListener != null && (id == EditorInfo.IME_ACTION_DONE || keyEvent.getKeyCode() == android.inputmethodservice.Keyboard.KEYCODE_CANCEL)) {
                    cancelListener.onCancelClicked();
                    return true;
                }

                return false;
            }
        });

        edittext.setOnKeyListener((v, keyCode, event) -> {
            if (cancelListener != null && (keyCode == Keyboard.KEYCODE_CANCEL) && (event.getAction() == KeyEvent.ACTION_DOWN)) {
                cancelListener.onCancelClicked();
                return true;
            }
            return false;
        });

        // Disable spell check (hex strings look like words to Android)
        edittext.setInputType(edittext.getInputType() | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
    }

    public enum KBTypes {
        AMOUNT_KB( true ),
        TEXT_KB( false ),
        TEXT_KB_CAPS( false ),
        TEXT_KB_CAPS_NO_SYM( false ),
        IP_PORT_KB( true ),
        PHONE_KB( true ),
        USR_PASS_KB( true ),
        NUM_ALPHA( false ),
        NUM_ALPHA_NO_SYM( false );

        private boolean numericInputOnly;

        KBTypes( boolean numericInputOnly ){
            this.numericInputOnly = numericInputOnly;
        }

        boolean isNumericInputOnly(){
            return this.numericInputOnly;
        }
    }
}
