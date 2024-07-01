package com.linkly.libengine.action.MenuOperations.admin;

import static com.linkly.libengine.printing.IPrintManager.PrintPreference.PRINT_PREFERENCE_DEFAULT;
import static com.linkly.libengine.printing.IPrintManager.PrintPreference.PRINT_PREFERENCE_SCREEN;
import static com.linkly.libengine.printing.IPrintManager.ReportType.GENERIC_REPORT;
import static com.linkly.libengine.printing.Receipt.getText;
import static com.linkly.libui.IUIDisplay.String_id.STR_EMPTY;
import static com.linkly.libui.IUIDisplay.String_id.STR_EMV_CONFIG;
import static com.linkly.libui.UIScreenDef.PROCESSING_PLEASE_WAIT_SHORT;

import com.linkly.libengine.action.IAction;
import com.linkly.libengine.printing.IPrintManager;
import com.linkly.libengine.printing.IReceipt;
import com.linkly.libmal.MalFactory;
import com.linkly.libsecapp.EmvCfg;
import com.linkly.libsecapp.P2PLib;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import timber.log.Timber;

public class PrintEmvConfig extends IAction {

    @Override
    public String getName() {
        return "PrintEmvConfig";
    }

    @Override
    public void run() {
        ArrayList<HashMap<String, String>> hashmaps = makeEmvConfigHashMap();
        if (hashmaps != null) {
            addKernelVersionHashMap(hashmaps);
            ui.showScreen(PROCESSING_PLEASE_WAIT_SHORT);
            IReceipt receipt = d.getPrintManager().getReceiptForReport(d, GENERIC_REPORT, mal);
            IPrintManager.PrintPreference printPreference = MalFactory.getInstance().getHardware().hasPrinter() ? PRINT_PREFERENCE_DEFAULT : PRINT_PREFERENCE_SCREEN;
            ArrayList<String> blacklistKeys = new ArrayList<>();
            d.getPrintManager().printReceipt(d, receipt.generateReceipt(hashmaps, blacklistKeys, false, getText(STR_EMV_CONFIG)), null, false, STR_EMPTY, printPreference, mal);
        }
        ui.displayMainMenuScreen();
    }

    private ArrayList<HashMap<String, String>> makeEmvConfigHashMap() {
        EmvCfg emvCfg = d.getConfig().getEmvCfg();
        ArrayList<String> blacklistKeys = new ArrayList<>();
        blacklistKeys.add("capks");
        blacklistKeys.add("aids");
        try {
            HashMap<String, String> sectionTitleMap = new HashMap<>();
            sectionTitleMap.put("schemes", "scheme_label");
            JSONObject emvConfigJSON = new JSONObject(emvCfg.parseToString());
            return PrintUtils.hashMapsToPrint(emvConfigJSON, blacklistKeys, sectionTitleMap);
        } catch (JSONException e) {
            Timber.w(e);
        }
        return null;
    }

    private void addKernelVersionHashMap(ArrayList<HashMap<String, String>> hm) {
        HashMap<String, String> titleMap = new HashMap<>();
        titleMap.put("sectionTitle", "EMV L2 Kernel Version");
        hm.add(titleMap);
        HashMap<String, String> versionMap = new HashMap<>();
        String kernelVersion = d.getP2PLib().getIP2PEmv().getSecAppEMVKernelVersion();
        versionMap.put( "Version", kernelVersion );
        hm.add(versionMap);
    }
}
