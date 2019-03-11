package com.rarchives.ripme.tst;


import com.rarchives.ripme.utils.Utils;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import junit.framework.TestCase;


public class UtilsTest extends TestCase {
    public void testGetEXTFromMagic() {
        TestCase.assertEquals("jpeg", Utils.getEXTFromMagic(new byte[]{ -1, -40, -1, -37, 0, 0, 0, 0 }));
        TestCase.assertEquals("png", Utils.getEXTFromMagic(new byte[]{ -119, 80, 78, 71, 13, 0, 0, 0 }));
    }

    public void testStripURLParameter() {
        TestCase.assertEquals("http://example.tld/image.ext", Utils.stripURLParameter("http://example.tld/image.ext?param", "param"));
    }

    public void testShortenPath() {
        String path = "/test/test/test/test/test/test/test/test/";
        TestCase.assertEquals("/test/test1", Utils.shortenPath("/test/test1"));
        TestCase.assertEquals("/test/test/t...st/test/test", Utils.shortenPath(path));
    }

    public void testBytesToHumanReadable() {
        TestCase.assertEquals("10.00iB", Utils.bytesToHumanReadable(10));
        TestCase.assertEquals("1.00KiB", Utils.bytesToHumanReadable(1024));
        TestCase.assertEquals("1.00MiB", Utils.bytesToHumanReadable((1024 * 1024)));
        TestCase.assertEquals("1.00GiB", Utils.bytesToHumanReadable(((1024 * 1024) * 1024)));
    }

    public void testGetListOfAlbumRippers() throws Exception {
        assert !(Utils.getListOfAlbumRippers().isEmpty());
    }

    public void testGetByteStatusText() {
        TestCase.assertEquals("5%  - 500.00iB / 97.66KiB", Utils.getByteStatusText(5, 500, 100000));
    }

    public void testBetween() {
        TestCase.assertEquals(Arrays.asList(" is a "), Utils.between("This is a test", "This", "test"));
    }

    public void testShortenFileNameWindows() throws FileNotFoundException {
        String filename = "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff.png";
        // Test filename shortening for windows
        File f = Utils.shortenSaveAsWindows("D:/rips/test/reddit/deep", filename);
        TestCase.assertEquals(new File("D:/rips/test/reddit/deep/fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff.png"), f);
    }

    public void testSanitizeSaveAs() {
        TestCase.assertEquals("This is a _ !__ test", Utils.sanitizeSaveAs("This is a \" !<? test"));
    }
}
