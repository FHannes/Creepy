package tests;

import net.fhannes.creepy.CreepyURL;
import org.junit.Test;

import static junit.framework.Assert.*;

/**
 */
public class CreepyURLTest {

    @Test
    public void testToString_EmptyString() {
        assertEquals("", new CreepyURL("").toString());
    }

    @Test
    public void testToString_InvalidURL() {
        assertEquals("", new CreepyURL("example.com").toString());
    }

    @Test
    public void testToString_LowerCase() {
        assertEquals("http://example.com/index.php", new CreepyURL("http://EXamPlE.cOm/index.php").toString());
    }

    @Test
    public void testHasParams_NoParams() throws CreepyURL.Exception {
        assertFalse(new CreepyURL("http://example.com/index.php").hasParams());
    }

    @Test
    public void testHasParams_WithParams() throws CreepyURL.Exception {
        assertTrue(new CreepyURL("http://example.com/index.php?action=view").hasParams());
    }

    @Test(expected = CreepyURL.Exception.class)
    public void testHasParams_Invalid() throws CreepyURL.Exception {
        new CreepyURL("").hasParams();
    }

}
