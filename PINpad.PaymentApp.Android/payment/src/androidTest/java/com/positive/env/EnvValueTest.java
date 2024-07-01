package com.positive.env;


import static com.linkly.libengine.env.EnvVar.deleteAll;
import static com.linkly.libengine.env.EnvVar.deleteEnvValue;
import static com.linkly.libengine.env.EnvVar.getIntegerAutoIncrement;
import static com.linkly.libengine.env.EnvVar.setEnvValue;
import static com.linkly.libengine.env.EnvVarManager.getEnvVarDao;
import static org.junit.Assert.assertEquals;

import androidx.test.filters.LargeTest;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.runner.AndroidJUnitRunner;

import com.linkly.libengine.config.Config;
import com.linkly.libengine.dependencies.Dependencies;
import com.linkly.libengine.engine.Engine;
import com.linkly.libengine.env.BatchNumber;
import com.linkly.libengine.env.EnvVar;
import com.linkly.libengine.env.EnvVarManager;
import com.linkly.libengine.env.ReceiptNumber;
import com.linkly.libengine.status.StatusReport;
import com.linkly.libmal.MalFactory;
import com.linkly.libpositive.messages.Messages;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.util.List;


@Ignore("Ignoring as Integration")
@SuppressWarnings("deprecated")
@LargeTest
public class EnvValueTest extends AndroidJUnitRunner {
    private static final String TAG = "EnvValueTest";

    @BeforeClass
    public static void setUp() throws Exception {
        // set up a minimal set of dependencies to run the tests
        Dependencies d = new Dependencies();
        d.setConfig(Config.getInstance());
        d.setStatusReporter(StatusReport.getInstance());
        d.setMessages(Messages.getInstance());
        Engine.init(d, InstrumentationRegistry.getInstrumentation().getTargetContext(), MalFactory.getInstance());
    }

    @AfterClass
    public static void tearDown() throws Exception {

    }

    @Test
    public void testBaseFuncs() throws Exception {
        // instantiate singleton
        EnvVarManager.getInstance();

        deleteAll();
        // set some values
        setEnvValue( "fruit", "apples" );
        setEnvValue( "fruit", "bananas" );
        setEnvValue( "transport", "bicycle" );
        setEnvValue( "flowers", "blue" );
        setEnvValue( "colours", "blue" );
        setEnvValue( "value", "123456789" );
        setEnvValue( "value", "" );
        setEnvValue( "name", "billybob" );
        deleteEnvValue( "colours" );
        deleteEnvValue( "fruity" );

        // read out all values
        List<EnvVar> list = getEnvVarDao().getAll();

//        for( EnvVar val : list ) {
//            Timber.i("Env var idx = " + val.getUid() + ", name = " + val.getName() + ", value = " + val.getValue() );
//        }

        // check expected values
        assertEquals( "fruit", list.get(0).getName() );
        assertEquals( "bananas", list.get(0).getValue() );

        assertEquals( "transport", list.get(1).getName() );
        assertEquals( "bicycle", list.get(1).getValue() );

        assertEquals( "flowers", list.get(2).getName() );
        assertEquals( "blue", list.get(2).getValue() );

        assertEquals( "value", list.get(3).getName() );
        assertEquals( "", list.get(3).getValue() );

        assertEquals( "name", list.get(4).getName() );
        assertEquals( "billybob", list.get(4).getValue() );
    }


    @Test
    public void testReceiptNumber() throws Exception {
        deleteAll();

        assertEquals( 1, (int)ReceiptNumber.getNewValue() );
        assertEquals( 2, (int)ReceiptNumber.getNewValue() );
        assertEquals( 3, (int)ReceiptNumber.getNewValue() );
    }

    @Test
    public void testAutoIncrement() throws Exception {
        deleteAll();

        assertEquals( 2, (int)getIntegerAutoIncrement( "TEST_VAL", 9 ) );
        assertEquals( 1, (int)getIntegerAutoIncrement( "TEST_VAL", 9 ) );
        assertEquals( 3, (int)getIntegerAutoIncrement( "TEST_VAL", 9 ) );
        assertEquals( 4, (int)getIntegerAutoIncrement( "TEST_VAL", 9 ) );
        assertEquals( 5, (int)getIntegerAutoIncrement( "TEST_VAL", 9 ) );
        assertEquals( 6, (int)getIntegerAutoIncrement( "TEST_VAL", 9 ) );
        assertEquals( 7, (int)getIntegerAutoIncrement( "TEST_VAL", 9 ) );
        assertEquals( 8, (int)getIntegerAutoIncrement( "TEST_VAL", 9 ) );
        assertEquals( 9, (int)getIntegerAutoIncrement( "TEST_VAL", 9 ) );
        assertEquals( 1, (int)getIntegerAutoIncrement( "TEST_VAL", 9 ) );
    }

    @Test
    public void testBooleans() throws Exception {
        deleteAll();

        // not present, should return false
        boolean val = EnvVar.getEnvValueBoolean( "TEST" );
        assertEquals( false, val );

        // set a couple of things, check them
        setEnvValue( "TEST", true );
        setEnvValue( "TEST1", false );
        setEnvValue( "TEST2", true );
        setEnvValue( "TEST3", false );

        assertEquals( true, EnvVar.getEnvValueBoolean( "TEST" ) );
        assertEquals( false, EnvVar.getEnvValueBoolean( "TEST1" ) );
        assertEquals( true, EnvVar.getEnvValueBoolean( "TEST2" ) );
        assertEquals( false, EnvVar.getEnvValueBoolean( "TEST3" ) );

    }


    @Test
    public void testStrings() throws Exception {
        deleteAll();

        // not present, should return empty string
        assertEquals( "", EnvVar.getEnvValueString( "TEST" ) );

        // set a couple of things, check them
        setEnvValue( "TEST", "the quick" );
        setEnvValue( "TEST1", "brown fox" );
        setEnvValue( "TEST2", "jumps over" );
        setEnvValue( "TEST3", "the lazy dogs" );

        assertEquals( "jumps over", EnvVar.getEnvValueString( "TEST2" ) );
        assertEquals( "brown fox", EnvVar.getEnvValueString( "TEST1" ) );
        assertEquals( "the lazy dogs", EnvVar.getEnvValueString( "TEST3" ) );
        assertEquals( "the quick", EnvVar.getEnvValueString( "TEST" ) );
    }

    @Test
    public void shouldNotUpdateBatchNumberWhenBatchNumberGreaterThan999() throws Exception {
        BatchNumber.setNewValue(Integer.valueOf("230507"));

        assertEquals(Integer.valueOf(1), BatchNumber.getCurValue());
    }

    @Test
    public void shouldUpdateBatchNumberWhenBatchNumber() throws Exception {
        BatchNumber.setNewValue(Integer.valueOf("500"));

        assertEquals(Integer.valueOf(500), BatchNumber.getCurValue());
    }
}
