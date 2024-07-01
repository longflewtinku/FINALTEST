package com.linkly.libengine.action.check;

import static com.linkly.libengine.status.IStatus.STATUS_EVENT.STATUS_ENTER_PASSWORD;
import static com.linkly.libui.UIScreenDef.PASSWORD_INCORRECT;

import androidx.annotation.NonNull;

import com.linkly.libengine.action.IAction;
import com.linkly.libengine.action.TransactionCanceller;
import com.linkly.libengine.action.TransactionDecliner;
import com.linkly.libengine.dependencies.IDependency;
import com.linkly.libengine.engine.protocol.IProto;
import com.linkly.libengine.engine.transactions.TransRec;
import com.linkly.libengine.users.UserManager;
import com.linkly.libmal.global.util.Util;
import com.linkly.libui.IUIDisplay;
import com.linkly.libui.UIScreenDef;

import java.util.HashMap;

import timber.log.Timber;

/**
 * Checks the password for certain transaction types
 * */
public class CheckPassword extends IAction {

    @Override
    public String getName() {
        return "CheckPassword";
    }

    @Override
    public void run() {
        final String configPassword = d.getCustomer().getTransPasscode(trans);
        int retryLimit = d.getCustomer().getTransPasscodeRetryCount(trans);

        // Check if the terminal is locked. This stops the issue where the password prompt in standalone check will happen even if the terminal is locked
        if(UserManager.getInstance().isFunctionUserLocked()) {
            userLocked(trans);
        } else if (!Util.isNullOrEmpty(configPassword) && !d.getCurrentTransaction().getFunctionUserLoggedIn()) {
            // Add moto password screen
            UIScreenDef screenDef = UIScreenDef.ENTER_PASSWORD;
            d.getStatusReporter().reportStatusEvent( STATUS_ENTER_PASSWORD , trans.isSuppressPosDialog());
            ui.showInputScreen( screenDef, null );

            while (retryLimit > 0) {

                IUIDisplay.UIResultCode uiResult = ui.getResultCode(screenDef.id, IUIDisplay.LONG_TIMEOUT);
                switch ( uiResult ) {
                    case OK:
                        String inputPassword = ui.getResultText( screenDef.id, IUIDisplay.uiResultText1 );
                        if (!configPassword.equals( inputPassword )) {
                            UserManager.getInstance().functionUserLoginFail();
                            if (UserManager.getInstance().isFunctionUserLocked()) {
                                userLocked(trans);
                                return;
                            } else {
                                HashMap<String,Object> map = new HashMap<>();
                                map.put(IUIDisplay.uiError, d.getPrompt(IUIDisplay.String_id.STR_PASSWORD_INCORRECT_TRY_AGAIN));
                                ui.showInputScreen(screenDef,map);
                                retryLimit--;
                                if (retryLimit == 0) {
                                    ui.showScreen(PASSWORD_INCORRECT);
                                    d.getProtocol().setInternalRejectReason(trans, IProto.RejectReasonType.PASSWORD_CHECK_FAILED);
                                    d.getWorkflowEngine().setNextAction(TransactionCanceller.class);
                                }
                            }
                        } else {
                            if ( UserManager.getInstance().isFunctionUserLocked() ) {
                                userLocked(trans);
                                return;
                            } else {
                                UserManager.getInstance().functionUserLoginSuccess();
                                d.getCurrentTransaction().setFunctionUserLoggedIn(true);
                                return;
                            }
                        }
                        break;
                    case TIMEOUT:
                        setTransDeclined(IProto.RejectReasonType.USER_TIMEOUT);
                        return;
                    default:
                        Timber.e("Password failed with [%s], setting to cancel", uiResult.toString());
                        d.getProtocol().setInternalRejectReason(trans, IProto.RejectReasonType.CANCELLED);
                        d.getWorkflowEngine().setNextAction(TransactionCanceller.class);
                        return;
                }
            }
        }
    }

    private void userLocked(TransRec trans) {
        UserManager.getInstance().displayFunctionUserLocked();
        d.getProtocol().setInternalRejectReason(trans, IProto.RejectReasonType.PASSWORD_CHECK_FAILED);
        d.getWorkflowEngine().setNextAction(TransactionCanceller.class);
    }

    /**
     * returns password from configuration, based on current trans type
     *
     * @param d     dependencies
     * @param trans transaction object
     * @return empty string = no password required. non-empty string = password for transaction
     */
    public static String loadPasswordFromConfig(IDependency d, @NonNull TransRec trans) {
        // checking for moto first as all the moto transactions need to be controlled
        // using moto password configuration even if it is a refund for a moto transaction
        if (trans.isMoto()) {
            // return password if enabled
            if( d.getPayCfg().isMotoPasswordPrompt() ) {
                return d.getPayCfg().getMotoRefundPassword();
            }
        } else if (trans.isRefund() && d.getPayCfg().isRefundPasswordPrompt()) {
            // return password if enabled
            return d.getPayCfg().getRefundPassword();
        }
        // else return empty string, meaning no password required
        return "";
    }

    /**
     /**
     * returns password from configuration, based on current trans type
     *
     * @param d     dependencies
     * @param trans transaction object
     * @return int, default of 3 otherwise
     */
    public static int loadPasswordRetryLimitFromConfig(IDependency d, @NonNull TransRec trans) {
        // checking for moto first as all the moto transactions need to be controlled
        // using moto password configuration even if it is a refund for a moto transaction
        try {
            if (trans.isMoto()) {
                return Integer.parseInt(d.getPayCfg().getMotoPasswordRetryLimit());
            } else if (trans.isRefund()) {
                return Integer.parseInt(d.getPayCfg().getRefundPasswordRetryLimit());
            } else {
                return 3; // default to 3
            }
        }
        catch (NumberFormatException e) {
            return 3; // default
        }
    }

    /**
     * Helper method to decline a transaction
     * @param rejectReasonType {@link com.linkly.libengine.engine.protocol.IProto.RejectReasonType} object
     * */
    private void setTransDeclined( IProto.RejectReasonType rejectReasonType ) {
        d.getProtocol().setInternalRejectReason( trans, rejectReasonType );
        d.getWorkflowEngine().setNextAction( TransactionDecliner.class );
    }
}
