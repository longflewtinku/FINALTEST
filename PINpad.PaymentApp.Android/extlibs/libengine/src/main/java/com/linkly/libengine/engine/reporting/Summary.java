//package com.linkly.libengine.engine.reporting;
//
//import com.linkly.libengine.engine.transactions.TransRec;
//import com.linkly.libengine.engine.transactions.properties.TReconciliationFigures;
//import static com.linkly.libengine.engine.EngineManager.TransType.SUMMARY;
//import static com.linkly.libengine.engine.reporting.ReconciliationManager.reconciliationDao;
//
//public class Summary {
//    public static boolean SummarizeTransactions() {
//
//        DailyBatch dailyBatch = new DailyBatch();
//
//        TransRec newSummaryTrans = new TransRec(SUMMARY);
//        TransRec currentSummaryTrans = transRecDao.getLatestByTransType( SUMMARY );
//
//        Reconciliation newSummaryRecTotals = dailyBatch.generateDailyBatch(false, true);
//
//        TReconciliationFigures newSummaryFigures = newSummaryRecTotals.getReconciliationFigures();
//
//        if (currentSummaryTrans != null) {
//
//            Reconciliation currentSummaryRecTotals = reconciliationDao.findByTransId( currentSummaryTrans.getUid() );
//            if (currentSummaryRecTotals != null) {
//                TReconciliationFigures currentSummaryFigures = currentSummaryRecTotals.getReconciliationFigures();
//
//                newSummaryFigures.AddFigures(currentSummaryFigures);
//                newSummaryRecTotals.setReconciliationFigures(newSummaryFigures);
//
//                if (newSummaryRecTotals.getEndReceiptTran() == 0) {
//                    newSummaryRecTotals.setEndTran(currentSummaryRecTotals.getEndTran());
//                    newSummaryRecTotals.setEndReceiptTran(currentSummaryRecTotals.getEndReceiptTran());
//                }
//
//                newSummaryRecTotals.setStartTran(currentSummaryRecTotals.getStartTran());
//                newSummaryRecTotals.setStartReceiptTran(currentSummaryRecTotals.getStartReceiptTran());
//                newSummaryRecTotals.AddValues(currentSummaryRecTotals);
//
//                // sub/total amounts/counts for receipt
//                newSummaryRecTotals.setSubTotalAmount(currentSummaryRecTotals.getSubTotalAmount() + newSummaryRecTotals.getSubTotalAmount());
//                newSummaryRecTotals.setSubTotalCount(currentSummaryRecTotals.getSubTotalCount() + newSummaryRecTotals.getSubTotalCount());
//
//                newSummaryRecTotals.setTotalAmount(currentSummaryRecTotals.getTotalAmount() + newSummaryRecTotals.getTotalAmount());
//                newSummaryRecTotals.setTotalCount(currentSummaryRecTotals.getTotalCount() + newSummaryRecTotals.getTotalCount());
//
//                // store the reconciliation transaction list (for printing)
//                if (currentSummaryRecTotals.getRecTransList() != null) {
//                    newSummaryRecTotals.getRecTransList().addAll(currentSummaryRecTotals.getRecTransList());
//                }
//
//                reconciliationDao.delete(currentSummaryRecTotals);
//            }
//            transRecDao.delete(currentSummaryTrans);
//        }
//
//        // We don't care about the receipt number here
//        newSummaryTrans.getAudit().setReceiptNumber(0);
//        newSummaryRecTotals.setReceiptNumber(0);
//        newSummaryTrans.save();
//        newSummaryRecTotals.save(newSummaryTrans);
//
//        return true;
//    }
//
//}
