package com.linkly.libconfig.cpat.Woolworths;

import static com.linkly.libconfig.cpat.Woolworths.ProcessingSpecificationCode.REJECT_TRANS;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ProcessingSpecificationCodeTest {
    @Test
    public void simpleTest() {
        assertEquals( REJECT_TRANS, ProcessingSpecificationCode.getPsc( "00" ) );
    }

    @Test(expected = IllegalArgumentException.class)
    public void illegalCharsTest() {
        ProcessingSpecificationCode.getPsc( "AA" );
    }

    @Test(expected = IllegalArgumentException.class)
    public void illegalLengthTest() {
        ProcessingSpecificationCode.getPsc( "123" );
    }

}