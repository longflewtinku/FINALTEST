package eft.com.eftcore;//package eft.com.positive.database;


import static org.junit.Assert.assertEquals;

import com.linkly.libengine.engine.reporting.Reconciliation;

import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class CardSchemeTotalsTests {
    private static final String TAG = "CardSchemeTotalsTests";
//
//    @Before
//    public void setUp() throws Exception {
//        // set up a minimal set of dependencies to run the tests
//        Dependencies d = new Dependencies();
//        d.setMal( MalFactory.getInstance() );
//        d.setContext(InstrumentationRegistry.getInstrumentation().getTargetContext());
//        d.setConfig(Config.getInstance());
//        d.setStatusReporter(StatusReport.getInstance());
//        d.setMessages(Messages.getInstance());
//        Engine.Init(d);
//    }
//
    @Test
    public void testSerialisation() {
        try {
            Reconciliation rec = new Reconciliation( true );
            rec.getSale().amount = 3300;
            rec.getSale().count = 3;

            HashMap<String, Reconciliation.CardSchemeTotals> cardTotals = new HashMap<>();
            cardTotals.put( "VISA", new Reconciliation.CardSchemeTotals( "VISA", 400, 1, 600, 2, 0, 0,200,1,1200, 4, 48, 1 ));
            cardTotals.put( "MASTERCARD", new Reconciliation.CardSchemeTotals( "MASTERCARD", 0, 0, 600, 2, 1234, 11,300,1, -934, 14, 63, 2 ));
            cardTotals.put( "EFTPOS", new Reconciliation.CardSchemeTotals( "EFTPOS", 100, 1, 200, 2, 700, 3, 400,2,0, 8,174, 3 ));

            rec.setPreviousSchemeTotals( cardTotals );

            String packedCardSchemeTotals = rec.getPreviousSchemeTotalsData();

            assertEquals("[{\"name\":\"EFTPOS\",\"purchaseAmount\":100,\"purchaseCount\":1,\"cashoutAmount\":200,\"cashoutCount\":2,\"refundAmount\":700,\"refundCount\":3,\"completionAmount\":400,\"completionCount\":2,\"totalAmount\":0,\"totalCount\":8,\"surchargeAmount\":174,\"surchargeCount\":3},"+
                            "{\"name\":\"MASTERCARD\",\"purchaseAmount\":0,\"purchaseCount\":0,\"cashoutAmount\":600,\"cashoutCount\":2,\"refundAmount\":1234,\"refundCount\":11,\"completionAmount\":300,\"completionCount\":1,\"totalAmount\":-934,\"totalCount\":14,\"surchargeAmount\":63,\"surchargeCount\":2},"+
                            "{\"name\":\"VISA\",\"purchaseAmount\":400,\"purchaseCount\":1,\"cashoutAmount\":600,\"cashoutCount\":2,\"refundAmount\":0,\"refundCount\":0,\"completionAmount\":200,\"completionCount\":1,\"totalAmount\":1200,\"totalCount\":4,\"surchargeAmount\":48,\"surchargeCount\":1}]",
                    packedCardSchemeTotals);

        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testDeserialisation() {
        try {
            Reconciliation rec = new Reconciliation( true );
            rec.getSale().amount = 3300;
            rec.getSale().count = 3;

            HashMap<String, Reconciliation.CardSchemeTotals> cardTotals = new HashMap<>();
            cardTotals.put( "VISA", new Reconciliation.CardSchemeTotals( "VISA", 400, 1, 600, 2, 0, 0,200,1,1200, 4, 11, 2 ));
            cardTotals.put( "MASTERCARD", new Reconciliation.CardSchemeTotals( "MASTERCARD", 0, 0, 600, 2, 1234, 11,300,1, -934, 14,166,8 ));
            cardTotals.put( "EFTPOS", new Reconciliation.CardSchemeTotals( "EFTPOS", 100, 1, 200, 2, 700, 3, 400,2,0, 8, 274, 15 ));

            rec.setPreviousSchemeTotals( cardTotals );

            String packedCardSchemeTotals = rec.getPreviousSchemeTotalsData();
            assertEquals("[{\"name\":\"EFTPOS\",\"purchaseAmount\":100,\"purchaseCount\":1,\"cashoutAmount\":200,\"cashoutCount\":2,\"refundAmount\":700,\"refundCount\":3,\"completionAmount\":400,\"completionCount\":2,\"totalAmount\":0,\"totalCount\":8,\"surchargeAmount\":274,\"surchargeCount\":15},"+
                            "{\"name\":\"MASTERCARD\",\"purchaseAmount\":0,\"purchaseCount\":0,\"cashoutAmount\":600,\"cashoutCount\":2,\"refundAmount\":1234,\"refundCount\":11,\"completionAmount\":300,\"completionCount\":1,\"totalAmount\":-934,\"totalCount\":14,\"surchargeAmount\":166,\"surchargeCount\":8},"+
                            "{\"name\":\"VISA\",\"purchaseAmount\":400,\"purchaseCount\":1,\"cashoutAmount\":600,\"cashoutCount\":2,\"refundAmount\":0,\"refundCount\":0,\"completionAmount\":200,\"completionCount\":1,\"totalAmount\":1200,\"totalCount\":4,\"surchargeAmount\":11,\"surchargeCount\":2}]",
                    packedCardSchemeTotals);

            // now convert back from string to array
            ArrayList<Reconciliation.CardSchemeTotals> cardTotalsArray = rec.getPreviousSchemeTotalsAsArray();
            assertEquals( "EFTPOS", cardTotalsArray.get(0).name );
            assertEquals( "MASTERCARD", cardTotalsArray.get(1).name );
            assertEquals( "VISA", cardTotalsArray.get(2).name );
            assertEquals( 100, cardTotalsArray.get(0).purchaseAmount );
            assertEquals( -934, cardTotalsArray.get(1).totalAmount );
            assertEquals( 4, cardTotalsArray.get(2).totalCount );

            // get as hashmap
            HashMap<String, Reconciliation.CardSchemeTotals> hashMap = rec.expandStringIntoSchemeTotals(packedCardSchemeTotals);
            assertEquals( "EFTPOS", hashMap.get("EFTPOS").name );
            assertEquals( "MASTERCARD", hashMap.get("MASTERCARD").name );
            assertEquals( "VISA", hashMap.get("VISA").name );
            assertEquals( 100, hashMap.get("EFTPOS").purchaseAmount );
            assertEquals( -934, hashMap.get("MASTERCARD").totalAmount );
            assertEquals( 4, hashMap.get("VISA").totalCount );


        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

