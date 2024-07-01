package com.linkly.libui.strings;

import android.content.Context;
import android.content.res.Configuration;

import com.linkly.libmal.MalFactory;
import com.linkly.libui.IUIDisplay;
import com.linkly.libui.IUIDisplay.String_id;
import com.linkly.libui.IUIStrings;

import java.util.Locale;

public class UIStrings implements IUIStrings {

    private static UIStrings ourInstance = new UIStrings();
    private boolean initialised = false; /* used so we only configure resources once */
    public static IUIStrings getInstance() {
        return ourInstance;
    }

    public UIStrings() {
    }
    private void Init(Context context) {

        if (!initialised) {
            Configuration config = new Configuration(context.getResources().getConfiguration());
            Locale requestedLocale = MalFactory.getInstance().getHardware().getSystemLocale();
            config.setLocale(requestedLocale);
            initialised = true;
        }
    }
    public String getPrompt(IUIDisplay.String_id promptId) {
        return getLocaleStringResource(promptId.getId(), MalFactory.getInstance().getMalContext());
    }

    @Override
    public String getPromptVA(String_id promptId, String... formatArgs) {
        return getLocaleStringResourceVA(promptId.getId(), MalFactory.getInstance().getMalContext(), formatArgs);
    }

    private String getLocaleStringResource(int resourceId, Context context) {

        Init(context);
        Configuration config = new Configuration(context.getResources().getConfiguration());
        return context.createConfigurationContext(config).getString(resourceId);
    }


    private String getLocaleStringResourceVA(int resourceId, Context context, String... formatArgs) {
        Init(context);
        Configuration config = new Configuration(context.getResources().getConfiguration());

        if (formatArgs != null) {
            return context.createConfigurationContext(config).getString(resourceId, (Object[])formatArgs);
        }
        return "";
    }
}
