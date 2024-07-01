package com.linkly.libengine.helpers;

import static com.linkly.libengine.engine.transactions.properties.TCard.CardType.CTLS;
import static com.linkly.libengine.engine.transactions.properties.TCard.CardType.EMV;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.ACC_TYPE_CHEQUE;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.ACC_TYPE_CREDIT;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.ACC_TYPE_DEFAULT;
import static com.linkly.libengine.engine.transactions.properties.TProtocol.ACC_TYPE_SAVINGS;
import static com.linkly.libpositive.events.PositiveEvent.EventType.QUERY_TRANS;
import static com.linkly.libpositive.messages.IMessages.ReportType;
import static com.linkly.libpositive.messages.IMessages.SERVICE_EVENT;

import android.content.Context;
import android.content.Intent;

import com.google.gson.Gson;
import com.linkly.libengine.action.Printing.PrintFirst;
import com.linkly.libengine.config.BinRangesCfg;
import com.linkly.libengine.config.PayCfg;
import com.linkly.libengine.dependencies.IDependency;
import com.linkly.libengine.engine.Engine;
import com.linkly.libengine.engine.EngineManager;
import com.linkly.libengine.engine.reporting.Reconciliation;
import com.linkly.libengine.engine.transactions.TransRec;
import com.linkly.libengine.engine.transactions.TransRecManager;
import com.linkly.libengine.engine.transactions.properties.TProtocol;
import com.linkly.libmal.MalFactory;
import com.linkly.libpositive.PosIntegrate;
import com.linkly.libpositive.events.PositiveTransEvent;
import com.linkly.libpositive.wrappers.HistoryTransResult;
import com.linkly.libpositive.wrappers.PositiveConfigureBankLinkRequest;
import com.linkly.libpositive.wrappers.PositiveError;
import com.linkly.libpositive.wrappers.PositiveErrorResponse;
import com.linkly.libpositive.wrappers.PositiveLogonResult;
import com.linkly.libpositive.wrappers.PositiveReadCardResult;
import com.linkly.libpositive.wrappers.PositiveReportResult;
import com.linkly.libpositive.wrappers.PositiveTransListingResult;
import com.linkly.libpositive.wrappers.PositiveTransResult;
import com.linkly.libpositive.wrappers.TagDataToPOS;
import com.pax.market.android.app.sdk.util.StringUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import timber.log.Timber;


public class ECRHelpers {
    private static final String LAST_STATUS = "LAST_STATUS";

    private static boolean isValidStringValue(String v) {
        return v != null && !v.isEmpty();
    }

    public static void packTransEvent(Intent intent, PositiveTransEvent event) {

        if (event == null)
            return;

        intent.putExtra(SERVICE_EVENT, event.toJson());
    }

    public static void ipcSendNullReportResponse(IDependency d, PositiveTransEvent originalEvent, PosIntegrate.ResultResponse response, Context context) {
        if (originalEvent == null) {
            return;
        }
        Intent intent = new Intent();
        /*Send the Notification */
        Timber.i( "Send NULL Report Response");

        packTransEvent(intent, originalEvent);

        // Adding in an empty PositiveReportResult class to the intent just to distinguish the response as a report response
        PositiveReportResult result = new PositiveReportResult();
        intent.putExtra(PositiveReportResult.class.getName(), result.toJsonString());

        // Pack an error response.
        PositiveErrorResponse errorResponse = new PositiveErrorResponse( PositiveError.INVALID_ARG, response);
        intent.putExtra( PositiveErrorResponse.class.getName(), errorResponse.toJsonString());

        d.getMessages().sendTransactionResults(context, intent, originalEvent.isLegacy());
    }

    public static void ipcSendNullTransResponse(IDependency d, PositiveTransEvent originalEvent, PosIntegrate.ResultResponse response, Context context) {

        PositiveTransResult result = new PositiveTransResult();

        if (originalEvent == null) {
            return;
        }
        Intent intent = new Intent();
        /*Send the Notification */
        Timber.i( "Send NULL Transaction Response");

        packTransEvent(intent, originalEvent);

        result.setTransApproved(false);
        result.setTransFound(false);
        result.setTransResponse(false);
        result.setTransQuery(originalEvent.getType() == QUERY_TRANS);
        result.setDeviceCode(originalEvent.getDeviceCode());
        result.setUTI(originalEvent.getUti());

        // Pack the transaction result with fields set as false
        intent.putExtra(PositiveTransResult.class.getName(), result.toJsonString());

        // Pack an error response.
        PositiveErrorResponse errorResponse = new PositiveErrorResponse( PositiveError.TRANSACTION_NOT_FOUND, response);
        intent.putExtra( PositiveErrorResponse.class.getName(), errorResponse.toJsonString());

        d.getMessages().sendTransactionResults(context, intent, originalEvent.isLegacy());
    }

    private static PositiveTransResult.AccountType applyAccountType(int accountType) {

        switch( accountType) {
            case ACC_TYPE_CHEQUE:
                return PositiveTransResult.AccountType.CHEQUE;
            case ACC_TYPE_CREDIT:
                return PositiveTransResult.AccountType.CREDIT;
            case ACC_TYPE_SAVINGS:
                return PositiveTransResult.AccountType.SAVINGS;
            case ACC_TYPE_DEFAULT:
            default:
                return PositiveTransResult.AccountType.DEFAULT;
        }
    }


    public static void packTransactionDetails(TransRec trans, PositiveTransResult result, PayCfg config, IDependency d) {
        if (trans == null || result == null )
            return;

        result.setTransDetails(true);
        result.setReceiptNumber(trans.getAudit().getReceiptNumber());

        // Send the server response code
        result.setResponseCode(trans.getProtocol().getPosResponseCode());
        // When setting the response text, sanitise any \n. The same value sent
        // to pos for display is sent as message text. E.g "DUPLCT TRANSACTION\nREFERENCE"
        result.setResponseText(trans.getProtocol().getPosResponseText().replace('\n', ' '));

        // set RRN
        result.setRetrievalReferenceNumber(trans.getProtocol().getRRN());

        // Send the server response code
        result.setBankResponseCode(trans.getProtocol().getServerResponseCode());

        result.setStan(Integer.toString(trans.getProtocol().getStan()));
        // TODO: shift this value to the transaction
        if( trans.getTransEvent() != null) {
            result.setTurnaroundTime(new Date().getTime() - trans.getTransEvent().getTimestamp());
        }
        result.setAuthorisationCode(trans.getProtocol().getAuthCode());

        if (isValidStringValue(trans.getProtocol().getMerchantTokenId()))
            result.setMerchantTokenId(trans.getProtocol().getMerchantTokenId());

        result.setBankTime(trans.getProtocol().getBankTime());
        result.setBankDate(trans.getProtocol().getBankDate()); // source/dest both yymmdd format
        result.setSettlementDate(trans.getProtocol().getSettlementDate()); // source/dest both yymmdd format

        /*Card Details*/
        if (trans.getCard() != null) {

            result.setCardType(trans.getCard().getCardType().name());

            if (trans.getCard().getCardType() == EMV || trans.getCard().getCardType() == CTLS) {

                if (trans.getCard().getAid() != null) {
                    result.setEmvAid(trans.getCard().getAid());
                }

                if (isValidStringValue(trans.getCard().getTsi())) {
                    result.setEmvTsi(trans.getCard().getTsi());
                }

                if (isValidStringValue(trans.getCard().getTvr())) {
                    result.setEmvTvr(trans.getCard().getTvr());
                }

                if (isValidStringValue(trans.getCard().getCardHolderName())) {
                    result.setEmvCardholderName(trans.getCard().getCardHolderName());
                }

                if (trans.getCard().getCryptogram() != null) {
                    result.setEmvCryptogram(trans.getCard().getCryptogram());
                    result.setEmvCryptogramType(trans.getCard().getCryptogramTypeCode());
                }
            }

            // Uses the Merchant pan mask from config cardproduct.json.
            if(isValidStringValue(trans.getMaskedPan(config))) {
                // Return the merchanct configured mask pan to be return to the pos
                result.setCardPan(trans.getMaskedPan(TransRec.MaskType.MERCH_MASK, config));
            }

            //Strings
            if (isValidStringValue(trans.getCard().getExpiry())) {
                result.setCardExpiryDate(trans.getCard().getExpiry());
            }
            if (isValidStringValue(trans.getCard().getValidFrom())) {
                result.setCardStartDate(trans.getCard().getValidFrom());
            }

            result.setCardScheme(trans.getCard().getCardIssuer().getDisplayName());

            if (trans.getCard().getPsn() != null) {
                result.setCardPanSequenceNumber(String.format("%03d", trans.getCard().getPsn()));
            }

            // Obtain the bin linkly specific number for the card.
            result.setBinNumber(Integer.toString(trans.getCard().getBinNumber(config)));
            result.setAccountType(applyAccountType(trans.getProtocol().getAccountType()));
        }

        // Mirrored from start of the transaction
        result.setPosTxnRef(trans.getAudit().getReference());
        // Flag array
        result.setOfflineFlag(trans.getProtocol().isAuthMethodOfflineApproved() ? "1" : "0");
        result.setReceiptPrintedFlag("1"); // 0
        result.setBankConnectionFlag("0"); // Default is always 0
        result.setCurrencyConvertedFlag("0"); // Currently set to 0, will need to change when DCC is implemented
        result.setPreswipeFlag(trans.getCard().isPaypassTransaction() ? "1" : "0");
        result.setCdoPerformedFlag(trans.getCard().isEmvCdoPerformed() ? "1" : "0");
        result.setUploadedFlag(trans.getProtocol().getMessageStatus().isFinalised() ? "1" : "0");

        // A transevent may not be available if a batch upload is happening outside of a pos event
        if(trans.getTransEvent() != null) {
            result.setDeviceCode(trans.getTransEvent().getDeviceCode());
        } else {
            // Discussed with Robbie, just set a 0 for a default.
            result.setDeviceCode("0");
        }

        // pack receipt data back to Connect app
        if(trans.getReceipts() == null || trans.getReceipts().isEmpty()) {
            ArrayList<PositiveTransResult.Receipt> receipts = new ArrayList<>();
            // Generate the receipts
            receipts.add(PrintFirst.buildReceipt(d, trans, MalFactory.getInstance(), true));
            receipts.add(PrintFirst.buildReceipt(d, trans, MalFactory.getInstance(), false));
            result.setReceipts(receipts);
        } else {
            result.setReceipts(trans.getReceipts());
        }
    }


    public static void packConfigDetails(IDependency d, PositiveTransResult result, String softwareVersion) {
        PayCfg cfg = d.getPayCfg();
        /*Terminal MalConfig Properties */
        if (cfg != null && cfg.isValidCfg() && result != null) {
            result.setTransCurrencyCode(cfg.getCurrencyCode());
            result.setTerminalId(cfg.getStid());
            result.setMerchantId(cfg.getMid());
            result.setSoftwareVersion(softwareVersion);
            result.setAcquirerID(Engine.getCustomer() != null ? Engine.getCustomer().getAcquirerCode():" "); // get from customer class
        }
    }

    // The trans amount field will change depending on what type of transaction it is.
    public static void packAmounts(TransRec trans, PositiveTransResult result) {

        switch(trans.getTransType()) {
            // Cash out /
            case CASH:
            case CASH_AUTO:
                result.setAmountCashback(trans.getAmounts().getCashbackAmount()); // Pack the amount field into the cashback field
                break;

            // Refund Transactions
            case REFUND:
            case REFUND_AUTO:
            case REFUND_MOTO_AUTO:
            case REFUND_MOTO:
                result.setAmountRefund(trans.getAmounts().getAmount());
                break;

            // Treat all transactions as either regular transactions
            case SALE:
            case SALE_AUTO:
            default:
                result.setAmountTrans(trans.getAmounts().getAmount());
                result.setAmountCashback(trans.getAmounts().getCashbackAmount());                 // The tip amount (minor units, 100 = £1.00)
                result.setSurcharge(trans.getAmounts().getSurcharge());
                break;

        }

        // Gratuity always maps to the same field
        result.setAmountGratuity(trans.getAmounts().getTip());                 // The tip amount (minor units, 100 = £1.00)

    }

    public static void ipcSendTransResponse(IDependency d, TransRec trans, Context context) {
        ipcSendTransResponse(d, trans, context, true);
    }

    // We actually send responses regardless if auto or not.
    // Intention is other apps can listen to the transaction results and handle accordingly
    // AKA - allows our connect app to listen with out issues.

    /**
     * Send transaction response to connect using libpositive
     *
     * @param d             a {@link IDependency} object to get the required dependencies from
     * @param trans         current {@link TransRec}
     * @param context       {@link Context} to use
     * @param finalResponse flag to indicate if the response is the final response for the original request
     */
    public static void ipcSendTransResponse(IDependency d, TransRec trans, Context context, boolean finalResponse) {
        TransRec reversalTrans = null;

        if (trans != null && trans.getTransType() == EngineManager.TransType.MANUAL_REVERSAL_AUTO) {
            // find latest txn that can be reversed
            reversalTrans = TransRecManager.getInstance().getTransRecDao().getLatest();
            // Trans event contains additional meta data that can be used when packing the response. i.e device code
            reversalTrans.setTransEvent(trans.getTransEvent());
        }

        Intent intent = new Intent();
        if ((trans != null && trans.getTransType() != EngineManager.TransType.MANUAL_REVERSAL_AUTO) || (reversalTrans != null)) {
            Timber.i("Send Transaction Response");

            packTransEvent(intent, trans.getTransEvent());

            PositiveTransResult result = packTransactionResult(d,trans, finalResponse);

            // for reversal we pack the details from the original transaction
            packTransactionDetails((trans.getTransType() != EngineManager.TransType.MANUAL_REVERSAL_AUTO) ? trans : reversalTrans, result, d.getPayCfg(), d);

            final String TRANS_RESULT_STRING = result.toJsonString();

            Timber.i( "TransResult%s", TRANS_RESULT_STRING);
            intent.putExtra(PositiveTransResult.class.getName(),TRANS_RESULT_STRING);
        } else {
            PositiveErrorResponse errorResponse = new PositiveErrorResponse( PositiveError.TRANSACTION_NOT_FOUND, PosIntegrate.ResultResponse.RES_RESPONSE_NOT_RECEIVED );
            intent.putExtra( PositiveErrorResponse.class.getName(), errorResponse.toJsonString());
        }

        intent.putExtra("LastStatus", d.getStatusReporter().getLastStatus().name());

        d.getMessages().sendTransactionResults(context, intent, false);
    }

    public static void ipcSendTransListResponse(IDependency d, List<TransRec> transList, Context context) {
        Intent intent = new Intent();
        Timber.i( "Send Transaction Listing Response");
        if(transList != null) {
            Timber.i("Transaction List size : %s", transList.size());
            ArrayList<PositiveTransListingResult> positiveTransListingResults = new ArrayList<>();
            for (int i = 0; i < transList.size(); i++) {
                PositiveTransListingResult result = packTransactionListingResult(transList.get(i));
                positiveTransListingResults.add(result);
            }

            Gson gson = new Gson();
            String data = gson.toJson(positiveTransListingResults);
            Timber.i("TransResult len : %d", data.length());
            try {
                intent.putExtra(PositiveTransListingResult.class.getName(),data);
            } catch( Exception e ) {
                Timber.e(e, "ERROR, exception adding transaction listing data" );
            }
        } else {
            PositiveErrorResponse errorResponse = new PositiveErrorResponse( PositiveError.TRANSACTION_NOT_FOUND, PosIntegrate.ResultResponse.RES_RESPONSE_NOT_RECEIVED );
            intent.putExtra( PositiveErrorResponse.class.getName(), errorResponse.toJsonString());
        }
        d.getMessages().sendTransactionResults(context, intent,false);
    }

    /**
     * Pack transaction data to PositiveTransResult
     *
     * @param d               a {@link IDependency} object to get the required dependencies from
     * @param transRec        current {@link TransRec}
     * @param isFinalResponse true if the response is the final response for the original request false otherwise
     * @return Packed {@link PositiveTransResult}
     */
    public static PositiveTransResult packTransactionResult(IDependency d, TransRec transRec, boolean isFinalResponse) {
        PositiveTransResult result = new PositiveTransResult();
        // If the transaction is reversed set this flag
        if (transRec.getProtocol().getOriginalStan() != null) {
            result.setOriginalStan(transRec.getProtocol().getOriginalStan().toString());
        }
        // pack PAD tag data
        TagDataToPOS tagDataToPOS = transRec.getTagDataToPos() == null ? new TagDataToPOS() : transRec.getTagDataToPos();
        tagDataToPOS.setCem(transRec.getCard().getCardEntryMode());
        transRec.setTagDataToPos(tagDataToPOS);
        result.setPadTagJson(TagDataToPOS.buildToJson(transRec.getTagDataToPos()));
        result.setJournalType(transRec.getJournalType());
        result.setFinalResponse(isFinalResponse);
        result.setTransResponse(true);
        result.setTransFound(true);

        if( d.getCurrentTransaction() != null ) {
            // use 'current transaction' object values
            if (d.getCurrentTransaction().getProtocol().getAuthCode() != null) {
                result.setBankRefNumber(d.getCurrentTransaction().getProtocol().getAuthCode());
            }
            if (d.getCurrentTransaction().getProtocol().getServerResponseCode() != null) {
                result.setBankResponseCode(d.getCurrentTransaction().getProtocol().getServerResponseCode());
            }
        } else {
            // use trans rec values
            if (transRec.getProtocol().getAuthCode() != null) {
                result.setBankRefNumber(transRec.getProtocol().getAuthCode());
            }
            if (transRec.getProtocol().getServerResponseCode() != null) {
                result.setBankResponseCode(transRec.getProtocol().getServerResponseCode());
            }
        }
        result.setBankResponseText(null);

        if(transRec.getTransEvent() != null) {
            result.setTransQuery(transRec.getTransEvent().getType() == QUERY_TRANS);
        } else {
            result.setTransQuery(false);
        }

        result.setTransType(transRec.getTransType().toString());
        result.setUTI(transRec.getAudit().getUti());
        // Pack our amounts
        packAmounts(transRec, result);

        result.setMsgStatus(transRec.getProtocol().getMessageStatus().displayName);
        result.setTransApproved(transRec.isApprovedOrDeferred());
        result.setTransCancelled(transRec.isCancelled());
        result.setCvmSigRequired(transRec.getCard().isSignatureVerificationRequired());
        result.setCvmPinVerified(transRec.getCard().isPinVerificationRequired());
        packConfigDetails(d, result, d.getPayCfg().getPaymentAppVersion());

        // TODO: shift this value to the transaction
        if(transRec.getTransEvent() != null) {
            result.setStartTime(transRec.getTransEvent().getTimestamp());
        }
        return result;
    }

    public static PositiveTransListingResult packTransactionListingResult(TransRec transRec) {
        PositiveTransListingResult result = new PositiveTransListingResult();
        // Mirrored from start of the transaction
        result.setPosTxnRef(transRec.getAudit().getReference());

        boolean finalised = transRec.getProtocol().getMessageStatus().isFinalised();
        boolean voided = transRec.isReversal();
        String adviceResponseCode = transRec.getProtocol().getAdviceResponseCode();
        String serverResponseCode = transRec.getProtocol().getServerResponseCode();

        // Flag array
        result.setUploadedFlag(finalised ? "1" : "0");
        result.setVoidedFlag(voided ? "1" : "0");
        result.setDeferredAuthTrans(transRec.isDeferredAuth());
        result.setBankDate(transRec.getProtocol().getBankDate()); // source/dest both yymmdd format
        result.setBankTime(transRec.getProtocol().getBankTime());
        result.setRetrievalReferenceNumber(transRec.getProtocol().getRRN());

        if (finalised && adviceResponseCode != null) {
            // if deferred auth or advice, send back the advice response code
            result.setBankResponseCode(adviceResponseCode);
            result.setHostApproved(transRec.getProtocol().getHostResult() == TProtocol.HostResult.AUTHORISED);
        } else if (finalised && serverResponseCode != null) {
            // if it was online authorised transaction, i.e. not an advice or deferred auth
            result.setBankResponseCode(serverResponseCode);
            result.setHostApproved(transRec.getProtocol().getHostResult() == TProtocol.HostResult.AUTHORISED);
        } else {
            // else not finalised
            result.setBankResponseCode("");
            result.setHostApproved(false);
        }
        return result;
    }

    public static void ipcSendReportResponse(IDependency d, TransRec trans, Reconciliation rec, String reportType, Context context) {
        ipcSendReportResponse(d, trans, rec, reportType, context, true);
    }

    /**
     * Send report response to connect using libpositive
     *
     * @param d             a {@link IDependency} object to get the required dependencies from
     * @param trans         current {@link TransRec}
     * @param reportType    type of report to send, should be one of {@link ReportType}
     * @param context       {@link Context} to use
     * @param finalResponse flag to indicate if the response is the final response for the original request
     */
    // Suppressing complexity warning
    @SuppressWarnings("java:S6541")
    public static void ipcSendReportResponse(IDependency d, TransRec trans, Reconciliation rec, String reportType, Context context, boolean finalResponse) {

        Intent intent = new Intent();
        Timber.i( "Send Report Response");

        if (reportType.equals(ReportType.HistoryReport.toString())) {
            ArrayList<HistoryTransResult> historyTransList = new ArrayList<>();

            if(rec != null) {
                ArrayList<TransRec> transactionList = rec.getRecTransList();
                Timber.i("TransactionList:  size%d", transactionList.size());
                for (int i = 0; i < transactionList.size(); i++) {
                    String result = "DECLINED";
                    if (transactionList.get(i).isApprovedOrDeferred()) {
                        result = "APPROVED";
                    } else if (transactionList.get(i).isCancelled()) {
                        result = "CANCELLED";
                    }

                    HistoryTransResult historyResult = new HistoryTransResult();
                    historyResult.setTransType(transactionList.get(i).getTransType().name().replace("MANUAL_", ""));
                    historyResult.setTransAmount(transactionList.get(i).getAmounts().getTotalAmount());
                    historyResult.setTransApproved(result);
                    historyResult.setTransDate(transactionList.get(i).getAudit().getTransDateTimeAsString("yyyy-MM-dd hh:mm:ss"));
                    historyResult.setTransPan(transactionList.get(i).getMaskedPan(TransRec.MaskType.REPORT_MASK, d.getPayCfg()));
                    historyResult.setRnn(transactionList.get(i).getProtocol().getRRN());
                    historyResult.setReceiptNo(transactionList.get(i).getAudit().getReceiptNumber());
                    historyTransList.add(historyResult);
                }
            }

            Gson gson = new Gson();
            String data = gson.toJson(historyTransList);
            intent.putExtra(HistoryTransResult.class.getName(), data);

        } else {
            PositiveReportResult reportResult = new PositiveReportResult();
            reportResult.setReportType(reportType);
            // on issues where the terminal may not be configured correctly we wont sent back information
            if(rec != null) {
                reportResult.setSaleCount(rec.getSale().count - rec.getSale().reversalCount);
                reportResult.setSaleAmount(rec.getSale().amount - rec.getSale().reversalAmount);
                reportResult.setRefundCount(rec.getRefund().count - rec.getRefund().reversalCount);
                reportResult.setRefundAmount(rec.getRefund().amount - rec.getRefund().reversalAmount);
                reportResult.setCompletionCount(rec.getCompletion().count - rec.getCompletion().reversalCount);
                reportResult.setCompletionAmount(rec.getCompletion().amount - rec.getCompletion().reversalAmount);
                reportResult.setCashbackCount((rec.getCashback().count - rec.getCashback().reversalCount)+(rec.getCash().count - rec.getCash().reversalCount));
                reportResult.setCashbackAmount((rec.getCashback().amount - rec.getCashback().reversalAmount)+(rec.getCash().amount - rec.getCash().reversalAmount));
                reportResult.setGratuityCount(rec.getTips().count - rec.getTips().reversalCount);
                reportResult.setGratuityAmount(rec.getTips().amount - rec.getTips().reversalAmount);

                // TODO: previous scheme totals?
                for (Reconciliation.CardSchemeTotals total : rec.getPreviousSchemeTotalsAsArray()) {
                    reportResult.getTotals().add(new PositiveReportResult.CardSchemeTotals(total.name, total.purchaseAmount, total.purchaseCount, total.cashoutAmount, total.cashoutCount, total.refundAmount, total.refundCount, total.totalAmount, total.totalCount));
                }
            }

            if( trans != null && trans.getTransEvent() != null) {
                reportResult.setTurnaroundTime(new Date().getTime() - trans.getTransEvent().getTimestamp());
                reportResult.setDeviceCode(trans.getTransEvent().getDeviceCode());
            } else {
                reportResult.setTurnaroundTime(0); // It is possible that this happened automatically which means it wasn't started via an event.
            }

            reportResult.setFinalResponse(finalResponse);
            reportResult.setReportResponse(true);

            if( trans != null && trans.getProtocol() != null ) {
                PayCfg cfg = d.getPayCfg();
                /*Terminal MalConfig Properties */
                if (cfg != null && cfg.isValidCfg()) {

                    reportResult.setTid(cfg.getStid());
                    reportResult.setMid(cfg.getMid());
                    reportResult.setAcquirer(Engine.getCustomer() != null ? Engine.getCustomer().getAcquirerCode():" "); // get from customer class
                }

                reportResult.setResponseCode(trans.getProtocol().getPosResponseCode());
                reportResult.setResponseText(trans.getProtocol().getPosResponseText());
                reportResult.setStan(Integer.toString(trans.getProtocol().getStan()));
                reportResult.setBankDate(convertBankDateToDDMMYY(trans)); // dest is DDMMYY format, source is YYMMDD
                reportResult.setBankTime(trans.getProtocol().getBankTime());
            }

            // if either date or time is null, means it wasn't set by protocol layer. set both to current date/time
            if( reportResult.getBankDate() == null || reportResult.getBankTime() == null ){
                // processed offline, set date/time as current clock date/time
                SimpleDateFormat dateTimeFormatter = new SimpleDateFormat("ddMMyy", Locale.getDefault());
                Date now = new Date();
                reportResult.setBankDate(dateTimeFormatter.format(now));
                dateTimeFormatter.applyPattern( "HHmmss" );
                reportResult.setBankTime(dateTimeFormatter.format(now));
            }

            // set approved flag
            if( trans != null ) {
                reportResult.setApproved(trans.isApproved());
            } else {
                // assume success
                reportResult.setApproved(true);
            }

            String data = reportResult.toJsonString();
            intent.putExtra(PositiveReportResult.class.getName(), data);
        }

        d.getMessages().sendTransactionResults(context, intent,false);
    }

    public static void ipcSendCardReadResponse(IDependency d, PositiveReadCardResult positiveReadCardResult, Context context ){
        final Intent INTENT = packIntent( d, PositiveReadCardResult.class.getName(), positiveReadCardResult.toJsonString() );
        /*Send the Notification */
        Timber.d( "Send Card Read Response" );
        Timber.i( positiveReadCardResult.toJsonString());

        d.getMessages().sendCardReadResults(context, INTENT );
    }

    private static String convertBankDateToDDMMYY(TransRec trans){
        String input = trans.getProtocol().getBankDate(); // yyMMdd format
        if( input != null && input.length() == 6 ) {
            // swap YY and DD chars in output
            return input.substring(4,6) + input.substring(2,4) + input.substring(0,2);
        }
        return null;
    }
    public static void ipcSendLogonResponse(IDependency d, TransRec trans, Context context) {
        ipcSendLogonResponse(d, trans, context, true);
    }

    /**
     * Helper method to pack Logon intent for Connect App
     *
     * @param d             a {@link IDependency} object to get the required dependencies from
     * @param trans         current {@link TransRec}
     * @param context       {@link Context} to use
     * @param finalResponse flag to indicate if the response is the final response for the original request
     */
    public static void ipcSendLogonResponse( IDependency d, TransRec trans, Context context, boolean finalResponse ){
        /*Send the Notification */
        Timber.d( "Send Logon Response" );
        PositiveLogonResult result = new PositiveLogonResult();

        Timber.d( "Trans members: " );
        Timber.d( "Logon approved = %b", trans.isApproved() );
        Timber.d( "Response Code = %s", trans.getProtocol().getServerResponseCode() );
        Timber.d( "Bank Date from trans.protocol (YYMMDD) = %s", trans.getProtocol().getBankDate() );
        Timber.d( "Bank Time from trans.protocol (hhmmss) = %s", trans.getProtocol().getBankTime() );
        Timber.d( "Stan = %s", trans.getProtocol().getStan() );
        Timber.d( "Terminal ID = %s", trans.getAudit().getTerminalId() );
        Timber.d( "Merchant ID = %s", trans.getAudit().getMerchantId() );

        result.setApproved( trans.isApproved() );
        if( trans.getTransEvent() != null ) {
            result.setOperationType( trans.getTransEvent().getOperationType() );
            result.setDeviceCode(trans.getTransEvent().getDeviceCode());
        }
        result.setFinalResponse(finalResponse);
        result.setResponseCode( trans.getProtocol().getPosResponseCode() );
        result.setResponseText( trans.getProtocol().getPosResponseText() );
        result.setBankDate(convertBankDateToDDMMYY(trans)); // this dest object format is different to other lib positive, source is YYMMDD and dest is DDMMYY
        result.setBankTime( trans.getProtocol().getBankTime() );
        result.setStan( Integer.toString( trans.getProtocol().getStan() ) );
        result.setTerminalId( trans.getAudit().getTerminalId() );
        result.setMerchantId( trans.getAudit().getMerchantId() );
        result.setSoftwareVersion(d.getPayCfg().getPaymentAppVersion()); // keep the software version decoupled from the transaction version as has nothing to do with a record.
        result.setEftposTransactionsAllowed( true );
        result.setAgencyTransactionsAllowed( false );
        result.setAcquirerCode( Engine.getCustomer() != null ? Engine.getCustomer().getAcquirerCode():" " ); // get from customer class

        // TODO: Tag data

        final Intent INTENT = packIntent( d, PositiveLogonResult.class.getName(), result.toJson() );

        d.getMessages().sendLogonResponse( context, INTENT );
    }

    /**
     * Helper method to pack response for Config update
     * @param d {@link IDependency} object
     * @param approved boolean to indicate if config was updated successfully
     * */
    public static void ipcSendConfigUpdateStatus( IDependency d, boolean approved, Context context ){
        PositiveLogonResult result = new PositiveLogonResult();

        result.setOperationType( PositiveLogonResult.OperationType.CONFIG_UPDATE );
        if( approved ) {
            result.setResponseCode( "00" );
            result.setResponseText( "APPROVED" );
        } else {
            result.setResponseCode( "ZZ" );
            result.setResponseText( "DECLINED" );
        }

        d.getMessages().sendLogonResponse( context,
                packIntent( d, PositiveLogonResult.class.getName(), result.toJson() ) );
    }

    /**
     * Private packer method which packs intents in a fixed format.
     * It will pack 2 pairs
     * 1. Key = class name, value = class object Json
     * 2. Key = {@link #LAST_STATUS}, value = Name of the {@link com.linkly.libengine.status.IStatus.STATUS_EVENT} event
     * @param d {@link IDependency} object
     * @param className name of the class object to be used as key
     * @param jsonString json packed object of the class
     * */
    private static Intent packIntent( IDependency d, String className, String jsonString ){
        Intent intent = new Intent(  );

        intent.putExtra( className, jsonString );
        intent.putExtra( LAST_STATUS, d.getStatusReporter().getLastStatus().name() );

        return intent;
    }

    public static void ipcConfigureBankLink(IDependency d, PositiveConfigureBankLinkRequest message, Context context ) {
        Timber.d( "Configuring bank link" );
        d.getMessages().sendConfigureBankLinkMessage(context, message);
    }
    /**
     * Helper method to pack card data response for POS response(eg, Query card processing)
     *
     * Input format - exactly as it comes from secApp
     * Masked PAN (first 8, last 4 unmasked), delimiter (either = or D char), 4 digits expiry, 0-n extra chars (discretionary data)
     *
     * Output format:
     * Masked PAN (first 6, last 4 unmasked), ‘=’, <Expiry Date>, <pad with end sentinels (?)>
     * e.g.  456789****9012=1299????????????
     *
     * @param track2Str {@link String} input track
     *
     * @return formatted track2 data as string
     * */
    public static String getMaskedTrackData(String track2Str) {
        // early return if null
        if (track2Str == null){
            return null;
        }

        final String FIELD_SEPARATOR_REGEX = "=";
        StringBuilder output = new StringBuilder();

        // replace 'D' chars with = delimiter, remove 'F' padding nibbles
        track2Str = track2Str.replace("D", "=").replace("F", "");

        // split input by delimiter
        String[] parts = track2Str.split(FIELD_SEPARATOR_REGEX);

        // partIdx 0 is the PAN part. add masked to output
        output.append(getMaskedPanFirst6Last4(parts[0]));

        for (int partIdx = 1; partIdx < parts.length; partIdx++) {
            // add delimiter to output
            output.append(FIELD_SEPARATOR_REGEX);
            String part = parts[partIdx];
            output.append(maskPart(partIdx, part));
        }
        return output.toString();
    }

    /**
     * masks part of the input track data, called for each delimiter separated part of input track data
     *
     * @param partIdx index of part 1 = expiry, 2 or higher called if there are multiple delimiters in input
     * @param part input string for this 'part'
     * @return masked data
     */
    private static String maskPart(int partIdx, String part) {
        // Mask the expiry section (after PAN, which is index 0)
        if (partIdx == 1) {
            // the 'expiry partIdx' is 1. we want to mask all chars after the expiry
            if (part.length() >= 4) {
                //expiry date is present, then all digits following the four-digit expiry date (after the equals sign) must be masked with ‘?’
                return part.substring(0, 4) + StringUtils.repeat("?", part.length() - 4);
            } else {
                // less than 4 expiry digits, add digits unmasked
                return part;
            }
        } else {
            // if not 1st (PAN) or 2nd (expiry) part, mask it with ? chars
            return StringUtils.repeat("?", part.length());
        }
    }


    private static String getMaskedPanFirst6Last4(String input){
        final String OUTPUT_MASK_CHAR = "0";

        if (input.length() <= 10) {
            return input;
        }

        int numPadDigits = input.length() - 10;
        String firstSix = input.substring(0, 6);
        String lastFour = input.substring(input.length()-4);

        return firstSix + StringUtils.repeat(OUTPUT_MASK_CHAR, numPadDigits) + lastFour;
    }

    private static String extractLeadingDigits(String str) {
        Pattern pattern = Pattern.compile("^\\d+");
        Matcher matcher = pattern.matcher(str);

        if (matcher.find()) {
            return matcher.group();
        }
        return ""; // Return empty string if no leading digits are found
    }

    /**
     * checks if passed track string is a financial card
     * input could be:
     * - masked,   e.g. 49291234****1234=1234????????????????
     * - unmasked, e.g. 4929123401201234=12340100239010293800
     *
     * if it's an unmasked, and identified as financial (card prefix matches an entry in BIN table)
     * then return true,
     * if it's masked (PAN part left of delimiter contains a * masking character), assume it's financial, return true
     * else return false
     *
     * @param trackStr - input track data string
     * @param binRangesCfg - BIN table configuration
     * @return true if card is an unmasked financial card range, else false
     */
    private static boolean isFinancialCard(PayCfg payCfg, String trackStr, BinRangesCfg binRangesCfg){
        final String FIELD_SEPARATOR_REGEX = "[=D]";
        final int MINIMUM_PAN_DIGITS_FOR_FINANCIAL_CARD = 14;
        boolean result = false;

        if (trackStr != null) {
            String[] parts = trackStr.split(FIELD_SEPARATOR_REGEX); // extract PAN
            // financial cards have delimiter char, so parts will be >= 2
            if (parts.length >= 2){
                if (parts[0].contains("*")) {
                    // if 'pan part' left of delimiter contains a masking char, then treat this as a financial card, i.e. it should remain masked
                    Timber.i("isFinancialCardBin PAN is masked already, assume financial");
                    result = true;
                } else {
                    // extract digits from start of parts[0] to get ready for BIN check
                    String trackBin = extractLeadingDigits(parts[0]);

                    // if we have less than minimum PAN digits for financial card, it's not a financial card
                    if (trackBin.length() >= MINIMUM_PAN_DIGITS_FOR_FINANCIAL_CARD) {
                        // check BIN table for matching BIN
                        int index = binRangesCfg.getCardsCfgIndex(payCfg, trackBin);
                        // if index is not negative, we have a match => this is a financial card
                        result = index >= 0;
                        Timber.i("isFinancialCardBin index = %d", index);
                    } else {
                        // less than MINIMUM_PAN_DIGITS_FOR_FINANCIAL_CARD leading PAN digits. may be masked already, or short PAN (loyalty) card
                        Timber.i("isFinancialCardBin trackStr bin length %d is < %d digits", trackBin.length(), MINIMUM_PAN_DIGITS_FOR_FINANCIAL_CARD);
                    }
                }
            } else {
                Timber.i("isFinancialCardBin trackStr contains < 2 parts, can't be financial");
            }
        } else {
            Timber.i("isFinancialCardBin trackStr is null");
        }

        Timber.e("isFinancialCardBin returning %b", result);
        return result;
    }

    /**
     * takes input track 2 and
     * - converts delimiter to =
     * - removes F padding
     * - converts * masking chars to 0's
     *
     * @param track2Str input track data
     * @return output sanitised track data
     */
    public static String getSanitisedTrackData(String track2Str) {
        String output = track2Str;

        if (output != null) {
            // replace 'D' chars with = delimiter
            output = output.replace("D", "=");
            // remove 'F' padding nibbles
            output = output.replace("F", "");
            // convert '*' padding chars to '0's
            output = output.replace("*", "0");
        }
        return output;
    }

    public static String maskIfFinancial(PayCfg payCfg, String input, BinRangesCfg binRangesCfg){
        // if input is a financial card
        if (isFinancialCard(payCfg, input, binRangesCfg)) {
            // return masked version of the track data
            return getMaskedTrackData(input);
        } else {
            // return unmasked but sanitised track data
            return getSanitisedTrackData(input);
        }
    }

}
