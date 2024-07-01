package com.linkly.libengine.action.user_action;

import static com.linkly.libconfig.cpat.CardProductCfg.ACC_CREDIT;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.ACC_TYPE_CHEQUE;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.ACC_TYPE_CREDIT;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.ACC_TYPE_DEFAULT;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.ACC_TYPE_SAVINGS;
import static com.linkly.libengine.status.IStatus.STATUS_EVENT.STATUS_UI_ACCOUNT_SELECTION;
import static com.linkly.libui.IUICurrency.EAmountFormat.FMT_AMT_SHOW_SYMBOL;
import static com.linkly.libui.IUIDisplay.String_id.STR_AMOUNT;
import static com.linkly.libui.IUIDisplay.String_id.STR_CANCEL;
import static com.linkly.libui.IUIDisplay.String_id.STR_CHEQUE;
import static com.linkly.libui.IUIDisplay.String_id.STR_CREDIT;
import static com.linkly.libui.IUIDisplay.String_id.STR_SAVINGS;
import static com.linkly.libui.IUIDisplay.String_id.STR_SELECT_ACCOUNT;
import static com.linkly.libui.IUIDisplay.UIResultCode.ABORT;
import static com.linkly.libui.IUIDisplay.UIResultCode.TIMEOUT;
import static com.linkly.libui.UIScreenDef.ONLY_CREDIT_ACCOUNT_ALLOWED;
import static com.linkly.libui.UIScreenDef.SELECT_ACCOUNT;
import static com.linkly.libui.display.DisplayQuestion.EButtonStyle.BTN_STYLE_PRIMARY_DEFAULT_DOUBLE;
import static com.linkly.libui.display.DisplayQuestion.EButtonStyle.BTN_STYLE_TRANSPARENT_DOUBLE;

import com.linkly.libconfig.cpat.CardProductCfg;
import com.linkly.libconfig.uiconfig.ConfigTimeouts;
import com.linkly.libengine.action.IAction;
import com.linkly.libengine.action.TransactionCanceller;
import com.linkly.libengine.action.TransactionDecliner;
import com.linkly.libengine.debug.IDebug;
import com.linkly.libengine.engine.protocol.IProto;
import com.linkly.libengine.engine.transactions.properties.TAmounts;
import com.linkly.libui.IUIDisplay;
import com.linkly.libui.UIScreenDef;
import com.linkly.libui.display.DisplayFragmentOption;
import com.linkly.libui.display.DisplayQuestion;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import timber.log.Timber;

public class InputAccount extends IAction {

    @Override
    public String getName() {
        return "InputAccount";
    }

    @Override
    public void run() {

        // if account already selected, skip this step
        //AND app selection was done
        if (trans.getProtocol().getAccountType() != 0 && trans.getCard().isAppSelected())
            return;

        //check the value of accountSelection in cardproduct.json and display the options accordingly.
        int allowedAccTypes = trans.getAccountSelection(d.getPayCfg());

        // if preauth and 'preauthCreditAccountOnly' is set, limit account type to credit only
        if( trans.isPreAuth() && d.getPayCfg().isPreauthCreditAccountOnly() ) {
            allowedAccTypes &= ACC_CREDIT;

            // if no valid account types, reject txn with txn not allowed type error
            if( allowedAccTypes == 0 ) {
                Timber.e( "Preauth txn and only credit is allowed, but card doesn't have credit account, declining" );
                //Display credit only Allowed screen.
                ui.showScreen(ONLY_CREDIT_ACCOUNT_ALLOWED);

                Objects.requireNonNull( d.getProtocol() ).setInternalRejectReason( super.trans, IProto.RejectReasonType.PREAUTH_NOT_ALLOWED_FOR_CARD );
                d.getWorkflowEngine().setNextAction( TransactionDecliner.class );
                return;
            }
        }

        // Don't display select account screen if one account is allowed
        switch (allowedAccTypes){
            case CardProductCfg.ACC_DEFAULT:
                //select the ‘default’ account (as2805 woolies de3 processing code account type 0
                trans.getProtocol().setAccountType(ACC_TYPE_DEFAULT);
                return;
            case CardProductCfg.ACC_CHEQUE:
                trans.getProtocol().setAccountType(ACC_TYPE_CHEQUE);
                d.getDebugReporter().reportDebugAccountSelect(IDebug.DEBUG_ACCOUNT.CHEQUE);
                return;
            case CardProductCfg.ACC_SAVINGS:
                trans.getProtocol().setAccountType(ACC_TYPE_SAVINGS);
                d.getDebugReporter().reportDebugAccountSelect(IDebug.DEBUG_ACCOUNT.SAVINGS);
                return;
            case ACC_CREDIT:
                trans.getProtocol().setAccountType(ACC_TYPE_CREDIT);
                d.getDebugReporter().reportDebugAccountSelect(IDebug.DEBUG_ACCOUNT.CREDIT);
                return;
        }

        boolean running = true;

        while (running) {
            // report 'waiting for account selection' status
            d.getStatusReporter().reportStatusEvent(STATUS_UI_ACCOUNT_SELECTION , trans.isSuppressPosDialog());

            // TODO: display amount on this screen as well
            TAmounts amounts = trans.getAmounts();
            String amountFormatted = curr.formatUIAmount(String.valueOf(amounts.getTotalAmountWithoutSurcharge()), FMT_AMT_SHOW_SYMBOL, d.getPayCfg().getCountryCode());

            HashMap<String, Object> map = new HashMap<>();
            map.put(IUIDisplay.uiScreenFragType, IUIDisplay.FRAG_TYPE.FRAG_GRID);

            ArrayList<DisplayFragmentOption> fragOptions = new ArrayList<>();
            fragOptions.add(new DisplayFragmentOption(d.getPrompt(STR_AMOUNT) + ":", amountFormatted));
            fragOptions.add(new DisplayFragmentOption(d.getPrompt(STR_SELECT_ACCOUNT)));
            map.put(IUIDisplay.uiScreenFragOptionList, fragOptions);

            ArrayList<DisplayQuestion> options = new ArrayList<>();

            //Populate the account buttons based on the selected value.
            if ((allowedAccTypes & CardProductCfg.ACC_CHEQUE) != 0) {
                options.add(new DisplayQuestion(STR_CHEQUE, "1", BTN_STYLE_PRIMARY_DEFAULT_DOUBLE));
            }
            if ((allowedAccTypes & CardProductCfg.ACC_SAVINGS) != 0) {
                options.add(new DisplayQuestion(STR_SAVINGS, "2", BTN_STYLE_PRIMARY_DEFAULT_DOUBLE));
            }
            if ((allowedAccTypes & ACC_CREDIT) != 0) {
                options.add(new DisplayQuestion(STR_CREDIT, "3", BTN_STYLE_PRIMARY_DEFAULT_DOUBLE));
            }

            options.add(new DisplayQuestion(STR_CANCEL, "C", BTN_STYLE_TRANSPARENT_DOUBLE));
            map.put(IUIDisplay.uiScreenOptionList, options);
            map.put(IUIDisplay.uiTitleId, trans.getTransType().displayId);

            ui.showScreen( SELECT_ACCOUNT, map );
            IUIDisplay.UIResultCode uiResultCode =
                    ui.getResultCode( UIScreenDef.valueOf( SELECT_ACCOUNT.name() ).id,
                            super.d.getPayCfg().
                                    getUiConfigTimeouts().
                                    getTimeoutMilliSecs( ConfigTimeouts.ACCOUNT_SELECTION_TIMEOUT,
                                                    trans.getAudit().isAccessMode() ) );

            if (uiResultCode == IUIDisplay.UIResultCode.OK) {
                String result = ui.getResultText(UIScreenDef.valueOf(SELECT_ACCOUNT.name()).id, IUIDisplay.uiResultText1);

                Timber.i( "Select account result %s", result );
                switch( result ) {
                    case "C":
                        d.getWorkflowEngine().setNextAction(TransactionCanceller.class);
                        d.getDebugReporter().reportCancelSelect(IDebug.DEBUG_POSITION.SELECT_ACCOUNT);
                        break;
                    case "1":
                        trans.getProtocol().setAccountType(ACC_TYPE_CHEQUE);
                        d.getDebugReporter().reportDebugAccountSelect(IDebug.DEBUG_ACCOUNT.CHEQUE);
                        break;
                    case "2":
                        trans.getProtocol().setAccountType(ACC_TYPE_SAVINGS);
                        d.getDebugReporter().reportDebugAccountSelect(IDebug.DEBUG_ACCOUNT.SAVINGS);
                        break;
                    case "3":
                        trans.getProtocol().setAccountType(ACC_TYPE_CREDIT);
                        d.getDebugReporter().reportDebugAccountSelect(IDebug.DEBUG_ACCOUNT.CREDIT);
                        break;
                    default:
                        break;
                }
                running = false;
            } else if (uiResultCode == TIMEOUT || uiResultCode == ABORT) { // Handle failure cases.
                d.getWorkflowEngine().setNextAction(TransactionCanceller.class);
                d.getDebugReporter().reportTimeout(IDebug.DEBUG_POSITION.SELECT_ACCOUNT);
                d.getProtocol().setInternalRejectReason( trans, uiResultCode == TIMEOUT ? IProto.RejectReasonType.USER_TIMEOUT : IProto.RejectReasonType.CANCELLED );
                running = false;
                Timber.e("Select Account Timeout");
            }
        }
    }
}


