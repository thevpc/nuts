package net.thevpc.nuts.runtime.standalone.collections;

import net.thevpc.nuts.util.NMatchType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class DefaultNCharQueueTest {

    @Test
    void testSkipValueDoesNotOverrunTheLiveWindow() {
        DefaultNCharQueue q = new DefaultNCharQueue(8);
        q.write("abcdef");
        q.skip(2);

        assertEquals(NMatchType.FULL_MATCH, q.skipValue("cd"));
        // only "ef" is available, "efg" is a partial value and must not read past the window
        assertEquals(NMatchType.PARTIAL_MATCH, q.skipValue("efg"));
        assertEquals(NMatchType.NO_MATCH, q.skipValue("zz"));
        assertEquals(NMatchType.FULL_MATCH, q.skipValue("ef"));
        assertEquals(0, q.length());
    }

    @Test
    void testSkipValueAtTheEndOfTheWindowIsAFullMatch() {
        DefaultNCharQueue q = new DefaultNCharQueue(8);
        q.write("abc");

        assertEquals(NMatchType.FULL_MATCH, q.skipValue("abc"));
        assertEquals(0, q.length());
    }

    @Test
    void testReadNewLineWaitsForTheSecondHalfOfCrLf() {
        DefaultNCharQueue q = new DefaultNCharQueue(8);
        q.write("\r");

        assertNull(q.readNewLine(false));
        assertEquals(1, q.length());

        q.write("\n");
        assertEquals("\r\n", q.readNewLine(false));
    }
}
