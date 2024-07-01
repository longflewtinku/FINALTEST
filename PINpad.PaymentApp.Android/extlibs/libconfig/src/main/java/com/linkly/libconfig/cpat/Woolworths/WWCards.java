package com.linkly.libconfig.cpat.Woolworths;

import androidx.annotation.Keep;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * MW: It doesn't look like it is possible to test XML string parsing as it relies on XMLPullParserFactory
 * Which is in Android.jar file
 * */
@Keep
public class WWCards {
    public String getVersion() {
        return this.version;
    }

    public List<Entry> getEntryList() {
        return this.entryList;
    }

    @Keep
    public static class Entry{
        /**
         * Woolworths Index
         * This is the one which is present in {@link WoolworthsCPATEntry#getCARD_NAME_INDEX()}
         * */
        @SerializedName("INDEX")
        private String index;
        /**
         * Linkly Bin Index
         * This is the index which we send to the POS.
         * */
        @SerializedName("PCEFTBIN")
        private String linklyBinNumber;
        /**
         * App Name
         * Not used currently
         * */
        @SerializedName("DESCR")
        private String appName;

        public String getIndex() {
            return this.index;
        }

        public String getLinklyBinNumber() {
            return this.linklyBinNumber;
        }

        public String getAppName() {
            return this.appName;
        }

        // Useful for testing etc...
        public Entry(String idx, String binNumber, String description) {
            index = idx;
            linklyBinNumber = binNumber;
            appName = description;
        }
    }

    /**
     * Version number of the file
     * Not used
     * */
    @SerializedName("VER")
    private String version;

    /**
     * List of Entries present
     * */
    @SerializedName("ENTRY")
    private List<Entry> entryList;
}
