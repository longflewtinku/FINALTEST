package com.linkly.libengine.action.user_action;

import static com.linkly.libengine.config.paycfg.ReferenceRequired.DISABLED;
import static com.linkly.libsecapp.IP2PActivity.EXTRA_OUT_AMOUNT;
import static com.linkly.libsecapp.IP2PActivity.EXTRA_OUT_HOUSE_NUMBER;
import static com.linkly.libsecapp.IP2PActivity.EXTRA_OUT_IS_MAIL_ORDER;
import static com.linkly.libsecapp.IP2PActivity.EXTRA_OUT_POSTCODE;
import static com.linkly.libsecapp.IP2PActivity.EXTRA_OUT_REFERENCE;
import static com.linkly.libsecapp.IP2PEncrypt.ElementType.CVV;
import static com.linkly.libsecapp.IP2PEncrypt.ElementType.EXPIRY_YYMM;
import static com.linkly.libsecapp.IP2PEncrypt.ElementType.PAN;

import android.os.Bundle;

import com.linkly.libengine.action.IAction;
import com.linkly.libengine.action.TransactionCanceller;
import com.linkly.libengine.config.PayCfg;
import com.linkly.libengine.dependencies.IDependency;
import com.linkly.libengine.engine.transactions.TransRec;
import com.linkly.libengine.engine.transactions.properties.TCard;
import com.linkly.libmal.global.util.Util;
import com.linkly.libsecapp.IP2PEncrypt;
import com.linkly.libsecapp.IP2PLib;
import com.linkly.libsecapp.IP2PSec;
import com.linkly.libsecapp.P2PLib;
import com.linkly.libui.IUIDisplay;

import timber.log.Timber;

public class DisplayCNP extends IAction {

    @Override
    public String getName() {
        return "DisplayCNP";
    }

    @Override
    public void run() {

        IP2PLib p2pInstance = d.getP2PLib();
        IP2PEncrypt p2pEncrypt = p2pInstance.getIP2PEncrypt();

        PayCfg payCfg = d.getPayCfg();

        boolean isCutDown = !d.getCustomer().supportAvs();
        boolean isPhoneAllowed = payCfg.isTelephone();
        boolean isMailAllowed = payCfg.isMailOrder();
        boolean isReferenceMandatory = payCfg.isReferenceMandatory(getRefRequiredSetting(d));
        boolean isRefundSoHideCsc = (trans.isRefund() && !d.getCustomer().supportCscForRefund());
        long initialAmount = trans.getAmounts().getTotalAmount();
        String initialReference = trans.getAudit().getReference();
        Bundle extras = new Bundle();

        ui.cancelScreenSaver();

        p2pEncrypt.clearData();
        if(p2pInstance.getIP2PActivity().getCardNotPresent2(isRefundSoHideCsc, isCutDown, isPhoneAllowed, isMailAllowed,
                isReferenceMandatory, initialAmount, initialReference, IUIDisplay.CARD_GET_TIMEOUT, payCfg.getCountryNum(), extras)) {
            TCard cardInfo = trans.getCard();

            //Get card fields from secure module
            String maskedPan = p2pEncrypt.getData(PAN);
            String expiry    = p2pEncrypt.getData(EXPIRY_YYMM);
            String maskedCsc = p2pEncrypt.getData(CVV);

            //Extract additional fields from extras
            boolean mailOrder   = extras.getBoolean(EXTRA_OUT_IS_MAIL_ORDER, false);
            long    amount      = extras.getLong   (EXTRA_OUT_AMOUNT,        0);
            String  houseNumber = extras.getString (EXTRA_OUT_HOUSE_NUMBER,  "");
            String  postcode    = extras.getString (EXTRA_OUT_POSTCODE,      "");
            String  reference   = extras.getString (EXTRA_OUT_REFERENCE,     "");

            //Apply values to transaction
            if (d.getP2PLib().getIP2PSec().getInstalledKeyType() == IP2PSec.InstalledKeyType.DUKPT) {
                d.getP2PLib().getIP2PSec().incDUKPTKsn(IP2PSec.KeyGroup.TRANS_GROUP);
            }

            //PAN
            cardInfo.setPan(maskedPan);
            cardInfo.setMaskedPan(trans.getMaskedPan(TransRec.MaskType.MERCH_MASK, d.getPayCfg()));
            //Expiry
            cardInfo.setExpiry(expiry);
            Timber.i("EXPIRY=%s", expiry);
            //CVV/CSC
            trans.getCard().setCvv(maskedCsc);

            if (mailOrder) {
                trans.getCard().setMailOrder(true);
            } else {
                trans.getCard().setOverTelephone(true);
            }

            trans.getCard().setHouseNumber(houseNumber);
            trans.getCard().setPostCodeNumber(postcode);

            if(Util.isNullOrEmpty(reference)) {
                trans.getAudit().setSkipReference(true);
            } else {
                trans.getAudit().setReference(reference);
            }

            trans.getCard().setCardholderPresent(false);

            // as the Tip amount is included in the total amount we send to the front end
            if (trans.getAmounts().getTip() > 0)
                amount = amount - trans.getAmounts().getTip();
            trans.getAmounts().setAmount(amount);

            trans.updateCardStateToMatchType(TCard.CardType.MANUAL);
        } else {
            d.getWorkflowEngine().setNextAction(TransactionCanceller.class);
        }

        ui.resetScreenSaver();
    }

    public static int getRefRequiredSetting(IDependency d) {
        return DISABLED.getReferenceCode();
    }
}
