package net.thevpc.nuts.core.test;

import net.thevpc.nuts.lib.common.collections.NMaps;
import net.thevpc.nuts.lib.common.collections.NStringMap;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

public class Test52_NStringMap {

    @Test
    public void test1() {
        NStringMap<String> sm=new NStringMap<>(new HashMap<>(),'.' );
        sm.put("a.b.c","1");
        sm.put("a.b.d","2");
        sm.put("a.c.e","3");
        Assertions.assertEquals(
                new HashSet<>(Arrays.asList("c","d")),
                sm.nextKeys("a.b")
        );

        Assertions.assertEquals(
                NMaps.of("c","1","d","2"),
                sm.toMap("a.b")
        );

        Assertions.assertEquals(
                NMaps.of("a.c.e","3"),
                sm.copy().removeAll("a.b").toMap()
        );

        Assertions.assertEquals(
                NMaps.of(
                        "a.b.c","2",
                        "a.b.d","3",
                        "a.c.e","3"
                ),
                sm.copy().putAll("a.b",NMaps.of("c","2","d","3")).toMap()
        );

    }
}
