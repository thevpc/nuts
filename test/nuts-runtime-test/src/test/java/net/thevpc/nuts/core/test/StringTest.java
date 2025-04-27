/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.core.test;

import net.thevpc.nuts.util.NOptional;
import net.thevpc.nuts.io.NTerminalMode;
import net.thevpc.nuts.runtime.standalone.xtra.expr.StringPlaceHolderParser;
import net.thevpc.nuts.util.NNameFormat;
import net.thevpc.nuts.util.NStringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.function.Function;

/**
 *
 * @author thevpc
 */
public class StringTest {

    @BeforeAll
    public static void init() {
    }

    @Test
    public void test01() {
        String q = StringPlaceHolderParser.replaceDollarPlaceHolders("A$B.C ${B}A ${H:hello}", new Function<String, String>() {
            @Override
            public String apply(String s) {
                switch (s) {
                    case "B":
                        return "X";
                }
                return null;
            }
        });

        Assertions.assertEquals("AX.C XA hello",q);
    }
   @Test
    public void test02() {
       Assertions.assertEquals(
               Arrays.asList("",""),
               NStringUtils.split(",", ",", true, false)
       );
    }
   @Test
    public void test03() {
       NOptional<NTerminalMode> a = NTerminalMode.parse("never");
       a.get();
   }


    @Test
    public void test04() {
        String[] a = NNameFormat.parse("aBc_r");
        Assertions.assertArrayEquals(
                new String[]{"a","Bc","r"},
                a
        );
    }
    @Test
    public void test05() {
        String[] a = NNameFormat.parse("setName");
        Assertions.assertArrayEquals(
                new String[]{"set","Name"},
                a
        );
    }
    @Test
    public void test06() {
        String[] a = NNameFormat.parse("setTHEName");
        Assertions.assertArrayEquals(
                new String[]{"set","THEName"},
                a
        );
    }

    @Test
    public void test07() {
        String[] a = NNameFormat.parse("/a/dAt");
        Assertions.assertArrayEquals(
                new String[]{"a","d","At"},
                a
        );
    }

    @Test
    public void test08() {
        String[] a = NNameFormat.parse(".Net");
        Assertions.assertArrayEquals(
                new String[]{"Net"},
                a
        );
    }
}
