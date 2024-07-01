package com.linkly.libbins;

import java.util.List;
import java.util.StringTokenizer;

import timber.log.Timber;

public class BinRanges {

    private static final int BIN_ACCURACY = 8; /* number of digits of bin we check to */

    public String removeMaskedData(String binData) {
        if (!binData.contains("*")) {
            return binData;
        }
        return binData.substring(0, binData.indexOf("*"));
    }

    public String fmt(double d) {
        if (d == (long) d) {
            return String.format("%d", (long) d);
        } else {
            return String.format("%s", d);
        }
    }

    public void debugBinRanges(String cfgName, List<BinRange> binRanges) {
        if (binRanges == null) {
            return;
        }

        for (int i = 0; i < binRanges.size(); i++) {
            BinRange b = binRanges.get(i);
            Timber.i( "CfgName: " + cfgName + " Index: " + b.index + " "  + " RANGE:" + fmt(b.start) + ":" + fmt(b.finish));
        }
    }

    private void addBinRange(List<BinRange> binRanges, int index, String start, String finish, String name) {
        start = start.trim();
        finish = finish.trim();
        start = makeXDigitsExact(start, "0");
        finish = makeXDigitsExact(finish, "9");
        BinRange b = new BinRange();
        b.index = index;
        b.start = Double.valueOf(start);
        b.finish = Double.valueOf(finish);
        b.value = name;
        binRanges.add(b);
    }

    public void addIinRange(List<BinRange> binRanges, int index, String iinRange, String name) {

        if( iinRange != null ) {
            StringTokenizer st1 = new StringTokenizer( iinRange, "," );
            while ( st1.hasMoreElements() ) {
                String individualRange = ( String ) st1.nextElement();

                StringTokenizer st2 = new StringTokenizer( individualRange, "-" );
                if ( st2.countTokens() == 2 ) {
                    String start = ( String ) st2.nextElement();
                    String finish = ( String ) st2.nextElement();
                    addBinRange( binRanges, index, start, finish, name );

                } else if ( st2.countTokens() == 1 ) {
                    String start = ( String ) st2.nextElement();
                    String finish = start + "9";
                    start = start + "0";
                    addBinRange( binRanges, index, start, finish, name );
                }
            }
        }
    }

    public String makeXDigitsExact(String s, String c) {

        if (s.length() > BIN_ACCURACY) {
            s = s.substring(0, BIN_ACCURACY);
        }

        while (s.length() < BIN_ACCURACY) {
            s += c;
        }
        return s;
    }

    public int getBinAccuracy() {
        return BIN_ACCURACY;
    }

    public static class BinRange {
        public int index;
        public double start;
        public double finish;
        public String value;

        public int getIndex() {
            return this.index;
        }

        public double getStart() {
            return this.start;
        }

        public double getFinish() {
            return this.finish;
        }

        public String getValue() {
            return this.value;
        }
    }
}

