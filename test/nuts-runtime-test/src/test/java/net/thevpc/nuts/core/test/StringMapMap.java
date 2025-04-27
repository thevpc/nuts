package net.thevpc.nuts.core.test;

import net.thevpc.nuts.util.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.*;

public class StringMapMap {

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

    @Test
    public void test01() {
        NStringMapFormat t = NStringMapFormatBuilder.of().setEqualsChars("=").setQuoteSupported(true).build();
        NOptional<Map<String, List<String>>> u = t.parseDuplicates("src=\"https://protos-erp.com/assets/crm/protos-banner-fr.png\"\n" +
                "        tf=\"p\" tn=\"banner-fr.png\" trackerSecretEmail=\"${email}\" tsc=\"${company}\"\n" +
                "        alt=\"Banner 2024\" style=\"outline:none;text-decoration:none;border:none;max-width:100%;font-size:16px;border-radius: 25px;\" width=\"560\" align=\"middle\"/");
//        NOptional<Map<String, List<String>>> u = t.parseDuplicates("src=\"https\"\n");
        System.out.println(u);
    }

    @Test
    public void test02() {
        Map<String, String> u = NStringMapFormat.DEFAULT.parse("c=52&s=17.0").get();
//        NOptional<Map<String, List<String>>> u = t.parseDuplicates("src=\"https\"\n");
        System.out.println(u);
    }

}
