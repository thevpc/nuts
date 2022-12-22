/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.core.test;

import net.thevpc.nuts.NutsMessage;
import net.thevpc.nuts.NutsNoSessionOptionalErrorException;
import net.thevpc.nuts.NutsOptional;
import net.thevpc.nuts.NutsSession;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;

/**
 * @author thevpc
 */
public class Test33_Optional {

    static NutsSession session;

    @BeforeAll
    public static void init() {
//        session = TestUtils.openNewMinTestWorkspace();
    }

    @Test
    public void test01() {

        {
            NutsOptional<String> a = NutsOptional.of("example");
            Assertions.assertEquals("example", a.get());
            Assertions.assertEquals(false, a.isEmpty());
            Assertions.assertEquals(false, a.isError());
            Assertions.assertEquals(true, a.isPresent());
            Assertions.assertEquals(false, a.isNotPresent());
        }
        {
            NutsOptional<String> a = NutsOptional.of(null);
            Assertions.assertThrows(NoSuchElementException.class, () -> a.get());
            Assertions.assertEquals(true, a.isEmpty());
            Assertions.assertEquals(false, a.isError());
            Assertions.assertEquals(false, a.isPresent());
            Assertions.assertEquals(true, a.isNotPresent());
        }

        {
            NutsOptional<String> a = NutsOptional.of("");
            a.get();
            Assertions.assertEquals(false, a.isEmpty());
            Assertions.assertEquals(false, a.isError());
            Assertions.assertEquals(true, a.isPresent());
            Assertions.assertEquals(false, a.isNotPresent());
        }

        {
            NutsOptional<String> a = NutsOptional.of("");
            a.get();
            Assertions.assertEquals(false, a.isEmpty());
            Assertions.assertEquals(false, a.isError());
            Assertions.assertEquals(true, a.isPresent());
            Assertions.assertEquals(false, a.isNotPresent());

            NutsOptional<String> b=a.ifBlankEmpty();
            Assertions.assertThrows(NoSuchElementException.class, () -> b.get());
            Assertions.assertEquals(true, b.isEmpty());
            Assertions.assertEquals(false, b.isError());
            Assertions.assertEquals(false, b.isPresent());
            Assertions.assertEquals(true, b.isNotPresent());
        }

        {
            NutsOptional<String> a = NutsOptional.ofError(s-> NutsMessage.ofPlain("error"));
            Assertions.assertThrows(NutsNoSessionOptionalErrorException.class, () -> a.get());
            Assertions.assertEquals(false, a.isEmpty());
            Assertions.assertEquals(true, a.isError());
            Assertions.assertEquals(false, a.isPresent());
            Assertions.assertEquals(true, a.isNotPresent());
        }
    }

}
