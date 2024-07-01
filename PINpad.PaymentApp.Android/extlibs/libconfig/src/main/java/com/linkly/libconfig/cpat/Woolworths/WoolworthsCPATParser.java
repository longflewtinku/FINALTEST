package com.linkly.libconfig.cpat.Woolworths;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

/**
 * This class will parse the CPAT file & generate a list of CPAT Entries
 * */
public class WoolworthsCPATParser {
    /**
     * Will contain a list of all {@link WoolworthsCPATEntry} after parsing
     * Will be immutable
     * */
    private final List<WoolworthsCPATEntry> woolworthsCPATEntries;
    /**
     * Contains the CPAT Version of the file among others
     */
    private final ProcessingParametersRecord processingParametersRecord;

    /**
     * Will initialize the List of CPAT Entries by parsing the CPAT file.
     * @param filePath CPAT file path
     * @throws FileNotFoundException if CPAT File doesn't exist
     * */
    public WoolworthsCPATParser( String filePath ) throws FileNotFoundException {
        final String END_MARKER = "999999999 99 9 999999999999 99";
        File myFile = new File( filePath );
        Scanner scanner = new Scanner( myFile ).useDelimiter( System.lineSeparator() );
        List<WoolworthsCPATEntry> woolworthsCPATEntryList = new ArrayList<>();
        // Detect if last not empty line
        String lastLine = "";

        while( scanner.hasNext() ){
            String line = scanner.next().trim();

            // Check if second-last line
            if( line.equals( END_MARKER ) ) {
                lastLine = scanner.next();
            } else {
                woolworthsCPATEntryList.add( new WoolworthsCPATEntry( line ) );
            }
        }

        this.woolworthsCPATEntries = Collections.unmodifiableList( woolworthsCPATEntryList );

        // Extract final line
        this.processingParametersRecord = new ProcessingParametersRecord( lastLine );
    }

    public List<WoolworthsCPATEntry> getWoolworthsCPATEntries() {
        return this.woolworthsCPATEntries;
    }

    public ProcessingParametersRecord getProcessingParametersRecord() {
        return this.processingParametersRecord;
    }
}
