import com.linkly.libengine.engine.transactions.TransRec;

import org.junit.Assert;
import org.junit.Test;

import java.util.Calendar;

public class DBTransPurgeTest {

        private long convertDateTimeToMillis( int year, int month, int day, int hour, int minute, int second ) {
                Calendar dateTime = Calendar.getInstance();
                dateTime.set( Calendar.DAY_OF_MONTH, day );
                dateTime.set( Calendar.MONTH, month );
                dateTime.set( Calendar.YEAR, year );
                dateTime.set( Calendar.HOUR_OF_DAY, hour );
                dateTime.set( Calendar.MINUTE, minute );
                dateTime.set( Calendar.SECOND, second );
                return dateTime.getTimeInMillis();
        }

        @SuppressWarnings("static")
        @Test
        public void TestExpiryOverMidnight() {

                TransRec transRec = new TransRec();

                long transDateTime = convertDateTimeToMillis( 2022, 3, 2, 17, 26, 42 );

                long expiryTime = transRec.getTransExpiryTime( transDateTime, 7 );

                // test before expiry
                Assert.assertTrue( convertDateTimeToMillis( 2022, 3, 9, 17, 26, 42 ) < expiryTime );

                // test after expiry
                Assert.assertTrue( convertDateTimeToMillis( 2022, 3, 10, 17, 26, 42 ) > expiryTime );

                // test before midnight before expiry day (not expired)
                Assert.assertTrue( convertDateTimeToMillis( 2022, 3, 9, 23, 59, 42 ) < expiryTime );

                // test just after midnight on expiry day (expired)
                Assert.assertTrue( convertDateTimeToMillis( 2022, 3, 10, 0, 0, 5 ) > expiryTime );

        }
}
