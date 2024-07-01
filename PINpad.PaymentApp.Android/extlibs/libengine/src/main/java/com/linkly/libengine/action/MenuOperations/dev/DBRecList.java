package com.linkly.libengine.action.MenuOperations.dev;


import static com.linkly.libengine.engine.reporting.ReconciliationManager.reconciliationDao;
import static com.linkly.libui.IUIDisplay.ACTIVITY_ID.ACT_TABLE;
import static com.linkly.libui.IUIDisplay.UIResultCode.OK;
import static com.linkly.libui.UIScreenDef.DATABASE_EMPTY;
import static com.linkly.libui.UIScreenDef.RECONCILIATION_LIST;
import static com.linkly.libui.display.DisplayTableItem.LEFT_ALIGN;
import static com.linkly.libui.display.DisplayTableItem.TEXT_BOLD;
import static com.linkly.libui.display.DisplayTableItem.TEXT_NORMAL;

import com.linkly.libengine.action.IAction;
import com.linkly.libengine.engine.reporting.Reconciliation;
import com.linkly.libengine.engine.reporting.ReconciliationManager;
import com.linkly.libui.IUIDisplay;
import com.linkly.libui.IUIDisplay.String_id;
import com.linkly.libui.display.DisplayTableArray;
import com.linkly.libui.display.DisplayTableDivider;
import com.linkly.libui.display.DisplayTableItem;
import com.linkly.libui.display.DisplayTableRow;

import java.util.HashMap;
import java.util.List;

public class DBRecList extends IAction {

    @Override
    public String getName() {
        return "DBRecList";
    }

    @Override
    public void run() {
        HashMap<String, Object> map = new HashMap<>();
        DisplayTableArray config = new DisplayTableArray();

        // retrieve all records
        ReconciliationManager.getInstance();
        List<Reconciliation> allTrans = reconciliationDao.getAll();

        if (allTrans == null) {
            ui.showScreen(DATABASE_EMPTY);
            return;
        }

        DisplayTableRow row = new DisplayTableRow();
        row.getItems().add(new DisplayTableItem(String_id.STR_RECONCILIATION_BR_VAR, LEFT_ALIGN, TEXT_BOLD, Integer.toString(allTrans.size())));
        row.getItems().add(new DisplayTableDivider());
        config.getRows().add(row);

        for (Reconciliation tran : allTrans) {
            row = new DisplayTableRow();
            row.getItems().add(new DisplayTableItem( String_id.STR_TRANS_ID_VAR, LEFT_ALIGN, TEXT_NORMAL,Integer.toString(tran.getTransID())));
            row.getItems().add(new DisplayTableItem( String_id.STR_BATCH_NUMBER_VAR, LEFT_ALIGN, TEXT_NORMAL,Integer.toString(tran.getBatchNumber())));
            row.getItems().add(new DisplayTableItem( String_id.STR_START_TRAN_VAR, LEFT_ALIGN, TEXT_NORMAL,Long.toString(tran.getStartTran())));
            row.getItems().add(new DisplayTableItem( String_id.STR_END_TRAN_VAR, LEFT_ALIGN, TEXT_NORMAL,Long.toString(tran.getEndTran())));
            row.getItems().add(new DisplayTableItem( String_id.STR_TOTAL_AMOUNT_VAR, LEFT_ALIGN, TEXT_NORMAL,Long.toString(tran.getTotalAmount())));
            row.getItems().add(new DisplayTableItem( String_id.STR_TOTAL_COUNT_VAR, LEFT_ALIGN, TEXT_NORMAL,Long.toString(tran.getTotalCount())));

            //TODO Add more Details that you want to see here

            config.getRows().add(row);
        }


        map.put(IUIDisplay.uiScreenTableData, config);

        /*Request to Display the Screen*/
        ui.showScreen(RECONCILIATION_LIST, map);

        IUIDisplay.UIResultCode res = ui.getResultCode(ACT_TABLE, IUIDisplay.MAX_TIMEOUT);


        if (res == OK) {
            String SelectedRow = ui.getResultText(ACT_TABLE, IUIDisplay.uiResultText1);
            Integer index = Integer.parseInt(SelectedRow);
            Reconciliation selected = allTrans.get(index);

            selected.debug();


        }
    }

}
