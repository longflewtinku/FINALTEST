package com.linkly.libengine.action.MenuOperations.admin;

import static com.linkly.libengine.printing.IPrintManager.PrintPreference.PRINT_PREFERENCE_BOTH;
import static com.linkly.libengine.printing.IPrintManager.PrintPreference.PRINT_PREFERENCE_SCREEN;
import static com.linkly.libmal.global.printing.PrintReceipt.SCREEN_ICON.PR_SUCCESS_ICON;
import static com.linkly.libui.IUIDisplay.ACTIVITY_ID.ACT_INFORMATION;
import static com.linkly.libui.IUIDisplay.ACTIVITY_ID.ACT_INPUT;
import static com.linkly.libui.IUIDisplay.ACTIVITY_ID.ACT_PRE_AUTH_SEARCH_AND_VIEW;
import static com.linkly.libui.IUIDisplay.ACTIVITY_ID.ACT_PRE_AUTH_SEARCH_AND_VIEW_SELECT_DATE;
import static com.linkly.libui.IUIDisplay.String_id.STR_LIST_PRE_AUTH;
import static com.linkly.libui.IUIDisplay.String_id.STR_PRE_AUTH_SEARCH_AND_VIEW;
import static com.linkly.libui.IUIDisplay.String_id.STR_PRE_AUTH_SEARCH_AND_VIEW_SELECT_DATE;
import static com.linkly.libui.IUIDisplay.String_id.STR_PRINTING;
import static com.linkly.libui.IUIDisplay.UIResultCode.ABORT;
import static com.linkly.libui.IUIDisplay.UIResultCode.OK;
import static com.linkly.libui.UIScreenDef.DISPLAY_PRE_AUTH_SEARCH_AND_VIEW;
import static com.linkly.libui.UIScreenDef.DISPLAY_PRE_AUTH_SEARCH_AND_VIEW_SELECT_DATE;
import static com.linkly.libui.UIScreenDef.ENTER_AUTH_CODE;
import static com.linkly.libui.UIScreenDef.ENTER_RRN;
import static com.linkly.libui.UIScreenDef.UNABLE_TO_READ_RECORD;

import com.linkly.libengine.action.IAction;
import com.linkly.libengine.dependencies.IDependency;
import com.linkly.libengine.engine.transactions.TransRec;
import com.linkly.libengine.engine.transactions.TransRecManager;
import com.linkly.libengine.printing.IPrintManager;
import com.linkly.libengine.printing.IReceipt;
import com.linkly.libmal.IMal;
import com.linkly.libmal.MalFactory;
import com.linkly.libmal.global.printing.PrintReceipt;
import com.linkly.libmal.global.util.Util;
import com.linkly.libui.IUIDisplay;
import com.linkly.libui.UIScreenDef;

import java.util.HashMap;

import timber.log.Timber;

public class PreAuthSearchAndView extends IAction {
    @Override
    public String getName() {
        return "PreAuthSearchAndView";
    }

    @Override
    public void run() {
        IUIDisplay.UIResultCode res;
        do {
            HashMap<String, Object> dateRangeMap = new HashMap<>();
            dateRangeMap.put(IUIDisplay.uiTitleId, STR_PRE_AUTH_SEARCH_AND_VIEW_SELECT_DATE);
            ui.showInputScreen(DISPLAY_PRE_AUTH_SEARCH_AND_VIEW_SELECT_DATE, dateRangeMap);
            res = ui.getResultCode(ACT_PRE_AUTH_SEARCH_AND_VIEW_SELECT_DATE, IUIDisplay.EXTRA_LONG_TIMEOUT);
            if (res == OK) {
                String selectedDates = ui.getResultText(ACT_PRE_AUTH_SEARCH_AND_VIEW_SELECT_DATE, IUIDisplay.uiResultText1);
                if (!Util.isNullOrEmpty(selectedDates)) {
                    if( selectedDates.equals("RRN")) {
                        // Input RRN
                        String result = inputScreen(ENTER_RRN);
                        if (result.isEmpty())
                            continue;
                        // Update fetch parameters
                        // <start date>,<end date>,<RRN>,<AuthCode>
                        selectedDates = ",,"+result+",";
                    }
                    else if(selectedDates.equals("AUTHCODE")) {
                        // Input Auth Code
                        String result = inputScreen(ENTER_AUTH_CODE);
                        if (result.isEmpty())
                            continue;
                        // Update fetch parameters
                        // <start date>,<end date>,<RRN>,<AuthCode>
                        selectedDates = ",,,"+result;
                    }

                    HashMap<String, Object> preAuthMap = new HashMap<>();
                    preAuthMap.put(IUIDisplay.uiTitleId, STR_PRE_AUTH_SEARCH_AND_VIEW);
                    preAuthMap.put(IUIDisplay.uiResultText1, selectedDates);
                    ui.showInputScreen(DISPLAY_PRE_AUTH_SEARCH_AND_VIEW, preAuthMap);
                    res = ui.getResultCode(ACT_PRE_AUTH_SEARCH_AND_VIEW, 60 * IUIDisplay.ONE_MINUTE_TIMEOUT); // safety timeout - handling user activity timeout in Fragment
                    if (res == OK) {
                        String SelectedRow = ui.getResultText(ACT_PRE_AUTH_SEARCH_AND_VIEW, IUIDisplay.uiResultText1);
                        int uid;
                        try {
                            uid = Integer.parseInt(SelectedRow);
                        } catch (NumberFormatException ex) {
                            Timber.e("format error while parsing uid");
                            ui.showScreen(UNABLE_TO_READ_RECORD);
                            ui.getResultCode(ACT_INFORMATION, IUIDisplay.SHORT_TIMEOUT);
                            continue;
                        }

                        TransRec trans = TransRecManager.getInstance().getTransRecDao().getByUid(uid);
                        if (trans == null) {
                            ui.showScreen(UNABLE_TO_READ_RECORD);
                            ui.getResultCode(ACT_INFORMATION, IUIDisplay.SHORT_TIMEOUT);
                            continue;
                        }

                        print(d, trans, mal);
                    }
                    // "Back" button pressed in List View, do not quit do-while
                    if (res == ABORT) res = OK;
                }
            }
        } while(res == OK);
        ui.displayMainMenuScreen();
    }

    private String inputScreen(UIScreenDef def) {
        HashMap<String, Object> map = new HashMap<>();
        map.put(IUIDisplay.uiTitleId, STR_LIST_PRE_AUTH);
        ui.showInputScreen(def, map);
        IUIDisplay.UIResultCode res = ui.getResultCode(ACT_INPUT, IUIDisplay.LONG_TIMEOUT);
        if (res == OK)
            return ui.getResultText(ACT_INPUT, IUIDisplay.uiResultText1);
        return "";
    }

   private static void print(IDependency d, TransRec t, IMal mal) {
        IReceipt receiptGenerator = d.getPrintManager().getReceiptForTrans(d,t, mal);
        if (receiptGenerator == null) {
            return;
        }

       receiptGenerator.setIsMerchantCopy(true);
       receiptGenerator.setIsCardHolderCopy(false);
       receiptGenerator.setIsDuplicate(true);

       PrintReceipt receiptToPrint = receiptGenerator.generateReceipt(t);

       receiptToPrint.setIconFinished(PR_SUCCESS_ICON);
       receiptToPrint.setIconWhilePrinting(PR_SUCCESS_ICON);
       IPrintManager.PrintPreference printPreference = MalFactory.getInstance().getHardware().hasPrinter() ? PRINT_PREFERENCE_BOTH : PRINT_PREFERENCE_SCREEN;
       d.getPrintManager().printReceipt(d, receiptToPrint, "", true, STR_PRINTING, printPreference, mal);
   }

}
