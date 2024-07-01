package com.linkly.libui;

import com.linkly.libui.currency.CountryCode;

public interface IUICurrency {

    String formatAmount(String lclAmount, EAmountFormat amtFormat, CountryCode cCode);


    String formatUIAmount(String lclAmount, EAmountFormat amtFormat, CountryCode cCode);/* special case for when UI is different to normal formatting   */
    String formatUIAmount(String lclAmount, EAmountFormat amtFormat, CountryCode cCode, boolean keepMinorDigits); /* special case for when UI is different to normal formatting   */
    String formatUIAmountResponse(String lclAmount, CountryCode cCode); /* special case for when UI is different to normal formatting   */

    public enum EAmountFormat {
        FMT_AMT_FULL,
        FMT_AMT_MIN,
        FMT_AMT_SHOW_SYMBOL,
        FMT_AMT_SHOW_CURR,
        FMT_AMT_AUTO
    }
}
