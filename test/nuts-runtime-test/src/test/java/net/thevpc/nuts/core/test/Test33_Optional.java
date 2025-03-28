/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.core.test;

import net.thevpc.nuts.NNoSuchElementException;
import net.thevpc.nuts.core.test.utils.TestUtils;
import net.thevpc.nuts.reserved.optional.NDetachedEmptyOptionalException;
import net.thevpc.nuts.reserved.optional.NDetachedErrorOptionalException;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.NNoSessionOptionalErrorException;
import net.thevpc.nuts.util.NOptional;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.util.NOptionalErrorException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;

/**
 * @author thevpc
 */
public class Test33_Optional {

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
            Assertions.assertThrows(NDetachedEmptyOptionalException.class, () -> a.get());
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

            NOptional<String> b=a.ifBlankEmpty();
            Assertions.assertThrows(NDetachedEmptyOptionalException.class, () -> b.get());
            Assertions.assertEquals(true, b.isEmpty());
            Assertions.assertEquals(false, b.isError());
            Assertions.assertEquals(false, b.isPresent());
            Assertions.assertEquals(true, b.isNotPresent());
        }

        {
            NOptional<String> a = NOptional.ofError(NMsg.ofPlain("error"));
            Assertions.assertThrows(NDetachedErrorOptionalException.class, () -> a.get());
            Assertions.assertEquals(false, a.isEmpty());
            Assertions.assertEquals(true, a.isError());
            Assertions.assertEquals(false, a.isPresent());
            Assertions.assertEquals(true, a.isNotPresent());
        }
    }

}
