package com.linkly.libengine.action.MenuOperations.dev;


import static com.linkly.libui.IUICurrency.EAmountFormat.FMT_AMT_FULL;
import static com.linkly.libui.IUIDisplay.ACTIVITY_ID.ACT_TABLE;
import static com.linkly.libui.IUIDisplay.UIResultCode.OK;
import static com.linkly.libui.UIScreenDef.DATABASE_EMPTY;
import static com.linkly.libui.UIScreenDef.TRANSACTION_DETAILS;
import static com.linkly.libui.UIScreenDef.TRANSACTION_LIST;
import static com.linkly.libui.display.DisplayTableItem.LEFT_ALIGN;
import static com.linkly.libui.display.DisplayTableItem.TEXT_BOLD;
import static com.linkly.libui.display.DisplayTableItem.TEXT_NORMAL;

import com.linkly.libengine.action.IAction;
import com.linkly.libengine.engine.transactions.TransRec;
import com.linkly.libengine.engine.transactions.TransRecManager;
import com.linkly.libui.IUIDisplay;
import com.linkly.libui.IUIDisplay.String_id;
import com.linkly.libui.display.DisplayTableArray;
import com.linkly.libui.display.DisplayTableDivider;
import com.linkly.libui.display.DisplayTableItem;
import com.linkly.libui.display.DisplayTableRow;

import java.util.HashMap;
import java.util.List;

public class DBList extends IAction {

    @Override
    public String getName() {
        return "DBList";
    }

    @Override
    public void run() {
        HashMap<String, Object> map = new HashMap<>();
        DisplayTableArray config = new DisplayTableArray();

        // retrieve all records
        List<TransRec> allTrans = TransRecManager.getInstance().getTransRecDao().findAll();

        if (allTrans == null) {
            ui.showScreen(DATABASE_EMPTY);
            return;
        }

        DisplayTableRow row = new DisplayTableRow();
        row.getItems().add(new DisplayTableItem(String_id.STR_TRANSACTION_BR_VAR, LEFT_ALIGN, TEXT_BOLD, Integer.toString(allTrans.size())));
        row.getItems().add(new DisplayTableDivider());
        config.getRows().add(row);

        for (TransRec tran : allTrans) {
            row = new DisplayTableRow();
            row.getItems().add(new DisplayTableItem( String_id.STR_STAN_VAR, LEFT_ALIGN, TEXT_NORMAL,Integer.toString(tran.getProtocol().getStan())));
            row.getItems().add(new DisplayTableItem(String_id.STR_REC_VAR, LEFT_ALIGN, TEXT_NORMAL, Integer.toString(tran.getAudit().getReceiptNumber())));
            row.getItems().add(new DisplayTableItem(tran.getTransType().getDisplayName() + ", ", LEFT_ALIGN, TEXT_NORMAL));
            row.getItems().add(new DisplayTableItem(curr.formatAmount(tran.getAmounts().getTotalAmount() + " ", FMT_AMT_FULL, d.getPayCfg().getCountryCode()), LEFT_ALIGN, TEXT_NORMAL));
            row.getItems().add(new DisplayTableItem("(" + tran.getProtocol().getMessageStatus().displayName + ")", LEFT_ALIGN, TEXT_NORMAL));
            row.getItems().add(new DisplayTableItem(String_id.STR_REVERSALS_VAR, LEFT_ALIGN, TEXT_NORMAL, Integer.toString(tran.getProtocol().getReversalCount())));
            //TODO Add more Details that you want to see here

            config.getRows().add(row);
        }


        map.put(IUIDisplay.uiScreenTableData, config);

        /*Request to Display the Screen*/
        ui.showScreen(TRANSACTION_LIST, map);

        IUIDisplay.UIResultCode res = ui.getResultCode(ACT_TABLE, IUIDisplay.MAX_TIMEOUT);


        if (res == OK) {
            String SelectedRow = ui.getResultText(ACT_TABLE, IUIDisplay.uiResultText1);
            Integer index = Integer.parseInt(SelectedRow);
            TransRec selected = allTrans.get(index);

            selected.debug();

            displayTransDetails(selected);
        }
    }

    private void displayTransDetails(TransRec tran) {
        HashMap<String, Object> map = new HashMap<>();
        DisplayTableArray config = new DisplayTableArray();
        DisplayTableRow row = new DisplayTableRow();

        /*Configure the Screen*/
        row.getItems().add(new DisplayTableItem(String_id.STR_TRANSACTION_BR_VAR, LEFT_ALIGN, TEXT_BOLD, tran.getTransType().getDisplayName()));
        row.getItems().add(new DisplayTableDivider());
        config.getRows().add(row);

        map.put(IUIDisplay.uiScreenTableData, config);
        ui.showScreen(TRANSACTION_DETAILS, map);

        row = new DisplayTableRow();
        row.getItems().add(new DisplayTableItem(String_id.STR_UID_VAR, Integer.toString(tran.getUid())));
        config.getRows().add(row);

        row = new DisplayTableRow();
        row.getItems().add(new DisplayTableItem(String_id.STR_STATE_VAR,(tran.isApproved() ? "TRUE" : "FALSE")));
        config.getRows().add(row);

        row = new DisplayTableRow();
        row.getItems().add(new DisplayTableItem(String_id.STR_MSG_STATUS_VAR,tran.getProtocol().getMessageStatus().displayName));
        config.getRows().add(row);

        row = new DisplayTableRow();
        row.getItems().add(new DisplayTableItem(String_id.STR_APPROVED_VAR,(tran.isApprovedOrDeferred() ? "TRUE" : "FALSE")));
        config.getRows().add(row);

        row = new DisplayTableRow();
        row.getItems().add(new DisplayTableItem(String_id.STR_AMOUNT_VAR, curr.formatAmount(tran.getAmounts().getTotalAmount() + " ", FMT_AMT_FULL, d.getPayCfg().getCountryCode())));
        config.getRows().add(row);

        row = new DisplayTableRow();
        row.getItems().add(new DisplayTableItem(String_id.STR_CASH_BACK_AMOUNT_VAR, curr.formatAmount(tran.getAmounts().getCashbackAmount() + " ", FMT_AMT_FULL, d.getPayCfg().getCountryCode())));
        config.getRows().add(row);

        row = new DisplayTableRow();
        row.getItems().add(new DisplayTableItem(String_id.STR_GRATUITY_VAR, curr.formatAmount(tran.getAmounts().getTip() + " ", FMT_AMT_FULL, d.getPayCfg().getCountryCode())));
        config.getRows().add(row);


        row = new DisplayTableRow();
        row.getItems().add(new DisplayTableItem(String_id.STR_HOST_RESULT_VAR, tran.getProtocol().getHostResult().toString()));
        config.getRows().add(row);

        row = new DisplayTableRow();
        row.getItems().add(new DisplayTableItem(String_id.STR_AUTH_METHOD_VAR, tran.getProtocol().getAuthMethod().toString()));
        config.getRows().add(row);

        row = new DisplayTableRow();
        row.getItems().add(new DisplayTableItem(String_id.STR_STAN_VAR, Integer.toString(tran.getProtocol().getStan())));
        config.getRows().add(row);

        row = new DisplayTableRow();
        row.getItems().add(new DisplayTableItem(String_id.STR_AUTH_CODE_VAR, tran.getProtocol().getAuthCode()));
        config.getRows().add(row);

        row = new DisplayTableRow();
        row.getItems().add(new DisplayTableItem(String_id.STR_RRN_VAR, tran.getProtocol().getRRN()));
        config.getRows().add(row);

        row = new DisplayTableRow();
        row.getItems().add(new DisplayTableItem(String_id.STR_BATCH_VAR, Integer.toString(tran.getProtocol().getBatchNumber())));
        config.getRows().add(row);

        row = new DisplayTableRow();
        row.getItems().add(new DisplayTableItem(String_id.STR_UTI_VAR, tran.getAudit().getUti()));
        config.getRows().add(row);

        row = new DisplayTableRow();
        row.getItems().add(new DisplayTableItem(String_id.STR_SHOW_MESSAGES_FOR_UID_VAR, Integer.toString(tran.getUid())));
        config.getRows().add(row);

        map.put(IUIDisplay.uiScreenTableData, config);
        ui.showScreen(TRANSACTION_DETAILS, map);

        IUIDisplay.UIResultCode res = ui.getResultCode(ACT_TABLE, IUIDisplay.MAX_TIMEOUT);



    }

}
