package com.linkly.libengine.engine.protocol;

import com.linkly.libengine.dependencies.IDependency;
import com.linkly.libengine.engine.protocol.iso8583.As2805EftexProto;
import com.linkly.libengine.engine.protocol.iso8583.As2805SuncorpProto;
import com.linkly.libengine.engine.protocol.iso8583.As2805TillProto;
import com.linkly.libengine.engine.protocol.iso8583.As2805WoolworthsProto;
import com.linkly.libengine.engine.protocol.iso8583.DemoProto;
import com.linkly.libengine.engine.transactions.TransRec;
import com.linkly.libengine.engine.transactions.properties.TProtocol;
import com.linkly.libsecapp.emv.Tag;

import java.util.ArrayList;

import timber.log.Timber;

public class Protocol implements IProto {
    private TaskProtocolType protocolType;
    private IProto proto;

    public boolean init(IDependency dependencies) {
        this.protocolType = dependencies.getCustomer().getProtocolType();
        switch( protocolType ) {
            case AS2805_SUNCORP: proto = new As2805SuncorpProto(); break;
            case AS2805_WOOLWORTHS: proto = new As2805WoolworthsProto(); break;
            case AS2805_EFTEX: proto = new As2805EftexProto(); break;
            case AS2805_TILL: proto = new As2805TillProto(); break;
            case DEMO: proto = new DemoProto(); break;

            default:
                Timber.e( "protocol [%s] not implemented", protocolType.name() );
                return false;
        }

        Timber.e( "Protocol selected: [%s]", protocolType.name() );
        return proto.init(dependencies);
    }

    /* perform any tasks that this protocol must do before doing an auth */
    public boolean preAuthorize(TransRec trans) {
        return proto.preAuthorize(trans);
    }

    /* perform an authorization for this particular transaction */
    /* if its set to an advice then no need to go online */
    /* once we are about to go online then set flag to say it has been sent */
    /* if we need to sendTCP reversals and make it a new transaction message then we do so */

    public boolean authorize(TransRec trans) {
        return proto.authorize(trans);
    }

    /**
     * authorise transaction offline, either pre-comms or post-comms, depending on authMethod
     *
     * @param trans trans record
     * @param authMethod authorisation method - pre comms, post comms
     */
    @Override
    public void authorizeOffline(TransRec trans, TProtocol.AuthMethod authMethod) { proto.authorizeOffline(trans, authMethod);}

    /* perform any tasks that this protocol must do after doing an auth */
    /* for example the final info message on openway */
    public boolean postAuthorize(TransRec trans) {
        return proto.postAuthorize(trans);
    }


    /* go through the transactions in the DB and upload as many as possible */
    /* if protocol state is not finalised then sendTCP to protocol to  deal with */
    public ProtoResult batchUpload(boolean silent) {
        return proto.batchUpload(silent);
    }


    @Override
    public String saveEmvTagValuesForDB(IDependency d, TransRec trans) {
        if (proto != null) {
            return proto.saveEmvTagValuesForDB(d, trans);
        }
        return null;
    }
    public String saveCtlsTagValuesForDB(IDependency d, TransRec trans) {
        if (proto != null) {
            return proto.saveCtlsTagValuesForDB(d, trans);
        }
        return null;
    }

    @Override
    public void saveSignatureDeclined( TransRec transRec ) {
        if( proto != null ){
            proto.saveSignatureDeclined( transRec );
        }
    }

    @Override
    public boolean performProtocolChecks() {
        if( proto != null ){
            return proto.performProtocolChecks();
        }
        return false;
    }

    @Override
    public void setInternalRejectReason( TransRec trans, RejectReasonType rejectReasonType ) {
        trans.getAudit().setRejectReasonType( rejectReasonType );
        if( proto != null ) {
            proto.setInternalRejectReason(trans, rejectReasonType);
        } else {
            Timber.e( "proto was NULL" );
        }
    }

    @Override
    public void setInternalRejectReason(TransRec trans, RejectReasonType rejectReasonType, String errorText) {
        trans.getAudit().setRejectReasonType( rejectReasonType );
        if( proto != null ) {
            proto.setInternalRejectReason(trans, rejectReasonType, errorText);
        } else {
            Timber.e( "proto was NULL" );
        }
    }


    @Override
    public boolean timeSync(TransRec trans) {
        return proto.timeSync(trans);
    }

    public ArrayList<Tag> getEmvTagList() {
        if (proto != null) {
            return proto.getEmvTagList();
        }
        return null;
    }

    @Override
    public boolean discountVoucherRedeem(TransRec trans) { return proto.discountVoucherRedeem(trans); }

    @Override
    public boolean discountVoucherReverse(TransRec trans) { return proto.discountVoucherReverse(trans); }

    @Override
    public String encryptCardData(IDependency d, TransRec trans) { return proto.encryptCardData(d, trans); }

    public String getEmvProcessingCode(TransRec trans) {
        return proto.getEmvProcessingCode(trans);
    }

    public String calculateRRN(TransRec trans) {
        return proto.calculateRRN(trans);
    }

    public byte[] getLastTxMessage() { return proto.getLastTxMessage(); }
    public byte[] getLastRxMessage() { return proto.getLastRxMessage(); }
    public boolean requiresDeclinedAdvices() {
        return proto.requiresDeclinedAdvices();
    }
    public int getMaxBatchNumber() { return proto.getMaxBatchNumber(); }

    @Override
    public TransRec lookupOriginalTransaction(String txnReference) {
        return proto.lookupOriginalTransaction(txnReference);
    }

    public TaskProtocolType getProtocolType() {
        return this.protocolType;
    }

    public IProto getProto() {
        return this.proto;
    }
}
