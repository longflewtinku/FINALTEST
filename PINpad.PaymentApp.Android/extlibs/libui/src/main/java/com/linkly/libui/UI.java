package com.linkly.libui;

import static android.app.Notification.DEFAULT_SOUND;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.linkly.libmal.MalFactory;
import com.linkly.libmal.global.util.Util;
import com.linkly.libui.currency.Currency;
import com.linkly.libui.display.Display;
import com.linkly.libui.strings.UIStrings;

public class UI implements IUI {

    protected static UI thisInstance;

    protected static Display display;
    protected static IUIDisplay iUi;

    protected boolean initialisedUI = false;
    protected IUICurrency iCurrency;
    protected IUIStrings IUIStrings;

    // initialises all the components that dont need the Display thread
    public UI() {
        super();
        iCurrency = (IUICurrency) new Currency();
        thisInstance = this;
    }

    static public IUI getInstance() {
        if (thisInstance == null) {
            thisInstance = new UI();
        }
        return thisInstance;
    }



    public IUI initialiseUI(IUICallbacks uiHandler) {
        return initialiseUI(uiHandler, false);
    }
    // initialises all the components that do need the Display thread (or context)
    public IUI initialiseUI(IUICallbacks uiHandler, boolean p2peServer) {

        if (uiHandler != null) {
            // only one instance of the Display as it provides callback ability to call between front end and back end of the app
            if (iUi == null) {
                iUi = (IUIDisplay) new Display(uiHandler);
            }
        }

        if (initialisedUI && thisInstance != null)
            return thisInstance;

        initialisedUI = true;
        return thisInstance;
    }

    public void overrideP2P(IUIDisplay ui) {
        iUi = ui;
    }
    public String getVersion() {
        return "EFT ALIB 1.0";
    }
    public IUIDisplay getUI() {
        return iUi;
    }
    public IUICurrency getCurrency() {
        return iCurrency;
    }

    public IUIStrings getStrings() {
        if (IUIStrings == null) {
            IUIStrings = new UIStrings();
        }
        return IUIStrings;
    }

    public String getPrompt(IUIDisplay.String_id promptId){
        return getStrings().getPrompt(promptId);
    }

    public String getPrompt(IUIDisplay.String_id promptId, String... formatArgs){
        if (formatArgs != null)
            return getStrings().getPromptVA(promptId, formatArgs);
        return getStrings().getPrompt(promptId);
    }

    public void postUINotification(String title, String message) {
        NotificationManager mNotifyManager;
        NotificationCompat.Builder mBuilder;

        mNotifyManager = (NotificationManager) MalFactory.getInstance().getMalContext().getSystemService(Context.NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(MalFactory.getInstance().getMalContext(), "");
        mBuilder.setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(android.R.color.transparent).setDefaults(Notification.DEFAULT_ALL & ~DEFAULT_SOUND);
        if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.N ) {
                    mBuilder.setPriority(NotificationManager.IMPORTANCE_HIGH);
        }
        mNotifyManager.notify(0, mBuilder.build());

        Util.Sleep(10 );
        mNotifyManager.cancel(0);
    }

}
