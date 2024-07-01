//package eft.com.positive.database;
//
//
//import androidx.test.platform.app.InstrumentationRegistry;
//
//import com.linkly.libengine.config.Config;
//import com.linkly.libengine.dependencies.Dependencies;
//import com.linkly.libengine.engine.Engine;
//import com.linkly.libengine.engine.reporting.Reconciliation;
//import com.linkly.libengine.status.StatusReport;
//import com.linkly.libmal.MalFactory;
//import com.linkly.libpositive.messages.Messages;
//
//import org.junit.Before;
//import org.junit.Test;
//
//import java.util.HashMap;
//
//import static org.junit.Assert.assertEquals;
//
///**
// * Example local unit test, which will execute on the development machine (host).
// *
// * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
// */
//public class CardSchemeTotalsTests {
//    private static final String TAG = "CardSchemeTotalsTests";
////
////    @Before
////    public void setUp() throws Exception {
////        // set up a minimal set of dependencies to run the tests
////        Dependencies d = new Dependencies();
////        d.setMal( MalFactory.getInstance() );
////        d.setContext(InstrumentationRegistry.getInstrumentation().getTargetContext());
////        d.setConfig(Config.getInstance());
////        d.setStatusReporter(StatusReport.getInstance());
////        d.setMessages(Messages.getInstance());
////        Engine.Init(d);
////    }
////
//    @Test
//    public void testSerialisation() {
//        try {
//            Reconciliation rec = new Reconciliation();
//            rec.getSale().amount = 3300;
//            rec.getSale().count = 3;
//
//            HashMap<String, Reconciliation.CardSchemeTotals> cardTotals = new HashMap<>();
//            cardTotals.put( "VISA", new Reconciliation.CardSchemeTotals( "VISA", 1000, 1 ));
//            cardTotals.put( "MASTERCARD", new Reconciliation.CardSchemeTotals( "MASTERCARD", 1100, 1 ));
//            cardTotals.put( "EFTPOS", new Reconciliation.CardSchemeTotals( "EFTPOS", 1200, 1 ));
//
//            rec.setPreviousSchemeTotals( cardTotals );
//
//            String packedCardSchemeTotals = rec.getPreviousSchemeTotalsData();
//            assertEquals("some data here", packedCardSchemeTotals);
//
//        } catch (IllegalArgumentException e) {
//            throw e;
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//
//}
//
