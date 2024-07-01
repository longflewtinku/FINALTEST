package com.linkly.libpositivesvc.utils;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.io.File;

public class UtilsTest {

    @Test
    public void testFilesEqual() {
        File file1 = new File("src/test/res/file1.xml");
        File file2 = new File("src/test/res/file2_equal.xml");
        assertTrue(Utils.filesContentEqual(file1, file2));
    }


    @Test
    public void testFilesNotEqual() {
        File file1 = new File("src/test/res/file1.xml");
        File file2 = new File("src/test/res/file2_notEqual.xml");
        assertFalse(Utils.filesContentEqual(file1, file2));
    }

    @Test
    public void testFilesNotEqualNoFile() {
        File file1 = new File("src/test/res/file1.xml");
        File file2 = new File("nonexistentFile.xml");
        assertFalse(Utils.filesContentEqual(file1, file2));
    }


    @Test
    public void testFilesNotEqualNull() {
        assertFalse(Utils.filesContentEqual(null, null));
    }
}
