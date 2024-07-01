package com.linkly.libengine.engine.transactions;

import androidx.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.linkly.libengine.engine.EngineManager;
import com.linkly.libengine.engine.protocol.IProto;
import com.linkly.libengine.engine.transactions.properties.TAmounts;
import com.linkly.libengine.engine.transactions.properties.TAudit;
import com.linkly.libengine.engine.transactions.properties.TCard;
import com.linkly.libengine.engine.transactions.properties.TProtocol;
import com.linkly.libengine.engine.transactions.properties.TSec;
import com.linkly.libpositive.wrappers.PositiveTransResult;
import com.linkly.libpositive.wrappers.TagDataToPOS;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;

public class TransRecConverters {
    // converters for TProtocol
    @TypeConverter
    public String protocolObjToString(TProtocol input) {
        Gson gson = new Gson();
        String ret = gson.toJson(input);
        return ret;
    }

    @TypeConverter
    public TProtocol stringToProtocolObj( String serialisedObject ) {
        Gson gson = new Gson();
        TProtocol ret = gson.fromJson(serialisedObject, TProtocol.class);
        return ret;
    }

    // converters for TCard
    @TypeConverter
    public String cardObjToString(TCard input) {
        Gson gson = new Gson();
        String ret = gson.toJson(input);
        return ret;
    }

    @TypeConverter
    public TCard stringToCardObj( String serialisedObject ) {
        Gson gson = new Gson();
        TCard ret = gson.fromJson(serialisedObject, TCard.class);
        return ret;
    }

    // converters for TAmounts
    @TypeConverter
    public String amountsObjToString(TAmounts input) {
        Gson gson = new Gson();
        String ret = gson.toJson(input);
        return ret;
    }

    @TypeConverter
    public TAmounts stringToAmountsObj( String serialisedObject ) {
        Gson gson = new Gson();
        TAmounts ret = gson.fromJson(serialisedObject, TAmounts.class);
        return ret;
    }

    // converters for TAudit
    @TypeConverter
    public String auditObjToString(TAudit input) {
        Gson gson = new Gson();
        String ret = gson.toJson(input);
        return ret;
    }

    @TypeConverter
    public TAudit stringToAuditObj( String serialisedObject ) {
        Gson gson = new Gson();
        TAudit ret = gson.fromJson(serialisedObject, TAudit.class);
        return ret;
    }

    // converters for TSec
    @TypeConverter
    public String secObjToString(TSec input) {
        Gson gson = new Gson();
        String ret = gson.toJson(input);
        return ret;
    }

    @TypeConverter
    public TSec stringToSecObj( String serialisedObject ) {
        Gson gson = new Gson();
        TSec ret = gson.fromJson(serialisedObject, TSec.class);
        return ret;
    }

    // converters for TransType
    @TypeConverter
    public int transTypeToInt(EngineManager.TransType input) {
        return input.ordinal();
    }

    @TypeConverter
    public EngineManager.TransType intToTransType( int input ) {
        return EngineManager.TransType.values()[input];
    }

    // converters for MessageStatus
    @TypeConverter
    public int messageStatusToInt(TProtocol.MessageStatus input) {
        return input.ordinal();
    }

    @TypeConverter
    public TProtocol.MessageStatus intToMessageStatus(int input ) {
        return TProtocol.MessageStatus.values()[input];
    }

    // converters for TProtocol.ReversalState
    @TypeConverter
    public int reversalStateToInt(TProtocol.ReversalState input) {
        return input.ordinal();
    }

    @TypeConverter
    public TProtocol.ReversalState intToReversalState(int input ) {
        return TProtocol.ReversalState.values()[input];
    }

    // HostResult
    @TypeConverter
    public int hostResultToInt(TProtocol.HostResult input) {
        return input.ordinal();
    }

    @TypeConverter
    public TProtocol.HostResult intToHostResult(int input ) {
        return TProtocol.HostResult.values()[input];
    }

    // AuthMethod
    @TypeConverter
    public int authMethodToInt(TProtocol.AuthMethod input) {
        return input.ordinal();
    }

    @TypeConverter
    public TProtocol.AuthMethod intToAuthMethod(int input ) {
        return TProtocol.AuthMethod.values()[input];
    }

    // ReversalReason
    @TypeConverter
    public int reversalReasonToInt(TProtocol.ReversalReason input) {
        return input.ordinal();
    }

    @TypeConverter
    public TProtocol.ReversalReason intToReversalReason(int input ) {
        return TProtocol.ReversalReason.values()[input];
    }

    // AuthEntity
    @TypeConverter
    public int authEntityToInt(TProtocol.AuthEntity input) {
        return input.ordinal();
    }

    @TypeConverter
    public TProtocol.AuthEntity intToAuthEntity(int input ) {
        return TProtocol.AuthEntity.values()[input];
    }

    // serverDateTime
    @TypeConverter
    public long dateTimeToLong(Date input) {
        if( input == null )
            return 0;

        return input.getTime();
    }

    @TypeConverter
    public Date longToDateTime( long input ) {
        return new Date(input);
    }

    // TCard.CardType
    @TypeConverter
    public int cardTypeToInt(TCard.CardType input) {
        return input.ordinal();
    }

    @TypeConverter
    public TCard.CardType intToCardType(int input ) {
        return TCard.CardType.values()[input];
    }

    // TCard.CvmType
    @TypeConverter
    public int cvmTypeToInt(TCard.CvmType input) {
        return input.ordinal();
    }

    @TypeConverter
    public TCard.CvmType intToCvmType(int input ) {
        return TCard.CvmType.values()[input];
    }

    // TCard.CaptureMethod
    @TypeConverter
    public int captureMethodToInt(TCard.CaptureMethod input) {
        return input.ordinal();
    }

    @TypeConverter
    public TCard.CaptureMethod intToCaptureMethod(int input ) {
        return TCard.CaptureMethod.values()[input];
    }


    // EngineManager.TransClass
    @TypeConverter
    public int transClassToInt(EngineManager.TransClass input) {
        return input.ordinal();
    }

    @TypeConverter
    public EngineManager.TransClass intToTransClass(int input ) {
        return EngineManager.TransClass.values()[input];
    }

    // RejectReasonType
    @TypeConverter
    public int rejectReasonTypeToInt( IProto.RejectReasonType input) {
        return input.ordinal();
    }

    @TypeConverter
    public IProto.RejectReasonType intToRejectReasonType( int input ) {
        return IProto.RejectReasonType.values()[input];
    }

    // ReasonOnlineCode
    @TypeConverter
    public int reasonOnlineCodeToInt(TAudit.ReasonOnlineCode input) {
        return input.ordinal();
    }

    @TypeConverter
    public TAudit.ReasonOnlineCode intToReasonOnlineCode(int input ) {
        return TAudit.ReasonOnlineCode.values()[input];
    }

    @TypeConverter
    public ArrayList<PositiveTransResult.Receipt> fromReceiptString(String value) {
        Type listType = new TypeToken<ArrayList<PositiveTransResult.Receipt>>() {}.getType();
        return new Gson().fromJson(value, listType);
    }

    @TypeConverter
    public String fromReceiptArrayList(ArrayList<PositiveTransResult.Receipt> list) {
        Gson gson = new Gson();
        return gson.toJson(list);
    }

    @TypeConverter
    public ArrayList<String> fromString(String value) {
        Type listType = new TypeToken<ArrayList<String>>() {}.getType();
        return new Gson().fromJson(value, listType);
    }

    @TypeConverter
    public String fromArrayList(ArrayList<String> list) {
        Gson gson = new Gson();
        return gson.toJson(list);
    }

    // converters for TagDataToPOS
    @TypeConverter
    public String tagDataToString(TagDataToPOS input) {
        Gson gson = new Gson();
        return gson.toJson(input);
    }

    @TypeConverter
    public TagDataToPOS stringToTagData( String serialisedObject ) {
        Gson gson = new Gson();
        return gson.fromJson(serialisedObject, TagDataToPOS.class);
    }

}
