package com.linkly.libui.currency;

import com.linkly.libui.IUICurrency;

import java.text.NumberFormat;

public class Currency implements IUICurrency {

    private static Currency ourInstance = new Currency();

    public static Currency getInstance() { return ourInstance; }

    public String formatAmount(String lclAmount, IUICurrency.EAmountFormat amtFormat, CountryCode cCode) {
        double lngAmount = 0.0;
        if (lclAmount != null) {
            lngAmount = Double.valueOf(lclAmount);
        }

        boolean bShowSymbol = false;
        boolean bShowCurr = false;

        if (cCode == null) {
            cCode = new CountryCode("", "", "2", "");
        }

        String symbol = cCode.getSymbol();
        String alphaCode = cCode.getAlphaCode();
        NumberFormat currencyFormatter = NumberFormat.getInstance();

        currencyFormatter.setMinimumFractionDigits(cCode.getDecimals());
        //Set Local Flags Based on Format Requested
        switch(amtFormat) {
            case FMT_AMT_FULL:
                bShowSymbol = true;
                bShowCurr = true;
                break;
            case FMT_AMT_SHOW_SYMBOL:
                bShowSymbol = true;
                break;
            case FMT_AMT_SHOW_CURR:
                bShowCurr = true;
                break;
            case FMT_AMT_AUTO:
                bShowSymbol = true;
                if(symbol != null && (symbol.length() == 0 || symbol.equals("$"))) {
                    //Override symbol with currency code
                    symbol = alphaCode;
                }
                break;
        }

        //Adjust for the decimal offset
        if (cCode.getDecimals() > 0) {
            lngAmount = lngAmount / Math.pow(10, cCode.getDecimals());
        } else {
            currencyFormatter.setGroupingUsed(false);
            currencyFormatter.setMinimumIntegerDigits(1);
        }

        //Format the Amount
        String formattedAmount = currencyFormatter.format(lngAmount);

        //If set this will add the Currency Code to the End
        if (bShowCurr && alphaCode.length() > 0) {
            formattedAmount += " " + alphaCode;
        }

        if (bShowSymbol && symbol != null) {
            formattedAmount = (lngAmount >= 0 ? symbol + formattedAmount : "-" + symbol + currencyFormatter.format(lngAmount * -1));
        }

        return formattedAmount;
    }

    public String formatUIAmount(String lclAmount, IUICurrency.EAmountFormat amtFormat, CountryCode cCode) {

        return formatUIAmount(lclAmount, amtFormat, cCode, false);
    }

    public String formatUIAmount(String lclAmount, IUICurrency.EAmountFormat amtFormat, CountryCode cCode, boolean keepMinorDigits) {

        /* special case for Iceland UI */
        if (cCode.getAlphaCode().contains("ISK")) {
            cCode = new CountryCode(cCode); /* i have done this so we arent changing the standard IsoCountryCodes map for everyone */
            cCode.setDecimals(0);

            if (!keepMinorDigits) {
                int value = Integer.valueOf(lclAmount);
                int newAmount = (value / 100);
                lclAmount = String.valueOf(newAmount);
            }
        }
        return formatAmount(lclAmount, amtFormat, cCode);
    }

    public String formatUIAmountResponse(String lclAmount, CountryCode cCode) {
        if (cCode.getAlphaCode().contains("ISK")) {
            lclAmount = lclAmount + "00";
        }
        return lclAmount;
    }

}
