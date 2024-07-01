package com.linkly.libpositivesvc.paxstore;

import static com.linkly.libpositivesvc.paxstore.DownloadParamService.getNewFiles;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.linkly.libmal.IMalFile;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

// Unit Tests for some of the static functions that are specific for service
public class DownloadParamServiceTests {

    IMalFile file;

    @Test
    public void testNoRebootRequiredAllMatchingFiles() {
        file = mock(IMalFile.class);
        when(file.getWorkingDir()).thenReturn("src/test/res/terminal_files_structure/files");
        when(file.getCommonDir()).thenReturn("src/test/res/terminal_files_structure/files/EFT");

        List<String> noRebootRequiredFiles = Arrays.asList("hotloadparams.xml");

        assertFalse(DownloadParamService.requiresReboot(file, getNewFiles(file), noRebootRequiredFiles));
    }


    @Test
    public void testRebootRequiredDifferenceInFile() {
        file = mock(IMalFile.class);
        when(file.getWorkingDir()).thenReturn("src/test/res/terminal_files_structure/files_not_equal");
        when(file.getCommonDir()).thenReturn("src/test/res/terminal_files_structure/files_not_equal/EFT");

        List<String> noRebootRequiredFiles = Arrays.asList("hotloadparams.xml");

        assertTrue(DownloadParamService.requiresReboot(file, getNewFiles(file), noRebootRequiredFiles));
    }

    @Test
    public void testRebootNotRequiredDifferenceInFileOnlyInHotload() {
        file = mock(IMalFile.class);
        when(file.getWorkingDir()).thenReturn("src/test/res/terminal_files_structure/files_not_equal_but_no_reboot_required");
        when(file.getCommonDir()).thenReturn("src/test/res/terminal_files_structure/files_not_equal_but_no_reboot_required/EFT");

        List<String> noRebootRequiredFiles = Arrays.asList("hotloadparams.xml");

        assertFalse(DownloadParamService.requiresReboot(file, getNewFiles(file), noRebootRequiredFiles));
    }

}
