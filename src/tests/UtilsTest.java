package tests;

import net.fhannes.creepy.URLUtils;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;

/**
 */
public class UtilsTest {

    @Test
    public void testNormalizeURL_EmptyString() {
        assertEquals("", URLUtils.normalizeURL(""));
    }

    @Test
    public void testNormalizeURL_InvalidURL() {
        assertEquals("", URLUtils.normalizeURL("http:/test.com"));
    }

    @Test
    public void testNormalizeURL_LowerCase() {
        assertEquals("http://example.com/index.php", URLUtils.normalizeURL("http://EXamPlE.cOm/index.php"));
    }

    @Test
    public void testNormalizeURL_Params() {
        assertEquals("", URLUtils.normalizeURL("http://EXamPlE.cOm/index.php?test=value"));
    }

}
