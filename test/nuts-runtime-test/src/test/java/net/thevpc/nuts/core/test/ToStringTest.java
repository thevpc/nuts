package net.thevpc.nuts.core.test;

import net.thevpc.nuts.core.test.utils.TestUtils;
import net.thevpc.nuts.util.NMaps;
import net.thevpc.nuts.util.NToStringBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ToStringTest {

    @Test
    public void test1() {
        //TestUtils.println(new NToStringBuilder("hello").add("a","b").toString());
        NToStringBuilder r = NToStringBuilder.of("hello").add("a", NMaps.of("a\nb", "a\nb"));
        String a = r.toString();
        String b = r.toString();
        TestUtils.println(a);
        TestUtils.println(b);
        Assertions.assertTrue(true);
    }
}
