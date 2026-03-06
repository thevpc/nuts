/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.core.test;

import net.thevpc.nuts.core.test.utils.TestUtils;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.NEmptyOptionalException;
import net.thevpc.nuts.util.NErrorOptionalException;
import net.thevpc.nuts.util.NOptional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * @author thevpc
 */
public class OptionalTest {

    @BeforeAll
    public static void init() {
        TestUtils.openNewMinTestWorkspace();
    }

    @Test
    public void test01() {

        {
            NOptional<String> a = NOptional.of("example");
            Assertions.assertEquals("example", a.get());
            Assertions.assertEquals(false, a.isEmpty());
            Assertions.assertEquals(false, a.isError());
            Assertions.assertEquals(true, a.isPresent());
            Assertions.assertEquals(false, a.isNotPresent());
        }
        {
            NOptional<String> a = NOptional.of(null);
            Assertions.assertThrows(NEmptyOptionalException.class, () -> a.get());
            Assertions.assertEquals(true, a.isEmpty());
            Assertions.assertEquals(false, a.isError());
            Assertions.assertEquals(false, a.isPresent());
            Assertions.assertEquals(true, a.isNotPresent());
        }

        {
            NOptional<String> a = NOptional.of("");
            a.get();
            Assertions.assertEquals(false, a.isEmpty());
            Assertions.assertEquals(false, a.isError());
            Assertions.assertEquals(true, a.isPresent());
            Assertions.assertEquals(false, a.isNotPresent());
        }

        {
            NOptional<String> a = NOptional.of("");
            a.get();
            Assertions.assertEquals(false, a.isEmpty());
            Assertions.assertEquals(false, a.isError());
            Assertions.assertEquals(true, a.isPresent());
            Assertions.assertEquals(false, a.isNotPresent());

            NOptional<String> b=a.onBlankEmpty();
            Assertions.assertThrows(NEmptyOptionalException.class, () -> b.get());
            Assertions.assertEquals(true, b.isEmpty());
            Assertions.assertEquals(false, b.isError());
            Assertions.assertEquals(false, b.isPresent());
            Assertions.assertEquals(true, b.isNotPresent());
        }

        {
            NOptional<String> a = NOptional.ofError(NMsg.ofPlain("error"));
            Assertions.assertThrows(NErrorOptionalException.class, () -> a.get());
            Assertions.assertEquals(false, a.isEmpty());
            Assertions.assertEquals(true, a.isError());
            Assertions.assertEquals(false, a.isPresent());
            Assertions.assertEquals(true, a.isNotPresent());
        }
    }

}
