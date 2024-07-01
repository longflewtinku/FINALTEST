package com.linkly.libengine.engine.protocol.iso8583;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import com.linkly.libengine.engine.protocol.IProto;
import com.linkly.libengine.engine.transactions.properties.TProtocol;

import org.junit.Test;

public class As2805WoolworthsRspCodeMapTest {
    private final As2805WoolworthsRspCodeMap RSP_CODE_MAP = new As2805WoolworthsRspCodeMap();


    @Test
    public void sanityTests() {
        TProtocol protocol = RSP_CODE_MAP.populateProtocolRecord( null, "00" );

        assertNull( protocol );
    }

    @Test
    public void fieldIsPresentTest(){
        TProtocol tProtocol = new TProtocol( );

        assertNotNull( tProtocol );

        // Most recent addition
        tProtocol = RSP_CODE_MAP.populateProtocolRecord( tProtocol, "00" );
        assertNotNull( tProtocol );
        assertEquals( "00", tProtocol.getServerResponseCode() );
        assertEquals( "Approved", tProtocol.getAdditionalResponseText() );
        assertEquals( "APPROVED", tProtocol.getCardAcceptorPrinterData() );
        assertEquals( "APPROVED", tProtocol.getPosResponseText() );

    }

    @Test
    public void fieldIsNotPresent() {
        TProtocol tProtocol = new TProtocol( );

        assertNotNull( tProtocol );

        // Definitely not present
        tProtocol = RSP_CODE_MAP.populateProtocolRecord( tProtocol, "AA" );
        assertNotNull( tProtocol );
        assertEquals( "AA", tProtocol.getServerResponseCode() );
        assertEquals( "System Error", tProtocol.getAdditionalResponseText() );
        assertEquals( "", tProtocol.getCardAcceptorPrinterData() );
        assertEquals( "SYSTEM ERROR AA", tProtocol.getPosResponseText() );
    }

    @Test
    public void ensureOtherFieldsPresentTest() {
        TProtocol tProtocol = new TProtocol( );

        assertNotNull( tProtocol );
        tProtocol.setAuthCode( "30" );

        tProtocol = RSP_CODE_MAP.populateProtocolRecord( tProtocol, "X7" );
        assertNotNull( tProtocol );
        assertEquals( "30", tProtocol.getAuthCode() );
        assertEquals( "X7", tProtocol.getServerResponseCode() );

        tProtocol = RSP_CODE_MAP.populateProtocolRecord( tProtocol, "ZZ" );
        assertNotNull( tProtocol );
        assertEquals( "30", tProtocol.getAuthCode() );
    }

    @Test
    public void simpleRejectReasonTypeTest() {
        TProtocol tProtocol = new TProtocol();

        assertNotNull( tProtocol );

        tProtocol = RSP_CODE_MAP.populateProtocolRecord( tProtocol, IProto.RejectReasonType.COMMS_ERROR );
        assertNotNull( tProtocol );
        assertEquals( "X0", tProtocol.getServerResponseCode() );
        assertEquals( "Cancelled\nNo Response", tProtocol.getAdditionalResponseText() );
    }

    @Test
    public void rejectReasonNotFoundTest() {
        TProtocol tProtocol = new TProtocol( );

        assertNotNull( tProtocol );
        tProtocol.setAuthCode( "30" );

        tProtocol = RSP_CODE_MAP.populateProtocolRecord( tProtocol, IProto.RejectReasonType.ABANDONED );
        assertNotNull( tProtocol );
        assertEquals( "30", tProtocol.getAuthCode() );
        assertNull( tProtocol.getServerResponseCode() );
    }
}
