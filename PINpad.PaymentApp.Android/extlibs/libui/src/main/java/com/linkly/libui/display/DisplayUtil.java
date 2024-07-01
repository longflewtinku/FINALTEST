package com.linkly.libui.display;


import static com.linkly.libui.IUIDisplay.String_id.STR_APRIL;
import static com.linkly.libui.IUIDisplay.String_id.STR_AUGUST;
import static com.linkly.libui.IUIDisplay.String_id.STR_DECEMBER;
import static com.linkly.libui.IUIDisplay.String_id.STR_FEBRUARY;
import static com.linkly.libui.IUIDisplay.String_id.STR_FRIDAY;
import static com.linkly.libui.IUIDisplay.String_id.STR_JANUARY;
import static com.linkly.libui.IUIDisplay.String_id.STR_JULY;
import static com.linkly.libui.IUIDisplay.String_id.STR_JUNE;
import static com.linkly.libui.IUIDisplay.String_id.STR_MARCH;
import static com.linkly.libui.IUIDisplay.String_id.STR_MAY;
import static com.linkly.libui.IUIDisplay.String_id.STR_MONDAY;
import static com.linkly.libui.IUIDisplay.String_id.STR_NOVEMBER;
import static com.linkly.libui.IUIDisplay.String_id.STR_OCTOBER;
import static com.linkly.libui.IUIDisplay.String_id.STR_SATURDAY;
import static com.linkly.libui.IUIDisplay.String_id.STR_SEPTEMBER;
import static com.linkly.libui.IUIDisplay.String_id.STR_SUNDAY;
import static com.linkly.libui.IUIDisplay.String_id.STR_THURSDAY;
import static com.linkly.libui.IUIDisplay.String_id.STR_TUESDAY;
import static com.linkly.libui.IUIDisplay.String_id.STR_WEDNESDAY;

import com.linkly.libui.IUIDisplay.String_id;
import com.linkly.libui.UI;

import java.util.Calendar;
import java.util.Locale;

public class DisplayUtil {
    public static String GetSignalLevelString(int level) {

        switch(level) {
            case -1:
                return "N/A";
            case 0:
                return UI.getInstance().getPrompt(String_id.STR_MAL_NO_SIGNAL);
            case 1:
                return UI.getInstance().getPrompt(String_id.STR_MAL_LOW_SIGNAL);
            case 2:
                return UI.getInstance().getPrompt(String_id.STR_MAL_AVERAGE_SIGNAL);
            case 3:
                return UI.getInstance().getPrompt(String_id.STR_MAL_GOOD_SIGNAL);
            default:
                return UI.getInstance().getPrompt(String_id.STR_MAL_EXCELLENT_SIGNAL);
        }
    }

    public static String getCurrentMonth() {
        Calendar sCalendar = Calendar.getInstance();
        String monthLongName = sCalendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault());
        switch (monthLongName.toUpperCase()) {
            case "JANUARY":
                return UI.getInstance().getPrompt(STR_JANUARY);
            case "FEBRUARY":
                return UI.getInstance().getPrompt(STR_FEBRUARY);
            case "MARCH":
                return UI.getInstance().getPrompt(STR_MARCH);
            case "APRIL":
                return UI.getInstance().getPrompt(STR_APRIL);
            case "MAY":
                return UI.getInstance().getPrompt(STR_MAY);
            case "JUNE":
                return UI.getInstance().getPrompt(STR_JUNE);
            case "JULY":
                return UI.getInstance().getPrompt(STR_JULY);
            case "AUGUST":
                return UI.getInstance().getPrompt(STR_AUGUST);
            case "SEPTEMBER":
                return UI.getInstance().getPrompt(STR_SEPTEMBER);
            case "OCTOBER":
                return UI.getInstance().getPrompt(STR_OCTOBER);
            case "NOVEMBER":
                return UI.getInstance().getPrompt(STR_NOVEMBER);
            case "DECEMBER":
                return UI.getInstance().getPrompt(STR_DECEMBER);
                default:
                    return monthLongName;
        }
    }

    public static String getCurrentWeekDay() {
        Calendar sCalendar = Calendar.getInstance();
        String dayLongName = sCalendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault());
        switch (dayLongName.toUpperCase()) {
            case "MONDAY":
                return UI.getInstance().getPrompt(STR_MONDAY);
            case "TUESDAY":
                return UI.getInstance().getPrompt(STR_TUESDAY);
            case "WEDNESDAY":
                return UI.getInstance().getPrompt(STR_WEDNESDAY);
            case "THURSDAY":
                return UI.getInstance().getPrompt(STR_THURSDAY);
            case "FRIDAY":
                return UI.getInstance().getPrompt(STR_FRIDAY);
            case "SATURDAY":
                return UI.getInstance().getPrompt(STR_SATURDAY);
            case "SUNDAY":
                return UI.getInstance().getPrompt(STR_SUNDAY);
                default:
                    return dayLongName;
        }
    }
}
