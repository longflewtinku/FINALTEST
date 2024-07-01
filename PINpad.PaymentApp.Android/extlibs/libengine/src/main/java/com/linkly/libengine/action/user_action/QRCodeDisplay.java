package com.linkly.libengine.action.user_action;

import static com.linkly.libui.IUIDisplay.ACTIVITY_ID.ACT_QR_SCAN;
import static com.linkly.libui.UIScreenDef.ACT_INFORMATION_SCREEN;
import static com.linkly.libui.UIScreenDef.ACT_QR_CODE_SCREEN;

import android.graphics.Bitmap;

import com.linkly.libengine.action.IAction;
import com.linkly.libmal.global.util.QRCodeGenerator;
import com.linkly.libui.IUIDisplay;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;

public class QRCodeDisplay extends IAction {

    @Override
    public String getName() {
        return "QRCodeDisplay";
    }

    @Override
    public void run() {

        HashMap<String, Object> map = new HashMap<>();

        ui.showScreen(ACT_INFORMATION_SCREEN, map);
        ui.getResultCode(ACT_QR_SCAN, IUIDisplay.IMMEDIATE_TIMEOUT);

        ByteArrayOutputStream bStream = new ByteArrayOutputStream();
        Bitmap qrBitmap = QRCodeGenerator.generateQRCodeBitmap("test Data");
        if(qrBitmap != null) {
            qrBitmap.compress(Bitmap.CompressFormat.PNG, 100, bStream);
            byte[] byteArray = bStream.toByteArray();

            map = new HashMap<>();
            map.put(IUIDisplay.uiQRBitmapData, byteArray);

            ui.showScreen(ACT_QR_CODE_SCREEN, map);
            ui.getResultCode(ACT_QR_SCAN, IUIDisplay.LONG_TIMEOUT);
        }
    }
}
