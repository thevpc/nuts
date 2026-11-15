package net.thevpc.nuts.core.test;

import net.thevpc.nuts.util.NStringMapFormat;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Regression tests for the tokenizer/parse fast paths of {@link NStringMapFormat}.
 * Expected values were cross-checked against the previous parser implementation.
 */
public class NStringMapFormatTest {

    private static Map<String, String> map(Object... kv) {
        LinkedHashMap<String, String> m = new LinkedHashMap<>();
        for (int i = 0; i < kv.length; i += 2) {
            m.put((String) kv[i], (String) kv[i + 1]);
        }
        return m;
    }

    @Test
    public void urlFormBasics() {
        assertEquals(map(), NStringMapFormat.DEFAULT.parse("").get());
        assertEquals(map("a", "b", "c", "d"), NStringMapFormat.DEFAULT.parse("a=b&c=d").get());
        assertEquals(map("a", null), NStringMapFormat.DEFAULT.parse("a=").get());
        assertEquals(map("a", "b c"), NStringMapFormat.DEFAULT.parse("a=b%20c").get());
        assertEquals(map("a", "b c"), NStringMapFormat.DEFAULT.parse("a=b+c").get());
        assertEquals(map("a", "b=c"), NStringMapFormat.DEFAULT.parse("a=b%3Dc").get());
    }

    @Test
    public void urlFormNullAndDuplicateKeys() {
        assertEquals(map(null, null, "a", "b"), NStringMapFormat.DEFAULT.parse("&a=b").get());
        assertEquals(map("a", null, "b", null), NStringMapFormat.DEFAULT.parse("a&&b").get());
        assertEquals(map("a", "c"), NStringMapFormat.DEFAULT.parse("a=b&a=c").get());

        Map<String, List<String>> duplicates = NStringMapFormat.DEFAULT.parseDuplicates("a=b&a=c").get();
        assertEquals(Arrays.asList("b", "c"), duplicates.get("a"));
    }

    @Test
    public void commaQuotedAndEscaped() {
        assertEquals(map("a", "b,c", "d", "e"), NStringMapFormat.COMMA_FORMAT.parse("a='b,c',d=e").get());
        assertEquals(map("a", "b,c", "d", "e"), NStringMapFormat.COMMA_FORMAT.parse("a=\"b,c\",d=e").get());
        assertEquals(map("a", "b,c"), NStringMapFormat.COMMA_FORMAT.parse("a=b\\,c").get());
        assertEquals(map("a", "b", "c", "d"), NStringMapFormat.COMMA_FORMAT.parse("a=b,c=d").get());
    }

    @Test
    public void httpHeader() {
        assertEquals(map("a", "b", "c", "d"), NStringMapFormat.HTTP_HEADER_FORMAT.parse("a=b;c=d").get());
    }

    @Test
    public void escapeAndEncoding() {
        // '?' is the default escape character and is consumed by the tokenizer
        assertEquals(map("a", "bcc"), NStringMapFormat.DEFAULT.parse("a=b?c").get());
        assertEquals(map("a", "b=c", "d", "e"), NStringMapFormat.DEFAULT.parse("a=b%3Dc&d=e").get());
    }
}
