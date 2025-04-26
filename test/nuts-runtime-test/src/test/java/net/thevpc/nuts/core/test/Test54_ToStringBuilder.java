package net.thevpc.nuts.core.test;

import net.thevpc.nuts.util.NMaps;
import net.thevpc.nuts.util.NToStringBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class Test54_ToStringBuilder {

    @Test
    public void test1() {
        //System.out.println(new NToStringBuilder("hello").add("a","b").toString());
        NToStringBuilder r = new NToStringBuilder("hello").add("a", NMaps.of("a\nb", "a\nb"));
        String a = r.toString();
        String b = r.toString();
        System.out.println(a);
        System.out.println(b);
        Assertions.assertTrue(true);
    }
}
