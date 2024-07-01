package com.linkly.libengine.action.MenuOperations.dev;

import static com.linkly.libengine.engine.transactions.properties.TAudit.ReasonOnlineCode.RTIME_FORCED_CARD_ACCEPTOR;
import static com.linkly.libengine.engine.transactions.properties.TCard.CardType.CTLS;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.ACC_TYPE_CREDIT;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.MessageStatus.FINALISED;
import static com.linkly.libpositive.wrappers.PositiveTransResult.JournalType.NONE;
import static com.linkly.libui.IUIDisplay.ACTIVITY_ID.ACT_INPUT;
import static com.linkly.libui.IUIDisplay.SCREEN_ICON.PROCESSING_ICON;
import static com.linkly.libui.IUIDisplay.UIResultCode.OK;
import static com.linkly.libui.UIScreenDef.ACT_INFORMATION_SCREEN;
import static com.linkly.libui.UIScreenDef.ENTER_NUMBER_TRANSACTIONS;

import com.linkly.libengine.action.IAction;
import com.linkly.libengine.engine.EngineManager;
import com.linkly.libengine.engine.transactions.TransRec;
import com.linkly.libengine.engine.transactions.properties.TCard;
import com.linkly.libpositive.wrappers.PositiveTransResult;
import com.linkly.libui.IUIDisplay;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;

import timber.log.Timber;

public class TestAddTrans extends IAction {

    @Override
    public String getName() {
        return "TestAddTrans";
    }

    @Override
    public void run() {
        int numTrans = uiInputNumTransactions();
        if( numTrans <= 0 ) {
            ui.displayMainMenuScreen();
            return;
        }

        Timber.i( "TestAddTrans adding %d test transactions to database", numTrans );

        HashMap<String, Object> map = new HashMap<>();
        map.put(IUIDisplay.uiScreenIcon, PROCESSING_ICON);
        ui.showScreen(ACT_INFORMATION_SCREEN, map);

        for (int i = 0; i < numTrans; i++){
            TransRec t = new TransRec(EngineManager.TransType.SALE_AUTO, d);
            t.getAmounts().setAmount(i);
            t.getAmounts().setAmountUserEntered(String.format(Locale.getDefault(),"%d",i));

            t.getCard().setCaptureMethod(TCard.CaptureMethod.CTLS);
            t.getCard().setPan("5454545454545454");
            t.getCard().setMaskedPan(t.getMaskedPan(TransRec.MaskType.REPORT_MASK, d.getPayCfg()));
            t.getCard().setExpiry("2103");
            t.getCard().setCardholderPresent(true);
            t.getCard().setFaultyMsr(true);
            t.getCard().setCvmType(TCard.CvmType.ENCIPHERED_ONLINE_PIN);
            t.getCard().setCardType(CTLS);
            t.getCard().setLinklyBinNumber(4);
            t.getCard().setPsn(4);
            t.getCard().setAid("A0000000031010");
            t.getCard().setCryptogram( new byte[]{24,101,6,-74,33,-11,10,30} );
            t.getCard().setCryptogramType(0x80);
            t.getCard().setTsi("0000");
            t.getCard().setTvr("0000000000");

            t.setJournalType(NONE);

            t.getAudit().setReasonOnlineCode(RTIME_FORCED_CARD_ACCEPTOR);
            t.getAudit().setUti( "858b59bd-2e7b-4a7a-b939-ed4a4d653d92" );
            t.getAudit().setReference("DA88FEEEF1444E29");
            t.getAudit().setReceiptNumber(1011);
            t.getProtocol().setRRN("03BCqY003001");

            t.setReceipts(getTestReceipts());

            t.setApproved(true);

            t.getProtocol().setAuthCode( "123456" );
            t.getProtocol().setServerResponseCode( "000" );
            t.getProtocol().setPosResponseText( "APPROVED" );
            t.getProtocol().setBankTime("123456");
            t.getProtocol().setBankDate("221204");
            t.getProtocol().setAccountType(ACC_TYPE_CREDIT);
            t.getProtocol().incAuthCount();
            t.getProtocol().setSettlementDate("221205");

            t.setFinalised(true);
            int index = d.getBinRangesCfg().getCardsCfgIndex(d.getPayCfg(),"5454545454545454");
            t.getCard().setCardIndex(index);
            d.getProtocol().preAuthorize(t);
            t.getProtocol().setMessageStatus(FINALISED);
            t.save();
        }
        ui.displayMainMenuScreen();
    }

    private ArrayList<PositiveTransResult.Receipt> getTestReceipts() {
        ArrayList<PositiveTransResult.Receipt> receipts = new ArrayList<>();
        receipts.add(new PositiveTransResult.Receipt( "M", Arrays.asList("","     Merchant Line 0    ","     Merchant Line 1    ","     Merchant Line 2    ","     Merchant Line 3    ","MERCH ID: 00002547000002","TERM ID:        47000002","BATCH NO:            003","STAN/INV:         000009","RRN:        03BCqY003001","AUTH CODE         720413","CARD: ............2100 T","ASB VISA          CREDIT","PSN: 05        ATC: 0017","TSI: 0000               ","AID:      A0000000031010","ARQC:   186506B621F50A1E","TVR:          0000000000","PURCHASE          $10.00","      ------------      ","TOTAL             $10.00","APPROVED             000","12/10/22 13:33 AEDT     "," ","   **MERCHANT COPY**  ") ));
        receipts.add(new PositiveTransResult.Receipt( "C", Arrays.asList("","     Merchant Line 0    ","     Merchant Line 1    ","     Merchant Line 2    ","     Merchant Line 3    ","MERCH ID: 00002547000002","TERM ID:        47000002","BATCH NO:            003","STAN/INV:         000009","RRN:        03BCqY003001","AUTH CODE         720413","CARD: ............2100 T","ASB VISA          CREDIT","PSN: 05        ATC: 0017","TSI: 0000               ","AID:      A0000000031010","ARQC:   186506B621F50A1E","TVR:          0000000000","PURCHASE          $10.00","      ------------      ","TOTAL             $10.00","APPROVED             000","12/10/22 13:33 AEDT     "," ","  **CARDHOLDER COPY** ") ));
        return receipts;
    }

    private int uiInputNumTransactions() {
        HashMap<String, Object> map = new HashMap<>();
        ui.showInputScreen(ENTER_NUMBER_TRANSACTIONS, map);
        IUIDisplay.UIResultCode res = ui.getResultCode(ACT_INPUT, IUIDisplay.LONG_TIMEOUT);
        if (res == OK) {
            return Integer.parseInt(ui.getResultText(ACT_INPUT, IUIDisplay.uiResultText1));
        } else {
            return 0;
        }
    }
}
