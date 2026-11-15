package net.thevpc.nuts.core.test;

import net.thevpc.nuts.text.NMsg;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.text.MessageFormat;
import java.util.Date;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Regression tests for the {@link NMsg#formatAsJSimple} fast path used by
 * {@code NMsg.ofJ(...)}.
 */
public class NMsgJFormatTest {

    private Locale oldLocale;

    @BeforeEach
    public void setUp() {
        oldLocale = Locale.getDefault();
        Locale.setDefault(Locale.US);
    }

    @AfterEach
    public void tearDown() {
        Locale.setDefault(oldLocale);
    }

    private static void assertSameAsMessageFormat(String pattern, Object... args) {
        assertEquals(MessageFormat.format(pattern, args), NMsg.ofJ(pattern, args).toString());
    }

    @Test
    public void stringsAndIndexes() {
        assertEquals("hello world", NMsg.ofJ("hello {0}", "world").toString());
        assertEquals("b a", NMsg.ofJ("{1} {0}", "a", "b").toString());
        assertEquals("x x", NMsg.ofJ("{0} {0}", "x").toString());
        assertEquals("plain text", NMsg.ofJ("plain text").toString());
        assertEquals("a b", NMsg.ofJ("{} {}", "a", "b").toString());
    }

    @Test
    public void numbersAndSimpleValues() {
        assertEquals("n=1,234", NMsg.ofJ("n={0}", 1234).toString());
        assertEquals("v=null", NMsg.ofJ("v={0}", (Object) null).toString());
        assertEquals("v=true", NMsg.ofJ("v={0}", true).toString());
        assertEquals("v=x", NMsg.ofJ("v={0}", 'x').toString());
        assertEquals("v=1,234.5", NMsg.ofJ("v={0}", 1234.5).toString());
    }

    @Test
    public void fallbackCasesStillMatchMessageFormat() {
        assertSameAsMessageFormat("it's {0}", "x");
        assertSameAsMessageFormat("{0,number}", 1234);
        assertSameAsMessageFormat("{0}", new Date(0L));
        assertSameAsMessageFormat("{0} }", "x");
    }
}
