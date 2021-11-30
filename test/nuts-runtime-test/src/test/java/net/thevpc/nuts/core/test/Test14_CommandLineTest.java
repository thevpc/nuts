/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.core.test;

import net.thevpc.nuts.*;
import net.thevpc.nuts.core.test.utils.TestUtils;
import net.thevpc.nuts.runtime.standalone.app.cmdline.DefaultNutsArgument;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 *
 * @author thevpc
 */
public class Test14_CommandLineTest {
    static NutsSession session;

    @BeforeAll
    public static void init() {
        session = TestUtils.openNewMinTestWorkspace();
    }


    @Test
    public void test1() throws Exception {
        NutsArgument[] cmd = NutsCommandLine.of("-ad+ +ad--",session).toArgumentArray();
        Set<String> set = Arrays.stream(cmd).map(x -> x.toString()).collect(Collectors.toSet());
        Set<String> expectedSet = new HashSet<>(Arrays.asList(
                "-a", "-d+", "+a","+d--"
        ));
        Assertions.assertEquals(set,expectedSet);
    }



    @Test
    public void testArgument01() {
        NutsElements elems = NutsElements.of(session);
        checkDefaultNutsArgument(
                new DefaultNutsArgument(null,elems),
                true,
                false,
                false,
                false,
                null,
                null,
                null,
                null,
                "="
        );
    }

    @Test
    public void testArgument02() {
        NutsElements elems = NutsElements.of(session);
        checkDefaultNutsArgument(
                new DefaultNutsArgument("",elems),
                true,
                false,
                false,
                false,
                "",
                null,
                null,
                null,
                "="
        );
    }

    @Test
    public void testArgument03() {
        NutsElements elems = NutsElements.of(session);
        checkDefaultNutsArgument(
                new DefaultNutsArgument("hello",elems),
                true,
                false,
                false,
                false,
                "hello",
                null,
                null,
                null,
                "="
        );
    }

    @Test
    public void testArgument04() {
        NutsElements elems = NutsElements.of(session);
        checkDefaultNutsArgument(
                new DefaultNutsArgument("!hello",elems),
                true,
                false,
                false,
                false,
                "!hello",
                null,
                null,
                null,
                "="
        );
    }

    @Test
    public void testArgument05() {
        NutsElements elems = NutsElements.of(session);
        checkDefaultNutsArgument(
                new DefaultNutsArgument("//!hello",elems),
                true,
                false,
                false,
                false,
                "//!hello",
                null,
                null,
                null,
                "="
        );
    }

    @Test
    public void testArgument06() {
        NutsElements elems = NutsElements.of(session);
        checkDefaultNutsArgument(
                new DefaultNutsArgument("/!hello",elems),
                true,
                false,
                false,
                false,
                "/!hello",
                null,
                null,
                null,
                "="
        );
    }

    @Test
    public void testArgument07() {
        NutsElements elems = NutsElements.of(session);
        checkDefaultNutsArgument(
                new DefaultNutsArgument("/!hello=me",elems),
                true,
                false,
                false,
                false,
                "/!hello=me",
                null,
                null,
                null,
                "="
        );
    }

    @Test
    public void testArgument08() {
        NutsElements elems = NutsElements.of(session);
        checkDefaultNutsArgument(
                new DefaultNutsArgument("--!hello=me",elems),
                true,
                true,
                true,
                true,
                "--hello",
                "me",
                "hello",
                "--",
                "="
        );
    }

    @Test
    public void testArgument09() {
        NutsElements elems = NutsElements.of(session);
        checkDefaultNutsArgument(
                new DefaultNutsArgument("--//!hello=me",elems),
                false,
                true,
                true,
                true,
                "--hello",
                "me",
                "hello",
                "--",
                "="
        );
    }


    @Test
    public void testArgument10() {
        NutsElements elems = NutsElements.of(session);
        checkDefaultNutsArgument(
                new DefaultNutsArgument("--//=",elems),
                false,
                true,
                true,
                false,
                "--",
                "",
                "",
                "--",
                "="
        );
    }

    private static void checkDefaultNutsArgument(NutsArgument a, boolean active, boolean option, boolean keyValue, boolean negated
            , String key
            , String value
            , String optionName
            , String optionPrefix
            , String eq
    ){
        Assertions.assertEquals(option,a.isOption(),"Option:"+a.getString());
        Assertions.assertEquals(active,a.isActive(),"Enabled:"+a.getString());
        Assertions.assertEquals(keyValue,a.isKeyValue(),"KeyValue:"+a.getString());
        Assertions.assertEquals(negated,a.isNegated(),"Negated:"+a.getString());
        Assertions.assertEquals(key,a.getKey().getString(),"StringKey:"+a.getString());
        Assertions.assertEquals(value,a.getValue().getString(),"StringValue:"+a.getString());
        Assertions.assertEquals(optionName,a.getOptionName().getString(),"StringOptionName:"+a.getString());
        Assertions.assertEquals(optionPrefix,a.getOptionPrefix().getString(),"StringOptionPrefix:"+a.getString());
        Assertions.assertEquals(eq,a.getSeparator(),"KeyValueSeparator:"+a.getString());
        TestUtils.println("OK : "+a.getString());
    }
}
