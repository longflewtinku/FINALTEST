package com.linkly.payment.activities;

import static com.linkly.libengine.engine.EngineManager.TransType.AUTOSETTLEMENT;
import static com.linkly.payment.utilities.UIUtilities.screenSaverRequest;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;

import com.linkly.libengine.action.Loyalty.GameCode;
import com.linkly.libengine.action.Loyalty.GameStatus;
import com.linkly.libengine.action.Loyalty.LoyaltyProcessing;
import com.linkly.libengine.engine.Engine;
import com.linkly.libengine.engine.transactions.TransRec;
import com.linkly.libengine.jobs.EFTJob;
import com.linkly.libengine.users.UserManager;
import com.linkly.libmal.global.platform.EFTPlatform;
import com.linkly.libmal.global.util.DisplayKiosk;
import com.linkly.libpositive.events.PositiveEvent;
import com.linkly.libpositive.events.PositiveTransEvent;
import com.linkly.libpositive.messages.IMessages;
import com.linkly.libui.display.DisplayRequest;
import com.linkly.payment.R;
import com.linkly.payment.application.AppCallbacks;
import com.linkly.payment.databinding.ActivityTransBinding;
import com.linkly.payment.fragments.FragAccountSelect;
import com.linkly.payment.fragments.FragAddTip;
import com.linkly.payment.fragments.FragAnimatedPrint;
import com.linkly.payment.fragments.FragApplicationSelect;
import com.linkly.payment.fragments.FragApplicationSelectionAccessMode;
import com.linkly.payment.fragments.FragBigInfo;
import com.linkly.payment.fragments.FragBlank;
import com.linkly.payment.fragments.FragDeferredAuths;
import com.linkly.payment.fragments.FragGetCard;
import com.linkly.payment.fragments.FragHeader;
import com.linkly.payment.fragments.FragInformation;
import com.linkly.payment.fragments.FragInformationBasic;
import com.linkly.payment.fragments.FragInput;
import com.linkly.payment.fragments.FragInputAmount;
import com.linkly.payment.fragments.FragInputAmountIdle;
import com.linkly.payment.fragments.FragInputPin;
import com.linkly.payment.fragments.FragLogin;
import com.linkly.payment.fragments.FragPreAuthList;
import com.linkly.payment.fragments.FragPreAuthSearchAndViewSelectDate;
import com.linkly.payment.fragments.FragPrint;
import com.linkly.payment.fragments.FragQRDisplay;
import com.linkly.payment.fragments.FragQRScan;
import com.linkly.payment.fragments.FragQuestion;
import com.linkly.payment.fragments.FragReboot;
import com.linkly.payment.fragments.FragSAFViewAndClear;
import com.linkly.payment.fragments.FragSig;
import com.linkly.payment.fragments.FragTable;
import com.linkly.payment.fragments.FragTipPick;
import com.linkly.payment.fragments.FragTransactionHistory;
import com.linkly.payment.viewmodel.TransactionViewModel;

import timber.log.Timber;


public class ActTransaction extends BaseActivity<ActivityTransBinding, TransactionViewModel> implements FragHeader.onHeaderMenuItemSelectedListener {

    private static final String TAG = ActTransaction.class.getSimpleName();
    private static final String COM_PAX_PHYSICAL_KEY = "com.pax.physical.key";

    DisplayKiosk.NavigationBarState previousNavigationState = null; // Stores our previous state.

    private TransactionViewModel transViewModel;
    private final BroadcastReceiver PHYSICAL_KEY_RECEIVER = new BroadcastReceiver() {
        @Override
        public void onReceive( Context context, Intent intent ) {
            String action = intent.getAction();

            if( COM_PAX_PHYSICAL_KEY.equals( action ) ){
                String CANCEL_KEY = "1001";
                String KEY_NUM = "key_num";

                Timber.d("Intent = " + intent );
                if( CANCEL_KEY.equals( intent.getStringExtra( KEY_NUM ) ) ){
                    // Send abort message
                    Timber.d("Cancel Keypress received" );
                    PositiveTransEvent event = new PositiveTransEvent( PositiveEvent.EventType.CANCEL_TRANS );

                    event.setPosKeypress( PositiveTransEvent.POS_KEYPRESS.CANCEL );
                    Engine.getJobs().add( new EFTJob( event ) );
                }
            }
        }
    };

    //-------------------------------------------------------------------------------
    // Frag header Navigation overrides.
    // Hack due to crappy OS and system wide stateful navigation bar.
    // Seems that we can have menus being displayed under act transaction (login screen).
    // To allow to us to exit system screens when menu selecting we need to allow to "override" rather than "popping" off and returning to the original state.
    // This is only for options that we don't have control (eg system settings; wifi etc).
    boolean onOverrideToShowBackNavigationBar = false;
    @Override
    public void onHeaderMenuItemSelected() {
        // no-op
    }

    @Override
    public void onHeaderMenuItemNavigationBarOverride(boolean showBackNavigationBar) {
        onOverrideToShowBackNavigationBar = showBackNavigationBar;
    }
    //-------------------------------------------------------------------------------

    @Override
    public int getBindingVariable() {
        return com.linkly.payment.BR.transactionViewModel;
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_trans;
    }

    @Override
    public TransactionViewModel initViewModel() {
        transViewModel = ViewModelProviders.of(this).get(TransactionViewModel.class);
        return transViewModel;
    }

    @Override
    public TransactionViewModel getViewModel() {
        return transViewModel;
    }

    private LiveData<DisplayRequest> observed = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        overridePendingTransition(0, 0);
        if (observed == null || !observed.hasActiveObservers()) {
            observed = transViewModel.getDisplay();
            observed.observe(this, this::onDisplayChanged);
        }

        getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Timber.d("handleOnBackPressed...");
                finishAfterTransition();
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();

        RegisterAsExtendedActivity(IMessages.APP_FINISH_TRANSACTION_EVENT);
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction( ActTransaction.COM_PAX_PHYSICAL_KEY );
        getApplicationContext().registerReceiver( this.PHYSICAL_KEY_RECEIVER, intentFilter );
        previousNavigationState = new DisplayKiosk.NavigationBarState();
    }

    @VisibleForTesting
    public void onDisplayChanged(DisplayRequest display) {
        getViewModel().setDisplayRequestForResponse(display);
        showScreen(display);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (resultCode == Activity.RESULT_OK && data != null) {
            if (requestCode == GameCode.OPEN_GAME_REQUEST) {

                int status = data.getIntExtra(GameCode.GAME_STATUS, 0);
                Timber.i("Loyalty: onActivityResult: OPEN_GAME_REQUEST status=" + status);
                if (status == GameStatus.REWARD) {
                    String discountAmount = data.getStringExtra(GameCode.DISCOUNT_AMOUNT);
                    LoyaltyProcessing.proceedFromGame(discountAmount);
                    return;
                }
            }
        }
        LoyaltyProcessing.proceedFromGame(null);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onBackPressed() {
        Timber.d("onBackPressed...");
        getOnBackPressedDispatcher().onBackPressed();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // If the menu is being displaying in fragments that are in the activity a menu option is being displayed.
        // If selected and the menu option is causing us to go to an external screen that will require a back button to display.
        if(onOverrideToShowBackNavigationBar) {
            DisplayKiosk.getInstance().onResume(true);
            onOverrideToShowBackNavigationBar = false; // Reset in case the activity is not destroyed.
        }else {
            DisplayKiosk.getInstance().setNavigationBarAndButtonsState(previousNavigationState, true);
        }

        // moved from onDestroy: should probably unregister in onPause and register in onResume.
        getApplicationContext().unregisterReceiver( this.PHYSICAL_KEY_RECEIVER );
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    private void showScreen(DisplayRequest display) {
        Timber.i("showScreen:" + this.toString() + ":" + display.getActivityID().name());

        Fragment f = null;
        String name = "";
        switch(display.getActivityID()) {
            case ACT_SCREEN_SAVER:
            case ACT_MAIN_MENU:    break;

            case ACT_SCREEN_PRINT:  f = FragPrint.newInstance();        name = FragPrint.class.getSimpleName();break;
            case ACT_ANIMATED_PRINT:f = FragAnimatedPrint.newInstance();name = FragAnimatedPrint.class.getSimpleName();break;
            case ACT_SIG:           f = FragSig.newInstance();          name = FragSig.class.getSimpleName();break;
            case ACT_BIG_INFO:      f = FragBigInfo.newInstance();      name = FragBigInfo.class.getSimpleName();break;
            case ACT_INPUT:         f = FragInput.newInstance();        name = FragInput.class.getSimpleName();break;
            case ACT_INPUT_AMOUNT:  f = FragInputAmount.newInstance();  name = FragInputAmount.class.getSimpleName();break;
            case ACT_INPUT_AMOUNT_IDLE:  {
                boolean shouldForceSaleTransRec = AppCallbacks.getInstance().getShouldMainMenuDisplayInputAmountIdle();
                f = FragInputAmountIdle.newInstance(shouldForceSaleTransRec);  name = FragInputAmountIdle.class.getSimpleName();break; }
            case ACT_USER_LOGIN:    f = FragLogin.newInstance();        name = FragLogin.class.getSimpleName();break;
            case ACT_INPUT_PIN:     f = FragInputPin.newInstance();     name = FragInputPin.class.getSimpleName();break;
            case ACT_GET_CARD:      f = FragGetCard.newInstance();      name = FragGetCard.class.getSimpleName();break;
            case ACT_QUESTION:      f = FragQuestion.newInstance();     name = FragQuestion.class.getSimpleName();break;
            case ACT_ADD_TIP:       f = FragAddTip.newInstance();       name = FragAddTip.class.getSimpleName();break;
            case ACT_PICK_GRATUITY: f = FragTipPick.newInstance();      name = FragTipPick.class.getSimpleName();break;
            case ACT_TABLE:         f = FragTable.newInstance();        name = FragTable.class.getSimpleName();break;
            case ACT_REBOOT:        f = FragReboot.newInstance();       name = FragReboot.class.getSimpleName();break;
            case ACT_SELECT_ACCOUNT: f = FragAccountSelect.newInstance(); name = FragAccountSelect.class.getSimpleName(); break;
            case ACT_QR_SCAN:       f = FragQRScan.newInstance();       name = FragQRScan.class.getSimpleName();break;
            case ACT_QR_CODE:       f = FragQRDisplay.newInstance();    name = FragQRDisplay.class.getSimpleName();break;
            case ACT_BLANK:         f = FragBlank.newInstance();        name = FragBlank.class.getSimpleName();break;
            case ACT_INFORMATION:   f = FragInformation.newInstance();  name = FragInformation.class.getSimpleName(); break;
            case ACT_SELECT_APPLICATION: f = FragApplicationSelect.newInstance(); name = FragApplicationSelect.class.getSimpleName(); break;
            case ACT_INFORMATION_BASIC:   f = FragInformationBasic.newInstance();  name = FragInformationBasic.class.getSimpleName(); break;
            case ACT_LOYALTY_POST_AMOUNT_ENTRY: LoyaltyProcessing.openGameApp(this, GameCode.POST_AMOUNT_ENTRY ); break;
            case ACT_LOYALTY_POST_TRANSACTION: LoyaltyProcessing.openGameApp(this, GameCode.POST_TRANSACTION ); break;
            case ACT_LOYALTY_POST_CARD_PRESENTED: LoyaltyProcessing.openGameApp(this, GameCode.POST_CARD_PRESENTED );break;
            case ACT_DEFERRED_AUTHS: f = FragDeferredAuths.newInstance();     name = FragDeferredAuths.class.getSimpleName();break;
            case ACT_ACCESS_APP_SELECTION: f = FragApplicationSelectionAccessMode.newInstance();     name = FragApplicationSelectionAccessMode.class.getSimpleName();break;
            case ACT_SAF_VIEW_AND_CLEAR:   f = FragSAFViewAndClear.newInstance();       name = FragSAFViewAndClear.class.getSimpleName();break;
            case ACT_PRE_AUTH_SEARCH_AND_VIEW_SELECT_DATE:   f = FragPreAuthSearchAndViewSelectDate.newInstance();       name = FragPreAuthSearchAndViewSelectDate.class.getSimpleName();break;
            case ACT_PRE_AUTH_SEARCH_AND_VIEW:   f = FragPreAuthList.newInstance();       name = FragPreAuthList.class.getSimpleName();break;
            case ACT_TRANSACTION_HISTORY:   f = FragTransactionHistory.Companion.newInstance();       name = FragTransactionHistory.class.getSimpleName();break;
            default:
                Timber.i("Unsupported screen:");
        }

        if (f != null) {

            screenSaverRequest(display);

            getSupportFragmentManager()
                    .beginTransaction()
                    .disallowAddToBackStack()
                    .replace(R.id.fragment_frame_layout, f, name)
                    .commit();
        }
    }

    protected void finishTransaction() {
        Timber.d("finishTransaction...");
        super.finishTransaction();

        if (EFTPlatform.startupParams.hideWhenDone) {
            moveTaskToBack(true);
        }

        // Move task to back if last transaction was autosettlement and there is no user logged in
        if (UserManager.getActiveUser() == null) {
            TransRec trans = Engine.getDep().getCurrentTransaction();
            if (trans != null && trans.getReconciliationOriginalTransType() == AUTOSETTLEMENT) {
                moveTaskToBack(true);
            }
        }

        Timber.i("Exit the transaction screen and move to back as hidden");
        finishAfterTransition();
    }
}
