package com.linkly.libengine.action.Loyalty;

import static com.linkly.libengine.action.Loyalty.GameCode.POST_AMOUNT_ENTRY;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.MessageStatus.FINALISED;
import static com.linkly.libsecapp.IP2PEncrypt.ElementType.CARD_TOKEN;
import static com.linkly.libsecapp.IP2PEncrypt.EncryptAlgorithm.NONE;
import static com.linkly.libsecapp.IP2PEncrypt.PackFormat.ASCII;
import static com.linkly.libui.UIScreenDef.ACT_PROCESSING_LOYALTY_CTLS;
import static com.linkly.libui.UIScreenDef.LOYALTY_APP_POST_AMOUNT_ENTRY;
import static com.linkly.libui.UIScreenDef.LOYALTY_APP_POST_CARD_PRESENTED;
import static com.linkly.libui.UIScreenDef.LOYALTY_APP_POST_TRANSACTION;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;

import com.linkly.libengine.action.IAction;
import com.linkly.libengine.action.TransactionApproval;
import com.linkly.libengine.action.cardprocessing.GetCard;
import com.linkly.libengine.engine.Engine;
import com.linkly.libengine.engine.cards.Ctls;
import com.linkly.libengine.engine.transactions.TransRec;
import com.linkly.libmal.MalFactory;
import com.linkly.libmal.global.util.Util;
import com.linkly.libsecapp.CardholderDataElement;
import com.linkly.libsecapp.EncryptResult;
import com.linkly.libsecapp.IP2PEncrypt;
import com.linkly.libsecapp.IP2PSec;
import com.linkly.libsecapp.P2PLib;
import com.linkly.libui.IUIDisplay;

import java.util.HashMap;

import timber.log.Timber;

public class LoyaltyProcessing extends IAction {

    static String lastToken;
    static String lastAmount;
    static boolean runningApp = false;
    static final Object gameLock = new Object();
    static final Object broadcastLock = new Object();

    public enum LoyaltySteps{LOYALTY_STEP_NOT_SET, LOYALTY_STEP_AFTER_AMOUNT_ENTERED, LOYALTY_STEP_AFTER_CARD_TAPPED, LOYALTY_STEP_AFTER_AUTHORISED };
    private LoyaltySteps currentStep = LoyaltySteps.LOYALTY_STEP_NOT_SET;
    private static LoyaltySteps lastStep = LoyaltySteps.LOYALTY_STEP_NOT_SET;

    public LoyaltyProcessing(LoyaltySteps step) {
        currentStep = step;
    }

    @Override
    public String getName() {
        return "LoyaltyProcessing";
    }

    @Override
    public void run() {

        if ( !d.getPayCfg().isLoyaltySupported())
            return;

        if (!isPLMInstalled()) {
            d.getPayCfg().setLoyaltySupported(false);
            Timber.i( "Stop Trying as not installed");
            return;
        }

        runningApp = false;
        lastStep = currentStep;

        if (isWinLoyaltyInstalled() && currentStep == LoyaltySteps.LOYALTY_STEP_AFTER_AMOUNT_ENTERED) {
            Timber.i( "LOYALTY_STEP_AFTER_AMOUNT_ENTERED");
            AfterAmountEntered();
        }

        if (currentStep == LoyaltySteps.LOYALTY_STEP_AFTER_CARD_TAPPED) {
            Timber.i( "LOYALTY_STEP_AFTER_CARD_TAPPED");
            AfterCardTapped();
        }

        if (currentStep == LoyaltySteps.LOYALTY_STEP_AFTER_AUTHORISED) {
            Timber.i( "LOYALTY_STEP_AFTER_AUTHORISED");
            AfterAuthorised();
        }
    }

    private String getSensitiveElement( IP2PEncrypt.ElementType element ) {

        IP2PEncrypt p2pEncrypt = d.getP2PLib().getIP2PEncrypt();

        int dataLen = p2pEncrypt.getElementLength( element );
        if( dataLen <= 0 ) {
            Timber.i( "WARNING - addSensitiveElement element " + element.name() + " not found, skipping" );
            return null;
        }

        CardholderDataElement[] elements = new CardholderDataElement[1];

        elements[0] = new CardholderDataElement( element, false, 0, dataLen, ASCII );
        String template = new String(new char[dataLen]).replace("\0", "X"); // 'X' masked data

        // using p2pe module in cleartext mode, substitute track 2 (mag) with X chars
        EncryptResult encryptResult = p2pEncrypt.encrypt( template.getBytes(), new IP2PEncrypt.EncryptParameters( IP2PEncrypt.PaddingAlgorithm.NONE, NONE, IP2PSec.KeyGroup.TERM_GROUP.getKeyIndex() ), elements );
        if( encryptResult == null ) {
            Timber.i( "WARNING - addSensitiveElement element encryption failed" );
            return null;
        }

        return new String( encryptResult.getEncryptedMessage() );
    }

    public void AfterAmountEntered() {
        Timber.i( "AfterAmountEntered");

        if (!trans.isSale()) {
            Timber.i( "Only Offered on Sales");
            return;
        }

        if (trans.getAmounts().getTotalAmount() == 0) {
            Timber.i( "Only Offered on transactions with amount");
            return;
        }

        if (trans.getAmounts().getDiscountedAmount() > 0) {
            Timber.i( "Discount already applied to this transaction"); /* for ctls we cant tap twice and get 2 discounts */
            return;
        }

        if (trans.getAudit().isLoyaltyPlayed()) {
            Timber.i( "Loyalty app already contacted for win");
            return;
        }
        trans.getAudit().setLoyaltyPlayed(true);


        lastAmount = String.format("%d.%02d", trans.getAmounts().getTotalAmount() / 100 , trans.getAmounts().getTotalAmount() % 100);
        Timber.i( "AMT:" + lastAmount);

        sendDataToGameApp(lastAmount, lastToken, POST_AMOUNT_ENTRY);

    }

    private void AfterCardTapped() {

        Timber.i( "AfterCardTapped");

        if (!trans.isSale()) {
            Timber.i( "Only Offered on Sales");
            return;
        }

        if (trans.getAmounts().getTotalAmount() == 0) {
            Timber.i( "Only Offered on transactions with amount");
            return;
        }

        if (trans.getAmounts().getDiscountedAmount() > 0) {
            Timber.i( "Discount already applied to this transaction"); /* for ctls we cant tap twice and get 2 discounts */
            return;
        }

        if (trans.getAudit().isLoyaltyAppSentCard()) {
            Timber.i( "Loyalty app already sent this card");
            return;
        }
        trans.getAudit().setLoyaltyAppSentCard(true);

        lastAmount = String.format("%d.%02d", trans.getAmounts().getTotalAmount() / 100 , trans.getAmounts().getTotalAmount() % 100);
        lastToken = getSensitiveElement(CARD_TOKEN);
        Timber.i( "" + lastToken + "AMT:" + lastAmount);

        if (Util.isNullOrWhitespace(lastToken))
            return;

        sendDataToGameApp(lastAmount, lastToken, GameCode.POST_CARD_PRESENTED);

        /* special cases for when a discount has been applied */
        if ( trans.getAmounts().getDiscountedAmount() > 0 ) {

            /* check if there is nothing to pay now */
            if (trans.getAmounts().getAmount() == 0) {
                trans.updateMessageStatus(FINALISED);
                d.getWorkflowEngine().setNextAction(TransactionApproval.class);
            }
            else if (trans.getCard().isCtlsCaptured()) {
                ui.showScreen(ACT_PROCESSING_LOYALTY_CTLS);
                d.getWorkflowEngine().setNextAction(GetCard.class);
                Ctls.getInstance().ctlsRetryCardEntry(d, trans);
            }
        }


    }

    public void AfterAuthorised() {
        Timber.i( "AfterAuthorised");
        // notify and send data to game app
        if (!Util.isNullOrWhitespace(lastToken) && !Util.isNullOrWhitespace(lastAmount)) {
            sendDataToGameApp(lastAmount, lastToken, GameCode.POST_TRANSACTION);
        }

    }

    private void waitForBroadcastResponseOrAppResponse() {
        Timber.i( "Wait For Broadcast Response");
        waitForLock(broadcastLock, 3000);
        Timber.i( "After Wait For Broadcast Response");

        if (runningApp) {
            Timber.i( "Wait For App Response");
            waitForLock(gameLock, 120000);
            Timber.i( "After Wait For App Response");
            runningApp = false;
        }
    }


    private static boolean isWinLoyaltyInstalled() {
        PackageManager packageManager = MalFactory.getInstance().getMalContext().getPackageManager();
        if ( packageManager == null)
            return false;

        Intent winGame = packageManager.getLaunchIntentForPackage(GameCode.WIN_GAME_PACKAGE_NAME);
        if (winGame != null) {
            Timber.i( "isWinLoyaltyInstalled: true (WIN_GAME_PACKAGE_NAME)");
            return true;
        }

        Timber.i( "isWinLoyaltyInstalled: false");
        return false;
    }

    private static boolean isPLMInstalled() {
        PackageManager packageManager = MalFactory.getInstance().getMalContext().getPackageManager();
        if ( packageManager == null)
            return false;

        Intent nominate = packageManager.getLaunchIntentForPackage(GameCode.NOMINATE_GAME_PACKAGE_NAME);
        if (nominate != null) {
            Timber.i( "isPLMInstalled: true (NOMINATE_GAME_PACKAGE_NAME)");
            return true;
        }

        if (isWinLoyaltyInstalled()) {
            Timber.i( "isPLMInstalled: true (WIN_GAME_PACKAGE_NAME)");
            return true;
        }

        Timber.i( "isPLMInstalled: false");
        return false;
    }

    private void sendDataToGameApp(String lastAmount, String lastToken, String launchFrom) {

        // notify and send data to game app
        Intent intent = new Intent();
        intent.setAction(GameCode.GAME_ACTION);

        if (!Util.isNullOrEmpty(lastToken))
            intent.putExtra(GameCode.CARD_TOKEN, lastToken);

        if (!Util.isNullOrEmpty(lastAmount))
            intent.putExtra(GameCode.AMOUNT, lastAmount);
        intent.putExtra(GameCode.LAUNCH_FROM, launchFrom);
        intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        MalFactory.getInstance().getMalContext().sendBroadcast(intent);

        waitForBroadcastResponseOrAppResponse();

    }

    public static void openGameApp(Activity activity, String launchFrom) {
        Timber.i( "openGameApp:" + launchFrom);
        Intent gameIntent = new Intent();
        if (gameIntent != null) {
            // We found the activity now start the activity
            gameIntent.setAction(GameCode.OPEN_PLM_ACTION);
            gameIntent.setFlags(0);
            if (!Util.isNullOrEmpty(lastToken))
                gameIntent.putExtra(GameCode.CARD_TOKEN, lastToken);

            if (!Util.isNullOrEmpty(lastAmount))
                gameIntent.putExtra(GameCode.AMOUNT, lastAmount);

            if (!Util.isNullOrEmpty(launchFrom))
                gameIntent.putExtra(GameCode.LAUNCH_FROM, launchFrom);

            activity.startActivityForResult(gameIntent, GameCode.OPEN_GAME_REQUEST);
        }
    }

    public static void processIntent(Intent intent) {

        HashMap<String, Object> map = new HashMap<>();
        map.put(IUIDisplay.uiKeepOnScreen, true);

        Timber.i( "BroadcastReceiver");

        if ( lastStep == LoyaltySteps.LOYALTY_STEP_AFTER_AMOUNT_ENTERED) {
            Timber.i( "LOYALTY_STEP_AFTER_AMOUNT_ENTERED");

            int status = intent.getIntExtra(GameCode.GAME_STATUS, 0);
            if (status ==  GameStatus.OPEN_APP && !LoyaltyProcessing.getRunningLoyaltyApp()) {
                Timber.i( "Received LOYALTY_APP_POST_CARD_PRESENTED");
                LoyaltyProcessing.setRunningLoyaltyApp();
                // TODO: try delete this. We don't seem to use this. however issues with this is that this comes directly from an intent...
                Engine.getDep().getUI().showScreen(LOYALTY_APP_POST_AMOUNT_ENTRY, map);
            }
        }


        if ( lastStep == LoyaltySteps.LOYALTY_STEP_AFTER_CARD_TAPPED && intent.hasExtra(GameCode.GAME_STATUS)) {  // changed from IS_REWARD
            Timber.i( "Received IS_REWARD (After Card Tapped)");

            int status = intent.getIntExtra(GameCode.GAME_STATUS, 0);
            if (status ==  GameStatus.OPEN_APP && !LoyaltyProcessing.getRunningLoyaltyApp()) {
                Timber.i( "Received LOYALTY_APP_POST_CARD_PRESENTED");
                LoyaltyProcessing.setRunningLoyaltyApp();
                Engine.getDep().getUI().showScreen(LOYALTY_APP_POST_CARD_PRESENTED, map);
            }
        }

        if (lastStep == LoyaltySteps.LOYALTY_STEP_AFTER_AUTHORISED  && intent.hasExtra(GameCode.GAME_STATUS)) {

            int status = intent.getIntExtra(GameCode.GAME_STATUS, 0);
            Timber.i( "Received GAME_STATUS: " + status);
            if (status == GameStatus.OPEN_APP && !LoyaltyProcessing.getRunningLoyaltyApp()) {

                Timber.i( "Received LOYALTY_APP_POST_TRANSACTION");
                LoyaltyProcessing.setRunningLoyaltyApp();
                Engine.getDep().getUI().showScreen(LOYALTY_APP_POST_TRANSACTION, map);
            }
        }
        LoyaltyProcessing.proceedFromBroadcast();
    }
    public static void setRunningLoyaltyApp() {
        Timber.i( "Running the App");
        runningApp = true;
    }

    public static boolean getRunningLoyaltyApp() {
        return runningApp;
    }

    public static void proceedFromGame(String discountAmount) {
        Timber.i( "proceedFromGame");
        runningApp = false;

        if (discountAmount != null) {
            TransRec trans = Engine.getDep().getCurrentTransaction();

            discountAmount = discountAmount.replace(".", "");

            long longDiscountAmount = Long.parseLong(discountAmount);
            long amount = trans.getAmounts().getAmount();
            long revisedAmount = amount - longDiscountAmount;

            if (revisedAmount < 0)
                revisedAmount = 0;

            trans.getAmounts().setAmount(revisedAmount);
            trans.getAmounts().setDiscountedAmount(longDiscountAmount);

            Timber.i( "Update Amount with:" + revisedAmount + "(discount = " + longDiscountAmount + ")");

        }
        proceed(gameLock);
    }

    public static void proceedFromBroadcast() {
        Timber.i( "proceedFromBroadcast");
        proceed(broadcastLock);
    }


    private static void proceed(Object lock) {
        Timber.i( "Call proceed");
        try {
            synchronized (lock) {
                lock.notify();
            }
        } catch ( Exception e) {
        }
    }

    private void waitForLock(Object lock, long timeout) {
        try {
            synchronized (lock) {
                lock.wait(timeout);
            }
        } catch ( Exception e) {
            lock.notify();
        }
    }

}
