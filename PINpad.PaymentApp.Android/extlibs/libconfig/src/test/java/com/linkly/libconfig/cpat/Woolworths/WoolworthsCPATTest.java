package com.linkly.libconfig.cpat.Woolworths;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import org.junit.Rule;
import org.junit.Test;
import org.junit.function.ThrowingRunnable;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import timber.log.Timber;

public class WoolworthsCPATTest {
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();
    private static final String FILE_NAME = "WW_CPAT.txt";


    /**
     * Creates a dummy file in users' Temporary file path
     * @param txtToWrite Text to be contained in the file
     * */
    private void createFile( String txtToWrite ) {
        try {
            FileWriter myWriter = new FileWriter( new File(
                    temporaryFolder.getRoot(),
                    FILE_NAME
            ).getAbsoluteFile() );
            myWriter.write( txtToWrite );
            myWriter.close();
            System.out.println( "Successfully wrote to the file." );
        } catch ( IOException e ) {
            System.out.println( "An error occurred." );
            Timber.w(e);
        }
    }

    /**
     * The Path separators are determined based on what OS the method runs on.
     * @return path of the file.
     * */
    private String filePath() {
        Path path = Paths.get( temporaryFolder.getRoot().getAbsolutePath(), FILE_NAME );
        return path.toString();
    }

    @Test
    public void fileNotFoundTest() {
        assertThrows( FileNotFoundException.class, new ThrowingRunnable() {
            @Override
            public void run() throws Throwable {
                new WoolworthsCPATParser( filePath() );
            }
        } );
    }

    @Test
    public void readMultipleLines() throws FileNotFoundException {
        this.createFile( "9FFFFFFFF 19 0 805000000300 31" + System.lineSeparator() + 
                "999999999 99 9 999999999999 99" + System.lineSeparator() + 
                "000021142 18 0 009002009641 99" );

        WoolworthsCPATParser woolworthsCPAT = new WoolworthsCPATParser( filePath() );
        assertEquals( "000021142", woolworthsCPAT.getProcessingParametersRecord().getVersionNumber() );
    }
}