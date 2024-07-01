package com.linkly.libengine.action.MenuOperations.admin;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import timber.log.Timber;

public class PrintUtils {

    private static final ArrayList<HashMap<String, String>> hashMaps = new ArrayList<>();

    public static ArrayList<HashMap<String, String>> hashMapsToPrint(JSONObject json, ArrayList<String> blacklist, HashMap<String, String> sectionTitleKey) {
        hashMaps.clear();
        makeMap(json, blacklist, sectionTitleKey);
        return hashMaps;
    }

    private static HashMap<String, String> makeMap(JSONObject json,  ArrayList<String> blacklist, HashMap<String, String> sectionTitleKey) {
        HashMap<String, String> emvConfigMap = new HashMap<String, String>();
        ArrayList<String> keys = new ArrayList<>();
        for (Iterator<String> it = json.keys(); it.hasNext();) {
            String key = it.next();
            try {
                if (json.get(key) instanceof JSONObject) {
                    emvConfigMap.putAll(makeMap(json.getJSONObject(key), blacklist, sectionTitleKey));
                } else if (json.get(key) instanceof JSONArray) {
                    keys.add(key);
                } else {
                    emvConfigMap.put(key, json.getString(key));
                }
            } catch (JSONException e) {
                Timber.w(e);
            }
        }
        try {
            hashMaps.add(emvConfigMap);
            for (String key : keys) {
                if (!blacklist.contains(key)) {
                    processItems(json.getJSONArray(key), key, blacklist, sectionTitleKey);
                }
            }
        } catch (JSONException e) {
            Timber.w(e);
        }
        return emvConfigMap;
    }

    private static void processItems(JSONArray jsonArray, String key, ArrayList<String> blacklist, HashMap<String,String> sectionTitleKey) {
        try {
            for (int i = 0; i < jsonArray.length(); i++) {
                HashMap<String, String> titleMap = new HashMap<>();
                String sectionTitle;
                if (sectionTitleKey.get(key) != null) {
                    sectionTitle = jsonArray.getJSONObject(i).optString(sectionTitleKey.get(key));
                } else {
                    sectionTitle = stripPlurality(key) + " " + (i + 1);
                }
                sectionTitle = (sectionTitle != null && sectionTitle.length() > 0) ? sectionTitle : stripPlurality(key) + " " + (i + 1);
                titleMap.put("sectionTitle", sectionTitle);
                hashMaps.add(titleMap);
                makeMap(jsonArray.getJSONObject(i), blacklist, sectionTitleKey);
            }
        } catch (JSONException e) {
            Timber.w(e);
        }
    }

    private static String stripPlurality(String key) {
        if (key.endsWith("s")) {
            return key.substring(0, key.length() - 1);
        } else {
            return key;
        }
    }
}
