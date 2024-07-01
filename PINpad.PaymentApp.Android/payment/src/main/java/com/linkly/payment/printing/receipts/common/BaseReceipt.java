package com.linkly.payment.printing.receipts.common;

import static com.linkly.libengine.engine.transactions.properties.TProtocol.ACC_TYPE_CHEQUE;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.ACC_TYPE_CREDIT;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.ACC_TYPE_SAVINGS;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.AuthMethod.NOT_SET;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.HostResult.CONNECT_FAILED;
import static com.linkly.libmal.global.printing.PrintReceipt.FONT.FONT_FIXED_WIDTH_POS;
import static com.linkly.libsecapp.emv.Tag.appl_cryptogram;
import static com.linkly.libsecapp.emv.Tag.appl_cryptogram_genAc2;
import static com.linkly.libsecapp.emv.Tag.auth_resp_code;
import static com.linkly.libsecapp.emv.Tag.crypt_info_data;
import static com.linkly.libsecapp.emv.Tag.crypt_info_data_genAc2;
import static com.linkly.libsecapp.emv.Tag.tvr;
import static com.linkly.libui.IUIDisplay.String_id.STR_AAC_LABEL;
import static com.linkly.libui.IUIDisplay.String_id.STR_ARQC_LABEL;
import static com.linkly.libui.IUIDisplay.String_id.STR_AUTH_CODE;
import static com.linkly.libui.IUIDisplay.String_id.STR_BATCH_NO;
import static com.linkly.libui.IUIDisplay.String_id.STR_CARDHOLDER_SIGN;
import static com.linkly.libui.IUIDisplay.String_id.STR_EXPIRES;
import static com.linkly.libui.IUIDisplay.String_id.STR_MAIL_ORDER;
import static com.linkly.libui.IUIDisplay.String_id.STR_PRE_AUTH;
import static com.linkly.libui.IUIDisplay.String_id.STR_RRN;
import static com.linkly.libui.IUIDisplay.String_id.STR_STAN_INV;
import static com.linkly.libui.IUIDisplay.String_id.STR_TC_LABEL;
import static com.linkly.libui.IUIDisplay.String_id.STR_TELEPHONE;
import static com.linkly.libui.IUIDisplay.String_id.STR_VERIFIED_BY_PIN;
import static com.linkly.libui.IUIDisplay.String_id.STR_WOW_AID;
import static com.linkly.libui.IUIDisplay.String_id.STR_WOW_APPROVED;
import static com.linkly.libui.IUIDisplay.String_id.STR_WOW_CANCELLED;
import static com.linkly.libui.IUIDisplay.String_id.STR_WOW_CARD;
import static com.linkly.libui.IUIDisplay.String_id.STR_WOW_CASHOUT;
import static com.linkly.libui.IUIDisplay.String_id.STR_WOW_CHEQUE;
import static com.linkly.libui.IUIDisplay.String_id.STR_WOW_COMPLETION;
import static com.linkly.libui.IUIDisplay.String_id.STR_WOW_CREDIT;
import static com.linkly.libui.IUIDisplay.String_id.STR_WOW_DECLINED;
import static com.linkly.libui.IUIDisplay.String_id.STR_WOW_DEPOSIT;
import static com.linkly.libui.IUIDisplay.String_id.STR_WOW_MID;
import static com.linkly.libui.IUIDisplay.String_id.STR_WOW_PREAUTH;
import static com.linkly.libui.IUIDisplay.String_id.STR_WOW_PREAUTH_CANCELLATION;
import static com.linkly.libui.IUIDisplay.String_id.STR_WOW_PURCHASE;
import static com.linkly.libui.IUIDisplay.String_id.STR_WOW_REFUND;
import static com.linkly.libui.IUIDisplay.String_id.STR_WOW_SAVINGS;
import static com.linkly.libui.IUIDisplay.String_id.STR_WOW_SURCHARGE;
import static com.linkly.libui.IUIDisplay.String_id.STR_WOW_TID;
import static com.linkly.libui.IUIDisplay.String_id.STR_WOW_TIP;
import static com.linkly.libui.IUIDisplay.String_id.STR_WOW_TOTAL;
import static com.linkly.libui.IUIDisplay.String_id.STR_WOW_TVR;

import com.linkly.libengine.config.PayCfg;
import com.linkly.libengine.engine.EngineManager;
import com.linkly.libengine.engine.transactions.TransRec;
import com.linkly.libengine.engine.transactions.properties.TCard;
import com.linkly.libengine.engine.transactions.properties.TProtocol;
import com.linkly.libengine.env.BatchNumber;
import com.linkly.libengine.env.IccDiags;
import com.linkly.libengine.printing.Receipt;
import com.linkly.libmal.global.platform.EFTPlatform;
import com.linkly.libmal.global.printing.PrintReceipt;
import com.linkly.libmal.global.util.Util;
import com.linkly.libpositive.wrappers.TagDataFromPOS;
import com.linkly.libui.IUICurrency;
import com.linkly.libui.IUIDisplay;
import com.pax.market.android.app.sdk.util.StringUtils;

import java.util.Arrays;
import java.util.Date;

import timber.log.Timber;

public class BaseReceipt extends Receipt {

    private static final int CARD_ACCEPTOR_NAME_LOCATION_OFFSET = 20;
    private static final int CARD_DESCRIPTION_MAX_LENGTH = 16;
    private static final int ACCOUNT_NAME_MAX_LENGTH = 7;

    @Override
    public PrintReceipt generateReceipt(Object obj ) {
        TransRec trans = ( TransRec ) obj;
        PrintReceipt receipt = new PrintReceipt();

        /* Disclaimer */
        addDisclaimer( receipt );
        addHeader( receipt , trans);
        return receipt;
    }

    /**
     * Print Merchant Lines according to specs
     * @param printReceipt {@link PrintReceipt} object
     * @param line to be printed if it exists
     * */
    public void addMerchantLine( PrintReceipt printReceipt, String line ){
        if (!Util.isNullOrEmpty(line) ) {
            if (line.length() <= CARD_ACCEPTOR_NAME_LOCATION_OFFSET ) {
                addLineCentered( printReceipt, line, FONT_FIXED_WIDTH_POS );
            }
            else {
                addLineCentered( printReceipt, line.substring(0,CARD_ACCEPTOR_NAME_LOCATION_OFFSET), FONT_FIXED_WIDTH_POS );
                addLineCentered( printReceipt, line.substring(CARD_ACCEPTOR_NAME_LOCATION_OFFSET), FONT_FIXED_WIDTH_POS );
            }
        }
    }

    public void addHeader(PrintReceipt receipt, TransRec trans) {
        PayCfg paycfg = d.getPayCfg();

        super.addLogo( receipt );
        if (paycfg.getReceipt() != null && paycfg.getReceipt().getMerchant() != null) {
            this.addMerchantLine( receipt, paycfg.getReceipt().getMerchant().getLine0() );
            this.addMerchantLine( receipt, paycfg.getReceipt().getMerchant().getLine1() );
            this.addMerchantLine( receipt, paycfg.getReceipt().getMerchant().getLine2() );
            this.addMerchantLine( receipt, paycfg.getReceipt().getMerchant().getLine3() );
            this.addMerchantLine( receipt, paycfg.getReceipt().getMerchant().getLine4() );
            this.addMerchantLine( receipt, paycfg.getReceipt().getMerchant().getLine5() );
        }

        addReversalText(receipt, trans);
        // add Merchant ID
        receipt.getLines().add(new PrintReceipt.PrintTableLine(getText(STR_WOW_MID), d.getPayCfg().getMid(), FONT_FIXED_WIDTH_POS));
        // add Terminal ID
        receipt.getLines().add(new PrintReceipt.PrintTableLine(getText(STR_WOW_TID), d.getPayCfg().getStid(), FONT_FIXED_WIDTH_POS));
    }


    /**
     * Print **VOID** with space on top and bottom for Reversals
     */
    public void addReversalText(PrintReceipt receipt, TransRec transRec) {
        if (EngineManager.TransType.MANUAL_REVERSAL_AUTO.equals(transRec.getTransType())
                || EngineManager.TransType.MANUAL_REVERSAL.equals(transRec.getTransType())) {
            receipt.getLines().add(new PrintReceipt.PrintSpaceLine());
            receipt.getLines().add(new PrintReceipt.PrintLine(
                    getText(IUIDisplay.String_id.STR_RECEIPT_VOID).toUpperCase(),
                    LARGE_FONT,
                    PrintReceipt.PrintLine.TextAlignment.CENTER)
            );
            receipt.getLines().add(new PrintReceipt.PrintSpaceLine());
        }
    }

    public void addReversalDateTime(PrintReceipt receipt, TransRec transRec) {
        if (EngineManager.TransType.MANUAL_REVERSAL_AUTO.equals(transRec.getTransType())
                || EngineManager.TransType.MANUAL_REVERSAL.equals(transRec.getTransType())) {
            Date dateTime = new Date(transRec.getAudit().getReversalDateTime());
            String dateTimeString = formatDateTime(dateTime, "dd/MM/yy HH:mm");

            receipt.getLines().add(new PrintReceipt.PrintTableLine(
                    getText(IUIDisplay.String_id.STR_RECEIPT_VOIDED_AT).toUpperCase(),
                    dateTimeString,
                    FONT_FIXED_WIDTH_POS)
            );
        }
    }
    public static void addBatch(PrintReceipt receipt, TransRec tran) {
        String batch = "";
        if (tran.getProtocol().getBatchNumber() == -1) {
            tran.getProtocol().setBatchNumber(BatchNumber.getCurValue());
        }

        if (!Util.isNullOrWhitespace(Integer.toString(tran.getProtocol().getBatchNumber()))) {
            batch = Integer.toString(tran.getProtocol().getBatchNumber());
        }
        if (!batch.equals("")) {
            receipt.getLines().add(new PrintReceipt.PrintTableLine(
                    getText(STR_BATCH_NO),
                    Util.leftPad("" + batch, 3, '0'),
                    FONT_FIXED_WIDTH_POS)
            );
        }
    }

    public void addStan(PrintReceipt receipt, TransRec tran) {
        String stan = "";

        if ((tran.getTransType() == EngineManager.TransType.COMPLETION) ||
                (tran.getTransType() == EngineManager.TransType.COMPLETION_AUTO)) {
            // Completions are advices, so they don't get a STAN until sent to the host. Omit from receipt.
            return;
        }

        // if offline approved, there may be a STAN allocated (e.g. where NULL stan block is required), but we don't print on the receipt
        // because the actual stan for the advice will be assigned at the time the 0220 is sent
        if(tran.getProtocol().isAuthMethodOfflineApproved()) {
            return;
        }

        // Offline declined transactions may have a STAN allocated, but we don't print on the receipt as this STAN is not used for online host communication
        if (tran.getProtocol().getHostResult() == CONNECT_FAILED && tran.getProtocol().getAuthMethod() == NOT_SET) {
            return;
        }

        if (tran.getProtocol().getOriginalStan() != null) {
            stan = Integer.toString(tran.getProtocol().getOriginalStan());
        } else if (!Util.isNullOrWhitespace(Integer.toString(tran.getProtocol().getStan()))) {
            stan = Integer.toString(tran.getProtocol().getStan());
        }
        if (!stan.equals("") && !stan.equals("0")) {
            receipt.getLines().add(new PrintReceipt.PrintTableLine(
                    getText(STR_STAN_INV),
                    Util.leftPad("" + stan, 6, '0'),
                    FONT_FIXED_WIDTH_POS)
            );
        }
    }

    public void addCardData(PrintReceipt receipt, TransRec trans) {
        addCardLine(receipt, trans);
        addCardExpiry(receipt, trans);
        // Add Card description and account
        String cardName = trans.getCard().getCardName(d.getPayCfg());
        if (cardName.length()> CARD_DESCRIPTION_MAX_LENGTH) {
            cardName = cardName.substring(0,CARD_DESCRIPTION_MAX_LENGTH);
        }
        String accountName = "";
        Integer accountType = trans.getProtocol().getAccountType();
        if (accountType == ACC_TYPE_SAVINGS ) accountName = getText(STR_WOW_SAVINGS);
        else if (accountType == ACC_TYPE_CHEQUE ) accountName = getText(STR_WOW_CHEQUE);
        else if (accountType == ACC_TYPE_CREDIT ) accountName = getText(STR_WOW_CREDIT);

        if (accountName.length()>ACCOUNT_NAME_MAX_LENGTH) {
            accountName = accountName.substring(0,ACCOUNT_NAME_MAX_LENGTH);
        }
        receipt.getLines().add(new PrintReceipt.PrintTableLine(cardName, accountName, FONT_FIXED_WIDTH_POS));

        // Add EMV Data
        addEmvData(receipt, trans);
    }

    private void addCardLine(PrintReceipt receipt, TransRec trans) {
        PrintReceipt.PrintLine line = new PrintReceipt.PrintLine("");
        // Add masked PAN and card type
        String pan4 = "****";
        String panMasked = trans.getMaskedPan( TransRec.MaskType.CUSTOMER_MASK, d.getPayCfg() );
        int panMaskedLen = panMasked.length();
        String panMaskedPart = "....";
        if (panMaskedLen > 4) {
            pan4 = panMasked.substring( panMaskedLen - 4 );
            panMaskedPart = StringUtils.repeat( ".", panMaskedLen-4 ); // print masked digits as dots
        }

        String cardCaptureMethod;
        if (trans.getCard().isCtlsCaptured()) {
            cardCaptureMethod = "T";
        } else if (trans.getCard().isIccCaptured()) {
            cardCaptureMethod = "D";
        } else if (trans.getCard().isManual()) {
            cardCaptureMethod = "K";
        } else if (trans.getCard().isSwiped()) {
            cardCaptureMethod = "S";
        } else if (trans.getCard().isIccFallbackCaptured()) {
            cardCaptureMethod = "S";
        } else {
            cardCaptureMethod = " ";
        }

        // Line format e.g. for fixed length 24 cpl
        // 123456789012345678901234
        // CARD: ............2100 D

        // work out if we can fit the line format above on 1 line or split over 2 if not
        if( getText(STR_WOW_CARD).length() + " ".length() + panMaskedPart.length() + pan4.length() + " ".length() + cardCaptureMethod.length() > line.getCharsPerLine(FONT_FIXED_WIDTH_POS) ) {
            // split over 2
            // first line just as 'card:' on it, left-justified
            receipt.getLines().add(new PrintReceipt.PrintLine(getText(STR_WOW_CARD), FONT_FIXED_WIDTH_POS));
            // second line has card right-justified
            receipt.getLines().add(new PrintReceipt.PrintTableLine("", panMaskedPart + pan4 + " " + cardCaptureMethod, FONT_FIXED_WIDTH_POS));
        } else {
            // fit on one line, with 'card:' left justified and masked PAN and card capture char right justified
            receipt.getLines().add(new PrintReceipt.PrintTableLine(getText(STR_WOW_CARD), panMaskedPart + pan4 + " " + cardCaptureMethod, FONT_FIXED_WIDTH_POS));
        }
    }

    // Add card expiry date for EFB transactions
    private void addCardExpiry(PrintReceipt receipt, TransRec trans) {
        TProtocol.AuthMethod authMethod = trans.getProtocol().getAuthMethod();
        if ( authMethod == TProtocol.AuthMethod.EFB_AUTHORISED || authMethod == TProtocol.AuthMethod.OFFLINE_EFB_AUTHORISED) {
            String cardExpiry = trans.getCard().getExpiry();
            if (!Util.isNullOrEmpty(cardExpiry)) {
                receipt.getLines().add(new PrintReceipt.PrintTableLine(getText(STR_EXPIRES), String.format("%s/%s", cardExpiry.substring(2, 4), cardExpiry.substring(0, 2)), FONT_FIXED_WIDTH_POS));
            }
        }
    }

    protected void addDateTime(PrintReceipt receipt, TransRec tran) {
        Date dateTime = getTranDate(tran);
        String dateTimeString = formatDateTime(dateTime, "dd/MM/yy HH:mm zzz");

        receipt.getLines().add(new PrintReceipt.PrintTableLine(
                dateTimeString,
                "",
                FONT_FIXED_WIDTH_POS)
        );
    }

    public void addAmounts(PrintReceipt receipt, TransRec trans, String procCode) {

        String tranType = getText(STR_WOW_PURCHASE);

        if( trans.isPreAuthCancellation() ) {
            tranType = getText(STR_WOW_PREAUTH_CANCELLATION);
        } else if( trans.isPreAuth() ) {
            tranType = getText(STR_WOW_PREAUTH);
        } else if( trans.isCompletion() ) {
            receipt.getLines().add( new PrintReceipt.PrintLine( getText( STR_PRE_AUTH ).toUpperCase(), FONT_FIXED_WIDTH_POS ) );
            tranType = getText(STR_WOW_COMPLETION);
        } else if (procCode.equals("00")) {
            tranType = getText(STR_WOW_PURCHASE);
        } else if (procCode.equals("01")) {
            tranType = getText(STR_WOW_CASHOUT);
        } else if (procCode.equals("09")) {
            tranType = getText(STR_WOW_PURCHASE);
        } else if (procCode.equals("20")) {
            tranType = getText(STR_WOW_REFUND);
        } else if (procCode.equals("21")) {
            tranType = getText(STR_WOW_DEPOSIT);
        }

        long amountTotal = trans.getAmounts().getTotalAmount(); // includes extras - cash, surcharge, tip etc
        long amountCash = trans.getAmounts().getCashbackAmount();
        long amountSurcharge = trans.getAmounts().getSurcharge();
        long amountTip = trans.getAmounts().getTip();
        long amountBase = trans.getAmounts().getAmount(); // excludes extras

        String amountTotalText = " " + d.getFramework().getCurrency().formatUIAmount(amountTotal + "", IUICurrency.EAmountFormat.FMT_AMT_SHOW_SYMBOL, d.getPayCfg().getCountryCode());
        String amountSurchargeText = " " + d.getFramework().getCurrency().formatUIAmount(amountSurcharge + "", IUICurrency.EAmountFormat.FMT_AMT_SHOW_SYMBOL, d.getPayCfg().getCountryCode());
        String amountTipText = " " + d.getFramework().getCurrency().formatUIAmount(amountTip + "", IUICurrency.EAmountFormat.FMT_AMT_SHOW_SYMBOL, d.getPayCfg().getCountryCode());
        String amountCashText = " " + d.getFramework().getCurrency().formatUIAmount(amountCash + "", IUICurrency.EAmountFormat.FMT_AMT_SHOW_SYMBOL, d.getPayCfg().getCountryCode());
        String amountBaseText = " " + d.getFramework().getCurrency().formatUIAmount(amountBase + "", IUICurrency.EAmountFormat.FMT_AMT_SHOW_SYMBOL, d.getPayCfg().getCountryCode());

        if (amountBase > 0) {
            receipt.getLines().add(new PrintReceipt.PrintTableLine(tranType, amountBaseText, FONT_FIXED_WIDTH_POS));
        }
        if (amountTip > 0) {
            receipt.getLines().add(new PrintReceipt.PrintTableLine(getText(STR_WOW_TIP), amountTipText, FONT_FIXED_WIDTH_POS));
        }
        if (amountCash > 0) {
            receipt.getLines().add(new PrintReceipt.PrintTableLine(getText(STR_WOW_CASHOUT), amountCashText, FONT_FIXED_WIDTH_POS));
        }
        if (amountSurcharge > 0) {
            receipt.getLines().add(new PrintReceipt.PrintTableLine(getText(STR_WOW_SURCHARGE), amountSurchargeText, FONT_FIXED_WIDTH_POS));
        }
        addLineCentered(receipt, "------------", FONT_FIXED_WIDTH_POS);
        receipt.getLines().add(new PrintReceipt.PrintTableLine(getText(STR_WOW_TOTAL), amountTotalText, FONT_FIXED_WIDTH_POS));

    }

    private void addAID( PrintReceipt receipt, TransRec trans ) {
        String aid0 = trans.getCard().getAid();
        String aid1 = "";
        PrintReceipt.PrintLine line = new PrintReceipt.PrintLine("");
        if (!Util.isNullOrEmpty(aid0)) {
            int maxAidPerLine = line.getCharsPerLine(FONT_FIXED_WIDTH_POS) - getText(STR_WOW_AID).length();
            if (aid0.length() > maxAidPerLine)  {
                aid1 = aid0.substring( maxAidPerLine, aid0.length());
                aid0 = aid0.substring(0, maxAidPerLine);
                if (aid1.length() > maxAidPerLine) {
                    aid1 = aid1.substring(0, maxAidPerLine);
                }
            }
            receipt.getLines().add(new PrintReceipt.PrintTableLine(getText(STR_WOW_AID), aid0, FONT_FIXED_WIDTH_POS));
            if (aid1.length()>0) {
                String space = getText(STR_WOW_AID).replace("."," ");
                receipt.getLines().add(new PrintReceipt.PrintLine(space+aid1, FONT_FIXED_WIDTH_POS));
            }
        }
    }

    private String cidToLabel(char cidChar) {
        final String AAC_LABEL = getText(STR_AAC_LABEL);
        final String TC_LABEL = getText(STR_TC_LABEL);
        final String ARQC_LABEL = getText(STR_ARQC_LABEL);
        final char AAC_IDENTIFIER = '0';
        final char TC_IDENTIFIER = '4';
        final char ARQC_IDENTIFIER = '8';

        switch( cidChar ) {
            case AAC_IDENTIFIER:
                return AAC_LABEL;
            case TC_IDENTIFIER:
                return TC_LABEL;
            case ARQC_IDENTIFIER:
                return ARQC_LABEL;
            default:
                Timber.e( "unexpected cidChar %c", cidChar );
                return "";
        }
    }

    private boolean isArqc(char cidChar) {
        final char ARQC_IDENTIFIER = '8';
        return cidChar == ARQC_IDENTIFIER;
    }

    private void addCryptograms( PrintReceipt receipt, TransRec trans ) {
        TCard card = trans.getCard();
        if (card.getTags() != null && !card.getTags().isEmpty()) {
            // add CID and Cryptogram
            String cid = card.getTags().getString(crypt_info_data);

            if (!Util.isNullOrEmpty(cid)) {
                String cidLabel = cidToLabel(cid.charAt(0));

                String cryptogram = card.getTags().getString(appl_cryptogram);
                receipt.getLines().add(new PrintReceipt.PrintTableLine(cidLabel, cryptogram, FONT_FIXED_WIDTH_POS));

                // if 1st Gen AC result was ARQC, then print the 2nd Gen AC CID label and cryptogram
                if( isArqc(cid.charAt(0))) {
                    String cid2 = card.getTags().getString(crypt_info_data_genAc2);
                    String cryptogram2 = card.getTags().getString(appl_cryptogram_genAc2);
                    if (!Util.isNullOrEmpty(cid2)) {
                        String cid2Label = cidToLabel(cid2.charAt(0));
                        receipt.getLines().add(new PrintReceipt.PrintTableLine(cid2Label, cryptogram2, FONT_FIXED_WIDTH_POS));
                    }
                }
            }
        }
    }

    private void addTvr( PrintReceipt receipt, TransRec trans ) {
        TCard card = trans.getCard();
        if (card.getTags() != null && !card.getTags().isEmpty()) {
            // add TVR
            String tvrStr = card.getTags().getString(tvr);
            if( tvrStr != null ) {
                receipt.getLines().add(new PrintReceipt.PrintTableLine(getText(STR_WOW_TVR), tvrStr, FONT_FIXED_WIDTH_POS));
            }
        }
    }

    private void addEmvData( PrintReceipt receipt, TransRec trans ) {
        if (trans.getCard().isCtlsCaptured() || trans.getCard().isIccCaptured()) {
            // pan sequence number (PSN) and application transaction counter (ATC)
            addPsnAndAtc(receipt, trans, FONT_FIXED_WIDTH_POS);
            // transaction status information (TSI) and CDO chip decision override and MCR merchant choice routing flags
            addTsiAndCdoMcrFlags(receipt, trans, FONT_FIXED_WIDTH_POS);
            // AID
            addAID(receipt, trans);
            // one or two lines of cryptograms
            addCryptograms(receipt, trans);
            // one line for TVR
            addTvr(receipt, trans);
        }
    }

    public void addAuthResult(PrintReceipt receipt, TransRec tran) {
        String respLine1;
        String respLine2 = "";
        String serverRC = addServerResponse(tran);
        if (tran.isApproved()) {
            respLine1 = getText(STR_WOW_APPROVED);
        } else if (tran.getProtocol().getHostResult() == TProtocol.HostResult.CONNECT_FAILED || tran.getProtocol().getHostResult() == TProtocol.HostResult.NO_RESPONSE) {
            respLine1 = getText(STR_WOW_CANCELLED);
            respLine2 = tran.getProtocol().getHostResult().displayName.toUpperCase();
        } else if (tran.isCancelled()) {
            respLine1 = tran.getProtocol().getPosResponseText().toUpperCase();
        } else if (!Util.isNullOrEmpty(tran.getProtocol().getCardAcceptorPrinterData())) {
            // print decline reason from as2805 table if set
            respLine1 = tran.getProtocol().getCardAcceptorPrinterData();
        } else {
            respLine1 = getText(STR_WOW_DECLINED);
        }

        String[] split = respLine1.split("\n");
        if (split.length > 1) {
            respLine1 = split[0];
            respLine2 = split[1];
        }

        receipt.getLines().add(new PrintReceipt.PrintTableLine(respLine1, serverRC, FONT_FIXED_WIDTH_POS));
        if (respLine2.length()>0) {
            addLineCentered( receipt, respLine2, FONT_FIXED_WIDTH_POS );
        }
    }

    private String addServerResponse(TransRec tran) {
        String serverResponse = "";
        if ( !Util.isNullOrEmpty(tran.getProtocol().getServerResponseCode())) {
            serverResponse = tran.getProtocol().getServerResponseCode();
        }
        if ((tran.getCard().isCtlsCaptured() || tran.getCard().isIccCaptured()) && tran.getCard().getTags() != null ) {
            byte[] arc = tran.getCard().getTags().getTag(auth_resp_code);
            // only if legit "Y1" - cless kernel (EPAL) might have set ARC=Y1 even if ARQC generated and transaction went online
            if (arc != null && arc.length >= 2 && ((Arrays.equals(arc, new byte[]{'Y', '1'}) && serverResponse.length() == 0) || Arrays.equals(arc, new byte[]{'Y', '3'}))) {
                serverResponse = "" + (char)(arc[0]&0xFF) + (char)(arc[1]&0xFF);
            }
        }
        return serverResponse;
    }

    public void addRRN(PrintReceipt receipt, TransRec tran) {
        String rrn = tran.getProtocol().getRRN();
        if (!Util.isNullOrWhitespace(rrn)) {
            receipt.getLines().add(new PrintReceipt.PrintTableLine(getText(STR_RRN), rrn, FONT_FIXED_WIDTH_POS));
        }
    }

    @Override
    public void addAuthCode(PrintReceipt receipt, TransRec tran){
        String authCode = tran.getProtocol().getAuthCode();
        if (!Util.isNullOrWhitespace(authCode)) {
            receipt.getLines().add(new PrintReceipt.PrintTableLine(getText(STR_AUTH_CODE), authCode, FONT_FIXED_WIDTH_POS));
        }
    }

    public void addMOTOType(PrintReceipt receipt, TransRec tran) {
        if(tran != null && (tran.getCard().isMailOrder() || tran.getCard().isOverTelephone())) {
            receipt.getLines().add(new PrintReceipt.PrintTableLine("MOTO:", getText(tran.getCard().isMailOrder() ? STR_MAIL_ORDER : STR_TELEPHONE)  , FONT_FIXED_WIDTH_POS));
        }
    }

    public void addDateTimeRRN(PrintReceipt receipt, TransRec tran) {
        Date dateTime = getTranDate(tran);
        String dateTimeString = formatDateTime(dateTime, "dd/MM/yy HH:mm ");
        receipt.getLines().add(new PrintReceipt.PrintTableLine(dateTimeString, "", FONT_FIXED_WIDTH_POS));
    }

    public void addVerificationDetails(PrintReceipt receipt, TransRec tran) {

        if (!tran.isApproved()) {
            return;
        }

        if (tran.isSignatureRequired() && isMerchantCopy()) {
            this.addSpaceLine(receipt);
            receipt.getLines().add(new PrintReceipt.PrintLine(getText(STR_CARDHOLDER_SIGN), FONT_FIXED_WIDTH_POS));

            //If print to screen and signature
            if (EFTPlatform.printToScreen()) {

                String filename = "/sig/" + tran.getAudit().getUti() + ".png";
                receipt.getLines().add(new PrintReceipt.PrintImageLine(filename));

            } else {
                this.addSpaceLines(receipt, 5);
                receipt.getLines().add(new PrintReceipt.PrintFillLine('_'));
                this.addSpaceLine(receipt);
            }
        }
        if (tran.isVerifiedByPinRequiredOnReceipt()) {
            addLineCentered(receipt, getText(STR_VERIFIED_BY_PIN), FONT_FIXED_WIDTH_POS);
        }
    }

    public void addEmvDiagnosticData(PrintReceipt receipt, TransRec tran) {
        // only print on merchant copy
        if (!isMerchantCopy()) {
            return;
        }

        TagDataFromPOS tagData = tran.getTagDataFromPos();

        // if tag data set from POS and EMV pad tag is present, update env var flag based on this.
        if( tagData != null && tagData.getEMV() != null ) {
            IccDiags.setNewValue(tagData.getEMV().equals("1"));
        }

        // if print ICC diags env var is false, return early
        if( !IccDiags.getCurValue() ) {
            return;
        }
        // add icc diags
        addIccDiags(receipt, tran);
    }

    @Override
    public void addFooter(PrintReceipt receipt) {
        PayCfg paycfg = d.getPayCfg();
        this.addSpaceLine(receipt); // Always add a space line here to give a gap between the footer and the previous area.
        if (isCardHolderCopy() && (paycfg.getReceipt().getFooter() != null)) {

                // We only print the line if it has data in it not empty/null
                if (paycfg.getReceipt().getFooter().getLine1() != null && paycfg.getReceipt().getFooter().getLine1().length() > 0) {
                    Receipt.addLineCentered( receipt, paycfg.getReceipt().getFooter().getLine1(), FONT_FIXED_WIDTH_POS );
                }

                // We only print the line if it has data in it not empty/null
                if (paycfg.getReceipt().getFooter().getLine2() != null && paycfg.getReceipt().getFooter().getLine2().length() > 0) {
                    Receipt.addLineCentered( receipt, paycfg.getReceipt().getFooter().getLine2(), FONT_FIXED_WIDTH_POS );
                }


        }
        addBanner(receipt);
    }
}
