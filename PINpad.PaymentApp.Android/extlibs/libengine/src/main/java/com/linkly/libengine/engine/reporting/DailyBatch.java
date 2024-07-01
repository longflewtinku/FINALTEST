package com.linkly.libengine.engine.reporting;

import static com.linkly.libengine.engine.reporting.ReconciliationManager.reconciliationDao;
import static com.linkly.libengine.engine.transactions.properties.TCard.CardIssuer.UNKNOWN;

import com.linkly.libengine.config.PayCfg;
import com.linkly.libengine.dependencies.IDependency;
import com.linkly.libengine.engine.Engine;
import com.linkly.libengine.engine.EngineManager;
import com.linkly.libengine.engine.transactions.TransRec;
import com.linkly.libengine.engine.transactions.TransRecManager;
import com.linkly.libengine.engine.transactions.properties.TReconciliationFigures;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class DailyBatch implements IDailyBatch {

    @SuppressWarnings("fallthrough")
    public Reconciliation generateDailyBatch(boolean isRec, IDependency dependency) {
        boolean last = true;   // true when we are searching for the last (most recent) transaction that goes to the reconciliation

        // create Reconciliation for TransRec
        Reconciliation dailyBatch = new Reconciliation();

        // get references to its TReconciliationFigures and TransRec ArrayList
        TReconciliationFigures reconcFigures = dailyBatch.getReconciliationFigures();
        ArrayList<TransRec> recTransList = dailyBatch.getRecTransList();

        // find all transactions that haven't been marked as reconciled
        List<TransRec> allTrans = TransRecManager.getInstance().getTransRecDao().findBySummedOrReced( false );

        if (allTrans == null) {
            /*No Data */
            return dailyBatch;
        }

        EngineManager.TransType transType;

        long amountTrans;
        int reversalCount;
        int authCount;

        Collections.reverse(allTrans);  // reverse the order so the last  would come first

        // calculate totals (we exit the loop if we find previous reconciliation)
        for (Object obj : allTrans) {
            if(obj.getClass() == TransRec.class) {
                TransRec trans = (TransRec)obj;
                transType = trans.getTransType();


                // if the transaction has already been included in a rec ignore it and continue the loop
                if (trans.isSummedOrReced()) {
                    continue;
                }

                // flag the transaction as being recorded in a rec ('reconciled')
                if (isRec) {
                    trans.setSummedOrReced(true);
                    trans.save();
                }

                if (trans.approvedAndIncludeInReconciliation()) {

                    // update the last transID
                    if (last) {
                        dailyBatch.setEndTran(trans.getProtocol().getStan());
                        dailyBatch.setEndReceiptTran(trans.getAudit().getReceiptNumber());
                        last = false;

                        // record the batch id for this txn in the reconciliation record
                        dailyBatch.setBatchNumber(trans.getProtocol().getBatchNumber());
                    }

                    // update the first transID
                    dailyBatch.setStartTran(trans.getProtocol().getStan());
                    dailyBatch.setStartReceiptTran(trans.getAudit().getReceiptNumber());
                    amountTrans = trans.getAmounts().getTotalAmount();
                    authCount = trans.getProtocol().getAuthCount();
                    reversalCount = trans.getProtocol().getReversalCount();

                    /* the auth count and reversal counts are only really for iso protocols */
                    if (!dependency.getCustomer().supportRecWithAuthCount()) {
                        authCount = 1;
                        reversalCount = 0;
                    }
                    // TODO: tips, cash amount for cashback (not for ISO, but for receipt)
                    // calculations for the reconciliation receipt
                    switch (transType) {
                        default:
                            break;

                        case CASHBACK:
                        case CASHBACK_AUTO:
                            dailyBatch.getCashback().updateAmounts(reversalCount, authCount, trans.getAmounts().getCashbackAmount());
                            if (trans.getAmounts().getAmount() == 0) {
                                break;
                            }
                            /*Fall through intended so cashback sales are counted correctly*/
                        case SALE:
                        case SALE_AUTO:
                        case OFFLINESALE:
                        case CARD_NOT_PRESENT:
                        case SALE_MOTO:
                        case SALE_MOTO_AUTO:
                            long amount = trans.getAmounts().getTotalAmountWithoutCashback();

                            if ( dependency.getPayCfg() != null && dependency.getPayCfg().isTipAllowed() && dependency.getCustomer().supportTipsOnReports() ) {
                                amount = trans.getAmounts().getTotalAmountWithoutCashback();
                                if (trans.getAmounts().getTip() > 0) {
                                    dailyBatch.getTips().updateAmounts(reversalCount, authCount, trans.getAmounts().getTip());
                                }
                            }
                            if (trans.isVas()) {
                                dailyBatch.getVas().updateAmounts(reversalCount, authCount, amount);
                            } else {
                                dailyBatch.getSale().updateAmounts(reversalCount, authCount, amount);
                            }

                            if( dependency.getPayCfg() != null && dependency.getPayCfg().isSurchargeSupported() && trans.getAmounts().getSurcharge() > 0 ){
                                dailyBatch.getSurcharge().updateAmounts( reversalCount, authCount, trans.getAmounts().getSurcharge() );
                            }

                            break;

                        case CASH:
                        case CASH_AUTO:
                        case OFFLINECASH:
                            dailyBatch.getCash().updateAmounts(reversalCount, authCount, amountTrans);
                            break;

                        case PREAUTH:
                        case PREAUTH_AUTO:
                        case PREAUTH_MOTO:
                        case PREAUTH_MOTO_AUTO:
                            dailyBatch.getPreauth().updateAmounts(reversalCount, authCount, amountTrans);
                            break;

                        case COMPLETION:
                        case COMPLETION_AUTO:
                            dailyBatch.getCompletion().updateAmounts(reversalCount, authCount, amountTrans);
                            break;

                        // TODO: do we need this ?
                    /*
                    case TOPUPPREAUTH:
                        reconciliation.UpdateTopupPreauth(isReversal, amountTrans);
                        break;

                    case TOPUPCOMPLETION:
                        reconciliation.UpdateTopupCompletion(isReversal, amountTrans);
                        break;
                     */

                        case REFUND:
                        case REFUND_AUTO:
                        case REFUND_MOTO:
                        case REFUND_MOTO_AUTO:
                        case CARD_NOT_PRESENT_REFUND:
                            dailyBatch.getRefund().updateAmounts(reversalCount, authCount, amountTrans);
                            break;
                        case DEPOSIT:
                            dailyBatch.getDeposit().updateAmounts(reversalCount, authCount, amountTrans);
                            break;
                    }

                    // add transaction to the list for receipt
                    recTransList.add(trans);
                }
            }
        }

        // DE-74 -> Credits, number (financial transactions with processing codes 20-29)
        reconcFigures.setCreditsNumber(dailyBatch.getRefund().count + dailyBatch.getDeposit().count); //[JH]

        // DE-75 -> Credits, reversal number (reversals of transactions with processing codes 00-19)//[JH]
        reconcFigures.setCreditsReversalNumber(dailyBatch.getSale().reversalCount +
                dailyBatch.getVas().reversalCount +
                dailyBatch.getCash().reversalCount +
                dailyBatch.getCompletion().reversalCount);

        // DE-76 -> Debits, number (financial transactions with processing codes 00-19)             //[JH]
        reconcFigures.setDebitsNumber(dailyBatch.getSale().count +
                dailyBatch.getVas().count +
                dailyBatch.getCash().count +
                dailyBatch.getCompletion().count);

        // DE-77 -> Debits, reversal number (reversals of transactions with processing codes 20-29) //[JH]
        reconcFigures.setDebitsReversalNumber(dailyBatch.getRefund().reversalCount +
                dailyBatch.getDeposit().reversalCount);

        // DE-81 -> Authorisations, number
        reconcFigures.setAuthorisationsNumber(dailyBatch.getPreauth().count);                        //[JH]

        // DE-86 -> Credits, amount (financial transactions with processing codes 20-29; excluding fees)
        reconcFigures.setCreditsAmount(dailyBatch.getRefund().amount +                               //[JH]
                dailyBatch.getDeposit().amount);

        // DE-87 -> Credits, reversal amount (reversals of transactions with processing codes 00-19; excluding fees)
        reconcFigures.setCreditsReversalAmount(dailyBatch.getSale().reversalAmount +                 //[JH]
                dailyBatch.getVas().reversalAmount +
                dailyBatch.getCashback().reversalAmount +
                dailyBatch.getCash().reversalAmount +
                dailyBatch.getCompletion().reversalAmount);

        // DE-88 -> Debits, amount (financial transactions with processing codes 00-19)             //[JH]
        reconcFigures.setDebitsAmount(dailyBatch.getSale().amount +
                dailyBatch.getVas().amount +
                dailyBatch.getCashback().amount +
                dailyBatch.getCash().amount +
                dailyBatch.getCompletion().amount);

        // DE-89 -> Debits, reversal amount (reversals of transactions with processing codes 20-29; excluding fees)
        reconcFigures.setDebitsReversalAmount(dailyBatch.getRefund().reversalAmount +                //[JH]
                dailyBatch.getDeposit().reversalAmount);

        reconcFigures.setCashoutsAmount(dailyBatch.getCash().amount + dailyBatch.getCashback().amount);
        reconcFigures.setCashoutsNumber(dailyBatch.getCash().count + dailyBatch.getCashback().count);

        // DE-97
        reconcFigures.setNetReconciliationAmount(dailyBatch.getSale().amount - dailyBatch.getSale().reversalAmount +
                dailyBatch.getVas().amount - dailyBatch.getVas().reversalAmount +
                dailyBatch.getTips().amount - dailyBatch.getTips().reversalAmount +
                dailyBatch.getCashback().amount - dailyBatch.getCashback().reversalAmount +
                dailyBatch.getSurcharge().amount - dailyBatch.getSurcharge().reversalAmount +
                dailyBatch.getCash().amount - dailyBatch.getCash().reversalAmount +
                dailyBatch.getCompletion().amount - dailyBatch.getCompletion().reversalAmount -
                dailyBatch.getRefund().amount + dailyBatch.getRefund().reversalAmount -
                dailyBatch.getDeposit().amount + dailyBatch.getDeposit().amount);

        // sub/total amounts/counts for receipt
        // TODO: subtotal to be exclusive of cashback
        dailyBatch.setSubTotalAmount( reconcFigures.getNetReconciliationAmount() - dailyBatch.getTips().amount - dailyBatch.getSurcharge().amount );
        dailyBatch.setSubTotalCount(reconcFigures.getCreditsNumber() + reconcFigures.getDebitsNumber() +
                reconcFigures.getCreditsReversalNumber() + reconcFigures.getDebitsReversalNumber());

        dailyBatch.setTotalAmount(dailyBatch.getTips().amount + dailyBatch.getSubTotalAmount() + dailyBatch.getSurcharge().amount );
        dailyBatch.setTotalCount(dailyBatch.getTips().count  + dailyBatch.getSubTotalCount() + dailyBatch.getSurcharge().count );

        // store the reconciliation transaction list (for printing)
        dailyBatch.setRecTransList(recTransList);

        //Store card scheme totals here
        dailyBatch.setPreviousSchemeTotals(getCardSchemeTotals(dailyBatch, dependency.getPayCfg().isSignatureSupported()));
        return dailyBatch;
    }

    private HashMap<String, Reconciliation.CardSchemeTotals> getCardSchemeTotals(Reconciliation reconciliationTotals, boolean surchargeSupported) {
        if (reconciliationTotals == null)
            return new HashMap<>();

        HashMap<String, Reconciliation.CardSchemeTotals> schemeTotals = new HashMap<>(reconciliationTotals.getPreviousSchemeTotals());

        ArrayList<TransRec> transList = reconciliationTotals.getRecTransList();

        if (transList == null || transList.size() == 0)
            return schemeTotals;

        for (TransRec trans : transList) {
            String schemeName = (trans.getCard() != null) ? trans.getCard().getCardIssuer().getDisplayName() : UNKNOWN.getDisplayName();
            Reconciliation.CardSchemeTotals total = schemeTotals.get(schemeName);
            if (total != null) {
                addTransToTotals(total, trans, surchargeSupported);
            } else {
                Reconciliation.CardSchemeTotals newEntry = new Reconciliation.CardSchemeTotals();
                addTransToTotals(newEntry, trans, surchargeSupported);
                schemeTotals.put(newEntry.name, newEntry);
            }
        }

        return schemeTotals;

    }

    private static void addTransToTotals(Reconciliation.CardSchemeTotals totals, TransRec trans, boolean surchargeSupported) {

        // don't count if( not approved OR this txn is reversal OR original txn was reversed )
        if (!trans.isApproved() || trans.isReversal() || trans.getProtocol().getReversalCount() > 0)
            return;

        totals.name = (trans.getCard() != null) ? trans.getCard().getCardIssuer().getDisplayName() : UNKNOWN.getDisplayName();
        totals.totalCount++;

        EngineManager.TransClass transClass = trans.getTransType().getTransClass();

        if (transClass == EngineManager.TransClass.DEBIT) {
            // e.g. sale, cashout
            totals.totalAmount += trans.getAmounts().getTotalAmount();
        } else {
            // e.g. refund
            totals.totalAmount -= trans.getAmounts().getTotalAmount();
        }

        // increment purchase/cashout/refund totals
        if( trans.isSale() ) {
            totals.purchaseAmount += trans.getAmounts().getTotalAmount();
            totals.purchaseCount++;
        } else if( trans.isCash() ) {
            totals.cashoutAmount += trans.getAmounts().getTotalAmount();
            totals.cashoutCount++;
        } else if( trans.isRefund() ) {
            totals.refundAmount += trans.getAmounts().getTotalAmount();
            totals.refundCount++;
        } else if( trans.isCashback() ) {
            // increment both purchase and cashout amount for pwcb
            totals.purchaseAmount += trans.getAmounts().getTotalAmountWithoutCashback();
            totals.purchaseCount++;
            totals.cashoutAmount += trans.getAmounts().getCashbackAmount();
            totals.cashoutCount++;
        } else if (trans.isCompletion()) {
            totals.completionAmount += trans.getAmounts().getTotalAmount();
            totals.completionCount++;
        }
        addSurchargeTotals(totals, trans, surchargeSupported);
    }

    public Reconciliation AddSummaryTrans(boolean isRec, Reconciliation rec, TransRec summary) {

        if (summary == null) {
            return rec;
        }

        Reconciliation summaryRec = reconciliationDao.findByTransId( summary.getUid() );
        if (summaryRec == null) {
            return rec;
        }
        TReconciliationFigures summaryFigures = summaryRec.getReconciliationFigures();
        TReconciliationFigures recFigs = rec.getReconciliationFigures();

        recFigs.AddFigures(summaryFigures);
        rec.setReconciliationFigures(recFigs);

        if (rec.getEndReceiptTran() == 0) {
            rec.setEndTran(summaryRec.getEndTran());
            rec.setEndReceiptTran(summaryRec.getEndReceiptTran());
        }

        rec.setStartTran(summaryRec.getStartTran());
        rec.setStartReceiptTran(summaryRec.getStartReceiptTran());

        rec.AddValues(summaryRec);

        // sub/total amounts/counts for receipt
        rec.setSubTotalAmount(rec.getSubTotalAmount() + summaryRec.getSubTotalAmount());
        rec.setSubTotalCount(rec.getSubTotalCount() + summaryRec.getSubTotalCount());

        rec.setTotalAmount(rec.getTotalAmount() + summaryRec.getTotalAmount());
        rec.setTotalCount(rec.getTotalCount() + summaryRec.getSubTotalCount());

        if (isRec) {
            reconciliationDao.delete( summaryRec );
            TransRecManager.getInstance().getTransRecDao().delete(summary);
        }

        return rec;
    }

    private static void addSurchargeTotals(Reconciliation.CardSchemeTotals totals, TransRec trans, boolean surchargeSupported) {
       if (surchargeSupported && trans.getAmounts().getSurcharge() > 0) {
           totals.surchargeAmount += trans.getAmounts().getSurcharge();
           totals.surchargeCount++;
        }
    }
}