package com.linkly.libengine.workflow;

import static com.linkly.libengine.action.CheckEFB.CheckEFBMode.CHECK_EFB_AFTER_ONLINE_PROCESSING;
import static com.linkly.libengine.action.CheckEFB.CheckEFBMode.CHECK_EFB_BEFORE_ONLINE_PROCESSING;
import static com.linkly.libengine.action.Loyalty.LoyaltyProcessing.LoyaltySteps.LOYALTY_STEP_AFTER_AMOUNT_ENTERED;
import static com.linkly.libengine.action.Loyalty.LoyaltyProcessing.LoyaltySteps.LOYALTY_STEP_AFTER_AUTHORISED;
import static com.linkly.libengine.action.Loyalty.LoyaltyProcessing.LoyaltySteps.LOYALTY_STEP_AFTER_CARD_TAPPED;

import com.linkly.libengine.action.CheckDuplicates;
import com.linkly.libengine.action.CheckEFB;
import com.linkly.libengine.action.ConstrainedAction;
import com.linkly.libengine.action.DB.DBSave;
import com.linkly.libengine.action.DB.DBTransPurge;
import com.linkly.libengine.action.DB.DBUpdateShiftTotals;
import com.linkly.libengine.action.DCC.DCCLookup;
import com.linkly.libengine.action.HostActions.Authorise;
import com.linkly.libengine.action.HostActions.AuthoriseOffline;
import com.linkly.libengine.action.HostActions.CheckDeferredAuth;
import com.linkly.libengine.action.HostActions.PostAuthorisation;
import com.linkly.libengine.action.HostActions.PreAuthorisation;
import com.linkly.libengine.action.IPC.EmailUpload;
import com.linkly.libengine.action.IPC.EnablePowerKey;
import com.linkly.libengine.action.IPC.TransResponse;
import com.linkly.libengine.action.IPC.UserUndoUpgrade;
import com.linkly.libengine.action.InitialProcessing;
import com.linkly.libengine.action.InterfaceSelected;
import com.linkly.libengine.action.JumpTo;
import com.linkly.libengine.action.Loyalty.LoyaltyProcessing;
import com.linkly.libengine.action.MenuOperations.admin.SubmitTransactionsSchedule;
import com.linkly.libengine.action.PerformProtocolTasks;
import com.linkly.libengine.action.Printing.PrintFirst;
import com.linkly.libengine.action.Printing.PrintSecond;
import com.linkly.libengine.action.Printing.SkipPrintingIfNoCardPresented;
import com.linkly.libengine.action.TransactionApproval;
import com.linkly.libengine.action.TransactionCanceller;
import com.linkly.libengine.action.TransactionDecliner;
import com.linkly.libengine.action.TransactionDeferred;
import com.linkly.libengine.action.TransactionFinalizer;
import com.linkly.libengine.action.TransactionReferral;
import com.linkly.libengine.action.cardprocessing.CTLSProcessing;
import com.linkly.libengine.action.cardprocessing.CardPostcomms;
import com.linkly.libengine.action.cardprocessing.CardResetData;
import com.linkly.libengine.action.cardprocessing.CardWipeData;
import com.linkly.libengine.action.cardprocessing.DCCProcessing;
import com.linkly.libengine.action.cardprocessing.FallbackRemoveCard;
import com.linkly.libengine.action.cardprocessing.GetCard;
import com.linkly.libengine.action.cardprocessing.ICCGAC1;
import com.linkly.libengine.action.cardprocessing.MagstripeProcessing;
import com.linkly.libengine.action.cardprocessing.ManualProcessing;
import com.linkly.libengine.action.cardprocessing.PKEProcessing;
import com.linkly.libengine.action.cardprocessing.RemoveCard;
import com.linkly.libengine.action.cardprocessing.StartCardProcessing;
import com.linkly.libengine.action.check.CheckAmounts;
import com.linkly.libengine.action.check.CheckBINRange;
import com.linkly.libengine.action.check.CheckConfig;
import com.linkly.libengine.action.check.CheckCreditAccountAllowed;
import com.linkly.libengine.action.check.CheckOfflineAllowed;
import com.linkly.libengine.action.check.CheckP2P;
import com.linkly.libengine.action.check.CheckPrinter;
import com.linkly.libengine.action.check.CheckReferralRequired;
import com.linkly.libengine.action.check.CheckResult;
import com.linkly.libengine.action.check.CheckScreenSignature;
import com.linkly.libengine.action.check.CheckSignature;
import com.linkly.libengine.action.check.CheckSignatureRequired;
import com.linkly.libengine.action.check.CheckTransAllowed;
import com.linkly.libengine.action.check.CheckUserLevel;
import com.linkly.libengine.action.check.IsSignatureRequired;
import com.linkly.libengine.action.check.TimeNeedsFixing;
import com.linkly.libengine.action.user_action.BackToIdlePosNotification;
import com.linkly.libengine.action.user_action.DisplayFinishTransaction;
import com.linkly.libengine.action.user_action.DisplaySummary;
import com.linkly.libengine.action.user_action.InputAccount;
import com.linkly.libengine.action.user_action.InputAmount;
import com.linkly.libengine.action.user_action.InputCashback;
import com.linkly.libengine.action.user_action.InputReference;
import com.linkly.libengine.action.user_action.InputReferral;
import com.linkly.libengine.action.user_action.InputTip;
import com.linkly.libengine.action.user_action.MainMenu;
import com.linkly.libengine.action.user_action.UiApproved;
import com.linkly.libengine.action.user_action.UiCancelled;
import com.linkly.libengine.action.user_action.UiDeclined;
import com.linkly.libengine.action.user_action.UiInputOnlinePin;
import com.linkly.libengine.action.user_action.UiProcessing;
import com.linkly.libengine.engine.transactions.properties.TCard;

public class CommonPayment extends Workflow {
    public CommonPayment() {


        //initial
        this.addAction(new CheckDuplicates());
        this.addAction(new InitialProcessing());
        this.addAction(new CardWipeData());
        this.addAction(new CheckConfig());
        this.addAction(new CheckPrinter());
        this.addAction(new CheckP2P());
        this.addAction(new FallbackRemoveCard());
        this.addAction(new CheckTransAllowed());

        this.addAction(new PerformProtocolTasks());

        this.addAction(new InputAmount());

        this.addAction(new InputTip());
        this.addAction(new LoyaltyProcessing(LOYALTY_STEP_AFTER_AMOUNT_ENTERED));
        this.addAction(new InputReference());
        this.addAction(new CheckUserLevel());
        this.addAction(new GetCard());

        //card processing
        this.addAction(new InterfaceSelected());

        this.addAction(new ConstrainedAction(new UiProcessing(), new ConstrainedAction.IsCaptureMethod(TCard.CaptureMethod.SWIPED)));
        this.addAction(new ConstrainedAction(new UiProcessing(), new ConstrainedAction.IsCaptureMethod(TCard.CaptureMethod.CTLS_MSR)));

        this.addAction(new StartCardProcessing());
        this.addAction(new CTLSProcessing());
        this.addAction(new CheckBINRange());

        this.addAction(new CheckAmounts());
        this.addAction(new LoyaltyProcessing(LOYALTY_STEP_AFTER_CARD_TAPPED));
        this.addAction(new PKEProcessing());
        this.addAction(new ManualProcessing());
        this.addAction(new MagstripeProcessing());

        this.addAction(new ConstrainedAction(new InputAccount(), new ConstrainedAction.IsCaptureMethod(TCard.CaptureMethod.SWIPED)));
        this.addAction(new ConstrainedAction(new InputAccount(), new ConstrainedAction.IsCaptureMethod(TCard.CaptureMethod.ICC)));
        this.addAction(new ConstrainedAction(new InputAccount(), new ConstrainedAction.IsCaptureMethod(TCard.CaptureMethod.ICC_FALLBACK_SWIPED)));
        this.addAction(new CheckCreditAccountAllowed());

        //sale
        this.addAction(new ConstrainedAction(new CheckOfflineAllowed(), new ConstrainedAction.IsCaptureMethod(TCard.CaptureMethod.SWIPED)));
        this.addAction(new ConstrainedAction(new CheckOfflineAllowed(), new ConstrainedAction.IsCaptureMethod(TCard.CaptureMethod.CTLS_MSR)));

        this.addAction(new CheckUserLevel());
        this.addAction(new InputCashback());

        //this.addAction(new CheckP2PE()); should be removed

        // todo add a dcc not implemented placeholder, this is because dcc in this form will be removed
        this.addAction(new DCCLookup());

        this.addAction(new DCCProcessing());
        this.addAction(new DisplaySummary());

        this.addAction(new CheckEFB(CHECK_EFB_BEFORE_ONLINE_PROCESSING));
        this.addAction(new PreAuthorisation());
        this.addAction(new ICCGAC1());
        this.addAction(new IsSignatureRequired()); // never before seen a question to user if signature is required
        //todo task online pin
        this.addAction(new UiInputOnlinePin());
        this.addAction(new CheckReferralRequired());
        this.addAction(new Authorise()); //this is same for all interfaces
        this.addAction(new CardPostcomms()); // 2ndgen ac happens here.
        this.addAction(new CheckDeferredAuth());

        this.addAction(new CheckEFB(CHECK_EFB_AFTER_ONLINE_PROCESSING));
        // offline authorisation action
        this.addAction(new AuthoriseOffline());

        this.addAction(new CheckResult()); // Decision state, would jump to approved, declined etc.

        // referred transaction state
        this.addAction(new TransactionReferral());
        this.addAction(new DBSave());
        this.addAction(new InputReferral());
        this.addAction(new PrintFirst());
        this.addAction(new PrintSecond());
        this.addAction(new JumpTo(TransactionFinalizer.class));

        //approved
        this.addAction(new TransactionApproval());
        this.addAction(new DBSave());
        this.addAction(new CheckSignatureRequired());
        this.addAction(new UiApproved());
        this.addAction(new LoyaltyProcessing(LOYALTY_STEP_AFTER_AUTHORISED));
        this.addAction(new CheckScreenSignature());
        this.addAction(new PrintFirst()); // Can block for a period of time
        this.addAction(new CheckSignature());
        this.addAction(new PrintSecond());
        this.addAction(new JumpTo(TransactionFinalizer.class));

        // declined transaction state
        this.addAction(new TransactionDecliner());
        this.addAction(new DBSave());
        this.addAction(new UiDeclined());
        this.addAction(new SkipPrintingIfNoCardPresented());
        this.addAction(new PrintFirst());
        this.addAction(new PrintSecond());
        this.addAction(new JumpTo(TransactionFinalizer.class));

        // cancelled transaction state
        this.addAction(new TransactionCanceller());
        this.addAction(new DBSave());
        this.addAction(new UiCancelled());
        this.addAction(new SkipPrintingIfNoCardPresented());
        this.addAction(new PrintFirst());
        this.addAction(new PrintSecond());
        this.addAction(new JumpTo(TransactionFinalizer.class));

        //deferred
        this.addAction(new TransactionDeferred());
        this.addAction(new DBSave());
        this.addAction(new CardPostcomms());
        this.addAction(new UiApproved());
        this.addAction(new PrintFirst());
        this.addAction(new CheckSignature());
        this.addAction(new PrintSecond());
        this.addAction(new JumpTo(TransactionFinalizer.class));

        //finished
        this.addAction(new TransactionFinalizer());
        this.addAction(new PostAuthorisation());
        this.addAction(new DBUpdateShiftTotals());

        this.addAction(new DBSave());
        this.addAction(new RemoveCard());
        this.addAction(new DisplayFinishTransaction());
        this.addAction(new CardResetData());

        // sends txn response to POS
        this.addAction(new TransResponse());

        this.addAction(new EmailUpload());
        this.addAction(new UserUndoUpgrade());
        this.addAction(new EnablePowerKey());
        this.addAction(new TimeNeedsFixing());
        this.addAction(new MainMenu());
        this.addAction(new SubmitTransactionsSchedule(true));
        this.addAction(new DBTransPurge());
        this.addAction(new BackToIdlePosNotification());
    }
}
