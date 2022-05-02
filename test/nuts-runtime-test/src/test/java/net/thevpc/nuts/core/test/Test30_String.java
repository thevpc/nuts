/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.core.test;

import net.thevpc.nuts.reserved.NutsReservedStringUtils;
import net.thevpc.nuts.runtime.standalone.xtra.expr.StringPlaceHolderParser;
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

}
