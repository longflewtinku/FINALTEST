package com.linkly.libengine.action.MenuOperations.admin;

import static com.linkly.libengine.printing.IPrintManager.PrintPreference.PRINT_PREFERENCE_DEFAULT;
import static com.linkly.libengine.printing.IPrintManager.PrintPreference.PRINT_PREFERENCE_SCREEN;
import static com.linkly.libengine.printing.IPrintManager.ReportType.GENERIC_REPORT;
import static com.linkly.libengine.printing.Receipt.getText;
import static com.linkly.libui.IUIDisplay.String_id.STR_CTLS_CONFIG;
import static com.linkly.libui.IUIDisplay.String_id.STR_EMPTY;
import static com.linkly.libui.UIScreenDef.PROCESSING_PLEASE_WAIT_SHORT;

import com.linkly.libengine.action.IAction;
import com.linkly.libengine.printing.IPrintManager;
import com.linkly.libengine.printing.IReceipt;
import com.linkly.libmal.MalFactory;
import com.linkly.libsecapp.CtlsCfg;
import com.linkly.libsecapp.P2PLib;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import timber.log.Timber;

public class PrintCtlsConfig extends IAction {

    @Override
    public String getName() {
        return "PrintCtlsConfig";
    }

    @Override
    public void run() {
        ArrayList<HashMap<String, String>> hashmaps = makeCtlsConfigHashMap();
        if (hashmaps != null) {
            addKernelsVersionsHashMap(hashmaps);
            ui.showScreen(PROCESSING_PLEASE_WAIT_SHORT);
            IReceipt receipt = d.getPrintManager().getReceiptForReport(d, GENERIC_REPORT, mal);
            IPrintManager.PrintPreference printPreference = MalFactory.getInstance().getHardware().hasPrinter() ? PRINT_PREFERENCE_DEFAULT : PRINT_PREFERENCE_SCREEN;
            d.getPrintManager().printReceipt(d, receipt.generateReceipt(hashmaps, new ArrayList<>(), false, getText(STR_CTLS_CONFIG)), null, false, STR_EMPTY, printPreference, mal);
        }
        ui.displayMainMenuScreen();
    }

    private ArrayList<HashMap<String, String>> makeCtlsConfigHashMap() {
        CtlsCfg ctlsCfg = d.getConfig().getCtlsCfg();
        ArrayList<String> blacklistKeys = new ArrayList<String>();
        blacklistKeys.add("capks");
        try {
            //add section titles corresponding to the object
            HashMap<String, String> sectionTitleMap = new HashMap<>();
            sectionTitleMap.put("aids", "schemeLabel");
            JSONObject emvConfigJSON = new JSONObject(ctlsCfg.parseToString());
            return PrintUtils.hashMapsToPrint(emvConfigJSON, blacklistKeys, sectionTitleMap);
        } catch (JSONException e) {
            Timber.w(e);
        }
        return null;
    }

    private void addKernelsVersionsHashMap(ArrayList<HashMap<String, String>> hm) {
        HashMap<String, String> titleMap = new HashMap<>();
        titleMap.put("sectionTitle", "C-less Kernel Versions");
        hm.add(titleMap);

        String kernelVersions = d.getP2PLib().getIP2PCtls().getSecAppCtlsKernelsVersions();

        try {
            JSONArray jsonArray = new JSONArray(kernelVersions);
            for (int i = 0; i < jsonArray.length(); i++) {
                HashMap<String, String> versionMap = new HashMap<>();
                versionMap.put( jsonArray.getJSONObject(i).getString("name"), jsonArray.getJSONObject(i).getString("ver"));
                hm.add(versionMap);
            }
        } catch (JSONException e) {
            Timber.w(e);
        }
    }

}
