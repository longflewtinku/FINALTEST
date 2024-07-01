package com.linkly.libpositivesvc.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;

import timber.log.Timber;

public class Utils {

    /***
     * Does binary file comparison.
     * Only checks the contents not meta data or file name.
     * @param oldFile file 1
     * @param newFile file 2
     * @return if all contents are equal then will return true.
     */
    public static boolean filesContentEqual(File oldFile, File newFile) {
        if (oldFile == null || newFile == null || oldFile.length() != newFile.length()) {
            return false;  // Different file sizes, files are not equal
        }

        try (FileInputStream fis1 = new FileInputStream(oldFile);
             FileInputStream fis2 = new FileInputStream(newFile)) {

            byte[] buffer1 = new byte[8192];
            byte[] buffer2 = new byte[8192];

            int bytesRead1, bytesRead2;

            do {
                bytesRead1 = fis1.read(buffer1);
                bytesRead2 = fis2.read(buffer2);

                if (bytesRead1 != bytesRead2 || !Arrays.equals(buffer1, buffer2)) {
                    return false;  // Bytes differ, files are not equal
                }

            } while (bytesRead1 != -1);

            return true;  // Files have the same content
        } catch(IOException e) {
            Timber.e("FileStream Reading Error");
            return false;
        }
    }
}
