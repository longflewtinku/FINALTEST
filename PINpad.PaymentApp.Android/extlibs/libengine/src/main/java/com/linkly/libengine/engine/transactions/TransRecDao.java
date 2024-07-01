package com.linkly.libengine.engine.transactions;

import androidx.lifecycle.LiveData;
import androidx.paging.PagingSource;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.RawQuery;
import androidx.room.Update;
import androidx.sqlite.db.SupportSQLiteQuery;

import com.linkly.libengine.engine.EngineManager;
import com.linkly.libengine.engine.transactions.properties.TProtocol;

import java.util.List;

@Dao
public interface TransRecDao {
    // general rules -
    // 'find latest' type scans return ordered by uid desc (newest first in list)
    // 'find' returns a list ordered by uid (oldest first)
    // 'get' returns single record
    // 'getLatest' returns latest single record matching criteria
    // 'find' returns a list
    // 'count' counts multiple records matching criteria

    @Query("SELECT * FROM transRecs")
    List<TransRec> findAll();

    @Query("SELECT * FROM transRecs ORDER BY uid DESC")
    LiveData<List<TransRec>> findAllLatestFirst();

    @Query("SELECT * FROM transRecs ORDER BY uid DESC LIMIT :num OFFSET :start")
    List<TransRec> findXTransFromOffset(int num, int start);

    @Query("SELECT COUNT(*) FROM transRecs")
    long getTransCount();

    @Query("SELECT * FROM transRecs WHERE uid = :uid LIMIT 1")
    TransRec getByUid( int uid );

    @Query("SELECT * FROM transRecs ORDER BY uid DESC LIMIT 1")
    TransRec getLatest();

    @Query("SELECT * FROM transRecs ORDER BY uid DESC LIMIT 1 OFFSET 1")
    TransRec getPrevious();

    @Query("SELECT * FROM transRecs WHERE prot_messageStatus != 0 ORDER BY uid DESC LIMIT 1 OFFSET 1")
    TransRec getPreviousSent();

    @Query("SELECT * FROM transRecs WHERE transType = :ttype ORDER BY uid DESC LIMIT 1")
    TransRec getLatestByTransType( EngineManager.TransType ttype );

    @Query("SELECT * FROM transRecs WHERE transType = :ttype ORDER BY uid DESC")
    List<TransRec> findLatestByTransType( EngineManager.TransType ttype );

    @Query("SELECT * FROM transRecs WHERE transType = :ttype ORDER BY uid DESC LIMIT :limit")
    List<TransRec> findLatestByTransType( EngineManager.TransType ttype, int limit );

    @Query("SELECT * FROM transRecs WHERE transType = :ttype")
    List<TransRec> findAllByTransType( EngineManager.TransType ttype );

    @Query("SELECT * FROM transRecs WHERE transType = :ttype LIMIT :limit")
    List<TransRec> findAllByTransType( EngineManager.TransType ttype, int limit );

    @Query("SELECT * FROM transRecs WHERE transType = :ttype AND approved = :approved")
    List<TransRec> findAllByTransTypeAndApproved( EngineManager.TransType ttype, boolean approved );

    @Query("SELECT * FROM transRecs WHERE summedOrReced = :summedOrReced")
    List<TransRec> findBySummedOrReced( boolean summedOrReced );

    @Query("SELECT * FROM transRecs WHERE cancelled = :cancelled")
    List<TransRec> findByCancelled( boolean cancelled );

    @Query("SELECT * FROM transRecs WHERE prot_messageStatus != :val" )
    List<TransRec> findAllNotMessageStatus( TProtocol.MessageStatus val );

    @Query("SELECT * FROM transRecs WHERE prot_messageStatus = :val" )
    List<TransRec> findAllByMessageStatus( TProtocol.MessageStatus val );

    @Query("SELECT * FROM transRecs WHERE prot_messageStatus IN (:statusList)")
    LiveData<List<TransRec>> findAllByMessageStatus(List<TProtocol.MessageStatus> statusList);

    @Query("SELECT * FROM transRecs WHERE deferredAuth = 1" )
    List<TransRec> findAllDeferredAuths();

    @Query("SELECT * FROM transRecs WHERE audit_receiptNumber = :receiptNumber LIMIT 1")
    TransRec getByReceiptNumber( int receiptNumber );

    @Query("SELECT * FROM transRecs WHERE audit_receiptNumber = :receiptNumber LIMIT 1")
    TransRec getByReceiptNumber( String receiptNumber );

    @Query("SELECT * FROM transRecs WHERE prot_authCode = :authCode LIMIT 1")
    TransRec getByAuthCode( String authCode );

    // returns latest by UTI - duplicate UTIs may exist, e.g. where a record has both original auth/reversal and a subsequent advice
    @Query("SELECT * FROM transRecs WHERE audit_uti = :uti ORDER BY uid DESC LIMIT 1")
    TransRec getByUti( String uti );

    @Query("SELECT COUNT(*) FROM transRecs WHERE audit_userId = :userId")
    long countTransByUser( String userId );

    @Query("SELECT * FROM transRecs WHERE audit_userId = :userId")
    List<TransRec> findByUser( String userId );

    @Query("SELECT * FROM transRecs WHERE prot_reversalState = :revState AND cancelled != :cancelled ORDER BY uid DESC LIMIT 1")
    TransRec getLatestByReversalState(TProtocol.ReversalState revState, boolean cancelled);

    @Query("SELECT * FROM transRecs WHERE transType IN (:list) AND :extraCondition order by audit_TransDateTime desc limit :count")
    List<TransRec> getLastXTransactionsList( List<EngineManager.TransType> list , String extraCondition, int count);

    @Query("SELECT * FROM transRecs WHERE transType IN (:list) AND :extraCondition order by audit_TransDateTime desc")
    List<TransRec> getTransactionsList( List<EngineManager.TransType> list , String extraCondition);

    @Query("SELECT * FROM transRecs WHERE cancelled = 0 AND transType IN ( :list ) ORDER BY uid DESC limit 1")
    TransRec getLatestFromTransTypeList(List<EngineManager.TransType> list);

    @Query("DELETE FROM transRecs WHERE cancelled = :cancelled")
    int deleteByCancelled( boolean cancelled );

    @Query("DELETE FROM transRecs WHERE uid < :uid")
    int deleteTxnsBeforeUid( int uid );

    @Query("DELETE FROM transRecs WHERE uid < :uid AND transType != :transType1 AND transType != :transType2 AND transType != :transType3 AND transType != :transType4")
    int deleteTxnsBeforeUidExceptTransTypes( int uid, EngineManager.TransType transType1, EngineManager.TransType transType2, EngineManager.TransType transType3, EngineManager.TransType transType4 );

    @Query("SELECT COUNT(*) FROM transRecs WHERE prot_messageStatus = :msgStatus AND deferredAuth = 1")
    int countDeferredCount( int msgStatus );

    @Query("SELECT * FROM transRecs WHERE prot_messageStatus = :msgStatus")
    List<TransRec> findAllDeferredAuths( int msgStatus );

    @RawQuery
    int executeIntQuery( SupportSQLiteQuery query );

    @RawQuery
    List<TransRec> executeTransListQuery( SupportSQLiteQuery query );

    @RawQuery
    TransRec executeTransRecQuery( SupportSQLiteQuery query );

    @Insert
    long insert( TransRec transRec );

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void update( TransRec transRec );

    @Delete
    void delete(TransRec transRec);

    @Query("DELETE FROM transRecs")
    void deleteAll();

    @Query("SELECT * FROM transRecs WHERE prot_paxstoreUploaded = :uploaded")
    List<TransRec> getWherePaxstorUploaded(boolean uploaded);

    @RawQuery
    List<Integer> executeListQuery(SupportSQLiteQuery query);

    @Query("DELETE FROM transRecs WHERE uid in (:condition)")
    int deleteTransByCondition(List<Integer> condition);

    @Query("SELECT * FROM transRecs WHERE prot_paxstoreUploaded = :uploaded ORDER BY audit_transFinishedDateTime ASC LIMIT :n")
    List<TransRec> getOldestNPaxstoreUploaded(boolean uploaded, int n);

    @Query("SELECT * FROM transRecs WHERE prot_merchantEmailToUpload = :toUpload ORDER BY audit_transFinishedDateTime ASC LIMIT :n")
    List<TransRec> getOldestNMerchantEmailToUpload(boolean toUpload, int n);

    @Query("SELECT * FROM transRecs WHERE prot_customerEmailToUpload = :toUpload ORDER BY audit_transFinishedDateTime ASC LIMIT :n")
    List<TransRec> getOldestNCustomerEmailToUpload(boolean toUpload, int n);


    @Query( "SELECT * FROM transrecs WHERE transType in  (:ttype1, :ttype2, :ttype3, :ttype4) AND audit_transFinishedDateTime > :oldestTime AND approved = 1" )
    List<TransRec> getTransRecsUntilATime( EngineManager.TransType ttype1, EngineManager.TransType ttype2,  EngineManager.TransType ttype3, EngineManager.TransType ttype4, long oldestTime );

    @Query("SELECT * FROM transRecs WHERE prot_reversalState = :revState AND cancelled != :cancelled ")
    List<TransRec> getAllTransByReversalState(int revState, boolean cancelled);

    @Query("SELECT * FROM transRecs WHERE transType in  (:ttype1, :ttype2, :ttype3, :ttype4) AND audit_terminalId = :tid AND prot_RRN = :rrn AND prot_authCode = :authNumber")
    TransRec findByTidRrnAuthNumber(EngineManager.TransType ttype1, EngineManager.TransType ttype2,  EngineManager.TransType ttype3, EngineManager.TransType ttype4, String tid, String rrn, String authNumber);

    @Query("SELECT * FROM transRecs WHERE transType in (:ttype1, :ttype2, :ttype3, :ttype4) AND prot_RRN = :rrn LIMIT 1")
    TransRec getByTransTypeAndRrn(EngineManager.TransType ttype1, EngineManager.TransType ttype2, EngineManager.TransType ttype3, EngineManager.TransType ttype4, String rrn );

    @Query("SELECT * FROM transRecs WHERE transType in (:ttype1, :ttype2, :ttype3, :ttype4) AND prot_authCode = :authCode LIMIT 1")
    TransRec getByTransTypeAndAuthCode(EngineManager.TransType ttype1, EngineManager.TransType ttype2, EngineManager.TransType ttype3, EngineManager.TransType ttype4, String authCode );

    @Query( "SELECT * FROM transRecs WHERE (transType = :transType1 OR transType = :transType2) AND audit_transFinishedDateTime >= :startTime AND audit_transFinishedDateTime <= :endTime AND approved = 1")
    List<TransRec> findByTypeDateRange( EngineManager.TransType transType1, EngineManager.TransType transType2, long startTime, long endTime );

    @Query("SELECT * FROM transRecs WHERE (transType = :transType1 OR transType = :transType2) AND (prot_RRN = :rrn OR prot_authCode = :authCode) AND approved = 1")
    List<TransRec> findByTypeRrnOrAuthCode( EngineManager.TransType transType1, EngineManager.TransType transType2, String rrn, String authCode );

    @Query("SELECT COUNT(*) FROM transRecs WHERE transType in (:ttype1, :ttype2, :ttype3, :ttype4) AND approved = 1 ")
    long countTransByTypeAndApproved( EngineManager.TransType ttype1,EngineManager.TransType ttype2,EngineManager.TransType ttype3,EngineManager.TransType ttype4 );

    @Query("SELECT * FROM transRecs WHERE transType in (:ttype1, :ttype2, :ttype3, :ttype4) AND approved = 1")
    List<TransRec> getByTransTypesAndApproved( EngineManager.TransType ttype1, EngineManager.TransType ttype2, EngineManager.TransType ttype3, EngineManager.TransType ttype4 );

    @Query("SELECT * FROM transRecs WHERE transType in (:ttype1, :ttype2, :ttype3, :ttype4) AND approved = 1")
    LiveData<List<TransRec>> getByTransTypesAndApprovedLiveData( EngineManager.TransType ttype1, EngineManager.TransType ttype2, EngineManager.TransType ttype3, EngineManager.TransType ttype4 );

    @Query("SELECT * FROM transRecs WHERE audit_reference = :transactionReference LIMIT 1")
    TransRec getByReference(String transactionReference);

    @Query("SELECT COUNT(*) FROM transRecs WHERE prot_messageStatus IN (:messageStatuses) AND approved = 1")
    int getTransCountByMsgStatus(TProtocol.MessageStatus... messageStatuses);

    @Query("SELECT SUM(amts_amount+amts_cashbackAmount+amts_tip+amts_surcharge) FROM transrecs WHERE prot_messageStatus IN (:messageStatuses) AND approved = 1")
    long getTotalAmountWithMsgStatus(TProtocol.MessageStatus... messageStatuses);

    @Query("SELECT * FROM transRecs WHERE prot_messageStatus = :val AND approved = 1" )
    List<TransRec> findAllByMessageStatusAndApproved( TProtocol.MessageStatus val );

    @Query("SELECT * FROM transRecs WHERE transType IN (:list) AND approved = 1")
    List<TransRec> findAllApprovedTrans(List<EngineManager.TransType> list );

    @Query("SELECT COUNT(*) FROM transRecs WHERE prot_messageStatus = :val1 AND (prot_authMethod = :auth1 OR prot_authMethod = :auth2)")
    int getTransCountByMsgStatusAndAuthMethod(TProtocol.MessageStatus val1, TProtocol.AuthMethod auth1, TProtocol.AuthMethod auth2);

    @Query( "SELECT * FROM transRecs WHERE audit_transFinishedDateTime >= :startTime AND audit_transFinishedDateTime < :endTime AND approved = 1")
    List<TransRec> findByDateRangeFinishedApproved(long startTime, long endTime );

    @Query("SELECT  * FROM transRecs WHERE approved = 1 OR declined = 1 ORDER BY audit_transDateTime DESC")
    PagingSource<Integer, TransRec> getTransRecForPager();
}
