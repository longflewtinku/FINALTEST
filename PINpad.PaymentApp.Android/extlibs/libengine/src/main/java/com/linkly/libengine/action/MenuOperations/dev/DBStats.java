package com.linkly.libengine.action.MenuOperations.dev;


import static com.linkly.libui.IUIDisplay.String_id.STR_DATABASE_COUNTS;
import static com.linkly.libui.IUIDisplay.String_id.STR_TOTAL_RECORDS;
import static com.linkly.libui.IUIDisplay.String_id.STR_TOTAL_REFUNDS;
import static com.linkly.libui.IUIDisplay.String_id.STR_TOTAL_REFUND_REVERSALS;
import static com.linkly.libui.IUIDisplay.String_id.STR_TOTAL_SALES;
import static com.linkly.libui.IUIDisplay.String_id.STR_TOTAL_SALES_REVERSALS;
import static com.linkly.libui.UIScreenDef.DATABASE_STATS;
import static com.linkly.libui.display.DisplayTableItem.LEFT_ALIGN;
import static com.linkly.libui.display.DisplayTableItem.RIGHT_ALIGN;
import static com.linkly.libui.display.DisplayTableItem.TEXT_BOLD;
import static com.linkly.libui.display.DisplayTableItem.TEXT_NORMAL;

import com.linkly.libengine.action.IAction;
import com.linkly.libengine.engine.EngineManager;
import com.linkly.libengine.engine.transactions.TransRec;
import com.linkly.libengine.engine.transactions.TransRecManager;
import com.linkly.libui.IUIDisplay;
import com.linkly.libui.display.DisplayTableArray;
import com.linkly.libui.display.DisplayTableDivider;
import com.linkly.libui.display.DisplayTableItem;
import com.linkly.libui.display.DisplayTableRow;

import java.util.HashMap;
import java.util.List;

public class DBStats extends IAction {

    @Override
    public String getName() {
        return "DBStats";
    }

    @Override
    public void run() {
        HashMap<String,Object> map = new HashMap<>();

        // retrieve all records
        List<TransRec> allTrans = TransRecManager.getInstance().getTransRecDao().findAll();

        if (allTrans == null)
            return;

        Integer totalRecs = allTrans.size();
        Integer totSale = 0;
        Integer totSaleRev = 0;
        Integer totRefund = 0;
        Integer totRefundRev = 0;


        if (allTrans != null) {
            for (TransRec tran : allTrans) {
                if (tran.getTransType() == EngineManager.TransType.SALE) {
                    totSale++;
                }
                if (tran.getTransType() == EngineManager.TransType.REFUND) {
                    totRefund++;
                }
                //TODO add lots of Fun logic here to add more stuff to the table.
            }
        }


        /*Configure the Screen*/
        DisplayTableArray config = new DisplayTableArray();

        DisplayTableRow row = new DisplayTableRow();
        row.getItems().add(new DisplayTableItem(STR_DATABASE_COUNTS, LEFT_ALIGN, TEXT_BOLD));
        row.getItems().add(new DisplayTableDivider());
        config.getRows().add(row);

        row = new DisplayTableRow();
        row.getItems().add(new DisplayTableItem(d.getPrompt(STR_TOTAL_RECORDS) + ": ", LEFT_ALIGN, TEXT_BOLD));
        row.getItems().add(new DisplayTableItem("" + totalRecs, RIGHT_ALIGN, TEXT_NORMAL));
        config.getRows().add(row);

        if (totSale > 0) {
            row = new DisplayTableRow();
            row.getItems().add(new DisplayTableItem(d.getPrompt(STR_TOTAL_SALES) + ": ", LEFT_ALIGN, TEXT_BOLD));
            row.getItems().add(new DisplayTableItem("" + totSale, RIGHT_ALIGN, TEXT_NORMAL));
            config.getRows().add(row);
        }

        if (totSaleRev > 0) {
            row = new DisplayTableRow();
            row.getItems().add(new DisplayTableItem( d.getPrompt(STR_TOTAL_SALES_REVERSALS) + ": ", LEFT_ALIGN, TEXT_BOLD));
            row.getItems().add(new DisplayTableItem("" + totSaleRev, RIGHT_ALIGN, TEXT_NORMAL));
            config.getRows().add(row);
        }

        if (totRefund > 0) {
            row = new DisplayTableRow();
            row.getItems().add(new DisplayTableItem(d.getPrompt(STR_TOTAL_REFUNDS) + ": ", LEFT_ALIGN, TEXT_BOLD));
            row.getItems().add(new DisplayTableItem("" + totRefund, RIGHT_ALIGN, TEXT_NORMAL));
            config.getRows().add(row);
        }

        if (totRefundRev > 0) {
            row = new DisplayTableRow();
            row.getItems().add(new DisplayTableItem(d.getPrompt(STR_TOTAL_REFUND_REVERSALS) + ": ", LEFT_ALIGN, TEXT_BOLD));
            row.getItems().add(new DisplayTableItem("" + totRefundRev, RIGHT_ALIGN, TEXT_NORMAL));
            config.getRows().add(row);
        }

        map.put(IUIDisplay.uiScreenTableData, config);
        map.put(IUIDisplay.uiEnableBackButton, true);

        /*Request to Display the Screen*/
        ui.showScreen(DATABASE_STATS, map);
    }
}
