/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.core.test;

import net.thevpc.nuts.NutsOptional;
import net.thevpc.nuts.io.NutsTerminalMode;
import net.thevpc.nuts.reserved.NutsReservedStringUtils;
import net.thevpc.nuts.runtime.standalone.xtra.expr.StringPlaceHolderParser;
import net.thevpc.nuts.util.NutsNameFormat;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.function.Function;

/**
 *
 * @author thevpc
 */
public class Test30_String {

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
               NutsReservedStringUtils.split(",", ",", true, false)
       );
    }
   @Test
    public void test03() {
       NutsOptional<NutsTerminalMode> a = NutsTerminalMode.parse("never");
       a.get();
   }


    @Test
    public void test04() {
        String[] a = NutsNameFormat.parseName("aBc_r");
        Assertions.assertArrayEquals(
                new String[]{"a","Bc","r"},
                a
        );
    }
    @Test
    public void test05() {
        String[] a = NutsNameFormat.parseName("setName");
        Assertions.assertArrayEquals(
                new String[]{"set","Name"},
                a
        );
    }
    @Test
    public void test06() {
        String[] a = NutsNameFormat.parseName("setTHEName");
        Assertions.assertArrayEquals(
                new String[]{"set","THEName"},
                a
        );
    }

    @Test
    public void test07() {
        String[] a = NutsNameFormat.parseName("/a/dAt");
        Assertions.assertArrayEquals(
                new String[]{"a","d","At"},
                a
        );
    }

    @Test
    public void test08() {
        String[] a = NutsNameFormat.parseName(".Net");
        Assertions.assertArrayEquals(
                new String[]{"Net"},
                a
        );
    }
}
